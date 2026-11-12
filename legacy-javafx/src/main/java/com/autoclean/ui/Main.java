package com.autoclean.ui;

import javafx.application.Application;

/**
 * Punto de entrada principal de la aplicación de escritorio AutoClean.
 * Lanza el entorno gráfico moderno en JavaFX (MainFrame) de forma compatible
 * tanto desde el IDE directamente como mediante Maven.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(MainFrame.class, args);
    }
}
