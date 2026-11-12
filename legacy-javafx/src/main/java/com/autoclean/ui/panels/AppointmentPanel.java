package com.autoclean.ui.panels;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.EmployeeService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Appointment;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
 * Panel JavaFX para la gestión de turnos de servicio y control de la máquina de
 * estados.
 * Conserva todas las reglas de transición de AppointmentService e incluye
 * etiquetas visuales de estado.
 */
public class AppointmentPanel extends BorderPane {

    private final AppointmentService appointmentService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final ServiceTypeService serviceTypeService;
    private final EmployeeService employeeService;

    private final SimpleFormPanel formPanel;
    private final ComboBox<Customer> cbCustomer;
    private final ComboBox<Vehicle> cbVehicle;
    private final ComboBox<ServiceType> cbService;
    private final ComboBox<Employee> cbEmployee;
    private final ComboBox<String> statusComboBox;

    private final TableView<Appointment> table;
    private final ObservableList<Appointment> tableData;
    private final TextField txtFilter;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AppointmentPanel() {
        this.appointmentService = ServiceFactory.getAppointmentService();
        this.customerService = ServiceFactory.getCustomerService();
        this.vehicleService = ServiceFactory.getVehicleService();
        this.serviceTypeService = ServiceFactory.getServiceTypeService();
        this.employeeService = ServiceFactory.getEmployeeService();

        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);
        this.txtFilter = new TextField();

        this.cbCustomer = new ComboBox<>();
        this.cbVehicle = new ComboBox<>();
        this.cbService = new ComboBox<>();
        this.cbEmployee = new ComboBox<>();
        this.statusComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "PENDING", "IN_PROGRESS", "FINISHED", "CANCELLED"));

        getStyleClass().add("content-area");

        // Formulario izquierdo dentro de un ScrollPane ligero por si la ventana es
        // compacta
        formPanel = new SimpleFormPanel();
        formPanel.setPrefWidth(365);
        formPanel.setMinWidth(335);
        formPanel.setHeader("Programación de Turno",
                "Asigne cliente, vehículo, tipo de servicio, técnico y controle su estado.");

        formPanel.addField("ID", "Autogenerado o ej. TRN-005");

        // Selectores rápidos sincronizados con los campos de ID
        cbCustomer.setPromptText("Elegir cliente...");
        cbCustomer.setMaxWidth(Double.MAX_VALUE);
        cbCustomer.setOnAction(e -> {
            Customer c = cbCustomer.getValue();
            if (c != null) {
                formPanel.setFieldText("ID Cliente", c.getId());
                List<Vehicle> customerVehicles = vehicleService.findByCustomerId(c.getId());
                if (!customerVehicles.isEmpty()) {
                    cbVehicle.getItems().setAll(customerVehicles);
                    cbVehicle.setValue(customerVehicles.get(0));
                    formPanel.setFieldText("ID Vehículo", customerVehicles.get(0).getId());
                } else {
                    cbVehicle.getItems().setAll(vehicleService.findAll());
                }
            }
        });
        formPanel.addCustomControl("Selector de Cliente", cbCustomer);
        formPanel.addField("ID Cliente", "Ej. CLI-001");

        cbVehicle.setPromptText("Elegir vehículo...");
        cbVehicle.setMaxWidth(Double.MAX_VALUE);
        cbVehicle.setOnAction(e -> {
            Vehicle v = cbVehicle.getValue();
            if (v != null) {
                formPanel.setFieldText("ID Vehículo", v.getId());
            }
        });
        formPanel.addCustomControl("Selector de Vehículo", cbVehicle);
        formPanel.addField("ID Vehículo", "Ej. VEH-001");

        cbService.setPromptText("Elegir servicio del catálogo...");
        cbService.setMaxWidth(Double.MAX_VALUE);
        cbService.setOnAction(e -> {
            ServiceType st = cbService.getValue();
            if (st != null) {
                formPanel.setFieldText("ID Tipo Servicio", st.getId());
            }
        });
        formPanel.addCustomControl("Selector de Servicio", cbService);
        formPanel.addField("ID Tipo Servicio", "Ej. SRV-001");

        cbEmployee.setPromptText("Elegir especialista técnico...");
        cbEmployee.setMaxWidth(Double.MAX_VALUE);
        cbEmployee.setOnAction(e -> {
            Employee emp = cbEmployee.getValue();
            if (emp != null) {
                formPanel.setFieldText("ID Empleado", emp.getId());
            }
        });
        formPanel.addCustomControl("Selector de Empleado", cbEmployee);
        formPanel.addField("ID Empleado", "Ej. EMP-001");

        formPanel.addField("Fecha y Hora (yyyy-MM-dd HH:mm)", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        formPanel.setFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)",
                LocalDateTime.now().withMinute(0).format(DATE_TIME_FORMATTER));

        statusComboBox.setValue("PENDING");
        statusComboBox.setMaxWidth(Double.MAX_VALUE);
        formPanel.addCustomControl("Estado del Turno", statusComboBox);

        // Botones de acción
        FlowPane buttonsPane = new FlowPane(8, 8);
        Button btnCreate = new Button("＋ Crear");
        Button btnUpdate = new Button("✎ Actualizar");
        Button btnChangeStatus = new Button("⚡ Cambiar Estado");
        Button btnDelete = new Button("🗑 Eliminar");
        Button btnList = new Button("↻ Listar");
        Button btnClear = new Button("Limpiar");

        btnCreate.getStyleClass().add("btn-primary");
        btnUpdate.getStyleClass().add("btn-dark");
        btnChangeStatus.getStyleClass().add("btn-success");
        btnDelete.getStyleClass().add("btn-danger");
        btnList.getStyleClass().add("btn-secondary");
        btnClear.getStyleClass().add("btn-secondary");

        buttonsPane.getChildren().addAll(btnCreate, btnUpdate, btnChangeStatus, btnDelete, btnList, btnClear);
        formPanel.getChildren().add(buttonsPane);

        ScrollPane leftScroll = new ScrollPane(formPanel);
        leftScroll.setFitToWidth(true);
        leftScroll.setMinWidth(385);
        leftScroll.getStyleClass().add("content-scroll");

        // Panel derecho: Tabla de turnos con etiquetas de estado
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Control de Turnos y Estados de Servicio");
        lblTitle.getStyleClass().add("card-title");
        Label lblSub = new Label(
                "Transiciones válidas: PENDING → IN_PROGRESS / CANCELLED | IN_PROGRESS → FINISHED / CANCELLED");
        lblSub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtFilter.setPromptText("Filtrar por ID, cliente, placa o estado...");
        txtFilter.setPrefWidth(250);
        txtFilter.getStyleClass().add("text-field");
        txtFilter.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));

        tableHeader.getChildren().addAll(titleBox, spacer, txtFilter);

        configureTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableHeader, table);

        HBox mainLayout = new HBox(20, leftScroll, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        setCenter(mainLayout);

        // Eventos
        btnCreate.setOnAction(e -> handleCreate());
        btnUpdate.setOnAction(e -> handleUpdate());
        btnChangeStatus.setOnAction(e -> handleChangeStatus());
        btnDelete.setOnAction(e -> handleDelete());
        btnList.setOnAction(e -> loadAppointments());
        btnClear.setOnAction(e -> {
            formPanel.clear();
            formPanel.setFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)",
                    LocalDateTime.now().withMinute(0).format(DATE_TIME_FORMATTER));
            cbCustomer.setValue(null);
            cbVehicle.setValue(null);
            cbService.setValue(null);
            cbEmployee.setValue(null);
            statusComboBox.setValue("PENDING");
            table.getSelectionModel().clearSelection();
        });

        loadAppointments();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay turnos programados para mostrar."));

        TableColumn<Appointment, String> colId = new TableColumn<>("ID Turno");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(100);
        colId.setMinWidth(80);

        TableColumn<Appointment, String> colCust = new TableColumn<>("Cliente");
        colCust.setCellValueFactory(cell -> {
            String cid = cell.getValue().getCustomerId();
            Customer c = customerService.findById(cid);
            return new SimpleStringProperty(c != null ? c.getName() + " [" + cid + "]" : cid);
        });
        colCust.setMinWidth(150);

        TableColumn<Appointment, String> colVeh = new TableColumn<>("Vehículo");
        colVeh.setCellValueFactory(cell -> {
            String vid = cell.getValue().getVehicleId();
            Vehicle v = vehicleService.findById(vid);
            return new SimpleStringProperty(v != null ? v.getPlate() + " - " + v.getBrand() : vid);
        });
        colVeh.setMinWidth(120);

        TableColumn<Appointment, String> colSrv = new TableColumn<>("Servicio");
        colSrv.setCellValueFactory(cell -> {
            String sid = cell.getValue().getServiceTypeId();
            ServiceType s = serviceTypeService.findById(sid);
            return new SimpleStringProperty(s != null ? s.getName() : sid);
        });
        colSrv.setMinWidth(160);

        TableColumn<Appointment, String> colEmp = new TableColumn<>("Técnico");
        colEmp.setCellValueFactory(cell -> {
            String eid = cell.getValue().getEmployeeId();
            Employee emp = employeeService.findById(eid);
            return new SimpleStringProperty(emp != null ? emp.getName() : eid);
        });

        TableColumn<Appointment, String> colDate = new TableColumn<>("Fecha / Hora");
        colDate.setCellValueFactory(cell -> {
            String dt = cell.getValue().getDateTime() != null
                    ? cell.getValue().getDateTime().format(DATE_TIME_FORMATTER)
                    : "";
            return new SimpleStringProperty(dt);
        });
        colDate.setMinWidth(125);

        TableColumn<Appointment, String> colStatus = new TableColumn<>("Estado");
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(Theme.createStatusBadge(status));
                    setText(null);
                }
            }
        });
        colStatus.setMinWidth(120);
        colStatus.setMaxWidth(135);

        table.getColumns().addAll(colId, colCust, colVeh, colSrv, colEmp, colDate, colStatus);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                formPanel.setFieldText("ID", selected.getId());
                formPanel.setFieldText("ID Cliente", selected.getCustomerId());
                formPanel.setFieldText("ID Vehículo", selected.getVehicleId());
                formPanel.setFieldText("ID Tipo Servicio", selected.getServiceTypeId());
                formPanel.setFieldText("ID Empleado", selected.getEmployeeId());
                formPanel.setFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)",
                        selected.getDateTime() != null ? selected.getDateTime().format(DATE_TIME_FORMATTER) : "");
                statusComboBox.setValue(selected.getStatus());
            }
        });
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();
        try {
            if (text.contains("T")) {
                return LocalDateTime.parse(text);
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (Exception ex) {
            return null;
        }
    }

    private void handleCreate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String vehicleId = formPanel.getFieldText("ID Vehículo");
        String serviceTypeId = formPanel.getFieldText("ID Tipo Servicio");
        String employeeId = formPanel.getFieldText("ID Empleado");
        String dateTimeStr = formPanel.getFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)");
        String status = statusComboBox.getValue();

        if (customerId.isEmpty() || vehicleId.isEmpty() || serviceTypeId.isEmpty() || dateTimeStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar cliente, vehículo, servicio y fecha/hora.");
            return;
        }

        LocalDateTime dateTime = parseDateTime(dateTimeStr);
        if (dateTime == null) {
            Theme.showWarning("Validación", "Formato de fecha inválido. Use: yyyy-MM-dd HH:mm (ej. 2026-11-01 10:30)");
            return;
        }

        try {
            Appointment app = new Appointment(id.isEmpty() ? null : id, customerId, vehicleId, serviceTypeId,
                    employeeId, dateTime, status);
            appointmentService.create(app);
            Theme.showInfo("Éxito", "Turno registrado exitosamente.");
            formPanel.clear();
            formPanel.setFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)",
                    LocalDateTime.now().withMinute(0).format(DATE_TIME_FORMATTER));
            loadAppointments();
        } catch (Exception ex) {
            Theme.showError("Error al crear turno", ex.getMessage());
        }
    }

    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String vehicleId = formPanel.getFieldText("ID Vehículo");
        String serviceTypeId = formPanel.getFieldText("ID Tipo Servicio");
        String employeeId = formPanel.getFieldText("ID Empleado");
        String dateTimeStr = formPanel.getFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)");
        String status = statusComboBox.getValue();

        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe seleccionar o ingresar el ID del turno a actualizar.");
            return;
        }

        LocalDateTime dateTime = parseDateTime(dateTimeStr);
        if (dateTime == null) {
            Theme.showWarning("Validación", "Formato de fecha inválido. Use: yyyy-MM-dd HH:mm");
            return;
        }

        try {
            Appointment app = new Appointment(id, customerId, vehicleId, serviceTypeId, employeeId, dateTime, status);
            appointmentService.update(app);
            Theme.showInfo("Éxito", "Turno actualizado exitosamente.");
            loadAppointments();
        } catch (Exception ex) {
            Theme.showError("Error al actualizar turno", ex.getMessage());
        }
    }

    private void handleChangeStatus() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Seleccione un turno o ingrese el ID para cambiar su estado.");
            return;
        }

        String newStatus = statusComboBox.getValue();
        try {
            appointmentService.updateStatus(id, newStatus);
            Theme.showInfo("Estado Actualizado", "El estado del turno #" + id + " cambió a " + newStatus + ".");
            loadAppointments();
        } catch (Exception ex) {
            Theme.showError("Transición de estado no permitida", ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            Theme.showWarning("Validación", "Debe indicar el ID del turno a eliminar.");
            return;
        }

        if (!Theme.showConfirm("Confirmar eliminación", "¿Está seguro de eliminar el turno #" + id + "?")) {
            return;
        }

        try {
            appointmentService.delete(id);
            Theme.showInfo("Éxito", "Turno eliminado exitosamente.");
            formPanel.clear();
            loadAppointments();
        } catch (Exception ex) {
            Theme.showError("Error al eliminar turno", ex.getMessage());
        }
    }

    public final void loadAppointments() {
        try {
            cbCustomer.getItems().setAll(customerService.findAll());
            cbVehicle.getItems().setAll(vehicleService.findAll());
            cbService.getItems().setAll(serviceTypeService.findAll());
            cbEmployee.getItems().setAll(employeeService.findAll());

            List<Appointment> list = appointmentService.findAll();
            tableData.setAll(list);
            applyFilter(txtFilter.getText());
        } catch (Exception ex) {
            Theme.showError("Error al consultar turnos", ex.getMessage());
        }
    }

    private void applyFilter(String query) {
        List<Appointment> all = appointmentService.findAll();
        if (query == null || query.trim().isEmpty()) {
            tableData.setAll(all);
            return;
        }
        String q = query.trim().toLowerCase();
        List<Appointment> filtrados = new ArrayList<>();
        for (Appointment a : all) {
            boolean coincideId = a.getId() != null && a.getId().toLowerCase().contains(q);
            boolean coincideCliente = a.getCustomerId() != null && a.getCustomerId().toLowerCase().contains(q);
            boolean coincideVehiculo = a.getVehicleId() != null && a.getVehicleId().toLowerCase().contains(q);
            boolean coincideServicio = a.getServiceTypeId() != null && a.getServiceTypeId().toLowerCase().contains(q);
            boolean coincideEstado = a.getStatus() != null && a.getStatus().toLowerCase().contains(q);

            if (coincideId || coincideCliente || coincideVehiculo || coincideServicio || coincideEstado) {
                filtrados.add(a);
            }
        }
        tableData.setAll(filtrados);
    }
}
