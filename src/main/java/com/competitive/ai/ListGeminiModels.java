package com.competitive.ai;

import okhttp3.*;
import java.io.IOException;

/**
 * List all available Gemini models for your API key
 * 
 * @author Nguyễn Thành Huy
 */
public class ListGeminiModels {
    
    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        
        System.out.println("============================================================");
        System.out.println("LISTING AVAILABLE GEMINI MODELS");
        System.out.println("============================================================");
        System.out.println();
        
        // Create HTTP client
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        
        // List models endpoint
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("❌ FAILED!");
                System.err.println("HTTP " + response.code() + ": " + response.message());
                if (response.body() != null) {
                    System.err.println(response.body().string());
                }
                return;
            }
            
            String responseBody = response.body().string();
            
            System.out.println("✅ SUCCESS! Available models:");
            System.out.println();
            System.out.println(responseBody);
            System.out.println();
            
            // Parse and show model names
            System.out.println("============================================================");
            System.out.println("EXTRACTING MODEL NAMES");
            System.out.println("============================================================");
            System.out.println();
            
            // Simple parsing (find all "name": "models/..." patterns)
            String[] lines = responseBody.split("\n");
            int count = 0;
            for (String line : lines) {
                if (line.contains("\"name\":") && line.contains("models/")) {
                    // Extract model name
                    int start = line.indexOf("models/");
                    int end = line.indexOf("\"", start);
                    if (start != -1 && end != -1) {
                        String modelName = line.substring(start, end);
                        count++;
                        System.out.println(count + ". " + modelName);
                        
                        // Check if it supports generateContent
                        if (line.contains("generateContent") || 
                            (lines.length > count && lines[count].contains("generateContent"))) {
                            System.out.println("   ✅ Supports generateContent");
                        }
                    }
                }
            }
            
            System.out.println();
            System.out.println("============================================================");
            System.out.println("TOTAL: " + count + " models found");
            System.out.println("============================================================");
            System.out.println();
            System.out.println("Recommended models for free tier:");
            System.out.println("  • models/gemini-1.5-flash");
            System.out.println("  • models/gemini-1.5-flash-latest");
            System.out.println("  • models/gemini-pro");
            
        } catch (IOException e) {
            System.err.println("❌ FAILED!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
