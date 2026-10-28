package fungsi;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Komponen yang sama untuk CAPTCHA login awal dan pembukaan sesi. */
public final class LoginCaptchaPanel extends JPanel {
    public static final int PANEL_HEIGHT = 41;
    private boolean loginStyle;
    private final JLabel image = new JLabel() {
        @Override protected void paintComponent(java.awt.Graphics graphics) {
            if (!(getIcon() instanceof ImageIcon)) return;
            ImageIcon icon = (ImageIcon) getIcon();
            double scale = Math.min((double) getWidth() / icon.getIconWidth(),
                    (double) getHeight() / icon.getIconHeight());
            int width = Math.max(1, (int) Math.round(icon.getIconWidth() * scale));
            int height = Math.max(1, (int) Math.round(icon.getIconHeight() * scale));
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            try {
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(icon.getImage(), (getWidth() - width) / 2,
                        (getHeight() - height) / 2, width, height, null);
            } finally { g.dispose(); }
        }
    };
    private final JTextField answer = new JTextField() {
        @Override protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            try {
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(loginStyle ? new Color(255, 255, 255, 244) : new Color(247, 250, 252));
                int arc = loginStyle ? getHeight() : 10;
                g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            } finally { g.dispose(); }
            super.paintComponent(graphics);
            if (getText().isEmpty()) {
                g = (java.awt.Graphics2D) graphics.create();
                try {
                    g.setFont(getFont().deriveFont(loginStyle ? 14f : 12f));
                    g.setColor(loginStyle ? new Color(132, 141, 149) : new Color(119, 137, 149));
                    String hint = "Kode";
                    java.awt.FontMetrics metrics = g.getFontMetrics();
                    g.drawString(hint, (getWidth() - metrics.stringWidth(hint)) / 2,
                            (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                } finally { g.dispose(); }
            }
        }
        @Override protected void paintBorder(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            try {
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(loginStyle
                        ? (isFocusOwner() ? new Color(117, 220, 247, 230) : new Color(255, 255, 255, 110))
                        : (isFocusOwner() ? new Color(28, 148, 201) : new Color(180, 200, 213)));
                g.setStroke(new java.awt.BasicStroke(isFocusOwner() ? 1.5f : 1f));
                int arc = loginStyle ? getHeight() : 10;
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            } finally { g.dispose(); }
        }
    };
    private final JButton refresh = new JButton();
    private String challengeId = "";
    private Runnable onRefresh = () -> { };
    private Runnable onSubmit = () -> { };

    public LoginCaptchaPanel() {
        super(new GridBagLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(258, PANEL_HEIGHT));
        image.setPreferredSize(new Dimension(134, 36));
        image.setMinimumSize(new Dimension(112, 28));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.getAccessibleContext().setAccessibleName("Gambar kode CAPTCHA");
        refresh.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        refresh.setOpaque(false);
        refresh.setContentAreaFilled(false);
        refresh.setBorderPainted(false);
        refresh.setRolloverEnabled(true);
        java.net.URL icon = LoginCaptchaPanel.class.getResource("/picture/refresh1.png");
        if (icon != null) refresh.setIcon(new ImageIcon(icon));
        refresh.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        refresh.getAccessibleContext().setAccessibleName("Ganti gambar CAPTCHA");
        refresh.setPreferredSize(new Dimension(24, 34));
        refresh.setMargin(new java.awt.Insets(0, 0, 0, 0));
        refresh.setFocusPainted(false);
        refresh.setToolTipText("Ganti gambar CAPTCHA");
        refresh.addActionListener(event -> onRefresh.run());
        answer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        answer.setOpaque(false);
        answer.setForeground(new Color(35, 57, 72));
        answer.setCaretColor(new Color(35, 57, 72));
        answer.setHorizontalAlignment(SwingConstants.CENTER);
        answer.setToolTipText("Ketik 5 karakter pada gambar. Huruf besar/kecil dianggap sama.");
        answer.getAccessibleContext().setAccessibleName("Jawaban CAPTCHA");
        answer.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
        ((AbstractDocument) answer.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text,
                    AttributeSet attrs) throws BadLocationException {
                replace(bypass, offset, 0, text, attrs);
            }
            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text,
                    AttributeSet attrs) throws BadLocationException {
                String normalized = (text == null ? "" : text)
                        .toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]", "");
                int available = LoginCaptcha.LENGTH - (bypass.getDocument().getLength() - length);
                if (normalized.length() > available) normalized = normalized.substring(0, Math.max(0, available));
                super.replace(bypass, offset, length, normalized, attrs);
            }
        });
        answer.addActionListener(event -> onSubmit.run());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new java.awt.Insets(0, 0, 0, 6);
        add(answer, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(image, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        add(refresh, c);
        updateAnswerWidth();
        setVisible(false);
    }

    /** Login awal memakai tema pill yang sama dengan field password. */
    public void setLoginStyle() {
        loginStyle = true;
        answer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        answer.setForeground(new Color(38, 50, 61));
        answer.setCaretColor(new Color(8, 105, 171));
        answer.setSelectionColor(new Color(178, 224, 246));
        answer.setSelectedTextColor(new Color(25, 44, 58));
        updateAnswerWidth();
        revalidate();
        repaint();
    }

    private void updateAnswerWidth() {
        java.awt.FontMetrics metrics = answer.getFontMetrics(answer.getFont());
        int widest = 0;
        for (char letter : "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()) {
            widest = Math.max(widest, metrics.charWidth(letter));
        }
        Dimension size = new Dimension(Math.max(76, widest * LoginCaptcha.LENGTH + 20),
                36);
        answer.setPreferredSize(size);
        answer.setMinimumSize(size);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (image.getWidth() <= 0 || image.getHeight() <= 0) return;
        // Ikuti tinggi gambar yang benar-benar terlihat setelah diskalakan.
        double scale = Math.min((double) image.getWidth() / LoginCaptcha.WIDTH,
                (double) image.getHeight() / LoginCaptcha.HEIGHT);
        int height = Math.max(1, (int) Math.round(LoginCaptcha.HEIGHT * scale));
        answer.setBounds(answer.getX(), image.getY() + (image.getHeight() - height) / 2,
                answer.getWidth(), height);
    }

    public void setOnRefresh(Runnable action) { onRefresh = action; }
    public void setOnSubmit(Runnable action) { onSubmit = action; }
    public void setChallenge(LoginCaptcha.Challenge challenge) {
        challengeId = challenge == null ? "" : challenge.getId();
        image.setIcon(challenge == null ? null : new ImageIcon(challenge.getImage()));
        answer.setText("");
        setVisible(challenge != null);
    }
    public String getChallengeId() { return challengeId; }
    public String getAnswer() { return answer.getText(); }
    public void focusAnswer() { answer.requestFocusInWindow(); }
    public void setBusy(boolean busy) {
        answer.setEditable(!busy);
        refresh.setEnabled(!busy);
    }
}
