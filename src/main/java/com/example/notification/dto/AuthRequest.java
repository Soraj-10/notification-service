package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 6, max = 72) String password
) {
}