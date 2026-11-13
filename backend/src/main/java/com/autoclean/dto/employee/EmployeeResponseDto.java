package com.autoclean.dto.employee;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para representar un empleado de AutoClean.
 *
 * @param id identificador único del empleado
 * @param name nombre completo
 * @param role cargo o función
 * @param createdAt fecha y hora de registro
 */
public record EmployeeResponseDto(
        UUID id,
        String name,
        String role,
        OffsetDateTime createdAt
) {
}
