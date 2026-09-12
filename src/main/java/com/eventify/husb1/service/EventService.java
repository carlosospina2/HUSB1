package com.eventify.husb1.service;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para la entidad Event.
 *
 * Responsabilidades:
 *   1. Validar los datos del evento antes de persistirlo.
 *   2. Orquestar la comunicación con EventRepository.
 *   3. Mantener las reglas de negocio aisladas del Controller.
 *
 * Inyección por Constructor:
 *   Se inyecta EventRepository a través del constructor (no @Autowired en campo)
 *   para facilitar el desacoplamiento y la creación de mocks en tests unitarios.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Constructor — Spring inyecta automáticamente el bean EventRepository.
     * Al ser el único constructor, @Autowired es implícito en Spring Boot.
     */
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // -------------------------------------------------------------------------
    // Casos de uso
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo evento tras validar sus datos.
     *
     * Validaciones aplicadas:
     *   - El nombre no puede ser nulo ni estar vacío.
     *   - La fecha no puede ser nula.
     *
     * @param event Objeto Event enviado desde el Controller.
     * @return El evento persistido con su ID asignado.
     * @throws IllegalArgumentException si alguna validación falla.
     */
    public Event registrar(Event event) {
        validarEvento(event);
        return eventRepository.save(event);
    }

    /**
     * Retorna el catálogo completo de eventos.
     * Si no hay eventos registrados retorna una lista vacía (nunca null).
     *
     * @return Lista de todos los eventos almacenados.
     */
    public List<Event> listarTodos() {
        return eventRepository.findAll();
    }

    /**
     * Busca un evento por su ID.
     *
     * @param id Identificador del evento.
     * @return El evento encontrado.
     * @throws IllegalArgumentException si el evento no existe.
     */
    public Event buscarPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un evento con id: " + id));
    }

    // -------------------------------------------------------------------------
    // Validaciones de negocio (privadas — solo visibles en esta capa)
    // -------------------------------------------------------------------------

    /**
     * Valida que el evento cumpla las reglas mínimas de negocio.
     * Escenario 2: un nombre vacío debe ser rechazado aquí, antes de
     * llegar al repositorio.
     */
    private void validarEvento(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        if (event.getNombre() == null || event.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del evento es obligatorio y no puede estar vacío.");
        }
        if (event.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del evento es obligatoria.");
        }
    }
}
