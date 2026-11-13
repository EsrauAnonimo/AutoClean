package com.autoclean.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.config.SecurityConfig;
import com.autoclean.dto.costing.CostEstimateRequest;
import com.autoclean.dto.costing.CostEstimateResponse;
import com.autoclean.dto.costing.CostLineRequest;
import com.autoclean.dto.costing.CostLineResponse;
import com.autoclean.exception.GlobalExceptionHandler;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.security.JwtService;
import com.autoclean.service.CostingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas MVC para {@link CostingController} con seguridad JWT y validación de DTOs.
 */
@WebMvcTest(CostingController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class CostingControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @MockitoBean
    private CostingService costingService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    CostingControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @Test
    @DisplayName("POST /api/costing/estimate crea estimación y retorna 201 con token válido")
    void shouldEstimateCostAndReturnCreatedWhenAuthenticated() throws Exception {
        UUID serviceTypeId = UUID.randomUUID();
        UUID estimateId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CostEstimateRequest request = new CostEstimateRequest(
                serviceTypeId,
                null,
                List.of(
                        new CostLineRequest("Cera", new BigDecimal("25000.00")),
                        new CostLineRequest("Mano de obra", new BigDecimal("35000.00"))
                )
        );

        CostEstimateResponse response = new CostEstimateResponse(
                estimateId,
                serviceTypeId,
                "Polichado",
                null,
                List.of(
                        new CostLineResponse(UUID.randomUUID(), "Cera", new BigDecimal("25000.00"), now),
                        new CostLineResponse(UUID.randomUUID(), "Mano de obra", new BigDecimal("35000.00"), now)
                ),
                new BigDecimal("60000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("60000.00"),
                now
        );

        String token = mockAuthenticatedAdminToken();
        when(costingService.estimateCost(any(CostEstimateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/costing/estimate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/costing/" + estimateId))
                .andExpect(jsonPath("$.estimatedCost").value(60000.00))
                .andExpect(jsonPath("$.price").value(120000.00))
                .andExpect(jsonPath("$.difference").value(60000.00))
                .andExpect(jsonPath("$.lines.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/costing/estimate rechaza peticiones sin token JWT")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        CostEstimateRequest request = new CostEstimateRequest(UUID.randomUUID(), null, List.of());

        mockMvc.perform(post("/api/costing/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/costing/estimate retorna 400 cuando alguna línea tiene monto negativo")
    void shouldReturnBadRequestWhenCostLineAmountIsNegative() throws Exception {
        String token = mockAuthenticatedAdminToken();
        CostEstimateRequest invalidRequest = new CostEstimateRequest(
                UUID.randomUUID(),
                null,
                List.of(new CostLineRequest("Insumo", new BigDecimal("-100.00")))
        );

        mockMvc.perform(post("/api/costing/estimate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private String mockAuthenticatedAdminToken() {
        User admin = new User("admin@autoclean.com", "$2a$10$hash", Role.ADMIN, null);
        when(userDetailsService.loadUserByUsername("admin@autoclean.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("admin@autoclean.com")
                        .password("$2a$10$hash")
                        .roles("ADMIN")
                        .build());
        return jwtService.generateToken(admin);
    }
}

