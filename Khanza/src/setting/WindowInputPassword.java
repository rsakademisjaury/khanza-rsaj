/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package setting;

import fungsi.PasswordPolicy;
import fungsi.AccountPasswordService;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;

/**
 *
 * @author windiartonugroho
 */
public class WindowInputPassword extends javax.swing.JDialog {
    private final validasi Valid=new validasi();
    private final sekuel Sequel=new sekuel();
    private String user="";
    private AccountPasswordService.Account account = AccountPasswordService.Account.USER;
    
    /**
     * Creates new form WindowInputPassword
     */
    public WindowInputPassword(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        PasswordPolicy.configure(PassLama);
        PasswordPolicy.configure(Passbaru1);
        PasswordPolicy.configure(PassBaru2);
        pasangTampilanModern();
    }

    /** Tampilan baru memakai field dan listener simpan/tutup yang sudah ada. */
    private void pasangTampilanModern() {
        javax.swing.JPanel view = buatTampilanModern(PassLama, Passbaru1, PassBaru2,
                BtnSimpanPass, BtnClosePass);
        setContentPane(view);
        getRootPane().setBorder(javax.swing.BorderFactory.createEmptyBorder());
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent event) { bulatkanWindow(); }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent event) { bulatkanWindow(); }
        });
        getRootPane().setDefaultButton(BtnSimpanPass);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "password.close");
        getRootPane().getActionMap().put("password.close", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) { BtnClosePass.doClick(); }
        });
        java.awt.event.MouseAdapter drag = new java.awt.event.MouseAdapter() {
            private java.awt.Point mulai, posisi;
            @Override public void mousePressed(java.awt.event.MouseEvent event) {
                mulai = event.getLocationOnScreen(); posisi = getLocation();
            }
            @Override public void mouseDragged(java.awt.event.MouseEvent event) {
                if (mulai == null) return;
                java.awt.Point sekarang = event.getLocationOnScreen();
                setLocation(posisi.x + sekarang.x - mulai.x, posisi.y + sekarang.y - mulai.y);
            }
        };
        pasangDrag((java.awt.Component) view.getClientProperty("password.header"), drag);
        pack();
        bulatkanWindow();
        setLocationRelativeTo(getOwner());
    }

    /** Memotong WINDOW asli, bukan hanya panel, agar tidak ada sudut putih kotak. */
    private void bulatkanWindow() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        try {
            java.awt.GraphicsConfiguration config = getGraphicsConfiguration();
            if (config != null && config.getDevice().isWindowTranslucencySupported(
                    java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
                setShape(bentukWindow(getWidth(), getHeight()));
            } else if (config != null && config.isTranslucencyCapable()) {
                // Alternatif untuk desktop yang mendukung alpha tetapi tidak mendukung window shape.
                setBackground(new java.awt.Color(0, 0, 0, 0));
                getRootPane().setOpaque(false);
                getLayeredPane().setOpaque(false);
            }
        } catch (UnsupportedOperationException | SecurityException exception) {
            // Desktop lama tetap dapat membuka form dan menyimpan password.
            java.util.logging.Logger.getLogger(WindowInputPassword.class.getName())
                    .log(java.util.logging.Level.FINE, "Desktop tidak mendukung sudut window transparan.", exception);
        }
    }

    static java.awt.Shape bentukWindow(int width, int height) {
        return new java.awt.geom.RoundRectangle2D.Double(0, 0, width, height, 24, 24);
    }

    private static void pasangDrag(java.awt.Component component, java.awt.event.MouseAdapter listener) {
        if (component instanceof javax.swing.AbstractButton) return;
        component.addMouseListener(listener); component.addMouseMotionListener(listener);
        if (component instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) component).getComponents()) pasangDrag(child, listener);
        }
    }

    /** Dapat dirender tanpa window untuk pemeriksaan ukuran dan kontras. */
    static javax.swing.JPanel buatTampilanModern(javax.swing.JPasswordField lama,
            javax.swing.JPasswordField baru, javax.swing.JPasswordField konfirmasi,
            javax.swing.JButton simpan, javax.swing.JButton batal) {
        RoundPanel card = new RoundPanel(24, java.awt.Color.WHITE, new java.awt.Color(209, 224, 237));
        card.setName("PasswordModernRoot");
        card.setLayout(new java.awt.BorderLayout());
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0)) {
            @Override protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setPaint(new java.awt.GradientPaint(0, 0, new java.awt.Color(9, 89, 153),
                        getWidth(), getHeight(), new java.awt.Color(13, 134, 202)));
                g.fillRect(0, 0, getWidth(), getHeight()); g.dispose();
            }
        };
        header.setName("PasswordHeader");
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(22, 22, 22, 18));
        RoundPanel badge = new RoundPanel(12, new java.awt.Color(255, 255, 255, 34), null);
        badge.setLayout(new java.awt.GridBagLayout()); badge.setPreferredSize(new java.awt.Dimension(46, 46));
        badge.add(new javax.swing.JLabel(ikonPng("shield-white", 28)));
        header.add(badge, java.awt.BorderLayout.WEST);
        javax.swing.JPanel heading = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        javax.swing.JLabel title = label("Ganti Password", 21, true, java.awt.Color.WHITE);
        javax.swing.JLabel subtitle = label("Jaga keamanan akses SIMRS Anda.", 11, false, new java.awt.Color(224, 243, 255));
        heading.add(title); heading.add(subtitle); header.add(heading, java.awt.BorderLayout.CENTER);
        javax.swing.JButton tutup = new javax.swing.JButton(ikonPng("close-white", 16));
        tutup.setName("PasswordClose"); tutup.setToolTipText("Tutup (Esc)");
        tutup.getAccessibleContext().setAccessibleName("Tutup form ganti password");
        tombolIkon(tutup); tutup.setPreferredSize(new java.awt.Dimension(26, 26));
        tutup.addActionListener(event -> batal.doClick());
        javax.swing.JPanel closeBox = new javax.swing.JPanel(new java.awt.BorderLayout()); closeBox.setOpaque(false);
        closeBox.add(tutup, java.awt.BorderLayout.NORTH); header.add(closeBox, java.awt.BorderLayout.EAST);
        card.add(header, java.awt.BorderLayout.NORTH); card.putClientProperty("password.header", header);

        javax.swing.JPanel body = new javax.swing.JPanel(new java.awt.GridBagLayout());
        body.setOpaque(false); body.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 24, 24));
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        lama.setToolTipText("Ketik password yang Anda gunakan saat ini.");
        String[] titles = {"Password saat ini", "Password baru", "Konfirmasi password baru"};
        String[] names = {"PasswordCurrent", "PasswordNew", "PasswordConfirmation"};
        javax.swing.JPasswordField[] fields = {lama, baru, konfirmasi};
        for (int i = 0; i < fields.length; i++) {
            javax.swing.JLabel caption = label(titles[i], 12, true, new java.awt.Color(42, 64, 83));
            caption.setLabelFor(fields[i]); c.gridy = i * 2; c.insets = new java.awt.Insets(0, 0, 6, 0);
            body.add(caption, c);
            fields[i].setName(names[i]);
            String[] hints = {"Masukkan password saat ini", "Buat password baru", "Ulangi password baru"};
            fields[i].putClientProperty("password.placeholder", hints[i]);
            fields[i].getAccessibleContext().setAccessibleDescription(hints[i]); c.gridy++; c.insets = new java.awt.Insets(0, 0, 14, 0);
            body.add(kotakPassword(fields[i], i == 0 ? "lock" : i == 1 ? "key" : "check"), c);
        }
        RoundPanel note = new RoundPanel(10, new java.awt.Color(237, 247, 255), new java.awt.Color(221, 237, 250));
        note.setLayout(new java.awt.BorderLayout(10, 0));
        note.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        note.add(new javax.swing.JLabel(ikonPng("info", 18)), java.awt.BorderLayout.WEST);
        javax.swing.JPanel noteText = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 2)); noteText.setOpaque(false);
        noteText.add(label("Minimal 8 karakter", 11, true, new java.awt.Color(30, 91, 135)));
        noteText.add(label("Huruf besar, huruf kecil, angka, dan simbol.", 11, false, new java.awt.Color(65, 101, 129)));
        note.add(noteText, java.awt.BorderLayout.CENTER);
        c.gridy = 6; c.insets = new java.awt.Insets(0, 0, 18, 0); body.add(note, c);
        javax.swing.JPanel actions = new javax.swing.JPanel(new java.awt.GridBagLayout()); actions.setOpaque(false);
        gayaTombol(batal, false); gayaTombol(simpan, true);
        batal.setText("Batal"); batal.setIcon(ikonPng("close", 16));
        simpan.setText("Simpan Password"); simpan.setIcon(ikonPng("check-white", 17));
        batal.setToolTipText("Batal / tutup (Esc atau Alt+2)"); simpan.setToolTipText("Simpan password baru (Enter atau Alt+1)");
        batal.setPreferredSize(new java.awt.Dimension(130, 42)); simpan.setPreferredSize(new java.awt.Dimension(218, 42));
        java.awt.GridBagConstraints a = new java.awt.GridBagConstraints(); a.gridy = 0; a.fill = java.awt.GridBagConstraints.HORIZONTAL;
        a.gridx = 0; a.weightx = 0.38; a.insets = new java.awt.Insets(0, 0, 0, 12); actions.add(batal, a);
        a.gridx = 1; a.weightx = 0.62; a.insets = new java.awt.Insets(0, 0, 0, 0); actions.add(simpan, a);
        c.gridy = 7; c.insets = new java.awt.Insets(0, 0, 0, 0); body.add(actions, c);
        card.add(body, java.awt.BorderLayout.CENTER);
        // Lebar ditetapkan, tinggi mengikuti font/komponen agar tidak terpotong.
        java.awt.Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new java.awt.Dimension(Math.max(472, preferred.width), preferred.height));
        return card;
    }

    private static javax.swing.JLabel label(String text, int size, boolean bold, java.awt.Color color) {
        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setFont(new java.awt.Font(bold ? "Segoe UI Semibold" : "Segoe UI", java.awt.Font.PLAIN, size));
        label.setForeground(color); return label;
    }

    private static javax.swing.JPanel kotakPassword(javax.swing.JPasswordField field, String icon) {
        RoundPanel shell = new RoundPanel(10, java.awt.Color.WHITE, new java.awt.Color(203, 217, 229)) {
            @Override protected java.awt.Color warnaGaris() {
                return field.isFocusOwner() ? new java.awt.Color(19, 121, 195) : super.warnaGaris();
            }
        };
        shell.setName(field.getName() + "Shell"); shell.setLayout(new java.awt.BorderLayout(10, 0));
        shell.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 12, 1, 6));
        shell.setPreferredSize(new java.awt.Dimension(392, 42));
        shell.add(new javax.swing.JLabel(ikonPng(icon, 20)), java.awt.BorderLayout.WEST);
        field.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field.setOpaque(false); field.setForeground(new java.awt.Color(31, 53, 73));
        field.setCaretColor(new java.awt.Color(13, 113, 186)); field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        field.setSelectionColor(new java.awt.Color(205, 231, 250)); field.setSelectedTextColor(new java.awt.Color(25, 56, 81));
        field.setHighlighter(new javax.swing.text.DefaultHighlighter()); field.setEchoChar('\u2022');
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent event) { shell.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent event) { shell.repaint(); }
        });
        javax.swing.JLayer<javax.swing.JPasswordField> layer = new javax.swing.JLayer<>(field, new PlaceholderUI());
        layer.setName(field.getName() + "Placeholder");
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void refresh() { layer.repaint(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { refresh(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { refresh(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { refresh(); }
        });
        shell.add(layer, java.awt.BorderLayout.CENTER);
        javax.swing.JButton eye = new javax.swing.JButton(); eye.setName(field.getName() + "Eye");
        tombolIkon(eye); eye.setPreferredSize(new java.awt.Dimension(28, 28));
        field.putClientProperty("password.eye", eye);
        aturMata(field, false);
        eye.addActionListener(event -> {
            aturMata(field, field.getEchoChar() != 0);
            field.requestFocusInWindow();
        });
        shell.add(eye, java.awt.BorderLayout.EAST); return shell;
    }

    private static void aturMata(javax.swing.JPasswordField field, boolean terlihat) {
        field.setEchoChar(terlihat ? (char) 0 : '\u2022');
        Object control = field.getClientProperty("password.eye");
        if (control instanceof javax.swing.JButton) {
            javax.swing.JButton eye = (javax.swing.JButton) control;
            eye.setIcon(ikonPng(terlihat ? "eye-off" : "eye", 20));
            eye.setToolTipText(terlihat ? "Sembunyikan password" : "Lihat password");
            eye.getAccessibleContext().setAccessibleName(eye.getToolTipText());
        }
    }

    private static void tombolIkon(javax.swing.JButton button) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder()); button.setFocusPainted(false);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    private static void gayaTombol(javax.swing.JButton button, boolean utama) {
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        button.setForeground(utama ? java.awt.Color.WHITE : new java.awt.Color(55, 78, 99));
        button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
        button.setIconTextGap(8); button.setRolloverEnabled(true);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(java.awt.Graphics graphics, javax.swing.JComponent component) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                javax.swing.ButtonModel model = button.getModel();
                java.awt.Color background = utama ? new java.awt.Color(13, 115, 187) : new java.awt.Color(244, 248, 252);
                if (model.isRollover()) background = utama ? new java.awt.Color(9, 101, 169) : new java.awt.Color(232, 241, 249);
                if (model.isPressed()) background = utama ? new java.awt.Color(7, 83, 143) : new java.awt.Color(221, 234, 245);
                if (!button.isEnabled()) background = new java.awt.Color(206, 220, 231);
                g.setColor(background); g.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 10, 10);
                if (!utama) { g.setColor(new java.awt.Color(211, 225, 236)); g.drawRoundRect(0, 0, component.getWidth()-1, component.getHeight()-1, 10, 10); }
                g.dispose(); super.paint(graphics, component);
            }
        });
    }

    private static class RoundPanel extends javax.swing.JPanel {
        private final int radius; private final java.awt.Color background, outline;
        RoundPanel(int radius, java.awt.Color background, java.awt.Color outline) {
            this.radius = radius; this.background = background; this.outline = outline; setOpaque(false);
        }
        protected java.awt.Color warnaGaris() { return outline; }
        @Override protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(background); g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (warnaGaris() != null) { g.setColor(warnaGaris()); g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius); }
            g.dispose();
        }
        @Override protected void paintChildren(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.clip(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            super.paintChildren(g); g.dispose();
        }
    }

    /** Placeholder hanya dilukis saat document kosong; tidak pernah menjadi isi password. */
    private static final class PlaceholderUI extends javax.swing.plaf.LayerUI<javax.swing.JPasswordField> {
        @Override public void paint(java.awt.Graphics graphics, javax.swing.JComponent component) {
            super.paint(graphics, component);
            javax.swing.JLayer<?> layer = (javax.swing.JLayer<?>) component;
            javax.swing.JPasswordField field = (javax.swing.JPasswordField) layer.getView();
            if (field.getDocument().getLength() != 0) return;
            Object hint = field.getClientProperty("password.placeholder");
            if (!(hint instanceof String)) return;
            java.awt.Point origin = javax.swing.SwingUtilities.convertPoint(field, 0, 0, layer);
            java.awt.Insets insets = field.getInsets();
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.clipRect(origin.x, origin.y, field.getWidth(), field.getHeight());
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(field.getFont()); g.setColor(new java.awt.Color(120, 141, 159));
            java.awt.FontMetrics metrics = g.getFontMetrics();
            int baseline = origin.y + (field.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString((String) hint, origin.x + insets.left + 2, baseline);
            g.dispose();
        }
    }

    private static final java.util.Map<String, javax.swing.ImageIcon> ICONS = new java.util.HashMap<>();

    /** Semua ikon berasal dari PNG transparan; varian resolusi menjaga ketajaman pada DPI Windows. */
    private static javax.swing.ImageIcon ikonPng(String name, int size) {
        String key = name + ":" + size;
        javax.swing.ImageIcon cached = ICONS.get(key);
        if (cached != null) return cached;
        String path = "/picture/password-r5/" + name + ".png";
        java.net.URL resource = WindowInputPassword.class.getResource(path);
        if (resource == null) throw new IllegalStateException("Ikon belum disalin: " + path);
        try {
            java.awt.image.BufferedImage source = javax.imageio.ImageIO.read(resource);
            if (source == null) throw new java.io.IOException("Format PNG tidak valid");
            java.awt.Image[] variants = new java.awt.Image[4];
            for (int i = 0; i < variants.length; i++) {
                int pixels = size * (i + 1);
                java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
                        pixels, pixels, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(source, 0, 0, pixels, pixels, null); g.dispose();
                variants[i] = scaled;
            }
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(
                    new java.awt.image.BaseMultiResolutionImage(variants));
            ICONS.put(key, icon); return icon;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Tidak dapat membaca ikon: " + path, exception);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame6 = new widget.InternalFrame();
        PassLama = new javax.swing.JPasswordField();
        jLabel9 = new widget.Label();
        BtnClosePass = new widget.Button();
        BtnSimpanPass = new widget.Button();
        jLabel10 = new widget.Label();
        Passbaru1 = new javax.swing.JPasswordField();
        jLabel12 = new widget.Label();
        PassBaru2 = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Ubah Password ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); // NOI18N
        internalFrame6.setLayout(null);

        PassLama.setHighlighter(null);
        internalFrame6.add(PassLama);
        PassLama.setBounds(128, 30, 190, 23);

        jLabel9.setText("Password Lama :");
        internalFrame6.add(jLabel9);
        jLabel9.setBounds(0, 30, 125, 23);

        BtnClosePass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnClosePass.setMnemonic('2');
        BtnClosePass.setText("Tutup");
        BtnClosePass.setToolTipText("Alt+2");
        BtnClosePass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnClosePassActionPerformed(evt);
            }
        });
        BtnClosePass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnClosePassKeyPressed(evt);
            }
        });
        internalFrame6.add(BtnClosePass);
        BtnClosePass.setBounds(230, 130, 100, 30);

        BtnSimpanPass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanPass.setMnemonic('1');
        BtnSimpanPass.setText("Simpan");
        BtnSimpanPass.setToolTipText("Alt+1");
        BtnSimpanPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanPassActionPerformed(evt);
            }
        });
        BtnSimpanPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanPassKeyPressed(evt);
            }
        });
        internalFrame6.add(BtnSimpanPass);
        BtnSimpanPass.setBounds(20, 130, 100, 30);

        jLabel10.setText("Password Baru :");
        internalFrame6.add(jLabel10);
        jLabel10.setBounds(0, 60, 125, 23);

        Passbaru1.setHighlighter(null);
        internalFrame6.add(Passbaru1);
        Passbaru1.setBounds(128, 60, 190, 23);

        jLabel12.setText("Password Baru :");
        internalFrame6.add(jLabel12);
        jLabel12.setBounds(0, 90, 125, 23);

        PassBaru2.setHighlighter(null);
        internalFrame6.add(PassBaru2);
        PassBaru2.setBounds(128, 90, 190, 23);

        getContentPane().add(internalFrame6, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnClosePassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnClosePassActionPerformed
        dispose();
    }//GEN-LAST:event_BtnClosePassActionPerformed

    private void BtnClosePassKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnClosePassKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            dispose();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            PassBaru2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            PassLama.requestFocus();
        }
    }//GEN-LAST:event_BtnClosePassKeyPressed

    private void BtnSimpanPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanPassActionPerformed
        char[] oldPassword = PassLama.getPassword();
        char[] newPassword = Passbaru1.getPassword();
        char[] confirmation = PassBaru2.getPassword();
        try {
            if (oldPassword.length == 0) { Valid.textKosong(PassLama, "Password Lama"); return; }
            String problem = PasswordPolicy.problem(newPassword);
            if (problem != null) { JOptionPane.showMessageDialog(this, problem); Passbaru1.requestFocus(); return; }
            if (!PasswordPolicy.matches(newPassword, confirmation)) {
                JOptionPane.showMessageDialog(this, "Konfirmasi password baru tidak sama."); PassBaru2.requestFocus(); return;
            }
            if (!new AccountPasswordService(koneksiDB::condbVerifikasiSesi).changeOwn(account, user, oldPassword, newPassword)) {
                JOptionPane.showMessageDialog(this, "Password lama salah atau akun sudah berubah."); PassLama.requestFocus(); return;
            }
            Sequel.SimpanTrack("CHANGE PASSWORD " + account.name() + " akun=" + user + " [PASSWORD DISEMBUNYIKAN]");
            JOptionPane.showMessageDialog(this, "Password baru berhasil disimpan.");
            dispose();
        } catch (java.sql.SQLException e) {
            PasswordDialogs.failure(this);
        } finally {
            java.util.Arrays.fill(oldPassword, '\0'); java.util.Arrays.fill(newPassword, '\0'); java.util.Arrays.fill(confirmation, '\0');
        }
    }
//GEN-LAST:event_BtnSimpanPassActionPerformed

    private void BtnSimpanPassKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanPassKeyPressed
        Valid.pindah(evt,PassLama,PassBaru2);
    }//GEN-LAST:event_BtnSimpanPassKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WindowInputPassword.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WindowInputPassword.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WindowInputPassword.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WindowInputPassword.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WindowInputPassword dialog = new WindowInputPassword(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnClosePass;
    private widget.Button BtnSimpanPass;
    private javax.swing.JPasswordField PassBaru2;
    private javax.swing.JPasswordField PassLama;
    private javax.swing.JPasswordField Passbaru1;
    private widget.InternalFrame internalFrame6;
    private widget.Label jLabel10;
    private widget.Label jLabel12;
    private widget.Label jLabel9;
    // End of variables declaration//GEN-END:variables
    @Override
    public void dispose() {
        PassLama.setText(""); Passbaru1.setText(""); PassBaru2.setText("");
        super.dispose();
    }

    public void setAccount(String user, AccountPasswordService.Account account) {
        if (account == null) throw new IllegalArgumentException("Jenis akun wajib tersedia.");
        SetUserPass(user, "");
        this.account = account;
    }

    public void SetUserPass(String user, String pass){
        this.account = AccountPasswordService.Account.USER;
        PassLama.setText("");
        Passbaru1.setText("");
        PassBaru2.setText("");
        this.user=user;
        aturMata(PassLama, false); aturMata(Passbaru1, false); aturMata(PassBaru2, false);
        // Parameter pass dipertahankan untuk kompatibilitas pemanggil; tidak disimpan.
    }
}
