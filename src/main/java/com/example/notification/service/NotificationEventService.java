package com.example.notification.service;

import com.example.notification.dto.NotificationResponse;
import com.example.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationEventService {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventService.class);

    private record Subscriber(String username, boolean admin) {}

    private final Map<SseEmitter, Subscriber> subscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username, boolean admin) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        subscribers.put(emitter, new Subscriber(username, admin));
        emitter.onCompletion(() -> subscribers.remove(emitter));
        emitter.onTimeout(() -> subscribers.remove(emitter));
        emitter.onError(e -> subscribers.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (Exception e) {
            subscribers.remove(emitter);
        }
        return emitter;
    }

    public void publish(Notification n) {
        NotificationResponse data = NotificationResponse.from(n);
        subscribers.forEach((emitter, sub) -> {
            boolean owner = sub.username().equals(n.getCreatedBy());
            if (!sub.admin() && !owner) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } catch (Exception e) {
                subscribers.remove(emitter);
                log.debug("SSE client removed: {}", e.getMessage());
            }
        });
    }
}