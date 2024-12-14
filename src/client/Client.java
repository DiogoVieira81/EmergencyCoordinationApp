package client;

import enums.OperationType;
import enums.UserRole;
import models.*;
import server.DatabaseManager;
import utils.ProtocolHandler;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User currentUser;
    private Queue<Message> messageQueue;
    private Queue<Notification> notificationQueue;
    private volatile boolean isRunning;

    public Client(String serverAddress, int serverPort) {
        try {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.messageQueue = new ConcurrentLinkedQueue<>();
            this.notificationQueue = new ConcurrentLinkedQueue<>();
            connect();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //shouldn't the socket used here be client socket?
    public void connect() throws IOException {
        try {
            socket = new Socket(serverAddress, serverPort);
            socket.setSoTimeout(10000); //times out in 10 seconds if it doesn't get a response
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            isRunning = true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            if (socket != null) {
                socket.close();
            }
            throw e;
        }
    }

    public void disconnect() throws IOException {
        isRunning = false;
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

//    public boolean login(String username, String password) throws IOException {
//        try {
//            // Create a login request object
//            ProtocolHandler.Request loginRequest = ProtocolHandler.createLoginRequest(username, password);
//            System.out.println("Antes de enviar o pedido");
//            // Send the login request to the server
//            out.writeObject(loginRequest);
//            out.flush(); // Ensure the request is sent immediately
//            System.out.println("Depois de enviar o pedido"); // o programa para aqui
//            System.out.println("a receber resposta" + in.readObject().toString());
//            ProtocolHandler.Response serverResponse = (ProtocolHandler.Response) in.readObject();
//            System.out.println(serverResponse.isSuccess());
//            // Check if login was successful
//            if (serverResponse.isSuccess()) {
//                this.setCurrentUser((User) serverResponse.getData());
//                System.out.println("Login successful: " + serverResponse.getMessage());
//                return true; // Login was successful
//            } else {
//                System.out.println("Login failed: " + serverResponse.getMessage());
//                return false; // Login failed
//            }
//        } catch (EOFException e) {
//            System.err.println("Connection closed by server.");
//            throw new IOException("Connection closed by server.", e);
//        } catch (ClassNotFoundException e) {
//            throw new IOException("Error reading server response", e);
//        }
//    }

    public boolean login(String username, String password) throws IOException, ClassNotFoundException {
        try {
            // Create a login request object
            ProtocolHandler.Request loginRequest = ProtocolHandler.createLoginRequest(username, password);
            System.out.println("Antes de enviar o pedido");
            // Send the login request to the server
            out.writeObject(loginRequest);
            out.flush(); // Ensure the request is sent immediately
            System.out.println("Depois de enviar o pedido");

            // Read the server's response
            ProtocolHandler.Response serverResponse = (ProtocolHandler.Response) in.readObject();
            System.out.println("Recebeu resposta do servidor: " + serverResponse.isSuccess());

            // Check if login was successful
            if (serverResponse.isSuccess()) {
                this.setCurrentUser((User) serverResponse.getData());
                System.out.println("Login successful: " + serverResponse.getMessage());
                return true; // Login was successful
            } else {
                System.out.println("Login failed: " + serverResponse.getMessage());
                return false; // Login failed
            }
        } catch (EOFException e) {
            System.err.println("Connection closed by server.");
            throw new IOException("Connection closed by server.", e);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Error reading server response", e);
        } catch (IOException e) {
            System.err.println("Error during login: " + e.getMessage());
            throw e;
        }
    }


    private void startListening() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Object received = in.readObject(); // here
                    if (received instanceof Message) {
                        messageQueue.offer((Message) received);
                    } else if (received instanceof Notification) {
                        notificationQueue.offer((Notification) received);
                    }
                } catch (EOFException e) {
                    System.err.println("Connection closed by server.");
                    break; // Exit loop if connection is closed
                } catch (IOException | ClassNotFoundException e) {
                    if (isRunning) {
                        e.printStackTrace();
                        try {
                            disconnect();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }).start();
    }


    public void sendMessage(Message message) throws IOException {
        try {
            ProtocolHandler.Request request = ProtocolHandler.createSendMessageRequest(message);
            out.writeObject(request);
            out.flush();

            ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();
            if (!response.isSuccess()) {
                throw new IOException("Failed to send message: " + response.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading server response", e);
        }
    }

    public void joinChannel(String channelId) throws IOException {
        try {
            ProtocolHandler.Request request = ProtocolHandler.createJoinChannelRequest(channelId);
            out.writeObject(request);
            out.flush();

            ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();
            if (!response.isSuccess()) {
                throw new IOException("Failed to join channel: " + response.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading server response", e);
        }
    }

    public List<Channel> requestChannelList() throws IOException {
        try {
            ProtocolHandler.Request request = ProtocolHandler.createChannelListRequest();
            out.writeObject(request);
            out.flush();

            ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();
            if (response.isSuccess()) {
                return (List<Channel>) response.getData();
            } else {
                throw new IOException("Failed to retrieve channel list: " + response.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading server response", e);
        }
    }

    public List<Message> getMessages() throws IOException {
        try {
            // Create a request to get messages
            ProtocolHandler.Request getMessagesRequest = ProtocolHandler.createGetMessagesRequest();

            // Send the request to the server
            out.writeObject(getMessagesRequest);
            out.flush();

            // Wait for the response from the server
            Object serverResponse = in.readObject();
            if (serverResponse instanceof ProtocolHandler.Response) {
                ProtocolHandler.Response response = (ProtocolHandler.Response) serverResponse;
                if (response.isSuccess()) {
                    return (List<Message>) response.getData();
                } else {
                    throw new IOException("Failed to get messages: " + response.getMessage());
                }
            } else {
                throw new IOException("Unexpected response type: " + serverResponse.getClass().getName());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading server response", e);
        }
    }


    public Message getNextMessage() {
        return messageQueue.poll();
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public Notification getNextNotification() {
        return notificationQueue.poll();
    }

    public void initiateOperation(String name, String description, OperationType type) throws IOException, ClassNotFoundException {
        if (currentUser == null) throw new IllegalStateException("User not logged in");
        Operation operation = new Operation(name, description, type, currentUser.getId(), currentUser.getRole());
        out.writeObject(ProtocolHandler.createInitiateOperationRequest(operation.getType(), operation.getDescription()));

        ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();
        DatabaseManager.saveOperation(operation);
        if (!response.isSuccess()) {
            throw new IOException("Failed to initiate operation: " + response.getMessage());
        } else {
            System.out.println("Operation initiated successfully.");
        }
    }

    public User getUserByUsername(String username) throws IOException, ClassNotFoundException {
        // Implementação para obter um usuário pelo nome de usuário
        ProtocolHandler.Request request = ProtocolHandler.createGetUserRequest(username);
        out.writeObject(request);
        out.flush();

        Object response = in.readObject();
        if (response instanceof ProtocolHandler.Response) {
            ProtocolHandler.Response userResponse = (ProtocolHandler.Response) response;
            if (userResponse.isSuccess()) {
                return (User) userResponse.getData();
            } else {
                return null;
            }
        } else {
            throw new IOException("Unexpected response type: " + response.getClass().getName());
        }
    }

    public List<Notification> getNotifications() throws IOException, ClassNotFoundException {
        out.writeObject(ProtocolHandler.createNotificationRequest());
        Object responseObj = in.readObject();
        if (responseObj instanceof ProtocolHandler.Response) {
            ProtocolHandler.Response response = (ProtocolHandler.Response) responseObj;
            if (response.isSuccess() && response.getData() instanceof List<?>) {
                return (List<Notification>) response.getData();
            } else {
                throw new IOException("Failed to retrieve notifications: " + response.getMessage());
            }
        } else {
            throw new IOException("Unexpected response type");
        }
    }

    public void logout() throws IOException, ClassNotFoundException {
        out.writeObject(ProtocolHandler.createLogoutRequest());
        ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();
        if (response.isSuccess()) {
            this.setCurrentUser(null);
        } else {
            throw new IOException("Logout failed: " + response.getMessage());
        }
    }

    public boolean createChannel(String channelName, String description, User userToAdd, boolean isEmergency) throws IOException {
        try {
            // Create a request to create a new channel
            ProtocolHandler.Request createChannelRequest = ProtocolHandler.createCreateChannelRequest(channelName, description, userToAdd, isEmergency);

            // Send the request to the server
            out.writeObject(createChannelRequest);
            out.flush();

            // Wait for the response from the server
            Object serverResponse = in.readObject();
            if (serverResponse instanceof ProtocolHandler.Response response) {
                return response.isSuccess();
            } else {
                throw new IOException("Unexpected response type: " + serverResponse.getClass().getName());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading server response", e);
        }
    }

    public void approveOperation(String operationId) throws IOException {
        try {
            ProtocolHandler.Request request = ProtocolHandler.createApproveOperationRequest(operationId);

            out.writeObject(request);
            out.flush();

            ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();

            if (response.isSuccess()) {
                System.out.println("Operação aprovada com sucesso!");
            } else {
                System.out.println("Falha ao aprovar a operação: " + response.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao ler a resposta do servidor", e);
        }
    }

    public void registerUser(String name, String password, UserRole userRole) throws IOException {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("Apenas administradores podem registar novos usuários.");
            return;
        }

        try {
            User userToRegister = new User(name, password, userRole);
            ProtocolHandler.Request request = ProtocolHandler.createRegisterUserRequest(userToRegister);

            out.writeObject(request);
            out.flush();

            ProtocolHandler.Response response = (ProtocolHandler.Response) in.readObject();

            if (response.isSuccess()) {
                User user = (User) response.getData();
                System.out.println("Nome: " + user.getName());
            } else {
                System.out.println("Falha no registo: " + response.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao ler a resposta do servidor", e);
        }
    }

}

