package com.autoclean.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el resultado tabulado de un reporte generado en el sistema.
 */
public class ReportResult {

    private String title;
    private LocalDateTime generatedAt;
    private List<String[]> rows = new ArrayList<>();

    /**
     * Constructor por defecto.
     */
    public ReportResult() {
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Constructor completo.
     *
     * @param title título o encabezado del reporte
     * @param generatedAt fecha y hora de generación
     * @param rows filas de datos con valores en formato String
     */
    public ReportResult(String title, LocalDateTime generatedAt, List<String[]> rows) {
        setTitle(title);
        setGeneratedAt(generatedAt);
        setRows(rows);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del reporte no puede estar vacío");
        }
        this.title = title.trim();
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        if (generatedAt == null) {
            throw new IllegalArgumentException("La fecha de generación no puede ser nula");
        }
        this.generatedAt = generatedAt;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void setRows(List<String[]> rows) {
        this.rows = rows != null ? new ArrayList<>(rows) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return title + " (" + rows.size() + " filas, generado: " + generatedAt + ")";
    }
}
