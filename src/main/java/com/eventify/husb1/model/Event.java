package com.eventify.husb1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un Evento en Eventify.
 *
 * @Entity  → Hibernate la mapea a la tabla "events".
 * @Table   → nombre explícito de la tabla en la BD.
 *
 * Relación con Venue:
 *   Se mantiene como FK simple (venue_id) para esta fase.
 *   La relación @ManyToOne puede añadirse en una iteración futura
 *   sin romper la estructura actual.
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
     * ID del Venue asociado.
     * Almacenado como FK simple — integridad referencial gestionada
     * en la capa Service (verifica que el venue exista antes de guardar).
     */
    @Column(name = "venue_id")
    private Long venueId;
}
