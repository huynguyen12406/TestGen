package com.competitive.bugfix;

import com.competitive.ai.AIService;
import com.competitive.ai.AIConnector;
import com.competitive.model.Problem;
import com.competitive.model.SampleCode;
import com.competitive.service.CompilerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Testcase Verification Fix
 * 
 * Property 1: Bug Condition - AC Code Receives WA Due to Incorrect AI-Generated Expected Outputs
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * This test encodes the expected behavior - it will validate the fix when it passes after implementation.
 * 
 * Goal: Surface counterexamples that demonstrate the bug exists.
 * 
 * Test Strategy:
 * 1. Create a simple problem (Longest Increasing Subsequence)
 * 2. Generate test cases using UNFIXED workflow (direct AIService.generateTestcases())
 * 3. Observe that AI generates incorrect expected outputs
 * 4. Generate AC code for the problem
 * 5. Run AC code against the AI-generated test cases
 * 6. Assert that AC code receives AC verdict (will FAIL because expected outputs are wrong)
 * 
 * Expected Outcome: Test FAILS (this is correct - it proves the bug exists)
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5
 */
public class TestcaseVerificationBugTest {
    
    private AIService aiService;
    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");
    
    @BeforeEach
    public void setUp() {
        // Initialize AIService with Groq provider
        aiService = new AIService(AIConnector.Provider.GROQ, GROQ_API_KEY);
    }
    
    @Test
    @DisplayName("Bug Condition: AC code receives WA due to incorrect AI-generated expected outputs")
    public void testBugCondition_ACCodeReceivesWA_DueToIncorrectExpectedOutputs() throws Exception {
        // Step 1: Create a simple problem (Longest Increasing Subsequence)
        Problem problem = createLongestIncreasingSubsequenceProblem();
        
        // Step 2: Generate test cases using UNFIXED workflow (direct AI calculation)
        // This is the current buggy behavior where AI calculates expected outputs without verification
        System.out.println("[TEST] Generating test cases using UNFIXED workflow (AI calculates expected outputs)...");
        aiService.generateTestcases(problem, 5);
        
        List<com.competitive.model.Testcase> testcases = problem.getTestcases();
        assertNotNull(testcases, "Test cases should be generated");
        assertFalse(testcases.isEmpty(), "Test cases should not be empty");
        
        System.out.println("[TEST] Generated " + testcases.size() + " test cases");
        
        // Print test cases to observe AI-generated expected outputs
        for (int i = 0; i < testcases.size(); i++) {
            com.competitive.model.Testcase tc = testcases.get(i);
            System.out.println("[TEST] Test case #" + (i + 1) + ":");
            System.out.println("  Input: " + tc.getInput().replace("\n", "\\n"));
            System.out.println("  Expected Output (AI-calculated): " + tc.getExpectedOutput());
        }
        
        // Step 3: Generate AC code for the problem
        System.out.println("[TEST] Generating AC code...");
        aiService.generateSampleCode(problem, SampleCode.Language.CPP);
        
        String acCode = problem.getAcCode();
        assertNotNull(acCode, "AC code should be generated");
        assertFalse(acCode.isEmpty(), "AC code should not be empty");
        
        System.out.println("[TEST] AC code generated (" + acCode.length() + " chars)");
        
        // Step 4: Run AC code against AI-generated test cases
        System.out.println("[TEST] Running AC code against AI-generated test cases...");
        
        int acCount = 0;
        int waCount = 0;
        int otherCount = 0;
        
        try (CompilerService compiler = new CompilerService()) {
            // Compile AC code
            CompilerService.CompileResult compileResult = compiler.compile(
                acCode, 
                CompilerService.Language.CPP, 
                "solution"
            );
            
            assertTrue(compileResult.isSuccess(), 
                "AC code should compile successfully. Compilation log: " + compileResult.getLog());
            
            System.out.println("[TEST] AC code compiled successfully");
            
            // Run each test case
            for (int i = 0; i < testcases.size(); i++) {
                com.competitive.model.Testcase tc = testcases.get(i);
                
                CompilerService.RunResult runResult = compiler.run(
                    compileResult, 
                    tc.getInput(), 
                    getTimeLimitMs(problem)
                );
                
                String actualOutput = runResult.getStdout();
                String expectedOutput = tc.getExpectedOutput();
                
                // Judge the result
                com.competitive.model.Testcase.Status verdict = compiler.judge(expectedOutput, actualOutput);
                
                System.out.println("[TEST] Test case #" + (i + 1) + " verdict: " + verdict);
                System.out.println("  Expected: " + expectedOutput.trim());
                System.out.println("  Actual:   " + actualOutput.trim());
                
                if (verdict == com.competitive.model.Testcase.Status.AC) {
                    acCount++;
                } else if (verdict == com.competitive.model.Testcase.Status.WA) {
                    waCount++;
                    System.out.println("  ❌ WA detected! AC code produced correct output but doesn't match AI-calculated expected output");
                } else {
                    otherCount++;
                }
            }
        }
        
        System.out.println("[TEST] Results: AC=" + acCount + ", WA=" + waCount + ", Other=" + otherCount);
        
        // Step 5: Assert that AC code receives AC verdict on all test cases
        // This assertion will FAIL on unfixed code because AI-generated expected outputs are incorrect
        // When the bug is fixed, this assertion will PASS because expected outputs will be verified
        assertEquals(testcases.size(), acCount, 
            "AC code should receive AC verdict on all test cases. " +
            "Bug detected: " + waCount + " test cases have incorrect expected outputs. " +
            "This proves the bug exists - AI-calculated expected outputs don't match AC code output.");
    }
    
    /**
     * Create a simple Longest Increasing Subsequence problem for testing.
     * This is a well-known problem where we can easily verify correctness.
     */
    private Problem createLongestIncreasingSubsequenceProblem() {
        Problem problem = new Problem();
        
        problem.setTitle("Dãy con tăng dài nhất");
        
        problem.setStatement(
            "Cho dãy số nguyên A gồm N phần tử. " +
            "Tìm độ dài của dãy con tăng dài nhất.\n\n" +
            "Dãy con tăng là dãy các phần tử a[i1], a[i2], ..., a[ik] " +
            "sao cho i1 < i2 < ... < ik và a[i1] < a[i2] < ... < a[ik].\n\n" +
            "Input:\n" +
            "- Dòng đầu tiên chứa số nguyên N (1 ≤ N ≤ 1000)\n" +
            "- Dòng thứ hai chứa N số nguyên a[i] (-10^9 ≤ a[i] ≤ 10^9)\n\n" +
            "Output:\n" +
            "- In ra độ dài của dãy con tăng dài nhất\n\n" +
            "Ví dụ:\n" +
            "Input:\n" +
            "5\n" +
            "1 2 3 4 5\n" +
            "Output:\n" +
            "5"
        );
        
        problem.setConstraints("1 ≤ N ≤ 1000, -10^9 ≤ a[i] ≤ 10^9");
        problem.setTimeLimit("1000ms");
        problem.setMemoryLimit("256MB");
        
        return problem;
    }
    
    /**
     * Helper method to get time limit in milliseconds.
     */
    private int getTimeLimitMs(Problem problem) {
        String timeLimit = problem.getTimeLimit();
        if (timeLimit == null) return 1000;
        
        try {
            String normalized = timeLimit.toLowerCase().replaceAll("\\s+", "");
            
            if (normalized.endsWith("ms")) {
                return Integer.parseInt(normalized.substring(0, normalized.length() - 2));
            } else if (normalized.endsWith("s")) {
                double seconds = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return (int) (seconds * 1000);
            } else {
                return Integer.parseInt(normalized);
            }
        } catch (Exception e) {
            return 1000; // Default 1s
        }
    }
}
