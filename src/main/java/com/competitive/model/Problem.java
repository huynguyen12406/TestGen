package com.competitive.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a competitive programming problem.
 */
public class Problem {
    private String title;
    private String statement;       // full problem text
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String examples;        // sample I/O from problem
    private String contestType;     // IOI, ICPC, CF, etc.
    private String timeLimit;       // e.g. "1s"
    private String memoryLimit;     // e.g. "256MB"
    private List<Testcase> testcases = new ArrayList<>();
    private String checkerCode;     // custom checker (C++ or Java)
    private String acCode;
    private String waCode;
    private String tleCode;
    private String imagePath;       // if problem was loaded from image

    public Problem() {}

    // ---- Getters & Setters ----

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

    public String getExamples() { return examples; }
    public void setExamples(String examples) { this.examples = examples; }

    public String getContestType() { return contestType; }
    public void setContestType(String contestType) { this.contestType = contestType; }

    public String getTimeLimit() { return timeLimit; }
    public void setTimeLimit(String timeLimit) { 
        // Validate time limit format
        if (timeLimit != null && !timeLimit.isBlank()) {
            String normalized = timeLimit.replaceAll("\\s+", "").toLowerCase();
            if (!normalized.matches("\\d+(\\.\\d+)?(ms|s)?")) {
                throw new IllegalArgumentException("Invalid time limit format: " + timeLimit + ". Expected format: '1s', '1000ms', '1.5s'");
            }
        }
        this.timeLimit = timeLimit; 
    }

    public String getMemoryLimit() { return memoryLimit; }
    public void setMemoryLimit(String memoryLimit) { this.memoryLimit = memoryLimit; }

    public List<Testcase> getTestcases() { return testcases; }
    public void setTestcases(List<Testcase> testcases) { this.testcases = testcases; }

    public String getCheckerCode() { return checkerCode; }
    public void setCheckerCode(String checkerCode) { this.checkerCode = checkerCode; }

    public String getAcCode() { return acCode; }
    public void setAcCode(String acCode) { this.acCode = acCode; }

    public String getWaCode() { return waCode; }
    public void setWaCode(String waCode) { this.waCode = waCode; }

    public String getTleCode() { return tleCode; }
    public void setTleCode(String tleCode) { this.tleCode = tleCode; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return title != null ? title : "Untitled Problem";
    }
}
