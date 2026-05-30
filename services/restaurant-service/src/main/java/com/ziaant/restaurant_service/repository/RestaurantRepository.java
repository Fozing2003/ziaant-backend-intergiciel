package com.ziaant.restaurant_service.repository;

import com.ziaant.restaurant_service.entity.Restaurant;
import com.ziaant.restaurant_service.entity.StatutRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByStatut(StatutRestaurant statut);

    List<Restaurant> findByRestaurateurId(Long restaurateurId);

    List<Restaurant> findByStatutAndVilleIgnoreCase(StatutRestaurant statut, String ville);

    List<Restaurant> findByStatutAndCuisineIgnoreCase(StatutRestaurant statut, String cuisine);

    List<Restaurant> findByStatutAndNameContainingIgnoreCase(StatutRestaurant statut, String name);
}
