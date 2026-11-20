package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientRoleAndRecipientIdOrderByCreatedAtDesc(
            String recipientRole, Integer recipientId);

    List<Notification> findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(
            String recipientRole, Integer recipientId);

    Optional<Notification> findByIdAndRecipientRoleAndRecipientId(
            Long id, String recipientRole, Integer recipientId);

    long countByRecipientRoleAndRecipientIdAndReadAtIsNull(String recipientRole, Integer recipientId);

    Optional<Notification> findByDeduplicationKey(String deduplicationKey);
}
