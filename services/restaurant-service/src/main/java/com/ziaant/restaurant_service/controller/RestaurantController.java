package com.ziaant.restaurant_service.controller;

import com.ziaant.restaurant_service.dto.*;
import com.ziaant.restaurant_service.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ENDPOINTS PUBLICS (pas de token requis)


    @GetMapping
    @Tag(name = "Public")
    @Operation(summary = "Liste des restaurants",
               description = "Retourne tous les restaurants ACTIF. Filtres optionnels : ville, cuisine, search")
    public ResponseEntity<List<RestaurantResponse>> getPublicList(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer prixMin,
            @RequestParam(required = false) Integer prixMax) {
        return ResponseEntity.ok(restaurantService.getPublicList(ville, cuisine, search, prixMin, prixMax));
    }

    @GetMapping("/{id}")
    @Tag(name = "Public")
    @Operation(summary = "Detail d un restaurant",
               description = "Retourne les infos completes d un restaurant ACTIF")
    public ResponseEntity<RestaurantResponse> getPublicDetail(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getPublicDetail(id));
    }

    @GetMapping("/{id}/menu")
    @Tag(name = "Public")
    @Operation(summary = "Menu d un restaurant",
               description = "Retourne le menu classe par categorie : entrees, plats, desserts, boissons")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenu(id));
    }

    // ENDPOINTS RESTAURATEUR (token requis, role RESTAURATEUR ou ADMIN)


    @PostMapping
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creer un restaurant",
               description = "Reserve au RESTAURATEUR. Le restaurant est cree avec statut EN_ATTENTE.")
    public ResponseEntity<RestaurantResponse> creer(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.creer(authHeader, request));
    }

    @PutMapping("/{id}")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier un restaurant",
               description = "Reserve au RESTAURATEUR proprietaire du restaurant")
    public ResponseEntity<RestaurantResponse> modifier(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.modifier(authHeader, id, request));
    }

    @GetMapping("/mes-restaurants")
    @Tag(name = "Restaurateur")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mes restaurants",
               description = "Retourne les restaurants du restaurateur connecte")
    public ResponseEntity<List<RestaurantResponse>> getMesRestaurants(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(restaurantService.getMesRestaurants(authHeader));
    }

    // Menu 

    @PostMapping("/{id}/menu")
    @Tag(name = "Restaurateur - Menu")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ajouter un plat au menu")
    public ResponseEntity<MenuItemResponse> ajouterPlat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.ajouterPlat(authHeader, id, request));
    }

    @PutMapping("/{restaurantId}/menu/{itemId}")
    @Tag(name = "Restaurateur - Menu")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier un plat du menu")
    public ResponseEntity<MenuItemResponse> modifierPlat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.modifierPlat(authHeader, restaurantId, itemId, request));
    }

    @DeleteMapping("/{restaurantId}/menu/{itemId}")
    @Tag(name = "Restaurateur - Menu")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer un plat du menu")
    public ResponseEntity<MessageResponse> supprimerPlat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        restaurantService.supprimerPlat(authHeader, restaurantId, itemId);
        return ResponseEntity.ok(new MessageResponse("Plat supprime avec succes."));
    }

  
    // ENDPOINTS ADMIN (token requis, role ADMIN)
 

    @GetMapping("/admin/tous")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tous les restaurants",
               description = "Reserve a l'ADMIN. Retourne tous les restaurants (tous statuts)")
    public ResponseEntity<List<RestaurantResponse>> getTous(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(restaurantService.getTous(authHeader));
    }

    @GetMapping("/admin/en-attente")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Restaurants en attente de validation",
               description = "Reserve a l'ADMIN")
    public ResponseEntity<List<RestaurantResponse>> getEnAttente(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(restaurantService.getEnAttente(authHeader));
    }

    @PutMapping("/admin/valider/{id}")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Valider et publier un restaurant",
               description = "Reserve a l'ADMIN. Passe le statut a ACTIF.")
    public ResponseEntity<MessageResponse> valider(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.changerStatut(authHeader, id, "ACTIF"));
    }

    @PutMapping("/admin/suspendre/{id}")
    @Tag(name = "Admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Suspendre un restaurant",
               description = "Reserve a l'ADMIN. Passe le statut a SUSPENDU.")
    public ResponseEntity<MessageResponse> suspendre(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.changerStatut(authHeader, id, "SUSPENDU"));
    }
}
