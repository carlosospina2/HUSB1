package com.eventify.husb3.config;

import com.eventify.husb3.model.Event;
import com.eventify.husb3.model.Venue;
import com.eventify.husb3.repository.EventRepository;
import com.eventify.husb3.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Seeder de datos iniciales.
 * Solo inserta si la BD está vacía → evita duplicados en reinicios.
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(VenueRepository venueRepository,
                                      EventRepository eventRepository) {
        return args -> {

            if (venueRepository.count() > 0) {
                System.out.println("[Eventify] BD con datos — seeder omitido.");
                return;
            }

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

            eventRepository.save(Event.builder()
                    .nombre("Conferencia Internacional de Tecnología 2025")
                    .fecha(LocalDate.of(2025, 9, 15))
                    .descripcion("Congreso anual sobre innovación tecnológica.")
                    .venueId(v1.getId())
                    .build());

            eventRepository.save(Event.builder()
                    .nombre("Foro Nacional de Startups")
                    .fecha(LocalDate.of(2025, 10, 3))
                    .descripcion("Encuentro de emprendedores latinoamericanos.")
                    .venueId(v1.getId())
                    .build());

            eventRepository.save(Event.builder()
                    .nombre("Gala de Música Clásica — Temporada 2025")
                    .fecha(LocalDate.of(2025, 11, 20))
                    .descripcion("Orquesta Filarmónica de Bogotá: Beethoven y Mahler.")
                    .venueId(v2.getId())
                    .build());

            System.out.println("========================================");
            System.out.println("  [Eventify] Seeder OK — 2 venues, 3 eventos");
            System.out.println("========================================");
        };
    }
}
