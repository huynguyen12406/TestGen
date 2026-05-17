package vn.testgen.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Reusable styled components for the TestGen UI
 */
public class Components {

    // ── Accent Button ──────────────────────────────────────────────────────
    public static class AccentButton extends JButton {
        private Color baseColor;
        private boolean hovered = false;

        public AccentButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(Theme.FONT_UI_B);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 38));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        public AccentButton(String text) { this(text, Theme.ACCENT_CYAN); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = hovered ? baseColor.brighter() : baseColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            // subtle glow
            if (hovered) {
                g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 60));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Ghost Button (outlined) ────────────────────────────────────────────
    public static class GhostButton extends JButton {
        private boolean hovered = false;

        public GhostButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(Theme.FONT_UI);
            setForeground(Theme.TEXT_SECONDARY);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 36));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  setForeground(Theme.TEXT_PRIMARY); repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; setForeground(Theme.TEXT_SECONDARY); repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color border = hovered ? Theme.ACCENT_CYAN : Theme.BG_BORDER;
            g2.setColor(hovered ? Theme.BG_HOVER : Theme.BG_PANEL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Dark Text Area ─────────────────────────────────────────────────────
    public static JTextArea darkTextArea(String placeholder) {
        JTextArea ta = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(Theme.FONT_MONO);
                    g2.drawString(placeholder, getInsets().left + 4, getInsets().top + 17);
                    g2.dispose();
                }
            }
        };
        ta.setBackground(Theme.BG_INPUT);
        ta.setForeground(Theme.TEXT_PRIMARY);
        ta.setCaretColor(Theme.ACCENT_CYAN);
        ta.setFont(Theme.FONT_MONO);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        ta.setSelectionColor(new Color(Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(), Theme.ACCENT_CYAN.getBlue(), 60));
        return ta;
    }

    // ── Styled TextField ───────────────────────────────────────────────────
    public static JTextField darkTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(Theme.FONT_UI);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 2, getHeight()/2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setBackground(Theme.BG_INPUT);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setCaretColor(Theme.ACCENT_CYAN);
        tf.setFont(Theme.FONT_UI);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BG_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    // ── Dark Combo Box ─────────────────────────────────────────────────────
    public static <T> JComboBox<T> darkCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.FONT_UI);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? Theme.ACCENT_CYAN.darker().darker() : Theme.BG_INPUT);
                setForeground(Theme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        return cb;
    }

    // ── Section Header Label ───────────────────────────────────────────────
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_H2);
        lbl.setForeground(Theme.ACCENT_CYAN);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    // ── Card Panel ─────────────────────────────────────────────────────────
    public static JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 12)); // Tăng gap từ 10 lên 12
        p.setBackground(Theme.BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BG_BORDER, 1),
            BorderFactory.createEmptyBorder(14, 16, 16, 16) // Tăng bottom padding từ 14 lên 16
        ));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = sectionLabel(title);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0)); // Thêm margin bottom cho title
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    // ── Verdict Badge Label ────────────────────────────────────────────────
    public static JLabel verdictBadge(String verdict) {
        JLabel lbl = new JLabel(verdict, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = Theme.verdictColor(verdict);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(Theme.FONT_MONO_B);
        lbl.setForeground(Theme.verdictColor(verdict));
        lbl.setOpaque(false);
        lbl.setPreferredSize(new Dimension(54, 24));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return lbl;
    }

    // ── Spinner (loading indicator via animated label) ─────────────────────
    public static JLabel spinnerLabel() {
        JLabel lbl = new JLabel("⟳");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lbl.setForeground(Theme.ACCENT_CYAN);
        return lbl;
    }

    // ── Horizontal Separator ───────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BG_BORDER);
        sep.setBackground(Theme.BG_DARKEST);
        return sep;
    }

    // ── Styled Scroll Pane ─────────────────────────────────────────────────
    public static JScrollPane darkScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(Theme.BG_INPUT);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        sp.getViewport().setBackground(Theme.BG_INPUT);
        sp.getVerticalScrollBar().setBackground(Theme.BG_INPUT);
        sp.getHorizontalScrollBar().setBackground(Theme.BG_INPUT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }
}
