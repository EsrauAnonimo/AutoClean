package com.autoclean.ui.panels;

import com.autoclean.contracts.EmployeeService;
import com.autoclean.model.Employee;
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
import javafx.scene.control.TableCell;
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
 * Panel JavaFX para la administración de empleados y especialistas técnicos
 * (CRUD).
 * Conserva toda la lógica conectada a EmployeeService.
 */
public class EmployeePanel extends BorderPane {

    private final EmployeeService employeeService;
    private final SimpleFormPanel formPanel;
    private final TableView<Employee> table;
    private final ObservableList<Employee> tableData;
    private final TextField txtFilter;

    public EmployeePanel() {
        this.employeeService = ServiceFactory.getEmployeeService();
        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);
        this.txtFilter = new TextField();

        getStyleClass().add("content-area");

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(340);
        formPanel.setMinWidth(310);
        formPanel.setHeader("Gestión de Personal",
                "Registre los especialistas técnicos y asesores responsables de atender los turnos.");

        formPanel.addField("ID", "Autogenerado o ej. EMP-004");
        formPanel.addField("Nombre", "Nombre completo del empleado");
        formPanel.addField("Rol", "Ej. Especialista en Detailing, Lavador");

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
        Label lblTitle = new Label("Plantilla de Empleados y Técnicos");
        lblTitle.getStyleClass().add("card-title");
        Label lblSub = new Label("Personal activo disponible para asignación de servicios automotrices");
        lblSub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtFilter.setPromptText("Buscar por nombre o cargo...");
        txtFilter.setPrefWidth(240);
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
        btnList.setOnAction(e -> loadEmployees());
        btnClear.setOnAction(e -> {
            formPanel.clear();
            table.getSelectionModel().clearSelection();
        });

        loadEmployees();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay empleados registrados actualmente."));

        TableColumn<Employee, String> colId = new TableColumn<>("ID Empleado");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(130);
        colId.setMinWidth(95);

        TableColumn<Employee, String> colName = new TableColumn<>("Nombre del Especialista");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colName.setMinWidth(200);

        TableColumn<Employee, String> colRole = new TableColumn<>("Cargo / Especialidad");
        colRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(role);
                    badge.getStyleClass().addAll("badge", "badge-neutral");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colRole.setMinWidth(200);

        table.getColumns().addAll(colId, colName, colRole);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                formPanel.setFieldText("ID", selected.getId());
                formPanel.setFieldText("Nombre", selected.getName());
                formPanel.setFieldText("Rol", selected.getRole());
            }
        });
    }

    private void handleCreate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String role = formPanel.getFieldText("Rol");

        if (name.isEmpty() || role.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar el nombre y el rol del empleado.");
            return;
        }

        try {
            Employee employee = new Employee(id.isEmpty() ? null : id, name, role);
            employeeService.create(employee);
            Theme.showInfo("Éxito", "Empleado creado exitosamente.");
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            Theme.showError("Error al crear empleado", ex.getMessage());
        }
    }

    private void handleFindById() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Ingrese el ID del empleado a consultar.");
            return;
        }

        Employee found = employeeService.findById(id);
        if (found == null) {
            Theme.showWarning("Sin resultados", "No existe un empleado con el ID: " + id);
            return;
        }

        formPanel.setFieldText("ID", found.getId());
        formPanel.setFieldText("Nombre", found.getName());
        formPanel.setFieldText("Rol", found.getRole());
        table.getSelectionModel().select(found);
    }

    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String role = formPanel.getFieldText("Rol");

        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe seleccionar o ingresar el ID del empleado.");
            return;
        }
        if (name.isEmpty() || role.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar el nombre y el rol del empleado.");
            return;
        }

        try {
            Employee employee = new Employee(id, name, role);
            employeeService.update(employee);
            Theme.showInfo("Éxito", "Empleado actualizado exitosamente.");
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            Theme.showError("Error al actualizar empleado", ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe seleccionar o ingresar el ID a eliminar.");
            return;
        }

        if (!Theme.showConfirm("Confirmar eliminación", "¿Está seguro de eliminar el empleado #" + id + "?")) {
            return;
        }

        try {
            employeeService.delete(id);
            Theme.showInfo("Éxito", "Empleado eliminado exitosamente.");
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            Theme.showError("Error al eliminar empleado", ex.getMessage());
        }
    }

    public final void loadEmployees() {
        try {
            List<Employee> list = employeeService.findAll();
            tableData.setAll(list);
            applyFilter(txtFilter.getText());
        } catch (Exception ex) {
            Theme.showError("Error al consultar empleados", ex.getMessage());
        }
    }

    private void applyFilter(String query) {
        List<Employee> all = employeeService.findAll();
        if (query == null || query.trim().isEmpty()) {
            tableData.setAll(all);
            return;
        }
        String q = query.trim().toLowerCase();
        List<Employee> filtrados = new ArrayList<>();
        for (Employee e : all) {
            boolean coincideId = e.getId() != null && e.getId().toLowerCase().contains(q);
            boolean coincideNombre = e.getName() != null && e.getName().toLowerCase().contains(q);
            boolean coincideRol = e.getRole() != null && e.getRole().toLowerCase().contains(q);

            if (coincideId || coincideNombre || coincideRol) {
                filtrados.add(e);
            }
        }
        tableData.setAll(filtrados);
    }
}
