package com.example.notification.service.sender;

import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;

public interface NotificationSender {

    NotificationType getType();

    void send(Notification notification);
}