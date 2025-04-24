package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.UIManager;

/**
 * Tombol dengan sudut melengkung yang dapat diatur dan efek hover saat disentuh oleh mouse.
 */
public class ButtonBig extends JButton {

    private static final long serialVersionUID = 1L;
    private int cornerRadius = 8; // Default sudut lengkung tombol
    private Color normalColor = new Color(240, 240, 240, 0); // Warna normal tombol transparan
    private Color hoverColor = new Color(225, 225, 225); // Warna saat hover

    public ButtonBig() {
        super();
        setDefaults();
        addMouseEffects(); // Menambahkan efek hover
    }

    /**
     * Mengatur tampilan default tombol.
     */
    private void setDefaults() {
        setForeground(new Color(50, 50, 50));
        setFont(UIManager.getFont("Button.font")); // Menggunakan font sesuai sistem operasi
        setContentAreaFilled(false); // Mengisi area konten secara manual
        setFocusPainted(false); // Menghilangkan efek fokus
        setBorderPainted(false); // Menghilangkan efek border
        setBackground(normalColor); // Warna latar belakang default (transparan)
        setOpaque(false); // Agar tombol transparan dan bisa diubah background-nya
    }

    /**
     * Menambahkan efek hover (saat mouse berada di atas tombol).
     */
    private void addMouseEffects() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor); // Ubah warna ke abu-abu saat disentuh
                repaint(); // Gambar ulang tombol
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor); // Kembali ke warna transparan saat tidak disentuh
                repaint(); // Gambar ulang tombol
            }
        });
    }

    /**
     * Mengatur sudut lengkung tombol.
     * @param radius Nilai radius lengkungan sudut
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint(); // Meminta tombol untuk digambar ulang dengan sudut baru
    }

    /**
     * Menggambar tombol dengan sudut melengkung.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Mengatur anti-aliasing agar hasil gambar lebih halus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Mengatur warna latar belakang sesuai kondisi (hover atau tidak)
        g2.setColor(getBackground());
        
        // Menggambar tombol dengan sudut melengkung sesuai cornerRadius
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Memanggil paintComponent dari JButton untuk menangani teks dan ikon
        super.paintComponent(g);
        
        g2.dispose(); // Menghilangkan resources Graphics2D
    }
}
