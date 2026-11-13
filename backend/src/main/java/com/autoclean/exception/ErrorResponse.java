package com.autoclean.exception;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * DTO para estandarizar las respuestas de error en la API REST.
 *
 * @param timestamp fecha y hora en que ocurrió el error
 * @param status código de estado HTTP
 * @param error nombre descriptivo del error HTTP
 * @param message mensaje explicativo del error
 * @param path ruta del recurso que originó la petición
 * @param validationErrors detalle de errores de validación de campos (opcional)
 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(OffsetDateTime.now(), status, error, message, path, null);
    }
}
