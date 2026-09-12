package com.eventify.husb1.repository;

import com.eventify.husb1.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria para la entidad Venue.
 *
 * Simula una base de datos utilizando una List<Venue>.
 * Sigue exactamente el mismo contrato que EventRepository para
 * mantener coherencia en la capa de acceso a datos.
 *
 * @Repository registra este Bean en el contexto de Spring como
 * componente de la capa de persistencia.
 */
@Repository
public class VenueRepository {

    /** Colección en memoria que actúa como tabla de venues. */
    private final List<Venue> storage = new ArrayList<>();

    /** Contador autoincremental para generar IDs únicos. */
    private final AtomicLong idCounter = new AtomicLong(1);

    /**
     * Persiste un nuevo venue asignándole un ID generado automáticamente.
     *
     * @param venue Venue a guardar (sin ID).
     * @return El mismo venue con el ID asignado.
     */
    public Venue save(Venue venue) {
        venue.setId(idCounter.getAndIncrement());
        storage.add(venue);
        return venue;
    }

    /**
     * Retorna todos los venues almacenados.
     *
     * @return Lista de venues (vacía si no hay ninguno registrado).
     */
    public List<Venue> findAll() {
        return new ArrayList<>(storage);
    }

    /**
     * Busca un venue por su ID.
     *
     * @param id Identificador del venue.
     * @return Optional con el venue si existe, vacío en caso contrario.
     */
    public Optional<Venue> findById(Long id) {
        return storage.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
    }

    /**
     * Elimina todos los venues. Útil para reiniciar el seeder en pruebas.
     */
    public void clear() {
        storage.clear();
        idCounter.set(1);
    }
}
