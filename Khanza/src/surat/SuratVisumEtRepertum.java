package surat;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * Builder-ready form untuk Surat Visum Et Repertum.
 * Dibuat agar pasangan .java dan .form lebih mudah dibuka di NetBeans GUI Builder.
 */
public class SuratVisumEtRepertum extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final SimpleDateFormat tanggalJam = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat tanggalSaja = new SimpleDateFormat("yyyy-MM-dd");

    private Long idVisum = null;
    private String noRawat = "";
    private String noRkmMedis = "";
    private String namaPasien = "";
    private String kdDokter = "";
    private String namaDokter = "";

    private DefaultTableModel tabMode;
    private DefaultTableModel tabLampiran;

    public SuratVisumEtRepertum(Frame parent, boolean modal) {
        super(parent, modal);
        tanggalJam.setLenient(false);
        tanggalSaja.setLenient(false);
        initComponents();
        initAfterBuilder();
        emptTeks();
    }

    public void isCek() {
        // Placeholder agar konsisten dengan pola dialog surat lain di Khanza.
    }

    public void emptTeks() {
        idVisum = null;
        txtNoSuratVer.setText(generateNoSurat());
        txtNoSuratPermintaan.setText("");
        txtTglTerimaSPV.setText(tanggalJam.format(new java.util.Date()));
        txtPihakPembuatSPV.setText("");
        txtJenisPemeriksaan.setText("");
        cmbStatusData.setSelectedItem("DRAFT");

        txtTempatPemeriksaan.setText("");
        txtTglPemeriksaan.setText(tanggalJam.format(new java.util.Date()));
        txtNamaIdentitas.setText(namaPasien);
        txtTglLahir.setText("");
        txtUmur.setText("");
        txtAlamat.setText("");
        txtNoBuktiIdentitas.setText("");
        txtAnamnesis.setText("");

        txtKesadaran.setText("");
        txtDenyutNadi.setText("");
        txtPernapasan.setText("");
        txtTekananDarah.setText("");
        txtSuhuTubuh.setText("");
        txtPakaian.setText("");
        txtTinggiBadan.setText("");
        txtBeratBadan.setText("");
        txtCiriKhusus.setText("");
        txtKepala.setText("");
        txtLeher.setText("");
        txtBahu.setText("");
        txtDada.setText("");
        txtPunggung.setText("");
        txtPerut.setText("");
        txtPinggang.setText("");
        txtBokong.setText("");
        txtDubur.setText("");
        txtAlatKelamin.setText("");
        txtAnggotaGerakAtas.setText("");
        txtAnggotaGerakBawah.setText("");

        txtLaboratorium.setText("");
        txtRadiologi.setText("");
        txtOdontogram.setText("");
        txtLainLain.setText("");
        txtRingkasan.setText("");
        txtKesimpulan.setText("");
        txtTempatKeluar.setText(akses.getkabupatenrs());
        txtTglKeluar.setText(tanggalSaja.format(new java.util.Date()));
        txtKdDokter.setText(kdDokter);
        txtNamaDokter.setText(namaDokter);
        txtNipDokter.setText("");
        txtJabatanKompetensi.setText("");

        resetLampiran();
        isiDataPasienDariRegistrasi();
        tbData.clearSelection();
    }

    public void tampil() {
        tabMode.setRowCount(0);
        if (noRawat.trim().equals("")) {
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                "select id,no_surat_ver,ifnull(tgl_pemeriksaan,created_at) as tgl_periksa,status_data,ifnull(kesimpulan,'') as kesimpulan " +
                "from surat_visum_et_repertum where no_rawat=? order by created_at desc"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    Long.valueOf(rs.getLong("id")),
                    rs.getString("no_surat_ver"),
                    rs.getString("tgl_periksa"),
                    rs.getString("status_data"),
                    potong(rs.getString("kesimpulan"), 120)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data Visum Et Repertum:\n" + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
    }

    public void setNoRm(String noRawat, String noRkmMedis, String namaPasien, String kdDokter, String namaDokter, java.util.Date tanggalAwal, java.util.Date tanggalAkhir) {
        this.noRawat = safe(noRawat);
        this.noRkmMedis = safe(noRkmMedis);
        this.namaPasien = safe(namaPasien);
        this.kdDokter = safe(kdDokter);
        this.namaDokter = safe(namaDokter);

        txtNoRawatInfo.setText(this.noRawat);
        txtNoRMInfo.setText(this.noRkmMedis);
        txtNamaInfo.setText(this.namaPasien);
        txtDokterInfo.setText(this.namaDokter);

        txtKdDokter.setText(this.kdDokter);
        txtNamaDokter.setText(this.namaDokter);
        txtNamaIdentitas.setText(this.namaPasien);

        isiDataPasienDariRegistrasi();
        tampil();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelInfo = new widget.PanelBiasa();
        lblNoRawatInfo = new widget.Label();
        lblNoRMInfo = new widget.Label();
        lblNamaInfo = new widget.Label();
        lblDokterInfo = new widget.Label();
        txtNoRawatInfo = new widget.TextBox();
        txtNoRMInfo = new widget.TextBox();
        txtNamaInfo = new widget.TextBox();
        txtDokterInfo = new widget.TextBox();
        panelList = new widget.PanelBiasa();
        lblDaftarData = new widget.Label();
        scrollTbData = new widget.ScrollPane();
        tbData = new widget.Table();
        tabMain = new widget.TabPane();
        scrollSPV = new widget.ScrollPane();
        panelSPVInner = new widget.PanelBiasa();
        lblNoSuratVer = new widget.Label();
        txtNoSuratVer = new widget.TextBox();
        lblNoSuratPermintaan = new widget.Label();
        txtNoSuratPermintaan = new widget.TextBox();
        lblTglTerimaSPV = new widget.Label();
        txtTglTerimaSPV = new widget.TextBox();
        lblPihakPembuatSPV = new widget.Label();
        scrollPihakPembuatSPV = new widget.ScrollPane();
        txtPihakPembuatSPV = new widget.TextArea();
        lblJenisPemeriksaan = new widget.Label();
        txtJenisPemeriksaan = new widget.TextBox();
        lblStatusData = new widget.Label();
        cmbStatusData = new widget.ComboBox();
        scrollIdentitas = new widget.ScrollPane();
        panelIdentitasInner = new widget.PanelBiasa();
        lblTempatPemeriksaan = new widget.Label();
        txtTempatPemeriksaan = new widget.TextBox();
        lblTglPemeriksaan = new widget.Label();
        txtTglPemeriksaan = new widget.TextBox();
        lblNamaIdentitas = new widget.Label();
        txtNamaIdentitas = new widget.TextBox();
        lblTglLahir = new widget.Label();
        txtTglLahir = new widget.TextBox();
        lblUmur = new widget.Label();
        txtUmur = new widget.TextBox();
        lblAlamat = new widget.Label();
        scrollAlamat = new widget.ScrollPane();
        txtAlamat = new widget.TextArea();
        lblNoBuktiIdentitas = new widget.Label();
        txtNoBuktiIdentitas = new widget.TextBox();
        lblAnamnesis = new widget.Label();
        scrollAnamnesis = new widget.ScrollPane();
        txtAnamnesis = new widget.TextArea();
        scrollFisis = new widget.ScrollPane();
        panelFisisInner = new widget.PanelBiasa();
        lblKesadaran = new widget.Label();
        txtKesadaran = new widget.TextBox();
        lblDenyutNadi = new widget.Label();
        txtDenyutNadi = new widget.TextBox();
        lblPernapasan = new widget.Label();
        txtPernapasan = new widget.TextBox();
        lblTekananDarah = new widget.Label();
        txtTekananDarah = new widget.TextBox();
        lblSuhuTubuh = new widget.Label();
        txtSuhuTubuh = new widget.TextBox();
        lblPakaian = new widget.Label();
        scrollPakaian = new widget.ScrollPane();
        txtPakaian = new widget.TextArea();
        lblTinggiBadan = new widget.Label();
        txtTinggiBadan = new widget.TextBox();
        lblBeratBadan = new widget.Label();
        txtBeratBadan = new widget.TextBox();
        lblCiriKhusus = new widget.Label();
        scrollCiriKhusus = new widget.ScrollPane();
        txtCiriKhusus = new widget.TextArea();
        lblKepala = new widget.Label();
        scrollKepala = new widget.ScrollPane();
        txtKepala = new widget.TextArea();
        lblLeher = new widget.Label();
        scrollLeher = new widget.ScrollPane();
        txtLeher = new widget.TextArea();
        lblBahu = new widget.Label();
        scrollBahu = new widget.ScrollPane();
        txtBahu = new widget.TextArea();
        lblDada = new widget.Label();
        scrollDada = new widget.ScrollPane();
        txtDada = new widget.TextArea();
        lblPunggung = new widget.Label();
        scrollPunggung = new widget.ScrollPane();
        txtPunggung = new widget.TextArea();
        lblPerut = new widget.Label();
        scrollPerut = new widget.ScrollPane();
        txtPerut = new widget.TextArea();
        lblPinggang = new widget.Label();
        scrollPinggang = new widget.ScrollPane();
        txtPinggang = new widget.TextArea();
        lblBokong = new widget.Label();
        scrollBokong = new widget.ScrollPane();
        txtBokong = new widget.TextArea();
        lblDubur = new widget.Label();
        scrollDubur = new widget.ScrollPane();
        txtDubur = new widget.TextArea();
        lblAlatKelamin = new widget.Label();
        scrollAlatKelamin = new widget.ScrollPane();
        txtAlatKelamin = new widget.TextArea();
        lblAnggotaGerakAtas = new widget.Label();
        scrollAnggotaGerakAtas = new widget.ScrollPane();
        txtAnggotaGerakAtas = new widget.TextArea();
        lblAnggotaGerakBawah = new widget.Label();
        scrollAnggotaGerakBawah = new widget.ScrollPane();
        txtAnggotaGerakBawah = new widget.TextArea();
        scrollPenunjang = new widget.ScrollPane();
        panelPenunjangInner = new widget.PanelBiasa();
        lblLaboratorium = new widget.Label();
        scrollLaboratorium = new widget.ScrollPane();
        txtLaboratorium = new widget.TextArea();
        lblRadiologi = new widget.Label();
        scrollRadiologi = new widget.ScrollPane();
        txtRadiologi = new widget.TextArea();
        lblOdontogram = new widget.Label();
        scrollOdontogram = new widget.ScrollPane();
        txtOdontogram = new widget.TextArea();
        lblLainLain = new widget.Label();
        scrollLainLain = new widget.ScrollPane();
        txtLainLain = new widget.TextArea();
        lblRingkasan = new widget.Label();
        scrollRingkasan = new widget.ScrollPane();
        txtRingkasan = new widget.TextArea();
        lblKesimpulan = new widget.Label();
        scrollKesimpulan = new widget.ScrollPane();
        txtKesimpulan = new widget.TextArea();
        lblTempatKeluar = new widget.Label();
        txtTempatKeluar = new widget.TextBox();
        lblTglKeluar = new widget.Label();
        txtTglKeluar = new widget.TextBox();
        lblKdDokter = new widget.Label();
        txtKdDokter = new widget.TextBox();
        lblNamaDokter = new widget.Label();
        txtNamaDokter = new widget.TextBox();
        lblNipDokter = new widget.Label();
        txtNipDokter = new widget.TextBox();
        lblJabatanKompetensi = new widget.Label();
        scrollJabatanKompetensi = new widget.ScrollPane();
        txtJabatanKompetensi = new widget.TextArea();
        lblLampiran = new widget.Label();
        scrollTbLampiran = new widget.ScrollPane();
        tbLampiran = new widget.Table();
        panelTombol = new widget.PanelBiasa();
        btnBaru = new widget.Button();
        btnSimpan = new widget.Button();
        btnHapus = new widget.Button();
        btnCetak = new widget.Button();
        btnTutup = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Surat Visum Et Repertum");
        setBackground(new java.awt.Color(255, 255, 255));
        setName("SuratVisumEtRepertum"); // NOI18N
        getContentPane().setLayout(null);

        panelInfo.setBackground(new java.awt.Color(255, 255, 255));
        panelInfo.setLayout(null);

        lblNoRawatInfo.setText("No. Rawat");
        panelInfo.add(lblNoRawatInfo);
        lblNoRawatInfo.setBounds(10, 12, 160, 23);

        lblNoRMInfo.setText("No. RM");
        panelInfo.add(lblNoRMInfo);
        lblNoRMInfo.setBounds(10, 42, 160, 23);

        lblNamaInfo.setText("Nama Pasien");
        panelInfo.add(lblNamaInfo);
        lblNamaInfo.setBounds(420, 12, 130, 23);

        lblDokterInfo.setText("Dokter IGD");
        panelInfo.add(lblDokterInfo);
        lblDokterInfo.setBounds(420, 42, 130, 23);

        txtNoRawatInfo.setEditable(false);
        txtNoRawatInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNoRawatInfoActionPerformed(evt);
            }
        });
        panelInfo.add(txtNoRawatInfo);
        txtNoRawatInfo.setBounds(170, 12, 220, 23);

        txtNoRMInfo.setEditable(false);
        panelInfo.add(txtNoRMInfo);
        txtNoRMInfo.setBounds(170, 42, 220, 23);

        txtNamaInfo.setEditable(false);
        panelInfo.add(txtNamaInfo);
        txtNamaInfo.setBounds(560, 12, 390, 23);

        txtDokterInfo.setEditable(false);
        panelInfo.add(txtDokterInfo);
        txtDokterInfo.setBounds(560, 42, 390, 23);

        getContentPane().add(panelInfo);
        panelInfo.setBounds(10, 10, 1150, 80);

        panelList.setBackground(new java.awt.Color(255, 255, 255));
        panelList.setLayout(null);

        lblDaftarData.setText("Daftar Visum Et Repertum per No. Rawat");
        panelList.add(lblDaftarData);
        lblDaftarData.setBounds(10, 10, 270, 23);

        scrollTbData.setViewportView(tbData);

        panelList.add(scrollTbData);
        scrollTbData.setBounds(10, 40, 280, 485);

        getContentPane().add(panelList);
        panelList.setBounds(10, 100, 300, 540);

        panelSPVInner.setBackground(new java.awt.Color(255, 255, 255));
        panelSPVInner.setLayout(null);

        lblNoSuratVer.setText("No. Surat Keterangan VeR");
        panelSPVInner.add(lblNoSuratVer);
        lblNoSuratVer.setBounds(10, 12, 190, 23);
        panelSPVInner.add(txtNoSuratVer);
        txtNoSuratVer.setBounds(210, 12, 420, 23);

        lblNoSuratPermintaan.setText("No. Surat Permintaan VeR");
        panelSPVInner.add(lblNoSuratPermintaan);
        lblNoSuratPermintaan.setBounds(10, 45, 190, 23);
        panelSPVInner.add(txtNoSuratPermintaan);
        txtNoSuratPermintaan.setBounds(210, 45, 420, 23);

        lblTglTerimaSPV.setText("Tgl/Jam SPV diterima");
        panelSPVInner.add(lblTglTerimaSPV);
        lblTglTerimaSPV.setBounds(10, 78, 190, 23);
        panelSPVInner.add(txtTglTerimaSPV);
        txtTglTerimaSPV.setBounds(210, 78, 420, 23);

        lblPihakPembuatSPV.setText("Pihak pembuat SPV");
        panelSPVInner.add(lblPihakPembuatSPV);
        lblPihakPembuatSPV.setBounds(10, 111, 190, 23);

        txtPihakPembuatSPV.setColumns(20);
        txtPihakPembuatSPV.setRows(5);
        scrollPihakPembuatSPV.setViewportView(txtPihakPembuatSPV);

        panelSPVInner.add(scrollPihakPembuatSPV);
        scrollPihakPembuatSPV.setBounds(210, 111, 420, 50);

        lblJenisPemeriksaan.setText("Jenis pemeriksaan diminta");
        panelSPVInner.add(lblJenisPemeriksaan);
        lblJenisPemeriksaan.setBounds(10, 207, 190, 23);
        panelSPVInner.add(txtJenisPemeriksaan);
        txtJenisPemeriksaan.setBounds(210, 207, 420, 23);

        lblStatusData.setText("Status Data");
        panelSPVInner.add(lblStatusData);
        lblStatusData.setBounds(10, 240, 190, 23);

        cmbStatusData.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DRAFT", "FINAL" }));
        panelSPVInner.add(cmbStatusData);
        cmbStatusData.setBounds(210, 240, 180, 23);

        scrollSPV.setViewportView(panelSPVInner);

        tabMain.addTab("SPV", scrollSPV);

        panelIdentitasInner.setBackground(new java.awt.Color(255, 255, 255));
        panelIdentitasInner.setLayout(null);

        lblTempatPemeriksaan.setText("Tempat pemeriksaan");
        panelIdentitasInner.add(lblTempatPemeriksaan);
        lblTempatPemeriksaan.setBounds(10, 12, 190, 23);
        panelIdentitasInner.add(txtTempatPemeriksaan);
        txtTempatPemeriksaan.setBounds(210, 12, 420, 23);

        lblTglPemeriksaan.setText("Tgl/Jam pemeriksaan");
        panelIdentitasInner.add(lblTglPemeriksaan);
        lblTglPemeriksaan.setBounds(10, 45, 190, 23);
        panelIdentitasInner.add(txtTglPemeriksaan);
        txtTglPemeriksaan.setBounds(210, 45, 420, 23);

        lblNamaIdentitas.setText("Nama pasien/korban");
        panelIdentitasInner.add(lblNamaIdentitas);
        lblNamaIdentitas.setBounds(10, 78, 190, 23);
        panelIdentitasInner.add(txtNamaIdentitas);
        txtNamaIdentitas.setBounds(210, 78, 420, 23);

        lblTglLahir.setText("Tanggal lahir (yyyy-MM-dd)");
        panelIdentitasInner.add(lblTglLahir);
        lblTglLahir.setBounds(10, 111, 190, 23);
        panelIdentitasInner.add(txtTglLahir);
        txtTglLahir.setBounds(210, 111, 420, 23);

        lblUmur.setText("Umur");
        panelIdentitasInner.add(lblUmur);
        lblUmur.setBounds(10, 144, 190, 23);
        panelIdentitasInner.add(txtUmur);
        txtUmur.setBounds(210, 144, 420, 23);

        lblAlamat.setText("Alamat");
        panelIdentitasInner.add(lblAlamat);
        lblAlamat.setBounds(10, 177, 190, 23);

        txtAlamat.setColumns(20);
        txtAlamat.setRows(5);
        scrollAlamat.setViewportView(txtAlamat);

        panelIdentitasInner.add(scrollAlamat);
        scrollAlamat.setBounds(210, 177, 420, 90);

        lblNoBuktiIdentitas.setText("No. bukti identitas");
        panelIdentitasInner.add(lblNoBuktiIdentitas);
        lblNoBuktiIdentitas.setBounds(10, 277, 190, 23);
        panelIdentitasInner.add(txtNoBuktiIdentitas);
        txtNoBuktiIdentitas.setBounds(210, 277, 420, 23);

        lblAnamnesis.setText("Anamnesis");
        panelIdentitasInner.add(lblAnamnesis);
        lblAnamnesis.setBounds(10, 310, 190, 23);

        txtAnamnesis.setColumns(20);
        txtAnamnesis.setRows(5);
        scrollAnamnesis.setViewportView(txtAnamnesis);

        panelIdentitasInner.add(scrollAnamnesis);
        scrollAnamnesis.setBounds(210, 310, 420, 140);

        scrollIdentitas.setViewportView(panelIdentitasInner);

        tabMain.addTab("Identitas & Pemeriksaan", scrollIdentitas);

        panelFisisInner.setLayout(null);

        lblKesadaran.setText("Kesadaran");
        panelFisisInner.add(lblKesadaran);
        lblKesadaran.setBounds(10, 12, 190, 23);
        panelFisisInner.add(txtKesadaran);
        txtKesadaran.setBounds(210, 12, 420, 23);

        lblDenyutNadi.setText("Denyut nadi");
        panelFisisInner.add(lblDenyutNadi);
        lblDenyutNadi.setBounds(10, 45, 190, 23);
        panelFisisInner.add(txtDenyutNadi);
        txtDenyutNadi.setBounds(210, 45, 420, 23);

        lblPernapasan.setText("Pernapasan");
        panelFisisInner.add(lblPernapasan);
        lblPernapasan.setBounds(10, 78, 190, 23);
        panelFisisInner.add(txtPernapasan);
        txtPernapasan.setBounds(210, 78, 420, 23);

        lblTekananDarah.setText("Tekanan darah");
        panelFisisInner.add(lblTekananDarah);
        lblTekananDarah.setBounds(10, 111, 190, 23);
        panelFisisInner.add(txtTekananDarah);
        txtTekananDarah.setBounds(210, 111, 420, 23);

        lblSuhuTubuh.setText("Suhu tubuh");
        panelFisisInner.add(lblSuhuTubuh);
        lblSuhuTubuh.setBounds(10, 144, 190, 23);
        panelFisisInner.add(txtSuhuTubuh);
        txtSuhuTubuh.setBounds(210, 144, 420, 23);

        lblPakaian.setText("Pakaian");
        panelFisisInner.add(lblPakaian);
        lblPakaian.setBounds(10, 177, 190, 23);

        txtPakaian.setColumns(20);
        txtPakaian.setRows(5);
        scrollPakaian.setViewportView(txtPakaian);

        panelFisisInner.add(scrollPakaian);
        scrollPakaian.setBounds(210, 177, 420, 70);

        lblTinggiBadan.setText("Tinggi badan");
        panelFisisInner.add(lblTinggiBadan);
        lblTinggiBadan.setBounds(10, 257, 190, 23);
        panelFisisInner.add(txtTinggiBadan);
        txtTinggiBadan.setBounds(210, 257, 420, 23);

        lblBeratBadan.setText("Berat badan");
        panelFisisInner.add(lblBeratBadan);
        lblBeratBadan.setBounds(10, 290, 190, 23);
        panelFisisInner.add(txtBeratBadan);
        txtBeratBadan.setBounds(210, 290, 420, 23);

        lblCiriKhusus.setText("Ciri khusus");
        panelFisisInner.add(lblCiriKhusus);
        lblCiriKhusus.setBounds(10, 323, 190, 23);

        txtCiriKhusus.setColumns(20);
        txtCiriKhusus.setRows(5);
        scrollCiriKhusus.setViewportView(txtCiriKhusus);

        panelFisisInner.add(scrollCiriKhusus);
        scrollCiriKhusus.setBounds(210, 323, 420, 70);

        lblKepala.setText("Kepala");
        panelFisisInner.add(lblKepala);
        lblKepala.setBounds(10, 403, 190, 23);

        txtKepala.setColumns(20);
        txtKepala.setRows(5);
        scrollKepala.setViewportView(txtKepala);

        panelFisisInner.add(scrollKepala);
        scrollKepala.setBounds(210, 403, 420, 70);

        lblLeher.setText("Leher");
        panelFisisInner.add(lblLeher);
        lblLeher.setBounds(10, 483, 190, 23);

        txtLeher.setColumns(20);
        txtLeher.setRows(5);
        scrollLeher.setViewportView(txtLeher);

        panelFisisInner.add(scrollLeher);
        scrollLeher.setBounds(210, 483, 420, 70);

        lblBahu.setText("Bahu");
        panelFisisInner.add(lblBahu);
        lblBahu.setBounds(10, 563, 190, 23);

        txtBahu.setColumns(20);
        txtBahu.setRows(5);
        scrollBahu.setViewportView(txtBahu);

        panelFisisInner.add(scrollBahu);
        scrollBahu.setBounds(210, 563, 420, 70);

        lblDada.setText("Dada");
        panelFisisInner.add(lblDada);
        lblDada.setBounds(10, 643, 190, 23);

        txtDada.setColumns(20);
        txtDada.setRows(5);
        scrollDada.setViewportView(txtDada);

        panelFisisInner.add(scrollDada);
        scrollDada.setBounds(210, 643, 420, 70);

        lblPunggung.setText("Punggung");
        panelFisisInner.add(lblPunggung);
        lblPunggung.setBounds(10, 723, 190, 23);

        txtPunggung.setColumns(20);
        txtPunggung.setRows(5);
        scrollPunggung.setViewportView(txtPunggung);

        panelFisisInner.add(scrollPunggung);
        scrollPunggung.setBounds(210, 723, 420, 70);

        lblPerut.setText("Perut");
        panelFisisInner.add(lblPerut);
        lblPerut.setBounds(10, 803, 190, 23);

        txtPerut.setColumns(20);
        txtPerut.setRows(5);
        scrollPerut.setViewportView(txtPerut);

        panelFisisInner.add(scrollPerut);
        scrollPerut.setBounds(210, 803, 420, 70);

        lblPinggang.setText("Pinggang");
        panelFisisInner.add(lblPinggang);
        lblPinggang.setBounds(10, 883, 190, 23);

        txtPinggang.setColumns(20);
        txtPinggang.setRows(5);
        scrollPinggang.setViewportView(txtPinggang);

        panelFisisInner.add(scrollPinggang);
        scrollPinggang.setBounds(210, 883, 420, 70);

        lblBokong.setText("Bokong");
        panelFisisInner.add(lblBokong);
        lblBokong.setBounds(10, 963, 190, 23);

        txtBokong.setColumns(20);
        txtBokong.setRows(5);
        scrollBokong.setViewportView(txtBokong);

        panelFisisInner.add(scrollBokong);
        scrollBokong.setBounds(210, 963, 420, 70);

        lblDubur.setText("Dubur");
        panelFisisInner.add(lblDubur);
        lblDubur.setBounds(10, 1043, 190, 23);

        txtDubur.setColumns(20);
        txtDubur.setRows(5);
        scrollDubur.setViewportView(txtDubur);

        panelFisisInner.add(scrollDubur);
        scrollDubur.setBounds(210, 1043, 420, 70);

        lblAlatKelamin.setText("Alat kelamin");
        panelFisisInner.add(lblAlatKelamin);
        lblAlatKelamin.setBounds(10, 1123, 190, 23);

        txtAlatKelamin.setColumns(20);
        txtAlatKelamin.setRows(5);
        scrollAlatKelamin.setViewportView(txtAlatKelamin);

        panelFisisInner.add(scrollAlatKelamin);
        scrollAlatKelamin.setBounds(210, 1123, 420, 70);

        lblAnggotaGerakAtas.setText("Anggota gerak atas");
        panelFisisInner.add(lblAnggotaGerakAtas);
        lblAnggotaGerakAtas.setBounds(10, 1203, 190, 23);

        txtAnggotaGerakAtas.setColumns(20);
        txtAnggotaGerakAtas.setRows(5);
        scrollAnggotaGerakAtas.setViewportView(txtAnggotaGerakAtas);

        panelFisisInner.add(scrollAnggotaGerakAtas);
        scrollAnggotaGerakAtas.setBounds(210, 1203, 420, 70);

        lblAnggotaGerakBawah.setText("Anggota gerak bawah");
        panelFisisInner.add(lblAnggotaGerakBawah);
        lblAnggotaGerakBawah.setBounds(10, 1283, 190, 23);

        txtAnggotaGerakBawah.setColumns(20);
        txtAnggotaGerakBawah.setRows(5);
        scrollAnggotaGerakBawah.setViewportView(txtAnggotaGerakBawah);

        panelFisisInner.add(scrollAnggotaGerakBawah);
        scrollAnggotaGerakBawah.setBounds(210, 1283, 420, 70);

        scrollFisis.setViewportView(panelFisisInner);

        tabMain.addTab("Pemeriksaan Fisis", scrollFisis);

        panelPenunjangInner.setLayout(null);

        lblLaboratorium.setText("Laboratorium");
        panelPenunjangInner.add(lblLaboratorium);
        lblLaboratorium.setBounds(10, 12, 190, 23);

        txtLaboratorium.setColumns(20);
        txtLaboratorium.setRows(5);
        scrollLaboratorium.setViewportView(txtLaboratorium);

        panelPenunjangInner.add(scrollLaboratorium);
        scrollLaboratorium.setBounds(210, 12, 420, 70);

        lblRadiologi.setText("Radiologi");
        panelPenunjangInner.add(lblRadiologi);
        lblRadiologi.setBounds(10, 92, 190, 23);

        txtRadiologi.setColumns(20);
        txtRadiologi.setRows(5);
        scrollRadiologi.setViewportView(txtRadiologi);

        panelPenunjangInner.add(scrollRadiologi);
        scrollRadiologi.setBounds(210, 92, 420, 70);

        lblOdontogram.setText("Odontogram");
        panelPenunjangInner.add(lblOdontogram);
        lblOdontogram.setBounds(10, 172, 190, 23);

        txtOdontogram.setColumns(20);
        txtOdontogram.setRows(5);
        scrollOdontogram.setViewportView(txtOdontogram);

        panelPenunjangInner.add(scrollOdontogram);
        scrollOdontogram.setBounds(210, 172, 420, 70);

        lblLainLain.setText("Lain-lain");
        panelPenunjangInner.add(lblLainLain);
        lblLainLain.setBounds(10, 252, 190, 23);

        txtLainLain.setColumns(20);
        txtLainLain.setRows(5);
        scrollLainLain.setViewportView(txtLainLain);

        panelPenunjangInner.add(scrollLainLain);
        scrollLainLain.setBounds(210, 252, 420, 70);

        lblRingkasan.setText("Ringkasan pemeriksaan");
        panelPenunjangInner.add(lblRingkasan);
        lblRingkasan.setBounds(10, 332, 190, 23);

        txtRingkasan.setColumns(20);
        txtRingkasan.setRows(5);
        scrollRingkasan.setViewportView(txtRingkasan);

        panelPenunjangInner.add(scrollRingkasan);
        scrollRingkasan.setBounds(210, 332, 420, 90);

        lblKesimpulan.setText("Kesimpulan");
        panelPenunjangInner.add(lblKesimpulan);
        lblKesimpulan.setBounds(10, 432, 190, 23);

        txtKesimpulan.setColumns(20);
        txtKesimpulan.setRows(5);
        scrollKesimpulan.setViewportView(txtKesimpulan);

        panelPenunjangInner.add(scrollKesimpulan);
        scrollKesimpulan.setBounds(210, 432, 420, 90);

        lblTempatKeluar.setText("Tempat keluar VeR");
        panelPenunjangInner.add(lblTempatKeluar);
        lblTempatKeluar.setBounds(10, 532, 190, 23);
        panelPenunjangInner.add(txtTempatKeluar);
        txtTempatKeluar.setBounds(210, 532, 420, 23);

        lblTglKeluar.setText("Tgl keluar VeR (yyyy-MM-dd)");
        panelPenunjangInner.add(lblTglKeluar);
        lblTglKeluar.setBounds(10, 565, 190, 23);
        panelPenunjangInner.add(txtTglKeluar);
        txtTglKeluar.setBounds(210, 565, 420, 23);

        lblKdDokter.setText("Kode dokter");
        panelPenunjangInner.add(lblKdDokter);
        lblKdDokter.setBounds(10, 598, 190, 23);
        panelPenunjangInner.add(txtKdDokter);
        txtKdDokter.setBounds(210, 598, 420, 23);

        lblNamaDokter.setText("Nama dokter");
        panelPenunjangInner.add(lblNamaDokter);
        lblNamaDokter.setBounds(10, 631, 190, 23);
        panelPenunjangInner.add(txtNamaDokter);
        txtNamaDokter.setBounds(210, 631, 420, 23);

        lblNipDokter.setText("NIP dokter");
        panelPenunjangInner.add(lblNipDokter);
        lblNipDokter.setBounds(10, 664, 190, 23);
        panelPenunjangInner.add(txtNipDokter);
        txtNipDokter.setBounds(210, 664, 420, 23);

        lblJabatanKompetensi.setText("Jabatan / kompetensi");
        panelPenunjangInner.add(lblJabatanKompetensi);
        lblJabatanKompetensi.setBounds(10, 697, 190, 23);

        txtJabatanKompetensi.setColumns(20);
        txtJabatanKompetensi.setRows(5);
        scrollJabatanKompetensi.setViewportView(txtJabatanKompetensi);

        panelPenunjangInner.add(scrollJabatanKompetensi);
        scrollJabatanKompetensi.setBounds(210, 697, 420, 70);

        lblLampiran.setText("Lampiran Pemeriksaan");
        panelPenunjangInner.add(lblLampiran);
        lblLampiran.setBounds(10, 780, 190, 23);

        scrollTbLampiran.setViewportView(tbLampiran);

        panelPenunjangInner.add(scrollTbLampiran);
        scrollTbLampiran.setBounds(210, 780, 420, 150);

        scrollPenunjang.setViewportView(panelPenunjangInner);

        tabMain.addTab("Penunjang & Penutup", scrollPenunjang);

        getContentPane().add(tabMain);
        tabMain.setBounds(320, 100, 840, 540);

        panelTombol.setBackground(new java.awt.Color(255, 255, 255));
        panelTombol.setLayout(null);

        btnBaru.setText("Baru");
        panelTombol.add(btnBaru);
        btnBaru.setBounds(520, 8, 100, 28);

        btnSimpan.setText("Simpan / Ubah");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });
        panelTombol.add(btnSimpan);
        btnSimpan.setBounds(625, 8, 130, 28);

        btnHapus.setText("Hapus");
        panelTombol.add(btnHapus);
        btnHapus.setBounds(760, 8, 100, 28);

        btnCetak.setText("Preview VeR");
        panelTombol.add(btnCetak);
        btnCetak.setBounds(865, 8, 110, 28);

        btnTutup.setText("Tutup");
        panelTombol.add(btnTutup);
        btnTutup.setBounds(980, 8, 100, 28);

        getContentPane().add(panelTombol);
        panelTombol.setBounds(10, 650, 1150, 45);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNoRawatInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNoRawatInfoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNoRawatInfoActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void initAfterBuilder() {
        tabMode = new DefaultTableModel(new Object[]{"ID", "No. Surat VeR", "Tgl/Jam Pemeriksaan", "Status", "Kesimpulan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbData.setModel(tabMode);
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbData.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tbData.getSelectedRow() > -1) {
                    Object obj = tbData.getValueAt(tbData.getSelectedRow(), 0);
                    if (obj != null) {
                        loadData(((Long) obj).longValue());
                    }
                }
            }
        });
        if (tbData.getColumnModel().getColumnCount() > 0) {
            tbData.getColumnModel().getColumn(0).setMinWidth(0);
            tbData.getColumnModel().getColumn(0).setMaxWidth(0);
            tbData.getColumnModel().getColumn(0).setWidth(0);
        }

        tabLampiran = new DefaultTableModel(new Object[]{"Jenis Lampiran", "Keterangan", "File Path"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };
        tbLampiran.setModel(tabLampiran);
        resetLampiran();

        btnBaru.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emptTeks();
            }
        });
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpan();
            }
        });
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapus();
            }
        });
        btnCetak.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preview();
            }
        });
        btnTutup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
    }

    private void simpan() {
        if (noRawat.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "No. rawat kosong. Pilih pasien dulu dari DlgIGD.");
            return;
        }
        if (txtNoSuratVer.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "No. Surat Keterangan VeR belum diisi.");
            txtNoSuratVer.requestFocus();
            return;
        }
        if (txtNamaIdentitas.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Nama pasien/korban belum diisi.");
            txtNamaIdentitas.requestFocus();
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (idVisum == null) {
                ps = koneksi.prepareStatement(
                    "insert into surat_visum_et_repertum(" +
                    "no_rawat,no_rkm_medis,no_surat_ver,no_surat_permintaan,tgl_terima_spv,pihak_pembuat_spv,jenis_pemeriksaan_diminta," +
                    "tempat_pemeriksaan,tgl_pemeriksaan,nama_pasien,tgl_lahir,umur,alamat,no_bukti_identitas,anamnesis," +
                    "kesadaran,denyut_nadi,pernapasan,tekanan_darah,suhu_tubuh,pakaian,tinggi_badan,berat_badan,ciri_khusus,kepala,leher,bahu,dada,punggung,perut,pinggang,bokong,dubur,alat_kelamin,anggota_gerak_atas,anggota_gerak_bawah," +
                    "pemeriksaan_laboratorium,pemeriksaan_radiologi,pemeriksaan_odontogram,pemeriksaan_lain_lain,ringkasan_pemeriksaan,kesimpulan," +
                    "tempat_keluar_ver,tgl_keluar_ver,kd_dokter,nama_dokter,nip_dokter,jabatan_kompetensi,created_by,updated_by,status_data) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                isiParameter(ps, false);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    idVisum = Long.valueOf(rs.getLong(1));
                }
                simpanLampiran();
                JOptionPane.showMessageDialog(this, "Data Visum Et Repertum berhasil disimpan.");
            } else {
                ps = koneksi.prepareStatement(
                    "update surat_visum_et_repertum set " +
                    "no_rawat=?,no_rkm_medis=?,no_surat_ver=?,no_surat_permintaan=?,tgl_terima_spv=?,pihak_pembuat_spv=?,jenis_pemeriksaan_diminta=?," +
                    "tempat_pemeriksaan=?,tgl_pemeriksaan=?,nama_pasien=?,tgl_lahir=?,umur=?,alamat=?,no_bukti_identitas=?,anamnesis=?," +
                    "kesadaran=?,denyut_nadi=?,pernapasan=?,tekanan_darah=?,suhu_tubuh=?,pakaian=?,tinggi_badan=?,berat_badan=?,ciri_khusus=?,kepala=?,leher=?,bahu=?,dada=?,punggung=?,perut=?,pinggang=?,bokong=?,dubur=?,alat_kelamin=?,anggota_gerak_atas=?,anggota_gerak_bawah=?," +
                    "pemeriksaan_laboratorium=?,pemeriksaan_radiologi=?,pemeriksaan_odontogram=?,pemeriksaan_lain_lain=?,ringkasan_pemeriksaan=?,kesimpulan=?," +
                    "tempat_keluar_ver=?,tgl_keluar_ver=?,kd_dokter=?,nama_dokter=?,nip_dokter=?,jabatan_kompetensi=?,updated_by=?,status_data=? where id=?"
                );
                isiParameter(ps, true);
                ps.setLong(51, idVisum.longValue());
                ps.executeUpdate();
                simpanLampiran();
                JOptionPane.showMessageDialog(this, "Data Visum Et Repertum berhasil diubah.");
            }
            tampil();
            pilihBarisDataAktif();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data Visum Et Repertum:\n" + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
    }

    private void isiParameter(PreparedStatement ps, boolean modeUpdate) throws Exception {
        int i = 1;
        ps.setString(i++, noRawat);
        ps.setString(i++, txtNoRMInfo.getText().trim());
        ps.setString(i++, txtNoSuratVer.getText().trim());
        ps.setString(i++, nullIfEmpty(txtNoSuratPermintaan.getText()));
        setTimestamp(ps, i++, txtTglTerimaSPV.getText());
        ps.setString(i++, nullIfEmpty(txtPihakPembuatSPV.getText()));
        ps.setString(i++, nullIfEmpty(txtJenisPemeriksaan.getText()));

        ps.setString(i++, nullIfEmpty(txtTempatPemeriksaan.getText()));
        setTimestamp(ps, i++, txtTglPemeriksaan.getText());

        ps.setString(i++, nullIfEmpty(txtNamaIdentitas.getText()));
        setDate(ps, i++, txtTglLahir.getText());
        ps.setString(i++, nullIfEmpty(txtUmur.getText()));
        ps.setString(i++, nullIfEmpty(txtAlamat.getText()));
        ps.setString(i++, nullIfEmpty(txtNoBuktiIdentitas.getText()));
        ps.setString(i++, nullIfEmpty(txtAnamnesis.getText()));

        ps.setString(i++, nullIfEmpty(txtKesadaran.getText()));
        ps.setString(i++, nullIfEmpty(txtDenyutNadi.getText()));
        ps.setString(i++, nullIfEmpty(txtPernapasan.getText()));
        ps.setString(i++, nullIfEmpty(txtTekananDarah.getText()));
        ps.setString(i++, nullIfEmpty(txtSuhuTubuh.getText()));
        ps.setString(i++, nullIfEmpty(txtPakaian.getText()));
        ps.setString(i++, nullIfEmpty(txtTinggiBadan.getText()));
        ps.setString(i++, nullIfEmpty(txtBeratBadan.getText()));
        ps.setString(i++, nullIfEmpty(txtCiriKhusus.getText()));
        ps.setString(i++, nullIfEmpty(txtKepala.getText()));
        ps.setString(i++, nullIfEmpty(txtLeher.getText()));
        ps.setString(i++, nullIfEmpty(txtBahu.getText()));
        ps.setString(i++, nullIfEmpty(txtDada.getText()));
        ps.setString(i++, nullIfEmpty(txtPunggung.getText()));
        ps.setString(i++, nullIfEmpty(txtPerut.getText()));
        ps.setString(i++, nullIfEmpty(txtPinggang.getText()));
        ps.setString(i++, nullIfEmpty(txtBokong.getText()));
        ps.setString(i++, nullIfEmpty(txtDubur.getText()));
        ps.setString(i++, nullIfEmpty(txtAlatKelamin.getText()));
        ps.setString(i++, nullIfEmpty(txtAnggotaGerakAtas.getText()));
        ps.setString(i++, nullIfEmpty(txtAnggotaGerakBawah.getText()));

        ps.setString(i++, nullIfEmpty(txtLaboratorium.getText()));
        ps.setString(i++, nullIfEmpty(txtRadiologi.getText()));
        ps.setString(i++, nullIfEmpty(txtOdontogram.getText()));
        ps.setString(i++, nullIfEmpty(txtLainLain.getText()));
        ps.setString(i++, nullIfEmpty(txtRingkasan.getText()));
        ps.setString(i++, nullIfEmpty(txtKesimpulan.getText()));

        ps.setString(i++, nullIfEmpty(txtTempatKeluar.getText()));
        setDate(ps, i++, txtTglKeluar.getText());
        ps.setString(i++, nullIfEmpty(txtKdDokter.getText()));
        ps.setString(i++, nullIfEmpty(txtNamaDokter.getText()));
        ps.setString(i++, nullIfEmpty(txtNipDokter.getText()));
        ps.setString(i++, nullIfEmpty(txtJabatanKompetensi.getText()));

        if (!modeUpdate) {
            ps.setString(i++, safe(akses.getkode()));
        }
        ps.setString(i++, safe(akses.getkode()));
        ps.setString(i++, cmbStatusData.getSelectedItem().toString());
    }

    private void hapus() {
        if (idVisum == null) {
            JOptionPane.showMessageDialog(this, "Pilih data Visum Et Repertum yang akan dihapus dulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus data Visum Et Repertum terpilih?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement("delete from surat_visum_et_repertum where id=?");
            ps.setLong(1, idVisum.longValue());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Visum Et Repertum berhasil dihapus.");
            emptTeks();
            tampil();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data Visum Et Repertum:\n" + e.getMessage());
        } finally {
            close(ps);
        }
    }

    private void preview() {
        JTextArea area = new JTextArea(buildPreviewText());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setCaretPosition(0);
        area.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(820, 520));
        JOptionPane.showMessageDialog(this, scroll, "Preview Surat Visum Et Repertum", JOptionPane.INFORMATION_MESSAGE);
    }

    private String buildPreviewText() {
        StringBuilder sb = new StringBuilder();
        sb.append("SURAT KETERANGAN VISUM ET REPERTUM\n\n");
        sb.append("PRO JUSTITIA\n");
        sb.append("No. Surat Keterangan VeR : ").append(txtNoSuratVer.getText()).append("\n\n");

        sb.append("I. Surat Permintaan VeR\n");
        sb.append("a) Nomor Surat Permintaan VeR : ").append(txtNoSuratPermintaan.getText()).append("\n");
        sb.append("b) Tanggal dan Waktu SPV diterima : ").append(txtTglTerimaSPV.getText()).append("\n");
        sb.append("c) Pihak yang membuat SPV : ").append(txtPihakPembuatSPV.getText()).append("\n");
        sb.append("d) Jenis pemeriksaan yang diminta : ").append(txtJenisPemeriksaan.getText()).append("\n\n");

        sb.append("II. Laporan Visum et Repertum\n");
        sb.append("a) Tempat, Tanggal, dan Waktu Pemeriksaan : ").append(txtTempatPemeriksaan.getText()).append(" / ").append(txtTglPemeriksaan.getText()).append("\n");
        sb.append("b) Identitas Pasien/Korban\n");
        sb.append("   1. Nama : ").append(txtNamaIdentitas.getText()).append("\n");
        sb.append("   2. Tanggal Lahir/Umur : ").append(txtTglLahir.getText()).append(" / ").append(txtUmur.getText()).append("\n");
        sb.append("   3. Alamat : ").append(txtAlamat.getText()).append("\n");
        sb.append("   4. No. Bukti Identitas : ").append(txtNoBuktiIdentitas.getText()).append("\n\n");

        sb.append("c) Hasil Pemeriksaan\n");
        sb.append("   1. Anamnesis : ").append(txtAnamnesis.getText()).append("\n");
        sb.append("   2. Pemeriksaan Fisis\n");
        sb.append("      - Kesadaran : ").append(txtKesadaran.getText()).append("\n");
        sb.append("      - Denyut nadi : ").append(txtDenyutNadi.getText()).append("\n");
        sb.append("      - Pernapasan : ").append(txtPernapasan.getText()).append("\n");
        sb.append("      - Tekanan darah : ").append(txtTekananDarah.getText()).append("\n");
        sb.append("      - Suhu tubuh : ").append(txtSuhuTubuh.getText()).append("\n");
        sb.append("      - Pakaian : ").append(txtPakaian.getText()).append("\n");
        sb.append("      - Tinggi badan : ").append(txtTinggiBadan.getText()).append("\n");
        sb.append("      - Berat badan : ").append(txtBeratBadan.getText()).append("\n");
        sb.append("      - Ciri khusus : ").append(txtCiriKhusus.getText()).append("\n");
        sb.append("      - Kepala : ").append(txtKepala.getText()).append("\n");
        sb.append("      - Leher : ").append(txtLeher.getText()).append("\n");
        sb.append("      - Bahu : ").append(txtBahu.getText()).append("\n");
        sb.append("      - Dada : ").append(txtDada.getText()).append("\n");
        sb.append("      - Punggung : ").append(txtPunggung.getText()).append("\n");
        sb.append("      - Perut : ").append(txtPerut.getText()).append("\n");
        sb.append("      - Pinggang : ").append(txtPinggang.getText()).append("\n");
        sb.append("      - Bokong : ").append(txtBokong.getText()).append("\n");
        sb.append("      - Dubur : ").append(txtDubur.getText()).append("\n");
        sb.append("      - Alat kelamin : ").append(txtAlatKelamin.getText()).append("\n");
        sb.append("      - Anggota gerak atas : ").append(txtAnggotaGerakAtas.getText()).append("\n");
        sb.append("      - Anggota gerak bawah : ").append(txtAnggotaGerakBawah.getText()).append("\n\n");

        sb.append("   3. Pemeriksaan Penunjang\n");
        sb.append("      - Laboratorium : ").append(txtLaboratorium.getText()).append("\n");
        sb.append("      - Radiologi : ").append(txtRadiologi.getText()).append("\n");
        sb.append("      - Odontogram : ").append(txtOdontogram.getText()).append("\n");
        sb.append("      - Lain-lain : ").append(txtLainLain.getText()).append("\n");
        sb.append("   4. Ringkasan Pemeriksaan : ").append(txtRingkasan.getText()).append("\n");
        sb.append("   5. Kesimpulan : ").append(txtKesimpulan.getText()).append("\n\n");

        sb.append("III. Penutup\n");
        sb.append("Tempat dan tanggal dikeluarkan VeR : ").append(txtTempatKeluar.getText()).append(", ").append(txtTglKeluar.getText()).append("\n");
        sb.append("Dokter berwenang : ").append(txtNamaDokter.getText()).append(" (KD: ").append(txtKdDokter.getText()).append(")\n");
        sb.append("NIP : ").append(txtNipDokter.getText()).append("\n");
        sb.append("Jabatan / kompetensi : ").append(txtJabatanKompetensi.getText()).append("\n\n");

        sb.append("IV. Lampiran Pemeriksaan\n");
        int i;
        for (i = 0; i < tabLampiran.getRowCount(); i++) {
            sb.append("- ").append(tabLampiran.getValueAt(i, 0)).append(" : ")
              .append(tabLampiran.getValueAt(i, 1) == null ? "" : tabLampiran.getValueAt(i, 1).toString())
              .append(" | ")
              .append(tabLampiran.getValueAt(i, 2) == null ? "" : tabLampiran.getValueAt(i, 2).toString())
              .append("\n");
        }
        return sb.toString();
    }

    private void loadData(long id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select * from surat_visum_et_repertum where id=?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                idVisum = Long.valueOf(rs.getLong("id"));
                txtNoSuratVer.setText(safe(rs.getString("no_surat_ver")));
                txtNoSuratPermintaan.setText(safe(rs.getString("no_surat_permintaan")));
                txtTglTerimaSPV.setText(formatTimestamp(rs.getTimestamp("tgl_terima_spv")));
                txtPihakPembuatSPV.setText(safe(rs.getString("pihak_pembuat_spv")));
                txtJenisPemeriksaan.setText(safe(rs.getString("jenis_pemeriksaan_diminta")));
                cmbStatusData.setSelectedItem(safe(rs.getString("status_data")).equals("") ? "DRAFT" : rs.getString("status_data"));

                txtTempatPemeriksaan.setText(safe(rs.getString("tempat_pemeriksaan")));
                txtTglPemeriksaan.setText(formatTimestamp(rs.getTimestamp("tgl_pemeriksaan")));
                txtNamaIdentitas.setText(safe(rs.getString("nama_pasien")));
                txtTglLahir.setText(formatDate(rs.getDate("tgl_lahir")));
                txtUmur.setText(safe(rs.getString("umur")));
                txtAlamat.setText(safe(rs.getString("alamat")));
                txtNoBuktiIdentitas.setText(safe(rs.getString("no_bukti_identitas")));
                txtAnamnesis.setText(safe(rs.getString("anamnesis")));

                txtKesadaran.setText(safe(rs.getString("kesadaran")));
                txtDenyutNadi.setText(safe(rs.getString("denyut_nadi")));
                txtPernapasan.setText(safe(rs.getString("pernapasan")));
                txtTekananDarah.setText(safe(rs.getString("tekanan_darah")));
                txtSuhuTubuh.setText(safe(rs.getString("suhu_tubuh")));
                txtPakaian.setText(safe(rs.getString("pakaian")));
                txtTinggiBadan.setText(safe(rs.getString("tinggi_badan")));
                txtBeratBadan.setText(safe(rs.getString("berat_badan")));
                txtCiriKhusus.setText(safe(rs.getString("ciri_khusus")));
                txtKepala.setText(safe(rs.getString("kepala")));
                txtLeher.setText(safe(rs.getString("leher")));
                txtBahu.setText(safe(rs.getString("bahu")));
                txtDada.setText(safe(rs.getString("dada")));
                txtPunggung.setText(safe(rs.getString("punggung")));
                txtPerut.setText(safe(rs.getString("perut")));
                txtPinggang.setText(safe(rs.getString("pinggang")));
                txtBokong.setText(safe(rs.getString("bokong")));
                txtDubur.setText(safe(rs.getString("dubur")));
                txtAlatKelamin.setText(safe(rs.getString("alat_kelamin")));
                txtAnggotaGerakAtas.setText(safe(rs.getString("anggota_gerak_atas")));
                txtAnggotaGerakBawah.setText(safe(rs.getString("anggota_gerak_bawah")));

                txtLaboratorium.setText(safe(rs.getString("pemeriksaan_laboratorium")));
                txtRadiologi.setText(safe(rs.getString("pemeriksaan_radiologi")));
                txtOdontogram.setText(safe(rs.getString("pemeriksaan_odontogram")));
                txtLainLain.setText(safe(rs.getString("pemeriksaan_lain_lain")));
                txtRingkasan.setText(safe(rs.getString("ringkasan_pemeriksaan")));
                txtKesimpulan.setText(safe(rs.getString("kesimpulan")));
                txtTempatKeluar.setText(safe(rs.getString("tempat_keluar_ver")));
                txtTglKeluar.setText(formatDate(rs.getDate("tgl_keluar_ver")));
                txtKdDokter.setText(safe(rs.getString("kd_dokter")));
                txtNamaDokter.setText(safe(rs.getString("nama_dokter")));
                txtNipDokter.setText(safe(rs.getString("nip_dokter")));
                txtJabatanKompetensi.setText(safe(rs.getString("jabatan_kompetensi")));
            }
            loadLampiran(id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuka data Visum Et Repertum:\n" + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
    }

    private void loadLampiran(long id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            resetLampiran();
            ps = koneksi.prepareStatement("select jenis_lampiran,keterangan,file_path from surat_visum_et_repertum_lampiran where id_visum=?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                setLampiranValue(rs.getString("jenis_lampiran"), rs.getString("keterangan"), rs.getString("file_path"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuka lampiran Visum Et Repertum:\n" + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
    }

    private void simpanLampiran() throws Exception {
        if (idVisum == null) {
            return;
        }

        PreparedStatement psHapus = null;
        PreparedStatement psInsert = null;
        try {
            psHapus = koneksi.prepareStatement("delete from surat_visum_et_repertum_lampiran where id_visum=?");
            psHapus.setLong(1, idVisum.longValue());
            psHapus.executeUpdate();
            close(psHapus);
            psHapus = null;

            psInsert = koneksi.prepareStatement(
                "insert into surat_visum_et_repertum_lampiran(id_visum,jenis_lampiran,keterangan,file_path) values (?,?,?,?)"
            );
            int i;
            for (i = 0; i < tabLampiran.getRowCount(); i++) {
                psInsert.setLong(1, idVisum.longValue());
                psInsert.setString(2, tabLampiran.getValueAt(i, 0).toString());
                psInsert.setString(3, nullIfEmpty(valueAtLampiran(i, 1)));
                psInsert.setString(4, nullIfEmpty(valueAtLampiran(i, 2)));
                psInsert.addBatch();
            }
            psInsert.executeBatch();
        } finally {
            close(psInsert);
            close(psHapus);
        }
    }

    private void isiDataPasienDariRegistrasi() {
        if (noRawat.trim().equals("")) {
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.alamat,reg_periksa.kd_dokter,dokter.nm_dokter " +
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter where reg_periksa.no_rawat=? limit 1"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                txtNoRawatInfo.setText(safe(rs.getString("no_rawat")));
                txtNoRMInfo.setText(safe(rs.getString("no_rkm_medis")));
                txtNamaInfo.setText(safe(rs.getString("nm_pasien")));
                txtDokterInfo.setText(safe(rs.getString("nm_dokter")));

                txtNamaIdentitas.setText(safe(rs.getString("nm_pasien")));
                txtTglLahir.setText(formatDate(rs.getDate("tgl_lahir")));
                txtUmur.setText(hitungUmur(rs.getDate("tgl_lahir")));
                txtAlamat.setText(safe(rs.getString("alamat")));
                if (txtKdDokter.getText().trim().equals("")) {
                    txtKdDokter.setText(safe(rs.getString("kd_dokter")));
                }
                if (txtNamaDokter.getText().trim().equals("")) {
                    txtNamaDokter.setText(safe(rs.getString("nm_dokter")));
                }
            }
        } catch (Exception e) {
            // Tidak memutus alur; cukup dibiarkan kosong bila data tambahan gagal diambil.
            System.out.println("Notifikasi SuratVisumEtRepertum : " + e);
        } finally {
            close(rs);
            close(ps);
        }
    }

    private void pilihBarisDataAktif() {
        if (idVisum == null) {
            return;
        }
        int i;
        for (i = 0; i < tabMode.getRowCount(); i++) {
            Object obj = tabMode.getValueAt(i, 0);
            if (obj != null && ((Long) obj).longValue() == idVisum.longValue()) {
                tbData.setRowSelectionInterval(i, i);
                tbData.scrollRectToVisible(tbData.getCellRect(i, 0, true));
                break;
            }
        }
    }







    private void resetLampiran() {
        tabLampiran.setRowCount(0);
        tabLampiran.addRow(new Object[]{"HASIL_PEMERIKSAAN_KLINIS", "", ""});
        tabLampiran.addRow(new Object[]{"TOKSIKOLOGI", "", ""});
        tabLampiran.addRow(new Object[]{"HISTOPATOLOGI", "", ""});
        tabLampiran.addRow(new Object[]{"FOTO", "", ""});
        tabLampiran.addRow(new Object[]{"VIDEO", "", ""});
        tabLampiran.addRow(new Object[]{"LAIN_LAIN", "", ""});
    }

    private void setLampiranValue(String jenis, String keterangan, String filePath) {
        int i;
        for (i = 0; i < tabLampiran.getRowCount(); i++) {
            if (tabLampiran.getValueAt(i, 0).toString().equals(jenis)) {
                tabLampiran.setValueAt(safe(keterangan), i, 1);
                tabLampiran.setValueAt(safe(filePath), i, 2);
                return;
            }
        }
    }

    private String valueAtLampiran(int row, int col) {
        Object obj = tabLampiran.getValueAt(row, col);
        return obj == null ? "" : obj.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String nullIfEmpty(String value) {
        String hasil = safe(value).trim();
        return hasil.equals("") ? null : hasil;
    }

    private String potong(String value, int panjang) {
        String hasil = safe(value).replace('\n', ' ');
        return hasil.length() > panjang ? hasil.substring(0, panjang) + "..." : hasil;
    }

    private String generateNoSurat() {
        return "VER/" + new SimpleDateFormat("yyyyMMdd/HHmmss").format(new java.util.Date());
    }

    private void setTimestamp(PreparedStatement ps, int index, String value) throws Exception {
        String hasil = safe(value).trim();
        if (hasil.equals("")) {
            ps.setTimestamp(index, null);
        } else {
            try {
                ps.setTimestamp(index, new Timestamp(tanggalJam.parse(hasil).getTime()));
            } catch (ParseException e) {
                throw new Exception("Format tanggal/jam harus yyyy-MM-dd HH:mm:ss pada nilai: " + hasil);
            }
        }
    }

    private void setDate(PreparedStatement ps, int index, String value) throws Exception {
        String hasil = safe(value).trim();
        if (hasil.equals("")) {
            ps.setDate(index, null);
        } else {
            try {
                ps.setDate(index, new java.sql.Date(tanggalSaja.parse(hasil).getTime()));
            } catch (ParseException e) {
                throw new Exception("Format tanggal harus yyyy-MM-dd pada nilai: " + hasil);
            }
        }
    }

    private String formatTimestamp(Timestamp ts) {
        return ts == null ? "" : tanggalJam.format(ts);
    }

    private String formatDate(java.util.Date date) {
        return date == null ? "" : tanggalSaja.format(date);
    }

    private String hitungUmur(java.util.Date tglLahir) {
        if (tglLahir == null) {
            return "";
        }
        Calendar lahir = Calendar.getInstance();
        lahir.setTime(tglLahir);
        Calendar sekarang = Calendar.getInstance();
        int tahun = sekarang.get(Calendar.YEAR) - lahir.get(Calendar.YEAR);
        int bulan = sekarang.get(Calendar.MONTH) - lahir.get(Calendar.MONTH);
        if (sekarang.get(Calendar.DAY_OF_MONTH) < lahir.get(Calendar.DAY_OF_MONTH)) {
            bulan--;
        }
        if (bulan < 0) {
            tahun--;
            bulan += 12;
        }
        return tahun + " Th " + bulan + " Bl";
    }

    private void close(Object obj) {
        try {
            if (obj instanceof ResultSet) {
                ((ResultSet)obj).close();
            } else if (obj instanceof PreparedStatement) {
                ((PreparedStatement)obj).close();
            } else if (obj instanceof Statement) {
                ((Statement)obj).close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi SuratVisumEtRepertum : " + e);
        }
    }


    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        return gbc;
    }

    private void addBuilderField(javax.swing.JPanel panel, GridBagConstraints gbc, int row, widget.Label label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void addBuilderArea(javax.swing.JPanel panel, GridBagConstraints gbc, int row, widget.Label label, JScrollPane scroll, int rows) {
        Component view = scroll.getViewport().getView();
        if (view instanceof widget.TextArea) {
            ((widget.TextArea) view).setRows(rows);
        }
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scroll, gbc);
    }

    private void siapkanTextArea(widget.TextArea area) {
        area.setColumns(20);
        area.setRows(3);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button btnBaru;
    private widget.Button btnCetak;
    private widget.Button btnHapus;
    private widget.Button btnSimpan;
    private widget.Button btnTutup;
    private widget.ComboBox cmbStatusData;
    private widget.Label lblAlamat;
    private widget.Label lblAlatKelamin;
    private widget.Label lblAnamnesis;
    private widget.Label lblAnggotaGerakAtas;
    private widget.Label lblAnggotaGerakBawah;
    private widget.Label lblBahu;
    private widget.Label lblBeratBadan;
    private widget.Label lblBokong;
    private widget.Label lblCiriKhusus;
    private widget.Label lblDada;
    private widget.Label lblDaftarData;
    private widget.Label lblDenyutNadi;
    private widget.Label lblDokterInfo;
    private widget.Label lblDubur;
    private widget.Label lblJabatanKompetensi;
    private widget.Label lblJenisPemeriksaan;
    private widget.Label lblKdDokter;
    private widget.Label lblKepala;
    private widget.Label lblKesadaran;
    private widget.Label lblKesimpulan;
    private widget.Label lblLaboratorium;
    private widget.Label lblLainLain;
    private widget.Label lblLampiran;
    private widget.Label lblLeher;
    private widget.Label lblNamaDokter;
    private widget.Label lblNamaIdentitas;
    private widget.Label lblNamaInfo;
    private widget.Label lblNipDokter;
    private widget.Label lblNoBuktiIdentitas;
    private widget.Label lblNoRMInfo;
    private widget.Label lblNoRawatInfo;
    private widget.Label lblNoSuratPermintaan;
    private widget.Label lblNoSuratVer;
    private widget.Label lblOdontogram;
    private widget.Label lblPakaian;
    private widget.Label lblPernapasan;
    private widget.Label lblPerut;
    private widget.Label lblPihakPembuatSPV;
    private widget.Label lblPinggang;
    private widget.Label lblPunggung;
    private widget.Label lblRadiologi;
    private widget.Label lblRingkasan;
    private widget.Label lblStatusData;
    private widget.Label lblSuhuTubuh;
    private widget.Label lblTekananDarah;
    private widget.Label lblTempatKeluar;
    private widget.Label lblTempatPemeriksaan;
    private widget.Label lblTglKeluar;
    private widget.Label lblTglLahir;
    private widget.Label lblTglPemeriksaan;
    private widget.Label lblTglTerimaSPV;
    private widget.Label lblTinggiBadan;
    private widget.Label lblUmur;
    private widget.PanelBiasa panelFisisInner;
    private widget.PanelBiasa panelIdentitasInner;
    private widget.PanelBiasa panelInfo;
    private widget.PanelBiasa panelList;
    private widget.PanelBiasa panelPenunjangInner;
    private widget.PanelBiasa panelSPVInner;
    private widget.PanelBiasa panelTombol;
    private widget.ScrollPane scrollAlamat;
    private widget.ScrollPane scrollAlatKelamin;
    private widget.ScrollPane scrollAnamnesis;
    private widget.ScrollPane scrollAnggotaGerakAtas;
    private widget.ScrollPane scrollAnggotaGerakBawah;
    private widget.ScrollPane scrollBahu;
    private widget.ScrollPane scrollBokong;
    private widget.ScrollPane scrollCiriKhusus;
    private widget.ScrollPane scrollDada;
    private widget.ScrollPane scrollDubur;
    private widget.ScrollPane scrollFisis;
    private widget.ScrollPane scrollIdentitas;
    private widget.ScrollPane scrollJabatanKompetensi;
    private widget.ScrollPane scrollKepala;
    private widget.ScrollPane scrollKesimpulan;
    private widget.ScrollPane scrollLaboratorium;
    private widget.ScrollPane scrollLainLain;
    private widget.ScrollPane scrollLeher;
    private widget.ScrollPane scrollOdontogram;
    private widget.ScrollPane scrollPakaian;
    private widget.ScrollPane scrollPenunjang;
    private widget.ScrollPane scrollPerut;
    private widget.ScrollPane scrollPihakPembuatSPV;
    private widget.ScrollPane scrollPinggang;
    private widget.ScrollPane scrollPunggung;
    private widget.ScrollPane scrollRadiologi;
    private widget.ScrollPane scrollRingkasan;
    private widget.ScrollPane scrollSPV;
    private widget.ScrollPane scrollTbData;
    private widget.ScrollPane scrollTbLampiran;
    private widget.TabPane tabMain;
    private widget.Table tbData;
    private widget.Table tbLampiran;
    private widget.TextArea txtAlamat;
    private widget.TextArea txtAlatKelamin;
    private widget.TextArea txtAnamnesis;
    private widget.TextArea txtAnggotaGerakAtas;
    private widget.TextArea txtAnggotaGerakBawah;
    private widget.TextArea txtBahu;
    private widget.TextBox txtBeratBadan;
    private widget.TextArea txtBokong;
    private widget.TextArea txtCiriKhusus;
    private widget.TextArea txtDada;
    private widget.TextBox txtDenyutNadi;
    private widget.TextBox txtDokterInfo;
    private widget.TextArea txtDubur;
    private widget.TextArea txtJabatanKompetensi;
    private widget.TextBox txtJenisPemeriksaan;
    private widget.TextBox txtKdDokter;
    private widget.TextArea txtKepala;
    private widget.TextBox txtKesadaran;
    private widget.TextArea txtKesimpulan;
    private widget.TextArea txtLaboratorium;
    private widget.TextArea txtLainLain;
    private widget.TextArea txtLeher;
    private widget.TextBox txtNamaDokter;
    private widget.TextBox txtNamaIdentitas;
    private widget.TextBox txtNamaInfo;
    private widget.TextBox txtNipDokter;
    private widget.TextBox txtNoBuktiIdentitas;
    private widget.TextBox txtNoRMInfo;
    private widget.TextBox txtNoRawatInfo;
    private widget.TextBox txtNoSuratPermintaan;
    private widget.TextBox txtNoSuratVer;
    private widget.TextArea txtOdontogram;
    private widget.TextArea txtPakaian;
    private widget.TextBox txtPernapasan;
    private widget.TextArea txtPerut;
    private widget.TextArea txtPihakPembuatSPV;
    private widget.TextArea txtPinggang;
    private widget.TextArea txtPunggung;
    private widget.TextArea txtRadiologi;
    private widget.TextArea txtRingkasan;
    private widget.TextBox txtSuhuTubuh;
    private widget.TextBox txtTekananDarah;
    private widget.TextBox txtTempatKeluar;
    private widget.TextBox txtTempatPemeriksaan;
    private widget.TextBox txtTglKeluar;
    private widget.TextBox txtTglLahir;
    private widget.TextBox txtTglPemeriksaan;
    private widget.TextBox txtTglTerimaSPV;
    private widget.TextBox txtTinggiBadan;
    private widget.TextBox txtUmur;
    // End of variables declaration//GEN-END:variables
}
