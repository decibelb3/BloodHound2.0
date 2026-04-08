package com.bloodhound2.service;

import com.bloodhound2.model.User;

/**
 * Holds the authenticated user for the current application session.
 */
public final class SessionContext {

    private static User currentUser;

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
