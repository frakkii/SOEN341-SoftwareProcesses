package com.example.demo.controller;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.model.EmployerUser;
import com.example.demo.model.JobPosting;
import com.example.demo.service.RecruiterService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/postings")
public class JobPostingController {

private final JobPostingRepository jobPostingRepository;
    private final RecruiterService recruiterService;

    public JobPostingController(JobPostingRepository jobPostingRepository, RecruiterService recruiterService) {
        this.jobPostingRepository = jobPostingRepository;
        this.recruiterService = recruiterService;
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
        if (request.applicationDeadline() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Application deadline is required"));
        }
        if (request.applicationDeadline().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Application deadline can't be in the past"));
        }

        // Save to database here
        JobPosting posting = new JobPosting(
                userId,
                request.title().trim(),
                request.department() == null ? "" : request.department().trim(),
                request.salary() == null ? "" : request.salary().trim(),
                request.requiredSkills() == null ? "" : request.requiredSkills().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.applicationDeadline(),
                request.location() == null || request.location().isBlank() ? "Not specified" : request.location().trim()
        );

        JobPosting saved = jobPostingRepository.save(posting);
        return ResponseEntity.ok(Map.of("message", "Job listing created", "userId", userId, "postId", saved.getId()));
    }

     @GetMapping
    public ResponseEntity<List<JobPostingResponse>> getMyPostings(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (!"Recruiter".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        List<JobPosting> mine = jobPostingRepository.findByEmployerId(userId);
        Map<Integer, Long> applicants = recruiterService.countApplicants(mine.stream().map(JobPosting::getId).toList());

        List<JobPostingResponse> postings = mine
                .stream()
                .map(posting -> new JobPostingResponse(
                        posting.getId(),
                        posting.getTitle(),
                        posting.getDepartment(),
                        posting.getSalary(),
                        posting.getRequiredSkills(),
                        posting.getDescription(),
                        posting.getApplicationDeadline(),
                        posting.getLocation(),
                        posting.getStatus(),
                        applicants.getOrDefault(posting.getId(), 0L)
                ))
                .toList();

        return ResponseEntity.ok(postings);
    }
    
    public record JobPostingRequest(
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline,
            String location
    ) {}

    public record JobPostingResponse(
            Integer id,
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline,
            String location,
            String status,
            long applicants
    ) {}
}
