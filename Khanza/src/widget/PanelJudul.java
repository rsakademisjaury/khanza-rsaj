package widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * PanelJudul yang mengikuti tampilan sistem operasi Windows dengan efek gradien.
 * 
 * @author dosen
 */
public class PanelJudul extends JPanel {
    private static final long serialVersionUID = 1L;
    private BufferedImage gradientImage;
    private BufferedImage lightImage;

    // Warna yang disesuaikan untuk menciptakan tampilan Windows
    private final Color light = new Color(255, 255, 255, 0.5F); // Warna terang
    private final Color dark = new Color(255, 255, 255, 0.0F);  // Warna gelap
    private Color colorTop; // Warna gradien atas
    private Color colorBottom; // Warna gradien bawah

    public PanelJudul() {
        super();
        setDefaultColors();
    }

    private void setDefaultColors() {
        // Mengambil warna dari UIManager untuk mengikuti tema Windows
        colorTop = UIManager.getColor("Panel.background"); // Warna atas
        colorBottom = UIManager.getColor("Panel.foreground"); // Warna bawah
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setUpGradientImage();
        setUpLightImage();

        if (isOpaque()) {
            g.drawImage(gradientImage, 0, 0, getWidth(), getHeight(), null);
            g.drawImage(lightImage, 0, 0, getWidth(), getHeight() / 2, null);
        }
    }

    private void setUpGradientImage() {
        gradientImage = new BufferedImage(1, getHeight(), BufferedImage.TYPE_INT_ARGB);

        GradientPaint paint = new GradientPaint(0, 0, colorTop, 0, getHeight(), colorBottom);

        Graphics2D g = (Graphics2D) gradientImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(paint);
        g.fillRect(0, 0, 1, getHeight());
        g.dispose();
    }

    private void setUpLightImage() {
        lightImage = new BufferedImage(1, getHeight() / 2, BufferedImage.TYPE_INT_ARGB);

        GradientPaint paint = new GradientPaint(0, 0, light, 0, getHeight(), dark);

        Graphics2D g = (Graphics2D) lightImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(paint);
        g.fillRect(0, 0, 1, getHeight() / 2);
        g.dispose();
    }
}
