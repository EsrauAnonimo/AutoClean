package com.autoclean.contracts;

import com.autoclean.model.Appointment;
import java.util.List;

/**
 * Contrato de servicio para la asignación y gestión de turnos de servicio.
 */
public interface AppointmentService {

    /**
     * Registra un nuevo turno.
     */
    Appointment create(Appointment appointment);

    /**
     * Actualiza los datos de un turno existente.
     */
    Appointment update(Appointment appointment);

    /**
     * Elimina un turno por su identificador.
     */
    void delete(String id);

    /**
     * Busca un turno por su identificador.
     */
    Appointment findById(String id);

    /**
     * Obtiene el listado de todos los turnos.
     */
    List<Appointment> findAll();

    /**
     * Cambia el estado de un turno (PENDING, IN_PROGRESS, FINISHED, CANCELLED).
     */
    void updateStatus(String appointmentId, String newStatus);
}
