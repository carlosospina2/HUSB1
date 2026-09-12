package com.eventify.husb1.service;

import com.eventify.husb1.model.Venue;
import com.eventify.husb1.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para la entidad Venue.
 *
 * Responsabilidades:
 *   1. Validar los datos del venue antes de persistirlo.
 *   2. Orquestar la comunicación con VenueRepository.
 *   3. Mantener las reglas de negocio aisladas del Controller.
 *
 * Inyección por Constructor:
 *   VenueRepository se inyecta por constructor para permitir
 *   la creación de instancias con mocks en pruebas unitarias
 *   sin necesidad de levantar el contexto de Spring.
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * Constructor — Spring inyecta automáticamente el bean VenueRepository.
     */
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    // -------------------------------------------------------------------------
    // Casos de uso
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo venue tras validar sus datos.
     *
     * Validaciones aplicadas:
     *   - El nombre no puede ser nulo ni estar vacío.
     *   - La capacidad debe ser mayor que cero.
     *
     * @param venue Objeto Venue enviado desde el Controller.
     * @return El venue persistido con su ID asignado.
     * @throws IllegalArgumentException si alguna validación falla.
     */
    public Venue registrar(Venue venue) {
        validarVenue(venue);
        return venueRepository.save(venue);
    }

    /**
     * Retorna el catálogo completo de venues.
     * Si no hay venues registrados retorna una lista vacía (nunca null).
     *
     * @return Lista de todos los venues almacenados.
     */
    public List<Venue> listarTodos() {
        return venueRepository.findAll();
    }

    /**
     * Busca un venue por su ID.
     *
     * @param id Identificador del venue.
     * @return El venue encontrado.
     * @throws IllegalArgumentException si el venue no existe.
     */
    public Venue buscarPorId(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un venue con id: " + id));
    }

    // -------------------------------------------------------------------------
    // Validaciones de negocio
    // -------------------------------------------------------------------------

    /**
     * Valida que el venue cumpla las reglas mínimas de negocio.
     */
    private void validarVenue(Venue venue) {
        if (venue == null) {
            throw new IllegalArgumentException("El venue no puede ser nulo.");
        }
        if (venue.getNombre() == null || venue.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del venue es obligatorio y no puede estar vacío.");
        }
        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad del venue debe ser un número mayor que cero.");
        }
    }
}
