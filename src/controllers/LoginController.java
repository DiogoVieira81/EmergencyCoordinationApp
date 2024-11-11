package controllers;

import application.EmergencyCoordinationApp;
import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label statusLabel;

    private EmergencyCoordinationApp app;
    private Client client;

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin()); //here
    }

    public void setApp(EmergencyCoordinationApp app) {
        this.app = app;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void handleLogin() {
        loginButton.setDisable(true);  // Disable to prevent multiple clicks
        String username = usernameField.getText();
        String password = passwordField.getText();
        loginButton.setDisable(false);

        try {
            if (client == null) {
                throw new Exception("Client is not initialized");
            }
            boolean loginSuccess = client.login(username, password); //here
            if (loginSuccess) {
                statusLabel.setText("Login successful");
                app.showDashboardView();
            } else {
                statusLabel.setText("Login failed. Please check your credentials.");
                System.out.println("Login failed for user: " + username);
            }
        } catch (IOException e) {
            statusLabel.setText("Network error during login. Please try again.");
            System.err.println("Network error during login: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("Error during login: " + e.getMessage());
            System.err.println("Login Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

