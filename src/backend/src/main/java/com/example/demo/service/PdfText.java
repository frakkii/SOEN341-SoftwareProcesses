package com.example.demo.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Pulls the text out of a PDF resume, one line per resume line, for demo mode's rule-based
// suggestions. (With an API key, Claude reads the PDF itself and this isn't used.)
public final class PdfText {

    // Bullet symbols as they come out of PDFs, including the private-use glyphs of the Symbol font
    static final Pattern BULLET_MARK = Pattern.compile("^[-*•▪●◦–\\uF0B7\\uF0A7]\\s*");

    private PdfText() {
    }

    // The resume's lines, or null when the file can't be read or has no text (a scanned image)
    public static String extract(byte[] pdf, int maxChars) {
        String raw;
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            raw = stripper.getText(document);
        } catch (IOException | RuntimeException e) {
            return null;
        }
        String text = joinWrappedLines(raw);
        if (text.isBlank()) {
            return null;
        }
        return text.length() > maxChars ? text.substring(0, maxChars) : text;
    }

    // A line that only wraps because of the page width continues in lowercase, so it is joined
    // to the line before it; a new bullet or a capitalised line starts a new resume line
    static String joinWrappedLines(String raw) {
        List<String> lines = new ArrayList<>();
        for (String piece : raw.split("\\R")) {
            String line = piece.strip();
            if (line.isEmpty()) {
                continue;
            }
            boolean continues = !lines.isEmpty()
                    && !BULLET_MARK.matcher(line).find()
                    && Character.isLowerCase(line.codePointAt(0));
            if (continues) {
                lines.set(lines.size() - 1, lines.get(lines.size() - 1) + " " + line);
            } else {
                lines.add(line);
            }
        }
        return String.join("\n", lines);
    }
}
