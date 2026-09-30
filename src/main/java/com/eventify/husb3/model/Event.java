package com.eventify.husb3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un Evento en Eventify.
 * Mapeada a la tabla "events" en H2.
 */
@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del evento. NOT NULL, máximo 200 caracteres. */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Fecha de realización. NOT NULL. */
    @Column(nullable = false)
    private LocalDate fecha;

    /** Descripción detallada. Texto largo, opcional. */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * ID del Venue asociado (FK simple).
     * La integridad se valida en la capa Service.
     */
    @Column(name = "venue_id")
    private Long venueId;
}
