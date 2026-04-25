package com.clinicabienestar.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API Clínica Bienestar",
        version = "1.0.0",
        description = "Documentación de la API para la gestión de la Clínica Bienestar."
    ),
    security = @SecurityRequirement(name = "bearerAuth") // Aplica la seguridad a todos los endpoints
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Auth description",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}