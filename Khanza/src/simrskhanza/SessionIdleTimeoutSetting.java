package simrskhanza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Pembacaan timeout idle terpisah dari aturan single login dan lease sesi. */
final class SessionIdleTimeoutSetting {
    static final int DEFAULT_MINUTES = 15;
    static final int MIN_MINUTES = 1;
    static final int MAX_MINUTES = 1440;
    static final int REFRESH_MILLIS = 60_000;
    private static final String SQL =
            "select timeout_minutes from rsaj_login_timeout_setting where id=1";

    @FunctionalInterface
    interface ConnectionFactory { Connection open() throws SQLException; }
    enum Source { DATABASE, MISSING_TABLE, MISSING_ROW }

    static final class Value {
        final int minutes;
        final Source source;
        private Value(int minutes, Source source) { this.minutes = minutes; this.source = source; }
        long millis() { return TimeUnit.MINUTES.toMillis(minutes); }
    }

    private final ConnectionFactory connections;
    SessionIdleTimeoutSetting(ConnectionFactory connections) {
        if (connections == null) throw new IllegalArgumentException("Koneksi pengaturan wajib tersedia.");
        this.connections = connections;
    }

    /** SELECT saja; migrasi dilakukan oleh SQL pemasangan, bukan aplikasi. */
    Value read() throws SQLException {
        try (Connection c = connections.open()) {
            if (c == null) throw new SQLException("Koneksi pengaturan timeout tidak tersedia.", "08003");
            try (PreparedStatement q = c.prepareStatement(SQL)) {
                q.setQueryTimeout(5);
                try (ResultSet row = q.executeQuery()) {
                    if (!row.next()) return new Value(DEFAULT_MINUTES, Source.MISSING_ROW);
                    String raw = row.getString(1);
                    if (raw == null || !raw.matches("[0-9]{1,4}")) throw invalid();
                    int minutes = Integer.parseInt(raw);
                    if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) throw invalid();
                    return new Value(minutes, Source.DATABASE);
                }
            }
        } catch (SQLException failure) {
            if (failure.getErrorCode() == 1146 && "42S02".equals(failure.getSQLState())) {
                return new Value(DEFAULT_MINUTES, Source.MISSING_TABLE);
            }
            throw failure;
        }
    }

    private static SQLException invalid() {
        return new SQLException("Durasi timeout harus 1 sampai 1440 menit.", "22023");
    }

    /** Waktu idle tidak direset oleh polling/penggantian nilai pengaturan. */
    static final class IdleClock {
        private long timeoutNanos;
        private long lastActivityNanos;
        IdleClock(long timeoutMillis) { setTimeoutMillis(timeoutMillis); }
        void activity(long nowNanos) { lastActivityNanos = nowNanos; }
        void setTimeoutMillis(long timeoutMillis) {
            if (timeoutMillis <= 0) throw new IllegalArgumentException("Timeout harus lebih dari nol.");
            timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        }
        long timeoutMillis() { return TimeUnit.NANOSECONDS.toMillis(timeoutNanos); }
        boolean expired(long nowNanos) { return nowNanos - lastActivityNanos >= timeoutNanos; }
    }

    /** Maksimal satu pembacaan aktif; JDBC berjalan di worker, callback di EDT. */
    static final class Poller implements AutoCloseable {
        private final SessionIdleTimeoutSetting setting;
        private final Consumer<Value> applied;
        private final Consumer<Throwable> failed;
        private final Timer timer;
        private SwingWorker<Value, Void> worker;
        private long generation;
        private boolean active, closed;

        Poller(SessionIdleTimeoutSetting setting, Consumer<Value> applied, Consumer<Throwable> failed) {
            this(setting, applied, failed, REFRESH_MILLIS);
        }
        Poller(SessionIdleTimeoutSetting setting, Consumer<Value> applied, Consumer<Throwable> failed, int intervalMillis) {
            if (setting == null || applied == null || failed == null || intervalMillis <= 0) {
                throw new IllegalArgumentException("Pemantau timeout tidak valid.");
            }
            this.setting = setting; this.applied = applied; this.failed = failed;
            timer = new Timer(intervalMillis, event -> refresh());
            timer.setCoalesce(true);
        }
        void start() {
            requireEdt();
            if (closed) return;
            active = true;
            timer.start();
            refresh();
        }
        void stop() {
            requireEdt();
            active = false;
            generation++;
            timer.stop();
            SwingWorker<Value, Void> previous = worker;
            worker = null;
            if (previous != null) previous.cancel(true);
        }
        void refresh() {
            requireEdt();
            if (!active || closed || worker != null) return;
            final long checked = generation;
            worker = new SwingWorker<Value, Void>() {
                @Override protected Value doInBackground() throws Exception { return setting.read(); }
                @Override protected void done() {
                    if (worker != this || checked != generation || !active || closed) return;
                    worker = null;
                    Value value;
                    try { value = get(); }
                    catch (CancellationException ignored) { return; }
                    catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        failed.accept(interrupted);
                        return;
                    } catch (ExecutionException failure) {
                        failed.accept(failure.getCause() == null ? failure : failure.getCause());
                        return;
                    }
                    applied.accept(value);
                }
            };
            worker.execute();
        }
        @Override public void close() {
            requireEdt();
            if (closed) return;
            stop();
            closed = true;
        }
        private static void requireEdt() {
            if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Pemantau timeout harus di EDT.");
        }
    }
}
