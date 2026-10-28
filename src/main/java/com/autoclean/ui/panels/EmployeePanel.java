package com.autoclean.ui.panels;

import com.autoclean.contracts.EmployeeService;
import com.autoclean.model.Employee;
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
 * Panel para la administración de empleados (CRUD).
 */
public class EmployeePanel extends JPanel {

    private final EmployeeService employeeService;
    private final SimpleFormPanel formPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public EmployeePanel() {
        this.employeeService = ServiceFactory.getEmployeeService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título superior
        JLabel titleLabel = new JLabel("Gestión de Empleados");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.addField("ID");
        formPanel.addField("Nombre");
        formPanel.addField("Rol");

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

        // Tabla de empleados
        String[] columns = {"ID", "Nombre", "Rol"};
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
                formPanel.setFieldText("Nombre", String.valueOf(tableModel.getValueAt(row, 1)));
                formPanel.setFieldText("Rol", String.valueOf(tableModel.getValueAt(row, 2)));
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Eventos
        btnCreate.addActionListener(e -> handleCreate());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnList.addActionListener(e -> loadEmployees());
        btnClear.addActionListener(e -> formPanel.clear());

        // Carga inicial
        loadEmployees();
    }

    /**
     * Valida campos y registra un nuevo empleado.
     */
    private void handleCreate() {
        String name = formPanel.getFieldText("Nombre");
        String role = formPanel.getFieldText("Rol");

        if (name.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y el rol del empleado", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Employee employee = new Employee(null, name, role);
            employeeService.create(employee);
            JOptionPane.showMessageDialog(this, "Empleado creado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear empleado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida campos y actualiza un empleado existente.
     */
    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String role = formPanel.getFieldText("Rol");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar o ingresar el ID del empleado", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y el rol del empleado", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Employee employee = new Employee(id, name, role);
            employeeService.update(employee);
            JOptionPane.showMessageDialog(this, "Empleado actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar empleado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el empleado seleccionado.
     */
    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar o ingresar el ID a eliminar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el empleado #" + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            employeeService.delete(id);
            JOptionPane.showMessageDialog(this, "Empleado eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadEmployees();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar empleado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consulta y recarga los empleados desde el servicio.
     */
    private void loadEmployees() {
        try {
            List<Employee> list = employeeService.findAll();
            tableModel.setRowCount(0);
            if (list != null) {
                for (Employee emp : list) {
                    tableModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getRole()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar empleados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
