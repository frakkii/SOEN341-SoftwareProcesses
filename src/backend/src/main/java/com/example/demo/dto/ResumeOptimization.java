package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.io.Serializable;
import java.util.List;

// Line-by-line AI suggestions for a resume. The Claude API's structured output schema is
// generated from this record (see ClaudeResumeAdvisor), so the descriptions below are part
// of the prompt. Serializable because the latest result is kept in the user's session until
// they download it or save it as a new resume version.
public record ResumeOptimization(
        @JsonPropertyDescription("Two or three sentences on the main ways your suggestions improve this resume, addressed to its owner as \"you\".")
        String summary,

        @JsonPropertyDescription("Every line of the resume, in reading order, each with your suggested rewrite if it needs one.")
        List<Line> lines
) implements Serializable {

    public record Line(
            @JsonPropertyDescription("What the line is: \"heading\" for a section title or the person's name, \"bullet\" for a bullet point, or \"text\" for anything else.")
            String kind,

            @JsonPropertyDescription("The line exactly as it is written in the resume, without any bullet symbol. Empty only when suggesting a brand-new line that the resume is missing.")
            String original,

            @JsonPropertyDescription("Your rewrite of the line, without any bullet symbol. Empty when the line is fine as it is.")
            String improved,

            @JsonPropertyDescription("When there is a rewrite: one of Content, Clarity, Impact, Formatting or Concision. Otherwise empty.")
            String area,

            @JsonPropertyDescription("When there is a rewrite: one sentence telling the resume's owner why it is better. Otherwise empty.")
            String reason
    ) implements Serializable {
    }
}
