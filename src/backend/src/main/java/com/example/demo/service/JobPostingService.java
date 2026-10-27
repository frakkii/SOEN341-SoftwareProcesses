
package com.example.demo.service;

import com.example.demo.model.JobPosting;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.exception.ApplicationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    // Retrieve all active job postings
    public List<JobPosting> getActiveJobs(String keyword, String location, String industry, String jobType) {
        return jobPostingRepository.findActivePostings(
                normalize(keyword), normalize(location), normalize(industry), normalize(jobType), LocalDate.now());
    }

    public JobPosting getJobById(Integer postId) {
        if (postId == null || postId <= 0) {
            throw new ApplicationException("Job id must be a positive number.");
        }
        return jobPostingRepository.findById(postId)
                .filter(job -> Boolean.TRUE.equals(job.getStatus()))
                .filter(job -> job.getDeadline() != null && !job.getDeadline().isBefore(LocalDate.now()))
                .orElse(null);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 100) {
            throw new ApplicationException("Search parameters must be 100 characters or fewer.");
        }
        return normalized;
    }
}