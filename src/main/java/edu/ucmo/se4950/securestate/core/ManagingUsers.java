package edu.ucmo.se4950.securestate.core;

/**
 * List accounts and unlock locked ones.
 * This page does not check the role itself: reaching it at all requires the
 * security state to allow manageUsers(), which only LoggedInAdmin does.
 */
class ManagingUsers extends UserState {

    @Override
    void toManagingUsers(UserFunctionsMachine m) { m.log("[User] already on the Managing Users page"); }

    @Override
    void unlockUser(UserFunctionsMachine m, String username) {
        boolean done = m.users().unlock(username);
        m.log(done ? "[User] account '" + username + "' unlocked"
                   : "[User] no locked account named '" + username + "'");
    }

    @Override
    String pageName() { return "Managing Users"; }
}
