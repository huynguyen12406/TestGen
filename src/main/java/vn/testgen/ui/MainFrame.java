package vn.testgen.ui;

import vn.testgen.model.Problem;
import vn.testgen.model.TestCase;
import vn.testgen.backend.BackendService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main application window.
 * Custom tab bar at top → 4 panels: Input | Generate | Evaluate | Settings
 */
public class MainFrame extends JFrame {

    private ProblemInputPanel inputPanel;
    private TestGenPanel      testGenPanel;
    private EvaluationPanel   evalPanel;
    private SettingsPanel     settingsPanel;

    private JPanel    tabBar;
    private JPanel    contentArea;
    private CardLayout cardLayout;

    private JButton[] tabButtons;
    private int        activeTab = 0;

    // Shared state
    private Problem       currentProblem;
    private List<TestCase> currentTests;

    // Tab names & icons
    private static final String[] TAB_NAMES = {
        "1  NHẬP ĐỀ", "2  SINH TEST", "3  ĐÁNH GIÁ", "⚙  CÀI ĐẶT"
    };
    private static final String[] CARD_KEYS = {"input", "testgen", "eval", "settings"};

    public MainFrame() {
        super("TestGen — Hệ thống sinh test tự động cho kỳ thi lập trình");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // App icon (colored square fallback)
        setIconImage(buildAppIcon());

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_DARKEST);
        setContentPane(root);

        root.add(buildHeaderBar(), BorderLayout.NORTH);

        // Content area with CardLayout
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_DARKEST);

        // Create panels
        inputPanel    = new ProblemInputPanel(this::onProblemParsed);
        testGenPanel  = new TestGenPanel(this::onTestsGenerated);
        evalPanel     = new EvaluationPanel();
        settingsPanel = new SettingsPanel();

        contentArea.add(inputPanel,    "input");
        contentArea.add(testGenPanel,  "testgen");
        contentArea.add(evalPanel,     "eval");
        contentArea.add(settingsPanel, "settings");

        root.add(contentArea, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        // Show first tab
        switchTab(0);
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));

        // Logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        logoArea.setBackground(Theme.BG_PANEL);
        logoArea.setPreferredSize(new Dimension(180, 48));

        JLabel logo = new JLabel("⚡ TestGen") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle gradient text effect via clip
                GradientPaint gp = new GradientPaint(
                    0, 0, Theme.ACCENT_CYAN,
                    getWidth(), 0, new Color(0x7C3AED));
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Theme.ACCENT_CYAN);
        logo.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        logoArea.add(logo);

        // Version chip
        JLabel ver = new JLabel("v1.0");
        ver.setFont(Theme.FONT_SMALL);
        ver.setForeground(Theme.TEXT_MUTED);
        ver.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        logoArea.add(ver);

        header.add(logoArea, BorderLayout.WEST);

        // Tab buttons
        tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(Theme.BG_PANEL);

        tabButtons = new JButton[TAB_NAMES.length];
        for (int i = 0; i < TAB_NAMES.length; i++) {
            tabButtons[i] = createTabButton(TAB_NAMES[i], i);
            tabBar.add(tabButtons[i]);
        }
        header.add(tabBar, BorderLayout.CENTER);

        // Right: workflow arrows hint
        JLabel wfHint = new JLabel("Nhập đề → Sinh test → Đánh giá  ");
        wfHint.setFont(Theme.FONT_SMALL);
        wfHint.setForeground(Theme.TEXT_MUTED);
        header.add(wfHint, BorderLayout.EAST);

        return header;
    }

    private JButton createTabButton(String label, int index) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = (activeTab == index);
                boolean hov    = getModel().isRollover();

                g2.setColor(active ? Theme.BG_DARKEST : (hov ? Theme.BG_HOVER : Theme.BG_PANEL));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Active underline
                if (active) {
                    g2.setColor(Theme.ACCENT_CYAN);
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(Theme.FONT_UI_B);
        btn.setForeground(index == 0 ? Theme.ACCENT_CYAN : Theme.TEXT_SECONDARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 48));
        btn.setMargin(new Insets(0, 0, 0, 0));

        final int idx = index;
        btn.addActionListener(e -> switchTab(idx));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x090D13));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BG_BORDER),
            BorderFactory.createEmptyBorder(3, 12, 3, 12)
        ));

        JLabel left = new JLabel("TestGen  |  Java " + System.getProperty("java.version"));
        left.setFont(Theme.FONT_SMALL);
        left.setForeground(Theme.TEXT_MUTED);
        bar.add(left, BorderLayout.WEST);

        JLabel right = new JLabel("Nhóm: [Tên nhóm]  |  Deadline: 15/05/2026  ");
        right.setFont(Theme.FONT_SMALL);
        right.setForeground(Theme.TEXT_MUTED);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private void switchTab(int idx) {
        activeTab = idx;
        cardLayout.show(contentArea, CARD_KEYS[idx]);

        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setForeground(i == idx ? Theme.ACCENT_CYAN : Theme.TEXT_SECONDARY);
            tabButtons[i].repaint();
        }
    }

    // ── Data flow callbacks ────────────────────────────────────────────────
    private void onProblemParsed(Problem problem) {
        this.currentProblem = problem;
        testGenPanel.setProblem(problem);
        // Auto-switch to test gen tab
        SwingUtilities.invokeLater(() -> {
            switchTab(1);
            showNotification("✓ Đề bài \"" + problem.getTitle() + "\" đã được phân tích. Hãy sinh test!");
        });
    }

    private void onTestsGenerated(List<TestCase> tests) {
        this.currentTests = tests;
        evalPanel.setData(currentProblem, tests);
        
        // Set generated code to EvaluationPanel
        BackendService backend = BackendService.getInstance();
        if (backend.getLastGeneratedAcCode() != null && !backend.getLastGeneratedAcCode().isBlank()) {
            evalPanel.setAcCode(backend.getLastGeneratedAcCode());
        }
        if (backend.getLastGeneratedWaCode() != null && !backend.getLastGeneratedWaCode().isBlank()) {
            evalPanel.setWaCode(backend.getLastGeneratedWaCode());
        }
        if (backend.getLastGeneratedTleCode() != null && !backend.getLastGeneratedTleCode().isBlank()) {
            evalPanel.setTleCode(backend.getLastGeneratedTleCode());
        }
        
        // Show notification
        SwingUtilities.invokeLater(() ->
            showNotification("✓ Đã sinh " + tests.size() + " tests. Chuyển sang Tab 3 để đánh giá."));
    }

    private void showNotification(String msg) {
        // Toast-style notification at bottom right
        JWindow toast = new JWindow(this);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_CYAN, 1),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        JLabel lbl = new JLabel(msg);
        lbl.setFont(Theme.FONT_UI);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        p.add(lbl);
        toast.setContentPane(p);
        toast.pack();

        // Position bottom-right of window
        Point loc = getLocationOnScreen();
        Dimension sz = getSize();
        toast.setLocation(loc.x + sz.width - toast.getWidth() - 20,
                          loc.y + sz.height - toast.getHeight() - 60);
        toast.setVisible(true);

        // Auto-dismiss after 3s
        Timer t = new Timer(3000, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }

    private Image buildAppIcon() {
        // Draw a simple ⚡ icon as 64x64
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG_PANEL);
        g2.fillRoundRect(0, 0, 64, 64, 12, 12);
        g2.setColor(Theme.ACCENT_CYAN);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2.drawString("⚡", 8, 50);
        g2.dispose();
        return img;
    }
}
