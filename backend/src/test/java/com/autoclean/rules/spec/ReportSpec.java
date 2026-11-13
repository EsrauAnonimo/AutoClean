package com.autoclean.rules.spec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato espejo de la generación de reportes y validación de rangos de fecha del legacy.
 */
public interface ReportSpec {

    /**
     * Resultado tabulado de un reporte.
     */
    record ReportResultSpec(String title, LocalDateTime generatedAt, List<String[]> rows) {
    }

    /**
     * Registro mínimo de turno para pruebas de reportes por periodo.
     */
    record AppointmentRecordSpec(
            String id,
            String customerName,
            String serviceName,
            String status,
            LocalDateTime dateTime,
            double price,
            double estimatedCost) {
    }

    /**
     * Registra un turno para consultas de reportes.
     *
     * @param record turno con datos resueltos
     */
    void registerAppointment(AppointmentRecordSpec record);

    /**
     * Genera el reporte de servicios dentro del rango de fechas dado.
     *
     * @param from fecha inicial inclusive
     * @param to fecha final inclusive
     * @return resultado del reporte
     */
    ReportResultSpec servicesByPeriod(LocalDate from, LocalDate to);

    /**
     * Genera el reporte comparativo de ingresos vs costos dentro del rango dado.
     *
     * @param from fecha inicial inclusive
     * @param to fecha final inclusive
     * @return resultado del reporte
     */
    ReportResultSpec revenueVsCost(LocalDate from, LocalDate to);
}
