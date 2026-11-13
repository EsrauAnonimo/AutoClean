package com.autoclean.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Representa una línea individual de gasto dentro de una estimación de costo.
 */
@Entity
@Table(name = "lineas_costo")
public class CostLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calculo_id", nullable = false)
    private CostEstimate costEstimate;

    @Column(name = "concepto", nullable = false)
    private String concept;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public CostLine() {
    }

    public CostLine(String concept, BigDecimal amount) {
        setConcept(concept);
        setAmount(amount);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CostEstimate getCostEstimate() {
        return costEstimate;
    }

    public void setCostEstimate(CostEstimate costEstimate) {
        this.costEstimate = ModelValidations.requireNonNull(costEstimate, "El cálculo padre no puede ser nulo");
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = ModelValidations.requireNonBlank(concept, "El concepto no puede estar vacío");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = ModelValidations.requireNonNegative(amount, "El monto no puede ser negativo");
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
        if (!(o instanceof CostLine other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}
