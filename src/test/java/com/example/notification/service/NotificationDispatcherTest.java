
package com.example.notification.service;

import com.example.notification.dto.NotificationStatus;
import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDispatcherTest {

    @Mock NotificationSender emailSender;
    @Mock NotificationRepository repository;
    @Mock NotificationEventService events;

    NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        when(emailSender.getType()).thenReturn(NotificationType.EMAIL);
        dispatcher = new NotificationDispatcher(List.of(emailSender), repository, events);
    }

    private Notification pending() {
        Notification n = new Notification();
        n.setType(NotificationType.EMAIL);
        n.setRecipient("a@b.com");
        n.setStatus(NotificationStatus.PENDING);
        return n;
    }

    @Test
    void dispatch_success_marksSent() throws Exception {
        Notification n = pending();

        dispatcher.dispatch(n);

        assertEquals(NotificationStatus.SENT, n.getStatus());
        verify(repository).save(n);
        verify(events).publish(n);
    }

    @Test
    void dispatch_failure_marksFailedAndCountsRetry() throws Exception {
        Notification n = pending();
        doThrow(new RuntimeException("Authentication failed"))
                .when(emailSender).send(n);

        dispatcher.dispatch(n);

        assertEquals(NotificationStatus.FAILED, n.getStatus());
        assertEquals(1, n.getRetryCount());
        assertEquals("Authentication failed", n.getErrorMessage());
        verify(repository).save(n);
        verify(events).publish(n);
    }
}
