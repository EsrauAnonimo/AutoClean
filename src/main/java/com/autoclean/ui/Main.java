package com.autoclean.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada principal de la aplicación Swing AutoClean.
 */
public class Main {

    public static void main(String[] args) {
        // Intentar aplicar el Look and Feel nativo del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, Swing utilizará el estilo predeterminado Metal/Ocean sin interrumpir la ejecución
        }

        // Iniciar la interfaz gráfica de forma segura dentro del Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
