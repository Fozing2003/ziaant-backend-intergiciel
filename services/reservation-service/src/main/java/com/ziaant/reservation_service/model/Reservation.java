package com.ziaant.reservation_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;
    private Long restaurantId;
    private Long tableId;

    private String clientName;
    private String clientPhone;
    private String clientEmail;

    private LocalDate dateReservation;
    private String heureReservation;
    private int nombrePersonnes;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(length = 500)
    private String commentaire;
    private String restaurateurEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
