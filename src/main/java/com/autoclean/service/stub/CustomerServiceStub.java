package com.autoclean.service.stub;

import com.autoclean.contracts.CustomerService;
import com.autoclean.model.Customer;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para CustomerService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class CustomerServiceStub implements CustomerService {

    @Override
    public Customer create(Customer customer) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Customer update(Customer customer) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Customer findById(String id) {
        // Retorna null para evitar crasheos al arrancar la UI
        return null;
    }

    @Override
    public List<Customer> findAll() {
        // Retorna lista vacía para permitir inicialización de tablas en la UI
        return new ArrayList<>();
    }
}
