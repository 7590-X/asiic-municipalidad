package com.assic.muni.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    public static final String SCHEME_NAME = "bearerAuth";
    public static final String SCHEME_TYPE = "bearer";
    public static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${openapi.service.title:Muni API}") String title,
            @Value("${openapi.service.version:1.0.0}") String version,
            @Value("${openapi.service.description:Documentación de servicios REST}") String description) {

        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo UMG")
                                .email("soporte@assic.com")
                                .url("https://assic.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // Aplica el esquema de seguridad de forma global a los endpoints
                //.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(SCHEME_TYPE)
                                .bearerFormat(BEARER_FORMAT)
                                .description("Ingresa tu token JWT en el formato: Bearer {token}")));
    }
}