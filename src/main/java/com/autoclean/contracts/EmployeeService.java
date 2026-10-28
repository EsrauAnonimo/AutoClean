package com.autoclean.contracts;

import com.autoclean.model.Employee;
import java.util.List;

/**
 * Contrato de servicio para la administración del personal y empleados.
 */
public interface EmployeeService {

    /**
     * Registra un nuevo empleado.
     */
    Employee create(Employee employee);

    /**
     * Actualiza la información de un empleado existente.
     */
    Employee update(Employee employee);

    /**
     * Elimina un empleado por su identificador.
     */
    void delete(String id);

    /**
     * Busca un empleado por su identificador.
     */
    Employee findById(String id);

    /**
     * Obtiene el listado de todos los empleados.
     */
    List<Employee> findAll();
}
