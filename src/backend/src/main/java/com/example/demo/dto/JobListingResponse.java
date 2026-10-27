package com.example.demo.dto;

import com.example.demo.model.JobPosting;

import java.time.LocalDate;

// What job seekers see for one posting in job search and on the job detail page
public record JobListingResponse(
        Integer postId,
        String title,
        String companyName,
        String location,
        String department,
        String industry,
        String jobType,
        String salary,
        String requiredSkills,
        String description,
        LocalDate applicationDeadline
) {
    public static JobListingResponse from(JobPosting posting) {
        return new JobListingResponse(
                posting.getId(),
                posting.getTitle(),
                posting.getCompanyName(),
                posting.getLocation(),
                posting.getDepartment(),
                posting.getIndustry(),
                posting.getJobType(),
                posting.getSalary(),
                posting.getRequiredSkills(),
                posting.getDescription(),
                posting.getApplicationDeadline()
        );
    }
}
