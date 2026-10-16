package edu.ucmo.se4950.securestate.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory account store shared by all sessions. */
public class UserRepository {

    private final Map<String, UserCredentials> accounts = new ConcurrentHashMap<>();

    /** Demo accounts: name / password / MFA code. */
    public static UserRepository withDemoUsers() {
        UserRepository repo = new UserRepository();
        repo.add("alice", "alice123", true, "111111");   // Admin
        repo.add("dave", "dave123", true, "444444");     // second Admin: can unlock alice
        repo.add("bob", "bob123", false, "222222");      // Clerk
        repo.add("carol", "carol123", false, "333333");  // Clerk
        return repo;
    }

    public void add(String name, String password, boolean administrator, String mfaCode) {
        accounts.put(name, new UserCredentials(name, password, administrator, mfaCode));
    }

    Optional<UserCredentials> find(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(accounts.get(name));
    }

    boolean unlock(String name) {
        return find(name).map(UserCredentials::unlock).orElse(false);
    }

    List<UserSummary> summaries() {
        return accounts.values().stream()
                .map(UserCredentials::summary)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();
    }
}
