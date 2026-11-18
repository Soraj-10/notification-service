package com.example.notification.service.sender;

import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsSender implements NotificationSender {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

    @Override
    public void send(Notification notification) {

        Message.creator(
                new PhoneNumber(notification.getRecipient()),
                new PhoneNumber(fromNumber),
                notification.getMessage()
        ).create();
    }
}