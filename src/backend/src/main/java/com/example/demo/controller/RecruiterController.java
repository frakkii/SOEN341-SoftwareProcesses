package com.example.demo.controller;

import com.example.demo.dto.ApplicantResponse;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.JobPosting;
import com.example.demo.model.Resume;
import com.example.demo.service.RecruiterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// A recruiter reviewing applicants. Like ApplicationController, the recruiter comes
// from the session set up at login, never from the request, so they only see the
// applications to their own postings.
@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {

    private static final String RECRUITER = "Recruiter";

    private final RecruiterService recruiterService;

    public RecruiterController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    // Body of PATCH requests that change a status
    public record StatusRequest(String status) {
    }

    @GetMapping("/postings/{postId}/applications")
    public ResponseEntity<List<ApplicantResponse>> getApplicants(@PathVariable Integer postId, HttpSession session) {
        return ResponseEntity.ok(recruiterService.getApplicants(recruiterId(session), postId));
    }

    @PatchMapping("/postings/{postId}")
    public ResponseEntity<?> setPostingStatus(@PathVariable Integer postId,
                                              @RequestBody(required = false) StatusRequest request,
                                              HttpSession session) {
        JobPosting posting = recruiterService.setPostingStatus(
                recruiterId(session), postId, request == null ? null : request.status());
        return ResponseEntity.ok(Map.of(
                "message", "Posting is now " + posting.getStatus() + ".",
                "postId", posting.getId(),
                "status", posting.getStatus()
        ));
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long applicationId,
                                          @RequestBody(required = false) StatusRequest request,
                                          HttpSession session) {
        ApplicantResponse applicant = recruiterService.updateStatus(
                recruiterId(session), applicationId, request == null ? null : request.status());
        return ResponseEntity.ok(Map.of(
                "message", "Status updated to " + applicant.status() + ".",
                "applicant", applicant
        ));
    }

    @GetMapping("/applications/{applicationId}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long applicationId, HttpSession session) {
        Resume resume = recruiterService.getApplicantResume(recruiterId(session), applicationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(resume.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resume.getData());
    }

    // The logged-in recruiter's id; 401 if nobody is logged in, 403 if it's a job seeker
    private static Integer recruiterId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!RECRUITER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Only recruiters can manage applicants.");
        }
        return userId;
    }
}
