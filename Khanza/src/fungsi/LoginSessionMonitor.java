package fungsi;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.util.function.Consumer;

/** Poll ringan di background; semua state UI dan callback hanya di EDT. */
public final class LoginSessionMonitor {
    private final LoginSessionGuard guard;
    private final Consumer<Boolean> onLost;
    private final Timer timer;
    private LoginSessionGuard.Ticket ticket;
    private SwingWorker<Boolean, Void> worker;

    public LoginSessionMonitor(LoginSessionGuard guard, Consumer<Boolean> onLost) {
        this(guard, onLost, 2000);
    }

    /** Interval khusus juga memungkinkan pengujian tanpa menunggu dua detik. */
    public LoginSessionMonitor(LoginSessionGuard guard, Consumer<Boolean> onLost, int interval) {
        if (guard == null || onLost == null || interval <= 0) throw new IllegalArgumentException("Pemantau sesi tidak valid.");
        this.guard = guard;
        this.onLost = onLost;
        timer = new Timer(interval, event -> check());
        timer.setCoalesce(true);
    }

    public LoginSessionGuard.Ticket ticket() { return ticket; }

    public void start(LoginSessionGuard.Ticket next) {
        requireEdt();
        if (next == null) throw new IllegalArgumentException("Ticket login belum tersedia.");
        if (ticket == next) return;
        stop(true);
        ticket = next;
        timer.start();
        check();
    }

    public void stop(boolean release) {
        requireEdt();
        LoginSessionGuard.Ticket old = ticket;
        ticket = null;
        timer.stop();
        // Worker lama boleh selesai; hasilnya tidak boleh mengakhiri sesi baru.
        worker = null;
        if (release) discard(old);
    }

    public void discard(LoginSessionGuard.Ticket old) {
        if (old == null) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                guard.release(old);
                return null;
            }
            @Override protected void done() {
                try { get(); }
                catch (Exception ignored) {
                    // Sisa row setelah aplikasi/koneksi terputus tidak menghalangi
                    // login baru: claim berikutnya selalu mengganti token.
                }
            }
        }.execute();
    }

    private void check() {
        if (ticket == null || worker != null) return;
        final LoginSessionGuard.Ticket checked = ticket;
        worker = new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return guard.isCurrent(checked);
            }
            @Override protected void done() {
                if (worker != this || ticket != checked) return;
                worker = null;
                boolean valid = false;
                boolean unavailable = false;
                try { valid = get(); }
                catch (Exception exception) { unavailable = true; }
                if (!valid) {
                    stop(false);
                    // Error koneksi dibedakan dari penggantian sesi oleh login lain.
                    onLost.accept(unavailable);
                }
            }
        };
        worker.execute();
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Pemantau sesi harus di EDT.");
    }
}
