package com.autoclean.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Representa un cliente registrado en AutoClean propietario de vehículos y citas.
 */
@Entity
@Table(name = "clientes")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "telefono", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Customer() {
    }

    public Customer(String name, String phone, String email) {
        setName(name);
        setPhone(phone);
        setEmail(email);
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
        this.name = ModelValidations.requireNonBlank(name, "El nombre del cliente no puede estar vacío");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = ModelValidations.requireNonBlank(phone, "El teléfono no puede estar vacío");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = ModelValidations.requireNonBlank(email, "El correo no puede estar vacío");
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
        if (!(o instanceof Customer other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}
