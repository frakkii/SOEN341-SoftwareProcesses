package com.example.demo.controller;

import com.example.demo.model.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.ResumeRepository.ResumeInfo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// The logged-in job seeker's resume. The job seeker comes from the session set up
// at login, never from the request, so users can only see and replace their own.
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private static final String JOB_SEEKER = "Job Seeker";

    // Checked by extension because browsers don't report content types consistently
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            ".pdf", "application/pdf",
            ".doc", "application/msword",
            ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final ResumeRepository resumes;

    public ResumeController(ResumeRepository resumes) {
        this.resumes = resumes;
    }

    @GetMapping
    public ResponseEntity<?> getResume(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        Optional<ResumeInfo> info = resumes.findInfoByPersonId(personId);
        if (info.isEmpty()) {
            return noResume();
        }
        return ResponseEntity.ok(Map.of(
                "fileName", info.get().getFileName(),
                "fileSize", info.get().getFileSize(),
                "uploadedAt", info.get().getUploadedAt().toString()
        ));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadResume(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        Optional<Resume> resume = resumes.findById(personId);
        if (resume.isEmpty()) {
            return noResume();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.get().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(resume.get().getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resume.get().getData());
    }

    @PostMapping
    public ResponseEntity<?> uploadResume(@RequestParam(value = "resume", required = false) MultipartFile file,
                                          HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        if (file == null || file.isEmpty()) {
            return badRequest("Uploaded file is empty.");
        }

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        String contentType = contentTypeFor(fileName);
        if (contentType == null) {
            return badRequest("Resume must be a PDF, DOC or DOCX file.");
        }
        if (fileName.length() > 255) {
            return badRequest("File name must be at most 255 characters.");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            return badRequest("Error reading the uploaded file.");
        }

        // Same id as the job seeker's existing resume (if any), so this replaces it
        resumes.save(new Resume(personId, fileName, contentType, data));

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully.",
                "fileName", fileName,
                "fileSize", data.length
        ));
    }

    private static String contentTypeFor(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        return ALLOWED_TYPES.get(fileName.substring(dot).toLowerCase(Locale.ROOT));
    }

    // The logged-in job seeker's id, or null if nobody is logged in or it's a recruiter
    private static Integer jobSeekerId(HttpSession session) {
        if (!JOB_SEEKER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            return null;
        }
        return (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    private static ResponseEntity<?> notJobSeeker(HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_USER_ID) == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Please sign in to manage your resume."));
        }
        return ResponseEntity.status(403).body(Map.of("message", "Only job seekers can upload a resume."));
    }

    private static ResponseEntity<?> noResume() {
        return ResponseEntity.status(404).body(Map.of("message", "No resume uploaded yet."));
    }

    private static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
