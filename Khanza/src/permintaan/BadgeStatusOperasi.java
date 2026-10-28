package permintaan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** Tampilan kapsul status operasi; tidak mengatur status, hak akses, atau database. */
final class BadgeStatusOperasi extends JButton {
    private static final Color[] AKSEN = {
        new Color(37,99,235), new Color(180,83,9),
        new Color(124,58,237), new Color(21,128,61)
    };
    private static final Color LATAR_NETRAL = new Color(243,244,246);
    private static final Color TEKS_NETRAL = new Color(100,116,139);
    private static final Color TEKS_BERIKUT = new Color(51,65,85);
    private static final Color TEPI_NETRAL = new Color(203,213,225);
    private final Color aksen;
    private boolean statusSekarang;

    BadgeStatusOperasi(String teks, int urutanWarna) {
        super(teks);
        aksen = AKSEN[urutanWarna];
        setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(7,17,7,17));
        setForeground(TEKS_NETRAL);
        setBackground(LATAR_NETRAL);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void setStatusSekarang(boolean sekarang) {
        statusSekarang = sekarang;
        // Warna status tetap terbaca walaupun tombol status saat ini tidak dapat diklik.
        setBackground(sekarang ? aksen : LATAR_NETRAL);
        setForeground(sekarang ? Color.WHITE : isEnabled() ? TEKS_BERIKUT : TEKS_NETRAL);
        repaint();
    }

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setForeground(statusSekarang ? Color.WHITE : enabled ? TEKS_BERIKUT : TEKS_NETRAL);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(Math.max(110,fm.stringWidth(getText())+36),Math.max(34,fm.getHeight()+16));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int lebar = getWidth()-3, tinggi = getHeight()-3;
            if(lebar <= 0 || tinggi <= 0) return;
            Color isi = getBackground();
            if(isEnabled() && getModel().isPressed() && getModel().isArmed()) {
                isi = campur(isi,TEPI_NETRAL,0.30f);
            } else if(isEnabled() && getModel().isRollover()) {
                isi = campur(isi,TEPI_NETRAL,0.15f);
            }
            // Diameter lengkungan mengikuti tinggi: ujung kapsul benar-benar setengah lingkaran.
            g.setColor(isi);
            g.fillRoundRect(1,1,lebar,tinggi,tinggi,tinggi);
            g.setStroke(new BasicStroke(statusSekarang || isEnabled() ? 1.5f : 1.0f));
            g.setColor(statusSekarang ? aksen : isEnabled() ? new Color(148,163,184) : TEPI_NETRAL);
            g.drawRoundRect(1,1,lebar,tinggi,tinggi,tinggi);
            if(isEnabled() && hasFocus()) {
                g.setStroke(new BasicStroke(1.5f));
                g.setColor(TEKS_BERIKUT);
                g.drawRoundRect(4,4,lebar-6,tinggi-6,tinggi-6,tinggi-6);
            }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String teks = getText() == null ? "" : getText();
            g.setColor(getForeground());
            g.drawString(teks,(getWidth()-fm.stringWidth(teks))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
        } finally {
            g.dispose();
        }
    }

    private static Color campur(Color asal, Color tujuan, float porsi) {
        float sisa = 1-porsi;
        return new Color(Math.round(asal.getRed()*sisa+tujuan.getRed()*porsi),
                Math.round(asal.getGreen()*sisa+tujuan.getGreen()*porsi),
                Math.round(asal.getBlue()*sisa+tujuan.getBlue()*porsi));
    }

    static javax.swing.JPanel tahapDenganPanah(JButton tombol, boolean denganPanah) {
        javax.swing.JPanel tahap = new javax.swing.JPanel(new java.awt.BorderLayout(6,0));
        tahap.setOpaque(false);
        if(denganPanah) {
            javax.swing.JLabel panah = new javax.swing.JLabel("\u2192",javax.swing.SwingConstants.CENTER);
            panah.setFont(new Font("Segoe UI",Font.PLAIN,17));
            panah.setForeground(new Color(148,163,184));
            panah.setPreferredSize(new Dimension(18,34));
            tahap.add(panah,java.awt.BorderLayout.WEST);
        }
        tahap.add(tombol,java.awt.BorderLayout.CENTER);
        return tahap;
    }

    /** Tinggi bar mengikuti pembungkusan kapsul pada jendela yang lebih sempit. */
    static final class PanelAlur extends javax.swing.JPanel {
        PanelAlur() {
            super(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,8,4));
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    revalidate();
                }
            });
        }

        @Override public Dimension getPreferredSize() {
            int lebar = getWidth();
            if(lebar <= 0 && getParent() != null) lebar = getParent().getWidth();
            if(lebar <= 0) return super.getPreferredSize();
            java.awt.Insets tepi = getInsets();
            int tersedia = Math.max(1,lebar-tepi.left-tepi.right-16);
            int x=0, tinggiBaris=0, tinggi=tepi.top+tepi.bottom+8;
            for(java.awt.Component anak : getComponents()) {
                if(!anak.isVisible()) continue;
                Dimension ukuran = anak.getPreferredSize();
                if(x > 0 && x+8+ukuran.width > tersedia) {
                    tinggi += tinggiBaris+4;
                    x=0;
                    tinggiBaris=0;
                }
                x += (x > 0 ? 8 : 0)+ukuran.width;
                tinggiBaris = Math.max(tinggiBaris,ukuran.height);
            }
            return new Dimension(lebar,tinggi+tinggiBaris);
        }
    }
}
