package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Kelas Button yang disesuaikan tanpa efek glass dan dengan sudut melengkung.
 * Tombol akan berubah warna menjadi abu-abu saat disentuh oleh mouse dan kembali putih saat tidak disentuh.
 * Sudut lengkung tombol dapat disesuaikan.
 * 
 * @author usu
 */
public class Button extends JButton {

    private static final long serialVersionUID = 1L;
    private int cornerRadius = 12;  // Sudut lengkung tombol, default 15

    public Button() {
        super();
        setDefaults();
        applySystemLookAndFeel();
        addMouseEffects();  // Menambahkan efek hover pada tombol
    }

    /**
     * Mengatur properti default tombol
     */
    private void setDefaults() {
        // Mengatur font tombol agar mengikuti font versi sistem operasi
        setFont(UIManager.getFont("Button.font"));
        
        // Mengatur warna dan tampilan lain
        setForeground(new Color(50, 50, 50)); // Warna teks tombol
        setBackground(Color.WHITE); // Warna latar belakang tombol
        setMargin(new Insets(2, 7, 2, 7)); // Margin tombol
        setIconTextGap(4); // Jarak antara ikon dan teks
        setFocusPainted(false); // Menghilangkan garis fokus saat tombol dipilih
        setBorderPainted(false); // Menghilangkan border default tombol
        setContentAreaFilled(false); // Kami akan mengisi area sendiri
        setOpaque(false); // Agar latar belakang digambar sendiri
    }

    /**
     * Method untuk mengatur tampilan sesuai dengan sistem operasi.
     */
    private void applySystemLookAndFeel() {
        try {
            // Mengatur Look and Feel agar sesuai dengan sistem operasi
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Memastikan tampilan tombol sesuai dengan Look and Feel yang baru
        updateUI();
    }

    /**
     * Menambahkan efek hover (saat mouse berada di atas tombol).
     */
    private void addMouseEffects() {
        // Tambahkan MouseListener untuk mendeteksi saat mouse masuk dan keluar dari tombol
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(238, 238, 238));  // Warna abu-abu saat disentuh
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);  // Kembali ke warna putih saat tidak disentuh
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
     * Menggambarkan tombol dengan sudut melengkung
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Mengatur anti-aliasing agar hasil gambar lebih halus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Mengatur warna latar belakang
        g2.setColor(getBackground());
        
        // Menggambar tombol dengan sudut melengkung sesuai cornerRadius
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Memanggil paintComponent dari JButton untuk menangani teks dan ikon
        super.paintComponent(g);
        
        g2.dispose(); // Menghilangkan resources Graphics2D
    }
}
