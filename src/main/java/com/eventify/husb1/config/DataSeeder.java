package com.eventify.husb1.config;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.model.Venue;
import com.eventify.husb1.repository.EventRepository;
import com.eventify.husb1.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Clase de configuración que carga datos iniciales (Seeders) al arrancar
 * la aplicación mediante el mecanismo @Bean + CommandLineRunner.
 *
 * @Configuration le indica a Spring que esta clase contiene definiciones
 * de Beans que deben registrarse en el contexto de la aplicación.
 *
 * Patrón utilizado:
 *   @Bean que retorna un CommandLineRunner — Spring lo ejecuta
 *   automáticamente una vez que el contexto está completamente levantado.
 *
 * Inyección por Constructor en los @Bean:
 *   Los repositorios se reciben como parámetros del método @Bean,
 *   lo que equivale a inyección por constructor y permite desactivar
 *   el seeder fácilmente para el Escenario 3 (lista vacía).
 *
 * Para desactivar el seeder (Escenario 3 — catálogo vacío):
 *   Comentar el cuerpo del método seedData() o agregar la anotación
 *   @Profile("!seeder") para excluirlo en ciertos entornos.
 */
@Configuration
public class DataSeeder {

    /**
     * Bean que carga datos de prueba al iniciar la aplicación.
     *
     * Registra 2 Venues y 3 Events con datos coherentes entre sí.
     * Los IDs de los eventos apuntan a los IDs generados por los venues.
     *
     * @param venueRepository Repositorio de venues (inyectado por Spring).
     * @param eventRepository Repositorio de eventos (inyectado por Spring).
     * @return CommandLineRunner ejecutado automáticamente al arranque.
     */
    @Bean
    public CommandLineRunner seedData(VenueRepository venueRepository,
                                      EventRepository eventRepository) {
        return args -> {

            // ------------------------------------------------------------------
            // Venues iniciales
            // ------------------------------------------------------------------
            Venue venue1 = venueRepository.save(
                    Venue.builder()
                            .nombre("Centro de Convenciones Bogotá")
                            .direccion("Calle 26 # 59 - 41, Bogotá")
                            .capacidad(5000)
                            .build()
            );

            Venue venue2 = venueRepository.save(
                    Venue.builder()
                            .nombre("Teatro Mayor Julio Mario Santo Domingo")
                            .direccion("Calle 170 # 67 - 51, Bogotá")
                            .capacidad(1800)
                            .build()
            );

            // ------------------------------------------------------------------
            // Eventos iniciales asociados a los venues anteriores
            // ------------------------------------------------------------------
            eventRepository.save(
                    Event.builder()
                            .nombre("Conferencia Internacional de Tecnología 2025")
                            .fecha(LocalDate.of(2025, 9, 15))
                            .descripcion("Congreso anual sobre innovación tecnológica y transformación digital.")
                            .venueId(venue1.getId())
                            .build()
            );

            eventRepository.save(
                    Event.builder()
                            .nombre("Foro Nacional de Startups")
                            .fecha(LocalDate.of(2025, 10, 3))
                            .descripcion("Encuentro de emprendedores y fondos de inversión del ecosistema latinoamericano.")
                            .venueId(venue1.getId())
                            .build()
            );

            eventRepository.save(
                    Event.builder()
                            .nombre("Gala de Música Clásica — Temporada 2025")
                            .fecha(LocalDate.of(2025, 11, 20))
                            .descripcion("Presentación de la Orquesta Filarmónica de Bogotá con obras de Beethoven y Mahler.")
                            .venueId(venue2.getId())
                            .build()
            );

            System.out.println("========================================");
            System.out.println("  [Eventify] Seeder ejecutado con éxito");
            System.out.println("  Venues cargados : 2");
            System.out.println("  Eventos cargados: 3");
            System.out.println("========================================");
        };
    }
}
