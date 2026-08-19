package surat;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Perekam tanda tangan layar penuh untuk drawing pad absolute/screen mapping.
 * Hasil akhir dipotong rapat mengikuti seluruh sisi tinta tanda tangan.
 */
public final class TandaTanganPasienWaliDialog extends JDialog {
    private final SignaturePad pad;
    private BufferedImage hasil;
    /* Menangkap F3 di level aplikasi agar tetap berfungsi walau fokus berada pada drawing pad/perangkat pen tablet. */
    private KeyEventDispatcher dispatcherF3;

    private TandaTanganPasienWaliDialog(Window owner, BufferedImage sebelumnya) {
        super(owner, "Tanda Tangan Pasien / Wali", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());

        pad = new SignaturePad(sebelumnya);
        pad.setBorder(BorderFactory.createLineBorder(new Color(25, 91, 151), 2));
        add(pad, BorderLayout.CENTER);
        add(buatPanelTombol(), BorderLayout.SOUTH);

        pasangShortcut();

        GraphicsConfiguration konfigurasi = owner != null ? owner.getGraphicsConfiguration() : null;
        Rectangle area = konfigurasi != null
                ? konfigurasi.getBounds()
                : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(area);
        setMinimumSize(new Dimension(800, 600));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() { pad.requestFocusInWindow(); }
                });
            }
        });
    }

    public static BufferedImage ambil(Window owner, BufferedImage sebelumnya) {
        TandaTanganPasienWaliDialog dialog = new TandaTanganPasienWaliDialog(owner, sebelumnya);
        dialog.setVisible(true);
        return dialog.hasil;
    }

    private JPanel buatPanelTombol() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 11));
        panel.setBackground(new Color(240, 247, 253));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(184, 206, 226)));

        JButton gunakan = new JButton("Gunakan Tanda Tangan (F2)");
        gunakan.setFocusable(false);
        gunakan.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { simpanHasil(); }
        });
        JButton ulangi = new JButton("Ulangi / Bersihkan (F3)");
        ulangi.setFocusable(false);
        ulangi.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { pad.bersihkan(); }
        });
        JButton batal = new JButton("Batal (ESC)");
        batal.setFocusable(false);
        batal.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
        panel.add(gunakan);
        panel.add(ulangi);
        panel.add(batal);
        return panel;
    }

    private void pasangShortcut() {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "simpan-ttd");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "ulang-ttd");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "batal-ttd");

        root.getActionMap().put("simpan-ttd", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { simpanHasil(); }
        });
        root.getActionMap().put("ulang-ttd", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { pad.bersihkan(); }
        });
        root.getActionMap().put("batal-ttd", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });

        /*
         * Sebagian driver drawing pad / keyboard menangkap F3 sebelum event sampai
         * ke komponen Swing yang sedang fokus. Dispatcher global ini memastikan
         * F3 selalu menghapus kanvas selama dialog tanda tangan sedang terbuka.
         */
        dispatcherF3 = new KeyEventDispatcher() {
            @Override public boolean dispatchKeyEvent(KeyEvent e) {
                if (isShowing() && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F3) {
                    pad.bersihkan();
                    e.consume();
                    return true;
                }
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcherF3);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (dispatcherF3 != null) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcherF3);
                    dispatcherF3 = null;
                }
            }
        });
    }

    private void simpanHasil() {
        if (!pad.adaTandaTangan()) {
            pad.tampilkanPesan("Belum ada tanda tangan. Silakan bubuhkan tanda tangan terlebih dahulu.");
            return;
        }
        hasil = pad.hasilTerpotong();
        dispose();
    }

    private static final class SignaturePad extends JComponent {
        private BufferedImage gambar;
        private BufferedImage gambarSebelumnya;
        private boolean gambarSebelumnyaDitempel = false;
        private Point titikTerakhir;
        private boolean adaTandaTangan;
        private String pesan = "Silakan tanda tangan di area mana saja pada layar ini";
        private long waktuPesan = 0L;

        private SignaturePad(BufferedImage sebelumnya) {
            setOpaque(true);
            setBackground(Color.WHITE);
            setFocusable(true);
            if (sebelumnya != null) {
                gambarSebelumnya = salin(sebelumnya);
                adaTandaTangan = true;
            }

            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { mulai(e); }
                @Override public void mouseDragged(MouseEvent e) { gambar(e); }
                @Override public void mouseReleased(MouseEvent e) { titikTerakhir = null; }
                @Override public void mouseExited(MouseEvent e) { titikTerakhir = null; }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void pastikanKanvas() {
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            if (gambar == null) {
                gambar = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                tempelTandaTanganSebelumnya();
            } else if (gambar.getWidth() != w || gambar.getHeight() != h) {
                BufferedImage baru = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = baru.createGraphics();
                g.drawImage(gambar, 0, 0, null);
                g.dispose();
                gambar = baru;
            }
        }

        private void tempelTandaTanganSebelumnya() {
            if (gambarSebelumnya == null || gambarSebelumnyaDitempel || gambar == null) return;
            Graphics2D g = gambar.createGraphics();
            int x = Math.max(0, (gambar.getWidth() - gambarSebelumnya.getWidth()) / 2);
            int y = Math.max(0, (gambar.getHeight() - gambarSebelumnya.getHeight()) / 2);
            g.drawImage(gambarSebelumnya, x, y, null);
            g.dispose();
            gambarSebelumnyaDitempel = true;
        }

        private void mulai(MouseEvent e) {
            requestFocusInWindow();
            pastikanKanvas();
            titikTerakhir = e.getPoint();
            Graphics2D g = gambar.createGraphics();
            siapkanPena(g);
            g.fillOval(e.getX() - 1, e.getY() - 1, 3, 3);
            g.dispose();
            adaTandaTangan = true;
            repaint();
        }

        private void gambar(MouseEvent e) {
            pastikanKanvas();
            if (titikTerakhir == null) titikTerakhir = e.getPoint();
            Graphics2D g = gambar.createGraphics();
            siapkanPena(g);
            g.drawLine(titikTerakhir.x, titikTerakhir.y, e.getX(), e.getY());
            g.dispose();
            titikTerakhir = e.getPoint();
            adaTandaTangan = true;
            repaint();
        }

        private void siapkanPena(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 34, 48));
            g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        private void bersihkan() {
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            if (gambar != null) {
                gambar.flush();
            }
            /* Buat kanvas transparan baru: semua coretan, termasuk gambar sebelumnya, dihapus total. */
            gambar = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            gambarSebelumnya = null;
            gambarSebelumnyaDitempel = true;
            titikTerakhir = null;
            adaTandaTangan = false;
            tampilkanPesan("Area telah dibersihkan. Silakan tanda tangan kembali.");
            repaint();
            /* Paksa pembaruan langsung agar pengguna melihat kanvas putih tanpa menunggu event berikutnya. */
            paintImmediately(0, 0, w, h);
        }

        private boolean adaTandaTangan() { return adaTandaTangan; }

        private void tampilkanPesan(String isi) {
            pesan = isi;
            waktuPesan = System.currentTimeMillis();
            repaint();
        }

        /** Memotong rapat seluruh sisi gambar sampai batas tinta tanda tangan. */
        private BufferedImage hasilTerpotong() {
            pastikanKanvas();
            int minX = gambar.getWidth();
            int minY = gambar.getHeight();
            int maxX = -1;
            int maxY = -1;

            for (int y = 0; y < gambar.getHeight(); y++) {
                for (int x = 0; x < gambar.getWidth(); x++) {
                    if (((gambar.getRGB(x, y) >>> 24) & 0xFF) > 10) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX < minX || maxY < minY) {
                return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }

            final int margin = 4;
            minX = Math.max(0, minX - margin);
            minY = Math.max(0, minY - margin);
            maxX = Math.min(gambar.getWidth() - 1, maxX + margin);
            maxY = Math.min(gambar.getHeight() - 1, maxY + margin);
            int w = Math.max(1, maxX - minX + 1);
            int h = Math.max(1, maxY - minY + 1);

            BufferedImage hasilAkhir = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = hasilAkhir.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(gambar, 0, 0, w, h, minX, minY, maxX + 1, maxY + 1, null);
            g.dispose();
            return hasilAkhir;
        }

        private static BufferedImage salin(BufferedImage sumber) {
            BufferedImage salinan = new BufferedImage(sumber.getWidth(), sumber.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = salinan.createGraphics();
            g.drawImage(sumber, 0, 0, null);
            g.dispose();
            return salinan;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            pastikanKanvas();
            g.drawImage(gambar, 0, 0, null);

            Graphics2D teks = (Graphics2D) g.create();
            teks.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            teks.setColor(new Color(18, 78, 136));
            teks.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
            teks.drawString("TANDA TANGAN PASIEN / WALI", 32, 42);
            teks.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
            teks.setColor(new Color(70, 82, 96));
            teks.drawString("Silakan bubuhkan tanda tangan di mana saja pada layar. Area hasil akan dipotong otomatis.", 32, 70);
            if (!adaTandaTangan || (System.currentTimeMillis() - waktuPesan) < 3500L) {
                teks.setColor(new Color(120, 90, 0));
                teks.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
                teks.drawString(pesan, 32, 100);
            }
            teks.dispose();
        }
    }
}
