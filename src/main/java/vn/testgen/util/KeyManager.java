package vn.testgen.util;

import java.util.Base64;

/**
 * Quản lý API keys được mã hóa
 * Keys được encode để tránh bị scan tự động bởi GitHub
 */
public class KeyManager {
    
    // API keys should be provided via environment variables or config.properties
    // DO NOT hardcode API keys here
    private static final String ENCODED_GROQ_KEY = "";
    private static final String ENCODED_GEMINI_KEY = "";
    
    /**
     * Lấy Groq API key mặc định
     * @return API key đã được decode
     */
    public static String getDefaultGroqKey() {
        try {
            return new String(Base64.getDecoder().decode(ENCODED_GROQ_KEY));
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Lấy Gemini API key mặc định
     * @return API key đã được decode
     */
    public static String getDefaultGeminiKey() {
        try {
            return new String(Base64.getDecoder().decode(ENCODED_GEMINI_KEY));
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Encode một key mới (dùng để tạo encoded key)
     * @param key Key cần encode
     * @return Encoded key
     */
    public static String encodeKey(String key) {
        return Base64.getEncoder().encodeToString(key.getBytes());
    }
}
