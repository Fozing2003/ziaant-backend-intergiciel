package com.ziaant.user_service.controller;

import com.ziaant.user_service.dtos.UserResponse;
import com.ziaant.user_service.dtos.UserUpdateRequest;
import com.ziaant.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des profils utilisateurs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Récupérer mon profil (utilisateur connecté)")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        // Plus tard on récupérera l'utilisateur à partir du JWT
        return ResponseEntity.ok(userService.getUserById(1L)); // temporaire
    }

    @Operation(summary = "Récupérer un utilisateur par ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Mettre à jour mon profil")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        // Plus tard on récupérera l'ID depuis le token
        return ResponseEntity.ok(userService.updateUser(1L, request)); // temporaire
    }

    @Operation(summary = "Mettre à jour un utilisateur (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Liste tous les utilisateurs (Admin)")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}