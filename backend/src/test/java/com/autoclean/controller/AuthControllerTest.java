package com.autoclean.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoclean.config.SecurityConfig;
import com.autoclean.dto.auth.LoginRequest;
import com.autoclean.dto.auth.LoginResponse;
import com.autoclean.dto.auth.RegisterRequest;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.security.JwtService;
import com.autoclean.service.AuthService;
import com.autoclean.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AuthController.class, CustomerController.class})
@Import({SecurityConfig.class, JwtService.class})
@ActiveProfiles("test")
class AuthControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    AuthControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @Test
    void shouldAllowPublicLoginAndReturnToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token-demo", "Bearer", "carlos@autoclean.com", "CLIENTE", null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("carlos@autoclean.com", "secreta123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-demo"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void shouldAllowPublicRegisterAndReturnCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new LoginResponse("jwt-new-user", "Bearer", "nuevo@autoclean.com", "CLIENTE", null));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Nuevo", "3001112233", "nuevo@autoclean.com", "secreta123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-new-user"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void shouldRejectProtectedBusinessEndpointWithoutTokenAndAllowWithValidJwt() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden());

        User domainUser = new User("carlos@autoclean.com", "$2a$10$hash", Role.CLIENTE, null);
        String validToken = jwtService.generateToken(domainUser);
        when(userDetailsService.loadUserByUsername("carlos@autoclean.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("carlos@autoclean.com")
                        .password("$2a$10$hash")
                        .roles("CLIENTE")
                        .build());
        when(customerService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}

