package vn.testgen.backend;

import vn.testgen.model.Problem;
import vn.testgen.model.TestCase;

// Import AIService của Huy
import com.competitive.ai.AIService;
import com.competitive.ai.AIConnector;
import com.competitive.model.Testcase;
import com.competitive.model.SampleCode;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * BackendService TÍCH HỢP với AIService của Huy.
 * 
 * Đây là version mới thay thế BackendService.java cũ (stub).
 * Tích hợp thật với Gemini API thông qua AIService của Huy.
 * 
 * @author Nguyễn Thành Huy (Backend AI) + Hòa (GUI Integration)
 * @version 2.0 - Integrated
 */
public class BackendService {

    private static BackendService instance;
    
    private AIService aiService;
    // API keys - loaded from config.properties or environment variables
    // Users must provide their own API keys via Settings UI or config file
    private String apiKey;
    private String visionApiKey;
    
    // Store generated code for retrieval
    private String lastGeneratedAcCode = null;
    private String lastGeneratedWaCode = null;
    private String lastGeneratedTleCode = null;
    
    private BackendService() {
        // Load API keys from config file or environment variables
        loadApiKeys();
        
        // Always initialize aiService with Groq provider (even with empty key)
        // This prevents NullPointerException when accessing aiService methods
        this.aiService = new AIService(AIConnector.Provider.GROQ, 
                                       (apiKey != null && !apiKey.isEmpty()) ? apiKey : "");
    }
    
    /**
     * Load API keys from config.properties file or environment variables
     */
    private void loadApiKeys() {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        
        // Try to load from config.properties file first
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                apiKey = props.getProperty("groq.api.key", "");
                visionApiKey = props.getProperty("gemini.api.key", "");
                System.out.println("[INFO] Loaded API keys from config.properties");
            } catch (IOException e) {
                System.err.println("[WARN] Could not load config.properties: " + e.getMessage());
            }
        }
        
        // Fallback to environment variables if not found in config file
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("GROQ_API_KEY");
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("[INFO] Loaded GROQ_API_KEY from environment variable");
            }
        }
        
        if (visionApiKey == null || visionApiKey.isEmpty()) {
            visionApiKey = System.getenv("GEMINI_API_KEY");
            if (visionApiKey != null && !visionApiKey.isEmpty()) {
                System.out.println("[INFO] Loaded GEMINI_API_KEY from environment variable");
            }
        }
        
        // Fallback to default encoded keys (for easy setup - users can clone and run immediately)
        if (apiKey == null || apiKey.isEmpty()) {
            // No default key - users must provide their own via Settings UI or config file
            apiKey = "";
            System.out.println("[WARN] No API key found. Please set via Settings or config.properties");
        }
        
        if (visionApiKey == null || visionApiKey.isEmpty()) {
            // No default key - users must provide their own via Settings UI or config file
            visionApiKey = "";
            System.out.println("[WARN] No Vision API key found. Please set via Settings or config.properties");
        }
        
        // Set to empty string if still null
        if (apiKey == null) apiKey = "";
        if (visionApiKey == null) visionApiKey = "";
    }
    
    public static synchronized BackendService getInstance() {
        if (instance == null) {
            instance = new BackendService();
        }
        return instance;
    }

    // ===== CONFIGURATION =====
    
    public void setApiKey(String key) {
        this.apiKey = key;
        if (this.aiService != null) {
            this.aiService.setApiKey(key);
        }
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setVisionApiKey(String key) {
        this.visionApiKey = key;
    }
    
    public String getVisionApiKey() {
        return visionApiKey;
    }
    
    public void setProvider(String providerName) {
        try {
            AIConnector.Provider provider = AIConnector.Provider.valueOf(providerName.toUpperCase());
            if (this.aiService != null) {
                this.aiService.setProvider(provider);
            }
            System.out.println("[INFO] Đã chuyển sang provider: " + providerName);
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Provider không hợp lệ: " + providerName);
            System.err.println("[ERROR] Các provider hợp lệ: GEMINI, GROQ, OPENAI");
        }
    }
    
    public String getCurrentProvider() {
        if (this.aiService == null) {
            return "GROQ"; // Default provider
        }
        return this.aiService.getProvider().toString();
    }
    
    // ===== GET GENERATED CODE =====
    
    public String getLastGeneratedAcCode() {
        return lastGeneratedAcCode;
    }
    
    public String getLastGeneratedWaCode() {
        return lastGeneratedWaCode;
    }
    
    public String getLastGeneratedTleCode() {
        return lastGeneratedTleCode;
    }

    // ===== 1. PARSE PROBLEM FROM TEXT =====
    
    /**
     * Phân tích đề bài từ text sử dụng AI (Gemini/OpenAI).
     * 
     * @param rawText văn bản đề bài
     * @param type loại contest (IOI, ICPC, CF, ...)
     * @param logger callback để log progress
     * @return Problem object đã được parse
     * @throws Exception nếu có lỗi
     */
    public Problem parseProblem(String rawText, String type, Consumer<String> logger)
            throws Exception {
        logger.accept("[AI] Đang kết nối với AI API...");
        
        try {
            // Gọi AIService của Huy để phân tích
            logger.accept("[AI] Đang phân tích đề bài...");
            com.competitive.model.Problem huyProblem = aiService.analyzeProblem(rawText);
            
            // Convert từ model của Huy sang model của Hòa
            Problem hoaProblem = convertToHoaModel(huyProblem, type);
            
            logger.accept("[AI] ✓ Phân tích xong: " + hoaProblem.getTitle());
            logger.accept("[AI] ✓ Phát hiện: " + hoaProblem.getConstraints());
            
            return hoaProblem;
            
        } catch (IOException e) {
            // Handle specific errors
            String errorMsg = e.getMessage();
            
            if (errorMsg.contains("401") || errorMsg.contains("API key")) {
                logger.accept("[ERR] ✗ API key không hợp lệ. Kiểm tra lại ở Tab Settings.");
                throw new Exception("API key không hợp lệ", e);
            } else if (errorMsg.contains("429") || errorMsg.contains("rate limit")) {
                logger.accept("[ERR] ✗ Quá nhiều request. Đợi 1 phút rồi thử lại.");
                throw new Exception("Rate limit exceeded", e);
            } else if (errorMsg.contains("timeout")) {
                logger.accept("[ERR] ✗ Timeout. Kiểm tra kết nối internet.");
                throw new Exception("Connection timeout", e);
            } else {
                logger.accept("[ERR] ✗ Lỗi: " + errorMsg);
                throw new Exception("Failed to parse problem: " + errorMsg, e);
            }
        }
    }
    
    /**
     * Phân tích đề bài từ hình ảnh.
     * Workflow: OCR (AI Vision) → Parse text → Problem
     * 
     * @param imageFile file hình ảnh đề bài
     * @param type loại contest (IOI, ICPC, CF, ...)
     * @param logger callback để log progress
     * @return Problem object đã được parse
     * @throws Exception nếu có lỗi
     */
    public Problem parseProblemFromImage(File imageFile, String type, Consumer<String> logger)
            throws Exception {
        
        // Step 1: Extract text from image using AI Vision
        logger.accept("[IMG] Đang trích xuất text từ hình ảnh...");
        String extractedText = extractTextFromImage(imageFile, logger);
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new Exception("Không thể đọc text từ hình ảnh. Vui lòng thử ảnh rõ hơn.");
        }
        
        logger.accept("[IMG] ✓ Đã trích xuất " + extractedText.length() + " ký tự");
        
        // Step 2: Parse the extracted text as a problem
        logger.accept("[IMG] Đang phân tích đề bài từ text...");
        return parseProblem(extractedText, type, logger);
    }
    
    /**
     * OCR - Chuyển hình ảnh thành text thuần túy (không parse).
     * Dùng Tesseract OCR (portable, offline, unlimited).
     * 
     * @param imageFile file hình ảnh
     * @param logger callback để log progress
     * @return Text được trích xuất từ ảnh
     * @throws Exception nếu có lỗi
     */
    public String extractTextFromImage(File imageFile, Consumer<String> logger)
            throws Exception {
        logger.accept("[OCR] Khởi tạo Tesseract OCR...");
        
        try {
            // Create OCR service
            com.competitive.service.OCRService ocrService = new com.competitive.service.OCRService();
            
            // Check if Tesseract is available
            if (!ocrService.isTesseractAvailable()) {
                logger.accept("[OCR] ✗ Tesseract không tìm thấy");
                logger.accept("[OCR] Tải Tesseract tại: https://github.com/UB-Mannheim/tesseract/wiki");
                logger.accept("[OCR] Giải nén vào thư mục tesseract/ trong project root");
                throw new Exception("Tesseract not found. Please download from https://github.com/UB-Mannheim/tesseract/wiki and extract to tesseract/ folder.");
            }
            
            logger.accept("[OCR] ✓ Tesseract sẵn sàng");
            logger.accept("[OCR] Đang đọc text từ hình ảnh...");
            
            // Extract text using Tesseract
            String extractedText = ocrService.extractText(imageFile, logger);
            
            logger.accept("[OCR] ✓ Đã trích xuất " + extractedText.length() + " ký tự (Tesseract OCR)");
            
            return extractedText;
            
        } catch (IOException e) {
            // Handle specific errors
            String errorMsg = e.getMessage();
            
            if (errorMsg.contains("Tesseract not available")) {
                logger.accept("[ERR] ✗ Tesseract chưa được cài đặt");
                logger.accept("[ERR] Tải Tesseract: https://github.com/UB-Mannheim/tesseract/wiki → giải nén vào tesseract/");
                throw new Exception("Tesseract not available. Download from https://github.com/UB-Mannheim/tesseract/wiki and extract to tesseract/ folder.", e);
            } else if (errorMsg.contains("timeout")) {
                logger.accept("[ERR] ✗ OCR timeout (30s)");
                throw new Exception("OCR timeout", e);
            } else if (errorMsg.contains("Image file not found")) {
                logger.accept("[ERR] ✗ File ảnh không tồn tại");
                throw new Exception("Image file not found", e);
            } else {
                logger.accept("[ERR] ✗ Lỗi OCR: " + errorMsg);
                throw new Exception("Failed to extract text from image: " + errorMsg, e);
            }
        }
    }

    // ===== 2. GENERATE TEST CASES =====
    
    /**
     * Sinh test cases sử dụng AI.
     * 
     * @param problem Problem đã được parse
     * @param count số lượng test cases cần sinh
     * @param logger callback để log progress
     * @return List of TestCase
     * @throws Exception nếu có lỗi
     */
    public List<TestCase> generateTestCases(Problem problem, int count,
                                             Consumer<String> logger) throws Exception {
        logger.accept("[GEN] Đang chuẩn bị sinh " + count + " test cases...");
        
        try {
            // Convert sang model của Huy
            com.competitive.model.Problem huyProblem = convertToHuyModel(problem);
            
            // Gọi AIService để generate test cases
            logger.accept("[GEN] Đang gọi AI để sinh test cases...");
            aiService.generateTestcases(huyProblem, count);
            
            // Convert test cases về model của Hòa
            List<TestCase> hoaTests = new ArrayList<>();
            List<Testcase> huyTests = huyProblem.getTestcases();
            
            for (int i = 0; i < Math.min(count, huyTests.size()); i++) {
                Testcase ht = huyTests.get(i);
                
                // Determine test type based on note
                TestCase.Type testType = determineTestType(ht.getNote());
                
                TestCase hoaTest = new TestCase(i + 1, ht.getInput(), testType);
                hoaTest.setExpectedOutput(ht.getExpectedOutput());
                hoaTest.setGenerationTimeMs(100L); // Placeholder
                
                hoaTests.add(hoaTest);
                
                logger.accept(String.format("[GEN] Test #%d [%s] - %d bytes", 
                    i + 1, testType, ht.getInput().length()));
            }
            
            logger.accept("[GEN] ✓ Đã sinh " + hoaTests.size() + " test cases");
            return hoaTests;
            
        } catch (IOException e) {
            logger.accept("[ERR] ✗ Lỗi khi sinh test cases: " + e.getMessage());
            throw new Exception("Failed to generate test cases: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sinh test cases với VERIFICATION - expected output được tạo bằng cách chạy AC code.
     * Đây là workflow mới để fix bug: AC code bị đánh giá WA do AI tính sai expected output.
     * 
     * Workflow:
     * 1. Generate AC code
     * 2. Compile AC code
     * 3. Generate test inputs only (không có expected output)
     * 4. Run AC code với mỗi input để lấy verified expected output
     * 5. Tạo test cases với verified expected outputs
     * 
     * @param problem Problem đã được parse
     * @param count số lượng test cases cần sinh
     * @param logger callback để log progress
     * @return List of TestCase với verified expected outputs
     * @throws Exception nếu có lỗi ở bất kỳ bước nào
     */
    public List<TestCase> generateTestcasesWithVerification(Problem problem, int count,
                                                             Consumer<String> logger) throws Exception {
        logger.accept("[VERIFY] Bắt đầu workflow verification...");
        logger.accept("[VERIFY] Workflow: AC code → Compile → Test inputs → Run AC → Verified outputs");
        
        // Step 1: Generate AC code
        logger.accept("[VERIFY] Step 1/5: Generating AC code...");
        String acCode = generateSolution(problem, "AC", logger);
        
        if (acCode == null || acCode.isEmpty()) {
            throw new Exception("Failed to generate AC code - code is empty");
        }
        
        logger.accept("[VERIFY] ✓ AC code generated (" + acCode.length() + " chars)");
        
        // Step 2: Compile AC code
        logger.accept("[VERIFY] Step 2/5: Compiling AC code...");
        
        com.competitive.service.CompilerService.CompileResult compileResult;
        com.competitive.service.CompilerService compiler = null;
        
        try {
            compiler = new com.competitive.service.CompilerService();
            
            compileResult = compiler.compile(
                acCode,
                com.competitive.service.CompilerService.Language.CPP,
                "solution"
            );
            
            if (!compileResult.isSuccess()) {
                String errorMsg = "AC code compilation failed:\n" + compileResult.getLog();
                logger.accept("[VERIFY] ✗ " + errorMsg);
                throw new Exception(errorMsg);
            }
            
            logger.accept("[VERIFY] ✓ AC code compiled successfully");
            
            // Step 3: Generate test inputs only (without expected outputs)
            logger.accept("[VERIFY] Step 3/5: Generating test inputs only (no expected outputs)...");
            
            // Convert sang model của Huy
            com.competitive.model.Problem huyProblem = convertToHuyModel(problem);
            
            // Generate test inputs only
            List<com.competitive.ai.ResponseParser.TestInput> testInputs = 
                aiService.generateTestInputsOnly(huyProblem, count);
            
            if (testInputs == null || testInputs.isEmpty()) {
                throw new Exception("Failed to generate test inputs - list is empty");
            }
            
            logger.accept("[VERIFY] ✓ Generated " + testInputs.size() + " test inputs");
            
            // Step 4: Run AC code with each input to get verified expected output
            logger.accept("[VERIFY] Step 4/5: Running AC code to generate verified expected outputs...");
            
            List<TestCase> verifiedTestCases = new ArrayList<>();
            int timeLimitMs = problem.getTimeLimitMs();
            
            for (int i = 0; i < testInputs.size(); i++) {
                com.competitive.ai.ResponseParser.TestInput testInput = testInputs.get(i);
                
                logger.accept(String.format("[VERIFY] Running AC code for test #%d...", i + 1));
                
                // Run AC code with this input
                com.competitive.service.CompilerService.RunResult runResult = 
                    compiler.run(compileResult, testInput.getInput(), timeLimitMs);
                
                // Check for runtime errors
                if (!runResult.isSuccess()) {
                    String errorMsg = "AC code execution failed for test #" + (i + 1) + ": " + 
                                      runResult.getStderr();
                    logger.accept("[VERIFY] ✗ " + errorMsg);
                    throw new Exception(errorMsg);
                }
                
                // Check for TLE
                if (runResult.getStatus() == Testcase.Status.TLE) {
                    String errorMsg = "AC code got TLE for test #" + (i + 1) + 
                                      " (elapsed: " + runResult.getElapsedMs() + "ms, limit: " + timeLimitMs + "ms)";
                    logger.accept("[VERIFY] ✗ " + errorMsg);
                    throw new Exception(errorMsg);
                }
                
                // Check for RE
                if (runResult.getStatus() == Testcase.Status.RE) {
                    String errorMsg = "AC code got RE for test #" + (i + 1) + ": " + 
                                      runResult.getStderr();
                    logger.accept("[VERIFY] ✗ " + errorMsg);
                    throw new Exception(errorMsg);
                }
                
                // Step 5: Capture stdout as verified expected output
                String verifiedExpectedOutput = runResult.getStdout();
                
                if (verifiedExpectedOutput == null || verifiedExpectedOutput.isEmpty()) {
                    String errorMsg = "AC code produced empty output for test #" + (i + 1);
                    logger.accept("[VERIFY] ✗ " + errorMsg);
                    throw new Exception(errorMsg);
                }
                
                // Determine test type based on description
                TestCase.Type testType = determineTestTypeFromDescription(testInput.getDescription());
                
                // Create TestCase with verified expected output
                TestCase verifiedTestCase = new TestCase(i + 1, testInput.getInput(), testType);
                verifiedTestCase.setExpectedOutput(verifiedExpectedOutput);
                verifiedTestCase.setGenerationTimeMs(runResult.getElapsedMs());
                
                verifiedTestCases.add(verifiedTestCase);
                
                logger.accept(String.format("[VERIFY] ✓ Test #%d verified (output: %d bytes, time: %dms)", 
                    i + 1, verifiedExpectedOutput.length(), runResult.getElapsedMs()));
            }
            
            logger.accept("[VERIFY] Step 5/5: All test cases verified successfully");
            logger.accept("[VERIFY] ✓ Generated " + verifiedTestCases.size() + 
                          " test cases with VERIFIED expected outputs");
            
            return verifiedTestCases;
            
        } catch (IOException e) {
            logger.accept("[VERIFY] ✗ IOException: " + e.getMessage());
            throw new Exception("Verification workflow failed: " + e.getMessage(), e);
        } finally {
            // Clean up compiler resources
            if (compiler != null) {
                try {
                    compiler.close();
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    // ===== 3. GENERATE CHECKER CODE =====
    
    /**
     * Sinh checker code sử dụng AI.
     * 
     * @param problem Problem đã được parse
     * @param logger callback để log progress
     * @return Checker code (Java)
     * @throws Exception nếu có lỗi
     */
    public String generateChecker(Problem problem, Consumer<String> logger) throws Exception {
        logger.accept("[AI] Đang sinh checker code...");
        
        try {
            // Convert sang model của Huy
            com.competitive.model.Problem huyProblem = convertToHuyModel(problem);
            
            // Gọi AIService để generate checker
            aiService.generateChecker(huyProblem);
            
            String checkerCode = huyProblem.getCheckerCode();
            
            if (checkerCode == null || checkerCode.isEmpty()) {
                logger.accept("[WARN] ⚠ Không sinh được checker. Dùng default checker.");
                return generateDefaultChecker(problem);
            }
            
            logger.accept("[AI] ✓ Checker đã được sinh (" + checkerCode.length() + " chars)");
            return checkerCode;
            
        } catch (IOException e) {
            logger.accept("[WARN] ⚠ Lỗi khi sinh checker: " + e.getMessage());
            logger.accept("[WARN] ⚠ Sử dụng default checker");
            return generateDefaultChecker(problem);
        }
    }

    // ===== 4. GENERATE SOLUTION CODE =====
    
    /**
     * Sinh solution code (AC, WA, TLE) sử dụng AI.
     * 
     * @param problem Problem đã được parse
     * @param solutionType "AC", "WA", hoặc "TLE"
     * @param logger callback để log progress
     * @return Solution code (C++)
     * @throws Exception nếu có lỗi
     */
    public String generateSolution(Problem problem, String solutionType,
                                    Consumer<String> logger) throws Exception {
        logger.accept("[AI] Đang sinh code " + solutionType + "...");
        
        try {
            // Convert sang model của Huy
            com.competitive.model.Problem huyProblem = convertToHuyModel(problem);
            
            // Generate sample code
            aiService.generateSampleCode(huyProblem, SampleCode.Language.CPP);
            
            // Get code based on type
            String code = switch (solutionType.toUpperCase()) {
                case "AC" -> huyProblem.getAcCode();
                case "WA" -> huyProblem.getWaCode();
                case "TLE" -> huyProblem.getTleCode();
                default -> throw new IllegalArgumentException("Invalid solution type: " + solutionType);
            };
            
            if (code == null || code.isEmpty()) {
                throw new IOException("AI returned empty code");
            }
            
            // Store generated code for retrieval
            switch (solutionType.toUpperCase()) {
                case "AC" -> lastGeneratedAcCode = code;
                case "WA" -> lastGeneratedWaCode = code;
                case "TLE" -> lastGeneratedTleCode = code;
            }
            
            logger.accept("[AI] ✓ Code " + solutionType + " đã được sinh (" + code.length() + " chars)");
            return code;
            
        } catch (IOException e) {
            logger.accept("[ERR] ✗ Lỗi khi sinh code: " + e.getMessage());
            throw new Exception("Failed to generate " + solutionType + " code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sinh nhiều loại code cùng lúc (batch) - tiết kiệm API calls.
     * 
     * @param problem Problem đã được parse
     * @param includeAC có sinh AC code không
     * @param includeWA có sinh WA code không
     * @param includeTLE có sinh TLE code không
     * @param logger callback để log progress
     * @throws Exception nếu có lỗi
     */
    public void generateSolutionsBatch(Problem problem, boolean includeAC, boolean includeWA, boolean includeTLE,
                                       Consumer<String> logger) throws Exception {
        // Đếm số code cần sinh
        int count = (includeAC ? 1 : 0) + (includeWA ? 1 : 0) + (includeTLE ? 1 : 0);
        
        if (count == 0) {
            logger.accept("[AI] Không có code nào được chọn để sinh");
            return;
        }
        
        // Nếu chỉ sinh 1 code, dùng method cũ (không batch) để tránh lỗi parse
        if (count == 1) {
            String type = includeAC ? "AC" : (includeWA ? "WA" : "TLE");
            logger.accept("[AI] Đang sinh code " + type + " (1 API call)...");
            generateSolution(problem, type, logger);
            return;
        }
        
        // Sinh nhiều code (2 hoặc 3) - dùng batch
        StringBuilder types = new StringBuilder();
        if (includeAC) types.append("AC");
        if (includeWA) {
            if (types.length() > 0) types.append(", ");
            types.append("WA");
        }
        if (includeTLE) {
            if (types.length() > 0) types.append(", ");
            types.append("TLE");
        }
        
        logger.accept("[AI] Đang sinh code: " + types + " (1 API call)...");
        
        try {
            // Convert sang model của Huy
            com.competitive.model.Problem huyProblem = convertToHuyModel(problem);
            
            // Generate batch
            aiService.generateSampleCodeBatch(huyProblem, SampleCode.Language.CPP, 
                                             includeAC, includeWA, includeTLE);
            
            // Store generated code
            if (includeAC && huyProblem.getAcCode() != null) {
                lastGeneratedAcCode = huyProblem.getAcCode();
                logger.accept("[AI] ✓ Code AC đã được sinh (" + lastGeneratedAcCode.length() + " chars)");
            }
            if (includeWA && huyProblem.getWaCode() != null) {
                lastGeneratedWaCode = huyProblem.getWaCode();
                logger.accept("[AI] ✓ Code WA đã được sinh (" + lastGeneratedWaCode.length() + " chars)");
            }
            if (includeTLE && huyProblem.getTleCode() != null) {
                lastGeneratedTleCode = huyProblem.getTleCode();
                logger.accept("[AI] ✓ Code TLE đã được sinh (" + lastGeneratedTleCode.length() + " chars)");
            }
            
        } catch (IOException e) {
            logger.accept("[ERR] ✗ Lỗi khi sinh code: " + e.getMessage());
            throw new Exception("Failed to generate code batch: " + e.getMessage(), e);
        }
    }

    // ===== 5. RUN SOLUTION (Tích hợp với CompilerService của Trung) =====
    
    /**
     * Compile và run solution trên test cases.
     * 
     * Tích hợp với CompilerService và TestcaseRunner của Trung.
     * 
     * @param code source code
     * @param lang ngôn ngữ (Java, C++, Python)
     * @param tests list of test cases
     * @param timeLimitMs time limit in milliseconds
     * @param logger callback để log progress
     * @return Map từ test index → verdict
     * @throws Exception nếu có lỗi
     */
    public Map<Integer, String> runSolution(String code, String lang,
                                             List<TestCase> tests,
                                             int timeLimitMs,
                                             Consumer<String> logger) throws Exception {
        logger.accept("[RUN] Khởi tạo compiler service...");
        
        // Determine language
        com.competitive.service.CompilerService.Language compilerLang;
        String className = "Main";
        
        switch (lang.toUpperCase()) {
            case "JAVA":
                compilerLang = com.competitive.service.CompilerService.Language.JAVA;
                break;
            case "C++":
            case "CPP":
                compilerLang = com.competitive.service.CompilerService.Language.CPP;
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + lang + ". Only Java and C++ are supported.");
        }
        
        // Use try-with-resources to ensure cleanup
        try (com.competitive.service.CompilerService compiler = 
                new com.competitive.service.CompilerService()) {
            
            logger.accept("[RUN] Biên dịch code " + lang + "...");
            
            // Compile the code
            com.competitive.service.CompilerService.CompileResult compileResult = 
                compiler.compile(code, compilerLang, className);
            
            if (!compileResult.isSuccess()) {
                logger.accept("[RUN] ✗ Biên dịch thất bại");
                throw new Exception("Compilation failed:\n" + compileResult.getLog());
            }
            
            logger.accept("[RUN] ✓ Biên dịch thành công");
            
            // Run each test case
            Map<Integer, String> results = new LinkedHashMap<>();
            
            for (TestCase tc : tests) {
                logger.accept(String.format("[RUN] Đang chạy test #%d...", tc.getIndex()));
                
                try {
                    // Run the solution with the test input
                    com.competitive.service.CompilerService.RunResult runResult = 
                        compiler.run(compileResult, tc.getInput(), timeLimitMs);
                    
                    String verdict;
                    long elapsed = runResult.getElapsedMs();
                    
                    // Determine verdict based on run result
                    if (runResult.getStatus() == Testcase.Status.TLE) {
                        verdict = "TLE";
                        logger.accept(String.format("[RUN] Test #%d → TLE (%dms)", tc.getIndex(), elapsed));
                    } else if (runResult.getStatus() == Testcase.Status.RE) {
                        verdict = "RE";
                        logger.accept(String.format("[RUN] Test #%d → RE (%dms)", tc.getIndex(), elapsed));
                    } else if (runResult.getStatus() == Testcase.Status.CE) {
                        verdict = "CE";
                        logger.accept(String.format("[RUN] Test #%d → CE", tc.getIndex()));
                    } else {
                        // Compare output with expected
                        String actualOutput = runResult.getStdout();
                        String expectedOutput = tc.getExpectedOutput();
                        
                        // Use compiler's judge method for comparison
                        Testcase.Status judgeResult = compiler.judge(expectedOutput, actualOutput);
                        
                        if (judgeResult == Testcase.Status.AC) {
                            verdict = "AC";
                            logger.accept(String.format("[RUN] Test #%d → AC (%dms)", tc.getIndex(), elapsed));
                        } else {
                            verdict = "WA";
                            logger.accept(String.format("[RUN] Test #%d → WA (%dms)", tc.getIndex(), elapsed));
                        }
                    }
                    
                    results.put(tc.getIndex(), verdict);
                    
                } catch (IOException e) {
                    // Handle runtime errors
                    results.put(tc.getIndex(), "RE");
                    logger.accept(String.format("[RUN] Test #%d → RE (Error: %s)", tc.getIndex(), e.getMessage()));
                }
            }
            
            // Summary
            long acCount = results.values().stream().filter(v -> v.equals("AC")).count();
            logger.accept(String.format("[RUN] ✓ Hoàn thành: %d/%d AC", acCount, tests.size()));
            
            return results;
            
        } catch (IOException e) {
            logger.accept("[ERR] ✗ Lỗi khởi tạo compiler: " + e.getMessage());
            throw new Exception("Failed to initialize compiler: " + e.getMessage(), e);
        }
    }

    // ===== HELPER METHODS =====
    
    /**
     * Convert từ model của Huy sang model của Hòa.
     */
    private Problem convertToHoaModel(com.competitive.model.Problem huyProblem, String type) {
        Problem hoaProblem = new Problem();
        
        hoaProblem.setTitle(huyProblem.getTitle());
        hoaProblem.setStatement(huyProblem.getStatement());
        hoaProblem.setConstraints(huyProblem.getConstraints());
        
        // Parse time limit (convert "1s" to 1000ms)
        String timeLimit = huyProblem.getTimeLimit();
        if (timeLimit != null) {
            hoaProblem.setTimeLimitMs(parseTimeLimit(timeLimit));
        } else {
            hoaProblem.setTimeLimitMs(1000); // Default 1s
        }
        
        // Parse memory limit (convert "256MB" to 256)
        String memLimit = huyProblem.getMemoryLimit();
        if (memLimit != null) {
            hoaProblem.setMemoryLimitMb(parseMemoryLimit(memLimit));
        } else {
            hoaProblem.setMemoryLimitMb(256); // Default 256MB
        }
        
        // Set contest type
        try {
            hoaProblem.setContestType(Problem.ContestType.valueOf(type.toUpperCase()));
        } catch (Exception e) {
            hoaProblem.setContestType(Problem.ContestType.IOI); // Default
        }
        
        return hoaProblem;
    }
    
    /**
     * Convert từ model của Hòa sang model của Huy.
     */
    private com.competitive.model.Problem convertToHuyModel(Problem hoaProblem) {
        com.competitive.model.Problem huyProblem = new com.competitive.model.Problem();
        
        huyProblem.setTitle(hoaProblem.getTitle());
        huyProblem.setStatement(hoaProblem.getStatement());
        huyProblem.setConstraints(hoaProblem.getConstraints());
        huyProblem.setTimeLimit(hoaProblem.getTimeLimitMs() + "ms");
        huyProblem.setMemoryLimit(hoaProblem.getMemoryLimitMb() + "MB");
        
        return huyProblem;
    }
    
    /**
     * Parse time limit string to milliseconds.
     * Examples: "1s" → 1000, "1000ms" → 1000, "1.5s" → 1500
     */
    private int parseTimeLimit(String timeLimit) {
        try {
            String normalized = timeLimit.toLowerCase().replaceAll("\\s+", "");
            
            if (normalized.endsWith("ms")) {
                return Integer.parseInt(normalized.substring(0, normalized.length() - 2));
            } else if (normalized.endsWith("s")) {
                double seconds = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return (int) (seconds * 1000);
            } else {
                // Assume milliseconds
                return Integer.parseInt(normalized);
            }
        } catch (Exception e) {
            return 1000; // Default 1s
        }
    }
    
    /**
     * Parse memory limit string to MB.
     * Examples: "256MB" → 256, "256" → 256
     */
    private int parseMemoryLimit(String memLimit) {
        try {
            String normalized = memLimit.toUpperCase().replaceAll("\\s+", "");
            
            if (normalized.endsWith("MB")) {
                return Integer.parseInt(normalized.substring(0, normalized.length() - 2));
            } else {
                return Integer.parseInt(normalized);
            }
        } catch (Exception e) {
            return 256; // Default 256MB
        }
    }
    
    /**
     * Determine test type from note.
     */
    private TestCase.Type determineTestType(String note) {
        if (note == null) return TestCase.Type.MEDIUM;
        
        String lower = note.toLowerCase();
        
        if (lower.contains("edge") || lower.contains("biên") || 
            lower.contains("min") || lower.contains("max")) {
            return TestCase.Type.EDGE;
        } else if (lower.contains("stress") || lower.contains("large") || 
                   lower.contains("lớn")) {
            return TestCase.Type.LARGE;
        } else if (lower.contains("small") || lower.contains("nhỏ")) {
            return TestCase.Type.SMALL;
        } else {
            return TestCase.Type.MEDIUM;
        }
    }
    
    /**
     * Determine test type from description (for verification workflow).
     */
    private TestCase.Type determineTestTypeFromDescription(String description) {
        if (description == null) return TestCase.Type.MEDIUM;
        
        String lower = description.toLowerCase();
        
        if (lower.contains("edge") || lower.contains("biên") || 
            lower.contains("min") || lower.contains("max") ||
            lower.contains("đặc biệt") || lower.contains("special")) {
            return TestCase.Type.EDGE;
        } else if (lower.contains("stress") || lower.contains("large") || 
                   lower.contains("lớn") || lower.contains("big")) {
            return TestCase.Type.LARGE;
        } else if (lower.contains("small") || lower.contains("nhỏ") ||
                   lower.contains("tiny")) {
            return TestCase.Type.SMALL;
        } else {
            return TestCase.Type.MEDIUM;
        }
    }
    
    /**
     * Generate default checker for simple problems.
     */
    private String generateDefaultChecker(Problem problem) {
        return """
public class Checker {
    public static boolean check(String input, String output, String answer) {
        // Default checker: exact match
        if (output == null || answer == null) return false;
        return output.trim().equals(answer.trim());
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java Checker <input_file> <output_file> <answer_file>");
            return;
        }
        
        String input = readFile(args[0]);
        String output = readFile(args[1]);
        String answer = readFile(args[2]);
        
        if (check(input, output, answer)) {
            System.out.println("AC");
        } else {
            System.out.println("WA: Output does not match expected answer");
        }
    }
    
    private static String readFile(String path) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(path)));
    }
}
""";
    }
    
}
