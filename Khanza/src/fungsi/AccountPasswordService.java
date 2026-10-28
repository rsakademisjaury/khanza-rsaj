package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Penulisan kredensial berparameter; tidak membaca/dekripsi password lama. */
public final class AccountPasswordService {
    @FunctionalInterface
    public interface ConnectionFactory { Connection open() throws SQLException; }
    public enum Account {
        USER("user", "id_user", "password"), ADMIN("admin", "usere", "passworde");
        final String table, id, password;
        Account(String table, String id, String password) {
            this.table = table; this.id = id; this.password = password;
        }
    }
    private final ConnectionFactory connections;
    public AccountPasswordService(ConnectionFactory connections) {
        if (connections == null) throw new IllegalArgumentException("Koneksi wajib tersedia.");
        this.connections = connections;
    }

    /** Defaults adalah daftar flag false statis dari form, bukan input pengguna. */
    public boolean createUser(String username, char[] password, String defaults) throws SQLException {
        PasswordPolicy.require(password); requireUsername(username);
        if (defaults == null || defaults.isEmpty()) throw new IllegalArgumentException("Daftar hak akses awal tidak valid.");
        for (String flag : defaults.split(",", -1)) {
            if (!"'false'".equals(flag.trim())) throw new IllegalArgumentException("Daftar hak akses awal tidak valid.");
        }
        return execute("insert into user values(AES_ENCRYPT(?,'nur'),AES_ENCRYPT(?,'windi')," + defaults + ")",
                username, new String(password)) == 1;
    }

    /** Kosong hanya berarti pertahankan password saat mengedit akun yang ada. */
    public boolean editUser(String original, String username, char[] password, String permissions) throws SQLException {
        requireUsername(original); requireUsername(username);
        if (password == null) throw new IllegalArgumentException("Password belum tersedia.");
        if (password.length > 0) PasswordPolicy.require(password);
        if (permissions == null || permissions.isEmpty()) throw new IllegalArgumentException("Daftar hak akses tidak valid.");
        for (String entry : permissions.split(",", -1)) {
            String flag = entry.trim();
            if (!flag.matches("[a-zA-Z0-9_]+='(?:true|false)'")) throw new IllegalArgumentException("Daftar hak akses tidak valid.");
            String column = flag.substring(0, flag.indexOf('='));
            if ("password".equalsIgnoreCase(column) || "id_user".equalsIgnoreCase(column)) {
                throw new IllegalArgumentException("Kredensial tidak boleh disalin sebagai hak akses.");
            }
        }
        String sql = "update user set id_user=AES_ENCRYPT(?,'nur'),"
                + (password.length == 0 ? "" : "password=AES_ENCRYPT(?,'windi'),")
                + permissions + " where id_user=AES_ENCRYPT(?,'nur')";
        try (Connection c = open()) {
            try (PreparedStatement q = prepare(c, sql)) {
                int p = 1; q.setString(p++, username);
                if (password.length > 0) q.setString(p++, new String(password));
                q.setString(p, original);
                return q.executeUpdate() > 0 || (original.equals(username) && exists(c, Account.USER, username));
            }
        }
    }

    public boolean createAdmin(String username, char[] password) throws SQLException {
        requireUsername(username); PasswordPolicy.require(password);
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement q = prepare(c, "select usere from admin for update"); ResultSet row = q.executeQuery()) {
                    if (row.next()) throw new SQLException("Admin utama sudah tersedia.", "23000");
                }
                try (PreparedStatement q = prepare(c, "insert into admin values(AES_ENCRYPT(?,'nur'),AES_ENCRYPT(?,'windi'))")) {
                    q.setString(1, username); q.setString(2, new String(password));
                    if (q.executeUpdate() != 1) throw new SQLException("Admin belum tersimpan.");
                }
                c.commit(); return true;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException failure) { e.addSuppressed(failure); }
                throw e;
            }
        }
    }

    /** UPDATE langsung: kegagalan simpan tidak menghapus akun admin. */
    public boolean editAdmin(String original, String username, char[] password) throws SQLException {
        requireUsername(original); requireUsername(username);
        if (password == null) throw new IllegalArgumentException("Password belum tersedia.");
        if (password.length > 0) PasswordPolicy.require(password);
        try (Connection c = open(); PreparedStatement q = prepare(c, "update admin set usere=AES_ENCRYPT(?,'nur')"
                + (password.length == 0 ? "" : ",passworde=AES_ENCRYPT(?,'windi')")
                + " where usere=AES_ENCRYPT(?,'nur')")) {
            int p = 1; q.setString(p++, username);
            if (password.length > 0) q.setString(p++, new String(password));
            q.setString(p, original);
            return q.executeUpdate() > 0 || (original.equals(username) && exists(c, Account.ADMIN, username));
        }
    }

    public boolean reset(Account account, String username, char[] password) throws SQLException {
        if (account == null) throw new IllegalArgumentException("Jenis akun wajib tersedia.");
        requireUsername(username); PasswordPolicy.require(password);
        try (Connection c = open(); PreparedStatement q = prepare(c, "update " + account.table + " set "
                + account.password + "=AES_ENCRYPT(?,'windi') where " + account.id + "=AES_ENCRYPT(?,'nur')")) {
            q.setString(1, new String(password)); q.setString(2, username);
            return q.executeUpdate() > 0 || matches(c, account, username, password);
        }
    }

    /** Password lama diperiksa terhadap DB sekarang, bukan salinan saat dialog dibuka. */
    public boolean changeOwnUser(String username, char[] oldPassword, char[] newPassword) throws SQLException {
        return changeOwn(Account.USER, username, oldPassword, newPassword);
    }

    public boolean changeOwn(Account account, String username, char[] oldPassword, char[] newPassword) throws SQLException {
        if (account == null) throw new IllegalArgumentException("Jenis akun wajib tersedia.");
        requireUsername(username); PasswordPolicy.require(newPassword);
        if (oldPassword == null || oldPassword.length == 0) return false;
        try (Connection c = open(); PreparedStatement q = prepare(c,
                "update " + account.table + " set " + account.password + "=AES_ENCRYPT(?,'windi') where "
                + account.id + "=AES_ENCRYPT(?,'nur') and " + account.password + "=AES_ENCRYPT(?,'windi')")) {
            q.setString(1, new String(newPassword)); q.setString(2, username); q.setString(3, new String(oldPassword));
            return q.executeUpdate() > 0 || (PasswordPolicy.matches(oldPassword, newPassword)
                    && matches(c, account, username, oldPassword));
        }
    }

    private int execute(String sql, String... values) throws SQLException {
        try (Connection c = open(); PreparedStatement q = prepare(c, sql)) {
            for (int i = 0; i < values.length; i++) q.setString(i + 1, values[i]);
            return q.executeUpdate();
        }
    }
    private static boolean exists(Connection c, Account account, String username) throws SQLException {
        try (PreparedStatement q = prepare(c, "select 1 from " + account.table + " where " + account.id + "=AES_ENCRYPT(?,'nur')")) {
            q.setString(1, username); try (ResultSet row = q.executeQuery()) { return row.next(); }
        }
    }
    private static boolean matches(Connection c, Account account, String username, char[] password) throws SQLException {
        try (PreparedStatement q = prepare(c, "select 1 from " + account.table + " where " + account.id
                + "=AES_ENCRYPT(?,'nur') and " + account.password + "=AES_ENCRYPT(?,'windi')")) {
            q.setString(1, username); q.setString(2, new String(password));
            try (ResultSet row = q.executeQuery()) { return row.next(); }
        }
    }
    private Connection open() throws SQLException {
        Connection c = connections.open();
        if (c == null) throw new SQLException("Koneksi password belum tersedia.");
        return c;
    }
    private static PreparedStatement prepare(Connection c, String sql) throws SQLException {
        PreparedStatement q = c.prepareStatement(sql);
        try { q.setQueryTimeout(5); return q; } catch (SQLException e) { q.close(); throw e; }
    }
    private static void requireUsername(String username) {
        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("Username wajib diisi.");
    }
}
