package com.ziaant.restaurant_service.repository;

import com.ziaant.restaurant_service.entity.CategorieMenu;
import com.ziaant.restaurant_service.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndCategorie(Long restaurantId, CategorieMenu categorie);
}
