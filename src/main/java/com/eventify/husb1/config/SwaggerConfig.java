package com.eventify.husb1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI 3 / Swagger UI.
 *
 * Personaliza los metadatos que aparecen en la cabecera de
 * la interfaz Swagger accesible en /swagger-ui.html.
 *
 * La dependencia springdoc-openapi-starter-webmvc-ui detecta
 * automáticamente todos los @RestController y genera los
 * endpoints documentados; esta clase solo enriquece los metadatos.
 *
 * Acceso en desarrollo:
 *   Swagger UI  → http://localhost:8080/swagger-ui.html
 *   JSON spec   → http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define los metadatos globales de la API en la interfaz Swagger.
     *
     * @return Objeto OpenAPI con título, versión, descripción y contacto.
     */
    @Bean
    public OpenAPI eventifyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eventify API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de Eventos y Venues de la plataforma Eventify.
                                
                                Funcionalidades disponibles:
                                - Registrar y consultar Eventos (POST/GET /api/events)
                                - Registrar y consultar Venues (POST/GET /api/venues)
                                
                                Los datos se almacenan en memoria (fase inicial sin persistencia real).
                                """)
                        .contact(new Contact()
                                .name("Equipo Eventify")
                                .email("dev@eventify.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                );
    }
}
