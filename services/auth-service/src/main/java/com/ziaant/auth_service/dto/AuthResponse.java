package com.ziaant.auth_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String token;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String statut;
}
