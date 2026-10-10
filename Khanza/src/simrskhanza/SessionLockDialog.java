package simrskhanza;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.BasicStroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

/**
 * Pengunci ringkas memakai komponen Swing standar dan ikon milik login awal.
 * Jendela tetap opaque/unshaped; wallpaper blur opsional berasal dari login,
 * bukan screenshot data pasien. Komponen DlgLogin tidak dipindahkan.
 */
final class SessionLockDialog extends JDialog {
    // RGB dari LoginBackgroundPanel pada frmUtama.
    private static final Color BLUE = new Color(7, 82, 145);
    private static final Color BLUE_BOTTOM = new Color(28, 148, 201);
    private static final Color FIELD = new Color(247, 250, 252);
    private static final Color BORDER = new Color(180, 200, 213);
    private static final Color INK = new Color(35, 57, 72);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final String PLACEHOLDER = "Masukkan password disini";
    private static final int PADDING = 16;
    private static final int FRAME = 8;
    private static final int SIDE = 92;
    private static final int BUTTON_GAP = 10;
    private static final int FIELD_HEIGHT = 34;
    private final JLabel username = new JLabel();
    private final JPasswordField password = new PlaceholderPasswordField();
    private final JButton open = new SessionButton("Buka Sesi", true);
    private final JButton switchUser = new SessionButton("Ganti User", false);
    private final JToggleButton reveal = new JToggleButton();
    private final JTextArea status = new JTextArea();
    private final JPanel actions = new JPanel(new java.awt.GridLayout(1, 2, BUTTON_GAP, 0));
    private final JPanel passwordContainer;
    private final JPanel right = new JPanel(new GridBagLayout());
    private final JLabel timeoutIcon = new JLabel(loadTimeoutIcon(), SwingConstants.CENTER);
    private BufferedImage backdrop;

    SessionLockDialog(Window owner, Icon userIcon, Icon keyIcon,
            Icon viewIcon, Icon hideIcon, Runnable onOpen, Runnable onSwitchUser) {
        super(owner, "Sesi SIMRS terkunci", Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setBackground(SessionIdleLock.LOCK_BACKGROUND);
        CardPanel card = new CardPanel();
        card.setLayout(new java.awt.BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(FRAME, FRAME, FRAME, FRAME));
        JPanel left = new JPanel(new java.awt.BorderLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(SIDE, 1));
        timeoutIcon.getAccessibleContext().setAccessibleName("Sesi timeout");
        left.add(timeoutIcon, java.awt.BorderLayout.CENTER);
        card.add(left, java.awt.BorderLayout.WEST);
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        card.add(right, java.awt.BorderLayout.CENTER);
        setContentPane(card);

        // JLabel tidak punya input/caret: identitas akun cukup ikon dan teks.
        // Bentuk ikon tetap dari resource login; hanya warnanya diselaraskan dengan teks pada panel putih.
        username.setIcon(tintIcon(userIcon, INK));
        username.putClientProperty("login.sourceIcon", userIcon);
        username.setIconTextGap(8);
        username.setFont(FONT);
        username.setForeground(INK);
        username.setOpaque(false);
        username.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        username.getAccessibleContext().setAccessibleName("Username sesi terkunci");
        username.setToolTipText("Gunakan Ganti User untuk masuk dengan akun lain");
        addRow(right, username, 0, 10);

        password.setUI(new BasicPasswordFieldUI());
        password.setFont(FONT);
        password.setForeground(INK);
        password.setCaretColor(INK);
        password.setOpaque(false);
        password.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        password.setEchoChar('*');
        password.getAccessibleContext().setAccessibleName("Password untuk membuka sesi");
        password.getAccessibleContext().setAccessibleDescription(PLACEHOLDER);
        password.putClientProperty("session.placeholder", PLACEHOLDER);
        password.setToolTipText("Masukkan password akun yang sedang terkunci");
        password.addActionListener(evt -> open.doClick());
        passwordContainer = passwordPanel(keyIcon, viewIcon, hideIcon);
        addRow(right, passwordContainer, 1, 14);

        actions.setOpaque(false);
        open.addActionListener(evt -> onOpen.run());
        switchUser.addActionListener(evt -> onSwitchUser.run());
        switchUser.setToolTipText("Logout dan masuk dengan akun lain tanpa menutup SIMRS");
        actions.add(open);
        actions.add(switchUser);

        status.setFont(FONT.deriveFont(11f));
        status.setForeground(new Color(166, 48, 47));
        status.setEditable(false);
        status.setFocusable(false);
        status.setOpaque(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setVisible(false);
        // Tinggi dialog mengikuti isian; tidak ada ruang ekstra untuk pemusatan.
        addRow(right, status, 2, 10);
        addRow(right, actions, 3, 0);
        getRootPane().setDefaultButton(open);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent event) {
                SwingUtilities.invokeLater(() -> password.requestFocusInWindow());
            }
        });
        updateDialogSize();
    }

    private JPanel passwordPanel(Icon keyIcon, Icon viewIcon, Icon hideIcon) {
        JPanel panel = new JPanel(new java.awt.BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(FIELD);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } finally {
                    g.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(password.isFocusOwner() ? BLUE_BOTTOM : BORDER);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                } finally {
                    g.dispose();
                }
            }
        };
        panel.setOpaque(false);
        // Sisakan satu piksel atas/bawah supaya child tidak menimpa garis.
        panel.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
        JLabel prefix = new JLabel(keyIcon, SwingConstants.CENTER);
        prefix.setOpaque(false);
        prefix.setPreferredSize(new Dimension(24, FIELD_HEIGHT));
        panel.add(prefix, java.awt.BorderLayout.WEST);
        panel.add(password, java.awt.BorderLayout.CENTER);

        reveal.setUI(new BasicButtonUI());
        reveal.setIcon(viewIcon);
        reveal.setSelectedIcon(hideIcon);
        reveal.setContentAreaFilled(false);
        reveal.setOpaque(false);
        reveal.setBorderPainted(false);
        reveal.setMargin(new Insets(0, 0, 0, 0));
        reveal.setPreferredSize(new Dimension(26, FIELD_HEIGHT));
        reveal.setToolTipText("Tampilkan/sembunyikan password");
        reveal.getAccessibleContext().setAccessibleName("Tampilkan password");
        reveal.addActionListener(evt -> {
            password.setEchoChar(reveal.isSelected() ? (char) 0 : '*');
            password.requestFocusInWindow();
        });
        panel.add(reveal, java.awt.BorderLayout.EAST);
        // Repaint field transparan hanya mencakup area teks. Perubahan fokus
        // harus mengecat seluruh panel, termasuk garis di sekitar kedua ikon.
        password.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                panel.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                panel.repaint();
            }
        });
        return panel;
    }

    private static void addRow(JPanel parent, java.awt.Component component,
            int row, int bottom) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, bottom, 0);
        parent.add(component, c);
    }

    /** Pewarnaan sekali pada bitmap ikon asli, bukan menggambar ulang simbol. */
    private static Icon tintIcon(Icon source, Color color) {
        if (source == null) {
            return null;
        }
        int width = Math.max(1, source.getIconWidth());
        int height = Math.max(1, source.getIconHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            source.paintIcon(null, g, 0, 0);
        } finally {
            g.dispose();
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = image.getRGB(x, y) & 0xff000000;
                image.setRGB(x, y, alpha | (color.getRGB() & 0x00ffffff));
            }
        }
        return new ImageIcon(image);
    }

    void prepare(String user) {
        username.setText(user);
        clearPassword();
        setBusy(false);
        setStatus(" ");
    }

    String getUsername() {
        return username.getText();
    }

    char[] getPassword() {
        return password.getPassword();
    }

    void clearPassword() {
        password.setText("");
        reveal.setSelected(false);
        password.setEchoChar('*');
    }

    void focusPassword() {
        password.requestFocusInWindow();
    }

    void setStatus(String text) {
        status.setText(text == null ? "" : text);
        status.setVisible(!status.getText().trim().isEmpty());
        updateDialogSize();
    }

    void setBusy(boolean busy) {
        open.setEnabled(!busy);
        open.setText(busy ? "Memeriksa..." : "Buka Sesi");
        password.setEditable(!busy);
        reveal.setEnabled(!busy);
        // Ganti User tersedia saat DB lambat; hasil worker lama diabaikan.
        updateDialogSize();
    }

    /** Tinggi mengikuti isian agar margin atas/bawah sama dengan margin sisi. */
    private void updateDialogSize() {
        int minimumInput = Math.max(
                open.getPreferredSize().width + BUTTON_GAP + switchUser.getPreferredSize().width,
                password.getFontMetrics(FONT.deriveFont(12f)).stringWidth(PLACEHOLDER) + 82);
        int units = (Math.max(384, 2 * FRAME + SIDE + 2 * PADDING + minimumInput) + 15) / 16;
        int baseHeight = 2 * FRAME + 2 * PADDING + 22 + 10 + FIELD_HEIGHT + 14
                + open.getPreferredSize().height;
        int width = units * 16 - 2 * FRAME - SIDE - 2 * PADDING;
        int height = baseHeight + (status.isVisible() ? messageHeight(width) + 10 : 0);
        username.setPreferredSize(new Dimension(width, 22));
        passwordContainer.setPreferredSize(new Dimension(width, FIELD_HEIGHT));
        passwordContainer.setMinimumSize(new Dimension(width, FIELD_HEIGHT));
        status.setPreferredSize(new Dimension(width, messageHeight(width)));
        getContentPane().setPreferredSize(new Dimension(units * 16, height));
        pack();
        if (getOwner() != null && getOwner().isShowing()) {
            setLocationRelativeTo(getOwner());
        }
    }

    private int messageHeight(int width) {
        java.awt.FontMetrics metrics = status.getFontMetrics(status.getFont());
        int lines = 1;
        String line = "";
        for (String word : status.getText().trim().split("\\s+")) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && metrics.stringWidth(next) > width - 2) {
                lines++;
                line = word;
            } else {
                line = next;
            }
        }
        return lines * metrics.getHeight() + 2;
    }

    /** Petunjuk digambar hanya saat kosong; dokumen password tetap kosong. */
    private static final class PlaceholderPasswordField extends JPasswordField {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getDocument().getLength() != 0) {
                return;
            }
            Object hint = getClientProperty("session.placeholder");
            if (hint == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                Insets insets = getInsets();
                g.clipRect(insets.left, insets.top,
                        getWidth() - insets.left - insets.right,
                        getHeight() - insets.top - insets.bottom);
                g.setFont(getFont().deriveFont(12f));
                g.setColor(new Color(119, 137, 149));
                java.awt.FontMetrics metrics = g.getFontMetrics();
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g.drawString(hint.toString(), insets.left, y);
            } finally {
                g.dispose();
            }
        }
    }

    void setBackdrop(BufferedImage image) {
        backdrop = image;
        getContentPane().repaint();
    }

    private static Icon loadTimeoutIcon() {
        java.net.URL resource = SessionLockDialog.class.getResource("/picture/session_timeout.png");
        return resource == null ? null : new ImageIcon(resource);
    }

    private final class CardPanel extends JPanel {
        CardPanel() {
            setOpaque(true);
            setBackground(SessionIdleLock.LOCK_BACKGROUND);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            SessionLockBackdrop.paint(graphics, this, backdrop);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D shape = new RoundRectangle2D.Double(
                        FRAME, FRAME, getWidth() - 2 * FRAME - 1, getHeight() - 2 * FRAME - 1, 24, 24);
                g.setColor(Color.WHITE);
                g.fill(shape);
                java.awt.Shape previousClip = g.getClip();
                g.clip(shape);
                g.setPaint(new GradientPaint(FRAME, FRAME, BLUE, FRAME,
                        getHeight() - FRAME, BLUE_BOTTOM));
                g.fillRect(FRAME, FRAME, SIDE, getHeight() - 2 * FRAME);
                g.setClip(previousClip);
                g.setColor(BORDER);
                g.setStroke(new BasicStroke(1f));
                g.draw(shape);
            } finally {
                g.dispose();
            }
        }
    }

    /** Tombol flat, hover dan pressed memakai palet biru login. */
    private static final class SessionButton extends JButton {
        private final boolean primary;

        SessionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setUI(new BasicButtonUI());
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            setForeground(primary ? Color.WHITE : BLUE);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            int textWidth = getFontMetrics(getFont()).stringWidth(getText());
            return new Dimension(textWidth + 32, 30);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            ButtonModel model = getModel();
            Color fill = primary ? BLUE : Color.WHITE;
            if (!model.isEnabled()) {
                fill = new Color(190, 201, 209);
            } else if (model.isPressed() && model.isArmed()) {
                fill = primary ? new Color(5, 67, 119) : new Color(219, 235, 245);
            } else if (model.isRollover()) {
                fill = primary ? BLUE_BOTTOM : new Color(238, 247, 252);
            }
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.setColor(primary ? fill : BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (isFocusOwner()) {
                    g.setColor(primary ? Color.WHITE : BLUE_BOTTOM);
                    g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 6, 6);
                }
            } finally {
                g.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
