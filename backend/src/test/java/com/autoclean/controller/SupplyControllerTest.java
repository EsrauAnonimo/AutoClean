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

import com.autoclean.config.SecurityConfig;
import com.autoclean.dto.supply.SupplyRequest;
import com.autoclean.dto.supply.SupplyResponse;
import com.autoclean.exception.GlobalExceptionHandler;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.security.JwtService;
import com.autoclean.service.SupplyService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Pruebas MVC para {@link SupplyController} con seguridad JWT y validación de DTOs.
 */
@WebMvcTest(SupplyController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class SupplyControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @MockitoBean
    private SupplyService supplyService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    SupplyControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @Test
    @DisplayName("GET, POST, PUT y DELETE en /api/supplies funcionan con JWT válido")
    void shouldSupportCrudEndpointsWhenAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();
        SupplyResponse created = new SupplyResponse(id, "Shampoo", 12, "litros", OffsetDateTime.now());
        SupplyResponse updated = new SupplyResponse(id, "Shampoo PH Neutro", 20, "litros", OffsetDateTime.now());

        String token = mockAuthenticatedAdminToken();
        when(supplyService.findAll()).thenReturn(List.of(created));
        when(supplyService.findById(id)).thenReturn(created);
        when(supplyService.create(any(SupplyRequest.class))).thenReturn(created);
        when(supplyService.update(eq(id), any(SupplyRequest.class))).thenReturn(updated);
        doNothing().when(supplyService).delete(id);

        mockMvc.perform(get("/api/supplies").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/supplies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupplyRequest("Shampoo", 12, "litros"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/supplies/" + id))
                .andExpect(jsonPath("$.name").value("Shampoo"));

        mockMvc.perform(put("/api/supplies/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupplyRequest("Shampoo PH Neutro", 20, "litros"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));

        mockMvc.perform(delete("/api/supplies/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Rechaza acceso sin token y rechaza stock negativo con 400")
    void shouldRejectUnauthenticatedAndInvalidStockRequests() throws Exception {
        mockMvc.perform(get("/api/supplies"))
                .andExpect(status().isForbidden());

        String token = mockAuthenticatedAdminToken();
        mockMvc.perform(post("/api/supplies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupplyRequest("Cera", -5, "unidades"))))
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

