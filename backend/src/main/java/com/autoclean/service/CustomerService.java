package com.autoclean.service;

import com.autoclean.dto.customer.CustomerRequest;
import com.autoclean.dto.customer.CustomerResponse;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para las operaciones CRUD de clientes.
 */
public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(UUID id, CustomerRequest request);

    void delete(UUID id);

    CustomerResponse findById(UUID id);

    List<CustomerResponse> findAll();
}
