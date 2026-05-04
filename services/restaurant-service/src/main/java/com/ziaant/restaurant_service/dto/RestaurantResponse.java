package com.ziaant.restaurant_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reponse complete d un restaurant")
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String ville;
    private String phone;
    private String email;
    private String imageUrl;
    private String openHours;
    private String priceRange;
    private Double rating;
    private Integer reviewCount;
    private Boolean featured;
    private String tags;
    private String statut;
    private Long restaurateurId;
    private LocalDateTime createdAt;
    private List<MenuItemResponse> menuItems;
}
