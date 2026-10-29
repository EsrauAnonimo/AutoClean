package com.autoclean.ui.panels;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.model.Appointment;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la gestión de turnos de servicio y control de estados.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final SimpleFormPanel formPanel;
    private final JComboBox<String> statusComboBox;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AppointmentPanel() {
        this.appointmentService = ServiceFactory.getAppointmentService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título
        JLabel titleLabel = new JLabel("Gestión y Programación de Turnos");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.addField("ID");
        formPanel.addField("ID Cliente");
        formPanel.addField("ID Vehículo");
        formPanel.addField("ID Tipo Servicio");
        formPanel.addField("ID Empleado");
        formPanel.addField("Fecha y Hora (yyyy-MM-dd HH:mm)");

        // Selector de estado
        String[] statuses = {"PENDING", "IN_PROGRESS", "FINISHED", "CANCELLED"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(Theme.FONT_REGULAR);

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        statusWrapper.setBackground(Theme.GRIS_FONDO);
        JLabel lblStatus = new JLabel("Estado:");
        lblStatus.setFont(Theme.FONT_BOLD);
        lblStatus.setForeground(Theme.AZUL_OSCURO);
        statusWrapper.add(lblStatus);
        statusWrapper.add(statusComboBox);

        // Botones de acción
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        buttonsPanel.setBackground(Theme.BLANCO);

        JButton btnCreate = new JButton("Crear");
        JButton btnUpdate = new JButton("Actualizar");
        JButton btnChangeStatus = new JButton("Cambiar estado");
        JButton btnDelete = new JButton("Eliminar");
        JButton btnList = new JButton("Listar");
        JButton btnClear = new JButton("Limpiar");

        Theme.styleButton(btnCreate);
        Theme.styleButton(btnUpdate);
        Theme.styleButton(btnChangeStatus);
        Theme.styleSecondaryButton(btnDelete);
        Theme.styleButton(btnList);
        Theme.styleSecondaryButton(btnClear);

        buttonsPanel.add(btnCreate);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnChangeStatus);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnList);
        buttonsPanel.add(btnClear);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(Theme.BLANCO);
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(statusWrapper, BorderLayout.CENTER);
        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);

        // Tabla de turnos
        String[] columns = {"ID", "ID Cliente", "ID Vehículo", "ID Servicio", "ID Empleado", "Fecha/Hora", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);

        // Al seleccionar fila, cargar datos al formulario
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                formPanel.setFieldText("ID", String.valueOf(tableModel.getValueAt(row, 0)));
                formPanel.setFieldText("ID Cliente", String.valueOf(tableModel.getValueAt(row, 1)));
                formPanel.setFieldText("ID Vehículo", String.valueOf(tableModel.getValueAt(row, 2)));
                formPanel.setFieldText("ID Tipo Servicio", String.valueOf(tableModel.getValueAt(row, 3)));
                formPanel.setFieldText("ID Empleado", String.valueOf(tableModel.getValueAt(row, 4)));
                formPanel.setFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)", String.valueOf(tableModel.getValueAt(row, 5)));
                String st = String.valueOf(tableModel.getValueAt(row, 6));
                statusComboBox.setSelectedItem(st);
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Eventos
        btnCreate.addActionListener(e -> handleCreate());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnChangeStatus.addActionListener(e -> handleChangeStatus());
        btnDelete.addActionListener(e -> handleDelete());
        btnList.addActionListener(e -> loadAppointments());
        btnClear.addActionListener(e -> {
            formPanel.clear();
            statusComboBox.setSelectedIndex(0);
        });

        // Carga inicial
        loadAppointments();
    }

    /**
     * Parsea una cadena de texto a LocalDateTime soportando formato con espacio o con T.
     */
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

    /**
     * Valida campos y crea un nuevo turno de servicio.
     */
    private void handleCreate() {
        String customerId = formPanel.getFieldText("ID Cliente");
        String vehicleId = formPanel.getFieldText("ID Vehículo");
        String serviceTypeId = formPanel.getFieldText("ID Tipo Servicio");
        String employeeId = formPanel.getFieldText("ID Empleado");
        String dateTimeStr = formPanel.getFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)");
        String status = (String) statusComboBox.getSelectedItem();

        if (customerId.isEmpty() || vehicleId.isEmpty() || serviceTypeId.isEmpty() || dateTimeStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar cliente, vehículo, servicio y fecha/hora", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime dateTime = parseDateTime(dateTimeStr);
        if (dateTime == null) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use: yyyy-MM-dd HH:mm (ej. 2026-11-01 10:30)", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Appointment app = new Appointment(null, customerId, vehicleId, serviceTypeId, employeeId, dateTime, status);
            appointmentService.create(app);
            JOptionPane.showMessageDialog(this, "Turno registrado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear turno: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida campos y actualiza los datos del turno.
     */
    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String vehicleId = formPanel.getFieldText("ID Vehículo");
        String serviceTypeId = formPanel.getFieldText("ID Tipo Servicio");
        String employeeId = formPanel.getFieldText("ID Empleado");
        String dateTimeStr = formPanel.getFieldText("Fecha y Hora (yyyy-MM-dd HH:mm)");
        String status = (String) statusComboBox.getSelectedItem();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar o ingresar el ID del turno a actualizar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime dateTime = parseDateTime(dateTimeStr);
        if (dateTime == null) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use: yyyy-MM-dd HH:mm", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Appointment app = new Appointment(id, customerId, vehicleId, serviceTypeId, employeeId, dateTime, status);
            appointmentService.update(app);
            JOptionPane.showMessageDialog(this, "Turno actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar turno: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cambia únicamente el estado del turno especificado.
     */
    private void handleChangeStatus() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un turno o ingrese el ID para cambiar su estado", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus = (String) statusComboBox.getSelectedItem();
        try {
            appointmentService.updateStatus(id, newStatus);
            JOptionPane.showMessageDialog(this, "Estado del turno #" + id + " actualizado a " + newStatus, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cambiar estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el turno indicado por ID.
     */
    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe indicar el ID del turno a eliminar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el turno #" + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            appointmentService.delete(id);
            JOptionPane.showMessageDialog(this, "Turno eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar turno: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consulta y recarga los turnos desde el servicio.
     */
    private void loadAppointments() {
        try {
            List<Appointment> list = appointmentService.findAll();
            tableModel.setRowCount(0);
            if (list != null) {
                for (Appointment a : list) {
                    String dt = a.getDateTime() != null ? a.getDateTime().format(DATE_TIME_FORMATTER) : "";
                    tableModel.addRow(new Object[]{
                        a.getId(), a.getCustomerId(), a.getVehicleId(),
                        a.getServiceTypeId(), a.getEmployeeId(), dt, a.getStatus()
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar turnos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
