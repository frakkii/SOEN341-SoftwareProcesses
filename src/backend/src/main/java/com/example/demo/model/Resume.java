package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// A job seeker's resume. The job seeker's id is the primary key, so each job
// seeker has at most one resume and uploading a new one replaces the old row.
@Entity
@Table(name = "resume")
public class Resume {

    @Id
    @Column(name = "person_id")
    private Integer personId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Integer fileSize;

    private byte[] data;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    protected Resume() {
    }

    public Resume(Integer personId, String fileName, String contentType, byte[] data) {
        this.personId = personId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = data.length;
        this.data = data;
        this.uploadedAt = Instant.now();
    }

    public Integer getPersonId() {
        return personId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
