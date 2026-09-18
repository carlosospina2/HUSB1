package com.eventify.husb1.service;

import com.eventify.husb1.exception.ResourceNotFoundException;
import com.eventify.husb1.model.Venue;
import com.eventify.husb1.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Capa de negocio para Venue.
 *
 * Responsabilidades:
 *   1. Validar datos antes de persistir.
 *   2. Lanzar ResourceNotFoundException (→ 404) cuando el recurso no existe.
 *   3. Orquestar el acceso a VenueRepository.
 *   4. Exponer listados paginados y ordenados mediante Pageable.
 *
 * Inyección por constructor en todos los componentes.
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

    /**
     * Registra un nuevo venue tras validar sus datos.
     *
     * @param venue Datos del venue a crear (sin ID).
     * @return Venue persistido con ID generado por la BD.
     * @throws IllegalArgumentException si la validación de negocio falla.
     */
    public Venue registrar(Venue venue) {
        validar(venue);
        return venueRepository.save(venue);
    }

    // -------------------------------------------------------------------------
    // READ — listado paginado
    // -------------------------------------------------------------------------

    /**
     * Retorna una página de venues.
     * Soporta ?page=0&size=10&sort=nombre,asc mediante el objeto Pageable.
     *
     * Escenario 3: con 50 venues y page=0&size=5 retorna exactamente 5 registros
     * más los metadatos de paginación (totalElements, totalPages, etc.).
     *
     * @param pageable Parámetros de paginación y ordenamiento inyectados por Spring MVC.
     * @return Page<Venue> con los datos y metadatos de paginación.
     */
    public Page<Venue> listarPaginado(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    /**
     * Busca venues cuyo nombre contenga el texto dado (case-insensitive), paginado.
     */
    public Page<Venue> buscarPorNombre(String nombre, Pageable pageable) {
        return venueRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    // -------------------------------------------------------------------------
    // READ — por ID
    // -------------------------------------------------------------------------

    /**
     * Busca un venue por su ID.
     *
     * @throws ResourceNotFoundException (→ HTTP 404) si no existe.
     */
    public Venue buscarPorId(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    // -------------------------------------------------------------------------
    // UPDATE (PUT)
    // -------------------------------------------------------------------------

    /**
     * Actualiza un venue existente.
     * Valida que el recurso exista antes de modificarlo (→ 404 si no existe).
     *
     * @param id    ID del venue a actualizar.
     * @param datos Nuevos datos a aplicar.
     * @return Venue actualizado y persistido.
     */
    public Venue actualizar(Long id, Venue datos) {
        // 1. Verificar existencia — lanza 404 si no existe
        Venue existente = buscarPorId(id);

        // 2. Validar los nuevos datos
        validar(datos);

        // 3. Aplicar cambios sobre la entidad gestionada por JPA
        existente.setNombre(datos.getNombre());
        existente.setDireccion(datos.getDireccion());
        existente.setCapacidad(datos.getCapacidad());

        return venueRepository.save(existente);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Elimina un venue de la BD.
     * Lanza 404 si el ID no existe, garantizando que el cliente reciba
     * un error claro en lugar de una eliminación silenciosa.
     *
     * Escenario 4: eliminación exitosa → el controller retorna 204 No Content.
     *
     * @param id ID del venue a eliminar.
     */
    public void eliminar(Long id) {
        // Verificar existencia antes de borrar
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue", id);
        }
        venueRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Validaciones de negocio
    // -------------------------------------------------------------------------

    private void validar(Venue v) {
        if (v == null) {
            throw new IllegalArgumentException("El venue no puede ser nulo.");
        }
        if (v.getNombre() == null || v.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del venue es obligatorio y no puede estar vacío.");
        }
        if (v.getCapacidad() == null || v.getCapacidad() <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad del venue debe ser un número mayor que cero.");
        }
    }
}
