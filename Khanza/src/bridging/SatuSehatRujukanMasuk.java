/*
 * SatuSehatRujukanMasuk.java
 *
 * Form mandiri untuk RS Anda menerima dan memproses RUJUKAN MASUK
 * dari fasyankes lain via Satu Sehat.
 *
 * Sesuai Buku Panduan SATUSEHAT (Playbook) Rujukan Pasien v5.1, 4 Maret 2026:
 *   - Bab 1.3.2 (Pendaftaran kunjungan rujukan rajal)
 *   - Bab 2.3.2 (Persetujuan/Penolakan tugas rujukan ranap & IGD)
 *   - Bab 2.4 (Pengiriman rujukan ranap & IGD)
 *
 * Layout: form tunggal - semua tipe rujukan dicampur, filter di UI.
 *
 * Flow user:
 *   1. Buka form → auto fetch GET ServiceRequest?performer={my-org}
 *   2. Filter (tanggal, tipe rajal/ranap/igd, status, no rujukan)
 *   3. Klik baris → tampilkan detail (pasien, diagnosa, faskes perujuk)
 *   4. Action:
 *      [Lihat Detail]      → GET ServiceRequest with supportingInfo
 *      [Lihat CarePlan]    → GET CarePlan
 *      [Accept]            → PATCH Task accepted (ranap/igd) → otomatis daftar kunjungan
 *      [Reject]            → PATCH Task rejected dengan alasan
 *      [Daftar Kunjungan]  → khusus rajal: langsung daftar tanpa accept Task
 *
 * @author SIMRS Khanza Bridging
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

public final class SatuSehatRujukanMasuk extends javax.swing.JDialog {

    // ===== Helper Khanza =====
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    // ===== Service =====
    private final SatuSehatRujukanService svc = new SatuSehatRujukanService();
    private final SatuSehatCekNIK cekIhs = new SatuSehatCekNIK();
    private final ApiSatuSehat ihs = new ApiSatuSehat();

    // ===== State =====
    private String user = "";
    private String selSrId = "";
    private String selNoRujukanSS = "";
    private String selTipeRujukan = "";
    private String selIdPasienSS = "";
    private String selNamaPasien = "";
    private String selNoKtpPasien = "";       // NIK pasien dari Patient resource lookup
    private String selTglLahirPasien = "";    // tgl lahir (yyyy-MM-dd) untuk match pasien
    private String selJkPasien = "";          // L/P
    private String selNoKartuBpjs = "";       // No.Kartu BPJS dari SR.identifier
    private String selKdDiagnosa = "";
    private String selIdFaskesPerujuk = "";
    private String selNamaFaskesPerujuk = "";
    private String selCarePlanId = "";
    private String selTaskId = "";
    private String selStatusLokal = "";
    private String selNoRawatLokal = "";

    // ===== Models =====
    private final DefaultTableModel modelRujukan;

    // ===== UI =====
    private widget.InternalFrame frameMain;
    private widget.PanelBiasa pnlFilter, pnlTabel, pnlDetail, pnlAction;

    // Filter
    private widget.Tanggal dtMulai, dtAkhir;
    private widget.ComboBox cbTipe, cbStatus;
    private widget.TextBox tCariNoRujukan;
    private widget.Button btnRefresh, btnCariByNomor;

    // Tabel rujukan
    private widget.Table tblRujukan;
    private widget.ScrollPane scrollRujukan;

    // Detail panel
    private widget.TextBox tDetailNoRujukan, tDetailTipe, tDetailTgl;
    private widget.TextBox tDetailPasien, tDetailDiagnosa, tDetailFaskesPerujuk;
    private widget.TextBox tDetailNoRujukanPCare, tDetailNoKartu;
    private widget.TextBox tDetailStatusLokal, tDetailNoRawatLokal;
    private JTextArea taDetailPesanKlinis;

    // Action buttons
    private widget.Button btnLihatDetail, btnLihatCarePlan;
    private widget.Button btnAccept, btnReject, btnDaftarKunjungan;
    private widget.Button btnTutup;
    private widget.Label lblStatus;

    // Tipe filter preset
    private static final String[][] TIPE_FILTER = {
        {"", "Semua Tipe"},
        {"rajal", "Rawat Jalan"},
        {"ranap", "Rawat Inap"},
        {"igd", "IGD/Darurat"}
    };

    // Status filter preset
    private static final String[][] STATUS_FILTER = {
        {"", "Semua Status"},
        {"pending", "Pending (belum direspon)"},
        {"accepted", "Accepted"},
        {"rejected", "Rejected"},
        {"registered", "Sudah Daftar Kunjungan"},
        {"completed", "Completed"}
    };

    public SatuSehatRujukanMasuk(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        modelRujukan = new DefaultTableModel(null, new Object[]{
                "No.Rujukan Nasional", "Tipe", "Tgl Rujukan", "No.RM/IHS",
                "Nama Pasien", "Faskes Perujuk", "Diagnosa", "Status Lokal"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        initComponents();
        setSize(1280, 800);
        setLocationRelativeTo(parent);

        try {
            user = akses.getkode().replace(" ", "").substring(0, 9);
        } catch (Exception e) {
            user = akses.getkode();
        }

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                refreshRujukanMasuk();
            }
        });
    }

    // =================================================================
    //  initComponents
    // =================================================================
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rujukan Masuk - Satu Sehat");

        frameMain = new widget.InternalFrame();
        frameMain.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Rujukan Masuk dari Fasyankes Lain - Satu Sehat ]::"));
        frameMain.setLayout(new BorderLayout(2, 2));

        // ============ TOP: Filter ============
        pnlFilter = new widget.PanelBiasa();
        pnlFilter.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));
        pnlFilter.setPreferredSize(new Dimension(1280, 45));

        pnlFilter.add(mkLabel("Tgl:"));
        dtMulai = new widget.Tanggal();
        dtMulai.setDisplayFormat("dd-MM-yyyy");
        dtMulai.setPreferredSize(new Dimension(110, 23));
        // Default 7 hari ke belakang
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
        dtMulai.setDate(cal.getTime());
        pnlFilter.add(dtMulai);

        pnlFilter.add(mkLabel("s.d:"));
        dtAkhir = new widget.Tanggal();
        dtAkhir.setDisplayFormat("dd-MM-yyyy");
        dtAkhir.setPreferredSize(new Dimension(110, 23));
        dtAkhir.setDate(new Date());
        pnlFilter.add(dtAkhir);

        pnlFilter.add(mkLabel("Tipe:"));
        cbTipe = new widget.ComboBox();
        String[] tItems = new String[TIPE_FILTER.length];
        for (int i = 0; i < TIPE_FILTER.length; i++) tItems[i] = TIPE_FILTER[i][1];
        cbTipe.setModel(new javax.swing.DefaultComboBoxModel<>(tItems));
        cbTipe.setPreferredSize(new Dimension(140, 23));
        cbTipe.addActionListener(e -> filterTabelLokal());
        pnlFilter.add(cbTipe);

        pnlFilter.add(mkLabel("Status:"));
        cbStatus = new widget.ComboBox();
        String[] sItems = new String[STATUS_FILTER.length];
        for (int i = 0; i < STATUS_FILTER.length; i++) sItems[i] = STATUS_FILTER[i][1];
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(sItems));
        cbStatus.setPreferredSize(new Dimension(180, 23));
        cbStatus.addActionListener(e -> filterTabelLokal());
        pnlFilter.add(cbStatus);

        btnRefresh = mkBtn("Refresh dari Satu Sehat", "/picture/refresh.png");
        btnRefresh.setPreferredSize(new Dimension(170, 28));
        btnRefresh.addActionListener(e -> refreshRujukanMasuk());
        pnlFilter.add(btnRefresh);

        pnlFilter.add(mkLabel(" | No.Rujukan:"));
        tCariNoRujukan = new widget.TextBox();
        tCariNoRujukan.setPreferredSize(new Dimension(160, 23));
        pnlFilter.add(tCariNoRujukan);

        btnCariByNomor = mkBtn("Cari", "/picture/Search-16x16.png");
        btnCariByNomor.setPreferredSize(new Dimension(80, 28));
        btnCariByNomor.addActionListener(e -> cariRujukanByNomor());
        pnlFilter.add(btnCariByNomor);

        frameMain.add(pnlFilter, BorderLayout.NORTH);

        // ============ MID: Tabel rujukan ============
        pnlTabel = new widget.PanelBiasa();
        pnlTabel.setLayout(new BorderLayout(2, 2));
        pnlTabel.setPreferredSize(new Dimension(1280, 280));
        pnlTabel.setBorder(BorderFactory.createTitledBorder("Daftar Rujukan Masuk"));

        tblRujukan = new widget.Table();
        tblRujukan.setModel(modelRujukan);
        tblRujukan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRujukan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] wp = {180, 80, 130, 130, 200, 230, 200, 130};
        for (int i = 0; i < wp.length; i++) {
            tblRujukan.getColumnModel().getColumn(i).setPreferredWidth(wp[i]);
        }
        tblRujukan.setDefaultRenderer(Object.class, new WarnaTable());
        tblRujukan.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting() && tblRujukan.getSelectedRow() >= 0) {
                onRujukanSelected();
            }
        });

        scrollRujukan = new widget.ScrollPane();
        scrollRujukan.setViewportView(tblRujukan);
        pnlTabel.add(scrollRujukan, BorderLayout.CENTER);

        frameMain.add(pnlTabel, BorderLayout.CENTER);

        // ============ BOTTOM: Detail + Action ============
        widget.PanelBiasa pnlBottom = new widget.PanelBiasa();
        pnlBottom.setLayout(new BorderLayout(2, 2));
        pnlBottom.setPreferredSize(new Dimension(1280, 380));

        // Detail panel
        pnlDetail = new widget.PanelBiasa();
        pnlDetail.setLayout(null);
        pnlDetail.setPreferredSize(new Dimension(1280, 220));
        pnlDetail.setBorder(BorderFactory.createTitledBorder("Detail Rujukan Terpilih"));

        int x1 = 10, x2 = 130, w1 = 110, w2 = 320, h = 22, gap = 26;
        int y = 22;

        addLbl(pnlDetail, "No.Rujukan SS:", x1, y, w1, h);
        tDetailNoRujukan = roField(pnlDetail, x2, y, w2, h);

        addLbl(pnlDetail, "Tipe:", x2 + w2 + 30, y, 50, h);
        tDetailTipe = roField(pnlDetail, x2 + w2 + 80, y, 100, h);

        addLbl(pnlDetail, "Tgl Rujukan:", x2 + w2 + 200, y, 90, h);
        tDetailTgl = roField(pnlDetail, x2 + w2 + 290, y, 200, h);
        y += gap;

        addLbl(pnlDetail, "Pasien:", x1, y, w1, h);
        tDetailPasien = roField(pnlDetail, x2, y, w2, h);

        addLbl(pnlDetail, "Diagnosa:", x2 + w2 + 30, y, 80, h);
        tDetailDiagnosa = roField(pnlDetail, x2 + w2 + 110, y, 380, h);
        y += gap;

        addLbl(pnlDetail, "Faskes Perujuk:", x1, y, w1, h);
        tDetailFaskesPerujuk = roField(pnlDetail, x2, y, w2, h);

        addLbl(pnlDetail, "No.PCare:", x2 + w2 + 30, y, 80, h);
        tDetailNoRujukanPCare = roField(pnlDetail, x2 + w2 + 110, y, 200, h);

        addLbl(pnlDetail, "No.Kartu:", x2 + w2 + 320, y, 70, h);
        tDetailNoKartu = roField(pnlDetail, x2 + w2 + 390, y, 170, h);
        y += gap;

        addLbl(pnlDetail, "Status Lokal:", x1, y, w1, h);
        tDetailStatusLokal = roField(pnlDetail, x2, y, w2, h);

        addLbl(pnlDetail, "No.Rawat Lokal:", x2 + w2 + 30, y, 100, h);
        tDetailNoRawatLokal = roField(pnlDetail, x2 + w2 + 130, y, 200, h);
        y += gap + 5;

        addLbl(pnlDetail, "Pesan Klinis:", x1, y, w1, h);
        taDetailPesanKlinis = new JTextArea();
        taDetailPesanKlinis.setEditable(false);
        taDetailPesanKlinis.setLineWrap(true);
        taDetailPesanKlinis.setWrapStyleWord(true);
        taDetailPesanKlinis.setBackground(new java.awt.Color(245, 250, 240));
        widget.ScrollPane spDetail = new widget.ScrollPane();
        spDetail.setViewportView(taDetailPesanKlinis);
        spDetail.setBounds(x2, y, 1100, 60);
        pnlDetail.add(spDetail);

        pnlBottom.add(pnlDetail, BorderLayout.CENTER);

        // Action panel
        pnlAction = new widget.PanelBiasa();
        pnlAction.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));
        pnlAction.setPreferredSize(new Dimension(1280, 50));
        pnlAction.setBorder(BorderFactory.createTitledBorder("Aksi"));

        btnLihatDetail = mkBtn("Lihat Detail SR", "/picture/Search-16x16.png");
        btnLihatDetail.setPreferredSize(new Dimension(140, 28));
        btnLihatDetail.addActionListener(e -> doLihatDetail());
        pnlAction.add(btnLihatDetail);

        btnLihatCarePlan = mkBtn("Lihat CarePlan", "/picture/Search-16x16.png");
        btnLihatCarePlan.setPreferredSize(new Dimension(140, 28));
        btnLihatCarePlan.addActionListener(e -> doLihatCarePlan());
        pnlAction.add(btnLihatCarePlan);

        btnAccept = mkBtn("ACCEPT (+ Daftar)", "/picture/save-16x16.png");
        btnAccept.setPreferredSize(new Dimension(180, 28));
        btnAccept.setForeground(new java.awt.Color(0, 100, 0));
        btnAccept.addActionListener(e -> doAccept());
        pnlAction.add(btnAccept);

        btnReject = mkBtn("REJECT", "/picture/stop_f2.png");
        btnReject.setPreferredSize(new Dimension(120, 28));
        btnReject.setForeground(new java.awt.Color(180, 0, 0));
        btnReject.addActionListener(e -> doReject());
        pnlAction.add(btnReject);

        btnDaftarKunjungan = mkBtn("Daftar Kunjungan", "/picture/save-16x16.png");
        btnDaftarKunjungan.setPreferredSize(new Dimension(170, 28));
        btnDaftarKunjungan.addActionListener(e -> doDaftarKunjungan());
        pnlAction.add(btnDaftarKunjungan);

        btnTutup = mkBtn("Tutup", "/picture/exit.png");
        btnTutup.setPreferredSize(new Dimension(100, 28));
        btnTutup.addActionListener(e -> dispose());
        pnlAction.add(btnTutup);

        pnlBottom.add(pnlAction, BorderLayout.NORTH);

        lblStatus = new widget.Label();
        lblStatus.setText("Status: siap. Klik [Refresh dari Satu Sehat] untuk ambil daftar.");
        lblStatus.setForeground(new java.awt.Color(0, 100, 0));
        lblStatus.setPreferredSize(new Dimension(1280, 22));
        pnlBottom.add(lblStatus, BorderLayout.SOUTH);

        frameMain.add(pnlBottom, BorderLayout.SOUTH);

        getContentPane().add(frameMain, BorderLayout.CENTER);
        pack();
    }

    // =================================================================
    //  HELPER UI
    // =================================================================
    private widget.Label mkLabel(String text) {
        widget.Label l = new widget.Label();
        l.setText(text);
        return l;
    }

    private void addLbl(widget.PanelBiasa parent, String text, int x, int y, int w, int h) {
        widget.Label l = new widget.Label();
        l.setText(text);
        l.setBounds(x, y, w, h);
        parent.add(l);
    }

    private widget.TextBox roField(widget.PanelBiasa parent, int x, int y, int w, int h) {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        t.setBackground(new java.awt.Color(245, 250, 240));
        t.setBounds(x, y, w, h);
        parent.add(t);
        return t;
    }

    private widget.Button mkBtn(String text, String icon) {
        widget.Button b = new widget.Button();
        b.setText(text);
        try { b.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon))); } catch (Exception ig) {}
//        b.setGlassColor(new java.awt.Color(255, 255, 255));
        return b;
    }

    private void setStatus(String msg, boolean error) {
        lblStatus.setText("Status: " + msg);
        lblStatus.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
        System.out.println("[RujukanMasuk] " + msg);
    }

    // =================================================================
    //  REFRESH FROM SATU SEHAT
    // =================================================================
    private void refreshRujukanMasuk() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Mengambil rujukan masuk dari Satu Sehat...", false);

        String tglMulai = Valid.SetTgl(dtMulai.getSelectedItem() + "");
        String tglAkhir = Valid.SetTgl(dtAkhir.getSelectedItem() + "");

        try {
            JsonNode resp = svc.cariRujukanMasuk(tglMulai, tglAkhir, null);

            int count = 0;
            JsonNode entries = resp.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode sr = entry.path("resource");
                    if (!"ServiceRequest".equals(sr.path("resourceType").asText())) continue;

                    upsertRujukanMasuk(sr);
                    count++;
                }
            }

            setStatus("Berhasil ambil " + count + " rujukan dari Satu Sehat.", false);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }

        // Reload tabel dari DB lokal
        loadTabelDariDb();
    }

    /**
     * Insert atau update rujukan ke bridging_rujukan_masuk.
     * Kalau sudah ada, hanya update field server (tidak override status_lokal/no_rawat_lokal).
     */
    private void upsertRujukanMasuk(JsonNode srResource) {
        String srId = srResource.path("id").asText();
        if (srId.isEmpty()) return;

        // Parse semua field dari ServiceRequest
        String tipe = svc.parseTipeRujukan(srResource);

        // SAFETY CHECK: skip kalau bukan rujukan pasien
        // (mis. permintaan radiologi/lab internal yang RS Anda jadi performer-nya)
        // Rujukan harus pakai SNOMED 737481003/385868005/737492002 di code.coding.
        if (tipe == null || tipe.isEmpty()) {
            // Bukan rujukan, skip silently
            return;
        }

        String idPasienSS = svc.parsePatientIdFromSR(srResource);
        String idFaskesPerujuk = svc.parseRequesterIdFromSR(srResource);
        String careplanId = svc.parseCarePlanIdFromSR(srResource);
        String noPCare = svc.parseNoRujukanPCareFromSR(srResource);
        String noKartu = svc.parseNoKartuAsuransiFromSR(srResource);

        // Nomor rujukan nasional
        String noRujukanNas = "";
        JsonNode ids = srResource.path("identifier");
        if (ids.isArray()) {
            for (JsonNode id : ids) {
                if (id.path("system").asText().contains("referral-number-satusehat")) {
                    noRujukanNas = id.path("value").asText();
                    break;
                }
            }
        }

        // Tanggal
        String tglRujukan = srResource.path("authoredOn").asText();
        if (tglRujukan.length() > 19) tglRujukan = tglRujukan.substring(0, 19).replace("T", " ");

        // Diagnosa - dari reasonReference (perlu query lebih lanjut, kita simpan ref-nya saja)
        String kdDiagnosa = "", nmDiagnosa = "";
        JsonNode reasonRef = srResource.path("reasonReference");
        if (reasonRef.isArray() && reasonRef.size() > 0) {
            // Ambil display kalau ada
            nmDiagnosa = reasonRef.get(0).path("display").asText();
        }

        // Lookup nama pasien dari IHS (kalau ada di pasien.no_ktp)
        String namaPasien = "", noRkmMedisLokal = "";
        try {
            // Cari di pasien lokal kalau IHS-nya sudah pernah teregister
            // Lookup nama pasien dari history bridging_rujukan_masuk (kalau pernah).
            // Kalau belum pernah, namaPasien akan kosong dan diisi default.
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select brm.nama_pasien, brm.no_rkm_medis_lokal "
                    + "from bridging_rujukan_masuk brm "
                    + "where brm.id_pasien_satusehat = ? "
                    + "  and brm.nama_pasien is not null "
                    + "  and brm.nama_pasien <> '' "
                    + "limit 1")) {
                ps.setString(1, idPasienSS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        namaPasien = rs.getString(1);
                        noRkmMedisLokal = rs.getString(2);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Lookup nama pasien gagal: " + e);
        }
        if (namaPasien == null || namaPasien.isEmpty()) {
            namaPasien = "(belum terdaftar lokal: IHS=" + idPasienSS + ")";
        }

        // Lookup nama faskes perujuk (kalau ada di tabel cache, atau pakai display dari SR)
        String namaFaskesPerujuk = srResource.path("requester").path("display").asText();
        if (namaFaskesPerujuk.isEmpty()) namaFaskesPerujuk = "ID:" + idFaskesPerujuk;

        // INSERT or UPDATE
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into bridging_rujukan_masuk "
                + "(service_request_id, no_rujukan_satusehat, no_rujukan_pcare, no_kartu_asuransi, "
                + " careplan_id, tipe_rujukan, id_pasien_satusehat, nama_pasien, "
                + " id_faskes_perujuk, nama_faskes_perujuk, kd_diagnosa, nm_diagnosa, "
                + " tgl_rujukan, no_rkm_medis_lokal) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update "
                + " no_rujukan_satusehat=values(no_rujukan_satusehat), "
                + " no_rujukan_pcare=values(no_rujukan_pcare), "
                + " no_kartu_asuransi=values(no_kartu_asuransi), "
                + " careplan_id=values(careplan_id), "
                + " tipe_rujukan=values(tipe_rujukan), "
                + " id_pasien_satusehat=values(id_pasien_satusehat), "
                + " nama_faskes_perujuk=values(nama_faskes_perujuk), "
                + " tgl_rujukan=values(tgl_rujukan)")) {
            ps.setString(1, srId);
            ps.setString(2, noRujukanNas);
            ps.setString(3, noPCare);
            ps.setString(4, noKartu);
            ps.setString(5, careplanId);
            ps.setString(6, tipe.isEmpty() ? "unknown" : tipe);
            ps.setString(7, idPasienSS);
            ps.setString(8, namaPasien);
            ps.setString(9, idFaskesPerujuk);
            ps.setString(10, namaFaskesPerujuk);
            ps.setString(11, kdDiagnosa);
            ps.setString(12, nmDiagnosa);
            ps.setString(13, tglRujukan.isEmpty() ? null : tglRujukan);
            ps.setString(14, noRkmMedisLokal);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("upsertRujukanMasuk err: " + ex);
        }
    }

    /**
     * Cari satu rujukan berdasarkan No.Rujukan Nasional.
     */
    private void cariRujukanByNomor() {
        String nomor = tCariNoRujukan.getText().trim();
        if (nomor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi No.Rujukan Nasional dulu.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Mencari rujukan dengan nomor: " + nomor, false);

        try {
            JsonNode resp = svc.cariRujukanMasukByNomor(nomor);
            int count = 0;
            JsonNode entries = resp.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode sr = entry.path("resource");
                    if ("ServiceRequest".equals(sr.path("resourceType").asText())) {
                        upsertRujukanMasuk(sr);
                        count++;
                    }
                }
            }
            setStatus("Cari nomor: " + nomor + " - ditemukan " + count, false);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }

        loadTabelDariDb();
    }

    // =================================================================
    //  LOAD TABEL DARI DB LOKAL (dengan filter)
    // =================================================================
    private void loadTabelDariDb() {
        modelRujukan.setRowCount(0);

        StringBuilder sql = new StringBuilder();
        sql.append("select service_request_id, no_rujukan_satusehat, tipe_rujukan, ")
           .append("tgl_rujukan, ")
           .append("ifnull(no_rkm_medis_lokal, id_pasien_satusehat) as no_rm_or_ihs, ")
           .append("nama_pasien, nama_faskes_perujuk, ")
           .append("concat(ifnull(kd_diagnosa,''),' - ',ifnull(nm_diagnosa,'')) as diagnosa, ")
           .append("status_lokal ")
           .append("from bridging_rujukan_masuk where 1=1 ");

        // Filter tipe
        int idxTipe = cbTipe.getSelectedIndex();
        String tipeFilter = TIPE_FILTER[idxTipe][0];
        if (!tipeFilter.isEmpty()) {
            sql.append("and tipe_rujukan='").append(tipeFilter).append("' ");
        }

        // Filter status
        int idxStatus = cbStatus.getSelectedIndex();
        String statusFilter = STATUS_FILTER[idxStatus][0];
        if (!statusFilter.isEmpty()) {
            sql.append("and status_lokal='").append(statusFilter).append("' ");
        }

        sql.append("order by tgl_rujukan desc limit 500");

        try (PreparedStatement ps = koneksi.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelRujukan.addRow(new Object[]{
                        safe(rs.getString("no_rujukan_satusehat")),
                        safe(rs.getString("tipe_rujukan")).toUpperCase(),
                        safe(rs.getString("tgl_rujukan")),
                        safe(rs.getString("no_rm_or_ihs")),
                        safe(rs.getString("nama_pasien")),
                        safe(rs.getString("nama_faskes_perujuk")),
                        safe(rs.getString("diagnosa")),
                        safe(rs.getString("status_lokal"))
                });
                // Simpan SR id di kolom hidden? Atau pakai approach beda:
                // modelRujukan.addRow tidak simpan srId — kita ambil pas onRujukanSelected.
            }
        } catch (Exception ex) {
            System.out.println("loadTabelDariDb err: " + ex);
            JOptionPane.showMessageDialog(this, "Error load tabel: " + ex.getMessage());
        }

        setStatus("Total rujukan masuk di DB lokal: " + modelRujukan.getRowCount(), false);
    }

    private void filterTabelLokal() {
        loadTabelDariDb();
    }

    // =================================================================
    //  ON RUJUKAN SELECTED
    // =================================================================
    private void onRujukanSelected() {
        int row = tblRujukan.getSelectedRow();
        if (row < 0) return;

        // Ambil SR ID dari DB berdasarkan no_rujukan_satusehat (kolom 0)
        String noRujukanNas = String.valueOf(modelRujukan.getValueAt(row, 0));

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from bridging_rujukan_masuk where no_rujukan_satusehat=? limit 1")) {
            ps.setString(1, noRujukanNas);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selSrId = safe(rs.getString("service_request_id"));
                    selNoRujukanSS = safe(rs.getString("no_rujukan_satusehat"));
                    selTipeRujukan = safe(rs.getString("tipe_rujukan"));
                    selIdPasienSS = safe(rs.getString("id_pasien_satusehat"));
                    selNamaPasien = safe(rs.getString("nama_pasien"));
                    selNoKtpPasien = safe(rs.getString("no_ktp_pasien"));
                    selTglLahirPasien = safe(rs.getString("tgl_lahir_pasien"));
                    selJkPasien = safe(rs.getString("jk_pasien"));
                    selNoKartuBpjs = safe(rs.getString("no_kartu_asuransi"));
                    selKdDiagnosa = safe(rs.getString("kd_diagnosa"));
                    selIdFaskesPerujuk = safe(rs.getString("id_faskes_perujuk"));
                    selNamaFaskesPerujuk = safe(rs.getString("nama_faskes_perujuk"));
                    selCarePlanId = safe(rs.getString("careplan_id"));
                    selTaskId = safe(rs.getString("task_id"));
                    selStatusLokal = safe(rs.getString("status_lokal"));
                    selNoRawatLokal = safe(rs.getString("no_rawat_lokal"));

                    tDetailNoRujukan.setText(selNoRujukanSS);
                    tDetailTipe.setText(selTipeRujukan.toUpperCase());
                    tDetailTgl.setText(safe(rs.getString("tgl_rujukan")));
                    tDetailPasien.setText(selNamaPasien + " (IHS:" + selIdPasienSS + ")");
                    tDetailDiagnosa.setText(safe(rs.getString("kd_diagnosa"))
                            + " - " + safe(rs.getString("nm_diagnosa")));
                    tDetailFaskesPerujuk.setText(selNamaFaskesPerujuk);
                    tDetailNoRujukanPCare.setText(safe(rs.getString("no_rujukan_pcare")));
                    tDetailNoKartu.setText(safe(rs.getString("no_kartu_asuransi")));
                    tDetailStatusLokal.setText(selStatusLokal);
                    tDetailNoRawatLokal.setText(selNoRawatLokal);
                    taDetailPesanKlinis.setText("");

                    // Update tombol enabled state berdasarkan tipe & status
                    updateButtonsState();
                }
            }
        } catch (Exception ex) {
            System.out.println("onRujukanSelected err: " + ex);
        }
    }

    private void updateButtonsState() {
        boolean isPending = "pending".equals(selStatusLokal);
        boolean isAccepted = "accepted".equals(selStatusLokal);
        boolean isRanapOrIGD = "ranap".equals(selTipeRujukan) || "igd".equals(selTipeRujukan);
        boolean isRajal = "rajal".equals(selTipeRujukan);
        boolean isRegistered = "registered".equals(selStatusLokal) || "completed".equals(selStatusLokal);

        // Accept: hanya kalau pending & ranap/igd (rajal tidak perlu accept Task)
        btnAccept.setEnabled(isPending && isRanapOrIGD && !isRegistered);

        // Reject: hanya kalau pending & ranap/igd
        btnReject.setEnabled(isPending && isRanapOrIGD && !isRegistered);

        // Daftar Kunjungan: aktif kalau belum registered
        //   - rajal: bebas, langsung daftar
        //   - ranap/igd: idealnya setelah accept, tapi tetap allow (dengan warning)
        btnDaftarKunjungan.setEnabled(!isRegistered && !selSrId.isEmpty());

        btnLihatDetail.setEnabled(true);
        btnLihatCarePlan.setEnabled(selCarePlanId != null && !selCarePlanId.isEmpty());
    }

    // =================================================================
    //  ACTION: LIHAT DETAIL SR
    // =================================================================
    private void doLihatDetail() {
        if (selSrId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih rujukan dari tabel dulu.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JsonNode resp = svc.getServiceRequestWithSupport(selSrId);
            String pretty = ihs.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resp);
            tampilkanJsonDialog("Detail ServiceRequest", pretty);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  ACTION: LIHAT CAREPLAN
    // =================================================================
    private void doLihatCarePlan() {
        if (selCarePlanId == null || selCarePlanId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Rujukan ini tidak punya CarePlan.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JsonNode resp = svc.getCarePlanWithSupport(selCarePlanId);
            String pretty = ihs.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resp);
            tampilkanJsonDialog("Detail CarePlan", pretty);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void tampilkanJsonDialog(String title, String content) {
        JTextArea ta = new JTextArea(content);
        ta.setEditable(false);
        ta.setFont(new java.awt.Font("Monospaced", 0, 11));
        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(ta);
        sp.setPreferredSize(new Dimension(800, 500));
        JOptionPane.showMessageDialog(this, sp, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // =================================================================
    //  ACTION: ACCEPT TASK + Auto Daftar Kunjungan
    // =================================================================
    private void doAccept() {
        if (selSrId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih rujukan dulu.");
            return;
        }
        if (!"ranap".equals(selTipeRujukan) && !"igd".equals(selTipeRujukan)) {
            JOptionPane.showMessageDialog(this,
                    "Accept Task hanya untuk rujukan ranap/IGD.\n"
                    + "Untuk rujukan rajal, langsung pakai [Daftar Kunjungan].");
            return;
        }

        // Cari Task ID kalau belum ada
        if (selTaskId == null || selTaskId.isEmpty()) {
            try {
                JsonNode tResp = svc.cariTaskRujukanMasuk(selIdPasienSS);
                JsonNode entries = tResp.path("entry");
                if (entries.isArray()) {
                    for (JsonNode entry : entries) {
                        JsonNode task = entry.path("resource");
                        // Cari task yang valueReference-nya ke RS Anda
                        // (sederhana: pakai task pertama yang status='requested')
                        if ("requested".equals(task.path("status").asText())) {
                            selTaskId = task.path("id").asText();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Cari Task err: " + e);
            }
        }

        if (selTaskId == null || selTaskId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Task ID tidak ditemukan. Tidak bisa accept.\n"
                    + "Coba klik [Refresh dari Satu Sehat] dulu.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "ACCEPT rujukan dari " + selNamaFaskesPerujuk + "\n"
                + "untuk pasien " + selNamaPasien + "?\n\n"
                + "Setelah accept, sistem akan otomatis:\n"
                + "1. PATCH Task ke 'accepted' di Satu Sehat\n"
                + "2. Buat reg_periksa baru di SIMRS\n"
                + "3. Kirim Encounter ke Satu Sehat\n\n"
                + "Lanjut?",
                "Konfirmasi Accept", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Step 1: PATCH Task accepted
            setStatus("PATCH Task ke accepted...", false);
            JsonNode patchResp = svc.patchTaskAccept(selTaskId);
            System.out.println("Patch Task resp: " + patchResp);

            // Update DB lokal: status accepted
            updateStatusLokal("accepted", null);

            // Step 2: Auto-create reg_periksa lokal
            setStatus("Auto-create reg_periksa...", false);
            String noRawatBaru = autoCreateRegPeriksa();
            if (noRawatBaru == null || noRawatBaru.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Task sudah di-accept, tapi gagal auto-create reg_periksa.\n"
                        + "Lakukan pendaftaran manual via menu Pendaftaran.\n"
                        + "Status rujukan: ACCEPTED");
                refreshDetailDanTabel();
                return;
            }

            // Step 3: Kirim Encounter ke Satu Sehat
            setStatus("Kirim Encounter ke Satu Sehat...", false);
            String dpjpKtp = Sequel.cariIsi(
                    "select pegawai.no_ktp from reg_periksa "
                    + "inner join pegawai on reg_periksa.kd_dokter=pegawai.nik "
                    + "where reg_periksa.no_rawat=?", noRawatBaru);
            String idDokterSS = "";
            if (dpjpKtp != null && !dpjpKtp.isEmpty()) {
                idDokterSS = cekIhs.tampilIDParktisi(dpjpKtp);
            }

            String tipeKelas = "ranap".equals(selTipeRujukan) ? "IMP" : "EMER";
            JsonNode encResp = svc.kirimEncounterRujukanMasuk(
                    selIdPasienSS, idDokterSS, selSrId, tipeKelas, "");
            String idEncounter = svc.getEncounterId(encResp);

            // Simpan id_encounter ke satu_sehat_encounter (tabel existing)
            if (idEncounter != null && !idEncounter.isEmpty()) {
                try {
                    Sequel.menyimpantf2("satu_sehat_encounter", "?,?,?",
                            "Encounter", 3, new String[]{
                                    noRawatBaru,
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                    idEncounter
                            });
                } catch (Exception e) {
                    System.out.println("Simpan satu_sehat_encounter err: " + e);
                }
            }

            // Update bridging_rujukan_masuk: status=registered, no_rawat_lokal, id_encounter
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "update bridging_rujukan_masuk set status_lokal='registered', "
                    + "no_rawat_lokal=?, id_encounter_response=?, tgl_daftar=now() "
                    + "where service_request_id=?")) {
                ps.setString(1, noRawatBaru);
                ps.setString(2, idEncounter);
                ps.setString(3, selSrId);
                ps.executeUpdate();
            } catch (Exception e) {
                System.out.println("Update bridging_rujukan_masuk err: " + e);
            }

            JOptionPane.showMessageDialog(this,
                    "Rujukan berhasil diterima!\n\n"
                    + "No.Rawat baru: " + noRawatBaru + "\n"
                    + "ID Encounter: " + idEncounter + "\n\n"
                    + "Pasien sudah terdaftar di SIMRS Anda.");
            setStatus("Berhasil. No.Rawat: " + noRawatBaru, false);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshDetailDanTabel();
        }
    }

    // =================================================================
    //  ACTION: REJECT TASK
    // =================================================================
    private void doReject() {
        if (selSrId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih rujukan dulu.");
            return;
        }
        if (selTaskId == null || selTaskId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Task ID tidak ditemukan. Coba [Refresh] dulu.");
            return;
        }

        String alasan = JOptionPane.showInputDialog(this,
                "Alasan menolak rujukan:",
                "REJECT Rujukan", JOptionPane.QUESTION_MESSAGE);
        if (alasan == null) return;  // user cancel
        if (alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan tidak boleh kosong.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JsonNode resp = svc.patchTaskReject(selTaskId, alasan);
            System.out.println("Reject resp: " + resp);

            updateStatusLokal("rejected", alasan);

            JOptionPane.showMessageDialog(this,
                    "Rujukan ditolak.\nAlasan: " + alasan);
            setStatus("Rujukan rejected.", false);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshDetailDanTabel();
        }
    }

    // =================================================================
    //  ACTION: DAFTAR KUNJUNGAN (khusus rajal - tanpa accept Task)
    // =================================================================
    /**
     * Daftarkan kunjungan dari rujukan masuk - generic untuk rajal/ranap/IGD.
     * Alur:
     *   1. Auto-create reg_periksa (sesuai tipe rujukan)
     *   2. Kirim Encounter ke Satu Sehat dengan basedOn=ServiceRequest
     *   3. Untuk RANAP: tawarkan buka form pilih kamar inap setelahnya
     *   4. Update status di bridging_rujukan_masuk
     *
     * Note: untuk ranap/IGD, biasanya dipanggil OTOMATIS dari doAccept() setelah
     * PATCH Task accept berhasil. Tombol manual tetap bisa dipakai (idempotent).
     */
    private void doDaftarKunjungan() {
        if (selSrId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih rujukan dulu.");
            return;
        }

        // Cek apakah sudah pernah didaftarkan
        String existingNoRawat = Sequel.cariIsi(
                "select no_rawat_lokal from bridging_rujukan_masuk "
                + "where service_request_id=? and no_rawat_lokal is not null and no_rawat_lokal<>''",
                selSrId);
        if (existingNoRawat != null && !existingNoRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Rujukan ini sudah didaftarkan sebelumnya.\nNo.Rawat: " + existingNoRawat);
            return;
        }

        // Untuk ranap/IGD, validasi sudah accept dulu
        String statusLokal = Sequel.cariIsi(
                "select status_lokal from bridging_rujukan_masuk where service_request_id=?",
                selSrId);
        if (("ranap".equals(selTipeRujukan) || "igd".equals(selTipeRujukan))
                && !"accepted".equals(statusLokal)) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Rujukan " + selTipeRujukan.toUpperCase() + " ini belum di-ACCEPT.\n"
                    + "Lebih baik klik [ACCEPT] dulu (akan auto-daftar).\n\n"
                    + "Tetap lanjutkan daftar kunjungan saja?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        String tipeLabel = selTipeRujukan.toUpperCase();
        int conf = JOptionPane.showConfirmDialog(this,
                "Daftarkan kunjungan " + tipeLabel + " untuk pasien " + selNamaPasien + "?\n\n"
                + "Sistem akan otomatis:\n"
                + "1. Buat reg_periksa baru\n"
                + "2. Kirim Encounter ke Satu Sehat dengan basedOn ke rujukan ini"
                + ("ranap".equals(selTipeRujukan) ? "\n3. Tawarkan pilih kamar inap" : ""),
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        prosesDaftarKunjungan(false);
    }

    /**
     * Inti proses daftar kunjungan, dipanggil dari doDaftarKunjungan() (manual)
     * atau dari doAccept() (auto setelah PATCH accept).
     *
     * @param fromAcceptFlow true kalau dipanggil dari flow Accept (skip beberapa konfirmasi)
     */
    private void prosesDaftarKunjungan(boolean fromAcceptFlow) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // 1. Auto-create reg_periksa
            String noRawatBaru = autoCreateRegPeriksa();
            if (noRawatBaru == null) {
                // autoCreateRegPeriksa sudah tampil pesan errornya
                return;
            }

            // 2. Lookup IHS dokter dari reg_periksa yang baru dibuat
            String dpjpKtp = Sequel.cariIsi(
                    "select pegawai.no_ktp from reg_periksa "
                    + "inner join pegawai on reg_periksa.kd_dokter=pegawai.nik "
                    + "where reg_periksa.no_rawat=?", noRawatBaru);
            String idDokterSS = "";
            if (dpjpKtp != null && !dpjpKtp.isEmpty()) {
                idDokterSS = cekIhs.tampilIDParktisi(dpjpKtp);
            }

            // 3. Tentukan class code Encounter berdasarkan tipe rujukan
            String classCode;
            if ("ranap".equals(selTipeRujukan)) classCode = "IMP";       // inpatient
            else if ("igd".equals(selTipeRujukan)) classCode = "EMER";   // emergency
            else classCode = "AMB";                                      // ambulatory (rajal)

            // 4. Kirim Encounter ke Satu Sehat
            JsonNode encResp = svc.kirimEncounterRujukanMasuk(
                    selIdPasienSS, idDokterSS, selSrId, classCode, "");
            String idEncounter = svc.getEncounterId(encResp);

            // 5. Simpan Encounter ke tabel satu_sehat_encounter (existing)
            if (idEncounter != null && !idEncounter.isEmpty()) {
                try {
                    Sequel.menyimpantf2("satu_sehat_encounter", "?,?,?",
                            "Encounter", 3, new String[]{
                                    noRawatBaru,
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                    idEncounter
                            });
                } catch (Exception e) {
                    System.out.println("Simpan satu_sehat_encounter err: " + e);
                }
            }

            // 6. Update status di bridging_rujukan_masuk
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "update bridging_rujukan_masuk set status_lokal='registered', "
                    + "no_rawat_lokal=?, id_encounter_response=?, tgl_daftar=now(), "
                    + "petugas_daftar=? where service_request_id=?")) {
                ps.setString(1, noRawatBaru);
                ps.setString(2, idEncounter);
                ps.setString(3, user);
                ps.setString(4, selSrId);
                ps.executeUpdate();
            } catch (Exception e) {
                System.out.println("Update bridging_rujukan_masuk err: " + e);
            }

            // 7. Untuk RANAP: tawarkan buka form pilih kamar
            String pesanExtra = "";
            if ("ranap".equals(selTipeRujukan)) {
                pesanExtra = "\n\nUntuk RANAP, silakan lengkapi pilih kamar inap di:\n"
                        + "Menu Khanza > Rawat Inap > Pendaftaran Pasien Rawat Inap";
            }

            JOptionPane.showMessageDialog(this,
                    "Pendaftaran kunjungan " + selTipeRujukan.toUpperCase() + " berhasil!\n\n"
                    + "No.Rawat: " + noRawatBaru + "\n"
                    + "ID Encounter Satu Sehat: " + idEncounter
                    + pesanExtra);
            setStatus("Berhasil daftar kunjungan " + selTipeRujukan + ": " + noRawatBaru, false);

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshDetailDanTabel();
        }
    }

    /**
     * @deprecated Dipertahankan untuk backward compat - delegates ke doDaftarKunjungan().
     */
    @Deprecated
    private void doDaftarKunjunganRajal() {
        doDaftarKunjungan();
    }

    // =================================================================
    //  AUTO CREATE reg_periksa
    //
    //  CATATAN PENTING:
    //  Method ini melakukan auto-create reg_periksa SEDERHANA untuk demo
    //  alur. Di production, banyak field perlu disesuaikan dengan setup
    //  Khanza Anda (mis. kd_poli yang valid, kd_dokter default,
    //  generate no_rawat sesuai pattern existing, dll).
    //
    //  Field yang perlu disesuaikan:
    //  - kd_poli       : sekarang hardcode 'INT' (Penyakit Dalam) - ganti dengan poli umum / triase RS Anda
    //  - kd_dokter     : sekarang hardcode 'DOK001' - ganti dengan dokter triase / penanggungjawab default
    //  - kd_pj         : sekarang 'A01' - sesuaikan
    //  - generate no_rawat : sesuaikan dengan pattern Khanza Anda
    //
    //  RETURNS: no_rawat baru, atau null kalau gagal
    // =================================================================
    /**
     * Auto-create reg_periksa untuk pasien rujukan masuk.
     *
     * Flow:
     *  1. Cari pasien existing by NIK (paling reliable)
     *  2. Kalau tidak ada → tawarkan dialog [Daftar Pasien Baru] / [Manual] / [Batal]
     *     - [Daftar Pasien Baru]: buka form mini DaftarPasienBaru, dapat no_rkm_medis baru
     *     - [Manual]: minta user input no_rkm_medis existing manual
     *     - [Batal]: abort
     *  3. Generate no_rawat baru (pattern Khanza: yyyy/mm/dd/000NNN)
     *  4. INSERT reg_periksa
     *
     * @return no_rawat baru, atau null kalau gagal/dibatalkan user
     */
    private String autoCreateRegPeriksa() {
        // ====== Step 1: Cari pasien existing by NIK ======
        String noRkmMedis = lookupPasienExisting();

        // ====== Step 2: Pasien tidak ditemukan → tawarkan opsi ======
        if (noRkmMedis == null || noRkmMedis.isEmpty()) {
            noRkmMedis = handlePasienTidakDitemukan();
            if (noRkmMedis == null || noRkmMedis.isEmpty()) {
                // user batal atau gagal
                return null;
            }
        }

        // Update bridging_rujukan_masuk dengan no_rkm_medis yang ditemukan/baru
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_masuk set no_rkm_medis_lokal=? "
                + "where service_request_id=?")) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, selSrId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Update no_rkm_medis_lokal err: " + e);
        }

        // ====== Step 3: Generate no_rawat ======
        String noRawat = generateNoRawat();
        if (noRawat == null || noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gagal generate no_rawat baru.");
            return null;
        }

        // ====== Step 4: INSERT reg_periksa ======
        String tglNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String jamNow = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String statusLanjut = "ranap".equals(selTipeRujukan) ? "Ranap" : "Ralan";

        // Mapping default kd_poli per tipe rujukan
        // CATATAN: silakan sesuaikan dengan setup poliklinik RS Anda
        String kdPoliDefault;
        if ("igd".equals(selTipeRujukan)) {
            kdPoliDefault = "IGDK";  // poli IGD - sesuaikan dengan kd_poli IGD di RS Anda
        } else if ("ranap".equals(selTipeRujukan)) {
            kdPoliDefault = "ANK";   // poli rawat inap pertama (penyakit dalam/anak) - sesuaikan
        } else {
            kdPoliDefault = "INT";   // poli umum/triase rajal - sesuaikan
        }

        // Cari kd_dokter default (dokter triase / dokter pertama dari poli ini)
        String kdDokterDefault = Sequel.cariIsi(
                "select kd_dokter from dokter_klinik where kd_poli=? limit 1", kdPoliDefault);
        if (kdDokterDefault == null || kdDokterDefault.isEmpty()) {
            kdDokterDefault = Sequel.cariIsi(
                    "select kode_dokter from dokter limit 1", "");
        }
        if (kdDokterDefault == null || kdDokterDefault.isEmpty()) {
            kdDokterDefault = "DOK001";  // fallback hardcode
        }

        // kd_pj (penjamin): kalau ada no_kartu BPJS → A01 (BPJS), kalau tidak → UMUM
        String kdPj = (selNoKartuBpjs != null && !selNoKartuBpjs.isEmpty()) ? "A01" : "UMU";

        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into reg_periksa (no_reg, no_rawat, tgl_registrasi, jam_reg, "
                + "kd_dokter, no_rkm_medis, kd_poli, p_jawab, almt_pj, hubunganpj, "
                + "biaya_reg, stts, stts_daftar, status_lanjut, kd_pj, umurdaftar, "
                + "sttsumur, status_bayar, status_poli) "
                + "values ('001', ?, ?, ?, ?, ?, ?, '', '', '', "
                + "0, 'Belum', 'Baru', ?, ?, 0, 'Th', 'Belum Bayar', 'Lama')")) {
            ps.setString(1, noRawat);
            ps.setString(2, tglNow);
            ps.setString(3, jamNow);
            ps.setString(4, kdDokterDefault);
            ps.setString(5, noRkmMedis);
            ps.setString(6, kdPoliDefault);
            ps.setString(7, statusLanjut);
            ps.setString(8, kdPj);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Insert reg_periksa err: " + e);
            JOptionPane.showMessageDialog(this,
                    "Gagal create reg_periksa:\n" + e.getMessage()
                    + "\n\nKemungkinan ada field yang harus disesuaikan dengan setup RS Anda:"
                    + "\n  - kd_poli '" + kdPoliDefault + "' tidak ada di tabel poliklinik?"
                    + "\n  - kd_dokter '" + kdDokterDefault + "' tidak ada di tabel dokter?"
                    + "\n  - kd_pj '" + kdPj + "' tidak ada di tabel penjab?"
                    + "\n\nCek autoCreateRegPeriksa() di SatuSehatRujukanMasuk.java.");
            return null;
        }

        return noRawat;
    }

    /**
     * Cari pasien existing di tabel pasien lokal.
     * Strategi pencarian (urutan):
     *  1. Pernah daftar dari rujukan masuk sebelumnya (bridging_rujukan_masuk.no_rkm_medis_lokal)
     *  2. By NIK pasien (kalau ada di SR atau di Patient resource)
     *  3. By nama pasien + tgl lahir (last resort, manual confirmation)
     *
     * @return no_rkm_medis kalau ketemu, atau null kalau tidak ada match
     */
    private String lookupPasienExisting() {
        String noRkmMedis = "";

        // Strategi 1: cari di bridging_rujukan_masuk (kalau pasien pernah dirujuk masuk sebelumnya)
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rkm_medis_lokal from bridging_rujukan_masuk "
                + "where id_pasien_satusehat=? and no_rkm_medis_lokal is not null "
                + "and no_rkm_medis_lokal<>'' limit 1")) {
            ps.setString(1, selIdPasienSS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) noRkmMedis = safe(rs.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Lookup bridging_rujukan_masuk err: " + e);
        }
        if (!noRkmMedis.isEmpty()) {
            System.out.println("Pasien ditemukan via riwayat rujukan masuk: " + noRkmMedis);
            return noRkmMedis;
        }

        // Strategi 2: by NIK (kalau ada)
        if (selNoKtpPasien != null && !selNoKtpPasien.isEmpty()) {
            noRkmMedis = Sequel.cariIsi(
                    "select no_rkm_medis from pasien where no_ktp=?", selNoKtpPasien);
            if (noRkmMedis != null && !noRkmMedis.isEmpty()) {
                System.out.println("Pasien ditemukan via NIK: " + noRkmMedis);
                return noRkmMedis;
            }
        }

        // Strategi 3: by nama + tgl lahir (perlu konfirmasi user kalau ada match)
        if (selNamaPasien != null && !selNamaPasien.isEmpty()
                && selTglLahirPasien != null && !selTglLahirPasien.isEmpty()) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select no_rkm_medis, nm_pasien, no_ktp from pasien "
                    + "where nm_pasien=? and tgl_lahir=? limit 5")) {
                ps.setString(1, selNamaPasien);
                ps.setString(2, selTglLahirPasien);
                try (ResultSet rs = ps.executeQuery()) {
                    java.util.List<String> kandidat = new java.util.ArrayList<>();
                    java.util.List<String> labels = new java.util.ArrayList<>();
                    while (rs.next()) {
                        kandidat.add(rs.getString("no_rkm_medis"));
                        labels.add(rs.getString("no_rkm_medis") + " - "
                                + rs.getString("nm_pasien")
                                + " (NIK: " + safe(rs.getString("no_ktp")) + ")");
                    }
                    if (!kandidat.isEmpty()) {
                        // Tampilkan dialog confirm: ini pasien yang dimaksud?
                        Object pilihan = JOptionPane.showInputDialog(this,
                                "Ditemukan pasien dengan nama & tgl lahir cocok.\n"
                                + "Apakah ini pasien yang dirujuk?",
                                "Konfirmasi Pasien", JOptionPane.QUESTION_MESSAGE,
                                null, labels.toArray(), labels.get(0));
                        if (pilihan != null) {
                            int idx = labels.indexOf(pilihan.toString());
                            if (idx >= 0) {
                                noRkmMedis = kandidat.get(idx);
                                System.out.println("Pasien dipilih manual: " + noRkmMedis);
                                return noRkmMedis;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Lookup by nama+tgl err: " + e);
            }
        }

        // Tidak ada match
        return null;
    }

    /**
     * Ditampilkan kalau pasien tidak ditemukan di tabel pasien lokal.
     * Tawarkan 3 opsi:
     *  - [Daftar Pasien Baru]: buka form mini DaftarPasienBaru
     *  - [Manual Input No.RM]: user input no_rkm_medis existing
     *  - [Batal]: return null
     *
     * @return no_rkm_medis (baru atau existing) atau null kalau dibatalkan
     */
    private String handlePasienTidakDitemukan() {
        String[] options = {"Daftar Pasien Baru", "Input No.RM Manual", "Batal"};
        int pilih = JOptionPane.showOptionDialog(this,
                "Pasien rujukan TIDAK DITEMUKAN di SIMRS Anda:\n"
                + "  Nama: " + selNamaPasien + "\n"
                + "  NIK : " + (selNoKtpPasien.isEmpty() ? "(tidak ada)" : selNoKtpPasien) + "\n"
                + "  IHS : " + selIdPasienSS + "\n\n"
                + "Apa yang ingin Anda lakukan?",
                "Pasien Tidak Ditemukan",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (pilih == 0) {
            // [Daftar Pasien Baru] - buka form mini
            return bukaFormDaftarPasienBaru();
        } else if (pilih == 1) {
            // [Input Manual] - user ketik no_rkm_medis
            String input = JOptionPane.showInputDialog(this,
                    "Input No.RM existing untuk pasien ini:",
                    "Input No.RM", JOptionPane.QUESTION_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                String norm = input.trim();
                // Validasi: no_rkm_medis harus ada di tabel pasien
                String exists = Sequel.cariIsi(
                        "select no_rkm_medis from pasien where no_rkm_medis=?", norm);
                if (exists != null && !exists.isEmpty()) {
                    return exists;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No.RM " + norm + " tidak ditemukan di tabel pasien.");
                    return null;
                }
            }
            return null;
        } else {
            // [Batal]
            return null;
        }
    }

    /**
     * Buka form mini DaftarPasienBaru, return no_rkm_medis kalau berhasil.
     */
    private String bukaFormDaftarPasienBaru() {
        try {
            DaftarPasienBaru dlg = new DaftarPasienBaru(
                    (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), true);
            // Pre-fill data dari rujukan masuk
            dlg.preFill(selNoKtpPasien, selNamaPasien, selTglLahirPasien, selJkPasien);
            dlg.setVisible(true);
            return dlg.getNoRkmMedisHasil();  // null kalau dibatalkan
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal buka form Daftar Pasien Baru:\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate no_rawat baru sesuai pattern Khanza: yyyy/mm/dd/000NNN
     */
    private String generateNoRawat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String prefix = sdf.format(new Date());
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select max(no_rawat) from reg_periksa where no_rawat like ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    int nextSeq = 1;
                    if (last != null && last.length() == 17) {
                        try { nextSeq = Integer.parseInt(last.substring(11)) + 1; } catch (Exception e) {}
                    }
                    return prefix + "/" + String.format("%06d", nextSeq);
                }
            }
        } catch (Exception e) {
            System.out.println("Generate no_rawat err: " + e);
        }
        return null;
    }

    // =================================================================
    //  HELPERS
    // =================================================================
    private void updateStatusLokal(String newStatus, String alasan) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_masuk set status_lokal=?, "
                + "alasan_tolak=?, tgl_respon=now(), petugas_respon=? "
                + "where service_request_id=?")) {
            ps.setString(1, newStatus);
            ps.setString(2, alasan);
            ps.setString(3, user);
            ps.setString(4, selSrId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("updateStatusLokal err: " + e);
        }
    }

    private void refreshDetailDanTabel() {
        // Re-load tabel
        loadTabelDariDb();
        // Re-select baris yang sama (kalau masih ada)
        for (int i = 0; i < modelRujukan.getRowCount(); i++) {
            if (selNoRujukanSS.equals(String.valueOf(modelRujukan.getValueAt(i, 0)))) {
                tblRujukan.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void handleApiError(Exception ex) {
        ex.printStackTrace();
        String msg = ex.toString();
        JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + msg, true);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatRujukanMasuk dlg = new SatuSehatRujukanMasuk(new javax.swing.JFrame(), true);
            dlg.setVisible(true);
        });
    }
}