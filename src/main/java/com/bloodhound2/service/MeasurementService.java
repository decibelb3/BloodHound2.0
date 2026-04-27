package com.bloodhound2.service;

import com.bloodhound2.dao.MeasurementDao;
import com.bloodhound2.model.Measurement;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MeasurementService {

    private final MeasurementDao measurementDao;

    public MeasurementService(MeasurementDao measurementDao) {
        this.measurementDao = measurementDao;
    }

    public long addMeasurement(
            long userId,
            Integer systolic,
            Integer diastolic,
            Double totalCholesterol,
            Double hdl,
            Double ldl,
            Double weight,
            String notes,
            LocalDateTime measurementDateTime)
            throws SQLException {
        if (measurementDateTime == null) {
            throw new IllegalArgumentException("Measurement date/time is required.");
        }
        Measurement m =
                new Measurement(
                        null,
                        userId,
                        systolic,
                        diastolic,
                        totalCholesterol,
                        hdl,
                        ldl,
                        weight,
                        notes,
                        measurementDateTime);
        return measurementDao.insert(m);
    }

    /** Newest measurements first (dashboard table). */
    public List<Measurement> listForUserNewestFirst(long userId) throws SQLException {
        return measurementDao.findByUserIdNewestFirst(userId);
    }

    /** Oldest first for charts / trends. */
    public List<Measurement> listForUserChronological(long userId) throws SQLException {
        return measurementDao.findByUserIdChronological(userId);
    }

    /** Oldest first within an inclusive date-time range. */
    public List<Measurement> listForUserByDateRange(
            long userId, LocalDateTime startInclusive, LocalDateTime endInclusive) throws SQLException {
        if (startInclusive == null || endInclusive == null) {
            throw new IllegalArgumentException("Start and end date/time are required.");
        }
        if (endInclusive.isBefore(startInclusive)) {
            throw new IllegalArgumentException("End date/time must be on or after start date/time.");
        }
        return measurementDao.findByUserIdWithinDateRange(userId, startInclusive, endInclusive);
    }

    /**
     * Updates the measurement with the given ID, only if it belongs to {@code userId}.
     *
     * @return true if the row was updated
     */
    public boolean editMeasurement(Measurement measurement) throws SQLException {
        return measurementDao.updateByIdAndUserId(measurement);
    }

    /**
     * Deletes the measurement with the given ID, only if it belongs to {@code userId}.
     *
     * @return true if the row was deleted
     */
    public boolean deleteMeasurement(long measurementId, long userId) throws SQLException {
        return measurementDao.deleteByIdAndUserId(measurementId, userId);
    }
}
