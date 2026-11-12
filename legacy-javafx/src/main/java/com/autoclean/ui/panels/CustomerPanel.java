package com.autoclean.ui.panels;

import com.autoclean.contracts.CustomerService;
import com.autoclean.model.Customer;
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
 * Panel JavaFX para la administración de clientes (CRUD completo y búsqueda).
 * Conserva toda la lógica existente conectada a CustomerService.
 */
public class CustomerPanel extends BorderPane {

    private final CustomerService customerService;
    private final SimpleFormPanel formPanel;
    private final TableView<Customer> table;
    private final ObservableList<Customer> tableData;
    private final TextField txtFilter;

    public CustomerPanel() {
        this.customerService = ServiceFactory.getCustomerService();
        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);
        this.txtFilter = new TextField();

        getStyleClass().add("content-area");

        // Panel izquierdo: Formulario de registro y edición
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(340);
        formPanel.setMinWidth(310);
        formPanel.setHeader("Datos del Cliente",
                "Complete la información para registrar, consultar o actualizar un propietario.");
        formPanel.addField("ID", "Autogenerado o ej. CLI-005");
        formPanel.addField("Nombre", "Nombre completo del cliente");
        formPanel.addField("Teléfono", "Ej. 3104567890");
        formPanel.addField("Correo", "cliente@correo.com");

        // Botones de acción CRUD
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

        // Panel central: Tabla estilizada con filtro rápido
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label lblTableTitle = new Label("Directorio de Clientes Registrados");
        lblTableTitle.getStyleClass().add("card-title");
        Label lblTableSub = new Label("Seleccione cualquier fila para cargar sus datos en el formulario lateral");
        lblTableSub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblTableTitle, lblTableSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtFilter.setPromptText("Filtrar por nombre, teléfono o correo...");
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
        btnList.setOnAction(e -> loadCustomers());
        btnClear.setOnAction(e -> {
            formPanel.clear();
            table.getSelectionModel().clearSelection();
        });

        loadCustomers();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay clientes registrados que coincidan con la búsqueda."));

        TableColumn<Customer, String> colId = new TableColumn<>("ID Cliente");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(130);
        colId.setMinWidth(95);

        TableColumn<Customer, String> colName = new TableColumn<>("Nombre Completo");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colName.setMinWidth(180);

        TableColumn<Customer, String> colPhone = new TableColumn<>("Teléfono");
        colPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        colPhone.setMinWidth(120);

        TableColumn<Customer, String> colEmail = new TableColumn<>("Correo Electrónico");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        colEmail.setMinWidth(180);

        table.getColumns().addAll(colId, colName, colPhone, colEmail);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                formPanel.setFieldText("ID", selected.getId());
                formPanel.setFieldText("Nombre", selected.getName());
                formPanel.setFieldText("Teléfono", selected.getPhone());
                formPanel.setFieldText("Correo", selected.getEmail());
            }
        });
    }

    private void handleCreate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String phone = formPanel.getFieldText("Teléfono");
        String email = formPanel.getFieldText("Correo");

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Theme.showWarning("Validación", "Debe completar nombre, teléfono y correo del cliente.");
            return;
        }

        try {
            Customer customer = new Customer(id.isEmpty() ? null : id, name, phone, email);
            customerService.create(customer);
            Theme.showInfo("Éxito", "Cliente creado exitosamente.");
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            Theme.showError("Error al crear cliente", ex.getMessage());
        }
    }

    private void handleFindById() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Ingrese el ID del cliente que desea consultar.");
            return;
        }

        Customer found = customerService.findById(id);
        if (found == null) {
            Theme.showWarning("Sin resultados", "No se encontró ningún cliente con el ID: " + id);
            return;
        }

        formPanel.setFieldText("ID", found.getId());
        formPanel.setFieldText("Nombre", found.getName());
        formPanel.setFieldText("Teléfono", found.getPhone());
        formPanel.setFieldText("Correo", found.getEmail());
        table.getSelectionModel().select(found);
    }

    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String phone = formPanel.getFieldText("Teléfono");
        String email = formPanel.getFieldText("Correo");

        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe especificar o seleccionar el ID del cliente a actualizar.");
            return;
        }
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Theme.showWarning("Validación", "Debe completar todos los datos del cliente.");
            return;
        }

        try {
            Customer customer = new Customer(id, name, phone, email);
            customerService.update(customer);
            Theme.showInfo("Éxito", "Cliente actualizado exitosamente.");
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            Theme.showError("Error al actualizar cliente", ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe seleccionar o ingresar el ID del cliente a eliminar.");
            return;
        }

        if (!Theme.showConfirm("Confirmar eliminación", "¿Está seguro de eliminar el cliente #" + id + "?")) {
            return;
        }

        try {
            customerService.delete(id);
            Theme.showInfo("Éxito", "Cliente eliminado exitosamente.");
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            Theme.showError("Error al eliminar cliente", ex.getMessage());
        }
    }

    public final void loadCustomers() {
        try {
            List<Customer> list = customerService.findAll();
            tableData.setAll(list);
            applyFilter(txtFilter.getText());
        } catch (Exception ex) {
            Theme.showError("Error al consultar clientes", ex.getMessage());
        }
    }

    private void applyFilter(String query) {
        List<Customer> all = customerService.findAll();
        if (query == null || query.trim().isEmpty()) {
            tableData.setAll(all);
            return;
        }
        String q = query.trim().toLowerCase();
        List<Customer> filtrados = new ArrayList<>();
        for (Customer c : all) {
            boolean coincideId = c.getId() != null && c.getId().toLowerCase().contains(q);
            boolean coincideNombre = c.getName() != null && c.getName().toLowerCase().contains(q);
            boolean coincideTelefono = c.getPhone() != null && c.getPhone().toLowerCase().contains(q);
            boolean coincideCorreo = c.getEmail() != null && c.getEmail().toLowerCase().contains(q);

            if (coincideId || coincideNombre || coincideTelefono || coincideCorreo) {
                filtrados.add(c);
            }
        }
        tableData.setAll(filtrados);
    }
}
