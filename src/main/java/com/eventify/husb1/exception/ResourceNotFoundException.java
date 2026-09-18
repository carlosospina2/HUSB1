package com.eventify.husb1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la BD.
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND) le indica a Spring MVC que,
 * cuando esta excepción llegue sin ser capturada en el Controller,
 * la respuesta HTTP debe tener código 404 Not Found automáticamente.
 *
 * Escenario 2 (Camino de Error):
 *   - GET  /api/events/9999  → 404 "Event no encontrado con id: 9999"
 *   - PUT  /api/events/9999  → 404
 *   - DELETE /api/events/9999 → 404
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param entidad Nombre del tipo de recurso (ej: "Event", "Venue").
     * @param id      Identificador que no fue encontrado.
     */
    public ResourceNotFoundException(String entidad, Long id) {
        super(entidad + " no encontrado con id: " + id);
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
