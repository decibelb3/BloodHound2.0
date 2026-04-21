package com.bloodhound2.controller;

import com.bloodhound2.app.AppContext;
import com.bloodhound2.app.AppNavigator;
import com.bloodhound2.model.Measurement;
import com.bloodhound2.service.HealthAlertResult;
import com.bloodhound2.service.HealthAlertService;
import com.bloodhound2.service.MeasurementChartDataService;
import com.bloodhound2.service.MeasurementService;
import com.bloodhound2.service.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    /** US-style: month/day/year with 12-hour clock and AM/PM (not 24-hour). */
    private static final DateTimeFormatter DT_DISPLAY =
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US);

    private final MeasurementService measurementService;
    private final MeasurementChartDataService chartDataService;
    private final HealthAlertService healthAlertService;
    private final AppNavigator navigator;

    @FXML
    private Label welcomeLabel;

    @FXML
    private TabPane dashboardTabPane;

    @FXML
    private ComboBox<String> orderCombo;

    @FXML
    private TableView<Measurement> measurementTable;

    @FXML
    private TableColumn<Measurement, String> dateTimeColumn;

    @FXML
    private TableColumn<Measurement, String> systolicColumn;

    @FXML
    private TableColumn<Measurement, String> diastolicColumn;

    @FXML
    private TableColumn<Measurement, String> tcColumn;

    @FXML
    private TableColumn<Measurement, String> hdlColumn;

    @FXML
    private TableColumn<Measurement, String> ldlColumn;

    @FXML
    private TableColumn<Measurement, String> weightColumn;

    @FXML
    private TableColumn<Measurement, Void> actionColumn;

    @FXML
    private TextField systolicField;

    @FXML
    private TextField diastolicField;

    @FXML
    private TextField totalCholesterolField;

    @FXML
    private TextField hdlField;

    @FXML
    private TextField ldlField;

    @FXML
    private TextField weightField;

    @FXML
    private TextField measurementDateTimeField;

    @FXML
    private Label formErrorLabel;

    @FXML
    private Label healthSummaryLabel;

    @FXML
    private Label chartsNoDataLabel;

    @FXML
    private ScrollPane chartsScrollPane;

    @FXML
    private LineChart<String, Number> bpChart;

    @FXML
    private LineChart<String, Number> cholesterolChart;

    @FXML
    private LineChart<String, Number> weightChart;

    public DashboardController(AppContext context, AppNavigator navigator) {
        this.measurementService = context.measurementService;
        this.chartDataService = context.measurementChartDataService;
        this.healthAlertService = context.healthAlertService;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        var user = SessionContext.getCurrentUser();
        if (user == null) {
            navigator.showLogin();
            return;
        }
        welcomeLabel.setText("Signed in as " + user.getUsername());
        healthSummaryLabel.setText("Save a measurement to see a quick health summary here.");

        orderCombo.setItems(FXCollections.observableArrayList("Newest first", "Oldest first (time series)"));
        orderCombo.getSelectionModel().selectFirst();
        orderCombo.setOnAction(e -> refreshTable());

        dateTimeColumn.setCellValueFactory(
                c -> new SimpleStringProperty(DT_DISPLAY.format(c.getValue().getMeasurementDateTime())));
        systolicColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getSystolic())));
        diastolicColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getDiastolic())));
        tcColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getTotalCholesterol())));
        hdlColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getHdl())));
        ldlColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getLdl())));
        weightColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getWeight())));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Measurement m = getTableView().getItems().get(getIndex());
                    onDeleteMeasurement(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        measurementDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
        refreshTable();

        dashboardTabPane
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener((obs, oldIdx, newIdx) -> {
                    if (newIdx != null && newIdx.intValue() == 1) {
                        refreshCharts();
                    }
                });
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @FXML
    private void onLogout() {
        SessionContext.clear();
        navigator.showLogin();
    }

    @FXML
    private void onAddMeasurement() {
        formErrorLabel.setText("");
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            formErrorLabel.setText("Not signed in.");
            return;
        }
        try {
            Integer sys = parseIntOrNull(systolicField.getText());
            Integer dia = parseIntOrNull(diastolicField.getText());
            Double tc = parseDoubleOrNull(totalCholesterolField.getText());
            Double hdl = parseDoubleOrNull(hdlField.getText());
            Double ldl = parseDoubleOrNull(ldlField.getText());
            Double w = parseDoubleOrNull(weightField.getText());
            LocalDateTime when = parseDateTimeOrNow(measurementDateTimeField.getText());

            measurementService.addMeasurement(user.getUserId(), sys, dia, tc, hdl, ldl, w, when);
            HealthAlertResult health = healthAlertService.analyze(sys, dia, tc, hdl, ldl);
            applyHealthSummary(health);
            showHealthAlertDialog(health);
            clearForm();
            measurementDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
            refreshTable();
            refreshCharts();
        } catch (NumberFormatException e) {
            formErrorLabel.setText("Enter valid numbers for numeric fields.");
        } catch (DateTimeParseException e) {
            formErrorLabel.setText("Use US date/time, e.g. 3/31/2026 3:45 PM");
        } catch (IllegalArgumentException e) {
            formErrorLabel.setText(e.getMessage());
        } catch (SQLException e) {
            formErrorLabel.setText("Could not save measurement.");
        }
    }

    private void onDeleteMeasurement(Measurement measurement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete measurement");
        confirm.setHeaderText("Delete this measurement?");
        confirm.setContentText(
                "Recorded on " + DT_DISPLAY.format(measurement.getMeasurementDateTime()) + "\nThis cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            var user = SessionContext.getCurrentUser();
            if (user == null || user.getUserId() == null || measurement.getMeasurementId() == null) {
                formErrorLabel.setText("Could not delete measurement.");
                return;
            }
            try {
                measurementService.deleteMeasurement(measurement.getMeasurementId(), user.getUserId());
                refreshTable();
                refreshCharts();
            } catch (SQLException e) {
                formErrorLabel.setText("Could not delete measurement.");
            }
        });
    }

    private void clearForm() {
        systolicField.clear();
        diastolicField.clear();
        totalCholesterolField.clear();
        hdlField.clear();
        ldlField.clear();
        weightField.clear();
    }

    private void refreshTable() {
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            List<Measurement> rows;
            boolean newestFirst = orderCombo.getSelectionModel().getSelectedIndex() == 0;
            if (newestFirst) {
                rows = measurementService.listForUserNewestFirst(user.getUserId());
            } else {
                rows = measurementService.listForUserChronological(user.getUserId());
            }
            measurementTable.setItems(FXCollections.observableArrayList(rows));
        } catch (SQLException e) {
            formErrorLabel.setText("Could not load measurements.");
        }
    }

    /** Loads chronological rows from MySQL for the logged-in user and updates line charts. */
    private void refreshCharts() {
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            chartsNoDataLabel.setText("No data available");
            List<Measurement> chronological = measurementService.listForUserChronological(user.getUserId());
            boolean empty = chronological.isEmpty();
            chartsNoDataLabel.setVisible(empty);
            chartsNoDataLabel.setManaged(empty);
            chartsScrollPane.setVisible(!empty);
            chartsScrollPane.setManaged(!empty);
            if (empty) {
                bpChart.getData().clear();
                cholesterolChart.getData().clear();
                weightChart.getData().clear();
                return;
            }
            chartDataService.applyBloodPressureChart(bpChart, chronological);
            chartDataService.applyCholesterolChart(cholesterolChart, chronological);
            chartDataService.applyWeightChart(weightChart, chronological);
            tiltChartTimeLabels(bpChart);
            tiltChartTimeLabels(cholesterolChart);
            tiltChartTimeLabels(weightChart);
        } catch (SQLException e) {
            chartsNoDataLabel.setText("Could not load chart data.");
            chartsNoDataLabel.setVisible(true);
            chartsNoDataLabel.setManaged(true);
            chartsScrollPane.setVisible(false);
            chartsScrollPane.setManaged(false);
        }
    }

    private static void tiltChartTimeLabels(LineChart<String, Number> chart) {
        if (chart.getXAxis() instanceof CategoryAxis axis) {
            axis.setTickLabelRotation(-45);
        }
    }

    private void applyHealthSummary(HealthAlertResult result) {
        healthSummaryLabel.setText(result.bodyText());
    }

    private static void showHealthAlertDialog(HealthAlertResult result) {
        Alert dialog = new Alert(result.hasConcerns() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
        dialog.setTitle("Health summary");
        dialog.setHeaderText(dialogHeaderFor(result));
        dialog.setContentText(result.bodyText());
        dialog.showAndWait();
    }

    private static String dialogHeaderFor(HealthAlertResult result) {
        if (result.hasConcerns()) {
            return "Here is what we noticed";
        }
        String text = result.bodyText();
        if (text.contains("Your measurement was saved")) {
            return "Measurement saved";
        }
        return "You are doing well";
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return Integer.parseInt(s.trim());
    }

    private static Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return Double.parseDouble(s.trim());
    }

    private static LocalDateTime parseDateTimeOrNow(String s) {
        if (s == null || s.isBlank()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(s.trim(), DT_DISPLAY);
    }
}
