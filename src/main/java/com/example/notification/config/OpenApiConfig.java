
package com.example.notification.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Notification Microservice API")
                        .description(
                                "REST API documentation for the Notification Microservice. "
                                        + "Supports email, SMS, JWT authentication, notification history, "
                                        + "ownership, and retry operations."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Notification Service Team")
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                )
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
