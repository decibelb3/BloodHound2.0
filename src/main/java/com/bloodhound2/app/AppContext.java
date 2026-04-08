package com.bloodhound2.app;

import com.bloodhound2.config.DatabaseConfig;
import com.bloodhound2.dao.MeasurementDao;
import com.bloodhound2.dao.UserDao;
import com.bloodhound2.service.AuthService;
import com.bloodhound2.service.HealthAlertService;
import com.bloodhound2.service.MeasurementChartDataService;
import com.bloodhound2.service.MeasurementService;

/**
 * Application-wide wiring for persistence and services.
 */
public final class AppContext {

    public final DatabaseConfig databaseConfig = new DatabaseConfig();
    public final UserDao userDao = new UserDao(databaseConfig);
    public final MeasurementDao measurementDao = new MeasurementDao(databaseConfig);
    public final AuthService authService = new AuthService(userDao);
    public final MeasurementService measurementService = new MeasurementService(measurementDao);
    public final MeasurementChartDataService measurementChartDataService = new MeasurementChartDataService();
    public final HealthAlertService healthAlertService = new HealthAlertService();
}
