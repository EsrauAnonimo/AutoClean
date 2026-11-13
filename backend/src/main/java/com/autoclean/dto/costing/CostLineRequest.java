package com.autoclean.dto.costing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO de entrada para una línea individual de costo directo.
 */
public record CostLineRequest(
        @NotBlank(message = "El concepto de la línea de costo es obligatorio")
        String concept,

        @NotNull(message = "El monto de la línea de costo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto de la línea de costo no puede ser negativo")
        BigDecimal amount
) {
}

