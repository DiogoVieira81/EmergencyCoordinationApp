package application;

import server.DatabaseManager;
import utils.ProtocolHandler;
import enums.UserRole;
import models.*;

import java.util.List;
import java.util.Scanner;

public class EmergencyCoordinationCLI {

    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isAuthenticated = false; // Controle de autenticação

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        System.out.println("\n=== Emergency Coordination System ===\n");

        boolean running = true;

        while (running) {
            // Exibe o menu conforme o estado de autenticação
            if (!isAuthenticated) {
                System.out.println("\nPlease select an option:");
                System.out.println("1. Login");
                System.out.println("2. Exit");
            } else {
                System.out.println("\nPlease select an option:");
                System.out.println("1. Register a User");
                System.out.println("2. View Users");
                System.out.println("3. Create a Channel");
                System.out.println("4. View Channels");
                System.out.println("5. Send a Message");
                System.out.println("6. View Messages");
                System.out.println("7. Logout");
                System.out.println("8. Exit");
            }
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (!isAuthenticated) {
                switch (choice) {
                    case 1 -> loginUser(); // Apenas Login
                    case 2 -> {
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } else {
                switch (choice) {
                    case 1 -> registerUser();
                    case 2 -> viewUsers();
                    case 3 -> createChannel();
                    case 4 -> viewChannels();
                    case 5 -> sendMessage();
                    case 6 -> viewMessages();
                    case 7 -> logoutUser();
                    case 8 -> {
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = DatabaseManager.getUser(username);
        if (user != null && user.getPassword().equals(DatabaseManager.hashPassword(password))) {
            isAuthenticated = true; // Autenticado com sucesso
            loggedInUser = user;  // Atribuindo o usuário logado
            System.out.println("Login successful. Welcome, " + user.getName() + "!");
            System.out.println("Your role is: " + user.getRole());  // Verificar o role do usuário

            // Verificar o role do usuário
            if (user.getRole() == UserRole.ADMIN) {  // Comparação direta com o enum
                System.out.println("You have administrative permissions.");
                // Permitir criação de canais e usuários
            } else {
                System.out.println("You do not have administrative permissions.");
                // Restringir a criação de canais e usuários
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }




    private static void logoutUser() {
        isAuthenticated = false; // Desautentica o usuário
        System.out.println("You have been logged out.");
    }

    private static void registerUser() {
        // Verificar se o usuário está logado e se é ADMIN
        if (loggedInUser == null || loggedInUser.getRole() != UserRole.ADMIN) {
            System.out.println("Access denied. Only ADMIN users can register new users.");
            return;
        }

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (DatabaseManager.userExists(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Enter role (LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, ADMIN): ");
        String roleInput = scanner.nextLine().toUpperCase();

        UserRole role;
        try {
            // Comparar diretamente com as constantes do enum
            role = UserRole.valueOf(roleInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role. Defaulting to LOW_LEVEL.");
            role = UserRole.LOW_LEVEL;  // Atribui LOW_LEVEL caso o input seja inválido
        }

        User user = new User(name, username, password, role);
        DatabaseManager.saveUser(user);
        System.out.println("User registered successfully.");
    }



    private static void viewUsers() {
        List<User> users = DatabaseManager.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("\n=== Registered Users ===");
            for (User user : users) {
                System.out.println("Name: " + user.getName() + ", Role: " + user.getRole());
            }
        }
    }

    private static User loggedInUser = null; // Armazena o usuário autenticado

    private static void createChannel() {
        if (loggedInUser == null ||
                !(loggedInUser.getRole() == UserRole.ADMIN || loggedInUser.getRole() == UserRole.HIGH_LEVEL)) {
            System.out.println("Access denied. Only ADMIN and HIGH_LEVEL users can create channels.");
            return;
        }

        System.out.print("Enter channel name: ");
        String name = scanner.nextLine();

        System.out.print("Enter channel description: ");
        String description = scanner.nextLine();

        System.out.print("Is this an emergency channel? (yes/no): ");
        String isEmergencyInput = scanner.nextLine().toLowerCase();
        boolean isEmergency = isEmergencyInput.equals("yes");

        Channel channel = new Channel(name, description, loggedInUser.getUsername(), isEmergency);
        DatabaseManager.saveChannel(channel);
        System.out.println("Channel created successfully.");
    }


    private static void viewChannels() {
        List<Channel> channels = DatabaseManager.getAllChannels();
        if (channels.isEmpty()) {
            System.out.println("No channels found.");
        } else {
            System.out.println("\n=== Channels ===");
            for (Channel channel : channels) {
                System.out.println("Name: " + channel.getName() + ", Description: " + channel.getDescription() + ", Emergency: " + channel.isEmergencyChannel());
            }
        }
    }

    private static void sendMessage() {
        System.out.print("Enter recipient ID: ");
        String recipientId = scanner.nextLine();

        System.out.print("Enter message content: ");
        String content = scanner.nextLine();

        System.out.println("Select message type:");
        System.out.println("1. DIRECT");
        System.out.println("2. CHANNEL");
        System.out.println("3. NOTIFICATION");
        System.out.println("4. ALERT");
        System.out.print("Enter your choice: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        Message.MessageType messageType;
        switch (typeChoice) {
            case 1 -> messageType = Message.MessageType.DIRECT;
            case 2 -> messageType = Message.MessageType.CHANNEL;
            case 3 -> messageType = Message.MessageType.NOTIFICATION;
            case 4 -> messageType = Message.MessageType.ALERT;
            default -> {
                System.out.println("Invalid choice. Defaulting to DIRECT.");
                messageType = Message.MessageType.DIRECT;
            }
        }

        Message message = new Message("admin", recipientId, null, content, messageType);
        DatabaseManager.saveMessage(message);
        System.out.println("Message sent successfully.");
    }


    private static void viewMessages() {
        System.out.print("Enter user ID to view messages: ");
        String userId = scanner.nextLine();

        List<Message> messages = DatabaseManager.getMessagesForUser(userId);
        if (messages.isEmpty()) {
            System.out.println("No messages found for this user.");
        } else {
            System.out.println("\n=== Messages ===");
            for (Message message : messages) {
                System.out.println("From: " + message.getSenderId() + ", Content: " + message.getContent());
            }
        }
    }
}
