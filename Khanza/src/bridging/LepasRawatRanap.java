package bridging;

import fungsi.koneksiDB;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class LepasRawatRanap {
    private LepasRawatRanap() {
    }

    static final class Isi {
        final String obat;
        final String lab;
        final String pemeriksaan;

        Isi(String obat, String lab, String pemeriksaan) {
            this.obat = rapikanBaris(obat);
            this.lab = rapikanBaris(lab);
            this.pemeriksaan = rapikanBaris(pemeriksaan);
        }

        private static String rapikanBaris(String teks) {
            return Objects.toString(teks, "").replace("\r\n", "\n").replace('\r', '\n');
        }

        boolean samaDengan(Isi lain) {
            return obat.equals(lain.obat) && lab.equals(lain.lab) && pemeriksaan.equals(lain.pemeriksaan);
        }
    }

    static final class Data {
        final Isi isi;
        final long revisi;

        Data(Isi isi, long revisi) {
            this.isi = isi;
            this.revisi = revisi;
        }
    }

    static Data baca(Connection koneksi, String noRawat) throws SQLException {
        String sql = "select coalesce(d.revisi,0), "
                + "d.obat_pulang, "
                + "case when d.no_rawat is null then r.hasil_laborat else d.hasil_laborat end, "
                + "case when d.no_rawat is null then r.pemeriksaan_penunjang else d.pemeriksaan_lain end "
                + "from reg_periksa p left join resume_pasien_ranap r on r.no_rawat=p.no_rawat "
                + "left join rsaj_lepas_rawat_ranap d on d.no_rawat=p.no_rawat "
                + "where p.no_rawat=? and p.status_lanjut='Ranap'";
        Data data;
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Kunjungan rawat inap tidak ditemukan.");
                }
                data = new Data(new Isi(rs.getString(2), rs.getString(3), rs.getString(4)), rs.getLong(1));
            }
        }
        if (data.revisi != 0) {
            return data;
        }
        return new Data(new Isi(obatPulang(koneksi, noRawat), data.isi.lab, data.isi.pemeriksaan), 0);
    }

    static String urlTandaTanganDokter(Connection koneksi, String noSurat) {
        String sql = "select min(m.kd_dokter) as kd_dokter "
                + "from bridging_surat_kontrol_bpjs s join maping_dokter_dpjpvclaim m "
                + "on s.kd_dokter_bpjs=m.kd_dokter_bpjs "
                + "where s.no_surat=? having count(distinct m.kd_dokter)=1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noSurat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String kode = rs.getString("kd_dokter").trim();
                if (kode.isEmpty() || kode.contains("/") || kode.contains("\\")) {
                    return null;
                }
                String namaFile = URLEncoder.encode(kode, StandardCharsets.UTF_8).replace("+", "%20") + ".png";
                return "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                        + koneksiDB.HYBRIDWEB() + "/tandatangandokter/pages/upload/" + namaFile;
            }
        } catch (SQLException ex) {
            System.out.println("Gagal mengambil tanda tangan dokter untuk Formulir Lepas Rawat: " + ex.getMessage());
            return null;
        }
    }

    static LocalDate tanggalPulang(Connection koneksi, String noRawat) throws SQLException {
        String sql = "select cast(tgl_keluar as char(10)) as tgl_keluar from kamar_inap where no_rawat=? "
                + "and stts_pulang<>'Pindah Kamar' order by tgl_masuk desc,jam_masuk desc limit 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                String tanggal = rs.next() ? rs.getString("tgl_keluar") : null;
                if (tanggal == null || tanggal.isEmpty() || "0000-00-00".equals(tanggal)) {
                    return null;
                }
                try {
                    return LocalDate.parse(tanggal);
                } catch (DateTimeParseException ex) {
                    throw new SQLException("Format tanggal pulang tidak valid.", ex);
                }
            }
        }
    }

    static String obatPulang(Connection koneksi, String noRawat) throws SQLException {
        String sql = "select b.nama_brng,trim(coalesce(d.dosis,'')) as dosis "
                + "from permintaan_resep_pulang p join detail_permintaan_resep_pulang d on d.no_permintaan=p.no_permintaan "
                + "join databarang b on b.kode_brng=d.kode_brng where p.no_rawat=? and p.status='Belum' "
                + "union select b.nama_brng,trim(coalesce(r.dosis,'')) as dosis "
                + "from resep_pulang r join databarang b on b.kode_brng=r.kode_brng where r.no_rawat=? "
                + "order by nama_brng,dosis";
        List<String> obat = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            ps.setString(2, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String dosis = rs.getString(2);
                    obat.add("- " + rs.getString(1) + (dosis.isEmpty() ? "" : " — " + dosis));
                }
            }
        }
        return String.join("\n", obat);
    }

    static long simpan(Connection koneksi, String noRawat, Data awal, Isi isi, String petugas) throws SQLException {
        if (awal.revisi != 0 && isi.samaDengan(awal.isi)) {
            return awal.revisi;
        }
        String sql = awal.revisi == 0
                ? "insert into rsaj_lepas_rawat_ranap "
                    + "(obat_pulang,hasil_laborat,pemeriksaan_lain,petugas,no_rawat) values (?,?,?,?,?)"
                : "update rsaj_lepas_rawat_ranap set obat_pulang=?,hasil_laborat=?,pemeriksaan_lain=?,"
                    + "petugas=?,revisi=revisi+1 where no_rawat=? and revisi=?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, isi.obat);
            ps.setString(2, isi.lab);
            ps.setString(3, isi.pemeriksaan);
            ps.setString(4, petugas);
            ps.setString(5, noRawat);
            if (awal.revisi != 0) {
                ps.setLong(6, awal.revisi);
            }
            if (ps.executeUpdate() == 1) {
                return awal.revisi + 1;
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            if (ex.getErrorCode() != 1062) {
                throw ex;
            }
        }
        throw new SQLException("Formulir Lepas Rawat sudah diubah petugas lain. Buka ulang form untuk melihat data terbaru.", "40001");
    }

    static List<String> hasilLab(Connection koneksi, String noRawat) throws SQLException {
        String sql = "select d.tgl_periksa,coalesce(nullif(trim(j.nm_perawatan),''),d.kd_jenis_prw),"
                + "coalesce(t.Pemeriksaan,''),d.nilai,coalesce(t.satuan,''),d.keterangan "
                + "from detail_periksa_lab d "
                + "left join template_laboratorium t on t.id_template=d.id_template and t.kd_jenis_prw=d.kd_jenis_prw "
                + "left join jns_perawatan_lab j on j.kd_jenis_prw=d.kd_jenis_prw "
                + "where d.no_rawat=? and trim(d.nilai) not in ('','-') "
                + "order by d.tgl_periksa desc,d.jam desc,j.nm_perawatan,t.urut,d.id_template";
        List<String> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String jenis = rs.getString(2).trim();
                    String pemeriksaan = rs.getString(3).trim();
                    String satuan = rs.getString(5).trim();
                    String catatan = rs.getString(6).trim();
                    String nama = pemeriksaan.isEmpty() || pemeriksaan.equalsIgnoreCase(jenis)
                            ? jenis : jenis + " / " + pemeriksaan;
                    hasil.add(nama + ": " + rs.getString(4).trim()
                            + (satuan.isEmpty() || satuan.equals("-") ? "" : " " + satuan)
                            + (catatan.isEmpty() || catatan.equals("-") ? "" : " (" + catatan + ")")
                            + " [" + rs.getDate(1) + "]");
                }
            }
        }
        return hasil;
    }

    static List<String> hasilRadiologi(Connection koneksi, String noRawat) throws SQLException {
        String sql = "select h.tgl_periksa,h.jam,h.hasil, "
                + "(select group_concat(distinct j.nm_perawatan order by j.nm_perawatan separator ', ') "
                + " from periksa_radiologi p join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw "
                + " where p.no_rawat=h.no_rawat and p.tgl_periksa=h.tgl_periksa and p.jam=h.jam) "
                + "from hasil_radiologi h where h.no_rawat=? and trim(h.hasil) not in ('','-') "
                + "order by h.tgl_periksa desc,h.jam desc";
        List<String> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nama = Objects.toString(rs.getString(4), "Radiologi");
                    hasil.add(nama + " [" + rs.getDate(1) + " " + rs.getTime(2) + "]\n" + rs.getString(3).trim());
                }
            }
        }
        return hasil;
    }
}
