package integration;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import widget.ButtonBig;

/** Membuka portal Pelaporan RL di browser melalui tiket sekali pakai. */
public final class RlLauncher {
    private static final SecureRandom RANDOM = new SecureRandom();

    private RlLauncher() {}

    public static ButtonBig buatTombol(Component parent, ButtonBig contoh) {
        ButtonBig tombol = new ButtonBig();
        tombol.setName("btnRlWeb");
        tombol.setText("Pelaporan RL 3-5 (Web)");
        tombol.setIcon(contoh.getIcon());
        tombol.setFont(contoh.getFont());
        tombol.setPreferredSize(contoh.getPreferredSize());
        tombol.setIconTextGap(contoh.getIconTextGap());
        tombol.setHorizontalTextPosition(contoh.getHorizontalTextPosition());
        tombol.setVerticalTextPosition(contoh.getVerticalTextPosition());
        tombol.addActionListener(event -> open(parent, tombol));
        return tombol;
    }

    public static boolean bolehBuka() {
        String actor = akses.getkode();
        return actor != null && !actor.trim().isEmpty() && actor.length() <= 80
                && (akses.getadmin() || akses.getrl4asebab());
    }

    private static void open(Component parent, ButtonBig item) {
        if (!bolehBuka()) {
            JOptionPane.showMessageDialog(parent, "Akun belum memiliki akses Menu Laporan RL.");
            return;
        }
        String actor = akses.getkode();
        boolean admin = akses.getadmin();
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            JOptionPane.showMessageDialog(parent, "Browser bawaan komputer belum tersedia.");
            return;
        }

        String host = koneksiDB.HOSTHYBRIDWEB();
        String port = koneksiDB.PORTWEB();
        String webapps = koneksiDB.HYBRIDWEB();
        item.setEnabled(false);
        new SwingWorker<URI, Void>() {
            @Override
            protected URI doInBackground() throws Exception {
                if (!actor.equals(akses.getkode()) || !bolehBuka()) {
                    throw new SecurityException("Pengguna telah berganti.");
                }
                byte[] random = new byte[32];
                RANDOM.nextBytes(random);
                String token = hex(random);
                URI target = address(host, port, webapps, token);
                String hash = hex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII)));
                try (Connection db = koneksiDB.condbTransaksi();
                     PreparedStatement query = db.prepareStatement(
                         "INSERT INTO rsaj_rl32_ticket(token_hash,actor_id,is_admin,expires_at) "
                         + "VALUES(?,?,?,DATE_ADD(UTC_TIMESTAMP(),INTERVAL 90 SECOND))")) {
                    query.setQueryTimeout(8);
                    query.setString(1, hash);
                    query.setString(2, actor);
                    query.setBoolean(3, admin);
                    query.executeUpdate();
                    if (!db.getAutoCommit()) {
                        db.commit();
                    }
                }
                return target;
            }

            @Override
            protected void done() {
                item.setEnabled(true);
                try {
                    URI target = get();
                    if (!actor.equals(akses.getkode()) || !bolehBuka()) {
                        throw new SecurityException("Pengguna telah berganti.");
                    }
                    Desktop.getDesktop().browse(target);
                } catch (Exception error) {
                    Throwable cause = error.getCause() == null ? error : error.getCause();
                    String message = "Portal RL belum dapat dibuka. Periksa konfigurasi webapps dan koneksi database.";
                    if (cause instanceof SQLException && ((SQLException) cause).getErrorCode() == 1146) {
                        message = "Tabel akses portal RL belum tersedia. Jalankan migrasi modul.";
                    } else if (cause instanceof SecurityException) {
                        message = cause.getMessage();
                    }
                    JOptionPane.showMessageDialog(parent, message);
                }
            }
        }.execute();
    }

    static URI address(String host, String port, String webapps, String token) throws Exception {
        URI base = new URI(host.contains("://") ? host : "http://" + host);
        if (!("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))
                || base.getHost() == null || base.getUserInfo() != null
                || !(base.getPath().isEmpty() || "/".equals(base.getPath()))
                || base.getQuery() != null || base.getFragment() != null) {
            throw new IllegalArgumentException("Host webapps tidak valid.");
        }
        int selectedPort = base.getPort() >= 0 ? base.getPort() : Integer.parseInt(port);
        String path = webapps.replaceAll("^/+|/+$", "");
        if (selectedPort < 1 || selectedPort > 65535 || path.contains("..")
                || !path.matches("[a-zA-Z0-9_.\\/-]*") || !token.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException("Port atau folder webapps tidak valid.");
        }
        return new URI(base.getScheme(), null, base.getHost(), selectedPort,
            "/" + (path.isEmpty() ? "" : path + "/") + "rl/index.php", "launch=1", "launch=" + token);
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 15, 16));
            result.append(Character.forDigit(value & 15, 16));
        }
        return result.toString();
    }
}
