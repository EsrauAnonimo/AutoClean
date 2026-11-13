package com.autoclean.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.config.SecurityConfig;
import com.autoclean.dto.report.ReportResult;
import com.autoclean.exception.GlobalExceptionHandler;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.security.JwtService;
import com.autoclean.service.ReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas MVC para {@link ReportController} con seguridad JWT y parámetros de fecha.
 */
@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ReportControllerTest {

    private final MockMvc mockMvc;
    private final JwtService jwtService;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    ReportControllerTest(MockMvc mockMvc, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
    }

    @Test
    @DisplayName("GET /api/reports/services y /api/reports/revenue retornan ReportResult con token válido")
    void shouldReturnReportsWhenAuthenticated() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        String token = mockAuthenticatedAdminToken();

        ReportResult servicesReport = new ReportResult(
                "Servicios por periodo (2026-10-01 al 2026-10-31)",
                LocalDateTime.of(2026, 10, 31, 12, 0),
                List.<String[]>of(new String[]{"id-1", "Carlos", "Lavado", "FINISHED", "2026-10-15T10:00Z"})
        );
        ReportResult revenueReport = new ReportResult(
                "Sin datos en el periodo",
                LocalDateTime.of(2026, 10, 31, 12, 0),
                List.of()
        );

        when(reportService.servicesByPeriod(from, to)).thenReturn(servicesReport);
        when(reportService.revenueVsCost(from, to)).thenReturn(revenueReport);

        mockMvc.perform(get("/api/reports/services")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Servicios por periodo (2026-10-01 al 2026-10-31)"))
                .andExpect(jsonPath("$.rows.length()").value(1));

        mockMvc.perform(get("/api/reports/revenue")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sin datos en el periodo"))
                .andExpect(jsonPath("$.rows.length()").value(0));
    }

    @Test
    @DisplayName("Rechaza sin token (403) y retorna 400 cuando el rango de fechas es inválido")
    void shouldRejectUnauthenticatedAndInvalidDateRangeRequests() throws Exception {
        mockMvc.perform(get("/api/reports/services")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31"))
                .andExpect(status().isForbidden());

        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);
        when(reportService.servicesByPeriod(from, to))
                .thenThrow(new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial"));

        String token = mockAuthenticatedAdminToken();
        mockMvc.perform(get("/api/reports/services")
                        .param("from", "2026-10-31")
                        .param("to", "2026-10-01")
                        .header("Authorization", "Bearer " + token))
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

