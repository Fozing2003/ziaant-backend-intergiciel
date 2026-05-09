package com.ziaant.restaurant_service.repository;

import com.ziaant.restaurant_service.entity.Restaurant;
import com.ziaant.restaurant_service.entity.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByCityContainingIgnoreCaseAndCuisineContainingIgnoreCase(String city, String cuisine);
}