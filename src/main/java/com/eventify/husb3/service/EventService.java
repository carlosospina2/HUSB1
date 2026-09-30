package com.eventify.husb3.service;

import com.eventify.husb3.exception.ResourceNotFoundException;
import com.eventify.husb3.model.Event;
import com.eventify.husb3.repository.EventRepository;
import com.eventify.husb3.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para Event.
 * Valida datos, verifica que el Venue referenciado exista
 * y expone métodos para vista (@Controller) y API (@RestController).
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public EventService(EventRepository eventRepository,
                        VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository  = venueRepository;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public Event registrar(Event event) {
        validar(event);
        return eventRepository.save(event);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /** Lista completa — usada por las vistas Thymeleaf. */
    public List<Event> listarTodos() {
        return eventRepository.findAll();
    }

    /** Lista paginada — usada por la API REST. */
    public Page<Event> listarPaginado(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Page<Event> buscarPorNombre(String nombre, Pageable pageable) {
        return eventRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    public Event buscarPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public Event actualizar(Long id, Event datos) {
        Event existente = buscarPorId(id);
        validar(datos);
        existente.setNombre(datos.getNombre());
        existente.setFecha(datos.getFecha());
        existente.setDescripcion(datos.getDescripcion());
        existente.setVenueId(datos.getVenueId());
        return eventRepository.save(existente);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public void eliminar(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Validaciones
    // -------------------------------------------------------------------------

    private void validar(Event e) {
        if (e == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (e.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
        if (e.getVenueId() != null && !venueRepository.existsById(e.getVenueId())) {
            throw new ResourceNotFoundException("Venue", e.getVenueId());
        }
    }
}
