package vn.testgen.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/** Represents a parsed competitive programming problem */
public class Problem {
    public enum ContestType { IOI, ICPC, CF, CUSTOM }

    private String title;
    private String statement;          // raw problem text
    private String inputSpec;
    private String outputSpec;
    private String constraints;
    private int timeLimitMs;           // default 1000
    private int memoryLimitMb;         // default 256
    private ContestType contestType;
    private String imagePath;          // if uploaded as image
    private LocalDateTime createdAt;
    private List<SampleTest> samples;

    public Problem() {
        timeLimitMs   = 1000;
        memoryLimitMb = 256;
        contestType   = ContestType.ICPC;
        createdAt     = LocalDateTime.now();
        samples       = new ArrayList<>();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public String getTitle()                    { return title; }
    public void   setTitle(String t)            { this.title = t; }
    public String getStatement()                { return statement; }
    public void   setStatement(String s)        { this.statement = s; }
    public String getInputSpec()                { return inputSpec; }
    public void   setInputSpec(String s)        { this.inputSpec = s; }
    public String getOutputSpec()               { return outputSpec; }
    public void   setOutputSpec(String s)       { this.outputSpec = s; }
    public String getConstraints()              { return constraints; }
    public void   setConstraints(String s)      { this.constraints = s; }
    public int    getTimeLimitMs()              { return timeLimitMs; }
    public void   setTimeLimitMs(int t)         { this.timeLimitMs = t; }
    public int    getMemoryLimitMb()            { return memoryLimitMb; }
    public void   setMemoryLimitMb(int m)       { this.memoryLimitMb = m; }
    public ContestType getContestType()         { return contestType; }
    public void   setContestType(ContestType c) { this.contestType = c; }
    public String getImagePath()                { return imagePath; }
    public void   setImagePath(String p)        { this.imagePath = p; }
    public List<SampleTest> getSamples()        { return samples; }
    public void   addSample(SampleTest s)       { samples.add(s); }
    public LocalDateTime getCreatedAt()         { return createdAt; }
}

/** A single test case with input and expected output */
class SampleTest {
    public String input;
    public String expectedOutput;
    public SampleTest(String in, String out) { input = in; expectedOutput = out; }
}
