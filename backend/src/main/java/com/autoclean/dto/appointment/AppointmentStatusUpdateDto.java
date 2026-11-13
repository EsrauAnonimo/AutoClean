package com.autoclean.dto.appointment;

import com.autoclean.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para cambiar el estado de una cita aplicando su máquina de estados.
 *
 * @param status nuevo estado destino de la cita
 */
public record AppointmentStatusUpdateDto(
        @NotNull(message = "El nuevo estado de la cita es obligatorio")
        AppointmentStatus status
) {
}

