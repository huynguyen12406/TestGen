package com.competitive.ai;

import com.competitive.model.Problem;
import com.competitive.model.Testcase;

/**
 * Test AIService end-to-end workflow.
 * 
 * Test đơn giản với đề bài "Sum of Two Numbers".
 * 
 * @author Nguyễn Thành Huy
 */
public class TestAIService {
    
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("Testing AIService End-to-End Workflow");
        System.out.println("============================================================\n");
        
        // API key - set via environment variable or replace here
        String apiKey = System.getenv("GEMINI_API_KEY");
        
        // Sample problem text
        String problemText = """
Bài toán: Tổng hai số

Cho hai số nguyên a và b. Hãy tính tổng của chúng.

Input:
Một dòng chứa hai số nguyên a và b (1 ≤ a, b ≤ 1000)

Output:
In ra một số nguyên duy nhất là tổng a + b

Ví dụ:
Input: 2 3
Output: 5

Input: 10 20
Output: 30

Giới hạn:
- Time limit: 1s
- Memory limit: 256MB
""";
        
        try {
            // Create AIService
            System.out.println("Step 1: Creating AIService with Gemini provider...");
            AIService service = new AIService(AIConnector.Provider.GEMINI, apiKey);
            System.out.println("✅ AIService created\n");
            
            // Test 1: Analyze problem only
            System.out.println("Test 1: Analyze problem (no generation)");
            System.out.println("------------------------------------------------------------");
            long start1 = System.currentTimeMillis();
            
            Problem problem = service.analyzeProblem(problemText);
            
            long elapsed1 = System.currentTimeMillis() - start1;
            
            System.out.println("✅ SUCCESS!");
            System.out.println("Time: " + elapsed1 + "ms");
            System.out.println("\nProblem Info:");
            System.out.println("  Title: " + problem.getTitle());
            System.out.println("  Input Format: " + problem.getInputFormat());
            System.out.println("  Output Format: " + problem.getOutputFormat());
            System.out.println("  Constraints: " + problem.getConstraints());
            System.out.println("  Time Limit: " + problem.getTimeLimit());
            System.out.println("  Memory Limit: " + problem.getMemoryLimit());
            System.out.println();
            
            // Test 2: Generate test cases
            System.out.println("Test 2: Generate test cases");
            System.out.println("------------------------------------------------------------");
            long start2 = System.currentTimeMillis();
            
            service.generateTestcases(problem, 5);
            
            long elapsed2 = System.currentTimeMillis() - start2;
            
            System.out.println("✅ SUCCESS!");
            System.out.println("Time: " + elapsed2 + "ms");
            System.out.println("Generated " + problem.getTestcases().size() + " test cases:");
            
            for (Testcase tc : problem.getTestcases()) {
                System.out.println("  TC#" + tc.getId() + ":");
                System.out.println("    Input: " + tc.getInput());
                System.out.println("    Expected: " + tc.getExpectedOutput());
                if (tc.getNote() != null) {
                    System.out.println("    Note: " + tc.getNote());
                }
            }
            System.out.println();
            
            // Test 3: Generate sample code (AC, WA, TLE)
            System.out.println("Test 3: Generate sample code (AC, WA, TLE)");
            System.out.println("------------------------------------------------------------");
            long start3 = System.currentTimeMillis();
            
            service.generateSampleCode(problem, com.competitive.model.SampleCode.Language.JAVA);
            
            long elapsed3 = System.currentTimeMillis() - start3;
            
            System.out.println("✅ SUCCESS!");
            System.out.println("Time: " + elapsed3 + "ms");
            System.out.println("\nGenerated code:");
            
            if (problem.getAcCode() != null) {
                System.out.println("  ✅ AC Code: " + problem.getAcCode().length() + " chars");
                System.out.println("     First 300 chars: " + 
                    problem.getAcCode().substring(0, Math.min(300, problem.getAcCode().length())));
            }
            
            if (problem.getWaCode() != null) {
                System.out.println("  ✅ WA Code: " + problem.getWaCode().length() + " chars");
            }
            
            if (problem.getTleCode() != null) {
                System.out.println("  ✅ TLE Code: " + problem.getTleCode().length() + " chars");
            }
            System.out.println();
            
            // Summary
            System.out.println("============================================================");
            System.out.println("All tests completed successfully!");
            System.out.println("============================================================");
            System.out.println("Total time: " + (elapsed1 + elapsed2 + elapsed3) + "ms");
            System.out.println("\n✅ AIService is working correctly!");
            System.out.println("\nNext steps:");
            System.out.println("1. Test full workflow: service.analyzeAndGenerate(problemText)");
            System.out.println("2. Test with image input: service.analyzeAndGenerate(imageFile)");
            System.out.println("3. Integrate with GUI (Hòa's code)");
            System.out.println("4. Integrate with Compiler (Trung's code)");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERROR!");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Check API key is valid");
            System.err.println("2. Check internet connection");
            System.err.println("3. Check Gemini API quota");
            System.err.println("4. Run Project → Clean in Eclipse");
        }
    }
}
