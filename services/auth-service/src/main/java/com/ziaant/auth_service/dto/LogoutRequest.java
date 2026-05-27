package com.ziaant.auth_service.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
