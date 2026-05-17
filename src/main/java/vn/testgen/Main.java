package vn.testgen;

import vn.testgen.ui.MainFrame;
import vn.testgen.util.Logger;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize logging system
        Logger.init();
        Logger.info("Main", "TestGen application starting...");
        
        // Set system look and feel base, then override with custom
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            Logger.info("Main", "Look and feel set successfully");
        } catch (Exception e) {
            Logger.error("Main", "Failed to set look and feel", e);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                Logger.info("Main", "MainFrame created and displayed");
            } catch (Exception e) {
                Logger.error("Main", "Failed to create MainFrame", e);
                JOptionPane.showMessageDialog(null, 
                    "Lỗi khởi động ứng dụng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
