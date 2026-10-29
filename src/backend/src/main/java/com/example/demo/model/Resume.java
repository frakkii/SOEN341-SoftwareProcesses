package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// One version of a job seeker's resume. Uploading a new one keeps the old rows and
// marks only the newest as current (V7); the current one is sent with applications.
@Entity
@Table(name = "resume")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    private boolean current;

    protected Resume() {
    }

    public Resume(Integer personId, String fileName, String contentType, byte[] data) {
        this.personId = personId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = data.length;
        this.data = data;
        this.uploadedAt = Instant.now();
        this.current = true;
    }

    public Long getId() {
        return id;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
