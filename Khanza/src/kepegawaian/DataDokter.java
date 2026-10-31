package kepegawaian;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Data pencarian dokter; data opsional kosong tidak boleh menghentikan daftar. */
final class DataDokter {
    static final String SELECT_DOKTER =
            "select dokter.kd_dokter,dokter.nm_dokter,dokter.jk,dokter.tmp_lahir," +
            "dokter.tgl_lahir,dokter.gol_drh,dokter.agama,dokter.almt_tgl,dokter.no_telp," +
            "dokter.stts_nikah,spesialis.nm_sps,dokter.alumni,dokter.no_ijn_praktek,pegawai.no_ktp " +
            "from dokter left join spesialis on dokter.kd_sps=spesialis.kd_sps " +
            "left join pegawai on pegawai.nik=dokter.kd_dokter " +
            "where dokter.status='1' order by dokter.nm_dokter";
    private static final String[] KEYS = {"KodeDokter", "NamaDokter", "JK", "TmpLahir",
        "TglLahir", "GD", "Agama", "AlamatTinggal", "NoTelp", "SttsNikah",
        "Spesialis", "Alumni", "NoIjinPraktek"};

    private DataDokter() { }

    static List<String[]> bacaJadwal(Connection koneksi, int day, String poli) throws SQLException {
        String[] hari = {"", "AKHAD", "SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU"};
        return baca(koneksi,
                "select dokter.kd_dokter,dokter.nm_dokter,dokter.jk,dokter.tmp_lahir," +
                "dokter.tgl_lahir,dokter.gol_drh,dokter.agama,dokter.almt_tgl,dokter.no_telp," +
                "dokter.stts_nikah,spesialis.nm_sps,dokter.alumni,dokter.no_ijn_praktek,jadwal.kuota " +
                "from dokter left join spesialis on dokter.kd_sps=spesialis.kd_sps " +
                "inner join jadwal on dokter.kd_dokter=jadwal.kd_dokter " +
                "inner join poliklinik on poliklinik.kd_poli=jadwal.kd_poli " +
                "where jadwal.hari_kerja=? and poliklinik.nm_poli like ? " +
                "and dokter.status='1' order by dokter.nm_dokter",
                day >= 1 && day <= 7 ? hari[day] : "", poli);
    }

    static List<String[]> baca(Connection koneksi, String sql, String... parameter) throws SQLException {
        List<String[]> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < parameter.length; i++) {
                ps.setString(i + 1, parameter[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int kolom = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] baris = new String[kolom];
                    for (int i = 0; i < kolom; i++) {
                        String nilai = rs.getString(i + 1);
                        baris[i] = nilai == null ? "" : nilai;
                    }
                    hasil.add(baris);
                }
            }
        }
        return hasil;
    }

    static boolean cocok(String[] baris, String kata) {
        String cari = kata == null ? "" : kata.trim().toLowerCase(Locale.ROOT);
        return baris[0].toLowerCase(Locale.ROOT).contains(cari)
                || baris[1].toLowerCase(Locale.ROOT).contains(cari)
                || baris[10].toLowerCase(Locale.ROOT).contains(cari);
    }

    static String nama(Connection koneksi, String kode) throws SQLException {
        if (kode == null || kode.trim().isEmpty()) {
            return "";
        }
        List<String[]> hasil = baca(koneksi,
                "select nm_dokter from dokter where kd_dokter=?", kode.trim());
        return hasil.isEmpty() ? "" : hasil.get(0)[0];
    }

    /** Tulis setelah pembacaan lengkap; cache lama tetap utuh bila penulisan gagal. */
    static void simpanCache(List<String[]> data, String lokasi, String kolomTerakhir) throws IOException {
        List<Map<String, String>> daftar = new ArrayList<>();
        for (String[] baris : data) {
            Map<String, String> dokter = new LinkedHashMap<>();
            for (int i = 0; i < KEYS.length; i++) {
                dokter.put(KEYS[i], baris[i]);
            }
            if (baris.length > KEYS.length) {
                dokter.put(kolomTerakhir, baris[KEYS.length]);
            }
            daftar.add(dokter);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("dokter", daftar);
        Path target = new java.io.File(lokasi).toPath().toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path sementara = Files.createTempFile(target.getParent(), "dokter-", ".tmp");
        try {
            new ObjectMapper().writeValue(sementara.toFile(), root);
            Files.move(sementara, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(sementara);
        }
    }
}
