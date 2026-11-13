package com.autoclean.rules.spec;

/**
 * Contrato espejo de la máquina de estados de citas de AutoClean.
 * Permite ejecutar pruebas de especificación desacopladas de la implementación final en Spring.
 */
public interface AppointmentStatusSpec {

    /**
     * Registra un turno con estado inicial opcional (por defecto PENDING).
     *
     * @param appointmentId identificador del turno
     * @param initialStatus estado inicial
     */
    void registerAppointment(String appointmentId, String initialStatus);

    /**
     * Cambia el estado de un turno aplicando las transiciones válidas del sistema legacy.
     *
     * @param appointmentId identificador del turno
     * @param newStatus nuevo estado destino
     */
    void updateStatus(String appointmentId, String newStatus);

    /**
     * Consulta el estado actual de un turno registrado.
     *
     * @param appointmentId identificador del turno
     * @return estado actual normalizado
     */
    String getStatus(String appointmentId);
}
