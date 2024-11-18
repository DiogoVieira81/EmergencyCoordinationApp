import client.Client;
import ui.UserInterface;

public class Main {
    public static void main(String[] args) {
        Client client = new Client("localhost", 8080); // Initialize with server address and port
        UserInterface ui = new UserInterface(client);
        ui.start();
    }
}