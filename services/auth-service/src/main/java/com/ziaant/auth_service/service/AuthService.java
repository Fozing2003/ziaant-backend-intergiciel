package com.ziaant.auth_service.service;

import com.ziaant.auth_service.dto.*;
import com.ziaant.auth_service.model.*;
import com.ziaant.auth_service.repository.UserRepository;
import com.ziaant.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${admin.secret}")
    private String adminSecret;

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
        return buildResponse(user, jwtUtil.generateToken(user.getEmail(), user.getRole().name()));
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
        return buildResponse(user, jwtUtil.generateToken(user.getEmail(), user.getRole().name()));
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
        return buildResponse(user, jwtUtil.generateToken(user.getEmail(), user.getRole().name()));
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
        return buildResponse(user, jwtUtil.generateToken(user.getEmail(), user.getRole().name()));
    }

    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    public UserProfileResponse getProfile(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
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
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        user.setStatut(StatutCompte.valueOf(nouveauStatut));
        userRepository.save(user);
    }

    private void verifierAdmin(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token invalide ou expire.");
        }
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Acces refuse. Reserve a l'administrateur.");
        }
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
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .build();
    }
}
