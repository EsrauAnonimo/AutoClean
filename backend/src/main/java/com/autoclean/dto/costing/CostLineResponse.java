package com.autoclean.dto.costing;

import com.autoclean.model.CostLine;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de salida con el detalle de una línea de costo persistida.
 */
public record CostLineResponse(
        UUID id,
        String concept,
        BigDecimal amount,
        OffsetDateTime createdAt
) {

    /**
     * Convierte una entidad {@link CostLine} a DTO de respuesta.
     *
     * @param line entidad de línea de costo
     * @return DTO equivalente
     */
    public static CostLineResponse from(CostLine line) {
        return new CostLineResponse(
                line.getId(),
                line.getConcept(),
                line.getAmount(),
                line.getCreatedAt()
        );
    }
}

