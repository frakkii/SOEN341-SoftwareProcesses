package com.example.demo.service;

import com.example.demo.dto.ResumeFeedback;

// Gives feedback on a resume. ClaudeResumeAdvisor asks the Claude API; DemoResumeAdvisor
// returns sample feedback when no API key is configured (local runs, tests, CI).
public interface ResumeAdvisor {

    ResumeFeedback review(ResumeContent resume);

    // True when the feedback is sample data rather than a real AI review
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
