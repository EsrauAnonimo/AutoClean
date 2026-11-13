package com.autoclean.service;

import com.autoclean.dto.report.ReportResult;
import java.time.LocalDate;

/**
 * Contrato de servicio para la generación de reportes operativos y financieros.
 */
public interface ReportService {

    /**
     * Valida que ambas fechas existan y que {@code from} no sea posterior a {@code to}.
     *
     * @param from fecha inicial
     * @param to fecha final
     */
    void validateDateRange(LocalDate from, LocalDate to);

    /**
     * Genera el reporte de servicios ejecutados o programados en el rango indicado.
     *
     * @param from fecha inicial
     * @param to fecha final
     * @return resultado del reporte
     */
    ReportResult servicesByPeriod(LocalDate from, LocalDate to);

    /**
     * Genera el reporte comparativo de ingresos frente a costos estimados en el rango indicado.
     *
     * @param from fecha inicial
     * @param to fecha final
     * @return resultado del reporte
     */
    ReportResult revenueVsCost(LocalDate from, LocalDate to);
}

