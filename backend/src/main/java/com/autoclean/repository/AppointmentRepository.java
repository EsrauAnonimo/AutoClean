package com.autoclean.repository;

import com.autoclean.model.Appointment;
import com.autoclean.model.AppointmentStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Appointment}.
 * Provee operaciones CRUD y consultas sobre la tabla {@code citas}.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Override
    @EntityGraph(attributePaths = {"customer", "vehicle", "employee", "serviceType"})
    List<Appointment> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "vehicle", "employee", "serviceType"})
    Optional<Appointment> findById(UUID id);

    @EntityGraph(attributePaths = {"customer", "vehicle", "employee", "serviceType"})
    List<Appointment> findByCustomerId(UUID customerId);

    @EntityGraph(attributePaths = {"customer", "vehicle", "employee", "serviceType"})
    List<Appointment> findByStatus(AppointmentStatus status);

    @EntityGraph(attributePaths = {"customer", "vehicle", "employee", "serviceType"})
    List<Appointment> findByStatusIn(Collection<AppointmentStatus> statuses);
}

