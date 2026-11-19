package com.example.demo.controller;

import com.example.demo.service.DocxText;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Line-by-line AI resume suggestions. Tests run in demo mode (no API key in
// src/test/resources/application.properties), so they never call the Claude API.
@SpringBootTest
@AutoConfigureMockMvc
class ResumeOptimizationControllerTests {

    // Demo mode rewrites "Responsible for" (line 3) and "Worked on" (line 4), and adds a
    // "[measurable result]" figure to fill in to the bullet with no numbers (line 5)
    private static final String[] RESUME = {
            "JANE DOE",
            "jane@example.com",
            "EXPERIENCE",
            "- Responsible for the campus website",
            "Worked on the library app with 3 classmates",
            "- Organized weekly study sessions for first-year students"
    };
    private static final String WITH_FIGURE = "Organized weekly study sessions for first-year students, resulting in ";

    @Autowired
    private MockMvc mockMvc;

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

    private MockHttpSession jobSeekerWithResume() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");
        upload(session, "Jane CV.docx", docx(RESUME));
        return session;
    }

    private void upload(MockHttpSession session, String name, byte[] data) throws Exception {
        mockMvc.perform(multipart("/api/resume")
                        .file(new MockMultipartFile("resume", name, "application/octet-stream", data))
                        .session(session))
                .andExpect(status().is2xxSuccessful());
    }

    private ResultActions optimize(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/resume/optimization").session(session));
    }

    private ResultActions postAccepted(MockHttpSession session, String path, String accepted) throws Exception {
        return postChoices(session, path, "{\"accepted\":[" + accepted + "]}");
    }

    private ResultActions postChoices(MockHttpSession session, String path, String json) throws Exception {
        return mockMvc.perform(post("/api/resume/optimization" + path)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String docxText(ResultActions result) throws Exception {
        return DocxText.extract(result.andReturn().getResponse().getContentAsByteArray());
    }

    @Test
    void returnsEveryLineWithSuggestions() throws Exception {
        optimize(jobSeekerWithResume())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("Jane CV.docx"))
                .andExpect(jsonPath("$.demo").value(true))
                .andExpect(jsonPath("$.notice").value(containsString("AI-generated")))
                .andExpect(jsonPath("$.summary").value(containsString("demo mode")))
                .andExpect(jsonPath("$.lines.length()").value(6))
                .andExpect(jsonPath("$.suggestionCount").value(3))
                .andExpect(jsonPath("$.lines[0].kind").value("heading"))
                .andExpect(jsonPath("$.lines[0].suggested").value(false))
                .andExpect(jsonPath("$.lines[3].kind").value("bullet"))
                // the bullet symbol is removed; the page and the DOCX add their own
                .andExpect(jsonPath("$.lines[3].original").value("Responsible for the campus website"))
                .andExpect(jsonPath("$.lines[3].improved").value("Led the campus website"))
                .andExpect(jsonPath("$.lines[3].suggested").value(true))
                .andExpect(jsonPath("$.lines[3].reason").isNotEmpty())
                .andExpect(jsonPath("$.lines[3].figures.length()").value(0))
                .andExpect(jsonPath("$.lines[4].improved").value("Built the library app with 3 classmates"))
                .andExpect(jsonPath("$.lines[5].improved").value(WITH_FIGURE + "[measurable result]"))
                .andExpect(jsonPath("$.lines[5].figures[0]").value("measurable result"));
    }

    @Test
    void figuresTheJobSeekerEntersGoIntoTheImprovedResume() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        String text = docxText(postChoices(session, "/resume",
                "{\"accepted\":[3,5],\"figures\":{\"5\":[\"a 40% rise in attendance\"]}}")
                .andExpect(status().isOk()));
        assertThat(text).contains(WITH_FIGURE + "a 40% rise in attendance");
        assertThat(text).doesNotContain("[measurable result]");

        String report = docxText(postChoices(session, "/report",
                "{\"accepted\":[5],\"figures\":{\"5\":[\"a 40% rise in attendance\"]}}"));
        assertThat(report).contains("Suggested: " + WITH_FIGURE + "a 40% rise in attendance");
    }

    @Test
    void acceptedFiguresMustBeFilledInBeforeTheResumeIsMade() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        postAccepted(session, "/resume", "5")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Fill in \"measurable result\"")));
        postChoices(session, "/save", "{\"accepted\":[5],\"figures\":{\"5\":[\"   \"]}}")
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/resume/versions").session(session)).andExpect(jsonPath("$.length()").value(1));

        // the report can be downloaded before figures are filled in, and shows the placeholder
        assertThat(docxText(postAccepted(session, "/report", "5").andExpect(status().isOk())))
                .contains(WITH_FIGURE + "[measurable result]");
        // a figure for a suggestion that wasn't accepted is ignored
        postChoices(session, "/resume", "{\"accepted\":[],\"figures\":{\"5\":[\"40%\"]}}")
                .andExpect(status().isOk());
    }

    @Test
    void refusesFiguresThatDoNotFit() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        postChoices(session, "/resume", "{\"accepted\":[5],\"figures\":{\"5\":[\"[40%]\"]}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("square brackets")));
        postChoices(session, "/resume", "{\"accepted\":[5],\"figures\":{\"5\":[\"" + "9".repeat(61) + "\"]}}")
                .andExpect(status().isBadRequest());
        postChoices(session, "/resume", "{\"accepted\":[5],\"figures\":{\"5\":[\"40%\",\"extra\"]}}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void improvedResumeUsesOnlyAcceptedSuggestions() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session).andExpect(status().isOk());

        ResultActions download = postAccepted(session, "/resume", "3")
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("wordprocessingml.document")))
                .andExpect(header().string("Content-Disposition", containsString("Jane%20CV-improved.docx")));

        String text = docxText(download);
        assertThat(text).contains("JANE DOE", "jane@example.com", "Led the campus website");
        assertThat(text).doesNotContain("Responsible for");
        // line 4 was not accepted, so it stays as written
        assertThat(text).contains("Worked on the library app with 3 classmates");
    }

    @Test
    void nothingAcceptedGivesBackTheOriginalLines() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        String text = docxText(postAccepted(session, "/resume", "").andExpect(status().isOk()));
        assertThat(text).contains("Responsible for the campus website", "Worked on the library app");
    }

    @Test
    void reportListsEverySuggestionAndWhetherItWasAccepted() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        ResultActions report = postAccepted(session, "/report", "4")
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Jane%20CV-suggestions.docx")));

        String text = docxText(report);
        assertThat(text).contains("1 of 3 suggestions accepted.");
        assertThat(text).contains("Original: Responsible for the campus website", "Suggested: Led the campus website");
        assertThat(text).contains("Not accepted", "Accepted", "Why: ");
        assertThat(text).contains("Demo mode");
    }

    @Test
    void savingMakesTheImprovedResumeTheCurrentVersion() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        postAccepted(session, "/save", "3,4")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("Jane CV-improved.docx"))
                .andExpect(jsonPath("$.message").value(containsString("current resume")));

        mockMvc.perform(get("/api/resume").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("Jane CV-improved.docx"));
        mockMvc.perform(get("/api/resume/versions").session(session))
                .andExpect(jsonPath("$.length()").value(2));

        byte[] saved = mockMvc.perform(get("/api/resume/file").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(DocxText.extract(saved)).contains("Led the campus website", "Built the library app");

        // the saved file is a normal DOCX resume, so it can be optimized again
        optimize(session).andExpect(status().isOk()).andExpect(jsonPath("$.fileName").value("Jane CV-improved.docx"));
    }

    @Test
    void refusesIndexesThatAreNotSuggestions() throws Exception {
        MockHttpSession session = jobSeekerWithResume();
        optimize(session);

        postAccepted(session, "/resume", "0").andExpect(status().isBadRequest());     // a line with no suggestion
        postAccepted(session, "/resume", "99").andExpect(status().isBadRequest());
        postAccepted(session, "/save", "-1").andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/resume/versions").session(session)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void filesNeedSuggestionsFirst() throws Exception {
        postAccepted(jobSeekerWithResume(), "/resume", "")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Ask for suggestions again")));
    }

    @Test
    void pdfGetsClearlyLabeledSampleInDemoMode() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");
        upload(session, "cv.pdf", "%PDF-1.4 sample".getBytes(StandardCharsets.US_ASCII));

        optimize(session)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(containsString("aren't from your resume")))
                .andExpect(jsonPath("$.suggestionCount").value(greaterThanOrEqualTo(1)));
        String text = docxText(postChoices(session, "/resume", "{\"accepted\":[1],\"figures\":{\"1\":[\"25%\"]}}")
                .andExpect(status().isOk()));
        assertThat(text).contains("cutting page load time by 25%");
    }

    @Test
    void oldWordFilesAreRefused() throws Exception {
        MockHttpSession session = loggedIn("Job Seeker");
        byte[] doc = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1, 0 };
        upload(session, "cv.doc", doc);

        optimize(session)
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value(endsWith("upload it, then try again.")))
                .andExpect(jsonPath("$.message").value(containsString("The AI optimizer works with PDF and DOCX")));
    }

    @Test
    void needsAResume() throws Exception {
        optimize(loggedIn("Job Seeker"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Upload a resume first, then ask for suggestions."));
    }

    @Test
    void onlyLoggedInJobSeekers() throws Exception {
        mockMvc.perform(post("/api/resume/optimization")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/resume/optimization/save")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"accepted\":[]}"))
                .andExpect(status().isUnauthorized());
        optimize(loggedIn("Recruiter"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only job seekers can get resume suggestions."));
    }
}
