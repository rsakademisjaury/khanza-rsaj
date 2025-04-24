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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import laporan.DlgBerkasRawat;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


/**
 * 
 * @author salimmulyana
 */
public final class SuratJaminanPenunjangDiagnostik extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private String tgl,finger="",FileName="",kodeberkas="";
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public SuratJaminanPenunjangDiagnostik(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(628,674);
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Surat","No.Rawat","No.R.M.","Nama Pasien","Tgl.Periksa","diagnosa_masuk","Kode Dokter","Nama Dokter","Keperluan"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
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
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());        
        NoSurat.setDocument(new batasInput((byte)25).getKata(NoSurat));
        TNoRw.setDocument(new batasInput((byte)25).getKata(TNoRw));           
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));          
        Jadwal.setDocument(new batasInput((byte)50).getKata(Jadwal));   
        diagnosa_masuk.setDocument(new batasInput((byte)50).getKata(diagnosa_masuk));           
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
        MnCetakSJPD = new javax.swing.JMenuItem();
        ppBerkasDigital = new javax.swing.JMenuItem();
        MnCetakSJPDTTD = new javax.swing.JMenuItem();
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
        BtnUpload = new widget.Button();
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
        jLabel3 = new widget.Label();
        NoSurat = new widget.TextBox();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        TanggalPeriksa = new widget.Tanggal();
        jLabel5 = new widget.Label();
        Jadwal = new widget.TextBox();
        diagnosa_masuk = new widget.TextBox();
        KdDokter = new widget.TextBox();
        NamaDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        label14 = new widget.Label();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakSJPD.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakSJPD.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSJPD.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSJPD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSJPD.setText("Cetak SJPD Barcode");
        MnCetakSJPD.setName("MnCetakSJPD"); // NOI18N
        MnCetakSJPD.setPreferredSize(new java.awt.Dimension(316, 30));
        MnCetakSJPD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSJPDActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSJPD);

        ppBerkasDigital.setBackground(new java.awt.Color(255, 255, 254));
        ppBerkasDigital.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBerkasDigital.setForeground(new java.awt.Color(50, 50, 50));
        ppBerkasDigital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBerkasDigital.setText("Berkas Digital Perawatan");
        ppBerkasDigital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBerkasDigital.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBerkasDigital.setName("ppBerkasDigital"); // NOI18N
        ppBerkasDigital.setPreferredSize(new java.awt.Dimension(250, 30));
        ppBerkasDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBerkasDigitalBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBerkasDigital);

        MnCetakSJPDTTD.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakSJPDTTD.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSJPDTTD.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSJPDTTD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSJPDTTD.setText("Cetak SJPD TTD");
        MnCetakSJPDTTD.setName("MnCetakSJPDTTD"); // NOI18N
        MnCetakSJPDTTD.setPreferredSize(new java.awt.Dimension(316, 30));
        MnCetakSJPDTTD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSJPDTTDActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSJPDTTD);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Surat Jaminan Penunjang Diagnostik ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setAutoCreateRowSorter(true);
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

        BtnUpload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpload.setMnemonic('T');
        BtnUpload.setText("Upload");
        BtnUpload.setToolTipText("Alt+T");
        BtnUpload.setName("BtnUpload"); // NOI18N
        BtnUpload.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUploadActionPerformed(evt);
            }
        });
        BtnUpload.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUploadKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnUpload);

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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-11-2024" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-11-2024" }));
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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 96));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 165));
        FormInput.setLayout(null);

        jLabel3.setText("No. Surat :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(10, 160, 70, 23);

        NoSurat.setHighlighter(null);
        NoSurat.setName("NoSurat"); // NOI18N
        NoSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoSuratKeyPressed(evt);
            }
        });
        FormInput.add(NoSurat);
        NoSurat.setBounds(130, 160, 200, 23);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(321, 10, 250, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(218, 10, 100, 23);

        jLabel13.setText("Tanggal :");
        jLabel13.setEnabled(false);
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(330, 160, 55, 23);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Diagnosa Masuk :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(10, 40, 100, 23);

        TanggalPeriksa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-11-2024" }));
        TanggalPeriksa.setDisplayFormat("dd-MM-yyyy");
        TanggalPeriksa.setEnabled(false);
        TanggalPeriksa.setName("TanggalPeriksa"); // NOI18N
        FormInput.add(TanggalPeriksa);
        TanggalPeriksa.setBounds(390, 160, 90, 23);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("Jadwal :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(10, 70, 68, 23);

        Jadwal.setHighlighter(null);
        Jadwal.setName("Jadwal"); // NOI18N
        Jadwal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JadwalKeyPressed(evt);
            }
        });
        FormInput.add(Jadwal);
        Jadwal.setBounds(120, 70, 450, 23);

        diagnosa_masuk.setHighlighter(null);
        diagnosa_masuk.setName("diagnosa_masuk"); // NOI18N
        diagnosa_masuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosa_masukKeyPressed(evt);
            }
        });
        FormInput.add(diagnosa_masuk);
        diagnosa_masuk.setBounds(120, 40, 450, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(110, 250, 90, 23);

        NamaDokter.setEditable(false);
        NamaDokter.setName("NamaDokter"); // NOI18N
        NamaDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NamaDokter);
        NamaDokter.setBounds(200, 250, 290, 23);

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
        BtnDokter.setBounds(510, 220, 28, 23);

        label14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label14.setText("Dokter P.J. :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(60, 220, 70, 23);

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
       Valid.pindah(evt,TCari,diagnosa_masuk);
}//GEN-LAST:event_NoSuratKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
            isPsien();
        }else{            
            Valid.pindah(evt,TCari,NoSurat);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(Jadwal.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Jadwal");
        }else if(diagnosa_masuk.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Diagnosa Masuk");
        }else{
            if(Sequel.menyimpantf("surat_jaminan_penunjang_diagnostik","?,?,?,?,?,?","No.Surat",6,new String[]{
                    NoSurat.getText(),TNoRw.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),diagnosa_masuk.getText(),Jadwal.getText(),KdDokter.getText()
            })==true){
                tampil();
                emptTeks();
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Jadwal,BtnBatal);
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
        if(Valid.hapusTabletf(tabMode,NoSurat,"surat_jaminan_penunjang_diagnostik","no_surat")==true){
            if(tbObat.getSelectedRow()!= -1){
                tabMode.removeRow(tbObat.getSelectedRow());
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
        if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(Jadwal.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Keperluan");
        }else if(diagnosa_masuk.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"diagnosa_masuk");
        }else{
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.mengedittf("surat_jaminan_penunjang_diagnostik","no_surat=?","no_surat=?,no_rawat=?,tanggalperiksa=?,diagnosa_masuk=?,jadwal=?",6,new String[]{
                        NoSurat.getText(),TNoRw.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),diagnosa_masuk.getText(),Jadwal.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
                    })==true){
                    tbObat.setValueAt(NoSurat.getText(),tbObat.getSelectedRow(),0);
                    tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),1);
                    tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),2);
                    tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),3);
                    tbObat.setValueAt(Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),tbObat.getSelectedRow(),4);
                    tbObat.setValueAt(diagnosa_masuk.getText(),tbObat.getSelectedRow(),5);
                    tbObat.setValueAt(Jadwal.getText(),tbObat.getSelectedRow(),8);
                    emptTeks();
                }
            }
        }
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
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        if(tabMode.getRowCount()==0){
//            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
//            BtnBatal.requestFocus();
//        }else if(tabMode.getRowCount()!=0){
//            Map<String, Object> param = new HashMap<>(); 
//                param.put("namars",akses.getnamars());
//                param.put("alamatrs",akses.getalamatrs());
//                param.put("kotars",akses.getkabupatenrs());
//                param.put("propinsirs",akses.getpropinsirs());
//                param.put("kontakrs",akses.getkontakrs());
//                param.put("emailrs",akses.getemailrs());   
//                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
//            tgl=" surat_jaminan_penunjang_diagnostik.tanggalperiksa between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
//            if(TCari.getText().trim().equals("")){
//                Valid.MyReportqry("rptDataSuratKewaspadaanKesehatan.jasper","report","::[ Data Surat Keterangan Kewaspadaan Kesehatan ]::",
//                     "select surat_jaminan_penunjang_diagnostik.no_surat,surat_jaminan_penunjang_diagnostik.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
//                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa,surat_jaminan_penunjang_diagnostik.diagnosa_masuk,reg_periksa.kd_dokter,dokter.nm_dokter,surat_jaminan_penunjang_diagnostik.jadwal "+                  
//                     "from surat_jaminan_penunjang_diagnostik inner join reg_periksa on surat_jaminan_penunjang_diagnostik.no_rawat=reg_periksa.no_rawat "+
//                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on dokter.kd_dokter=reg_periksa.kd_dokter "+
//                     "where "+tgl+"order by surat_jaminan_penunjang_diagnostik.no_surat",param);
//            }else{
//                Valid.MyReportqry("rptDataSuratKewaspadaanKesehatan.jasper","report","::[ Data Surat Keterangan Kewaspadaan Kesehatan ]::",
//                     "select surat_jaminan_penunjang_diagnostik.no_surat,surat_jaminan_penunjang_diagnostik.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
//                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa,surat_jaminan_penunjang_diagnostik.diagnosa_masuk,reg_periksa.kd_dokter,dokter.nm_dokter,surat_jaminan_penunjang_diagnostik.jadwal "+                  
//                     "from surat_jaminan_penunjang_diagnostik inner join reg_periksa on surat_jaminan_penunjang_diagnostik.no_rawat=reg_periksa.no_rawat "+
//                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on dokter.kd_dokter=reg_periksa.kd_dokter "+
//                     "where "+tgl+"and (no_surat like '%"+TCari.getText().trim()+"%' or surat_jaminan_penunjang_diagnostik.no_rawat like '%"+TCari.getText().trim()+"%' or "+
//                     "reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or  pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
//                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa like '%"+TCari.getText().trim()+"%' or surat_jaminan_penunjang_diagnostik.jadwal like '%"+TCari.getText().trim()+"%') "+                    
//                     "order by surat_jaminan_penunjang_diagnostik.no_surat",param);
//            }
//            
//        }
//        this.setCursor(Cursor.getDefaultCursor());        
if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();         
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs()); 
                String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
                String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
                param.put("logo2", logoPath);
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),6).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()));
                Valid.MyReportqry("rptSuratJaminanPenunjangDiagnostik.jasper","report","::[ Surat Keterangan Kewaspadaan Kesehatan ]::",
                "SELECT surat_jaminan_penunjang_diagnostik.no_surat,DATE_FORMAT( reg_periksa.tgl_registrasi, '%d-%m-%Y' ) AS tglregistrasi,"+
                "surat_jaminan_penunjang_diagnostik.diagnosa_masuk,dokter.nm_dokter,pasien.jk,pasien.nm_pasien,DATE_FORMAT( pasien.tgl_lahir, '%d-%m-%Y' ) AS tgl_lahir,"+
                "pasien.tmp_lahir,pasien.no_rkm_medis,pasien.pekerjaan,dokter.kd_dokter,concat( pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab ) AS alamat,"+
                "surat_jaminan_penunjang_diagnostik.jadwal,pasien.no_peserta AS nobpjs "+
                "FROM surat_jaminan_penunjang_diagnostik INNER JOIN reg_periksa INNER JOIN pasien INNER JOIN dokter INNER JOIN kelurahan INNER JOIN kecamatan "+
                "INNER JOIN kabupaten ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "+
                "AND reg_periksa.kd_dokter = dokter.kd_dokter "+
                "AND pasien.kd_kel = kelurahan.kd_kel "+
                "AND pasien.kd_kec = kecamatan.kd_kec "+
                "AND pasien.kd_kab = kabupaten.kd_kab "+
                "AND reg_periksa.no_rawat = surat_jaminan_penunjang_diagnostik.no_rawat "+
                "WHERE reg_periksa.no_rawat='"+TNoRw.getText()+"' ",param);
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

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
       isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void MnCetakSJPDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSJPDActionPerformed
       if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();         
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs()); 
                String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
                String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
                param.put("logo2", logoPath);
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),6).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()));
                Valid.MyReportqry("rptSuratJaminanPenunjangDiagnostik.jasper","report","::[ Surat Keterangan Kewaspadaan Kesehatan ]::",
                "SELECT surat_jaminan_penunjang_diagnostik.no_surat,DATE_FORMAT( reg_periksa.tgl_registrasi, '%d-%m-%Y' ) AS tglregistrasi,"+
                "surat_jaminan_penunjang_diagnostik.diagnosa_masuk,dokter.nm_dokter,pasien.jk,pasien.nm_pasien,DATE_FORMAT( pasien.tgl_lahir, '%d-%m-%Y' ) AS tgl_lahir,"+
                "pasien.tmp_lahir,pasien.no_rkm_medis,pasien.pekerjaan,dokter.kd_dokter,concat( pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab ) AS alamat,"+
                "surat_jaminan_penunjang_diagnostik.jadwal,pasien.no_peserta AS nobpjs "+
                "FROM surat_jaminan_penunjang_diagnostik INNER JOIN reg_periksa INNER JOIN pasien INNER JOIN dokter INNER JOIN kelurahan INNER JOIN kecamatan "+
                "INNER JOIN kabupaten ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "+
                "AND reg_periksa.kd_dokter = dokter.kd_dokter "+
                "AND pasien.kd_kel = kelurahan.kd_kel "+
                "AND pasien.kd_kec = kecamatan.kd_kec "+
                "AND pasien.kd_kab = kabupaten.kd_kab "+
                "AND reg_periksa.no_rawat = surat_jaminan_penunjang_diagnostik.no_rawat "+
                "WHERE reg_periksa.no_rawat='"+TNoRw.getText()+"' ",param);
                this.setCursor(Cursor.getDefaultCursor());  
       }
    }//GEN-LAST:event_MnCetakSJPDActionPerformed

    private void DTPCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DTPCari1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari1ActionPerformed

    private void DTPCari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DTPCari2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari2ActionPerformed

    private void JadwalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JadwalKeyPressed
        Valid.pindah(evt,diagnosa_masuk,BtnSimpan);
    }//GEN-LAST:event_JadwalKeyPressed

    private void diagnosa_masukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosa_masukKeyPressed
        Valid.pindah(evt,NoSurat,Jadwal);
    }//GEN-LAST:event_diagnosa_masukKeyPressed

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBerkasDigitalBtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(!tbObat.getValueAt(tbObat.getSelectedRow(),1).toString().equals("")){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgBerkasRawat berkas=new DlgBerkasRawat(null,true);
                    berkas.setJudul("::[ Berkas Digital Perawatan ]::","berkasrawat/pages");
                    try {
                        if(akses.gethapus_berkas_digital_perawatan()==true){
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                        }else{
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2nonhapus.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                        }
                    } catch (Exception ex) {
                        System.out.println("Notifikasi : "+ex);
                    }

                    berkas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    berkas.setLocationRelativeTo(internalFrame1);
                    berkas.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppBerkasDigitalBtnPrintActionPerformed

    private void MnCetakSJPDTTDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSJPDTTDActionPerformed
           if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();         
                param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",KdDokter.getText()));            
                param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs()); 
                String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
                String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
                param.put("logo2", logoPath);
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),6).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()));
                Valid.MyReportqry("rptSuratJaminanPenunjangDiagnostikTTD.jasper","report","::[ Surat Keterangan Kewaspadaan Kesehatan ]::",
                "SELECT surat_jaminan_penunjang_diagnostik.no_surat,DATE_FORMAT( reg_periksa.tgl_registrasi, '%d-%m-%Y' ) AS tglregistrasi,"+
                "surat_jaminan_penunjang_diagnostik.diagnosa_masuk,dokter.nm_dokter,pasien.jk,pasien.nm_pasien,DATE_FORMAT( pasien.tgl_lahir, '%d-%m-%Y' ) AS tgl_lahir,"+
                "pasien.tmp_lahir,pasien.no_rkm_medis,pasien.pekerjaan,dokter.kd_dokter,concat( pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab ) AS alamat,"+
                "surat_jaminan_penunjang_diagnostik.jadwal,pasien.no_peserta AS nobpjs "+
                "FROM surat_jaminan_penunjang_diagnostik INNER JOIN reg_periksa INNER JOIN pasien INNER JOIN dokter INNER JOIN kelurahan INNER JOIN kecamatan "+
                "INNER JOIN kabupaten ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "+
                "AND reg_periksa.kd_dokter = dokter.kd_dokter "+
                "AND pasien.kd_kel = kelurahan.kd_kel "+
                "AND pasien.kd_kec = kecamatan.kd_kec "+
                "AND pasien.kd_kab = kabupaten.kd_kab "+
                "AND reg_periksa.no_rawat = surat_jaminan_penunjang_diagnostik.no_rawat "+
                "WHERE reg_periksa.no_rawat='"+TNoRw.getText()+"' ",param);
                this.setCursor(Cursor.getDefaultCursor());  
       }
    }//GEN-LAST:event_MnCetakSJPDTTDActionPerformed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        Valid.pindah(evt,TCari,diagnosa_masuk);
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,TCari,diagnosa_masuk);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());

        FileName = timestamp + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().replace("/", "") + "_ProtokolHemodialisa";

        CreatePDF(FileName);
        ConvertPDFtoJPG(FileName);
        UploadJPG(FileName, "berkasrawat/pages/upload/");
        HapusJPG();

        ppBerkasDigitalBtnPrintActionPerformed(evt);
    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratJaminanPenunjangDiagnostik dialog = new SuratJaminanPenunjangDiagnostik(new javax.swing.JFrame(), true);
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
    private widget.Button BtnUpload;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Jadwal;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private javax.swing.JMenuItem MnCetakSJPD;
    private javax.swing.JMenuItem MnCetakSJPDTTD;
    private widget.TextBox NamaDokter;
    private widget.TextBox NoSurat;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalPeriksa;
    private widget.TextBox diagnosa_masuk;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel19;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label label14;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBerkasDigital;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            tgl=" surat_jaminan_penunjang_diagnostik.tanggalperiksa between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                     "select surat_jaminan_penunjang_diagnostik.no_surat,surat_jaminan_penunjang_diagnostik.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa,surat_jaminan_penunjang_diagnostik.diagnosa_masuk,reg_periksa.kd_dokter,dokter.nm_dokter,surat_jaminan_penunjang_diagnostik.jadwal "+                  
                     "from surat_jaminan_penunjang_diagnostik inner join reg_periksa on surat_jaminan_penunjang_diagnostik.no_rawat=reg_periksa.no_rawat "+
                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on dokter.kd_dokter=reg_periksa.kd_dokter "+
                     "where "+tgl+"order by surat_jaminan_penunjang_diagnostik.no_surat");
            }else{
                ps=koneksi.prepareStatement(
                    "select surat_jaminan_penunjang_diagnostik.no_surat,surat_jaminan_penunjang_diagnostik.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa,surat_jaminan_penunjang_diagnostik.diagnosa_masuk,reg_periksa.kd_dokter,dokter.nm_dokter,surat_jaminan_penunjang_diagnostik.jadwal "+                  
                     "from surat_jaminan_penunjang_diagnostik inner join reg_periksa on surat_jaminan_penunjang_diagnostik.no_rawat=reg_periksa.no_rawat "+
                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on dokter.kd_dokter=reg_periksa.kd_dokter "+
                     "where "+tgl+"and (no_surat like '%"+TCari.getText().trim()+"%' or surat_jaminan_penunjang_diagnostik.no_rawat like '%"+TCari.getText().trim()+"%' or "+
                     "reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or  pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                     "surat_jaminan_penunjang_diagnostik.tanggalperiksa like '%"+TCari.getText().trim()+"%' or surat_jaminan_penunjang_diagnostik.jadwal like '%"+TCari.getText().trim()+"%') "+                    
                     "order by surat_jaminan_penunjang_diagnostik.no_surat");
            }
                
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString(1),rs.getString(2),rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getString(6),
                        rs.getString(7),rs.getString(8),rs.getString(9)                          
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        TNoRw.setText("");
        TNoRM.setText("");
        TPasien.setText("");
        NoSurat.setText("");
        Jadwal.setText("");
        diagnosa_masuk.setText("");
        TanggalPeriksa.setDate(new Date());
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(surat_jaminan_penunjang_diagnostik.no_surat,3),signed)),0) from surat_jaminan_penunjang_diagnostik where surat_jaminan_penunjang_diagnostik.tanggalperiksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"' ",
                "SKK"+TanggalPeriksa.getSelectedItem().toString().substring(6,10)+TanggalPeriksa.getSelectedItem().toString().substring(3,5)+TanggalPeriksa.getSelectedItem().toString().substring(0,2),3,NoSurat); 
        diagnosa_masuk.requestFocus();
    }

 
    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            NoSurat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            Valid.SetTgl(TanggalPeriksa,tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            diagnosa_masuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());  
            KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            Jadwal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());      
            
        }
    }

    private void isRawat() {
         Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='"+TNoRw.getText()+"' ",TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }
    
    public void setNoRm(String norwt,String norm,String pasien, Date tgl1, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);        
        DTPCari1.setDate(tgl1);
        DTPCari2.setDate(tgl2);
        TNoRM.setText(norm);
        TPasien.setText(pasien);
        ChkInput.setSelected(true);
        isForm();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,150));
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
    
private void CreatePDF(String FileName) {
           if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();         
                param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",KdDokter.getText()));            
                param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs()); 
                String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
                String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
                param.put("logo2", logoPath);
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),6).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()));
                Valid.MyReportPDFqryUpload("rptSuratDosisHemofilia.jasper","report","::[ Surat Keterangan Kewaspadaan Kesehatan ]::",
                "SELECT surat_dosis_hemofilia.no_surat,DATE_FORMAT( reg_periksa.tgl_registrasi, '%d %M %Y' ) AS tglregistrasi,"+
                "surat_dosis_hemofilia.dosis_perhitungan,dokter.nm_dokter,pasien.nm_pasien,"+
                "pasien.no_rkm_medis,dokter.kd_dokter,"+
                "surat_dosis_hemofilia.on_demand,surat_dosis_hemofilia.evaluasi,surat_dosis_hemofilia.kadar_faktor,surat_dosis_hemofilia.berat_badan "+
                "FROM surat_dosis_hemofilia "+
                "INNER JOIN reg_periksa "+                
                "INNER JOIN dokter "+
                "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "+
                "AND reg_periksa.kd_dokter = dokter.kd_dokter "+                
                "AND reg_periksa.no_rawat = surat_dosis_hemofilia.no_rawat "+
                "WHERE reg_periksa.no_rawat='"+TNoRw.getText()+"' ",FileName, param);
                this.setCursor(Cursor.getDefaultCursor());  
       }

//    if (tabMode.getRowCount() == 0) {
//        JOptionPane.showMessageDialog(null, "Maaf, table masih kosong...!!!!");
//        TCari.requestFocus();
//    } else {
//        if (tbObat.getSelectedRow() != -1) {
//            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//            Map<String, Object> param = new HashMap<>();
//            param.put("kd_dokter", Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", KdDokter.getText()));            
//            param.put("ttd", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/tandatangandokter/pages/upload/" + KdDokter.getText() + ".png");
//            param.put("namars", akses.getnamars());
//            param.put("alamatrs", akses.getalamatrs());
//            param.put("kotars", akses.getkabupatenrs());
//            param.put("propinsirs", akses.getpropinsirs());
//            param.put("kontakrs", akses.getkontakrs());
//            param.put("emailrs", akses.getemailrs());
//
//            // Menentukan lokasi logo
//            String projectDir = System.getProperty("user.dir");
//            String logoPath = projectDir + "/setting/logo2.png";
//            param.put("logo2", logoPath);
//
//            // Mengambil data sidik jari pegawai dari database
//            String finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", 
//                                           tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
//
//            // Format tanda tangan digital
//            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() +
//                        "\nDitandatangani secara elektronik oleh " + tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString() +
//                        "\nID " + (finger.equals("") ? tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString() : finger) +
//                        "\n" + Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString()));
//
//            // Nama file laporan
//            String reportFile = "rptSuratDosisHemofilia.jasper";
//
//            // Query untuk mendapatkan data dari tabel surat_dosis_hemofilia
//            String query = "SELECT surat_dosis_hemofilia.no_surat, DATE_FORMAT(reg_periksa.tgl_registrasi, '%d %M %Y') AS tglregistrasi, " +
//                           "surat_dosis_hemofilia.dosis_perhitungan, dokter.nm_dokter, pasien.nm_pasien, " +
//                           "pasien.no_rkm_medis, dokter.kd_dokter, surat_dosis_hemofilia.on_demand, " +
//                           "surat_dosis_hemofilia.evaluasi, surat_dosis_hemofilia.kadar_faktor, surat_dosis_hemofilia.berat_badan " +
//                           "FROM surat_dosis_hemofilia " +
//                           "INNER JOIN reg_periksa " +                
//                           "INNER JOIN dokter " +
//                           "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
//                           "AND reg_periksa.kd_dokter = dokter.kd_dokter " +                
//                           "AND reg_periksa.no_rawat = surat_dosis_hemofilia.no_rawat " +
//                           "WHERE reg_periksa.no_rawat='" + TNoRw.getText() + "'";
//
//            // Membuat PDF dari laporan Surat Dosis Hemofilia
//            Valid.MyReportPDFqryUpload(reportFile, "report", "::[ Surat Keterangan Kewaspadaan Kesehatan ]::", query, FileName, param);
//
//            this.setCursor(Cursor.getDefaultCursor());
//        } else {
//            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data pasien terlebih dahulu...!!!!");
//        }
//    }
}

private void UploadPDF(String FileName, String docpath) {
    try {
        File file = new File("tmpPDF/" + FileName + ".pdf");
        byte[] data = FileUtils.readFileToByteArray(file);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/upload.php?doc=" + docpath);
        ByteArrayBody fileData = new ByteArrayBody(data, FileName + ".pdf");
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("file", fileData);
        postRequest.setEntity(reqEntity);
        httpClient.execute(postRequest);

        // Menyimpan ke database
        boolean uploadSuccess = false;
        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%SO Digital%'");
        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".pdf'") > 0) {
            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf", "pages/upload/" + FileName + ".pdf"
            });
        } else {
            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf"
            });
        }

        // Menampilkan notifikasi
        if (uploadSuccess) {
            JOptionPane.showMessageDialog(null, "Upload berhasil!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Upload gagal disimpan ke database.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    } catch (Exception e) {
        System.out.println("Upload error: " + e);
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat upload: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }
}

private void HapusPDF() {
    File file = new File("tmpPDF");
    String[] myFiles;
    if (file.isDirectory()) {
        myFiles = file.list();
        for (int i = 0; i < myFiles.length; i++) {
            File myFile = new File(file, myFiles[i]);
            myFile.delete();
        }
    }
}

private void ConvertPDFtoJPG(String FileName) {
    try {
        // Pastikan file PDF ada
        File pdfFile = new File("tmpPDF/" + FileName + ".pdf");
        if (!pdfFile.exists()) {
            System.err.println("File PDF tidak ditemukan: " + pdfFile.getAbsolutePath());
            return;
        }

        // Load PDF
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();

        // Pastikan folder tmpJPG ada
        File jpgDir = new File("tmpJPG");
        if (!jpgDir.exists() && !jpgDir.mkdir()) {
            System.err.println("Gagal membuat folder tmpJPG.");
            document.close();
            return;
        }

        // Iterasi untuk setiap halaman PDF dan konversi ke JPG
        for (int page = 0; page < totalPages; page++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);
            
            // Tentukan nama file
            String fileName = totalPages == 1 ? FileName + ".jpg" : FileName + "_page_" + (page + 1) + ".jpg";
            File jpgFile = new File(jpgDir, fileName);
            
            // Simpan tiap halaman sebagai JPG
            ImageIO.write(image, "jpg", jpgFile);

            System.out.println("Konversi berhasil: " + jpgFile.getAbsolutePath());
        }

        // Tutup dokumen PDF
        document.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void UploadJPG(String FileName, String docpath) {
    try {
        File jpgDir = new File("tmpJPG");
        if (!jpgDir.exists()) {
            System.err.println("Folder tmpJPG tidak ditemukan.");
            return;
        }

        // Cari semua file JPG untuk FileName
        File[] jpgFiles = jpgDir.listFiles((dir, name) -> 
            (name.equals(FileName + ".jpg") || (name.startsWith(FileName + "_page_") && name.endsWith(".jpg")))
        );
        
        if (jpgFiles == null || jpgFiles.length == 0) {
            System.err.println("Tidak ada file JPG ditemukan untuk di-upload.");
            return;
        }

        HttpClient httpClient = new DefaultHttpClient();

        for (File jpgFile : jpgFiles) {
            byte[] data = FileUtils.readFileToByteArray(jpgFile);
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/upload.php?doc=" + docpath);
            ByteArrayBody fileData = new ByteArrayBody(data, jpgFile.getName());
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileData);
            postRequest.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(postRequest);
            try {
                // Simpan ke database setiap file yang di-upload
                boolean uploadSuccess = false;
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Protokol Hemodialisa%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    System.out.println("Upload berhasil: " + jpgFile.getName());
                } else {
                    System.err.println("Upload gagal disimpan ke database: " + jpgFile.getName());
                }
            } finally {
                // Pastikan untuk melepaskan koneksi setelah digunakan
                if (response.getEntity() != null) {
                    EntityUtils.consume(response.getEntity());
                }
            }
        }

        JOptionPane.showMessageDialog(null, "Semua file berhasil di-upload!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
        System.out.println("Upload error: " + e);
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat upload: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

}

private void HapusJPG() {
    File file = new File("tmpJPG");
    String[] myFiles;
    if (file.isDirectory()) {
        myFiles = file.list();
        for (int i = 0; i < myFiles.length; i++) {
            File myFile = new File(file, myFiles[i]);
            myFile.delete();
        }
    }
}        
}



