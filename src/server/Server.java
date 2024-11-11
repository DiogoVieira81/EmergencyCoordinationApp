package server;

import utils.AuthenticationManager;
import utils.HierarchyManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 100;

    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final AuthenticationManager authManager;
    private final HierarchyManager hierarchyManager;
    private final DatabaseManager dbManager;
    private final OperationLogger logger;
    private volatile boolean isRunning;

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.hierarchyManager = new HierarchyManager();
        this.dbManager = new DatabaseManager();
        this.dbManager.initializeDatabase();
        this.authManager = new AuthenticationManager(this.dbManager);
        this.logger = new OperationLogger();
        this.isRunning = false;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            logger.logSystemEvent("Server started on port " + PORT);
            System.out.println("Server is listening on port " + PORT);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, authManager, hierarchyManager, dbManager, logger);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    if (isRunning) {
                        logger.logSystemEvent("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.logSystemEvent("Server failed to start: " + e.getMessage());
        } finally {
            stop();
            shutdown();
        }
    }

    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.logSystemEvent("Error closing server socket: " + e.getMessage());
            }
            threadPool.shutdown();
            logger.logSystemEvent("Server stopped");
        }
    }

    private void shutdown() {
        threadPool.shutdownNow();
        dbManager.close();
        logger.shutdown();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
