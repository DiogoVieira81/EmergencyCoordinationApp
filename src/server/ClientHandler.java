package server;

import enums.UserRole;
import models.Message;
import models.Notification;
import models.Operation;
import models.User;
import utils.AuthenticationManager;
import utils.HierarchyManager;
import utils.ProtocolHandler;
import utils.ProtocolHandler.Request;
import utils.ProtocolHandler.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HierarchyManager hierarchyManager;
    private OperationLogger logger;
    private User currentUser;
    private boolean isRunning;

    public ClientHandler(Socket socket, HierarchyManager hierarchyManager, OperationLogger logger) {
        this.clientSocket = socket;
        this.hierarchyManager = hierarchyManager;
        this.logger = logger;
        this.isRunning = true;
    }

    public ClientHandler() {

    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (isRunning) {
                Request request = (Request) in.readObject();
                handleRequest(request);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    private void handleRequest(Request request) throws Exception {
        switch (request.getType()) {
            case LOGIN:
                handleLogin(request);
                break;
            case LOGOUT:
                handleLogout();
                break;
            case SEND_MESSAGE:
                handleSendMessage(request);
                break;
            case JOIN_CHANNEL:
                handleJoinChannel(request);
                break;
            case INITIATE_OPERATION:
                handleInitiateOperation(request);
                break;
            case APPROVE_OPERATION:
                handleApproveOperation(request);
                break;
            case GET_NOTIFICATIONS:
                handleGetNotifications();
                break;
            case GET_MESSAGES:
                handleGetMessages();
                break;
            case REGISTER_USER:
                handleRegisterUser(request);
                break;
            default:
                sendResponse(ProtocolHandler.createErrorResponse("Unknown request type"));
        }
    }

    private void handleLogin(Request request) {
        System.out.print("Username: ");
        Scanner scanner = null;
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = AuthenticationManager.authenticateUser(username, password);
        if (user != null) {
            currentUser = AuthenticationManager.getUserByUsername(username);
            System.out.println("Login successful!");
            logger.logAction(currentUser.getId(), "LOGIN", "User logged in");
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    private void handleGetMessages() throws IOException {
        User currentUser = getCurrentUser();
        List<Message> messages = DatabaseManager.getMessagesForUser(currentUser.getId());
        Response response = new Response(true, "Messages retrieved successfully", messages);
        out.writeObject(response);
        out.flush();
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    private void handleGetNotifications() throws IOException {
        if (currentUser == null) {
            ProtocolHandler.Response response = ProtocolHandler.createErrorResponse("User not authenticated");
            sendResponse(response);
        } else {
            try {
                List<Notification> notifications = DatabaseManager.getNotificationsForUser(currentUser.getId()); //error: Cannot resolve method 'getNotificationsForUser' in 'DatabaseManager'

                // Mark retrieved notifications as read
                for (Notification notification : notifications) {
                    notification.setRead(true);
                    DatabaseManager.updateNotification(notification); //error: Cannot resolve method 'updateNotification' in 'DatabaseManager'
                }

                ProtocolHandler.Response response = ProtocolHandler.createSuccessResponse("Notifications retrieved", notifications);
                sendResponse(response);

                logger.logAction(currentUser.getId(), "GET_NOTIFICATIONS", "Retrieved " + notifications.size() + " notifications");
            } catch (Exception e) {
                logger.logAction(currentUser.getId(), "GET_NOTIFICATIONS_ERROR", e.getMessage());
                ProtocolHandler.Response response = ProtocolHandler.createErrorResponse("Error retrieving notifications: " + e.getMessage());
                sendResponse(response);
            }
        }
    }


    private void handleLogout() throws IOException {
        if (currentUser != null) {
            AuthenticationManager.logoutUser(currentUser.getId());
            logger.logAction(currentUser.getId(), "LOGOUT", "User logged out");
            currentUser = null;
            sendResponse(ProtocolHandler.createSuccessResponse("Logout successful", null));
        } else {
            sendResponse(ProtocolHandler.createErrorResponse("Not logged in"));
        }
    }

    private void handleRegisterUser(Request request) throws Exception {
        //Checks client
        if (currentUser == null) throw new Exception("Current user is null.");
        //Checks request
        if(request == null) throw new Exception("Request to register user isn't valid (null)");
        //Checks request type
        if(!request.getType().equals(ProtocolHandler.RequestType.REGISTER_USER)) throw new Exception("Wrong request type: " + request.getType() + "\n Expected type: REGISTER_USER");

        User userToRegister = new User((String) request.getData("name"), (String) request.getData("password"), (UserRole) request.getData("role"));
        //if the user doesn't exist then registers it.
        if(!DatabaseManager.userExists(userToRegister.getUsername()))
            DatabaseManager.saveUser(Objects.requireNonNull(AuthenticationManager.registerUser(userToRegister.getName(), userToRegister.getUsername(), userToRegister.getPassword(), userToRegister.getRole())));
    }

    private void handleSendMessage(Request request) throws IOException {
        if (currentUser == null) {
            sendResponse(ProtocolHandler.createErrorResponse("Not authenticated"));
            return;
        }
        Message message = (Message) request.getData("message");
        DatabaseManager.saveMessage(message);
        logger.logAction(currentUser.getId(), "SEND_MESSAGE", "Message sent to " + message.getRecipientId());
        sendResponse(ProtocolHandler.createSuccessResponse("Message sent", null));
    }

    private void handleJoinChannel(Request request) throws IOException {
        if (currentUser == null) {
            sendResponse(ProtocolHandler.createErrorResponse("Not authenticated"));
            return;
        }

        String channelId = (String) request.getData("channelId");
        boolean joined = DatabaseManager.addUserToChannel(currentUser.getId(), channelId);

        if (joined) {
            logger.logAction(currentUser.getId(), "JOIN_CHANNEL", "Joined channel " + channelId);
            sendResponse(ProtocolHandler.createSuccessResponse("Joined channel successfully", null));

            // Notify other users in the channel
            notifyChannelMembers(channelId, currentUser.getName() + " has joined the channel.");
        } else {
            sendResponse(ProtocolHandler.createErrorResponse("Failed to join channel"));
        }
    }

    //TODO
    private void notifyChannelMembers(String channelId, String message) {
        // This method would send a notification to all members of the channel
        // Implementation depends on how you're managing connected clients and notifications
    }


    private void handleInitiateOperation(Request request) throws IOException {
        if (currentUser == null) {
            sendResponse(ProtocolHandler.createErrorResponse("Not authenticated"));
            return;
        }
        Operation operation = (Operation) request.getData("operation");
        boolean initiated = hierarchyManager.initiateOperation(operation, currentUser);
        if (initiated) {
            logger.logAction(currentUser.getId(), "INITIATE_OPERATION", "Initiated operation " + operation.getId());
            sendResponse(ProtocolHandler.createSuccessResponse("Operation initiated", null));
        } else {
            sendResponse(ProtocolHandler.createErrorResponse("Failed to initiate operation"));
        }
    }

    private void handleApproveOperation(Request request) throws IOException {
        if (currentUser == null) {
            sendResponse(ProtocolHandler.createErrorResponse("Not authenticated"));
            return;
        }
        String operationId = (String) request.getData("operationId");
        boolean approved = hierarchyManager.approveOperation(operationId, currentUser);
        if (approved) {
            logger.logAction(currentUser.getId(), "APPROVE_OPERATION", "Approved operation " + operationId);
            sendResponse(ProtocolHandler.createSuccessResponse("Operation approved", null));
        } else {
            sendResponse(ProtocolHandler.createErrorResponse("Failed to approve operation"));
        }
    }

    private void sendResponse(Response response) throws IOException {
        out.writeObject(response);
        out.flush();
    }

    private void closeConnection() {
        isRunning = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
