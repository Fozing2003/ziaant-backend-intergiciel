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


        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("Token manquant dans le header Authorization");
        }
        String token = authHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        return token;
    }


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

    @Operation(summary = "Connexion - retourne un access token et un refresh token")

    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/api/auth/refresh")
    @Operation(summary = "Renouveler l'access token avec un refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/api/auth/logout")
    @Operation(summary = "Deconnexion et revocation des tokens")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        authService.logout(extractToken(authHeader), request);
        return ResponseEntity.ok(Map.of("message", "Deconnexion effectuee"));
    }

    @GetMapping("/api/auth/validate")

    @Operation(summary = "Valider un token JWT (utilisé par le Gateway)")

    public ResponseEntity<Map<String, Boolean>> validate(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }


    @GetMapping("/api/users/me")
    @Operation(summary = "Récupérer mon profil")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(authService.getProfile(extractToken(authHeader)));
    }

    @PutMapping("/api/users/me")
    @Operation(summary = "Mettre à jour mon profil")
    public ResponseEntity<UserProfileResponse> updateProfile(

            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(extractToken(authHeader), request));
    }

    @GetMapping("/api/users/admin/all")
    @Operation(summary = "Lister tous les utilisateurs (Admin uniquement)")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getAllUsers(extractToken(authHeader)));
    }

    @GetMapping("/api/users/admin/en-attente")
    @Operation(summary = "Lister les comptes en attente de validation")
    public ResponseEntity<List<UserProfileResponse>> getEnAttente(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getEnAttente(extractToken(authHeader)));
    }

    @PutMapping("/api/users/admin/{userId}/statut")
    @Operation(summary = "Changer le statut manuellement (APPROUVE / SUSPENDU / EN_ATTENTE)")
    public ResponseEntity<Map<String, String>> changeStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam String statut) {
        authService.changerStatut(extractToken(authHeader), userId, statut);
        return ResponseEntity.ok(Map.of("message", "Statut mis à jour avec succès"));
    }

    @PutMapping("/api/users/admin/{userId}/valider")
    @Operation(summary = "Valider le compte d'un restaurateur")
    public ResponseEntity<Map<String, String>> validerCompte(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        authService.changerStatut(extractToken(authHeader), userId, "APPROUVE");
        return ResponseEntity.ok(Map.of("message", "Compte approuvé avec succès."));
    }

    @PutMapping("/api/users/admin/{userId}/suspendre")
    @Operation(summary = "Suspendre le compte d'un utilisateur")
    public ResponseEntity<Map<String, String>> suspendreCompte(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        authService.changerStatut(extractToken(authHeader), userId, "SUSPENDU");
        return ResponseEntity.ok(Map.of("message", "Compte suspendu avec succès."));
    }
}

