package com.autoclean.controller;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.dto.appointment.AppointmentStatusUpdateDto;
import com.autoclean.dto.appointment.OccupiedSlotDto;
import com.autoclean.dto.appointment.VehicleStatusUpdateDto;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.service.AppointmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de citas automotrices y sus cambios de estado.
 * Delega toda la lógica al servicio y expone exclusivamente DTOs.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Inyección por constructor del servicio de citas.
     *
     * @param appointmentService contrato de servicio de citas
     */
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) AppointmentStatus status) {
        List<AppointmentResponseDto> result;
        if (customerId != null) {
            result = appointmentService.findByCustomerId(customerId);
        } else if (status != null) {
            result = appointmentService.findByStatus(status);
        } else {
            result = appointmentService.findAll();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<OccupiedSlotDto>> getOccupiedSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID employeeId) {
        return ResponseEntity.ok(appointmentService.findOccupiedSlots(date, employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentRequestDto request) {
        AppointmentResponseDto created = appointmentService.create(request);
        URI location = URI.create("/api/appointments/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequestDto request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusUpdateDto request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request.status()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PatchMapping("/{id}/vehicle-status")
    public ResponseEntity<AppointmentResponseDto> updateVehicleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleStatusUpdateDto request) {
        return ResponseEntity.ok(appointmentService.updateVehicleStatus(id, request.vehicleStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

