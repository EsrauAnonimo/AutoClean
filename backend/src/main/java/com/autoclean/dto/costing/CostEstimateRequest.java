package com.autoclean.dto.costing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * DTO de entrada para calcular y registrar una estimación de costo de servicio.
 */
public record CostEstimateRequest(
        @NotNull(message = "El tipo de servicio es obligatorio")
        UUID serviceTypeId,

        UUID appointmentId,

        @Valid
        List<CostLineRequest> lines
) {
}

