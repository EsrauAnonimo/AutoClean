package com.autoclean;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.model.Appointment;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.service.impl.AppointmentServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para las reglas de negocio de turnos (AppointmentServiceImpl).
 */
public class AppointmentServiceImplTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        AppointmentRepository repository = new AppointmentRepository();
        appointmentService = new AppointmentServiceImpl(repository);
    }

    @Test
    @DisplayName("Debe permitir el flujo completo de transiciones válidas: PENDING -> IN_PROGRESS -> FINISHED")
    void testValidTransitionsPendingToInProgressToFinished() {
        // Crear turno en estado inicial PENDING
        Appointment appointment = new Appointment(null, "cli-1", "veh-1", "srv-1", "emp-1",
                LocalDateTime.now(), "PENDING");
        Appointment created = appointmentService.create(appointment);

        assertEquals("PENDING", created.getStatus());

        // Transición válida: PENDING -> IN_PROGRESS
        appointmentService.updateStatus(created.getId(), "IN_PROGRESS");
        Appointment inProgress = appointmentService.findById(created.getId());
        assertEquals("IN_PROGRESS", inProgress.getStatus());

        // Transición válida: IN_PROGRESS -> FINISHED
        appointmentService.updateStatus(created.getId(), "FINISHED");
        Appointment finished = appointmentService.findById(created.getId());
        assertEquals("FINISHED", finished.getStatus());
    }

    @Test
    @DisplayName("Debe permitir cancelar un turno desde PENDING y desde IN_PROGRESS")
    void testValidTransitionsToCancelled() {
        // Caso 1: Cancelar desde PENDING
        Appointment app1 = appointmentService.create(new Appointment(null, "cli-1", "veh-1", "srv-1", "emp-1",
                LocalDateTime.now(), "PENDING"));
        appointmentService.updateStatus(app1.getId(), "CANCELLED");
        assertEquals("CANCELLED", appointmentService.findById(app1.getId()).getStatus());

        // Caso 2: Cancelar desde IN_PROGRESS
        Appointment app2 = appointmentService.create(new Appointment(null, "cli-2", "veh-2", "srv-2", "emp-2",
                LocalDateTime.now(), "PENDING"));
        appointmentService.updateStatus(app2.getId(), "IN_PROGRESS");
        appointmentService.updateStatus(app2.getId(), "CANCELLED");
        assertEquals("CANCELLED", appointmentService.findById(app2.getId()).getStatus());
    }

    @Test
    @DisplayName("Debe rechazar transición inválida: salto directo de PENDING a FINISHED")
    void testInvalidTransitionPendingToFinishedThrowsException() {
        Appointment appointment = appointmentService.create(new Appointment(null, "cli-1", "veh-1", "srv-1", "emp-1",
                LocalDateTime.now(), "PENDING"));

        // Intentar pasar directamente a FINISHED sin pasar por IN_PROGRESS
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointment.getId(), "FINISHED"));
        assertTrue(ex.getMessage().contains("Transición inválida"));
    }

    @Test
    @DisplayName("Debe rechazar cualquier cambio si el turno ya está en estado final FINISHED")
    void testFinishedIsFinalStateAndThrowsExceptionOnFurtherChanges() {
        Appointment appointment = appointmentService.create(new Appointment(null, "cli-1", "veh-1", "srv-1", "emp-1",
                LocalDateTime.now(), "PENDING"));
        appointmentService.updateStatus(appointment.getId(), "IN_PROGRESS");
        appointmentService.updateStatus(appointment.getId(), "FINISHED");

        // Intentar modificar desde FINISHED
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointment.getId(), "CANCELLED"));
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointment.getId(), "IN_PROGRESS"));
    }

    @Test
    @DisplayName("Debe rechazar cualquier cambio si el turno ya está en estado final CANCELLED")
    void testCancelledIsFinalStateAndThrowsExceptionOnFurtherChanges() {
        Appointment appointment = appointmentService.create(new Appointment(null, "cli-1", "veh-1", "srv-1", "emp-1",
                LocalDateTime.now(), "PENDING"));
        appointmentService.updateStatus(appointment.getId(), "CANCELLED");

        // Intentar modificar desde CANCELLED
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointment.getId(), "PENDING"));
        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(appointment.getId(), "IN_PROGRESS"));
    }
}

