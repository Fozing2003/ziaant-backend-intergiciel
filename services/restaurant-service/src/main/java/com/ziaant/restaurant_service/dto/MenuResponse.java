package com.ziaant.restaurant_service.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private List<MenuItemResponse> entrees;
    private List<MenuItemResponse> plats;
    private List<MenuItemResponse> desserts;
    private List<MenuItemResponse> boissons;
}
