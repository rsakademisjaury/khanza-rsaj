package bridging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Viewer JavaFX seperti iCare BPJS; satu jendela SSRME per proses SIMRS. */
public final class SsrmeViewer extends JDialog {
    private static SsrmeViewer active;
    private SsrmeClient.Link link;
    private final BooleanSupplier session;
    private final LinkRequest newLink;
    private final SsrmeTrace trace;
    private final JPanel canvas = new JPanel(new BorderLayout());
    private final JTextArea status = new JTextArea();
    private final JButton reload = SsrmeLauncher.button("Muat Ulang");
    private final JButton external = SsrmeLauncher.button("Buka di Browser");
    private final Timer guard;
    private SsrmeBrowser browser;
    private volatile boolean closed;
    private boolean externalMode;
    @FunctionalInterface public interface LinkRequest { SsrmeClient.Link request() throws Exception; }

    public static void open(Window owner, SsrmeClient.Setup setup, SsrmeClient.Link link,
                            String action, BooleanSupplier session, LinkRequest newLink, SsrmeTrace trace) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("EDT required");
        if (!session.getAsBoolean()) return;
        if (active != null) active.dispose();
        SsrmeViewer dialog = new SsrmeViewer(owner, setup, link, action, session, newLink, trace);
        active = dialog;
        dialog.start();
        if (!dialog.closed) dialog.setVisible(true);
    }

    private SsrmeViewer(Window owner, SsrmeClient.Setup setup, SsrmeClient.Link link,
                        String action, BooleanSupplier session, LinkRequest newLink, SsrmeTrace trace) {
        super(owner, "SATUSEHAT RME · SIMRS RSAJ", ModalityType.APPLICATION_MODAL);
        this.link = link; this.session = session; this.newLink = newLink; this.trace=trace;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel body = new JPanel(new BorderLayout(0, 10)); body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18)); setContentPane(body);
        JPanel head = new JPanel(new BorderLayout(12, 5)); head.setOpaque(false);
        JPanel identity = new JPanel(new java.awt.GridLayout(0, 1, 0, 5)); identity.setOpaque(false);
        JLabel title = text("SATUSEHAT RME", true); title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 21));
        title.setForeground(new Color(35, 99, 190)); identity.add(title);
        identity.add(text(setup.patientName + "  ·  No. RM " + setup.patientRm, true));
        identity.add(text(("VIEW".equals(action) ? "Rekam medis nasional" : "Persetujuan akses")
                + "  |  Operator: " + setup.actor, false));
        head.add(identity, BorderLayout.CENTER);
        JLabel official = text("Halaman resmi SATUSEHAT", false);
        official.setForeground(new Color(0, 126, 112)); head.add(official, BorderLayout.EAST);
        body.add(head, BorderLayout.NORTH);
        canvas.setBackground(new Color(242, 247, 253));
        canvas.setBorder(BorderFactory.createLineBorder(new Color(216, 226, 239)));
        canvas.add(text("  Menyiapkan browser SATUSEHAT...", false), BorderLayout.CENTER);
        body.add(canvas, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 8)); footer.setOpaque(false);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12)); status.setEditable(false);
        status.setFocusable(false); status.setLineWrap(true); status.setWrapStyleWord(true);
        status.setOpaque(false); status.setRows(2); footer.add(status, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        JButton close = SsrmeLauncher.button("Tutup"); close.addActionListener(e -> dispose());
        reload.setEnabled(false);
        reload.setIcon(new ActionIcon(0)); external.setIcon(new ActionIcon(1)); close.setIcon(new ActionIcon(2));
        reload.addActionListener(e -> {
            if (!valid()) { dispose(); return; }
            if (!fresh(link)) { message("Tautan sudah kedaluwarsa. Tutup dan minta tautan baru melalui SIMRS.", true); return; }
            try { browser.reload(); } catch (RuntimeException ex) { failed(); }
        });
        external.addActionListener(e -> openExternal());
        actions.add(reload); actions.add(external); actions.add(close);
        JPanel bottom=new JPanel(new BorderLayout());bottom.setOpaque(false);bottom.add(trace.button(this),BorderLayout.WEST);bottom.add(actions,BorderLayout.EAST);
        footer.add(bottom, BorderLayout.SOUTH); body.add(footer, BorderLayout.SOUTH);
        guard = new Timer(400, e -> { if (!valid()) dispose(); });
        SsrmeUi.fit(this, owner);
    }

    private boolean valid() { return !closed && session.getAsBoolean(); }

    private void start() {
        guard.start();
        if (!valid()) { dispose(); return; }
        if (!fresh(link)) {
            message("Tautan SSRME sudah kedaluwarsa. Tutup dan minta tautan baru, atau gunakan Buka di Browser.", true);
            return;
        }
        message("Menyiapkan Microsoft Edge WebView2 dalam SIMRS...", false);
        try {
            Consumer<String> report = value -> SwingUtilities.invokeLater(() -> {
                if (valid() && !externalMode) message(value, false);
            });
            Runnable failure = () -> SwingUtilities.invokeLater(() -> {
                if (valid() && !externalMode) failed();
            });
            final String host = link.url.getHost();
            Predicate<String> navigation = url -> !closed && allowedNavigation(url, host);
            // Muat saat diperlukan supaya kegagalan library JavaFX tetap dapat ditangani dialog fallback.
            Class<?> type = SsrmeEdgeLoader.type();
            browser = (SsrmeBrowser) type.getMethod("create", URI.class, Consumer.class, Predicate.class, Runnable.class)
                    .invoke(null, link.url, report, navigation, failure);
            canvas.removeAll(); canvas.add(browser.component(), BorderLayout.CENTER);
            canvas.revalidate(); canvas.repaint(); reload.setEnabled(true);
        } catch (Exception | LinkageError e) { failed(); }
    }

    /** Hanya navigasi halaman utama; request API/gambar tetap ditangani WebView dan kebijakan web. */
    public static boolean allowedNavigation(String value, String expectedHost) {
        if ("about:blank".equals(value) || "about:srcdoc".equals(value)) return true;
        if (("satusehat.kemkes.go.id".equals(expectedHost)||"satusehat-stg.dto.kemkes.go.id".equals(expectedHost))
                && value != null && value.startsWith("blob:https://"+expectedHost+"/")) return true;
        try {
            URI uri = new URI(value);
            boolean official = "satusehat.kemkes.go.id".equals(expectedHost)
                    || "satusehat-stg.dto.kemkes.go.id".equals(expectedHost);
            return official && "https".equals(uri.getScheme()) && expectedHost.equals(uri.getHost())
                    && uri.getUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443)
                    && ("/rekammedis".equals(uri.getPath()) || uri.getPath().startsWith("/rekammedis/"));
        } catch (Exception e) { return false; }
    }

    static boolean fresh(SsrmeClient.Link link) {
        try { return link != null && OffsetDateTime.parse(link.expiresAt).toInstant().isAfter(Instant.now()); }
        catch (Exception e) { return false; }
    }

    private void openExternal() {
        if (!valid()) { dispose(); return; }
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) throw new IllegalStateException();
        } catch (Exception e) { message("Browser default belum dapat dibuka. Minta IT memeriksa pengaturan browser komputer ini.", true); return; }
        externalMode = true;
        // Tautan launch dapat sekali pakai. Minta tautan baru; jangan kirim ulang yang sudah dinavigasi.
        if (browser != null) { try { browser.close(); } catch (RuntimeException | LinkageError ignored) { } browser = null; }
        canvas.removeAll(); canvas.add(text("  Menyiapkan pembukaan melalui browser...", false), BorderLayout.CENTER);
        canvas.revalidate(); canvas.repaint(); reload.setEnabled(false); external.setEnabled(false);
        message("Meminta tautan baru untuk browser...", false);
        new SwingWorker<SsrmeClient.Link, Void>() {
            @Override protected SsrmeClient.Link doInBackground() throws Exception { return newLink.request(); }
            @Override protected void done() {
                if (!valid()) { dispose(); return; }
                external.setEnabled(true);
                try {
                    SsrmeClient.Link result = get();
                    if (!fresh(result)) throw new IllegalStateException();
                    Desktop.getDesktop().browse(result.url); dispose();
                } catch (Exception e) {
                    Throwable cause = e instanceof java.util.concurrent.ExecutionException ? e.getCause() : e;
                    message(cause instanceof SsrmeClient.Failure ? cause.getMessage()
                            : "Browser belum dapat dibuka. Periksa koneksi server dan pengaturan browser komputer ini.", true);
                }
            }
        }.execute();
    }

    private void failed() {
        trace.add(new SsrmeTrace.Entry(SsrmeTrace.now(),"LOCAL","local://viewer",0,0,"{}","{\"code\":\"VIEWER_UNAVAILABLE\"}"));
        if (browser != null) { try { browser.close(); } catch (RuntimeException | LinkageError ignored) { } browser = null; }
        canvas.removeAll(); canvas.add(text("  Viewer internal belum tersedia pada komputer ini.", false), BorderLayout.CENTER);
        canvas.revalidate(); canvas.repaint(); reload.setEnabled(false);
        message("Viewer Edge belum tersedia. Gunakan Buka di Browser, lalu periksa paket runtime/ssrme-edge, Windows/Java 64-bit dan Microsoft Edge WebView2 Runtime.", true);
    }

    private void message(String value, boolean failed) {
        status.setForeground(failed ? new Color(166, 52, 52) : new Color(36, 53, 75));
        status.setText(value); status.setCaretPosition(0);
    }

    @Override public void dispose() {
        if (!closed) {
            closed = true; guard.stop(); canvas.removeAll(); canvas.repaint();
            if (browser != null) { try { browser.close(); } catch (RuntimeException | LinkageError ignored) { } browser = null; }
            link = null; if (active == this) active = null;
        }
        super.dispose();
    }

    private static JLabel text(String value, boolean bold) {
        // JLabel menafsirkan <html>; identitas dari database harus selalu teks biasa.
        JLabel label = new JLabel(value); label.putClientProperty("html.disable", Boolean.TRUE);
        label.setFont(new Font(bold ? "Segoe UI Semibold" : "Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(36, 53, 75)); return label;
    }
    private static final class ActionIcon implements javax.swing.Icon {
        private final int kind;
        ActionIcon(int kind) { this.kind = kind; }
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics original, int x, int y) {
            java.awt.Graphics2D g = (java.awt.Graphics2D)original.create(); g.translate(x, y);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.isEnabled() ? new Color(35,99,190) : new Color(155,164,177));
            g.setStroke(new java.awt.BasicStroke(1.7f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            if (kind == 0) { g.drawArc(2,2,11,11,40,290); g.drawLine(12,1,13,5); g.drawLine(9,5,13,5); }
            else if (kind == 1) { g.drawLine(9,2,14,2); g.drawLine(14,2,14,7); g.drawLine(14,2,7,9); g.drawLine(6,4,2,4); g.drawLine(2,4,2,14); g.drawLine(2,14,12,14); g.drawLine(12,14,12,10); }
            else { g.drawLine(4,4,12,12); g.drawLine(12,4,4,12); }
            g.dispose();
        }
    }
}
