package edu.ucmo.se4950.securestate.core;

/**
 * TODO (Part 2): The password is correct, but the MFA code has not been verified yet.
 *
 * Steps:
 *  1. SecurityState: add a new security event, denied by default like the others:
 *         boolean verifyMfa(ExampleSystem ctx, String code)
 *  2. ExampleSystem.verifyMfa(): forward the request to the current security
 *     state, using the same style as login() and logout().
 *  3. NotLoggedIn.login(): when the password is correct, change the state to
 *     MfaPending instead of logging the user in. Move the "log in" code
 *     (reset failed attempts, choose LoggedInAdmin / LoggedInClerk, start the
 *     user session) into verifyMfa() of this class.
 *  4. In this class:
 *       - verifyMfa(): correct code -> log the user in (see step 3)
 *                      wrong code   -> registerFailure(ctx, user, "MFA")
 *                      (hint: user.checkMfaCode(code))
 *       - logout():    cancel the login and go back to NotLoggedIn
 *
 * Think: which events must stay denied while MFA is pending, and what do you
 * need to write to deny them?
 */
class MfaPending extends SecurityState {

    private final UserCredentials user;

    MfaPending(UserCredentials user) { this.user = user; }

    // TODO (Part 2): implement this state here.
}
