package com.autoclean.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un empleado del centro de detailing AutoClean.
 * Mapeada a la tabla 'empleados' conforme a la migración V1__init_schema.sql.
 */
@Entity
@Table(name = "empleados")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "cargo", nullable = false)
    private String role;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Employee() {
    }

    /**
     * Constructor completo para inicialización del empleado.
     *
     * @param id identificador único del empleado
     * @param name nombre completo del empleado
     * @param role cargo o función que desempeña en el lavadero
     */
    public Employee(UUID id, String name, String role) {
        this.id = id;
        setName(name);
        setRole(role);
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
            throw new IllegalArgumentException("El nombre del empleado no puede estar vacío");
        }
        this.name = name.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("El rol del empleado no puede estar vacío");
        }
        this.role = role.trim();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
