



package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.persistence.Lob;

@Entity
public class Application {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotEmpty(message = "Application name is required")
    private String name;
    @Lob
    private byte[] document;
    private boolean isSubmitted;
    private String status;

    public Application() {
        this.name = "";
        this.document = null;
        this.isSubmitted = false;
        this.status = "Pending";
    }

    public Application(String name) {
        this.name = name;
        this.isSubmitted = false;
        this.status = "Pending";
    }

    public String getName() {
        return name;
    }

    public byte[] getDocument() {
        return document;
    }
    public Long getId() {
        return id;
    }
    public boolean getisSubmitted() {
        return isSubmitted;
    }

    public String getStatus() {
        return status;
    }
    public void uploadDocument(byte[] document) {
        this.document = document;
    }

    public void submit() {
        if (!isSubmitted) {
        isSubmitted = true;
        status = "Submitted";
        }
    }

    public void approve() {
        if (isSubmitted && ("Pending".equals(status) || "Submitted".equals(status))
                && !"Approved".equals(status) && !"Rejected".equals(status)) {
            status = "Approved";
        }
    }

    public void reject() {
        if (isSubmitted && ("Pending".equals(status) || "Submitted".equals(status))
                && !"Approved".equals(status) && !"Rejected".equals(status)) {
            status = "Rejected";
        }
    }
}
