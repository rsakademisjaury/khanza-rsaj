/*
 * SatuSehatRujukanIGD.java
 *
 * Form mandiri Rujukan IGD / Rawat Darurat - LANGSUNG ke Satu Sehat (TIDAK lewat BPJS-K).
 *
 * Sesuai Buku Panduan SATUSEHAT (Playbook) Rujukan Pasien v5.1, 4 Maret 2026,
 * Bab 2.x Rujukan Rawat Inap dan Rawat Darurat.
 *
 * Layout:
 *   - Atas: tabel daftar pasien IGD aktif (reg_periksa.kd_poli IGD, hari ini & belum pulang)
 *   - Tengah: panel info pasien terpilih + IHS info
 *   - Bawah: TabbedPane 4 tab (Pra → Cari Kandidat → Kirim Tugas → Rujukan Final)
 *
 * Flow:
 *   Tab 1: Kirim Task referral-pre-request → respon kuesioner kriteria
 *   Tab 2: Jawab kriteria + pilih wilayah → Task request-referral-candidate (tipe IGD)
 *   Tab 3: Pilih kandidat → POST Bundle (Task referral-approval-request + CarePlan IGD)
 *   Tab 4: Setelah ada Task ACCEPT → POST ServiceRequest IGD FINAL → Nomor Rujukan Nasional
 *
 * Karakteristik IGD (vs Ranap):
 *   - Tipe perawatan: SNOMED 385868005 (Emergency treatment management)
 *   - CarePlan.category: TK000068 (Emergency care plan, system Kemkes)
 *   - ServiceRequest.code: 385868005
 *   - CarePlan.activity[].detail.kind: ServiceRequest (segera, bukan Appointment)
 *   - Fokus pada kecepatan & kedekatan lokasi (sesuai Gambar 6 playbook hal.30)
 *
 * @author SIMRS Khanza Bridging
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
import java.util.Date;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public final class SatuSehatRujukanIGD extends javax.swing.JDialog {

    // ===== Helper Khanza =====
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    // ===== Service =====
    private final SatuSehatRujukanService svc = new SatuSehatRujukanService();
    private final SatuSehatCekNIK cekIhs = new SatuSehatCekNIK();
    private final ApiSatuSehat ihsEncounter = new ApiSatuSehat();


    // ===== Mode dummy untuk uji SATUSEHAT STG / BPJS DEV =====
    // Ubah ke false saat sudah masuk production agar kembali memakai data pasien asli.
    private static final boolean GUNAKAN_DUMMY_RUJUKAN_IGD = true;
    private static final String DUMMY_DIAGNOSA_KODE = "I61.9";
    private static final String DUMMY_DIAGNOSA_NAMA = "Intracerebral haemorrhage, unspecified";
    private final SatuSehatRujukanDummyData.Data dummyData =
            SatuSehatRujukanDummyData.getDefault();
    private String terakhirIdLokasiEncounterIGD = "";

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
    private String appointmentId = "";
    private String serviceRequestId = "";

    // No.Rujukan internal (PK ke bridging_rujukan_satusehat_igd)
    private String noRujukanInternal = "";
    private String user = "";

    // Guard agar proses pilih pasien tidak terpanggil berulang saat selection event JTable berubah.
    private boolean sedangProsesPilihPasien = false;
    private boolean sedangReloadPasien = false;

    // Guard agar ceklis kandidat hanya boleh satu baris dan tidak memicu event berulang.
    private boolean sedangSetCeklisKandidat = false;

    // ===== Riwayat response transaksi, ditampilkan seperti Postman =====
    private final java.util.List<ResponTransaksi> riwayatResponTransaksi = new java.util.ArrayList<>();
    private int urutResponTransaksi = 0;

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

    // Kabupaten/Kota preset untuk QuestionnaireResponse Q101.
    // Minimal disiapkan sesuai kebutuhan uji STG; bisa ditambah tanpa mengubah alur.
    private static final String[][] KABKOTA = {
        {"31", "3174", "Kota Jakarta Selatan"},
        {"32", "3273", "Kota Bandung"},
        {"33", "3374", "Kota Semarang"},
        {"34", "3471", "Kota Yogyakarta"},
        {"35", "3578", "Kota Surabaya"},
        {"36", "3671", "Kota Tangerang"},
        {"73", "7371", "Kota Makassar"}
    };

    // Komponen tambahan di Tab 2 dibuat runtime agar source lain dan .form lama tetap aman.
    private widget.Label lblKabupaten;
    private widget.ComboBox cbKabupaten;

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

    public SatuSehatRujukanIGD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        modelPasien = new DefaultTableModel(null, new Object[]{
                "No.Rawat", "No.RM", "Nama Pasien", "Poli", "Diagnosa",
                "Nama Diagnosa", "DPJP", "Tgl Reg", "Encounter", "Condition"
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
                "Task ID", "ServiceRequest ID", "Faskes Tujuan", "Status", "Respon", "Tgl Kirim"
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
        setupFilterTanggalDanActionPasien();
        setSize(1180, 820);
        setLocationRelativeTo(parent);

        try {
            user = akses.getkode().replace(" ", "").substring(0, 9);
        } catch (Exception e) {
            user = akses.getkode();
        }

        if (GUNAKAN_DUMMY_RUJUKAN_IGD) {
            setStatus("MODE DUMMY STG aktif. Data yang dikirim memakai pasien/dokter dummy.", false);
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

        // Jika form dibuka dari DlgIGD untuk no_rawat lama, sesuaikan filter tanggal
        // dengan tanggal registrasi pasien tersebut supaya barisnya tidak hilang karena
        // default filter hanya 30 hari terakhir.
        setPeriodeSesuaiNoRawat(targetNoRawat);

        tCariPasien.setText(targetNoRawat);
        tampilDataPasien();

        boolean ditemukan = false;
        for (int i = 0; i < modelPasien.getRowCount(); i++) {
            if (targetNoRawat.equals(String.valueOf(modelPasien.getValueAt(i, 0)))) {
                pilihBarisPasienByModelIndex(i);
                prosesPilihPasienDariTabel();
                setStatus("Pasien terpilih otomatis: " + targetNoRawat, false);
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            setStatus("No.Rawat " + targetNoRawat + " tidak ditemukan pada periode tanggal yang dipilih. Silakan ubah filter tanggal atau pilih manual.", true);
        }
    }

    private void setPeriodeSesuaiNoRawat(String noRawat) {
        try {
            String tglReg = Sequel.cariIsi("select tgl_registrasi from reg_periksa where no_rawat=?", noRawat);
            if (tglReg != null && !tglReg.trim().equals("")) {
                java.util.Date tgl = java.sql.Date.valueOf(tglReg.trim());
                DTPCari1.setDate(tgl);
                DTPCari2.setDate(tgl);
            }
        } catch (Exception e) {
            System.out.println("Set periode sesuai no_rawat gagal: " + e.getMessage());
        }
    }

    private void pilihBarisPasienByModelIndex(int modelIndex) {
        try {
            if (tblPasien.getModel() != modelPasien) {
                tblPasien.setModel(modelPasien);
            }
            int viewIndex = tblPasien.convertRowIndexToView(modelIndex);
            if (viewIndex >= 0 && viewIndex < tblPasien.getRowCount()) {
                tblPasien.setRowSelectionInterval(viewIndex, viewIndex);
                tblPasien.scrollRectToVisible(tblPasien.getCellRect(viewIndex, 0, true));
            }
        } catch (Exception e) {
            System.out.println("Pilih baris pasien gagal: " + e.getMessage());
        }
    }


    // =================================================================
    //  initComponents - manual layout (tanpa .form)
    // =================================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameMain = new widget.InternalFrame();
        panelTop = new widget.PanelBiasa();
        lblTgl1 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        lblTgl2 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        lblCari = new widget.Label();
        tCariPasien = new widget.TextBox();
        btnCari = new widget.Button();
        btnRefresh = new widget.Button();
        btnRujukanMasuk = new widget.Button();
        scrollPasien = new widget.ScrollPane();
        tblPasien = new widget.Table();
        tabPane = new javax.swing.JTabbedPane();
        pnlTab1 = new widget.PanelBiasa();
        btnKirimPra = new widget.Button();
        scrollKriteria = new widget.ScrollPane();
        tblKriteria = new widget.Table();
        lblTab1Status = new widget.Label();
        pnlTab2 = new widget.PanelBiasa();
        lblProvinsi = new widget.Label();
        cbProvinsi = new widget.ComboBox();
        btnCariKandidat = new widget.Button();
        scrollKandidat = new widget.ScrollPane();
        tblKandidat = new widget.Table();
        lblTab2Status = new widget.Label();
        pnlTab3 = new widget.PanelBiasa();
        btnKirimTugas = new widget.Button();
        btnKirimTugasTeman = new widget.Button();
        btnCekStatusSemua = new widget.Button();
        btnCekStatusBaris = new widget.Button();
        scrollTugas = new widget.ScrollPane();
        tblTugas = new widget.Table();
        lblTab3Status = new widget.Label();
        pnlTab4 = new widget.PanelBiasa();
        scrollAccepted = new widget.ScrollPane();
        tblAcceptedFaskes = new widget.Table();
        lblNoPCare = new widget.Label();
        tNoRujukanPCare = new widget.TextBox();
        lblNoKartu = new widget.Label();
        tNoKartuAsuransi = new widget.TextBox();
        lblAmbulans = new widget.Label();
        tNoRegAmbulans = new widget.TextBox();
        lblAppointmentMulai = new widget.Label();
        tAppointmentMulai = new widget.TextBox();
        lblAppointmentSelesai = new widget.Label();
        tAppointmentSelesai = new widget.TextBox();
        btnKirimAppointment = new widget.Button();
        lblTransport = new widget.Label();
        cbJenisTransport = new widget.ComboBox();
        btnKirimFinal = new widget.Button();
        lblNoRujukanNasional = new widget.Label();
        lblTab4Status = new widget.Label();
        panelMid = new widget.PanelBiasa();
        lbltNoRawat = new widget.Label();
        tNoRawat = new widget.TextBox();
        lbltPasien = new widget.Label();
        tPasien = new widget.TextBox();
        lbltDiagnosa = new widget.Label();
        tDiagnosa = new widget.TextBox();
        lbltEncounter = new widget.Label();
        tEncounter = new widget.TextBox();
        btnBuatEncounter = new widget.Button();
        lbltIdPasienIhs = new widget.Label();
        tIdPasienIhs = new widget.TextBox();
        lbltIdDokterIhs = new widget.Label();
        tIdDokterIhs = new widget.TextBox();
        lbltIdConditionIhs = new widget.Label();
        tIdConditionIhs = new widget.TextBox();
        btnBuatCondition = new widget.Button();
        btnPilihDiagnosa = new widget.Button();
        panelBottom = new widget.PanelBiasa();
        btnTampilRespon = new widget.Button();
        btnTutup = new widget.Button();
        lblStatus = new widget.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rujukan IGD / Rawat Darurat - Satu Sehat [v75 OWNER-TARGET INTEROP]");
        setUndecorated(true);
        setResizable(false);

        frameMain.setLayout(new java.awt.BorderLayout(10, 10));

        panelTop.setPreferredSize(new java.awt.Dimension(1180, 250));
        panelTop.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTgl1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTgl1.setText("Tanggal");
        panelTop.add(lblTgl1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 55, 26));

        DTPCari1.setDisplayFormat("dd/MM/yyyy");
        panelTop.add(DTPCari1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, 110, 26));

        lblTgl2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTgl2.setText("s.d.");
        panelTop.add(lblTgl2, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 10, 35, 26));

        DTPCari2.setDisplayFormat("dd/MM/yyyy");
        panelTop.add(DTPCari2, new org.netbeans.lib.awtextra.AbsoluteConstraints(235, 10, 110, 26));

        lblCari.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCari.setText("Cari No. RM / Nama");
        panelTop.add(lblCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 10, 110, 26));
        panelTop.add(tCariPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(475, 10, 240, 26));

        btnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Select.png"))); // NOI18N
        btnCari.setText("Cari");
        panelTop.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(725, 10, 65, 26));

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/rotate.png"))); // NOI18N
        btnRefresh.setText("Refresh");
        panelTop.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, 90, 26));

        btnRujukanMasuk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnRujukanMasuk.setText("Rujukan Masuk");
        btnRujukanMasuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRujukanMasukActionPerformed(evt);
            }
        });
        panelTop.add(btnRujukanMasuk, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 10, 130, 26));

        scrollPasien.setViewportView(tblPasien);

        panelTop.add(scrollPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1620, 180));

        frameMain.add(panelTop, java.awt.BorderLayout.NORTH);

        pnlTab1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnKirimPra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirimPra.setText("Kirim Pra Permintaan");
        pnlTab1.add(btnKirimPra, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 150, 30));

        scrollKriteria.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "     Kriteria     ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(102, 102, 102))); // NOI18N
        scrollKriteria.setViewportView(tblKriteria);

        pnlTab1.add(scrollKriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1090, 240));

        lblTab1Status.setForeground(new java.awt.Color(102, 102, 102));
        lblTab1Status.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTab1Status.setText("Status Tab 1");
        lblTab1Status.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        pnlTab1.add(lblTab1Status, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 315, 1070, 30));

        tabPane.addTab("     1. Pra Permintaan     ", pnlTab1);

        pnlTab2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblProvinsi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProvinsi.setText("Provinsi");
        pnlTab2.add(lblProvinsi, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 60, 30));
        pnlTab2.add(cbProvinsi, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 20, 230, 30));

        btnCariKandidat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnCariKandidat.setText("Cari Kandidat");
        btnCariKandidat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pnlTab2.add(btnCariKandidat, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 20, 120, 30));

        scrollKandidat.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "     Kandidat Faskes     ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(102, 102, 102))); // NOI18N
        scrollKandidat.setViewportView(tblKandidat);

        pnlTab2.add(scrollKandidat, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1090, 240));

        lblTab2Status.setForeground(new java.awt.Color(102, 102, 102));
        lblTab2Status.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTab2Status.setText("Status Tab 2");
        pnlTab2.add(lblTab2Status, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 315, 1070, 30));

        tabPane.addTab("     2. Cari Kandidat     ", pnlTab2);

        pnlTab3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnKirimTugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirimTugas.setText("Kirim Tugas");
        pnlTab3.add(btnKirimTugas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 110, 30));

        btnKirimTugasTeman.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirimTugasTeman.setText("Kirim Kompatibel RS Lain");
        btnKirimTugasTeman.setToolTipText("Mode kompatibel: kirim Task code referral-approval agar dapat dibaca SIMRS RS lain yang belum membaca referral-approval-request");
        pnlTab3.add(btnKirimTugasTeman, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 150, 30));

        btnCekStatusSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnCekStatusSemua.setText("Cek Semua");
        pnlTab3.add(btnCekStatusSemua, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 110, 30));

        btnCekStatusBaris.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnCekStatusBaris.setText("Cek Baris");
        pnlTab3.add(btnCekStatusBaris, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, 100, 30));

        scrollTugas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "     Tugas Rujukan     ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(102, 102, 102))); // NOI18N
        scrollTugas.setViewportView(tblTugas);

        pnlTab3.add(scrollTugas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1090, 240));

        lblTab3Status.setForeground(new java.awt.Color(102, 102, 102));
        lblTab3Status.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTab3Status.setText("Status Tab 3");
        pnlTab3.add(lblTab3Status, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 315, 1070, 30));

        tabPane.addTab("     3. Kirim Tugas     ", pnlTab3);

        pnlTab4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        scrollAccepted.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "   Faskes Accepted   ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(51, 51, 51))); // NOI18N
        scrollAccepted.setViewportView(tblAcceptedFaskes);

        pnlTab4.add(scrollAccepted, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1090, 170));

        lblNoPCare.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoPCare.setText("No. Rujukan PCare");
        pnlTab4.add(lblNoPCare, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 110, 30));
        pnlTab4.add(tNoRujukanPCare, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 180, 30));

        lblNoKartu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoKartu.setText("No. Kartu");
        pnlTab4.add(lblNoKartu, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 20, 60, 30));
        pnlTab4.add(tNoKartuAsuransi, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 180, 30));

        lblAmbulans.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAmbulans.setText("No. Reg Ambulans");
        pnlTab4.add(lblAmbulans, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 20, 110, 30));
        pnlTab4.add(tNoRegAmbulans, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 20, 180, 30));

        lblAppointmentMulai.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAppointmentMulai.setText("Appointment Mulai");
        pnlTab4.add(lblAppointmentMulai, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 110, 30));
        pnlTab4.add(tAppointmentMulai, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 240, 180, 30));

        lblAppointmentSelesai.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAppointmentSelesai.setText("Appointment Selesai");
        pnlTab4.add(lblAppointmentSelesai, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 240, 115, 30));
        pnlTab4.add(tAppointmentSelesai, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 240, 180, 30));

        btnKirimAppointment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirimAppointment.setText("Kirim Appointment");
        pnlTab4.add(btnKirimAppointment, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 240, 150, 30));

        lblTransport.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTransport.setText("Transport");
        pnlTab4.add(lblTransport, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 70, 30));
        pnlTab4.add(cbJenisTransport, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 280, 220, 30));

        btnKirimFinal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirimFinal.setText("Kirim Final");
        pnlTab4.add(btnKirimFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 280, 100, 30));

        lblNoRujukanNasional.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoRujukanNasional.setText("No. Rujukan Nasional : -");
        lblNoRujukanNasional.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        pnlTab4.add(lblNoRujukanNasional, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 280, 590, 30));

        lblTab4Status.setForeground(new java.awt.Color(102, 102, 102));
        lblTab4Status.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTab4Status.setText("Status Tab 4");
        pnlTab4.add(lblTab4Status, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 315, 1070, 30));

        tabPane.addTab("     4. Rujukan Final     ", pnlTab4);

        frameMain.add(tabPane, java.awt.BorderLayout.LINE_END);

        panelMid.setPreferredSize(new java.awt.Dimension(425, 105));
        panelMid.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbltNoRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltNoRawat.setText("No. Rawat");
        panelMid.add(lbltNoRawat, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 80, 26));
        panelMid.add(tNoRawat, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 140, 26));

        lbltPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltPasien.setText("Pasien");
        panelMid.add(lbltPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 90, 26));
        panelMid.add(tPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, 240, 26));

        lbltDiagnosa.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltDiagnosa.setText("Diagnosa");
        panelMid.add(lbltDiagnosa, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 80, 26));
        panelMid.add(tDiagnosa, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, 240, 26));

        lbltEncounter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltEncounter.setText("Encounter");
        panelMid.add(lbltEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 80, 26));
        panelMid.add(tEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 240, 26));

        btnBuatEncounter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus.png"))); // NOI18N
        btnBuatEncounter.setToolTipText("Buat Encounter IGD");
        panelMid.add(btnBuatEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 110, 30, 26));

        lbltIdPasienIhs.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltIdPasienIhs.setText("IHS Pasien");
        panelMid.add(lbltIdPasienIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 100, 26));
        panelMid.add(tIdPasienIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 140, 140, 26));

        lbltIdDokterIhs.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltIdDokterIhs.setText("IHS Dokter");
        panelMid.add(lbltIdDokterIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 100, 26));
        panelMid.add(tIdDokterIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 140, 26));

        lbltIdConditionIhs.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbltIdConditionIhs.setText("Condition");
        panelMid.add(lbltIdConditionIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 100, 26));
        panelMid.add(tIdConditionIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 200, 240, 26));

        btnBuatCondition.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus.png"))); // NOI18N
        btnBuatCondition.setToolTipText("Buat Conditions");
        panelMid.add(btnBuatCondition, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 200, 30, 26));

        btnPilihDiagnosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPilihDiagnosa.setToolTipText("Pilih Diagnosa");
        panelMid.add(btnPilihDiagnosa, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 80, 30, 25));

        frameMain.add(panelMid, java.awt.BorderLayout.CENTER);

        panelBottom.setPreferredSize(new java.awt.Dimension(1180, 55));
        panelBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        btnTampilRespon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question1.png"))); // NOI18N
        btnTampilRespon.setToolTipText("Tampilkan respon transaksi BPJS/SATUSEHAT");
        btnTampilRespon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTampilRespon.setPreferredSize(new java.awt.Dimension(30, 30));
        panelBottom.add(btnTampilRespon);

        btnTutup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        btnTutup.setText("Tutup");
        btnTutup.setPreferredSize(new java.awt.Dimension(90, 30));
        panelBottom.add(btnTutup);

        lblStatus.setForeground(new java.awt.Color(102, 102, 102));
        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblStatus.setText("Status: siap");
        lblStatus.setPreferredSize(new java.awt.Dimension(1000, 30));
        panelBottom.add(lblStatus);

        frameMain.add(panelBottom, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(frameMain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRujukanMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRujukanMasukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRujukanMasukActionPerformed

    /**
     * Setup tambahan untuk filter tanggal, combo, tombol cari/refresh, dan event pilih pasien.
     * Diletakkan di luar initComponents agar desain .form tetap aman diedit via NetBeans.
     */
    private void setupFilterTanggalDanActionPasien() {
        try {
            Calendar cal = Calendar.getInstance();
            DTPCari2.setDate(new Date());
            cal.add(Calendar.DATE, -30);
            DTPCari1.setDate(cal.getTime());
        } catch (Exception e) {
            System.out.println("Setup tanggal cari IGD gagal: " + e);
        }

        setupComboDefaultIGD();
        setupKabupatenKotaPostmanIGD();
        setDefaultAppointmentTimes();

        tblPasien.setModel(modelPasien);
        tblPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        try {
            int[] wp = {130, 80, 220, 160, 80, 220, 220, 140, 260, 260};
            for (int i = 0; i < wp.length && i < tblPasien.getColumnModel().getColumnCount(); i++) {
                tblPasien.getColumnModel().getColumn(i).setPreferredWidth(wp[i]);
            }
            tblPasien.setDefaultRenderer(Object.class, new WarnaTable());
        } catch (Exception e) {
            System.out.println("Setup tabel pasien IGD gagal: " + e.getMessage());
        }

        tblPasien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnCari.addActionListener(e -> aksiCariPasienIGD());
        btnRefresh.addActionListener(e -> {
            tCariPasien.setText("");
            aksiCariPasienIGD();
        });
        btnRujukanMasuk.addActionListener(e -> bukaFormRujukanMasuk());
        tCariPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    aksiCariPasienIGD();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    tCariPasien.setText("");
                    aksiCariPasienIGD();
                }
            }
        });

        tblPasien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !sedangReloadPasien && tblPasien.getSelectedRow() >= 0) {
                prosesPilihPasienDariTabel();
            }
        });

        tblPasien.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!sedangReloadPasien && tblPasien.getSelectedRow() >= 0) {
                    prosesPilihPasienDariTabel();
                }
            }
        });
        tblPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!sedangReloadPasien
                        && (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE)
                        && tblPasien.getSelectedRow() >= 0) {
                    prosesPilihPasienDariTabel();
                }
            }
        });

        btnPilihDiagnosa.addActionListener(e -> doPilihDiagnosa());
        btnBuatEncounter.addActionListener(e -> doBuatEncounterIGD());
        btnBuatCondition.addActionListener(e -> doBuatCondition());
        btnKirimPra.addActionListener(e -> doKirimPra());
        btnCariKandidat.addActionListener(e -> doCariKandidat());
        setupKandidatSingleSelection();
        btnKirimTugas.addActionListener(e -> doKirimTugas());
        btnKirimTugasTeman.addActionListener(e -> doKirimTugasKompatibelTeman());
        // v73: mode kompatibel RS lain ditampilkan kembali.
        // Mode resmi tetap tombol [Kirim Tugas], mode kompatibel memakai Task.code referral-approval.
        btnKirimTugasTeman.setVisible(true);
        btnKirimTugasTeman.setEnabled(true);
        btnKirimTugasTeman.setText("Kirim Kompatibel RS Lain");
        btnKirimTugasTeman.setToolTipText("Mode kompatibel: Task.code referral-approval untuk RS/vendor yang belum membaca referral-approval-request");
        btnCekStatusSemua.addActionListener(e -> doCekStatusSemua());
        btnCekStatusBaris.addActionListener(e -> doCekStatusBaris());
        btnKirimAppointment.addActionListener(e -> doKirimAppointment());
        btnKirimFinal.addActionListener(e -> doKirimFinal());
        btnTampilRespon.addActionListener(e -> tampilDialogResponTransaksi());
        btnTutup.addActionListener(e -> dispose());
    }


    /**
     * Revisi v66: kandidat hanya boleh satu yang dicentang.
     * Setelah user memilih/men-ceklis satu kandidat, form otomatis pindah ke Tab 3
     * agar user langsung bisa klik [Kirim Tugas].
     */
    private void setupKandidatSingleSelection() {
        try {
            modelKandidat.addTableModelListener(e -> {
                if (sedangSetCeklisKandidat) {
                    return;
                }
                if (e.getType() != javax.swing.event.TableModelEvent.UPDATE || e.getColumn() != 0) {
                    return;
                }

                int row = e.getFirstRow();
                if (row < 0 || row >= modelKandidat.getRowCount()) {
                    return;
                }

                Object nilai = modelKandidat.getValueAt(row, 0);
                if (!Boolean.TRUE.equals(nilai)) {
                    return;
                }

                sedangSetCeklisKandidat = true;
                try {
                    for (int i = 0; i < modelKandidat.getRowCount(); i++) {
                        if (i != row && Boolean.TRUE.equals(modelKandidat.getValueAt(i, 0))) {
                            modelKandidat.setValueAt(Boolean.FALSE, i, 0);
                        }
                    }

                    try {
                        int viewRow = tblKandidat.convertRowIndexToView(row);
                        if (viewRow >= 0) {
                            tblKandidat.setRowSelectionInterval(viewRow, viewRow);
                            tblKandidat.scrollRectToVisible(tblKandidat.getCellRect(viewRow, 0, true));
                        }
                    } catch (Exception ignore) {}

                    String namaFaskes = String.valueOf(modelKandidat.getValueAt(row, 3));
                    setStatusTab(lblTab2Status,
                            "Kandidat dipilih: " + namaFaskes + ". Otomatis pindah ke Tab 3 untuk kirim tugas.",
                            false);
                    setStatus("Kandidat dipilih: " + namaFaskes + ". Lanjut klik [Kirim Tugas].", false);

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        try {
                            tabPane.setSelectedIndex(2);
                        } catch (Exception ignore) {}
                    });
                } finally {
                    sedangSetCeklisKandidat = false;
                }
            });
        } catch (Exception e) {
            System.out.println("Setup single selection kandidat gagal: " + e.getMessage());
        }
    }

    private void bukaFormRujukanMasuk() {
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            java.awt.Frame parent = (w instanceof java.awt.Frame) ? (java.awt.Frame) w : null;
            SatuSehatRujukanMasuk dlg = new SatuSehatRujukanMasuk(parent, false);

            java.awt.Rectangle area;
            try {
                java.awt.GraphicsConfiguration gc = getGraphicsConfiguration();
                area = gc != null ? gc.getBounds() : java.awt.GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getMaximumWindowBounds();
                java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
                area = new java.awt.Rectangle(
                        area.x + insets.left,
                        area.y + insets.top,
                        area.width - insets.left - insets.right,
                        area.height - insets.top - insets.bottom);
            } catch (Exception e) {
                area = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            }
            dlg.setBounds(area);
            dlg.setSize(frameMain.getWidth()-20,frameMain.getHeight()-20);
            dlg.setLocationRelativeTo(frameMain);
            dlg.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Gagal membuka Form Rujukan Masuk SATUSEHAT:\n" + ex.getMessage(),
                    "Rujukan Masuk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupComboDefaultIGD() {
        try {
            cbProvinsi.removeAllItems();
            for (String[] p : PROVINSI) {
                cbProvinsi.addItem(p[0] + " - " + p[1]);
            }
            String defaultProv = GUNAKAN_DUMMY_RUJUKAN_IGD ? "31" : "73";
            for (int i = 0; i < PROVINSI.length; i++) {
                if (defaultProv.equals(PROVINSI[i][0])) {
                    cbProvinsi.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Setup combo provinsi IGD gagal: " + e.getMessage());
        }

        try {
            cbJenisTransport.removeAllItems();
            for (String[] t : TRANSPORT) {
                cbJenisTransport.addItem(t[1]);
            }
            if (cbJenisTransport.getItemCount() > 0) {
                cbJenisTransport.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.out.println("Setup combo transport IGD gagal: " + e.getMessage());
        }
    }

    /**
     * Tambahan Tab 2 agar JSON Candidate sesuai Postman IGD:
     * QuestionnaireResponse Q101 harus membawa Provinsi dan Kabupaten/Kota
     * sebagai valueCoding, bukan valueString.
     */
    private void setupKabupatenKotaPostmanIGD() {
        try {
            if (lblKabupaten == null) {
                lblKabupaten = new widget.Label();
                lblKabupaten.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                lblKabupaten.setText("Kab/Kota");
                pnlTab2.add(lblKabupaten, new org.netbeans.lib.awtextra.AbsoluteConstraints(455, 20, 70, 30));
            }
            if (cbKabupaten == null) {
                cbKabupaten = new widget.ComboBox();
                pnlTab2.add(cbKabupaten, new org.netbeans.lib.awtextra.AbsoluteConstraints(525, 20, 230, 30));
            }

            // Rapikan ulang posisi tombol agar tidak bertabrakan dengan Kab/Kota.
            try {
                btnCariKandidat.setBounds(765, 20, 130, 30);
            } catch (Exception ignore) {}

            cbProvinsi.addActionListener(e -> refreshKabupatenKota());
            refreshKabupatenKota();
        } catch (Exception e) {
            System.out.println("Setup Kabupaten/Kota IGD gagal: " + e.getMessage());
        }
    }

    private void refreshKabupatenKota() {
        if (cbKabupaten == null || cbProvinsi == null) {
            return;
        }
        String kdProv = getKodeProvinsiTerpilih();
        cbKabupaten.removeAllItems();
        for (String[] k : KABKOTA) {
            if (k[0].equals(kdProv)) {
                cbKabupaten.addItem(k[1] + " - " + k[2]);
            }
        }
        if (cbKabupaten.getItemCount() == 0) {
            for (String[] k : KABKOTA) {
                cbKabupaten.addItem(k[1] + " - " + k[2]);
            }
        }
        if (cbKabupaten.getItemCount() > 0) {
            cbKabupaten.setSelectedIndex(0);
        }
    }

    private String getKodeProvinsiTerpilih() {
        try {
            int idx = cbProvinsi.getSelectedIndex();
            if (idx >= 0 && idx < PROVINSI.length) {
                return PROVINSI[idx][0];
            }
            String v = String.valueOf(cbProvinsi.getSelectedItem());
            if (v.length() >= 2) return v.substring(0, 2);
        } catch (Exception ignore) {}
        return "31";
    }

    private String getNamaProvinsiTerpilih() {
        String kd = getKodeProvinsiTerpilih();
        for (String[] p : PROVINSI) {
            if (p[0].equals(kd)) return p[1];
        }
        return "DKI Jakarta";
    }

    private String getKodeKabupatenTerpilih() {
        try {
            if (cbKabupaten != null && cbKabupaten.getSelectedItem() != null) {
                String v = String.valueOf(cbKabupaten.getSelectedItem()).trim();
                int p = v.indexOf(" - ");
                return p > 0 ? v.substring(0, p) : v;
            }
        } catch (Exception ignore) {}
        String kdProv = getKodeProvinsiTerpilih();
        for (String[] k : KABKOTA) {
            if (k[0].equals(kdProv)) return k[1];
        }
        return "3174";
    }

    private String getNamaKabupatenTerpilih() {
        String kd = getKodeKabupatenTerpilih();
        for (String[] k : KABKOTA) {
            if (k[1].equals(kd)) return k[2];
        }
        return "Kota Jakarta Selatan";
    }

    private void aksiCariPasienIGD() {
        try {
            tblPasien.clearSelection();
        } catch (Exception ignore) {}
        tampilDataPasien();
    }

    private String getTanggalCari(widget.Tanggal dtp, String fallback) {
        try {
            String tgl = Valid.SetTgl(dtp.getSelectedItem() + "");
            if (tgl != null && tgl.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return tgl;
            }
        } catch (Exception ignore) {}
        return fallback;
    }

    private void kosongkanPanelPasienTerpilih() {
        try {
            selNoRawat = "";
            selNoRm = "";
            selNamaPasien = "";
            selKdPenyakit = "";
            selNmPenyakit = "";
            selKdDokter = "";
            selNoKtpPasien = "";
            selNoKtpDokter = "";
            selIdPasienSS = "";
            selIdDokterSS = "";
            selIdEncounterSS = "";
            selIdConditionSS = "";
            tNoRawat.setText("");
            tPasien.setText("");
            tDiagnosa.setText("");
            tEncounter.setText("");
            tIdPasienIhs.setText("");
            tIdDokterIhs.setText("");
            tIdConditionIhs.setText("");
            panelMid.revalidate();
            panelMid.repaint();
        } catch (Exception ignore) {}
    }

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
        frame.setBorder(BorderFactory.createTitledBorder("Kandidat Fasyankes (pilih/centang 1 kandidat)"));
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

        btnKirimTugasTeman = mkBtn("Kirim Kompatibel RS Lain", "/picture/save-16x16.png");
        btnKirimTugasTeman.setToolTipText("Mode kompatibel: Task.code referral-approval untuk RS/vendor yang belum membaca referral-approval-request");
        btnKirimTugasTeman.setPreferredSize(new Dimension(170, 28));
        btnKirimTugasTeman.addActionListener(e -> doKirimTugasKompatibelTeman());
        pnlTop.add(btnKirimTugasTeman);

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
        int[] wt = {220, 220, 300, 110, 180, 160};
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
        System.out.println("[RujukanIGD] " + msg);
    }

    private void setStatusTab(widget.Label lbl, String msg, boolean error) {
        lbl.setText(msg);
        lbl.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
    }

    // =================================================================
    //  ACTIONS - DAFTAR PASIEN
    // =================================================================

    private void tampilDataPasien() {
        if (tblPasien.getModel() != modelPasien) {
            tblPasien.setModel(modelPasien);
        }

        String keyText = tCariPasien.getText() == null ? "" : tCariPasien.getText().trim();
        String key = "%" + keyText + "%";
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String tgl1 = getTanggalCari(DTPCari1, today);
        String tgl2 = getTanggalCari(DTPCari2, today);

        if (tgl1.compareTo(tgl2) > 0) {
            String tmp = tgl1;
            tgl1 = tgl2;
            tgl2 = tmp;
        }

        sedangReloadPasien = true;
        try {
            tblPasien.clearSelection();
            modelPasien.setRowCount(0);
            kosongkanPanelPasienTerpilih();

            String sql = "select rp.no_rawat, p.no_rkm_medis, p.nm_pasien, "
                    + "concat(rp.kd_poli,' - ',pol.nm_poli) as poli, "
                    + "ifnull(dp.kd_penyakit,'') as kd_penyakit, "
                    + "ifnull(py.nm_penyakit,'') as nm_penyakit, "
                    + "concat(ifnull(pg.nik,rp.kd_dokter),' - ',ifnull(pg.nama,d.nm_dokter)) as dpjp, "
                    + "concat(rp.tgl_registrasi,' ',rp.jam_reg) as tgl_reg, "
                    + "ifnull(se.id_encounter,'') as id_encounter, "
                    + "ifnull(sc.id_condition,'') as id_condition "
                    + "from reg_periksa rp "
                    + "inner join pasien p on rp.no_rkm_medis = p.no_rkm_medis "
                    + "inner join poliklinik pol on rp.kd_poli = pol.kd_poli "
                    + "left join diagnosa_pasien dp on dp.no_rawat = rp.no_rawat and dp.prioritas = '1' "
                    + "left join penyakit py on dp.kd_penyakit = py.kd_penyakit "
                    + "left join dokter d on rp.kd_dokter = d.kd_dokter "
                    + "left join pegawai pg on d.kd_dokter = pg.nik "
                    + "left join (select no_rawat, max(id_encounter) as id_encounter "
                    + "           from satu_sehat_encounter group by no_rawat) se on se.no_rawat = rp.no_rawat "
                    + "left join (select no_rawat, kd_penyakit, max(id_condition) as id_condition "
                    + "           from satu_sehat_condition group by no_rawat, kd_penyakit) sc "
                    + "           on sc.no_rawat = rp.no_rawat and sc.kd_penyakit = dp.kd_penyakit "
                    + "where (pol.nm_poli like '%IGD%' "
                    + "    or pol.nm_poli like '%Gawat Darurat%' "
                    + "    or rp.kd_poli in ('IGDK','IGD','U0001')) "
                    + "and rp.status_lanjut = 'Ralan' "
                    + "and rp.tgl_registrasi between ? and ? "
                    + "and (p.no_rkm_medis like ? or p.nm_pasien like ? or rp.no_rawat like ? "
                    + "     or ifnull(dp.kd_penyakit,'') like ? or ifnull(py.nm_penyakit,'') like ? "
                    + "     or ifnull(se.id_encounter,'') like ? or ifnull(sc.id_condition,'') like ?) "
                    + "order by rp.tgl_registrasi desc, rp.jam_reg desc limit 500";

            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, tgl1);
                ps.setString(2, tgl2);
                ps.setString(3, key);
                ps.setString(4, key);
                ps.setString(5, key);
                ps.setString(6, key);
                ps.setString(7, key);
                ps.setString(8, key);
                ps.setString(9, key);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modelPasien.addRow(new Object[]{
                                safe(rs.getString(1)), safe(rs.getString(2)), safe(rs.getString(3)),
                                safe(rs.getString(4)), safe(rs.getString(5)), safe(rs.getString(6)),
                                safe(rs.getString(7)), safe(rs.getString(8)),
                                safe(rs.getString(9)), safe(rs.getString(10))
                        });
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load pasien IGD: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sedangReloadPasien = false;
        }

        setStatus("Total pasien IGD periode " + tgl1 + " s.d. " + tgl2
                + (keyText.isEmpty() ? "" : " | filter: " + keyText)
                + ": " + modelPasien.getRowCount(), false);
    }

    private void prosesPilihPasienDariTabel() {
        if (sedangReloadPasien || sedangProsesPilihPasien) {
            return;
        }
        if (tblPasien == null || tblPasien.getSelectedRow() < 0) {
            return;
        }

        sedangProsesPilihPasien = true;
        try {
            onPasienSelected();
            panelMid.revalidate();
            panelMid.repaint();
        } catch (Exception e) {
            setStatus("Gagal menampilkan data pasien terpilih: " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            sedangProsesPilihPasien = false;
        }
    }

    private void onPasienSelected() {
        int viewRow = tblPasien.getSelectedRow();
        if (viewRow < 0) return;

        int row = viewRow;
        try {
            row = tblPasien.convertRowIndexToModel(viewRow);
        } catch (Exception e) {
            row = viewRow;
        }
        if (row < 0 || row >= modelPasien.getRowCount()) {
            setStatus("Baris pasien tidak valid. Silakan klik Refresh lalu pilih ulang.", true);
            return;
        }

        // Reset state
        resetWorkflow();

        selNoRawat = safe(String.valueOf(modelPasien.getValueAt(row, 0)));
        selNoRm = safe(String.valueOf(modelPasien.getValueAt(row, 1)));
        selNamaPasien = safe(String.valueOf(modelPasien.getValueAt(row, 2)));
        selKdPenyakit = safe(String.valueOf(modelPasien.getValueAt(row, 4)));
        selNmPenyakit = safe(String.valueOf(modelPasien.getValueAt(row, 5)));

        // Kolom tambahan v27: Encounter dan Condition.
        // Ambil langsung dari baris tabel supaya panelMid langsung terisi saat pasien diklik.
        selIdEncounterSS = modelPasien.getColumnCount() > 8
                ? safe(String.valueOf(modelPasien.getValueAt(row, 8))) : "";
        selIdConditionSS = modelPasien.getColumnCount() > 9
                ? safe(String.valueOf(modelPasien.getValueAt(row, 9))) : "";

        tNoRawat.setText(selNoRawat);
        tPasien.setText(selNoRm + " - " + selNamaPasien);
        tDiagnosa.setText(selKdPenyakit + " - " + selNmPenyakit);
        tEncounter.setText(selIdEncounterSS);
        tIdConditionIhs.setText(selIdConditionSS);

        // Lookup data Satu Sehat
        loadDataSatuSehat();

        // Patch v66: jika pasien ini sudah pernah dikirim tugas rujukan,
        // muat ulang state dari DB agar data tidak hilang saat form ditutup/dibuka kembali.
        boolean adaRujukanTersimpan = muatRujukanIGDExisting();
        if (!adaRujukanTersimpan) {
            // No.Rujukan internal generated (prefix RJK-IGD untuk distinguish dari ranap)
            noRujukanInternal = "RJK-IGD-" + System.currentTimeMillis();
            setStatus("Pasien terpilih. Belum ada draft/tugas rujukan IGD tersimpan untuk no.rawat ini.", false);
        }

        panelMid.revalidate();
        panelMid.repaint();
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
        appointmentId = "";
        serviceRequestId = "";
        setDefaultAppointmentTimes();
        if (cbJenisTransport != null && cbJenisTransport.getItemCount() > 0) {
            cbJenisTransport.setSelectedIndex(0);
        }
        lblNoRujukanNasional.setText("-");
        setStatusTab(lblTab1Status, " ", false);
        setStatusTab(lblTab2Status, " ", false);
        setStatusTab(lblTab3Status, " ", false);
        setStatusTab(lblTab4Status, " ", false);
        if (tabPane != null && tabPane.getTabCount() > 0) {
            tabPane.setSelectedIndex(0);
        }
    }


    /**
     * Patch v66: memuat kembali draft/tugas rujukan IGD yang sudah pernah dikirim.
     * Sebelumnya modelTugas hanya hidup di memori JTable, sehingga setelah form ditutup
     * data "menunggu" hilang dan user seolah harus kirim ulang. Method ini mengambil
     * data dari bridging_rujukan_satusehat_igd dan bridging_task_rujukan_satusehat
     * berdasarkan no_rawat pasien terpilih.
     */
    private boolean muatRujukanIGDExisting() {
        if (safe(selNoRawat).equals("")) {
            return false;
        }

        pastikanModelTabelWorkflow();

        boolean ditemukan = false;
        String statusRujukan = "";
        String stepTerakhir = "";
        String noRujNasional = "";

        // Ambil rujukan IGD terakhir untuk no_rawat ini. no_rujukan memakai timestamp
        // RJK-IGD-{millis}, jadi ORDER BY no_rujukan desc cukup aman untuk mengambil yang terakhir.
        String sql = "select no_rujukan, ifnull(task_pra_id,''), ifnull(task_cari_id,''), "
                + "ifnull(service_request_id,''), ifnull(no_rujukan_satusehat,''), "
                + "ifnull(id_pasien_satusehat,''), ifnull(id_dokter_satusehat,''), "
                + "ifnull(kd_diagnosa_primer,''), ifnull(nm_diagnosa_primer,''), "
                + "ifnull(status_rujukan,''), ifnull(step_terakhir,''), ifnull(careplan_id,'') "
                + "from bridging_rujukan_satusehat_igd "
                + "where no_rawat=? order by no_rujukan desc limit 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, selNoRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ditemukan = true;
                    noRujukanInternal = safe(rs.getString(1));
                    taskPraId = safe(rs.getString(2));
                    taskCariId = safe(rs.getString(3));
                    serviceRequestId = safe(rs.getString(4));
                    noRujNasional = safe(rs.getString(5));
                    String idPasienDb = safe(rs.getString(6));
                    String idDokterDb = safe(rs.getString(7));
                    String kdDiagDb = safe(rs.getString(8));
                    String nmDiagDb = safe(rs.getString(9));
                    statusRujukan = safe(rs.getString(10));
                    stepTerakhir = safe(rs.getString(11));

                    if (!idPasienDb.equals("")) {
                        selIdPasienSS = idPasienDb;
                        tIdPasienIhs.setText(selIdPasienSS);
                    }
                    if (!idDokterDb.equals("")) {
                        selIdDokterSS = idDokterDb;
                        tIdDokterIhs.setText(selIdDokterSS);
                    }
                    if (!kdDiagDb.equals("")) {
                        selKdPenyakit = kdDiagDb;
                        selNmPenyakit = nmDiagDb;
                        tDiagnosa.setText(selKdPenyakit + " - " + selNmPenyakit);
                    }
                    if (!noRujNasional.equals("")) {
                        lblNoRujukanNasional.setText(noRujNasional);
                    }
                }
            }
        } catch (Exception ex) {
            // Fallback kalau ada instalasi lama belum punya kolom tambahan seperti careplan_id.
            System.out.println("muatRujukanIGDExisting query lengkap gagal: " + ex);
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select no_rujukan, ifnull(task_pra_id,''), ifnull(task_cari_id,''), "
                    + "ifnull(service_request_id,''), ifnull(id_pasien_satusehat,''), "
                    + "ifnull(id_dokter_satusehat,''), ifnull(status_rujukan,''), ifnull(step_terakhir,'') "
                    + "from bridging_rujukan_satusehat_igd where no_rawat=? order by no_rujukan desc limit 1")) {
                ps.setString(1, selNoRawat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ditemukan = true;
                        noRujukanInternal = safe(rs.getString(1));
                        taskPraId = safe(rs.getString(2));
                        taskCariId = safe(rs.getString(3));
                        serviceRequestId = safe(rs.getString(4));
                        String idPasienDb = safe(rs.getString(5));
                        String idDokterDb = safe(rs.getString(6));
                        statusRujukan = safe(rs.getString(7));
                        stepTerakhir = safe(rs.getString(8));
                        if (!idPasienDb.equals("")) {
                            selIdPasienSS = idPasienDb;
                            tIdPasienIhs.setText(selIdPasienSS);
                        }
                        if (!idDokterDb.equals("")) {
                            selIdDokterSS = idDokterDb;
                            tIdDokterIhs.setText(selIdDokterSS);
                        }
                    }
                }
            } catch (Exception ex2) {
                System.out.println("muatRujukanIGDExisting fallback gagal: " + ex2);
            }
        }

        if (!ditemukan || safe(noRujukanInternal).equals("")) {
            return false;
        }

        muatConditionDariCarePlanJikaKosong();
        int jumlahTask = muatTaskTrackingIGD();

        if (!taskPraId.equals("")) {
            setStatusTab(lblTab1Status, "Draft pra permintaan tersimpan. Task Pra: " + taskPraId, false);
        }
        if (!taskCariId.equals("")) {
            setStatusTab(lblTab2Status, "Data cari kandidat tersimpan. Task Cari: " + taskCariId, false);
        }
        if (jumlahTask > 0) {
            setStatusTab(lblTab3Status,
                    "Data tugas rujukan tersimpan sudah dimuat: " + jumlahTask
                    + " task. Klik [Cek Semua] untuk update respon terbaru.", false);
        }
        if (!serviceRequestId.equals("")) {
            setStatusTab(lblTab4Status,
                    "Rujukan final tersimpan. ServiceRequest: " + serviceRequestId, false);
        }

        arahkanTabSetelahRestore(stepTerakhir, jumlahTask);
        setStatus("Data rujukan IGD tersimpan dimuat kembali. No internal: " + noRujukanInternal
                + (statusRujukan.equals("") ? "" : " | Status: " + statusRujukan), false);
        return true;
    }

    private void muatConditionDariCarePlanJikaKosong() {
        if (!safe(selIdConditionSS).equals("")) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(id_condition_diagnosa,'') from bridging_careplan_rujukan "
                + "where no_rujukan=? order by no_rujukan desc limit 1")) {
            ps.setString(1, noRujukanInternal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selIdConditionSS = safe(rs.getString(1));
                    tIdConditionIhs.setText(selIdConditionSS);
                }
            }
        } catch (Exception ex) {
            System.out.println("muatConditionDariCarePlanJikaKosong err: " + ex);
        }
    }

    private int muatTaskTrackingIGD() {
        modelTugas.setRowCount(0);
        modelAcceptedFaskes.setRowCount(0);
        if (safe(noRujukanInternal).equals("")) {
            return 0;
        }
        int count = 0;
        String sql = "select task_id, ifnull(kdppk_kandidat,''), ifnull(nmppk_kandidat,''), "
                + "ifnull(task_status,''), respon_diterima "
                + "from bridging_task_rujukan_satusehat "
                + "where no_rujukan=? and tipe_rujukan='igd' order by task_id";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRujukanInternal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String taskId = safe(rs.getString(1));
                    String idFaskes = safe(rs.getString(2));
                    String namaFaskes = safe(rs.getString(3));
                    String status = safe(rs.getString(4));
                    Object objAccept = rs.getObject(5);
                    Boolean accept = null;
                    if (objAccept instanceof Number) {
                        accept = ((Number) objAccept).intValue() == 1;
                    } else if (objAccept != null) {
                        String v = String.valueOf(objAccept).trim();
                        if ("1".equals(v) || "true".equalsIgnoreCase(v)) accept = Boolean.TRUE;
                        if ("0".equals(v) || "false".equalsIgnoreCase(v)) accept = Boolean.FALSE;
                    }

                    if (status.equals("")) {
                        status = Boolean.TRUE.equals(accept) ? "completed" : "requested";
                    }
                    String respon = accept == null ? "menunggu" : (accept ? "ACCEPTED" : "REJECTED");
                    String sr = safe(serviceRequestId).equals("") ? "-" : serviceRequestId;

                    modelTugas.addRow(new Object[]{
                            taskId, sr, namaFaskes, status, respon, "tersimpan lokal"
                    });
                    if (Boolean.TRUE.equals(accept)) {
                        modelAcceptedFaskes.addRow(new Object[]{
                                Boolean.FALSE, taskId, idFaskes, namaFaskes, "tersimpan lokal"
                        });
                    }
                    count++;
                }
            }
        } catch (Exception ex) {
            System.out.println("muatTaskTrackingIGD err: " + ex);
        }
        return count;
    }

    private void arahkanTabSetelahRestore(String stepTerakhir, int jumlahTask) {
        try {
            int step = 0;
            try { step = Integer.parseInt(safe(stepTerakhir)); } catch (Exception ignore) {}
            if (step >= 4 || !safe(serviceRequestId).equals("")) {
                tabPane.setSelectedIndex(3);
            } else if (modelAcceptedFaskes.getRowCount() > 0) {
                tabPane.setSelectedIndex(3);
            } else if (jumlahTask > 0 || step >= 3) {
                tabPane.setSelectedIndex(2);
            } else if (!safe(taskCariId).equals("") || step >= 2) {
                tabPane.setSelectedIndex(1);
            } else if (!safe(taskPraId).equals("")) {
                tabPane.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            System.out.println("arahkanTabSetelahRestore err: " + ex);
        }
    }

    private void loadDataSatuSehat() {
        if (GUNAKAN_DUMMY_RUJUKAN_IGD) {
            applyDummyDataIGD();
            return;
        }

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

    private void applyDummyDataIGD() {
        selNoKtpPasien = dummyData.nikPasien;
        selNoKtpDokter = dummyData.nikDokter;
        selIdPasienSS = dummyData.ihsPasien;
        selIdDokterSS = dummyData.ihsDokter;

        // Kunci diagnosa dummy agar cocok dengan data uji SATUSEHAT STG.
        selKdPenyakit = DUMMY_DIAGNOSA_KODE;
        selNmPenyakit = DUMMY_DIAGNOSA_NAMA;

        // Encounter dan Condition tetap diambil dari kolom tabel jika sudah ada.
        // Catatan: untuk pengiriman ke STG, pastikan ID Encounter/Condition tersebut benar-benar berasal dari STG.
        selIdEncounterSS = safe(selIdEncounterSS);
        selIdConditionSS = safe(selIdConditionSS);

        tDiagnosa.setText(selKdPenyakit + " - " + selNmPenyakit);
        tIdPasienIhs.setText(selIdPasienSS);
        tIdDokterIhs.setText(selIdDokterSS);
        tEncounter.setText(selIdEncounterSS);
        tIdConditionIhs.setText(selIdConditionSS);
        tNoKartuAsuransi.setText(dummyData.noKartuJkn);
        tPasien.setText(selNoRm + " - " + dummyData.namaPasien + " (DUMMY STG)");

        String info = "MODE DUMMY STG aktif.";
        if (!selIdEncounterSS.isEmpty() || !selIdConditionSS.isEmpty()) {
            info += " Encounter/Condition dari tabel sudah dimuat ke panel pasien.";
        } else {
            info += " Isi/paste Encounter STG dulu sebelum kirim pra permintaan.";
        }
        setStatus(info, selIdEncounterSS.isEmpty());
    }

    /**
     * Sinkronkan field yang bisa diedit manual ke state internal sebelum kirim API.
     * Ini penting untuk mode dummy karena Encounter STG biasanya dipaste manual ke field Encounter.
     */
    private void syncFieldState() {
        selIdEncounterSS = safe(tEncounter.getText());
        selIdPasienSS = safe(tIdPasienIhs.getText());
        selIdDokterSS = safe(tIdDokterIhs.getText());
        selIdConditionSS = safe(tIdConditionIhs.getText());

        String diag = safe(tDiagnosa.getText());
        if (diag.contains(" - ")) {
            selKdPenyakit = safe(diag.substring(0, diag.indexOf(" - ")));
            selNmPenyakit = safe(diag.substring(diag.indexOf(" - ") + 3));
        }

        if (GUNAKAN_DUMMY_RUJUKAN_IGD) {
            if (selIdPasienSS.isEmpty()) {
                selIdPasienSS = dummyData.ihsPasien;
                tIdPasienIhs.setText(selIdPasienSS);
            }
            if (selIdDokterSS.isEmpty()) {
                selIdDokterSS = dummyData.ihsDokter;
                tIdDokterIhs.setText(selIdDokterSS);
            }
            if (selKdPenyakit.isEmpty()) {
                selKdPenyakit = DUMMY_DIAGNOSA_KODE;
                selNmPenyakit = DUMMY_DIAGNOSA_NAMA;
                tDiagnosa.setText(selKdPenyakit + " - " + selNmPenyakit);
            }
            if (safe(tNoKartuAsuransi.getText()).isEmpty()) {
                tNoKartuAsuransi.setText(dummyData.noKartuJkn);
            }
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
                int w = (int) (parentWindow.getWidth() * 0.95);
                int h = (int) (parentWindow.getHeight() * 0.95);
                dlg.setSize(Math.max(w, 1180), Math.max(h, 700));
            } else {
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
    //  ACTION - BUAT ENCOUNTER IGD
    //  Mode STG bisa merespons balik ID Encounter selama Patient, Practitioner,
    //  Organization, dan Location yang dikirim valid di SATUSEHAT STG.
    // =================================================================
    private void doBuatEncounterIGD() {
        if (!validatePasienTerpilih()) return;
        syncFieldState();

        if (selIdPasienSS.isEmpty() || selIdDokterSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "IHS Pasien / IHS Dokter belum lengkap.\n"
                    + "Pada mode dummy, IHS akan otomatis diisi dari data dummy setelah pasien dipilih.");
            return;
        }

        String idLokasi = pilihIdLokasiEncounterIGD();
        if (idLokasi == null || idLokasi.trim().equals("")) {
            setStatus("Buat Encounter dibatalkan karena Location SATUSEHAT belum diisi.", true);
            return;
        }
        idLokasi = bersihkanReferenceId(idLokasi, "Location/");
        terakhirIdLokasiEncounterIGD = idLokasi;

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Buat Encounter IGD baru di SATUSEHAT STG?\n\n"
                + "Patient     : " + selIdPasienSS + "\n"
                + "Practitioner: " + selIdDokterSS + "\n"
                + "Location    : " + idLokasi + "\n\n"
                + "Jika berhasil, ID Encounter akan otomatis masuk ke kotak Encounter.",
                "Konfirmasi Buat Encounter", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JsonNode resp = postEncounterIGDKeSatuSehat(idLokasi);
            String id = safe(resp.path("id").asText());
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "SATUSEHAT tidak mengembalikan ID Encounter. Response:\n" + resp);
                setStatus("Gagal membuat Encounter: response tidak berisi id.", true);
                return;
            }

            selIdEncounterSS = id;
            tEncounter.setText(id);
            selIdConditionSS = "";
            tIdConditionIhs.setText("");

            setStatus("Encounter IGD berhasil dibuat di SATUSEHAT STG. ID: " + id, false);
            JOptionPane.showMessageDialog(this,
                    "Encounter IGD berhasil dibuat.\n\nID Encounter:\n" + id
                    + "\n\nLanjutkan klik [Buat Condition].");
        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String pilihIdLokasiEncounterIGD() {
        String contoh = terakhirIdLokasiEncounterIGD;
        if (contoh == null || contoh.trim().equals("")) {
            contoh = "";
        }

        return JOptionPane.showInputDialog(this,
                "Masukkan ID Location SATUSEHAT STG untuk IGD.\n"
                + "Ambil dari menu: Mapping Lokasi Satu Sehat.\n"
                + "Isi ID saja, boleh juga paste format Location/{id}.\n\n"
                + "Catatan: SATUSEHAT menolak Encounter IGD jika Encounter.location kosong.",
                contoh);
    }

    private JsonNode postEncounterIGDKeSatuSehat(String idLokasiSS) throws Exception {
        String now = ihsEncounter.getUtcDatetimeNow();
        String orgId = safe(ihsEncounter.getOrgIdPerujuk());
        if (orgId.isEmpty()) {
            orgId = safe(koneksiDB.IDSATUSEHAT());
        }

        String idPasien = bersihkanReferenceId(selIdPasienSS, "Patient/");
        String idDokter = bersihkanReferenceId(selIdDokterSS, "Practitioner/");
        String idLokasi = bersihkanReferenceId(idLokasiSS, "Location/");
        String namaPasienKirim = GUNAKAN_DUMMY_RUJUKAN_IGD ? dummyData.namaPasien : selNamaPasien;

        String noRawatAman = safe(selNoRawat).replace("/", "-").replace(" ", "");
        if (noRawatAman.isEmpty()) {
            noRawatAman = "IGD";
        }
        String nilaiIdentifier = "DUMMY-IGD-" + noRawatAman + "-" + System.currentTimeMillis();

        String body = "{"
                + "\"resourceType\":\"Encounter\","
                + "\"identifier\":[{"
                    + "\"system\":\"http://sys-ids.kemkes.go.id/encounter/" + escape(orgId) + "\","
                    + "\"value\":\"" + escape(nilaiIdentifier) + "\""
                + "}],"
                + "\"status\":\"arrived\","
                + "\"statusHistory\":[{"
                    + "\"status\":\"arrived\","
                    + "\"period\":{\"start\":\"" + escape(now) + "\"}"
                + "}],"
                + "\"class\":{"
                    + "\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\","
                    + "\"code\":\"EMER\","
                    + "\"display\":\"emergency\""
                + "},"
                + "\"classHistory\":[{"
                    + "\"class\":{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\","
                        + "\"code\":\"EMER\","
                        + "\"display\":\"emergency\""
                    + "},"
                    + "\"period\":{\"start\":\"" + escape(now) + "\"}"
                + "}],"
                + "\"subject\":{"
                    + "\"reference\":\"Patient/" + escape(idPasien) + "\","
                    + "\"display\":\"" + escape(namaPasienKirim) + "\""
                + "},"
                + "\"participant\":[{"
                    + "\"type\":[{\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\","
                        + "\"code\":\"ATND\","
                        + "\"display\":\"attender\""
                    + "}]}],"
                    + "\"individual\":{\"reference\":\"Practitioner/" + escape(idDokter) + "\"}"
                + "}],"
                + "\"period\":{\"start\":\"" + escape(now) + "\"},"
                + "\"location\":[{"
                    + "\"location\":{\"reference\":\"Location/" + escape(idLokasi) + "\"},"
                    + "\"period\":{\"start\":\"" + escape(now) + "\"}"
                + "}],"
                + "\"serviceProvider\":{\"reference\":\"Organization/" + escape(orgId) + "\"}"
            + "}";

        return postFhirEncounterIGD("/Encounter", body);
    }

    private JsonNode postFhirEncounterIGD(String pathFhir, String body) throws Exception {
        String url = ihsEncounter.getBaseUrl() + (pathFhir == null ? "" : pathFhir);
        RestTemplate rest = ihsEncounter.getRest();

        System.out.println("==== POST Encounter IGD ====");
        System.out.println("URL : " + url);
        System.out.println("Body: " + body);

        try {
            HttpEntity<String> entity = new HttpEntity<>(body, ihsEncounter.buildAuthHeaders());
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("Status: " + resp.getStatusCode());
            System.out.println("Response: " + resp.getBody());
            catatResponTransaksi("Buat Encounter IGD", "POST", url, body, resp.getBody(), false);
            return ihsEncounter.getMapper().readTree(resp.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                System.out.println("Got 401 saat POST Encounter, refresh token & retry...");
                ihsEncounter.forceRefreshToken();
                HttpEntity<String> entity = new HttpEntity<>(body, ihsEncounter.buildAuthHeaders());
                ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);
                System.out.println("Status retry: " + resp.getStatusCode());
                System.out.println("Response retry: " + resp.getBody());
                catatResponTransaksi("Buat Encounter IGD (retry token)", "POST", url, body, resp.getBody(), false);
                return ihsEncounter.getMapper().readTree(resp.getBody());
            }
            System.out.println("HTTP error POST Encounter IGD " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            catatResponTransaksi("Buat Encounter IGD", "POST", url, body, e.getResponseBodyAsString(), true);
            throw e;
        }
    }

    private String bersihkanReferenceId(String id, String prefix) {
        String hasil = safe(id);
        if (prefix != null && !prefix.equals("") && hasil.startsWith(prefix)) {
            hasil = hasil.substring(prefix.length());
        }
        return hasil;
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

            // Karena ganti diagnosa, ID Condition lama jadi tidak relevan -> reset.
            // Mode dummy TIDAK boleh auto membuat Encounter/Condition dan tidak boleh re-use
            // condition lama dari DB lokal, karena data STG sering berbeda dengan production.
            selIdConditionSS = "";
            tIdConditionIhs.setText("");

            if (GUNAKAN_DUMMY_RUJUKAN_IGD) {
                JOptionPane.showMessageDialog(this,
                        "Diagnosa rujukan diset ke:\n" + kdPenyakit + " - " + nmPenyakit
                        + "\n\nMode dummy aktif: diagnosa tidak disimpan ke diagnosa_pasien."
                        + "\nKlik [Buat Condition] jika Encounter STG sudah valid.");
                return;
            }

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
                        // Insert dengan prioritas=1 (primer), status='Ralan' untuk IGD
                        Sequel.menyimpantf2("diagnosa_pasien",
                                "?,?,?,?", "Diagnosa Pasien", 4,
                                new String[]{
                                    selNoRawat,
                                    kdPenyakit,
                                    "Ralan",       // status (IGD = Ralan / Rawat Jalan IGD)
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

            tabelPenyakit.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) doPilih();
                }
            });
            tabelPenyakit.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE
                            || e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        doPilih();
                        e.consume();
                    }
                }
            });

            buildUi();
            loadData("");

            setSize(700, 500);
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            setLayout(new BorderLayout(4, 4));

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

            widget.ScrollPane sp = new widget.ScrollPane();
            sp.setViewportView(tabelPenyakit);
            add(sp, BorderLayout.CENTER);

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
        syncFieldState();
        if (selIdEncounterSS.isEmpty() || selIdPasienSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    GUNAKAN_DUMMY_RUJUKAN_IGD
                    ? "Encounter STG / IHS Pasien belum lengkap.\n\nMode dummy tidak membuat Encounter otomatis.\nSilakan paste ID Encounter STG yang valid ke field Encounter, lalu klik [Buat Condition]."
                    : "Encounter / IHS Pasien belum lengkap.");
            return;
        }

        // Jika user sudah mengisi ID Condition secara manual, gunakan saja.
        // Ini menjaga agar mode dummy tidak POST ulang tanpa sengaja.
        if (GUNAKAN_DUMMY_RUJUKAN_IGD && !safe(tIdConditionIhs.getText()).isEmpty()) {
            selIdConditionSS = safe(tIdConditionIhs.getText());
            setStatus("MODE DUMMY: Condition dari field dipakai. ID: " + selIdConditionSS, false);
            JOptionPane.showMessageDialog(this,
                    "Mode dummy: Condition dari field dipakai.\nID: " + selIdConditionSS);
            return;
        }

        // Step 1: cek tabel existing satu_sehat_condition dulu HANYA untuk mode real.
        // Mode dummy tidak re-use DB lokal supaya tidak tercampur dengan data production.
        String statusCondition = Sequel.cariIsi(
                "select status_lanjut from reg_periksa where no_rawat=?", selNoRawat);
        if (statusCondition == null || statusCondition.isEmpty()) {
            statusCondition = "Ralan";
        }

        if (!GUNAKAN_DUMMY_RUJUKAN_IGD) {
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
        }

        // Step 2: belum ada, POST baru. Tidak membuat Encounter otomatis.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String namaPasienKirim = GUNAKAN_DUMMY_RUJUKAN_IGD ? dummyData.namaPasien : selNamaPasien;
            JsonNode resp = svc.kirimCondition(
                    selIdPasienSS, selIdEncounterSS, namaPasienKirim,
                    selKdPenyakit, selNmPenyakit, "encounter-diagnosis");
            catatResponTransaksi("Buat Condition", "POST", "SatuSehatRujukanService.kirimCondition",
                    "Patient=" + selIdPasienSS + "\nEncounter=" + selIdEncounterSS + "\nDiagnosa=" + selKdPenyakit + " - " + selNmPenyakit,
                    jsonNodeToPretty(resp), false);

            String id = svc.getConditionId(resp);
            if (id == null || id.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Gagal buat Condition. Response:\n" + resp);
                setStatus("Gagal buat Condition.", true);
                return;
            }
            selIdConditionSS = id;
            tIdConditionIhs.setText(id);

            // Simpan ke tabel existing hanya untuk mode real. Dummy STG jangan menulis ke DB lokal.
            if (!GUNAKAN_DUMMY_RUJUKAN_IGD) {
                try {
                    Sequel.menyimpantf2("satu_sehat_condition", "?,?,?,?",
                            "Diagnosa", 4, new String[]{
                                    selNoRawat, selKdPenyakit, statusCondition, id
                            });
                } catch (Exception e) {
                    System.out.println("Simpan satu_sehat_condition gagal: " + e);
                }
            }

            setStatus((GUNAKAN_DUMMY_RUJUKAN_IGD ? "MODE DUMMY: " : "")
                    + "Condition berhasil dibuat. ID: " + id, false);
            JOptionPane.showMessageDialog(this,
                    "Condition diagnosa berhasil dibuat di Satu Sehat.\nID: " + id
                    + (GUNAKAN_DUMMY_RUJUKAN_IGD ? "\n\nMode dummy: ID tidak disimpan ke DB lokal." : ""));
        } catch (Exception ex) {
            if (isDuplicateConditionError(ex)) {
                try {
                    String existingId = cariConditionIhsExisting(selIdPasienSS, selIdEncounterSS, selKdPenyakit);
                    if (!existingId.equals("")) {
                        selIdConditionSS = existingId;
                        tIdConditionIhs.setText(existingId);

                        if (!GUNAKAN_DUMMY_RUJUKAN_IGD) {
                            try {
                                Sequel.menyimpantf2("satu_sehat_condition", "?,?,?,?",
                                        "Diagnosa", 4, new String[]{
                                                selNoRawat, selKdPenyakit, statusCondition, existingId
                                        });
                            } catch (Exception e) {
                                System.out.println("Simpan ulang satu_sehat_condition dari duplicate gagal: " + e);
                            }
                        }

                        setStatus("Condition sudah ada di SATUSEHAT, dipakai ulang. ID: " + existingId, false);
                        JOptionPane.showMessageDialog(this,
                                "SATUSEHAT menolak POST karena Condition sudah pernah dibuat.\n"
                                + "Sistem sudah mencari Condition existing dan memakainya ulang.\n\n"
                                + "ID Condition: " + existingId,
                                "Condition Sudah Ada", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(this,
                            "SATUSEHAT menolak POST karena Condition dianggap duplikat,\n"
                            + "tetapi pencarian Condition existing belum menemukan ID-nya.\n\n"
                            + "Coba klik tombol [?] untuk melihat response, atau cek manual Condition di SATUSEHAT STG.",
                            "Condition Duplikat", JOptionPane.WARNING_MESSAGE);
                    setStatus("Condition duplicate, tetapi ID existing belum ditemukan.", true);
                    return;
                } catch (Exception cariEx) {
                    System.out.println("Cari Condition existing setelah duplicate gagal: " + cariEx);
                    handleApiError(cariEx);
                    return;
                }
            }
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isDuplicateConditionError(Exception ex) {
        try {
            if (ex instanceof org.springframework.web.client.HttpStatusCodeException) {
                String body = ((org.springframework.web.client.HttpStatusCodeException) ex).getResponseBodyAsString();
                return body != null
                        && body.toLowerCase().contains("duplicate")
                        && body.toLowerCase().contains("condition");
            }
        } catch (Exception ignore) {}
        return false;
    }

    private String cariConditionIhsExisting(String patientId, String encounterId, String kdPenyakit) throws Exception {
        String id = cariConditionIhsExistingSekali(patientId, encounterId, kdPenyakit, true);
        if (!id.equals("")) {
            return id;
        }
        return cariConditionIhsExistingSekali(patientId, encounterId, kdPenyakit, false);
    }

    private String cariConditionIhsExistingSekali(String patientId, String encounterId, String kdPenyakit, boolean pakaiFilterCode) throws Exception {
        String subjectRef = "Patient/" + bersihkanReferenceId(patientId, "Patient/");
        String encounterRef = "Encounter/" + bersihkanReferenceId(encounterId, "Encounter/");

        String url = ihsEncounter.getBaseUrl()
                + "/Condition?subject=" + urlEncode(subjectRef)
                + "&encounter=" + urlEncode(encounterRef);
        if (pakaiFilterCode) {
            url += "&code=" + urlEncode("http://hl7.org/fhir/sid/icd-10|" + kdPenyakit);
        }

        System.out.println("==== SatuSehatRujukan GET Condition Existing ====");
        System.out.println("URL : " + url);

        RestTemplate rest = ihsEncounter.getRest();
        String body;
        try {
            HttpEntity<String> entity = new HttpEntity<>(ihsEncounter.buildAuthHeaders());
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);
            body = resp.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                ihsEncounter.forceRefreshToken();
                HttpEntity<String> entity = new HttpEntity<>(ihsEncounter.buildAuthHeaders());
                ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);
                body = resp.getBody();
            } else {
                catatResponTransaksi("Cari Condition Existing", "GET", url, "", e.getResponseBodyAsString(), true);
                throw e;
            }
        }

        catatResponTransaksi("Cari Condition Existing", "GET", url, "", body, false);
        JsonNode bundle = ihsEncounter.getMapper().readTree(body == null ? "{}" : body);
        JsonNode entries = bundle.path("entry");
        if (entries.isArray()) {
            for (JsonNode entry : entries) {
                JsonNode res = entry.path("resource");
                if (!"Condition".equals(res.path("resourceType").asText())) {
                    continue;
                }
                if (!kdPenyakit.equals("")) {
                    boolean kodeCocok = false;
                    JsonNode codings = res.path("code").path("coding");
                    if (codings.isArray()) {
                        for (JsonNode coding : codings) {
                            if (kdPenyakit.equals(coding.path("code").asText())) {
                                kodeCocok = true;
                                break;
                            }
                        }
                    }
                    if (!kodeCocok) {
                        continue;
                    }
                }
                String id = res.path("id").asText();
                if (id != null && !id.trim().equals("")) {
                    return id.trim();
                }
            }
        }
        return "";
    }

    private String urlEncode(String value) throws Exception {
        return java.net.URLEncoder.encode(value == null ? "" : value, "UTF-8").replace("+", "%20");
    }

    // =================================================================
    //  TAB 1 ACTION
    // =================================================================
    private void doKirimPra() {
        if (!validatePasienTerpilih()) return;
        syncFieldState();
        if (!validateIhsLengkap()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab1Status, "Mengirim pra permintaan sesuai Postman IGD SATUSEHAT...", false);
        try {
            JsonNode resp = kirimPraPermintaanIgdPostmanStyle();
            taskPraId = resp.path("id").asText();

            catatResponTransaksi("Kirim Pra Permintaan", "POST", "Task referral-pre-request IGD Postman Style",
                    "Patient=" + selIdPasienSS
                    + "\nEncounter=" + selIdEncounterSS
                    + "\nDiagnosa=" + selKdPenyakit + " - " + selNmPenyakit
                    + "\nManagement=385868005 Emergency treatment management",
                    jsonNodeToPretty(resp), false);

            pastikanModelTabelWorkflow();
            modelKriteria.setRowCount(0);
            boolean foundQ = isiKriteriaDariPraPermintaan(resp);
            setupJawabanKriteriaEditor();

            // Kalau STG tidak mengirim Questionnaire, tetap tampilkan 5 kriteria IGD default dari collection.
            if (!foundQ) {
                isiKriteriaIGDDefault();
                foundQ = modelKriteria.getRowCount() > 0;
            }

            simpanRujukanIGDDraft();

            if (foundQ) {
                JOptionPane.showMessageDialog(this,
                        "Pra Permintaan berhasil. Task ID: " + taskPraId
                        + "\nKriteria IGD sudah dimuat. Isi YA/TIDAK, lalu lanjut Tab 2.");
                setStatusTab(lblTab1Status,
                        "OK. Task ID: " + taskPraId + ", " + modelKriteria.getRowCount() + " kriteria IGD.", false);
                setStatus("Pra Permintaan terkirim sesuai JSON Postman IGD.", false);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Pra Permintaan terkirim (Task ID: " + taskPraId + ").\n"
                        + "Tidak ada kuesioner di response, lanjutkan Tab 2 jika diperlukan.");
                setStatusTab(lblTab1Status,
                        "OK. Task ID: " + taskPraId + " (tanpa kuesioner)", false);
                setStatus("Pra Permintaan terkirim.", false);
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
        syncFieldState();
        if (taskPraId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Klik [Kirim Pra Permintaan] di Tab 1 dulu.");
            return;
        }

        String qrCriteria = buildQrCriteriaJson();
        String qrArea = buildQrAreaJson();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab2Status, "Mencari kandidat sesuai Postman IGD SATUSEHAT...", false);
        try {
            JsonNode resp = cariKandidatIgdPostmanStyle(qrCriteria, qrArea);
            taskCariId = resp.path("id").asText();

            catatResponTransaksi("Cari Kandidat", "POST", "Task request-referral-candidate IGD Postman Style",
                    "Patient=" + selIdPasienSS
                    + "\nEncounter=" + selIdEncounterSS
                    + "\nDiagnosa=" + selKdPenyakit + " - " + selNmPenyakit
                    + "\nProvinsi=" + getKodeProvinsiTerpilih() + " - " + getNamaProvinsiTerpilih()
                    + "\nKab/Kota=" + getKodeKabupatenTerpilih() + " - " + getNamaKabupatenTerpilih(),
                    jsonNodeToPretty(resp), false);

            pastikanModelTabelWorkflow();
            modelKandidat.setRowCount(0);
            isiKandidatDariTask(resp);

            if (modelKandidat.getRowCount() == 0 && taskCariId != null && !taskCariId.trim().equals("")) {
                setStatusTab(lblTab2Status,
                        "Task kandidat berhasil dibuat. Menunggu output kandidat dari SATUSEHAT STG...",
                        false);
                for (int i = 1; i <= 5 && modelKandidat.getRowCount() == 0; i++) {
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    try {
                        JsonNode respCek = svc.cekStatusAccept(taskCariId);
                        catatResponTransaksi("Polling Kandidat", "GET", "GET Task/" + taskCariId,
                                "Task ID=" + taskCariId, jsonNodeToPretty(respCek), false);
                        isiKandidatDariTask(respCek);
                    } catch (Exception cekEx) {
                        System.out.println("Polling kandidat ke-" + i + " gagal: " + cekEx.getMessage());
                    }
                }
            }

            updateTaskCariId();

            if (modelKandidat.getRowCount() > 0) {
                setStatusTab(lblTab2Status,
                        "Ditemukan " + modelKandidat.getRowCount() + " kandidat. Centang salah satu kandidat; setelah dicentang otomatis pindah ke Tab 3.",
                        false);
                setStatus("Kandidat ditemukan: " + modelKandidat.getRowCount() + ". Pilih/ceklis satu kandidat di Tab 2.", false);
                // Revisi v66: jangan otomatis pindah tab setelah kandidat muncul.
                // User harus memilih/men-ceklis satu kandidat terlebih dahulu.
            } else {
                setStatusTab(lblTab2Status,
                        "Task kandidat terkirim, tapi SATUSEHAT STG belum/tidak mengembalikan kandidat. Task ID: " + taskCariId,
                        true);
                setStatus("Kandidat ditemukan: 0. Task kandidat sudah dibuat, namun response Task tidak berisi output kandidat.", true);
                JOptionPane.showMessageDialog(this,
                        "Permintaan kandidat berhasil dikirim ke SATUSEHAT STG, tetapi belum ada kandidat yang dikembalikan.\n\n"
                        + "Task ID: " + taskCariId + "\n\n"
                        + "Patch ini sudah mengikuti struktur Postman IGD: Q100 kriteria, Q101 provinsi/kabupaten, "
                        + "management 385868005, primary diagnosis, dan identifier task.\n\n"
                        + "Jika tetap kosong, kemungkinan kombinasi dummy STG belum punya jejaring faskes kandidat.",
                        "Kandidat Kosong", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            handleApiError(ex);
            setStatusTab(lblTab2Status, "Error: " + ex.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void isiKandidatDariTask(JsonNode resp) {
        if (resp == null) {
            return;
        }
        JsonNode outputs = resp.path("output");
        if (!outputs.isArray()) {
            return;
        }
        for (JsonNode out : outputs) {
            JsonNode typeCoding = out.path("type").path("coding");
            boolean isCandidate = false;
            if (typeCoding.isArray()) {
                for (JsonNode tc : typeCoding) {
                    String code = tc.path("code").asText();
                    if ("candidate".equals(code)
                            || "candidate-referral-facility".equals(code)
                            || "candidate-facility".equals(code)
                            || "referral-candidate".equals(code)) {
                        isCandidate = true;
                        break;
                    }
                }
            }
            if (!isCandidate) {
                continue;
            }

            String orgRef = out.path("valueReference").path("reference").asText();
            String orgId = orgRef.startsWith("Organization/") ? orgRef.substring(13) : orgRef;
            String orgDisp = out.path("valueReference").path("display").asText();

            String strata = ambilExtension(out.path("extension"), "strata");
            String distance = ambilExtension(out.path("extension"), "distance");
            String estimated = ambilExtension(out.path("extension"), "estimated-time");
            String bpjsCode = ambilExtension(out.path("extension"), "bpjs-code");

            if (distance.equals("") && !estimated.equals("")) {
                distance = estimated;
            } else if (!distance.equals("") && !estimated.equals("")) {
                distance = distance + " / " + estimated;
            }

            if (orgDisp == null || orgDisp.trim().equals("")) {
                continue;
            }
            if (orgId == null || orgId.trim().equals("")) {
                orgId = "null";
            }
            if (!kandidatSudahAda(orgId + "|" + orgDisp)) {
                modelKandidat.addRow(new Object[]{
                        Boolean.FALSE, orgId, bpjsCode, orgDisp, strata, distance
                });
            }
        }
    }

    private boolean kandidatSudahAda(String key) {
        if (key == null) {
            return false;
        }
        for (int i = 0; i < modelKandidat.getRowCount(); i++) {
            String k = String.valueOf(modelKandidat.getValueAt(i, 1))
                    + "|" + String.valueOf(modelKandidat.getValueAt(i, 3));
            if (key.equals(k) || key.equals(String.valueOf(modelKandidat.getValueAt(i, 1)))) {
                return true;
            }
        }
        return false;
    }


    // =================================================================
    //  HTTP & JSON POSTMAN STYLE IGD
    // =================================================================
    private JsonNode kirimPraPermintaanIgdPostmanStyle() throws Exception {
        String now = ihsEncounter.getUtcDatetimeNow();
        String orgId = getOrgIdPerujukAman();
        String registrationId = buatRegistrationId("PRA");

        String body = "{"
                + "\"resourceType\":\"Task\","
                + "\"identifier\":[{"
                    + "\"system\":\"http://sys-ids.kemkes.go.id/task/" + escape(orgId) + "\","
                    + "\"value\":\"" + escape(registrationId) + "\""
                + "}],"
                + "\"status\":\"requested\","
                + "\"intent\":\"instance-order\","
                + "\"priority\":\"routine\","
                + "\"code\":{\"coding\":[{"
                    + "\"system\":\"http://terminology.kemkes.go.id\","
                    + "\"code\":\"referral-pre-request\","
                    + "\"display\":\"Referral pre request\""
                + "}]},"
                + "\"authoredOn\":\"" + escape(now) + "\","
                + "\"lastModified\":\"" + escape(now) + "\","
                + "\"requester\":{\"reference\":\"Organization/" + escape(orgId) + "\"},"
                + "\"owner\":{\"reference\":\"Organization/" + escape(orgId) + "\"},"
                + "\"for\":{\"reference\":\"Patient/" + escape(bersihkanReferenceId(selIdPasienSS, "Patient/")) + "\"},"
                + "\"encounter\":{\"reference\":\"Encounter/" + escape(bersihkanReferenceId(selIdEncounterSS, "Encounter/")) + "\"},"
                + "\"input\":["
                    + "{\"type\":{\"coding\":[{"
                        + "\"system\":\"http://snomed.info/sct\","
                        + "\"code\":\"119270007\","
                        + "\"display\":\"Management procedure\""
                    + "}]},\"valueCoding\":{"
                        + "\"system\":\"http://snomed.info/sct\","
                        + "\"code\":\"385868005\","
                        + "\"display\":\"Emergency treatment management\""
                    + "}},"
                    + "{\"type\":{\"coding\":[{"
                        + "\"system\":\"http://terminology.kemkes.go.id\","
                        + "\"code\":\"primary-diagnosis\","
                        + "\"display\":\"Primary Diagnosis\""
                    + "}]},\"valueCoding\":{"
                        + "\"system\":\"http://hl7.org/fhir/sid/icd-10\","
                        + "\"code\":\"" + escape(selKdPenyakit) + "\","
                        + "\"display\":\"" + escape(selNmPenyakit) + "\""
                    + "}}"
                + "]"
            + "}";

        return postFhirRujukanIGD("Kirim Pra Permintaan IGD", "/Task", body);
    }

    private JsonNode cariKandidatIgdPostmanStyle(String qrCriteria, String qrArea) throws Exception {
        String now = ihsEncounter.getUtcDatetimeNow();
        String orgId = getOrgIdPerujukAman();
        String registrationId = buatRegistrationId("KAND");

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"resourceType\":\"Task\",")
          .append("\"contained\":[")
          .append(qrCriteria == null || qrCriteria.trim().equals("") ? buildQrCriteriaJson() : qrCriteria)
          .append(",")
          .append(qrArea == null || qrArea.trim().equals("") ? buildQrAreaJson() : qrArea)
          .append("],")
          .append("\"identifier\":[{")
              .append("\"system\":\"http://sys-ids.kemkes.go.id/task/").append(escape(orgId)).append("\",")
              .append("\"value\":\"").append(escape(registrationId)).append("\"")
          .append("}],")
          .append("\"status\":\"requested\",")
          .append("\"intent\":\"instance-order\",")
          .append("\"priority\":\"routine\",")
          .append("\"code\":{\"coding\":[{")
              .append("\"system\":\"http://terminology.kemkes.go.id\",")
              .append("\"code\":\"request-referral-candidate\",")
              .append("\"display\":\"Request for referral candidate\"")
          .append("}]},")
          .append("\"for\":{\"reference\":\"Patient/").append(escape(bersihkanReferenceId(selIdPasienSS, "Patient/"))).append("\"},")
          .append("\"authoredOn\":\"").append(escape(now)).append("\",")
          .append("\"lastModified\":\"").append(escape(now)).append("\",")
          .append("\"requester\":{\"reference\":\"Organization/").append(escape(orgId)).append("\"},")
          .append("\"owner\":{\"reference\":\"Organization/").append(escape(orgId)).append("\"},")
          .append("\"encounter\":{\"reference\":\"Encounter/").append(escape(bersihkanReferenceId(selIdEncounterSS, "Encounter/"))).append("\"},")
          .append("\"input\":[")
              .append("{\"type\":{\"coding\":[{")
                  .append("\"system\":\"http://terminology.kemkes.go.id\",")
                  .append("\"code\":\"referral-criteria\",")
                  .append("\"display\":\"Referral Criteria\"")
              .append("}]},\"valueReference\":{\"reference\":\"#qr-criteria\",\"display\":\"Referral Criteria Response\"}},")
              .append("{\"type\":{\"coding\":[{")
                  .append("\"system\":\"http://terminology.kemkes.go.id\",")
                  .append("\"code\":\"area\",")
                  .append("\"display\":\"Area\"")
              .append("}]},\"valueReference\":{\"reference\":\"#qr-area\",\"display\":\"Jejaring Wilayah Rujukan\"}},")
              .append("{\"type\":{\"coding\":[{")
                  .append("\"system\":\"http://snomed.info/sct\",")
                  .append("\"code\":\"119270007\",")
                  .append("\"display\":\"Management procedure\"")
              .append("}]},\"valueCoding\":{")
                  .append("\"system\":\"http://snomed.info/sct\",")
                  .append("\"code\":\"385868005\",")
                  .append("\"display\":\"Emergency treatment management\"}},")
              .append("{\"type\":{\"coding\":[{")
                  .append("\"system\":\"http://terminology.kemkes.go.id\",")
                  .append("\"code\":\"primary-diagnosis\",")
                  .append("\"display\":\"Primary Diagnosis\"")
              .append("}]},\"valueCoding\":{")
                  .append("\"system\":\"http://hl7.org/fhir/sid/icd-10\",")
                  .append("\"code\":\"").append(escape(selKdPenyakit)).append("\",")
                  .append("\"display\":\"").append(escape(selNmPenyakit)).append("\"}}");

        // Sekunder opsional. Form IGD belum punya field sekunder, jadi tidak dipaksa.
        sb.append("]}");

        return postFhirRujukanIGD("Cari Kandidat IGD", "/Task", sb.toString());
    }

    private JsonNode postFhirRujukanIGD(String judul, String pathFhir, String body) throws Exception {
        String url = ihsEncounter.getBaseUrl() + (pathFhir == null ? "" : pathFhir);
        RestTemplate rest = ihsEncounter.getRest();

        System.out.println("==== SatuSehatRujukan POST ====");
        System.out.println("URL : " + url);
        System.out.println("Body: " + body);

        try {
            HttpEntity<String> entity = new HttpEntity<>(body, ihsEncounter.buildAuthHeaders());
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("Status: " + resp.getStatusCode());
            System.out.println("Response: " + resp.getBody());
            catatResponTransaksi(judul, "POST", url, body, resp.getBody(), false);
            return ihsEncounter.getMapper().readTree(resp.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                ihsEncounter.forceRefreshToken();
                HttpEntity<String> entity = new HttpEntity<>(body, ihsEncounter.buildAuthHeaders());
                ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);
                System.out.println("Status retry: " + resp.getStatusCode());
                System.out.println("Response retry: " + resp.getBody());
                catatResponTransaksi(judul + " (retry token)", "POST", url, body, resp.getBody(), false);
                return ihsEncounter.getMapper().readTree(resp.getBody());
            }
            System.out.println("HTTP error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            catatResponTransaksi(judul, "POST", url, body, e.getResponseBodyAsString(), true);
            throw e;
        }
    }

    private String getOrgIdPerujukAman() {
        String orgId = safe(ihsEncounter.getOrgIdPerujuk());
        if (orgId.equals("")) {
            orgId = safe(koneksiDB.IDSATUSEHAT());
        }
        return orgId;
    }

    private String buatRegistrationId(String prefix) {
        String tgl = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        long rnd = Math.abs(System.currentTimeMillis() % 100000);
        return prefix + "-IGD-" + tgl + String.format("%05d", rnd);
    }

    private boolean isiKriteriaDariPraPermintaan(JsonNode resp) {
        boolean found = false;
        JsonNode contained = resp.path("contained");
        if (contained.isArray()) {
            for (JsonNode c : contained) {
                if (!"Questionnaire".equals(c.path("resourceType").asText())) {
                    continue;
                }
                String title = c.path("title").asText().toLowerCase();
                if (!title.contains("kriteria")) {
                    continue;
                }
                if (tambahItemKriteriaDariQuestionnaire(c.path("item"))) {
                    found = true;
                }
            }
        }
        return found;
    }

    private boolean tambahItemKriteriaDariQuestionnaire(JsonNode items) {
        boolean found = false;
        if (!items.isArray()) return false;
        for (JsonNode it : items) {
            JsonNode child = it.path("item");
            if (child.isArray() && child.size() > 0) {
                if (tambahItemKriteriaDariQuestionnaire(child)) found = true;
                continue;
            }
            String linkId = it.path("linkId").asText();
            String text = it.path("text").asText();
            String type = it.path("type").asText();
            if (!linkId.equals("") && !text.equals("")) {
                modelKriteria.addRow(new Object[]{linkId, text, type.equals("") ? "boolean" : type, defaultJawabanUntukTipe(type.equals("") ? "boolean" : type)});
                found = true;
            }
        }
        return found;
    }

    private void isiKriteriaIGDDefault() {
        modelKriteria.setRowCount(0);
        String[][] rows = {
            {"000001", "Mengancam nyawa, membahayakan diri dan orang lain/lingkungan", "boolean"},
            {"000002", "Adanya gangguan pada jalan nafas, pernafasan, dan sirkulasi", "boolean"},
            {"000003", "Adanya penurunan kesadaran", "boolean"},
            {"000004", "Adanya gangguan hemodinamik", "boolean"},
            {"000005", "Memerlukan tindakan segera", "boolean"}
        };
        for (String[] r : rows) {
            modelKriteria.addRow(new Object[]{r[0], r[1], r[2], "YA"});
        }
    }

    private String ambilExtension(JsonNode extensions, String targetUrl) {
        if (extensions == null || !extensions.isArray()) return "";
        for (JsonNode ex : extensions) {
            String url = ex.path("url").asText();
            if (targetUrl.equals(url)) {
                String v = ex.path("valueCode").asText();
                if (!v.equals("")) return v;
                v = ex.path("valueString").asText();
                if (!v.equals("")) return v;
                v = ex.path("valueQuantity").path("value").asText();
                if (!v.equals("")) {
                    String unit = ex.path("valueQuantity").path("unit").asText();
                    return unit.equals("") ? v : v + " " + unit;
                }
            }
            String nested = ambilExtension(ex.path("extension"), targetUrl);
            if (!nested.equals("")) return nested;
        }
        return "";
    }


    /**
     * Pastikan JTable workflow tetap memakai model yang benar walaupun .form diedit.
     * Ini penting agar response SATUSEHAT langsung muncul di tblKriteria, tblKandidat,
     * tblTugas, dan tblAcceptedFaskes.
     */
    private void pastikanModelTabelWorkflow() {
        try {
            if (tblKriteria != null && tblKriteria.getModel() != modelKriteria) {
                tblKriteria.setModel(modelKriteria);
            }
            if (tblKandidat != null && tblKandidat.getModel() != modelKandidat) {
                tblKandidat.setModel(modelKandidat);
            }
            if (tblTugas != null && tblTugas.getModel() != modelTugas) {
                tblTugas.setModel(modelTugas);
            }
            if (tblAcceptedFaskes != null && tblAcceptedFaskes.getModel() != modelAcceptedFaskes) {
                tblAcceptedFaskes.setModel(modelAcceptedFaskes);
            }
        } catch (Exception e) {
            System.out.println("Pastikan model tabel workflow gagal: " + e.getMessage());
        }
    }

    private String cariIdFaskesDariNamaKandidat(String namaFaskes) {
        if (namaFaskes == null) {
            return "";
        }
        String target = namaFaskes.trim();
        for (int i = 0; i < modelKandidat.getRowCount(); i++) {
            String nama = String.valueOf(modelKandidat.getValueAt(i, 3)).trim();
            if (target.equalsIgnoreCase(nama)) {
                return String.valueOf(modelKandidat.getValueAt(i, 1));
            }
        }
        return "";
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
                + "Mode normal: Bundle.tag referral-approval, Task.code referral-approval-request + CarePlan; ServiceRequest final dikirim setelah faskes ACCEPT",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // ==== Step A: POST CarePlan SEKALI (atau reuse kalau sudah ada) ====
        String idCarePlan = cariOrKirimCarePlan(centangedData.get(0)[0]);
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
        StringBuilder daftarTaskIdServer = new StringBuilder();
        for (String[] data : centangedData) {
            String idFaskesTujuan = data[0];
            String namaFaskes = data[1];

            try {
                JsonNode resp = svc.kirimTugasRujukanRefCarePlan(
                        selIdPasienSS, idFaskesTujuan, namaFaskes,
                        selIdConditionSS, idCarePlan,
                        selIdEncounterSS);
                catatResponTransaksi("Kirim Tugas Rujukan", "POST", "SatuSehatRujukanService.kirimTugasRujukanRefCarePlan",
                        "Faskes=" + idFaskesTujuan + " - " + namaFaskes + "\nCarePlan=" + idCarePlan + "\nCondition=" + selIdConditionSS + "\nBundleTag=referral-approval\nTaskCode=referral-approval-request",
                        jsonNodeToPretty(resp), false);

                // Cari Task ID SERVER dari response Bundle.
                // Jangan fallback ke resp.id karena itu ID Bundle, bukan ID Task.
                String taskKirimId = ambilTaskIdDariBundleResponse(resp);
                if (taskKirimId == null || taskKirimId.trim().equals("")) {
                    catatResponTransaksi("Task ID Tidak Terbaca", "POST", "SatuSehatRujukanService.kirimTugasRujukanRefCarePlan",
                            "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                            + "\nPERINGATAN: Task berhasil POST, tetapi ID Task server tidak ditemukan pada entry.response.location/resourceID."
                            + "\nJangan gunakan Bundle.id sebagai Task ID.",
                            jsonNodeToPretty(resp), true);
                    modelTugas.addRow(new Object[]{
                            "-", "-", namaFaskes, "UNKNOWN",
                            "Task ID tidak terbaca - buka tombol [?]",
                            new java.util.Date().toString()
                    });
                    gagal++;
                    continue;
                }

                // Patch v52: ServiceRequest TIDAK dikirim di tahap Kirim Tugas.
                // Sesuai alur SATUSEHAT, ServiceRequest final hanya dikirim setelah salah satu faskes ACCEPT.
                String serviceRequestId = "-";

                modelTugas.addRow(new Object[]{
                        taskKirimId, serviceRequestId, namaFaskes, "requested",
                        "menunggu",
                        new java.util.Date().toString()
                });
                simpanTaskTracking(taskKirimId, "approval", idFaskesTujuan, namaFaskes,
                                   "requested", null);
                if (daftarTaskIdServer.length() > 0) daftarTaskIdServer.append("\n");
                daftarTaskIdServer.append("- ").append(namaFaskes).append(" : ").append(taskKirimId);
                sukses++;

            } catch (Exception ex) {
                System.out.println("Gagal kirim ke " + namaFaskes + ": " + ex);
                String detail = ex.getMessage();
                if (ex instanceof HttpClientErrorException) {
                    try {
                        detail = ((HttpClientErrorException) ex).getResponseBodyAsString();
                    } catch (Exception ignore) {}
                }
                catatResponTransaksi("Kirim Tugas Rujukan", "POST", "SatuSehatRujukanService.kirimTugasRujukanRefCarePlan",
                        "Faskes=" + idFaskesTujuan + " - " + namaFaskes + "\nCondition=" + selIdConditionSS + "\nBundleTag=referral-approval\nTaskCode=referral-approval-request",
                        detail, true);
                modelTugas.addRow(new Object[]{
                        "-", "-", namaFaskes, "FAILED", "error: " + detail, new java.util.Date().toString()
                });
                gagal++;
            }
        }

        if (sukses > 0) {
            updateRujukanIGDSetelahKirimTugas();
        }

        setCursor(Cursor.getDefaultCursor());
        setStatusTab(lblTab3Status,
                "Selesai. Sukses: " + sukses + ", Gagal: " + gagal
                + ". Klik [Cek Status Semua] periodically untuk cek accept/reject.", false);
        setStatus("Tugas rujukan terkirim ke " + sukses + " fasyankes.", false);
        JOptionPane.showMessageDialog(this,
                "Tugas terkirim:\n- CarePlan ID: " + idCarePlan
                + "\n- ServiceRequest belum dikirim; tunggu faskes ACCEPT lalu kirim dari Tab 4"
                + "\n- Sukses: " + sukses + "\n- Gagal: " + gagal
                + (daftarTaskIdServer.length() > 0 ? "\n\nTask ID SERVER yang harus dicari RS tujuan:\n" + daftarTaskIdServer.toString() : "")
                + "\n\nKunci pencarian untuk RS tujuan:\nowner=" + new ApiSatuSehat().getOrgIdPerujuk() + " Patient/" + selIdPasienSS + " task=<Task ID SERVER di atas>"
                + "\n\nCatatan: jangan gunakan Bundle.id sebagai Task ID. Pakai Task ID SERVER di atas / entry.response.resourceID."
                + "\nKlik [Cek Status Semua] secara berkala untuk cek respon fasyankes tujuan.");
    }


    /**
     * Mode kompatibel RS lain: kirim Bundle transaction dengan Task.code=referral-approval.
     *
     * Catatan:
     * - Tombol utama [Kirim Tugas] tetap memakai alur resmi terbaru (referral-approval-request).
     * - Tombol ini disediakan untuk RS/vendor lain yang inbox rujukan masuknya masih membaca
     *   Task.code=referral-approval.
     * - ServiceRequest final tetap dikirim dari Tab 4 setelah fasyankes tujuan ACCEPT.
     */
    private void doKirimTugasKompatibelTeman() {
        if (!validatePasienTerpilih()) return;
        if (selIdConditionSS == null || selIdConditionSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Klik [Buat Condition] dulu di panel atas — diperlukan untuk reasonReference Task.");
            return;
        }

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
                    "Centang 1 kandidat di Tab 2 dulu. Mode kompatibel tetap membutuhkan fasyankes tujuan.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Kirim tugas rujukan MODE KOMPATIBEL ke " + centangedData.size() + " fasyankes?\n\n"
                + "Mode ini memakai Task.code = referral-approval agar dapat dibaca RS/vendor lain "
                + "yang belum membaca referral-approval-request.\n"
                + "ServiceRequest final tetap dikirim setelah fasyankes tujuan ACCEPT.",
                "Konfirmasi Mode Kompatibel RS Lain", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int sukses = 0, gagal = 0;
        StringBuilder daftarTaskIdServer = new StringBuilder();
        for (String[] data : centangedData) {
            String idFaskesTujuan = data[0];
            String namaFaskes = data[1];

            try {
                String carePlanJson = svc.buildCarePlanJson(
                        selIdDokterSS,
                        idFaskesTujuan,
                        selIdConditionSS,
                        SatuSehatRujukanService.SNOMED_IGD,
                        "",
                        "",
                        selIdPasienSS,
                        selIdEncounterSS,
                        "Rencana Rujukan IGD",
                        "Rujukan IGD/Rawat Darurat ke " + namaFaskes
                );

                JsonNode resp = svc.kirimTugasRujukanKompatibelTeman(
                        selIdPasienSS, idFaskesTujuan, namaFaskes,
                        selIdConditionSS, carePlanJson, selIdEncounterSS);

                catatResponTransaksi("Kirim Tugas Rujukan Kompatibel RS Lain", "POST",
                        "SatuSehatRujukanService.kirimTugasRujukanKompatibelTeman",
                        "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                        + "\nCondition=" + selIdConditionSS
                        + "\nTaskCode=referral-approval"
                        + "\nMode=Kompatibel RS lain",
                        jsonNodeToPretty(resp), false);

                String taskKirimId = ambilTaskIdDariBundleResponse(resp);
                if (taskKirimId == null || taskKirimId.trim().equals("")) {
                    catatResponTransaksi("Task ID Kompatibel Tidak Terbaca", "POST",
                            "SatuSehatRujukanService.kirimTugasRujukanKompatibelTeman",
                            "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                            + "\nPERINGATAN: Task berhasil POST, tetapi ID Task server tidak ditemukan pada entry.response.location/resourceID."
                            + "\nJangan gunakan Bundle.id sebagai Task ID.",
                            jsonNodeToPretty(resp), true);
                    modelTugas.addRow(new Object[]{
                            "-", "-", namaFaskes, "UNKNOWN",
                            "Task ID kompatibel tidak terbaca - buka tombol [?]",
                            new java.util.Date().toString()
                    });
                    gagal++;
                    continue;
                }

                modelTugas.addRow(new Object[]{
                        taskKirimId, "-", namaFaskes, "requested",
                        "menunggu (kompatibel)",
                        new java.util.Date().toString()
                });
                simpanTaskTracking(taskKirimId, "approval-kompatibel", idFaskesTujuan, namaFaskes,
                                   "requested", null);
                if (daftarTaskIdServer.length() > 0) daftarTaskIdServer.append("\n");
                daftarTaskIdServer.append("- ").append(namaFaskes).append(" : ").append(taskKirimId);
                sukses++;

            } catch (Exception ex) {
                System.out.println("Gagal kirim kompatibel ke " + namaFaskes + ": " + ex);
                String detail = ex.getMessage();
                if (ex instanceof HttpClientErrorException) {
                    try {
                        detail = ((HttpClientErrorException) ex).getResponseBodyAsString();
                    } catch (Exception ignore) {}
                }
                catatResponTransaksi("Kirim Tugas Rujukan Kompatibel RS Lain", "POST",
                        "SatuSehatRujukanService.kirimTugasRujukanKompatibelTeman",
                        "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                        + "\nCondition=" + selIdConditionSS
                        + "\nTaskCode=referral-approval"
                        + "\nMode=Kompatibel RS lain",
                        detail, true);
                modelTugas.addRow(new Object[]{
                        "-", "-", namaFaskes, "FAILED", "error kompatibel: " + detail, new java.util.Date().toString()
                });
                gagal++;
            }
        }

        if (sukses > 0) {
            updateRujukanIGDSetelahKirimTugas();
        }

        setCursor(Cursor.getDefaultCursor());
        setStatusTab(lblTab3Status,
                "Mode kompatibel selesai. Sukses: " + sukses + ", Gagal: " + gagal
                + ". Klik [Cek Status Semua] untuk cek accept/reject.", false);
        setStatus("Tugas rujukan kompatibel terkirim ke " + sukses + " fasyankes.", false);
        JOptionPane.showMessageDialog(this,
                "Tugas kompatibel terkirim:\n"
                + "- Task.code: referral-approval\n"
                + "- ServiceRequest belum dikirim; tunggu fasyankes ACCEPT lalu kirim dari Tab 4\n"
                + "- Sukses: " + sukses + "\n- Gagal: " + gagal
                + (daftarTaskIdServer.length() > 0 ? "\n\nTask ID SERVER yang harus dicari RS tujuan:\n" + daftarTaskIdServer.toString() : "")
                + "\n\nKunci pencarian untuk RS tujuan:\nowner=" + new ApiSatuSehat().getOrgIdPerujuk() + " Patient/" + selIdPasienSS + " task=<Task ID SERVER di atas>"
                + "\n\nCatatan: mode ini untuk kompatibilitas RS/vendor lain. Alur final tetap sama: ACCEPT Task → Kirim Final ServiceRequest.");
    }

    private String ambilTaskIdDariBundleResponse(JsonNode resp) {
        return cariResourceIdDariBundleResponse(resp, "Task");
    }

    /**
     * Cek apakah CarePlan rujukan sudah ada untuk no_rujukan ini.
     * Kalau ada → return id-nya. Kalau belum → POST baru ke Satu Sehat.
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
            try {
                JsonNode cekCarePlan = svc.getCarePlanById(existingId);
                String cekId = safe(cekCarePlan.path("id").asText());
                if (!cekId.isEmpty()) {
                    System.out.println("Re-use CarePlan id valid di SATUSEHAT STG: " + existingId);
                    catatResponTransaksi("Cek CarePlan Existing", "GET", "/CarePlan/" + existingId,
                            "CarePlan dari DB masih valid, dipakai ulang.",
                            jsonNodeToPretty(cekCarePlan), false);
                    return existingId;
                }
            } catch (Exception ex) {
                System.out.println("CarePlan DB tidak valid/tidak ditemukan di SATUSEHAT STG, akan buat ulang: " + existingId + " -> " + ex);
                catatResponTransaksi("Cek CarePlan Existing", "GET", "/CarePlan/" + existingId,
                        "CarePlan dari DB tidak valid/tidak ditemukan di STG. Akan buat ulang.",
                        ex.toString(), true);
                try (PreparedStatement psDel = koneksi.prepareStatement(
                        "delete from bridging_careplan_rujukan where no_rujukan=?")) {
                    psDel.setString(1, noRujukanInternal);
                    psDel.executeUpdate();
                } catch (Exception exDel) {
                    System.out.println("Hapus CarePlan invalid dari DB gagal: " + exDel);
                }
            }
        }

        // 2. POST baru ke Satu Sehat
        try {
            String namaDokter = Sequel.cariIsi(
                    "select nama from pegawai where nik=?", selKdDokter);
            String tglDibuat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date());
            String desc = "Rujukan IGD/Rawat Darurat untuk pasien " + selNamaPasien
                    + " dengan diagnosa " + selKdPenyakit + " - " + selNmPenyakit;

            JsonNode resp = svc.kirimCarePlanRujukan(
                    selNoRawat,
                    selIdPasienSS, selNamaPasien,
                    selIdEncounterSS,
                    selIdDokterSS, namaDokter,
                    idFaskesTujuanContributor,
                    selIdConditionSS,
                    SatuSehatRujukanService.SNOMED_IGD,
                    desc, tglDibuat,
                    "", ""
            );
            catatResponTransaksi("Kirim CarePlan", "POST", "SatuSehatRujukanService.kirimCarePlanRujukan",
                    "FaskesContributor=" + idFaskesTujuanContributor + "\nCondition=" + selIdConditionSS + "\nEncounter=" + selIdEncounterSS,
                    jsonNodeToPretty(resp), false);

            String id = svc.getCarePlanId(resp);
            if (id == null || id.isEmpty()) {
                System.out.println("CarePlan response tidak ada id: " + resp);
                return null;
            }

            // Simpan ke DB - tipe_rujukan='igd'
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into bridging_careplan_rujukan "
                    + "(no_rujukan, id_careplan, tipe_rujukan, kdppk_tujuan, "
                    + " id_condition_diagnosa, description) "
                    + "values (?,?,'igd',?,?,?)")) {
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
            catatResponTransaksi("Cek Status Task", "GET", "SatuSehatRujukanService.cekStatusAccept",
                    "Task ID=" + taskId, jsonNodeToPretty(resp), false);
            Boolean accept = svc.cekResponAccept(resp);
            String stt = resp.path("status").asText();
            modelTugas.setValueAt(stt, row, 3);

            String responText;
            if (accept == null) responText = "menunggu";
            else if (accept) responText = "ACCEPTED";
            else responText = "REJECTED";
            modelTugas.setValueAt(responText, row, 4);

            // Update DB
            updateTaskTrackingRespon(taskId, stt, accept);
        } catch (Exception ex) {
            modelTugas.setValueAt("error", row, 4);
            System.out.println("cekStatusOneRow err: " + ex);
        }
    }

    private void refreshAcceptedFaskesTab4() {
        modelAcceptedFaskes.setRowCount(0);
        for (int i = 0; i < modelTugas.getRowCount(); i++) {
            if ("ACCEPTED".equals(String.valueOf(modelTugas.getValueAt(i, 4)))) {
                String taskId = String.valueOf(modelTugas.getValueAt(i, 0));
                String namaFaskes = String.valueOf(modelTugas.getValueAt(i, 2));
                String idFaskesSS = Sequel.cariIsi(
                        "select kdppk_kandidat from bridging_task_rujukan_satusehat where task_id=?",
                        taskId);
                if (idFaskesSS == null || idFaskesSS.trim().equals("")) {
                    idFaskesSS = cariIdFaskesDariNamaKandidat(namaFaskes);
                }
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

    private void setDefaultAppointmentTimes() {
        try {
            java.util.Calendar mulai = java.util.Calendar.getInstance();
            mulai.add(java.util.Calendar.HOUR_OF_DAY, 1);
            java.util.Calendar selesai = (java.util.Calendar) mulai.clone();
            selesai.add(java.util.Calendar.HOUR_OF_DAY, 5);
            if (tAppointmentMulai != null && safe(tAppointmentMulai.getText()).isEmpty()) {
                tAppointmentMulai.setText(formatWibDateTime(mulai));
            }
            if (tAppointmentSelesai != null && safe(tAppointmentSelesai.getText()).isEmpty()) {
                tAppointmentSelesai.setText(formatWibDateTime(selesai));
            }
        } catch (Exception e) {
            System.out.println("Set default appointment gagal: " + e.getMessage());
        }
    }

    private String formatWibDateTime(java.util.Calendar cal) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(cal.getTime()) + "+07:00";
    }

    private int getSelectedAcceptedRow(boolean autoSelectSingle) {
        int selected = -1;
        for (int i = 0; i < modelAcceptedFaskes.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelAcceptedFaskes.getValueAt(i, 0))) {
                selected = i;
                break;
            }
        }
        if (selected < 0 && autoSelectSingle && modelAcceptedFaskes.getRowCount() == 1) {
            modelAcceptedFaskes.setValueAt(Boolean.TRUE, 0, 0);
            selected = 0;
        }
        return selected;
    }

    private String cariResourceIdDariBundleResponse(JsonNode root, String resourceType) {
        if (root == null) return "";
        String target = resourceType == null ? "" : resourceType.trim();
        if (target.equals("")) return "";

        JsonNode entries = root.path("entry");
        if (!entries.isArray()) return "";

        for (JsonNode entry : entries) {
            JsonNode response = entry.path("response");
            String location = safe(response.path("location").asText());
            String fromLocation = extractResourceIdDariLocation(location, target);
            if (!fromLocation.equals("")) {
                return fromLocation;
            }

            String rt = safe(response.path("resourceType").asText());
            String resourceID = safe(response.path("resourceID").asText());
            if (target.equalsIgnoreCase(rt) && !resourceID.equals("")) {
                return resourceID;
            }
        }
        return "";
    }

    private String extractResourceIdDariLocation(String location, String resourceType) {
        String loc = safe(location);
        String rt = safe(resourceType);
        if (loc.equals("") || rt.equals("")) return "";

        // Format umum FHIR transaction response:
        // Task/{id}/_history/{vid}
        // https://.../fhir-r4/v1/Task/{id}/_history/{vid}
        String marker = rt + "/";
        int pos = loc.indexOf(marker);
        if (pos < 0) {
            marker = "/" + rt + "/";
            pos = loc.indexOf(marker);
            if (pos >= 0) pos = pos + 1;
        }
        if (pos < 0) return "";

        String s = loc.substring(pos + rt.length() + 1);
        int slash = s.indexOf('/');
        String id = slash >= 0 ? s.substring(0, slash) : s;
        return safe(id);
    }

    /**
     * Buat ServiceRequest pendamping segera setelah Task terkirim agar RS tujuan yang
     * membuka menu ServiceRequest Masuk bisa melihat ID rujukan tanpa menunggu tombol Final.
     * Jika gagal, Task tetap dianggap terkirim; detail error dicatat di dialog response.
     */
    private String kirimServiceRequestPendamping(String idFaskesTujuan, String namaFaskes,
                                                 String idCarePlan, String taskKirimId,
                                                 String judulTransaksi) {
        if (taskKirimId == null || taskKirimId.trim().equals("") || "-".equals(taskKirimId)) {
            return "";
        }
        try {
            JsonNode srResp = svc.kirimRujukanFinalDenganTask(
                    idFaskesTujuan,
                    safe(idCarePlan),
                    taskKirimId,
                    "",
                    selIdConditionSS,
                    "",
                    SatuSehatRujukanService.SNOMED_IGD,
                    "", "", "", "", "", "",
                    selIdPasienSS,
                    selIdEncounterSS);
            String srId = safe(srResp.path("id").asText());
            String noNasional = safe(svc.getNomorRujukanNasional(srResp));
            catatResponTransaksi(judulTransaksi, "POST", "/ServiceRequest",
                    "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                    + "\nCarePlan=" + safe(idCarePlan)
                    + "\nTask=" + taskKirimId
                    + "\nCondition=" + selIdConditionSS
                    + "\nServiceRequest ID=" + srId
                    + (noNasional.equals("") ? "" : "\nNomor Rujukan Nasional=" + noNasional),
                    jsonNodeToPretty(srResp), false);
            return srId;
        } catch (Exception ex) {
            String detail = detailHttpError(ex);
            catatResponTransaksi(judulTransaksi, "POST", "/ServiceRequest",
                    "Faskes=" + idFaskesTujuan + " - " + namaFaskes
                    + "\nCarePlan=" + safe(idCarePlan)
                    + "\nTask=" + taskKirimId
                    + "\nCondition=" + selIdConditionSS,
                    detail, true);
            return "SR ERROR";
        }
    }

    private String detailHttpError(Exception ex) {
        String detail = ex == null ? "" : ex.getMessage();
        if (ex instanceof HttpClientErrorException) {
            try {
                detail = ((HttpClientErrorException) ex).getResponseBodyAsString();
            } catch (Exception ignore) {}
        }
        if (detail == null || detail.trim().equals("")) {
            detail = String.valueOf(ex);
        }
        return detail;
    }

    private void doKirimAppointment() {
        if (!validatePasienTerpilih()) return;
        syncFieldState();

        int rowChosen = getSelectedAcceptedRow(true);
        if (rowChosen < 0) {
            JOptionPane.showMessageDialog(this,
                    "Belum ada fasyankes ACCEPT. Klik [Cek Status Semua] dulu, lalu centang fasyankes accepted.");
            return;
        }

        String taskId = safe(String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 1)));
        String namaFaskes = safe(String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 3)));
        if (taskId.isEmpty() || "-".equals(taskId)) {
            JOptionPane.showMessageDialog(this, "Task ID accepted belum ada.");
            return;
        }

        String mulai = safe(tAppointmentMulai.getText());
        String selesai = safe(tAppointmentSelesai.getText());
        if (mulai.isEmpty() || selesai.isEmpty()) {
            setDefaultAppointmentTimes();
            mulai = safe(tAppointmentMulai.getText());
            selesai = safe(tAppointmentSelesai.getText());
        }

        String idPractitionerTujuan = JOptionPane.showInputDialog(this,
                "Masukkan IHS Practitioner tujuan untuk Appointment:\n"
                + "(default memakai IHS dokter dummy/DPJP jika belum ada mapping tujuan)",
                selIdDokterSS);
        if (idPractitionerTujuan == null) return;
        idPractitionerTujuan = safe(idPractitionerTujuan);
        if (idPractitionerTujuan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "IHS Practitioner tujuan wajib diisi untuk Appointment.");
            return;
        }
        String namaPractitionerTujuan = JOptionPane.showInputDialog(this,
                "Nama Practitioner tujuan:",
                GUNAKAN_DUMMY_RUJUKAN_IGD ? dummyData.namaDokter : "Dokter Tujuan");
        if (namaPractitionerTujuan == null) return;
        namaPractitionerTujuan = safe(namaPractitionerTujuan);

        int conf = JOptionPane.showConfirmDialog(this,
                "Kirim Appointment ke SATUSEHAT untuk fasyankes:\n" + namaFaskes
                + "\n\nMulai  : " + mulai
                + "\nSelesai: " + selesai
                + "\nTask   : " + taskId,
                "Konfirmasi Appointment", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatusTab(lblTab4Status, "Mengirim Appointment...", false);
        try {
            JsonNode cekTask = svc.cekStatusAccept(taskId);
            Boolean accept = svc.cekResponAccept(cekTask);
            catatResponTransaksi("Cek Status Sebelum Appointment", "GET", "/Task/" + taskId,
                    "Task ID=" + taskId, jsonNodeToPretty(cekTask), false);
            if (!Boolean.TRUE.equals(accept)) {
                JOptionPane.showMessageDialog(this,
                        "Task belum accepted oleh faskes tujuan. Appointment tidak dikirim.\n"
                        + "Klik [Cek Status Semua] atau tunggu faskes tujuan menerima.");
                setStatusTab(lblTab4Status, "Task belum accepted, Appointment dibatalkan.", true);
                return;
            }

            JsonNode resp = svc.kirimAppointmentRujukan(
                    selIdPasienSS, selNamaPasien,
                    idPractitionerTujuan, namaPractitionerTujuan,
                    taskId, SatuSehatRujukanService.SNOMED_IGD,
                    mulai, selesai);
            appointmentId = cariResourceIdDariBundleResponse(resp, "Appointment");
            if (appointmentId == null || appointmentId.isEmpty()) {
                appointmentId = resp.path("id").asText();
            }
            catatResponTransaksi("Kirim Appointment", "POST", "",
                    "Task=" + taskId + "\nPractitioner=" + idPractitionerTujuan
                    + "\nMulai=" + mulai + "\nSelesai=" + selesai,
                    jsonNodeToPretty(resp), false);
            setStatusTab(lblTab4Status, "Appointment terkirim. ID: " + appointmentId, false);
            setStatus("Appointment terkirim: " + appointmentId, false);
            JOptionPane.showMessageDialog(this, "Appointment terkirim.\nID: " + appointmentId);
        } catch (Exception ex) {
            handleApiError(ex);
            setStatusTab(lblTab4Status, "Gagal kirim Appointment: " + ex.getMessage(), true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  TAB 4 ACTION
    // =================================================================
    private void doKirimFinal() {
        if (!validatePasienTerpilih()) return;
        syncFieldState();

        // Cari faskes tercentang
        int rowChosen = getSelectedAcceptedRow(true);
        if (rowChosen < 0) {
            JOptionPane.showMessageDialog(this, "Centang 1 fasyankes tujuan FINAL.");
            return;
        }

        String taskAcceptedId = safe(String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 1)));
        String idFaskesTujuan = String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 2));
        String namaFaskes = String.valueOf(modelAcceptedFaskes.getValueAt(rowChosen, 3));

        int idxTr = cbJenisTransport.getSelectedIndex();
        // Patch v52: ServiceRequest.locationCode untuk sarana transportasi rujuk memakai RoleCode AMB.
        // Detail jenis kendaraan lokal tetap bisa dicatat di noRegAmbulans/catatan, tetapi tidak dikirim sebagai SNOMED locationCode.
        String kdTransport = (idxTr > 0) ? "AMB" : "";

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

            if (taskAcceptedId.isEmpty() || "-".equals(taskAcceptedId)) {
                JOptionPane.showMessageDialog(this, "Task ID accepted belum ada. Klik [Cek Status Semua] dulu.");
                return;
            }

            JsonNode cekTask = svc.cekStatusAccept(taskAcceptedId);
            Boolean accepted = svc.cekResponAccept(cekTask);
            catatResponTransaksi("Cek Status Sebelum Final", "GET", "/Task/" + taskAcceptedId,
                    "Task ID=" + taskAcceptedId, jsonNodeToPretty(cekTask), false);
            if (!Boolean.TRUE.equals(accepted)) {
                JOptionPane.showMessageDialog(this,
                        "Task faskes tujuan belum ACCEPTED, final belum boleh dikirim.\n"
                        + "Klik [Cek Status Semua] atau tunggu faskes tujuan menerima.");
                setStatusTab(lblTab4Status, "Task belum accepted, final dibatalkan.", true);
                return;
            }

            JsonNode resp = svc.kirimRujukanFinalDenganTask(
                    idFaskesTujuan,
                    idCarePlan,
                    taskAcceptedId,
                    appointmentId,
                    selIdConditionSS,
                    "",  // idConditionKriteriaKlinis
                    SatuSehatRujukanService.SNOMED_IGD,
                    tNoRujukanPCare.getText().trim(),
                    tNoKartuAsuransi.getText().trim(),
                    "bpjs-kesehatan",
                    "HOSP",  // locationCode jenis faskes tujuan: Hospital
                    kdTransport,
                    tNoRegAmbulans.getText().trim(),
                    selIdPasienSS,
                    selIdEncounterSS
            );
            catatResponTransaksi("Kirim Rujukan Final", "POST", "/ServiceRequest",
                    "Faskes=" + idFaskesTujuan + "\nCarePlan=" + idCarePlan + "\nTask=" + taskAcceptedId
                    + "\nAppointment=" + appointmentId + "\nCondition=" + selIdConditionSS + "\nTransport=" + kdTransport,
                    jsonNodeToPretty(resp), false);

            String srId = resp.path("id").asText();
            serviceRequestId = srId;
            String noRujNas = svc.getNomorRujukanNasional(resp);

            if (noRujNas == null || noRujNas.isEmpty()) {
                noRujNas = srId;  // fallback pakai SR id
            }
            lblNoRujukanNasional.setText(noRujNas);
            simpanRujukanIGDFinal(srId, noRujNas, idFaskesTujuan, namaFaskes);
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
        if (modelKriteria.getRowCount() == 0) {
            isiKriteriaIGDDefault();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"resourceType\":\"QuestionnaireResponse\",")
          .append("\"id\":\"qr-criteria\",")
          .append("\"questionnaire\":\"https://fhir.kemkes.go.id/Questionnaire/Q100\",")
          .append("\"status\":\"completed\",")
          .append("\"subject\":{\"reference\":\"Patient/").append(escape(bersihkanReferenceId(selIdPasienSS, "Patient/"))).append("\"},")
          .append("\"encounter\":{\"reference\":\"Encounter/").append(escape(bersihkanReferenceId(selIdEncounterSS, "Encounter/"))).append("\"},")
          .append("\"item\":[{")
          .append("\"linkId\":\"0\",")
          .append("\"text\":\"GAWAT DARURAT\",")
          .append("\"item\":[");

        boolean first = true;
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            String linkId = String.valueOf(modelKriteria.getValueAt(i, 0));
            String text = String.valueOf(modelKriteria.getValueAt(i, 1));
            String type = String.valueOf(modelKriteria.getValueAt(i, 2));
            String jwb = String.valueOf(modelKriteria.getValueAt(i, 3)).trim();
            if (linkId == null || linkId.trim().equals("")) continue;
            if (text == null || text.trim().equals("")) text = linkId;

            if (!first) sb.append(",");
            sb.append("{\"linkId\":\"").append(escape(linkId)).append("\",")
              .append("\"text\":\"").append(escape(text)).append("\",")
              .append("\"answer\":[{");
            if ("boolean".equalsIgnoreCase(type) || type == null || type.trim().equals("")) {
                String bool = normalisasiBooleanKriteria(jwb);
                if (bool == null) bool = "false";
                sb.append("\"valueBoolean\":").append(bool);
            } else {
                sb.append("\"valueString\":\"").append(escape(jwb)).append("\"");
            }
            sb.append("}]}");
            first = false;
        }

        sb.append("]}]}");
        return sb.toString();
    }

    private String buildQrAreaJson() {
        String kdProv = getKodeProvinsiTerpilih();
        String nmProv = getNamaProvinsiTerpilih();
        String kdKab = getKodeKabupatenTerpilih();
        String nmKab = getNamaKabupatenTerpilih();

        return "{\"resourceType\":\"QuestionnaireResponse\","
                + "\"id\":\"qr-area\","
                + "\"questionnaire\":\"https://fhir.kemkes.go.id/Questionnaire/Q101\","
                + "\"status\":\"completed\","
                + "\"subject\":{\"reference\":\"Patient/" + escape(bersihkanReferenceId(selIdPasienSS, "Patient/")) + "\"},"
                + "\"encounter\":{\"reference\":\"Encounter/" + escape(bersihkanReferenceId(selIdEncounterSS, "Encounter/")) + "\"},"
                + "\"item\":[{"
                    + "\"linkId\":\"1\","
                    + "\"text\":\"Jejaring wilayah rujukan\","
                    + "\"item\":["
                        + "{\"linkId\":\"1.1\",\"text\":\"Provinsi\",\"answer\":[{\"valueCoding\":{"
                            + "\"system\":\"http://sys-ids.kemkes.go.id/administrative-area\","
                            + "\"code\":\"" + escape(kdProv) + "\","
                            + "\"display\":\"" + escape(nmProv) + "\""
                        + "}}]},"
                        + "{\"linkId\":\"1.2\",\"text\":\"Kabupaten/Kota\",\"answer\":[{\"valueCoding\":{"
                            + "\"system\":\"http://sys-ids.kemkes.go.id/administrative-area\","
                            + "\"code\":\"" + escape(kdKab) + "\","
                            + "\"display\":\"" + escape(nmKab) + "\""
                        + "}}]}"
                    + "]}]}";
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
        syncFieldState();
        if (selIdEncounterSS.isEmpty() || selIdPasienSS.isEmpty() || selIdDokterSS.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    GUNAKAN_DUMMY_RUJUKAN_IGD
                    ? "Encounter STG / IHS Pasien / IHS Dokter belum lengkap."
                            + "\n\nMode dummy tidak membuat Encounter otomatis."
                            + "\nIsi/paste Encounter STG yang valid di panel pasien, lalu lanjutkan proses."
                    : "Encounter / IHS Pasien / IHS Dokter belum lengkap. Lihat panel Pasien Terpilih.");
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
    private void simpanRujukanIGDDraft() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into bridging_rujukan_satusehat_igd "
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
            ps.setString(10, SatuSehatRujukanService.SNOMED_IGD);
            ps.setString(11, user);
            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("simpanRujukanIGDDraft err: " + ex);
        }
    }

    private void updateTaskCariId() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_satusehat_igd set task_cari_id=?, "
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
                + "values (?,?,'igd',?,?,?,?,?) "
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


    private void updateRujukanIGDSetelahKirimTugas() {
        if (safe(noRujukanInternal).equals("")) {
            noRujukanInternal = "RJK-IGD-" + System.currentTimeMillis();
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_satusehat_igd set step_terakhir=3, "
                + "status_rujukan='task-sent' where no_rujukan=?")) {
            ps.setString(1, noRujukanInternal);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                // Guard untuk kasus data lama/ujicoba: pastikan header rujukan tetap ada.
                simpanRujukanIGDDraft();
                try (PreparedStatement ps2 = koneksi.prepareStatement(
                        "update bridging_rujukan_satusehat_igd set step_terakhir=3, "
                        + "status_rujukan='task-sent' where no_rujukan=?")) {
                    ps2.setString(1, noRujukanInternal);
                    ps2.executeUpdate();
                }
            }
        } catch (Exception ex) {
            System.out.println("updateRujukanIGDSetelahKirimTugas err: " + ex);
        }
    }

    private void simpanRujukanIGDFinal(String srId, String noRujNas,
                                          String kdppkTujuan, String nmppkTujuan) {
        // Ambil careplan_id dari bridging_careplan_rujukan
        String idCarePlan = Sequel.cariIsi(
                "select id_careplan from bridging_careplan_rujukan where no_rujukan=?",
                noRujukanInternal);
        if (idCarePlan == null) idCarePlan = "";

        try (PreparedStatement ps = koneksi.prepareStatement(
                "update bridging_rujukan_satusehat_igd set "
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
            System.out.println("simpanRujukanIGDFinal err: " + ex);
        }
    }


    // =================================================================
    //  DIALOG RIWAYAT RESPONSE - gaya Postman
    // =================================================================
    private static class ResponTransaksi {
        int no;
        String waktu;
        String judul;
        String method;
        String url;
        String request;
        String response;
        boolean error;

        @Override
        public String toString() {
            String status = error ? "ERROR" : "OK";
            return String.format("#%02d  %s  %s", no, status, judul);
        }
    }

    private void catatResponTransaksi(String judul, String method, String url,
                                      String request, String response, boolean error) {
        try {
            ResponTransaksi item = new ResponTransaksi();
            item.no = ++urutResponTransaksi;
            item.waktu = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            item.judul = safe(judul);
            item.method = safe(method);
            item.url = normalisasiUrlResponseTransaksi(method, url);
            item.request = request == null ? "" : request;
            item.response = response == null ? "" : response;
            item.error = error;
            riwayatResponTransaksi.add(item);
            while (riwayatResponTransaksi.size() > 200) {
                riwayatResponTransaksi.remove(0);
            }
        } catch (Exception e) {
            System.out.println("Gagal catat respon transaksi: " + e.getMessage());
        }
    }

    private String normalisasiUrlResponseTransaksi(String method, String url) {
        String u = safe(url);
        if (u.equals("")) {
            return "";
        }
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return u;
        }

        String base = "";
        try {
            base = safe(ihsEncounter.getBaseUrl());
        } catch (Exception ignore) {}

        // Service lama mencatat nama method, bukan URL. Supaya saat dikirim ke SATUSEHAT
        // URL yang tampil sama seperti console/Postman.
        if (!base.equals("")) {
            if (u.contains("kirimTugasRujukanRefCarePlan") || u.contains("kirimTugasRujukanKompatibelTeman")) {
                return base; // Bundle transaction dikirim ke base FHIR, tanpa path tambahan
            }
            if (u.contains("kirimCarePlanRujukan")) {
                return base + "/CarePlan";
            }
            if (u.contains("kirimCondition")) {
                return base + "/Condition";
            }
            if (u.contains("kirimRujukanFinal")) {
                return base + "/ServiceRequest";
            }
            if (u.contains("cekStatusAccept")) {
                return base + "/Task/{taskId}";
            }
            if (u.startsWith("GET Task/")) {
                return base + "/Task/" + u.substring("GET Task/".length()).trim();
            }
            if (u.startsWith("Task ") || u.contains("referral-pre-request") || u.contains("request-referral-candidate")) {
                return base + "/Task";
            }
            if (u.startsWith("/")) {
                return base + u;
            }
        }
        return u;
    }

    private void tampilDialogResponTransaksi() {
        final javax.swing.JDialog dlg = new javax.swing.JDialog(this,
                "Response BPJS / SATUSEHAT", true);
        dlg.setLayout(new java.awt.BorderLayout(0, 0));

        javax.swing.DefaultListModel<ResponTransaksi> listModel = new javax.swing.DefaultListModel<>();
        for (ResponTransaksi item : riwayatResponTransaksi) {
            listModel.addElement(item);
        }

        javax.swing.JList<ResponTransaksi> list = new javax.swing.JList<>(listModel);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        list.setFixedCellHeight(42);
        list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel lbl = (javax.swing.JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ResponTransaksi) {
                    ResponTransaksi item = (ResponTransaksi) value;
                    String badge = item.error ? "ERROR" : "OK";
                    lbl.setText("<html><div style='font-family:Segoe UI;font-size:11px;'><b>#"
                            + item.no + " · " + badge + "</b><br>" + escapeHtml(item.judul) + "</div></html>");
                    lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8));
                    if (!isSelected) {
                        lbl.setBackground(item.error ? new java.awt.Color(255, 244, 244) : new java.awt.Color(246, 252, 248));
                        lbl.setForeground(new java.awt.Color(45, 45, 45));
                    }
                }
                return lbl;
            }
        });

        javax.swing.JTextPane area = new javax.swing.JTextPane();
        area.setEditable(false);
        area.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        area.setBackground(new java.awt.Color(255, 255, 255));
        area.setForeground(new java.awt.Color(45, 45, 45));
        area.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));

        javax.swing.JLabel lblHeader = new javax.swing.JLabel("Pilih transaksi di sisi kiri");
        lblHeader.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));

        javax.swing.JLabel tabRaw = new javax.swing.JLabel("raw (json)");
        tabRaw.setOpaque(true);
        tabRaw.setBackground(new java.awt.Color(255, 255, 255));
        tabRaw.setForeground(new java.awt.Color(40, 40, 40));
        tabRaw.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)),
                javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        tabRaw.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));

        javax.swing.JLabel badgeJson = new javax.swing.JLabel("json");
        badgeJson.setOpaque(true);
        badgeJson.setBackground(new java.awt.Color(245, 245, 245));
        badgeJson.setForeground(new java.awt.Color(90, 90, 90));
        badgeJson.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)),
                javax.swing.BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        badgeJson.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));

        final String[] teksAktif = new String[]{""};

        widget.Button btnCopyAtas = new widget.Button();
        btnCopyAtas.setText("Copy");
        btnCopyAtas.setToolTipText("Copy response yang sedang tampil");
        btnCopyAtas.setPreferredSize(new java.awt.Dimension(84, 30));
        btnCopyAtas.addActionListener(e -> copyTextToClipboard(teksAktif[0]));

        widget.PanelBiasa panelToolbar = new widget.PanelBiasa();
        panelToolbar.setLayout(new java.awt.BorderLayout(8, 0));
        panelToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 6, 10));
        panelToolbar.add(lblHeader, java.awt.BorderLayout.WEST);

        widget.PanelBiasa panelToolbarRight = new widget.PanelBiasa();
        panelToolbarRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        panelToolbarRight.add(tabRaw);
        panelToolbarRight.add(badgeJson);
        panelToolbarRight.add(btnCopyAtas);
        panelToolbar.add(panelToolbarRight, java.awt.BorderLayout.EAST);

        javax.swing.JScrollPane scrollArea = new javax.swing.JScrollPane(area);
        scrollArea.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(235, 235, 235)),
                javax.swing.BorderFactory.createEmptyBorder()));

        widget.PanelBiasa panelKanan = new widget.PanelBiasa();
        panelKanan.setLayout(new java.awt.BorderLayout(0, 0));
        panelKanan.add(panelToolbar, java.awt.BorderLayout.NORTH);
        panelKanan.add(scrollArea, java.awt.BorderLayout.CENTER);

        javax.swing.JScrollPane scrollList = new javax.swing.JScrollPane(list);
        scrollList.setPreferredSize(new java.awt.Dimension(285, 560));
        scrollList.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, new java.awt.Color(224, 224, 224)));

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                scrollList, panelKanan);
        split.setDividerLocation(285);
        split.setBorder(null);
        dlg.add(split, java.awt.BorderLayout.CENTER);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ResponTransaksi item = list.getSelectedValue();
                if (item != null) {
                    String text = formatResponPostman(item);
                    teksAktif[0] = text;
                    lblHeader.setText((item.error ? "ERROR · " : "OK · ") + item.judul + "   " + item.method + "   " + item.url);
                    setPostmanStyledText(area, text);
                    area.setCaretPosition(0);
                }
            }
        });

        if (listModel.getSize() > 0) {
            list.setSelectedIndex(listModel.getSize() - 1);
        } else {
            String kosong = "Belum ada response transaksi.\n\nKlik tombol aksi seperti Buat Encounter, Buat Condition, Kirim Pra Permintaan, Cari Kandidat, Kirim Tugas, dan lain-lain.";
            teksAktif[0] = kosong;
            setPostmanStyledText(area, kosong);
        }

        widget.PanelBiasa panelBawah = new widget.PanelBiasa();
        panelBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 7));
        panelBawah.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(235, 235, 235)));

        widget.Button btnBersihkan = new widget.Button();
        btnBersihkan.setText("Bersihkan");
        btnBersihkan.setPreferredSize(new java.awt.Dimension(100, 30));
        btnBersihkan.addActionListener(e -> {
            riwayatResponTransaksi.clear();
            listModel.clear();
            teksAktif[0] = "Riwayat response sudah dibersihkan.";
            setPostmanStyledText(area, teksAktif[0]);
            lblHeader.setText("Riwayat response dibersihkan");
        });
        panelBawah.add(btnBersihkan);

        widget.Button btnClose = new widget.Button();
        btnClose.setText("Tutup");
        btnClose.setPreferredSize(new java.awt.Dimension(90, 30));
        btnClose.addActionListener(e -> dlg.dispose());
        panelBawah.add(btnClose);
        dlg.add(panelBawah, java.awt.BorderLayout.SOUTH);

        dlg.setSize(1120, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private String formatResponPostman(ResponTransaksi item) {
        StringBuilder sb = new StringBuilder();
        sb.append("// ").append(item.judul).append("\n");
        sb.append("// Waktu  : ").append(item.waktu).append("\n");
        sb.append("// Status : ").append(item.error ? "ERROR" : "OK").append("\n");
        if (!safe(item.method).equals("")) {
            sb.append("// Method : ").append(item.method).append("\n");
        }
        if (!safe(item.url).equals("")) {
            sb.append("// URL    : ").append(item.url).append("\n");
        }
        sb.append("\n");
        sb.append("/* REQUEST / PARAMETER */\n");
        sb.append(prettyJson(item.request)).append("\n\n");
        sb.append("/* RESPONSE */\n");
        sb.append(prettyJson(item.response)).append("\n");
        return sb.toString();
    }

    private void copyTextToClipboard(String text) {
        try {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text == null ? "" : text), null);
        } catch (Exception e) {
            System.out.println("Gagal copy response: " + e.getMessage());
        }
    }

    private void setPostmanStyledText(javax.swing.JTextPane pane, String text) {
        try {
            javax.swing.text.StyledDocument doc = pane.getStyledDocument();
            doc.remove(0, doc.getLength());

            javax.swing.text.SimpleAttributeSet normal = new javax.swing.text.SimpleAttributeSet();
            javax.swing.text.StyleConstants.setFontFamily(normal, "Consolas");
            javax.swing.text.StyleConstants.setFontSize(normal, 12);
            javax.swing.text.StyleConstants.setForeground(normal, new java.awt.Color(45, 45, 45));

            javax.swing.text.SimpleAttributeSet comment = new javax.swing.text.SimpleAttributeSet(normal);
            javax.swing.text.StyleConstants.setForeground(comment, new java.awt.Color(120, 120, 120));

            javax.swing.text.SimpleAttributeSet key = new javax.swing.text.SimpleAttributeSet(normal);
            javax.swing.text.StyleConstants.setForeground(key, new java.awt.Color(190, 50, 90));

            javax.swing.text.SimpleAttributeSet value = new javax.swing.text.SimpleAttributeSet(normal);
            javax.swing.text.StyleConstants.setForeground(value, new java.awt.Color(35, 130, 70));

            javax.swing.text.SimpleAttributeSet boolnum = new javax.swing.text.SimpleAttributeSet(normal);
            javax.swing.text.StyleConstants.setForeground(boolnum, new java.awt.Color(55, 95, 170));

            javax.swing.text.SimpleAttributeSet section = new javax.swing.text.SimpleAttributeSet(normal);
            javax.swing.text.StyleConstants.setForeground(section, new java.awt.Color(42, 115, 200));
            javax.swing.text.StyleConstants.setBold(section, true);

            doc.insertString(0, text == null ? "" : text, normal);
            String isi = text == null ? "" : text;

            applyRegexStyle(doc, isi, "(?m)^//.*$", comment);
            applyRegexStyle(doc, isi, "/\\*[^*]*\\*/", section);
            applyRegexStyle(doc, isi, "\\\"[^\\\"]+\\\"\\s*:", key);
            applyRegexStyle(doc, isi, ":\\s*\\\"[^\\\"]*\\\"", value);
            applyRegexStyle(doc, isi, "\\b(true|false|null)\\b", boolnum);
            applyRegexStyle(doc, isi, "(?<![A-Za-z0-9_])[-]?[0-9]+(\\.[0-9]+)?(?![A-Za-z0-9_])", boolnum);
        } catch (Exception e) {
            pane.setText(text == null ? "" : text);
        }
    }

    private void applyRegexStyle(javax.swing.text.StyledDocument doc, String text, String regex,
                                 javax.swing.text.AttributeSet style) {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(text);
            while (m.find()) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), style, false);
            }
        } catch (Exception ignore) {}
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String jsonNodeToPretty(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        return prettyJson(node.toString());
    }

    private String prettyJson(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.equals("")) {
            return "";
        }
        try {
            if (text.startsWith("{") || text.startsWith("[")) {
                JsonNode node = ihsEncounter.getMapper().readTree(text);
                return ihsEncounter.getMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(node);
            }
        } catch (Exception ignore) {}
        return raw;
    }

    // =================================================================
    //  ERROR HANDLING
    // =================================================================
    private void handleApiError(Exception ex) {
        ex.printStackTrace();
        String msg = ex.toString();
        String bodyError = "";
        try {
            if (ex instanceof org.springframework.web.client.HttpStatusCodeException) {
                org.springframework.web.client.HttpStatusCodeException httpEx =
                        (org.springframework.web.client.HttpStatusCodeException) ex;
                bodyError = httpEx.getResponseBodyAsString();
                if (bodyError != null && !bodyError.trim().equals("")) {
                    msg = msg + "\n\nBody:\n" + bodyError;
                }
            }
        } catch (Exception ignore) {}
        catatResponTransaksi("ERROR TERAKHIR", "ERROR", "", ex.toString(), bodyError, true);
        JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + ex.toString(), true);
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
            SatuSehatRujukanIGD dlg = new SatuSehatRujukanIGD(new javax.swing.JFrame(), true);
            dlg.setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Button btnBuatCondition;
    private widget.Button btnBuatEncounter;
    private widget.Button btnCari;
    private widget.Button btnCariKandidat;
    private widget.Button btnCekStatusBaris;
    private widget.Button btnCekStatusSemua;
    private widget.Button btnKirimAppointment;
    private widget.Button btnKirimFinal;
    private widget.Button btnKirimPra;
    private widget.Button btnKirimTugas;
    private widget.Button btnKirimTugasTeman;
    private widget.Button btnPilihDiagnosa;
    private widget.Button btnRefresh;
    private widget.Button btnRujukanMasuk;
    private widget.Button btnTampilRespon;
    private widget.Button btnTutup;
    private widget.ComboBox cbJenisTransport;
    private widget.ComboBox cbProvinsi;
    private widget.InternalFrame frameMain;
    private widget.Label lblAmbulans;
    private widget.Label lblAppointmentMulai;
    private widget.Label lblAppointmentSelesai;
    private widget.Label lblCari;
    private widget.Label lblNoKartu;
    private widget.Label lblNoPCare;
    private widget.Label lblNoRujukanNasional;
    private widget.Label lblProvinsi;
    private widget.Label lblStatus;
    private widget.Label lblTab1Status;
    private widget.Label lblTab2Status;
    private widget.Label lblTab3Status;
    private widget.Label lblTab4Status;
    private widget.Label lblTgl1;
    private widget.Label lblTgl2;
    private widget.Label lblTransport;
    private widget.Label lbltDiagnosa;
    private widget.Label lbltEncounter;
    private widget.Label lbltIdConditionIhs;
    private widget.Label lbltIdDokterIhs;
    private widget.Label lbltIdPasienIhs;
    private widget.Label lbltNoRawat;
    private widget.Label lbltPasien;
    private widget.PanelBiasa panelBottom;
    private widget.PanelBiasa panelMid;
    private widget.PanelBiasa panelTop;
    private widget.PanelBiasa pnlTab1;
    private widget.PanelBiasa pnlTab2;
    private widget.PanelBiasa pnlTab3;
    private widget.PanelBiasa pnlTab4;
    private widget.ScrollPane scrollAccepted;
    private widget.ScrollPane scrollKandidat;
    private widget.ScrollPane scrollKriteria;
    private widget.ScrollPane scrollPasien;
    private widget.ScrollPane scrollTugas;
    private widget.TextBox tAppointmentMulai;
    private widget.TextBox tAppointmentSelesai;
    private widget.TextBox tCariPasien;
    private widget.TextBox tDiagnosa;
    private widget.TextBox tEncounter;
    private widget.TextBox tIdConditionIhs;
    private widget.TextBox tIdDokterIhs;
    private widget.TextBox tIdPasienIhs;
    private widget.TextBox tNoKartuAsuransi;
    private widget.TextBox tNoRawat;
    private widget.TextBox tNoRegAmbulans;
    private widget.TextBox tNoRujukanPCare;
    private widget.TextBox tPasien;
    private javax.swing.JTabbedPane tabPane;
    private widget.Table tblAcceptedFaskes;
    private widget.Table tblKandidat;
    private widget.Table tblKriteria;
    private widget.Table tblPasien;
    private widget.Table tblTugas;
    // End of variables declaration//GEN-END:variables

}