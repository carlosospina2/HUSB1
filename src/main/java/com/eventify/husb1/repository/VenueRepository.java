package com.eventify.husb1.repository;

import com.eventify.husb1.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Venue.
 *
 * Al extender JpaRepository<Venue, Long> obtenemos gratuitamente:
 *   - save(venue)           → INSERT o UPDATE
 *   - findById(id)          → SELECT por PK → Optional<Venue>
 *   - findAll()             → SELECT todos
 *   - findAll(Pageable)     → SELECT paginado + ordenado
 *   - deleteById(id)        → DELETE por PK
 *   - existsById(id)        → SELECT COUNT > 0
 *   - count()               → SELECT COUNT(*)
 *
 * Derived Queries (Spring Data genera el SQL a partir del nombre del método):
 *   findByNombreContainingIgnoreCase → LIKE %nombre% (case-insensitive)
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * Busca venues cuyo nombre contenga el texto dado (sin importar mayúsculas).
     * Ejemplo: findByNombreContainingIgnoreCase("teatro") encuentra "Teatro Mayor".
     *
     * @param nombre  Fragmento de texto a buscar.
     * @param pageable Parámetros de paginación y ordenamiento.
     * @return Página de venues que coinciden.
     */
    Page<Venue> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
