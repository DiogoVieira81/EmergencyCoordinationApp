package models;

import enums.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Channel {
    private AtomicInteger counter = new AtomicInteger(1);
    private String id = new String("CHANNEL_");
    private String name;
    private String description;
    private LocalDateTime creationTime;
    private String creatorId;
    private List<String> memberIds;
    private boolean isEmergencyChannel;

    public Channel(String name, String description, String creatorId, boolean isEmergencyChannel) {
        this.id = "CHANNEL_" + counter.getAndIncrement();
        this.name = name;
        this.description = description;
        this.creationTime = LocalDateTime.now();
        this.creatorId = creatorId;
        this.memberIds = new ArrayList<>();
        this.memberIds.add(creatorId);
        this.isEmergencyChannel = isEmergencyChannel;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public List<String> getMemberIds() {
        return new ArrayList<>(memberIds);
    }

    public boolean isEmergencyChannel() {
        return isEmergencyChannel;
    }

    // Methods to manage channel members
    public void addMember(String userId) {
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }
    }

    public void removeMember(String userId) {
        memberIds.remove(userId);
    }

    public boolean hasMember(String userId) {
        return memberIds.contains(userId);
    }

    // Method to update channel description
    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    // Static factory method to create a channel
    public static Channel createChannel(String name, String description, User creator, boolean isEmergencyChannel) {
        if (creator.getRole() == UserRole.ADMIN || creator.getRole() == UserRole.HIGH_LEVEL) {
            String channel = ": " + name;
            return new Channel(channel, description, creator.getId(), isEmergencyChannel);
        } else {
            throw new IllegalArgumentException("Only ADMIN or HIGH_LEVEL users can create channels.");
        }
    }

    // Method to set emergency status
    public void setEmergencyStatus(boolean status) {
        this.isEmergencyChannel = status;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creationTime=" + creationTime +
                ", creatorId='" + creatorId + '\'' +
                ", memberCount=" + memberIds.size() +
                ", isEmergencyChannel=" + isEmergencyChannel +
                '}';
    }
}
