package com.autoclean.service.impl;

import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.ServiceTypeRepository;
import java.util.List;

/**
 * Implementación de la lógica de negocio para el catálogo de tipos de servicio.
 */
public class ServiceCatalogServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository repository;

    /**
     * Constructor por defecto utilizando el repositorio en memoria estándar.
     */
    public ServiceCatalogServiceImpl() {
        this(new ServiceTypeRepository());
    }

    /**
     * Constructor con inyección de repositorio.
     *
     * @param repository repositorio de tipos de servicio a utilizar
     */
    public ServiceCatalogServiceImpl(ServiceTypeRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio de tipos de servicio no puede ser nulo");
        }
        this.repository = repository;
    }

    @Override
    public ServiceType create(ServiceType serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("El tipo de servicio no puede ser nulo");
        }
        return repository.save(serviceType);
    }

    @Override
    public ServiceType update(ServiceType serviceType) {
        if (serviceType == null || serviceType.getId() == null || serviceType.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio y su identificador son requeridos para actualizar");
        }
        ServiceType existing = repository.findById(serviceType.getId());
        if (existing == null) {
            throw new IllegalArgumentException("No existe un tipo de servicio con el identificador: " + serviceType.getId());
        }
        return repository.save(serviceType);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del tipo de servicio no puede estar vacío");
        }
        repository.delete(id);
    }

    @Override
    public ServiceType findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<ServiceType> findAll() {
        return repository.findAll();
    }
}

