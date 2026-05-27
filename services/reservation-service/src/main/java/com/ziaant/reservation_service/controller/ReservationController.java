package com.ziaant.reservation_service.controller;

import com.ziaant.reservation_service.dto.ReservationRequest;
import com.ziaant.reservation_service.dto.ReservationResponse;
import com.ziaant.reservation_service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // CLIENT 

    @PostMapping
    @Tag(name = "Client")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Créer une réservation",
               description = "Le client connecté crée une réservation. Son identité est lue depuis le JWT.")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReservationRequest request) {
        return ResponseEntity.status(201)
                .body(reservationService.createReservation(authHeader, request));
    }

    @GetMapping("/mes-reservations")
    @Tag(name = "Client")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mes réservations",
               description = "Retourne les réservations du client connecté (lu depuis le JWT).")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(reservationService.getMyReservations(authHeader));
    }

    @DeleteMapping("/{id}")
    @Tag(name = "Client")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    //RESTAURATEUR 

    @GetMapping("/restaurant/{restaurantId}")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Réservations d'un restaurant")
    public ResponseEntity<List<ReservationResponse>> getRestaurantReservations(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(reservationService.getRestaurantReservations(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/en-attente")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Réservations en attente d'un restaurant")
    public ResponseEntity<List<ReservationResponse>> getPendingReservations(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(reservationService.getPendingReservations(restaurantId));
    }

    @PutMapping("/{id}/confirmer")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmer une réservation")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    @PutMapping("/{id}/refuser")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Refuser une réservation")
    public ResponseEntity<ReservationResponse> refuseReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.refuseReservation(id));
    }

    // ADMIN 

    @GetMapping
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toutes les réservations (Admin)")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer une réservation par ID")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/client/{clientId}")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Réservations d'un client par ID (Admin)")
    public ResponseEntity<List<ReservationResponse>> getClientReservations(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getClientReservations(clientId));
    }

    @GetMapping("/stats")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Statistiques des réservations")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(reservationService.getStats());
    }

    @DeleteMapping("/admin/{id}")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer définitivement une réservation (Admin)")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
