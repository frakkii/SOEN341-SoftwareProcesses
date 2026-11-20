package com.example.demo.controller;

import com.example.demo.exception.ApplicationException;
import com.example.demo.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationService.NotificationResponse> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly, HttpSession session) {
        Recipient recipient = recipient(session);
        return notifications.list(recipient.role(), recipient.id(), unreadOnly);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(HttpSession session) {
        Recipient recipient = recipient(session);
        return Map.of("count", notifications.unreadCount(recipient.role(), recipient.id()));
    }

    @PatchMapping("/{id}/read")
    public NotificationService.NotificationResponse markRead(@PathVariable Long id, HttpSession session) {
        Recipient recipient = recipient(session);
        return notifications.markRead(recipient.role(), recipient.id(), id);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(HttpSession session) {
        Recipient recipient = recipient(session);
        return Map.of("updated", notifications.markAllRead(recipient.role(), recipient.id()));
    }

    private static Recipient recipient(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        if (!"Job Seeker".equals(role) && !"Recruiter".equals(role)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Unsupported account role.");
        }
        return new Recipient(role, userId);
    }

    private record Recipient(String role, Integer id) {
    }
}
