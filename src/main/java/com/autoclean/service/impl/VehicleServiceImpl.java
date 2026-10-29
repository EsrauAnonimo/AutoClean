package com.autoclean.service.impl;

import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Vehicle;
import com.autoclean.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de vehículos.
 */
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;

    /**
     * Constructor por defecto utilizando el repositorio en memoria estándar.
     */
    public VehicleServiceImpl() {
        this(new VehicleRepository());
    }

    /**
     * Constructor con inyección de repositorio.
     *
     * @param repository repositorio de vehículos a utilizar
     */
    public VehicleServiceImpl(VehicleRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio de vehículos no puede ser nulo");
        }
        this.repository = repository;
    }

    @Override
    public Vehicle create(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo");
        }
        return repository.save(vehicle);
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        if (vehicle == null || vehicle.getId() == null || vehicle.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El vehículo y su identificador son requeridos para actualizar");
        }
        Vehicle existing = repository.findById(vehicle.getId());
        if (existing == null) {
            throw new IllegalArgumentException("No existe un vehículo con el identificador: " + vehicle.getId());
        }
        return repository.save(vehicle);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del vehículo no puede estar vacío");
        }
        repository.delete(id);
    }

    @Override
    public Vehicle findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Vehicle> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findByCustomerId(customerId.trim());
    }
}

