package com.autoclean.security;

import com.autoclean.model.User;
import com.autoclean.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Manejador de éxito OAuth2 que emite un JWT propio del sistema y redirige al frontend.
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String redirectUri;

    /**
     * Inyección por constructor de repositorios, emisor JWT y URL de redirección al frontend.
     *
     * @param userRepository repositorio de usuarios para resolver entidad y rol local
     * @param jwtService servicio de emisión de tokens JWT
     * @param redirectUri URL destino del frontend (ej. {@code http://localhost:5173/oauth2/redirect})
     */
    public OAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}") String redirectUri) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String email = extractEmail(authentication);
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario OAuth2 no encontrado: " + normalizedEmail));

        String token = jwtService.generateToken(user);
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    private String extractEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String emailAttribute = oauth2User.getAttribute("email");
            if (emailAttribute != null && !emailAttribute.isBlank()) {
                return emailAttribute;
            }
        }
        String fallbackName = authentication.getName();
        if (fallbackName != null && !fallbackName.isBlank()) {
            return fallbackName;
        }
        throw new IllegalStateException("No fue posible obtener el correo del usuario OAuth2");
    }
}
