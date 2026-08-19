package simrskhanza;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import javax.sound.sampled.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;


/**
 * Toast dengan:
 * - Animasi SLIDE-IN dari kanan + FADE-IN, dan FADE-OUT saat hilang
 * - Progress bar durasi di bawah
 * - Tombol close (×) kanan-atas
 * - Icon di kiri (default)
 * - Stack manager: urutan dinamis (yang muncul duluan di atas), relayout saat ada yang hilang
 * - Jeda 1 detik antar toast
 * - Anchor ke kanan-atas LAYAR (bukan jendela), offset 5mm dari kanan & 90px dari atas
 *
 * API yang dipakai frmUtama:
 *   BaseToast.showInfo(owner, message, durationMs, onClick, soundPathOrResource);
 *   BaseToast.showSuccess(owner, message, durationMs, onClick, soundPathOrResource);
 */
public class BaseToast extends JWindow {

    // ====== Konfigurasi visual ======
    private static final int WIDTH_PX   = 360;
    private static final int MIN_HEIGHT = 64;
    private static final int PADDING    = 12;
    private static final int ARC        = 16;
    private static final int SPACING    = 8;       // jarak antar toast secara vertikal
    private static final int FADE_TICK  = 16;      // ~60fps
    private static final float OP_STEP  = 0.08f;   // delta opacity per tick
    private static final int SLIDE_PX   = 28;      // start dari kanan sejauh ini lalu geser ke kiri (masuk)
    private static final int RELOC_STEP = 12;      // px per tick saat naik turun reposisi
    private static final Font FONT      = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color FG       = new Color(245,245,245);

    // Tema
    private static final Color BLUE  = new Color(33,150,243);
    private static final Color GREEN = new Color(76,175,80);
    // Tambahan tema merah untuk notifikasi penting
    private static final Color RED = new Color(211, 47, 47);


    // Offset global: kanan dalam mm, atas dalam px (sesuai permintaan)
    private static int RIGHT_OFFSET_PX = 2;
    private static int    TOP_OFFSET_PX   = 100;

    // Jeda antar toast
    private static final int STAGGER_MS = 2000;

    // ====== Komponen ======
    private final JLabel iconLabel  = new JLabel();
    private final JLabel textLabel  = new JLabel();
    private final JLabel closeLabel = new JLabel("\u00D7"); // ×
    private final JComponent progressBar = new JComponent() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = (int) Math.round(getWidth() * progressRatio);
            g2.setColor(progressColor);
            g2.fillRoundRect(0, 0, Math.max(0,w), getHeight(), 6, 6);
            g2.dispose();
        }
    };

    // ====== State ======
    private Color bgColor;
    private Color progressColor = new Color(255,255,255,160);
    private Runnable onClick;
    private int durationMs;
    private long startAt;   // waktu mulai tampil (untuk progress)
    private double progressRatio = 1.0;

    // target lokasi (untuk slide & relayout)
    private int targetX, targetY;

    // Icon default (boleh diganti via setter)
    private static Icon DEFAULT_INFO_ICON    = UIManager.getIcon("OptionPane.informationIcon");
    private static Icon DEFAULT_SUCCESS_ICON = UIManager.getIcon("OptionPane.informationIcon");
    
    public static void showInfoNoIcon(Component owner, String message, int durationMs, Runnable onClick, String sound) {
    show(owner, message, durationMs, onClick, sound, BLUE, null);
    }
    public static void showSuccessNoIcon(Component owner, String message, int durationMs, Runnable onClick, String sound) {
        show(owner, message, durationMs, onClick, sound, GREEN, null);
    }
    
    // Notifikasi tanpa ikon dengan tema merah (danger/error)
public static void showDangerNoIcon(Component owner, String message, int durationMs, Runnable onClick, String soundPathOrResource) {
    show(owner, message, durationMs, onClick, soundPathOrResource, RED, null);
}


    // ====== Manager stack ======
    private static final ToastManager MANAGER = new ToastManager();

    // ====== Ctor private: pakai lewat showInfo/showSuccess ======
    private BaseToast(Window owner, Color bg, String htmlMessage, int durationMs,
                      Runnable onClick, Icon icon) {
        super(owner);
        this.bgColor   = withAlpha(bg, 230);
        this.onClick   = onClick;
        this.durationMs= Math.max(1200, durationMs); // minimal 1.2s biar kelihatan
        setAlwaysOnTop(true);
        setBackground(new Color(0,0,0,0));

        JPanel content = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        content.setOpaque(false);
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Panel konten (icon + teks + close)
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        iconLabel.setOpaque(false);
        iconLabel.setIcon(icon);

        textLabel.setForeground(FG);
        textLabel.setFont(FONT);
        textLabel.setText(wrapHtml(htmlMessage));

        closeLabel.setForeground(new Color(255,255,255,200));
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        closeLabel.setToolTipText("Tutup");

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(closeLabel, BorderLayout.NORTH);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        // progress (tipis di bawah)
        progressBar.setPreferredSize(new Dimension(10, 3));
        progressBar.setOpaque(false);

        content.add(row, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);
        setContentPane(content);

        // Klik pada konten (bukan tombol close) → onClick
        MouseAdapter openAd = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getSource() == closeLabel) return; // biar close tidak ikut panggil onClick
                if (onClick != null) {
                    try { onClick.run(); } catch (Throwable ignored) {}
                }
                hideAndDispose();
            }
        };
        content.addMouseListener(openAd);
        row.addMouseListener(openAd);
        textLabel.addMouseListener(openAd);
        iconLabel.addMouseListener(openAd);

        // Close
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { hideAndDispose(); }
        });
    }

    // ========= PUBLIC API =========

    /** Info (biru). */
    public static void showInfo(Component owner, String message, int durationMs, Runnable onClick, String soundPathOrResource) {
        show(owner, message, durationMs, onClick, soundPathOrResource, BLUE, DEFAULT_INFO_ICON);
    }

    /** Success (hijau). */
    public static void showSuccess(Component owner, String message, int durationMs, Runnable onClick, String soundPathOrResource) {
        show(owner, message, durationMs, onClick, soundPathOrResource, GREEN, DEFAULT_SUCCESS_ICON);
    }

    /** Set offset global (mm ke kanan, px dari atas). */
    public static void setGlobalOffsetsMmPx(int rightMm, int topPx) {
        RIGHT_OFFSET_PX = Math.max(0, rightMm);
        TOP_OFFSET_PX   = Math.max(0, topPx);
    }

    /** (Opsional) Ganti icon default. Path boleh resource di classpath atau file. */
    public static void setDefaultIcons(Icon info, Icon success) {
        if (info != null) DEFAULT_INFO_ICON = info;
        if (success != null) DEFAULT_SUCCESS_ICON = success;
        
    }

    // ========= IMPLEMENTASI =========

    private static void show(Component owner, String message, int durationMs, Runnable onClick,
                         String soundPathOrResource, Color theme, Icon icon) {
    SwingUtilities.invokeLater(() -> {
        Window w = owner != null ? SwingUtilities.getWindowAncestor(owner) : null;
        BaseToast toast = new BaseToast(w, theme, message, durationMs, onClick, icon);
        toast.prepareSize();
        // ⬇️ TRIM biar "notifikasi.wav " tidak bikin gagal load
        String sound = (soundPathOrResource == null) ? null : soundPathOrResource.trim();
        MANAGER.enqueue(toast, sound);
    });
}

    private void prepareSize() {
        int contentW = WIDTH_PX - (PADDING * 2);
        textLabel.setPreferredSize(new Dimension(contentW, textLabel.getPreferredSize().height));
        pack();
        int h = Math.max(MIN_HEIGHT, getPreferredSize().height);
        setSize(WIDTH_PX, h);
    }

    private static final boolean DEBUG_SOUND = false;

private void playSoundIfAny(String pathOrResource) {
    if (pathOrResource == null || pathOrResource.isEmpty()) return;

    new Thread(() -> {
        try {
            AudioInputStream ais = null;

            // 1) Prefix khusus
            if (pathOrResource.startsWith("classpath:")) {
                String p = pathOrResource.substring("classpath:".length());
                InputStream in = getResourceStreamFlexible(p);
                if (in != null) ais = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            } else if (pathOrResource.startsWith("file:")) {
                File f = new File(pathOrResource.substring("file:".length()));
                if (f.exists()) ais = AudioSystem.getAudioInputStream(f);
            }

            // 2) Classpath (tanpa prefix)
            if (ais == null) {
                InputStream in = getResourceStreamFlexible(pathOrResource);
                if (in != null) ais = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            }

            // 3) File path relatif/absolute
            if (ais == null) {
                File f = new File(pathOrResource);
                if (!f.isAbsolute()) {
                    // coba user.dir
                    File f2 = new File(System.getProperty("user.dir"), pathOrResource);
                    if (f2.exists()) f = f2;
                    // coba directory JAR (kalau jalan dari JAR)
                    if (!f.exists()) {
                        try {
                            java.net.URL url = BaseToast.class.getProtectionDomain().getCodeSource().getLocation();
                            File jarDir = new File(url.toURI()).getParentFile();
                            File f3 = new File(jarDir, pathOrResource);
                            if (f3.exists()) f = f3;
                        } catch (Throwable ignored) {}
                    }
                }
                if (f.exists()) ais = AudioSystem.getAudioInputStream(f);
            }

            if (ais == null) {
                if (DEBUG_SOUND) System.out.println("[Toast] Sound NOT FOUND: " + pathOrResource);
                return;
            }

            final Clip clip = AudioSystem.getClip();
            clip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP || ev.getType() == LineEvent.Type.CLOSE) {
                    try { clip.close(); } catch (Exception ignored) {}
                }
            });
            clip.open(ais);
            clip.setFramePosition(0);
            clip.start();

            if (DEBUG_SOUND) System.out.println("[Toast] Sound PLAY: " + pathOrResource);
        } catch (Throwable t) {
            if (DEBUG_SOUND) t.printStackTrace();
        }
    }, "ToastSoundPlayer").start();
}

/** Coba resource di classpath dengan beberapa variasi path. */
private InputStream getResourceStreamFlexible(String p) {
    if (p == null || p.isEmpty()) return null;
    // coba apa adanya
    InputStream in = BaseToast.class.getResourceAsStream(p);
    if (in != null) return in;
    // coba dengan leading slash
    in = BaseToast.class.getResourceAsStream(p.startsWith("/") ? p : "/" + p);
    if (in != null) return in;
    // coba lewat ClassLoader
    in = Thread.currentThread().getContextClassLoader().getResourceAsStream(p.startsWith("/") ? p.substring(1) : p);
    return in;
}


    // ========== Animasi & Layout ==========
    private void computeAnchorAndSetBounds(int slotIndex) {
        GraphicsConfiguration gc = (getOwner() != null && getOwner().isShowing())
                ? getOwner().getGraphicsConfiguration()
                : GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        Rectangle screen = gc.getBounds();
        Insets insets    = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int w = getWidth();
        int h = getHeight();

        int rightPx = mmToPxOnGc(RIGHT_OFFSET_PX, gc);
        int x = screen.x + screen.width  - insets.right - w - rightPx;
        int y = screen.y + insets.top + TOP_OFFSET_PX
                + slotIndex * (h + SPACING);

        // target untuk animasi
        targetX = x;
        targetY = y;

        // mulai dari kanan sedikit (SLIDE_PX) untuk efek masuk
        setBounds(x + SLIDE_PX, y, w, h);
    }

    private void animateInThenLifeThenOut(String soundPath) {
        // Fade + slide in
        setOpacitySafe(0f);
        Timer fadeIn = new Timer(FADE_TICK, null);
        fadeIn.addActionListener(e -> {
            Point p = getLocation();
            int nextX = Math.max(targetX, p.x - 8); // geser kiri menuju target
            setLocation(nextX, targetY);

            float cur = getOpacitySafe();
            float nextOp = Math.min(1f, cur + OP_STEP);
            setOpacitySafe(nextOp);

            if (nextX <= targetX && nextOp >= 1f) {
                ((Timer)e.getSource()).stop();
                startLifeThenFadeOut();
            }
        });
        fadeIn.start();

        // Suara
        playSoundIfAny(soundPath);
    }

    private void startLifeThenFadeOut() {
        startAt = System.currentTimeMillis();
        int tick = 40; // refresh progress 25fps
        Timer life = new Timer(tick, null);
        life.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startAt;
            progressRatio = Math.max(0.0, 1.0 - (elapsed / (double) durationMs));
            progressBar.repaint();
            if (elapsed >= durationMs) {
                ((Timer)e.getSource()).stop();
                fadeOutAndRemove();
            }
        });
        life.start();
    }

    private void fadeOutAndRemove() {
        Timer out = new Timer(FADE_TICK, null);
        out.addActionListener(e -> {
            float cur = getOpacitySafe();
            float next = cur - OP_STEP;
            setOpacitySafe(Math.max(0f, next));
            if (next <= 0f) {
                ((Timer)e.getSource()).stop();
                MANAGER.remove(this);
                hideAndDisposeBare();
            }
        });
        out.start();
    }

    private void hideAndDispose() {
        // Klik close → langsung fade out
        fadeOutAndRemove();
    }
    private void hideAndDisposeBare() {
        try { setVisible(false); dispose(); } catch (Throwable ignored) {}
    }

    // Dipanggil Manager saat perlu reposisi (misal toast di atas hilang → yang bawah naik)
    private void retargetToSlot(int slotIndex) {
        GraphicsConfiguration gc = (getOwner() != null && getOwner().isShowing())
                ? getOwner().getGraphicsConfiguration()
                : GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        Rectangle screen = gc.getBounds();
        Insets insets    = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int w = getWidth();
        int h = getHeight();

        int rightPx = mmToPxOnGc(RIGHT_OFFSET_PX, gc);
        targetX = screen.x + screen.width  - insets.right - w - rightPx;
        int newY = screen.y + insets.top + TOP_OFFSET_PX
                + slotIndex * (h + SPACING);

        // animasi vertikal halus menuju newY
        Timer slide = new Timer(FADE_TICK, null);
        slide.addActionListener(e -> {
            Point p = getLocation();
            int dy = newY - p.y;
            if (Math.abs(dy) <= RELOC_STEP) {
                setLocation(targetX, newY);
                ((Timer)e.getSource()).stop();
            } else {
                setLocation(targetX, p.y + (dy > 0 ? RELOC_STEP : -RELOC_STEP));
            }
        });
        slide.start();
    }

    // ====== Util ======
    private static String wrapHtml(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.startsWith("<html") ? t : "<html>" + t + "</html>";
    }
    private static Color withAlpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }
    private void   setOpacitySafe(float v) { try { setOpacity(v); } catch (Throwable ignored) {} }
    private float  getOpacitySafe() { try { return getOpacity(); } catch (Throwable e) { return 1f; } }

    private static int mmToPxOnGc(double mm, GraphicsConfiguration gc) {
        if (mm <= 0) return 0;
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        double scaleX = 1.0;
        try { scaleX = gc.getDefaultTransform().getScaleX(); } catch (Throwable ignored) {}
        double px = (mm / 25.4) * dpi * scaleX;
        return (int)Math.round(px);
    }

    // ====== Manager stack & jadwal ======
    private static final class ToastManager {
        private final List<BaseToast> active = new ArrayList<>();
        private long lastStartAt = 0L;

        synchronized void enqueue(BaseToast t, String sound) {
            // jeda 1 detik antar toast
            long now = System.currentTimeMillis();
            long delay = Math.max(0, STAGGER_MS - (now - lastStartAt));
            lastStartAt = now + delay;

            Timer startTimer = new Timer((int)delay, null);
            startTimer.setRepeats(false);
            startTimer.addActionListener(e -> {
                synchronized (ToastManager.this) {
                    active.add(t);
                    // slot = index berdasarkan urutan masuk
                    int slot = active.indexOf(t);
                    t.computeAnchorAndSetBounds(slot);
                    t.setVisible(true);
                    t.animateInThenLifeThenOut(sound);
                }
            });
            startTimer.start();
        }

        synchronized void remove(BaseToast t) {
            int idx = active.indexOf(t);
            if (idx >= 0) active.remove(idx);
            // relayout semua (naikkan yang di bawah)
            for (int i = 0; i < active.size(); i++) {
                BaseToast toast = active.get(i);
                toast.retargetToSlot(i);
            }
        }
    }
    
// ====== Simple WAV player (file:... atau resource di classpath) ======
public static void playSound(String soundPathOrResource) {
    if (soundPathOrResource == null || soundPathOrResource.trim().isEmpty()) return;
    try {
        AudioInputStream ais;
        if (soundPathOrResource.startsWith("file:")) {
            File f = new File(soundPathOrResource.substring(5));
            if (!f.exists()) return;
            ais = AudioSystem.getAudioInputStream(f);
        } else {
            String res = soundPathOrResource.startsWith("/") ? soundPathOrResource.substring(1) : soundPathOrResource;
            URL url = BaseToast.class.getClassLoader().getResource(res);
            if (url == null) return;
            ais = AudioSystem.getAudioInputStream(url);
        }
        Clip clip = AudioSystem.getClip();
        clip.addLineListener(ev -> {
            if (LineEvent.Type.STOP.equals(ev.getType())) {
                clip.close();
            }
        });
        clip.open(ais);
        clip.start();
    } catch (Exception ex) {
        System.out.println("BaseToast.playSound err: " + ex.getMessage());
    }
}
    

}
