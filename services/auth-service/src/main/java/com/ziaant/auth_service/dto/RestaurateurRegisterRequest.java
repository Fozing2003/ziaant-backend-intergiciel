package com.ziaant.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RestaurateurRegisterRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String phone;

    @NotBlank @Size(min = 6, message = "Mot de passe : 6 caractères minimum")
    private String password;

    @NotBlank(message = "Le nom du restaurant est obligatoire")
    private String nomRestaurant;
}
