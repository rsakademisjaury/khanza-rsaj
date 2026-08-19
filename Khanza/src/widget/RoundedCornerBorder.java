package widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;

/**
 * Border dengan sudut membulat yang mengikuti gaya antarmuka pengguna Windows.
 */
public class RoundedCornerBorder extends AbstractBorder {

    /**
     * Menggambar border dengan sudut membulat.
     */
    @Override 
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arcSize = 8; // Ukuran sudut membulat
        int w = width - 1;
        int h = height - 1;

        Area round = new Area(new RoundRectangle2D.Float(x, y, w, h, arcSize, arcSize));
        Container parent = c.getParent();
        
        // Mengatur warna latar belakang sesuai dengan warna parent
        if (parent != null) {
            g2.setColor(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
            corner.subtract(round);
            g2.fill(corner);
        }
        
        // Menggambar border
        g2.setColor(c.getForeground());
        g2.draw(round);
        g2.dispose();
    }

    /**
     * Mendapatkan ukuran border.
     */
    @Override 
    public Insets getBorderInsets(Component c) {
        return new Insets(5, 5, 5, 5); // Memberikan ruang di sekitar border
    }

    /**
     * Mendapatkan ukuran border dengan parameter insets.
     */
    @Override 
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = 5;
        insets.top = insets.bottom = 5; // Memberikan ruang di sekitar border
        return insets;
    }
}
