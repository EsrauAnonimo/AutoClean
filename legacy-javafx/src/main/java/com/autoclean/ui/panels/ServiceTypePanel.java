package com.autoclean.ui.panels;

import com.autoclean.contracts.ServiceTypeService;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel JavaFX para la administración del catálogo de tipos de servicio (CRUD
 * completo).
 * Conserva toda la lógica de validación conectada a ServiceTypeService.
 */
public class ServiceTypePanel extends BorderPane {

    private final ServiceTypeService serviceTypeService;
    private final SimpleFormPanel formPanel;
    private final TableView<ServiceType> table;
    private final ObservableList<ServiceType> tableData;
    private final TextField txtFilter;

    public ServiceTypePanel() {
        this.serviceTypeService = ServiceFactory.getServiceTypeService();
        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);
        this.txtFilter = new TextField();

        getStyleClass().add("content-area");

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(350);
        formPanel.setMinWidth(320);
        formPanel.setHeader("Catálogo de Servicios",
                "Configure los paquetes de lavado, detallado y protección con su precio público.");

        formPanel.addField("ID", "Autogenerado o ej. SRV-005");
        formPanel.addField("Nombre", "Ej. Lavado Detallado Premium");
        formPanel.addField("Descripción", "Detalle de procesos incluidos");
        formPanel.addField("Precio", "Valor base en pesos ($)");

        FlowPane buttonsPane = new FlowPane(8, 8);
        Button btnCreate = new Button("＋ Crear");
        Button btnFind = new Button("🔍 Consultar ID");
        Button btnUpdate = new Button("✎ Actualizar");
        Button btnDelete = new Button("🗑 Eliminar");
        Button btnList = new Button("↻ Listar");
        Button btnClear = new Button("Limpiar");

        btnCreate.getStyleClass().add("btn-primary");
        btnFind.getStyleClass().add("btn-secondary");
        btnUpdate.getStyleClass().add("btn-dark");
        btnDelete.getStyleClass().add("btn-danger");
        btnList.getStyleClass().add("btn-secondary");
        btnClear.getStyleClass().add("btn-secondary");

        buttonsPane.getChildren().addAll(btnCreate, btnUpdate, btnFind, btnDelete, btnList, btnClear);
        formPanel.getChildren().add(buttonsPane);

        // Tabla derecha
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Portafolio de Tipos de Servicio");
        lblTitle.getStyleClass().add("card-title");
        Label lblSub = new Label("Tarifario oficial para agendamiento de turnos y cálculo de costeo");
        lblSub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtFilter.setPromptText("Buscar servicio por nombre o descripción...");
        txtFilter.setPrefWidth(260);
        txtFilter.getStyleClass().add("text-field");
        txtFilter.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));

        tableHeader.getChildren().addAll(titleBox, spacer, txtFilter);

        configureTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableHeader, table);

        HBox mainLayout = new HBox(20, formPanel, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        setCenter(mainLayout);

        // Eventos
        btnCreate.setOnAction(e -> handleCreate());
        btnFind.setOnAction(e -> handleFindById());
        btnUpdate.setOnAction(e -> handleUpdate());
        btnDelete.setOnAction(e -> handleDelete());
        btnList.setOnAction(e -> loadServices());
        btnClear.setOnAction(e -> {
            formPanel.clear();
            table.getSelectionModel().clearSelection();
        });

        loadServices();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay servicios registrados en el catálogo."));

        TableColumn<ServiceType, String> colId = new TableColumn<>("ID Servicio");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(120);
        colId.setMinWidth(90);

        TableColumn<ServiceType, String> colName = new TableColumn<>("Nombre del Servicio");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colName.setMinWidth(180);

        TableColumn<ServiceType, String> colDesc = new TableColumn<>("Descripción Técnica");
        colDesc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colDesc.setMinWidth(220);

        TableColumn<ServiceType, String> colPrice = new TableColumn<>("Precio Público ($)");
        colPrice.setCellValueFactory(
                cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getPrice())));
        colPrice.setMaxWidth(150);
        colPrice.setMinWidth(120);

        table.getColumns().addAll(colId, colName, colDesc, colPrice);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                formPanel.setFieldText("ID", selected.getId());
                formPanel.setFieldText("Nombre", selected.getName());
                formPanel.setFieldText("Descripción", selected.getDescription());
                formPanel.setFieldText("Precio", String.valueOf(selected.getPrice()));
            }
        });
    }

    private void handleCreate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String description = formPanel.getFieldText("Descripción");
        String priceStr = formPanel.getFieldText("Precio");

        if (name.isEmpty() || priceStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar el nombre y el precio del servicio.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            Theme.showWarning("Validación", "El precio debe ser un número positivo válido.");
            return;
        }

        try {
            ServiceType serviceType = new ServiceType(id.isEmpty() ? null : id, name, description, price);
            serviceTypeService.create(serviceType);
            Theme.showInfo("Éxito", "Servicio registrado exitosamente.");
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            Theme.showError("Error al crear servicio", ex.getMessage());
        }
    }

    private void handleFindById() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Ingrese el ID del servicio a consultar.");
            return;
        }

        ServiceType found = serviceTypeService.findById(id);
        if (found == null) {
            Theme.showWarning("Sin resultados", "No existe un servicio con el ID: " + id);
            return;
        }

        formPanel.setFieldText("ID", found.getId());
        formPanel.setFieldText("Nombre", found.getName());
        formPanel.setFieldText("Descripción", found.getDescription());
        formPanel.setFieldText("Precio", String.valueOf(found.getPrice()));
        table.getSelectionModel().select(found);
    }

    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String description = formPanel.getFieldText("Descripción");
        String priceStr = formPanel.getFieldText("Precio");

        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe indicar o seleccionar el ID a actualizar.");
            return;
        }
        if (name.isEmpty() || priceStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar el nombre y el precio del servicio.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            Theme.showWarning("Validación", "El precio debe ser un número positivo válido.");
            return;
        }

        try {
            ServiceType serviceType = new ServiceType(id, name, description, price);
            serviceTypeService.update(serviceType);
            Theme.showInfo("Éxito", "Servicio actualizado exitosamente.");
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            Theme.showError("Error al actualizar servicio", ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe indicar o seleccionar el ID a eliminar.");
            return;
        }

        if (!Theme.showConfirm("Confirmar eliminación", "¿Está seguro de eliminar el servicio #" + id + "?")) {
            return;
        }

        try {
            serviceTypeService.delete(id);
            Theme.showInfo("Éxito", "Servicio eliminado exitosamente.");
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            Theme.showError("Error al eliminar servicio", ex.getMessage());
        }
    }

    public final void loadServices() {
        try {
            List<ServiceType> list = serviceTypeService.findAll();
            tableData.setAll(list);
            applyFilter(txtFilter.getText());
        } catch (Exception ex) {
            Theme.showError("Error al consultar servicios", ex.getMessage());
        }
    }

    private void applyFilter(String query) {
        List<ServiceType> all = serviceTypeService.findAll();
        if (query == null || query.trim().isEmpty()) {
            tableData.setAll(all);
            return;
        }
        String q = query.trim().toLowerCase();
        List<ServiceType> filtrados = new ArrayList<>();
        for (ServiceType s : all) {
            boolean coincideId = s.getId() != null && s.getId().toLowerCase().contains(q);
            boolean coincideNombre = s.getName() != null && s.getName().toLowerCase().contains(q);
            boolean coincideDesc = s.getDescription() != null && s.getDescription().toLowerCase().contains(q);

            if (coincideId || coincideNombre || coincideDesc) {
                filtrados.add(s);
            }
        }
        tableData.setAll(filtrados);
    }
}
