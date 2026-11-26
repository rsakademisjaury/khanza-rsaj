package integration;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

/** Pintu masuk browser, tanpa password pada URL dan tanpa mengubah sesi Khanza. */
public final class FlowsheetIcuLauncher {
    private FlowsheetIcuLauncher() {}

    public static void pasangMenu(final Component parent, final JMenu menu, final JTable table) {
        final JMenuItem item = new JMenuItem("Flowsheet ICU (Web)");
        item.setFont(menu.getFont());
        item.setIcon(menu.getIcon());
        item.addActionListener(event -> {
            final String actor = akses.getkode();
            final boolean canRead = akses.getadmin() || akses.getkamar_inap() || akses.gettindakan_ranap();
            final boolean canWrite = akses.gettindakan_ranap();
            final boolean canMedicate = akses.getberi_obat();
            if (!canRead || actor == null || actor.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Akun belum memiliki akses rawat inap.");
                return;
            }
            int row = table.getSelectedRow();
            if (row < 0 || table.getValueAt(row, 0) == null || table.getValueAt(row, 0).toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Pilih baris pasien yang memuat No.Rawat terlebih dahulu.");
                return;
            }
            final String noRawat = table.getValueAt(row, 0).toString().trim();
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                JOptionPane.showMessageDialog(parent, "Browser bawaan komputer belum tersedia.");
                return;
            }
            final String host = koneksiDB.HOSTHYBRIDWEB();
            final String port = koneksiDB.PORTWEB();
            final String webapps = koneksiDB.HYBRIDWEB();
            item.setEnabled(false);
            new SwingWorker<URI, Void>() {
                @Override protected URI doInBackground() throws Exception {
                    if (!actor.equals(akses.getkode())) throw new SecurityException("Pengguna telah berganti.");
                    String ticket;
                    try (Connection db = koneksiDB.condbTransaksi()) {
                        ticket = issueTicket(db, noRawat, actor, canWrite, canMedicate);
                    }
                    return address(host, port, webapps, ticket);
                }
                @Override protected void done() {
                    item.setEnabled(true);
                    try {
                        URI uri = get();
                        if (!actor.equals(akses.getkode())) throw new SecurityException("Pengguna telah berganti. Buka kembali menu.");
                        Desktop.getDesktop().browse(uri);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        String message = "Flowsheet ICU belum dapat dibuka. Periksa pemasangan modul, migrasi tabel, dan sesi Khanza.";
                        if (cause instanceof SQLException && ((SQLException) cause).getErrorCode() == 1146)
                            message = "Tabel flowsheet belum tersedia. Jalankan database/001_flowsheet_icu.sql terlebih dahulu.";
                        if (cause instanceof SecurityException) message = cause.getMessage();
                        JOptionPane.showMessageDialog(parent, message);
                        System.err.println("Flowsheet ICU: " + cause.getClass().getSimpleName());
                    }
                }
            }.execute();
        });
        menu.addSeparator();
        menu.add(item);
    }

    static String issueTicket(Connection db, String noRawat, String actor, boolean write, boolean medicate) throws Exception {
        if (noRawat == null || noRawat.length() > 50 || actor == null || actor.length() > 80)
            throw new SecurityException("Identitas kunjungan atau petugas tidak valid.");
        db.setAutoCommit(false);
        try {
            try (PreparedStatement q = db.prepareStatement("SELECT rp.no_rawat FROM reg_periksa rp WHERE rp.no_rawat=? AND EXISTS(SELECT 1 FROM kamar_inap ki WHERE ki.no_rawat=rp.no_rawat)")) {
                q.setQueryTimeout(8); q.setString(1, noRawat);
                try (ResultSet r = q.executeQuery()) { if (!r.next()) throw new SecurityException("Kunjungan rawat inap tidak ditemukan."); }
            }
            String name = null, type = null;
            if ("Admin Utama".equals(actor)) { name = "Admin Utama"; type = "admin"; write = false; medicate = false; }
            if (name == null) {
                try (PreparedStatement q = db.prepareStatement("SELECT nm_dokter FROM dokter WHERE kd_dokter=?")) {
                    q.setQueryTimeout(8); q.setString(1, actor);
                    try (ResultSet r = q.executeQuery()) { if (r.next()) { name = r.getString(1); type = "doctor"; } }
                }
            }
            if (name == null) {
                try (PreparedStatement q = db.prepareStatement("SELECT nama FROM petugas WHERE nip=? AND status='1'")) {
                    q.setQueryTimeout(8); q.setString(1, actor);
                    try (ResultSet r = q.executeQuery()) { if (r.next()) { name = r.getString(1); type = "staff"; } }
                }
            }
            if (name == null || name.trim().isEmpty()) throw new SecurityException("Akun belum terpetakan sebagai dokter atau petugas aktif.");
            byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
            String token = hex(bytes);
            String hash = hex(MessageDigest.getInstance("SHA-256").digest(token.getBytes("UTF-8")));
            try (PreparedStatement q = db.prepareStatement("INSERT INTO rsaj_icu_ticket(token_hash,no_rawat,actor_id,actor_name,actor_type,can_write,can_medicate,issued_at,expires_at) VALUES(?,?,?,?,?,?,?,UTC_TIMESTAMP(),DATE_ADD(UTC_TIMESTAMP(),INTERVAL 90 SECOND))")) {
                q.setQueryTimeout(8); q.setString(1, hash); q.setString(2, noRawat); q.setString(3, actor);
                q.setString(4, name); q.setString(5, type); q.setInt(6, write ? 1 : 0); q.setInt(7, medicate ? 1 : 0); q.executeUpdate();
            }
            try (PreparedStatement q = db.prepareStatement("DELETE FROM rsaj_icu_ticket WHERE expires_at<DATE_SUB(UTC_TIMESTAMP(),INTERVAL 1 DAY) LIMIT 100")) {
                q.setQueryTimeout(8); q.executeUpdate();
            }
            db.commit(); return token;
        } catch (Exception ex) { db.rollback(); throw ex; }
    }

    static URI address(String host, String port, String webapps, String token) throws Exception {
        if (host == null || webapps == null || !token.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Alamat webapps tidak valid.");
        URI base = new URI(host.contains("://") ? host : "http://" + host);
        if (!"http".equals(base.getScheme()) && !"https".equals(base.getScheme())) throw new IllegalArgumentException("Protokol tidak didukung.");
        if (base.getHost() == null || base.getUserInfo() != null || base.getQuery() != null || base.getFragment() != null)
            throw new IllegalArgumentException("Host webapps tidak valid.");
        int selectedPort = base.getPort() >= 0 ? base.getPort() : Integer.parseInt(port);
        if (selectedPort < 1 || selectedPort > 65535) throw new IllegalArgumentException("Port webapps tidak valid.");
        String path = webapps.replaceAll("^/+|/+$", "");
        if (path.contains("..") || path.contains("?") || path.contains("#")) throw new IllegalArgumentException("Folder webapps tidak valid.");
        return new URI(base.getScheme(), null, base.getHost(), selectedPort,
                "/" + (path.isEmpty() ? "" : path + "/") + "flowsheet_icu/index.php", null, "launch=" + token);
    }
    private static String hex(byte[] bytes) {
        StringBuilder s = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) s.append(String.format("%02x", b & 0xff));
        return s.toString();
    }
}
