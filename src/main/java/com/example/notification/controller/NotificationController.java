package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationStatus;
import com.example.notification.entity.Notification;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    public Notification send(@Valid @RequestBody NotificationRequest request) {
        return service.create(request);
    }

    @GetMapping
    public Page<Notification> history(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (status == null) {
            return service.getAll(pageable);
        }
        return service.getByStatus(status, pageable);
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return service.getStats();
    }

    @GetMapping("/{id}")
    public Notification getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Notification resend(@PathVariable Long id) {
        return service.resend(id);
    }
}