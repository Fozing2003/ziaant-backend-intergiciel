package com.ziaant.reservation_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reservation Service API")
                        .description("API de gestion des réservations - ReserveTable CM")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Ziaant")
                                .email("contact@reservetable.cm")
                        )
                );
    }
}