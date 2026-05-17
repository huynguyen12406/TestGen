package vn.testgen.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Centralized logging utility for TestGen application.
 * Provides file-based logging with rotation and multiple log levels.
 */
public class Logger {
    
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "testgen.log";
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static java.util.logging.Logger javaLogger;
    private static boolean initialized = false;
    
    /**
     * Initialize the logging system
     */
    public static synchronized void init() {
        if (initialized) return;
        
        try {
            // Create logs directory if not exists
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // Setup Java logger
            javaLogger = java.util.logging.Logger.getLogger("TestGen");
            javaLogger.setLevel(Level.ALL);
            javaLogger.setUseParentHandlers(false); // Don't use console handler
            
            // File handler with rotation
            String logPath = LOG_DIR + File.separator + LOG_FILE;
            FileHandler fileHandler = new FileHandler(logPath, MAX_LOG_SIZE, 5, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new CustomFormatter());
            javaLogger.addHandler(fileHandler);
            
            // Console handler for errors only
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            consoleHandler.setFormatter(new CustomFormatter());
            javaLogger.addHandler(consoleHandler);
            
            initialized = true;
            info("Logger", "Logging system initialized");
            
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Log debug message
     */
    public static void debug(String component, String message) {
        ensureInitialized();
        javaLogger.fine(formatMessage(component, message));
    }
    
    /**
     * Log info message
     */
    public static void info(String component, String message) {
        ensureInitialized();
        javaLogger.info(formatMessage(component, message));
    }
    
    /**
     * Log warning message
     */
    public static void warn(String component, String message) {
        ensureInitialized();
        javaLogger.warning(formatMessage(component, message));
    }
    
    /**
     * Log error message
     */
    public static void error(String component, String message) {
        ensureInitialized();
        javaLogger.severe(formatMessage(component, message));
    }
    
    /**
     * Log error with exception
     */
    public static void error(String component, String message, Throwable throwable) {
        ensureInitialized();
        javaLogger.log(Level.SEVERE, formatMessage(component, message), throwable);
    }
    
    /**
     * Log exception
     */
    public static void exception(String component, Throwable throwable) {
        error(component, "Exception occurred", throwable);
    }
    
    private static String formatMessage(String component, String message) {
        return String.format("[%s] %s", component, message);
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }
    
    /**
     * Custom log formatter
     */
    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            
            // Timestamp
            sb.append(LocalDateTime.now().format(TIMESTAMP_FORMAT));
            sb.append(" ");
            
            // Level
            sb.append(String.format("%-7s", record.getLevel().getName()));
            sb.append(" ");
            
            // Thread
            sb.append(String.format("[%-15s]", Thread.currentThread().getName()));
            sb.append(" ");
            
            // Message
            sb.append(formatMessage(record));
            sb.append("\n");
            
            // Exception if present
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                sb.append(sw.toString());
            }
            
            return sb.toString();
        }
    }
}
