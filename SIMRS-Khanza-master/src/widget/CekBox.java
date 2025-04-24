package widget;

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * CekBox mengikuti tampilan sistem operasi.
 * 
 * @author dosen3
 */
public class CekBox extends JCheckBox {

    private static final long serialVersionUID = 1L;

    public CekBox() {
        super();
        setDefaults();
        applySystemLookAndFeel();
    }

    /**
     * Mengatur properti default checkbox.
     */
    private void setDefaults() {
        // Menggunakan font dan warna yang diatur oleh sistem operasi
        setFont(UIManager.getFont("CheckBox.font"));
        setBackground(new Color(255, 255, 255));  // Default, bisa diganti oleh sistem OS
        setForeground(new Color(50, 50, 50));

        // Mengatur properti lain seperti sebelumnya
        setFocusPainted(false);
        setBorder(javax.swing.BorderFactory.createLineBorder(new Color(239, 244, 234)));
        setOpaque(true);
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

        // Mengatur tampilan checkbox agar mengikuti sistem operasi
        setFont(UIManager.getFont("CheckBox.font"));
        setBackground(UIManager.getColor("CheckBox.background"));
        setForeground(UIManager.getColor("CheckBox.foreground"));
    }
}
