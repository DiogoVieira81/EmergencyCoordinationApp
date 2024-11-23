package server;


import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import enums.UserRole;
import models.*;
import org.bson.Document;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String CONNECTION_STRING = "mongodb+srv://8200690:ydZaOosEtnKCXisa@emergencysituationapp.8g6p1.mongodb.net/?retryWrites=true&w=majority&appName=EmergencySituationApp";
    private static final String DATABASE_NAME = "EmergencySituationApp";
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> usersCollection;

    static {
        try {
            String connectionString = CONNECTION_STRING;
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("emergencyDB");
            usersCollection = database.getCollection("users");

            // Verify if the collection exists, if not, create it
            if (!collectionExists("users")) {
                database.createCollection("users");
                System.out.println("Users collection created.");
            }

            System.out.println("Connected to MongoDB Atlas successfully!");
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB Atlas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to check if a collection exists
    private static boolean collectionExists(String collectionName) {
        for (String name : database.listCollectionNames()) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }
    // checks if the user exists
    public static boolean userExists(String username) {
        Document user = usersCollection.find(new Document("username", username)).first();
        return user != null;
    }

    public static void initializeDatabase() {
        try {
            if (usersCollection == null) {
                System.err.println("Users collection is not initialized!");
                return;
            }

            // Check if admin user exists
            if (getUserByUsername("admin") == null) {
                User adminUser = new User(
                        "Admin",
                        hashPassword("adminpassword"),
                        UserRole.ADMIN
                );
                saveUser(adminUser);
                System.out.println("Admin user created successfully.");
            } else {
                System.out.println("Admin user already exists.");
            }
        } catch (Exception e) {
            System.err.println("Error in initializeDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static User getUserByUsername(String username) {
        if (usersCollection == null) {
            System.err.println("Users collection is not initialized!");
            return null;
        }
        Document doc = usersCollection.find(new Document("username", username)).first();
        if (doc != null) {
            return new User(
                    doc.getString("name"),
                    doc.getString("password"),
                    UserRole.valueOf(doc.getString("role"))
            );
        }
        return null;
    }

    public static List<User> getUsers() {
        if (usersCollection == null) {
            System.err.println("Users collection is not initilized!");
            return null;
        }
        List<User> users = new ArrayList<>();
        FindIterable<Document> documents = usersCollection.find();

        for (Document doc : documents) {
            User user = new User(
                    doc.getString("name"),
                    doc.getString("password"),
                    UserRole.valueOf(doc.getString("role"))
            );
            users.add(user);
        }

        return users;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }


    public static void saveUser(User user) {
        MongoCollection<Document> collection = database.getCollection("users");
        Document doc = new Document("_id", user.getId())
                .append("name", user.getName())
                .append("username", user.getUsername())
                .append("password", user.getPassword())
                .append("role", user.getRole().toString());
        collection.insertOne(doc);
    }

    public static User getUser(String username) {
        MongoCollection<Document> collection = database.getCollection("users");
        Document doc = collection.find(Filters.eq("username", username)).first();
        if (doc != null) {
            return new User(
                    doc.getString("name"),
                    doc.getString("password"),
                    UserRole.valueOf(doc.getString("role"))
            );
        }
        return null;
    }

    public static void saveMessage(Message message) {
        MongoCollection<Document> collection = database.getCollection("messages");
        Document doc = new Document("_id", message.getId())
                .append("senderId", message.getSenderId())
                .append("recipientId", message.getRecipientId())
                .append("channelId", message.getChannelId())
                .append("content", message.getContent())
                .append("timestamp", message.getTimestamp())
                .append("type", message.getType().toString());
        collection.insertOne(doc);
    }

    public static List<Message> getMessagesForUser(String userId) {
        List<Message> messages = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("messages");
        FindIterable<Document> docs = collection.find(
                Filters.or(
                        Filters.eq("recipientId", userId),
                        Filters.eq("channelId", new Document("$in", getUserChannels(userId)))
                )
        );
        for (Document doc : docs) {
            messages.add(new Message(
                    doc.getString("senderId"),
                    doc.getString("recipientId"),
                    doc.getString("channelId"),
                    doc.getString("content"),
                    Message.MessageType.valueOf(doc.getString("type"))
            ));
        }
        return messages;
    }

    private static List<String> getUserChannels(String userId) {
        List<String> channels = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("user_channels");
        FindIterable<Document> docs = collection.find(Filters.eq("userId", userId));
        for (Document doc : docs) {
            channels.add(doc.getString("channelId"));
        }
        return channels;
    }

    public static void saveChannel(Channel channel) {
        MongoCollection<Document> collection = database.getCollection("channels");
        Document doc = new Document("_id", channel.getId())
                .append("name", channel.getName())
                .append("description", channel.getDescription())
                .append("creatorId", channel.getCreatorId())
                .append("isEmergencyChannel", channel.isEmergencyChannel());
        collection.insertOne(doc);
    }

    public static void saveOperation(Operation operation) {
        MongoCollection<Document> collection = database.getCollection("operations");
        Document doc = new Document("_id", operation.getId())
                .append("name", operation.getName())
                .append("description", operation.getDescription())
                .append("type", operation.getType().toString())
                .append("initiatorId", operation.getInitiatorId())
                .append("status", operation.getStatus().toString())
                .append("requiredApprovalRole", operation.getRequiredApprovalRole().toString());
        collection.insertOne(doc);
    }

    public static void updateOperationStatus(String operationId, Operation.OperationStatus status) {
        MongoCollection<Document> collection = database.getCollection("operations");
        collection.updateOne(
                Filters.eq("_id", operationId),
                Updates.set("status", status.toString())
        );
    }

    public static List<Notification> getNotificationsForUser(String userId) {
        List<Notification> notifications = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("notifications");
        FindIterable<Document> docs = collection.find(Filters.eq("userId", userId));
        for (Document doc : docs) {
            notifications.add(new Notification(
                    doc.getString("title"),
                    doc.getString("content"),
                    Notification.NotificationType.valueOf(doc.getString("type")),
                    doc.getString("senderId"),
                    doc.getString("targetId"),
                    Notification.Priority.valueOf(doc.getString("priority"))
            ));
        }
        return notifications;
    }

    public static void updateNotification(Notification notification) {
        MongoCollection<Document> collection = database.getCollection("notifications");
        collection.updateOne(
                Filters.eq("_id", notification.getId()),
                Updates.set("isRead", notification.isRead())
        );
    }


    public static List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("channels");
        FindIterable<Document> docs = collection.find();
        for (Document doc : docs) {
            channels.add(new Channel(
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getString("creatorId"),
                    doc.getBoolean("isEmergencyChannel", false) // Default to false if field doesn't exist
            ));
        }
        return channels;
    }


    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public static boolean addUserToChannel(String userId, String channelId) {
        MongoCollection<Document> channelsCollection = database.getCollection("channels");
        MongoCollection<Document> userChannelsCollection = database.getCollection("user_channels");

        // Check if the channel exists
        Document channel = channelsCollection.find(Filters.eq("_id", channelId)).first();
        if (channel == null) {
            return false; // Channel doesn't exist
        }

        // Check if the user is already in the channel
        Document existingMembership = userChannelsCollection.find(
                Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("channelId", channelId)
                )
        ).first();

        if (existingMembership != null) {
            return true; // User is already in the channel
        }

        // Add the user to the channel
        Document membership = new Document("userId", userId)
                .append("channelId", channelId)
                .append("joinedAt", LocalDateTime.now());
        userChannelsCollection.insertOne(membership);

        return true;
    }


}
