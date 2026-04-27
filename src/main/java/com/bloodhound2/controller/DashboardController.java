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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private TableColumn<Measurement, String> notesColumn;

    @FXML
    private TableColumn<Measurement, Void> actionColumn;

    @FXML
    private TextField systolicField;

    @FXML
    private TextField diastolicField;

    @FXML
    private TextField weightField;

    @FXML
    private TextField bpDateTimeField;

    @FXML
    private TextField bpNotesField;

    @FXML
    private Label bpFormErrorLabel;

    @FXML
    private TextField totalCholesterolField;

    @FXML
    private TextField hdlField;

    @FXML
    private TextField ldlField;

    @FXML
    private TextField cholDateTimeField;

    @FXML
    private TextField cholNotesField;

    @FXML
    private Label cholFormErrorLabel;

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
        tcColumn.setCellValueFactory(c -> new SimpleStringProperty(formatCholesterol(c.getValue().getTotalCholesterol())));
        hdlColumn.setCellValueFactory(c -> new SimpleStringProperty(formatCholesterol(c.getValue().getHdl())));
        ldlColumn.setCellValueFactory(c -> new SimpleStringProperty(formatCholesterol(c.getValue().getLdl())));
        weightColumn.setCellValueFactory(c -> new SimpleStringProperty(formatCholesterol(c.getValue().getWeight())));
        notesColumn.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getNotes())));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(4, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Measurement m = getTableView().getItems().get(getIndex());
                    onEditMeasurement(m);
                });
                deleteBtn.setOnAction(e -> {
                    Measurement m = getTableView().getItems().get(getIndex());
                    onDeleteMeasurement(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Measurement m = getTableView().getItems().get(getIndex());
                // Synthetic merged rows do not map to a single DB record.
                setGraphic(m.getMeasurementId() == null ? null : box);
            }
        });

        bpDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
        cholDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
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

    private static String formatCholesterol(Double value) {
        if (value == null) {
            return "";
        }
        double rounded = Math.rint(value);
        if (Double.compare(value, rounded) == 0) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(value);
    }

    @FXML
    private void onLogout() {
        SessionContext.clear();
        navigator.showLogin();
    }

    @FXML
    private void onAddBloodPressure() {
        bpFormErrorLabel.setText("");
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            bpFormErrorLabel.setText("Not signed in.");
            return;
        }
        try {
            Integer sys = parseIntOrNull(systolicField.getText());
            Integer dia = parseIntOrNull(diastolicField.getText());
            Double w = parseDoubleOrNull(weightField.getText());
            String notes = bpNotesField.getText();
            LocalDateTime when = parseDateTimeOrNow(bpDateTimeField.getText());

            measurementService.addMeasurement(user.getUserId(), sys, dia, null, null, null, w, notes, when);
            HealthAlertResult health = healthAlertService.analyze(sys, dia, null, null, null);
            showHealthAlertDialog(health);
            clearBpForm();
            refreshTable();
            refreshCharts();
        } catch (NumberFormatException e) {
            bpFormErrorLabel.setText("Enter valid numbers for numeric fields.");
        } catch (DateTimeParseException e) {
            bpFormErrorLabel.setText("Use US date/time, e.g. 3/31/2026 3:45 PM");
        } catch (IllegalArgumentException e) {
            bpFormErrorLabel.setText(e.getMessage());
        } catch (SQLException e) {
            bpFormErrorLabel.setText("Could not save measurement.");
        }
    }

    @FXML
    private void onAddCholesterol() {
        cholFormErrorLabel.setText("");
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            cholFormErrorLabel.setText("Not signed in.");
            return;
        }
        try {
            Double tc = parseDoubleOrNull(totalCholesterolField.getText());
            Double hdl = parseDoubleOrNull(hdlField.getText());
            Double ldl = parseDoubleOrNull(ldlField.getText());
            String notes = cholNotesField.getText();
            LocalDateTime when = parseDateTimeOrNow(cholDateTimeField.getText());

            measurementService.addMeasurement(user.getUserId(), null, null, tc, hdl, ldl, null, notes, when);
            HealthAlertResult health = healthAlertService.analyze(null, null, tc, hdl, ldl);
            showHealthAlertDialog(health);
            clearCholForm();
            refreshTable();
            refreshCharts();
        } catch (NumberFormatException e) {
            cholFormErrorLabel.setText("Enter valid numbers for numeric fields.");
        } catch (DateTimeParseException e) {
            cholFormErrorLabel.setText("Use US date/time, e.g. 3/31/2026 3:45 PM");
        } catch (IllegalArgumentException e) {
            cholFormErrorLabel.setText(e.getMessage());
        } catch (SQLException e) {
            cholFormErrorLabel.setText("Could not save measurement.");
        }
    }

    private void onEditMeasurement(Measurement original) {
        Dialog<Measurement> dialog = new Dialog<>();
        dialog.setTitle("Edit measurement");
        dialog.setHeaderText("Editing measurement from " + DT_DISPLAY.format(original.getMeasurementDateTime()));

        boolean hasBpData =
                original.getSystolic() != null || original.getDiastolic() != null || original.getWeight() != null;
        boolean hasCholData =
                original.getTotalCholesterol() != null || original.getHdl() != null || original.getLdl() != null;
        boolean showBpSection = hasBpData || !hasCholData;
        boolean showCholSection = hasCholData || !hasBpData;

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField sysField = new TextField(str(original.getSystolic()));
        TextField diaField = new TextField(str(original.getDiastolic()));
        TextField tcField = new TextField(str(original.getTotalCholesterol()));
        TextField hdlEditField = new TextField(str(original.getHdl()));
        TextField ldlEditField = new TextField(str(original.getLdl()));
        TextField wField = new TextField(str(original.getWeight()));
        TextField dtField = new TextField(DT_DISPLAY.format(original.getMeasurementDateTime()));
        TextField notesEditField = new TextField(str(original.getNotes()));
        notesEditField.setPromptText("Optional notes");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;
        if (showBpSection) {
            grid.add(new Label("Systolic"), 0, row);
            grid.add(sysField, 1, row);
            grid.add(new Label("Diastolic"), 2, row);
            grid.add(diaField, 3, row);
            row++;
            grid.add(new Label("Weight"), 0, row);
            grid.add(wField, 1, row);
            row++;
        }
        if (showCholSection) {
            grid.add(new Label("Total chol."), 0, row);
            grid.add(tcField, 1, row);
            grid.add(new Label("HDL"), 2, row);
            grid.add(hdlEditField, 3, row);
            row++;
            grid.add(new Label("LDL"), 0, row);
            grid.add(ldlEditField, 1, row);
            row++;
        }
        grid.add(new Label("Date / time"), 0, row);
        grid.add(dtField, 1, row);
        row++;
        grid.add(new Label("Notes"), 0, row);
        grid.add(notesEditField, 1, row);
        GridPane.setColumnSpan(notesEditField, 3);
        row++;
        grid.add(errorLabel, 0, row);
        GridPane.setColumnSpan(errorLabel, 4);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                if (showBpSection) {
                    parseIntOrNull(sysField.getText());
                    parseIntOrNull(diaField.getText());
                    parseDoubleOrNull(wField.getText());
                }
                if (showCholSection) {
                    parseDoubleOrNull(tcField.getText());
                    parseDoubleOrNull(hdlEditField.getText());
                    parseDoubleOrNull(ldlEditField.getText());
                }
                parseDateTimeOrNow(dtField.getText());
                errorLabel.setText("");
            } catch (NumberFormatException e) {
                errorLabel.setText("Enter valid numbers for numeric fields.");
                event.consume();
            } catch (DateTimeParseException e) {
                errorLabel.setText("Use US date/time, e.g. 3/31/2026 3:45 PM");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButtonType) return null;
            return new Measurement(
                    original.getMeasurementId(),
                    original.getUserId(),
                    showBpSection ? parseIntOrNull(sysField.getText()) : original.getSystolic(),
                    showBpSection ? parseIntOrNull(diaField.getText()) : original.getDiastolic(),
                    showCholSection ? parseDoubleOrNull(tcField.getText()) : original.getTotalCholesterol(),
                    showCholSection ? parseDoubleOrNull(hdlEditField.getText()) : original.getHdl(),
                    showCholSection ? parseDoubleOrNull(ldlEditField.getText()) : original.getLdl(),
                    showBpSection ? parseDoubleOrNull(wField.getText()) : original.getWeight(),
                    notesEditField.getText(),
                    parseDateTimeOrNow(dtField.getText()));
        });

        dialog.showAndWait().ifPresent(updated -> {
            try {
                measurementService.editMeasurement(updated);
                refreshTable();
                refreshCharts();
            } catch (SQLException e) {
                bpFormErrorLabel.setText("Could not update measurement.");
            }
        });
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
                bpFormErrorLabel.setText("Could not delete measurement.");
                return;
            }
            try {
                measurementService.deleteMeasurement(measurement.getMeasurementId(), user.getUserId());
                refreshTable();
                refreshCharts();
            } catch (SQLException e) {
                bpFormErrorLabel.setText("Could not delete measurement.");
            }
        });
    }

    private void clearBpForm() {
        systolicField.clear();
        diastolicField.clear();
        weightField.clear();
        bpNotesField.clear();
        bpDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
    }

    private void clearCholForm() {
        totalCholesterolField.clear();
        hdlField.clear();
        ldlField.clear();
        cholNotesField.clear();
        cholDateTimeField.setText(DT_DISPLAY.format(LocalDateTime.now()));
    }

    private void refreshTable() {
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            long userId = user.getUserId();
            List<Measurement> newestRows = measurementService.listForUserNewestFirst(userId);
            List<Measurement> rows;
            boolean newestFirst = orderCombo.getSelectionModel().getSelectedIndex() == 0;
            if (newestFirst) {
                rows = mergeRowsForDisplay(newestRows);
            } else {
                rows = mergeRowsForDisplay(measurementService.listForUserChronological(userId));
            }
            measurementTable.setItems(FXCollections.observableArrayList(rows));
            refreshLatestHealthSummary(newestRows);
        } catch (SQLException e) {
            bpFormErrorLabel.setText("Could not load measurements.");
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
        // Keep dashboard summary compact on one line even when multiple insights exist.
        String singleLine = result.bodyText().replace("\n\n", " ").replace('\n', ' ').trim();
        healthSummaryLabel.setText(singleLine);
    }

    static List<Measurement> mergeRowsForDisplay(List<Measurement> rowsInDisplayOrder) {
        Map<LocalDateTime, Measurement> mergedByMinute = new LinkedHashMap<>();
        for (Measurement row : rowsInDisplayOrder) {
            LocalDateTime key = row.getMeasurementDateTime().truncatedTo(ChronoUnit.MINUTES);
            Measurement current = mergedByMinute.get(key);
            if (current == null) {
                mergedByMinute.put(key, row);
                continue;
            }
            mergedByMinute.put(key, mergeTwoRowsForDisplay(current, row, key));
        }
        return new ArrayList<>(mergedByMinute.values());
    }

    private static Measurement mergeTwoRowsForDisplay(
            Measurement a, Measurement b, LocalDateTime displayDateTime) {
        Integer systolic = firstNonNull(a.getSystolic(), b.getSystolic());
        Integer diastolic = firstNonNull(a.getDiastolic(), b.getDiastolic());
        Double totalCholesterol = firstNonNull(a.getTotalCholesterol(), b.getTotalCholesterol());
        Double hdl = firstNonNull(a.getHdl(), b.getHdl());
        Double ldl = firstNonNull(a.getLdl(), b.getLdl());
        Double weight = firstNonNull(a.getWeight(), b.getWeight());
        String notes = mergeNotes(a, b);
        Long displayId = firstNonNull(a.getMeasurementId(), b.getMeasurementId());

        return new Measurement(
                displayId,
                a.getUserId(),
                systolic,
                diastolic,
                totalCholesterol,
                hdl,
                ldl,
                weight,
                notes,
                displayDateTime);
    }

    private static <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return first != null ? first : second;
    }

    private static String mergeNotes(Measurement firstRow, Measurement secondRow) {
        String a = formatNoteWithSource(firstRow);
        String b = formatNoteWithSource(secondRow);
        if (a == null && b == null) {
            return "";
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.equals(b)) {
            return a;
        }
        return a + " | " + b;
    }

    private static String formatNoteWithSource(Measurement row) {
        String note = firstNonBlank(row.getNotes(), null);
        if (note == null) {
            return null;
        }
        boolean hasBp = row.getSystolic() != null || row.getDiastolic() != null || row.getWeight() != null;
        boolean hasChol = row.getTotalCholesterol() != null || row.getHdl() != null || row.getLdl() != null;
        if (hasBp && !hasChol) {
            return "BP: " + note;
        }
        if (hasChol && !hasBp) {
            return "Chol: " + note;
        }
        return "Entry: " + note;
    }

    private void refreshLatestHealthSummary(List<Measurement> newestFirstRows) {
        SummaryMetrics metrics = deriveLatestSummaryMetrics(newestFirstRows);
        HealthAlertResult summary =
                healthAlertService.analyze(
                        metrics.systolic(),
                        metrics.diastolic(),
                        metrics.totalCholesterol(),
                        metrics.hdl(),
                        metrics.ldl());
        applyHealthSummary(summary);
    }

    static SummaryMetrics deriveLatestSummaryMetrics(List<Measurement> newestFirstRows) {
        Integer systolic = null;
        Integer diastolic = null;
        Double totalCholesterol = null;
        Double hdl = null;
        Double ldl = null;

        for (Measurement row : newestFirstRows) {
            if (systolic == null && row.getSystolic() != null) {
                systolic = row.getSystolic();
            }
            if (diastolic == null && row.getDiastolic() != null) {
                diastolic = row.getDiastolic();
            }
            if (totalCholesterol == null && row.getTotalCholesterol() != null) {
                totalCholesterol = row.getTotalCholesterol();
            }
            if (hdl == null && row.getHdl() != null) {
                hdl = row.getHdl();
            }
            if (ldl == null && row.getLdl() != null) {
                ldl = row.getLdl();
            }
            if (systolic != null
                    && diastolic != null
                    && totalCholesterol != null
                    && hdl != null
                    && ldl != null) {
                break;
            }
        }

        return new SummaryMetrics(systolic, diastolic, totalCholesterol, hdl, ldl);
    }

    static record SummaryMetrics(
            Integer systolic, Integer diastolic, Double totalCholesterol, Double hdl, Double ldl) {}

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
