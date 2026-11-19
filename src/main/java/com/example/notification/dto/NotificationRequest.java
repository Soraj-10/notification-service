package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String recipient,
        @NotBlank String subject,
        @NotBlank String message
) {
}