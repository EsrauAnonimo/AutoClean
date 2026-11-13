package com.autoclean.config;

import com.autoclean.security.CustomOAuth2UserService;
import com.autoclean.security.JwtAuthenticationFilter;
import com.autoclean.security.JwtService;
import com.autoclean.security.OAuth2SuccessHandler;
import jakarta.servlet.DispatcherType;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad HTTP del backend con autenticación stateless mediante JWT y BCrypt.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Construye el filtro JWT cuando los servicios requeridos están presentes en el contexto.
     *
     * <p>Se instancia aquí (y no como bean servlet global) para evitar doble ejecución del filtro
     * y permitir slices {@code @WebMvcTest} mínimos como el de healthcheck.
     *
     * @param jwtServiceProvider proveedor del servicio JWT
     * @param userDetailsServiceProvider proveedor del servicio de usuarios
     */
    public SecurityConfig(
            ObjectProvider<JwtService> jwtServiceProvider,
            ObjectProvider<UserDetailsService> userDetailsServiceProvider) {
        JwtService jwtService = jwtServiceProvider.getIfAvailable();
        UserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable();
        this.jwtAuthenticationFilter = (jwtService != null && userDetailsService != null)
                ? new JwtAuthenticationFilter(jwtService, userDetailsService)
                : null;
    }

    /**
     * Define la cadena de filtros de seguridad para una API REST sin estado (stateless).
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros construida
     * @throws Exception si ocurre un error al configurar la seguridad HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            ObjectProvider<CustomOAuth2UserService> customOAuth2UserServiceProvider,
            ObjectProvider<OAuth2SuccessHandler> oAuth2SuccessHandlerProvider) throws Exception {
        http
                // Habilita CORS para el frontend local y despliegues en Vercel antes de validar JWT
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // API REST sin sesiones de navegador; se deshabilita CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permite forwards internos de error (/error) para no enmascarar 400/401/500 como 403
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**", "/error", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .anyRequest().authenticated())
                // Mantiene 403 en llamadas /api/** sin token en vez de redirigir al login OAuth2
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.FORBIDDEN),
                        request -> request.getRequestURI().startsWith("/api/")
                ));

        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            CustomOAuth2UserService customOAuth2UserService = customOAuth2UserServiceProvider.getIfAvailable();
            OAuth2SuccessHandler oAuth2SuccessHandler = oAuth2SuccessHandlerProvider.getIfAvailable();

            http.oauth2Login(oauth2 -> {
                if (customOAuth2UserService != null) {
                    oauth2.userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOAuth2UserService::loadOidcUser));
                }
                if (oAuth2SuccessHandler != null) {
                    oauth2.successHandler(oAuth2SuccessHandler);
                }
            });
        }

        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    /**
     * Configura los orígenes, métodos y encabezados permitidos para peticiones cross-origin (CORS).
     *
     * @return fuente de configuración CORS aplicada a todos los endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Codificador de contraseñas con BCrypt.
     *
     * @return instancia estándar de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el {@link AuthenticationManager} configurado por Spring Security.
     *
     * @param configuration configuración de autenticación
     * @return gestor de autenticación
     * @throws Exception si ocurre un error al obtener el gestor
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
