package edu.ucmo.se4950.securestate.web;

import edu.ucmo.se4950.securestate.core.DocumentStore;
import edu.ucmo.se4950.securestate.core.ExampleSystem;
import edu.ucmo.se4950.securestate.core.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thin web layer. It contains NO authorization logic: every request is simply
 * forwarded to the ExampleSystem of the current HTTP session, and the security
 * state machine decides. (This controller is in another package, so it cannot
 * touch the user machine or the state objects even if it wanted to.)
 */
@Controller
public class DemoController {

    private static final String SESSION_KEY = "exampleSystem";

    private final UserRepository users;
    private final DocumentStore documents;

    public DemoController(UserRepository users, DocumentStore documents) {
        this.users = users;
        this.documents = documents;
    }

    /** One ExampleSystem (one pair of state machines) per HTTP session. */
    private ExampleSystem system(HttpSession session) {
        ExampleSystem sys = (ExampleSystem) session.getAttribute(SESSION_KEY);
        if (sys == null) {
            sys = new ExampleSystem(users, documents);
            session.setAttribute(SESSION_KEY, sys);
        }
        return sys;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        ExampleSystem sys = system(session);
        model.addAttribute("securityState", sys.securityStateName());
        model.addAttribute("page", sys.currentPageName());
        model.addAttribute("userName", sys.currentUserName());
        model.addAttribute("documentText", sys.documentTextIfPermitted());
        model.addAttribute("accounts", sys.userListIfPermitted());
        List<String> newestFirst = new ArrayList<>(sys.logLines());
        Collections.reverse(newestFirst);
        model.addAttribute("log", newestFirst);
        model.addAttribute("securityStates",
                List.of("NotLoggedIn", "MfaPending", "LoggedInClerk", "LoggedInAdmin", "Locked"));
        model.addAttribute("pages", List.of("Viewing", "Editing", "Managing Users"));
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        system(session).login(username.trim(), password);
        return "redirect:/";
    }

    @PostMapping("/mfa")
    public String mfa(@RequestParam String code, HttpServletRequest request) {
        ExampleSystem sys = system(request.getSession());
        sys.verifyMfa(code.trim());
        // New session id once fully authenticated (prevents session fixation).
        if (sys.isAuthenticated()) request.changeSessionId();
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        system(request.getSession()).logout();
        request.changeSessionId();
        return "redirect:/";
    }

    /** Routing only (page name -> method). The permission check happens inside ExampleSystem. */
    @PostMapping("/page/{page}")
    public String page(@PathVariable String page, HttpSession session) {
        ExampleSystem sys = system(session);
        switch (page) {
            case "viewing" -> sys.openViewing();
            case "editing" -> sys.openEditing();
            case "managing-users" -> sys.openManagingUsers();
            default -> { }
        }
        return "redirect:/";
    }

    @PostMapping("/document")
    public String saveDocument(@RequestParam String text, HttpSession session) {
        system(session).saveDocument(text);
        return "redirect:/";
    }

    @PostMapping("/users/unlock")
    public String unlock(@RequestParam String username, HttpSession session) {
        system(session).unlockUser(username);
        return "redirect:/";
    }

    /** Throw away the whole session: shows that an account lock survives a new session. */
    @PostMapping("/session/new")
    public String newSession(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
