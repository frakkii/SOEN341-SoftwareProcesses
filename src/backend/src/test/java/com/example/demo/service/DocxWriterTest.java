package com.example.demo.service;

import com.example.demo.service.DocxWriter.Paragraph;
import com.example.demo.service.DocxWriter.Style;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocxWriterTest {

    @Test
    void writesAPackageWordCanOpen() throws Exception {
        byte[] docx = DocxWriter.write(List.of(Paragraph.of(Style.TEXT, "Hello")));

        Set<String> parts = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                parts.add(entry.getName());
            }
        }
        assertThat(parts).contains("[Content_Types].xml", "_rels/.rels", "word/document.xml",
                "word/_rels/document.xml.rels", "word/styles.xml");
    }

    @Test
    void textReadsBackOneLinePerParagraph() throws Exception {
        byte[] docx = DocxWriter.write(List.of(
                Paragraph.of(Style.TITLE, "Jane Doe"),
                Paragraph.of(Style.HEADING, "Experience"),
                Paragraph.of(Style.BULLET, "Led the campus website"),
                Paragraph.of(Style.TEXT, "2024\t2026"),
                Paragraph.of(Style.NOTE, "Note")));

        assertThat(DocxText.extract(docx)).isEqualTo(
                "Jane Doe\nExperience\n•\tLed the campus website\n2024\t2026\nNote");
    }

    @Test
    void escapesMarkupAndDropsCharactersXmlCannotHold() throws Exception {
        byte[] docx = DocxWriter.write(List.of(
                Paragraph.of(Style.TEXT, "R&D <team> \"lead\"\u0007 & more")));

        assertThat(DocxText.extract(docx)).isEqualTo("R&D <team> \"lead\" & more");
    }

    @Test
    void outputNamesKeepTheOriginalNameAndFitTheUploadLimit() {
        assertThat(ResumeOptimizationService.outputName("Jane CV.pdf", "-improved")).isEqualTo("Jane CV-improved.docx");
        assertThat(ResumeOptimizationService.outputName(".docx", "-improved")).isEqualTo(".docx-improved.docx");
        assertThat(ResumeOptimizationService.outputName("x".repeat(255) + ".docx", "-suggestions")).hasSize(255);
    }
}
