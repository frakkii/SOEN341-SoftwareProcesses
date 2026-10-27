
package com.example.demo.service;

import com.example.demo.dto.JobListingResponse;
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
    public List<JobListingResponse> getActiveJobs(String keyword, String location, String industry, String jobType) {
        return jobPostingRepository.findActivePostings(
                        normalize(keyword), normalize(location), normalize(industry), normalize(jobType), LocalDate.now())
                .stream()
                .map(JobListingResponse::from)
                .toList();
    }

    // Closed and expired postings look the same as missing ones
    public JobListingResponse getJobById(Integer postId) {
        if (postId == null || postId <= 0) {
            throw new ApplicationException("Job id must be a positive number.");
        }
        return jobPostingRepository.findWithEmployerById(postId)
                .filter(job -> job.isAcceptingApplications(LocalDate.now()))
                .map(JobListingResponse::from)
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
