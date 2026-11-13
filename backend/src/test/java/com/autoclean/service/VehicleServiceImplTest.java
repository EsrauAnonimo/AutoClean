package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.vehicle.VehicleRequest;
import com.autoclean.dto.vehicle.VehicleResponse;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.Customer;
import com.autoclean.model.Vehicle;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.VehicleRepository;
import com.autoclean.service.impl.VehicleServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    private VehicleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehicleServiceImpl(vehicleRepository, customerRepository);
    }

    @Test
    void shouldCreateVehicleForRealCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Customer customer = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        customer.setId(customerId);

        Vehicle saved = new Vehicle(customer, "abc123", "Mazda", "3", 2023);
        saved.setId(vehicleId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(saved);

        VehicleResponse response = service.create(new VehicleRequest(customerId, "abc123", "Mazda", "3", 2023));

        assertEquals(vehicleId, response.id());
        assertEquals(customerId, response.customerId());
        assertEquals("ABC123", response.plate());
    }

    @Test
    void shouldRejectVehicleWhenCustomerMissingOrModelInvalid() {
        UUID missingCustomerId = UUID.randomUUID();
        when(customerRepository.findById(missingCustomerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new VehicleRequest(missingCustomerId, "ABC123", "Mazda", "3", 2023)));

        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(new VehicleRequest(customerId, "ABC123", "Mazda", "3", 1850)));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingVehicleAndRejectMissingId() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Customer customer = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        customer.setId(customerId);
        Vehicle existing = new Vehicle(customer, "ABC123", "Mazda", "3", 2020);
        existing.setId(vehicleId);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse updated = service.update(vehicleId,
                new VehicleRequest(customerId, "xyz987", "Toyota", "Corolla", 2024));

        assertEquals("XYZ987", updated.plate());
        assertEquals("Toyota", updated.brand());
        assertEquals(2024, updated.year());

        UUID missingVehicleId = UUID.randomUUID();
        when(vehicleRepository.findById(missingVehicleId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.update(missingVehicleId, new VehicleRequest(customerId, "XYZ987", "Toyota", "Corolla", 2024)));
    }

    @Test
    void shouldDeleteExistingAndRejectMissingVehicle() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        when(vehicleRepository.existsById(existingId)).thenReturn(true);
        when(vehicleRepository.existsById(missingId)).thenReturn(false);

        service.delete(existingId);
        verify(vehicleRepository).deleteById(existingId);

        assertThrows(IllegalArgumentException.class, () -> service.delete(missingId));
        assertThrows(IllegalArgumentException.class, () -> service.delete(null));
    }

    @Test
    void shouldFindVehiclesByCustomerIdAndAll() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        customer.setId(customerId);
        Vehicle vehicle = new Vehicle(customer, "DEF456", "Kia", "Rio", 2022);
        vehicle.setId(UUID.randomUUID());

        when(vehicleRepository.findByCustomerId(customerId)).thenReturn(List.of(vehicle));
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));

        List<VehicleResponse> byCustomer = service.findByCustomerId(customerId);
        assertEquals(1, byCustomer.size());
        assertEquals("DEF456", byCustomer.get(0).plate());
        assertEquals(1, service.findAll().size());
        assertThrows(IllegalArgumentException.class, () -> service.findByCustomerId(null));
    }

    @Test
    void shouldFindVehicleByPlateIgnoringCaseOrThrow() {
        Customer customer = new Customer("Carlos", "3104589210", "carlos@autoclean.com");
        customer.setId(UUID.randomUUID());
        Vehicle vehicle = new Vehicle(customer, "ABC123", "Mazda", "3", 2023);
        vehicle.setId(UUID.randomUUID());

        when(vehicleRepository.findByPlateIgnoreCase("ABC123")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findByPlateIgnoreCase("ZZZ999")).thenReturn(Optional.empty());

        VehicleResponse found = service.findByPlate(" abc123 ");
        assertEquals("ABC123", found.plate());

        assertThrows(IllegalArgumentException.class, () -> service.findByPlate("   "));
        assertThrows(ResourceNotFoundException.class, () -> service.findByPlate("zzz999"));
    }
}
