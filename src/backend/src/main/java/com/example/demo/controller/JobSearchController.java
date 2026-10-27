package com.example.demo.controller;

import com.example.demo.dto.JobListingResponse;
import com.example.demo.service.JobPostingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Job search for job seekers. Recruiters manage their own postings in JobPostingController.
@RestController
@RequestMapping("/api/jobs")
public class JobSearchController {

    private final JobPostingService jobPostingService;

    public JobSearchController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    public ResponseEntity<List<JobListingResponse>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String jobType) {
        return ResponseEntity.ok(jobPostingService.getActiveJobs(keyword, location, industry, jobType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobListingResponse> getJobById(
            @PathVariable Integer id) {
        JobListingResponse job = jobPostingService.getJobById(id);
        return job == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(job);
    }
}
