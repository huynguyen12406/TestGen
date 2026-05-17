package vn.testgen.model;

import java.util.List;
import java.util.ArrayList;

/** A generated test case */
public class TestCase {
    public enum Type { SMALL, MEDIUM, LARGE, EDGE, STRESS, CUSTOM }

    private int    index;
    private String input;
    private String expectedOutput;   // from checker / AC solution
    private Type   type;
    private long   generationTimeMs;
    private boolean verified;

    public TestCase(int index, String input, Type type) {
        this.index = index;
        this.input = input;
        this.type  = type;
    }

    public int     getIndex()                   { return index; }
    public String  getInput()                   { return input; }
    public void    setInput(String s)           { this.input = s; }
    public String  getExpectedOutput()          { return expectedOutput; }
    public void    setExpectedOutput(String s)  { this.expectedOutput = s; }
    public Type    getType()                    { return type; }
    public boolean isVerified()                 { return verified; }
    public void    setVerified(boolean v)       { this.verified = v; }
    public long    getGenerationTimeMs()        { return generationTimeMs; }
    public void    setGenerationTimeMs(long ms) { this.generationTimeMs = ms; }

    @Override
    public String toString() {
        return String.format("Test #%d [%s]%s", index, type, verified ? " ✓" : "");
    }
}

/** Result of running a solution on a test case */
class RunResult {
    public String  verdict;      // AC / WA / TLE / MLE / RE / CE
    public String  actualOutput;
    public long    runtimeMs;
    public long    memoryKb;
    public String  solutionType; // "AC_solution" / "WA_solution" / "TLE_solution"

    public RunResult(String verdict, long runtimeMs, long memoryKb) {
        this.verdict   = verdict;
        this.runtimeMs = runtimeMs;
        this.memoryKb  = memoryKb;
    }
}

/** Full evaluation report for a problem */
class EvaluationReport {
    private String problemTitle;
    private int    totalTests;
    private int    passedAC;
    private int    foundWA;
    private int    foundTLE;
    private List<String> log = new ArrayList<>();
    private long   evaluationTimeMs;

    public EvaluationReport(String title) { this.problemTitle = title; }

    public void addLog(String line)  { log.add(line); }
    public List<String> getLog()     { return log; }
    public String getProblemTitle()  { return problemTitle; }
    public int getTotalTests()       { return totalTests; }
    public void setTotalTests(int n) { this.totalTests = n; }
    public int getPassedAC()         { return passedAC; }
    public void setPassedAC(int n)   { this.passedAC = n; }
    public int getFoundWA()          { return foundWA; }
    public void setFoundWA(int n)    { this.foundWA = n; }
    public int getFoundTLE()         { return foundTLE; }
    public void setFoundTLE(int n)   { this.foundTLE = n; }
    public long getEvaluationTimeMs(){ return evaluationTimeMs; }
    public void setEvaluationTimeMs(long ms) { this.evaluationTimeMs = ms; }
}
