package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.dto.NotificationStatus;
import com.example.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationResponse send(
            @Valid @RequestBody NotificationRequest request,
            Authentication authentication) {

        return NotificationResponse.from(
                service.create(request, authentication.getName()));
    }

    @GetMapping
    public Page<NotificationResponse> history(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        PageRequest pageable =
                PageRequest.of(page, Math.min(size, 100), Sort.by("id").descending());

        return service.getForUser(auth.getName(), isAdmin(auth), status, pageable)
                .map(NotificationResponse::from);
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return service.getStats();
    }

    @GetMapping("/{id}")
    public NotificationResponse getOne(
            @PathVariable Long id,
            Authentication auth) {

        return NotificationResponse.from(
                service.getByIdForUser(id, auth.getName(), isAdmin(auth)));
    }

    @PostMapping("/{id}/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationResponse resend(
            @PathVariable Long id,
            Authentication auth) {

        return NotificationResponse.from(
                service.resendForUser(id, auth.getName(), isAdmin(auth)));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}