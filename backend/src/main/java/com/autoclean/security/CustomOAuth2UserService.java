package com.autoclean.security;

import com.autoclean.model.Customer;
import com.autoclean.model.Role;
import com.autoclean.model.User;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio OAuth2 que intercepta usuarios autenticados por Google y aprovisiona cuentas
 * locales con rol {@link Role#CLIENTE} cuando aún no existen en la base de datos.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2Delegate;
    private final OAuth2UserService<OidcUserRequest, OidcUser> oidcDelegate;

    /**
     * Constructor principal usado por Spring para delegar en los servicios estándar OAuth2 y OIDC.
     */
    @Autowired
    public CustomOAuth2UserService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this(userRepository, customerRepository, passwordEncoder, null, new OidcUserService());
    }

    /**
     * Constructor de pruebas unitarias para inyectar delegados OAuth2/OIDC simulados.
     */
    CustomOAuth2UserService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2Delegate,
            OAuth2UserService<OidcUserRequest, OidcUser> oidcDelegate) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.oauth2Delegate = oauth2Delegate;
        this.oidcDelegate = oidcDelegate;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = oauth2Delegate != null
                ? oauth2Delegate.loadUser(userRequest)
                : super.loadUser(userRequest);
        ensureUserRegistered(oauth2User.getAttribute("email"));
        return oauth2User;
    }

    /**
     * Procesa inicios de sesión OIDC de Google (cuando el alcance incluye {@code openid}).
     *
     * @param userRequest solicitud OIDC entrante
     * @return usuario OIDC autenticado tras asegurar su registro local
     */
    @Transactional
    public OidcUser loadOidcUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = oidcDelegate.loadUser(userRequest);
        ensureUserRegistered(oidcUser.getEmail());
        return oidcUser;
    }

    /**
     * Registra automáticamente el correo con rol {@link Role#CLIENTE} y contraseña aleatoria
     * encriptada si todavía no existe en {@code usuarios}.
     */
    User ensureUserRegistered(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email", "El proveedor Google no devolvió un correo electrónico válido", null)
            );
        }

        String normalizedEmail = rawEmail.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    Customer linkedCustomer = customerRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
                    String randomEncodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    return userRepository.save(new User(normalizedEmail, randomEncodedPassword, Role.CLIENTE, linkedCustomer));
                });
    }
}

