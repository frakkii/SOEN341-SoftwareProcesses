package com.example.demo.dto;

import java.time.LocalDate;

// Body of PUT /api/recruiter/postings/{postId}: the full set of editable details,
// the same fields as creating a posting. Blank optional fields are stored as empty.
public record PostingUpdateRequest(
        String title,
        String department,
        String salary,
        String requiredSkills,
        String description,
        LocalDate applicationDeadline,
        String location,
        String industry,
        String jobType
) {
}
