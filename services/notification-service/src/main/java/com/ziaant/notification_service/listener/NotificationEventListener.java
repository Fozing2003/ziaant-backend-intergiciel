package com.ziaant.notification_service.listener;

import com.ziaant.notification_service.dto.NotificationRequest;
import com.ziaant.notification_service.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.ziaant.notification_service.config.RabbitMQConfig.NOTIFICATION_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = NOTIFICATION_QUEUE)
    public void handleNotification( @Valid NotificationRequest request) {
        log.info("Reçu un message RabbitMQ: {}", request);
        notificationService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
    }
}