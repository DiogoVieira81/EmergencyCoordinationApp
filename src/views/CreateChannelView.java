package views;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class CreateChannelView extends VBox {
    private TextField channelNameField;
    private TextField userToAddField;
    private Button createButton;

    public CreateChannelView() {
        channelNameField = new TextField();
        channelNameField.setPromptText("Enter channel name");

        userToAddField = new TextField();
        userToAddField.setPromptText("Enter username to add");

        createButton = new Button("Create Channel");

        this.getChildren().addAll(channelNameField, userToAddField, createButton);
    }

    public TextField getChannelNameField() {
        return channelNameField;
    }

    public TextField getUserToAddField() {
        return userToAddField;
    }

    public Button getCreateButton() {
        return createButton;
    }
}
