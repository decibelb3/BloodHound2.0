package com.bloodhound2.dao;

import com.bloodhound2.config.DatabaseConfig;
import com.bloodhound2.model.Measurement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDao {

    private final DatabaseConfig databaseConfig;

    public MeasurementDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public long insert(Measurement measurement) throws SQLException {
        final String sql =
                "INSERT INTO measurements (user_id, systolic, diastolic, total_cholesterol, hdl, ldl, "
                        + "weight, measurement_datetime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, measurement.getUserId());
            setNullableInt(ps, 2, measurement.getSystolic());
            setNullableInt(ps, 3, measurement.getDiastolic());
            setNullableDouble(ps, 4, measurement.getTotalCholesterol());
            setNullableDouble(ps, 5, measurement.getHdl());
            setNullableDouble(ps, 6, measurement.getLdl());
            setNullableDouble(ps, 7, measurement.getWeight());
            ps.setTimestamp(8, Timestamp.valueOf(measurement.getMeasurementDateTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Insert measurement failed: no generated key");
    }

    /**
     * All measurements for a user, oldest first (suitable for time-series / charting).
     */
    public List<Measurement> findByUserIdChronological(long userId) throws SQLException {
        final String sql =
                "SELECT measurement_id, user_id, systolic, diastolic, total_cholesterol, hdl, ldl, "
                        + "weight, measurement_datetime FROM measurements WHERE user_id = ? "
                        + "ORDER BY measurement_datetime ASC, measurement_id ASC";
        return queryList(userId, sql);
    }

    /**
     * All measurements for a user, newest first (dashboard table default).
     */
    public List<Measurement> findByUserIdNewestFirst(long userId) throws SQLException {
        final String sql =
                "SELECT measurement_id, user_id, systolic, diastolic, total_cholesterol, hdl, ldl, "
                        + "weight, measurement_datetime FROM measurements WHERE user_id = ? "
                        + "ORDER BY measurement_datetime DESC, measurement_id DESC";
        return queryList(userId, sql);
    }

    private List<Measurement> queryList(long userId, String sql) throws SQLException {
        List<Measurement> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private static Measurement mapRow(ResultSet rs) throws SQLException {
        long mid = rs.getLong("measurement_id");
        long uid = rs.getLong("user_id");
        Integer sys = rs.getObject("systolic") != null ? rs.getInt("systolic") : null;
        Integer dia = rs.getObject("diastolic") != null ? rs.getInt("diastolic") : null;
        Double tc = rs.getObject("total_cholesterol") != null ? rs.getDouble("total_cholesterol") : null;
        Double hdl = rs.getObject("hdl") != null ? rs.getDouble("hdl") : null;
        Double ldl = rs.getObject("ldl") != null ? rs.getDouble("ldl") : null;
        Double w = rs.getObject("weight") != null ? rs.getDouble("weight") : null;
        Timestamp ts = rs.getTimestamp("measurement_datetime");
        LocalDateTime dt = java.util.Objects.requireNonNull(ts, "measurement_datetime").toLocalDateTime();
        return new Measurement(mid, uid, sys, dia, tc, hdl, ldl, w, dt);
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setInt(index, value);
        }
    }

    private static void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setDouble(index, value);
        }
    }
}
