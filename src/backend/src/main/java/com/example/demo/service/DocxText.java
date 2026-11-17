package com.example.demo.service;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// Pulls the plain text out of a .docx file (a ZIP archive whose body is word/document.xml),
// one line per paragraph. No extra library needed: only the document text is used.
public final class DocxText {

    private static final String WORD_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    // Resumes are tiny; this stops a crafted file from expanding to gigabytes ("zip bomb")
    private static final long MAX_XML_BYTES = 20L * 1024 * 1024;

    private DocxText() {
    }

    // Thrown when the bytes are not a readable .docx
    public static class UnreadableDocxException extends Exception {
        public UnreadableDocxException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static String extract(byte[] docx) throws UnreadableDocxException {
        byte[] xml;
        try {
            xml = documentXml(docx);
        } catch (IOException e) {
            throw new UnreadableDocxException("Not a readable .docx file", e);
        }
        if (xml == null) {
            throw new UnreadableDocxException("The .docx file has no word/document.xml", null);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // Uploaded files are untrusted: no DTDs, no external entities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            NodeList paragraphs = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml))
                    .getElementsByTagNameNS(WORD_NS, "p");
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < paragraphs.getLength(); i++) {
                StringBuilder line = new StringBuilder();
                appendRuns(paragraphs.item(i), line);
                if (!line.toString().isBlank()) {
                    text.append(line.toString().strip()).append('\n');
                }
            }
            return text.toString().strip();
        } catch (Exception e) {
            throw new UnreadableDocxException("The .docx document text could not be parsed", e);
        }
    }

    // Text runs (w:t), tabs and line breaks inside one paragraph, in document order
    private static void appendRuns(Node node, StringBuilder line) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && WORD_NS.equals(element.getNamespaceURI())) {
                switch (element.getLocalName()) {
                    case "t" -> line.append(element.getTextContent());
                    case "tab" -> line.append('\t');
                    case "br", "cr" -> line.append('\n');
                    case "p" -> { }   // nested paragraphs (e.g. in text boxes) are visited on their own
                    default -> appendRuns(element, line);
                }
            }
        }
    }

    private static byte[] documentXml(byte[] docx) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                if ("word/document.xml".equals(entry.getName())) {
                    return readLimited(zip);
                }
            }
        }
        return null;
    }

    private static byte[] readLimited(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        for (int n; (n = in.read(buffer)) != -1; ) {
            total += n;
            if (total > MAX_XML_BYTES) {
                throw new IOException("word/document.xml is too large");
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
