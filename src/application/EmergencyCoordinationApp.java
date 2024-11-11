package application;

import client.Client;
import controllers.DashboardController;
import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EmergencyCoordinationApp extends Application {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8080;
    private Stage primaryStage;
    private Client client;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            this.client = new Client(SERVER_ADDRESS, SERVER_PORT);
            showLoginView();
        } catch (Exception e) {
            System.err.println("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/LoginView.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setApp(this);
            controller.setClient(client);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Emergency Coordination System - Login");
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showDashboardView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setApp(this);
            controller.setClient(client);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Emergency Coordination System - Dashboard");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
