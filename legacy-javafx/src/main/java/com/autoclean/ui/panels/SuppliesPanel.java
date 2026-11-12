package com.autoclean.ui.panels;

import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Panel JavaFX para la visualización y gestión de Insumos de Detailing.
 * Nota: Como el dominio base no incluye persistencia de inventario, este módulo utiliza
 * datos de prueba claramente identificados para demostración visual (según sección 3 y 4).
 */
public class SuppliesPanel extends BorderPane {

    public static class SupplyItem {
        private String code;
        private String name;
        private String category;
        private int stock;
        private double unitCost;
        private String status;

        public SupplyItem(String code, String name, String category, int stock, double unitCost, String status) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.stock = stock;
            this.unitCost = unitCost;
            this.status = status;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public double getUnitCost() { return unitCost; }
        public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    private final SimpleFormPanel formPanel;
    private final TableView<SupplyItem> table;
    private final ObservableList<SupplyItem> suppliesData;

    public SuppliesPanel() {
        this.suppliesData = FXCollections.observableArrayList(
                new SupplyItem("INS-001", "Shampoo Espuma Activa pH Neutro (Galón)", "Químicos de Lavado", 18, 45000.0, "FINISHED"),
                new SupplyItem("INS-002", "Recubrimiento Cerámico 9H 30ml", "Protección Nanotecnológica", 6, 115000.0, "IN_PROGRESS"),
                new SupplyItem("INS-003", "Cera Carnauba Brasileña Grado Show", "Brillo y Sellado", 12, 68000.0, "FINISHED"),
                new SupplyItem("INS-004", "Compuesto Pulidor Corte Pesado 1L", "Corrección de Pintura", 3, 82000.0, "PENDING"),
                new SupplyItem("INS-005", "Paños de Microfibra 600 GSM (Pack x10)", "Accesorios", 25, 38000.0, "FINISHED")
        );
        this.table = new TableView<>(suppliesData);

        getStyleClass().add("content-area");

        // Banner informativo superior identificando los datos de demostración
        HBox demoBanner = new HBox(10);
        demoBanner.getStyleClass().add("info-banner");
        demoBanner.setAlignment(Pos.CENTER_LEFT);
        Label lblBanner = new Label("ℹ Módulo de Insumos (Datos de demostración visual): Referencia de costos unitarios para alimentar el módulo de Costeo.");
        lblBanner.getStyleClass().add("info-banner-text");
        demoBanner.getChildren().add(lblBanner);

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(340);
        formPanel.setMinWidth(310);
        formPanel.setHeader("Bodega de Insumos", "Gestione los productos químicos y materiales de embellecimiento (Demostración).");

        formPanel.addField("Código", "Ej. INS-006");
        formPanel.addField("Nombre Insumo", "Ej. Limpiador de Rines APC");
        formPanel.addField("Categoría", "Ej. Descontaminación");
        formPanel.addField("Stock Disponible", "Cantidad en unidades");
        formPanel.addField("Costo Unitario ($)", "Valor de compra");

        FlowPane buttonsPane = new FlowPane(8, 8);
        Button btnAdd = new Button("＋ Registrar");
        Button btnUpdate = new Button("✎ Actualizar");
        Button btnDelete = new Button("🗑 Eliminar");
        Button btnClear = new Button("Limpiar");

        btnAdd.getStyleClass().add("btn-primary");
        btnUpdate.getStyleClass().add("btn-dark");
        btnDelete.getStyleClass().add("btn-danger");
        btnClear.getStyleClass().add("btn-secondary");

        buttonsPane.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear);
        formPanel.getChildren().add(buttonsPane);

        // Tabla derecha
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        Label lblTitle = new Label("Inventario de Insumos y Productos de Detailing");
        lblTitle.getStyleClass().add("card-title");
        Label lblSub = new Label("Control visual de existencias y costos de referencia por unidad");
        lblSub.getStyleClass().add("card-subtitle");

        configureTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(new VBox(2, lblTitle, lblSub), table);

        HBox centerContent = new HBox(20, formPanel, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        VBox wrapper = new VBox(14, demoBanner, centerContent);
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        setCenter(wrapper);

        // Eventos locales de demostración
        btnAdd.setOnAction(e -> {
            String code = formPanel.getFieldText("Código");
            String name = formPanel.getFieldText("Nombre Insumo");
            String cat = formPanel.getFieldText("Categoría");
            String stockStr = formPanel.getFieldText("Stock Disponible");
            String costStr = formPanel.getFieldText("Costo Unitario ($)");

            if (code.isEmpty() || name.isEmpty() || stockStr.isEmpty() || costStr.isEmpty()) {
                Theme.showWarning("Validación", "Complete código, nombre, stock y costo unitario.");
                return;
            }
            try {
                int stock = Integer.parseInt(stockStr);
                double cost = Double.parseDouble(costStr);
                String status = stock <= 4 ? "PENDING" : "FINISHED";
                suppliesData.add(new SupplyItem(code, name, cat.isEmpty() ? "General" : cat, stock, cost, status));
                formPanel.clear();
                Theme.showInfo("Insumo agregado", "Insumo registrado en la vista de demostración.");
            } catch (NumberFormatException ex) {
                Theme.showWarning("Validación", "Stock y costo deben ser valores numéricos.");
            }
        });

        btnUpdate.setOnAction(e -> {
            SupplyItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                Theme.showWarning("Validación", "Seleccione un insumo de la tabla para actualizarlo.");
                return;
            }
            try {
                sel.setCode(formPanel.getFieldText("Código"));
                sel.setName(formPanel.getFieldText("Nombre Insumo"));
                sel.setCategory(formPanel.getFieldText("Categoría"));
                int st = Integer.parseInt(formPanel.getFieldText("Stock Disponible"));
                sel.setStock(st);
                sel.setUnitCost(Double.parseDouble(formPanel.getFieldText("Costo Unitario ($)")));
                sel.setStatus(st <= 4 ? "PENDING" : "FINISHED");
                table.refresh();
                Theme.showInfo("Actualizado", "Datos del insumo actualizados.");
            } catch (NumberFormatException ex) {
                Theme.showWarning("Validación", "Stock y costo deben ser valores numéricos.");
            }
        });

        btnDelete.setOnAction(e -> {
            SupplyItem sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                suppliesData.remove(sel);
                formPanel.clear();
            } else {
                Theme.showWarning("Validación", "Seleccione una fila para eliminar.");
            }
        });

        btnClear.setOnAction(e -> {
            formPanel.clear();
            table.getSelectionModel().clearSelection();
        });
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SupplyItem, String> colCode = new TableColumn<>("Código");
        colCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        colCode.setMaxWidth(110);

        TableColumn<SupplyItem, String> colName = new TableColumn<>("Producto / Insumo");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colName.setMinWidth(220);

        TableColumn<SupplyItem, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));

        TableColumn<SupplyItem, String> colStock = new TableColumn<>("Unidades");
        colStock.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getStock())));
        colStock.setMaxWidth(95);

        TableColumn<SupplyItem, String> colCost = new TableColumn<>("Costo Unitario");
        colCost.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.0f", cell.getValue().getUnitCost())));

        TableColumn<SupplyItem, String> colStatus = new TableColumn<>("Nivel Stock");
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String st, boolean empty) {
                super.updateItem(st, empty);
                if (empty || st == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label("PENDING".equals(st) ? "STOCK BAJO" : "ÓPTIMO");
                    badge.getStyleClass().addAll("badge", "PENDING".equals(st) ? "badge-pending" : "badge-finished");
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(colCode, colName, colCat, colStock, colCost, colStatus);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) {
                formPanel.setFieldText("Código", sel.getCode());
                formPanel.setFieldText("Nombre Insumo", sel.getName());
                formPanel.setFieldText("Categoría", sel.getCategory());
                formPanel.setFieldText("Stock Disponible", String.valueOf(sel.getStock()));
                formPanel.setFieldText("Costo Unitario ($)", String.valueOf(sel.getUnitCost()));
            }
        });
    }
}

