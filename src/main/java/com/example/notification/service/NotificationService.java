
package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationStatus;
import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import com.example.notification.exception.NotificationNotFoundException;
import com.example.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class NotificationService {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;

    public NotificationService(NotificationRepository repository,
                               NotificationDispatcher dispatcher) {
        this.repository = repository;
        this.dispatcher = dispatcher;
    }

    public Notification create(NotificationRequest request, String username) {
        validateRecipient(request.type(), request.recipient());

        Notification notification = new Notification();
        notification.setType(request.type());
        notification.setRecipient(request.recipient());
        notification.setSubject(request.subject());
        notification.setMessage(request.message());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setCreatedBy(username);

        Notification saved = repository.save(notification);
        dispatcher.dispatch(saved);
        return saved;
    }

    private void validateRecipient(NotificationType type, String recipient) {
        if (type == NotificationType.EMAIL && !EMAIL.matcher(recipient).matches()) {
            throw new IllegalArgumentException("Invalid email address: " + recipient);
        }

        if (type == NotificationType.SMS && !PHONE.matcher(recipient).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number. Use international format, e.g. +8801XXXXXXXXX");
        }
    }

    public Page<Notification> getForUser(String username, boolean admin,
                                         NotificationStatus status, Pageable pageable) {
        if (admin) {
            return status == null
                    ? repository.findAll(pageable)
                    : repository.findByStatus(status, pageable);
        }

        return status == null
                ? repository.findByCreatedBy(username, pageable)
                : repository.findByCreatedByAndStatus(username, status, pageable);
    }

    public Notification getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    public Notification getByIdForUser(Long id, String username, boolean admin) {
        Notification notification = getById(id);

        if (!admin && !username.equals(notification.getCreatedBy())) {
            throw new NotificationNotFoundException(id);
        }

        return notification;
    }

    public Notification resendForUser(Long id, String username, boolean admin) {
        getByIdForUser(id, username, admin);
        return resend(id);
    }

    public Notification resend(Long id) {
        Notification notification = getById(id);

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException(
                    "Only FAILED notifications can be resent. Current status: "
                            + notification.getStatus());
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setErrorMessage(null);
        notification.setRetryCount(0);

        Notification saved = repository.save(notification);
        dispatcher.dispatch(saved);
        return saved;
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", repository.count());
        stats.put("pending", repository.countByStatus(NotificationStatus.PENDING));
        stats.put("sent", repository.countByStatus(NotificationStatus.SENT));
        stats.put("failed", repository.countByStatus(NotificationStatus.FAILED));
        return stats;
    }
}
