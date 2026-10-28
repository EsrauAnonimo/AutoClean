package com.autoclean.contracts;

import com.autoclean.model.Vehicle;
import java.util.List;

/**
 * Contrato de servicio para la administración de vehículos.
 */
public interface VehicleService {

    /**
     * Registra un nuevo vehículo.
     */
    Vehicle create(Vehicle vehicle);

    /**
     * Actualiza la información de un vehículo existente.
     */
    Vehicle update(Vehicle vehicle);

    /**
     * Elimina un vehículo por su identificador.
     */
    void delete(String id);

    /**
     * Busca un vehículo por su identificador.
     */
    Vehicle findById(String id);

    /**
     * Obtiene la lista completa de vehículos.
     */
    List<Vehicle> findAll();

    /**
     * Busca los vehículos asociados a un cliente en particular.
     */
    List<Vehicle> findByCustomerId(String customerId);
}
