package com.ziaant.auth_service.controller;

import com.ziaant.auth_service.dto.*;
import com.ziaant.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, Connexion et validation JWT")
public class AuthController {

    private final AuthService authService;

    private String extraireToken(String authHeader) {
        if (authHeader == null) throw new RuntimeException("Token manquant.");
        String token = authHeader.replace("Bearer ", "").trim();
        return token;
    }

    @PostMapping("/register")
    @Operation(summary = "Créer un compte CLIENT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/register/restaurateur")
    @Operation(summary = "Créer un compte RESTAURATEUR", description = "Compte en attente de validation admin")
    public ResponseEntity<AuthResponse> registerRestaurateur(@Valid @RequestBody RestaurateurRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerRestaurateur(request));
    }

    @PostMapping("/register/admin")
    @Operation(summary = "Créer un compte ADMIN", description = "Nécessite le secret admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter", description = "Retourne un token JWT valable 24h")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    @Operation(summary = "Valider un token JWT", description = "Utilisé par le Gateway")
    public ResponseEntity<Map<String, Boolean>> validate(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne le profil de l'utilisateur connecté")
    public ResponseEntity<UserProfileResponse> me(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getProfile(extraireToken(authHeader)));
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Lister tous les utilisateurs", description = "Réservé à l'admin")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getAllUsers(extraireToken(authHeader)));
    }

    @PutMapping("/admin/valider/{userId}")
    @Operation(summary = "Valider un compte restaurateur", description = "Réservé à l'admin")
    public ResponseEntity<Map<String, String>> validerCompte(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        authService.changerStatut(extraireToken(authHeader), userId, "APPROUVE");
        return ResponseEntity.ok(Map.of("message", "Compte approuvé avec succès."));
    }

    @PutMapping("/admin/suspendre/{userId}")
    @Operation(summary = "Suspendre un compte", description = "Réservé à l'admin")
    public ResponseEntity<Map<String, String>> suspendreCompte(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        authService.changerStatut(extraireToken(authHeader), userId, "SUSPENDU");
        return ResponseEntity.ok(Map.of("message", "Compte suspendu avec succès."));
    }

    @GetMapping("/admin/en-attente")
    @Operation(summary = "Lister les comptes en attente", description = "Réservé à l'admin")
    public ResponseEntity<List<UserProfileResponse>> getEnAttente(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getEnAttente(extraireToken(authHeader)));
    }
}
