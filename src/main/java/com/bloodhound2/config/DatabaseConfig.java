package com.bloodhound2.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC configuration for MySQL. Connections are obtained per operation and closed by callers.
 */
public final class DatabaseConfig {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/bloodhound2";
    private static final String USERNAME = "bloodhound_user";
    private static final String PASSWORD = "change_me";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not on classpath", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}
