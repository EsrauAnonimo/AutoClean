package com.autoclean.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador HTTP de verificación de salud del backend.
 * Aplica SRP: únicamente expone el estado operativo básico del servicio.
 */
@RestController
public class HealthController {

    /**
     * Constructor explícito para mantener el estándar de inyección por constructor
     * y permitir extender indicadores de salud sin cambiar los contratos HTTP.
     */
    public HealthController() {
        // Sin dependencias requeridas en el esqueleto inicial
    }

    /**
     * Retorna el estado básico de disponibilidad del servicio.
     *
     * @return mapa JSON con clave status y valor ok
     */
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
