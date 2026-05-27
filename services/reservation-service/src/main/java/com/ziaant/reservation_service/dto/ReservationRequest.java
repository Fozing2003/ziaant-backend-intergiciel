package com.ziaant.reservation_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRequest {

    @NotNull
    private Long restaurantId;

    @NotNull
    private Long tableId;

    @NotNull
    private Long userId;

    @NotNull
    private LocalDate dateReservation;

    @NotBlank
    private String heureReservation;

    @NotNull
    @Min(1)
    private int nombrePersonnes;

    private String commentaire;
    private String restaurateurEmail;
}
