package com.autoclean.dto.servicetype;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para representar un tipo de servicio del catálogo.
 *
 * @param id identificador único del servicio
 * @param name nombre del servicio
 * @param description descripción
 * @param price precio base
 * @param durationMin duración en minutos
 * @param createdAt fecha y hora de creación
 */
public record ServiceTypeResponseDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMin,
        OffsetDateTime createdAt
) {
}
