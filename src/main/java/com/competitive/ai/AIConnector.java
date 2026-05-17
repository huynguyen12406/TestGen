package com.competitive.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Handles low-level HTTP communication with AI APIs (Gemini and OpenAI).
 * 
 * Responsibilities:
 * - Establish connections to AI APIs with proper authentication
 * - Send HTTP requests with retry logic and exponential backoff
 * - Handle timeouts (30 seconds per request)
 * - Return raw response strings for parsing by ResponseParser
 * 
 * Design Decisions:
 * - Uses OkHttp for HTTP communication (consistent with existing OpenAIService)
 * - Implements exponential backoff: 1s, 2s, 4s delays between retries
 * - 30-second timeout per request (configurable via constant)
 * - Throws IOException with descriptive messages for all failure cases
 * - Supports both Gemini and OpenAI with provider-specific request formatting
 * 
 * Error Handling:
 * - Authentication errors (401): Fail immediately without retry
 * - Rate limit errors (429): Retry with longer backoff (5s, 10s, 20s)
 * - Network errors: Retry with standard backoff (1s, 2s, 4s)
 * - Timeout errors: Return descriptive error message
 * 
 * @author Nguyễn Thành Huy
 * @version 1.0
 */
public class AIConnector {
    
    // ---- Constants ----
    
    /** Maximum number of retry attempts for failed requests */
    private static final int MAX_RETRIES = 3;
    
    /** Timeout in seconds for each HTTP request */
    private static final int TIMEOUT_SECONDS = 30;
    
    /** Gemini API base URL */
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    
    /** OpenAI API endpoint */
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    /** Groq API endpoint */
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    /** Gemini model name for text */
    private static final String GEMINI_MODEL_TEXT = "gemini-2.5-flash";
    
    /** Gemini model name for vision (with image) */
    private static final String GEMINI_MODEL_VISION = "gemini-2.5-flash";
    
    /** OpenAI model name for text */
    private static final String OPENAI_MODEL_TEXT = "gpt-4o";
    
    /** OpenAI model name for vision (with image) */
    private static final String OPENAI_MODEL_VISION = "gpt-4o";
    
    /** Groq model name for text (best model) */
    private static final String GROQ_MODEL_TEXT = "llama-3.3-70b-versatile";
    
    // ---- Fields ----
    
    /** Current AI provider (Gemini or OpenAI) */
    private Provider provider;
    
    /** API key for authentication */
    private String apiKey;
    
    /** OkHttp client for making HTTP requests */
    private final OkHttpClient httpClient;
    
    /** Jackson ObjectMapper for JSON processing */
    private final ObjectMapper mapper;
    
    // ---- Enums ----
    
    /**
     * Supported AI providers
     */
    public enum Provider {
        /** Google Gemini API */
        GEMINI,
        /** OpenAI API (GPT-4) */
        OPENAI,
        /** Groq API (Llama 3.3 70B) */
        GROQ
    }
    
    // ---- Constructors ----
    
    /**
     * Creates a new AIConnector with specified provider and API key.
     * 
     * @param provider the AI provider to use (GEMINI or OPENAI)
     * @param apiKey the API key for authentication
     * @throws IllegalArgumentException if provider or apiKey is null
     */
    public AIConnector(Provider provider, String apiKey) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        // Allow empty API key initially - will be validated when making API calls
        
        this.provider = provider;
        this.apiKey = apiKey != null ? apiKey : "";
        this.mapper = new ObjectMapper();
        
        // Configure OkHttp client with timeouts
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }
    
    // ---- Configuration Methods ----
    
    /**
     * Sets the AI provider.
     * 
     * @param provider the new provider (GEMINI or OPENAI)
     * @throws IllegalArgumentException if provider is null
     */
    public void setProvider(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        this.provider = provider;
    }
    
    /**
     * Sets the API key.
     * 
     * @param apiKey the new API key
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public void setApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        this.apiKey = apiKey;
    }
    
    /**
     * Gets the current provider.
     * 
     * @return the current provider
     */
    public Provider getProvider() {
        return provider;
    }
    
    /**
     * Checks if a valid API key is configured.
     * 
     * @return true if API key is not null and not empty
     */
    public boolean hasValidApiKey() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    // ---- Public API Methods ----
    
    /**
     * Sends a text prompt to the AI and returns the response.
     * 
     * This method:
     * 1. Validates that API key is configured
     * 2. Delegates to provider-specific method (Gemini or OpenAI)
     * 3. Implements retry logic with exponential backoff
     * 4. Handles timeouts and errors
     * 
     * @param prompt the text prompt to send
     * @return the AI's response as a string
     * @throws IOException if the request fails after all retries, or if API key is not configured
     */
    public String sendRequest(String prompt) throws IOException {
        if (!hasValidApiKey()) {
            throw new IOException("API key is not configured. Please set API key before making requests.");
        }
        
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        
        return retryWithBackoff(() -> {
            if (provider == Provider.GEMINI) {
                return sendToGemini(prompt);
            } else if (provider == Provider.GROQ) {
                return sendToGroq(prompt);
            } else {
                return sendToOpenAI(prompt);
            }
        });
    }
    
    /**
     * Sends a prompt with an image to the AI and returns the response.
     * 
     * This method:
     * 1. Validates that API key is configured
     * 2. Validates that image file exists and is readable
     * 3. Delegates to provider-specific method (Gemini or OpenAI)
     * 4. Implements retry logic with exponential backoff
     * 5. Handles timeouts and errors
     * 
     * @param prompt the text prompt to send
     * @param imageFile the image file to include
     * @return the AI's response as a string
     * @throws IOException if the request fails after all retries, if API key is not configured, or if image file is invalid
     */
    public String sendRequestWithImage(String prompt, File imageFile) throws IOException {
        if (!hasValidApiKey()) {
            throw new IOException("API key is not configured. Please set API key before making requests.");
        }
        
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("Image file does not exist: " + imageFile);
        }
        
        if (!imageFile.canRead()) {
            throw new IOException("Cannot read image file: " + imageFile);
        }
        
        return retryWithBackoff(() -> {
            if (provider == Provider.GEMINI) {
                return sendToGeminiWithImage(prompt, imageFile);
            } else if (provider == Provider.GROQ) {
                // Groq doesn't support vision, throw error
                throw new IOException("Groq API does not support image input. Please use Gemini or OpenAI for image analysis.");
            } else {
                return sendToOpenAIWithImage(prompt, imageFile);
            }
        });
    }
    
    // ---- Gemini API Methods ----
    
    /**
     * Sends a text prompt to Gemini API.
     * 
     * Gemini API format:
     * POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=API_KEY
     * Body: {
     *   "contents": [{
     *     "parts": [{"text": "prompt"}]
     *   }]
     * }
     * 
     * @param prompt the text prompt
     * @return the AI's response
     * @throws IOException if the request fails
     */
    private String sendToGemini(String prompt) throws IOException {
        // Build request URL with model name and API key
        String url = GEMINI_API_BASE + GEMINI_MODEL_TEXT + ":generateContent?key=" + apiKey;
        
        // Build request body
        ObjectNode body = mapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", prompt);
        
        String jsonBody = mapper.writeValueAsString(body);
        
        // Create HTTP request
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // Execute request and parse response
        try (Response response = httpClient.newCall(request).execute()) {
            return handleGeminiResponse(response);
        }
    }
    
    /**
     * Sends a prompt with an image to Gemini API.
     * 
     * Gemini API format for vision:
     * POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=API_KEY
     * Body: {
     *   "contents": [{
     *     "parts": [
     *       {"text": "prompt"},
     *       {"inline_data": {"mime_type": "image/jpeg", "data": "base64_encoded_image"}}
     *     ]
     *   }]
     * }
     * 
     * @param prompt the text prompt
     * @param imageFile the image file
     * @return the AI's response
     * @throws IOException if the request fails
     */
    private String sendToGeminiWithImage(String prompt, File imageFile) throws IOException {
        // Build request URL with model name and API key
        String url = GEMINI_API_BASE + GEMINI_MODEL_VISION + ":generateContent?key=" + apiKey;
        
        // Read and encode image
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = detectMimeType(imageFile.getName());
        
        // Build request body
        ObjectNode body = mapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        
        // Add text part
        ObjectNode textPart = parts.addObject();
        textPart.put("text", prompt);
        
        // Add image part
        ObjectNode imagePart = parts.addObject();
        ObjectNode inlineData = imagePart.putObject("inline_data");
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);
        
        String jsonBody = mapper.writeValueAsString(body);
        
        // Create HTTP request
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // Execute request and parse response
        try (Response response = httpClient.newCall(request).execute()) {
            return handleGeminiResponse(response);
        }
    }
    
    /**
     * Handles Gemini API response and extracts the generated text.
     * 
     * Gemini response format:
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{"text": "generated text"}]
     *     }
     *   }]
     * }
     * 
     * @param response the HTTP response
     * @return the generated text
     * @throws IOException if response is unsuccessful or cannot be parsed
     */
    private String handleGeminiResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        
        if (!response.isSuccessful()) {
            int code = response.code();
            
            // Handle specific error codes
            if (code == 401 || code == 403) {
                throw new IOException("Gemini API authentication failed (HTTP " + code + "): Invalid API key. Please check your API key in Settings.");
            } else if (code == 429) {
                throw new IOException("Gemini API rate limit exceeded (HTTP 429): Too many requests. Please try again later.");
            } else if (code >= 500) {
                throw new IOException("Gemini API server error (HTTP " + code + "): " + responseBody);
            } else {
                throw new IOException("Gemini API error (HTTP " + code + "): " + responseBody);
            }
        }
        
        // Parse response JSON
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                throw new IOException("Gemini API returned empty response: " + responseBody);
            }
            
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.path("content");
            JsonNode parts = content.path("parts");
            
            if (parts.isMissingNode() || parts.isEmpty()) {
                throw new IOException("Gemini API response missing 'parts' field: " + responseBody);
            }
            
            JsonNode firstPart = parts.get(0);
            JsonNode text = firstPart.path("text");
            
            if (text.isMissingNode()) {
                throw new IOException("Gemini API response missing 'text' field: " + responseBody);
            }
            
            return text.asText();
            
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini API response: " + e.getMessage() + "\nResponse: " + responseBody, e);
        }
    }
    
    // ---- OpenAI API Methods ----
    
    /**
     * Sends a text prompt to Groq API.
     * 
     * Groq API format (compatible with OpenAI):
     * POST https://api.groq.com/openai/v1/chat/completions
     * Headers: Authorization: Bearer API_KEY
     * Body: {
     *   "model": "llama-3.3-70b-versatile",
     *   "messages": [{"role": "user", "content": "prompt"}],
     *   "temperature": 0.2,
     *   "max_tokens": 4096
     * }
     * 
     * @param prompt the text prompt
     * @return the AI's response
     * @throws IOException if the request fails
     */
    private String sendToGroq(String prompt) throws IOException {
        // Build request body (same format as OpenAI)
        ObjectNode body = mapper.createObjectNode();
        body.put("model", GROQ_MODEL_TEXT);
        body.put("temperature", 0.2);
        body.put("max_tokens", 8192);
        
        ArrayNode messages = body.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        String jsonBody = mapper.writeValueAsString(body);
        
        // Create HTTP request
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // Execute request and parse response (same format as OpenAI)
        try (Response response = httpClient.newCall(request).execute()) {
            return handleGroqResponse(response);
        }
    }
    
    /**
     * Handles Groq API response and extracts the generated text.
     * 
     * Groq response format (same as OpenAI):
     * {
     *   "choices": [{
     *     "message": {
     *       "content": "generated text"
     *     }
     *   }]
     * }
     * 
     * @param response the HTTP response
     * @return the generated text
     * @throws IOException if response is unsuccessful or cannot be parsed
     */
    private String handleGroqResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        
        if (!response.isSuccessful()) {
            int code = response.code();
            
            // Handle specific error codes
            if (code == 401) {
                throw new IOException("Groq API authentication failed (HTTP 401): Invalid API key. Please check your API key in Settings.");
            } else if (code == 429) {
                throw new IOException("Groq API rate limit exceeded (HTTP 429): Too many requests. Please try again later.");
            } else if (code >= 500) {
                throw new IOException("Groq API server error (HTTP " + code + "): " + responseBody);
            } else {
                throw new IOException("Groq API error (HTTP " + code + "): " + responseBody);
            }
        }
        
        // Parse response JSON (same format as OpenAI)
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new IOException("Groq API returned empty response: " + responseBody);
            }
            
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            JsonNode content = message.path("content");
            
            if (content.isMissingNode()) {
                throw new IOException("Groq API response missing 'content' field: " + responseBody);
            }
            
            return content.asText();
            
        } catch (Exception e) {
            throw new IOException("Failed to parse Groq API response: " + e.getMessage() + "\nResponse: " + responseBody, e);
        }
    }
    
    // ---- OpenAI API Methods ----
    
    /**
     * Sends a text prompt to OpenAI API.
     * 
     * OpenAI API format:
     * POST https://api.openai.com/v1/chat/completions
     * Headers: Authorization: Bearer API_KEY
     * Body: {
     *   "model": "gpt-4o",
     *   "messages": [{"role": "user", "content": "prompt"}],
     *   "temperature": 0.2,
     *   "max_tokens": 4096
     * }
     * 
     * @param prompt the text prompt
     * @return the AI's response
     * @throws IOException if the request fails
     */
    private String sendToOpenAI(String prompt) throws IOException {
        // Build request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", OPENAI_MODEL_TEXT);
        body.put("temperature", 0.2);
        body.put("max_tokens", 8192);
        
        ArrayNode messages = body.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        String jsonBody = mapper.writeValueAsString(body);
        
        // Create HTTP request
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // Execute request and parse response
        try (Response response = httpClient.newCall(request).execute()) {
            return handleOpenAIResponse(response);
        }
    }
    
    /**
     * Sends a prompt with an image to OpenAI API.
     * 
     * OpenAI API format for vision:
     * POST https://api.openai.com/v1/chat/completions
     * Headers: Authorization: Bearer API_KEY
     * Body: {
     *   "model": "gpt-4o",
     *   "messages": [{
     *     "role": "user",
     *     "content": [
     *       {"type": "text", "text": "prompt"},
     *       {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,..."}}
     *     ]
     *   }],
     *   "temperature": 0.2,
     *   "max_tokens": 4096
     * }
     * 
     * @param prompt the text prompt
     * @param imageFile the image file
     * @return the AI's response
     * @throws IOException if the request fails
     */
    private String sendToOpenAIWithImage(String prompt, File imageFile) throws IOException {
        // Read and encode image
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = detectMimeType(imageFile.getName());
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;
        
        // Build request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", OPENAI_MODEL_VISION);
        body.put("temperature", 0.2);
        body.put("max_tokens", 8192);
        
        ArrayNode messages = body.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        
        ArrayNode content = message.putArray("content");
        
        // Add text part
        ObjectNode textPart = content.addObject();
        textPart.put("type", "text");
        textPart.put("text", prompt);
        
        // Add image part
        ObjectNode imagePart = content.addObject();
        imagePart.put("type", "image_url");
        ObjectNode imageUrl = imagePart.putObject("image_url");
        imageUrl.put("url", dataUrl);
        imageUrl.put("detail", "high");
        
        String jsonBody = mapper.writeValueAsString(body);
        
        // Create HTTP request
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // Execute request and parse response
        try (Response response = httpClient.newCall(request).execute()) {
            return handleOpenAIResponse(response);
        }
    }
    
    /**
     * Handles OpenAI API response and extracts the generated text.
     * 
     * OpenAI response format:
     * {
     *   "choices": [{
     *     "message": {
     *       "content": "generated text"
     *     }
     *   }]
     * }
     * 
     * @param response the HTTP response
     * @return the generated text
     * @throws IOException if response is unsuccessful or cannot be parsed
     */
    private String handleOpenAIResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        
        if (!response.isSuccessful()) {
            int code = response.code();
            
            // Handle specific error codes
            if (code == 401) {
                throw new IOException("OpenAI API authentication failed (HTTP 401): Invalid API key. Please check your API key in Settings.");
            } else if (code == 429) {
                throw new IOException("OpenAI API rate limit exceeded (HTTP 429): Too many requests. Please try again later.");
            } else if (code >= 500) {
                throw new IOException("OpenAI API server error (HTTP " + code + "): " + responseBody);
            } else {
                throw new IOException("OpenAI API error (HTTP " + code + "): " + responseBody);
            }
        }
        
        // Parse response JSON
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new IOException("OpenAI API returned empty response: " + responseBody);
            }
            
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            JsonNode content = message.path("content");
            
            if (content.isMissingNode()) {
                throw new IOException("OpenAI API response missing 'content' field: " + responseBody);
            }
            
            return content.asText();
            
        } catch (Exception e) {
            throw new IOException("Failed to parse OpenAI API response: " + e.getMessage() + "\nResponse: " + responseBody, e);
        }
    }
    
    // ---- Retry Logic ----
    
    /**
     * Executes an operation with retry logic and exponential backoff.
     * 
     * Retry strategy:
     * - Maximum 3 retry attempts
     * - Exponential backoff delays: 1s, 2s, 4s
     * - Authentication errors (401): Fail immediately without retry
     * - Rate limit errors (429): Retry with longer backoff (5s, 10s, 20s)
     * - Other errors: Retry with standard backoff
     * 
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     * @throws IOException if all retry attempts fail
     */
    private <T> T retryWithBackoff(RetryableOperation<T> operation) throws IOException {
        int attempt = 0;
        IOException lastException = null;
        
        while (attempt < MAX_RETRIES) {
            try {
                return operation.execute();
            } catch (IOException e) {
                lastException = e;
                attempt++;
                
                // Don't retry on authentication errors or invalid API key
                if (e.getMessage().contains("400") || e.getMessage().contains("401") || 
                    e.getMessage().contains("403") || 
                    e.getMessage().contains("authentication failed") || 
                    e.getMessage().contains("Invalid API key") ||
                    e.getMessage().contains("API_KEY_INVALID")) {
                    throw e;
                }
                
                // If we've exhausted all retries, throw the exception
                if (attempt >= MAX_RETRIES) {
                    break;
                }
                
                // Calculate delay based on error type
                long delayMs;
                if (e.getMessage().contains("429") || e.getMessage().contains("rate limit")) {
                    // Longer backoff for rate limit errors: 5s, 10s, 20s
                    delayMs = (long) Math.pow(2, attempt + 1) * 2500;
                } else {
                    // Standard backoff: 1s, 2s, 4s
                    delayMs = (long) Math.pow(2, attempt - 1) * 1000;
                }
                
                // Log retry attempt (in production, use proper logging)
                System.err.println("AIConnector: Request failed (attempt " + attempt + "/" + MAX_RETRIES + 
                                 "), retrying in " + delayMs + "ms: " + e.getMessage());
                
                // Sleep before retry
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
            }
        }
        
        // All retries failed
        throw new IOException("Failed after " + MAX_RETRIES + " attempts: " + 
                            (lastException != null ? lastException.getMessage() : "Unknown error"), 
                            lastException);
    }
    
    /**
     * Functional interface for operations that can be retried.
     * 
     * @param <T> the return type
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        /**
         * Executes the operation.
         * 
         * @return the result
         * @throws IOException if the operation fails
         */
        T execute() throws IOException;
    }
    
    // ---- Helper Methods ----
    
    /**
     * Detects MIME type from file extension.
     * 
     * @param filename the filename
     * @return the MIME type
     */
    private String detectMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/png"; // Default
    }
}
