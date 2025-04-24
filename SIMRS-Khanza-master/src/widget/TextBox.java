//package widget;
//
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.event.FocusEvent;
//import java.awt.event.FocusListener;
//import java.awt.geom.RoundRectangle2D;
//import javax.swing.BorderFactory;
//import javax.swing.UIManager;
//import javax.swing.border.Border;
//import javax.swing.text.Caret;
//import javax.swing.text.DefaultCaret;
//import usu.widget.glass.TextBoxGlass;
//
///**
// * TextBox yang mengikuti font sesuai dengan sistem operasi (Windows atau Mac),
// * dengan radius lengkung 15 dan efek hover (berubah warna saat disentuh mouse).
// * 
// * @author usu
// */
//public class TextBox extends TextBoxGlass {
//
//    private boolean isHovered = false; // Flag untuk melacak apakah textbox disentuh mouse
//    private final int cornerRadius = 12; // Radius lengkungan
//
//    public TextBox() {
//        super();
//
//        // Mengatur font sesuai dengan sistem operasi
//        setFont(getSystemFont());
//
//        // Mengatur warna pemilihan teks untuk konsistensi
//        setSelectionColor(new Color(0, 156, 215)); // Warna biru yang lebih redup
//        setSelectedTextColor(Color.BLACK); // Warna teks yang lebih kontras saat dipilih
//
//        // Mengatur warna teks dan latar belakang
//        setForeground(new Color(51, 51, 51)); // Warna teks gelap untuk keterbacaan
//        setBackground(new Color(255, 255, 255)); // Latar belakang putih untuk kesan bersih dan profesional
//
//        // Mengatur perataan teks
//        setHorizontalAlignment(LEFT);
//
//        // Menetapkan ukuran area teks
//        setColumns(20); // Menetapkan jumlah kolom untuk area teks
//        setSize(getPreferredSize().width, 30); // Menetapkan tinggi lebih besar untuk memberikan ruang yang lebih baik
//
//        // Menambahkan efek hover pada mouse
//        setFocusable(true); // TextBox dapat difokuskan
//        addFocusListener(new FocusListener() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                isHovered = true; // Ketika TextBox disentuh
//                repaint(); // Gambar ulang agar perubahan terlihat
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                isHovered = false; // Ketika TextBox tidak disentuh lagi
//                repaint(); // Kembali ke warna semula
//            }
//        });
//
//        // Mengatur border transparan untuk mengakomodasi lengkungan
//        setOpaque(false);
//        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
//        setBorder(emptyBorder);
//
//        // Mengatur kursor teks agar terlihat (blinking cursor)
//        Caret caret = new DefaultCaret();
//        setCaret(caret); // Menetapkan caret default
//        setCaretColor(new Color(0, 156, 215)); // Warna kursor teks diatur ke hitam
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g;
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//        // Warna border bergantung pada apakah mouse sedang menyentuh TextBox
//        Color borderColor = isHovered ? new Color(0, 156, 215) : new Color(180, 180, 180);
//
//        // Menggambar latar belakang dengan radius lengkung
//        g2.setColor(getBackground());
//        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
//
//        // Menggambar border dengan radius lengkung
//        g2.setColor(borderColor);
//        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
//
//        // Menggambar teks di dalam textbox tanpa memanggil super.paintComponent
//        g2.setColor(getForeground());
//        g2.setFont(getFont());
//        g2.drawString(getText(), 10, getHeight() / 2 + g.getFontMetrics().getAscent() / 2 - 2);
//    }
//
//    /**
//     * Method untuk mendapatkan font yang sesuai dengan sistem operasi.
//     */
//    private Font getSystemFont() {
//        // Mendapatkan Look and Feel dari UIManager
//        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
//        Font systemFont;
//
//        // Mengatur font berdasarkan Look and Feel
//        if (lookAndFeel.contains("Windows")) {
//            systemFont = new Font("Segoe UI", Font.PLAIN, 12); // Font untuk Windows
//        } else if (lookAndFeel.contains("Mac")) {
//            systemFont = new Font("Lucida Grande", Font.PLAIN, 12); // Font untuk Mac
//        } else {
//            systemFont = new Font("Tahoma", Font.PLAIN, 12); // Font default untuk sistem lain
//        }
//
//        return systemFont;
//    }
//}

package widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import usu.widget.glass.TextBoxGlass;

/**
 * TextBox yang mengikuti font sesuai dengan sistem operasi (Windows atau Mac),
 * dengan radius lengkung 15 dan efek hover (berubah warna saat disentuh mouse).
 * 
 * @author usu
 */
public class TextBox extends TextBoxGlass {

    private boolean isHovered = false; // Flag untuk melacak apakah textbox disentuh mouse
    private final int cornerRadius = 11; // Radius lengkungan

    public TextBox() {
        super();

        // Mengatur font sesuai dengan sistem operasi
        setFont(getSystemFont());

        // Mengatur warna pemilihan teks untuk konsistensi
        setSelectionColor(new Color(0, 156, 215)); // Warna biru yang lebih redup
        setSelectedTextColor(Color.BLACK); // Warna teks yang lebih kontras saat dipilih

        // Mengatur warna teks dan latar belakang
        setForeground(new Color(51, 51, 51)); // Warna teks gelap untuk keterbacaan
        setBackground(new Color(255, 255, 255)); // Latar belakang putih untuk kesan bersih dan profesional

        // Mengatur perataan teks
        setHorizontalAlignment(LEFT);

        // Menetapkan ukuran area teks
        setColumns(20); // Menetapkan jumlah kolom untuk area teks
        setSize(getPreferredSize().width, 30); // Menetapkan tinggi lebih besar untuk memberikan ruang yang lebih baik

        // Menambahkan efek hover pada mouse
        setFocusable(true); // TextBox dapat difokuskan
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isHovered = true; // Ketika TextBox disentuh
                repaint(); // Gambar ulang agar perubahan terlihat
            }

            @Override
            public void focusLost(FocusEvent e) {
                isHovered = false; // Ketika TextBox tidak disentuh lagi
                repaint(); // Kembali ke warna semula
            }
        });

        // Mengatur border transparan untuk mengakomodasi lengkungan
        setOpaque(false);  // Membuat latar belakang menjadi transparan
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 10, 5, 5);
        setBorder(emptyBorder);

        // Mengatur kursor teks agar terlihat (blinking cursor)
        DefaultCaret caret = new DefaultCaret();
        caret.setBlinkRate(500); // Mengatur kedipan kursor
        setCaret(caret);
        setCaretColor(Color.BLACK); // Warna kursor teks diatur ke hitam
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Menghilangkan latar belakang hitam dengan tidak memanggil super.paintComponent(g)
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Warna border bergantung pada apakah mouse sedang menyentuh TextBox
        Color borderColor = isHovered ? new Color(0, 156, 215) : new Color(180, 180, 180);

        // Menggambar latar belakang dengan radius lengkung
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Menggambar border dengan radius lengkung
        g2.setColor(borderColor);
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

        // Memanggil super.paintComponent(g) hanya untuk menggambar teks dan kursor
        // Menggunakan clipping untuk hanya menggambar teks tanpa menggambar background atau border default
        g2.setClip(new RoundRectangle2D.Double(5, 5, getWidth() - 10, getHeight() - 10, cornerRadius, cornerRadius));
        super.paintComponent(g); // Ini hanya mengatur teks, kursor, dan highlight
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Kita override paintBorder agar tidak ada border tambahan yang digambar
        // Membiarkan pengaturan border melalui paintComponent di atas saja
    }

    /**
     * Method untuk mendapatkan font yang sesuai dengan sistem operasi.
     */
    private Font getSystemFont() {
        // Mendapatkan Look and Feel dari UIManager
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        Font systemFont;

        // Mengatur font berdasarkan Look and Feel
        if (lookAndFeel.contains("Windows")) {
            systemFont = new Font("Segoe UI", Font.PLAIN, 12); // Font untuk Windows
        } else if (lookAndFeel.contains("Mac")) {
            systemFont = new Font("Lucida Grande", Font.PLAIN, 12); // Font untuk Mac
        } else {
            systemFont = new Font("Tahoma", Font.PLAIN, 12); // Font default untuk sistem lain
        }

        return systemFont;
    }
}
