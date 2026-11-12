package com.autoclean.ui.panels;

import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Customer;
import com.autoclean.model.Vehicle;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel JavaFX para la administración de vehículos (CRUD completo).
 * Conserva toda la lógica conectada a VehicleService e incorpora selector
 * auxiliar de clientes.
 */
public class VehiclePanel extends BorderPane {

    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final SimpleFormPanel formPanel;
    private final ComboBox<Customer> customerSelector;
    private final TableView<Vehicle> table;
    private final ObservableList<Vehicle> tableData;
    private final TextField txtFilter;

    public VehiclePanel() {
        this.vehicleService = ServiceFactory.getVehicleService();
        this.customerService = ServiceFactory.getCustomerService();
        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);
        this.customerSelector = new ComboBox<>();
        this.txtFilter = new TextField();

        getStyleClass().add("content-area");

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(350);
        formPanel.setMinWidth(320);
        formPanel.setHeader("Registro de Vehículo",
                "Vincule cada automóvil a su propietario e ingrese placa, marca, línea y modelo.");

        formPanel.addField("ID", "Autogenerado o ej. VEH-005");

        customerSelector.setPromptText("Seleccionar propietario registrado...");
        customerSelector.setMaxWidth(Double.MAX_VALUE);
        customerSelector.setOnAction(e -> {
            Customer selected = customerSelector.getValue();
            if (selected != null) {
                formPanel.setFieldText("ID Cliente", selected.getId());
            }
        });
        formPanel.addCustomControl("Selector Rápido de Cliente", customerSelector);

        formPanel.addField("ID Cliente", "ID del propietario (ej. CLI-001)");
        formPanel.addField("Placa", "Matrícula (ej. ABC-123)");
        formPanel.addField("Marca", "Ej. Mazda, Toyota, BMW");
        formPanel.addField("Modelo", "Ej. CX-30, Corolla, Serie 3");
        formPanel.addField("Año", "Año modelo (1900 - 2100)");

        // Botones de acción
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
        Label lblTitle = new Label("Parque Automotor Registrado");
        lblTitle.getStyleClass().add("card-title");
        Label lblSub = new Label("Haga clic en un vehículo para cargar sus datos o filtrar por placa/cliente");
        lblSub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtFilter.setPromptText("Buscar por placa, marca o cliente...");
        txtFilter.setPrefWidth(250);
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
        btnList.setOnAction(e -> loadVehicles());
        btnClear.setOnAction(e -> {
            formPanel.clear();
            customerSelector.setValue(null);
            table.getSelectionModel().clearSelection();
        });

        loadVehicles();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay vehículos registrados en el sistema."));

        TableColumn<Vehicle, String> colId = new TableColumn<>("ID Vehículo");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(120);
        colId.setMinWidth(90);

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Placa");
        colPlate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlate()));
        colPlate.setMaxWidth(110);
        colPlate.setMinWidth(90);

        TableColumn<Vehicle, String> colBrand = new TableColumn<>("Marca");
        colBrand.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBrand()));

        TableColumn<Vehicle, String> colModel = new TableColumn<>("Línea / Modelo");
        colModel.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModel()));
        colModel.setMinWidth(150);

        TableColumn<Vehicle, String> colYear = new TableColumn<>("Año");
        colYear.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getYear())));
        colYear.setMaxWidth(85);
        colYear.setMinWidth(70);

        TableColumn<Vehicle, String> colOwner = new TableColumn<>("Propietario (ID Cliente)");
        colOwner.setCellValueFactory(cell -> {
            String cid = cell.getValue().getCustomerId();
            Customer c = customerService.findById(cid);
            return new SimpleStringProperty(c != null ? c.getName() + " [" + cid + "]" : cid);
        });
        colOwner.setMinWidth(180);

        table.getColumns().addAll(colId, colPlate, colBrand, colModel, colYear, colOwner);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                formPanel.setFieldText("ID", selected.getId());
                formPanel.setFieldText("ID Cliente", selected.getCustomerId());
                formPanel.setFieldText("Placa", selected.getPlate());
                formPanel.setFieldText("Marca", selected.getBrand());
                formPanel.setFieldText("Modelo", selected.getModel());
                formPanel.setFieldText("Año", String.valueOf(selected.getYear()));
            }
        });
    }

    private void handleCreate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String plate = formPanel.getFieldText("Placa");
        String brand = formPanel.getFieldText("Marca");
        String model = formPanel.getFieldText("Modelo");
        String yearStr = formPanel.getFieldText("Año");

        if (customerId.isEmpty() || plate.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe completar todos los datos del vehículo.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ex) {
            Theme.showWarning("Validación", "El año debe ser un número entero válido.");
            return;
        }

        try {
            Vehicle vehicle = new Vehicle(id.isEmpty() ? null : id, customerId, plate, brand, model, year);
            vehicleService.create(vehicle);
            Theme.showInfo("Éxito", "Vehículo registrado exitosamente.");
            formPanel.clear();
            customerSelector.setValue(null);
            loadVehicles();
        } catch (Exception ex) {
            Theme.showError("Error al crear vehículo", ex.getMessage());
        }
    }

    private void handleFindById() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Ingrese el ID del vehículo a consultar.");
            return;
        }

        Vehicle found = vehicleService.findById(id);
        if (found == null) {
            Theme.showWarning("Sin resultados", "No existe un vehículo con el ID: " + id);
            return;
        }

        formPanel.setFieldText("ID", found.getId());
        formPanel.setFieldText("ID Cliente", found.getCustomerId());
        formPanel.setFieldText("Placa", found.getPlate());
        formPanel.setFieldText("Marca", found.getBrand());
        formPanel.setFieldText("Modelo", found.getModel());
        formPanel.setFieldText("Año", String.valueOf(found.getYear()));
        table.getSelectionModel().select(found);
    }

    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String plate = formPanel.getFieldText("Placa");
        String brand = formPanel.getFieldText("Marca");
        String model = formPanel.getFieldText("Modelo");
        String yearStr = formPanel.getFieldText("Año");

        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar o seleccionar el ID a actualizar.");
            return;
        }
        if (customerId.isEmpty() || plate.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe completar todos los datos del vehículo.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ex) {
            Theme.showWarning("Validación", "El año debe ser un número entero válido.");
            return;
        }

        try {
            Vehicle vehicle = new Vehicle(id, customerId, plate, brand, model, year);
            vehicleService.update(vehicle);
            Theme.showInfo("Éxito", "Vehículo actualizado exitosamente.");
            formPanel.clear();
            customerSelector.setValue(null);
            loadVehicles();
        } catch (Exception ex) {
            Theme.showError("Error al actualizar vehículo", ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar o seleccionar el ID a eliminar.");
            return;
        }

        if (!Theme.showConfirm("Confirmar eliminación", "¿Está seguro de eliminar el vehículo #" + id + "?")) {
            return;
        }

        try {
            vehicleService.delete(id);
            Theme.showInfo("Éxito", "Vehículo eliminado exitosamente.");
            formPanel.clear();
            customerSelector.setValue(null);
            loadVehicles();
        } catch (Exception ex) {
            Theme.showError("Error al eliminar vehículo", ex.getMessage());
        }
    }

    public final void loadVehicles() {
        try {
            customerSelector.getItems().setAll(customerService.findAll());
            List<Vehicle> list = vehicleService.findAll();
            tableData.setAll(list);
            applyFilter(txtFilter.getText());
        } catch (Exception ex) {
            Theme.showError("Error al consultar vehículos", ex.getMessage());
        }
    }

    private void applyFilter(String query) {
        List<Vehicle> all = vehicleService.findAll();
        if (query == null || query.trim().isEmpty()) {
            tableData.setAll(all);
            return;
        }
        String q = query.trim().toLowerCase();
        List<Vehicle> filtrados = new ArrayList<>();
        for (Vehicle v : all) {
            boolean coincideId = v.getId() != null && v.getId().toLowerCase().contains(q);
            boolean coincidePlaca = v.getPlate() != null && v.getPlate().toLowerCase().contains(q);
            boolean coincideMarca = v.getBrand() != null && v.getBrand().toLowerCase().contains(q);
            boolean coincideModelo = v.getModel() != null && v.getModel().toLowerCase().contains(q);
            boolean coincideCliente = v.getCustomerId() != null && v.getCustomerId().toLowerCase().contains(q);

            if (coincideId || coincidePlaca || coincideMarca || coincideModelo || coincideCliente) {
                filtrados.add(v);
            }
        }
        tableData.setAll(filtrados);
    }
}
