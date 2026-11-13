package com.autoclean.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.controller.HealthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración MVC para verificar las cabeceras CORS en {@link SecurityConfig}.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityCorsTest {

    private final MockMvc mockMvc;

    @Autowired
    SecurityCorsTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("Preflight CORS permite localhost:3000, localhost:5173 y *.vercel.app con credenciales y Authorization")
    void shouldAllowConfiguredCorsOriginsAndHeaders() throws Exception {
        String[] allowedOrigins = {
                "http://localhost:3000",
                "http://localhost:5173",
                "https://autoclean-frontend.vercel.app"
        };

        for (String origin : allowedOrigins) {
            mockMvc.perform(options("/api/auth/login")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }
    }

    @Test
    @DisplayName("Preflight CORS rechaza orígenes no autorizados")
    void shouldRejectDisallowedCorsOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://untrusted.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}

