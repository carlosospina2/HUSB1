package com.eventify.husb3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lanzada cuando un recurso solicitado no existe en la BD.
 * @ResponseStatus(NOT_FOUND) → Spring MVC retorna 404 automáticamente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entidad, Long id) {
        super(entidad + " no encontrado con id: " + id);
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
