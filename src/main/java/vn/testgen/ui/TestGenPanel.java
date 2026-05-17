package vn.testgen.ui;

import vn.testgen.model.Problem;
import vn.testgen.model.TestCase;
import vn.testgen.backend.BackendService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel 2: Test Case Generation
 * Shows options for generating tests, a live generation log, and the test list.
 */
public class TestGenPanel extends JPanel {

    private Problem currentProblem;

    // Config controls
    private JSpinner   testCountSpinner;
    private JCheckBox  cbSmall, cbMedium, cbLarge, cbEdge, cbStress;
    private JCheckBox  cbGenChecker, cbGenAC, cbGenWA, cbGenTLE;
    private JComboBox<String> checkerTypeCombo;

    // Live log
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    // Test table
    private DefaultTableModel tableModel;
    private JTable testTable;

    // Buttons
    private JButton generateBtn;
    private JButton stopBtn;
    private JButton clearLogBtn;

    // Result tests
    private List<TestCase> generatedTests = new ArrayList<>();

    // Callback: tests ready
    private Consumer<List<TestCase>> onTestsGenerated;

    private SwingWorker<?, ?> activeWorker;

    public TestGenPanel(Consumer<List<TestCase>> onTestsGenerated) {
        this.onTestsGenerated = onTestsGenerated;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARKEST);
        buildUI();
    }

    public void setProblem(Problem p) {
        this.currentProblem = p;
        if (p != null) {
            appendLog("[INFO] Đề bài đã được tải: " + p.getTitle());
            appendLog("[INFO] Giới hạn: " + p.getTimeLimitMs() + "ms / " + p.getMemoryLimitMb() + "MB");
            generateBtn.setEnabled(true);
        }
    }

    private void buildUI() {
        // ── Top toolbar ────────────────────────────────────────────────────
        JPanel toolbar = buildToolbar();
        add(toolbar, BorderLayout.NORTH);

        // ── Center: left config + right log+table ─────────────────────────
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setBackground(Theme.BG_DARKEST);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(4);
        mainSplit.setDividerLocation(260);
        mainSplit.setResizeWeight(0.0);

        mainSplit.setLeftComponent(buildConfigPanel());
        mainSplit.setRightComponent(buildRightPanel());

        add(mainSplit, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tb.setBackground(Theme.BG_PANEL);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));

        JLabel title = new JLabel("⚡  SINH TEST CASES");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_CYAN);
        tb.add(title);

        return tb;
    }

    private JPanel buildConfigPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JPanel card = Components.card("Cấu hình sinh test");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 4, 4); // Tăng top margin từ 4 lên 8
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Count
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(cfgLabel("Số lượng test:"), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 4, 8, 4); // Thêm bottom margin cho spinner
        testCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 200, 1));
        styleSpinner(testCountSpinner);
        card.add(testCountSpinner, gbc);

        // Test types
        gbc.insets = new Insets(4, 4, 4, 4); // Reset insets
        gbc.gridy = 2;
        card.add(Components.separator(), gbc);
        gbc.gridy = 3;
        card.add(cfgLabel("Loại test:"), gbc);

        cbSmall  = darkCheck("Small (n nhỏ)", true);
        cbMedium = darkCheck("Medium", true);
        cbLarge  = darkCheck("Large (n lớn)", true);
        cbEdge   = darkCheck("Edge cases", true);
        cbStress = darkCheck("Stress test", false);

        for (JCheckBox cb : new JCheckBox[]{cbSmall, cbMedium, cbLarge, cbEdge, cbStress}) {
            gbc.gridy++;
            card.add(cb, gbc);
        }

        // Checker
        gbc.gridy++;
        card.add(Components.separator(), gbc);
        gbc.gridy++;
        card.add(cfgLabel("Checker:"), gbc);
        gbc.gridy++;
        checkerTypeCombo = Components.darkCombo(new String[]{
            "Token-based (mặc định)", "Custom checker (AI)", "Special judge"});
        card.add(checkerTypeCombo, gbc);
        gbc.gridy++;
        cbGenChecker = darkCheck("AI sinh checker", false);
        card.add(cbGenChecker, gbc);

        // Solution generation
        gbc.gridy++;
        card.add(Components.separator(), gbc);
        gbc.gridy++;
        card.add(cfgLabel("Sinh code mẫu:"), gbc);
        cbGenAC  = darkCheck("Sinh AC solution", true);
        cbGenWA  = darkCheck("Sinh WA solution", false);
        cbGenTLE = darkCheck("Sinh TLE solution", false);
        for (JCheckBox cb : new JCheckBox[]{cbGenAC, cbGenWA, cbGenTLE}) {
            gbc.gridy++;
            card.add(cb, gbc);
        }

        // Spring
        gbc.gridy++; gbc.weighty = 1.0;
        card.add(Box.createVerticalGlue(), gbc);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildRightPanel() {
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setBackground(Theme.BG_DARKEST);
        rightSplit.setBorder(null);
        rightSplit.setDividerSize(4);
        rightSplit.setDividerLocation(220);
        rightSplit.setResizeWeight(0.4);

        rightSplit.setTopComponent(buildLogPanel());
        rightSplit.setBottomComponent(buildTestTablePanel());

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Theme.BG_DARKEST);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
        wrap.add(rightSplit, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildLogPanel() {
        JPanel card = Components.card("Log sinh test");
        card.setLayout(new BorderLayout(0, 6));

        // Progress
        JPanel progress = new JPanel(new BorderLayout(8, 0));
        progress.setBackground(Theme.BG_PANEL);
        progressBar = new JProgressBar(0, 100);
        progressBar.setBackground(Theme.BG_INPUT);
        progressBar.setForeground(Theme.ACCENT_CYAN);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setValue(0);
        progressLabel = new JLabel("Chờ...");
        progressLabel.setFont(Theme.FONT_SMALL);
        progressLabel.setForeground(Theme.TEXT_SECONDARY);
        progress.add(progressBar, BorderLayout.CENTER);
        progress.add(progressLabel, BorderLayout.EAST);
        card.add(progress, BorderLayout.NORTH);

        logArea = Components.darkTextArea("");
        logArea.setFont(Theme.FONT_MONO);
        logArea.setEditable(false);
        logArea.setForeground(Theme.TEXT_CODE);
        JScrollPane logScroll = Components.darkScroll(logArea);
        card.add(logScroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTestTablePanel() {
        JPanel card = Components.card("Danh sách test cases");
        card.setLayout(new BorderLayout(0, 6));

        String[] cols = {"#", "Loại", "Kích thước", "Trạng thái", "Input (preview)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        testTable = new JTable(tableModel);
        styleTable(testTable);

        // Column widths
        int[] widths = {40, 80, 90, 100, 0};
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > 0) testTable.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
        }

        // Row click: show full test
        testTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showTestDetail();
            }
        });

        card.add(Components.darkScroll(testTable), BorderLayout.CENTER);

        // Footer
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        foot.setBackground(Theme.BG_PANEL);
        foot.add(cfgLabel("Nhấp đúp vào test để xem chi tiết"));
        card.add(foot, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BG_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel info = new JLabel("Chưa có đề bài. Hãy nhập đề ở Tab 1 trước.");
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.TEXT_SECONDARY);
        bar.add(info, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.BG_PANEL);

        clearLogBtn = new Components.GhostButton("🗑  Xóa log");
        clearLogBtn.setPreferredSize(new Dimension(110, 36));
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        btns.add(clearLogBtn);

        stopBtn = new Components.AccentButton("⏹  Dừng", Theme.ACCENT_RED);
        stopBtn.setPreferredSize(new Dimension(110, 36));
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> stopGeneration());
        btns.add(stopBtn);

        generateBtn = new Components.AccentButton("▶  Sinh test", Theme.ACCENT_CYAN);
        generateBtn.setPreferredSize(new Dimension(140, 36));
        generateBtn.setEnabled(false);
        generateBtn.addActionListener(e -> startGeneration());
        btns.add(generateBtn);

        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ── Generation Logic ───────────────────────────────────────────────────
    private void startGeneration() {
        if (currentProblem == null) {
            JOptionPane.showMessageDialog(this,
                "Chưa có đề bài! Hãy nhập đề ở Tab 1 trước.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int count = (int) testCountSpinner.getValue();
        generatedTests.clear();
        tableModel.setRowCount(0);
        generateBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);

        appendLog("════════════════════════════════════");
        appendLog("[START] Bắt đầu sinh test cho: " + currentProblem.getTitle());
        appendLog("[CFG]   Số test: " + count);
        appendLog("════════════════════════════════════");

        activeWorker = new SwingWorker<List<TestCase>, Object[]>() {
            @Override
            protected List<TestCase> doInBackground() throws Exception {
                List<TestCase> tests = BackendService.getInstance()
                    .generateTestCases(currentProblem, count, msg -> publish(new Object[]{msg, null}));

                // Publish each test as it's created
                for (int i = 0; i < tests.size(); i++) {
                    TestCase tc = tests.get(i);
                    publish(new Object[]{null, tc});
                    int pct = (int)((i + 1) * 100.0 / tests.size());
                    progressBar.setValue(pct);
                }

                // Optionally generate checker / solutions
                if (cbGenChecker.isSelected()) {
                    publish(new Object[]{"[CHK] Đang sinh checker...", null});
                    String checker = BackendService.getInstance()
                        .generateChecker(currentProblem, msg -> publish(new Object[]{msg, null}));
                    publish(new Object[]{"[CHK] ✓ Checker đã sinh (" + checker.length() + " chars)", null});
                }

                // Generate solutions BATCH (1 API call thay vì 3)
                boolean genAC = cbGenAC.isSelected();
                boolean genWA = cbGenWA.isSelected();
                boolean genTLE = cbGenTLE.isSelected();
                
                if (genAC || genWA || genTLE) {
                    BackendService.getInstance().generateSolutionsBatch(
                        currentProblem, genAC, genWA, genTLE,
                        msg -> publish(new Object[]{msg, null})
                    );
                }

                return tests;
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] chunk : chunks) {
                    if (chunk[0] != null) appendLog((String) chunk[0]);
                    if (chunk[1] != null) addTestRow((TestCase) chunk[1]);
                }
            }

            @Override
            protected void done() {
                stopBtn.setEnabled(false);
                generateBtn.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                try {
                    generatedTests = get();
                    appendLog("════════════════════════════════════");
                    appendLog("[DONE] ✓ Sinh xong " + generatedTests.size() + " test cases!");
                    progressLabel.setText(generatedTests.size() + " tests");
                    if (onTestsGenerated != null) onTestsGenerated.accept(generatedTests);
                } catch (Exception ex) {
                    if (!isCancelled()) {
                        appendLog("[ERR]  ✗ " + ex.getMessage());
                        progressLabel.setText("Lỗi");
                    } else {
                        appendLog("[STOP] Đã dừng sinh test.");
                        progressLabel.setText("Đã dừng");
                    }
                }
            }
        };
        activeWorker.execute();
    }

    private void stopGeneration() {
        if (activeWorker != null) activeWorker.cancel(true);
        stopBtn.setEnabled(false);
        generateBtn.setEnabled(true);
        progressBar.setIndeterminate(false);
    }

    private void addTestRow(TestCase tc) {
        String preview = tc.getInput().replace("\n", " ").trim();
        if (preview.length() > 40) preview = preview.substring(0, 40) + "…";
        tableModel.addRow(new Object[]{
            tc.getIndex(),
            tc.getType().toString(),
            tc.getInput().length() + " B",
            tc.isVerified() ? "✓ Verified" : "Pending",
            preview
        });
        generatedTests.add(tc);
        // Scroll to bottom
        int last = testTable.getRowCount() - 1;
        if (last >= 0) testTable.scrollRectToVisible(testTable.getCellRect(last, 0, true));
    }

    private void showTestDetail() {
        int row = testTable.getSelectedRow();
        if (row < 0 || row >= generatedTests.size()) return;
        TestCase tc = generatedTests.get(row);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Test #" + tc.getIndex() + " [" + tc.getType() + "]", false);
        dlg.setSize(600, 400);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG_PANEL);
        dlg.setLayout(new BorderLayout(8, 8));

        JPanel split = new JPanel(new GridLayout(1, 2, 8, 0));
        split.setBackground(Theme.BG_PANEL);
        split.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JTextArea inArea  = Components.darkTextArea("");
        inArea.setText(tc.getInput());
        inArea.setEditable(false);

        JTextArea outArea = Components.darkTextArea("");
        outArea.setText(tc.getExpectedOutput() != null ? tc.getExpectedOutput() : "(Chưa có output)");
        outArea.setEditable(false);

        JPanel inPanel  = new JPanel(new BorderLayout(0, 4)); inPanel.setBackground(Theme.BG_PANEL);
        JPanel outPanel = new JPanel(new BorderLayout(0, 4)); outPanel.setBackground(Theme.BG_PANEL);
        inPanel.add(Components.sectionLabel("Input"), BorderLayout.NORTH);
        inPanel.add(Components.darkScroll(inArea), BorderLayout.CENTER);
        outPanel.add(Components.sectionLabel("Expected Output"), BorderLayout.NORTH);
        outPanel.add(Components.darkScroll(outArea), BorderLayout.CENTER);

        split.add(inPanel);
        split.add(outPanel);
        dlg.add(split, BorderLayout.CENTER);

        Components.GhostButton close = new Components.GhostButton("Đóng");
        close.addActionListener(e -> dlg.dispose());
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.setBackground(Theme.BG_PANEL);
        foot.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        foot.add(close);
        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private JLabel cfgLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JCheckBox darkCheck(String label, boolean selected) {
        JCheckBox cb = new JCheckBox(label, selected);
        cb.setBackground(Theme.BG_PANEL);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.FONT_UI);
        cb.setFocusPainted(false);
        return cb;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(Theme.BG_INPUT);
        sp.setForeground(Theme.TEXT_PRIMARY);
        sp.setFont(Theme.FONT_UI);
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(Theme.BG_INPUT);
            de.getTextField().setForeground(Theme.TEXT_PRIMARY);
            de.getTextField().setCaretColor(Theme.ACCENT_CYAN);
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.BG_INPUT);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_MONO);
        table.setRowHeight(28);
        table.setGridColor(Theme.BG_BORDER);
        table.setSelectionBackground(new Color(
            Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(),
            Theme.ACCENT_CYAN.getBlue(), 50));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.BG_PANEL);
        header.setForeground(Theme.ACCENT_CYAN);
        header.setFont(Theme.FONT_UI_B);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));
    }

    public List<TestCase> getGeneratedTests() { return generatedTests; }
}
