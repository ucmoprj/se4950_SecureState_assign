package edu.ucmo.se4950.securestate.core;

import java.util.function.Consumer;

/**
 * User-level functionality state machine (right side of the Secure State pattern).
 *
 * It knows nothing about roles or authentication: it simply performs page
 * transitions and page actions. Whether the caller is allowed to do so has
 * already been decided by the security state machine in ExampleSystem.
 *
 * The constructor is package-private, so only ExampleSystem can create it.
 */
final class UserFunctionsMachine {

    private final UserRepository users;
    private final DocumentStore documents;
    private final Consumer<String> log;
    private UserState state = new Viewing();

    UserFunctionsMachine(UserRepository users, DocumentStore documents, Consumer<String> log) {
        this.users = users;
        this.documents = documents;
        this.log = log;
    }

    void toViewing() { state.toViewing(this); }
    void toEditing() { state.toEditing(this); }
    void toManagingUsers() { state.toManagingUsers(this); }
    void saveDocument(String text) { state.saveDocument(this, text); }
    void unlockUser(String username) { state.unlockUser(this, username); }

    String pageName() { return state.pageName(); }

    // ---- used by the UserState classes ----

    void switchTo(UserState next) {
        log("[User] switch to " + next.pageName());
        state = next;
        log("[Current page] : " + state.pageName());
    }

    UserRepository users() { return users; }
    DocumentStore documents() { return documents; }
    void log(String line) { log.accept(line); }
}
