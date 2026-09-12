package com.eventify.husb1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad que representa un Evento en la plataforma Eventify.
 *
 * Lombok elimina el código de plomería:
 *   @Data          → genera getters, setters, equals, hashCode y toString.
 *   @Builder       → patrón builder para construcción fluida de objetos.
 *   @NoArgsConstructor / @AllArgsConstructor → constructores necesarios
 *                   para deserialización JSON y tests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    /** Identificador único del evento (generado en el repositorio). */
    private Long id;

    /** Nombre del evento. No puede ser nulo ni vacío (validado en el Service). */
    private String nombre;

    /** Fecha de realización del evento. */
    private LocalDate fecha;

    /** Descripción detallada del evento. */
    private String descripcion;

    /**
     * Identificador del Venue donde se realizará el evento.
     * Permite asociar Event → Venue sin necesidad de persistencia relacional.
     */
    private Long venueId;
}
