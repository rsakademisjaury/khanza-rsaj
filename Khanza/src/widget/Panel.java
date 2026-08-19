package widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Panel yang mengikuti tampilan sistem operasi Windows.
 * 
 * @author dosen
 */
public class Panel extends JPanel {

    private static final long serialVersionUID = -1;
    private BufferedImage gradientImage;

    public Panel() {
        super();
        // this.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(50,60,70)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            setUpGradient(g2);
            g2.drawImage(gradientImage, 0, 0, getWidth(), getHeight(), null);

            int width = getWidth();
            int height = getHeight() * 5 / 100;

            Color light = new Color(1F, 1F, 1F, 0.5F);
            Color dark = new Color(1F, 1F, 1F, 0.0F);

            GradientPaint paint = new GradientPaint(0, 0, light, 0, height, dark);
            GeneralPath path = new GeneralPath();
            path.moveTo(0, 0);
            path.lineTo(0, height);
            path.curveTo(0, height, width / 2, height / 2, width, height);
            path.lineTo(width, 0);
            path.closePath();

            g2.setPaint(paint);
            g2.fill(path);

            paint = new GradientPaint(0, getHeight(), light, 0, getHeight() - height, dark);
            path = new GeneralPath();
            path.moveTo(0, getHeight());
            path.lineTo(0, getHeight() - height);
            path.curveTo(0, getHeight() - height, width / 2, getHeight() - height / 2, width, getHeight() - height);
            path.lineTo(width, getHeight());
            path.closePath();

            g2.setPaint(paint);
            g2.fill(path);
            g2.dispose();
        }
    }

    private void setUpGradient(Graphics2D g2) {
        gradientImage = new BufferedImage(1, getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Image = (Graphics2D) gradientImage.getGraphics();
        g2Image.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Mengambil warna dari UIManager untuk mengikuti tampilan Windows
        Color topColor = UIManager.getColor("Panel.background");
        Color bottomColor = UIManager.getColor("Panel.foreground");

        GradientPaint paint = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);

        g2Image.setPaint(paint);
        g2Image.fillRect(0, 0, 1, getHeight());
        g2Image.dispose();
    }
}
