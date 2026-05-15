
package com.ziaant.reservation_service.dto;

import com.ziaant.reservation_service.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long clientId;
    private Long restaurantId;
    private String clientName;
    private String clientPhone;
    private LocalDateTime reservationDate;
    private String timeSlot;
    private int numberOfGuests;
    private ReservationStatus status;
    private String notes;
    private LocalDateTime createdAt;
}