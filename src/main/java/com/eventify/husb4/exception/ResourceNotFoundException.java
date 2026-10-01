package com.eventify.husb4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lanzada cuando un recurso solicitado no existe en la BD.
 * @ResponseStatus(NOT_FOUND) → Spring MVC retorna 404 automáticamente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, Long id) {
        super(entity + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
