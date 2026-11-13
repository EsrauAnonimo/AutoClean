package com.autoclean.service;

import com.autoclean.dto.servicetype.ServiceTypeRequestDto;
import com.autoclean.dto.servicetype.ServiceTypeResponseDto;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la administración del catálogo de tipos de servicio.
 */
public interface ServiceTypeService {

    /**
     * Registra un nuevo tipo de servicio en el catálogo.
     *
     * @param request datos del servicio a crear
     * @return DTO del servicio creado
     */
    ServiceTypeResponseDto create(ServiceTypeRequestDto request);

    /**
     * Actualiza la información de un tipo de servicio existente.
     *
     * @param id identificador único del servicio a actualizar
     * @param request datos actualizados
     * @return DTO del servicio actualizado
     */
    ServiceTypeResponseDto update(UUID id, ServiceTypeRequestDto request);

    /**
     * Elimina un tipo de servicio por su identificador.
     *
     * @param id identificador del servicio a eliminar
     */
    void delete(UUID id);

    /**
     * Busca un tipo de servicio por su identificador.
     *
     * @param id identificador del servicio
     * @return DTO del servicio encontrado
     */
    ServiceTypeResponseDto findById(UUID id);

    /**
     * Obtiene el listado completo de tipos de servicio disponibles.
     *
     * @return lista de DTOs de servicios
     */
    List<ServiceTypeResponseDto> findAll();
}
