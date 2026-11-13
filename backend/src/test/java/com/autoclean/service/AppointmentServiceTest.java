package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.Appointment;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.model.VehicleStatus;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.EmployeeRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.repository.VehicleRepository;
import com.autoclean.service.impl.AppointmentServiceImpl;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para {@link AppointmentServiceImpl} y su máquina de estados estricta.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

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

    private UUID customerId;
    private UUID vehicleId;
    private UUID employeeId;
    private UUID serviceTypeId;
    private UUID appointmentId;
    private Customer customer;
    private Vehicle vehicle;
    private Employee employee;
    private ServiceType serviceType;
    private OffsetDateTime dateTime;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                customerRepository,
                vehicleRepository,
                employeeRepository,
                serviceTypeRepository
        );

        customerId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        serviceTypeId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        dateTime = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        customer = new Customer("Juan Pérez", "3001234567", "juan@autoclean.com");
        customer.setId(customerId);

        vehicle = new Vehicle(customer, "ABC123", "Mazda", "CX-5", 2023);
        vehicle.setId(vehicleId);

        employee = new Employee("Carlos López", "Detailer");
        employee.setId(employeeId);

        serviceType = new ServiceType("Lavado Full", "Lavado completo interior/exterior", new BigDecimal("55000.00"), 60);
        serviceType.setId(serviceTypeId);
    }

    @Test
    @DisplayName("create debe asignar estado PENDING por defecto cuando status es nulo")
    void shouldCreateAppointmentWithDefaultPendingStatus() {
        mockAllReferencesFound();
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(appointmentId);
            return saved;
        });

        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, dateTime, null
        );

        AppointmentResponseDto response = appointmentService.create(request);

        assertNotNull(response);
        assertEquals(appointmentId, response.id());
        assertEquals(AppointmentStatus.PENDING, response.status());
        assertEquals(customerId, response.customerId());
        assertEquals("ABC123", response.vehiclePlate());
        assertEquals("Lavado Full", response.serviceTypeName());
    }

    @Test
    @DisplayName("create debe rechazar solicitud nula o referencia inexistente")
    void shouldRejectNullRequestOrMissingReferenceOnCreate() {
        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(null));

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, dateTime, null
        );

        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(request));
    }

    @Test
    @DisplayName("update debe actualizar datos cuando la cita y referencias existen")
    void shouldUpdateAppointmentData() {
        Appointment existing = buildAppointment(AppointmentStatus.PENDING);
        mockAllReferencesFound();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime updatedTime = dateTime.plusHours(3);
        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, updatedTime, AppointmentStatus.PENDING
        );

        AppointmentResponseDto response = appointmentService.update(appointmentId, request);

        assertEquals(updatedTime, response.dateTime());
        assertEquals(AppointmentStatus.PENDING, response.status());
    }

    @Test
    @DisplayName("update debe rechazar cambio ilegal de estado mediante la máquina de estados")
    void shouldRejectIllegalStatusChangeDuringUpdate() {
        Appointment existing = buildAppointment(AppointmentStatus.FINISHED);
        mockAllReferencesFound();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));

        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, dateTime, AppointmentStatus.PENDING
        );

        assertThrows(IllegalStateException.class, () -> appointmentService.update(appointmentId, request));
    }

    @Test
    @DisplayName("update y delete deben lanzar IllegalArgumentException cuando la cita no existe")
    void shouldRejectMissingAppointmentOnUpdateAndDelete() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());
        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, dateTime, null
        );
        assertThrows(IllegalArgumentException.class, () -> appointmentService.update(appointmentId, request));

        when(appointmentRepository.existsById(appointmentId)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> appointmentService.delete(appointmentId));
    }

    @Test
    @DisplayName("delete debe eliminar una cita existente")
    void shouldDeleteExistingAppointment() {
        when(appointmentRepository.existsById(appointmentId)).thenReturn(true);

        appointmentService.delete(appointmentId);

        verify(appointmentRepository).deleteById(appointmentId);
    }

    @Test
    @DisplayName("findById debe retornar DTO si existe o lanzar ResourceNotFoundException si no")
    void shouldFindByIdOrThrowResourceNotFound() {
        Appointment existing = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));

        AppointmentResponseDto found = appointmentService.findById(appointmentId);
        assertEquals(appointmentId, found.id());

        UUID missingId = UUID.randomUUID();
        when(appointmentRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.findById(missingId));
    }

    @Test
    @DisplayName("findAll, findByCustomerId y findByStatus deben retornar DTOs mapeados")
    void shouldListAppointmentsByFilters() {
        Appointment existing = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findAll()).thenReturn(List.of(existing));
        when(appointmentRepository.findByCustomerId(customerId)).thenReturn(List.of(existing));
        when(appointmentRepository.findByStatus(AppointmentStatus.PENDING)).thenReturn(List.of(existing));

        assertEquals(1, appointmentService.findAll().size());
        assertEquals(1, appointmentService.findByCustomerId(customerId).size());
        assertEquals(1, appointmentService.findByStatus(AppointmentStatus.PENDING).size());
    }

    @Test
    @DisplayName("PENDING -> IN_PROGRESS debe permitirse")
    void shouldAllowPendingToInProgress() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDto updated = appointmentService.updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS);
        assertEquals(AppointmentStatus.IN_PROGRESS, updated.status());
    }

    @Test
    @DisplayName("PENDING -> CANCELLED debe permitirse")
    void shouldAllowPendingToCancelled() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDto updated = appointmentService.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
        assertEquals(AppointmentStatus.CANCELLED, updated.status());
    }

    @Test
    @DisplayName("PENDING -> FINISHED y PENDING -> PENDING deben lanzar IllegalStateException")
    void shouldRejectInvalidTransitionsFromPending() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, AppointmentStatus.FINISHED));
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, AppointmentStatus.PENDING));
    }

    @Test
    @DisplayName("IN_PROGRESS -> FINISHED y IN_PROGRESS -> CANCELLED deben permitirse")
    void shouldAllowValidTransitionsFromInProgress() {
        Appointment appointmentForFinish = buildAppointment(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointmentForFinish));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(AppointmentStatus.FINISHED,
                appointmentService.updateStatus(appointmentId, AppointmentStatus.FINISHED).status());

        Appointment appointmentForCancel = buildAppointment(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointmentForCancel));

        assertEquals(AppointmentStatus.CANCELLED,
                appointmentService.updateStatus(appointmentId, AppointmentStatus.CANCELLED).status());
    }

    @Test
    @DisplayName("IN_PROGRESS -> PENDING y IN_PROGRESS -> IN_PROGRESS deben lanzar IllegalStateException")
    void shouldRejectInvalidTransitionsFromInProgress() {
        Appointment appointment = buildAppointment(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, AppointmentStatus.PENDING));
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS));
    }

    @ParameterizedTest
    @EnumSource(AppointmentStatus.class)
    @DisplayName("FINISHED es estado final y cualquier transición debe lanzar IllegalStateException")
    void shouldRejectAnyTransitionFromFinished(AppointmentStatus targetStatus) {
        Appointment appointment = buildAppointment(AppointmentStatus.FINISHED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, targetStatus));
    }

    @ParameterizedTest
    @EnumSource(AppointmentStatus.class)
    @DisplayName("CANCELLED es estado final y cualquier transición debe lanzar IllegalStateException")
    void shouldRejectAnyTransitionFromCancelled(AppointmentStatus targetStatus) {
        Appointment appointment = buildAppointment(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointmentId, targetStatus));
    }

    @Test
    @DisplayName("updateStatus debe rechazar id nulo, estado nulo o cita inexistente con IllegalArgumentException")
    void shouldRejectNullArgumentsOrMissingAppointmentOnUpdateStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(null, AppointmentStatus.IN_PROGRESS));
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(appointmentId, null));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS));
    }

    @ParameterizedTest
    @CsvSource({"7,0", "12,0", "14,30", "21,0"})
    @DisplayName("create debe aceptar horarios dentro de 7:00-12:00 y 14:30-21:00")
    void shouldAllowCreationWithinOperatingHours(int hour, int minute) {
        mockAllReferencesFound();
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(appointmentId);
            return saved;
        });

        OffsetDateTime validDateTime = OffsetDateTime.of(2026, 11, 14, hour, minute, 0, 0, ZoneOffset.ofHours(-5));
        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, validDateTime, null
        );

        AppointmentResponseDto response = appointmentService.create(request);
        assertEquals(VehicleStatus.RECEIVED, response.vehicleStatus());
        assertEquals(AppointmentStatus.PENDING, response.status());
    }

    @ParameterizedTest
    @CsvSource({"6,59", "12,1", "14,29", "21,1"})
    @DisplayName("create debe rechazar horarios fuera de atención con el mensaje exacto")
    void shouldRejectCreationOutsideOperatingHours(int hour, int minute) {
        OffsetDateTime invalidDateTime = OffsetDateTime.of(2026, 11, 14, hour, minute, 0, 0, ZoneOffset.ofHours(-5));
        AppointmentRequestDto request = new AppointmentRequestDto(
                customerId, vehicleId, employeeId, serviceTypeId, invalidDateTime, null
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.create(request)
        );
        assertEquals("Horario de atención: 7am-12pm y 2:30pm-9pm", ex.getMessage());
    }

    @Test
    @DisplayName("cancel debe pasar a CANCELLED solo si la cita está en PENDING")
    void shouldCancelOnlyWhenPending() {
        Appointment pending = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(pending));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDto cancelled = appointmentService.cancel(appointmentId);
        assertEquals(AppointmentStatus.CANCELLED, cancelled.status());

        for (AppointmentStatus nonPending : List.of(
                AppointmentStatus.IN_PROGRESS, AppointmentStatus.FINISHED, AppointmentStatus.CANCELLED)) {
            Appointment other = buildAppointment(nonPending);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(other));
            assertThrows(IllegalStateException.class, () -> appointmentService.cancel(appointmentId));
        }
    }

    @Test
    @DisplayName("updateVehicleStatus debe actualizar el estado en vivo del vehículo y rechazar nulos")
    void shouldUpdateVehicleStatusAndRejectNulls() {
        Appointment appointment = buildAppointment(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDto updated = appointmentService.updateVehicleStatus(appointmentId, VehicleStatus.WASHING);
        assertEquals(VehicleStatus.WASHING, updated.vehicleStatus());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateVehicleStatus(null, VehicleStatus.READY));
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateVehicleStatus(appointmentId, null));
    }

    private void mockAllReferencesFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
    }

    private Appointment buildAppointment(AppointmentStatus status) {
        Appointment appointment = new Appointment(customer, vehicle, employee, serviceType, dateTime, status);
        appointment.setId(appointmentId);
        return appointment;
    }
}

