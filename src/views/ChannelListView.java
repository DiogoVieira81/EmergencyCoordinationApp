package views;

import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import models.Channel;

public class ChannelListView extends VBox {
    private ListView<Channel> channelListView;
    private Button createChannelButton;

    public ChannelListView() {
        channelListView = new ListView<>();
        createChannelButton = new Button("Create New Channel");

        this.getChildren().addAll(channelListView, createChannelButton);
    }

    public ListView<Channel> getChannelListView() {
        return channelListView;
    }

    public Button getCreateChannelButton() {
        return createChannelButton;
    }
}
