package com.autoclean.model;

/**
 * Representa un empleado del centro de embellecimiento.
 */
public class Employee {

    private String id;
    private String name;
    private String role;

    /**
     * Constructor por defecto.
     */
    public Employee() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del empleado
     * @param name nombre completo del empleado
     * @param role cargo o rol desempeñado
     */
    public Employee(String id, String name, String role) {
        setId(id);
        setName(name);
        setRole(role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        return name != null ? name + " - " + role : (id != null ? id : "");
    }
}
