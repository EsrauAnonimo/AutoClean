package com.autoclean.dto.report;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO de entrada para solicitar reportes por rango de fechas.
 */
public record ReportRequest(
        @NotNull(message = "La fecha inicial es obligatoria")
        LocalDate from,

        @NotNull(message = "La fecha final es obligatoria")
        LocalDate to
) {
}

