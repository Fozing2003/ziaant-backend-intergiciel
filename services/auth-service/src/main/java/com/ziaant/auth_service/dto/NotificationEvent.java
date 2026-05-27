package com.ziaant.auth_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {
    private String to;
    private String subject;
    private String body;
}
