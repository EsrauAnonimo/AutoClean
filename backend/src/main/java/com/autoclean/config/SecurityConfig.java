package com.autoclean.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración base de seguridad HTTP del backend.
 * Protege todos los endpoints por defecto excepto la ruta pública de verificación de salud.
 */
@Configuration
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad para una API REST sin estado (stateless).
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros construida
     * @throws Exception si ocurre un error al configurar la seguridad HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API REST sin sesiones de navegador; se deshabilita CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .anyRequest().authenticated())
                .build();
    }
}
