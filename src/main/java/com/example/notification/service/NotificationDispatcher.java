
package com.example.notification.service;

import com.example.notification.dto.NotificationStatus;
import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.sender.NotificationSender;
import com.example.notification.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final Map<NotificationType, NotificationSender> senders;
    private final NotificationRepository repository;
    private final NotificationWebSocketHandler socketHandler;

    public NotificationDispatcher(List<NotificationSender> senderList,
                                  NotificationRepository repository,
                                  NotificationWebSocketHandler socketHandler) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::getType, Function.identity()));
        this.repository = repository;
        this.socketHandler = socketHandler;
    }

    @Async
    public void dispatch(Notification notification) {
        log.info(">>> DISPATCH STARTED for id {}", notification.getId());
        try {
            NotificationSender sender = senders.get(notification.getType());
            if (sender == null) {
                throw new IllegalArgumentException("No sender found for type: " + notification.getType());
            }
            log.info(">>> SENDING {} to {}", notification.getType(), notification.getRecipient());
            sender.send(notification);
            notification.setStatus(NotificationStatus.SENT);
            log.info(">>> SENT OK");
        } catch (Exception e) {
            log.error(">>> SEND FAILED", e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            String error = String.valueOf(e.getMessage());
            notification.setErrorMessage(error.length() > 900 ? error.substring(0, 900) : error);
        }
        repository.save(notification);
        socketHandler.broadcast(notification);
        log.info(">>> STATUS SAVED: {}", notification.getStatus());
    }
}
