package com.eventify.husb3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un Lugar (Venue) en Eventify.
 * Mapeada a la tabla "venues" en H2.
 */
@Entity
@Table(name = "venues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del venue. NOT NULL, máximo 150 caracteres. */
    @Column(nullable = false, length = 150)
    private String nombre;

    /** Dirección física. Opcional. */
    @Column(length = 255)
    private String direccion;

    /**
     * Capacidad máxima de personas.
     * NOT NULL — validado también en el Service (> 0).
     */
    @Column(nullable = false)
    private Integer capacidad;
}
