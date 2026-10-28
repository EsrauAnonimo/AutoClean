package com.autoclean.ui.theme;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Paleta de colores, fuentes y utilidades de estilo visual para la interfaz Swing.
 */
public final class Theme {

    // Colores corporativos solicitados
    public static final Color AZUL_OSCURO = new Color(0x0B2545);
    public static final Color TURQUESA = new Color(0x13A89E);
    public static final Color BLANCO = Color.WHITE;
    public static final Color GRIS_FONDO = new Color(0xF4F6F9);
    public static final Color GRIS_TEXTO = new Color(0x333333);
    public static final Color GRIS_BORDE = new Color(0xCCCCCC);

    // Tipografías
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private Theme() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Aplica el estilo visual primario (turquesa) a un botón Swing.
     */
    public static void styleButton(JButton button) {
        button.setBackground(TURQUESA);
        button.setForeground(BLANCO);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Aplica el estilo visual secundario (azul oscuro) a un botón Swing.
     */
    public static void styleSecondaryButton(JButton button) {
        button.setBackground(AZUL_OSCURO);
        button.setForeground(BLANCO);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Aplica estilo moderno a una tabla Swing y a su encabezado.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(26);
        table.setGridColor(new Color(0xE0E0E0));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(0xD0EAE7));
        table.setSelectionForeground(AZUL_OSCURO);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(AZUL_OSCURO);
        header.setForeground(BLANCO);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        header.setReorderingAllowed(false);
    }

    /**
     * Aplica estilo a una etiqueta de título de sección.
     */
    public static void styleTitleLabel(JLabel label) {
        label.setFont(FONT_TITLE);
        label.setForeground(AZUL_OSCURO);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
    }
}
