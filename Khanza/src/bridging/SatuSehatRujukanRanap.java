/*
 * SatuSehatRujukanRanap.java
 *
 * Form mandiri Rujukan Rawat Inap - LANGSUNG ke Satu Sehat (TIDAK lewat BPJS-K).
 *
 * Sesuai Buku Panduan SATUSEHAT (Playbook) Rujukan Pasien v5.1, 4 Maret 2026,
 * Bab 2.x Rujukan Rawat Inap dan Rawat Darurat.
 *
 * Layout:
 *   - Atas: tabel daftar pasien rawat inap aktif (status='Belum Pulang' di kamar_inap)
 *   - Tengah: panel info pasien terpilih + IHS info
 *   - Bawah: TabbedPane 4 tab (Pra → Cari Kandidat → Kirim Tugas → Rujukan Final)
 *
 * Flow:
 *   Tab 1: Kirim Task referral-pre-request → respon kuesioner kriteria
 *   Tab 2: Jawab kriteria + pilih wilayah → Task request-referral-candidate → list kandidat
 *   Tab 3: Pilih beberapa kandidat → POST Bundle (Task approval-request + CarePlan) per kandidat
 *          → poll status accept/reject
 *   Tab 4: Pilih 1 kandidat yang ACCEPT → POST ServiceRequest → dapat Nomor Rujukan Nasional
 *
 * Prerequisite per pasien terpilih:
 *   1. Encounter Satu Sehat sudah ada (cek di tabel satu_sehat_encounter)
 *   2. IHS Pasien terambil dari NIK pasien
 *   3. IHS Dokter terambil dari NIK DPJP
 *   4. Diagnosa ICD-10 sudah ada
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
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

public final class SatuSehatRujukanRanap extends javax.swing.JDialog {

    // ===== Helper Khanza =====
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    // ===== Service =====
    private final SatuSehatRujukanService svc = new SatuSehatRujukanService();
    private final SatuSehatCekNIK cekIhs = new SatuSehatCekNIK();

    // ===== State pasien terpilih =====
    private String selNoRawat = "";
    private String selNoRm = "";
    private String selNamaPasien = "";
    private String selKdPenyakit = "";
    private String selNmPenyakit = "";
    private String selKdDokter = "";    // kd_dokter Khanza (nik pegawai)
    private String selNoKtpPasien = "";
    private String selNoKtpDokter = "";

    private String selIdPasienSS = "";
    private String selIdDokterSS = "";
    private String selIdEncounterSS = "";
    private String selIdConditionSS = "";  // diisi setelah klik [Buat Condition]

    // ===== State per step =====
    private String taskPraId = "";
    private String taskCariId = "";

    // No.Rujukan internal (PK ke bridging_rujukan_satusehat_ranap)
    private String noRujukanInternal = "";
    private String user = "";

    // ===== Models =====
    private final DefaultTableModel modelPasien;
    private final DefaultTableModel modelKriteria;
    private final DefaultTableModel modelKandidat;
    private final DefaultTableModel modelTugas;
    private final DefaultTableModel modelAcceptedFaskes;

    // Provinsi preset
    private static final String[][] PROVINSI = {
        {"31", "DKI Jakarta"}, {"32", "Jawa Barat"}, {"33", "Jawa Tengah"},
        {"34", "DI Yogyakarta"}, {"35", "Jawa Timur"}, {"36", "Banten"},
        {"73", "Sulawesi Selatan"}
    };

    // Jenis transportasi preset (Lampiran 2 playbook)
    private static final String[][] TRANSPORT = {
        {"", "(tidak ada)"},
        {"49122002", "Ambulance (umum)"},
        {"1285128001", "Ambulans Gawat Darurat"},
        {"465341007", "Ambulance Transport (PSC)"},
        {"71783008", "Mobil"},
        {"90748009", "Motor"},
        {"74964007", "Lain-lain"}
    };

    public SatuSehatRujukanRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        modelPasien = new DefaultTableModel(null, new Object[]{
                "No.Rawat", "No.RM", "Nama Pasien", "Kamar", "Diagnosa",
                "Nama Diagnosa", "DPJP", "Tgl Masuk"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        modelKriteria = new DefaultTableModel(null, new Object[]{
                "linkId", "Pertanyaan Kriteria", "Tipe", "Jawaban (YA/TIDAK atau teks)"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };

        modelKandidat = new DefaultTableModel(null, new Object[]{
                "Pilih", "ID SS Faskes", "Kode BPJS", "Nama Faskes", "Strata", "Jarak (m)"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
        };

        modelTugas = new DefaultTableModel(null, new Object[]{
                "Task ID", "Faskes Tujuan", "Status", "Respon", "Tgl Kirim"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        modelAcceptedFaskes = new DefaultTableModel(null, new Object[]{
                "Pilih", "Task ID", "ID SS Faskes", "Nama Faskes", "Tgl Accept"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
        };

        initComponents();
        setSize(1180, 820);
        setLocationRelativeTo(parent);

        try {
            user = akses.getkode().replace(" ", "").substring(0, 9);
        } catch (Exception e) {
            user = akses.getkode();
        }

        // Auto-load data pasien saat form dibuka
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                tampilDataPasien();
            }
        });
    }

    /**
     * Dipanggil dari DlgKamarInap/DlgIGD agar pasien yang sedang dipilih
     * langsung terpilih saat form rujukan dibuka.
     * Method ini sengaja tidak mengubah alur utama; hanya mengisi pencarian,
     * reload tabel pasien aktif, lalu memilih baris yang no_rawat-nya sama.
     */
    public void setNoRawatTerpilih(String noRawat) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return;
        }

        String targetNoRawat = noRawat.trim();
        tCariPasien.setText(targetNoRawat);
        tampilDataPasien();

        boolean ditemukan = false;
        for (int i = 0; i < modelPasien.getRowCount(); i++) {
            if (targetNoRawat.equals(String.valueOf(modelPasien.getValueAt(i, 0)))) {
                tblPasien.setRowSelectionInterval(i, i);
                tblPasien.scrollRectToVisible(tblPasien.getCellRect(i, 0, true));
                onPasienSelected();
                setStatus("Pasien terpilih otomatis: " + targetNoRawat, false);
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            setStatus("No.Rawat " + targetNoRawat + " tidak ditemukan pada daftar pasien aktif. Silakan pilih manual.", true);
        }
    }


    // =================================================================
    //  initComponents - manual layout (tanpa .form)
    // =================================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rujukan Rawat Inap - Satu Sehat");

        frameMain = new widget.InternalFrame();
        frameMain.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Rujukan Rawat Inap - Satu Sehat ]::"));
        frameMain.setLayout(new BorderLayout(2, 2));

        // ============ TOP: tabel pasien ============
        widget.PanelBiasa panelTop = new widget.PanelBiasa();
        panelTop.setLayout(new BorderLayout(2, 2));
        panelTop.setPreferredSize(new Dimension(1180, 230));

        widget.PanelBiasa pnlCari = new widget.PanelBiasa();
        pnlCari.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        widget.Label lblCari = new widget.Label();
        lblCari.setText("Cari (No.RM/Nama):");
        pnlCari.add(lblCari);

        tCariPasien = new widget.TextBox();
        tCariPasien.setPreferredSize(new Dimension(200, 23));
        pnlCari.add(tCariPasien);

        btnCari = mkBtn("Cari", "/picture/Search-16x16.png");
        btnCari.addActionListener(e -> tampilDataPasien());
        pnlCari.add(btnCari);

        btnRefresh = mkBtn("Refresh", "/picture/refresh.png");
        btnRefresh.addActionListener(e -> { tCariPasien.setText(""); tampilDataPasien(); });
        pnlCari.add(btnRefresh);

        widget.Label lblInfo = new widget.Label();
        lblInfo.setText("  (klik salah satu baris untuk pilih pasien)");
        lblInfo.setForeground(new java.awt.Color(80, 80, 80));
        pnlCari.add(lblInfo);

        panelTop.add(pnlCari, BorderLayout.NORTH);

        tblPasien = new widget.Table();
        tblPasien.setModel(modelPasien);
        tblPasien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] wp = {130, 90, 200, 80, 70, 200, 130, 100};
        for (int i = 0; i < wp.length; i++) {
            tblPasien.getColumnModel().getColumn(i).setPreferredWidth(wp[i]);
        }
        tblPasien.setDefaultRenderer(Object.class, new WarnaTable());
        tblPasien.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting() && tblPasien.getSelectedRow() >= 0) {
                onPasienSelected();
            }
        });

        scrollPasien = new widget.ScrollPane();
        scrollPasien.setViewportView(tblPasien);
        panelTop.add(scrollPasien, BorderLayout.CENTER);

        frameMain.add(panelTop, BorderLayout.NORTH);

        // ============ MID: info pasien terpilih ============
        widget.PanelBiasa panelMid = new widget.PanelBiasa();
        panelMid.setLayout(null);
        panelMid.setPreferredSize(new Dimension(1180, 110));
        panelMid.setBorder(BorderFactory.createTitledBorder("Pasien Terpilih"));

        // Layout grid 3 kolom dengan jarak konsisten:
        //   Kolom 1: label x=10, field x=100, lebar field 200
        //   Kolom 2: label x=330, field x=420, lebar field 240
        //   Kolom 3: label x=685, field x=790, lebar field 220
        // Total kolom 3 ends: 790+220 = 1010 (cukup di panel 1180)
        int h = 22, gap = 26;
        int y = 20;

        // ===== Row 1: No.Rawat | Pasien | Encounter =====
        addLbl(panelMid, "No.Rawat:",  10, y,  90, h);
        tNoRawat = roField(panelMid, 100, y, 200, h);

        addLbl(panelMid, "Pasien:",   330, y,  90, h);
        tPasien = roField(panelMid,   420, y, 240, h);

        addLbl(panelMid, "Encounter:", 685, y,  90, h);
        tEncounter = roField(panelMid, 790, y, 220, h);
        y += gap;

        // ===== Row 2: Diagnosa | [Pilih] | IHS Pasien =====
        addLbl(panelMid, "Diagnosa:",  10, y,  90, h);
        tDiagnosa = roField(panelMid, 100, y, 480, h);

        btnPilihDiagnosa = mkBtn("Pilih", "/picture/Search-16x16.png");
        btnPilihDiagnosa.setBounds(585, y, 75, h);
        btnPilihDiagnosa.addActionListener(e -> doPilihDiagnosa());
        panelMid.add(btnPilihDiagnosa);

        addLbl(panelMid, "IHS Pasien:", 685, y, 90, h);
        tIdPasienIhs = roField(panelMid, 790, y, 220, h);
        y += gap;

        // ===== Row 3: IHS Dokter | IHS Condition | [Buat Condition] =====
        addLbl(panelMid, "IHS Dokter:",  10, y,  90, h);
        tIdDokterIhs = roField(panelMid, 100, y, 200, h);

        addLbl(panelMid, "IHS Condition:", 330, y, 95, h);
        tIdConditionIhs = roField(panelMid, 425, y, 235, h);

        btnBuatCondition = mkBtn("Buat Condition", "/picture/save-16x16.png");
        btnBuatCondition.setBounds(685, y, 150, h);
        btnBuatCondition.addActionListener(e -> doBuatCondition());
        panelMid.add(btnBuatCondition);

        frameMain.add(panelMid, BorderLayout.CENTER);

        // ============ BOTTOM: tabs ============
        widget.PanelBiasa panelBottom = new widget.PanelBiasa();
        panelBottom.setLayout(new BorderLayout(2, 2));
        panelBottom.setPreferredSize(new Dimension(1180, 460));

        tabPane = new JTabbedPane();
        tabPane.setFont(new java.awt.Font("Tahoma", 0, 11));

        pnlTab1 = buildTab1();
        pnlTab2 = buildTab2();
        pnlTab3 = buildTab3();
        pnlTab4 = buildTab4();

        tabPane.addTab("1. Pra Permintaan", pnlTab1);
        tabPane.addTab("2. Cari Kandidat", pnlTab2);
        tabPane.addTab("3. Kirim Tugas", pnlTab3);
        tabPane.addTab("4. Rujukan Final", pnlTab4);

        panelBottom.add(tabPane, BorderLayout.CENTER);

        // Bottom action bar
        widget.PanelBiasa pnlAction = new widget.PanelBiasa();
        pnlAction.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlAction.setPreferredSize(new Dimension(1180, 38));

        lblStatus = new widget.Label();
        lblStatus.setText("Status: pilih pasien dari tabel di atas.");
        lblStatus.setForeground(new java.awt.Color(0, 100, 0));
        lblStatus.setPreferredSize(new Dimension(900, 22));
        pnlAction.add(lblStatus);

        btnTutup = mkBtn("Tutup", "/picture/exit.png");
        btnTutup.addActionListener(e -> dispose());
        pnlAction.add(btnTutup);

        panelBottom.add(pnlAction, BorderLayout.SOUTH);

        frameMain.add(panelBottom, BorderLayout.SOUTH);

        getContentPane().add(frameMain, BorderLayout.CENTER);
        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    // =================================================================
    //  TAB 1: PRA PERMINTAAN
    // =================================================================
    private widget.PanelBiasa buildTab1() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new BorderLayout(2, 2));

        widget.PanelBiasa pnlTop = new widget.PanelBiasa();
        pnlTop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlTop.setPreferredSize(new Dimension(1180, 38));

        btnKirimPra = mkBtn("Kirim Pra Permintaan", "/picture/save-16x16.png");
        btnKirimPra.setPreferredSize(new Dimension(200, 28));
        btnKirimPra.addActionListener(e -> doKirimPra());
        pnlTop.add(btnKirimPra);

        widget.Label tip = new widget.Label();
        tip.setText("  → kirim Task referral-pre-request ke Satu Sehat. Pastikan Encounter & IHS sudah lengkap.");
        tip.setForeground(new java.awt.Color(80, 80, 80));
        pnlTop.add(tip);

        p.add(pnlTop, BorderLayout.NORTH);

        // Tabel kriteria
        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder("Kuesioner Kriteria Rujukan (isi jawaban di kolom kanan)"));
        frame.setLayout(new BorderLayout(1, 1));

        tblKriteria = new widget.Table();
        tblKriteria.setModel(modelKriteria);
        tblKriteria.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] wk = {80, 450, 90, 350};
        for (int i = 0; i < wk.length; i++) {
            tblKriteria.getColumnModel().getColumn(i).setPreferredWidth(wk[i]);
        }
        tblKriteria.setDefaultRenderer(Object.class, new WarnaTable());
        setupJawabanKriteriaEditor();

        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(tblKriteria);
        frame.add(sp, BorderLayout.CENTER);

        p.add(frame, BorderLayout.CENTER);

        lblTab1Status = new widget.Label();
        lblTab1Status.setText(" ");
        lblTab1Status.setPreferredSize(new Dimension(1180, 22));
        p.add(lblTab1Status, BorderLayout.SOUTH);

        return p;
    }

    // =================================================================
    //  TAB 2: CARI KANDIDAT
    // =================================================================
    private widget.PanelBiasa buildTab2() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new BorderLayout(2, 2));

        widget.PanelBiasa pnlTop = new widget.PanelBiasa();
        pnlTop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlTop.setPreferredSize(new Dimension(1180, 38));

        widget.Label lblProv = new widget.Label();
        lblProv.setText("Provinsi:");
        pnlTop.add(lblProv);

        cbProvinsi = new widget.ComboBox();
        String[] provItems = new String[PROVINSI.length];
        for (int i = 0; i < PROVINSI.length; i++) provItems[i] = PROVINSI[i][0] + " - " + PROVINSI[i][1];
        cbProvinsi.setModel(new javax.swing.DefaultComboBoxModel<>(provItems));
        cbProvinsi.setPreferredSize(new Dimension(220, 23));
        pnlTop.add(cbProvinsi);

        btnCariKandidat = mkBtn("Cari Kandidat", "/picture/Search-16x16.png");
        btnCariKandidat.setPreferredSize(new Dimension(180, 28));
        btnCariKandidat.addActionListener(e -> doCariKandidat());
        pnlTop.add(btnCariKandidat);

        widget.Label tip = new widget.Label();
        tip.setText("  → harus klik [Kirim Pra Permintaan] dulu di Tab 1 dan jawaban kriteria terisi.");
        tip.setForeground(new java.awt.Color(80, 80, 80));
        pnlTop.add(tip);

        p.add(pnlTop, BorderLayout.NORTH);

        // Tabel kandidat
        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder("Kandidat Fasyankes (centang yang akan dikirimi tugas rujukan, multi-select)"));
        frame.setLayout(new BorderLayout(1, 1));

        tblKandidat = new widget.Table();
        tblKandidat.setModel(modelKandidat);
        tblKandidat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] wkn = {50, 220, 90, 380, 90, 100};
        for (int i = 0; i < wkn.length; i++) {
            tblKandidat.getColumnModel().getColumn(i).setPreferredWidth(wkn[i]);
        }
        tblKandidat.setDefaultRenderer(Object.class, new WarnaTable());

        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(tblKandidat);
        frame.add(sp, BorderLayout.CENTER);

        p.add(frame, BorderLayout.CENTER);

        lblTab2Status = new widget.Label();
        lblTab2Status.setText(" ");
        lblTab2Status.setPreferredSize(new Dimension(1180, 22));
        p.add(lblTab2Status, BorderLayout.SOUTH);

        return p;
    }

    // =================================================================
    //  TAB 3: KIRIM TUGAS RUJUKAN
    // =================================================================
    private widget.PanelBiasa buildTab3() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new BorderLayout(2, 2));

        widget.PanelBiasa pnlTop = new widget.PanelBiasa();
        pnlTop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlTop.setPreferredSize(new Dimension(1180, 38));

        btnKirimTugas = mkBtn("Kirim Tugas ke Kandidat", "/picture/save-16x16.png");
        btnKirimTugas.setPreferredSize(new Dimension(200, 28));
        btnKirimTugas.addActionListener(e -> doKirimTugas());
        pnlTop.add(btnKirimTugas);

        btnCekStatusBaris = mkBtn("Cek Status Baris", "/picture/refresh.png");
        btnCekStatusBaris.setPreferredSize(new Dimension(160, 28));
        btnCekStatusBaris.addActionListener(e -> doCekStatusBaris());
        pnlTop.add(btnCekStatusBaris);

        btnCekStatusSemua = mkBtn("Cek Status Semua", "/picture/refresh.png");
        btnCekStatusSemua.setPreferredSize(new Dimension(160, 28));
        btnCekStatusSemua.addActionListener(e -> doCekStatusSemua());
        pnlTop.add(btnCekStatusSemua);

        widget.Label tip = new widget.Label();
        tip.setText("  → tunggu fasyankes tujuan kirim accept/reject. Tab 4 aktif setelah ada accept.");
        tip.setForeground(new java.awt.Color(80, 80, 80));
        pnlTop.add(tip);

        p.add(pnlTop, BorderLayout.NORTH);

        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder("Status Tugas Rujukan per Fasyankes Tujuan"));
        frame.setLayout(new BorderLayout(1, 1));

        tblTugas = new widget.Table();
        tblTugas.setModel(modelTugas);
        tblTugas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] wt = {220, 380, 130, 130, 160};
        for (int i = 0; i < wt.length; i++) {
            tblTugas.getColumnModel().getColumn(i).setPreferredWidth(wt[i]);
        }
        tblTugas.setDefaultRenderer(Object.class, new WarnaTable());

        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(tblTugas);
        frame.add(sp, BorderLayout.CENTER);

        p.add(frame, BorderLayout.CENTER);

        lblTab3Status = new widget.Label();
        lblTab3Status.setText(" ");
        lblTab3Status.setPreferredSize(new Dimension(1180, 22));
        p.add(lblTab3Status, BorderLayout.SOUTH);

        return p;
    }

    // =================================================================
    //  TAB 4: RUJUKAN FINAL
    // =================================================================
    private widget.PanelBiasa buildTab4() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new BorderLayout(2, 2));

        // Section 1 - tabel faskes accepted
        widget.InternalFrame frameTbl = new widget.InternalFrame();
        frameTbl.setBorder(BorderFactory.createTitledBorder("Pilih 1 Fasyankes Tujuan FINAL (yang sudah accept)"));
        frameTbl.setLayout(new BorderLayout(1, 1));
        frameTbl.setPreferredSize(new Dimension(1180, 180));

        tblAcceptedFaskes = new widget.Table();
        tblAcceptedFaskes.setModel(modelAcceptedFaskes);
        tblAcceptedFaskes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] waf = {50, 220, 220, 360, 160};
        for (int i = 0; i < waf.length; i++) {
            tblAcceptedFaskes.getColumnModel().getColumn(i).setPreferredWidth(waf[i]);
        }
        tblAcceptedFaskes.setDefaultRenderer(Object.class, new WarnaTable());

        // Single-select (uncheck others when one is checked)
        modelAcceptedFaskes.addTableModelListener(e -> {
            if (e.getColumn() == 0 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (Boolean.TRUE.equals(modelAcceptedFaskes.getValueAt(row, 0))) {
                    for (int i = 0; i < modelAcceptedFaskes.getRowCount(); i++) {
                        if (i != row) modelAcceptedFaskes.setValueAt(Boolean.FALSE, i, 0);
                    }
                }
            }
        });

        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(tblAcceptedFaskes);
        frameTbl.add(sp, BorderLayout.CENTER);
        p.add(frameTbl, BorderLayout.NORTH);

        // Section 2 - input opsional + tombol
        widget.PanelBiasa pnlInput = new widget.PanelBiasa();
        pnlInput.setLayout(null);
        pnlInput.setPreferredSize(new Dimension(1180, 200));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Data Pelengkap (opsional)"));

        int x1 = 10, x2 = 130, w1 = 120, w2 = 280, h = 23, gap = 28;
        int y = 25;

        addLbl(pnlInput, "No.Rujukan PCare:", x1, y, w1, h);
        tNoRujukanPCare = new widget.TextBox();
        tNoRujukanPCare.setBounds(x2, y, w2, h);
        pnlInput.add(tNoRujukanPCare);

        addLbl(pnlInput, "No.Kartu BPJS:", x2 + w2 + 30, y, w1, h);
        tNoKartuAsuransi = new widget.TextBox();
        tNoKartuAsuransi.setBounds(x2 + w2 + 30 + w1, y, w2, h);
        pnlInput.add(tNoKartuAsuransi);
        y += gap;

        addLbl(pnlInput, "Jenis Transport:", x1, y, w1, h);
        cbJenisTransport = new widget.ComboBox();
        String[] trItems = new String[TRANSPORT.length];
        for (int i = 0; i < TRANSPORT.length; i++) trItems[i] = TRANSPORT[i][1];
        cbJenisTransport.setModel(new javax.swing.DefaultComboBoxModel<>(trItems));
        cbJenisTransport.setBounds(x2, y, w2, h);
        pnlInput.add(cbJenisTransport);

        addLbl(pnlInput, "No.Reg.Ambulans:", x2 + w2 + 30, y, w1, h);
        tNoRegAmbulans = new widget.TextBox();
        tNoRegAmbulans.setBounds(x2 + w2 + 30 + w1, y, w2, h);
        pnlInput.add(tNoRegAmbulans);
        y += gap + 5;

        btnKirimFinal = mkBtn("Kirim Rujukan Final", "/picture/save-16x16.png");
        btnKirimFinal.setBounds(x1, y, 220, 32);
        btnKirimFinal.addActionListener(e -> doKirimFinal());
        pnlInput.add(btnKirimFinal);

        addLbl(pnlInput, "Nomor Rujukan Nasional:", x1 + 240, y + 5, 160, h);
        lblNoRujukanNasional = new widget.Label();
        lblNoRujukanNasional.setBounds(x1 + 405, y + 5, 500, h);
        lblNoRujukanNasional.setText("-");
        lblNoRujukanNasional.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12));
        lblNoRujukanNasional.setForeground(new java.awt.Color(0, 100, 0));
        pnlInput.add(lblNoRujukanNasional);

        p.add(pnlInput, BorderLayout.CENTER);

        lblTab4Status = new widget.Label();
        lblTab4Status.setText(" ");
        lblTab4Status.setPreferredSize(new Dimension(1180, 22));
        p.add(lblTab4Status, BorderLayout.SOUTH);

        return p;
    }

    // =================================================================
    //  HELPER UI
    // =================================================================
    private void addLbl(widget.PanelBiasa parent, String text, int x, int y, int w, int h) {
        widget.Label l = new widget.Label();
        l.setText(text);
        l.setBounds(x, y, w, h);
        parent.add(l);
    }

    /** Helper untuk bikin widget.Label dengan text - widget Khanza pakai constructor no-args. */
    private widget.Label makeLabelText(String text) {
        widget.Label l = new widget.Label();
        l.setText(text);
        return l;
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
        b.setPreferredSize(new Dimension(120, 28));
//        b.setGlassColor(new java.awt.Color(255, 255, 255));
        return b;
    }



    private void setupJawabanKriteriaEditor() {
        try {
            if (tblKriteria != null && tblKriteria.getColumnModel().getColumnCount() > 3) {
                tblKriteria.getColumnModel().getColumn(3).setCellEditor(new KriteriaJawabanCellEditor(tblKriteria));
                tblKriteria.getColumnModel().getColumn(3).setCellRenderer(new KriteriaJawabanRenderer());
                tblKriteria.setRowHeight(24);
            }
        } catch (Exception e) {
            System.out.println("Gagal setup editor jawaban kriteria: " + e.getMessage());
        }
    }

    private String defaultJawabanUntukTipe(String tipe) {
        return "boolean".equalsIgnoreCase(safe(tipe)) ? "YA" : "";
    }

    private String normalisasiBooleanKriteria(String jawaban) {
        String v = safe(jawaban).trim().toLowerCase();
        if (v.equals("ya") || v.equals("y") || v.equals("true") || v.equals("1")) return "true";
        if (v.equals("tidak") || v.equals("tdk") || v.equals("t") || v.equals("false") || v.equals("0")) return "false";
        return null;
    }

    private static class KriteriaJawabanCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final javax.swing.JComboBox<String> cmbBoolean = new javax.swing.JComboBox<>(new String[]{"YA", "TIDAK"});
        private final javax.swing.JTextField txtText = new javax.swing.JTextField();
        private java.awt.Component active;
        KriteriaJawabanCellEditor(javax.swing.JTable table) {}
        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            String tipe = String.valueOf(table.getModel().getValueAt(modelRow, 2));
            if ("boolean".equalsIgnoreCase(tipe)) {
                String val = value == null ? "YA" : value.toString().trim();
                if ("true".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val)) val = "YA";
                if ("false".equalsIgnoreCase(val) || "0".equals(val) || "T".equalsIgnoreCase(val) || "TDK".equalsIgnoreCase(val)) val = "TIDAK";
                if (!"YA".equalsIgnoreCase(val) && !"TIDAK".equalsIgnoreCase(val)) val = "YA";
                cmbBoolean.setSelectedItem(val.toUpperCase());
                active = cmbBoolean;
                return cmbBoolean;
            }
            txtText.setText(value == null ? "" : value.toString());
            active = txtText;
            return txtText;
        }
        @Override public Object getCellEditorValue() {
            return active == cmbBoolean ? cmbBoolean.getSelectedItem() : txtText.getText();
        }
    }

    private static class KriteriaJawabanRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                int modelRow = table.convertRowIndexToModel(row);
                String tipe = String.valueOf(table.getModel().getValueAt(modelRow, 2));
                if ("boolean".equalsIgnoreCase(tipe)) {
                    String val = value == null ? "" : value.toString().trim();
                    if ("true".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val)) val = "YA";
                    if ("false".equalsIgnoreCase(val) || "0".equals(val) || "T".equalsIgnoreCase(val) || "TDK".equalsIgnoreCase(val)) val = "TIDAK";
                    setText(val.isEmpty() ? "Pilih YA/TIDAK" : val);
                }
            } catch (Exception ignore) {}
            return c;
        }
    }
    private void setStatus(String msg, boolean error) {
        lblStatus.setText("Status: " + msg);
        lblStatus.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
        System.out.println("[RujukanRanap] " + msg);
    }

    private void setStatusTab(widget.Label lbl, String msg, boolean error) {
        lbl.setText(msg);
        lbl.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
    }

    // =================================================================
    //  ACTIONS - DAFTAR PASIEN
    // =================================================================

    private void tampilDataPasien() {
        modelPasien.setRowCount(0);
        String key = "%" + tCariPasien.getText().trim() + "%";

        // Query: pasien rawat inap aktif (status_pulang='-' di kamar_inap)
        // Diagnosa diambil dari tabel diagnosa_pasien (prioritas=1 untuk diagnosa primer)
        String sql = "select ki.no_rawat, p.no_rkm_medis, p.nm_pasien, ki.kd_kamar, "
                + "ifnull(dp.kd_penyakit,'') as kd_penyakit, "
                + "ifnull(py.nm_penyakit,'') as nm_penyakit, "
                + "concat(pg.nik,' - ',pg.nama) as dpjp, ki.tgl_masuk "
                + "from kamar_inap ki "
                + "inner join reg_periksa rp on ki.no_rawat = rp.no_rawat "
                + "inner join pasien p on rp.no_rkm_medis = p.no_rkm_medis "
                + "left join diagnosa_pasien dp on dp.no_rawat = rp.no_rawat and dp.prioritas = '1' "
                + "left join penyakit py on dp.kd_penyakit = py.kd_penyakit "
                + "left join pegawai pg on rp.kd_dokter = pg.nik "
                + "where ki.stts_pulang = '-' "
                + "and (p.no_rkm_medis like ? or p.nm_pasien like ?) "
                + "order by ki.tgl_masuk desc limit 200";

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelPasien.addRow(new Object[]{
                            safe(rs.getString(1)), safe(rs.getString(2)), safe(rs.getString(3)),
                            safe(rs.getString(4)), safe(rs.getString(5)), safe(rs.getString(6)),
                            safe(rs.getString(7)), safe(rs.getString(8))
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load pasien: " + e.getMessage());
            e.printStackTrace();
        }
        setStatus("Total pasien rawat inap aktif: " + modelPasien.getRowCount(), false);
    }

    private void onPasienSelected() {
        int row = tblPasien.getSelectedRow();
        if (row < 0) return;

        // Reset state
        resetWorkflow();

        selNoRawat = String.valueOf(modelPasien.getValueAt(row, 0));
        selNoRm = String.valueOf(modelPasien.getValueAt(row, 1));
        selNamaPasien = String.valueOf(modelPasien.getValueAt(row, 2));
        selKdPenyakit = String.valueOf(modelPasien.getValueAt(row, 4));
        selNmPenyakit = String.valueOf(modelPasien.getValueAt(row, 5));

        tNoRawat.setText(selNoRawat);
        tPasien.setText(selNoRm + " - " + selNamaPasien);
        tDiagnosa.setText(selKdPenyakit + " - " + selNmPenyakit);

        // Lookup data Satu Sehat
        loadDataSatuSehat();

        // No.Rujukan internal generated
        noRujukanInternal = "RJK-RI-" + System.currentTimeMillis();
    }

    private void resetWorkflow() {
        modelKriteria.setRowCount(0);
        modelKandidat.setRowCount(0);
        modelTugas.setRowCount(0);
        modelAcceptedFaskes.setRowCount(0);
        taskPraId = "";
        taskCariId = "";
        selIdConditionSS = "";
        tIdConditionIhs.setText("");
        tNoRujukanPCare.setText("");
        tNoKartuAsuransi.setText("");
        tNoRegAmbulans.setText("");
        cbJenisTransport.setSelectedIndex(0);
        lblNoRujukanNasional.setText("-");
        setStatusTab(lblTab1Status, " ", false);
        setStatusTab(lblTab2Status, " ", false);
        setStatusTab(lblTab3Status, " ", false);
        setStatusTab(lblTab4Status, " ", false);
        tabPane.setSelectedIndex(0);
    }

    private void loadDataSatuSehat() {
        // 1. Encounter dari satu_sehat_encounter
        selIdEncounterSS = Sequel.cariIsi(
                "select id_encounter from satu_sehat_encounter where no_rawat=?", selNoRawat);
        tEncounter.setText(selIdEncounterSS);

        // 2. NIK pasien & dokter
        selNoKtpPasien = Sequel.cariIsi(
                "select no_ktp from pasien where no_rkm_medis=?", selNoRm);
        selKdDokter = Sequel.cariIsi(
                "select kd_dokter from reg_periksa where no_rawat=?", selNoRawat);
        if (selKdDokter != null && !selKdDokter.isEmpty()) {
            selNoKtpDokter = Sequel.cariIsi(
                    "select no_ktp from pegawai where nik=?", selKdDokter);
        }

        // 3. IHS via SatuSehatCekNIK
        try {
            if (selNoKtpPasien != null && !selNoKtpPasien.isEmpty()) {
                selIdPasienSS = cekIhs.tampilIDPasien(selNoKtpPasien);
            }
            if (selNoKtpDokter != null && !selNoKtpDokter.isEmpty()) {
                selIdDokterSS = cekIhs.tampilIDParktisi(selNoKtpDokter);
            }
        } catch (Exception e) {
            System.out.println("Gagal lookup IHS: " + e);
        }

        tIdPasienIhs.setText(safe(selIdPasienSS));
        tIdDokterIhs.setText(safe(selIdDokterSS));

        // 4. Cek apakah Condition sudah pernah dikirim untuk diagnosa ini
        try {
            // Pakai PreparedStatement langsung karena Sequel.cariIsi cuma 1 param
            String existingCondId = "";
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select id_condition from satu_sehat_condition "
                    + "where no_rawat=? and kd_penyakit=? "
                    + "order by id_condition desc limit 1")) {
                ps.setString(1, selNoRawat);
                ps.setString(2, selKdPenyakit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existingCondId = safe(rs.getString(1));
                }
            }
            if (!existingCondId.isEmpty()) {
                selIdConditionSS = existingCondId;
                tIdConditionIhs.setText(existingCondId);
            }
        } catch (Exception e) {
            System.out.println("Cek existing Condition: " + e);
        }

        // 5. Validasi prerequisite
        StringBuilder warn = new StringBuilder();
        if (selIdEncounterSS == null || selIdEncounterSS.isEmpty()) {
            warn.append("\n- Encounter Satu Sehat BELUM ADA untuk no_rawat ini.");
        }
        if (selIdPasienSS == null || selIdPasienSS.isEmpty()) {
            warn.append("\n- IHS Pasien BELUM ADA (cek NIK pasien).");
        }
        if (selIdDokterSS == null || selIdDokterSS.isEmpty()) {
            warn.append("\n- IHS Dokter BELUM ADA (cek NIK DPJP).");
        }
        if (selKdPenyakit == null || selKdPenyakit.isEmpty()) {
            warn.append("\n- Diagnosa ICD-10 BELUM ADA di reg_periksa.");
        }

        if (warn.length() > 0) {
            int opt = JOptionPane.showOptionDialog(this,
                    "Data Satu Sehat untuk pasien ini belum lengkap:" + warn.toString()
                    + "\n\nUntuk melengkapi Encounter, buka form 'Satu Sehat Kirim Encounter'.\n\n"
                    + "Buka form tersebut sekarang?",
                    "Data Tidak Lengkap", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE, null,
                    new Object[]{"Buka Form Encounter", "Tutup"}, "Tutup");

            if (opt == JOptionPane.YES_OPTION) {
                bukaFormEncounter();
            }
            setStatus("PERINGATAN: data tidak lengkap, lengkapi dulu.", true);
        } else {
            setStatus("Data lengkap. Lanjut ke Tab 1: Pra Permintaan.", false);
        }
    }

    private void bukaFormEncounter() {
        try {
            // Cari window parent (frmUtama / PanelUtama) supaya form Encounter
            // bisa diset fullscreen seperti pattern Khanza standar.
            java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
            java.awt.Frame parentFrame = (parentWindow instanceof java.awt.Frame)
                    ? (java.awt.Frame) parentWindow : null;

            SatuSehatKirimEncounter dlg = new SatuSehatKirimEncounter(parentFrame, false);

            // Ukuran form: pakai ukuran parent supaya tidak terpotong
            if (parentWindow != null) {
                // Set ke 90% ukuran parent supaya tetap kelihatan jelas
                int w = (int) (parentWindow.getWidth() * 0.95);
                int h = (int) (parentWindow.getHeight() * 0.95);
                dlg.setSize(Math.max(w, 1180), Math.max(h, 700));
            } else {
                // Fallback: ukuran cukup besar
                dlg.setSize(1280, 720);
            }
            dlg.setLocationRelativeTo(parentWindow);
            dlg.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal buka form Satu Sehat Kirim Encounter:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // =================================================================
    //  ACTION - PILIH DIAGNOSA (popup picker dari tabel penyakit lokal)
    //  Berguna kalau pasien belum punya diagnosa di diagnosa_pasien,
    //  atau dokter mau ganti diagnosa untuk rujukan.
    // =================================================================
    private void doPilihDiagnosa() {
        if (selNoRawat == null || selNoRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Pilih pasien dulu dari tabel di atas.");
            return;
        }

        // Buka popup dialog picker penyakit
        PenyakitPicker picker = new PenyakitPicker(
                javax.swing.SwingUtilities.getWindowAncestor(this));
        picker.setVisible(true);

        // Setelah dialog ditutup, cek hasilnya
        if (picker.isPilih()) {
            String kdPenyakit = picker.getKdPenyakit();
            String nmPenyakit = picker.getNmPenyakit();

            // Update state form
            selKdPenyakit = kdPenyakit;
            selNmPenyakit = nmPenyakit;
            tDiagnosa.setText(kdPenyakit + " - " + nmPenyakit);

            // Karena ganti diagnosa, ID Condition lama jadi tidak relevan -> reset
            selIdConditionSS = "";
            tIdConditionIhs.setText("");

            // Cek apakah Condition untuk diagnosa baru sudah pernah dikirim
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select id_condition from satu_sehat_condition "
                    + "where no_rawat=? and kd_penyakit=? "
                    + "order by id_condition desc limit 1")) {
                ps.setString(1, selNoRawat);
                ps.setString(2, kdPenyakit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String existingId = safe(rs.getString(1));
                        if (!existingId.isEmpty()) {
                            selIdConditionSS = existingId;
                            tIdConditionIhs.setText(existingId);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Cek Condition setelah ganti diagnosa: " + e);
            }

            // Optional: update/insert ke diagnosa_pasien supaya konsisten
            // (hanya kalau belum ada record dengan kd_penyakit ini di no_rawat tsb)
            try {
                String existing = "";
                try (PreparedStatement psCek = koneksi.prepareStatement(
                        "select kd_penyakit from diagnosa_pasien where no_rawat=? and kd_penyakit=?")) {
                    psCek.setString(1, selNoRawat);
                    psCek.setString(2, kdPenyakit);
                    try (ResultSet rs = psCek.executeQuery()) {
                        if (rs.next()) existing = rs.getString(1);
                    }
                }
                if (existing.isEmpty()) {
                    int conf = JOptionPane.showConfirmDialog(this,
                            "Diagnosa ini belum tercatat di diagnosa pasien.\n"
                            + "Tambahkan ke diagnosa_pasien (sebagai diagnosa primer rujukan)?\n\n"
                            + "Pilih YA = otomatis catat sebagai diagnosa primer.\n"
                            + "Pilih TIDAK = hanya pakai untuk rujukan saja, tidak disimpan ke RM.",
                            "Konfirmasi Tambah Diagnosa",
                            JOptionPane.YES_NO_OPTION);
                    if (conf == JOptionPane.YES_OPTION) {
                        // Insert dengan prioritas=1 (primer), status='Ralan' default ranap
                        Sequel.menyimpantf2("diagnosa_pasien",
                                "?,?,?,?", "Diagnosa Pasien", 4,
                                new String[]{
                                    selNoRawat,
                                    kdPenyakit,
                                    "Ranap",       // status
                                    "1"            // prioritas (primer)
                                });
                    }
                }
            } catch (Exception e) {
                System.out.println("Update diagnosa_pasien: " + e);
            }

            JOptionPane.showMessageDialog(this,
                    "Diagnosa rujukan diset ke:\n" + kdPenyakit + " - " + nmPenyakit
                    + (selIdConditionSS.isEmpty()
                        ? "\n\nCondition belum dikirim - klik [Buat Condition] untuk mengirim."
                        : "\n\nCondition sudah ada: " + selIdConditionSS));
        }
    }

    // =================================================================
    //  Inner class: PenyakitPicker - popup dialog cari penyakit dari DB lokal
    // =================================================================
    private class PenyakitPicker extends javax.swing.JDialog {
        private final widget.TextBox tCariPenyakit = new widget.TextBox();
        private final DefaultTableModel modelPenyakit;
        private final widget.Table tabelPenyakit = new widget.Table();
        private boolean pilih = false;
        private String kdPenyakitTerpilih = "";
        private String nmPenyakitTerpilih = "";

        public PenyakitPicker(java.awt.Window owner) {
            super(owner, "Cari Penyakit (ICD-10)", java.awt.Dialog.ModalityType.APPLICATION_MODAL);

            modelPenyakit = new DefaultTableModel(
                    new Object[]{"Kode ICD-10", "Nama Penyakit"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tabelPenyakit.setModel(modelPenyakit);
            tabelPenyakit.getColumnModel().getColumn(0).setPreferredWidth(120);
            tabelPenyakit.getColumnModel().getColumn(1).setPreferredWidth(500);
            tabelPenyakit.getSelectionModel().setSelectionMode(
                    javax.swing.ListSelectionModel.SINGLE_SELECTION);

            // Double-click untuk pilih
            tabelPenyakit.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) doPilih();
                }
            });

            buildUi();
            loadData("");

            setSize(700, 500);
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            setLayout(new BorderLayout(4, 4));

            // Top: search bar
            widget.PanelBiasa panelTop = new widget.PanelBiasa();
            panelTop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            panelTop.add(makeLabelText("Cari (kode/nama):"));
            tCariPenyakit.setPreferredSize(new Dimension(300, 24));
            tCariPenyakit.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override public void keyReleased(java.awt.event.KeyEvent e) {
                    loadData(tCariPenyakit.getText().trim());
                }
            });
            panelTop.add(tCariPenyakit);
            widget.Button btnCari = mkBtn("Cari", "/picture/Search-16x16.png");
            btnCari.addActionListener(e -> loadData(tCariPenyakit.getText().trim()));
            panelTop.add(btnCari);
            add(panelTop, BorderLayout.NORTH);

            // Center: tabel
            widget.ScrollPane sp = new widget.ScrollPane();
            sp.setViewportView(tabelPenyakit);
            add(sp, BorderLayout.CENTER);

            // Bottom: tombol
            widget.PanelBiasa panelBtm = new widget.PanelBiasa();
            panelBtm.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            widget.Button btnPilih = mkBtn("Pilih", "/picture/save-16x16.png");
            btnPilih.addActionListener(e -> doPilih());
            panelBtm.add(btnPilih);
            widget.Button btnBatal = mkBtn("Batal", "/picture/exit-16x16.png");
            btnBatal.addActionListener(e -> dispose());
            panelBtm.add(btnBatal);
            add(panelBtm, BorderLayout.SOUTH);
        }

        private void loadData(String key) {
            modelPenyakit.setRowCount(0);
            String like = "%" + key + "%";
            String sql = "select kd_penyakit, nm_penyakit from penyakit "
                    + "where kd_penyakit like ? or nm_penyakit like ? "
                    + "order by kd_penyakit limit 200";
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, like);
                ps.setString(2, like);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modelPenyakit.addRow(new Object[]{
                            safe(rs.getString(1)), safe(rs.getString(2))
                        });
                    }
                }
            } catch (Exception ex) {
                System.out.println("Load penyakit: " + ex);
            }
        }

        private void doPilih() {
            int row = tabelPenyakit.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris dulu.");
                return;
            }
            kdPenyakitTerpilih = String.valueOf(modelPenyakit.getValueAt(row, 0));
            nmPenyakitTerpilih = String.valueOf(modelPenyakit.getValueAt(row, 1));
            pilih = true;
            dispose();
        }

        public boolean isPilih()       { return pilih; }
        public String getKdPenyakit()  { return kdPenyakitTerpilih; }
        public String getNmPenyakit()  { return nmPenyakitTerpilih; }
    }

    // =================================================================
    //  ACTION - BUAT CONDITION
    //  Cek dulu apakah Condition sudah ada di satu_sehat_condition (tabel existing).
    //  Kalau ada, ambil id-nya (skip POST). Kalau belum, POST baru lalu simpan.
    // =================================================================
    private void doBuatCondition() {
        if (!validatePasienTerpilih()) return;
        if (selIdEncounterSS.isEmpty() || selIdPasienSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Encounter / IHS Pasien belum lengkap.");
            return;
        }

        // Step 1: cek tabel existing satu_sehat_condition dulu
        // Struktur: no_rawat, kd_penyakit, status, id_condition
        // Untuk diagnosa primer rawat inap, status biasanya 'Inpatient' atau sesuai reg_periksa.status_lanjut
        String statusCondition = Sequel.cariIsi(
                "select status_lanjut from reg_periksa where no_rawat=?", selNoRawat);
        if (statusCondition == null || statusCondition.isEmpty()) {
            statusCondition = "Ranap";
        }

        String existingId = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select id_condition from satu_sehat_condition "
                + "where no_rawat=? and kd_penyakit=? "
                + "order by id_condition desc limit 1")) {
            ps.setString(1, selNoRawat);
            ps.setString(2, selKdPenyakit);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) existingId = safe(rs.getString(1));
            }
        } catch (Exception ex) {
            System.out.println("Cek satu_sehat_condition gagal: " + ex);
        }

        if (!existingId.isEmpty()) {
            selIdConditionSS = existingId;
            tIdConditionIhs.setText(existingId);
            setStatus("Condition sudah ada (re-use). ID: " + existingId, false);
            JOptionPane.showMessageDialog(this,
                    "Condition untuk diagnosa ini sudah pernah dikirim ke Satu Sehat.\n"
                    + "ID Condition: " + existingId + "\n\n"
                    + "Tidak perlu POST ulang.");
            return;
        }

        // Step 2: belum ada, POST baru
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JsonNode resp = svc.kirimCondition(
                    selIdPasienSS, selIdEncounterSS, selNamaPasien,
                    selKdPenyakit, selNmPenyakit, "encounter-diagnosis");

            String id = svc.getConditionId(resp);
            if (id == null || id.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Gagal buat Condition. Response:\n" + resp);
                setStatus("Gagal buat Condition.", true);
                return;
            }
            selIdConditionSS = id;
            tIdConditionIhs.setText(id);

            // Simpan ke tabel existing satu_sehat_condition (4 kolom)
            try {
                Sequel.menyimpantf2("satu_sehat_condition", "?,?,?,?",
                        "Diagnosa", 4, new String[]{
                                selNoRawat, selKdPenyakit, statusCondition, id
                        });
            } catch (Exception e) {
                System.out.println("Simpan satu_sehat_condition gagal: " + e);
            }

            setStatus("Condition berhasil dibuat. ID: " + id, false);
            JOptionPane.showMessageDialog(this,
                    "Condition diagnosa berhasil dibuat di Satu Sehat.\nID: " + id);
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  TAB 1 ACTION
    // =================================================================
    private void doKirimPra() {
        if (!validatePasienTerpilih()) return;
        if (!validateIhsLengkap()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab1Status, "Mengirim pra permintaan...", false);
        try {
            JsonNode resp = svc.kirimPraPermintaan(
                    selIdPasienSS, selIdEncounterSS, selKdPenyakit, selNmPenyakit);
            taskPraId = resp.path("id").asText();

            // Server bisa kirim balasan dengan kuesioner di Task.contained,
            // atau respon kriteria di Task.output.
            // Coba cari Questionnaire.item di contained.
            modelKriteria.setRowCount(0);
            JsonNode contained = resp.path("contained");
            boolean foundQ = false;
            if (contained.isArray()) {
                for (JsonNode c : contained) {
                    if ("Questionnaire".equals(c.path("resourceType").asText())) {
                        JsonNode items = c.path("item");
                        if (items.isArray()) {
                            for (JsonNode it : items) {
                                modelKriteria.addRow(new Object[]{
                                        it.path("linkId").asText(),
                                        it.path("text").asText(),
                                        it.path("type").asText(),
                                        ""
                                });
                                foundQ = true;
                            }
                        }
                    }
                }
            }

            // Simpan task_pra_id ke DB sebagai jejak audit
            simpanRujukanRanapDraft();

            if (foundQ) {
                JOptionPane.showMessageDialog(this,
                        "Pra Permintaan berhasil. Task ID: " + taskPraId
                        + "\nIsi jawaban kriteria di tabel, lalu pindah ke Tab 2.");
                setStatusTab(lblTab1Status,
                        "OK. Task ID: " + taskPraId + ", " + modelKriteria.getRowCount() + " kriteria.", false);
                setStatus("Pra Permintaan terkirim.", false);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Pra Permintaan terkirim (Task ID: " + taskPraId + "), "
                        + "tapi tidak ada kuesioner di response.\n"
                        + "Anda bisa langsung ke Tab 2 (Cari Kandidat) tanpa isi kriteria.");
                setStatusTab(lblTab1Status,
                        "OK. Task ID: " + taskPraId + " (tanpa kuesioner)", false);
                setStatus("Pra Permintaan terkirim (no kuesioner).", false);
            }
        } catch (Exception ex) {
            handleApiError(ex);
            setStatusTab(lblTab1Status, "Error: " + ex.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  TAB 2 ACTION
    // =================================================================
    private void doCariKandidat() {
        if (!validatePasienTerpilih()) return;
        if (taskPraId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Klik [Kirim Pra Permintaan] di Tab 1 dulu.");
            return;
        }

        // Build QuestionnaireResponse dari modelKriteria
        String qrCriteria = buildQrCriteriaJson();
        String qrArea = buildQrAreaJson();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab2Status, "Mencari kandidat...", false);
        try {
            JsonNode resp = svc.cariKandidatFasyankes(
                    SatuSehatRujukanService.SNOMED_RANAP,
                    selIdPasienSS,
                    selKdPenyakit, selNmPenyakit,
                    "", "",
                    qrCriteria, qrArea,
                    selIdEncounterSS);

            taskCariId = resp.path("id").asText();

            modelKandidat.setRowCount(0);
            JsonNode outputs = resp.path("output");
            if (outputs.isArray()) {
                for (JsonNode out : outputs) {
                    JsonNode typeCoding = out.path("type").path("coding");
                    boolean isCandidate = false;
                    if (typeCoding.isArray()) {
                        for (JsonNode tc : typeCoding) {
                            if ("candidate-referral-facility".equals(tc.path("code").asText())) {
                                isCandidate = true; break;
                            }
                        }
                    }
                    if (!isCandidate) continue;

                    String orgRef = out.path("valueReference").path("reference").asText();
                    String orgId = orgRef.startsWith("Organization/") ? orgRef.substring(13) : orgRef;
                    String orgDisp = out.path("valueReference").path("display").asText();

                    String strata = "";
                    String distance = "";
                    String bpjsCode = "";

                    JsonNode exts = out.path("extension");
                    if (exts.isArray()) {
                        for (JsonNode ex : exts) {
                            String url = ex.path("url").asText();
                            if (url.contains("strata")) {
                                strata = ex.path("valueCode").asText();
                            } else if (url.contains("distance")) {
                                distance = ex.path("valueQuantity").path("value").asText();
                            } else if (url.contains("bpjs-code")) {
                                bpjsCode = ex.path("valueCode").asText();
                            }
                        }
                    }

                    modelKandidat.addRow(new Object[]{
                            Boolean.FALSE, orgId, bpjsCode, orgDisp, strata, distance
                    });
                }
            }

            // Update DB - simpan task_cari_id
            updateTaskCariId();

            setStatusTab(lblTab2Status,
                    "Ditemukan " + modelKandidat.getRowCount() + " kandidat. Centang yang akan dikirim tugas, lalu ke Tab 3.",
                    false);
            setStatus("Kandidat ditemukan: " + modelKandidat.getRowCount(), false);

            if (modelKandidat.getRowCount() > 0) {
                tabPane.setSelectedIndex(2);
            }
        } catch (Exception ex) {
            handleApiError(ex);
            setStatusTab(lblTab2Status, "Error: " + ex.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  TAB 3 ACTION
    // =================================================================
    private void doKirimTugas() {
        if (!validatePasienTerpilih()) return;
        if (selIdConditionSS == null || selIdConditionSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Klik [Buat Condition] dulu di panel atas — diperlukan untuk reasonReference Task.");
            return;
        }

        // Kumpulkan kandidat tercentang
        java.util.List<String[]> centangedData = new java.util.ArrayList<>();
        for (int i = 0; i < modelKandidat.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelKandidat.getValueAt(i, 0))) {
                centangedData.add(new String[]{
                        String.valueOf(modelKandidat.getValueAt(i, 1)),  // ID SS
                        String.valueOf(modelKandidat.getValueAt(i, 3))   // Nama
                });
            }
        }
        if (centangedData.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Centang minimal 1 kandidat di Tab 2 dulu.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Kirim tugas rujukan ke " + centangedData.size() + " fasyankes?\n"
                + "(akan POST CarePlan rujukan dulu, lalu Task ke tiap fasyankes)",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // ==== Step A: POST CarePlan SEKALI (atau reuse kalau sudah ada) ====
        String idCarePlan = cariOrKirimCarePlan(centangedData.get(0)[0]);  // pakai org tujuan pertama sbg contributor
        if (idCarePlan == null || idCarePlan.isEmpty()) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,
                    "Gagal POST CarePlan rujukan. Lihat console log untuk detail.");
            setStatusTab(lblTab3Status, "Gagal POST CarePlan.", true);
            return;
        }

        setStatusTab(lblTab3Status,
                "CarePlan terkirim (id=" + idCarePlan + "). Sekarang kirim Task per kandidat...", false);

        // ==== Step B: Loop kandidat, kirim Task dengan basedOn ke CarePlan ====
        int sukses = 0, gagal = 0;
        for (String[] data : centangedData) {
            String idFaskesTujuan = data[0];
            String namaFaskes = data[1];

            try {
                JsonNode resp = svc.kirimTugasRujukanRefCarePlan(
                        selIdPasienSS, idFaskesTujuan, namaFaskes,
                        selIdConditionSS, idCarePlan,
                        selIdEncounterSS);

                // Cari Task ID di response Bundle (entry pertama)
                String taskKirimId = "";
                JsonNode entries = resp.path("entry");
                if (entries.isArray()) {
                    for (JsonNode en : entries) {
                        String loc = en.path("response").path("location").asText();
                        if (loc.startsWith("Task/")) {
                            int slash = loc.indexOf('/', 5);
                            taskKirimId = (slash > 0) ? loc.substring(5, slash) : loc.substring(5);
                            break;
                        }
                    }
                }

                modelTugas.addRow(new Object[]{
                        taskKirimId, namaFaskes, "requested", "menunggu", new java.util.Date().toString()
                });
                simpanTaskTracking(taskKirimId, "approval-request", idFaskesTujuan, namaFaskes,
                                   "requested", null);
                sukses++;

            } catch (Exception ex) {
                System.out.println("Gagal kirim ke " + namaFaskes + ": " + ex);
                modelTugas.addRow(new Object[]{
                        "-", namaFaskes, "FAILED", "error: " + ex.getMessage(), new java.util.Date().toString()
                });
                gagal++;
            }
        }

        setCursor(Cursor.getDefaultCursor());
        setStatusTab(lblTab3Status,
                "Selesai. Sukses: " + sukses + ", Gagal: " + gagal
                + ". Klik [Cek Status Semua] periodically untuk cek accept/reject.", false);
        setStatus("Tugas rujukan terkirim ke " + sukses + " fasyankes.", false);
        JOptionPane.showMessageDialog(this,
                "Tugas terkirim:\n- CarePlan ID: " + idCarePlan
                + "\n- Sukses: " + sukses + "\n- Gagal: " + gagal
                + "\n\nKlik [Cek Status Semua] secara berkala untuk cek respon fasyankes tujuan.");
    }

    /**
     * Cek apakah CarePlan rujukan sudah ada untuk no_rujukan ini.
     * Kalau ada → return id-nya. Kalau belum → POST baru ke Satu Sehat,
     * simpan ke bridging_careplan_rujukan, return id baru.
     *
     * @param idFaskesTujuanContributor org tujuan pertama (untuk CarePlan.contributor)
     * @return CarePlan.id atau null kalau gagal
     */
    private String cariOrKirimCarePlan(String idFaskesTujuanContributor) {
        // 1. Cek di DB
        String existingId = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select id_careplan from bridging_careplan_rujukan where no_rujukan=?")) {
            ps.setString(1, noRujukanInternal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) existingId = safe(rs.getString(1));
            }
        } catch (Exception ex) {
            System.out.println("Cek bridging_careplan_rujukan err: " + ex);
        }
        if (!existingId.isEmpty()) {
            System.out.println("Re-use CarePlan id: " + existingId);
            return existingId;
        }

        // 2. POST baru ke Satu Sehat
        try {
            String namaDokter = Sequel.cariIsi(
                    "select nama from pegawai where nik=?", selKdDokter);
            String tglDibuat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date());
            String desc = "Rujukan rawat inap untuk pasien " + selNamaPasien
                    + " dengan diagnosa " + selKdPenyakit + " - " + selNmPenyakit;

            JsonNode resp = svc.kirimCarePlanRujukan(
                    selNoRawat,
                    selIdPasienSS, selNamaPasien,
                    selIdEncounterSS,
                    selIdDokterSS, namaDokter,
                    idFaskesTujuanContributor,
                    selIdConditionSS,
                    SatuSehatRujukanService.SNOMED_RANAP,
                    desc, tglDibuat,
                    "", ""  // kdSpesialisasi, kdKelasPerawatan - kosong
            );

            String id = svc.getCarePlanId(resp);
            if (id == null || id.isEmpty()) {
                System.out.println("CarePlan response tidak ada id: " + resp);
                return null;
            }

            // Simpan ke DB
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into bridging_careplan_rujukan "
                    + "(no_rujukan, id_careplan, tipe_rujukan, kdppk_tujuan, "
                    + " id_condition_diagnosa, description) "
                    + "values (?,?,'ranap',?,?,?)")) {
                ps.setString(1, noRujukanInternal);
                ps.setString(2, id);
                ps.setString(3, idFaskesTujuanContributor);
                ps.setString(4, selIdConditionSS);
                ps.setString(5, desc);
                ps.executeUpdate();
            } catch (Exception ex) {
                System.out.println("Simpan bridging_careplan_rujukan err: " + ex);
            }
            return id;
        } catch (Exception ex) {
            System.out.println("Kirim CarePlan err: " + ex);
            ex.printStackTrace();
            return null;
        }
    }

    private void doCekStatusBaris() {
        int row = tblTugas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu.");
            return;
        }
        cekStatusOneRow(row);
        refreshAcceptedFaskesTab4();
    }

    private void doCekStatusSemua() {
        if (modelTugas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada tugas rujukan terkirim.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (int i = 0; i < modelTugas.getRowCount(); i++) {
            cekStatusOneRow(i);
        }
        setCursor(Cursor.getDefaultCursor());
        refreshAcceptedFaskesTab4();
    }

    private void cekStatusOneRow(int row) {
        String taskId = String.valueOf(modelTugas.getValueAt(row, 0));
        if (taskId == null || taskId.isEmpty() || "-".equals(taskId)) return;

        try {
            JsonNode resp = svc.cekStatusAccept(taskId);
            Boolean accept = svc.cekResponAccept(resp);
            String stt = resp.path("status").asText();
            modelTugas.setValueAt(stt, row, 2);

            String responText;
            if (accept == null) responText = "menunggu";
            else if (accept) responText = "ACCEPTED";
            else responText = "REJECTED";
            modelTugas.setValueAt(responText, row, 3);

            // Update DB
            updateTaskTrackingRespon(taskId, stt, accept);
        } catch (Exception ex) {
            modelTugas.setValueAt("error", row, 3);
            System.out.println("cekStatusOneRow err: " + ex);
        }
    }

    private void refreshAcceptedFaskesTab4() {
        modelAcceptedFaskes.setRowCount(0);
        for (int i = 0; i < modelTugas.getRowCount(); i++) {
            if ("ACCEPTED".equals(String.valueOf(modelTugas.getValueAt(i, 3)))) {
                String taskId = String.valueOf(modelTugas.getValueAt(i, 0));
                String namaFaskes = String.valueOf(modelTugas.getValueAt(i, 1));
                String idFaskesSS = Sequel.cariIsi(
                        "select kdppk_kandidat from bridging_task_rujukan_satusehat where task_id=?",
                        taskId);
                modelAcceptedFaskes.addRow(new Object[]{
                        Boolean.FALSE, taskId, idFaskesSS, namaFaskes,
                        new java.util.Date().toString()
                });
            }
        }
        if (modelAcceptedFaskes.getRowCount() > 0) {
            setStatusTab(lblTab3Status,
                    modelAcceptedFaskes.getRowCount() + " fasyankes ACCEPT. Lanjut ke Tab 4.", false);
        }
    }

    // =================================================================
    //  TAB 4 ACTION
    // =================================================================
    private void doKirimFinal() {
        if (!validatePasienTerpilih()) return;

        // Cari faskes tercentang
        int rowChosen = -1;
        for (int i = 0; i < modelAcceptedFaskes.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelAcceptedFaskes.getValueAt(i, 0))) {
                rowChosen = i; break;
            }
        }
        if (rowChosen < 0) {
            JOptionPane.showMessageDialog(this, "Centang 1 fasyankes tujuan FINAL.");
            return;
        }

        String idFaskesTujuan = String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 2));
        String namaFaskes = String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 3));

        int idxTr = cbJenisTransport.getSelectedIndex();
        String kdTransport = TRANSPORT[idxTr][0];

        int conf = JOptionPane.showConfirmDialog(this,
                "Kirim Rujukan FINAL untuk pasien " + selNamaPasien
                + "\nke " + namaFaskes + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab4Status, "Mengirim rujukan final...", false);
        try {
            // Ambil idCarePlan yang sudah dikirim di Tab 3
            String idCarePlan = Sequel.cariIsi(
                    "select id_careplan from bridging_careplan_rujukan where no_rujukan=?",
                    noRujukanInternal);
            if (idCarePlan == null) idCarePlan = "";

            JsonNode resp = svc.kirimRujukanFinal(
                    idFaskesTujuan,
                    idCarePlan,
                    selIdConditionSS,
                    "",  // idConditionKriteriaKlinis
                    SatuSehatRujukanService.SNOMED_RANAP,
                    tNoRujukanPCare.getText().trim(),
                    tNoKartuAsuransi.getText().trim(),
                    "bpjs-kesehatan",
                    "",  // kd lokasi jenis faskes
                    kdTransport,
                    tNoRegAmbulans.getText().trim(),
                    selIdPasienSS,
                    selIdEncounterSS
            );

            String srId = resp.path("id").asText();
            String noRujNas = svc.getNomorRujukanNasional(resp);

            if (noRujNas == null || noRujNas.isEmpty()) {
                noRujNas = srId;  // fallback pakai SR id
            }
            lblNoRujukanNasional.setText(noRujNas);
            simpanRujukanRanapFinal(srId, noRujNas, idFaskesTujuan, namaFaskes);
            setStatusTab(lblTab4Status, "BERHASIL. Nomor Rujukan Nasional: " + noRujNas, false);
            setStatus("Rujukan FINAL terkirim. NoRujukanNasional: " + noRujNas, false);
            JOptionPane.showMessageDialog(this,
                    "Rujukan berhasil dikirim!\n\nNomor Rujukan Nasional:\n" + noRujNas);
        } catch (Exception ex) {
            handleApiError(ex);
            setStatusTab(lblTab4Status, "Error: " + ex.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  HELPER: build QuestionnaireResponse JSON
    // =================================================================
    private String buildQrCriteriaJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"qr-criteria\",")
          .append("\"status\":\"completed\",\"item\":[");
        boolean first = true;
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            String linkId = String.valueOf(modelKriteria.getValueAt(i, 0));
            String type = String.valueOf(modelKriteria.getValueAt(i, 2));
            String jwb = String.valueOf(modelKriteria.getValueAt(i, 3)).trim();
            if (jwb.isEmpty()) continue;

            if (!first) sb.append(",");
            sb.append("{\"linkId\":\"").append(linkId).append("\",\"answer\":[{");
            if ("boolean".equalsIgnoreCase(type)) {
                String bool = normalisasiBooleanKriteria(jwb);
                if (bool == null) bool = "false";
                sb.append("\"valueBoolean\":").append(bool);
            } else {
                sb.append("\"valueString\":\"").append(escape(jwb)).append("\"");
            }
            sb.append("}]}");
            first = false;
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildQrAreaJson() {
        int idx = cbProvinsi.getSelectedIndex();
        String kdProv = PROVINSI[idx][0];
        String nmProv = PROVINSI[idx][1];
        return "{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"qr-area\","
                + "\"status\":\"completed\",\"item\":[{"
                + "\"linkId\":\"propinsi\",\"answer\":[{"
                + "\"valueString\":\"" + kdProv + " - " + escape(nmProv) + "\""
                + "}]}]}";
    }

    // =================================================================
    //  HELPER: validations
    // =================================================================
    private boolean validatePasienTerpilih() {
        if (selNoRawat == null || selNoRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien dari tabel di atas dulu.");
            return false;
        }
        return true;
    }

    private boolean validateIhsLengkap() {
        if (selIdEncounterSS.isEmpty() || selIdPasienSS.isEmpty() || selIdDokterSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Encounter / IHS Pasien / IHS Dokter belum lengkap. Lihat panel Pasien Terpilih.");
            return false;
        }
        if (selKdPenyakit == null || selKdPenyakit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosa ICD-10 belum ada di reg_periksa.");
            return false;
        }
        return true;
    }

    // =================================================================
    //  HELPER: DB persistence
    // =================================================================
    private void simpanRujukanRanapDraft() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into bridging_rujukan_satusehat_ranap "
                + "(no_rujukan, no_rawat, no_rkm_medis, task_pra_id, "
                + " id_pasien_satusehat, id_dokter_satusehat, kode_faskes_satusehat, "
                + " kd_diagnosa_primer, nm_diagnosa_primer, tipe_perawatan, "
                + " step_terakhir, status_rujukan, petugas) "
                + "values (?,?,?,?,?,?,?,?,?,?,1,'pra-sent',?) "
                + "on duplicate key update task_pra_id=values(task_pra_id), "
                + " step_terakhir=values(step_terakhir), status_rujukan=values(status_rujukan)")) {
            ps.setString(1, noRujukanInternal);
            ps.setString(2, selNoRawat);
            ps.setString(3, selNoRm);
            ps.setString(4, taskPraId);
            ps.setString(5, selIdPasienSS);
            ps.setString(6, selIdDokterSS);
            ps.setString(7, ""); // org perujuk dari ApiSatuSehat - opsional
            ps.setString(8, selKdPenyakit);
            ps.setString(9, selNmPenyakit);
            ps.setString(10, SatuSehatRujukanService.SNOMED_RANAP);
            ps.setString(11, user);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("simpanRujukanRanapDraft err: " + ex);
        }
    }

    private void updateTaskCariId() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_satusehat_ranap set task_cari_id=?, "
                + "step_terakhir=2, status_rujukan='kandidat-found' where no_rujukan=?")) {
            ps.setString(1, taskCariId);
            ps.setString(2, noRujukanInternal);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("updateTaskCariId err: " + ex);
        }
    }

    private void simpanTaskTracking(String taskId, String taskType, String kdppkKandidat,
                                     String nmppkKandidat, String taskStatus, Boolean accept) {
        if (taskId == null || taskId.isEmpty() || "-".equals(taskId)) return;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into bridging_task_rujukan_satusehat "
                + "(task_id, no_rujukan, tipe_rujukan, task_type, kdppk_kandidat, "
                + " nmppk_kandidat, task_status, respon_diterima) "
                + "values (?,?,'ranap',?,?,?,?,?) "
                + "on duplicate key update task_status=values(task_status), "
                + " respon_diterima=values(respon_diterima)")) {
            ps.setString(1, taskId);
            ps.setString(2, noRujukanInternal);
            ps.setString(3, taskType);
            ps.setString(4, kdppkKandidat);
            ps.setString(5, nmppkKandidat);
            ps.setString(6, taskStatus);
            if (accept == null) ps.setNull(7, java.sql.Types.TINYINT);
            else ps.setInt(7, accept ? 1 : 0);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("simpanTaskTracking err: " + ex);
        }
    }

    private void updateTaskTrackingRespon(String taskId, String taskStatus, Boolean accept) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_task_rujukan_satusehat set task_status=?, respon_diterima=?, "
                + "tgl_respon=now() where task_id=?")) {
            ps.setString(1, taskStatus);
            if (accept == null) ps.setNull(2, java.sql.Types.TINYINT);
            else ps.setInt(2, accept ? 1 : 0);
            ps.setString(3, taskId);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("updateTaskTrackingRespon err: " + ex);
        }
    }

    private void simpanRujukanRanapFinal(String srId, String noRujNas,
                                          String kdppkTujuan, String nmppkTujuan) {
        // Ambil careplan_id dari bridging_careplan_rujukan
        String idCarePlan = Sequel.cariIsi(
                "select id_careplan from bridging_careplan_rujukan where no_rujukan=?",
                noRujukanInternal);
        if (idCarePlan == null) idCarePlan = "";

        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_satusehat_ranap set "
                + "service_request_id=?, no_rujukan_satusehat=?, careplan_id=?, "
                + "kdppk_tujuan_final=?, nmppk_tujuan_final=?, "
                + "step_terakhir=4, status_rujukan='completed', tgl_dikirim=now() "
                + "where no_rujukan=?")) {
            ps.setString(1, srId);
            ps.setString(2, noRujNas);
            ps.setString(3, idCarePlan);
            ps.setString(4, kdppkTujuan);
            ps.setString(5, nmppkTujuan);
            ps.setString(6, noRujukanInternal);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("simpanRujukanRanapFinal err: " + ex);
        }
    }

    // =================================================================
    //  ERROR HANDLING
    // =================================================================
    private void handleApiError(Exception ex) {
        ex.printStackTrace();
        String msg = ex.toString();
        JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + msg, true);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatRujukanRanap dlg = new SatuSehatRujukanRanap(new javax.swing.JFrame(), true);
            dlg.setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.InternalFrame frameMain;
    private widget.PanelBiasa panelTop;
    private widget.Label lblCari;
    private widget.TextBox tCariPasien;
    private widget.Button btnCari;
    private widget.Button btnRefresh;
    private widget.ScrollPane scrollPasien;
    private widget.Table tblPasien;
    private widget.PanelBiasa panelMid;
    private widget.Label lbltNoRawat;
    private widget.TextBox tNoRawat;
    private widget.Label lbltPasien;
    private widget.TextBox tPasien;
    private widget.Label lbltDiagnosa;
    private widget.TextBox tDiagnosa;
    private widget.Label lbltEncounter;
    private widget.TextBox tEncounter;
    private widget.Label lbltIdPasienIhs;
    private widget.TextBox tIdPasienIhs;
    private widget.Label lbltIdDokterIhs;
    private widget.TextBox tIdDokterIhs;
    private widget.Label lbltIdConditionIhs;
    private widget.TextBox tIdConditionIhs;
    private widget.Button btnBuatCondition;
    private widget.Button btnPilihDiagnosa;
    private javax.swing.JTabbedPane tabPane;
    private widget.PanelBiasa pnlTab1;
    private widget.Button btnKirimPra;
    private widget.ScrollPane scrollKriteria;
    private widget.Table tblKriteria;
    private widget.Label lblTab1Status;
    private widget.PanelBiasa pnlTab2;
    private widget.Label lblProvinsi;
    private widget.ComboBox cbProvinsi;
    private widget.Button btnCariKandidat;
    private widget.ScrollPane scrollKandidat;
    private widget.Table tblKandidat;
    private widget.Label lblTab2Status;
    private widget.PanelBiasa pnlTab3;
    private widget.Button btnKirimTugas;
    private widget.Button btnCekStatusSemua;
    private widget.Button btnCekStatusBaris;
    private widget.ScrollPane scrollTugas;
    private widget.Table tblTugas;
    private widget.Label lblTab3Status;
    private widget.PanelBiasa pnlTab4;
    private widget.ScrollPane scrollAccepted;
    private widget.Table tblAcceptedFaskes;
    private widget.Label lblNoPCare;
    private widget.TextBox tNoRujukanPCare;
    private widget.Label lblNoKartu;
    private widget.TextBox tNoKartuAsuransi;
    private widget.Label lblAmbulans;
    private widget.TextBox tNoRegAmbulans;
    private widget.Label lblTransport;
    private widget.ComboBox cbJenisTransport;
    private widget.Button btnKirimFinal;
    private widget.Label lblNoRujukanNasional;
    private widget.Label lblTab4Status;
    private widget.PanelBiasa panelBottom;
    private widget.Button btnTutup;
    private widget.Label lblStatus;
    // End of variables declaration//GEN-END:variables

}