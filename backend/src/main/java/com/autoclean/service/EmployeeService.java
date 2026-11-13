package com.autoclean.service;

import com.autoclean.dto.employee.EmployeeRequestDto;
import com.autoclean.dto.employee.EmployeeResponseDto;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la administración del personal y empleados.
 */
public interface EmployeeService {

    /**
     * Registra un nuevo empleado en el sistema.
     *
     * @param request datos del empleado a registrar
     * @return DTO del empleado registrado
     */
    EmployeeResponseDto create(EmployeeRequestDto request);

    /**
     * Actualiza la información de un empleado existente.
     *
     * @param id identificador único del empleado a actualizar
     * @param request datos actualizados
     * @return DTO del empleado actualizado
     */
    EmployeeResponseDto update(UUID id, EmployeeRequestDto request);

    /**
     * Elimina un empleado por su identificador.
     *
     * @param id identificador del empleado a eliminar
     */
    void delete(UUID id);

    /**
     * Busca un empleado por su identificador.
     *
     * @param id identificador del empleado
     * @return DTO del empleado encontrado
     */
    EmployeeResponseDto findById(UUID id);

    /**
     * Obtiene el listado completo de todos los empleados.
     *
     * @return lista de DTOs de empleados
     */
    List<EmployeeResponseDto> findAll();
}
