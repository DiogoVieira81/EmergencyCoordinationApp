package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private String id;
    private String senderId;
    private String recipientId;
    private String channelId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private MessageType type;

    public enum MessageType {
        DIRECT, CHANNEL, NOTIFICATION, ALERT
    }

    public Message(String senderId, String recipientId, String channelId, String content, MessageType type) {
        this.id = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.channelId = channelId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.type = type;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public MessageType getType() {
        return type;
    }

    // Setter for isRead
    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                ", type=" + type +
                '}';
    }
}
