package setting;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/** Ikon mata khusus admin; pratinjau DB terpisah dari input untuk penyimpanan. */
final class UserPasswordEye extends JPanel {
    private final JPasswordField input;
    private final JPasswordField pratinjau = new JPasswordField();
    private final JButton mata = new JButton();
    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);
    private final BooleanSupplier admin;
    private final Supplier<String> userTerpilih;
    private final Callable<Connection> koneksi;
    private final char echo;
    private final ImageIcon lihat = ikon("eye");
    private final ImageIcon tutup = ikon("eye-off");
    private boolean terlihat;
    private long versi;

    UserPasswordEye(JPasswordField input, BooleanSupplier admin,
            Supplier<String> userTerpilih, Callable<Connection> koneksi) {
        super(new BorderLayout(0, 0));
        this.input = input;
        this.admin = admin;
        this.userTerpilih = userTerpilih;
        this.koneksi = koneksi;
        echo = input.getEchoChar() == 0 ? '\u2022' : input.getEchoChar();
        setOpaque(false);
        isi.setOpaque(false);
        pratinjau.setName("PratinjauPasswordUser");
        pratinjau.setEditable(false);
        pratinjau.setFont(input.getFont());
        pratinjau.setBorder(input.getBorder());
        pratinjau.setBackground(input.getBackground());
        pratinjau.setForeground(input.getForeground());
        pratinjau.setMargin(input.getMargin());
        pratinjau.setToolTipText("Password tersimpan (hanya lihat). Klik mata untuk kembali mengisi password baru.");
        isi.add(input, "input");
        isi.add(pratinjau, "pratinjau");
        add(isi, BorderLayout.CENTER);
        mata.setName("BtnLihatPasswordUser");
        mata.setPreferredSize(new Dimension(25, 23));
        mata.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mata.setFocusable(true);
        mata.setContentAreaFilled(false);
        mata.setBorderPainted(false);
        mata.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        mata.addActionListener(event -> gantiTampilan());
        add(mata, BorderLayout.EAST);
        pantau(input.getDocument());
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                sembunyikan();
            }
        });
        sembunyikan();
    }

    void pantau(Document document) {
        document.addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { sembunyikan(); }
            @Override public void removeUpdate(DocumentEvent event) { sembunyikan(); }
            @Override public void changedUpdate(DocumentEvent event) { sembunyikan(); }
        });
    }

    void sembunyikan() {
        versi++;
        terlihat = false;
        input.setEchoChar(echo);
        pratinjau.setEchoChar(echo);
        pratinjau.setText("");
        kartu.show(isi, "input");
        mata.setVisible(admin.getAsBoolean());
        mata.setEnabled(true);
        perbaruiIkon();
    }

    private void perbaruiIkon() {
        mata.setIcon(terlihat ? tutup : lihat);
        mata.setToolTipText(terlihat ? "Sembunyikan password" : "Tampilkan password");
        mata.getAccessibleContext().setAccessibleName(mata.getToolTipText());
    }

    private void gantiTampilan() {
        if (!admin.getAsBoolean() || terlihat) {
            sembunyikan();
            return;
        }
        if (input.getDocument().getLength() > 0) {
            input.setEchoChar((char) 0);
            terlihat = true;
            perbaruiIkon();
            return;
        }
        final String username = userTerpilih.get();
        if (username == null || username.isEmpty()) {
            pesan("Pilih user terlebih dahulu atau isi password baru.");
            return;
        }
        final long permintaan = ++versi;
        mata.setEnabled(false);
        new SwingWorker<char[], Void>() {
            @Override protected char[] doInBackground() throws Exception {
                // Periksa lagi sebelum membaca; koneksi khusus tidak memakai koneksi form bersama.
                if (!admin.getAsBoolean()) return null;
                try (Connection c = koneksi.call()) {
                    if (c == null) throw new java.sql.SQLException("Koneksi belum tersedia.");
                    try (PreparedStatement q = c.prepareStatement(
                            "select AES_DECRYPT(password,'windi') from user where id_user=AES_ENCRYPT(?,'nur')")) {
                        q.setQueryTimeout(5);
                        q.setString(1, username);
                        try (ResultSet row = q.executeQuery()) {
                            if (!row.next()) return null;
                            String password = row.getString(1);
                            return password == null ? null : password.toCharArray();
                        }
                    }
                }
            }

            @Override protected void done() {
                char[] password = null;
                try {
                    password = get();
                    if (permintaan != versi || !admin.getAsBoolean()
                            || !username.equals(userTerpilih.get())) return;
                    if (password == null) {
                        pesan("Password user tidak tersedia. Muat ulang daftar user.");
                        return;
                    }
                    // Jangan mengisi input asli: melihat password tidak boleh mengubah alur Simpan/Ganti.
                    pratinjau.setText(new String(password));
                    pratinjau.setEchoChar((char) 0);
                    kartu.show(isi, "pratinjau");
                    terlihat = true;
                    perbaruiIkon();
                } catch (Exception exception) {
                    if (permintaan == versi && admin.getAsBoolean()) {
                        pesan("Password belum dapat ditampilkan. Periksa koneksi database lalu coba lagi.");
                    }
                } finally {
                    if (password != null) Arrays.fill(password, '\0');
                    if (permintaan == versi) mata.setEnabled(true);
                }
            }
        }.execute();
    }

    private void pesan(String text) {
        JOptionPane.showMessageDialog(this, text, "Password User", JOptionPane.INFORMATION_MESSAGE);
    }

    private static ImageIcon ikon(String nama) {
        java.net.URL resource = UserPasswordEye.class.getResource("/picture/password-r5/" + nama + ".png");
        if (resource == null) throw new IllegalStateException("Ikon password belum tersedia: " + nama);
        return new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(
                16, 16, java.awt.Image.SCALE_SMOOTH));
    }
}
