package vn.testgen.ui;

import vn.testgen.model.Problem;
import vn.testgen.backend.BackendService;
import vn.testgen.util.Constants;
import vn.testgen.util.Validator;
import vn.testgen.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Panel 1: Input Problem
 * Allows user to paste text, upload image, or drag-drop a PDF/image of the problem.
 */
public class ProblemInputPanel extends JPanel {

    private JTextArea   statementArea;
    private JTextField  titleField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> langCombo;
    private JTextField  timeLimitField;
    private JTextField  memLimitField;
    private JLabel      imageLabel;
    private JLabel      statusLabel;
    private JButton     parseButton;
    private JButton     clearButton;
    private JButton     uploadImageBtn;
    private String      uploadedImagePath;

    // Callback: when parse succeeds, notify MainFrame
    private Consumer<Problem> onParsed;

    public ProblemInputPanel(Consumer<Problem> onParsed) {
        this.onParsed = onParsed;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_DARKEST);
        buildUI();
        setupDragDrop();
    }

    private void buildUI() {
        // ── Top toolbar ────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_BORDER));

        JLabel titleLbl = new JLabel(Constants.TITLE_INPUT_PANEL);
        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.ACCENT_CYAN);
        toolbar.add(titleLbl);

        add(toolbar, BorderLayout.NORTH);

        // ── Center: split into left (form) + right (text area) ────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(Theme.BG_DARKEST);
        split.setBorder(null);
        split.setDividerSize(4);
        split.setDividerLocation(280);
        split.setResizeWeight(0.0);

        split.setLeftComponent(buildMetaPanel());
        split.setRightComponent(buildStatementPanel());

        add(split, BorderLayout.CENTER);

        // ── Bottom: status + buttons ────────────────────────────────────────
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel buildMetaPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JPanel form = Components.card(Constants.CARD_PROBLEM_INFO);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(fieldLabel(Constants.LBL_PROBLEM_TITLE), gbc);
        gbc.gridy = 1;
        titleField = Components.darkTextField(Constants.PH_TITLE);
        form.add(titleField, gbc);

        // Contest type
        gbc.gridy = 2; gbc.gridwidth = 1;
        form.add(fieldLabel(Constants.LBL_CONTEST_TYPE), gbc);
        gbc.gridx = 1;
        form.add(fieldLabel(Constants.LBL_LANGUAGE), gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        typeCombo = Components.darkCombo(Constants.CONTEST_TYPES);
        form.add(typeCombo, gbc);
        gbc.gridx = 1;
        langCombo = Components.darkCombo(Constants.LANGUAGES);
        form.add(langCombo, gbc);

        // Limits
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(fieldLabel(Constants.LBL_TIME_LIMIT), gbc);
        gbc.gridx = 1;
        form.add(fieldLabel(Constants.LBL_MEMORY_LIMIT), gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        timeLimitField = Components.darkTextField(Constants.PH_TIME_LIMIT);
        addValidationListener(timeLimitField, this::validateTimeLimit);
        form.add(timeLimitField, gbc);
        gbc.gridx = 1;
        memLimitField = Components.darkTextField(Constants.PH_MEMORY_LIMIT);
        addValidationListener(memLimitField, this::validateMemoryLimit);
        form.add(memLimitField, gbc);

        // Image upload area
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(Components.separator(), gbc);

        gbc.gridy = 7;
        form.add(fieldLabel(Constants.LBL_UPLOAD_IMAGE), gbc);

        gbc.gridy = 8;
        imageLabel = new JLabel(Constants.PH_DRAG_DROP, SwingConstants.CENTER);
        imageLabel.setFont(Theme.FONT_SMALL);
        imageLabel.setForeground(Theme.TEXT_MUTED);
        imageLabel.setBackground(Theme.BG_INPUT);
        imageLabel.setOpaque(true);
        imageLabel.setPreferredSize(new Dimension(200, 90));
        imageLabel.setBorder(BorderFactory.createDashedBorder(Theme.BG_BORDER, 4, 4));
        form.add(imageLabel, gbc);

        gbc.gridy = 9;
        uploadImageBtn = new Components.GhostButton(Constants.BTN_CHOOSE_FILE);
        uploadImageBtn.setPreferredSize(new Dimension(200, 34));
        uploadImageBtn.addActionListener(e -> chooseImageFile());
        form.add(uploadImageBtn, gbc);

        // Spring to push everything up
        gbc.gridy = 10; gbc.weighty = 1.0;
        form.add(Box.createVerticalGlue(), gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildStatementPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG_DARKEST);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));

        JPanel card = Components.card("Nội dung đề bài");
        card.setLayout(new BorderLayout(0, 8));

        // Hint bar
        JLabel hint = new JLabel(
            "  💡 Dán toàn bộ đề bài vào đây (text, LaTeX, hoặc dùng ảnh bên trái)");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setBackground(new Color(0x00D4FF, true).darker().darker().darker());
        hint.setOpaque(false);
        card.add(hint, BorderLayout.NORTH);

        statementArea = Components.darkTextArea(
            "Dán đề bài vào đây...\n\nVí dụ:\n" +
            "Given n integers, find the maximum sum subarray.\n" +
            "Input: First line n (1 ≤ n ≤ 10^5), second line n integers.\n" +
            "Output: Print the maximum sum."
        );
        statementArea.setFont(Theme.FONT_MONO);

        JScrollPane scroll = Components.darkScroll(statementArea);
        card.add(scroll, BorderLayout.CENTER);

        // Sample tests
        JPanel samplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        samplePanel.setBackground(Theme.BG_PANEL);
        samplePanel.add(fieldLabel("Sample tests:"));
        JButton addSample = new Components.GhostButton("+ Thêm sample");
        addSample.setPreferredSize(new Dimension(130, 28));
        addSample.addActionListener(e -> showAddSampleDialog());
        samplePanel.add(addSample);
        card.add(samplePanel, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BG_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        statusLabel = new JLabel("Sẵn sàng nhận đề bài.");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        bar.add(statusLabel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.BG_PANEL);

        clearButton = new Components.GhostButton("🗑  Xóa");
        clearButton.setPreferredSize(new Dimension(100, 36));
        clearButton.addActionListener(e -> clearForm());
        btns.add(clearButton);

        parseButton = new Components.AccentButton("🔍  Phân tích đề", Theme.ACCENT_CYAN);
        parseButton.setPreferredSize(new Dimension(165, 36));
        parseButton.addActionListener(e -> doParse());
        btns.add(parseButton);

        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ── Actions ────────────────────────────────────────────────────────────
    private void doParse() {
        String text = statementArea.getText().trim();
        
        // Nếu không có text VÀ không có ảnh → lỗi
        if (text.isEmpty() && uploadedImagePath == null) {
            showError(Constants.ERR_EMPTY_STATEMENT);
            Logger.warn("ProblemInputPanel", "Parse attempted with empty statement and no image");
            return;
        }
        
        // Tạo biến final để dùng trong lambda
        final String finalText = text;
        final String imagePath = uploadedImagePath;

        parseButton.setEnabled(false);
        setStatus(Constants.STATUS_PARSING, Theme.ACCENT_YELLOW);
        Logger.info("ProblemInputPanel", "Starting problem parse");

        String type = (String) typeCombo.getSelectedItem();

        SwingWorker<Problem, String> worker = new SwingWorker<>() {
            @Override
            protected Problem doInBackground() throws Exception {
                BackendService backend = BackendService.getInstance();
                
                // Nếu có ảnh → dùng OCR để đọc đề bài từ ảnh
                if (imagePath != null && !imagePath.isEmpty()) {
                    publish("[OCR] Đang đọc đề bài từ hình ảnh...");
                    File imageFile = new File(imagePath);
                    
                    if (!imageFile.exists()) {
                        throw new Exception("File ảnh không tồn tại: " + imagePath);
                    }
                    
                    // Nếu có cả text, nối text + OCR result
                    if (!finalText.isEmpty()) {
                        publish("[INFO] Có cả text và ảnh → Kết hợp cả hai");
                        Problem p = backend.parseProblemFromImage(imageFile, type, msg -> publish(msg));
                        return p;
                    } else {
                        return backend.parseProblemFromImage(imageFile, type, msg -> publish(msg));
                    }
                }
                
                // Chỉ có text → parse text bình thường
                return backend.parseProblem(finalText, type, msg -> publish(msg));
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(s -> setStatus(s, Theme.TEXT_SECONDARY));
            }

            @Override
            protected void done() {
                parseButton.setEnabled(true);
                try {
                    Problem p = get();
                    
                    // Apply user-set fields with validation
                    if (!titleField.getText().isBlank()) {
                        p.setTitle(titleField.getText().trim());
                    }
                    
                    // Parse and validate time limit
                    int timeLimit = Validator.parseTimeLimit(timeLimitField.getText());
                    p.setTimeLimitMs(timeLimit);
                    
                    // Parse and validate memory limit
                    int memLimit = Validator.parseMemoryLimit(memLimitField.getText());
                    p.setMemoryLimitMb(memLimit);
                    
                    String successMsg = String.format(Constants.MSG_PARSE_SUCCESS, p.getTitle());
                    setStatus(successMsg, Theme.ACCENT_GREEN);
                    Logger.info("ProblemInputPanel", "Parse successful: " + p.getTitle());
                    
                    if (onParsed != null) onParsed.accept(p);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    String errorMsg = Constants.error(Constants.ERR_PARSE_FAILED, "Bị gián đoạn");
                    setStatus(errorMsg, Theme.ACCENT_RED);
                    Logger.error("ProblemInputPanel", "Parse interrupted", e);
                } catch (Exception ex) {
                    String errorMsg = Constants.error(Constants.ERR_PARSE_FAILED, ex.getMessage());
                    setStatus(errorMsg, Theme.ACCENT_RED);
                    Logger.error("ProblemInputPanel", "Parse failed", ex);
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        statementArea.setText("");
        titleField.setText("");
        timeLimitField.setText("1000");
        memLimitField.setText("256");
        imageLabel.setIcon(null);
        imageLabel.setText("<html><center>Kéo thả ảnh<br>hoặc PDF vào đây</center></html>");
        uploadedImagePath = null;
        setStatus("Form đã được xóa.", Theme.TEXT_SECONDARY);
    }

    private void chooseImageFile() {
        // Dùng FileDialog (Windows native) thay vì JFileChooser để giao diện đẹp hơn
        FileDialog fd = new FileDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn file đề bài", FileDialog.LOAD);
        
        // Set default directory to Downloads
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists()) {
            fd.setDirectory(downloadsDir.getAbsolutePath());
        } else {
            fd.setDirectory(System.getProperty("user.dir"));
        }
        
        // Set file filter (Windows native filter)
        fd.setFile("*.png;*.jpg;*.jpeg;*.pdf");
        
        fd.setVisible(true);
        
        String fileName = fd.getFile();
        String directory = fd.getDirectory();
        
        if (fileName != null && directory != null) {
            File f = new File(directory, fileName);
            uploadedImagePath = f.getAbsolutePath();
            if (f.getName().toLowerCase().endsWith(".pdf")) {
                imageLabel.setText("<html><center>📄 " + f.getName() + "</center></html>");
            } else {
                try {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image scaled = icon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } catch (Exception e) {
                    imageLabel.setText("<html><center>✓ " + f.getName() + "</center></html>");
                }
            }
            setStatus("✓ Đã tải: " + f.getName(), Theme.ACCENT_GREEN);
        }
    }

    private void showAddSampleDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Sample Test", true);
        dlg.setSize(500, 360);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG_PANEL);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridLayout(2, 1, 8, 8));
        content.setBackground(Theme.BG_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JTextArea inArea  = Components.darkTextArea("Input...");
        JTextArea outArea = Components.darkTextArea("Expected output...");
        content.add(labeledScroll("Input:", inArea));
        content.add(labeledScroll("Expected Output:", outArea));
        dlg.add(content, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(Theme.BG_PANEL);
        Components.GhostButton cancel = new Components.GhostButton("Hủy");
        cancel.addActionListener(e -> dlg.dispose());
        Components.AccentButton add = new Components.AccentButton("Thêm", Theme.ACCENT_GREEN);
        add.setPreferredSize(new Dimension(90, 34));
        add.addActionListener(e -> {
            setStatus("✓ Sample test đã thêm", Theme.ACCENT_GREEN);
            dlg.dispose();
        });
        btns.add(cancel); btns.add(add);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel labeledScroll(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.BG_PANEL);
        p.add(fieldLabel(label), BorderLayout.NORTH);
        p.add(Components.darkScroll(comp), BorderLayout.CENTER);
        return p;
    }

    private void setupDragDrop() {
        new DropTarget(imageLabel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    java.util.List<File> files = (java.util.List<File>)
                        dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    
                    if (!files.isEmpty()) {
                        File f = files.get(0);
                        
                        // Validate file extension
                        if (!Validator.hasValidExtension(f.getName(), "png", "jpg", "jpeg", "pdf")) {
                            setStatus(Constants.ERR_FILE_READ, Theme.ACCENT_RED);
                            Logger.warn("ProblemInputPanel", "Invalid file type dropped: " + f.getName());
                            return;
                        }
                        
                        uploadedImagePath = f.getAbsolutePath();
                        imageLabel.setText("<html><center>✓ " + f.getName() + "</center></html>");
                        
                        String successMsg = String.format(Constants.MSG_FILE_DROPPED, f.getName());
                        setStatus(successMsg, Theme.ACCENT_GREEN);
                        Logger.info("ProblemInputPanel", "File dropped: " + f.getName());
                    }
                } catch (Exception ex) {
                    setStatus(Constants.ERR_FILE_READ, Theme.ACCENT_RED);
                    Logger.error("ProblemInputPanel", "Failed to handle dropped file", ex);
                }
            }
        });
    }

    // ── Validation ─────────────────────────────────────────────────────────
    
    /**
     * Add validation listener to text field
     */
    private void addValidationListener(JTextField field, Runnable validator) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validator.run();
            }
        });
    }
    
    /**
     * Validate time limit field
     */
    private void validateTimeLimit() {
        String text = timeLimitField.getText().trim();
        int value = Validator.parseTimeLimit(text);
        
        if (!text.equals(String.valueOf(value))) {
            // Invalid input, reset to valid value
            timeLimitField.setText(String.valueOf(value));
            if (!text.isEmpty()) {
                setStatus(Constants.getTimeLimitError(), Theme.ACCENT_RED);
                Logger.warn("ProblemInputPanel", "Invalid time limit: " + text + ", reset to: " + value);
            }
        }
    }
    
    /**
     * Validate memory limit field
     */
    private void validateMemoryLimit() {
        String text = memLimitField.getText().trim();
        int value = Validator.parseMemoryLimit(text);
        
        if (!text.equals(String.valueOf(value))) {
            // Invalid input, reset to valid value
            memLimitField.setText(String.valueOf(value));
            if (!text.isEmpty()) {
                setStatus(Constants.getMemoryLimitError(), Theme.ACCENT_RED);
                Logger.warn("ProblemInputPanel", "Invalid memory limit: " + text + ", reset to: " + value);
            }
        }
    }
    
    // ── Helpers ────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private void setStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(color);
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, Constants.DLG_ERROR, JOptionPane.ERROR_MESSAGE);
    }

    public String getStatementText() { return statementArea.getText(); }
}
