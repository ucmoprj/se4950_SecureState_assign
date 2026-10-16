package edu.ucmo.se4950.securestate.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleSystemTest {

    private UserRepository users;
    private DocumentStore documents;
    private ExampleSystem sys;

    @BeforeEach
    void setUp() {
        users = UserRepository.withDemoUsers();
        documents = new DocumentStore();
        sys = new ExampleSystem(users, documents);
    }

    private String lastLog() {
        var lines = sys.logLines();
        return lines.get(lines.size() - 1);
    }

    @Test
    void startsNotLoggedInWithoutUserMachine() {
        assertEquals("NotLoggedIn", sys.securityStateName());
        assertNull(sys.currentPageName());
        assertNull(sys.documentTextIfPermitted());
    }

    @Test
    void pagesAreDeniedBeforeLogin() {
        sys.openViewing();
        assertTrue(lastLog().contains("DENIED"));
        assertEquals("NotLoggedIn", sys.securityStateName());
    }

    @Test
    void correctPasswordGoesToMfaPendingAndPagesStayDenied() {
        sys.login("bob", "bob123");
        assertEquals("MfaPending", sys.securityStateName());

        sys.openEditing();
        assertTrue(lastLog().contains("DENIED"));
        assertNull(sys.currentPageName(), "MFA must not be skippable");
    }

    @Test
    void clerkAfterMfaCanEditButNotManageUsers() {
        sys.login("bob", "bob123");
        sys.verifyMfa("222222");
        assertEquals("LoggedInClerk", sys.securityStateName());
        assertEquals("Viewing", sys.currentPageName());

        sys.openEditing();
        assertEquals("Editing", sys.currentPageName());

        sys.openManagingUsers();
        assertTrue(lastLog().contains("DENIED"));
        assertEquals("Editing", sys.currentPageName());
        assertTrue(sys.userListIfPermitted().isEmpty());
    }

    @Test
    void adminAfterMfaCanManageUsers() {
        sys.login("alice", "alice123");
        sys.verifyMfa("111111");
        assertEquals("LoggedInAdmin", sys.securityStateName());

        sys.openManagingUsers();
        assertEquals("Managing Users", sys.currentPageName());
        assertEquals(4, sys.userListIfPermitted().size());
    }

    @Test
    void secondAdminCanUnlockALockedAdmin() {
        failPasswordUntilLocked("alice");
        sys.logout();

        sys.login("dave", "dave123");
        sys.verifyMfa("444444");
        assertEquals("LoggedInAdmin", sys.securityStateName());
        sys.openManagingUsers();
        sys.unlockUser("alice");
        sys.logout();

        sys.login("alice", "alice123");
        assertEquals("MfaPending", sys.securityStateName());
    }

    @Test
    void loginIsDeniedWhileAlreadyLoggedIn() {
        sys.login("bob", "bob123");
        sys.verifyMfa("222222");
        sys.login("alice", "alice123");
        assertTrue(lastLog().contains("DENIED"));
        assertEquals("LoggedInClerk", sys.securityStateName());
    }

    @Test
    void wrongMfaReturnsToNotLoggedIn() {
        sys.login("bob", "bob123");
        sys.verifyMfa("000000");
        assertEquals("NotLoggedIn", sys.securityStateName());
    }

    private void failPasswordUntilLocked(String username) {
        for (int i = 0; i < UserCredentials.MAX_FAILED_ATTEMPTS; i++) sys.login(username, "wrong");
    }

    @Test
    void passwordAndMfaFailuresAddUpAndLockTheAccount() {
        for (int i = 0; i < UserCredentials.MAX_FAILED_ATTEMPTS - 1; i++) sys.login("carol", "wrong");
        assertEquals("NotLoggedIn", sys.securityStateName());

        sys.login("carol", "carol123");
        sys.verifyMfa("000000");
        assertEquals("Locked", sys.securityStateName());

        sys.openViewing();
        assertTrue(lastLog().contains("DENIED"));
    }

    @Test
    void lockSurvivesANewSessionEvenWithCorrectPassword() {
        failPasswordUntilLocked("carol");

        ExampleSystem newSession = new ExampleSystem(users, documents);
        newSession.login("carol", "carol123");
        assertEquals("Locked", newSession.securityStateName());
    }

    @Test
    void successfulMfaResetsTheFailureCounter() {
        for (int i = 0; i < UserCredentials.MAX_FAILED_ATTEMPTS - 1; i++) sys.login("carol", "wrong");
        sys.login("carol", "carol123");
        sys.verifyMfa("333333");
        sys.logout();

        sys.login("carol", "wrong");
        assertEquals("NotLoggedIn", sys.securityStateName(), "counter must restart after a successful login");
    }

    @Test
    void logoutResetsUserStateForTheNextUser() {
        sys.login("alice", "alice123");
        sys.verifyMfa("111111");
        sys.openManagingUsers();
        sys.logout();

        sys.login("bob", "bob123");
        sys.verifyMfa("222222");
        assertEquals("Viewing", sys.currentPageName(), "clerk must not inherit the admin page");
    }

    @Test
    void adminCanUnlockOnlyFromManagingUsersPage() {
        failPasswordUntilLocked("carol");
        sys.logout();

        sys.login("alice", "alice123");
        sys.verifyMfa("111111");
        sys.unlockUser("carol");                       // still on Viewing page
        assertTrue(lastLog().contains("open the Managing Users page"));

        sys.openManagingUsers();
        sys.unlockUser("carol");
        sys.logout();

        sys.login("carol", "carol123");
        assertEquals("MfaPending", sys.securityStateName());
    }

    @Test
    void savingRequiresEditingPage() {
        sys.login("bob", "bob123");
        sys.verifyMfa("222222");

        sys.saveDocument("changed");
        assertNotEquals("changed", sys.documentTextIfPermitted());

        sys.openEditing();
        sys.saveDocument("changed");
        assertEquals("changed", sys.documentTextIfPermitted());
    }
}
