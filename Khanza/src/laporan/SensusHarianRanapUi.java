package laporan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

final class SensusHarianRanapUi {
    static final Color LATAR = new Color(245, 248, 252);
    static final Color GARIS = new Color(207, 220, 237);
    private static final Color PUTIH = Color.WHITE;
    private static final Color TINTA = new Color(29, 45, 68);
    private static final Color SEKUNDER = new Color(83, 103, 129);
    private static final Color BIRU = new Color(28, 104, 211);
    private static final Color BIRU_MUDA = new Color(237, 245, 255);
    private static final int TINGGI_INPUT = 36;
    private static final boolean ADA_SEMIBOLD = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames()).contains("Segoe UI Semibold");

    private SensusHarianRanapUi() {
    }

    private static Font font(int ukuran, boolean semibold) {
        String keluarga = semibold && ADA_SEMIBOLD ? "Segoe UI Semibold" : "Segoe UI";
        return new Font(keluarga, semibold && !ADA_SEMIBOLD ? Font.BOLD : Font.PLAIN, ukuran);
    }

    static JLabel label(String teks, int ukuran, boolean semibold) {
        JLabel label = new JLabel(teks);
        label.setFont(font(ukuran, semibold));
        label.setForeground(ukuran >= 16 ? TINTA : SEKUNDER);
        return label;
    }

    static void fontPratinjau(Container panel) {
        panel.setFont(font(12, false));
        for (Component anak : panel.getComponents()) {
            if (anak instanceof Container) {
                fontPratinjau((Container) anak);
            }
        }
    }

    static JComponent kolom(String nama, JComponent isian) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = label(nama, 12, true);
        label.setLabelFor(isian);
        panel.add(label, BorderLayout.NORTH);
        panel.add(isian, BorderLayout.CENTER);
        return panel;
    }

    static <T> JComboBox<T> pilihan(int lebar) {
        JComboBox<T> combo = new JComboBox<>();
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return panah(false);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle area, boolean fokus) {
                // Latar digambar bersama garis tepi agar sudut tetap membulat.
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                catBulat(g, c, c.isEnabled() ? PUTIH : LATAR, fokusDiDalam(c) ? BIRU : GARIS);
                super.paint(g, c);
            }
        });
        combo.setOpaque(false);
        combo.setBackground(PUTIH);
        combo.setForeground(TINTA);
        combo.setFont(font(13, false));
        combo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 2));
        combo.setPreferredSize(new Dimension(lebar, TINGGI_INPUT));
        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object nilai, int indeks,
                                                          boolean dipilih, boolean fokus) {
                super.getListCellRendererComponent(list, nilai, indeks, dipilih, fokus);
                setFont(combo.getFont());
                setForeground(!combo.isEnabled() ? SEKUNDER : dipilih && indeks >= 0 ? BIRU : TINTA);
                setBackground(dipilih ? BIRU_MUDA : PUTIH);
                setOpaque(indeks >= 0);
                setBorder(BorderFactory.createEmptyBorder(indeks < 0 ? 0 : 6, indeks < 0 ? 0 : 10, indeks < 0 ? 0 : 6, 8));
                return this;
            }
        });
        catUlangSaatFokus(combo, combo);
        return combo;
    }

    static JSpinner tahun(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                catBulat(g, this, isEnabled() ? PUTIH : LATAR, fokusDiDalam(this) ? BIRU : GARIS);
                super.paintComponent(g);
            }
        };
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                JButton tombol = panah(true);
                installNextButtonListeners(tombol);
                return tombol;
            }

            @Override
            protected Component createPreviousButton() {
                JButton tombol = panah(false);
                installPreviousButtonListeners(tombol);
                return tombol;
            }
        });
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0000");
        JFormattedTextField teks = editor.getTextField();
        teks.setFont(font(13, false));
        teks.setForeground(TINTA);
        teks.setBorder(BorderFactory.createEmptyBorder());
        teks.setOpaque(false);
        editor.setOpaque(false);
        spinner.setEditor(editor);
        spinner.setOpaque(false);
        spinner.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 3));
        spinner.setPreferredSize(new Dimension(92, TINGGI_INPUT));
        catUlangSaatFokus(teks, spinner);
        return spinner;
    }

    static JButton tombol(String teks, boolean utama) {
        JButton tombol = new JButton(teks) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Color isi = !isEnabled() ? LATAR : utama
                        ? getModel().isPressed() ? new Color(19, 82, 172) : BIRU
                        : getModel().isRollover() ? BIRU_MUDA : PUTIH;
                Color tepi = hasFocus() ? (utama ? new Color(141, 190, 255) : BIRU)
                        : utama && isEnabled() ? isi : GARIS;
                catBulat(g, this, isi, tepi);
                super.paintComponent(g);
            }
        };
        tombol.setUI(new BasicButtonUI());
        tombol.setForeground(utama ? PUTIH : TINTA);
        tombol.setFont(font(13, true));
        tombol.setOpaque(false);
        tombol.setContentAreaFilled(false);
        tombol.setFocusPainted(false);
        tombol.setRolloverEnabled(true);
        tombol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        tombol.setPreferredSize(new Dimension(tombol.getPreferredSize().width, TINGGI_INPUT));
        catUlangSaatFokus(tombol, tombol);
        return tombol;
    }

    private static JButton panah(boolean atas) {
        JButton tombol = new JButton() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                halus(g);
                g.setColor(isEnabled() ? SEKUNDER : GARIS);
                int x = getWidth() / 2;
                int y = getHeight() / 2;
                int arah = atas ? -1 : 1;
                g.drawPolyline(new int[]{x - 4, x, x + 4}, new int[]{y - 2 * arah, y + 2 * arah, y - 2 * arah}, 3);
                g.dispose();
            }
        };
        tombol.setUI(new BasicButtonUI());
        tombol.setOpaque(false);
        tombol.setContentAreaFilled(false);
        tombol.setFocusable(false);
        tombol.setBorder(BorderFactory.createEmptyBorder());
        tombol.setPreferredSize(new Dimension(23, 14));
        return tombol;
    }

    private static boolean fokusDiDalam(JComponent kontrol) {
        Component fokus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return fokus == kontrol || (fokus != null && SwingUtilities.isDescendingFrom(fokus, kontrol));
    }

    private static void catUlangSaatFokus(JComponent kontrol, JComponent target) {
        kontrol.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                target.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                target.repaint();
            }
        });
    }

    private static void catBulat(Graphics graphics, JComponent kontrol, Color isi, Color garis) {
        Graphics2D g = (Graphics2D) graphics.create();
        halus(g);
        g.setColor(isi);
        g.fillRoundRect(0, 0, kontrol.getWidth() - 1, kontrol.getHeight() - 1, 12, 12);
        g.setColor(garis);
        g.drawRoundRect(0, 0, kontrol.getWidth() - 1, kontrol.getHeight() - 1, 12, 12);
        g.dispose();
    }

    private static void halus(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
