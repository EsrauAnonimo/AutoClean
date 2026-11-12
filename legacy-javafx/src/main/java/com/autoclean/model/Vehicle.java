package com.autoclean.model;

/**
 * Representa un vehículo asociado a un cliente.
 */
public class Vehicle {

    private String id;
    private String customerId;
    private String plate;
    private String brand;
    private String model;
    private int year;

    /**
     * Constructor por defecto.
     */
    public Vehicle() {
    }

    /**
     * Constructor completo.
     *
     * @param id identificador único del vehículo
     * @param customerId identificador del cliente propietario
     * @param plate placa o matrícula del vehículo
     * @param brand marca del vehículo
     * @param model modelo o línea del vehículo
     * @param year año de fabricación
     */
    public Vehicle(String id, String customerId, String plate, String brand, String model, int year) {
        setId(id);
        setCustomerId(customerId);
        setPlate(plate);
        setBrand(brand);
        setModel(model);
        setYear(year);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del cliente no puede estar vacío");
        }
        this.customerId = customerId.trim();
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía");
        }
        this.plate = plate.trim().toUpperCase();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("La marca no puede estar vacía");
        }
        this.brand = brand.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo no puede estar vacío");
        }
        this.model = model.trim();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("El año debe ser válido (entre 1900 y 2100)");
        }
        this.year = year;
    }

    @Override
    public String toString() {
        return plate != null ? plate + " - " + brand + " " + model : (id != null ? id : "");
    }
}
