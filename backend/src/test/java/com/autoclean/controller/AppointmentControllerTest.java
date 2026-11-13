package com.autoclean.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.dto.appointment.AppointmentRequestDto;
import com.autoclean.dto.appointment.AppointmentResponseDto;
import com.autoclean.dto.appointment.AppointmentStatusUpdateDto;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de capa web para {@link AppointmentController}.
 */
@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("GET /api/appointments debe retornar listado de citas en DTO")
    void shouldReturnAllAppointments() throws Exception {
        AppointmentResponseDto dto = sampleDto(UUID.randomUUID(), AppointmentStatus.PENDING);
        when(appointmentService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.id().toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].vehiclePlate").value("ABC123"));
    }

    @Test
    @DisplayName("GET /api/appointments?customerId=... debe filtrar por cliente")
    void shouldFilterAppointmentsByCustomerId() throws Exception {
        UUID customerId = UUID.randomUUID();
        AppointmentResponseDto dto = sampleDto(UUID.randomUUID(), AppointmentStatus.PENDING);
        when(appointmentService.findByCustomerId(customerId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments").param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} debe retornar una cita por identificador")
    void shouldReturnAppointmentById() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentResponseDto dto = sampleDto(id, AppointmentStatus.IN_PROGRESS);
        when(appointmentService.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/appointments debe crear la cita y retornar 201 Created con Location")
    void shouldCreateAppointment() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentRequestDto request = new AppointmentRequestDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now().plusDays(1),
                null
        );
        AppointmentResponseDto created = sampleDto(id, AppointmentStatus.PENDING);
        when(appointmentService.create(any(AppointmentRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/appointments/" + id))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id} debe actualizar datos de la cita")
    void shouldUpdateAppointment() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentRequestDto request = new AppointmentRequestDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now().plusDays(2),
                AppointmentStatus.PENDING
        );
        AppointmentResponseDto updated = sampleDto(id, AppointmentStatus.PENDING);
        when(appointmentService.update(eq(id), any(AppointmentRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/appointments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/status debe transicionar estado válido con 200 OK")
    void shouldUpdateStatusWithPatch() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentStatusUpdateDto request = new AppointmentStatusUpdateDto(AppointmentStatus.IN_PROGRESS);
        AppointmentResponseDto updated = sampleDto(id, AppointmentStatus.IN_PROGRESS);
        when(appointmentService.updateStatus(id, AppointmentStatus.IN_PROGRESS)).thenReturn(updated);

        mockMvc.perform(patch("/api/appointments/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/status debe retornar 409 Conflict ante transición ilegal")
    void shouldReturnConflictOnIllegalStateTransition() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentStatusUpdateDto request = new AppointmentStatusUpdateDto(AppointmentStatus.FINISHED);
        when(appointmentService.updateStatus(id, AppointmentStatus.FINISHED))
                .thenThrow(new IllegalStateException("Transición inválida desde PENDING hacia FINISHED"));

        mockMvc.perform(patch("/api/appointments/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/status debe retornar 400 Bad Request si status es nulo")
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/appointments/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("DELETE /api/appointments/{id} debe retornar 204 No Content")
    void shouldDeleteAppointment() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(appointmentService).delete(id);

        mockMvc.perform(delete("/api/appointments/{id}", id))
                .andExpect(status().isNoContent());
    }

    private AppointmentResponseDto sampleDto(UUID id, AppointmentStatus status) {
        OffsetDateTime now = OffsetDateTime.now();
        return new AppointmentResponseDto(
                id,
                UUID.randomUUID(),
                "Juan Pérez",
                UUID.randomUUID(),
                "ABC123",
                UUID.randomUUID(),
                "Carlos López",
                UUID.randomUUID(),
                "Lavado Full",
                now.plusDays(1),
                status,
                now,
                now
        );
    }
}

