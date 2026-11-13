package com.autoclean.dto.supply;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para crear o actualizar un insumo de inventario.
 */
public record SupplyRequest(
        @NotBlank(message = "El nombre del insumo es obligatorio")
        String name,

        @NotNull(message = "El stock del insumo es obligatorio")
        @Min(value = 0, message = "El stock del insumo no puede ser negativo")
        Integer stock,

        @NotBlank(message = "La unidad de medida es obligatoria")
        String unit
) {
}

