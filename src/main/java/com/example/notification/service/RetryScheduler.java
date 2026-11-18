package com.example.notification.service;

import com.example.notification.dto.NotificationStatus;
import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);
    private static final int MAX_ATTEMPTS = 3;

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;

    public RetryScheduler(NotificationRepository repository,
                          NotificationDispatcher dispatcher) {
        this.repository = repository;
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void retryFailed() {
        List<Notification> failed = repository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_ATTEMPTS);

        for (Notification n : failed) {
            log.info(">>> AUTO RETRY id {} (attempt {})", n.getId(), n.getRetryCount() + 1);
            n.setStatus(NotificationStatus.PENDING);
            repository.save(n);
            dispatcher.dispatch(n);
        }
    }
}