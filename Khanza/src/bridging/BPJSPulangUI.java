package bridging;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/** Komponen khusus dialog update pulang; tidak mengubah widget/form lainnya. */
public final class BPJSPulangUI {
    private BPJSPulangUI() { }
    private static final Color BIRU = new Color(37, 99, 193);
    private static final Map<String, Icon> IKON = new HashMap<String, Icon>();

    public static Icon ikon(String nama) {
        Icon ikon = IKON.get(nama);
        if (ikon == null) {
            java.net.URL sumber = BPJSPulangUI.class.getResource("/picture/bpjs_pulang/" + nama);
            if (sumber == null) {
                throw new IllegalStateException("Ikon tidak ditemukan: picture/bpjs_pulang/" + nama);
            }
            ikon = new ImageIcon(sumber);
            IKON.put(nama, ikon);
        }
        return ikon;
    }

    /** Satu border pada komponen tanggal, tanpa kotak pembungkus. */
    public static void rapikanTanggal(final JComponent tanggal) {
        tanggal.setOpaque(false);
        tanggal.setBorder(BorderFactory.createCompoundBorder(
                new Bingkai(new Color(205, 218, 235), 1f, 12),
                BorderFactory.createEmptyBorder(1, 10, 1, 8)));
        for (Component anak : tanggal.getComponents()) { bersihkanAnakTanggal(anak); }
    }

    private static void bersihkanAnakTanggal(Component komponen) {
        if (komponen instanceof javax.swing.text.JTextComponent) {
            JComponent teks = (JComponent) komponen;
            teks.setBorder(BorderFactory.createEmptyBorder());
            teks.setOpaque(false);
        } else if (komponen instanceof AbstractButton) {
            AbstractButton tombol = (AbstractButton) komponen;
            tombol.setBorder(BorderFactory.createEmptyBorder());
            tombol.setBorderPainted(false);
            tombol.setContentAreaFilled(false);
            tombol.setOpaque(false);
            tombol.setText("");
            tombol.setIconTextGap(0);
            tombol.setIcon(ikon("chevron.png"));
        } else if (komponen instanceof JComponent) {
            ((JComponent) komponen).setBorder(BorderFactory.createEmptyBorder());
            ((JComponent) komponen).setOpaque(false);
        }
        if (komponen instanceof Container) {
            for (Component anak : ((Container) komponen).getComponents()) { bersihkanAnakTanggal(anak); }
        }
    }

    public static final class Bingkai extends AbstractBorder {
        private final Color warna;
        private final float tebal;
        private final int radius;
        public Bingkai(Color warna, float tebal, int radius) {
            this.warna = warna; this.tebal = tebal; this.radius = radius;
        }
        public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
        public Insets getBorderInsets(Component c, Insets i) { i.set(2, 2, 2, 2); return i; }
        public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(warna); g.setStroke(new BasicStroke(tebal));
            float sela = tebal / 2;
            g.draw(new RoundRectangle2D.Float(x + sela, y + sela,
                    width - tebal, height - tebal, radius, radius));
            g.dispose();
        }
    }

    public static final class PanelDialog extends widget.InternalFrame {
        public PanelDialog() { setOpaque(false); }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
            g.dispose();
        }
    }

    /** Kotak khusus status dan nomor LP. */
    public static final class KotakInput extends JPanel {
        public KotakInput() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(1, 12, 1, 10));
        }
        protected void addImpl(Component c, Object constraints, int index) {
            super.addImpl(c, constraints, index);
            pantauFokus(c);
        }
        private void pantauFokus(Component c) {
            c.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e) { repaint(); }
            });
            if (c instanceof Container) {
                for (Component anak : ((Container) c).getComponents()) { pantauFokus(anak); }
            }
        }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g.dispose();
        }
        protected void paintBorder(Graphics graphics) {
            Component fokus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            boolean aktif = fokus != null && SwingUtilities.isDescendingFrom(fokus, this);
            new Bingkai(aktif ? BIRU : new Color(205, 218, 235), aktif ? 1.6f : 1f, 12)
                    .paintBorder(this, graphics, 0, 0, getWidth(), getHeight());
        }
        protected void paintChildren(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.clip(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 11, 11));
            super.paintChildren(g); g.dispose();
        }
    }

    public static final class Tombol extends JButton {
        public Tombol() {
            setContentAreaFilled(false); setOpaque(false);
            setBorderPainted(false); setFocusPainted(false); setRolloverEnabled(true);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color warna = getBackground();
            if (!isEnabled()) { warna = new Color(222, 228, 237); }
            else if (getModel().isPressed()) { warna = warna.darker(); }
            else if (getModel().isRollover()) {
                warna = new Color(Math.max(0, warna.getRed() - 9),
                        Math.max(0, warna.getGreen() - 9), Math.max(0, warna.getBlue() - 9));
            }
            g.setColor(warna);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Hanya untuk pilihan status, tidak mengganti UI kalender. */
    public static class ComboStatus extends javax.swing.plaf.basic.BasicComboBoxUI {
        protected JButton createArrowButton() {
            JButton button = new JButton(ikon("chevron.png"));
            button.setContentAreaFilled(false); button.setOpaque(false);
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(24, 24));
            return button;
        }
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean fokus) {
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    /** Kalender lokal dialog ini: popup/date model asli, tanpa lukisan kotak dari LAF OS. */
    public static final class Tanggal extends uz.ncipro.calendar.JDateTimePicker {
        public void updateUI() {
            if (popupEditor != null) { popupEditor.removePropertyChangeListener(this); }
            ComboTanggal tampilan = new ComboTanggal(popupEditor);
            setUI(tampilan);
            popupEditor = tampilan.kalender;
            popupEditor.addPropertyChangeListener(this);
            rapikanTanggal(this);
        }
    }

    private static final class ComboTanggal extends ComboStatus {
        private uz.ncipro.calendar.JCalendar kalender;
        ComboTanggal(uz.ncipro.calendar.JCalendar kalender) { this.kalender = kalender; }
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            if (kalender == null) { kalender = new uz.ncipro.calendar.JCalendar(); }
            uz.ncipro.calendar.plaf.basic.BasicJDateTimePickerPopup popup =
                    new uz.ncipro.calendar.plaf.basic.BasicJDateTimePickerPopup(comboBox, kalender);
            popup.getAccessibleContext().setAccessibleParent(comboBox);
            return popup;
        }
    }
}
