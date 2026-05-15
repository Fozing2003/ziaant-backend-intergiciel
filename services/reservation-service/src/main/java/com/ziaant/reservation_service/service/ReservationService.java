package com.ziaant.reservation_service.service;

import com.ziaant.reservation_service.model.Reservation;
import com.ziaant.reservation_service.model.ReservationStatus;
import com.ziaant.reservation_service.dto.ReservationRequest;
import com.ziaant.reservation_service.dto.ReservationResponse;
import com.ziaant.reservation_service.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Reservation reservation = Reservation.builder()
                .clientId(1L) // À remplacer par l'ID du user connecté (via JWT plus tard)
                .restaurantId(request.getRestaurantId())
                .clientName("Client Test") // À remplacer par infos du user
                .clientPhone("237699123456")
                .reservationDate(request.getReservationDate())
                .timeSlot(request.getTimeSlot())
                .numberOfGuests(request.getNumberOfGuests())
                .notes(request.getNotes())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = repository.save(reservation);
        return mapToResponse(saved);
    }

    public List<ReservationResponse> getClientReservations(Long clientId) {
        return repository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        return mapToResponse(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.CANCELLED);
        repository.save(reservation);
    }

    public List<ReservationResponse> getRestaurantReservations(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReservationResponse> getPendingReservations(Long restaurantId) {
        return repository.findByRestaurantIdAndStatus(restaurantId, ReservationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse confirmReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return mapToResponse(repository.save(reservation));
    }

    @Transactional
    public ReservationResponse refuseReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.REFUSED);
        return mapToResponse(repository.save(reservation));
    }

    public List<ReservationResponse> getAllReservations() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Map<String, Object> getStats() {
        long total = repository.count();
        long pending = repository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .count();
        return Map.of(
                "totalReservations", total,
                "pendingReservations", pending
        );
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .clientId(reservation.getClientId())
                .restaurantId(reservation.getRestaurantId())
                .clientName(reservation.getClientName())
                .clientPhone(reservation.getClientPhone())
                .reservationDate(reservation.getReservationDate())
                .timeSlot(reservation.getTimeSlot())
                .numberOfGuests(reservation.getNumberOfGuests())
                .status(reservation.getStatus())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}