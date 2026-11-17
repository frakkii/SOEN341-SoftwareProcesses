package com.example.demo.controller;

import com.example.demo.exception.ApplicationException;
import com.example.demo.service.ResumeFeedbackService;
import com.example.demo.service.ResumeFeedbackService.FeedbackResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// AI feedback on the logged-in job seeker's current resume. Like ResumeController, the
// job seeker comes from the session, never from the request, so only their own resume is read.
// POST because every request runs a new (possibly paid) AI review.
@RestController
@RequestMapping("/api/resume/feedback")
public class ResumeFeedbackController {

    private static final String JOB_SEEKER = "Job Seeker";

    private final ResumeFeedbackService feedbackService;

    public ResumeFeedbackController(ResumeFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResult> getFeedback(HttpSession session) {
        return ResponseEntity.ok(feedbackService.reviewCurrentResume(jobSeekerId(session)));
    }

    // 401 if nobody is logged in, 403 if it's a recruiter
    private static Integer jobSeekerId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!JOB_SEEKER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Only job seekers can get resume feedback.");
        }
        return userId;
    }
}
