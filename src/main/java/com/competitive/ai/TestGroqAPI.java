package com.competitive.ai;

import com.competitive.model.Problem;

/**
 * Test Groq API integration.
 * 
 * @author Nguyễn Thành Huy
 */
public class TestGroqAPI {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("TEST GROQ API INTEGRATION");
        System.out.println("=".repeat(60));
        
        // Your Groq API key - set via environment variable or replace here
        String groqApiKey = System.getenv("GROQ_API_KEY");
        
        // Create AIService with Groq provider
        AIService aiService = new AIService(AIConnector.Provider.GROQ, groqApiKey);
        
        System.out.println("\n[INFO] Provider: " + aiService.getProvider());
        System.out.println("[INFO] Model: llama-3.3-70b-versatile");
        System.out.println();
        
        // Test 1: Analyze problem from text
        System.out.println("TEST 1: Phân tích đề bài");
        System.out.println("-".repeat(60));
        
        String problemText = """
Bài toán: Số chẵn lẻ

Mô tả:
Cho một số nguyên n. Hãy kiểm tra xem n là số chẵn hay số lẻ.

Input:
- Một số nguyên n (1 ≤ n ≤ 10^9)

Output:
- In ra "CHAN" nếu n là số chẵn
- In ra "LE" nếu n là số lẻ

Ví dụ:
Input: 4
Output: CHAN

Input: 5
Output: LE
""";
        
        try {
            System.out.println("[INFO] Đang gọi Groq API...");
            long startTime = System.currentTimeMillis();
            
            Problem problem = aiService.analyzeProblem(problemText);
            
            long elapsed = System.currentTimeMillis() - startTime;
            
            System.out.println("[SUCCESS] ✓ Phân tích xong trong " + elapsed + "ms");
            System.out.println();
            System.out.println("Kết quả:");
            System.out.println("  Title: " + problem.getTitle());
            System.out.println("  Input Format: " + problem.getInputFormat());
            System.out.println("  Output Format: " + problem.getOutputFormat());
            System.out.println("  Constraints: " + problem.getConstraints());
            System.out.println("  Time Limit: " + problem.getTimeLimit());
            System.out.println("  Memory Limit: " + problem.getMemoryLimit());
            
        } catch (Exception e) {
            System.err.println("[ERROR] ✗ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("TEST HOÀN TẤT");
        System.out.println("=".repeat(60));
    }
}
