package com.ziaant.restaurant_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TableRequest {
    @NotNull
    @Min(1)
    private Integer capacity;
    private Boolean available;

    // Getters et setters
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}