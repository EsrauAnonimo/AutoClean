package com.autoclean.ui.panels;

import com.autoclean.contracts.CostingService;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para el costeo de servicios y cálculo de margen preliminar.
 */
public class CostingPanel extends JPanel {

    private final CostingService costingService;
    private final SimpleFormPanel headerFormPanel;
    private final JTextField txtConcept;
    private final JTextField txtAmount;
    private final DefaultTableModel linesTableModel;
    private final JTable linesTable;

    private final JLabel lblEstimatedCost;
    private final JLabel lblPrice;
    private final JLabel lblDifference;

    public CostingPanel() {
        this.costingService = ServiceFactory.getCostingService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Título superior
        JLabel titleLabel = new JLabel("Módulo de Estimación de Costos");
        Theme.styleTitleLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        // Panel superior para configuración del servicio
        headerFormPanel = new SimpleFormPanel();
        headerFormPanel.addField("ID Tipo Servicio");
        headerFormPanel.addField("ID Turno (Opcional)");

        // Panel de entrada para líneas de costo
        JPanel lineInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        lineInputPanel.setBackground(Theme.GRIS_FONDO);
        lineInputPanel.setBorder(BorderFactory.createTitledBorder("Agregar Ítem / Línea de Costo"));

        JLabel lblConcept = new JLabel("Concepto:");
        lblConcept.setFont(Theme.FONT_BOLD);
        txtConcept = new JTextField(12);
        txtConcept.setFont(Theme.FONT_REGULAR);

        JLabel lblAmount = new JLabel("Monto ($):");
        lblAmount.setFont(Theme.FONT_BOLD);
        txtAmount = new JTextField(8);
        txtAmount.setFont(Theme.FONT_REGULAR);

        JButton btnAddLine = new JButton("Agregar Línea");
        JButton btnRemoveLine = new JButton("Quitar Selección");
        Theme.styleButton(btnAddLine);
        Theme.styleSecondaryButton(btnRemoveLine);

        lineInputPanel.add(lblConcept);
        lineInputPanel.add(txtConcept);
        lineInputPanel.add(lblAmount);
        lineInputPanel.add(txtAmount);
        lineInputPanel.add(btnAddLine);
        lineInputPanel.add(btnRemoveLine);

        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setBackground(Theme.BLANCO);
        topContainer.add(headerFormPanel, BorderLayout.NORTH);
        topContainer.add(lineInputPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.WEST);

        // Tabla temporal de líneas de costo
        String[] columns = {"Concepto", "Monto ($)"};
        linesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        linesTable = new JTable(linesTableModel);
        Theme.styleTable(linesTable);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Theme.BLANCO);
        centerPanel.setBorder(BorderFactory.createTitledBorder("Líneas de Costo Directo"));
        centerPanel.add(new JScrollPane(linesTable), BorderLayout.CENTER);

        // Panel inferior con botón de estimación y resultados
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));
        bottomContainer.setBackground(Theme.BLANCO);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionPanel.setBackground(Theme.BLANCO);
        JButton btnEstimate = new JButton("Estimar costo");
        JButton btnClearAll = new JButton("Limpiar todo");
        Theme.styleButton(btnEstimate);
        Theme.styleSecondaryButton(btnClearAll);
        actionPanel.add(btnEstimate);
        actionPanel.add(btnClearAll);

        // Panel de métricas calculadas
        JPanel resultsCard = new JPanel(new GridLayout(2, 2, 10, 5));
        resultsCard.setBackground(Theme.GRIS_FONDO);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.AZUL_OSCURO, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        lblEstimatedCost = new JLabel("Costo estimado: $0.00");
        lblEstimatedCost.setFont(Theme.FONT_BOLD);
        lblPrice = new JLabel("Precio al público: $0.00");
        lblPrice.setFont(Theme.FONT_BOLD);
        lblDifference = new JLabel("Diferencia calculada: $0.00");
        lblDifference.setFont(Theme.FONT_BOLD);

        // Nota obligatoria solicitada en los requerimientos
        JLabel lblWarning = new JLabel("Nota: La diferencia no es ganancia neta");
        lblWarning.setFont(Theme.FONT_BOLD);
        lblWarning.setForeground(new Color(0xB00020));

        resultsCard.add(lblEstimatedCost);
        resultsCard.add(lblPrice);
        resultsCard.add(lblDifference);
        resultsCard.add(lblWarning);

        bottomContainer.add(actionPanel, BorderLayout.NORTH);
        bottomContainer.add(resultsCard, BorderLayout.SOUTH);

        centerPanel.add(bottomContainer, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Acciones de botones
        btnAddLine.addActionListener(e -> handleAddCostLine());
        btnRemoveLine.addActionListener(e -> handleRemoveSelectedLine());
        btnEstimate.addActionListener(e -> handleEstimateCost());
        btnClearAll.addActionListener(e -> handleClearAll());
    }

    /**
     * Agrega un ítem de costo a la tabla temporal.
     */
    private void handleAddCostLine() {
        String concept = txtConcept.getText().trim();
        String amountStr = txtAmount.getText().trim();

        if (concept.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el concepto y el monto del costo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                JOptionPane.showMessageDialog(this, "El monto no puede ser negativo", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            linesTableModel.addRow(new Object[]{concept, amount});
            txtConcept.setText("");
            txtAmount.setText("");
            txtConcept.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un valor numérico válido", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Remueve la fila seleccionada de la tabla temporal.
     */
    private void handleRemoveSelectedLine() {
        int selectedRow = linesTable.getSelectedRow();
        if (selectedRow != -1) {
            linesTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione la fila que desea eliminar de la tabla", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Invoca al servicio CostingService para calcular la estimación.
     */
    private void handleEstimateCost() {
        String serviceTypeId = headerFormPanel.getFieldText("ID Tipo Servicio");
        String appointmentId = headerFormPanel.getFieldText("ID Turno (Opcional)");

        if (serviceTypeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el ID del Tipo de Servicio para la estimación", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (linesTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos una línea de costo a la lista", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CostLine> lines = new ArrayList<>();
        for (int i = 0; i < linesTableModel.getRowCount(); i++) {
            String concept = String.valueOf(linesTableModel.getValueAt(i, 0));
            double amount = Double.parseDouble(String.valueOf(linesTableModel.getValueAt(i, 1)));
            lines.add(new CostLine(concept, amount));
        }

        try {
            CostEstimate estimate;
            if (appointmentId.isEmpty()) {
                estimate = costingService.estimateCost(serviceTypeId, lines);
            } else {
                estimate = costingService.estimateCost(serviceTypeId, appointmentId, lines);
            }

            if (estimate != null) {
                lblEstimatedCost.setText(String.format("Costo estimado: $%.2f", estimate.getEstimatedCost()));
                lblPrice.setText(String.format("Precio al público: $%.2f", estimate.getPrice()));
                lblDifference.setText(String.format("Diferencia calculada: $%.2f", estimate.getDifference()));
                JOptionPane.showMessageDialog(this, "Estimación de costo generada correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al estimar costo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia los campos del panel y restablece las etiquetas.
     */
    private void handleClearAll() {
        headerFormPanel.clear();
        txtConcept.setText("");
        txtAmount.setText("");
        linesTableModel.setRowCount(0);
        lblEstimatedCost.setText("Costo estimado: $0.00");
        lblPrice.setText("Precio al público: $0.00");
        lblDifference.setText("Diferencia calculada: $0.00");
    }
}
