package com.ziaant.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReserveTable CM - User Service API")
                        .description("API de gestion des utilisateurs (Client, Owner, Admin)")
                        .version("1.0")
                        .contact(new Contact()
                                .name("ReserveTable CM")
                                .email("contact@reservetable.cm")
                                .url("https://reservetable.cm"))
                );
    }
}