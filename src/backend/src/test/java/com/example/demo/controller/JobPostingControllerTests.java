package com.example.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobPostingControllerTests {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(2000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int activePostId;

    @BeforeEach
    void insertPostings() {
        jdbc.update("DELETE FROM job_postings");
        jdbc.update("DELETE FROM employer_user");
        int employerId = NEXT_ID.getAndIncrement();
        activePostId = NEXT_ID.getAndIncrement();
        jdbc.update("""
                INSERT INTO employer_user
                (employer_id, email, recruiter_name, recruiter_surname, company_name, country,
                 province_state, city, address, postal_code_zip, phone, password)
                VALUES (?, ?, 'Recruiter', 'Test', 'Acme Software', 'Canada', 'Quebec',
                        'Montreal', '1 Main Street', 'H1H 1H1', '514-555-0100', 'secret')
                """, employerId, "recruiter-" + employerId + "@example.com");
        insertPosting(activePostId, employerId, "Java Developer", "Backend engineering",
                "Montreal", "Technology", "Full-time", true, LocalDate.now().plusDays(10));
        insertPosting(NEXT_ID.getAndIncrement(), employerId, "Expired Developer", "Old role",
                "Montreal", "Technology", "Full-time", true, LocalDate.now().minusDays(1));
        insertPosting(NEXT_ID.getAndIncrement(), employerId, "Inactive Developer", "Closed role",
                "Toronto", "Finance", "Contract", false, LocalDate.now().plusDays(10));
    }

    private void insertPosting(int postId, int employerId, String title, String description,
                               String location, String industry, String jobType,
                               boolean active, LocalDate deadline) {
        jdbc.update("""
                INSERT INTO job_postings
                (employer_id, post_id, title, description, posted_date, deadline, status,
                 location, industry, job_type)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)
                """, employerId, postId, title, description, deadline, active,
                location, industry, jobType);
    }

    @Test
    void listsOnlyActiveAndUnexpiredJobsWithCompanyName() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(activePostId))
                .andExpect(jsonPath("$[0].companyName").value("Acme Software"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void appliesKeywordAndAllFiltersTogether() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "java")
                        .param("location", "montreal")
                        .param("industry", "technology")
                        .param("jobType", "full-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    void returnsEmptyArrayForNoMatchesAndNotFoundForMissingOrUnavailableDetails() throws Exception {
        mockMvc.perform(get("/api/jobs").param("location", "Vancouver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/jobs/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidSearchParameters() throws Exception {
        mockMvc.perform(get("/api/jobs").param("keyword", "x".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search parameters must be 100 characters or fewer."));
        mockMvc.perform(get("/api/jobs/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Job id must be a positive number."));
    }
}
