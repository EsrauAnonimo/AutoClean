package com.autoclean.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class OAuth2SuccessHandlerTest {

    @Test
    @DisplayName("Emite JWT propio y redirige al frontend con el token en la URL")
    void shouldGenerateJwtAndRedirectToFrontendWithToken() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = new JwtService(
                "clave-secreta-de-pruebas-oauth2-autoclean-2026",
                3_600_000L,
                new ObjectMapper()
        );

        User persistedUser = new User("cliente.google@gmail.com", "$2a$10$hash", Role.CLIENTE, null);
        persistedUser.setId(UUID.randomUUID());
        when(userRepository.findByEmailIgnoreCase("cliente.google@gmail.com"))
                .thenReturn(Optional.of(persistedUser));

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
                userRepository,
                jwtService,
                "http://localhost:5173/oauth2/redirect"
        );

        OAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("email", "Cliente.Google@Gmail.com"),
                "email"
        );
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        String redirectedUrl = response.getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.startsWith("http://localhost:5173/oauth2/redirect?token="));

        String token = redirectedUrl.substring("http://localhost:5173/oauth2/redirect?token=".length());
        assertEquals("cliente.google@gmail.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "cliente.google@gmail.com"));
    }
}

