package com.ziaant.notification_service.repository;

import com.ziaant.notification_service.model.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {
    List<NotificationDocument> findByRecipientOrderByCreatedAtDesc(String recipient);
}
