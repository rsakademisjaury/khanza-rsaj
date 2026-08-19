/*
 * DaftarPasienBaru.java
 *
 * Form MINI untuk daftar pasien baru dari rujukan masuk.
 * Dipanggil dari SatuSehatRujukanMasuk.java kalau pasien rujukan tidak ditemukan
 * di tabel pasien lokal.
 *
 * Hanya mengisi field MINIMUM yang dibutuhkan oleh tabel pasien:
 *   no_rkm_medis, nm_pasien, no_ktp, tgl_lahir, jk, alamat
 *
 * User bisa lengkapi data pasien yang lain nanti via menu Master Pasien Khanza.
 *
 * @author SIMRS Khanza Bridging
 */

package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

public final class DaftarPasienBaru extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();

    // Hasil: no_rkm_medis baru, atau null kalau dibatalkan
    private String noRkmMedisHasil = null;

    // Form fields
    private widget.TextBox tNoRkmMedis;       // auto-generated, read-only
    private widget.TextBox tNoKtp;
    private widget.TextBox tNamaPasien;
    private widget.TextBox tTglLahir;          // format yyyy-MM-dd
    private widget.ComboBox cbJk;
    private widget.TextBox tAlamat;
    private widget.TextBox tKota;              // kelurahan/kota
    private widget.TextBox tNamaIbu;
    private widget.TextBox tNoTlp;
    private widget.ComboBox cbAgama;
    private widget.ComboBox cbSttsKawin;

    private widget.Button btnSimpan, btnBatal;
    private widget.Label lblStatus;

    public DaftarPasienBaru(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(560, 480);
        setLocationRelativeTo(parent);
        // Generate no_rkm_medis preview
        tNoRkmMedis.setText(generateNoRkmMedis());
    }

    /**
     * Pre-fill form dengan data dari rujukan masuk.
     * Dipanggil sebelum setVisible(true).
     */
    public void preFill(String noKtp, String nama, String tglLahir, String jk) {
        if (noKtp != null) tNoKtp.setText(noKtp);
        if (nama != null) tNamaPasien.setText(nama);
        if (tglLahir != null) tTglLahir.setText(tglLahir);
        if ("L".equals(jk)) cbJk.setSelectedIndex(0);
        else if ("P".equals(jk)) cbJk.setSelectedIndex(1);
    }

    /**
     * @return no_rkm_medis baru kalau berhasil simpan, atau null kalau dibatalkan.
     */
    public String getNoRkmMedisHasil() {
        return noRkmMedisHasil;
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Daftar Pasien Baru (dari Rujukan Masuk)");

        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder(
                "::[ Daftar Pasien Baru - Data Minimum ]::"));
        frame.setLayout(new BorderLayout(2, 2));

        // ===== Center: form fields =====
        widget.PanelBiasa panel = new widget.PanelBiasa();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(540, 380));

        int x1 = 15, x2 = 145, w1 = 125, w2 = 350, h = 23, gap = 28;
        int y = 15;

        addLbl(panel, "No.RM (auto):", x1, y, w1, h);
        tNoRkmMedis = new widget.TextBox();
        tNoRkmMedis.setEditable(false);
        tNoRkmMedis.setBackground(new java.awt.Color(245, 245, 220));
        tNoRkmMedis.setBounds(x2, y, 150, h);
        panel.add(tNoRkmMedis);
        y += gap;

        addLbl(panel, "NIK (KTP):", x1, y, w1, h);
        tNoKtp = new widget.TextBox();
        tNoKtp.setBounds(x2, y, w2, h);
        panel.add(tNoKtp);
        y += gap;

        addLbl(panel, "Nama Pasien *:", x1, y, w1, h);
        tNamaPasien = new widget.TextBox();
        tNamaPasien.setBounds(x2, y, w2, h);
        panel.add(tNamaPasien);
        y += gap;

        addLbl(panel, "Tgl Lahir *:", x1, y, w1, h);
        tTglLahir = new widget.TextBox();
        tTglLahir.setBounds(x2, y, 150, h);
        panel.add(tTglLahir);
        addLbl(panel, "(yyyy-MM-dd)", x2 + 155, y, 100, h);
        y += gap;

        addLbl(panel, "Jenis Kelamin *:", x1, y, w1, h);
        cbJk = new widget.ComboBox();
        cbJk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Laki-laki", "Perempuan"}));
        cbJk.setBounds(x2, y, 150, h);
        panel.add(cbJk);
        y += gap;

        addLbl(panel, "Alamat:", x1, y, w1, h);
        tAlamat = new widget.TextBox();
        tAlamat.setBounds(x2, y, w2, h);
        panel.add(tAlamat);
        y += gap;

        addLbl(panel, "Kota/Kelurahan:", x1, y, w1, h);
        tKota = new widget.TextBox();
        tKota.setBounds(x2, y, w2, h);
        panel.add(tKota);
        y += gap;

        addLbl(panel, "Nama Ibu:", x1, y, w1, h);
        tNamaIbu = new widget.TextBox();
        tNamaIbu.setBounds(x2, y, w2, h);
        panel.add(tNamaIbu);
        y += gap;

        addLbl(panel, "No.Telp:", x1, y, w1, h);
        tNoTlp = new widget.TextBox();
        tNoTlp.setBounds(x2, y, 200, h);
        panel.add(tNoTlp);
        y += gap;

        addLbl(panel, "Agama:", x1, y, w1, h);
        cbAgama = new widget.ComboBox();
        cbAgama.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                "ISLAM", "PROTESTAN", "KATOLIK", "HINDU", "BUDHA", "KONGHUCU", "LAINNYA"
        }));
        cbAgama.setBounds(x2, y, 150, h);
        panel.add(cbAgama);

        addLbl(panel, "Status Kawin:", x2 + 165, y, 90, h);
        cbSttsKawin = new widget.ComboBox();
        cbSttsKawin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                "BELUM MENIKAH", "MENIKAH", "JANDA", "DUDA"
        }));
        cbSttsKawin.setBounds(x2 + 260, y, 130, h);
        panel.add(cbSttsKawin);

        // Tip
        widget.Label lblTip = new widget.Label();
        lblTip.setText("* = wajib diisi. Field lain bisa dilengkapi nanti via Master Pasien.");
        lblTip.setBounds(x1, y + gap + 5, 510, h);
        lblTip.setForeground(new java.awt.Color(80, 80, 80));
        panel.add(lblTip);

        frame.add(panel, BorderLayout.CENTER);

        // ===== Bottom: actions =====
        widget.PanelBiasa pnlBottom = new widget.PanelBiasa();
        pnlBottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlBottom.setPreferredSize(new Dimension(540, 50));

        lblStatus = new widget.Label();
        lblStatus.setText(" ");
        lblStatus.setPreferredSize(new Dimension(280, 22));
        lblStatus.setForeground(new java.awt.Color(0, 100, 0));
        pnlBottom.add(lblStatus);

        btnSimpan = mkBtn("Simpan", "/picture/save-16x16.png");
        btnSimpan.setPreferredSize(new Dimension(110, 28));
        btnSimpan.addActionListener(e -> doSimpan());
        pnlBottom.add(btnSimpan);

        btnBatal = mkBtn("Batal", "/picture/exit.png");
        btnBatal.setPreferredSize(new Dimension(110, 28));
        btnBatal.addActionListener(e -> {
            noRkmMedisHasil = null;
            dispose();
        });
        pnlBottom.add(btnBatal);

        frame.add(pnlBottom, BorderLayout.SOUTH);

        getContentPane().add(frame, BorderLayout.CENTER);
        pack();
    }

    private void addLbl(widget.PanelBiasa parent, String text, int x, int y, int w, int h) {
        widget.Label l = new widget.Label();
        l.setText(text);
        l.setBounds(x, y, w, h);
        parent.add(l);
    }

    private widget.Button mkBtn(String text, String icon) {
        widget.Button b = new widget.Button();
        b.setText(text);
        try { b.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon))); } catch (Exception ig) {}
        return b;
    }

    private void setStatus(String msg, boolean error) {
        lblStatus.setText(msg);
        lblStatus.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
    }

    /**
     * Generate no_rkm_medis baru dengan format Khanza standar.
     * Pattern: 6 digit angka, increment dari max(no_rkm_medis).
     */
    private String generateNoRkmMedis() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select max(cast(no_rkm_medis as unsigned)) from pasien")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long last = rs.getLong(1);
                    long next = last + 1;
                    return String.format("%06d", next);
                }
            }
        } catch (Exception e) {
            System.out.println("Generate no_rkm_medis err: " + e);
        }
        return "000001";
    }

    private void doSimpan() {
        // Validasi minimal
        String nama = tNamaPasien.getText().trim();
        String tglLahir = tTglLahir.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Pasien wajib diisi.");
            return;
        }
        if (tglLahir.isEmpty() || !tglLahir.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Tgl Lahir wajib diisi dengan format yyyy-MM-dd (contoh: 1990-05-15).");
            return;
        }

        String nik = tNoKtp.getText().trim();
        // Cek duplikasi NIK kalau diisi
        if (!nik.isEmpty()) {
            String existingRm = Sequel.cariIsi(
                    "select no_rkm_medis from pasien where no_ktp=?", nik);
            if (existingRm != null && !existingRm.isEmpty()) {
                int opt = JOptionPane.showConfirmDialog(this,
                        "NIK " + nik + " sudah terdaftar atas pasien:\n"
                        + "No.RM " + existingRm + "\n\n"
                        + "Pakai No.RM existing ini?",
                        "NIK Duplikat", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    noRkmMedisHasil = existingRm;
                    dispose();
                }
                return;
            }
        }

        String noRkmMedis = tNoRkmMedis.getText().trim();
        String jk = (cbJk.getSelectedIndex() == 0) ? "L" : "P";
        String alamat = tAlamat.getText().trim();
        String kota = tKota.getText().trim();
        String namaIbu = tNamaIbu.getText().trim();
        String noTlp = tNoTlp.getText().trim();
        String agama = String.valueOf(cbAgama.getSelectedItem());
        String sttsKawin = String.valueOf(cbSttsKawin.getSelectedItem());

        // Hitung umur
        int umur = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dob = sdf.parse(tglLahir);
            long diffMs = System.currentTimeMillis() - dob.getTime();
            umur = (int) (diffMs / (1000L * 60 * 60 * 24 * 365));
            if (umur < 0) umur = 0;
        } catch (Exception ignored) {}

        // Insert ke tabel pasien (DISESUAIKAN dengan struktur Khanza)
        // Field minimum yang ada di hampir semua versi Khanza:
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into pasien (no_rkm_medis, nm_pasien, no_ktp, jk, tmp_lahir, tgl_lahir, "
                + "nm_ibu, alamat, gol_darah, pekerjaan, stts_nikah, agama, tgl_daftar, no_tlp, "
                + "umur, pnd, keluarga, namakeluarga, kd_pj, no_peserta, kd_kel, kd_kec, kd_kab, "
                + "pekerjaanpj, alamatpj, kelurahanpj, kecamatanpj, kabupatenpj, perusahaan_pasien, "
                + "suku_bangsa, bahasa_pasien, cacat_fisik, kd_prop, email, nip) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, '-', '-', ?, ?, curdate(), ?, "
                + "?, '-', 'LAINNYA', '-', 'A01', '', '0', '0', '0', "
                + "'-', '-', '0', '0', '0', '0', "
                + "'0', '0', 'TIDAK ADA', '0', '-', '-')")) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, nama);
            ps.setString(3, nik.isEmpty() ? "0" : nik);
            ps.setString(4, jk);
            ps.setString(5, kota.isEmpty() ? "-" : kota);
            ps.setString(6, tglLahir);
            ps.setString(7, namaIbu.isEmpty() ? "-" : namaIbu);
            ps.setString(8, alamat.isEmpty() ? "-" : alamat);
            ps.setString(9, sttsKawin);
            ps.setString(10, agama);
            ps.setString(11, noTlp.isEmpty() ? "-" : noTlp);
            ps.setString(12, String.valueOf(umur));
            ps.executeUpdate();

            noRkmMedisHasil = noRkmMedis;
            JOptionPane.showMessageDialog(this,
                    "Pasien berhasil didaftarkan!\n\n"
                    + "No.RM: " + noRkmMedis + "\n"
                    + "Nama : " + nama + "\n\n"
                    + "Catatan: data minimum sudah disimpan. Silakan lengkapi data lain "
                    + "(pekerjaan, gol darah, kontak penanggung jawab, dll) "
                    + "via menu Master Pasien Khanza nanti.");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Gagal simpan pasien:\n" + e.getMessage()
                    + "\n\nKemungkinan field di tabel pasien Anda berbeda dengan default.\n"
                    + "Cek doSimpan() di DaftarPasienBaru.java dan sesuaikan SQL insert.");
            setStatus("Error: " + e.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            DaftarPasienBaru dlg = new DaftarPasienBaru(new javax.swing.JFrame(), true);
            dlg.preFill("3273123456780001", "Budi Santoso", "1990-05-15", "L");
            dlg.setVisible(true);
            System.out.println("Hasil: " + dlg.getNoRkmMedisHasil());
        });
    }
}