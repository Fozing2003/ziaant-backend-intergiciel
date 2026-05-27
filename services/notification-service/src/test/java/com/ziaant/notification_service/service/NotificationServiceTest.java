package com.ziaant.notification_service.service;

import com.ziaant.notification_service.model.NotificationDocument;
import com.ziaant.notification_service.model.NotificationStatus;
import com.ziaant.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationService notificationService = new NotificationService(mailSender, notificationRepository);

    @Test
    void sendEmailPersistsSentNotification() {
        notificationService.sendEmail("user@gmail.com", "Hello", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(notificationRepository).save(argThat(notification ->
                notification.getRecipient().equals("user@gmail.com")
                        && notification.getSubject().equals("Hello")
                        && notification.getStatus() == NotificationStatus.SENT
                        && notification.getCreatedAt() != null
        ));
    }

    @Test
    void sendEmailPersistsFailedNotificationWhenMailFails() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> notificationService.sendEmail("user@gmail.com", "Hello", "Body"))
                .isInstanceOf(MailSendException.class);

        verify(notificationRepository).save(argThat(notification ->
                isFailedNotification(notification)
                        && notification.getErrorMessage().contains("smtp down")
        ));
    }

    private boolean isFailedNotification(NotificationDocument notification) {
        return notification.getRecipient().equals("user@gmail.com")
                && notification.getStatus() == NotificationStatus.FAILED
                && notification.getCreatedAt() != null;
    }
}
