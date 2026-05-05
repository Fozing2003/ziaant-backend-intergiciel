package com.ziaant.notification_service.controller;

import com.ziaant.notification_service.dto.NotificationRequest;
import com.ziaant.notification_service.dto.NotificationResponse;
import com.ziaant.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints pour l'envoi de notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    @Operation(summary = "Envoyer un email")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody NotificationRequest request) {
        // Le service s'occupe de l'envoi effectif
        notificationService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
        // Retourne une réponse 200 avec un message de succès
        return ResponseEntity.ok(new NotificationResponse("SUCCESS", "Email envoyé à " + request.getTo()));
    }
}