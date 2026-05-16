package com.ziaant.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numéro de téléphone invalide (9 à 15 chiffres)")
    private String phone;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
}
