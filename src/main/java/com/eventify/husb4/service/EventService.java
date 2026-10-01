package com.eventify.husb4.service;

import com.eventify.husb4.dto.EventSummaryDTO;
import com.eventify.husb4.exception.ResourceNotFoundException;
import com.eventify.husb4.model.Category;
import com.eventify.husb4.model.Event;
import com.eventify.husb4.model.Venue;
import com.eventify.husb4.repository.CategoryRepository;
import com.eventify.husb4.repository.EventRepository;
import com.eventify.husb4.repository.VenueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Capa de negocio para Event.
 *
 * Puntos clave HU-SB4:
 *
 * 1. SOFT DELETE:
 *    softDelete(id) → llama al repositorio softDeleteById (UPDATE directo).
 *    Después, @SQLRestriction filtra el evento automáticamente.
 *    NUNCA se hace DELETE físico desde este service.
 *
 * 2. SLICE:
 *    Todos los listados retornan Slice<T> para paginación eficiente
 *    sin COUNT(*). El controller usa slice.hasNext() para la navegación.
 *
 * 3. FILTROS COMBINADOS:
 *    search(city, categoryName, pageable) decide qué consulta usar
 *    según los parámetros provistos, manteniendo las reglas de negocio
 *    separadas del repositorio.
 *
 * 4. RELACIONES:
 *    save() resuelve los IDs de Venue y Category antes de persistir,
 *    garantizando integridad referencial desde la capa de negocio.
 */
@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository   eventRepository;
    private final VenueRepository   venueRepository;
    private final CategoryRepository categoryRepository;

    public EventService(EventRepository eventRepository,
                        VenueRepository venueRepository,
                        CategoryRepository categoryRepository) {
        this.eventRepository    = eventRepository;
        this.venueRepository    = venueRepository;
        this.categoryRepository = categoryRepository;
    }

    /** Cuenta eventos activos (respeta @SQLRestriction). */
    public long countActive() {
        return eventRepository.count();
    }

    // -------------------------------------------------------------------------
    // CREATE / UPDATE
    // -------------------------------------------------------------------------

    /**
     * Persiste un nuevo evento.
     * Resuelve el Venue y las Categories por ID antes de guardar.
     *
     * @param name         Nombre del evento.
     * @param date         Fecha de realización.
     * @param description  Descripción opcional.
     * @param venueId      ID del venue (obligatorio).
     * @param categoryIds  IDs de las categorías seleccionadas.
     */
    @Transactional
    public Event save(String name, LocalDate date, String description,
                      Long venueId, List<Long> categoryIds) {
        validate(name, date, venueId);

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));

        Set<Category> categories = resolveCategories(categoryIds);

        Event event = Event.builder()
                .name(name)
                .date(date)
                .description(description)
                .venue(venue)
                .categories(categories)
                .build();

        return eventRepository.save(event);
    }

    /**
     * Actualiza un evento existente.
     * Verifica existencia (→ 404), luego aplica los cambios.
     */
    @Transactional
    public Event update(Long id, String name, LocalDate date, String description,
                        Long venueId, List<Long> categoryIds) {
        Event existing = findWithDetailsById(id);
        validate(name, date, venueId);

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));

        existing.setName(name);
        existing.setDate(date);
        existing.setDescription(description);
        existing.setVenue(venue);
        existing.setCategories(resolveCategories(categoryIds));

        return eventRepository.save(existing);
    }

    // -------------------------------------------------------------------------
    // SOFT DELETE
    // -------------------------------------------------------------------------

    /**
     * Borrado lógico: marca el evento como inactivo (active = false).
     * Después de esta llamada, @SQLRestriction lo filtra automáticamente
     * de TODAS las consultas JPA. NUNCA se hace DELETE físico.
     *
     * @throws ResourceNotFoundException si el ID no existe.
     */
    @Transactional
    public void softDelete(Long id) {
        if (!eventRepository.existsById(id))
            throw new ResourceNotFoundException("Event", id);
        eventRepository.softDeleteById(id);
    }

    // -------------------------------------------------------------------------
    // READ — Slice (paginación eficiente sin COUNT)
    // -------------------------------------------------------------------------

    /**
     * Lista todos los eventos (proyección liviana EventSummaryDTO).
     * Retorna Slice para paginación Anterior/Siguiente.
     * Ordenado por fecha descendente (Escenario 2).
     */
    public Slice<EventSummaryDTO> findAllSummary(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAllSummary(pageable);
    }

    /**
     * Panel admin: carga completa con venue y categories (@EntityGraph).
     * Una sola query SQL (Escenario 2).
     */
    public Slice<Event> findAllWithDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAllWithVenueAndCategories(pageable);
    }

    // -------------------------------------------------------------------------
    // MOTOR DE BÚSQUEDA — filtros combinados (Escenario 3 y 6)
    // -------------------------------------------------------------------------

    /**
     * Motor de búsqueda filtrada con persistencia de contexto.
     * Decide qué consulta ejecutar según city y categoryName.
     *
     * Escenario 6: búsqueda parcial e insensible a mayúsculas.
     * Escenario 3: los parámetros viajan en la URL para persistir en Slice nav.
     *
     * @param city         Ciudad (parcial, case-insensitive). Puede ser null.
     * @param categoryName Nombre de categoría (parcial). Puede ser null.
     * @param page         Número de página (0-based).
     * @param size         Tamaño de página.
     * @return Slice<EventSummaryDTO> con los resultados filtrados.
     */
    public Slice<EventSummaryDTO> search(String city, String categoryName,
                                         int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        boolean hasCity     = city         != null && !city.isBlank();
        boolean hasCategory = categoryName != null && !categoryName.isBlank();

        if (hasCity && hasCategory)
            return eventRepository.findByCityAndCategory(city, categoryName, pageable);
        if (hasCity)
            return eventRepository.findByCityContaining(city, pageable);
        if (hasCategory)
            return eventRepository.findByCategoryName(categoryName, pageable);

        return eventRepository.findAllSummary(pageable);
    }

    /** Búsqueda por rango de fechas. */
    public Slice<EventSummaryDTO> findByDateRange(LocalDate from, LocalDate to,
                                                   int page, int size) {
        return eventRepository.findByDateBetween(from, to, PageRequest.of(page, size));
    }

    // -------------------------------------------------------------------------
    // READ — por ID
    // -------------------------------------------------------------------------

    /**
     * Obtiene un evento con venue y categories cargados (@EntityGraph).
     * @throws ResourceNotFoundException si no existe.
     */
    public Event findWithDetailsById(Long id) {
        return eventRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validate(String name, LocalDate date, Long venueId) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        if (date == null)
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        if (venueId == null)
            throw new IllegalArgumentException("El venue del evento es obligatorio.");
    }

    private Set<Category> resolveCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(categoryRepository.findAllById(ids));
    }
}
