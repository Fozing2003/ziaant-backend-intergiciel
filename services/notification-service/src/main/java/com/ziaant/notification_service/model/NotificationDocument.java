package com.ziaant.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDocument {
    @Id
    private String id;

    private String recipient;
    private String subject;
    private String body;
    private NotificationStatus status;
    private String errorMessage;
    private Instant createdAt;
}
