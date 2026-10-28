package com.example.demo.controller;

import com.example.demo.model.Application;
import com.example.demo.model.Resume;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.ResumeRepository.ResumeInfo;
import jakarta.transaction.Transactional;
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

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private final ApplicationRepository applications;

    public ResumeController(ResumeRepository resumes, ApplicationRepository applications) {
        this.resumes = resumes;
        this.applications = applications;
    }

    @GetMapping
    public ResponseEntity<?> getResume(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        Optional<ResumeInfo> info = resumes.findInfoByPersonIdAndCurrentTrue(personId);
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

        Optional<Resume> resume = resumes.findByPersonIdAndCurrentTrue(personId);
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
    @GetMapping("/applications/{applicationId}/file")
       public ResponseEntity<?> downloadApplicantResume(
        @PathVariable Long applicationId,
        HttpSession session) {

    Integer employerId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);

    if (employerId == null) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Please sign in first."));
    }

    if (!"Recruiter".equals(session.getAttribute(AuthController.SESSION_ROLE))) {
        return ResponseEntity.status(403)
                .body(Map.of("message", "Only recruiters can access applicant resumes."));
    }

    if (applications.countApplicationOwnedByEmployer(applicationId, employerId) == 0) {
        return ResponseEntity.notFound().build();
    }

    Optional<Application> application = applications.findById(applicationId);
    if (application.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    Optional<Resume> resume = resumes.findByPersonIdAndCurrentTrue(application.get().getPersonId());

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
    @Transactional
    public ResponseEntity<?> uploadResume(@RequestParam(value = "resume", required = false) MultipartFile file,
                                          HttpSession session) {

        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        if (file == null || file.isEmpty()) {
            return badRequest("Uploaded file is empty.");
        }

        String fileName = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().trim();

        if (fileName.isBlank()
                || fileName.length() > 255
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.chars().anyMatch(Character::isISOControl)) {
            return badRequest("Invalid file name.");
        }

        String contentType = contentTypeFor(fileName);
        if (contentType == null) {
            return badRequest("Resume must be a PDF, DOC or DOCX file.");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            return badRequest("Error reading the uploaded file.");
        }
        if (!matchesFileType(fileName, data)) {
            return badRequest("File contents do not match the file extension.");
        }
        Optional<Resume> currentResume = resumes.findByPersonIdAndCurrentTrue(personId);
        boolean replacing = currentResume.isPresent();
        currentResume.ifPresent(resume -> resume.setCurrent(false));
        resumes.save(new Resume(personId, fileName, contentType, data));

        return ResponseEntity.status(replacing ? 200 : 201).body(Map.of(
                "message", replacing ? "Resume replaced successfully." : "Resume uploaded successfully.",
                "action", replacing ? "replaced" : "created",
                "fileName", fileName,
                "fileSize", data.length
        ));
    }

    @GetMapping("/versions")
    public ResponseEntity<?> getResumeVersions(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }
        return ResponseEntity.ok(resumes.findAllByPersonIdOrderByUploadedAtDesc(personId));
    }

    @PutMapping("/versions/{resumeId}/current")
    @Transactional
    public ResponseEntity<?> selectCurrentResume(@PathVariable Long resumeId, HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        Optional<Resume> selected = resumes.findByIdAndPersonId(resumeId, personId);
        if (selected.isEmpty()) {
            return noResume();
        }

        resumes.findByPersonIdAndCurrentTrue(personId)
                .ifPresent(currentResume -> currentResume.setCurrent(false));
        selected.get().setCurrent(true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> deleteResume(HttpSession session) {
        Integer personId = jobSeekerId(session);
        if (personId == null) {
            return notJobSeeker(session);
        }

        Optional<Resume> currentResume = resumes.findByPersonIdAndCurrentTrue(personId);
        if (currentResume.isEmpty()) {
            return noResume();
        }

        resumes.delete(currentResume.get());
        resumes.findFirstByPersonIdOrderByUploadedAtDesc(personId)
                .ifPresent(resume -> resume.setCurrent(true));
        return ResponseEntity.noContent().build();
    }
    private static boolean matchesFileType(String fileName, byte[] data) {
        String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case ".pdf" -> startsWith(data, new byte[] { '%', 'P', 'D', 'F', '-' });
            case ".doc" -> startsWith(data, new byte[] {
                    (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                    (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
            });
            case ".docx" -> isDocx(data);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }

        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDocx(byte[] data) {
        boolean hasContentTypes = false;
        boolean hasDocument = false;
        int entryCount = 0;
        long uncompressedBytes = 0;
        byte[] buffer = new byte[4096];

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entryCount > 100) {
                    return false;
                }

                String name = entry.getName();
                hasContentTypes |= "[Content_Types].xml".equals(name);
                hasDocument |= "word/document.xml".equals(name);

                int bytesRead;
                while ((bytesRead = zip.read(buffer)) != -1) {
                    uncompressedBytes += bytesRead;
                    if (uncompressedBytes > 25 * 1024 * 1024) {
                        return false;
                    }
                }

                zip.closeEntry();
            }
        } catch (IOException e) {
            return false;
        }

        return hasContentTypes && hasDocument;
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
