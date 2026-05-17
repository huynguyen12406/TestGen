package vn.testgen.ui;

import java.awt.*;

/**
 * Centralized theme constants - Dark IDE-style theme with cyan accents
 * Inspired by competitive programming judges (Codeforces, ICPC-tools)
 */
public class Theme {
    // ── Background Layers ──────────────────────────────────────────────────
    public static final Color BG_DARKEST   = new Color(0x0D1117);  // window bg
    public static final Color BG_PANEL     = new Color(0x161B22);  // card/panel
    public static final Color BG_INPUT     = new Color(0x1C2333);  // inputs, editors
    public static final Color BG_HOVER     = new Color(0x21262D);  // hover state
    public static final Color BG_BORDER    = new Color(0x30363D);  // borders

    // ── Accent Colors ──────────────────────────────────────────────────────
    public static final Color ACCENT_CYAN   = new Color(0x00D4FF);  // primary accent
    public static final Color ACCENT_PURPLE = new Color(0x7C3AED);  // secondary
    public static final Color ACCENT_GREEN  = new Color(0x3FB950);  // success / AC
    public static final Color ACCENT_RED    = new Color(0xF85149);  // error / WA
    public static final Color ACCENT_YELLOW = new Color(0xD29922);  // warning / TLE
    public static final Color ACCENT_ORANGE = new Color(0xF0883E);  // MLE

    // ── Text ───────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0xE6EDF3);
    public static final Color TEXT_SECONDARY = new Color(0x8B949E);
    public static final Color TEXT_MUTED     = new Color(0x484F58);
    public static final Color TEXT_CODE      = new Color(0x79C0FF);

    // ── Fonts ──────────────────────────────────────────────────────────────
    public static final Font FONT_MONO   = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_MONO_B = new Font("Monospaced", Font.BOLD,  13);
    public static final Font FONT_UI     = new Font("SansSerif",  Font.PLAIN, 13);
    public static final Font FONT_UI_B   = new Font("SansSerif",  Font.BOLD,  13);
    public static final Font FONT_TITLE  = new Font("SansSerif",  Font.BOLD,  18);
    public static final Font FONT_H2     = new Font("SansSerif",  Font.BOLD,  15);
    public static final Font FONT_SMALL  = new Font("SansSerif",  Font.PLAIN, 11);

    // ── Verdict badge colors ───────────────────────────────────────────────
    public static Color verdictColor(String verdict) {
        if (verdict == null) return TEXT_SECONDARY;
        return switch (verdict.toUpperCase()) {
            case "AC"  -> ACCENT_GREEN;
            case "WA"  -> ACCENT_RED;
            case "TLE" -> ACCENT_YELLOW;
            case "MLE" -> ACCENT_ORANGE;
            case "CE"  -> new Color(0xA371F7);
            case "RE"  -> new Color(0xFF7B72);
            default    -> TEXT_SECONDARY;
        };
    }
}
