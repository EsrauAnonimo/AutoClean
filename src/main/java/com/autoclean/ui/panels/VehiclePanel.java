package com.autoclean.ui.panels;

import com.autoclean.contracts.VehicleService;
import com.autoclean.model.Vehicle;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la administración de vehículos (CRUD).
 */
public class VehiclePanel extends JPanel {

    private final VehicleService vehicleService;
    private final SimpleFormPanel formPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public VehiclePanel() {
        this.vehicleService = ServiceFactory.getVehicleService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título superior
        JLabel titleLabel = new JLabel("Gestión de Vehículos");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.addField("ID");
        formPanel.addField("ID Cliente");
        formPanel.addField("Placa");
        formPanel.addField("Marca");
        formPanel.addField("Modelo");
        formPanel.addField("Año");

        // Botones de acción
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonsPanel.setBackground(Theme.BLANCO);

        JButton btnCreate = new JButton("Crear");
        JButton btnUpdate = new JButton("Actualizar");
        JButton btnDelete = new JButton("Eliminar");
        JButton btnList = new JButton("Listar");
        JButton btnClear = new JButton("Limpiar");

        Theme.styleButton(btnCreate);
        Theme.styleButton(btnUpdate);
        Theme.styleSecondaryButton(btnDelete);
        Theme.styleButton(btnList);
        Theme.styleSecondaryButton(btnClear);

        buttonsPanel.add(btnCreate);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnList);
        buttonsPanel.add(btnClear);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(Theme.BLANCO);
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);

        // Tabla de vehículos
        String[] columns = {"ID", "ID Cliente", "Placa", "Marca", "Modelo", "Año"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);

        // Selección en tabla carga formulario
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                formPanel.setFieldText("ID", String.valueOf(tableModel.getValueAt(row, 0)));
                formPanel.setFieldText("ID Cliente", String.valueOf(tableModel.getValueAt(row, 1)));
                formPanel.setFieldText("Placa", String.valueOf(tableModel.getValueAt(row, 2)));
                formPanel.setFieldText("Marca", String.valueOf(tableModel.getValueAt(row, 3)));
                formPanel.setFieldText("Modelo", String.valueOf(tableModel.getValueAt(row, 4)));
                formPanel.setFieldText("Año", String.valueOf(tableModel.getValueAt(row, 5)));
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Eventos
        btnCreate.addActionListener(e -> handleCreate());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnList.addActionListener(e -> loadVehicles());
        btnClear.addActionListener(e -> formPanel.clear());

        // Carga inicial
        loadVehicles();
    }

    /**
     * Valida campos y solicita la creación de un nuevo vehículo.
     */
    private void handleCreate() {
        String customerId = formPanel.getFieldText("ID Cliente");
        String plate = formPanel.getFieldText("Placa");
        String brand = formPanel.getFieldText("Marca");
        String model = formPanel.getFieldText("Modelo");
        String yearStr = formPanel.getFieldText("Año");

        if (customerId.isEmpty() || plate.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los datos del vehículo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año debe ser un número entero válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Vehicle vehicle = new Vehicle(null, customerId, plate, brand, model, year);
            vehicleService.create(vehicle);
            JOptionPane.showMessageDialog(this, "Vehículo registrado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadVehicles();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear vehículo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida campos y actualiza el vehículo indicado por su ID.
     */
    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String customerId = formPanel.getFieldText("ID Cliente");
        String plate = formPanel.getFieldText("Placa");
        String brand = formPanel.getFieldText("Marca");
        String model = formPanel.getFieldText("Modelo");
        String yearStr = formPanel.getFieldText("Año");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar o seleccionar el ID a actualizar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (customerId.isEmpty() || plate.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los datos del vehículo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año debe ser un número entero válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Vehicle vehicle = new Vehicle(id, customerId, plate, brand, model, year);
            vehicleService.update(vehicle);
            JOptionPane.showMessageDialog(this, "Vehículo actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadVehicles();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar vehículo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el vehículo con el ID especificado.
     */
    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar o seleccionar el ID a eliminar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el vehículo #" + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            vehicleService.delete(id);
            JOptionPane.showMessageDialog(this, "Vehículo eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadVehicles();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar vehículo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consulta y recarga la lista de vehículos desde el servicio.
     */
    private void loadVehicles() {
        try {
            List<Vehicle> list = vehicleService.findAll();
            tableModel.setRowCount(0);
            if (list != null) {
                for (Vehicle v : list) {
                    tableModel.addRow(new Object[]{v.getId(), v.getCustomerId(), v.getPlate(), v.getBrand(), v.getModel(), v.getYear()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar vehículos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
