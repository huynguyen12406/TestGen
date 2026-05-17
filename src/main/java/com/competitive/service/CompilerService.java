package com.competitive.service;

import com.competitive.model.Testcase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * Compiles and runs Java or C++ code against testcases.
 */
public class CompilerService implements AutoCloseable {

    public enum Language { JAVA, CPP }

    private static final long DEFAULT_TIMEOUT_MS = 5000;
    private static final long COMPILE_TIMEOUT_SECONDS = 30;
    private static final long CHECKER_TIMEOUT_SECONDS = 10;
    private final Path workDir;

    public CompilerService() throws IOException {
        workDir = Files.createTempDirectory("competitive_runner_");
        // Register shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    @Override
    public void close() {
        cleanup();
    }

    private void cleanup() {
        try {
            if (Files.exists(workDir)) {
                Files.walk(workDir)
                    .sorted((a, b) -> -a.compareTo(b)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    // -----------------------------------------------------------------------
    // Compile
    // -----------------------------------------------------------------------

    public CompileResult compile(String sourceCode, Language lang, String className) throws IOException {
        if (lang == Language.JAVA) {
            return compileJava(sourceCode, className);
        } else {
            return compileCpp(sourceCode);
        }
    }

    private CompileResult compileJava(String sourceCode, String className) throws IOException {
        // Validate className to prevent command injection
        if (!isValidJavaClassName(className)) {
            return new CompileResult(false, "Invalid class name: " + className, className, Language.JAVA);
        }

        Path srcFile = workDir.resolve(className + ".java");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);

        ProcessBuilder pb = new ProcessBuilder("javac", "-encoding", "UTF-8", srcFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        String output = readStream(proc.getInputStream());
        boolean finished;
        try {
            finished = proc.waitFor(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            return new CompileResult(false, "Compilation interrupted", className, Language.JAVA);
        }

        if (!finished) {
            proc.destroyForcibly();
            return new CompileResult(false, "Compilation timeout after " + COMPILE_TIMEOUT_SECONDS + "s", className, Language.JAVA);
        }

        boolean success = proc.exitValue() == 0;
        return new CompileResult(success, output, className, Language.JAVA);
    }

    private boolean isValidJavaClassName(String className) {
        if (className == null || className.isEmpty()) return false;
        // Allow only alphanumeric and underscore
        return className.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private CompileResult compileCpp(String sourceCode) throws IOException {
        Path srcFile = workDir.resolve("solution.cpp");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
        Path outFile = workDir.resolve("solution_bin");

        // Try g++ first, then cl (MSVC)
        String compiler = findCppCompiler();
        if (compiler == null) {
            return new CompileResult(false, "No C++ compiler found. Install g++ or MSVC.", "solution_bin", Language.CPP);
        }

        ProcessBuilder pb;
        if (compiler.equals("g++")) {
            pb = new ProcessBuilder("g++", "-O2", "-o", outFile.toString(), srcFile.toString());
        } else {
            pb = new ProcessBuilder("cl", "/O2", "/Fe:" + outFile + ".exe", srcFile.toString());
        }
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        String output = readStream(proc.getInputStream());
        boolean finished;
        try {
            finished = proc.waitFor(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            return new CompileResult(false, "Compilation interrupted", outFile.toString(), Language.CPP);
        }

        if (!finished) {
            proc.destroyForcibly();
            return new CompileResult(false, "Compilation timeout after " + COMPILE_TIMEOUT_SECONDS + "s", outFile.toString(), Language.CPP);
        }

        boolean success = proc.exitValue() == 0;
        return new CompileResult(success, output, outFile.toString(), Language.CPP);
    }

    // -----------------------------------------------------------------------
    // Run
    // -----------------------------------------------------------------------

    public RunResult run(CompileResult compiled, String input, long timeLimitMs) throws IOException {
        if (!compiled.isSuccess()) {
            return new RunResult(false, "", "Compilation failed", timeLimitMs, Testcase.Status.CE);
        }

        ProcessBuilder pb;
        if (compiled.getLanguage() == Language.JAVA) {
            pb = new ProcessBuilder("java", "-cp", workDir.toString(), compiled.getArtifact());
        } else {
            String exe = compiled.getArtifact();
            if (!exe.endsWith(".exe") && isWindows()) exe += ".exe";
            pb = new ProcessBuilder(exe);
        }
        pb.directory(workDir.toFile());

        long start = System.currentTimeMillis();
        Process proc = pb.start();

        // Write input
        try (OutputStream os = proc.getOutputStream()) {
            os.write(input.getBytes(StandardCharsets.UTF_8));
        }

        // Read output with timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executor.submit(() -> readStream(proc.getInputStream()));
        Future<String> errorFuture = executor.submit(() -> readStream(proc.getErrorStream()));

        boolean finished;
        try {
            finished = proc.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finished = false;
        }

        long elapsed = System.currentTimeMillis() - start;

        if (!finished) {
            proc.destroyForcibly();
            executor.shutdownNow();
            return new RunResult(false, "", "Time Limit Exceeded", elapsed, Testcase.Status.TLE);
        }

        String stdout = "";
        String stderr = "";
        try {
            stdout = outputFuture.get(2, TimeUnit.SECONDS);
            stderr = errorFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            // ignore
        } finally {
            executor.shutdownNow();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int exitCode = proc.exitValue();
        if (exitCode != 0) {
            return new RunResult(false, stdout, "Runtime Error (exit " + exitCode + "): " + stderr, elapsed, Testcase.Status.RE);
        }

        return new RunResult(true, stdout, stderr, elapsed, Testcase.Status.UNKNOWN);
    }

    // -----------------------------------------------------------------------
    // Judge
    // -----------------------------------------------------------------------

    /**
     * Compare actual vs expected output (token-based, ignoring trailing whitespace).
     */
    public Testcase.Status judge(String expected, String actual) {
        // Handle null and empty cases consistently
        if (expected == null) expected = "";
        if (actual == null) actual = "";
        
        String e = normalizeOutput(expected);
        String a = normalizeOutput(actual);
        return e.equals(a) ? Testcase.Status.AC : Testcase.Status.WA;
    }

    /**
     * Run a custom Java checker.
     * Checker receives: inputFile expectedFile actualFile
     * Prints "AC" or "WA: reason"
     */
    public String runChecker(CompileResult checker, String input, String expected, String actual) throws IOException {
        Path inputFile = workDir.resolve("checker_input.txt");
        Path expectedFile = workDir.resolve("checker_expected.txt");
        Path actualFile = workDir.resolve("checker_actual.txt");

        Files.writeString(inputFile, input, StandardCharsets.UTF_8);
        Files.writeString(expectedFile, expected, StandardCharsets.UTF_8);
        Files.writeString(actualFile, actual, StandardCharsets.UTF_8);

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", workDir.toString(), checker.getArtifact(),
                inputFile.toString(), expectedFile.toString(), actualFile.toString()
        );
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        String output = readStream(proc.getInputStream());
        boolean finished;
        try {
            finished = proc.waitFor(CHECKER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            return "WA: Checker timeout";
        }
        
        if (!finished) {
            proc.destroyForcibly();
            return "WA: Checker timeout after " + CHECKER_TIMEOUT_SECONDS + "s";
        }
        
        return output.trim();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String normalizeOutput(String s) {
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            sb.append(line.stripTrailing()).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    private String readStream(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String findCppCompiler() {
        for (String cmd : new String[]{"g++", "g++.exe"}) {
            try {
                Process p = new ProcessBuilder(cmd, "--version").start();
                p.waitFor(5, TimeUnit.SECONDS);
                if (p.exitValue() == 0) return "g++";
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public Path getWorkDir() { return workDir; }

    // -----------------------------------------------------------------------
    // Result classes
    // -----------------------------------------------------------------------

    public static class CompileResult {
        private final boolean success;
        private final String log;
        private final String artifact;   // class name (Java) or exe path (C++)
        private final Language language;

        public CompileResult(boolean success, String log, String artifact, Language language) {
            this.success = success;
            this.log = log;
            this.artifact = artifact;
            this.language = language;
        }

        public boolean isSuccess() { return success; }
        public String getLog() { return log; }
        public String getArtifact() { return artifact; }
        public Language getLanguage() { return language; }
    }

    public static class RunResult {
        private final boolean success;
        private final String stdout;
        private final String stderr;
        private final long elapsedMs;
        private final Testcase.Status status;

        public RunResult(boolean success, String stdout, String stderr, long elapsedMs, Testcase.Status status) {
            this.success = success;
            this.stdout = stdout;
            this.stderr = stderr;
            this.elapsedMs = elapsedMs;
            this.status = status;
        }

        public boolean isSuccess() { return success; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public long getElapsedMs() { return elapsedMs; }
        public Testcase.Status getStatus() { return status; }
    }
}
