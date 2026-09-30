package com.eventify.husb3.repository;

import com.eventify.husb3.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para Event.
 * Hereda CRUD completo + paginación de JpaRepository.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /** Búsqueda parcial por nombre, insensible a mayúsculas, paginada. */
    Page<Event> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /** Todos los eventos de un venue específico. */
    List<Event> findByVenueId(Long venueId);
}
