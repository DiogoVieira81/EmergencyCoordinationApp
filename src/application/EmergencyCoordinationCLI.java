package application;

import models.User;
import models.Message;
import server.DatabaseManager;
import server.OperationLogger;
import utils.AuthenticationManager;
import enums.UserRole;
import server.ClientHandler;

import java.util.List;
import java.util.Scanner;

public class EmergencyCoordinationCLI {
    private Scanner scanner;
    private boolean isRunning;
    private User currentUser;
    private OperationLogger logger;

    public EmergencyCoordinationCLI() {
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
        this.logger = new OperationLogger(); // Initialize the logger
    }

    public void start() {
        System.out.println("Welcome to the Emergency Coordination System");
        while (isRunning) {
            if (currentUser == null) {
                printLoginMenu();
            } else {
                printMainMenu();
            }
            String choice = scanner.nextLine();
            handleUserChoice(choice);
        }
    }

    private void printLoginMenu() {
        System.out.println("\n--- Login Menu ---");
        System.out.println("1. Login");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Send Message");
        System.out.println("2. Join Channel");
        System.out.println("3. Initiate Operation");
        System.out.println("4. Approve Operation");
        System.out.println("5. View Notifications");
        System.out.println("6. View Messages");
        if (currentUser.getRole() == UserRole.ADMIN) {
            System.out.println("7. Register New User");
        }
        System.out.println("8. Logout");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void handleUserChoice(String choice) {
        if (currentUser == null) {
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "0":
                    isRunning = false;
                    System.out.println("Exiting the program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } else {
            switch (choice) {
                case "1":
                    handleSendMessage();
                    break;
                case "2":
                    handleJoinChannel();
                    break;
                case "3":
                    handleInitiateOperation();
                    break;
                case "4":
                    handleApproveOperation();
                    break;
                case "5":
                    handleGetNotifications();
                    break;
                case "6":
                    handleGetMessages();
                    break;
                case "7":
                    if (currentUser.getRole() == UserRole.ADMIN) {
                        handleRegisterUser();
                    } else {
                        System.out.println("Invalid option. Please try again.");
                    }
                    break;
                case "8":
                    handleLogout();
                    break;
                case "0":
                    isRunning = false;
                    System.out.println("Exiting the program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleGetMessages() {
        if (currentUser == null) {
            System.out.println("Error: Not authenticated. Please log in first.");
            return;
        }

        try {
            List<Message> messages = DatabaseManager.getMessagesForUser(currentUser.getId());
            System.out.println("Messages retrieved successfully:");
            for (Message message : messages) {
                System.out.println("From: " + message.getSenderId());
                System.out.println("Content: " + message.getContent());
                System.out.println("Timestamp: " + message.getTimestamp());
                System.out.println("Type: " + message.getType());
                System.out.println("--------------------");
            }
            logger.logAction(currentUser.getId(), "GET_MESSAGES", "Retrieved " + messages.size() + " messages");
        } catch (Exception e) {
            System.out.println("Error retrieving messages: " + e.getMessage());
            logger.logAction(currentUser.getId(), "GET_MESSAGES_ERROR", e.getMessage());
        }
    }

    private void handleGetNotifications() {
        
    }

    private void handleApproveOperation() {
        
    }

    private void handleInitiateOperation() {
        
    }

    private void handleJoinChannel() {
        
    }

    private void handleSendMessage() {
        if (currentUser == null) {
            System.out.println("Error: Not authenticated. Please log in first.");
            return;
        }

        System.out.print("Enter recipient ID (or channel ID for channel messages): ");
        String recipientId = scanner.nextLine();

        System.out.print("Enter message content: ");
        String content = scanner.nextLine();

        System.out.print("Enter message type (DIRECT, CHANNEL, NOTIFICATION, ALERT): ");
        String typeInput = scanner.nextLine().toUpperCase();
        Message.MessageType type;
        try {
            type = Message.MessageType.valueOf(typeInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid message type. Defaulting to DIRECT.");
            type = Message.MessageType.DIRECT;
        }

        String channelId = type == Message.MessageType.CHANNEL ? recipientId : null;
        Message message = new Message(currentUser.getId(), recipientId, channelId, content, type);

        try {
            DatabaseManager.saveMessage(message);
            logger.logAction(currentUser.getId(), "SEND_MESSAGE", "Message sent to " + message.getRecipientId());
            System.out.println("Message sent successfully.");
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = AuthenticationManager.authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful!");
            logger.logAction(currentUser.getId(), "LOGIN", "User logged in");
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    private void handleRegisterUser() {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("Only ADMIN users can register new users.");
            return;
        }

        try {
            System.out.print("Enter new user's name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new user's username: ");
            String username = scanner.nextLine();
            System.out.print("Enter new user's password: ");
            String password = scanner.nextLine();
            System.out.print("Enter new user's role (LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, ADMIN): ");
            UserRole role = UserRole.valueOf(scanner.nextLine().toUpperCase());

            if (!DatabaseManager.userExists(username)) {
                User newUser = AuthenticationManager.registerUser(name, username, password, role);
                if (newUser != null) {
                    DatabaseManager.saveUser(newUser);
                    System.out.println("User registered successfully!");
                    logger.logAction(currentUser.getId(), "REGISTER_USER", "Registered new user: " + username);
                } else {
                    System.out.println("Failed to register user.");
                }
            } else {
                System.out.println("Username already exists. Please choose a different username.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role. Please enter a valid role.");
        } catch (Exception e) {
            System.out.println("Error registering user: " + e.getMessage());
        }
    }

    private void handleLogout() {
        logger.logAction(currentUser.getId(), "LOGOUT", "User logged out");
        currentUser = null;
        System.out.println("Logout successful!");
    }

    // Other methods (handleSendMessage, handleJoinChannel, etc.) remain the same

    public static void main(String[] args) {
        EmergencyCoordinationCLI cli = new EmergencyCoordinationCLI();
        cli.start();
    }
}