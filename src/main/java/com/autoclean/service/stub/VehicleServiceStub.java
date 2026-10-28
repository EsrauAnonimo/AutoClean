package com.autoclean.service.stub;

import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para VehicleService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class VehicleServiceStub implements VehicleService {

    @Override
    public Vehicle create(Vehicle vehicle) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Vehicle findById(String id) {
        return null;
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>();
    }

    @Override
    public List<Vehicle> findByCustomerId(String customerId) {
        return new ArrayList<>();
    }
}
