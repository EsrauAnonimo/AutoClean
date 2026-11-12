package com.autoclean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal del backend Spring Boot de AutoClean.
 */
@SpringBootApplication
public class AutoCleanApplication {

    /**
     * Inicia el contexto de Spring Boot y el servidor web embebido.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AutoCleanApplication.class, args);
    }
}
