package com.autoclean.repository;

import com.autoclean.model.Employee;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Employee.
 * Provee operaciones CRUD y consultas sobre la tabla 'empleados'.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
