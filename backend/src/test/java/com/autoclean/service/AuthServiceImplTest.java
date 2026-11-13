package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.auth.LoginRequest;
import com.autoclean.dto.auth.LoginResponse;
import com.autoclean.dto.auth.RegisterRequest;
import com.autoclean.model.Customer;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.UserRepository;
import com.autoclean.security.JwtService;
import com.autoclean.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-at-least-32-bytes-long", 3600000L, new ObjectMapper());
        authService = new AuthServiceImpl(authenticationManager, userRepository, customerRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterClienteWithBcryptPasswordAndReturnJwt() {
        Customer savedCustomer = new Customer("Carlos Mendoza", "3104589210", "carlos@autoclean.com");
        savedCustomer.setId(UUID.randomUUID());

        when(userRepository.existsByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(false);
        when(customerRepository.findByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        LoginResponse response = authService.register(
                new RegisterRequest("Carlos Mendoza", "3104589210", "carlos@autoclean.com", "secreta123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();

        assertEquals(Role.CLIENTE, persisted.getRole());
        assertNotEquals("secreta123", persisted.getPasswordHash());
        assertTrue(passwordEncoder.matches("secreta123", persisted.getPasswordHash()));
        assertEquals("Bearer", response.type());
        assertEquals("CLIENTE", response.role());
        assertTrue(jwtService.isTokenValid(response.token(), "carlos@autoclean.com"));
    }

    @Test
    void shouldRejectDuplicateEmailOnRegister() {
        when(userRepository.existsByEmailIgnoreCase("carlos@autoclean.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(
                new RegisterRequest("Carlos", "3104589210", "carlos@autoclean.com", "secreta123")));
    }

    @Test
    void shouldAuthenticateAndReturnJwtOnLogin() {
        User user = new User("admin@autoclean.com", passwordEncoder.encode("admin123"), Role.ADMIN, null);
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmailIgnoreCase("admin@autoclean.com")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("admin@autoclean.com", "admin123"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("admin@autoclean.com", response.email());
        assertEquals("ADMIN", response.role());
        assertTrue(jwtService.isTokenValid(response.token(), "admin@autoclean.com"));
    }
}

