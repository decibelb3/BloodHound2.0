package com.bloodhound2.controller;

import com.bloodhound2.model.Measurement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DashboardControllerSummaryTest {

    @Test
    void deriveLatestSummaryMetrics_combinesLatestNonNullValuesAcrossSeparateRows() {
        Measurement latestBpOnly =
                measurementWith(10L, 1L, 126, 82, null, null, null, LocalDateTime.of(2026, 4, 27, 9, 0));
        Measurement latestCholOnly =
                measurementWith(9L, 1L, null, null, 198.0, 55.0, 112.0, LocalDateTime.of(2026, 4, 27, 8, 45));
        Measurement olderMixed =
                measurementWith(8L, 1L, 120, 78, 185.0, 60.0, 100.0, LocalDateTime.of(2026, 4, 26, 20, 0));

        DashboardController.SummaryMetrics metrics =
                DashboardController.deriveLatestSummaryMetrics(List.of(latestBpOnly, latestCholOnly, olderMixed));

        assertEquals(126, metrics.systolic());
        assertEquals(82, metrics.diastolic());
        assertEquals(198.0, metrics.totalCholesterol());
        assertEquals(55.0, metrics.hdl());
        assertEquals(112.0, metrics.ldl());
    }

    @Test
    void deriveLatestSummaryMetrics_returnsNullsWhenMetricNeverProvided() {
        Measurement onlyBp =
                measurementWith(10L, 1L, 122, 80, null, null, null, LocalDateTime.of(2026, 4, 27, 9, 0));

        DashboardController.SummaryMetrics metrics =
                DashboardController.deriveLatestSummaryMetrics(List.of(onlyBp));

        assertEquals(122, metrics.systolic());
        assertEquals(80, metrics.diastolic());
        assertNull(metrics.totalCholesterol());
        assertNull(metrics.hdl());
        assertNull(metrics.ldl());
    }

    private static Measurement measurementWith(
            long id,
            long userId,
            Integer systolic,
            Integer diastolic,
            Double totalCholesterol,
            Double hdl,
            Double ldl,
            LocalDateTime when) {
        return new Measurement(id, userId, systolic, diastolic, totalCholesterol, hdl, ldl, null, null, when);
    }
}
