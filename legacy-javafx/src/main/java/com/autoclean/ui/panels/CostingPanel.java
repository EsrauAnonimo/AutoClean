package com.autoclean.ui.panels;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CostingService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.ServiceType;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Panel JavaFX para el costeo de servicios y cálculo de diferencia bruta preliminar.
 * Conserva toda la lógica conectada a CostingService e incluye la nota contable obligatoria.
 */
public class CostingPanel extends BorderPane {

    private final CostingService costingService;
    private final ServiceTypeService serviceTypeService;
    private final AppointmentService appointmentService;

    private final SimpleFormPanel headerFormPanel;
    private final ComboBox<ServiceType> cbService;
    private final ComboBox<Appointment> cbAppointment;

    private final TextField txtConcept;
    private final TextField txtAmount;
    private final TableView<CostLine> linesTable;
    private final ObservableList<CostLine> linesData;

    private final TableView<CostEstimate> historyTable;
    private final ObservableList<CostEstimate> historyData;

    private final Label lblPriceValue;
    private final Label lblEstimatedCostValue;
    private final Label lblDifferenceValue;

    public CostingPanel() {
        this.costingService = ServiceFactory.getCostingService();
        this.serviceTypeService = ServiceFactory.getServiceTypeService();
        this.appointmentService = ServiceFactory.getAppointmentService();

        this.cbService = new ComboBox<>();
        this.cbAppointment = new ComboBox<>();
        this.txtConcept = new TextField();
        this.txtAmount = new TextField();
        this.linesData = FXCollections.observableArrayList();
        this.linesTable = new TableView<>(linesData);
        this.historyData = FXCollections.observableArrayList();
        this.historyTable = new TableView<>(historyData);

        this.lblPriceValue = new Label("$0.00");
        this.lblEstimatedCostValue = new Label("$0.00");
        this.lblDifferenceValue = new Label("$0.00");

        getStyleClass().add("content-area");

        // Panel izquierdo: Configuración del servicio y adición de líneas de costo
        headerFormPanel = new SimpleFormPanel();
        headerFormPanel.setPrefWidth(365);
        headerFormPanel.setMinWidth(335);
        headerFormPanel.setHeader("Parámetros de Costeo", "Seleccione el tipo de servicio y agregue cada ítem de costo directo (materiales, insumos, mano de obra).");

        cbService.setPromptText("Seleccionar servicio del catálogo...");
        cbService.setMaxWidth(Double.MAX_VALUE);
        cbService.setOnAction(e -> {
            ServiceType st = cbService.getValue();
            if (st != null) {
                headerFormPanel.setFieldText("ID Tipo Servicio", st.getId());
                lblPriceValue.setText(String.format("$%,.2f", st.getPrice()));
            }
        });
        headerFormPanel.addCustomControl("Selector de Servicio", cbService);
        headerFormPanel.addField("ID Tipo Servicio", "Ej. SRV-001");

        cbAppointment.setPromptText("Vincular a turno existente (opcional)...");
        cbAppointment.setMaxWidth(Double.MAX_VALUE);
        cbAppointment.setOnAction(e -> {
            Appointment app = cbAppointment.getValue();
            if (app != null) {
                headerFormPanel.setFieldText("ID Turno (Opcional)", app.getId());
                headerFormPanel.setFieldText("ID Tipo Servicio", app.getServiceTypeId());
            }
        });
        headerFormPanel.addCustomControl("Selector de Turno", cbAppointment);
        headerFormPanel.addField("ID Turno (Opcional)", "Ej. TRN-001 (Opcional)");

        // Subsección para agregar líneas de costo directo
        Label lblAddSection = new Label("Agregar Línea de Costo Directo");
        lblAddSection.getStyleClass().add("form-section-title");

        txtConcept.setPromptText("Concepto (ej. Shampoo pH Neutro, Mano de obra)");
        txtConcept.getStyleClass().add("text-field");

        txtAmount.setPromptText("Monto en pesos ($) ej. 15000");
        txtAmount.getStyleClass().add("text-field");

        FlowPane lineButtons = new FlowPane(8, 8);
        Button btnAddLine = new Button("＋ Agregar Línea");
        Button btnRemoveLine = new Button("Quitar Selección");
        btnAddLine.getStyleClass().add("btn-dark");
        btnRemoveLine.getStyleClass().add("btn-secondary");
        lineButtons.getChildren().addAll(btnAddLine, btnRemoveLine);

        FlowPane mainActions = new FlowPane(8, 8);
        Button btnEstimate = new Button("🧮 Estimar y Guardar Costeo");
        Button btnClearAll = new Button("Limpiar Todo");
        btnEstimate.getStyleClass().add("btn-primary");
        btnClearAll.getStyleClass().add("btn-secondary");
        mainActions.getChildren().addAll(btnEstimate, btnClearAll);

        headerFormPanel.getChildren().addAll(
                lblAddSection,
                createLabeledControl("Concepto del Ítem", txtConcept),
                createLabeledControl("Monto del Costo ($)", txtAmount),
                lineButtons,
                mainActions
        );

        // Panel central-derecho: Tarjetas de resultado + Tabla de líneas actuales + Historial
        VBox rightColumn = new VBox(16);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        // 1. Tarjetas de métricas calculadas + Advertencia contable obligatoria
        HBox metricsRow = new HBox(14);
        VBox cardPrice = createMetricBox("PRECIO AL PÚBLICO", lblPriceValue, "kpi-value");
        VBox cardCost = createMetricBox("COSTO ESTIMADO", lblEstimatedCostValue, "kpi-value-warning");
        VBox cardDiff = createMetricBox("DIFERENCIA CALCULADA", lblDifferenceValue, "kpi-value-positive");
        HBox.setHgrow(cardPrice, Priority.ALWAYS);
        HBox.setHgrow(cardCost, Priority.ALWAYS);
        HBox.setHgrow(cardDiff, Priority.ALWAYS);
        metricsRow.getChildren().addAll(cardPrice, cardCost, cardDiff);

        HBox warningBox = new HBox(10);
        warningBox.getStyleClass().add("warning-banner");
        warningBox.setAlignment(Pos.CENTER_LEFT);
        Label lblWarning = new Label("⚠ Nota contable importante: La diferencia calculada (precio − costo estimado) es margen bruto preliminar y NO representa ganancia neta.");
        lblWarning.getStyleClass().add("warning-banner-text");
        warningBox.getChildren().add(lblWarning);

        // 2. Tabla de líneas de costo en preparación
        VBox linesCard = new VBox(8);
        linesCard.getStyleClass().add("card");
        VBox.setVgrow(linesCard, Priority.ALWAYS);
        Label lblLinesTitle = new Label("Líneas de Costo Directo de la Estimación Actual");
        lblLinesTitle.getStyleClass().add("card-title");
        configureLinesTable();
        linesCard.getChildren().addAll(lblLinesTitle, linesTable);

        // 3. Tabla histórica de estimaciones registradas en CostingService
        VBox historyCard = new VBox(8);
        historyCard.getStyleClass().add("card");
        VBox.setVgrow(historyCard, Priority.ALWAYS);
        Label lblHistTitle = new Label("Historial de Estimaciones Procesadas");
        lblHistTitle.getStyleClass().add("card-title");
        configureHistoryTable();
        historyCard.getChildren().addAll(lblHistTitle, historyTable);

        rightColumn.getChildren().addAll(metricsRow, warningBox, linesCard, historyCard);

        HBox mainLayout = new HBox(20, headerFormPanel, rightColumn);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        setCenter(mainLayout);

        // Eventos
        btnAddLine.setOnAction(e -> handleAddCostLine());
        btnRemoveLine.setOnAction(e -> handleRemoveSelectedLine());
        btnEstimate.setOnAction(e -> handleEstimateCost());
        btnClearAll.setOnAction(e -> handleClearAll());

        refreshData();
    }

    private VBox createLabeledControl(String label, TextField field) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private VBox createMetricBox(String title, Label valueLabel, String valueStyle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("kpi-card");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-title");
        valueLabel.getStyleClass().add(valueStyle);
        card.getChildren().addAll(lblTitle, valueLabel);
        return card;
    }

    private void configureLinesTable() {
        linesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        linesTable.setPrefHeight(175);
        linesTable.setPlaceholder(new Label("Agregue al menos una línea de costo (ej. insumos, mano de obra) para estimar."));

        TableColumn<CostLine, String> colConcept = new TableColumn<>("Concepto del Costo Directo");
        colConcept.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcept()));

        TableColumn<CostLine, String> colAmount = new TableColumn<>("Monto ($)");
        colAmount.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getAmount())));
        colAmount.setMaxWidth(160);
        colAmount.setMinWidth(120);

        linesTable.getColumns().addAll(colConcept, colAmount);
    }

    private void configureHistoryTable() {
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(185);
        historyTable.setPlaceholder(new Label("No hay estimaciones registradas todavía."));

        TableColumn<CostEstimate, String> colService = new TableColumn<>("Servicio");
        colService.setCellValueFactory(cell -> {
            String sid = cell.getValue().getServiceTypeId();
            ServiceType st = serviceTypeService.findById(sid);
            return new SimpleStringProperty(st != null ? st.getName() + " [" + sid + "]" : sid);
        });

        TableColumn<CostEstimate, String> colApp = new TableColumn<>("Turno Vinculado");
        colApp.setCellValueFactory(cell -> {
            String aid = cell.getValue().getAppointmentId();
            return new SimpleStringProperty(aid != null && !aid.isEmpty() ? aid : "General");
        });
        colApp.setMaxWidth(130);

        TableColumn<CostEstimate, String> colPrice = new TableColumn<>("Precio Público");
        colPrice.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getPrice())));

        TableColumn<CostEstimate, String> colCost = new TableColumn<>("Costo Estimado");
        colCost.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getEstimatedCost())));

        TableColumn<CostEstimate, String> colDiff = new TableColumn<>("Diferencia Bruta");
        colDiff.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getDifference())));

        historyTable.getColumns().addAll(colService, colApp, colPrice, colCost, colDiff);
    }

    private void handleAddCostLine() {
        String concept = txtConcept.getText() != null ? txtConcept.getText().trim() : "";
        String amountStr = txtAmount.getText() != null ? txtAmount.getText().trim() : "";

        if (concept.isEmpty() || amountStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar el concepto y el monto del costo.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                Theme.showWarning("Validación", "El monto no puede ser negativo.");
                return;
            }
            linesData.add(new CostLine(concept, amount));
            txtConcept.clear();
            txtAmount.clear();
            txtConcept.requestFocus();
        } catch (NumberFormatException ex) {
            Theme.showWarning("Validación", "El monto debe ser un valor numérico válido.");
        }
    }

    private void handleRemoveSelectedLine() {
        CostLine selected = linesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            linesData.remove(selected);
        } else {
            Theme.showWarning("Aviso", "Seleccione la fila que desea eliminar de la tabla de líneas.");
        }
    }

    private void handleEstimateCost() {
        String serviceTypeId = headerFormPanel.getFieldText("ID Tipo Servicio");
        String appointmentId = headerFormPanel.getFieldText("ID Turno (Opcional)");

        if (serviceTypeId.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar o seleccionar el ID del Tipo de Servicio para la estimación.");
            return;
        }

        if (linesData.isEmpty()) {
            Theme.showWarning("Validación", "Debe agregar al menos una línea de costo a la lista.");
            return;
        }

        List<CostLine> lines = new ArrayList<>(linesData);

        try {
            CostEstimate estimate;
            if (appointmentId.isEmpty()) {
                estimate = costingService.estimateCost(serviceTypeId, lines);
            } else {
                estimate = costingService.estimateCost(serviceTypeId, appointmentId, lines);
            }

            if (estimate != null) {
                lblEstimatedCostValue.setText(String.format("$%,.2f", estimate.getEstimatedCost()));
                lblPriceValue.setText(String.format("$%,.2f", estimate.getPrice()));
                lblDifferenceValue.setText(String.format("$%,.2f", estimate.getDifference()));
                refreshData();
                Theme.showInfo("Éxito", "Estimación de costo generada y registrada correctamente.");
            }
        } catch (Exception ex) {
            Theme.showError("Error al estimar costo", ex.getMessage());
        }
    }

    private void handleClearAll() {
        headerFormPanel.clear();
        cbService.setValue(null);
        cbAppointment.setValue(null);
        txtConcept.clear();
        txtAmount.clear();
        linesData.clear();
        lblEstimatedCostValue.setText("$0.00");
        lblPriceValue.setText("$0.00");
        lblDifferenceValue.setText("$0.00");
    }

    public final void refreshData() {
        cbService.getItems().setAll(serviceTypeService.findAll());
        cbAppointment.getItems().setAll(appointmentService.findAll());
        historyData.setAll(costingService.findAll());
    }
}
