package edu.ucmo.se4950.securestate.core;

import java.util.Optional;

/** Nobody is logged in. Only "login" is allowed. */
class NotLoggedIn extends SecurityState {

    @Override
    boolean login(ExampleSystem ctx, String username, String password) {
        Optional<UserCredentials> found = ctx.users().find(username);
        if (found.isEmpty()) {
            // Same message as a wrong password: do not reveal which usernames exist.
            ctx.log("[SEC] login failed: unknown user or wrong password");
            return true;
        }
        UserCredentials user = found.get();

        if (user.isLocked()) {
            ctx.log("[SEC] account '" + user.getName() + "' is locked -> Locked");
            ctx.changeState(new Locked(user));
        } else if (user.checkPassword(password)) {
            // TODO (Part 1): administrators must go to LoggedInAdmin.
            // TODO (Part 2): go to MfaPending first and move the log-in code below
            //                into MfaPending.verifyMfa().
            user.resetFailedAttempts();
            ctx.log("[SEC] login ok -> LoggedInClerk");
            ctx.changeState(new LoggedInClerk(user));
            ctx.startUserSession(user);
        } else {
            registerFailure(ctx, user, "password");
        }
        return true;
    }
}
