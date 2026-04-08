package com.bloodhound2.dao;

import com.bloodhound2.config.DatabaseConfig;
import com.bloodhound2.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {

    private final DatabaseConfig databaseConfig;

    public UserDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public long insert(User user) throws SQLException {
        final String sql =
                "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Insert user failed: no generated key");
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        final String sql =
                "SELECT user_id, username, email, password_hash FROM users WHERE username = ?";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) throws SQLException {
        final String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"));
    }
}
