package com.autoclean.repository;

import com.autoclean.model.Vehicle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad Vehicle.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class VehicleRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, Vehicle> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza un vehículo. Si no tiene ID, se autogenera con UUID.
     *
     * @param vehicle vehículo a guardar
     * @return vehículo guardado con su ID asignado
     */
    public Vehicle save(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo");
        }
        if (vehicle.getId() == null || vehicle.getId().trim().isEmpty()) {
            vehicle.setId(UUID.randomUUID().toString());
        }
        storage.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    /**
     * Busca un vehículo por su identificador.
     *
     * @param id identificador del vehículo
     * @return vehículo encontrado o null si no existe
     */
    public Vehicle findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todos los vehículos registrados.
     *
     * @return lista de vehículos
     */
    public List<Vehicle> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina un vehículo por su identificador.
     *
     * @param id identificador del vehículo a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    /**
     * Busca los vehículos pertenecientes a un cliente específico.
     *
     * @param customerId identificador del cliente
     * @return lista de vehículos asociados al cliente
     */
    public List<Vehicle> findByCustomerId(String customerId) {
        List<Vehicle> result = new ArrayList<>();
        if (customerId == null) {
            return result;
        }
        for (Vehicle v : storage.values()) {
            if (customerId.equals(v.getCustomerId())) {
                result.add(v);
            }
        }
        return result;
    }
}

