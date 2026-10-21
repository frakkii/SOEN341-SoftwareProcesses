package com.example.demo.dto;

import com.example.demo.model.Application;

// What the API shows for one application. Times are ISO-8601 strings.
public record ApplicationResponse(
        Long id,
        Integer postId,
        String status,
        String appliedAt,
        String updatedAt
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getPostId(),
                application.getStatus().getLabel(),
                application.getAppliedAt().toString(),
                application.getUpdatedAt().toString()
        );
    }
}
