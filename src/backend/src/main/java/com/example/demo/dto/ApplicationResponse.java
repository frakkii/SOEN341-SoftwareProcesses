package com.example.demo.dto;

import com.example.demo.model.Application;

// What the API shows for one application. Times are ISO-8601 strings. The job
// title and company come from the posting so the tracker can show them.
public record ApplicationResponse(
        Long id,
        Integer postId,
        String jobTitle,
        String companyName,
        String status,
        String appliedAt,
        String updatedAt
) {
    public static ApplicationResponse from(Application application, String jobTitle, String companyName) {
        return new ApplicationResponse(
                application.getId(),
                application.getPostId(),
                jobTitle,
                companyName,
                application.getStatus().getLabel(),
                application.getAppliedAt().toString(),
                application.getUpdatedAt().toString()
        );
    }
}
