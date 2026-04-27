package com.bloodhound2.service;

import com.bloodhound2.config.DatabaseConfig;
import com.bloodhound2.dao.MeasurementDao;
import com.bloodhound2.model.Measurement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementServiceTest {

    @Test
    void addMeasurement_addValidMeasurement_returnsGeneratedId() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        dao.nextInsertId = 99L;
        MeasurementService service = new MeasurementService(dao);

        long id =
                service.addMeasurement(
                        5L,
                        120,
                        78,
                        175.0,
                        55.0,
                        95.0,
                        180.0,
                        "Feeling good",
                        LocalDateTime.of(2026, 4, 20, 10, 0));

        assertEquals(99L, id);
        assertNotNull(dao.lastInserted);
        assertEquals(5L, dao.lastInserted.getUserId());
        assertEquals(120, dao.lastInserted.getSystolic());
    }

    @Test
    void addMeasurement_rejectInvalidMeasurement_missingDateTime() {
        MeasurementService service = new MeasurementService(new FakeMeasurementDao());

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.addMeasurement(5L, 120, 78, 175.0, 55.0, 95.0, 180.0, null, null));

        assertTrue(ex.getMessage().contains("date/time"));
    }

    @Test
    void listForUserNewestFirst_getMeasurementHistoryForUser() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        Measurement m1 = measurementWith(1L, 5L, LocalDateTime.of(2026, 4, 20, 10, 0));
        Measurement m2 = measurementWith(2L, 5L, LocalDateTime.of(2026, 4, 21, 10, 0));
        dao.newest = List.of(m2, m1);
        MeasurementService service = new MeasurementService(dao);

        List<Measurement> result = service.listForUserNewestFirst(5L);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getMeasurementId());
        assertEquals(1L, result.get(1).getMeasurementId());
    }

    @Test
    void filterByDateRange_returnsExpectedWindow() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        Measurement inRange = measurementWith(12L, 5L, LocalDateTime.of(2026, 4, 21, 9, 30));
        dao.range = List.of(inRange);
        MeasurementService service = new MeasurementService(dao);
        LocalDateTime start = LocalDateTime.of(2026, 4, 21, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 21, 23, 59);

        List<Measurement> result = service.listForUserByDateRange(5L, start, end);

        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).getMeasurementId());
        assertEquals(start, dao.lastRangeStart);
        assertEquals(end, dao.lastRangeEnd);
    }

    @Test
    void editMeasurement_updateMeasurement_returnsTrueWhenOwnedByUser() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        MeasurementService service = new MeasurementService(dao);
        Measurement edited = measurementWith(10L, 5L, LocalDateTime.of(2026, 4, 20, 10, 0));

        boolean updated = service.editMeasurement(edited);

        assertTrue(updated);
    }

    @Test
    void deleteMeasurement_deleteMeasurement_returnsTrueWhenOwnedByUser() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        MeasurementService service = new MeasurementService(dao);

        boolean deleted = service.deleteMeasurement(10L, 5L);

        assertTrue(deleted);
    }

    @Test
    void deleteMeasurement_blockAccessToAnotherUsersMeasurement_returnsFalse() throws Exception {
        FakeMeasurementDao dao = new FakeMeasurementDao();
        dao.ownerUserId = 5L;
        MeasurementService service = new MeasurementService(dao);

        boolean deleted = service.deleteMeasurement(10L, 999L);

        assertFalse(deleted);
    }

    private static Measurement measurementWith(long id, long userId, LocalDateTime dateTime) {
        return new Measurement(id, userId, 120, 78, 175.0, 55.0, 95.0, 180.0, "note", dateTime);
    }

    private static final class FakeMeasurementDao extends MeasurementDao {
        private long nextInsertId = 1L;
        private long ownerUserId = 5L;
        private Measurement lastInserted;
        private List<Measurement> newest = List.of();
        private List<Measurement> chronological = List.of();
        private List<Measurement> range = List.of();
        private LocalDateTime lastRangeStart;
        private LocalDateTime lastRangeEnd;

        private FakeMeasurementDao() {
            super(new DatabaseConfig());
        }

        @Override
        public long insert(Measurement measurement) {
            this.lastInserted = measurement;
            return nextInsertId;
        }

        @Override
        public List<Measurement> findByUserIdNewestFirst(long userId) {
            return newest;
        }

        @Override
        public List<Measurement> findByUserIdChronological(long userId) {
            return chronological;
        }

        @Override
        public List<Measurement> findByUserIdWithinDateRange(
                long userId, LocalDateTime startInclusive, LocalDateTime endInclusive) {
            this.lastRangeStart = startInclusive;
            this.lastRangeEnd = endInclusive;
            return range;
        }

        @Override
        public boolean updateByIdAndUserId(Measurement measurement) {
            return measurement.getUserId() == ownerUserId;
        }

        @Override
        public boolean deleteByIdAndUserId(long measurementId, long userId) {
            return userId == ownerUserId;
        }
    }
}
