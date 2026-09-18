package com.eventify.husb1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un Lugar (Venue) en Eventify.
 *
 * @Entity  → Hibernate la mapea a la tabla "venues".
 * @Table   → nombre explícito de la tabla en la BD.
 * @Id     → clave primaria gestionada por JPA.
 * @GeneratedValue(IDENTITY) → el motor de BD genera el ID automáticamente.
 * @Column  → restricciones de columna propagadas al DDL generado por Hibernate.
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

    /** Dirección física. Opcional, máximo 255 caracteres. */
    @Column(length = 255)
    private String direccion;

    /**
     * Capacidad máxima de personas.
     * NOT NULL — validado también en la capa Service (> 0).
     */
    @Column(nullable = false)
    private Integer capacidad;
}
