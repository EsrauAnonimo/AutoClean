package com.autoclean.service.impl;

import com.autoclean.dto.customer.CustomerRequest;
import com.autoclean.dto.customer.CustomerResponse;
import com.autoclean.model.Customer;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.service.CustomerService;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la gestión de clientes.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final CustomerRepository customerRepository;

    /**
     * Inyección por constructor del repositorio de clientes.
     *
     * @param customerRepository repositorio JPA de clientes
     */
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validateRequest(request);
        String normalizedEmail = request.email().trim();
        if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el correo: " + normalizedEmail);
        }
        Customer customer = new Customer(request.name(), request.phone(), normalizedEmail);
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del cliente es requerido para actualizar");
        }
        validateRequest(request);
        Customer existing = findExisting(id);
        String normalizedEmail = request.email().trim();
        customerRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Ya existe otro cliente con el correo: " + normalizedEmail);
                });

        existing.setName(request.name());
        existing.setPhone(request.phone());
        existing.setEmail(normalizedEmail);
        return CustomerResponse.from(customerRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un cliente con el identificador: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        return CustomerResponse.from(findExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    private Customer findExisting(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un cliente con el identificador: " + id));
    }

    private void validateRequest(CustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (request.phone() == null || !PHONE_PATTERN.matcher(request.phone().trim()).matches()) {
            throw new IllegalArgumentException("El teléfono es inválido o está vacío");
        }
        if (request.email() == null || !EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new IllegalArgumentException("El correo es inválido o está vacío");
        }
    }
}
