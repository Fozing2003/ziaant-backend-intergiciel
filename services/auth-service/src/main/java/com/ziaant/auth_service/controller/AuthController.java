package com.ziaant.auth_service.controller;

import com.ziaant.auth_service.dto.*;
import com.ziaant.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth & Users", description = "Authentification et gestion des profils")
public class AuthController {

    private final AuthService authService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token manquant ou mal formaté");
        }
        return authHeader.substring(7).trim();
    }

    // ====================== AUTHENTIFICATION ======================
    @PostMapping("/api/auth/register")
    @Operation(summary = "Inscription Client")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/api/auth/register/restaurateur")
    @Operation(summary = "Inscription Restaurateur")
    public ResponseEntity<AuthResponse> registerRestaurateur(@Valid @RequestBody RestaurateurRegisterRequest request) {
        return ResponseEntity.status(201).body(authService.registerRestaurateur(request));
    }

    @PostMapping("/api/auth/register/admin")
    @Operation(summary = "Inscription Admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return ResponseEntity.status(201).body(authService.registerAdmin(request));
    }

    @PostMapping("/api/auth/login")
    @Operation(summary = "Connexion")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/api/auth/validate")
    @Operation(summary = "Valider token JWT (pour Gateway)")
    public ResponseEntity<Map<String, Boolean>> validate(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }

    // ====================== PROFIL UTILISATEUR ======================
    @GetMapping("/api/users/me")
    @Operation(summary = "Récupérer mon profil")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getProfile(extractToken(authHeader)));
    }

    @PutMapping("/api/users/me")
    @Operation(summary = "Mettre à jour mon profil")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(extractToken(authHeader), request));
    }

    // ====================== ADMIN ======================
    @GetMapping("/api/users/admin/all")
    @Operation(summary = "Tous les utilisateurs (Admin)")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getAllUsers(extractToken(authHeader)));
    }

    @GetMapping("/api/users/admin/en-attente")
    @Operation(summary = "Comptes en attente de validation")
    public ResponseEntity<List<UserProfileResponse>> getEnAttente(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getEnAttente(extractToken(authHeader)));
    }

    @PutMapping("/api/users/admin/{userId}/statut")
    @Operation(summary = "Changer statut d'un utilisateur")
    public ResponseEntity<Map<String, String>> changeStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam String statut) {
        authService.changerStatut(extractToken(authHeader), userId, statut);
        return ResponseEntity.ok(Map.of("message", "Statut mis à jour avec succès"));
    }
}