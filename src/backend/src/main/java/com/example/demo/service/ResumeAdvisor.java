package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization;

// Suggests line-by-line improvements to a resume. ClaudeResumeAdvisor asks the Claude API;
// DemoResumeAdvisor returns sample suggestions when no API key is configured (local runs, tests, CI).
public interface ResumeAdvisor {

    // Line-by-line suggested rewrites, used to build an improved resume
    ResumeOptimization optimize(ResumeContent resume);

    // True when the suggestions are sample data rather than a real AI review
    boolean isDemo();

    // A resume to review: either the PDF file itself or plain text extracted from a DOCX
    record ResumeContent(String fileName, byte[] pdf, String text) {
        public static ResumeContent ofPdf(String fileName, byte[] pdf) {
            return new ResumeContent(fileName, pdf, null);
        }

        public static ResumeContent ofText(String fileName, String text) {
            return new ResumeContent(fileName, null, text);
        }

        public boolean isPdf() {
            return pdf != null;
        }
    }
}
