package bridging;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;

/** WebView yang sama dengan iCare, tanpa paket browser/native tambahan. */
public final class SsrmeFxBrowser implements SsrmeBrowser {
    private final JFXPanel panel;
    private final AtomicBoolean closed = new AtomicBoolean();
    private WebEngine engine; // Hanya diakses dari thread JavaFX.
    private Path profile;

    public static SsrmeBrowser create(URI url, Consumer<String> report,
                                      Predicate<String> navigation, Runnable failure) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("EDT required");
        return new SsrmeFxBrowser(url, report, navigation, failure);
    }

    private SsrmeFxBrowser(URI url, Consumer<String> report, Predicate<String> navigation, Runnable failure) {
        panel = new JFXPanel();
        // Toolkit JavaFX dipakai juga oleh iCare dan form lain. Penutupan SSRME tidak mematikannya.
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            if (closed.get()) return;
            try {
                profile = Files.createTempDirectory("rsaj-ssrme-web-");
                WebView view = new WebView(); engine = view.getEngine();
                engine.setUserDataDirectory(profile.toFile());
                engine.setJavaScriptEnabled(true);
                // Sama seperti iCare: popup tetap memakai WebEngine dalam jendela SIMRS.
                engine.setCreatePopupHandler(features -> closed.get() ? null : engine);
                engine.setOnAlert(event -> {
                    askSwing(() -> {
                        javax.swing.JOptionPane.showMessageDialog(panel, plain(event.getData()), "SATUSEHAT", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        return Boolean.TRUE;
                    }, Boolean.FALSE);
                });
                engine.setConfirmHandler(message -> askSwing(() -> javax.swing.JOptionPane.showConfirmDialog(panel,
                        plain(message), "Konfirmasi SATUSEHAT", javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE) == javax.swing.JOptionPane.YES_OPTION, Boolean.FALSE));
                engine.setPromptHandler(prompt -> askSwing(() -> (String)javax.swing.JOptionPane.showInputDialog(panel,
                        plain(prompt.getMessage()), "SATUSEHAT", javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, null, prompt.getDefaultValue()), null));
                engine.locationProperty().addListener((observable, before, after) -> {
                    // WebEngine mengosongkan location ketika memulai load; itu bukan URL tujuan.
                    if (closed.get() || after == null || after.isEmpty() || navigation.test(after)) return;
                    engine.getLoadWorker().cancel();
                    engine.load("about:blank");
                    report.accept("Navigasi di luar halaman SSRME dibatasi. Gunakan Buka di Browser jika diperlukan.");
                });
                engine.getLoadWorker().stateProperty().addListener((observable, before, after) -> {
                    if (closed.get()) return;
                    if (after == Worker.State.RUNNING) report.accept("Memuat halaman resmi SATUSEHAT...");
                    else if (after == Worker.State.SUCCEEDED && !"about:blank".equals(engine.getLocation()))
                        report.accept("Halaman SATUSEHAT selesai dimuat. Periksa status pada halaman.");
                    else if (after == Worker.State.FAILED)
                        report.accept("Halaman belum dapat dimuat di JavaFX. Coba Muat Ulang atau gunakan Buka di Browser.");
                });
                engine.setOnError(event -> {
                    if (!closed.get()) report.accept("Viewer mengalami kendala. Gunakan Buka di Browser bila halaman tidak tampil dengan benar.");
                });
                panel.setScene(new Scene(view));
                if (!closed.get() && navigation.test(url.toString())) engine.load(url.toString());
                else release();
            } catch (IOException | RuntimeException | LinkageError e) { release(); failure.run(); }
        });
    }

    @Override public Component component() { return panel; }
    @Override public void reload() {
        if (!closed.get()) Platform.runLater(() -> { if (!closed.get() && engine != null) engine.reload(); });
    }
    @Override public void close() {
        if (closed.compareAndSet(false, true)) Platform.runLater(this::release);
    }
    private void release() {
        if (engine != null) {
            engine.getLoadWorker().cancel(); engine.setCreatePopupHandler(null);
            engine.setJavaScriptEnabled(false); engine.load("about:blank");
            panel.setScene(null); engine = null;
        }
        if (profile != null) {
            // Best effort: beberapa versi WebKit masih menahan file sampai engine dibebaskan JVM.
            try (java.util.stream.Stream<Path> files = Files.walk(profile)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException ignored) { }
                });
            } catch (IOException ignored) { }
            profile = null;
        }
        // Tidak mengubah CookieHandler global: cookie iCare/form lain tidak dihapus.
        // Tidak mencetak URL/token dan tidak memanggil Platform.exit().
    }
    private <T> T askSwing(java.util.concurrent.Callable<T> task, T fallback) {
        if (closed.get()) return fallback;
        java.util.concurrent.atomic.AtomicReference<T> result = new java.util.concurrent.atomic.AtomicReference<T>(fallback);
        try {
            SwingUtilities.invokeAndWait(() -> {
                if (!closed.get()) try { result.set(task.call()); } catch (Exception ignored) { }
            });
        } catch (Exception ignored) { }
        return result.get();
    }
    private static javax.swing.JTextArea plain(String message) {
        String value = message == null ? "" : message;
        if (value.length() > 1000) value = value.substring(0,1000);
        javax.swing.JTextArea text = new javax.swing.JTextArea(value, 0, 48);
        text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); text.setOpaque(false);
        text.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12)); return text;
    }
}
