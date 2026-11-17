package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// AI resume feedback (story #10, AT-31). Tests run in demo mode (no API key in
// src/test/resources/application.properties), so they never call the Claude API.
@SpringBootTest
@AutoConfigureMockMvc
class ResumeFeedbackControllerTests {

    @Autowired
    private MockMvc mockMvc;

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
                .andReturn().getRequest().getSession();
    }

    private void upload(MockHttpSession session, String name, byte[] data) throws Exception {
        mockMvc.perform(multipart("/api/resume")
                        .file(new MockMultipartFile("resume", name, "application/octet-stream", data))
                        .session(session))
                .andExpect(status().is2xxSuccessful());   // 201 for a first resume, 200 when replacing it
    }

    private ResultActions feedback(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/resume/feedback").session(session));
    }

    // A real .docx body: one <w:p> per paragraph
    static byte[] docx(String... paragraphs) throws IOException {
        StringBuilder body = new StringBuilder();
        for (String p : paragraphs) {
            body.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(p).append("</w:t></w:r></w:p>");
        }
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:body>" + body + "</w:body></w:document>";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes(StandardCharsets.UTF_8));
            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write(xml.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    @Test
    void pdfResumeGetsLabeledDemoFeedback() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        upload(jobSeeker, "jane-resume.pdf", ResumeControllerTests.contents("jane-resume.pdf", "Jane Doe resume"));

        feedback(jobSeeker)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("jane-resume.pdf"))
                .andExpect(jsonPath("$.demo").value(true))
                .andExpect(jsonPath("$.notice").value(containsString("Review it before changing your resume")))
                .andExpect(jsonPath("$.feedback.summary").value(containsString("sample feedback")))
                .andExpect(jsonPath("$.feedback.strengths.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.feedback.suggestions.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.feedback.suggestions[0].area").isString())
                .andExpect(jsonPath("$.feedback.suggestions[0].suggestion").isString());
    }

    @Test
    void docxResumeTextIsReadForFeedback() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        upload(jobSeeker, "resume.docx", docx("Jane Doe", "Software developer", "Built a web app"));

        // Demo mode counts the words it extracted: 8 words across the three paragraphs
        feedback(jobSeeker)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("resume.docx"))
                .andExpect(jsonPath("$.feedback.summary").value(containsString("about 8 words")))
                // No numbers and no email in the text, so those suggestions appear
                .andExpect(jsonPath("$.feedback.suggestions[*].suggestion").value(org.hamcrest.Matchers.hasItem(containsString("Add numbers"))))
                .andExpect(jsonPath("$.feedback.suggestions[*].suggestion").value(org.hamcrest.Matchers.hasItem(containsString("email address"))));
    }

    @Test
    void feedbackUsesTheCurrentResumeVersion() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        upload(jobSeeker, "old.pdf", ResumeControllerTests.contents("old.pdf", "old"));
        upload(jobSeeker, "new.docx", docx("New resume"));

        feedback(jobSeeker).andExpect(jsonPath("$.fileName").value("new.docx"));
    }

    @Test
    void oldWordDocFilesAreRefusedWithAClearMessage() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        byte[] doc = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1, 1, 2, 3};
        upload(jobSeeker, "resume.doc", doc);

        feedback(jobSeeker)
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value(
                        "AI feedback works with PDF and DOCX resumes. Save your resume as a PDF or DOCX, upload it, then try again."));
    }

    @Test
    void unreadableDocxShowsAProcessingError() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        // Passes the upload check (a ZIP with word/document.xml) but the XML is broken
        upload(jobSeeker, "broken.docx", ResumeControllerTests.contents("broken.docx", "<w:document><not-closed>"));

        feedback(jobSeeker)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(containsString("couldn't read your resume file")));
    }

    @Test
    void docxWithNoTextAsksForARealTextResume() throws Exception {
        MockHttpSession jobSeeker = loggedIn("Job Seeker");
        upload(jobSeeker, "scan.docx", docx("   "));

        feedback(jobSeeker)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(containsString("couldn't find any text")));
    }

    @Test
    void jobSeekerWithoutAResumeIsAskedToUploadOne() throws Exception {
        feedback(loggedIn("Job Seeker"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Upload a resume first, then ask for feedback."));
    }

    @Test
    void feedbackNeedsALoggedInJobSeeker() throws Exception {
        mockMvc.perform(post("/api/resume/feedback"))
                .andExpect(status().isUnauthorized());
        feedback(loggedIn("Recruiter"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only job seekers can get resume feedback."));
    }
}
