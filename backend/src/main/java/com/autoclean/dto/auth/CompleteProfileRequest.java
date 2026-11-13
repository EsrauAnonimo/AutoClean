package com.autoclean.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para completar o vincular el perfil de cliente de una cuenta ya autenticada
 * (por ejemplo, usuarios registrados mediante Google OAuth2).
 */
public record CompleteProfileRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        String name,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "El formato del teléfono es inválido")
        String phone
) {
}

