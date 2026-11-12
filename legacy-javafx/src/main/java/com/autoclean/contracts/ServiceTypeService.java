package com.autoclean.contracts;

import com.autoclean.model.ServiceType;
import java.util.List;

/**
 * Contrato de servicio para el catálogo de tipos de servicio automotriz.
 */
public interface ServiceTypeService {

    /**
     * Registra un nuevo tipo de servicio.
     */
    ServiceType create(ServiceType serviceType);

    /**
     * Actualiza un tipo de servicio existente.
     */
    ServiceType update(ServiceType serviceType);

    /**
     * Elimina un tipo de servicio por su identificador.
     */
    void delete(String id);

    /**
     * Busca un tipo de servicio por su identificador.
     */
    ServiceType findById(String id);

    /**
     * Obtiene el listado de todos los tipos de servicios disponibles.
     */
    List<ServiceType> findAll();
}
