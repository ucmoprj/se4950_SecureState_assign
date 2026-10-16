package edu.ucmo.se4950.securestate.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void directRequestToAdminPageIsDeniedForClerk() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/login").session(session).param("username", "bob").param("password", "bob123"))
           .andExpect(status().is3xxRedirection());
        mvc.perform(post("/mfa").session(session).param("code", "222222"));

        // A clerk "types the URL" of the admin page directly.
        mvc.perform(post("/page/managing-users").session(session));

        mvc.perform(get("/").session(session))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("[SEC] DENIED: Managing Users page (LoggedInClerk)")));
    }
}
