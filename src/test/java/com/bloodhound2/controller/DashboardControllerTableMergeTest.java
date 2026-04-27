package com.bloodhound2.controller;

import com.bloodhound2.model.Measurement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardControllerTableMergeTest {

    @Test
    void mergeRowsForDisplay_mergesBpAndCholRowsInSameMinute() {
        LocalDateTime sameMinute = LocalDateTime.of(2026, 4, 27, 8, 29, 50);
        Measurement chol =
                measurementWith(11L, 1L, null, null, 200.0, 60.0, 80.0, null, "chol", sameMinute);
        Measurement bp =
                measurementWith(12L, 1L, 120, 78, null, null, null, 180.0, "bp", sameMinute.minusSeconds(40));

        List<Measurement> merged = DashboardController.mergeRowsForDisplay(List.of(chol, bp));

        assertEquals(1, merged.size());
        Measurement row = merged.get(0);
        assertEquals(120, row.getSystolic());
        assertEquals(78, row.getDiastolic());
        assertEquals(200.0, row.getTotalCholesterol());
        assertEquals(60.0, row.getHdl());
        assertEquals(80.0, row.getLdl());
        assertEquals(180.0, row.getWeight());
        assertEquals(11L, row.getMeasurementId());
        assertTrue(row.getNotes().contains("Chol: chol"));
        assertTrue(row.getNotes().contains("BP: bp"));
    }

    @Test
    void mergeRowsForDisplay_keepsSeparateRowsWhenMinutesDiffer() {
        Measurement first =
                measurementWith(11L, 1L, null, null, 200.0, 60.0, 80.0, null, null, LocalDateTime.of(2026, 4, 27, 8, 29));
        Measurement second =
                measurementWith(12L, 1L, 120, 78, null, null, null, 180.0, null, LocalDateTime.of(2026, 4, 27, 8, 31));

        List<Measurement> merged = DashboardController.mergeRowsForDisplay(List.of(first, second));

        assertEquals(2, merged.size());
    }

    private static Measurement measurementWith(
            long id,
            long userId,
            Integer systolic,
            Integer diastolic,
            Double totalCholesterol,
            Double hdl,
            Double ldl,
            Double weight,
            String notes,
            LocalDateTime when) {
        return new Measurement(id, userId, systolic, diastolic, totalCholesterol, hdl, ldl, weight, notes, when);
    }
}
