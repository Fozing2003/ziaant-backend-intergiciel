package com.ziaant.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String phone;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;   // optionnel (si l'utilisateur veut changer son mot de passe)
}