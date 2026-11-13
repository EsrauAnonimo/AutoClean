package com.autoclean.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO de entrada para crear o actualizar un vehículo asociado a un cliente.
 */
public record VehicleRequest(
        @NotNull(message = "El identificador del cliente es obligatorio")
        UUID customerId,

        @NotBlank(message = "La placa no puede estar vacía")
        String plate,

        @NotBlank(message = "La marca no puede estar vacía")
        String brand,

        @NotBlank(message = "El modelo no puede estar vacío")
        String model,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 1900, message = "El año debe ser válido (entre 1900 y 2100)")
        @Max(value = 2100, message = "El año debe ser válido (entre 1900 y 2100)")
        Integer year
) {
}
