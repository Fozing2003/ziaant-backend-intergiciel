package com.ziaant.restaurant_service.repository;

import com.ziaant.restaurant_service.entity.Restaurant;

import com.ziaant.restaurant_service.entity.StatutRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Liste publique : seulement les ACTIF
    List<Restaurant> findByStatut(StatutRestaurant statut);

    // Recherche par ville
    List<Restaurant> findByStatutAndVilleIgnoreCase(StatutRestaurant statut, String ville);

    // Recherche par cuisine
    List<Restaurant> findByStatutAndCuisineIgnoreCase(StatutRestaurant statut, String cuisine);

    // Recherche par nom
    List<Restaurant> findByStatutAndNameContainingIgnoreCase(StatutRestaurant statut, String name);

    // Restaurants d'un restaurateur
    List<Restaurant> findByRestaurateurId(Long restaurateurId);

    // Recherche combinee
    @Query("SELECT r FROM Restaurant r WHERE r.statut = :statut " +
           "AND (:ville IS NULL OR LOWER(r.ville) = LOWER(:ville)) " +
           "AND (:cuisine IS NULL OR LOWER(r.cuisine) = LOWER(:cuisine)) " +
           "AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:prixMin IS NULL OR r.prixMax >= :prixMin) " +
           "AND (:prixMax IS NULL OR r.prixMin <= :prixMax)")
    List<Restaurant> search(@Param("statut") StatutRestaurant statut,
                            @Param("ville") String ville,
                            @Param("cuisine") String cuisine,
                            @Param("search") String search,
                            @Param("prixMin") Integer prixMin,
                            @Param("prixMax") Integer prixMax);
}

