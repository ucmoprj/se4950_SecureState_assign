package edu.ucmo.se4950.securestate.core;

/**
 * A page (activity) of the user-level state machine.
 *
 * By default a page can switch to any other page, and page-specific actions
 * are rejected. Each concrete page overrides only what is different for it.
 */
abstract class UserState {

    void toViewing(UserFunctionsMachine m) { m.switchTo(new Viewing()); }
    void toEditing(UserFunctionsMachine m) { m.switchTo(new Editing()); }
    void toManagingUsers(UserFunctionsMachine m) { m.switchTo(new ManagingUsers()); }

    void saveDocument(UserFunctionsMachine m, String text) {
        m.log("[User] open the Editing page before saving");
    }

    void unlockUser(UserFunctionsMachine m, String username) {
        m.log("[User] open the Managing Users page before unlocking accounts");
    }

    abstract String pageName();
}
