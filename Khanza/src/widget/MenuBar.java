package widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
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

    public MenuBar() {
        super();
        setBorder(BorderFactory.createEmptyBorder(5, 6, 6, 6));
        setFont(getSystemFont()); // Atur font sesuai dengan sistem operasi
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque() || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gradasi = new GradientPaint(
                0, 0, new Color(7, 81, 138),
                getWidth(), 0, new Color(11, 167, 213));
        g2.setPaint(gradasi);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Ornamen halus mengikuti latar splash tanpa mengganggu teks menu.
        g2.setColor(new Color(125, 231, 245, 18));
        g2.fillOval((int) (getWidth() * 0.64), -70, 190, 140);
        g2.setColor(new Color(255, 255, 255, 18));
        g2.drawLine(0, 0, getWidth(), 0);
        g2.setColor(new Color(0, 65, 125, 45));
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
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
