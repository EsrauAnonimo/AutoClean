package com.autoclean.model;

import java.time.LocalDateTime;

/**
 * Representa un turno de servicio programado o en ejecución.
 */
public class Appointment {

    private String id;
    private String customerId;
    private String vehicleId;
    private String serviceTypeId;
    private String employeeId;
    private LocalDateTime dateTime;
    private String status; // PENDING, IN_PROGRESS, FINISHED, CANCELLED

    /**
     * Constructor por defecto.
     */
    public Appointment() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del turno
     * @param customerId identificador del cliente
     * @param vehicleId identificador del vehículo
     * @param serviceTypeId identificador del servicio
     * @param employeeId identificador del empleado asignado
     * @param dateTime fecha y hora del turno
     * @param status estado actual (PENDING, IN_PROGRESS, FINISHED, CANCELLED)
     */
    public Appointment(String id, String customerId, String vehicleId, String serviceTypeId,
                       String employeeId, LocalDateTime dateTime, String status) {
        setId(id);
        setCustomerId(customerId);
        setVehicleId(vehicleId);
        setServiceTypeId(serviceTypeId);
        setEmployeeId(employeeId);
        setDateTime(dateTime);
        setStatus(status);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente no puede estar vacío");
        }
        this.customerId = customerId.trim();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El vehículo no puede estar vacío");
        }
        this.vehicleId = vehicleId.trim();
    }

    public String getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(String serviceTypeId) {
        if (serviceTypeId == null || serviceTypeId.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio no puede estar vacío");
        }
        this.serviceTypeId = serviceTypeId.trim();
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId != null ? employeeId.trim() : "";
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("La fecha y hora del turno no pueden ser nulas");
        }
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado del turno no puede estar vacío");
        }
        this.status = status.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return "Turno #" + id + " [" + status + "] " + (dateTime != null ? dateTime.toString() : "");
    }
}
