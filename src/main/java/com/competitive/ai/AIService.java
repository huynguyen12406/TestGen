package com.competitive.ai;

import com.competitive.model.Problem;
import com.competitive.model.Testcase;
import com.competitive.model.SampleCode;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main API cho AI operations - orchestrate toàn bộ workflow.
 * Kết hợp AIConnector + PromptBuilder + ResponseParser để:
 * - Phân tích đề bài (text hoặc image)
 * - Sinh test cases
 * - Sinh checker code
 * - Sinh sample code (AC, WA, TLE)
 * 
 * @author Nguyễn Thành Huy
 * @version 1.0
 */
public class AIService {
    
    private AIConnector connector;
    private int defaultTestcaseCount = 10;
    
    // ===== CONSTRUCTORS =====
    
    /**
     * Tạo AIService với provider và API key.
     * 
     * @param provider AI provider (GEMINI hoặc OPENAI)
     * @param apiKey API key cho provider
     */
    public AIService(AIConnector.Provider provider, String apiKey) {
        this.connector = new AIConnector(provider, apiKey);
    }
    
    // ===== CONFIGURATION =====
    
    /**
     * Đổi AI provider (GEMINI hoặc OPENAI).
     */
    public void setProvider(AIConnector.Provider provider) {
        this.connector.setProvider(provider);
    }
    
    /**
     * Đổi API key.
     */
    public void setApiKey(String apiKey) {
        this.connector.setApiKey(apiKey);
    }
    
    /**
     * Set số lượng test case mặc định khi generate.
     */
    public void setDefaultTestcaseCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Test case count must be at least 1");
        }
        this.defaultTestcaseCount = count;
    }
    
    /**
     * Get AI provider hiện tại.
     */
    public AIConnector.Provider getProvider() {
        return this.connector.getProvider();
    }
    
    // ===== MAIN API METHODS =====
    
    /**
     * Workflow hoàn chỉnh: Phân tích đề bài từ text và generate tất cả.
     * 
     * Các bước:
     * 1. Phân tích đề bài → Problem object
     * 2. Sinh test cases
     * 3. Sinh checker code
     * 4. Sinh sample code (AC, WA, TLE)
     * 
     * @param problemText văn bản đề bài
     * @return Problem object với đầy đủ thông tin
     * @throws IOException nếu có lỗi ở bất kỳ bước nào
     */
    public Problem analyzeAndGenerate(String problemText) throws IOException {
        // Step 1: Analyze problem
        Problem problem = analyzeProblem(problemText);
        
        // Step 2: Generate test cases
        generateTestcases(problem, defaultTestcaseCount);
        
        // Step 3: Generate checker (if needed)
        try {
            generateChecker(problem);
        } catch (IOException e) {
            // Checker generation is optional, log warning but continue
            System.err.println("Warning: Failed to generate checker - " + e.getMessage());
        }
        
        // Step 4: Generate sample code (AC, WA, TLE)
        generateSampleCode(problem, SampleCode.Language.JAVA);
        
        return problem;
    }
    
    /**
     * Workflow hoàn chỉnh: Phân tích đề bài từ image và generate tất cả.
     * 
     * @param imageFile file ảnh chứa đề bài
     * @return Problem object với đầy đủ thông tin
     * @throws IOException nếu có lỗi ở bất kỳ bước nào
     */
    public Problem analyzeAndGenerate(File imageFile) throws IOException {
        // Step 1: Analyze problem from image
        Problem problem = analyzeProblemFromImage(imageFile);
        
        // Step 2-4: Same as text workflow
        generateTestcases(problem, defaultTestcaseCount);
        
        try {
            generateChecker(problem);
        } catch (IOException e) {
            System.err.println("Warning: Failed to generate checker - " + e.getMessage());
        }
        
        generateSampleCode(problem, SampleCode.Language.JAVA);
        
        return problem;
    }
    
    // ===== INDIVIDUAL OPERATIONS =====
    
    /**
     * Chỉ phân tích đề bài từ text (không generate test cases/code).
     * 
     * @param problemText văn bản đề bài
     * @return Problem object với thông tin cơ bản
     * @throws IOException nếu phân tích thất bại
     */
    public Problem analyzeProblem(String problemText) throws IOException {
        // Cho phép text trống nếu là placeholder cho image
        if (problemText == null || (problemText.isBlank() && !problemText.contains("[Đọc đề bài từ hình ảnh]"))) {
            throw new IllegalArgumentException("AIService analyzeProblem: Problem text cannot be empty");
        }
        
        try {
            // Build prompt
            String prompt = PromptBuilder.analyzeProblemFromText(problemText);
            
            // Send request to AI
            String response = connector.sendRequest(prompt);
            
            // Parse response
            Problem problem = ResponseParser.parseProblemAnalysis(response);
            
            return problem;
            
        } catch (IOException e) {
            throw new IOException("AIService analyzeProblem: Failed to analyze problem - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Chỉ phân tích đề bài từ image (không generate test cases/code).
     * 
     * @param imageFile file ảnh chứa đề bài
     * @return Problem object với thông tin cơ bản
     * @throws IOException nếu phân tích thất bại
     */
    public Problem analyzeProblemFromImage(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("AIService analyzeProblemFromImage: Image file does not exist");
        }
        
        try {
            // Build prompt
            String prompt = PromptBuilder.analyzeProblemFromImage();
            
            // Send request with image to AI
            String response = connector.sendRequestWithImage(prompt, imageFile);
            
            // Parse response
            Problem problem = ResponseParser.parseProblemAnalysis(response);
            
            // Set image path
            problem.setImagePath(imageFile.getAbsolutePath());
            
            return problem;
            
        } catch (IOException e) {
            throw new IOException("AIService analyzeProblemFromImage: Failed to analyze problem from image - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * OCR - Extract text from image (không parse, chỉ lấy text thuần).
     * 
     * @param imageFile file hình ảnh
     * @return Text được trích xuất từ ảnh
     * @throws IOException nếu trích xuất thất bại
     */
    public String extractTextFromImage(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("AIService extractTextFromImage: Image file does not exist");
        }
        
        try {
            // Build simple OCR prompt
            String prompt = """
Bạn là chuyên gia OCR (Optical Character Recognition).

Hãy đọc và trích xuất TẤT CẢ text từ hình ảnh này.

YÊU CẦU:
- Trích xuất CHÍNH XÁC tất cả text
- Giữ nguyên format, xuống dòng, khoảng trắng
- KHÔNG thêm giải thích, KHÔNG thêm markdown
- KHÔNG thêm bất kỳ text nào ngoài text trong ảnh
- Chỉ trả về TEXT THUẦN TÚY từ ảnh

BẮT ĐẦU NGAY VỚI TEXT TỪ ẢNH:
""";
            
            // Send request with image to AI
            String response = connector.sendRequestWithImage(prompt, imageFile);
            
            // Return raw text (no parsing needed)
            return response.trim();
            
        } catch (IOException e) {
            throw new IOException("AIService extractTextFromImage: Failed to extract text from image - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Generate test cases cho problem (modify problem in-place).
     * 
     * @param problem Problem object (phải đã có analysis)
     * @param count số lượng test cases cần generate
     * @throws IOException nếu generation thất bại
     */
    public void generateTestcases(Problem problem, int count) throws IOException {
        if (problem == null) {
            throw new IllegalArgumentException("AIService generateTestcases: Problem cannot be null");
        }
        
        if (count < 1) {
            throw new IllegalArgumentException("AIService generateTestcases: Count must be at least 1");
        }
        
        try {
            populateTestcases(problem, count);
        } catch (IOException e) {
            throw new IOException("AIService generateTestcases: Failed to generate test cases - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Generate test inputs only (KHÔNG có expected output) cho problem.
     * Dùng cho verification workflow: AI sinh inputs, sau đó run AC code để lấy expected output.
     * 
     * @param problem Problem object (phải đã có analysis)
     * @param count số lượng test inputs cần generate
     * @return List of TestInput objects (chỉ có input, không có expected output)
     * @throws IOException nếu generation thất bại
     */
    public List<ResponseParser.TestInput> generateTestInputsOnly(Problem problem, int count) throws IOException {
        if (problem == null) {
            throw new IllegalArgumentException("AIService generateTestInputsOnly: Problem cannot be null");
        }
        
        if (count < 1) {
            throw new IllegalArgumentException("AIService generateTestInputsOnly: Count must be at least 1");
        }
        
        try {
            // Build prompt using PromptBuilder
            String prompt = PromptBuilder.generateTestInputsOnly(problem, count);
            
            // Send request to AI
            String response = connector.sendRequest(prompt);
            
            // Parse response using ResponseParser
            List<ResponseParser.TestInput> testInputs = ResponseParser.parseTestInputsOnly(response);
            
            // Validate that inputs are not too long (< 500 chars each)
            for (int i = 0; i < testInputs.size(); i++) {
                ResponseParser.TestInput testInput = testInputs.get(i);
                if (testInput.getInput().length() > 500) {
                    throw new IOException("AIService generateTestInputsOnly: Test input #" + (i + 1) + 
                                          " exceeds 500 character limit (" + testInput.getInput().length() + " chars)");
                }
            }
            
            return testInputs;
            
        } catch (IOException e) {
            throw new IOException("AIService generateTestInputsOnly: Failed to generate test inputs - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Generate checker code cho problem (modify problem in-place).
     * 
     * @param problem Problem object (phải đã có analysis)
     * @throws IOException nếu generation thất bại
     */
    public void generateChecker(Problem problem) throws IOException {
        if (problem == null) {
            throw new IllegalArgumentException("AIService generateChecker: Problem cannot be null");
        }
        
        try {
            populateChecker(problem);
        } catch (IOException e) {
            throw new IOException("AIService generateChecker: Failed to generate checker - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Generate sample code (AC, WA, TLE) cho problem (modify problem in-place).
     * 
     * @param problem Problem object (phải đã có analysis)
     * @param language ngôn ngữ lập trình
     * @throws IOException nếu generation thất bại
     */
    public void generateSampleCode(Problem problem, SampleCode.Language language) throws IOException {
        if (problem == null) {
            throw new IllegalArgumentException("AIService generateSampleCode: Problem cannot be null");
        }
        
        if (language == null) {
            throw new IllegalArgumentException("AIService generateSampleCode: Language cannot be null");
        }
        
        try {
            populateSampleCode(problem, language);
        } catch (IOException e) {
            throw new IOException("AIService generateSampleCode: Failed to generate sample code - " + 
                                  e.getMessage(), e);
        }
    }
    
    /**
     * Generate nhiều loại code cùng lúc (batch) - tiết kiệm API calls.
     * 
     * @param problem Problem object (phải đã có analysis)
     * @param language ngôn ngữ lập trình
     * @param includeAC có sinh AC code không
     * @param includeWA có sinh WA code không
     * @param includeTLE có sinh TLE code không
     * @throws IOException nếu generation thất bại
     */
    public void generateSampleCodeBatch(Problem problem, SampleCode.Language language,
                                        boolean includeAC, boolean includeWA, boolean includeTLE) throws IOException {
        if (problem == null) {
            throw new IllegalArgumentException("AIService generateSampleCodeBatch: Problem cannot be null");
        }
        
        if (language == null) {
            throw new IllegalArgumentException("AIService generateSampleCodeBatch: Language cannot be null");
        }
        
        // Nếu không có gì được chọn, không làm gì
        if (!includeAC && !includeWA && !includeTLE) {
            return;
        }
        
        try {
            populateSampleCodeBatch(problem, language, includeAC, includeWA, includeTLE);
        } catch (IOException e) {
            throw new IOException("AIService generateSampleCodeBatch: Failed to generate sample code - " + 
                                  e.getMessage(), e);
        }
    }
    
    // ===== INTERNAL WORKFLOW METHODS =====
    
    /**
     * Internal: Generate test cases và add vào problem.
     */
    private void populateTestcases(Problem problem, int count) throws IOException {
        // Build prompt
        String prompt = PromptBuilder.generateTestCases(problem, count);
        
        // Send request
        String response = connector.sendRequest(prompt);
        
        // Parse response
        List<Testcase> testcases = ResponseParser.parseTestcases(response);
        
        // Add to problem
        problem.setTestcases(testcases);
    }
    
    /**
     * Internal: Generate checker code và set vào problem.
     */
    private void populateChecker(Problem problem) throws IOException {
        // Build prompt
        String prompt = PromptBuilder.generateChecker(problem);
        
        // Send request
        String response = connector.sendRequest(prompt);
        
        // Parse response
        String checkerCode = ResponseParser.parseCheckerCode(response);
        
        // Set to problem
        problem.setCheckerCode(checkerCode);
    }
    
    /**
     * Internal: Generate AC, WA, TLE code và set vào problem.
     */
    private void populateSampleCode(Problem problem, SampleCode.Language language) throws IOException {
        // Generate AC code
        String acPrompt = PromptBuilder.generateACCode(problem);
        String acResponse = connector.sendRequest(acPrompt);
        SampleCode acCode = ResponseParser.parseSampleCode(acResponse, SampleCode.CodeType.AC, language);
        problem.setAcCode(acCode.getSourceCode());
        
        // Generate WA code
        String waPrompt = PromptBuilder.generateWACode(problem);
        String waResponse = connector.sendRequest(waPrompt);
        SampleCode waCode = ResponseParser.parseSampleCode(waResponse, SampleCode.CodeType.WA, language);
        problem.setWaCode(waCode.getSourceCode());
        
        // Generate TLE code
        String tlePrompt = PromptBuilder.generateTLECode(problem);
        String tleResponse = connector.sendRequest(tlePrompt);
        SampleCode tleCode = ResponseParser.parseSampleCode(tleResponse, SampleCode.CodeType.TLE, language);
        problem.setTleCode(tleCode.getSourceCode());
    }
    
    /**
     * Internal: Generate nhiều code cùng lúc (batch) - chỉ 1 API call.
     */
    private void populateSampleCodeBatch(Problem problem, SampleCode.Language language,
                                         boolean includeAC, boolean includeWA, boolean includeTLE) throws IOException {
        // Build prompt cho tất cả code được chọn
        String prompt = PromptBuilder.generateMultipleCodes(problem, includeAC, includeWA, includeTLE);
        
        // Gọi AI 1 lần duy nhất
        String response = connector.sendRequest(prompt);
        
        // Parse response để lấy tất cả code
        java.util.Map<SampleCode.CodeType, String> codes = ResponseParser.parseMultipleCodes(response, language);
        
        // Set vào problem
        if (includeAC && codes.containsKey(SampleCode.CodeType.AC)) {
            problem.setAcCode(codes.get(SampleCode.CodeType.AC));
        }
        if (includeWA && codes.containsKey(SampleCode.CodeType.WA)) {
            problem.setWaCode(codes.get(SampleCode.CodeType.WA));
        }
        if (includeTLE && codes.containsKey(SampleCode.CodeType.TLE)) {
            problem.setTleCode(codes.get(SampleCode.CodeType.TLE));
        }
    }
}
