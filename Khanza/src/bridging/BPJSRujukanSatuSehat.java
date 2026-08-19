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
        scrollKeterangan1.setViewportView(tStatus);

        panelData.add(scrollKeterangan1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 490, 360, 80));

        lblKet1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKet1.setText("Respon API");
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
            frameKriteria.setBorder(BorderFactory.createTitledBorder("Kriteria Rujukan (isi jawaban di kolom paling kanan)"));
            frameFaskes.setBorder(BorderFactory.createTitledBorder("Faskes Tujuan (centang salah satu)"));

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

            tblFaskes.setModel(modelFaskes);
            tblFaskes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] widthsFaskes = {50, 130, 90, 320, 60, 150,200, 90,90, 80};
            for (int i = 0; i < widthsFaskes.length && i < tblFaskes.getColumnModel().getColumnCount(); i++) {
                tblFaskes.getColumnModel().getColumn(i).setPreferredWidth(widthsFaskes[i]);
            }
            tblFaskes.setDefaultRenderer(Object.class, new WarnaTable());

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
        } catch (Exception e) {
            System.out.println("Gagal setup tabel tengah: " + e.getMessage());
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
    tStatus.setLineWrap(true);
    tStatus.setWrapStyleWord(true);
    tStatus.setForeground(
            error
            ? new java.awt.Color(180, 0, 0)
            : new java.awt.Color(0, 100, 0)
    );

    tStatus.setText(
            "Status : " + safe(msg)
    );

    System.out.println(
            "[BPJSRujukanSatuSehat] " + msg
    );
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

                ToastMessage.showToast(
                        null,
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

    ToastMessage.showToast(
            null,
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
            
                ToastMessage.showToast(
                        null,
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
        popupPenyakit.setSize(900, 500);
        popupPenyakit.setLocationRelativeTo(this);
        popupPenyakit.setVisible(true);
        // hasil dipilih -> handler windowClosed di setupPopupListeners()
    }

    // =================================================================
    //  ACTION: PILIH POLI RUJUKAN (popup BPJSCekReferensiPoli)
    // =================================================================
    private void doPilihPoli() {
        popupPoli.setSize(900, 500);
        popupPoli.setLocationRelativeTo(this);
        popupPoli.setVisible(true);
        // hasil dipilih -> handler windowClosed di setupPopupListeners()
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

        
        
        String pesanHtml =
        "<html>" +
        "<div style='width:500px;padding:12px;font-family:Segoe UI,Arial,sans-serif;'>" +

        "<div style='text-align:center;'>" +
        "<h2 style='color:#0d6efd;margin-bottom:5px;'>🩺 Konfirmasi Pembuatan Encounter SATUSEHAT</h2>" +
        "<p style='color:#666666;margin-top:0;'>Silakan periksa kembali data berikut sebelum dikirim.</p>" +
        "</div>" +

        "<hr style='border:1px solid #0d6efd;'/>" +

        "<table style='width:100%;border-collapse:collapse;margin-top:10px;'>" +

        "<tr style='background:#f8f9fa;'>" +
        "<td style='padding:8px;font-weight:bold;width:35%;'>👤 Nama Pasien</td>" +
        "<td style='padding:8px;'>" + namaPasien + "</td>" +
        "</tr>" +

        "<tr>" +
        "<td style='padding:8px;font-weight:bold;'>🆔 IHS Pasien</td>" +
        "<td style='padding:8px;'>" + idPasienSatuSehat + "</td>" +
        "</tr>" +

        "<tr style='background:#f8f9fa;'>" +
        "<td style='padding:8px;font-weight:bold;'>👨‍⚕️ IHS Dokter</td>" +
        "<td style='padding:8px;'>" + kdDokterSatuSehat + "</td>" +
        "</tr>" +

        "<tr>" +
        "<td style='padding:8px;font-weight:bold;'>📋 No. Rawat</td>" +
        "<td style='padding:8px;'>" + noRawat + "</td>" +
        "</tr>" +

//        "<tr style='background:#f8f9fa;'>" +
//        "<td style='padding:8px;font-weight:bold;'>🏥 Organisasi</td>" +
//        "<td style='padding:8px;'>" + orgId + "</td>" +
//        "</tr>" +

        "<tr>" +
        "<td style='padding:8px;font-weight:bold;'>📅 Tanggal Encounter</td>" +
        "<td style='padding:8px;'>" +
        new java.text.SimpleDateFormat("dd-MM-yyyy").format(new Date()) +
        "</td>" +
        "</tr>" +

        "<tr style='background:#f8f9fa;'>" +
        "<td style='padding:8px;font-weight:bold;'>📌 Status Encounter</td>" +
        "<td style='padding:8px;'>ARRIVED</td>" +
        "</tr>" +

        "</table>" +

        "<div style='margin-top:15px;padding:10px;background:#fff3cd;border:1px solid #ffe69c;border-radius:4px;'>" +
        "<b>⚠ Perhatian</b><br>" +
        "Encounter yang sudah berhasil dikirim ke SATUSEHAT tidak dapat diubah secara bebas. " +
        "Pastikan pasien, dokter, dan nomor rawat sudah sesuai." +
        "</div>" +

        "<div style='margin-top:12px;padding:10px;background:#e8f5e9;border:1px solid #c8e6c9;border-radius:4px;'>" +
        "<b>✓ Tindakan yang akan dilakukan :</b><br>" +
        "• Membuat Encounter baru di SATUSEHAT<br>" +
        "• Menghubungkan pasien dengan dokter yang dipilih<br>" +
        "• Menyiapkan referensi Encounter untuk proses rujukan SATUSEHAT berikutnya" +
        "</div>" +

        "<p style='font-size:11px;color:#888888;margin-top:15px;text-align:center;'>" +
        "Klik <b>Yes</b> untuk melanjutkan atau <b>No</b> untuk membatalkan proses." +
        "</p>" +

        "</div>" +
        "</html>";

    int conf = JOptionPane.showConfirmDialog(
            this,
            pesanHtml,
            "Konfirmasi Encounter SATUSEHAT",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
    );

    if (conf != JOptionPane.YES_OPTION) {
        return;
    }
        
        
        
        
//        int conf = JOptionPane.showConfirmDialog(this,
//                "Akan kirim Encounter baru ke Satu Sehat dengan data:\n\n"
//                + "  Pasien    : " + namaPasien + "\n"
//                + "  IHS       : " + idPasienSatuSehat + "\n"
//                + "  Dokter    : " + kdDokterSatuSehat + "\n"
//                + "  No.Rawat  : " + noRawat + "\n"
//                + "  Periode   : " + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "\n\n"
//                + "Lanjutkan?",
//                "Konfirmasi Buat Encounter",
//                JOptionPane.YES_NO_OPTION);
//        if (conf != JOptionPane.YES_OPTION) return;

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

                ToastMessage.showToast(
                        null,
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

                ToastMessage.showToast(
                        null,
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
        ToastMessage.showToast(
                null,
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
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().setBackground(new Color(240, 248, 255));

    // ========== PANEL ATAS ==========
    JPanel topPanel = new JPanel(new BorderLayout(5, 5));
    topPanel.setBorder(BorderFactory.createTitledBorder("Cari Tindakan"));
    JLabel lblCari = new JLabel("🔍 Kata Kunci: ");
    JTextField txtCari = new JTextField();
    topPanel.add(lblCari, BorderLayout.WEST);
    topPanel.add(txtCari, BorderLayout.CENTER);
    dialog.add(topPanel, BorderLayout.NORTH);

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
                ToastMessage.showToast(
                        null,
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
                    ToastMessage.showToast(
                        null,
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
           ToastMessage.showToast(
                        null,
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
    dialog.setVisible(true);
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