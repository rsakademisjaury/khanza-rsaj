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
import rekammedis.StatusValidasiPengajuanCuti;

/**
 *
 * @author perpustakaan
 */
public final class PengajuanCutiAdminValidasi extends javax.swing.JDialog {

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
    private DlgCariPegawai petugas = new DlgCariPegawai(null, false);
    private DlgCariPegawai petugas1 = new DlgCariPegawai(null, false);
    private boolean isInitializing = true;  // ← TAMBAHKAN INI

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public PengajuanCutiAdminValidasi(java.awt.Frame parent, boolean modal) {
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
            "Status Kepala Unit", "Status HRD" // ← URUTAN BENAR: Kepala Unit dulu, HRD terakhir
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

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
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
        jLabel13 = new widget.Label();
        Sisacuti = new widget.TextBox();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        Kuota = new widget.TextBox();
        jLabel26 = new widget.Label();
        Status = new widget.ComboBox();
        Urgensi = new widget.ComboBox();
        jLabel27 = new widget.Label();
        Kuota1 = new widget.TextBox();
        jLabel96 = new widget.Label();
        Status1 = new widget.ComboBox();
        Status2 = new widget.ComboBox();
        jLabel94 = new widget.Label();
        NIP2 = new widget.TextBox();
        NIP = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        NamaPetugas2 = new widget.TextBox();
        BtnSeekPegawai1 = new widget.Button();
        BtnValidasi = new widget.Button();
        BtnSeekPegawai2 = new widget.Button();
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

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/checked.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Validasi");
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
        jLabel20.setBounds(430, 10, 95, 23);

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

        jLabel13.setText("Sisa Cuti :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(760, 70, 50, 23);

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
        Sisacuti.setBounds(810, 70, 45, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Hari");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(860, 70, 30, 23);

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
        Urgensi.setBounds(530, 10, 159, 23);

        jLabel27.setText("Keterangan Cuti :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(690, 10, 90, 23);

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
        Kuota1.setBounds(780, 10, 150, 23);

        jLabel96.setText("Kepala Unit / Instalasi :");
        jLabel96.setName("jLabel96"); // NOI18N
        FormInput.add(jLabel96);
        jLabel96.setBounds(-10, 160, 130, 23);

        Status1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Pengajuan", "Disetujui", "Ditolak" }));
        Status1.setName("Status1"); // NOI18N
        Status1.setPreferredSize(new java.awt.Dimension(55, 28));
        Status1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Status1KeyPressed(evt);
            }
        });
        FormInput.add(Status1);
        Status1.setBounds(120, 160, 130, 23);

        Status2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Pengajuan", "Disetujui", "Ditolak" }));
        Status2.setName("Status2"); // NOI18N
        Status2.setPreferredSize(new java.awt.Dimension(55, 28));
        Status2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Status2KeyPressed(evt);
            }
        });
        FormInput.add(Status2);
        Status2.setBounds(120, 190, 130, 23);

        jLabel94.setText("HRD :");
        jLabel94.setName("jLabel94"); // NOI18N
        FormInput.add(jLabel94);
        jLabel94.setBounds(20, 190, 90, 23);

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
        NIP2.setBounds(260, 190, 115, 23);

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
        NIP.setBounds(260, 160, 115, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setHighlighter(null);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        NamaPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NamaPetugasActionPerformed(evt);
            }
        });
        FormInput.add(NamaPetugas);
        NamaPetugas.setBounds(380, 160, 330, 23);

        NamaPetugas2.setEditable(false);
        NamaPetugas2.setHighlighter(null);
        NamaPetugas2.setName("NamaPetugas2"); // NOI18N
        NamaPetugas2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NamaPetugas2ActionPerformed(evt);
            }
        });
        FormInput.add(NamaPetugas2);
        NamaPetugas2.setBounds(380, 190, 330, 23);

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
        BtnSeekPegawai1.setBounds(710, 160, 30, 23);

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
        BtnValidasi.setBounds(740, 160, 140, 26);

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
        BtnSeekPegawai2.setBounds(710, 190, 28, 23);

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
         if (tbObat.getSelectedRow() == -1) {
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
            "⚠️ VALIDASI GAGAL!\n\n" +
            "HRD hanya dapat menyetujui jika KEPALA UNIT\n" +
            "sudah menyetujui terlebih dahulu.\n\n" +
            "Urutan validasi:\n" +
            "1. Kepala Unit approve ✓\n" +
            "2. Baru HRD bisa approve", 
            "Validasi Bertingkat", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Cek apakah ada perubahan status
    boolean adaPerubahan = !statusKepalaUnit.equals(statusAwalHRD) || 
                           !statusHRD.equals(statusAwalDireksi);

    if (!adaPerubahan) {
        JOptionPane.showMessageDialog(null, 
            "Tidak ada perubahan status untuk disimpan.", 
            "Informasi", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Konfirmasi
    int confirm = JOptionPane.showConfirmDialog(null,
        "╔═══════════════════════════╗\n" +
        "   KONFIRMASI VALIDASI CUTI\n" +
        "╚═══════════════════════════╝\n\n" +
        "No. Pengajuan : " + noPengajuan + "\n" +
        "Pegawai       : " + NmPetugas.getText() + "\n" +
        "Jenis Cuti    : " + jenisCuti + "\n\n" +
        "┌─────────────────────────┐\n" +
        "│ URUTAN VALIDASI:        │\n" +
        "│ 1. Kepala Unit: " + String.format("%-8s", statusKepalaUnit) + "│\n" +
        "│ 2. HRD        : " + String.format("%-8s", statusHRD) + "│\n" +
        "└─────────────────────────┘\n\n" +
        "Simpan validasi?",
        "Konfirmasi Validasi",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        // ✅ QUERY UPDATE YANG BENAR
        String sql = "UPDATE pengajuan_cuti SET " +
                    "status_kpl_unit = ?, " +  // Parameter 1 = Kepala Unit (Status1)
                    "status_hrd = ? " +         // Parameter 2 = HRD (Status2)
                    "WHERE no_pengajuan = ?";
        
        ps = koneksi.prepareStatement(sql);
        ps.setString(1, statusKepalaUnit);  // Status1
        ps.setString(2, statusHRD);          // Status2
        ps.setString(3, noPengajuan);
        
        int result = ps.executeUpdate();
        
        if (result > 0) {
            System.out.println("✅ UPDATE BERHASIL!");
            System.out.println("   Kepala Unit: " + statusKepalaUnit);
            System.out.println("   HRD        : " + statusHRD);
            
            // Tentukan status final
            String statusFinal = "Proses Pengajuan";
            
            if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Disetujui")) {
                statusFinal = "✅ Disetujui Penuh";
                
                // ✅ PENTING: Update sisa cuti hanya jika disetujui penuh
                int jumlahCuti = Integer.parseInt(Jumlah.getText().trim());
                updateSisaCuti(nikPegawai, jumlahCuti, jenisCuti);
                
                JOptionPane.showMessageDialog(null,
                    "✅ VALIDASI BERHASIL!\n\n" +
                    "Status: " + statusFinal + "\n\n" +
                    "📋 Detail:\n" +
                    "• Kepala Unit: DISETUJUI ✓\n" +
                    "• HRD: DISETUJUI ✓\n" +
                    "• Sisa cuti dikurangi: " + jumlahCuti + " hari",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } else if (statusKepalaUnit.equals("Ditolak") || statusHRD.equals("Ditolak")) {
                statusFinal = "❌ Ditolak";
                
                String alasanTolak = statusKepalaUnit.equals("Ditolak") ? 
                    "Ditolak oleh Kepala Unit" : "Ditolak oleh HRD";
                
                JOptionPane.showMessageDialog(null,
                    "✅ VALIDASI BERHASIL!\n\n" +
                    "Status: " + statusFinal + "\n" +
                    "Alasan: " + alasanTolak,
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Proses pengajuan - menunggu validasi berikutnya
                if (statusKepalaUnit.equals("Disetujui") && statusHRD.equals("Proses Pengajuan")) {
                    statusFinal = "⏳ Menunggu Validasi HRD";
                }
                
                JOptionPane.showMessageDialog(null,
                    "✅ VALIDASI BERHASIL!\n\n" +
                    "Status: " + statusFinal,
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
        /*       if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        } */
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        /*    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        } */
}//GEN-LAST:event_BtnKeluarKeyPressed

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

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_StatusKeyPressed

    private void UrgensiItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_UrgensiItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_UrgensiItemStateChanged

    private void UrgensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UrgensiActionPerformed
        String jenisCuti = Urgensi.getSelectedItem().toString();

        // Set kuota berdasarkan jenis cuti dari Map
        Integer kuota = kuotaCuti.get(jenisCuti);
        if (kuota != null && kuota > 0) {
            Kuota.setText(String.valueOf(kuota));

            // ✓ LOGIKA KETERANGAN CUTI
            if (jenisCuti.equalsIgnoreCase("Cuti Tahunan")) {
                Kuota1.setText("Cuti Pribadi");
            } else {
                Kuota1.setText("Cuti Normatif");
            }

            // Update sisa cuti jika NIK sudah terisi
            if (!KdPetugas.getText().trim().isEmpty()) {
                ambilDataCutiDariDB();
            } else {
                Sisacuti.setText(String.valueOf(kuota));
            }
        } else {
            Kuota.setText("0");
            Sisacuti.setText("0");
            Kuota1.setText("");
        }
    }//GEN-LAST:event_UrgensiActionPerformed

    private void UrgensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UrgensiKeyPressed
        Valid.pindah(evt, btnPetugas, btnPetugasPJ);
    }//GEN-LAST:event_UrgensiKeyPressed

    private void Kuota1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kuota1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kuota1ActionPerformed

    private void Kuota1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Kuota1KeyPressed
        Valid.pindah(evt, Tgl2, Urgensi);
    }//GEN-LAST:event_Kuota1KeyPressed

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

    private void BtnSeekPegawai1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekPegawai1ActionPerformed
        pilihan = 1;
        petugas1.emptTeks();
        //    petugas.isCek();
        petugas1.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas1.setLocationRelativeTo(internalFrame1);
        petugas1.setVisible(true);
    }//GEN-LAST:event_BtnSeekPegawai1ActionPerformed

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

    private void BtnSeekPegawai2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekPegawai2ActionPerformed
        pilihan = 2;
        petugas1.emptTeks();
        //  petugas.isCek();
        petugas1.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas1.setLocationRelativeTo(internalFrame1);
        petugas1.setVisible(true);
    }//GEN-LAST:event_BtnSeekPegawai2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            PengajuanCutiAdminValidasi dialog = new PengajuanCutiAdminValidasi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar;
    private widget.Button BtnSeekPegawai1;
    private widget.Button BtnSeekPegawai2;
    private widget.Button BtnSimpan;
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
        NIP.setText("");
        NamaPetugas.setText("");
        NIP2.setText("");
        NamaPetugas2.setText("");
    }

    private void getData() {
    if (tbObat.getSelectedRow() != -1) {
        int row = tbObat.getSelectedRow();
        
        try {
            // ✅ URUTAN 17 KOLOM (0-16):
            // 0  = No.Pengajuan
            // 1  = Tanggal
            // 2  = Tgl Awal
            // 3  = Tgl Akhir
            // 4  = NIK
            // 5  = Diajukan Oleh
            // 6  = Bidang
            // 7  = Departemen
            // 8  = Jenis Cuti
            // 9  = Alamat Tujuan
            // 10 = Jml Cuti
            // 11 = Kepentingan Cuti
            // 12 = NIK P.J.
            // 13 = P.J. Terkait
            // 14 = Status
            // 15 = Status Kepala Unit
            // 16 = Status HRD
            
            System.out.println("\n========================================");
            System.out.println("MENGAMBIL DATA DARI TABEL - Baris: " + row);
            System.out.println("========================================");
            
            // ✅ KOLOM 0-4: No Pengajuan, Tanggal, NIK
            String noPengajuan = tbObat.getValueAt(row, 0).toString();
            String tanggal = tbObat.getValueAt(row, 1).toString();
            String tglAwal = tbObat.getValueAt(row, 2).toString();
            String tglAkhir = tbObat.getValueAt(row, 3).toString();
            String nik = tbObat.getValueAt(row, 4).toString();
            
            NoPengajuan.setText(noPengajuan);
            Valid.SetTgl(Tanggal, tanggal);
            Valid.SetTgl(Tgl1, tglAwal);
            Valid.SetTgl(Tgl2, tglAkhir);
            KdPetugas.setText(nik);
            
            System.out.println("✓ No Pengajuan: " + noPengajuan);
            System.out.println("✓ Tanggal: " + tanggal);
            System.out.println("✓ Tgl Awal: " + tglAwal);
            System.out.println("✓ Tgl Akhir: " + tglAkhir);
            System.out.println("✓ NIK: " + nik);
            
            // ✅ KOLOM 5-7: Nama, Bidang, Departemen
            String namaPengaju = tbObat.getValueAt(row, 5).toString();
            String bidang = tbObat.getValueAt(row, 6).toString();
            String departemen = tbObat.getValueAt(row, 7).toString();
            
            NmPetugas.setText(namaPengaju);
            Bidang.setText(bidang);
            Departemen.setText(departemen);
            
            System.out.println("✓ Nama: " + namaPengaju);
            System.out.println("✓ Bidang: " + bidang);
            System.out.println("✓ Departemen: " + departemen);
            
            // ✅ KOLOM 8-11: Jenis Cuti, Alamat, Jumlah, Kepentingan
            String jenisCuti = tbObat.getValueAt(row, 8).toString();
            String alamat = tbObat.getValueAt(row, 9).toString();
            String jumlah = tbObat.getValueAt(row, 10).toString();
            String kepentingan = tbObat.getValueAt(row, 11).toString();
            
            Urgensi.setSelectedItem(jenisCuti);
            Alamat.setText(alamat);
            Jumlah.setText(jumlah);
            Kepentingan.setText(kepentingan);
            
            System.out.println("✓ Jenis Cuti: " + jenisCuti);
            System.out.println("✓ Alamat: " + alamat);
            System.out.println("✓ Jumlah: " + jumlah);
            System.out.println("✓ Kepentingan: " + kepentingan);
            
            // ✅ KOLOM 12-13: NIK PJ, Nama PJ
            String nikPJ = tbObat.getValueAt(row, 12).toString();
            String namaPJ = tbObat.getValueAt(row, 13).toString();
            
            KdPetugasPJ.setText(nikPJ);
            NmPetugasPJ.setText(namaPJ);
            
            System.out.println("✓ NIK PJ: " + nikPJ);
            System.out.println("✓ Nama PJ: " + namaPJ);
            
            // ✅ KOLOM 14: Status Utama
            String status = tbObat.getValueAt(row, 14).toString();
            Status.setSelectedItem(status);
            System.out.println("✓ Status: " + status);
            
            // ✅ KOLOM 15-16: Status Kepala Unit & HRD (PALING PENTING!)
            String statusKepalaUnit = tbObat.getValueAt(row, 15).toString();
            String statusHRD = tbObat.getValueAt(row, 16).toString();
            
            Status1.setSelectedItem(statusKepalaUnit);
            Status2.setSelectedItem(statusHRD);
            
            statusAwalHRD = statusKepalaUnit;      // Simpan status awal Kepala Unit
            statusAwalDireksi = statusHRD;         // Simpan status awal HRD
            
            System.out.println("✓ Status Kepala Unit (Status1): " + statusKepalaUnit);
            System.out.println("✓ Status HRD (Status2): " + statusHRD);
            
            // ✅ SIMPAN NIK PEGAWAI untuk penggunaan method lain
            nikPegawai = nik;
            
            System.out.println("\n--- VERIFIKASI FORM SETELAH DIISI ---");
            System.out.println("Form Status1: " + Status1.getSelectedItem());
            System.out.println("Form Status2: " + Status2.getSelectedItem());
            System.out.println("========================================\n");
            
            // ✅ Refresh data cuti SETELAH semua field terisi
            ambilDataCutiDariDB();
            
        } catch (Exception e) {
            System.err.println("❌ ERROR di getData(): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error mengambil data:\n" + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    } else {
        System.out.println("⚠️ Tidak ada baris yang dipilih!");
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
    }
// ===== 2. METHOD updateSisaCuti() - SUDAH SESUAI =====

    private void updateSisaCuti(String nik, int jumlahCutiDiambil, String jenisCuti) {
        PreparedStatement psUpdate = null;
        ResultSet rsCheck = null;

        try {
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
                // Jika belum ada, buat record baru dengan kuota awal
                System.out.println("⚠ Record kuota belum ada, membuat baru...");
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
            } else {
                rsCheck.close();
                psUpdate.close();
            }

            // Update sisa_cuti (KURANGI dengan jumlah cuti yang diambil)
            String queryUpdate = "UPDATE pengajuan_cuti_kuota "
                    + "SET sisa_cuti = sisa_cuti - ? "
                    + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";

            psUpdate = koneksi.prepareStatement(queryUpdate);
            psUpdate.setInt(1, jumlahCutiDiambil);
            psUpdate.setString(2, nik);
            psUpdate.setString(3, jenisCuti);

            int rowsAffected = psUpdate.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Sisa cuti " + jenisCuti + " berhasil dikurangi "
                        + jumlahCutiDiambil + " hari untuk NIK: " + nik);

                // Verifikasi hasil update
                psUpdate.close();
                String verifyQuery = "SELECT sisa_cuti FROM pengajuan_cuti_kuota "
                        + "WHERE nik = ? AND tahun = YEAR(CURDATE()) AND jenis_cuti = ?";
                psUpdate = koneksi.prepareStatement(verifyQuery);
                psUpdate.setString(1, nik);
                psUpdate.setString(2, jenisCuti);
                rsCheck = psUpdate.executeQuery();

                if (rsCheck.next()) {
                    int sisaBaru = rsCheck.getInt("sisa_cuti");
                    System.out.println("✓ Verifikasi: Sisa cuti " + jenisCuti + " = " + sisaBaru + " hari");
                }
            } else {
                System.out.println("✗ Gagal update sisa cuti - record tidak ditemukan");
            }

        } catch (Exception e) {
            System.out.println("✗ Error saat update sisa cuti: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal mengupdate sisa cuti:\n" + e.getMessage(),
                    "Error Update",
                    JOptionPane.ERROR_MESSAGE);
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
        //    BtnHapus.setEnabled(akses.getpengajuan_cuti());
        //      BtnEdit.setEnabled(akses.getpengajuan_cuti());
        Status.setEnabled(akses.getadmin());
    }

    private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(no_pengajuan,3),signed)),0) from pengajuan_cuti where tanggal='" + Valid.SetTgl(Tanggal.getSelectedItem() + "") + "' ",
                "PC" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoPengajuan);
    }

}
