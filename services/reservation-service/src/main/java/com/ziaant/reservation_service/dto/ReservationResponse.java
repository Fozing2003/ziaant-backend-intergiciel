package com.ziaant.reservation_service.dto;

import com.ziaant.reservation_service.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long clientId;
    private Long restaurantId;
    private Long tableId;
    private String clientName;
    private String clientPhone;
    private LocalDate dateReservation;
    private String heureReservation;
    private int nombrePersonnes;
    private ReservationStatus status;
    private String commentaire;
    private LocalDateTime createdAt;
}
