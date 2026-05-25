package com.ziaant.auth_service.service;

import com.ziaant.auth_service.config.RabbitMQConfig;
import com.ziaant.auth_service.dto.*;
import com.ziaant.auth_service.model.*;
import com.ziaant.auth_service.repository.UserRepository;
import com.ziaant.auth_service.security.JwtUtil;
import com.ziaant.auth_service.security.session.RefreshSession;
import com.ziaant.auth_service.security.session.TokenSessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;
    private final TokenSessionStore tokenSessionStore;

    @Value("${admin.secret}")
    private String adminSecret;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est deja utilise.");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .statut(StatutCompte.APPROUVE)
                .build();
        userRepository.save(user);
        sendNotification(user.getEmail(),
                "Bienvenue sur ReserveTable CM !",
                "Bonjour " + user.getName() + ",\n\nVotre compte a ete cree avec succes.\nBonne decouverte des restaurants !\n\nL'equipe ReserveTable CM");
        return buildResponse(user, null);
    }

    public AuthResponse registerRestaurateur(RestaurateurRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est deja utilise.");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.RESTAURATEUR)
                .statut(StatutCompte.EN_ATTENTE)
                .build();
        userRepository.save(user);
        sendNotification(user.getEmail(),
                "Demande de compte restaurateur recue",
                "Bonjour " + user.getName() + ",\n\nVotre demande a bien ete recue.\nVotre compte est en attente de validation par notre equipe.\nVous serez notifie(e) des validation.\n\nL'equipe ReserveTable CM");
        return buildResponse(user, null);
    }

    public AuthResponse registerAdmin(AdminRegisterRequest request) {
        if (!adminSecret.equals(request.getAdminSecret())) {
            throw new RuntimeException("Secret admin incorrect.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est deja utilise.");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .statut(StatutCompte.APPROUVE)
                .build();
        userRepository.save(user);
        return buildResponse(user, null);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect.");
        }
        if (user.getStatut() == StatutCompte.SUSPENDU) {
            throw new RuntimeException("Votre compte a ete suspendu. Contactez l'administrateur.");
        }
        if (user.getStatut() == StatutCompte.EN_ATTENTE) {
            throw new RuntimeException("Votre compte est en attente de validation par l'administrateur.");
        }
        return buildTokenResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshSession session = tokenSessionStore.consumeRefreshToken(hashRefreshToken(request.getRefreshToken()))
                .orElseThrow(() -> new RuntimeException("Refresh token invalide."));
        User user = userRepository.findById(session.userId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        if (user.getStatut() == StatutCompte.SUSPENDU) {
            throw new RuntimeException("Votre compte a ete suspendu. Contactez l'administrateur.");
        }
        if (user.getStatut() == StatutCompte.EN_ATTENTE) {
            throw new RuntimeException("Votre compte est en attente de validation par l'administrateur.");
        }
        return buildTokenResponse(user);
    }

    public void logout(String accessToken, LogoutRequest request) {
        if (jwtUtil.isTokenValid(accessToken)) {
            Duration ttl = Duration.between(Instant.now(), jwtUtil.extractExpiration(accessToken));
            tokenSessionStore.blacklistAccessToken(jwtUtil.extractTokenId(accessToken), ttl);
        }
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            tokenSessionStore.revokeRefreshToken(hashRefreshToken(request.getRefreshToken()));
        }
    }

    public boolean validateToken(String token) {
        return isAccessTokenAccepted(token);
    }

    public UserProfileResponse getProfile(String token) {
        requireAcceptedAccessToken(token);
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return toProfile(user);
    }

    public UserProfileResponse updateProfile(String token, UserUpdateRequest request) {
        requireAcceptedAccessToken(token);
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return toProfile(user);
    }

    public List<UserProfileResponse> getAllUsers(String token) {
        verifierAdmin(token);
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .map(this::toProfile)
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getEnAttente(String token) {
        verifierAdmin(token);
        return userRepository.findByStatut(StatutCompte.EN_ATTENTE).stream()
                .map(this::toProfile)
                .collect(Collectors.toList());
    }

    public void changerStatut(String token, Long userId, String nouveauStatut) {
        verifierAdmin(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setStatut(StatutCompte.valueOf(nouveauStatut));
        userRepository.save(user);
        if ("APPROUVE".equals(nouveauStatut)) {
            sendNotification(user.getEmail(),
                    "Compte approuve - ReserveTable CM",
                    "Bonjour " + user.getName() + ",\n\nVotre compte a ete approuve.\nVous pouvez maintenant vous connecter et acceder a votre espace.\n\nL'equipe ReserveTable CM");
        } else if ("SUSPENDU".equals(nouveauStatut)) {
            sendNotification(user.getEmail(),
                    "Compte suspendu - ReserveTable CM",
                    "Bonjour " + user.getName() + ",\n\nVotre compte a ete suspendu.\nPour plus d'informations, contactez notre support.\n\nL'equipe ReserveTable CM");
        }
    }

    private void sendNotification(String to, String subject, String body) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .to(to).subject(subject).body(body).build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event);
        } catch (Exception e) {
            // Ne pas bloquer si RabbitMQ est indisponible.
        }
    }

    private void verifierAdmin(String token) {
        requireAcceptedAccessToken(token);
        if (!"ADMIN".equals(jwtUtil.extractRole(token))) {
            throw new RuntimeException("Acces reserve a l'administrateur.");
        }
    }

    private void requireAcceptedAccessToken(String token) {
        if (!isAccessTokenAccepted(token)) {
            throw new RuntimeException("Token invalide, expire ou revoque.");
        }
    }

    private boolean isAccessTokenAccepted(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            return false;
        }
        String tokenId = jwtUtil.extractTokenId(token);
        return tokenId != null && !tokenSessionStore.isAccessTokenBlacklisted(tokenId);
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AuthResponse buildResponse(User user, String token) {
        return AuthResponse.builder()
                .id(user.getId())
                .accessToken(token)
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .build();
    }

    private AuthResponse buildTokenResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = generateRefreshToken();

        tokenSessionStore.storeRefreshToken(
                hashRefreshToken(refreshToken),
                new RefreshSession(user.getId(), user.getEmail()),
                Duration.ofMillis(refreshExpiration)
        );

        return AuthResponse.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessExpiration() / 1000)
                .refreshTokenExpiresIn(refreshExpiration / 1000)
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .build();
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
