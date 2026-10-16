package edu.ucmo.se4950.securestate.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * An account. The failed-attempt counter and the lock flag live here, on the
 * account, so they are shared by every session of the same user.
 *
 * NOTE (demo simplification): a real system stores a salted password hash
 * (e.g. BCrypt) and a TOTP secret instead of a fixed MFA code.
 */
final class UserCredentials {

    static final int MAX_FAILED_ATTEMPTS = 2;   // the slide example uses 5

    private final String name;
    private final String password;
    private final boolean administrator;
    private final String mfaCode;
    private int failedAttempts;
    private boolean locked;

    UserCredentials(String name, String password, boolean administrator, String mfaCode) {
        this.name = name;
        this.password = password;
        this.administrator = administrator;
        this.mfaCode = mfaCode;
    }

    String getName() { return name; }
    boolean isAdministrator() { return administrator; }

    boolean checkPassword(String candidate) { return constantTimeEquals(password, candidate); }
    boolean checkMfaCode(String candidate) { return constantTimeEquals(mfaCode, candidate); }

    synchronized int recordFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) locked = true;
        return failedAttempts;
    }

    synchronized void resetFailedAttempts() { failedAttempts = 0; }

    synchronized boolean isLocked() { return locked; }

    synchronized boolean unlock() {
        if (!locked) return false;
        locked = false;
        failedAttempts = 0;
        return true;
    }

    synchronized UserSummary summary() {
        return new UserSummary(name, administrator ? "Admin" : "Clerk", failedAttempts, locked);
    }

    private static boolean constantTimeEquals(String expected, String candidate) {
        if (candidate == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                     candidate.getBytes(StandardCharsets.UTF_8));
    }
}
