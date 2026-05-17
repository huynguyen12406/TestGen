package vn.testgen.util;

/**
 * Input validation utility class.
 * Provides validation methods for all user inputs in the application.
 */
public class Validator {
    
    /**
     * Validate time limit value
     * @param value Time limit in milliseconds
     * @return true if valid, false otherwise
     */
    public static boolean isValidTimeLimit(int value) {
        return value >= Constants.MIN_TIME_LIMIT_MS && value <= Constants.MAX_TIME_LIMIT_MS;
    }
    
    /**
     * Validate memory limit value
     * @param value Memory limit in MB
     * @return true if valid, false otherwise
     */
    public static boolean isValidMemoryLimit(int value) {
        return value >= Constants.MIN_MEMORY_LIMIT_MB && value <= Constants.MAX_MEMORY_LIMIT_MB;
    }
    
    /**
     * Validate test count value
     * @param value Number of tests
     * @return true if valid, false otherwise
     */
    public static boolean isValidTestCount(int value) {
        return value >= Constants.MIN_TEST_COUNT && value <= Constants.MAX_TEST_COUNT;
    }
    
    /**
     * Validate problem title
     * @param title Problem title
     * @return true if valid, false otherwise
     */
    public static boolean isValidTitle(String title) {
        if (title == null) return false;
        String trimmed = title.trim();
        return !trimmed.isEmpty() && trimmed.length() <= Constants.MAX_TITLE_LENGTH;
    }
    
    /**
     * Parse integer from string with validation
     * @param text Input text
     * @param defaultValue Default value if parsing fails
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Parsed value or default if invalid
     */
    public static int parseIntSafe(String text, int defaultValue, int min, int max) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            int value = Integer.parseInt(text.trim());
            if (value < min || value > max) {
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Validate and parse time limit from text field
     * @param text Input text
     * @return Parsed time limit or default value
     */
    public static int parseTimeLimit(String text) {
        return parseIntSafe(text, Constants.DEFAULT_TIME_LIMIT_MS, 
                           Constants.MIN_TIME_LIMIT_MS, Constants.MAX_TIME_LIMIT_MS);
    }
    
    /**
     * Validate and parse memory limit from text field
     * @param text Input text
     * @return Parsed memory limit or default value
     */
    public static int parseMemoryLimit(String text) {
        return parseIntSafe(text, Constants.DEFAULT_MEMORY_LIMIT_MB,
                           Constants.MIN_MEMORY_LIMIT_MB, Constants.MAX_MEMORY_LIMIT_MB);
    }
    
    /**
     * Check if string is null or empty/whitespace
     * @param text Input text
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    /**
     * Validate file extension
     * @param filename File name
     * @param allowedExtensions Allowed extensions (e.g., "png", "jpg", "pdf")
     * @return true if extension is allowed
     */
    public static boolean hasValidExtension(String filename, String... allowedExtensions) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        for (String ext : allowedExtensions) {
            if (lowerFilename.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
