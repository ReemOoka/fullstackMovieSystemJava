package com.example.util;

import com.example.model.Role; 
import com.example.model.User;

public class SessionManager {

    private static User currentUser;
    private static String csrfToken;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void setCsrfToken(String token) {
        csrfToken = token;
    }

    public static String getCsrfToken() {
        return csrfToken;
    }

    public static void clearSession() {
        currentUser = null;
        csrfToken = null;
    }
    public static boolean hasRole(Role role) {
        if (currentUser == null || currentUser.getRoles() == null || role == null) {
            return false;
        }
        return currentUser.getRoles().contains(role);
    }
}