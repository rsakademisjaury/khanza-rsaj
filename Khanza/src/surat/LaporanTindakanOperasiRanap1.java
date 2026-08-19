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
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import laporan.DlgBerkasRawat;


/**
 * 
 * @author salimmulyana
 */
public final class LaporanTindakanOperasiRanap1 extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private String tgl,finger,bln_angka = "", bln_romawi = "",kodedokter="",namadokter="";
    private DlgCariDokter dokter=new DlgCariDokter(null,false);


    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public LaporanTindakanOperasiRanap1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(628,674);
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat",           // 0
            "Nama Operator",      // 1
            "Nama Anestesi",      // 2
            "Jenis Anestesi",     // 3
            "Golongan Operasi",   // 4
            "Diagnosis Pra Operasi", // 5
            "Nama Asisten",       // 6
            "Nama Perawat",       // 7
            "Diagnosis Pasca Operasi", // 8
            "Indikasi Operasi",   // 9
            "Nama Tindakan",      // 10
            "Jaringan Dieksisi",  // 11
            "Mulai Operasi",      // 12
            "Selesai Operasi",    // 13
            "Komplikasi Operasi", // 14
            "Jumlah Perdarahan",  // 15
            "Perawatan Pasca Operasi",//16
            "Laporan Tindakan"    // 17
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbOperasi.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbOperasi.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbOperasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 17; i++) {
            TableColumn column = tbOperasi.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(105);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(150);
            }else if(i==4){
                column.setPreferredWidth(65);
            }else if(i==5){
                column.setPreferredWidth(150);
            }else if(i==6){
                column.setPreferredWidth(90);
            }else if(i==7){
                column.setPreferredWidth(150);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(150);    
            }else if(i==10){
                column.setPreferredWidth(150);        
            }else if(i==11){
                column.setPreferredWidth(150);  
            }else if(i==12){
                column.setPreferredWidth(150); 
            }else if(i==13){
                column.setPreferredWidth(150); 
            }else if(i==14){
                column.setPreferredWidth(150); 
            }else if(i==15){
                column.setPreferredWidth(150); 
            }else if(i==16){
                column.setPreferredWidth(150); 
            }else if(i==17){
                column.setPreferredWidth(150);             
            }
        }
        tbOperasi.setDefaultRenderer(Object.class, new WarnaTable());        
        DiagnosisPra.setDocument(new batasInput((byte)25).getKata(DiagnosisPra));
        TNoRw.setDocument(new batasInput((byte)25).getKata(TNoRw));           
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));          
//        diagnosa_utama.setDocument(new batasInput((byte)1000).getKata(diagnosa_utama));           
//        diagnosa_sekunder.setDocument(new batasInput((byte)1000).getKata(diagnosa_sekunder));     
//        indikasi.setDocument(new batasInput((byte)1000).getKata(indikasi));     
//        masa_berlaku.setDocument(new batasInput((byte)255).getKata(masa_berlaku));     
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
            });
        }        
        
        ChkInput.setSelected(true);
        tampil();
        isForm();
                

    }              
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakLaporanTindakan = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        DiagnosisPra = new widget.TextBox();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        jLabel14 = new widget.Label();
        MulaiOperasi = new widget.Tanggal();
        jLabel5 = new widget.Label();
        KdDokter = new widget.TextBox();
        NamaDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        label14 = new widget.Label();
        jLabel15 = new widget.Label();
        jLabel16 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        LaporanTindakan = new widget.TextArea();
        NamaAnestesi = new widget.TextBox();
        jLabel8 = new widget.Label();
        NamaOperator = new widget.TextBox();
        jLabel9 = new widget.Label();
        jLabel11 = new widget.Label();
        jLabel17 = new widget.Label();
        NamaAsisten = new widget.TextBox();
        NamaPerawat = new widget.TextBox();
        GolonganOperasi = new widget.ComboBox();
        jLabel10 = new widget.Label();
        DiagnosisPasca = new widget.TextBox();
        jLabel12 = new widget.Label();
        IndikasiOperasi = new widget.TextBox();
        jLabel18 = new widget.Label();
        NamaTindakan = new widget.TextBox();
        jLabel20 = new widget.Label();
        JaringanDieksisi = new widget.TextBox();
        jLabel21 = new widget.Label();
        KomplikasiOperasi = new widget.TextBox();
        jLabel22 = new widget.Label();
        JumlahPerdarahan = new widget.TextBox();
        jLabel23 = new widget.Label();
        label15 = new widget.Label();
        SelesaiOperasi = new widget.Tanggal();
        JenisAnestesi = new widget.ComboBox();
        jLabel24 = new widget.Label();
        PerawatanPasca = new widget.TextBox();
        ChkInput = new widget.CekBox();
        Scroll = new widget.ScrollPane();
        tbOperasi = new widget.Table();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakLaporanTindakan.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakLaporanTindakan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakLaporanTindakan.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakLaporanTindakan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakLaporanTindakan.setText("Cetak Laporan Tindakan Operasi");
        MnCetakLaporanTindakan.setName("MnCetakLaporanTindakan"); // NOI18N
        MnCetakLaporanTindakan.setPreferredSize(new java.awt.Dimension(316, 30));
        MnCetakLaporanTindakan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakLaporanTindakanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakLaporanTindakan);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Laporan Tindakan Operasi Rawat Inap ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
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
        panelGlass8.add(BtnAll);

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

        jLabel19.setText("Tgl. Periksa :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(67, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        DTPCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DTPCari1ActionPerformed(evt);
            }
        });
        panelGlass9.add(DTPCari1);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        DTPCari2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DTPCari2ActionPerformed(evt);
            }
        });
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 400));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1000, 600));
        FormInput.setLayout(null);

        DiagnosisPra.setHighlighter(null);
        DiagnosisPra.setName("DiagnosisPra"); // NOI18N
        DiagnosisPra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosisPraKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosisPra);
        DiagnosisPra.setBounds(140, 130, 420, 23);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 70, 23);

        TNoRw.setText("2025/01/06/000125");
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 120, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(265, 10, 295, 23);

        TNoRM.setEditable(false);
        TNoRM.setText("123456");
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(200, 10, 60, 23);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Jenis Anestesi");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(15, 100, 100, 23);

        MulaiOperasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2025 12:24:33" }));
        MulaiOperasi.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        MulaiOperasi.setName("MulaiOperasi"); // NOI18N
        FormInput.add(MulaiOperasi);
        MulaiOperasi.setBounds(720, 190, 140, 23);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("Diagnosis Pra Bedah");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(15, 130, 130, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(250, 610, 87, 23);

        NamaDokter.setEditable(false);
        NamaDokter.setName("NamaDokter"); // NOI18N
        NamaDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NamaDokter);
        NamaDokter.setBounds(360, 630, 280, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
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
        BtnDokter.setBounds(640, 630, 28, 23);

        label14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label14.setText("Tanggal Operasi");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(570, 190, 120, 23);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("Golongan Operasi");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(570, 100, 110, 20);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Nama dr. Anestesi");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(15, 70, 120, 26);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        LaporanTindakan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        LaporanTindakan.setColumns(20);
        LaporanTindakan.setForeground(new java.awt.Color(0, 0, 0));
        LaporanTindakan.setRows(5);
        LaporanTindakan.setName("LaporanTindakan"); // NOI18N
        LaporanTindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LaporanTindakanKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(LaporanTindakan);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(15, 280, 1150, 280);

        NamaAnestesi.setForeground(new java.awt.Color(0, 0, 0));
        NamaAnestesi.setHighlighter(null);
        NamaAnestesi.setName("NamaAnestesi"); // NOI18N
        NamaAnestesi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaAnestesiKeyPressed(evt);
            }
        });
        FormInput.add(NamaAnestesi);
        NamaAnestesi.setBounds(140, 70, 420, 23);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Nama Operator");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(15, 40, 100, 23);

        NamaOperator.setForeground(new java.awt.Color(0, 0, 0));
        NamaOperator.setHighlighter(null);
        NamaOperator.setName("NamaOperator"); // NOI18N
        NamaOperator.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaOperatorKeyPressed(evt);
            }
        });
        FormInput.add(NamaOperator);
        NamaOperator.setBounds(140, 40, 420, 23);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(161, 40, 40, 23);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Nama Asisten");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(571, 40, 90, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Nama Perawat");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(571, 70, 90, 26);

        NamaAsisten.setForeground(new java.awt.Color(0, 0, 0));
        NamaAsisten.setHighlighter(null);
        NamaAsisten.setName("NamaAsisten"); // NOI18N
        NamaAsisten.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaAsistenKeyPressed(evt);
            }
        });
        FormInput.add(NamaAsisten);
        NamaAsisten.setBounds(720, 40, 450, 23);

        NamaPerawat.setForeground(new java.awt.Color(0, 0, 0));
        NamaPerawat.setHighlighter(null);
        NamaPerawat.setName("NamaPerawat"); // NOI18N
        NamaPerawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaPerawatKeyPressed(evt);
            }
        });
        FormInput.add(NamaPerawat);
        NamaPerawat.setBounds(720, 70, 450, 23);

        GolonganOperasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Khusus", "Besar", "Sedang", "Kecil", "Elektive", "Emergency" }));
        GolonganOperasi.setName("GolonganOperasi"); // NOI18N
        FormInput.add(GolonganOperasi);
        GolonganOperasi.setBounds(720, 100, 450, 22);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText("Diagnosis Pasca Bedah");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(570, 130, 130, 23);

        DiagnosisPasca.setHighlighter(null);
        DiagnosisPasca.setName("DiagnosisPasca"); // NOI18N
        DiagnosisPasca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosisPascaKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosisPasca);
        DiagnosisPasca.setBounds(720, 130, 450, 23);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Indikasi Operasi");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(15, 160, 120, 23);

        IndikasiOperasi.setHighlighter(null);
        IndikasiOperasi.setName("IndikasiOperasi"); // NOI18N
        IndikasiOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IndikasiOperasiKeyPressed(evt);
            }
        });
        FormInput.add(IndikasiOperasi);
        IndikasiOperasi.setBounds(140, 160, 420, 23);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText("Nama Tindakan");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(570, 160, 130, 23);

        NamaTindakan.setHighlighter(null);
        NamaTindakan.setName("NamaTindakan"); // NOI18N
        NamaTindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaTindakanKeyPressed(evt);
            }
        });
        FormInput.add(NamaTindakan);
        NamaTindakan.setBounds(720, 160, 450, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("Jaringan dieksisi");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(15, 190, 110, 23);

        JaringanDieksisi.setHighlighter(null);
        JaringanDieksisi.setName("JaringanDieksisi"); // NOI18N
        JaringanDieksisi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JaringanDieksisiKeyPressed(evt);
            }
        });
        FormInput.add(JaringanDieksisi);
        JaringanDieksisi.setBounds(140, 190, 420, 23);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("Komplikasi Operasi");
        jLabel21.setName("jLabel21"); // NOI18N
        FormInput.add(jLabel21);
        jLabel21.setBounds(15, 220, 120, 23);

        KomplikasiOperasi.setHighlighter(null);
        KomplikasiOperasi.setName("KomplikasiOperasi"); // NOI18N
        KomplikasiOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KomplikasiOperasiKeyPressed(evt);
            }
        });
        FormInput.add(KomplikasiOperasi);
        KomplikasiOperasi.setBounds(140, 220, 420, 23);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("Jumlah Pendarahan");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(570, 220, 120, 23);

        JumlahPerdarahan.setHighlighter(null);
        JumlahPerdarahan.setName("JumlahPerdarahan"); // NOI18N
        JumlahPerdarahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JumlahPerdarahanKeyPressed(evt);
            }
        });
        FormInput.add(JumlahPerdarahan);
        JumlahPerdarahan.setBounds(720, 220, 450, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Laporan Tindakan Operasi");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(15, 250, 190, 23);

        label15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label15.setText("Selesai Operasi");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label15);
        label15.setBounds(920, 190, 100, 23);

        SelesaiOperasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2025 12:24:34" }));
        SelesaiOperasi.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        SelesaiOperasi.setName("SelesaiOperasi"); // NOI18N
        FormInput.add(SelesaiOperasi);
        SelesaiOperasi.setBounds(1020, 190, 150, 23);

        JenisAnestesi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Anestesi Umum (General Anesthesia)", "Anestesi Regional", "Anestesi Lokal", "Anestesi Sedasi", "Anestesi Kombinasi" }));
        JenisAnestesi.setName("JenisAnestesi"); // NOI18N
        FormInput.add(JenisAnestesi);
        JenisAnestesi.setBounds(140, 100, 420, 22);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Perawatan Pasca Operasi");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(570, 250, 150, 23);

        PerawatanPasca.setHighlighter(null);
        PerawatanPasca.setName("PerawatanPasca"); // NOI18N
        PerawatanPasca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PerawatanPascaKeyPressed(evt);
            }
        });
        FormInput.add(PerawatanPasca);
        PerawatanPasca.setBounds(720, 250, 450, 23);

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
        PanelInput.add(ChkInput, java.awt.BorderLayout.SOUTH);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbOperasi.setAutoCreateRowSorter(true);
        tbOperasi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbOperasi.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbOperasi.setComponentPopupMenu(jPopupMenu1);
        tbOperasi.setName("tbOperasi"); // NOI18N
        tbOperasi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbOperasiMouseClicked(evt);
            }
        });
        tbOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbOperasiKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbOperasiKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbOperasi);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        internalFrame1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void DiagnosisPraKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosisPraKeyPressed
       Valid.pindah(evt,TCari,LaporanTindakan);
}//GEN-LAST:event_DiagnosisPraKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
            isPsien();
        }else{            
            Valid.pindah(evt,TCari,DiagnosisPra);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
    if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
        Valid.textKosong(TNoRw, "pasien");
    } else if (NamaOperator.getText().trim().equals("")) {
        Valid.textKosong(NamaOperator, "Nama Operator");
    } else if (NamaAnestesi.getText().trim().equals("")) {
        Valid.textKosong(NamaAnestesi, "Nama Anestesi");
    } else if (JenisAnestesi.getSelectedItem().equals("-")) {
        Valid.textKosong(JenisAnestesi, "Jenis Anestesi");
    } else if (GolonganOperasi.getSelectedItem().equals("-")) {
        Valid.textKosong(GolonganOperasi, "Golongan Operasi");
    } else if (DiagnosisPra.getText().trim().equals("")) {
        Valid.textKosong(DiagnosisPra, "Diagnosis Pra Operasi");
    } else if (IndikasiOperasi.getText().trim().equals("")) {
        Valid.textKosong(IndikasiOperasi, "Indikasi Operasi");
    } else if (NamaTindakan.getText().trim().equals("")) {
        Valid.textKosong(NamaTindakan, "Nama Tindakan");
    } else if (JaringanDieksisi.getText().trim().equals("")) {
        Valid.textKosong(JaringanDieksisi, "Jaringan Dieksisi");
    } else if (KomplikasiOperasi.getText().trim().equals("")) {
        Valid.textKosong(KomplikasiOperasi, "Komplikasi Operasi");
    } else if (NamaAsisten.getText().trim().equals("")) {
        Valid.textKosong(NamaAsisten, "Nama Asisten");
    } else if (NamaPerawat.getText().trim().equals("")) {
        Valid.textKosong(NamaPerawat, "Nama Perawat");
    } else if (DiagnosisPasca.getText().trim().equals("")) {
        Valid.textKosong(DiagnosisPasca, "Diagnosis Pasca Operasi");
    } else if (JumlahPerdarahan.getText().trim().equals("")) {
        Valid.textKosong(JumlahPerdarahan, "Jumlah Perdarahan");
    } else if (PerawatanPasca.getText().trim().equals("")) {
        Valid.textKosong(PerawatanPasca, "Perawatan Pasca Operasi");    
    } else if (LaporanTindakan.getText().trim().equals("")) {
        Valid.textKosong(LaporanTindakan, "Laporan Tindakan Operasi");        
    } else {
        
        String mulaiOperasiFormatted = Valid.SetTanggalJam(MulaiOperasi.getSelectedItem().toString());
        String selesaiOperasiFormatted = Valid.SetTanggalJam(SelesaiOperasi.getSelectedItem().toString());
        
        if (Sequel.menyimpantf(
            "tindakan_operasi",
            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?", // Sesuaikan dengan jumlah parameter
            "No.Rawat",
            18, // Jumlah parameter yang sesuai
            new String[]{
                TNoRw.getText(),
                NamaOperator.getText(),
                NamaAnestesi.getText(),
                JenisAnestesi.getSelectedItem().toString(),
                GolonganOperasi.getSelectedItem().toString(),
                DiagnosisPra.getText(),
                NamaAsisten.getText(),
                NamaPerawat.getText(),
                DiagnosisPasca.getText(),
                IndikasiOperasi.getText(),
                NamaTindakan.getText(),
                JaringanDieksisi.getText(),
                mulaiOperasiFormatted,
                selesaiOperasiFormatted,
                KomplikasiOperasi.getText(),
                JumlahPerdarahan.getText(),        
                PerawatanPasca.getText(),
                LaporanTindakan.getText()                    
            }) == true
        ) {
            tampil();
            emptTeks();
            ChkInput.setSelected(false);
             // Menambahkan pesan sukses
            JOptionPane.showMessageDialog(null, "Laporan Tindakan Operasi telah disimpan", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,NamaAnestesi,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm(); 
        
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(Valid.hapusTabletf(tabMode,TNoRw,"tindakan_operasi","no_rawat")==true){
            if(tbOperasi.getSelectedRow()!= -1){
                tabMode.removeRow(tbOperasi.getSelectedRow());
                emptTeks();
                LCount.setText(""+tabMode.getRowCount());
            }
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
    if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
        Valid.textKosong(TNoRw, "pasien");
    } else if (NamaOperator.getText().trim().equals("")) {
        Valid.textKosong(NamaOperator, "Nama Operator");
    } else if (NamaAnestesi.getText().trim().equals("")) {
        Valid.textKosong(NamaAnestesi, "Nama Anestesi");
    } else if (JenisAnestesi.getSelectedItem().equals("-")) {
        Valid.textKosong(JenisAnestesi, "Jenis Anestesi");
    } else if (GolonganOperasi.getSelectedItem().equals("-")) {
        Valid.textKosong(GolonganOperasi, "Golongan Operasi");
    } else if (DiagnosisPra.getText().trim().equals("")) {
        Valid.textKosong(DiagnosisPra, "Diagnosis Pra Operasi");
    } else if (IndikasiOperasi.getText().trim().equals("")) {
        Valid.textKosong(IndikasiOperasi, "Indikasi Operasi");
    } else if (NamaTindakan.getText().trim().equals("")) {
        Valid.textKosong(NamaTindakan, "Nama Tindakan");
    } else if (JaringanDieksisi.getText().trim().equals("")) {
        Valid.textKosong(JaringanDieksisi, "Jaringan Dieksisi");
    } else if (KomplikasiOperasi.getText().trim().equals("")) {
        Valid.textKosong(KomplikasiOperasi, "Komplikasi Operasi");
    } else if (NamaAsisten.getText().trim().equals("")) {
        Valid.textKosong(NamaAsisten, "Nama Asisten");
    } else if (NamaPerawat.getText().trim().equals("")) {
        Valid.textKosong(NamaPerawat, "Nama Perawat");
    } else if (DiagnosisPasca.getText().trim().equals("")) {
        Valid.textKosong(DiagnosisPasca, "Diagnosis Pasca Operasi");
    } else if (JumlahPerdarahan.getText().trim().equals("")) {
        Valid.textKosong(JumlahPerdarahan, "Jumlah Perdarahan");
    } else if (PerawatanPasca.getText().trim().equals("")) {
        Valid.textKosong(PerawatanPasca, "Perawatan Pasca Operasi");    
    } else if (LaporanTindakan.getText().trim().equals("")) {
        Valid.textKosong(LaporanTindakan, "Laporan Tindakan Operasi");      
    } else {
        // Format waktu mulai dan selesai operasi
        String mulaiOperasiFormatted = Valid.SetTanggalJam(MulaiOperasi.getSelectedItem().toString());
        String selesaiOperasiFormatted = Valid.SetTanggalJam(SelesaiOperasi.getSelectedItem().toString());

        // Update data ke database
        if (Sequel.mengedittf(
            "tindakan_operasi", 
            "no_rawat=?",
            "operator=?,anastesi=?,jenis_anastesi=?,golongan_operasi=?,diagnosa_preop=?,"
            + "asisten=?,perawat=?,diagnosa_postop=?,indikasi_operasi=?,nama_tindakan=?,"
            + "jaringan_dieksisi=?,mulai_operasi=?,selesai_operasi=?,komplikasi=?,"
            + "jumlah_perdarahan=?,perawatan_pasca_operasi=?,laporan_tindakan_operasi=?", 
            18,
            new String[]{
                NamaOperator.getText(),
                NamaAnestesi.getText(),
                JenisAnestesi.getSelectedItem().toString(),
                GolonganOperasi.getSelectedItem().toString(),
                DiagnosisPra.getText(),
                NamaAsisten.getText(),
                NamaPerawat.getText(),
                DiagnosisPasca.getText(),
                IndikasiOperasi.getText(),
                NamaTindakan.getText(),
                JaringanDieksisi.getText(),
                mulaiOperasiFormatted,
                selesaiOperasiFormatted,
                KomplikasiOperasi.getText(),
                JumlahPerdarahan.getText(),
                PerawatanPasca.getText(),
                LaporanTindakan.getText(),
                TNoRw.getText()
            })
        ) {            
            ChkInput.setSelected(false);
            isForm();
            tampil();   
            
            // Menampilkan pesan sukses
            JOptionPane.showMessageDialog(null, "Data berhasil diperbarui", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Gagal memperbarui data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


//        if(DiagnosisPra.getText().trim().equals("")){
//            Valid.textKosong(DiagnosisPra,"No.Surat");
//        }else if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
//            Valid.textKosong(TNoRw,"pasien");        
//        }else if(LaporanTindakan.getText().trim().equals("")){
//            Valid.textKosong(TNoRw,"Dosis Perhitungan");        
//        }else if(NamaAnestesi.getText().trim().equals("")){
//            Valid.textKosong(TNoRw,"Kadar Faktor");  
//        }else{
//            if(tbObat.getSelectedRow()!= -1){
//                if(Sequel.mengedittf("tindakan_operasi","no_rawat=?","no_rawat=?,no_rawat=?,tanggalperiksa=?,dosis_perhitungan=?,on_demand=?,evaluasi=?,kadar_faktor=?",8,new String[]{
//                        DiagnosisPra.getText(),TNoRw.getText(),Valid.SetTgl(MulaiOperasi.getSelectedItem()+""),LaporanTindakan.getText(),NamaAnestesi.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
//                    })==true){
//                    tbObat.setValueAt(DiagnosisPra.getText(),tbObat.getSelectedRow(),0);
//                    tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),1);
//                    tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),2);
//                    tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),3);
//                    tbObat.setValueAt(Valid.SetTgl(MulaiOperasi.getSelectedItem()+""),tbObat.getSelectedRow(),4);
//                    tbObat.setValueAt(LaporanTindakan.getText(),tbObat.getSelectedRow(),5);                    
//                    tbObat.setValueAt(NamaAnestesi.getText(),tbObat.getSelectedRow(),8);                    
//                    emptTeks();
//                }
//            }
//        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
    if(TPasien.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
    } else {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Map<String, Object> param = new HashMap<>();         
        // Ambil kd_dokter dari database
        String kdDokter = Sequel.cariIsi("SELECT rawat_inap_drpr.kd_dokter FROM rawat_inap_drpr WHERE rawat_inap_drpr.no_rawat = ?", TNoRw.getText());
        
        // Masukkan ke dalam parameter
        param.put("kd_dokter", kdDokter);
        param.put("ttd", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/tandatangandokter/pages/upload/" + kdDokter + ".png");
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());         
        
        String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
        String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
        param.put("logo2", logoPath);        
        param.put("logo", logoPath);
        
        finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 6).toString());
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 7).toString() + "\nID " + (finger.equals("") ? tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 6).toString() : finger) + "\n" + Valid.SetTgl3(tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 4).toString()));

        Valid.MyReportqry("rptLaporanTindakanOperasi.jasper", "report", "::[ Laporan Tindakan Operasi ]::",
            "SELECT tindakan_operasi.no_rawat, "
            + "DATE_FORMAT(reg_periksa.tgl_registrasi, '%d %M %Y') AS tglregistrasi, "
            + "DATE_FORMAT( pasien.tgl_lahir, '%d %M %Y' ) AS tgl_lahir, "  
            + "poliklinik.nm_poli, "     
            + "reg_periksa.umurdaftar, "        
            + "reg_periksa.sttsumur, "
            + "pasien.jk, "        
            + "tindakan_operasi.operator, "
            + "tindakan_operasi.anastesi, "
            + "tindakan_operasi.jenis_anastesi, "
            + "tindakan_operasi.golongan_operasi, " 
            + "tindakan_operasi.diagnosa_preop, "
            + "tindakan_operasi.asisten, "
            + "tindakan_operasi.perawat, "
            + "tindakan_operasi.diagnosa_postop, "
            + "tindakan_operasi.indikasi_operasi, "
            + "tindakan_operasi.nama_tindakan, "
            + "tindakan_operasi.jaringan_dieksisi, " 
            + "tindakan_operasi.mulai_operasi, "
            + "tindakan_operasi.selesai_operasi, "
            + "IF(tindakan_operasi.selesai_operasi IS NULL OR tindakan_operasi.mulai_operasi IS NULL, '00:00:00', TIMEDIFF(tindakan_operasi.selesai_operasi, tindakan_operasi.mulai_operasi)) AS durasi_operasi, " // Penanganan Null
            + "tindakan_operasi.komplikasi, "
            + "tindakan_operasi.jumlah_perdarahan, "
            + "tindakan_operasi.perawatan_pasca_operasi, "
            + "tindakan_operasi.laporan_tindakan_operasi, "
            + "pasien.nm_pasien, "
            + "pasien.no_rkm_medis, "            
            + "rawat_inap_drpr.kd_dokter "
            + "FROM tindakan_operasi "
            + "INNER JOIN reg_periksa ON reg_periksa.no_rawat = tindakan_operasi.no_rawat "
            + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
            + "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
            + "INNER JOIN poliklinik ON reg_periksa.kd_poli = poliklinik.kd_poli "        
            + "LEFT JOIN rawat_inap_drpr ON tindakan_operasi.no_rawat = rawat_inap_drpr.no_rawat "                          
            + "WHERE tindakan_operasi.no_rawat = '" + TNoRw.getText() + "' ", param);
        
        this.setCursor(Cursor.getDefaultCursor());  
    }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
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
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil();
            TCari.setText("");
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed
   
                                  
    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
       
}//GEN-LAST:event_TNoRMKeyPressed

    private void tbOperasiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbOperasiMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbOperasiMouseClicked

    private void tbOperasiKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbOperasiKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbOperasiKeyReleased

    private void DTPCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DTPCari1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari1ActionPerformed

    private void DTPCari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DTPCari2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari2ActionPerformed

    private void MnCetakLaporanTindakanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakLaporanTindakanActionPerformed
    if(TPasien.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
    } else {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Map<String, Object> param = new HashMap<>();         
        param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",KdDokter.getText()));            
        param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());                 
        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
        
        finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 6).toString());
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 7).toString() + "\nID " + (finger.equals("") ? tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 6).toString() : finger) + "\n" + Valid.SetTgl3(tbOperasi.getValueAt(tbOperasi.getSelectedRow(), 4).toString()));

        Valid.MyReportqry("rptLaporanTindakanOperasiInap.jasper", "report", "::[ Laporan Tindakan Operasi ]::",
                        "SELECT tindakan_operasi.no_rawat, "
                        + "DATE_FORMAT(tindakan_operasi.selesai_operasi, '%d %M %Y') AS tglcetak, "
                        + "DATE_FORMAT(pasien.tgl_lahir, '%d %M %Y') AS tgl_lahir, "  
                        + "bangsal.nm_bangsal, "     
                        + "reg_periksa.umurdaftar, "        
                        + "reg_periksa.sttsumur, "
                        + "pasien.jk, "        
                        + "tindakan_operasi.operator, "
                        + "tindakan_operasi.anastesi, "
                        + "tindakan_operasi.jenis_anastesi, "
                        + "tindakan_operasi.golongan_operasi, " 
                        + "tindakan_operasi.diagnosa_preop, "
                        + "tindakan_operasi.asisten, "
                        + "tindakan_operasi.perawat, "
                        + "tindakan_operasi.diagnosa_postop, "
                        + "tindakan_operasi.indikasi_operasi, "
                        + "tindakan_operasi.nama_tindakan, "
                        + "tindakan_operasi.jaringan_dieksisi, " 
                        + "tindakan_operasi.mulai_operasi, "
                        + "tindakan_operasi.selesai_operasi, "                    
                        + "IF(tindakan_operasi.selesai_operasi IS NULL OR tindakan_operasi.mulai_operasi IS NULL, '00:00:00', TIMEDIFF(tindakan_operasi.selesai_operasi, tindakan_operasi.mulai_operasi)) AS durasi_operasi, "
                        + "tindakan_operasi.komplikasi, "
                        + "tindakan_operasi.jumlah_perdarahan, "
                        + "tindakan_operasi.perawatan_pasca_operasi, "
                        + "tindakan_operasi.laporan_tindakan_operasi, "
                        + "pasien.nm_pasien, "
                        + "pasien.no_rkm_medis, "            
                        + "rawat_inap_drpr.kd_dokter "
                        + "FROM tindakan_operasi "
                        + "INNER JOIN reg_periksa ON reg_periksa.no_rawat = tindakan_operasi.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN kamar_inap ON tindakan_operasi.no_rawat = kamar_inap.no_rawat "  
                        + "INNER JOIN kamar ON kamar.kd_kamar = kamar_inap.kd_kamar " 
                        + "INNER JOIN bangsal ON bangsal.kd_bangsal = kamar.kd_bangsal "        
                        + "LEFT JOIN rawat_inap_drpr ON tindakan_operasi.no_rawat = rawat_inap_drpr.no_rawat "                          
                        + "WHERE tindakan_operasi.no_rawat = '" + TNoRw.getText() + "' ", param);
        
        this.setCursor(Cursor.getDefaultCursor());  
    }
    }//GEN-LAST:event_MnCetakLaporanTindakanActionPerformed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        Valid.pindah(evt,TCari,LaporanTindakan);
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,TCari,LaporanTindakan);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void LaporanTindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LaporanTindakanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(evt.isShiftDown()){
                LaporanTindakan.requestFocus();
            }
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            LaporanTindakan.requestFocus();
        }
    }//GEN-LAST:event_LaporanTindakanKeyPressed

    private void NamaAnestesiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaAnestesiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaAnestesiKeyPressed

    private void NamaOperatorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaOperatorKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaOperatorKeyPressed

    private void NamaAsistenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaAsistenKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaAsistenKeyPressed

    private void NamaPerawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaPerawatKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaPerawatKeyPressed

    private void DiagnosisPascaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosisPascaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DiagnosisPascaKeyPressed

    private void IndikasiOperasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IndikasiOperasiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IndikasiOperasiKeyPressed

    private void NamaTindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaTindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamaTindakanKeyPressed

    private void JaringanDieksisiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JaringanDieksisiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JaringanDieksisiKeyPressed

    private void KomplikasiOperasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KomplikasiOperasiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KomplikasiOperasiKeyPressed

    private void JumlahPerdarahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahPerdarahanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JumlahPerdarahanKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void tbOperasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbOperasiKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    ChkInput.setSelected(true);
                    isForm(); 
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbOperasiKeyPressed

    private void PerawatanPascaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PerawatanPascaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PerawatanPascaKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            LaporanTindakanOperasiRanap1 dialog = new LaporanTindakanOperasiRanap1(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);            
            dialog.tampil();
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
    private widget.TextBox DiagnosisPasca;
    private widget.TextBox DiagnosisPra;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox GolonganOperasi;
    private widget.TextBox IndikasiOperasi;
    private widget.TextBox JaringanDieksisi;
    private widget.ComboBox JenisAnestesi;
    private widget.TextBox JumlahPerdarahan;
    private widget.TextBox KdDokter;
    private widget.TextBox KomplikasiOperasi;
    private widget.Label LCount;
    private widget.TextArea LaporanTindakan;
    private javax.swing.JMenuItem MnCetakLaporanTindakan;
    private widget.Tanggal MulaiOperasi;
    private widget.TextBox NamaAnestesi;
    private widget.TextBox NamaAsisten;
    private widget.TextBox NamaDokter;
    private widget.TextBox NamaOperator;
    private widget.TextBox NamaPerawat;
    private widget.TextBox NamaTindakan;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox PerawatanPasca;
    private widget.ScrollPane Scroll;
    private widget.Tanggal SelesaiOperasi;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
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
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label label14;
    private widget.Label label15;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPane2;
    private widget.Table tbOperasi;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
    DefaultTableModel tabMode = (DefaultTableModel) tbOperasi.getModel();
    tabMode.setRowCount(0); // Membersihkan tabel sebelum menampilkan data

    // Cek nilai no_rawat
    String noRawat = TNoRw.getText().trim();
//    if (noRawat.isEmpty()) {
//        JOptionPane.showMessageDialog(null, "No. Rawat tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
//        return;
//    }

    try {
        // Query untuk mengambil data dari tabel tindakan_operasi berdasarkan no_rawat
        String sql = "SELECT "
                + "no_rawat, "
                + "operator, "
                + "anastesi, "
                + "jenis_anastesi, "
                + "golongan_operasi, "
                + "diagnosa_preop, "
                + "asisten, "
                + "perawat, "
                + "diagnosa_postop, "
                + "indikasi_operasi, " 
                + "nama_tindakan, "
                + "jaringan_dieksisi, "
                + "mulai_operasi, "
                + "selesai_operasi, " 
                + "komplikasi, "
                + "jumlah_perdarahan, "
                + "perawatan_pasca_operasi, "
                + "laporan_tindakan_operasi "
                + "FROM tindakan_operasi "
                + "WHERE no_rawat = ?";

        // Debugging: Cetak query dan parameter
        System.out.println("No. Rawat: " + noRawat);
        System.out.println("Query: " + sql);

        ps = koneksi.prepareStatement(sql); // Membuat prepared statement
        ps.setString(1, noRawat); // Mengatur parameter no_rawat (tanpa wildcard)
        rs = ps.executeQuery(); // Eksekusi query

        while (rs.next()) {
            // Menambahkan data dari ResultSet ke tabel
            tabMode.addRow(new Object[]{
                rs.getString("no_rawat"),
                rs.getString("operator"),
                rs.getString("anastesi"),
                rs.getString("jenis_anastesi"),
                rs.getString("golongan_operasi"),
                rs.getString("diagnosa_preop"),
                rs.getString("asisten"),
                rs.getString("perawat"),
                rs.getString("diagnosa_postop"),
                rs.getString("indikasi_operasi"),
                rs.getString("nama_tindakan"),
                rs.getString("jaringan_dieksisi"),
                rs.getString("mulai_operasi"),
                rs.getString("selesai_operasi"),
                rs.getString("komplikasi"),
                rs.getString("jumlah_perdarahan"),
                rs.getString("perawatan_pasca_operasi"),
                rs.getString("laporan_tindakan_operasi")
            });
        }

//        // Jika tidak ada data yang ditemukan
//        if (tabMode.getRowCount() == 0) {
//            JOptionPane.showMessageDialog(null, "Tidak ada data ditemukan untuk No. Rawat: " + noRawat, "Informasi", JOptionPane.INFORMATION_MESSAGE);
//        }
    } catch (SQLException e) {
        // Menampilkan pesan error jika terjadi kesalahan
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menampilkan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menutup koneksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
 
    public void emptTeks() {
        TNoRw.setText("");
        TPasien.setText("");
        NamaOperator.setText("");
        NamaAnestesi.setText("");
        DiagnosisPra.setText("");
        IndikasiOperasi.setText("");
        NamaTindakan.setText("");
        JaringanDieksisi.setText("");
        KomplikasiOperasi.setText("");
        NamaAsisten.setText("");
        NamaPerawat.setText("");
        DiagnosisPasca.setText("");
        JumlahPerdarahan.setText("");
        PerawatanPasca.setText("");
        LaporanTindakan.setText("");

        // Mengosongkan ComboBox
        JenisAnestesi.setSelectedIndex(0);  // Jika Anda ingin setel ulang ke item pertama
        GolonganOperasi.setSelectedIndex(0); // Jika Anda ingin setel ulang ke item pertama
    }

 
    private void getData() {
        if (tbOperasi.getSelectedRow() != -1) { // Mengecek apakah ada baris yang dipilih
        int row = tbOperasi.getSelectedRow();
        
        // Mengambil nilai dari baris yang dipilih berdasarkan kolom
        TNoRw.setText(tbOperasi.getValueAt(row, 0).toString());
        NamaOperator.setText(tbOperasi.getValueAt(row, 1).toString());
        NamaAnestesi.setText(tbOperasi.getValueAt(row, 2).toString());
        JenisAnestesi.setSelectedItem(tbOperasi.getValueAt(row, 3).toString());
        GolonganOperasi.setSelectedItem(tbOperasi.getValueAt(row, 4).toString());
        DiagnosisPra.setText(tbOperasi.getValueAt(row, 5).toString());
        NamaAsisten.setText(tbOperasi.getValueAt(row, 6).toString());
        NamaPerawat.setText(tbOperasi.getValueAt(row, 7).toString());
        DiagnosisPasca.setText(tbOperasi.getValueAt(row, 8).toString());
        IndikasiOperasi.setText(tbOperasi.getValueAt(row, 9).toString());
        NamaTindakan.setText(tbOperasi.getValueAt(row, 10).toString());
        JaringanDieksisi.setText(tbOperasi.getValueAt(row, 11).toString());
        Valid.SetTgl(MulaiOperasi, tbOperasi.getValueAt(row, 12).toString()); // Mengisi field tanggal
        Valid.SetTgl(SelesaiOperasi, tbOperasi.getValueAt(row, 13).toString()); // Mengisi field tanggal
        KomplikasiOperasi.setText(tbOperasi.getValueAt(row, 14).toString());
        JumlahPerdarahan.setText(tbOperasi.getValueAt(row, 15).toString());
        PerawatanPasca.setText(tbOperasi.getValueAt(row, 16).toString());
        LaporanTindakan.setText(tbOperasi.getValueAt(row, 17).toString());
    } else {
        JOptionPane.showMessageDialog(null, "Silakan pilih data yang ingin ditampilkan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}

    private void isRawat() {
         Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='"+TNoRw.getText()+"' ",TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }
    
    public void setNoRm(String norwt,String norm,String pasien, Date tgl1, Date tgl2) {
    
//    PreparedStatement  ps = null;
//    ResultSet rs = null;
//        
//    TNoRw.setText(norwt);
//    TCari.setText(norwt);        
//    DTPCari1.setDate(tgl1);
//    DTPCari2.setDate(tgl2);
//    TNoRM.setText(norm);
//    TPasien.setText(pasien); 
//
//    // Menampilkan nama tindakan (nm_perawatan)
//    NamaTindakan.setText(Sequel.cariIsi(
//        "SELECT jns_perawatan_inap.nm_perawatan " +
//        "FROM rawat_inap_drpr " +
//        "INNER JOIN jns_perawatan_inap ON rawat_inap_drpr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw " +
//        "WHERE rawat_inap_drpr.no_rawat = ?", 
//        TNoRw.getText()
//    ));
//
//    // Menampilkan nama perawat (nama)
//    NamaPerawat.setText(Sequel.cariIsi(
//        "SELECT pegawai.nama " +
//        "FROM rawat_inap_drpr " +
//        "INNER JOIN pegawai ON rawat_inap_drpr.nip = pegawai.nik " +
//        "WHERE rawat_inap_drpr.no_rawat = ?", 
//        TNoRw.getText()
//    ));
//
//    // Menampilkan nama operator (nm_dokter)
//    NamaOperator.setText(Sequel.cariIsi(
//        "SELECT dokter.nm_dokter " +
//        "FROM rawat_inap_drpr " +
//        "INNER JOIN dokter ON rawat_inap_drpr.kd_dokter = dokter.kd_dokter " +
//        "WHERE rawat_inap_drpr.no_rawat = ?", 
//        TNoRw.getText()
//    ));
//    
//    // Menampilkan nama operator (nm_dokter)
//    KdDokter.setText(Sequel.cariIsi(
//        "SELECT kd_dokter " +
//        "FROM rawat_inap_drpr " +        
//        "WHERE rawat_inap_drpr.no_rawat = ?", 
//        TNoRw.getText()
//    ));
//
//    // Parsing tanggal secara langsung
//    String tanggalTindakan = Sequel.cariIsi("select tgl_perawatan from rawat_inap_drpr where no_rawat=?", TNoRw.getText());
//    String jamTindakan = Sequel.cariIsi("select jam_rawat from rawat_inap_drpr where no_rawat=?", TNoRw.getText());
//
//    System.out.println("Tanggal Tindakan: " + tanggalTindakan); // Debugging
//    System.out.println("Jam Tindakan: " + jamTindakan);         // Debugging
//
//    if (tanggalTindakan != null && jamTindakan != null) {
//        String tanggalDanJam = tanggalTindakan + " " + jamTindakan; // Gabungkan tanggal dan jam
//        System.out.println("Tanggal dan Jam Gabungan: " + tanggalDanJam); // Debugging
//
//        try {
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date mulaiOperasi = formatter.parse(tanggalDanJam); // Parsing ke Date
//            MulaiOperasi.setDate(mulaiOperasi); // Set ke MulaiOperasi
//        } catch (ParseException e) {
//            e.printStackTrace(); // Tangani error parsing
//            System.err.println("Error Parsing Date: " + e.getMessage()); // Debugging
//        }
//    } else {
//        MulaiOperasi.setDate(null); // Set ke null jika data tidak ada
//    }        
//
//    try {
//        String query = "Select count (*) from tindakan_operasi where no_rawat = ?";
//        ps = koneksi.prepareStatement(query);
//        ps.setString(1,norwt);
//        rs = ps.executeQuery();
//        
//        if (rs.next()) {
//            int count = rs.getInt(1);
//            ChkInput.setSelected(count==0);            
//        }        
//    } catch (SQLException e){
//        e.printStackTrace();        
//    } finally {
//        try{
//            if (rs !=null) rs.close();
//            if (ps !=null) ps.close();            
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//    
//    isForm();   
//    tampil();

    TNoRw.setText(norwt);
    TCari.setText(norwt);
    DTPCari1.setDate(tgl1);
    DTPCari2.setDate(tgl2);
    TNoRM.setText(norm);
    TPasien.setText(pasien);

    // Query gabungan untuk mengurangi jumlah akses database
    String query = "SELECT jpi.nm_perawatan, pegawai.nama, dokter.nm_dokter, rid.kd_dokter " +
                   "FROM rawat_inap_drpr rid " +
                   "LEFT JOIN jns_perawatan_inap jpi ON rid.kd_jenis_prw = jpi.kd_jenis_prw " +
                   "LEFT JOIN pegawai ON rid.nip = pegawai.nik " +
                   "LEFT JOIN dokter ON rid.kd_dokter = dokter.kd_dokter " +
                   "WHERE rid.no_rawat = ? LIMIT 1";

    try (PreparedStatement ps = koneksi.prepareStatement(query)) {
        ps.setString(1, norwt);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                NamaTindakan.setText(rs.getString("nm_perawatan"));
                NamaPerawat.setText(rs.getString("nama"));
                NamaOperator.setText(rs.getString("nm_dokter"));
                KdDokter.setText(rs.getString("kd_dokter"));

                // Menghapus logika terkait tanggalTindakan dan jamTindakan
                MulaiOperasi.setDate(new Date());
                SelesaiOperasi.setDate(new Date());
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Cek apakah ada tindakan operasi
    try (PreparedStatement ps = koneksi.prepareStatement("SELECT COUNT(*) FROM tindakan_operasi WHERE no_rawat = ?")) {
        ps.setString(1, norwt);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                ChkInput.setSelected(rs.getInt(1) == 0);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    isForm();
    tampil();

}        
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,this.getHeight()-122));
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
        BtnSimpan.setEnabled(akses.getsurat_kewaspadaan_kesehatan());
        BtnHapus.setEnabled(akses.getsurat_kewaspadaan_kesehatan());
        BtnEdit.setEnabled(akses.getsurat_kewaspadaan_kesehatan());
    }

    public void setNoRm(String string, String string0, Date date, Date date0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}




