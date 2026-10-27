package com.example.demo.dto;

import com.example.demo.model.Application;
import com.example.demo.model.PersonUser;

// What a recruiter sees for one person who applied to their posting. Times are
// ISO-8601 strings. The password and address are never included.
public record ApplicantResponse(
        Long applicationId,
        Integer postId,
        String name,
        String surname,
        String email,
        String phone,
        String city,
        String status,
        String appliedAt,
        String updatedAt,
        boolean hasResume
) {
    public static ApplicantResponse from(Application application, PersonUser person, boolean hasResume) {
        return new ApplicantResponse(
                application.getId(),
                application.getPostId(),
                person.getName(),
                person.getSurname(),
                person.getEmail(),
                person.getPhone(),
                person.getCity(),
                application.getStatus().getLabel(),
                application.getAppliedAt().toString(),
                application.getUpdatedAt().toString(),
                hasResume
        );
    }
}
