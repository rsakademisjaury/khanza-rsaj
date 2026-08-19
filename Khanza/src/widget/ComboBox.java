package widget;

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * ComboBox mengikuti tampilan sistem operasi.
 * 
 * @author dosen3
 */
public final class ComboBox extends JComboBox {

    private static final long serialVersionUID = 1L;

    public ComboBox() {
        super();
        setDefaults();
        applySystemLookAndFeel();
    }

    /**
     * Mengatur properti default ComboBox.
     */
    private void setDefaults() {
        // Mengatur font default yang digunakan oleh sistem operasi
        setFont(UIManager.getFont("ComboBox.font"));
        
        // Mengatur warna latar belakang dan teks default
        setBackground(new Color(255, 255, 255));
        setForeground(new Color(50, 50, 50));

        // Menentukan ukuran ComboBox
        setSize(WIDTH, 23);
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

        // Mengatur tampilan ComboBox agar mengikuti sistem operasi
        setFont(UIManager.getFont("ComboBox.font"));
        setBackground(UIManager.getColor("ComboBox.background"));
        setForeground(UIManager.getColor("ComboBox.foreground"));
    }
}
