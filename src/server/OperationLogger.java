package server;

import models.Operation;
import models.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OperationLogger {
    private static final String LOG_FILE = "operations.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ConcurrentLinkedQueue<String> logQueue;
    private Thread loggerThread;
    private volatile boolean isRunning;

    public OperationLogger() {
        this.logQueue = new ConcurrentLinkedQueue<>();
        this.isRunning = true;
        startLoggingThread();
    }

    private void startLoggingThread() {
        loggerThread = new Thread(() -> {
            while (isRunning) {
                try {
                    String logEntry = logQueue.poll();
                    if (logEntry != null) {
                        writeToFile(logEntry);
                    } else {
                        Thread.sleep(1000); // Sleep if no logs to process
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        loggerThread.start();
    }

    public void logAction(String userId, String action, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] User %s: %s - %s", timestamp, userId, action, details);
        logQueue.offer(logEntry);
    }

    public void logOperation(Operation operation) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] Operation %s: %s - Status: %s",
                timestamp, operation.getId(), operation.getName(), operation.getStatus());
        logQueue.offer(logEntry);
    }

    public void logSystemEvent(String event) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] SYSTEM: %s", timestamp, event);
        logQueue.offer(logEntry);
    }

    private void writeToFile(String logEntry) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    //TODO generate report
    public void generateReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for generating periodic reports
        // This could involve reading the log file, filtering entries by date,
        // and creating a summary report
    }

    public void shutdown() {
        isRunning = false;
        loggerThread.interrupt();
        try {
            loggerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
