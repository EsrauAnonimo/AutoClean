package com.autoclean.ui.panels;

import com.autoclean.contracts.ReportService;
import com.autoclean.model.ReportResult;
import com.autoclean.service.ServiceFactory;
import com.autoclean.ui.theme.Theme;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel JavaFX para la generación y visualización de reportes gerenciales por periodo.
 * Conserva toda la conexión con ReportService y presenta encabezados reales y legibles.
 */
public class ReportPanel extends BorderPane {

    private final ReportService reportService;
    private final DatePicker dpFrom;
    private final DatePicker dpTo;
    private final TextField txtFrom;
    private final TextField txtTo;

    private final Label lblReportTitle;
    private final Label lblReportDate;
    private final Label lblRowCountBadge;
    private final TableView<String[]> table;
    private final ObservableList<String[]> tableData;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportPanel() {
        this.reportService = ServiceFactory.getReportService();
        this.dpFrom = new DatePicker(LocalDate.now().withDayOfMonth(1));
        this.dpTo = new DatePicker(LocalDate.now().plusDays(7));
        this.txtFrom = new TextField(dpFrom.getValue().format(DATE_FORMATTER));
        this.txtTo = new TextField(dpTo.getValue().format(DATE_FORMATTER));

        this.lblReportTitle = new Label("Seleccione un tipo de reporte para consultar");
        this.lblReportDate = new Label("");
        this.lblRowCountBadge = new Label("0 registros");
        this.tableData = FXCollections.observableArrayList();
        this.table = new TableView<>(tableData);

        getStyleClass().add("content-area");

        //Sincronizar DatePickers con los campos de texto yyyy-MM-dd
        dpFrom.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtFrom.setText(newV.format(DATE_FORMATTER));
            }
        });
        dpTo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtTo.setText(newV.format(DATE_FORMATTER));
            }
        });

        // Barra superior de filtros de fecha y botones de reporte
        VBox filterCard = new VBox(14);
        filterCard.getStyleClass().add("card");

        HBox filterHeader = new HBox(10);
        filterHeader.setAlignment(Pos.CENTER_LEFT);
        VBox titleGroup = new VBox(2);
        Label lblMain = new Label("Generador de Reportes Gerenciales");
        lblMain.getStyleClass().add("card-title");
        Label lblSub = new Label("Consulte servicios ejecutados o el balance comparativo de ingresos vs costos estimados por rango de fechas");
        lblSub.getStyleClass().add("card-subtitle");
        titleGroup.getChildren().addAll(lblMain, lblSub);
        filterHeader.getChildren().add(titleGroup);

        HBox controlsRow = new HBox(14);
        controlsRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox fromGroup = new VBox(4);
        Label lblFrom = new Label("Fecha Inicio (yyyy-MM-dd)");
        lblFrom.getStyleClass().add("field-label");
        HBox fromInputs = new HBox(6, txtFrom, dpFrom);
        txtFrom.setPrefWidth(130);
        dpFrom.setPrefWidth(135);
        fromGroup.getChildren().addAll(lblFrom, fromInputs);

        VBox toGroup = new VBox(4);
        Label lblTo = new Label("Fecha Fin (yyyy-MM-dd)");
        lblTo.getStyleClass().add("field-label");
        HBox toInputs = new HBox(6, txtTo, dpTo);
        txtTo.setPrefWidth(130);
        dpTo.setPrefWidth(135);
        toGroup.getChildren().addAll(lblTo, toInputs);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnServices = new Button("📋 Servicios por Periodo");
        Button btnIncomeVsCost = new Button("💰 Ingresos vs Costos");
        Button btnClear = new Button("Limpiar");

        btnServices.getStyleClass().add("btn-primary");
        btnIncomeVsCost.getStyleClass().add("btn-dark");
        btnClear.getStyleClass().add("btn-secondary");

        HBox buttonsBox = new HBox(10, btnServices, btnIncomeVsCost, btnClear);
        buttonsBox.setAlignment(Pos.BOTTOM_RIGHT);

        controlsRow.getChildren().addAll(fromGroup, toGroup, spacer, buttonsBox);
        filterCard.getChildren().addAll(filterHeader, controlsRow);

        // Tarjeta central de resultados tabulados
        VBox resultsCard = new VBox(12);
        resultsCard.getStyleClass().add("card");
        VBox.setVgrow(resultsCard, Priority.ALWAYS);

        HBox reportMetaHeader = new HBox(12);
        reportMetaHeader.setAlignment(Pos.CENTER_LEFT);
        lblReportTitle.getStyleClass().add("card-title");
        lblRowCountBadge.getStyleClass().addAll("badge", "badge-progress");
        Region metaSpacer = new Region();
        HBox.setHgrow(metaSpacer, Priority.ALWAYS);
        lblReportDate.getStyleClass().add("card-subtitle");
        reportMetaHeader.getChildren().addAll(lblReportTitle, lblRowCountBadge, metaSpacer, lblReportDate);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Presione 'Servicios por Periodo' o 'Ingresos vs Costos' para visualizar los datos."));
        VBox.setVgrow(table, Priority.ALWAYS);

        resultsCard.getChildren().addAll(reportMetaHeader, table);

        VBox mainContainer = new VBox(18, filterCard, resultsCard);
        VBox.setVgrow(resultsCard, Priority.ALWAYS);
        setCenter(mainContainer);

        // Eventos
        btnServices.setOnAction(e -> generateReport(true));
        btnIncomeVsCost.setOnAction(e -> generateReport(false));
        btnClear.setOnAction(e -> {
            tableData.clear();
            table.getColumns().clear();
            lblReportTitle.setText("Ningún reporte generado aún");
            lblReportDate.setText("");
            lblRowCountBadge.setText("0 registros");
        });

        // Generar vista inicial automáticamente con los datos actuales
        generateReport(true);
    }

    public final void generateReport(boolean isServices) {
        String fromStr = txtFrom.getText() != null ? txtFrom.getText().trim() : "";
        String toStr = txtTo.getText() != null ? txtTo.getText().trim() : "";

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            Theme.showWarning("Validación", "Debe ingresar las fechas de inicio y fin.");
            return;
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(fromStr, DATE_FORMATTER);
            toDate = LocalDate.parse(toStr, DATE_FORMATTER);
        } catch (Exception ex) {
            Theme.showWarning("Validación", "Formato de fecha inválido. Use yyyy-MM-dd (ej. 2026-10-01).");
            return;
        }

        if (toDate.isBefore(fromDate)) {
            Theme.showWarning("Validación", "La fecha final no puede ser anterior a la fecha inicial.");
            return;
        }

        try {
            ReportResult result = isServices
                    ? reportService.getServicesByPeriod(fromDate, toDate)
                    : reportService.getIncomeVsCosts(fromDate, toDate);

            if (result != null) {
                displayReport(result, isServices);
            }
        } catch (Exception ex) {
            Theme.showError("Error al generar reporte", ex.getMessage());
        }
    }

    private void displayReport(ReportResult result, boolean isServices) {
        lblReportTitle.setText(result.getTitle());
        if (result.getGeneratedAt() != null) {
            lblReportDate.setText("Generado: " + result.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            lblReportDate.setText("");
        }

        List<String[]> rows = result.getRows();
        table.getColumns().clear();
        tableData.clear();

        if (rows == null || rows.isEmpty()) {
            lblRowCountBadge.setText("0 registros");
            table.setPlaceholder(new Label("Sin datos registrados en el periodo consultado (" + txtFrom.getText() + " al " + txtTo.getText() + ")."));
            return;
        }

        String[] headers = isServices
                ? new String[]{"ID Turno", "Cliente", "Servicio Realizado", "Estado", "Fecha y Hora"}
                : new String[]{"Servicio", "Precio Público ($)", "Costo Estimado ($)", "Diferencia Bruta ($)"};

        int colCount = rows.get(0).length;
        for (int i = 0; i < colCount; i++) {
            final int colIndex = i;
            String headerTitle = i < headers.length ? headers[i] : "Columna " + (i + 1);
            TableColumn<String[], String> col = new TableColumn<>(headerTitle);
            col.setCellValueFactory(cell -> {
                String[] r = cell.getValue();
                String val = (r != null && colIndex < r.length && r[colIndex] != null) ? r[colIndex] : "";
                return new SimpleStringProperty(val);
            });
            table.getColumns().add(col);
        }

        tableData.setAll(rows);
        lblRowCountBadge.setText(rows.size() + (rows.size() == 1 ? " registro" : " registros"));
    }
}
