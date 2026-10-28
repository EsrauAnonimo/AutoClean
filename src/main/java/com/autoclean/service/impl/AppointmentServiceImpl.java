package com.autoclean.service.impl;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.model.Appointment;
import com.autoclean.repository.AppointmentRepository;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de turnos de servicio automotriz.
 */
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repository;

    /**
     * Constructor por defecto utilizando el repositorio en memoria estándar.
     */
    public AppointmentServiceImpl() {
        this(new AppointmentRepository());
    }

    /**
     * Constructor con inyección de repositorio.
     *
     * @param repository repositorio de turnos a utilizar
     */
    public AppointmentServiceImpl(AppointmentRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio de turnos no puede ser nulo");
        }
        this.repository = repository;
    }

    @Override
    public Appointment create(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("PENDING");
        }
        return repository.save(appointment);
    }

    @Override
    public Appointment update(Appointment appointment) {
        if (appointment == null || appointment.getId() == null || appointment.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El turno y su identificador son requeridos para actualizar");
        }
        Appointment existing = repository.findById(appointment.getId());
        if (existing == null) {
            throw new IllegalArgumentException("No existe un turno con el identificador: " + appointment.getId());
        }
        return repository.save(appointment);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        repository.delete(id);
    }

    @Override
    public Appointment findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<Appointment> findAll() {
        return repository.findAll();
    }

    @Override
    public void updateStatus(String appointmentId, String newStatus) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío");
        }

        Appointment appointment = repository.findById(appointmentId.trim());
        if (appointment == null) {
            throw new IllegalArgumentException("No se encontró el turno con identificador: " + appointmentId);
        }

        String currentStatus = appointment.getStatus() != null ? appointment.getStatus().trim().toUpperCase() : "PENDING";
        String targetStatus = newStatus.trim().toUpperCase();

        // Reglas de negocio para la máquina de estados de turnos:
        // 1. PENDING solo puede transicionar a IN_PROGRESS o CANCELLED.
        // 2. IN_PROGRESS solo puede transicionar a FINISHED o CANCELLED.
        // 3. FINISHED y CANCELLED son estados finales e inmutables (no admiten más transiciones).
        if ("PENDING".equals(currentStatus)) {
            if (!"IN_PROGRESS".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                throw new IllegalStateException("Transición inválida: desde PENDING solo se permite IN_PROGRESS o CANCELLED, se intentó: " + targetStatus);
            }
        } else if ("IN_PROGRESS".equals(currentStatus)) {
            if (!"FINISHED".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                throw new IllegalStateException("Transición inválida: desde IN_PROGRESS solo se permite FINISHED o CANCELLED, se intentó: " + targetStatus);
            }
        } else if ("FINISHED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Transición inválida: el turno ya se encuentra en estado final (" + currentStatus + ") y no admite cambios");
        } else {
            throw new IllegalStateException("Estado actual desconocido: " + currentStatus);
        }

        appointment.setStatus(targetStatus);
        repository.save(appointment);
    }
}
