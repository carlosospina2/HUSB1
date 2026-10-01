package com.eventify.husb4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de Eventify HU-SB4.
 *
 * URLs al arrancar:
 *   Panel Admin  → http://localhost:8080/admin
 *   Eventos      → http://localhost:8080/admin/events
 *   Venues       → http://localhost:8080/admin/venues
 *   Categorías   → http://localhost:8080/admin/categories
 *   Swagger UI   → http://localhost:8080/swagger-ui.html
 *   H2 Console   → http://localhost:8080/h2-console
 */
@SpringBootApplication
public class EventifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventifyApplication.class, args);
    }
}
