package com.competitive.service;

import com.competitive.model.Problem;
import com.competitive.model.Testcase;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Orchestrates compiling a solution and running all testcases against it.
 */
public class TestcaseRunner {

    private final CompilerService compiler;

    public TestcaseRunner(CompilerService compiler) {
        this.compiler = compiler;
    }

    /**
     * Run all testcases for a given solution.
     *
     * @param sourceCode   the solution source
     * @param language     JAVA or CPP
     * @param className    Java class name (ignored for C++)
     * @param problem      the problem (for time limit and checker)
     * @param checkerCompiled compiled checker (null = use token comparison)
     * @param onProgress   callback(testcaseIndex, updatedTestcase) for UI updates
     * @return summary string
     */
    public String runAll(
            String sourceCode,
            CompilerService.Language language,
            String className,
            Problem problem,
            CompilerService.CompileResult checkerCompiled,
            BiConsumer<Integer, Testcase> onProgress
    ) {
        List<Testcase> testcases = problem.getTestcases();
        if (testcases.isEmpty()) return "No testcases to run.";

        // Parse time limit
        long timeLimitMs = parseTimeLimit(problem.getTimeLimit());

        // Compile solution
        CompilerService.CompileResult compiled;
        try {
            compiled = compiler.compile(sourceCode, language, className);
        } catch (IOException e) {
            return "Compilation error: " + e.getMessage();
        }

        if (!compiled.isSuccess()) {
            // Mark all as CE
            for (int i = 0; i < testcases.size(); i++) {
                Testcase tc = testcases.get(i);
                tc.setStatus(Testcase.Status.CE);
                tc.setActualOutput("Compilation Error:\n" + compiled.getLog());
                if (onProgress != null) onProgress.accept(i, tc);
            }
            return "Compilation Failed:\n" + compiled.getLog();
        }

        int ac = 0, wa = 0, tle = 0, re = 0, ce = 0;

        for (int i = 0; i < testcases.size(); i++) {
            Testcase tc = testcases.get(i);
            try {
                CompilerService.RunResult result = compiler.run(compiled, tc.getInput(), timeLimitMs);
                tc.setExecutionTimeMs(result.getElapsedMs());

                if (result.getStatus() == Testcase.Status.TLE) {
                    tc.setStatus(Testcase.Status.TLE);
                    tc.setActualOutput("TLE after " + result.getElapsedMs() + "ms");
                    tle++;
                } else if (result.getStatus() == Testcase.Status.RE) {
                    tc.setStatus(Testcase.Status.RE);
                    tc.setActualOutput(result.getStderr());
                    re++;
                } else {
                    tc.setActualOutput(result.getStdout());

                    // Judge
                    Testcase.Status verdict;
                    if (checkerCompiled != null && checkerCompiled.isSuccess()) {
                        String checkerOut = compiler.runChecker(
                                checkerCompiled,
                                tc.getInput(),
                                tc.getExpectedOutput() != null ? tc.getExpectedOutput() : "",
                                result.getStdout()
                        );
                        verdict = checkerOut.startsWith("AC") ? Testcase.Status.AC : Testcase.Status.WA;
                        if (verdict == Testcase.Status.WA) {
                            tc.setActualOutput(result.getStdout() + "\n[Checker: " + checkerOut + "]");
                        }
                    } else {
                        verdict = compiler.judge(tc.getExpectedOutput(), result.getStdout());
                    }

                    tc.setStatus(verdict);
                    if (verdict == Testcase.Status.AC) {
                        ac++;
                    } else {
                        wa++;
                    }
                }
            } catch (IOException e) {
                tc.setStatus(Testcase.Status.RE);
                tc.setActualOutput("Error: " + e.getMessage());
                re++;
            }

            if (onProgress != null) onProgress.accept(i, tc);
        }

        int total = testcases.size();
        return String.format("Results: %d/%d AC | %d WA | %d TLE | %d RE | %d CE",
                ac, total, wa, tle, re, ce);
    }

    private long parseTimeLimit(String tl) {
        if (tl == null || tl.isBlank()) return 5000;
        
        // Remove all whitespace and convert to lowercase
        tl = tl.replaceAll("\\s+", "").toLowerCase();
        
        try {
            if (tl.endsWith("ms")) {
                return Long.parseLong(tl.substring(0, tl.length() - 2));
            }
            if (tl.endsWith("s")) {
                String numStr = tl.substring(0, tl.length() - 1);
                return (long)(Double.parseDouble(numStr) * 1000);
            }
            // No unit, assume seconds
            return (long)(Double.parseDouble(tl) * 1000);
        } catch (NumberFormatException e) {
            // Invalid format, return default
            return 5000;
        }
    }
}
