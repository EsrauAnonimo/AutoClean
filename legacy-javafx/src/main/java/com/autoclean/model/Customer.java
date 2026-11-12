package com.autoclean.model;

/**
 * Representa un cliente del centro de embellecimiento automotriz.
 */
public class Customer {

    private String id;
    private String name;
    private String phone;
    private String email;

    /**
     * Constructor por defecto.
     */
    public Customer() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del cliente
     * @param name nombre completo del cliente
     * @param phone número de teléfono
     * @param email correo electrónico
     */
    public Customer(String id, String name, String phone, String email) {
        setId(id);
        setName(name);
        setPhone(phone);
        setEmail(email);
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
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        this.phone = phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        this.email = email.trim();
    }

    @Override
    public String toString() {
        return name != null ? name + " (" + (phone != null ? phone : "") + ")" : (id != null ? id : "");
    }
}
