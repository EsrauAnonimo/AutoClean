package com.autoclean.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un tipo de servicio de catálogo en AutoClean.
 * Mapeada a la tabla 'tipos_servicio' conforme a la migración V1__init_schema.sql.
 */
@Entity
@Table(name = "tipos_servicio")
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "duracion_min", nullable = false)
    private Integer durationMin;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public ServiceType() {
    }

    /**
     * Constructor completo para inicialización del servicio de catálogo.
     *
     * @param id identificador único del servicio
     * @param name nombre del servicio
     * @param description descripción detallada
     * @param price precio base del servicio
     * @param durationMin duración estimada en minutos
     */
    public ServiceType(UUID id, String name, String description, BigDecimal price, Integer durationMin) {
        this.id = id;
        setName(name);
        setDescription(description);
        setPrice(price);
        setDurationMin(durationMin);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del servicio no puede ser negativo");
        }
        this.price = price;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        if (durationMin == null || durationMin <= 0) {
            throw new IllegalArgumentException("La duración del servicio debe ser mayor a 0 minutos");
        }
        this.durationMin = durationMin;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
