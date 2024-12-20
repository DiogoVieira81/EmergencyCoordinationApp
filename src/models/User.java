package models;

import enums.UserRole;
import org.bson.Document;
import server.DatabaseManager;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private AtomicInteger counter = new AtomicInteger(0);
    private String id;
    private String name;
    private String username;
    private String password;
    private UserRole role;
    private boolean isOnline;

    public User(String name, String password, UserRole role) {
        this.name = name;
        this.id = this.name + this.counter.incrementAndGet();
        this.username = this.id;
        this.password = this.hashPassword(password);
        this.role = role;
        this.isOnline = false;
    }

    // Novo construtor
    public User(String name, String username, String password, UserRole role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.id = name + counter.incrementAndGet(); // Gera um ID único
        this.isOnline = false;
    }


    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPassword() {
        return DatabaseManager.hashPassword(this.password);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    // Method to check if user has authority for specific operations
    public boolean hasAuthority(UserRole requiredRole) {
        return this.role.ordinal() >= requiredRole.ordinal();
    }

    // Method to update user's password
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // Method to authenticate user
    public boolean authenticate(String inputPassword) {
        return Objects.requireNonNull(DatabaseManager.getUserByUsername(this.getUsername())).password.equals(inputPassword);
    }

    public Document toDocument() {
        return new Document("_id", this.id)
                .append("name", this.name)
                .append("username", this.username)
                .append("password", this.password)
                .append("role", this.role.toString())
                .append("isOnline", this.isOnline);
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


    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isOnline=" + isOnline +
                '}';
    }

}
