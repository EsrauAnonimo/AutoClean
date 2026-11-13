package com.autoclean.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Representa un cálculo o estimación de costo para un tipo de servicio y opcionalmente una cita.
 */
@Entity
@Table(name = "calculos_costo")
public class CostEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private ServiceType serviceType;

    // Cita opcional: las simulaciones desde catálogo no requieren cita asociada
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cita_id")
    private Appointment appointment;

    @OneToMany(mappedBy = "costEstimate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CostLine> lines = new ArrayList<>();

    @Column(name = "costo_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Puede ser negativa cuando costo_estimado supera al precio de catálogo
    @Column(name = "diferencia", nullable = false, precision = 12, scale = 2)
    private BigDecimal difference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public CostEstimate() {
    }

    public CostEstimate(ServiceType serviceType, Appointment appointment, List<CostLine> lines,
                        BigDecimal estimatedCost, BigDecimal price, BigDecimal difference) {
        setServiceType(serviceType);
        setAppointment(appointment);
        setLines(lines);
        setEstimatedCost(estimatedCost);
        setPrice(price);
        setDifference(difference);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = ModelValidations.requireNonNull(serviceType, "El tipo de servicio no puede ser nulo");
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public List<CostLine> getLines() {
        return lines;
    }

    public void setLines(List<CostLine> lines) {
        this.lines.clear();
        if (lines != null) {
            for (CostLine line : lines) {
                if (line != null) {
                    addLine(line);
                }
            }
        }
    }

    /**
     * Agrega una línea de costo manteniendo sincronizada la relación bidireccional.
     *
     * @param line línea de costo a asociar
     */
    public void addLine(CostLine line) {
        CostLine validLine = ModelValidations.requireNonNull(line, "La línea de costo no puede ser nula");
        validLine.setCostEstimate(this);
        this.lines.add(validLine);
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = ModelValidations.requireNonNegative(estimatedCost, "El costo estimado no puede ser negativo");
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = ModelValidations.requireNonNegative(price, "El precio no puede ser negativo");
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = ModelValidations.requireNonNull(difference, "La diferencia no puede ser nula");
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CostEstimate other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}
