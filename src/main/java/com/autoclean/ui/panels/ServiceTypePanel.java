package com.autoclean.ui.panels;

import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.model.ServiceType;
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
 * Panel para la administración de tipos de servicio (CRUD).
 */
public class ServiceTypePanel extends JPanel {

    private final ServiceTypeService serviceTypeService;
    private final SimpleFormPanel formPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public ServiceTypePanel() {
        this.serviceTypeService = ServiceFactory.getServiceTypeService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título superior
        JLabel titleLabel = new JLabel("Catálogo de Tipos de Servicio");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Formulario izquierdo
        formPanel = new SimpleFormPanel();
        formPanel.addField("ID");
        formPanel.addField("Nombre");
        formPanel.addField("Descripción");
        formPanel.addField("Precio");

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

        // Tabla de tipos de servicio
        String[] columns = {"ID", "Nombre", "Descripción", "Precio"};
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
                formPanel.setFieldText("Descripción", String.valueOf(tableModel.getValueAt(row, 2)));
                formPanel.setFieldText("Precio", String.valueOf(tableModel.getValueAt(row, 3)));
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Eventos
        btnCreate.addActionListener(e -> handleCreate());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnList.addActionListener(e -> loadServices());
        btnClear.addActionListener(e -> formPanel.clear());

        // Carga inicial
        loadServices();
    }

    /**
     * Valida campos y crea un nuevo tipo de servicio.
     */
    private void handleCreate() {
        String name = formPanel.getFieldText("Nombre");
        String description = formPanel.getFieldText("Descripción");
        String priceStr = formPanel.getFieldText("Precio");

        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y el precio del servicio", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número positivo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ServiceType serviceType = new ServiceType(null, name, description, price);
            serviceTypeService.create(serviceType);
            JOptionPane.showMessageDialog(this, "Servicio registrado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear servicio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida campos y actualiza un servicio existente.
     */
    private void handleUpdate() {
        String id = formPanel.getFieldText("ID");
        String name = formPanel.getFieldText("Nombre");
        String description = formPanel.getFieldText("Descripción");
        String priceStr = formPanel.getFieldText("Precio");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe indicar o seleccionar el ID a actualizar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y el precio del servicio", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número positivo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ServiceType serviceType = new ServiceType(id, name, description, price);
            serviceTypeService.update(serviceType);
            JOptionPane.showMessageDialog(this, "Servicio actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar servicio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el tipo de servicio especificado por ID.
     */
    private void handleDelete() {
        String id = formPanel.getFieldText("ID");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe indicar o seleccionar el ID a eliminar", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el servicio #" + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceTypeService.delete(id);
            JOptionPane.showMessageDialog(this, "Servicio eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            formPanel.clear();
            loadServices();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar servicio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consulta y recarga los tipos de servicio desde el servicio.
     */
    private void loadServices() {
        try {
            List<ServiceType> list = serviceTypeService.findAll();
            tableModel.setRowCount(0);
            if (list != null) {
                for (ServiceType st : list) {
                    tableModel.addRow(new Object[]{st.getId(), st.getName(), st.getDescription(), st.getPrice()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar servicios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
