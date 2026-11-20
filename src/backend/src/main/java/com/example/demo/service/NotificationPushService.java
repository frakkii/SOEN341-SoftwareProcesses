package com.example.demo.service;

import com.example.demo.service.NotificationService.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class NotificationPushService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPushService.class);
    private final ConcurrentMap<Recipient, Set<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String role, Integer userId) {
        Recipient recipient = new Recipient(role, userId);
        SseEmitter emitter = new SseEmitter(0L);
        subscribers.computeIfAbsent(recipient, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);

        Runnable removeSubscriber = () -> remove(recipient, emitter);
        emitter.onCompletion(removeSubscriber);
        emitter.onTimeout(removeSubscriber);
        emitter.onError(error -> removeSubscriber.run());
        return emitter;
    }

    public void publish(String role, Integer userId, NotificationResponse notification) {
        Set<SseEmitter> activeSubscribers = subscribers.get(new Recipient(role, userId));
        if (activeSubscribers == null) {
            return;
        }

        for (SseEmitter emitter : activeSubscribers) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException exception) {
                remove(new Recipient(role, userId), emitter);
                emitter.completeWithError(exception);
                logger.warn("Could not deliver a live notification; the subscriber was disconnected.", exception);
            }
        }
    }

    private void remove(Recipient recipient, SseEmitter emitter) {
        Set<SseEmitter> activeSubscribers = subscribers.get(recipient);
        if (activeSubscribers != null) {
            activeSubscribers.remove(emitter);
            if (activeSubscribers.isEmpty()) {
                subscribers.remove(recipient, activeSubscribers);
            }
        }
    }

    private record Recipient(String role, Integer userId) {
    }
}
