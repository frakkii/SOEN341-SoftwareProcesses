package com.example.demo.controller;

import com.example.demo.repository.PersonUserRepository;
import com.jayway.jsonpath.JsonPath;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    // Uploads are checked against their extension, so PDFs and DOCXs get real file contents with
    // the text inside (to tell versions apart). Empty text stays an empty file.
    static byte[] contents(String name, String text) {
        if (text.isEmpty()) {
            return new byte[0];
        }
        if (name.endsWith(".pdf")) {
            return ("%PDF-1.4\n" + text).getBytes(StandardCharsets.UTF_8);
        }
        if (name.endsWith(".docx")) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
                zip.write("<Types/>".getBytes(StandardCharsets.UTF_8));
                zip.putNextEntry(new ZipEntry("word/document.xml"));
                zip.write(text.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return bytes.toByteArray();
        }
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static MockMultipartFile file(String name, String text) {
        return new MockMultipartFile("resume", name, "application/octet-stream", contents(name, text));
    }

    private ResultActions upload(MockHttpSession session, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/api/resume").file(file).session(session));
    }

    @Test
    void jobSeekerCanUploadAndDownloadResume() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());

        byte[] pdf = contents("cv.pdf", "first resume");

        upload(session, file("cv.pdf", "first resume"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Resume uploaded successfully."))
                .andExpect(jsonPath("$.fileName").value("cv.pdf"));

        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("cv.pdf"))
                .andExpect(jsonPath("$.fileSize").value(pdf.length));

        mockMvc.perform(get("/api/resume/file").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void uploadingAgainReplacesTheCurrentResumeAndKeepsTheOldVersion() throws Exception {
        String email = uniqueEmail();
        MockHttpSession session = loggedIn("Job Seeker", email);
        Integer personId = personUsers.findByEmailIgnoreCase(email).orElseThrow().getId();

        byte[] docx = contents("new.docx", "new resume");

        upload(session, file("old.pdf", "old resume")).andExpect(status().isCreated());
        upload(session, file("new.docx", "new resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resume replaced successfully."));

        assertThat(resumes.findFirstByPersonIdAndCurrentTrueOrderByUploadedAtDesc(personId)).hasValueSatisfying(resume -> {
            assertThat(resume.getFileName()).isEqualTo("new.docx");
            assertThat(resume.getData()).isEqualTo(docx);
        });
        // Both versions are stored, and only the newest is current
        assertThat(resumes.findAll()).filteredOn(resume -> resume.getPersonId().equals(personId))
                .extracting(resume -> resume.getFileName() + ":" + resume.isCurrent())
                .containsExactlyInAnyOrder("old.pdf:false", "new.docx:true");

        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(jsonPath("$.fileName").value("new.docx"));
        mockMvc.perform(get("/api/resume/file").session(session))
                .andExpect(content().bytes(docx));
    }

    @Test
    void jobSeekersOnlySeeTheirOwnResume() throws Exception {
        MockHttpSession first = loggedIn("Job Seeker", uniqueEmail());
        MockHttpSession second = loggedIn("Job Seeker", uniqueEmail());

        upload(first, file("first.pdf", "first")).andExpect(status().isCreated());

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

    @Test
    void uploadRejectsFilesWhoseContentsDoNotMatchTheExtension() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());

        upload(session, new MockMultipartFile("resume", "cv.pdf", "application/pdf", "not really a pdf".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File contents do not match the file extension."));
        upload(session, new MockMultipartFile("resume", "cv.docx", "application/octet-stream", "plain text".getBytes()))
                .andExpect(status().isBadRequest());
    }

    // The id of one of the session's resume versions, found by file name
    private Long versionId(MockHttpSession session, String fileName) throws Exception {
        String body = mockMvc.perform(get("/api/resume/versions").session(session))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.fileName == '" + fileName + "')].id");
        return ids.get(0).longValue();
    }

    @Test
    void listsEveryVersionWithTheCurrentOneMarked() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());
        upload(session, file("old.pdf", "old")).andExpect(status().isCreated());
        upload(session, file("new.pdf", "new")).andExpect(status().isOk());

        mockMvc.perform(get("/api/resume/versions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.current == true)].fileName", contains("new.pdf")))
                .andExpect(jsonPath("$[?(@.current == false)].fileName", contains("old.pdf")));
    }

    @Test
    void choosingAnEarlierVersionMakesItCurrent() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());
        upload(session, file("old.pdf", "old")).andExpect(status().isCreated());
        upload(session, file("new.pdf", "new")).andExpect(status().isOk());
        Long oldId = versionId(session, "old.pdf");

        mockMvc.perform(put("/api/resume/versions/" + oldId + "/current").session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(jsonPath("$.fileName").value("old.pdf"));

        // Choosing the version that is already current keeps it current
        mockMvc.perform(put("/api/resume/versions/" + oldId + "/current").session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resume/versions").session(session))
                .andExpect(jsonPath("$[?(@.current == true)].fileName", contains("old.pdf")));
    }

    @Test
    void jobSeekersCannotChooseAnotherJobSeekersResume() throws Exception {
        MockHttpSession owner = loggedIn("Job Seeker", uniqueEmail());
        MockHttpSession other = loggedIn("Job Seeker", uniqueEmail());
        upload(owner, file("cv.pdf", "mine")).andExpect(status().isCreated());
        Long ownersId = versionId(owner, "cv.pdf");

        mockMvc.perform(put("/api/resume/versions/" + ownersId + "/current").session(other))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/resume/versions").session(other))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deletingTheCurrentResumeFallsBackToTheNextVersion() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker", uniqueEmail());
        upload(session, file("old.pdf", "old")).andExpect(status().isCreated());
        upload(session, file("new.pdf", "new")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/resume").session(session)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("old.pdf"));

        mockMvc.perform(delete("/api/resume").session(session)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resume").session(session)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/resume").session(session)).andExpect(status().isNotFound());
    }

    @Test
    void versionEndpointsRequireAJobSeeker() throws Exception {
        MockHttpSession recruiter = loggedIn("Recruiter", uniqueEmail());

        mockMvc.perform(get("/api/resume/versions").session(new MockHttpSession()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/resume/versions").session(recruiter)).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/resume/versions/1/current").session(recruiter)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/resume").session(recruiter)).andExpect(status().isForbidden());
    }
}
