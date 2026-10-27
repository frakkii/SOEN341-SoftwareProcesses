package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // Registers a new account, logs in and returns the logged-in session
    private MockHttpSession loggedIn(String role) throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String companyName = "Recruiter".equals(role) ? ",\"companyName\":\"Acme Corp\"" : "";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + role + "\",\"name\":\"Jane\",\"surname\":\"Doe\","
                                + "\"email\":\"" + email + "\",\"password\":\"secret123\","
                                + "\"phone\":\"514-555-1234\",\"address\":\"1455 De Maisonneuve Blvd W\","
                                + "\"city\":\"Montreal\",\"provinceState\":\"Quebec\",\"country\":\"Canada\","
                                + "\"postalCodeZip\":\"H3G 1M8\"" + companyName + "}"))
                .andExpect(status().isOk());

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    private ResultActions update(MockHttpSession session, String json) throws Exception {
        return mockMvc.perform(put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .session(session));
    }

    @Test
    void newJobSeekerHasAnEmptyProfileWithTheirRegistrationDetails() throws Exception {
        mockMvc.perform(get("/api/profile").session(loggedIn("Job Seeker")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.city").value("Montreal"))
                .andExpect(jsonPath("$.headline").doesNotExist())
                .andExpect(jsonPath("$.skills.length()").value(0))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void jobSeekerCanEditTheirProfileAndItIsSaved() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");

        update(session, "{\"headline\":\"  Software student  \",\"about\":\"I like tests.\","
                + "\"skills\":[\"Java\",\" SQL \",\"java\",\"\"],\"city\":\"Laval\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated."))
                .andExpect(jsonPath("$.profile.headline").value("Software student"));

        mockMvc.perform(get("/api/profile").session(session))
                .andExpect(jsonPath("$.headline").value("Software student"))
                .andExpect(jsonPath("$.about").value("I like tests."))
                .andExpect(jsonPath("$.skills.length()").value(2))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("SQL"))
                .andExpect(jsonPath("$.city").value("Laval"))
                // Left out of the request, so unchanged
                .andExpect(jsonPath("$.provinceState").value("Quebec"));
    }

    @Test
    void blankFieldsClearTheProfileText() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");
        update(session, "{\"headline\":\"Student\",\"skills\":[\"Java\"]}").andExpect(status().isOk());

        update(session, "{\"headline\":\"   \",\"skills\":[]}").andExpect(status().isOk());

        mockMvc.perform(get("/api/profile").session(session))
                .andExpect(jsonPath("$.headline").doesNotExist())
                .andExpect(jsonPath("$.skills.length()").value(0));
    }

    @Test
    void rejectsFieldsThatAreTooLongOrInvalid() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");

        update(session, "{\"headline\":\"" + "x".repeat(121) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Headline must be at most 120 characters."));
        update(session, "{\"skills\":[\"a,b\"]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Skills can't contain commas."));
        update(session, "{\"city\":\" \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City can't be empty."));
    }

    @Test
    void profileRequiresAJobSeeker() throws Exception {
        mockMvc.perform(get("/api/profile").session(new MockHttpSession()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile").session(loggedIn("Recruiter")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only job seekers have a profile."));
    }
}
