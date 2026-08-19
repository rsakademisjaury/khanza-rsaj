package fungsi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KhanzaAlert extends JDialog {

    public static final int SUCCESS = 1;
    public static final int ERROR   = 2;
    public static final int WARNING = 3;
    public static final int INFO    = 4;
    public static final int QUESTION = 5;

    private boolean result = false;

    public interface Callback {
        void onYes();
        void onNo();
    }

    public KhanzaAlert(
            Frame parent,
            String title,
            String message,
            int type,
            Callback callback
    ) {

        super(parent, true);

        setUndecorated(true);
        setSize(420, 220);
        setLocationRelativeTo(parent);
        setBackground(new Color(0,0,0,0));

        Color color;
        String icon;

        switch (type) {
            case SUCCESS:
                color = new Color(34,197,94);
                icon = "✓";
                break;
            case ERROR:
                color = new Color(239,68,68);
                icon = "✕";
                break;
            case WARNING:
                color = new Color(245,158,11);
                icon = "!";
                break;
            case QUESTION:
                color = new Color(59,130,246);
                icon = "?";
                break;
            default:
                color = new Color(100,116,139);
                icon = "i";
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15,15,15,15));
        panel.setBackground(Color.WHITE);

        // ================= HEADER ICON =================
        JPanel iconPanel = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.fillOval(0,0,60,60);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2.setColor(Color.WHITE);

                FontMetrics fm = g2.getFontMetrics();
                int x = (60 - fm.stringWidth(icon))/2;
                int y = (60 - fm.getHeight())/2 + fm.getAscent();

                g2.drawString(icon, x, y);
            }
        };

        iconPanel.setPreferredSize(new Dimension(60,60));
        iconPanel.setOpaque(false);

        // ================= TEXT =================
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea lblMsg = new JTextArea(message);

        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setEditable(false);
        lblMsg.setOpaque(false);
        lblMsg.setLineWrap(true);
        lblMsg.setWrapStyleWord(true);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblMsg);

        JPanel center = new JPanel(new BorderLayout(10,10));
        center.setOpaque(false);
        center.add(iconPanel, BorderLayout.WEST);
        center.add(textPanel, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        // ================= BUTTON =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton btnOk = new JButton("OK");
        JButton btnYes = new JButton("YES");
        JButton btnNo = new JButton("NO");

        style(btnOk);
        style(btnYes);
        style(btnNo);

        if(type == QUESTION){

            btnPanel.add(btnNo);
            btnPanel.add(btnYes);

            btnYes.addActionListener(e -> {
                dispose();
                if(callback != null) callback.onYes();
            });

            btnNo.addActionListener(e -> {
                dispose();
                if(callback != null) callback.onNo();
            });

        } else {

            btnPanel.add(btnOk);

            btnOk.addActionListener(e -> dispose());
        }

        panel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void style(JButton b){
        b.setFocusPainted(false);
        b.setBackground(new Color(241,245,249));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ================= STATIC METHOD =================

    public static void show(
            Frame parent,
            String title,
            String message,
            int type
    ){
        new KhanzaAlert(parent,title,message,type,null).setVisible(true);
    }

    public static void confirm(
            Frame parent,
            String title,
            String message,
            Callback cb
    ){
        new KhanzaAlert(parent,title,message,QUESTION,cb).setVisible(true);
    }
}