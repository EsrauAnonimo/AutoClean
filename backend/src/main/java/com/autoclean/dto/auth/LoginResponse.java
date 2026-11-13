package com.autoclean.dto.auth;

import java.util.UUID;

/**
 * DTO de respuesta emitido tras autenticar o registrar un usuario con su token JWT.
 */
public record LoginResponse(
        String token,
        String type,
        String email,
        String role,
        UUID customerId
) {
}

