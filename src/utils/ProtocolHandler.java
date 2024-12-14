package utils;

import enums.OperationType;
import models.Message;
import models.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ProtocolHandler {

    public enum RequestType {
        LOGIN, LOGOUT, SEND_MESSAGE, JOIN_CHANNEL, LEAVE_CHANNEL,
        CHANNEL_LIST, USER_LIST, INITIATE_OPERATION, APPROVE_OPERATION, GET_MESSAGES, GET_NOTIFICATIONS, CREATE_CHANNEL, GET_USER, REGISTER_USER
    }

    public static class Request implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private RequestType type;
        private Map<String, Object> data;

        public Request(RequestType type) {
            this.type = type;
            this.data = new HashMap<>();
        }

        public void addData(String key, Object value) {
            data.put(key, value);
        }

        public RequestType getType() {
            return type;
        }

        public Object getData(String key) {
            return data.get(key);
        }

    }

    public static Request createLoginRequest(String username, String password) {
        Request request = new Request(RequestType.LOGIN);
        request.addData("username", username);
        request.addData("password", password);
        return request;
    }

    public static Request createLogoutRequest() {
        return new Request(RequestType.LOGOUT);
    }

    public static Request createRegisterUserRequest(User user) {
        Request request = new Request(RequestType.REGISTER_USER);
        request.addData("name", user.getName());
        request.addData("password", user.getPassword());
        request.addData("role", user.getRole());

        return request;
    }

    public static Request createNotificationRequest() {
        return new Request(RequestType.GET_NOTIFICATIONS);
    }

    public static Request createSendMessageRequest(Message message) {
        Request request = new Request(RequestType.SEND_MESSAGE);
        request.addData("message", message);
        return request;
    }

    public static Request createGetMessagesRequest() {
        return new Request(RequestType.GET_MESSAGES);
    }

    public static Request createJoinChannelRequest(String channelId) {
        Request request = new Request(RequestType.JOIN_CHANNEL);
        request.addData("channelId", channelId);
        return request;
    }

    public static Request createCreateChannelRequest(String channelName, String description, User userToAdd, boolean isEmergency) {
        Request request = new Request(RequestType.CREATE_CHANNEL);
        request.addData("name", channelName);
        request.addData("description", description);
        request.addData("creatorId", userToAdd.getId());
        request.addData("isEmergency", isEmergency);
        return request;
    }

    public Request createLeaveChannelRequest(String channelId) {
        Request request = new Request(RequestType.LEAVE_CHANNEL);
        request.addData("channelId", channelId);
        return request;
    }

    public static Request createGetUserRequest(String username) {
        Request request = new Request(RequestType.GET_USER);
        request.addData("username", username);
        return request;
    }


    public static Request createChannelListRequest() {
        return new Request(RequestType.CHANNEL_LIST);
    }

    public Request createUserListRequest() {
        return new Request(RequestType.USER_LIST);
    }

    public static Request createInitiateOperationRequest(OperationType operationType, String description) {
        Request request = new Request(RequestType.INITIATE_OPERATION);
        request.addData("operationType", operationType);
        request.addData("description", description);
        return request;
    }

    public static Request createApproveOperationRequest(String operationId) {
        Request request = new Request(RequestType.APPROVE_OPERATION);
        request.addData("operationId", operationId);
        return request;
    }

    // Additional methods for handling server responses can be added here
    public static class Response implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private boolean success;
        private String message;
        private Object data;

        public Response(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }

    public static Response createSuccessResponse(String message, Object data) {
        return new Response(true, message, data);
    }

    public static Response createErrorResponse(String message) {
        return new Response(false, message, null);
    }
}
