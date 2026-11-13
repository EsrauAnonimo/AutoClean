package com.autoclean.service;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.dto.appointment.OccupiedSlotDto;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.model.VehicleStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la gestión de citas automotrices y su ciclo de vida.
 */
public interface AppointmentService {

    /**
     * Registra una nueva cita. Si no se indica estado, inicia en {@code PENDING}.
     *
     * @param request datos de creación de la cita
     * @return cita creada
     */
    AppointmentResponseDto create(AppointmentRequestDto request);

    /**
     * Actualiza los datos de una cita existente validando sus referencias y máquina de estados.
     *
     * @param id identificador único de la cita
     * @param request nuevos datos de la cita
     * @return cita actualizada
     */
    AppointmentResponseDto update(UUID id, AppointmentRequestDto request);

    /**
     * Elimina una cita existente por su identificador.
     *
     * @param id identificador único de la cita
     */
    void delete(UUID id);

    /**
     * Busca una cita por su identificador único.
     *
     * @param id identificador único de la cita
     * @return cita encontrada
     */
    AppointmentResponseDto findById(UUID id);

    /**
     * Lista todas las citas registradas.
     *
     * @return lista de citas
     */
    List<AppointmentResponseDto> findAll();

    /**
     * Lista las citas asociadas a un cliente específico.
     *
     * @param customerId identificador del cliente
     * @return citas del cliente
     */
    List<AppointmentResponseDto> findByCustomerId(UUID customerId);

    /**
     * Lista las citas filtradas por estado.
     *
     * @param status estado de la cita
     * @return citas en ese estado
     */
    List<AppointmentResponseDto> findByStatus(AppointmentStatus status);

    /**
     * Transiciona el estado de una cita aplicando estrictamente la máquina de estados.
     *
     * @param appointmentId identificador de la cita
     * @param newStatus estado destino solicitado
     * @return cita actualizada con el nuevo estado
     */
    AppointmentResponseDto updateStatus(UUID appointmentId, AppointmentStatus newStatus);

    /**
     * Cancela una cita por iniciativa del cliente; únicamente permitido si está en estado PENDING.
     *
     * @param appointmentId identificador de la cita
     * @return cita actualizada en estado CANCELLED
     */
    AppointmentResponseDto cancel(UUID appointmentId);

    /**
     * Actualiza el estado operativo en tiempo real del vehículo en la cita.
     *
     * @param appointmentId identificador de la cita
     * @param vehicleStatus nuevo estado del vehículo
     * @return cita actualizada con el estado del vehículo
     */
    AppointmentResponseDto updateVehicleStatus(UUID appointmentId, VehicleStatus vehicleStatus);

    /**
     * Consulta las franjas horarias ya ocupadas alrededor de una fecha para evitar cruces al agendar.
     *
     * @param date fecha objetivo en formato calendario
     * @param employeeId identificador opcional de especialista para filtrar ocupación
     * @return franjas ocupadas activas
     */
    List<OccupiedSlotDto> findOccupiedSlots(LocalDate date, UUID employeeId);
}

