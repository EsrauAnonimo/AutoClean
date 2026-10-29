package com.autoclean.service.impl;

import com.autoclean.repository.ServiceTypeRepository;

/**
 * Alias de ServiceCatalogServiceImpl para mantener consistencia con el contrato ServiceTypeService.
 */
public class ServiceTypeServiceImpl extends ServiceCatalogServiceImpl {

    public ServiceTypeServiceImpl() {
        super();
    }

    public ServiceTypeServiceImpl(ServiceTypeRepository repository) {
        super(repository);
    }
}

