package permintaan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.TreeSet;

/** Guard khusus form booking operasi; tidak mengubah skema atau endpoint BPJS. */
final class BookingOperasiGuard {
    static final String[] KOLOM = {"no_rawat", "kode_paket", "tanggal", "jam_mulai",
        "jam_selesai", "status", "kd_dokter", "kd_ruang_ok", "dokteranastesi", "perawat"};
    static final String[] STATUS = {"Permintaan", "Menunggu", "Proses Operasi", "Selesai"};

    static final class Data {
        final long id;
        final String[] nilai;
        Data(long id, String... nilai) {
            if (nilai.length != KOLOM.length) throw new IllegalArgumentException("Data jadwal tidak lengkap.");
            this.id = id;
            this.nilai = nilai.clone();
            for (int n = 0; n < this.nilai.length; n++) {
                if (this.nilai[n] == null) this.nilai[n] = "";
            }
        }
        boolean kunciSama(Data other) {
            for (int n : new int[]{0, 1, 2, 6}) {
                if (!nilai[n].equals(other.nilai[n])) return false;
            }
            return true;
        }
    }

    static int urutan(String status) {
        for (int n = 0; n < STATUS.length; n++) if (STATUS[n].equals(status)) return n;
        return -1;
    }

    static long cariDuplikat(Connection c, Data data, long kecuali) throws SQLException {
        String sql = "select id_booking from booking_operasi where no_rawat=? and kode_paket=? "
                + "and tanggal=? and kd_dokter=? and id_booking<>? order by id_booking limit 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, data.nilai[0]); ps.setString(2, data.nilai[1]);
            ps.setString(3, data.nilai[2]); ps.setString(4, data.nilai[6]); ps.setLong(5, kecuali);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0; }
        }
    }

    /** Gunakan koneksi transaksi tersendiri dari condbTransaksi(), bukan koneksi bersama. */
    static String simpanBaru(Connection c, Data baru) throws SQLException {
        if(baru == null || baru.id != 0 || !"Permintaan".equals(baru.nilai[5])) {
            throw new SQLException("Simpan Baru hanya untuk jadwal baru berstatus Permintaan.");
        }
        return ubah(c,baru,null,Aksi.BARU);
    }

    static String ganti(Connection c, Data baru, Data lama) throws SQLException {
        periksaIdentitas(baru,lama);
        return ubah(c,baru,lama,Aksi.GANTI);
    }

    private enum Aksi { BARU, GANTI, STATUS }

    private static void periksaIdentitas(Data baru, Data lama) throws SQLException {
        if(lama == null || lama.id <= 0 || baru == null || baru.id != lama.id) {
            throw new SQLException("Identitas jadwal yang akan diperbarui tidak valid. Pilih ulang jadwal; perubahan tidak boleh menjadi jadwal baru.");
        }
    }

    static String status(Connection c, Data lama, String tujuan) throws SQLException {
        periksaIdentitas(lama,lama);
        int asal = urutan(lama.nilai[5]);
        if (asal < 0 || asal >= STATUS.length - 1 || !STATUS[asal + 1].equals(tujuan)) {
            throw new SQLException("Badge hanya dapat melanjutkan ke status berikutnya.");
        }
        String[] nilai = lama.nilai.clone();
        nilai[5] = tujuan;
        return ubah(c, new Data(lama.id, nilai), lama, Aksi.STATUS);
    }

    private static String ubah(Connection c, Data baru, Data lama, Aksi aksi) throws SQLException {
        if(aksi != Aksi.BARU) periksaIdentitas(baru,lama);
        boolean hanyaStatus = aksi == Aksi.STATUS;
        c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        c.setAutoCommit(false);
        try {
            // Serialisasi penyimpanan dari form ini, termasuk dua petugas pada No. Rawat sama.
            TreeSet<String> rawat = new TreeSet<>();
            rawat.add(baru.nilai[0]);
            if (lama != null) rawat.add(lama.nilai[0]);
            for (String no : rawat) {
                try (PreparedStatement lock = c.prepareStatement(
                        "select no_rawat from reg_periksa where no_rawat=? for update")) {
                    lock.setString(1, no);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No. Rawat tidak ditemukan.");
                    }
                }
            }
            if (lama != null) {
                try (PreparedStatement lock = c.prepareStatement(
                        "select " + String.join(",", KOLOM) + " from booking_operasi where id_booking=? for update")) {
                    lock.setLong(1, lama.id);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw berubah();
                        for (int n = 0; n < KOLOM.length; n++) {
                            String isi = rs.getString(n + 1);
                            if (!lama.nilai[n].equals(isi == null ? "" : isi)) throw berubah();
                        }
                    }
                }
            }
            // Duplikat lama boleh diperbaiki satu per satu; kunci jadwal tidak boleh menjadi duplikat baru.
            if (!hanyaStatus && (lama == null || !baru.kunciSama(lama))
                    && cariDuplikat(c, baru, lama == null ? 0 : lama.id) > 0) {
                throw new SQLException("Jadwal untuk No. Rawat, tanggal, paket dan dokter ini sudah ada. "
                        + "Pilih jadwal tersebut, lalu gunakan Ganti untuk mengubah jam atau data lainnya.");
            }
            String sql;
            Object[] parameter;
            if (aksi == Aksi.BARU) {
                sql = "insert into booking_operasi (" + String.join(",", KOLOM)
                        + ",waktu) values (?,?,?,?,?,?,?,?,?,?,now())";
                parameter = baru.nilai;
            } else if (hanyaStatus) {
                sql = "update booking_operasi set status=?,waktu=now() where id_booking=?";
                parameter = new Object[]{baru.nilai[5], lama.id};
            } else {
                String[] set = new String[KOLOM.length];
                for (int n = 0; n < set.length; n++) set[n] = KOLOM[n] + "=?";
                sql = "update booking_operasi set " + String.join(",", set) + ",waktu=now() where id_booking=?";
                parameter = Arrays.copyOf(baru.nilai, 11, Object[].class);
                parameter[10] = lama.id;
            }
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int n = 0; n < parameter.length; n++) ps.setObject(n + 1, parameter[n]);
                if (ps.executeUpdate() != 1) throw berubah();
            }
            c.commit();
            return audit(sql, parameter);
        } catch (SQLException | RuntimeException ex) {
            try { c.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
            throw ex;
        }
    }

    private static SQLException berubah() {
        return new SQLException("Jadwal sudah berubah atau dialihkan oleh petugas lain. "
                + "Muat ulang daftar dan pilih kembali jadwal sebelum melanjutkan.");
    }

    private static String audit(String sql, Object[] parameter) {
        StringBuilder log = new StringBuilder();
        int n = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?') log.append('\'').append(String.valueOf(parameter[n++]).replace("'", "''")).append('\'');
            else log.append(ch);
        }
        return log.toString();
    }
}
