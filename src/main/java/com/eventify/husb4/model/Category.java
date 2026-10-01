package com.eventify.husb4.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Category — Clasificación temática de los eventos.
 *
 * Ejemplos: Conciertos, Talleres, Conferencias, Deportes,
 *           Gastronomía, Festivales, Teatro.
 *
 * Relación: N:M con Event (tabla intermedia events_categories).
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la categoría. NOT NULL, único, máx 80 caracteres. */
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    /** Descripción opcional de la categoría. */
    @Column(columnDefinition = "TEXT")
    private String description;
}
