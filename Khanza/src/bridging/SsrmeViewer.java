package bridging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.CardLayout;
import java.awt.Window;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Halaman WebView2 di dalam proses SIMRS; satu jendela viewer dengan kontrol jendela native. */
public final class SsrmeViewer extends SsrmeUi.WindowFrame {
    private static SsrmeViewer active;
    private SsrmeClient.Link link;
    private final BooleanSupplier session;
    private final LinkRequest newLink;
    private final SsrmeTrace trace;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel canvas = new JPanel(pageLayout);
    private final JPanel surface = new JPanel(new BorderLayout());
    private final SsrmeUi.StatusPanel status = new SsrmeUi.StatusPanel();
    private boolean problemVisible;
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
        try {
            dialog.start();
            if (!dialog.closed) dialog.showBlocking();
        } finally { dialog.dispose(); }
    }

    private SsrmeViewer(Window owner, SsrmeClient.Setup setup, SsrmeClient.Link link,
                        String action, BooleanSupplier session, LinkRequest newLink, SsrmeTrace trace) {
        super(owner, "SATUSEHAT RME · SIMRS RSAJ");
        this.link = link; this.session = session; this.newLink = newLink; this.trace=trace;
        JPanel body = new JPanel(new BorderLayout()); body.setBackground(Color.WHITE); setContentPane(body);
        SsrmeUi.Header head=new SsrmeUi.Header(); head.update(setup,action); body.add(head,BorderLayout.NORTH);
        canvas.setBackground(Color.WHITE);surface.setBackground(Color.WHITE);
        canvas.add(surface,"page");canvas.add(status,"status");
        status.showStatus("Menyiapkan halaman SATUSEHAT","Menyiapkan browser internal SSRME...",SsrmeUi.StatusPanel.INFO);
        pageLayout.show(canvas,"status");
        body.add(canvas, BorderLayout.CENTER);
        JButton close = SsrmeLauncher.button("Tutup"); close.addActionListener(e -> dispose());
        reload.setEnabled(false);
        reload.addActionListener(e -> {
            if (!valid()) { dispose(); return; }
            if (!fresh(link)) { showProblem("Tautan SSRME sudah kedaluwarsa","Tautan sudah kedaluwarsa. Tutup dan minta tautan baru melalui SIMRS.",SsrmeUi.StatusPanel.LOCKED); return; }
            try { browser.reload(); restorePage(); } catch (RuntimeException ex) { failed(); }
        });
        external.addActionListener(e -> openExternal());
        body.add(SsrmeUi.footer(trace.button(this),SsrmeUi.guideButton(this,"EMERGENCY".equals(action)),reload,external,close),BorderLayout.SOUTH);
        guard = new Timer(400, e -> { if (!valid()) dispose(); });
    }

    private boolean valid() { return !closed && session.getAsBoolean(); }

    private void start() {
        guard.start();
        if (!valid()) { dispose(); return; }
        if (!fresh(link)) {
            showProblem("Tautan SSRME sudah kedaluwarsa","Tautan SSRME sudah kedaluwarsa. Tutup dan minta tautan baru, atau gunakan Buka di Browser.",SsrmeUi.StatusPanel.LOCKED);
            return;
        }
        message("Menyiapkan Microsoft Edge WebView2 dalam SIMRS...", false);
        try {
            Consumer<String> report = value -> SwingUtilities.invokeLater(() -> {
                if (valid() && !externalMode) browserMessage(value);
            });
            Runnable failure = () -> SwingUtilities.invokeLater(() -> {
                if (valid() && !externalMode) failed();
            });
            final String host = link.url.getHost();
            Predicate<String> navigation = url -> !closed && allowedNavigation(url, host);
            // Muat saat diperlukan supaya kegagalan bridge tetap mempunyai fallback manual.
            Class<?> type = SsrmeEdgeLoader.type();
            browser = (SsrmeBrowser) type.getMethod("create", URI.class, Consumer.class, Predicate.class, Runnable.class)
                    .invoke(null, link.url, report, navigation, failure);
            surface.removeAll(); surface.add(browser.component(), BorderLayout.CENTER);
            if(!problemVisible) pageLayout.show(canvas,"page");
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
        surface.removeAll();
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
        surface.removeAll();
        canvas.revalidate(); canvas.repaint(); reload.setEnabled(false);
        showProblem("Viewer internal belum tersedia","Viewer Edge belum tersedia. Gunakan Buka di Browser, lalu periksa paket runtime/ssrme-edge, Windows/Java 64-bit dan Microsoft Edge WebView2 Runtime.",SsrmeUi.StatusPanel.ERROR);
    }

    private void message(String value, boolean failed) {
        if(failed){showProblem("Halaman SSRME belum dapat dibuka",value,SsrmeUi.StatusPanel.ERROR);return;}
        if(browser==null){problemVisible=false;status.setReturnAction(null);
            status.showStatus("Menyiapkan halaman SATUSEHAT",value,SsrmeUi.StatusPanel.INFO);pageLayout.show(canvas,"status");
            canvas.revalidate();canvas.repaint();}
    }
    private void showProblem(String title,String explanation,int kind){
        problemVisible=true;status.showStatus(SsrmeUi.SHARING_MESSAGE.equals(explanation)?"Periksa berbagi resume medis":title,explanation,kind);
        if(SsrmeUi.SHARING_MESSAGE.equals(explanation))status.setGuidance(SsrmeUi.PATIENT_GUIDE);
        status.setReturnAction(browser==null?null:this::restorePage);pageLayout.show(canvas,"status");canvas.revalidate();canvas.repaint();
    }
    private void restorePage(){
        if(!valid()){dispose();return;}
        if(browser!=null){problemVisible=false;pageLayout.show(canvas,"page");canvas.revalidate();canvas.repaint();}
    }
    private void browserMessage(String value) {
        if(value!=null&&value.startsWith("Viewer Edge siap.")){
            if(!problemVisible&&browser!=null)restorePage();
        }else if(value!=null&&value.startsWith("Navigasi di luar")){
            showProblem("Navigasi halaman dibatasi",value,SsrmeUi.StatusPanel.LOCKED);
        }else message(value,false);
    }

    @Override public void dispose() {
        if (!closed) {
            closed = true; guard.stop(); surface.removeAll(); canvas.repaint();
            if (browser != null) { try { browser.close(); } catch (RuntimeException | LinkageError ignored) { } browser = null; }
            link = null; if (active == this) active = null;
        }
        super.dispose();
    }

}
