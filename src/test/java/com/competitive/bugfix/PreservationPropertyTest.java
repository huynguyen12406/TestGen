package com.competitive.bugfix;

import com.competitive.ai.AIService;
import com.competitive.ai.AIConnector;
import com.competitive.model.Problem;
import com.competitive.model.SampleCode;
import com.competitive.model.Testcase;
import com.competitive.service.CompilerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preservation Property Tests for Testcase Verification Fix
 * 
 * Property 2: Preservation - Existing Workflows Unchanged
 * 
 * IMPORTANT: Follow observation-first methodology.
 * These tests observe behavior on UNFIXED code for non-buggy inputs.
 * 
 * Expected Outcome: Tests PASS (this confirms baseline behavior to preserve)
 * 
 * Test Coverage:
 * - Test 1: Direct AIService.generateTestcases() calls continue to work
 * - Test 2: CompilerService compilation and execution behavior remains unchanged
 * - Test 3: normalizeOutput() comparison logic continues to use same whitespace normalization rules
 * - Test 4: AIService generation of AC, WA, TLE code continues to work as before
 * - Test 5: API key and provider configuration continues to work with both Groq and Gemini
 * - Test 6: Compilation error (CE) and runtime error (RE) verdict handling remains unchanged
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7
 */
public class PreservationPropertyTest {
    
    private AIService aiService;
    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
    
    @BeforeEach
    public void setUp() {
        // Initialize AIService with Groq provider
        aiService = new AIService(AIConnector.Provider.GROQ, GROQ_API_KEY);
    }
    
    @Test
    @DisplayName("Preservation Test 1: Direct AIService.generateTestcases() continues to work")
    public void testPreservation_DirectAIServiceGenerateTestcases_ContinuesToWork() throws Exception {
        // Create a simple problem
        Problem problem = createSimpleProblem();
        
        // Call AIService.generateTestcases() directly (bypassing BackendService)
        // This is the existing workflow that should remain unchanged
        System.out.println("[PRESERVATION] Testing direct AIService.generateTestcases()...");
        
        aiService.generateTestcases(problem, 3);
        
        // Verify test cases were generated
        List<Testcase> testcases = problem.getTestcases();
        assertNotNull(testcases, "Test cases should be generated");
        assertEquals(3, testcases.size(), "Should generate 3 test cases");
        
        // Verify each test case has input and expected output (AI-calculated)
        for (Testcase tc : testcases) {
            assertNotNull(tc.getInput(), "Test case should have input");
            assertFalse(tc.getInput().isEmpty(), "Test case input should not be empty");
            assertNotNull(tc.getExpectedOutput(), "Test case should have expected output (AI-calculated)");
            assertFalse(tc.getExpectedOutput().isEmpty(), "Test case expected output should not be empty");
        }
        
        System.out.println("[PRESERVATION] ✓ Direct AIService.generateTestcases() works correctly");
    }
    
    @Test
    @DisplayName("Preservation Test 2: CompilerService compilation and execution behavior unchanged")
    public void testPreservation_CompilerService_BehaviorUnchanged() throws Exception {
        // Test C++ compilation and execution
        String cppCode = """
            #include <iostream>
            using namespace std;
            int main() {
                int a, b;
                cin >> a >> b;
                cout << (a + b) << endl;
                return 0;
            }
            """;
        
        System.out.println("[PRESERVATION] Testing CompilerService compilation and execution...");
        
        try (CompilerService compiler = new CompilerService()) {
            // Compile C++ code
            CompilerService.CompileResult compileResult = compiler.compile(
                cppCode,
                CompilerService.Language.CPP,
                "solution"
            );
            
            assertTrue(compileResult.isSuccess(), "C++ code should compile successfully");
            assertEquals(CompilerService.Language.CPP, compileResult.getLanguage(), 
                "Language should be CPP");
            
            // Run with test input
            String input = "5 3\n";
            CompilerService.RunResult runResult = compiler.run(compileResult, input, 1000);
            
            assertTrue(runResult.isSuccess(), "Code should run successfully");
            assertEquals("8\n", runResult.getStdout(), "Output should be 8");
            assertEquals(Testcase.Status.UNKNOWN, runResult.getStatus(), 
                "Status should be UNKNOWN (not judged yet)");
            
            System.out.println("[PRESERVATION] ✓ CompilerService works correctly");
        }
    }
    
    @Test
    @DisplayName("Preservation Test 3: normalizeOutput() comparison logic unchanged")
    public void testPreservation_NormalizeOutput_LogicUnchanged() throws Exception {
        System.out.println("[PRESERVATION] Testing normalizeOutput() comparison logic...");
        
        try (CompilerService compiler = new CompilerService()) {
            // Test case 1: Exact match
            Testcase.Status result1 = compiler.judge("5\n", "5\n");
            assertEquals(Testcase.Status.AC, result1, "Exact match should be AC");
            
            // Test case 2: Trailing whitespace ignored
            Testcase.Status result2 = compiler.judge("5\n", "5  \n");
            assertEquals(Testcase.Status.AC, result2, "Trailing whitespace should be ignored");
            
            // Test case 3: Different output
            Testcase.Status result3 = compiler.judge("5\n", "6\n");
            assertEquals(Testcase.Status.WA, result3, "Different output should be WA");
            
            // Test case 4: Multiple lines with trailing whitespace
            Testcase.Status result4 = compiler.judge("1\n2\n3\n", "1  \n2  \n3  \n");
            assertEquals(Testcase.Status.AC, result4, 
                "Multiple lines with trailing whitespace should be AC");
            
            System.out.println("[PRESERVATION] ✓ normalizeOutput() logic works correctly");
        }
    }
    
    @Test
    @DisplayName("Preservation Test 4: AIService generation of AC, WA, TLE code continues to work")
    public void testPreservation_AIServiceCodeGeneration_ContinuesToWork() throws Exception {
        // Create a simple problem
        Problem problem = createSimpleProblem();
        
        System.out.println("[PRESERVATION] Testing AIService code generation (AC, WA, TLE)...");
        
        // Generate sample code (AC, WA, TLE)
        aiService.generateSampleCode(problem, SampleCode.Language.CPP);
        
        // Verify AC code was generated
        assertNotNull(problem.getAcCode(), "AC code should be generated");
        assertFalse(problem.getAcCode().isEmpty(), "AC code should not be empty");
        assertTrue(problem.getAcCode().contains("#include"), "AC code should be C++");
        
        // Verify WA code was generated
        assertNotNull(problem.getWaCode(), "WA code should be generated");
        assertFalse(problem.getWaCode().isEmpty(), "WA code should not be empty");
        assertTrue(problem.getWaCode().contains("#include"), "WA code should be C++");
        
        // Verify TLE code was generated
        assertNotNull(problem.getTleCode(), "TLE code should be generated");
        assertFalse(problem.getTleCode().isEmpty(), "TLE code should not be empty");
        assertTrue(problem.getTleCode().contains("#include"), "TLE code should be C++");
        
        System.out.println("[PRESERVATION] ✓ AIService code generation works correctly");
    }
    
    @Test
    @DisplayName("Preservation Test 5: API key and provider configuration works with Groq and Gemini")
    public void testPreservation_APIConfiguration_WorksWithBothProviders() throws Exception {
        System.out.println("[PRESERVATION] Testing API configuration with Groq and Gemini...");
        
        // Test Groq provider
        AIService groqService = new AIService(AIConnector.Provider.GROQ, GROQ_API_KEY);
        assertEquals(AIConnector.Provider.GROQ, groqService.getProvider(), 
            "Provider should be GROQ");
        
        // Test Gemini provider
        AIService geminiService = new AIService(AIConnector.Provider.GEMINI, GEMINI_API_KEY);
        assertEquals(AIConnector.Provider.GEMINI, geminiService.getProvider(), 
            "Provider should be GEMINI");
        
        // Test provider switching
        groqService.setProvider(AIConnector.Provider.GEMINI);
        assertEquals(AIConnector.Provider.GEMINI, groqService.getProvider(), 
            "Provider should switch to GEMINI");
        
        // Test API key setting
        groqService.setApiKey("test-key");
        // No exception should be thrown
        
        System.out.println("[PRESERVATION] ✓ API configuration works correctly");
    }
    
    @Test
    @DisplayName("Preservation Test 6: CE and RE verdict handling remains unchanged")
    public void testPreservation_CEAndREVerdictHandling_Unchanged() throws Exception {
        System.out.println("[PRESERVATION] Testing CE and RE verdict handling...");
        
        try (CompilerService compiler = new CompilerService()) {
            // Test CE (Compilation Error)
            String invalidCode = """
                #include <iostream>
                int main() {
                    this is invalid syntax
                    return 0;
                }
                """;
            
            CompilerService.CompileResult ceResult = compiler.compile(
                invalidCode,
                CompilerService.Language.CPP,
                "solution"
            );
            
            assertFalse(ceResult.isSuccess(), "Invalid code should fail to compile");
            assertNotNull(ceResult.getLog(), "Compilation log should be present");
            
            // Test RE (Runtime Error)
            String reCode = """
                #include <iostream>
                using namespace std;
                int main() {
                    int* p = nullptr;
                    cout << *p << endl;  // Dereference null pointer
                    return 0;
                }
                """;
            
            CompilerService.CompileResult compileResult = compiler.compile(
                reCode,
                CompilerService.Language.CPP,
                "solution"
            );
            
            if (compileResult.isSuccess()) {
                CompilerService.RunResult runResult = compiler.run(compileResult, "", 1000);
                
                // Should get RE or non-zero exit code
                assertFalse(runResult.isSuccess() && runResult.getStatus() == Testcase.Status.UNKNOWN,
                    "Code with null pointer dereference should fail");
            }
            
            System.out.println("[PRESERVATION] ✓ CE and RE verdict handling works correctly");
        }
    }
    
    /**
     * Create a simple problem for testing.
     */
    private Problem createSimpleProblem() {
        Problem problem = new Problem();
        
        problem.setTitle("Tổng hai số");
        problem.setStatement(
            "Cho hai số nguyên a và b. Tính tổng a + b.\n\n" +
            "Input: Hai số nguyên a, b (-10^9 ≤ a, b ≤ 10^9)\n" +
            "Output: In ra tổng a + b"
        );
        problem.setConstraints("-10^9 ≤ a, b ≤ 10^9");
        problem.setTimeLimit("1000ms");
        problem.setMemoryLimit("256MB");
        
        return problem;
    }
}
