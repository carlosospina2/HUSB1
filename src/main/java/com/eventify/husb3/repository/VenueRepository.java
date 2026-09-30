package com.eventify.husb3.repository;

import com.eventify.husb3.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para Venue.
 * Hereda CRUD completo + paginación de JpaRepository.
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /** Búsqueda parcial por nombre, insensible a mayúsculas, paginada. */
    Page<Venue> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
