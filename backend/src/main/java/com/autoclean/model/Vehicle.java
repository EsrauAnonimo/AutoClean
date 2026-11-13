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
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Representa un vehículo asociado a un cliente que recibe servicios en el centro.
 */
@Entity
@Table(name = "vehiculos")
public class Vehicle {

    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer customer;

    @Column(name = "placa", nullable = false)
    private String plate;

    @Column(name = "marca", nullable = false)
    private String brand;

    @Column(name = "modelo", nullable = false)
    private String model;

    @Column(name = "anio", nullable = false)
    private Integer year;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Vehicle() {
    }

    public Vehicle(Customer customer, String plate, String brand, String model, Integer year) {
        setCustomer(customer);
        setPlate(plate);
        setBrand(brand);
        setModel(model);
        setYear(year);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = ModelValidations.requireNonNull(customer, "El cliente del vehículo no puede ser nulo");
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = ModelValidations.requireNonBlank(plate, "La placa no puede estar vacía").toUpperCase();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = ModelValidations.requireNonBlank(brand, "La marca no puede estar vacía");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = ModelValidations.requireNonBlank(model, "El modelo no puede estar vacío");
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        if (year == null || year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("El año debe ser válido (entre 1900 y 2100)");
        }
        this.year = year;
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
        if (!(o instanceof Vehicle other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}
