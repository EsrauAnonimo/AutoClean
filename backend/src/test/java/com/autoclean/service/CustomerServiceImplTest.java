package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.customer.CustomerRequest;
import com.autoclean.dto.customer.CustomerResponse;
import com.autoclean.model.Customer;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.service.impl.CustomerServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void shouldCreateValidCustomer() {
        CustomerRequest request = new CustomerRequest("Carlos Mendoza", "3104589210", "carlos@autoclean.com");
        UUID id = UUID.randomUUID();
        Customer saved = new Customer("Carlos Mendoza", "3104589210", "carlos@autoclean.com");
        saved.setId(id);

        when(customerRepository.existsByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = service.create(request);

        assertEquals(id, response.id());
        assertEquals("Carlos Mendoza", response.name());
        assertEquals("carlos@autoclean.com", response.email());
    }

    @Test
    void shouldRejectInvalidOrDuplicateCustomerOnCreate() {
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new CustomerRequest("Carlos", "abc", "carlos@autoclean.com")));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new CustomerRequest("Carlos", "3104589210", "correo-invalido")));

        when(customerRepository.existsByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new CustomerRequest("Carlos", "3104589210", "carlos@autoclean.com")));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingCustomer() {
        UUID id = UUID.randomUUID();
        Customer existing = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        existing.setId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.findByEmailIgnoreCase("carlos.new@autoclean.com")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse updated = service.update(id,
                new CustomerRequest("Carlos Actualizado", "3201112233", "carlos.new@autoclean.com"));

        assertEquals("Carlos Actualizado", updated.name());
        assertEquals("3201112233", updated.phone());
        assertEquals("carlos.new@autoclean.com", updated.email());
    }

    @Test
    void shouldRejectUpdateWhenCustomerDoesNotExistOrEmailConflicts() {
        UUID missingId = UUID.randomUUID();
        CustomerRequest request = new CustomerRequest("Carlos", "3104589210", "carlos@autoclean.com");
        when(customerRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(missingId, request));
        assertThrows(IllegalArgumentException.class, () -> service.update(null, request));

        UUID existingId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Customer existing = new Customer("Carlos", "3104589210", "old@autoclean.com");
        existing.setId(existingId);
        Customer other = new Customer("Ana", "3001112233", "carlos@autoclean.com");
        other.setId(otherId);

        when(customerRepository.findById(existingId)).thenReturn(Optional.of(existing));
        when(customerRepository.findByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class, () -> service.update(existingId, request));
    }

    @Test
    void shouldDeleteExistingAndRejectMissingCustomer() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        when(customerRepository.existsById(existingId)).thenReturn(true);
        when(customerRepository.existsById(missingId)).thenReturn(false);

        service.delete(existingId);
        verify(customerRepository).deleteById(existingId);

        assertThrows(IllegalArgumentException.class, () -> service.delete(missingId));
        assertThrows(IllegalArgumentException.class, () -> service.delete(null));
    }

    @Test
    void shouldFindAllAndFindById() {
        UUID id = UUID.randomUUID();
        Customer c = new Customer("Valentina", "3206549871", "valentina@autoclean.com");
        c.setId(id);

        when(customerRepository.findAll()).thenReturn(List.of(c));
        when(customerRepository.findById(id)).thenReturn(Optional.of(c));

        assertEquals(1, service.findAll().size());
        assertEquals("Valentina", service.findById(id).name());
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }
}
