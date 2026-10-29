package com.autoclean;

import com.autoclean.contracts.CustomerService;
import com.autoclean.model.Customer;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.service.impl.CustomerServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de clientes (CustomerServiceImpl).
 */
public class CustomerServiceImplTest {

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        // Inicializar repositorio aislado para cada prueba
        CustomerRepository repository = new CustomerRepository();
        customerService = new CustomerServiceImpl(repository);
    }

    @Test
    @DisplayName("Debe crear y buscar un cliente por su ID correctamente")
    void testCreateAndFindById() {
        // Crear cliente con datos válidos
        Customer customer = new Customer(null, "Carlos Perez", "3001234567", "carlos@example.com");
        Customer created = customerService.create(customer);

        // Validar que se haya generado un ID
        assertNotNull(created);
        assertNotNull(created.getId());
        assertFalse(created.getId().trim().isEmpty());

        // Buscar el cliente por su ID
        Customer found = customerService.findById(created.getId());
        assertNotNull(found);
        assertEquals("Carlos Perez", found.getName());
        assertEquals("3001234567", found.getPhone());
        assertEquals("carlos@example.com", found.getEmail());
    }

    @Test
    @DisplayName("Debe actualizar la información de un cliente existente")
    void testUpdate() {
        // Registrar cliente inicial
        Customer customer = new Customer(null, "Ana Gómez", "3109876543", "ana@example.com");
        Customer created = customerService.create(customer);

        // Modificar información
        created.setName("Ana Gómez Restrepo");
        created.setPhone("3111112233");
        Customer updated = customerService.update(created);

        // Verificar persistencia de los cambios
        assertEquals("Ana Gómez Restrepo", updated.getName());
        assertEquals("3111112233", updated.getPhone());

        Customer found = customerService.findById(created.getId());
        assertEquals("Ana Gómez Restrepo", found.getName());
    }

    @Test
    @DisplayName("Debe eliminar un cliente existente")
    void testDelete() {
        // Registrar cliente
        Customer customer = new Customer(null, "Luis Torres", "3155558899", "luis@example.com");
        Customer created = customerService.create(customer);

        // Eliminar cliente
        customerService.delete(created.getId());

        // Verificar que ya no exista
        Customer found = customerService.findById(created.getId());
        assertNull(found);
    }

    @Test
    @DisplayName("Debe listar todos los clientes registrados")
    void testFindAll() {
        customerService.create(new Customer(null, "Cliente 1", "3000000001", "c1@test.com"));
        customerService.create(new Customer(null, "Cliente 2", "3000000002", "c2@test.com"));

        List<Customer> all = customerService.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar crear un cliente nulo")
    void testCreateNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> customerService.create(null));
    }
}

