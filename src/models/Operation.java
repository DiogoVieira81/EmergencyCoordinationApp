package models;

import enums.OperationType;
import enums.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Operation {
    private String id;
    private String name;
    private String description;
    private OperationType type;
    private String initiatorId;
    private LocalDateTime initiationTime;
    private LocalDateTime completionTime;
    private OperationStatus status;
    private UserRole requiredApprovalRole;
    private List<String> approvals;
    private List<String> affectedAreas;
    private List<String> resources;

    public Operation(String operationName, String operationDescription, String username, boolean b) {
    }

    public enum OperationStatus {
        PENDING_APPROVAL, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Operation(String name, String description, OperationType type, String initiatorId, UserRole requiredApprovalRole) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.type = type;
        this.initiatorId = initiatorId;
        this.initiationTime = LocalDateTime.now();
        this.status = OperationStatus.PENDING_APPROVAL;
        this.requiredApprovalRole = requiredApprovalRole;
        this.approvals = new ArrayList<>();
        this.affectedAreas = new ArrayList<>();
        this.resources = new ArrayList<>();
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

    public OperationType getType() {
        return type;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public LocalDateTime getInitiationTime() {
        return initiationTime;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public UserRole getRequiredApprovalRole() {
        return requiredApprovalRole;
    }

    public List<String> getApprovals() {
        return new ArrayList<>(approvals);
    }

    public List<String> getAffectedAreas() {
        return new ArrayList<>(affectedAreas);
    }

    public List<String> getResources() {
        return new ArrayList<>(resources);
    }

    // Methods to manage operation
    public void addApproval(String userId) {
        if (!approvals.contains(userId)) {
            approvals.add(userId);
        }
    }

    public void addAffectedArea(String area) {
        if (!affectedAreas.contains(area)) {
            affectedAreas.add(area);
        }
    }

    public void addResource(String resource) {
        if (!resources.contains(resource)) {
            resources.add(resource);
        }
    }

    public void updateStatus(OperationStatus newStatus) {
        this.status = newStatus;
        if (newStatus == OperationStatus.COMPLETED) {
            this.completionTime = LocalDateTime.now();
        }
    }

    public boolean isApproved() {
        return status == OperationStatus.APPROVED || status == OperationStatus.IN_PROGRESS || status == OperationStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", initiatorId='" + initiatorId + '\'' +
                ", initiationTime=" + initiationTime +
                ", status=" + status +
                ", requiredApprovalRole=" + requiredApprovalRole +
                ", approvals=" + approvals.size() +
                ", affectedAreas=" + affectedAreas.size() +
                ", resources=" + resources.size() +
                '}';
    }
}

