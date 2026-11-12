package com.autoclean.ui.components;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Componente de formulario reutilizable en JavaFX que gestiona etiquetas,
 * campos de entrada y controles personalizados manteniendo el diseño corporativo.
 */
public class SimpleFormPanel extends VBox {

    private final Map<String, TextField> fields = new LinkedHashMap<>();

    public SimpleFormPanel() {
        setSpacing(10);
        getStyleClass().add("form-card");
    }

    /**
     * Agrega un título y subtítulo descriptivo en la cabecera del formulario.
     */
    public void setHeader(String title, String subtitle) {
        VBox headerBox = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("form-section-title");
        headerBox.getChildren().add(lblTitle);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            Label lblSub = new Label(subtitle);
            lblSub.getStyleClass().add("form-section-subtitle");
            lblSub.setWrapText(true);
            headerBox.getChildren().add(lblSub);
        }
        getChildren().add(0, headerBox);
    }

    /**
     * Agrega un campo de texto con su etiqueta descriptiva.
     *
     * @param labelText nombre identificador del campo
     */
    public void addField(String labelText) {
        addField(labelText, "");
    }

    /**
     * Agrega un campo de texto con etiqueta y texto de sugerencia (placeholder).
     *
     * @param labelText nombre identificador del campo
     * @param promptText texto guía dentro del campo
     */
    public void addField(String labelText, String promptText) {
        VBox group = new VBox(4);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");

        group.getChildren().addAll(label, textField);
        getChildren().add(group);
        fields.put(labelText, textField);
    }

    /**
     * Agrega un control personalizado de JavaFX (ej. ComboBox, DatePicker) con etiqueta.
     */
    public void addCustomControl(String labelText, Node control) {
        VBox group = new VBox(4);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        group.getChildren().addAll(label, control);
        getChildren().add(group);
    }

    /**
     * Obtiene el valor actual del campo sin espacios laterales.
     */
    public String getFieldText(String labelText) {
        TextField field = fields.get(labelText);
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    /**
     * Asigna un texto al campo indicado.
     */
    public void setFieldText(String labelText, String text) {
        TextField field = fields.get(labelText);
        if (field != null) {
            field.setText(text != null ? text : "");
        }
    }

    /**
     * Limpia todos los campos de texto del formulario.
     */
    public void clear() {
        for (TextField field : fields.values()) {
            field.clear();
        }
    }

    /**
     * Retorna la instancia de TextField asociada a una etiqueta.
     */
    public TextField getTextField(String labelText) {
        return fields.get(labelText);
    }
}
