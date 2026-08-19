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
//import bridging.koneksiDBWa;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Base64;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import rekammedis.StatusValidasiPengajuanCuti;

/**
 *
 * @author perpustakaan
 */
public final class PengajuanCutiAdmin extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksiwa;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0, pilihan = 0;
    private double total = 0;
    private String nikPegawai = "", notifwa = "", pesan = "", tanggaljamkirim = "";
    private String statusAwalHRD = "";
    private String statusAwalDireksi = "";
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Map<String, Integer> kuotaCuti = new HashMap<>();
    private DlgCariPegawai petugas = new DlgCariPegawai(null, false);
    private DlgCariPegawai petugas1 = new DlgCariPegawai(null, false);
    private boolean isInitializing = true;  // ← TAMBAHKAN INI

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public PengajuanCutiAdmin(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);

        // Inisialisasi kuota cuti
        initKuotaCuti();
        isInitializing = true;

        // GANTI BAGIAN INI DI CONSTRUCTOR (sekitar baris 70-95)
        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Pengajuan", "Tanggal", "Tgl Awal", "Tgl Akhir", "NIK",
            "Diajukan Oleh", "Bidang", "Departemen", "Jenis Cuti",
            "Alamat Tujuan", "Jml Cuti", "Kepentingan Cuti",
            "NIK P.J.", "P.J. Terkait", "Status",
            "Status Kepala Instalasi", "Status HRD" // ← URUTAN BENAR: Kepala Unit dulu, HRD terakhir
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// ✅ CUSTOM RENDERER DENGAN URUTAN KOLOM YANG BENAR
        tbObat.setDefaultRenderer(Object.class, new WarnaTable() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component cell = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                try {
                    // ✅ URUTAN KOLOM YANG BENAR:
                    String statusKepalaUnit = table.getValueAt(row, 15).toString(); // Kolom 15 = Status Kepala Unit
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

// ✅ SET COLUMN WIDTHS
        for (i = 0; i < 17; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(85);
            } else if (i == 1) {
                column.setPreferredWidth(65);
            } else if (i == 2) {
                column.setPreferredWidth(65);
            } else if (i == 3) {
                column.setPreferredWidth(65);
            } else if (i == 4) {
                column.setPreferredWidth(85);
            } else if (i == 5) {
                column.setPreferredWidth(160);
            } else if (i == 6) {
                column.setPreferredWidth(70);
            } else if (i == 7) {
                column.setPreferredWidth(70);
            } else if (i == 8) {
                column.setPreferredWidth(110);
            } else if (i == 9) {
                column.setPreferredWidth(170);
            } else if (i == 10) {
                column.setPreferredWidth(80);
            } else if (i == 11) {
                column.setPreferredWidth(170);
            } else if (i == 12) {
                column.setPreferredWidth(85);
            } else if (i == 13) {
                column.setPreferredWidth(160);
            } else if (i == 14) {
                column.setPreferredWidth(110);
            } else if (i == 15) {
                column.setPreferredWidth(130); // Status Kepala Unit
            } else if (i == 16) {
                column.setPreferredWidth(120); // Status HRD
            }
        }

        // Input limits
        NoPengajuan.setDocument(new batasInput((int) 17).getKata(NoPengajuan));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        isInitializing = false;
        ChkInput.setSelected(false);
        isForm();

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

        petugas1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (petugas1.getTable().getSelectedRow() != -1) {
                    if (pilihan == 1) {
                        NIP.setText(petugas1.getTable().getValueAt(petugas1.getTable().getSelectedRow(), 0).toString());
                        NamaPetugas.setText(petugas1.getTable().getValueAt(petugas1.getTable().getSelectedRow(), 1).toString());
                        BtnSeekPegawai1.requestFocus();
                    } else if (pilihan == 2) {
                        NIP2.setText(petugas1.getTable().getValueAt(petugas1.getTable().getSelectedRow(), 0).toString());
                        NamaPetugas2.setText(petugas1.getTable().getValueAt(petugas1.getTable().getSelectedRow(), 1).toString());
                        BtnSeekPegawai2.requestFocus();
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

        try {
//            notifwa = koneksiDB.NOTIFWA();
            //    idgroupwa = koneksiDB.IDGROUPWA();
        } catch (Exception e) {
            //   idgroupwa = "no";
        }

        ChkInput.setSelected(false);
        isForm();
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
        MnCutiPegawai = new javax.swing.JMenuItem();
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
        Urgensi = new widget.ComboBox();
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
        jLabel13 = new widget.Label();
        Sisacuti = new widget.TextBox();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        Kuota = new widget.TextBox();
        jLabel26 = new widget.Label();
        jLabel96 = new widget.Label();
        jLabel94 = new widget.Label();
        Status1 = new widget.ComboBox();
        Status2 = new widget.ComboBox();
        NIP2 = new widget.TextBox();
        NIP = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        NamaPetugas2 = new widget.TextBox();
        BtnSeekPegawai2 = new widget.Button();
        BtnSeekPegawai1 = new widget.Button();
        BtnStatusVerifikasi = new widget.Button();
        BtnValidasi = new widget.Button();
        jLabel27 = new widget.Label();
        Kuota1 = new widget.TextBox();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCutiPegawai.setBackground(new java.awt.Color(255, 255, 254));
        MnCutiPegawai.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCutiPegawai.setForeground(new java.awt.Color(50, 50, 50));
        MnCutiPegawai.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCutiPegawai.setText("VALIDASI CUTI PEGAWAI");
        MnCutiPegawai.setName("MnCutiPegawai"); // NOI18N
        MnCutiPegawai.setPreferredSize(new java.awt.Dimension(260, 26));
        MnCutiPegawai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCutiPegawaiActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCutiPegawai);

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
        internalFrame1.setPreferredSize(new java.awt.Dimension(462, 700));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.setPreferredScrollableViewportSize(new java.awt.Dimension(450, 350));
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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-11-2025" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-11-2025" }));
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

        PanelInput.setMinimumSize(new java.awt.Dimension(85, 20));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(85, 325));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(80, 350));
        FormInput.setLayout(null);

        jLabel8.setText("Tgl. Pengajuan :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(226, 10, 99, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-11-2025" }));
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
        jLabel20.setBounds(390, 10, 95, 23);

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
        Urgensi.setBounds(490, 10, 159, 23);

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
        Tgl1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-11-2025" }));
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
        Tgl2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-11-2025" }));
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

        jLabel13.setText("Sisa Cuti :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(770, 70, 50, 23);

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
        Sisacuti.setBounds(820, 70, 45, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Hari");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(870, 70, 30, 23);

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

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel26.setText("Hari/Bulan");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(890, 40, 50, 23);

        jLabel96.setText("Kepala Unit / Instalasi :");
        jLabel96.setName("jLabel96"); // NOI18N
        FormInput.add(jLabel96);
        jLabel96.setBounds(0, 160, 130, 23);

        jLabel94.setText("HRD :");
        jLabel94.setName("jLabel94"); // NOI18N
        FormInput.add(jLabel94);
        jLabel94.setBounds(40, 190, 90, 23);

        Status1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Pengajuan", "Disetujui", "Ditolak" }));
        Status1.setName("Status1"); // NOI18N
        Status1.setPreferredSize(new java.awt.Dimension(55, 28));
        Status1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Status1KeyPressed(evt);
            }
        });
        FormInput.add(Status1);
        Status1.setBounds(140, 160, 130, 23);

        Status2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Pengajuan", "Disetujui", "Ditolak" }));
        Status2.setName("Status2"); // NOI18N
        Status2.setPreferredSize(new java.awt.Dimension(55, 28));
        Status2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Status2KeyPressed(evt);
            }
        });
        FormInput.add(Status2);
        Status2.setBounds(140, 190, 130, 23);

        NIP2.setHighlighter(null);
        NIP2.setName("NIP2"); // NOI18N
        NIP2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NIP2ActionPerformed(evt);
            }
        });
        NIP2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIP2KeyPressed(evt);
            }
        });
        FormInput.add(NIP2);
        NIP2.setBounds(280, 190, 115, 23);

        NIP.setHighlighter(null);
        NIP.setName("NIP"); // NOI18N
        NIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NIPActionPerformed(evt);
            }
        });
        NIP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIPKeyPressed(evt);
            }
        });
        FormInput.add(NIP);
        NIP.setBounds(280, 160, 115, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setHighlighter(null);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        NamaPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NamaPetugasActionPerformed(evt);
            }
        });
        FormInput.add(NamaPetugas);
        NamaPetugas.setBounds(400, 160, 330, 23);

        NamaPetugas2.setEditable(false);
        NamaPetugas2.setHighlighter(null);
        NamaPetugas2.setName("NamaPetugas2"); // NOI18N
        NamaPetugas2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NamaPetugas2ActionPerformed(evt);
            }
        });
        FormInput.add(NamaPetugas2);
        NamaPetugas2.setBounds(400, 190, 330, 23);

        BtnSeekPegawai2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeekPegawai2.setMnemonic('4');
        BtnSeekPegawai2.setToolTipText("ALt+4");
        BtnSeekPegawai2.setName("BtnSeekPegawai2"); // NOI18N
        BtnSeekPegawai2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeekPegawai2ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeekPegawai2);
        BtnSeekPegawai2.setBounds(730, 190, 28, 23);

        BtnSeekPegawai1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeekPegawai1.setMnemonic('4');
        BtnSeekPegawai1.setToolTipText("ALt+4");
        BtnSeekPegawai1.setName("BtnSeekPegawai1"); // NOI18N
        BtnSeekPegawai1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeekPegawai1ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeekPegawai1);
        BtnSeekPegawai1.setBounds(730, 160, 30, 23);

        BtnStatusVerifikasi.setForeground(new java.awt.Color(0, 0, 0));
        BtnStatusVerifikasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/peminjaman.png"))); // NOI18N
        BtnStatusVerifikasi.setMnemonic('4');
        BtnStatusVerifikasi.setText("Validasi Cuti");
        BtnStatusVerifikasi.setToolTipText("ALt+4");
        BtnStatusVerifikasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        // BtnStatusVerifikasi.setGlassColor(new java.awt.Color(255, 153, 153));
        BtnStatusVerifikasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnStatusVerifikasi.setName("BtnStatusVerifikasi"); // NOI18N
        BtnStatusVerifikasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnStatusVerifikasiActionPerformed(evt);
            }
        });
        FormInput.add(BtnStatusVerifikasi);
        BtnStatusVerifikasi.setBounds(760, 160, 140, 26);

        BtnValidasi.setForeground(new java.awt.Color(0, 0, 0));
        BtnValidasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/peminjaman.png"))); // NOI18N
        BtnValidasi.setMnemonic('4');
        BtnValidasi.setText("Status Validasi");
        BtnValidasi.setToolTipText("ALt+4");
        BtnValidasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        // BtnValidasi.setGlassColor(new java.awt.Color(255, 153, 153));
        BtnValidasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnValidasi.setName("BtnValidasi"); // NOI18N
        BtnValidasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnValidasiActionPerformed(evt);
            }
        });
        FormInput.add(BtnValidasi);
        BtnValidasi.setBounds(760, 190, 140, 26);

        jLabel27.setText("Keterangan Cuti :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(650, 10, 90, 23);

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
        Kuota1.setBounds(740, 10, 150, 23);

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
        ChkInput.setMaximumSize(new java.awt.Dimension(85, 20));
        ChkInput.setMinimumSize(new java.awt.Dimension(85, 20));
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 13));
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
        /*        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null,
                    "Silahkan pilih data yang akan divalidasi!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String noPengajuan = NoPengajuan.getText().trim();

        // ✅ MAPPING YANG BENAR:
        // Status1 = Kepala Unit (PERTAMA yang validasi)
        // Status2 = HRD (TERAKHIR yang validasi)
        String statusKepalaUnit = Status1.getSelectedItem().toString();
        String statusHRD = Status2.getSelectedItem().toString();

        String jenisCuti = Urgensi.getSelectedItem().toString();
        String nikPegawai = KdPetugas.getText().trim();

        // ✅ VALIDASI BERTINGKAT: HRD hanya bisa approve jika Kepala Unit sudah approve
        if (statusHRD.equals("Disetujui") && !statusKepalaUnit.equals("Disetujui")) {
            JOptionPane.showMessageDialog(null,
                    "⚠️ VALIDASI GAGAL!\n\n"
                    + "HRD hanya dapat menyetujui jika KEPALA UNIT\n"
                    + "sudah menyetujui terlebih dahulu.\n\n"
                    + "Urutan validasi:\n"
                    + "1. Kepala Unit approve ✓\n"
                    + "2. Baru HRD bisa approve",
                    "Validasi Bertingkat", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cek apakah ada perubahan status
        boolean adaPerubahan = !statusKepalaUnit.equals(statusAwalHRD)
                || !statusHRD.equals(statusAwalDireksi);

        if (!adaPerubahan) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada perubahan status untuk disimpan.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Konfirmasi
        int confirm = JOptionPane.showConfirmDialog(null,
                "╔═══════════════════════════╗\n"
                + "   KONFIRMASI VALIDASI CUTI\n"
                + "╚═══════════════════════════╝\n\n"
                + "No. Pengajuan : " + noPengajuan + "\n"
                + "Pegawai       : " + NmPetugas.getText() + "\n"
                + "Jenis Cuti    : " + jenisCuti + "\n\n"
                + "┌─────────────────────────┐\n"
                + "│ URUTAN VALIDASI:        │\n"
                + "│ 1. Kepala Unit: " + String.format("%-8s", statusKepalaUnit) + "│\n"
                + "│ 2. HRD        : " + String.format("%-8s", statusHRD) + "│\n"
                + "└─────────────────────────┘\n\n"
                + "Simpan validasi?",
                "Konfirmasi Validasi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // ✅ QUERY UPDATE STATUS
            String sql = "UPDATE pengajuan_cuti SET "
                    + "status_kpl_unit = ?, "
                    + "status_hrd = ? "
                    + "WHERE no_pengajuan = ?";

            ps = koneksi.prepareStatement(sql);
            ps.setString(1, statusKepalaUnit);
            ps.setString(2, statusHRD);
            ps.setString(3, noPengajuan);

            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("✅ UPDATE STATUS BERHASIL!");
                System.out.println("   Kepala Unit: " + statusKepalaUnit);
                System.out.println("   HRD        : " + statusHRD);

                // ✅ SIMPAN DATA VALIDATOR (PENTING!)
                simpanDataValidator(noPengajuan, statusKepalaUnit, statusHRD);

                // Tentukan status final
                String statusFinal = "Proses Pengajuan";

                if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Disetujui")) {
                    statusFinal = "✅ Disetujui Penuh";

                    // Update sisa cuti
                    int jumlahCuti = Integer.parseInt(Jumlah.getText().trim());
                    updateSisaCuti(nikPegawai, jumlahCuti, jenisCuti);

                    JOptionPane.showMessageDialog(null,
                            "✅ VALIDASI BERHASIL!\n\n"
                            + "Status: " + statusFinal + "\n\n"
                            + "📋 Detail:\n"
                            + "• Kepala Unit: DISETUJUI ✓\n"
                            + "• HRD: DISETUJUI ✓\n"
                            + "• Sisa cuti dikurangi: " + jumlahCuti + " hari",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);

                } else if (statusKepalaUnit.equals("Ditolak") || statusHRD.equals("Ditolak")) {
                    statusFinal = "❌ Ditolak";

                    String alasanTolak = statusKepalaUnit.equals("Ditolak")
                            ? "Ditolak oleh Kepala Unit" : "Ditolak oleh HRD";

                    JOptionPane.showMessageDialog(null,
                            "✅ VALIDASI BERHASIL!\n\n"
                            + "Status: " + statusFinal + "\n"
                            + "Alasan: " + alasanTolak,
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Proses Pengajuan")) {
                        statusFinal = "⏳ Menunggu Validasi HRD";
                    }

                    JOptionPane.showMessageDialog(null,
                            "✅ VALIDASI BERHASIL!\n\n"
                            + "Status: " + statusFinal,
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                tampil();
                emptTeks();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Gagal menyimpan validasi!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            ps.close();

        } catch (Exception e) {
            System.out.println("❌ ERROR UPDATE: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } */
     if (tbObat.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(null,
                "Silahkan pilih data yang akan divalidasi!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String noPengajuan = NoPengajuan.getText().trim();
    String statusKepalaUnit = Status1.getSelectedItem().toString();
    String statusHRD = Status2.getSelectedItem().toString();
    String jenisCuti = Urgensi.getSelectedItem().toString();
    String nikPegawai = KdPetugas.getText().trim();

    // VALIDASI BERTINGKAT
    if (statusHRD.equals("Disetujui") && !statusKepalaUnit.equals("Disetujui")) {
        JOptionPane.showMessageDialog(null,
                "VALIDASI GAGAL!\n\n"
                + "HRD hanya dapat menyetujui jika KEPALA UNIT\n"
                + "sudah menyetujui terlebih dahulu.\n\n"
                + "Urutan validasi:\n"
                + "1. Kepala Unit approve\n"
                + "2. Baru HRD bisa approve",
                "Validasi Bertingkat", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Cek apakah ada perubahan status
    boolean adaPerubahan = !statusKepalaUnit.equals(statusAwalHRD)
            || !statusHRD.equals(statusAwalDireksi);

    if (!adaPerubahan) {
        JOptionPane.showMessageDialog(null,
                "Tidak ada perubahan status untuk disimpan.",
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // ✅ CEK STATUS
    boolean statusBaruDisetujuiPenuh = statusKepalaUnit.equals("Disetujui")
            && statusHRD.equals("Disetujui");

    boolean statusLamaSudahDisetujuiPenuh = statusAwalHRD.equals("Disetujui")
            && statusAwalDireksi.equals("Disetujui");

    // Konfirmasi
    int confirm = JOptionPane.showConfirmDialog(null,
            "KONFIRMASI VALIDASI CUTI\n\n"
            + "No. Pengajuan : " + noPengajuan + "\n"
            + "Pegawai       : " + NmPetugas.getText() + "\n"
            + "Jenis Cuti    : " + jenisCuti + "\n\n"
            + "URUTAN VALIDASI:\n"
            + "1. Kepala Unit: " + statusKepalaUnit + "\n"
            + "2. HRD        : " + statusHRD + "\n\n"
            + "Simpan validasi?",
            "Konfirmasi Validasi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        // UPDATE STATUS KE DATABASE
        String sql = "UPDATE pengajuan_cuti SET "
                + "status_kpl_unit = ?, "
                + "status_hrd = ? "
                + "WHERE no_pengajuan = ?";

        ps = koneksi.prepareStatement(sql);
        ps.setString(1, statusKepalaUnit);
        ps.setString(2, statusHRD);
        ps.setString(3, noPengajuan);

        int result = ps.executeUpdate();

        if (result > 0) {
            System.out.println("✅ Status berhasil diupdate ke database");

            // SIMPAN DATA VALIDATOR
            simpanDataValidator(noPengajuan, statusKepalaUnit, statusHRD);

            String statusFinal = "Proses Pengajuan";

            // ✅✅✅ KONDISI PENTING: HANYA POTONG JIKA BARU DISETUJUI PENUH
            if (statusBaruDisetujuiPenuh && !statusLamaSudahDisetujuiPenuh) {
                statusFinal = "Disetujui Penuh";

                System.out.println("✅ Status baru disetujui penuh, POTONG sisa cuti!");

                int jumlahCuti = Integer.parseInt(Jumlah.getText().trim());
                updateSisaCuti(nikPegawai, jumlahCuti, jenisCuti);

                // Refresh data cuti setelah update
                ambilDataCutiDariDB();

                // ✅✅✅ KIRIM NOTIFIKASI WA DI SINI (SETELAH DISETUJUI PENUH & HIJAU)
                NotifWaBuktiRegister();

                JOptionPane.showMessageDialog(null,
                        "VALIDASI CUTI BERHASIL!\n\n"
                        + "Status: " + statusFinal + "\n\n"
                        + "Detail:\n"
                        + "• Kepala Unit: DISETUJUI\n"
                        + "• HRD: DISETUJUI\n"
                        + "• Sisa cuti dikurangi: " + jumlahCuti + " hari\n"
                        + "• Notifikasi WhatsApp telah dikirim",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);

            } else if (statusBaruDisetujuiPenuh && statusLamaSudahDisetujuiPenuh) {
                statusFinal = "Disetujui Penuh";

                System.out.println("⚠ Status sudah disetujui sebelumnya, TIDAK POTONG!");

                JOptionPane.showMessageDialog(null,
                        "VALIDASI BERHASIL!\n\n"
                        + "Status: " + statusFinal + "\n\n"
                        + "Catatan: Sisa cuti tidak dipotong karena\n"
                        + "pengajuan ini sudah disetujui sebelumnya.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);

            } else if (statusKepalaUnit.equals("Ditolak") || statusHRD.equals("Ditolak")) {
                statusFinal = "Ditolak";

                String alasanTolak = statusKepalaUnit.equals("Ditolak")
                        ? "Ditolak oleh Kepala Unit" : "Ditolak oleh HRD";

                JOptionPane.showMessageDialog(null,
                        "VALIDASI BERHASIL!\n\n"
                        + "Status: " + statusFinal + "\n"
                        + "Alasan: " + alasanTolak,
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Proses Pengajuan")) {
                    statusFinal = "Menunggu Validasi HRD";
                }

                JOptionPane.showMessageDialog(null,
                        "VALIDASI BERHASIL!\n\n"
                        + "Status: " + statusFinal,
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // REFRESH DATA
            tampil();
            refreshDataCuti();
            emptTeks();

        } else {
            JOptionPane.showMessageDialog(null,
                    "Gagal menyimpan validasi!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        ps.close();

    } catch (Exception e) {
        System.out.println("❌ ERROR UPDATE: " + e);
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
                "Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
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

    private void UrgensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UrgensiKeyPressed
        Valid.pindah(evt, btnPetugas, btnPetugasPJ);
    }//GEN-LAST:event_UrgensiKeyPressed

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

    private void SisacutiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SisacutiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SisacutiActionPerformed

    private void SisacutiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SisacutiKeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_SisacutiKeyPressed

    private void KuotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KuotaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_KuotaActionPerformed

    private void KuotaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KuotaKeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_KuotaKeyPressed

    private void UrgensiItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_UrgensiItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_UrgensiItemStateChanged

    private void UrgensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UrgensiActionPerformed
 // ❌ JANGAN PANGGIL ambilDataCutiDariDB() DI SINI!
    // Biarkan hanya update kuota default saja
    
    String jenisCuti = Urgensi.getSelectedItem().toString();

    // Set kuota default dari Map
    Integer kuota = kuotaCuti.get(jenisCuti);
    if (kuota != null && kuota > 0) {
        Kuota.setText(String.valueOf(kuota));
        
        // Set keterangan cuti
        if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
            Kuota1.setText("Cuti Pribadi");
        } else {
            Kuota1.setText("Cuti Normatif");
        }
        
        // ✅ HANYA panggil ambilDataCutiDariDB() jika NIK sudah terisi DAN bukan saat inisialisasi
        if (!isInitializing && !KdPetugas.getText().trim().isEmpty()) {
            ambilDataCutiDariDB();
        } else {
            // Set sisa = kuota sementara
            Sisacuti.setText(String.valueOf(kuota));
        }
    } else {
        Kuota.setText("0");
        Sisacuti.setText("0");
        Kuota1.setText("");
    }
    }//GEN-LAST:event_UrgensiActionPerformed

    private void Status1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Status1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Status1KeyPressed

    private void Status2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Status2KeyPressed
        Valid.pindah(evt, Jumlah, Kepentingan);
    }//GEN-LAST:event_Status2KeyPressed

    private void NIP2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NIP2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NIP2ActionPerformed

    private void NIP2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIP2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NIP2KeyPressed

    private void NIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NIPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NIPActionPerformed

    private void NIPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIPKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            Sequel.cariIsi("select petugas.nama from petugas where petugas.nik=?", NamaPetugas, NIP.getText());
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            BtnSeekPegawai1ActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, BtnKeluar);
        }
    }//GEN-LAST:event_NIPKeyPressed

    private void NamaPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NamaPetugasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaPetugasActionPerformed

    private void NamaPetugas2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NamaPetugas2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaPetugas2ActionPerformed

    private void BtnSeekPegawai2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekPegawai2ActionPerformed
        pilihan = 2;
        petugas1.emptTeks();
        //  petugas.isCek();
        petugas1.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas1.setLocationRelativeTo(internalFrame1);
        petugas1.setVisible(true);
    }//GEN-LAST:event_BtnSeekPegawai2ActionPerformed

    private void BtnSeekPegawai1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekPegawai1ActionPerformed
        pilihan = 1;
        petugas1.emptTeks();
        //    petugas.isCek();
        petugas1.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas1.setLocationRelativeTo(internalFrame1);
        petugas1.setVisible(true);
    }//GEN-LAST:event_BtnSeekPegawai1ActionPerformed

    private void BtnStatusVerifikasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnStatusVerifikasiActionPerformed
        if (NoPengajuan.getText().trim().equals("") || KdPetugas.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
            TCari.requestFocus();
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PengajuanCutiAdminValidasi form = new PengajuanCutiAdminValidasi(null, false);
            form.isCek();
            form.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());

            // ✅ TAMBAHAN: Refresh data setelah form ditutup
            tampil();
        }
    }//GEN-LAST:event_BtnStatusVerifikasiActionPerformed

    private void BtnValidasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnValidasiActionPerformed
        if (NoPengajuan.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
            TCari.requestFocus();
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            StatusValidasiPengajuanCuti soap = new StatusValidasiPengajuanCuti(null, false);
            soap.setNoRawat(KdPetugas.getText(), NmPetugas.getText());
            soap.setSize(internalFrame1.getWidth(), internalFrame1.getHeight());
            soap.setLocationRelativeTo(internalFrame1);
            soap.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnValidasiActionPerformed

    private void Kuota1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kuota1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kuota1ActionPerformed

    private void Kuota1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Kuota1KeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_Kuota1KeyPressed

    private void MnCutiPegawaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCutiPegawaiActionPerformed
        NotifWaBuktiRegister();
    }//GEN-LAST:event_MnCutiPegawaiActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            PengajuanCutiAdmin dialog = new PengajuanCutiAdmin(new javax.swing.JFrame(), true);
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
    private widget.Button BtnSeekPegawai1;
    private widget.Button BtnSeekPegawai2;
    private widget.Button BtnSimpan;
    private widget.Button BtnStatusVerifikasi;
    private widget.Button BtnValidasi;
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
    private javax.swing.JMenuItem MnCutiPegawai;
    private widget.TextBox NIP;
    private widget.TextBox NIP2;
    private widget.TextBox NamaPetugas;
    private widget.TextBox NamaPetugas2;
    private widget.TextBox NmPetugas;
    private widget.TextBox NmPetugasPJ;
    private widget.TextBox NoPengajuan;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox Sisacuti;
    private widget.ComboBox Status;
    private widget.ComboBox Status1;
    private widget.ComboBox Status2;
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
    private widget.Label jLabel94;
    private widget.Label jLabel96;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
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
        int row = tbObat.getSelectedRow();

        // ✅ SET isInitializing = true SEBELUM load data
        isInitializing = true;

        NoPengajuan.setText(tbObat.getValueAt(row, 0).toString());
        Valid.SetTgl(Tanggal, tbObat.getValueAt(row, 1).toString());
        Valid.SetTgl(Tgl1, tbObat.getValueAt(row, 2).toString());
        Valid.SetTgl(Tgl2, tbObat.getValueAt(row, 3).toString());
        
        // SET NIK DULU!
        KdPetugas.setText(tbObat.getValueAt(row, 4).toString());
        NmPetugas.setText(tbObat.getValueAt(row, 5).toString());
        Bidang.setText(tbObat.getValueAt(row, 6).toString());
        Departemen.setText(tbObat.getValueAt(row, 7).toString());
        
        // Baru set jenis cuti (ini akan trigger UrgensiActionPerformed)
        Urgensi.setSelectedItem(tbObat.getValueAt(row, 8).toString());
        
        Alamat.setText(tbObat.getValueAt(row, 9).toString());
        Jumlah.setText(tbObat.getValueAt(row, 10).toString());
        Kepentingan.setText(tbObat.getValueAt(row, 11).toString());
        KdPetugasPJ.setText(tbObat.getValueAt(row, 12).toString());
        NmPetugasPJ.setText(tbObat.getValueAt(row, 13).toString());
        Status.setSelectedItem(tbObat.getValueAt(row, 14).toString());

        // Set status validasi
        if (tabMode.getColumnCount() > 15) {
            Status1.setSelectedItem(tbObat.getValueAt(row, 15).toString());
            statusAwalHRD = tbObat.getValueAt(row, 15).toString();
        }
        if (tabMode.getColumnCount() > 16) {
            Status2.setSelectedItem(tbObat.getValueAt(row, 16).toString());
            statusAwalDireksi = tbObat.getValueAt(row, 16).toString();
        }

        // ✅ SET isInitializing = false SEBELUM ambil data cuti
        isInitializing = false;

        // ✅ Ambil data cuti TERAKHIR (setelah semua field terisi)
        ambilDataCutiDariDB();

        System.out.println("=== GET DATA ===");
        System.out.println("NIK: " + KdPetugas.getText());
        System.out.println("Jenis Cuti: " + Urgensi.getSelectedItem());
        System.out.println("Sisa Cuti: " + Sisacuti.getText());
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

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, this.getHeight() - 350));
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

    /* public void ambilDataCutiDariDB() {
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
            // Query untuk mendapatkan sisa cuti berdasarkan jenis cuti
            String queryHakCuti = "SELECT kuota_tahunan, sisa_cuti FROM pengajuan_cuti_kuota "
                    + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";

            psDataCuti = koneksi.prepareStatement(queryHakCuti);
            psDataCuti.setString(1, nik);
            psDataCuti.setString(2, jenisCuti);
            rsDataCuti = psDataCuti.executeQuery();

            int kuotaAwal = kuotaDefault;
            int sisaCutiSaatIni = kuotaDefault;

            if (rsDataCuti.next()) {
                kuotaAwal = rsDataCuti.getInt("kuota_tahunan");
                sisaCutiSaatIni = rsDataCuti.getInt("sisa_cuti");

                System.out.println("✓ Data cuti ditemukan - NIK: " + nik
                        + " | Jenis: " + jenisCuti
                        + " | Kuota: " + kuotaAwal
                        + " | Sisa: " + sisaCutiSaatIni);
            } else {
                // Jika belum ada record, buat record baru
                System.out.println("⚠ Data cuti belum ada, membuat record baru untuk NIK: " + nik);

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

                kuotaAwal = kuotaDefault;
                sisaCutiSaatIni = kuotaDefault;
            }

            // Set ke TextField
            Kuota.setText(String.valueOf(kuotaAwal));
            Sisacuti.setText(String.valueOf(sisaCutiSaatIni));

            // ✓ LOGIKA KETERANGAN CUTI
            if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
                Kuota1.setText("Cuti Pribadi");
            } else {
                Kuota1.setText("Cuti Normatif");
            }

            // Warning jika sisa habis atau menipis
            if (sisaCutiSaatIni == 0) {
                JOptionPane.showMessageDialog(null,
                        "⚠ PERINGATAN!\n\n"
                        + "Kuota " + jenisCuti + " untuk:\n"
                        + NmPetugas.getText() + " (NIK: " + nik + ")\n\n"
                        + "SUDAH HABIS!",
                        "Kuota Cuti Habis",
                        JOptionPane.WARNING_MESSAGE);
            } else if (sisaCutiSaatIni <= 3 && sisaCutiSaatIni > 0
                    && !jenisCuti.equals("Cuti Hamil") && !jenisCuti.equals("Cuti Umroh/Haji")) {
                JOptionPane.showMessageDialog(null,
                        "⚠ Perhatian!\n\n"
                        + "Sisa " + jenisCuti + " " + NmPetugas.getText()
                        + " tinggal " + sisaCutiSaatIni + " hari",
                        "Sisa Cuti Menipis",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("✗ Error saat mengambil Kuota dan Sisa Cuti: " + e);
            e.printStackTrace();
            Kuota.setText(String.valueOf(kuotaDefault));
            Sisacuti.setText(String.valueOf(kuotaDefault));

            // Set keterangan default
            if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
                Kuota1.setText("Cuti Pribadi");
            } else {
                Kuota1.setText("Cuti Normatif");
            }

            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil data cuti:\n" + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rsDataCuti != null) {
                    rsDataCuti.close();
                }
                if (psDataCuti != null) {
                    psDataCuti.close();
                }
            } catch (Exception e) {
                System.out.println("Error close: " + e);
            }
        }
    } */
    // ===== METHOD 1: ambilDataCutiDariDB() =====
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

    try {
        // ✅ HITUNG TOTAL CUTI YANG BENAR-BENAR DISETUJUI (HIJAU)
        String queryTotalDisetujui = 
            "SELECT COALESCE(SUM(jumlah), 0) as total " +
            "FROM pengajuan_cuti " +
            "WHERE nik = ? " +
            "AND YEAR(tanggal) = YEAR(CURDATE()) " +
            "AND urgensi = ? " +
            "AND status_kpl_unit = 'Disetujui' " +  // ✅ Harus disetujui Kepala Unit
            "AND status_hrd = 'Disetujui'";          // ✅ Harus disetujui HRD

        PreparedStatement psTotal = koneksi.prepareStatement(queryTotalDisetujui);
        psTotal.setString(1, nik);
        psTotal.setString(2, jenisCuti);
        ResultSet rsTotal = psTotal.executeQuery();

        int totalDisetujui = 0;
        if (rsTotal.next()) {
            totalDisetujui = rsTotal.getInt("total");
        }
        rsTotal.close();
        psTotal.close();

        // ✅ HITUNG SISA CUTI YANG BENAR
        int sisaCutiBenar = kuotaDefault - totalDisetujui;

        // Set ke TextField
        Kuota.setText(String.valueOf(kuotaDefault));
        Sisacuti.setText(String.valueOf(sisaCutiBenar));

        // Set keterangan cuti
        if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
            Kuota1.setText("Cuti Pribadi");
        } else {
            Kuota1.setText("Cuti Normatif");
        }

        // ✅ DEBUG LOG
        System.out.println("========================================");
        System.out.println("HITUNG SISA CUTI");
        System.out.println("NIK: " + nik);
        System.out.println("Jenis: " + jenisCuti);
        System.out.println("Kuota Default: " + kuotaDefault);
        System.out.println("Total Disetujui (Hijau): " + totalDisetujui);
        System.out.println("Sisa Cuti: " + sisaCutiBenar);
        System.out.println("========================================");

    } catch (Exception e) {
        System.out.println("✗ Error ambil data cuti: " + e);
        e.printStackTrace();

        // Fallback ke kuota default
        Kuota.setText(String.valueOf(kuotaDefault));
        Sisacuti.setText(String.valueOf(kuotaDefault));

        if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
            Kuota1.setText("Cuti Pribadi");
        } else {
            Kuota1.setText("Cuti Normatif");
        }
        }
    }
    // ✅ METHOD UNTUK REFRESH DATA SETELAH SIMPAN

    private void refreshDataCuti() {
        if (!KdPetugas.getText().trim().isEmpty()) {
            ambilDataCutiDariDB();
        }
    }
// ===== METHOD 2: updateSisaCuti() =====

    private void updateSisaCuti(String nik, int jumlahCutiDiambil, String jenisCuti) {
        PreparedStatement psUpdate = null;
        ResultSet rsCheck = null;

        try {
            System.out.println("========================================");
            System.out.println("UPDATE SISA CUTI");
            System.out.println("NIK: " + nik);
            System.out.println("Jenis: " + jenisCuti);
            System.out.println("Jumlah Potong: " + jumlahCutiDiambil);
            System.out.println("========================================");

            // Cek apakah sudah ada record
            String checkQuery = "SELECT sisa_cuti FROM pengajuan_cuti_kuota "
                    + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";
            psUpdate = koneksi.prepareStatement(checkQuery);
            psUpdate.setString(1, nik);
            psUpdate.setString(2, jenisCuti);
            rsCheck = psUpdate.executeQuery();

            Integer kuotaDefault = kuotaCuti.get(jenisCuti);
            if (kuotaDefault == null) {
                kuotaDefault = 0;
            }

            if (!rsCheck.next()) {
                // Jika belum ada, buat record baru dulu
                rsCheck.close();
                psUpdate.close();

                System.out.println("Record belum ada, buat baru dulu...");

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

                System.out.println("Record baru dibuat dengan kuota: " + kuotaDefault);
            } else {
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
                System.out.println("✓ Berhasil kurangi " + jumlahCutiDiambil + " hari");

                // Verifikasi hasil
                psUpdate.close();
                String verifyQuery = "SELECT sisa_cuti FROM pengajuan_cuti_kuota "
                        + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";
                psUpdate = koneksi.prepareStatement(verifyQuery);
                psUpdate.setString(1, nik);
                psUpdate.setString(2, jenisCuti);
                rsCheck = psUpdate.executeQuery();

                if (rsCheck.next()) {
                    int sisaBaru = rsCheck.getInt("sisa_cuti");
                    System.out.println("✓ Sisa cuti sekarang: " + sisaBaru + " hari");
                }
            }
            System.out.println("========================================");

        } catch (Exception e) {
            System.out.println("✗ Error update sisa cuti: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (rsCheck != null) {
                    rsCheck.close();
                }
                if (psUpdate != null) {
                    psUpdate.close();
                }
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

    private void NotifWaBuktiRegister() {
  // Ambil data pegawai
    String tgl_lahir = Sequel.cariIsi("SELECT DATE_FORMAT(peg.tgl_lahir,'%d-%m-%Y') FROM pegawai peg LEFT JOIN petugas pet ON peg.nik = pet.nip WHERE peg.nik = '" + KdPetugas.getText() + "'");
    String jk = Sequel.cariIsi("SELECT peg.jk FROM pegawai peg WHERE peg.nik = '" + KdPetugas.getText() + "'");
    String noTelpPasien = Sequel.cariIsi("SELECT pet.no_telp FROM pegawai peg LEFT JOIN petugas pet ON peg.nik = pet.nip WHERE peg.nik = '" + KdPetugas.getText() + "'");

    System.out.println("========================================");
    System.out.println("CEK DATA WA:");
    System.out.println("NIK: " + KdPetugas.getText());
    System.out.println("No Telp ASLI: " + noTelpPasien);
    
    // ✅ VALIDASI NOMOR TELEPON
    if (noTelpPasien == null || noTelpPasien.trim().isEmpty()) {
        System.out.println("❌ Nomor telepon kosong!");
        JOptionPane.showMessageDialog(null, 
            "Nomor telepon pegawai tidak ditemukan!\n\n" +
            "Pastikan NIK: " + KdPetugas.getText() + "\n" +
            "sudah terdaftar di tabel petugas dengan no_telp yang valid.",
            "Data Tidak Lengkap", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // ✅ BERSIHKAN DAN FORMAT NOMOR
    // Hapus semua karakter non-digit
    noTelpPasien = noTelpPasien.replaceAll("[^0-9]", "");
    
    // Jika diawali 0, ganti dengan 62
    if (noTelpPasien.startsWith("0")) {
        noTelpPasien = "62" + noTelpPasien.substring(1);
    }
    
    // Jika tidak diawali 62, tambahkan 62
    if (!noTelpPasien.startsWith("62")) {
        noTelpPasien = "62" + noTelpPasien;
    }
    
    System.out.println("No Telp SETELAH FORMAT: " + noTelpPasien);
    
    // Validasi panjang (minimal 10 digit setelah 62)
    if (noTelpPasien.length() < 12) {
        System.out.println("❌ Nomor terlalu pendek: " + noTelpPasien);
        JOptionPane.showMessageDialog(null, 
            "Nomor telepon tidak valid!\n\n" +
            "No Telp: " + noTelpPasien + "\n" +
            "Format harus: 62xxx (min 12 digit)",
            "Format Nomor Salah", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    if (noTelpPasien.length() > 15) {
        System.out.println("❌ Nomor terlalu panjang: " + noTelpPasien);
        JOptionPane.showMessageDialog(null, 
            "Nomor telepon tidak valid!\n\n" +
            "No Telp: " + noTelpPasien + "\n" +
            "Maksimal 15 digit",
            "Format Nomor Salah", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    System.out.println("Tgl Lahir: " + tgl_lahir);
    System.out.println("JK: " + jk);
    System.out.println("Notif WA: " + notifwa);
    System.out.println("========================================");

    if (notifwa.equals("yes")) {
//        koneksiwa = koneksiDBWA.condb();
        
        if (koneksiwa == null) {
            System.out.println("❌ Koneksi WA gagal!");
            JOptionPane.showMessageDialog(null, 
                "Koneksi database WhatsApp gagal!\n\n" +
                "Periksa konfigurasi koneksi WA.",
                "Error Koneksi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        // ✅✅✅ WAJIB: SET UTF8MB4 DI AWAL KONEKSI
    try {
        Statement stmtCharset = koneksiwa.createStatement();
        stmtCharset.execute("SET NAMES utf8mb4");
        stmtCharset.execute("SET CHARACTER SET utf8mb4");
        stmtCharset.execute("SET character_set_connection=utf8mb4");
        stmtCharset.execute("SET character_set_results=utf8mb4");
        stmtCharset.execute("SET character_set_client=utf8mb4");
        stmtCharset.close();
        System.out.println("✅ UTF8MB4 charset berhasil di-set");
    } catch (Exception e) {
        System.out.println("❌ Gagal set UTF8MB4: " + e.getMessage());
        e.printStackTrace();
        return;
    }
        // Format pesan
        String jenisCuti = Urgensi.getSelectedItem().toString();
        String nikPegawai = KdPetugas.getText().trim();

        Integer kuotaDefault = kuotaCuti.get(jenisCuti);
        if (kuotaDefault == null) {
            kuotaDefault = 12;
        }

        String totalCutiDisetujui = Sequel.cariIsi(
                "SELECT COALESCE(SUM(jumlah), 0) FROM pengajuan_cuti "
                + "WHERE nik = '" + nikPegawai + "' "
                + "AND YEAR(tanggal) = YEAR(CURDATE()) "
                + "AND urgensi = '" + jenisCuti + "' "
                + "AND status_kpl_unit = 'Disetujui' "
                + "AND status_hrd = 'Disetujui'"
        );

        int totalDisetujui = 0;
        try {
            totalDisetujui = Integer.parseInt(totalCutiDisetujui);
        } catch (Exception e) {
            totalDisetujui = 0;
        }

        int sisaCutiBenar = kuotaDefault - totalDisetujui;
        if (sisaCutiBenar < 0) {
            sisaCutiBenar = 0;
        }

     // ❌ HAPUS INI (PAKAI EMOJI)
pesan = "==========================================\n"
        + "*** BUKTI VALIDASI CUTI ***\n"
        + "==========================================\n"
        + "| RS: " + akses.getnamars() + "\n"
        + "| Telp: " + akses.getkontakrs() + "\n"
        + "==========================================\n\n"
        + "* [DATA CUTI]\n"
        + "Tanggal Cuti : " + Valid.SetTgl(Tgl1.getSelectedItem() + "") + "\n"
        + "s/d          : " + Valid.SetTgl(Tgl2.getSelectedItem() + "") + "\n"
        + "No Pengajuan : " + NoPengajuan.getText() + "\n\n"
        + "* [DATA PEGAWAI]\n"
        + "NIK          : " + nikPegawai + "\n"
        + "Nama         : " + NmPetugas.getText() + "\n"
        + "- Tgl Lahir    : " + (tgl_lahir != null ? tgl_lahir : "-") + "\n"
        + "- Jenis Kelamin: " + (jk != null ? jk : "-") + "\n"
        + "- Alamat Tujuan: " + Alamat.getText() + "\n\n"
        + "* [DETAIL CUTI]\n"
        + "Kepentingan  : " + Kepentingan.getText() + "\n"
        + "- Jenis Cuti   : " + jenisCuti + "\n"
        + "- Jumlah Hari  : " + Jumlah.getText() + " hari\n"
        + "- Sisa Cuti    : " + sisaCutiBenar + " hari\n\n"
        + "==========================================\n"
        + "*** STATUS: DISETUJUI PENUH ***\n"
        + "  + Kepala Instalasi : DISETUJUI\n"
        + "  + HRD              : DISETUJUI\n"
        + "==========================================\n"
        + "KESEMBUHAN ANDA HARAPAN KAMI";
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tanggaljamkirim = dateformat.format(new Date());

        try {
            // ✅ FORMAT NOMOR DENGAN @c.us
            String nomorWA = noTelpPasien + "@c.us";
            
            System.out.println("========================================");
            System.out.println("AKAN INSERT KE wa_outbox:");
            System.out.println("Nomor WA: " + nomorWA);
            System.out.println("Panjang Pesan: " + pesan.length() + " karakter");
            System.out.println("========================================");
            
            String sql = "INSERT INTO wa_outbox (NOMOR, NOWA, PESAN, TANGGAL_JAM, STATUS, SOURCE, SENDER, SUCCESS, RESPONSE, REQUEST, TYPE, FILE) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = koneksiwa.prepareStatement(sql);
            ps.setLong(1, 0);
            ps.setString(2, nomorWA);  // ✅ GUNAKAN NOMOR YANG SUDAH DIFORMAT
            ps.setString(3, pesan);
            ps.setString(4, tanggaljamkirim);
            ps.setString(5, "ANTRIAN");
            ps.setString(6, "KHANZA");
            ps.setString(7, "NODEJS");
            ps.setString(8, "");
            ps.setString(9, "");
            ps.setString(10, "");
            ps.setString(11, "TEXT");
            ps.setString(12, "");

            int inserted = ps.executeUpdate();
            
            if (inserted > 0) {
                System.out.println("========================================");
                System.out.println("✅✅✅ WA BERHASIL MASUK KE wa_outbox!");
                System.out.println("   Penerima: " + NmPetugas.getText());
                System.out.println("   No WA: " + nomorWA);
                System.out.println("   Jenis Cuti: " + jenisCuti);
                System.out.println("   Jumlah: " + Jumlah.getText() + " hari");
                System.out.println("   Sisa Cuti: " + sisaCutiBenar + " hari");
                System.out.println("   Status: DISETUJUI PENUH");
                System.out.println("========================================");
                
                JOptionPane.showMessageDialog(null, 
                    "✅ Notifikasi WhatsApp berhasil dikirim!\n\n" +
                    "Penerima: " + NmPetugas.getText() + "\n" +
                    "No WA: " + nomorWA + "\n\n" +
                    "Pesan akan diproses oleh NodeJS Sender",
                    "WA Terkirim", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("❌ INSERT GAGAL (0 rows)");
            }
            
            ps.close();
            
        } catch (Exception e) {
            System.out.println("========================================");
            System.out.println("❌ ERROR INSERT WA:");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            JOptionPane.showMessageDialog(null, 
                "Gagal mengirim notifikasi WhatsApp!\n\n" +
                "Error: " + e.getMessage(),
                "Error WA", 
                JOptionPane.ERROR_MESSAGE);
        }
    } else {
        System.out.println("⚠️ Notif WA tidak aktif (notifwa != yes)");
        System.out.println("   Untuk mengaktifkan, set notifwa='yes' di konfigurasi");
    }
    }

    private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(no_pengajuan,3),signed)),0) from pengajuan_cuti where tanggal='" + Valid.SetTgl(Tanggal.getSelectedItem() + "") + "' ",
                "PC" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoPengajuan);
    }

    private void simpanDataValidator(String noPengajuan, String statusKepalaUnit, String statusHRD) {
        PreparedStatement psValidator = null;
        ResultSet rsCheck = null;

        try {
            // Ambil tanggal dan jam saat ini
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm:ss");
            String tglSekarang = sdfDate.format(new java.util.Date());
            String jamSekarang = sdfTime.format(new java.util.Date());

            // Cek apakah record sudah ada
            String checkQuery = "SELECT no_pengajuan FROM pengajuan_cuti_validasi WHERE no_pengajuan = ?";
            psValidator = koneksi.prepareStatement(checkQuery);
            psValidator.setString(1, noPengajuan);
            rsCheck = psValidator.executeQuery();

            if (rsCheck.next()) {
                // ✅ RECORD SUDAH ADA - UPDATE
                rsCheck.close();
                psValidator.close();

                StringBuilder updateQuery = new StringBuilder("UPDATE pengajuan_cuti_validasi SET ");
                boolean needComma = false;

                // Update Kepala Unit jika statusnya berubah dari status awal
                if (!statusKepalaUnit.equals(statusAwalHRD) && !statusKepalaUnit.equals("Proses Pengajuan")) {
                    updateQuery.append("nik_validator_kpl_unit = ?, ");
                    updateQuery.append("tgl_validasi_kpl_unit = ?, ");
                    updateQuery.append("jam_validasi_kpl_unit = ?");
                    needComma = true;
                }

                // Update HRD jika statusnya berubah dari status awal
                if (!statusHRD.equals(statusAwalDireksi) && !statusHRD.equals("Proses Pengajuan")) {
                    if (needComma) {
                        updateQuery.append(", ");
                    }
                    updateQuery.append("nik_validator_hrd = ?, ");
                    updateQuery.append("tgl_validasi_hrd = ?, ");
                    updateQuery.append("jam_validasi_hrd = ?");
                }

                updateQuery.append(" WHERE no_pengajuan = ?");

                psValidator = koneksi.prepareStatement(updateQuery.toString());
                int paramIndex = 1;

                // Set parameter Kepala Unit
                if (!statusKepalaUnit.equals(statusAwalHRD) && !statusKepalaUnit.equals("Proses Pengajuan")) {
                    psValidator.setString(paramIndex++, NIP.getText().trim().isEmpty() ? "-" : NIP.getText().trim());
                    psValidator.setString(paramIndex++, tglSekarang);
                    psValidator.setString(paramIndex++, jamSekarang);
                }

                // Set parameter HRD
                if (!statusHRD.equals(statusAwalDireksi) && !statusHRD.equals("Proses Pengajuan")) {
                    psValidator.setString(paramIndex++, NIP2.getText().trim().isEmpty() ? "-" : NIP2.getText().trim());
                    psValidator.setString(paramIndex++, tglSekarang);
                    psValidator.setString(paramIndex++, jamSekarang);
                }

                psValidator.setString(paramIndex, noPengajuan);
                psValidator.executeUpdate();

                System.out.println("✅ Data validator berhasil diupdate");

            } else {
                // ✅ RECORD BELUM ADA - INSERT BARU
                rsCheck.close();
                psValidator.close();

                String insertQuery = "INSERT INTO pengajuan_cuti_validasi "
                        + "(no_pengajuan, nik_validator_kpl_unit, tgl_validasi_kpl_unit, jam_validasi_kpl_unit, "
                        + "nik_validator_hrd, tgl_validasi_hrd, jam_validasi_hrd) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                psValidator = koneksi.prepareStatement(insertQuery);
                psValidator.setString(1, noPengajuan);

                // Kepala Unit
                if (!statusKepalaUnit.equals("Proses Pengajuan")) {
                    psValidator.setString(2, NIP.getText().trim().isEmpty() ? "-" : NIP.getText().trim());
                    psValidator.setString(3, tglSekarang);
                    psValidator.setString(4, jamSekarang);
                } else {
                    psValidator.setString(2, null);
                    psValidator.setString(3, null);
                    psValidator.setString(4, null);
                }

                // HRD
                if (!statusHRD.equals("Proses Pengajuan")) {
                    psValidator.setString(5, NIP2.getText().trim().isEmpty() ? "-" : NIP2.getText().trim());
                    psValidator.setString(6, tglSekarang);
                    psValidator.setString(7, jamSekarang);
                } else {
                    psValidator.setString(5, null);
                    psValidator.setString(6, null);
                    psValidator.setString(7, null);
                }

                psValidator.executeUpdate();
                System.out.println("✅ Data validator baru berhasil disimpan");
            }

        } catch (Exception e) {
            System.out.println("❌ Error simpan validator: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (rsCheck != null) {
                    rsCheck.close();
                }
                if (psValidator != null) {
                    psValidator.close();
                }
            } catch (Exception e) {
                System.out.println("Error close: " + e);
            }
        }
    }
}
