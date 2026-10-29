package com.autoclean.service.stub;

import com.autoclean.contracts.EmployeeService;
import com.autoclean.model.Employee;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para EmployeeService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class EmployeeServiceStub implements EmployeeService {

    @Override
    public Employee create(Employee employee) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Employee update(Employee employee) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Employee findById(String id) {
        return null;
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>();
    }
}
