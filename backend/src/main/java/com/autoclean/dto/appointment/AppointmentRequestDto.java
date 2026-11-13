package com.autoclean.dto.appointment;

import com.autoclean.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de entrada para crear o actualizar una cita de servicio automotriz.
 *
 * @param customerId identificador del cliente propietario de la cita
 * @param vehicleId identificador del vehículo asignado a la cita
 * @param employeeId identificador del empleado responsable de la atención
 * @param serviceTypeId identificador del tipo de servicio único (Opción A)
 * @param dateTime fecha y hora programada con zona horaria
 * @param status estado opcional (al crear asume {@code PENDING} si es nulo)
 */
public record AppointmentRequestDto(
        @NotNull(message = "El identificador del cliente es obligatorio")
        UUID customerId,

        @NotNull(message = "El identificador del vehículo es obligatorio")
        UUID vehicleId,

        @NotNull(message = "El identificador del empleado es obligatorio")
        UUID employeeId,

        @NotNull(message = "El identificador del tipo de servicio es obligatorio")
        UUID serviceTypeId,

        @NotNull(message = "La fecha y hora de la cita son obligatorias")
        OffsetDateTime dateTime,

        AppointmentStatus status
) {
}

