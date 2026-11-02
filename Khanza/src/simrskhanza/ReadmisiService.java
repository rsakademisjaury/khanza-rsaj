package simrskhanza;

import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Query terisolasi, snapshot rekap dan optimistic locking pencatatan readmisi. */
final class ReadmisiService {
    interface Connections { Connection open() throws SQLException; }
    private final Connections connections;
    ReadmisiService() { this(() -> koneksiDB.condbTransaksi()); }
    ReadmisiService(Connections connections) { this.connections = connections; }

    static final class Data {
        String asal, masuk, rm, nama, kamar, bangsal, ruangan, diagnosa;
        String alasan = "", keterangan = "";
        LocalDateTime pulang, datang;
        int versi;
        boolean perluSimpan;
        boolean sumberSama(Data d) {
            return d != null && Objects.equals(asal,d.asal) && Objects.equals(masuk,d.masuk)
                    && Objects.equals(rm,d.rm) && Objects.equals(nama,d.nama)
                    && Objects.equals(kamar,d.kamar) && Objects.equals(bangsal,d.bangsal)
                    && Objects.equals(ruangan,d.ruangan) && Objects.equals(diagnosa,d.diagnosa)
                    && Objects.equals(pulang,d.pulang) && Objects.equals(datang,d.datang);
        }
    }
    static final class SumberBerubah extends Exception {
        private static final long serialVersionUID=1L;
        final Data terbaru;
        SumberBerubah(Data terbaru) {
            super("Data sumber berubah. Periksa tanggal, diagnosa dan ruangan terbaru, lalu simpan kembali.");
            this.terbaru = terbaru;
        }
    }
    static void validasiJarak(LocalDateTime pulang, LocalDateTime masuk) throws Exception {
        if (pulang == null || masuk == null) throw new Exception("Tanggal/jam belum lengkap.");
        Duration jarak = Duration.between(pulang,masuk);
        if (jarak.isNegative() || jarak.compareTo(Duration.ofDays(5)) > 0)
            throw new Exception("Kunjungan rawat inap berikutnya tidak memenuhi batas readmisi <= 5 hari (120 jam).");
    }
    private void siap(Connection c) throws SQLException {
        try (PreparedStatement p=c.prepareStatement("SELECT no_rawat_asal,versi FROM rsaj_readmisi LIMIT 0");
             ResultSet r=p.executeQuery()) { /* tabel wajib tersedia, tanpa DDL otomatis */ }
        catch (SQLException e) {
            if (e.getErrorCode()==1146 || "42S02".equals(e.getSQLState()))
                throw new SQLException("Tabel readmisi belum tersedia. Jalankan rsaj_readmisi.sql terlebih dahulu.",e);
            throw e;
        }
    }
    Data muat(String noRawat) throws Exception {
        try (Connection c=connections.open()) {
            siap(c);
            Data lama=tersimpan(c,noRawat);
            // Catatan lama selalu dibuka dari snapshot agar koreksi catatan tetap dapat ditinjau.
            return lama==null ? sumber(c,noRawat) : lama;
        }
    }
    Data sumberTerbaru(String noRawat) throws Exception {
        try (Connection c=connections.open()) {
            siap(c);
            Data baru=sumber(c,noRawat), lama=tersimpan(c,noRawat);
            if (lama!=null) { baru.versi=lama.versi; baru.alasan=lama.alasan; baru.keterangan=lama.keterangan; baru.perluSimpan=!lama.sumberSama(baru); }
            return baru;
        }
    }
    private Data sumber(Connection c,String noRawat) throws Exception {
        Data d=new Data();
        // Ambil segmen kamar terakhir; Pindah Kamar / kamar aktif bukan kepulangan.
        String sql="SELECT r.no_rawat,r.no_rkm_medis,p.nm_pasien,k.kd_kamar,"
                + "b.kd_bangsal,b.nm_bangsal,k.diagnosa_akhir,k.tgl_keluar,k.jam_keluar,k.stts_pulang "
                + "FROM reg_periksa r JOIN pasien p ON p.no_rkm_medis=r.no_rkm_medis "
                + "JOIN kamar_inap k ON k.no_rawat=r.no_rawat "
                + "LEFT JOIN kamar km ON km.kd_kamar=k.kd_kamar "
                + "LEFT JOIN bangsal b ON b.kd_bangsal=km.kd_bangsal "
                + "WHERE r.no_rawat=? AND r.status_lanjut='Ranap' AND r.stts<>'Batal' "
                + "AND NOT EXISTS (SELECT 1 FROM kamar_inap a WHERE a.no_rawat=r.no_rawat AND a.stts_pulang='-') "
                + "ORDER BY k.tgl_masuk DESC,k.jam_masuk DESC,k.tgl_keluar DESC,k.jam_keluar DESC,k.kd_kamar LIMIT 1";
        try (PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,noRawat);
            try (ResultSet r=p.executeQuery()) {
                if (!r.next()) throw new Exception("Pilih episode rawat inap yang sudah pulang. Pasien aktif belum dapat dicatat readmisinya.");
                String status=teks(r,"stts_pulang");
                if (status.isEmpty() || status.equals("-") || status.equalsIgnoreCase("Pindah Kamar")
                        || status.equalsIgnoreCase("Status Belum Lengkap"))
                    throw new Exception("Episode terakhir belum memiliki kepulangan akhir yang valid.");
                d.asal=r.getString("no_rawat"); d.rm=r.getString("no_rkm_medis");
                d.nama=teks(r,"nm_pasien"); d.kamar=teks(r,"kd_kamar");
                d.bangsal=teks(r,"kd_bangsal"); d.ruangan=teks(r,"nm_bangsal");
                d.diagnosa=teks(r,"diagnosa_akhir");
                d.pulang=waktu(r,"tgl_keluar","jam_keluar");
            }
        }
        if (d.pulang==null) throw new Exception("Tanggal/jam pulang belum lengkap.");
        // Kunjungan Ranap pertama setelah pulang. Kunjungan Ralan/Batal tidak dianggap readmisi.
        sql="SELECT no_rawat,tgl_registrasi,jam_reg FROM reg_periksa "
                + "WHERE no_rkm_medis=? AND no_rawat<>? AND status_lanjut='Ranap' AND stts<>'Batal' "
                + "AND (tgl_registrasi>? OR (tgl_registrasi=? AND jam_reg>=?)) "
                + "ORDER BY tgl_registrasi,jam_reg,no_rawat LIMIT 1";
        try (PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,d.rm); p.setString(2,d.asal);
            p.setString(3,d.pulang.toLocalDate().toString());
            p.setString(4,d.pulang.toLocalDate().toString());
            p.setString(5,d.pulang.toLocalTime().toString());
            try (ResultSet r=p.executeQuery()) {
                if (!r.next()) throw new Exception("Belum ada kunjungan rawat inap berikutnya setelah pasien pulang.");
                d.masuk=r.getString("no_rawat"); d.datang=waktu(r,"tgl_registrasi","jam_reg");
            }
        }
        validasiJarak(d.pulang,d.datang);
        return d;
    }
    Data simpan(Data ditampilkan,String alasan,String keterangan,String petugas,boolean boleh) throws Exception {
        if (!boleh) throw new Exception("Anda tidak memiliki hak akses Kamar Inap.");
        if (ditampilkan==null) throw new Exception("Muat data pasien terlebih dahulu.");
        alasan=alasan==null ? "" : alasan.trim(); keterangan=keterangan==null ? "" : keterangan.trim();
        if (alasan.isEmpty()) throw new Exception("Alasan readmisi wajib diisi.");
        if (alasan.length()>4000 || keterangan.length()>4000) throw new Exception("Maksimal 4.000 karakter per catatan.");
        if (petugas==null || petugas.trim().isEmpty()) throw new Exception("Identitas petugas tidak tersedia. Silakan login ulang.");
        try (Connection c=connections.open()) {
            siap(c); c.setAutoCommit(false);
            try {
                Data baru=sumber(c,ditampilkan.asal);
                if (!ditampilkan.sumberSama(baru)) {
                    baru.alasan=alasan; baru.keterangan=keterangan; baru.versi=ditampilkan.versi; baru.perluSimpan=true;
                    throw new SumberBerubah(baru);
                }
                // Snapshot diperbarui hanya setelah nilai sumber yang sama sudah ditampilkan untuk ditinjau.
                String sql=ditampilkan.versi==0
                        ? "INSERT INTO rsaj_readmisi (no_rawat_asal,no_rawat_masuk,no_rkm_medis,nama_pasien,"
                          + "tgl_jam_pulang,tgl_jam_masuk,kd_kamar,kd_bangsal,ruangan,diagnosa,alasan_readmisi,"
                          + "keterangan,petugas_buat,petugas_ubah) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                        : "UPDATE rsaj_readmisi SET no_rawat_masuk=?,no_rkm_medis=?,nama_pasien=?,"
                          + "tgl_jam_pulang=?,tgl_jam_masuk=?,kd_kamar=?,kd_bangsal=?,ruangan=?,diagnosa=?,"
                          + "alasan_readmisi=?,keterangan=?,petugas_ubah=?,versi=versi+1 "
                          + "WHERE no_rawat_asal=? AND versi=?";
                try (PreparedStatement p=c.prepareStatement(sql)) {
                    int i=1;
                    if (ditampilkan.versi==0) p.setString(i++,baru.asal);
                    p.setString(i++,baru.masuk); p.setString(i++,baru.rm); p.setString(i++,baru.nama);
                    p.setTimestamp(i++,Timestamp.valueOf(baru.pulang)); p.setTimestamp(i++,Timestamp.valueOf(baru.datang));
                    p.setString(i++,baru.kamar); p.setString(i++,baru.bangsal); p.setString(i++,baru.ruangan);
                    p.setString(i++,baru.diagnosa); p.setString(i++,alasan); p.setString(i++,keterangan);
                    if (ditampilkan.versi==0) p.setString(i++,petugas);
                    p.setString(i++,petugas);
                    if (ditampilkan.versi!=0) { p.setString(i++,baru.asal); p.setInt(i++,ditampilkan.versi); }
                    if (p.executeUpdate()!=1) throw new Exception("Catatan sudah diubah petugas lain. Muat ulang sebelum menyimpan kembali.");
                }
                c.commit(); baru.versi=ditampilkan.versi+1; baru.alasan=alasan; baru.keterangan=keterangan;
                return baru;
            } catch (Exception e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                if (e instanceof SQLException && ((SQLException)e).getErrorCode()==1062)
                    throw new Exception("Pasangan kunjungan sudah dicatat petugas lain. Muat ulang data terlebih dahulu.",e);
                throw e;
            }
        }
    }
    List<Data> rekap(LocalDate awal,LocalDate akhir,String ruangan,String cari) throws Exception {
        if (awal==null || akhir==null || !akhir.isAfter(awal)) throw new Exception("Periode rekap tidak valid.");
        List<Data> daftar=new ArrayList<>();
        try (Connection c=connections.open()) {
            siap(c);
            String sql="SELECT * FROM rsaj_readmisi WHERE tgl_jam_masuk>=? AND tgl_jam_masuk<? "
                    + "AND (?='' OR ruangan=?) AND (?='' OR no_rkm_medis LIKE ? ESCAPE '!' OR nama_pasien LIKE ? ESCAPE '!') "
                    + "ORDER BY tgl_jam_masuk,no_rawat_asal";
            try (PreparedStatement p=c.prepareStatement(sql)) {
                p.setTimestamp(1,Timestamp.valueOf(awal.atStartOfDay()));
                p.setTimestamp(2,Timestamp.valueOf(akhir.atStartOfDay()));
                p.setString(3,ruangan); p.setString(4,ruangan); p.setString(5,cari);
                String pola="%"+cari.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
                // ESCAPE literal di bawah menjaga pencarian nama/RM, bukan pola SQL.
                p.setString(6,pola); p.setString(7,pola);
                try (ResultSet r=p.executeQuery()) { while(r.next()) daftar.add(map(r)); }
            }
        }
        return daftar;
    }
    List<String> ruangan() throws Exception {
        List<String> daftar=new ArrayList<>();
        try (Connection c=connections.open()) {
            siap(c);
            try (PreparedStatement p=c.prepareStatement("SELECT DISTINCT ruangan FROM rsaj_readmisi ORDER BY ruangan");
                 ResultSet r=p.executeQuery()) { while(r.next()) daftar.add(r.getString(1)); }
        }
        return daftar;
    }
    private Data tersimpan(Connection c,String noRawat) throws SQLException {
        try (PreparedStatement p=c.prepareStatement("SELECT * FROM rsaj_readmisi WHERE no_rawat_asal=?")) {
            p.setString(1,noRawat);
            try(ResultSet r=p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }
    private static Data map(ResultSet r) throws SQLException {
        Data d=new Data(); d.asal=r.getString("no_rawat_asal"); d.masuk=r.getString("no_rawat_masuk");
        d.rm=r.getString("no_rkm_medis"); d.nama=r.getString("nama_pasien");
        d.pulang=r.getTimestamp("tgl_jam_pulang").toLocalDateTime();
        d.datang=r.getTimestamp("tgl_jam_masuk").toLocalDateTime();
        d.kamar=r.getString("kd_kamar"); d.bangsal=r.getString("kd_bangsal"); d.ruangan=r.getString("ruangan");
        d.diagnosa=r.getString("diagnosa"); d.alasan=r.getString("alasan_readmisi");
        d.keterangan=r.getString("keterangan"); d.versi=r.getInt("versi"); return d;
    }
    private static String teks(ResultSet r,String kolom) throws SQLException {
        String s=r.getString(kolom); return s==null ? "" : s;
    }
    private static LocalDateTime waktu(ResultSet r,String tanggal,String jam) throws SQLException {
        String t=r.getString(tanggal), j=r.getString(jam);
        if (t==null || j==null || t.startsWith("0000-00-00") || t.trim().isEmpty() || j.trim().isEmpty()) return null;
        try { return LocalDateTime.parse(t+"T"+j); }
        catch (java.time.format.DateTimeParseException e) { throw new SQLException("Tanggal/jam sumber tidak valid.",e); }
    }
}
