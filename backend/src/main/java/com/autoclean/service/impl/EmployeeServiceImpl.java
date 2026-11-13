package com.autoclean.service.impl;

import com.autoclean.dto.employee.EmployeeRequestDto;
import com.autoclean.dto.employee.EmployeeResponseDto;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.Employee;
import com.autoclean.repository.EmployeeRepository;
import com.autoclean.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la administración del personal de AutoClean.
 * Replica las validaciones y reglas del sistema legacy (EmployeeServiceImpl / Employee.java).
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Inyección por constructor del repositorio de empleados.
     *
     * @param employeeRepository repositorio Spring Data JPA
     */
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        if (employeeRepository == null) {
            throw new IllegalArgumentException("El repositorio de empleados no puede ser nulo");
        }
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDto create(EmployeeRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La información del empleado no puede ser nula");
        }

        // Replica validaciones de negocio de legacy-javafx/.../model/Employee.java:43,54
        Employee employee = new Employee(
                request.name(),
                request.role()
        );

        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Override
    public EmployeeResponseDto update(UUID id, EmployeeRequestDto request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del empleado no puede ser nulo");
        }
        if (request == null) {
            throw new IllegalArgumentException("La información del empleado a actualizar no puede ser nula");
        }

        // Validación de existencia previa (EmployeeServiceImpl.java:46-48)
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un empleado con el identificador: " + id));

        existing.setName(request.name());
        existing.setRole(request.role());

        Employee updated = employeeRepository.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del empleado no puede ser nulo");
        }
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un empleado con el identificador: " + id));

        employeeRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del empleado no puede ser nulo");
        }
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un empleado con el identificador: " + id));

        return toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private EmployeeResponseDto toDto(Employee entity) {
        return new EmployeeResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }
}
