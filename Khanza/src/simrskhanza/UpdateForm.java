package simrskhanza;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Tampilan proses pemeriksaan dan pembaruan SIMRS.
 *
 * Method updateStatus() dan updateProgress() dipertahankan agar tetap
 * kompatibel dengan class updater yang sudah digunakan sebelumnya.
 */
public class UpdateForm extends JFrame {

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel percentLabel;
    private Timer animationTimer;
    private int animationFrame;

    public UpdateForm() {
        buildModernUpdateSplash();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildModernUpdateSplash() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));
        setPreferredSize(new Dimension(720, 420));
        setSize(720, 420);
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setUndecorated(true);
        setType(Window.Type.POPUP);

        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (Exception unsupportedTransparency) {
            setBackground(new Color(8, 66, 119));
        }

        ModernUpdatePanel panel = new ModernUpdatePanel();
        panel.setLayout(null);
        setContentPane(panel);

        JLabel appBadge = new JLabel("SIMRS  KHANZA");
        appBadge.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        appBadge.setForeground(new Color(199, 238, 255));
        appBadge.setBounds(72, 55, 210, 24);
        panel.add(appBadge);

        JLabel welcome = new JLabel("Pembaruan Sistem");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcome.setForeground(new Color(221, 245, 255));
        welcome.setBounds(72, 98, 300, 28);
        panel.add(welcome);

        JLabel hospital = new JLabel("RS. Akademis Jaury Jusuf Putera");
        hospital.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 31));
        hospital.setForeground(Color.WHITE);
        hospital.setBounds(72, 126, 500, 48);
        panel.add(hospital);

        JLabel subtitle = new JLabel("Memeriksa keamanan dan versi terbaru aplikasi");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(203, 237, 250));
        subtitle.setBounds(74, 174, 440, 28);
        panel.add(subtitle);

        GearAnimationPanel gears = new GearAnimationPanel();
        gears.setBounds(545, 92, 125, 118);
        panel.add(gears);

        statusLabel = new JLabel("Memeriksa pembaruan sistem...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(74, 276, 510, 25);
        panel.add(statusLabel);

        percentLabel = new JLabel("0%");
        percentLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        percentLabel.setForeground(Color.WHITE);
        percentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        percentLabel.setBounds(590, 276, 58, 25);
        panel.add(percentLabel);

        progressBar = new ModernProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setBounds(74, 310, 574, 14);
        panel.add(progressBar);

        JLabel footer = new JLabel("Powered by SIMKES Khanza  |  TIM IT RS AJJP");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(new Color(181, 225, 243));
        footer.setBounds(74, 349, 410, 22);
        panel.add(footer);

        animationTimer = new Timer(35, e -> {
            animationFrame = (animationFrame + 1) % 3600;
            gears.repaint();
            progressBar.repaint();
            panel.repaint();
        });
        animationTimer.start();
    }

    /** Memperbarui persen dan keterangan proses secara bersamaan. */
    public void updateProgress(int progress, String step) {
        runOnEdt(() -> {
            setProgressValue(progress);
            if (step != null && !step.trim().isEmpty()) {
                statusLabel.setText(step.trim());
            }
        });
    }

    /** Memperbarui keterangan proses tanpa mengubah nilai progress. */
    public void updateStatus(String status) {
        runOnEdt(() -> {
            if (status != null && !status.trim().isEmpty()) {
                statusLabel.setText(status.trim());
            }
        });
    }

    /** Memperbarui nilai progress tanpa mengubah keterangannya. */
    public void updateProgress(int progress) {
        runOnEdt(() -> setProgressValue(progress));
    }

    private void setProgressValue(int progress) {
        int safeProgress = Math.max(0, Math.min(100, progress));
        progressBar.setValue(safeProgress);
        percentLabel.setText(safeProgress + "%");
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    @Override
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        super.dispose();
    }

    private class ModernUpdatePanel extends JPanel {
        ModernUpdatePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setPaint(new GradientPaint(
                    0, 0, new Color(8, 66, 119),
                    getWidth(), getHeight(), new Color(0, 157, 211)));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));

            g2.setColor(new Color(255, 255, 255, 18));
            g2.fill(new Ellipse2D.Double(getWidth() - 210, -92, 290, 290));
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fill(new Ellipse2D.Double(-115, getHeight() - 150, 270, 270));

            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private class GearAnimationPanel extends JPanel {
        GearAnimationPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double angle = animationFrame * 0.035;
            paintGear(g2, 46, 52, 12, 29, 38, angle, 13);
            paintGear(g2, 91, 76, 9, 17, 24, -angle * 1.35, 8);
            paintGear(g2, 91, 28, 8, 12, 18, -angle * 1.35, 6);
            g2.dispose();
        }

        private void paintGear(Graphics2D g2, double centerX, double centerY,
                int teeth, double rootRadius, double tipRadius,
                double angle, double holeRadius) {
            Path2D.Double gear = new Path2D.Double();
            int points = teeth * 4;

            for (int i = 0; i < points; i++) {
                double pointAngle = angle + (Math.PI * 2.0 * i / points);
                int toothPoint = i % 4;
                double radius = (toothPoint == 1 || toothPoint == 2)
                        ? tipRadius : rootRadius;
                double x = centerX + Math.cos(pointAngle) * radius;
                double y = centerY + Math.sin(pointAngle) * radius;
                if (i == 0) {
                    gear.moveTo(x, y);
                } else {
                    gear.lineTo(x, y);
                }
            }
            gear.closePath();

            g2.setColor(new Color(255, 255, 255, 28));
            g2.fill(gear);
            g2.setColor(new Color(223, 247, 255, 235));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(gear);

            g2.setColor(new Color(18, 126, 169, 210));
            g2.fill(new Ellipse2D.Double(centerX - holeRadius, centerY - holeRadius,
                    holeRadius * 2, holeRadius * 2));
            g2.setColor(new Color(223, 247, 255, 235));
            g2.draw(new Ellipse2D.Double(centerX - holeRadius, centerY - holeRadius,
                    holeRadius * 2, holeRadius * 2));
        }
    }

    private class ModernProgressBar extends JProgressBar {
        ModernProgressBar() {
            setOpaque(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            int arc = height;

            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            int fillWidth = (int) Math.round(width * (getValue() / 100.0));
            if (fillWidth > 0) {
                g2.setPaint(new GradientPaint(0, 0, new Color(117, 238, 255),
                        width, 0, Color.WHITE));
                g2.fillRoundRect(0, 0, fillWidth, height, arc, arc);

                java.awt.Shape oldClip = g2.getClip();
                g2.clip(new RoundRectangle2D.Double(0, 0, fillWidth, height, arc, arc));
                int shimmerX = (animationFrame * 5) % (Math.max(width, 1) + 90) - 70;
                g2.setPaint(new GradientPaint(shimmerX, 0, new Color(255, 255, 255, 0),
                        shimmerX + 65, 0, new Color(255, 255, 255, 165), true));
                g2.fillRect(shimmerX, 0, 70, height);
                g2.setClip(oldClip);
            }
            g2.dispose();
        }
    }
}
