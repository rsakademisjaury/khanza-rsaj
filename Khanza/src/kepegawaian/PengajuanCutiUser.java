/*
 Kontribusi dari mas Haris, RS Bhayangkara Nganjuk
 */

 /*
 * DlgRujuk.java
 *
 * Created on 31 Mei 10, 20:19:56
 */
package kepegawaian;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class PengajuanCutiUser extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0, pilihan = 0;
    private double total = 0;
   private String nikPegawai = "";
    private String statusAwalHRD = "";
    private String statusAwalDireksi = "";
    private Map<String, Integer> kuotaCuti = new HashMap<>();
    private boolean isInitializing = true;  // ← TAMBAHKAN INI

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public PengajuanCutiUser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);
        
        // Inisialisasi kuota cuti
           initKuotaCuti();
           isInitializing = true;

         // ===== 1. DEFINISI TABEL DI CONSTRUCTOR (17 KOLOM) =====

tabMode = new DefaultTableModel(null, new Object[]{
    "No.Pengajuan", "Tanggal", "Tgl Awal", "Tgl Akhir", "NIK", 
    "Diajukan Oleh", "Bidang", "Departemen", "Jenis Cuti", 
    "Alamat Tujuan", "Jml Cuti", "Kepentingan Cuti", 
    "NIK P.J.", "P.J. Terkait", "Status",
    "Status Kepala Instalasi", "Status HRD"  // Kolom 15 & 16 (index 15, 16)
}) {
    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        return false;
    }
};
tbObat.setModel(tabMode);
tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// Custom renderer untuk warna
tbObat.setDefaultRenderer(Object.class, new WarnaTable() {
    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        java.awt.Component cell = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
        
        try {
           // ✅ URUTAN KOLOM YANG BENAR:
                    String statusKepalaUnit = table.getValueAt(row, 15).toString(); // Kolom 15 = Status Kepala Instalasi
                    String statusHRD = table.getValueAt(row, 16).toString();        // Kolom 16 = Status HRD

                    // ✅ LOGIKA WARNA:
                    if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Disetujui")) {
                        // ✅ DISETUJUI PENUH - HIJAU
                        if (!isSelected) {
                            cell.setBackground(new java.awt.Color(200, 255, 200)); // Hijau muda
                            cell.setForeground(new java.awt.Color(0, 100, 0));     // Teks hijau tua
                        }
                    } else if (statusKepalaUnit.equals("Ditolak") || statusHRD.equals("Ditolak")) {
                        // ❌ DITOLAK - MERAH
                        if (!isSelected) {
                            cell.setBackground(new java.awt.Color(255, 200, 200)); // Merah muda
                            cell.setForeground(new java.awt.Color(139, 0, 0));     // Teks merah tua
                        }
                    } else {
                        // ⏳ PROSES PENGAJUAN - KUNING
                        if (!isSelected) {
                            cell.setBackground(new java.awt.Color(255, 255, 200)); // Kuning muda
                            cell.setForeground(new java.awt.Color(0, 0, 0));       // Teks hitam
                        }
                    }

                } catch (Exception e) {
                    // Jika ada error, gunakan warna default
                    if (!isSelected) {
                        cell.setBackground(new java.awt.Color(255, 255, 255));
                        cell.setForeground(new java.awt.Color(0, 0, 0));
                    }
                }

                return cell;
            }
});

        // Set column widths
        for (i = 0; i < 17; i++) {
    TableColumn column = tbObat.getColumnModel().getColumn(i);
    if (i == 0) column.setPreferredWidth(85);       // No.Pengajuan
    else if (i == 1) column.setPreferredWidth(65);  // Tanggal
    else if (i == 2) column.setPreferredWidth(65);  // Tgl Awal
    else if (i == 3) column.setPreferredWidth(65);  // Tgl Akhir
    else if (i == 4) column.setPreferredWidth(85);  // NIK
    else if (i == 5) column.setPreferredWidth(160); // Diajukan Oleh
    else if (i == 6) column.setPreferredWidth(70);  // Bidang
    else if (i == 7) column.setPreferredWidth(70);  // Departemen
    else if (i == 8) column.setPreferredWidth(110); // Jenis Cuti
    else if (i == 9) column.setPreferredWidth(170); // Alamat Tujuan
    else if (i == 10) column.setPreferredWidth(80); // Jml Cuti
    else if (i == 11) column.setPreferredWidth(170);// Kepentingan Cuti
    else if (i == 12) column.setPreferredWidth(85); // NIK P.J.
    else if (i == 13) column.setPreferredWidth(160);// P.J. Terkait
    else if (i == 14) column.setPreferredWidth(110);// Status
    else if (i == 15) column.setPreferredWidth(120);// Status Kepala Instalasi
    else if (i == 16) column.setPreferredWidth(110);// Status HRD
}

        // Input limits
        NoPengajuan.setDocument(new batasInput((int) 17).getKata(NoPengajuan));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
        isInitializing = false;
        Kepentingan.setDocument(new batasInput((int) 70).getKata(Kepentingan));
        NoPengajuan.setDocument(new batasInput((int) 17).getKata(NoPengajuan));
        Alamat.setDocument(new batasInput((int) 100).getKata(Alamat));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
        Jumlah.setDocument(new batasInput((byte) 3).getOnlyAngka(Jumlah));
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
                        NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        Bidang.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 6).toString());
                        Departemen.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 5).toString());
                        Alamat.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 13).toString());
                        // *** TAMBAHKAN BARIS INI ***
                        ambilDataCutiDariDB(); // <-- Tambah ini!
                        btnPetugas.requestFocus();
                    } else if (pilihan == 2) {
                        KdPetugasPJ.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugasPJ.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        btnPetugasPJ.requestFocus();
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

        ChkInput.setSelected(false);
        isForm();
    }

    private DlgCariPegawai petugas = new DlgCariPegawai(null, false);

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
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
        jLabel18 = new widget.Label();
        LCount1 = new widget.Label();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel8 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabel5 = new widget.Label();
        KdPetugas = new widget.TextBox();
        btnPetugas = new widget.Button();
        NmPetugas = new widget.TextBox();
        scrollPane1 = new widget.ScrollPane();
        Alamat = new widget.TextArea();
        jLabel9 = new widget.Label();
        jLabel3 = new widget.Label();
        NoPengajuan = new widget.TextBox();
        jLabel20 = new widget.Label();
        jLabel4 = new widget.Label();
        Kepentingan = new widget.TextBox();
        jLabel11 = new widget.Label();
        Jumlah = new widget.TextBox();
        jLabel15 = new widget.Label();
        Bidang = new widget.TextBox();
        jLabel16 = new widget.Label();
        Departemen = new widget.TextBox();
        jLabel17 = new widget.Label();
        KdPetugasPJ = new widget.TextBox();
        NmPetugasPJ = new widget.TextBox();
        btnPetugasPJ = new widget.Button();
        jLabel14 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        jLabel22 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel12 = new widget.Label();
        jLabel23 = new widget.Label();
        Status = new widget.ComboBox();
        jLabel27 = new widget.Label();
        Kuota1 = new widget.TextBox();
        jLabel25 = new widget.Label();
        Kuota = new widget.TextBox();
        Sisacuti = new widget.TextBox();
        jLabel13 = new widget.Label();
        jLabel26 = new widget.Label();
        jLabel24 = new widget.Label();
        Urgensi = new widget.ComboBox();
        ChkInput = new widget.CekBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengajuan Cuti Pegawai ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

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
        jLabel7.setPreferredSize(new java.awt.Dimension(70, 23));
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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-11-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-11-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(190, 23));
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

        jLabel18.setText("Pengajuan :");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel18);

        LCount1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount1.setText("0");
        LCount1.setName("LCount1"); // NOI18N
        LCount1.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(LCount1);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(72, 250));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(55, 165));
        FormInput.setLayout(null);

        jLabel8.setText("Tgl. Pengajuan :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(226, 10, 99, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-11-2025" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(329, 10, 90, 23);

        jLabel5.setText("Alamat Tujuan :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(420, 70, 95, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setHighlighter(null);
        KdPetugas.setName("KdPetugas"); // NOI18N
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(92, 40, 110, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("Alt+2");
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
        FormInput.add(btnPetugas);
        btnPetugas.setBounds(391, 40, 28, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setHighlighter(null);
        NmPetugas.setName("NmPetugas"); // NOI18N
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(204, 40, 185, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        Alamat.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Alamat.setColumns(20);
        Alamat.setRows(5);
        Alamat.setName("Alamat"); // NOI18N
        Alamat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlamatKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(Alamat);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(520, 70, 235, 52);

        jLabel9.setText("Diajukan Oleh :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 40, 88, 23);

        jLabel3.setText("No.Pengajuan :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(0, 10, 88, 23);

        NoPengajuan.setHighlighter(null);
        NoPengajuan.setName("NoPengajuan"); // NOI18N
        FormInput.add(NoPengajuan);
        NoPengajuan.setBounds(92, 10, 130, 23);

        jLabel20.setText("Jenis Cuti :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(410, 10, 95, 23);

        jLabel4.setText("Kepentingan Cuti :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(250, 130, 120, 23);

        Kepentingan.setHighlighter(null);
        Kepentingan.setName("Kepentingan"); // NOI18N
        Kepentingan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KepentinganKeyPressed(evt);
            }
        });
        FormInput.add(Kepentingan);
        Kepentingan.setBounds(374, 130, 396, 23);

        jLabel11.setText("Jml. Cuti :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(320, 100, 50, 23);

        Jumlah.setHighlighter(null);
        Jumlah.setName("Jumlah"); // NOI18N
        Jumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JumlahKeyPressed(evt);
            }
        });
        FormInput.add(Jumlah);
        Jumlah.setBounds(370, 100, 45, 23);

        jLabel15.setText("Bidang :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(423, 40, 50, 23);

        Bidang.setEditable(false);
        Bidang.setHighlighter(null);
        Bidang.setName("Bidang"); // NOI18N
        Bidang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BidangKeyPressed(evt);
            }
        });
        FormInput.add(Bidang);
        Bidang.setBounds(477, 40, 95, 23);

        jLabel16.setText("Departemen :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(581, 40, 70, 23);

        Departemen.setEditable(false);
        Departemen.setHighlighter(null);
        Departemen.setName("Departemen"); // NOI18N
        Departemen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DepartemenKeyPressed(evt);
            }
        });
        FormInput.add(Departemen);
        Departemen.setBounds(655, 40, 115, 23);

        jLabel17.setText("P.J.Terkait :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(0, 70, 88, 23);

        KdPetugasPJ.setEditable(false);
        KdPetugasPJ.setHighlighter(null);
        KdPetugasPJ.setName("KdPetugasPJ"); // NOI18N
        FormInput.add(KdPetugasPJ);
        KdPetugasPJ.setBounds(92, 70, 110, 23);

        NmPetugasPJ.setEditable(false);
        NmPetugasPJ.setHighlighter(null);
        NmPetugasPJ.setName("NmPetugasPJ"); // NOI18N
        FormInput.add(NmPetugasPJ);
        NmPetugasPJ.setBounds(204, 70, 185, 23);

        btnPetugasPJ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugasPJ.setMnemonic('2');
        btnPetugasPJ.setToolTipText("Alt+2");
        btnPetugasPJ.setName("btnPetugasPJ"); // NOI18N
        btnPetugasPJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasPJActionPerformed(evt);
            }
        });
        btnPetugasPJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugasPJKeyPressed(evt);
            }
        });
        FormInput.add(btnPetugasPJ);
        btnPetugasPJ.setBounds(391, 70, 28, 23);

        jLabel14.setText("Tanggal Cuti :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(0, 100, 88, 23);

        Tgl1.setForeground(new java.awt.Color(50, 70, 50));
        Tgl1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-11-2025" }));
        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setOpaque(false);
        Tgl1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Tgl1ItemStateChanged(evt);
            }
        });
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        FormInput.add(Tgl1);
        Tgl1.setBounds(92, 100, 90, 23);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("s/d");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(184, 100, 25, 23);

        Tgl2.setForeground(new java.awt.Color(50, 70, 50));
        Tgl2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-11-2025" }));
        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setOpaque(false);
        Tgl2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Tgl2ItemStateChanged(evt);
            }
        });
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        FormInput.add(Tgl2);
        Tgl2.setBounds(211, 100, 90, 23);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Hari");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(420, 100, 40, 23);

        jLabel23.setText("Status :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(0, 130, 88, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Pengajuan", "Disetujui", "Ditolak" }));
        Status.setName("Status"); // NOI18N
        Status.setPreferredSize(new java.awt.Dimension(55, 28));
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(92, 130, 159, 23);

        jLabel27.setText("Keterangan Cuti :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(780, 10, 90, 23);

        Kuota1.setHighlighter(null);
        Kuota1.setName("Kuota1"); // NOI18N
        Kuota1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kuota1ActionPerformed(evt);
            }
        });
        Kuota1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Kuota1KeyPressed(evt);
            }
        });
        FormInput.add(Kuota1);
        Kuota1.setBounds(870, 10, 150, 23);

        jLabel25.setText("Kuota Cuti :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(770, 40, 70, 23);

        Kuota.setHighlighter(null);
        Kuota.setName("Kuota"); // NOI18N
        Kuota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KuotaActionPerformed(evt);
            }
        });
        Kuota.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KuotaKeyPressed(evt);
            }
        });
        FormInput.add(Kuota);
        Kuota.setBounds(840, 40, 45, 23);

        Sisacuti.setHighlighter(null);
        Sisacuti.setName("Sisacuti"); // NOI18N
        Sisacuti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SisacutiActionPerformed(evt);
            }
        });
        Sisacuti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SisacutiKeyPressed(evt);
            }
        });
        FormInput.add(Sisacuti);
        Sisacuti.setBounds(830, 70, 45, 23);

        jLabel13.setText("Sisa Cuti :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(780, 70, 50, 23);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel26.setText("Hari/Bulan");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(890, 40, 50, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Hari");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(880, 70, 30, 23);

        Urgensi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cuti Tahunan", "Cuti Hamil", "Cuti Menikah", "Cuti Khitan Anak", "Cuti Menikahkan Anak", "Cuti Keluarga Meninggal ( Suami/Istri, Orang Tua/Mertua, Anak)", "Cuti Menunggu Keluarga Sakit", "Cuti Membabtiskan Anak", "Cuti Istri Melahirkan", "Cuti Keluarga Meninggal (satu rumah)", "Cuti Umroh/Haji" }));
        Urgensi.setName("Urgensi"); // NOI18N
        Urgensi.setPreferredSize(new java.awt.Dimension(55, 28));
        Urgensi.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                UrgensiItemStateChanged(evt);
            }
        });
        Urgensi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UrgensiActionPerformed(evt);
            }
        });
        Urgensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                UrgensiKeyPressed(evt);
            }
        });
        FormInput.add(Urgensi);
        Urgensi.setBounds(510, 10, 250, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

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

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
 // ========== VALIDASI INPUT ==========
    if (NoPengajuan.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "No.Pengajuan tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
        NoPengajuan.requestFocus();
        return;
    } else if (KdPetugas.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "Pegawai yang mengajukan belum dipilih!", "Validasi", JOptionPane.WARNING_MESSAGE);
        btnPetugas.requestFocus();
        return;
    } else if (Alamat.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "Alamat tujuan belum diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
        Alamat.requestFocus();
        return;
    } else if (Jumlah.getText().trim().equals("") || Jumlah.getText().trim().equals("0")) {
        JOptionPane.showMessageDialog(null, "Jumlah cuti belum diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
        Jumlah.requestFocus();
        return;
    } else if (Kepentingan.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "Kepentingan cuti belum diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
        Kepentingan.requestFocus();
        return;
    } else if (KdPetugasPJ.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(null, "Penanggung jawab belum dipilih!", "Validasi", JOptionPane.WARNING_MESSAGE);
        btnPetugasPJ.requestFocus();
        return;
    }

    // ========== VALIDASI SISA CUTI ==========
    try {
        int jumlahCutiDiminta = Integer.parseInt(Jumlah.getText().trim());
        int sisaCutiTersedia = Integer.parseInt(Sisacuti.getText().trim());
        String nik = KdPetugas.getText().trim();
        String jenisCuti = Urgensi.getSelectedItem().toString();
        String noPengajuanIni = NoPengajuan.getText().trim();
        
        // ✅ CEK: Apakah ini pengajuan baru atau edit?
        boolean isPengajuanBaru = true;
        int jumlahCutiSebelumnya = 0;
        
        String checkQuery = "SELECT jumlah FROM pengajuan_cuti WHERE no_pengajuan = ?";
        PreparedStatement psCheck = koneksi.prepareStatement(checkQuery);
        psCheck.setString(1, noPengajuanIni);
        ResultSet rsCheck = psCheck.executeQuery();
        
        if (rsCheck.next()) {
            isPengajuanBaru = false;
            jumlahCutiSebelumnya = rsCheck.getInt("jumlah");
        }
        
        rsCheck.close();
        psCheck.close();
        
        // ✅ HITUNG SISA SETELAH PENGAJUAN INI
        int sisaSetelahPengajuan;
        if (isPengajuanBaru) {
            // Pengajuan baru: kurangi langsung
            sisaSetelahPengajuan = sisaCutiTersedia - jumlahCutiDiminta;
        } else {
            // Edit pengajuan: kembalikan jumlah lama, lalu kurangi jumlah baru
            sisaSetelahPengajuan = sisaCutiTersedia + jumlahCutiSebelumnya - jumlahCutiDiminta;
        }
        
        // ✅ VALIDASI: Cek apakah sisa mencukupi
        if (sisaSetelahPengajuan < 0) {
            int kuotaAwal = Integer.parseInt(Kuota.getText().trim());
            int totalSudahDisetujui = kuotaAwal - sisaCutiTersedia;
            
            JOptionPane.showMessageDialog(null,
                "════════════════════════════════\n" +
                "   ⚠ SISA CUTI TIDAK CUKUP!\n" +
                "════════════════════════════════\n\n" +
                "📊 DETAIL KUOTA CUTI:\n" +
                "─────────────────────────────────\n" +
                "Kuota Awal        : " + kuotaAwal + " hari\n" +
                "Sudah Disetujui   : " + totalSudahDisetujui + " hari\n" +
                "Sisa Tersedia     : " + sisaCutiTersedia + " hari\n\n" +
                "📝 PENGAJUAN INI:\n" +
                "─────────────────────────────────\n" +
                "Jumlah Diminta    : " + jumlahCutiDiminta + " hari\n" +
                (isPengajuanBaru ? "" : "Jumlah Sebelumnya : " + jumlahCutiSebelumnya + " hari\n") +
                "Sisa Setelahnya   : " + sisaSetelahPengajuan + " hari ❌\n\n" +
                "════════════════════════════════\n" +
                "Silakan kurangi jumlah hari cuti!\n" +
                "Maksimal yang bisa diajukan: " + (isPengajuanBaru ? sisaCutiTersedia : (sisaCutiTersedia + jumlahCutiSebelumnya)) + " hari",
                "Validasi Kuota Cuti",
                JOptionPane.ERROR_MESSAGE);
            Jumlah.requestFocus();
            return;
        }

        // ========== KONFIRMASI SIMPAN ==========
        int confirm = JOptionPane.showConfirmDialog(null,
            "════════════════════════════════\n" +
            "   " + (isPengajuanBaru ? "KONFIRMASI SIMPAN CUTI" : "KONFIRMASI EDIT CUTI") + "\n" +
            "════════════════════════════════\n\n" +
            "📋 DATA PENGAJUAN:\n" +
            "─────────────────────────────────\n" +
            "No. Pengajuan     : " + noPengajuanIni + "\n" +
            "Pegawai           : " + NmPetugas.getText() + "\n" +
            "NIK               : " + nik + "\n" +
            "Jenis Cuti        : " + jenisCuti + "\n" +
            "Keterangan        : " + Kuota1.getText() + "\n" +
            "Tanggal Cuti      : " + Tgl1.getSelectedItem() + " s/d " + Tgl2.getSelectedItem() + "\n" +
            "Jumlah Cuti       : " + jumlahCutiDiminta + " hari\n\n" +
            "📊 KUOTA CUTI:\n" +
            "─────────────────────────────────\n" +
            "Kuota Awal        : " + Kuota.getText() + " hari\n" +
            "Sisa Saat Ini     : " + sisaCutiTersedia + " hari\n" +
            (isPengajuanBaru ? "" : "Jumlah Sebelumnya : " + jumlahCutiSebelumnya + " hari\n") +
            "Sisa Setelahnya   : " + sisaSetelahPengajuan + " hari ✓\n\n" +
            "════════════════════════════════\n" +
            "⚠ CATATAN PENTING:\n" +
            "Pengajuan ini berstatus 'Proses'.\n" +
            "Sisa cuti di atas adalah PREDIKSI\n" +
            "jika pengajuan ini disetujui penuh\n" +
            "oleh Kepala Unit & HRD.\n\n" +
            "Simpan pengajuan cuti?",
            "Konfirmasi " + (isPengajuanBaru ? "Simpan" : "Edit"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // ========== PROSES SIMPAN DATA ==========
        String namaPegawai = NmPetugas.getText();
        int jumlahCuti = jumlahCutiDiminta;
        
        if (isPengajuanBaru) {
            // ✅ INSERT DATA BARU
            String sql = "INSERT INTO pengajuan_cuti (no_pengajuan, tanggal, tanggal_awal, tanggal_akhir, " +
                         "nik, urgensi, alamat, jumlah, kepentingan, nik_pj, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, noPengajuanIni);
            ps.setString(2, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
            ps.setString(3, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
            ps.setString(4, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            ps.setString(5, nik);
            ps.setString(6, jenisCuti);
            ps.setString(7, Alamat.getText());
            ps.setString(8, String.valueOf(jumlahCuti));
            ps.setString(9, Kepentingan.getText());
            ps.setString(10, KdPetugasPJ.getText());
            ps.setString(11, "Proses Pengajuan"); // Status default
            
            int result = ps.executeUpdate();
            ps.close();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(null,
                    "════════════════════════════════\n" +
                    "   ✓ PENGAJUAN BERHASIL DISIMPAN!\n" +
                    "════════════════════════════════\n\n" +
                    "📋 DETAIL PENGAJUAN:\n" +
                    "─────────────────────────────────\n" +
                    "No. Pengajuan     : " + noPengajuanIni + "\n" +
                    "Pegawai           : " + namaPegawai + "\n" +
                    "NIK               : " + nik + "\n" +
                    "Jenis Cuti        : " + jenisCuti + "\n" +
                    "Jumlah            : " + jumlahCuti + " hari\n" +
                    "Status            : Proses Pengajuan\n\n" +
                    "⚠ PENTING:\n" +
                    "Pengajuan Anda menunggu approval\n" +
                    "dari Kepala Unit & HRD.\n\n" +
                    "Sisa cuti akan berkurang SETELAH\n" +
                    "pengajuan ini disetujui penuh.\n" +
                    "════════════════════════════════",
                    "Pengajuan Tersimpan",
                    JOptionPane.INFORMATION_MESSAGE);
                
                tampil();
                emptTeks();
            } else {
                JOptionPane.showMessageDialog(null,
                    "✗ Gagal menyimpan data ke database!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            // ✅ UPDATE DATA YANG SUDAH ADA
            String sql = "UPDATE pengajuan_cuti SET " +
                         "tanggal=?, tanggal_awal=?, tanggal_akhir=?, " +
                         "nik=?, urgensi=?, alamat=?, jumlah=?, " +
                         "kepentingan=?, nik_pj=? " +
                         "WHERE no_pengajuan=?";
            
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
            ps.setString(3, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            ps.setString(4, nik);
            ps.setString(5, jenisCuti);
            ps.setString(6, Alamat.getText());
            ps.setString(7, String.valueOf(jumlahCuti));
            ps.setString(8, Kepentingan.getText());
            ps.setString(9, KdPetugasPJ.getText());
            ps.setString(10, noPengajuanIni);
            
            int result = ps.executeUpdate();
            ps.close();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(null,
                    "════════════════════════════════\n" +
                    "   ✓ PERUBAHAN BERHASIL DISIMPAN!\n" +
                    "════════════════════════════════\n\n" +
                    "📋 DETAIL PERUBAHAN:\n" +
                    "─────────────────────────────────\n" +
                    "No. Pengajuan     : " + noPengajuanIni + "\n" +
                    "Jumlah Sebelumnya : " + jumlahCutiSebelumnya + " hari\n" +
                    "Jumlah Baru       : " + jumlahCuti + " hari\n\n" +
                    "Data pengajuan berhasil diperbarui.\n" +
                    "════════════════════════════════",
                    "Edit Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
                
                tampil();
                emptTeks();
            } else {
                JOptionPane.showMessageDialog(null,
                    "✗ Gagal mengupdate data!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null,
            "════════════════════════════════\n" +
            "   FORMAT ANGKA TIDAK VALID\n" +
            "════════════════════════════════\n\n" +
            "Periksa kembali input:\n" +
            "• Jumlah Cuti (harus angka)\n" +
            "• Kuota Cuti (harus angka)\n" +
            "• Sisa Cuti (harus angka)\n\n" +
            "Error: " + e.getMessage(),
            "Error Format",
            JOptionPane.ERROR_MESSAGE);
        Jumlah.requestFocus();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
            "════════════════════════════════\n" +
            "   DATABASE ERROR\n" +
            "════════════════════════════════\n\n" +
            "Error: " + e.getMessage() + "\n\n" +
            "Kemungkinan penyebab:\n" +
            "• Koneksi database terputus\n" +
            "• Tabel tidak ditemukan\n" +
            "• Data tidak valid\n\n" +
            "Silakan hubungi administrator.",
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, Kepentingan, BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        ChkInput.setSelected(true);
        isForm();
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            Sequel.meghapus("pengajuan_cuti", "no_pengajuan", tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            tampil();
            emptTeks();
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
        if (NoPengajuan.getText().trim().equals("")) {
            Valid.textKosong(NoPengajuan, "No.Pengajuan");
        } else if (NmPetugas.getText().trim().equals("")) {
            Valid.textKosong(KdPetugas, "Yang Mengajukan");
        } else if (Alamat.getText().trim().equals("")) {
            Valid.textKosong(Alamat, "Alamat Tujuan");
        } else if (Jumlah.getText().trim().equals("") || Jumlah.getText().trim().equals("0")) {
            Valid.textKosong(Jumlah, "Jml Cuti");
        } else if (Kepentingan.getText().trim().equals("")) {
            Valid.textKosong(Kepentingan, "Kepentingan Cuti");
        } else if (NmPetugasPJ.getText().trim().equals("")) {
            Valid.textKosong(KdPetugasPJ, "P.J. terkait pengajuan");
        } else {
            if (tbObat.getSelectedRow() > -1) {
                if (Sequel.mengedittf("pengajuan_cuti", "no_pengajuan=?", "no_pengajuan=?,tanggal=?,tanggal_awal=?,tanggal_akhir=?,nik=?,urgensi=?,alamat=?,jumlah=?,kepentingan=?,nik_pj=?,status=?", 12, new String[]{
                    NoPengajuan.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + ""), Valid.SetTgl(Tgl1.getSelectedItem() + ""), Valid.SetTgl(Tgl2.getSelectedItem() + ""), KdPetugas.getText(), Urgensi.getSelectedItem().toString(),
                    Alamat.getText(), Jumlah.getText(), Kepentingan.getText(), KdPetugasPJ.getText(), Status.getSelectedItem().toString(), tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                }) == true) {
                    tampil();
                    emptTeks();
                }
            }
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
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
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
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            if (TCari.getText().equals("")) {
                Valid.MyReportqry("rptPengajuanCutiAdmin.jasper", "report", "::[ Data Pengajuan Cuti ]::",
                        "select pengajuan_cuti.no_pengajuan,pengajuan_cuti.tanggal,pengajuan_cuti.tanggal_awal,pengajuan_cuti.tanggal_akhir,"
                        + "pengajuan_cuti.nik,peg1.nama as namapengaju,peg1.bidang,peg1.departemen,pengajuan_cuti.urgensi,pengajuan_cuti.alamat,"
                        + "pengajuan_cuti.jumlah,pengajuan_cuti.kepentingan,pengajuan_cuti.nik_pj,peg2.nama as namapj,pengajuan_cuti.status "
                        + "from pengajuan_cuti inner join pegawai as peg1 on pengajuan_cuti.nik=peg1.nik "
                        + "inner join pegawai as peg2 on pengajuan_cuti.nik_pj=peg2.nik where "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' order by pengajuan_cuti.tanggal", param);
            } else {
                Valid.MyReportqry("rptPengajuanCutiAdmin.jasper", "report", "::[ Data Pengajuan Cuti ]::",
                        "select pengajuan_cuti.no_pengajuan,pengajuan_cuti.tanggal,pengajuan_cuti.tanggal_awal,pengajuan_cuti.tanggal_akhir,"
                        + "pengajuan_cuti.nik,peg1.nama as namapengaju,peg1.bidang,peg1.departemen,pengajuan_cuti.urgensi,pengajuan_cuti.alamat,"
                        + "pengajuan_cuti.jumlah,pengajuan_cuti.kepentingan,pengajuan_cuti.nik_pj,peg2.nama as namapj,pengajuan_cuti.status "
                        + "from pengajuan_cuti inner join pegawai as peg1 on pengajuan_cuti.nik=peg1.nik "
                        + "inner join pegawai as peg2 on pengajuan_cuti.nik_pj=peg2.nik where "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.no_pengajuan like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.nik like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and peg1.nama like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and peg1.bidang like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and peg1.departemen like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.urgensi like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.alamat like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.kepentingan like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.nik_pj like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and peg2.nama like '%" + TCari.getText().trim() + "%' or "
                        + "pengajuan_cuti.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and pengajuan_cuti.status like '%" + TCari.getText().trim() + "%' order by pengajuan_cuti.tanggal", param);
            }

        }
        this.setCursor(Cursor.getDefaultCursor());
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
        tampil();
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
            TCari.setText("");
            tampil();
        } else {
            Valid.pindah(evt, BtnCari, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, TCari, Tgl1);
}//GEN-LAST:event_TanggalKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
    pilihan = 1;
    petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
    petugas.setLocationRelativeTo(internalFrame1);
    petugas.setVisible(true);
}//GEN-LAST:event_btnPetugasActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        emptTeks();
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void BidangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BidangKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BidangKeyPressed

    private void DepartemenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DepartemenKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DepartemenKeyPressed

    private void btnPetugasPJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasPJActionPerformed
        pilihan = 2;
        petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugasPJActionPerformed

    private void KepentinganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KepentinganKeyPressed
        Valid.pindah(evt, Status, BtnSimpan);
    }//GEN-LAST:event_KepentinganKeyPressed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        Valid.pindah(evt, Kepentingan, Urgensi);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void JumlahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahKeyPressed
        Valid.pindah(evt, Tgl2, Status);
    }//GEN-LAST:event_JumlahKeyPressed

    private void AlamatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlamatKeyPressed
        Valid.pindah(evt, btnPetugasPJ, Tgl1);
    }//GEN-LAST:event_AlamatKeyPressed

    private void btnPetugasPJKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasPJKeyPressed
        Valid.pindah(evt, btnPetugas, Alamat);
    }//GEN-LAST:event_btnPetugasPJKeyPressed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        Valid.pindah(evt, Alamat, Tgl2);
    }//GEN-LAST:event_Tgl1KeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed
        Valid.pindah(evt, Tgl1, Jumlah);
    }//GEN-LAST:event_Tgl2KeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        Valid.pindah(evt, Jumlah, Kepentingan);
    }//GEN-LAST:event_StatusKeyPressed

    private void Tgl1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Tgl1ItemStateChanged
        Sequel.cariIsi("select to_days('" + Valid.SetTgl(Tgl2.getSelectedItem() + "") + "')-to_days('" + Valid.SetTgl(Tgl1.getSelectedItem() + "") + "')+1,1", Jumlah);
    }//GEN-LAST:event_Tgl1ItemStateChanged

    private void Tgl2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Tgl2ItemStateChanged
        Sequel.cariIsi("select to_days('" + Valid.SetTgl(Tgl2.getSelectedItem() + "") + "')-to_days('" + Valid.SetTgl(Tgl1.getSelectedItem() + "") + "')+1,1", Jumlah);
    }//GEN-LAST:event_Tgl2ItemStateChanged

    private void Kuota1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kuota1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kuota1ActionPerformed

    private void Kuota1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Kuota1KeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_Kuota1KeyPressed

    private void KuotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KuotaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_KuotaActionPerformed

    private void KuotaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KuotaKeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_KuotaKeyPressed

    private void SisacutiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SisacutiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SisacutiActionPerformed

    private void SisacutiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SisacutiKeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_SisacutiKeyPressed

    private void UrgensiItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_UrgensiItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_UrgensiItemStateChanged

    private void UrgensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UrgensiActionPerformed
String jenisCuti = Urgensi.getSelectedItem().toString();

    // Jika Cuti Tahunan
    if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
        Kuota1.setText("Cuti Pribadi");
        Kuota.setText("12");
        Sisacuti.setText("12");
    } else {
        // Selain Cuti Tahunan = Cuti Normatif
        Kuota1.setText("Cuti Normatif");
        
        // Set kuota berdasarkan jenis cuti dari Map
        Integer kuota = kuotaCuti.get(jenisCuti);
        if (kuota != null && kuota > 0) {
            Kuota.setText(String.valueOf(kuota));
            Sisacuti.setText(String.valueOf(kuota));
        } else {
            Kuota.setText("0");
            Sisacuti.setText("0");
        }
    }

    // Update sisa cuti dari database jika NIK sudah terisi
    if (!KdPetugas.getText().trim().isEmpty()) {
        ambilDataCutiDariDB();
    }
    }//GEN-LAST:event_UrgensiActionPerformed

    private void UrgensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UrgensiKeyPressed
        Valid.pindah(evt, btnPetugas, btnPetugasPJ);
    }//GEN-LAST:event_UrgensiKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            PengajuanCutiUser dialog = new PengajuanCutiUser(new javax.swing.JFrame(), true);
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
    private widget.TextArea Alamat;
    private widget.TextBox Bidang;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox Departemen;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Jumlah;
    private widget.TextBox KdPetugas;
    private widget.TextBox KdPetugasPJ;
    private widget.TextBox Kepentingan;
    private widget.TextBox Kuota;
    private widget.TextBox Kuota1;
    private widget.Label LCount;
    private widget.Label LCount1;
    private widget.TextBox NmPetugas;
    private widget.TextBox NmPetugasPJ;
    private widget.TextBox NoPengajuan;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox Sisacuti;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.Tanggal Tanggal;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.ComboBox Urgensi;
    private widget.Button btnPetugas;
    private widget.Button btnPetugasPJ;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

   private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            // ✅ QUERY DENGAN URUTAN YANG BENAR: Kepala Unit dulu, HRD terakhir
            String baseQuery
                    = "SELECT pc.no_pengajuan, pc.tanggal, pc.tanggal_awal, pc.tanggal_akhir, "
                    + "pc.nik, peg1.nama as namapengaju, peg1.bidang, peg1.departemen, "
                    + "pc.urgensi, pc.alamat, pc.jumlah, pc.kepentingan, "
                    + "pc.nik_pj, peg2.nama as namapj, pc.status, "
                    + "IFNULL(pc.status_kpl_unit, 'Proses Pengajuan') as status_kpl_unit, "
                    + // Kolom 15
                    "IFNULL(pc.status_hrd, 'Proses Pengajuan') as status_hrd "
                    + // Kolom 16
                    "FROM pengajuan_cuti pc "
                    + "INNER JOIN pegawai peg1 ON pc.nik = peg1.nik "
                    + "INNER JOIN pegawai peg2 ON pc.nik_pj = peg2.nik "
                    + "WHERE pc.tanggal BETWEEN ? AND ? ";

            if (!TCari.getText().trim().isEmpty()) {
                baseQuery += "AND (pc.no_pengajuan LIKE ? OR pc.nik LIKE ? OR peg1.nama LIKE ? OR "
                        + "peg1.bidang LIKE ? OR peg1.departemen LIKE ? OR pc.urgensi LIKE ? OR "
                        + "pc.alamat LIKE ? OR pc.kepentingan LIKE ? OR pc.nik_pj LIKE ? OR "
                        + "peg2.nama LIKE ? OR pc.status LIKE ?) ";
            }

            baseQuery += "ORDER BY pc.tanggal DESC";

            ps = koneksi.prepareStatement(baseQuery);
            ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));

            if (!TCari.getText().trim().isEmpty()) {
                String searchTerm = "%" + TCari.getText().trim() + "%";
                for (int i = 3; i <= 13; i++) {
                    ps.setString(i, searchTerm);
                }
            }

            rs = ps.executeQuery();
            total = 0;

            while (rs.next()) {
                // ✅ ADD ROW DENGAN URUTAN YANG BENAR
                tabMode.addRow(new String[]{
                    rs.getString("no_pengajuan"), // 0
                    rs.getString("tanggal"), // 1
                    rs.getString("tanggal_awal"), // 2
                    rs.getString("tanggal_akhir"), // 3
                    rs.getString("nik"), // 4
                    rs.getString("namapengaju"), // 5
                    rs.getString("bidang"), // 6
                    rs.getString("departemen"), // 7
                    rs.getString("urgensi"), // 8
                    rs.getString("alamat"), // 9
                    rs.getString("jumlah"), // 10
                    rs.getString("kepentingan"), // 11
                    rs.getString("nik_pj"), // 12
                    rs.getString("namapj"), // 13
                    rs.getString("status"), // 14
                    rs.getString("status_kpl_unit"), // 15 ← Kepala Unit
                    rs.getString("status_hrd") // 16 ← HRD
                });
                total += rs.getDouble("jumlah");

                // ✅ DEBUG: Print status untuk verifikasi
                System.out.println("Row added - Kepala Unit: " + rs.getString("status_kpl_unit")
                        + " | HRD: " + rs.getString("status_hrd"));
            }

            rs.close();
            ps.close();

            System.out.println("✅ Total rows: " + tabMode.getRowCount());

        } catch (Exception e) {
            System.out.println("❌ Error tampil: " + e);
            e.printStackTrace();
        }

        LCount.setText("" + tabMode.getRowCount());
        LCount1.setText(Valid.SetAngka(total));
    }

    private void emptTeks() {
        Tanggal.setDate(new Date());
        Tgl1.setDate(new Date());
        Tgl2.setDate(new Date());
        Alamat.setText("");
        Jumlah.setText("0");
        Kepentingan.setText("");
        KdPetugasPJ.setText("");
        NmPetugasPJ.setText("");
        KdPetugas.setText("");
        NmPetugas.setText("");
        Status.setSelectedItem("");
        autoNomor();
        Urgensi.requestFocus();
    }

    private void getData() {
         if (tbObat.getSelectedRow() != -1) {
        NoPengajuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
        Valid.SetTgl(Tgl1, tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
        Valid.SetTgl(Tgl2, tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
        KdPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        NmPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        Bidang.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
        Departemen.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
        Urgensi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
        Alamat.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
        Jumlah.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
        Kepentingan.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
        KdPetugasPJ.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
        NmPetugasPJ.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
        Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
        
        // Ambil data cuti dari database setelah load data
        ambilDataCutiDariDB();
    }
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 185));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }
    // ===== 1. METHOD ambilDataCutiDariDB() - SUDAH SESUAI =====

  public void ambilDataCutiDariDB() {
    if (KdPetugas.getText().trim().isEmpty()) {
        Kuota.setText("0");
        Sisacuti.setText("0");
        Kuota1.setText("");
        return;
    }

    String jenisCuti = Urgensi.getSelectedItem().toString();
    Integer kuotaDefault = kuotaCuti.get(jenisCuti);

    if (kuotaDefault == null || kuotaDefault == 0) {
        Kuota.setText("0");
        Sisacuti.setText("0");
        Kuota1.setText("");
        return;
    }

    String nik = KdPetugas.getText();
    PreparedStatement psDataCuti = null;
    ResultSet rsDataCuti = null;

    try {
        // ✅ HITUNG TOTAL CUTI YANG SUDAH DISETUJUI PENUH
        String queryTotalDisetujui = 
            "SELECT IFNULL(SUM(jumlah), 0) as total_disetujui " +
            "FROM pengajuan_cuti " +
            "WHERE nik = ? " +
            "  AND urgensi = ? " +
            "  AND YEAR(tanggal) = YEAR(CURDATE()) " +
            "  AND status_kpl_unit = 'Disetujui' " +
            "  AND status_hrd = 'Disetujui'";
        
        psDataCuti = koneksi.prepareStatement(queryTotalDisetujui);
        psDataCuti.setString(1, nik);
        psDataCuti.setString(2, jenisCuti);
        rsDataCuti = psDataCuti.executeQuery();
        
        int totalYangSudahDisetujui = 0;
        if (rsDataCuti.next()) {
            totalYangSudahDisetujui = rsDataCuti.getInt("total_disetujui");
        }
        
        rsDataCuti.close();
        psDataCuti.close();
        
        // ✅ HITUNG SISA = KUOTA - YANG SUDAH DISETUJUI
        int kuotaAwal = kuotaDefault;
        int sisaCutiSaatIni = kuotaDefault - totalYangSudahDisetujui;
        
        // Pastikan sisa tidak negatif
        if (sisaCutiSaatIni < 0) {
            sisaCutiSaatIni = 0;
        }
        
        System.out.println("═══════════════════════════════");
        System.out.println("PERHITUNGAN SISA CUTI");
        System.out.println("═══════════════════════════════");
        System.out.println("NIK              : " + nik);
        System.out.println("Jenis Cuti       : " + jenisCuti);
        System.out.println("Kuota Awal       : " + kuotaAwal + " hari");
        System.out.println("Sudah Disetujui  : " + totalYangSudahDisetujui + " hari");
        System.out.println("Sisa Tersedia    : " + sisaCutiSaatIni + " hari");
        System.out.println("═══════════════════════════════");

        // Set ke TextField
        Kuota.setText(String.valueOf(kuotaAwal));
        Sisacuti.setText(String.valueOf(sisaCutiSaatIni));

        // Logika keterangan cuti
        if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
            Kuota1.setText("Cuti Pribadi");
        } else {
            Kuota1.setText("Cuti Normatif");
        }

    } catch (Exception e) {
        System.out.println("✗ Error saat mengambil Kuota dan Sisa Cuti: " + e);
        e.printStackTrace();
        Kuota.setText(String.valueOf(kuotaDefault));
        Sisacuti.setText(String.valueOf(kuotaDefault));

        if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
            Kuota1.setText("Cuti Pribadi");
        } else {
            Kuota1.setText("Cuti Normatif");
        }
    } finally {
        try {
            if (rsDataCuti != null) rsDataCuti.close();
            if (psDataCuti != null) psDataCuti.close();
        } catch (Exception e) {
            System.out.println("Error close: " + e);
        }
    }
    }
    // ✅ METHOD UNTUK REFRESH DATA SETELAH SIMPAN
private void refreshDataCuti() {
    if (!KdPetugas.getText().trim().isEmpty()) {
        ambilDataCutiDariDB();
    }
}
    
     private void initKuotaCuti() {
    kuotaCuti.put("Cuti Tahunan", 12);
    kuotaCuti.put("Cuti Hamil", 90);
    kuotaCuti.put("Cuti Menikah", 3);
    kuotaCuti.put("Cuti Khitan Anak", 2);
    kuotaCuti.put("Cuti Menikahkan Anak", 2);
    kuotaCuti.put("Cuti Keluarga Meninggal ( Suami/Istri, Orang Tua/Mertua, Anak)", 2);
    kuotaCuti.put("Cuti Menunggu Keluarga Sakit", 1);
    kuotaCuti.put("Cuti Membabtiskan Anak", 2);
    kuotaCuti.put("Cuti Istri Melahirkan", 2);
    kuotaCuti.put("Cuti Keluarga Meninggal (satu rumah)", 1);
    kuotaCuti.put("Cuti Umroh/Haji", 22);
 }
// ===== 2. METHOD updateSisaCuti() - SUDAH SESUAI =====

   private void updateSisaCuti(String nik, int jumlahCutiDiambil, String jenisCuti) {
    PreparedStatement psUpdate = null;
    ResultSet rsCheck = null;

    try {
        System.out.println("=== MASUK updateSisaCuti ===");
        System.out.println("NIK: " + nik);
        System.out.println("Jenis Cuti: " + jenisCuti);
        System.out.println("Jumlah akan dipotong: " + jumlahCutiDiambil);
        
        // ✅ CEK STATUS LAMA (SEBELUM UPDATE)
        String noPengajuan = NoPengajuan.getText().trim();
        
        // ⚠️ PENTING: Gunakan statusAwal yang disimpan saat getData(), BUKAN dari database
        System.out.println("Status AWAL Kepala Unit: " + statusAwalHRD);
        System.out.println("Status AWAL HRD: " + statusAwalDireksi);
        
        // ❌ JIKA SEBELUMNYA SUDAH DISETUJUI PENUH, JANGAN POTONG LAGI!
        if ("Disetujui".equals(statusAwalHRD) && "Disetujui".equals(statusAwalDireksi)) {
            System.out.println("⚠️ SKIP UPDATE - Cuti ini sudah pernah dipotong sebelumnya!");
            System.out.println("   Status lama KU: " + statusAwalHRD);
            System.out.println("   Status lama HRD: " + statusAwalDireksi);
            return; // KELUAR - JANGAN POTONG LAGI!
        }
        
        System.out.println("✓ Status belum disetujui penuh sebelumnya, lanjut update...");

        // CEK APAKAH RECORD KUOTA SUDAH ADA
        String checkQuery = "SELECT sisa_cuti FROM pengajuan_cuti_kuota "
                + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";
        psUpdate = koneksi.prepareStatement(checkQuery);
        psUpdate.setString(1, nik);
        psUpdate.setString(2, jenisCuti);
        rsCheck = psUpdate.executeQuery();

        Integer kuotaDefault = kuotaCuti.get(jenisCuti);
        if (kuotaDefault == null) kuotaDefault = 0;

        if (!rsCheck.next()) {
            // BELUM ADA RECORD - BUAT BARU
            System.out.println("Record belum ada, membuat baru...");
            rsCheck.close();
            psUpdate.close();

            String insertQuery = "INSERT INTO pengajuan_cuti_kuota "
                    + "(nik, tahun, jenis_cuti, kuota_tahunan, sisa_cuti) "
                    + "VALUES (?, YEAR(CURDATE()), ?, ?, ?)";
            PreparedStatement psInsert = koneksi.prepareStatement(insertQuery);
            psInsert.setString(1, nik);
            psInsert.setString(2, jenisCuti);
            psInsert.setInt(3, kuotaDefault);
            psInsert.setInt(4, kuotaDefault);
            psInsert.executeUpdate();
            psInsert.close();
            
            System.out.println("✓ Record baru dibuat dengan kuota: " + kuotaDefault);
        } else {
            int sisaSebelum = rsCheck.getInt("sisa_cuti");
            System.out.println("Sisa cuti sebelum dipotong: " + sisaSebelum);
            rsCheck.close();
            psUpdate.close();
        }

        // UPDATE SISA CUTI (KURANGI)
        String queryUpdate = "UPDATE pengajuan_cuti_kuota "
                + "SET sisa_cuti = sisa_cuti - ? "
                + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";

        psUpdate = koneksi.prepareStatement(queryUpdate);
        psUpdate.setInt(1, jumlahCutiDiambil);
        psUpdate.setString(2, nik);
        psUpdate.setString(3, jenisCuti);

        int rowsAffected = psUpdate.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("✓ UPDATE BERHASIL! Rows affected: " + rowsAffected);

            // VERIFIKASI HASIL
            psUpdate.close();
            String verifyQuery = "SELECT sisa_cuti FROM pengajuan_cuti_kuota "
                    + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";
            psUpdate = koneksi.prepareStatement(verifyQuery);
            psUpdate.setString(1, nik);
            psUpdate.setString(2, jenisCuti);
            rsCheck = psUpdate.executeQuery();

            if (rsCheck.next()) {
                int sisaSetelah = rsCheck.getInt("sisa_cuti");
                System.out.println("✓ Sisa cuti setelah dipotong: " + sisaSetelah);
            }
        } else {
            System.out.println("⚠️ Tidak ada baris yang ter-update!");
        }

    } catch (Exception e) {
        System.out.println("✗ ERROR updateSisaCuti: " + e);
        e.printStackTrace();
    } finally {
        try {
            if (rsCheck != null) rsCheck.close();
            if (psUpdate != null) psUpdate.close();
        } catch (Exception e) {
            System.out.println("Error close: " + e);
        }
    }
    }
// ===== BONUS: Method untuk Inisialisasi Data Awal (Opsional) =====
// Jalankan method ini SEKALI untuk mengisi data semua pegawai

    public void inisialisasiKuotaSemua() {
        try {
            String query = "INSERT INTO pengajuan_cuti_kuota (nik, tahun, kuota_tahunan, sisa_cuti) "
                    + "SELECT nik, YEAR(CURDATE()), 12, 12 FROM pegawai "
                    + "WHERE nik NOT IN (SELECT nik FROM pengajuan_cuti_kuota WHERE tahun = YEAR(CURDATE()))";

            PreparedStatement ps = koneksi.prepareStatement(query);
            int rows = ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(null,
                    "✓ Berhasil inisialisasi kuota untuk " + rows + " pegawai tahun " + java.time.Year.now());
        } catch (Exception e) {
            System.out.println("Error inisialisasi kuota: " + e);
            e.printStackTrace();
        }
    }
// ===== MODIFIKASI METHOD YANG SUDAH ADA =====

    public void isCek() {
        BtnSimpan.setEnabled(akses.getpengajuan_cuti());
        BtnHapus.setEnabled(akses.getpengajuan_cuti());
        BtnEdit.setEnabled(akses.getpengajuan_cuti());
        Status.setEnabled(akses.getadmin());
    }

    private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(no_pengajuan,3),signed)),0) from pengajuan_cuti where tanggal='" + Valid.SetTgl(Tanggal.getSelectedItem() + "") + "' ",
                "PC" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoPengajuan);
    }

}
