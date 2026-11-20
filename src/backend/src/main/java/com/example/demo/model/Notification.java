package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_role", nullable = false, length = 20)
    private String recipientRole;

    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(length = 500)
    private String link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "deduplication_key", unique = true, length = 255)
    private String deduplicationKey;

    protected Notification() {
    }

    public Notification(String recipientRole, Integer recipientId, String title, String message,
                        String type, String link, String deduplicationKey) {
        this.recipientRole = recipientRole;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.link = link;
        this.deduplicationKey = deduplicationKey;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }
}
