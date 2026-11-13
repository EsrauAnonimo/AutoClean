package com.autoclean.dto.servicetype;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO de solicitud para la creación o actualización de un tipo de servicio.
 *
 * @param name nombre del servicio (obligatorio)
 * @param description descripción del servicio (opcional)
 * @param price precio base del servicio (obligatorio, no negativo)
 * @param durationMin duración estimada en minutos (obligatorio, mayor a 0)
 */
public record ServiceTypeRequestDto(
        @NotBlank(message = "El nombre del servicio no puede estar vacío")
        String name,

        String description,

        @NotNull(message = "El precio del servicio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio del servicio no puede ser negativo")
        BigDecimal price,

        @NotNull(message = "La duración del servicio es obligatoria")
        @Min(value = 1, message = "La duración del servicio debe ser mayor a 0 minutos")
        Integer durationMin
) {
}
