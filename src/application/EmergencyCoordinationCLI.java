package application;

import enums.UserRole;
import models.Message;
import models.User;
import utils.ProtocolHandler;
import utils.ProtocolHandler.Request;
import utils.ProtocolHandler.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class EmergencyCoordinationCLI {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User currentUser;

    public EmergencyCoordinationCLI() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to the Server System ===");
        while (true) {
            if (currentUser == null) {
                printInitialMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir nova linha

                switch (choice) {
                    case 1 -> handleLogin(scanner);
                    case 0 -> exit();
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } else {
                printMainMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir nova linha

                switch (choice) {
                    case 2 -> handleRegister(scanner); // Apenas ADMIN
                    case 3 -> handleSendMessage(scanner);
                    case 4 -> handleCreateChannel(scanner);
                    case 5 -> handleGetNotifications();
                    case 6 -> handleLogout();
                    case 0 -> exit();
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }



    private void printInitialMenu() {
        System.out.println("\nInitial Menu:");
        System.out.println("1. Login");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private void printMainMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("2. Register User (Admin Only)");
        System.out.println("3. Send Message");
        System.out.println("4. Create Channel");
        System.out.println("5. Get Notifications");
        System.out.println("6. Logout");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Request loginRequest = ProtocolHandler.createLoginRequest(username, password);
        Response response = sendRequest(loginRequest);

        if (response.isSuccess()) {
            currentUser = (User) response.getData();
            System.out.println("Login successful! Welcome, " + currentUser.getName());
        } else {
            System.out.println("Login failed: " + response.getMessage());
        }
    }

    private void handleRegister(Scanner scanner) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("You do not have permission to register new users.");
            return;
        }

        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Role (LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, ADMIN): ");
        String roleInput = scanner.nextLine().toUpperCase();

        try {
            UserRole role = UserRole.valueOf(roleInput);

            Request registerRequest = ProtocolHandler.createRegisterUserRequest(
                    new User(name, username, password, role));
            Response response = sendRequest(registerRequest);

            if (response.isSuccess()) {
                System.out.println("User registered successfully!");
            } else {
                System.out.println("Registration failed: " + response.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role. Please try again.");
        }
    }

    private void handleSendMessage(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        System.out.print("Message content: ");
        String content = scanner.nextLine();

        Message message = new Message(currentUser.getId(), content);
        Request sendMessageRequest = ProtocolHandler.createSendMessageRequest(message);
        Response response = sendRequest(sendMessageRequest);

        if (response.isSuccess()) {
            System.out.println("Message sent!");
        } else {
            System.out.println("Failed to send message: " + response.getMessage());
        }
    }

    private void handleCreateChannel(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        System.out.print("Channel name: ");
        String name = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Is this an emergency channel? (true/false): ");
        boolean isEmergency = scanner.nextBoolean();

        Request createChannelRequest = ProtocolHandler.createCreateChannelRequest(
                name, description, currentUser, isEmergency);
        Response response = sendRequest(createChannelRequest);

        if (response.isSuccess()) {
            System.out.println("Channel created successfully!");
        } else {
            System.out.println("Failed to create channel: " + response.getMessage());
        }
    }

    private void handleGetNotifications() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        Request notificationRequest = ProtocolHandler.createNotificationRequest();
        Response response = sendRequest(notificationRequest);

        if (response.isSuccess()) {
            System.out.println("Notifications: " + response.getData());
        } else {
            System.out.println("Failed to fetch notifications: " + response.getMessage());
        }
    }

    private void handleLogout() {
        if (currentUser == null) {
            System.out.println("You are not logged in.");
            return;
        }

        Request logoutRequest = ProtocolHandler.createLogoutRequest();
        Response response = sendRequest(logoutRequest);

        if (response.isSuccess()) {
            currentUser = null;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("Logout failed: " + response.getMessage());
        }
    }

    private Response sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            System.err.println("Error communicating with server: " + e.getMessage());
            return ProtocolHandler.createErrorResponse("Communication error");
        }
    }

    private void exit() {
        System.out.println("Exiting...");
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        EmergencyCoordinationCLI ui = new EmergencyCoordinationCLI();
        ui.start();
    }
}
