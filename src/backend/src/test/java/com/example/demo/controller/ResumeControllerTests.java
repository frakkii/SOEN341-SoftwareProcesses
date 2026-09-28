package com.example.demo.controller;

import com.example.demo.repository.PersonUserRepository;
import com.example.demo.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonUserRepository personUsers;

    @Autowired
    private ResumeRepository resumes;

    // Registers a new account, logs in and returns the logged-in session
    private MockHttpSession loggedIn(String role, String email) throws Exception {
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

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static MockMultipartFile file(String name, String contents) {
        return new MockMultipartFile("resume", name, "application/octet-stream", contents.getBytes());
    }

    private ResultActions upload(MockHttpSession session, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/api/resume").file(file).session(session));
    }

    @Test
    void jobSeekerCanUploadAndDownloadResume() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());

        upload(session, file("cv.pdf", "first resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("cv.pdf"));

        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("cv.pdf"))
                .andExpect(jsonPath("$.fileSize").value("first resume".length()));

        mockMvc.perform(get("/api/resume/file").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes("first resume".getBytes()));
    }

    @Test
    void uploadingAgainReplacesTheOldResume() throws Exception {
        String email = uniqueEmail();
        MockHttpSession session = loggedIn("Job Seeker", email);
        Integer personId = personUsers.findByEmailIgnoreCase(email).orElseThrow().getId();

        upload(session, file("old.pdf", "old resume")).andExpect(status().isOk());
        upload(session, file("new.docx", "new resume")).andExpect(status().isOk());

        assertThat(resumes.findById(personId)).hasValueSatisfying(resume -> {
            assertThat(resume.getFileName()).isEqualTo("new.docx");
            assertThat(resume.getData()).isEqualTo("new resume".getBytes());
        });
        assertThat(resumes.findAll()).filteredOn(resume -> resume.getPersonId().equals(personId)).hasSize(1);
    }

    @Test
    void jobSeekersOnlySeeTheirOwnResume() throws Exception {
        MockHttpSession first = loggedIn("Job Seeker", uniqueEmail());
        MockHttpSession second = loggedIn("Job Seeker", uniqueEmail());

        upload(first, file("first.pdf", "first")).andExpect(status().isOk());

        mockMvc.perform(get("/api/resume").session(second))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No resume uploaded yet."));
    }

    @Test
    void uploadRequiresLogin() throws Exception {
        upload(new MockHttpSession(), file("cv.pdf", "resume"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruitersCannotUploadResume() throws Exception {
        MockHttpSession session = loggedIn("Recruiter", uniqueEmail());

        upload(session, file("cv.pdf", "resume"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only job seekers can upload a resume."));
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());

        upload(session, file("cv.pdf", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded file is empty."));
    }

    @Test
    void uploadRejectsOtherFileTypes() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());

        upload(session, file("cv.exe", "resume"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resume must be a PDF, DOC or DOCX file."));
    }
}
