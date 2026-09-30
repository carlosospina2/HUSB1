package com.eventify.husb3.service;

import com.eventify.husb3.exception.ResourceNotFoundException;
import com.eventify.husb3.model.Venue;
import com.eventify.husb3.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para Venue.
 * Contiene validaciones, orquesta el repositorio y expone
 * métodos para los controladores de vista (@Controller) y de API (@RestController).
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public Venue registrar(Venue venue) {
        validar(venue);
        return venueRepository.save(venue);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /** Lista completa — usada por las vistas Thymeleaf. */
    public List<Venue> listarTodos() {
        return venueRepository.findAll();
    }

    /** Lista paginada — usada por la API REST. */
    public Page<Venue> listarPaginado(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    public Page<Venue> buscarPorNombre(String nombre, Pageable pageable) {
        return venueRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    public Venue buscarPorId(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public Venue actualizar(Long id, Venue datos) {
        Venue existente = buscarPorId(id);
        validar(datos);
        existente.setNombre(datos.getNombre());
        existente.setDireccion(datos.getDireccion());
        existente.setCapacidad(datos.getCapacidad());
        return venueRepository.save(existente);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public void eliminar(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue", id);
        }
        venueRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Validaciones
    // -------------------------------------------------------------------------

    private void validar(Venue v) {
        if (v == null) {
            throw new IllegalArgumentException("El venue no puede ser nulo.");
        }
        if (v.getNombre() == null || v.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del venue es obligatorio.");
        }
        if (v.getCapacidad() == null || v.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
    }
}
