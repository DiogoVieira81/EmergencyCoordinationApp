package utils;

import enums.UserRole;
import models.User;
import server.DatabaseManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthenticationManager {
    private static Map<String, User> users;
    private static Map<String, String> sessions;

    public AuthenticationManager() {
        this.users = new HashMap<>();
        loadUsersFromDatabase();

    }

    private void loadUsersFromDatabase() {
        List<User> userList = DatabaseManager.getUsers();
        if (userList != null) {
            for (User user : userList) {
                users.put(user.getUsername(), user);
            }
            System.out.println("Loaded " + users.size() + " users from the database.");
        } else {
            System.err.println("Failed to load users from the database.");
        }
    }

    public static User registerUser(String name, String username, String password, UserRole role) {
        if (users.containsKey(username)) {
            return null; // User already exists
        }

        String id = UUID.randomUUID().toString();
        String hashedPassword = hashPassword(password);
        User newUser = new User(name, hashedPassword, role);
        users.put(username, newUser);
        return newUser;
    }

    public static User authenticateUser(String username, String password) {
        User user = DatabaseManager.getUserByUsername(username);
        String hashedPassword = hashPassword(password);
        if (user != null && user.authenticate(hashedPassword)) {
            return user;
        }
        return null;
    }

    public static void logoutUser(String sessionToken) {
        String userId = sessions.remove(sessionToken);
        if (userId != null) {
            users.values().stream()
                    .filter(user -> user.getId().equals(userId))
                    .findFirst()
                    .ifPresent(user -> user.setOnline(false));
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static User getUserByUsername(String username) {
        return DatabaseManager.getUserByUsername(username);
    }
}
