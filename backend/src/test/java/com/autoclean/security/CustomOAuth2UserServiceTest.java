package com.autoclean.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.model.Customer;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class CustomOAuth2UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Registra automáticamente con rol CLIENTE y contraseña encriptada aleatoria cuando el correo no existe")
    @SuppressWarnings("unchecked")
    void shouldRegisterClientUserWithEncryptedRandomPasswordWhenMissing() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2Delegate = mock(OAuth2UserService.class);
        OAuth2UserService<OidcUserRequest, OidcUser> oidcDelegate = mock(OAuth2UserService.class);
        OAuth2User googlePrincipal = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "google-123", "email", "Nuevo.Cliente@Gmail.com"),
                "email"
        );

        Customer existingCustomer = new Customer("Nuevo Cliente", "+57 3001112233", "nuevo.cliente@gmail.com");
        when(oauth2Delegate.loadUser(any())).thenReturn(googlePrincipal);
        when(userRepository.findByEmailIgnoreCase("nuevo.cliente@gmail.com")).thenReturn(Optional.empty());
        when(customerRepository.findByEmailIgnoreCase("nuevo.cliente@gmail.com"))
                .thenReturn(Optional.of(existingCustomer));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomOAuth2UserService service = new CustomOAuth2UserService(
                userRepository, customerRepository, passwordEncoder, oauth2Delegate, oidcDelegate);

        OAuth2User result = service.loadUser(mock(OAuth2UserRequest.class));

        assertSame(googlePrincipal, result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals("nuevo.cliente@gmail.com", saved.getEmail());
        assertEquals(Role.CLIENTE, saved.getRole());
        assertSame(existingCustomer, saved.getCustomer());
        assertTrue(saved.getPasswordHash().startsWith("$2"));
        assertNotEquals("Nuevo.Cliente@Gmail.com", saved.getPasswordHash());
    }

    @Test
    @DisplayName("No duplica usuario existente al iniciar sesión OIDC con Google")
    @SuppressWarnings("unchecked")
    void shouldReuseExistingUserDuringOidcLogin() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2Delegate = mock(OAuth2UserService.class);
        OAuth2UserService<OidcUserRequest, OidcUser> oidcDelegate = mock(OAuth2UserService.class);

        OidcIdToken idToken = new OidcIdToken(
                "id-token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("sub", "google-456", "email", "luis@autoclean.com")
        );
        OidcUser oidcPrincipal = new DefaultOidcUser(List.of(), idToken, "email");

        User existingAdmin = new User("luis@autoclean.com", "$2a$10$hash", Role.ADMIN, null);
        when(oidcDelegate.loadUser(any())).thenReturn(oidcPrincipal);
        when(userRepository.findByEmailIgnoreCase("luis@autoclean.com")).thenReturn(Optional.of(existingAdmin));

        CustomOAuth2UserService service = new CustomOAuth2UserService(
                userRepository, customerRepository, passwordEncoder, oauth2Delegate, oidcDelegate);

        OidcUser result = service.loadOidcUser(mock(OidcUserRequest.class));

        assertSame(oidcPrincipal, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza el acceso OAuth2 cuando Google no entrega correo")
    void shouldThrowWhenEmailIsMissing() {
        CustomOAuth2UserService service = new CustomOAuth2UserService(
                userRepository, customerRepository, passwordEncoder);

        assertThrows(OAuth2AuthenticationException.class, () -> service.ensureUserRegistered("  "));
    }
}

