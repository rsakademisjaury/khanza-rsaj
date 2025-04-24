package simrskhanza;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import simrskhanza.DlgCariPoli;
import kepegawaian.DlgCariPetugas;
import javax.swing.text.html.HTMLEditorKit;


/**
 *
 * @author periwayat_pembedahanustakaan
 */
public final class LembarKonsulRanap extends javax.swing.JDialog {
    
    private final DefaultTableModel tabMode,tabModeTindakanKomplikasi,tabModeDetailKomplikasi;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2,rs3;
    private int i=0,jml=0,index=0;
    private boolean[] pilih; 
    private String[] kode,masalah;
    private DlgCariPoli poli1=new DlgCariPoli(null,false);
    private DlgCariDokter dokter2=new DlgCariDokter(null,false);
    private DlgCariDokter dokter1=new DlgCariDokter(null,false);
    private StringBuilder htmlContent;
    private String pilihan="";
    private String finger="";
    private String konsultasiparamput="";
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
      public LembarKonsulRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        //hidden tabel master konsul
        Scroll10.setVisible(false);
        BtnTambahMasalah2.setVisible(false);
        BtnCariPemeriksaan3.setVisible(false);
        TCariKomplikasi.setVisible(false);
        scrollPane6.setEnabled(false);        
        
        tabMode=new DefaultTableModel(null,new Object[]{            
          "No.Rawat",//1
          "No.RM",//2
          "Nama Pasien",//3
          "Tgl.Lahir",//4
          "J.K.",//5
          "Kode Poli",//6
          "Nama Poli",//7
          "Kode Dokter1",//8
          "Dokter Rujukan",//9
          "Kode Dokter2",//10
          "Dokter Perujuk",//11
          "diagnosa",//12
          "Terapi Yang Diberikan",//13
          "Tanggal",//14
          "Tgl.Jawab",//15
//          "Permintaan",//16
          "Status",//17
          "Pengobatan",//18
          "Usul",//19
          "Pemeriksaan Yang Dilakukan",//20      
          "Hasil",//21
          "Diagnosa Jawaban"//22      

         }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 21; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(25);
            }else if(i==5){
                column.setPreferredWidth(85);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(85);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(90);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(117);
            }else if(i==12){
                column.setPreferredWidth(117);
            }else if(i==13){
                column.setPreferredWidth(117);
            }else if(i==14){
                column.setPreferredWidth(117);
            }else if(i==15){
                column.setPreferredWidth(117);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        //
    
                //tindakan konsultasi
tabModeTindakanKomplikasi=new DefaultTableModel(null,new Object[]{
                "P","KODE","KONSULTASI"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbMasalahKomplikasiKehamilan.setModel(tabModeTindakanKomplikasi);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahKomplikasiKehamilan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMasalahKomplikasiKehamilan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbMasalahKomplikasiKehamilan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        
        
        tbMasalahKomplikasiKehamilan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailKomplikasi=new DefaultTableModel(null,new Object[]{
                "Kode","KONSULTASI"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKomplikasi.setModel(tabModeDetailKomplikasi);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbKomplikasi.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKomplikasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbKomplikasi.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(420);
            }
        }
        tbKomplikasi.setDefaultRenderer(Object.class, new WarnaTable());
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));        
//        
        TCari.setDocument(new batasInput((int)100).getKata(TCari));        
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
        
        //sampai sini
             poli1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli1.getTable().getSelectedRow()!= -1){ 
                    if(i==1){
                        kd_poli.setText(poli1.getTable().getValueAt(poli1.getTable().getSelectedRow(),0).toString());
                        nm_poli.setText(poli1.getTable().getValueAt(poli1.getTable().getSelectedRow(),1).toString());  
                      
                    }
                         
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        poli1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli1.getTable().getSelectedRow()!= -1){ 
                    kd_poli.setText(poli1.getTable().getValueAt(poli1.getTable().getSelectedRow(),0).toString());
                    nm_poli.setText(poli1.getTable().getValueAt(poli1.getTable().getSelectedRow(),1).toString());  
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        //
        dokter2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter2.getTable().getSelectedRow()!= -1){ 
                    if(i==1){
                        kd_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),0).toString());
                        nama_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());  
                      
                    }
                         
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        dokter2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter2.getTable().getSelectedRow()!= -1){ 
                    kd_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),0).toString());
                    nama_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());  
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
              dokter1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter1.getTable().getSelectedRow()!= -1){ 
                    kd_dokter2.setText(dokter1.getTable().getValueAt(dokter1.getTable().getSelectedRow(),0).toString());
                    nama_dokter2.setText(dokter1.getTable().getValueAt(dokter1.getTable().getSelectedRow(),1).toString());  
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
   
            dokter1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter1.getTable().getSelectedRow()!= -1){ 
                    kd_dokter2.setText(dokter1.getTable().getValueAt(dokter1.getTable().getSelectedRow(),0).toString());
                    nama_dokter2.setText(dokter1.getTable().getValueAt(dokter1.getTable().getSelectedRow(),1).toString());  
                }              
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        ChkAccor.setSelected(false);
        ChkAccor.setVisible(false);
        isMenu();
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
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        status = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        tbKomplikasiKehamilanSebelumnya = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        nama_dokter = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        label11 = new widget.Label();
        jLabel11 = new widget.Label();
        TglAsuhan = new widget.Tanggal();
        label16 = new widget.Label();
        nama_dokter2 = new widget.TextBox();
        BtnDPJP = new widget.Button();
        scrollPane2 = new widget.ScrollPane();
        jawab_permintaan = new widget.TextArea();
        jLabel31 = new widget.Label();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        Scroll10 = new widget.ScrollPane();
        tbMasalahKomplikasiKehamilan = new widget.Table();
        TCariKomplikasi = new widget.TextBox();
        BtnCariPemeriksaan3 = new widget.Button();
        BtnTambahMasalah2 = new widget.Button();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel32 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        saran_tindakan = new widget.TextArea();
        jLabel33 = new widget.Label();
        jLabel15 = new widget.Label();
        jLabel12 = new widget.Label();
        nm_poli = new widget.TextBox();
        BtnPoli = new widget.Button();
        scrollPane4 = new widget.ScrollPane();
        catatan = new widget.TextArea();
        kd_poli = new widget.TextBox();
        kd_dokter2 = new widget.TextBox();
        kd_dokter = new widget.TextBox();
        TglAsuhan1 = new widget.Tanggal();
        scrollPane6 = new widget.ScrollPane();
        isi_permintaan = new widget.TextArea();
        scrollPane7 = new widget.ScrollPane();
        hasil = new widget.TextArea();
        label17 = new widget.Label();
        diagnosa = new widget.TextBox();
        label18 = new widget.Label();
        label22 = new widget.Label();
        jLabel35 = new widget.Label();
        label19 = new widget.Label();
        diagnosa_jawaban = new widget.TextBox();
        BtnCopirawat = new widget.Button();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormMenu = new widget.PanelBiasa();
        jLabel34 = new widget.Label();
        TNoRM1 = new widget.TextBox();
        TPasien1 = new widget.TextBox();
        BtnPrint1 = new widget.Button();
        FormMasalahRencana = new widget.PanelBiasa();
        Scroll11 = new widget.ScrollPane();
        tbKomplikasi = new widget.Table();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        jMenuItem1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Select.png"))); // NOI18N
        jMenuItem1.setText("report hal1");
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem1);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N

        status.setEditable(false);
        status.setText("sudah");
        status.setHighlighter(null);
        status.setName("status"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), ":: Form Konsultasi Antar Poliklinik ::"));
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1138, 650));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setMinimumSize(new java.awt.Dimension(44, 54));
        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.CENTER);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setMinimumSize(new java.awt.Dimension(496, 697));
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.setPreferredSize(new java.awt.Dimension(928, 700));
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setMaximumSize(new java.awt.Dimension(47483647, 47483647));
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setPreferredSize(new java.awt.Dimension(102, 757));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        tbKomplikasiKehamilanSebelumnya.setBackground(new java.awt.Color(255, 255, 255));
        tbKomplikasiKehamilanSebelumnya.setBorder(null);
        tbKomplikasiKehamilanSebelumnya.setName("tbKomplikasiKehamilanSebelumnya"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.setPreferredSize(new java.awt.Dimension(1138, 600));
        tbKomplikasiKehamilanSebelumnya.setLayout(null);

        TNoRw.setForeground(new java.awt.Color(0, 0, 0));
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRwActionPerformed(evt);
            }
        });
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TNoRw);
        TNoRw.setBounds(90, 10, 138, 23);

        TPasien.setEditable(false);
        TPasien.setForeground(new java.awt.Color(0, 0, 0));
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(TPasien);
        TPasien.setBounds(293, 10, 237, 23);

        TNoRM.setEditable(false);
        TNoRM.setForeground(new java.awt.Color(0, 0, 0));
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(TNoRM);
        TNoRM.setBounds(230, 10, 60, 23);

        label14.setText(" dr Perujuk :");
        label14.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(55, 44));
        tbKomplikasiKehamilanSebelumnya.add(label14);
        label14.setBounds(0, 40, 80, 23);

        nama_dokter.setEditable(false);
        nama_dokter.setForeground(new java.awt.Color(0, 0, 0));
        nama_dokter.setName("nama_dokter"); // NOI18N
        nama_dokter.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(nama_dokter);
        nama_dokter.setBounds(200, 40, 330, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('2');
        BtnPetugas.setToolTipText("Alt+2");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        BtnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnPetugas);
        BtnPetugas.setBounds(530, 40, 30, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel8);
        jLabel8.setBounds(570, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setForeground(new java.awt.Color(0, 0, 0));
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(TglLahir);
        TglLahir.setBounds(640, 10, 100, 23);

        Jk.setEditable(false);
        Jk.setForeground(new java.awt.Color(0, 0, 0));
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(Jk);
        Jk.setBounds(810, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(55, 44));
        tbKomplikasiKehamilanSebelumnya.add(jLabel10);
        jLabel10.setBounds(0, 10, 80, 23);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        tbKomplikasiKehamilanSebelumnya.add(label11);
        label11.setBounds(580, 40, 50, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel11);
        jLabel11.setBounds(770, 10, 30, 23);

        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "19-04-2024 13:13:33" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TglAsuhan);
        TglAsuhan.setBounds(640, 40, 140, 23);

        label16.setText("Kepada dr :");
        label16.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(55, 44));
        tbKomplikasiKehamilanSebelumnya.add(label16);
        label16.setBounds(570, 70, 60, 23);

        nama_dokter2.setEditable(false);
        nama_dokter2.setForeground(new java.awt.Color(0, 0, 0));
        nama_dokter2.setName("nama_dokter2"); // NOI18N
        nama_dokter2.setPreferredSize(new java.awt.Dimension(207, 23));
        nama_dokter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nama_dokter2ActionPerformed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(nama_dokter2);
        nama_dokter2.setBounds(750, 70, 330, 23);

        BtnDPJP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDPJP.setMnemonic('2');
        BtnDPJP.setToolTipText("Alt+2");
        BtnDPJP.setName("BtnDPJP"); // NOI18N
        BtnDPJP.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDPJP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDPJPActionPerformed(evt);
            }
        });
        BtnDPJP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDPJPKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnDPJP);
        BtnDPJP.setBounds(1080, 70, 30, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        jawab_permintaan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jawab_permintaan.setColumns(20);
        jawab_permintaan.setForeground(new java.awt.Color(0, 0, 0));
        jawab_permintaan.setRows(5);
        jawab_permintaan.setName("jawab_permintaan"); // NOI18N
        jawab_permintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jawab_permintaanKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(jawab_permintaan);

        tbKomplikasiKehamilanSebelumnya.add(scrollPane2);
        scrollPane2.setBounds(570, 390, 540, 80);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Jawaban Konsultasi Antar Poliklinik");
        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel31.setName("jLabel31"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel31);
        jLabel31.setBounds(20, 260, 330, 40);

        jSeparator11.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator11.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator11.setName("jSeparator11"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jSeparator11);
        jSeparator11.setBounds(0, 1840, 890, 0);

        jSeparator13.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator13.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator13.setName("jSeparator13"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jSeparator13);
        jSeparator13.setBounds(0, 1840, 890, 0);

        Scroll10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll10.setEnabled(false);
        Scroll10.setName("Scroll10"); // NOI18N
        Scroll10.setOpaque(true);
        Scroll10.setPreferredSize(new java.awt.Dimension(552, 502));

        tbMasalahKomplikasiKehamilan.setName("tbMasalahKomplikasiKehamilan"); // NOI18N
        Scroll10.setViewportView(tbMasalahKomplikasiKehamilan);

        tbKomplikasiKehamilanSebelumnya.add(Scroll10);
        Scroll10.setBounds(1180, 130, 320, 120);

        TCariKomplikasi.setToolTipText("Alt+C");
        TCariKomplikasi.setEnabled(false);
        TCariKomplikasi.setName("TCariKomplikasi"); // NOI18N
        TCariKomplikasi.setOpaque(true);
        TCariKomplikasi.setPreferredSize(new java.awt.Dimension(140, 23));
        TCariKomplikasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCariKomplikasiActionPerformed(evt);
            }
        });
        TCariKomplikasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKomplikasiKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TCariKomplikasi);
        TCariKomplikasi.setBounds(1240, 270, 210, 20);

        BtnCariPemeriksaan3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariPemeriksaan3.setMnemonic('1');
        BtnCariPemeriksaan3.setToolTipText("Alt+1");
        BtnCariPemeriksaan3.setEnabled(false);
        BtnCariPemeriksaan3.setName("BtnCariPemeriksaan3"); // NOI18N
        BtnCariPemeriksaan3.setOpaque(true);
        BtnCariPemeriksaan3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariPemeriksaan3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariPemeriksaan3ActionPerformed(evt);
            }
        });
        BtnCariPemeriksaan3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariPemeriksaan3KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnCariPemeriksaan3);
        BtnCariPemeriksaan3.setBounds(1450, 270, 30, 20);

        BtnTambahMasalah2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahMasalah2.setMnemonic('3');
        BtnTambahMasalah2.setToolTipText("Alt+3");
        BtnTambahMasalah2.setEnabled(false);
        BtnTambahMasalah2.setName("BtnTambahMasalah2"); // NOI18N
        BtnTambahMasalah2.setOpaque(true);
        BtnTambahMasalah2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahMasalah2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahMasalah2ActionPerformed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnTambahMasalah2);
        BtnTambahMasalah2.setBounds(1490, 270, 30, 20);

        jSeparator12.setBackground(new java.awt.Color(204, 204, 204));
        jSeparator12.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jSeparator12);
        jSeparator12.setBounds(20, 253, 1090, 10);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("Pengobatan :");
        jLabel32.setName("jLabel32"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel32);
        jLabel32.setBounds(570, 370, 100, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        saran_tindakan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        saran_tindakan.setColumns(20);
        saran_tindakan.setForeground(new java.awt.Color(0, 0, 0));
        saran_tindakan.setRows(5);
        saran_tindakan.setName("saran_tindakan"); // NOI18N
        saran_tindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saran_tindakanKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(saran_tindakan);

        tbKomplikasiKehamilanSebelumnya.add(scrollPane3);
        scrollPane3.setBounds(570, 500, 540, 80);

        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("Usul :");
        jLabel33.setName("jLabel33"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel33);
        jLabel33.setBounds(570, 480, 100, 23);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("Tanggal :");
        jLabel15.setName("jLabel15"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel15);
        jLabel15.setBounds(20, 310, 70, 23);

        jLabel12.setText("Poli Tujuan :");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(55, 44));
        tbKomplikasiKehamilanSebelumnya.add(jLabel12);
        jLabel12.setBounds(0, 70, 80, 23);

        nm_poli.setForeground(new java.awt.Color(0, 0, 0));
        nm_poli.setHighlighter(null);
        nm_poli.setName("nm_poli"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(nm_poli);
        nm_poli.setBounds(200, 70, 330, 23);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setMnemonic('2');
        BtnPoli.setToolTipText("Alt+2");
        BtnPoli.setName("BtnPoli"); // NOI18N
        BtnPoli.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoliActionPerformed(evt);
            }
        });
        BtnPoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoliKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnPoli);
        BtnPoli.setBounds(530, 70, 30, 23);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane4.setName("scrollPane4"); // NOI18N

        catatan.setEditable(false);
        catatan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        catatan.setColumns(20);
        catatan.setForeground(new java.awt.Color(0, 0, 0));
        catatan.setRows(5);
        catatan.setName("catatan"); // NOI18N
        catatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                catatanKeyPressed(evt);
            }
        });
        scrollPane4.setViewportView(catatan);

        tbKomplikasiKehamilanSebelumnya.add(scrollPane4);
        scrollPane4.setBounds(570, 154, 540, 80);

        kd_poli.setForeground(new java.awt.Color(0, 0, 0));
        kd_poli.setHighlighter(null);
        kd_poli.setName("kd_poli"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(kd_poli);
        kd_poli.setBounds(90, 70, 108, 26);

        kd_dokter2.setEditable(false);
        kd_dokter2.setForeground(new java.awt.Color(0, 0, 0));
        kd_dokter2.setName("kd_dokter2"); // NOI18N
        kd_dokter2.setPreferredSize(new java.awt.Dimension(80, 23));
        kd_dokter2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kd_dokter2KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(kd_dokter2);
        kd_dokter2.setBounds(640, 70, 108, 23);

        kd_dokter.setEditable(false);
        kd_dokter.setForeground(new java.awt.Color(0, 0, 0));
        kd_dokter.setName("kd_dokter"); // NOI18N
        kd_dokter.setPreferredSize(new java.awt.Dimension(80, 23));
        kd_dokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kd_dokterKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(kd_dokter);
        kd_dokter.setBounds(90, 40, 108, 23);

        TglAsuhan1.setForeground(new java.awt.Color(0, 0, 0));
        TglAsuhan1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "19-04-2024 13:13:34" }));
        TglAsuhan1.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan1.setName("TglAsuhan1"); // NOI18N
        TglAsuhan1.setOpaque(false);
        TglAsuhan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhan1KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TglAsuhan1);
        TglAsuhan1.setBounds(80, 310, 140, 20);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane6.setName("scrollPane6"); // NOI18N

        isi_permintaan.setEditable(false);
        isi_permintaan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        isi_permintaan.setColumns(20);
        isi_permintaan.setForeground(new java.awt.Color(0, 0, 0));
        isi_permintaan.setRows(5);
        isi_permintaan.setName("isi_permintaan"); // NOI18N
        isi_permintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                isi_permintaanKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(isi_permintaan);

        tbKomplikasiKehamilanSebelumnya.add(scrollPane6);
        scrollPane6.setBounds(20, 154, 540, 80);

        scrollPane7.setBackground(new java.awt.Color(204, 204, 204));
        scrollPane7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        scrollPane7.setName("scrollPane7"); // NOI18N

        hasil.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        hasil.setColumns(20);
        hasil.setForeground(new java.awt.Color(0, 0, 0));
        hasil.setRows(5);
        hasil.setName("hasil"); // NOI18N
        hasil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hasilKeyPressed(evt);
            }
        });
        scrollPane7.setViewportView(hasil);

        tbKomplikasiKehamilanSebelumnya.add(scrollPane7);
        scrollPane7.setBounds(20, 390, 540, 190);

        label17.setText("Diagnosa :");
        label17.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label17);
        label17.setBounds(0, 100, 80, 23);

        diagnosa.setEditable(false);
        diagnosa.setForeground(new java.awt.Color(0, 0, 0));
        diagnosa.setName("diagnosa"); // NOI18N
        diagnosa.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(diagnosa);
        diagnosa.setBounds(90, 100, 990, 23);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label18.setText("Pemeriksaan yang telah dilakukan :");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label18);
        label18.setBounds(20, 130, 180, 23);

        label22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label22.setText("Terapi yang diberikan :");
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label22);
        label22.setBounds(570, 130, 150, 23);

        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setText("Hasil :");
        jLabel35.setName("jLabel35"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel35);
        jLabel35.setBounds(20, 370, 130, 23);

        label19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label19.setText("Diagnosa :");
        label19.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label19);
        label19.setBounds(20, 340, 70, 23);

        diagnosa_jawaban.setForeground(new java.awt.Color(0, 0, 0));
        diagnosa_jawaban.setName("diagnosa_jawaban"); // NOI18N
        diagnosa_jawaban.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(diagnosa_jawaban);
        diagnosa_jawaban.setBounds(80, 340, 890, 23);

        BtnCopirawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/copy.png"))); // NOI18N
        BtnCopirawat.setMnemonic('6');
        BtnCopirawat.setText("Samakan Diagnosa");
        BtnCopirawat.setToolTipText("Salin No. Rawat");
        BtnCopirawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnCopirawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnCopirawat.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnCopirawat.setIconTextGap(7);
        BtnCopirawat.setName("BtnCopirawat"); // NOI18N
        BtnCopirawat.setPreferredSize(new java.awt.Dimension(16, 16));
        BtnCopirawat.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCopirawat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCopirawatActionPerformed(evt);
            }
        });
        BtnCopirawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCopirawatKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnCopirawat);
        BtnCopirawat.setBounds(970, 340, 140, 20);

        scrollInput.setViewportView(tbKomplikasiKehamilanSebelumnya);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Jawaban Konsultasi Antar Poliklinik", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 400));

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
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "19-04-2024" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "19-04-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(195, 23));
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
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setEnabled(false);
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(0, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 248));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelected(true);
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));

        jLabel34.setText("Pasien :");
        jLabel34.setName("jLabel34"); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(55, 23));
        FormMenu.add(jLabel34);

        TNoRM1.setEditable(false);
        TNoRM1.setHighlighter(null);
        TNoRM1.setName("TNoRM1"); // NOI18N
        TNoRM1.setPreferredSize(new java.awt.Dimension(100, 23));
        FormMenu.add(TNoRM1);

        TPasien1.setEditable(false);
        TPasien1.setBackground(new java.awt.Color(245, 250, 240));
        TPasien1.setHighlighter(null);
        TPasien1.setName("TPasien1"); // NOI18N
        TPasien1.setPreferredSize(new java.awt.Dimension(250, 23));
        FormMenu.add(TPasien1);

        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png"))); // NOI18N
        BtnPrint1.setMnemonic('T');
        BtnPrint1.setToolTipText("Alt+T");
        BtnPrint1.setName("BtnPrint1"); // NOI18N
        BtnPrint1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint1ActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPrint1);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        FormMasalahRencana.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        FormMasalahRencana.setName("FormMasalahRencana"); // NOI18N
        FormMasalahRencana.setLayout(new java.awt.GridLayout(3, 1, 1, 1));

        Scroll11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll11.setName("Scroll11"); // NOI18N
        Scroll11.setOpaque(true);
        Scroll11.setPreferredSize(new java.awt.Dimension(700, 402));

        tbKomplikasi.setName("tbKomplikasi"); // NOI18N
        Scroll11.setViewportView(tbKomplikasi);

        FormMasalahRencana.add(Scroll11);

        PanelAccor.add(FormMasalahRencana, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Riwayat Jawaban", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
//           if(TNoRw.getText().trim().equals("")){
//            Valid.textKosong(TNoRw,"Nama Pasien");   
//        }else{
//            if(tbObat.getSelectedRow()>-1){
//                if(Sequel.mengedittf("rujukan_internal_dpjp",
//                        "no_rawat=?",
//                        "no_rawat=?,"
//                        +"kd_dokter=?,"
//                        +"kd_poli=?,"
//                        +"nm_poli=?," 
//                        +"kd_dokter2=?," 
//                        +"nama_dokter2=?," 
//                        +"diagnosa=?,"
//                        +"catatan=?,"  
//                        +"tanggal=?,"
//                        +"status=?,"
//                        +"tanggal_jawab=?,"
//                        +"jawab_permintaan=?,"
//                        +"saran_tindakan=?"
//                            ,14,new String[]{
//                        TNoRw.getText(),
//                        kd_dokter2.getText(),
//                        kd_poli.getText(),
//                        nm_poli.getText(),
//                        kd_dokter.getText(),
//                        nama_dokter.getText(),
//                        diagnosa.getText(),
//                        catatan.getText(),
//                        Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
//                        status.getText(),
//                        Valid.SetTgl(TglAsuhan1.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
//                    
//                        jawab_permintaan.getText(),
//                        saran_tindakan.getText(),
//            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
//                  })==true){
//             
//                Sequel.meghapus("konsultasi","no_rawat",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
//                for (i = 0; i < tbMasalahKomplikasiKehamilan.getRowCount(); i++) {
//                            if(tbMasalahKomplikasiKehamilan.getValueAt(i,0).toString().equals("true")){
//                                Sequel.menyimpan2("konsultasi","?,?",2,new String[]{TNoRw.getText(),tbMasalahKomplikasiKehamilan.getValueAt(i,1).toString()});
//                            }
//                            }
//                        getKehamilanSebelumnya();
//                        tampil();
//                        emptTeks();
//                        TabRawat.setSelectedIndex(0);
//        }
//        }
//        }

Valid.editTable(tabMode,"rujukan_internal_dpjp","no_rawat",TNoRw," jawab_permintaan='"+jawab_permintaan.getText()+"'");
Valid.editTable(tabMode,"rujukan_internal_dpjp","no_rawat",TNoRw," saran_tindakan='"+saran_tindakan.getText()+"'");
Valid.editTable(tabMode,"rujukan_internal_dpjp","no_rawat",TNoRw," hasil='"+hasil.getText()+"'");
Valid.editTable(tabMode,"rujukan_internal_dpjp","no_rawat",TNoRw," diagnosa_jawaban='"+diagnosa_jawaban.getText()+"'");
BtnSimpan.setText("Edit");
JOptionPane.showMessageDialog(null,"Proses simpan selesai...!");
tampil();
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed

}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
        tampilKehamilanSebelumnya();
    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();    
            param.put("kd_dokter2",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",kd_dokter2.getText()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+kd_dokter2.getText()+".png");
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            param.put("norawat",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            try {
                ps=koneksi.prepareStatement("select * from riwayat_persalinan_pasien where no_rkm_medis=? order by tgl_thn");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                    rs=ps.executeQuery();
                    i=1;
                    while(rs.next()){
                        param.put("no"+i,i+"");  
                        param.put("tgl"+i,rs.getString("tgl_thn"));
                        param.put("tempatpersalinan"+i,rs.getString("tempat_persalinan"));
                        param.put("usiahamil"+i,rs.getString("usia_hamil"));
                        param.put("jenispersalinan"+i,rs.getString("jenis_persalinan"));
                        param.put("penolong"+i,rs.getString("penolong"));
                        param.put("penyulit"+i,rs.getString("penyulit"));
                        param.put("jk"+i,rs.getString("jk"));
                        param.put("bbpb"+i,rs.getString("bbpb"));
                        param.put("keadaan"+i,rs.getString("keadaan"));
                        i++;
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
               try {
                konsultasiparamput="";
                ps2=koneksi.prepareStatement(
                    "select master_konsultasi.kode_masalah,master_konsultasi.nama_masalah from master_konsultasi "+
                    "inner join konsultasi on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                    "where konsultasi.no_rawat=? order by kode_masalah");
                try {
                    ps2.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs2=ps2.executeQuery();
                    while(rs2.next()){
                        konsultasiparamput=rs2.getString("nama_masalah")+", "+konsultasiparamput;
                    }
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                    if(ps2!=null){
                        ps2.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
//                param.put("konsultasi1",konsultasiparamput);
//                    Valid.MyReportqry("rptLembarpermintaankonsul.jasper","report","::[ Laporan Penilaian Awal Ralan Kebidanan & Kandungan ]::",               
//"select reg_periksa.no_rkm_medis,rujukan_internal_dpjp.no_rawat,pasien.nm_pasien,pasien.tgl_lahir,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
//        + "rujukan_internal_dpjp.kd_poli,poliklinik.nm_poli,"      //7
//+ "rujukan_internal_dpjp.kd_dokter,dokter.nm_dokter,rujukan_internal_dpjp.kd_dokter2,rujukan_internal_dpjp.nama_dokter2,rujukan_internal_dpjp.catatan,rujukan_internal_dpjp.isi_permintaan,rujukan_internal_dpjp.hasil,rujukan_internal_dpjp.diagnosa_jawaban,"
//        + "rujukan_internal_dpjp.tanggal,rujukan_internal_dpjp.tanggal_jawab,"//14
//+ "rujukan_internal_dpjp.jawab_permintaan,rujukan_internal_dpjp.saran_tindakan from reg_periksa "
//                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
//                    + "inner join rujukan_internal_dpjp on reg_periksa.no_rawat=rujukan_internal_dpjp.no_rawat "
//                    + "inner join dokter on rujukan_internal_dpjp.kd_dokter=dokter.kd_dokter "
//                    + "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "
//                    + "inner join konsultasi on reg_periksa.no_rawat=konsultasi.no_rawat "  
//                    + "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);   
            Valid.MyReportqry("rptLembarpermintaankonsul.jasper","report","::[ Cetakan Konsultasi Antar Poliklinik ]::",              
        "select "
        + "reg_periksa.no_rkm_medis,"//1
        + "rujukan_internal_dpjp.no_rawat,"//2
        + "pasien.nm_pasien,"//3
        + "pasien.tgl_lahir,"//3
//        + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"//4
        + "rujukan_internal_dpjp.kd_poli,"
        + "poliklinik.nm_poli,"      //5
        + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,"
        + "rujukan_internal_dpjp.kd_dokter,"//6
        + "dokter.nm_dokter,"//7
        + "rujukan_internal_dpjp.kd_dokter2,"//8
        + "rujukan_internal_dpjp.nama_dokter2,"//9
        + "rujukan_internal_dpjp.catatan,"//10 "terapi yang diberikan"
        + "rujukan_internal_dpjp.diagnosa,"//10 "diagnosa"        
        + "rujukan_internal_dpjp.tanggal,"//11 "tanggal konsul"
        + "rujukan_internal_dpjp.tanggal_jawab,"//12 "tanggal jawab"
        + "rujukan_internal_dpjp.jawab_permintaan," //14 "PENGOBATAN"
        + "rujukan_internal_dpjp.saran_tindakan," //15  "USUL"
        + "rujukan_internal_dpjp.isi_permintaan," //16 "pemeriksaan yang dilakukan"
        + "rujukan_internal_dpjp.hasil," //16 "hasil"        
        + "rujukan_internal_dpjp.diagnosa_jawaban " //17        
        + "from reg_periksa "
        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
        + "inner join rujukan_internal_dpjp on reg_periksa.no_rawat=rujukan_internal_dpjp.no_rawat "
        + "inner join dokter on rujukan_internal_dpjp.kd_dokter=dokter.kd_dokter "
        + "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "
        + "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);                                             
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();    
            param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",kd_dokter.getText()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+kd_dokter.getText()+".png");
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            param.put("norawat",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            try {
                ps=koneksi.prepareStatement("select * from riwayat_persalinan_pasien where no_rkm_medis=? order by tgl_thn");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                    rs=ps.executeQuery();
                    i=1;
                    while(rs.next()){
                        param.put("no"+i,i+"");  
                        param.put("tgl"+i,rs.getString("tgl_thn"));
                        param.put("tempatpersalinan"+i,rs.getString("tempat_persalinan"));
                        param.put("usiahamil"+i,rs.getString("usia_hamil"));
                        param.put("jenispersalinan"+i,rs.getString("jenis_persalinan"));
                        param.put("penolong"+i,rs.getString("penolong"));
                        param.put("penyulit"+i,rs.getString("penyulit"));
                        param.put("jk"+i,rs.getString("jk"));
                        param.put("bbpb"+i,rs.getString("bbpb"));
                        param.put("keadaan"+i,rs.getString("keadaan"));
                        i++;
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
               try {
                konsultasiparamput="";
                ps2=koneksi.prepareStatement(
                    "select master_konsultasi.kode_masalah,master_konsultasi.nama_masalah from master_konsultasi "+
                    "inner join konsultasi on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                    "where konsultasi.no_rawat=? order by kode_masalah");
                try {
                    ps2.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs2=ps2.executeQuery();
                    while(rs2.next()){
                        konsultasiparamput=rs2.getString("nama_masalah")+", "+konsultasiparamput;
                    }
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                    if(ps2!=null){
                        ps2.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
                param.put("konsultasi1",konsultasiparamput);
//                    Valid.MyReportqry("rptLembarpermintaankonsul.jasper","report","::[ Laporan Penilaian Awal Ralan Kebidanan & Kandungan ]::",               
//"select reg_periksa.no_rkm_medis,rujukan_internal_dpjp.no_rawat,pasien.nm_pasien,pasien.tgl_lahir,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
//        + "rujukan_internal_dpjp.kd_poli,poliklinik.nm_poli,"      //7
//+ "rujukan_internal_dpjp.kd_dokter,dokter.nm_dokter,rujukan_internal_dpjp.kd_dokter2,rujukan_internal_dpjp.nama_dokter2,rujukan_internal_dpjp.catatan,rujukan_internal_dpjp.isi_permintaan,rujukan_internal_dpjp.hasil,rujukan_internal_dpjp.diagnosa_jawaban,"
//        + "rujukan_internal_dpjp.tanggal,rujukan_internal_dpjp.tanggal_jawab,"//14
//+ "rujukan_internal_dpjp.jawab_permintaan,rujukan_internal_dpjp.saran_tindakan from reg_periksa "
//                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
//                    + "inner join rujukan_internal_dpjp on reg_periksa.no_rawat=rujukan_internal_dpjp.no_rawat "
//                    + "inner join dokter on rujukan_internal_dpjp.kd_dokter=dokter.kd_dokter "
//                    + "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "
//                    + "inner join konsultasi on reg_periksa.no_rawat=konsultasi.no_rawat "  
//                    + "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);   
            Valid.MyReportqry("rptLembarjawabankonsul.jasper","report","::[ Cetakan Jawaban Konsultasi Antar Poliklinik ]::",              
        "select "
        + "reg_periksa.no_rkm_medis,"//1
        + "rujukan_internal_dpjp.no_rawat,"//2
        + "pasien.nm_pasien,"//3
        + "pasien.tgl_lahir,"//3
//        + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"//4
        + "rujukan_internal_dpjp.kd_poli,"
        + "poliklinik.nm_poli,"      //5
        + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,"
        + "rujukan_internal_dpjp.kd_dokter,"//6
        + "dokter.nm_dokter,"//7
        + "rujukan_internal_dpjp.kd_dokter2,"//8
        + "rujukan_internal_dpjp.nama_dokter2,"//9
        + "rujukan_internal_dpjp.catatan,"//10 "terapi yang diberikan"
        + "rujukan_internal_dpjp.diagnosa,"//10 "diagnosa"        
        + "rujukan_internal_dpjp.tanggal,"//11 "tanggal konsul"
        + "rujukan_internal_dpjp.tanggal_jawab,"//12 "tanggal jawab"
        + "rujukan_internal_dpjp.jawab_permintaan," //14 "PENGOBATAN"
        + "rujukan_internal_dpjp.saran_tindakan," //15  "USUL"
        + "rujukan_internal_dpjp.isi_permintaan," //16 "pemeriksaan yang dilakukan"
        + "rujukan_internal_dpjp.hasil," //16 "hasil"        
        + "rujukan_internal_dpjp.diagnosa_jawaban " //17        
        + "from reg_periksa "
        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
        + "inner join rujukan_internal_dpjp on reg_periksa.no_rawat=rujukan_internal_dpjp.no_rawat "
        + "inner join dokter on rujukan_internal_dpjp.kd_dokter=dokter.kd_dokter "
        + "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "
        + "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);                                             
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void BtnTambahMasalah2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahMasalah2ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterKonsultasi form=new MasterKonsultasi(null,false);
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());        // TODO add your handling code here:
    }//GEN-LAST:event_BtnTambahMasalah2ActionPerformed

    private void BtnCariPemeriksaan3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaan3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariPemeriksaan3KeyPressed

    private void BtnCariPemeriksaan3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaan3ActionPerformed
        tampilKehamilanSebelumnya();       // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariPemeriksaan3ActionPerformed

    private void TCariKomplikasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKomplikasiKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilKehamilanSebelumnya();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            //Rencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            //KetDokter.requestFocus();
        }        // TODO add your handling code here:
    }//GEN-LAST:event_TCariKomplikasiKeyPressed

    private void TCariKomplikasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TCariKomplikasiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariKomplikasiActionPerformed

    private void BtnDPJPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDPJPKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnDPJPKeyPressed

    private void BtnDPJPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDPJPActionPerformed
        dokter1.isCek();
        dokter1.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter1.setLocationRelativeTo(internalFrame1);
        dokter1.setAlwaysOnTop(false);
        dokter1.setVisible(true);
    }//GEN-LAST:event_BtnDPJPActionPerformed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
        //Valid.pindah(evt,Rencana,Informasi);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        //Valid.pindah(evt,Monitoring,BtnSimpan);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        dokter2.isCek();
        dokter2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter2.setLocationRelativeTo(internalFrame1);
        dokter2.setAlwaysOnTop(false);
        dokter2.setVisible(true);

    }//GEN-LAST:event_BtnPetugasActionPerformed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
//            isRawat();
        }else{
            Valid.pindah(evt,TCari,BtnPetugas);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void jawab_permintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jawab_permintaanKeyPressed
        //  Valid.pindah2(evt,RPK,RBedah);
    }//GEN-LAST:event_jawab_permintaanKeyPressed

    private void saran_tindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saran_tindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_saran_tindakanKeyPressed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        poli1.isCek();
        poli1.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli1.setLocationRelativeTo(internalFrame1);
        poli1.setAlwaysOnTop(false);
        poli1.setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void BtnPoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoliKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoliKeyPressed

    private void catatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_catatanKeyPressed

    }//GEN-LAST:event_catatanKeyPressed

    private void kd_dokter2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kd_dokter2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kd_dokter2KeyPressed

    private void kd_dokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kd_dokterKeyPressed

    }//GEN-LAST:event_kd_dokterKeyPressed

    private void TglAsuhan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TglAsuhan1KeyPressed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed

    }//GEN-LAST:event_BtnPrint1ActionPerformed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isMenu();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
    }//GEN-LAST:event_BtnCariActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    ChkAccor.setSelected(false);
                    isMenu();
                    //getMasalah();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    BtnSimpan.setText("Edit");
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked

        if(tabMode.getRowCount()!=0){
            try {
                ChkAccor.setSelected(false);
                isMenu();

                getKehamilanSebelumnya();

            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbObatMouseClicked

    private void TNoRwActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRwActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRwActionPerformed

    private void nama_dokter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nama_dokter2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nama_dokter2ActionPerformed

    private void isi_permintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_isi_permintaanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_isi_permintaanKeyPressed

    private void hasilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hasilKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_hasilKeyPressed

    private void BtnCopirawatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCopirawatActionPerformed
        // TODO add your handling code here:
        diagnosa_jawaban.setText(diagnosa.getText());        
        //        JOptionPane.showMessageDialog(this, "Salin No. Rawat");        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCopirawatActionPerformed

    private void BtnCopirawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCopirawatKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCopirawatKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            LembarKonsulRanap dialog = new LembarKonsulRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnCariPemeriksaan3;
    private widget.Button BtnCopirawat;
    private widget.Button BtnDPJP;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPoli;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambahMasalah2;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormMasalahRencana;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox Jk;
    private widget.Label LCount;
    private widget.PanelBiasa PanelAccor;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll10;
    private widget.ScrollPane Scroll11;
    private widget.TextBox TCari;
    private widget.TextBox TCariKomplikasi;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRM1;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox TPasien1;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal TglAsuhan;
    private widget.Tanggal TglAsuhan1;
    private widget.TextBox TglLahir;
    private widget.TextArea catatan;
    private widget.TextBox diagnosa;
    private widget.TextBox diagnosa_jawaban;
    private widget.TextArea hasil;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.TextArea isi_permintaan;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel15;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator3;
    private widget.TextArea jawab_permintaan;
    private widget.TextBox kd_dokter;
    private widget.TextBox kd_dokter2;
    private widget.TextBox kd_poli;
    private widget.Label label11;
    private widget.Label label14;
    private widget.Label label16;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label22;
    private widget.TextBox nama_dokter;
    private widget.TextBox nama_dokter2;
    private widget.TextBox nm_poli;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextArea saran_tindakan;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.TextBox status;
    private widget.Table tbKomplikasi;
    private widget.PanelBiasa tbKomplikasiKehamilanSebelumnya;
    private widget.Table tbMasalahKomplikasiKehamilan;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                    "select "
                    + "rujukan_internal_dpjp.no_rawat,"//1
                    + "reg_periksa.no_rkm_medis,"//2
                    + "pasien.nm_pasien,"//3
                    + "pasien.tgl_lahir,"//4
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"//5
                    + "rujukan_internal_dpjp.kd_poli,"    //6
                    + "poliklinik.nm_poli,"      //7
                    + "rujukan_internal_dpjp.kd_dokter,"//8
                    + "dokter.nm_dokter,"//9
                    + "rujukan_internal_dpjp.kd_dokter2,"//10
                    + "rujukan_internal_dpjp.nama_dokter2,"//11
                    + "rujukan_internal_dpjp.diagnosa,"//15
                    + "rujukan_internal_dpjp.catatan,"//12  
                    + "rujukan_internal_dpjp.tanggal,"//13
                    + "rujukan_internal_dpjp.tanggal_jawab,"//14
                    + "rujukan_internal_dpjp.jawab_permintaan,"//16
                    + "rujukan_internal_dpjp.status,"//17
                    + "rujukan_internal_dpjp.saran_tindakan,"//18
                    + "rujukan_internal_dpjp.isi_permintaan,"//19
                    + "rujukan_internal_dpjp.hasil,"//20        
                    + "rujukan_internal_dpjp.diagnosa_jawaban "//21
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join rujukan_internal_dpjp on reg_periksa.no_rawat=rujukan_internal_dpjp.no_rawat "
                    + "inner join dokter on rujukan_internal_dpjp.kd_dokter=dokter.kd_dokter "
                    + "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "
                    + "where rujukan_internal_dpjp.tanggal between ? and ? "+
                 (TCari.getText().trim().equals("")?"":"and (reg_periksa.no_rawat"
                         + " like ? or pasien.no_rkm_medis"
                         + " like ? or pasien.nm_pasien "
                         + " like ? or rujukan_internal_dpjp.kd_poli "
                         + " like ? or poliklinik.nm_poli "
                         + " like ?  or rujukan_internal_dpjp.kd_dokter"
                         + " like ? or dokter.nm_dokter like ?)")+
                 " order by rujukan_internal_dpjp.tanggal");            
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                    ps.setString(9,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),//1
                        rs.getString("no_rkm_medis"),//2
                        rs.getString("nm_pasien"),//3
                        rs.getString("tgl_lahir"),//4
                        rs.getString("jk"),//5
                        rs.getString("kd_poli"),//6
                        rs.getString("nm_poli"),//7
                        rs.getString("kd_dokter"),//8
                        rs.getString("nm_dokter"),//9
                        rs.getString("kd_dokter2"),//10
                        rs.getString("nama_dokter2"),//11
                        rs.getString("diagnosa"),//15
                        rs.getString("catatan"),//12
                        rs.getString("tanggal"),//13/
                        rs.getString("tanggal_jawab"),//14
//                        rs.getString("permintaan"),//15
                        rs.getString("status"),//17
                        rs.getString("jawab_permintaan"),//16
                        rs.getString("saran_tindakan"),//18
                        rs.getString("isi_permintaan"),//19
                        rs.getString("hasil"),//20
                        rs.getString("diagnosa_jawaban")//21    
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
        TglAsuhan.setDate(new Date());
        TglAsuhan.setDate(new Date());
        TglAsuhan1.setDate(new Date());
        TabRawat.setSelectedIndex(0);
    } 

    private void getData() {        
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()); 
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString()); 
            Jk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()); 
            kd_poli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString()); 
            nm_poli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            kd_dokter2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            nama_dokter2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            kd_dokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString()); 
            nama_dokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString()); 
            diagnosa.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString()); 
            catatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString()); 
            Valid.SetTgl2(TglAsuhan,tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
//            Valid.SetTgl2(TglAsuhan1,tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            TglAsuhan1.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
//            CmbJam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            status.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            jawab_permintaan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            saran_tindakan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());            
            isi_permintaan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            hasil.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            diagnosa_jawaban.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
           //Valid.SetTgl2(TglAsuhan,tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());         
            try {
               Valid.tabelKosong(tabModeTindakanKomplikasi);
                
                ps=koneksi.prepareStatement(
                        "select master_konsultasi.kode_masalah,"
                      + "master_konsultasi.nama_masalah "
                      + "from master_konsultasi "
                      + "inner join konsultasi "
                      + "on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                        "where konsultasi.no_rawat=? order by kode_masalah");                
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeTindakanKomplikasi.addRow(new Object[]{true,rs.getString(1),rs.getString(2)});    
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
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
            }
        }
    }

     private void isRawat() {
         Sequel.cariIsi("select kd_dokter from rujukan_internal_dpjp where no_rawat=? ",kd_dokter,TNoRw.getText());
         Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",nama_dokter,kd_dokter.getText());
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"+
                    "pasien.tgl_lahir,pasien.agama,"
                    + "bahasa_pasien.nama_bahasa,"
                    + "cacat_fisik.nama_cacat,"
                    + "rujukan_internal_dpjp.nm_poli,"
                    + "rujukan_internal_dpjp.kd_poli,"
                    + "rujukan_internal_dpjp.tanggal,"
                    + "rujukan_internal_dpjp.kd_dokter2,"
                    + "rujukan_internal_dpjp.nama_dokter2,"
                    + "rujukan_internal_dpjp.diagnosa,"
                    + "rujukan_internal_dpjp.catatan,"
                    + "rujukan_internal_dpjp.isi_permintaan,"
                    + "rujukan_internal_dpjp.hasil,"        
                    + "rujukan_internal_dpjp.diagnosa_jawaban,"        
                    + "reg_periksa.tgl_registrasi "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik "+
//                    "inner join poliklinik on rujukan_internal_dpjp.kd_poli=poliklinik.kd_poli "+
                    "inner join rujukan_internal_dpjp on rujukan_internal_dpjp.no_rawat=reg_periksa.no_rawat "+
//                    "inner join rujukan_internal_dpjp on rujukan_internal_dpjp.no_rawat=reg_periksa.no_rawat "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    kd_poli.setText(rs.getString("kd_poli"));
                    nm_poli.setText(rs.getString("nm_poli"));
                    diagnosa.setText(rs.getString("diagnosa"));
                    catatan.setText(rs.getString("catatan"));
                    isi_permintaan.setText(rs.getString("isi_permintaan"));
                    kd_dokter2.setText(rs.getString("kd_dokter2"));
                    nama_dokter2.setText(rs.getString("nama_dokter2"));
//                    jawab_permintaan.setText(rs.getString("jawab_permintaan"));
//                    saran_tindakan.setText(rs.getString("saran_tindakan"));
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
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
    

 
       private void isForm(){

    }
  public void setNoRm(String norwt, Date tgl1, Date tgl2,String unit) {
        getData();  
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari1.setDate(tgl1);
        DTPCari2.setDate(tgl2);     
        isRawat();
//        tampil();   
       tampilMasalah67();
//        ChkInput.setSelected(true);
        isForm();
        
    }
    


    
    public void isCek(){
        if(akses.getjml2()>=1){
            kd_dokter.setEditable(false);
            BtnPetugas.setEnabled(false);
            kd_dokter.setText(akses.getkode());
            TNoRw.setText(akses.getkode());
            Sequel.cariIsi("select nama from dokter where nip=?", nama_dokter,kd_dokter.getText());
            Sequel.cariIsi("select diagnosa from rujukan_internal_dpjp where no_rawat=?", diagnosa, TNoRw.getText());
            Sequel.cariIsi("select catatan from rujukan_internal_dpjp where no_rawat=?", catatan, TNoRw.getText());
            Sequel.cariIsi("select isi_permintaan from rujukan_internal_dpjp where no_rawat=?", isi_permintaan, TNoRw.getText());
            Sequel.cariIsi("select jawab_permintaan from rujukan_internal_dpjp where no_rawat=?", jawab_permintaan, TNoRw.getText());
            Sequel.cariIsi("select saran_tindakan from rujukan_internal_dpjp where no_rawat=?", saran_tindakan, TNoRw.getText());
            Sequel.cariIsi("select hasil from rujukan_internal_dpjp where no_rawat=?", hasil, TNoRw.getText());
            Sequel.cariIsi("select diagnosa_jawaban from rujukan_internal_dpjp where no_rawat=?", diagnosa_jawaban, TNoRw.getText());
//            getData();
//            getKehamilanSebelumnya();
            if(nama_dokter.getText().equals("")){
                kd_dokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }            
    }

    public void setTampil(){
       TabRawat.setSelectedIndex(1);
       tampil();
    }
    private void tampilMasalah67() {
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
        try {
                Valid.tabelKosong(tabModeTindakanKomplikasi);
                
                ps=koneksi.prepareStatement(
                        "select master_konsultasi.kode_masalah,master_konsultasi.nama_masalah from master_konsultasi "+
                        "inner join konsultasi on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                        "where konsultasi.no_rawat=? order by kode_masalah");
                try {
                    ps.setString(1,TNoRw.getText());
                    //ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    //if(rs.next()){
                    while(rs.next()){
                        tabModeTindakanKomplikasi.addRow(new Object[]{true,rs.getString(1),rs.getString(2)});
                        //RPD4.setText(rs.getString("nama_masalah")+", "+rs.getString("nama_masalah"));
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
                
                
            } catch (Exception e) {
                System.out.println("Notif : "+e);
}
    }
    private void isMenu(){
        if(ChkAccor.isSelected()==false){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(0,HEIGHT));
            FormMenu.setVisible(false);  
            FormMasalahRencana.setVisible(false);
            ChkAccor.setVisible(false);
        }else if(ChkAccor.isSelected()==false){    
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(0,HEIGHT));
            FormMenu.setVisible(false);  
            FormMasalahRencana.setVisible(false);  
            ChkAccor.setVisible(false);
        }
    }
    private void hapus() {
        if(Sequel.queryu2tf("delete from lembar_konsul where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            Sequel.meghapus("konsultasi","no_rawat",
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM1.setText("");
            TPasien1.setText("");
            ChkAccor.setSelected(false);
            isMenu();
            tampil();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }
    
   

private void tampilKehamilanSebelumnya(){
 try{
            jml=0;
            for(i=0;i<tbMasalahKomplikasiKehamilan.getRowCount();i++){
                
                if(tbMasalahKomplikasiKehamilan.getValueAt(i,0).toString().equals("true")){
                    jml++;
                }
            }

            pilih=null;
            pilih=new boolean[jml]; 
            kode=null;
            kode=new String[jml];
            masalah=null;
            masalah=new String[jml];

            index=0;        
            for(i=0;i<tbMasalahKomplikasiKehamilan.getRowCount();i++){
                if(tbMasalahKomplikasiKehamilan.getValueAt(i,0).toString().equals("true")){
                    pilih[index]=true;
                    kode[index]=tbMasalahKomplikasiKehamilan.getValueAt(i,1).toString();
                    masalah[index]=tbMasalahKomplikasiKehamilan.getValueAt(i,2).toString();
                    index++;
                }
            } 

            Valid.tabelKosong(tabModeTindakanKomplikasi);

            for(i=0;i<jml;i++){
                tabModeTindakanKomplikasi.addRow(new Object[] {
                    pilih[i],kode[i],masalah[i]
                });
            }
            ps=koneksi.prepareStatement("select * from master_konsultasi where kode_masalah like ? or nama_masalah like ? order by kode_masalah");
            try {
                ps.setString(1,"%"+TCariKomplikasi.getText().trim()+"%");
                ps.setString(2,"%"+TCariKomplikasi.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabModeTindakanKomplikasi.addRow(new Object[]{false,rs.getString(1),rs.getString(2)});
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
 }

private void getKehamilanSebelumnya() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRM1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()); 
            try {
                Valid.tabelKosong(tabModeDetailKomplikasi);
                ps=koneksi.prepareStatement(
               "select master_konsultasi.kode_masalah,"
                      + "master_konsultasi.nama_masalah "
                      + "from master_konsultasi "
                      + "inner join konsultasi "
                      + "on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                        "where konsultasi.no_rawat=? order by kode_masalah");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeDetailKomplikasi.addRow(new Object[]{rs.getString(1),rs.getString(2)});
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
        }
        
    }
 private void jam(){
  
    }
    private void ganti() {
        if(Sequel.mengedittf("lembar_konsul","no_rawat=?","no_rawat=?,tanggal=?,Anamnesis=?,tiba_diruang_rawat=?,cara_masuk=?,keluhan=?,riwayat_penyakit_keluarga=?,penyakit_selama_kehamilan=?,riwayat_pembedahan=?,alergi=?,keterangan_konsultasi_sebelumnya=?,riwayat_mens_umur=?,riwayat_mens_lamanya=?,riwayat_mens_banyaknya=?,riwayat_mens_siklus=?,riwayat_mens_ket_siklus=?,riwayat_mens_dirasakan=?,ket_riwayat_mens_dirasakan=?,riwayat_perkawinan_status=?,riwayat_perkawinan_ket_status=?,riwayat_perkawinan_usia1=?,riwayat_perkawinan_ket_usia1=?,riwayat_perkawinan_usia2=?,riwayat_perkawinan_ket_usia2=?,riwayat_perkawinan_usia3=?,riwayat_perkawinan_ket_usia3=?,riwayat_persalinan_g=?,riwayat_persalinan_p=?,riwayat_persalinan_a=?,riwayat_persalinan_hidup=?,riwayat_hamil_hpht=?,riwayat_hamil_usiahamil=?,riwayat_hamil_tp=?,riwayat_hamil_imunisasi=?,riwayat_hamil_anc=?,jumlah=?,tempat_periksa=?,keluhan_masalah=?,,riwayat_kb=?,riwayat_kb_lamanya=?,riwayat_kb_konsultasi=?,riwayat_kb_ket_konsultasi=?,riwayat_kb_kapaberhenti=?,riwayat_kb_alasanberhenti=?,riwayat_genekologi=?,riwayat_kebiasaan_obat=?,riwayat_kebiasaan_ket_obat=?,riwayat_kebiasaan_merokok=?,riwayat_kebiasaan_ket_merokok=?,riwayat_kebiasaan_alkohol=?,riwayat_kebiasaan_ket_alkohol=?,riwayat_kebiasaan_narkoba=?,pemeriksaan_kebidanan_mental=?,pemeriksaan_kebidanan_keadaan_umum=?,ket_keadaan_umum=?,pemeriksaan_kebidanan_gcs=?,pemeriksaan_kebidanan_td=?,pemeriksaan_kebidanan_nadi=?,pemeriksaan_kebidanan_rr=?,pemeriksaan_kebidanan_suhu=?,pemeriksaan_kebidanan_spo2=?,pemeriksaan_kebidanan_bb=?,pemeriksaan_kebidanan_tb=?,pemeriksaan_kebidanan_lila=?,pemeriksaan_kebidanan_tfu=?,pemeriksaan_kebidanan_tbj=?,pemeriksaan_kebidanan_letak=?,pemeriksaan_kebidanan_presentasi=?,pemeriksaan_kebidanan_penurunan=?,pemeriksaan_kebidanan_his=?,pemeriksaan_kebidanan_kekuatan=?,pemeriksaan_kebidanan_lamanya=?,pemeriksaan_kebidanan_djj=?,pemeriksaan_kebidanan_ket_djj=?,pemeriksaan_kebidanan_portio=?,pemeriksaan_kebidanan_pembukaan=?,pemeriksaan_kebidanan_ketuban=?,pemeriksaan_kebidanan_hodge=?,pemeriksaan_kebidanan_panggul=?,pemeriksaan_kebidanan_inspekulo=?,pemeriksaan_kebidanan_ket_inspekulo=?,pemeriksaan_kebidanan_lakmus=?,pemeriksaan_kebidanan_ket_lakmus=?,pemeriksaan_kebidanan_ctg=?,kepala=?,muka=?,pemeriksaan_umum_mata=?,pemeriksaan_umum_hidung=?,pemeriksaan_umum_telinga=?,pemeriksaan_umum_mulut=?,pemeriksaan_umum_leher=?,pemeriksaan_umum_dada=?,pemeriksaan_umum_perut=?,pemeriksaan_umum_genitalia=?,pemeriksaan_umum_ekstrimitas=?,pengkajian_fungsi_kemampuan_aktifitas=?,pengkajian_fungsi_berjalan=?,pengkajian_fungsi_ket_berjalan=?,pengkajian_fungsi_aktivitas=?,pengkajian_fungsi_ambulasi=?,pengkajian_fungsi_ekstrimitas_atas=?,pengkajian_fungsi_ket_ekstrimitas_atas=?,pengkajian_fungsi_ekstrimitas_bawah=?,pengkajian_fungsi_ket_ekstrimitas_bawah=?,pengkajian_fungsi_kemampuan_menggenggam=?,pengkajian_fungsi_ket_kemampuan_menggenggam=?,pengkajian_fungsi_koordinasi=?,pengkajian_fungsi_ket_koordinasi=?,pengkajian_fungsi_gangguan_fungsi=?,riwayat_psiko_kondisipsiko=?,riwayat_psiko_adakah_prilaku=?,riwayat_psiko_ket_adakah_prilaku=?,riwayat_psiko_gangguan_jiwa=?,riwayat_psiko_hubungan_pasien=?,riwayat_psiko_tinggal_dengan=?,riwayat_psiko_ket_tinggal_dengan=?,riwayat_psiko_budaya=?,riwayat_psiko_ket_budaya=?,riwayat_psiko_pend_pj=?,riwayat_psiko_edukasi_pada=?,riwayat_psiko_ket_edukasi_pada=?,penilaian_nyeri=?,penilaian_nyeri_penyebab=?,penilaian_nyeri_ket_penyebab=?,penilaian_nyeri_kualitas=?,penilaian_nyeri_ket_kualitas=?,penilaian_nyeri_lokasi=?,penilaian_nyeri_menyebar=?,penilaian_nyeri_skala=?,penilaian_nyeri_waktu=?,penilaian_nyeri_hilang=?,penilaian_nyeri_ket_hilang=?,penilaian_nyeri_diberitahukan_dokter=?,penilaian_nyeri_jam_diberitahukan_dokter=?,penilaian_jatuh_skala1=?,penilaian_jatuh_nilai1=?,penilaian_jatuh_skala2=?,penilaian_jatuh_nilai2=?,penilaian_jatuh_skala3=?,penilaian_jatuh_nilai3=?,penilaian_jatuh_skala4=?,penilaian_jatuh_nilai4=?,penilaian_jatuh_skala5=?,penilaian_jatuh_nilai5=?,penilaian_jatuh_skala6=?,penilaian_jatuh_nilai6=?,penilaian_jatuh_totalnilai=?,skrining_gizi1=?,nilai_gizi1=?,skrining_gizi2=?,nilai_gizi2=?,nilai_total_gizi=?,skrining_gizi_diagnosa_khusus=?,skrining_gizi_ket_diagnosa_khusus=?,skrining_gizi_diketahui_dietisen=?,skrining_gizi_jam_diketahui_dietisen=?,masalah=?,rencana=?,nip1=?,kd_dokter=?",163,new String[]{
                TNoRw.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),         
                kd_dokter.getText(),kd_dokter2.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
             })==true){

                tampil();
                emptTeks();
                TabRawat.setSelectedIndex(1);
        }
    }
}
