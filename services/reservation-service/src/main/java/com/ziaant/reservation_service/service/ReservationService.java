package com.ziaant.reservation_service.service;

import com.ziaant.reservation_service.config.RabbitMQConfig;
import com.ziaant.reservation_service.dto.NotificationEvent;
import com.ziaant.reservation_service.dto.ReservationRequest;
import com.ziaant.reservation_service.dto.ReservationResponse;
import com.ziaant.reservation_service.model.Reservation;
import com.ziaant.reservation_service.model.ReservationStatus;
import com.ziaant.reservation_service.repository.ReservationRepository;
import com.ziaant.reservation_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository repository;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token manquant ou mal formaté");
        }
        return authHeader.substring(7).trim();
    }

    private void sendNotification(String to, String subject, String body) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            log.info("Notification envoyée à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi notification RabbitMQ : {}", e.getMessage());
        }
    }

    @Transactional
    public ReservationResponse createReservation(String authHeader, ReservationRequest request) {
        String token = extractToken(authHeader);

        Long clientId      = jwtUtil.extractUserId(token);
        String clientName  = jwtUtil.extractUserName(token);
        String clientPhone = jwtUtil.extractUserPhone(token);
        String clientEmail = jwtUtil.extractEmail(token);

        Reservation reservation = Reservation.builder()
                .clientId(clientId)
                .restaurantId(request.getRestaurantId())
                .clientName(clientName)
                .clientPhone(clientPhone)
                .clientEmail(clientEmail)
                .reservationDate(request.getReservationDate())
                .timeSlot(request.getTimeSlot())
                .numberOfGuests(request.getNumberOfGuests())
                .notes(request.getNotes())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = repository.save(reservation);

        sendNotification(
                clientEmail,
                "Réservation reçue - ReserveTable CM",
                String.format(
                    "Bonjour %s,\n\nVotre demande de réservation pour le %s (%s) a bien été reçue.\n" +
                    "Nombre de personnes : %d\n\nVous serez notifié(e) dès que le restaurant confirme.\n\nMerci d'utiliser ReserveTable CM !",
                    clientName,
                    saved.getReservationDate().toLocalDate(),
                    saved.getTimeSlot(),
                    saved.getNumberOfGuests()
                )
        );

        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = repository.save(reservation);

        // clientEmail est maintenant stocké en base — plus de problème
        sendNotification(
                saved.getClientEmail(),
                "Réservation confirmée - ReserveTable CM",
                String.format(
                    "Bonjour %s,\n\nVotre réservation du %s à %s pour %d personne(s) a été CONFIRMÉE.\n\nÀ bientôt !",
                    saved.getClientName(),
                    saved.getReservationDate().toLocalDate(),
                    saved.getTimeSlot(),
                    saved.getNumberOfGuests()
                )
        );

        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse refuseReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.REFUSED);
        Reservation saved = repository.save(reservation);

        sendNotification(
                saved.getClientEmail(),
                "Réservation refusée - ReserveTable CM",
                String.format(
                    "Bonjour %s,\n\nNous sommes désolés, votre réservation du %s à %s a été refusée par le restaurant.\n\nVous pouvez faire une nouvelle demande sur ReserveTable CM.",
                    saved.getClientName(),
                    saved.getReservationDate().toLocalDate(),
                    saved.getTimeSlot()
                )
        );

        return mapToResponse(saved);
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatus(ReservationStatus.CANCELLED);
        repository.save(reservation);
    }

    public List<ReservationResponse> getMyReservations(String authHeader) {
        String token = extractToken(authHeader);
        Long clientId = jwtUtil.extractUserId(token);
        return repository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .toList();
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

    public List<ReservationResponse> getAllReservations() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Map<String, Object> getStats() {
        long total     = repository.count();
        long pending   = repository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING).count();
        long confirmed = repository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
        return Map.of(
                "totalReservations",     total,
                "pendingReservations",   pending,
                "confirmedReservations", confirmed
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

    @Transactional
    public void deleteReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Réservation introuvable");
        }
        repository.deleteById(id);
    }
    
}
