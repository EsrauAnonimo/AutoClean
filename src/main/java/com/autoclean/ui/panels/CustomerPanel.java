package com.autoclean.ui.panels;

import com.autoclean.contracts.CustomerService;
import com.autoclean.model.Customer;
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
 * Panel para la administración de clientes (CRUD).
 */
public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private final SimpleFormPanel formPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public CustomerPanel() {
        this.customerService = ServiceFactory.getCustomerService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título superior
        JLabel titleLabel = new JLabel("Gestión de Clientes");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Formulario central-izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.addField("ID");
        formPanel.addField("Nombre");
        formPanel.addField("Teléfono");
        formPanel.addField("Correo");

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

        // Tabla de datos
        String[] columns = {"ID", "Nombre", "Teléfono", "Correo"};
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
                formPanel.setFieldText("Teléfono", String.valueOf(tableModel.getValueAt(row, 2)));
                formPanel.setFieldText("Correo", String.valueOf(tableModel.getValueAt(row, 3)));
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Configuración de eventos
        btnCreate.addActionListener(e -> handleCreate());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnList.addActionListener(e -> loadCustomers());
        btnClear.addActionListener(e -> formPanel.clear());

        // Carga inicial
        loadCustomers();
    }

    /**
     * Valida los campos y llama al servicio para crear un cliente.
     */
    private void handleCreate() {
        String name = formPanel.getFieldText("Nombre");
        String phone = formPanel.getFieldText("Teléfono");
        String email = formPanel.getFieldText("Correo");

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar nombre, teléfono y correo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Customer customer = new Customer(null, name, phone, email);
            customerService.create(customer);
            JOptionPane.showMessageDialog(this, "Cliente creado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida los campos y llama al servicio para actualizar un cliente.
     */
    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String phone = formPanel.getFieldText("Teléfono");
        String email = formPanel.getFieldText("Correo");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe especificar el ID del cliente a actualizar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los datos del cliente", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Customer customer = new Customer(id, name, phone, email);
            customerService.update(customer);
            JOptionPane.showMessageDialog(this, "Cliente actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Llama al servicio para eliminar el cliente seleccionado.
     */
    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar o ingresar el ID del cliente a eliminar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el cliente #" + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            customerService.delete(id);
            JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadCustomers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consulta y recarga la lista de clientes en la tabla.
     */
    private void loadCustomers() {
        try {
            List<Customer> list = customerService.findAll();
            tableModel.setRowCount(0);
            if (list != null) {
                for (Customer c : list) {
                    tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getEmail()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
