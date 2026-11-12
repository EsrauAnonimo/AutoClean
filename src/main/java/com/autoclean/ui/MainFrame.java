package com.autoclean.ui;

import com.autoclean.ui.panels.AppointmentPanel;
import com.autoclean.ui.panels.CostingPanel;
import com.autoclean.ui.panels.CustomerPanel;
import com.autoclean.ui.panels.DashboardPanel;
import com.autoclean.ui.panels.EmployeePanel;
import com.autoclean.ui.panels.LoginPanel;
import com.autoclean.ui.panels.ReportPanel;
import com.autoclean.ui.panels.ServiceTypePanel;
import com.autoclean.ui.panels.SuppliesPanel;
import com.autoclean.ui.panels.VehiclePanel;
import com.autoclean.ui.theme.Theme;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana principal en JavaFX de AutoClean con diseño de Dashboard Empresarial:
 * Menú lateral fijo azul oscuro, encabezado superior con buscador y perfil,
 * contenedor central adaptable y barra de estado inferior.
 */
public class MainFrame extends Application {

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_CUSTOMERS = "CUSTOMERS";
    public static final String CARD_VEHICLES = "VEHICLES";
    public static final String CARD_SERVICES = "SERVICES";
    public static final String CARD_APPOINTMENTS = "APPOINTMENTS";
    public static final String CARD_EMPLOYEES = "EMPLOYEES";
    public static final String CARD_SUPPLIES = "SUPPLIES";
    public static final String CARD_COSTING = "COSTING";
    public static final String CARD_REPORTS = "REPORTS";

    private final Map<String, Node> views = new LinkedHashMap<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, String[]> viewMetadata = new LinkedHashMap<>();

    private StackPane rootSceneContainer;
    private BorderPane dashboardLayout;
    private LoginPanel loginPanel;

    private StackPane contentContainer;
    private Label headerTitleLabel;
    private Label headerSubtitleLabel;
    private Label userAvatarLabel;
    private Label userNameLabel;
    private Label statusLabel;

    private DashboardPanel dashboardPanel;
    private CustomerPanel customerPanel;
    private VehiclePanel vehiclePanel;
    private ServiceTypePanel serviceTypePanel;
    private AppointmentPanel appointmentPanel;
    private EmployeePanel employeePanel;
    private SuppliesPanel suppliesPanel;
    private CostingPanel costingPanel;
    private ReportPanel reportPanel;

    @Override
    public void start(Stage primaryStage) {
        // Inicializar datos semilla si los repositorios en memoria están vacíos
        Theme.ensureSeedDataLoaded();

        primaryStage.setTitle("AutoClean — Sistema de Gestión de Centros de Embellecimiento Automotriz");
        primaryStage.setMinWidth(1060);
        primaryStage.setMinHeight(680);

        dashboardLayout = new BorderPane();

        // 1. Inicializar paneles de cada módulo
        initPanels();

        // 2. Construir Menú Lateral Fijo (Sidebar)
        VBox sidebar = buildSidebar();
        dashboardLayout.setLeft(sidebar);

        // 3. Construir Área Derecha (Encabezado Superior + Contenedor Central + Barra de Estado)
        BorderPane rightContainer = new BorderPane();
        rightContainer.setTop(buildTopHeader());

        contentContainer = new StackPane();
        rightContainer.setCenter(contentContainer);
        rightContainer.setBottom(buildStatusBar());

        dashboardLayout.setCenter(rightContainer);

        // 4. Inicializar pantalla de Login y contenedor raíz reutilizable
        loginPanel = new LoginPanel(this::handleLoginSuccess);
        rootSceneContainer = new StackPane(loginPanel);

        // 5. Crear escena y cargar hojas de estilo CSS externas
        Scene scene = new Scene(rootSceneContainer, 1280, 780);
        Theme.applyStylesheets(scene);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void handleLoginSuccess(String adminName) {
        if (userNameLabel != null) {
            userNameLabel.setText(adminName);
        }
        if (userAvatarLabel != null && adminName != null && adminName.length() >= 2) {
            userAvatarLabel.setText(adminName.substring(0, 2).toUpperCase());
        }
        rootSceneContainer.getChildren().setAll(dashboardLayout);
        showCard(CARD_DASHBOARD);
    }

    private void handleLogout() {
        loginPanel.resetForm();
        rootSceneContainer.getChildren().setAll(loginPanel);
    }

    private void initPanels() {
        dashboardPanel = new DashboardPanel(this::showCard);
        customerPanel = new CustomerPanel();
        vehiclePanel = new VehiclePanel();
        serviceTypePanel = new ServiceTypePanel();
        appointmentPanel = new AppointmentPanel();
        employeePanel = new EmployeePanel();
        suppliesPanel = new SuppliesPanel();
        costingPanel = new CostingPanel();
        reportPanel = new ReportPanel();

        registerView(CARD_DASHBOARD, dashboardPanel,
                "Panel Administrativo General",
                "Resumen ejecutivo de operaciones, turnos del día, ingresos y márgenes estimados");
        registerView(CARD_CUSTOMERS, customerPanel,
                "Gestión de Clientes",
                "Administración de propietarios, datos de contacto y consulta rápida");
        registerView(CARD_VEHICLES, vehiclePanel,
                "Gestión de Vehículos",
                "Control de placas, marcas, modelos y vinculación con propietarios");
        registerView(CARD_SERVICES, serviceTypePanel,
                "Catálogo de Servicios",
                "Portafolio de lavado, detailing, porcelanizado y precios al público");
        registerView(CARD_APPOINTMENTS, appointmentPanel,
                "Gestión de Turnos y Citas",
                "Programación de agenda y control de estados (Pendiente, En Proceso, Finalizado, Cancelado)");
        registerView(CARD_EMPLOYEES, employeePanel,
                "Gestión de Empleados y Técnicos",
                "Directorio de especialistas y personal operativo del centro");
        registerView(CARD_SUPPLIES, suppliesPanel,
                "Control de Insumos",
                "Referencia de productos químicos, ceras y materiales de detailing");
        registerView(CARD_COSTING, costingPanel,
                "Motor de Estimación de Costeo",
                "Cálculo de costos directos por servicio y margen bruto preliminar");
        registerView(CARD_REPORTS, reportPanel,
                "Reportes Gerenciales",
                "Consultas consolidadas de servicios por periodo y balance de ingresos vs costos");
    }

    private void registerView(String key, Node view, String title, String subtitle) {
        views.put(key, view);
        viewMetadata.put(key, new String[]{title, subtitle});
    }

    /**
     * Construye el menú lateral fijo (Sidebar) en azul oscuro con iconos y secciones.
     */
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Cabecera de marca AutoClean
        HBox brandBox = new HBox(12);
        brandBox.getStyleClass().add("sidebar-brand");
        Label logoBadge = new Label("AC");
        logoBadge.getStyleClass().add("brand-logo-badge");

        VBox brandTexts = new VBox(1);
        Label lblBrand = new Label("AutoClean");
        lblBrand.getStyleClass().add("brand-title");
        Label lblTag = new Label("Detailing & Car Care ERP");
        lblTag.getStyleClass().add("brand-subtitle");
        brandTexts.getChildren().addAll(lblBrand, lblTag);
        brandBox.getChildren().addAll(logoBadge, brandTexts);

        // Navegación por categorías
        VBox navContainer = new VBox(4);
        navContainer.getStyleClass().add("sidebar-nav-container");

        Label secOverview = new Label("PANEL PRINCIPAL");
        secOverview.getStyleClass().add("sidebar-section-label");

        Button btnDashboard = createNavButton(CARD_DASHBOARD, "📊", "Dashboard");

        Label secOperacion = new Label("OPERACIÓN Y AGENDA");
        secOperacion.getStyleClass().add("sidebar-section-label");

        Button btnAppointments = createNavButton(CARD_APPOINTMENTS, "📅", "Turnos / Citas");
        Button btnCustomers = createNavButton(CARD_CUSTOMERS, "👥", "Clientes");
        Button btnVehicles = createNavButton(CARD_VEHICLES, "🚗", "Vehículos");
        Button btnServices = createNavButton(CARD_SERVICES, "✨", "Servicios");

        Label secRecursos = new Label("RECURSOS Y FINANZAS");
        secRecursos.getStyleClass().add("sidebar-section-label");

        Button btnEmployees = createNavButton(CARD_EMPLOYEES, "🧑‍🔧", "Empleados");
        Button btnSupplies = createNavButton(CARD_SUPPLIES, "🧪", "Insumos");
        Button btnCosting = createNavButton(CARD_COSTING, "🧮", "Costeo");
        Button btnReports = createNavButton(CARD_REPORTS, "📈", "Reportes");

        navContainer.getChildren().addAll(
                secOverview, btnDashboard,
                secOperacion, btnAppointments, btnCustomers, btnVehicles, btnServices,
                secRecursos, btnEmployees, btnSupplies, btnCosting, btnReports
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brandBox, navContainer, spacer);
        return sidebar;
    }

    private Button createNavButton(String cardKey, String icon, String text) {
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("nav-icon");

        Button btn = new Button(text);
        btn.setGraphic(iconLbl);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> showCard(cardKey));

        navButtons.put(cardKey, btn);
        return btn;
    }

    /**
     * Construye el encabezado superior con título dinámico, buscador de módulos y perfil del usuario.
     */
    private HBox buildTopHeader() {
        HBox header = new HBox(18);
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        headerTitleLabel = new Label("Panel Administrativo General");
        headerTitleLabel.getStyleClass().add("header-page-title");
        headerSubtitleLabel = new Label("Resumen ejecutivo de operaciones");
        headerSubtitleLabel.getStyleClass().add("header-page-subtitle");
        titleBox.getChildren().addAll(headerTitleLabel, headerSubtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buscador rápido de módulos en el encabezado
        HBox searchBox = new HBox(6);
        searchBox.getStyleClass().add("search-box");
        Label searchIcon = new Label("🔍");
        searchIcon.getStyleClass().add("search-icon");
        TextField searchInput = new TextField();
        searchInput.setPromptText("Ir a módulo (ej. turnos, clientes, costeo)...");
        searchInput.setPrefWidth(250);
        searchInput.getStyleClass().add("search-input");
        searchInput.setOnAction(e -> handleQuickModuleSearch(searchInput.getText()));
        searchBox.getChildren().addAll(searchIcon, searchInput);

        // Perfil del usuario administrador y botón de cerrar sesión
        HBox profileBox = new HBox(10);
        profileBox.getStyleClass().add("user-profile-box");
        userAvatarLabel = new Label("AD");
        userAvatarLabel.getStyleClass().add("user-avatar");

        VBox userTexts = new VBox(1);
        userNameLabel = new Label("Admin Principal");
        userNameLabel.getStyleClass().add("user-name");
        Label userRole = new Label("Administrador");
        userRole.getStyleClass().add("user-role");
        userTexts.getChildren().addAll(userNameLabel, userRole);

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.getStyleClass().add("logout-btn");
        btnLogout.setOnAction(e -> handleLogout());

        profileBox.getChildren().addAll(userAvatarLabel, userTexts, btnLogout);

        header.getChildren().addAll(titleBox, spacer, searchBox, profileBox);
        return header;
    }

    private void handleQuickModuleSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String q = query.trim().toLowerCase();
        if (q.contains("dash") || q.contains("inicio") || q.contains("panel")) {
            showCard(CARD_DASHBOARD);
        } else if (q.contains("cli")) {
            showCard(CARD_CUSTOMERS);
        } else if (q.contains("veh") || q.contains("auto") || q.contains("placa")) {
            showCard(CARD_VEHICLES);
        } else if (q.contains("serv") || q.contains("cat")) {
            showCard(CARD_SERVICES);
        } else if (q.contains("turn") || q.contains("cit") || q.contains("agen")) {
            showCard(CARD_APPOINTMENTS);
        } else if (q.contains("emp") || q.contains("tec") || q.contains("per")) {
            showCard(CARD_EMPLOYEES);
        } else if (q.contains("ins") || q.contains("stock") || q.contains("mat")) {
            showCard(CARD_SUPPLIES);
        } else if (q.contains("cost") || q.contains("marg")) {
            showCard(CARD_COSTING);
        } else if (q.contains("rep") || q.contains("inform")) {
            showCard(CARD_REPORTS);
        }
    }

    /**
     * Construye la barra de estado inferior.
     */
    private HBox buildStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.getStyleClass().add("status-dot-online");

        statusLabel = new Label("Sistema activo • Módulo: Dashboard");
        statusLabel.getStyleClass().add("status-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rightInfo = new Label("Persistencia en Memoria Activa • Arquitectura POO + ServiceFactory");
        rightInfo.getStyleClass().add("status-bar-text");

        statusBar.getChildren().addAll(dot, statusLabel, spacer, rightInfo);
        return statusBar;
    }

    /**
     * Cambia la vista activa, actualiza el estado visual del menú lateral
     * y sincroniza los datos en tiempo real del módulo seleccionado.
     */
    public void showCard(String cardKey) {
        Node targetView = views.get(cardKey);
        if (targetView == null) {
            return;
        }

        // Refrescar datos del módulo antes de mostrarlo
        refreshModuleData(cardKey);

        contentContainer.getChildren().setAll(targetView);

        // Actualizar estilo activo en el menú lateral
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            Button btn = entry.getValue();
            btn.getStyleClass().remove("nav-button-active");
            if (entry.getKey().equals(cardKey)) {
                btn.getStyleClass().add("nav-button-active");
            }
        }

        // Actualizar textos del encabezado y barra de estado
        String[] meta = viewMetadata.get(cardKey);
        if (meta != null) {
            headerTitleLabel.setText(meta[0]);
            headerSubtitleLabel.setText(meta[1]);
            if (statusLabel != null) {
                statusLabel.setText("Módulo activo: " + meta[0] + "  |  AutoClean v2.0");
            }
        }
    }

    private void refreshModuleData(String cardKey) {
        switch (cardKey) {
            case CARD_DASHBOARD:
                dashboardPanel.refreshDashboard();
                break;
            case CARD_CUSTOMERS:
                customerPanel.loadCustomers();
                break;
            case CARD_VEHICLES:
                vehiclePanel.loadVehicles();
                break;
            case CARD_SERVICES:
                serviceTypePanel.loadServices();
                break;
            case CARD_APPOINTMENTS:
                appointmentPanel.loadAppointments();
                break;
            case CARD_EMPLOYEES:
                employeePanel.loadEmployees();
                break;
            case CARD_COSTING:
                costingPanel.refreshData();
                break;
            case CARD_REPORTS:
                reportPanel.generateReport(true);
                break;
            default:
                break;
        }
    }
}
