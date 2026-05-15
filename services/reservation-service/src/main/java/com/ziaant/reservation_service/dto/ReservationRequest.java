package com.ziaant.reservation_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    @NotNull
    private Long restaurantId;

    @NotNull
    private LocalDateTime reservationDate;

    @NotNull
    private String timeSlot;

    @NotNull
    @Min(1)
    private int numberOfGuests;

    private String notes;
}