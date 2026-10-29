package com.autoclean.repository;

import com.autoclean.model.Customer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad Customer.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class CustomerRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, Customer> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza un cliente. Si no tiene ID, se autogenera con UUID.
     *
     * @param customer cliente a guardar
     * @return cliente guardado con su ID asignado
     */
    public Customer save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (customer.getId() == null || customer.getId().trim().isEmpty()) {
            customer.setId(UUID.randomUUID().toString());
        }
        storage.put(customer.getId(), customer);
        return customer;
    }

    /**
     * Busca un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return cliente encontrado o null si no existe
     */
    public Customer findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todos los clientes registrados.
     *
     * @return lista de clientes
     */
    public List<Customer> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina un cliente por su identificador.
     *
     * @param id identificador del cliente a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}
