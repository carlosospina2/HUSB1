package com.eventify.husb4.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración Swagger/OpenAPI.
 * Solo documenta rutas bajo /api/** (configurado en application.properties).
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eventifyOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Eventify API — HU-SB4")
                .version("4.0.0")
                .description("""
                        API REST de Eventify con relaciones JPA, Soft Delete y Slice.
                        
                        Reglas de negocio documentadas:
                        - Los eventos eliminados usan Soft Delete (active=false).
                          Nunca aparecen en listados gracias a @SQLRestriction.
                        - Los listados usan Records (EventSummaryDTO) proyectados en JPQL.
                        - La paginación usa Slice (sin COUNT) para máxima eficiencia.
                        - Los filtros por ciudad y categoría son case-insensitive y parciales.
                        
                        Rutas /admin/** son vistas Thymeleaf y no forman parte de esta API.
                        """)
                .contact(new Contact().name("Equipo Eventify").email("dev@eventify.com"))
        );
    }
}
