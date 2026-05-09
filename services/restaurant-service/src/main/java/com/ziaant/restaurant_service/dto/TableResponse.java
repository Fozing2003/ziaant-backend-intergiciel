package com.ziaant.restaurant_service.dto;

public class TableResponse {
    private Long id;
    private Integer capacity;
    private Boolean available;
    private Long restaurantId;

    public TableResponse() {}
    public TableResponse(Long id, Integer capacity, Boolean available, Long restaurantId) {
        this.id = id;
        this.capacity = capacity;
        this.available = available;
        this.restaurantId = restaurantId;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
}