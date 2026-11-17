package com.example.demo.service;

import com.example.demo.dto.ResumeFeedback;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.ResumeAdvisor.ResumeContent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

// AI feedback on the job seeker's current resume. Works with PDF and DOCX; old .doc files
// are refused with a clear message rather than guessed at.
@Service
public class ResumeFeedbackService {

    // Far more than any real resume; stops a huge DOCX from being sent to the AI
    static final int MAX_TEXT_CHARS = 60_000;
    private static final HttpStatus UNPROCESSABLE = HttpStatus.valueOf(422);

    public static final String REVIEW_NOTICE =
            "This feedback is AI-generated. Review it before changing your resume; it can be wrong or miss context.";

    private final ResumeRepository resumes;
    private final ResumeAdvisor advisor;

    public ResumeFeedbackService(ResumeRepository resumes, ResumeAdvisor advisor) {
        this.resumes = resumes;
        this.advisor = advisor;
    }

    public record FeedbackResult(String fileName, boolean demo, String notice, ResumeFeedback feedback) {
    }

    public FeedbackResult reviewCurrentResume(Integer personId) {
        Resume resume = resumes.findFirstByPersonIdAndCurrentTrueOrderByUploadedAtDesc(personId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,
                        "Upload a resume first, then ask for feedback."));

        ResumeFeedback feedback = advisor.review(contentOf(resume));
        return new FeedbackResult(resume.getFileName(), advisor.isDemo(), REVIEW_NOTICE, feedback);
    }

    private static ResumeContent contentOf(Resume resume) {
        String name = resume.getFileName() == null ? "" : resume.getFileName().toLowerCase(Locale.ROOT);
        byte[] data = resume.getData();
        if (data == null || data.length == 0) {
            throw new ApplicationException(UNPROCESSABLE, "Your resume file is empty. Upload it again, then ask for feedback.");
        }

        if (name.endsWith(".pdf")) {
            if (!new String(data, 0, Math.min(5, data.length), StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw unreadable();
            }
            return ResumeContent.ofPdf(resume.getFileName(), data);
        }
        if (name.endsWith(".docx")) {
            String text;
            try {
                text = DocxText.extract(data);
            } catch (DocxText.UnreadableDocxException e) {
                throw unreadable();
            }
            if (text.isBlank()) {
                throw new ApplicationException(UNPROCESSABLE,
                        "We couldn't find any text in your resume. If it's a scanned image, upload a PDF or DOCX with real text.");
            }
            if (text.length() > MAX_TEXT_CHARS) {
                throw new ApplicationException(UNPROCESSABLE,
                        "Your resume is too long for AI feedback. Shorten it, then try again.");
            }
            return ResumeContent.ofText(resume.getFileName(), text);
        }
        throw new ApplicationException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "AI feedback works with PDF and DOCX resumes. Save your resume as a PDF or DOCX, upload it, then try again.");
    }

    private static ApplicationException unreadable() {
        return new ApplicationException(UNPROCESSABLE,
                "We couldn't read your resume file. Save it again as a PDF or DOCX, upload it, then try again.");
    }
}
