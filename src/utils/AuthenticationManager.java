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
    private Map<String, User> users;
    private Map<String, String> sessions;
    private DatabaseManager dbManager;

    public AuthenticationManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.users = new HashMap<>();
        loadUsersFromDatabase();

    }

    private void loadUsersFromDatabase() {
        List<User> userList = dbManager.getUsers();
        if (userList != null) {
            for (User user : userList) {
                users.put(user.getUsername(), user);
            }
            System.out.println("Loaded " + users.size() + " users from the database.");
        } else {
            System.err.println("Failed to load users from the database.");
        }
    }

    public User registerUser(String name, String username, String password, UserRole role) {
        if (users.containsKey(username)) {
            return null; // User already exists
        }

        String id = UUID.randomUUID().toString();
        String hashedPassword = hashPassword(password);
        User newUser = new User(id, name, username, hashedPassword, role);
        users.put(username, newUser);
        return newUser;
    }

    public User authenticateUser(String username, String password) {
        User user = dbManager.getUserByUsername(username);
        String hashedPassword = hashPassword(password);
        if (user != null && user.authenticate(hashedPassword)) {
            return user;
        }
        return null;
    }

    public void logoutUser(String sessionToken) {
        String userId = sessions.remove(sessionToken);
        if (userId != null) {
            users.values().stream()
                    .filter(user -> user.getId().equals(userId))
                    .findFirst()
                    .ifPresent(user -> user.setOnline(false));
        }
    }

    private String hashPassword(String password) {
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

    public User getUserByUsername(String username) {
        return dbManager.getUserByUsername(username);
    }
}
