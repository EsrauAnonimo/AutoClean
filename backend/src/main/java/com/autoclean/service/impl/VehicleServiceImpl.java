package com.autoclean.service.impl;

import com.autoclean.dto.vehicle.VehicleRequest;
import com.autoclean.dto.vehicle.VehicleResponse;
import com.autoclean.model.Customer;
import com.autoclean.model.Vehicle;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.VehicleRepository;
import com.autoclean.service.VehicleService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la gestión de vehículos.
 */
@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    /**
     * Inyección por constructor de los repositorios requeridos.
     *
     * @param vehicleRepository repositorio JPA de vehículos
     * @param customerRepository repositorio JPA de clientes para validar pertenencia
     */
    public VehicleServiceImpl(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo");
        }
        Customer customer = findRealCustomer(request.customerId());
        Vehicle vehicle = new Vehicle(customer, request.plate(), request.brand(), request.model(), request.year());
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del vehículo es requerido para actualizar");
        }
        if (request == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo");
        }
        Vehicle existing = findExisting(id);
        Customer customer = findRealCustomer(request.customerId());

        existing.setCustomer(customer);
        existing.setPlate(request.plate());
        existing.setBrand(request.brand());
        existing.setModel(request.model());
        existing.setYear(request.year());
        return VehicleResponse.from(vehicleRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del vehículo no puede ser nulo");
        }
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un vehículo con el identificador: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del vehículo no puede ser nulo");
        }
        return VehicleResponse.from(findExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        return vehicleRepository.findByCustomerId(customerId).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    private Vehicle findExisting(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un vehículo con el identificador: " + id));
    }

    private Customer findRealCustomer(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un cliente con el identificador: " + customerId));
    }
}
