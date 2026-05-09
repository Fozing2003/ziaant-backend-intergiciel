package com.ziaant.restaurant_service.service;

import com.ziaant.restaurant_service.dto.TableRequest;
import com.ziaant.restaurant_service.dto.TableResponse;
import com.ziaant.restaurant_service.entity.Restaurant;
import com.ziaant.restaurant_service.entity.TableRestaurant;
import com.ziaant.restaurant_service.repository.RestaurantRepository;
import com.ziaant.restaurant_service.repository.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;

    public TableService(TableRepository tableRepository, RestaurantRepository restaurantRepository) {
        this.tableRepository = tableRepository;
        this.restaurantRepository = restaurantRepository;
    }

    private TableResponse toResponse(TableRestaurant table) {
        return new TableResponse(
            table.getId(),
            table.getCapacity(),
            table.getAvailable(),
            table.getRestaurant().getId()
        );
    }

    @Transactional
    public TableResponse addTable(Long restaurantId, TableRequest request, Long currentUserId, boolean isAdmin) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant non trouvé"));
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter une table à ce restaurant");
        }
        TableRestaurant table = new TableRestaurant();
        table.setCapacity(request.getCapacity());
        Boolean available = request.getAvailable();
        table.setAvailable(available != null ? available : true);
        table.setRestaurant(restaurant);
        TableRestaurant saved = tableRepository.save(table);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getTablesByRestaurant(Long restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableResponse getTableById(Long tableId) {
        TableRestaurant table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table non trouvée"));
        return toResponse(table);
    }

    @Transactional
    public TableResponse updateTable(Long tableId, TableRequest request, Long currentUserId, boolean isAdmin) {
        TableRestaurant table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table non trouvée"));
        Restaurant restaurant = table.getRestaurant();
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette table");
        }
        table.setCapacity(request.getCapacity());
        if (request.getAvailable() != null) {
            table.setAvailable(request.getAvailable());
        }
        TableRestaurant updated = tableRepository.save(table);
        return toResponse(updated);
    }

    @Transactional
    public void deleteTable(Long tableId, Long currentUserId, boolean isAdmin) {
        TableRestaurant table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table non trouvée"));
        Restaurant restaurant = table.getRestaurant();
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette table");
        }
        tableRepository.delete(table);
        System.out.println("Table supprimée : " + tableId);
    }

    @Transactional
    public void changeTableAvailability(Long tableId, Boolean available, Long currentUserId, boolean isAdmin) {
        TableRestaurant table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table non trouvée"));
        Restaurant restaurant = table.getRestaurant();
        if (!restaurant.getOwnerId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier la disponibilité");
        }
        table.setAvailable(available);
        tableRepository.save(table);
        System.out.println("Disponibilité de la table " + tableId + " changée à " + available);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTablesByRestaurant(Long restaurantId) {
        return tableRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}