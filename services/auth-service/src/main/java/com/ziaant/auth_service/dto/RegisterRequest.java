package com.ziaant.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "L'email est obligatoire")
    @Pattern(
        regexp = "^[A-Za-z0-9+_.-]+@(gmail\\.com|yahoo\\.com|yahoo\\.fr)$",
        message = "Email invalide (domaines acceptés : gmail.com, yahoo.com, yahoo.fr)"
    )
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numéro de téléphone invalide (9 à 15 chiffres)")
    private String phone;

    @NotBlank
    @Size(min = 6, message = "Mot de passe : 6 caractères minimum")
    private String password;
}
