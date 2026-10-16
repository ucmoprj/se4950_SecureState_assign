package edu.ucmo.se4950.securestate.core;

/**
 * The account has been locked after too many failed attempts.
 *
 * Every user-level request is denied. Unlike the slide example (one
 * ExampleSystem per user), a web session can be shared by different people,
 * so "logout" is allowed here to leave the locked screen. The lock itself is
 * stored on the account, so logging in again still ends up in Locked until an
 * administrator unlocks the account.
 */
class Locked extends SecurityState {

    private final UserCredentials user;

    Locked(UserCredentials user) { this.user = user; }

    @Override
    boolean logout(ExampleSystem ctx) {
        ctx.log("[SEC] leave locked screen (" + user.getName() + " stays locked) -> NotLoggedIn");
        ctx.changeState(new NotLoggedIn());
        return true;
    }
}
