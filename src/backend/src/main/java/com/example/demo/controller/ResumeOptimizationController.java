package com.example.demo.controller;

import com.example.demo.exception.ApplicationException;
import com.example.demo.service.DocxWriter;
import com.example.demo.service.ResumeOptimizationService;
import com.example.demo.service.ResumeOptimizationService.Choices;
import com.example.demo.service.ResumeOptimizationService.GeneratedFile;
import com.example.demo.service.ResumeOptimizationService.OptimizationView;
import com.example.demo.service.ResumeOptimizationService.StoredOptimization;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// Line-by-line AI suggestions for the logged-in job seeker's current resume.
// POST /api/resume/optimization runs the AI and keeps the result in the session; the other
// endpoints take only the indexes of the accepted suggestions and the figures typed into their
// placeholders, so the files are always built from the AI's own result and the job seeker's own
// resume, never from free text sent by the page.
@RestController
@RequestMapping("/api/resume/optimization")
public class ResumeOptimizationController {

    static final String SESSION_KEY = "resumeOptimization";
    private static final String JOB_SEEKER = "Job Seeker";

    private final ResumeOptimizationService optimizations;

    public ResumeOptimizationController(ResumeOptimizationService optimizations) {
        this.optimizations = optimizations;
    }

    // accepted: indexes of the accepted suggestions; figures: index -> the figures typed into
    // that suggestion's placeholders, in order
    public record AcceptedSuggestions(List<Integer> accepted, Map<Integer, List<String>> figures) {
    }

    // POST because every request runs a new (possibly paid) AI review
    @PostMapping
    public ResponseEntity<OptimizationView> optimize(HttpSession session) {
        StoredOptimization stored = optimizations.optimizeCurrentResume(jobSeekerId(session));
        session.setAttribute(SESSION_KEY, stored);
        return ResponseEntity.ok(optimizations.view(stored));
    }

    @PostMapping("/resume")
    public ResponseEntity<byte[]> downloadImprovedResume(@RequestBody(required = false) AcceptedSuggestions body,
                                                         HttpSession session) {
        return docx(optimizations.improvedResume(stored(session), choices(body)));
    }

    @PostMapping("/report")
    public ResponseEntity<byte[]> downloadReport(@RequestBody(required = false) AcceptedSuggestions body,
                                                 HttpSession session) {
        return docx(optimizations.suggestionsReport(stored(session), choices(body)));
    }

    // Saves the improved resume as the new current version (the one sent with applications)
    @PostMapping("/save")
    public ResponseEntity<?> saveAsNewVersion(@RequestBody(required = false) AcceptedSuggestions body,
                                              HttpSession session) {
        StoredOptimization stored = stored(session);
        GeneratedFile file = optimizations.saveAsNewVersion(stored.personId(), stored, choices(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Saved " + file.fileName() + " as your current resume. Your earlier resume is still in your versions.",
                "fileName", file.fileName(),
                "fileSize", file.data().length));
    }

    private static ResponseEntity<byte[]> docx(GeneratedFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DocxWriter.CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(file.data());
    }

    private static Choices choices(AcceptedSuggestions body) {
        return body == null ? new Choices(null, null) : new Choices(body.accepted(), body.figures());
    }

    // The job seeker's latest result; checks it belongs to whoever is logged in now
    private static StoredOptimization stored(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (session.getAttribute(SESSION_KEY) instanceof StoredOptimization stored
                && personId.equals(stored.personId())) {
            return stored;
        }
        throw new ApplicationException(HttpStatus.CONFLICT,
                "Your suggestions have expired. Ask for suggestions again.");
    }

    // 401 if nobody is logged in, 403 if it's a recruiter
    private static Integer jobSeekerId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!JOB_SEEKER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Only job seekers can get resume suggestions.");
        }
        return userId;
    }
}
