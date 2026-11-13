package com.autoclean.rules.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub espejo de {@code AppointmentServiceImpl.updateStatus} en el sistema legacy.
 */
public class LegacyMirrorAppointmentStatusSpec implements AppointmentStatusSpec {

    private final Map<String, String> statuses = new HashMap<>();

    @Override
    public void registerAppointment(String appointmentId, String initialStatus) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        String normalized = (initialStatus == null || initialStatus.trim().isEmpty())
                ? "PENDING"
                : initialStatus.trim().toUpperCase();
        statuses.put(appointmentId.trim(), normalized);
    }

    @Override
    public void updateStatus(String appointmentId, String newStatus) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío");
        }

        String id = appointmentId.trim();
        String currentStatus = statuses.get(id);
        if (currentStatus == null) {
            throw new IllegalArgumentException("No se encontró el turno con identificador: " + appointmentId);
        }

        String targetStatus = newStatus.trim().toUpperCase();

        // Replica exactamente las transiciones de AppointmentServiceImpl.java:99-111
        if ("PENDING".equals(currentStatus)) {
            if (!"IN_PROGRESS".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                throw new IllegalStateException("Transición inválida desde PENDING hacia " + targetStatus);
            }
        } else if ("IN_PROGRESS".equals(currentStatus)) {
            if (!"FINISHED".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                throw new IllegalStateException("Transición inválida desde IN_PROGRESS hacia " + targetStatus);
            }
        } else if ("FINISHED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("El turno ya se encuentra en estado final: " + currentStatus);
        } else {
            throw new IllegalStateException("Estado actual desconocido: " + currentStatus);
        }

        statuses.put(id, targetStatus);
    }

    @Override
    public String getStatus(String appointmentId) {
        return appointmentId == null ? null : statuses.get(appointmentId.trim());
    }
}
