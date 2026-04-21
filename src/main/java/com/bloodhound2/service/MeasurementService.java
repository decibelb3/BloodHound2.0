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

    /**
     * Deletes the measurement with the given ID, only if it belongs to {@code userId}.
     *
     * @return true if the row was deleted
     */
    public boolean deleteMeasurement(long measurementId, long userId) throws SQLException {
        return measurementDao.deleteByIdAndUserId(measurementId, userId);
    }
}
