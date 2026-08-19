package raven.toast;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ToastNotification extends JDialog {
    public ToastNotification(Frame owner, String title, String message, Icon icon, Runnable onView, Runnable onSkip) {
        super(owner, false);
        setUndecorated(true);
        setSize(320, 100);
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Icon & teks
        JLabel iconLabel = new JLabel(icon);
        panel.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        JLabel msgLabel = new JLabel("<html>" + message + "</html>");
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(msgLabel, BorderLayout.CENTER);
        panel.add(textPanel, BorderLayout.CENTER);

        // Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton viewBtn = new JButton("Lihat Konsultasi");
        JButton skipBtn = new JButton("Lewati");

        viewBtn.addActionListener(e -> {
            dispose();
            ToastManager.removeToast(this);
            onView.run();
        });

        skipBtn.addActionListener(e -> {
            dispose();
            ToastManager.removeToast(this);
            onSkip.run();
        });

        // Close bulat
        JButton closeBtn = new JButton("⨉");
        closeBtn.setMargin(new Insets(0, 6, 0, 6));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(Color.DARK_GRAY);
        closeBtn.setFont(new Font("Dialog", Font.BOLD, 12));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            dispose();
            ToastManager.removeToast(this);
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeBtn, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        buttonPanel.add(viewBtn);
        buttonPanel.add(skipBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        pack();
    }

    // Panel dengan sudut bulat
    static class RoundedPanel extends JPanel {
        public RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 30, 30, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        }
    }
}