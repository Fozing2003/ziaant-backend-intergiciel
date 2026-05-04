package com.ziaant.restaurant_service.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReserveTable CM - Restaurant Service API")
                        .description("Gestion des restaurants et menus")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ziaant Team")
                                .email("contact@reservetable.cm")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu depuis auth-service /api/auth/login")));
    }
}
