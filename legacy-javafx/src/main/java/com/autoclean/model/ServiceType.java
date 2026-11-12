package com.autoclean.model;

/**
 * Representa un tipo de servicio de embellecimiento ofrecido (ej. Lavado general, Porcelanizado).
 */
public class ServiceType {

    private String id;
    private String name;
    private String description;
    private double price;

    /**
     * Constructor por defecto.
     */
    public ServiceType() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del servicio
     * @param name nombre del servicio
     * @param description descripción del servicio
     * @param price precio base del servicio
     */
    public ServiceType(String id, String name, String description, double price) {
        setId(id);
        setName(name);
        setDescription(description);
        setPrice(price);
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("El precio del servicio no puede ser negativo");
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return name != null ? name + " ($" + price + ")" : (id != null ? id : "");
    }
}
