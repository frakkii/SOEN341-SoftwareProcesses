
package com.example.demo.controller;

import com.example.demo.model.JobPosting;
import com.example.demo.service.JobPostingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    public ResponseEntity<List<JobPosting>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String jobType) {
        return ResponseEntity.ok(jobPostingService.getActiveJobs(keyword, location, industry, jobType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPosting> getJobById(
            @PathVariable Integer id) {
        JobPosting job = jobPostingService.getJobById(id);
        return job == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(job);
    }
}
