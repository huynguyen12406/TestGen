package com.competitive.ai;

/**
 * Quick test để kiểm tra xem Eclipse đã compile đúng chưa
 * 
 * Cách chạy:
 * 1. Click phải vào file này trong Eclipse
 * 2. Run As → Java Application
 * 3. Xem kết quả trong Console
 * 
 * @author Nguyễn Thành Huy
 */
public class QuickTest {
    
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("QUICK TEST - Kiểm tra Eclipse compile");
        System.out.println("============================================================");
        System.out.println();
        
        // Test 1: Kiểm tra AIConnector class có load được không
        System.out.println("Test 1: Load AIConnector class");
        System.out.println("------------------------------------------------------------");
        try {
            Class<?> clazz = Class.forName("com.competitive.ai.AIConnector");
            System.out.println("✅ SUCCESS! AIConnector class loaded");
            System.out.println("   Class name: " + clazz.getName());
            System.out.println("   Package: " + clazz.getPackage().getName());
        } catch (ClassNotFoundException e) {
            System.out.println("❌ FAILED! AIConnector class not found");
            System.out.println("   Eclipse chưa compile AIConnector.java");
            System.out.println("   Hãy chạy: Project → Clean");
            return;
        }
        System.out.println();
        
        // Test 2: Tạo AIConnector instance
        System.out.println("Test 2: Create AIConnector instance");
        System.out.println("------------------------------------------------------------");
        try {
            AIConnector connector = new AIConnector(
                AIConnector.Provider.GEMINI, 
                "test-key-123"
            );
            System.out.println("✅ SUCCESS! AIConnector instance created");
            System.out.println("   Instance: " + connector.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("❌ FAILED! Cannot create AIConnector");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println();
        
        // Test 3: Kiểm tra constants
        System.out.println("Test 3: Check AIConnector constants");
        System.out.println("------------------------------------------------------------");
        try {
            // Dùng reflection để đọc private constants
            java.lang.reflect.Field field = AIConnector.class.getDeclaredField("GEMINI_API_URL");
            field.setAccessible(true);
            String geminiUrl = (String) field.get(null);
            
            System.out.println("✅ SUCCESS! Constants accessible");
            System.out.println("   GEMINI_API_URL: " + geminiUrl);
            
            // Kiểm tra model name trong URL
            if (geminiUrl.contains("gemini-1.5-flash")) {
                System.out.println("   ✅ Model name CORRECT: gemini-1.5-flash");
            } else if (geminiUrl.contains("gemini-1.5-pro")) {
                System.out.println("   ❌ Model name WRONG: gemini-1.5-pro");
                System.out.println("   Eclipse chưa compile lại file mới!");
                System.out.println("   Hãy chạy: Project → Clean");
            } else {
                System.out.println("   ⚠ Model name UNKNOWN: " + geminiUrl);
            }
        } catch (Exception e) {
            System.out.println("❌ FAILED! Cannot access constants");
            System.out.println("   Error: " + e.getMessage());
        }
        System.out.println();
        
        // Test 4: Kiểm tra dependencies
        System.out.println("Test 4: Check dependencies");
        System.out.println("------------------------------------------------------------");
        
        // OkHttp
        try {
            Class.forName("okhttp3.OkHttpClient");
            System.out.println("   ✅ OkHttp available");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ OkHttp NOT available");
        }
        
        // Jackson
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            System.out.println("   ✅ Jackson available");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ Jackson NOT available");
        }
        
        System.out.println();
        System.out.println("============================================================");
        System.out.println("QUICK TEST COMPLETED");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Nếu tất cả tests PASS:");
        System.out.println("  → Eclipse đã compile đúng");
        System.out.println("  → Có thể chạy TestGeminiAPI");
        System.out.println();
        System.out.println("Nếu có test FAIL:");
        System.out.println("  → Chạy: Project → Clean");
        System.out.println("  → Đợi Eclipse compile xong");
        System.out.println("  → Chạy lại QuickTest này");
    }
}
