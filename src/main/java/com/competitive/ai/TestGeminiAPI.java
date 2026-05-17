package com.competitive.ai;

import java.io.IOException;

/**
 * Simple test class to verify Gemini API key is working.
 * 
 * This is a quick manual test - NOT a unit test.
 * Run this to verify your API key before proceeding with development.
 * 
 * @author Nguyễn Thành Huy
 */
public class TestGeminiAPI {
    
    public static void main(String[] args) {
        // Your Gemini API key - set via environment variable or replace here
        String apiKey = System.getenv("GEMINI_API_KEY");
        
        System.out.println("=".repeat(60));
        System.out.println("Testing Gemini API Connection");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Create AIConnector with Gemini provider
        AIConnector connector = new AIConnector(AIConnector.Provider.GEMINI, apiKey);
        
        // Test 1: Simple text request
        System.out.println("Test 1: Simple text request");
        System.out.println("-".repeat(60));
        try {
            String prompt = "Say 'Hello, I am working!' in exactly one sentence.";
            System.out.println("Sending prompt: " + prompt);
            System.out.println("Waiting for response...");
            
            long startTime = System.currentTimeMillis();
            String response = connector.sendRequest(prompt);
            long elapsed = System.currentTimeMillis() - startTime;
            
            System.out.println("✅ SUCCESS!");
            System.out.println("Response time: " + elapsed + "ms");
            System.out.println("Response: " + response);
            System.out.println();
            
        } catch (IOException e) {
            System.err.println("❌ FAILED!");
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            e.printStackTrace();
            return; // Stop if first test fails
        }
        
        // Test 2: Problem analysis request (more realistic)
        System.out.println("Test 2: Problem analysis request");
        System.out.println("-".repeat(60));
        try {
            String problemText = """
                Problem: Sum of Two Numbers
                
                Given two integers a and b, calculate their sum.
                
                Input: Two integers a and b (1 ≤ a, b ≤ 1000)
                Output: Single integer a + b
                
                Time Limit: 1 second
                Memory Limit: 256 MB
                
                Example:
                Input: 2 3
                Output: 5
                """;
            
            String prompt = "Analyze this competitive programming problem and extract: title, time limit, memory limit, input format, output format. Return as JSON.";
            System.out.println("Sending problem analysis request...");
            System.out.println("Waiting for response...");
            
            long startTime = System.currentTimeMillis();
            String response = connector.sendRequest(prompt + "\n\n" + problemText);
            long elapsed = System.currentTimeMillis() - startTime;
            
            System.out.println("✅ SUCCESS!");
            System.out.println("Response time: " + elapsed + "ms");
            System.out.println("Response (first 500 chars):");
            System.out.println(response.substring(0, Math.min(500, response.length())));
            if (response.length() > 500) {
                System.out.println("... (truncated)");
            }
            System.out.println();
            
        } catch (IOException e) {
            System.err.println("❌ FAILED!");
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            e.printStackTrace();
            return;
        }
        
        // Test 3: Test retry logic with invalid API key
        System.out.println("Test 3: Test error handling (invalid API key)");
        System.out.println("-".repeat(60));
        long startTime3 = System.currentTimeMillis();
        try {
            AIConnector badConnector = new AIConnector(AIConnector.Provider.GEMINI, "invalid_key_12345");
            System.out.println("Sending request with invalid API key...");
            System.out.println("Expected: Should fail immediately without retry");
            
            badConnector.sendRequest("Test");
            long elapsed = System.currentTimeMillis() - startTime3;
            
            System.err.println("❌ UNEXPECTED: Request succeeded with invalid key!");
            
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - startTime3;
            System.out.println("✅ EXPECTED FAILURE!");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Failed quickly (no retry): " + (elapsed < 2000 ? "YES" : "NO"));
            System.out.println();
        }
        
        // Summary
        System.out.println("=".repeat(60));
        System.out.println("All tests completed!");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("✅ Your Gemini API key is working correctly!");
        System.out.println("✅ AIConnector is ready to use!");
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("1. Implement PromptBuilder.java");
        System.out.println("2. Implement ResponseParser.java");
        System.out.println("3. Implement AIService.java");
    }
}
