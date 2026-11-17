package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocxTextTest {

    private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

    private static byte[] zip(String documentXml) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write(documentXml.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    @Test
    void readsParagraphsRunsTabsAndBreaksInOrder() throws Exception {
        String xml = "<w:document " + W + "><w:body>"
                + "<w:p><w:r><w:t>Jane</w:t></w:r><w:r><w:t xml:space=\"preserve\"> Doe</w:t></w:r></w:p>"
                + "<w:p><w:r><w:t>Skills:</w:t><w:tab/><w:t>Java</w:t></w:r></w:p>"
                + "<w:p></w:p>"
                + "<w:p><w:r><w:t>Line one</w:t><w:br/><w:t>Line two</w:t></w:r></w:p>"
                + "</w:body></w:document>";

        assertThat(DocxText.extract(zip(xml))).isEqualTo("Jane Doe\nSkills:\tJava\nLine one\nLine two");
    }

    @Test
    void tableCellsAreRead() throws Exception {
        String xml = "<w:document " + W + "><w:body><w:tbl><w:tr><w:tc>"
                + "<w:p><w:r><w:t>2024</w:t></w:r></w:p></w:tc><w:tc>"
                + "<w:p><w:r><w:t>Intern at Acme</w:t></w:r></w:p>"
                + "</w:tc></w:tr></w:tbl></w:body></w:document>";

        assertThat(DocxText.extract(zip(xml))).isEqualTo("2024\nIntern at Acme");
    }

    @Test
    void rejectsFilesThatAreNotDocx() {
        assertThatThrownBy(() -> DocxText.extract("%PDF-1.4 not a zip".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(DocxText.UnreadableDocxException.class);
    }

    @Test
    void rejectsAZipWithoutADocumentBody() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("other.txt"));
            zip.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        assertThatThrownBy(() -> DocxText.extract(bytes.toByteArray()))
                .isInstanceOf(DocxText.UnreadableDocxException.class);
    }

    @Test
    void refusesDoctypesSoUploadedFilesCannotReadServerFiles() throws Exception {
        // Classic XXE: an external entity pointing at a local file
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE d [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<w:document " + W + "><w:body><w:p><w:r><w:t>&x;</w:t></w:r></w:p></w:body></w:document>";

        assertThatThrownBy(() -> DocxText.extract(zip(xml)))
                .isInstanceOf(DocxText.UnreadableDocxException.class);
    }
}
