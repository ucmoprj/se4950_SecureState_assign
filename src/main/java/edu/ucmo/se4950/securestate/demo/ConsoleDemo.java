package edu.ucmo.se4950.securestate.demo;

import edu.ucmo.se4950.securestate.core.DocumentStore;
import edu.ucmo.se4950.securestate.core.ExampleSystem;
import edu.ucmo.se4950.securestate.core.UserRepository;

/**
 * Console version of the scenario (same style as the original assignment output).
 * Run this class directly from the IDE; no web server is needed.
 */
public class ConsoleDemo {

    public static void main(String[] args) {
        UserRepository users = UserRepository.withDemoUsers();
        ExampleSystem sys = new ExampleSystem(users, new DocumentStore());

        sys.openEditing();                 // denied: NotLoggedIn
        sys.login("bob", "bob123");        // -> MfaPending
        sys.openEditing();                 // denied: MfaPending (MFA cannot be skipped)
        sys.verifyMfa("222222");           // -> LoggedInClerk, page Viewing
        sys.openEditing();                 // allowed
        sys.openManagingUsers();           // denied: clerk
        sys.logout();

        sys.login("carol", "wrong");       // failure 1/2
        sys.login("carol", "wrong");       // failure 2/2 -> Locked
        sys.logout();                      // leave the locked screen

        sys.login("alice", "alice123");
        sys.verifyMfa("111111");           // -> LoggedInAdmin
        sys.openManagingUsers();           // allowed
        sys.unlockUser("carol");
        sys.logout();
    }
}
