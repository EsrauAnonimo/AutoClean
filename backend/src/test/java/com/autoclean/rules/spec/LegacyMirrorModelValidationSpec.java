package com.autoclean.rules.spec;

/**
 * Stub espejo de las validaciones en {@code Customer}, {@code Vehicle} y {@code ServiceType} del legacy.
 */
public class LegacyMirrorModelValidationSpec implements ModelValidationSpec {

    @Override
    public void validateCustomer(String name, String phone, String email) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (isBlank(phone)) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        if (isBlank(email)) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
    }

    @Override
    public void validateVehicle(String customerId, String plate, String brand, String model, int year) {
        if (isBlank(customerId)) {
            throw new IllegalArgumentException("El identificador del cliente no puede estar vacío");
        }
        if (isBlank(plate)) {
            throw new IllegalArgumentException("La placa no puede estar vacía");
        }
        if (isBlank(brand)) {
            throw new IllegalArgumentException("La marca no puede estar vacía");
        }
        if (isBlank(model)) {
            throw new IllegalArgumentException("El modelo no puede estar vacío");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("El año debe ser válido (entre 1900 y 2100)");
        }
    }

    @Override
    public void validateServiceType(String name, double price) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
        }
        if (price < 0) {
            throw new IllegalArgumentException("El precio del servicio no puede ser negativo");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
