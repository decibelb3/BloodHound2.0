package com.bloodhound2.service;

import com.bloodhound2.dao.UserDao;
import com.bloodhound2.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> login(String username, String rawPassword) throws SQLException {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> found = userDao.findByUsername(username.trim());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User u = found.get();
        if (u.getPasswordHash() == null || !BCrypt.checkpw(rawPassword, u.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(new User(u.getUserId(), u.getUsername(), u.getEmail(), null));
    }

    public User register(String username, String email, String rawPassword) throws SQLException {
        validateRegistration(username, email, rawPassword);
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        User toInsert = new User(null, username.trim(), email.trim(), hash);
        try {
            long id = userDao.insert(toInsert);
            return new User(id, toInsert.getUsername(), toInsert.getEmail(), null);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("That username or email is already registered.");
            }
            throw e;
        }
    }

    private static void validateRegistration(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (username.length() > 64) {
            throw new IllegalArgumentException("Username must be at most 64 characters.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email must be at most 255 characters.");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }
}
