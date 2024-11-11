package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChannelsController {
    @FXML private ListView<String> channelList;
    @FXML private TextField channelNameInput;

    private Button createChannelButton;
    private Client client;

    @FXML
    private void createChannel() {
        String channelName = channelNameInput.getText();
        // Implement logic to create new channel
        channelNameInput.clear();
    }

    public void initialize() {
        //createChannelButton.setOnAction(actionEvent -> handleCreateChannel());
    }

    private void handleCreateChannel() {
        createChannelButton.setDisable(true);
        if(client == null) {

        }
    }
}
