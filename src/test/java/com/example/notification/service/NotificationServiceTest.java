package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationStatus;
import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import com.example.notification.exception.NotificationNotFoundException;
import com.example.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repository;
    @Mock NotificationDispatcher dispatcher;
    @InjectMocks NotificationService service;

    private Notification notification(NotificationStatus status, String owner) {
        Notification n = new Notification();
        n.setType(NotificationType.EMAIL);
        n.setRecipient("a@b.com");
        n.setStatus(status);
        n.setCreatedBy(owner);
        n.setRetryCount(3);
        n.setErrorMessage("boom");
        return n;
    }

    @Test
    void create_savesPendingAndDispatches() {
        when(repository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
        NotificationRequest req = new NotificationRequest(
                NotificationType.EMAIL, "a@b.com", "Hi", "Hello");

        Notification result = service.create(req, "soraj");

        assertEquals(NotificationStatus.PENDING, result.getStatus());
        assertEquals("soraj", result.getCreatedBy());
        verify(dispatcher).dispatch(result);
    }

    @Test
    void create_invalidEmail_throwsAndDoesNotSave() {
        NotificationRequest req = new NotificationRequest(
                NotificationType.EMAIL, "not-an-email", "Hi", "Hello");

        assertThrows(IllegalArgumentException.class, () -> service.create(req, "soraj"));
        verify(repository, never()).save(any());
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void create_invalidPhone_throws() {
        NotificationRequest req = new NotificationRequest(
                NotificationType.SMS, "01700000000", "Hi", "Hello");

        assertThrows(IllegalArgumentException.class, () -> service.create(req, "soraj"));
    }

    @Test
    void resend_nonFailed_throws() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(notification(NotificationStatus.SENT, "soraj")));

        assertThrows(IllegalStateException.class, () -> service.resend(1L));
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void resend_failed_resetsAndDispatches() {
        Notification n = notification(NotificationStatus.FAILED, "soraj");
        when(repository.findById(1L)).thenReturn(Optional.of(n));
        when(repository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification result = service.resend(1L);

        assertEquals(NotificationStatus.PENDING, result.getStatus());
        assertEquals(0, result.getRetryCount());
        assertNull(result.getErrorMessage());
        verify(dispatcher).dispatch(result);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getByIdForUser_otherUser_throwsNotFound() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(notification(NotificationStatus.SENT, "soraj")));

        assertThrows(NotificationNotFoundException.class,
                () -> service.getByIdForUser(1L, "rahim", false));
    }

    @Test
    void getByIdForUser_owner_ok() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(notification(NotificationStatus.SENT, "soraj")));

        assertNotNull(service.getByIdForUser(1L, "soraj", false));
    }

    @Test
    void getByIdForUser_admin_canSeeAnyone() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(notification(NotificationStatus.SENT, "soraj")));

        assertNotNull(service.getByIdForUser(1L, "admin", true));
    }
}