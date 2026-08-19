/*
 * Fitur koreksi data ranap: memisahkan data kunjungan kedua dari no_rawat lama
 * ke no_rawat baru berdasarkan Tanggal/Jam Kunjungan Kedua.
 * Dibuat terpisah agar tidak mengganggu alur DlgKamarInap yang sudah ada.
 */
package simrskhanza;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Form utilitas khusus admin untuk memisahkan kunjungan ranap yang terlanjur
 * tergabung pada satu no_rawat.
 *
 * Prinsip utama:
 * - Data SEBELUM Tanggal/Jam Kunjungan Kedua tetap di no_rawat lama.
 * - Data MULAI Tanggal/Jam Kunjungan Kedua dipindahkan ke no_rawat baru.
 * - Tabel yang tidak punya acuan tanggal/jam tidak dipindahkan otomatis.
 * - Semua proses update berjalan dalam transaction.
 * - Sebelum update, baris yang terdampak disalin ke tabel backup log.
 *
 * Catatan penggunaan:
 * No. rawat baru sebaiknya sudah dibuat dari modul registrasi Khanza terlebih dahulu,
 * lalu form ini hanya memindahkan data layanan berdasarkan batas tanggal/jam.
 *
 * @author IT RSAJ
 */
public class DlgPisahKunjunganRanap extends javax.swing.JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdfTanggalDB = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat sdfTanggalIndo = new SimpleDateFormat("dd-MM-yyyy");
    private DefaultTableModel tabMode;

    private final List<Rule> autoRules = new ArrayList<Rule>();
    private final String[] manualTables = new String[]{
        "diagnosa_pasien",
        "prosedur_pasien",
        "dpjp_ranap",
        "resume_pasien",
        "resume_pasien_ranap",
        "catatan_pasien",
        "berkas_digital_perawatan",
        "bridging_sep",
        "bridging_sep_internal",
        "nota_inap",
        "piutang_pasien"
    };

    private boolean sukses = false;

    public DlgPisahKunjunganRanap(Frame parent, boolean modal) {
        super(parent, modal);
        sdf.setLenient(false);
        sdfTanggalDB.setLenient(false);
        sdfTanggalIndo.setLenient(false);
        initRules();
        initComponents();
        initTable();
        setTanggalJam(new Date());
    }

    private void initTable() {
        tabMode = new DefaultTableModel(null, new Object[]{
            "Kelompok/Tabel", "Acuan", "Jumlah", "Status"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        tbPreview.setModel(tabMode);
        tbPreview.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tbPreview.getColumnModel().getColumn(0).setPreferredWidth(230);
        tbPreview.getColumnModel().getColumn(1).setPreferredWidth(360);
        tbPreview.getColumnModel().getColumn(2).setPreferredWidth(80);
        tbPreview.getColumnModel().getColumn(3).setPreferredWidth(300);
    }

    public void setData(String noRawatLama, String noRM, String namaPasien) {
        txtNoRawatLama.setText(noRawatLama == null ? "" : noRawatLama);
        txtNoRM.setText(noRM == null ? "" : noRM);
        txtNama.setText(namaPasien == null ? "" : namaPasien);
        setTanggalJam(cariDefaultBatasKunjunganKedua(noRawatLama));
        txtNoRawatBaru.requestFocus();
        lblInfo.setText("No.rawat baru harus sudah ada dari registrasi baru pasien yang sama.");
    }

    public boolean isSukses() {
        return sukses;
    }

    private void initRules() {
        autoRules.add(new Rule("kamar_inap", "no_rawat", "tgl_masuk", "jam_masuk", "Kamar Inap"));
        autoRules.add(new Rule("rawat_inap_dr", "no_rawat", "tgl_perawatan", "jam_rawat", "Tindakan Ranap Dokter"));
        autoRules.add(new Rule("rawat_inap_pr", "no_rawat", "tgl_perawatan", "jam_rawat", "Tindakan Ranap Perawat"));
        autoRules.add(new Rule("rawat_inap_drpr", "no_rawat", "tgl_perawatan", "jam_rawat", "Tindakan Ranap Dokter & Perawat"));
        autoRules.add(new Rule("pemeriksaan_ranap", "no_rawat", "tgl_perawatan", "jam_rawat", "Pemeriksaan Ranap"));

        autoRules.add(new Rule("detail_pemberian_obat", "no_rawat", "tgl_perawatan", "jam", "Pemberian Obat"));
        autoRules.add(new Rule("aturan_pakai", "no_rawat", "tgl_perawatan", "jam", "Aturan Pakai Obat"));
        autoRules.add(new Rule("resep_obat", "no_rawat", "tgl_peresepan", "jam_peresepan", "Resep Obat"));
        autoRules.add(new Rule("resep_pulang", "no_rawat", "tgl_perawatan", "jam", "Resep Pulang"));
        autoRules.add(new Rule("permintaan_resep_pulang", "no_rawat", "tgl_permintaan", "jam_permintaan", "Permintaan Resep Pulang"));
        autoRules.add(new Rule("stok_obat_pasien", "no_rawat", "tanggal", "jam", "Stok Obat Pasien"));
        autoRules.add(new Rule("permintaan_stok_obat_pasien", "no_rawat", "tgl_permintaan", "jam_permintaan", "Permintaan Stok Obat Pasien"));

        autoRules.add(new Rule("periksa_lab", "no_rawat", "tgl_periksa", "jam", "Periksa Laboratorium"));
        autoRules.add(new Rule("detail_periksa_lab", "no_rawat", "tgl_periksa", "jam", "Detail Laboratorium"));
        autoRules.add(new Rule("saran_kesan_lab", "no_rawat", "tgl_periksa", "jam", "Saran/Kesan Laboratorium"));
        autoRules.add(new Rule("permintaan_lab", "no_rawat", "tgl_permintaan", "jam_permintaan", "Permintaan Laboratorium"));
        autoRules.add(new Rule("periksa_lab_pa", "no_rawat", "tgl_periksa", "jam", "Periksa Lab PA"));
        autoRules.add(new Rule("detail_periksa_lab_pa", "no_rawat", "tgl_periksa", "jam", "Detail Lab PA"));
        autoRules.add(new Rule("periksa_lab_mb", "no_rawat", "tgl_periksa", "jam", "Periksa Lab MB"));
        autoRules.add(new Rule("detail_periksa_lab_mb", "no_rawat", "tgl_periksa", "jam", "Detail Lab MB"));

        autoRules.add(new Rule("periksa_radiologi", "no_rawat", "tgl_periksa", "jam", "Periksa Radiologi"));
        autoRules.add(new Rule("hasil_radiologi", "no_rawat", "tgl_periksa", "jam", "Hasil Radiologi"));
        autoRules.add(new Rule("gambar_radiologi", "no_rawat", "tgl_periksa", "jam", "Gambar Radiologi"));
        autoRules.add(new Rule("permintaan_radiologi", "no_rawat", "tgl_permintaan", "jam_permintaan", "Permintaan Radiologi"));

        autoRules.add(new Rule("operasi", "no_rawat", "tgl_operasi", null, "Operasi"));
        autoRules.add(new Rule("booking_operasi", "no_rawat", "tanggal", "jam_mulai", "Booking Operasi"));
        autoRules.add(new Rule("diet_pasien", "no_rawat", "tgl_perawatan", null, "Diet Pasien"));

        autoRules.add(new Rule("catatan_observasi_ranap", "no_rawat", "tgl_perawatan", "jam_rawat", "Catatan Observasi Ranap"));
        autoRules.add(new Rule("catatan_keperawatan_ranap", "no_rawat", "tanggal", "jam", "Catatan Keperawatan Ranap"));
        autoRules.add(new Rule("monitoring_asuhan_gizi", "no_rawat", "tanggal", "jam", "Monitoring Asuhan Gizi"));
        autoRules.add(new Rule("data_HAIs", "no_rawat", "tanggal", "jam", "Data HAIs"));
    }

    private String cariDefaultBatasKunjunganKedua(String noRawat) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return sdf.format(new Date());
        }
        String hasil = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "select concat(tgl_masuk,' ',jam_masuk) as batas from kamar_inap "
                    + "where no_rawat=? order by tgl_masuk,jam_masuk limit 1,1");
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                hasil = rs.getString("batas");
            }
        } catch (Exception e) {
            System.out.println("Cari default batas kunjungan kedua : " + e);
        } finally {
            close(rs);
            close(ps);
        }
        if (hasil == null || hasil.trim().equals("")) {
            hasil = sdf.format(new Date());
        }
        return hasil;
    }

    private void setTanggalJam(String batas) {
        try {
            setTanggalJam(sdf.parse(batas));
        } catch (Exception e) {
            setTanggalJam(new Date());
        }
    }

    private void setTanggalJam(Date tanggalJam) {
        if (tanggalJam == null) {
            tanggalJam = new Date();
        }
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[]{sdfTanggalIndo.format(tanggalJam)}));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setSelectedItem(sdfTanggalIndo.format(tanggalJam));
        cmbJam.setSelectedItem(new SimpleDateFormat("HH").format(tanggalJam));
        cmbMnt.setSelectedItem(new SimpleDateFormat("mm").format(tanggalJam));
        cmbDtk.setSelectedItem(new SimpleDateFormat("ss").format(tanggalJam));
    }

    private String getTanggalDB() {
        try {
            Date d = Tanggal.getDate();
            if (d != null) {
                return sdfTanggalDB.format(d);
            }
        } catch (Exception e) {
            // pakai fallback selected item
        }
        try {
            Object pilihan = Tanggal.getSelectedItem();
            if (pilihan instanceof Date) {
                return sdfTanggalDB.format((Date) pilihan);
            }
            String nilai = pilihan == null ? "" : pilihan.toString().trim();
            if (nilai.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return nilai;
            }
            return sdfTanggalDB.format(sdfTanggalIndo.parse(nilai));
        } catch (Exception e) {
            return "";
        }
    }

    private String getBatas() {
        String tanggal = getTanggalDB();
        if (tanggal.equals("")) {
            return "";
        }
        return tanggal + " " + cmbJam.getSelectedItem() + ":" + cmbMnt.getSelectedItem() + ":" + cmbDtk.getSelectedItem();
    }

    private void previewData() {
        try {
            String pesan = validasiInput(false);
            if (!pesan.equals("")) {
                JOptionPane.showMessageDialog(this, pesan);
                return;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            isiPreview();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal preview data : " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void isiPreview() throws Exception {
        tabMode.setRowCount(0);
        String noLama = txtNoRawatLama.getText().trim();
        String batas = getBatas();
        String tanggal = batas.substring(0, 10);
        int totalAuto = 0;
        int totalManual = 0;

        for (int i = 0; i < autoRules.size(); i++) {
            Rule r = autoRules.get(i);
            RuleState st = cekRule(r);
            if (!st.usable) {
                continue;
            }
            int jumlah = countByRule(r, noLama, batas, tanggal, st);
            if (jumlah > 0) {
                totalAuto += jumlah;
                tabMode.addRow(new Object[]{
                    r.label + " (" + r.table + ")",
                    getKondisiLabel(r, st),
                    jumlah,
                    st.usesTime ? "Otomatis, akurat tanggal & jam" : "Otomatis, tanggal saja - cek ulang data pada hari batas"
                });
            }
        }

        for (int i = 0; i < manualTables.length; i++) {
            String tbl = manualTables[i];
            if (tableExists(tbl) && columnExists(tbl, "no_rawat")) {
                int jml = countManual(tbl, noLama);
                if (jml > 0) {
                    totalManual += jml;
                    tabMode.addRow(new Object[]{
                        tbl,
                        "no_rawat = " + noLama,
                        jml,
                        "Tidak dipindah otomatis, tidak ada acuan tanggal/jam"
                    });
                }
            }
        }

        lblInfo.setText("Preview selesai. Data otomatis: " + totalAuto + " baris. Data perlu cek manual: " + totalManual + " baris.");
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ditemukan data setelah Tanggal/Jam Kunjungan Kedua pada no.rawat lama.");
        }
    }

    private void prosesPisah() {
        String pesan = validasiInput(true);
        if (!pesan.equals("")) {
            JOptionPane.showMessageDialog(this, pesan);
            return;
        }

        try {
            isiPreview();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membaca ulang preview : " + e.getMessage());
            return;
        }

        int totalAuto = hitungTotalAutoPreview();
        if (totalAuto <= 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data otomatis yang bisa dipindahkan berdasarkan Tanggal/Jam Kunjungan Kedua.");
            return;
        }

        if (Sequel.cariRegistrasi(txtNoRawatBaru.getText().trim()) > 0) {
            JOptionPane.showMessageDialog(this, "No.rawat baru sudah billing/registrasinya terverifikasi. Proses dibatalkan agar data billing tidak rusak.");
            return;
        }

        String warning = "Proses ini akan memindahkan " + totalAuto + " baris data dari no.rawat lama ke no.rawat baru.\n"
                + "Batas pemisah: " + getBatas() + "\n\n";
        if (Sequel.cariRegistrasi(txtNoRawatLama.getText().trim()) > 0) {
            warning += "PERINGATAN: No.rawat lama terdeteksi sudah terverifikasi billing.\n"
                    + "Pastikan kasir/keuangan mengetahui proses koreksi ini.\n\n";
        }
        warning += "Lanjutkan proses pisah kunjungan?";

        int pilih = JOptionPane.showConfirmDialog(this, warning, "Konfirmasi Pisah Kunjungan", JOptionPane.YES_NO_OPTION);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }

        long idLog = -1;
        boolean oldAutoCommit = true;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            buatTabelLogJikaBelumAda();
            idLog = insertLogMaster("PROSES", "Proses dimulai");

            oldAutoCommit = koneksi.getAutoCommit();
            koneksi.setAutoCommit(false);

            String noLama = txtNoRawatLama.getText().trim();
            String noBaru = txtNoRawatBaru.getText().trim();
            String batas = getBatas();
            String tanggal = batas.substring(0, 10);

            int totalPindah = 0;
            for (int i = 0; i < autoRules.size(); i++) {
                Rule r = autoRules.get(i);
                RuleState st = cekRule(r);
                if (!st.usable) {
                    continue;
                }
                int jumlah = countByRule(r, noLama, batas, tanggal, st);
                if (jumlah <= 0) {
                    continue;
                }
                backupRows(idLog, r, noLama, noBaru, batas, tanggal, st);
                int update = updateByRule(r, noLama, noBaru, batas, tanggal, st);
                insertLogDetail(idLog, r.table, update, getKondisiLabel(r, st));
                totalPindah += update;
            }

            updateStatusRegPeriksaBaru(noBaru);

            koneksi.commit();
            koneksi.setAutoCommit(oldAutoCommit);
            updateLogMaster(idLog, "SUKSES", "Berhasil memindahkan " + totalPindah + " baris data.");
            sukses = true;
            JOptionPane.showMessageDialog(this, "Proses pisah kunjungan berhasil.\nTotal data dipindahkan: " + totalPindah + " baris.\nLog ID: " + idLog);
            dispose();
        } catch (Exception e) {
            try {
                koneksi.rollback();
            } catch (Exception ex) {
                System.out.println("Rollback gagal : " + ex);
            }
            try {
                koneksi.setAutoCommit(oldAutoCommit);
            } catch (Exception ex) {
                System.out.println("Set auto commit gagal : " + ex);
            }
            if (idLog > 0) {
                updateLogMaster(idLog, "GAGAL", e.toString());
            }
            JOptionPane.showMessageDialog(this, "Proses gagal, semua perubahan dibatalkan.\n" + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String validasiInput(boolean wajibAlasan) {
        String batas = getBatas();
        if (txtNoRawatLama.getText().trim().equals("")) {
            return "No.rawat lama masih kosong.";
        }
        if (txtNoRawatBaru.getText().trim().equals("")) {
            return "No.rawat baru masih kosong.";
        }
        if (txtNoRawatLama.getText().trim().equals(txtNoRawatBaru.getText().trim())) {
            return "No.rawat baru tidak boleh sama dengan no.rawat lama.";
        }
        if (batas.equals("")) {
            return "Tanggal/Jam Kunjungan Kedua masih kosong atau tidak valid.";
        }
        try {
            sdf.parse(batas);
        } catch (Exception e) {
            return "Format Tanggal/Jam Kunjungan Kedua tidak valid.";
        }
        if (wajibAlasan && txtAlasan.getText().trim().equals("")) {
            return "Alasan wajib diisi untuk kebutuhan log/backup koreksi.";
        }

        String rmLama = cariNoRM(txtNoRawatLama.getText().trim());
        String rmBaru = cariNoRM(txtNoRawatBaru.getText().trim());
        if (rmLama.equals("")) {
            return "No.rawat lama tidak ditemukan di reg_periksa.";
        }
        if (rmBaru.equals("")) {
            return "No.rawat baru tidak ditemukan di reg_periksa. Buat registrasi baru terlebih dahulu.";
        }
        if (!rmLama.equals(rmBaru)) {
            return "No.rawat lama dan no.rawat baru bukan pasien yang sama.\nRM lama: " + rmLama + "\nRM baru: " + rmBaru;
        }
        return "";
    }

    private int hitungTotalAutoPreview() {
        int total = 0;
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            String status = String.valueOf(tabMode.getValueAt(i, 3));
            if (status.startsWith("Otomatis")) {
                try {
                    total += Integer.parseInt(String.valueOf(tabMode.getValueAt(i, 2)));
                } catch (Exception e) {
                    // abaikan parsing error preview
                }
            }
        }
        return total;
    }

    private String cariNoRM(String noRawat) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select no_rkm_medis from reg_periksa where no_rawat=?");
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            System.out.println("Cari no RM : " + e);
        } finally {
            close(rs);
            close(ps);
        }
        return "";
    }

    private boolean tableExists(String tableName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select count(*) from information_schema.TABLES where TABLE_SCHEMA=database() and TABLE_NAME=?");
            ps.setString(1, tableName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println("Cek tabel " + tableName + " : " + e);
        } finally {
            close(rs);
            close(ps);
        }
        return false;
    }

    private boolean columnExists(String tableName, String columnName) {
        if (columnName == null || columnName.trim().equals("")) {
            return false;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select count(*) from information_schema.COLUMNS where TABLE_SCHEMA=database() and TABLE_NAME=? and COLUMN_NAME=?");
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println("Cek kolom " + tableName + "." + columnName + " : " + e);
        } finally {
            close(rs);
            close(ps);
        }
        return false;
    }

    private String columnType(String tableName, String columnName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select DATA_TYPE from information_schema.COLUMNS where TABLE_SCHEMA=database() and TABLE_NAME=? and COLUMN_NAME=?");
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1) == null ? "" : rs.getString(1).toLowerCase();
            }
        } catch (Exception e) {
            System.out.println("Cek tipe kolom " + tableName + "." + columnName + " : " + e);
        } finally {
            close(rs);
            close(ps);
        }
        return "";
    }

    private RuleState cekRule(Rule r) {
        RuleState st = new RuleState();
        st.usable = false;
        if (!tableExists(r.table) || !columnExists(r.table, r.noRawatCol) || !columnExists(r.table, r.dateCol)) {
            return st;
        }
        st.dateType = columnType(r.table, r.dateCol);
        st.usesTime = r.timeCol != null && columnExists(r.table, r.timeCol);
        st.dateIsDateTime = st.dateType.contains("datetime") || st.dateType.contains("timestamp");
        st.usable = true;
        return st;
    }

    private String getWhereSql(Rule r, RuleState st) {
        if (st.usesTime) {
            return "`" + r.noRawatCol + "`=? and concat(`" + r.dateCol + "`,' ',`" + r.timeCol + "`) >= ?";
        }
        if (st.dateIsDateTime) {
            return "`" + r.noRawatCol + "`=? and `" + r.dateCol + "` >= ?";
        }
        return "`" + r.noRawatCol + "`=? and `" + r.dateCol + "` >= ?";
    }

    private String getParameterBatas(String batas, String tanggal, RuleState st) {
        if (st.usesTime || st.dateIsDateTime) {
            return batas;
        }
        return tanggal;
    }

    private String getKondisiLabel(Rule r, RuleState st) {
        String batas = getBatas();
        if (st.usesTime) {
            return r.dateCol + " + " + r.timeCol + " >= " + batas;
        }
        if (st.dateIsDateTime) {
            return r.dateCol + " >= " + batas;
        }
        return r.dateCol + " >= " + batas.substring(0, 10) + " (kolom jam tidak tersedia)";
    }

    private int countByRule(Rule r, String noRawat, String batas, String tanggal, RuleState st) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select count(*) from `" + r.table + "` where " + getWhereSql(r, st));
            ps.setString(1, noRawat);
            ps.setString(2, getParameterBatas(batas, tanggal, st));
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            close(rs);
            close(ps);
        }
        return 0;
    }

    private int countManual(String tableName, String noRawat) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select count(*) from `" + tableName + "` where `no_rawat`=?");
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            close(rs);
            close(ps);
        }
        return 0;
    }

    private int updateByRule(Rule r, String noLama, String noBaru, String batas, String tanggal, RuleState st) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement("update `" + r.table + "` set `" + r.noRawatCol + "`=? where " + getWhereSql(r, st));
            ps.setString(1, noBaru);
            ps.setString(2, noLama);
            ps.setString(3, getParameterBatas(batas, tanggal, st));
            return ps.executeUpdate();
        } finally {
            close(ps);
        }
    }

    private void backupRows(long idLog, Rule r, String noLama, String noBaru, String batas, String tanggal, RuleState st) throws SQLException {
        PreparedStatement psSelect = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;
        try {
            psSelect = koneksi.prepareStatement("select * from `" + r.table + "` where " + getWhereSql(r, st));
            psSelect.setString(1, noLama);
            psSelect.setString(2, getParameterBatas(batas, tanggal, st));
            rs = psSelect.executeQuery();

            psInsert = koneksi.prepareStatement(
                    "insert into rsaj_backup_pisah_kunjungan_ranap "
                    + "(id_log,nama_tabel,no_rawat_lama,no_rawat_baru,tgl_jam_batas,data_teks,waktu_backup) "
                    + "values (?,?,?,?,?,?,now())");
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                StringBuilder data = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        data.append(" | ");
                    }
                    data.append(md.getColumnLabel(i)).append("=").append(rs.getString(i));
                }
                psInsert.setLong(1, idLog);
                psInsert.setString(2, r.table);
                psInsert.setString(3, noLama);
                psInsert.setString(4, noBaru);
                psInsert.setString(5, batas);
                psInsert.setString(6, data.toString());
                psInsert.addBatch();
            }
            psInsert.executeBatch();
        } finally {
            close(rs);
            close(psSelect);
            close(psInsert);
        }
    }

    private void updateStatusRegPeriksaBaru(String noBaru) throws SQLException {
        if (!tableExists("reg_periksa") || !columnExists("reg_periksa", "status_lanjut")) {
            return;
        }
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement("update reg_periksa set status_lanjut='Ranap' where no_rawat=?");
            ps.setString(1, noBaru);
            ps.executeUpdate();
        } finally {
            close(ps);
        }
    }

    private void buatTabelLogJikaBelumAda() throws SQLException {
        Statement st = null;
        try {
            st = koneksi.createStatement();
            st.executeUpdate("create table if not exists rsaj_log_pisah_kunjungan_ranap ("
                    + "id_log bigint auto_increment primary key,"
                    + "no_rawat_lama varchar(17) not null,"
                    + "no_rawat_baru varchar(17) not null,"
                    + "no_rkm_medis varchar(15) not null,"
                    + "tgl_jam_batas datetime not null,"
                    + "user_proses varchar(100) not null,"
                    + "alasan text,"
                    + "waktu_proses datetime not null,"
                    + "status varchar(20) not null,"
                    + "keterangan text"
                    + ") engine=InnoDB default charset=latin1");

            st.executeUpdate("create table if not exists rsaj_log_pisah_kunjungan_ranap_detail ("
                    + "id_detail bigint auto_increment primary key,"
                    + "id_log bigint not null,"
                    + "nama_tabel varchar(100) not null,"
                    + "jumlah_data int not null default 0,"
                    + "kondisi_pindah text"
                    + ") engine=InnoDB default charset=latin1");

            st.executeUpdate("create table if not exists rsaj_backup_pisah_kunjungan_ranap ("
                    + "id_backup bigint auto_increment primary key,"
                    + "id_log bigint not null,"
                    + "nama_tabel varchar(100) not null,"
                    + "no_rawat_lama varchar(17) not null,"
                    + "no_rawat_baru varchar(17) not null,"
                    + "tgl_jam_batas datetime not null,"
                    + "data_teks longtext,"
                    + "waktu_backup datetime not null"
                    + ") engine=InnoDB default charset=latin1");
        } finally {
            close(st);
        }
    }

    private long insertLogMaster(String status, String keterangan) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "insert into rsaj_log_pisah_kunjungan_ranap "
                    + "(no_rawat_lama,no_rawat_baru,no_rkm_medis,tgl_jam_batas,user_proses,alasan,waktu_proses,status,keterangan) "
                    + "values (?,?,?,?,?,?,now(),?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, txtNoRawatLama.getText().trim());
            ps.setString(2, txtNoRawatBaru.getText().trim());
            ps.setString(3, txtNoRM.getText().trim());
            ps.setString(4, getBatas());
            ps.setString(5, getUserProses());
            ps.setString(6, txtAlasan.getText().trim());
            ps.setString(7, status);
            ps.setString(8, keterangan);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } finally {
            close(rs);
            close(ps);
        }
        return Long.parseLong(Sequel.cariIsi("select last_insert_id()"));
    }

    private void insertLogDetail(long idLog, String tabel, int jumlah, String kondisi) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                    "insert into rsaj_log_pisah_kunjungan_ranap_detail "
                    + "(id_log,nama_tabel,jumlah_data,kondisi_pindah) values (?,?,?,?)");
            ps.setLong(1, idLog);
            ps.setString(2, tabel);
            ps.setInt(3, jumlah);
            ps.setString(4, kondisi);
            ps.executeUpdate();
        } finally {
            close(ps);
        }
    }

    private void updateLogMaster(long idLog, String status, String keterangan) {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement("update rsaj_log_pisah_kunjungan_ranap set status=?, keterangan=? where id_log=?");
            ps.setString(1, status);
            ps.setString(2, keterangan);
            ps.setLong(3, idLog);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Update log pisah kunjungan gagal : " + e);
        } finally {
            close(ps);
        }
    }

    private String getUserProses() {
        try {
            if (akses.getkode() != null && !akses.getkode().trim().equals("")) {
                return akses.getkode();
            }
        } catch (Exception e) {
            System.out.println("User akses kosong : " + e);
        }
        return System.getProperty("user.name", "unknown");
    }

    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            // abaikan
        }
    }

    private void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (Exception e) {
            // abaikan
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        lblJudul = new widget.Label();
        panelForm = new widget.PanelBiasa();
        lblNoRawatLama = new widget.Label();
        txtNoRawatLama = new widget.TextBox();
        lblNoRM = new widget.Label();
        txtNoRM = new widget.TextBox();
        lblNamaPasien = new widget.Label();
        txtNama = new widget.TextBox();
        lblNoRawatBaru = new widget.Label();
        txtNoRawatBaru = new widget.TextBox();
        lblTanggal = new widget.Label();
        Tanggal = new widget.Tanggal();
        lblJam = new widget.Label();
        cmbJam = new widget.ComboBox();
        cmbMnt = new widget.ComboBox();
        cmbDtk = new widget.ComboBox();
        lblAlasan = new widget.Label();
        scrollAlasan = new widget.ScrollPane();
        txtAlasan = new widget.TextArea();
        lblKet = new widget.Label();
        scrollPreview = new widget.ScrollPane();
        tbPreview = new widget.Table();
        panelBawah = new widget.panelisi();
        lblInfo = new widget.Label();
        btnPreview = new widget.Button();
        btnProses = new widget.Button();
        btnTutup = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pisah Kunjungan Ranap");
        setMinimumSize(new java.awt.Dimension(1020, 620));
        setName("DlgPisahKunjunganRanap"); // NOI18N
        setResizable(false);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(1020, 620));
        setType(java.awt.Window.Type.POPUP);

        internalFrame1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 225, 205)));
        internalFrame1.setMinimumSize(new java.awt.Dimension(1020, 620));
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1020, 620));
        internalFrame1.setLayout(null);

        lblJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblJudul.setText("Pisah Kunjungan Ranap Berdasarkan Tanggal/Jam Kunjungan Kedua");
        lblJudul.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblJudul.setName("lblJudul"); // NOI18N
        internalFrame1.add(lblJudul);
        lblJudul.setBounds(15, 10, 700, 23);

        panelForm.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Pemisahan", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        panelForm.setName("panelForm"); // NOI18N
        panelForm.setLayout(null);

        lblNoRawatLama.setText("No. Rawat Lama :");
        lblNoRawatLama.setName("lblNoRawatLama"); // NOI18N
        panelForm.add(lblNoRawatLama);
        lblNoRawatLama.setBounds(10, 20, 105, 23);

        txtNoRawatLama.setEditable(false);
        txtNoRawatLama.setHighlighter(null);
        txtNoRawatLama.setName("txtNoRawatLama"); // NOI18N
        panelForm.add(txtNoRawatLama);
        txtNoRawatLama.setBounds(120, 20, 250, 23);

        lblNoRM.setText("No. RM :");
        lblNoRM.setName("lblNoRM"); // NOI18N
        panelForm.add(lblNoRM);
        lblNoRM.setBounds(390, 20, 70, 23);

        txtNoRM.setEditable(false);
        txtNoRM.setHighlighter(null);
        txtNoRM.setName("txtNoRM"); // NOI18N
        panelForm.add(txtNoRM);
        txtNoRM.setBounds(465, 20, 510, 23);

        lblNamaPasien.setText("Nama Pasien :");
        lblNamaPasien.setName("lblNamaPasien"); // NOI18N
        panelForm.add(lblNamaPasien);
        lblNamaPasien.setBounds(10, 50, 105, 23);

        txtNama.setEditable(false);
        txtNama.setHighlighter(null);
        txtNama.setName("txtNama"); // NOI18N
        panelForm.add(txtNama);
        txtNama.setBounds(120, 50, 855, 23);

        lblNoRawatBaru.setText("No. Rawat Baru :");
        lblNoRawatBaru.setName("lblNoRawatBaru"); // NOI18N
        panelForm.add(lblNoRawatBaru);
        lblNoRawatBaru.setBounds(10, 80, 105, 23);

        txtNoRawatBaru.setHighlighter(null);
        txtNoRawatBaru.setName("txtNoRawatBaru"); // NOI18N
        panelForm.add(txtNoRawatBaru);
        txtNoRawatBaru.setBounds(120, 80, 250, 23);

        lblTanggal.setText("Tanggal :");
        lblTanggal.setName("lblTanggal"); // NOI18N
        panelForm.add(lblTanggal);
        lblTanggal.setBounds(390, 80, 70, 23);

        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-06-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.setPreferredSize(new java.awt.Dimension(95, 23));
        panelForm.add(Tanggal);
        Tanggal.setBounds(465, 80, 115, 23);

        lblJam.setText("Jam :");
        lblJam.setName("lblJam"); // NOI18N
        panelForm.add(lblJam);
        lblJam.setBounds(590, 80, 45, 23);

        cmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cmbJam.setName("cmbJam"); // NOI18N
        panelForm.add(cmbJam);
        cmbJam.setBounds(635, 80, 55, 23);

        cmbMnt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbMnt.setName("cmbMnt"); // NOI18N
        panelForm.add(cmbMnt);
        cmbMnt.setBounds(695, 80, 55, 23);

        cmbDtk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbDtk.setName("cmbDtk"); // NOI18N
        panelForm.add(cmbDtk);
        cmbDtk.setBounds(755, 80, 55, 23);

        lblAlasan.setText("Alasan :");
        lblAlasan.setName("lblAlasan"); // NOI18N
        panelForm.add(lblAlasan);
        lblAlasan.setBounds(10, 110, 105, 23);

        scrollAlasan.setName("scrollAlasan"); // NOI18N

        txtAlasan.setColumns(20);
        txtAlasan.setRows(3);
        txtAlasan.setName("txtAlasan"); // NOI18N
        scrollAlasan.setViewportView(txtAlasan);

        panelForm.add(scrollAlasan);
        scrollAlasan.setBounds(120, 110, 855, 65);

        lblKet.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKet.setText("Semua data pada no.rawat lama mulai dari Tanggal/Jam Kunjungan Kedua dipindahkan ke no.rawat baru. Data sebelumnya tetap di no.rawat lama.");
        lblKet.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        lblKet.setName("lblKet"); // NOI18N
        panelForm.add(lblKet);
        lblKet.setBounds(10, 180, 965, 23);

        internalFrame1.add(panelForm);
        panelForm.setBounds(10, 40, 1000, 210);

        scrollPreview.setName("scrollPreview"); // NOI18N

        tbPreview.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kelompok/Tabel", "Acuan", "Jumlah", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbPreview.setName("tbPreview"); // NOI18N
        scrollPreview.setViewportView(tbPreview);

        internalFrame1.add(scrollPreview);
        scrollPreview.setBounds(10, 260, 1000, 300);

        panelBawah.setName("panelBawah"); // NOI18N
        panelBawah.setPreferredSize(new java.awt.Dimension(1000, 45));
        panelBawah.setLayout(null);

        lblInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblInfo.setText("Isi tanggal dan jam kunjungan kedua, lalu klik Preview Data.");
        lblInfo.setName("lblInfo"); // NOI18N
        panelBawah.add(lblInfo);
        lblInfo.setBounds(0, 10, 620, 23);

        btnPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnPreview.setMnemonic('P');
        btnPreview.setText("Preview Data");
        btnPreview.setToolTipText("Alt+P");
        btnPreview.setName("btnPreview"); // NOI18N
        btnPreview.setPreferredSize(new java.awt.Dimension(120, 30));
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });
        panelBawah.add(btnPreview);
        btnPreview.setBounds(635, 7, 120, 30);

        btnProses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        btnProses.setMnemonic('S');
        btnProses.setText("Proses Pisah");
        btnProses.setToolTipText("Alt+S");
        btnProses.setName("btnProses"); // NOI18N
        btnProses.setPreferredSize(new java.awt.Dimension(120, 30));
        btnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProsesActionPerformed(evt);
            }
        });
        panelBawah.add(btnProses);
        btnProses.setBounds(760, 7, 120, 30);

        btnTutup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        btnTutup.setMnemonic('T');
        btnTutup.setText("Tutup");
        btnTutup.setToolTipText("Alt+T");
        btnTutup.setName("btnTutup"); // NOI18N
        btnTutup.setPreferredSize(new java.awt.Dimension(110, 30));
        btnTutup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTutupActionPerformed(evt);
            }
        });
        panelBawah.add(btnTutup);
        btnTutup.setBounds(885, 7, 110, 30);

        internalFrame1.add(panelBawah);
        panelBawah.setBounds(10, 565, 1000, 45);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewActionPerformed
        previewData();
    }//GEN-LAST:event_btnPreviewActionPerformed

    private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed
        prosesPisah();
    }//GEN-LAST:event_btnProsesActionPerformed

    private void btnTutupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTutupActionPerformed
        dispose();
    }//GEN-LAST:event_btnTutupActionPerformed

    private static class Rule {
        String table;
        String noRawatCol;
        String dateCol;
        String timeCol;
        String label;

        Rule(String table, String noRawatCol, String dateCol, String timeCol, String label) {
            this.table = table;
            this.noRawatCol = noRawatCol;
            this.dateCol = dateCol;
            this.timeCol = timeCol;
            this.label = label;
        }
    }

    private static class RuleState {
        boolean usable;
        boolean usesTime;
        boolean dateIsDateTime;
        String dateType;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Tanggal Tanggal;
    private widget.Button btnPreview;
    private widget.Button btnProses;
    private widget.Button btnTutup;
    private widget.ComboBox cmbDtk;
    private widget.ComboBox cmbJam;
    private widget.ComboBox cmbMnt;
    private widget.InternalFrame internalFrame1;
    private widget.Label lblAlasan;
    private widget.Label lblInfo;
    private widget.Label lblJam;
    private widget.Label lblJudul;
    private widget.Label lblKet;
    private widget.Label lblNamaPasien;
    private widget.Label lblNoRM;
    private widget.Label lblNoRawatBaru;
    private widget.Label lblNoRawatLama;
    private widget.Label lblTanggal;
    private widget.panelisi panelBawah;
    private widget.PanelBiasa panelForm;
    private widget.ScrollPane scrollAlasan;
    private widget.ScrollPane scrollPreview;
    private widget.Table tbPreview;
    private widget.TextArea txtAlasan;
    private widget.TextBox txtNama;
    private widget.TextBox txtNoRM;
    private widget.TextBox txtNoRawatBaru;
    private widget.TextBox txtNoRawatLama;
    // End of variables declaration//GEN-END:variables
}
