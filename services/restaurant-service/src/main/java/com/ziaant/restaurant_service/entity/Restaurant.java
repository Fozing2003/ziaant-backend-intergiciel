package com.ziaant.restaurant_service.entity;

import com.ziaant.restaurant_service.entity.enums.RestaurantStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String city;
    private String cuisine;
    private Double rating;
    @Enumerated(EnumType.STRING)
    private RestaurantStatus status = RestaurantStatus.PENDING;
    @Column(name = "owner_id")
    private Long ownerId;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
     @JsonIgnore 
    private List<TableRestaurant> tables = new ArrayList<>();

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
    public List<TableRestaurant> getTables() { return tables; }
    public void setTables(List<TableRestaurant> tables) { this.tables = tables; }
}