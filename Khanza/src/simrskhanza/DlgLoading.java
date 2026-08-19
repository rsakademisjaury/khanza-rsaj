package simrskhanza;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferStrategy;
import javax.swing.JDialog;

/**
 * Loading dialog global yang tetap bergerak walaupun Swing EDT sedang sibuk.
 *
 * Seluruh isi loading dirender oleh Canvas + BufferStrategy pada thread khusus,
 * sehingga spinner, progress bar dan persentase tidak ikut membeku ketika
 * constructor/query form lama memblokir Event Dispatch Thread.
 */
public final class DlgLoading extends JDialog {

    private static final int DIALOG_WIDTH = 420;
    private static final int DIALOG_HEIGHT = 106;

    private final LoadingCanvas canvas = new LoadingCanvas();

    public DlgLoading(Window owner) {
        super(owner);
        initDialog();
    }

    private void initDialog() {
        setUndecorated(true);
        setModalityType(ModalityType.MODELESS);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setFocusableWindowState(false);
        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        getContentPane().setLayout(null);
        getContentPane().add(canvas);
        canvas.setBounds(0, 0, DIALOG_WIDTH, DIALOG_HEIGHT);

        try {
            setAlwaysOnTop(true);
        } catch (Throwable ignored) {
        }

        try {
            setAutoRequestFocus(false);
        } catch (Throwable ignored) {
        }

        // Bentuk kartu membulat. Jika OS/driver tidak mendukung, fallback tetap
        // berupa kotak putih biasa tanpa memengaruhi fungsi aplikasi.
        try {
            setShape(new RoundRectangle2D.Double(
                    0, 0, DIALOG_WIDTH, DIALOG_HEIGHT, 22, 22));
        } catch (Throwable ignored) {
        }
    }

    public void showLoading(Window owner, String message, int progress) {
        canvas.setMessage(message);
        canvas.setProgress(progress);

        if (owner != null) {
            setLocationRelativeTo(owner);
        } else {
            setLocationRelativeTo(null);
        }

        if (!isVisible()) {
            setVisible(true);
        }
        toFront();

        // BufferStrategy dibuat setelah peer/window benar-benar visible.
        canvas.startRenderer();

        // Paksa frame pertama muncul sebelum handler pembuka form masuk ke
        // constructor/query berat di EDT.
        try {
            canvas.renderOnce();
            Toolkit.getDefaultToolkit().sync();
        } catch (Throwable ignored) {
        }
    }

    /** Thread-safe: hanya mengubah model volatile, renderer khusus yang menggambar. */
    public void setMessage(String message) {
        canvas.setMessage(message);
    }

    /** Thread-safe: boleh dipanggil dari thread progress global. */
    public void setProgress(int progress) {
        canvas.setProgress(progress);
    }

    public int getProgress() {
        return canvas.getProgress();
    }

    public void closeLoading() {
        canvas.stopRenderer();
        setVisible(false);
        dispose();
    }

    private static final class LoadingCanvas extends Canvas implements Runnable {

        private static final Color BLUE = new Color(27, 104, 230);
        private static final Color TEXT = new Color(32, 45, 67);
        private static final Color TRACK = new Color(229, 236, 247);
        private static final Color BORDER = new Color(225, 231, 240);

        private volatile String message = "Memuat data...";
        private volatile int progress = 0;
        private volatile boolean running = false;
        private volatile Thread renderThread;

        LoadingCanvas() {
            setIgnoreRepaint(true);
            setBackground(Color.WHITE);
            setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        }

        void setMessage(String value) {
            if (value != null && !value.trim().isEmpty()) {
                message = value.trim();
            }
        }

        void setProgress(int value) {
            progress = Math.max(0, Math.min(100, value));
        }

        int getProgress() {
            return progress;
        }

        synchronized void startRenderer() {
            if (running) {
                return;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(2);
                }
            } catch (Throwable ignored) {
                // Renderer akan mencoba lagi pada loop berikutnya.
            }

            running = true;
            Thread thread = new Thread(this, "RSAJ-GlobalLoading-Renderer");
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY + 1);
            renderThread = thread;
            thread.start();
        }

        synchronized void stopRenderer() {
            running = false;
            Thread thread = renderThread;
            renderThread = null;
            if (thread != null) {
                thread.interrupt();
            }
        }

        @Override
        public void run() {
            while (running) {
                try {
                    renderOnce();
                    Thread.sleep(33L); // ~30 FPS, ringan tetapi tetap halus.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable ignored) {
                    try {
                        Thread.sleep(80L);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        void renderOnce() {
            BufferStrategy strategy = getBufferStrategy();
            if (strategy == null) {
                try {
                    createBufferStrategy(2);
                    strategy = getBufferStrategy();
                } catch (Throwable ignored) {
                    return;
                }
            }

            do {
                do {
                    Graphics2D g2 = null;
                    try {
                        g2 = (Graphics2D) strategy.getDrawGraphics();
                        drawFrame(g2);
                    } finally {
                        if (g2 != null) {
                            g2.dispose();
                        }
                    }
                } while (strategy.contentsRestored());

                strategy.show();
                Toolkit.getDefaultToolkit().sync();
            } while (strategy.contentsLost());
        }

        private void drawFrame(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            // Border sangat tipis agar kartu tetap bersih seperti mockup.
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, width - 1, height - 1, 21, 21);

            drawSpinner(g2, 42, 42);

            Font normal = createFont(Font.PLAIN, 14f);
            Font bold = createFont(Font.BOLD, 14f);

            g2.setFont(normal);
            g2.setColor(TEXT);
            String visibleMessage = ellipsize(g2, message, 238);
            g2.drawString(visibleMessage, 72, 42);

            String percentText = progress + "%";
            g2.setFont(bold);
            g2.setColor(BLUE);
            FontMetrics percentMetrics = g2.getFontMetrics();
            int percentX = 389 - percentMetrics.stringWidth(percentText);
            g2.drawString(percentText, percentX, 42);

            int barX = 72;
            int barY = 58;
            int barW = 316;
            int barH = 8;

            g2.setColor(TRACK);
            g2.fillRoundRect(barX, barY, barW, barH, barH, barH);

            int fillWidth = Math.round(barW * (progress / 100f));
            if (fillWidth > 0) {
                g2.setColor(BLUE);
                g2.fillRoundRect(barX, barY, fillWidth, barH, barH, barH);
            }
        }

        private void drawSpinner(Graphics2D g2, int cx, int cy) {
            long frame = (System.currentTimeMillis() / 75L) % 12L;
            double radius = 11.0;

            for (int i = 0; i < 12; i++) {
                int distance = (int) ((i - frame + 12L) % 12L);
                int alpha = Math.max(45, 255 - (distance * 18));
                double angle = Math.toRadians(i * 30.0 - 90.0);
                int x = (int) Math.round(cx + Math.cos(angle) * radius);
                int y = (int) Math.round(cy + Math.sin(angle) * radius);

                g2.setColor(new Color(BLUE.getRed(), BLUE.getGreen(), BLUE.getBlue(), alpha));
                g2.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        private static Font createFont(int style, float size) {
            Font font = new Font("Segoe UI", style, Math.round(size));
            if (!"Segoe UI".equalsIgnoreCase(font.getFamily())) {
                font = new Font(Font.SANS_SERIF, style, Math.round(size));
            }
            return font.deriveFont(size);
        }

        private static String ellipsize(Graphics2D g2, String text, int maxWidth) {
            if (text == null || text.isEmpty()) {
                return "Memuat data...";
            }

            FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(text) <= maxWidth) {
                return text;
            }

            String suffix = "...";
            int available = maxWidth - fm.stringWidth(suffix);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String candidate = result.toString() + text.charAt(i);
                if (fm.stringWidth(candidate) > available) {
                    break;
                }
                result.append(text.charAt(i));
            }
            return result.toString().trim() + suffix;
        }
    }
}
