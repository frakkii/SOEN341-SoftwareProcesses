package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// A job seeker's application to one job posting. A job seeker can apply to a
// posting only once (unique person_id + post_id in the database).
@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Integer personId;

    @Column(name = "post_id")
    private Integer postId;

    // Stored as the ApplicationStatus constant name, e.g. "UNDER_REVIEW"
    private String status;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Application() {
    }

    // Every new application starts as Applied
    public Application(Integer personId, Integer postId) {
        this.personId = personId;
        this.postId = postId;
        this.status = ApplicationStatus.APPLIED.name();
        this.appliedAt = Instant.now();
        this.updatedAt = this.appliedAt;
    }

    public Long getId() {
        return id;
    }

    public Integer getPersonId() {
        return personId;
    }

    public Integer getPostId() {
        return postId;
    }

    public ApplicationStatus getStatus() {
        return ApplicationStatus.valueOf(status);
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus.name();
        this.updatedAt = Instant.now();
    }
}
