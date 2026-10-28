package com.autoclean.contracts;

import com.autoclean.model.Customer;
import java.util.List;

/**
 * Contrato de servicio para la administración de clientes.
 */
public interface CustomerService {

    /**
     * Registra un nuevo cliente.
     */
    Customer create(Customer customer);

    /**
     * Actualiza la información de un cliente existente.
     */
    Customer update(Customer customer);

    /**
     * Elimina un cliente por su identificador.
     */
    void delete(String id);

    /**
     * Busca un cliente por su identificador.
     */
    Customer findById(String id);

    /**
     * Obtiene la lista completa de clientes.
     */
    List<Customer> findAll();
}
