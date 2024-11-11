package controllers;

import application.EmergencyCoordinationApp;
import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import models.Message;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesController {
    @FXML
    private ListView<String> messageList;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendMessageButton = new Button();
    @FXML
    Button listMessagesButton = new Button();

    private EmergencyCoordinationApp app;
    private Client client;

    public void setApp(EmergencyCoordinationApp app) {
        this.app = app;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText();
        // Implement logic to send message
        messageInput.clear();
    }

    @FXML
    private void listMessages() {
        try {
            handleListMessages();
        } catch (IOException e) {
            System.err.println("Error listing messages: " + e.getMessage());
            // Show an error alert to the user
            showAlert("Error", "Failed to retrieve messages", e.getMessage());
        }
    }


    private void handleListMessages() throws IOException {
        if (client == null) {
            throw new IllegalStateException("Client is not initialized");
        }

        List<Message> messages = client.getMessages();

        if (messages.isEmpty()) {
            // If there are no messages, update the UI to show this
            messageListView.getItems().clear();
            messageListView.getItems().add("No messages");
        } else {
            // Clear the existing items in the ListView
            messageListView.getItems().clear();

            // Add each message to the ListView
            for (Message message : messages) {
                String displayText = String.format("[%s] %s: %s",
                        message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        message.getSenderId(),
                        message.getContent());
                messageListView.getItems().add(displayText);
            }
        }
    }


    private void handleSendMessage() {

    }

    private void loadMessagesFromDB() {

    }

    public void initialize() {
        sendMessageButton.setOnAction(event -> handleSendMessage());
        listMessagesButton.setOnAction(event -> handleListMessages());
    }
}
