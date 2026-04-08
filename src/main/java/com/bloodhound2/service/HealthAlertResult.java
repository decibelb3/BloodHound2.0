package com.bloodhound2.service;

import java.util.List;

/**
 * Outcome of analyzing a set of vitals for user-facing messages. Built by {@link HealthAlertService}.
 */
public record HealthAlertResult(boolean hasConcerns, List<String> lines) {

    public String bodyText() {
        return String.join("\n\n", lines);
    }
}
