package com.autoclean.ui.panels;

import com.autoclean.contracts.ReportService;
import com.autoclean.model.ReportResult;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.components.SimpleFormPanel;
import com.autoclean.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la generación y visualización de reportes gerenciales.
 */
public class ReportPanel extends JPanel {

    private final ReportService reportService;
    private final SimpleFormPanel formPanel;
    private final JLabel lblReportTitle;
    private final JLabel lblReportDate;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportPanel() {
        this.reportService = ServiceFactory.getReportService();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BLANCO);

        // Cabecera superior
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setBackground(Theme.BLANCO);

        JLabel mainTitle = new JLabel("Módulo de Reportes");
        Theme.styleTitleLabel(mainTitle);
        northPanel.add(mainTitle, BorderLayout.NORTH);

        // Formulario de fechas
        formPanel = new SimpleFormPanel();
        formPanel.addField("Fecha Inicio (yyyy-MM-dd)");
        formPanel.addField("Fecha Fin (yyyy-MM-dd)");

        // Sugerir rango por defecto (mes actual)
        LocalDate now = LocalDate.now();
        formPanel.setFieldText("Fecha Inicio (yyyy-MM-dd)", now.withDayOfMonth(1).format(DATE_FORMATTER));
        formPanel.setFieldText("Fecha Fin (yyyy-MM-dd)", now.format(DATE_FORMATTER));

        // Botones de generación de reportes
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonContainer.setBackground(Theme.BLANCO);

        JButton btnServices = new JButton("Servicios por periodo");
        JButton btnIncomeVsCost = new JButton("Ingresos vs Costos");
        JButton btnClear = new JButton("Limpiar");

        Theme.styleButton(btnServices);
        Theme.styleButton(btnIncomeVsCost);
        Theme.styleSecondaryButton(btnClear);

        buttonContainer.add(btnServices);
        buttonContainer.add(btnIncomeVsCost);
        buttonContainer.add(btnClear);

        JPanel controlsWrapper = new JPanel(new BorderLayout(5, 5));
        controlsWrapper.setBackground(Theme.BLANCO);
        controlsWrapper.add(formPanel, BorderLayout.CENTER);
        controlsWrapper.add(buttonContainer, BorderLayout.SOUTH);

        northPanel.add(controlsWrapper, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // Panel central de resultados del reporte
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Theme.BLANCO);

        JPanel reportHeaderPanel = new JPanel(new BorderLayout(5, 5));
        reportHeaderPanel.setBackground(Theme.GRIS_FONDO);

        lblReportTitle = new JLabel("Ningún reporte generado aún");
        lblReportTitle.setFont(Theme.FONT_HEADER);
        lblReportTitle.setForeground(Theme.AZUL_OSCURO);

        lblReportDate = new JLabel("");
        lblReportDate.setFont(Theme.FONT_REGULAR);

        reportHeaderPanel.add(lblReportTitle, BorderLayout.WEST);
        reportHeaderPanel.add(lblReportDate, BorderLayout.EAST);
        centerPanel.add(reportHeaderPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        Theme.styleTable(table);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Eventos
        btnServices.addActionListener(e -> generateReport(true));
        btnIncomeVsCost.addActionListener(e -> generateReport(false));
        btnClear.addActionListener(e -> {
            formPanel.clear();
            tableModel.setDataVector(new Object[][]{}, new Object[]{});
            lblReportTitle.setText("Ningún reporte generado aún");
            lblReportDate.setText("");
        });
    }

    /**
     * Valida fechas e invoca el método correspondiente de ReportService.
     *
     * @param isServices true para 'Servicios por periodo', false para 'Ingresos vs Costos'
     */
    private void generateReport(boolean isServices) {
        String fromStr = formPanel.getFieldText("Fecha Inicio (yyyy-MM-dd)");
        String toStr = formPanel.getFieldText("Fecha Fin (yyyy-MM-dd)");

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar las fechas de inicio y fin", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(fromStr, DATE_FORMATTER);
            toDate = LocalDate.parse(toStr, DATE_FORMATTER);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Debe ser yyyy-MM-dd (ej. 2026-10-01)", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (toDate.isBefore(fromDate)) {
            JOptionPane.showMessageDialog(this, "La fecha final no puede ser anterior a la inicial", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ReportResult result;
            if (isServices) {
                result = reportService.getServicesByPeriod(fromDate, toDate);
            } else {
                result = reportService.getIncomeVsCosts(fromDate, toDate);
            }

            if (result != null) {
                displayReport(result);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra el título y las filas del ReportResult en la tabla Swing.
     */
    private void displayReport(ReportResult result) {
        lblReportTitle.setText("Reporte: " + result.getTitle());
        if (result.getGeneratedAt() != null) {
            lblReportDate.setText("Generado: " + result.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            lblReportDate.setText("");
        }

        List<String[]> rows = result.getRows();
        if (rows == null || rows.isEmpty()) {
            tableModel.setDataVector(new Object[][]{}, new Object[]{"Mensaje"});
            tableModel.addRow(new Object[]{"No se encontraron datos para el rango especificado."});
            return;
        }

        // Determinar número de columnas por la primera fila
        int colCount = rows.get(0).length;
        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= colCount; i++) {
            columnNames.add("Columna " + i);
        }

        Vector<Vector<Object>> data = new Vector<>();
        for (String[] row : rows) {
            Vector<Object> rowVector = new Vector<>();
            for (String val : row) {
                rowVector.add(val);
            }
            data.add(rowVector);
        }

        tableModel.setDataVector(data, columnNames);
    }
}
