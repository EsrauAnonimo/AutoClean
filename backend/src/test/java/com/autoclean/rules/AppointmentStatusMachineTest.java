package com.autoclean.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.autoclean.rules.spec.AppointmentStatusSpec;
import com.autoclean.rules.spec.LegacyMirrorAppointmentStatusSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Especificación de la máquina de estados de citas (AGENTS.md §8 / AppointmentServiceImpl).
 */
class AppointmentStatusMachineTest {

    private AppointmentStatusSpec statusMachine;

    @BeforeEach
    void setUp() {
        statusMachine = new LegacyMirrorAppointmentStatusSpec();
        statusMachine.registerAppointment("app-1", "PENDING");
    }

    @Test
    @DisplayName("PENDING -> IN_PROGRESS debe permitirse")
    void shouldAllowPendingToInProgress() {
        statusMachine.updateStatus("app-1", "IN_PROGRESS");
        assertEquals("IN_PROGRESS", statusMachine.getStatus("app-1"));
    }

    @Test
    @DisplayName("PENDING -> CANCELLED debe permitirse")
    void shouldAllowPendingToCancelled() {
        statusMachine.updateStatus("app-1", "CANCELLED");
        assertEquals("CANCELLED", statusMachine.getStatus("app-1"));
    }

    @Test
    @DisplayName("PENDING -> FINISHED debe lanzar IllegalStateException")
    void shouldRejectPendingToFinished() {
        assertThrows(IllegalStateException.class,
                () -> statusMachine.updateStatus("app-1", "FINISHED"));
    }

    @Test
    @DisplayName("IN_PROGRESS -> FINISHED debe permitirse")
    void shouldAllowInProgressToFinished() {
        statusMachine.updateStatus("app-1", "IN_PROGRESS");
        statusMachine.updateStatus("app-1", "FINISHED");
        assertEquals("FINISHED", statusMachine.getStatus("app-1"));
    }

    @Test
    @DisplayName("IN_PROGRESS -> CANCELLED debe permitirse")
    void shouldAllowInProgressToCancelled() {
        statusMachine.updateStatus("app-1", "IN_PROGRESS");
        statusMachine.updateStatus("app-1", "CANCELLED");
        assertEquals("CANCELLED", statusMachine.getStatus("app-1"));
    }

    @Test
    @DisplayName("IN_PROGRESS -> PENDING debe lanzar IllegalStateException")
    void shouldRejectInProgressToPending() {
        statusMachine.updateStatus("app-1", "IN_PROGRESS");
        assertThrows(IllegalStateException.class,
                () -> statusMachine.updateStatus("app-1", "PENDING"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "IN_PROGRESS", "FINISHED", "CANCELLED"})
    @DisplayName("FINISHED es estado final y cualquier transición lanza IllegalStateException")
    void shouldRejectAnyTransitionFromFinished(String targetStatus) {
        statusMachine.updateStatus("app-1", "IN_PROGRESS");
        statusMachine.updateStatus("app-1", "FINISHED");

        assertThrows(IllegalStateException.class,
                () -> statusMachine.updateStatus("app-1", targetStatus));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "IN_PROGRESS", "FINISHED", "CANCELLED"})
    @DisplayName("CANCELLED es estado final y cualquier transición lanza IllegalStateException")
    void shouldRejectAnyTransitionFromCancelled(String targetStatus) {
        statusMachine.updateStatus("app-1", "CANCELLED");

        assertThrows(IllegalStateException.class,
                () -> statusMachine.updateStatus("app-1", targetStatus));
    }
}
