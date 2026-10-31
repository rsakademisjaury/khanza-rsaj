package fungsi;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Batas komputer per akun yang dapat dimatikan; setiap aplikasi memiliki ticket terpisah. */
public final class LoginSessionGuard {
    @FunctionalInterface
    public interface ConnectionFactory { Connection open() throws SQLException; }
    public static final class Ticket {
        private final String accountKey, deviceId, generation, token;
        private Ticket(String accountKey, String deviceId, String generation, String token) {
            this.accountKey = accountKey; this.deviceId = deviceId;
            this.generation = generation; this.token = token;
        }
    }
    private static final int LEASE_SECONDS = 90;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ConnectionFactory writes, reads;
    public LoginSessionGuard(ConnectionFactory writes, ConnectionFactory reads) {
        if (writes == null || reads == null) throw new IllegalArgumentException("Koneksi sesi wajib tersedia.");
        this.writes = writes; this.reads = reads;
    }

    /** Dibaca ulang tanpa cache agar perubahan status berlaku pada sesi berjalan. */
    private static boolean singleLoginEnabled(Connection c) throws SQLException {
        try (PreparedStatement q = prepare(c,
                "select status from rsaj_login_session_setting where id=1");
             ResultSet row = q.executeQuery()) {
            // Instalasi lama tetap memakai perilaku ON sampai migrasi dipasang.
            if (!row.next()) return true;
            String status = row.getString(1);
            if ("0".equals(status)) return false;
            if ("1".equals(status)) return true;
            throw new SQLException("Status single login harus 0 atau 1.");
        } catch (SQLException exception) {
            // Hanya tabel baru yang belum ada boleh menggunakan default ON.
            // Gangguan koneksi, izin SELECT, dan konfigurasi rusak tetap dilaporkan.
            if (exception.getErrorCode() == 1146 && "42S02".equals(exception.getSQLState())) return true;
            throw exception;
        }
    }

    // Guard autentikasi telah mengunci baris rsaj_login_security akun yang sama.
    static Ticket claim(Connection c, String accountKey, String username, String deviceId) throws SQLException {
        if (deviceId == null || !deviceId.matches("[0-9a-f]{64}")) throw new SQLException("Identitas komputer tidak valid.");
        int maximum = Integer.MAX_VALUE;
        if (singleLoginEnabled(c)) {
            maximum = 1;
            try (PreparedStatement q = prepare(c, "select max_devices,aktif from rsaj_login_multi_device "
                + "where username=? and binary username=binary ?")) {
                q.setString(1, username); q.setString(2, username);
                try (ResultSet row = q.executeQuery()) {
                    if (row.next()) {
                        int active = row.getInt(2), configured = row.getInt(1);
                        if (active != 0 && active != 1) throw new SQLException("Pengaturan komputer aktif tidak valid.");
                        if (active == 1) {
                            if (configured < 1 || configured > 2) throw new SQLException("Batas komputer hanya satu atau dua.");
                            maximum = configured;
                        }
                    }
                }
            }
        }
        prune(c, accountKey);
        List<String> devices = new ArrayList<>();
        String generation = null;
        long sequence = 0;
        try (PreparedStatement q = prepare(c, "select device_id,generation_token,login_order from rsaj_login_device "
                + "where account_key=? order by login_order,device_id for update")) {
            q.setString(1, accountKey);
            try (ResultSet row = q.executeQuery()) {
                while (row.next()) {
                    String id = row.getString(1); devices.add(id);
                    sequence = Math.max(sequence, row.getLong(3));
                    if (id.equals(deviceId)) generation = row.getString(2);
                }
            }
        }
        if (generation == null) {
            generation = randomToken(); devices.add(deviceId);
            try (PreparedStatement q = prepare(c, "insert into rsaj_login_device "
                    + "(account_key,device_id,generation_token,login_order,started_at) values (?,?,?,?,current_timestamp)")) {
                q.setString(1, accountKey); q.setString(2, deviceId); q.setString(3, generation);
                q.setLong(4, sequence + 1); q.executeUpdate();
            }
        }
        // Login baru dipertahankan; cabut seluruh instance komputer paling awal.
        while (devices.size() > maximum) {
            String oldest = null;
            for (String id : devices) if (!id.equals(deviceId)) { oldest = id; break; }
            if (oldest == null) throw new SQLException("Registry komputer tidak valid.");
            try (PreparedStatement q = prepare(c, "delete from rsaj_login_instance where account_key=? and device_id=?")) {
                q.setString(1, accountKey); q.setString(2, oldest); q.executeUpdate();
            }
            try (PreparedStatement q = prepare(c, "delete from rsaj_login_device where account_key=? and device_id=?")) {
                q.setString(1, accountKey); q.setString(2, oldest); q.executeUpdate();
            }
            devices.remove(oldest);
        }
        Ticket ticket = new Ticket(accountKey, deviceId, generation, randomToken());
        try (PreparedStatement q = prepare(c, "insert into rsaj_login_instance "
                + "(session_token,account_key,device_id,generation_token,expires_at) "
                + "values (?,?,?,?,timestampadd(second,?,current_timestamp))")) {
            bind(q, ticket); q.setInt(5, LEASE_SECONDS); q.executeUpdate();
        }
        return ticket;
    }

    private static final String CURRENT = "select i.expires_at<=timestampadd(second,60,current_timestamp) "
            + "from rsaj_login_instance i join rsaj_login_device d on d.account_key=i.account_key "
            + "and d.device_id=i.device_id and d.generation_token=i.generation_token "
            + "where i.session_token=? and i.account_key=? and i.device_id=? and i.generation_token=? "
            + "and i.expires_at>current_timestamp";

    static boolean current(Connection c, Ticket ticket, String accountKey, boolean lock) throws SQLException {
        if (ticket == null || !ticket.accountKey.equals(accountKey)) return false;
        // BUKA_SESI tetap memeriksa password/CAPTCHA melalui LoginAttemptGuard.
        if (!singleLoginEnabled(c)) return true;
        try (PreparedStatement q = prepare(c, CURRENT + (lock ? " for update" : ""))) {
            bind(q, ticket); try (ResultSet row = q.executeQuery()) { return row.next(); }
        }
    }

    /** SELECT tiap 2 detik; perpanjangan lease kira-kira tiap 30 detik per aplikasi. */
    public boolean isCurrent(Ticket ticket) throws SQLException {
        if (ticket == null) return false;
        boolean renew;
        try (Connection c = reads.open()) {
            if (c == null) throw new SQLException("Koneksi pemeriksaan sesi tidak tersedia.");
            boolean enabled = singleLoginEnabled(c);
            try (PreparedStatement q = prepare(c, CURRENT)) {
                bind(q, ticket);
                try (ResultSet row = q.executeQuery()) {
                    if (!row.next()) return !enabled;
                    renew = row.getBoolean(1);
                }
            }
        }
        if (!renew) return true;
        // Lease tetap diperbarui saat OFF supaya tiket aktif tidak kedaluwarsa
        // semata-mata karena fitur dimatikan. OFF tidak mencabut tiket pengguna.
        try (Connection c = writes.open()) {
            if (c == null) throw new SQLException("Koneksi perpanjangan sesi tidak tersedia.");
            try (PreparedStatement q = prepare(c, "update rsaj_login_instance "
                    + "set expires_at=timestampadd(second,90,current_timestamp) "
                    + "where session_token=? and account_key=? and device_id=? and generation_token=? "
                    + "and expires_at>current_timestamp")) {
                bind(q, ticket);
                q.executeUpdate();
            }
            return current(c, ticket, ticket.accountKey, false);
        }
    }

    /** Logout satu aplikasi tidak mencabut aplikasi lain di komputer yang sama. */
    public void release(Ticket ticket) throws SQLException {
        if (ticket == null) return;
        try (Connection c = writes.open()) {
            if (c == null) throw new SQLException("Koneksi penutupan sesi tidak tersedia.");
            c.setAutoCommit(false);
            try {
                try (PreparedStatement q = prepare(c, "select account_key from rsaj_login_security where account_key=? for update")) {
                    q.setString(1, ticket.accountKey); try (ResultSet ignored = q.executeQuery()) { while (ignored.next()) { } }
                }
                try (PreparedStatement q = prepare(c, "delete from rsaj_login_instance "
                        + "where session_token=? and account_key=? and device_id=? and generation_token=?")) {
                    bind(q, ticket); q.executeUpdate();
                }
                prune(c, ticket.accountKey);
                c.commit();
            } catch (SQLException | RuntimeException exception) {
                try { c.rollback(); } catch (SQLException failure) { exception.addSuppressed(failure); }
                throw exception;
            }
        }
    }

    private static void prune(Connection c, String accountKey) throws SQLException {
        try (PreparedStatement q = prepare(c, "delete from rsaj_login_instance where account_key=? and expires_at<=current_timestamp")) {
            q.setString(1, accountKey); q.executeUpdate();
        }
        try (PreparedStatement q = prepare(c, "delete d from rsaj_login_device d where d.account_key=? "
                + "and not exists (select 1 from rsaj_login_instance i where i.account_key=d.account_key "
                + "and i.device_id=d.device_id and i.generation_token=d.generation_token)")) {
            q.setString(1, accountKey); q.executeUpdate();
        }
    }
    private static void bind(PreparedStatement q, Ticket t) throws SQLException {
        q.setString(1, t.token); q.setString(2, t.accountKey); q.setString(3, t.deviceId); q.setString(4, t.generation);
    }
    private static PreparedStatement prepare(Connection c, String sql) throws SQLException {
        PreparedStatement q = c.prepareStatement(sql);
        try { q.setQueryTimeout(5); return q; } catch (SQLException exception) { q.close(); throw exception; }
    }
    private static String randomToken() {
        byte[] random = new byte[32]; RANDOM.nextBytes(random);
        StringBuilder value = new StringBuilder(64);
        for (byte part : random) value.append(String.format(java.util.Locale.ROOT, "%02x", part & 0xff));
        return value.toString();
    }
}
