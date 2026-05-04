package com.ziaant.restaurant_service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private String price;
    private String categorie;
    private boolean disponible;
}
