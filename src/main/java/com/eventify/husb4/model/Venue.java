package com.eventify.husb4.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Venue — Lugar físico donde se realizan los eventos.
 *
 * Evolución HU-SB4: se añade el campo city (ciudad),
 * obligatorio para segmentación geográfica.
 */
@Entity
@Table(name = "venues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del venue. NOT NULL, máx 150 caracteres. */
    @Column(nullable = false, length = 150)
    private String name;

    /** Dirección física. Opcional. */
    @Column(length = 255)
    private String address;

    /**
     * Ciudad donde está ubicado el venue.
     * NOT NULL — requerido para búsquedas y segmentación geográfica.
     */
    @Column(nullable = false, length = 100)
    private String city;

    /** Capacidad máxima de personas. NOT NULL. */
    @Column(nullable = false)
    private Integer capacity;
}
