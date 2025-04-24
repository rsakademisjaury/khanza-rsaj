package widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

/**
 * MenuBar yang mengikuti tampilan sistem operasi.
 * 
 * @author dosen
 */
public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = 1L;
    private BufferedImage gradientImage;

    public MenuBar() {
        super();
        setBorder(BorderFactory.createEmptyBorder(5, 6, 6, 6));
        setFont(getSystemFont()); // Atur font sesuai dengan sistem operasi
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setUpGradientImage();

        if (isOpaque()) {
            g.drawImage(gradientImage, 0, 0, getWidth(), getHeight(), null);
        }
    }

    private void setUpGradientImage() {
        gradientImage = new BufferedImage(1, getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) gradientImage.getGraphics();

        // Menggunakan satu warna untuk latar belakang (kode RGB [0, 156, 215])
        Color menuColor = new Color(0, 156, 215); // Kode RGB yang diinginkan

        // Menggunakan warna yang ditentukan
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(menuColor);
        g.fillRect(0, 0, 1, getHeight());
        g.dispose();
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
