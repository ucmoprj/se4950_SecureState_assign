package edu.ucmo.se4950.securestate.core;

/**
 * Security state machine (left side of the Secure State pattern).
 *
 * A security state only DECIDES whether an event is allowed (true / false).
 * It never performs user-level functionality itself; ExampleSystem does that
 * through the UserFunctionsMachine only when the answer is true.
 *
 * Deny-by-default: every event returns false unless a concrete state
 * explicitly overrides it. A new state therefore starts with no permissions.
 */
abstract class SecurityState {

    // ---- security events (they change the security state) ----
    boolean login(ExampleSystem ctx, String username, String password) { return false; }
    boolean logout(ExampleSystem ctx) { return false; }
    // TODO (Part 2): add the verifyMfa event here.

    // ---- permissions for user-level functionality ----
    boolean view(ExampleSystem ctx) { return false; }         // op1: any authenticated user
    boolean edit(ExampleSystem ctx) { return false; }         // op1: any authenticated user
    boolean manageUsers(ExampleSystem ctx) { return false; }  // op2: administrator only

    String name() { return getClass().getSimpleName(); }

    /**
     * Shared handling of a failed password or MFA code.
     * The failure counter lives on the account (UserCredentials), not in a
     * state object, so failures from different states add up and the lock
     * survives a brand-new session.
     */
    static void registerFailure(ExampleSystem ctx, UserCredentials user, String what) {
        int failures = user.recordFailedAttempt();
        if (user.isLocked()) {
            ctx.log("[SEC] " + what + " failed (" + failures + "/" + UserCredentials.MAX_FAILED_ATTEMPTS
                    + ") -> Locked");
            ctx.changeState(new Locked(user));
        } else {
            ctx.log("[SEC] " + what + " failed (" + failures + "/" + UserCredentials.MAX_FAILED_ATTEMPTS
                    + ") -> NotLoggedIn");
            ctx.changeState(new NotLoggedIn());
        }
    }
}
