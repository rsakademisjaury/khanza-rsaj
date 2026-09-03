/*
 * BPJSRujukanSatuSehat.java
 *
 * Form Rujukan Keluar terintegrasi BPJS + Satu Sehat (Sisrute).
 * Dipanggil dari BPJSDataSEP setelah user memilih SEP rawat jalan.
 *
 * Flow:
 *   1. setDataSEP(...) dipanggil parent → form auto-isi data
 *   2. User cek/lengkapi data, lalu klik [Cek Kriteria]
 *      → POST /Rujukan/GetKriteriaRujukan
 *      → Tabel kriteria muncul, user isi jawaban (boolean/text)
 *   3. User klik [Cari Faskes] → POST /Sisrute/GetFaskesRujukan
 *      → Tabel faskes muncul, user pilih satu RS tujuan
 *   4. User klik [Kirim Rujukan] → POST /Sisrute/postKunjungan
 *      → Simpan ke 3 tabel:
 *         - bridging_rujukan_bpjs       (existing)
 *         - bridging_rujukan_satusehat  (BARU)
 *         - bridging_kriteria_rujukan_satusehat (BARU)
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
import java.util.HashSet;
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

    // ===== Snapshot respons API terakhir (hanya untuk tampilan/debugging) =====
    private String lastApiResponseJson = "";
    private String lastApiResponseSource = "Belum ada respon API";
    private String lastApiResponseTime = "";
    private boolean lastApiResponseError = false;
    private String lastProcessStatusMessage = "Form siap digunakan.";
    private boolean lastProcessStatusError = false;

    // Label ringkas pada header panel tabel (UI saja, tidak ikut payload/API).
    private javax.swing.JLabel lblJumlahKriteriaUi;
    private javax.swing.JLabel lblJumlahFaskesUi;

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

            modelFaskes.addTableModelListener(e -> {
                if (e.getColumn() == 0 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    Object val = modelFaskes.getValueAt(row, 0);
                    if (Boolean.TRUE.equals(val)) {
                        for (int i = 0; i < modelFaskes.getRowCount(); i++) {
                            if (i != row) {
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
                }
            });

            // Tinggi kedua panel selalu dihitung ulang setelah isi model berubah.
            modelKriteria.addTableModelListener(e ->
                    javax.swing.SwingUtilities.invokeLater(this::updateDynamicTableHeights));
            modelFaskes.addTableModelListener(e ->
                    javax.swing.SwingUtilities.invokeLater(this::updateDynamicTableHeights));
            javax.swing.SwingUtilities.invokeLater(this::updateDynamicTableHeights);
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
        frameKriteria.add(createTableSectionHeader(
                "Kriteria Rujukan",
                "Isi jawaban pada kolom paling kanan sesuai kondisi pasien.",
                lblJumlahKriteriaUi), java.awt.BorderLayout.NORTH);
        frameKriteria.add(scrollKriteria, java.awt.BorderLayout.CENTER);

        frameFaskes.removeAll();
        frameFaskes.setLayout(new java.awt.BorderLayout());
        frameFaskes.setOpaque(true);
        frameFaskes.setBackground(java.awt.Color.WHITE);
        frameFaskes.setBorder(javax.swing.BorderFactory.createLineBorder(line));
        frameFaskes.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        frameFaskes.add(createTableSectionHeader(
                "Faskes Tujuan",
                "Centang satu fasilitas kesehatan sebagai tujuan rujukan.",
                lblJumlahFaskesUi), java.awt.BorderLayout.NORTH);
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
        panelTengah.revalidate();
        panelTengah.repaint();
    }

    private javax.swing.JPanel createTableSectionHeader(String titleText,
            String subtitleText, javax.swing.JLabel countBadge) {
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

        javax.swing.JLabel title = new javax.swing.JLabel(titleText);
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel subtitle = new javax.swing.JLabel(subtitleText);
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));

        textBox.add(title);
        textBox.add(subtitle);
        javax.swing.JPanel badgeBox = new javax.swing.JPanel(new java.awt.GridBagLayout());
        badgeBox.setOpaque(false);
        badgeBox.add(countBadge);
        header.add(textBox, java.awt.BorderLayout.CENTER);
        header.add(badgeBox, java.awt.BorderLayout.EAST);
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
     * Menampilkan maksimal enam baris sebelum scrollbar vertikal diperlukan.
     * Tinggi scrollbar horizontal turut dihitung agar baris terakhir tidak
     * terpotong meskipun lebar kolom melebihi viewport.
     */
    private void updateDynamicTableHeights() {
        updateTableSectionHeight(frameKriteria, scrollKriteria, tblKriteria);
        updateTableSectionHeight(frameFaskes, scrollFaskes, tblFaskes);

        if (lblJumlahKriteriaUi != null) {
            int count = modelKriteria.getRowCount();
            lblJumlahKriteriaUi.setText(count + " item");
        }
        if (lblJumlahFaskesUi != null) {
            int count = modelFaskes.getRowCount();
            lblJumlahFaskesUi.setText(count + " faskes");
        }

        panelTengah.revalidate();
        panelTengah.repaint();
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
        btnHapus.addActionListener(e -> doHapusRujukan());
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
     * Menjaga tombol operasional tetap di kiri dan tombol Respon API selalu
     * menempel di sisi kanan panel, berapa pun lebar formnya.
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
        panelKiri.add(btnHapus);
        panelKiri.add(BtnPrint);
        panelKiri.add(btnTutup);

        btnResponApi.setIcon(new JsonButtonIcon());
        btnResponApi.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        btnResponApi.setToolTipText("Lihat respon API terakhir dalam format JSON");
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

        javax.swing.JPanel identityBody = createFormRowsPanel(
                createDualFormRow(lblNoSep, tNoSep, lblNoSep1, tNoRujukanBpjs),
                createDualFormRow(lblNoRawat, tNoRawat, lblNoSep2, tNoRujukanSatuSehat),
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
        javax.swing.JPanel notesCard = createModernFormCard(
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
        stack.add(notesCard, stackGbc);

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

        updateStatusProcessStyle(tStatus.getText(), false);
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
        java.awt.Color result = new java.awt.Color(255, 251, 235);
        java.awt.Color resultLine = new java.awt.Color(253, 230, 138);

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
    //  DIALOG RESPON API (snapshot terakhir, read-only)
    // =================================================================
    private void rememberApiResponse(String source, JsonNode response) {
        lastApiResponseSource = safe(source);
        lastApiResponseTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new java.util.Date());
        lastApiResponseError = responseLooksError(response);
        try {
            lastApiResponseJson = response == null
                    ? "{}"
                    : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            lastApiResponseJson = response == null ? "{}" : response.toString();
        }
    }

    private void rememberApiResponse(String source, String rawJson) {
        lastApiResponseSource = safe(source);
        lastApiResponseTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new java.util.Date());
        try {
            JsonNode parsed = mapper.readTree(rawJson == null ? "{}" : rawJson);
            lastApiResponseError = responseLooksError(parsed);
            lastApiResponseJson = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(parsed);
        } catch (Exception e) {
            lastApiResponseError = true;
            lastApiResponseJson = rawJson == null ? "" : rawJson.trim();
        }
    }

    private void rememberApiException(String source, Exception exception) {
        String responseBody = extractApiErrorBody(exception);
        if (!responseBody.isEmpty()) {
            rememberApiResponse(source, responseBody);
            lastApiResponseError = true;
            return;
        }

        lastApiResponseSource = safe(source);
        lastApiResponseTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new java.util.Date());
        lastApiResponseError = true;
        try {
            com.fasterxml.jackson.databind.node.ObjectNode error = mapper.createObjectNode();
            error.put("status", "ERROR");
            error.put("exception", exception == null
                    ? "Exception"
                    : exception.getClass().getSimpleName());
            error.put("message", exception == null ? "" : safe(exception.getMessage()));
            lastApiResponseJson = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(error);
        } catch (Exception ignored) {
            lastApiResponseJson = "{\n  \"status\" : \"ERROR\"\n}";
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
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, true);
        dialog.setUndecorated(true);
        configureRoundedPopupWindow(dialog, true);
        dialog.setTitle("Respon API BPJS & SATUSEHAT");
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(java.awt.Color.WHITE);
        root.setBorder(new RoundedPopupBorder(
                new java.awt.Color(203, 213, 225)));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        header.setBackground(new java.awt.Color(248, 250, 252));
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(14, 18, 13, 12)));

        javax.swing.JPanel heading = new javax.swing.JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JPanel titleRow = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel("Respon API");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 16));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel badge = new javax.swing.JLabel(
                lastApiResponseJson.isEmpty()
                        ? "BELUM ADA"
                        : (lastApiResponseError ? "GAGAL" : "BERHASIL"));
        badge.setOpaque(true);
        badge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 10));
        badge.setForeground(lastApiResponseJson.isEmpty()
                ? new java.awt.Color(71, 85, 105)
                : (lastApiResponseError
                        ? new java.awt.Color(185, 28, 28)
                        : new java.awt.Color(21, 128, 61)));
        badge.setBackground(lastApiResponseJson.isEmpty()
                ? new java.awt.Color(226, 232, 240)
                : (lastApiResponseError
                        ? new java.awt.Color(254, 226, 226)
                        : new java.awt.Color(220, 252, 231)));
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8));

        titleRow.add(title);
        titleRow.add(badge);

        String metadata = lastApiResponseSource;
        if (!lastApiResponseTime.isEmpty()) metadata += "  \u2022  " + lastApiResponseTime;
        javax.swing.JLabel subtitle = new javax.swing.JLabel(metadata);
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        subtitle.setForeground(new java.awt.Color(100, 116, 139));
        subtitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));

        heading.add(titleRow);
        heading.add(subtitle);

        javax.swing.JButton closeHeader = createFlatCloseButton();
        closeHeader.setToolTipText("Tutup");
        closeHeader.addActionListener(e -> dialog.dispose());

        header.add(heading, java.awt.BorderLayout.CENTER);
        header.add(closeHeader, java.awt.BorderLayout.EAST);

        String jsonToShow = lastApiResponseJson.isEmpty()
                ? "{\n  \"message\" : \"Belum ada respon API pada sesi ini\"\n}"
                : lastApiResponseJson;

        javax.swing.JTextPane jsonPane = new javax.swing.JTextPane();
        jsonPane.setEditable(false);
        jsonPane.setBackground(new java.awt.Color(248, 250, 252));
        jsonPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setJsonDocument(jsonPane, jsonToShow);
        jsonPane.setCaretPosition(0);

        javax.swing.JScrollPane jsonScroll = new javax.swing.JScrollPane(jsonPane);
        jsonScroll.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(226, 232, 240)));
        jsonScroll.getVerticalScrollBar().setUnitIncrement(16);
        jsonScroll.setRowHeaderView(createJsonLineNumbers(jsonToShow));

        javax.swing.JPanel body = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        body.setBackground(java.awt.Color.WHITE);
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 12, 16));
        body.add(createApiProcessStatusPanel(), java.awt.BorderLayout.NORTH);
        body.add(jsonScroll, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 10));
        footer.setBackground(java.awt.Color.WHITE);
        footer.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, new java.awt.Color(241, 245, 249)));

        javax.swing.JButton copy = new javax.swing.JButton("Salin JSON");
        styleDialogButton(copy, true);
        copy.setEnabled(!lastApiResponseJson.isEmpty());
        copy.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(lastApiResponseJson);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(selection, selection);
            showModernToast(dialog, "Respon JSON berhasil disalin.", ToastMessage.SUCCESS, 1);
        });

        javax.swing.JButton closeFooter = new javax.swing.JButton("Tutup");
        styleDialogButton(closeFooter, false);
        closeFooter.addActionListener(e -> dialog.dispose());

        footer.add(copy);
        footer.add(closeFooter);

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
        dialog.setSize(860, 610);
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
        java.awt.Color[] colors = resolveProcessStatusColors(
                message, lastProcessStatusError);

        javax.swing.JPanel status = new javax.swing.JPanel(new java.awt.BorderLayout());
        status.setBackground(colors[0]);
        status.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(colors[2]),
                javax.swing.BorderFactory.createEmptyBorder(8, 11, 8, 11)));
        status.setPreferredSize(new java.awt.Dimension(10, 64));

        javax.swing.JPanel content = new javax.swing.JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JPanel titleRow = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel("Status Proses");
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        title.setForeground(colors[1]);

        javax.swing.JLabel badge = new javax.swing.JLabel(lastProcessStatusError
                ? "PERINGATAN" : (processing ? "DIPROSES" : "SIAP"));
        badge.setOpaque(true);
        badge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 9));
        badge.setForeground(colors[1]);
        badge.setBackground(java.awt.Color.WHITE);
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
        statusText.setForeground(colors[1]);
        statusText.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 0, 0));

        content.add(titleRow);
        content.add(statusText);
        status.add(content, java.awt.BorderLayout.CENTER);
        return status;
    }

    private void styleDialogButton(javax.swing.JButton button, boolean primary) {
        // BasicButtonUI memastikan warna solid tetap konsisten pada Windows LAF.
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        button.setPreferredSize(new java.awt.Dimension(112, 30));
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setForeground(java.awt.Color.WHITE);
        button.setBackground(primary
                ? new java.awt.Color(37, 99, 235)
                : new java.awt.Color(220, 38, 38));
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 13, 5, 13));
        button.setBorderPainted(false);
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
    private final JPanel pnlIcd9 = new JPanel(new BorderLayout());
    private final JTextField txtIcd9 = new JTextField();
    private final JButton btnCariIcd9 = new JButton("Cari ICD-9");
    private java.awt.Component active;
    private JTable ownerTable;
    private int editingRow, editingCol;

    KriteriaJawabanCellEditor(JTable table) {
        this.ownerTable = table;
        // Setup panel untuk ICD-9
        pnlIcd9.add(txtIcd9, BorderLayout.CENTER);
        pnlIcd9.add(btnCariIcd9, BorderLayout.EAST);
        txtIcd9.setEditable(true); // agar bisa dihapus kalau salah   
        
        btnCariIcd9.addActionListener(e -> {
            String existing = txtIcd9.getText().trim();
            String selectedCode = showIcd9SearchDialog(ownerTable, existing);
            if (selectedCode != null && !selectedCode.isEmpty()) {
                txtIcd9.setText(selectedCode);
                stopCellEditing();
            }
        });
    }
    
    // Helper untuk menggabungkan dan menghapus duplikat
    private String combineAndDeduplicate(String existing, String newCodes) {
        StringBuilder sb = new StringBuilder();
        if (existing != null && !existing.isEmpty()) {
            sb.append(existing);
            if (!existing.endsWith(";")) sb.append(";");
        }
        sb.append(newCodes);
        // Hilangkan duplikat kode
        String[] parts = sb.toString().split(";");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>(java.util.Arrays.asList(parts));
        return String.join(";", set);
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
            String val = value == null ? "YA" : value.toString().trim();
            if ("true".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val)) val = "YA";
            if ("false".equalsIgnoreCase(val) || "0".equals(val) || "T".equalsIgnoreCase(val) || "TDK".equalsIgnoreCase(val)) val = "TIDAK";
            if (!"YA".equalsIgnoreCase(val) && !"TIDAK".equalsIgnoreCase(val)) val = "YA";
            cmbBoolean.setSelectedItem(val.toUpperCase());
            active = cmbBoolean;
            return cmbBoolean;
        }

        // --- Untuk tipe text, cek apakah kriteria ini mengharuskan ICD-9
        if (isKriteriaTindakanMedis(kriteriaText)) {
            String currentValue = (value == null) ? "" : value.toString();
            txtIcd9.setText(currentValue);
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
        if (active == cmbBoolean) return cmbBoolean.getSelectedItem();
        if (active == pnlIcd9) return txtIcd9.getText();
        return txtText.getText();
    }

    // Helper: menentukan apakah kriteria ini memerlukan pencarian ICD-9
    private boolean isKriteriaTindakanMedis(String kriteriaText) {
        if (kriteriaText == null) return false;
        String lower = kriteriaText.toLowerCase();
        // Sesuaikan dengan teks yang muncul di kriteria rujukan (misal dari BPJS)
        return lower.contains("tindakan") || lower.contains("icd9") || lower.contains("icd-9");
        // Atau bisa juga berdasarkan linkId: jika linkId tertentu, tapi tidak ada di parameter editor.
        // Untuk itu lebih baik gunakan pengecekan saat pembuatan editor di luar kelas ini.
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

            JOptionPane.showMessageDialog(
                    this,
                    "Data pasien tidak ditemukan",
                    "Informasi",
                    JOptionPane.WARNING_MESSAGE
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
            JOptionPane.showMessageDialog(this,
                    "Data Satu Sehat belum lengkap:\n" + warn.toString()
                            + "\nLengkapi data tersebut sebelum mengirim rujukan.",
                    "Data Tidak Lengkap", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                    "Encounter sudah ada untuk no_rawat ini:\n" + existing);
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
                JOptionPane.showMessageDialog(this,
                        "Encounter berhasil dibuat:\n" + idEncounter);
            } else {
                setStatus("Gagal buat Encounter (response kosong).", true);
            }
        } catch (Exception ex) {
            rememberApiException("SATUSEHAT - POST Encounter", ex);
            setStatus("Error: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this,
                    "Gagal buat Encounter:\n" + ex.getMessage());
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
            modelKriteria.setRowCount(0);
            JsonNode arr = resp.path("response").path("kriteriaRujukan");
            if (arr.isArray()) {
                for (JsonNode k : arr) {
                    modelKriteria.addRow(new Object[]{
                            k.path("linkId").asText(),
                            k.path("text").asText(),
                            k.path("type").asText(),
                            defaultJawabanUntukTipe(k.path("type").asText())
                    });
                }
            }

            setStatus("Berhasil. Isi jawaban kriteria, lalu klik [Cari Faskes].", false);
            JOptionPane.showMessageDialog(this,
                    "Kriteria rujukan berhasil diambil (" + modelKriteria.getRowCount()
                            + " item).\nIsi jawaban di kolom paling kanan.\n\n"
                            + "Catatan format jawaban:\n"
                            + "- type=boolean → pilih YA / TIDAK\n"
                            + "- type=text    → isi teks/kode tindakan sesuai kebutuhan",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

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
            JOptionPane.showMessageDialog(this,
                    "Klik [Cek Kriteria] dulu untuk mengambil daftar kriteria.",
                    "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi semua jawaban kriteria sudah diisi
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            String jwb = String.valueOf(modelKriteria.getValueAt(i, 3)).trim();
            if (jwb.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Jawaban kriteria belum lengkap (baris " + (i + 1) + ").",
                        "Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
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

            setStatus("Ditemukan " + count + " faskes. Centang salah satu.", false);

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Build kriteriaRujukan.item JSON dari isi tabel. */
    private String buildKriteriaJsonItem() {
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
                    JOptionPane.showMessageDialog(this,
                            "Baris " + (i + 1) + ": jawaban tipe boolean harus dipilih YA atau TIDAK.",
                            "Format Salah", JOptionPane.WARNING_MESSAGE);
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
        if (!validateBeforeApi()) return;

        if (selectedKdppkTujuan.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu faskes tujuan dengan mencentang salah satu baris.",
                    "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (modelKriteria.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Klik [Cek Kriteria] dulu, dan lengkapi jawabannya.",
                    "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kriteriaJson = buildKriteriaJsonItem();
        if (kriteriaJson == null) return;

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
                JOptionPane.showMessageDialog(this,
                        "Gagal kirim: " + SisruteService.getMessage(resp),
                        "Sisrute", JOptionPane.ERROR_MESSAGE);
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
            // Simpan ke 3 tabel
            saveToDb(req, noRujukan, noRujukanSatuSehat, serviceRequestId);
            
                        
            // Buat pesan HTML dengan gaya yang rapi
            String pesanHtml = "<html>" +
                "<div style='width: 400px; padding: 10px; font-family: Arial, sans-serif;'>" +
                "<h3 style='color: #2c7da0; text-align: center;'>✅ Rujukan Berhasil Dikirim</h3>" +
                "<hr style='border: 1px solid #2c7da0;' />" +
                "<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>" +
                "  <tr style='background-color: #e9f5f9;'>" +
                "    <td style='padding: 8px; font-weight: bold; width: 40%;'>No Rujukan</td>" +
                "    <td style='padding: 8px;'>" + noRujukan + "</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding: 8px; font-weight: bold;'>No Rujukan SatuSehat</td>" +
                "    <td style='padding: 8px;'>" + noRujukanSatuSehat + "</td>" +
                "  </tr>" +
                "  <tr style='background-color: #e9f5f9;'>" +
                "    <td style='padding: 8px; font-weight: bold;'>Service Request ID</td>" +
                "    <td style='padding: 8px;'>" + serviceRequestId + "</td>" +
                "  </tr>" +
                "</table>" +
                "<p style='font-size: 12px; color: gray; margin-top: 15px;'>* Simpan nomor ini untuk referensi</p>" +
                "</div>" +
                "</html>";
            
//                ToastMessage.showToast(
//                null,
//                pesanHtml,
//                ToastMessage.SUCCESS,
//                2   // autoHide 2 = 5 detik (lihat switch di kode Anda)
//                );
            // Tampilkan popup dengan informasi lengkap
         String msg =
            "Nomor Rujukan\t\t: " + noRujukan + "\n"
          + "No Rujukan SatuSehat\t: " + noRujukanSatuSehat + "\n"
          + "Service ID\t\t: " + serviceRequestId;
         
          //Frame parent = (Frame) SwingUtilities.getWindowAncestor((Component) evt.getSource());
//            KhanzaAlert.show(null,
//                "Sukses",
//                msg,
//                KhanzaAlert.SUCCESS);
//            
            JOptionPane.showMessageDialog(this,pesanHtml,
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Berhasil. No Rujukan: " + noRujukan, false);

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void saveToDb(SisruteService.InsertRujukanRequest req,
                          String noRujukan,
                          String noRujukanSatuSehat,
                          String serviceRequestId) {
        try {
            // 1. bridging_rujukan_bpjs
            Sequel.menyimpantf2("bridging_rujukan_bpjs",
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?",
                    "No.Rujukan", 14, new String[]{
                            req.noSep, req.tglRujukan, req.tglRencanaKunjungan,
                            req.ppkDirujuk, selectedNmppkTujuan,
                            req.jnsPelayanan, req.catatan,
                            req.diagRujukan, nmPenyakit,
                            cbTipeRujukan.getSelectedItem().toString(),
                            req.poliRujukan, tNmPoliRujuk.getText().trim(),
                            noRujukan, user
                    });
             // 2. bridging_rujukan_satusehat
            Sequel.SimpanData(
                "bridging_rujukan_satusehat",
                new String[]{
                        "no_rujukan",
                        "no_rujukan_satusehat",
                        "service_request_id",
                        "kode_faskes_satusehat",
                        "id_pasien_satusehat",
                        "kdppk_satusehat_tujuan",
                        "nama_faskes_tujuan",
                        "kd_dokter_satusehat",
                        "encounter_reference",
                        "patient_instruction",
                        "keterangan_rujukan",
                        "kode_propinsi",
                        "nama_propinsi",
                        "kode_kabupaten",
                        "nama_kabupaten",
                        "kode_poli_rujuk"                    
                },
                new String[]{
                        noRujukan,
                        noRujukanSatuSehat,
                        serviceRequestId,
                        req.kodeFaskesSatuSehat,
                        req.idPasienSatuSehat,
                        req.kdppkSatuSehatTujuanRujukan,
                        selectedNmppkTujuan,
                        req.kdDokterSatuSehat,
                        req.encounterReference,
                        req.patientInstruction,
                        req.keteranganRujukan,
                        req.kodePropinsi,
                        req.namaPropinsi,
                        req.kodeKabupaten,
                        req.namaKabupaten,
                        kdPoliRujuk
                }
        );
            
//            
//            
//            
//            
//
//            // 2. bridging_rujukan_satusehat
//            Sequel.menyimpan("bridging_rujukan_satusehat",
//                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now()",
//                    "No.Rujukan SatuSehat", 16, new String[]{
//                            noRujukan,
//                            noRujukanSatuSehat,
//                            serviceRequestId,
//                            req.kodeFaskesSatuSehat,
//                            req.idPasienSatuSehat,
//                            req.kdppkSatuSehatTujuanRujukan,
//                            selectedNmppkTujuan,
//                            req.kdDokterSatuSehat,
//                            req.encounterReference,
//                            req.patientInstruction,
//                            req.keteranganRujukan,
//                            req.kodePropinsi,
//                            req.namaPropinsi,
//                            req.kodeKabupaten,
//                            req.namaKabupaten,
//                            kdPoliRujuk
//                    });

            // 3. bridging_kriteria_rujukan_satusehat
            for (int i = 0; i < modelKriteria.getRowCount(); i++) {
                String linkId = String.valueOf(modelKriteria.getValueAt(i, 0));
                String text   = String.valueOf(modelKriteria.getValueAt(i, 1));
                String tipe   = String.valueOf(modelKriteria.getValueAt(i, 2));
                String jwb    = String.valueOf(modelKriteria.getValueAt(i, 3)).trim();

                String valBool = "";
                String valStr = "";
                if ("boolean".equalsIgnoreCase(tipe)) {
                    String bool = normalisasiBooleanKriteria(jwb);
                    valBool = "true".equalsIgnoreCase(bool) ? "1" : "0";
                } else {
                    valStr = jwb;
                }

                Sequel.menyimpan("bridging_kriteria_rujukan_satusehat",
                        "?,?,?,?,?,?",
                        "Kriteria", 6, new String[]{
                                noRujukan, linkId, text, tipe, valBool, valStr
                        });
            }
        } catch (Exception ex) {
            System.out.println("Gagal simpan DB: " + ex);
            JOptionPane.showMessageDialog(this,
                    "Rujukan terkirim ke BPJS, tapi gagal simpan ke DB lokal:\n" + ex,
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =================================================================
    //  ACTION 4: HAPUS RUJUKAN
    //  DELETE /Rujukan/Delete
    // =================================================================
    private void doHapusRujukan() {
        // Cari rujukan terakhir untuk no_sep ini
        String noRujukan = Sequel.cariIsi(
                "select b.no_rujukan from bridging_rujukan_bpjs b "
                + "where b.no_sep=? order by b.tglRujukan desc limit 1", noSep);

        if (noRujukan == null || noRujukan.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada rujukan tersimpan untuk SEP " + noSep,
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Hapus rujukan No. " + noRujukan + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        // Ambil data tambahan dari bridging_rujukan_satusehat
        String kdppkTujuan = "", patientInstr = "", keteranganRujuk = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kdppk_satusehat_tujuan, patient_instruction, keterangan_rujukan "
                + "from bridging_rujukan_satusehat where no_rujukan=?")) {
            ps.setString(1, noRujukan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kdppkTujuan = safe(rs.getString(1));
                    patientInstr = safe(rs.getString(2));
                    keteranganRujuk = safe(rs.getString(3));
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil data rujukan satusehat: " + e);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Menghapus rujukan...", false);
        try {
            JsonNode resp = sisrute.deleteRujukan(
                    noRujukan, user,
                    kodeFaskesSatuSehat, idPasienSatuSehat,
                    kdppkTujuan, kdDokterSatuSehat,
                    encounterRef, patientInstr, keteranganRujuk);

            rememberApiResponse("BPJS/SISRUTE - Hapus Rujukan", resp);

            if (!SisruteService.isOk(resp)) {
                JOptionPane.showMessageDialog(this,
                        "Gagal hapus: " + SisruteService.getMessage(resp),
                        "Sisrute", JOptionPane.ERROR_MESSAGE);
                setStatus("Gagal: " + SisruteService.getMessage(resp), true);
                return;
            }

            // Hapus dari DB lokal
            Sequel.queryu2("delete from bridging_kriteria_rujukan_satusehat where no_rujukan=?",
                    1, new String[]{noRujukan});
            Sequel.queryu2("delete from bridging_rujukan_satusehat where no_rujukan=?",
                    1, new String[]{noRujukan});
            Sequel.meghapus("bridging_rujukan_bpjs", "no_rujukan", noRujukan);

            JOptionPane.showMessageDialog(this,
                    "Rujukan " + noRujukan + " berhasil dihapus.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Berhasil hapus rujukan " + noRujukan, false);

        } catch (Exception ex) {
            handleApiError(ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
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
            JOptionPane.showMessageDialog(this,
                    "Koneksi ke server BPJS Sisrute terputus.",
                    "Koneksi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
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
    
  private static String showIcd9SearchDialog(java.awt.Component parent, String existingCodes) {
    // Ambil semua data dari database (tanpa filter keyword awal)
    List<Icd9Item> listFull = fetchIcd9List(null);
    if (listFull.isEmpty()) {
        JOptionPane.showMessageDialog(parent, "Data ICD-9 kosong atau koneksi gagal.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    List<Icd9Item> listDisplay = new ArrayList<>(listFull);
    Set<String> existingSet = new HashSet<>();
    
    Set<String> selectedCodes = new LinkedHashSet<>(existingSet);
    if (existingCodes != null && !existingCodes.trim().isEmpty()) {
        String[] codes = existingCodes.split(";");
        for (String code : codes) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) existingSet.add(trimmed);
        }
    }

    JDialog dialog = new JDialog((Frame) null, "Pilih Tindakan Medis (ICD-9)", true);
    dialog.setUndecorated(true);
    configureRoundedPopupWindow(dialog, true);
    dialog.getRootPane().setBorder(new RoundedPopupBorder(
            new Color(203, 213, 225)));
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().setBackground(Color.WHITE);

    JPanel popupHeader = new JPanel(new BorderLayout(10, 0));
    popupHeader.setBackground(new Color(248, 250, 252));
    popupHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(
                    0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(12, 16, 11, 10)));

    JLabel popupTitle = new JLabel("Pilih Tindakan Medis (ICD-9)");
    popupTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
    popupTitle.setForeground(new Color(30, 41, 59));

    JButton popupClose = new JButton("\u00d7");
    popupClose.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
    popupClose.setForeground(new Color(100, 116, 139));
    popupClose.setPreferredSize(new Dimension(32, 28));
    popupClose.setMargin(new Insets(0, 0, 2, 0));
    popupClose.setFocusPainted(false);
    popupClose.setBorderPainted(false);
    popupClose.setContentAreaFilled(false);
    popupClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    popupClose.addActionListener(e -> dialog.dispose());

    popupHeader.add(popupTitle, BorderLayout.CENTER);
    popupHeader.add(popupClose, BorderLayout.EAST);

    final Point[] popupPressedAt = {null};
    popupHeader.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            popupPressedAt[0] = e.getPoint();
        }
    });
    popupHeader.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (popupPressedAt[0] != null) {
                Point screen = e.getLocationOnScreen();
                dialog.setLocation(
                        screen.x - popupPressedAt[0].x,
                        screen.y - popupPressedAt[0].y);
            }
        }
    });

    // ========== PANEL ATAS ==========
    JPanel topPanel = new JPanel(new BorderLayout(5, 5));
    topPanel.setBorder(BorderFactory.createTitledBorder("Cari Tindakan"));
    JLabel lblCari = new JLabel("🔍 Kata Kunci: ");
    JTextField txtCari = new JTextField();
    topPanel.add(lblCari, BorderLayout.WEST);
    topPanel.add(txtCari, BorderLayout.CENTER);

    JPanel northPanel = new JPanel(new BorderLayout(0, 8));
    northPanel.setBackground(Color.WHITE);
    northPanel.add(popupHeader, BorderLayout.NORTH);
    northPanel.add(topPanel, BorderLayout.CENTER);
    dialog.add(northPanel, BorderLayout.NORTH);

    // ========== TABEL ==========
    String[] columnNames = {"Kode", "Deskripsi"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    JTable tableIcd9 = new JTable(model);
    tableIcd9.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    tableIcd9.setRowHeight(25);
    tableIcd9.getTableHeader().setBackground(new Color(70, 130, 200));
    tableIcd9.getTableHeader().setForeground(Color.WHITE);

    // Renderer untuk baris tidak valid
    tableIcd9.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Icd9Item item = listDisplay.get(row);
            boolean selectable = "1".equals(item.getValidcode()) && !"1".equals(item.getIm());
            if (!selectable && !isSelected) {
                c.setBackground(Color.LIGHT_GRAY);
                c.setForeground(Color.GRAY);
            } else if (isSelected) {
                c.setBackground(new Color(51, 153, 255));
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    });

    JScrollPane scroll = new JScrollPane(tableIcd9);
    dialog.add(scroll, BorderLayout.CENTER);

    // ========== PANEL BAWAH (Status & Tombol) ==========
    JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createTitledBorder("Kode yang dipilih"));
    JTextArea txtSelected = new JTextArea(2, 30);
    txtSelected.setText(existingCodes == null ? "" : existingCodes);
    txtSelected.setEditable(true);
    txtSelected.setFont(new Font("Monospaced", Font.PLAIN, 12));
    txtSelected.setBackground(new Color(255, 255, 225));
    JScrollPane selectedScroll = new JScrollPane(txtSelected);
    statusPanel.add(selectedScroll, BorderLayout.CENTER);
    bottomPanel.add(statusPanel, BorderLayout.NORTH);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnPilih = new JButton("Pilih");
    JButton btnBatal = new JButton("Batal");
    btnPanel.add(btnPilih);
    btnPanel.add(btnBatal);
    bottomPanel.add(btnPanel, BorderLayout.SOUTH);
    dialog.add(bottomPanel, BorderLayout.SOUTH);


    tableIcd9.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {

        int row = tableIcd9.getSelectedRow();

        if (row >= 0) {

            Icd9Item item = listDisplay.get(row);

            boolean selectable =
                    "1".equals(item.getValidcode())
                    && !"1".equals(item.getIm());

            if (!selectable) {
                showModernToast(
                        parent,
                        "Kode tidak bisa dipilih!!! ",
                        ToastMessage.WARNING,
                        0
                        );
                return;
            }

            String current = txtSelected.getText().trim();

            LinkedHashSet<String> set = new LinkedHashSet<>();

            if (!current.isEmpty()) {
                for (String s : current.split(";")) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        set.add(s);
                    }
                }
            }

            set.add(item.getCode());

            txtSelected.setText(
                    String.join(";", set)
            );
        }
    }
});

    // Pre-seleksi baris berdasarkan existingCodes
    for (int i = 0; i < listDisplay.size(); i++) {
        if (existingSet.contains(listDisplay.get(i).getCode())) {
            tableIcd9.addRowSelectionInterval(i, i);
        }
    }


    // ========== PENCARIAN DENGAN TIMER ==========
    javax.swing.Timer searchTimer = new javax.swing.Timer(500, ev -> {
        String keyword = txtCari.getText().trim();
        List<Icd9Item> newList = fetchIcd9List(keyword.isEmpty() ? null : keyword);
        listDisplay.clear();
        listDisplay.addAll(newList);
        model.setRowCount(0);
        for (Icd9Item item : listDisplay) {
            model.addRow(new Object[]{item.getCode(), item.getName()});
        }
        // Kembalikan seleksi yang sesuai dengan existingSet
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

    // ========== VALIDASI KLIK BARIS TIDAK VALID ==========
    tableIcd9.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = tableIcd9.rowAtPoint(e.getPoint());
            if (row >= 0) {
                Icd9Item item = listDisplay.get(row);
                boolean selectable = "1".equals(item.getValidcode()) && !"1".equals(item.getIm());
                if (!selectable) {
                    showModernToast(
                        parent,
                        "Kode tidak bisa dipilih!!! ",
                        ToastMessage.WARNING,
                        0
                        );
                    
                    tableIcd9.getSelectionModel().removeSelectionInterval(row, row);
                }
            }
        }
    });

    // ========== TOMBOL PILIH ==========
    final String[] result = {null};

    btnPilih.addActionListener(e -> {

        String hasil = txtSelected.getText().trim();

        if (hasil.isEmpty()) {
           showModernToast(
                        parent,
                        "Pilih minimal satu tindakan ",
                        ToastMessage.WARNING,
                        0
                );
            return;
        }

        
        
        
        
        result[0] = hasil;
        dialog.dispose();
    });

    btnBatal.addActionListener(e -> dialog.dispose());

    // Isi data awal
    for (Icd9Item item : listDisplay) {
        model.addRow(new Object[]{item.getCode(), item.getName()});
    }
// atur tampilan dialog sesuai selera khanza mania
    dialog.setSize(850, 650);

    dialog.setLocationRelativeTo(parent);

    dialog.setLocation(
     dialog.getX(),
     dialog.getY() + 200
    );
    java.awt.Window ownerWindow = parent == null
            ? null
            : javax.swing.SwingUtilities.getWindowAncestor(parent);
    if (ownerWindow instanceof BPJSRujukanSatuSehat) {
        ((BPJSRujukanSatuSehat) ownerWindow).showModalDialogWithBlur(dialog);
    } else {
        dialog.setVisible(true);
    }
    System.out.println(parent.getClass().getName());
    return result[0];
}

// Contoh class sederhana
private static class Icd9Item {
    private String code;
    private String name;
    private String validcode;
    private String im;

    public Icd9Item() {}
    // getter & setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValidcode() { return validcode; }
    public void setValidcode(String validcode) { this.validcode = validcode; }
    public String getIm() { return im; }
    public void setIm(String im) { this.im = im; }
}


  
       

private static List<Icd9Item> fetchIcd9List(String keyword) {
    List<Icd9Item> result = new ArrayList<>();
    String sql;
    if (keyword == null || keyword.trim().isEmpty()) {
        sql = "SELECT kode, deskripsi_panjang, deskripsi_pendek, validcode, im FROM icd9 ORDER BY kode LIMIT 200";
    } else {
        // Pencarian dengan LIKE (case-insensitive, cari di kode atau deskripsi)
        sql = "SELECT kode, deskripsi_panjang, deskripsi_pendek, validcode, im FROM icd9 " +
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
            item.setValidcode(rs.getString("validcode"));
            item.setIm(rs.getString("im"));
            result.add(item);
        }
    } catch (Exception e) {
        System.out.println("Gagal load data ICD-9: " + e);
        e.printStackTrace();        
        
        
        JOptionPane.showMessageDialog(null, "Gagal mengambil data ICD-9: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
