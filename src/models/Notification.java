package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
    private String id;
    private String title;
    private String content;
    private NotificationType type;
    private String senderId;
    private String targetId; // Can be userId, channelId, or null for system-wide
    private LocalDateTime timestamp;
    private boolean isRead;
    private Priority priority;

    public enum NotificationType {
        ALERT, INFORMATION, OPERATION_UPDATE, SYSTEM_MESSAGE
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public Notification(String title, String content, NotificationType type, String senderId, String targetId, Priority priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.type = type;
        this.senderId = senderId;
        this.targetId = targetId;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.priority = priority;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public NotificationType getType() {
        return type;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public Priority getPriority() {
        return priority;
    }

    // Setter for isRead
    public void setRead(boolean read) {
        isRead = read;
    }

    // Method to check if the notification is for a specific user
    public boolean isForUser(String userId) {
        return targetId == null || targetId.equals(userId);
    }

    // Method to check if the notification is for a specific channel
    public boolean isForChannel(String channelId) {
        return targetId != null && targetId.equals(channelId);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", senderId='" + senderId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                ", priority=" + priority +
                '}';
    }
}
