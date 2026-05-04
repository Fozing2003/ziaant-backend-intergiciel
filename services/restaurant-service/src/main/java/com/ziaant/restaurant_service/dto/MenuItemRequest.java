package com.ziaant.restaurant_service.dto;

import com.ziaant.restaurant_service.entity.CategorieMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Donnees pour ajouter un plat au menu")
public class MenuItemRequest {

    @NotBlank(message = "Le nom du plat est obligatoire")
    @Schema(example = "Ndole aux crevettes")
    private String name;

    @Schema(example = "Feuilles de ndole, crevettes sechees, arachides pilees")
    private String description;

    @NotBlank(message = "Le prix est obligatoire")
    @Schema(example = "2 500 FCFA")
    private String price;

    @NotNull(message = "La categorie est obligatoire")
    @Schema(example = "PLAT")
    private CategorieMenu categorie;

    @Schema(example = "true")
    private boolean disponible = true;
}
