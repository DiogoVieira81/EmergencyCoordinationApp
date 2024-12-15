package application;

import client.Client;
import enums.OperationType;
import enums.UserRole;
import models.Channel;
import models.Message;
import models.Notification;
import models.User;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class EmergencyCoordinationCLI {

//    private static final Scanner scanner = new Scanner(System.in);
//    private static boolean isAuthenticated = false;
//    private static EmergencyCoordinationCLI hierarchyManager;
//    private final Client client;

    private Client client;
    private User currentUser;
    private boolean isAuthenticated = false;
    private boolean isRunning = true;
    private Scanner scanner = new Scanner(System.in);

    public EmergencyCoordinationCLI(Client client) {
        this.client = client;
    }

    public void start() {
        System.out.println("Welcome to the Emergency Coordination System");
        while (true) {
            try {
                if (currentUser == null) {
                    showLoginMenu();
                } else {
                    showMainMenu();
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                // You can choose to exit the application or handle the exception differently
                System.exit(1);
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("\n--- Login Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                try {
                    login();
                } catch (Exception e) {
                    System.err.println("Error during login: " + e.getMessage());
                }
                break;
            case 2:
                System.out.println("Exiting the system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void login() throws Exception {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            boolean success = client.login(username, password);
            if (success) {
                System.out.println("Login successful!");
                this.currentUser = client.getCurrentUser();
                showMainMenu(); // Show main menu after successful login
            } else {
                System.out.println("Login failed. Please check your credentials.");
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
        }
    }

//        while (true) {
//            try {
//                if (currentUser == null) {
//                    showLoginMenu();
//                    if (currentUser != null) { // Check if login was successful
//                        System.out.println("Login successful. Proceeding to main menu...");
//                    }
//                } else {
//                    showMainMenu();
//                }
//            } catch (Exception e) {
//                System.err.println("An error occurred: " + e.getMessage());
//                // You can choose to exit the application or handle the exception differently
//                System.exit(1);
//            }
//        }


    //    private void showLoginMenu() {
//        System.out.println("\n--- Login Menu ---");
//        System.out.println("1. Login");
//        System.out.println("2. Exit");
//        System.out.print("Enter your choice: ");
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume newline
//
//        switch (choice) {
//            case 1:
//                login();
//                break;
//            case 2:
//                System.out.println("Exiting the system. Goodbye!");
//                System.exit(0);
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
    private void showMainMenu() throws IOException {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View Messages");
        if (currentUser.getRole() != UserRole.LOW_LEVEL) {
            System.out.println("2. Send Message");
            System.out.println("3. View Notifications");
            System.out.println("4. List Channels");
            System.out.println("5. Join Channel");
        }
        if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN) {
            System.out.println("6. Create Channel");
            System.out.println("7. Initiate Operation");
            System.out.println("8. Register User");
        }
        System.out.println("0. Logout");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 0:
                logout();
                break;
            case 1:
                viewMessages();
                break;
            case 2:
                if (currentUser.getRole() != UserRole.LOW_LEVEL) sendMessage();
                else invalidChoice();
                break;
            case 3:
                if (currentUser.getRole() != UserRole.LOW_LEVEL) viewNotifications();
                else invalidChoice();
                break;
            case 4:
                if (currentUser.getRole() != UserRole.LOW_LEVEL) listChannels();
                else invalidChoice();
                break;
            case 5:
                if (currentUser.getRole() != UserRole.LOW_LEVEL) joinChannel();
                else invalidChoice();
                break;
            case 6:
                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN)
                    createChannel();
                else invalidChoice();
                break;
            case 7:
                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN)
                    initiateOperation();
                else invalidChoice();
                break;
            case 8:
                if (currentUser.getRole() == UserRole.ADMIN)
                    registerUser();
                else invalidChoice();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void invalidChoice() {
        System.out.println("You don't have permission to perform this action.");
    }

//    private void login() throws Exception {
//        System.out.print("Enter username: ");
//        String username = this.scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = this.scanner.nextLine();
//        System.out.println("Dadods inseridos");
//
//        boolean success = client.login(username, password);
//        System.out.println("Login sucesso? " + success);
//        if (success) {
//            System.out.println("Login successful!");
//            this.currentUser = client.getCurrentUser();
//        } else {
//            System.out.println("Login failed. Please check your credentials.");
//        }
//
//    }

    private void logout() {
        try {
            client.logout();
            currentUser = null;
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
        }
    }

    private void viewMessages() {
        try {
            List<Message> messages = client.getMessages();
            if (messages.isEmpty()) {
                System.out.println("No messages to display.");
            } else {
                System.out.println("--- Your Messages ---");
                for (Message message : messages) {
                    System.out.println(message);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving messages: " + e.getMessage());
        }
    }

    private void sendMessage() {
        System.out.print("Enter recipient username or channel ID: ");
        String recipient = this.scanner.nextLine();
        System.out.print("Enter message content: ");
        String content = this.scanner.nextLine();

        try {
            Message message = new Message(currentUser.getId(), recipient, null, content, Message.MessageType.DIRECT);
            client.sendMessage(message);
            System.out.println("Message sent successfully.");
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    private void viewNotifications() {
        try {
            List<Notification> notifications = client.getNotifications();
            if (notifications.isEmpty()) {
                System.out.println("No notifications to display.");
            } else {
                System.out.println("--- Your Notifications ---");
                for (Notification notification : notifications) {
                    System.out.println(notification);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving notifications: " + e.getMessage());
        }
    }

    private void listChannels() {
        try {
            List<Channel> channels = client.requestChannelList();
            if (channels.isEmpty()) {
                System.out.println("No channels available.");
            } else {
                System.out.println("--- Available Channels ---");
                for (Channel channel : channels) {
                    System.out.println(channel);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving channels: " + e.getMessage());
        }
    }

    private void joinChannel() {
        System.out.print("Enter channel ID to join: ");
        String channelId = this.scanner.nextLine();

        try {
            client.joinChannel(channelId);
            System.out.println("Successfully joined the channel.");
        } catch (Exception e) {
            System.out.println("Error joining channel: " + e.getMessage());
        }
    }

    private void createChannel() {
        System.out.print("Enter channel name: ");
        String channelName = this.scanner.nextLine();
        System.out.print("Enter channel description: ");
        String description = this.scanner.nextLine();
        System.out.print("Enter username to add: ");
        String usernameToAdd = this.scanner.nextLine();
        System.out.print("Is this an emergency channel? (true/false): ");
        boolean isEmergency = Boolean.parseBoolean(scanner.nextLine());

        try {
            User userToAdd = client.getUserByUsername(usernameToAdd);
            if (userToAdd == null) {
                System.out.println("User not found: " + usernameToAdd);
                return;
            }

            boolean success = client.createChannel(channelName, description, userToAdd, isEmergency);
            if (success) {
                System.out.println("Channel created successfully.");
            } else {
                System.out.println("Failed to create channel.");
            }
        } catch (Exception e) {
            System.out.println("Error creating channel: " + e.getMessage());
        }
    }

    private void initiateOperation() {
        System.out.print("Enter operation name: ");
        String name = this.scanner.nextLine();
        System.out.print("Enter operation description: ");
        String description = this.scanner.nextLine();
        System.out.println("Enter operation type (EVACUATION, RESCUE, SUPPLY_DISTRIBUTION): ");
        OperationType type = OperationType.valueOf(scanner.nextLine().toUpperCase());

        try {
            client.initiateOperation(name, description, type);
            System.out.println("Operation initiated successfully.");
        } catch (Exception e) {
            System.out.println("Error initiating operation: " + e.getMessage());
        }
    }


//    public static void main(String[] args) {
//        DatabaseManager.initializeDatabase();
//        System.out.println("\n=== Emergency Coordination System ===\n");
//
//        boolean running = true;
//

//    private void menu() throws Exception {
//        while (true) {
//            if (currentUser == null) {
//                System.out.println("\nPlease select an option:");
//                System.out.println("1. Login");
//                System.out.println("2. Exit");
//            } else {
//                System.out.println("\nPlease select an option:");
//                System.out.println("1. Register a User");
//                System.out.println("2. View Users");
//                System.out.println("3. Create a Channel");
//                System.out.println("4. View Channels");
//                System.out.println("5. Send a Message");
//                System.out.println("6. View Messages");
//                System.out.println("7. Initiate Operation");
//                System.out.println("8. Logout");
//                System.out.println("9. Exit");
//            }
//            System.out.print("Enter your choice: ");
//
//            int choice = this.scanner.nextInt();
//            this.scanner.nextLine();
//
//            if (currentUser == null) {
//                switch (choice) {
//                    case 1 -> login();
//                    case 2 -> {
//                        System.exit(0);
//                    }
//                    default -> System.out.println("Invalid choice. Please try again.");
//                }
//            } else {
//                switch (choice) {
//                    case 1 -> registerUser();
////                    case 2 -> viewUsers();
////                    case 3 -> createChannel();
////                    case 4 -> viewChannels();
////                    case 5 -> sendMessage();
////                    case 6 -> viewMessages();
////                    //case 7 -> break;
////                    case 8 -> logoutUser();
////                    case 9 -> {
////                        running = false;
////                        System.out.println("Exiting the system. Goodbye!");
////                    }
//                    default -> System.out.println("Invalid choice. Please try again.");
//                }
//            }
//        }
//    }

    private void registerUser() throws IOException {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return;
        }
        System.out.println("Insert user's name: ");
        String name = scanner.nextLine();
        System.out.println("Inser user's password: ");
        String password = scanner.nextLine();
        System.out.println("Inser user's role (HIGH_LEVEL, MID_LEVEL, LOW_LEVEL): ");
        String role = scanner.nextLine();

        client.registerUser(name, password, UserRole.toUserRole(role));
    }
//
//    private static void loginUser() {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        User user = DatabaseManager.getUser(username);
//        if (user != null && user.getPassword().equals(DatabaseManager.hashPassword(password))) {
//            isAuthenticated = true;
//            loggedInUser = user;
//            System.out.println("Login successful. Welcome, " + user.getName() + "!");
//            System.out.println("Your role is: " + user.getRole());
//
//
//            if (user.getRole() == UserRole.ADMIN) {
//                System.out.println("You have administrative permissions.");
//
//            } else {
//                System.out.println("You do not have administrative permissions.");
//
//            }
//        } else {
//            System.out.println("Invalid username or password.");
//        }
//    }
//
//
//
//
//    private static void logoutUser() {
//        isAuthenticated = false;
//        System.out.println("You have been logged out.");
//    }
//
//    private static void registerUser() {
//
//        if (loggedInUser == null || loggedInUser.getRole() != UserRole.ADMIN) {
//            System.out.println("Access denied. Only ADMIN users can register new users.");
//            return;
//        }
//
//        System.out.print("Enter name: ");
//        String name = scanner.nextLine();
//
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//
//        if (DatabaseManager.userExists(username)) {
//            System.out.println("Username already exists.");
//            return;
//        }
//
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        System.out.println("Enter role (LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, ADMIN): ");
//        String roleInput = scanner.nextLine().toUpperCase();
//
//        UserRole role;
//        try {
//
//            role = UserRole.valueOf(roleInput);
//        } catch (IllegalArgumentException e) {
//            System.out.println("Invalid role. Defaulting to LOW_LEVEL.");
//            role = UserRole.LOW_LEVEL;
//        }
//
//        User user = new User(name, username, password, role);
//        DatabaseManager.saveUser(user);
//        System.out.println("User registered successfully.");
//    }
//
//
//
//    private static void viewUsers() {
//        List<User> users = DatabaseManager.getUsers();
//        if (users.isEmpty()) {
//            System.out.println("No users found.");
//        } else {
//            System.out.println("\n=== Registered Users ===");
//            for (User user : users) {
//                System.out.println("Name: " + user.getName() + ", Role: " + user.getRole());
//            }
//        }
//    }
//
//    private static User loggedInUser = null;
//
//    private static void createChannel() {
//        if (loggedInUser == null ||
//                !(loggedInUser.getRole() == UserRole.ADMIN || loggedInUser.getRole() == UserRole.HIGH_LEVEL)) {
//            System.out.println("Access denied. Only ADMIN and HIGH_LEVEL users can create channels.");
//            return;
//        }
//
//        System.out.print("Enter channel name: ");
//        String name = scanner.nextLine();
//
//        System.out.print("Enter channel description: ");
//        String description = scanner.nextLine();
//
//        System.out.print("Is this an emergency channel? (yes/no): ");
//        String isEmergencyInput = scanner.nextLine().toLowerCase();
//        boolean isEmergency = isEmergencyInput.equals("yes");
//
//        Channel channel = new Channel(name, description, loggedInUser.getUsername(), isEmergency);
//        DatabaseManager.saveChannel(channel);
//        System.out.println("Channel created successfully.");
//    }
//
//
//    private static void viewChannels() {
//        List<Channel> channels = DatabaseManager.getAllChannels();
//        if (channels.isEmpty()) {
//            System.out.println("No channels found.");
//        } else {
//            System.out.println("\n=== Channels ===");
//            for (Channel channel : channels) {
//                System.out.println("Name: " + channel.getName() + ", Description: " + channel.getDescription() + ", Emergency: " + channel.isEmergencyChannel());
//            }
//        }
//    }
//
//    private static void sendMessage() {
//        System.out.print("Enter recipient ID: ");
//        String recipientId = scanner.nextLine();
//
//        System.out.print("Enter message content: ");
//        String content = scanner.nextLine();
//
//        System.out.println("Select message type:");
//        System.out.println("1. DIRECT");
//        System.out.println("2. CHANNEL");
//        System.out.println("3. NOTIFICATION");
//        System.out.println("4. ALERT");
//        System.out.print("Enter your choice: ");
//        int typeChoice = scanner.nextInt();
//        scanner.nextLine();
//
//        Message.MessageType messageType;
//        switch (typeChoice) {
//            case 1 -> messageType = Message.MessageType.DIRECT;
//            case 2 -> messageType = Message.MessageType.CHANNEL;
//            case 3 -> messageType = Message.MessageType.NOTIFICATION;
//            case 4 -> messageType = Message.MessageType.ALERT;
//            default -> {
//                System.out.println("Invalid choice. Defaulting to DIRECT.");
//                messageType = Message.MessageType.DIRECT;
//            }
//        }
//
//        Message message = new Message("admin", recipientId, null, content, messageType);
//        DatabaseManager.saveMessage(message);
//        System.out.println("Message sent successfully.");
//    }
//
//
//    private static void viewMessages() {
//        System.out.print("Enter user ID to view messages: ");
//        String userId = scanner.nextLine();
//
//        List<Message> messages = DatabaseManager.getMessagesForUser(userId);
//        if (messages.isEmpty()) {
//            System.out.println("No messages found for this user.");
//        } else {
//            System.out.println("\n=== Messages ===");
//            for (Message message : messages) {
//                System.out.println("From: " + message.getSenderId() + ", Content: " + message.getContent());
//            }
//        }
//    }
//
//    private void initiateOperation() throws IOException, ClassNotFoundException {
////        if (loggedInUser == null || loggedInUser.getRole() == UserRole.LOW_LEVEL || loggedInUser.getRole() == UserRole.MID_LEVEL) {
////            System.out.println("Access denied. Only HIGH_LEVEL or ADMIN users can initiate an operation.");
////            return;
////        }
////
//        System.out.print("Enter operation name: ");
//        String operationName = scanner.nextLine();
//
//        System.out.print("Enter operation description: ");
//        String operationDescription = scanner.nextLine();
//
//        System.out.println("Enter operation type (MASS_EVACUATION, EMERGENCY_COMMUNICATIONS_COORDINATION, EMERGENCY_RESOURCES_DISTRIBUTION): ");
//        String operationType = scanner.nextLine();
//
//        this.client.initiateOperation(operationName, operationDescription, OperationType.toOperationType(operationType));
//
//    }

}

