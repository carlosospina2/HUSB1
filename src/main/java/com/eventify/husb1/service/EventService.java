package com.eventify.husb1.service;

import com.eventify.husb1.exception.ResourceNotFoundException;
import com.eventify.husb1.model.Event;
import com.eventify.husb1.repository.EventRepository;
import com.eventify.husb1.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Capa de negocio para Event.
 *
 * Responsabilidades:
 *   1. Validar datos de negocio antes de persistir.
 *   2. Verificar que el Venue referenciado exista.
 *   3. Lanzar ResourceNotFoundException (→ 404) cuando el evento no existe.
 *   4. Exponer listados paginados y ordenados mediante Pageable.
 *   5. CRUD completo: crear, leer, actualizar, eliminar.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    /**
     * Inyección por constructor de ambos repositorios.
     * VenueRepository se usa para verificar que el venue referenciado exista.
     */
    public EventService(EventRepository eventRepository,
                        VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository  = venueRepository;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo evento.
     * Valida datos y verifica que el venue referenciado exista en la BD.
     *
     * @param event Datos del evento a crear (sin ID).
     * @return Event persistido con ID generado por la BD.
     */
    public Event registrar(Event event) {
        validar(event);
        return eventRepository.save(event);
    }

    // -------------------------------------------------------------------------
    // READ — listado paginado
    // -------------------------------------------------------------------------

    /**
     * Retorna una página de eventos ordenables.
     * Acepta ?page=0&size=5&sort=nombre,asc desde la URL.
     *
     * Escenario 3: 50 eventos, page=0, size=5 → 5 registros + totalPages=10.
     *
     * @param pageable Parámetros de paginación inyectados por Spring MVC.
     * @return Page<Event> con datos y metadatos de paginación.
     */
    public Page<Event> listarPaginado(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    /**
     * Busca eventos cuyo nombre contenga el texto dado (case-insensitive), paginado.
     */
    public Page<Event> buscarPorNombre(String nombre, Pageable pageable) {
        return eventRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    // -------------------------------------------------------------------------
    // READ — por ID
    // -------------------------------------------------------------------------

    /**
     * Busca un evento por su ID.
     *
     * @throws ResourceNotFoundException (→ HTTP 404) si no existe.
     */
    public Event buscarPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    // -------------------------------------------------------------------------
    // UPDATE (PUT)
    // -------------------------------------------------------------------------

    /**
     * Actualiza un evento existente.
     * Verifica existencia antes de modificar (→ 404 si no existe).
     *
     * @param id    ID del evento a actualizar.
     * @param datos Nuevos datos a aplicar.
     * @return Event actualizado y persistido.
     */
    public Event actualizar(Long id, Event datos) {
        // 1. Verificar existencia — lanza 404 si no existe
        Event existente = buscarPorId(id);

        // 2. Validar nuevos datos
        validar(datos);

        // 3. Aplicar cambios
        existente.setNombre(datos.getNombre());
        existente.setFecha(datos.getFecha());
        existente.setDescripcion(datos.getDescripcion());
        existente.setVenueId(datos.getVenueId());

        return eventRepository.save(existente);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Elimina un evento de la BD.
     * Lanza 404 si el ID no existe.
     *
     * Escenario 4: eliminación exitosa → el controller retorna 204 No Content.
     *
     * @param id ID del evento a eliminar.
     */
    public void eliminar(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Validaciones de negocio
    // -------------------------------------------------------------------------

    private void validar(Event e) {
        if (e == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del evento es obligatorio y no puede estar vacío.");
        }
        if (e.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del evento es obligatoria.");
        }
        // Verificar que el venue referenciado exista, si se proporcionó
        if (e.getVenueId() != null && !venueRepository.existsById(e.getVenueId())) {
            throw new ResourceNotFoundException("Venue", e.getVenueId());
        }
    }
}
