/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgJnsPerawatan.java
 *
 * Created on May 22, 2010, 11:58:21 PM
 */

package simrskhanza;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import raven.toast.Notifications;

/**
 *
 * @author dosen
 */
public final class KeluhanIT extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;    
//    private JComboBox<String> comboKategori;
//    private JLabel labelketkategori;
//    private InventarisKoleksi inventaris=new InventarisKoleksi(null,false);
//    private InventarisRuang ruang=new InventarisRuang(null,false); 

    /** Creates new form DlgJnsPerawatan
     * @param parent
     * @param modal */
    public KeluhanIT(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        isiDataLogin(); 

        this.setLocation(8,1);
        setSize(628,674);       
        
        tabMode = new DefaultTableModel(null, new Object[]{
            "ID Keluhan", "Tgl Permintaan", "Unit", "Petugas", "Kategori", 
            "Jenis Keluhan", "Urgensi", "Detail Keluhan", "Status Keluhan", 
            "Tim IT", "Detail Penyelesaian", "Tgl Penyelesaian", "Waktu Penyelesaian","On Proses"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbKeluhan.setModel(tabMode);

        // Set ukuran kolom
        tbKeluhan.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbKeluhan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 14; i++) {
            TableColumn column = tbKeluhan.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30); // ID
            } else if (i == 1) {
                column.setPreferredWidth(130); // Tgl Permintaan
            } else if (i == 2) {
                column.setPreferredWidth(150); // Unit
            } else if (i == 3) {
                column.setPreferredWidth(170); // Petugas
            } else if (i == 4) {
                column.setPreferredWidth(100); // Kategori
            } else if (i == 5) {
                column.setPreferredWidth(100); // Jenis Keluhan
            } else if (i == 6) {
                column.setPreferredWidth(50); // Urgensi
            } else if (i == 7) {
                column.setPreferredWidth(250); // Detail Keluhan
            } else if (i == 8) {
                column.setPreferredWidth(80); // Status
            } else if (i == 9) {
                column.setPreferredWidth(70); // Tim IT
            } else if (i == 10) {
                column.setPreferredWidth(250); // Detail Penyelesaian
            } else if (i == 11) {
                column.setPreferredWidth(130); // Tgl Penyelesaian
            } else if (i == 12) {
                column.setPreferredWidth(100); // Waktu Penyelesaian
            } else if (i == 13) {
                column.setPreferredWidth(100); // Waktu Penyelesaian
            }
        }
        tbKeluhan.setDefaultRenderer(Object.class, new WarnaTable());
        
        ChkInput.setSelected(false);
        isForm(); 
        
        // Event Listener untuk comboKategori
        comboKategori.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLabelKategori();
            }
        });        
        
        // Event Listener untuk comboKategori
        combourgensi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLabelUrgensi();
            }
        });  
    
//    String nikLogin = akses.getkode();
//    String namaPetugasLogin = Sequel.cariIsi("SELECT nama FROM pegawai WHERE nik=?", nikLogin);
//    String unitLogin = Sequel.cariIsi(
//        "SELECT departemen.nama FROM departemen INNER JOIN pegawai ON departemen.dep_id = pegawai.departemen WHERE pegawai.nik=?", nikLogin
//    );
//    petugas_pemohon.setText(namaPetugasLogin);    
//    unit.setText(unitLogin); 
  

       
    // Misalnya ada variabel userRole yang menyimpan peran pengguna yang login
       String nikPengguna = akses.getkode(); // Pastikan ini mengambil NIK login aktif

    // Daftar NIK yang juga diberi akses seperti admin
    List<String> nikDiizinkan = Arrays.asList("19951137", "20101730", "20111816", "20091716", "K231974");

    if (akses.getadmin() || nikDiizinkan.contains(nikPengguna)) {
        Btndibatalkan.setVisible(true);
        BtnOnProses.setVisible(true);
        BtnSelesai.setVisible(true);    
        timIT.setEnabled(true);
        tgl_selesai.setEnabled(true);
        detailpenyelesaian.setEnabled(true);
        BtnEdit.setEnabled(true);
        BtnHapus.setVisible(true);
        labelID.setVisible(true);
        id_keluhan.setVisible(true);
        label11.setVisible(true);
        unit.setVisible(true);
        label13.setVisible(true);
        petugas_pemohon.setVisible(true);
        label14.setVisible(true);
        timIT.setVisible(true);
        labeltglselesai.setVisible(true);
        tgl_selesai.setVisible(true);
        labeltglselesai1.setVisible(true);
        waktu_penyelesaian.setVisible(true);
        labeldetailpenyelesaian.setVisible(true);
        detailpenyelesaian.setVisible(true);
        jScrollPane2.setVisible(true);
        BtnUpdateWaktu.setVisible(true);
    //  BtnMonev.setVisible(true);
        tgl_onproses.setVisible(true);
    } else {
        Btndibatalkan.setVisible(false);
        BtnOnProses.setVisible(false);
        BtnSelesai.setVisible(false);
        timIT.setEnabled(false);
        tgl_selesai.setEnabled(false);
        detailpenyelesaian.setEnabled(false);
        BtnEdit.setEnabled(false);
        BtnHapus.setVisible(false);
        labelID.setVisible(false);
        id_keluhan.setVisible(false);
        label11.setVisible(false);
        unit.setVisible(false);
        label13.setVisible(false);
        petugas_pemohon.setVisible(false);
        label14.setVisible(false);
        timIT.setVisible(false);
        labeltglselesai.setVisible(false);
        tgl_selesai.setVisible(false);
        labeltglselesai1.setVisible(false);
        waktu_penyelesaian.setVisible(false);
        labeldetailpenyelesaian.setVisible(false);
        detailpenyelesaian.setVisible(false);
        jScrollPane2.setVisible(false);
        BtnUpdateWaktu.setVisible(false);
        BtnHapus.setVisible(false);
        BtnEdit.setVisible(false);
    //  BtnMonev.setVisible(false);
        tgl_onproses.setVisible(false);
    }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbKeluhan = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBaru = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnMonev = new widget.Button();
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
        Btndibatalkan = new widget.Button();
        BtnOnProses = new widget.Button();
        BtnSelesai = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        labelID = new widget.Label();
        id_keluhan = new widget.TextBox();
        labeltglpermintaan = new widget.Label();
        tgl_permintaan = new widget.Tanggal();
        labelkategori1 = new widget.Label();
        label11 = new widget.Label();
        unit = new widget.TextBox();
        label13 = new widget.Label();
        comboKategori = new widget.ComboBox();
        combojenis = new widget.ComboBox();
        labeljenis = new widget.Label();
        jScrollPane1 = new javax.swing.JScrollPane();
        detailkeluhan = new widget.TextArea();
        labeldetailkeluhan = new widget.Label();
        labelketkategori = new widget.Label();
        labelurgensi = new widget.Label();
        combourgensi = new widget.ComboBox();
        statuskeluhan = new widget.ComboBox();
        labelstatus = new widget.Label();
        tgl_selesai = new widget.Tanggal();
        labeltglselesai = new widget.Label();
        labeldetailpenyelesaian = new widget.Label();
        jScrollPane2 = new javax.swing.JScrollPane();
        detailpenyelesaian = new widget.TextArea();
        petugas_pemohon = new widget.TextBox();
        timIT = new widget.TextBox();
        waktu_penyelesaian = new widget.TextBox();
        labelketurgensi = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        label14 = new widget.Label();
        labeltglselesai1 = new widget.Label();
        tgl_onproses = new widget.Tanggal();
        BtnUpdateWaktu = new widget.Button();
        ChkInput = new widget.CekBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        jPanel1.setName("jPanel1"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "   Form Dukungan Teknis dan Pengembangan Aplikasi   ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setAlignmentX(1.0F);
        internalFrame1.setAlignmentY(1.0F);
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(10000, 836));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbKeluhan.setAutoCreateRowSorter(true);
        tbKeluhan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbKeluhan.setName("tbKeluhan"); // NOI18N
        tbKeluhan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbKeluhanMouseClicked(evt);
            }
        });
        tbKeluhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbKeluhanKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbKeluhanKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbKeluhan);

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

        BtnBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBaru.setMnemonic('B');
        BtnBaru.setText("Buat Pengajuan");
        BtnBaru.setToolTipText("Alt+B");
        BtnBaru.setName("BtnBaru"); // NOI18N
        BtnBaru.setPreferredSize(new java.awt.Dimension(135, 30));
        BtnBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBaruActionPerformed(evt);
            }
        });
        BtnBaru.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBaruKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBaru);

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

        BtnMonev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/calendar (4).png"))); // NOI18N
        BtnMonev.setMnemonic('T');
        BtnMonev.setText("Monitoring Keluhan ");
        BtnMonev.setToolTipText("Alt+T");
        BtnMonev.setName("BtnMonev"); // NOI18N
        BtnMonev.setPreferredSize(new java.awt.Dimension(150, 30));
        BtnMonev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnMonevActionPerformed(evt);
            }
        });
        BtnMonev.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnMonevKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnMonev);

        jLabel7.setText("Record  :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(75, 23));
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
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 8);
        flowLayout1.setAlignOnBaseline(true);
        panelGlass9.setLayout(flowLayout1);

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(54, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-05-2025" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-05-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel6);

        TCari.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(150, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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

        Btndibatalkan.setForeground(new java.awt.Color(0, 0, 0));
        Btndibatalkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cancelled.png"))); // NOI18N
        Btndibatalkan.setMnemonic('M');
        Btndibatalkan.setText("Dibatalkan");
        Btndibatalkan.setToolTipText("Alt+M");
        Btndibatalkan.setName("Btndibatalkan"); // NOI18N
        Btndibatalkan.setPreferredSize(new java.awt.Dimension(95, 30));
        Btndibatalkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtndibatalkanActionPerformed(evt);
            }
        });
        Btndibatalkan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtndibatalkanKeyPressed(evt);
            }
        });
        panelGlass9.add(Btndibatalkan);

        BtnOnProses.setForeground(new java.awt.Color(0, 0, 0));
        BtnOnProses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/sand-clock.png"))); // NOI18N
        BtnOnProses.setMnemonic('M');
        BtnOnProses.setText("On Proses");
        BtnOnProses.setToolTipText("Alt+M");
        BtnOnProses.setName("BtnOnProses"); // NOI18N
        BtnOnProses.setPreferredSize(new java.awt.Dimension(95, 30));
        BtnOnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnOnProsesActionPerformed(evt);
            }
        });
        BtnOnProses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnOnProsesKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnOnProses);

        BtnSelesai.setForeground(new java.awt.Color(0, 0, 0));
        BtnSelesai.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/check (1).png"))); // NOI18N
        BtnSelesai.setMnemonic('M');
        BtnSelesai.setText("Selesai");
        BtnSelesai.setToolTipText("Alt+M");
        BtnSelesai.setName("BtnSelesai"); // NOI18N
        BtnSelesai.setPreferredSize(new java.awt.Dimension(80, 30));
        BtnSelesai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSelesaiActionPerformed(evt);
            }
        });
        BtnSelesai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSelesaiKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnSelesai);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(1000, 350));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1000, 300));
        FormInput.setLayout(null);

        labelID.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelID.setText("No. ID");
        labelID.setName("labelID"); // NOI18N
        FormInput.add(labelID);
        labelID.setBounds(680, 20, 110, 26);

        id_keluhan.setEditable(false);
        id_keluhan.setName("id_keluhan"); // NOI18N
        id_keluhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                id_keluhanKeyPressed(evt);
            }
        });
        FormInput.add(id_keluhan);
        id_keluhan.setBounds(770, 20, 140, 26);

        labeltglpermintaan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeltglpermintaan.setText("Tanggal");
        labeltglpermintaan.setName("labeltglpermintaan"); // NOI18N
        FormInput.add(labeltglpermintaan);
        labeltglpermintaan.setBounds(20, 53, 100, 26);

        tgl_permintaan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-05-2025 17:22:30" }));
        tgl_permintaan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl_permintaan.setName("tgl_permintaan"); // NOI18N
        tgl_permintaan.setOpaque(false);
        tgl_permintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tgl_permintaanKeyPressed(evt);
            }
        });
        FormInput.add(tgl_permintaan);
        tgl_permintaan.setBounds(130, 53, 140, 26);

        labelkategori1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelkategori1.setText("Kategori ");
        labelkategori1.setName("labelkategori1"); // NOI18N
        FormInput.add(labelkategori1);
        labelkategori1.setBounds(20, 87, 110, 26);

        label11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label11.setText("Unit");
        label11.setName("label11"); // NOI18N
        FormInput.add(label11);
        label11.setBounds(680, 53, 70, 26);

        unit.setEditable(false);
        unit.setName("unit"); // NOI18N
        FormInput.add(unit);
        unit.setBounds(770, 53, 530, 26);

        label13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label13.setText("Pemohon");
        label13.setName("label13"); // NOI18N
        FormInput.add(label13);
        label13.setBounds(680, 87, 70, 26);

        comboKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Software", "Hardware", "Jaringan" }));
        comboKategori.setName("comboKategori"); // NOI18N
        comboKategori.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboKategoriKeyPressed(evt);
            }
        });
        FormInput.add(comboKategori);
        comboKategori.setBounds(130, 87, 140, 26);

        combojenis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Perbaikan", "Pemeliharaan", "Penggantian", "Pengembangan" }));
        combojenis.setName("combojenis"); // NOI18N
        combojenis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                combojenisKeyPressed(evt);
            }
        });
        FormInput.add(combojenis);
        combojenis.setBounds(130, 120, 140, 26);

        labeljenis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeljenis.setText("Jenis");
        labeljenis.setName("labeljenis"); // NOI18N
        FormInput.add(labeljenis);
        labeljenis.setBounds(20, 120, 100, 26);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        detailkeluhan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        detailkeluhan.setColumns(20);
        detailkeluhan.setRows(5);
        detailkeluhan.setName("detailkeluhan"); // NOI18N
        jScrollPane1.setViewportView(detailkeluhan);

        FormInput.add(jScrollPane1);
        jScrollPane1.setBounds(20, 210, 620, 100);

        labeldetailkeluhan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeldetailkeluhan.setText("Detail Keluhan");
        labeldetailkeluhan.setName("labeldetailkeluhan"); // NOI18N
        FormInput.add(labeldetailkeluhan);
        labeldetailkeluhan.setBounds(20, 185, 100, 23);

        labelketkategori.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelketkategori.setText("( Permasalahan terkait Aplikasi, SIMRS, Sistem Operasi, Virus, dll )");
        labelketkategori.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        labelketkategori.setName("labelketkategori"); // NOI18N
        FormInput.add(labelketkategori);
        labelketkategori.setBounds(280, 87, 380, 26);

        labelurgensi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelurgensi.setText("Urgensi");
        labelurgensi.setName("labelurgensi"); // NOI18N
        FormInput.add(labelurgensi);
        labelurgensi.setBounds(20, 153, 110, 26);

        combourgensi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Low", "Medium", "High" }));
        combourgensi.setName("combourgensi"); // NOI18N
        combourgensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                combourgensiKeyPressed(evt);
            }
        });
        FormInput.add(combourgensi);
        combourgensi.setBounds(130, 153, 140, 26);

        statuskeluhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Menunggu", "On Progress", "Selesai", "Dibatalkan" }));
        statuskeluhan.setEnabled(false);
        statuskeluhan.setName("statuskeluhan"); // NOI18N
        statuskeluhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                statuskeluhanKeyPressed(evt);
            }
        });
        FormInput.add(statuskeluhan);
        statuskeluhan.setBounds(130, 20, 140, 26);

        labelstatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelstatus.setText("Status");
        labelstatus.setName("labelstatus"); // NOI18N
        FormInput.add(labelstatus);
        labelstatus.setBounds(20, 20, 100, 26);

        tgl_selesai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-05-2025 17:22:31" }));
        tgl_selesai.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl_selesai.setName("tgl_selesai"); // NOI18N
        tgl_selesai.setOpaque(false);
        tgl_selesai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tgl_selesaiKeyPressed(evt);
            }
        });
        FormInput.add(tgl_selesai);
        tgl_selesai.setBounds(770, 153, 140, 26);

        labeltglselesai.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeltglselesai.setText("Tgl. Selesai");
        labeltglselesai.setName("labeltglselesai"); // NOI18N
        FormInput.add(labeltglselesai);
        labeltglselesai.setBounds(680, 153, 80, 26);

        labeldetailpenyelesaian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeldetailpenyelesaian.setText("Detail Penyelesaian");
        labeldetailpenyelesaian.setName("labeldetailpenyelesaian"); // NOI18N
        FormInput.add(labeldetailpenyelesaian);
        labeldetailpenyelesaian.setBounds(680, 185, 210, 23);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        detailpenyelesaian.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        detailpenyelesaian.setColumns(20);
        detailpenyelesaian.setRows(5);
        detailpenyelesaian.setName("detailpenyelesaian"); // NOI18N
        jScrollPane2.setViewportView(detailpenyelesaian);

        FormInput.add(jScrollPane2);
        jScrollPane2.setBounds(680, 210, 620, 100);

        petugas_pemohon.setEditable(false);
        petugas_pemohon.setName("petugas_pemohon"); // NOI18N
        FormInput.add(petugas_pemohon);
        petugas_pemohon.setBounds(770, 87, 530, 26);

        timIT.setEditable(false);
        timIT.setName("timIT"); // NOI18N
        FormInput.add(timIT);
        timIT.setBounds(770, 120, 530, 26);

        waktu_penyelesaian.setEditable(false);
        waktu_penyelesaian.setName("waktu_penyelesaian"); // NOI18N
        FormInput.add(waktu_penyelesaian);
        waktu_penyelesaian.setBounds(1190, 153, 110, 26);

        labelketurgensi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelketurgensi.setText("( Tidak Mendesak )");
        labelketurgensi.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        labelketurgensi.setName("labelketurgensi"); // NOI18N
        FormInput.add(labelketurgensi);
        labelketurgensi.setBounds(280, 153, 320, 26);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(660, 20, 1, 310);

        label14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label14.setText("Tim IT");
        label14.setName("label14"); // NOI18N
        FormInput.add(label14);
        label14.setBounds(680, 120, 70, 26);

        labeltglselesai1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labeltglselesai1.setText("Lama Penyelesaian ");
        labeltglselesai1.setName("labeltglselesai1"); // NOI18N
        FormInput.add(labeltglselesai1);
        labeltglselesai1.setBounds(1070, 153, 110, 26);

        tgl_onproses.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-05-2025 12:02:41" }));
        tgl_onproses.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl_onproses.setName("tgl_onproses"); // NOI18N
        tgl_onproses.setOpaque(false);
        tgl_onproses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tgl_onprosesKeyPressed(evt);
            }
        });
        FormInput.add(tgl_onproses);
        tgl_onproses.setBounds(920, 20, 140, 26);

        BtnUpdateWaktu.setForeground(new java.awt.Color(0, 0, 0));
        BtnUpdateWaktu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/check (1).png"))); // NOI18N
        BtnUpdateWaktu.setMnemonic('M');
        BtnUpdateWaktu.setText("Update Waktu");
        BtnUpdateWaktu.setToolTipText("Alt+M");
        BtnUpdateWaktu.setName("BtnUpdateWaktu"); // NOI18N
        BtnUpdateWaktu.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnUpdateWaktu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateWaktuActionPerformed(evt);
            }
        });
        BtnUpdateWaktu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateWaktuKeyPressed(evt);
            }
        });
        FormInput.add(BtnUpdateWaktu);
        BtnUpdateWaktu.setBounds(1310, 150, 120, 30);

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

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane3.setViewportView(jTextPane1);

        internalFrame1.add(jScrollPane3, java.awt.BorderLayout.LINE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
    if (unit.getText().trim().equals("") || petugas_pemohon.getText().trim().equals("") ||
        comboKategori.getSelectedItem() == null || combojenis.getSelectedItem() == null ||
        combourgensi.getSelectedItem() == null || detailkeluhan.getText().trim().equals("")) {

        Notifications.getInstance().show(
            Notifications.Type.INFO,
            Notifications.Location.TOP_CENTER,
            "Terdapat kolom yang belum diisi !"
        );
        return;
    }

    try {
        String tglPermintaanFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tgl_permintaan.getDate());

        ps = koneksi.prepareStatement(
            "INSERT INTO keluhan_it (" +
            "tgl_permintaan, unit, petugas, kategori, jenis_keluhan, urgensi, " +
            "detail_keluhan, status_keluhan, tim_it, detail_penyelesaian" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        ps.setString(1, tglPermintaanFull);
        ps.setString(2, unit.getText());
        ps.setString(3, petugas_pemohon.getText());
        ps.setString(4, comboKategori.getSelectedItem().toString());
        ps.setString(5, combojenis.getSelectedItem().toString());
        ps.setString(6, combourgensi.getSelectedItem().toString());
        ps.setString(7, detailkeluhan.getText());
        ps.setString(8, statuskeluhan.getSelectedItem().toString());
        ps.setString(9, timIT.getText());

        if (!detailpenyelesaian.getText().trim().isEmpty()) {
            ps.setString(10, detailpenyelesaian.getText());
        } else {
            ps.setNull(10, java.sql.Types.VARCHAR);
        }

        ps.executeUpdate();

        Notifications.getInstance().show(
                Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                "Data berhasil disimpan!");
        kirimWhatsAppKeluhan();
        tampil();
        emptTeks();

    } catch (Exception e) {
        Notifications.getInstance().show(
                Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                "Gagal menyimpan data!" + e);
    }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,detailkeluhan,BtnBaru);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBaruActionPerformed
    
    ChkInput.setSelected(true);
    isForm(); // Kalau sudah selected, tetap pastikan formnya aktif
    emptTeks();
}//GEN-LAST:event_BtnBaruActionPerformed

    private void BtnBaruKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBaruKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBaruKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
//        if(akses.getjml2()>=1){
//            if(akses.getkode().equals(nip.getText())){
//                Valid.hapusTable(tabMode,nopermintaan,"permintaan_perbaikan_inventaris","no_permintaan");
//                Sequel.mengedit("inventaris","no_inventaris='"+no_inventaris.getText()+"'","status_barang='Ada'");
//            }else{
//                JOptionPane.showMessageDialog(null,"Harus dihapus oleh yang meminta perbaikan...!!");
//            }
//        }else{
//            Valid.hapusTable(tabMode,nopermintaan,"permintaan_perbaikan_inventaris","no_permintaan");
//            Sequel.mengedit("inventaris","no_inventaris='"+no_inventaris.getText()+"'","status_barang='Ada'");
//        }
//        BtnCariActionPerformed(evt);
//        emptTeks();
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBaru, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed

    // Validasi input
    if (unit.getText().trim().isEmpty() ||
        petugas_pemohon.getText().trim().isEmpty() ||
        comboKategori.getSelectedItem() == null ||
        combojenis.getSelectedItem() == null ||
        combourgensi.getSelectedItem() == null ||
        detailkeluhan.getText().trim().isEmpty()) {
        
        Notifications.getInstance().show(
            Notifications.Type.INFO,
            Notifications.Location.TOP_CENTER,
            "Terdapat kolom yang belum diisi !"
        );
        return;
    }

    if (tgl_permintaan.getDate() == null) {
        Notifications.getInstance().show(
            Notifications.Type.INFO,
            Notifications.Location.TOP_CENTER,
            "Tanggal permintaan belum dipilih !"
        );
        return;
    }

    if (id_keluhan.getText().trim().isEmpty()) {
        Notifications.getInstance().show(
            Notifications.Type.WARNING,
            Notifications.Location.TOP_CENTER,
            "Silakan pilih data yang ingin diedit terlebih dahulu !"
        );
        return;
    }

    try {
        String tglPermintaanFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tgl_permintaan.getDate());

        String sql = "UPDATE keluhan_it SET tgl_permintaan=?, unit=?, petugas=?, kategori=?, jenis_keluhan=?," +
                     "urgensi=?, detail_keluhan=?, status_keluhan=?, tim_it=?, detail_penyelesaian=? WHERE id_keluhan=?";

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, tglPermintaanFull);
            ps.setString(2, unit.getText().trim());
            ps.setString(3, petugas_pemohon.getText().trim());
            ps.setString(4, comboKategori.getSelectedItem().toString());
            ps.setString(5, combojenis.getSelectedItem().toString());
            ps.setString(6, combourgensi.getSelectedItem().toString());
            ps.setString(7, detailkeluhan.getText().trim());
            ps.setString(8, statuskeluhan.getSelectedItem().toString());
            ps.setString(9, timIT.getText().trim());

            if (!detailpenyelesaian.getText().trim().isEmpty()) {
                ps.setString(10, detailpenyelesaian.getText().trim());
            } else {
                ps.setNull(10, java.sql.Types.VARCHAR);
            }

            ps.setString(11, id_keluhan.getText().trim());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_CENTER,
                    "Data berhasil diubah!"
                );
                tampil();
                emptTeks();
            } else {
                Notifications.getInstance().show(
                    Notifications.Type.WARNING,
                    Notifications.Location.TOP_CENTER,
                    "Update gagal: ID tidak ditemukan atau data tidak berubah!"
                );
            }
        }

    } catch (Exception e) {
        Notifications.getInstance().show(
            Notifications.Type.ERROR,
            Notifications.Location.TOP_CENTER,
            "Gagal mengubah data! " + e.getMessage()
        );
    }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnMonev);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
    ChkInput.setSelected(false);
    dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnMonevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnMonevActionPerformed

//    try {
//        StringBuilder htmlContent = new StringBuilder();
//
//        htmlContent.append("<!DOCTYPE html>")
//            .append("<html lang='id'>")
//            .append("<head>")
//            .append("<meta charset='UTF-8'>")
//            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
//            .append("<title>Monitoring Keluhan IT</title>")
//            .append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap' rel='stylesheet'>")
//            .append("<style>")
//            .append("body { font-family: 'Inter', sans-serif; margin: 20px; background-color: #f9fafb; color: #333; font-size: 16px; }")
//            .append(".container { width: 100%; overflow-x: auto; }")
//            .append("table { width: 100%; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-radius: 8px; overflow: hidden; }")
//            .append("th, td { border: 1px solid #e5e7eb; padding: 12px 15px; vertical-align: top; text-align: left; }")
//            .append("th { background-color: #2563eb; color: white; text-align: center; font-weight: 600; font-size: 14px; }")
//            .append("td { font-size: 15px; }")
//            .append(".badge { display: inline-block; padding: 5px 12px; border-radius: 20px; color: white; font-size: 0.85em; font-weight: 600; text-transform: capitalize; }")
//            .append(".status-menunggu { background-color: #f59e0b; }")
//            .append(".status-progress { background-color: #10b981; }")
//            .append(".status-selesai { background-color: #3b82f6; }")
//            .append(".status-dibatalkan { background-color: #ef4444; }")
//            .append(".urgensi-low { background-color: #fbbf24; }")
//            .append(".urgensi-medium { background-color: #22c55e; }")
//            .append(".urgensi-high { background-color: #dc2626; }")
//            .append("</style>")
//            .append("</head>")
//            .append("<body>")
//            .append("<div class='container'><table>")
//            .append("<thead><tr>")
//            .append("<th>No</th><th>Data Pemohon</th><th>Jenis & Kategori</th><th>Detail Keluhan</th><th>Tindak Lanjut</th><th>Detail Tindak Lanjut</th>")
//            .append("</tr></thead><tbody>");
//
//        int no = 1;
//        ps = koneksi.prepareStatement(
//            "SELECT id_keluhan, tgl_permintaan, unit, petugas, kategori, " +
//            "jenis_keluhan, urgensi, detail_keluhan, status_keluhan, " +
//            "tim_it, detail_penyelesaian, tgl_penyelesaian, waktu_penyelesaian, tgl_onproses " +
//            "FROM keluhan_it ORDER BY tgl_permintaan DESC"
//        );
//        rs = ps.executeQuery();
//
//        while (rs.next()) {
//            String status = rs.getString("status_keluhan");
//            String urgensi = rs.getString("urgensi");
//            String statusClass = "";
//            String urgensiClass = "";
//            String waktuTindakLanjut = "-";
//
//            switch (status.toLowerCase()) {
//                case "menunggu":
//                    statusClass = "status-menunggu";
//                    waktuTindakLanjut = rs.getString("tgl_permintaan");
//                    break;
//                case "on progress":
//                    statusClass = "status-progress";
//                    waktuTindakLanjut = rs.getString("tgl_onproses");
//                    break;
//                case "selesai":
//                    statusClass = "status-selesai";
//                    waktuTindakLanjut = rs.getString("tgl_penyelesaian");
//                    break;
//                case "dibatalkan":
//                    statusClass = "status-dibatalkan";
//                    waktuTindakLanjut = rs.getString("tgl_penyelesaian");
//                    break;
//            }
//
//            switch (urgensi.toLowerCase()) {
//                case "low":
//                    urgensiClass = "urgensi-low";
//                    break;
//                case "medium":
//                    urgensiClass = "urgensi-medium";
//                    break;
//                case "high":
//                    urgensiClass = "urgensi-high";
//                    break;
//            }
//
//            htmlContent.append("<tr>");
//            htmlContent.append("<td>").append(no++).append("</td>");
//
//            htmlContent.append("<td>")
//                .append("<strong>ID Keluhan:</strong> ").append(rs.getString("id_keluhan")).append("<br><br>")
//                .append("<strong>Pemohon / Unit:</strong><br>").append(rs.getString("petugas")).append(" / <br>").append(rs.getString("unit")).append("<br><br>")
//                .append("<strong>Waktu Lapor:</strong> <br>").append(rs.getString("tgl_permintaan"))
//                .append("</td>");
//
//            htmlContent.append("<td>")
//                .append("<strong>Kategori / Jenis:</strong><br>").append(rs.getString("kategori")).append(" / ").append(rs.getString("jenis_keluhan")).append("<br><br>")
//                .append("<strong>Urgensi:</strong><br><span class='badge ").append(urgensiClass).append("'>").append(urgensi).append("</span>")
//                .append("</td>");
//
//            htmlContent.append("<td>")
//                .append("<strong>Detail Keluhan:</strong><br>")
//                .append(rs.getString("detail_keluhan"))
//                .append("</td>");
//
//            htmlContent.append("<td>")
//                .append("<strong>Status:</strong><br><span class='badge ").append(statusClass).append("'>").append(status).append("</span><br><br>")
//                .append("<strong>Waktu:</strong><br>").append(waktuTindakLanjut).append("<br><br>")
//                .append("<strong>Tim IT :</strong><br>").append(rs.getString("tim_it") == null ? "-" : rs.getString("tim_it"))
//                .append("</td>");
//
//            htmlContent.append("<td>")
//                .append(rs.getString("detail_penyelesaian") == null ? "-" : rs.getString("detail_penyelesaian"))
//                .append("<div><br><strong>Waktu Penyelesaian:</strong><br>")
//                .append(rs.getString("waktu_penyelesaian") == null ? "-" : rs.getString("waktu_penyelesaian"))
//                .append("</div>")
//                .append("</td>");
//
//            htmlContent.append("</tr>");
//        }
//
//        htmlContent.append("</tbody></table></div></body></html>");
//
//        File file = new File("monitoring_keluhan.html");
//        try (FileWriter writer = new FileWriter(file)) {
//            writer.write(htmlContent.toString());
//        }
//
//        Desktop.getDesktop().browse(file.toURI());
//
//    } catch (Exception e) {
//        JOptionPane.showMessageDialog(null, "Gagal membuka monitoring: " + e.getMessage());
//    } finally {
//        try {
//            if (rs != null) rs.close();
//            if (ps != null) ps.close();
//        } catch (Exception e) {
//            // log
//        }
//    }    

    try {
        StringBuilder htmlContent = new StringBuilder();

        htmlContent.append("<!DOCTYPE html>")
            .append("<html lang='id'>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<title>Monitoring Keluhan IT</title>")
            .append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap' rel='stylesheet'>")

            // DataTables CSS & JS
            .append("<link rel='stylesheet' href='https://cdn.datatables.net/1.13.6/css/jquery.dataTables.min.css'>")
            .append("<link rel='stylesheet' href='https://cdn.datatables.net/buttons/2.4.1/css/buttons.dataTables.min.css'>")
            .append("<script src='https://code.jquery.com/jquery-3.7.1.min.js'></script>")
            .append("<script src='https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js'></script>")
            .append("<script src='https://cdn.datatables.net/buttons/2.4.1/js/dataTables.buttons.min.js'></script>")
            .append("<script src='https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js'></script>")
            .append("<script src='https://cdn.datatables.net/buttons/2.4.1/js/buttons.html5.min.js'></script>")

            .append("<style>")
            .append("body { font-family: 'Inter', sans-serif; margin: 20px; background-color: #f9fafb; color: #333; font-size: 16px; }")
            .append("table { width: 100%; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }")
            .append("th, td { border: 1px solid #e5e7eb; padding: 12px 15px; vertical-align: top; text-align: left; }")
            .append("thead th { background-color: #2563eb; color: white; text-align: center; font-weight: 600; font-size: 14px; position: sticky; top: 0; z-index: 1; }")
            .append("td { font-size: 15px; }")
            .append("strong { font-size: 13px; color: #555; }")
            .append(".badge { display: inline-block; padding: 5px 12px; border-radius: 20px; color: white; font-size: 0.85em; font-weight: 600; text-transform: capitalize; }")
            .append(".status-menunggu { background-color: #f59e0b; }")
            .append(".status-progress { background-color: #10b981; }")
            .append(".status-selesai { background-color: #3b82f6; }")
            .append(".status-dibatalkan { background-color: #ef4444; }")
            .append(".urgensi-low { background-color: #fbbf24; }")
            .append(".urgensi-medium { background-color: #22c55e; }")
            .append(".urgensi-high { background-color: #dc2626; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<h2>Monitoring Keluhan IT</h2>")
            .append("<div class='table-wrapper'><table id='keluhanTable'>")
            .append("<thead><tr>")
            .append("<th>No</th><th>Data Pemohon</th><th>Jenis & Kategori</th><th>Detail Keluhan</th><th>Tindak Lanjut</th><th>Detail Tindak Lanjut</th>")
            .append("</tr></thead><tbody>");

        int no = 1;
        ps = koneksi.prepareStatement(
            "SELECT id_keluhan, tgl_permintaan, unit, petugas, kategori, " +
            "jenis_keluhan, urgensi, detail_keluhan, status_keluhan, " +
            "tim_it, detail_penyelesaian, tgl_penyelesaian, waktu_penyelesaian, tgl_onproses " +
            "FROM keluhan_it ORDER BY tgl_permintaan DESC"
        );
        rs = ps.executeQuery();

        while (rs.next()) {
            String status = rs.getString("status_keluhan");
            String urgensi = rs.getString("urgensi");
            String statusClass = "";
            String urgensiClass = "";
            String waktuTindakLanjut = "-";

            switch (status.toLowerCase()) {
                case "menunggu": statusClass = "status-menunggu"; waktuTindakLanjut = rs.getString("tgl_permintaan"); break;
                case "on progress": statusClass = "status-progress"; waktuTindakLanjut = rs.getString("tgl_onproses"); break;
                case "selesai": statusClass = "status-selesai"; waktuTindakLanjut = rs.getString("tgl_penyelesaian"); break;
                case "dibatalkan": statusClass = "status-dibatalkan"; waktuTindakLanjut = rs.getString("tgl_penyelesaian"); break;
            }

            switch (urgensi.toLowerCase()) {
                case "low": urgensiClass = "urgensi-low"; break;
                case "medium": urgensiClass = "urgensi-medium"; break;
                case "high": urgensiClass = "urgensi-high"; break;
            }

            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(no++).append("</td>");

            htmlContent.append("<td>")
                .append("<strong>ID Keluhan:</strong> ").append(rs.getString("id_keluhan")).append("<br><br>")
                .append("<strong>Pemohon / Unit:</strong><br>").append(rs.getString("petugas")).append(" / <br>").append(rs.getString("unit")).append("<br><br>")
                .append("<strong>Waktu Lapor:</strong><br>").append(rs.getString("tgl_permintaan"))
                .append("</td>");

            htmlContent.append("<td>")
                .append("<strong>Kategori / Jenis:</strong><br>").append(rs.getString("kategori")).append(" / ").append(rs.getString("jenis_keluhan")).append("<br><br>")
                .append("<strong>Urgensi:</strong><br><span class='badge ").append(urgensiClass).append("'>").append(urgensi).append("</span>")
                .append("</td>");

            htmlContent.append("<td>")
                .append("<strong>Detail Keluhan:</strong><br>")
                .append(rs.getString("detail_keluhan"))
                .append("</td>");

            htmlContent.append("<td>")
                .append("<strong>Status:</strong><br><span class='badge ").append(statusClass).append("'>").append(status).append("</span><br><br>")
                .append("<strong>Waktu:</strong><br>").append(waktuTindakLanjut).append("<br><br>")
                .append("<strong>Tim IT:</strong><br>").append(rs.getString("tim_it") == null ? "-" : rs.getString("tim_it"))
                .append("</td>");

            htmlContent.append("<td>")
                .append(rs.getString("detail_penyelesaian") == null ? "-" : rs.getString("detail_penyelesaian"))
                .append("<div><br><strong>Waktu Penyelesaian:</strong><br>")
                .append(rs.getString("waktu_penyelesaian") == null ? "-" : rs.getString("waktu_penyelesaian"))
                .append("</div>")
                .append("</td>");

            htmlContent.append("</tr>");
        }

        htmlContent.append("</tbody></table></div>")
            .append("<script>")
            .append("$(document).ready(function() {")
            .append("  $('#keluhanTable').DataTable({")
            .append("    dom: 'Bfrtip',")
            .append("    buttons: [ { extend: 'excelHtml5', title: 'Monitoring_Keluhan_IT' } ],")
            .append("    paging: true,")
            .append("    searching: true,")
            .append("    ordering: true,")
            .append("    scrollY: '500px',")
            .append("    scrollCollapse: true")
            .append("  });")
            .append("});")
            .append("</script>")
            .append("</body></html>");

        File file = new File("monitoring_keluhan.html");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(htmlContent.toString());
        }

        Desktop.getDesktop().browse(file.toURI());

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal membuka monitoring: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (Exception e) {
            // log
        }
    }

}//GEN-LAST:event_BtnMonevActionPerformed

    private void BtnMonevKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnMonevKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnMonevActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnMonevKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        try {
            tampil();
        } catch (SQLException ex) {
            Logger.getLogger(KeluhanIT.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        try {
            //        nm_ruangcari.setText("");
            tampil();
        } catch (SQLException ex) {
            Logger.getLogger(KeluhanIT.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnMonev,BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbKeluhanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbKeluhanMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }            
        }
}//GEN-LAST:event_tbKeluhanMouseClicked

    private void tbKeluhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbKeluhanKeyPressed
        if(tabMode.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.setText("");
                TCari.requestFocus();
            }
        }
}//GEN-LAST:event_tbKeluhanKeyPressed

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

private void id_keluhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_id_keluhanKeyPressed
    Valid.pindah(evt,TCari, tgl_permintaan);
}//GEN-LAST:event_id_keluhanKeyPressed

private void tgl_permintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tgl_permintaanKeyPressed
    Valid.pindah(evt,id_keluhan,tgl_permintaan);
}//GEN-LAST:event_tgl_permintaanKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            isiDataLogin();
            tampil();
        } catch (SQLException ex) {
            Logger.getLogger(KeluhanIT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowOpened

    private void tbKeluhanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbKeluhanKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbKeluhanKeyReleased

    private void comboKategoriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboKategoriKeyPressed
        Valid.pindah(evt,tgl_permintaan,combojenis);
    }//GEN-LAST:event_comboKategoriKeyPressed

    private void combojenisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_combojenisKeyPressed
        Valid.pindah(evt,comboKategori,combourgensi);
    }//GEN-LAST:event_combojenisKeyPressed

    private void combourgensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_combourgensiKeyPressed
        Valid.pindah(evt,combojenis,detailkeluhan);
    }//GEN-LAST:event_combourgensiKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbKeluhan.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void statuskeluhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_statuskeluhanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_statuskeluhanKeyPressed

    private void tgl_selesaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tgl_selesaiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tgl_selesaiKeyPressed

    private void BtnOnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnOnProsesActionPerformed
        statuskeluhan.setSelectedItem("On Progress");

        if (id_keluhan.getText().trim().isEmpty()) {
            Notifications.getInstance().show(
                Notifications.Type.INFO,
                Notifications.Location.TOP_CENTER,
                "Silakan pilih data keluhan yang ingin diproses!"
            );
            return;
        }

        try {
            // Ambil NIK user login aktif
            String kodeTimIT = akses.getkode(); // Misalnya: "20101730"

            // Ambil nama pegawai dari tabel pegawai berdasarkan NIK
            String namaTimIT = Sequel.cariIsi("select nama from pegawai where nik=?", kodeTimIT);

            // Set ke textbox timIT (bukan lagi NIK, tapi nama pegawai)
            timIT.setText(namaTimIT);
                               
            java.util.Date now = new java.util.Date();
            java.sql.Timestamp tglOnProses = new java.sql.Timestamp(now.getTime());

            ps = koneksi.prepareStatement(
                "UPDATE keluhan_it SET tgl_onproses=?, status_keluhan=?, tim_it=? WHERE id_keluhan=?"
            );
            ps.setTimestamp(1, tglOnProses);
            ps.setString(2, "On Progress");
            ps.setString(3, namaTimIT);
            ps.setString(4, id_keluhan.getText());

            ps.executeUpdate();

            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Status keluhan berhasil diubah menjadi 'On Progress'!"
            );

            tampil();
            emptTeks();

        } catch (Exception e) {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Gagal mengubah status ke 'On Progress'! " + e
            );
        }
    }//GEN-LAST:event_BtnOnProsesActionPerformed

    private void BtnOnProsesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnOnProsesKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnOnProsesKeyPressed

    private void BtnSelesaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSelesaiActionPerformed
//    if (id_keluhan.getText().trim().isEmpty()) {
//        Notifications.getInstance().show(
//            Notifications.Type.INFO,
//            Notifications.Location.TOP_CENTER,
//            "Silakan pilih data keluhan yang ingin diselesaikan!"
//        );
//        return;
//    }
//
//    statuskeluhan.setSelectedItem("Selesai");
//
//    try {
//        // Waktu sekarang sebagai waktu penyelesaian
//        java.util.Date now = new java.util.Date();
//        java.sql.Timestamp tglSelesai = new java.sql.Timestamp(now.getTime());
//
//        // Ambil tanggal permintaan dari form
//        java.util.Date tglProses = tgl_onproses.getDate();
//
//        // Hitung durasi dalam format human-readable
//        String durasiFormatted = hitungDurasi(tglProses, now);
//
//        // Simpan ke database
//        PreparedStatement ps = koneksi.prepareStatement(
//            "UPDATE keluhan_it SET tgl_penyelesaian=?, waktu_penyelesaian=?, status_keluhan=? WHERE id_keluhan=?"
//        );
//        ps.setTimestamp(1, tglSelesai);
//        ps.setString(2, durasiFormatted);
//        ps.setString(3, "Selesai");
//        ps.setString(4, id_keluhan.getText());
//        ps.executeUpdate();
//
//        Notifications.getInstance().show(
//            Notifications.Type.SUCCESS,
//            Notifications.Location.TOP_CENTER,
//            "Data berhasil diselesaikan!"
//        );
//
//        tampil();
//        emptTeks();
//
//    } catch (Exception e) {
//        Notifications.getInstance().show(
//            Notifications.Type.ERROR,
//            Notifications.Location.TOP_CENTER,
//            "Gagal menyelesaikan data! " + e.getMessage()
//        );
//    }

    if (id_keluhan.getText().trim().isEmpty()) {
        Notifications.getInstance().show(
            Notifications.Type.INFO,
            Notifications.Location.TOP_CENTER,
            "Silakan pilih data keluhan yang ingin diselesaikan!"
        );
        return;
    }

    statuskeluhan.setSelectedItem("Selesai");

    try {
         // Ambil NIK user login aktif
            String kodeTimIT = akses.getkode(); // Misalnya: "20101730"

        // Ambil nama pegawai dari tabel pegawai berdasarkan NIK
        String namaTimIT = Sequel.cariIsi("select nama from pegawai where nik=?", kodeTimIT);

        // Set ke textbox timIT (bukan lagi NIK, tapi nama pegawai)
        timIT.setText(namaTimIT);
        
        // Ambil waktu sekarang sebagai tgl_selesai
        java.util.Date now = new java.util.Date();
        java.sql.Timestamp tglSelesai = new java.sql.Timestamp(now.getTime());

        // Update tgl_penyelesaian dan status_keluhan saja
        PreparedStatement ps = koneksi.prepareStatement(
            "UPDATE keluhan_it SET detail_penyelesaian=?, tgl_penyelesaian=?, status_keluhan=?, tim_it=? WHERE id_keluhan=?"
        );
        ps.setString(1, detailpenyelesaian.getText());
        ps.setTimestamp(2, tglSelesai);
        ps.setString(3, "Selesai");
        ps.setString(4, namaTimIT);
        ps.setString(5, id_keluhan.getText());
        ps.executeUpdate();

        // Jalankan proses update waktu_penyelesaian dari BtnSelesai1
        BtnUpdateWaktuActionPerformed(evt);

        Notifications.getInstance().show(
            Notifications.Type.SUCCESS,
            Notifications.Location.TOP_CENTER,
            "Data keluhan berhasil diselesaikan!"
        );
        kirimWhatsAppSelesai();
        tampil();
        emptTeks();

    } catch (Exception e) {
        Notifications.getInstance().show(
            Notifications.Type.ERROR,
            Notifications.Location.TOP_CENTER,
            "Gagal menyelesaikan data! " + e.getMessage()
        );
    }

    }//GEN-LAST:event_BtnSelesaiActionPerformed

private String hitungDurasi(java.util.Date mulai, java.util.Date selesai) {
    if (mulai == null || selesai == null) return null;

    long selisihMillis = selesai.getTime() - mulai.getTime();
    long totalDetik = selisihMillis / 1000;

    long hari = totalDetik / (24 * 3600);
    long sisaHari = totalDetik % (24 * 3600);
    long jam = sisaHari / 3600;
    long sisaJam = sisaHari % 3600;
    long menit = sisaJam / 60;
    long detik = sisaJam % 60;

    StringBuilder sb = new StringBuilder();
    if (hari > 0) sb.append(hari).append(" hari ");
    if (jam > 0) sb.append(jam).append(" jam ");
    if (menit > 0) sb.append(menit).append(" menit ");
    if (detik > 0 || sb.length() == 0) sb.append(detik).append(" detik");

    return sb.toString().trim();
}    
    
    private void BtnSelesaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSelesaiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSelesaiKeyPressed

    private void BtndibatalkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtndibatalkanActionPerformed
        statuskeluhan.setSelectedItem("Dibatalkan");

        if (id_keluhan.getText().trim().isEmpty()) {
            Notifications.getInstance().show(
                Notifications.Type.INFO,
                Notifications.Location.TOP_CENTER,
                "Silakan pilih data keluhan yang ingin dibatalkan!"
            );
            return;
        }

        try {
            // Ambil NIK user login aktif
            String kodeTimIT = akses.getkode(); // Misalnya: "20101730"

            // Ambil nama pegawai dari tabel pegawai berdasarkan NIK
            String namaTimIT = Sequel.cariIsi("select nama from pegawai where nik=?", kodeTimIT);

            // Set ke textbox timIT (bukan lagi NIK, tapi nama pegawai)
            timIT.setText(namaTimIT);
        
            ps = koneksi.prepareStatement(
                "UPDATE keluhan_it SET status_keluhan=?, tim_it=? WHERE id_keluhan=?"
            );
            ps.setString(1, "Dibatalkan");
            ps.setString(2, namaTimIT);
            ps.setString(3, id_keluhan.getText());

            ps.executeUpdate();

            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Status keluhan berhasil dibatalkan!"
            );

            tampil();
            emptTeks();

        } catch (Exception e) {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Gagal membatalkan keluhan! " + e
            );
        }
    }//GEN-LAST:event_BtndibatalkanActionPerformed

    private void BtndibatalkanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtndibatalkanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtndibatalkanKeyPressed

    private void tgl_onprosesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tgl_onprosesKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tgl_onprosesKeyPressed

    private void BtnUpdateWaktuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateWaktuActionPerformed
        try {
            // Ambil tanggal on proses dan tanggal selesai dari form
            java.util.Date tglOnProses = tgl_onproses.getDate();
            java.util.Date tglSelesai = tgl_selesai.getDate();
                        if (tglSelesai == null) {
                            tglSelesai = new Date(); // waktu saat ini
                        }

            if (tglOnProses == null || tglSelesai == null) {
                Notifications.getInstance().show(
                    Notifications.Type.WARNING,
                    Notifications.Location.TOP_CENTER,
                    "Tanggal on proses atau tanggal selesai belum diisi!"
                );
                return;
            }

            // Hitung durasi antara tglOnProses dan tglSelesai
            String durasiFormatted = hitungDurasi(tglOnProses, tglSelesai);

            // Simpan durasi ke kolom waktu_penyelesaian
            PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE keluhan_it SET waktu_penyelesaian=? WHERE id_keluhan=?"
            );
            ps.setString(1, durasiFormatted);
            ps.setString(2, id_keluhan.getText());
            ps.executeUpdate();

            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Waktu penyelesaian berhasil diperbarui!"
            );

            tampil();

        } catch (Exception e) {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Gagal memperbarui waktu penyelesaian! " + e.getMessage()
            );
        }
    }//GEN-LAST:event_BtnUpdateWaktuActionPerformed

    private void BtnUpdateWaktuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateWaktuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateWaktuKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeluhanIT dialog = new KeluhanIT(new javax.swing.JFrame(), true);
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
    private widget.Button BtnBaru;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnMonev;
    private widget.Button BtnOnProses;
    private widget.Button BtnSelesai;
    private widget.Button BtnSimpan;
    private widget.Button BtnUpdateWaktu;
    private widget.Button Btndibatalkan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.Label LCount;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.ComboBox comboKategori;
    private widget.ComboBox combojenis;
    private widget.ComboBox combourgensi;
    private widget.TextArea detailkeluhan;
    private widget.TextArea detailpenyelesaian;
    private widget.TextBox id_keluhan;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextPane jTextPane1;
    private widget.Label label11;
    private widget.Label label13;
    private widget.Label label14;
    private widget.Label labelID;
    private widget.Label labeldetailkeluhan;
    private widget.Label labeldetailpenyelesaian;
    private widget.Label labeljenis;
    private widget.Label labelkategori1;
    private widget.Label labelketkategori;
    private widget.Label labelketurgensi;
    private widget.Label labelstatus;
    private widget.Label labeltglpermintaan;
    private widget.Label labeltglselesai;
    private widget.Label labeltglselesai1;
    private widget.Label labelurgensi;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextBox petugas_pemohon;
    private widget.ComboBox statuskeluhan;
    private widget.Table tbKeluhan;
    private widget.Tanggal tgl_onproses;
    private widget.Tanggal tgl_permintaan;
    private widget.Tanggal tgl_selesai;
    private widget.TextBox timIT;
    private widget.TextBox unit;
    private widget.TextBox waktu_penyelesaian;
    // End of variables declaration//GEN-END:variables

    public void tampil() throws SQLException {
    // Ambil NIK user login
    String nikLogin = akses.getkode();
    
    // Ambil data petugas dan departemen
    String namaPetugas = Sequel.cariIsi("SELECT nama FROM pegawai WHERE nik=?", nikLogin);
    String namaUnit = Sequel.cariIsi(
        "SELECT departemen.nama FROM departemen " +
        "INNER JOIN pegawai ON departemen.dep_id = pegawai.departemen " +
        "WHERE pegawai.nik=?", nikLogin
    );
    
    // Set nilai ke field form
    petugas_pemohon.setText(namaPetugas);
    unit.setText(namaUnit);
    timIT.getText(); // diasumsikan user login juga jadi pengerja
    tgl_permintaan.setDate(new Date()); // default ke hari ini

    // Tampilkan semua data di tabel (jika ingin langsung munculin semua keluhan yang pernah diinput)
    Valid.tabelKosong(tabMode);
    try {
        ps = koneksi.prepareStatement(
            "SELECT id_keluhan, tgl_permintaan, unit, petugas, kategori, " +
            "jenis_keluhan, urgensi, detail_keluhan, status_keluhan, " +
            "tim_it, detail_penyelesaian, tgl_penyelesaian, waktu_penyelesaian, tgl_onproses " +
            "FROM keluhan_it " +
            "WHERE unit LIKE ? OR petugas LIKE ? OR kategori LIKE ? OR status_keluhan LIKE ? " +
            "ORDER BY tgl_permintaan DESC"
        );
        try {
            String keyword = "%" + TCari.getText().trim() + "%";
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);
            ps.setString(4, keyword);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("id_keluhan"),
                    rs.getString("tgl_permintaan"),
                    rs.getString("unit"),
                    rs.getString("petugas"),
                    rs.getString("kategori"),
                    rs.getString("jenis_keluhan"),
                    rs.getString("urgensi"),
                    rs.getString("detail_keluhan"),
                    rs.getString("status_keluhan"),
                    rs.getString("tim_it"),
                    rs.getString("detail_penyelesaian"),
                    rs.getString("tgl_penyelesaian"),
                    rs.getString("waktu_penyelesaian"),
                    rs.getString("tgl_onproses")
                });
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampil: " + e);
        } finally {
            if (rs != null) { rs.close(); }
            if (ps != null) { ps.close(); }
        }
    } catch (Exception e) {
        System.out.println("Notifikasi tampil outer: " + e);
    }
}

    public void emptTeks() {       
    id_keluhan.setText("");
    tgl_permintaan.setDate(new Date()); // misalnya default hari ini
//    unit.setText("");
//    petugas_pemohon.setText("");
    comboKategori.setSelectedIndex(0);
    combojenis.setSelectedIndex(0);
    combourgensi.setSelectedIndex(0);
    detailkeluhan.setText("");
    statuskeluhan.setSelectedIndex(0);
    timIT.setText("");
    detailpenyelesaian.setText("");

    // Jangan set ke format aneh, cukup null
    tgl_selesai.setDate(new Date()); // misalnya default hari ini

    waktu_penyelesaian.setText("");
    
    }

    private void getData() {
    
    detailpenyelesaian.setText("");    
    detailpenyelesaian.setText("");
    int row = tbKeluhan.getSelectedRow();
    if (row != -1) {
        try {            
            id_keluhan.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),0).toString());
//            Valid.SetTgl(tgl_permintaan,tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),1).toString());
            unit.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),2).toString());
            petugas_pemohon.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),3).toString());
            comboKategori.setSelectedItem(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),4).toString());
            combojenis.setSelectedItem(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),5).toString());
            combourgensi.setSelectedItem(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),6).toString());
            detailkeluhan.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),7).toString());
            statuskeluhan.setSelectedItem(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),8).toString());
            timIT.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),9).toString());
            detailpenyelesaian.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),10).toString());
//            Valid.SetTgl(tgl_selesai,tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),11).toString());
            waktu_penyelesaian.setText(tbKeluhan.getValueAt(tbKeluhan.getSelectedRow(),12).toString());    
            
            try {
                Object val = tbKeluhan.getValueAt(row, 1);
                if (val != null && !val.toString().trim().equals("") && !"null".equalsIgnoreCase(val.toString())) {
                    java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(val.toString());
                    tgl_permintaan.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_permintaan, parsedDate);
                } else {
                    tgl_permintaan.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_permintaan, (Object) null);
                }
            } catch (Exception e) {
                System.out.println("Error parsing " + tgl_permintaan + ": " + e);
            }
            
            try {
                Object val = tbKeluhan.getValueAt(row, 11);
                if (val != null && !val.toString().trim().equals("") && !"null".equalsIgnoreCase(val.toString())) {
                    java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(val.toString());
                    tgl_selesai.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_selesai, parsedDate);
                } else {
                    tgl_selesai.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_selesai, (Object) null);
                }
            } catch (Exception e) {
                System.out.println("Error parsing " + tgl_selesai + ": " + e);
            }
            
            try {
                Object val = tbKeluhan.getValueAt(row, 13);
                if (val != null && !val.toString().trim().equals("") && !"null".equalsIgnoreCase(val.toString())) {
                    java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(val.toString());
                    tgl_onproses.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_onproses, parsedDate);
                } else {
                    tgl_onproses.getClass().getMethod("setDate", java.util.Date.class).invoke(tgl_onproses, (Object) null);
                }
            } catch (Exception e) {
                System.out.println("Error parsing " + tgl_onproses + ": " + e);
            }
            
        } catch (Exception ex) {
            System.out.println("Error getData(): " + ex);
        }
    }
}

private void setTanggalDariTable(int row, int colIndex, Object komponenTanggal, String namaField) {
    try {
        Object val = tabMode.getValueAt(row, colIndex);
        if (val != null && !val.toString().trim().equals("") && !"null".equalsIgnoreCase(val.toString())) {
            java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(val.toString());
            komponenTanggal.getClass().getMethod("setDate", java.util.Date.class).invoke(komponenTanggal, parsedDate);
        } else {
            komponenTanggal.getClass().getMethod("setDate", java.util.Date.class).invoke(komponenTanggal, (Object) null);
        }
    } catch (Exception e) {
        System.out.println("Error parsing " + namaField + ": " + e);
    }
}

    public JTable getTable(){
        return tbKeluhan;
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,350));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    public void isCek(){
        if(akses.getjml2()>=1){
//            nip.setEditable(false);
//            btnPtg.setEnabled(false);
            BtnSimpan.setEnabled(akses.getperbaikan_inventaris());
            BtnHapus.setEnabled(akses.getperbaikan_inventaris());
            BtnEdit.setEnabled(akses.getperbaikan_inventaris());
            BtnMonev.setEnabled(akses.getperbaikan_inventaris());
//            nip.setText(akses.getkode());
//            nama_petugas.setText(petugas.tampil3(nip.getText()));
        } 
        TCari.requestFocus();
    }

// Method untuk mengatur teks label
private void updateLabelKategori() {
String selected = (String) comboKategori.getSelectedItem();
if (selected != null) {
        switch (selected) {
            case "Software":
                labelketkategori.setText("( Permasalahan terkait Aplikasi, SIMRS, Sistem Operasi, Virus, dll )");
                break;
            case "Hardware":
                labelketkategori.setText("( Permasalahan terkait PC, Monitor, Printer, Scanner, dll )");
                break;
            case "Jaringan":
                labelketkategori.setText("( Permasalahan terkait Internet, Koneksi, dll )");
                break;
            default:
                labelketkategori.setText("");
                break;
        }
    }
}

// Method untuk mengatur teks label
private void updateLabelUrgensi() {
String selected = (String) combourgensi.getSelectedItem();
if (selected != null) {
        switch (selected) {
            case "Low":
                labelketurgensi.setText("( Tidak Mendesak )");
                break;
            case "Medium":
                labelketurgensi.setText("( Sedang )");
                break;
            case "High":
                labelketurgensi.setText("( Sangat Mendesak )");
                break;
            default:
                labelketurgensi.setText("");
                break;
        }
    }
}

//private void kirimWhatsApp() {
//    try {
//        String apiKey = "D82dDMpq16n-PbkS@WPd"; // Ganti dengan API Key asli D82dDMpq16n-PbkS@WPd
//        String targetNomor = "120363381103636508@g.us"; // RAWAT INAP
////        String targetNomor = "120363322805623245@g.us"; // RAWAT JALAN
//
//        String pesan = "🛠 *Keluhan Baru Masuk*\n"
//                     + "*ID Keluhan:* " + id_keluhan.getText() + "\n"
//                     + "*Unit:* " + unit.getText() + "\n"
//                     + "*Pemohon:* " + petugas_pemohon.getText() + "\n"
//                     + "*Kategori:* " + comboKategori.getSelectedItem().toString() + "\n"
//                     + "*Jenis:* " + combojenis.getSelectedItem().toString() + "\n"
//                     + "*Urgensi:* " + combourgensi.getSelectedItem().toString() + "\n"
//                     + "*Detail:* " + detailkeluhan.getText() + "\n"
//                     + "*Status:* " + statuskeluhan.getSelectedItem().toString();
//
//        URL url = new URL("https://api.fonnte.com/send");
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//        conn.setRequestProperty("Authorization", apiKey);
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//        String data = "target=" + targetNomor + "&message=" + URLEncoder.encode(pesan, "UTF-8");
//
//        try (OutputStream os = conn.getOutputStream()) {
//            os.write(data.getBytes());
//        }
//
//        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            System.out.println("Response dari Fonnte : " + response.toString());
//        }
//    } catch (Exception e) {
//        System.err.println("Gagal mengirim WA: " + e.getMessage());
//    }
//}


private void kirimWhatsAppKeluhan() {
    try {
        String apiKey = "D82dDMpq16n-PbkS@WPd"; // Ganti dengan API Key asli

        // Dua target grup
        String targetNomorRalan = "120363381103636508@g.us"; // RAWAT INAP
        String targetNomorRanap = "120363322805623245@g.us"; // RAWAT JALAN
        String targetNomorMonev = "120363419621292687@g.us"; // RAWAT JALAN120363419621292687@g.us

        // Format tanggal permintaan
        String tglPermintaanStr = "";
        if (tgl_permintaan.getDate() != null) {
            tglPermintaanStr = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(tgl_permintaan.getDate());
        }

        // Format isi pesan WA
        String pesan = ""
                        + "--------------------------\n"
                        + "🛠 *_Keluhan Baru Masuk_*\n"
                        + "--------------------------\n"
                        + "*_Jam Lapor :_*\n"
                        + tglPermintaanStr + "\n"
                        + "*_Pemohon / Unit :_* \n"
                        + petugas_pemohon.getText() + " / " + unit.getText() + "\n"
                        + "*_Kategori / Jenis :_* \n"
                        + comboKategori.getSelectedItem().toString() + " / " + combojenis.getSelectedItem().toString() + "\n"
                        + "*_Urgensi / Status :_* \n"
                        + combourgensi.getSelectedItem().toString() + " / " + statuskeluhan.getSelectedItem().toString() + "\n"
                        + "--------------------------\n"
                        + "*_Detail :_* \n"
                        + detailkeluhan.getText() + "\n"
                        + "--------------------------";

        // Kirim ke dua grup
        kirimKeFonnte(apiKey, targetNomorRalan, pesan);
        kirimKeFonnte(apiKey, targetNomorRanap, pesan);
        kirimKeFonnte(apiKey, targetNomorMonev, pesan);

    } catch (Exception e) {
        System.err.println("Gagal menyiapkan pesan WA: " + e.getMessage());
    }
}

private void kirimWhatsAppSelesai() {
    try {
        String apiKey = "D82dDMpq16n-PbkS@WPd"; // Ganti dengan API Key asli

        // Dua target grup
        String targetNomorRalan = "120363381103636508@g.us"; // RAWAT INAP
        String targetNomorRanap = "120363322805623245@g.us"; // RAWAT JALAN
        String targetNomorMonev = "120363419621292687@g.us"; // RAWAT JALAN120363419621292687@g.us     
       
        // Format tanggal permintaan
        String tglSelesaiStr = "";
        if (tgl_selesai.getDate() != null) {
            tglSelesaiStr = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(tgl_selesai.getDate());
        }

        // Format isi pesan WhatsApp
        String pesan = ""
                + "--------------------------\n"
                + "🛠 *_Penyelesaian Keluhan_*\n"
                + "--------------------------\n"
                + "*_Jam Selesai :_*\n"
                + tglSelesaiStr + "\n"
                + "*_Diselesai oleh :_*\n"
                + timIT.getText() + "\n"                                
                + "*_Waktu Penyelesaian :_*\n"
                + waktu_penyelesaian.getText() + "\n"
                + "--------------------------\n"
                + "*_Detail Keluhan :_*\n"
                + detailkeluhan.getText() + "\n"
                + "--------------------------\n"                               
                + "*_Detail Penyelesaian :_*\n"
                + detailpenyelesaian.getText() + "\n"
                + "--------------------------";

        // Kirim ke grup WhatsApp
        kirimKeFonnte(apiKey, targetNomorRalan, pesan);
        kirimKeFonnte(apiKey, targetNomorRanap, pesan);
        kirimKeFonnte(apiKey, targetNomorMonev, pesan);

    } catch (Exception e) {
        System.err.println("Gagal menyiapkan pesan WA: " + e.getMessage());
    }
}

// Method bantu untuk kirim
private void kirimKeFonnte(String apiKey, String targetNomor, String pesan) {
    try {
        URL url = new URL("https://api.fonnte.com/send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", apiKey);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String data = "target=" + targetNomor + "&message=" + URLEncoder.encode(pesan, "UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("WA ke " + targetNomor + ": " + response.toString());
        }
    } catch (Exception e) {
        System.err.println("Gagal kirim ke WA: " + e.getMessage());
    }
}


private void isiDataLogin() {
    String nikLogin = akses.getkode();
    String namaPetugasLogin = Sequel.cariIsi("SELECT nama FROM pegawai WHERE nik=?", nikLogin);
    String unitLogin = Sequel.cariIsi(
        "SELECT departemen.nama FROM departemen INNER JOIN pegawai ON departemen.dep_id = pegawai.departemen WHERE pegawai.nik=?", nikLogin
    );
    petugas_pemohon.setText(namaPetugasLogin);    
    unit.setText(unitLogin);  
}

private String getStringValue(Object obj) {
    return obj != null ? obj.toString() : "";
}


}
