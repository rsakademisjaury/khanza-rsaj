package widget;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Label yang mengikuti tampilan sistem operasi Windows.
 * 
 * @author usu
 */
public class Label extends JLabel {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    public Label() {
        super();
        setDefaults();
    }

    /**
     * Mengatur properti default label.
     */
    private void setDefaults() {
        // Mengatur warna dan font dari UIManager
        setForeground(UIManager.getColor("Label.foreground")); // Mengambil warna foreground dari sistem
        setFont(UIManager.getFont("Label.font")); // Mengambil font dari sistem
        
        // Mengatur alignment
        setHorizontalAlignment(RIGHT);
        setVerticalAlignment(CENTER);
        setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(CENTER);
    }
}