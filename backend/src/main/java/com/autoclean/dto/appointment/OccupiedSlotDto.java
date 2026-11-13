package com.autoclean.dto.appointment;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO público autenticado con franjas horarias ya ocupadas para evitar cruces al agendar.
 * No expone datos personales de otros clientes.
 */
public record OccupiedSlotDto(
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        UUID employeeId
) {
}

