package widget;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * TextBox2 yang mengikuti font sistem operasi (Windows atau Mac).
 * 
 * @author usu
 */
public class TextBox2 extends JTextField {

    public TextBox2() {
        super();

        // Mengatur font sesuai dengan sistem operasi
        setFont(getSystemFont());

        // Mengatur warna pemilihan teks
        setSelectionColor(new Color(173, 216, 230)); // Warna biru muda yang lembut
        setSelectedTextColor(Color.BLACK); // Warna teks yang lebih kontras saat dipilih

        // Mengatur warna teks dan latar belakang
        setForeground(new Color(51, 51, 51)); // Warna teks gelap untuk keterbacaan
        setBackground(new Color(255, 255, 255)); // Latar belakang putih untuk kesan bersih dan profesional

        // Mengatur perataan teks
        setHorizontalAlignment(LEFT);

        // Menetapkan ukuran area teks
        setColumns(20); // Mengatur jumlah kolom untuk area teks
        setPreferredSize(new java.awt.Dimension(200, 30)); // Menetapkan ukuran yang lebih baik untuk tampilan
    }

    /**
     * Method untuk mendapatkan font yang sesuai dengan sistem operasi.
     */
    private Font getSystemFont() {
        // Mendapatkan font dari UIManager berdasarkan sistem operasi
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
