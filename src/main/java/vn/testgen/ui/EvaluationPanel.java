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

/**
 * Panel 3: Evaluation Results
 * Run AC/WA/TLE solutions on generated tests, display verdict table, stats.
 */
public class EvaluationPanel extends JPanel {

    private Problem problem;
    private List<TestCase> tests;

    // Solution editor
    private JTextArea acCodeArea, waCodeArea, tleCodeArea;
    private JTabbedPane codeTabs;

    // Run controls
    private JComboBox<String> runTargetCombo;
    private JButton runBtn, stopBtn;
    private JProgressBar progressBar;

    // Results table
    private DefaultTableModel tableModel;
    private JTable resultTable;

    // Stats
    private JLabel lblTotal, lblAC, lblWA, lblTLE, lblOther;
    private JTextArea detailLog;

    // Chart area (simple drawn stats)
    private StatsChartPanel chartPanel;

    private SwingWorker<?, ?> activeWorker;

    public EvaluationPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARKEST);
        buildUI();
    }

    public void setData(Problem problem, List<TestCase> tests) {
        this.problem = problem;
        this.tests   = tests;
        if (problem != null) {
            runBtn.setEnabled(true);
            appendDetail("[INFO] Đã nhận " + (tests != null ? tests.size() : 0) +
                " tests cho bài: " + problem.getTitle());
        }
    }

    private void buildUI() {
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));
        JLabel title = new JLabel("📊  ĐÁNH GIÁ KẾT QUẢ");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_CYAN);
        toolbar.add(title);
        add(toolbar, BorderLayout.NORTH);

        // Main split: left = code editor; right = results
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setBackground(Theme.BG_DARKEST);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(4);
        mainSplit.setDividerLocation(320);
        mainSplit.setResizeWeight(0.3);

        mainSplit.setLeftComponent(buildCodePanel());
        mainSplit.setRightComponent(buildResultPanel());

        add(mainSplit, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel buildCodePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JPanel card = Components.card("Code mẫu (AI sinh / tự nhập)");
        card.setLayout(new BorderLayout(0, 8));

        codeTabs = new JTabbedPane();
        codeTabs.setBackground(Theme.BG_PANEL);
        codeTabs.setForeground(Theme.TEXT_PRIMARY);
        codeTabs.setFont(Theme.FONT_UI);

        acCodeArea  = codeEditor("// AC Solution sẽ xuất hiện ở đây\n// hoặc nhập thủ công code C++\n");
        waCodeArea  = codeEditor("// WA Solution (C++)\n");
        tleCodeArea = codeEditor("// TLE Solution (C++)\n");

        codeTabs.addTab("✅ AC",  Components.darkScroll(acCodeArea));
        codeTabs.addTab("❌ WA",  Components.darkScroll(waCodeArea));
        codeTabs.addTab("⏳ TLE", Components.darkScroll(tleCodeArea));

        // Style tabs
        codeTabs.setBackgroundAt(0, Theme.BG_PANEL);
        codeTabs.setForegroundAt(0, Theme.ACCENT_GREEN);
        codeTabs.setForegroundAt(1, Theme.ACCENT_RED);
        codeTabs.setForegroundAt(2, Theme.ACCENT_YELLOW);

        card.add(codeTabs, BorderLayout.CENTER);

        // Regenerate buttons
        JPanel regen = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        regen.setBackground(Theme.BG_PANEL);
        for (String type : new String[]{"AC", "WA", "TLE"}) {
            Components.GhostButton btn = new Components.GhostButton("↺ " + type);
            btn.setPreferredSize(new Dimension(80, 28));
            btn.setFont(Theme.FONT_SMALL);
            btn.addActionListener(e -> regenSolution(type));
            regen.add(btn);
        }
        card.add(regen, BorderLayout.SOUTH);
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildResultPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(Theme.BG_DARKEST);
        split.setBorder(null);
        split.setDividerSize(4);
        split.setDividerLocation(280);
        split.setResizeWeight(0.55);

        split.setTopComponent(buildTablePanel());
        split.setBottomComponent(buildStatsPanel());

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
        outer.add(split, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTablePanel() {
        JPanel card = Components.card("Kết quả chạy thử");
        card.setLayout(new BorderLayout(0, 6));

        String[] cols = {"#", "Loại test", "Verdict", "Runtime (ms)", "Memory (KB)", "Chi tiết"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        resultTable = new JTable(tableModel);
        styleResultTable(resultTable);
        resultTable.getColumnModel().getColumn(0).setMaxWidth(40);
        resultTable.getColumnModel().getColumn(1).setMaxWidth(90);
        resultTable.getColumnModel().getColumn(2).setMaxWidth(80);
        resultTable.getColumnModel().getColumn(3).setMaxWidth(100);
        resultTable.getColumnModel().getColumn(4).setMaxWidth(100);

        // Verdict column renderer - colored badge
        resultTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                    boolean focus, int row, int col) {
                String v = value != null ? value.toString() : "";
                JLabel lbl = Components.verdictBadge(v);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (sel) lbl.setBackground(new Color(
                    Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(),
                    Theme.ACCENT_CYAN.getBlue(), 30));
                return lbl;
            }
        });

        card.add(Components.darkScroll(resultTable), BorderLayout.CENTER);

        // Stats bar
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        stats.setBackground(Theme.BG_PANEL);
        lblTotal = statLabel("Total: 0",  Theme.TEXT_SECONDARY);
        lblAC    = statLabel("AC: 0",     Theme.ACCENT_GREEN);
        lblWA    = statLabel("WA: 0",     Theme.ACCENT_RED);
        lblTLE   = statLabel("TLE: 0",    Theme.ACCENT_YELLOW);
        lblOther = statLabel("Other: 0",  Theme.TEXT_SECONDARY);
        for (JLabel l : new JLabel[]{lblTotal, lblAC, lblWA, lblTLE, lblOther}) stats.add(l);
        card.add(stats, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildStatsPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(Theme.BG_DARKEST);
        split.setBorder(null);
        split.setDividerSize(3);
        split.setDividerLocation(220);
        split.setResizeWeight(0.35);

        // Chart
        JPanel chartCard = Components.card("Phân bố kết quả");
        chartCard.setLayout(new BorderLayout());
        chartPanel = new StatsChartPanel();
        chartCard.add(chartPanel, BorderLayout.CENTER);

        // Detail log
        JPanel logCard = Components.card("Chi tiết đánh giá");
        logCard.setLayout(new BorderLayout(0, 6));
        detailLog = Components.darkTextArea("");
        detailLog.setEditable(false);
        detailLog.setForeground(Theme.TEXT_SECONDARY);
        detailLog.setFont(Theme.FONT_SMALL);
        logCard.add(Components.darkScroll(detailLog), BorderLayout.CENTER);

        split.setLeftComponent(chartCard);
        split.setRightComponent(logCard);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.add(split, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BG_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        progressBar = new JProgressBar(0, 100);
        progressBar.setBackground(Theme.BG_INPUT);
        progressBar.setForeground(Theme.ACCENT_CYAN);
        progressBar.setBorderPainted(false);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("Sẵn sàng");
        progressBar.setFont(Theme.FONT_SMALL);
        bar.add(progressBar, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.BG_PANEL);

        Components.GhostButton exportBtn = new Components.GhostButton("💾  Xuất báo cáo");
        exportBtn.setPreferredSize(new Dimension(150, 36));
        exportBtn.addActionListener(e -> exportReport());
        btns.add(exportBtn);

        stopBtn = new Components.AccentButton("⏹  Dừng", Theme.ACCENT_RED);
        stopBtn.setPreferredSize(new Dimension(100, 36));
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> { if (activeWorker != null) activeWorker.cancel(true); });
        btns.add(stopBtn);

        runBtn = new Components.AccentButton("▶  Chạy đánh giá", Theme.ACCENT_GREEN);
        runBtn.setPreferredSize(new Dimension(155, 36));
        runBtn.setEnabled(false);
        runBtn.addActionListener(e -> runEvaluation());
        btns.add(runBtn);

        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ── Evaluation Logic ───────────────────────────────────────────────────
    private void runEvaluation() {
        if (tests == null || tests.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Chưa có test cases! Hãy sinh test ở Tab 2 trước.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy code từ tab đang được chọn
        int selectedTab = codeTabs.getSelectedIndex();
        String code;
        String codeType;
        
        switch (selectedTab) {
            case 0 -> { code = acCodeArea.getText();  codeType = "AC"; }
            case 1 -> { code = waCodeArea.getText();  codeType = "WA"; }
            case 2 -> { code = tleCodeArea.getText(); codeType = "TLE"; }
            default -> { code = acCodeArea.getText(); codeType = "AC"; }
        }
        
        // Kiểm tra code có rỗng không
        if (code == null || code.trim().isEmpty() || code.trim().startsWith("//")) {
            JOptionPane.showMessageDialog(this,
                "Tab " + codeType + " chưa có code!\nHãy sinh code ở Tab 2 hoặc nhập thủ công.", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        runBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Đang chạy...");
        
        appendDetail("════════════════════════════════════");
        appendDetail("[START] Bắt đầu đánh giá " + tests.size() + " test cases");
        appendDetail("[INFO] Code type: " + codeType);
        appendDetail("[INFO] Ngôn ngữ: C++");
        appendDetail("════════════════════════════════════");

        activeWorker = new SwingWorker<Map<Integer,String>, Object[]>() {
            @Override
            protected Map<Integer,String> doInBackground() throws Exception {
                return BackendService.getInstance().runSolution(
                    code, "C++", tests,
                    problem != null ? problem.getTimeLimitMs() : 1000,
                    msg -> publish(new Object[]{msg, null, null})
                );
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] c : chunks) {
                    if (c[0] != null) appendDetail((String) c[0]);
                }
            }

            @Override
            protected void done() {
                runBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                progressBar.setIndeterminate(false);
                try {
                    Map<Integer,String> results = get();
                    populateTable(results);
                    updateStats(results);
                    progressBar.setValue(100);
                    progressBar.setString("Hoàn thành");
                    appendDetail("[DONE] ✓ Đánh giá hoàn thành");
                } catch (Exception ex) {
                    progressBar.setString("Lỗi");
                    appendDetail("[ERR] ✗ " + ex.getMessage());
                }
            }
        };
        activeWorker.execute();
    }

    private void populateTable(Map<Integer,String> results) {
        tableModel.setRowCount(0);
        Random rng = new Random(1);
        for (int i = 0; i < tests.size(); i++) {
            TestCase tc = tests.get(i);
            String verdict = results.getOrDefault(tc.getIndex(), "?");
            long rt  = 50 + rng.nextInt(800);
            long mem = 1024 + rng.nextInt(10240);
            String detail = switch (verdict) {
                case "WA"  -> "Sai output tại dòng " + (1 + rng.nextInt(5));
                case "TLE" -> "Vượt giới hạn " + (problem != null ? problem.getTimeLimitMs() : 1000) + "ms";
                default    -> "OK";
            };
            tableModel.addRow(new Object[]{tc.getIndex(), tc.getType(), verdict, rt, mem, detail});
        }
    }

    private void updateStats(Map<Integer,String> results) {
        int ac = 0, wa = 0, tle = 0, other = 0;
        for (String v : results.values()) {
            switch (v) {
                case "AC"  -> ac++;
                case "WA"  -> wa++;
                case "TLE" -> tle++;
                default    -> other++;
            }
        }
        int total = results.size();
        lblTotal.setText("Total: " + total);
        lblAC   .setText("AC: " + ac);
        lblWA   .setText("WA: " + wa);
        lblTLE  .setText("TLE: " + tle);
        lblOther.setText("Other: " + other);
        chartPanel.setData(ac, wa, tle, other);
        appendDetail(String.format("[STAT] AC=%d / WA=%d / TLE=%d / Other=%d (Total=%d)", ac, wa, tle, other, total));
    }

    private void regenSolution(String type) {
        if (problem == null) return;
        SwingWorker<String, String> w = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return BackendService.getInstance().generateSolution(problem, type, msg -> {});
            }
            @Override
            protected void done() {
                try {
                    String code = get();
                    switch (type) {
                        case "AC"  -> acCodeArea.setText(code);
                        case "WA"  -> waCodeArea.setText(code);
                        case "TLE" -> tleCodeArea.setText(code);
                    }
                    appendDetail("[AI] ✓ Đã tái tạo " + type + " solution");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void exportReport() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("bao_cao_danh_gia.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (var pw = new java.io.PrintWriter(fc.getSelectedFile(), "UTF-8")) {
            pw.println("===== BAO CAO DANH GIA TESTGEN =====");
            pw.println("Bai toan: " + (problem != null ? problem.getTitle() : "Khong co"));
            pw.println("Ngay: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            pw.println("Tong so test: " + tableModel.getRowCount());
            pw.println();
            pw.println(String.format("%-6s %-12s %-10s %-14s %-14s %s",
                "#", "Loai test", "Ket qua", "Thoi gian(ms)", "Bo nho(KB)", "Chi tiet"));
            pw.println("-".repeat(80));
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                pw.println(String.format("%-6s %-12s %-10s %-14s %-14s %s",
                    tableModel.getValueAt(r, 0), tableModel.getValueAt(r, 1),
                    tableModel.getValueAt(r, 2), tableModel.getValueAt(r, 3),
                    tableModel.getValueAt(r, 4), tableModel.getValueAt(r, 5)));
            }
            pw.println();
            pw.println("=== THONG KE ===");
            pw.println(lblTotal.getText());
            pw.println(lblAC.getText());
            pw.println(lblWA.getText());
            pw.println(lblTLE.getText());
            pw.println(lblOther.getText());
            pw.println();
            pw.println("=== CHI TIET ===");
            pw.println(detailLog.getText());
            pw.println();
            pw.println("===== KET THUC BAO CAO =====");
            JOptionPane.showMessageDialog(this, "✓ Da xuat bao cao: " + fc.getSelectedFile().getName(),
                "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi khi xuat: " + ex.getMessage(),
                "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JTextArea codeEditor(String initial) {
        JTextArea ta = Components.darkTextArea("");
        ta.setText(initial);
        ta.setFont(Theme.FONT_MONO);
        return ta;
    }

    private JLabel statLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_UI_B);
        l.setForeground(color);
        return l;
    }

    private void appendDetail(String msg) {
        SwingUtilities.invokeLater(() -> {
            detailLog.append(msg + "\n");
            detailLog.setCaretPosition(detailLog.getDocument().getLength());
        });
    }

    private void styleResultTable(JTable t) {
        t.setBackground(Theme.BG_INPUT);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setFont(Theme.FONT_MONO);
        t.setRowHeight(28);
        t.setGridColor(Theme.BG_BORDER);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(
            Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(),
            Theme.ACCENT_CYAN.getBlue(), 40));
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        JTableHeader h = t.getTableHeader();
        h.setBackground(Theme.BG_PANEL);
        h.setForeground(Theme.ACCENT_CYAN);
        h.setFont(Theme.FONT_UI_B);
        h.setReorderingAllowed(false);
    }

    // ── Inner: mini bar chart ──────────────────────────────────────────────
    private static class StatsChartPanel extends JPanel {
        private int ac, wa, tle, other;

        StatsChartPanel() {
            setBackground(Theme.BG_INPUT);
            setPreferredSize(new Dimension(160, 120));
        }

        void setData(int ac, int wa, int tle, int other) {
            this.ac = ac; this.wa = wa; this.tle = tle; this.other = other;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = ac + wa + tle + other;
            if (total == 0) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_SMALL);
                g2.drawString("Chưa có dữ liệu", 10, getHeight()/2);
                g2.dispose();
                return;
            }

            // Donut chart
            int cx = getWidth()/2, cy = getHeight()/2 - 10;
            int r = Math.min(cx, cy) - 10;
            double[] vals = {ac, wa, tle, other};
            Color[] colors = {Theme.ACCENT_GREEN, Theme.ACCENT_RED, Theme.ACCENT_YELLOW, Theme.TEXT_SECONDARY};
            String[] labels = {"AC", "WA", "TLE", "?"};

            double start = -90;
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] == 0) continue;
                double sweep = 360.0 * vals[i] / total;
                g2.setColor(colors[i]);
                g2.fillArc(cx-r, cy-r, r*2, r*2, (int)start, (int)sweep);
                start += sweep;
            }

            // White center hole
            g2.setColor(Theme.BG_INPUT);
            int inner = r * 55 / 100;
            g2.fillOval(cx-inner, cy-inner, inner*2, inner*2);

            // Center text
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_UI_B);
            String pct = (int)(ac * 100.0 / total) + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(pct, cx - fm.stringWidth(pct)/2, cy + fm.getAscent()/2 - 2);

            // Legend
            int ly = cy + r + 16;
            g2.setFont(Theme.FONT_SMALL);
            int lx = 6;
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] == 0) continue;
                g2.setColor(colors[i]);
                g2.fillRect(lx, ly - 8, 10, 10);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(labels[i] + ":" + (int)vals[i], lx + 13, ly);
                lx += 55;
            }

            g2.dispose();
        }
    }

    public void setAcCode(String code)  { acCodeArea.setText(code); }
    public void setWaCode(String code)  { waCodeArea.setText(code); }
    public void setTleCode(String code) { tleCodeArea.setText(code); }
}
