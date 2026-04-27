package com.bloodhound2.service;

import com.bloodhound2.model.Measurement;
import javafx.scene.chart.XYChart;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementChartDataServiceTest {

    private final MeasurementChartDataService service = new MeasurementChartDataService();

    @Test
    void systolicSeries_buildTrendPointsInChronologicalOrder() {
        Measurement older = measurementWith(1L, 5L, 118, null, LocalDateTime.of(2026, 4, 20, 8, 0));
        Measurement newer = measurementWith(2L, 5L, 124, null, LocalDateTime.of(2026, 4, 21, 8, 0));

        XYChart.Series<String, Number> series = service.systolicSeries(List.of(older, newer));

        assertEquals(2, series.getData().size());
        assertTrue(series.getData().get(0).getXValue().contains("4/20/26"));
        assertTrue(series.getData().get(1).getXValue().contains("4/21/26"));
        assertEquals(118.0, series.getData().get(0).getYValue().doubleValue());
        assertEquals(124.0, series.getData().get(1).getYValue().doubleValue());
    }

    @Test
    void systolicSeries_skipNullValues() {
        Measurement missing = measurementWith(1L, 5L, null, null, LocalDateTime.of(2026, 4, 20, 8, 0));
        Measurement present = measurementWith(2L, 5L, 124, null, LocalDateTime.of(2026, 4, 21, 8, 0));

        XYChart.Series<String, Number> series = service.systolicSeries(List.of(missing, present));

        assertEquals(1, series.getData().size());
        assertEquals(124.0, series.getData().get(0).getYValue().doubleValue());
    }

    @Test
    void systolicSeries_returnUserScopedDataOnly_whenInputAlreadyScoped() {
        Measurement user5 = measurementWith(1L, 5L, 118, null, LocalDateTime.of(2026, 4, 20, 8, 0));
        Measurement user5b = measurementWith(2L, 5L, 124, null, LocalDateTime.of(2026, 4, 21, 8, 0));

        XYChart.Series<String, Number> series = service.systolicSeries(List.of(user5, user5b));

        assertEquals(2, series.getData().size());
        assertEquals(118.0, series.getData().get(0).getYValue().doubleValue());
        assertEquals(124.0, series.getData().get(1).getYValue().doubleValue());
    }

    private static Measurement measurementWith(
            long id, long userId, Integer systolic, Integer diastolic, LocalDateTime at) {
        return new Measurement(id, userId, systolic, diastolic, null, null, null, null, null, at);
    }
}
