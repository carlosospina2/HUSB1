package com.eventify.husb3.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI.
 *
 * La restricción de rutas (solo /api/**) se configura en
 * application.properties con:
 *   springdoc.paths-to-match=/api/**
 *
 * Esto garantiza que las rutas de Thymeleaf (/admin/**)
 * NO aparezcan en la documentación Swagger.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eventifyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eventify API")
                        .version("1.0.0")
                        .description("""
                                API REST de Eventify — solo endpoints bajo /api/.
                                
                                Las rutas /admin/** son vistas Thymeleaf y no forman parte de la API.
                                
                                Endpoints disponibles:
                                  POST/GET/PUT/DELETE /api/events
                                  POST/GET/PUT/DELETE /api/venues
                                """)
                        .contact(new Contact()
                                .name("Equipo Eventify")
                                .email("dev@eventify.com"))
                );
    }
}
