package com.autoclean.service.impl;

import com.autoclean.dto.auth.LoginRequest;
import com.autoclean.dto.auth.LoginResponse;
import com.autoclean.dto.auth.RegisterRequest;
import com.autoclean.model.Customer;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.UserRepository;
import com.autoclean.security.JwtService;
import com.autoclean.service.AuthService;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de autenticación con Spring Security, BCrypt y tokens JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Inyección por constructor de los componentes de autenticación y persistencia.
     *
     * @param authenticationManager gestor de autenticación de Spring Security
     * @param userRepository repositorio JPA de usuarios
     * @param customerRepository repositorio JPA de clientes
     * @param passwordEncoder codificador BCrypt de contraseñas
     * @param jwtService servicio de emisión de tokens JWT
     */
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Las credenciales de acceso son obligatorias");
        }
        String normalizedEmail = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + normalizedEmail));

        return toLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de registro son obligatorios");
        }
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el correo: " + normalizedEmail);
        }

        Customer linkedCustomer = resolveCustomerForRegistration(request, normalizedEmail);
        String encodedPassword = passwordEncoder.encode(request.password());
        User savedUser = userRepository.save(new User(normalizedEmail, encodedPassword, Role.CLIENTE, linkedCustomer));
        return toLoginResponse(savedUser);
    }

    private Customer resolveCustomerForRegistration(RegisterRequest request, String normalizedEmail) {
        return customerRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    if (isNotBlank(request.name()) && isNotBlank(request.phone())) {
                        return customerRepository.save(new Customer(request.name(), request.phone(), normalizedEmail));
                    }
                    return null;
                });
    }

    private LoginResponse toLoginResponse(User user) {
        String token = jwtService.generateToken(user);
        UUID customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;
        return new LoginResponse(token, "Bearer", user.getEmail(), user.getRole().name(), customerId);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

