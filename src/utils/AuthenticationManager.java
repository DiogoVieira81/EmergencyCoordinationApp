package utils;

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
        users = new HashMap<>();
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

    public static boolean registerUser(User user) {
        if (!DatabaseManager.userExists(user.getUsername())) {
            String id = UUID.randomUUID().toString();
            users.put(user.getUsername(), user);
            DatabaseManager.saveUser(user);
            return true;
        }
        return false;
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
