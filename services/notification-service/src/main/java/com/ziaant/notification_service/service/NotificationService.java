package com.ziaant.notification_service.service;

import com.ziaant.notification_service.model.NotificationDocument;
import com.ziaant.notification_service.model.NotificationStatus;
import com.ziaant.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public void sendEmail(String to, String subject, String body) {
        NotificationDocument notification = NotificationDocument.builder()
                .recipient(to)
                .subject(subject)
                .body(body)
                .createdAt(Instant.now())
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);
            log.info("Email envoye a {}", to);
        } catch (RuntimeException e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            throw e;
        }
    }
}
