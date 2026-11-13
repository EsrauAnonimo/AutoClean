package com.autoclean.dto.costing;

import com.autoclean.model.CostEstimate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida con el resumen de costeo y las líneas de gasto asociadas.
 */
public record CostEstimateResponse(
        UUID id,
        UUID serviceTypeId,
        String serviceTypeName,
        UUID appointmentId,
        List<CostLineResponse> lines,
        BigDecimal estimatedCost,
        BigDecimal price,
        BigDecimal difference,
        OffsetDateTime createdAt
) {

    /**
     * Convierte una entidad {@link CostEstimate} a DTO de salida sin exponer entidades JPA.
     *
     * @param estimate entidad de estimación de costo
     * @return DTO de respuesta con sus líneas
     */
    public static CostEstimateResponse from(CostEstimate estimate) {
        List<CostLineResponse> lineResponses = estimate.getLines() == null
                ? List.of()
                : estimate.getLines().stream().map(CostLineResponse::from).toList();

        return new CostEstimateResponse(
                estimate.getId(),
                estimate.getServiceType().getId(),
                estimate.getServiceType().getName(),
                estimate.getAppointment() != null ? estimate.getAppointment().getId() : null,
                lineResponses,
                estimate.getEstimatedCost(),
                estimate.getPrice(),
                estimate.getDifference(),
                estimate.getCreatedAt()
        );
    }
}

