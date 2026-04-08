package com.bloodhound2.model;

import java.util.Objects;

public final class User {

    private final Long userId;
    private final String username;
    private final String email;
    private final String passwordHash;

    public User(Long userId, String username, String email, String passwordHash) {
        this.userId = userId;
        this.username = Objects.requireNonNull(username, "username");
        this.email = Objects.requireNonNull(email, "email");
        this.passwordHash = passwordHash;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    /** Present only for authentication; not for display. */
    public String getPasswordHash() {
        return passwordHash;
    }

    public User withId(long id) {
        return new User(id, username, email, passwordHash);
    }
}
