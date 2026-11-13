package com.autoclean.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.dto.vehicle.VehicleResponse;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.service.VehicleService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de capa web para {@link VehicleController}.
 */
@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Test
    @DisplayName("GET /api/vehicles/plate/{plate} debe retornar 200 OK si existe o 404 si no")
    void shouldFindByPlateOrReturnNotFound() throws Exception {
        VehicleResponse response = new VehicleResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos",
                "ABC123",
                "Mazda",
                "CX-5",
                2023,
                OffsetDateTime.now()
        );
        when(vehicleService.findByPlate("ABC123")).thenReturn(response);
        when(vehicleService.findByPlate("NOPE99"))
                .thenThrow(new ResourceNotFoundException("No se encontró un vehículo con la placa: NOPE99"));

        mockMvc.perform(get("/api/vehicles/plate/{plate}", "ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"))
                .andExpect(jsonPath("$.brand").value("Mazda"));

        mockMvc.perform(get("/api/vehicles/plate/{plate}", "NOPE99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

