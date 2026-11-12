package com.autoclean.ui.panels;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CostingService;
import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.EmployeeService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.theme.Theme;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel administrativo principal (Dashboard) de AutoClean.
 * Muestra indicadores clave (KPIs) en tiempo real basados en los servicios del proyecto,
 * accesos rápidos a módulos, próximos turnos, servicios en proceso y actividad reciente.
 */
public class DashboardPanel extends BorderPane {

    private final AppointmentService appointmentService = ServiceFactory.getAppointmentService();
    private final CustomerService customerService = ServiceFactory.getCustomerService();
    private final VehicleService vehicleService = ServiceFactory.getVehicleService();
    private final ServiceTypeService serviceTypeService = ServiceFactory.getServiceTypeService();
    private final EmployeeService employeeService = ServiceFactory.getEmployeeService();
    private final CostingService costingService = ServiceFactory.getCostingService();

    private final Consumer<String> navigationCallback;

    private final Label lblServicesToday = new Label("0");
    private final Label lblPendingCount = new Label("0");
    private final Label lblTotalRevenue = new Label("$0");
    private final Label lblEstimatedCosts = new Label("$0");
    private final Label lblEstimatedMargin = new Label("$0");

    private final TableView<Appointment> upcomingTable = new TableView<>();
    private final ObservableList<Appointment> upcomingData = FXCollections.observableArrayList();
    private final VBox inProgressContainer = new VBox(10);
    private final VBox recentActivityContainer = new VBox(10);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardPanel(Consumer<String> navigationCallback) {
        this.navigationCallback = navigationCallback;
        getStyleClass().add("content-area");

        VBox mainContent = new VBox(22);
        mainContent.setFillWidth(true);

        // 1. Fila de 5 Tarjetas de Estadísticas (KPIs)
        HBox kpiRow = buildKpiSection();

        // 2. Sección de Accesos Rápidos
        VBox quickActionsSection = buildQuickActionsSection();

        // 3. Sección Inferior: Próximos Turnos (Izquierda) + Servicios en Proceso y Actividad (Derecha)
        GridPane bottomGrid = buildBottomSection();

        mainContent.getChildren().addAll(kpiRow, quickActionsSection, bottomGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        setCenter(scrollPane);

        refreshDashboard();
    }

    /**
     * Construye las 5 tarjetas de indicadores solicitados en el requerimiento.
     */
    private HBox buildKpiSection() {
        HBox row = new HBox(16);
        row.setFillHeight(true);

        VBox card1 = createKpiCard("TOTAL SERVICIOS HOY", lblServicesToday, "Turnos agendados en el día", "📅", "kpi-icon-cyan", "kpi-value");
        VBox card2 = createKpiCard("TURNOS PENDIENTES", lblPendingCount, "En espera de atención", "⏳", "kpi-icon-orange", "kpi-value-warning");
        VBox card3 = createKpiCard("INGRESOS REGISTRADOS", lblTotalRevenue, "Facturación de servicios", "💵", "kpi-icon-green", "kpi-value-positive");
        VBox card4 = createKpiCard("COSTOS ESTIMADOS", lblEstimatedCosts, "Insumos y mano de obra directa", "🧪", "kpi-icon-slate", "kpi-value");
        VBox card5 = createKpiCard("MARGEN ESTIMADO", lblEstimatedMargin, "Diferencia bruta (no ganancia neta)", "📈", "kpi-icon-blue", "kpi-value-cyan");

        for (VBox card : new VBox[]{card1, card2, card3, card4, card5}) {
            HBox.setHgrow(card, Priority.ALWAYS);
            card.setMaxWidth(Double.MAX_VALUE);
        }

        row.getChildren().addAll(card1, card2, card3, card4, card5);
        return row;
    }

    private VBox createKpiCard(String title, Label valueLabel, String caption, String iconText, String iconStyle, String valueStyle) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconBadge = new Label(iconText);
        iconBadge.getStyleClass().add(iconStyle);

        topRow.getChildren().addAll(lblTitle, spacer, iconBadge);

        valueLabel.getStyleClass().add(valueStyle);

        Label lblCaption = new Label(caption);
        lblCaption.getStyleClass().add("kpi-caption");

        card.getChildren().addAll(topRow, valueLabel, lblCaption);
        return card;
    }

    /**
     * Construye la barra de accesos rápidos para registrar clientes, vehículos, servicios y turnos.
     */
    private VBox buildQuickActionsSection() {
        VBox container = new VBox(10);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblSection = new Label("Accesos Rápidos de Operación");
        lblSection.getStyleClass().add("card-title");
        Label lblHint = new Label("Navegue directamente a los módulos de registro principales");
        lblHint.getStyleClass().add("card-subtitle");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻ Actualizar Indicadores");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> refreshDashboard());

        header.getChildren().addAll(lblSection, lblHint, spacer, btnRefresh);

        HBox actionsRow = new HBox(14);
        Button btnNewCustomer = createQuickActionButton("👤 Registrar Cliente", "Alta de propietarios y contacto", "CUSTOMERS");
        Button btnNewVehicle = createQuickActionButton("🚗 Registrar Vehículo", "Asociar placa, marca y modelo", "VEHICLES");
        Button btnNewService = createQuickActionButton("✨ Nuevo Servicio", "Configurar catálogo y precios", "SERVICES");
        Button btnNewAppointment = createQuickActionButton("📆 Agendar Turno", "Programación y control de estados", "APPOINTMENTS");
        Button btnCosting = createQuickActionButton("🧮 Estimar Costeo", "Calcular costos directos y margen", "COSTING");

        for (Button b : new Button[]{btnNewCustomer, btnNewVehicle, btnNewService, btnNewAppointment, btnCosting}) {
            HBox.setHgrow(b, Priority.ALWAYS);
            b.setMaxWidth(Double.MAX_VALUE);
        }

        actionsRow.getChildren().addAll(btnNewCustomer, btnNewVehicle, btnNewService, btnNewAppointment, btnCosting);
        container.getChildren().addAll(header, actionsRow);
        return container;
    }

    private Button createQuickActionButton(String title, String subtitle, String targetCard) {
        VBox textContainer = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("quick-action-title");
        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("quick-action-sub");
        textContainer.getChildren().addAll(lblTitle, lblSub);

        Button btn = new Button();
        btn.setGraphic(textContainer);
        btn.getStyleClass().add("quick-action-btn");
        btn.setOnAction(e -> {
            if (navigationCallback != null) {
                navigationCallback.accept(targetCard);
            }
        });
        return btn;
    }

    /**
     * Construye la sección inferior dividida en tabla de próximos turnos,
     * servicios en proceso y actividad reciente.
     */
    private GridPane buildBottomSection() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Columna izquierda: Tabla de Próximos Turnos y Agenda
        VBox upcomingCard = new VBox(12);
        upcomingCard.getStyleClass().add("card");
        GridPane.setHgrow(upcomingCard, Priority.ALWAYS);

        HBox upHeader = new HBox(10);
        upHeader.setAlignment(Pos.CENTER_LEFT);
        VBox upTitles = new VBox(2);
        Label lblUpTitle = new Label("Agenda de Turnos y Servicios Recientes");
        lblUpTitle.getStyleClass().add("card-title");
        Label lblUpSub = new Label("Vista consolidada de turnos programados, en ejecución y finalizados");
        lblUpSub.getStyleClass().add("card-subtitle");
        upTitles.getChildren().addAll(lblUpTitle, lblUpSub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnGoAppointments = new Button("Ver módulo Turnos →");
        btnGoAppointments.getStyleClass().add("btn-secondary");
        btnGoAppointments.setOnAction(e -> {
            if (navigationCallback != null) {
                navigationCallback.accept("APPOINTMENTS");
            }
        });
        upHeader.getChildren().addAll(upTitles, spacer, btnGoAppointments);

        configureUpcomingTable();
        upcomingCard.getChildren().addAll(upHeader, upcomingTable);

        // Columna derecha: Servicios en Proceso + Actividad Reciente
        VBox rightColumn = new VBox(18);
        rightColumn.setMinWidth(360);
        rightColumn.setPrefWidth(390);

        VBox inProgressCard = new VBox(10);
        inProgressCard.getStyleClass().add("card");
        Label lblInProgTitle = new Label("Bahías / Servicios en Proceso");
        lblInProgTitle.getStyleClass().add("card-title");
        Label lblInProgSub = new Label("Vehículos que están siendo atendidos en este momento");
        lblInProgSub.getStyleClass().add("card-subtitle");
        inProgressCard.getChildren().addAll(lblInProgTitle, lblInProgSub, inProgressContainer);

        VBox activityCard = new VBox(10);
        activityCard.getStyleClass().add("card");
        Label lblActTitle = new Label("Resumen Operativo y Actividad");
        lblActTitle.getStyleClass().add("card-title");
        Label lblActSub = new Label("Estado general de recursos registrados en memoria");
        lblActSub.getStyleClass().add("card-subtitle");
        activityCard.getChildren().addAll(lblActTitle, lblActSub, recentActivityContainer);

        rightColumn.getChildren().addAll(inProgressCard, activityCard);

        grid.add(upcomingCard, 0, 0);
        grid.add(rightColumn, 1, 0);
        return grid;
    }

    private void configureUpcomingTable() {
        upcomingTable.setItems(upcomingData);
        upcomingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        upcomingTable.setPrefHeight(330);
        upcomingTable.setPlaceholder(new Label("No hay turnos registrados actualmente."));

        TableColumn<Appointment, String> colId = new TableColumn<>("Turno");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colId.setMaxWidth(95);
        colId.setMinWidth(80);

        TableColumn<Appointment, String> colCustomer = new TableColumn<>("Cliente");
        colCustomer.setCellValueFactory(cell -> {
            Customer c = customerService.findById(cell.getValue().getCustomerId());
            return new SimpleStringProperty(c != null ? c.getName() : cell.getValue().getCustomerId());
        });

        TableColumn<Appointment, String> colVehicle = new TableColumn<>("Vehículo");
        colVehicle.setCellValueFactory(cell -> {
            Vehicle v = vehicleService.findById(cell.getValue().getVehicleId());
            return new SimpleStringProperty(v != null ? v.getPlate() + " (" + v.getBrand() + ")" : cell.getValue().getVehicleId());
        });

        TableColumn<Appointment, String> colService = new TableColumn<>("Servicio");
        colService.setCellValueFactory(cell -> {
            ServiceType s = serviceTypeService.findById(cell.getValue().getServiceTypeId());
            return new SimpleStringProperty(s != null ? s.getName() : cell.getValue().getServiceTypeId());
        });

        TableColumn<Appointment, String> colDate = new TableColumn<>("Fecha / Hora");
        colDate.setCellValueFactory(cell -> {
            String dt = cell.getValue().getDateTime() != null ? cell.getValue().getDateTime().format(TIME_FMT) : "";
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
        colStatus.setMinWidth(115);
        colStatus.setMaxWidth(130);

        upcomingTable.getColumns().addAll(colId, colCustomer, colVehicle, colService, colDate, colStatus);
    }

    /**
     * Recalcula todos los indicadores del Dashboard en base a los datos reales de los servicios.
     */
    public final void refreshDashboard() {
        List<Appointment> appointments = appointmentService.findAll();
        List<CostEstimate> estimates = costingService.findAll();
        LocalDate today = LocalDate.now();

        int todayCount = 0;
        int pendingCount = 0;
        double totalRevenue = 0.0;

        for (Appointment app : appointments) {
            if (app.getDateTime() != null && app.getDateTime().toLocalDate().equals(today)) {
                todayCount++;
            }
            if ("PENDING".equalsIgnoreCase(app.getStatus())) {
                pendingCount++;
            }
            if (!"CANCELLED".equalsIgnoreCase(app.getStatus())) {
                ServiceType st = serviceTypeService.findById(app.getServiceTypeId());
                if (st != null) {
                    totalRevenue += st.getPrice();
                }
            }
        }

        double totalEstimatedCost = 0.0;
        double totalMargin = 0.0;
        if (!estimates.isEmpty()) {
            for (CostEstimate ce : estimates) {
                totalEstimatedCost += ce.getEstimatedCost();
                totalMargin += ce.getDifference();
            }
        } else {
            totalMargin = totalRevenue;
        }

        lblServicesToday.setText(String.valueOf(todayCount));
        lblPendingCount.setText(String.valueOf(pendingCount));
        lblTotalRevenue.setText(String.format("$%,.0f", totalRevenue));
        lblEstimatedCosts.setText(String.format("$%,.0f", totalEstimatedCost));
        lblEstimatedMargin.setText(String.format("$%,.0f", totalMargin));

        // Actualizar tabla de turnos
        upcomingData.setAll(appointments);

        // Actualizar tarjetas de servicios en proceso
        inProgressContainer.getChildren().clear();
        int inProgCount = 0;
        for (Appointment app : appointments) {
            if ("IN_PROGRESS".equalsIgnoreCase(app.getStatus())) {
                inProgCount++;
                Vehicle v = vehicleService.findById(app.getVehicleId());
                ServiceType st = serviceTypeService.findById(app.getServiceTypeId());
                Employee emp = employeeService.findById(app.getEmployeeId());

                VBox item = new VBox(4);
                item.getStyleClass().add("activity-item");

                HBox top = new HBox(8);
                top.setAlignment(Pos.CENTER_LEFT);
                Label lblPlate = new Label(v != null ? v.getPlate() + " — " + v.getBrand() + " " + v.getModel() : app.getVehicleId());
                lblPlate.getStyleClass().add("activity-title");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                top.getChildren().addAll(lblPlate, sp, Theme.createStatusBadge("IN_PROGRESS"));

                Label lblService = new Label("Servicio: " + (st != null ? st.getName() : app.getServiceTypeId()));
                lblService.getStyleClass().add("activity-meta");

                Label lblTech = new Label("Técnico asignado: " + (emp != null ? emp.getName() : "Sin asignar"));
                lblTech.getStyleClass().add("activity-meta");

                item.getChildren().addAll(top, lblService, lblTech);
                inProgressContainer.getChildren().add(item);
            }
        }

        if (inProgCount == 0) {
            Label emptyLbl = new Label("No hay vehículos en proceso en este instante.");
            emptyLbl.getStyleClass().add("card-subtitle");
            emptyLbl.setPadding(new Insets(6, 0, 6, 0));
            inProgressContainer.getChildren().add(emptyLbl);
        }

        // Actualizar actividad reciente y conteos del sistema
        recentActivityContainer.getChildren().clear();
        recentActivityContainer.getChildren().addAll(
                createSummaryRow("👥 Clientes registrados", customerService.findAll().size() + " propietarios activos"),
                createSummaryRow("🚘 Flota de vehículos", vehicleService.findAll().size() + " vehículos vinculados"),
                createSummaryRow("🧼 Catálogo de servicios", serviceTypeService.findAll().size() + " paquetes disponibles"),
                createSummaryRow("🧑‍🔧 Personal técnico", employeeService.findAll().size() + " especialistas en plantilla"),
                createSummaryRow("📊 Cálculos de costeo", estimates.size() + " estimaciones procesadas")
        );
    }

    private HBox createSummaryRow(String label, String detail) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("activity-item");

        Label lblLeft = new Label(label);
        lblLeft.getStyleClass().add("activity-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblRight = new Label(detail);
        lblRight.getStyleClass().addAll("badge", "badge-neutral");

        box.getChildren().addAll(lblLeft, spacer, lblRight);
        return box;
    }
}

