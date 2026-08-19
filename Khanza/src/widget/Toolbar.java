package widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JToolBar;

/**
 *
 * @author usu
 */
public class Toolbar extends JToolBar {

    private static final long serialVersionUID = 1L;
    private BufferedImage gradientImage;
    private BufferedImage lightImage;
    private final Color light = new Color(240, 240, 240, 150); // Warna terang dengan transparansi
    private final Color dark = new Color(255, 255, 255, 0); // Warna gelap transparan
    private final Color black = new Color(50, 50, 50); // Warna hitam gelap
    private final Color primaryColor = new Color(70, 130, 180); // Warna biru yang lebih lembut

    /**
     * 
     */
    public Toolbar() {
        super();
        setFloatable(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Menambahkan padding
        setBackground(new Color(255, 255, 255)); // Warna latar belakang putih untuk kesan bersih
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

    /**
     * Membuat gambar gradient background
     */
    private void setUpGradientImage() {
        gradientImage = new BufferedImage(1, getHeight(), BufferedImage.TYPE_INT_ARGB);

        GradientPaint paint = new GradientPaint(0, 0, primaryColor, 0, getHeight(), black);

        Graphics2D g = (Graphics2D) gradientImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(paint);
        g.fillRect(0, 0, 1, getHeight());
        g.dispose();
    }

    /**
     * Membuat gambar light effect
     */
    private void setUpLightImage() {
        lightImage = new BufferedImage(1, getHeight() / 2, BufferedImage.TYPE_INT_ARGB);

        GradientPaint paint = new GradientPaint(0, 0, light, 0, getHeight(), dark);

        Graphics2D g = (Graphics2D) lightImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(paint);
        g.fillRect(2, 2, 5, getHeight() / 2);
        g.dispose();
    }
}
