package com.ziaant.user_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 2, max = 100)
    private String name;

    private String phone;

    @Email
    private String email;
}