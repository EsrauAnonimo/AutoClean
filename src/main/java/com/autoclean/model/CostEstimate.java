package com.autoclean.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la estimación de costos asociada a un servicio o turno.
 */
public class CostEstimate {

    private String id;
    private String serviceTypeId;
    private String appointmentId;
    private List<CostLine> lines = new ArrayList<>();
    private double estimatedCost;
    private double price;
    private double difference;

    /**
     * Constructor por defecto.
     */
    public CostEstimate() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del cálculo
     * @param serviceTypeId identificador del tipo de servicio
     * @param appointmentId identificador del turno asociado (opcional)
     * @param lines lista de líneas de costo
     * @param estimatedCost costo total estimado
     * @param price precio cobrado al cliente
     * @param difference diferencia (precio - costo estimado)
     */
    public CostEstimate(String id, String serviceTypeId, String appointmentId,
                        List<CostLine> lines, double estimatedCost, double price, double difference) {
        setId(id);
        setServiceTypeId(serviceTypeId);
        setAppointmentId(appointmentId);
        setLines(lines);
        setEstimatedCost(estimatedCost);
        setPrice(price);
        setDifference(difference);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId != null ? appointmentId.trim() : null;
    }

    public List<CostLine> getLines() {
        return lines;
    }

    public void setLines(List<CostLine> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        if (estimatedCost < 0) {
            throw new IllegalArgumentException("El costo estimado no puede ser negativo");
        }
        this.estimatedCost = estimatedCost;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.price = price;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    @Override
    public String toString() {
        return "CostEstimate [id=" + id + ", cost=" + estimatedCost + ", price=" + price + ", diff=" + difference + "]";
    }
}
