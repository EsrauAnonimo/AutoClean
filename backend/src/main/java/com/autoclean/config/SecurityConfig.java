package com.autoclean.config;

import com.autoclean.security.JwtAuthenticationFilter;
import com.autoclean.security.JwtService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST sin sesiones de navegador; se deshabilita CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .anyRequest().authenticated());

        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
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
