package com.autoclean.dto.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida con el título, fecha de generación y filas tabuladas de un reporte gerencial.
 */
public record ReportResult(
        String title,
        LocalDateTime generatedAt,
        List<String[]> rows
) {

    /**
     * Valida los invariantes básicos del reporte y normaliza una lista inmutable de filas.
     */
    public ReportResult {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del reporte no puede estar vacío");
        }
        if (generatedAt == null) {
            throw new IllegalArgumentException("La fecha de generación no puede ser nula");
        }
        title = title.trim();
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}

