package simrskhanza;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/** Latar opsional dari wallpaper login, tanpa capture form/data pasien. */
final class SessionLockBackdrop {
    private static final long GB = 1024L * 1024L * 1024L;
    private static final long MIN_HEADROOM = 64L * 1024L * 1024L;
    private static final long BUILD_BUDGET_NANOS = 900_000_000L;
    private static final long PAINT_BUDGET_NANOS = 75_000_000L;

    private SessionLockBackdrop() { }

    // Kebijakan murni juga dipakai verifikasi: AUTO konservatif seperti login FULL.
    static boolean eligible(String os, long physicalRam, int cores,
            long heapHeadroom, boolean graphicsSupported) {
        return os != null && !os.toLowerCase(java.util.Locale.ROOT).contains("windows 7")
                && physicalRam > 6 * GB && cores >= 4
                && heapHeadroom >= MIN_HEADROOM && graphicsSupported;
    }

    static boolean eligible(Window owner) {
        try {
            long physicalRam = -1;
            java.lang.management.OperatingSystemMXBean bean =
                    java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof com.sun.management.OperatingSystemMXBean) {
                physicalRam = ((com.sun.management.OperatingSystemMXBean) bean)
                        .getTotalPhysicalMemorySize();
            }
            Runtime runtime = Runtime.getRuntime();
            GraphicsConfiguration configuration = owner == null ? null : owner.getGraphicsConfiguration();
            // Pengecekan ini konservatif; hasil digambar opaque, tanpa setOpacity/setShape.
            boolean graphicsSupported = configuration != null
                    && configuration.getDevice().isWindowTranslucencySupported(
                            GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
            return eligible(System.getProperty("os.name", ""), physicalRam,
                    runtime.availableProcessors(), runtime.maxMemory() - runtime.totalMemory()
                            + runtime.freeMemory(), graphicsSupported);
        } catch (RuntimeException | LinkageError ex) {
            return false;
        }
    }

    static BufferedImage build(ImageIcon wallpaper) {
        return build(wallpaper, BUILD_BUDGET_NANOS);
    }

    static BufferedImage build(ImageIcon wallpaper, long budgetNanos) {
        if (wallpaper == null || budgetNanos <= 0 || Thread.currentThread().isInterrupted()) {
            return null;
        }
        long start = System.nanoTime();
        BufferedImage small = null;
        BufferedImage horizontal = null;
        BufferedImage result = null;
        boolean success = false;
        try {
            int w = wallpaper.getIconWidth(), h = wallpaper.getIconHeight();
            if (w <= 0 || h <= 0) {
                return null;
            }
            double scale = Math.min(1.0, Math.min(960.0 / w, 540.0 / h));
            int width = Math.max(1, (int) Math.round(w * scale));
            int height = Math.max(1, (int) Math.round(h * scale));
            small = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = small.createGraphics();
            try {
                g.setColor(SessionIdleLock.LOCK_BACKGROUND);
                g.fillRect(0, 0, width, height);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                Image source = wallpaper.getImage();
                if (!g.drawImage(source, 0, 0, width, height, null)) {
                    return null;
                }
            } finally {
                g.dispose();
            }
            if (width < 13 || height < 13 || overBudget(start, budgetNanos)) {
                return null;
            }
            float[] weights = new float[13];
            java.util.Arrays.fill(weights, 1f / weights.length);
            horizontal = new ConvolveOp(new Kernel(13, 1, weights), ConvolveOp.EDGE_NO_OP, null)
                    .filter(small, null);
            if (overBudget(start, budgetNanos)) {
                return null;
            }
            result = new ConvolveOp(new Kernel(1, 13, weights), ConvolveOp.EDGE_NO_OP, null)
                    .filter(horizontal, null);
            g = result.createGraphics();
            try {
                // Tint sama dengan backdrop login. Alpha dibaurkan ke bitmap RGB,
                // bukan transparansi native atau transparansi terhadap form pasien.
                g.setColor(new Color(0, 38, 72, 70));
                g.fillRect(0, 0, width, height);
            } finally {
                g.dispose();
            }
            if (overBudget(start, budgetNanos)) {
                return null;
            }
            success = true;
            return result;
        } catch (RuntimeException | OutOfMemoryError ex) {
            return null;
        } finally {
            if (small != null) small.flush();
            if (horizontal != null) horizontal.flush();
            if (!success && result != null) result.flush();
        }
    }

    private static boolean overBudget(long start, long budget) {
        return Thread.currentThread().isInterrupted() || System.nanoTime() - start > budget;
    }

    /** Cover gambar statis; dialog dan glass pane memakai koordinat latar sama. */
    static boolean paint(Graphics graphics, Component component, BufferedImage image) {
        Window window = SwingUtilities.getWindowAncestor(component);
        Window base = window instanceof SessionLockDialog ? window.getOwner() : window;
        return paint(graphics, component, image, base);
    }

    static boolean paint(Graphics graphics, Component component, BufferedImage image, Window base) {
        if (image == null) {
            return true;
        }
        long start = System.nanoTime();
        Graphics2D g = null;
        try {
            if (base == null || !component.isShowing()) return true;
            Point origin = base.getLocationOnScreen();
            Point local = component.getLocationOnScreen();
            double scale = Math.max(base.getWidth() / (double) image.getWidth(),
                    base.getHeight() / (double) image.getHeight());
            int width = (int) Math.ceil(image.getWidth() * scale);
            int height = (int) Math.ceil(image.getHeight() * scale);
            int x = (base.getWidth() - width) / 2 - (local.x - origin.x);
            int y = (base.getHeight() - height) / 2 - (local.y - origin.y);
            g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, x, y, width, height, null);
            return System.nanoTime() - start <= PAINT_BUDGET_NANOS;
        } catch (RuntimeException | OutOfMemoryError ex) {
            return false;
        } finally {
            if (g != null) g.dispose();
        }
    }
}
