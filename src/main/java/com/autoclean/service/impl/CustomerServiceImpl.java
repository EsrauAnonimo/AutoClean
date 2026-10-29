package com.autoclean.service.impl;

import com.autoclean.contracts.CustomerService;
import com.autoclean.model.Customer;
import com.autoclean.repository.CustomerRepository;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de clientes.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    /**
     * Constructor por defecto utilizando el repositorio en memoria estándar.
     */
    public CustomerServiceImpl() {
        this(new CustomerRepository());
    }

    /**
     * Constructor con inyección de repositorio.
     *
     * @param repository repositorio de clientes a utilizar
     */
    public CustomerServiceImpl(CustomerRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio de clientes no puede ser nulo");
        }
        this.repository = repository;
    }

    @Override
    public Customer create(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        return repository.save(customer);
    }

    @Override
    public Customer update(Customer customer) {
        if (customer == null || customer.getId() == null || customer.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente y su identificador son requeridos para actualizar");
        }
        Customer existing = repository.findById(customer.getId());
        if (existing == null) {
            throw new IllegalArgumentException("No existe un cliente con el identificador: " + customer.getId());
        }
        return repository.save(customer);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del cliente no puede estar vacío");
        }
        repository.delete(id);
    }

    @Override
    public Customer findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll();
    }
}

