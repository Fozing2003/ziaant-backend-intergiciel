package com.ziaant.restaurant_service.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cuisine;       // ex: Camerounaise, Grillades, Italienne
    private String address;
    private String ville;
    private String phone;
    private String email;
    private String imageUrl;
    private String openHours;     // ex: 07h-15h · 18h-22h
    private String priceRange;    // ex: 1 000 - 3 000 FCFA

    private Double rating;
    private Integer reviewCount;

    private Boolean featured;

    // Tags comme "Traditionnel", "Famille", etc. stockes en JSON string
    @Column(columnDefinition = "TEXT")
    private String tags;          // ex: Traditionnel,Famille,Produits locaux

    // ID du restaurateur proprietaire (vient de auth-service)
    @Column(nullable = false)
    private Long restaurateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRestaurant statut = StatutRestaurant.EN_ATTENTE;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

