package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String recipient,
        @Size(max = 255) String subject,
        @NotBlank @Size(max = 2000) String message
) {
}