package com.eventify.husb4.repository;

import com.eventify.husb4.dto.EventSummaryDTO;
import com.eventify.husb4.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repositorio JPA para Event.
 *
 * Técnicas aplicadas en HU-SB4:
 *
 * 1. @SQLRestriction en la entidad → todos los métodos aquí solo
 *    retornan eventos activos (active = true) automáticamente.
 *
 * 2. Slice<T>:
 *    Retorna un fragmento de datos SIN ejecutar COUNT(*).
 *    Más eficiente que Page<T> cuando no se necesita el total.
 *    Provee hasNext() para la navegación Anterior/Siguiente.
 *
 * 3. @EntityGraph:
 *    Resuelve el problema N+1 cargando venue y categories en
 *    una sola consulta SQL (JOIN FETCH implícito).
 *    Se aplica en findById y en la consulta del panel admin.
 *
 * 4. JPQL con NEW (proyección al Record):
 *    Construye EventSummaryDTO directamente en la BD.
 *    Solo transfiere id, name, date, venue.name, venue.city.
 *    Prohíbe el uso de entidades pesadas en listados masivos.
 *
 * 5. Filtros avanzados (Escenario 6):
 *    Búsquedas por ciudad y categoría son case-insensitive
 *    y soportan coincidencias parciales (LIKE %:param%).
 *
 * 6. Soft Delete vía @Modifying:
 *    UPDATE directo que solo cambia active = false.
 *    Evita cargar la entidad en memoria para desactivarla.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // -------------------------------------------------------------------------
    // Consulta principal del panel — @EntityGraph resuelve N+1
    // -------------------------------------------------------------------------

    /**
     * Carga de la lista admin con venue y categories en una sola query.
     *
     * @EntityGraph indica a Hibernate que haga JOIN FETCH sobre
     * "venue" y "categories", evitando múltiples SELECT adicionales.
     *
     * Retorna Slice para evitar COUNT(*): más eficiente para paginación
     * Anterior/Siguiente sin necesidad del total de registros.
     *
     * Ordenamiento: por fecha descendente (más reciente primero).
     * Escenario 2: una sola consulta SQL optimizada con JOIN.
     */
    @EntityGraph(attributePaths = {"venue", "categories"})
    @Query("SELECT e FROM Event e ORDER BY e.date DESC")
    Slice<Event> findAllWithVenueAndCategories(Pageable pageable);

    // -------------------------------------------------------------------------
    // findById enriquecido con @EntityGraph
    // -------------------------------------------------------------------------

    /**
     * Busca un evento por ID cargando venue y categories en la misma query.
     * Elimina N+1 al acceder a e.getVenue() o e.getCategories().
     */
    @EntityGraph(attributePaths = {"venue", "categories"})
    Optional<Event> findWithDetailsById(Long id);

    // -------------------------------------------------------------------------
    // Proyección liviana con Record — JPQL + NEW
    // -------------------------------------------------------------------------

    /**
     * Listado masivo usando Record EventSummaryDTO.
     *
     * NEW com.eventify.husb4.dto.EventSummaryDTO(...) construye el Record
     * directamente en la consulta JPQL: solo viajan 5 campos desde la BD.
     * Prohíbe entidades pesadas en listados (regla técnica HU-SB4).
     *
     * Retorna Slice para paginación eficiente sin COUNT(*).
     * Ordenado por fecha descendente.
     */
    @Query("""
            SELECT new com.eventify.husb4.dto.EventSummaryDTO(
                e.id, e.name, e.date, e.venue.name, e.venue.city
            )
            FROM Event e
            ORDER BY e.date DESC
            """)
    Slice<EventSummaryDTO> findAllSummary(Pageable pageable);

    // -------------------------------------------------------------------------
    // Filtros avanzados — ciudad (Escenario 6)
    // -------------------------------------------------------------------------

    /**
     * Búsqueda por ciudad insensible a mayúsculas y parcial (LIKE %:city%).
     * Escenario 6: "bog" encuentra eventos en "Bogotá".
     * Retorna Slice ordenado por fecha descendente.
     */
    @Query("""
            SELECT new com.eventify.husb4.dto.EventSummaryDTO(
                e.id, e.name, e.date, e.venue.name, e.venue.city
            )
            FROM Event e
            WHERE LOWER(e.venue.city) LIKE LOWER(CONCAT('%', :city, '%'))
            ORDER BY e.date DESC
            """)
    Slice<EventSummaryDTO> findByCityContaining(
            @Param("city") String city,
            Pageable pageable);

    // -------------------------------------------------------------------------
    // Filtros avanzados — categoría (Escenario 6)
    // -------------------------------------------------------------------------

    /**
     * Búsqueda por nombre de categoría insensible a mayúsculas y parcial.
     * Escenario 6: "rock" encuentra eventos en la categoría "Conciertos de Rock".
     */
    @Query("""
            SELECT new com.eventify.husb4.dto.EventSummaryDTO(
                e.id, e.name, e.date, e.venue.name, e.venue.city
            )
            FROM Event e
            JOIN e.categories c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))
            ORDER BY e.date DESC
            """)
    Slice<EventSummaryDTO> findByCategoryName(
            @Param("categoryName") String categoryName,
            Pageable pageable);

    // -------------------------------------------------------------------------
    // Filtros avanzados — ciudad + categoría combinados
    // -------------------------------------------------------------------------

    @Query("""
            SELECT new com.eventify.husb4.dto.EventSummaryDTO(
                e.id, e.name, e.date, e.venue.name, e.venue.city
            )
            FROM Event e
            JOIN e.categories c
            WHERE LOWER(e.venue.city) LIKE LOWER(CONCAT('%', :city, '%'))
              AND LOWER(c.name)       LIKE LOWER(CONCAT('%', :categoryName, '%'))
            ORDER BY e.date DESC
            """)
    Slice<EventSummaryDTO> findByCityAndCategory(
            @Param("city") String city,
            @Param("categoryName") String categoryName,
            Pageable pageable);

    // -------------------------------------------------------------------------
    // Filtros avanzados — rango de fechas
    // -------------------------------------------------------------------------

    @Query("""
            SELECT new com.eventify.husb4.dto.EventSummaryDTO(
                e.id, e.name, e.date, e.venue.name, e.venue.city
            )
            FROM Event e
            WHERE e.date BETWEEN :from AND :to
            ORDER BY e.date DESC
            """)
    Slice<EventSummaryDTO> findByDateBetween(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to,
            Pageable pageable);

    // -------------------------------------------------------------------------
    // Soft Delete vía UPDATE directo — no carga la entidad en memoria
    // -------------------------------------------------------------------------

    /**
     * Desactiva un evento (borrado lógico) con un UPDATE directo.
     * @Modifying → indica a Spring Data que es una operación de escritura.
     * @Transactional → garantiza que el UPDATE se ejecute en una transacción.
     *
     * Después de este UPDATE, @SQLRestriction("active = true") filtra
     * automáticamente el evento en todas las consultas JPA posteriores.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.active = false WHERE e.id = :id")
    int softDeleteById(@Param("id") Long id);
}
