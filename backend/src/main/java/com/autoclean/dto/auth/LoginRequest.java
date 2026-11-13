package com.autoclean.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para autenticación de usuarios mediante correo y contraseña.
 */
public record LoginRequest(
        @NotBlank(message = "El correo no puede estar vacío")
        @Email(message = "El formato del correo es inválido")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}

