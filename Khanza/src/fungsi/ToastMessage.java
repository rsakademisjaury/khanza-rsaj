package fungsi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ToastMessage extends JDialog {

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int INFO = 4;

    private float opacity = 0f;

    public ToastMessage(
            Frame parent,
            String message,
            int type,
            int autoHide) {

        super(parent, false);

        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        Color iconColor;

        switch (type) {
            case SUCCESS:
                iconColor = new Color(34, 197, 94);
                break;
            case ERROR:
                iconColor = new Color(239, 68, 68);
                break;
            case WARNING:
                iconColor = new Color(245, 158, 11);
                break;
            default:
                iconColor = new Color(59, 130, 246);
                break;
        }

        JPanel panel = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                for (int i = 12; i >= 1; i--) {

                    g2.setColor(new Color(0, 0, 0, 8));

                    g2.fillRoundRect(
                            i,
                            i,
                            getWidth() - (i * 2),
                            getHeight() - (i * 2),
                            30,
                            30);
                }

                g2.setColor(new Color(255, 255, 255, 235));

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth() - 12,
                        getHeight() - 12,
                        30,
                        30);

                g2.dispose();
            }
        };

        panel.setOpaque(false);

        panel.setBorder(new EmptyBorder(
                20,
                25,
                20,
                25));

        // ==================================
        // HEADER
        // ==================================

        JPanel header = new JPanel(
                new BorderLayout());

        header.setOpaque(false);

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS));

        JLabel lblTitle =
                new JLabel("SIMRS Khanza");

        lblTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24));

        lblTitle.setForeground(
                new Color(30, 41, 59));

        JLabel lblSub =
                new JLabel("Pesan System");

        lblSub.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15));

        lblSub.setForeground(
                new Color(100, 116, 139));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        JButton btnClose =
                new JButton("✕");

        btnClose.setPreferredSize(
                new Dimension(42, 42));

        btnClose.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18));

        btnClose.setForeground(
                new Color(71, 85, 105));

        btnClose.setBorder(null);

        btnClose.setFocusPainted(false);

        btnClose.setContentAreaFilled(false);

        btnClose.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR));

        btnClose.addMouseListener(
                new MouseAdapter() {

            @Override
            public void mouseEntered(
                    MouseEvent e) {

                btnClose.setOpaque(true);

                btnClose.setBackground(
                        new Color(
                                241,
                                245,
                                249));
            }

            @Override
            public void mouseExited(
                    MouseEvent e) {

                btnClose.setOpaque(false);
            }
        });

        btnClose.addActionListener(
                e -> fadeOut());

        header.add(
                titlePanel,
                BorderLayout.WEST);

        header.add(
                btnClose,
                BorderLayout.EAST);

        panel.add(
                header,
                BorderLayout.NORTH);

        // ==================================
        // CONTENT
        // ==================================

        JPanel content =
                new JPanel(
                        new BorderLayout(
                                20,
                                0));

        content.setOpaque(false);

        content.setBorder(
                new EmptyBorder(
                        20,
                        0,
                        0,
                        0));

        JPanel iconPanel =
                new JPanel() {

            @Override
            protected void paintComponent(
                    Graphics g) {

                Graphics2D g2 =
                        (Graphics2D) g;

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(iconColor);

                g2.fillOval(
                        0,
                        0,
                        60,
                        60);

                g2.setColor(
                        Color.WHITE);

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                32));

                String s;

                if (type == SUCCESS) {
                    s = "✓";
                } else if (type == ERROR) {
                    s = "✕";
                } else if (type == WARNING) {
                    s = "!";
                } else {
                    s = "i";
                }

                FontMetrics fm =
                        g2.getFontMetrics();

                int x =
                        (60 - fm.stringWidth(s)) / 2;

                int y =
                        ((60 - fm.getHeight()) / 2)
                        + fm.getAscent();

                g2.drawString(
                        s,
                        x,
                        y);
            }
        };

        iconPanel.setOpaque(false);

        iconPanel.setPreferredSize(
                new Dimension(
                        60,
                        60));

        JPanel textPanel =
                new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS));

        JLabel lblStatus =
                new JLabel(
                        type == ERROR
                        ? "Gagal"
                        : type == SUCCESS
                        ? "Berhasil"
                        : type == WARNING
                        ? "Peringatan"
                        : "Informasi");

        lblStatus.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22));

        lblStatus.setForeground(
                new Color(
                        15,
                        23,
                        42));

        JLabel lblMessage =
                new JLabel(
                        "<html><div style='width:500px;'>"
                        + message
                        + "</div></html>");

        lblMessage.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        16));

        lblMessage.setForeground(
                new Color(
                        51,
                        65,
                        85));

        textPanel.add(lblStatus);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(lblMessage);

        content.add(
                iconPanel,
                BorderLayout.WEST);

        content.add(
                textPanel,
                BorderLayout.CENTER);

        panel.add(
                content,
                BorderLayout.CENTER);

        setContentPane(panel);

        setSize(600, 220);

        Dimension screen =
                Toolkit.getDefaultToolkit()
                        .getScreenSize();

        setLocation(
                (screen.width - getWidth()) / 2,
                40);

        setOpacity(0f);

        Timer fadeIn =
                new Timer(
                        15,
                        null);

        fadeIn.addActionListener(
                e -> {

            opacity += 0.05f;

            if (opacity >= 1f) {

                opacity = 1f;

                fadeIn.stop();
            }

            setOpacity(opacity);
        });

        fadeIn.start();

        int delay = 0;

        switch (autoHide) {
            case 1:
                delay = 3000;
                break;
            case 2:
                delay = 5000;
                break;
            case 3:
                delay = 8000;
                break;
            case 4:
                delay = 10000;
                break;
        }

        if (delay > 0) {

            Timer timer =
                    new Timer(
                            delay,
                            e -> fadeOut());

            timer.setRepeats(false);

            timer.start();
        }
    }

    private void fadeOut() {

        Timer fade =
                new Timer(
                        15,
                        null);

        fade.addActionListener(
                e -> {

            opacity -= 0.05f;

            if (opacity <= 0f) {

                opacity = 0f;

                fade.stop();

                dispose();
            }

            setOpacity(opacity);
        });

        fade.start();
    }

    public static void showToast(
            Frame parent,
            String message,
            int type,
            int autoHide) {

        SwingUtilities.invokeLater(() -> {

            ToastMessage toast =
                    new ToastMessage(
                            parent,
                            message,
                            type,
                            autoHide);

            toast.setVisible(true);
        });
    }
}