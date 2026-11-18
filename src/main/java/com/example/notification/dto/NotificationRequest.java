package com.example.notification.dto;

import com.twilio.http.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String to,
        String subject,
        @NotBlank String message
) {
}