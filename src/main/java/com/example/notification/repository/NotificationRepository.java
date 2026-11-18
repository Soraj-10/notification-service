package com.example.notification.repository;

import com.example.notification.dto.NotificationStatus;
import com.example.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    long countByStatus(NotificationStatus status);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}