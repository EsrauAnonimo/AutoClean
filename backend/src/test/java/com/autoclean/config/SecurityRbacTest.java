package com.autoclean.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.controller.AppointmentController;
import com.autoclean.controller.CostingController;
import com.autoclean.controller.CustomerController;
import com.autoclean.controller.ReportController;
import com.autoclean.controller.ServiceTypeController;
import com.autoclean.controller.SupplyController;
import com.autoclean.dto.report.ReportResult;
import com.autoclean.exception.GlobalExceptionHandler;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.security.JwtService;
import com.autoclean.service.AppointmentService;
import com.autoclean.service.CostingService;
import com.autoclean.service.CustomerService;
import com.autoclean.service.ReportService;
import com.autoclean.service.ServiceTypeService;
import com.autoclean.service.SupplyService;
import java.time.LocalDateTime;
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
 * Pruebas de autorización por roles (RBAC) en {@link SecurityConfig}.
 */
@WebMvcTest({
        ReportController.class,
        CostingController.class,
        SupplyController.class,
        CustomerController.class,
        ServiceTypeController.class,
        AppointmentController.class
})
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class SecurityRbacTest {

    private final MockMvc mockMvc;
    private final JwtService jwtService;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private CostingService costingService;

    @MockitoBean
    private SupplyService supplyService;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private ServiceTypeService serviceTypeService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    SecurityRbacTest(MockMvc mockMvc, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
    }

    @Test
    @DisplayName("CLIENTE no debe acceder a endpoints administrativos de reportes, costeo, insumos o mutaciones")
    void shouldRejectClientRoleOnStaffEndpoints() throws Exception {
        String clientToken = mockToken("cliente@autoclean.com", Role.CLIENTE);
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/reports/services")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-30")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/costing")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/supplies")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/service-types")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Lavado\",\"price\":25000,\"durationMin\":30}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/appointments/{id}/status", id)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENTE puede consultar disponibilidad de horarios y ADMIN/OPERATOR acceden a gestión")
    void shouldAllowStaffOnAdminEndpointsAndClientOnAvailability() throws Exception {
        String clientToken = mockToken("cliente@autoclean.com", Role.CLIENTE);
        String operatorToken = mockToken("operador@autoclean.com", Role.OPERATOR);

        when(appointmentService.findOccupiedSlots(any(), any())).thenReturn(List.of());
        when(costingService.findAll()).thenReturn(List.of());
        when(supplyService.findAll()).thenReturn(List.of());
        when(reportService.servicesByPeriod(any(), any()))
                .thenReturn(new ReportResult("Servicios por periodo", LocalDateTime.now(), List.of()));

        mockMvc.perform(get("/api/appointments/availability")
                        .param("date", "2026-11-15")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/costing")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/supplies")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/services")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-30")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());
    }

    private String mockToken(String email, Role role) {
        User user = new User(email, "$2a$10$hash", role, null);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername(email)
                        .password("$2a$10$hash")
                        .roles(role.name())
                        .build());
        return jwtService.generateToken(user);
    }
}
