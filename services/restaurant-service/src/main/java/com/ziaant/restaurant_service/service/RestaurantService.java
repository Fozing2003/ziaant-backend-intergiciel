package com.ziaant.restaurant_service.service;

import com.ziaant.restaurant_service.dto.RestaurantRequest;
import com.ziaant.restaurant_service.dto.RestaurantResponse;
import com.ziaant.restaurant_service.entity.Restaurant;
import com.ziaant.restaurant_service.entity.enums.RestaurantStatus;
import com.ziaant.restaurant_service.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddress(),
            restaurant.getCity(),
            restaurant.getCuisine(),
            restaurant.getRating(),
            restaurant.getStatus(),
            restaurant.getOwnerId()
        );
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOwnerId(request.getOwnerId());
        restaurant.setStatus(RestaurantStatus.PENDING);
        Restaurant saved = restaurantRepository.save(restaurant);
        System.out.println("Restaurant créé : " + saved.getName() + " par l'utilisateur " + saved.getOwnerId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé avec id : " + id));
        return toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantByOwnerId(Long ownerId) {
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé pour ce propriétaire : " + ownerId));
        return toResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long currentUserId, boolean isAdmin) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé"));
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce restaurant");
        }
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setCuisine(request.getCuisine());
        Restaurant updated = restaurantRepository.save(restaurant);
        return toResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Long id, Long currentUserId, boolean isAdmin) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé"));
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce restaurant");
        }
        restaurantRepository.delete(restaurant);
        System.out.println("Restaurant supprimé : " + id);
    }

    @Transactional
    public void updateRestaurantStatus(Long id, RestaurantStatus newStatus, boolean isAdmin) {
        if (!isAdmin) throw new RuntimeException("Seul un admin peut changer le statut");
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé"));
        restaurant.setStatus(newStatus);
        restaurantRepository.save(restaurant);
        System.out.println("Statut du restaurant " + id + " mis à jour : " + newStatus);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String city, String cuisine) {
        return restaurantRepository.findByCityContainingIgnoreCaseAndCuisineContainingIgnoreCase(city, cuisine).stream()
                .filter(r -> r.getStatus() == RestaurantStatus.ACTIVE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}