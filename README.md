# Secure State Pattern Demo (Login + MFA + Roles + Lockout)

SE 4950 - starter project. See the assignment document for your tasks.

## Prerequisites

- **JDK 17 or later** — check with `java -version`
- **Maven** — only needed if you use `mvn` or want to generate the Maven Wrapper (see below). Check with `mvn -version`.

## Maven vs. Maven Wrapper

You can build this project with an installed Maven (`mvn`) or with the Maven Wrapper (`mvnw`).

|                        | `mvn` (installed Maven)                     | `mvnw` (Maven Wrapper)                                        |
|------------------------|---------------------------------------------|---------------------------------------------------------------|
| Maven installation     | Required on every machine                   | Not required — downloaded automatically on first run          |
| Maven version          | Whatever version is installed locally       | Fixed by the project (`.mvn/wrapper/maven-wrapper.properties`) |
| Same build everywhere  | No — versions may differ between students   | Yes — everyone uses the same Maven version                    |
| First run              | Fast                                        | Slower (downloads Maven once into `~/.m2/wrapper`)             |
| Command (Windows)      | `mvn <goal>`                                | `.\mvnw <goal>` (PowerShell) or `mvnw <goal>` (cmd)            |
| Command (macOS/Linux)  | `mvn <goal>`                                | `./mvnw <goal>`                                               |
| Files in the project   | None                                        | `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`                           |

### Generating the Maven Wrapper

Run once in the project folder (requires an installed Maven):

```
mvn wrapper:wrapper
```

To pin a specific Maven version (in PowerShell, keep the quotes):

```
mvn wrapper:wrapper "-Dmaven=3.9.9"
```

This creates:

```
mvnw                                   <- script for macOS/Linux
mvnw.cmd                               <- script for Windows
.mvn/wrapper/maven-wrapper.properties  <- Maven version and download URL
```

Notes:
- Keep the `.mvn` folder when you zip or share the project. It starts with a dot, so it is easy to leave out, and `mvnw` does not work without it.
- On macOS/Linux you may need `chmod +x mvnw` once.

In the commands below, replace `mvn` with `.\mvnw` (Windows) or `./mvnw` (macOS/Linux) if you use the wrapper.

## Run

```
mvn spring-boot:run
```

Open http://localhost:8080

| User  | Password | MFA    | Role  |
|-------|----------|--------|-------|
| alice | alice123 | 111111 | Admin |
| dave  | dave123  | 444444 | Admin |
| bob   | bob123   | 222222 | Clerk |
| carol | carol123 | 333333 | Clerk |

Console version (no web server): run `edu.ucmo.se4950.securestate.demo.ConsoleDemo`.

Tests: `mvn test`

## Static analysis (SpotBugs + FindSecBugs)

The SpotBugs plugin, with the FindSecBugs security rules, is configured in `pom.xml`.
It runs only when you call it; normal builds are not affected.

| Command                          | Result                                                        |
|----------------------------------|---------------------------------------------------------------|
| `mvn compile spotbugs:gui`       | Opens a window listing every warning with its source line (recommended) |
| `mvn compile spotbugs:spotbugs`  | Writes the report to `target/spotbugsXml.xml`                 |
| `mvn compile site`               | Builds HTML reports; open `target/site/spotbugs.html` in a browser |
| `mvn compile spotbugs:check`     | Fails the build if any warning exists (for CI)               |

The first run downloads the plugins and takes longer.

## Structure

```
core/   pure Java - the Secure State pattern
  ExampleSystem          security context; gate: if (state.x(this)) userMachine.x();
  SecurityState          deny-by-default base class (all events return false)
    NotLoggedIn -> MfaPending -> LoggedInClerk / LoggedInAdmin,  Locked (2 failures)
  UserFunctionsMachine   user-level machine (created only after full authentication)
  UserState              Viewing / Editing / ManagingUsers
web/    thin Spring MVC + Thymeleaf layer (no authorization logic)
demo/   ConsoleDemo
```

## Security state permissions

| State         | login | verifyMfa | logout | view/edit (op1) | manageUsers (op2) |
|---------------|:-----:|:---------:|:------:|:---------------:|:-----------------:|
| NotLoggedIn   |   O   |     -     |   -    |        -        |         -         |
| MfaPending    |   -   |     O     | O (cancel) |     -        |         -         |
| LoggedInClerk |   -   |     -     |   O    |        O        |         -         |
| LoggedInAdmin |   -   |     -     |   O    |        O        |         O         |
| Locked        |   -   |     -     | O (leave)  |     -        |         -         |
