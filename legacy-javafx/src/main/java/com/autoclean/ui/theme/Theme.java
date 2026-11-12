package com.autoclean.ui.theme;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CostingService;
import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.EmployeeService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostLine;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.service.ServiceFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

/**
 * Gestor de estilos CSS, diálogos de notificación, etiquetas de estado
 * y datos iniciales de demostración para la interfaz JavaFX de AutoClean.
 */
public final class Theme {

    private static final String[] STYLESHEETS = {
        "/styles/main.css",
        "/styles/sidebar.css",
        "/styles/dashboard.css",
        "/styles/forms.css",
        "/styles/tables.css"
    };

    private static boolean seeded = false;

    private Theme() {
        // Clase utilitaria
    }

    /**
     * Carga todas las hojas de estilo CSS modulares en la escena principal de JavaFX.
     *
     * @param scene escena de JavaFX donde se aplicarán los estilos
     */
    public static void applyStylesheets(Scene scene) {
        if (scene == null) {
            return;
        }
        for (String path : STYLESHEETS) {
            URL resource = Theme.class.getResource(path);
            if (resource != null) {
                String externalForm = resource.toExternalForm();
                if (!scene.getStylesheets().contains(externalForm)) {
                    scene.getStylesheets().add(externalForm);
                }
            }
        }
    }

    /**
     * Crea una etiqueta (badge) estilizada según el estado del turno o servicio.
     *
     * @param status estado del turno (PENDING, IN_PROGRESS, FINISHED, CANCELLED)
     * @return componente Label con su clase CSS semántica
     */
    public static Label createStatusBadge(String status) {
        String normalized = status != null ? status.trim().toUpperCase() : "PENDING";
        String display;
        String styleClass;

        switch (normalized) {
            case "PENDING":
                display = "PENDIENTE";
                styleClass = "badge-pending";
                break;
            case "IN_PROGRESS":
                display = "EN PROCESO";
                styleClass = "badge-progress";
                break;
            case "FINISHED":
                display = "FINALIZADO";
                styleClass = "badge-finished";
                break;
            case "CANCELLED":
                display = "CANCELADO";
                styleClass = "badge-cancelled";
                break;
            default:
                display = normalized;
                styleClass = "badge-neutral";
                break;
        }

        Label badge = new Label(display);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }

    /**
     * Muestra un diálogo informativo en JavaFX.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de advertencia o validación en JavaFX.
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de error en JavaFX.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación (Sí / Cancelar) en JavaFX.
     *
     * @return true si el usuario confirma la acción
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleAlert(Alert alert) {
        if (alert.getDialogPane() != null && alert.getDialogPane().getScene() != null) {
            applyStylesheets(alert.getDialogPane().getScene());
        }
    }

    /**
     * Inicializa datos de demostración claramente identificados en los repositorios en memoria
     * únicamente si el sistema arranca vacío, para que el dashboard y las tablas luzcan completos.
     */
    public static synchronized void ensureSeedDataLoaded() {
        if (seeded) {
            return;
        }
        seeded = true;

        CustomerService customerService = ServiceFactory.getCustomerService();
        VehicleService vehicleService = ServiceFactory.getVehicleService();
        ServiceTypeService serviceTypeService = ServiceFactory.getServiceTypeService();
        EmployeeService employeeService = ServiceFactory.getEmployeeService();
        AppointmentService appointmentService = ServiceFactory.getAppointmentService();
        CostingService costingService = ServiceFactory.getCostingService();

        if (!customerService.findAll().isEmpty()) {
            return;
        }

        // Clientes iniciales de demostración
        customerService.create(new Customer("CLI-001", "Carlos Andrés Mendoza", "3104589210", "carlos.mendoza@email.com"));
        customerService.create(new Customer("CLI-002", "Laura Sofía Restrepo", "3158923411", "laura.restrepo@empresa.co"));
        customerService.create(new Customer("CLI-003", "Javier Eduardo Mora", "3007418529", "javier.mora@correo.com"));
        customerService.create(new Customer("CLI-004", "Valentina Gómez Ríos", "3206549871", "valentina.gomez@email.com"));

        // Vehículos asociados
        vehicleService.create(new Vehicle("VEH-001", "CLI-001", "KLM-482", "Mazda", "CX-30 Grand Touring", 2023));
        vehicleService.create(new Vehicle("VEH-002", "CLI-002", "JPR-915", "Toyota", "Corolla Cross Hybrid", 2024));
        vehicleService.create(new Vehicle("VEH-003", "CLI-003", "fgt-209", "BMW", "320i Sport Line", 2022));
        vehicleService.create(new Vehicle("VEH-004", "CLI-004", "MNB-774", "Kia", "Sportage Zenith", 2023));

        // Catálogo de servicios de embellecimiento automotriz
        serviceTypeService.create(new ServiceType("SRV-001", "Lavado Detallado Premium", "Lavado exterior con espuma activa, descontaminado y aspirado profundo", 65000.0));
        serviceTypeService.create(new ServiceType("SRV-002", "Porcelanizado Cerámico 9H", "Corrección de barniz y protección cerámica de alto brillo por 2 años", 480000.0));
        serviceTypeService.create(new ServiceType("SRV-003", "Limpieza de Tapicería e Interior", "Lavado en seco de cojinería, techo, paneles e hidratación de plásticos", 160000.0));
        serviceTypeService.create(new ServiceType("SRV-004", "Polichado y Brillo Espejo", "Eliminación de microrayones (swirls) y sellador sintético", 220000.0));

        // Equipo de empleados / técnicos
        employeeService.create(new Employee("EMP-001", "Andrés Felipe Salazar", "Especialista en Detailing y Cerámico"));
        employeeService.create(new Employee("EMP-002", "Miguel Ángel Torres", "Técnico de Lavado e Interiorismo"));
        employeeService.create(new Employee("EMP-003", "Diana Carolina Vélez", "Supervisora de Calidad y Recepción"));

        // Turnos del día y programados
        LocalDate today = LocalDate.now();
        appointmentService.create(new Appointment("TRN-001", "CLI-001", "VEH-001", "SRV-001", "EMP-002",
                LocalDateTime.of(today, LocalTime.of(9, 0)), "FINISHED"));
        appointmentService.create(new Appointment("TRN-002", "CLI-002", "VEH-002", "SRV-002", "EMP-001",
                LocalDateTime.of(today, LocalTime.of(10, 30)), "IN_PROGRESS"));
        appointmentService.create(new Appointment("TRN-003", "CLI-003", "VEH-003", "SRV-004", "EMP-001",
                LocalDateTime.of(today, LocalTime.of(14, 0)), "PENDING"));
        appointmentService.create(new Appointment("TRN-004", "CLI-004", "VEH-004", "SRV-003", "EMP-002",
                LocalDateTime.of(today, LocalTime.of(16, 0)), "PENDING"));

        // Estimaciones de costo iniciales asociadas
        costingService.estimateCost("SRV-001", "TRN-001", Arrays.asList(
                new CostLine("Shampoo pH Neutro + Microfibras", 12000.0),
                new CostLine("Mano de obra técnico (1.5h)", 18000.0)
        ));
        costingService.estimateCost("SRV-002", "TRN-002", Arrays.asList(
                new CostLine("Kit Recubrimiento Cerámico 9H", 115000.0),
                new CostLine("Compuestos de pulimento y pads", 35000.0),
                new CostLine("Mano de obra especialista", 95000.0)
        ));
        costingService.estimateCost("SRV-004", "TRN-003", Arrays.asList(
                new CostLine("Cera Carnauba y Sellador", 28000.0),
                new CostLine("Mano de obra polichado", 52000.0)
        ));
    }
}
