package com.example.demo.controller;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.model.EmployerUser;
import com.example.demo.model.JobPosting;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/postings")
public class JobPostingController {

private final JobPostingRepository jobPostingRepository;

    public JobPostingController(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createPosting(@RequestBody JobPostingRequest request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User is not logged in"));
        }

        if (!"Recruiter".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only recruiters can create job postings"));
        }

        // Validate input
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Job title is required"));
        }

        // Save to database here
        JobPosting posting = new JobPosting(
                userId,
                request.title().trim(),
                request.department() == null ? "" : request.department().trim(),
                request.salary() == null ? "" : request.salary().trim(),
                request.requiredSkills() == null ? "" : request.requiredSkills().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.applicationDeadline()
        );

        JobPosting saved = jobPostingRepository.save(posting);
        return ResponseEntity.ok(Map.of("message", "Job listing created", "userId", userId));
    }

    public record JobPostingRequest(
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline
    ) {}
}
