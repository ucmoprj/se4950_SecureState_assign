package edu.ucmo.se4950.securestate.core;

/**
 * TODO (Part 1): Fully authenticated administrator.
 *
 * An administrator can do everything a clerk can do (see LoggedInClerk)
 * AND manage users (op2 in the lecture slides).
 *
 * Steps:
 *  1. Override logout(): log the logout, end the user session, and change the
 *     security state to NotLoggedIn. LoggedInClerk shows how.
 *  2. Override the permission methods an administrator is allowed to use
 *     so that they return true. Check SecurityState for the full list.
 *  3. In NotLoggedIn.login(), send administrators to this state instead of
 *     LoggedInClerk (hint: user.isAdministrator()).
 *
 * Remember: every event you do NOT override is denied by default.
 */
class LoggedInAdmin extends SecurityState {

    private final UserCredentials user;

    LoggedInAdmin(UserCredentials user) { this.user = user; }

    // TODO (Part 1): implement this state here.
}
