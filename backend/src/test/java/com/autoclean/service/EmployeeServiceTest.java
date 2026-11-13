package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.employee.EmployeeRequestDto;
import com.autoclean.dto.employee.EmployeeResponseDto;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.Employee;
import com.autoclean.repository.EmployeeRepository;
import com.autoclean.service.impl.EmployeeServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para la lógica de negocio de EmployeeService.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository);
    }

    @Test
    @DisplayName("Debe crear un empleado exitosamente")
    void shouldCreateEmployeeSuccessfully() {
        UUID generatedId = UUID.randomUUID();
        EmployeeRequestDto request = new EmployeeRequestDto("Juan Pérez", "Detailer Especialista");

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(generatedId);
            return emp;
        });

        EmployeeResponseDto response = employeeService.create(request);

        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("Juan Pérez", response.name());
        assertEquals("Detailer Especialista", response.role());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debe rechazar nombre vacío al crear empleado")
    void shouldRejectBlankNameOnCreate() {
        EmployeeRequestDto request = new EmployeeRequestDto("   ", "Lavador");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar rol vacío al crear empleado")
    void shouldRejectBlankRoleOnCreate() {
        EmployeeRequestDto request = new EmployeeRequestDto("Carlos Ruiz", "");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar un empleado existente")
    void shouldUpdateEmployeeSuccessfully() {
        UUID id = UUID.randomUUID();
        Employee existing = new Employee("Carlos Ruiz", "Operario");
        existing.setId(id);
        EmployeeRequestDto updateRequest = new EmployeeRequestDto("Carlos Ruiz Gómez", "Líder de Lavado");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDto updated = employeeService.update(id, updateRequest);

        assertEquals("Carlos Ruiz Gómez", updated.name());
        assertEquals("Líder de Lavado", updated.role());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al actualizar empleado inexistente")
    void shouldThrowWhenUpdatingNonExistentEmployee() {
        UUID nonExistentId = UUID.randomUUID();
        EmployeeRequestDto request = new EmployeeRequestDto("Fantasma", "Rol");

        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(nonExistentId, request));
    }

    @Test
    @DisplayName("Debe eliminar un empleado existente")
    void shouldDeleteEmployeeSuccessfully() {
        UUID id = UUID.randomUUID();
        Employee existing = new Employee("Pedro Silva", "Pulidor");
        existing.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existing));

        employeeService.delete(id);

        verify(employeeRepository).delete(existing);
    }

    @Test
    @DisplayName("Debe buscar un empleado por ID")
    void shouldFindEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee existing = new Employee("Laura Vélez", "Supervisora");
        existing.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existing));

        EmployeeResponseDto found = employeeService.findById(id);

        assertNotNull(found);
        assertEquals(id, found.id());
        assertEquals("Laura Vélez", found.name());
        assertEquals("Supervisora", found.role());
    }

    @Test
    @DisplayName("Debe listar todos los empleados")
    void shouldFindAllEmployees() {
        Employee e1 = new Employee("E1", "R1");
        e1.setId(UUID.randomUUID());
        Employee e2 = new Employee("E2", "R2");
        e2.setId(UUID.randomUUID());
        List<Employee> list = List.of(e1, e2);

        when(employeeRepository.findAll()).thenReturn(list);

        List<EmployeeResponseDto> all = employeeService.findAll();

        assertEquals(2, all.size());
    }
}
