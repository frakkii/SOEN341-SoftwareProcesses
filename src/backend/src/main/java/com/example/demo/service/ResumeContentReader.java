package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Resume;
import com.example.demo.service.ResumeAdvisor.ResumeContent;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

// Turns a stored resume into something the AI can read: PDFs are passed as they are, DOCX files
// as their extracted text. Old .doc files are refused with a clear message rather than guessed at.
final class ResumeContentReader {

    // Far more than any real resume; stops a huge DOCX from being sent to the AI
    static final int MAX_TEXT_CHARS = 60_000;
    private static final HttpStatus UNPROCESSABLE = HttpStatus.valueOf(422);

    private ResumeContentReader() {
    }

    static ResumeContent read(Resume resume) {
        String name = resume.getFileName() == null ? "" : resume.getFileName().toLowerCase(Locale.ROOT);
        byte[] data = resume.getData();
        if (data == null || data.length == 0) {
            throw new ApplicationException(UNPROCESSABLE, "Your resume file is empty. Upload it again, then try again.");
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
                        "Your resume is too long for the AI optimizer. Shorten it, then try again.");
            }
            return ResumeContent.ofText(resume.getFileName(), text);
        }
        throw new ApplicationException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "The AI optimizer works with PDF and DOCX resumes. Save your resume as a PDF or DOCX, upload it, then try again.");
    }

    private static ApplicationException unreadable() {
        return new ApplicationException(UNPROCESSABLE,
                "We couldn't read your resume file. Save it again as a PDF or DOCX, upload it, then try again.");
    }
}
