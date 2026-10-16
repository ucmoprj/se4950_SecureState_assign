package edu.ucmo.se4950.securestate.core;

/** Fully authenticated clerk: may view and edit, may NOT manage users. */
class LoggedInClerk extends SecurityState {

    private final UserCredentials user;

    LoggedInClerk(UserCredentials user) { this.user = user; }

    @Override
    boolean logout(ExampleSystem ctx) {
        ctx.log("[SEC] logout ok (" + user.getName() + ") -> NotLoggedIn");
        ctx.endUserSession();
        ctx.changeState(new NotLoggedIn());
        return true;
    }

    @Override
    boolean view(ExampleSystem ctx) { return true; }

    @Override
    boolean edit(ExampleSystem ctx) { return true; }

    // manageUsers() is not overridden -> denied by default.
}
