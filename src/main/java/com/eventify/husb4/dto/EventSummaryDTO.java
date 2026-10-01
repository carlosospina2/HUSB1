package com.eventify.husb4.dto;

import java.time.LocalDate;

/**
 * Record de proyección para listados masivos de eventos.
 *
 * Java Record (Java 16+):
 *   - Inmutable por diseño (todos los campos son final).
 *   - Genera automáticamente constructor, getters, equals, hashCode y toString.
 *   - Ideal para DTOs de solo lectura: no necesita setters ni anotaciones extra.
 *
 * Propósito en HU-SB4:
 *   Reemplaza el uso de entidades pesadas (Event con LAZY relations) en los
 *   listados. La consulta JPQL construye este Record directamente en la BD
 *   con NEW com.eventify.husb4.dto.EventSummaryDTO(...), "aplanando" la
 *   información de Event + Venue en una sola proyección liviana.
 *
 * Campos "aplanados" (Event + Venue):
 *   id          → Event.id
 *   eventName   → Event.name
 *   date        → Event.date
 *   venueName   → Venue.name
 *   city        → Venue.city
 *
 * Regla técnica cumplida:
 *   Se prohíbe el uso de entidades pesadas en listados → este Record
 *   garantiza que solo los campos necesarios viajan desde la BD.
 */
public record EventSummaryDTO(
        Long      id,
        String    eventName,
        LocalDate date,
        String    venueName,
        String    city
) {}
