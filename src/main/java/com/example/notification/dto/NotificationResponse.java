package com.example.notification.dto;

import com.example.notification.entity.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String recipient,
        String subject,
        String message,
        NotificationStatus status,
        String errorMessage,
        int retryCount,
        LocalDateTime createdAt) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getRecipient(), n.getSubject(),
                n.getMessage(), n.getStatus(), n.getErrorMessage(),
                n.getRetryCount(), n.getCreatedAt());
    }
}