package com.autoclean.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.autoclean.model.Customer;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService jwtService = new JwtService(SECRET, 3600000L, objectMapper);
        Customer customer = new Customer("Carlos Mendoza", "3104589210", "carlos@autoclean.com");
        customer.setId(UUID.randomUUID());
        User user = new User("carlos@autoclean.com", "$2a$10$hash", Role.CLIENTE, customer);
        user.setId(UUID.randomUUID());

        String token = jwtService.generateToken(user);

        assertEquals(3, token.split("\\.").length);
        assertEquals("carlos@autoclean.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "carlos@autoclean.com"));
        assertFalse(jwtService.isTokenValid(token, "otro@autoclean.com"));
    }

    @Test
    void shouldRejectTamperedOrMalformedToken() {
        JwtService jwtService = new JwtService(SECRET, 3600000L, objectMapper);
        User user = new User("admin@autoclean.com", "$2a$10$hash", Role.ADMIN, null);

        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertFalse(jwtService.isTokenValid(tampered, "admin@autoclean.com"));
        assertFalse(jwtService.isTokenValid("token-invalido", "admin@autoclean.com"));
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractEmail(tampered));
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 3600000L, objectMapper);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString(objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
        long pastExp = Instant.now().minusSeconds(60).getEpochSecond();
        String payload = encoder.encodeToString(objectMapper.writeValueAsBytes(Map.of("sub", "carlos@autoclean.com", "exp", pastExp)));
        String signingInput = header + "." + payload;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String sig = encoder.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        String expiredToken = signingInput + "." + sig;

        assertFalse(jwtService.isTokenValid(expiredToken, "carlos@autoclean.com"));
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractEmail(expiredToken));
    }
}

