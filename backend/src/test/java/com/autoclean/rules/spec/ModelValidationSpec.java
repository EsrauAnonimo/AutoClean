package com.autoclean.rules.spec;

/**
 * Contrato espejo para las validaciones de entidades de dominio del sistema legacy.
 */
public interface ModelValidationSpec {

    /**
     * Valida los datos obligatorios de un cliente.
     *
     * @param name nombre completo
     * @param phone teléfono de contacto
     * @param email correo electrónico
     */
    void validateCustomer(String name, String phone, String email);

    /**
     * Valida los datos obligatorios y rango de año de un vehículo.
     *
     * @param customerId identificador del propietario
     * @param plate placa del vehículo
     * @param brand marca del vehículo
     * @param model modelo del vehículo
     * @param year año de fabricación en [1900, 2100]
     */
    void validateVehicle(String customerId, String plate, String brand, String model, int year);

    /**
     * Valida los datos obligatorios y precio no negativo de un tipo de servicio.
     *
     * @param name nombre del servicio
     * @param price precio base (>= 0)
     */
    void validateServiceType(String name, double price);
}
