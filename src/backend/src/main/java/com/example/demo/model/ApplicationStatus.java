package com.example.demo.model;

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
}
