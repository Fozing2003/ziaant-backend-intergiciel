package com.ziaant.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank
    @Email
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$",
             message = "Seules les adresses Gmail sont autorisées") // Restriction au domaine gmail.com
    private String to;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}