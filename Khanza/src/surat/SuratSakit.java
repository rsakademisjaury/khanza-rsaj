/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * kontribusi dari dokter Salim Mulyana
 */
package surat;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.validasi2;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;

/**
 *
 * @author salimmulyana
 */
public final class SuratSakit extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private validasi2 Valid1 = new validasi2();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private String tgl, finger = "", kodedokter = "", namadokter = "", date1, date2, bln_angka = "", bln_romawi = "";
    private DlgCariDokter dokter = new DlgCariDokter(null, false);
    private String nomorSuratGenerated = "";

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public SuratSakit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Surat Sakit", "No.Rawat", "No.R.M.", "Nama Pasien", "Dari Tanggal", "Sampai Tanggal", "Lama Sakit", "Kode Dokter", "Nama Dokter"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(105);
            } else if (i == 1) {
                column.setPreferredWidth(105);
            } else if (i == 2) {
                column.setPreferredWidth(70);
            } else if (i == 3) {
                column.setPreferredWidth(170);
            } else if (i == 4) {
                column.setPreferredWidth(90);
            } else if (i == 5) {
                column.setPreferredWidth(90);
            } else if (i == 6) {
                column.setPreferredWidth(100);
            } else if (i == 7) {
                column.setPreferredWidth(100);
            } else if (i == 8) {
                column.setPreferredWidth(250);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        NoSurat.setDocument(new batasInput((byte) 17).getKata(NoSurat));
        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        LamaSakit.setDocument(new batasInput((byte) 20).getKata(LamaSakit));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
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
                    KdDokter.requestFocus();
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakSuratSakit = new javax.swing.JMenuItem();
        MnCetakSuratSakit1 = new javax.swing.JMenuItem();
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
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel3 = new widget.Label();
        NoSurat = new widget.TextBox();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TanggalAkhir = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        LamaSakit = new widget.TextBox();
        jLabel16 = new widget.Label();
        jLabel18 = new widget.Label();
        TanggalAwal = new widget.Tanggal();
        jLabel13 = new widget.Label();
        Status = new widget.ComboBox();
        jLabel20 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakSuratSakit.setBackground(new java.awt.Color(250, 250, 250));
        MnCetakSuratSakit.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratSakit.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSuratSakit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratSakit.setText("Cetak Surat Keterangan Istirahat");
        MnCetakSuratSakit.setName("MnCetakSuratSakit"); // NOI18N
        MnCetakSuratSakit.setPreferredSize(new java.awt.Dimension(200, 30));
        MnCetakSuratSakit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratSakitActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratSakit);

        MnCetakSuratSakit1.setBackground(new java.awt.Color(250, 250, 250));
        MnCetakSuratSakit1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratSakit1.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSuratSakit1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratSakit1.setText("Cetak Surat Sakit 2");
        MnCetakSuratSakit1.setEnabled(false);
        MnCetakSuratSakit1.setName("MnCetakSuratSakit1"); // NOI18N
        MnCetakSuratSakit1.setPreferredSize(new java.awt.Dimension(200, 30));
        MnCetakSuratSakit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratSakit1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratSakit1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Surat Keterangan Sakit ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
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
        BtnSimpan.setPreferredSize(new java.awt.Dimension(85, 30));
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
        BtnBatal.setPreferredSize(new java.awt.Dimension(85, 30));
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
        BtnHapus.setPreferredSize(new java.awt.Dimension(85, 30));
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
        BtnEdit.setPreferredSize(new java.awt.Dimension(85, 30));
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
        BtnPrint.setPreferredSize(new java.awt.Dimension(85, 30));
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
        jLabel7.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(85, 30));
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

        jLabel19.setText("Tgl. Surat :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(67, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-01-2025" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-01-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(205, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnCari.setIconTextGap(8);
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(70, 23));
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
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(85, 30));
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

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(300, 130));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(200, 300));
        FormInput.setLayout(null);

        jLabel3.setText("No. Surat Sakit :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(0, 40, 95, 23);

        NoSurat.setEditable(false);
        NoSurat.setHighlighter(null);
        NoSurat.setName("NoSurat"); // NOI18N
        NoSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoSuratKeyPressed(evt);
            }
        });
        FormInput.add(NoSurat);
        NoSurat.setBounds(99, 40, 141, 23);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 95, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(99, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(355, 10, 360, 23);

        TanggalAkhir.setForeground(new java.awt.Color(50, 70, 50));
        TanggalAkhir.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-01-2025" }));
        TanggalAkhir.setDisplayFormat("dd-MM-yyyy");
        TanggalAkhir.setName("TanggalAkhir"); // NOI18N
        TanggalAkhir.setOpaque(false);
        TanggalAkhir.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalAkhirItemStateChanged(evt);
            }
        });
        TanggalAkhir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalAkhirActionPerformed(evt);
            }
        });
        TanggalAkhir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalAkhirKeyPressed(evt);
            }
        });
        FormInput.add(TanggalAkhir);
        TanggalAkhir.setBounds(460, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(242, 10, 111, 23);

        LamaSakit.setHighlighter(null);
        LamaSakit.setName("LamaSakit"); // NOI18N
        LamaSakit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LamaSakitActionPerformed(evt);
            }
        });
        LamaSakit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LamaSakitKeyPressed(evt);
            }
        });
        FormInput.add(LamaSakit);
        LamaSakit.setBounds(630, 40, 200, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("s.d.");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(430, 40, 27, 23);

        jLabel18.setText("Lama Sakit :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(550, 40, 70, 23);

        TanggalAwal.setForeground(new java.awt.Color(50, 70, 50));
        TanggalAwal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-01-2025" }));
        TanggalAwal.setDisplayFormat("dd-MM-yyyy");
        TanggalAwal.setName("TanggalAwal"); // NOI18N
        TanggalAwal.setOpaque(false);
        TanggalAwal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalAwalItemStateChanged(evt);
            }
        });
        TanggalAwal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalAwalActionPerformed(evt);
            }
        });
        TanggalAwal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalAwalKeyPressed(evt);
            }
        });
        FormInput.add(TanggalAwal);
        TanggalAwal.setBounds(340, 40, 90, 23);

        jLabel13.setText("Dari Tanggal :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(250, 40, 80, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "IGD", "Poli", "Ranap" }));
        Status.setEnabled(false);
        Status.setName("Status"); // NOI18N
        Status.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                StatusItemStateChanged(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(720, 10, 110, 23);

        jLabel20.setText("Dokter :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(25, 70, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(100, 70, 140, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(243, 70, 550, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("ALt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        BtnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(800, 70, 28, 23);

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
        internalFrame1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void NoSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoSuratKeyPressed
        Valid.pindah(evt, TCari, TanggalAwal);
}//GEN-LAST:event_NoSuratKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            isRawat();
            isPsien();
        } else {
            Valid.pindah(evt, TCari, LamaSakit);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt, TCari, BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
//    // Validasi input
//    if (NoSurat.getText().trim().equals("")) {
//        Valid.textKosong(NoSurat, "No.Surat Sakit");
    if (LamaSakit.getText().trim().equals("")) {
        Valid.textKosong(LamaSakit, "lama sakit");
    } else if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
        Valid.textKosong(TNoRw, "pasien");
    } else if (KdDokter.getText().trim().equals("")) {
        Valid.textKosong(KdDokter, "Dokter");
    } else {
//        // Query untuk mengecek apakah data sudah ada berdasarkan no_rawat, tanggalawal, dan tanggalakhir
//        String query = "SELECT COUNT(*) FROM suratsakit WHERE no_rawat = ? AND tanggalawal = ? AND tanggalakhir = ?";
//        
//        // Menggunakan Sequel.cariInteger untuk mendapatkan jumlah data yang ada
//        int count = Sequel.cariInteger(query, TNoRw.getText(), 
//                                       Valid.SetTgl(TanggalAwal.getSelectedItem() + ""),
//                                       Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""));
//        
//        // Jika data sudah ada
//        if (count > 0) {
//            JOptionPane.showMessageDialog(null, "Maaf, data sudah ada, silahkan filter tanggal dibawah.");
//        } else {
//            // Jika data belum ada, lakukan penyimpanan
//            if (Sequel.menyimpantf("suratsakit", "?,?,?,?,?,?", "No.Surat Sakit", 6, new String[] {
//                NoSurat.getText(),
//                TNoRw.getText(),
//                Valid.SetTgl(TanggalAwal.getSelectedItem() + ""),
//                Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""),
//                LamaSakit.getText(),
//                KdDokter.getText()
//            }) == true) {
//                tampil();  // Tampilkan data
//                emptTeks();  // Kosongkan form
//            }
//        }
//    }
//
//        if (NoSurat.getText().trim().equals("")) {
//            Valid.textKosong(NoSurat, "No.Surat Sakit");
//        } else if (LamaSakit.getText().trim().equals("")) {
//            Valid.textKosong(LamaSakit, "lama sakit");
//        } else if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
//            Valid.textKosong(TNoRw, "pasien");
//        } else if (KdDokter.getText().trim().equals("")) {
//            Valid.textKosong(KdDokter, "Dokter");
//        } else {
//            if (Sequel.menyimpantf("suratsakit", "?,?,?,?,?,?", "No.Surat Sakit", 6, new String[]{
//                NoSurat.getText(), TNoRw.getText(), Valid.SetTgl(TanggalAwal.getSelectedItem() + ""), Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""), LamaSakit.getText(), KdDokter.getText()
//            }) == true) {
//                tampil();
//                emptTeks();
//            }
//        }

    try {
            // Mulai transaksi
            koneksi.setAutoCommit(false);

            // Panggil metode untuk generate nomor surat
            nomorSurat();

            int maxAttempts = 10; // Batas maksimum pengulangan
            int attempts = 0;

            while (Integer.parseInt(Sequel.cariIsi("SELECT COUNT(*) FROM suratsakit WHERE no_surat = ?", nomorSuratGenerated)) > 0) {
                nomorSurat(); // Regenerate nomor surat
                attempts++;

                if (attempts >= maxAttempts) {
                    JOptionPane.showMessageDialog(null, "Gagal menghasilkan nomor surat unik setelah 10 kali percobaan.");
                    return; // Keluar dari metode jika tidak bisa menghasilkan nomor unik
                }
            }

            // Simpan ke database
            if (Sequel.menyimpantf("suratsakit", "?,?,?,?,?,?", "No.Surat", 6, new String[]{                
                nomorSuratGenerated, TNoRw.getText(), Valid.SetTgl(TanggalAwal.getSelectedItem() + ""), Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""), LamaSakit.getText(), KdDokter.getText()
                })) {
                tabMode.addRow(new String[]{
                    nomorSuratGenerated, TNoRw.getText(), TNoRM.getText(), TPasien.getText(),
                    Valid.SetTgl(TanggalAwal.getSelectedItem() + ""), Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""), LamaSakit.getText(), KdDokter.getText()
                });
                LCount.setText("" + tabMode.getRowCount());
                emptTeks();
            } else {
                throw new Exception("Gagal menyimpan data!");
            }

            // Commit transaksi
            koneksi.commit();
        } catch (Exception e) {
            try {
                // Rollback jika terjadi error
                koneksi.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rollback: " + ex);
            }
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        } finally {
            try {
                koneksi.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Error set auto commit: " + ex);
            }
        }
    }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, LamaSakit, BtnBatal);
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
        Valid.hapusTable(tabMode, NoSurat, "suratsakit", "no_surat");
        tampil();
        emptTeks();
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (NoSurat.getText().trim().equals("")) {
            Valid.textKosong(NoSurat, "No.Surat Sakit");
        } else if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "pasien");
        } else {
            if (tbObat.getSelectedRow() != -1) {
                if (Sequel.mengedittf("suratsakit", "no_surat=?", "no_surat=?,no_rawat=?,tanggalawal=?,tanggalakhir=?,lamasakit=?,kd_dokter=?", 7, new String[]{
                    NoSurat.getText(), TNoRw.getText(), Valid.SetTgl(TanggalAwal.getSelectedItem() + ""), Valid.SetTgl(TanggalAkhir.getSelectedItem() + ""), LamaSakit.getText(), KdDokter.getText(), tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
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
            tgl = " suratsakit.tanggalawal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' ";
            if (TCari.getText().trim().equals("")) {
                Valid.MyReportqry("rptDataSuratSakit.jasper", "report", "::[ Data Surat Sakit Pasien ]::",
                        "select suratsakit.no_surat,suratsakit.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                        + "suratsakit.tanggalawal,suratsakit.tanggalakhir,suratsakit.lamasakit "
                        + "from suratsakit inner join reg_periksa on suratsakit.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "where " + tgl + "order by suratsakit.no_surat", param);
            } else {
                Valid.MyReportqry("rptDataSuratSakit.jasper", "report", "::[ Data Surat Sakit Pasien ]::",
                        "select suratsakit.no_surat,suratsakit.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                        + "suratsakit.tanggalawal,suratsakit.tanggalakhir,suratsakit.lamasakit "
                        + "from suratsakit inner join reg_periksa on suratsakit.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "where " + tgl + "and no_surat like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.no_rawat like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and reg_periksa.no_rkm_medis like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.tanggalawal like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.tanggalakhir like '%" + TCari.getText().trim() + "%' "
                        + "order by suratsakit.no_surat", param);
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
            tampil();
            TCari.setText("");
        } else {
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed


    private void TanggalAkhirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalAkhirKeyPressed
        Valid.pindah(evt, TanggalAwal, LamaSakit);
}//GEN-LAST:event_TanggalAkhirKeyPressed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed

}//GEN-LAST:event_TNoRMKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void LamaSakitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LamaSakitKeyPressed
        Valid.pindah(evt, TanggalAkhir, BtnSimpan);
    }//GEN-LAST:event_LamaSakitKeyPressed

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

    private void LamaSakitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LamaSakitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LamaSakitActionPerformed

    private void TanggalAkhirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalAkhirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalAkhirActionPerformed

    private void TanggalAwalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalAwalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalAwalActionPerformed

    private void TanggalAwalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalAwalKeyPressed
        Valid.pindah(evt, NoSurat, TanggalAkhir);
    }//GEN-LAST:event_TanggalAwalKeyPressed

    private void MnCetakSuratSakitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratSakitActionPerformed
        if (TPasien.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu pasien...!!!");
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String, Object> param = new HashMap<>();
            param.put("hari", LamaSakit.getText());
            param.put("TanggalAwal", TanggalAwal.getSelectedItem().toString());
            param.put("TanggalAkhir", TanggalAkhir.getSelectedItem().toString());
            param.put("nosakit", NoSurat.getText());
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
            String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
            param.put("logo2", logoPath);
            param.put("namadokter", Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", kodedokter));
            param.put("dokter",Sequel.cariIsi("select nm_dokter from dokter inner join suratsakit on suratsakit.kd_dokter=dokter.kd_dokter where suratsakit.no_rawat=?",TNoRw.getText()));
//          kodedokter=Sequel.cariIsi("select reg_periksa.kd_dokter from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText());  
            kodedokter = Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.no_rawat=? LIMIT 1", TNoRw.getText());
            namadokter=Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",kodedokter);
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kodedokter);
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+namadokter+"\nID "+(finger.equals("")?kodedokter:finger)+"\n"+Sequel.cariIsi("select DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y') from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));  
            param.put("penyakit", Sequel.cariIsi("select concat(diagnosa_pasien.kd_penyakit,' ',penyakit.nm_penyakit) from diagnosa_pasien inner join reg_periksa inner join penyakit "
                    + "on diagnosa_pasien.no_rawat=reg_periksa.no_rawat and diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                    + "where diagnosa_pasien.no_rawat=? and diagnosa_pasien.prioritas='1'", TNoRw.getText()));
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqry("rptSuratSakit5.jasper", "report", "::[ Surat Sakit ]::",
                    "select pasien.no_rkm_medis, DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y')as tgl_registrasi,perusahaan_pasien.nama_perusahaan,reg_periksa.no_rawat,IF(dk.kd_dokter IS NULL, dokter.nm_dokter, dk.nm_dokter) As nm_dokter,pasien.keluarga,pasien.namakeluarga,pasien.tgl_lahir,pasien.jk,"
                    + " pasien.nm_pasien,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,pasien.pekerjaan,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat"
                    + " from suratsakit AS sk  inner join reg_periksa inner join pasien inner join dokter inner join kelurahan inner join perusahaan_pasien inner join kecamatan inner join kabupaten"
                    + " on sk.no_rawat = reg_periksa.no_rawat AND reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_dokter=dokter.kd_dokter and pasien.kd_kel=kelurahan.kd_kel "
                    + "and pasien.perusahaan_pasien=perusahaan_pasien.kode_perusahaan and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab "
                    + " left join dokter as dk ON sk.kd_dokter = dk.kd_dokter "
                    + " where sk.no_surat ='" + NoSurat.getText() + "' ", param);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnCetakSuratSakitActionPerformed

    private void MnCetakSuratSakit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratSakit1ActionPerformed
        if (TPasien.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan anda pilih dulu pasien...!!!");
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String, Object> param = new HashMap<>();
            param.put("hari", LamaSakit.getText());
            param.put("TanggalAwal", TanggalAwal.getSelectedItem().toString());
            param.put("TanggalAkhir", TanggalAkhir.getSelectedItem().toString());
            param.put("nosakit", NoSurat.getText());
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            kodedokter = Sequel.cariIsi("SELECT IF(sk.kd_dokter IS NULL, rp.kd_dokter, sk.kd_dokter) AS kd_dokter FROM suratsakit sk LEFT JOIN reg_periksa rp ON sk.no_rawat = rp.no_rawat WHERE sk.no_surat = ?", NoSurat.getText());
//                kodedokter=Sequel.cariIsi("select reg_periksa.kd_dokter from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText());
            namadokter = Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", kodedokter);
            finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kodedokter);
            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + namadokter + "\nID " + (finger.equals("") ? kodedokter : finger) + "\n" + Sequel.cariIsi("select DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y') from reg_periksa where reg_periksa.no_rawat=?", TNoRw.getText()));
            Valid.MyReportqry("rptSuratSakit.jasper", "report", "::[ Surat Sakit ]::",
                    " select pasien.no_rkm_medis,reg_periksa.no_rawat,IF(dk.kd_dokter IS NULL, dokter.nm_dokter, dk.nm_dokter) As nm_dokter,pasien.keluarga,pasien.namakeluarga,pasien.tgl_lahir,pasien.jk,DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y')as tgl_registrasi,"
                    + " pasien.nm_pasien,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,pasien.pekerjaan,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat "
                    + " from suratsakit AS sk inner join reg_periksa inner join pasien inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "
                    + " on sk.no_rawat = reg_periksa.no_rawat AND reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_dokter=dokter.kd_dokter and pasien.kd_kel=kelurahan.kd_kel "
                    + " and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab "
                    + " left join dokter as dk ON sk.kd_dokter = dk.kd_dokter "
                    + " where sk.no_surat='" + NoSurat.getText() + "' ", param);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnCetakSuratSakit1ActionPerformed

    private void TanggalAwalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalAwalItemStateChanged

        Tanggal();
        String bln_angka = "";
        String bln_romawi = "";
        Object selectedItem = TanggalAwal.getSelectedItem();

        if (selectedItem != null) {
            // Convert selected item to string and extract month
            String selectedDate = selectedItem.toString();

            if (selectedDate.length() >= 5) {
                bln_angka = selectedDate.substring(3, 5);

                // Map month number to Roman numeral
                String[] romawi = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
                int monthIndex = Integer.parseInt(bln_angka) - 1; // Adjust for zero-based index

                if (monthIndex >= 0 && monthIndex < romawi.length) {
                    bln_romawi = romawi[monthIndex];
                }


                // Construct SQL query and call the method
                String baseDate = Valid.SetTgl(selectedDate).substring(0, 7); // Ensure Valid.SetTgl() is properly defined
                if (Status.getSelectedItem().toString().equals("IGD")) {
                    String query = "SELECT IFNULL(MAX(CONVERT(LEFT(no_surat, 3), SIGNED)), 0) FROM suratsakit WHERE tanggalawal LIKE '%" + baseDate + "%'";
                    String newNumberFormat = "/SKS/RSAJ/" + bln_romawi + "/" + Valid1.SetTgl(selectedDate).substring(0, 4);
                    Valid1.autoNomerSuratKhusus1(query, newNumberFormat, 3, NoSurat);
                } else if (Status.getSelectedItem().toString().equals("Poli")) {
                    String query1 = "SELECT IFNULL(MAX(CONVERT(LEFT(no_surat, 3), SIGNED)), 0) FROM suratsakit WHERE tanggalawal LIKE '%" + baseDate + "%'";
                    String newNumberFormat1 = "/SKS/RSAJ/" + bln_romawi + "/" + Valid.SetTgl(selectedDate).substring(0, 4);
                    Valid1.autoNomerSuratKhusus1(query1, newNumberFormat1, 3, NoSurat);
                } else if (Status.getSelectedItem().toString().equals("Ranap")){
                    String query2 = "SELECT IFNULL(MAX(CONVERT(LEFT(no_surat, 3), SIGNED)), 0) FROM suratsakit WHERE tanggalawal LIKE '%" + baseDate + "%'";
                    String newNumberFormat2 = "/SKS/RSAJ/" + bln_romawi + "/" + Valid.SetTgl(selectedDate).substring(0, 4);
                    Valid1.autoNomerSuratKhusus1(query2, newNumberFormat2, 3, NoSurat);
                }else {
                // Handle case where the selected date format is unexpected
                System.out.println("Selected date format is unexpected: " + selectedDate);
            }
            } else {
//                 Handle case where selected item is null
            System.out.println("No item selected.");
            }
        }


    }//GEN-LAST:event_TanggalAwalItemStateChanged

    private void TanggalAkhirItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalAkhirItemStateChanged
        Tanggal(); // TODO add your handling code here:
    }//GEN-LAST:event_TanggalAkhirItemStateChanged

    private void StatusItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_StatusItemStateChanged
        if (Status.getSelectedItem().toString().equals("IGD")) {
            nomorsuratIGD();
        } else if (Status.getSelectedItem().toString().equals("Poli")) {
            nomorsuratPoli();
        } else {
            nomorSurat();
        }// TODO add your handling code here:
    }//GEN-LAST:event_StatusItemStateChanged

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?", NmDokter, KdDokter.getText());
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            TanggalAwal.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            BtnDokterActionPerformed(null);
        }
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
//        Valid.pindah(evt,Detik,cmbSkor1);
    }//GEN-LAST:event_BtnDokterKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratSakit dialog = new SuratSakit(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private widget.TextBox LamaSakit;
    private javax.swing.JMenuItem MnCetakSuratSakit;
    private javax.swing.JMenuItem MnCetakSuratSakit1;
    private widget.TextBox NmDokter;
    private widget.TextBox NoSurat;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalAkhir;
    private widget.Tanggal TanggalAwal;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel13;
    private widget.Label jLabel16;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            tgl = " suratsakit.tanggalawal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' ";
            if (TCari.getText().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "select suratsakit.no_surat,suratsakit.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                        + "suratsakit.tanggalawal,suratsakit.tanggalakhir,suratsakit.lamasakit,suratsakit.kd_dokter,dokter.nm_dokter "
                        + "from suratsakit inner join reg_periksa on suratsakit.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join dokter on suratsakit.kd_dokter=dokter.kd_dokter "
                        + "where " + tgl + "order by suratsakit.no_surat");
            } else {
                ps = koneksi.prepareStatement(
                        "select suratsakit.no_surat,suratsakit.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                        + "suratsakit.tanggalawal,suratsakit.tanggalakhir,suratsakit.lamasakit,suratsakit.kd_dokter,dokter.nm_dokter "
                        + "from suratsakit inner join reg_periksa on suratsakit.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join dokter on suratsakit.kd_dokter=dokter.kd_dokter "
                        + "where " + tgl + "and no_surat like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.no_rawat like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and reg_periksa.no_rkm_medis like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.tanggalawal like '%" + TCari.getText().trim() + "%' or "
                        + tgl + "and suratsakit.tanggalakhir like '%" + TCari.getText().trim() + "%' "
                        + "order by suratsakit.no_surat");
            }

            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new String[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getString(7), rs.getString("kd_dokter"), rs.getString("nm_dokter")
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
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    public void emptTeks() {
        TNoRw.setText("");
        TNoRM.setText("");
        TPasien.setText("");
        NoSurat.setText("");
//        LamaSakit.setText("");
        LamaSakit.setText("1 (Satu)");
        TanggalAwal.setDate(new Date());
        TanggalAkhir.setDate(new Date());
        if (Status.getSelectedItem().toString().equals("IGD")) {
            nomorsuratIGD();
        } else if (Status.getSelectedItem().toString().equals("Poli")) {
            nomorsuratPoli();
        } else {
            nomorSurat();
        }
        NoSurat.requestFocus();
    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
            NoSurat.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
            Valid.SetTgl(TanggalAwal, tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
            Valid.SetTgl(TanggalAkhir, tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            LamaSakit.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
            NmDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());

        }
    }

    private void isRawat() {
        Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='" + TNoRw.getText() + "' ", TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", TPasien);
    }

    public void setNoRm(String norwt, Date tgl1, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari1.setDate(tgl1);
        DTPCari2.setDate(tgl2);
        isRawat();
        isPsien();
        ChkInput.setSelected(true);
        isForm();
    }

    public void setNoRm2(String norwt, String status) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
//        DTPCari1.setDate(tgl1);
//        DTPCari2.setDate(tgl2);
        isRawat();
        isPsien();
        ChkInput.setSelected(true);
        isForm();
        Status.setSelectedItem(status);
        if (Status.getSelectedItem().toString().equals("IGD")) {

            nomorsuratIGD();
        } else if (Status.getSelectedItem().toString().equals("Poli")) {
            nomorsuratPoli();
        } else {
            nomorSurat();
        }
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 125));
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
        BtnSimpan.setEnabled(akses.getsurat_sakit());
        BtnHapus.setEnabled(akses.getsurat_sakit());
        BtnEdit.setEnabled(akses.getsurat_sakit());
        if (akses.getjml2() >= 1) {
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter, KdDokter.getText());
            if (NmDokter.getText().equals("")) {
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan Dokter...!!");
            }
        }
    }

    
    
    private void Tanggal() {
    if (TanggalAkhir.getSelectedItem() != null && TanggalAwal.getSelectedItem() != null) {
        try {
            // Format tanggal yang digunakan
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            // Konversi string tanggal dari ComboBox ke LocalDate
            LocalDate tglAkhir = LocalDate.parse(TanggalAkhir.getSelectedItem().toString(), formatter);
            LocalDate tglAwal = LocalDate.parse(TanggalAwal.getSelectedItem().toString(), formatter);

            // Hitung selisih hari
            long dayDifference = ChronoUnit.DAYS.between(tglAwal, tglAkhir) + 1;

            // Konversi angka ke teks
            String angkaDalamHuruf = convertNumberToText((int) dayDifference);

            // Set hasil perhitungan ke Label
            LamaSakit.setText(dayDifference + " ( " + angkaDalamHuruf + " )");
        } catch (Exception e) {
            e.printStackTrace();
            LamaSakit.setText("Error dalam perhitungan tanggal");
        }
    } else {
        // Handle the case where no item is selected
        LamaSakit.setText("Pilih Tanggal Terlebih Dahulu");
    }
}

// Fungsi untuk mengonversi angka ke teks
private String convertNumberToText(int number) {
    String[] angka = {
        "nol", "Satu", "Dua", "Tiga", "Empat", "Lima", 
        "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", 
        "Sebelas"
    };
    if (number < 12) {
        return angka[number];
    } else if (number < 20) {
        return angka[number - 10] + " Belas";
    } else if (number < 100) {
        return angka[number / 10] + " Puluh" + 
               (number % 10 != 0 ? " " + angka[number % 10] : "");
    } else if (number < 200) {
        return "Seratus" + (number % 100 != 0 ? " " + convertNumberToText(number % 100) : "");
    } else if (number < 1000) {
        return angka[number / 100] + " Ratus" + 
               (number % 100 != 0 ? " " + convertNumberToText(number % 100) : "");
    }
    return String.valueOf(number); // Jika lebih dari 999, kembalikan angka sebagai string
}

    private void nomorsuratIGD() {
//        bln_angka = "";
//        bln_romawi = "";
//        bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);
//
//        if (bln_angka.equals("01")) {
//            bln_romawi = "I";
//        } else if (bln_angka.equals("02")) {
//            bln_romawi = "II";
//        } else if (bln_angka.equals("03")) {
//            bln_romawi = "III";
//        } else if (bln_angka.equals("04")) {
//            bln_romawi = "IV";
//        } else if (bln_angka.equals("05")) {
//            bln_romawi = "V";
//        } else if (bln_angka.equals("06")) {
//            bln_romawi = "VI";
//        } else if (bln_angka.equals("07")) {
//            bln_romawi = "VII";
//        } else if (bln_angka.equals("08")) {
//            bln_romawi = "VIII";
//        } else if (bln_angka.equals("09")) {
//            bln_romawi = "IX";
//        } else if (bln_angka.equals("10")) {
//            bln_romawi = "X";
//        } else if (bln_angka.equals("11")) {
//            bln_romawi = "XI";
//        } else if (bln_angka.equals("12")) {
//            bln_romawi = "XII";
//        }
//
//        Valid1.autoNomerSuratKhusus1("select ifnull(MAX(CONVERT(LEFT(no_surat,3),signed)),0) from suratsakit where tanggalawal like '%" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 7) + "%' ", "/SKS/RSAJ/" + bln_romawi + "/" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 4), 3, NoSurat);
//        NoSurat.requestFocus();

    // MODIF MUSTAFA
    
    bln_angka = "";
    bln_romawi = "";

    bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);

    switch (bln_angka) {
        case "01": bln_romawi = "I"; break;
        case "02": bln_romawi = "II"; break;
        case "03": bln_romawi = "III"; break;
        case "04": bln_romawi = "IV"; break;
        case "05": bln_romawi = "V"; break;
        case "06": bln_romawi = "VI"; break;
        case "07": bln_romawi = "VII"; break;
        case "08": bln_romawi = "VIII"; break;
        case "09": bln_romawi = "IX"; break;
        case "10": bln_romawi = "X"; break;
        case "11": bln_romawi = "XI"; break;
        case "12": bln_romawi = "XII"; break;
    }

    // Ambil nomor terakhir dari database
    String lastNumber = Sequel.cariIsi(
        "SELECT MAX(CONVERT(LEFT(no_surat, 3), SIGNED)) FROM suratsakit WHERE " +
        "no_surat LIKE '%/SKS/RSAJ/" + bln_romawi + "/" + TanggalAwal.getSelectedItem().toString().substring(6, 10) + "'"
    );

    int nextNumber = 1; // Default jika belum ada nomor di database
    if (lastNumber != null && !lastNumber.isEmpty()) {
        nextNumber = Integer.parseInt(lastNumber) + 1; // Tambahkan 1 dari nomor terakhir
    }

    // Format nomor surat baru
    nomorSuratGenerated = String.format("%03d", nextNumber) + "/SKS/RSAJ/" + bln_romawi + "/" + 
                          TanggalAwal.getSelectedItem().toString().substring(6, 10);
    
    // END MODIF MUSTAFA
    }

    private void nomorsuratPoli() {
//        bln_angka = "";
//        bln_romawi = "";
//        bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);
//
//        if (bln_angka.equals("01")) {
//            bln_romawi = "I";
//        } else if (bln_angka.equals("02")) {
//            bln_romawi = "II";
//        } else if (bln_angka.equals("03")) {
//            bln_romawi = "III";
//        } else if (bln_angka.equals("04")) {
//            bln_romawi = "IV";
//        } else if (bln_angka.equals("05")) {
//            bln_romawi = "V";
//        } else if (bln_angka.equals("06")) {
//            bln_romawi = "VI";
//        } else if (bln_angka.equals("07")) {
//            bln_romawi = "VII";
//        } else if (bln_angka.equals("08")) {
//            bln_romawi = "VIII";
//        } else if (bln_angka.equals("09")) {
//            bln_romawi = "IX";
//        } else if (bln_angka.equals("10")) {
//            bln_romawi = "X";
//        } else if (bln_angka.equals("11")) {
//            bln_romawi = "XI";
//        } else if (bln_angka.equals("12")) {
//            bln_romawi = "XII";
//        }
//
//        Valid1.autoNomerSuratKhusus1("select ifnull(MAX(CONVERT(LEFT(no_surat,3),signed)),0) from suratsakit where tanggalawal like '%" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 7) + "%'", "/SKS/RSAJ/" + bln_romawi + "/" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 4), 3, NoSurat);
//        NoSurat.requestFocus();

    // MODIF MUSTAFA
    
    bln_angka = "";
    bln_romawi = "";

    bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);

    switch (bln_angka) {
        case "01": bln_romawi = "I"; break;
        case "02": bln_romawi = "II"; break;
        case "03": bln_romawi = "III"; break;
        case "04": bln_romawi = "IV"; break;
        case "05": bln_romawi = "V"; break;
        case "06": bln_romawi = "VI"; break;
        case "07": bln_romawi = "VII"; break;
        case "08": bln_romawi = "VIII"; break;
        case "09": bln_romawi = "IX"; break;
        case "10": bln_romawi = "X"; break;
        case "11": bln_romawi = "XI"; break;
        case "12": bln_romawi = "XII"; break;
    }

    // Ambil nomor terakhir dari database
    String lastNumber = Sequel.cariIsi(
        "SELECT MAX(CONVERT(LEFT(no_surat, 3), SIGNED)) FROM suratsakit WHERE " +
        "no_surat LIKE '%/SKS/RSAJ/" + bln_romawi + "/" + TanggalAwal.getSelectedItem().toString().substring(6, 10) + "'"
    );

    int nextNumber = 1; // Default jika belum ada nomor di database
    if (lastNumber != null && !lastNumber.isEmpty()) {
        nextNumber = Integer.parseInt(lastNumber) + 1; // Tambahkan 1 dari nomor terakhir
    }

    // Format nomor surat baru
    nomorSuratGenerated = String.format("%03d", nextNumber) + "/SKS/RSAJ/" + bln_romawi + "/" + 
                          TanggalAwal.getSelectedItem().toString().substring(6, 10);
    
    // END MODIF MUSTAFA
    }

    private void nomorSurat() {
//        bln_angka = "";
//        bln_romawi = "";
//        bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);
//
//        if (bln_angka.equals("01")) {
//            bln_romawi = "I";
//        } else if (bln_angka.equals("02")) {
//            bln_romawi = "II";
//        } else if (bln_angka.equals("03")) {
//            bln_romawi = "III";
//        } else if (bln_angka.equals("04")) {
//            bln_romawi = "IV";
//        } else if (bln_angka.equals("05")) {
//            bln_romawi = "V";
//        } else if (bln_angka.equals("06")) {
//            bln_romawi = "VI";
//        } else if (bln_angka.equals("07")) {
//            bln_romawi = "VII";
//        } else if (bln_angka.equals("08")) {
//            bln_romawi = "VIII";
//        } else if (bln_angka.equals("09")) {
//            bln_romawi = "IX";
//        } else if (bln_angka.equals("10")) {
//            bln_romawi = "X";
//        } else if (bln_angka.equals("11")) {
//            bln_romawi = "XI";
//        } else if (bln_angka.equals("12")) {
//            bln_romawi = "XII";
//        }
//
//        Valid1.autoNomerSuratKhusus1("select ifnull(MAX(CONVERT(LEFT(no_surat,3),signed)),0) from suratsakit where tanggalawal like '%" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 7) + "%'", "/SKS/RSAJ/" + bln_romawi + "/" + Valid.SetTgl(TanggalAwal.getSelectedItem() + "").substring(0, 4), 3, NoSurat);
//        NoSurat.requestFocus();
        
    // MODIF MUSTAFA
    
    bln_angka = "";
    bln_romawi = "";

    bln_angka = TanggalAwal.getSelectedItem().toString().substring(3, 5);

    switch (bln_angka) {
        case "01": bln_romawi = "I"; break;
        case "02": bln_romawi = "II"; break;
        case "03": bln_romawi = "III"; break;
        case "04": bln_romawi = "IV"; break;
        case "05": bln_romawi = "V"; break;
        case "06": bln_romawi = "VI"; break;
        case "07": bln_romawi = "VII"; break;
        case "08": bln_romawi = "VIII"; break;
        case "09": bln_romawi = "IX"; break;
        case "10": bln_romawi = "X"; break;
        case "11": bln_romawi = "XI"; break;
        case "12": bln_romawi = "XII"; break;
    }

    // Ambil nomor terakhir dari database
    String lastNumber = Sequel.cariIsi(
        "SELECT MAX(CONVERT(LEFT(no_surat, 3), SIGNED)) FROM suratsakit WHERE " +
        "no_surat LIKE '%/SKS/RSAJ/" + bln_romawi + "/" + TanggalAwal.getSelectedItem().toString().substring(6, 10) + "'"
    );

    int nextNumber = 1; // Default jika belum ada nomor di database
    if (lastNumber != null && !lastNumber.isEmpty()) {
        nextNumber = Integer.parseInt(lastNumber) + 1; // Tambahkan 1 dari nomor terakhir
    }

    // Format nomor surat baru
    nomorSuratGenerated = String.format("%03d", nextNumber) + "/SKS/RSAJ/" + bln_romawi + "/" + 
                          TanggalAwal.getSelectedItem().toString().substring(6, 10);
    
    // END MODIF MUSTAFA
    }

}
