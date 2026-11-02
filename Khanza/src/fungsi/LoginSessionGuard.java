package fungsi;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Saat OFF tidak menggunakan identitas komputer, registry, atau heartbeat sesi. */
public final class LoginSessionGuard {
    @FunctionalInterface
    public interface ConnectionFactory { Connection open() throws SQLException; }

    private static final class Registration {
        final String deviceId, generation, token;
        Registration(String deviceId, String generation, String token) {
            this.deviceId = deviceId; this.generation = generation; this.token = token;
        }
    }
    public static final class Ticket {
        private final String accountKey, username;
        private final LoginDeviceIdentity.Provider identity;
        private volatile Registration registration;
        private volatile boolean released;
        private Ticket(String accountKey, String username, LoginDeviceIdentity.Provider identity, Registration registration) {
            this.accountKey = accountKey; this.username = username;
            this.identity = identity; this.registration = registration;
        }
        private Ticket(String accountKey, String deviceId, String generation, String token) {
            this(accountKey, null, () -> deviceId, new Registration(deviceId, generation, token));
        }
    }
    private static final int LEASE_SECONDS = 90;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ConnectionFactory writes, reads;
    public LoginSessionGuard(ConnectionFactory writes, ConnectionFactory reads) {
        if (writes == null || reads == null) throw new IllegalArgumentException("Koneksi sesi wajib tersedia.");
        this.writes = writes; this.reads = reads;
    }

    /** Gangguan koneksi/izin atau nilai rusak tidak diartikan sebagai OFF. */
    private static boolean singleLoginEnabled(Connection c) throws SQLException {
        try (PreparedStatement q = prepare(c, "select status from rsaj_login_session_setting where id=1");
             ResultSet row = q.executeQuery()) {
            if (!row.next()) return true;
            String status = row.getString(1);
            if ("0".equals(status)) return false;
            if ("1".equals(status)) return true;
            throw new SQLException("Status single login harus 0 atau 1.");
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1146 && "42S02".equals(exception.getSQLState())) return true;
            throw exception;
        }
    }

    // Autentikasi telah mengunci baris rsaj_login_security akun yang sama.
    static Ticket claim(Connection c, String accountKey, String username, String deviceId) throws SQLException {
        return claimWithIdentity(c, accountKey, username, () -> deviceId);
    }

    /** Baca sakelar SEBELUM memanggil provider ID, termasuk saat folder ID tidak bisa ditulis. */
    static Ticket claimWithIdentity(Connection c, String accountKey, String username,
            LoginDeviceIdentity.Provider identity) throws SQLException {
        if (identity == null) throw new IllegalArgumentException("Penyedia identitas komputer wajib tersedia.");
        if (!singleLoginEnabled(c)) return new Ticket(accountKey, username, identity, (Registration) null);
        return claimEnabled(c, accountKey, username, identity);
    }

    private static Ticket claimEnabled(Connection c, String accountKey, String username,
            LoginDeviceIdentity.Provider identity) throws SQLException {
        String deviceId = identity.id();
        if (deviceId == null || !deviceId.matches("[0-9a-f]{64}")) throw new SQLException("Identitas komputer tidak valid.");
        int maximum = 1;
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
        Registration registration = new Registration(deviceId, generation, randomToken());
        try (PreparedStatement q = prepare(c, "insert into rsaj_login_instance "
                + "(session_token,account_key,device_id,generation_token,expires_at) "
                + "values (?,?,?,?,timestampadd(second,?,current_timestamp))")) {
            bind(q, accountKey, registration); q.setInt(5, LEASE_SECONDS); q.executeUpdate();
        }
        return new Ticket(accountKey, username, identity, registration);
    }

    private static final String CURRENT = "select i.expires_at<=timestampadd(second,60,current_timestamp) "
            + "from rsaj_login_instance i join rsaj_login_device d on d.account_key=i.account_key "
            + "and d.device_id=i.device_id and d.generation_token=i.generation_token "
            + "where i.session_token=? and i.account_key=? and i.device_id=? and i.generation_token=? "
            + "and i.expires_at>current_timestamp";

    static boolean current(Connection c, Ticket ticket, String accountKey, boolean lock) throws SQLException {
        if (ticket == null || ticket.released || !ticket.accountKey.equals(accountKey)) return false;
        if (!singleLoginEnabled(c)) return true;
        Registration registration = ticket.registration;
        // Tiket lokal OFF berasal dari login yang sudah sah. Pendaftaran ON
        // disiapkan sesudah password benar dan baru diadopsi setelah commit.
        if (registration == null) return ticket.username != null && ticket.identity != null;
        return registeredCurrent(c, accountKey, registration, lock);
    }

    static Ticket prepareResume(Connection c, Ticket ticket, String accountKey, String username,
            LoginDeviceIdentity.Provider identity) throws SQLException {
        if (ticket == null || ticket.released || !ticket.accountKey.equals(accountKey)) {
            throw new SQLException("Sesi pembukaan login tidak valid.");
        }
        if (!singleLoginEnabled(c)) return new Ticket(accountKey, username, identity, (Registration) null);
        Registration registration = ticket.registration;
        if (registration != null && registeredCurrent(c, accountKey, registration, true)) return ticket;
        return claimEnabled(c, accountKey, username, identity);
    }

    /** Tidak memublikasikan hasil registrasi transaksi yang kemudian rollback. */
    static Ticket adoptAfterCommit(Ticket original, Ticket prepared) {
        if (original == null || original == prepared) return prepared;
        if (!original.accountKey.equals(prepared.accountKey)) throw new IllegalArgumentException("Akun sesi tidak sesuai.");
        synchronized (original) {
            if (!original.released) original.registration = prepared.registration;
        }
        return original;
    }

    private static boolean registeredCurrent(Connection c, String accountKey, Registration registration,
            boolean lock) throws SQLException {
        try (PreparedStatement q = prepare(c, CURRENT + (lock ? " for update" : ""))) {
            bind(q, accountKey, registration);
            try (ResultSet row = q.executeQuery()) { return row.next(); }
        }
    }

    /** OFF hanya membaca sakelar; tidak menyentuh registry/heartbeat perangkat. */
    public boolean isCurrent(Ticket ticket) throws SQLException {
        if (ticket == null || ticket.released) return false;
        Registration registration;
        boolean renew = false;
        try (Connection c = reads.open()) {
            if (c == null) throw new SQLException("Koneksi pemeriksaan sesi tidak tersedia.");
            if (!singleLoginEnabled(c)) return disableRegistration(ticket);
            registration = ticket.registration;
            if (registration != null) {
                try (PreparedStatement q = prepare(c, CURRENT)) {
                    bind(q, ticket.accountKey, registration);
                    try (ResultSet row = q.executeQuery()) {
                        if (!row.next()) {
                            if (!singleLoginEnabled(c)) return disableRegistration(ticket);
                            return false;
                        }
                        renew = row.getBoolean(1);
                    }
                }
            }
        }
        if (registration == null) return activateRegistration(ticket);
        if (!renew) return !ticket.released;
        try (Connection c = writes.open()) {
            if (c == null) throw new SQLException("Koneksi perpanjangan sesi tidak tersedia.");
            if (!singleLoginEnabled(c)) return disableRegistration(ticket);
            try (PreparedStatement q = prepare(c, "update rsaj_login_instance "
                    + "set expires_at=timestampadd(second,90,current_timestamp) "
                    + "where session_token=? and account_key=? and device_id=? and generation_token=? "
                    + "and expires_at>current_timestamp")) {
                bind(q, ticket.accountKey, registration); q.executeUpdate();
            }
            if (ticket.released) return false;
            if (ticket.registration != registration) return true;
            return registeredCurrent(c, ticket.accountKey, registration, false);
        }
    }

    private static boolean disableRegistration(Ticket ticket) {
        synchronized (ticket) {
            if (ticket.released) return false;
            ticket.registration = null;
            return true;
        }
    }

    /** OFF -> ON: worker mendaftarkan ulang sesi sah dengan row lock akun. */
    private boolean activateRegistration(Ticket ticket) throws SQLException {
        try (Connection c = writes.open()) {
            if (c == null) throw new SQLException("Koneksi aktivasi sesi tidak tersedia.");
            c.setAutoCommit(false);
            try {
                lockAccount(c, ticket.accountKey);
                if (ticket.released) { c.rollback(); return false; }
                if (!singleLoginEnabled(c)) { c.commit(); return disableRegistration(ticket); }
                Registration already = ticket.registration;
                if (already != null) {
                    boolean valid = registeredCurrent(c, ticket.accountKey, already, true);
                    c.commit();
                    return valid && !ticket.released;
                }
                if (ticket.identity == null || ticket.username == null) throw new SQLException("Identitas sesi belum tersedia.");
                Ticket prepared = claimEnabled(c, ticket.accountKey, ticket.username, ticket.identity);
                c.commit();
                synchronized (ticket) {
                    if (ticket.released) return false;
                    if (ticket.registration == null) ticket.registration = prepared.registration;
                }
                return true;
            } catch (SQLException | RuntimeException exception) {
                try { c.rollback(); } catch (SQLException failure) { exception.addSuppressed(failure); }
                throw exception;
            }
        }
    }

    /** Logout tiket OFF tidak membuka koneksi registry sesi. */
    public void release(Ticket ticket) throws SQLException {
        if (ticket == null) return;
        Registration registration;
        synchronized (ticket) {
            if (ticket.released) return;
            ticket.released = true;
            registration = ticket.registration;
            ticket.registration = null;
        }
        if (registration == null) return;
        try (Connection c = writes.open()) {
            if (c == null) throw new SQLException("Koneksi penutupan sesi tidak tersedia.");
            // Admin dapat mematikan fitur sebelum poll berikutnya mengubah tiket.
            if (!singleLoginEnabled(c)) return;
            c.setAutoCommit(false);
            try {
                lockAccount(c, ticket.accountKey);
                try (PreparedStatement q = prepare(c, "delete from rsaj_login_instance "
                        + "where session_token=? and account_key=? and device_id=? and generation_token=?")) {
                    bind(q, ticket.accountKey, registration); q.executeUpdate();
                }
                prune(c, ticket.accountKey);
                c.commit();
            } catch (SQLException | RuntimeException exception) {
                try { c.rollback(); } catch (SQLException failure) { exception.addSuppressed(failure); }
                throw exception;
            }
        }
    }

    private static void lockAccount(Connection c, String accountKey) throws SQLException {
        try (PreparedStatement q = prepare(c, "select account_key from rsaj_login_security where account_key=? for update")) {
            q.setString(1, accountKey);
            try (ResultSet row = q.executeQuery()) {
                if (!row.next()) throw new SQLException("Status pengaman login tidak ditemukan.");
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
    private static void bind(PreparedStatement q, String accountKey, Registration registration) throws SQLException {
        q.setString(1, registration.token); q.setString(2, accountKey);
        q.setString(3, registration.deviceId); q.setString(4, registration.generation);
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
