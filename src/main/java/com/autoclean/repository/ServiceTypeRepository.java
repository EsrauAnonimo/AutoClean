package com.autoclean.repository;

import com.autoclean.model.ServiceType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad ServiceType.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class ServiceTypeRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, ServiceType> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza un tipo de servicio. Si no tiene ID, se autogenera con UUID.
     *
     * @param serviceType tipo de servicio a guardar
     * @return tipo de servicio guardado con su ID asignado
     */
    public ServiceType save(ServiceType serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("El tipo de servicio no puede ser nulo");
        }
        if (serviceType.getId() == null || serviceType.getId().trim().isEmpty()) {
            serviceType.setId(UUID.randomUUID().toString());
        }
        storage.put(serviceType.getId(), serviceType);
        return serviceType;
    }

    /**
     * Busca un tipo de servicio por su identificador.
     *
     * @param id identificador del tipo de servicio
     * @return tipo de servicio encontrado o null si no existe
     */
    public ServiceType findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todos los tipos de servicios registrados.
     *
     * @return lista de tipos de servicio
     */
    public List<ServiceType> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina un tipo de servicio por su identificador.
     *
     * @param id identificador del tipo de servicio a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}

