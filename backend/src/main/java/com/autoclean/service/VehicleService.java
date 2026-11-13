package com.autoclean.service;

import com.autoclean.dto.vehicle.VehicleRequest;
import com.autoclean.dto.vehicle.VehicleResponse;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para las operaciones CRUD y consultas de vehículos.
 */
public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    VehicleResponse update(UUID id, VehicleRequest request);

    void delete(UUID id);

    VehicleResponse findById(UUID id);

    List<VehicleResponse> findAll();

    List<VehicleResponse> findByCustomerId(UUID customerId);
}
