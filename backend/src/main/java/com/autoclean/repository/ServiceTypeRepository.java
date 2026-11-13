package com.autoclean.repository;

import com.autoclean.model.ServiceType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad ServiceType.
 * Provee operaciones CRUD y paginación sobre la tabla 'tipos_servicio'.
 */
@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {
}
