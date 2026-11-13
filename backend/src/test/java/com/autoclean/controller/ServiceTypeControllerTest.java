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

import com.autoclean.dto.servicetype.ServiceTypeRequestDto;
import com.autoclean.dto.servicetype.ServiceTypeResponseDto;
import com.autoclean.service.ServiceTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
 * Pruebas de capa web para ServiceTypeController.
 */
@WebMvcTest(ServiceTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ServiceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceTypeService serviceTypeService;

    @Test
    @DisplayName("GET /api/service-types debe retornar lista de servicios")
    void shouldReturnAllServiceTypes() throws Exception {
        UUID id = UUID.randomUUID();
        ServiceTypeResponseDto dto = new ServiceTypeResponseDto(
                id, "Lavado General", "Detalle", new BigDecimal("35000.00"), 45, OffsetDateTime.now()
        );

        when(serviceTypeService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/service-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lavado General"))
                .andExpect(jsonPath("$[0].price").value(35000.00));
    }

    @Test
    @DisplayName("GET /api/service-types/{id} debe retornar el servicio correspondiente")
    void shouldReturnServiceTypeById() throws Exception {
        UUID id = UUID.randomUUID();
        ServiceTypeResponseDto dto = new ServiceTypeResponseDto(
                id, "Porcelanizado", "Brillo", new BigDecimal("150000.00"), 90, OffsetDateTime.now()
        );

        when(serviceTypeService.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/service-types/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Porcelanizado"));
    }

    @Test
    @DisplayName("POST /api/service-types debe crear un servicio y retornar 201 Created con cabecera Location")
    void shouldCreateServiceType() throws Exception {
        UUID id = UUID.randomUUID();
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "Encerado", "Cera líquida", new BigDecimal("45000.00"), 40
        );
        ServiceTypeResponseDto created = new ServiceTypeResponseDto(
                id, "Encerado", "Cera líquida", new BigDecimal("45000.00"), 40, OffsetDateTime.now()
        );

        when(serviceTypeService.create(any(ServiceTypeRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/service-types/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Encerado"));
    }

    @Test
    @DisplayName("POST /api/service-types debe retornar 400 Bad Request cuando los datos son inválidos")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        ServiceTypeRequestDto invalidRequest = new ServiceTypeRequestDto(
                "", null, new BigDecimal("-10.00"), 0
        );

        mockMvc.perform(post("/api/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/service-types/{id} debe actualizar un servicio")
    void shouldUpdateServiceType() throws Exception {
        UUID id = UUID.randomUUID();
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "Lavado Motor", "Vapor", new BigDecimal("55000.00"), 60
        );
        ServiceTypeResponseDto updated = new ServiceTypeResponseDto(
                id, "Lavado Motor", "Vapor", new BigDecimal("55000.00"), 60, OffsetDateTime.now()
        );

        when(serviceTypeService.update(eq(id), any(ServiceTypeRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/service-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lavado Motor"));
    }

    @Test
    @DisplayName("DELETE /api/service-types/{id} debe retornar 204 No Content")
    void shouldDeleteServiceType() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(serviceTypeService).delete(id);

        mockMvc.perform(delete("/api/service-types/{id}", id))
                .andExpect(status().isNoContent());
    }
}
