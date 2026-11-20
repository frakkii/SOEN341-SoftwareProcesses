package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
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
            return notifications.saveAndFlush(new Notification(
                    role, userId, title, message, type, link, deduplicationKey));
        } catch (DataIntegrityViolationException exception) {
            if (deduplicationKey != null) {
                return notifications.findByDeduplicationKey(deduplicationKey).orElseThrow(() -> exception);
            }
            throw exception;
        }
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
