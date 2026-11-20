package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.JobPosting;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PersonUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    // Notification types; the notifications page (notifications.js, shell.js) shows only these
    public static final String APPLICATION_STATUS_UPDATED = "APPLICATION_STATUS_UPDATED";
    public static final String INTERVIEW_INVITATION = "INTERVIEW_INVITATION";
    public static final String DEADLINE_REMINDER = "DEADLINE_REMINDER";
    public static final String NEW_APPLICATION = "NEW_APPLICATION";
    public static final String NEW_JOB_POSTING = "NEW_JOB_POSTING";

    private final NotificationRepository notifications;
    private final PersonUserRepository jobSeekers;
    private final NotificationPushService notificationPush;

    public NotificationService(NotificationRepository notifications, PersonUserRepository jobSeekers,
                               NotificationPushService notificationPush) {
        this.notifications = notifications;
        this.jobSeekers = jobSeekers;
        this.notificationPush = notificationPush;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String role, Integer userId, boolean unreadOnly) {
        List<Notification> records = unreadOnly
                ? notifications.findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(role, userId)
                : notifications.findByRecipientRoleAndRecipientIdOrderByCreatedAtDesc(role, userId);
        return records.stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String role, Integer userId) {
        return notifications.countByRecipientRoleAndRecipientIdAndReadAtIsNull(role, userId);
    }

    @Transactional
    public NotificationResponse markRead(String role, Integer userId, Long notificationId) {
        Notification notification = notifications.findByIdAndRecipientRoleAndRecipientId(
                        notificationId, role, userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Notification not found."));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String role, Integer userId) {
        List<Notification> unread = notifications
                .findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(role, userId);
        unread.forEach(Notification::markRead);
        notifications.saveAll(unread);
        return unread.size();
    }

    public Notification create(String role, Integer userId, String title, String message,
                               String type, String link, String deduplicationKey) {
        if (deduplicationKey != null) {
            Notification existing = notifications.findByDeduplicationKey(deduplicationKey).orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        try {
            Notification created = notifications.saveAndFlush(new Notification(
                    role, userId, title, message, type, link, deduplicationKey));
            publishAfterCommit(created);
            return created;
        } catch (DataIntegrityViolationException exception) {
            if (deduplicationKey != null) {
                return notifications.findByDeduplicationKey(deduplicationKey).orElseThrow(() -> exception);
            }
            throw exception;
        }
    }

    @Transactional
    public int notifyJobSeekersOfNewPosting(JobPosting posting) {
        List<Notification> newJobNotifications = jobSeekers.findAllIds().stream()
                .map(jobSeekerId -> new Notification(
                        "Job Seeker",
                        jobSeekerId,
                        "New job posted: " + posting.getTitle(),
                        "Applications are open until " + posting.getApplicationDeadline() + ".",
                        NEW_JOB_POSTING,
                        "/job.html?id=" + posting.getId(),
                        "NEW_JOB_POSTING:" + posting.getId() + ":" + jobSeekerId))
                .toList();
        notifications.saveAllAndFlush(newJobNotifications);
        newJobNotifications.forEach(this::publishAfterCommit);
        return newJobNotifications.size();
    }

    private void publishAfterCommit(Notification notification) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            notificationPush.publish(notification.getRecipientRole(), notification.getRecipientId(),
                    NotificationResponse.from(notification));
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationPush.publish(notification.getRecipientRole(), notification.getRecipientId(),
                        NotificationResponse.from(notification));
            }
        });
    }

    public record NotificationResponse(
            Long id,
            String title,
            String message,
            String type,
            String link,
            Instant createdAt,
            boolean read,
            Instant readAt
    ) {
        private static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getType(),
                    notification.getLink(),
                    notification.getCreatedAt(),
                    notification.isRead(),
                    notification.getReadAt());
        }
    }
}
