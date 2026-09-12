package com.eventify.husb1.repository;

import com.eventify.husb1.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria para la entidad Event.
 *
 * Simula una base de datos utilizando una List<Event>.
 * El ID se genera de forma automática e incremental con AtomicLong
 * para garantizar seguridad ante accesos concurrentes básicos.
 *
 * @Repository le indica a Spring que este Bean pertenece a la capa
 * de acceso a datos y habilita la traducción de excepciones de persistencia.
 */
@Repository
public class EventRepository {

    /** Colección en memoria que actúa como tabla de eventos. */
    private final List<Event> storage = new ArrayList<>();

    /** Contador autoincremental para generar IDs únicos. */
    private final AtomicLong idCounter = new AtomicLong(1);

    /**
     * Persiste un nuevo evento asignándole un ID generado automáticamente.
     *
     * @param event Evento a guardar (sin ID).
     * @return El mismo evento con el ID asignado.
     */
    public Event save(Event event) {
        event.setId(idCounter.getAndIncrement());
        storage.add(event);
        return event;
    }

    /**
     * Retorna todos los eventos almacenados.
     *
     * @return Lista de eventos (vacía si no hay ninguno registrado).
     */
    public List<Event> findAll() {
        return new ArrayList<>(storage);
    }

    /**
     * Busca un evento por su ID.
     *
     * @param id Identificador del evento.
     * @return Optional con el evento si existe, vacío en caso contrario.
     */
    public Optional<Event> findById(Long id) {
        return storage.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    /**
     * Elimina todos los eventos. Útil para el seeder en pruebas.
     */
    public void clear() {
        storage.clear();
        idCounter.set(1);
    }
}
