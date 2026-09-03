package khanzahmsservicefinger;

import fungsi.BPJSApiFinger;
import fungsi.BPJSApiFinger.HasilFinger;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service console Pantau Finger BPJS.
 * Hanya memproses pasien yang lebih dahulu dimasukkan oleh tombol DlgReg
 * ke tabel bpjs_finger_monitor; tidak menyisir seluruh pasien secara massal.
 */
public class FingerBPJSConsoleService {
    private static final long LOOP_DELAY_MS = 3000L;       // membaca antrean lokal
    private static final int MAX_ATTEMPT_PER_SESSION = 4;   // maksimal hit setelah satu klik Pantau
    private static final int MAX_HIT_HARIAN = 1000;         // circuit breaker internal
    private static final String SERVICE_LOCK = "rsaj_service_finger_bpjs";

    private final BPJSApiFinger api = new BPJSApiFinger();
    private final SimpleDateFormat waktu = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Connection koneksi;
    private long terakhirLogBatas = 0L;
    private long terakhirLogKonfigurasi = 0L;

    public static void main(String[] args) {
        new FingerBPJSConsoleService().start();
    }

    public void start() {
        banner();
        try {
            koneksi = koneksiDB.condb();
            if (koneksi == null) {
                log("ERROR", "Koneksi database gagal. Service dihentikan.");
                return;
            }
            if (!ambilLockService()) {
                log("ERROR", "Service Finger BPJS lain sudah berjalan. Instance ini dihentikan.");
                return;
            }
            pulihkanAntrianProcessing();
            log("INFO", "Service aktif. Hanya antrean dari tombol Pantau Finger BPJS yang diproses.");
            log("INFO", "Retry per sesi: maksimum " + MAX_ATTEMPT_PER_SESSION + " hit; circuit breaker harian: " + MAX_HIT_HARIAN + " hit.");
            log("INFO", "Tekan CTRL+C untuk menghentikan service.");
            garis();
            while (true) {
                try {
                    tutupAntrianYangSudahSelesai();
                    cobaSimpanFingerValidPending();
                    if (jumlahHitHariIni() >= MAX_HIT_HARIAN) {
                        long now = System.currentTimeMillis();
                        if (now - terakhirLogBatas > 60000L) {
                            log("STOP", "Batas hit harian tercapai. Service menahan request BPJS sampai hari berikutnya.");
                            terakhirLogBatas = now;
                        }
                    } else if (konfigurasiSiap()) {
                        prosesSatuAntrianDue();
                    }
                    tidur(LOOP_DELAY_MS);
                } catch (Exception e) {
                    log("ERROR", "Loop service bermasalah: " + aman(e.getMessage()));
                    tidur(5000L);
                }
            }
        } catch (Exception e) {
            log("ERROR", "Service gagal dijalankan: " + aman(e.getMessage()));
        }
    }

    private boolean konfigurasiSiap() {
        String masalah = api.validasiKonfigurasi();
        if (masalah == null || masalah.trim().isEmpty()) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - terakhirLogKonfigurasi > 60000L) {
            log("ERROR", masalah);
            log("INFO", "Antrean ditahan. Tidak ada request BPJS dan attempt_count tidak ditambah sampai konfigurasi lengkap.");
            terakhirLogKonfigurasi = now;
        }
        return false;
    }

    private void prosesSatuAntrianDue() {
        Antrian a = ambilAntrianDue();
        if (a == null) return;
        if (!tandaiProcessing(a.id)) return;
        int percobaan = a.attemptCount + 1;
        Long logHitId = catatRencanaHit(a, percobaan);
        if (logHitId == null) {
            jadwalkanUlang(a.id, percobaan, "ERROR", "Log hit gagal disimpan; request BPJS dibatalkan demi keamanan");
            log("ERROR", "Request BPJS dibatalkan karena audit log tidak dapat ditulis: " + a.noRawat);
            return;
        }
        log("SYNC", "Cek finger no_rawat=" + a.noRawat + " kartu=" + masker(a.noKartu) + " percobaan=" + percobaan + "/" + MAX_ATTEMPT_PER_SESSION);
        HasilFinger hasil = api.cekFinger(a.noKartu, a.tanggalPelayanan);
        catatHasilHit(logHitId, hasil);
        if (hasil.sudahFinger()) {
            try {
                simpanSidikJari(a.noRawat);
                selesaiSukses(a.id, percobaan, ringkas(hasil.pesan));
                log("OK", "Finger valid, data tersimpan ke sidik_jari_bpjs: " + a.noRawat);
            } catch (Exception e) {
                tandaiValidMenungguSimpan(a.id, percobaan, ringkas(hasil.pesan + " | gagal simpan lokal: " + aman(e.getMessage())));
                log("ERROR", "Finger BPJS valid tetapi gagal disimpan lokal; akan retry simpan tanpa hit BPJS baru: " + a.noRawat);
            }
            return;
        }
        if (!hasil.requestValid) {
            jadwalkanUlang(a.id, percobaan, "ERROR", ringkas(hasil.metadataMessage));
            log("WARN", "Request BPJS gagal/tidak valid untuk " + a.noRawat + ": " + ringkas(hasil.metadataMessage));
        } else {
            jadwalkanUlang(a.id, percobaan, "NOT_FINGERED", ringkas(hasil.pesan));
            log("INFO", "Belum terbaca finger untuk " + a.noRawat + ": " + ringkas(hasil.pesan));
        }
    }

    private Antrian ambilAntrianDue() {
        String sql = "SELECT id,no_rawat,no_kartu,DATE_FORMAT(tgl_pelayanan,'%Y-%m-%d') AS tgl_pelayanan,attempt_count "
                + "FROM bpjs_finger_monitor m "
                + "WHERE m.status IN ('ACTIVE','NOT_FINGERED','ERROR') "
                + "AND m.next_check_at IS NOT NULL AND m.next_check_at<=NOW() "
                + "AND m.attempt_count<? "
                + "AND NOT EXISTS (SELECT 1 FROM sidik_jari_bpjs s WHERE s.no_rawat=m.no_rawat) "
                + "AND NOT EXISTS (SELECT 1 FROM bridging_sep b WHERE b.no_rawat=m.no_rawat AND IFNULL(b.no_sep,'')<>'') "
                + "ORDER BY m.next_check_at,m.id LIMIT 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setInt(1, MAX_ATTEMPT_PER_SESSION);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Antrian(rs.getLong("id"), rs.getString("no_rawat"), rs.getString("no_kartu"),
                            rs.getString("tgl_pelayanan"), rs.getInt("attempt_count"));
                }
            }
        } catch (Exception e) {
            log("ERROR", "Gagal membaca antrean: " + aman(e.getMessage()));
        }
        return null;
    }

    private boolean tandaiProcessing(long id) {
        String sql = "UPDATE bpjs_finger_monitor SET status='PROCESSING', attempt_count=attempt_count+1, "
                + "last_checked_at=NOW(), updated_at=NOW() "
                + "WHERE id=? AND status IN ('ACTIVE','NOT_FINGERED','ERROR') AND next_check_at<=NOW()";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            log("ERROR", "Gagal lock antrean " + id + ": " + aman(e.getMessage()));
            return false;
        }
    }

    private void simpanSidikJari(String noRawat) throws Exception {
        try (PreparedStatement update = koneksi.prepareStatement("UPDATE sidik_jari_bpjs SET validasi=NOW() WHERE no_rawat=?")) {
            update.setString(1, noRawat);
            if (update.executeUpdate() == 0) {
                try (PreparedStatement insert = koneksi.prepareStatement("INSERT INTO sidik_jari_bpjs(no_rawat,validasi) VALUES(?,NOW())")) {
                    insert.setString(1, noRawat);
                    insert.executeUpdate();
                }
            }
        }
    }

    private void selesaiSukses(long id, int attempt, String pesan) {
        updateStatus(id, "SUCCESS", attempt, null, "1", pesan, true);
    }

    private void tandaiValidMenungguSimpan(long id, int attempt, String pesan) {
        updateStatus(id, "FINGER_VALID_PENDING_SAVE", attempt, 10, "1", pesan, false);
    }

    private void cobaSimpanFingerValidPending() {
        String sql = "SELECT id,no_rawat,attempt_count FROM bpjs_finger_monitor "
                + "WHERE status='FINGER_VALID_PENDING_SAVE' AND next_check_at<=NOW() ORDER BY next_check_at,id LIMIT 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long id = rs.getLong("id");
                String noRawat = rs.getString("no_rawat");
                int attempt = rs.getInt("attempt_count");
                try {
                    simpanSidikJari(noRawat);
                    selesaiSukses(id, attempt, "Finger BPJS valid; simpan lokal berhasil setelah retry internal");
                    log("OK", "Retry simpan lokal sukses tanpa hit BPJS baru: " + noRawat);
                } catch (Exception e) {
                    tandaiValidMenungguSimpan(id, attempt, ringkas("Retry simpan lokal gagal: " + aman(e.getMessage())));
                    log("ERROR", "Retry simpan lokal masih gagal tanpa hit BPJS: " + noRawat);
                }
            }
        } catch (Exception e) {
            log("WARN", "Gagal membaca antrian pending simpan lokal: " + aman(e.getMessage()));
        }
    }

    private void jadwalkanUlang(long id, int attempt, String status, String pesan) {
        if (attempt >= MAX_ATTEMPT_PER_SESSION) {
            updateStatus(id, "STOPPED", attempt, null, status, "Batas percobaan tercapai. " + pesan, true);
            log("STOP", "Antrean berhenti setelah " + attempt + " hit. User dapat klik Pantau Finger kembali bila pasien siap.");
            return;
        }
        int jedaDetik = attempt == 1 ? 30 : (attempt == 2 ? 60 : 120);
        updateStatus(id, status, attempt, jedaDetik, status, pesan, false);
    }

    private void updateStatus(long id, String status, int attempt, Integer jedaDetik,
            String responseCode, String message, boolean completed) {
        String next = jedaDetik == null ? "NULL" : "DATE_ADD(NOW(), INTERVAL " + jedaDetik + " SECOND)";
        String done = completed ? "NOW()" : "NULL";
        String sql = "UPDATE bpjs_finger_monitor SET status=?,attempt_count=?,next_check_at=" + next
                + ",response_code=?,response_message=?,completed_at=" + done + ",updated_at=NOW() WHERE id=?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, attempt);
            ps.setString(3, responseCode);
            ps.setString(4, potong(message, 255));
            ps.setLong(5, id);
            ps.executeUpdate();
        } catch (Exception e) {
            log("ERROR", "Gagal memperbarui status antrean " + id + ": " + aman(e.getMessage()));
        }
    }

    private void tutupAntrianYangSudahSelesai() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE bpjs_finger_monitor m INNER JOIN sidik_jari_bpjs s ON s.no_rawat=m.no_rawat "
                + "SET m.status='SUCCESS',m.completed_at=IFNULL(m.completed_at,NOW()),m.next_check_at=NULL,m.updated_at=NOW() "
                + "WHERE m.status IN ('ACTIVE','NOT_FINGERED','ERROR','PROCESSING')")) {
            ps.executeUpdate();
        } catch (Exception e) {
            log("WARN", "Gagal sinkron status sukses lokal: " + aman(e.getMessage()));
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE bpjs_finger_monitor m INNER JOIN bridging_sep b ON b.no_rawat=m.no_rawat "
                + "SET m.status='CLOSED_SEP',m.completed_at=IFNULL(m.completed_at,NOW()),m.next_check_at=NULL,m.updated_at=NOW() "
                + "WHERE m.status IN ('ACTIVE','NOT_FINGERED','ERROR','PROCESSING') AND IFNULL(b.no_sep,'')<>''")) {
            ps.executeUpdate();
        } catch (Exception e) {
            log("WARN", "Gagal menutup antrean yang sudah memiliki SEP: " + aman(e.getMessage()));
        }
    }

    private void pulihkanAntrianProcessing() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE bpjs_finger_monitor SET status='ERROR',next_check_at=NOW(),response_message='Dipulihkan setelah service berhenti',updated_at=NOW() "
                + "WHERE status='PROCESSING' AND (last_checked_at IS NULL OR last_checked_at<DATE_SUB(NOW(), INTERVAL 5 MINUTE))")) {
            int n = ps.executeUpdate();
            if (n > 0) log("INFO", "Antrean PROCESSING lama yang dipulihkan: " + n);
        } catch (Exception e) {
            log("WARN", "Pemulihan antrean PROCESSING gagal: " + aman(e.getMessage()));
        }
    }

    private Long catatRencanaHit(Antrian a, int percobaan) {
        String sql = "INSERT INTO bpjs_finger_hit_log(monitor_id,no_rawat,no_kartu_masked,tgl_pelayanan,attempt_no,requested_at,result_status) "
                + "VALUES(?,?,?,?,?,NOW(),'REQUESTING')";
        try (PreparedStatement ps = koneksi.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, a.id);
            ps.setString(2, a.noRawat);
            ps.setString(3, masker(a.noKartu));
            ps.setString(4, a.tanggalPelayanan);
            ps.setInt(5, percobaan);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        } catch (Exception e) {
            log("ERROR", "Gagal membuat audit log request: " + aman(e.getMessage()));
            return null;
        }
    }

    private void catatHasilHit(Long id, HasilFinger hasil) {
        String status = hasil.sudahFinger() ? "SUCCESS" : (hasil.requestValid ? "NOT_FINGERED" : "ERROR");
        String code = hasil.sudahFinger() ? hasil.kodeFinger : (hasil.requestValid ? hasil.kodeFinger : hasil.metadataCode);
        String message = hasil.requestValid ? hasil.pesan : hasil.metadataMessage;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE bpjs_finger_hit_log SET completed_at=NOW(),result_status=?,response_code=?,response_message=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setString(2, code);
            ps.setString(3, potong(ringkas(message), 255));
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (Exception e) {
            log("WARN", "Request selesai tetapi pembaruan audit log gagal: " + aman(e.getMessage()));
        }
    }

    private int jumlahHitHariIni() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "SELECT COUNT(*) FROM bpjs_finger_hit_log WHERE DATE(requested_at)=CURDATE()" );
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            log("WARN", "Gagal menghitung hit harian dari audit log: " + aman(e.getMessage()));
            return MAX_HIT_HARIAN;
        }
    }

    private boolean ambilLockService() {
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT GET_LOCK(?,0)");) {
            ps.setString(1, SERVICE_LOCK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (Exception e) {
            log("ERROR", "Gagal mendapatkan lock service: " + aman(e.getMessage()));
            return false;
        }
    }

    private String masker(String kartu) {
        if (kartu == null || kartu.length() < 4) return "****";
        return "********" + kartu.substring(kartu.length() - 4);
    }
    private String ringkas(String text) { return potong(aman(text), 160); }
    private String potong(String text, int max) { return text == null ? "" : (text.length() > max ? text.substring(0, max) : text); }
    private String aman(String text) { return text == null ? "" : text.replace('\r', ' ').replace('\n', ' '); }
    private void tidur(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
    private void log(String level, String pesan) { System.out.println("[" + waktu.format(new Date()) + "] [" + level + "] " + pesan); }
    private void garis() { System.out.println("===================================================================="); }
    private void banner() {
        garis();
        System.out.println("           SIMKES KHANZA SERVICE PANTAU FINGER BPJS - CONSOLE");
        garis();
    }

    private static final class Antrian {
        final long id; final String noRawat; final String noKartu; final String tanggalPelayanan; final int attemptCount;
        Antrian(long id, String noRawat, String noKartu, String tanggalPelayanan, int attemptCount) {
            this.id = id; this.noRawat = noRawat; this.noKartu = noKartu; this.tanggalPelayanan = tanggalPelayanan; this.attemptCount = attemptCount;
        }
    }
}
