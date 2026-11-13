package com.autoclean.dto.appointment;

import com.autoclean.model.Appointment;
import com.autoclean.model.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de salida con la representación pública de una cita automotriz.
 * Evita exponer directamente la entidad JPA y sus relaciones perezosas.
 */
public record AppointmentResponseDto(
        UUID id,
        UUID customerId,
        String customerName,
        UUID vehicleId,
        String vehiclePlate,
        UUID employeeId,
        String employeeName,
        UUID serviceTypeId,
        String serviceTypeName,
        OffsetDateTime dateTime,
        AppointmentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Convierte una entidad {@link Appointment} en su DTO de respuesta.
     *
     * @param appointment entidad persistida
     * @return DTO de respuesta equivalente
     */
    public static AppointmentResponseDto from(Appointment appointment) {
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getCustomer() != null ? appointment.getCustomer().getId() : null,
                appointment.getCustomer() != null ? appointment.getCustomer().getName() : null,
                appointment.getVehicle() != null ? appointment.getVehicle().getId() : null,
                appointment.getVehicle() != null ? appointment.getVehicle().getPlate() : null,
                appointment.getEmployee() != null ? appointment.getEmployee().getId() : null,
                appointment.getEmployee() != null ? appointment.getEmployee().getName() : null,
                appointment.getServiceType() != null ? appointment.getServiceType().getId() : null,
                appointment.getServiceType() != null ? appointment.getServiceType().getName() : null,
                appointment.getDateTime(),
                appointment.getStatus(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}

