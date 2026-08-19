/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * DlgDataSkriningGiziLanjut.java
 * Kontribusi Haris Rochmatullah RS Bhayangkara Nganjuk
 * Created on 11 November 2020, 20:19:56
 */
package rekammedis;

import freehand.DlgTTDPasien;
import freehand.DlgTTDPasienUrl;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMLayananKedokteranFisikRehabilitasi extends javax.swing.JDialog {

    private final DefaultTableModel tabMode, tabMode2, tabMode3;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0, pilihan = 0;
    private DlgCariDokter dokter = new DlgCariDokter(null, false);
    private String finger = "";
    private DlgCariPetugas petugas = new DlgCariPetugas(null, false);
    private RMLayananKedokteranFisikRehabilitasiCari fisioterapi = new RMLayananKedokteranFisikRehabilitasiCari(null, false);

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public RMLayananKedokteranFisikRehabilitasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.R.M.", "Nama Pasien", "JK", "Tgl.Lahir", "Tanggal Uji", "Kode Dokter", "Nama Dokter", "Anamnesa", "Pemeriksaan Fisik",
            "Diagnosis Medis", "Diagnosis Fungsi", "Pemeriksaan Penunjang", "Tata laksana", "Anjuran", "Goal Of Treatment", "Evaluasi", "Suspek Penyakit Akibat Kerja", "Keterangan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

// Menyesuaikan ukuran tampilan
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// Mengatur lebar kolom
        int[] lebarKolom = {105, 65, 160, 20, 65, 120, 150, 150, 150, 150, 150, 80, 140, 140, 140, 140, 140, 140, 140};

        for (int i = 0; i < lebarKolom.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom[i]);
        }

// Menambahkan renderer untuk warna tabel
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        tabMode2 = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.R.M.", "Nama Pasien", "JK", "Tgl.Lahir", "Tanggal Uji", "Kode Dokter", "Nama Dokter",
            "Program", "Kode Petugas", "Nama Petugas"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat2.setModel(tabMode2);

// Menyesuaikan ukuran tampilan
        tbObat2.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// Mengatur lebar kolom
        int[] lebarKolom2 = {105, 65, 160, 20, 65, 120, 150, 150, 150, 150, 150};

        for (int i = 0; i < lebarKolom2.length; i++) {
            TableColumn column = tbObat2.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom2[i]);
        }

// Menambahkan renderer untuk warna tabel
        tbObat2.setDefaultRenderer(Object.class, new WarnaTable());

        tabMode3 = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.R.M.", "Nama Pasien", "JK", "Tgl.Lahir", "Tanggal Uji", "Kode Dokter", "Nama Dokter",
            "Hasil", "Pemeriksaan Fisik dan Fungsi", "Kode Petugas", "Nama Petugas"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat3.setModel(tabMode3);

// Menyesuaikan ukuran tampilan
        tbObat3.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// Mengatur lebar kolom
        int[] lebarKolom3 = {105, 65, 160, 20, 65, 120, 150, 150, 150, 150, 150, 150};

        for (int i = 0; i < lebarKolom3.length; i++) {
            TableColumn column = tbObat3.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom3[i]);
        }

// Menambahkan renderer untuk warna tabel
        tbObat3.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KdDokter.setDocument(new batasInput((byte) 20).getKata(KdDokter));
        Goal.setDocument(new batasInput((int) 100).getKata(Goal));
        Evaluasi.setDocument(new batasInput((int) 100).getKata(Evaluasi));
        Goal.setDocument(new batasInput((int) 100).getKata(Goal));
        SuspekKet.setDocument(new batasInput((int) 100).getKata(SuspekKet));
        TCari.setDocument(new batasInput((int) 100).getKata(TCari));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }
            });
        }

        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                }
                KdDokter.requestFocus();
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (petugas.getTable().getSelectedRow() != -1) {
                    if (pilihan == 1) {
                        KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NamaPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        KdPetugas.requestFocus();
                    } else if (pilihan == 2) {
                        KdPetugas1.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NamaPetugas1.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        KdPetugas1.requestFocus();
                    }

                }

            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        fisioterapi.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                Diagnosa.setText(fisioterapi.getTable().getValueAt(fisioterapi.getTable().getSelectedRow(), 10).toString());
                Terapi.setText(fisioterapi.getTable().getValueAt(fisioterapi.getTable().getSelectedRow(), 13).toString());
                Diagnosa.requestFocus();

            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

    /*    fisioterapi.getTable().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    // Logika saat tombol spasi ditekan
                    int selectedRow = fisioterapi.getTable().getSelectedRow();
                    if (selectedRow != -1) {
                        Diagnosa.setText(fisioterapi.getTable().getValueAt(selectedRow, 10).toString());
                        Terapi.setText(fisioterapi.getTable().getValueAt(selectedRow, 13).toString());
                        Diagnosa.requestFocus(); // Berikan fokus kembali ke komponen lain
                    }
                }
            }
        }); */

        ChkInput.setSelected(false);
        isForm();

        jam();
        DlgCetak.setSize(550, 145);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnTTDPasien = new javax.swing.JMenuItem();
        MnCetakLembarFormulir = new javax.swing.JMenuItem();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        MnTTDPasien2 = new javax.swing.JMenuItem();
        MnCetakLembarProgram = new javax.swing.JMenuItem();
        DlgCetak = new javax.swing.JDialog();
        internalFrame5 = new widget.InternalFrame();
        panelBiasa4 = new widget.PanelBiasa();
        BtnPrint5 = new widget.Button();
        BtnKeluar4 = new widget.Button();
        Terapi = new widget.TextBox();
        jLabel28 = new widget.Label();
        jLabel29 = new widget.Label();
        Diagnosa = new widget.TextBox();
        BtnCariFisioterapi = new widget.Button();
        internalFrame1 = new widget.InternalFrame();
        tabRehab = new widget.TabPane();
        internalFrame2 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        scrollPane3 = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        jLabel12 = new widget.Label();
        jLabel13 = new widget.Label();
        jLabel15 = new widget.Label();
        Goal = new widget.TextBox();
        jLabel17 = new widget.Label();
        Evaluasi = new widget.TextBox();
        jLabel20 = new widget.Label();
        SuspekKet = new widget.TextBox();
        jLabel24 = new widget.Label();
        Anamnesa = new widget.TextBox();
        jLabel25 = new widget.Label();
        PemeriksaanFisik = new widget.TextBox();
        jLabel23 = new widget.Label();
        PemeriksaanPenunjang = new widget.TextBox();
        jLabel26 = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        Tatalaksana = new widget.TextArea();
        jLabel27 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        Anjuran = new widget.TextArea();
        Suspek = new widget.ComboBox();
        DiagnosisFungsi = new widget.TextBox();
        DiagnosisMedis = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll1 = new widget.ScrollPane();
        tbObat2 = new widget.Table();
        PanelInput1 = new javax.swing.JPanel();
        FormInput1 = new widget.PanelBiasa();
        jLabel33 = new widget.Label();
        Program = new widget.TextBox();
        jLabel34 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        btnPetugas1 = new widget.Button();
        ChkInput1 = new widget.CekBox();
        internalFrame4 = new widget.InternalFrame();
        Scroll2 = new widget.ScrollPane();
        tbObat3 = new widget.Table();
        jPanel5 = new javax.swing.JPanel();
        PanelInput2 = new javax.swing.JPanel();
        FormInput2 = new widget.PanelBiasa();
        jLabel46 = new widget.Label();
        Hasil = new widget.TextBox();
        jLabel47 = new widget.Label();
        PxFisikUjiFungsi = new widget.ComboBox();
        jLabel35 = new widget.Label();
        KdPetugas1 = new widget.TextBox();
        NamaPetugas1 = new widget.TextBox();
        btnPetugas2 = new widget.Button();
        ChkInput2 = new widget.CekBox();
        panelBiasa1 = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel18 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        btnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        JK = new widget.TextBox();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnTTDPasien.setBackground(new java.awt.Color(255, 255, 254));
        MnTTDPasien.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnTTDPasien.setForeground(new java.awt.Color(70, 70, 70));
        MnTTDPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnTTDPasien.setText("Ambil Tanda Tangan Pasien");
        MnTTDPasien.setName("MnTTDPasien"); // NOI18N
        MnTTDPasien.setPreferredSize(new java.awt.Dimension(200, 26));
        MnTTDPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnTTDPasienActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnTTDPasien);

        MnCetakLembarFormulir.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakLembarFormulir.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakLembarFormulir.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakLembarFormulir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakLembarFormulir.setText("Formulir/Lembar Uji Fungsi/Prosedur KFR");
        MnCetakLembarFormulir.setName("MnCetakLembarFormulir"); // NOI18N
        MnCetakLembarFormulir.setPreferredSize(new java.awt.Dimension(270, 26));
        MnCetakLembarFormulir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakLembarFormulirActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakLembarFormulir);

        jPopupMenu2.setName("jPopupMenu2"); // NOI18N

        MnTTDPasien2.setBackground(new java.awt.Color(255, 255, 254));
        MnTTDPasien2.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnTTDPasien2.setForeground(new java.awt.Color(70, 70, 70));
        MnTTDPasien2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnTTDPasien2.setText("Ambil Tanda Tangan Pasien");
        MnTTDPasien2.setName("MnTTDPasien2"); // NOI18N
        MnTTDPasien2.setPreferredSize(new java.awt.Dimension(200, 26));
        MnTTDPasien2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnTTDPasien2ActionPerformed(evt);
            }
        });
        jPopupMenu2.add(MnTTDPasien2);

        MnCetakLembarProgram.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakLembarProgram.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakLembarProgram.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakLembarProgram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakLembarProgram.setText("Cetak Lembar Program");
        MnCetakLembarProgram.setName("MnCetakLembarProgram"); // NOI18N
        MnCetakLembarProgram.setPreferredSize(new java.awt.Dimension(270, 26));
        MnCetakLembarProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakLembarProgramActionPerformed(evt);
            }
        });
        jPopupMenu2.add(MnCetakLembarProgram);

        DlgCetak.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        DlgCetak.setName("DlgCetak"); // NOI18N
        DlgCetak.setUndecorated(true);
        DlgCetak.setResizable(false);

        internalFrame5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 235, 225)), "::[ Cetak Program Fisioterapi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 70, 50))); // NOI18N
        internalFrame5.setName("internalFrame5"); // NOI18N
        internalFrame5.setLayout(new java.awt.BorderLayout(1, 1));

        panelBiasa4.setName("panelBiasa4"); // NOI18N
        panelBiasa4.setLayout(null);

        BtnPrint5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint5.setMnemonic('T');
        BtnPrint5.setText("Cetak");
        BtnPrint5.setToolTipText("Alt+T");
        BtnPrint5.setName("BtnPrint5"); // NOI18N
        BtnPrint5.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint5ActionPerformed(evt);
            }
        });
        panelBiasa4.add(BtnPrint5);
        BtnPrint5.setBounds(10, 80, 100, 30);

        BtnKeluar4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar4.setMnemonic('K');
        BtnKeluar4.setText("Tutup");
        BtnKeluar4.setToolTipText("Alt+K");
        BtnKeluar4.setName("BtnKeluar4"); // NOI18N
        BtnKeluar4.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluar4ActionPerformed(evt);
            }
        });
        panelBiasa4.add(BtnKeluar4);
        BtnKeluar4.setBounds(430, 80, 100, 30);

        Terapi.setName("Terapi"); // NOI18N
        Terapi.setPreferredSize(new java.awt.Dimension(300, 23));
        panelBiasa4.add(Terapi);
        Terapi.setBounds(150, 40, 340, 23);

        jLabel28.setText("Permintaaan Terapi:");
        jLabel28.setName("jLabel28"); // NOI18N
        jLabel28.setPreferredSize(new java.awt.Dimension(60, 23));
        panelBiasa4.add(jLabel28);
        jLabel28.setBounds(7, 40, 140, 23);

        jLabel29.setText("Diagnosa:");
        jLabel29.setName("jLabel29"); // NOI18N
        jLabel29.setPreferredSize(new java.awt.Dimension(60, 23));
        panelBiasa4.add(jLabel29);
        jLabel29.setBounds(7, 10, 140, 23);

        Diagnosa.setName("Diagnosa"); // NOI18N
        Diagnosa.setPreferredSize(new java.awt.Dimension(300, 23));
        panelBiasa4.add(Diagnosa);
        Diagnosa.setBounds(150, 10, 340, 23);

        BtnCariFisioterapi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnCariFisioterapi.setMnemonic('6');
        BtnCariFisioterapi.setToolTipText("ALt+6");
        BtnCariFisioterapi.setName("BtnCariFisioterapi"); // NOI18N
        BtnCariFisioterapi.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariFisioterapi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariFisioterapiActionPerformed(evt);
            }
        });
        panelBiasa4.add(BtnCariFisioterapi);
        BtnCariFisioterapi.setBounds(500, 30, 28, 23);

        internalFrame5.add(panelBiasa4, java.awt.BorderLayout.CENTER);

        DlgCetak.getContentPane().add(internalFrame5, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ LEMBAR RAWAT JALAN LAYANAN KEDOKTERAN FISIK & REHABILITASI ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        tabRehab.setName("tabRehab"); // NOI18N

        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout());

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));
        Scroll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ScrollMouseClicked(evt);
            }
        });

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 400));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        scrollPane3.setMinimumSize(new java.awt.Dimension(100, 350));
        scrollPane3.setName("scrollPane3"); // NOI18N
        scrollPane3.setPreferredSize(new java.awt.Dimension(102, 350));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 700));
        FormInput.setLayout(null);

        jLabel12.setText("Diagnosis Fungsi:");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(30, 100, 150, 23);

        jLabel13.setText("Diagnosis Medis :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(10, 70, 170, 23);

        jLabel15.setText("Goal of Treatment:");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(20, 380, 135, 23);

        Goal.setFocusTraversalPolicyProvider(true);
        Goal.setName("Goal"); // NOI18N
        Goal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GoalKeyPressed(evt);
            }
        });
        FormInput.add(Goal);
        Goal.setBounds(190, 380, 620, 23);

        jLabel17.setText("Evaluasi:");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(20, 410, 135, 23);

        Evaluasi.setFocusTraversalPolicyProvider(true);
        Evaluasi.setName("Evaluasi"); // NOI18N
        Evaluasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EvaluasiKeyPressed(evt);
            }
        });
        FormInput.add(Evaluasi);
        Evaluasi.setBounds(190, 410, 620, 23);

        jLabel20.setText("Suspek Penyakit Akibat Kerja :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(20, 440, 150, 23);

        SuspekKet.setFocusTraversalPolicyProvider(true);
        SuspekKet.setName("SuspekKet"); // NOI18N
        SuspekKet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SuspekKetKeyPressed(evt);
            }
        });
        FormInput.add(SuspekKet);
        SuspekKet.setBounds(290, 440, 520, 23);

        jLabel24.setText("Anamnesa:");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(10, 10, 170, 23);

        Anamnesa.setFocusTraversalPolicyProvider(true);
        Anamnesa.setName("Anamnesa"); // NOI18N
        Anamnesa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnamnesaKeyPressed(evt);
            }
        });
        FormInput.add(Anamnesa);
        Anamnesa.setBounds(200, 10, 600, 23);

        jLabel25.setText("Pemeriksaan Fisik dan Uji Fungsi:");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(10, 40, 170, 23);

        PemeriksaanFisik.setFocusTraversalPolicyProvider(true);
        PemeriksaanFisik.setName("PemeriksaanFisik"); // NOI18N
        PemeriksaanFisik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanFisikKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanFisik);
        PemeriksaanFisik.setBounds(200, 40, 600, 23);

        jLabel23.setText("Tata Laksana KFR :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(10, 160, 160, 23);

        PemeriksaanPenunjang.setFocusTraversalPolicyProvider(true);
        PemeriksaanPenunjang.setName("PemeriksaanPenunjang"); // NOI18N
        PemeriksaanPenunjang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanPenunjangKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanPenunjang);
        PemeriksaanPenunjang.setBounds(200, 130, 600, 23);

        jLabel26.setText("Pemeriksaan Penunjang:");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(30, 130, 150, 23);

        scrollPane1.setName("scrollPane1"); // NOI18N

        Tatalaksana.setBorder(new javax.swing.border.MatteBorder(null));
        Tatalaksana.setColumns(20);
        Tatalaksana.setRows(5);
        Tatalaksana.setName("Tatalaksana"); // NOI18N
        scrollPane1.setViewportView(Tatalaksana);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(200, 160, 600, 100);

        jLabel27.setText("Anjuran:");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(10, 270, 160, 23);

        scrollPane2.setName("scrollPane2"); // NOI18N

        Anjuran.setBorder(new javax.swing.border.MatteBorder(null));
        Anjuran.setColumns(20);
        Anjuran.setRows(5);
        Anjuran.setName("Anjuran"); // NOI18N
        scrollPane2.setViewportView(Anjuran);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(200, 270, 600, 100);

        Suspek.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        Suspek.setName("Suspek"); // NOI18N
        FormInput.add(Suspek);
        Suspek.setBounds(190, 440, 90, 22);

        DiagnosisFungsi.setFocusTraversalPolicyProvider(true);
        DiagnosisFungsi.setName("DiagnosisFungsi"); // NOI18N
        DiagnosisFungsi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosisFungsiKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosisFungsi);
        DiagnosisFungsi.setBounds(200, 100, 600, 23);

        DiagnosisMedis.setFocusTraversalPolicyProvider(true);
        DiagnosisMedis.setName("DiagnosisMedis"); // NOI18N
        DiagnosisMedis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosisMedisKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosisMedis);
        DiagnosisMedis.setBounds(200, 70, 600, 23);

        scrollPane3.setViewportView(FormInput);

        PanelInput.add(scrollPane3, java.awt.BorderLayout.PAGE_START);

        internalFrame2.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        tabRehab.addTab("Kunjungan Pertama", internalFrame2);

        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout());

        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);
        Scroll1.setPreferredSize(new java.awt.Dimension(452, 200));
        Scroll1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Scroll1MouseClicked(evt);
            }
        });

        tbObat2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbObat2.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat2.setComponentPopupMenu(jPopupMenu2);
        tbObat2.setName("tbObat2"); // NOI18N
        tbObat2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObat2MouseClicked(evt);
            }
        });
        tbObat2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObat2KeyPressed(evt);
            }
        });
        Scroll1.setViewportView(tbObat2);

        internalFrame3.add(Scroll1, java.awt.BorderLayout.CENTER);

        PanelInput1.setName("PanelInput1"); // NOI18N
        PanelInput1.setOpaque(false);
        PanelInput1.setPreferredSize(new java.awt.Dimension(192, 100));
        PanelInput1.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput1.setBackground(new java.awt.Color(250, 255, 245));
        FormInput1.setName("FormInput1"); // NOI18N
        FormInput1.setPreferredSize(new java.awt.Dimension(100, 350));
        FormInput1.setLayout(null);

        jLabel33.setText("Program:");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput1.add(jLabel33);
        jLabel33.setBounds(10, 10, 160, 23);

        Program.setFocusTraversalPolicyProvider(true);
        Program.setName("Program"); // NOI18N
        Program.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ProgramKeyPressed(evt);
            }
        });
        FormInput1.add(Program);
        Program.setBounds(170, 10, 630, 23);

        jLabel34.setText("Fisiotherapist:");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput1.add(jLabel34);
        jLabel34.setBounds(10, 40, 160, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setHighlighter(null);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugasKeyPressed(evt);
            }
        });
        FormInput1.add(KdPetugas);
        KdPetugas.setBounds(174, 40, 100, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        FormInput1.add(NamaPetugas);
        NamaPetugas.setBounds(280, 40, 177, 23);

        btnPetugas1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas1.setMnemonic('2');
        btnPetugas1.setToolTipText("ALt+2");
        btnPetugas1.setName("btnPetugas1"); // NOI18N
        btnPetugas1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugas1ActionPerformed(evt);
            }
        });
        btnPetugas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugas1KeyPressed(evt);
            }
        });
        FormInput1.add(btnPetugas1);
        btnPetugas1.setBounds(460, 40, 28, 23);

        PanelInput1.add(FormInput1, java.awt.BorderLayout.CENTER);

        ChkInput1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput1.setMnemonic('I');
        ChkInput1.setText(".: Input Data");
        ChkInput1.setToolTipText("Alt+I");
        ChkInput1.setBorderPainted(true);
        ChkInput1.setBorderPaintedFlat(true);
        ChkInput1.setFocusable(false);
        ChkInput1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput1.setName("ChkInput1"); // NOI18N
        ChkInput1.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput1.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInput1ActionPerformed(evt);
            }
        });
        PanelInput1.add(ChkInput1, java.awt.BorderLayout.PAGE_END);

        internalFrame3.add(PanelInput1, java.awt.BorderLayout.PAGE_START);

        tabRehab.addTab("Program", internalFrame3);

        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout());

        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);
        Scroll2.setPreferredSize(new java.awt.Dimension(452, 200));
        Scroll2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Scroll2MouseClicked(evt);
            }
        });

        tbObat3.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat3.setComponentPopupMenu(jPopupMenu1);
        tbObat3.setName("tbObat3"); // NOI18N
        tbObat3.setRowHeight(50);
        tbObat3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObat3MouseClicked(evt);
            }
        });
        tbObat3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObat3KeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbObat3);

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel5.setLayout(new java.awt.BorderLayout(1, 1));
        Scroll2.setViewportView(jPanel5);

        internalFrame4.add(Scroll2, java.awt.BorderLayout.CENTER);

        PanelInput2.setName("PanelInput2"); // NOI18N
        PanelInput2.setOpaque(false);
        PanelInput2.setPreferredSize(new java.awt.Dimension(192, 150));
        PanelInput2.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput2.setBackground(new java.awt.Color(250, 255, 245));
        FormInput2.setName("FormInput2"); // NOI18N
        FormInput2.setPreferredSize(new java.awt.Dimension(100, 350));
        FormInput2.setLayout(null);

        jLabel46.setText("Hasil:");
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput2.add(jLabel46);
        jLabel46.setBounds(10, 10, 160, 23);

        Hasil.setFocusTraversalPolicyProvider(true);
        Hasil.setName("Hasil"); // NOI18N
        Hasil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilKeyPressed(evt);
            }
        });
        FormInput2.add(Hasil);
        Hasil.setBounds(170, 10, 630, 23);

        jLabel47.setText("Pemeriksaan Fisik dan Uji Fungsi:");
        jLabel47.setName("jLabel47"); // NOI18N
        FormInput2.add(jLabel47);
        jLabel47.setBounds(10, 40, 340, 23);

        PxFisikUjiFungsi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pasien memerlukan tambahan siklus terapi/ assessment baru", "Pasien kontrol kembali ke DPJP perujuk", "Pasien kembali ke FKTP" }));
        PxFisikUjiFungsi.setName("PxFisikUjiFungsi"); // NOI18N
        FormInput2.add(PxFisikUjiFungsi);
        PxFisikUjiFungsi.setBounds(360, 40, 430, 22);

        jLabel35.setText("Fisiotherapist:");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput2.add(jLabel35);
        jLabel35.setBounds(90, 70, 160, 23);

        KdPetugas1.setEditable(false);
        KdPetugas1.setHighlighter(null);
        KdPetugas1.setName("KdPetugas1"); // NOI18N
        KdPetugas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugas1KeyPressed(evt);
            }
        });
        FormInput2.add(KdPetugas1);
        KdPetugas1.setBounds(260, 70, 100, 23);

        NamaPetugas1.setEditable(false);
        NamaPetugas1.setName("NamaPetugas1"); // NOI18N
        FormInput2.add(NamaPetugas1);
        NamaPetugas1.setBounds(360, 70, 177, 23);

        btnPetugas2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas2.setMnemonic('2');
        btnPetugas2.setToolTipText("ALt+2");
        btnPetugas2.setName("btnPetugas2"); // NOI18N
        btnPetugas2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugas2ActionPerformed(evt);
            }
        });
        btnPetugas2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugas2KeyPressed(evt);
            }
        });
        FormInput2.add(btnPetugas2);
        btnPetugas2.setBounds(540, 70, 28, 23);

        PanelInput2.add(FormInput2, java.awt.BorderLayout.CENTER);

        ChkInput2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput2.setMnemonic('I');
        ChkInput2.setText(".: Input Data");
        ChkInput2.setToolTipText("Alt+I");
        ChkInput2.setBorderPainted(true);
        ChkInput2.setBorderPaintedFlat(true);
        ChkInput2.setFocusable(false);
        ChkInput2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput2.setName("ChkInput2"); // NOI18N
        ChkInput2.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput2.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput2.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInput2ActionPerformed(evt);
            }
        });
        PanelInput2.add(ChkInput2, java.awt.BorderLayout.PAGE_END);

        internalFrame4.add(PanelInput2, java.awt.BorderLayout.PAGE_START);

        tabRehab.addTab("Achievent of Goals ( Tahap Pengembangan dari IT)", internalFrame4);

        internalFrame1.add(tabRehab, java.awt.BorderLayout.CENTER);

        panelBiasa1.setName("panelBiasa1"); // NOI18N
        panelBiasa1.setPreferredSize(new java.awt.Dimension(260, 80));
        panelBiasa1.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        panelBiasa1.add(jLabel4);
        jLabel4.setBounds(0, 10, 75, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        panelBiasa1.add(TNoRw);
        TNoRw.setBounds(79, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        panelBiasa1.add(TPasien);
        TPasien.setBounds(336, 10, 240, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-02-2025" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        panelBiasa1.add(Tanggal);
        Tanggal.setBounds(79, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        panelBiasa1.add(TNoRM);
        TNoRM.setBounds(222, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        panelBiasa1.add(jLabel16);
        jLabel16.setBounds(0, 40, 75, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        panelBiasa1.add(Jam);
        Jam.setBounds(173, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        panelBiasa1.add(Menit);
        Menit.setBounds(238, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        panelBiasa1.add(Detik);
        Detik.setBounds(303, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        panelBiasa1.add(ChkKejadian);
        ChkKejadian.setBounds(368, 40, 23, 23);

        jLabel18.setText("Dokter Sp.RM :");
        jLabel18.setName("jLabel18"); // NOI18N
        panelBiasa1.add(jLabel18);
        jLabel18.setBounds(400, 40, 80, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        panelBiasa1.add(KdDokter);
        KdDokter.setBounds(484, 40, 94, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        panelBiasa1.add(NmDokter);
        NmDokter.setBounds(580, 40, 177, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("ALt+2");
        btnPetugas.setName("btnPetugas"); // NOI18N
        btnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasActionPerformed(evt);
            }
        });
        btnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugasKeyPressed(evt);
            }
        });
        panelBiasa1.add(btnPetugas);
        btnPetugas.setBounds(761, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        panelBiasa1.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        panelBiasa1.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        JK.setEditable(false);
        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N
        JK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JKKeyPressed(evt);
            }
        });
        panelBiasa1.add(JK);
        JK.setBounds(578, 10, 50, 23);

        internalFrame1.add(panelBiasa1, java.awt.BorderLayout.PAGE_START);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-02-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-02-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(320, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            isRawat();
            isPsien();
        } else {
            Valid.pindah(evt, TCari, Tanggal);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt, TCari, BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdDokter.getText().trim().equals("") || NmDokter.getText().trim().equals("")) {
                    Valid.textKosong(KdDokter, "Dokter");
                } else if (Anamnesa.getText().trim().equals("")) {
                    Valid.textKosong(Anamnesa, "Anamnesa");
                } else if (PemeriksaanFisik.getText().trim().equals("")) {
                    Valid.textKosong(PemeriksaanFisik, "Pemeriksaan Fisik");
                } else if (DiagnosisMedis.getText().trim().equals("")) {
                    Valid.textKosong(DiagnosisMedis, "Diagnosis Medis");
                } else if (DiagnosisFungsi.getText().trim().equals("")) {
                    Valid.textKosong(DiagnosisFungsi, "Diagnosis Fungsi");
                } else if (PemeriksaanPenunjang.getText().trim().equals("")) {
                    Valid.textKosong(PemeriksaanPenunjang, "Pemeriksaan Penunjang");
                } else if (Tatalaksana.getText().trim().equals("")) {
                    Valid.textKosong(Tatalaksana, "Tatalaksana");
                } else if (Anjuran.getText().trim().equals("")) {
                    Valid.textKosong(Anjuran, "Anjuran");
                } else if (Goal.getText().trim().equals("")) {
                    Valid.textKosong(Goal, "Goal");
                } else if (Evaluasi.getText().trim().equals("")) {
                    Valid.textKosong(Evaluasi, "Evaluasi");
                } else {
                    if (Sequel.menyimpantf("rehab_kunjunganpertama", "?,?,?,?,?,?,?,?,?,?,?,?,?,?", "Data", 14, new String[]{
                        TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDokter.getText(), Anamnesa.getText(), PemeriksaanFisik.getText(), DiagnosisMedis.getText(), DiagnosisFungsi.getText(), PemeriksaanPenunjang.getText(),
                        Tatalaksana.getText(), Anjuran.getText(), Goal.getText(), Evaluasi.getText(), Suspek.getSelectedItem().toString(), SuspekKet.getText()
                    }) == true) {
                        tampil();
                        emptTeks();
                    }
                }

                break;
            case 1:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdPetugas.getText().trim().equals("") || NamaPetugas.getText().trim().equals("")) {
                    Valid.textKosong(KdPetugas, "Petugas");
                } else if (Program.getText().trim().equals("")) {
                    Valid.textKosong(Program, "Program");
                } else {
                    if (Sequel.menyimpantf("rehab_program", "?,?,?,?,?", "Data", 5, new String[]{
                        TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDokter.getText(), Program.getText(), KdPetugas.getText()
                    }) == true) {
                        tampil2();
                        emptTeks();
                    }
                }
                break;
            case 2:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdPetugas1.getText().trim().equals("") || NamaPetugas1.getText().trim().equals("")) {
                    Valid.textKosong(KdPetugas1, "Petugas");
                } else if (Hasil.getText().trim().equals("")) {
                    Valid.textKosong(Hasil, "Hasil");
                } else {
                    if (Sequel.menyimpantf("rehab_goal", "?,?,?,?,?,?", "Data", 6, new String[]{
                        TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDokter.getText(), Hasil.getText(), PxFisikUjiFungsi.getSelectedItem().toString(), KdPetugas1.getText()
                    }) == true) {
                        tampil2();
                        emptTeks();
                    }
                }
                break;
            default:
        }


}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            //Valid.pindah(evt,cmbSkor3,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
}//GEN-LAST:event_BtnBatalKeyPressed


    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                if (tbObat.getSelectedRow() != -1) {
                    if (akses.getkode().equals("Admin Utama")) {
                        hapus();
                    } else {
                        if (KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString())) {
                            hapus();
                        } else {
                            JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                }
                break;
            case 1:
                if (tbObat2.getSelectedRow() != -1) {
                    if (akses.getkode().equals("Admin Utama")) {
                        hapus2();
                    } else {
                        if (KdDokter.getText().equals(tbObat2.getValueAt(tbObat2.getSelectedRow(), 7).toString())) {
                            hapus2();
                        } else {
                            JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                }
                break;
            case 2:
                if (tbObat3.getSelectedRow() != -1) {
                    if (akses.getkode().equals("Admin Utama")) {
                        hapus3();
                    } else {
                        if (KdDokter.getText().equals(tbObat3.getValueAt(tbObat3.getSelectedRow(), 7).toString())) {
                            hapus3();
                        } else {
                            JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                }
                break;
            default:
        }

}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdDokter.getText().trim().equals("") || NmDokter.getText().trim().equals("")) {
                    Valid.textKosong(KdDokter, "Dokter");
                } else if (Anamnesa.getText().trim().equals("")) {
                    Valid.textKosong(Anamnesa, "Anamnesa");
                } else if (PemeriksaanFisik.getText().trim().equals("")) {
                    Valid.textKosong(PemeriksaanFisik, "Pemeriksaan Fisik");
                } else if (DiagnosisMedis.getText().trim().equals("")) {
                    Valid.textKosong(DiagnosisMedis, "Diagnosis Medis");
                } else if (DiagnosisFungsi.getText().trim().equals("")) {
                    Valid.textKosong(DiagnosisFungsi, "Diagnosis Fungsi");
                } else if (PemeriksaanPenunjang.getText().trim().equals("")) {
                    Valid.textKosong(PemeriksaanPenunjang, "Pemeriksaan Penunjang");
                } else if (Tatalaksana.getText().trim().equals("")) {
                    Valid.textKosong(Tatalaksana, "Tatalaksana");
                } else if (Anjuran.getText().trim().equals("")) {
                    Valid.textKosong(Anjuran, "Anjuran");
                } else if (Goal.getText().trim().equals("")) {
                    Valid.textKosong(Goal, "Goal");
                } else if (Evaluasi.getText().trim().equals("")) {
                    Valid.textKosong(Evaluasi, "Evaluasi");
                } else {
                    if (tbObat.getSelectedRow() > -1) {
                        if (akses.getkode().equals("Admin Utama")) {
                            ganti();
                        } else {
                            if (KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString())) {
                                ganti();
                            } else {
                                JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                    }
                }
                break;
            case 1:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdPetugas.getText().trim().equals("") || NamaPetugas.getText().trim().equals("")) {
                    Valid.textKosong(KdPetugas, "Petugas");
                } else if (Program.getText().trim().equals("")) {
                    Valid.textKosong(Program, "Program");
                } else {
                    if (tbObat2.getSelectedRow() > -1) {
                        if (akses.getkode().equals("Admin Utama")) {
                            ganti2();
                        } else {
                            if (KdDokter.getText().equals(tbObat2.getValueAt(tbObat2.getSelectedRow(), 6).toString())) {
                                ganti2();
                            } else {
                                JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                    }
                }
                break;
            case 2:
                if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
                    Valid.textKosong(TNoRw, "Pasien");
                } else if (KdPetugas.getText().trim().equals("") || NamaPetugas.getText().trim().equals("")) {
                    Valid.textKosong(KdPetugas, "Petugas");
                } else if (Hasil.getText().trim().equals("")) {
                    Valid.textKosong(Hasil, "Hasil");
                } else {
                    if (tbObat3.getSelectedRow() > -1) {
                        if (akses.getkode().equals("Admin Utama")) {
                            ganti3();
                        } else {
                            if (KdDokter.getText().equals(tbObat3.getValueAt(tbObat3.getSelectedRow(), 6).toString())) {
                                ganti3();
                            } else {
                                JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
                    }
                }
                break;
            default:
        }


}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dokter.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnKeluarActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (tabMode.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    BtnBatal.requestFocus();
                } else if (tabMode.getRowCount() != 0) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("namars", akses.getnamars());
                    param.put("alamatrs", akses.getalamatrs());
                    param.put("kotars", akses.getkabupatenrs());
                    param.put("propinsirs", akses.getpropinsirs());
                    param.put("kontakrs", akses.getkontakrs());
                    param.put("emailrs", akses.getemailrs());
                    param.put("url", akses.getemailrs());
                    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                    if (TCari.getText().trim().equals("")) {
                        Valid.MyReportqry("rptUjiFungsiKFR.jasper", "report", "::[ Data Uji Fugsi/Prosedur KFR ]::",
                                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                                + "pasien.jk,rehab_kunjunganpertama.tanggal,rehab_kunjunganpertama.diagnosis_fungsional,rehab_kunjunganpertama.diagnosis_medis,rehab_kunjunganpertama.hasil_didapat,"
                                + "rehab_kunjunganpertama.kesimpulan,rehab_kunjunganpertama.rekomedasi,rehab_kunjunganpertama.kd_dokter,dokter.nm_dokter,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                                + "from rehab_kunjunganpertama inner join reg_periksa on rehab_kunjunganpertama.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join dokter on rehab_kunjunganpertama.kd_dokter=dokter.kd_dokter where "
                                + "rehab_kunjunganpertama.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' order by rehab_kunjunganpertama.tanggal ", param);
                    } else {
                        Valid.MyReportqry("rptUjiFungsiKFR.jasper", "report", "::[ Data Uji Fugsi/Prosedur KFR ]::",
                                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                                + "pasien.jk,rehab_kunjunganpertama.tanggal,rehab_kunjunganpertama.diagnosis_fungsional,rehab_kunjunganpertama.diagnosis_medis,rehab_kunjunganpertama.hasil_didapat,"
                                + "rehab_kunjunganpertama.kesimpulan,rehab_kunjunganpertama.rekomedasi,rehab_kunjunganpertama.kd_dokter,dokter.nm_dokter,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                                + "from rehab_kunjunganpertama inner join reg_periksa on rehab_kunjunganpertama.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join dokter on rehab_kunjunganpertama.kd_dokter=dokter.kd_dokter "
                                + "where rehab_kunjunganpertama.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' and "
                                + "(reg_periksa.no_rawat like '%" + TCari.getText().trim() + "%' or pasien.no_rkm_medis like '%" + TCari.getText().trim() + "%' or pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                                + "rehab_kunjunganpertama.kd_dokter like '%" + TCari.getText().trim() + "%' or dokter.nm_dokter like '%" + TCari.getText().trim() + "%') order by rehab_kunjunganpertama.tanggal ", param);
                    }
                }
                this.setCursor(Cursor.getDefaultCursor());
                break;
            case 1:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                DlgCetak.setLocationRelativeTo(internalFrame1);
                DlgCetak.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
                break;
            case 2:
                break;
            default:

        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                tampil();
                break;
            case 1:
                tampil2();
                break;
            case 2:
                tampil3();
                break;
            default:
        }
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            tampil();
            TCari.setText("");
        } else {
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, TCari, Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        // Valid.pindah(evt, TNm, BtnSimpan);
}//GEN-LAST:event_TNoRMKeyPressed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt, Tanggal, Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt, Jam, Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt, Menit, btnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            NmDokter.setText(dokter.tampil3(KdDokter.getText()));
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            Detik.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            //Alergi.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            btnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_KdDokterKeyPressed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        dokter.emptTeks();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        //    Valid.pindah(evt,Detik,KdDiagnosisFungsional);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void MnCetakLembarFormulirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakLembarFormulirActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

            try {
                param.put("tandatangan", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/imagefreehand/rehabmedik/pertama/ttdrehabpertama" + TNoRw.getText().replaceAll("/", "") + ".jpg");
            } catch (Exception e) {
            }

            finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString() + "\nID " + (finger.equals("") ? tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString() : finger) + "\n" + Tanggal.getSelectedItem());
            Valid.MyReportqry("rptCetakLembarFisioterapiKunjunganPertama.jasper", "report", "::[ Lembar Formulir Rawat Jalan Layanan Kedokteran Fisik dan Rehabilitasi ]::",
                    "SELECT rehab_kunjunganpertama.no_rawat, DATE_FORMAT(rehab_kunjunganpertama.tanggal,'%d-%m-%Y')as tanggal, rehab_kunjunganpertama.kd_dokter, rehab_kunjunganpertama.anamnesa, "
                    + "rehab_kunjunganpertama.pemeriksaanfisik, rehab_kunjunganpertama.diagnosismedis, rehab_kunjunganpertama.diagnosisfungsi, rehab_kunjunganpertama.penunjang, "
                    + "rehab_kunjunganpertama.tatalaksana, rehab_kunjunganpertama.anjuran, rehab_kunjunganpertama.goal, rehab_kunjunganpertama.evaluasi, rehab_kunjunganpertama.suspek, "
                    + "rehab_kunjunganpertama.suspekket, reg_periksa.no_rkm_medis,reg_periksa.almt_pj, pasien.nm_pasien, pasien.jk, DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y')as tgl_lahir,pasien.no_tlp,dokter.nm_dokter FROM rehab_kunjunganpertama INNER JOIN "
                    + "reg_periksa ON rehab_kunjunganpertama.no_rawat = reg_periksa.no_rawat INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN dokter ON "
                    + "rehab_kunjunganpertama.kd_dokter = dokter.kd_dokter where reg_periksa.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'", param);
        }
    }//GEN-LAST:event_MnCetakLembarFormulirActionPerformed

    private void JKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JKKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JKKeyPressed

    private void MnTTDPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnTTDPasienActionPerformed
        if (TNoRw.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DlgTTDPasien ttd = new DlgTTDPasien(null, false);
            ttd.setNoRm(TNoRw.getText(), TNoRM.getText(), TPasien.getText(), "imagefreehand/rehabmedik/pertama/", "RK" + TNoRw.getText().replaceAll("/", "") + ".png");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.width * 0.55);  // 20% dari lebar layar
            int height = (int) (screenSize.height * 0.55); // 20% dari tinggi layar

            // Mengatur ukuran dialog
            ttd.setSize(width, height);
            ttd.setLocationRelativeTo(null); // Memposisikan di tengah layar
            ttd.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnTTDPasienActionPerformed

    private void MnTTDPasien2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnTTDPasien2ActionPerformed
        if (TNoRw.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DlgTTDPasienUrl ttd = new DlgTTDPasienUrl(null, false);
            ttd.setNoRm(TNoRw.getText(), TNoRM.getText(), TPasien.getText(), "imagefreehand/rehabmedik/program/", "RP" + TNoRw.getText().replaceAll("/", "") + ".png");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.width * 0.30);  // 20% dari lebar layar
            int height = (int) (screenSize.height * 0.30); // 20% dari tinggi layar

            // Mengatur ukuran dialog
            ttd.setSize(width, height);
            ttd.setLocationRelativeTo(null); // Memposisikan di tengah layar
            ttd.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnTTDPasien2ActionPerformed

    private void MnCetakLembarProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakLembarProgramActionPerformed
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (tabMode.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    BtnBatal.requestFocus();
                } else if (tabMode.getRowCount() != 0) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("namars", akses.getnamars());
                    param.put("alamatrs", akses.getalamatrs());
                    param.put("kotars", akses.getkabupatenrs());
                    param.put("propinsirs", akses.getpropinsirs());
                    param.put("kontakrs", akses.getkontakrs());
                    param.put("emailrs", akses.getemailrs());
                    param.put("url", akses.getemailrs());
                    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                    if (TCari.getText().trim().equals("")) {
                        Valid.MyReportqry("rptUjiFungsiKFR.jasper", "report", "::[ Data Uji Fugsi/Prosedur KFR ]::",
                                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                                + "pasien.jk,rehab_kunjunganpertama.tanggal,rehab_kunjunganpertama.diagnosis_fungsional,rehab_kunjunganpertama.diagnosis_medis,rehab_kunjunganpertama.hasil_didapat,"
                                + "rehab_kunjunganpertama.kesimpulan,rehab_kunjunganpertama.rekomedasi,rehab_kunjunganpertama.kd_dokter,dokter.nm_dokter,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                                + "from rehab_kunjunganpertama inner join reg_periksa on rehab_kunjunganpertama.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join dokter on rehab_kunjunganpertama.kd_dokter=dokter.kd_dokter where "
                                + "rehab_kunjunganpertama.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' order by rehab_kunjunganpertama.tanggal ", param);
                    } else {
                        Valid.MyReportqry("rptUjiFungsiKFR.jasper", "report", "::[ Data Uji Fugsi/Prosedur KFR ]::",
                                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                                + "pasien.jk,rehab_kunjunganpertama.tanggal,rehab_kunjunganpertama.diagnosis_fungsional,rehab_kunjunganpertama.diagnosis_medis,rehab_kunjunganpertama.hasil_didapat,"
                                + "rehab_kunjunganpertama.kesimpulan,rehab_kunjunganpertama.rekomedasi,rehab_kunjunganpertama.kd_dokter,dokter.nm_dokter,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                                + "from rehab_kunjunganpertama inner join reg_periksa on rehab_kunjunganpertama.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join dokter on rehab_kunjunganpertama.kd_dokter=dokter.kd_dokter "
                                + "where rehab_kunjunganpertama.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' and "
                                + "(reg_periksa.no_rawat like '%" + TCari.getText().trim() + "%' or pasien.no_rkm_medis like '%" + TCari.getText().trim() + "%' or pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                                + "rehab_kunjunganpertama.kd_dokter like '%" + TCari.getText().trim() + "%' or dokter.nm_dokter like '%" + TCari.getText().trim() + "%') order by rehab_kunjunganpertama.tanggal ", param);
                    }
                }
                this.setCursor(Cursor.getDefaultCursor());
                break;
            case 1:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                DlgCetak.setLocationRelativeTo(internalFrame1);
                DlgCetak.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
                break;
            case 2:
                break;
            default:

        }
    }//GEN-LAST:event_MnCetakLembarProgramActionPerformed

    private void BtnPrint5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint5ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode2.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        } else if (tabMode2.getRowCount() != 0) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("diagnosa", Diagnosa.getText());
            param.put("terapi", Terapi.getText());
            try {
                param.put("url", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/imagefreehand/");
            } catch (Exception e) {
            }

            finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", KdDokter.getText());
            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + NmDokter.getText() + "\nID " + (finger.equals("") ? KdDokter.getText() : finger) + "\n Tanggal" + Tanggal.getSelectedItem());
            if (TCari.getText().trim().equals("")) {
                Valid.MyReportqry("rptCetakLembarFisioterapiProgram.jasper", "report", "::[ Cetak Program Fisioterapi ]::",
                        "SELECT reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, DATE_FORMAT(rehab_program.tanggal,'%d-%m-%Y')as tanggal, rehab_program.kd_dokter, rehab_program.program, rehab_program.kdpetugas, "
                        + "dokter.nm_dokter, petugas.nama,rehab_program_image_marking.url_image FROM reg_periksa INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN rehab_program ON reg_periksa.no_rawat = "
                        + "rehab_program.no_rawat INNER JOIN dokter ON rehab_program.kd_dokter = dokter.kd_dokter INNER JOIN petugas ON rehab_program.kdpetugas = petugas.nip INNER JOIN rehab_program_image_marking ON rehab_program_image_marking.no_rawat=rehab_program.no_rawat "
                        + "where rehab_program.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' and reg_periksa.no_rkm_medis = '" + TNoRM.getText() + "' order by rehab_program.tanggal ", param);
            } else {
                Valid.MyReportqry("rptCetakLembarFisioterapiProgram.jasper", "report", "::[ Cetak Program Fisioterapi ]::",
                        "SELECT reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, DATE_FORMAT(rehab_program.tanggal,'%d-%m-%Y')as tanggal, rehab_program.kd_dokter, rehab_program.program, rehab_program.kdpetugas, "
                        + "dokter.nm_dokter, petugas.nama,rehab_program_image_marking.url_image FROM reg_periksa INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN rehab_program ON reg_periksa.no_rawat = "
                        + "rehab_program.no_rawat INNER JOIN dokter ON rehab_program.kd_dokter = dokter.kd_dokter INNER JOIN petugas ON rehab_program.kdpetugas = petugas.nip INNER JOIN rehab_program_image_marking ON rehab_program_image_marking.no_rawat=rehab_program.no_rawat where reg_periksa.no_rkm_medis='" + TNoRM.getText() + "' "
                        + "and rehab_program.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' and "
                        + "(reg_periksa.no_rawat like '%" + TCari.getText().trim() + "%' or pasien.no_rkm_medis like '%" + TCari.getText().trim() + "%' or pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                        + "rehab_program.kd_dokter like '%" + TCari.getText().trim() + "%' or dokter.nm_dokter like '%" + TCari.getText().trim() + "%') order by rehab_program.tanggal ", param);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrint5ActionPerformed

    private void BtnKeluar4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluar4ActionPerformed
        DlgCetak.dispose();
    }//GEN-LAST:event_BtnKeluar4ActionPerformed

    private void BtnCariFisioterapiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariFisioterapiActionPerformed
        fisioterapi.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        fisioterapi.setRM(TNoRM.getText());
        fisioterapi.setLocationRelativeTo(internalFrame1);
        fisioterapi.setVisible(true);
    }//GEN-LAST:event_BtnCariFisioterapiActionPerformed

    private void ChkInput2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInput2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChkInput2ActionPerformed

    private void btnPetugas2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugas2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPetugas2KeyPressed

    private void btnPetugas2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugas2ActionPerformed
        pilihan = 2;
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugas2ActionPerformed

    private void KdPetugas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugas1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdPetugas1KeyPressed

    private void HasilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_HasilKeyPressed

    private void Scroll2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Scroll2MouseClicked
        if (tabMode3.getRowCount() != 0) {
            try {
                getData3();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_Scroll2MouseClicked

    private void tbObat3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObat3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbObat3KeyPressed

    private void tbObat3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObat3MouseClicked
        if (tabMode3.getRowCount() != 0) {
            try {
                getData3();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbObat3MouseClicked

    private void ChkInput1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInput1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChkInput1ActionPerformed

    private void btnPetugas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugas1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPetugas1KeyPressed

    private void btnPetugas1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugas1ActionPerformed
        pilihan = 1;
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugas1ActionPerformed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdPetugasKeyPressed

    private void ProgramKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ProgramKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ProgramKeyPressed

    private void Scroll1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Scroll1MouseClicked

        if (tabMode2.getRowCount() != 0) {
            try {
                getData2();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_Scroll1MouseClicked

    private void tbObat2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObat2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbObat2KeyPressed

    private void tbObat2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObat2MouseClicked
        if (tabMode2.getRowCount() != 0) {
            try {
                getData2();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbObat2MouseClicked

    private void DiagnosisMedisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosisMedisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DiagnosisMedisKeyPressed

    private void DiagnosisFungsiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosisFungsiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DiagnosisFungsiKeyPressed

    private void PemeriksaanPenunjangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanPenunjangKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemeriksaanPenunjangKeyPressed

    private void PemeriksaanFisikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanFisikKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemeriksaanFisikKeyPressed

    private void AnamnesaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AnamnesaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_AnamnesaKeyPressed

    private void SuspekKetKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SuspekKetKeyPressed
        Valid.pindah(evt, Evaluasi, BtnSimpan);
    }//GEN-LAST:event_SuspekKetKeyPressed

    private void EvaluasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EvaluasiKeyPressed
        Valid.pindah(evt, Goal, SuspekKet);
    }//GEN-LAST:event_EvaluasiKeyPressed

    private void GoalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GoalKeyPressed
        //     Valid.pindah(evt,KdDiagnosisMedis,Evaluasi);
    }//GEN-LAST:event_GoalKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void ScrollMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ScrollMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_ScrollMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbObatMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMLayananKedokteranFisikRehabilitasi dialog = new RMLayananKedokteranFisikRehabilitasi(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.TextBox Anamnesa;
    private widget.TextArea Anjuran;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariFisioterapi;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnKeluar4;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint5;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkInput1;
    private widget.CekBox ChkInput2;
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.TextBox Diagnosa;
    private widget.TextBox DiagnosisFungsi;
    private widget.TextBox DiagnosisMedis;
    private javax.swing.JDialog DlgCetak;
    private widget.TextBox Evaluasi;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormInput1;
    private widget.PanelBiasa FormInput2;
    private widget.TextBox Goal;
    private widget.TextBox Hasil;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPetugas;
    private widget.TextBox KdPetugas1;
    private widget.Label LCount;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnCetakLembarFormulir;
    private javax.swing.JMenuItem MnCetakLembarProgram;
    private javax.swing.JMenuItem MnTTDPasien;
    private javax.swing.JMenuItem MnTTDPasien2;
    private widget.TextBox NamaPetugas;
    private widget.TextBox NamaPetugas1;
    private widget.TextBox NmDokter;
    private javax.swing.JPanel PanelInput;
    private javax.swing.JPanel PanelInput1;
    private javax.swing.JPanel PanelInput2;
    private widget.TextBox PemeriksaanFisik;
    private widget.TextBox PemeriksaanPenunjang;
    private widget.TextBox Program;
    private widget.ComboBox PxFisikUjiFungsi;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.ComboBox Suspek;
    private widget.TextBox SuspekKet;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextArea Tatalaksana;
    private widget.TextBox Terapi;
    private widget.TextBox TglLahir;
    private widget.Button btnPetugas;
    private widget.Button btnPetugas1;
    private widget.Button btnPetugas2;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.InternalFrame internalFrame5;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel4;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private widget.PanelBiasa panelBiasa1;
    private widget.PanelBiasa panelBiasa4;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.TabPane tabRehab;
    private widget.Table tbObat;
    private widget.Table tbObat2;
    private widget.Table tbObat3;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().toString().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "SELECT rehab_kunjunganpertama.no_rawat, rehab_kunjunganpertama.tanggal, rehab_kunjunganpertama.kd_dokter, rehab_kunjunganpertama.anamnesa, "
                        + "rehab_kunjunganpertama.pemeriksaanfisik, rehab_kunjunganpertama.diagnosismedis, rehab_kunjunganpertama.diagnosisfungsi, rehab_kunjunganpertama.penunjang, "
                        + "rehab_kunjunganpertama.tatalaksana, rehab_kunjunganpertama.anjuran, rehab_kunjunganpertama.goal, rehab_kunjunganpertama.evaluasi, rehab_kunjunganpertama.suspek, "
                        + "rehab_kunjunganpertama.suspekket, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, dokter.nm_dokter FROM rehab_kunjunganpertama INNER JOIN "
                        + "reg_periksa ON rehab_kunjunganpertama.no_rawat = reg_periksa.no_rawat INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN dokter ON "
                        + "rehab_kunjunganpertama.kd_dokter = dokter.kd_dokter where "
                        + "rehab_kunjunganpertama.tanggal between ? and ? order by rehab_kunjunganpertama.tanggal ");
            } else {
                ps = koneksi.prepareStatement(
                        "SELECT rehab_kunjunganpertama.no_rawat, rehab_kunjunganpertama.tanggal, rehab_kunjunganpertama.kd_dokter, rehab_kunjunganpertama.anamnesa, "
                        + "rehab_kunjunganpertama.pemeriksaanfisik, rehab_kunjunganpertama.diagnosismedis, rehab_kunjunganpertama.diagnosisfungsi, rehab_kunjunganpertama.penunjang, "
                        + "rehab_kunjunganpertama.tatalaksana, rehab_kunjunganpertama.anjuran, rehab_kunjunganpertama.goal, rehab_kunjunganpertama.evaluasi, rehab_kunjunganpertama.suspek, "
                        + "rehab_kunjunganpertama.suspekket, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, dokter.nm_dokter FROM rehab_kunjunganpertama INNER JOIN "
                        + "reg_periksa ON rehab_kunjunganpertama.no_rawat = reg_periksa.no_rawat INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN dokter ON "
                        + "rehab_kunjunganpertama.kd_dokter = dokter.kd_dokter where "
                        + "rehab_kunjunganpertama.tanggal between ? and ? and "
                        + "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or rehab_kunjunganpertama.kd_dokter like ? or dokter.nm_dokter like ?) "
                        + "order by rehab_kunjunganpertama.tanggal ");
            }

            try {
                if (TCari.getText().toString().trim().equals("")) {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                } else {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getString("jk"), rs.getString("tgl_lahir"), rs.getString("tanggal"),
                        rs.getString("kd_dokter"), rs.getString("nm_dokter"), rs.getString("anamnesa"),
                        rs.getString("pemeriksaanfisik"), rs.getString("diagnosismedis"), rs.getString("diagnosisfungsi"),
                        rs.getString("penunjang"), rs.getString("tatalaksana"), rs.getString("anjuran"),
                        rs.getString("goal"), rs.getString("evaluasi"), rs.getString("suspek"), rs.getString("suspekket")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void tampil2() {
        Valid.tabelKosong(tabMode2); // Kosongkan tabel terlebih dahulu
        try {
            // Tentukan query sesuai kondisi
            if (TCari.getText().toString().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "SELECT reg_periksa.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, rehab_program.tanggal, rehab_program.kd_dokter, "
                        + "rehab_program.program, rehab_program.kdpetugas, dokter.nm_dokter, petugas.nama FROM reg_periksa "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN rehab_program ON reg_periksa.no_rawat = rehab_program.no_rawat "
                        + "INNER JOIN dokter ON rehab_program.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN petugas on rehab_program.kdpetugas = petugas.nip "
                        + "WHERE rehab_program.tanggal BETWEEN ? AND ? ORDER BY rehab_program.tanggal"
                );
            } else {
                ps = koneksi.prepareStatement(
                        "SELECT reg_periksa.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, rehab_program.tanggal, rehab_program.kd_dokter, "
                        + "rehab_program.program, rehab_program.kdpetugas, dokter.nm_dokter, petugas.nama FROM reg_periksa "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN rehab_program ON reg_periksa.no_rawat = rehab_program.no_rawat "
                        + "INNER JOIN dokter ON rehab_program.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN petugas on rehab_program.kdpetugas = petugas.nip "
                        + "WHERE rehab_program.tanggal BETWEEN ? AND ? "
                        + "AND (reg_periksa.no_rawat LIKE ? OR pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? "
                        + "OR rehab_program.kd_dokter LIKE ? OR dokter.nm_dokter LIKE ?) "
                        + "ORDER BY rehab_program.tanggal"
                );
            }

            // Set parameter query
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                if (!TCari.getText().toString().trim().equals("")) {
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                }

                // Debugging: Print query
                // System.out.println("Executing query: " + ps.toString());
                // Eksekusi query
                rs = ps.executeQuery();

                // Cek apakah ada data
                if (!rs.isBeforeFirst()) {
                    System.out.println("No data found for the given parameters.");
                }

                // Tambahkan data ke tabel
                while (rs.next()) {
                    tabMode2.addRow(new String[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("jk"),
                        rs.getString("tgl_lahir"),
                        rs.getString("tanggal"),
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter"),
                        rs.getString("program"),
                        rs.getString("kdpetugas"),
                        rs.getString("nama")
                    });
                }
            } catch (Exception e) {
                System.out.println("Error executing query: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Tutup ResultSet dan PreparedStatement
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error preparing statement: " + e.getMessage());
            e.printStackTrace();
        }

        // Set jumlah baris ke label
        LCount.setText("" + tabMode2.getRowCount());
    }

    private void tampil3() {
        Valid.tabelKosong(tabMode3); // Kosongkan tabel terlebih dahulu
        try {
            // Tentukan query sesuai kondisi
            if (TCari.getText().toString().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "SELECT reg_periksa.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, rehab_goal.tanggal, rehab_goal.kd_dokter, "
                        + "rehab_goal.hasil, rehab_goal.pemeriksaanfisik, rehab_goal.kdpetugas, dokter.nm_dokter, petugas.nip, petugas.nama FROM reg_periksa INNER JOIN "
                        + "pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN rehab_goal ON reg_periksa.no_rawat = rehab_goal.no_rawat INNER JOIN dokter ON "
                        + "rehab_goal.kd_dokter = dokter.kd_dokter INNER JOIN petugas ON rehab_goal.kdpetugas = petugas.nip "
                        + "WHERE rehab_goal.tanggal BETWEEN ? AND ? ORDER BY rehab_goal.tanggal"
                );
            } else {
                ps = koneksi.prepareStatement(
                        "SELECT reg_periksa.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, rehab_goal.tanggal, rehab_goal.kd_dokter, "
                        + "rehab_goal.hasil, rehab_goal.pemeriksaanfisik, rehab_goal.kdpetugas, dokter.nm_dokter, petugas.nip, petugas.nama FROM reg_periksa INNER JOIN "
                        + "pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis INNER JOIN rehab_goal ON reg_periksa.no_rawat = rehab_goal.no_rawat INNER JOIN dokter ON "
                        + "rehab_goal.kd_dokter = dokter.kd_dokter INNER JOIN petugas ON rehab_goal.kdpetugas = petugas.nip "
                        + "WHERE rehab_goal.tanggal BETWEEN ? AND ? "
                        + "AND (reg_periksa.no_rawat LIKE ? OR pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? "
                        + "OR rehab_goal.kd_dokter LIKE ? OR dokter.nm_dokter LIKE ?) "
                        + "ORDER BY rehab_goal.tanggal"
                );
            }

            // Set parameter query
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                if (!TCari.getText().toString().trim().equals("")) {
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                }

                // Debugging: Print query
                System.out.println("Executing query: " + ps.toString());

                // Eksekusi query
                rs = ps.executeQuery();

                // Cek apakah ada data
                if (!rs.isBeforeFirst()) {
                    System.out.println("No data found for the given parameters.");
                }

                // Tambahkan data ke tabel
                while (rs.next()) {
                    tabMode3.addRow(new String[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("jk"),
                        rs.getString("tgl_lahir"),
                        rs.getString("tanggal"),
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter"),
                        rs.getString("hasil"),
                        rs.getString("pemeriksaanfisik"),
                        rs.getString("kdpetugas"),
                        rs.getString("nama")
                    });
                }
            } catch (Exception e) {
                System.out.println("Error executing query: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Tutup ResultSet dan PreparedStatement
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error preparing statement: " + e.getMessage());
            e.printStackTrace();
        }

        // Set jumlah baris ke label
        LCount.setText("" + tabMode3.getRowCount());
    }

    public void emptTeks() {
        switch (tabRehab.getSelectedIndex()) {
            case 0:
                Anamnesa.setText("");
                PemeriksaanFisik.setText("");
                DiagnosisMedis.setText("");
                DiagnosisFungsi.setText("");
                PemeriksaanPenunjang.setText("");
                Tatalaksana.setText("");
                Anjuran.setText("");
                Goal.setText("");
                Evaluasi.setText("");
                Suspek.setSelectedIndex(0);
                SuspekKet.setText("");
                break;
            case 1:
                TNoRw.setText("");
                Program.setText("");
                KdPetugas.setText("");
                NamaPetugas.setText("");
                break;
            case 2:
                Hasil.setText("");
                break;
            default:
        }

    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
            /*"No.Rawat", "No.R.M.", "Nama Pasien", "JK", "Tgl.Lahir", "Tanggal Uji", "Kode Dokter", "Nama Dokter", "Anamnesa", "Pemeriksaan Fisik",
            "Diagnosis Medis", "Diagnosis Fungsi", "Pemeriksaan Penunjang", "Tata laksana", "Anjuran", "Goal Of Treatment", "Evaluasi", 
             */
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
            KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            NmDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
            Anamnesa.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
            PemeriksaanFisik.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
            DiagnosisMedis.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
            DiagnosisFungsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
            PemeriksaanPenunjang.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
            Tatalaksana.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
            Anjuran.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
            Goal.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString());
            Evaluasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());
            Suspek.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 17).toString());
            SuspekKet.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 18).toString());
            Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(11, 13));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(14, 15));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(17, 19));
        }
    }

    private void getData2() {
        if (tbObat2.getSelectedRow() != -1) {
            TNoRw.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 0).toString());
            TNoRM.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 2).toString());
            JK.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 3).toString());
            TglLahir.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 4).toString());
            KdDokter.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 6).toString());
            NmDokter.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 7).toString());
            Program.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 8).toString());
            KdPetugas.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 9).toString());
            NamaPetugas.setText(tbObat2.getValueAt(tbObat2.getSelectedRow(), 10).toString());
            Valid.SetTgl(Tanggal, tbObat2.getValueAt(tbObat2.getSelectedRow(), 5).toString());
            Jam.setSelectedItem(tbObat2.getValueAt(tbObat2.getSelectedRow(), 5).toString().substring(11, 13));
            Menit.setSelectedItem(tbObat2.getValueAt(tbObat2.getSelectedRow(), 5).toString().substring(14, 15));
            Detik.setSelectedItem(tbObat2.getValueAt(tbObat2.getSelectedRow(), 5).toString().substring(17, 19));
        }
    }

    private void getData3() {
        if (tbObat3.getSelectedRow() != -1) {
            /*    "No.Rawat", "No.R.M.", "Nama Pasien", "JK", "Tgl.Lahir", "Tanggal Uji", "Kode Dokter", "Nama Dokter",
            "Program", "Kode Petugas", "Nama Petugas" */
            TNoRw.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 0).toString());
            TNoRM.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 2).toString());
            JK.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 3).toString());
            TglLahir.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 4).toString());
            KdDokter.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 6).toString());
            NmDokter.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 7).toString());
            Hasil.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 8).toString());
            PxFisikUjiFungsi.setSelectedItem(tbObat3.getValueAt(tbObat3.getSelectedRow(), 9).toString());
            KdPetugas.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 9).toString());
            NamaPetugas.setText(tbObat3.getValueAt(tbObat3.getSelectedRow(), 10).toString());
            Valid.SetTgl(Tanggal, tbObat3.getValueAt(tbObat3.getSelectedRow(), 5).toString());
            Jam.setSelectedItem(tbObat3.getValueAt(tbObat3.getSelectedRow(), 5).toString().substring(11, 13));
            Menit.setSelectedItem(tbObat3.getValueAt(tbObat3.getSelectedRow(), 5).toString().substring(14, 15));
            Detik.setSelectedItem(tbObat3.getValueAt(tbObat3.getSelectedRow(), 5).toString().substring(17, 19));
        }
    }

    private void isRawat() {
        Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='" + TNoRw.getText() + "' ", TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", TPasien);
        Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", JK);
        Sequel.cariIsi("select date_format(pasien.tgl_lahir,'%d-%m-%Y') from pasien where pasien.no_rkm_medis=? ", TglLahir, TNoRM.getText());
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);

        Sequel.cariIsi("select reg_periksa.tgl_registrasi from reg_periksa where reg_periksa.no_rawat='" + norwt + "'", DTPCari1);
        DTPCari2.setDate(tgl2);
        isRawat();
        isPsien();
        TCari.setText(TNoRM.getText());
        ChkInput.setSelected(true);
        isForm();
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 300));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getuji_fungsi_kfr());
        BtnHapus.setEnabled(akses.getuji_fungsi_kfr());
        BtnEdit.setEnabled(akses.getuji_fungsi_kfr());
        BtnPrint.setEnabled(akses.getuji_fungsi_kfr());
        if (akses.getjml2() >= 1) {
            KdDokter.setEditable(false);
            btnPetugas.setEnabled(false);
            KdDokter.setText(akses.getkode());
            NmDokter.setText(dokter.tampil3(KdDokter.getText()));
            if (NmDokter.getText().equals("")) {
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan dokter...!!");
            }
        }
    }

    private void jam() {
        ActionListener taskPerformer = new ActionListener() {
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;

            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";

                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if (ChkKejadian.isSelected() == true) {
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                } else if (ChkKejadian.isSelected() == false) {
                    nilai_jam = Jam.getSelectedIndex();
                    nilai_menit = Menit.getSelectedIndex();
                    nilai_detik = Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    private void ganti() {
        Sequel.mengedit("rehab_kunjunganpertama", "no_rawat=?", "no_rawat=?,tanggal=?,kd_dokter=?,anamnesa=?,pemeriksaanfisik=?,diagnosismedis=?,diagnosisfungsi=?,penunjang=?,"
                + "tatalaksana=?,anjuran=?,goal=?,evaluasi=?,suspek=?,suspekket=?",
                15, new String[]{
                    TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    KdDokter.getText(), Anamnesa.getText(), PemeriksaanFisik.getText(), DiagnosisMedis.getText(), DiagnosisFungsi.getText(), PemeriksaanPenunjang.getText(),
                    Tatalaksana.getText(), Anjuran.getText(), Goal.getText(), Evaluasi.getText(), Suspek.getSelectedItem().toString(), SuspekKet.getText(),
                    tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                });
        if (tabMode.getRowCount() != 0) {
            tampil();
        }
        emptTeks();
    }

    private void ganti2() {
        Sequel.mengedit("rehab_program", "no_rawat=?", "no_rawat=?,tanggal=?,kd_dokter=?,program=?,kdpetugas=?",
                6, new String[]{
                    TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    KdDokter.getText(), Program.getText(), KdPetugas.getText(),
                    tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                });
        if (tabMode2.getRowCount() != 0) {
            tampil2();
        }
        emptTeks();
    }

    private void ganti3() {
        Sequel.mengedit("rehab_goal", "no_rawat=?", "no_rawat=?,tanggal=?,kd_dokter=?,program=?,pemeriksaanifisik=?,kdpetugas=?,",
                6, new String[]{
                    TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    KdDokter.getText(), Hasil.getText(), PxFisikUjiFungsi.getSelectedItem().toString(), KdPetugas1.getText(),
                    tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                });
        if (tabMode3.getRowCount() != 0) {
            tampil3();
        }
        emptTeks();
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from rehab_kunjunganpertama where no_rawat=?", 1, new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
        }) == true) {
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText("" + tabMode.getRowCount());
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private void hapus2() {
        if (Sequel.queryu2tf("delete from rehab_program where no_rawat=?", 1, new String[]{
            tbObat2.getValueAt(tbObat2.getSelectedRow(), 0).toString()
        }) == true) {
            tabMode2.removeRow(tbObat2.getSelectedRow());
            LCount.setText("" + tabMode2.getRowCount());
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private void hapus3() {
        if (Sequel.queryu2tf("delete from rehab_goal where no_rawat=?", 1, new String[]{
            tbObat3.getValueAt(tbObat3.getSelectedRow(), 0).toString()
        }) == true) {
            tabMode3.removeRow(tbObat3.getSelectedRow());
            LCount.setText("" + tabMode3.getRowCount());
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }
}
