package controllers;

import application.EmergencyCoordinationApp;
import client.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.EOFException;

public class DashboardController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button logoutButton = new Button();
    @FXML
    private Button exitButton = new Button();

    private EmergencyCoordinationApp app;
    private Client client;
    private Stage stage;


    public void setApp(EmergencyCoordinationApp app) {
        this.app = app;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void initialize() {
        logoutButton.setOnAction(event -> {
            try {
                handleLogout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        exitButton.setOnAction(event -> handleExit());
    }

    @FXML
    private void handleLogout() {
        try {
            if (client == null) {
                throw new Exception("Client is not initialized");
            }

            boolean logoutSuccess = client.getCurrentUser() == null;
            if (logoutSuccess) {
                System.out.println("Logout Successful");
                app.showLoginView();
            } else {
                System.out.println("Logout failed");
            }
        } catch (EOFException e) {
            System.err.println("Lost connection to the server during logout: " + e.getMessage());
            // Handle disconnection (e.g., force logout and return to login screen)
            app.showLoginView();
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            // Handle other exceptions (e.g., show an error message to the user)
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void showMessages() {
        try {
            if (client == null) {
                throw new Exception("Client is not initialized");
            }


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            MessagesController controller = loader.getController();
            controller.setApp(app);
            controller.setClient(client);
            stage.setScene(new Scene(root));
            stage.setTitle("Messages");
            stage.show();


        } catch (EOFException e) {
            System.err.println("Lost connection to the server during logout: " + e.getMessage());
            // Handle disconnection (e.g., force logout and return to login screen)
            app.showLoginView();
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            // Handle other exceptions (e.g., show an error message to the user)
        }

    }

    @FXML
    private void showChannels() {
        // Load and show ChannelsView in contentArea
    }

    @FXML
    private void showOperations() {
        // Load and show OperationsView in contentArea
    }

    @FXML
    private void showNotifications() {
        // Load and show NotificationsView in contentArea
    }
}
