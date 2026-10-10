package simrskhanza;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.MenuSelectionManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import java.awt.image.BufferedImage;

/**
 * Pengunci workstation ringan. Hanya input pengguna memperbarui waktu idle;
 * timer, alarm, repaint dan polling tetap berjalan tanpa memperpanjang sesi.
 * Tidak mengambil screenshot, memindahkan komponen login, atau mengubah akses.
 * Seluruh metode pengendali dijalankan di EDT.
 */
final class SessionIdleLock implements AutoCloseable {
    static final long DEFAULT_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);
    static final Color LOCK_BACKGROUND = new Color(224, 233, 240);

    private final JDialog initialLogin;
    private final Runnable showLogin;
    private final long timeoutNanos;
    private final Timer timer;
    private final InputQueue inputQueue;
    private final Map<JRootPane, ShieldState> shields = new IdentityHashMap<>();
    private Component previousFocus;
    private JDialog lockDialog;
    private long lastActivityNanos;
    private boolean active;
    private boolean locked;
    private boolean closed;
    private SwingWorker<BufferedImage, Void> backdropWorker;
    private BufferedImage backdropImage;

    SessionIdleLock(JDialog initialLogin, Runnable showLogin) {
        this(initialLogin, showLogin, DEFAULT_TIMEOUT_MILLIS);
    }

    // Interval pendek hanya untuk uji; produksi selalu lima menit.
    SessionIdleLock(JDialog initialLogin, Runnable showLogin, long timeoutMillis) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("SessionIdleLock harus dibuat di EDT");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("Timeout harus lebih dari nol");
        }
        this.initialLogin = initialLogin;
        this.showLogin = showLogin;
        timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        inputQueue = new InputQueue();
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(inputQueue);
        timer = new Timer((int) Math.min(1000L, timeoutMillis), evt -> checkTimeout());
        timer.setCoalesce(true);
    }

    void start() {
        if (closed || locked) {
            return;
        }
        active = true;
        lastActivityNanos = System.nanoTime();
        timer.start();
    }

    void stop() {
        active = false;
        timer.stop();
    }

    boolean isLocked() {
        return locked;
    }

    private boolean expired() {
        return active && System.nanoTime() - lastActivityNanos >= timeoutNanos;
    }

    private void checkTimeout() {
        if (!closed && !locked && expired()) {
            locked = true; // Tutup celah input sebelum masuk modal event loop.
            timer.stop();
            previousFocus = java.awt.KeyboardFocusManager
                    .getCurrentKeyboardFocusManager().getFocusOwner();
            MenuSelectionManager.defaultManager().clearSelectedPath();
            for (Window window : Window.getWindows()) {
                shield(window);
            }
            showLogin.run();
        }
    }

    /** Dipanggil hanya setelah password akun semula berhasil diverifikasi. */
    void unlock() {
        if (!locked) {
            return;
        }
        Component focus = previousFocus;
        previousFocus = null;
        release();
        start();
        if (focus != null && focus.isShowing()) {
            Window window = SwingUtilities.getWindowAncestor(focus);
            if (window != null) {
                window.toFront();
            }
            focus.requestFocusInWindow();
        }
    }

    /** Logout melepas pengunci tanpa mengaktifkan sesi atau draft akun lama. */
    void releaseForLogout() {
        stop();
        previousFocus = null;
        release();
    }

    private void release() {
        clearBackdrop();
        JDialog dialog = lockDialog;
        lockDialog = null;
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }
        for (Map.Entry<JRootPane, ShieldState> entry : shields.entrySet()) {
            JRootPane root = entry.getKey();
            ShieldState state = entry.getValue();
            state.shield.setVisible(false);
            root.setGlassPane(state.previousGlass);
            state.previousGlass.setVisible(state.previousVisible);
            root.revalidate();
            root.repaint();
        }
        shields.clear();
        locked = false;
    }

    /** Shield polos sudah terpasang sebelum worker wallpaper mulai. */
    void prepareBackdrop(ImageIcon wallpaper) {
        clearBackdrop();
        if (!locked || wallpaper == null || !SessionLockBackdrop.eligible(initialLogin.getOwner())) {
            System.out.println("[Timeout] Latar=POLOS (AUTO)");
            return;
        }
        startBackdropWorker(wallpaper);
    }

    private void startBackdropWorker(ImageIcon wallpaper) {
        backdropWorker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                return SessionLockBackdrop.build(wallpaper);
            }

            @Override
            protected void done() {
                if (backdropWorker != this || !locked || isCancelled()) return;
                backdropWorker = null;
                try {
                    applyBackdrop(get());
                } catch (Exception | OutOfMemoryError ex) {
                    clearBackdrop();
                }
                System.out.println("[Timeout] Latar=" + (backdropImage == null ? "POLOS" : "BLUR") + " (AUTO)");
            }
        };
        backdropWorker.execute();
    }

    private void applyBackdrop(BufferedImage image) {
        backdropImage = image;
        if (lockDialog instanceof SessionLockDialog) {
            ((SessionLockDialog) lockDialog).setBackdrop(image);
        }
        for (ShieldState state : shields.values()) state.shield.repaint();
    }

    private void clearBackdrop() {
        if (backdropWorker != null) {
            backdropWorker.cancel(true);
            backdropWorker = null;
        }
        BufferedImage previous = backdropImage;
        applyBackdrop(null);
        if (previous != null) previous.flush();
    }

    /** Dialog terpisah; DlgLogin beserta semua komponennya tetap di tempatnya. */
    void showLockedLogin(JDialog dialog) {
        if (!locked || lockDialog != null) {
            return;
        }
        lockDialog = dialog;
        if (dialog instanceof SessionLockDialog) {
            ((SessionLockDialog) dialog).setBackdrop(backdropImage);
        }
        initialLogin.setVisible(false);
        updateSurfaceBounds();
        SwingUtilities.invokeLater(this::restoreLoginFocus);
        dialog.setVisible(true);
    }

    // Nama API dipertahankan untuk jalur resize/fokus frmUtama versi sebelumnya.
    void updateSurfaceBounds() {
        if (lockDialog != null && initialLogin.getOwner().isShowing()) {
            lockDialog.setLocationRelativeTo(initialLogin.getOwner());
        }
    }

    private boolean loginWindow(Window window) {
        for (Window current = window; current != null; current = current.getOwner()) {
            if (current == lockDialog) {
                return true;
            }
        }
        return false;
    }

    private Window sourceWindow(AWTEvent event) {
        Object source = event.getSource();
        if (source instanceof Window) {
            return (Window) source;
        }
        return source instanceof Component
                ? SwingUtilities.getWindowAncestor((Component) source) : null;
    }

    void restoreLoginFocus() {
        if (lockDialog == null || !lockDialog.isVisible()) {
            return;
        }
        Window focused = java.awt.KeyboardFocusManager
                .getCurrentKeyboardFocusManager().getFocusedWindow();
        if (focused != null && loginWindow(focused)) {
            return;
        }
        lockDialog.toFront();
        lockDialog.requestFocus();
        Component focus = lockDialog.getMostRecentFocusOwner();
        if (focus != null) {
            focus.requestFocusInWindow();
        }
    }

    private void shield(Window window) {
        if (window == null || !window.isShowing() || loginWindow(window)
                || !(window instanceof RootPaneContainer)) {
            return;
        }
        JRootPane root = ((RootPaneContainer) window).getRootPane();
        if (shields.containsKey(root)) {
            return;
        }
        Component previousGlass = root.getGlassPane();
        SolidShield shield = new SolidShield();
        shields.put(root, new ShieldState(previousGlass,
                previousGlass.isVisible(), shield));
        root.setGlassPane(shield);
        shield.setVisible(true);
        // Jika glass pane lama sudah visible, JRootPane tidak selalu segera
        // melayout penggantinya. Ukuran eksplisit menutup form sebelum modal
        // tampil, termasuk dialog tanpa owner/modal-excluded.
        root.revalidate();
        root.doLayout();
        shield.setBounds(0, 0, root.getWidth(), root.getHeight());
        shield.paintImmediately(0, 0, shield.getWidth(), shield.getHeight());
        root.repaint();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        stop();
        release();
        previousFocus = null;
        closed = true;
        inputQueue.remove();
    }

    /** Guard berlaku juga untuk form tanpa owner/modal-excluded dan pintasan. */
    private final class InputQueue extends EventQueue {
        @Override
        protected void dispatchEvent(AWTEvent event) {
            Window window = sourceWindow(event);
            if (!closed && locked && window != null && !loginWindow(window)
                    && event.getID() == WindowEvent.WINDOW_CLOSING) {
                restoreLoginFocus();
                return;
            }
            if (!closed && event instanceof InputEvent && window != null) {
                if (!locked && expired()) {
                    checkTimeout();
                    // Jangan teruskan input kedaluwarsa setelah modal loop selesai.
                    ((InputEvent) event).consume();
                    return;
                }
                if (locked && !loginWindow(window)) {
                    ((InputEvent) event).consume();
                    if (!(event instanceof MouseEvent)
                            || event.getID() == MouseEvent.MOUSE_PRESSED) {
                        restoreLoginFocus();
                    }
                    return;
                }
                if (active && !locked) {
                    lastActivityNanos = System.nanoTime();
                }
            }
            if (!closed && locked && (event instanceof WindowEvent
                    || event.getID() == ComponentEvent.COMPONENT_SHOWN)) {
                shield(window);
            }
            super.dispatchEvent(event);
            if (!closed && locked && window == initialLogin.getOwner()
                    && (event.getID() == ComponentEvent.COMPONENT_MOVED
                        || event.getID() == ComponentEvent.COMPONENT_RESIZED
                        || event.getID() == WindowEvent.WINDOW_DEICONIFIED)) {
                updateSurfaceBounds();
            }
            if (!closed && locked && window != null && !loginWindow(window)
                    && (event.getID() == WindowEvent.WINDOW_GAINED_FOCUS
                        || event.getID() == WindowEvent.WINDOW_ACTIVATED)) {
                restoreLoginFocus();
            }
        }

        void remove() {
            pop();
        }
    }

    private static final class ShieldState {
        final Component previousGlass;
        final boolean previousVisible;
        final SolidShield shield;

        ShieldState(Component previousGlass, boolean previousVisible, SolidShield shield) {
            this.previousGlass = previousGlass;
            this.previousVisible = previousVisible;
            this.shield = shield;
        }
    }

    /** Penutup opaque; wallpaper blur opsional dibagi tanpa snapshot data pasien. */
    private final class SolidShield extends JComponent {
        SolidShield() {
            setOpaque(true);
            setBackground(LOCK_BACKGROUND);
            setFocusable(true);
            addMouseListener(new MouseAdapter() { });
            addMouseMotionListener(new MouseAdapter() { });
            addMouseWheelListener(evt -> evt.consume());
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            BufferedImage painted = backdropImage;
            if (!SessionLockBackdrop.paint(g, this, painted, initialLogin.getOwner())) {
                // Jangan ubah koleksi/state dari paint; fallback pada giliran EDT berikut.
                SwingUtilities.invokeLater(() -> {
                    if (backdropImage == painted) clearBackdrop();
                });
            }
        }
    }
}
