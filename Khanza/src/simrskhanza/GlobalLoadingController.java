package simrskhanza;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Controller loading global untuk SIMRS Khanza.
 *
 * Dipasang satu kali dari frmUtama:
 *     GlobalLoadingController.install(this);
 *
 * Controller memantau aksi pembuka modul secara global, menampilkan DlgLoading
 * sebelum handler tombol/menu dijalankan, lalu menutupnya otomatis ketika
 * Window tujuan tampil. Tidak mengubah handler, query, validasi, ataupun
 * fungsi bisnis pada form-form yang sudah ada.
 *
 * Catatan:
 * Persentase pada mode global adalah progress visual/estimasi karena ratusan
 * form lama tidak melaporkan progress internalnya satu per satu. Pada V3,
 * progress dirender dan dihitung di thread khusus agar tetap bergerak saat
 * Swing EDT sedang sibuk membuka form berat.
 */
public final class GlobalLoadingController {

    private static final Object LOCK = new Object();
    private static final int START_PROGRESS = 0;
    private static final int MAX_AUTO_PROGRESS = 94;
    private static final int PROGRESS_INTERVAL_MS = 80;
    private static final int NO_WINDOW_GRACE_MS = 550;
    private static final int SAFETY_TIMEOUT_MS = 45000;

    private static volatile boolean installed = false;
    private static WeakReference<Window> rootRef = new WeakReference<Window>(null);
    private static AWTEventListener globalListener;

    private static long sequence = 0L;
    private static Session activeSession;
    private static ScheduledFuture<?> progressFuture;
    private static Timer graceTimer;
    private static Timer safetyTimer;

    private static final ScheduledExecutorService PROGRESS_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "RSAJ-GlobalLoading-Progress");
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY + 1);
                    return thread;
                }
            });

    private GlobalLoadingController() {
        // Utility class.
    }

    /**
     * Memasang listener global. Aman dipanggil lebih dari sekali; listener
     * hanya benar-benar dipasang satu kali.
     */
    public static void install(Window rootWindow) {
        if (rootWindow == null) {
            throw new IllegalArgumentException("rootWindow tidak boleh null");
        }

        synchronized (LOCK) {
            rootRef = new WeakReference<Window>(rootWindow);
            if (installed) {
                return;
            }

            globalListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    try {
                        handleEvent(event);
                    } catch (Throwable ex) {
                        // Controller global tidak boleh mengganggu event aplikasi.
                        System.out.println("[GLOBAL LOADING] " + ex.getMessage());
                    }
                }
            };

            long mask = AWTEvent.MOUSE_EVENT_MASK
                    | AWTEvent.KEY_EVENT_MASK
                    | AWTEvent.ACTION_EVENT_MASK
                    | AWTEvent.WINDOW_EVENT_MASK
                    | AWTEvent.COMPONENT_EVENT_MASK;

            Toolkit.getDefaultToolkit().addAWTEventListener(globalListener, mask);
            installed = true;
            System.out.println("[GLOBAL LOADING] Controller aktif (V3 - progress independen dari EDT)");
        }
    }

    /**
     * Opsional untuk shutdown/testing. Biasanya tidak perlu dipanggil.
     */
    public static void uninstall() {
        synchronized (LOCK) {
            stopAllTimers();
            activeSession = null;
            LoadingManager.hide();

            if (installed && globalListener != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
            }
            globalListener = null;
            installed = false;
            rootRef = new WeakReference<Window>(null);
        }
    }

    public static boolean isInstalled() {
        return installed;
    }

    private static void handleEvent(AWTEvent event) {
        if (!installed || event == null) {
            return;
        }

        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
            return;
        }

        if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
            return;
        }

        // Beberapa komponen AWT native benar-benar mendispatch ActionEvent.
        // Swing JButton biasanya sudah ter-cover oleh MOUSE_RELEASED/keyboard.
        if (event instanceof ActionEvent) {
            handleActionEvent((ActionEvent) event);
            return;
        }

        if (event instanceof WindowEvent) {
            handleWindowEvent((WindowEvent) event);
            return;
        }

        if (event instanceof ComponentEvent) {
            handleComponentEvent((ComponentEvent) event);
        }
    }

    private static void handleMouseEvent(MouseEvent event) {
        if (event.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        // PENTING: mulai sejak MOUSE_PRESSED, bukan MOUSE_RELEASED.
        // Dengan begitu loading punya satu siklus event penuh untuk benar-benar
        // tergambar sebelum release memicu ActionListener/constructor form berat.
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            Component trigger = resolveLauncher(event.getComponent());
            if (!isLikelyFormLauncher(trigger)) {
                return;
            }
            beginLoading(trigger);
            return;
        }

        // Setelah release selesai diproses, baru cek apakah benar ada Window baru.
        // Callback invokeLater di scheduleNoWindowCheck akan menunggu handler lama
        // selesai tanpa memindahkan source Khanza ke thread lain.
        if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            long token = getActiveToken();
            if (token >= 0L) {
                scheduleNoWindowCheck(token);
            }
        }
    }

    private static void handleKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode != KeyEvent.VK_ENTER && keyCode != KeyEvent.VK_SPACE) {
            return;
        }

        if (event.getID() == KeyEvent.KEY_PRESSED) {
            Component trigger = resolveLauncher(event.getComponent());
            if (!isLikelyFormLauncher(trigger)) {
                return;
            }
            beginLoading(trigger);
            return;
        }

        if (event.getID() == KeyEvent.KEY_RELEASED) {
            long token = getActiveToken();
            if (token >= 0L) {
                scheduleNoWindowCheck(token);
            }
        }
    }

    private static void handleActionEvent(ActionEvent event) {
        Object sourceObject = event.getSource();
        if (!(sourceObject instanceof Component)) {
            return;
        }

        Component source = (Component) sourceObject;
        Component trigger = resolveLauncher(source);
        if (!isLikelyFormLauncher(trigger)) {
            return;
        }

        // Fallback untuk aksi programatik/keyboard yang tidak melewati mouse.
        long token;
        synchronized (LOCK) {
            token = activeSession != null ? activeSession.token : -1L;
        }
        if (token < 0L) {
            token = beginLoading(trigger);
        }
        scheduleNoWindowCheck(token);
    }

    private static void handleWindowEvent(WindowEvent event) {
        int id = event.getID();
        if (id != WindowEvent.WINDOW_OPENED && id != WindowEvent.WINDOW_ACTIVATED) {
            return;
        }
        completeIfTargetWindow(event.getWindow());
    }

    private static void handleComponentEvent(ComponentEvent event) {
        if (event.getID() != ComponentEvent.COMPONENT_SHOWN) {
            return;
        }

        Object source = event.getSource();
        if (source instanceof Window) {
            completeIfTargetWindow((Window) source);
            return;
        }

        // Beberapa modul dapat memakai JInternalFrame di dalam desktop utama.
        if (source instanceof javax.swing.JInternalFrame) {
            long token = getActiveToken();
            if (token >= 0L && ((javax.swing.JInternalFrame) source).isShowing()) {
                complete(token);
            }
        }
    }

    private static long getActiveToken() {
        synchronized (LOCK) {
            return activeSession != null ? activeSession.token : -1L;
        }
    }

    private static long beginLoading(Component trigger) {
        final Window root = rootRef.get();
        if (root == null || trigger == null) {
            return -1L;
        }

        final Session session;
        synchronized (LOCK) {
            // Satu proses pembukaan form pada satu waktu. Klik tambahan saat
            // loading aktif tidak membuat dialog loading bertumpuk.
            if (activeSession != null) {
                return activeSession.token;
            }

            sequence++;
            session = new Session(
                    sequence,
                    trigger,
                    collectVisibleWindows(),
                    buildLoadingMessage(trigger)
            );
            activeSession = session;
        }

        LoadingManager.show(root, session.message, START_PROGRESS);
        System.out.println("[GLOBAL LOADING] Mulai -> " + session.message
                + " | " + describeTrigger(trigger));
        startProgressAnimation(session.token);
        startSafetyTimeout(session.token);
        return session.token;
    }

    private static void completeIfTargetWindow(Window candidate) {
        final Session session;
        synchronized (LOCK) {
            session = activeSession;
            if (session == null || !isTargetWindow(candidate, session)) {
                return;
            }
            session.targetDetected = true;
        }
        complete(session.token);
    }

    private static boolean isTargetWindow(Window candidate, Session session) {
        if (candidate == null || session == null) {
            return false;
        }

        Window root = rootRef.get();
        if (candidate == root || candidate instanceof DlgLoading) {
            return false;
        }

        // Popup/toast ringan tidak dianggap sebagai form tujuan.
        if (!(candidate instanceof Dialog) && !(candidate instanceof Frame)) {
            return false;
        }

        String className = candidate.getClass().getName().toLowerCase(Locale.ENGLISH);
        if (className.contains("toast")
                || className.contains("popup")
                || className.contains("tooltip")
                || className.contains("splash")) {
            return false;
        }

        // Window yang sudah terlihat sebelum klik bukan form baru yang sedang dibuka.
        if (session.visibleBefore.contains(candidate)) {
            return false;
        }

        return candidate.isShowing() || candidate.isVisible();
    }

    private static void scheduleNoWindowCheck(final long token) {
        if (token < 0L) {
            return;
        }

        // Diposting setelah event tombol selesai diproses. Jika handler sinkron
        // sedang membuat form berat, callback ini otomatis menunggu handler
        // selesai tanpa memindahkan source lama ke background thread.
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (LOCK) {
                    if (!isActiveToken(token)) {
                        return;
                    }
                    stopTimer(graceTimer);
                    graceTimer = new Timer(NO_WINDOW_GRACE_MS, e -> {
                        synchronized (LOCK) {
                            if (!isActiveToken(token)) {
                                return;
                            }
                            if (activeSession != null && !activeSession.targetDetected) {
                                cancel(token);
                            }
                        }
                    });
                    graceTimer.setRepeats(false);
                    graceTimer.start();
                }
            }
        });
    }

    private static void startProgressAnimation(final long token) {
        synchronized (LOCK) {
            stopProgressFuture();

            final long startedAt = System.nanoTime();
            progressFuture = PROGRESS_EXECUTOR.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    String message;
                    synchronized (LOCK) {
                        if (!isActiveToken(token)) {
                            stopProgressFuture();
                            return;
                        }
                        message = activeSession != null
                                ? activeSession.message
                                : "Memuat data...";
                    }

                    // Kurva progress estimasi yang terasa natural:
                    // cepat di awal, lalu melambat mendekati 94% sambil menunggu
                    // Window tujuan benar-benar tampil. Berjalan pada scheduler
                    // daemon sendiri sehingga tidak ikut beku ketika EDT sibuk.
                    double elapsedMs = (System.nanoTime() - startedAt) / 1_000_000.0;
                    int computed = (int) Math.round(
                            MAX_AUTO_PROGRESS * (1.0 - Math.exp(-elapsedMs / 3500.0)));
                    computed = Math.max(START_PROGRESS,
                            Math.min(MAX_AUTO_PROGRESS, computed));

                    int current = LoadingManager.getProgress();
                    int next = Math.max(current, computed);
                    LoadingManager.update(next, message);
                }
            }, 0L, PROGRESS_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    private static void startSafetyTimeout(final long token) {
        synchronized (LOCK) {
            stopTimer(safetyTimer);
            safetyTimer = new Timer(SAFETY_TIMEOUT_MS, e -> cancel(token));
            safetyTimer.setRepeats(false);
            safetyTimer.start();
        }
    }

    private static void complete(long token) {
        synchronized (LOCK) {
            if (!isActiveToken(token)) {
                return;
            }
            stopAllTimers();
            activeSession = null;
        }
        System.out.println("[GLOBAL LOADING] Selesai -> form tujuan terdeteksi");
        LoadingManager.update(100, "Menampilkan form...");
        LoadingManager.hide(true);
    }

    private static void cancel(long token) {
        synchronized (LOCK) {
            if (!isActiveToken(token)) {
                return;
            }
            stopAllTimers();
            activeSession = null;
        }
        System.out.println("[GLOBAL LOADING] Batal -> tidak ada form baru yang terdeteksi");
        LoadingManager.hide();
    }

    private static boolean isActiveToken(long token) {
        return activeSession != null && activeSession.token == token;
    }

    private static boolean isLikelyFormLauncher(Component source) {
        if (source == null || isInsideLoading(source) || !isApplicationWindow(source)) {
            return false;
        }

        // JMenu induk hanya membuka dropdown, bukan form.
        if (source instanceof JMenu) {
            return false;
        }

        String className = source.getClass().getName();
        String identifier = buildIdentifier(source).toLowerCase(Locale.ENGLISH);

        if (isExcludedAction(identifier)) {
            return false;
        }

        if (source instanceof JMenuItem) {
            return true;
        }

        if (source instanceof AbstractButton) {
            // ButtonBig di frmUtama/DlgHome pada umumnya adalah launcher modul.
            if (className.endsWith("ButtonBig") || className.equals("widget.ButtonBig")) {
                return true;
            }

            // Tombol biasa tetap didukung agar dialog pencarian/detail dari form lain
            // ikut mendapat loading, selama bukan tombol aksi CRUD yang dikecualikan.
            return hasUsableIdentity(identifier);
        }

        // Fallback untuk widget custom Khanza yang berperilaku seperti tombol
        // tetapi tidak mewarisi AbstractButton secara langsung.
        String simple = source.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
        if (simple.contains("button") || simple.contains("menuitem")) {
            return hasUsableIdentity(identifier);
        }

        return false;
    }

    /**
     * Event pada widget custom kadang jatuh ke komponen anak (label/icon/panel).
     * Naikkan sumber event sampai menemukan komponen tombol/menu yang sebenarnya.
     */
    private static Component resolveLauncher(Component source) {
        Component current = source;
        int depth = 0;
        while (current != null && depth < 10) {
            if (current instanceof JMenuItem || current instanceof AbstractButton) {
                return current;
            }

            String simple = current.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
            if (simple.contains("buttonbig") || simple.contains("button")) {
                return current;
            }

            current = current.getParent();
            depth++;
        }
        return source;
    }

    private static String buildIdentifier(Component source) {
        if (source == null) {
            return "";
        }

        StringBuilder id = new StringBuilder();
        if (source instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) source;
            id.append(safe(button.getText())).append(' ')
                    .append(safe(button.getName())).append(' ')
                    .append(safe(button.getActionCommand()));
        } else {
            id.append(safe(source.getName())).append(' ')
                    .append(source.getClass().getSimpleName());
            // Beberapa widget custom tetap menyediakan getText(), namun bukan
            // AbstractButton. Ambil secara reflektif tanpa dependency tambahan.
            try {
                java.lang.reflect.Method m = source.getClass().getMethod("getText");
                Object value = m.invoke(source);
                if (value != null) {
                    id.append(' ').append(value.toString());
                }
            } catch (Throwable ignored) {
                // Tidak semua komponen mempunyai getText().
            }
        }
        return id.toString().trim();
    }

    private static String describeTrigger(Component trigger) {
        if (trigger == null) {
            return "trigger=null";
        }
        return "class=" + trigger.getClass().getSimpleName()
                + ", name=" + safe(trigger.getName())
                + ", id=" + buildIdentifier(trigger);
    }

    private static boolean isApplicationWindow(Component source) {
        Window sourceWindow = SwingUtilities.getWindowAncestor(source);
        if (sourceWindow == null || sourceWindow instanceof DlgLoading) {
            return false;
        }

        Window root = rootRef.get();
        if (root == null) {
            return false;
        }

        if (sourceWindow == root) {
            return true;
        }

        // Semua dialog/frame yang masih berada dalam keluarga owner frmUtama.
        Window current = sourceWindow;
        while (current != null) {
            if (current == root) {
                return true;
            }
            current = current.getOwner();
        }

        // Beberapa form Khanza dibuat dengan owner null. Bila frmUtama sedang
        // aktif dan window sumber adalah Dialog/Frame aplikasi, tetap izinkan.
        return sourceWindow instanceof Dialog || sourceWindow instanceof Frame;
    }

    private static boolean isInsideLoading(Component source) {
        Window window = SwingUtilities.getWindowAncestor(source);
        return window instanceof DlgLoading;
    }

    private static boolean isExcludedAction(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return true;
        }

        // Aksi yang lazimnya memproses data di form saat ini, bukan membuka form.
        String[] excluded = new String[]{
            "simpan", "save", "hapus", "delete", "batal", "cancel",
            "keluar", "tutup", "close", "login", "logout", "ubah", "edit",
            "ganti", "update", "refresh", "segarkan", "proses", "kirim",
            "upload", "download", "cetak", "print", "panggil", "ambil",
            "setuju", "tolak", "ya", "tidak", "ok", "btnmenu", "btncancel",
            "btnlogin"
        };

        for (String word : excluded) {
            if (containsWord(identifier, word)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String text, String word) {
        if (text == null || word == null || word.isEmpty()) {
            return false;
        }

        // Kata sangat pendek seperti "ok"/"ya" tidak boleh memakai contains(),
        // karena bisa salah memblokir nama seperti "Dokter" atau "Bayar".
        if (word.length() <= 2) {
            String normalized = " " + text.replaceAll("[^a-z0-9]+", " ").trim() + " ";
            return normalized.contains(" " + word + " ")
                    || text.contains("btn" + word)
                    || text.contains("mn" + word);
        }
        return text.contains(word);
    }

    private static boolean hasUsableIdentity(String identifier) {
        if (identifier == null) {
            return false;
        }
        String cleaned = identifier.replaceAll("[^a-z0-9]", "");
        return cleaned.length() >= 3;
    }

    private static String buildLoadingMessage(Component trigger) {
        String label = "";

        if (trigger instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) trigger;
            label = safe(button.getText());
            if (label.trim().isEmpty()) {
                label = safe(button.getActionCommand());
            }
            if (label.trim().isEmpty()) {
                label = safe(button.getName());
            }
        } else {
            label = buildIdentifier(trigger);
        }

        label = cleanLabel(label);
        if (label.isEmpty() || looksLikeTechnicalName(label)) {
            return "Memuat modul...";
        }

        if (label.length() > 36) {
            label = label.substring(0, 33).trim() + "...";
            return "Memuat " + label;
        }
        return "Memuat " + label + "...";
    }

    private static String cleanLabel(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("(?i)<br\\s*/?>", " ")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean looksLikeTechnicalName(String label) {
        String lower = label.toLowerCase(Locale.ENGLISH);
        return lower.startsWith("btn")
                || lower.startsWith("mn")
                || lower.startsWith("jbutton")
                || lower.matches("[a-z]+\\d+");
    }

    private static Set<Window> collectVisibleWindows() {
        Set<Window> windows = Collections.newSetFromMap(
                new IdentityHashMap<Window, Boolean>());
        for (Window window : Window.getWindows()) {
            if (window != null && window.isShowing()) {
                windows.add(window);
            }
        }
        return windows;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void stopAllTimers() {
        stopProgressFuture();
        stopTimer(graceTimer);
        stopTimer(safetyTimer);
        graceTimer = null;
        safetyTimer = null;
    }

    private static void stopProgressFuture() {
        ScheduledFuture<?> future = progressFuture;
        progressFuture = null;
        if (future != null) {
            future.cancel(false);
        }
    }

    private static void stopTimer(Timer timer) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    private static final class Session {
        private final long token;
        private final Component trigger;
        private final Set<Window> visibleBefore;
        private final String message;
        private boolean targetDetected;

        private Session(long token, Component trigger,
                Set<Window> visibleBefore, String message) {
            this.token = token;
            this.trigger = trigger;
            this.visibleBefore = visibleBefore;
            this.message = message;
            this.targetDetected = false;
        }
    }
}
