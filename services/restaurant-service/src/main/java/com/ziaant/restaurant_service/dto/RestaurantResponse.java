package com.ziaant.restaurant_service.dto;

import com.ziaant.restaurant_service.entity.enums.RestaurantStatus;

public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String cuisine;
    private Double rating;
    private RestaurantStatus status;
    private Long ownerId;

    // Constructeurs
    public RestaurantResponse() {}
    public RestaurantResponse(Long id, String name, String address, String city, String cuisine, Double rating, RestaurantStatus status, Long ownerId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.cuisine = cuisine;
        this.rating = rating;
        this.status = status;
        this.ownerId = ownerId;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public RestaurantStatus getStatus() { return status; }
    public void setStatus(RestaurantStatus status) { this.status = status; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}