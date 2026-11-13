package com.autoclean.repository;

import com.autoclean.model.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la gestión de persistencia de vehículos.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByCustomerId(UUID customerId);

    Optional<Vehicle> findByPlateIgnoreCase(String plate);
}
