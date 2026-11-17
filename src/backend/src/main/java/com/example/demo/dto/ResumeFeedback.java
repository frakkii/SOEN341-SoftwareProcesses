package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

// AI feedback on a resume. The Claude API's structured output schema is generated from
// this record (see ClaudeResumeAdvisor), so the descriptions below are part of the prompt.
public record ResumeFeedback(
        @JsonPropertyDescription("Two or three sentences with an honest overall impression of the resume, addressed to its owner as \"you\".")
        String summary,

        @JsonPropertyDescription("Two to four things this resume already does well, each one specific to this resume.")
        List<String> strengths,

        @JsonPropertyDescription("Three to six concrete improvements, most important first. Each must point at something actually in (or missing from) this resume.")
        List<Suggestion> suggestions
) {
    public record Suggestion(
            @JsonPropertyDescription("Which part of the resume this is about: one of Content, Organization, Clarity, Formatting or Impact.")
            String area,

            @JsonPropertyDescription("The change to make, written as advice to the resume's owner, in one or two sentences.")
            String suggestion
    ) {
    }
}
