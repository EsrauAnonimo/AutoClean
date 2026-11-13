package com.autoclean.dto.appointment;

import com.autoclean.model.VehicleStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para actualizar el estado en vivo del vehículo en una cita.
 */
public record VehicleStatusUpdateDto(
        @JsonAlias("status")
        @NotNull(message = "El estado del vehículo es obligatorio")
        VehicleStatus vehicleStatus
) {
}

