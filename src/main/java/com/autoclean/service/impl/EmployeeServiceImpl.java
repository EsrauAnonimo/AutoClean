package com.autoclean.service.impl;

import com.autoclean.contracts.EmployeeService;
import com.autoclean.model.Employee;
import com.autoclean.repository.EmployeeRepository;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de empleados y personal.
 */
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    /**
     * Constructor por defecto utilizando el repositorio en memoria estándar.
     */
    public EmployeeServiceImpl() {
        this(new EmployeeRepository());
    }

    /**
     * Constructor con inyección de repositorio.
     *
     * @param repository repositorio de empleados a utilizar
     */
    public EmployeeServiceImpl(EmployeeRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio de empleados no puede ser nulo");
        }
        this.repository = repository;
    }

    @Override
    public Employee create(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        return repository.save(employee);
    }

    @Override
    public Employee update(Employee employee) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El empleado y su identificador son requeridos para actualizar");
        }
        Employee existing = repository.findById(employee.getId());
        if (existing == null) {
            throw new IllegalArgumentException("No existe un empleado con el identificador: " + employee.getId());
        }
        return repository.save(employee);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del empleado no puede estar vacío");
        }
        repository.delete(id);
    }

    @Override
    public Employee findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<Employee> findAll() {
        return repository.findAll();
    }
}

