package com.autoclean.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para crear o actualizar un cliente.
 */
public record CustomerRequest(
        @NotBlank(message = "El nombre del cliente no puede estar vacío")
        String name,

        @NotBlank(message = "El teléfono no puede estar vacío")
        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "El formato del teléfono es inválido")
        String phone,

        @NotBlank(message = "El correo no puede estar vacío")
        @Email(message = "El formato del correo es inválido")
        String email
) {
}
