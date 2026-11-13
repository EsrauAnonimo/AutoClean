package com.autoclean.dto.employee;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para la creación o actualización de un empleado.
 *
 * @param name nombre completo del empleado (obligatorio)
 * @param role cargo o rol desempeñado en el taller (obligatorio)
 */
public record EmployeeRequestDto(
        @NotBlank(message = "El nombre del empleado no puede estar vacío")
        String name,

        @NotBlank(message = "El cargo o rol del empleado no puede estar vacío")
        String role
) {
}
