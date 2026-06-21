package com.master.inventario.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventario API",
                version = "1.0.0",
                description = "REST API para la gestión de inventario de productos y entradas de stock. " +
                              "Desarrollada con Clean Architecture como proyecto de Trabajo de Fin de Máster.",
                contact = @Contact(
                        name = "Felix Abalia",
                        email = "felix@gmail.com"
                )
        )
)
public class OpenApiConfig {
}
