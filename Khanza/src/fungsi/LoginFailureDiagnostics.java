package fungsi;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.net.ssl.SSLException;

/** Diagnostik login tanpa pesan exception mentah, query, parameter, atau path. */
public final class LoginFailureDiagnostics {
    public enum Stage {
        LOGIN_UI, CONNECT, SECURITY_SETTING, ACCOUNT_LOCK, SESSION_CHECK,
        CAPTCHA, VERIFY_PASSWORD, DEVICE_ID, CLAIM_SESSION, RESET_ATTEMPTS,
        AUDIT, COMMIT, CLOSE_CONNECTION
    }

    public enum Reason {
        DEVICE_PERMISSION, DEVICE_ID, DATABASE_CONFIG, SECURITY_CONFIG,
        DATABASE_PERMISSION, MISSING_TABLE, TIMEOUT, NETWORK, TLS, BUSY, UNKNOWN
    }

    private static final int MAX_ERRORS = 12;
    private LoginFailureDiagnostics() { }

    /** Simpan konteks tanpa mengganti SQLState, kode vendor, dan cause aslinya. */
    private static final class Failure extends SQLException {
        private final Stage stage;
        Failure(Stage stage, SQLException cause) {
            super("Pengaman login gagal pada tahap " + stage.name() + ".",
                    cause.getSQLState(), cause.getErrorCode(), cause);
            this.stage = stage;
        }
    }

    public static SQLException at(Stage stage, SQLException cause) {
        if (cause == null || stage == null) throw new IllegalArgumentException("Konteks diagnostik wajib tersedia.");
        return cause instanceof Failure ? cause : new Failure(stage, cause);
    }

    public static Stage stage(Throwable error, Stage fallback) {
        for (Throwable item : chain(error)) {
            if (item instanceof Failure) return ((Failure) item).stage;
        }
        return fallback == null ? Stage.LOGIN_UI : fallback;
    }

    public static Reason reason(Throwable error, Stage fallback) {
        Stage stage = stage(error, fallback);
        boolean permission = false, missing = false, timeout = false;
        boolean network = false, tls = false, busy = false;
        boolean databaseConfig = false, securityConfig = false;
        for (Throwable item : chain(error)) {
            if (stage == Stage.DEVICE_ID
                    && (item instanceof AccessDeniedException || item instanceof SecurityException)) {
                return Reason.DEVICE_PERMISSION;
            }
            timeout |= item instanceof SocketTimeoutException || item instanceof SQLTimeoutException;
            network |= item instanceof ConnectException || item instanceof NoRouteToHostException
                    || item instanceof UnknownHostException
                    || item.getClass().getSimpleName().equals("CommunicationsException");
            tls |= item instanceof SSLException;
            if (item instanceof SQLException) {
                SQLException sql = (SQLException) item;
                String state = sql.getSQLState();
                network |= state != null && state.startsWith("08");
                timeout |= "HYT00".equals(state) || "HYT01".equals(state) || "S1T00".equals(state);
                int code = sql.getErrorCode();
                permission |= code == 1044 || code == 1045 || code == 1142 || code == 1143
                        || "28000".equals(state);
                missing |= code == 1146 || "42S02".equals(state);
                busy |= code == 1205 || code == 1213;
                // Hanya cocokkan pesan aplikasi yang tetap; tidak pernah mencetaknya.
                String message = sql.getMessage();
                databaseConfig |= "Konfigurasi database tidak tersedia.".equals(message);
                securityConfig |= "Pengaturan pengaman login belum dipasang.".equals(message)
                        || "Pengaturan pengaman login tidak valid.".equals(message)
                        || "Pengaturan komputer aktif tidak valid.".equals(message)
                        || "Batas komputer hanya satu atau dua.".equals(message)
                        || "Status single login harus 0 atau 1.".equals(message);
                network |= "Koneksi pengaman login tidak tersedia.".equals(message)
                        || "Koneksi pemeriksaan sesi tidak tersedia.".equals(message)
                        || "Koneksi perpanjangan sesi tidak tersedia.".equals(message);
            }
        }
        if (stage == Stage.DEVICE_ID) return Reason.DEVICE_ID;
        if (databaseConfig) return Reason.DATABASE_CONFIG;
        if (securityConfig) return Reason.SECURITY_CONFIG;
        if (permission) return Reason.DATABASE_PERMISSION;
        if (missing) return Reason.MISSING_TABLE;
        if (tls) return Reason.TLS;
        if (timeout) return Reason.TIMEOUT;
        if (network) return Reason.NETWORK;
        if (busy) return Reason.BUSY;
        return Reason.UNKNOWN;
    }

    /** Pesan dua baris agar tetap sesuai area pesan pada form login yang ada. */
    public static String userMessage(Throwable error) {
        switch (reason(error, Stage.LOGIN_UI)) {
            case DEVICE_PERMISSION:
                return "Izin identitas komputer ditolak.\nHubungi Tim IT.";
            case DEVICE_ID:
                return "Identitas komputer belum tersedia.\nHubungi Tim IT.";
            case DATABASE_CONFIG:
            case SECURITY_CONFIG:
                return "Pengaturan login belum siap.\nHubungi Tim IT.";
            case DATABASE_PERMISSION:
                return "Akses database pengaman ditolak.\nHubungi Tim IT.";
            case MISSING_TABLE:
                return "Tabel pengaman login belum siap.\nHubungi Tim IT.";
            case TIMEOUT:
                return "Pemeriksaan login kehabisan waktu.\nCoba lagi atau hubungi Tim IT.";
            case NETWORK:
                return "Koneksi database belum tersedia.\nPeriksa koneksi, lalu coba lagi.";
            case TLS:
                return "Keamanan koneksi database gagal.\nHubungi Tim IT.";
            case BUSY:
                return "Pengaman login sedang sibuk.\nTunggu sebentar lalu coba lagi.";
            default:
                return "Login belum dapat diperiksa.\nHubungi Tim IT.";
        }
    }

    public static void log(Throwable error) { log(error, Stage.LOGIN_UI); }

    public static void log(Throwable error, Stage fallback) {
        System.out.print(format(error, fallback));
    }

    public static String format(Throwable error, Stage fallback) {
        StringBuilder output = new StringBuilder("[Pengaman Login] stage=")
                .append(stage(error, fallback)).append(", reason=").append(reason(error, fallback))
                .append(", os=").append(os()).append('\n');
        int index = 0;
        for (Throwable item : chain(error)) {
            output.append("[Pengaman Login] error[").append(index++).append("]=")
                    .append(symbol(item.getClass().getName()));
            if (item instanceof SQLException) {
                SQLException sql = (SQLException) item;
                String state = sql.getSQLState();
                output.append(", SQLState=").append(state == null ? "null"
                        : state.matches("[A-Z0-9]{5}") ? state : "OTHER")
                        .append(", kode=").append(sql.getErrorCode());
            }
            output.append('\n');
            StackTraceElement[] frames = item.getStackTrace();
            for (int i = 0; i < Math.min(3, frames.length); i++) {
                StackTraceElement frame = frames[i];
                output.append("[Pengaman Login] at ").append(symbol(frame.getClassName()))
                        .append('.').append(symbol(frame.getMethodName()))
                        .append(':').append(frame.getLineNumber()).append('\n');
            }
        }
        return output.toString();
    }

    private static List<Throwable> chain(Throwable error) {
        List<Throwable> result = new ArrayList<>();
        if (error == null) return result;
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(error);
        while (!pending.isEmpty() && result.size() < MAX_ERRORS) {
            Throwable item = pending.removeFirst();
            if (!seen.add(item)) continue;
            result.add(item);
            if (item.getCause() != null) pending.addLast(item.getCause());
            if (item instanceof SQLException && ((SQLException) item).getNextException() != null) {
                pending.addLast(((SQLException) item).getNextException());
            }
            Throwable[] suppressed = item.getSuppressed();
            for (int i = 0; i < Math.min(MAX_ERRORS, suppressed.length); i++) pending.addLast(suppressed[i]);
        }
        return result;
    }

    private static String symbol(String value) {
        return value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9_.$<>]", "_");
    }

    private static String os() {
        String value;
        try { value = System.getProperty("os.name", "").toLowerCase(Locale.ROOT); }
        catch (SecurityException denied) { return "OTHER"; }
        if (value.contains("mac")) return "MAC";
        if (value.contains("win")) return "WINDOWS";
        if (value.contains("linux")) return "LINUX";
        return "OTHER";
    }
}
