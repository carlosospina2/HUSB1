package com.eventify.husb1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un Lugar (Venue) en la plataforma Eventify.
 *
 * Un Venue es el espacio físico donde se realizan los eventos.
 * Puede estar asociado a múltiples Events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    /** Identificador único del venue (generado en el repositorio). */
    private Long id;

    /** Nombre del venue. No puede ser nulo ni vacío (validado en el Service). */
    private String nombre;

    /** Dirección física del venue. */
    private String direccion;

    /**
     * Capacidad máxima de personas que puede albergar el venue.
     * Debe ser mayor que cero (validado en el Service).
     */
    private Integer capacidad;
}
