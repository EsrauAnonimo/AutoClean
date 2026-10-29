package com.autoclean.repository;

import com.autoclean.model.Employee;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad Employee.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class EmployeeRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, Employee> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza un empleado. Si no tiene ID, se autogenera con UUID.
     *
     * @param employee empleado a guardar
     * @return empleado guardado con su ID asignado
     */
    public Employee save(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (employee.getId() == null || employee.getId().trim().isEmpty()) {
            employee.setId(UUID.randomUUID().toString());
        }
        storage.put(employee.getId(), employee);
        return employee;
    }

    /**
     * Busca un empleado por su identificador.
     *
     * @param id identificador del empleado
     * @return empleado encontrado o null si no existe
     */
    public Employee findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todos los empleados registrados.
     *
     * @return lista de empleados
     */
    public List<Employee> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina un empleado por su identificador.
     *
     * @param id identificador del empleado a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}

