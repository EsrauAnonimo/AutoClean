package com.autoclean.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.dto.employee.EmployeeRequestDto;
import com.autoclean.dto.employee.EmployeeResponseDto;
import com.autoclean.service.EmployeeService;
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
 * Pruebas de capa web para EmployeeController.
 */
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/employees debe retornar lista de empleados")
    void shouldReturnAllEmployees() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeResponseDto dto = new EmployeeResponseDto(id, "Carlos Gómez", "Detailer", OffsetDateTime.now());

        when(employeeService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Carlos Gómez"))
                .andExpect(jsonPath("$[0].role").value("Detailer"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} debe retornar el empleado correspondiente")
    void shouldReturnEmployeeById() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeResponseDto dto = new EmployeeResponseDto(id, "Mariana López", "Administradora", OffsetDateTime.now());

        when(employeeService.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Mariana López"));
    }

    @Test
    @DisplayName("POST /api/employees debe crear un empleado y retornar 201 Created con cabecera Location")
    void shouldCreateEmployee() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeRequestDto request = new EmployeeRequestDto("Mateo Rios", "Lavador");
        EmployeeResponseDto created = new EmployeeResponseDto(id, "Mateo Rios", "Lavador", OffsetDateTime.now());

        when(employeeService.create(any(EmployeeRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Mateo Rios"));
    }

    @Test
    @DisplayName("POST /api/employees debe retornar 400 Bad Request cuando los datos son inválidos")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        EmployeeRequestDto invalidRequest = new EmployeeRequestDto("", "");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} debe actualizar un empleado")
    void shouldUpdateEmployee() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeRequestDto request = new EmployeeRequestDto("Mateo Rios", "Supervisor");
        EmployeeResponseDto updated = new EmployeeResponseDto(id, "Mateo Rios", "Supervisor", OffsetDateTime.now());

        when(employeeService.update(eq(id), any(EmployeeRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("Supervisor"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} debe retornar 204 No Content")
    void shouldDeleteEmployee() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(employeeService).delete(id);

        mockMvc.perform(delete("/api/employees/{id}", id))
                .andExpect(status().isNoContent());
    }
}
