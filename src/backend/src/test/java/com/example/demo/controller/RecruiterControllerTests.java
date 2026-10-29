package com.example.demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecruiterControllerTests {

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

    // Creates a posting through the API as this recruiter and returns its id
    private Integer createPosting(MockHttpSession recruiter) throws Exception {
        String body = mockMvc.perform(post("/api/postings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"QA Tester\",\"department\":\"Engineering\",\"salary\":\"$60k\","
                                + "\"requiredSkills\":\"Selenium\",\"description\":\"Test things\","
                                + "\"location\":\"Montreal, QC\","
                                + "\"applicationDeadline\":\"" + LocalDate.now().plusDays(14) + "\"}")
                        .session(recruiter))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.postId");
    }

    // A job seeker with a resume applies to the posting; returns the application id
    private Long applyTo(Integer postId) throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        mockMvc.perform(multipart("/api/resume")
                        .file(new MockMultipartFile("resume", "cv.pdf", "application/pdf",
                                ResumeControllerTests.contents("cv.pdf", "my resume")))
                        .session(jobSeeker))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":" + postId + "}")
                        .session(jobSeeker))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.application.id");
        return id.longValue();
    }

    private ResultActions setStatus(MockHttpSession session, Long applicationId, String status) throws Exception {
        return mockMvc.perform(patch("/api/recruiter/applications/" + applicationId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}")
                .session(session));
    }

    // ---- Postings ---------------------------------------------------------

    @Test
    void recruiterSeesTheirPostingsWithApplicantCounts() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Integer postId = createPosting(recruiter);
        applyTo(postId);
        applyTo(postId);

        mockMvc.perform(get("/api/postings").session(recruiter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(postId))
                .andExpect(jsonPath("$[0].location").value("Montreal, QC"))
                .andExpect(jsonPath("$[0].status").value("Open"))
                .andExpect(jsonPath("$[0].applicants").value(2));
    }

    @Test
    void creatingAPostingRequiresAFutureDeadline() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");

        mockMvc.perform(post("/api/postings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"QA Tester\"}")
                        .session(recruiter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application deadline is required"));

        mockMvc.perform(post("/api/postings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"QA Tester\",\"applicationDeadline\":\"" + LocalDate.now().minusDays(1) + "\"}")
                        .session(recruiter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application deadline can't be in the past"));
    }

    @Test
    void closedPostingStopsNewApplicationsAndCanBeReopened() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Integer postId = createPosting(recruiter);

        mockMvc.perform(patch("/api/recruiter/postings/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Closed\"}")
                        .session(recruiter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Closed"));

        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        mockMvc.perform(multipart("/api/resume")
                        .file(new MockMultipartFile("resume", "cv.pdf", "application/pdf",
                                ResumeControllerTests.contents("cv.pdf", "cv")))
                        .session(jobSeeker))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":" + postId + "}")
                        .session(jobSeeker))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/recruiter/postings/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"open\"}")
                        .session(recruiter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Open"));
    }

    @Test
    void postingStatusMustBeOpenOrClosed() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Integer postId = createPosting(recruiter);

        mockMvc.perform(patch("/api/recruiter/postings/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Paused\"}")
                        .session(recruiter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Posting status must be Open or Closed."));
    }

    @Test
    void recruitersCannotCloseAnotherRecruitersPosting() throws Exception {
        Integer postId = createPosting(loggedIn("Recruiter"));

        mockMvc.perform(patch("/api/recruiter/postings/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Closed\"}")
                        .session(loggedIn("Recruiter")))
                .andExpect(status().isNotFound());
    }

    // ---- Applicants -------------------------------------------------------

    @Test
    void recruiterSeesWhoAppliedToTheirPosting() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Integer postId = createPosting(recruiter);
        Long applicationId = applyTo(postId);

        mockMvc.perform(get("/api/recruiter/postings/" + postId + "/applications").session(recruiter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].applicationId").value(applicationId))
                .andExpect(jsonPath("$[0].name").value("Jane"))
                .andExpect(jsonPath("$[0].status").value("Applied"))
                .andExpect(jsonPath("$[0].hasResume").value(true))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void recruitersCannotSeeApplicantsForAnotherRecruitersPosting() throws Exception {
        Integer postId = createPosting(loggedIn("Recruiter"));
        applyTo(postId);

        mockMvc.perform(get("/api/recruiter/postings/" + postId + "/applications").session(loggedIn("Recruiter")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job posting not found."));
    }

    @Test
    void recruiterEndpointsRequireARecruiter() throws Exception {
        mockMvc.perform(get("/api/recruiter/postings/1/applications").session(new MockHttpSession()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/recruiter/postings/1/applications").session(loggedIn("Job Seeker")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only recruiters can manage applicants."));
    }

    @Test
    void recruiterCanDownloadAnApplicantsResume() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Long applicationId = applyTo(createPosting(recruiter));

        mockMvc.perform(get("/api/recruiter/applications/" + applicationId + "/resume").session(recruiter))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(ResumeControllerTests.contents("cv.pdf", "my resume")));

        mockMvc.perform(get("/api/recruiter/applications/" + applicationId + "/resume").session(loggedIn("Recruiter")))
                .andExpect(status().isNotFound());
    }

    // ---- Status changes ---------------------------------------------------

    @Test
    void statusMovesThroughTheStagesAndTheJobSeekerSeesIt() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Long applicationId = applyTo(createPosting(recruiter));

        setStatus(recruiter, applicationId, "Under Review")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicant.status").value("Under Review"));
        setStatus(recruiter, applicationId, "INTERVIEW")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicant.status").value("Interview"));
        setStatus(recruiter, applicationId, "Offered")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated to Offered."));
    }

    @Test
    void statusCannotGoBackwards() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Long applicationId = applyTo(createPosting(recruiter));

        setStatus(recruiter, applicationId, "Interview").andExpect(status().isOk());
        setStatus(recruiter, applicationId, "Under Review")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An application can't move from Interview back to Under Review."));
    }

    @Test
    void finalDecisionsCannotBeChanged() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Long applicationId = applyTo(createPosting(recruiter));

        setStatus(recruiter, applicationId, "Rejected").andExpect(status().isOk());
        setStatus(recruiter, applicationId, "Offered")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This application is already Rejected and can't be changed."));
    }

    @Test
    void unknownStatusIsRejected() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter");
        Long applicationId = applyTo(createPosting(recruiter));

        setStatus(recruiter, applicationId, "Hired")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Status must be one of: Applied, Under Review, Interview, Offered, Rejected."));
    }

    @Test
    void recruitersCannotChangeApplicationsToAnotherRecruitersPosting() throws Exception {
        Long applicationId = applyTo(createPosting(loggedIn("Recruiter")));

        setStatus(loggedIn("Recruiter"), applicationId, "Interview")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Application not found."));
    }
}
