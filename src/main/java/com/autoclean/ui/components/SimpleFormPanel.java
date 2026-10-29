package com.autoclean.ui.components;

import com.autoclean.ui.theme.Theme;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Panel de formulario reutilizable que gestiona etiquetas y campos de texto de manera dinámica.
 */
public class SimpleFormPanel extends JPanel {

    private final Map<String, JTextField> fields = new LinkedHashMap<>();
    private final GridBagConstraints gbc = new GridBagConstraints();
    private int currentRow = 0;

    public SimpleFormPanel() {
        setLayout(new GridBagLayout());
        setBackground(Theme.GRIS_FONDO);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.GRIS_BORDE),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    /**
     * Agrega un nuevo campo de texto con su etiqueta descriptiva.
     *
     * @param labelText texto de la etiqueta que identifica el campo
     */
    public void addField(String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BOLD);
        label.setForeground(Theme.AZUL_OSCURO);

        JTextField textField = new JTextField(15);
        textField.setFont(Theme.FONT_REGULAR);

        // Posicionar etiqueta en columna 0
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.weightx = 0.2;
        add(label, gbc);

        // Posicionar campo de texto en columna 1
        gbc.gridx = 1;
        gbc.gridy = currentRow;
        gbc.weightx = 0.8;
        add(textField, gbc);

        fields.put(labelText, textField);
        currentRow++;
    }

    /**
     * Obtiene el texto actual del campo indicado, recortando espacios en blanco.
     *
     * @param labelText nombre de la etiqueta del campo
     * @return texto del campo o cadena vacía si no existe
     */
    public String getFieldText(String labelText) {
        JTextField field = fields.get(labelText);
        return field != null ? field.getText().trim() : "";
    }

    /**
     * Establece el texto del campo indicado.
     *
     * @param labelText nombre de la etiqueta del campo
     * @param text texto a asignar
     */
    public void setFieldText(String labelText, String text) {
        JTextField field = fields.get(labelText);
        if (field != null) {
            field.setText(text != null ? text : "");
        }
    }

    /**
     * Limpia el contenido de todos los campos de texto registrados en el formulario.
     */
    public void clear() {
        for (JTextField field : fields.values()) {
            field.setText("");
        }
    }

    /**
     * Retorna el componente JTextField asociado a una etiqueta.
     */
    public JTextField getTextField(String labelText) {
        return fields.get(labelText);
    }
}
