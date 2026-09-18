package com.eventify.husb1.repository;

import com.eventify.husb1.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Event.
 *
 * Hereda de JpaRepository<Event, Long> todos los métodos CRUD estándar
 * más soporte completo para Pageable y Sort.
 *
 * Derived Queries declaradas:
 *   findByNombreContainingIgnoreCase → búsqueda parcial por nombre.
 *   findByVenueId                   → todos los eventos de un venue.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Busca eventos cuyo nombre contenga el texto dado (case-insensitive).
     * Soporta paginación: ?page=0&size=5&sort=nombre,asc
     *
     * @param nombre   Fragmento de texto a buscar.
     * @param pageable Parámetros de paginación y ordenamiento.
     * @return Página de eventos que coinciden.
     */
    Page<Event> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Retorna todos los eventos asociados a un Venue específico.
     *
     * @param venueId ID del venue.
     * @return Lista de eventos del venue.
     */
    List<Event> findByVenueId(Long venueId);
}
