package com.autoclean.service.impl;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.dto.appointment.OccupiedSlotDto;
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
import com.autoclean.service.AppointmentService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la gestión de citas automotrices.
 * Replica las validaciones y la máquina de estados finita del sistema legacy.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final LocalTime MORNING_START = LocalTime.of(7, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(14, 30);
    private static final LocalTime AFTERNOON_END = LocalTime.of(21, 0);
    private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.IN_PROGRESS
    );

    // Tabla de transiciones permitidas (OCP: facilita extender reglas sin reescribir condicionales)
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            AppointmentStatus.PENDING, EnumSet.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED),
            AppointmentStatus.IN_PROGRESS, EnumSet.of(AppointmentStatus.FINISHED, AppointmentStatus.CANCELLED),
            AppointmentStatus.FINISHED, EnumSet.noneOf(AppointmentStatus.class),
            AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class)
    );

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    /**
     * Inyección por constructor de los repositorios requeridos para gestionar citas.
     *
     * @param appointmentRepository repositorio de citas
     * @param customerRepository repositorio de clientes
     * @param vehicleRepository repositorio de vehículos
     * @param employeeRepository repositorio de empleados
     * @param serviceTypeRepository repositorio de tipos de servicio
     */
    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            EmployeeRepository employeeRepository,
            ServiceTypeRepository serviceTypeRepository) {
        if (appointmentRepository == null || customerRepository == null || vehicleRepository == null
                || employeeRepository == null || serviceTypeRepository == null) {
            throw new IllegalArgumentException("Todos los repositorios requeridos para citas deben estar presentes");
        }
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.employeeRepository = employeeRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Override
    @Transactional
    public AppointmentResponseDto create(AppointmentRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        validateOperatingHours(request.dateTime());

        ResolvedReferences refs = resolveReferences(request);
        // Regla legacy (AppointmentServiceImpl.java:39-41): estado por defecto PENDING
        AppointmentStatus initialStatus = request.status() != null
                ? request.status()
                : AppointmentStatus.PENDING;

        if (ACTIVE_STATUSES.contains(initialStatus)) {
            validateNoScheduleConflict(
                    null,
                    refs.employee().getId(),
                    refs.vehicle().getId(),
                    request.dateTime(),
                    resolveDurationMinutes(refs.serviceType())
            );
        }

        Appointment appointment = new Appointment(
                refs.customer(),
                refs.vehicle(),
                refs.employee(),
                refs.serviceType(),
                request.dateTime(),
                initialStatus
        );

        return AppointmentResponseDto.from(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDto update(UUID id, AppointmentRequestDto request) {
        if (id == null || request == null) {
            throw new IllegalArgumentException("El turno y su identificador son requeridos para actualizar");
        }

        Appointment existing = findExistingForMutation(id);
        ResolvedReferences refs = resolveReferences(request);

        if (request.status() != null && request.status() != existing.getStatus()) {
            validateTransition(existing.getStatus(), request.status());
            existing.setStatus(request.status());
        }

        AppointmentStatus targetStatus = request.status() != null ? request.status() : existing.getStatus();
        if (ACTIVE_STATUSES.contains(targetStatus)) {
            validateNoScheduleConflict(
                    id,
                    refs.employee().getId(),
                    refs.vehicle().getId(),
                    request.dateTime(),
                    resolveDurationMinutes(refs.serviceType())
            );
        }

        existing.setCustomer(refs.customer());
        existing.setVehicle(refs.vehicle());
        existing.setEmployee(refs.employee());
        existing.setServiceType(refs.serviceType());
        existing.setDateTime(request.dateTime());

        return AppointmentResponseDto.from(appointmentRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        if (!appointmentRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un turno con el identificador: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del turno no puede ser nulo");
        }
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el turno con identificador: " + id));
        return AppointmentResponseDto.from(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findAll() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        return appointmentRepository.findByCustomerId(customerId).stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findByStatus(AppointmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de búsqueda no puede ser nulo");
        }
        return appointmentRepository.findByStatus(status).stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDto updateStatus(UUID appointmentId, AppointmentStatus newStatus) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío");
        }

        Appointment appointment = findExistingForMutation(appointmentId);
        AppointmentStatus currentStatus = appointment.getStatus() != null
                ? appointment.getStatus()
                : AppointmentStatus.PENDING;

        validateTransition(currentStatus, newStatus);

        appointment.setStatus(newStatus);
        return AppointmentResponseDto.from(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDto cancel(UUID appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }

        Appointment appointment = findExistingForMutation(appointmentId);
        AppointmentStatus currentStatus = appointment.getStatus() != null
                ? appointment.getStatus()
                : AppointmentStatus.PENDING;

        if (currentStatus != AppointmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar citas en estado PENDING; estado actual: " + currentStatus);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponseDto.from(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDto updateVehicleStatus(UUID appointmentId, VehicleStatus vehicleStatus) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("El identificador del turno no puede estar vacío");
        }
        if (vehicleStatus == null) {
            throw new IllegalArgumentException("El estado del vehículo no puede estar vacío");
        }

        Appointment appointment = findExistingForMutation(appointmentId);
        appointment.setVehicleStatus(vehicleStatus);
        return AppointmentResponseDto.from(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OccupiedSlotDto> findOccupiedSlots(LocalDate date, UUID employeeId) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha de consulta no puede ser nula");
        }

        // Ventana amplia (+/- 1 día en UTC) para evitar cortes por huso horario entre cliente y servidor
        OffsetDateTime windowStart = date.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime windowEnd = date.plusDays(2).atStartOfDay().atOffset(ZoneOffset.UTC);

        return appointmentRepository.findByStatusIn(ACTIVE_STATUSES).stream()
                .filter(existing -> existing.getDateTime() != null)
                .filter(existing -> !existing.getDateTime().isBefore(windowStart)
                        && existing.getDateTime().isBefore(windowEnd))
                .filter(existing -> employeeId == null
                        || (existing.getEmployee() != null && employeeId.equals(existing.getEmployee().getId())))
                .map(existing -> new OccupiedSlotDto(
                        existing.getDateTime(),
                        existing.getDateTime().plusMinutes(resolveDurationMinutes(existing.getServiceType())),
                        existing.getEmployee() != null ? existing.getEmployee().getId() : null
                ))
                .toList();
    }

    private void validateNoScheduleConflict(
            UUID excludeAppointmentId,
            UUID employeeId,
            UUID vehicleId,
            OffsetDateTime startTime,
            int durationMinutes) {
        if (startTime == null) {
            return;
        }
        OffsetDateTime endTime = startTime.plusMinutes(durationMinutes);

        for (Appointment existing : appointmentRepository.findByStatusIn(ACTIVE_STATUSES)) {
            if (excludeAppointmentId != null && excludeAppointmentId.equals(existing.getId())) {
                continue;
            }
            OffsetDateTime existingStart = existing.getDateTime();
            if (existingStart == null) {
                continue;
            }
            OffsetDateTime existingEnd = existingStart.plusMinutes(resolveDurationMinutes(existing.getServiceType()));
            boolean overlaps = startTime.isBefore(existingEnd) && existingStart.isBefore(endTime);
            if (!overlaps) {
                continue;
            }

            if (employeeId != null && existing.getEmployee() != null && employeeId.equals(existing.getEmployee().getId())) {
                throw new IllegalStateException(
                        "El especialista seleccionado ya tiene una cita programada en ese horario");
            }
            if (vehicleId != null && existing.getVehicle() != null && vehicleId.equals(existing.getVehicle().getId())) {
                throw new IllegalStateException(
                        "Este vehículo ya tiene otra cita programada en ese horario");
            }
        }
    }

    private int resolveDurationMinutes(ServiceType serviceType) {
        if (serviceType == null || serviceType.getDurationMin() == null || serviceType.getDurationMin() <= 0) {
            return 60;
        }
        return serviceType.getDurationMin();
    }

    private void validateOperatingHours(OffsetDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("La fecha y hora de la cita no pueden ser nulas");
        }
        LocalTime time = dateTime.toLocalTime();
        boolean inMorningWindow = !time.isBefore(MORNING_START) && !time.isAfter(MORNING_END);
        boolean inAfternoonWindow = !time.isBefore(AFTERNOON_START) && !time.isAfter(AFTERNOON_END);
        if (!inMorningWindow && !inAfternoonWindow) {
            throw new IllegalArgumentException("Horario de atención: 7am-12pm y 2:30pm-9pm");
        }
    }

    private Appointment findExistingForMutation(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un turno con el identificador: " + id));
    }

    private void validateTransition(AppointmentStatus currentStatus, AppointmentStatus targetStatus) {
        // Reglas de negocio de la máquina de estados (legacy AppointmentServiceImpl.java:99-111)
        if (currentStatus == AppointmentStatus.FINISHED || currentStatus == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Transición inválida: el turno ya se encuentra en estado final (" + currentStatus + ") y no admite cambios");
        }

        Set<AppointmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                currentStatus, EnumSet.noneOf(AppointmentStatus.class));

        if (!allowed.contains(targetStatus)) {
            throw new IllegalStateException(
                    "Transición inválida: desde " + currentStatus + " solo se permite " + allowed + ", se intentó: " + targetStatus);
        }
    }

    private ResolvedReferences resolveReferences(AppointmentRequestDto request) {
        if (request.customerId() == null) {
            throw new IllegalArgumentException("El identificador del cliente no puede ser nulo");
        }
        if (request.vehicleId() == null) {
            throw new IllegalArgumentException("El identificador del vehículo no puede ser nulo");
        }
        if (request.employeeId() == null) {
            throw new IllegalArgumentException("El identificador del empleado no puede ser nulo");
        }
        if (request.serviceTypeId() == null) {
            throw new IllegalArgumentException("El tipo de servicio no puede estar vacío");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con el identificador: " + request.customerId()));
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un vehículo con el identificador: " + request.vehicleId()));
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un empleado con el identificador: " + request.employeeId()));
        ServiceType serviceType = serviceTypeRepository.findById(request.serviceTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un tipo de servicio con el identificador: " + request.serviceTypeId()));

        return new ResolvedReferences(customer, vehicle, employee, serviceType);
    }

    private record ResolvedReferences(
            Customer customer,
            Vehicle vehicle,
            Employee employee,
            ServiceType serviceType
    ) {
    }
}

