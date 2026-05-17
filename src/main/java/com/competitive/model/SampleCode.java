package com.competitive.model;

/**
 * Represents a sample code solution with metadata.
 * Used to store AI-generated code samples (AC, WA, TLE) for testing purposes.
 */
public class SampleCode {
    
    /**
     * Type of code based on expected behavior
     */
    public enum CodeType {
        /** Accepted - Correct solution that passes all test cases */
        AC,
        /** Wrong Answer - Solution with logical errors */
        WA,
        /** Time Limit Exceeded - Correct but too slow solution */
        TLE
    }
    
    /**
     * Programming language of the code
     */
    public enum Language {
        JAVA,
        CPP,
        PYTHON
    }
    
    private CodeType type;
    private Language language;
    private String sourceCode;
    private String explanation;
    
    // ---- Constructors ----
    
    /**
     * Default constructor
     */
    public SampleCode() {
    }
    
    /**
     * Constructor with type, language, and source code
     * 
     * @param type the code type (AC, WA, or TLE)
     * @param language the programming language
     * @param sourceCode the complete source code
     */
    public SampleCode(CodeType type, Language language, String sourceCode) {
        this.type = type;
        this.language = language;
        this.sourceCode = sourceCode;
    }
    
    /**
     * Constructor with all fields
     * 
     * @param type the code type (AC, WA, or TLE)
     * @param language the programming language
     * @param sourceCode the complete source code
     * @param explanation explanation of why this code behaves as specified
     */
    public SampleCode(CodeType type, Language language, String sourceCode, String explanation) {
        this.type = type;
        this.language = language;
        this.sourceCode = sourceCode;
        this.explanation = explanation;
    }
    
    // ---- Getters and Setters ----
    
    public CodeType getType() {
        return type;
    }
    
    public void setType(CodeType type) {
        this.type = type;
    }
    
    public Language getLanguage() {
        return language;
    }
    
    public void setLanguage(Language language) {
        this.language = language;
    }
    
    public String getSourceCode() {
        return sourceCode;
    }
    
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    // ---- Object methods ----
    
    @Override
    public String toString() {
        return String.format("%s Code (%s)", type, language);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SampleCode that = (SampleCode) obj;
        
        if (type != that.type) return false;
        if (language != that.language) return false;
        if (sourceCode != null ? !sourceCode.equals(that.sourceCode) : that.sourceCode != null) return false;
        return explanation != null ? explanation.equals(that.explanation) : that.explanation == null;
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (sourceCode != null ? sourceCode.hashCode() : 0);
        result = 31 * result + (explanation != null ? explanation.hashCode() : 0);
        return result;
    }
}
