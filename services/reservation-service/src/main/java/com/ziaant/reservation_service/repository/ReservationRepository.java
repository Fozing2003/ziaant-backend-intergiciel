package com.ziaant.reservation_service.repository;

import com.ziaant.reservation_service.model.Reservation;
import com.ziaant.reservation_service.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByRestaurantId(Long restaurantId);
    List<Reservation> findByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);
}