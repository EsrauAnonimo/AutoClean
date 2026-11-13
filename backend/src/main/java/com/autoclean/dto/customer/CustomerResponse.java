package com.autoclean.dto.customer;

import com.autoclean.model.Customer;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de salida con la representación pública de un cliente.
 */
public record CustomerResponse(
        UUID id,
        String name,
        String phone,
        String email,
        OffsetDateTime createdAt
) {

    /**
     * Convierte una entidad {@link Customer} en su DTO de respuesta.
     *
     * @param customer entidad persistida
     * @return DTO de respuesta equivalente
     */
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getCreatedAt()
        );
    }
}
