package com.ziaant.restaurant_service.controller;

import com.ziaant.restaurant_service.dto.MessageResponse;
import com.ziaant.restaurant_service.dto.RestaurantRequest;
import com.ziaant.restaurant_service.dto.RestaurantResponse;
import com.ziaant.restaurant_service.entity.enums.RestaurantStatus;
import com.ziaant.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    private Long getCurrentUserId() { return 1L; }
    private boolean isAdmin() { return true; }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        request.setOwnerId(getCurrentUserId());
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllActiveRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllActiveRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/restaurateur/{userId}")
    public ResponseEntity<RestaurantResponse> getRestaurantByOwnerId(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId) && !isAdmin()) {
            throw new RuntimeException("Accès non autorisé");
        }
        return ResponseEntity.ok(restaurantService.getRestaurantByOwnerId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.updateRestaurant(id, request, getCurrentUserId(), isAdmin());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id, getCurrentUserId(), isAdmin());
        return ResponseEntity.ok(new MessageResponse("Restaurant supprimé avec succès"));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long id, @RequestParam RestaurantStatus status) {
        restaurantService.updateRestaurantStatus(id, status, isAdmin());
        return ResponseEntity.ok(new MessageResponse("Statut mis à jour"));
    }

    @GetMapping("/en-attente")
    public ResponseEntity<List<RestaurantResponse>> getPendingRestaurants() {
        if (!isAdmin()) throw new RuntimeException("Accès réservé à l'admin");
        return ResponseEntity.ok(restaurantService.getPendingRestaurants());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(city, cuisine));
    }
}