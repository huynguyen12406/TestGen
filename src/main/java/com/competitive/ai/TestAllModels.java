package com.competitive.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;

/**
 * Test all possible Gemini model names to find which one works
 * 
 * @author Nguyễn Thành Huy
 */
public class TestAllModels {
    
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("TESTING ALL POSSIBLE GEMINI MODELS");
        System.out.println("============================================================");
        System.out.println();
        
        // List of possible model names to try
        String[] models = {
            "gemini-1.5-flash",
            "gemini-1.5-flash-latest",
            "gemini-1.5-flash-8b",
            "gemini-1.5-flash-8b-latest",
            "gemini-1.5-pro",
            "gemini-1.5-pro-latest",
            "gemini-pro",
            "gemini-pro-vision",
            "gemini-1.0-pro",
            "gemini-1.0-pro-latest"
        };
        
        // List of API versions to try
        String[] versions = {"v1beta", "v1"};
        
        int successCount = 0;
        String workingModel = null;
        String workingVersion = null;
        
        for (String version : versions) {
            System.out.println("Testing API version: " + version);
            System.out.println("-".repeat(60));
            
            for (String model : models) {
                String url = String.format(
                    "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
                    version, model, API_KEY
                );
                
                System.out.print("  " + model + " ... ");
                
                try {
                    // Create simple request
                    ObjectNode root = mapper.createObjectNode();
                    ArrayNode contents = mapper.createArrayNode();
                    ObjectNode content = mapper.createObjectNode();
                    ArrayNode parts = mapper.createArrayNode();
                    ObjectNode part = mapper.createObjectNode();
                    part.put("text", "Say 'OK'");
                    parts.add(part);
                    content.set("parts", parts);
                    contents.add(content);
                    root.set("contents", contents);
                    
                    String jsonBody = mapper.writeValueAsString(root);
                    
                    RequestBody body = RequestBody.create(
                        jsonBody,
                        MediaType.parse("application/json")
                    );
                    
                    Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                    
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            System.out.println("✅ WORKS!");
                            successCount++;
                            if (workingModel == null) {
                                workingModel = model;
                                workingVersion = version;
                            }
                        } else {
                            int code = response.code();
                            if (code == 404) {
                                System.out.println("❌ Not found");
                            } else if (code == 401) {
                                System.out.println("❌ Auth error");
                            } else if (code == 429) {
                                System.out.println("⚠️  Rate limit");
                            } else {
                                System.out.println("❌ HTTP " + code);
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    System.out.println("❌ Error: " + e.getMessage());
                }
                
                // Small delay to avoid rate limiting
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            System.out.println();
        }
        
        System.out.println("============================================================");
        System.out.println("RESULTS");
        System.out.println("============================================================");
        System.out.println("Total working models: " + successCount);
        
        if (workingModel != null) {
            System.out.println();
            System.out.println("✅ RECOMMENDED MODEL:");
            System.out.println("   Model: " + workingModel);
            System.out.println("   API Version: " + workingVersion);
            System.out.println();
            System.out.println("Use this in AIConnector.java:");
            System.out.println("   private static final String GEMINI_API_URL = ");
            System.out.println("       \"https://generativelanguage.googleapis.com/" + workingVersion + 
                             "/models/" + workingModel + ":generateContent\";");
        } else {
            System.out.println();
            System.out.println("❌ NO WORKING MODELS FOUND!");
            System.out.println("   Check your API key or try again later.");
        }
        
        System.out.println();
        System.out.println("============================================================");
    }
}
