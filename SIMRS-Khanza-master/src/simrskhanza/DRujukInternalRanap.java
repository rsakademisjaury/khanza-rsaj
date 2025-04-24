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
import java.awt.event.KeyListener;
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
import keuangan.DlgKamar;


/**
 *
 * @author periwayat_pembedahanustakaan
 */
public final class DRujukInternalRanap extends javax.swing.JDialog {
    
    private final DefaultTableModel tabMode,tabModeTindakanKomplikasi,tabModeDetailKomplikasi;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2,rs3;
    private int i=0,jml=0,index=0;
    private boolean[] pilih; 
    private String[] kode,masalah;
    private DlgCariPoli poli1=new DlgCariPoli(null,true);
    private DlgCariPoli poli2=new DlgCariPoli(null,true);
    private DlgCariDokter dokter=new DlgCariDokter(null,true);
    private DlgCariDokter dokter2=new DlgCariDokter(null,true);
    private DlgCariDokter dpjp_ranap=new DlgCariDokter(null,true);
    private StringBuilder htmlContent;
    private String pilihan="";
    private String finger="";
    private String konsultasiparamput="";
    public  DlgKamar kamar=new DlgKamar(null,true);
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
      public DRujukInternalRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        //hidden tabel master konsul
        Scroll10.setVisible(false);
        BtnTambahMasalah2.setVisible(false);
        BtnCariPemeriksaan3.setVisible(false);
        TCariKomplikasi.setVisible(false);
        
        tabMode=new DefaultTableModel(null,new Object[]{            
          "No.Rawat",//1
          "No.RM",//2
          "Nama Pasien",//3
          "Tgl.Lahir",//4
          "J.K.",//5
          "Kode Poli",//6
          "Nama Poli",//7
          "Kode Dokter1",//8
          "Nama Dokter",//9
          "Kode Dokter2",//10
          "Nama Dokter",//11
          "Catatan",//11
          "Tanggal",//12
          "Tgl.Jawab",//13
          "Jam.Jawab",//14
          "Permintaan",//15
          "Saran Tindakan"//16

         }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
//        tbObat.setModel(tabMode);
//        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
//        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
//            TableColumn column = tbObat.getColumnModel().getColumn(i);
//            if(i==0){
//                column.setPreferredWidth(105);
//            }else if(i==1){
//                column.setPreferredWidth(65);
//            }else if(i==2){
//                column.setPreferredWidth(160);
//            }else if(i==3){
//                column.setPreferredWidth(65);
//            }else if(i==4){
//                column.setPreferredWidth(25);
//            }else if(i==5){
//                column.setPreferredWidth(85);
//            }else if(i==6){
//                column.setPreferredWidth(150);
//            }else if(i==7){
//                column.setPreferredWidth(85);
//            }else if(i==8){
//                column.setPreferredWidth(150);
//            }else if(i==9){
//                column.setPreferredWidth(90);
//            }else if(i==10){
//                column.setPreferredWidth(150);
//            }else if(i==11){
//                column.setPreferredWidth(117);
//            }else if(i==12){
//                column.setPreferredWidth(117);
//            }else if(i==13){
//                column.setPreferredWidth(117);
//            }else if(i==14){
//                column.setPreferredWidth(117);
//            }else if(i==15){
//                column.setPreferredWidth(117);
//            }
        }
//        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
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
        //
        tabModeDetailKomplikasi=new DefaultTableModel(null,new Object[]{
                "Kode","KONSULTASI"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
//      
        
        //sampai sini
        poli2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli2.getTable().getSelectedRow()!= -1){                    
                    KdPoliAsal.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),0).toString());
                    NmPoliAsal.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),1).toString());
                    
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
                        nm_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());  
                      
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
                    nm_dokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());  
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
              dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){ 
                    kd_dokter2.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    nama_dokter2.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());  
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
   
            dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){ 
                    kd_dokter2.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    nama_dokter2.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());  
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
            
        kamar.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
   //             if(akses.getform().equals("SuratKonsul")){
                    if(kamar.getTable().getSelectedRow()!= -1){   
                        KdPoliAsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),1).toString());
                        NmPoliAsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),3).toString());
                }
  //          }
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

        kamar.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("DlgKamarInap")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        kamar.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });            
//        ChkAccor.setSelected(false);
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
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        tbKomplikasiKehamilanSebelumnya = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        kd_dokter = new widget.TextBox();
        nm_dokter = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        label11 = new widget.Label();
        jLabel11 = new widget.Label();
        TglAsuhan = new widget.Tanggal();
        label16 = new widget.Label();
        kd_dokter2 = new widget.TextBox();
        nama_dokter2 = new widget.TextBox();
        BtnDPJP = new widget.Button();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        Scroll10 = new widget.ScrollPane();
        tbMasalahKomplikasiKehamilan = new widget.Table();
        TCariKomplikasi = new widget.TextBox();
        label15 = new widget.Label();
        BtnCariPemeriksaan3 = new widget.Button();
        BtnTambahMasalah2 = new widget.Button();
        jLabel12 = new widget.Label();
        nm_poli = new widget.TextBox();
        kd_poli = new widget.TextBox();
        BtnPoli = new widget.Button();
        label18 = new widget.Label();
        scrollPane4 = new widget.ScrollPane();
        catatan = new widget.TextArea();
        status = new widget.TextBox();
        jawab_permintaan = new widget.TextArea();
        saran_tindakan = new widget.TextArea();
        label19 = new widget.Label();
        TglAsuhan1 = new widget.Tanggal();
        BtnPoli1 = new widget.Button();
        scrollPane6 = new widget.ScrollPane();
        isi_permintaan = new widget.TextArea();
        diagnosa = new widget.TextBox();
        KdPoliAsal = new widget.TextBox();
        jLabel27 = new widget.Label();
        NmPoliAsal = new widget.TextBox();
        BtnPoli2 = new widget.Button();

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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), ":: Form Konsultasi Antar DPJP ::"));
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1150, 365));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setMaximumSize(new java.awt.Dimension(47483647, 47483647));
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        tbKomplikasiKehamilanSebelumnya.setBackground(new java.awt.Color(255, 255, 255));
        tbKomplikasiKehamilanSebelumnya.setBorder(null);
        tbKomplikasiKehamilanSebelumnya.setName("tbKomplikasiKehamilanSebelumnya"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.setPreferredSize(new java.awt.Dimension(700, 200));
        tbKomplikasiKehamilanSebelumnya.setLayout(null);

        TNoRw.setForeground(new java.awt.Color(0, 0, 0));
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
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
        TNoRM.setText("123456");
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(TNoRM);
        TNoRM.setBounds(230, 10, 60, 23);

        label14.setForeground(new java.awt.Color(0, 0, 0));
        label14.setText(" dr Perujuk :");
        label14.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        tbKomplikasiKehamilanSebelumnya.add(label14);
        label14.setBounds(0, 70, 80, 23);

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
        kd_dokter.setBounds(90, 70, 108, 23);

        nm_dokter.setEditable(false);
        nm_dokter.setForeground(new java.awt.Color(0, 0, 0));
        nm_dokter.setName("nm_dokter"); // NOI18N
        nm_dokter.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(nm_dokter);
        nm_dokter.setBounds(200, 70, 330, 23);

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
        BtnPetugas.setBounds(530, 70, 28, 23);

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel8.setName("jLabel8"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel8);
        jLabel8.setBounds(560, 10, 70, 20);

        TglLahir.setEditable(false);
        TglLahir.setForeground(new java.awt.Color(0, 0, 0));
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(TglLahir);
        TglLahir.setBounds(640, 10, 100, 24);

        Jk.setEditable(false);
        Jk.setForeground(new java.awt.Color(0, 0, 0));
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(Jk);
        Jk.setBounds(790, 10, 80, 23);

        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("No.Rawat :");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel10.setName("jLabel10"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel10);
        jLabel10.setBounds(0, 10, 80, 23);

        label11.setForeground(new java.awt.Color(0, 0, 0));
        label11.setText("Tanggal :");
        label11.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        tbKomplikasiKehamilanSebelumnya.add(label11);
        label11.setBounds(570, 70, 60, 20);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel11);
        jLabel11.setBounds(750, 10, 30, 23);

        TglAsuhan.setForeground(new java.awt.Color(0, 0, 0));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-04-2024 13:03:08" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TglAsuhan);
        TglAsuhan.setBounds(640, 70, 140, 20);

        label16.setForeground(new java.awt.Color(0, 0, 0));
        label16.setText("Kepada dr :");
        label16.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(70, 23));
        tbKomplikasiKehamilanSebelumnya.add(label16);
        label16.setBounds(570, 100, 60, 23);

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
        kd_dokter2.setBounds(640, 100, 108, 23);

        nama_dokter2.setEditable(false);
        nama_dokter2.setForeground(new java.awt.Color(0, 0, 0));
        nama_dokter2.setName("nama_dokter2"); // NOI18N
        nama_dokter2.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(nama_dokter2);
        nama_dokter2.setBounds(750, 100, 330, 23);

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
        BtnDPJP.setBounds(1080, 100, 28, 23);

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
        Scroll10.setBounds(1160, 400, 320, 120);

        TCariKomplikasi.setEditable(false);
        TCariKomplikasi.setToolTipText("Alt+C");
        TCariKomplikasi.setEnabled(false);
        TCariKomplikasi.setName("TCariKomplikasi"); // NOI18N
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
        TCariKomplikasi.setBounds(1170, 540, 210, 20);

        label15.setForeground(new java.awt.Color(0, 0, 0));
        label15.setText("Diagnosa :");
        label15.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label15);
        label15.setBounds(0, 130, 80, 23);

        BtnCariPemeriksaan3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariPemeriksaan3.setMnemonic('1');
        BtnCariPemeriksaan3.setToolTipText("Alt+1");
        BtnCariPemeriksaan3.setEnabled(false);
        BtnCariPemeriksaan3.setName("BtnCariPemeriksaan3"); // NOI18N
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
        BtnCariPemeriksaan3.setBounds(1380, 540, 30, 20);

        BtnTambahMasalah2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahMasalah2.setMnemonic('3');
        BtnTambahMasalah2.setToolTipText("Alt+3");
        BtnTambahMasalah2.setEnabled(false);
        BtnTambahMasalah2.setName("BtnTambahMasalah2"); // NOI18N
        BtnTambahMasalah2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahMasalah2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahMasalah2ActionPerformed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnTambahMasalah2);
        BtnTambahMasalah2.setBounds(1420, 540, 30, 20);

        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Poli Tujuan :");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel12.setName("jLabel12"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel12);
        jLabel12.setBounds(0, 100, 80, 23);

        nm_poli.setForeground(new java.awt.Color(0, 0, 0));
        nm_poli.setHighlighter(null);
        nm_poli.setName("nm_poli"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(nm_poli);
        nm_poli.setBounds(200, 100, 330, 23);

        kd_poli.setForeground(new java.awt.Color(0, 0, 0));
        kd_poli.setHighlighter(null);
        kd_poli.setName("kd_poli"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(kd_poli);
        kd_poli.setBounds(90, 100, 108, 23);

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
        BtnPoli.setBounds(530, 100, 28, 23);

        label18.setForeground(new java.awt.Color(0, 0, 0));
        label18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label18.setText("Pemeriksaan yang telah dilakukan :");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label18);
        label18.setBounds(20, 160, 180, 23);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane4.setName("scrollPane4"); // NOI18N

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
        scrollPane4.setBounds(570, 180, 540, 80);

        status.setEditable(false);
        status.setText("belum");
        status.setHighlighter(null);
        status.setName("status"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(status);
        status.setBounds(1230, 110, 80, 23);

        jawab_permintaan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jawab_permintaan.setColumns(20);
        jawab_permintaan.setRows(5);
        jawab_permintaan.setName("jawab_permintaan"); // NOI18N
        jawab_permintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jawab_permintaanKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(jawab_permintaan);
        jawab_permintaan.setBounds(150, 440, 162, 72);

        saran_tindakan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        saran_tindakan.setColumns(20);
        saran_tindakan.setRows(5);
        saran_tindakan.setName("saran_tindakan"); // NOI18N
        saran_tindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saran_tindakanKeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(saran_tindakan);
        saran_tindakan.setBounds(280, 440, 162, 72);

        label19.setForeground(new java.awt.Color(0, 0, 0));
        label19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label19.setText("Terapi yang diberikan :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(60, 23));
        tbKomplikasiKehamilanSebelumnya.add(label19);
        label19.setBounds(570, 160, 150, 23);

        TglAsuhan1.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "22-04-2024 13:03:09" }));
        TglAsuhan1.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan1.setName("TglAsuhan1"); // NOI18N
        TglAsuhan1.setOpaque(false);
        TglAsuhan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhan1KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(TglAsuhan1);
        TglAsuhan1.setBounds(130, 370, 140, 20);

        BtnPoli1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli1.setMnemonic('2');
        BtnPoli1.setToolTipText("Alt+2");
        BtnPoli1.setName("BtnPoli1"); // NOI18N
        BtnPoli1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPoli1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoli1ActionPerformed(evt);
            }
        });
        BtnPoli1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoli1KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnPoli1);
        BtnPoli1.setBounds(560, 450, 28, 23);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane6.setName("scrollPane6"); // NOI18N

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
        scrollPane6.setBounds(20, 180, 540, 80);

        diagnosa.setForeground(new java.awt.Color(0, 0, 0));
        diagnosa.setName("diagnosa"); // NOI18N
        diagnosa.setPreferredSize(new java.awt.Dimension(207, 23));
        tbKomplikasiKehamilanSebelumnya.add(diagnosa);
        diagnosa.setBounds(90, 130, 990, 23);

        KdPoliAsal.setEditable(false);
        KdPoliAsal.setHighlighter(null);
        KdPoliAsal.setName("KdPoliAsal"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(KdPoliAsal);
        KdPoliAsal.setBounds(90, 40, 108, 23);

        jLabel27.setText("Ruangan :");
        jLabel27.setName("jLabel27"); // NOI18N
        tbKomplikasiKehamilanSebelumnya.add(jLabel27);
        jLabel27.setBounds(0, 40, 80, 23);

        NmPoliAsal.setEditable(false);
        NmPoliAsal.setHighlighter(null);
        NmPoliAsal.setName("NmPoliAsal"); // NOI18N
        NmPoliAsal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NmPoliAsalActionPerformed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(NmPoliAsal);
        NmPoliAsal.setBounds(200, 40, 330, 23);

        BtnPoli2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli2.setMnemonic('X');
        BtnPoli2.setToolTipText("Alt+X");
        BtnPoli2.setName("BtnPoli2"); // NOI18N
        BtnPoli2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoli2ActionPerformed(evt);
            }
        });
        BtnPoli2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoli2KeyPressed(evt);
            }
        });
        tbKomplikasiKehamilanSebelumnya.add(BtnPoli2);
        BtnPoli2.setBounds(530, 40, 28, 23);

        scrollInput.setViewportView(tbKomplikasiKehamilanSebelumnya);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Permintaan Konsultasi Antar Poliklinik", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        TabRawat.getAccessibleContext().setAccessibleName("Permintaan Konsultasi Antar DPJP");

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"Pasien");
        }else if(nm_poli.getText().trim().equals("")||kd_poli.getText().trim().equals("")){
            Valid.textKosong(kd_poli,"poliklinik");
        }else if(kd_dokter.getText().trim().equals("")||nm_dokter.getText().trim().equals("")){
            Valid.textKosong(kd_dokter,"dokter");
        }else if(KdPoliAsal.getText().trim().equals("")||NmPoliAsal.getText().trim().equals("")){
            Valid.textKosong(NmPoliAsal,"Asal Ruangan");    
        }else{          
            if(Sequel.menyimpantf("rujukan_internal_dpjp","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Rujukan Sama",16,new String[]{
                TNoRw.getText(),
                kd_dokter.getText(),
                kd_poli.getText(),
                nm_poli.getText(),
                kd_dokter2.getText(),
                nama_dokter2.getText(),
                diagnosa.getText(),                
                catatan.getText(),
                Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
                status.getText(),
                Valid.SetTgl(TglAsuhan1.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),            
                jawab_permintaan.getText(),
                saran_tindakan.getText(),                
                isi_permintaan.getText(),
//                hasil.getText(),
//                diagnosa_jawaban.getText(),
                })==true){
//                tbMasalahKomplikasiKehamilan.setValueAt(false,i,0);
                 for (i = 0; i < tbMasalahKomplikasiKehamilan.getRowCount(); i++) {
                        if(tbMasalahKomplikasiKehamilan.getValueAt(i,0).toString().equals("true")){
                        Sequel.menyimpan2("konsultasi","?,?",2,new String[]{TNoRw.getText(),tbMasalahKomplikasiKehamilan.getValueAt(i,1).toString()});
               
            }                      
        }  
                  BtnKeluarActionPerformed(evt);
            }
       }   
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        kd_poli.setText("");
        nm_poli.setText("");
        kd_dokter2.setText("");
        nama_dokter2.setText("");
        kd_dokter.setText("");
        nm_dokter.setText("");
        catatan.setText("");
         for (i = 0; i < tbMasalahKomplikasiKehamilan.getRowCount(); i++) {
            tbMasalahKomplikasiKehamilan.setValueAt(false,i,0);
        }
//        tbMasalahKomplikasiKehamilan.setSelectedIndex(0);
//        TabRawat.setSelectedIndex(0);
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }
//        else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
    
    }//GEN-LAST:event_TabRawatMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

        tampilKehamilanSebelumnya();
    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

    }//GEN-LAST:event_jMenuItem1ActionPerformed

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
        akses.setform("DRujukInternalRanap");
        dokter.emptTeks();
        dokter.isCek();
        dokter.TCari.requestFocus();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
//        dokter1.isCek();
//        dokter1.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
//        dokter1.setLocationRelativeTo(internalFrame1);
//        dokter1.setAlwaysOnTop(false);
//        dokter1.setVisible(true);
    }//GEN-LAST:event_BtnDPJPActionPerformed

    private void kd_dokter2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kd_dokter2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kd_dokter2KeyPressed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
        //Valid.pindah(evt,Rencana,Informasi);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void kd_dokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kd_dokterKeyPressed

    }//GEN-LAST:event_kd_dokterKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{
//            Valid.pindah(evt,TCari,BtnPetugas);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

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

    private void jawab_permintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jawab_permintaanKeyPressed
        //  Valid.pindah2(evt,RPK,RBedah);
    }//GEN-LAST:event_jawab_permintaanKeyPressed

    private void saran_tindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saran_tindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_saran_tindakanKeyPressed

    private void TglAsuhan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TglAsuhan1KeyPressed

    private void BtnPoli1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoli1ActionPerformed
        dokter2.isCek();
        dokter2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter2.setLocationRelativeTo(internalFrame1);
        dokter2.setAlwaysOnTop(false);
        dokter2.setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoli1ActionPerformed

    private void BtnPoli1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoli1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoli1KeyPressed

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

    private void isi_permintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_isi_permintaanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_isi_permintaanKeyPressed

    private void NmPoliAsalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NmPoliAsalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NmPoliAsalActionPerformed

    private void BtnPoli2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoli2ActionPerformed
        if(Sequel.cariInteger("select count(kamar_inap.no_rawat) from kamar_inap where kamar_inap.no_rawat=?",TNoRw.getText())>0){
            kamar.load();
            kamar.isCek();
            kamar.emptTeks();
            kamar.tampil();
            kamar.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kamar.setLocationRelativeTo(internalFrame1);
            kamar.setVisible(true);
        }else{
            poli1.isCek();
            poli1.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            poli1.setLocationRelativeTo(internalFrame1);
            poli1.setVisible(true);
        }
    }//GEN-LAST:event_BtnPoli2ActionPerformed

    private void BtnPoli2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoli2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoli2KeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DRujukInternalRanap dialog = new DRujukInternalRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCariPemeriksaan3;
    private widget.Button BtnDPJP;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPoli;
    private widget.Button BtnPoli1;
    private widget.Button BtnPoli2;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambahMasalah2;
    private widget.TextBox Jk;
    private widget.TextBox KdPoliAsal;
    private widget.TextBox NmPoliAsal;
    private widget.ScrollPane Scroll10;
    private widget.TextBox TCariKomplikasi;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal TglAsuhan;
    private widget.Tanggal TglAsuhan1;
    private widget.TextBox TglLahir;
    private widget.TextArea catatan;
    private widget.TextBox diagnosa;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.TextArea isi_permintaan;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel27;
    private widget.Label jLabel8;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator3;
    private widget.TextArea jawab_permintaan;
    private widget.TextBox kd_dokter;
    private widget.TextBox kd_dokter2;
    private widget.TextBox kd_poli;
    private widget.Label label11;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.Label label18;
    private widget.Label label19;
    private widget.TextBox nama_dokter2;
    private widget.TextBox nm_dokter;
    private widget.TextBox nm_poli;
    private widget.panelisi panelGlass8;
    private widget.TextArea saran_tindakan;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane6;
    private widget.TextBox status;
    private widget.PanelBiasa tbKomplikasiKehamilanSebelumnya;
    private widget.Table tbMasalahKomplikasiKehamilan;
    // End of variables declaration//GEN-END:variables

    
    public void emptTeks() {
        
        
        
        TglAsuhan.setDate(new Date());
     
        
        
        TglAsuhan.setDate(new Date());
//        TglAsuhan2.setDate(new Date());
        

     
     
      
        TabRawat.setSelectedIndex(0);
    
     
    } 

   
    
    private void isRawat() {
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
      
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRw.getText());
        try {
            ps=koneksi.prepareStatement(
                    "select  reg_periksa.no_rawat,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,"
                  + "reg_periksa.kd_dokter,dokter.nm_dokter,poliklinik.nm_poli,reg_periksa.kd_poli "+
                    "from reg_periksa " +
                            "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                          + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                             "where reg_periksa.no_rawat=?");
//                    "select  nm_pasien, if(jk='L','Laki-Laki','Perempuan') as jk,tgl_lahir,agama,pekerjaan,"+
//                     "reg_periksa.kd_dokter,dokter.nm_dokter,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat, penjab.png_jawab "+
//                    "from pasien " +
//                    "inner join penjab on penjab.kd_pj=pasien.kd_pj "+
//                    "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
//                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik "+
//                    "where no_rkm_medis=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
//                    kd_poli.setText(rs.getString("kd_poli"));
//                    nm_poli.setText(rs.getString("nm_poli"));
                    kd_dokter2.setText(rs.getString("kd_dokter"));
                    nama_dokter2.setText(rs.getString("nm_dokter"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                   
                    //CacatFisik.setText(rs.getString("nama_cacat"));
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
     private void isPsien() {
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }
     private void isPsien2() {
        Sequel.cariIsi("select tgl_lahir from pasien where no_rkm_medis='"+TNoRM.getText()+"' ",TglLahir);
    }
    
    public void setNoRm(String norw,String norm,String namapasien,int lebar,int tinggi) {
        TNoRw.setText(norw);
        TNoRM.setText(norm);
        TPasien.setText(namapasien);  
      isRawat();
        isPsien();        
         isPsien2();
        
    }
    


    
    public void isCek(){
        if(akses.getjml2()>=1){
            kd_dokter.setEditable(false);
            BtnPoli1.setEnabled(false);
            kd_dokter.setText(akses.getkode());
            Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", nm_dokter,kd_dokter.getText());
            if(nm_dokter.getText().equals("")){
                kd_dokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }            
    }

    public void setTampil(){
       TabRawat.setSelectedIndex(1);

    }
    
    private void isMenu(){

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

 private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            @Override
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                // Membuat Date
                //Date dt = new Date();
                Date now = Calendar.getInstance().getTime();

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
//                CmbDetik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }
 
}
