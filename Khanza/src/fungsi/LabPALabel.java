package fungsi;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Dialog;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/** Label PA memakai nomor yang sudah diterbitkan, tanpa mengubah data/urutan PA. */
public final class LabPALabel {
    private LabPALabel() {}

    // Hubungan noorder pada hasil didahulukan. Data lama hanya boleh cocok lewat
    // kode DAN waktu hasil yang tepat, serta hanya satu order yang cocok.
    private static final String SQL =
        "select d.kd_jenis_prw, j.nm_perawatan as pemeriksaan, n.no_pa, "
        + "p.nm_pasien as nama, p.no_rkm_medis as norm, p.tgl_lahir, dr.nm_dokter as dokter, "
        + "case when pl.status='ranap' then "
        + "(select b.nm_bangsal from kamar_inap ki inner join kamar k on k.kd_kamar=ki.kd_kamar "
        + "inner join bangsal b on b.kd_bangsal=k.kd_bangsal where ki.no_rawat=pl.no_rawat "
        + "order by ki.tgl_masuk desc, ki.jam_masuk desc limit 1) else poli.nm_poli end as unit "
        + "from permintaan_labpa pl "
        + "inner join permintaan_pemeriksaan_labpa d on d.noorder=pl.noorder "
        + "inner join jns_perawatan_lab j on j.kd_jenis_prw=d.kd_jenis_prw "
        + "inner join reg_periksa rp on rp.no_rawat=pl.no_rawat "
        + "inner join pasien p on p.no_rkm_medis=rp.no_rkm_medis "
        + "left join dokter dr on dr.kd_dokter=pl.dokter_perujuk "
        + "left join poliklinik poli on poli.kd_poli=rp.kd_poli "
        + "left join rsaj_labpa_nomor n on n.no_rawat=pl.no_rawat and n.kd_jenis_prw=d.kd_jenis_prw and ("
        + "exists (select 1 from rsaj_labpa_hasil h where h.no_rawat=n.no_rawat "
        + "and h.kd_jenis_prw=n.kd_jenis_prw and h.tgl_periksa=n.tgl_periksa and h.jam=n.jam and h.noorder=pl.noorder) "
        + "or (pl.tgl_hasil=n.tgl_periksa and pl.jam_hasil=n.jam "
        + "and not exists (select 1 from rsaj_labpa_hasil h where h.no_rawat=n.no_rawat "
        + "and h.kd_jenis_prw=n.kd_jenis_prw and h.tgl_periksa=n.tgl_periksa and h.jam=n.jam "
        + "and coalesce(trim(h.noorder),'')<>'') "
        + "and (select count(*) from permintaan_labpa p2 inner join permintaan_pemeriksaan_labpa d2 on d2.noorder=p2.noorder "
        + "where p2.no_rawat=n.no_rawat and d2.kd_jenis_prw=n.kd_jenis_prw "
        + "and p2.tgl_hasil=n.tgl_periksa and p2.jam_hasil=n.jam)=1)) "
        + "where pl.noorder=? and pl.no_rawat=? order by j.nm_perawatan,d.kd_jenis_prw,n.no_pa";

    private static String teks(String value) { return value == null ? "" : value.trim(); }
    private static String tampil(String value) { return teks(value).isEmpty() ? "-" : teks(value); }

    /** Ambil tepat satu hasil: nomor, kode, tanggal dan jam tidak boleh tertukar. */
    public static List<Map<String,?>> dataHasil(Connection c, LabPAData.Kunci k) throws SQLException {
        if (k == null || k.noRawat.isEmpty() || k.kode.isEmpty() || k.tanggal.isEmpty() || k.jam.isEmpty()) {
            throw new SQLException("Pilih pemeriksaan PA yang akan dicetak terlebih dahulu.");
        }
        String sql = "select n.no_pa,j.nm_perawatan as pemeriksaan,p.nm_pasien as nama,"
                + "p.no_rkm_medis as norm,p.tgl_lahir,dr.nm_dokter as dokter,"
                + "case when lower(pl.status)='ranap' then "
                + "(select b.nm_bangsal from kamar_inap ki inner join kamar km on km.kd_kamar=ki.kd_kamar "
                + "inner join bangsal b on b.kd_bangsal=km.kd_bangsal where ki.no_rawat=pl.no_rawat "
                + "order by ki.tgl_masuk desc,ki.jam_masuk desc limit 1) else poli.nm_poli end as unit "
                + "from periksa_lab pl "
                + "inner join jns_perawatan_lab j on j.kd_jenis_prw=pl.kd_jenis_prw "
                + "inner join reg_periksa rp on rp.no_rawat=pl.no_rawat "
                + "inner join pasien p on p.no_rkm_medis=rp.no_rkm_medis "
                + "left join dokter dr on dr.kd_dokter=pl.dokter_perujuk "
                + "left join poliklinik poli on poli.kd_poli=rp.kd_poli "
                + "left join rsaj_labpa_nomor n on n.no_rawat=pl.no_rawat and n.kd_jenis_prw=pl.kd_jenis_prw "
                + "and n.tgl_periksa=pl.tgl_periksa and n.jam=pl.jam "
                + "where pl.no_rawat=? and pl.kd_jenis_prw=? and pl.tgl_periksa=? and pl.jam=? and pl.kategori='PA'";
        List<Map<String,?>> rows = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            k.bind(ps,1);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Hasil pemeriksaan PA yang dipilih tidak ditemukan. Muat ulang daftar pemeriksaan.");
                if (teks(rs.getString("no_pa")).isEmpty()) {
                    throw new SQLException("No. PA belum diterbitkan untuk pemeriksaan ini. Klik Buat No. PA terlebih dahulu.");
                }
                rows.add(barisLabel(rs));
                if (rs.next()) throw new SQLException("Lebih dari satu hasil cocok. Periksa data pemeriksaan PA sebelum mencetak label.");
            }
        }
        return rows;
    }

    private static Map<String,Object> barisLabel(ResultSet rs) throws SQLException {
        Map<String,Object> row = new HashMap<>();
        for (String field : new String[]{"no_pa","nama","norm","unit","dokter","pemeriksaan"}) {
            row.put(field,tampil(rs.getString(field)));
        }
        String lahir = teks(rs.getString("tgl_lahir"));
        try {
            lahir = LocalDate.parse(lahir).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (java.time.format.DateTimeParseException ex) { lahir = "-"; }
        row.put("tgllahir",lahir);
        return row;
    }

    public static List<Map<String,?>> data(Connection c, String noorder, String noRawat) throws SQLException {
        List<Map<String,?>> rows = new ArrayList<>();
        List<String> belum = new ArrayList<>();
        Set<String> kode = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setString(1,noorder);
            ps.setString(2,noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!kode.add(rs.getString("kd_jenis_prw"))) {
                        throw new SQLException("Lebih dari satu hasil PA cocok untuk pemeriksaan "
                                + tampil(rs.getString("pemeriksaan")) + ". Periksa hubungan permintaan dan hasil PA.");
                    }
                    String nomor = teks(rs.getString("no_pa"));
                    if (nomor.isEmpty()) belum.add(tampil(rs.getString("pemeriksaan")));
                    rows.add(barisLabel(rs));
                }
            }
        }
        if (rows.isEmpty()) throw new SQLException("Tidak ada pemeriksaan pada permintaan PA yang dipilih.");
        if (!belum.isEmpty()) {
            throw new SQLException("Label belum dapat dicetak. No. PA belum diterbitkan atau belum terhubung "
                    + "ke permintaan ini untuk:\n- " + String.join("\n- ",belum)
                    + "\nTerbitkan No. PA melalui menu hasil pemeriksaan PA (Buat No. PA), "
                    + "atau periksa hubungan permintaan dan hasilnya.");
        }
        return rows;
    }

    public static void tampilkan(Connection c, String noorder, String noRawat) throws Exception {
        tampilkanBaris(data(c,noorder,noRawat));
    }

    public static void tampilkanHasil(Connection c, LabPAData.Kunci k) throws Exception {
        tampilkanBaris(dataHasil(c,k));
    }

    private static void tampilkanBaris(List<Map<String,?>> rows) throws Exception {
        File report = new File("report/rptLabelPermintaanLabPA.jasper");
        if (!report.isFile()) throw new java.io.IOException("File report/rptLabelPermintaanLabPA.jasper belum dipasang.");
        JasperPrint print = JasperFillManager.fillReport(report.getPath(),new HashMap<String,Object>(),
                new JRMapCollectionDataSource(rows));
        JasperViewer viewer = new JasperViewer(print,false);
        viewer.setTitle("::[ Label No. PA ]::");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        viewer.setSize(screen.width-50,screen.height-50);
        viewer.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
        viewer.setLocationRelativeTo(null);
        viewer.setVisible(true);
    }
}
