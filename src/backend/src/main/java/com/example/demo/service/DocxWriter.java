package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Writes a simple .docx file: a ZIP with the few parts Word needs, and one paragraph per
// line. Like DocxText, no extra library: resumes and suggestion reports only need styled
// paragraphs, which a few lines of WordprocessingML cover.
public final class DocxWriter {

    public static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public enum Style { TITLE, HEADING, BULLET, TEXT, NOTE }

    public record Paragraph(Style style, String text) {
        public static Paragraph of(Style style, String text) {
            return new Paragraph(style, text);
        }
    }

    private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

    private static final String CONTENT_TYPES = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">\
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>\
            <Default Extension="xml" ContentType="application/xml"/>\
            <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>\
            <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>\
            </Types>""";

    private static final String PACKAGE_RELS = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">\
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>\
            </Relationships>""";

    private static final String DOCUMENT_RELS = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">\
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>\
            </Relationships>""";

    // Document-wide defaults: Calibri 11pt with a little space after each paragraph
    private static final String STYLES = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:styles xmlns:w="%s"><w:docDefaults>\
            <w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Calibri" w:cs="Calibri"/>\
            <w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr></w:rPrDefault>\
            <w:pPrDefault><w:pPr><w:spacing w:after="80" w:line="264" w:lineRule="auto"/></w:pPr></w:pPrDefault>\
            </w:docDefaults></w:styles>""".formatted(W);

    private DocxWriter() {
    }

    public static byte[] write(List<Paragraph> paragraphs) {
        StringBuilder body = new StringBuilder();
        for (Paragraph paragraph : paragraphs) {
            appendParagraph(body, paragraph);
        }
        String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<w:document xmlns:w=\"" + W + "\"><w:body>" + body
                // US Letter with 0.75" margins
                + "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
                + "<w:pgMar w:top=\"1080\" w:right=\"1080\" w:bottom=\"1080\" w:left=\"1080\""
                + " w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/></w:sectPr>"
                + "</w:body></w:document>";

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            put(zip, "[Content_Types].xml", CONTENT_TYPES);
            put(zip, "_rels/.rels", PACKAGE_RELS);
            put(zip, "word/_rels/document.xml.rels", DOCUMENT_RELS);
            put(zip, "word/styles.xml", STYLES);
            put(zip, "word/document.xml", document);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the .docx file", e);   // in-memory, so not expected
        }
        return bytes.toByteArray();
    }

    private static void appendParagraph(StringBuilder out, Paragraph paragraph) {
        String text = paragraph.text() == null ? "" : paragraph.text();
        String pPr = "";
        String rPr = "";
        switch (paragraph.style()) {
            case TITLE -> {
                pPr = "<w:spacing w:after=\"120\"/>";
                rPr = "<w:b/><w:sz w:val=\"32\"/><w:szCs w:val=\"32\"/>";
            }
            case HEADING -> {
                pPr = "<w:keepNext/><w:spacing w:before=\"200\" w:after=\"60\"/>";
                rPr = "<w:b/><w:color w:val=\"1F2937\"/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>";
            }
            case BULLET -> {
                pPr = "<w:spacing w:after=\"40\"/><w:ind w:left=\"360\" w:hanging=\"240\"/>";
                text = "•\t" + text;
            }
            case NOTE -> rPr = "<w:i/><w:color w:val=\"6B7280\"/><w:sz w:val=\"18\"/><w:szCs w:val=\"18\"/>";
            case TEXT -> { }
        }

        out.append("<w:p>");
        if (!pPr.isEmpty()) {
            out.append("<w:pPr>").append(pPr).append("</w:pPr>");
        }
        out.append("<w:r>");
        if (!rPr.isEmpty()) {
            out.append("<w:rPr>").append(rPr).append("</w:rPr>");
        }
        appendRunText(out, text);
        out.append("</w:r></w:p>");
    }

    // Text with tabs and line breaks turned into their Word elements
    private static void appendRunText(StringBuilder out, String text) {
        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t' || c == '\n') {
                flush(out, chunk);
                out.append(c == '\t' ? "<w:tab/>" : "<w:br/>");
            } else if (c != '\r') {
                chunk.append(c);
            }
        }
        flush(out, chunk);
    }

    private static void flush(StringBuilder out, StringBuilder chunk) {
        if (chunk.length() > 0) {
            out.append("<w:t xml:space=\"preserve\">").append(escape(chunk)).append("</w:t>");
            chunk.setLength(0);
        }
    }

    // XML-escapes text and drops characters XML 1.0 can't contain (control characters)
    static String escape(CharSequence text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> {
                    if (c >= 0x20 && c != 0xFFFE && c != 0xFFFF) {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
