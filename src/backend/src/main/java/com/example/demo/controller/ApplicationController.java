package com.example.demo.controller;

import com.example.demo.dto.ApplicationRequest;
import com.example.demo.dto.ApplicationResponse;
import com.example.demo.exception.ApplicationException;
import com.example.demo.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// A job seeker's applications. Like ResumeController, the job seeker comes from the
// session set up at login, never from the request, so they only see their own.
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private static final String JOB_SEEKER = "Job Seeker";

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<?> submitApplication(@RequestBody(required = false) ApplicationRequest request,
                                               HttpSession session) {
        Integer personId = jobSeekerId(session, "Only job seekers can apply for jobs.");

        ApplicationResponse application = applicationService.submitApplication(
                personId, request == null ? null : request.postId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Application submitted successfully.",
                "application", application
        ));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(HttpSession session) {
        Integer personId = jobSeekerId(session, "Only job seekers have applications.");
        return ResponseEntity.ok(applicationService.getApplications(personId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long id, HttpSession session) {
        Integer personId = jobSeekerId(session, "Only job seekers have applications.");
        return ResponseEntity.ok(applicationService.getApplication(personId, id));
    }

    // The logged-in job seeker's id; 401 if nobody is logged in, 403 if it's a recruiter
    private static Integer jobSeekerId(HttpSession session, String notJobSeekerMessage) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!JOB_SEEKER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, notJobSeekerMessage);
        }
        return userId;
    }
}
