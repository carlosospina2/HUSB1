package com.eventify.husb4.repository;

import com.eventify.husb4.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para Venue.
 * Hereda CRUD completo de JpaRepository.
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * Búsqueda insensible a mayúsculas por ciudad (parcial).
     * Escenario 6: "bog" encuentra "Bogotá".
     */
    List<Venue> findByCityContainingIgnoreCase(String city);

    /** Todos los venues de una ciudad exacta (case-insensitive). */
    List<Venue> findByCityIgnoreCase(String city);
}
