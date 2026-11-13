package com.autoclean.repository;

import com.autoclean.model.CostEstimate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para gestionar las estimaciones de costo persistidas en {@code calculos_costo}.
 */
public interface CostEstimateRepository extends JpaRepository<CostEstimate, UUID> {

    @Override
    @EntityGraph(attributePaths = {"serviceType", "appointment", "lines"})
    Optional<CostEstimate> findById(UUID id);

    @EntityGraph(attributePaths = {"serviceType", "appointment", "lines"})
    List<CostEstimate> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"serviceType", "appointment", "lines"})
    List<CostEstimate> findByAppointmentId(UUID appointmentId);
}

