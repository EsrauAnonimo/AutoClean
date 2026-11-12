package com.autoclean.contracts;

import com.autoclean.model.ReportResult;
import java.time.LocalDate;

/**
 * Contrato de servicio para la generación de reportes gerenciales.
 */
public interface ReportService {

    /**
     * Genera un reporte de servicios ejecutados en un rango de fechas.
     */
    ReportResult getServicesByPeriod(LocalDate from, LocalDate to);

    /**
     * Genera un reporte comparativo de ingresos versus costos en un rango de fechas.
     */
    ReportResult getIncomeVsCosts(LocalDate from, LocalDate to);
}
