package bridging;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/** Semua ukuran dalam koordinat AWT/logical pixel; mendukung monitor dengan posisi negatif. */
public final class SsrmeUi {
    private SsrmeUi() { }
    public static Rectangle usable(Rectangle bounds, Insets taskbar) {
        return new Rectangle(bounds.x + taskbar.left, bounds.y + taskbar.top,
                Math.max(1, bounds.width-taskbar.left-taskbar.right),
                Math.max(1, bounds.height-taskbar.top-taskbar.bottom));
    }
    public static void fit(Window window, Window owner) {
        GraphicsConfiguration gc = owner == null ? window.getGraphicsConfiguration() : owner.getGraphicsConfiguration();
        if (gc == null) gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle area=usable(gc.getBounds(), Toolkit.getDefaultToolkit().getScreenInsets(gc));
        window.setMinimumSize(new Dimension(Math.min(680,area.width),Math.min(440,area.height)));
        window.setBounds(area);
    }
}
