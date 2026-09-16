/*
 * BPJSRujukanSatuSehat.java
 *
 * Form Rujukan Keluar terintegrasi BPJS + Satu Sehat (Sisrute).
 * Dipanggil dari BPJSDataSEP setelah user memilih SEP rawat jalan.
 *
 * PATCH CETAK SEP V12 - 09 September 2026
 * - transaksi atomik untuk snapshot dan tiga tabel bridging
 * - composite key no_rujukan + link_id untuk banyak kriteria
 * - preview modern serta tombol Cetak SEP Rujukan
 * - tabel penyimpanan lokal dibuat/dimigrasikan otomatis sebelum kirim
 * - PATCH ALUR KRITERIA V13 - 09 September 2026
 * - sinkronisasi satu-dari-tiga kriteria dapat dinonaktifkan oleh user
 * - alur tombol Cek Kriteria > Cari Faskes > Kirim Rujukan
 *
 * Flow:
 *   1. setDataSEP(...) dipanggil parent → form auto-isi data
 *   2. User cek/lengkapi data, lalu klik [Cek Kriteria]
 *      → POST /Rujukan/GetKriteriaRujukan
 *      → Tabel kriteria muncul, user isi jawaban (boolean/text)
 *   3. User klik [Cari Faskes] → POST /Sisrute/GetFaskesRujukan
 *      → Tabel faskes muncul, user pilih satu RS tujuan
 *   4. User klik [Kirim Rujukan] → POST /Sisrute/postKunjungan
 *      → Simpan ke tabel bridging lama + snapshot lengkap form:
 *         - bridging_rujukan_bpjs       (existing)
 *         - bridging_rujukan_satusehat  (BARU)
 *         - bridging_kriteria_rujukan_satusehat (BARU)
 *         - bridging_rujukan_satusehat_form (snapshot pemulihan form)
 *
 * @author SIMRS Khanza Bridging Sisrute
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.ToastMessage;

import javax.swing.*;
import javax.swing.ListSelectionModel;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.LinkedHashSet;


 import java.awt.Frame;


public final class BPJSRujukanSatuSehat extends javax.swing.JDialog {

    // ===== Helper Khanza =====
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final ObjectMapper mapper = new ObjectMapper();
    private PreparedStatement ps;
    private ResultSet rs;   
    // ===== Service =====
    private final SisruteService sisrute = new SisruteService();

    // ===== State data dari parent (BPJSDataSEP) =====
    private String noSep = "";
    private String noRawat = "";
    private String noRm = "";
    private String namaPasien = "";
    private String kdPenyakit = "";        // ICD-10 (mis. I10)
    private String nmPenyakit = "";
    private String kdPoliBpjs = "";        // kode poli/spesialis BPJS (mis. INT)
    private String nmPoli = "";
    private String kdDokterRs = "";        // kd_dokter Khanza
    private String user = "";

    // ===== State data Satu Sehat =====
    private String kodeFaskesSatuSehat = "";  // dari koneksiDB.IDSATUSEHAT()
    private String idPasienSatuSehat = "";    // IHS pasien
    private String kdDokterSatuSehat = "";    // IHS dokter (dari mapping)
    private String encounterRef = "";         // dari satu_sehat_encounter
    private String kodeSpesialis = "";        // dari maping_poli_bpjs

    // ===== State faskes tujuan terpilih =====
    private String selectedKdppkTujuan = "";
    private String selectedNmppkTujuan = "";
    private String selectedKdppkSatuSehatTujuan = "";

    // ===== State rujukan aktif yang sudah diterbitkan =====
    // Snapshot disimpan terpisah dari tabel bridging lama agar seluruh isi form
    // dapat dipulihkan tanpa mengubah kontrak tabel/modul Khanza yang sudah ada.
    private static final String SNAPSHOT_TABLE =
            "bridging_rujukan_satusehat_form";
    private String activeServiceRequestId = "";
    private boolean referralPersisted = false;
    private String lastLocalSaveError = "";
    private boolean restoringReferralSnapshot = false;
    private boolean snapshotTableWarningShown = false;

    // ===== State diagnosa & poli RUJUKAN (boleh beda dari diagnosa kunjungan) =====
    private String kdPenyakitRujuk = "";   // ICD-10 untuk rujukan (default = kdPenyakit kunjungan)
    private String nmPenyakitRujuk = "";
    private String kdPoliRujuk = "";       // kode poli BPJS untuk rujukan (default = kdPoliBpjs kunjungan)
    private String nmPoliRujuk = "";

    // ===== Popup picker BPJS (reuse existing popup) =====
    private final BPJSCekReferensiPenyakit popupPenyakit = new BPJSCekReferensiPenyakit(null, true);
    private final BPJSCekReferensiPoli     popupPoli     = new BPJSCekReferensiPoli(null, true);

    // ===== Helper cari IHS pasien & dokter =====
    private final SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();

    // ===== Tabel models =====
    private final DefaultTableModel modelKriteria;
    private final DefaultTableModel modelFaskes;

    // Daftar kode-nama provinsi (preset 5 wilayah jejaring umum, bisa diperluas)
    private static final String[][] PROVINSI = {
        {"31", "DKI Jakarta"},
        {"32", "Jawa Barat"},
        {"33", "Jawa Tengah"},
        {"34", "Daerah Istimewa Yogyakarta"},
        {"35", "Jawa Timur"},
        {"36", "Banten"},
        {"73", "Sulawesi Selatan"},
    };

    
     // Daftar kode-nama provinsi (preset 5 wilayah jejaring umum, bisa diperluas)
    private static final String[][] KABUPATEN = {
        {"7371", "Kota Makassar"},
    };
    
     public static int posisiTinggi = 0;
    
     // ===== State data dari parent (BPJSDataSEP) =====
    private String noRujukanBPJS = "";
    private String noRujukanSatusehat = "";

    // ===== Riwayat respons API selama form masih dibuka (read-only) =====
    private static final class ApiResponseEntry {
        private final String source;
        private final String time;
        private final String json;
        private final boolean error;

        ApiResponseEntry(String source, String time, String json, boolean error) {
            this.source = source;
            this.time = time;
            this.json = json;
            this.error = error;
        }
    }

    private final java.util.List<ApiResponseEntry> apiResponseHistory =
            new java.util.ArrayList<>();
    private String lastApiResponseJson = "";
    private String lastApiResponseSource = "Belum ada respon API";
    private String lastApiResponseTime = "";
    private boolean lastApiResponseError = false;
    private String lastProcessStatusMessage = "Form siap digunakan.";
    private boolean lastProcessStatusError = false;

    // Label ringkas pada header panel tabel (UI saja, tidak ikut payload/API).
    private javax.swing.JLabel lblJumlahKriteriaUi;
    private javax.swing.JLabel lblJumlahFaskesUi;
    private javax.swing.JTextField txtCariFaskesUi;
    private javax.swing.JCheckBox chkSinkronKriteriaUi;
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterFaskesUi;
    private javax.swing.JPanel notesCardUi;
    private javax.swing.JButton btnCopyNoRujukanBpjsUi;
    private javax.swing.JButton btnCopyNoRujukanSatuSehatUi;
    private final widget.Button btnPulihkanRujukanUi = new widget.Button();
    private final widget.Button btnCetakSepRujukanUi = new widget.Button();
    private static final int TOAST_INFO = Integer.MIN_VALUE;
    private boolean syncingCriteriaAnswers = false;

    /** Satu catatan panggilan API selama proses pemulihan rujukan lama. */
    private static final class RecoveryApiLog {
        private final String source;
        private final String time;
        private final JsonNode response;

        RecoveryApiLog(String source, JsonNode response) {
            this.source = source;
            this.time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .format(new java.util.Date());
            this.response = response;
        }
    }

    /** Kandidat rujukan BPJS yang ditemukan melalui riwayat peserta. */
    private static final class RecoveryReferralCandidate {
        private String noRujukan = "";
        private String tglRujukan = "";
        private String kodeDiagnosa = "";
        private String namaDiagnosa = "";
        private String kodePoli = "";
        private String namaPoli = "";
        private String kodePerujuk = "";
        private String namaPerujuk = "";
    }

    /**
     * Hasil gabungan data lokal, BPJS VClaim, dan SATUSEHAT. Tidak ada data
     * yang langsung diterapkan ke form sebelum pengguna menekan Simpan.
     */
    private static final class ReferralRecoveryResult {
        private String noSep = "";
        private String noRujukanBpjs = "";
        private String noRujukanSatuSehat = "";
        private String serviceRequestId = "";
        private String noKartuPeserta = "";
        private String namaPasienBpjs = "";
        private String kodeFaskesAsalSatuSehat = "";
        private String idPasienSatuSehat = "";
        private String kdppkSatuSehatTujuan = "";
        private String kodePpkTujuan = "";
        private String namaFaskesTujuan = "";
        private String kdDokterSatuSehat = "";
        private String encounterReference = "";
        private String patientInstruction = "";
        private String keteranganRujukan = "";
        private String tglRujukan = "";
        private String tglRencanaKunjungan = "";
        private String jenisPelayanan = "";
        private String tipeRujukan = "";
        private String catatan = "";
        private String kodeDiagnosa = "";
        private String namaDiagnosa = "";
        private String kodePoli = "";
        private String namaPoli = "";
        private boolean foundFromLocal;
        private boolean foundFromBpjs;
        private boolean foundFromSatuSehat;
        private boolean verifiedBpjsNumber;
        private boolean verifiedSatuSehatNumber;
        private final java.util.List<String> notices =
                new java.util.ArrayList<>();
        private final java.util.List<RecoveryApiLog> apiLogs =
                new java.util.ArrayList<>();
        private final java.util.List<Object[]> criteriaRows =
                new java.util.ArrayList<>();
    }

    // Toast dibuat lokal agar perubahan tampilan tidak memengaruhi form Khanza lain.
    private static final java.util.List<javax.swing.JWindow> ACTIVE_RUJUKAN_TOASTS =
            new java.util.ArrayList<>();

    // Backdrop blur khusus form ini. Glass pane lama selalu dikembalikan saat
    // popup ditutup agar tidak memengaruhi komponen maupun form Khanza lain.
    private java.awt.Component previousMainGlassPane;
    private boolean previousMainGlassPaneVisible;
    private boolean mainFormBlurActive;
    private static final int POPUP_CORNER_RADIUS = 20;
    
    public BPJSRujukanSatuSehat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        // Init table models
        modelKriteria = new DefaultTableModel(null, new Object[]{
                "linkId", "Pertanyaan Kriteria", "Tipe", "Jawaban (YA/TIDAK atau teks)"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };

        modelFaskes = new DefaultTableModel(null, new Object[]{
                "Pilih", "Kode SatuSehat", "Kode PPK", "Nama RS", "Kelas","Strata",
                "Kab/Kota", "Jarak (m)","Kapasitas", "% Beban"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
        };

        initComponents();
        setupKomponenTambahan();
       
     
        setSize(1100, 720);
        setLocationRelativeTo(parent);

        // Ambil user yang login
        try {
            user = akses.getkode().replace(" ", "").substring(0, 9);
        } catch (Exception e) {
            user = akses.getkode();
        }

        // Ambil kode faskes Satu Sehat
        try {
            kodeFaskesSatuSehat = koneksiDB.IDSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Gagal ambil IDSATUSEHAT : " + e);
        }

        // Setup window listener popup picker - copy data ke field saat dialog ditutup
        setupPopupListeners();
    }

    /**
     * Setup callback saat user pilih item di popup BPJSCekReferensiPenyakit & Poli.
     * Pattern reuse dari BPJSRujukanKeluar existing.
     */
    private void setupPopupListeners() {
        popupPenyakit.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                pilihDiagnosaDariPopup();
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });

        popupPoli.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                pilihPoliDariPopup();
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });

        setupPopupAutoCloseListeners();
    }

    /**
     * Mempermudah user memilih data dari popup referensi:
     * - klik baris lalu tekan SPACE = popup menutup dan data terisi
     * - double click baris = popup menutup dan data terisi
     *
     * Data tetap diambil oleh method pilihDiagnosaDariPopup()/pilihPoliDariPopup(),
     * sehingga tidak mengubah flow lama yang sebelumnya memakai windowClosed.
     */
    private void setupPopupAutoCloseListeners() {
        try {
            popupPenyakit.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2 && popupPenyakit.getTable().getSelectedRow() != -1) {
                        pilihDiagnosaDariPopup();
                        javax.swing.SwingUtilities.invokeLater(() -> popupPenyakit.dispose());
                    }
                }
            });

            popupPenyakit.getTable().addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE
                            && popupPenyakit.getTable().getSelectedRow() != -1) {
                        e.consume();
                        pilihDiagnosaDariPopup();
                        javax.swing.SwingUtilities.invokeLater(() -> popupPenyakit.dispose());
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Gagal setup auto close popup diagnosa: " + e.getMessage());
        }

        try {
            popupPoli.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2 && popupPoli.getTable().getSelectedRow() != -1) {
                        pilihPoliDariPopup();
                        javax.swing.SwingUtilities.invokeLater(() -> popupPoli.dispose());
                    }
                }
            });

            popupPoli.getTable().addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE
                            && popupPoli.getTable().getSelectedRow() != -1) {
                        e.consume();
                        pilihPoliDariPopup();
                        javax.swing.SwingUtilities.invokeLater(() -> popupPoli.dispose());
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Gagal setup auto close popup poli: " + e.getMessage());
        }
    }

    private void pilihDiagnosaDariPopup() {
        try {
            if (popupPenyakit.getTable().getSelectedRow() != -1) {
                int row = popupPenyakit.getTable().getSelectedRow();
                kdPenyakitRujuk = popupPenyakit.getTable().getValueAt(row, 1).toString();
                nmPenyakitRujuk = popupPenyakit.getTable().getValueAt(row, 2).toString();
                tKdDiagnosaRujuk.setText(kdPenyakitRujuk);
                tNmDiagnosaRujuk.setText(nmPenyakitRujuk);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil diagnosa dari popup: " + e.getMessage());
        }
    }

    private void pilihPoliDariPopup() {
        try {
            if (popupPoli.getTable().getSelectedRow() != -1) {
                int row = popupPoli.getTable().getSelectedRow();
                kdPoliRujuk = popupPoli.getTable().getValueAt(row, 1).toString();
                nmPoliRujuk = popupPoli.getTable().getValueAt(row, 2).toString();
                tKdPoliRujuk.setText(kdPoliRujuk);
                tNmPoliRujuk.setText(nmPoliRujuk);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil poli dari popup: " + e.getMessage());
        }
    }

    // =================================================================
    //  initComponents - dibangun manual (tidak pakai NetBeans Form Editor)
    //  supaya gampang di-port dan tidak butuh .form file.
    // =================================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameMain = new widget.InternalFrame();
        panelData = new widget.PanelBiasa();
        lblNoSep = new widget.Label();
        tNoSep = new widget.TextBox();
        lblNoRawat = new widget.Label();
        tNoRawat = new widget.TextBox();
        lblPasien = new widget.Label();
        tPasien = new widget.TextBox();
        lblDiag = new widget.Label();
        tKdDiagnosaRujuk = new widget.TextBox();
        tNmDiagnosaRujuk = new widget.TextBox();
        btnPilihDiagnosa = new widget.Button();
        lblTglRujukan = new widget.Label();
        dtTglRujukan = new widget.Tanggal();
        lblTglRencana = new widget.Label();
        dtTglRencana = new widget.Tanggal();
        lblTipeRujukan = new widget.Label();
        cbTipeRujukan = new widget.ComboBox();
        lblProvinsi = new widget.Label();
        cbProvinsi = new widget.ComboBox();
        lblEncounter = new widget.Label();
        tEncounter = new widget.TextBox();
        btnBuatEncounter = new widget.Button();
        lblIhsPasien = new widget.Label();
        tIdPasienIhs = new widget.TextBox();
        lblIhsDokter = new widget.Label();
        tIdDokterIhs = new widget.TextBox();
        lblPoli = new widget.Label();
        tKdPoliRujuk = new widget.TextBox();
        tNmPoliRujuk = new widget.TextBox();
        btnPilihPoli = new widget.Button();
        lblJnsPelayanan = new widget.Label();
        cbJnsPelayanan = new widget.ComboBox();
        lblCatatan = new widget.Label();
        scrollCatatan = new widget.ScrollPane();
        taCatatan = new widget.TextArea();
        lblKet = new widget.Label();
        scrollKeterangan = new widget.ScrollPane();
        taKeterangan = new widget.TextArea();
        lblStatus = new widget.Label();
        scrollKeterangan1 = new widget.ScrollPane();
        tStatus = new widget.TextArea();
        lblKet1 = new widget.Label();
        lblNoSep1 = new widget.Label();
        tNoRujukanBpjs = new widget.TextBox();
        tNoRujukanSatuSehat = new widget.TextBox();
        lblNoSep2 = new widget.Label();
        panelTengah = new widget.PanelBiasa();
        frameKriteria = new widget.InternalFrame();
        scrollKriteria = new widget.ScrollPane();
        tblKriteria = new widget.Table();
        frameFaskes = new widget.InternalFrame();
        scrollFaskes = new widget.ScrollPane();
        tblFaskes = new widget.Table();
        panelTombol = new widget.PanelBiasa();
        btnCekKriteria = new widget.Button();
        btnCariFaskes = new widget.Button();
        btnKirim = new widget.Button();
        btnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        btnTutup = new widget.Button();
        btnResponApi = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rujukan Keluar Sisrute BPJS + SATUSEHAT");
        setUndecorated(true);
        setResizable(false);

        frameMain.setLayout(new java.awt.BorderLayout());

        panelData.setPreferredSize(new java.awt.Dimension(550, 250));
        panelData.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblNoSep.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoSep.setText("No. SEP");
        panelData.add(lblNoSep, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 80, 26));

        tNoSep.setEditable(false);
        panelData.add(tNoSep, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 140, 26));

        lblNoRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoRawat.setText("No. Rawat");
        panelData.add(lblNoRawat, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 80, 26));
        panelData.add(tNoRawat, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, 140, 26));

        lblPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPasien.setText("Nama Pasien");
        panelData.add(lblPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 80, 26));

        tPasien.setEditable(false);
        panelData.add(tPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, 390, 26));

        lblDiag.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDiag.setText("Diagnosa Rujuk");
        panelData.add(lblDiag, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 95, 26));

        tKdDiagnosaRujuk.setEditable(false);
        panelData.add(tKdDiagnosaRujuk, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 200, 60, 26));
        panelData.add(tNmDiagnosaRujuk, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 200, 260, 26));

        btnPilihDiagnosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPilihDiagnosa.setIconTextGap(10);
        btnPilihDiagnosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPilihDiagnosaActionPerformed(evt);
            }
        });
        panelData.add(btnPilihDiagnosa, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 200, 30, 26));

        lblTglRujukan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTglRujukan.setText("Tgl. Rujukan");
        panelData.add(lblTglRujukan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, 95, 26));

        dtTglRujukan.setDisplayFormat("dd/MM/yyyy");
        panelData.add(dtTglRujukan, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 290, 140, 26));

        lblTglRencana.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTglRencana.setText("Tgl. Rencana");
        panelData.add(lblTglRencana, new org.netbeans.lib.awtextra.AbsoluteConstraints(295, 290, 80, 26));

        dtTglRencana.setDisplayFormat("dd/MM/yyyy");
        panelData.add(dtTglRencana, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 290, 110, 26));

        lblTipeRujukan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTipeRujukan.setText("Tipe Rujukan");
        panelData.add(lblTipeRujukan, new org.netbeans.lib.awtextra.AbsoluteConstraints(295, 260, 80, 26));
        panelData.add(cbTipeRujukan, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 260, 110, 26));

        lblProvinsi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProvinsi.setText("Provinsi");
        panelData.add(lblProvinsi, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, 75, 26));
        panelData.add(cbProvinsi, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 320, 360, 26));

        lblEncounter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblEncounter.setText("Encounter");
        panelData.add(lblEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 80, 26));

        tEncounter.setEditable(false);
        panelData.add(tEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 240, 26));

        btnBuatEncounter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/add.png"))); // NOI18N
        btnBuatEncounter.setText("Buat Encounter");
        btnBuatEncounter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBuatEncounter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuatEncounterActionPerformed(evt);
            }
        });
        panelData.add(btnBuatEncounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 110, 130, 26));

        lblIhsPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIhsPasien.setText("IHS Pasien");
        panelData.add(lblIhsPasien, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 80, 26));

        tIdPasienIhs.setEditable(false);
        tIdPasienIhs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tIdPasienIhsActionPerformed(evt);
            }
        });
        panelData.add(tIdPasienIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 140, 140, 26));

        lblIhsDokter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIhsDokter.setText("IHS Dokter");
        panelData.add(lblIhsDokter, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 80, 26));

        tIdDokterIhs.setEditable(false);
        panelData.add(tIdDokterIhs, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 140, 26));

        lblPoli.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPoli.setText("Poli Rujuk");
        panelData.add(lblPoli, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 95, 26));

        tKdPoliRujuk.setEditable(false);
        panelData.add(tKdPoliRujuk, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 230, 60, 26));

        tNmPoliRujuk.setEditable(false);
        panelData.add(tNmPoliRujuk, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 230, 260, 26));

        btnPilihPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPilihPoli.setIconTextGap(10);
        panelData.add(btnPilihPoli, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 230, 30, 26));

        lblJnsPelayanan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblJnsPelayanan.setText("Jenis Pelayanan");
        panelData.add(lblJnsPelayanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 260, 95, 26));
        panelData.add(cbJnsPelayanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 260, 140, 26));

        lblCatatan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCatatan.setText("Catatan");
        panelData.add(lblCatatan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 350, 95, 26));

        scrollCatatan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollCatatan.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollCatatan.setViewportView(taCatatan);

        panelData.add(scrollCatatan, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 353, 360, 60));

        lblKet.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKet.setText("Keterangan");
        panelData.add(lblKet, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 420, 95, 26));

        scrollKeterangan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollKeterangan.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollKeterangan.setViewportView(taKeterangan);

        panelData.add(scrollKeterangan, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 420, 360, 60));

        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblStatus.setText("Status: siap.");
        lblStatus.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        panelData.add(lblStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 580, 440, 40));

        scrollKeterangan1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollKeterangan1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tStatus.setEditable(false);
        tStatus.setFont(new java.awt.Font("Segoe UI", 0, 11));
        scrollKeterangan1.setViewportView(tStatus);

        panelData.add(scrollKeterangan1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 490, 360, 80));

        lblKet1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKet1.setText("Status Proses");
        panelData.add(lblKet1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 490, 95, 20));

        lblNoSep1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoSep1.setText("No. RJK BPJS");
        panelData.add(lblNoSep1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, 80, 26));

        tNoRujukanBpjs.setEditable(false);
        tNoRujukanBpjs.setBackground(new java.awt.Color(255, 255, 204));
        panelData.add(tNoRujukanBpjs, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 20, 140, 26));

        tNoRujukanSatuSehat.setEditable(false);
        tNoRujukanSatuSehat.setBackground(new java.awt.Color(255, 255, 204));
        panelData.add(tNoRujukanSatuSehat, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 50, 140, 26));

        lblNoSep2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoSep2.setText("No. RJK Satu Sehat");
        panelData.add(lblNoSep2, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 50, -1, 26));

        frameMain.add(panelData, java.awt.BorderLayout.WEST);
        panelData.getAccessibleContext().setAccessibleName("Rujukan");

        panelTengah.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        panelTengah.setLayout(new java.awt.GridLayout(2, 1, 5, 10));

        frameKriteria.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        frameKriteria.setPreferredSize(new java.awt.Dimension(454, 300));
        frameKriteria.setLayout(new java.awt.BorderLayout());

        scrollKriteria.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollKriteria.setPreferredSize(new java.awt.Dimension(452, 350));
        scrollKriteria.setViewportView(tblKriteria);

        frameKriteria.add(scrollKriteria, java.awt.BorderLayout.CENTER);

        panelTengah.add(frameKriteria);

        frameFaskes.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        frameFaskes.setLayout(new java.awt.BorderLayout());

        scrollFaskes.setViewportView(tblFaskes);

        frameFaskes.add(scrollFaskes, java.awt.BorderLayout.CENTER);

        panelTengah.add(frameFaskes);

        frameMain.add(panelTengah, java.awt.BorderLayout.CENTER);

        panelTombol.setPreferredSize(new java.awt.Dimension(1100, 55));
        panelTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        btnCekKriteria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnCekKriteria.setText("Cek Kriteria");
        btnCekKriteria.setPreferredSize(new java.awt.Dimension(120, 30));
        btnCekKriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekKriteriaActionPerformed(evt);
            }
        });
        panelTombol.add(btnCekKriteria);

        btnCariFaskes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        btnCariFaskes.setText("Cari Faskes");
        btnCariFaskes.setPreferredSize(new java.awt.Dimension(120, 30));
        panelTombol.add(btnCariFaskes);

        btnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        btnKirim.setText("Kirim Rujukan");
        btnKirim.setPreferredSize(new java.awt.Dimension(120, 30));
        btnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKirimActionPerformed(evt);
            }
        });
        panelTombol.add(btnKirim);

        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        btnHapus.setText("Hapus Rujukan");
        btnHapus.setEnabled(false);
        btnHapus.setPreferredSize(new java.awt.Dimension(150, 30));
        panelTombol.add(btnHapus);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Data Rujukan");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setPreferredSize(new java.awt.Dimension(150, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        panelTombol.add(BtnPrint);

        btnTutup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        btnTutup.setText("Tutup");
        btnTutup.setPreferredSize(new java.awt.Dimension(120, 30));
        panelTombol.add(btnTutup);

        btnResponApi.setText("Respon API");
        btnResponApi.setPreferredSize(new java.awt.Dimension(130, 30));
        panelTombol.add(btnResponApi);

        frameMain.add(panelTombol, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(frameMain, java.awt.BorderLayout.CENTER);
        frameMain.getAccessibleContext().setAccessibleName("Rujukan");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tIdPasienIhsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tIdPasienIhsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tIdPasienIhsActionPerformed

    private void btnPilihDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilihDiagnosaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPilihDiagnosaActionPerformed

    private void btnBuatEncounterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuatEncounterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBuatEncounterActionPerformed

    private void btnCekKriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekKriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCekKriteriaActionPerformed

    private void btnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKirimActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnKirimActionPerformed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            BPJSRujukanKeluarSatuSehat aplikasi=new BPJSRujukanKeluarSatuSehat(null,false);
            //aplikasi.setNoRawat(TNoRw.getText().trim());
            //aplikasi.loadData();
            aplikasi.setSize(frameMain.getWidth(),frameMain.getHeight());
            aplikasi.setLocationRelativeTo(frameMain);
            aplikasi.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintActionPerformed

    /**
     * Inisialisasi lanjutan di luar blok NetBeans GUI Builder.
     * Sengaja dipisahkan agar file .form tetap bisa diedit/drag-drop di NetBeans,
     * sementara model tabel, editor YA/TIDAK, combo, dan event tombol tetap aman.
     */
    private void setupKomponenTambahan() {
       
        setupComboDefaults();
        setupTabelTengah();
        setupActionTombol();
        setupPanelTombolModern();
        setupModernPatientForm();
        setupFormHeader();
        setupModernDiagnosisPopup();
        setupModernPoliPopup();
        setupPopupBlurEffect();
        tNoRujukanBpjs.setText("");
        tNoRujukanSatuSehat.setText("");
        updateReferralActionState();
    }

    private void setupComboDefaults() {
        try {
            cbJnsPelayanan.removeAllItems();
            cbJnsPelayanan.addItem("2. Rawat Jalan");

            cbTipeRujukan.removeAllItems();
            cbTipeRujukan.addItem("0. Penuh");
            cbTipeRujukan.addItem("1. Partial");
            cbTipeRujukan.addItem("2. Rujuk Balik");

            cbProvinsi.removeAllItems();
            for (String[] p : PROVINSI) {
                cbProvinsi.addItem(p[0] + " - " + p[1]);
            }
            for (int i = 0; i < PROVINSI.length; i++) {
                if ("73".equals(PROVINSI[i][0])) {
                    cbProvinsi.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal setup combo rujukan: " + e.getMessage());
        }
    }

    private void setupTabelTengah() {
        try {
            tblKriteria.setModel(modelKriteria);
            tblKriteria.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            if (tblKriteria.getColumnModel().getColumnCount() >= 4) {
                TableColumn[] colsKriteria = {
                    tblKriteria.getColumnModel().getColumn(0),
                    tblKriteria.getColumnModel().getColumn(1),
                    tblKriteria.getColumnModel().getColumn(2),
                    tblKriteria.getColumnModel().getColumn(3)
                };
                colsKriteria[0].setPreferredWidth(70);
                colsKriteria[1].setPreferredWidth(400);
                colsKriteria[2].setPreferredWidth(80);
                colsKriteria[3].setPreferredWidth(400);
            }
            tblKriteria.setDefaultRenderer(Object.class, new WarnaTable());
            setupJawabanKriteriaEditor();
            styleModernDataTable(tblKriteria);
            setupLinkIdCellPadding();

            tblFaskes.setModel(modelFaskes);
            tblFaskes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] widthsFaskes = {50, 130, 90, 320, 60, 150,200, 90,90, 80};
            for (int i = 0; i < widthsFaskes.length && i < tblFaskes.getColumnModel().getColumnCount(); i++) {
                tblFaskes.getColumnModel().getColumn(i).setPreferredWidth(widthsFaskes[i]);
            }
            tblFaskes.setDefaultRenderer(Object.class, new WarnaTable());
            styleModernDataTable(tblFaskes);

            styleModernScrollPane(scrollKriteria);
            styleModernScrollPane(scrollFaskes);
            rebuildModernTableSections();
            setupFaskesRowAutoSelection();
            scrollKriteria.getViewport().addComponentListener(
                    new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    javax.swing.SwingUtilities.invokeLater(
                            BPJSRujukanSatuSehat.this::adjustKriteriaAnswerColumnWidth);
                }
            });

            modelFaskes.addTableModelListener(e -> {
                if (e.getColumn() == 0 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    Object val = modelFaskes.getValueAt(row, 0);
                    if (Boolean.TRUE.equals(val)) {
                        for (int i = 0; i < modelFaskes.getRowCount(); i++) {
                            if (i != row && Boolean.TRUE.equals(
                                    modelFaskes.getValueAt(i, 0))) {
                                modelFaskes.setValueAt(Boolean.FALSE, i, 0);
                            }
                        }
                        
                        selectedKdppkSatuSehatTujuan = String.valueOf(modelFaskes.getValueAt(row, 1));
                        selectedKdppkTujuan = String.valueOf(modelFaskes.getValueAt(row, 2));
                        selectedNmppkTujuan = String.valueOf(modelFaskes.getValueAt(row, 3));
                        
                        setStatus("Faskes tujuan dipilih: " + selectedNmppkTujuan, false);
                    } else {
                        selectedKdppkSatuSehatTujuan = "";
                        selectedKdppkTujuan = "";
                        selectedNmppkTujuan = "";
                         
                    }
                    updateReferralActionState();
                }
            });

            // Tinggi kedua panel selalu dihitung ulang setelah isi model berubah.
            modelKriteria.addTableModelListener(e -> {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE
                        && e.getColumn() == 3
                        && !syncingCriteriaAnswers
                        && !restoringReferralSnapshot) {
                    applyCriteriaSynchronization(e.getFirstRow());
                    clearFaskesResultsAfterCriteriaChange();
                }
                javax.swing.SwingUtilities.invokeLater(() -> {
                        adjustKriteriaQuestionColumnWidth();
                        adjustKriteriaAnswerColumnWidth();
                        updateDynamicTableHeights();
                        updateReferralActionState();
                    });
            });
            modelFaskes.addTableModelListener(e ->
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        adjustFaskesColumnWidths();
                        updateDynamicTableHeights();
                        updateReferralActionState();
                    }));
            javax.swing.SwingUtilities.invokeLater(() -> {
                adjustKriteriaQuestionColumnWidth();
                adjustKriteriaAnswerColumnWidth();
                adjustFaskesColumnWidths();
                updateDynamicTableHeights();
            });
        } catch (Exception e) {
            System.out.println("Gagal setup tabel tengah: " + e.getMessage());
        }
    }

    /**
     * Menyusun ulang dua tabel sebagai card datar. Komponen tabel dan scroll lama
     * tetap dipakai sehingga event serta model datanya tidak berubah.
     */
    private void rebuildModernTableSections() {
        java.awt.Color page = new java.awt.Color(248, 250, 252);
        java.awt.Color line = new java.awt.Color(226, 232, 240);

        lblJumlahKriteriaUi = createTableCountBadge("0 item");
        lblJumlahFaskesUi = createTableCountBadge("0 faskes");

        frameKriteria.removeAll();
        frameKriteria.setLayout(new java.awt.BorderLayout());
        frameKriteria.setOpaque(true);
        frameKriteria.setBackground(java.awt.Color.WHITE);
        frameKriteria.setBorder(javax.swing.BorderFactory.createLineBorder(line));
        frameKriteria.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        frameKriteria.add(createCriteriaTableSectionHeader(),
                java.awt.BorderLayout.NORTH);
        frameKriteria.add(scrollKriteria, java.awt.BorderLayout.CENTER);

        frameFaskes.removeAll();
        frameFaskes.setLayout(new java.awt.BorderLayout());
        frameFaskes.setOpaque(true);
        frameFaskes.setBackground(java.awt.Color.WHITE);
        frameFaskes.setBorder(javax.swing.BorderFactory.createLineBorder(line));
        frameFaskes.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        frameFaskes.add(createFaskesTableSectionHeader(),
                java.awt.BorderLayout.NORTH);
        frameFaskes.add(scrollFaskes, java.awt.BorderLayout.CENTER);

        panelTengah.removeAll();
        panelTengah.setLayout(new javax.swing.BoxLayout(
                panelTengah, javax.swing.BoxLayout.Y_AXIS));
        panelTengah.setOpaque(true);
        panelTengah.setBackground(page);
        panelTengah.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelTengah.add(frameKriteria);
        panelTengah.add(javax.swing.Box.createVerticalStrut(8));
        panelTengah.add(frameFaskes);
        panelTengah.add(javax.swing.Box.createVerticalGlue());
        setupFaskesSearch();
        panelTengah.revalidate();
        panelTengah.repaint();
    }

    private javax.swing.JPanel createFaskesTableSectionHeader() {
        javax.swing.JPanel header = new javax.swing.JPanel(
                new java.awt.BorderLayout(12, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        header.setPreferredSize(new java.awt.Dimension(10, 52));

        javax.swing.JPanel textBox = new javax.swing.JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new javax.swing.BoxLayout(
                textBox, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Faskes Tujuan");
        title.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Pilih satu fasilitas kesehatan sebagai tujuan rujukan.");
        subtitle.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 10));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        textBox.add(title);
        textBox.add(subtitle);

        txtCariFaskesUi = new javax.swing.JTextField() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D background =
                        (java.awt.Graphics2D) g.create();
                background.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                background.setColor(java.awt.Color.WHITE);
                background.fillRoundRect(0, 0,
                        Math.max(0, getWidth() - 1),
                        Math.max(0, getHeight() - 1), 12, 12);
                background.dispose();

                super.paintComponent(g);
                if (getText().isEmpty()) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(
                            java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(new java.awt.Color(148, 163, 184));
                    java.awt.FontMetrics metrics = g2.getFontMetrics();
                    int y = (getHeight() - metrics.getHeight()) / 2
                            + metrics.getAscent();
                    g2.drawString(
                            "Cari kode, RS, kelas/strata, atau kota...",
                            getInsets().left, y);
                    g2.dispose();
                }
            }

            @Override
            protected void paintBorder(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner()
                        ? new java.awt.Color(96, 165, 250)
                        : new java.awt.Color(203, 213, 225));
                g2.drawRoundRect(0, 0,
                        Math.max(0, getWidth() - 1),
                        Math.max(0, getHeight() - 1), 12, 12);
                g2.dispose();
            }
        };
        txtCariFaskesUi.setOpaque(false);
        txtCariFaskesUi.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        txtCariFaskesUi.setForeground(new java.awt.Color(30, 41, 59));
        txtCariFaskesUi.setBackground(java.awt.Color.WHITE);
        txtCariFaskesUi.setCaretColor(new java.awt.Color(30, 41, 59));
        txtCariFaskesUi.setPreferredSize(new java.awt.Dimension(300, 29));
        txtCariFaskesUi.setToolTipText(
                "Cari berdasarkan Kode SatuSehat, Kode PPK, Nama RS, "
                + "Kelas, Strata, atau Kab/Kota");
        txtCariFaskesUi.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                4, 10, 4, 10));
        txtCariFaskesUi.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                txtCariFaskesUi.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                txtCariFaskesUi.repaint();
            }
        });

        javax.swing.JPanel searchBox = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 4));
        searchBox.setOpaque(false);
        searchBox.add(txtCariFaskesUi);

        javax.swing.JPanel badgeBox = new javax.swing.JPanel(
                new java.awt.GridBagLayout());
        badgeBox.setOpaque(false);
        badgeBox.add(lblJumlahFaskesUi);

        header.add(textBox, java.awt.BorderLayout.WEST);
        header.add(searchBox, java.awt.BorderLayout.CENTER);
        header.add(badgeBox, java.awt.BorderLayout.EAST);
        return header;
    }

    private void setupFaskesSearch() {
        sorterFaskesUi = new javax.swing.table.TableRowSorter<>(modelFaskes);
        tblFaskes.setRowSorter(sorterFaskesUi);
        if (txtCariFaskesUi == null) return;

        txtCariFaskesUi.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            private void changed() {
                applyFaskesSearchFilter();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });
    }

    private void applyFaskesSearchFilter() {
        if (sorterFaskesUi == null || txtCariFaskesUi == null) return;

        String keyword = txtCariFaskesUi.getText().trim().toLowerCase(
                java.util.Locale.ROOT);
        if (keyword.isEmpty()) {
            sorterFaskesUi.setRowFilter(null);
        } else {
            final String[] tokens = keyword.split("\\s+");
            sorterFaskesUi.setRowFilter(
                    new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel,
                        ? extends Integer> entry) {
                    StringBuilder searchable = new StringBuilder();
                    // Kode SatuSehat, Kode PPK, Nama RS, Kelas, Strata, Kab/Kota.
                    for (int column = 1; column <= 6; column++) {
                        searchable.append(' ')
                                .append(entry.getStringValue(column));
                    }
                    String value = searchable.toString().toLowerCase(
                            java.util.Locale.ROOT);
                    for (String token : tokens) {
                        if (!value.contains(token)) return false;
                    }
                    return true;
                }
            });
        }
        updateDynamicTableHeights();
    }

    private javax.swing.JPanel createCriteriaTableSectionHeader() {
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        header.setPreferredSize(new java.awt.Dimension(10, 52));

        javax.swing.JPanel textBox = new javax.swing.JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new javax.swing.BoxLayout(textBox, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Kriteria Rujukan");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Pilih satu kriteria atau nonaktifkan sinkronisasi untuk input manual.");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));

        textBox.add(title);
        textBox.add(subtitle);

        chkSinkronKriteriaUi = new javax.swing.JCheckBox("Sinkron otomatis", true);
        chkSinkronKriteriaUi.setOpaque(false);
        chkSinkronKriteriaUi.setFocusPainted(false);
        chkSinkronKriteriaUi.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 10));
        chkSinkronKriteriaUi.setForeground(new java.awt.Color(51, 65, 85));
        chkSinkronKriteriaUi.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.HAND_CURSOR));
        chkSinkronKriteriaUi.setToolTipText(
                "Hilangkan centang untuk mengisi ketiga kriteria secara manual");
        chkSinkronKriteriaUi.addActionListener(
                e -> handleCriteriaSynchronizationToggle());

        javax.swing.JPanel actionBox = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        actionBox.setOpaque(false);
        actionBox.add(chkSinkronKriteriaUi);
        actionBox.add(lblJumlahKriteriaUi);
        header.add(textBox, java.awt.BorderLayout.CENTER);
        header.add(actionBox, java.awt.BorderLayout.EAST);
        return header;
    }

    private javax.swing.JLabel createTableCountBadge(String text) {
        javax.swing.JLabel badge = new javax.swing.JLabel(text);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        badge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        badge.setForeground(new java.awt.Color(30, 64, 175));
        badge.setBackground(new java.awt.Color(219, 234, 254));
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 9, 3, 9));
        return badge;
    }

    private void styleModernDataTable(javax.swing.JTable table) {
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        table.setRowHeight(30);
        table.setBackground(java.awt.Color.WHITE);
        table.setForeground(new java.awt.Color(30, 41, 59));
        table.setSelectionBackground(new java.awt.Color(219, 234, 254));
        table.setSelectionForeground(new java.awt.Color(30, 64, 175));
        table.setGridColor(new java.awt.Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(false);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(
                    new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
            table.getTableHeader().setBackground(new java.awt.Color(241, 245, 249));
            table.getTableHeader().setForeground(new java.awt.Color(51, 65, 85));
            table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));
            table.getTableHeader().setReorderingAllowed(false);

            javax.swing.table.DefaultTableCellRenderer headerRenderer =
                    new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        javax.swing.JTable headerTable, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    java.awt.Component rendered = super.getTableCellRendererComponent(
                            headerTable, value, isSelected, hasFocus, row, column);
                    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return rendered;
                }
            };
            headerRenderer.setOpaque(true);
            headerRenderer.setFont(
                    new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
            headerRenderer.setBackground(new java.awt.Color(241, 245, 249));
            headerRenderer.setForeground(new java.awt.Color(51, 65, 85));
            headerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
        }
    }

    /** Menyamakan titik awal nilai linkId dengan padding judul kolomnya. */
    private void setupLinkIdCellPadding() {
        if (tblKriteria == null
                || tblKriteria.getColumnModel().getColumnCount() == 0) return;

        final javax.swing.table.TableCellRenderer baseRenderer =
                tblKriteria.getDefaultRenderer(Object.class);
        javax.swing.table.DefaultTableCellRenderer paddedRenderer =
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                java.awt.Component styled = baseRenderer
                        .getTableCellRendererComponent(table, value, isSelected,
                                hasFocus, row, column);
                super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                setBackground(styled.getBackground());
                setForeground(styled.getForeground());
                setFont(styled.getFont());
                setEnabled(styled.isEnabled());
                setOpaque(!(styled instanceof javax.swing.JComponent)
                        || ((javax.swing.JComponent) styled).isOpaque());
                if (styled instanceof javax.swing.JLabel) {
                    setHorizontalAlignment(((javax.swing.JLabel) styled)
                            .getHorizontalAlignment());
                    setVerticalAlignment(((javax.swing.JLabel) styled)
                            .getVerticalAlignment());
                }
                setBorder(javax.swing.BorderFactory.createEmptyBorder(
                        0, 10, 0, 4));
                return this;
            }
        };
        tblKriteria.getColumnModel().getColumn(0)
                .setCellRenderer(paddedRenderer);
    }

    /**
     * Klik di bagian mana pun pada baris Faskes langsung menetapkan checkbox
     * Pilih. Perubahan baris melalui keyboard mendapat perilaku yang sama.
     */
    private void setupFaskesRowAutoSelection() {
        tblFaskes.setRowSelectionAllowed(true);
        tblFaskes.setColumnSelectionAllowed(false);
        tblFaskes.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tblFaskes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                javax.swing.SwingUtilities.invokeLater(
                        this::checkSelectedFaskesRow);
            }
        });
        tblFaskes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)
                        && tblFaskes.rowAtPoint(e.getPoint()) >= 0) {
                    javax.swing.SwingUtilities.invokeLater(
                            BPJSRujukanSatuSehat.this::checkSelectedFaskesRow);
                }
            }
        });
    }

    private void checkSelectedFaskesRow() {
        if (restoringReferralSnapshot) return;
        int viewRow = tblFaskes.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tblFaskes.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= modelFaskes.getRowCount()) return;
        if (!Boolean.TRUE.equals(modelFaskes.getValueAt(modelRow, 0))) {
            modelFaskes.setValueAt(Boolean.TRUE, modelRow, 0);
        }
    }

    private void styleModernScrollPane(javax.swing.JScrollPane scroll) {
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(java.awt.Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(9, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 9));
    }

    /**
     * Lebar kolom pertanyaan mengikuti teks terpanjang yang benar-benar tampil.
     * Min/max dibuat sama agar AUTO_RESIZE_OFF tidak melebarkan kolom kembali.
     */
    private void adjustKriteriaQuestionColumnWidth() {
        if (tblKriteria == null
                || tblKriteria.getColumnModel().getColumnCount() < 2) return;

        TableColumn questionColumn = tblKriteria.getColumnModel().getColumn(1);
        String headerText = questionColumn.getHeaderValue() == null
                ? "Pertanyaan Kriteria"
                : questionColumn.getHeaderValue().toString();
        java.awt.Font headerFont = tblKriteria.getTableHeader() == null
                ? tblKriteria.getFont()
                : tblKriteria.getTableHeader().getFont();
        java.awt.FontMetrics headerMetrics = tblKriteria.getFontMetrics(headerFont);
        int contentWidth = headerMetrics.stringWidth(headerText) + 24;
        java.awt.FontMetrics metrics = tblKriteria.getFontMetrics(tblKriteria.getFont());
        for (int row = 0; row < modelKriteria.getRowCount(); row++) {
            Object value = modelKriteria.getValueAt(row, 1);
            String text = value == null ? "" : value.toString();
            for (String line : text.split("\\r?\\n", -1)) {
                contentWidth = Math.max(contentWidth, metrics.stringWidth(line) + 24);
            }
        }

        questionColumn.setMaxWidth(Integer.MAX_VALUE);
        questionColumn.setMinWidth(15);
        questionColumn.setPreferredWidth(contentWidth);
        questionColumn.setWidth(contentWidth);
        questionColumn.setMinWidth(contentWidth);
        questionColumn.setMaxWidth(contentWidth);
    }

    /**
     * Setiap kolom Faskes mengikuti teks terpanjang pada hasil API, dengan
     * lebar judul kolom sebagai batas minimum.
     */
    private void adjustFaskesColumnWidths() {
        if (tblFaskes == null || tblFaskes.getColumnModel() == null) return;

        java.awt.Font headerFont = tblFaskes.getTableHeader() == null
                ? tblFaskes.getFont()
                : tblFaskes.getTableHeader().getFont();
        java.awt.FontMetrics headerMetrics =
                tblFaskes.getFontMetrics(headerFont);
        java.awt.FontMetrics valueMetrics =
                tblFaskes.getFontMetrics(tblFaskes.getFont());

        for (int viewColumn = 0;
                viewColumn < tblFaskes.getColumnModel().getColumnCount();
                viewColumn++) {
            TableColumn column = tblFaskes.getColumnModel()
                    .getColumn(viewColumn);
            int modelColumn = column.getModelIndex();
            String headerText = column.getHeaderValue() == null
                    ? "" : column.getHeaderValue().toString();
            int contentWidth = headerMetrics.stringWidth(headerText) + 24;

            for (int row = 0; row < modelFaskes.getRowCount(); row++) {
                Object value = modelFaskes.getValueAt(row, modelColumn);
                String text = value == null ? "" : value.toString();
                for (String line : text.split("\\r?\\n", -1)) {
                    contentWidth = Math.max(contentWidth,
                            valueMetrics.stringWidth(line) + 24);
                }
            }

            contentWidth = Math.max(40, contentWidth);
            column.setMaxWidth(Integer.MAX_VALUE);
            column.setMinWidth(contentWidth);
            column.setPreferredWidth(contentWidth);
            column.setWidth(contentWidth);
        }
    }

    /**
     * Kolom jawaban mengisi sisa lebar viewport agar tombol Cari ICD-9 pada
     * baris Tindakan Medis selalu berada di sisi kanan card Kriteria Rujukan.
     */
    private void adjustKriteriaAnswerColumnWidth() {
        if (tblKriteria == null || scrollKriteria == null
                || tblKriteria.getColumnModel().getColumnCount() < 4) return;

        int viewportWidth = scrollKriteria.getViewport().getExtentSize().width;
        if (viewportWidth <= 0) viewportWidth = scrollKriteria.getWidth();
        if (viewportWidth <= 0) return;

        int occupiedWidth = 0;
        for (int column = 0; column < 3; column++) {
            occupiedWidth += tblKriteria.getColumnModel()
                    .getColumn(column).getWidth();
        }

        TableColumn answerColumn = tblKriteria.getColumnModel().getColumn(3);
        String headerText = answerColumn.getHeaderValue() == null
                ? "Jawaban"
                : answerColumn.getHeaderValue().toString();
        java.awt.Font headerFont = tblKriteria.getTableHeader() == null
                ? tblKriteria.getFont()
                : tblKriteria.getTableHeader().getFont();
        int minimumWidth = tblKriteria.getFontMetrics(headerFont)
                .stringWidth(headerText) + 24;
        int targetWidth = Math.max(minimumWidth, viewportWidth - occupiedWidth);

        answerColumn.setMaxWidth(Integer.MAX_VALUE);
        answerColumn.setMinWidth(minimumWidth);
        answerColumn.setPreferredWidth(targetWidth);
        answerColumn.setWidth(targetWidth);
    }

    /**
     * Kriteria dan card Faskes yang masih kosong tetap ringkas. Setelah hasil
     * Faskes tersedia, garis bawahnya disejajarkan dengan Catatan Tambahan.
     */
    private void updateDynamicTableHeights() {
        updateTableSectionHeight(frameKriteria, scrollKriteria, tblKriteria);
        if (modelFaskes.getRowCount() > 0) {
            updateFaskesSectionToNotesBottom();
        } else {
            // Sebelum hasil pencarian tersedia, card tetap ringkas seperti semula.
            updateTableSectionHeight(frameFaskes, scrollFaskes, tblFaskes);
        }

        if (lblJumlahKriteriaUi != null) {
            int count = modelKriteria.getRowCount();
            lblJumlahKriteriaUi.setText(count + " item");
        }
        updateFaskesCountBadge();

        panelTengah.revalidate();
        panelTengah.repaint();
    }

    /**
     * Saat hasil faskes tersedia, panjangkan card hingga garis bawahnya sejajar
     * dengan garis bawah card Catatan Tambahan di kolom kiri.
     */
    private void updateFaskesSectionToNotesBottom() {
        int targetHeight = calculateFaskesBottomAlignedHeight();
        if (targetHeight <= 0) {
            updateTableSectionHeight(frameFaskes, scrollFaskes, tblFaskes);
            return;
        }

        java.awt.LayoutManager layout = frameFaskes.getLayout();
        java.awt.Component header = layout instanceof java.awt.BorderLayout
                ? ((java.awt.BorderLayout) layout)
                        .getLayoutComponent(java.awt.BorderLayout.NORTH)
                : null;
        int headerHeight = header == null
                ? 52
                : Math.max(header.getHeight(), header.getPreferredSize().height);
        int scrollHeight = Math.max(64, targetHeight - headerHeight - 2);

        scrollFaskes.setMinimumSize(new java.awt.Dimension(100, scrollHeight));
        scrollFaskes.setPreferredSize(new java.awt.Dimension(100, scrollHeight));
        frameFaskes.setMinimumSize(new java.awt.Dimension(100, targetHeight));
        frameFaskes.setPreferredSize(new java.awt.Dimension(100, targetHeight));
        frameFaskes.setMaximumSize(new java.awt.Dimension(
                Integer.MAX_VALUE, targetHeight));
    }

    private int calculateFaskesBottomAlignedHeight() {
        if (notesCardUi == null || notesCardUi.getParent() == null
                || frameFaskes.getParent() == null || frameMain == null) {
            return -1;
        }

        int notesHeight = notesCardUi.getHeight() > 0
                ? notesCardUi.getHeight()
                : notesCardUi.getPreferredSize().height;
        try {
            java.awt.Point faskesTop = javax.swing.SwingUtilities.convertPoint(
                    frameFaskes, 0, 0, frameMain);
            java.awt.Point notesBottom = javax.swing.SwingUtilities.convertPoint(
                    notesCardUi, 0, notesHeight, frameMain);
            int targetHeight = notesBottom.y - faskesTop.y;
            return targetHeight >= 150 ? targetHeight : -1;
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    private void updateFaskesCountBadge() {
        if (lblJumlahFaskesUi == null) return;

        int total = modelFaskes.getRowCount();
        int visible = tblFaskes.getRowCount();
        boolean filtered = sorterFaskesUi != null
                && sorterFaskesUi.getRowFilter() != null;
        lblJumlahFaskesUi.setText(filtered
                ? visible + " / " + total + " faskes"
                : total + " faskes");
    }

    private void updateTableSectionHeight(javax.swing.JComponent section,
            javax.swing.JScrollPane scroll, javax.swing.JTable table) {
        int visibleRows = Math.max(1, Math.min(table.getRowCount(), 6));
        int headerHeight = table.getTableHeader() == null
                ? 32 : Math.max(32, table.getTableHeader().getPreferredSize().height);
        int rowsHeight = 0;
        for (int row = 0; row < visibleRows; row++) {
            rowsHeight += row < table.getRowCount()
                    ? table.getRowHeight(row) : table.getRowHeight();
        }
        int horizontalBarHeight = Math.max(9,
                scroll.getHorizontalScrollBar().getPreferredSize().height);
        int scrollHeight = headerHeight + rowsHeight + horizontalBarHeight + 3;
        int sectionHeight = 52 + scrollHeight + 2;

        scroll.setMinimumSize(new java.awt.Dimension(100, scrollHeight));
        scroll.setPreferredSize(new java.awt.Dimension(100, scrollHeight));
        section.setMinimumSize(new java.awt.Dimension(100, sectionHeight));
        section.setPreferredSize(new java.awt.Dimension(100, sectionHeight));
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, sectionHeight));
    }

    private static final class ModernScrollBarUI
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            trackColor = new java.awt.Color(248, 250, 252);
            thumbColor = new java.awt.Color(203, 213, 225);
            thumbDarkShadowColor = thumbColor;
            thumbHighlightColor = thumbColor;
            thumbLightShadowColor = thumbColor;
        }

        @Override
        protected javax.swing.JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private javax.swing.JButton createZeroButton() {
            javax.swing.JButton button = new javax.swing.JButton();
            java.awt.Dimension zero = new java.awt.Dimension(0, 0);
            button.setPreferredSize(zero);
            button.setMinimumSize(zero);
            button.setMaximumSize(zero);
            return button;
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c,
                java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover()
                    ? new java.awt.Color(148, 163, 184) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    Math.max(3, thumbBounds.width - 4),
                    Math.max(3, thumbBounds.height - 4), 8, 8);
            g2.dispose();
        }
    }

    private void setupActionTombol() {
        btnPilihDiagnosa.addActionListener(e -> doPilihDiagnosa());
        btnPilihPoli.addActionListener(e -> doPilihPoli());
        btnBuatEncounter.addActionListener(e -> doBuatEncounter());
        btnCekKriteria.addActionListener(e -> doCekKriteria());
        btnCariFaskes.addActionListener(e -> doCariFaskes());
        btnKirim.addActionListener(e -> doKirimRujukan());
        btnCetakSepRujukanUi.addActionListener(
                e -> showSepReferralReportPreview());
        btnHapus.addActionListener(e -> doHapusRujukan());
        btnPulihkanRujukanUi.addActionListener(e -> showRecoverReferralDialog());
        btnResponApi.addActionListener(e -> showApiResponseDialog());
        btnTutup.addActionListener(e -> dispose());
    }

    private void addLabel(String text, int x, int y, int w, int h) {
        widget.Label l = new widget.Label();
        l.setText(text);
        l.setBounds(x, y, w, h);
        panelData.add(l);
    }

    private widget.TextBox roField(int x, int y, int w, int h) {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        t.setBackground(new java.awt.Color(245, 250, 240));
        t.setBounds(x, y, w, h);
        panelData.add(t);
        return t;
    }

    private widget.Button mkBtn(String text, String icon) {
        widget.Button b = new widget.Button();
        b.setText(text);
        try {
            b.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon)));
        } catch (Exception ignore) {}
        b.setPreferredSize(new Dimension(140, 30));
//        b.setGlassColor(new java.awt.Color(255, 255, 255));
        return b;
    }

    /**
     * Menjaga urutan tombol proses di kiri. Pulihkan dan Respon API ditempatkan
     * berdampingan di sisi kanan agar tidak memutus alur utama rujukan.
     */
    private void setupPanelTombolModern() {
        javax.swing.JPanel panelKiri = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));
        javax.swing.JPanel panelKanan = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 9));

        panelKiri.setOpaque(false);
        panelKanan.setOpaque(false);

        panelTombol.removeAll();
        panelTombol.setLayout(new java.awt.BorderLayout());
        panelTombol.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, new java.awt.Color(226, 232, 240)));

        panelKiri.add(btnCekKriteria);
        panelKiri.add(btnCariFaskes);
        panelKiri.add(btnKirim);

        btnCetakSepRujukanUi.setText("Cetak SEP Rujukan");
        try {
            btnCetakSepRujukanUi.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/picture/b_print.png")));
        } catch (Exception ignore) {
        }
        btnCetakSepRujukanUi.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        btnCetakSepRujukanUi.setPreferredSize(
                new java.awt.Dimension(155, 30));
        btnCetakSepRujukanUi.setVisible(false);
        panelKiri.add(btnCetakSepRujukanUi);

        btnHapus.setVisible(false);
        panelKiri.add(btnHapus);

        btnPulihkanRujukanUi.setText("");
        java.net.URL redoIcon = getClass().getResource("/picture/redo.png");
        btnPulihkanRujukanUi.setIcon(redoIcon == null
                ? new RecoveryButtonIcon(new java.awt.Color(37, 99, 235))
                : new javax.swing.ImageIcon(redoIcon));
        btnPulihkanRujukanUi.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        btnPulihkanRujukanUi.setPreferredSize(
                new java.awt.Dimension(120, 30));
        btnPulihkanRujukanUi.setToolTipText(
                "Tarik kembali rujukan lama dari BPJS/SATUSEHAT tanpa mengirim ulang");

        panelKiri.add(BtnPrint);
        panelKiri.add(btnTutup);

        btnResponApi.setIcon(new JsonButtonIcon());
        btnResponApi.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        btnResponApi.setToolTipText("Lihat seluruh riwayat respon API dalam format JSON");
        panelKanan.add(btnPulihkanRujukanUi);
        panelKanan.add(btnResponApi);

        panelTombol.add(panelKiri, java.awt.BorderLayout.CENTER);
        panelTombol.add(panelKanan, java.awt.BorderLayout.EAST);
        panelTombol.revalidate();
        panelTombol.repaint();
    }

    /**
     * Header ringkas agar konteks form langsung terbaca tanpa mengurangi area
     * kerja secara berlebihan.
     */
    private void setupFormHeader() {
        if (Boolean.TRUE.equals(frameMain.getClientProperty("bpjs.main.header"))) return;
        frameMain.putClientProperty("bpjs.main.header", Boolean.TRUE);

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setPreferredSize(new java.awt.Dimension(10, 44));
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(5, 12, 5, 12)));

        javax.swing.JPanel accent = new javax.swing.JPanel();
        accent.setBackground(new java.awt.Color(37, 99, 235));
        accent.setPreferredSize(new java.awt.Dimension(4, 10));

        javax.swing.JPanel heading = new javax.swing.JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Integrasi Rujukan Satu Sehat");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 14));
        title.setForeground(new java.awt.Color(15, 23, 42));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Rujukan keluar terintegrasi BPJS VClaim dan SATUSEHAT");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));

        heading.add(title);
        heading.add(subtitle);

        javax.swing.JLabel integrationBadge = new javax.swing.JLabel("BPJS  \u2022  SATUSEHAT");
        integrationBadge.setOpaque(true);
        integrationBadge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        integrationBadge.setForeground(new java.awt.Color(30, 64, 175));
        integrationBadge.setBackground(new java.awt.Color(239, 246, 255));
        integrationBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));

        javax.swing.JPanel badgeBox = new javax.swing.JPanel(new java.awt.GridBagLayout());
        badgeBox.setOpaque(false);
        badgeBox.add(integrationBadge);

        header.add(accent, java.awt.BorderLayout.WEST);
        header.add(heading, java.awt.BorderLayout.CENTER);
        header.add(badgeBox, java.awt.BorderLayout.EAST);
        frameMain.add(header, java.awt.BorderLayout.NORTH);
        frameMain.revalidate();
        frameMain.repaint();
    }

    /**
     * Menata ulang panel kiri dengan bahasa visual yang sama seperti panel
     * Kriteria/Faskes. Seluruh field, combo, tanggal, tombol, dan text area lama
     * dipakai kembali agar event serta alur datanya tidak berubah.
     */
    private void setupModernPatientForm() {
        if (Boolean.TRUE.equals(panelData.getClientProperty("bpjs.patient.form.modernized"))) {
            return;
        }
        panelData.putClientProperty("bpjs.patient.form.modernized", Boolean.TRUE);

        styleModernPatientComponents();

        javax.swing.JComponent noRujukanBpjsField = createCopyableReferralField(
                tNoRujukanBpjs, true);
        javax.swing.JComponent noRujukanSatuSehatField = createCopyableReferralField(
                tNoRujukanSatuSehat, false);

        javax.swing.JPanel identityBody = createFormRowsPanel(
                createDualFormRow(lblNoSep, tNoSep, lblNoSep1, noRujukanBpjsField),
                createDualFormRow(lblNoRawat, tNoRawat, lblNoSep2, noRujukanSatuSehatField),
                createSingleFormRow(lblPasien, tPasien),
                createEncounterFormRow(),
                createDualFormRow(lblIhsPasien, tIdPasienIhs,
                        lblIhsDokter, tIdDokterIhs));

        javax.swing.JPanel referralBody = createFormRowsPanel(
                createPickerFormRow(lblDiag, tKdDiagnosaRujuk,
                        tNmDiagnosaRujuk, btnPilihDiagnosa),
                createPickerFormRow(lblPoli, tKdPoliRujuk,
                        tNmPoliRujuk, btnPilihPoli),
                createDualFormRow(lblJnsPelayanan, cbJnsPelayanan,
                        lblTipeRujukan, cbTipeRujukan),
                createDualFormRow(lblTglRujukan, dtTglRujukan,
                        lblTglRencana, dtTglRencana),
                createSingleFormRow(lblProvinsi, cbProvinsi));

        javax.swing.JPanel notesBody = createNotesBody();

        javax.swing.JPanel identityCard = createModernFormCard(
                "Identitas Pasien",
                "Data kunjungan dan identitas integrasi SATUSEHAT",
                identityBody, 214);
        javax.swing.JPanel referralCard = createModernFormCard(
                "Detail Rujukan",
                "Diagnosa, poli, jenis layanan, dan jadwal rujukan",
                referralBody, 214);
        notesCardUi = createModernFormCard(
                "Catatan Tambahan",
                "Catatan klinis dan keterangan pendukung rujukan",
                notesBody, 150);

        // GridBag dipakai agar setiap card selalu mengisi lebar panel dari kiri.
        // BoxLayout sebelumnya dapat membentuk alignment axis dari komponen badge
        // yang lebih kecil sehingga card terdorong ke kanan dan menyisakan ruang.
        javax.swing.JPanel stack = new javax.swing.JPanel(new java.awt.GridBagLayout());
        stack.setOpaque(false);
        stack.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);

        java.awt.GridBagConstraints stackGbc = new java.awt.GridBagConstraints();
        stackGbc.gridx = 0;
        stackGbc.weightx = 1;
        stackGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        stackGbc.anchor = java.awt.GridBagConstraints.NORTHWEST;

        stackGbc.gridy = 0;
        stackGbc.insets = new java.awt.Insets(0, 0, 8, 0);
        stack.add(identityCard, stackGbc);

        stackGbc.gridy = 1;
        stack.add(referralCard, stackGbc);

        stackGbc.gridy = 2;
        stackGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        stack.add(notesCardUi, stackGbc);

        javax.swing.JPanel filler = new javax.swing.JPanel();
        filler.setOpaque(false);
        stackGbc.gridy = 3;
        stackGbc.weighty = 1;
        stackGbc.fill = java.awt.GridBagConstraints.BOTH;
        stack.add(filler, stackGbc);

        panelData.removeAll();
        panelData.setLayout(new java.awt.BorderLayout());
        panelData.setOpaque(true);
        panelData.setBackground(new java.awt.Color(248, 250, 252));
        panelData.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 4));
        panelData.setPreferredSize(new java.awt.Dimension(590, 250));
        panelData.add(stack, java.awt.BorderLayout.CENTER);
        panelData.revalidate();
        panelData.repaint();

        // Jalankan setelah kedua kolom selesai dilayout agar koordinat garis
        // bawah Catatan Tambahan sudah dapat dipakai oleh card Faskes.
        javax.swing.SwingUtilities.invokeLater(this::updateDynamicTableHeights);

        updateStatusProcessStyle(tStatus.getText(), false);
        updateReferralActionState();
    }

    /**
     * Membungkus field nomor rujukan dengan tombol copy yang tetap berada di
     * dalam garis field. Field asli tetap dipakai sehingga binding lama aman.
     */
    private javax.swing.JComponent createCopyableReferralField(
            final javax.swing.JTextField field, final boolean bpjsNumber) {
        final java.awt.Color background = new java.awt.Color(239, 246, 255);
        final java.awt.Color border = new java.awt.Color(191, 219, 254);

        javax.swing.JPanel wrapper = new javax.swing.JPanel(
                new java.awt.BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                try {
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(background);
                    g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                            Math.max(0, getHeight() - 1), 10, 10);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                try {
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1),
                            Math.max(0, getHeight() - 1), 10, 10);
                } finally {
                    g2.dispose();
                }
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        wrapper.setMinimumSize(new java.awt.Dimension(40, 28));
        wrapper.setPreferredSize(new java.awt.Dimension(140, 28));

        field.setOpaque(false);
        field.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 3));
        field.setBackground(background);

        javax.swing.JButton copy = new javax.swing.JButton(
                new javax.swing.ImageIcon(getClass().getResource("/picture/copy_.png")));
        copy.setPreferredSize(new java.awt.Dimension(32, 26));
        copy.setMinimumSize(new java.awt.Dimension(32, 26));
        copy.setMaximumSize(new java.awt.Dimension(32, 26));
        copy.setBackground(background);
        copy.setForeground(new java.awt.Color(37, 99, 235));
        copy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 5));
        copy.setContentAreaFilled(false);
        copy.setBorderPainted(false);
        copy.setFocusPainted(false);
        copy.setOpaque(false);
        copy.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.HAND_CURSOR));
        copy.setToolTipText(bpjsNumber
                ? "Salin No. RJK BPJS" : "Salin No. RJK SatuSehat");
        copy.getAccessibleContext().setAccessibleName(copy.getToolTipText());
        copy.addActionListener(e -> copyReferralNumber(field, copy.getToolTipText()));

        if (bpjsNumber) {
            btnCopyNoRujukanBpjsUi = copy;
        } else {
            btnCopyNoRujukanSatuSehatUi = copy;
        }

        wrapper.add(field, java.awt.BorderLayout.CENTER);
        wrapper.add(copy, java.awt.BorderLayout.EAST);
        return wrapper;
    }

    private void copyReferralNumber(javax.swing.JTextField field, String description) {
        String value = safe(field == null ? "" : field.getText());
        if (value.isEmpty()) {
            showModernToast(this, "Nomor rujukan belum tersedia.",
                    ToastMessage.WARNING, 0);
            return;
        }

        try {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(value);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(selection, null);
            showModernToast(this, description + " berhasil: " + value,
                    ToastMessage.SUCCESS, 0);
        } catch (Exception e) {
            showModernToast(this, "Gagal menyalin nomor rujukan: "
                    + safe(e.getMessage()), ToastMessage.ERROR, 0);
        }
    }

    private javax.swing.JPanel createModernFormCard(String titleText,
            String subtitleText, javax.swing.JPanel body, int height) {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout());
        card.setOpaque(true);
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(226, 232, 240)));
        card.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        card.add(createModernFormSectionHeader(titleText, subtitleText),
                java.awt.BorderLayout.NORTH);
        card.add(body, java.awt.BorderLayout.CENTER);
        card.setMinimumSize(new java.awt.Dimension(100, height));
        card.setPreferredSize(new java.awt.Dimension(100, height));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, height));
        return card;
    }

    private javax.swing.JPanel createModernFormSectionHeader(String titleText,
            String subtitleText) {
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setBackground(java.awt.Color.WHITE);
        header.setPreferredSize(new java.awt.Dimension(10, 40));
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12)));

        javax.swing.JPanel textBox = new javax.swing.JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new javax.swing.BoxLayout(textBox, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel(titleText);
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(subtitleText);
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));

        textBox.add(title);
        textBox.add(subtitle);
        header.add(textBox, java.awt.BorderLayout.CENTER);
        return header;
    }

    private javax.swing.JPanel createFormRowsPanel(javax.swing.JPanel... rows) {
        javax.swing.JPanel body = new javax.swing.JPanel();
        body.setOpaque(true);
        body.setBackground(java.awt.Color.WHITE);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
        for (int i = 0; i < rows.length; i++) {
            body.add(rows[i]);
            if (i < rows.length - 1) {
                body.add(javax.swing.Box.createVerticalStrut(5));
            }
        }
        return body;
    }

    private javax.swing.JPanel createDualFormRow(javax.swing.JLabel leftLabel,
            javax.swing.JComponent leftComponent, javax.swing.JLabel rightLabel,
            javax.swing.JComponent rightComponent) {
        javax.swing.JPanel row = createBaseFormRow();
        java.awt.GridBagConstraints gbc = baseFormConstraints();

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        row.add(createFormLabelCell(leftLabel), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        leftComponent.setPreferredSize(new java.awt.Dimension(140, 28));
        rightComponent.setPreferredSize(new java.awt.Dimension(140, 28));
        row.add(leftComponent, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(0, 18, 0, 8);
        row.add(createFormLabelCell(rightLabel), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        row.add(rightComponent, gbc);
        return row;
    }

    private javax.swing.JPanel createSingleFormRow(javax.swing.JLabel label,
            javax.swing.JComponent component) {
        javax.swing.JPanel row = createBaseFormRow();
        java.awt.GridBagConstraints gbc = baseFormConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        row.add(createFormLabelCell(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 3;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        row.add(component, gbc);
        return row;
    }

    private javax.swing.JPanel createEncounterFormRow() {
        javax.swing.JPanel row = createBaseFormRow();
        java.awt.GridBagConstraints gbc = baseFormConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        row.add(createFormLabelCell(lblEncounter), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 10);
        row.add(tEncounter, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        row.add(btnBuatEncounter, gbc);
        return row;
    }

    private javax.swing.JPanel createPickerFormRow(javax.swing.JLabel label,
            javax.swing.JComponent codeField, javax.swing.JComponent nameField,
            javax.swing.AbstractButton pickerButton) {
        javax.swing.JPanel row = createBaseFormRow();
        java.awt.GridBagConstraints gbc = baseFormConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        row.add(createFormLabelCell(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        codeField.setPreferredSize(new java.awt.Dimension(68, 28));
        codeField.setMinimumSize(new java.awt.Dimension(68, 28));
        codeField.setMaximumSize(new java.awt.Dimension(68, 28));
        row.add(codeField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 6);
        row.add(nameField, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        row.add(pickerButton, gbc);
        return row;
    }

    private javax.swing.JPanel createBaseFormRow() {
        javax.swing.JPanel row = new javax.swing.JPanel(new java.awt.GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        row.setMinimumSize(new java.awt.Dimension(100, 28));
        row.setPreferredSize(new java.awt.Dimension(100, 28));
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 28));
        return row;
    }

    /**
     * Wrapper ini membuat titik awal seluruh textbox/combobox konsisten.
     * JLabel custom Khanza memiliki preferred-size berbeda sesuai panjang teks,
     * sehingga tanpa wrapper setiap baris terlihat bergeser.
     */
    private javax.swing.JPanel createFormLabelCell(javax.swing.JLabel label) {
        javax.swing.JPanel cell = new javax.swing.JPanel(new java.awt.BorderLayout());
        cell.setOpaque(false);
        cell.setPreferredSize(new java.awt.Dimension(100, 28));
        cell.setMinimumSize(new java.awt.Dimension(100, 28));
        cell.setMaximumSize(new java.awt.Dimension(100, 28));
        cell.add(label, java.awt.BorderLayout.CENTER);
        return cell;
    }

    private java.awt.GridBagConstraints baseFormConstraints() {
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private javax.swing.JPanel createNotesBody() {
        javax.swing.JPanel body = new javax.swing.JPanel(new java.awt.GridBagLayout());
        body.setBackground(java.awt.Color.WHITE);
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 8, 12));

        javax.swing.JPanel catatanBlock = createTextAreaBlock(lblCatatan, scrollCatatan);
        javax.swing.JPanel keteranganBlock = createTextAreaBlock(lblKet, scrollKeterangan);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 6);
        body.add(catatanBlock, gbc);

        gbc.gridx = 1;
        gbc.insets = new java.awt.Insets(0, 6, 0, 0);
        body.add(keteranganBlock, gbc);
        return body;
    }

    private javax.swing.JPanel createTextAreaBlock(javax.swing.JLabel label,
            javax.swing.JScrollPane scroll) {
        javax.swing.JPanel block = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        block.setOpaque(false);
        label.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        label.setForeground(new java.awt.Color(71, 85, 105));
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setPreferredSize(new java.awt.Dimension(10, 18));
        block.add(label, java.awt.BorderLayout.NORTH);
        block.add(scroll, java.awt.BorderLayout.CENTER);
        return block;
    }

    private void styleModernPatientComponents() {
        javax.swing.JLabel[] labels = {
            lblNoSep, lblNoSep1, lblNoRawat, lblNoSep2, lblPasien,
            lblEncounter, lblIhsPasien, lblIhsDokter, lblDiag, lblPoli,
            lblJnsPelayanan, lblTipeRujukan, lblTglRujukan,
            lblTglRencana, lblProvinsi
        };
        for (javax.swing.JLabel label : labels) {
            label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            label.setForeground(new java.awt.Color(71, 85, 105));
            label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            label.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
            label.setPreferredSize(new java.awt.Dimension(100, 28));
        }

        java.awt.Color white = java.awt.Color.WHITE;
        java.awt.Color readOnly = new java.awt.Color(248, 250, 252);
        java.awt.Color line = new java.awt.Color(203, 213, 225);
        java.awt.Color result = new java.awt.Color(239, 246, 255);
        java.awt.Color resultLine = new java.awt.Color(191, 219, 254);

        styleModernFormInput(tNoRawat, white, line);
        styleModernFormInput(tNmDiagnosaRujuk, white, line);
        styleModernFormInput(cbJnsPelayanan, white, line);
        styleModernFormInput(cbTipeRujukan, white, line);
        styleModernFormInput(dtTglRujukan, white, line);
        styleModernFormInput(dtTglRencana, white, line);
        styleModernFormInput(cbProvinsi, white, line);

        styleModernFormInput(tNoSep, readOnly, line);
        styleModernFormInput(tPasien, readOnly, line);
        styleModernFormInput(tEncounter, readOnly, line);
        styleModernFormInput(tIdPasienIhs, readOnly, line);
        styleModernFormInput(tIdDokterIhs, readOnly, line);
        styleModernFormInput(tKdDiagnosaRujuk, readOnly, line);
        styleModernFormInput(tKdPoliRujuk, readOnly, line);
        styleModernFormInput(tNmPoliRujuk, readOnly, line);
        styleModernFormInput(tNoRujukanBpjs, result, resultLine);
        styleModernFormInput(tNoRujukanSatuSehat, result, resultLine);

        stylePickerButton(btnPilihDiagnosa);
        stylePickerButton(btnPilihPoli);
        styleEncounterButton();

        styleModernTextArea(taCatatan, scrollCatatan);
        styleModernTextArea(taKeterangan, scrollKeterangan);
        styleModernTextArea(tStatus, scrollKeterangan1);
    }

    private void styleModernFormInput(javax.swing.JComponent component,
            java.awt.Color background, java.awt.Color borderColor) {
        component.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        component.setForeground(new java.awt.Color(30, 41, 59));
        component.setBackground(background);
        javax.swing.border.Border lineBorder =
                javax.swing.BorderFactory.createLineBorder(borderColor);

        if (component instanceof javax.swing.text.JTextComponent) {
            component.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    lineBorder,
                    javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            javax.swing.text.JTextComponent textComponent =
                    (javax.swing.text.JTextComponent) component;
            textComponent.setMargin(new java.awt.Insets(0, 0, 0, 0));

            if (textComponent instanceof javax.swing.JTextField) {
                ((javax.swing.JTextField) textComponent).setHorizontalAlignment(
                        javax.swing.SwingConstants.LEFT);
            }
        } else {
            component.setBorder(lineBorder);
        }

        java.awt.Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new java.awt.Dimension(
                Math.max(40, preferred.width), 28));
        component.setMinimumSize(new java.awt.Dimension(40, 28));
    }

    private void stylePickerButton(javax.swing.AbstractButton button) {
        button.setPreferredSize(new java.awt.Dimension(30, 28));
        button.setMinimumSize(new java.awt.Dimension(30, 28));
        button.setMaximumSize(new java.awt.Dimension(30, 28));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setToolTipText("Buka pencarian referensi");
    }

    private void styleEncounterButton() {
        btnBuatEncounter.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        btnBuatEncounter.setForeground(new java.awt.Color(30, 64, 175));
        btnBuatEncounter.setBackground(new java.awt.Color(239, 246, 255));
        btnBuatEncounter.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(191, 219, 254)));
        btnBuatEncounter.setPreferredSize(new java.awt.Dimension(132, 28));
        btnBuatEncounter.setMinimumSize(new java.awt.Dimension(132, 28));
        btnBuatEncounter.setFocusPainted(false);
        btnBuatEncounter.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnBuatEncounter.setCursor(
                java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnBuatEncounter.setOpaque(true);
        btnBuatEncounter.setContentAreaFilled(true);
    }

    private void styleModernTextArea(javax.swing.JTextArea textArea,
            javax.swing.JScrollPane scroll) {
        textArea.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        textArea.setForeground(new java.awt.Color(51, 65, 85));
        textArea.setBackground(java.awt.Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 7, 5, 7));
        scroll.setVerticalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleModernScrollPane(scroll);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(203, 213, 225)));
    }

    private void updateStatusProcessStyle(String message, boolean error) {
        java.awt.Color[] colors = resolveProcessStatusColors(message, error);
        java.awt.Color background = colors[0];
        java.awt.Color foreground = colors[1];
        java.awt.Color line = colors[2];

        tStatus.setBackground(background);
        tStatus.setForeground(foreground);
        scrollKeterangan1.getViewport().setBackground(background);
        scrollKeterangan1.setBorder(javax.swing.BorderFactory.createLineBorder(line));
    }

    private boolean isProcessingStatus(String message) {
        String normalized = safe(message).toLowerCase();
        return normalized.contains("memanggil")
                || normalized.contains("mengirim")
                || normalized.contains("menyiapkan")
                || normalized.contains("mencari")
                || normalized.contains("memproses");
    }

    private java.awt.Color[] resolveProcessStatusColors(String message, boolean error) {
        if (error) {
            return new java.awt.Color[]{
                new java.awt.Color(254, 242, 242),
                new java.awt.Color(185, 28, 28),
                new java.awt.Color(254, 202, 202)
            };
        }
        if (isProcessingStatus(message)) {
            return new java.awt.Color[]{
                new java.awt.Color(239, 246, 255),
                new java.awt.Color(30, 64, 175),
                new java.awt.Color(191, 219, 254)
            };
        }
        return new java.awt.Color[]{
            new java.awt.Color(240, 253, 244),
            new java.awt.Color(21, 128, 61),
            new java.awt.Color(187, 247, 208)
        };
    }

    /**
     * Memberi shell visual baru pada popup BPJSCekReferensiPenyakit. Isi,
     * pencarian, tombol, tabel, dan callback popup lama tetap digunakan.
     */
    private void setupModernDiagnosisPopup() {
        try {
            java.awt.Container originalContent = popupPenyakit.getContentPane();
            if (originalContent == null
                    || Boolean.TRUE.equals(popupPenyakit.getRootPane()
                            .getClientProperty("bpjs.diagnosis.modernized"))) {
                return;
            }

            popupPenyakit.getRootPane().putClientProperty("bpjs.diagnosis.modernized", Boolean.TRUE);
            popupPenyakit.setTitle("Referensi Diagnosa VClaim");
            configureRoundedPopupWindow(popupPenyakit, true);
            popupPenyakit.getRootPane().setBorder(
                    javax.swing.BorderFactory.createEmptyBorder());

            javax.swing.JPanel shell = new javax.swing.JPanel(new java.awt.BorderLayout());
            shell.setBackground(java.awt.Color.WHITE);
            shell.setBorder(new RoundedPopupBorder(
                    new java.awt.Color(203, 213, 225)));

            javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
            header.setBackground(new java.awt.Color(248, 250, 252));
            header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(
                            0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                    javax.swing.BorderFactory.createEmptyBorder(14, 18, 13, 12)));

            javax.swing.JPanel titleBox = new javax.swing.JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));

            javax.swing.JLabel title = new javax.swing.JLabel("Referensi Diagnosa VClaim");
            title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 15));
            title.setForeground(new java.awt.Color(30, 41, 59));

            javax.swing.JLabel subtitle = new javax.swing.JLabel(
                    "Cari berdasarkan kode ICD-10 atau nama penyakit, lalu pilih diagnosa rujukan.");
            subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            subtitle.setForeground(new java.awt.Color(100, 116, 139));
            subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 0, 0));

            titleBox.add(title);
            titleBox.add(subtitle);

            javax.swing.JButton close = createFlatCloseButton();
            close.setToolTipText("Tutup pencarian diagnosa");
            close.addActionListener(e -> popupPenyakit.dispose());

            header.add(titleBox, java.awt.BorderLayout.CENTER);
            header.add(close, java.awt.BorderLayout.EAST);

            if (originalContent instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) originalContent).setBorder(
                        javax.swing.BorderFactory.createEmptyBorder(10, 14, 14, 14));
            }

            popupPenyakit.setContentPane(shell);
            shell.add(header, java.awt.BorderLayout.NORTH);
            shell.add(originalContent, java.awt.BorderLayout.CENTER);

            modernizeDiagnosisTree(originalContent);
            styleDiagnosisTable(popupPenyakit.getTable());
            installDialogDragSupport(header, popupPenyakit);
        } catch (Exception e) {
            System.out.println("Gagal memperbarui tampilan popup diagnosa: " + e.getMessage());
        }
    }

    /**
     * Memberi shell visual yang sama pada popup BPJSCekReferensiPoli. Isi,
     * proses pencarian, pemilihan, dan callback popup asli tetap dipakai.
     */
    private void setupModernPoliPopup() {
        try {
            java.awt.Container originalContent = popupPoli.getContentPane();
            if (originalContent == null
                    || Boolean.TRUE.equals(popupPoli.getRootPane()
                            .getClientProperty("bpjs.poli.modernized"))) {
                return;
            }

            popupPoli.getRootPane().putClientProperty("bpjs.poli.modernized", Boolean.TRUE);
            popupPoli.setTitle("Referensi Poli/Unit VClaim");
            configureRoundedPopupWindow(popupPoli, true);
            popupPoli.getRootPane().setBorder(
                    javax.swing.BorderFactory.createEmptyBorder());

            javax.swing.JPanel shell = new javax.swing.JPanel(new java.awt.BorderLayout());
            shell.setBackground(java.awt.Color.WHITE);
            shell.setBorder(new RoundedPopupBorder(
                    new java.awt.Color(203, 213, 225)));

            javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
            header.setBackground(new java.awt.Color(248, 250, 252));
            header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(
                            0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                    javax.swing.BorderFactory.createEmptyBorder(14, 18, 13, 12)));

            javax.swing.JPanel titleBox = new javax.swing.JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));

            javax.swing.JLabel title = new javax.swing.JLabel("Referensi Poli/Unit VClaim");
            title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 15));
            title.setForeground(new java.awt.Color(30, 41, 59));

            javax.swing.JLabel subtitle = new javax.swing.JLabel(
                    "Cari berdasarkan kode atau nama poli/unit, lalu pilih tujuan rujukan.");
            subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            subtitle.setForeground(new java.awt.Color(100, 116, 139));
            subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 0, 0));

            titleBox.add(title);
            titleBox.add(subtitle);

            javax.swing.JButton close = createFlatCloseButton();
            close.setToolTipText("Tutup pencarian poli/unit");
            close.addActionListener(e -> popupPoli.dispose());

            header.add(titleBox, java.awt.BorderLayout.CENTER);
            header.add(close, java.awt.BorderLayout.EAST);

            if (originalContent instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) originalContent).setBorder(
                        javax.swing.BorderFactory.createEmptyBorder(10, 14, 14, 14));
            }

            popupPoli.setContentPane(shell);
            shell.add(header, java.awt.BorderLayout.NORTH);
            shell.add(originalContent, java.awt.BorderLayout.CENTER);

            modernizeDiagnosisTree(originalContent);
            styleDiagnosisTable(popupPoli.getTable());
            installDialogDragSupport(header, popupPoli);
        } catch (Exception e) {
            System.out.println("Gagal memperbarui tampilan popup poli: " + e.getMessage());
        }
    }

    private void modernizeDiagnosisTree(java.awt.Component component) {
        if (component == null) return;

        if (component instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) component;
            javax.swing.border.Border border = jc.getBorder();
            if (border instanceof javax.swing.border.TitledBorder) {
                String borderTitle = ((javax.swing.border.TitledBorder) border).getTitle();
                if (borderTitle != null) {
                    String normalizedTitle = borderTitle.toLowerCase();
                    if (normalizedTitle.contains("pencarian data referensi diagnosa")
                            || normalizedTitle.contains("pencarian data referensi poli")
                            || normalizedTitle.contains("pencarian data referensi unit")) {
                        jc.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    }
                }
            }
        }

        if (component instanceof javax.swing.JLabel) {
            component.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            component.setForeground(new java.awt.Color(51, 65, 85));
        } else if (component instanceof javax.swing.AbstractButton) {
            component.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        } else if (component instanceof javax.swing.text.JTextComponent) {
            component.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        } else if (component instanceof javax.swing.JComboBox) {
            component.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        }

        if (component instanceof javax.swing.JPanel) {
            ((javax.swing.JPanel) component).setBackground(java.awt.Color.WHITE);
        }

        if (component instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) component).getComponents()) {
                modernizeDiagnosisTree(child);
            }
        }
    }

    private void styleDiagnosisTable(javax.swing.JTable table) {
        if (table == null) return;

        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setBackground(java.awt.Color.WHITE);
        table.setForeground(new java.awt.Color(30, 41, 59));
        table.setSelectionBackground(new java.awt.Color(219, 234, 254));
        table.setSelectionForeground(new java.awt.Color(30, 64, 175));
        table.setGridColor(new java.awt.Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));

        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(
                    new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
            table.getTableHeader().setBackground(new java.awt.Color(241, 245, 249));
            table.getTableHeader().setForeground(new java.awt.Color(51, 65, 85));
            table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 30));
            table.getTableHeader().setReorderingAllowed(false);
        }

        java.awt.Container parent = table.getParent();
        while (parent != null && !(parent instanceof javax.swing.JScrollPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof javax.swing.JScrollPane) {
            ((javax.swing.JScrollPane) parent).setBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        }
    }

    private javax.swing.JButton createFlatCloseButton() {
        javax.swing.JButton close = new javax.swing.JButton("\u00d7");
        close.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 18));
        close.setForeground(new java.awt.Color(100, 116, 139));
        close.setPreferredSize(new java.awt.Dimension(32, 28));
        close.setMargin(new java.awt.Insets(0, 0, 2, 0));
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.setContentAreaFilled(false);
        close.setOpaque(false);
        close.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return close;
    }

    private void installDialogDragSupport(javax.swing.JComponent handle, final java.awt.Window window) {
        final java.awt.Point[] pressedAt = {null};
        handle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pressedAt[0] = e.getPoint();
            }
        });
        handle.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (pressedAt[0] != null) {
                    java.awt.Point screen = e.getLocationOnScreen();
                    window.setLocation(screen.x - pressedAt[0].x, screen.y - pressedAt[0].y);
                }
            }
        });
    }

    // =================================================================
    //  MODAL BACKDROP BLUR
    // =================================================================

    /**
     * Menangani dialog standar (termasuk JOptionPane) yang dibuka dari form ini.
     * JWindow milik toast sengaja tidak diproses supaya toast tetap ringan dan
     * tidak membuat form utama ikut blur.
     */
    private void setupPopupBlurEffect() {
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                java.awt.Window popup = e.getOppositeWindow();
                if (popup instanceof java.awt.Dialog
                        && popup != BPJSRujukanSatuSehat.this) {
                    if (popup instanceof javax.swing.JDialog) {
                        configureRoundedPopupWindow(
                                (javax.swing.JDialog) popup, false);
                    }
                    setMainFormBlurred(true);
                }
            }

            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                setMainFormBlurred(false);
            }
        });
    }

    /**
     * Menampilkan dialog modal dengan backdrop yang sudah blur sebelum dialog
     * terlihat. Pemulihan di finally menjamin glass pane tidak tertinggal.
     */
    private void showModalDialogWithBlur(javax.swing.JDialog dialog) {
        if (dialog == null) return;

        setMainFormBlurred(true);
        try {
            dialog.setVisible(true);
        } finally {
            setMainFormBlurred(false);
        }
    }

    private void setMainFormBlurred(boolean blurred) {
        javax.swing.JRootPane root = getRootPane();
        if (root == null) return;

        if (blurred) {
            if (mainFormBlurActive) return;

            java.awt.image.BufferedImage snapshot = createBlurredMainSnapshot(root);
            if (snapshot == null) return;

            previousMainGlassPane = root.getGlassPane();
            previousMainGlassPaneVisible = previousMainGlassPane != null
                    && previousMainGlassPane.isVisible();

            MainFormBlurPane blurPane = new MainFormBlurPane(snapshot);
            root.setGlassPane(blurPane);
            mainFormBlurActive = true;
            blurPane.setVisible(true);
            root.repaint();
            return;
        }

        if (!mainFormBlurActive) return;

        java.awt.Component activeGlassPane = root.getGlassPane();
        if (activeGlassPane != null) {
            activeGlassPane.setVisible(false);
        }

        if (previousMainGlassPane != null) {
            root.setGlassPane(previousMainGlassPane);
            previousMainGlassPane.setVisible(previousMainGlassPaneVisible);
        }

        mainFormBlurActive = false;
        previousMainGlassPane = null;
        previousMainGlassPaneVisible = false;
        root.repaint();
    }

    /**
     * Membuat snapshot lalu menerapkan Gaussian blur separable pada resolusi
     * asli. Tidak ada lagi pembesaran gambar kecil yang membuat teks terlihat
     * pecah atau seperti kotak-kotak.
     */
    private java.awt.image.BufferedImage createBlurredMainSnapshot(
            javax.swing.JRootPane root) {
        int width = root.getWidth();
        int height = root.getHeight();
        if (width <= 0 || height <= 0) return null;

        java.awt.image.BufferedImage source = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D sourceGraphics = source.createGraphics();
        try {
            sourceGraphics.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            root.paintAll(sourceGraphics);
        } finally {
            sourceGraphics.dispose();
        }
        return applyGaussianBlur(source, 6, 3.25d);
    }

    /**
     * Gaussian blur dua tahap (horizontal lalu vertikal). Sampling pada tepi
     * memakai koordinat terdekat agar tidak meninggalkan garis tajam di pinggir.
     */
    private java.awt.image.BufferedImage applyGaussianBlur(
            java.awt.image.BufferedImage source, int radius, double sigma) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] input = source.getRGB(0, 0, width, height, null, 0, width);
        int[] horizontal = new int[input.length];
        int[] output = new int[input.length];
        double[] kernel = createGaussianKernel(radius, sigma);

        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                double alpha = 0d;
                double red = 0d;
                double green = 0d;
                double blue = 0d;

                for (int k = -radius; k <= radius; k++) {
                    int sampleX = Math.max(0, Math.min(width - 1, x + k));
                    int argb = input[rowOffset + sampleX];
                    double weight = kernel[k + radius];
                    alpha += ((argb >>> 24) & 0xff) * weight;
                    red += ((argb >>> 16) & 0xff) * weight;
                    green += ((argb >>> 8) & 0xff) * weight;
                    blue += (argb & 0xff) * weight;
                }

                horizontal[rowOffset + x] =
                        (clampColor(alpha) << 24)
                        | (clampColor(red) << 16)
                        | (clampColor(green) << 8)
                        | clampColor(blue);
            }
        }

        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                double alpha = 0d;
                double red = 0d;
                double green = 0d;
                double blue = 0d;

                for (int k = -radius; k <= radius; k++) {
                    int sampleY = Math.max(0, Math.min(height - 1, y + k));
                    int argb = horizontal[(sampleY * width) + x];
                    double weight = kernel[k + radius];
                    alpha += ((argb >>> 24) & 0xff) * weight;
                    red += ((argb >>> 16) & 0xff) * weight;
                    green += ((argb >>> 8) & 0xff) * weight;
                    blue += (argb & 0xff) * weight;
                }

                output[rowOffset + x] =
                        (clampColor(alpha) << 24)
                        | (clampColor(red) << 16)
                        | (clampColor(green) << 8)
                        | clampColor(blue);
            }
        }

        java.awt.image.BufferedImage blurred = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        blurred.setRGB(0, 0, width, height, output, 0, width);
        return blurred;
    }

    private double[] createGaussianKernel(int radius, double sigma) {
        double[] kernel = new double[(radius * 2) + 1];
        double total = 0d;
        double sigmaFactor = 2d * sigma * sigma;

        for (int i = -radius; i <= radius; i++) {
            double value = Math.exp(-(i * i) / sigmaFactor);
            kernel[i + radius] = value;
            total += value;
        }

        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= total;
        }
        return kernel;
    }

    private int clampColor(double value) {
        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }

    /**
     * Membulatkan bentuk window popup tanpa menyentuh isi ataupun event di
     * dalamnya. Dialog standar yang sudah displayable tetap diperlakukan aman:
     * bentuk dibulatkan bila platform mendukung, tanpa memaksa dekorasinya.
     */
    private static void configureRoundedPopupWindow(
            final javax.swing.JDialog dialog, boolean forceUndecorated) {
        if (dialog == null) return;

        if (forceUndecorated && !dialog.isUndecorated()
                && !dialog.isDisplayable()) {
            dialog.setUndecorated(true);
        }

        if (dialog.isUndecorated()) {
            try {
                dialog.setBackground(new java.awt.Color(0, 0, 0, 0));
            } catch (Exception ignored) {
                // Beberapa remote desktop tidak mendukung transparansi window.
            }
        }

        if (!Boolean.TRUE.equals(dialog.getRootPane()
                .getClientProperty("bpjs.popup.rounded-shape"))) {
            dialog.getRootPane().putClientProperty(
                    "bpjs.popup.rounded-shape", Boolean.TRUE);
            dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentShown(java.awt.event.ComponentEvent e) {
                    applyRoundedPopupShape(dialog);
                }

                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    applyRoundedPopupShape(dialog);
                }
            });
        }
        applyRoundedPopupShape(dialog);
    }

    private static void applyRoundedPopupShape(java.awt.Window popup) {
        if (popup == null || popup.getWidth() <= 0 || popup.getHeight() <= 0) {
            return;
        }
        try {
            popup.setShape(new java.awt.geom.RoundRectangle2D.Double(
                    0, 0, popup.getWidth(), popup.getHeight(),
                    POPUP_CORNER_RADIUS, POPUP_CORNER_RADIUS));
        } catch (Exception ignored) {
            // Fallback aman: popup tetap tampil normal pada platform lama.
        }
    }

    /** Border anti-aliased yang mengikuti bentuk keempat sudut popup. */
    private static final class RoundedPopupBorder
            extends javax.swing.border.AbstractBorder {
        private final java.awt.Color color;

        RoundedPopupBorder(java.awt.Color color) {
            this.color = color;
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(1, 1, 1, 1);
        }

        @Override
        public java.awt.Insets getBorderInsets(
                java.awt.Component c, java.awt.Insets insets) {
            insets.top = 1;
            insets.left = 1;
            insets.bottom = 1;
            insets.right = 1;
            return insets;
        }

        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g,
                int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.drawRoundRect(x, y, width - 1, height - 1,
                        POPUP_CORNER_RADIUS, POPUP_CORNER_RADIUS);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private static final class MainFormBlurPane extends javax.swing.JComponent {
        private final java.awt.image.BufferedImage snapshot;

        MainFormBlurPane(java.awt.image.BufferedImage snapshot) {
            this.snapshot = snapshot;
            setOpaque(false);

            // Glass pane juga menahan interaksi bila suatu saat dialog dipakai
            // secara modeless, tanpa mengubah enabled-state komponen utama.
            addMouseListener(new java.awt.event.MouseAdapter() {});
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
            addMouseWheelListener(e -> e.consume());
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(
                        java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(snapshot, 0, 0, getWidth(), getHeight(), null);

                // Tint tipis membantu popup putih tetap menonjol tanpa membuat
                // form utama terlihat gelap berlebihan.
                g2.setColor(new java.awt.Color(15, 23, 42, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g2.dispose();
            }
        }
    }

    // =================================================================
    //  DIALOG RESPON API (riwayat lengkap selama sesi form, read-only)
    // =================================================================
    private void rememberApiResponse(String source, JsonNode response) {
        String formatted;
        try {
            formatted = response == null
                    ? "{}"
                    : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            formatted = response == null ? "{}" : response.toString();
        }
        storeApiResponse(source, formatted, responseLooksError(response));
    }

    private void rememberApiResponse(String source, String rawJson) {
        try {
            JsonNode parsed = mapper.readTree(rawJson == null ? "{}" : rawJson);
            storeApiResponse(
                    source,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed),
                    responseLooksError(parsed));
        } catch (Exception e) {
            storeApiResponse(source, rawJson == null ? "" : rawJson.trim(), true);
        }
    }

    private void rememberApiException(String source, Exception exception) {
        String responseBody = extractApiErrorBody(exception);
        if (!responseBody.isEmpty()) {
            String formatted = responseBody;
            try {
                formatted = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(mapper.readTree(responseBody));
            } catch (Exception ignored) {
                // Body non-JSON tetap ditampilkan apa adanya di riwayat.
            }
            storeApiResponse(source, formatted, true);
            return;
        }

        String formatted;
        try {
            com.fasterxml.jackson.databind.node.ObjectNode error = mapper.createObjectNode();
            error.put("status", "ERROR");
            error.put("exception", exception == null
                    ? "Exception"
                    : exception.getClass().getSimpleName());
            error.put("message", exception == null ? "" : safe(exception.getMessage()));
            formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(error);
        } catch (Exception ignored) {
            formatted = "{\n  \"status\" : \"ERROR\"\n}";
        }
        storeApiResponse(source, formatted, true);
    }

    private void storeApiResponse(String source, String json, boolean error) {
        storeApiResponse(source, json, error, "");
    }

    private void storeApiResponse(String source, String json, boolean error,
            String responseTime) {
        lastApiResponseSource = safe(source);
        lastApiResponseTime = safe(responseTime).isEmpty()
                ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(new java.util.Date())
                : safe(responseTime);
        lastApiResponseJson = json == null ? "" : json.trim();
        lastApiResponseError = error;
        apiResponseHistory.add(new ApiResponseEntry(
                lastApiResponseSource,
                lastApiResponseTime,
                lastApiResponseJson,
                lastApiResponseError));
        persistActiveResponseHistoryQuietly();
    }

    /** Memperbarui riwayat jika rujukan aktif sudah memiliki snapshot. */
    private void persistActiveResponseHistoryQuietly() {
        if (tNoRujukanBpjs == null
                || safe(tNoRujukanBpjs.getText()).isEmpty()
                || safe(noRawat).isEmpty()) {
            return;
        }
        String sql = "UPDATE " + SNAPSHOT_TABLE
                + " SET response_api_json=?,updated_at=? WHERE no_rawat=?";
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            statement.setString(1, buildApiResponseHistoryJson());
            statement.setTimestamp(2, new java.sql.Timestamp(
                    System.currentTimeMillis()));
            statement.setString(3, noRawat);
            statement.executeUpdate();
        } catch (Exception ex) {
            // Respons API tetap tersedia di memori sesi. Kegagalan update kecil
            // ini tidak boleh menutupi hasil utama panggilan API.
            System.out.println("Gagal memperbarui riwayat API snapshot: " + ex);
        }
    }

    private String apiResponsePositionLabel(int index) {
        int size = apiResponseHistory.size();
        if (size == 1) return "AWAL & TERAKHIR";
        if (index == 0) return "AWAL";
        if (index == size - 1) return "TERAKHIR";
        return "";
    }

    private String buildApiResponseHistoryJson() {
        if (apiResponseHistory.isEmpty()) {
            return "{\n  \"message\" : \"Belum ada respon API pada sesi ini\"\n}";
        }

        try {
            com.fasterxml.jackson.databind.node.ObjectNode root = mapper.createObjectNode();
            root.put("jumlahRespon", apiResponseHistory.size());
            com.fasterxml.jackson.databind.node.ArrayNode history = root.putArray("riwayat");

            // Urutan kronologis memudahkan melihat respons awal sampai terakhir.
            for (int i = 0; i < apiResponseHistory.size(); i++) {
                ApiResponseEntry entry = apiResponseHistory.get(i);
                com.fasterxml.jackson.databind.node.ObjectNode item = history.addObject();
                item.put("urutan", i + 1);
                item.put("penanda", apiResponsePositionLabel(i));
                item.put("waktu", entry.time);
                item.put("sumber", entry.source);
                item.put("status", entry.error ? "GAGAL" : "BERHASIL");
                try {
                    item.set("response", mapper.readTree(entry.json));
                } catch (Exception ignored) {
                    item.put("response", entry.json);
                }
            }
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return lastApiResponseJson;
        }
    }

    private String extractApiErrorBody(Exception exception) {
        if (exception == null) return "";
        try {
            java.lang.reflect.Method method = exception.getClass()
                    .getMethod("getResponseBodyAsString");
            Object value = method.invoke(exception);
            return value == null ? "" : value.toString().trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean responseLooksError(JsonNode response) {
        if (response == null || response.isNull() || response.isMissingNode()) return true;
        if (response.has("fault") || response.has("error") || response.has("errors")) return true;

        String resourceType = response.path("resourceType").asText();
        if ("OperationOutcome".equalsIgnoreCase(resourceType)) {
            JsonNode issues = response.path("issue");
            if (issues.isArray()) {
                for (JsonNode issue : issues) {
                    String severity = issue.path("severity").asText();
                    if ("error".equalsIgnoreCase(severity)
                            || "fatal".equalsIgnoreCase(severity)) {
                        return true;
                    }
                }
            }
        }

        String code = response.path("metaData").path("code").asText().trim();
        if (!code.isEmpty()
                && !("200".equals(code) || "201".equals(code) || "1".equals(code))) {
            return true;
        }
        return false;
    }

    private void showApiResponseDialog() {
        final java.awt.Color primaryBlue = new java.awt.Color(37, 99, 235);
        final java.awt.Color pageColor = new java.awt.Color(248, 250, 252);
        final java.awt.Color lineColor = new java.awt.Color(226, 232, 240);
        final java.awt.Color textColor = new java.awt.Color(30, 41, 59);
        final java.awt.Color mutedColor = new java.awt.Color(100, 116, 139);

        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        dialog.setUndecorated(true);
        configureRoundedPopupWindow(dialog, true);
        dialog.setTitle("Respon API BPJS & SATUSEHAT");
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(pageColor);
        root.setBorder(new RoundedPopupBorder(new java.awt.Color(203, 213, 225)));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 3, 0, primaryBlue),
                javax.swing.BorderFactory.createEmptyBorder(13, 18, 12, 12)));

        javax.swing.JPanel heading = new javax.swing.JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JPanel titleRow = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel(
                "API Response History");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 16));
        title.setForeground(textColor);

        javax.swing.JLabel badge = new javax.swing.JLabel(
                apiResponseHistory.size() + " RESPONSE");
        badge.setOpaque(true);
        badge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        badge.setForeground(java.awt.Color.WHITE);
        badge.setBackground(primaryBlue);
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8));

        titleRow.add(title);
        titleRow.add(badge);

        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "BPJS / SATUSEHAT  \u2022  urutan waktu dari respons awal sampai terakhir");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        subtitle.setForeground(mutedColor);
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));

        heading.add(titleRow);
        heading.add(subtitle);

        javax.swing.JButton closeHeader = createFlatCloseButton();
        closeHeader.setToolTipText("Tutup");
        closeHeader.setForeground(mutedColor);
        closeHeader.addActionListener(e -> dialog.dispose());

        header.add(heading, java.awt.BorderLayout.CENTER);
        header.add(closeHeader, java.awt.BorderLayout.EAST);

        javax.swing.JTextPane jsonPane = new javax.swing.JTextPane();
        jsonPane.setEditable(false);
        jsonPane.setBackground(java.awt.Color.WHITE);
        jsonPane.setCaretColor(textColor);
        jsonPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));

        javax.swing.JScrollPane jsonScroll = new javax.swing.JScrollPane(jsonPane);
        jsonScroll.setBorder(javax.swing.BorderFactory.createLineBorder(lineColor));
        jsonScroll.getViewport().setBackground(java.awt.Color.WHITE);
        jsonScroll.getVerticalScrollBar().setUnitIncrement(16);

        javax.swing.JLabel responseSource = new javax.swing.JLabel("Belum ada respons API");
        responseSource.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        responseSource.setForeground(textColor);

        javax.swing.JLabel responseTime = new javax.swing.JLabel("-");
        responseTime.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        responseTime.setForeground(mutedColor);

        javax.swing.JLabel responseStatus = new javax.swing.JLabel("BELUM ADA");
        responseStatus.setOpaque(true);
        responseStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        responseStatus.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        responseStatus.setForeground(new java.awt.Color(90, 90, 90));
        responseStatus.setBackground(new java.awt.Color(232, 232, 232));
        responseStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 9, 4, 9));

        javax.swing.JPanel responseIdentity = new javax.swing.JPanel();
        responseIdentity.setOpaque(false);
        responseIdentity.setLayout(new javax.swing.BoxLayout(
                responseIdentity, javax.swing.BoxLayout.Y_AXIS));
        responseIdentity.add(responseSource);
        responseIdentity.add(responseTime);

        javax.swing.JLabel bodyTab = new javax.swing.JLabel("Body");
        bodyTab.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        bodyTab.setForeground(primaryBlue);
        bodyTab.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, primaryBlue),
                javax.swing.BorderFactory.createEmptyBorder(8, 12, 7, 12)));

        javax.swing.JPanel responseToolbar = new javax.swing.JPanel(
                new java.awt.BorderLayout(14, 0));
        responseToolbar.setBackground(java.awt.Color.WHITE);
        responseToolbar.setBorder(javax.swing.BorderFactory.createMatteBorder(
                0, 0, 1, 0, lineColor));
        responseToolbar.add(bodyTab, java.awt.BorderLayout.WEST);
        responseToolbar.add(responseIdentity, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel statusBox = new javax.swing.JPanel(new java.awt.GridBagLayout());
        statusBox.setOpaque(false);
        statusBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
        statusBox.add(responseStatus);
        responseToolbar.add(statusBox, java.awt.BorderLayout.EAST);

        javax.swing.DefaultListModel<ApiResponseEntry> historyModel =
                new javax.swing.DefaultListModel<>();
        for (ApiResponseEntry entry : apiResponseHistory) {
            historyModel.addElement(entry);
        }

        javax.swing.JList<ApiResponseEntry> historyList =
                new javax.swing.JList<>(historyModel);
        historyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyList.setBackground(java.awt.Color.WHITE);
        historyList.setForeground(textColor);
        historyList.setFixedCellHeight(74);
        historyList.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 4, 0));
        historyList.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel label = (javax.swing.JLabel) super
                        .getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);
                ApiResponseEntry entry = (ApiResponseEntry) value;
                String marker = apiResponsePositionLabel(index);
                String markerText = marker.isEmpty() ? "" : "  \u2022  " + marker;
                String statusText = entry.error ? "GAGAL" : "BERHASIL";
                String statusColor = entry.error ? "#b91c1c" : "#15803d";
                label.setText("<html><b><font color='#2563eb'>#"
                        + String.format("%02d", index + 1) + markerText
                        + "</font></b><br><font color='#1e293b'>"
                        + escapeHtml(entry.source)
                        + "</font><br><font color='#64748b'>"
                        + entry.time + "  \u2022  </font><font color='"
                        + statusColor + "'>" + statusText + "</font></html>");
                label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
                label.setBackground(isSelected
                        ? new java.awt.Color(219, 234, 254)
                        : java.awt.Color.WHITE);
                label.setForeground(textColor);
                label.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(
                                0, isSelected ? 4 : 0, 1, 0,
                                isSelected ? primaryBlue : lineColor),
                        javax.swing.BorderFactory.createEmptyBorder(
                                7, isSelected ? 8 : 12, 7, 8)));
                return label;
            }
        });

        javax.swing.JLabel historyTitle = new javax.swing.JLabel(
                "HISTORY  (" + apiResponseHistory.size() + ")");
        historyTitle.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        historyTitle.setForeground(textColor);
        historyTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 9, 8));

        javax.swing.JScrollPane historyScroll = new javax.swing.JScrollPane(historyList);
        historyScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        historyScroll.getViewport().setBackground(java.awt.Color.WHITE);
        historyScroll.getVerticalScrollBar().setUnitIncrement(16);

        javax.swing.JPanel historyPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        historyPanel.setBackground(java.awt.Color.WHITE);
        historyPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(
                0, 0, 0, 1, lineColor));
        historyPanel.setMinimumSize(new java.awt.Dimension(245, 100));
        historyPanel.setPreferredSize(new java.awt.Dimension(275, 100));
        historyPanel.add(historyTitle, java.awt.BorderLayout.NORTH);
        historyPanel.add(historyScroll, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel responsePanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        responsePanel.setBackground(java.awt.Color.WHITE);
        responsePanel.add(responseToolbar, java.awt.BorderLayout.NORTH);
        responsePanel.add(jsonScroll, java.awt.BorderLayout.CENTER);

        javax.swing.JSplitPane responseSplit = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, historyPanel, responsePanel);
        responseSplit.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        responseSplit.setDividerSize(5);
        responseSplit.setDividerLocation(275);
        responseSplit.setResizeWeight(0.27);
        responseSplit.setContinuousLayout(true);

        javax.swing.JPanel body = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        body.setBackground(pageColor);
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        body.add(createApiProcessStatusPanel(), java.awt.BorderLayout.NORTH);
        body.add(responseSplit, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 10));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, lineColor));

        final String[] selectedJson = {""};
        javax.swing.JButton copySelected = new javax.swing.JButton("Salin Respon");
        styleDialogButton(copySelected, true);
        copySelected.setEnabled(false);
        copySelected.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(selectedJson[0]);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(selection, selection);
            showModernToast(dialog, "Respon JSON berhasil disalin.", ToastMessage.SUCCESS, 1);
        });

        javax.swing.JButton copyAll = new javax.swing.JButton("Salin Semua");
        styleDialogButton(copyAll, false);
        copyAll.setEnabled(!apiResponseHistory.isEmpty());
        copyAll.addActionListener(e -> {
            String allJson = buildApiResponseHistoryJson();
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(allJson);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(selection, selection);
            showModernToast(dialog, "Semua riwayat respon berhasil disalin.", ToastMessage.SUCCESS, 1);
        });

        javax.swing.JButton closeFooter = new javax.swing.JButton("Tutup");
        styleDialogButton(closeFooter, false);
        closeFooter.addActionListener(e -> dialog.dispose());

        footer.add(copySelected);
        footer.add(copyAll);
        footer.add(closeFooter);

        historyList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            ApiResponseEntry entry = historyList.getSelectedValue();
            if (entry == null) return;

            int selectedIndex = historyList.getSelectedIndex();
            String marker = apiResponsePositionLabel(selectedIndex);
            responseSource.setText(entry.source);
            responseTime.setText(entry.time
                    + (marker.isEmpty() ? "" : "  \u2022  " + marker));
            responseStatus.setText(entry.error ? "GAGAL" : "BERHASIL");
            responseStatus.setForeground(entry.error
                    ? new java.awt.Color(181, 45, 45)
                    : new java.awt.Color(28, 126, 69));
            responseStatus.setBackground(entry.error
                    ? new java.awt.Color(255, 232, 232)
                    : new java.awt.Color(224, 246, 233));

            selectedJson[0] = entry.json;
            setJsonDocument(jsonPane, entry.json);
            jsonPane.setCaretPosition(0);
            jsonScroll.setRowHeaderView(createJsonLineNumbers(entry.json));
            copySelected.setEnabled(true);
        });

        if (!apiResponseHistory.isEmpty()) {
            historyList.setSelectedIndex(apiResponseHistory.size() - 1);
            historyList.ensureIndexIsVisible(apiResponseHistory.size() - 1);
        } else {
            String emptyJson = "{\n  \"message\" : \"Belum ada respon API pada sesi ini\"\n}";
            setJsonDocument(jsonPane, emptyJson);
            jsonScroll.setRowHeaderView(createJsonLineNumbers(emptyJson));
            jsonPane.setCaretPosition(0);
        }

        root.add(header, java.awt.BorderLayout.NORTH);
        root.add(body, java.awt.BorderLayout.CENTER);
        root.add(footer, java.awt.BorderLayout.SOUTH);
        dialog.setContentPane(root);

        dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                        "close-api-response");
        dialog.getRootPane().getActionMap().put("close-api-response",
                new javax.swing.AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        dialog.dispose();
                    }
                });

        installDialogDragSupport(header, dialog);
        dialog.setSize(1040, 680);
        dialog.setLocationRelativeTo(this);
        showModalDialogWithBlur(dialog);
    }

    /**
     * Status proses dipusatkan di dialog Respon API agar form utama tetap
     * ringkas, tetapi user masih dapat melihat kondisi proses terakhir.
     */
    private javax.swing.JPanel createApiProcessStatusPanel() {
        String message = safe(lastProcessStatusMessage);
        boolean processing = !lastProcessStatusError && isProcessingStatus(message);
        java.awt.Color accent = lastProcessStatusError
                ? new java.awt.Color(211, 47, 47)
                : (processing
                        ? new java.awt.Color(37, 99, 235)
                        : new java.awt.Color(38, 166, 91));
        java.awt.Color background = lastProcessStatusError
                ? new java.awt.Color(255, 242, 242)
                : (processing
                        ? new java.awt.Color(239, 246, 255)
                        : new java.awt.Color(240, 253, 244));

        javax.swing.JPanel status = new javax.swing.JPanel(new java.awt.BorderLayout());
        status.setBackground(background);
        status.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                javax.swing.BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        status.setPreferredSize(new java.awt.Dimension(10, 58));

        javax.swing.JPanel content = new javax.swing.JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JPanel titleRow = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel("Status Proses");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel badge = new javax.swing.JLabel(lastProcessStatusError
                ? "PERINGATAN" : (processing ? "DIPROSES" : "SIAP"));
        badge.setOpaque(true);
        badge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 9));
        badge.setForeground(java.awt.Color.WHITE);
        badge.setBackground(accent);
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 7, 2, 7));

        titleRow.add(title);
        titleRow.add(badge);

        javax.swing.JTextArea statusText = new javax.swing.JTextArea(message);
        statusText.setEditable(false);
        statusText.setFocusable(false);
        statusText.setOpaque(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        statusText.setForeground(new java.awt.Color(71, 85, 105));
        statusText.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 0, 0));

        content.add(titleRow);
        content.add(statusText);
        status.add(content, java.awt.BorderLayout.CENTER);
        return status;
    }

    private void styleDialogButton(javax.swing.JButton button, boolean primary) {
        // Tampilan terang mengikuti palet form utama.
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        int width = Math.max(100,
                button.getFontMetrics(button.getFont()).stringWidth(button.getText()) + 30);
        button.setPreferredSize(new java.awt.Dimension(width, 31));
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setForeground(primary
                ? java.awt.Color.WHITE
                : new java.awt.Color(51, 65, 85));
        button.setBackground(primary
                ? new java.awt.Color(37, 99, 235)
                : java.awt.Color.WHITE);
        button.setBorder(primary
                ? javax.swing.BorderFactory.createEmptyBorder(5, 13, 5, 13)
                : javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(
                                new java.awt.Color(203, 213, 225)),
                        javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
    }

    private javax.swing.JTextArea createJsonLineNumbers(String json) {
        int lineCount = Math.max(1, json.split("\\r?\\n", -1).length);
        StringBuilder numbers = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            numbers.append(i);
            if (i < lineCount) numbers.append('\n');
        }

        javax.swing.JTextArea lines = new javax.swing.JTextArea(numbers.toString());
        lines.setEditable(false);
        lines.setFocusable(false);
        lines.setFont(new java.awt.Font(getJsonMonospaceFont(), java.awt.Font.PLAIN, 12));
        lines.setForeground(new java.awt.Color(148, 163, 184));
        lines.setBackground(new java.awt.Color(241, 245, 249));
        lines.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 8, 12, 8));
        return lines;
    }

    private void setJsonDocument(javax.swing.JTextPane pane, String json) {
        javax.swing.text.StyledDocument document = pane.getStyledDocument();
        String mono = getJsonMonospaceFont();

        try {
            document.remove(0, document.getLength());
        } catch (javax.swing.text.BadLocationException ignored) {
            pane.setText("");
        }

        javax.swing.text.Style normal = document.addStyle("json-normal", null);
        javax.swing.text.StyleConstants.setFontFamily(normal, mono);
        javax.swing.text.StyleConstants.setFontSize(normal, 12);
        javax.swing.text.StyleConstants.setForeground(normal, new java.awt.Color(51, 65, 85));

        javax.swing.text.Style key = document.addStyle("json-key", normal);
        javax.swing.text.StyleConstants.setForeground(key, new java.awt.Color(37, 99, 235));
        javax.swing.text.StyleConstants.setBold(key, true);

        javax.swing.text.Style string = document.addStyle("json-string", normal);
        javax.swing.text.StyleConstants.setForeground(string, new java.awt.Color(22, 101, 52));

        javax.swing.text.Style number = document.addStyle("json-number", normal);
        javax.swing.text.StyleConstants.setForeground(number, new java.awt.Color(109, 40, 217));

        javax.swing.text.Style literal = document.addStyle("json-literal", normal);
        javax.swing.text.StyleConstants.setForeground(literal, new java.awt.Color(194, 65, 12));
        javax.swing.text.StyleConstants.setBold(literal, true);

        try {
            document.insertString(0, json, normal);
            applyJsonStyle(document, json,
                    java.util.regex.Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"(?=\\s*:)") , key);
            applyJsonStyle(document, json,
                    java.util.regex.Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"(?!\\s*:)") , string);
            applyJsonStyle(document, json,
                    java.util.regex.Pattern.compile("(?<![\\w\"])-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b"), number);
            applyJsonStyle(document, json,
                    java.util.regex.Pattern.compile("\\b(?:true|false|null)\\b"), literal);
        } catch (javax.swing.text.BadLocationException e) {
            pane.setText(json);
        }
    }

    private void applyJsonStyle(javax.swing.text.StyledDocument document, String json,
            java.util.regex.Pattern pattern, javax.swing.text.Style style) {
        java.util.regex.Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            document.setCharacterAttributes(
                    matcher.start(), matcher.end() - matcher.start(), style, true);
        }
    }

    private String getJsonMonospaceFont() {
        String[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String font : fonts) {
            if ("Consolas".equalsIgnoreCase(font)) return font;
        }
        return java.awt.Font.MONOSPACED;
    }

    private static class JsonButtonIcon implements javax.swing.Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 16; }
        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new java.awt.Color(37, 99, 235));
            g2.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
            g2.drawString("{ }", x, y + 12);
            g2.dispose();
        }
    }

    /** Ikon unduh/pemulihan yang digambar lokal agar tidak menambah asset JAR. */
    private static final class RecoveryButtonIcon implements javax.swing.Icon {
        private final java.awt.Color color;

        RecoveryButtonIcon(java.awt.Color color) {
            this.color = color;
        }

        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }

        @Override
        public void paintIcon(java.awt.Component component, java.awt.Graphics graphics,
                int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(1.7f,
                        java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                g2.drawArc(x + 2, y + 2, 11, 11, 35, 285);
                g2.drawLine(x + 2, y + 3, x + 2, y + 8);
                g2.drawLine(x + 2, y + 3, x + 7, y + 3);
                g2.drawLine(x + 8, y + 7, x + 8, y + 14);
                g2.drawLine(x + 5, y + 11, x + 8, y + 14);
                g2.drawLine(x + 11, y + 11, x + 8, y + 14);
            } finally {
                g2.dispose();
            }
        }
    }

    // =================================================================
    //  TOAST MODERN KHUSUS BPJSRujukanSatuSehat
    // =================================================================
    private static void showModernToast(java.awt.Component context, String message,
            int type, int autoHide) {
        Runnable showTask = () -> {
            final java.awt.Window toastOwner = resolveToastOwner(context);
            final java.awt.Window anchor = resolveToastAnchor(context);
            final javax.swing.JWindow toast = toastOwner == null
                    ? new javax.swing.JWindow()
                    : new javax.swing.JWindow(toastOwner);

            try {
                toast.setBackground(new java.awt.Color(0, 0, 0, 0));
            } catch (Exception ignored) {
                toast.setBackground(java.awt.Color.WHITE);
            }

            final ToastCard card = new ToastCard(type, normalizeToastMessage(message));
            toast.setContentPane(card);
            toast.pack();

            int width = 410;
            int height = Math.max(84, Math.min(150, toast.getPreferredSize().height));
            toast.setSize(width, height);

            synchronized (ACTIVE_RUJUKAN_TOASTS) {
                java.util.Iterator<javax.swing.JWindow> iterator =
                        ACTIVE_RUJUKAN_TOASTS.iterator();
                while (iterator.hasNext()) {
                    javax.swing.JWindow active = iterator.next();
                    if (active == null || !active.isDisplayable()) iterator.remove();
                }
                ACTIVE_RUJUKAN_TOASTS.add(toast);
                positionToast(toast, anchor);
            }

            Runnable closeToast = () -> {
                if (toast.isDisplayable()) toast.dispose();
                synchronized (ACTIVE_RUJUKAN_TOASTS) {
                    ACTIVE_RUJUKAN_TOASTS.remove(toast);
                }
            };

            card.getCloseButton().addActionListener(e -> closeToast.run());
            toast.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    synchronized (ACTIVE_RUJUKAN_TOASTS) {
                        ACTIVE_RUJUKAN_TOASTS.remove(toast);
                    }
                }
            });

            toast.setVisible(true);

            javax.swing.Timer timer = new javax.swing.Timer(
                    toastDuration(autoHide), e -> closeToast.run());
            timer.setRepeats(false);
            timer.start();
        };

        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            showTask.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(showTask);
        }
    }

    private static int toastDuration(int autoHide) {
        if (autoHide == 1) return 4000;
        if (autoHide == 2) return 5000;
        return 6500;
    }

    private static java.awt.Window resolveToastAnchor(java.awt.Component context) {
        java.awt.Window anchor = resolveToastOwner(context);

        // Naik ke frame utama supaya koordinat kanan/bawah MenuBar selalu konsisten.
        while (anchor != null && anchor.getOwner() != null
                && anchor.getOwner().isShowing()) {
            anchor = anchor.getOwner();
        }

        // Beberapa form Khanza dibuat dengan parent null. Dalam kondisi itu,
        // cari JFrame utama yang terlihat dan memiliki area terbesar.
        if (!(anchor instanceof javax.swing.JFrame)) {
            java.awt.Window bestFrame = null;
            long bestArea = -1L;
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof javax.swing.JFrame && window.isShowing()) {
                    long area = (long) window.getWidth() * (long) window.getHeight();
                    if (area > bestArea) {
                        bestArea = area;
                        bestFrame = window;
                    }
                }
            }
            if (bestFrame != null) anchor = bestFrame;
        }
        return anchor;
    }

    private static java.awt.Window resolveToastOwner(java.awt.Component context) {
        java.awt.Window owner = context instanceof java.awt.Window
                ? (java.awt.Window) context
                : (context == null
                        ? null
                        : javax.swing.SwingUtilities.getWindowAncestor(context));
        if (owner == null) {
            owner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .getActiveWindow();
        }
        return owner;
    }

    private static void positionToast(javax.swing.JWindow toast, java.awt.Window anchor) {
        java.awt.Rectangle usable = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        java.awt.Rectangle bounds = anchor != null && anchor.isShowing()
                ? anchor.getBounds()
                : usable;

        int x = bounds.x + bounds.width - toast.getWidth() - 14;
        int y = resolveMenuBottom(anchor, bounds) + 10;

        for (javax.swing.JWindow active : ACTIVE_RUJUKAN_TOASTS) {
            if (active != toast && active != null && active.isShowing()) {
                y = Math.max(y, active.getY() + active.getHeight() + 8);
            }
        }

        x = Math.max(usable.x + 8,
                Math.min(x, usable.x + usable.width - toast.getWidth() - 8));
        y = Math.max(usable.y + 8,
                Math.min(y, usable.y + usable.height - toast.getHeight() - 8));
        toast.setLocation(x, y);
    }

    private static int resolveMenuBottom(java.awt.Window anchor, java.awt.Rectangle bounds) {
        if (anchor instanceof javax.swing.JFrame) {
            javax.swing.JMenuBar menuBar = ((javax.swing.JFrame) anchor).getJMenuBar();
            if (menuBar != null && menuBar.isShowing()) {
                try {
                    java.awt.Point location = menuBar.getLocationOnScreen();
                    return location.y + menuBar.getHeight();
                } catch (Exception ignored) {
                }
            }
        }
        return bounds.y + 34;
    }

    private static String normalizeToastMessage(String message) {
        if (message == null) return "";
        return message
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .trim();
    }

    private static String escapeToastHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .replace("\r", "<br>");
    }

    private static java.awt.Color toastColor(int type) {
        if (type == ToastMessage.SUCCESS) return new java.awt.Color(22, 163, 74);
        if (type == ToastMessage.ERROR) return new java.awt.Color(220, 38, 38);
        if (type == ToastMessage.WARNING) return new java.awt.Color(245, 158, 11);
        return new java.awt.Color(37, 99, 235);
    }

    private static String toastTitle(int type) {
        if (type == ToastMessage.SUCCESS) return "Berhasil";
        if (type == ToastMessage.ERROR) return "Kesalahan";
        if (type == ToastMessage.WARNING) return "Peringatan";
        return "Informasi";
    }

    private static class ToastCard extends javax.swing.JPanel {
        private final int type;
        private final javax.swing.JButton closeButton;

        ToastCard(int type, String message) {
            super(new java.awt.BorderLayout(12, 0));
            this.type = type;
            setOpaque(false);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 16, 15, 15));

            ToastTypeIcon icon = new ToastTypeIcon(type);

            javax.swing.JPanel content = new javax.swing.JPanel();
            content.setOpaque(false);
            content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));

            javax.swing.JLabel title = new javax.swing.JLabel(toastTitle(type));
            title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13));
            title.setForeground(new java.awt.Color(30, 41, 59));
            title.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

            javax.swing.JLabel body = new javax.swing.JLabel(
                    "<html><body style='width:286px;'>"
                    + escapeToastHtml(message)
                    + "</body></html>");
            body.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            body.setForeground(new java.awt.Color(71, 85, 105));
            body.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
            body.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

            content.add(title);
            content.add(body);

            closeButton = new javax.swing.JButton("\u00d7");
            closeButton.setFont(new java.awt.Font(
                    "Segoe UI Semibold", java.awt.Font.PLAIN, 17));
            closeButton.setForeground(new java.awt.Color(100, 116, 139));
            closeButton.setPreferredSize(new java.awt.Dimension(26, 26));
            closeButton.setMargin(new java.awt.Insets(0, 0, 2, 0));
            closeButton.setFocusPainted(false);
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setOpaque(false);
            closeButton.setCursor(java.awt.Cursor.getPredefinedCursor(
                    java.awt.Cursor.HAND_CURSOR));
            closeButton.setToolTipText("Tutup");

            add(icon, java.awt.BorderLayout.WEST);
            add(content, java.awt.BorderLayout.CENTER);
            add(closeButton, java.awt.BorderLayout.EAST);
        }

        javax.swing.JButton getCloseButton() {
            return closeButton;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 7;
            int height = getHeight() - 7;
            g2.setColor(new java.awt.Color(15, 23, 42, 42));
            g2.fillRoundRect(5, 5, width - 1, height - 1, 16, 16);
            g2.setColor(java.awt.Color.WHITE);
            g2.fillRoundRect(1, 1, width - 1, height - 1, 16, 16);
            g2.setColor(toastColor(type));
            g2.fillRoundRect(1, 1, 5, height - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ToastTypeIcon extends javax.swing.JComponent {
        private final int type;

        ToastTypeIcon(int type) {
            this.type = type;
            setPreferredSize(new java.awt.Dimension(34, 34));
            setMinimumSize(new java.awt.Dimension(34, 34));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(toastColor(type));
            g2.fillOval(1, 1, 32, 32);
            g2.setColor(java.awt.Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2.2f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));

            if (type == ToastMessage.SUCCESS) {
                g2.drawLine(9, 17, 14, 22);
                g2.drawLine(14, 22, 25, 11);
            } else if (type == ToastMessage.ERROR) {
                g2.drawLine(11, 11, 23, 23);
                g2.drawLine(23, 11, 11, 23);
            } else if (type == ToastMessage.WARNING) {
                g2.drawLine(17, 9, 17, 19);
                g2.fillOval(15, 23, 4, 4);
            } else {
                g2.drawLine(17, 15, 17, 25);
                g2.fillOval(15, 8, 4, 4);
            }
            g2.dispose();
        }
    }



    private void setupJawabanKriteriaEditor() {
        try {
            if (tblKriteria != null && tblKriteria.getColumnModel().getColumnCount() > 3) {
                tblKriteria.getColumnModel().getColumn(3).setCellEditor(new KriteriaJawabanCellEditor(tblKriteria));
                tblKriteria.getColumnModel().getColumn(3).setCellRenderer(new KriteriaJawabanRenderer());
                tblKriteria.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                tblKriteria.setRowHeight(24);
            }
        } catch (Exception e) {
            System.out.println("Gagal setup editor jawaban kriteria: " + e.getMessage());
        }
    }

    private String defaultJawabanUntukTipe(String tipe) {
        return "boolean".equalsIgnoreCase(safe(tipe)) ? "TIDAK" : "";
    }

    private String normalisasiBooleanKriteria(String jawaban) {
        String v = safe(jawaban).trim().toLowerCase();
        if (v.equals("ya") || v.equals("y") || v.equals("true") || v.equals("1")) return "true";
        if (v.equals("tidak") || v.equals("tdk") || v.equals("t") || v.equals("false") || v.equals("0")) return "false";
        return null;
    }

    /**
     * Saat aktif, tepat satu dari tiga baris kriteria boleh dipilih. Baris
     * kedua dianggap dipilih ketika kode tindakan medis tidak kosong.
     */
    private boolean isCriteriaSynchronizationEnabled() {
        return chkSinkronKriteriaUi == null
                || chkSinkronKriteriaUi.isSelected();
    }

    private void handleCriteriaSynchronizationToggle() {
        if (isCriteriaSynchronizationEnabled()
                && modelKriteria.getRowCount() >= 3) {
            int activeRow = findSingleActiveCriteriaRow();
            if (activeRow >= 0) {
                applyCriteriaSynchronization(activeRow);
            } else if (countActiveCriteriaRows() > 1) {
                showModernToast(this,
                        "Sinkronisasi otomatis aktif kembali. Pilih tepat satu "
                                + "kriteria agar dapat melanjutkan Cari Faskes.",
                        ToastMessage.WARNING, 0);
            }
        }
        clearFaskesResultsAfterCriteriaChange();
        updateReferralActionState();
    }

    private void applyCriteriaSynchronization(int changedRow) {
        if (!isCriteriaSynchronizationEnabled()
                || syncingCriteriaAnswers
                || modelKriteria.getRowCount() < 3
                || changedRow < 0
                || changedRow >= modelKriteria.getRowCount()) {
            return;
        }

        boolean selected = false;
        if (changedRow == 0 || changedRow == 2) {
            selected = "true".equalsIgnoreCase(normalisasiBooleanKriteria(
                    tableValue(modelKriteria, changedRow, 3)));
        } else if (changedRow == 1) {
            selected = !tableValue(modelKriteria, changedRow, 3).isEmpty();
        }
        if (!selected) return;

        syncingCriteriaAnswers = true;
        try {
            if (changedRow == 0) {
                setCriteriaAnswerIfChanged(0, "YA");
                setCriteriaAnswerIfChanged(1, "");
                setCriteriaAnswerIfChanged(2, "TIDAK");
            } else if (changedRow == 1) {
                setCriteriaAnswerIfChanged(0, "TIDAK");
                setCriteriaAnswerIfChanged(2, "TIDAK");
            } else if (changedRow == 2) {
                setCriteriaAnswerIfChanged(0, "TIDAK");
                setCriteriaAnswerIfChanged(1, "");
                setCriteriaAnswerIfChanged(2, "YA");
            }
        } finally {
            syncingCriteriaAnswers = false;
        }
    }

    private void setCriteriaAnswerIfChanged(int row, String value) {
        if (row < 0 || row >= modelKriteria.getRowCount()) return;
        String current = tableValue(modelKriteria, row, 3);
        if (!safe(value).equals(current)) {
            modelKriteria.setValueAt(value, row, 3);
        }
    }

    private int countActiveCriteriaRows() {
        if (modelKriteria.getRowCount() < 3) return 0;
        int count = 0;
        if ("true".equalsIgnoreCase(normalisasiBooleanKriteria(
                tableValue(modelKriteria, 0, 3)))) count++;
        if (!tableValue(modelKriteria, 1, 3).isEmpty()) count++;
        if ("true".equalsIgnoreCase(normalisasiBooleanKriteria(
                tableValue(modelKriteria, 2, 3)))) count++;
        return count;
    }

    private int findSingleActiveCriteriaRow() {
        if (countActiveCriteriaRows() != 1) return -1;
        if ("true".equalsIgnoreCase(normalisasiBooleanKriteria(
                tableValue(modelKriteria, 0, 3)))) return 0;
        if (!tableValue(modelKriteria, 1, 3).isEmpty()) return 1;
        if ("true".equalsIgnoreCase(normalisasiBooleanKriteria(
                tableValue(modelKriteria, 2, 3)))) return 2;
        return -1;
    }

    private boolean commitCriteriaCellEditing() {
        if (tblKriteria == null || !tblKriteria.isEditing()) return true;
        javax.swing.table.TableCellEditor editor = tblKriteria.getCellEditor();
        if (editor == null || editor.stopCellEditing()) return true;
        showModernToast(this,
                "Selesaikan dahulu pengisian jawaban kriteria yang sedang aktif.",
                ToastMessage.WARNING, 0);
        return false;
    }

    private boolean validateCriteriaSelectionForRequest() {
        if (!isCriteriaSynchronizationEnabled()
                || modelKriteria.getRowCount() < 3) {
            return true;
        }
        if (countActiveCriteriaRows() == 1) return true;

        showModernToast(this,
                "Pilih tepat satu kriteria: baris pertama, Tindakan Medis, "
                        + "atau baris ketiga. Hilangkan centang Sinkron otomatis "
                        + "jika diperlukan pengisian manual.",
                ToastMessage.WARNING, 0);
        return false;
    }

    private boolean isCriteriaReadyForFaskes() {
        if (modelKriteria.getRowCount() == 0) return false;
        for (int row = 0; row < modelKriteria.getRowCount(); row++) {
            String type = tableValue(modelKriteria, row, 2);
            if ("boolean".equalsIgnoreCase(type)
                    && normalisasiBooleanKriteria(
                            tableValue(modelKriteria, row, 3)) == null) {
                return false;
            }
        }
        return !isCriteriaSynchronizationEnabled()
                || modelKriteria.getRowCount() < 3
                || countActiveCriteriaRows() == 1;
    }

    private void clearFaskesResultsAfterCriteriaChange() {
        if (tNoRujukanBpjs != null
                && !safe(tNoRujukanBpjs.getText()).isEmpty()) return;

        boolean hadFaskesData = modelFaskes.getRowCount() > 0
                || !safe(selectedKdppkTujuan).isEmpty();
        if (!hadFaskesData) return;

        restoringReferralSnapshot = true;
        try {
            if (txtCariFaskesUi != null) txtCariFaskesUi.setText("");
            if (sorterFaskesUi != null) sorterFaskesUi.setRowFilter(null);
            modelFaskes.setRowCount(0);
        } finally {
            restoringReferralSnapshot = false;
        }
        selectedKdppkSatuSehatTujuan = "";
        selectedKdppkTujuan = "";
        selectedNmppkTujuan = "";
        if (tblFaskes != null) tblFaskes.clearSelection();
        setStatus("Kriteria berubah. Klik [Cari Faskes] kembali.", false);
        updateReferralActionState();
    }

    private static boolean isKriteriaTindakanMedis(String kriteriaText) {
        if (kriteriaText == null) return false;
        String lower = kriteriaText.toLowerCase();
        return lower.contains("tindakan")
                || lower.contains("icd9")
                || lower.contains("icd-9");
    }

    private static class KriteriaJawabanCellEditor1 extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final javax.swing.JComboBox<String> cmbBoolean = new javax.swing.JComboBox<>(new String[]{"YA", "TIDAK"});
        private final javax.swing.JTextField txtText = new javax.swing.JTextField();
        private java.awt.Component active;
        KriteriaJawabanCellEditor1(javax.swing.JTable table) {}
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
private static class KriteriaJawabanCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
    private final javax.swing.JComboBox<String> cmbBoolean = new javax.swing.JComboBox<>(new String[]{"YA", "TIDAK"});
    private final JTextField txtText = new JTextField();
    private final JPanel pnlBoolean = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
    private final JPanel pnlIcd9 = new JPanel(new BorderLayout(6, 0));
    private final JTextField txtIcd9 = new JTextField();
    private final JButton btnCariIcd9 = new JButton("Cari ICD-9");
    private java.awt.Component active;
    private JTable ownerTable;
    private int editingRow, editingCol;
    private boolean loadingEditorValue;

    KriteriaJawabanCellEditor(JTable table) {
        this.ownerTable = table;

        // Combo boolean hanya selebar teks terpanjang, yaitu "TIDAK".
        cmbBoolean.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbBoolean.setPrototypeDisplayValue("TIDAK");
        int booleanWidth = cmbBoolean.getFontMetrics(cmbBoolean.getFont())
                .stringWidth("TIDAK") + 34;
        Dimension booleanSize = new Dimension(booleanWidth, 26);
        cmbBoolean.setPreferredSize(booleanSize);
        cmbBoolean.setMinimumSize(booleanSize);
        cmbBoolean.setMaximumSize(booleanSize);
        pnlBoolean.setOpaque(false);
        pnlBoolean.add(cmbBoolean);
        cmbBoolean.addActionListener(e -> {
            if (!loadingEditorValue && ownerTable != null
                    && ownerTable.isEditing()
                    && ownerTable.getCellEditor() == this) {
                stopCellEditing();
            }
        });

        // Textbox prosedur memenuhi ruang jawaban; tombol selalu menempel kanan.
        txtIcd9.setEditable(true); // tetap dapat dikoreksi/dihapus manual
        txtIcd9.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtIcd9.setPreferredSize(new Dimension(240, 26));
        txtIcd9.setMinimumSize(new Dimension(120, 26));
        txtIcd9.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            private void changed() {
                updateIcd9ValueInModel();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });
        txtIcd9.addActionListener(e -> stopCellEditing());

        btnCariIcd9.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnCariIcd9.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 10));
        btnCariIcd9.setForeground(Color.WHITE);
        btnCariIcd9.setBackground(new Color(37, 99, 235));
        btnCariIcd9.setFocusPainted(false);
        btnCariIcd9.setBorderPainted(false);
        btnCariIcd9.setContentAreaFilled(true);
        btnCariIcd9.setOpaque(true);
        btnCariIcd9.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int searchButtonWidth = btnCariIcd9.getFontMetrics(btnCariIcd9.getFont())
                .stringWidth(btnCariIcd9.getText()) + 24;
        Dimension searchButtonSize = new Dimension(searchButtonWidth, 26);
        btnCariIcd9.setPreferredSize(searchButtonSize);
        btnCariIcd9.setMinimumSize(searchButtonSize);
        btnCariIcd9.setMaximumSize(searchButtonSize);

        pnlIcd9.setOpaque(false);
        // Beri jarak dari garis kanan card agar tombol tidak terlihat menempel.
        pnlIcd9.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 10));
        pnlIcd9.add(txtIcd9, BorderLayout.CENTER);
        pnlIcd9.add(btnCariIcd9, BorderLayout.EAST);
        
        btnCariIcd9.addActionListener(e -> {
            showIcd9SearchPopup(txtIcd9, this::applyIcd9Selection);
        });
    }

    private void applyIcd9Selection(String selectedCode) {
        String combined = appendIcd9Code(txtIcd9.getText(), selectedCode);
        txtIcd9.setText(combined);

        boolean committedByEditor = ownerTable != null
                && ownerTable.isEditing()
                && ownerTable.getCellEditor() == this
                && stopCellEditing();
        if (!committedByEditor && ownerTable != null
                && editingRow >= 0 && editingRow < ownerTable.getRowCount()
                && editingCol >= 0 && editingCol < ownerTable.getColumnCount()) {
            int modelRow = ownerTable.convertRowIndexToModel(editingRow);
            int modelColumn = ownerTable.convertColumnIndexToModel(editingCol);
            ownerTable.getModel().setValueAt(combined, modelRow, modelColumn);
        }
    }

    private void updateIcd9ValueInModel() {
        if (loadingEditorValue || ownerTable == null
                || !ownerTable.isEditing()
                || ownerTable.getCellEditor() != this
                || editingRow < 0 || editingRow >= ownerTable.getRowCount()
                || editingCol < 0 || editingCol >= ownerTable.getColumnCount()) {
            return;
        }
        int modelRow = ownerTable.convertRowIndexToModel(editingRow);
        int modelColumn = ownerTable.convertColumnIndexToModel(editingCol);
        String value = txtIcd9.getText();
        Object current = ownerTable.getModel().getValueAt(modelRow, modelColumn);
        if (current == null || !value.equals(current.toString())) {
            ownerTable.getModel().setValueAt(value, modelRow, modelColumn);
        }
    }
    
    // Menjaga urutan kode, menghapus duplikat, dan memakai pemisah koma.
    private String appendIcd9Code(String existing, String selectedCode) {
        java.util.LinkedHashSet<String> codes = new java.util.LinkedHashSet<>();
        if (existing != null && !existing.trim().isEmpty()) {
            for (String code : existing.split("[,;]")) {
                String trimmed = code.trim();
                if (!trimmed.isEmpty()) codes.add(trimmed);
            }
        }
        if (selectedCode != null && !selectedCode.trim().isEmpty()) {
            codes.add(selectedCode.trim());
        }
        return String.join(", ", codes);
    }

    @Override
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        editingRow = row;
        editingCol = column;
        int modelRow = table.convertRowIndexToModel(row);
        String tipe = String.valueOf(table.getModel().getValueAt(modelRow, 2));
        String kriteriaText = String.valueOf(table.getModel().getValueAt(modelRow, 1)); // kolom "text"

        // --- Khusus untuk tipe boolean
        if ("boolean".equalsIgnoreCase(tipe)) {
            String val = value == null ? "TIDAK" : value.toString().trim();
            if ("true".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val)) val = "YA";
            if ("false".equalsIgnoreCase(val) || "0".equals(val) || "T".equalsIgnoreCase(val) || "TDK".equalsIgnoreCase(val)) val = "TIDAK";
            if (!"YA".equalsIgnoreCase(val) && !"TIDAK".equalsIgnoreCase(val)) val = "TIDAK";
            loadingEditorValue = true;
            try {
                cmbBoolean.setSelectedItem(val.toUpperCase());
            } finally {
                loadingEditorValue = false;
            }
            active = pnlBoolean;
            return pnlBoolean;
        }

        // --- Untuk tipe text, cek apakah kriteria ini mengharuskan ICD-9
        if (isKriteriaTindakanMedis(kriteriaText)) {
            String currentValue = (value == null) ? "" : value.toString();
            loadingEditorValue = true;
            try {
                txtIcd9.setText(currentValue);
            } finally {
                loadingEditorValue = false;
            }
            active = pnlIcd9;
            return pnlIcd9;
        }

        // --- Text biasa (tanpa lookup)
        txtText.setText(value == null ? "" : value.toString());
        active = txtText;
        return txtText;
    }

    @Override
    public Object getCellEditorValue() {
        if (active == pnlBoolean) return cmbBoolean.getSelectedItem();
        if (active == pnlIcd9) return txtIcd9.getText();
        return txtText.getText();
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
        lastProcessStatusMessage = safe(msg);
        lastProcessStatusError = error;
        tStatus.setLineWrap(true);
        tStatus.setWrapStyleWord(true);
        tStatus.setText("Status : " + lastProcessStatusMessage);
        updateStatusProcessStyle(msg, error);
        System.out.println("[BPJSRujukanSatuSehat] " + msg);
    }
    private void setStatus1(String msg, boolean error) {
    lblStatus.setForeground(error ? new java.awt.Color(180, 0, 0) : new java.awt.Color(0, 100, 0));
    lblStatus.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    String text = "Status: " + safe(msg);

    lblStatus.setText(
        "<html>"
        + "<body style='width:460px; font-family:Segoe UI; font-size:11px;'>"
        + escapeHtml(text).replace("\n", "<br>")
        + "</body>"
        + "</html>"
    );

    System.out.println("[BPJSRujukanSatuSehat] " + msg);
}

private String escapeHtml(String s) {
    if (s == null) {
        return "";
    }
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}

    // =================================================================
    //  PUBLIC API - dipanggil oleh BPJSDataSEP setelah pilih SEP
    // =================================================================

    /**
     * Set data SEP dan terkait dari row tbDataSEP yang dipilih.
     * Kolom yang dipakai (sesuai struktur tbDataSEP di BPJSDataSEP.java):
     *   0 No.SEP, 1 No.Rawat, 2 No.RM, 3 Nama Pasien,
     *   13 Kode Diagnosa (ICD-10), 14 Nama Diagnosa,
     *   15 Kode Poli BPJS, 16 Nama Poli, 44 Kd DPJP
     */
    public void setDataSEP(String noSep, String noRawat, String noRm, String namaPasien,
                           String kdPenyakit, String nmPenyakit,
                           String kdPoli, String nmPoli, String kdDokterRs) {
        this.noSep = safe(noSep);
        this.noRawat = safe(noRawat);
        this.noRm = safe(noRm);
        this.namaPasien = safe(namaPasien);
        this.kdPenyakit = safe(kdPenyakit);
        this.nmPenyakit = safe(nmPenyakit);
        this.kdPoliBpjs = safe(kdPoli);
        this.nmPoli = safe(nmPoli);
        this.kdDokterRs = safe(kdDokterRs);

        // Tampilkan ke field
        tNoSep.setText(this.noSep);
        tNoRawat.setText(this.noRawat);
        tPasien.setText(this.noRm + " - " + this.namaPasien);

        // Default: diagnosa & poli rujukan = diagnosa & poli kunjungan (user bisa ganti)
        this.kdPenyakitRujuk = this.kdPenyakit;
        this.nmPenyakitRujuk = this.nmPenyakit;
        this.kdPoliRujuk = this.kdPoliBpjs;
        this.nmPoliRujuk = this.nmPoli;
        tKdDiagnosaRujuk.setText(this.kdPenyakitRujuk);
        tNmDiagnosaRujuk.setText(this.nmPenyakitRujuk);
        tKdPoliRujuk.setText(this.kdPoliRujuk);
        tNmPoliRujuk.setText(this.nmPoliRujuk);

        // Lookup data Satu Sehat
        loadDataSatuSehat();
        loadSavedReferralSnapshot();
    }

    public void setNoRawat(String NoRawat){
         this.noRawat = safe(NoRawat);     
    }
            
    public void loadData() {

    String SQL =
            "SELECT " +
            "    bs.no_sep, " +
            "    rp.no_rkm_medis AS norm, " +
            "    rp.no_rawat, " +
            "    ps.nm_pasien, " +
            "    sse.id_encounter, " +
            "    ps.no_ktp AS no_ktp_pasien, " +
            "    dp.kd_penyakit, " +
            "    py.nm_penyakit, " +
            "    rp.kd_poli AS kdpoli, " +
            "    mp.kd_poli_bpjs AS kdpolibpjs, " +
            "    mp.nm_poli_bpjs AS nmpolibpjs, " +
            "    rp.kd_dokter, " +
            "    dk.nm_dokter, " +
            "    pg.no_ktp AS no_ktp_dokter, " +
            "    md.kd_dokter_bpjs " +
            "FROM reg_periksa rp " +
            "LEFT JOIN pasien ps " +
            "    ON ps.no_rkm_medis = rp.no_rkm_medis " +
            "LEFT JOIN bridging_sep bs " +
            "    ON bs.no_rawat = rp.no_rawat " +
            "LEFT JOIN diagnosa_pasien dp " +
            "    ON dp.no_rawat = rp.no_rawat " +
            "    AND dp.prioritas = '1' " +
            "LEFT JOIN penyakit py " +
            "    ON py.kd_penyakit = dp.kd_penyakit " +
            "LEFT JOIN maping_poli_bpjs mp " +
            "    ON mp.kd_poli_rs = rp.kd_poli " +
            "LEFT JOIN maping_dokter_dpjpvclaim md " +
            "    ON md.kd_dokter = rp.kd_dokter " +
            "LEFT JOIN dokter dk " +
            "    ON dk.kd_dokter = rp.kd_dokter " +
            "LEFT JOIN pegawai pg " +
            "    ON pg.nik = dk.kd_dokter " +
            "LEFT JOIN satu_sehat_encounter sse " +
            "    ON sse.no_rawat = rp.no_rawat " +
            "WHERE rp.no_rawat = ? " +
            "LIMIT 1";

    try {

        ps = koneksi.prepareStatement(SQL);
        ps.setString(1, noRawat);

        rs = ps.executeQuery();

        if (rs.next()) {

            // =========================
            // DATA PASIEN
            // =========================
            this.noSep =
                    safe(rs.getString("no_sep"));

            this.noRm =
                    safe(rs.getString("norm"));

            this.namaPasien =
                    safe(rs.getString("nm_pasien"));

            this.kdPenyakit =
                    safe(rs.getString("kd_penyakit"));

            this.nmPenyakit =
                    safe(rs.getString("nm_penyakit"));

            this.kdPoliBpjs =
                    safe(rs.getString("kdpolibpjs"));

            this.nmPoli =
                    safe(rs.getString("nmpolibpjs"));

            this.kdDokterRs =
                    safe(rs.getString("kd_dokter"));

            // =========================
            // SATUSEHAT
            // =========================
            encounterRef =
                    safe(rs.getString("id_encounter"));

            String noKtpPasien =
                    safe(rs.getString("no_ktp_pasien"));

            String noKtpDokter =
                    safe(rs.getString("no_ktp_dokter"));

            // =========================
            // TAMPIL FIELD
            // =========================
            tNoSep.setText(this.noSep);

            tNoRawat.setText(this.noRawat);

            tPasien.setText(
                    this.noRm + " - " + this.namaPasien
            );

            tEncounter.setText(encounterRef);

            // =========================
            // DEFAULT RUJUKAN
            // =========================
            this.kdPenyakitRujuk =
                    this.kdPenyakit;

            this.nmPenyakitRujuk =
                    this.nmPenyakit;

            this.kdPoliRujuk =
                    this.kdPoliBpjs;

            this.nmPoliRujuk =
                    this.nmPoli;

            tKdDiagnosaRujuk.setText(
                    this.kdPenyakitRujuk);

            tNmDiagnosaRujuk.setText(
                    this.nmPenyakitRujuk);

            tKdPoliRujuk.setText(
                    this.kdPoliRujuk);

            tNmPoliRujuk.setText(
                    this.nmPoliRujuk);

            // =========================
            // LOAD IHS PASIEN
            // =========================
            try {

                idPasienSatuSehat = "";

                if (!noKtpPasien.isEmpty()) {

                    // ==========================
                    // CEK CACHE LOKAL
                    // ==========================
                    idPasienSatuSehat = Sequel.cariIsi(
                            "SELECT ihs_pasien FROM satu_sehat_pasien WHERE no_ktp=?",
                            noKtpPasien
                    );

                    if (!idPasienSatuSehat.isEmpty()) {

                        System.out.println(
                                "IHS Pasien ditemukan di cache lokal : "
                                + idPasienSatuSehat
                        );

                    } else {

                        System.out.println(
                                "IHS Pasien tidak ditemukan di cache, cek SATUSEHAT"
                        );

                        // ==========================
                        // CEK SATUSEHAT
                        // ==========================
                        idPasienSatuSehat = cekViaSatuSehat.tampilIDPasien(noKtpPasien);

                        if (!idPasienSatuSehat.isEmpty()) {

                            // ==========================
                            // SIMPAN MASTER
                            // ==========================
                            Sequel.SimpanData(
                                    "satu_sehat_pasien",
                                    new String[]{
                                        "no_ktp",
                                        "no_rkm_medis",
                                        "ihs_pasien"                                       
                                    },
                                    new String[]{
                                        noKtpPasien,
                                        noRm,
                                        idPasienSatuSehat
                                    }
                            );

                            // ==========================
                            // SIMPAN LOG
                            // ==========================
                            Sequel.SimpanData(
                                    "satu_sehat_ihs_log",
                                    new String[]{
                                        "jenis",
                                        "referensi",
                                        "no_ktp",
                                        "ihs_number",
                                        "status_sync",
                                        "keterangan"
                                    },
                                    new String[]{
                                        "PASIEN",
                                        noRm,
                                        noKtpPasien,
                                        idPasienSatuSehat,
                                        "SUKSES",
                                        "Sinkron awal SATUSEHAT"
                                    }
                            );

                            System.out.println(
                                    "IHS Pasien berhasil disimpan : "
                                    + idPasienSatuSehat
                            );

                        } else {

                            // ==========================
                            // LOG GAGAL
                            // ==========================
                            Sequel.SimpanData(
                                    "satu_sehat_ihs_log",
                                    new String[]{
                                        "jenis",
                                        "referensi",
                                        "no_ktp",
                                        "status_sync",
                                        "keterangan"
                                    },
                                    new String[]{
                                        "PASIEN",
                                        noRm,
                                        noKtpPasien,
                                        "GAGAL",
                                        "IHS Pasien tidak ditemukan di SATUSEHAT"
                                    }
                            );

                            System.out.println(
                                    "IHS Pasien tidak ditemukan di SATUSEHAT"
                            );
                        }
                    }
                }

            } catch (Exception e) {

                System.out.println("Gagal ambil IHS Pasien : " + e);

                showModernToast(
                        this,
                        "Gagal ambil IHS Pasien : "
                        + e.getClass().getSimpleName()
                        + " - "
                        + safe(e.getMessage()),
                        ToastMessage.ERROR,
                        1
                );
            }
//            try {
//                
//                
//                
//                if (!noKtpPasien.isEmpty()) {
//                    idPasienSatuSehat = cekViaSatuSehat.tampilIDPasien(noKtpPasien);
//                }
//                
//                
//                
//            } catch (Exception e) {
//                System.out.println("Gagal ambil IHS Pasien : " + e);
//                ToastMessage.showToast(
//                        null,
//                        "Gagal ambil IHS Pasien : "
//                                + e.getClass().getSimpleName()
//                                + " - "
//                                + safe(e.getMessage()),
//                        ToastMessage.ERROR,1
//                );
//            }

            tIdPasienIhs.setText(idPasienSatuSehat);

            // =========================
            // LOAD IHS DOKTER
            // =========================
            try {

    kdDokterSatuSehat = "";

    if (!noKtpDokter.isEmpty()) {

        // ==========================
        // CEK CACHE LOKAL
        // ==========================
        kdDokterSatuSehat = Sequel.cariIsi(
                "SELECT ihs_praktisi FROM satu_sehat_praktisi WHERE no_ktp=?",
                noKtpDokter
        );

        if (!kdDokterSatuSehat.isEmpty()) {

            System.out.println(
                    "IHS Praktisi ditemukan di cache lokal : "
                    + kdDokterSatuSehat
            );

        } else {

            System.out.println(
                    "IHS Praktisi tidak ditemukan di cache, cek SATUSEHAT"
            );

            // ==========================
            // CEK SATUSEHAT
            // ==========================
            kdDokterSatuSehat = cekViaSatuSehat.tampilIDParktisi(noKtpDokter);

            if (!kdDokterSatuSehat.isEmpty()) {

                // ==========================
                // SIMPAN MASTER
                // ==========================
                Sequel.SimpanData(
                        "satu_sehat_praktisi",
                        new String[]{
                            "no_ktp",
                            "kd_dokter",
                            "ihs_praktisi"
                        },
                        new String[]{
                            noKtpDokter,
                            kdDokterRs,
                            kdDokterSatuSehat
                        }
                );

                // ==========================
                // SIMPAN LOG
                // ==========================
                Sequel.SimpanData(
                        "satu_sehat_ihs_log",
                        new String[]{
                            "jenis",
                            "referensi",
                            "no_ktp",
                            "ihs_number",
                            "status_sync",
                            "keterangan"
                        },
                        new String[]{
                            "PRAKTISI",
                            kdDokterRs,
                            noKtpDokter,
                            kdDokterSatuSehat,
                            "SUKSES",
                            "Sinkron awal SATUSEHAT"
                        }
                );

                System.out.println(
                        "IHS Praktisi berhasil disimpan : "
                        + kdDokterSatuSehat
                );

            } else {

                // ==========================
                // LOG GAGAL
                // ==========================
                Sequel.SimpanData(
                        "satu_sehat_ihs_log",
                        new String[]{
                            "jenis",
                            "referensi",
                            "no_ktp",
                            "status_sync",
                            "keterangan"
                        },
                        new String[]{
                            "PRAKTISI",
                            kdDokterRs,
                            noKtpDokter,
                            "GAGAL",
                            "IHS Praktisi tidak ditemukan di SATUSEHAT"
                        }
                );

                System.out.println(
                        "IHS Praktisi tidak ditemukan di SATUSEHAT"
                );
            }
        }
    }

} catch (Exception e) {

    System.out.println(
            "Gagal ambil IHS Dokter : " + e
    );

    showModernToast(
            this,
            "Gagal ambil IHS Dokter : "
            + e.getClass().getSimpleName()
            + " - "
            + safe(e.getMessage()),
            ToastMessage.ERROR,
            1
    );
}
//            try {
//
//                if (!noKtpDokter.isEmpty()) {
//
//                    kdDokterSatuSehat = cekViaSatuSehat.tampilIDParktisi(noKtpDokter);
//
//                }
//
//            } catch (Exception e) {
//
//                System.out.println(
//                        "Gagal ambil IHS Dokter : " + e);
//                
//                ToastMessage.showToast(
//                        null,
//                        "Gagal ambil IHS Dokter : "
//                                + e.getClass().getSimpleName()
//                                + " - "
//                                + safe(e.getMessage()),
//                        ToastMessage.ERROR,1
//                );
//
//            }

            tIdDokterIhs.setText(kdDokterSatuSehat);

            // =========================
            // KODE SPESIALIS
            // =========================
            kodeSpesialis = this.kdPoliBpjs;

            // =========================
            // VALIDASI
            // =========================
            StringBuilder warn =
                    new StringBuilder();

            if (encounterRef.isEmpty()) {

                warn.append(
                        "Encounter Satu Sehat belum ada. ");

            }

            if (idPasienSatuSehat.isEmpty()) {

                warn.append(
                        "IHS Pasien tidak ditemukan. ");

            }

            if (kdDokterSatuSehat.isEmpty()) {

                warn.append(
                        "IHS Dokter tidak ditemukan. ");

            }
             if (noSep.isEmpty()) {

                warn.append(
                        "Pasein Belum dibuatkan SEP !!!! ");

            }
            

            if (warn.length() > 0) {

                setStatus(
                        "PERINGATAN : " + warn.toString(),
                        true
                );
            
                showModernToast(
                        this,
                        "Data Rujukan SATU SEHAT belum lengkap",
                        ToastMessage.WARNING,
                        0
                );
                

            } else {

                setStatus(
                        "Data Satu Sehat lengkap. Klik [Cek Kriteria].",
                        false
                );

            }

        } else {

            showModernToast(
                    this,
                    "Data pasien tidak ditemukan.",
                    ToastMessage.WARNING,
                    0
            );

        }

    } catch (Exception e) {

        System.out.println(
                "Gagal load data rujukan : " + e);

    } finally {

        try {

            if (rs != null) {
                rs.close();
            }

            if (ps != null) {
                ps.close();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // Setelah data dasar selesai dimuat, pulihkan rujukan aktif (jika ada).
    loadSavedReferralSnapshot();
}
    private void loadDataSatuSehat() {
        // 1. Encounter Satu Sehat (dari satu_sehat_encounter)
        encounterRef = Sequel.cariIsi(
                "select id_encounter from satu_sehat_encounter where no_rawat=?",
                noRawat);
        tEncounter.setText(encounterRef);

        // 2. IHS Pasien (lookup dari NIK pasien)
        try {
            String noKtpPasien = Sequel.cariIsi(
                    "select pasien.no_ktp from pasien where pasien.no_rkm_medis=?",
                    noRm);
            if (noKtpPasien != null && !noKtpPasien.isEmpty()) {
                idPasienSatuSehat = cekViaSatuSehat.tampilIDPasien(noKtpPasien);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil IHS Pasien: " + e);
        }
        tIdPasienIhs.setText(idPasienSatuSehat);

        // 3. IHS Dokter (lookup dari NIK pegawai/dokter)
        try {
            String noKtpDokter = Sequel.cariIsi(
                    "select pegawai.no_ktp from pegawai where pegawai.nik=?",
                    kdDokterRs);
            if (noKtpDokter != null && !noKtpDokter.isEmpty()) {
                kdDokterSatuSehat = cekViaSatuSehat.tampilIDParktisi(noKtpDokter);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil IHS Dokter: " + e);
        }
        tIdDokterIhs.setText(kdDokterSatuSehat);

        // 4. Kode Spesialis untuk GetFaskesRujukan = kd_poli_bpjs
        //    (sesuai diskusi: pakai kode poli BPJS sebagai kodeSpesialis Sisrute)
        kodeSpesialis = kdPoliBpjs;

        // Validasi
        StringBuilder warn = new StringBuilder();
        if (encounterRef.isEmpty()) {
            warn.append("Encounter Satu Sehat belum ada untuk no_rawat ini. ");
        }
        if (idPasienSatuSehat.isEmpty()) {
            warn.append("IHS Pasien tidak ditemukan. ");
        }
        if (kdDokterSatuSehat.isEmpty()) {
            warn.append("IHS Dokter tidak ditemukan. ");
        }

        if (warn.length() > 0) {
            setStatus("PERINGATAN: " + warn.toString(), true);
            showModernToast(this,
                    "Data Satu Sehat belum lengkap:\n" + warn.toString()
                            + "\nLengkapi data sebelum mengirim rujukan.",
                    ToastMessage.WARNING, 0);
        } else {
            setStatus("Data Satu Sehat lengkap. Klik [Cek Kriteria] untuk mulai.", false);
        }
    }

    // =================================================================
    //  ACTION: PILIH DIAGNOSA RUJUKAN (popup BPJSCekReferensiPenyakit)
    // =================================================================
    private void doPilihDiagnosa() {
        popupPenyakit.setSize(920, 560);
        popupPenyakit.getTable().clearSelection();
        popupPenyakit.setLocationRelativeTo(this);
        showModalDialogWithBlur(popupPenyakit);
        // hasil dipilih -> handler windowClosed di setupPopupListeners()
    }

    // =================================================================
    //  ACTION: PILIH POLI RUJUKAN (popup BPJSCekReferensiPoli)
    // =================================================================
    private void doPilihPoli() {
        popupPoli.setSize(920, 560);
        popupPoli.getTable().clearSelection();
        popupPoli.setLocationRelativeTo(this);
        showModalDialogWithBlur(popupPoli);
        // hasil dipilih -> handler windowClosed di setupPopupListeners()
    }

    // =================================================================
    //  DIALOG KONFIRMASI ENCOUNTER SATUSEHAT
    //  Hanya mengganti tampilan konfirmasi; proses POST tetap di method lama.
    // =================================================================
    private boolean showEncounterConfirmationDialog() {
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        final boolean[] confirmed = {false};

        dialog.setUndecorated(true);
        configureRoundedPopupWindow(dialog, true);
        dialog.setResizable(false);
        dialog.setTitle("Konfirmasi Encounter SATUSEHAT");
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        java.awt.Color pageColor = new java.awt.Color(248, 250, 252);
        java.awt.Color lineColor = new java.awt.Color(226, 232, 240);
        java.awt.Color primaryColor = new java.awt.Color(37, 99, 235);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(pageColor);
        root.setBorder(new RoundedPopupBorder(
                new java.awt.Color(203, 213, 225)));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(14, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, lineColor),
                javax.swing.BorderFactory.createEmptyBorder(16, 28, 14, 12)));
        header.setPreferredSize(new java.awt.Dimension(10, 84));

        javax.swing.JPanel titleArea = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        titleArea.setOpaque(false);

        javax.swing.JPanel accent = new javax.swing.JPanel();
        accent.setBackground(primaryColor);
        accent.setPreferredSize(new java.awt.Dimension(5, 48));

        javax.swing.JPanel accentBox = new javax.swing.JPanel(new java.awt.GridBagLayout());
        accentBox.setOpaque(false);
        accentBox.add(accent);

        javax.swing.JPanel heading = new javax.swing.JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Konfirmasi Encounter SATUSEHAT");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 17));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Periksa kembali data berikut sebelum dikirim.");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));

        heading.add(title);
        heading.add(subtitle);
        titleArea.add(accentBox, java.awt.BorderLayout.WEST);
        titleArea.add(heading, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel headerActions = new javax.swing.JPanel();
        headerActions.setOpaque(false);
        headerActions.setLayout(new javax.swing.BoxLayout(
                headerActions, javax.swing.BoxLayout.X_AXIS));

        javax.swing.JLabel satuSehatBadge = new EncounterPillLabel(
                "SATUSEHAT", new java.awt.Color(239, 246, 255), 8);
        satuSehatBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        satuSehatBadge.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        satuSehatBadge.setForeground(new java.awt.Color(30, 64, 175));
        satuSehatBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 13, 6, 13));
        satuSehatBadge.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.JButton close = createFlatCloseButton();
        close.setToolTipText("Tutup");
        close.addActionListener(e -> dialog.dispose());

        headerActions.add(satuSehatBadge);
        headerActions.add(javax.swing.Box.createHorizontalStrut(12));
        headerActions.add(close);

        header.add(titleArea, java.awt.BorderLayout.CENTER);
        header.add(headerActions, java.awt.BorderLayout.EAST);

        javax.swing.JPanel body = new javax.swing.JPanel();
        body.setBackground(pageColor);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 28, 26, 28));

        javax.swing.JPanel dataCard = createEncounterDataCard();
        dataCard.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(dataCard);
        body.add(javax.swing.Box.createVerticalStrut(12));

        javax.swing.JPanel warningCard = createEncounterMessageCard(
                true,
                "Perhatian",
                "Encounter yang telah dikirim tidak dapat diubah secara bebas. "
                + "Pastikan pasien, dokter, dan nomor rawat sudah sesuai.");
        warningCard.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(warningCard);
        body.add(javax.swing.Box.createVerticalStrut(12));

        javax.swing.JPanel processCard = createEncounterProcessCard();
        processCard.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(processCard);

        javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor),
                javax.swing.BorderFactory.createEmptyBorder(12, 28, 12, 28)));
        footer.setPreferredSize(new java.awt.Dimension(10, 62));

        javax.swing.JLabel reminder = new javax.swing.JLabel(
                "Pastikan seluruh data telah sesuai sebelum melanjutkan.");
        reminder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        reminder.setForeground(new java.awt.Color(100, 116, 139));

        javax.swing.JPanel actions = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        javax.swing.JButton cancelButton = new EncounterActionButton("Batal", false);
        styleEncounterDialogButton(cancelButton, false);
        cancelButton.setPreferredSize(new java.awt.Dimension(100, 34));
        cancelButton.addActionListener(e -> dialog.dispose());

        javax.swing.JButton confirmButton = new EncounterActionButton(
                "Buat Encounter", true);
        styleEncounterDialogButton(confirmButton, true);
        confirmButton.setIcon(new EncounterCheckIcon(java.awt.Color.WHITE, 14));
        confirmButton.setIconTextGap(7);
        confirmButton.setPreferredSize(new java.awt.Dimension(146, 34));
        confirmButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        actions.add(cancelButton);
        actions.add(confirmButton);
        footer.add(reminder, java.awt.BorderLayout.CENTER);
        footer.add(actions, java.awt.BorderLayout.EAST);

        root.add(header, java.awt.BorderLayout.NORTH);
        root.add(body, java.awt.BorderLayout.CENTER);
        root.add(footer, java.awt.BorderLayout.SOUTH);
        dialog.setContentPane(root);

        dialog.getRootPane().setDefaultButton(confirmButton);
        dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(
                        java.awt.event.KeyEvent.VK_ESCAPE, 0), "cancel-encounter");
        dialog.getRootPane().getActionMap().put("cancel-encounter",
                new javax.swing.AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        dialog.dispose();
                    }
                });

        installDialogDragSupport(header, dialog);
        dialog.setSize(760, 630);
        dialog.setLocationRelativeTo(this);
        showModalDialogWithBlur(dialog);
        return confirmed[0];
    }

    private javax.swing.JPanel createEncounterDataCard() {
        java.awt.Color lineColor = new java.awt.Color(226, 232, 240);

        javax.swing.JPanel card = new EncounterRoundedPanel(
                new java.awt.BorderLayout(), java.awt.Color.WHITE, lineColor, 10);
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 240));
        card.setPreferredSize(new java.awt.Dimension(100, 240));

        javax.swing.JPanel cardHeader = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 10));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(javax.swing.BorderFactory.createMatteBorder(
                0, 0, 1, 0, lineColor));
        cardHeader.setPreferredSize(new java.awt.Dimension(10, 42));

        javax.swing.JLabel cardTitle = new javax.swing.JLabel("Data Encounter");
        cardTitle.setIcon(new EncounterDocumentIcon());
        cardTitle.setIconTextGap(8);
        cardTitle.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        cardTitle.setForeground(new java.awt.Color(30, 64, 175));
        cardHeader.add(cardTitle);

        javax.swing.JPanel rows = new javax.swing.JPanel();
        rows.setOpaque(false);
        rows.setLayout(new javax.swing.BoxLayout(rows, javax.swing.BoxLayout.Y_AXIS));

        rows.add(createEncounterDataRow(
                EncounterDataIcon.PATIENT,
                "Nama Pasien", namaPasien, false, false));
        rows.add(createEncounterDataRow(
                EncounterDataIcon.CLIPBOARD,
                "No. Rawat", noRawat, false, false));
        rows.add(createEncounterDataRow(
                EncounterDataIcon.ID_CARD,
                "IHS Pasien", idPasienSatuSehat, false, false));
        rows.add(createEncounterDataRow(
                EncounterDataIcon.DOCTOR,
                "IHS Dokter", kdDokterSatuSehat, false, false));
        rows.add(createEncounterDataRow(
                EncounterDataIcon.CALENDAR,
                "Tanggal Encounter",
                new java.text.SimpleDateFormat("dd MMMM yyyy",
                        new java.util.Locale("id", "ID")).format(new java.util.Date()),
                false, false));
        rows.add(createEncounterDataRow(
                EncounterDataIcon.FLAG,
                "Status Encounter", "ARRIVED", true, true));

        card.add(cardHeader, java.awt.BorderLayout.NORTH);
        card.add(rows, java.awt.BorderLayout.CENTER);
        return card;
    }

    private javax.swing.JPanel createEncounterDataRow(int iconType, String labelText,
            String valueText, boolean badgeValue, boolean lastRow) {
        javax.swing.JPanel row = new javax.swing.JPanel(new java.awt.BorderLayout(14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 33));
        row.setPreferredSize(new java.awt.Dimension(100, 33));

        javax.swing.border.Border padding = javax.swing.BorderFactory.createEmptyBorder(
                0, 14, 0, 14);
        if (lastRow) {
            row.setBorder(padding);
        } else {
            row.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(
                            0, 0, 1, 0, new java.awt.Color(241, 245, 249)),
                    padding));
        }

        javax.swing.JPanel labelBox = new javax.swing.JPanel(
                new java.awt.BorderLayout(7, 0));
        labelBox.setOpaque(false);
        labelBox.setPreferredSize(new java.awt.Dimension(230, 30));

        javax.swing.JLabel icon = new javax.swing.JLabel(new EncounterDataIcon(iconType));
        icon.setPreferredSize(new java.awt.Dimension(22, 30));

        javax.swing.JLabel label = new javax.swing.JLabel(labelText);
        label.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        label.setForeground(new java.awt.Color(71, 85, 105));
        labelBox.add(icon, java.awt.BorderLayout.WEST);
        labelBox.add(label, java.awt.BorderLayout.CENTER);
        row.add(labelBox, java.awt.BorderLayout.WEST);

        if (badgeValue) {
            javax.swing.JPanel badgeBox = new javax.swing.JPanel(
                    new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 7));
            badgeBox.setOpaque(false);

            javax.swing.JLabel badge = new EncounterPillLabel(
                    safe(valueText), new java.awt.Color(37, 99, 235), 8);
            badge.setFont(new java.awt.Font(
                    "Segoe UI Semibold", java.awt.Font.PLAIN, 10));
            badge.setForeground(java.awt.Color.WHITE);
            badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 9, 3, 9));
            badgeBox.add(badge);
            row.add(badgeBox, java.awt.BorderLayout.CENTER);
        } else {
            javax.swing.JLabel value = new javax.swing.JLabel(safe(valueText));
            value.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            value.setForeground(new java.awt.Color(30, 41, 59));
            row.add(value, java.awt.BorderLayout.CENTER);
        }
        return row;
    }

    private javax.swing.JPanel createEncounterMessageCard(boolean warning,
            String titleText, String messageText) {
        java.awt.Color background = warning
                ? new java.awt.Color(255, 251, 235)
                : new java.awt.Color(239, 246, 255);
        java.awt.Color foreground = warning
                ? new java.awt.Color(180, 83, 9)
                : new java.awt.Color(29, 78, 216);
        java.awt.Color border = warning
                ? new java.awt.Color(253, 230, 138)
                : new java.awt.Color(191, 219, 254);

        javax.swing.JPanel card = new EncounterRoundedPanel(
                new java.awt.BorderLayout(10, 0), background, border, 10);
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 64));
        card.setPreferredSize(new java.awt.Dimension(100, 64));

        card.add(new EncounterCalloutIcon(warning), java.awt.BorderLayout.WEST);

        javax.swing.JPanel content = new javax.swing.JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel(titleText);
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        title.setForeground(foreground);
        title.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        javax.swing.JTextArea message = new javax.swing.JTextArea(messageText);
        message.setEditable(false);
        message.setFocusable(false);
        message.setOpaque(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        message.setForeground(new java.awt.Color(71, 85, 105));
        message.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 0, 0));
        message.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(message);
        card.add(content, java.awt.BorderLayout.CENTER);
        return card;
    }

    private javax.swing.JPanel createEncounterProcessCard() {
        javax.swing.JPanel card = new EncounterRoundedPanel(
                new java.awt.BorderLayout(10, 0),
                new java.awt.Color(239, 246, 255),
                new java.awt.Color(191, 219, 254), 10);
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 12, 9, 12));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 112));
        card.setPreferredSize(new java.awt.Dimension(100, 112));
        card.add(new EncounterCalloutIcon(false), java.awt.BorderLayout.WEST);

        javax.swing.JPanel content = new javax.swing.JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Proses yang akan dilakukan");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        title.setForeground(new java.awt.Color(29, 78, 216));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 3, 0));
        title.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(createEncounterChecklistRow(
                "Membuat Encounter baru di SATUSEHAT"));
        content.add(createEncounterChecklistRow(
                "Menghubungkan pasien dengan dokter yang dipilih"));
        content.add(createEncounterChecklistRow(
                "Menyiapkan referensi Encounter untuk proses rujukan berikutnya"));
        card.add(content, java.awt.BorderLayout.CENTER);
        return card;
    }

    private javax.swing.JPanel createEncounterChecklistRow(String text) {
        javax.swing.JPanel row = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 1));
        row.setOpaque(false);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setIcon(new EncounterCheckIcon(new java.awt.Color(37, 99, 235), 14));
        label.setIconTextGap(7);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        label.setForeground(new java.awt.Color(51, 65, 85));
        row.add(label);
        return row;
    }

    private void styleEncounterDialogButton(javax.swing.JButton button, boolean primary) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);

        if (primary) {
            button.setForeground(java.awt.Color.WHITE);
            button.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
        } else {
            button.setForeground(new java.awt.Color(51, 65, 85));
            button.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }
    }

    private static final class EncounterRoundedPanel extends javax.swing.JPanel {
        private final java.awt.Color fillColor;
        private final java.awt.Color outlineColor;
        private final int arc;

        EncounterRoundedPanel(java.awt.LayoutManager layout,
                java.awt.Color fillColor, java.awt.Color outlineColor, int arc) {
            super(layout);
            this.fillColor = fillColor;
            this.outlineColor = outlineColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(outlineColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    private static final class EncounterPillLabel extends javax.swing.JLabel {
        private final java.awt.Color fillColor;
        private final int arc;

        EncounterPillLabel(String text, java.awt.Color fillColor, int arc) {
            super(text);
            this.fillColor = fillColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class EncounterActionButton extends javax.swing.JButton {
        private final boolean primary;

        EncounterActionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            boolean pressed = getModel().isArmed() && getModel().isPressed();
            boolean rollover = getModel().isRollover();
            java.awt.Color fill;
            if (primary) {
                fill = pressed
                        ? new java.awt.Color(29, 78, 216)
                        : (rollover
                                ? new java.awt.Color(30, 64, 175)
                                : new java.awt.Color(37, 99, 235));
            } else {
                fill = pressed
                        ? new java.awt.Color(241, 245, 249)
                        : (rollover
                                ? new java.awt.Color(248, 250, 252)
                                : java.awt.Color.WHITE);
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            if (!primary) {
                g2.setColor(new java.awt.Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class DangerActionButton extends javax.swing.JButton {
        DangerActionButton(String text) {
            super(text);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.Color fill;
                if (!isEnabled()) {
                    fill = new java.awt.Color(203, 213, 225);
                } else if (getModel().isArmed() && getModel().isPressed()) {
                    fill = new java.awt.Color(153, 27, 27);
                } else if (getModel().isRollover()) {
                    fill = new java.awt.Color(185, 28, 28);
                } else {
                    fill = new java.awt.Color(220, 38, 38);
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1,
                        getHeight() - 1, 7, 7);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private static final class ReferralCopyIcon implements javax.swing.Icon {
        private final java.awt.Color color;

        ReferralCopyIcon(java.awt.Color color) {
            this.color = color;
        }

        @Override public int getIconWidth() { return 15; }
        @Override public int getIconHeight() { return 15; }

        @Override
        public void paintIcon(java.awt.Component component,
                java.awt.Graphics graphics, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(component.isEnabled()
                        ? color : new java.awt.Color(148, 163, 184));
                g2.setStroke(new java.awt.BasicStroke(1.3f));
                g2.drawRoundRect(x + 1, y + 1, 9, 10, 2, 2);
                g2.drawRoundRect(x + 5, y + 4, 9, 10, 2, 2);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class EncounterDataIcon implements javax.swing.Icon {
        static final int PATIENT = 0;
        static final int CLIPBOARD = 1;
        static final int ID_CARD = 2;
        static final int DOCTOR = 3;
        static final int CALENDAR = 4;
        static final int FLAG = 5;

        private final int type;

        EncounterDataIcon(int type) {
            this.type = type;
        }

        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new java.awt.Color(37, 99, 235));
            g2.setStroke(new java.awt.BasicStroke(1.5f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));

            if (type == PATIENT) {
                g2.drawOval(x + 6, y + 2, 6, 6);
                g2.drawArc(x + 3, y + 9, 12, 8, 0, 180);
            } else if (type == CLIPBOARD) {
                g2.drawRoundRect(x + 3, y + 3, 12, 13, 2, 2);
                g2.drawRoundRect(x + 6, y + 1, 6, 4, 2, 2);
                g2.drawLine(x + 6, y + 8, x + 12, y + 8);
                g2.drawLine(x + 6, y + 12, x + 12, y + 12);
            } else if (type == ID_CARD) {
                g2.drawRoundRect(x + 1, y + 3, 16, 12, 2, 2);
                g2.drawOval(x + 4, y + 6, 4, 4);
                g2.drawArc(x + 3, y + 10, 6, 3, 0, 180);
                g2.drawLine(x + 11, y + 7, x + 15, y + 7);
                g2.drawLine(x + 11, y + 11, x + 15, y + 11);
            } else if (type == DOCTOR) {
                g2.drawOval(x + 4, y + 2, 6, 6);
                g2.drawArc(x + 1, y + 9, 12, 8, 0, 180);
                g2.drawLine(x + 14, y + 10, x + 14, y + 16);
                g2.drawLine(x + 11, y + 13, x + 17, y + 13);
            } else if (type == CALENDAR) {
                g2.drawRoundRect(x + 2, y + 3, 14, 13, 2, 2);
                g2.drawLine(x + 2, y + 7, x + 16, y + 7);
                g2.drawLine(x + 6, y + 1, x + 6, y + 5);
                g2.drawLine(x + 12, y + 1, x + 12, y + 5);
                g2.fillRect(x + 5, y + 10, 2, 2);
                g2.fillRect(x + 10, y + 10, 2, 2);
            } else {
                g2.drawLine(x + 3, y + 2, x + 3, y + 17);
                java.awt.Polygon flag = new java.awt.Polygon(
                        new int[]{x + 4, x + 15, x + 12, x + 4},
                        new int[]{y + 3, y + 3, y + 9, y + 9}, 4);
                g2.drawPolygon(flag);
            }
            g2.dispose();
        }
    }

    private static final class EncounterDocumentIcon implements javax.swing.Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new java.awt.Color(37, 99, 235));
            g2.setStroke(new java.awt.BasicStroke(1.7f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x + 2, y + 1, 13, 16, 2, 2);
            g2.drawLine(x + 5, y + 6, x + 12, y + 6);
            g2.drawLine(x + 5, y + 10, x + 12, y + 10);
            g2.drawLine(x + 5, y + 14, x + 10, y + 14);
            g2.dispose();
        }
    }

    private static final class EncounterCalloutIcon extends javax.swing.JComponent {
        private final boolean warning;

        EncounterCalloutIcon(boolean warning) {
            this.warning = warning;
            setPreferredSize(new java.awt.Dimension(26, 26));
            setMinimumSize(new java.awt.Dimension(26, 26));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new java.awt.BasicStroke(1.8f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));

            if (warning) {
                g2.setColor(new java.awt.Color(245, 158, 11));
                java.awt.Polygon triangle = new java.awt.Polygon(
                        new int[]{13, 3, 23}, new int[]{3, 22, 22}, 3);
                g2.drawPolygon(triangle);
                g2.drawLine(13, 9, 13, 15);
                g2.fillOval(12, 18, 2, 2);
            } else {
                g2.setColor(new java.awt.Color(37, 99, 235));
                g2.drawOval(3, 3, 20, 20);
                g2.drawLine(13, 11, 13, 18);
                g2.fillOval(12, 7, 2, 2);
            }
            g2.dispose();
        }
    }

    private static final class EncounterCheckIcon implements javax.swing.Icon {
        private final java.awt.Color color;
        private final int size;

        EncounterCheckIcon(java.awt.Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(1.7f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g2.drawOval(x + 1, y + 1, size - 3, size - 3);
            g2.drawLine(x + 4, y + size / 2, x + 6, y + size - 5);
            g2.drawLine(x + 6, y + size - 5, x + size - 4, y + 4);
            g2.dispose();
        }
    }

    // =================================================================
    //  ACTION: BUAT ENCOUNTER BARU
    //  Langsung POST minimal Encounter ke Satu Sehat (tanpa buka form lain).
    //  Encounter akan disimpan ke satu_sehat_encounter dan field tEncounter
    //  langsung terisi.
    // =================================================================
    private void doBuatEncounter() {
        // Validasi prerequisite
//        if (kosong(tNoSep,
//        "Data SEP belum di-set. Tutup form dan pilih SEP terlebih dahulu.")) return ;

        if (kosong(tNoRawat,
                "PILIH DULU DATA PASIEN.")) return ;

//        if (kosong(tEncounter,
//                "Encounter Satu Sehat belum ada untuk no_rawat ini. Silakan kirim Encounter dulu.")) return ;

        if (kosong(tIdPasienIhs,
                "IHS Pasien tidak ditemukan. Pastikan pasien sudah punya IHS di Satu Sehat.")) return ;

        if (kosong(tIdDokterIhs,
                "IHS Dokter tidak ditemukan. Pastikan dokter sudah punya IHS di Satu Sehat.")) return ;
        
     
        // Cek lagi ke DB apakah encounter sudah ada (in case di-create di session lain)
        String existing = Sequel.cariIsi(
                "select id_encounter from satu_sehat_encounter where no_rawat=?",
                noRawat);
        if (existing != null && !existing.isEmpty()) {
            encounterRef = existing;
            tEncounter.setText(existing);
            showModernToast(this,
                    "Encounter sudah ada untuk no_rawat ini:\n" + existing,
                    TOAST_INFO, 0);
            return;
        }
        if (!showEncounterConfirmationDialog()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Mengirim Encounter ke Satu Sehat...", false);
        try {
            String idEncounter = postEncounterMinimal();
            if (idEncounter != null && !idEncounter.isEmpty()) {
                // Simpan ke satu_sehat_encounter (sesuai pattern existing)
                Sequel.menyimpantf2("satu_sehat_encounter", "?,?", "Encounter Rujukan", 2,
                        new String[]{noRawat, idEncounter});
                encounterRef = idEncounter;
                tEncounter.setText(idEncounter);
                setStatus("Encounter berhasil dibuat: " + idEncounter, false);
                showModernToast(this,
                        "Encounter berhasil dibuat:\n" + idEncounter,
                        ToastMessage.SUCCESS, 0);
            } else {
                setStatus("Gagal buat Encounter (response kosong).", true);
            }
        } catch (Exception ex) {
            rememberApiException("SATUSEHAT - POST Encounter", ex);
            setStatus("Error: " + ex.getMessage(), true);
            showModernToast(this,
                    "Gagal membuat Encounter:\n" + safe(ex.getMessage()),
                    ToastMessage.ERROR, 0);
            ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * POST minimal Encounter ke Satu Sehat (tanpa diagnosis/treatment detail).
     * Cukup untuk dipakai di rujukan rajal.
     * @return id Encounter yang baru di-create, atau null kalau gagal
     */
    private String postEncounterMinimal() throws Exception {

    ApiSatuSehat ihs = new ApiSatuSehat();

    String orgId = ihs.getOrgIdPerujuk();
    String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String encounterTime = today + "T00:00:00+07:00";

    String noRawat = tNoRawat.getText();

    // =========================
    // AMBIL DATA LOKASI & DOKTER
    // =========================
    String nmPoli = "";
    String idLokasiSatuSehat = "";
    String namaDokter = "";

    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        ps = koneksi.prepareStatement(
            "select p.nm_poli, m.id_lokasi_satusehat, pg.nama " +
            "from reg_periksa rp " +
            "inner join poliklinik p on p.kd_poli = rp.kd_poli " +
            "inner join satu_sehat_mapping_lokasi_ralan m on m.kd_poli = p.kd_poli " +
            "inner join pegawai pg on pg.nik = rp.kd_dokter " +
            "where rp.no_rawat = ? "
        );

        ps.setString(1, noRawat);
        rs = ps.executeQuery();

        if (rs.next()) {
            nmPoli = rs.getString("nm_poli");
            idLokasiSatuSehat = rs.getString("id_lokasi_satusehat");
            namaDokter = rs.getString("nama");
        }

    } finally {
        if (rs != null) rs.close();
        if (ps != null) ps.close();
    }

    // =========================
    // BUILD JSON ENCOUNTER
    // =========================
    StringBuilder sb = new StringBuilder();

    sb.append("{")
      .append("\"resourceType\":\"Encounter\",")
      .append("\"status\":\"arrived\",")

      .append("\"class\":{")
          .append("\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\",")
          .append("\"code\":\"AMB\",")
          .append("\"display\":\"ambulatory\"")
      .append("},")

      .append("\"subject\":{")
          .append("\"reference\":\"Patient/")
          .append(idPasienSatuSehat)
          .append("\",")
          .append("\"display\":\"")
          .append(escapeJson(namaPasien))
          .append("\"")
      .append("},")

      .append("\"participant\":[{")
          .append("\"type\":[{")
              .append("\"coding\":[{")
                  .append("\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\",")
                  .append("\"code\":\"ATND\",")
                  .append("\"display\":\"attender\"")
              .append("}]")
          .append("}],")

          .append("\"individual\":{")
              .append("\"reference\":\"Practitioner/")
              .append(kdDokterSatuSehat)
              .append("\",")
              .append("\"display\":\"")
              .append(escapeJson(namaDokter))
              .append("\"")
          .append("}")
      .append("}],")

      .append("\"period\":{")
          .append("\"start\":\"")
          .append(encounterTime)
          .append("\"")
      .append("},")

      .append("\"statusHistory\":[{")
          .append("\"status\":\"arrived\",")
          .append("\"period\":{")
              .append("\"start\":\"")
              .append(encounterTime)
              .append("\",")
              .append("\"end\":\"")
              .append(encounterTime)
              .append("\"")
          .append("}")
      .append("}],")

      // =========================
      // LOCATION
      // =========================
      .append("\"location\":[{")
          .append("\"location\":{")
              .append("\"reference\":\"Location/")
              .append(idLokasiSatuSehat)
              .append("\",")
              .append("\"display\":\"")
              .append(escapeJson(nmPoli))
              .append("\"")
          .append("}")
      .append("}],")

      .append("\"serviceProvider\":{")
          .append("\"reference\":\"Organization/")
          .append(orgId)
          .append("\"")
      .append("},")

      .append("\"identifier\":[{")
          .append("\"system\":\"http://sys-ids.kemkes.go.id/encounter/")
          .append(orgId)
          .append("\",")
          .append("\"value\":\"")
          .append(noRawat)
          .append("\"")
      .append("}]")

    .append("}");

    String body = sb.toString();

    String baseUrl = ihs.getBaseUrl();

    org.springframework.http.HttpHeaders h = ihs.buildAuthHeaders();
    h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    org.springframework.http.HttpEntity<String> req =
            new org.springframework.http.HttpEntity<>(body, h);

    System.out.println("==== POST Encounter ====");
    System.out.println("URL : " + baseUrl + "/Encounter");
    System.out.println("Body : " + body);

    try {

    String json = ihs.getRest()
            .exchange(
                    baseUrl + "/Encounter",
                    org.springframework.http.HttpMethod.POST,
                    req,
                    String.class
            )
            .getBody();

    rememberApiResponse("SATUSEHAT - POST Encounter", json);

    System.out.println("RESPONSE:");
    System.out.println(json);

    JsonNode root = mapper.readTree(json);
    return root.path("id").asText();

} catch (Exception e) {
    
    System.out.println("===== REQUEST URL =====");
    System.out.println(baseUrl + "/Encounter");

    System.out.println("===== REQUEST JSON =====");
    System.out.println(body);

    System.out.println("===== ERROR CLASS =====");
    System.out.println(e.getClass().getName());

    System.out.println("===== ERROR MESSAGE =====");
    System.out.println(e.getMessage());

    e.printStackTrace();

    throw e;
}
}
    

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Mengambil seluruh kode tindakan ICD-9 yang sudah tercatat untuk no_rawat
     * aktif. Kode digabung dengan koma agar konsisten dengan tampilan textbox
     * tindakan medis dan tetap dikirim sebagai satu jawaban kriteria.
     */
    private String loadKodeTindakanPasien() {
        String rawat = safe(noRawat);
        if (rawat.isEmpty() && tNoRawat != null) {
            rawat = safe(tNoRawat.getText());
        }
        if (rawat.isEmpty()) return "";

        java.util.LinkedHashSet<String> kodeTindakan =
                new java.util.LinkedHashSet<>();
        String sql = "select kode from prosedur_pasien "
                + "where no_rawat=? order by kode";

        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            statement.setString(1, rawat);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String kode = safe(result.getString("kode"));
                    if (!kode.isEmpty()) kodeTindakan.add(kode);
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal mengambil tindakan medis untuk no_rawat "
                    + rawat + " : " + e.getMessage());
        }

        return String.join(", ", kodeTindakan);
    }

    // =================================================================
    //  ACTION 1: CEK KRITERIA RUJUKAN
    //  POST /Rujukan/GetKriteriaRujukan
    // =================================================================
    private void doCekKriteria() {
        if (!validateBeforeApi()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Memanggil GetKriteriaRujukan...", false);
        try {
            JsonNode resp = sisrute.getKriteriaRujukan(
                    kodeFaskesSatuSehat, kdPenyakitRujuk, encounterRef);

            rememberApiResponse("BPJS/SISRUTE - Get Kriteria Rujukan", resp);

//            if (!SisruteService.isOk(resp)) {
//                JOptionPane.showMessageDialog(this,
//                        "Gagal: " + SisruteService.getMessage(resp),
//                        "Sisrute", JOptionPane.ERROR_MESSAGE);
//                setStatus("Gagal: " + SisruteService.getMessage(resp), true);
//                return;
//            }
            
            if (!SisruteService.isOk(resp)) {

                String msg =
                        safe(SisruteService.getMessage(resp));

                showModernToast(
                        this,
                        "Gagal Sisrute :<br>" + msg,
                        ToastMessage.ERROR,
                        0
                );

                setStatus(
                        "Gagal: " + msg,
                        true
                );

                return;
            }
            
            // Isi tabel kriteria
            clearFaskesResultsAfterCriteriaChange();
            modelKriteria.setRowCount(0);
            String kodeTindakanPasien = loadKodeTindakanPasien();
            JsonNode arr = resp.path("response").path("kriteriaRujukan");
            if (arr.isArray()) {
                for (JsonNode k : arr) {
                    String tipe = k.path("type").asText();
                    String pertanyaan = k.path("text").asText();
                    String jawaban = defaultJawabanUntukTipe(tipe);
                    if ("text".equalsIgnoreCase(tipe)
                            && isKriteriaTindakanMedis(pertanyaan)
                            && !kodeTindakanPasien.isEmpty()) {
                        jawaban = kodeTindakanPasien;
                    }
                    modelKriteria.addRow(new Object[]{
                            k.path("linkId").asText(),
                            pertanyaan,
                            tipe,
                            jawaban
                    });
                }
            }

            // Bila tindakan pasien sudah tersedia, baris kedua menjadi pilihan
            // aktif dan kedua baris boolean tetap TIDAK.
            if (isCriteriaSynchronizationEnabled()
                    && modelKriteria.getRowCount() >= 3
                    && !tableValue(modelKriteria, 1, 3).isEmpty()) {
                applyCriteriaSynchronization(1);
            }
            updateReferralActionState();

            setStatus("Berhasil. Isi jawaban kriteria, lalu klik [Cari Faskes].", false);
            showModernToast(this,
                    "Kriteria rujukan berhasil diambil ("
                            + modelKriteria.getRowCount()
                            + " item).\nIsi jawaban pada kolom paling kanan.",
                    ToastMessage.SUCCESS, 0);

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // =================================================================
    //  ACTION 2: CARI FASKES TUJUAN
    //  POST /Sisrute/GetFaskesRujukan
    // =================================================================
    private void doCariFaskes() {
        if (!validateBeforeApi()) return;

        if (modelKriteria.getRowCount() == 0) {
            showModernToast(this,
                    "Klik [Cek Kriteria] dulu untuk mengambil daftar kriteria.",
                    ToastMessage.WARNING, 0);
            return;
        }

        // Build JSON kriteria item
        String kriteriaJson = buildKriteriaJsonItem();
        if (kriteriaJson == null) return;  // user error sudah ditampilkan

        // Ambil provinsi terpilih
        int idxProv = cbProvinsi.getSelectedIndex();
        String kdProv = PROVINSI[idxProv][0];
        String nmProv = PROVINSI[idxProv][1];
        String kdKab = "7371";
        String nmKab = "Kota Makassar";

        // Tanggal rencana
        String tglRencana = Valid.SetTgl(dtTglRencana.getSelectedItem() + "");

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Memanggil GetFaskesRujukan...", false);
        try {
            JsonNode resp = sisrute.getFaskesRujukan(
                    kodeFaskesSatuSehat, kdPenyakitRujuk, kdPoliRujuk,
                    tglRencana, kriteriaJson,
                    kdProv, nmProv, kdKab, nmKab,
                    encounterRef);

            rememberApiResponse("BPJS/SISRUTE - Get Faskes Rujukan", resp);

//            if (!SisruteService.isOk(resp)) {
//                JOptionPane.showMessageDialog(this,
//                        "Gagal: " + SisruteService.getMessage(resp),
//                        "Sisrute", JOptionPane.ERROR_MESSAGE);
//                setStatus("Gagal: " + SisruteService.getMessage(resp), true);
//                return;
//            }
            
            
            
             if (!SisruteService.isOk(resp)) {

                String msg =
                        safe(SisruteService.getMessage(resp));

                showModernToast(
                        this,
                        "Gagal Sisrute :<br>" + msg,
                        ToastMessage.ERROR,
                        0
                );

                setStatus(
                        "Gagal: " + msg,
                        true
                );

                return;
            }

            if (txtCariFaskesUi != null) {
                txtCariFaskesUi.setText("");
            }
            modelFaskes.setRowCount(0);
            selectedKdppkTujuan = "";
            selectedNmppkTujuan = "";

           JsonNode list = resp.path("response").path("list");
            int count = resp.path("response").path("count").asInt(0);

            if (list.isArray()) {
                for (JsonNode f : list) {
                    //antisipasi kalau ganto kode dari organization menjadi lainnya 
                    String kodeFaskesSatuSehat = f.path("kodeFaskesSatuSehat").asText();

                    if (kodeFaskesSatuSehat.contains("/")) {
                        kodeFaskesSatuSehat =
                            kodeFaskesSatuSehat.substring(
                                kodeFaskesSatuSehat.lastIndexOf("/") + 1
                            );
                    }

                    modelFaskes.addRow(new Object[]{                       
                        Boolean.FALSE,
                        kodeFaskesSatuSehat,
                        f.path("kdppk").asText(),
                        f.path("nmppk").asText(),
                        f.path("kelas").asText(),
                        f.path("strataSatuSehat").asText(),                        
                        f.path("nmkc").asText(),
                        f.path("distance").asText(),
                        f.path("kapasitas").asText(),
                        f.path("persentase").asText()
                    });
                }
            }

            setStatus("Ditemukan " + count
                    + " faskes. Pilih salah satu baris.", false);
            if (modelFaskes.getRowCount() > 0 && txtCariFaskesUi != null) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    txtCariFaskesUi.requestFocusInWindow();
                    txtCariFaskesUi.setCaretPosition(
                            txtCariFaskesUi.getDocument().getLength());
                    txtCariFaskesUi.repaint();
                });
            }

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Build kriteriaRujukan.item JSON dari isi tabel. */
    private String buildKriteriaJsonItem() {
        if (!commitCriteriaCellEditing()
                || !validateCriteriaSelectionForRequest()) return null;

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            String linkId = String.valueOf(modelKriteria.getValueAt(i, 0));
            String text = String.valueOf(modelKriteria.getValueAt(i, 1));
            String type = String.valueOf(modelKriteria.getValueAt(i, 2));
            String jwb = String.valueOf(modelKriteria.getValueAt(i, 3)).trim();

            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"linkId\":\"").append(linkId).append("\",")
              .append("\"text\":\"").append(escape(text)).append("\",")
              .append("\"answer\":[{");

            if ("boolean".equalsIgnoreCase(type)) {
                String bool = normalisasiBooleanKriteria(jwb);
                if (bool == null) {
                    showModernToast(this,
                            "Baris " + (i + 1)
                                    + ": jawaban tipe boolean harus dipilih YA atau TIDAK.",
                            ToastMessage.WARNING, 0);
                    return null;
                }
                sb.append("\"valueBoolean\":").append(bool);
            } else {
                sb.append("\"valueString\":\"").append(escape(jwb)).append("\"");
            }

            sb.append("}]}");
        }
        sb.append("]");
        return sb.toString();
    }

    // =================================================================
    //  ACTION 3: KIRIM RUJUKAN
    //  POST /Sisrute/postKunjungan
    // =================================================================
    private void doKirimRujukan() {
        if (!safe(tNoRujukanBpjs.getText()).isEmpty()) {
            showModernToast(this,
                    "Rujukan aktif sudah terbit. Hapus rujukan tersebut terlebih "
                            + "dahulu sebelum mengirim ke faskes lain.",
                    ToastMessage.WARNING, 0);
            return;
        }

        if (!validateBeforeApi()) return;
        if (!commitCriteriaCellEditing()) return;

        if (selectedKdppkTujuan.isEmpty()) {
            showModernToast(this,
                    "Pilih dulu faskes tujuan dengan mencentang salah satu baris.",
                    ToastMessage.WARNING, 0);
            return;
        }

        if (modelKriteria.getRowCount() == 0) {
            showModernToast(this,
                    "Klik [Cek Kriteria] dulu, lalu lengkapi jawabannya.",
                    ToastMessage.WARNING, 0);
            return;
        }

        String kriteriaJson = buildKriteriaJsonItem();
        if (kriteriaJson == null) return;

        if (!validateLocalReferralStorage()) return;

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Kirim rujukan untuk pasien " + namaPasien + "\nke " + selectedNmppkTujuan + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        SisruteService.InsertRujukanRequest req = new SisruteService.InsertRujukanRequest();
        req.noSep = noSep;
        req.tglRujukan = Valid.SetTgl(dtTglRujukan.getSelectedItem() + "");
        req.tglRencanaKunjungan = Valid.SetTgl(dtTglRencana.getSelectedItem() + "");
        req.ppkDirujuk = selectedKdppkTujuan;  // pakai kode dari hasil GetFaskesRujukan
        req.jnsPelayanan = cbJnsPelayanan.getSelectedItem().toString().substring(0, 1);
        req.catatan = taCatatan.getText();
        req.diagRujukan = kdPenyakitRujuk;
        req.tipeRujukan = cbTipeRujukan.getSelectedItem().toString().substring(0, 1);
        req.poliRujukan = kdPoliRujuk;
        req.user = user;

        req.kodeFaskesSatuSehat = kodeFaskesSatuSehat;
        req.idPasienSatuSehat = idPasienSatuSehat;
        req.kdppkSatuSehatTujuanRujukan = selectedKdppkSatuSehatTujuan;
        req.kdDokterSatuSehat = kdDokterSatuSehat;
        req.encounterReference = encounterRef;
        req.patientInstruction = "Rujukan ke " + selectedNmppkTujuan;
        req.kriteriaJsonItem = kriteriaJson;
        req.keteranganRujukan = taKeterangan.getText().isEmpty()
                ? "Rujukan ke " + selectedNmppkTujuan
                : taKeterangan.getText();

        int idxProv = cbProvinsi.getSelectedIndex();
        req.kodePropinsi = PROVINSI[idxProv][0];
        req.namaPropinsi = PROVINSI[idxProv][1];
        req.kodeKabupaten = "";
        req.namaKabupaten = "";

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Mengirim rujukan...", false);
        try {
            JsonNode resp = sisrute.insertRujukan(req);

            rememberApiResponse("BPJS/SISRUTE - Kirim Rujukan", resp);

            if (!SisruteService.isOk(resp)) {
                showModernToast(this,
                        "Gagal mengirim rujukan:\n"
                                + safe(SisruteService.getMessage(resp)),
                        ToastMessage.ERROR, 0);
                setStatus("Gagal: " + SisruteService.getMessage(resp), true);
                return;
            }

            // Ambil noRujukan dari response
            JsonNode rujuk = resp.path("response").path("rujukan");
            String noRujukan = rujuk.path("noRujukan").asText();
            String noRujukanSatuSehat = rujuk.path("noRujukanSatuSehat").asText();
            String serviceRequestId = rujuk.path("serviceRequestId").asText();

            if (noRujukan == null || noRujukan.isEmpty()) {
                noRujukan = "RJK" + System.currentTimeMillis();  // fallback
            }
            tNoRujukanBpjs.setText(noRujukan.trim());
            tNoRujukanSatuSehat.setText(noRujukanSatuSehat.trim());
            noRujukanBPJS = noRujukan.trim();
            noRujukanSatusehat = noRujukanSatuSehat.trim();
            activeServiceRequestId = safe(serviceRequestId);
            referralPersisted = false;
            updateReferralActionState();

            // Snapshot lengkap adalah penyimpanan utama untuk memulihkan form.
            // Tiga tabel bridging lama tetap diisi untuk kompatibilitas modul lain.
            boolean tersimpanLokal = saveToDb(
                    req, noRujukan, noRujukanSatuSehat, serviceRequestId);
            if (!tersimpanLokal) {
                showModernToast(this,
                        "Rujukan sudah diterbitkan oleh BPJS/SATUSEHAT, tetapi "
                                + "data lokal gagal disimpan. Jangan tutup form; "
                                + "nomor rujukan masih dapat disalin atau dihapus.\n"
                                + "Detail: " + safe(lastLocalSaveError),
                        ToastMessage.ERROR, 0);
                setStatus("Rujukan terbit, tetapi penyimpanan lokal gagal. No Rujukan: "
                        + noRujukan, true);
                return;
            }

            referralPersisted = true;
            updateReferralActionState();
            showModernToast(this,
                    "Rujukan berhasil dikirim.\n"
                            + "No Rujukan: " + noRujukan + "\n"
                            + "No Rujukan SatuSehat: " + noRujukanSatuSehat + "\n"
                            + "Service ID: " + serviceRequestId,
                    ToastMessage.SUCCESS, 0);
            setStatus("Berhasil. No Rujukan: " + noRujukan, false);

            // Preview hanya dibuka sesudah API sukses dan seluruh transaksi DB
            // selesai. invokeLater memastikan cursor WAIT sudah dipulihkan dulu.
            javax.swing.SwingUtilities.invokeLater(
                    () -> showSepReferralReportPreview());

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Menyiapkan tabel milik fitur ini sebelum API menerbitkan rujukan. V11
     * hanya memeriksa tabel dan langsung menghentikan proses ketika tabel
     * snapshot belum pernah dibuat. V12 membuat tiga tabel khusus fitur ini
     * secara otomatis dan memperbarui key tabel kriteria tanpa menyentuh isi
     * tabel bridging BPJS bawaan Khanza.
     */
    private boolean validateLocalReferralStorage() {
        try {
            ensureLocalReferralStorage();
        } catch (Exception ex) {
            String detail = friendlyLocalDatabaseError(ex);
            showModernToast(this,
                    "Penyimpanan rujukan belum dapat disiapkan otomatis. "
                            + "Pastikan user database memiliki izin CREATE/ALTER.\n"
                            + "Detail: " + detail,
                    ToastMessage.ERROR, 0);
            setStatus("Penyimpanan lokal gagal disiapkan; API belum dipanggil.",
                    true);
            System.out.println("Gagal menyiapkan tabel rujukan SatuSehat: " + ex);
            return false;
        }

        java.util.LinkedHashSet<String> requiredTables =
                new java.util.LinkedHashSet<>();
        requiredTables.add(SNAPSHOT_TABLE);
        requiredTables.add("bridging_kriteria_rujukan_satusehat");
        requiredTables.add("bridging_rujukan_satusehat");
        requiredTables.add("bridging_rujukan_bpjs");

        String tableSql = "select table_name from information_schema.tables "
                + "where table_schema=database() and table_name in (?,?,?,?)";
        try (PreparedStatement statement = koneksi.prepareStatement(tableSql)) {
            statement.setString(1, SNAPSHOT_TABLE);
            statement.setString(2, "bridging_kriteria_rujukan_satusehat");
            statement.setString(3, "bridging_rujukan_satusehat");
            statement.setString(4, "bridging_rujukan_bpjs");
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String found = safe(result.getString(1));
                    java.util.Iterator<String> iterator =
                            requiredTables.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().equalsIgnoreCase(found)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showModernToast(this,
                    "Tidak dapat memeriksa tabel penyimpanan lokal: "
                            + safe(ex.getMessage()),
                    ToastMessage.ERROR, 0);
            setStatus("Pemeriksaan database gagal; API belum dipanggil.", true);
            return false;
        }

        if (!requiredTables.isEmpty()) {
            showModernToast(this,
                    "Tabel penyimpanan yang belum tersedia: "
                            + String.join(", ", requiredTables)
                            + ". Rujukan belum dikirim.",
                    ToastMessage.ERROR, 0);
            setStatus("Penyimpanan lokal belum lengkap; API belum dipanggil.",
                    true);
            return false;
        }

        return hasCompositeCriteriaKey();
    }

    private void ensureLocalReferralStorage() throws Exception {
        if (!databaseTableExists(SNAPSHOT_TABLE)) {
            executeStorageDdl("create table if not exists `"
                + SNAPSHOT_TABLE + "` ("
                + "`no_rawat` varchar(17) not null,"
                + "`no_sep` varchar(40) not null default '',"
                + "`no_rujukan_bpjs` varchar(50) not null,"
                + "`no_rujukan_satusehat` varchar(100) not null default '',"
                + "`service_request_id` varchar(100) not null default '',"
                + "`no_rkm_medis` varchar(20) not null default '',"
                + "`nama_pasien` varchar(100) not null default '',"
                + "`kode_faskes_satusehat` varchar(100) not null default '',"
                + "`id_pasien_satusehat` varchar(100) not null default '',"
                + "`kdppk_satusehat_tujuan` varchar(100) not null default '',"
                + "`kode_ppk_tujuan` varchar(50) not null default '',"
                + "`nama_faskes_tujuan` varchar(255) not null default '',"
                + "`kd_dokter_rs` varchar(20) not null default '',"
                + "`kd_dokter_satusehat` varchar(100) not null default '',"
                + "`encounter_reference` varchar(100) not null default '',"
                + "`patient_instruction` text not null,"
                + "`keterangan_rujukan` text not null,"
                + "`user` varchar(50) not null default '',"
                + "`form_json` longtext not null,"
                + "`kriteria_json` longtext not null,"
                + "`faskes_json` longtext not null,"
                + "`response_api_json` longtext not null,"
                + "`created_at` datetime not null,"
                + "`updated_at` datetime not null,"
                + "primary key (`no_rawat`),"
                + "unique key `uk_brsf_no_rujukan_bpjs` "
                + "(`no_rujukan_bpjs`),"
                + "key `idx_brsf_no_sep` (`no_sep`),"
                + "key `idx_brsf_no_rujukan_satusehat` "
                + "(`no_rujukan_satusehat`)"
                + ") engine=InnoDB default charset=utf8mb4");
        }

        if (!databaseTableExists("bridging_rujukan_satusehat")) {
            executeStorageDdl("create table if not exists "
                + "`bridging_rujukan_satusehat` ("
                + "`no_rujukan` varchar(50) not null,"
                + "`no_rujukan_satusehat` varchar(100) not null default '',"
                + "`service_request_id` varchar(100) not null default '',"
                + "`kode_faskes_satusehat` varchar(100) not null default '',"
                + "`id_pasien_satusehat` varchar(100) not null default '',"
                + "`kdppk_satusehat_tujuan` varchar(100) not null default '',"
                + "`nama_faskes_tujuan` varchar(255) not null default '',"
                + "`kd_dokter_satusehat` varchar(100) not null default '',"
                + "`encounter_reference` varchar(100) not null default '',"
                + "`patient_instruction` text not null,"
                + "`keterangan_rujukan` text not null,"
                + "`kode_propinsi` varchar(10) not null default '',"
                + "`nama_propinsi` varchar(100) not null default '',"
                + "`kode_kabupaten` varchar(10) not null default '',"
                + "`nama_kabupaten` varchar(100) not null default '',"
                + "`kode_poli_rujuk` varchar(20) not null default '',"
                + "`created_at` datetime not null default current_timestamp,"
                + "`update_at` datetime not null default current_timestamp "
                + "on update current_timestamp,"
                + "primary key (`no_rujukan`),"
                + "key `idx_brss_no_rujukan_satusehat` "
                + "(`no_rujukan_satusehat`),"
                + "key `idx_brss_service_request_id` (`service_request_id`)"
                + ") engine=InnoDB default charset=utf8mb4");
        }

        if (!databaseTableExists("bridging_kriteria_rujukan_satusehat")) {
            executeStorageDdl("create table if not exists "
                + "`bridging_kriteria_rujukan_satusehat` ("
                + "`no_rujukan` varchar(50) not null,"
                + "`link_id` varchar(50) not null,"
                + "`pertanyaan` varchar(500) not null default '',"
                + "`tipe` varchar(20) not null default '',"
                + "`jawaban_boolean` varchar(5) not null default '',"
                + "`jawaban_text` text not null,"
                + "primary key (`no_rujukan`,`link_id`),"
                + "key `idx_bkrss_link_id` (`link_id`)"
                + ") engine=InnoDB default charset=utf8mb4");
        }

        migrateCriteriaCompositeKey();
    }

    private boolean databaseTableExists(String tableName) throws Exception {
        String sql = "select count(*) from information_schema.tables "
                + "where table_schema=database() and table_name=?";
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    private void executeStorageDdl(String sql) throws Exception {
        try (java.sql.Statement statement = koneksi.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void migrateCriteriaCompositeKey() throws Exception {
        String indexSql = "select index_name,"
                + "group_concat(column_name order by seq_in_index separator ',') "
                + "from information_schema.statistics "
                + "where table_schema=database() "
                + "and table_name='bridging_kriteria_rujukan_satusehat' "
                + "and non_unique=0 group by index_name";
        String primaryColumns = "";
        java.util.ArrayList<String> obsoleteUniqueIndexes =
                new java.util.ArrayList<>();

        try (PreparedStatement statement = koneksi.prepareStatement(indexSql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String indexName = safe(result.getString(1));
                String columns = safe(result.getString(2));
                if ("PRIMARY".equalsIgnoreCase(indexName)) {
                    primaryColumns = columns;
                } else if ("no_rujukan".equalsIgnoreCase(columns)) {
                    obsoleteUniqueIndexes.add(indexName);
                }
            }
        }

        for (String indexName : obsoleteUniqueIndexes) {
            String quotedName = indexName.replace("`", "``");
            executeStorageDdl("alter table "
                    + "`bridging_kriteria_rujukan_satusehat` drop index `"
                    + quotedName + "`");
        }

        if (!"no_rujukan,link_id".equalsIgnoreCase(primaryColumns)) {
            String alterSql = "alter table "
                    + "`bridging_kriteria_rujukan_satusehat` ";
            if (!primaryColumns.isEmpty()) {
                alterSql += "drop primary key, ";
            }
            alterSql += "add primary key (`no_rujukan`,`link_id`)";
            executeStorageDdl(alterSql);
        }
    }

    private boolean hasCompositeCriteriaKey() {
        String indexSql = "select "
                + "group_concat(column_name order by seq_in_index separator ',') "
                + "from information_schema.statistics "
                + "where table_schema=database() "
                + "and table_name='bridging_kriteria_rujukan_satusehat' "
                + "and index_name='PRIMARY' group by index_name";
        try (PreparedStatement statement = koneksi.prepareStatement(indexSql);
                ResultSet result = statement.executeQuery()) {
            if (result.next() && "no_rujukan,link_id".equalsIgnoreCase(
                    safe(result.getString(1)))) {
                return true;
            }
        } catch (Exception ex) {
            showModernToast(this,
                    "Tidak dapat memeriksa key tabel kriteria: "
                            + safe(ex.getMessage()),
                    ToastMessage.ERROR, 0);
            setStatus("Pemeriksaan key database gagal; API belum dipanggil.", true);
            return false;
        }

        showModernToast(this,
                "Key tabel kriteria belum berhasil diubah menjadi "
                        + "no_rujukan + link_id. Rujukan belum dikirim.",
                ToastMessage.ERROR, 0);
        setStatus("Migrasi key kriteria belum berhasil; API belum dipanggil.",
                true);
        return false;
    }

    private boolean saveToDb(SisruteService.InsertRujukanRequest req,
                          String noRujukan,
                          String noRujukanSatuSehat,
                          String serviceRequestId) {
        boolean originalAutoCommit = true;
        java.sql.Savepoint savepoint = null;
        lastLocalSaveError = "";

        try {
            originalAutoCommit = koneksi.getAutoCommit();
            if (originalAutoCommit) {
                koneksi.setAutoCommit(false);
            } else {
                savepoint = koneksi.setSavepoint(
                        "save_rujukan_satusehat_form");
            }

            // Keempat tabel memakai koneksi dan transaksi yang sama. Tidak ada
            // lagi kondisi snapshot sukses tetapi tabel kriteria diam-diam gagal.
            saveFormSnapshot(req, noRujukan, noRujukanSatuSehat,
                    serviceRequestId);
            upsertLegacyBpjsReferral(req, noRujukan);
            upsertLegacySatuSehatReferral(req, noRujukan,
                    noRujukanSatuSehat, serviceRequestId);
            replaceLegacyReferralCriteria(noRujukan);

            if (originalAutoCommit) koneksi.commit();
            return true;
        } catch (Exception ex) {
            try {
                if (originalAutoCommit) {
                    koneksi.rollback();
                } else if (savepoint != null) {
                    koneksi.rollback(savepoint);
                }
            } catch (Exception rollbackException) {
                System.out.println("Rollback penyimpanan rujukan gagal: "
                        + rollbackException);
            }

            lastLocalSaveError = friendlyLocalDatabaseError(ex);
            System.out.println("Gagal simpan seluruh data rujukan: " + ex);
            return false;
        } finally {
            if (originalAutoCommit) {
                try {
                    koneksi.setAutoCommit(true);
                } catch (Exception ex) {
                    System.out.println("Gagal mengembalikan auto-commit: " + ex);
                }
            }
        }
    }

    private void upsertLegacyBpjsReferral(
            SisruteService.InsertRujukanRequest req,
            String noRujukan) throws Exception {
        String sql = "insert into bridging_rujukan_bpjs "
                + "(no_sep,tglRujukan,tglRencanaKunjungan,ppkDirujuk,"
                + "nm_ppkDirujuk,jnsPelayanan,catatan,diagRujukan,"
                + "nama_diagRujukan,tipeRujukan,poliRujukan,"
                + "nama_poliRujukan,no_rujukan,`user`) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update no_sep=values(no_sep),"
                + "tglRujukan=values(tglRujukan),"
                + "tglRencanaKunjungan=values(tglRencanaKunjungan),"
                + "ppkDirujuk=values(ppkDirujuk),"
                + "nm_ppkDirujuk=values(nm_ppkDirujuk),"
                + "jnsPelayanan=values(jnsPelayanan),"
                + "catatan=values(catatan),diagRujukan=values(diagRujukan),"
                + "nama_diagRujukan=values(nama_diagRujukan),"
                + "tipeRujukan=values(tipeRujukan),"
                + "poliRujukan=values(poliRujukan),"
                + "nama_poliRujukan=values(nama_poliRujukan),"
                + "`user`=values(`user`)";
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, req.noSep);
            statement.setString(index++, req.tglRujukan);
            statement.setString(index++, req.tglRencanaKunjungan);
            statement.setString(index++, req.ppkDirujuk);
            statement.setString(index++, selectedNmppkTujuan);
            statement.setString(index++, req.jnsPelayanan);
            statement.setString(index++, req.catatan);
            statement.setString(index++, req.diagRujukan);
            statement.setString(index++, nmPenyakitRujuk);
            statement.setString(index++, selectedComboText(cbTipeRujukan));
            statement.setString(index++, req.poliRujukan);
            statement.setString(index++, tNmPoliRujuk.getText().trim());
            statement.setString(index++, noRujukan);
            statement.setString(index, user);
            statement.executeUpdate();
        }
    }

    private void upsertLegacySatuSehatReferral(
            SisruteService.InsertRujukanRequest req,
            String noRujukan, String noRujukanSatuSehat,
            String serviceRequestId) throws Exception {
        String sql = "insert into bridging_rujukan_satusehat "
                + "(no_rujukan,no_rujukan_satusehat,service_request_id,"
                + "kode_faskes_satusehat,id_pasien_satusehat,"
                + "kdppk_satusehat_tujuan,nama_faskes_tujuan,"
                + "kd_dokter_satusehat,encounter_reference,"
                + "patient_instruction,keterangan_rujukan,kode_propinsi,"
                + "nama_propinsi,kode_kabupaten,nama_kabupaten,"
                + "kode_poli_rujuk) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update "
                + "no_rujukan_satusehat=values(no_rujukan_satusehat),"
                + "service_request_id=values(service_request_id),"
                + "kode_faskes_satusehat=values(kode_faskes_satusehat),"
                + "id_pasien_satusehat=values(id_pasien_satusehat),"
                + "kdppk_satusehat_tujuan=values(kdppk_satusehat_tujuan),"
                + "nama_faskes_tujuan=values(nama_faskes_tujuan),"
                + "kd_dokter_satusehat=values(kd_dokter_satusehat),"
                + "encounter_reference=values(encounter_reference),"
                + "patient_instruction=values(patient_instruction),"
                + "keterangan_rujukan=values(keterangan_rujukan),"
                + "kode_propinsi=values(kode_propinsi),"
                + "nama_propinsi=values(nama_propinsi),"
                + "kode_kabupaten=values(kode_kabupaten),"
                + "nama_kabupaten=values(nama_kabupaten),"
                + "kode_poli_rujuk=values(kode_poli_rujuk)";
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, noRujukan);
            statement.setString(index++, noRujukanSatuSehat);
            statement.setString(index++, serviceRequestId);
            statement.setString(index++, req.kodeFaskesSatuSehat);
            statement.setString(index++, req.idPasienSatuSehat);
            statement.setString(index++, req.kdppkSatuSehatTujuanRujukan);
            statement.setString(index++, selectedNmppkTujuan);
            statement.setString(index++, req.kdDokterSatuSehat);
            statement.setString(index++, req.encounterReference);
            statement.setString(index++, req.patientInstruction);
            statement.setString(index++, req.keteranganRujukan);
            statement.setString(index++, req.kodePropinsi);
            statement.setString(index++, req.namaPropinsi);
            statement.setString(index++, req.kodeKabupaten);
            statement.setString(index++, req.namaKabupaten);
            statement.setString(index, kdPoliRujuk);
            statement.executeUpdate();
        }
    }

    private void replaceLegacyReferralCriteria(String noRujukan)
            throws Exception {
        java.util.LinkedHashMap<String, String[]> uniqueCriteria =
                new java.util.LinkedHashMap<>();
        for (int row = 0; row < modelKriteria.getRowCount(); row++) {
            String linkId = tableValue(modelKriteria, row, 0);
            String question = tableValue(modelKriteria, row, 1);
            String type = tableValue(modelKriteria, row, 2);
            String answer = tableValue(modelKriteria, row, 3);
            if (linkId.isEmpty()) {
                throw new java.sql.SQLException(
                        "Link ID kriteria pada baris " + (row + 1) + " kosong.");
            }

            String booleanValue = "";
            String textValue = "";
            if ("boolean".equalsIgnoreCase(type)) {
                String normalized = normalisasiBooleanKriteria(answer);
                booleanValue = "true".equalsIgnoreCase(normalized) ? "1" : "0";
            } else {
                textValue = answer;
            }
            uniqueCriteria.put(linkId, new String[]{
                question, type, booleanValue, textValue
            });
        }

        try (PreparedStatement delete = koneksi.prepareStatement(
                "delete from bridging_kriteria_rujukan_satusehat "
                        + "where no_rujukan=?")) {
            delete.setString(1, noRujukan);
            delete.executeUpdate();
        }

        String sql = "insert into bridging_kriteria_rujukan_satusehat "
                + "(no_rujukan,link_id,pertanyaan,tipe,"
                + "jawaban_boolean,jawaban_text) values (?,?,?,?,?,?)";
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            for (java.util.Map.Entry<String, String[]> entry
                    : uniqueCriteria.entrySet()) {
                String[] values = entry.getValue();
                statement.setString(1, noRujukan);
                statement.setString(2, entry.getKey());
                statement.setString(3, values[0]);
                statement.setString(4, values[1]);
                statement.setString(5, values[2]);
                statement.setString(6, values[3]);
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result == java.sql.Statement.EXECUTE_FAILED) {
                    throw new java.sql.SQLException(
                            "Salah satu baris kriteria gagal disimpan.");
                }
            }
        }
    }

    private String friendlyLocalDatabaseError(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof java.sql.SQLException) {
                java.sql.SQLException sqlException =
                        (java.sql.SQLException) current;
                String message = safe(sqlException.getMessage());
                if (sqlException.getErrorCode() == 1062
                        || message.toLowerCase(java.util.Locale.ROOT)
                                .contains("duplicate")) {
                    return "Key data masih bertabrakan. Pastikan SQL migrasi key "
                            + "kriteria sudah dijalankan.";
                }
                if (!message.isEmpty()) {
                    return message.length() > 240
                            ? message.substring(0, 240) + "..." : message;
                }
            }
            current = current.getCause();
        }
        String message = safe(exception == null ? "" : exception.getMessage());
        return message.isEmpty() ? "Kesalahan database tidak diketahui." : message;
    }

    // =================================================================
    //  SNAPSHOT FORM RUJUKAN
    //  Satu row per no_rawat, berisi seluruh state yang perlu dipulihkan.
    // =================================================================
    private void saveFormSnapshot(SisruteService.InsertRujukanRequest req,
            String noRujukan, String noRujukanSatuSehat,
            String serviceRequestId) throws Exception {
        String sql = "INSERT INTO " + SNAPSHOT_TABLE + " ("
                + "no_rawat,no_sep,no_rujukan_bpjs,no_rujukan_satusehat,"
                + "service_request_id,no_rkm_medis,nama_pasien,"
                + "kode_faskes_satusehat,id_pasien_satusehat,"
                + "kdppk_satusehat_tujuan,kode_ppk_tujuan,nama_faskes_tujuan,"
                + "kd_dokter_rs,kd_dokter_satusehat,encounter_reference,"
                + "patient_instruction,keterangan_rujukan,`user`,form_json,"
                + "kriteria_json,faskes_json,response_api_json,created_at,updated_at"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE "
                + "no_sep=VALUES(no_sep),no_rujukan_bpjs=VALUES(no_rujukan_bpjs),"
                + "no_rujukan_satusehat=VALUES(no_rujukan_satusehat),"
                + "service_request_id=VALUES(service_request_id),"
                + "no_rkm_medis=VALUES(no_rkm_medis),nama_pasien=VALUES(nama_pasien),"
                + "kode_faskes_satusehat=VALUES(kode_faskes_satusehat),"
                + "id_pasien_satusehat=VALUES(id_pasien_satusehat),"
                + "kdppk_satusehat_tujuan=VALUES(kdppk_satusehat_tujuan),"
                + "kode_ppk_tujuan=VALUES(kode_ppk_tujuan),"
                + "nama_faskes_tujuan=VALUES(nama_faskes_tujuan),"
                + "kd_dokter_rs=VALUES(kd_dokter_rs),"
                + "kd_dokter_satusehat=VALUES(kd_dokter_satusehat),"
                + "encounter_reference=VALUES(encounter_reference),"
                + "patient_instruction=VALUES(patient_instruction),"
                + "keterangan_rujukan=VALUES(keterangan_rujukan),"
                + "`user`=VALUES(`user`),form_json=VALUES(form_json),"
                + "kriteria_json=VALUES(kriteria_json),"
                + "faskes_json=VALUES(faskes_json),"
                + "response_api_json=VALUES(response_api_json),"
                + "updated_at=VALUES(updated_at)";

        String formJson = buildFormSnapshotJson(
                req, noRujukan, noRujukanSatuSehat, serviceRequestId);
        String kriteriaJson = buildKriteriaSnapshotJson();
        String faskesJson = buildFaskesSnapshotJson();
        String responseJson = buildApiResponseHistoryJson();
        java.sql.Timestamp now = new java.sql.Timestamp(
                System.currentTimeMillis());

        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, noRawat);
            statement.setString(index++, req.noSep);
            statement.setString(index++, noRujukan);
            statement.setString(index++, noRujukanSatuSehat);
            statement.setString(index++, serviceRequestId);
            statement.setString(index++, noRm);
            statement.setString(index++, namaPasien);
            statement.setString(index++, req.kodeFaskesSatuSehat);
            statement.setString(index++, req.idPasienSatuSehat);
            statement.setString(index++, req.kdppkSatuSehatTujuanRujukan);
            statement.setString(index++, selectedKdppkTujuan);
            statement.setString(index++, selectedNmppkTujuan);
            statement.setString(index++, kdDokterRs);
            statement.setString(index++, req.kdDokterSatuSehat);
            statement.setString(index++, req.encounterReference);
            statement.setString(index++, req.patientInstruction);
            statement.setString(index++, req.keteranganRujukan);
            statement.setString(index++, req.user);
            statement.setString(index++, formJson);
            statement.setString(index++, kriteriaJson);
            statement.setString(index++, faskesJson);
            statement.setString(index++, responseJson);
            statement.setTimestamp(index++, now);
            statement.setTimestamp(index, now);
            statement.executeUpdate();
        }
    }

    private String buildFormSnapshotJson(
            SisruteService.InsertRujukanRequest req,
            String noRujukan, String noRujukanSatuSehat,
            String serviceRequestId) throws Exception {
        com.fasterxml.jackson.databind.node.ObjectNode root =
                mapper.createObjectNode();
        root.put("schemaVersion", 1);
        root.put("noSep", noSep);
        root.put("noRawat", noRawat);
        root.put("noRm", noRm);
        root.put("namaPasien", namaPasien);
        root.put("noRujukanBpjs", noRujukan);
        root.put("noRujukanSatuSehat", noRujukanSatuSehat);
        root.put("serviceRequestId", serviceRequestId);
        root.put("encounterReference", encounterRef);
        root.put("idPasienSatuSehat", idPasienSatuSehat);
        root.put("kdDokterRs", kdDokterRs);
        root.put("kdDokterSatuSehat", kdDokterSatuSehat);
        root.put("kodeFaskesSatuSehat", kodeFaskesSatuSehat);
        root.put("kodeSpesialis", kodeSpesialis);
        root.put("kdPenyakit", kdPenyakit);
        root.put("nmPenyakit", nmPenyakit);
        root.put("kdPoliBpjs", kdPoliBpjs);
        root.put("nmPoli", nmPoli);
        root.put("kdPenyakitRujuk", kdPenyakitRujuk);
        root.put("nmPenyakitRujuk", nmPenyakitRujuk);
        root.put("kdPoliRujuk", kdPoliRujuk);
        root.put("nmPoliRujuk", nmPoliRujuk);
        root.put("jenisPelayanan", selectedComboText(cbJnsPelayanan));
        root.put("tipeRujukan", selectedComboText(cbTipeRujukan));
        root.put("tglRujukan", req.tglRujukan);
        root.put("tglRencana", req.tglRencanaKunjungan);
        root.put("kodeProvinsi", req.kodePropinsi);
        root.put("namaProvinsi", req.namaPropinsi);
        root.put("catatan", taCatatan.getText());
        root.put("keterangan", taKeterangan.getText());
        root.put("patientInstruction", req.patientInstruction);
        root.put("user", user);

        com.fasterxml.jackson.databind.node.ObjectNode faskes =
                root.putObject("faskesTerpilih");
        int selectedRow = getSelectedFaskesModelRow();
        faskes.put("kodeSatuSehat", selectedKdppkSatuSehatTujuan);
        faskes.put("kodePpk", selectedKdppkTujuan);
        faskes.put("namaRs", selectedNmppkTujuan);
        faskes.put("kelas", tableValue(modelFaskes, selectedRow, 4));
        faskes.put("strata", tableValue(modelFaskes, selectedRow, 5));
        faskes.put("kabKota", tableValue(modelFaskes, selectedRow, 6));
        faskes.put("jarak", tableValue(modelFaskes, selectedRow, 7));
        faskes.put("kapasitas", tableValue(modelFaskes, selectedRow, 8));
        faskes.put("persentaseBeban", tableValue(modelFaskes, selectedRow, 9));

        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(root);
    }

    private String buildKriteriaSnapshotJson() throws Exception {
        com.fasterxml.jackson.databind.node.ArrayNode rows =
                mapper.createArrayNode();
        for (int row = 0; row < modelKriteria.getRowCount(); row++) {
            com.fasterxml.jackson.databind.node.ObjectNode item = rows.addObject();
            item.put("linkId", tableValue(modelKriteria, row, 0));
            item.put("pertanyaan", tableValue(modelKriteria, row, 1));
            item.put("tipe", tableValue(modelKriteria, row, 2));
            item.put("jawaban", tableValue(modelKriteria, row, 3));
        }
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(rows);
    }

    private String buildFaskesSnapshotJson() throws Exception {
        com.fasterxml.jackson.databind.node.ArrayNode rows =
                mapper.createArrayNode();
        for (int row = 0; row < modelFaskes.getRowCount(); row++) {
            com.fasterxml.jackson.databind.node.ObjectNode item = rows.addObject();
            item.put("dipilih", Boolean.TRUE.equals(
                    modelFaskes.getValueAt(row, 0)));
            item.put("kodeSatuSehat", tableValue(modelFaskes, row, 1));
            item.put("kodePpk", tableValue(modelFaskes, row, 2));
            item.put("namaRs", tableValue(modelFaskes, row, 3));
            item.put("kelas", tableValue(modelFaskes, row, 4));
            item.put("strata", tableValue(modelFaskes, row, 5));
            item.put("kabKota", tableValue(modelFaskes, row, 6));
            item.put("jarak", tableValue(modelFaskes, row, 7));
            item.put("kapasitas", tableValue(modelFaskes, row, 8));
            item.put("persentaseBeban", tableValue(modelFaskes, row, 9));
        }
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(rows);
    }

    private int getSelectedFaskesModelRow() {
        for (int row = 0; row < modelFaskes.getRowCount(); row++) {
            if (Boolean.TRUE.equals(modelFaskes.getValueAt(row, 0))) {
                return row;
            }
        }
        return -1;
    }

    private String tableValue(DefaultTableModel model, int row, int column) {
        if (model == null || row < 0 || row >= model.getRowCount()
                || column < 0 || column >= model.getColumnCount()) {
            return "";
        }
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private String selectedComboText(javax.swing.JComboBox combo) {
        Object value = combo == null ? null : combo.getSelectedItem();
        return value == null ? "" : value.toString().trim();
    }

    private boolean loadSavedReferralSnapshot() {
        referralPersisted = false;
        if (safe(noRawat).isEmpty()) {
            updateReferralActionState();
            return false;
        }

        String sql = "SELECT no_sep,no_rujukan_bpjs,no_rujukan_satusehat,"
                + "service_request_id,no_rkm_medis,nama_pasien,"
                + "kode_faskes_satusehat,id_pasien_satusehat,"
                + "kdppk_satusehat_tujuan,kode_ppk_tujuan,nama_faskes_tujuan,"
                + "kd_dokter_rs,kd_dokter_satusehat,encounter_reference,"
                + "patient_instruction,keterangan_rujukan,form_json,"
                + "kriteria_json,faskes_json,response_api_json "
                + "FROM " + SNAPSHOT_TABLE + " WHERE no_rawat=? LIMIT 1";

        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            statement.setString(1, noRawat);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    tNoRujukanBpjs.setText("");
                    tNoRujukanSatuSehat.setText("");
                    noRujukanBPJS = "";
                    noRujukanSatusehat = "";
                    activeServiceRequestId = "";
                    updateReferralActionState();
                    return false;
                }

                restoringReferralSnapshot = true;
                noSep = safe(result.getString("no_sep"));
                noRm = safe(result.getString("no_rkm_medis"));
                namaPasien = safe(result.getString("nama_pasien"));
                noRujukanBPJS = safe(result.getString("no_rujukan_bpjs"));
                noRujukanSatusehat = safe(
                        result.getString("no_rujukan_satusehat"));
                activeServiceRequestId = safe(
                        result.getString("service_request_id"));
                kodeFaskesSatuSehat = safe(
                        result.getString("kode_faskes_satusehat"));
                idPasienSatuSehat = safe(
                        result.getString("id_pasien_satusehat"));
                selectedKdppkSatuSehatTujuan = safe(
                        result.getString("kdppk_satusehat_tujuan"));
                selectedKdppkTujuan = safe(
                        result.getString("kode_ppk_tujuan"));
                selectedNmppkTujuan = safe(
                        result.getString("nama_faskes_tujuan"));
                kdDokterRs = safe(result.getString("kd_dokter_rs"));
                kdDokterSatuSehat = safe(
                        result.getString("kd_dokter_satusehat"));
                encounterRef = safe(result.getString("encounter_reference"));

                String formJson = result.getString("form_json");
                String criteriaJson = result.getString("kriteria_json");
                String facilitiesJson = result.getString("faskes_json");
                String responsesJson = result.getString("response_api_json");
                try {
                    restoreFormSnapshotJson(formJson);
                } catch (Exception ex) {
                    System.out.println("Gagal pulihkan detail form snapshot: " + ex);
                }
                try {
                    restoreKriteriaSnapshotJson(criteriaJson);
                } catch (Exception ex) {
                    System.out.println("Gagal pulihkan kriteria snapshot: " + ex);
                }
                try {
                    restoreFaskesSnapshotJson(facilitiesJson);
                } catch (Exception ex) {
                    System.out.println("Gagal pulihkan faskes snapshot: " + ex);
                }
                try {
                    restoreApiResponseHistoryJson(responsesJson);
                } catch (Exception ex) {
                    System.out.println("Gagal pulihkan riwayat API snapshot: " + ex);
                }

                tNoSep.setText(noSep);
                tNoRawat.setText(noRawat);
                tPasien.setText(noRm + " - " + namaPasien);
                tNoRujukanBpjs.setText(noRujukanBPJS);
                tNoRujukanSatuSehat.setText(noRujukanSatusehat);
                tEncounter.setText(encounterRef);
                tIdPasienIhs.setText(idPasienSatuSehat);
                tIdDokterIhs.setText(kdDokterSatuSehat);
                referralPersisted = !noRujukanBPJS.isEmpty();
            }

            setStatus("Rujukan aktif dimuat. No Rujukan: "
                    + noRujukanBPJS, false);
            showModernToast(this,
                    "Data rujukan tersimpan berhasil dipulihkan untuk No. Rawat "
                            + noRawat + ".",
                    TOAST_INFO, 0);
            return true;
        } catch (Exception ex) {
            System.out.println("Gagal memuat snapshot rujukan: " + ex);
            if (!snapshotTableWarningShown) {
                snapshotTableWarningShown = true;
                String message = isMissingSnapshotTable(ex)
                        ? "Tabel penyimpanan rujukan belum tersedia. Jalankan file "
                                + "SQL migrasi bridging_rujukan_satusehat_form."
                        : "Gagal memuat data rujukan tersimpan: "
                                + safe(ex.getMessage());
                showModernToast(this, message, ToastMessage.WARNING, 0);
            }
            return false;
        } finally {
            restoringReferralSnapshot = false;
            updateReferralActionState();
            javax.swing.SwingUtilities.invokeLater(() -> {
                adjustKriteriaQuestionColumnWidth();
                adjustKriteriaAnswerColumnWidth();
                adjustFaskesColumnWidths();
                updateDynamicTableHeights();
            });
        }
    }

    private void restoreFormSnapshotJson(String json) throws Exception {
        if (safe(json).isEmpty()) return;
        JsonNode root = mapper.readTree(json);

        noSep = snapshotText(root, "noSep", noSep);
        noRawat = snapshotText(root, "noRawat", noRawat);
        noRm = snapshotText(root, "noRm", noRm);
        namaPasien = snapshotText(root, "namaPasien", namaPasien);
        encounterRef = snapshotText(
                root, "encounterReference", encounterRef);
        idPasienSatuSehat = snapshotText(
                root, "idPasienSatuSehat", idPasienSatuSehat);
        kdDokterRs = snapshotText(root, "kdDokterRs", kdDokterRs);
        kdDokterSatuSehat = snapshotText(
                root, "kdDokterSatuSehat", kdDokterSatuSehat);
        kodeFaskesSatuSehat = snapshotText(
                root, "kodeFaskesSatuSehat", kodeFaskesSatuSehat);
        kodeSpesialis = snapshotText(root, "kodeSpesialis", kodeSpesialis);
        kdPenyakit = snapshotText(root, "kdPenyakit", kdPenyakit);
        nmPenyakit = snapshotText(root, "nmPenyakit", nmPenyakit);
        kdPoliBpjs = snapshotText(root, "kdPoliBpjs", kdPoliBpjs);
        nmPoli = snapshotText(root, "nmPoli", nmPoli);
        kdPenyakitRujuk = snapshotText(
                root, "kdPenyakitRujuk", kdPenyakitRujuk);
        nmPenyakitRujuk = snapshotText(
                root, "nmPenyakitRujuk", nmPenyakitRujuk);
        kdPoliRujuk = snapshotText(root, "kdPoliRujuk", kdPoliRujuk);
        nmPoliRujuk = snapshotText(root, "nmPoliRujuk", nmPoliRujuk);

        tKdDiagnosaRujuk.setText(kdPenyakitRujuk);
        tNmDiagnosaRujuk.setText(nmPenyakitRujuk);
        tKdPoliRujuk.setText(kdPoliRujuk);
        tNmPoliRujuk.setText(nmPoliRujuk);
        taCatatan.setText(snapshotText(root, "catatan", ""));
        taKeterangan.setText(snapshotText(root, "keterangan", ""));

        selectComboValue(cbJnsPelayanan,
                snapshotText(root, "jenisPelayanan", ""));
        selectComboValue(cbTipeRujukan,
                snapshotText(root, "tipeRujukan", ""));
        selectProvinceByCode(snapshotText(root, "kodeProvinsi", ""));
        restoreSnapshotDate(dtTglRujukan,
                snapshotText(root, "tglRujukan", ""));
        restoreSnapshotDate(dtTglRencana,
                snapshotText(root, "tglRencana", ""));

        JsonNode selected = root.path("faskesTerpilih");
        if (selected.isObject()) {
            selectedKdppkSatuSehatTujuan = snapshotText(
                    selected, "kodeSatuSehat", selectedKdppkSatuSehatTujuan);
            selectedKdppkTujuan = snapshotText(
                    selected, "kodePpk", selectedKdppkTujuan);
            selectedNmppkTujuan = snapshotText(
                    selected, "namaRs", selectedNmppkTujuan);
        }
    }

    private void restoreKriteriaSnapshotJson(String json) throws Exception {
        modelKriteria.setRowCount(0);
        if (safe(json).isEmpty()) return;
        JsonNode rows = mapper.readTree(json);
        if (!rows.isArray()) return;
        for (JsonNode item : rows) {
            modelKriteria.addRow(new Object[]{
                snapshotText(item, "linkId", ""),
                snapshotText(item, "pertanyaan", ""),
                snapshotText(item, "tipe", ""),
                snapshotText(item, "jawaban", "")
            });
        }
    }

    private void restoreFaskesSnapshotJson(String json) throws Exception {
        if (txtCariFaskesUi != null) txtCariFaskesUi.setText("");
        modelFaskes.setRowCount(0);
        if (safe(json).isEmpty()) return;
        JsonNode rows = mapper.readTree(json);
        if (!rows.isArray()) return;

        int selectedRow = -1;
        int rowIndex = 0;
        for (JsonNode item : rows) {
            boolean selected = item.path("dipilih").asBoolean(false);
            modelFaskes.addRow(new Object[]{
                selected,
                snapshotText(item, "kodeSatuSehat", ""),
                snapshotText(item, "kodePpk", ""),
                snapshotText(item, "namaRs", ""),
                snapshotText(item, "kelas", ""),
                snapshotText(item, "strata", ""),
                snapshotText(item, "kabKota", ""),
                snapshotText(item, "jarak", ""),
                snapshotText(item, "kapasitas", ""),
                snapshotText(item, "persentaseBeban", "")
            });
            if (selected) selectedRow = rowIndex;
            rowIndex++;
        }

        if (selectedRow >= 0) {
            final int modelRow = selectedRow;
            javax.swing.SwingUtilities.invokeLater(() -> {
                int viewRow = tblFaskes.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    tblFaskes.setRowSelectionInterval(viewRow, viewRow);
                    tblFaskes.scrollRectToVisible(
                            tblFaskes.getCellRect(viewRow, 0, true));
                }
            });
        }
    }

    private void restoreApiResponseHistoryJson(String json) throws Exception {
        apiResponseHistory.clear();
        lastApiResponseJson = "";
        lastApiResponseSource = "Belum ada respon API";
        lastApiResponseTime = "";
        lastApiResponseError = false;
        if (safe(json).isEmpty()) return;

        JsonNode root = mapper.readTree(json);
        JsonNode history = root.path("riwayat");
        if (!history.isArray()) return;
        for (JsonNode item : history) {
            JsonNode response = item.path("response");
            String responseJson = response.isTextual()
                    ? response.asText()
                    : mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(response);
            ApiResponseEntry entry = new ApiResponseEntry(
                    snapshotText(item, "sumber", "API BPJS/SATUSEHAT"),
                    snapshotText(item, "waktu", ""),
                    responseJson,
                    "GAGAL".equalsIgnoreCase(
                            snapshotText(item, "status", "")));
            apiResponseHistory.add(entry);
            lastApiResponseSource = entry.source;
            lastApiResponseTime = entry.time;
            lastApiResponseJson = entry.json;
            lastApiResponseError = entry.error;
        }
    }

    private String snapshotText(JsonNode node, String field, String fallback) {
        if (node == null) return safe(fallback);
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) return safe(fallback);
        return safe(value.asText());
    }

    private void selectComboValue(javax.swing.JComboBox combo, String value) {
        if (combo == null || safe(value).isEmpty()) return;
        for (int index = 0; index < combo.getItemCount(); index++) {
            Object item = combo.getItemAt(index);
            if (item != null && value.equalsIgnoreCase(item.toString().trim())) {
                combo.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectProvinceByCode(String code) {
        if (safe(code).isEmpty()) return;
        for (int index = 0; index < PROVINSI.length; index++) {
            if (code.equals(PROVINSI[index][0])) {
                cbProvinsi.setSelectedIndex(index);
                return;
            }
        }
    }

    private void restoreSnapshotDate(widget.Tanggal component, String isoDate) {
        if (component == null || safe(isoDate).isEmpty()) return;
        try {
            component.setDate(java.sql.Date.valueOf(isoDate));
        } catch (Exception ex) {
            System.out.println("Tanggal snapshot tidak valid (" + isoDate + "): " + ex);
        }
    }

    private boolean isMissingSnapshotTable(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof java.sql.SQLException) {
                java.sql.SQLException sqlException =
                        (java.sql.SQLException) current;
                if (sqlException.getErrorCode() == 1146
                        || "42S02".equals(sqlException.getSQLState())) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private void showSepReferralReportPreview() {
        final String referralNumber = safe(tNoRujukanBpjs.getText());
        if (!referralPersisted || referralNumber.isEmpty()) {
            showModernToast(this,
                    "Cetakan belum tersedia. Pastikan rujukan sudah berhasil "
                            + "dikirim dan tersimpan ke database.",
                    ToastMessage.WARNING, 0);
            return;
        }

        setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.WAIT_CURSOR));
        final net.sf.jasperreports.engine.JasperPrint jasperPrint;
        try {
            java.util.Map<String, Object> parameters =
                    new java.util.HashMap<>();
            parameters.put("namars", akses.getnamars());
            parameters.put("alamatrs", akses.getalamatrs());
            parameters.put("kotars", akses.getkabupatenrs());
            parameters.put("propinsirs", akses.getpropinsirs());
            parameters.put("kontakrs", akses.getkontakrs());
            parameters.put("emailrs", akses.getemailrs());
            parameters.put("norujuk", referralNumber);
            parameters.put("logo", Sequel.cariGambar(
                    "select gambar.bpjs from gambar"));

            jasperPrint = fillSepReferralReport(parameters);
            if (jasperPrint.getPages() == null
                    || jasperPrint.getPages().isEmpty()) {
                throw new net.sf.jasperreports.engine.JRException(
                        "Data cetakan untuk No. Rujukan "
                                + referralNumber + " tidak ditemukan.");
            }
        } catch (Exception ex) {
            System.out.println("Gagal menyiapkan cetakan SEP rujukan: " + ex);
            showModernToast(this,
                    "Gagal menampilkan cetakan SEP rujukan: "
                            + safe(ex.getMessage()),
                    ToastMessage.ERROR, 0);
            setStatus("Cetakan SEP rujukan gagal ditampilkan.", true);
            return;
        } finally {
            setCursor(java.awt.Cursor.getDefaultCursor());
        }

        showModernSepReferralViewer(jasperPrint, referralNumber);
    }

    private net.sf.jasperreports.engine.JasperPrint fillSepReferralReport(
            java.util.Map<String, Object> parameters) throws Exception {
        final String exactReportName =
                "rptBridgingRujukanBPJSSatuSehat.jasper";
        java.io.InputStream bundled = getClass().getResourceAsStream(
                "/report/" + exactReportName);
        if (bundled != null) {
            try (java.io.InputStream input = bundled) {
                return net.sf.jasperreports.engine.JasperFillManager.fillReport(
                        input, parameters, koneksi);
            }
        }

        java.io.File exactFile = new java.io.File(
                System.getProperty("user.dir"), "report"
                        + java.io.File.separator + exactReportName);
        if (!exactFile.isFile()) {
            // Kompatibilitas nama lama yang berbeda kapitalisasi. Nama exact
            // tetap menjadi prioritas agar aman juga pada Linux.
            java.io.File legacyFile = new java.io.File(
                    System.getProperty("user.dir"), "report"
                            + java.io.File.separator
                            + "rptBridgingRujukanBPJSsatuSehat.jasper");
            if (legacyFile.isFile()) exactFile = legacyFile;
        }
        if (!exactFile.isFile()) {
            throw new java.io.FileNotFoundException(
                    "Report " + exactReportName + " tidak ditemukan di folder report.");
        }
        return net.sf.jasperreports.engine.JasperFillManager.fillReport(
                exactFile.getAbsolutePath(), parameters, koneksi);
    }

    private void showModernSepReferralViewer(
            final net.sf.jasperreports.engine.JasperPrint jasperPrint,
            final String referralNumber) {
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        configureRoundedPopupWindow(dialog, true);

        javax.swing.JPanel root = new javax.swing.JPanel(
                new java.awt.BorderLayout(0, 0));
        root.setBackground(java.awt.Color.WHITE);
        root.setBorder(new RoundedPopupBorder(
                new java.awt.Color(203, 213, 225)));
        dialog.setContentPane(root);

        javax.swing.JPanel header = new javax.swing.JPanel(
                new java.awt.BorderLayout(12, 0));
        header.setBackground(new java.awt.Color(248, 250, 252));
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 12)));

        javax.swing.JLabel title = new javax.swing.JLabel(
                "<html><b>Preview SEP Rujukan</b><br>"
                        + "<span style='font-size:9px;color:#64748B'>"
                        + "No. RJK BPJS: " + escapeHtml(referralNumber)
                        + " &nbsp;&nbsp; No. RJK SatuSehat: "
                        + escapeHtml(safe(tNoRujukanSatuSehat.getText()))
                        + "</span></html>");
        title.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 14));
        title.setForeground(new java.awt.Color(15, 23, 42));
        header.add(title, java.awt.BorderLayout.CENTER);

        javax.swing.JButton closeHeader = createFlatCloseButton();
        closeHeader.setToolTipText("Tutup preview");
        closeHeader.addActionListener(e -> dialog.dispose());
        header.add(closeHeader, java.awt.BorderLayout.EAST);
        installDialogDragSupport(header, dialog);
        root.add(header, java.awt.BorderLayout.NORTH);

        net.sf.jasperreports.swing.JRViewer viewer =
                new net.sf.jasperreports.swing.JRViewer(jasperPrint);
        viewer.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                8, 8, 2, 8));
        viewer.setBackground(new java.awt.Color(241, 245, 249));
        root.add(viewer, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, new java.awt.Color(226, 232, 240)));

        javax.swing.JButton print = new EncounterActionButton("Cetak", true);
        try {
            print.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/picture/b_print.png")));
        } catch (Exception ignore) {
        }
        styleEncounterDialogButton(print, true);
        print.setPreferredSize(new java.awt.Dimension(112, 34));
        print.addActionListener(e -> {
            try {
                boolean printed = net.sf.jasperreports.engine.JasperPrintManager
                        .printReport(jasperPrint, true);
                if (printed) {
                    showModernToast(dialog,
                            "Dokumen SEP rujukan telah dikirim ke printer.",
                            ToastMessage.SUCCESS, 0);
                }
            } catch (Exception ex) {
                System.out.println("Gagal mencetak SEP rujukan: " + ex);
                showModernToast(dialog,
                        "Gagal mencetak SEP rujukan: "
                                + safe(ex.getMessage()),
                        ToastMessage.ERROR, 0);
            }
        });

        javax.swing.JButton close = new EncounterActionButton("Tutup", false);
        styleEncounterDialogButton(close, false);
        close.setPreferredSize(new java.awt.Dimension(100, 34));
        close.addActionListener(e -> dialog.dispose());
        footer.add(print);
        footer.add(close);
        root.add(footer, java.awt.BorderLayout.SOUTH);

        java.awt.Rectangle bounds = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(1120, Math.max(760, bounds.width - 100));
        int height = Math.min(780, Math.max(560, bounds.height - 90));
        dialog.setSize(width, height);
        dialog.setMinimumSize(new java.awt.Dimension(760, 560));
        dialog.setLocationRelativeTo(this);
        showModalDialogWithBlur(dialog);
    }

    private void updateReferralActionState() {
        boolean active = tNoRujukanBpjs != null
                && !safe(tNoRujukanBpjs.getText()).isEmpty();
        boolean criteriaAvailable = modelKriteria != null
                && modelKriteria.getRowCount() > 0;
        boolean criteriaReady = criteriaAvailable
                && isCriteriaReadyForFaskes();
        boolean facilitySelected = !safe(selectedKdppkTujuan).isEmpty()
                && !safe(selectedKdppkSatuSehatTujuan).isEmpty();
        boolean printable = active && referralPersisted;

        if (btnCekKriteria != null) {
            btnCekKriteria.setEnabled(!active);
            btnCekKriteria.setToolTipText(active
                    ? "Hapus rujukan aktif sebelum memulai proses baru"
                    : "Langkah 1: ambil kriteria rujukan");
        }
        if (chkSinkronKriteriaUi != null) {
            chkSinkronKriteriaUi.setEnabled(!active);
        }
        if (btnCariFaskes != null) {
            btnCariFaskes.setEnabled(!active && criteriaReady);
            btnCariFaskes.setToolTipText(active
                    ? "Hapus rujukan aktif sebelum mencari faskes lain"
                    : !criteriaAvailable
                            ? "Selesaikan langkah Cek Kriteria terlebih dahulu"
                            : !criteriaReady
                                    ? "Pilih kriteria sesuai aturan sebelum mencari faskes"
                                    : "Langkah 2: cari faskes tujuan");
        }
        if (btnHapus != null) {
            btnHapus.setEnabled(active);
            btnHapus.setVisible(active);
            btnHapus.setToolTipText(active
                    ? "Hapus rujukan aktif agar dapat memilih faskes lain"
                    : "Belum ada rujukan aktif yang dapat dihapus");
        }
        if (btnKirim != null) {
            btnKirim.setEnabled(!active && criteriaReady && facilitySelected);
            btnKirim.setToolTipText(active
                    ? "Hapus rujukan aktif sebelum mengirim rujukan baru"
                    : !criteriaReady
                            ? "Selesaikan Cek Kriteria dan Cari Faskes terlebih dahulu"
                            : !facilitySelected
                                    ? "Pilih satu faskes tujuan terlebih dahulu"
                                    : "Langkah 3: kirim rujukan ke faskes terpilih");
        }
        if (btnCetakSepRujukanUi != null) {
            btnCetakSepRujukanUi.setVisible(printable);
            btnCetakSepRujukanUi.setEnabled(printable);
            btnCetakSepRujukanUi.setToolTipText(printable
                    ? "Tampilkan kembali cetakan SEP rujukan"
                    : "Cetakan tersedia setelah rujukan berhasil disimpan");
        }
        if (btnCopyNoRujukanBpjsUi != null) {
            btnCopyNoRujukanBpjsUi.setEnabled(active);
        }
        if (btnCopyNoRujukanSatuSehatUi != null) {
            btnCopyNoRujukanSatuSehatUi.setEnabled(
                    tNoRujukanSatuSehat != null
                            && !safe(tNoRujukanSatuSehat.getText()).isEmpty());
        }
        if (panelTombol != null) {
            panelTombol.revalidate();
            panelTombol.repaint();
        }
    }

    // =================================================================
    //  PEMULIHAN RUJUKAN LAMA
    //  Hanya melakukan GET dan menyimpan ulang hasil; tidak pernah INSERT API.
    // =================================================================
    private void showRecoverReferralDialog() {
        if (safe(noRawat).isEmpty()) {
            showModernToast(this,
                    "No. Rawat belum tersedia. Buka pemulihan dari data SEP pasien.",
                    ToastMessage.WARNING, 0);
            return;
        }

        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        final ReferralRecoveryResult[] recovered = {null};
        final boolean[] updatingInputs = {false};

        dialog.setUndecorated(true);
        configureRoundedPopupWindow(dialog, true);
        dialog.setResizable(false);
        dialog.setTitle("Pulihkan Rujukan Lama");
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        final java.awt.Color pageColor = new java.awt.Color(248, 250, 252);
        final java.awt.Color lineColor = new java.awt.Color(226, 232, 240);
        final java.awt.Color primaryColor = new java.awt.Color(37, 99, 235);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(pageColor);
        root.setBorder(new RoundedPopupBorder(new java.awt.Color(203, 213, 225)));

        javax.swing.JPanel header = new javax.swing.JPanel(
                new java.awt.BorderLayout(14, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, lineColor),
                javax.swing.BorderFactory.createEmptyBorder(14, 24, 13, 12)));
        header.setPreferredSize(new java.awt.Dimension(10, 78));

        javax.swing.JPanel titleArea = new javax.swing.JPanel(
                new java.awt.BorderLayout(12, 0));
        titleArea.setOpaque(false);
        javax.swing.JPanel accent = new javax.swing.JPanel();
        accent.setBackground(primaryColor);
        accent.setPreferredSize(new java.awt.Dimension(5, 44));

        javax.swing.JPanel heading = new javax.swing.JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(
                heading, javax.swing.BoxLayout.Y_AXIS));
        javax.swing.JLabel title = new javax.swing.JLabel(
                "Pulihkan Rujukan Lama");
        title.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 17));
        title.setForeground(new java.awt.Color(30, 41, 59));
        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Tarik data yang sudah terbit tanpa mengirim rujukan baru.");
        subtitle.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
        heading.add(title);
        heading.add(subtitle);
        titleArea.add(accent, java.awt.BorderLayout.WEST);
        titleArea.add(heading, java.awt.BorderLayout.CENTER);

        javax.swing.JButton close = createFlatCloseButton();
        close.setToolTipText("Tutup");
        close.addActionListener(e -> dialog.dispose());
        header.add(titleArea, java.awt.BorderLayout.CENTER);
        header.add(close, java.awt.BorderLayout.EAST);

        javax.swing.JTextField inputBpjs = createRecoveryInputField(
                safe(tNoRujukanBpjs.getText()));
        javax.swing.JTextField inputSatuSehat = createRecoveryInputField(
                safe(tNoRujukanSatuSehat.getText()));
        javax.swing.JTextField inputSep = createRecoveryInputField(noSep);

        javax.swing.JPanel formCard = new EncounterRoundedPanel(
                new java.awt.GridBagLayout(), java.awt.Color.WHITE,
                lineColor, 10);
        formCard.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formCard.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 142));
        formCard.setPreferredSize(new java.awt.Dimension(100, 142));
        addRecoveryInputRow(formCard, 0, "No. RJK BPJS", inputBpjs,
                "Pilihan utama untuk mengambil detail VClaim.");
        addRecoveryInputRow(formCard, 1, "No. RJK SatuSehat", inputSatuSehat,
                "Dipakai untuk menemukan ServiceRequest SATUSEHAT.");
        addRecoveryInputRow(formCard, 2, "No. SEP", inputSep,
                "Fallback melalui SEP dan riwayat rujukan peserta.");

        javax.swing.JTextArea preview = new javax.swing.JTextArea();
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        preview.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        preview.setForeground(new java.awt.Color(71, 85, 105));
        preview.setBackground(java.awt.Color.WHITE);
        preview.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        preview.setText("Masukkan minimal satu nomor, lalu klik Cari Data.\n"
                + "No. RJK BPJS + No. RJK SatuSehat akan memberi hasil paling lengkap.");

        javax.swing.JScrollPane previewScroll = new javax.swing.JScrollPane(preview);
        previewScroll.setBorder(javax.swing.BorderFactory.createLineBorder(lineColor));
        previewScroll.getViewport().setBackground(java.awt.Color.WHITE);
        previewScroll.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        previewScroll.setPreferredSize(new java.awt.Dimension(100, 190));
        previewScroll.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 190));

        javax.swing.JLabel progress = new javax.swing.JLabel("Siap mencari data.");
        progress.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        progress.setForeground(new java.awt.Color(100, 116, 139));
        progress.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        javax.swing.JPanel body = new javax.swing.JPanel();
        body.setBackground(pageColor);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));
        body.add(createRecoveryInfoCard());
        body.add(javax.swing.Box.createVerticalStrut(10));
        body.add(formCard);
        body.add(javax.swing.Box.createVerticalStrut(10));
        body.add(previewScroll);
        body.add(javax.swing.Box.createVerticalStrut(8));
        body.add(progress);

        javax.swing.JButton searchButton = new EncounterActionButton(
                "Cari Data", true);
        styleEncounterDialogButton(searchButton, true);
        searchButton.setPreferredSize(new java.awt.Dimension(118, 34));

        javax.swing.JButton saveButton = new EncounterActionButton(
                "Simpan Pemulihan", true);
        styleEncounterDialogButton(saveButton, true);
        saveButton.setPreferredSize(new java.awt.Dimension(150, 34));
        saveButton.setEnabled(false);

        javax.swing.JButton cancelButton = new EncounterActionButton(
                "Batal", false);
        styleEncounterDialogButton(cancelButton, false);
        cancelButton.setPreferredSize(new java.awt.Dimension(90, 34));
        cancelButton.addActionListener(e -> dialog.dispose());

        javax.swing.JPanel actions = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(searchButton);
        actions.add(saveButton);

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.BorderLayout(12, 0));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        1, 0, 0, 0, lineColor),
                javax.swing.BorderFactory.createEmptyBorder(11, 24, 11, 24)));
        footer.setPreferredSize(new java.awt.Dimension(10, 58));
        javax.swing.JLabel safety = new javax.swing.JLabel(
                "Sistem hanya membaca API; endpoint Insert tidak dipanggil.");
        safety.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 10));
        safety.setForeground(new java.awt.Color(100, 116, 139));
        footer.add(safety, java.awt.BorderLayout.CENTER);
        footer.add(actions, java.awt.BorderLayout.EAST);

        javax.swing.event.DocumentListener invalidateResult =
                new javax.swing.event.DocumentListener() {
            private void changed() {
                if (updatingInputs[0]) return;
                recovered[0] = null;
                saveButton.setEnabled(false);
                progress.setText("Nomor berubah. Klik Cari Data kembali.");
                progress.setForeground(new java.awt.Color(100, 116, 139));
            }

            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        };
        inputBpjs.getDocument().addDocumentListener(invalidateResult);
        inputSatuSehat.getDocument().addDocumentListener(invalidateResult);
        inputSep.getDocument().addDocumentListener(invalidateResult);

        searchButton.addActionListener(e -> {
            final String searchBpjs = safe(inputBpjs.getText());
            final String searchSatuSehat = safe(inputSatuSehat.getText());
            final String searchSep = safe(inputSep.getText());
            if (searchBpjs.isEmpty() && searchSatuSehat.isEmpty()
                    && searchSep.isEmpty()) {
                showModernToast(dialog,
                        "Isi minimal No. RJK BPJS, No. RJK SatuSehat, atau No. SEP.",
                        ToastMessage.WARNING, 0);
                return;
            }

            searchButton.setEnabled(false);
            saveButton.setEnabled(false);
            inputBpjs.setEnabled(false);
            inputSatuSehat.setEnabled(false);
            inputSep.setEnabled(false);
            progress.setText("Sedang menarik data dari BPJS dan SATUSEHAT...");
            progress.setForeground(primaryColor);
            preview.setText("Mohon tunggu. Sistem sedang melakukan pencarian read-only...");
            setCursor(java.awt.Cursor.getPredefinedCursor(
                    java.awt.Cursor.WAIT_CURSOR));

            new javax.swing.SwingWorker<ReferralRecoveryResult, Void>() {
                @Override
                protected ReferralRecoveryResult doInBackground() {
                    return lookupReferralForRecovery(
                            searchBpjs, searchSatuSehat, searchSep);
                }

                @Override
                protected void done() {
                    try {
                        ReferralRecoveryResult result = get();
                        appendRecoveryApiLogs(result);
                        enrichRecoveryFromLoadedFaskes(result);
                        String identityError = validateRecoveredIdentity(result);
                        if (!identityError.isEmpty()) {
                            recovered[0] = null;
                            preview.setText(buildRecoveryPreview(result)
                                    + "\n\nPEMULIHAN DIBLOKIR:\n" + identityError);
                            progress.setText("Data ditemukan, tetapi identitas pasien tidak cocok.");
                            progress.setForeground(new java.awt.Color(220, 38, 38));
                            showModernToast(dialog, identityError,
                                    ToastMessage.ERROR, 0);
                            return;
                        }

                        updatingInputs[0] = true;
                        try {
                            if (!safe(result.noRujukanBpjs).isEmpty()) {
                                inputBpjs.setText(result.noRujukanBpjs);
                            }
                            if (!safe(result.noRujukanSatuSehat).isEmpty()) {
                                inputSatuSehat.setText(result.noRujukanSatuSehat);
                            }
                            if (!safe(result.noSep).isEmpty()) {
                                inputSep.setText(result.noSep);
                            }
                        } finally {
                            updatingInputs[0] = false;
                        }

                        recovered[0] = result;
                        preview.setText(buildRecoveryPreview(result));
                        preview.setCaretPosition(0);
                        boolean canSave = result.verifiedBpjsNumber
                                && !safe(result.noRujukanBpjs).isEmpty();
                        saveButton.setEnabled(canSave);
                        if (canSave) {
                            progress.setText("Data ditemukan. Periksa hasil lalu klik Simpan Pemulihan.");
                            progress.setForeground(new java.awt.Color(22, 163, 74));
                            showModernToast(dialog,
                                    "Data rujukan ditemukan. Silakan periksa pratinjau.",
                                    ToastMessage.SUCCESS, 0);
                        } else {
                            progress.setText("No. RJK BPJS belum dapat diverifikasi. Periksa nomor lalu cari kembali.");
                            progress.setForeground(new java.awt.Color(245, 158, 11));
                            showModernToast(dialog,
                                    "ServiceRequest/SEP ditemukan, tetapi No. RJK BPJS belum dapat dipastikan.",
                                    ToastMessage.WARNING, 0);
                        }
                    } catch (Exception ex) {
                        Exception cause = unwrapRecoveryException(ex);
                        rememberApiException("Pemulihan Rujukan", cause);
                        recovered[0] = null;
                        preview.setText("Pemulihan gagal:\n" + safe(cause.getMessage()));
                        progress.setText("Pencarian gagal. Periksa Respon API.");
                        progress.setForeground(new java.awt.Color(220, 38, 38));
                        showModernToast(dialog,
                                "Gagal menarik data rujukan: "
                                        + safe(cause.getMessage()),
                                ToastMessage.ERROR, 0);
                    } finally {
                        searchButton.setEnabled(true);
                        inputBpjs.setEnabled(true);
                        inputSatuSehat.setEnabled(true);
                        inputSep.setEnabled(true);
                        setCursor(java.awt.Cursor.getDefaultCursor());
                    }
                }
            }.execute();
        });

        saveButton.addActionListener(e -> {
            ReferralRecoveryResult result = recovered[0];
            if (result == null) {
                showModernToast(dialog, "Cari dan periksa data terlebih dahulu.",
                        ToastMessage.WARNING, 0);
                return;
            }
            String activeNumber = safe(tNoRujukanBpjs.getText());
            if (!activeNumber.isEmpty()
                    && !activeNumber.equals(result.noRujukanBpjs)) {
                showModernToast(dialog,
                        "Form sudah memiliki rujukan aktif " + activeNumber
                                + ". Hapus rujukan tersebut sebelum memulihkan nomor lain.",
                        ToastMessage.ERROR, 0);
                return;
            }

            if (saveRecoveredReferral(result)) {
                dialog.dispose();
                showModernToast(BPJSRujukanSatuSehat.this,
                        "Rujukan lama berhasil dipulihkan dan disimpan.",
                        ToastMessage.SUCCESS, 0);
            }
        });

        root.add(header, java.awt.BorderLayout.NORTH);
        root.add(body, java.awt.BorderLayout.CENTER);
        root.add(footer, java.awt.BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(searchButton);
        dialog.getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                javax.swing.KeyStroke.getKeyStroke(
                        java.awt.event.KeyEvent.VK_ESCAPE, 0), "close-recovery");
        dialog.getRootPane().getActionMap().put("close-recovery",
                new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });
        installDialogDragSupport(header, dialog);
        dialog.setSize(760, 610);
        dialog.setLocationRelativeTo(this);
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (safe(inputBpjs.getText()).isEmpty()) inputBpjs.requestFocusInWindow();
            else inputBpjs.selectAll();
        });
        showModalDialogWithBlur(dialog);
    }

    private javax.swing.JTextField createRecoveryInputField(String value) {
        javax.swing.JTextField field = new javax.swing.JTextField(safe(value));
        styleModernFormInput(field, java.awt.Color.WHITE,
                new java.awt.Color(203, 213, 225));
        field.setPreferredSize(new java.awt.Dimension(310, 30));
        field.setMinimumSize(new java.awt.Dimension(180, 30));
        return field;
    }

    private void addRecoveryInputRow(javax.swing.JPanel parent, int row,
            String labelText, javax.swing.JTextField field, String helpText) {
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = row;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(row == 0 ? 12 : 5, 16,
                row == 2 ? 12 : 5, 8);

        javax.swing.JLabel label = new javax.swing.JLabel(labelText);
        label.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        label.setForeground(new java.awt.Color(71, 85, 105));
        label.setPreferredSize(new java.awt.Dimension(135, 30));
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        parent.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.62;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(row == 0 ? 12 : 5, 0,
                row == 2 ? 12 : 5, 12);
        parent.add(field, gbc);

        javax.swing.JLabel help = new javax.swing.JLabel(helpText);
        help.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        help.setForeground(new java.awt.Color(100, 116, 139));
        gbc.gridx = 2;
        gbc.weightx = 0.38;
        gbc.insets = new java.awt.Insets(row == 0 ? 12 : 5, 0,
                row == 2 ? 12 : 5, 16);
        parent.add(help, gbc);
    }

    private javax.swing.JPanel createRecoveryInfoCard() {
        javax.swing.JPanel card = new EncounterRoundedPanel(
                new java.awt.BorderLayout(10, 0),
                new java.awt.Color(239, 246, 255),
                new java.awt.Color(191, 219, 254), 10);
        card.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 54));
        card.setPreferredSize(new java.awt.Dimension(100, 54));
        javax.swing.JLabel icon = new javax.swing.JLabel(
                new RecoveryButtonIcon(new java.awt.Color(37, 99, 235)));
        icon.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 0));
        javax.swing.JLabel message = new javax.swing.JLabel(
                "<html>Gunakan nomor yang masih tersedia. Jika hanya ada SEP, "
                + "sistem mencari rujukan terbaru milik pasien dari faskes ini.</html>");
        message.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        message.setForeground(new java.awt.Color(30, 64, 175));
        message.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 7, 14));
        card.add(icon, java.awt.BorderLayout.WEST);
        card.add(message, java.awt.BorderLayout.CENTER);
        return card;
    }

    private Exception unwrapRecoveryException(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current instanceof Exception
                ? (Exception) current : new Exception(current.toString());
    }

    private ReferralRecoveryResult lookupReferralForRecovery(
            String inputBpjs, String inputSatuSehat, String inputSep) {
        ReferralRecoveryResult result = new ReferralRecoveryResult();
        // Jangan langsung menganggap SEP pada form sebagai hasil API. Nilai
        // form baru menjadi fallback setelah seluruh sumber selesai diperiksa,
        // sehingga SEP berbeda dari data lokal masih dapat diblokir validasi.
        result.noSep = safe(inputSep);
        result.noRujukanBpjs = safe(inputBpjs);
        result.noRujukanSatuSehat = safe(inputSatuSehat);
        result.kodeFaskesAsalSatuSehat = safe(kodeFaskesSatuSehat);
        result.idPasienSatuSehat = safe(idPasienSatuSehat);
        result.kdDokterSatuSehat = safe(kdDokterSatuSehat);
        result.encounterReference = safe(encounterRef);
        result.kodeDiagnosa = safe(kdPenyakitRujuk);
        result.namaDiagnosa = safe(nmPenyakitRujuk);
        result.kodePoli = safe(kdPoliRujuk);
        result.namaPoli = safe(nmPoliRujuk);

        loadLegacyRecoveryData(result, inputBpjs, inputSatuSehat, inputSep);

        String referralFromSep = "";
        if (!safe(inputSep).isEmpty()) {
            try {
                JsonNode sepResponse = callBpjsRecoveryGet(
                        "/SEP/" + encodeRecoveryPath(inputSep));
                result.apiLogs.add(new RecoveryApiLog(
                        "Pemulihan - BPJS Detail SEP", sepResponse));
                if (isBpjsRecoverySuccess(sepResponse)) {
                    referralFromSep = mergeSepRecoveryResult(result, sepResponse);
                } else {
                    result.notices.add("Detail SEP: "
                            + bpjsRecoveryMessage(sepResponse));
                }
            } catch (Exception ex) {
                addRecoveryFailure(result, "Pemulihan - BPJS Detail SEP", ex);
            }
        }

        // Nomor SATUSEHAT sering membawa identifier BPJS di resource yang sama.
        if (!safe(inputSatuSehat).isEmpty()) {
            findAndMergeServiceRequest(result,
                    "http://sys-ids.kemkes.go.id/referral-number-satusehat",
                    inputSatuSehat,
                    "Pemulihan - SATUSEHAT via No. RJK SatuSehat");
        }

        // Jika hanya ada SEP, cari kandidat rujukan keluar pada riwayat peserta.
        if (safe(result.noRujukanBpjs).isEmpty()
                && !safe(result.noKartuPeserta).isEmpty()) {
            try {
                JsonNode history = callBpjsRecoveryGet(
                        "/Rujukan/RS/List/Peserta/"
                                + encodeRecoveryPath(result.noKartuPeserta));
                result.apiLogs.add(new RecoveryApiLog(
                        "Pemulihan - BPJS Riwayat Rujukan Peserta", history));
                if (isBpjsRecoverySuccess(history)) {
                    RecoveryReferralCandidate candidate =
                            chooseRecoveryCandidate(history, result,
                                    referralFromSep);
                    if (candidate != null) {
                        result.noRujukanBpjs = candidate.noRujukan;
                        mergeRecoveryCandidate(result, candidate);
                    }
                } else {
                    result.notices.add("Riwayat rujukan BPJS: "
                            + bpjsRecoveryMessage(history));
                }
            } catch (Exception ex) {
                addRecoveryFailure(result,
                        "Pemulihan - BPJS Riwayat Rujukan Peserta", ex);
            }
        }

        // Verifikasi serta lengkapi data inti melalui endpoint detail rujukan.
        if (!safe(result.noRujukanBpjs).isEmpty()) {
            try {
                JsonNode bpjsResponse = callBpjsRecoveryGet(
                        "/Rujukan/RS/"
                                + encodeRecoveryPath(result.noRujukanBpjs));
                result.apiLogs.add(new RecoveryApiLog(
                        "Pemulihan - BPJS Detail Rujukan", bpjsResponse));
                if (isBpjsRecoverySuccess(bpjsResponse)) {
                    mergeBpjsRecoveryResult(result, bpjsResponse);
                } else {
                    result.notices.add("Detail rujukan BPJS: "
                            + bpjsRecoveryMessage(bpjsResponse));
                }
            } catch (Exception ex) {
                addRecoveryFailure(result,
                        "Pemulihan - BPJS Detail Rujukan", ex);
            }
        }

        // Bila ServiceRequest belum didapat, cari memakai identifier BPJS.
        if (!result.foundFromSatuSehat
                && !safe(result.noRujukanBpjs).isEmpty()) {
            findAndMergeServiceRequest(result,
                    "http://sys-ids.kemkes.go.id/referral-number-pcare",
                    result.noRujukanBpjs,
                    "Pemulihan - SATUSEHAT via No. RJK BPJS");
        }

        // Beberapa implementasi lama menaruh No. SEP sebagai value identifier
        // referral-number-pcare. Jalur ini hanya fallback dan tetap tidak boleh
        // menggantikan No. RJK BPJS yang sudah diverifikasi VClaim.
        if (!result.foundFromSatuSehat && !safe(result.noSep).isEmpty()) {
            findAndMergeServiceRequest(result,
                    "http://sys-ids.kemkes.go.id/referral-number-pcare",
                    result.noSep,
                    "Pemulihan - SATUSEHAT via No. SEP");
        }

        // Resource yang ditemukan via BPJS dapat mengungkap nomor nasional;
        // sebaliknya nomor nasional dapat mengungkap nomor BPJS.
        if (!result.foundFromSatuSehat
                && !safe(result.noRujukanSatuSehat).isEmpty()) {
            findAndMergeServiceRequest(result,
                    "http://sys-ids.kemkes.go.id/referral-number-satusehat",
                    result.noRujukanSatuSehat,
                    "Pemulihan - SATUSEHAT verifikasi akhir");
        }

        // _include tidak selalu didukung seluruh server FHIR. Jika performer
        // hanya berisi reference, lengkapi nama dan kode PPK melalui detail
        // Organization tanpa mengubah resource apa pun.
        enrichRecoveredDestinationOrganization(result);

        normalizeRecoveryResult(result);
        return result;
    }

    private JsonNode callBpjsRecoveryGet(String endpoint) throws Exception {
        ApiBPJS api = new ApiBPJS();
        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.add("X-Cons-ID", koneksiDB.CONSIDAPIBPJS());
        String utc = String.valueOf(api.GetUTCdatetimeAsString());
        headers.add("X-Timestamp", utc);
        headers.add("X-Signature", api.getHmac(utc));
        headers.add("user_key", koneksiDB.USERKEYAPIBPJS());

        org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(headers);
        String raw = api.getRest().exchange(
                koneksiDB.URLAPIBPJS() + endpoint,
                org.springframework.http.HttpMethod.GET,
                entity, String.class).getBody();
        JsonNode root = mapper.readTree(raw == null ? "{}" : raw);

        if (isBpjsRecoverySuccess(root) && root.has("response")) {
            JsonNode responseNode = root.path("response");
            JsonNode decoded = responseNode;
            if (!responseNode.isContainerNode()) {
                String encrypted = responseNode.asText();
                if (!safe(encrypted).isEmpty()) {
                    try {
                        decoded = mapper.readTree(api.Decrypt(encrypted, utc));
                    } catch (Exception decryptError) {
                        // Beberapa server development mengembalikan JSON tanpa
                        // enkripsi. Coba parse langsung sebelum menyatakan gagal.
                        decoded = mapper.readTree(encrypted);
                    }
                }
            }
            if (root instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) root)
                        .set("response", decoded);
            }
        }
        return root;
    }

    private JsonNode callSatuSehatRecoveryGet(String endpoint) throws Exception {
        ApiSatuSehat ihs = new ApiSatuSehat();
        org.springframework.http.HttpHeaders headers = ihs.buildAuthHeaders();
        org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(headers);
        String url = ihs.getBaseUrl() + endpoint;
        try {
            String raw = ihs.getRest().exchange(url,
                    org.springframework.http.HttpMethod.GET,
                    entity, String.class).getBody();
            return mapper.readTree(raw == null ? "{}" : raw);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 401) {
                ihs.forceRefreshToken();
                headers = ihs.buildAuthHeaders();
                entity = new org.springframework.http.HttpEntity<>(headers);
                String raw = ihs.getRest().exchange(url,
                        org.springframework.http.HttpMethod.GET,
                        entity, String.class).getBody();
                return mapper.readTree(raw == null ? "{}" : raw);
            }
            throw ex;
        }
    }

    private void findAndMergeServiceRequest(ReferralRecoveryResult result,
            String identifierSystem, String identifierValue, String source) {
        if (safe(identifierValue).isEmpty()) return;

        try {
            JsonNode response = queryServiceRequestForRecovery(
                    identifierSystem, identifierValue, true);
            result.apiLogs.add(new RecoveryApiLog(source, response));
            boolean found = mergeSatuSehatRecoveryResult(
                    result, response, identifierValue);

            // Fallback tanpa system membantu bila implementasi BPJS memakai
            // system identifier berbeda tetapi value nomornya tetap sama.
            if (!found) {
                JsonNode generic = queryServiceRequestForRecovery(
                        "", identifierValue, true);
                result.apiLogs.add(new RecoveryApiLog(
                        source + " (fallback identifier)", generic));
                found = mergeSatuSehatRecoveryResult(
                        result, generic, identifierValue);
            }
            if (!found) {
                result.notices.add("ServiceRequest SATUSEHAT tidak ditemukan untuk "
                        + identifierValue + ".");
            }
        } catch (Exception firstError) {
            // Sebagian FHIR server tidak mengizinkan _include pada pencarian.
            try {
                JsonNode simple = queryServiceRequestForRecovery(
                        identifierSystem, identifierValue, false);
                result.apiLogs.add(new RecoveryApiLog(
                        source + " (tanpa include)", simple));
                boolean found = mergeSatuSehatRecoveryResult(
                        result, simple, identifierValue);
                if (!found && !safe(identifierSystem).isEmpty()) {
                    JsonNode genericSimple = queryServiceRequestForRecovery(
                            "", identifierValue, false);
                    result.apiLogs.add(new RecoveryApiLog(
                            source + " (fallback tanpa include)", genericSimple));
                    found = mergeSatuSehatRecoveryResult(
                            result, genericSimple, identifierValue);
                }
                if (!found) {
                    result.notices.add("ServiceRequest SATUSEHAT tidak ditemukan untuk "
                            + identifierValue + ".");
                }
            } catch (Exception secondError) {
                // Jika filter system ditolak gateway, coba value identifier
                // saja sebagai upaya terakhir (tetap diverifikasi dari resource).
                try {
                    JsonNode genericSimple = queryServiceRequestForRecovery(
                            "", identifierValue, false);
                    result.apiLogs.add(new RecoveryApiLog(
                            source + " (identifier saja)", genericSimple));
                    if (!mergeSatuSehatRecoveryResult(
                            result, genericSimple, identifierValue)) {
                        result.notices.add("ServiceRequest SATUSEHAT tidak ditemukan untuk "
                                + identifierValue + ".");
                    }
                } catch (Exception finalError) {
                    addRecoveryFailure(result, source, finalError);
                }
            }
        }
    }

    private JsonNode queryServiceRequestForRecovery(String identifierSystem,
            String identifierValue, boolean includeResources) throws Exception {
        String token = safe(identifierSystem).isEmpty()
                ? safe(identifierValue)
                : safe(identifierSystem) + "|" + safe(identifierValue);
        String encoded = java.net.URLEncoder.encode(token, "UTF-8")
                .replace("+", "%20");
        String endpoint = "/ServiceRequest?identifier=" + encoded;
        if (includeResources) {
            endpoint += "&_include=ServiceRequest:performer"
                    + "&_include=ServiceRequest:supporting-info";
        }
        return callSatuSehatRecoveryGet(endpoint);
    }

    private void loadLegacyRecoveryData(ReferralRecoveryResult result,
            String inputBpjs, String inputSatuSehat, String inputSep) {
        String referralNumber = safe(inputBpjs);

        if (referralNumber.isEmpty() && !safe(inputSatuSehat).isEmpty()) {
            try (PreparedStatement statement = koneksi.prepareStatement(
                    "select no_rujukan,no_rujukan_satusehat,service_request_id,"
                    + "kode_faskes_satusehat,id_pasien_satusehat,"
                    + "kdppk_satusehat_tujuan,nama_faskes_tujuan,"
                    + "kd_dokter_satusehat,encounter_reference,"
                    + "patient_instruction,keterangan_rujukan "
                    + "from bridging_rujukan_satusehat "
                    + "where no_rujukan_satusehat=? limit 1")) {
                statement.setString(1, safe(inputSatuSehat));
                try (ResultSet data = statement.executeQuery()) {
                    if (data.next()) {
                        referralNumber = safe(data.getString(1));
                        mergeLegacySatuSehatData(result, data);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Pemulihan: data SATUSEHAT lokal tidak tersedia: " + ex);
            }
        }

        if (referralNumber.isEmpty() && !safe(inputSep).isEmpty()) {
            try (PreparedStatement statement = koneksi.prepareStatement(
                    "select no_rujukan from bridging_rujukan_bpjs "
                    + "where no_sep=? order by tglRujukan desc limit 1")) {
                statement.setString(1, safe(inputSep));
                try (ResultSet data = statement.executeQuery()) {
                    if (data.next()) referralNumber = safe(data.getString(1));
                }
            } catch (Exception ex) {
                System.out.println("Pemulihan: data BPJS lokal tidak tersedia: " + ex);
            }
        }

        if (!referralNumber.isEmpty()) {
            try (PreparedStatement statement = koneksi.prepareStatement(
                    "select no_sep,tglRujukan,tglRencanaKunjungan,ppkDirujuk,"
                    + "nm_ppkDirujuk,jnsPelayanan,catatan,diagRujukan,"
                    + "nama_diagRujukan,tipeRujukan,poliRujukan,"
                    + "nama_poliRujukan,no_rujukan "
                    + "from bridging_rujukan_bpjs where no_rujukan=? limit 1")) {
                statement.setString(1, referralNumber);
                try (ResultSet data = statement.executeQuery()) {
                    if (data.next()) {
                        result.foundFromLocal = true;
                        result.verifiedBpjsNumber = true;
                        result.noSep = firstRecoveryValue(
                                result.noSep, data.getString(1));
                        result.tglRujukan = safe(data.getString(2));
                        result.tglRencanaKunjungan = safe(data.getString(3));
                        result.kodePpkTujuan = safe(data.getString(4));
                        result.namaFaskesTujuan = safe(data.getString(5));
                        result.jenisPelayanan = safe(data.getString(6));
                        result.catatan = safe(data.getString(7));
                        result.kodeDiagnosa = firstRecoveryValue(
                                data.getString(8), result.kodeDiagnosa);
                        result.namaDiagnosa = firstRecoveryValue(
                                data.getString(9), result.namaDiagnosa);
                        result.tipeRujukan = safe(data.getString(10));
                        result.kodePoli = firstRecoveryValue(
                                data.getString(11), result.kodePoli);
                        result.namaPoli = firstRecoveryValue(
                                data.getString(12), result.namaPoli);
                        result.noRujukanBpjs = firstRecoveryValue(
                                result.noRujukanBpjs, data.getString(13));
                    }
                }
            } catch (Exception ex) {
                System.out.println("Pemulihan: detail BPJS lokal tidak tersedia: " + ex);
            }

            try (PreparedStatement statement = koneksi.prepareStatement(
                    "select no_rujukan,no_rujukan_satusehat,service_request_id,"
                    + "kode_faskes_satusehat,id_pasien_satusehat,"
                    + "kdppk_satusehat_tujuan,nama_faskes_tujuan,"
                    + "kd_dokter_satusehat,encounter_reference,"
                    + "patient_instruction,keterangan_rujukan "
                    + "from bridging_rujukan_satusehat "
                    + "where no_rujukan=? limit 1")) {
                statement.setString(1, referralNumber);
                try (ResultSet data = statement.executeQuery()) {
                    if (data.next()) mergeLegacySatuSehatData(result, data);
                }
            } catch (Exception ex) {
                System.out.println("Pemulihan: pelengkap SATUSEHAT lokal tidak tersedia: " + ex);
            }
        }
    }

    private void mergeLegacySatuSehatData(ReferralRecoveryResult result,
            ResultSet data) throws Exception {
        result.foundFromLocal = true;
        String localBpjsNumber = safe(data.getString(1));
        result.noRujukanBpjs = firstRecoveryValue(
                localBpjsNumber, result.noRujukanBpjs);
        if (!localBpjsNumber.isEmpty()) {
            result.verifiedBpjsNumber = true;
        }
        result.noRujukanSatuSehat = firstRecoveryValue(
                data.getString(2), result.noRujukanSatuSehat);
        if (!safe(data.getString(2)).isEmpty()) {
            result.verifiedSatuSehatNumber = true;
        }
        result.serviceRequestId = safe(data.getString(3));
        result.kodeFaskesAsalSatuSehat = firstRecoveryValue(
                data.getString(4), result.kodeFaskesAsalSatuSehat);
        result.idPasienSatuSehat = firstRecoveryValue(
                data.getString(5), result.idPasienSatuSehat);
        result.kdppkSatuSehatTujuan = safe(data.getString(6));
        result.namaFaskesTujuan = firstRecoveryValue(
                data.getString(7), result.namaFaskesTujuan);
        result.kdDokterSatuSehat = firstRecoveryValue(
                data.getString(8), result.kdDokterSatuSehat);
        result.encounterReference = firstRecoveryValue(
                data.getString(9), result.encounterReference);
        result.patientInstruction = safe(data.getString(10));
        result.keteranganRujukan = safe(data.getString(11));
    }

    private void addRecoveryFailure(ReferralRecoveryResult result,
            String source, Exception exception) {
        String message = safe(exception == null ? "" : exception.getMessage());
        if (message.isEmpty() && exception != null) {
            message = exception.getClass().getSimpleName();
        }
        result.notices.add(source + " gagal: " + message);
        com.fasterxml.jackson.databind.node.ObjectNode error =
                mapper.createObjectNode();
        error.put("status", "ERROR");
        error.put("sumber", source);
        error.put("exception", exception == null
                ? "Exception" : exception.getClass().getSimpleName());
        error.put("message", message);
        String responseBody = extractApiErrorBody(exception);
        if (!responseBody.isEmpty()) error.put("responseBody", responseBody);
        result.apiLogs.add(new RecoveryApiLog(source, error));
    }

    private boolean isBpjsRecoverySuccess(JsonNode root) {
        return root != null && "200".equals(
                root.path("metaData").path("code").asText());
    }

    private String bpjsRecoveryMessage(JsonNode root) {
        String message = root == null ? "" : root.path("metaData")
                .path("message").asText();
        return safe(message).isEmpty() ? "Data tidak ditemukan" : message;
    }

    private String encodeRecoveryPath(String value) throws Exception {
        return java.net.URLEncoder.encode(safe(value), "UTF-8")
                .replace("+", "%20");
    }

    private String firstRecoveryValue(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (!safe(value).isEmpty()) return safe(value);
        }
        return "";
    }

    private String mergeSepRecoveryResult(ReferralRecoveryResult result,
            JsonNode root) {
        JsonNode sep = root.path("response");
        result.noKartuPeserta = firstRecoveryValue(
                sep.path("peserta").path("noKartu").asText(),
                result.noKartuPeserta);
        result.namaPasienBpjs = firstRecoveryValue(
                sep.path("peserta").path("nama").asText(),
                result.namaPasienBpjs);
        result.namaDiagnosa = firstRecoveryValue(
                result.namaDiagnosa, sep.path("diagnosa").asText());
        result.namaPoli = firstRecoveryValue(
                result.namaPoli, sep.path("poli").asText());
        result.jenisPelayanan = firstRecoveryValue(
                result.jenisPelayanan,
                normalizeJenisPelayananRecovery(
                        sep.path("jnsPelayanan").asText()));
        result.tglRujukan = firstRecoveryValue(
                result.tglRujukan, dateOnlyRecovery(
                        sep.path("tglSep").asText()));
        return safe(sep.path("noRujukan").asText());
    }

    private void mergeBpjsRecoveryResult(ReferralRecoveryResult result,
            JsonNode root) {
        JsonNode response = root.path("response");
        JsonNode referral = response.has("rujukan")
                ? response.path("rujukan") : response;
        if (referral == null || referral.isMissingNode()
                || referral.isNull()) return;

        String apiNumber = safe(referral.path("noKunjungan").asText());
        if (apiNumber.isEmpty() && referral.size() == 0) return;
        result.foundFromBpjs = true;
        result.noRujukanBpjs = firstRecoveryValue(
                apiNumber, result.noRujukanBpjs);
        result.verifiedBpjsNumber = !safe(result.noRujukanBpjs).isEmpty();
        result.noKartuPeserta = firstRecoveryValue(
                referral.path("peserta").path("noKartu").asText(),
                result.noKartuPeserta);
        result.namaPasienBpjs = firstRecoveryValue(
                referral.path("peserta").path("nama").asText(),
                result.namaPasienBpjs);
        result.kodeDiagnosa = firstRecoveryValue(
                referral.path("diagnosa").path("kode").asText(),
                result.kodeDiagnosa);
        result.namaDiagnosa = firstRecoveryValue(
                referral.path("diagnosa").path("nama").asText(),
                result.namaDiagnosa);
        result.kodePoli = firstRecoveryValue(
                referral.path("poliRujukan").path("kode").asText(),
                result.kodePoli);
        result.namaPoli = firstRecoveryValue(
                referral.path("poliRujukan").path("nama").asText(),
                result.namaPoli);
        result.jenisPelayanan = firstRecoveryValue(
                normalizeJenisPelayananRecovery(
                        referral.path("pelayanan").path("kode").asText()),
                result.jenisPelayanan);
        result.catatan = firstRecoveryValue(
                referral.path("keluhan").asText(), result.catatan);
        result.tglRujukan = firstRecoveryValue(
                dateOnlyRecovery(referral.path("tglKunjungan").asText()),
                result.tglRujukan);
    }

    private RecoveryReferralCandidate chooseRecoveryCandidate(JsonNode root,
            ReferralRecoveryResult result, String referralFromSep) {
        JsonNode referrals = root.path("response").path("rujukan");
        if (!referrals.isArray()) return null;

        String currentPpk = currentRecoveryPpk();
        String minDate = safe(result.tglRujukan);
        java.util.List<RecoveryReferralCandidate> all =
                new java.util.ArrayList<>();
        java.util.List<RecoveryReferralCandidate> fromCurrentHospital =
                new java.util.ArrayList<>();

        for (JsonNode item : referrals) {
            RecoveryReferralCandidate candidate =
                    new RecoveryReferralCandidate();
            candidate.noRujukan = safe(item.path("noKunjungan").asText());
            candidate.tglRujukan = dateOnlyRecovery(
                    item.path("tglKunjungan").asText());
            candidate.kodeDiagnosa = safe(
                    item.path("diagnosa").path("kode").asText());
            candidate.namaDiagnosa = safe(
                    item.path("diagnosa").path("nama").asText());
            candidate.kodePoli = safe(
                    item.path("poliRujukan").path("kode").asText());
            candidate.namaPoli = safe(
                    item.path("poliRujukan").path("nama").asText());
            candidate.kodePerujuk = safe(
                    item.path("provPerujuk").path("kode").asText());
            candidate.namaPerujuk = safe(
                    item.path("provPerujuk").path("nama").asText());

            if (candidate.noRujukan.isEmpty()
                    || candidate.noRujukan.equals(safe(referralFromSep))) {
                continue;
            }
            if (!minDate.isEmpty() && !candidate.tglRujukan.isEmpty()
                    && candidate.tglRujukan.compareTo(minDate) < 0) {
                continue;
            }
            all.add(candidate);
            if (!currentPpk.isEmpty()
                    && (candidate.kodePerujuk.equalsIgnoreCase(currentPpk)
                    || candidate.noRujukan.toUpperCase(
                            java.util.Locale.ROOT).startsWith(
                                    currentPpk.toUpperCase(
                                            java.util.Locale.ROOT)))) {
                fromCurrentHospital.add(candidate);
            }
        }

        java.util.List<RecoveryReferralCandidate> candidates;
        if (!currentPpk.isEmpty()) {
            candidates = fromCurrentHospital;
            if (candidates.isEmpty()) {
                result.notices.add("Riwayat peserta ditemukan, tetapi tidak ada "
                        + "rujukan keluar yang berasal dari PPK RS ini. Masukkan "
                        + "No. RJK BPJS agar pencarian tetap pasti.");
                return null;
            }
        } else {
            candidates = all;
            if (candidates.size() > 1) {
                result.notices.add("Kode PPK RS belum tersedia dan terdapat "
                        + candidates.size() + " kandidat. Sistem tidak memilih "
                        + "otomatis; masukkan No. RJK BPJS yang tepat.");
                return null;
            }
        }
        if (candidates.isEmpty()) {
            result.notices.add("Tidak ada kandidat rujukan keluar yang dapat "
                    + "dipastikan dari riwayat peserta.");
            return null;
        }

        java.util.Collections.sort(candidates,
                new java.util.Comparator<RecoveryReferralCandidate>() {
            @Override
            public int compare(RecoveryReferralCandidate left,
                    RecoveryReferralCandidate right) {
                String leftKey = safe(left.tglRujukan) + safe(left.noRujukan);
                String rightKey = safe(right.tglRujukan) + safe(right.noRujukan);
                return rightKey.compareTo(leftKey);
            }
        });
        if (candidates.size() > 1) {
            result.notices.add("Ditemukan " + candidates.size()
                    + " kandidat berdasarkan SEP; sistem memilih rujukan terbaru "
                    + candidates.get(0).noRujukan
                    + ". Pastikan nomor pada pratinjau sudah benar.");
        } else {
            result.notices.add("No. RJK BPJS ditemukan melalui riwayat peserta SEP.");
        }
        return candidates.get(0);
    }

    private String currentRecoveryPpk() {
        try {
            java.lang.reflect.Method getter = koneksiDB.class.getMethod(
                    "KODEPPKBPJSSISRUTE");
            Object value = getter.invoke(null);
            if (value != null && !safe(value.toString()).isEmpty()) {
                return safe(value.toString());
            }
        } catch (Exception ignored) {
            // Instalasi lama dapat belum memiliki getter khusus SISRUTE.
        }
        try {
            return safe(akses.getkodeppkbpjs());
        } catch (Exception ignored) {
            return "";
        }
    }

    private void mergeRecoveryCandidate(ReferralRecoveryResult result,
            RecoveryReferralCandidate candidate) {
        if (candidate == null) return;
        result.noRujukanBpjs = firstRecoveryValue(
                candidate.noRujukan, result.noRujukanBpjs);
        result.verifiedBpjsNumber = !safe(candidate.noRujukan).isEmpty();
        result.tglRujukan = firstRecoveryValue(
                candidate.tglRujukan, result.tglRujukan);
        result.kodeDiagnosa = firstRecoveryValue(
                candidate.kodeDiagnosa, result.kodeDiagnosa);
        result.namaDiagnosa = firstRecoveryValue(
                candidate.namaDiagnosa, result.namaDiagnosa);
        result.kodePoli = firstRecoveryValue(
                candidate.kodePoli, result.kodePoli);
        result.namaPoli = firstRecoveryValue(
                candidate.namaPoli, result.namaPoli);
    }

    private boolean mergeSatuSehatRecoveryResult(
            ReferralRecoveryResult result, JsonNode response,
            String expectedIdentifier) {
        JsonNode serviceRequest = findServiceRequestRecoveryResource(
                response, expectedIdentifier);
        if (serviceRequest == null) return false;

        result.foundFromSatuSehat = true;
        result.serviceRequestId = firstRecoveryValue(
                serviceRequest.path("id").asText(),
                result.serviceRequestId);

        JsonNode identifiers = serviceRequest.path("identifier");
        if (identifiers.isArray()) {
            for (JsonNode identifier : identifiers) {
                String system = safe(identifier.path("system").asText())
                        .toLowerCase(java.util.Locale.ROOT);
                String value = safe(identifier.path("value").asText());
                if (system.contains("referral-number-satusehat")) {
                    result.noRujukanSatuSehat = firstRecoveryValue(
                            value, result.noRujukanSatuSehat);
                    if (!value.isEmpty()) {
                        result.verifiedSatuSehatNumber = true;
                    }
                } else if (system.contains("referral-number-pcare")
                        || system.contains("referral-number-bpjs")
                        || system.contains("referral-number-vclaim")) {
                    boolean valueIsSep = !value.isEmpty()
                            && (value.equalsIgnoreCase(safe(result.noSep))
                            || value.equalsIgnoreCase(safe(noSep)));
                    if (valueIsSep) {
                        result.noSep = firstRecoveryValue(
                                result.noSep, value);
                    } else if (!value.isEmpty()) {
                        if (!result.verifiedBpjsNumber) {
                            result.noRujukanBpjs = value;
                        } else if (!value.equalsIgnoreCase(
                                safe(result.noRujukanBpjs))) {
                            result.notices.add("Identifier BPJS pada ServiceRequest ("
                                    + value + ") berbeda dari hasil VClaim ("
                                    + result.noRujukanBpjs
                                    + "); nomor VClaim dipertahankan.");
                        }
                        result.verifiedBpjsNumber = true;
                    }
                } else if (system.contains("sep")) {
                    result.noSep = firstRecoveryValue(result.noSep, value);
                }
            }
        }

        result.idPasienSatuSehat = firstRecoveryValue(
                referenceIdRecovery(serviceRequest.path("subject")
                        .path("reference").asText(), "Patient"),
                result.idPasienSatuSehat);
        result.encounterReference = firstRecoveryValue(
                referenceIdRecovery(serviceRequest.path("encounter")
                        .path("reference").asText(), "Encounter"),
                result.encounterReference);

        JsonNode performer = serviceRequest.path("performer");
        if (performer.isArray() && performer.size() > 0) {
            JsonNode destination = performer.get(0);
            result.kdppkSatuSehatTujuan = firstRecoveryValue(
                    referenceIdRecovery(destination.path("reference").asText(),
                            "Organization"),
                    destination.path("identifier").path("value").asText(),
                    result.kdppkSatuSehatTujuan);
            result.namaFaskesTujuan = firstRecoveryValue(
                    destination.path("display").asText(),
                    result.namaFaskesTujuan);
        }

        JsonNode requester = serviceRequest.path("requester");
        String requesterReference = requester.path("reference").asText();
        String practitioner = referenceIdRecovery(
                requesterReference, "Practitioner");
        if (!practitioner.isEmpty()) {
            result.kdDokterSatuSehat = firstRecoveryValue(
                    practitioner, result.kdDokterSatuSehat);
        } else {
            result.kodeFaskesAsalSatuSehat = firstRecoveryValue(
                    referenceIdRecovery(requesterReference, "Organization"),
                    result.kodeFaskesAsalSatuSehat);
        }

        result.patientInstruction = firstRecoveryValue(
                serviceRequest.path("patientInstruction").asText(),
                result.patientInstruction);
        result.keteranganRujukan = firstRecoveryValue(
                firstArrayTextRecovery(serviceRequest.path("note"), "text"),
                serviceRequest.path("reasonCode").path(0).path("text").asText(),
                result.keteranganRujukan);
        result.tglRujukan = firstRecoveryValue(
                dateOnlyRecovery(serviceRequest.path("authoredOn").asText()),
                result.tglRujukan);
        result.tglRencanaKunjungan = firstRecoveryValue(
                dateOnlyRecovery(serviceRequest.path("occurrenceDateTime").asText()),
                dateOnlyRecovery(serviceRequest.path("occurrencePeriod")
                        .path("start").asText()),
                result.tglRencanaKunjungan);

        mergeDiagnosisFromServiceRequest(result, serviceRequest);
        mergeIncludedRecoveryResources(result, response);
        return true;
    }

    private JsonNode findServiceRequestRecoveryResource(JsonNode response,
            String expectedIdentifier) {
        if (response == null) return null;
        if ("ServiceRequest".equalsIgnoreCase(
                response.path("resourceType").asText())) {
            return safe(expectedIdentifier).isEmpty()
                    || serviceRequestHasIdentifier(response, expectedIdentifier)
                            ? response : null;
        }
        JsonNode entries = response.path("entry");
        if (!entries.isArray()) return null;

        for (JsonNode entry : entries) {
            JsonNode resource = entry.path("resource");
            if (!"ServiceRequest".equalsIgnoreCase(
                    resource.path("resourceType").asText())) continue;
            if (serviceRequestHasIdentifier(resource, expectedIdentifier)) {
                return resource;
            }
        }
        return null;
    }

    private boolean serviceRequestHasIdentifier(JsonNode resource,
            String expectedIdentifier) {
        if (safe(expectedIdentifier).isEmpty()) return true;
        for (JsonNode identifier : resource.path("identifier")) {
            if (safe(expectedIdentifier).equalsIgnoreCase(
                    safe(identifier.path("value").asText()))) return true;
        }
        return false;
    }

    private void mergeDiagnosisFromServiceRequest(
            ReferralRecoveryResult result, JsonNode serviceRequest) {
        JsonNode reasonCodes = serviceRequest.path("reasonCode");
        if (!reasonCodes.isArray()) return;
        for (JsonNode reason : reasonCodes) {
            for (JsonNode coding : reason.path("coding")) {
                String system = safe(coding.path("system").asText())
                        .toLowerCase(java.util.Locale.ROOT);
                if (system.contains("icd-10") || system.contains("icd10")) {
                    result.kodeDiagnosa = firstRecoveryValue(
                            coding.path("code").asText(),
                            result.kodeDiagnosa);
                    result.namaDiagnosa = firstRecoveryValue(
                            coding.path("display").asText(),
                            reason.path("text").asText(),
                            result.namaDiagnosa);
                    return;
                }
            }
        }
    }

    private void mergeIncludedRecoveryResources(
            ReferralRecoveryResult result, JsonNode response) {
        JsonNode entries = response.path("entry");
        if (!entries.isArray()) return;

        for (JsonNode entry : entries) {
            JsonNode resource = entry.path("resource");
            String resourceType = safe(resource.path("resourceType").asText());
            if ("Organization".equalsIgnoreCase(resourceType)) {
                mergeRecoveryOrganization(result, resource);
            } else if ("QuestionnaireResponse".equalsIgnoreCase(resourceType)) {
                collectRecoveryCriteriaRows(
                        resource.path("item"), result.criteriaRows);
            }
        }
    }

    private void enrichRecoveredDestinationOrganization(
            ReferralRecoveryResult result) {
        if (result == null || !result.foundFromSatuSehat
                || safe(result.kdppkSatuSehatTujuan).isEmpty()
                || (!safe(result.kodePpkTujuan).isEmpty()
                && !safe(result.namaFaskesTujuan).isEmpty())) {
            return;
        }
        try {
            JsonNode organization = callSatuSehatRecoveryGet(
                    "/Organization/" + encodeRecoveryPath(
                            result.kdppkSatuSehatTujuan));
            result.apiLogs.add(new RecoveryApiLog(
                    "Pemulihan - SATUSEHAT Detail Faskes Tujuan",
                    organization));
            mergeRecoveryOrganization(result, organization);
        } catch (Exception ex) {
            addRecoveryFailure(result,
                    "Pemulihan - SATUSEHAT Detail Faskes Tujuan", ex);
        }
    }

    private void mergeRecoveryOrganization(ReferralRecoveryResult result,
            JsonNode organization) {
        if (result == null || organization == null
                || !"Organization".equalsIgnoreCase(
                        organization.path("resourceType").asText())) {
            return;
        }
        String id = safe(organization.path("id").asText());
        if (!id.equalsIgnoreCase(
                safe(result.kdppkSatuSehatTujuan))) return;
        result.namaFaskesTujuan = firstRecoveryValue(
                organization.path("name").asText(),
                result.namaFaskesTujuan);
        result.kodePpkTujuan = firstRecoveryValue(
                findOrganizationPpkRecovery(organization),
                result.kodePpkTujuan);
    }

    private String findOrganizationPpkRecovery(JsonNode organization) {
        for (JsonNode identifier : organization.path("identifier")) {
            String system = safe(identifier.path("system").asText())
                    .toLowerCase(java.util.Locale.ROOT);
            String value = safe(identifier.path("value").asText());
            if (value.isEmpty()) continue;
            if (system.contains("bpjs") || system.contains("ppk")) {
                return value;
            }
        }
        // Jangan menebak kode PPK dari identifier organisasi umum karena
        // nilainya dapat berupa Organization ID SATUSEHAT.
        return "";
    }

    private void collectRecoveryCriteriaRows(JsonNode items,
            java.util.List<Object[]> output) {
        if (!items.isArray()) return;
        for (JsonNode item : items) {
            JsonNode answers = item.path("answer");
            if (answers.isArray() && answers.size() > 0) {
                JsonNode answer = answers.get(0);
                String type;
                String value;
                if (answer.has("valueBoolean")) {
                    type = "boolean";
                    value = answer.path("valueBoolean").asBoolean()
                            ? "YA" : "TIDAK";
                } else {
                    type = "text";
                    value = firstRecoveryValue(
                            answer.path("valueString").asText(),
                            answer.path("valueCoding").path("code").asText(),
                            answer.path("valueInteger").asText());
                }
                output.add(new Object[]{
                    safe(item.path("linkId").asText()),
                    safe(item.path("text").asText()),
                    type,
                    value
                });
            }
            collectRecoveryCriteriaRows(item.path("item"), output);
            for (JsonNode answer : answers) {
                collectRecoveryCriteriaRows(answer.path("item"), output);
            }
        }
    }

    private String referenceIdRecovery(String reference, String resourceType) {
        String value = safe(reference);
        String marker = safe(resourceType) + "/";
        int position = value.indexOf(marker);
        if (position < 0) return "";
        value = value.substring(position + marker.length());
        int slash = value.indexOf('/');
        int query = value.indexOf('?');
        int hash = value.indexOf('#');
        int end = value.length();
        if (slash >= 0) end = Math.min(end, slash);
        if (query >= 0) end = Math.min(end, query);
        if (hash >= 0) end = Math.min(end, hash);
        return safe(value.substring(0, end));
    }

    private String firstArrayTextRecovery(JsonNode array, String fieldName) {
        if (!array.isArray()) return "";
        for (JsonNode item : array) {
            String value = safe(item.path(fieldName).asText());
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    private String dateOnlyRecovery(String dateTime) {
        String value = safe(dateTime);
        if (value.length() >= 10
                && value.charAt(4) == '-' && value.charAt(7) == '-') {
            return value.substring(0, 10);
        }
        return value;
    }

    private String normalizeJenisPelayananRecovery(String value) {
        String normalized = safe(value).toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("1") || normalized.contains("inap")) return "1";
        if (normalized.startsWith("2") || normalized.contains("jalan")) return "2";
        return safe(value);
    }

    private void normalizeRecoveryResult(ReferralRecoveryResult result) {
        if (!result.verifiedSatuSehatNumber
                && !safe(result.noRujukanSatuSehat).isEmpty()) {
            result.notices.add("No. RJK SatuSehat yang dimasukkan belum "
                    + "terverifikasi dan tidak akan disimpan.");
            result.noRujukanSatuSehat = "";
        }
        result.noSep = firstRecoveryValue(result.noSep, noSep);
        result.kodeFaskesAsalSatuSehat = firstRecoveryValue(
                result.kodeFaskesAsalSatuSehat, kodeFaskesSatuSehat);
        result.idPasienSatuSehat = firstRecoveryValue(
                result.idPasienSatuSehat, idPasienSatuSehat);
        result.kdDokterSatuSehat = firstRecoveryValue(
                result.kdDokterSatuSehat, kdDokterSatuSehat);
        result.encounterReference = firstRecoveryValue(
                result.encounterReference, encounterRef);
        result.kodeDiagnosa = firstRecoveryValue(
                result.kodeDiagnosa, kdPenyakitRujuk, kdPenyakit);
        result.namaDiagnosa = firstRecoveryValue(
                result.namaDiagnosa, nmPenyakitRujuk, nmPenyakit);
        result.kodePoli = firstRecoveryValue(
                result.kodePoli, kdPoliRujuk, kdPoliBpjs);
        result.namaPoli = firstRecoveryValue(
                result.namaPoli, nmPoliRujuk, nmPoli);
        result.jenisPelayanan = firstRecoveryValue(
                normalizeJenisPelayananRecovery(result.jenisPelayanan), "2");
        result.tipeRujukan = normalizeTipeRujukanRecovery(
                result.tipeRujukan);
        result.tglRencanaKunjungan = firstRecoveryValue(
                result.tglRencanaKunjungan, result.tglRujukan);
        result.patientInstruction = firstRecoveryValue(
                result.patientInstruction,
                safe(result.namaFaskesTujuan).isEmpty() ? ""
                        : "Rujukan ke " + result.namaFaskesTujuan);
        result.keteranganRujukan = firstRecoveryValue(
                result.keteranganRujukan, result.patientInstruction);
    }

    private String normalizeTipeRujukanRecovery(String value) {
        String normalized = safe(value).toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("1") || normalized.contains("partial")) {
            return "1. Partial";
        }
        if (normalized.startsWith("2") || normalized.contains("balik")) {
            return "2. Rujuk Balik";
        }
        return "0. Penuh";
    }

    private void appendRecoveryApiLogs(ReferralRecoveryResult result) {
        if (result == null) return;
        for (RecoveryApiLog log : result.apiLogs) {
            String formatted;
            try {
                formatted = log.response == null ? "{}"
                        : mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(log.response);
            } catch (Exception ex) {
                formatted = log.response == null
                        ? "{}" : log.response.toString();
            }
            storeApiResponse(log.source, formatted,
                    responseLooksError(log.response), log.time);
        }
    }

    private String validateRecoveredIdentity(ReferralRecoveryResult result) {
        if (result == null) return "Hasil pemulihan kosong.";
        if (!safe(noSep).isEmpty() && !safe(result.noSep).isEmpty()
                && !safe(noSep).equalsIgnoreCase(safe(result.noSep))) {
            return "No. SEP hasil pencarian (" + result.noSep
                    + ") tidak sama dengan SEP pada form (" + noSep + ").";
        }
        if (!safe(idPasienSatuSehat).isEmpty()
                && !safe(result.idPasienSatuSehat).isEmpty()
                && !safe(idPasienSatuSehat).equalsIgnoreCase(
                        safe(result.idPasienSatuSehat))) {
            return "IHS pasien pada ServiceRequest tidak sama dengan pasien "
                    + "yang sedang dibuka.";
        }

        if (!safe(result.noKartuPeserta).isEmpty() && !safe(noRm).isEmpty()) {
            String localCard = safe(Sequel.cariIsi(
                    "select no_peserta from pasien where no_rkm_medis=?",
                    noRm));
            if (!localCard.isEmpty()
                    && !localCard.equalsIgnoreCase(result.noKartuPeserta)) {
                return "Nomor kartu peserta BPJS hasil pencarian tidak sama "
                        + "dengan pasien yang sedang dibuka.";
            }
        }
        return "";
    }

    private String buildRecoveryPreview(ReferralRecoveryResult result) {
        if (result == null) return "Tidak ada data.";
        StringBuilder preview = new StringBuilder();
        preview.append("HASIL PEMULIHAN\n")
                .append("No. Rawat              : ").append(safe(noRawat)).append('\n')
                .append("No. SEP                : ").append(displayRecoveryValue(result.noSep)).append('\n')
                .append("No. RJK BPJS           : ").append(displayRecoveryValue(result.noRujukanBpjs)).append('\n')
                .append("Verifikasi RJK BPJS    : ")
                .append(result.verifiedBpjsNumber
                        ? "TERVERIFIKASI" : "BELUM TERVERIFIKASI").append('\n')
                .append("No. RJK SatuSehat      : ").append(displayRecoveryValue(result.noRujukanSatuSehat)).append('\n')
                .append("ServiceRequest ID      : ").append(displayRecoveryValue(result.serviceRequestId)).append('\n')
                .append("Pasien BPJS            : ").append(displayRecoveryValue(result.namaPasienBpjs)).append('\n')
                .append("Diagnosa               : ").append(displayRecoveryPair(result.kodeDiagnosa, result.namaDiagnosa)).append('\n')
                .append("Poli tujuan            : ").append(displayRecoveryPair(result.kodePoli, result.namaPoli)).append('\n')
                .append("Faskes tujuan          : ").append(displayRecoveryPair(result.kodePpkTujuan, result.namaFaskesTujuan)).append('\n')
                .append("Kode SS faskes tujuan  : ").append(displayRecoveryValue(result.kdppkSatuSehatTujuan)).append('\n')
                .append("Tanggal rujukan        : ").append(displayRecoveryValue(result.tglRujukan)).append('\n')
                .append("Tanggal rencana        : ").append(displayRecoveryValue(result.tglRencanaKunjungan)).append('\n')
                .append("Sumber                  : ")
                .append(recoverySourceSummary(result));

        java.util.List<String> missing = new java.util.ArrayList<>();
        if (safe(result.noRujukanSatuSehat).isEmpty()) missing.add("No. RJK SatuSehat");
        if (safe(result.serviceRequestId).isEmpty()) missing.add("ServiceRequest ID");
        if (safe(result.kdppkSatuSehatTujuan).isEmpty()) {
            missing.add("Kode SS faskes tujuan");
        }
        if (!missing.isEmpty()) {
            preview.append("\n\nDATA BELUM DITEMUKAN\n- ")
                    .append(String.join("\n- ", missing));
        }
        if (!result.notices.isEmpty()) {
            preview.append("\n\nCATATAN\n- ")
                    .append(String.join("\n- ", result.notices));
        }
        return preview.toString();
    }

    private String displayRecoveryValue(String value) {
        return safe(value).isEmpty() ? "(belum ditemukan)" : safe(value);
    }

    private String displayRecoveryPair(String code, String name) {
        String value = (safe(code) + " " + safe(name)).trim();
        return value.isEmpty() ? "(belum ditemukan)" : value;
    }

    private String recoverySourceSummary(ReferralRecoveryResult result) {
        java.util.List<String> sources = new java.util.ArrayList<>();
        if (result.foundFromLocal) sources.add("database lokal");
        if (result.foundFromBpjs) sources.add("BPJS VClaim");
        if (result.foundFromSatuSehat) sources.add("FHIR SATUSEHAT");
        return sources.isEmpty() ? "tidak ada data terverifikasi"
                : String.join(" + ", sources);
    }

    private boolean saveRecoveredReferral(ReferralRecoveryResult result) {
        if (result == null || safe(result.noRujukanBpjs).isEmpty()
                || !result.verifiedBpjsNumber) {
            showModernToast(this,
                    "No. RJK BPJS belum terverifikasi sehingga data belum dapat disimpan.",
                    ToastMessage.WARNING, 0);
            return false;
        }
        if (safe(noRawat).isEmpty() || safe(result.noSep).isEmpty()) {
            showModernToast(this,
                    "No. Rawat atau No. SEP belum lengkap.",
                    ToastMessage.ERROR, 0);
            return false;
        }

        setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.WAIT_CURSOR));
        setStatus("Menyimpan hasil pemulihan rujukan...", false);
        try {
            applyRecoveredReferralToForm(result);
            SisruteService.InsertRujukanRequest request =
                    buildRecoveredSnapshotRequest(result);
            saveFormSnapshot(request, result.noRujukanBpjs,
                    result.noRujukanSatuSehat, result.serviceRequestId);

            boolean legacySaved = upsertRecoveredLegacyData(request, result);
            noRujukanBPJS = safe(result.noRujukanBpjs);
            noRujukanSatusehat = safe(result.noRujukanSatuSehat);
            activeServiceRequestId = safe(result.serviceRequestId);
            referralPersisted = legacySaved;
            updateReferralActionState();

            if (legacySaved) {
                setStatus("Rujukan lama berhasil dipulihkan: "
                        + result.noRujukanBpjs, false);
            } else {
                setStatus("Snapshot rujukan pulih, tetapi tabel bridging lama "
                        + "perlu diperiksa.", true);
                showModernToast(this,
                        "Snapshot berhasil disimpan, tetapi sebagian tabel bridging "
                                + "lama belum dapat diperbarui. Periksa log database.",
                        ToastMessage.WARNING, 0);
            }
            return true;
        } catch (Exception ex) {
            System.out.println("Gagal menyimpan hasil pemulihan rujukan: " + ex);
            rememberApiException("Pemulihan - Simpan Lokal", ex);
            showModernToast(this,
                    "Data ditemukan tetapi gagal disimpan: "
                            + safe(ex.getMessage()),
                    ToastMessage.ERROR, 0);
            setStatus("Gagal menyimpan pemulihan: "
                    + safe(ex.getMessage()), true);
            return false;
        } finally {
            setCursor(java.awt.Cursor.getDefaultCursor());
        }
    }

    private void applyRecoveredReferralToForm(
            ReferralRecoveryResult result) {
        noSep = firstRecoveryValue(result.noSep, noSep);
        kodeFaskesSatuSehat = firstRecoveryValue(
                result.kodeFaskesAsalSatuSehat, kodeFaskesSatuSehat);
        idPasienSatuSehat = firstRecoveryValue(
                result.idPasienSatuSehat, idPasienSatuSehat);
        kdDokterSatuSehat = firstRecoveryValue(
                result.kdDokterSatuSehat, kdDokterSatuSehat);
        encounterRef = firstRecoveryValue(
                result.encounterReference, encounterRef);

        kdPenyakitRujuk = firstRecoveryValue(
                result.kodeDiagnosa, kdPenyakitRujuk);
        nmPenyakitRujuk = firstRecoveryValue(
                result.namaDiagnosa, nmPenyakitRujuk);
        kdPoliRujuk = firstRecoveryValue(result.kodePoli, kdPoliRujuk);
        nmPoliRujuk = firstRecoveryValue(result.namaPoli, nmPoliRujuk);

        selectedKdppkSatuSehatTujuan = safe(
                result.kdppkSatuSehatTujuan);
        selectedKdppkTujuan = safe(result.kodePpkTujuan);
        selectedNmppkTujuan = safe(result.namaFaskesTujuan);

        tNoSep.setText(noSep);
        tNoRujukanBpjs.setText(result.noRujukanBpjs);
        tNoRujukanSatuSehat.setText(result.noRujukanSatuSehat);
        tEncounter.setText(encounterRef);
        tIdPasienIhs.setText(idPasienSatuSehat);
        tIdDokterIhs.setText(kdDokterSatuSehat);
        tKdDiagnosaRujuk.setText(kdPenyakitRujuk);
        tNmDiagnosaRujuk.setText(nmPenyakitRujuk);
        tKdPoliRujuk.setText(kdPoliRujuk);
        tNmPoliRujuk.setText(nmPoliRujuk);

        selectComboRecoveryValue(cbJnsPelayanan,
                normalizeJenisPelayananRecovery(result.jenisPelayanan));
        selectComboRecoveryValue(cbTipeRujukan,
                normalizeTipeRujukanRecovery(result.tipeRujukan));
        restoreSnapshotDate(dtTglRujukan, result.tglRujukan);
        restoreSnapshotDate(dtTglRencana, result.tglRencanaKunjungan);

        if (!safe(result.catatan).isEmpty()) {
            taCatatan.setText(result.catatan);
        }
        if (!safe(result.keteranganRujukan).isEmpty()) {
            taKeterangan.setText(result.keteranganRujukan);
        }

        if (!result.criteriaRows.isEmpty()) {
            modelKriteria.setRowCount(0);
            for (Object[] row : result.criteriaRows) {
                modelKriteria.addRow(row);
            }
        }
        applyRecoveredFacilityRow(result);
        updateReferralActionState();
        javax.swing.SwingUtilities.invokeLater(() -> {
            adjustKriteriaQuestionColumnWidth();
            adjustKriteriaAnswerColumnWidth();
            adjustFaskesColumnWidths();
            updateDynamicTableHeights();
        });
    }

    private void selectComboRecoveryValue(javax.swing.JComboBox combo,
            String desiredValue) {
        if (combo == null || safe(desiredValue).isEmpty()) return;
        String desired = safe(desiredValue).toLowerCase(java.util.Locale.ROOT);
        for (int index = 0; index < combo.getItemCount(); index++) {
            Object item = combo.getItemAt(index);
            String text = item == null ? "" : item.toString().trim();
            if (text.toLowerCase(java.util.Locale.ROOT).equals(desired)
                    || text.startsWith(desired.substring(0, 1))) {
                combo.setSelectedIndex(index);
                return;
            }
        }
    }

    private void applyRecoveredFacilityRow(ReferralRecoveryResult result) {
        if (safe(result.kdppkSatuSehatTujuan).isEmpty()
                && safe(result.kodePpkTujuan).isEmpty()
                && safe(result.namaFaskesTujuan).isEmpty()) return;

        int selectedRow = -1;
        for (int row = 0; row < modelFaskes.getRowCount(); row++) {
            boolean sameSatuSehat = !safe(result.kdppkSatuSehatTujuan).isEmpty()
                    && safe(result.kdppkSatuSehatTujuan).equalsIgnoreCase(
                            tableValue(modelFaskes, row, 1));
            boolean samePpk = !safe(result.kodePpkTujuan).isEmpty()
                    && safe(result.kodePpkTujuan).equalsIgnoreCase(
                            tableValue(modelFaskes, row, 2));
            if (sameSatuSehat || samePpk) selectedRow = row;
            if (Boolean.TRUE.equals(modelFaskes.getValueAt(row, 0))) {
                modelFaskes.setValueAt(Boolean.FALSE, row, 0);
            }
        }

        if (selectedRow < 0) {
            modelFaskes.addRow(new Object[]{
                Boolean.FALSE,
                safe(result.kdppkSatuSehatTujuan),
                safe(result.kodePpkTujuan),
                safe(result.namaFaskesTujuan),
                "", "", "", "", "", ""
            });
            selectedRow = modelFaskes.getRowCount() - 1;
        } else {
            if (!safe(result.kdppkSatuSehatTujuan).isEmpty()) {
                modelFaskes.setValueAt(result.kdppkSatuSehatTujuan,
                        selectedRow, 1);
            }
            if (!safe(result.kodePpkTujuan).isEmpty()) {
                modelFaskes.setValueAt(result.kodePpkTujuan, selectedRow, 2);
            }
            if (!safe(result.namaFaskesTujuan).isEmpty()) {
                modelFaskes.setValueAt(result.namaFaskesTujuan, selectedRow, 3);
            }
        }
        modelFaskes.setValueAt(Boolean.TRUE, selectedRow, 0);
        result.kdppkSatuSehatTujuan = firstRecoveryValue(
                result.kdppkSatuSehatTujuan,
                tableValue(modelFaskes, selectedRow, 1));
        result.kodePpkTujuan = firstRecoveryValue(
                result.kodePpkTujuan,
                tableValue(modelFaskes, selectedRow, 2));
        result.namaFaskesTujuan = firstRecoveryValue(
                result.namaFaskesTujuan,
                tableValue(modelFaskes, selectedRow, 3));
        selectedKdppkSatuSehatTujuan = result.kdppkSatuSehatTujuan;
        selectedKdppkTujuan = result.kodePpkTujuan;
        selectedNmppkTujuan = result.namaFaskesTujuan;
        int viewRow = tblFaskes.convertRowIndexToView(selectedRow);
        if (viewRow >= 0) tblFaskes.setRowSelectionInterval(viewRow, viewRow);
    }

    /** Melengkapi kode/nama tujuan dari daftar faskes yang sudah ada di form. */
    private void enrichRecoveryFromLoadedFaskes(
            ReferralRecoveryResult result) {
        if (result == null) return;
        for (int row = 0; row < modelFaskes.getRowCount(); row++) {
            boolean sameSatuSehat = !safe(result.kdppkSatuSehatTujuan).isEmpty()
                    && safe(result.kdppkSatuSehatTujuan).equalsIgnoreCase(
                            tableValue(modelFaskes, row, 1));
            boolean samePpk = !safe(result.kodePpkTujuan).isEmpty()
                    && safe(result.kodePpkTujuan).equalsIgnoreCase(
                            tableValue(modelFaskes, row, 2));
            if (!sameSatuSehat && !samePpk) continue;
            result.kdppkSatuSehatTujuan = firstRecoveryValue(
                    result.kdppkSatuSehatTujuan,
                    tableValue(modelFaskes, row, 1));
            result.kodePpkTujuan = firstRecoveryValue(
                    result.kodePpkTujuan,
                    tableValue(modelFaskes, row, 2));
            result.namaFaskesTujuan = firstRecoveryValue(
                    result.namaFaskesTujuan,
                    tableValue(modelFaskes, row, 3));
            return;
        }
    }

    private SisruteService.InsertRujukanRequest buildRecoveredSnapshotRequest(
            ReferralRecoveryResult result) {
        SisruteService.InsertRujukanRequest request =
                new SisruteService.InsertRujukanRequest();
        request.noSep = safe(result.noSep);
        request.tglRujukan = firstRecoveryValue(
                result.tglRujukan,
                Valid.SetTgl(dtTglRujukan.getSelectedItem() + ""));
        request.tglRencanaKunjungan = firstRecoveryValue(
                result.tglRencanaKunjungan,
                Valid.SetTgl(dtTglRencana.getSelectedItem() + ""),
                request.tglRujukan);
        request.ppkDirujuk = safe(result.kodePpkTujuan);
        request.jnsPelayanan = firstRecoveryValue(
                normalizeJenisPelayananRecovery(result.jenisPelayanan), "2");
        request.catatan = firstRecoveryValue(
                result.catatan, taCatatan.getText());
        request.diagRujukan = firstRecoveryValue(
                result.kodeDiagnosa, kdPenyakitRujuk);
        request.tipeRujukan = normalizeTipeRujukanRecovery(
                result.tipeRujukan).substring(0, 1);
        request.poliRujukan = firstRecoveryValue(
                result.kodePoli, kdPoliRujuk);
        request.user = user;
        request.kodeFaskesSatuSehat = firstRecoveryValue(
                result.kodeFaskesAsalSatuSehat, kodeFaskesSatuSehat);
        request.idPasienSatuSehat = firstRecoveryValue(
                result.idPasienSatuSehat, idPasienSatuSehat);
        request.kdppkSatuSehatTujuanRujukan =
                safe(result.kdppkSatuSehatTujuan);
        request.kdDokterSatuSehat = firstRecoveryValue(
                result.kdDokterSatuSehat, kdDokterSatuSehat);
        request.encounterReference = firstRecoveryValue(
                result.encounterReference, encounterRef);
        request.patientInstruction = firstRecoveryValue(
                result.patientInstruction,
                safe(result.namaFaskesTujuan).isEmpty() ? ""
                        : "Rujukan ke " + result.namaFaskesTujuan);
        request.kriteriaJsonItem = "[]";
        request.keteranganRujukan = firstRecoveryValue(
                result.keteranganRujukan, taKeterangan.getText(),
                request.patientInstruction);
        int provinceIndex = cbProvinsi.getSelectedIndex();
        if (provinceIndex >= 0 && provinceIndex < PROVINSI.length) {
            request.kodePropinsi = PROVINSI[provinceIndex][0];
            request.namaPropinsi = PROVINSI[provinceIndex][1];
        } else {
            request.kodePropinsi = "";
            request.namaPropinsi = "";
        }
        request.kodeKabupaten = "";
        request.namaKabupaten = "";
        return request;
    }

    private boolean upsertRecoveredLegacyData(
            SisruteService.InsertRujukanRequest request,
            ReferralRecoveryResult result) {
        boolean success = true;
        String bpjsSql = "insert into bridging_rujukan_bpjs "
                + "(no_sep,tglRujukan,tglRencanaKunjungan,ppkDirujuk,"
                + "nm_ppkDirujuk,jnsPelayanan,catatan,diagRujukan,"
                + "nama_diagRujukan,tipeRujukan,poliRujukan,"
                + "nama_poliRujukan,no_rujukan,`user`) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update no_sep=values(no_sep),"
                + "tglRujukan=values(tglRujukan),"
                + "tglRencanaKunjungan=values(tglRencanaKunjungan),"
                + "ppkDirujuk=values(ppkDirujuk),"
                + "nm_ppkDirujuk=values(nm_ppkDirujuk),"
                + "jnsPelayanan=values(jnsPelayanan),catatan=values(catatan),"
                + "diagRujukan=values(diagRujukan),"
                + "nama_diagRujukan=values(nama_diagRujukan),"
                + "tipeRujukan=values(tipeRujukan),"
                + "poliRujukan=values(poliRujukan),"
                + "nama_poliRujukan=values(nama_poliRujukan),"
                + "`user`=values(`user`)";
        try (PreparedStatement statement = koneksi.prepareStatement(bpjsSql)) {
            int index = 1;
            statement.setString(index++, request.noSep);
            statement.setString(index++, request.tglRujukan);
            statement.setString(index++, request.tglRencanaKunjungan);
            statement.setString(index++, request.ppkDirujuk);
            statement.setString(index++, safe(result.namaFaskesTujuan));
            statement.setString(index++, request.jnsPelayanan);
            statement.setString(index++, request.catatan);
            statement.setString(index++, request.diagRujukan);
            statement.setString(index++, firstRecoveryValue(
                    result.namaDiagnosa, nmPenyakitRujuk));
            statement.setString(index++, normalizeTipeRujukanRecovery(
                    result.tipeRujukan));
            statement.setString(index++, request.poliRujukan);
            statement.setString(index++, firstRecoveryValue(
                    result.namaPoli, nmPoliRujuk));
            statement.setString(index++, result.noRujukanBpjs);
            statement.setString(index, user);
            statement.executeUpdate();
        } catch (Exception ex) {
            success = false;
            System.out.println("Gagal upsert rujukan BPJS hasil pemulihan: " + ex);
        }

        if (!safe(result.noRujukanSatuSehat).isEmpty()) {
            String satuSehatSql = "insert into bridging_rujukan_satusehat "
                    + "(no_rujukan,no_rujukan_satusehat,service_request_id,"
                    + "kode_faskes_satusehat,id_pasien_satusehat,"
                    + "kdppk_satusehat_tujuan,nama_faskes_tujuan,"
                    + "kd_dokter_satusehat,encounter_reference,"
                    + "patient_instruction,keterangan_rujukan,kode_propinsi,"
                    + "nama_propinsi,kode_kabupaten,nama_kabupaten,"
                    + "kode_poli_rujuk) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                    + "on duplicate key update "
                    + "no_rujukan_satusehat=values(no_rujukan_satusehat),"
                    + "service_request_id=values(service_request_id),"
                    + "kode_faskes_satusehat=values(kode_faskes_satusehat),"
                    + "id_pasien_satusehat=values(id_pasien_satusehat),"
                    + "kdppk_satusehat_tujuan=values(kdppk_satusehat_tujuan),"
                    + "nama_faskes_tujuan=values(nama_faskes_tujuan),"
                    + "kd_dokter_satusehat=values(kd_dokter_satusehat),"
                    + "encounter_reference=values(encounter_reference),"
                    + "patient_instruction=values(patient_instruction),"
                    + "keterangan_rujukan=values(keterangan_rujukan),"
                    + "kode_propinsi=values(kode_propinsi),"
                    + "nama_propinsi=values(nama_propinsi),"
                    + "kode_kabupaten=values(kode_kabupaten),"
                    + "nama_kabupaten=values(nama_kabupaten),"
                    + "kode_poli_rujuk=values(kode_poli_rujuk)";
            try (PreparedStatement statement =
                    koneksi.prepareStatement(satuSehatSql)) {
                int index = 1;
                statement.setString(index++, result.noRujukanBpjs);
                statement.setString(index++, result.noRujukanSatuSehat);
                statement.setString(index++, result.serviceRequestId);
                statement.setString(index++, request.kodeFaskesSatuSehat);
                statement.setString(index++, request.idPasienSatuSehat);
                statement.setString(index++, request.kdppkSatuSehatTujuanRujukan);
                statement.setString(index++, safe(result.namaFaskesTujuan));
                statement.setString(index++, request.kdDokterSatuSehat);
                statement.setString(index++, request.encounterReference);
                statement.setString(index++, request.patientInstruction);
                statement.setString(index++, request.keteranganRujukan);
                statement.setString(index++, request.kodePropinsi);
                statement.setString(index++, request.namaPropinsi);
                statement.setString(index++, request.kodeKabupaten);
                statement.setString(index++, request.namaKabupaten);
                statement.setString(index, request.poliRujukan);
                statement.executeUpdate();
            } catch (Exception ex) {
                success = false;
                System.out.println("Gagal upsert rujukan SATUSEHAT hasil pemulihan: " + ex);
            }
        }
        return success;
    }

    // =================================================================
    //  ACTION 4: HAPUS RUJUKAN
    //  DELETE /Rujukan/Delete
    // =================================================================
    private void doHapusRujukan() {
        String noRujukan = findActiveReferralNumber();
        if (safe(noRujukan).isEmpty()) {
            showModernToast(this,
                    "Tidak ada rujukan tersimpan untuk SEP " + noSep + ".",
                    TOAST_INFO, 0);
            return;
        }

        // Payload memakai snapshot sebagai sumber utama dan tabel lama sebagai
        // fallback, sehingga bentuk request DELETE tetap sesuai kontrak API.
        String[] deletePayload = loadDeleteReferralPayload(noRujukan);
        String missingDeleteData = validateDeleteReferralPayload(deletePayload);
        if (!missingDeleteData.isEmpty()) {
            showModernToast(this,
                    "Rujukan belum dapat dihapus karena data payload berikut kosong: "
                            + missingDeleteData + ".",
                    ToastMessage.ERROR, 0);
            setStatus("Payload hapus rujukan belum lengkap: "
                    + missingDeleteData, true);
            return;
        }
        if (!showDeleteReferralConfirmation(noRujukan)) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Menghapus rujukan...", false);
        try {
            JsonNode resp = sisrute.deleteRujukan(
                    noRujukan, user,
                    deletePayload[0], deletePayload[1],
                    deletePayload[2], deletePayload[3],
                    deletePayload[4], deletePayload[5], deletePayload[6]);

            rememberApiResponse("BPJS/SISRUTE - Hapus Rujukan", resp);

            if (!SisruteService.isOk(resp)) {
                showModernToast(this,
                        "Gagal menghapus rujukan:\n"
                                + safe(SisruteService.getMessage(resp)),
                        ToastMessage.ERROR, 0);
                setStatus("Gagal: " + SisruteService.getMessage(resp), true);
                return;
            }

            // Data lokal baru dibersihkan setelah API memastikan penghapusan sukses.
            boolean localDeleted = deleteLocalReferralData(noRujukan);
            resetReferralAfterDelete();

            if (localDeleted) {
                showModernToast(this,
                        "Rujukan " + noRujukan + " berhasil dihapus. "
                                + "Silakan pilih faskes tujuan lain.",
                        ToastMessage.SUCCESS, 0);
                setStatus("Berhasil hapus rujukan " + noRujukan
                        + ". Pilih faskes lain untuk mengirim ulang.", false);
            } else {
                showModernToast(this,
                        "Rujukan berhasil dihapus dari BPJS/SATUSEHAT, tetapi "
                                + "sebagian pembersihan data lokal gagal. Periksa log DB.",
                        ToastMessage.WARNING, 0);
                setStatus("Rujukan terhapus dari API; pembersihan lokal perlu diperiksa.",
                        true);
            }

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String findActiveReferralNumber() {
        String number = safe(tNoRujukanBpjs.getText());
        if (!number.isEmpty()) return number;

        try (PreparedStatement statement = koneksi.prepareStatement(
                "SELECT no_rujukan_bpjs FROM " + SNAPSHOT_TABLE
                        + " WHERE no_rawat=? LIMIT 1")) {
            statement.setString(1, noRawat);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) number = safe(result.getString(1));
            }
        } catch (Exception ex) {
            System.out.println("Nomor rujukan tidak ditemukan di snapshot: " + ex);
        }

        if (number.isEmpty()) {
            number = safe(Sequel.cariIsi(
                    "select b.no_rujukan from bridging_rujukan_bpjs b "
                            + "where b.no_sep=? order by b.tglRujukan desc limit 1",
                    noSep));
        }
        return number;
    }

    /**
     * Urutan payload: kode faskes asal, IHS pasien, kode tujuan SATUSEHAT,
     * IHS dokter, encounter, instruksi pasien, dan keterangan rujukan.
     */
    private String[] loadDeleteReferralPayload(String noRujukan) {
        String[] payload = {
            safe(kodeFaskesSatuSehat), safe(idPasienSatuSehat),
            safe(selectedKdppkSatuSehatTujuan), safe(kdDokterSatuSehat),
            safe(encounterRef), safe(selectedNmppkTujuan).isEmpty()
                    ? "" : "Rujukan ke " + safe(selectedNmppkTujuan),
            safe(taKeterangan.getText())
        };

        String snapshotSql = "SELECT kode_faskes_satusehat,id_pasien_satusehat,"
                + "kdppk_satusehat_tujuan,kd_dokter_satusehat,"
                + "encounter_reference,patient_instruction,keterangan_rujukan "
                + "FROM " + SNAPSHOT_TABLE
                + " WHERE no_rawat=? OR no_rujukan_bpjs=? LIMIT 1";
        try (PreparedStatement statement = koneksi.prepareStatement(snapshotSql)) {
            statement.setString(1, noRawat);
            statement.setString(2, noRujukan);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    for (int index = 0; index < payload.length; index++) {
                        String value = safe(result.getString(index + 1));
                        if (!value.isEmpty()) payload[index] = value;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Gagal ambil payload hapus dari snapshot: " + ex);
        }

        // Kompatibilitas untuk rujukan lama yang dibuat sebelum tabel snapshot.
        try (PreparedStatement statement = koneksi.prepareStatement(
                "select kdppk_satusehat_tujuan,patient_instruction,"
                        + "keterangan_rujukan from bridging_rujukan_satusehat "
                        + "where no_rujukan=?")) {
            statement.setString(1, noRujukan);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String tujuan = safe(result.getString(1));
                    String instruction = safe(result.getString(2));
                    String description = safe(result.getString(3));
                    if (payload[2].isEmpty()) payload[2] = tujuan;
                    if (payload[5].isEmpty()) payload[5] = instruction;
                    if (payload[6].isEmpty()) payload[6] = description;
                }
            }
        } catch (Exception ex) {
            System.out.println("Gagal ambil payload hapus dari tabel lama: " + ex);
        }

        if (payload[5].isEmpty()) {
            payload[5] = "Rujukan ke " + safe(selectedNmppkTujuan);
        }
        if (payload[6].isEmpty()) payload[6] = payload[5];
        return payload;
    }

    private String validateDeleteReferralPayload(String[] payload) {
        String[] labels = {
            "Kode Faskes SATUSEHAT asal", "IHS Pasien",
            "Kode Faskes SATUSEHAT tujuan", "IHS Dokter", "Encounter"
        };
        StringBuilder missing = new StringBuilder();
        for (int index = 0; index < labels.length; index++) {
            if (payload == null || payload.length <= index
                    || safe(payload[index]).isEmpty()) {
                if (missing.length() > 0) missing.append(", ");
                missing.append(labels[index]);
            }
        }
        return missing.toString();
    }

    private boolean deleteLocalReferralData(String noRujukan) {
        boolean success = true;
        success &= executeLocalDelete(
                "delete from bridging_kriteria_rujukan_satusehat "
                        + "where no_rujukan=?", noRujukan, null);
        success &= executeLocalDelete(
                "delete from bridging_rujukan_satusehat where no_rujukan=?",
                noRujukan, null);
        success &= executeLocalDelete(
                "delete from bridging_rujukan_bpjs where no_rujukan=?",
                noRujukan, null);
        success &= executeLocalDelete(
                "delete from " + SNAPSHOT_TABLE
                        + " where no_rawat=? or no_rujukan_bpjs=?",
                noRawat, noRujukan);
        return success;
    }

    private boolean executeLocalDelete(String sql, String first, String second) {
        try (PreparedStatement statement = koneksi.prepareStatement(sql)) {
            statement.setString(1, safe(first));
            if (second != null) statement.setString(2, safe(second));
            statement.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.out.println("Gagal membersihkan data rujukan lokal: " + ex);
            return false;
        }
    }

    private void resetReferralAfterDelete() {
        if (tblKriteria != null && tblKriteria.isEditing()
                && tblKriteria.getCellEditor() != null) {
            tblKriteria.getCellEditor().cancelCellEditing();
        }
        if (tblFaskes != null && tblFaskes.isEditing()
                && tblFaskes.getCellEditor() != null) {
            tblFaskes.getCellEditor().cancelCellEditing();
        }

        tNoRujukanBpjs.setText("");
        tNoRujukanSatuSehat.setText("");
        noRujukanBPJS = "";
        noRujukanSatusehat = "";
        activeServiceRequestId = "";
        referralPersisted = false;

        restoringReferralSnapshot = true;
        syncingCriteriaAnswers = true;
        try {
            modelKriteria.setRowCount(0);
            modelFaskes.setRowCount(0);
            if (txtCariFaskesUi != null) txtCariFaskesUi.setText("");
            if (sorterFaskesUi != null) sorterFaskesUi.setRowFilter(null);
        } finally {
            syncingCriteriaAnswers = false;
            restoringReferralSnapshot = false;
        }
        if (chkSinkronKriteriaUi != null) {
            chkSinkronKriteriaUi.setSelected(true);
        }
        selectedKdppkSatuSehatTujuan = "";
        selectedKdppkTujuan = "";
        selectedNmppkTujuan = "";
        tblKriteria.clearSelection();
        tblFaskes.clearSelection();
        updateDynamicTableHeights();
        updateReferralActionState();
    }

    /**
     * Dialog konfirmasi berlapis: tombol hapus baru aktif setelah pengguna
     * mengetik tepat empat karakter terakhir nomor rujukan.
     */
    private boolean showDeleteReferralConfirmation(final String noRujukan) {
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        final boolean[] confirmed = {false};
        final String token = noRujukan.length() <= 4
                ? noRujukan : noRujukan.substring(noRujukan.length() - 4);

        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setTitle("Konfirmasi Hapus Rujukan");
        dialog.setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        configureRoundedPopupWindow(dialog, true);

        java.awt.Color page = new java.awt.Color(248, 250, 252);
        java.awt.Color line = new java.awt.Color(226, 232, 240);
        java.awt.Color danger = new java.awt.Color(220, 38, 38);

        javax.swing.JPanel root = new javax.swing.JPanel(
                new java.awt.BorderLayout());
        root.setBackground(page);
        root.setBorder(new RoundedPopupBorder(
                new java.awt.Color(203, 213, 225)));

        javax.swing.JPanel header = new javax.swing.JPanel(
                new java.awt.BorderLayout(12, 0));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, line),
                javax.swing.BorderFactory.createEmptyBorder(12, 20, 12, 12)));

        javax.swing.JPanel titleBox = new javax.swing.JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new javax.swing.BoxLayout(
                titleBox, javax.swing.BoxLayout.Y_AXIS));
        javax.swing.JLabel title = new javax.swing.JLabel("Hapus Rujukan Aktif");
        title.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 16));
        title.setForeground(new java.awt.Color(30, 41, 59));
        javax.swing.JLabel subtitle = new javax.swing.JLabel(
                "Tindakan ini juga menghapus seluruh snapshot form lokal.");
        subtitle.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        titleBox.add(title);
        titleBox.add(javax.swing.Box.createVerticalStrut(3));
        titleBox.add(subtitle);

        javax.swing.JButton close = createFlatCloseButton();
        close.addActionListener(e -> dialog.dispose());
        header.add(titleBox, java.awt.BorderLayout.CENTER);
        header.add(close, java.awt.BorderLayout.EAST);

        javax.swing.JPanel body = new javax.swing.JPanel();
        body.setBackground(page);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));

        javax.swing.JPanel warning = new EncounterRoundedPanel(
                new java.awt.BorderLayout(12, 0),
                new java.awt.Color(254, 242, 242),
                new java.awt.Color(254, 202, 202), 10);
        warning.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        warning.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 82));
        warning.setPreferredSize(new java.awt.Dimension(100, 82));
        warning.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                12, 14, 12, 14));
        javax.swing.JLabel warningIcon = new javax.swing.JLabel("!");
        warningIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        warningIcon.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.BOLD, 18));
        warningIcon.setForeground(danger);
        warningIcon.setPreferredSize(new java.awt.Dimension(28, 28));
        javax.swing.JLabel warningText = new javax.swing.JLabel(
                "<html><b>Pastikan RS tujuan memang menolak rujukan.</b><br>"
                        + "Penghapusan API tidak dapat dibatalkan dari form ini.</html>");
        warningText.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        warningText.setForeground(new java.awt.Color(127, 29, 29));
        warning.add(warningIcon, java.awt.BorderLayout.WEST);
        warning.add(warningText, java.awt.BorderLayout.CENTER);
        body.add(warning);
        body.add(javax.swing.Box.createVerticalStrut(14));

        javax.swing.JLabel numberLabel = new javax.swing.JLabel(
                "No. RJK BPJS:  " + noRujukan);
        numberLabel.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        numberLabel.setForeground(new java.awt.Color(30, 41, 59));
        numberLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(numberLabel);
        body.add(javax.swing.Box.createVerticalStrut(5));

        javax.swing.JLabel destinationLabel = new javax.swing.JLabel(
                "Faskes tujuan:  " + (safe(selectedNmppkTujuan).isEmpty()
                        ? "-" : selectedNmppkTujuan));
        destinationLabel.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        destinationLabel.setForeground(new java.awt.Color(71, 85, 105));
        destinationLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(destinationLabel);
        body.add(javax.swing.Box.createVerticalStrut(16));

        javax.swing.JLabel instruction = new javax.swing.JLabel(
                "Ketik 4 karakter terakhir nomor rujukan (" + token
                        + ") untuk mengaktifkan tombol hapus:");
        instruction.setFont(new java.awt.Font(
                "Segoe UI", java.awt.Font.PLAIN, 11));
        instruction.setForeground(new java.awt.Color(51, 65, 85));
        instruction.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(instruction);
        body.add(javax.swing.Box.createVerticalStrut(6));

        javax.swing.JTextField confirmation = new javax.swing.JTextField();
        confirmation.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        confirmation.setForeground(new java.awt.Color(30, 41, 59));
        confirmation.setBackground(java.awt.Color.WHITE);
        confirmation.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(
                        new java.awt.Color(203, 213, 225)),
                javax.swing.BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        confirmation.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
        confirmation.setPreferredSize(new java.awt.Dimension(100, 32));
        confirmation.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        body.add(confirmation);

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 11));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, line));

        javax.swing.JButton cancel = new EncounterActionButton("Batal", false);
        styleEncounterDialogButton(cancel, false);
        cancel.setPreferredSize(new java.awt.Dimension(96, 34));
        cancel.addActionListener(e -> dialog.dispose());

        javax.swing.JButton delete = new DangerActionButton("Hapus Rujukan");
        delete.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        delete.setFont(new java.awt.Font(
                "Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        delete.setForeground(java.awt.Color.WHITE);
        delete.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                6, 14, 6, 14));
        delete.setBorderPainted(false);
        delete.setContentAreaFilled(false);
        delete.setFocusPainted(false);
        delete.setOpaque(false);
        delete.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.HAND_CURSOR));
        delete.setPreferredSize(new java.awt.Dimension(132, 34));
        delete.setEnabled(false);
        delete.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        confirmation.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            private void validateToken() {
                delete.setEnabled(token.equalsIgnoreCase(
                        confirmation.getText().trim()));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateToken();
            }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateToken();
            }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateToken();
            }
        });

        footer.add(cancel);
        footer.add(delete);
        root.add(header, java.awt.BorderLayout.NORTH);
        root.add(body, java.awt.BorderLayout.CENTER);
        root.add(footer, java.awt.BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(delete);
        dialog.getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        javax.swing.KeyStroke.getKeyStroke(
                                java.awt.event.KeyEvent.VK_ESCAPE, 0),
                        "cancel-delete-referral");
        dialog.getRootPane().getActionMap().put(
                "cancel-delete-referral", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        installDialogDragSupport(header, dialog);
        dialog.setSize(570, 410);
        dialog.setLocationRelativeTo(this);
        javax.swing.SwingUtilities.invokeLater(
                confirmation::requestFocusInWindow);
        showModalDialogWithBlur(dialog);
        return confirmed[0];
    }

    // =================================================================
    //  HELPERS
    // =================================================================

    private boolean validateBeforeApi() {
        if (kosong(tNoSep,
        "Belum ada data SEP ")) return false;

        if (kosong(tNoRawat,
                "PILIH DULU DATA PASIEN.")) return false;

        if (kosong(tEncounter,
                "Encounter Satu Sehat belum ada untuk no_rawat ini. Silakan kirim Encounter dulu.")) return false;

        if (kosong(tIdPasienIhs,
                "IHS Pasien tidak ditemukan. Pastikan pasien sudah punya IHS di Satu Sehat.")) return false;

        if (kosong(tIdDokterIhs,
                "IHS Dokter tidak ditemukan. Pastikan dokter sudah punya IHS di Satu Sehat.")) return false;

        if (kosong(tNmDiagnosaRujuk,
                "Diagnosa Rujuk belum dipilih. Klik tombol [Pilih] di sebelah Diagnosa Rujuk.")) return false;

        if (kosong(tNmPoliRujuk,
                "Poli Rujuk belum dipilih. Klik tombol [Pilih] di sebelah Poli Rujuk.")) return false;
//        if (noSep.isEmpty() || noRawat.isEmpty()|| tNoSep.getText().equals("")) {
//            JOptionPane.showMessageDialog(this,
//                    "Data SEP belum di-set. Tutup form dan pilih SEP terlebih dahulu.",
//                    "Info", JOptionPane.WARNING_MESSAGE);
//            return false;
//        }
       return true;
    }
// ganti validasi 
    
    private boolean kosong(JTextField field, String pesan) {
    if (field.getText().trim().isEmpty()) {
        showModernToast(
                this,
                pesan,
                ToastMessage.WARNING,
                0
        );
        field.requestFocus();
        return true;
    }
    return false;
}
    
    private void handleApiError(Exception ex) {
        System.out.println("API error: " + ex);
        ex.printStackTrace();
        rememberApiException("API BPJS/SATUSEHAT", ex);
        String msg = ex.toString();
        if (msg.contains("UnknownHostException")) {
            showModernToast(this,
                    "Koneksi ke server BPJS Sisrute terputus.",
                    ToastMessage.ERROR, 0);
        } else {
            showModernToast(this,
                    "Error: " + msg,
                    ToastMessage.ERROR, 0);
        }
        setStatus("Error: " + msg, true);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    /** Untuk standalone test. */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            BPJSRujukanSatuSehat dlg = new BPJSRujukanSatuSehat(new javax.swing.JFrame(), true);
            // Contoh data dummy untuk tes UI
            dlg.setDataSEP(
                    "1001R0120126V000010", "2026/03/03/000123", "000123", "Budi Santoso",
                    "I10", "Essential (primary) hypertension",
                    "INT", "Penyakit Dalam",
                    "DR001"
            );
            dlg.setVisible(true);
        });
    }
    
private static void showIcd9SearchPopup(java.awt.Component anchor,
        java.util.function.Consumer<String> onSelected) {
    List<Icd9Item> initialItems = fetchIcd9List(null);
    if (initialItems.isEmpty()) {
        showModernToast(anchor,
                "Data ICD-9 kosong atau koneksi gagal.",
                ToastMessage.WARNING, 0);
        return;
    }

    final Color lineColor = new Color(203, 213, 225);
    final Color textColor = new Color(30, 41, 59);

    JPopupMenu popup = new JPopupMenu();
    popup.setLightWeightPopupEnabled(false);
    popup.setBorder(BorderFactory.createLineBorder(lineColor));
    popup.setFocusable(true);

    JPanel content = new JPanel(new BorderLayout());
    content.setBackground(Color.WHITE);

    JTextField searchField = new JTextField();
    searchField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    searchField.setForeground(textColor);
    searchField.setBackground(Color.WHITE);
    searchField.setCaretColor(textColor);
    searchField.setToolTipText("Ketik kode atau nama tindakan ICD-9");
    searchField.setPreferredSize(new Dimension(10, 30));
    searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(147, 197, 253)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.setBackground(Color.WHITE);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    searchPanel.add(searchField, BorderLayout.CENTER);
    content.add(searchPanel, BorderLayout.NORTH);

    DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "Tindakan Medis"}, 0) {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    JTable table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    table.setRowHeight(28);
    table.setBackground(Color.WHITE);
    table.setForeground(textColor);
    table.setSelectionBackground(new Color(14, 116, 190));
    table.setSelectionForeground(Color.WHITE);
    table.setGridColor(new Color(226, 232, 240));
    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(true);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setFillsViewportHeight(true);

    JTableHeader header = table.getTableHeader();
    header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    header.setBackground(new Color(239, 246, 255));
    header.setForeground(new Color(30, 64, 175));
    header.setPreferredSize(new Dimension(10, 28));
    header.setReorderingAllowed(false);

    DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
    headerRenderer.setOpaque(true);
    headerRenderer.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    headerRenderer.setBackground(new Color(239, 246, 255));
    headerRenderer.setForeground(new Color(30, 64, 175));
    headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
    headerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
    table.getColumnModel().getColumn(0).setHeaderRenderer(headerRenderer);
    table.getColumnModel().getColumn(1).setHeaderRenderer(headerRenderer);

    int codeWidth = table.getFontMetrics(table.getFont())
            .stringWidth("00000000") + 20;
    TableColumn codeColumn = table.getColumnModel().getColumn(0);
    codeColumn.setMinWidth(codeWidth);
    codeColumn.setPreferredWidth(codeWidth);
    codeColumn.setMaxWidth(codeWidth);

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor));
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.getVerticalScrollBar().setUnitIncrement(table.getRowHeight());
    content.add(scroll, BorderLayout.CENTER);

    List<Icd9Item> displayedItems = new ArrayList<>();
    java.util.function.Consumer<List<Icd9Item>> showItems = items -> {
        displayedItems.clear();
        displayedItems.addAll(items);
        model.setRowCount(0);
        for (Icd9Item item : displayedItems) {
            model.addRow(new Object[]{item.getCode(), item.getName()});
        }
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            table.scrollRectToVisible(table.getCellRect(0, 0, true));
        }
    };
    showItems.accept(initialItems);

    java.util.function.IntConsumer chooseRow = viewRow -> {
        if (viewRow < 0 || viewRow >= table.getRowCount()) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        String selectedCode = String.valueOf(model.getValueAt(modelRow, 0)).trim();
        if (selectedCode.isEmpty()) return;

        popup.setVisible(false);
        if (onSelected != null) onSelected.accept(selectedCode);
    };

    table.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            int viewRow = table.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            table.setRowSelectionInterval(viewRow, viewRow);
            chooseRow.accept(viewRow);
        }
    });

    javax.swing.Action moveDown = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            int last = table.getRowCount() - 1;
            if (last < 0) return;
            int current = table.getSelectedRow();
            int target = current < 0 ? 0 : Math.min(last, current + 1);
            table.setRowSelectionInterval(target, target);
            table.scrollRectToVisible(table.getCellRect(target, 0, true));
        }
    };
    javax.swing.Action moveUp = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            if (table.getRowCount() == 0) return;
            int current = table.getSelectedRow();
            int target = current <= 0 ? 0 : current - 1;
            table.setRowSelectionInterval(target, target);
            table.scrollRectToVisible(table.getCellRect(target, 0, true));
        }
    };
    javax.swing.Action chooseCurrent = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            chooseRow.accept(table.getSelectedRow());
        }
    };
    javax.swing.Action closePopup = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            popup.setVisible(false);
        }
    };

    searchField.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "icd9-next");
    searchField.getActionMap().put("icd9-next", moveDown);
    searchField.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "icd9-previous");
    searchField.getActionMap().put("icd9-previous", moveUp);
    searchField.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "icd9-choose");
    searchField.getActionMap().put("icd9-choose", chooseCurrent);
    searchField.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "icd9-close");
    searchField.getActionMap().put("icd9-close", closePopup);

    table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "icd9-choose");
    table.getActionMap().put("icd9-choose", chooseCurrent);
    table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "icd9-close");
    table.getActionMap().put("icd9-close", closePopup);

    javax.swing.Timer searchTimer = new javax.swing.Timer(300, e -> {
        String keyword = searchField.getText().trim();
        List<Icd9Item> results = keyword.isEmpty()
                ? initialItems : fetchIcd9List(keyword);
        showItems.accept(results);
    });
    searchTimer.setRepeats(false);
    searchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
        private void changed() { searchTimer.restart(); }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
            changed();
        }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
            changed();
        }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {
            changed();
        }
    });
    popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
        @Override public void popupMenuWillBecomeVisible(
                javax.swing.event.PopupMenuEvent e) {}
        @Override public void popupMenuWillBecomeInvisible(
                javax.swing.event.PopupMenuEvent e) {
            searchTimer.stop();
        }
        @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            searchTimer.stop();
        }
    });

    int visibleRows = Math.max(4, Math.min(7, initialItems.size()));
    int popupHeight = 39 + header.getPreferredSize().height
            + (visibleRows * table.getRowHeight()) + 2;
    content.setPreferredSize(new Dimension(
            calculateIcd9PopupWidth(anchor), popupHeight));
    popup.add(content);
    popup.show(anchor, 0, anchor.getHeight() + 2);

    SwingUtilities.invokeLater(() -> {
        searchField.requestFocusInWindow();
        searchField.selectAll();
    });
}

private static int calculateIcd9PopupWidth(Component anchor) {
    int fallbackWidth = 520;
    if (anchor == null || !anchor.isShowing()) return fallbackWidth;

    try {
        Point anchorPoint = anchor.getLocationOnScreen();
        int rightEdge = anchorPoint.x + fallbackWidth;
        Window ownerWindow = SwingUtilities.getWindowAncestor(anchor);
        if (ownerWindow instanceof BPJSRujukanSatuSehat) {
            widget.InternalFrame criteriaCard =
                    ((BPJSRujukanSatuSehat) ownerWindow).frameKriteria;
            if (criteriaCard != null && criteriaCard.isShowing()) {
                Point cardPoint = criteriaCard.getLocationOnScreen();
                rightEdge = cardPoint.x + criteriaCard.getWidth() - 2;
            }
        }

        GraphicsConfiguration configuration = anchor.getGraphicsConfiguration();
        if (configuration != null) {
            Rectangle screenBounds = configuration.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit()
                    .getScreenInsets(configuration);
            int screenRight = screenBounds.x + screenBounds.width
                    - screenInsets.right - 4;
            rightEdge = Math.min(rightEdge, screenRight);
        }
        return Math.max(340, rightEdge - anchorPoint.x);
    } catch (Exception ignored) {
        return fallbackWidth;
    }
}

  private static String showIcd9SearchDialog(java.awt.Component parent, String existingCodes) {
    List<Icd9Item> listFull = fetchIcd9List(null);
    if (listFull.isEmpty()) {
        showModernToast(parent,
                "Data ICD-9 kosong atau koneksi gagal.",
                ToastMessage.WARNING, 0);
        return null;
    }

    final Color pageColor = new Color(248, 250, 252);
    final Color lineColor = new Color(226, 232, 240);
    final Color textColor = new Color(30, 41, 59);
    final Color mutedColor = new Color(100, 116, 139);

    List<Icd9Item> listDisplay = new ArrayList<>(listFull);
    Set<String> existingSet = new LinkedHashSet<>();
    if (existingCodes != null && !existingCodes.trim().isEmpty()) {
        for (String code : existingCodes.split(";")) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) existingSet.add(trimmed);
        }
    }

    JDialog dialog = new JDialog((Frame) null,
            "Pilih Tindakan Medis (ICD-9)", true);
    dialog.setUndecorated(true);
    configureRoundedPopupWindow(dialog, true);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(pageColor);
    root.setBorder(new RoundedPopupBorder(new Color(203, 213, 225)));
    dialog.setContentPane(root);

    JPanel popupHeader = new JPanel(new BorderLayout(12, 0));
    popupHeader.setBackground(Color.WHITE);
    popupHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, lineColor),
            BorderFactory.createEmptyBorder(13, 17, 12, 10)));

    JPanel heading = new JPanel();
    heading.setOpaque(false);
    heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

    JLabel popupTitle = new JLabel("Pilih Tindakan Medis (ICD-9)");
    popupTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
    popupTitle.setForeground(textColor);

    JLabel popupSubtitle = new JLabel(
            "Cari kode atau deskripsi, lalu pilih satu atau beberapa tindakan.");
    popupSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    popupSubtitle.setForeground(mutedColor);
    popupSubtitle.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    heading.add(popupTitle);
    heading.add(popupSubtitle);

    JButton popupClose = new JButton("\u00d7");
    popupClose.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    popupClose.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
    popupClose.setForeground(mutedColor);
    popupClose.setBackground(Color.WHITE);
    popupClose.setPreferredSize(new Dimension(34, 30));
    popupClose.setMargin(new Insets(0, 0, 2, 0));
    popupClose.setFocusPainted(false);
    popupClose.setBorderPainted(false);
    popupClose.setContentAreaFilled(true);
    popupClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    popupClose.addActionListener(e -> dialog.dispose());

    popupHeader.add(heading, BorderLayout.CENTER);
    popupHeader.add(popupClose, BorderLayout.EAST);

    final Point[] popupPressedAt = {null};
    popupHeader.addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
            popupPressedAt[0] = e.getPoint();
        }
    });
    popupHeader.addMouseMotionListener(new MouseMotionAdapter() {
        @Override public void mouseDragged(MouseEvent e) {
            if (popupPressedAt[0] != null) {
                Point screen = e.getLocationOnScreen();
                dialog.setLocation(
                        screen.x - popupPressedAt[0].x,
                        screen.y - popupPressedAt[0].y);
            }
        }
    });

    JLabel searchLabel = new JLabel("Cari kode atau deskripsi");
    searchLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    searchLabel.setForeground(new Color(51, 65, 85));

    JTextField txtCari = new JTextField();
    txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    txtCari.setForeground(textColor);
    txtCari.setBackground(Color.WHITE);
    txtCari.setCaretColor(textColor);
    txtCari.setPreferredSize(new Dimension(10, 34));
    txtCari.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    txtCari.setToolTipText("Masukkan kode atau nama tindakan ICD-9");

    JLabel resultCount = new JLabel(listFull.size() + " data");
    resultCount.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 10));
    resultCount.setForeground(new Color(30, 64, 175));
    resultCount.setOpaque(true);
    resultCount.setBackground(new Color(219, 234, 254));
    resultCount.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));

    JPanel searchTitleRow = new JPanel(new BorderLayout());
    searchTitleRow.setOpaque(false);
    searchTitleRow.add(searchLabel, BorderLayout.WEST);
    searchTitleRow.add(resultCount, BorderLayout.EAST);

    JPanel searchPanel = new JPanel(new BorderLayout(0, 6));
    searchPanel.setBackground(pageColor);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(11, 14, 10, 14));
    searchPanel.add(searchTitleRow, BorderLayout.NORTH);
    searchPanel.add(txtCari, BorderLayout.CENTER);

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.setBackground(pageColor);
    northPanel.add(popupHeader, BorderLayout.NORTH);
    northPanel.add(searchPanel, BorderLayout.CENTER);
    root.add(northPanel, BorderLayout.NORTH);

    String[] columnNames = {"Kode", "Deskripsi Tindakan"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    JTable tableIcd9 = new JTable(model);
    tableIcd9.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    tableIcd9.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    tableIcd9.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    tableIcd9.setRowHeight(30);
    tableIcd9.setBackground(Color.WHITE);
    tableIcd9.setForeground(textColor);
    tableIcd9.setSelectionBackground(new Color(219, 234, 254));
    tableIcd9.setSelectionForeground(new Color(30, 64, 175));
    tableIcd9.setGridColor(new Color(241, 245, 249));
    tableIcd9.setShowVerticalLines(false);
    tableIcd9.setShowHorizontalLines(true);
    tableIcd9.setIntercellSpacing(new Dimension(0, 1));

    JTableHeader tableHeader = tableIcd9.getTableHeader();
    tableHeader.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    tableHeader.setBackground(new Color(241, 245, 249));
    tableHeader.setForeground(new Color(51, 65, 85));
    tableHeader.setPreferredSize(new Dimension(0, 32));
    tableHeader.setReorderingAllowed(false);

    DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
    headerRenderer.setOpaque(true);
    headerRenderer.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    headerRenderer.setBackground(new Color(241, 245, 249));
    headerRenderer.setForeground(new Color(51, 65, 85));
    headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
    headerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    for (int i = 0; i < tableIcd9.getColumnModel().getColumnCount(); i++) {
        tableIcd9.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
    }

    // Kolom kode tepat selebar textbox prosedur (fallback: delapan karakter).
    int codeWidth = parent != null && parent.getWidth() > 0
            ? parent.getWidth()
            : tableIcd9.getFontMetrics(tableIcd9.getFont())
                    .stringWidth("00000000") + 20;
    TableColumn codeColumn = tableIcd9.getColumnModel().getColumn(0);
    codeColumn.setMinWidth(codeWidth);
    codeColumn.setPreferredWidth(codeWidth);
    codeColumn.setMaxWidth(codeWidth);
    tableIcd9.getColumnModel().getColumn(1).setPreferredWidth(680);

    JScrollPane scroll = new JScrollPane(tableIcd9);
    scroll.setBorder(BorderFactory.createLineBorder(lineColor));
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.getVerticalScrollBar().setUnitIncrement(16);

    JPanel tableCard = new JPanel(new BorderLayout());
    tableCard.setBackground(pageColor);
    // Tanpa padding horizontal: deskripsi mengisi sampai sisi kanan popup/card.
    tableCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    tableCard.add(scroll, BorderLayout.CENTER);
    root.add(tableCard, BorderLayout.CENTER);

    JLabel selectedLabel = new JLabel("Kode tindakan terpilih");
    selectedLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    selectedLabel.setForeground(new Color(51, 65, 85));

    JLabel selectedHint = new JLabel("Pisahkan beberapa kode dengan tanda titik koma (;)");
    selectedHint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
    selectedHint.setForeground(mutedColor);

    JPanel selectedTitle = new JPanel(new BorderLayout());
    selectedTitle.setOpaque(false);
    selectedTitle.add(selectedLabel, BorderLayout.WEST);
    selectedTitle.add(selectedHint, BorderLayout.EAST);

    JTextArea txtSelected = new JTextArea(2, 30);
    txtSelected.setText(existingCodes == null ? "" : existingCodes);
    txtSelected.setEditable(true);
    txtSelected.setLineWrap(true);
    txtSelected.setWrapStyleWord(true);
    txtSelected.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    txtSelected.setForeground(textColor);
    txtSelected.setBackground(Color.WHITE);
    txtSelected.setCaretColor(textColor);
    txtSelected.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));

    JScrollPane selectedScroll = new JScrollPane(txtSelected);
    selectedScroll.setPreferredSize(new Dimension(10, 58));
    selectedScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

    JPanel selectedPanel = new JPanel(new BorderLayout(0, 6));
    selectedPanel.setBackground(pageColor);
    selectedPanel.add(selectedTitle, BorderLayout.NORTH);
    selectedPanel.add(selectedScroll, BorderLayout.CENTER);

    JButton btnPilih = new JButton("Gunakan Pilihan");
    JButton btnBatal = new JButton("Batal");
    styleIcd9PopupButton(btnPilih, true);
    styleIcd9PopupButton(btnBatal, false);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    btnPanel.setOpaque(false);
    btnPanel.add(btnBatal);
    btnPanel.add(btnPilih);

    JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
    bottomPanel.setBackground(pageColor);
    bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor),
            BorderFactory.createEmptyBorder(10, 14, 12, 14)));
    bottomPanel.add(selectedPanel, BorderLayout.CENTER);
    bottomPanel.add(btnPanel, BorderLayout.SOUTH);
    root.add(bottomPanel, BorderLayout.SOUTH);

    tableIcd9.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            int viewRow = tableIcd9.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            int row = tableIcd9.convertRowIndexToModel(viewRow);

            Icd9Item item = listDisplay.get(row);
            LinkedHashSet<String> selectedCodes = new LinkedHashSet<>();
            String current = txtSelected.getText().trim();
            if (!current.isEmpty()) {
                for (String code : current.split(";")) {
                    String trimmed = code.trim();
                    if (!trimmed.isEmpty()) selectedCodes.add(trimmed);
                }
            }
            selectedCodes.add(item.getCode());
            existingSet.clear();
            existingSet.addAll(selectedCodes);
            txtSelected.setText(String.join(";", selectedCodes));
        }
    });

    javax.swing.Timer searchTimer = new javax.swing.Timer(500, ev -> {
        String keyword = txtCari.getText().trim();
        List<Icd9Item> newList = fetchIcd9List(keyword.isEmpty() ? null : keyword);
        listDisplay.clear();
        listDisplay.addAll(newList);
        model.setRowCount(0);
        for (Icd9Item item : listDisplay) {
            model.addRow(new Object[]{item.getCode(), item.getName()});
        }
        resultCount.setText(listDisplay.size() + " data");
        tableIcd9.clearSelection();
        for (int i = 0; i < listDisplay.size(); i++) {
            if (existingSet.contains(listDisplay.get(i).getCode())) {
                tableIcd9.addRowSelectionInterval(i, i);
            }
        }
    });
    searchTimer.setRepeats(false);
    txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        void onChange() { searchTimer.restart(); }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange(); }
    });

    final String[] result = {null};
    btnPilih.addActionListener(e -> {
        String hasil = txtSelected.getText().trim();
        if (hasil.isEmpty()) {
            showModernToast(parent, "Pilih minimal satu tindakan",
                    ToastMessage.WARNING, 0);
            return;
        }
        result[0] = hasil;
        dialog.dispose();
    });
    btnBatal.addActionListener(e -> dialog.dispose());

    for (Icd9Item item : listDisplay) {
        model.addRow(new Object[]{item.getCode(), item.getName()});
    }
    for (int i = 0; i < listDisplay.size(); i++) {
        if (existingSet.contains(listDisplay.get(i).getCode())) {
            tableIcd9.addRowSelectionInterval(i, i);
        }
    }

    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-icd9-popup");
    dialog.getRootPane().getActionMap().put("close-icd9-popup",
            new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });

    java.awt.Window ownerWindow = parent == null
            ? null : SwingUtilities.getWindowAncestor(parent);
    dialog.setSize(880, 500);
    positionIcd9PopupBelowInput(dialog, parent, ownerWindow);
    SwingUtilities.invokeLater(() -> txtCari.requestFocusInWindow());

    if (ownerWindow instanceof BPJSRujukanSatuSehat) {
        ((BPJSRujukanSatuSehat) ownerWindow).showModalDialogWithBlur(dialog);
    } else {
        dialog.setVisible(true);
    }
    return result[0];
}

/**
 * Menambatkan popup ICD-9 tepat di bawah textbox prosedur. Lebar popup dimulai
 * dari sisi kiri textbox dan berakhir pada sisi kanan card Kriteria Rujukan.
 */
private static void positionIcd9PopupBelowInput(JDialog dialog,
        Component anchor, Window ownerWindow) {
    if (dialog == null) return;
    if (anchor == null || !anchor.isShowing()) {
        dialog.setLocationRelativeTo(ownerWindow);
        return;
    }

    Point anchorPoint;
    try {
        anchorPoint = anchor.getLocationOnScreen();
    } catch (IllegalComponentStateException ex) {
        dialog.setLocationRelativeTo(ownerWindow);
        return;
    }

    GraphicsConfiguration configuration = anchor.getGraphicsConfiguration();
    Rectangle usableBounds = configuration == null
            ? new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())
            : new Rectangle(configuration.getBounds());
    if (configuration != null) {
        try {
            Insets screenInsets = Toolkit.getDefaultToolkit()
                    .getScreenInsets(configuration);
            usableBounds.x += screenInsets.left;
            usableBounds.y += screenInsets.top;
            usableBounds.width -= screenInsets.left + screenInsets.right;
            usableBounds.height -= screenInsets.top + screenInsets.bottom;
        } catch (Exception ignored) {
            // Bounds monitor tetap cukup aman bila desktop tidak memberi inset.
        }
    }

    int cardRight = anchorPoint.x + dialog.getWidth();
    if (ownerWindow instanceof BPJSRujukanSatuSehat) {
        widget.InternalFrame criteriaCard =
                ((BPJSRujukanSatuSehat) ownerWindow).frameKriteria;
        if (criteriaCard != null && criteriaCard.isShowing()) {
            try {
                Point cardPoint = criteriaCard.getLocationOnScreen();
                cardRight = cardPoint.x + criteriaCard.getWidth();
            } catch (IllegalComponentStateException ignored) {
                // Gunakan lebar default dialog bila posisi card belum tersedia.
            }
        }
    }

    int screenRight = usableBounds.x + usableBounds.width - 4;
    int screenBottom = usableBounds.y + usableBounds.height - 4;
    int x = Math.max(usableBounds.x + 4, anchorPoint.x);
    int popupRight = Math.min(screenRight, cardRight);
    int width = popupRight - x;
    if (width < 320) {
        width = Math.min(440, screenRight - x);
    }
    width = Math.max(260, width);

    int y = anchorPoint.y + anchor.getHeight() + 3;
    int availableBelow = screenBottom - y;
    int height;
    if (availableBelow >= 280) {
        height = Math.min(500, availableBelow);
    } else {
        // Fallback hanya saat ruang di bawah benar-benar tidak mencukupi.
        height = Math.min(500, Math.max(280,
                anchorPoint.y - usableBounds.y - 7));
        y = Math.max(usableBounds.y + 4, anchorPoint.y - height - 3);
    }

    dialog.setSize(width, height);
    dialog.setLocation(x, y);
}

private static void styleIcd9PopupButton(JButton button, boolean primary) {
    button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
    int width = Math.max(92,
            button.getFontMetrics(button.getFont()).stringWidth(button.getText()) + 30);
    button.setPreferredSize(new Dimension(width, 31));
    button.setFocusPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setForeground(primary ? Color.WHITE : new Color(51, 65, 85));
    button.setBackground(primary ? new Color(37, 99, 235) : Color.WHITE);
    button.setBorder(primary
            ? BorderFactory.createEmptyBorder(5, 13, 5, 13)
            : BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225)),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)));
    button.setBorderPainted(true);
    button.setContentAreaFilled(true);
    button.setOpaque(true);
}

// Contoh class sederhana
private static class Icd9Item {
    private String code;
    private String name;

    public Icd9Item() {}
    // getter & setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}


  
       

private static List<Icd9Item> fetchIcd9List(String keyword) {
    List<Icd9Item> result = new ArrayList<>();
    String sql;
    if (keyword == null || keyword.trim().isEmpty()) {
        sql = "SELECT kode, deskripsi_panjang, deskripsi_pendek "
                + "FROM icd9 ORDER BY kode LIMIT 200";
    } else {
        // Pencarian dengan LIKE (case-insensitive, cari di kode atau deskripsi)
        sql = "SELECT kode, deskripsi_panjang, deskripsi_pendek FROM icd9 " +
              "WHERE kode LIKE ? OR deskripsi_panjang LIKE ? OR deskripsi_pendek LIKE ? " +
              "ORDER BY kode LIMIT 200";
    }

    Connection koneksi = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        koneksi = koneksiDB.condb();
        ps = koneksi.prepareStatement(sql);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
        }
        rs = ps.executeQuery();

        while (rs.next()) {
            Icd9Item item = new Icd9Item();
            item.setCode(rs.getString("kode"));
            String name = rs.getString("deskripsi_panjang");
            if (name == null || name.trim().isEmpty()) {
                name = rs.getString("deskripsi_pendek");
            }
            item.setName(name != null ? name : "");
            result.add(item);
        }
    } catch (Exception e) {
        System.out.println("Gagal load data ICD-9: " + e);
        e.printStackTrace();        
        
        
        showModernToast(null,
                "Gagal mengambil data ICD-9: "
                        + (e.getMessage() == null ? "" : e.getMessage().trim()),
                ToastMessage.ERROR, 0);
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (ps != null) ps.close(); } catch (Exception e) {}
        //try { if (koneksi != null) koneksi.close(); } catch (Exception e) {}
    }
    return result;
}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnPrint;
    private widget.Button btnBuatEncounter;
    private widget.Button btnCariFaskes;
    private widget.Button btnCekKriteria;
    private widget.Button btnHapus;
    private widget.Button btnKirim;
    private widget.Button btnPilihDiagnosa;
    private widget.Button btnPilihPoli;
    private widget.Button btnResponApi;
    private widget.Button btnTutup;
    private widget.ComboBox cbJnsPelayanan;
    private widget.ComboBox cbProvinsi;
    private widget.ComboBox cbTipeRujukan;
    private widget.Tanggal dtTglRencana;
    private widget.Tanggal dtTglRujukan;
    private widget.InternalFrame frameFaskes;
    private widget.InternalFrame frameKriteria;
    private widget.InternalFrame frameMain;
    private widget.Label lblCatatan;
    private widget.Label lblDiag;
    private widget.Label lblEncounter;
    private widget.Label lblIhsDokter;
    private widget.Label lblIhsPasien;
    private widget.Label lblJnsPelayanan;
    private widget.Label lblKet;
    private widget.Label lblKet1;
    private widget.Label lblNoRawat;
    private widget.Label lblNoSep;
    private widget.Label lblNoSep1;
    private widget.Label lblNoSep2;
    private widget.Label lblPasien;
    private widget.Label lblPoli;
    private widget.Label lblProvinsi;
    private widget.Label lblStatus;
    private widget.Label lblTglRencana;
    private widget.Label lblTglRujukan;
    private widget.Label lblTipeRujukan;
    private widget.PanelBiasa panelData;
    private widget.PanelBiasa panelTengah;
    private widget.PanelBiasa panelTombol;
    private widget.ScrollPane scrollCatatan;
    private widget.ScrollPane scrollFaskes;
    private widget.ScrollPane scrollKeterangan;
    private widget.ScrollPane scrollKeterangan1;
    private widget.ScrollPane scrollKriteria;
    private widget.TextBox tEncounter;
    private widget.TextBox tIdDokterIhs;
    private widget.TextBox tIdPasienIhs;
    private widget.TextBox tKdDiagnosaRujuk;
    private widget.TextBox tKdPoliRujuk;
    private widget.TextBox tNmDiagnosaRujuk;
    private widget.TextBox tNmPoliRujuk;
    private widget.TextBox tNoRawat;
    private widget.TextBox tNoRujukanBpjs;
    private widget.TextBox tNoRujukanSatuSehat;
    private widget.TextBox tNoSep;
    private widget.TextBox tPasien;
    private widget.TextArea tStatus;
    private widget.TextArea taCatatan;
    private widget.TextArea taKeterangan;
    private widget.Table tblFaskes;
    private widget.Table tblKriteria;
    // End of variables declaration//GEN-END:variables

}
