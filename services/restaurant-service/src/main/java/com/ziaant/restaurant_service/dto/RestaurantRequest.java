package com.ziaant.restaurant_service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Donnees pour creer ou modifier un restaurant")
public class RestaurantRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "Chez Mama Adjoua")
    private String name;

    @Schema(example = "La reference de la cuisine traditionnelle a Douala.")
    private String description;

    @Schema(example = "Camerounaise")
    private String cuisine;

    @NotBlank(message = "L adresse est obligatoire")
    @Schema(example = "Quartier Bali, Douala")
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    @Schema(example = "Douala")
    private String ville;

    @Schema(example = "+237 699 000 000")
    private String phone;

    @Schema(example = "contact@chezmamaadjoua.cm")
    private String email;

    @Schema(example = "https://images.unsplash.com/photo-xxx")
    private String imageUrl;

    @Schema(example = "07h-15h . 18h-22h")
    private String openHours;

    @Schema(example = "1 000 - 3 000 FCFA")
    private String priceRange;
    @Schema(example = "1000")
    private Integer prixMin;
    @Schema(example = "3000")
    private Integer prixMax;

    @Schema(example = "Traditionnel,Famille,Produits locaux")
    private String tags;
}

