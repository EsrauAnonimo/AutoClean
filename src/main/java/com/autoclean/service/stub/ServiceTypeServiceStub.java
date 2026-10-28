package com.autoclean.service.stub;

import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.model.ServiceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para ServiceTypeService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class ServiceTypeServiceStub implements ServiceTypeService {

    @Override
    public ServiceType create(ServiceType serviceType) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public ServiceType update(ServiceType serviceType) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public ServiceType findById(String id) {
        return null;
    }

    @Override
    public List<ServiceType> findAll() {
        return new ArrayList<>();
    }
}
