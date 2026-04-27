package com.bloodhound2.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthAlertServiceTest {

    private final HealthAlertService service = new HealthAlertService();

    @Test
    void analyze_handleEmptyDataset_returnsHelpfulNoDataMessage() {
        HealthAlertResult result = service.analyze(null, null, null, null, null);

        assertFalse(result.hasConcerns());
        assertFalse(result.lines().isEmpty());
        assertTrue(result.lines().get(0).contains("measurement was saved"));
    }

    @Test
    void analyze_validBloodPressureAndCholesterol_returnsNoConcerns() {
        HealthAlertResult result = service.analyze(118, 76, 170.0, 55.0, 95.0);

        assertFalse(result.hasConcerns());
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("healthy range")));
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("within common goals")));
    }

    @Test
    void analyze_highRiskValues_returnsConcerns() {
        HealthAlertResult result = service.analyze(185, 121, 245.0, 35.0, 160.0);

        assertTrue(result.hasConcerns());
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("very high")));
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("LDL")));
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("HDL")));
    }

    @Test
    void analyze_lowBloodPressure_returnsConcern() {
        HealthAlertResult result = service.analyze(88, 58, null, null, null);

        assertTrue(result.hasConcerns());
        assertTrue(result.lines().stream().anyMatch(line -> line.contains("lower than typical")));
    }
}
