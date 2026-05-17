package com.competitive.model;

/**
 * Represents a single testcase with input, expected output, and run results.
 */
public class Testcase {
    public enum Status { PENDING, AC, WA, TLE, RE, CE, UNKNOWN }

    private static final int MAX_INPUT_SIZE = 10 * 1024 * 1024;  // 10MB
    private static final int MAX_OUTPUT_SIZE = 10 * 1024 * 1024; // 10MB

    private int id;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private Status status = Status.PENDING;
    private long executionTimeMs;
    private String note;            // e.g. "edge case: n=0"
    private boolean isGenerated;    // true = AI-generated, false = manual

    public Testcase() {}

    public Testcase(int id, String input, String expectedOutput) {
        this.id = id;
        setInput(input);
        setExpectedOutput(expectedOutput);
    }

    // ---- Getters & Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInput() { return input; }
    public void setInput(String input) { 
        if (input != null && input.length() > MAX_INPUT_SIZE) {
            throw new IllegalArgumentException("Input size exceeds maximum allowed size of " + MAX_INPUT_SIZE + " bytes");
        }
        this.input = input; 
    }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { 
        if (expectedOutput != null && expectedOutput.length() > MAX_OUTPUT_SIZE) {
            throw new IllegalArgumentException("Expected output size exceeds maximum allowed size of " + MAX_OUTPUT_SIZE + " bytes");
        }
        this.expectedOutput = expectedOutput; 
    }

    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isGenerated() { return isGenerated; }
    public void setGenerated(boolean generated) { isGenerated = generated; }

    @Override
    public String toString() {
        return String.format("TC#%d [%s]", id, status);
    }
}
