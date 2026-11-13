package com.autoclean.dto.supply;

import com.autoclean.model.Supply;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de salida con la información pública de un insumo sin exponer entidades JPA.
 */
public record SupplyResponse(
        UUID id,
        String name,
        Integer stock,
        String unit,
        OffsetDateTime createdAt
) {

    /**
     * Mapea una entidad {@link Supply} a su representación de respuesta.
     *
     * @param supply entidad persistida
     * @return DTO equivalente
     */
    public static SupplyResponse from(Supply supply) {
        return new SupplyResponse(
                supply.getId(),
                supply.getName(),
                supply.getStock(),
                supply.getUnit(),
                supply.getCreatedAt()
        );
    }
}

