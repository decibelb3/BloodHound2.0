package com.bloodhound2.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Measurement {

    private final Long measurementId;
    private final long userId;
    private final Integer systolic;
    private final Integer diastolic;
    private final Double totalCholesterol;
    private final Double hdl;
    private final Double ldl;
    private final Double weight;
    private final LocalDateTime measurementDateTime;

    public Measurement(
            Long measurementId,
            long userId,
            Integer systolic,
            Integer diastolic,
            Double totalCholesterol,
            Double hdl,
            Double ldl,
            Double weight,
            LocalDateTime measurementDateTime) {
        this.measurementId = measurementId;
        this.userId = userId;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.totalCholesterol = totalCholesterol;
        this.hdl = hdl;
        this.ldl = ldl;
        this.weight = weight;
        this.measurementDateTime = Objects.requireNonNull(measurementDateTime, "measurementDateTime");
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public long getUserId() {
        return userId;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public Double getTotalCholesterol() {
        return totalCholesterol;
    }

    public Double getHdl() {
        return hdl;
    }

    public Double getLdl() {
        return ldl;
    }

    public Double getWeight() {
        return weight;
    }

    public LocalDateTime getMeasurementDateTime() {
        return measurementDateTime;
    }

    public Measurement withId(long id) {
        return new Measurement(
                id, userId, systolic, diastolic, totalCholesterol, hdl, ldl, weight, measurementDateTime);
    }
}
