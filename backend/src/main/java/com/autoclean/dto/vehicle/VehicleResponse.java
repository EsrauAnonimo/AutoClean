package com.autoclean.dto.vehicle;

import com.autoclean.model.Vehicle;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de salida con la representación pública de un vehículo.
 */
public record VehicleResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String plate,
        String brand,
        String model,
        Integer year,
        OffsetDateTime createdAt
) {

    /**
     * Convierte una entidad {@link Vehicle} en su DTO de respuesta.
     *
     * @param vehicle entidad persistida
     * @return DTO de respuesta equivalente
     */
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomer().getId(),
                vehicle.getCustomer().getName(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getCreatedAt()
        );
    }
}
