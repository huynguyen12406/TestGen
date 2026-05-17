package com.competitive.ai;

import java.io.File;

/**
 * Test OCR functionality - Extract text from image.
 * 
 * @author Nguyễn Thành Huy
 */
public class TestOCR {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("TEST OCR - Extract Text from Image");
        System.out.println("=".repeat(60));
        
        // API key - set via environment variable or replace here
        String visionApiKey = System.getenv("GEMINI_API_KEY");
        
        // Image file (change this to your image path)
        String imagePath = "sample-problem-image.html"; // Change to actual image file
        File imageFile = new File(imagePath);
        
        if (!imageFile.exists()) {
            System.err.println("[ERROR] File không tồn tại: " + imagePath);
            System.err.println("[INFO] Vui lòng thay đổi imagePath trong TestOCR.java");
            return;
        }
        
        try {
            // Create AIService with Gemini Vision
            AIService aiService = new AIService(AIConnector.Provider.GEMINI, visionApiKey);
            
            System.out.println("[INFO] Đang đọc text từ ảnh: " + imagePath);
            System.out.println("[INFO] Kết nối với Gemini Vision API...");
            
            // Extract text
            long startTime = System.currentTimeMillis();
            String extractedText = aiService.extractTextFromImage(imageFile);
            long elapsed = System.currentTimeMillis() - startTime;
            
            // Print results
            System.out.println("\n" + "=".repeat(60));
            System.out.println("TEXT ĐƯỢC TRÍCH XUẤT:");
            System.out.println("=".repeat(60));
            System.out.println(extractedText);
            System.out.println("=".repeat(60));
            
            System.out.println("\n[INFO] ✓ Thành công!");
            System.out.println("[INFO] Độ dài: " + extractedText.length() + " ký tự");
            System.out.println("[INFO] Thời gian: " + elapsed + "ms");
            
        } catch (Exception e) {
            System.err.println("\n[ERROR] ✗ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
