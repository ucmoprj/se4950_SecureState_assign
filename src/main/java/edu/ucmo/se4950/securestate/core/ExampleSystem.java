package edu.ucmo.se4950.securestate.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Security context of the Secure State pattern (slide 25).
 *
 * It owns the current SecurityState and acts as a proxy for the
 * UserFunctionsMachine: every user-level request is first checked by the
 * security state, and only then forwarded.
 *
 *     if (state.edit(this)) userMachine.toEditing();
 *
 * The user machine is private and is created only after full authentication,
 * so code outside this package has no way to reach it without passing the
 * security state machine.
 */
public class ExampleSystem {

    private static final int MAX_LOG_LINES = 200;

    private final UserRepository users;
    private final DocumentStore documents;
    private final List<String> log = new ArrayList<>();

    private SecurityState state = new NotLoggedIn();
    private UserFunctionsMachine userMachine;   // null unless fully authenticated
    private UserCredentials currentUser;        // null unless fully authenticated

    public ExampleSystem(UserRepository users, DocumentStore documents) {
        this.users = users;
        this.documents = documents;
        log("[Current State] : " + state.name());
    }

    // ===================== security events =====================

    public synchronized void login(String username, String password) {
        if (!state.login(this, username, password)) deny("login");
    }

    public synchronized void verifyMfa(String code) {
        // TODO (Part 2): forward to the security state, in the same style as login().
        deny("MFA verification");
    }

    public synchronized void logout() {
        if (!state.logout(this)) deny("logout");
    }

    // ============ user-level functionality (gated) ============

    public synchronized void openViewing() {
        if (state.view(this)) userMachine.toViewing();
        else deny("Viewing page");
    }

    public synchronized void openEditing() {
        if (state.edit(this)) userMachine.toEditing();
        else deny("Editing page");
    }

    public synchronized void saveDocument(String text) {
        if (state.edit(this)) userMachine.saveDocument(text);
        else deny("save document");
    }

    public synchronized void openManagingUsers() {
        if (state.manageUsers(this)) userMachine.toManagingUsers();
        else deny("Managing Users page");
    }

    public synchronized void unlockUser(String username) {
        if (state.manageUsers(this)) userMachine.unlockUser(username);
        else deny("unlock user");
    }

    // ================ read-only views for the UI ================

    public synchronized String securityStateName() { return state.name(); }

    /** Current page name, or null when no user session exists. */
    public synchronized String currentPageName() {
        return userMachine == null ? null : userMachine.pageName();
    }

    public synchronized String currentUserName() {
        return currentUser == null ? null : currentUser.getName();
    }

    public synchronized boolean isAuthenticated() { return userMachine != null; }

    /** Document text, only if the security state allows viewing. */
    public synchronized String documentTextIfPermitted() {
        return state.view(this) ? documents.getText() : null;
    }

    /** Account list, only if the security state allows user management. */
    public synchronized List<UserSummary> userListIfPermitted() {
        return state.manageUsers(this) ? users.summaries() : List.of();
    }

    public synchronized List<String> logLines() { return Collections.unmodifiableList(new ArrayList<>(log)); }

    // ======== package-private: used only by the state classes ========

    UserRepository users() { return users; }

    void changeState(SecurityState next) {
        state = next;
        log("[Current State] : " + state.name());
    }

    /** Only the security state machine creates the user-level machine (slide 20). */
    void startUserSession(UserCredentials user) {
        currentUser = user;
        userMachine = new UserFunctionsMachine(users, documents, this::log);
        log("[Current page] : " + userMachine.pageName());
    }

    /** Throw the user machine away, so the next user always starts on the Viewing page. */
    void endUserSession() {
        currentUser = null;
        userMachine = null;
    }

    void log(String line) {
        System.out.println(line);
        log.add(line);
        if (log.size() > MAX_LOG_LINES) log.remove(0);
    }

    private void deny(String request) {
        log("[SEC] DENIED: " + request + " (" + state.name() + ")");
    }
}
