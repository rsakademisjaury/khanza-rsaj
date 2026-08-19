/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laporan;

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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import laporan.ResumeSyncUtil;

/**
 *
 * @author khanzamedia
 */
public class PanelDiagnosa extends widget.panelisi {
    private final DefaultTableModel TabModeDiagnosaPasien,tabModeDiagnosa,tabModeProsedur,TabModeTindakanPasien;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement pspenyakit,psdiagnosapasien,psprosedur,pstindakanpasien;
    private ResultSet rs;
    private int jml=0,i=0,index=0;
    private String[] kode,nama,ciripny,keterangan,kategori,cirium,kode2,panjang,pendek;
    private boolean[] pilih;
    public String norawat="",status="",norm="",tanggal1="",tanggal2="",keyword="";
    private TukarPrioritasListener tukarListener;
    private String noRawat1 = "", kdPenyakit1 = "", prioritas1 = "";
    private String noRawat2 = "", kdPenyakit2 = "", prioritas2 = "";       
    private int jumlahDipilih = 0;    
    private String status1 = null, status2 = null;  // “Ralan” / “Ranap”

    
    /**
     * Creates new form panelDiagnosa
     */
    public PanelDiagnosa() {
        initComponents();
        TabModeDiagnosaPasien=new DefaultTableModel(null,new Object[]{
            "P",
            "Tgl.Rawat",
            "No.Rawat",
            "No.RM",
            "Nama Pasien",
            "Kode",
            "Nama Penyakit",
            "Status",
            "Kasus",
            "Prioritas",
            "Tgl. Lahir",
            "NIK",
            "Alamat"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class,
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class,
                java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDiagnosaPasien.setModel(TabModeDiagnosaPasien);
        tbDiagnosaPasien.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDiagnosaPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 13; i++) {
            TableColumn column = tbDiagnosaPasien.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(25);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(110);
            }else if(i==3){
                column.setPreferredWidth(50);
            }else if(i==4){
                column.setPreferredWidth(160);
            }else if(i==5){
                column.setPreferredWidth(50);
            }else if(i==6){
                column.setPreferredWidth(350);
            }else if(i==7){
                column.setPreferredWidth(50);
            }else if(i==8){
                column.setPreferredWidth(50);
            }else if(i==9){
                column.setPreferredWidth(55);    
            }else if(i==10){
                column.setPreferredWidth(70);     
            }else if(i==11){
                column.setPreferredWidth(105);     
            }else if(i==12){
                column.setPreferredWidth(300);     
            }
        }
        tbDiagnosaPasien.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDiagnosa=new DefaultTableModel(null,new Object[]{
            "P","Kode","Nama Penyakit","Ciri-ciri Penyakit","Keterangan","Ktg.Penyakit","Ciri-ciri Umum"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, 
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDiagnosa.setModel(tabModeDiagnosa);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbDiagnosa.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDiagnosa.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i= 0; i < 7; i++) {
            TableColumn column = tbDiagnosa.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(40);
            }else if(i==2){
                column.setPreferredWidth(280);
            }else if(i==3){
                column.setPreferredWidth(285);
            }else if(i==4){
                column.setPreferredWidth(75);
            }else if(i==5){
                column.setPreferredWidth(75);
            }else if(i==6){
                column.setPreferredWidth(75);
            }
        }
        tbDiagnosa.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeProsedur=new DefaultTableModel(null,new Object[]{
            "P","Kode","Deskripsi Panjang","Deskripsi Pendek"}){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbProsedur.setModel(tabModeProsedur);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbProsedur.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbProsedur.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 4; i++) {
            TableColumn column = tbProsedur.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(50);
            }else if(i==2){
                column.setPreferredWidth(350);
            }else if(i==3){
                column.setPreferredWidth(350);
            }
        }
        tbProsedur.setDefaultRenderer(Object.class, new WarnaTable());
        
        TabModeTindakanPasien=new DefaultTableModel(null,new Object[]{
            "P",
            "Tgl.Rawat",
            "No.Rawat",
            "No.R.M.",
            "Nama Pasien",
            "Kode",
            "Nama Prosedur",
            "Status",
            "Prioritas"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class, 
                java.lang.Object.class,
                java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbTindakanPasien.setModel(TabModeTindakanPasien);
        tbTindakanPasien.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTindakanPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 8; i++) {
            TableColumn column = tbTindakanPasien.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(80);
            }else if(i==2){
                column.setPreferredWidth(110);
            }else if(i==3){
                column.setPreferredWidth(70);
            }else if(i==4){
                column.setPreferredWidth(160);
            }else if(i==5){
                column.setPreferredWidth(50);
            }else if(i==6){
                column.setPreferredWidth(300);
            }else if(i==7){
                column.setPreferredWidth(50);
            }
        }
        tbTindakanPasien.setDefaultRenderer(Object.class, new WarnaTable());
           
        Diagnosa.setDocument(new batasInput((byte)100).getKata(Diagnosa));
        Prosedur.setDocument(new batasInput((byte)100).getKata(Prosedur));
        
        if(koneksiDB.CARICEPAT().equals("aktif")){
            Diagnosa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(Diagnosa.getText().length()>2){
                        tampildiagnosa();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(Diagnosa.getText().length()>2){
                        tampildiagnosa();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(Diagnosa.getText().length()>2){
                        tampildiagnosa();
                    }
                }
            });
        } 
        
        if(koneksiDB.CARICEPAT().equals("aktif")){
            Prosedur.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(Prosedur.getText().length()>2){
                        tampilprosedure();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(Prosedur.getText().length()>2){
                        tampilprosedure();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(Prosedur.getText().length()>2){
                        tampilprosedure();
                    }
                }
            });
        } 
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
        MnStatusBaru = new javax.swing.JMenuItem();
        MnStatusLama = new javax.swing.JMenuItem();
        btnTukarPrioritas = new javax.swing.JMenuItem();
        TabRawat = new javax.swing.JTabbedPane();
        ScrollInput = new widget.ScrollPane();
        FormData = new widget.PanelBiasa();
        jLabel13 = new widget.Label();
        Diagnosa = new widget.TextBox();
        BtnCariPenyakit = new widget.Button();
        btnTambahPenyakit = new widget.Button();
        Scroll1 = new widget.ScrollPane();
        tbDiagnosa = new widget.Table();
        jLabel15 = new widget.Label();
        Prosedur = new widget.TextBox();
        btnTambahProsedur = new widget.Button();
        BtnCariProsedur = new widget.Button();
        Scroll2 = new widget.ScrollPane();
        tbProsedur = new widget.Table();
        cbKode = new javax.swing.JCheckBox();
        cbNama = new javax.swing.JCheckBox();
        cbKodeP = new javax.swing.JCheckBox();
        cbNamaP = new javax.swing.JCheckBox();
        internalFrame2 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbDiagnosaPasien = new widget.Table();
        internalFrame3 = new widget.InternalFrame();
        Scroll3 = new widget.ScrollPane();
        tbTindakanPasien = new widget.Table();

        MnStatusBaru.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnStatusBaru.setForeground(new java.awt.Color(50, 50, 50));
        MnStatusBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnStatusBaru.setText("Status Penyakit Baru");
        MnStatusBaru.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnStatusBaru.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnStatusBaru.setPreferredSize(new java.awt.Dimension(170, 26));
        MnStatusBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnStatusBaruActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnStatusBaru);

        MnStatusLama.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnStatusLama.setForeground(new java.awt.Color(50, 50, 50));
        MnStatusLama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnStatusLama.setText("Status Penyakit Lama");
        MnStatusLama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnStatusLama.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnStatusLama.setIconTextGap(5);
        MnStatusLama.setPreferredSize(new java.awt.Dimension(170, 26));
        MnStatusLama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnStatusLamaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnStatusLama);

        btnTukarPrioritas.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        btnTukarPrioritas.setForeground(new java.awt.Color(50, 50, 50));
        btnTukarPrioritas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        btnTukarPrioritas.setText("Tukar Prioritas");
        btnTukarPrioritas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTukarPrioritas.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnTukarPrioritas.setPreferredSize(new java.awt.Dimension(170, 26));
        btnTukarPrioritas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTukarPrioritasActionPerformed(evt);
            }
        });
        jPopupMenu1.add(btnTukarPrioritas);

        setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        ScrollInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        ScrollInput.setOpaque(true);

        FormData.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        FormData.setPreferredSize(new java.awt.Dimension(865, 417));
        FormData.setLayout(null);

        jLabel13.setText("Diagnosa :");
        FormData.add(jLabel13);
        jLabel13.setBounds(190, 10, 60, 23);

        Diagnosa.setHighlighter(null);
        Diagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaKeyPressed(evt);
            }
        });
        FormData.add(Diagnosa);
        Diagnosa.setBounds(261, 10, 420, 23);

        BtnCariPenyakit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariPenyakit.setMnemonic('1');
        BtnCariPenyakit.setToolTipText("Alt+1");
        BtnCariPenyakit.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariPenyakit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariPenyakitActionPerformed(evt);
            }
        });
        FormData.add(BtnCariPenyakit);
        BtnCariPenyakit.setBounds(690, 10, 28, 23);

        btnTambahPenyakit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnTambahPenyakit.setMnemonic('2');
        btnTambahPenyakit.setToolTipText("Alt+2");
        btnTambahPenyakit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahPenyakitActionPerformed(evt);
            }
        });
        FormData.add(btnTambahPenyakit);
        btnTambahPenyakit.setBounds(720, 10, 28, 23);

        Scroll1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)));
        Scroll1.setOpaque(true);
        Scroll1.setViewportView(tbDiagnosa);

        FormData.add(Scroll1);
        Scroll1.setBounds(11, 40, 740, 440);

        jLabel15.setText("Prosedur :");
        FormData.add(jLabel15);
        jLabel15.setBounds(950, 10, 60, 23);

        Prosedur.setHighlighter(null);
        Prosedur.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ProsedurKeyPressed(evt);
            }
        });
        FormData.add(Prosedur);
        Prosedur.setBounds(1020, 10, 410, 23);

        btnTambahProsedur.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnTambahProsedur.setMnemonic('2');
        btnTambahProsedur.setToolTipText("Alt+2");
        btnTambahProsedur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahProsedurActionPerformed(evt);
            }
        });
        FormData.add(btnTambahProsedur);
        btnTambahProsedur.setBounds(1470, 10, 28, 23);

        BtnCariProsedur.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariProsedur.setMnemonic('1');
        BtnCariProsedur.setToolTipText("Alt+1");
        BtnCariProsedur.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariProsedur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariProsedurActionPerformed(evt);
            }
        });
        FormData.add(BtnCariProsedur);
        BtnCariProsedur.setBounds(1440, 10, 28, 23);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)));
        Scroll2.setOpaque(true);

        tbProsedur.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        Scroll2.setViewportView(tbProsedur);

        FormData.add(Scroll2);
        Scroll2.setBounds(765, 40, 740, 440);

        cbKode.setBackground(new java.awt.Color(255, 255, 255));
        cbKode.setSelected(true);
        cbKode.setText("Kode");
        cbKode.setIconTextGap(8);
        cbKode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbKodeActionPerformed(evt);
            }
        });
        FormData.add(cbKode);
        cbKode.setBounds(10, 10, 60, 25);

        cbNama.setBackground(new java.awt.Color(255, 255, 255));
        cbNama.setSelected(true);
        cbNama.setText("Nama Penyakit");
        cbNama.setIconTextGap(8);
        cbNama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbNamaActionPerformed(evt);
            }
        });
        FormData.add(cbNama);
        cbNama.setBounds(70, 10, 120, 25);

        cbKodeP.setBackground(new java.awt.Color(255, 255, 255));
        cbKodeP.setSelected(true);
        cbKodeP.setText("Kode");
        cbKodeP.setIconTextGap(8);
        cbKodeP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbKodePActionPerformed(evt);
            }
        });
        FormData.add(cbKodeP);
        cbKodeP.setBounds(770, 10, 60, 25);

        cbNamaP.setBackground(new java.awt.Color(255, 255, 255));
        cbNamaP.setSelected(true);
        cbNamaP.setText("Nama Prosedur");
        cbNamaP.setIconTextGap(8);
        cbNamaP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbNamaPActionPerformed(evt);
            }
        });
        FormData.add(cbNamaP);
        cbNamaP.setBounds(830, 10, 120, 25);

        ScrollInput.setViewportView(FormData);

        TabRawat.addTab("Input Data", ScrollInput);

        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setOpaque(true);

        tbDiagnosaPasien.setAutoCreateRowSorter(true);
        tbDiagnosaPasien.setComponentPopupMenu(jPopupMenu1);
        tbDiagnosaPasien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDiagnosaPasienMouseClicked(evt);
            }
        });
        Scroll.setViewportView(tbDiagnosaPasien);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Data Diagnosa", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll3.setOpaque(true);

        tbTindakanPasien.setAutoCreateRowSorter(true);
        tbTindakanPasien.setComponentPopupMenu(jPopupMenu1);
        Scroll3.setViewportView(tbTindakanPasien);

        internalFrame3.add(Scroll3, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Data Prosedur", internalFrame3);

        add(TabRawat, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void DiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampildiagnosa();            
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            if(akses.getpenyakit()==true){
                btnTambahPenyakitActionPerformed(null);
            }
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbDiagnosa.requestFocus();
        }
    }//GEN-LAST:event_DiagnosaKeyPressed

    private void BtnCariPenyakitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariPenyakitActionPerformed
        tampildiagnosa();
    }//GEN-LAST:event_BtnCariPenyakitActionPerformed

    private void btnTambahPenyakitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahPenyakitActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgPenyakit tariflab=new DlgPenyakit(null,false);
        tariflab.emptTeks();
        tariflab.isCek();
        tariflab.setSize(this.getWidth(),this.getHeight()+100);
        tariflab.setLocationRelativeTo(this);
        tariflab.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnTambahPenyakitActionPerformed

    private void ProsedurKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ProsedurKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilprosedure();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            if(akses.geticd9()==true){
                btnTambahProsedurActionPerformed(null);
            }
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbProsedur.requestFocus();
        }
    }//GEN-LAST:event_ProsedurKeyPressed

    private void btnTambahProsedurActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahProsedurActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgICD9 tariflab=new DlgICD9(null,false);
        tariflab.emptTeks();
        tariflab.isCek();
        tariflab.setSize(this.getWidth(),this.getHeight()+100);
        tariflab.setLocationRelativeTo(this);
        tariflab.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnTambahProsedurActionPerformed

    private void BtnCariProsedurActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariProsedurActionPerformed
        tampilprosedure();
    }//GEN-LAST:event_BtnCariProsedurActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        pilihTab();
    }//GEN-LAST:event_TabRawatMouseClicked

    private void MnStatusBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnStatusBaruActionPerformed
        if(norawat.equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            Sequel.queryu2("update diagnosa_pasien set status_penyakit='Baru' where no_rawat=? and kd_penyakit=?",2,new String[]{
                tbDiagnosaPasien.getValueAt(tbDiagnosaPasien.getSelectedRow(),2).toString(),tbDiagnosaPasien.getValueAt(tbDiagnosaPasien.getSelectedRow(),5).toString()
            });
            tampil();
        }
    }//GEN-LAST:event_MnStatusBaruActionPerformed

    private void MnStatusLamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnStatusLamaActionPerformed
        if(norawat.equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            Sequel.queryu2("update diagnosa_pasien set status_penyakit='Lama' where no_rawat=? and kd_penyakit=?",2,new String[]{
                tbDiagnosaPasien.getValueAt(tbDiagnosaPasien.getSelectedRow(),2).toString(),tbDiagnosaPasien.getValueAt(tbDiagnosaPasien.getSelectedRow(),5).toString()
            });
            tampil();
        }
    }//GEN-LAST:event_MnStatusLamaActionPerformed

    private void btnTukarPrioritasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTukarPrioritasActionPerformed

    if (jumlahDipilih != 2) return;

    if (noRawat1 == null || kdPenyakit1 == null || prioritas1 == null || status1 == null ||
        noRawat2 == null || kdPenyakit2 == null || prioritas2 == null || status2 == null) {
        JOptionPane.showMessageDialog(null, "Pilih dua diagnosa yang valid.");
        return;
    }
    if (!noRawat1.equals(noRawat2)) {
        JOptionPane.showMessageDialog(null, "Hanya boleh tukar prioritas dalam kunjungan (no_rawat) yang sama.");
        return;
    }
    if (!status1.equalsIgnoreCase(status2)) {
        JOptionPane.showMessageDialog(null, "Status kedua diagnosa harus sama (Ralan/Ranap).");
        return;
    }
    if (prioritas1.equals(prioritas2)) {
        JOptionPane.showMessageDialog(null, "Prioritas sama, tidak perlu ditukar.");
        resetSwapState();
        return;
    }

    int konfirmasi = JOptionPane.showConfirmDialog(null,
            "Yakin ingin menukar prioritas diagnosa ini?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (konfirmasi != JOptionPane.YES_OPTION) return;

    Connection conn = null;
    boolean oldAuto = true;

    try {
        conn = koneksi; // koneksi global
        oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        final int p1 = Integer.parseInt(prioritas1);
        final int p2 = Integer.parseInt(prioritas2);
        final int TEMP = findSafeTempPrioritas(conn, noRawat1, status1);

        // 1) Baris A -> TEMP
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE diagnosa_pasien SET prioritas=? " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND kd_penyakit=? AND prioritas=?"
        )) {
            ps.setInt(1, TEMP);
            ps.setString(2, noRawat1);
            ps.setString(3, status1);
            ps.setString(4, kdPenyakit1);
            ps.setInt(5, p1);
            if (ps.executeUpdate() != 1) throw new RuntimeException("Baris A tidak ditemukan/berubah.");
        }

        // 2) Baris B -> prioritas1
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE diagnosa_pasien SET prioritas=? " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND kd_penyakit=? AND prioritas=?"
        )) {
            ps.setInt(1, p1);
            ps.setString(2, noRawat2);
            ps.setString(3, status2);
            ps.setString(4, kdPenyakit2);
            ps.setInt(5, p2);
            if (ps.executeUpdate() != 1) throw new RuntimeException("Baris B tidak ditemukan/berubah.");
        }

        // 3) Baris A (TEMP) -> prioritas2
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE diagnosa_pasien SET prioritas=? " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND kd_penyakit=? AND prioritas=?"
        )) {
            ps.setInt(1, p2);
            ps.setString(2, noRawat1);
            ps.setString(3, status1);
            ps.setString(4, kdPenyakit1);
            ps.setInt(5, TEMP);
            if (ps.executeUpdate() != 1) throw new RuntimeException("Baris A (TEMP) tidak ditemukan/berubah.");
        }

        // 4) Sinkron resume sesuai status
        ResumeSyncUtil.syncByStatus(conn, noRawat1, status1);

        conn.commit();
        tampil();          // refresh tabel
        resetSwapState();  // bereskan state UI
        JOptionPane.showMessageDialog(null, "Prioritas berhasil ditukar & resume tersinkron.");

    } catch (Exception ex) {
        try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
        JOptionPane.showMessageDialog(null, "Gagal menukar prioritas: " + ex.getMessage());
    } finally {
        try { if (conn != null) conn.setAutoCommit(oldAuto); } catch (Exception ignore) {}
    }
}

private void resetSwapState() {
    jumlahDipilih = 0;
    noRawat1 = noRawat2 = null;
    kdPenyakit1 = kdPenyakit2 = null;
    prioritas1 = prioritas2 = null;
    status1 = status2 = null;
    btnTukarPrioritas.setEnabled(false); 
    }//GEN-LAST:event_btnTukarPrioritasActionPerformed

    private void tbDiagnosaPasienMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDiagnosaPasienMouseClicked

    int row = tbDiagnosaPasien.getSelectedRow();
    if (row == -1) return;

    // ======= SESUAIKAN INDEX KOL0M TABEL KAMU =======
    final int COL_NORAWAT   = 2;
    final int COL_KODE      = 5;
    final int COL_STATUS    = 7;  // "Ralan"/"Ranap"
    final int COL_PRIORITAS = 9;  // 1,2,3,...
    // ================================================

    String noRawat   = tbDiagnosaPasien.getValueAt(row, COL_NORAWAT).toString();
    String kode      = tbDiagnosaPasien.getValueAt(row, COL_KODE).toString();
    String st        = tbDiagnosaPasien.getValueAt(row, COL_STATUS).toString();
    String prioritas = tbDiagnosaPasien.getValueAt(row, COL_PRIORITAS).toString();

    if (jumlahDipilih == 0) {
        noRawat1     = noRawat;
        kdPenyakit1  = kode;
        prioritas1   = prioritas;
        status1      = st;
        jumlahDipilih = 1;
        btnTukarPrioritas.setEnabled(false);
    } else if (jumlahDipilih == 1) {
        if (noRawat.equals(noRawat1) && kode.equals(kdPenyakit1)) {
            JOptionPane.showMessageDialog(null, "Pilih baris yang berbeda untuk ditukar.");
            return;
        }
        noRawat2     = noRawat;
        kdPenyakit2  = kode;
        prioritas2   = prioritas;
        status2      = st;
        jumlahDipilih = 2;
        btnTukarPrioritas.setEnabled(true);
    } else {
        resetSwapState();
        noRawat1     = noRawat;
        kdPenyakit1  = kode;
        prioritas1   = prioritas;
        status1      = st;
        jumlahDipilih = 1;
    }

    }//GEN-LAST:event_tbDiagnosaPasienMouseClicked

private int findSafeTempPrioritas(java.sql.Connection conn, String noRawat, String status) throws Exception {
    // Ambil nilai sementara yg pasti belum dipakai: MAX(prioritas)+1
    final String sql = "SELECT COALESCE(MAX(prioritas),0)+1 " +
                       "FROM diagnosa_pasien WHERE no_rawat=? AND LOWER(status)=LOWER(?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, noRawat);
        ps.setString(2, status);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int temp = rs.getInt(1);

                // Guard kalau kolom tinyint signed & sudah mendekati batas 127:
                // fallback ke 0 kalau temp terlalu besar (atau aturan internalmu memperbolehkan 0).
                if (temp > 120) temp = 0;

                return temp;
            }
        }
    }
    return 0; // fallback aman
}
    
    private void cbKodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKodeActionPerformed

    private void cbNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbNamaActionPerformed

    private void cbKodePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKodePActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKodePActionPerformed

    private void cbNamaPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbNamaPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbNamaPActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnCariPenyakit;
    private widget.Button BtnCariProsedur;
    public widget.TextBox Diagnosa;
    public widget.PanelBiasa FormData;
    private javax.swing.JMenuItem MnStatusBaru;
    private javax.swing.JMenuItem MnStatusLama;
    private widget.TextBox Prosedur;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.ScrollPane Scroll3;
    public widget.ScrollPane ScrollInput;
    public javax.swing.JTabbedPane TabRawat;
    public widget.Button btnTambahPenyakit;
    public widget.Button btnTambahProsedur;
    private javax.swing.JMenuItem btnTukarPrioritas;
    private javax.swing.JCheckBox cbKode;
    private javax.swing.JCheckBox cbKodeP;
    private javax.swing.JCheckBox cbNama;
    private javax.swing.JCheckBox cbNamaP;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel13;
    private widget.Label jLabel15;
    private javax.swing.JPopupMenu jPopupMenu1;
    public widget.Table tbDiagnosa;
    public widget.Table tbDiagnosaPasien;
    public widget.Table tbProsedur;
    public widget.Table tbTindakanPasien;
    // End of variables declaration//GEN-END:variables
    public void tampil() {
        Valid.tabelKosong(TabModeDiagnosaPasien);
        try{            
            psdiagnosapasien=koneksi.prepareStatement(
                    "select reg_periksa.tgl_registrasi,"
                            + "diagnosa_pasien.no_rawat,"
                            + "reg_periksa.no_rkm_medis,"
                            + "pasien.nm_pasien,"
                            + "diagnosa_pasien.kd_penyakit,"
                            + "penyakit.nm_penyakit,"
                            + "diagnosa_pasien.status,"
                            + "diagnosa_pasien.status_penyakit,"
                            + "diagnosa_pasien.prioritas, "
                            + "pasien.tgl_lahir, "
                            + "pasien.no_ktp, "
                            + "pasien.alamat "+
                    "from diagnosa_pasien inner join reg_periksa on diagnosa_pasien.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                    "where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.no_rkm_medis like ? "+
                    (keyword.trim().equals("")?"":"and (diagnosa_pasien.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                    "pasien.nm_pasien like ? or diagnosa_pasien.kd_penyakit like ? or penyakit.nm_penyakit like ? or "+
                    "diagnosa_pasien.status_penyakit like ? or diagnosa_pasien.status like ?)")+
                    "order by reg_periksa.tgl_registrasi,diagnosa_pasien.prioritas ");
            try {
                psdiagnosapasien.setString(1,tanggal1);
                psdiagnosapasien.setString(2,tanggal2);
                psdiagnosapasien.setString(3,"%"+norm+"%"); 
                if(!keyword.trim().equals("")){
                    psdiagnosapasien.setString(4,"%"+keyword+"%");         
                    psdiagnosapasien.setString(5,"%"+keyword+"%");         
                    psdiagnosapasien.setString(6,"%"+keyword+"%");         
                    psdiagnosapasien.setString(7,"%"+keyword+"%");         
                    psdiagnosapasien.setString(8,"%"+keyword+"%");         
                    psdiagnosapasien.setString(9,"%"+keyword+"%");          
                    psdiagnosapasien.setString(10,"%"+keyword+"%");   
                }
                    
                rs=psdiagnosapasien.executeQuery();
                while(rs.next()){
                    TabModeDiagnosaPasien.addRow(new Object[]{
                        false,rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(11),
                        rs.getString(12)
                    });
                }            
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(psdiagnosapasien!=null){
                    psdiagnosapasien.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public int getRecord(){
        if(TabRawat.getSelectedIndex()==0){
            i=0;
        }else if(TabRawat.getSelectedIndex()==1){
            i=tbDiagnosaPasien.getRowCount();
        }else if(TabRawat.getSelectedIndex()==2){
            i=tbTindakanPasien.getRowCount();
        }
        return i;
    }
    
    private void tampildiagnosa() {
//        try{
//            jml=0;
//            for(i=0;i<tbDiagnosa.getRowCount();i++){
//                if(tbDiagnosa.getValueAt(i,0).toString().equals("true")){
//                    jml++;
//                }
//            }
//
//            pilih=null;
//            pilih=new boolean[jml];
//            kode=null;
//            kode=new String[jml];
//            nama=null;
//            nama=new String[jml];
//            ciripny=null;
//            ciripny=new String[jml];
//            keterangan=null;
//            keterangan=new String[jml];
//            kategori=null;
//            kategori=new String[jml];
//            cirium=null;
//            cirium=new String[jml];
//
//            index=0; 
//            for(i=0;i<tbDiagnosa.getRowCount();i++){
//                if(tbDiagnosa.getValueAt(i,0).toString().equals("true")){
//                    pilih[index]=true;
//                    kode[index]=tbDiagnosa.getValueAt(i,1).toString();
//                    nama[index]=tbDiagnosa.getValueAt(i,2).toString();
//                    ciripny[index]=tbDiagnosa.getValueAt(i,3).toString();
//                    keterangan[index]=tbDiagnosa.getValueAt(i,4).toString();
//                    kategori[index]=tbDiagnosa.getValueAt(i,5).toString();
//                    cirium[index]=tbDiagnosa.getValueAt(i,6).toString();
//                    index++;
//                }
//            }
//
//            Valid.tabelKosong(tabModeDiagnosa);
//            for(i=0;i<jml;i++){
//                tabModeDiagnosa.addRow(new Object[] {pilih[i],kode[i],nama[i],ciripny[i],keterangan[i],kategori[i],cirium[i]});
//            }       
//
//            pspenyakit=koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit,penyakit.ciri_ciri,penyakit.keterangan, "+
//                    "kategori_penyakit.nm_kategori,kategori_penyakit.ciri_umum "+
//                    "from kategori_penyakit inner join penyakit "+
//                    "on penyakit.kd_ktg=kategori_penyakit.kd_ktg where  "+
//                    " penyakit.kd_penyakit like ? or "+
//                    " penyakit.nm_penyakit like ? or "+
//                    " penyakit.ciri_ciri like ? or "+
//                    " penyakit.keterangan like ? or "+
//                    " kategori_penyakit.nm_kategori like ? or "+
//                    " kategori_penyakit.ciri_umum like ? "+
//                    "order by penyakit.kd_penyakit  LIMIT 1000");
//            try {
//                pspenyakit.setString(1,"%"+Diagnosa.getText().trim()+"%");
//                pspenyakit.setString(2,"%"+Diagnosa.getText().trim()+"%");
//                pspenyakit.setString(3,"%"+Diagnosa.getText().trim()+"%");
//                pspenyakit.setString(4,"%"+Diagnosa.getText().trim()+"%");
//                pspenyakit.setString(5,"%"+Diagnosa.getText().trim()+"%");
//                pspenyakit.setString(6,"%"+Diagnosa.getText().trim()+"%");  
//                rs=pspenyakit.executeQuery();
//                while(rs.next()){
//                    tabModeDiagnosa.addRow(new Object[]{false,rs.getString(1),
//                                   rs.getString(2),
//                                   rs.getString(3),
//                                   rs.getString(4),
//                                   rs.getString(5),
//                                   rs.getString(6)});
//                } 
//            } catch (Exception e) {
//                System.out.println("Notifikasi : "+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(pspenyakit!=null){
//                    pspenyakit.close();
//                }
//            }           
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }

    try {
        jml = 0;
        for (i = 0; i < tbDiagnosa.getRowCount(); i++) {
            if (tbDiagnosa.getValueAt(i, 0).toString().equals("true")) {
                jml++;
            }
        }

        pilih = new boolean[jml];
        kode = new String[jml];
        nama = new String[jml];
        ciripny = new String[jml];
        keterangan = new String[jml];
        kategori = new String[jml];
        cirium = new String[jml];

        index = 0;
        for (i = 0; i < tbDiagnosa.getRowCount(); i++) {
            if (tbDiagnosa.getValueAt(i, 0).toString().equals("true")) {
                pilih[index] = true;
                kode[index] = tbDiagnosa.getValueAt(i, 1).toString();
                nama[index] = tbDiagnosa.getValueAt(i, 2).toString();
                ciripny[index] = tbDiagnosa.getValueAt(i, 3).toString();
                keterangan[index] = tbDiagnosa.getValueAt(i, 4).toString();
                kategori[index] = tbDiagnosa.getValueAt(i, 5).toString();
                cirium[index] = tbDiagnosa.getValueAt(i, 6).toString();
                index++;
            }
        }

        Valid.tabelKosong(tabModeDiagnosa);
        for (i = 0; i < jml; i++) {
            tabModeDiagnosa.addRow(new Object[]{pilih[i], kode[i], nama[i], ciripny[i], keterangan[i], kategori[i], cirium[i]});
        }

        String keyword = Diagnosa.getText().trim();
        String sql = "SELECT penyakit.kd_penyakit, penyakit.nm_penyakit, penyakit.ciri_ciri, penyakit.keterangan, " +
                     "kategori_penyakit.nm_kategori, kategori_penyakit.ciri_umum " +
                     "FROM kategori_penyakit INNER JOIN penyakit ON penyakit.kd_ktg = kategori_penyakit.kd_ktg ";

        if (cbKode.isSelected() && !cbNama.isSelected()) {
            sql += "WHERE penyakit.kd_penyakit LIKE ? ";
        } else if (!cbKode.isSelected() && cbNama.isSelected()) {
            sql += "WHERE penyakit.nm_penyakit LIKE ? ";
        } else if (cbKode.isSelected() && cbNama.isSelected()) {
            sql += "WHERE penyakit.kd_penyakit LIKE ? OR penyakit.nm_penyakit LIKE ? ";
        } else {
            sql += "LIMIT 1000"; // tanpa filter
        }

        sql += "ORDER BY penyakit.kd_penyakit LIMIT 1000";

        pspenyakit = koneksi.prepareStatement(sql);
        if (cbKode.isSelected() && !cbNama.isSelected()) {
            pspenyakit.setString(1, "%" + keyword + "%");
        } else if (!cbKode.isSelected() && cbNama.isSelected()) {
            pspenyakit.setString(1, "%" + keyword + "%");
        } else if (cbKode.isSelected() && cbNama.isSelected()) {
            pspenyakit.setString(1, "%" + keyword + "%");
            pspenyakit.setString(2, "%" + keyword + "%");
        }

        rs = pspenyakit.executeQuery();
        while (rs.next()) {
            tabModeDiagnosa.addRow(new Object[]{
                false,
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)
            });
        }
    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    } finally {
        try {
            if (rs != null) rs.close();
            if (pspenyakit != null) pspenyakit.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    }
    
    private void tampilprosedure() {
//        try{
//            jml=0;
//            for(i=0;i<tbProsedur.getRowCount();i++){
//                if(tbProsedur.getValueAt(i,0).toString().equals("true")){
//                    jml++;
//                }
//            }
//
//            pilih=null;
//            pilih=new boolean[jml];
//            kode2=null;
//            kode2=new String[jml];
//            panjang=null;
//            panjang=new String[jml];
//            pendek=null;
//            pendek=new String[jml];
//
//            index=0; 
//            for(i=0;i<tbProsedur.getRowCount();i++){
//                if(tbProsedur.getValueAt(i,0).toString().equals("true")){
//                    pilih[index]=true;
//                    kode2[index]=tbProsedur.getValueAt(i,1).toString();
//                    panjang[index]=tbProsedur.getValueAt(i,2).toString();
//                    pendek[index]=tbProsedur.getValueAt(i,3).toString();
//                    index++;
//                }
//            }
//
//            Valid.tabelKosong(tabModeProsedur);
//            for(i=0;i<jml;i++){
//                tabModeProsedur.addRow(new Object[] {pilih[i],kode2[i],panjang[i],pendek[i]});
//            }
//            
//            psprosedur=koneksi.prepareStatement("select * from icd9 where kode like ? or "+
//                    " deskripsi_panjang like ? or  deskripsi_pendek like ? order by kode");
//            try{
//                psprosedur.setString(1,"%"+Prosedur.getText().trim()+"%");
//                psprosedur.setString(2,"%"+Prosedur.getText().trim()+"%");
//                psprosedur.setString(3,"%"+Prosedur.getText().trim()+"%");
//                rs=psprosedur.executeQuery();
//                while(rs.next()){
//                    tabModeProsedur.addRow(new Object[]{
//                        false,rs.getString(1),rs.getString(2),rs.getString(3)});
//                }
//            }catch(Exception ex){
//                System.out.println(ex);
//            }finally{
//                if(rs != null){
//                    rs.close();
//                }
//                if(psprosedur != null){
//                    psprosedur.close();
//                }
//            }
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }

    try {
        jml = 0;
        for (i = 0; i < tbProsedur.getRowCount(); i++) {
            if (tbProsedur.getValueAt(i, 0).toString().equals("true")) {
                jml++;
            }
        }

        pilih = new boolean[jml];
        kode2 = new String[jml];
        panjang = new String[jml];
        pendek = new String[jml];

        index = 0;
        for (i = 0; i < tbProsedur.getRowCount(); i++) {
            if (tbProsedur.getValueAt(i, 0).toString().equals("true")) {
                pilih[index] = true;
                kode2[index] = tbProsedur.getValueAt(i, 1).toString();
                panjang[index] = tbProsedur.getValueAt(i, 2).toString();
                pendek[index] = tbProsedur.getValueAt(i, 3).toString();
                index++;
            }
        }

        Valid.tabelKosong(tabModeProsedur);
        for (i = 0; i < jml; i++) {
            tabModeProsedur.addRow(new Object[]{pilih[i], kode2[i], panjang[i], pendek[i]});
        }

        String keyword = Prosedur.getText().trim();
        String sql = "SELECT * FROM icd9 ";
        if (cbKodeP.isSelected() && !cbNamaP.isSelected()) {
            sql += "WHERE kode LIKE ? ";
        } else if (!cbKodeP.isSelected() && cbNamaP.isSelected()) {
            sql += "WHERE deskripsi_panjang LIKE ? ";
        } else if (cbKodeP.isSelected() && cbNamaP.isSelected()) {
            sql += "WHERE kode LIKE ? OR deskripsi_panjang LIKE ? ";
        } else {
            sql += "LIMIT 1000";
        }

        sql += "ORDER BY kode";

        psprosedur = koneksi.prepareStatement(sql);
        if (cbKodeP.isSelected() && !cbNamaP.isSelected()) {
            psprosedur.setString(1, "%" + keyword + "%");
        } else if (!cbKodeP.isSelected() && cbNamaP.isSelected()) {
            psprosedur.setString(1, "%" + keyword + "%");
        } else if (cbKodeP.isSelected() && cbNamaP.isSelected()) {
            psprosedur.setString(1, "%" + keyword + "%");
            psprosedur.setString(2, "%" + keyword + "%");
        }

        rs = psprosedur.executeQuery();
        while (rs.next()) {
            tabModeProsedur.addRow(new Object[]{
                false,
                rs.getString("kode"),
                rs.getString("deskripsi_panjang"),
                rs.getString("deskripsi_pendek")
            });
        }
    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    } finally {
        try {
            if (rs != null) rs.close();
            if (psprosedur != null) psprosedur.close();
        } catch (Exception e) {
            System.out.println("Closing Error : " + e);
        }
    }
    
    }
    
    public void tampil2() {
        Valid.tabelKosong(TabModeTindakanPasien);
        try{            
            pstindakanpasien=koneksi.prepareStatement(
                    "select reg_periksa.tgl_registrasi,"
                            + "prosedur_pasien.no_rawat,"
                            + "reg_periksa.no_rkm_medis,"
                            + "pasien.nm_pasien,"
                            + "prosedur_pasien.kode,"
                            + "icd9.deskripsi_panjang,"
                            + "prosedur_pasien.status,"
                            + "prosedur_pasien.prioritas "+
                    "from prosedur_pasien inner join reg_periksa on prosedur_pasien.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join icd9 on prosedur_pasien.kode=icd9.kode "+
                    "where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.no_rkm_medis like ? "+
                    (keyword.trim().equals("")?"":"and (prosedur_pasien.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                    "pasien.nm_pasien like ? or prosedur_pasien.kode like ? or icd9.deskripsi_panjang like ? or "+
                    "prosedur_pasien.status like ?) ")+"order by reg_periksa.tgl_registrasi,prosedur_pasien.prioritas ");
            try {
                pstindakanpasien.setString(1,tanggal1);
                pstindakanpasien.setString(2,tanggal2);
                pstindakanpasien.setString(3,"%"+norm+"%");  
                if(!keyword.trim().equals("")){
                    pstindakanpasien.setString(4,"%"+keyword+"%");       
                    pstindakanpasien.setString(5,"%"+keyword+"%");        
                    pstindakanpasien.setString(6,"%"+keyword+"%");         
                    pstindakanpasien.setString(7,"%"+keyword+"%");         
                    pstindakanpasien.setString(8,"%"+keyword+"%");          
                    pstindakanpasien.setString(9,"%"+keyword+"%");  
                }
                     
                rs=pstindakanpasien.executeQuery();
                while(rs.next()){
                    TabModeTindakanPasien.addRow(new Object[]{false,
                                   rs.getString(1),
                                   rs.getString(2),
                                   rs.getString(3),
                                   rs.getString(4),
                                   rs.getString(5),
                                   rs.getString(6),
                                   rs.getString(7),
                                   rs.getString(8)
                    });
                }            
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(pstindakanpasien!=null){
                    pstindakanpasien.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void setRM(String norawat,String norm,String tanggal1,String tanggal2,String status,String keyword){
        this.norawat=norawat;
        this.norm=norm;
        this.tanggal1=tanggal1;
        this.tanggal2=tanggal2;
        this.status=status;
        this.keyword=keyword;
    }
    
    public void simpan(){
        try {
            koneksi.setAutoCommit(false);
            index=1;
            for(i=0;i<tbDiagnosa.getRowCount();i++){ 
                if(tbDiagnosa.getValueAt(i,0).toString().equals("true")){
                    if(Sequel.cariInteger(
                            "select count(diagnosa_pasien.kd_penyakit) from diagnosa_pasien "+
                            "inner join reg_periksa on diagnosa_pasien.no_rawat=reg_periksa.no_rawat "+
                            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where "+
                            "pasien.no_rkm_medis='"+norm+"' and diagnosa_pasien.kd_penyakit='"+tbDiagnosa.getValueAt(i,1).toString()+"'")>0){
                        Sequel.menyimpan("diagnosa_pasien","?,?,?,?,?","Penyakit",5,new String[]{
                            norawat,tbDiagnosa.getValueAt(i,1).toString(),status,
                            Sequel.cariIsi("select ifnull(MAX(diagnosa_pasien.prioritas)+1,1) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status='"+status+"'",norawat),"Lama"
                        });
                    }else{
                        Sequel.menyimpan("diagnosa_pasien","?,?,?,?,?","Penyakit",5,new String[]{
                            norawat,tbDiagnosa.getValueAt(i,1).toString(),status,
                            Sequel.cariIsi("select ifnull(MAX(diagnosa_pasien.prioritas)+1,1) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status='"+status+"'",norawat),"Baru"
                        });
                    }  
                    
                    if(index==1){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_diagnosa_utama=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_diagnosa_utama=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }   
                    }else if(index==2){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_diagnosa_sekunder=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_diagnosa_sekunder=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }else if(index==3){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_diagnosa_sekunder2=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_diagnosa_sekunder2=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }else if(index==4){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_diagnosa_sekunder3=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_diagnosa_sekunder3=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }   
                    }else if(index==5){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_diagnosa_sekunder4=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_diagnosa_sekunder4=?",2,new String[]{
                                tbDiagnosa.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }
                        
                    index++;
                }                    
            }
            koneksi.setAutoCommit(true);  
            for(i=0;i<tbDiagnosa.getRowCount();i++){ 
               tbDiagnosa.setValueAt(false,i,0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada data diagnosa yang sama dimasukkan sebelumnya...!");
        }

        try {
            koneksi.setAutoCommit(false);
            index=1;
            for(i=0;i<tbProsedur.getRowCount();i++){ 
                if(tbProsedur.getValueAt(i,0).toString().equals("true")){
                    Sequel.menyimpan("prosedur_pasien","?,?,?,?","ICD 9",4,new String[]{
                        norawat,tbProsedur.getValueAt(i,1).toString(),status,Sequel.cariIsi("select ifnull(MAX(prosedur_pasien.prioritas)+1,1) from prosedur_pasien where prosedur_pasien.no_rawat=? and prosedur_pasien.status='"+status+"'",norawat)
                    });
                    
                    if(index==1){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_prosedur_utama=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_prosedur_utama=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }else if(index==2){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_prosedur_sekunder=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_prosedur_sekunder=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }else if(index==3){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_prosedur_sekunder2=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_prosedur_sekunder2=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }else if(index==4){
                        if(status.equals("Ralan")){
                            Sequel.mengedit("resume_pasien","no_rawat=?","kd_prosedur_sekunder3=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }else if(status.equals("Ranap")){
                            Sequel.mengedit("resume_pasien_ranap","no_rawat=?","kd_prosedur_sekunder3=?",2,new String[]{
                                tbProsedur.getValueAt(i,1).toString(),norawat
                            });
                        }
                    }
                        
                    index++;
                }                    
            }
            koneksi.setAutoCommit(true);  
            for(i=0;i<tbProsedur.getRowCount();i++){ 
               tbProsedur.setValueAt(false,i,0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada data prosedur/ICD9 yang sama dimasukkan sebelumnya...!");
        }
        pilihTab();
    }

    public void pilihTab() {
        if(TabRawat.getSelectedIndex()==0){
            tampildiagnosa();
            tampilprosedure();
        }else if(TabRawat.getSelectedIndex()==1){
            tampil();
        }else if(TabRawat.getSelectedIndex()==2){
            tampil2();
        }
    }
    
    public void batal(){
        Diagnosa.setText("");
        for(i=0;i<tbDiagnosa.getRowCount();i++){ 
            tbDiagnosa.setValueAt(false,i,0);
        }
        for(i=0;i<tbProsedur.getRowCount();i++){ 
            tbProsedur.setValueAt(false,i,0);
        }
        Prosedur.setText("");
    }
    
    public void hapus(){
        if(TabRawat.getSelectedIndex()==1){
            if(TabModeDiagnosaPasien.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            }else{
                for(i=0;i<tbDiagnosaPasien.getRowCount();i++){ 
                    if(tbDiagnosaPasien.getValueAt(i,0).toString().equals("true")){
                        Sequel.queryu2("delete from diagnosa_pasien where no_rawat=? and kd_penyakit=?",2,new String[]{
                            tbDiagnosaPasien.getValueAt(i,2).toString(),tbDiagnosaPasien.getValueAt(i,5).toString()
                        });
                    }
                }
            }                     
        }else if(TabRawat.getSelectedIndex()==2){
            if(TabModeTindakanPasien.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            }else{
                for(i=0;i<tbTindakanPasien.getRowCount();i++){ 
                    if(tbTindakanPasien.getValueAt(i,0).toString().equals("true")){
                        Sequel.queryu2("delete from prosedur_pasien where no_rawat=? and kode=?",2,new String[]{
                            tbTindakanPasien.getValueAt(i,2).toString(),tbTindakanPasien.getValueAt(i,5).toString()
                        });
                    }
                }
            }
        }
        pilihTab();
    }
    
    public void cetak(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(TabRawat.getSelectedIndex()==1){
            if(TabModeDiagnosaPasien.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            }else if(TabModeDiagnosaPasien.getRowCount()!=0){
                Map<String, Object> param = new HashMap<>();
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                Valid.MyReportqry("rptDiagnosa.jasper","report","::[ Data Diagnosa Pasien ]::",
                        "select reg_periksa.tgl_registrasi,diagnosa_pasien.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                        "diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit, diagnosa_pasien.status,diagnosa_pasien.status_penyakit "+
                        "from diagnosa_pasien inner join reg_periksa inner join pasien inner join penyakit "+
                        "on diagnosa_pasien.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "and diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                        "where reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and reg_periksa.tgl_registrasi like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and diagnosa_pasien.no_rawat like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and reg_periksa.no_rkm_medis like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and pasien.nm_pasien like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and diagnosa_pasien.kd_penyakit like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and penyakit.nm_penyakit like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and diagnosa_pasien.status_penyakit like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and diagnosa_pasien.status like '%"+keyword+"%' "+
                        "order by reg_periksa.tgl_registrasi,diagnosa_pasien.prioritas ",param);
            }
        }else if(TabRawat.getSelectedIndex()==2){
            if(TabModeTindakanPasien.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            }else if(TabModeTindakanPasien.getRowCount()!=0){
                Map<String, Object> param = new HashMap<>();
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                Valid.MyReportqry("rptProsedur.jasper","report","::[ Data Prosedur Tindakan Pasien ]::",
                        "select reg_periksa.tgl_registrasi,prosedur_pasien.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                        "prosedur_pasien.kode,icd9.deskripsi_panjang, prosedur_pasien.status "+
                        "from prosedur_pasien inner join reg_periksa inner join pasien inner join icd9 "+
                        "on prosedur_pasien.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "and prosedur_pasien.kode=icd9.kode "+
                        "where reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and reg_periksa.tgl_registrasi like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and prosedur_pasien.no_rawat like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and reg_periksa.no_rkm_medis like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and pasien.nm_pasien like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and prosedur_pasien.kode like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and icd9.deskripsi_panjang like '%"+keyword+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+tanggal1+"' and '"+tanggal2+"' and reg_periksa.no_rkm_medis like '%"+norm+"%' and prosedur_pasien.status like '%"+keyword+"%' "+
                        "order by reg_periksa.tgl_registrasi,prosedur_pasien.prioritas ",param);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }
    
public void updatePrioritas(String noRawat, String kdPenyakit, String prioritasBaru) {
    Sequel.mengedit("diagnosa_pasien", 
        "no_rawat='" + noRawat + "' and kd_penyakit='" + kdPenyakit + "'",
        "prioritas='" + prioritasBaru + "'");
    tampil(); // Refresh tabel diagnosa
}

public void setTukarPrioritasListener(TukarPrioritasListener listener) {
    this.tukarListener = listener;
}

public JTable getTable() {
    return tbDiagnosaPasien; // atau nama table diagnosamu
}


    public void updateKodePenyakit(String noRawat, String kdPenyakitLama, String kdPenyakitBaru) {
        // Robust update: target 1 row exactly (no_rawat + status + prioritas + kd_penyakit)
        // Then upsert resume_pasien (ralan) based on current priorities
        if (noRawat == null || noRawat.trim().isEmpty()
                || kdPenyakitLama == null || kdPenyakitLama.trim().isEmpty()
                || kdPenyakitBaru == null || kdPenyakitBaru.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Data tidak lengkap untuk update kode penyakit.");
            return;
        }
        if (kdPenyakitLama.equals(kdPenyakitBaru)) {
            javax.swing.JOptionPane.showMessageDialog(null, "Kode penyakit baru sama dengan yang lama.");
            return;
        }

        // Ambil status & prioritas dari baris yang sedang dipilih di tabel (jika ada)
        String status = null;
        Integer prioritas = null;
        try {
            int row = tbDiagnosaPasien.getSelectedRow();
            if (row != -1) {
                String noRawatSel = tbDiagnosaPasien.getValueAt(row, 2).toString();
                String kdSel      = tbDiagnosaPasien.getValueAt(row, 5).toString();
                if (noRawat.equals(noRawatSel) && kdPenyakitLama.equals(kdSel)) {
                    status   = tbDiagnosaPasien.getValueAt(row, 7).toString(); // kolom "Status"
                    prioritas= Integer.valueOf(tbDiagnosaPasien.getValueAt(row, 9).toString()); // kolom "Prioritas"
                }
            }
        } catch (Exception ignore) {}

        // Jika tidak dapat dari tabel, fallback: ambil 1 baris dari DB (paling kecil prioritas)
        if (status == null || prioritas == null) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                "SELECT status, prioritas " +
                "FROM diagnosa_pasien " +
                "WHERE no_rawat=? AND kd_penyakit=? " +
                "ORDER BY prioritas ASC LIMIT 1")) {
                ps.setString(1, noRawat);
                ps.setString(2, kdPenyakitLama);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getString("status");
                        prioritas = rs.getInt("prioritas");
                    }
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(null, "Gagal membaca status/prioritas: " + ex.getMessage());
                return;
            }
        }

        if (status == null || prioritas == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Baris diagnosa tidak ditemukan/ambigu.");
            return;
        }

        boolean oldAuto = true;
        try {
            oldAuto = koneksi.getAutoCommit();
            koneksi.setAutoCommit(false);

            // 1) Update baris yang tepat
            try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE diagnosa_pasien " +
                "SET kd_penyakit=? " +
                "WHERE no_rawat=? AND status=? AND kd_penyakit=? AND prioritas=? " +
                "LIMIT 1")) {
                ps.setString(1, kdPenyakitBaru);
                ps.setString(2, noRawat);
                ps.setString(3, status);
                ps.setString(4, kdPenyakitLama);
                ps.setInt(5, prioritas.intValue());
                int n = ps.executeUpdate();
                if (n != 1) throw new RuntimeException("Baris diagnosa tidak ditemukan atau ambigu.");
            }

            // 2) Sinkronkan resume_pasien untuk status ralan
            try {
                syncResumePasienRalan(noRawat);
            } catch (Exception ex) {
                throw new RuntimeException("Sinkron resume_pasien gagal: " + ex.getMessage(), ex);
            }

            koneksi.commit();
            // Refresh tabel panel
            tampil();
        } catch (Exception e) {
            try { koneksi.rollback(); } catch (Exception ignore) {}
            javax.swing.JOptionPane.showMessageDialog(null, "Gagal update: " + e.getMessage());
        } finally {
            try { koneksi.setAutoCommit(oldAuto); } catch (Exception ignore) {}
        }
    }

private void syncResumePasienRalan(String noRawat) throws Exception {
    String utama=null, s1=null, s2=null, s3=null, s4=null;

    // Ambil diagnosa RALAN urut prioritas
    try (PreparedStatement ps = koneksi.prepareStatement(
        "SELECT prioritas, kd_penyakit " +
        "FROM diagnosa_pasien " +
        "WHERE no_rawat=? AND status='ralan' " +
        "ORDER BY prioritas ASC")) {
        ps.setString(1, noRawat);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int p = rs.getInt("prioritas");
                String kd = rs.getString("kd_penyakit");
                if (p==1)      utama = kd;
                else if (p==2) s1    = kd;
                else if (p==3) s2    = kd;
                else if (p==4) s3    = kd;
                else if (p==5) s4    = kd;
            }
        }
    }

    // UPSERT ke resume_pasien
    String upsert = "INSERT INTO resume_pasien " +
        "(no_rawat, kd_diagnosa_utama, kd_diagnosa_sekunder, kd_diagnosa_sekunder2, kd_diagnosa_sekunder3, kd_diagnosa_sekunder4) " +
        "VALUES (?,?,?,?,?,?) " +
        "ON DUPLICATE KEY UPDATE " +
        "kd_diagnosa_utama=VALUES(kd_diagnosa_utama), " +
        "kd_diagnosa_sekunder=VALUES(kd_diagnosa_sekunder), " +
        "kd_diagnosa_sekunder2=VALUES(kd_diagnosa_sekunder2), " +
        "kd_diagnosa_sekunder3=VALUES(kd_diagnosa_sekunder3), " +
        "kd_diagnosa_sekunder4=VALUES(kd_diagnosa_sekunder4)";
    try (PreparedStatement ps = koneksi.prepareStatement(upsert)) {
        ps.setString(1, noRawat);
        ps.setString(2, utama);
        ps.setString(3, s1);
        ps.setString(4, s2);
        ps.setString(5, s3);
        ps.setString(6, s4);
        ps.executeUpdate();
    }
}

}
