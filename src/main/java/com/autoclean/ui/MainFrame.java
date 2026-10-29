package com.autoclean.ui;

import com.autoclean.ui.panels.AppointmentPanel;
import com.autoclean.ui.panels.CostingPanel;
import com.autoclean.ui.panels.CustomerPanel;
import com.autoclean.ui.panels.EmployeePanel;
import com.autoclean.ui.panels.ReportPanel;
import com.autoclean.ui.panels.ServiceTypePanel;
import com.autoclean.ui.panels.VehiclePanel;
import com.autoclean.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Ventana principal de la aplicación AutoClean con navegación CardLayout y menú superior.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardsContainer;
    private final JLabel statusLabel;

    public static final String CARD_CUSTOMERS = "CUSTOMERS";
    public static final String CARD_VEHICLES = "VEHICLES";
    public static final String CARD_SERVICES = "SERVICES";
    public static final String CARD_EMPLOYEES = "EMPLOYEES";
    public static final String CARD_APPOINTMENTS = "APPOINTMENTS";
    public static final String CARD_COSTING = "COSTING";
    public static final String CARD_REPORTS = "REPORTS";

    public MainFrame() {
        setTitle("AutoClean - Sistema de Gestión de Centros de Embellecimiento Automotriz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);

        // Contenedor principal con CardLayout
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);

        // Registro de los módulos de la aplicación
        cardsContainer.add(new CustomerPanel(), CARD_CUSTOMERS);
        cardsContainer.add(new VehiclePanel(), CARD_VEHICLES);
        cardsContainer.add(new ServiceTypePanel(), CARD_SERVICES);
        cardsContainer.add(new EmployeePanel(), CARD_EMPLOYEES);
        cardsContainer.add(new AppointmentPanel(), CARD_APPOINTMENTS);
        cardsContainer.add(new CostingPanel(), CARD_COSTING);
        cardsContainer.add(new ReportPanel(), CARD_REPORTS);

        // Barra de menú superior (JMenuBar)
        setJMenuBar(buildMenuBar());

        // Barra de estado inferior
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusBar.setBackground(Theme.GRIS_FONDO);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.GRIS_BORDE));
        statusLabel = new JLabel("Módulo activo: Clientes | AutoClean v1.0.0");
        statusLabel.setFont(Theme.FONT_REGULAR);
        statusBar.add(statusLabel);

        // Ensamblado
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cardsContainer, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Mostrar módulo inicial
        showCard(CARD_CUSTOMERS, "Clientes");
    }

    /**
     * Construye la barra de menú con las opciones requeridas.
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Theme.AZUL_OSCURO);

        // Menú Módulos
        JMenu menuModules = new JMenu("Módulos de Gestión");
        menuModules.setForeground(Theme.BLANCO);
        menuModules.setFont(Theme.FONT_BOLD);

        JMenuItem miCustomers = new JMenuItem("Clientes");
        JMenuItem miVehicles = new JMenuItem("Vehículos");
        JMenuItem miServices = new JMenuItem("Servicios");
        JMenuItem miEmployees = new JMenuItem("Empleados");
        JMenuItem miAppointments = new JMenuItem("Turnos");
        JMenuItem miCosting = new JMenuItem("Costeo");
        JMenuItem miReports = new JMenuItem("Reportes");

        miCustomers.addActionListener(e -> showCard(CARD_CUSTOMERS, "Clientes"));
        miVehicles.addActionListener(e -> showCard(CARD_VEHICLES, "Vehículos"));
        miServices.addActionListener(e -> showCard(CARD_SERVICES, "Servicios"));
        miEmployees.addActionListener(e -> showCard(CARD_EMPLOYEES, "Empleados"));
        miAppointments.addActionListener(e -> showCard(CARD_APPOINTMENTS, "Turnos"));
        miCosting.addActionListener(e -> showCard(CARD_COSTING, "Costeo"));
        miReports.addActionListener(e -> showCard(CARD_REPORTS, "Reportes"));

        menuModules.add(miCustomers);
        menuModules.add(miVehicles);
        menuModules.add(miServices);
        menuModules.add(miEmployees);
        menuModules.add(miAppointments);
        menuModules.addSeparator();
        menuModules.add(miCosting);
        menuModules.add(miReports);

        // Menús individuales de acceso directo en la barra
        menuBar.add(menuModules);
        menuBar.add(createTopMenuDirect("Clientes", () -> showCard(CARD_CUSTOMERS, "Clientes")));
        menuBar.add(createTopMenuDirect("Vehículos", () -> showCard(CARD_VEHICLES, "Vehículos")));
        menuBar.add(createTopMenuDirect("Servicios", () -> showCard(CARD_SERVICES, "Servicios")));
        menuBar.add(createTopMenuDirect("Empleados", () -> showCard(CARD_EMPLOYEES, "Empleados")));
        menuBar.add(createTopMenuDirect("Turnos", () -> showCard(CARD_APPOINTMENTS, "Turnos")));
        menuBar.add(createTopMenuDirect("Costeo", () -> showCard(CARD_COSTING, "Costeo")));
        menuBar.add(createTopMenuDirect("Reportes", () -> showCard(CARD_REPORTS, "Reportes")));

        return menuBar;
    }

    /**
     * Crea un menú en la barra que funciona como acceso directo al hacer click.
     */
    private JMenu createTopMenuDirect(String title, Runnable action) {
        JMenu menu = new JMenu(title);
        menu.setForeground(Theme.BLANCO);
        menu.setFont(Theme.FONT_BOLD);
        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
        return menu;
    }

    /**
     * Cambia la tarjeta activa en el CardLayout y actualiza la barra de estado.
     */
    public void showCard(String cardName, String displayName) {
        cardLayout.show(cardsContainer, cardName);
        if (statusLabel != null) {
            statusLabel.setText("Módulo activo: " + displayName + " | AutoClean v1.0.0");
        }
    }
}
