package edu.ucmo.se4950.securestate.core;

/** Read-only account information for the Managing Users page (no secrets). */
public record UserSummary(String name, String role, int failedAttempts, boolean locked) { }
