package com.eventify.husb4.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Event — Evento de la plataforma Eventify.
 *
 * Novedades HU-SB4:
 *
 * 1. SOFT DELETE con @SQLRestriction:
 *    El filtro "active = true" se aplica automáticamente a TODAS las
 *    consultas JPA sobre esta entidad. Los eventos inactivos nunca
 *    aparecen en los resultados sin necesidad de filtrar manualmente.
 *    La "eliminación" invoca softDelete() → active = false.
 *
 * 2. RELACIÓN Many-to-One obligatoria con Venue:
 *    Cada evento debe tener un venue asignado (nullable = false).
 *    Carga LAZY por defecto; se usa @EntityGraph para cargas específicas.
 *
 * 3. RELACIÓN Many-to-Many con Category:
 *    Tabla intermedia "events_categories" con columnas explícitas
 *    event_id y category_id.
 *    CascadeType.MERGE permite que los vínculos se gestionen al
 *    persistir el evento.
 */
@Entity
@Table(name = "events")
@SQLRestriction("active = true")   // Filtro global automático de Soft Delete
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del evento. NOT NULL, máx 200 caracteres. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Fecha de realización. NOT NULL. */
    @Column(nullable = false)
    private LocalDate date;

    /** Descripción detallada. Opcional, texto largo. */
    @Column(columnDefinition = "TEXT")
    private String description;

    // -------------------------------------------------------------------------
    // SOFT DELETE
    // -------------------------------------------------------------------------

    /**
     * Indicador de estado lógico del registro.
     * true  = activo   (visible en todas las consultas JPA).
     * false = inactivo (filtrado automáticamente por @SQLRestriction).
     *
     * NUNCA se hace DELETE físico. Se llama softDelete() para desactivar.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Desactiva el evento (borrado lógico).
     * Después de llamar este método, el evento no aparecerá en
     * ninguna consulta JPA gracias a @SQLRestriction("active = true").
     */
    public void softDelete() {
        this.active = false;
    }

    // -------------------------------------------------------------------------
    // RELACIÓN Many-to-One → Venue (obligatoria)
    // -------------------------------------------------------------------------

    /**
     * Venue donde se realiza el evento.
     * nullable = false → todo evento DEBE tener un venue asignado.
     * FetchType.LAZY   → no se carga automáticamente (se usa @EntityGraph).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // -------------------------------------------------------------------------
    // RELACIÓN Many-to-Many → Category
    // -------------------------------------------------------------------------

    /**
     * Categorías temáticas del evento.
     *
     * @JoinTable define explícitamente:
     *   - Tabla intermedia: events_categories
     *   - Columna del evento: event_id
     *   - Columna de la categoría: category_id
     *
     * CascadeType.MERGE → al guardar/actualizar un evento se
     * actualizan automáticamente sus vínculos con categorías.
     *
     * FetchType.LAZY → carga diferida; se usa @EntityGraph cuando
     * se necesitan las categorías para evitar N+1.
     */
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "events_categories",
            joinColumns        = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();
}
