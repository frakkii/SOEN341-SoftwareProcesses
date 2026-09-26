package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // The user store is shared across tests, so each test uses its own email
    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // A complete, valid registration; tests override or remove fields as needed
    private static Map<String, String> jobSeeker(String email) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", "Jane");
        fields.put("surname", "Doe");
        fields.put("email", email);
        fields.put("password", "secret123");
        fields.put("phone", "514-555-1234");
        fields.put("address", "1455 De Maisonneuve Blvd W");
        fields.put("city", "Montreal");
        fields.put("provinceState", "Quebec");
        fields.put("country", "Canada");
        fields.put("postalCodeZip", "H3G 1M8");
        return fields;
    }

    private static Map<String, String> recruiter(String email) {
        Map<String, String> fields = jobSeeker(email);
        fields.put("role", "Recruiter");
        fields.put("companyName", "Acme Corp");
        return fields;
    }

    private static String toJson(Map<String, String> fields) {
        StringJoiner json = new StringJoiner(",", "{", "}");
        fields.forEach((key, value) -> json.add("\"" + key + "\":\"" + value + "\""));
        return json.toString();
    }

    private ResultActions register(Map<String, String> fields) throws Exception {
        return postJson("/api/auth/register", toJson(fields));
    }

    private ResultActions register(String name, String email, String password, String role) throws Exception {
        Map<String, String> fields = "Recruiter".equals(role) ? recruiter(email) : jobSeeker(email);
        fields.put("name", name);
        fields.put("password", password);
        if (role != null) {
            fields.put("role", role);
        }
        return register(fields);
    }

    private ResultActions login(String email, String password) throws Exception {
        return postJson("/api/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    @Test
    void registerThenLoginReturnsUserDetails() throws Exception {
        String email = uniqueEmail();

        register("  Jane ", email, "secret123", "Recruiter")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        login(email.toUpperCase(), "secret123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("Recruiter"));
    }

    @Test
    void registerDefaultsToJobSeekerRole() throws Exception {
        String email = uniqueEmail();

        register("Jane", email, "secret123", null).andExpect(status().isOk());

        login(email, "secret123").andExpect(jsonPath("$.role").value("Job Seeker"));
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail();

        register("Jane", email, "secret123", null).andExpect(status().isOk());
        register("Other", email, "different", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void registerRejectsEmailAlreadyUsedByOtherAccountType() throws Exception {
        String email = uniqueEmail();

        register("Jane", email, "secret123", "Job Seeker").andExpect(status().isOk());
        register("Jane", email, "secret123", "Recruiter")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void registerListsMissingFields() throws Exception {
        Map<String, String> fields = jobSeeker(uniqueEmail());
        fields.remove("surname");
        fields.remove("city");

        register(fields)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required fields: Last name, City"));
    }

    @Test
    void registerRequiresCompanyNameForRecruiters() throws Exception {
        Map<String, String> fields = recruiter(uniqueEmail());
        fields.remove("companyName");

        register(fields)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required fields: Company name"));
    }

    @Test
    void registerRejectsBlankFields() throws Exception {
        register("   ", uniqueEmail(), "secret123", null).andExpect(status().isBadRequest());
        register("Jane", "  ", "secret123", null).andExpect(status().isBadRequest());
        register("Jane", uniqueEmail(), "", null).andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsInvalidEmail() throws Exception {
        register("Jane", "not-an-email", "secret123", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email address is not valid"));
    }

    @Test
    void registerRejectsUnknownRole() throws Exception {
        register("Jane", uniqueEmail(), "secret123", "Admin")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role must be Job Seeker or Recruiter"));
    }

    @Test
    void registerRejectsValuesTooLongForTheirColumn() throws Exception {
        Map<String, String> fields = jobSeeker(uniqueEmail());
        fields.put("city", "a".repeat(51));

        register(fields)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City must be at most 50 characters"));
    }

    @Test
    void registerRejectsInvalidPhone() throws Exception {
        Map<String, String> fields = jobSeeker(uniqueEmail());
        fields.put("phone", "call me");

        register(fields)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone number is not valid"));
    }

    @Test
    void registerRejectsPasswordLongerThanColumn() throws Exception {
        register("Jane", uniqueEmail(), "a".repeat(256), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at most 255 characters"));
    }

    @Test
    void registerAcceptsPasswordOfExactly255Characters() throws Exception {
        String email = uniqueEmail();
        register("Jane", email, "a".repeat(255), null).andExpect(status().isOk());

        login(email, "a".repeat(255)).andExpect(status().isOk());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        String email = uniqueEmail();
        register("Jane", email, "secret123", null).andExpect(status().isOk());

        login(email, "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginRejectsUnknownEmail() throws Exception {
        login(uniqueEmail(), "secret123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginWithOverlongPasswordReturns401NotServerError() throws Exception {
        String email = uniqueEmail();
        register("Jane", email, "secret123", null).andExpect(status().isOk());

        login(email, "a".repeat(100)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsBlankFields() throws Exception {
        login("", "secret123").andExpect(status().isBadRequest());
        login(uniqueEmail(), " ").andExpect(status().isBadRequest());
    }

    @Test
    void concurrentRegistrationsOfSameEmailCreateOnlyOneAccount() throws Exception {
        String email = uniqueEmail();
        int attempts = 16;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                String password = "password-" + i;
                tasks.add(() -> register("Jane", email, password, null)
                        .andReturn().getResponse().getStatus());
            }

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : pool.invokeAll(tasks)) {
                statuses.add(result.get());
            }

            assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
            assertThat(statuses).filteredOn(s -> s == 409).hasSize(attempts - 1);
        } finally {
            pool.shutdownNow();
        }
    }
}
