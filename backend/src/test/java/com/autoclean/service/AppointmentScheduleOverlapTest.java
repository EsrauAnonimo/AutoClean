package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.dto.appointment.OccupiedSlotDto;
import com.autoclean.model.Appointment;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.EmployeeRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.repository.VehicleRepository;
import com.autoclean.service.impl.AppointmentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de validación de cruces de horario y consulta de disponibilidad de citas.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentScheduleOverlapTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    private AppointmentService appointmentService;

    private Customer customer;
    private Customer otherCustomer;
    private Vehicle vehicle;
    private Vehicle otherVehicle;
    private Employee employee;
    private Employee otherEmployee;
    private ServiceType serviceType;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                customerRepository,
                vehicleRepository,
                employeeRepository,
                serviceTypeRepository
        );

        customer = new Customer("Juan Pérez", "3001234567", "juan@autoclean.com");
        customer.setId(UUID.randomUUID());

        otherCustomer = new Customer("María Gómez", "3007654321", "maria@autoclean.com");
        otherCustomer.setId(UUID.randomUUID());

        vehicle = new Vehicle(customer, "ABC123", "Mazda", "CX-5", 2023);
        vehicle.setId(UUID.randomUUID());

        otherVehicle = new Vehicle(otherCustomer, "XYZ987", "Toyota", "Corolla", 2022);
        otherVehicle.setId(UUID.randomUUID());

        employee = new Employee("Carlos López", "Detailer");
        employee.setId(UUID.randomUUID());

        otherEmployee = new Employee("Ana Ruiz", "Detailer");
        otherEmployee.setId(UUID.randomUUID());

        serviceType = new ServiceType("Full Detailing", "Detalle completo", new BigDecimal("80000"), 60);
        serviceType.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Debe rechazar cita superpuesta para el mismo especialista")
    void shouldRejectOverlappingAppointmentForSameEmployee() {
        OffsetDateTime existingStart = OffsetDateTime.of(2026, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC);
        Appointment existing = new Appointment(otherCustomer, otherVehicle, employee, serviceType, existingStart, AppointmentStatus.PENDING);
        existing.setId(UUID.randomUUID());

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(serviceTypeRepository.findById(serviceType.getId())).thenReturn(Optional.of(serviceType));
        when(appointmentRepository.findByStatusIn(any())).thenReturn(List.of(existing));

        AppointmentRequestDto conflictingRequest = new AppointmentRequestDto(
                customer.getId(),
                vehicle.getId(),
                employee.getId(),
                serviceType.getId(),
                existingStart.plusMinutes(30),
                AppointmentStatus.PENDING
        );

        assertThrows(IllegalStateException.class, () -> appointmentService.create(conflictingRequest));
    }

    @Test
    @DisplayName("Debe rechazar cita superpuesta para el mismo vehículo aunque cambie el especialista")
    void shouldRejectOverlappingAppointmentForSameVehicle() {
        OffsetDateTime existingStart = OffsetDateTime.of(2026, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC);
        Appointment existing = new Appointment(customer, vehicle, employee, serviceType, existingStart, AppointmentStatus.IN_PROGRESS);
        existing.setId(UUID.randomUUID());

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(employeeRepository.findById(otherEmployee.getId())).thenReturn(Optional.of(otherEmployee));
        when(serviceTypeRepository.findById(serviceType.getId())).thenReturn(Optional.of(serviceType));
        when(appointmentRepository.findByStatusIn(any())).thenReturn(List.of(existing));

        AppointmentRequestDto conflictingRequest = new AppointmentRequestDto(
                customer.getId(),
                vehicle.getId(),
                otherEmployee.getId(),
                serviceType.getId(),
                existingStart,
                AppointmentStatus.PENDING
        );

        assertThrows(IllegalStateException.class, () -> appointmentService.create(conflictingRequest));
    }

    @Test
    @DisplayName("Debe permitir actualizar la misma cita en su franja sin conflicto consigo misma")
    void shouldAllowUpdatingSameAppointmentWithoutSelfConflict() {
        OffsetDateTime existingStart = OffsetDateTime.of(2026, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC);
        UUID id = UUID.randomUUID();
        Appointment existing = new Appointment(customer, vehicle, employee, serviceType, existingStart, AppointmentStatus.PENDING);
        existing.setId(id);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(serviceTypeRepository.findById(serviceType.getId())).thenReturn(Optional.of(serviceType));
        when(appointmentRepository.findByStatusIn(any())).thenReturn(List.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentRequestDto request = new AppointmentRequestDto(
                customer.getId(),
                vehicle.getId(),
                employee.getId(),
                serviceType.getId(),
                existingStart.plusMinutes(15),
                AppointmentStatus.PENDING
        );

        AppointmentResponseDto updated = appointmentService.update(id, request);
        assertEquals(existingStart.plusMinutes(15), updated.dateTime());
    }

    @Test
    @DisplayName("findOccupiedSlots debe filtrar por fecha y especialista retornando rangos ocupados")
    void shouldReturnOccupiedSlotsFilteredByDateAndEmployee() {
        OffsetDateTime target = OffsetDateTime.of(2026, 11, 20, 10, 0, 0, 0, ZoneOffset.UTC);
        Appointment a1 = new Appointment(customer, vehicle, employee, serviceType, target, AppointmentStatus.PENDING);
        a1.setId(UUID.randomUUID());
        Appointment a2 = new Appointment(otherCustomer, otherVehicle, otherEmployee, serviceType, target, AppointmentStatus.IN_PROGRESS);
        a2.setId(UUID.randomUUID());
        Appointment outsideDate = new Appointment(customer, vehicle, employee, serviceType, target.plusDays(5), AppointmentStatus.PENDING);
        outsideDate.setId(UUID.randomUUID());

        when(appointmentRepository.findByStatusIn(any())).thenReturn(List.of(a1, a2, outsideDate));

        List<OccupiedSlotDto> slots = appointmentService.findOccupiedSlots(LocalDate.of(2026, 11, 20), employee.getId());

        assertEquals(1, slots.size());
        assertEquals(target, slots.get(0).startTime());
        assertEquals(target.plusMinutes(60), slots.get(0).endTime());
        assertEquals(employee.getId(), slots.get(0).employeeId());
    }
}

