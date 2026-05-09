package com.ziaant.restaurant_service.repository;

import com.ziaant.restaurant_service.entity.TableRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<TableRestaurant, Long> {
    List<TableRestaurant> findByRestaurantId(Long restaurantId);
    List<TableRestaurant> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}