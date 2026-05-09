package com.ziaant.restaurant_service.controller;

import com.ziaant.restaurant_service.dto.MessageResponse;
import com.ziaant.restaurant_service.dto.TableRequest;
import com.ziaant.restaurant_service.dto.TableResponse;
import com.ziaant.restaurant_service.service.TableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    private Long getCurrentUserId() { return 1L; }
    private boolean isAdmin() { return false; }

    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<TableResponse> addTable(@PathVariable Long restaurantId, @Valid @RequestBody TableRequest request) {
        TableResponse response = tableService.addTable(restaurantId, request, getCurrentUserId(), isAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<TableResponse>> getTablesByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(tableService.getTablesByRestaurant(restaurantId));
    }

    @GetMapping("/{tableId}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long tableId) {
        return ResponseEntity.ok(tableService.getTableById(tableId));
    }

    @PutMapping("/{tableId}")
    public ResponseEntity<TableResponse> updateTable(@PathVariable Long tableId, @Valid @RequestBody TableRequest request) {
        TableResponse response = tableService.updateTable(tableId, request, getCurrentUserId(), isAdmin());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<MessageResponse> deleteTable(@PathVariable Long tableId) {
        tableService.deleteTable(tableId, getCurrentUserId(), isAdmin());
        return ResponseEntity.ok(new MessageResponse("Table supprimée avec succès"));
    }

    @PatchMapping("/{tableId}/disponibilite")
    public ResponseEntity<MessageResponse> changeAvailability(@PathVariable Long tableId, @RequestParam Boolean available) {
        tableService.changeTableAvailability(tableId, available, getCurrentUserId(), isAdmin());
        return ResponseEntity.ok(new MessageResponse("Disponibilité modifiée"));
    }

    @GetMapping("/restaurant/{restaurantId}/disponibles")
    public ResponseEntity<List<TableResponse>> getAvailableTables(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(tableService.getAvailableTablesByRestaurant(restaurantId));
    }
}