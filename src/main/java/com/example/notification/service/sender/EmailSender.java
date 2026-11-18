package com.example.notification.service.sender;

import com.example.notification.dto.NotificationType;
import com.example.notification.entity.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String mailPassword;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(Notification notification) {

        // TEMPORARY CHECK: remove these 2 lines after testing
        System.out.println(">>> USERNAME USED = " + fromEmail);
        System.out.println(">>> PASSWORD LENGTH = " + (mailPassword == null ? 0 : mailPassword.length()));

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(fromEmail);
        email.setTo(notification.getRecipient());
        email.setSubject(notification.getSubject() == null ? "Notification" : notification.getSubject());
        email.setText(notification.getMessage());

        mailSender.send(email);
    }
}