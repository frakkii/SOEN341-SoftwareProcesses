package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization;
import com.example.demo.dto.ResumeOptimization.Line;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Demo mode reads the text of PDF resumes, so PDF uploads get suggestions for their own lines
class PdfTextTest {

    // A one-page PDF with each string on its own line, like a resume exported from Word or Google Docs
    static byte[] pdf(String... lines) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.newLineAtOffset(50, 720);
                for (String line : lines) {
                    content.showText(line);
                    content.newLineAtOffset(0, -16);
                }
                content.endText();
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void readsEachLineOfAPdf() throws Exception {
        String text = PdfText.extract(pdf("Alex Tremblay", "EXPERIENCE", "- Worked on the flight software"), 10_000);
        assertThat(text).isEqualTo("Alex Tremblay\nEXPERIENCE\n- Worked on the flight software");
    }

    @Test
    void joinsABulletThatWrapsOntoTheNextLine() {
        String raw = "EXPERIENCE\n- Worked on the ground station dashboard used\nduring every launch\n- Responsible for unit tests\nSKILLS\n";
        assertThat(PdfText.joinWrappedLines(raw)).isEqualTo(
                "EXPERIENCE\n- Worked on the ground station dashboard used during every launch\n- Responsible for unit tests\nSKILLS");
    }

    @Test
    void notAPdfOrNoTextGivesNull() throws Exception {
        assertThat(PdfText.extract("%PDF-1.4 sample".getBytes(StandardCharsets.US_ASCII), 10_000)).isNull();
        assertThat(PdfText.extract(pdf(), 10_000)).isNull();   // a page with no text, like a scanned image
    }

    @Test
    void demoModeSuggestsRewritesForThePdfsOwnLines() throws Exception {
        byte[] resume = pdf("Alex Tremblay", "EXPERIENCE",
                "- Worked on the flight software for the rocket team",
                "- Responsible for writing unit tests");
        ResumeOptimization result = new DemoResumeAdvisor()
                .optimize(ResumeAdvisor.ResumeContent.ofPdf("cv.pdf", resume));

        assertThat(result.summary()).doesNotContain("aren't from your resume");
        Map<String, String> rewrites = result.lines().stream()
                .collect(Collectors.toMap(Line::original, Line::improved));
        assertThat(rewrites)
                .containsEntry("Alex Tremblay", "")
                .containsEntry("Worked on the flight software for the rocket team", "Built the flight software for the rocket team")
                .containsEntry("Responsible for writing unit tests", "Took ownership of writing unit tests");
    }
}
