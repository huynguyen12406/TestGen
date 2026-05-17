package vn.testgen.ui;

import vn.testgen.backend.BackendService;
import javax.swing.*;
import java.awt.*;

/**
 * Panel 4: Settings - API Key, default limits, language preferences
 */
public class SettingsPanel extends JPanel {

    private JPasswordField apiKeyField;
    private JPasswordField visionApiKeyField;
    private JComboBox<String> providerCombo;
    private JComboBox<String> modelCombo;
    private JTextField defaultTimeLimitField;
    private JTextField defaultMemLimitField;
    private JComboBox<String> defaultLangCombo;
    private JCheckBox cbAutoGenChecker;
    private JCheckBox cbAutoRunAfterGen;
    private JTextArea aboutArea;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARKEST);
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));
        JLabel title = new JLabel("⚙  CÀI ĐẶT");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_CYAN);
        toolbar.add(title);
        add(toolbar, BorderLayout.NORTH);

        // Main scrollable content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_DARKEST);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        content.add(buildApiSection());
        content.add(Box.createVerticalStrut(12));
        content.add(buildDefaultsSection());
        content.add(Box.createVerticalStrut(12));
        content.add(buildBehaviorSection());
        content.add(Box.createVerticalStrut(12));
        content.add(buildAboutSection());
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = Components.darkScroll(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        add(buildSaveBar(), BorderLayout.SOUTH);
    }

    private JPanel buildApiSection() {
        JPanel card = Components.card("🔑  API Credentials");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // AI Provider selection
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(smallLabel("AI Provider:"), gbc);
        gbc.gridy = 1;
        providerCombo = Components.darkCombo(new String[]{
            "GROQ (llama-3.3-70b - 14,400 req/ngày - Khuyến nghị)",
            "GEMINI (gemini-2.5-flash - 20 req/ngày)",
            "OPENAI (gpt-4o - Trả phí)"
        });
        
        // Load saved provider from BackendService
        String currentProvider = BackendService.getInstance().getCurrentProvider();
        if (currentProvider.equals("GROQ")) {
            providerCombo.setSelectedIndex(0);
        } else if (currentProvider.equals("GEMINI")) {
            providerCombo.setSelectedIndex(1);
        } else if (currentProvider.equals("OPENAI")) {
            providerCombo.setSelectedIndex(2);
        } else {
            providerCombo.setSelectedIndex(0); // Default to Groq
        }
        
        providerCombo.addActionListener(e -> updateApiKeyPlaceholder());
        card.add(providerCombo, gbc);

        // API Key field
        gbc.gridy = 2;
        card.add(smallLabel("API Key:"), gbc);
        gbc.gridy = 3;
        apiKeyField = new JPasswordField();
        apiKeyField.setBackground(Theme.BG_INPUT);
        apiKeyField.setForeground(Theme.TEXT_PRIMARY);
        apiKeyField.setCaretColor(Theme.ACCENT_CYAN);
        apiKeyField.setFont(Theme.FONT_MONO);
        apiKeyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BG_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        
        // Load saved API key
        String saved = BackendService.getInstance().getApiKey();
        if (!saved.isEmpty()) {
            apiKeyField.setText(saved);
        } else {
            // Default to empty - user must provide their own key
            apiKeyField.setText("");
        }
        card.add(apiKeyField, gbc);
        
        // Vision API Key field (for image analysis)
        gbc.gridy = 4;
        card.add(smallLabel("Vision API Key (Gemini - cho đọc ảnh):"), gbc);
        gbc.gridy = 5;
        visionApiKeyField = new JPasswordField();
        visionApiKeyField.setBackground(Theme.BG_INPUT);
        visionApiKeyField.setForeground(Theme.TEXT_PRIMARY);
        visionApiKeyField.setCaretColor(Theme.ACCENT_CYAN);
        visionApiKeyField.setFont(Theme.FONT_MONO);
        visionApiKeyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BG_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        
        // Load saved vision API key
        String savedVision = BackendService.getInstance().getVisionApiKey();
        if (!savedVision.isEmpty()) {
            visionApiKeyField.setText(savedVision);
        } else {
            // Default to empty - user must provide their own key
            visionApiKeyField.setText("");
        }
        card.add(visionApiKeyField, gbc);

        // Model selection (kept for compatibility, but not used with Groq)
        gbc.gridy = 6;
        card.add(smallLabel("Model AI (chỉ cho OpenAI/Gemini):"), gbc);
        gbc.gridy = 7;
        modelCombo = Components.darkCombo(new String[]{
            "claude-sonnet-4-20250514 (khuyên dùng)",
            "claude-opus-4-20250514 (mạnh nhất)",
            "claude-haiku-4-5-20251001 (nhanh nhất)"
        });
        modelCombo.setEnabled(false); // Disabled by default for Groq
        card.add(modelCombo, gbc);

        // Info hint
        gbc.gridy = 8;
        JLabel hint = new JLabel("  ℹ  Groq: text only | Gemini Vision: đọc ảnh | Groq: 14,400 req/ngày | Gemini: 20 req/ngày");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        card.add(hint, gbc);

        return wrapCard(card);
    }
    
    /**
     * Update API key based on selected provider
     */
    private void updateApiKeyPlaceholder() {
        int selected = providerCombo.getSelectedIndex();
        
        // Always update API key when provider changes
        switch (selected) {
            case 0: // Groq
                apiKeyField.setText("");
                modelCombo.setEnabled(false);
                break;
            case 1: // Gemini
                apiKeyField.setText("");
                modelCombo.setEnabled(false);
                break;
            case 2: // OpenAI
                apiKeyField.setText("sk-proj-...");
                modelCombo.setEnabled(true);
                break;
        }
    }

    private JPanel buildDefaultsSection() {
        JPanel card = Components.card("⏱  Giá trị mặc định");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(smallLabel("Time limit (ms):"), gbc);
        gbc.gridx = 1;
        card.add(smallLabel("Memory limit (MB):"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        defaultTimeLimitField = Components.darkTextField("1000");
        card.add(defaultTimeLimitField, gbc);
        gbc.gridx = 1;
        defaultMemLimitField = Components.darkTextField("256");
        card.add(defaultMemLimitField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(smallLabel("Ngôn ngữ lập trình mặc định:"), gbc);
        gbc.gridy = 3;
        defaultLangCombo = Components.darkCombo(new String[]{"C++17", "C++14", "Java 17", "Python 3"});
        card.add(defaultLangCombo, gbc);

        return wrapCard(card);
    }

    private JPanel buildBehaviorSection() {
        JPanel card = Components.card("🤖  Hành vi tự động");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        cbAutoGenChecker = darkCheck("Tự động sinh checker sau khi phân tích đề");
        cbAutoRunAfterGen = darkCheck("Tự động chạy đánh giá sau khi sinh test xong");

        card.add(cbAutoGenChecker);
        card.add(Box.createVerticalStrut(6));
        card.add(cbAutoRunAfterGen);

        return wrapCard(card);
    }

    private JPanel buildAboutSection() {
        JPanel card = Components.card("ℹ  Về chương trình");
        card.setLayout(new BorderLayout(0, 6));

        aboutArea = Components.darkTextArea("");
        aboutArea.setEditable(false);
        aboutArea.setForeground(Theme.TEXT_SECONDARY);
        aboutArea.setFont(Theme.FONT_SMALL);
        aboutArea.setText(
            "TestGen - Hệ thống sinh test tự động cho các kỳ thi lập trình\n" +
            "Phiên bản: 1.0.0\n\n" +
            "Phân công nhóm:\n" +
            "  • Frontend (GUI)    - [Tên SV 1]: Giao diện Swing, luồng dữ liệu\n" +
            "  • Backend AI        - [Tên SV 2]: Gọi Anthropic API, phân tích đề\n" +
            "  • Test Generator    - [Tên SV 3]: Logic sinh test, checker\n" +
            "  • Compiler/Runner   - [Tên SV 4]: Biên dịch, chạy, đánh giá\n\n" +
            "Hướng dẫn cài đặt: Xem README.md trong thư mục gốc\n" +
            "Ngôn ngữ: Java 17+ | Framework: Swing | AI: Anthropic Claude API\n\n" +
            "Deadline: 15/05/2026"
        );

        card.add(Components.darkScroll(aboutArea), BorderLayout.CENTER);
        return wrapCard(card);
    }

    private JPanel buildSaveBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BG_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel status = new JLabel("Thay đổi chưa được lưu");
        status.setFont(Theme.FONT_SMALL);
        status.setForeground(Theme.TEXT_MUTED);
        bar.add(status, BorderLayout.CENTER);

        Components.AccentButton saveBtn = new Components.AccentButton("💾  Lưu cài đặt", Theme.ACCENT_CYAN);
        saveBtn.setPreferredSize(new Dimension(150, 36));
        saveBtn.addActionListener(e -> {
            // Get selected provider
            int providerIndex = providerCombo.getSelectedIndex();
            String providerName = switch (providerIndex) {
                case 0 -> "GROQ";
                case 1 -> "GEMINI";
                case 2 -> "OPENAI";
                default -> "GROQ";
            };
            
            // Get API keys
            String key = new String(apiKeyField.getPassword()).trim();
            String visionKey = new String(visionApiKeyField.getPassword()).trim();
            
            // Update backend service
            BackendService backend = BackendService.getInstance();
            backend.setApiKey(key);
            backend.setVisionApiKey(visionKey);
            backend.setProvider(providerName);
            
            // Update status
            status.setText("✓ Đã lưu: " + providerName + " - " + java.time.LocalTime.now().toString().substring(0, 8));
            status.setForeground(Theme.ACCENT_GREEN);
            
            // Log to console
            System.out.println("[SETTINGS] Provider: " + providerName);
            System.out.println("[SETTINGS] API Key: " + key.substring(0, Math.min(10, key.length())) + "...");
            System.out.println("[SETTINGS] Vision API Key: " + visionKey.substring(0, Math.min(10, visionKey.length())) + "...");
        });
        bar.add(saveBtn, BorderLayout.EAST);
        return bar;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JCheckBox darkCheck(String label) {
        JCheckBox cb = new JCheckBox(label, false);
        cb.setBackground(Theme.BG_PANEL);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.FONT_UI);
        cb.setFocusPainted(false);
        cb.setAlignmentX(0f);
        return cb;
    }

    private JPanel wrapCard(JPanel card) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Theme.BG_DARKEST);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 40));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }
}
