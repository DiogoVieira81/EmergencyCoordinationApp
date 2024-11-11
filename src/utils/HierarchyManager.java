package utils;

import models.User;
import models.Operation;
import enums.UserRole;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class HierarchyManager {
    private Map<UserRole, List<User>> usersByRole;
    private Map<String, Operation> pendingOperations;

    public HierarchyManager() {
        usersByRole = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            usersByRole.put(role, new ArrayList<>());
        }
        pendingOperations = new HashMap<>();
    }

    public void addUser(User user) {
        usersByRole.get(user.getRole()).add(user);
    }

    public void removeUser(User user) {
        usersByRole.get(user.getRole()).remove(user);
    }

    public boolean initiateOperation(Operation operation, User initiator) {
        if (initiator.hasAuthority(operation.getRequiredApprovalRole())) {
            // If the initiator has the required authority, approve immediately
            operation.updateStatus(Operation.OperationStatus.APPROVED);
            return true;
        } else {
            // Otherwise, add to pending operations
            pendingOperations.put(operation.getId(), operation);
            return false;
        }
    }

    public boolean approveOperation(String operationId, User approver) {
        Operation operation = pendingOperations.get(operationId);
        if (operation == null) {
            return false; // Operation not found or already approved
        }

        if (approver.hasAuthority(operation.getRequiredApprovalRole())) {
            operation.addApproval(approver.getId());
            operation.updateStatus(Operation.OperationStatus.APPROVED);
            pendingOperations.remove(operationId);
            return true;
        }
        return false;
    }

    public List<Operation> getPendingOperationsForApproval(User user) {
        List<Operation> operationsForApproval = new ArrayList<>();
        for (Operation operation : pendingOperations.values()) {
            if (user.hasAuthority(operation.getRequiredApprovalRole())) {
                operationsForApproval.add(operation);
            }
        }
        return operationsForApproval;
    }

    public boolean canUserInitiateOperation(User user, UserRole requiredRole) {
        return user.hasAuthority(requiredRole);
    }

    public List<User> getUsersWithRole(UserRole role) {
        return new ArrayList<>(usersByRole.get(role));
    }

    public List<User> getUsersWithAuthorityForRole(UserRole requiredRole) {
        List<User> authorizedUsers = new ArrayList<>();
        for (UserRole role : UserRole.values()) {
            if (role.ordinal() >= requiredRole.ordinal()) {
                authorizedUsers.addAll(usersByRole.get(role));
            }
        }
        return authorizedUsers;
    }
}
