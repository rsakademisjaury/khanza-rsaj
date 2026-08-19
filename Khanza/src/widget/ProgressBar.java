package widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * Kelas ProgressBar yang menampilkan status kemajuan dengan efek gradien.
 */
public class ProgressBar extends JProgressBar {

    private static final long serialVersionUID = 1L;
    private Timer timer;
    private int percent;
    private final Color lightColor = new Color(1F, 1F, 1F, 0.4F);
    private final Color darkColor = new Color(1F, 1F, 1F, 0.4F);
    private final Color blackColor = Color.DARK_GRAY;
    private final Color accentColor = Color.MAGENTA;

    /**
     * Menginisialisasi ProgressBar.
     */
    public ProgressBar() {
        super();
        setPercent(0);
        setOpaque(false);
        setBorderPainted(false);
        super.setIndeterminate(false);
    }

    /**
     * Mengambil timer yang digunakan untuk animasi.
     * 
     * @return Timer yang sedang berjalan.
     */
    private Timer getTimer() {
        if (timer == null) {
            timer = new Timer(10, (ActionEvent e) -> {
                if (getPercent() >= 100) {
                    setPercent(0);
                }
                setValue(0);
                setPercent(getPercent() + 1);
            });
        }
        return timer;
    }

    /**
     * Mengambil nilai persentase saat ini.
     * 
     * @return Persentase kemajuan saat ini.
     */
    private int getPercent() {
        return percent;
    }

    /**
     * Mengatur nilai persentase kemajuan.
     * 
     * @param percent Persentase kemajuan baru.
     */
    private void setPercent(int percent) {
        this.percent = percent;
        repaint();
    }

    @Override
    public void setIndeterminate(boolean newValue) {
        if (newValue) {
            setPercent(0);
            getTimer().start();
        } else {
            if (getTimer().isRunning()) {
                timer.stop();
            }
            setPercent(0);
            setValue(0);
            super.setIndeterminate(newValue);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Menggambar latar belakang
        GradientPaint backgroundPaint = new GradientPaint(0, 0, blackColor, 0, getHeight(), accentColor);
        g2.setPaint(backgroundPaint);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Menggambar kemajuan berdasarkan persentase
        if (getPercent() > 0) {
            GradientPaint progressPaint = new GradientPaint(0, 0, accentColor, 0, getHeight(), blackColor);
            g2.setPaint(progressPaint);

            int filledWidth = getPercent() * getWidth() / 100;
            int barWidth = getWidth() * 10 / 100;
            Shape progressShape = new Rectangle2D.Double(filledWidth, 0, barWidth, getHeight());
            g2.fill(progressShape);
        }

        // Menggambar nilai saat ini
        if (getValue() > 0 && getValue() < getMaximum()) {
            int total = getMaximum() - getMinimum();
            double rate = (getWidth() * 1.0) / (total * 1.0);
            int currentValue = getValue() - getMinimum();

            GradientPaint currentPaint = new GradientPaint(0, 0, accentColor, 0, getHeight(), blackColor);
            g2.setPaint(currentPaint);

            Shape currentShape = new Rectangle2D.Double(0, 0, currentValue * rate, getHeight());
            g2.fill(currentShape);
        }

        // Menggambar lapisan cahaya
        GradientPaint lightPaint = new GradientPaint(0, 0, lightColor, 0, getHeight() * 3 / 4, darkColor);
        g2.setPaint(lightPaint);
        g2.fillRect(3, 3, getWidth(), getHeight() * 3 / 4);

        // Menggambar border
        g2.setColor(blackColor);
        g2.drawRect(3, 3, getWidth(), getHeight());

        g2.dispose();
    }
}
