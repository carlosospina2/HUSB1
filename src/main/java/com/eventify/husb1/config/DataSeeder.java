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
 * Seeder de datos iniciales para Eventify.
 *
 * Usa los JpaRepository reales — los datos se persisten en H2.
 *
 * Guarda condicionalmente: solo inserta si la tabla está vacía.
 * Esto evita duplicados si la app se reinicia con el archivo H2
 * ya existente (Escenario 1 — persistencia post-reinicio).
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(VenueRepository venueRepository,
                                      EventRepository eventRepository) {
        return args -> {

            // Si ya hay datos en la BD no volvemos a insertar
            if (venueRepository.count() > 0) {
                System.out.println("[Eventify] BD ya contiene datos — seeder omitido.");
                return;
            }

            // ------------------------------------------------------------------
            // Venues
            // ------------------------------------------------------------------
            Venue v1 = venueRepository.save(Venue.builder()
                    .nombre("Centro de Convenciones Bogotá")
                    .direccion("Calle 26 # 59-41, Bogotá")
                    .capacidad(5000)
                    .build());

            Venue v2 = venueRepository.save(Venue.builder()
                    .nombre("Teatro Mayor Julio Mario Santo Domingo")
                    .direccion("Calle 170 # 67-51, Bogotá")
                    .capacidad(1800)
                    .build());

            // ------------------------------------------------------------------
            // Eventos
            // ------------------------------------------------------------------
            eventRepository.save(Event.builder()
                    .nombre("Conferencia Internacional de Tecnología 2025")
                    .fecha(LocalDate.of(2025, 9, 15))
                    .descripcion("Congreso anual sobre innovación tecnológica y transformación digital.")
                    .venueId(v1.getId())
                    .build());

            eventRepository.save(Event.builder()
                    .nombre("Foro Nacional de Startups")
                    .fecha(LocalDate.of(2025, 10, 3))
                    .descripcion("Encuentro de emprendedores y fondos de inversión latinoamericanos.")
                    .venueId(v1.getId())
                    .build());

            eventRepository.save(Event.builder()
                    .nombre("Gala de Música Clásica — Temporada 2025")
                    .fecha(LocalDate.of(2025, 11, 20))
                    .descripcion("Orquesta Filarmónica de Bogotá: obras de Beethoven y Mahler.")
                    .venueId(v2.getId())
                    .build());

            System.out.println("========================================");
            System.out.println("  [Eventify] Seeder ejecutado con éxito");
            System.out.println("  Venues  : 2  |  Eventos: 3");
            System.out.println("========================================");
        };
    }
}
