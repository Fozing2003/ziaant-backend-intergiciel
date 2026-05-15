package com.ziaant.reservation_service.controller;

import com.ziaant.reservation_service.dto.ReservationRequest;
import com.ziaant.reservation_service.dto.ReservationResponse;
import com.ziaant.reservation_service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Gestion des réservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle réservation")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les réservations d'un client")
    public ResponseEntity<List<ReservationResponse>> getClientReservations(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getClientReservations(clientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réservation par ID")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Récupérer les réservations d'un restaurant")
    public ResponseEntity<List<ReservationResponse>> getRestaurantReservations(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reservationService.getRestaurantReservations(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/en-attente")
    @Operation(summary = "Récupérer les réservations en attente")
    public ResponseEntity<List<ReservationResponse>> getPendingReservations(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reservationService.getPendingReservations(restaurantId));
    }

    @PutMapping("/{id}/confirmer")
    @Operation(summary = "Confirmer une réservation")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    @PutMapping("/{id}/refuser")
    @Operation(summary = "Refuser une réservation")
    public ResponseEntity<ReservationResponse> refuseReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.refuseReservation(id));
    }

    @GetMapping
    @Operation(summary = "Toutes les réservations (Admin)")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des réservations")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(reservationService.getStats());
    }
}