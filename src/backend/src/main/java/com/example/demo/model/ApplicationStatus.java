package com.example.demo.model;

import java.util.Arrays;
import java.util.Optional;

// The stages a job application goes through. The database stores the constant
// name (e.g. UNDER_REVIEW); the API shows the label (e.g. "Under Review").
public enum ApplicationStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    INTERVIEW("Interview"),
    OFFERED("Offered"),
    REJECTED("Rejected");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Offered and Rejected are decisions, so the application stops there
    public boolean isFinal() {
        return this == OFFERED || this == REJECTED;
    }

    // Applications only move forward (stages can be skipped), and a candidate
    // can be rejected at any stage before a decision
    public boolean canMoveTo(ApplicationStatus next) {
        return !isFinal() && next.ordinal() > ordinal();
    }

    // Accepts either the label ("Under Review") or the constant name ("UNDER_REVIEW")
    public static Optional<ApplicationStatus> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(trimmed) || s.name().equalsIgnoreCase(trimmed))
                .findFirst();
    }
}
