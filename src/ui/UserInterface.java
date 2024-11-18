package ui;

import client.Client;
import models.Channel;
import models.Message;
import models.Notification;
import models.Operation;
import models.User;
import enums.OperationType;
import enums.UserRole;

import java.util.List;
import java.util.Scanner;

public class UserInterface {
    private Scanner scanner;
    private Client client;
    private User currentUser;

    public UserInterface(Client client) {
        this.scanner = new Scanner(System.in);
        this.client = client;
    }

    public void start() {
        System.out.println("Welcome to the Emergency Coordination System");
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
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
                login();
                break;
            case 2:
                System.out.println("Exiting the system. Goodbye!");
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void showMainMenu() {
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
//            System.out.println("7. Delete Channel");
//            System.out.println("8. Update Channel");
            System.out.println("7. Initiate Operation");
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
                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN) createChannel();
                else invalidChoice();
                break;
//            case 7:
//                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN) deleteChannel();
//                else invalidChoice();
//                break;
//            case 8:
//                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN) updateChannel();
//                else invalidChoice();
//                break;
            case 7:
                if (currentUser.getRole() == UserRole.HIGH_LEVEL || currentUser.getRole() == UserRole.ADMIN) initiateOperation();
                else invalidChoice();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void invalidChoice() {
        System.out.println("You don't have permission to perform this action.");
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            boolean success = client.login(username, password);
            if (success) {
                System.out.println("Login successful!");
                currentUser = client.getCurrentUser();
            } else {
                System.out.println("Login failed. Please check your credentials.");
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

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
        String recipient = scanner.nextLine();
        System.out.print("Enter message content: ");
        String content = scanner.nextLine();

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
        String channelId = scanner.nextLine();

        try {
            client.joinChannel(channelId);
            System.out.println("Successfully joined the channel.");
        } catch (Exception e) {
            System.out.println("Error joining channel: " + e.getMessage());
        }
    }

    private void createChannel() {
        System.out.print("Enter channel name: ");
        String channelName = scanner.nextLine();
        System.out.print("Enter channel description: ");
        String description = scanner.nextLine();
        System.out.print("Enter username to add: ");
        String userToAdd = scanner.nextLine();
        System.out.print("Is this an emergency channel? (true/false): ");
        boolean isEmergency = Boolean.parseBoolean(scanner.nextLine());

        try {
            // First, retrieve the user object from the server
            //TODO ir buscar o user à base de dados o adicionar ao channel

            if (userToAdd == null) {
                System.out.println("User not found: " ); //usernameToAdd);
                return;
            }
            boolean success = client.createChannel(channelName, description, userToAddObj, isEmergency);
            if (success) {
                System.out.println("Channel created successfully.");
            } else {
                System.out.println("Failed to create channel.");
            }
        } catch (Exception e) {
            System.out.println("Error creating channel: " + e.getMessage());
        }
    }

//    private void deleteChannel() {
//        System.out.print("Enter channel ID to delete: ");
//        String channelId = scanner.nextLine();
//
//        try {
//            // Implement deleteChannel method in Client class
//            boolean success = client.deleteChannel(channelId);
//            if (success) {
//                System.out.println("Channel deleted successfully.");
//            } else {
//                System.out.println("Failed to delete channel.");
//            }
//        } catch (Exception e) {
//            System.out.println("Error deleting channel: " + e.getMessage());
//        }
//    }

//    private void updateChannel() {
//        System.out.print("Enter channel ID to update: ");
//        String channelId = scanner.nextLine();
//        System.out.print("Enter new channel name (or press enter to skip): ");
//        String newName = scanner.nextLine();
//        System.out.print("Enter new channel description (or press enter to skip): ");
//        String newDescription = scanner.nextLine();
//
//        try {
//            // Implement updateChannel method in Client class
//            boolean success = client.updateChannel(channelId, newName, newDescription);
//            if (success) {
//                System.out.println("Channel updated successfully.");
//            } else {
//                System.out.println("Failed to update channel.");
//            }
//        } catch (Exception e) {
//            System.out.println("Error updating channel: " + e.getMessage());
//        }
//    }

    private void initiateOperation() {
        System.out.print("Enter operation name: ");
        String name = scanner.nextLine();
        System.out.print("Enter operation description: ");
        String description = scanner.nextLine();
        System.out.println("Enter operation type (EVACUATION, RESCUE, SUPPLY_DISTRIBUTION): ");
        OperationType type = OperationType.valueOf(scanner.nextLine().toUpperCase());

        try {
            client.initiateOperation(name, description, type);
            System.out.println("Operation initiated successfully.");
        } catch (Exception e) {
            System.out.println("Error initiating operation: " + e.getMessage());
        }
    }
}
