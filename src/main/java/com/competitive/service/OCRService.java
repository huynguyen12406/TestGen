package com.competitive.service;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * OCR Service using Tesseract Portable.
 * 
 * Tesseract is bundled in the project folder (tesseract/tesseract.exe).
 * Users don't need to install anything - just extract and run.
 * 
 * @author Nguyễn Thành Huy
 * @version 1.0 - Portable
 */
public class OCRService {
    
    private boolean tesseractAvailable = false;
    private String tesseractPath = null;
    
    public OCRService() {
        checkTesseractAvailability();
    }
    
    /**
     * Check if Tesseract portable is available in project folder.
     */
    private void checkTesseractAvailability() {
        // Get project directory (where the JAR or classes are running from)
        String projectDir = System.getProperty("user.dir");
        
        // Check for tesseract/tesseract.exe
        File tesseractExe = new File(projectDir, "tesseract/tesseract.exe");
        
        if (tesseractExe.exists() && tesseractExe.canExecute()) {
            tesseractAvailable = true;
            tesseractPath = tesseractExe.getAbsolutePath();
            System.out.println("[OCR] Tesseract found at: " + tesseractPath);
        } else {
            tesseractAvailable = false;
            System.out.println("[OCR] Tesseract not found at: " + tesseractExe.getAbsolutePath());
            System.out.println("[OCR] Please download Tesseract from https://github.com/UB-Mannheim/tesseract/wiki");
            System.out.println("[OCR] and extract/copy into the 'tesseract/' folder in the project root.");
            System.out.println("[OCR] Required path: " + projectDir + "/tesseract/tesseract.exe");
        }
    }
    
    /**
     * Check if Tesseract is available.
     */
    public boolean isTesseractAvailable() {
        return tesseractAvailable;
    }
    
    /**
     * Extract text from image using Tesseract OCR.
     * 
     * @param imageFile Image file (PNG, JPG, JPEG)
     * @return Extracted text
     * @throws IOException if OCR fails
     */
    public String extractText(File imageFile) throws IOException {
        if (!tesseractAvailable) {
            throw new IOException("Tesseract not available. Download from https://github.com/UB-Mannheim/tesseract/wiki and extract to tesseract/ folder.");
        }
        
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imageFile.getAbsolutePath());
        }
        
        // Create temp file for output
        File outputFile = File.createTempFile("tesseract-output-", ".txt");
        String outputBase = outputFile.getAbsolutePath().replace(".txt", "");
        
        try {
            // Build command: tesseract image.png output -l eng
            ProcessBuilder pb = new ProcessBuilder(
                tesseractPath,
                imageFile.getAbsolutePath(),
                outputBase,
                "-l", "eng"  // English language
            );
            
            // Set working directory to tesseract folder (for tessdata)
            File tesseractDir = new File(tesseractPath).getParentFile();
            pb.directory(tesseractDir);
            
            // Redirect error stream
            pb.redirectErrorStream(true);
            
            // Start process
            Process process = pb.start();
            
            // Read output (for debugging)
            StringBuilder processOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append("\n");
                }
            }
            
            // Wait for completion (timeout 30 seconds)
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Tesseract OCR timeout (30s)");
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode != 0) {
                throw new IOException("Tesseract OCR failed (exit code " + exitCode + "):\n" + processOutput);
            }
            
            // Read output file (Tesseract adds .txt automatically)
            File actualOutputFile = new File(outputBase + ".txt");
            
            if (!actualOutputFile.exists()) {
                throw new IOException("Tesseract output file not found: " + actualOutputFile.getAbsolutePath());
            }
            
            String extractedText = Files.readString(actualOutputFile.toPath());
            
            // Clean up output file
            actualOutputFile.delete();
            
            return extractedText;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Tesseract OCR interrupted", e);
        } finally {
            // Clean up temp files
            outputFile.delete();
            new File(outputBase + ".txt").delete();
        }
    }
    
    /**
     * Extract text from image with progress callback.
     * 
     * @param imageFile Image file
     * @param progressCallback Callback for progress messages
     * @return Extracted text
     * @throws IOException if OCR fails
     */
    public String extractText(File imageFile, java.util.function.Consumer<String> progressCallback) 
            throws IOException {
        
        if (progressCallback != null) {
            progressCallback.accept("[OCR] Checking Tesseract availability...");
        }
        
        if (!tesseractAvailable) {
            if (progressCallback != null) {
                progressCallback.accept("[OCR] ✗ Tesseract not found");
            }
            throw new IOException("Tesseract not available. Download from https://github.com/UB-Mannheim/tesseract/wiki and extract to tesseract/ folder.");
        }
        
        if (progressCallback != null) {
            progressCallback.accept("[OCR] ✓ Tesseract found");
            progressCallback.accept("[OCR] Processing image: " + imageFile.getName());
        }
        
        String result = extractText(imageFile);
        
        if (progressCallback != null) {
            progressCallback.accept("[OCR] ✓ Extracted " + result.length() + " characters");
        }
        
        return result;
    }
    
    /**
     * Get Tesseract version (for debugging).
     */
    public String getTesseractVersion() throws IOException {
        if (!tesseractAvailable) {
            return "Not available";
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(tesseractPath, "--version");
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            process.waitFor(5, TimeUnit.SECONDS);
            
            return output.toString().trim();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
