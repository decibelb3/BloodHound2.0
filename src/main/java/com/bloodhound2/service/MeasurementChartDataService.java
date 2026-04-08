package com.bloodhound2.service;

import com.bloodhound2.model.Measurement;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Builds {@link XYChart.Series} for time-series charts from {@link Measurement} rows loaded from the
 * database. Skips null metric values; x-axis labels use {@link Measurement#getMeasurementDateTime()}.
 */
public final class MeasurementChartDataService {

    private static final DateTimeFormatter X_AXIS_LABEL =
            DateTimeFormatter.ofPattern("M/d/yy h:mm a", Locale.US);

    public String xAxisLabel(Measurement m) {
        return X_AXIS_LABEL.format(m.getMeasurementDateTime());
    }

    public XYChart.Series<String, Number> systolicSeries(List<Measurement> chronological) {
        return buildSeries("Systolic", chronological, m -> m.getSystolic() != null, m -> m.getSystolic().doubleValue());
    }

    public XYChart.Series<String, Number> diastolicSeries(List<Measurement> chronological) {
        return buildSeries("Diastolic", chronological, m -> m.getDiastolic() != null, m -> m.getDiastolic().doubleValue());
    }

    public XYChart.Series<String, Number> totalCholesterolSeries(List<Measurement> chronological) {
        return buildSeries(
                "Total cholesterol",
                chronological,
                m -> m.getTotalCholesterol() != null,
                m -> m.getTotalCholesterol().doubleValue());
    }

    public XYChart.Series<String, Number> hdlSeries(List<Measurement> chronological) {
        return buildSeries("HDL", chronological, m -> m.getHdl() != null, m -> m.getHdl().doubleValue());
    }

    public XYChart.Series<String, Number> ldlSeries(List<Measurement> chronological) {
        return buildSeries("LDL", chronological, m -> m.getLdl() != null, m -> m.getLdl().doubleValue());
    }

    public XYChart.Series<String, Number> weightSeries(List<Measurement> chronological) {
        return buildSeries("Weight", chronological, m -> m.getWeight() != null, m -> m.getWeight().doubleValue());
    }

    /**
     * Applies series to a line chart; clears previous data and disables animation for stable updates.
     */
    public void applyBloodPressureChart(LineChart<String, Number> chart, List<Measurement> chronological) {
        applyChart(chart, List.of(systolicSeries(chronological), diastolicSeries(chronological)));
    }

    public void applyCholesterolChart(LineChart<String, Number> chart, List<Measurement> chronological) {
        applyChart(
                chart,
                List.of(
                        totalCholesterolSeries(chronological),
                        hdlSeries(chronological),
                        ldlSeries(chronological)));
    }

    public void applyWeightChart(LineChart<String, Number> chart, List<Measurement> chronological) {
        applyChart(chart, List.of(weightSeries(chronological)));
    }

    private static void applyChart(LineChart<String, Number> chart, List<XYChart.Series<String, Number>> series) {
        chart.setAnimated(false);
        chart.getData().clear();
        chart.getData().addAll(series);
    }

    private XYChart.Series<String, Number> buildSeries(
            String name,
            List<Measurement> chronological,
            java.util.function.Predicate<Measurement> include,
            java.util.function.ToDoubleFunction<Measurement> value) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Measurement m : chronological) {
            if (include.test(m)) {
                series.getData().add(new XYChart.Data<>(xAxisLabel(m), value.applyAsDouble(m)));
            }
        }
        return series;
    }
}
