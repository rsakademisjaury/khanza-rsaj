package widget;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Kelas RadioButton yang mengikuti font sesuai dengan sistem operasi (Windows atau Mac).
 */
public class RadioButton extends JRadioButton {

    private static final long serialVersionUID = 1L;

    /**
     * Menginisialisasi RadioButton dengan gaya yang sesuai dengan sistem operasi.
     */
    public RadioButton() {
        super();
        
        // Mengatur font sesuai dengan sistem operasi
        setFont(getSystemFont());
        
        // Mengatur warna latar belakang, teks, dan border
        setBackground(Color.WHITE);
        setForeground(new Color(50, 50, 50));        
        setFocusPainted(false); // Menghilangkan efek fokus
        setBorder(new EmptyBorder(2, 2, 2, 2)); // Padding untuk tombol radio
        
        // Agar latar belakang terlihat
        setOpaque(true);

        // Menyesuaikan ukuran
        setSize(getPreferredSize().width, 23); 
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
            systemFont = new Font("Segoe UI", Font.PLAIN, 11); // Font untuk Windows
        } else if (lookAndFeel.contains("Mac")) {
            systemFont = new Font("Lucida Grande", Font.PLAIN, 11); // Font untuk Mac
        } else {
            systemFont = new Font("Tahoma", Font.PLAIN, 11); // Font default untuk sistem lain
        }

        return systemFont;
    }
}
