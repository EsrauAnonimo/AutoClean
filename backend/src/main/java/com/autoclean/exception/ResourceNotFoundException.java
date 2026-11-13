package com.autoclean.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en la base de datos.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
