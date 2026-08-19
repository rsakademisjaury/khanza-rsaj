/*
 * Kontribusi dari Abdul Wahid, RSUD Cipayung Jakarta Timur
 */


package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPegawai;
import kepegawaian.DlgCariPetugas;
import rekammedis.DlgMasterRencanaKeperawatan;


/**
 *
 * @author perpustakaan
 */
public final class EWSRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeMasalah,tabModeDetailMasalah;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0,jml=0,index=0;
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    public  DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
    private boolean[] pilih; 
    private String[] kode,masalah;
    private String masalahkeperawatan=""; 
    private StringBuilder htmlContent;
    public DlgMasterRencanaKeperawatan masterr=new DlgMasterRencanaKeperawatan(null,false);
    private SimpleDateFormat tanggalNow = new SimpleDateFormat("yyyy-MM-dd");
//    private SimpleDateFormat tanggalNow = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat jamNow = new SimpleDateFormat("HH:mm:ss");
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public EWSRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal Lahir","J.K.","NIP","Nama Petugas","Tanggal","Jam","Pernafasan","Score Pernafasan","Saturasi Oksigen",
            "Score Saturasi Oksigen","Penggunaan Alat Bantu O2","Score Penggunaan Alat Bantu O2","Suhu","Score Suhu","Denyut Jantung","Score Denyut Jantung",
            "Tekanan Darah Sistolik","Score Tekanan Darah Sistolik","Kesadaran","Score Kesadaran","Total Score","Klasifikasi","Respon Klinis","Tindakan","Frekuensi Monitoring",
            "Skor Nyeri","BB(Kg)","TB(Cm)","Lingkar Kepala","Lingkar Perut","Masuk(Peroral/NGT)","Masuk(Parenteral/Transfusi)","Jumlah Masuk","Keluar(Fases)","Keluar(Urine)",
            "Keluar(Muntah/NGT)","Keluar(Drain/Darah)","Keluar(IWL)","Jumlah Keluar","Balance Cairan"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 43; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(50);
            }else if(i==4){
                column.setPreferredWidth(60);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else if(i==6){
                column.setPreferredWidth(90);
            }else if(i==7){
                column.setPreferredWidth(65);
            }else if(i==8){
                column.setPreferredWidth(120);
            }else if(i==9){
                column.setPreferredWidth(90);
            }else if(i==10){
                column.setPreferredWidth(35);
            }else if(i==11){
                column.setPreferredWidth(40);
            }else if(i==12){
                column.setPreferredWidth(35);
            }else if(i==13){
                column.setPreferredWidth(40);
            }else if(i==14){
                column.setPreferredWidth(35);
            }else if(i==15){
                column.setPreferredWidth(35);
            }else if(i==16){
                column.setPreferredWidth(35);
            }else if(i==17){
                column.setPreferredWidth(35);
            }else if(i==18){
                column.setPreferredWidth(180);
            }else if(i==19){
                column.setPreferredWidth(150);
            }else if(i==20){
                column.setPreferredWidth(150);
            }else if(i==21){
                column.setPreferredWidth(150);
            }else if(i==22){
                column.setPreferredWidth(100);
            }else if(i==23){
                column.setPreferredWidth(60);
            }else if(i==24){
                column.setPreferredWidth(90);
            }else if(i==25){
                column.setPreferredWidth(60);
            }else if(i==26){
                column.setPreferredWidth(90);
            }else if(i==27){
                column.setPreferredWidth(60);
            }else if(i==28){
                column.setPreferredWidth(180);
            }else if(i==29){
                column.setPreferredWidth(150);
            }else if(i==30){
                column.setPreferredWidth(150);
            }else if(i==31){
                column.setPreferredWidth(150);
            }else if(i==32){
                column.setPreferredWidth(100);
            }else if(i==33){
                column.setPreferredWidth(60);
            }else if(i==34){
                column.setPreferredWidth(90);
            }else if(i==35){
                column.setPreferredWidth(60);
            }else if(i==36){
                column.setPreferredWidth(90);
            }else if(i==37){
                column.setPreferredWidth(60);
            }else if(i==38){
                column.setPreferredWidth(60);
            }else if(i==39){
                column.setPreferredWidth(60);
            }else if(i==40){
                column.setPreferredWidth(60);
            }else if(i==41){
                column.setPreferredWidth(60);
            }else if(i==42){
                column.setPreferredWidth(60);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeMasalah=new DefaultTableModel(null,new Object[]{
                "P","KODE","MASALAH KEPERAWATAN"
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
//        tbMasalahKeperawatan.setModel(tabModeMasalah);
//
//        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
//        tbMasalahKeperawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
//        tbMasalahKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        
//        for (i = 0; i < 3; i++) {
//            TableColumn column = tbMasalahKeperawatan.getColumnModel().getColumn(i);
//            if(i==0){
//                column.setPreferredWidth(20);
//            }else if(i==1){
//                column.setMinWidth(0);
//                column.setMaxWidth(0);
//            }else if(i==2){
//                column.setPreferredWidth(350);
//            }
//        }
//        tbMasalahKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailMasalah=new DefaultTableModel(null,new Object[]{
                "Kode","Masalah Keperawatan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
//        tbMasalahDetailMasalah.setModel(tabModeDetailMasalah);
//
//        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
//        tbMasalahDetailMasalah.setPreferredScrollableViewportSize(new Dimension(500,500));
//        tbMasalahDetailMasalah.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//
//        for (i = 0; i < 2; i++) {
//            TableColumn column = tbMasalahDetailMasalah.getColumnModel().getColumn(i);
//            if(i==0){
//                column.setMinWidth(0);
//                column.setMaxWidth(0);
//            }else if(i==1){
//                column.setPreferredWidth(420);
//            }
//        }
//        tbMasalahDetailMasalah.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        Pernafasan1.setDocument(new batasInput((byte)5).getKata(Pernafasan1));
        Saturasi1.setDocument(new batasInput((byte)5).getKata(Saturasi1));
        Suhu1.setDocument(new batasInput((byte)5).getKata(Suhu1));
        Denyut1.setDocument(new batasInput((byte)5).getKata(Denyut1));
        Tekanan1.setDocument(new batasInput((byte)5).getKata(Tekanan1));
        BB.setDocument(new batasInput((byte)5).getKata(BB));
        TB.setDocument(new batasInput((byte)5).getKata(TB));
        LK.setDocument(new batasInput((byte)5).getKata(LK));
        LP.setDocument(new batasInput((byte)5).getKata(LP));
        Keluar1.setDocument(new batasInput((byte)5).getKata(Keluar1));
        Keluar2.setDocument(new batasInput((byte)5).getKata(Keluar2));
        Keluar3.setDocument(new batasInput((byte)5).getKata(Keluar3));
        Keluar4.setDocument(new batasInput((byte)5).getKata(Keluar4));
        Keluar5.setDocument(new batasInput((byte)5).getKata(Keluar5));
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
        
        petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(petugas.getTable().getSelectedRow()!= -1){ 
                    KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                    NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());   
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
                    KdPetugas.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmPetugas.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    KdPetugas.requestFocus();
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
        
        pegawai.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("EWSRajal")){
                    if(pegawai.getTable().getSelectedRow()!= -1){   
//                        KdPeg.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),0).toString());
//                        TPegawai.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),1).toString());
//                        Jabatan.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),3).toString());
//                        KdPeg.requestFocus();  
                        KdPetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),0).toString());
                        NmPetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),1).toString());
                        KdPetugas.requestFocus();
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
        
//        BB.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                isBMI();
//            }
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                isBMI();
//            }
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                isBMI();
//            }
//        });
//        
//        TB.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                isBMI();
//            }
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                isBMI();
//            }
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                isBMI();
//            }
//        });
        
//        masterr.addWindowListener(new WindowListener() {
//            @Override
//            public void windowOpened(WindowEvent e) {}
//            @Override
//            public void windowClosing(WindowEvent e) {}
//            @Override
//            public void windowClosed(WindowEvent e) {
//                if(akses.getform().equals("RMPenilaianAwalKeperawatanRalan")){
//                    if(masterr.getTable().getSelectedRow()!= -1){
//                        KetFisik.setText(masterr.getTable().getValueAt(masterr.getTable().getSelectedRow(),3).toString());
//                    }  
//                    KetFisik.requestFocus();
//                }
//            }
//            @Override
//            public void windowIconified(WindowEvent e) {}
//            @Override
//            public void windowDeiconified(WindowEvent e) {}
//            @Override
//            public void windowActivated(WindowEvent e) {}
//            @Override
//            public void windowDeactivated(WindowEvent e) {}
//        });
        
        masterr.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("RMPenilaianAwalKeperawatanRalan")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        masterr.dispose();
                    }                
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
        
        
//        ChkAccor.setSelected(false);
//        isMenu();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        jLabel11 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel219 = new widget.Label();
        jLabel220 = new widget.Label();
        ScorePernafasan = new widget.TextBox();
        jLabel222 = new widget.Label();
        ScoreSaturasi = new widget.TextBox();
        jLabel224 = new widget.Label();
        jLabel225 = new widget.Label();
        Alat = new widget.ComboBox();
        jLabel227 = new widget.Label();
        ScoreAlat = new widget.TextBox();
        jLabel228 = new widget.Label();
        jLabel230 = new widget.Label();
        ScoreSuhu = new widget.TextBox();
        jLabel231 = new widget.Label();
        jLabel233 = new widget.Label();
        ScoreDenyut = new widget.TextBox();
        jLabel234 = new widget.Label();
        jLabel236 = new widget.Label();
        ScoreTekanan = new widget.TextBox();
        jLabel237 = new widget.Label();
        Total = new widget.TextBox();
        jLabel238 = new widget.Label();
        Kesadaran = new widget.ComboBox();
        jLabel240 = new widget.Label();
        ScoreKesadaran = new widget.TextBox();
        jLabel106 = new widget.Label();
        scrollPane17 = new widget.ScrollPane();
        Respon = new widget.TextArea();
        jLabel262 = new widget.Label();
        jLabel241 = new widget.Label();
        Klasifikasi = new widget.TextBox();
        scrollPane18 = new widget.ScrollPane();
        Tindakan = new widget.TextArea();
        jLabel243 = new widget.Label();
        jLabel244 = new widget.Label();
        Frekuensi = new widget.TextBox();
        Suhu1 = new widget.TextBox();
        Pernafasan1 = new widget.TextBox();
        Saturasi1 = new widget.TextBox();
        Denyut1 = new widget.TextBox();
        Tekanan1 = new widget.TextBox();
        PanelWall = new usu.widget.glass.PanelGlass();
        jLabel85 = new widget.Label();
        SkalaNyeri = new widget.ComboBox();
        jLabel245 = new widget.Label();
        BB = new widget.TextBox();
        jLabel246 = new widget.Label();
        TB = new widget.TextBox();
        jLabel247 = new widget.Label();
        jLabel248 = new widget.Label();
        jLabel249 = new widget.Label();
        LK = new widget.TextBox();
        jLabel250 = new widget.Label();
        LP = new widget.TextBox();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel53 = new widget.Label();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel54 = new widget.Label();
        jLabel55 = new widget.Label();
        jLabel251 = new widget.Label();
        Masuk1 = new widget.TextBox();
        jLabel252 = new widget.Label();
        Masuk2 = new widget.TextBox();
        jLabel253 = new widget.Label();
        JumlahMasuk = new widget.TextBox();
        jLabel56 = new widget.Label();
        jLabel254 = new widget.Label();
        Keluar1 = new widget.TextBox();
        jLabel255 = new widget.Label();
        Keluar2 = new widget.TextBox();
        jLabel256 = new widget.Label();
        Keluar3 = new widget.TextBox();
        jLabel257 = new widget.Label();
        Keluar4 = new widget.TextBox();
        jLabel258 = new widget.Label();
        Keluar5 = new widget.TextBox();
        jLabel259 = new widget.Label();
        JumlahKeluar = new widget.TextBox();
        jLabel260 = new widget.Label();
        BC = new widget.TextBox();
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

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Early Warning Score Dewasa Rawat Inap ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(870, 800));
        FormInput.setLayout(null);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TPasienActionPerformed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        label14.setText("Petugas :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.setPreferredSize(new java.awt.Dimension(80, 23));
        KdPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugasKeyPressed(evt);
            }
        });
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(74, 40, 140, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        NmPetugas.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(215, 40, 220, 23);

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
        BtnDokter.setBounds(430, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(740, 10, 30, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 880, 1);

        jLabel219.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel219.setText("1. Pernafasan");
        jLabel219.setName("jLabel219"); // NOI18N
        FormInput.add(jLabel219);
        jLabel219.setBounds(30, 110, 160, 23);

        jLabel220.setText("Respon Klinis :");
        jLabel220.setName("jLabel220"); // NOI18N
        FormInput.add(jLabel220);
        jLabel220.setBounds(470, 160, 75, 23);

        ScorePernafasan.setEditable(false);
        ScorePernafasan.setFocusTraversalPolicyProvider(true);
        ScorePernafasan.setName("ScorePernafasan"); // NOI18N
        FormInput.add(ScorePernafasan);
        ScorePernafasan.setBounds(400, 110, 60, 23);

        jLabel222.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel222.setText("2. Saturasi Oksigen(%)");
        jLabel222.setName("jLabel222"); // NOI18N
        FormInput.add(jLabel222);
        jLabel222.setBounds(30, 140, 160, 23);

        ScoreSaturasi.setEditable(false);
        ScoreSaturasi.setToolTipText("");
        ScoreSaturasi.setFocusTraversalPolicyProvider(true);
        ScoreSaturasi.setName("ScoreSaturasi"); // NOI18N
        FormInput.add(ScoreSaturasi);
        ScoreSaturasi.setBounds(400, 140, 60, 23);

        jLabel224.setText("Score EWS :");
        jLabel224.setName("jLabel224"); // NOI18N
        FormInput.add(jLabel224);
        jLabel224.setBounds(320, 140, 75, 23);

        jLabel225.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel225.setText("3. Penggunaan Alat Bantu O2");
        jLabel225.setName("jLabel225"); // NOI18N
        FormInput.add(jLabel225);
        jLabel225.setBounds(30, 170, 160, 23);

        Alat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        Alat.setName("Alat"); // NOI18N
        Alat.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AlatItemStateChanged(evt);
            }
        });
        Alat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlatKeyPressed(evt);
            }
        });
        FormInput.add(Alat);
        Alat.setBounds(190, 170, 120, 23);

        jLabel227.setText("Score EWS :");
        jLabel227.setName("jLabel227"); // NOI18N
        FormInput.add(jLabel227);
        jLabel227.setBounds(320, 170, 75, 23);

        ScoreAlat.setEditable(false);
        ScoreAlat.setText("2");
        ScoreAlat.setFocusTraversalPolicyProvider(true);
        ScoreAlat.setName("ScoreAlat"); // NOI18N
        FormInput.add(ScoreAlat);
        ScoreAlat.setBounds(400, 170, 60, 23);

        jLabel228.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel228.setText("4. Suhu(C)");
        jLabel228.setName("jLabel228"); // NOI18N
        FormInput.add(jLabel228);
        jLabel228.setBounds(30, 200, 160, 23);

        jLabel230.setText("Score EWS :");
        jLabel230.setName("jLabel230"); // NOI18N
        FormInput.add(jLabel230);
        jLabel230.setBounds(320, 200, 75, 23);

        ScoreSuhu.setEditable(false);
        ScoreSuhu.setFocusTraversalPolicyProvider(true);
        ScoreSuhu.setName("ScoreSuhu"); // NOI18N
        ScoreSuhu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScoreSuhuActionPerformed(evt);
            }
        });
        FormInput.add(ScoreSuhu);
        ScoreSuhu.setBounds(400, 200, 60, 23);

        jLabel231.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel231.setText("5. Denyut Jantung");
        jLabel231.setName("jLabel231"); // NOI18N
        FormInput.add(jLabel231);
        jLabel231.setBounds(30, 230, 150, 23);

        jLabel233.setText("Score EWS :");
        jLabel233.setName("jLabel233"); // NOI18N
        FormInput.add(jLabel233);
        jLabel233.setBounds(320, 230, 75, 23);

        ScoreDenyut.setEditable(false);
        ScoreDenyut.setFocusTraversalPolicyProvider(true);
        ScoreDenyut.setName("ScoreDenyut"); // NOI18N
        FormInput.add(ScoreDenyut);
        ScoreDenyut.setBounds(400, 230, 60, 23);

        jLabel234.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel234.setText("6. Tekanan Darah Sistolik");
        jLabel234.setName("jLabel234"); // NOI18N
        FormInput.add(jLabel234);
        jLabel234.setBounds(30, 260, 140, 23);

        jLabel236.setText("Score EWS :");
        jLabel236.setName("jLabel236"); // NOI18N
        FormInput.add(jLabel236);
        jLabel236.setBounds(320, 260, 75, 23);

        ScoreTekanan.setEditable(false);
        ScoreTekanan.setFocusTraversalPolicyProvider(true);
        ScoreTekanan.setName("ScoreTekanan"); // NOI18N
        FormInput.add(ScoreTekanan);
        ScoreTekanan.setBounds(400, 260, 60, 23);

        jLabel237.setText("Total Score EWS:");
        jLabel237.setName("jLabel237"); // NOI18N
        FormInput.add(jLabel237);
        jLabel237.setBounds(280, 320, 110, 23);

        Total.setEditable(false);
        Total.setText("2");
        Total.setFocusTraversalPolicyProvider(true);
        Total.setName("Total"); // NOI18N
        Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TotalActionPerformed(evt);
            }
        });
        FormInput.add(Total);
        Total.setBounds(400, 320, 60, 23);

        jLabel238.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel238.setText("7. Kesadaran");
        jLabel238.setName("jLabel238"); // NOI18N
        FormInput.add(jLabel238);
        jLabel238.setBounds(30, 290, 150, 23);

        Kesadaran.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A", "P-V-U" }));
        Kesadaran.setName("Kesadaran"); // NOI18N
        Kesadaran.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                KesadaranItemStateChanged(evt);
            }
        });
        Kesadaran.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesadaranKeyPressed(evt);
            }
        });
        FormInput.add(Kesadaran);
        Kesadaran.setBounds(190, 290, 120, 23);

        jLabel240.setText("Score EWS :");
        jLabel240.setName("jLabel240"); // NOI18N
        FormInput.add(jLabel240);
        jLabel240.setBounds(320, 290, 75, 23);

        ScoreKesadaran.setEditable(false);
        ScoreKesadaran.setText("0");
        ScoreKesadaran.setFocusTraversalPolicyProvider(true);
        ScoreKesadaran.setName("ScoreKesadaran"); // NOI18N
        FormInput.add(ScoreKesadaran);
        ScoreKesadaran.setBounds(400, 290, 60, 23);

        jLabel106.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel106.setText("EARLY WARNING SCORE (EWS)");
        jLabel106.setName("jLabel106"); // NOI18N
        FormInput.add(jLabel106);
        jLabel106.setBounds(30, 80, 180, 23);

        scrollPane17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane17.setName("scrollPane17"); // NOI18N

        Respon.setEditable(false);
        Respon.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Respon.setColumns(20);
        Respon.setRows(5);
        Respon.setName("Respon"); // NOI18N
        Respon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ResponKeyPressed(evt);
            }
        });
        scrollPane17.setViewportView(Respon);

        FormInput.add(scrollPane17);
        scrollPane17.setBounds(560, 160, 280, 150);

        jLabel262.setText("Score EWS :");
        jLabel262.setName("jLabel262"); // NOI18N
        FormInput.add(jLabel262);
        jLabel262.setBounds(320, 110, 75, 23);

        jLabel241.setText("Klasifikasi :");
        jLabel241.setName("jLabel241"); // NOI18N
        FormInput.add(jLabel241);
        jLabel241.setBounds(470, 110, 70, 23);

        Klasifikasi.setEditable(false);
        Klasifikasi.setFocusTraversalPolicyProvider(true);
        Klasifikasi.setName("Klasifikasi"); // NOI18N
        FormInput.add(Klasifikasi);
        Klasifikasi.setBounds(560, 110, 170, 23);

        scrollPane18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane18.setName("scrollPane18"); // NOI18N

        Tindakan.setEditable(false);
        Tindakan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Tindakan.setColumns(20);
        Tindakan.setRows(5);
        Tindakan.setName("Tindakan"); // NOI18N
        Tindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TindakanKeyPressed(evt);
            }
        });
        scrollPane18.setViewportView(Tindakan);

        FormInput.add(scrollPane18);
        scrollPane18.setBounds(950, 160, 280, 150);

        jLabel243.setText("Tindakan :");
        jLabel243.setName("jLabel243"); // NOI18N
        FormInput.add(jLabel243);
        jLabel243.setBounds(860, 160, 75, 23);

        jLabel244.setText("Frekuensi Monitoring:");
        jLabel244.setName("jLabel244"); // NOI18N
        FormInput.add(jLabel244);
        jLabel244.setBounds(810, 110, 120, 23);

        Frekuensi.setEditable(false);
        Frekuensi.setFocusTraversalPolicyProvider(true);
        Frekuensi.setName("Frekuensi"); // NOI18N
        FormInput.add(Frekuensi);
        Frekuensi.setBounds(950, 110, 170, 23);

        Suhu1.setFocusTraversalPolicyProvider(true);
        Suhu1.setName("Suhu1"); // NOI18N
        Suhu1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Suhu1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                Suhu1KeyTyped(evt);
            }
        });
        FormInput.add(Suhu1);
        Suhu1.setBounds(190, 200, 120, 23);

        Pernafasan1.setFocusTraversalPolicyProvider(true);
        Pernafasan1.setName("Pernafasan1"); // NOI18N
        Pernafasan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Pernafasan1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                Pernafasan1KeyTyped(evt);
            }
        });
        FormInput.add(Pernafasan1);
        Pernafasan1.setBounds(190, 110, 120, 23);

        Saturasi1.setFocusTraversalPolicyProvider(true);
        Saturasi1.setName("Saturasi1"); // NOI18N
        Saturasi1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Saturasi1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                Saturasi1KeyTyped(evt);
            }
        });
        FormInput.add(Saturasi1);
        Saturasi1.setBounds(190, 140, 120, 23);

        Denyut1.setFocusTraversalPolicyProvider(true);
        Denyut1.setName("Denyut1"); // NOI18N
        Denyut1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Denyut1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                Denyut1KeyTyped(evt);
            }
        });
        FormInput.add(Denyut1);
        Denyut1.setBounds(190, 230, 120, 23);

        Tekanan1.setFocusTraversalPolicyProvider(true);
        Tekanan1.setName("Tekanan1"); // NOI18N
        Tekanan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Tekanan1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                Tekanan1KeyTyped(evt);
            }
        });
        FormInput.add(Tekanan1);
        Tekanan1.setBounds(190, 260, 120, 23);

        PanelWall.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/nyeri.png"))); // NOI18N
        PanelWall.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall.setRound(false);
        PanelWall.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall.setLayout(null);
        FormInput.add(PanelWall);
        PanelWall.setBounds(40, 380, 320, 130);

        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel85.setText("Skala Nyeri :");
        jLabel85.setName("jLabel85"); // NOI18N
        FormInput.add(jLabel85);
        jLabel85.setBounds(400, 380, 70, 23);

        SkalaNyeri.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        SkalaNyeri.setName("SkalaNyeri"); // NOI18N
        SkalaNyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaNyeriKeyPressed(evt);
            }
        });
        FormInput.add(SkalaNyeri);
        SkalaNyeri.setBounds(480, 380, 70, 23);

        jLabel245.setText("Cm");
        jLabel245.setName("jLabel245"); // NOI18N
        FormInput.add(jLabel245);
        jLabel245.setBounds(720, 410, 20, 23);

        BB.setFocusTraversalPolicyProvider(true);
        BB.setName("BB"); // NOI18N
        BB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BBKeyReleased(evt);
            }
        });
        FormInput.add(BB);
        BB.setBounds(480, 410, 70, 23);

        jLabel246.setText("TB : ");
        jLabel246.setName("jLabel246"); // NOI18N
        FormInput.add(jLabel246);
        jLabel246.setBounds(610, 410, 30, 23);

        TB.setFocusTraversalPolicyProvider(true);
        TB.setName("TB"); // NOI18N
        TB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TBKeyReleased(evt);
            }
        });
        FormInput.add(TB);
        TB.setBounds(650, 410, 70, 23);

        jLabel247.setText("BB : ");
        jLabel247.setName("jLabel247"); // NOI18N
        FormInput.add(jLabel247);
        jLabel247.setBounds(400, 410, 70, 23);

        jLabel248.setText("Kg");
        jLabel248.setName("jLabel248"); // NOI18N
        FormInput.add(jLabel248);
        jLabel248.setBounds(550, 410, 20, 23);

        jLabel249.setText("Lingkar kepala : ");
        jLabel249.setName("jLabel249"); // NOI18N
        FormInput.add(jLabel249);
        jLabel249.setBounds(370, 440, 100, 23);

        LK.setFocusTraversalPolicyProvider(true);
        LK.setName("LK"); // NOI18N
        LK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                LKKeyReleased(evt);
            }
        });
        FormInput.add(LK);
        LK.setBounds(480, 440, 70, 23);

        jLabel250.setText("Lingkar Perut : ");
        jLabel250.setName("jLabel250"); // NOI18N
        FormInput.add(jLabel250);
        jLabel250.setBounds(560, 440, 80, 23);

        LP.setFocusTraversalPolicyProvider(true);
        LP.setName("LP"); // NOI18N
        LP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                LPKeyReleased(evt);
            }
        });
        FormInput.add(LP);
        LP.setBounds(650, 440, 70, 23);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 350, 880, 1);

        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel53.setText("SCORE NYERI");
        jLabel53.setName("jLabel53"); // NOI18N
        FormInput.add(jLabel53);
        jLabel53.setBounds(30, 360, 380, 23);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 520, 880, 1);

        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel54.setText("BALANCE CAIRAN");
        jLabel54.setName("jLabel54"); // NOI18N
        FormInput.add(jLabel54);
        jLabel54.setBounds(30, 540, 380, 23);

        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel55.setText("MASUK");
        jLabel55.setName("jLabel55"); // NOI18N
        FormInput.add(jLabel55);
        jLabel55.setBounds(50, 570, 50, 23);

        jLabel251.setText("Peroral/NGT : ");
        jLabel251.setName("jLabel251"); // NOI18N
        FormInput.add(jLabel251);
        jLabel251.setBounds(80, 590, 100, 23);

        Masuk1.setFocusTraversalPolicyProvider(true);
        Masuk1.setName("Masuk1"); // NOI18N
        Masuk1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Masuk1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Masuk1KeyReleased(evt);
            }
        });
        FormInput.add(Masuk1);
        Masuk1.setBounds(180, 590, 70, 23);

        jLabel252.setText("Parenteral/Transfusi : ");
        jLabel252.setName("jLabel252"); // NOI18N
        FormInput.add(jLabel252);
        jLabel252.setBounds(60, 620, 120, 23);

        Masuk2.setFocusTraversalPolicyProvider(true);
        Masuk2.setName("Masuk2"); // NOI18N
        Masuk2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Masuk2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Masuk2KeyReleased(evt);
            }
        });
        FormInput.add(Masuk2);
        Masuk2.setBounds(180, 620, 70, 23);

        jLabel253.setText("Jumlah : ");
        jLabel253.setName("jLabel253"); // NOI18N
        FormInput.add(jLabel253);
        jLabel253.setBounds(60, 650, 120, 23);

        JumlahMasuk.setFocusTraversalPolicyProvider(true);
        JumlahMasuk.setName("JumlahMasuk"); // NOI18N
        JumlahMasuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JumlahMasukKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                JumlahMasukKeyReleased(evt);
            }
        });
        FormInput.add(JumlahMasuk);
        JumlahMasuk.setBounds(180, 650, 70, 23);

        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel56.setText("KELUAR");
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(290, 570, 50, 23);

        jLabel254.setText("Feses : ");
        jLabel254.setName("jLabel254"); // NOI18N
        FormInput.add(jLabel254);
        jLabel254.setBounds(320, 590, 100, 23);

        Keluar1.setFocusTraversalPolicyProvider(true);
        Keluar1.setName("Keluar1"); // NOI18N
        Keluar1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Keluar1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Keluar1KeyReleased(evt);
            }
        });
        FormInput.add(Keluar1);
        Keluar1.setBounds(420, 590, 70, 23);

        jLabel255.setText("Urine : ");
        jLabel255.setName("jLabel255"); // NOI18N
        FormInput.add(jLabel255);
        jLabel255.setBounds(300, 620, 120, 23);

        Keluar2.setFocusTraversalPolicyProvider(true);
        Keluar2.setName("Keluar2"); // NOI18N
        Keluar2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Keluar2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Keluar2KeyReleased(evt);
            }
        });
        FormInput.add(Keluar2);
        Keluar2.setBounds(420, 620, 70, 23);

        jLabel256.setText("Muntah/NGT : ");
        jLabel256.setName("jLabel256"); // NOI18N
        FormInput.add(jLabel256);
        jLabel256.setBounds(300, 650, 120, 23);

        Keluar3.setFocusTraversalPolicyProvider(true);
        Keluar3.setName("Keluar3"); // NOI18N
        Keluar3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Keluar3KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Keluar3KeyReleased(evt);
            }
        });
        FormInput.add(Keluar3);
        Keluar3.setBounds(420, 650, 70, 23);

        jLabel257.setText("Drain/Darah : ");
        jLabel257.setName("jLabel257"); // NOI18N
        FormInput.add(jLabel257);
        jLabel257.setBounds(320, 680, 100, 23);

        Keluar4.setFocusTraversalPolicyProvider(true);
        Keluar4.setName("Keluar4"); // NOI18N
        Keluar4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Keluar4KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Keluar4KeyReleased(evt);
            }
        });
        FormInput.add(Keluar4);
        Keluar4.setBounds(420, 680, 70, 23);

        jLabel258.setText("IWL : ");
        jLabel258.setName("jLabel258"); // NOI18N
        FormInput.add(jLabel258);
        jLabel258.setBounds(300, 710, 120, 23);

        Keluar5.setFocusTraversalPolicyProvider(true);
        Keluar5.setName("Keluar5"); // NOI18N
        Keluar5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Keluar5KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Keluar5KeyReleased(evt);
            }
        });
        FormInput.add(Keluar5);
        Keluar5.setBounds(420, 710, 70, 23);

        jLabel259.setText("Jumlah : ");
        jLabel259.setName("jLabel259"); // NOI18N
        FormInput.add(jLabel259);
        jLabel259.setBounds(300, 740, 120, 23);

        JumlahKeluar.setEditable(false);
        JumlahKeluar.setFocusTraversalPolicyProvider(true);
        JumlahKeluar.setName("JumlahKeluar"); // NOI18N
        JumlahKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JumlahKeluarKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                JumlahKeluarKeyReleased(evt);
            }
        });
        FormInput.add(JumlahKeluar);
        JumlahKeluar.setBounds(420, 740, 70, 23);

        jLabel260.setText("Balance Cairan : ");
        jLabel260.setName("jLabel260"); // NOI18N
        FormInput.add(jLabel260);
        jLabel260.setBounds(520, 570, 100, 23);

        BC.setEditable(false);
        BC.setFocusTraversalPolicyProvider(true);
        BC.setName("BC"); // NOI18N
        BC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BCKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BCKeyReleased(evt);
            }
        });
        FormInput.add(BC);
        BC.setBounds(620, 570, 70, 23);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Penilaian", internalFrame2);
        internalFrame2.getAccessibleContext().setAccessibleParent(TabRawat);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-12-2022" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-12-2022" }));
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

        TabRawat.addTab("Data Penilaian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,BtnDokter);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"NIP Petugas");
        }else if(Pernafasan1.getText().trim().equals("")){
            Valid.textKosong(Pernafasan1,"Pernafasan");
        }else if(ScorePernafasan.getText().trim().equals("")){
            Valid.textKosong(ScorePernafasan,"Score Pernafasan");
        }else if(Saturasi1.getText().trim().equals("")){
            Valid.textKosong(Saturasi1,"Saturasi");
        }else if(ScoreSaturasi.getText().trim().equals("")){
            Valid.textKosong(ScoreSaturasi,"Score Saturasi");
        }else if(Suhu1.getText().trim().equals("")){
            Valid.textKosong(Suhu1,"Suhu");
        }else if(ScoreSuhu.getText().trim().equals("")){
            Valid.textKosong(ScoreSuhu,"Score Suhu");
        }else if(Denyut1.getText().trim().equals("")){
            Valid.textKosong(Denyut1,"Denyut Jantung");
        }else if(ScoreDenyut.getText().trim().equals("")){
            Valid.textKosong(ScoreDenyut,"Score Denyut Jantung");
        }else if(Tekanan1.getText().trim().equals("")){
            Valid.textKosong(Tekanan1,"Tekanan Darah Sistolik");
        }else if(ScoreTekanan.getText().trim().equals("")){
            Valid.textKosong(ScoreTekanan,"Score Tekanan Darah Sistolik");
        }else if(Total.getText().trim().equals("")){
            Valid.textKosong(Total,"Total Score");
        }else if(Klasifikasi.getText().trim().equals("")){
            Valid.textKosong(Klasifikasi,"Klasifikasi");
        }else if(Respon.getText().trim().equals("")){
            Valid.textKosong(Respon,"Respon Klinis");
        }else if(Tindakan.getText().trim().equals("")){
            Valid.textKosong(Tindakan,"Tindakan");
        }else if(Frekuensi.getText().trim().equals("")){
            Valid.textKosong(Frekuensi,"Frekuensi Monitoring");
        }else if(Masuk1.getText().trim().equals("")){
            Valid.textKosong(Masuk1,"Peroral/NGT");
        }else if(Masuk2.getText().trim().equals("")){
            Valid.textKosong(Masuk2,"Parenteral/Transfusi");
        }else if(JumlahMasuk.getText().trim().equals("")){
            Valid.textKosong(JumlahMasuk,"Jumlah Masuk");
        }else if(Keluar1.getText().trim().equals("")){
            Valid.textKosong(Keluar1,"Feses");
        }else if(Keluar2.getText().trim().equals("")){
            Valid.textKosong(Keluar2,"Urine");
        }else if(Keluar3.getText().trim().equals("")){
            Valid.textKosong(Keluar3,"Muntah/NGT");
        }else if(Keluar4.getText().trim().equals("")){
            Valid.textKosong(Keluar4,"Drain/Darah");
        }else if(Keluar5.getText().trim().equals("")){
            Valid.textKosong(Keluar5,"IWL");
        }else if(JumlahKeluar.getText().trim().equals("")){
            Valid.textKosong(JumlahKeluar,"Jumlah Keluar");
        }else if(BC.getText().trim().equals("")){
            Valid.textKosong(BC,"Balance Cairan");
        }else{
            if(Sequel.menyimpantf("ews_ranap","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",38,new String[]{
                    TNoRw.getText(),tanggalNow.format(new Date()),jamNow.format(new Date()),KdPetugas.getText(),Pernafasan1.getText(),ScorePernafasan.getText(),Saturasi1.getText(),
                    ScoreSaturasi.getText(),Alat.getSelectedItem().toString(),ScoreAlat.getText(),Suhu1.getText(),ScoreSuhu.getText(),Denyut1.getText(),
                    ScoreDenyut.getText(),Tekanan1.getText(),ScoreTekanan.getText(),Kesadaran.getSelectedItem().toString(),ScoreKesadaran.getText(),Total.getText(),Klasifikasi.getText(),Respon.getText(),Tindakan.getText(),
                    Frekuensi.getText(),SkalaNyeri.getSelectedItem().toString(),BB.getText(),TB.getText(),LK.getText(),LP.getText(),Masuk1.getText(),Masuk2.getText(),JumlahMasuk.getText(),Keluar1.getText(),Keluar2.getText(),
                    Keluar3.getText(),Keluar4.getText(),Keluar5.getText(),JumlahKeluar.getText(),BC.getText()
                })==true){
                    emptTeks();
            }
        }
    
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Pernafasan1,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(Sequel.queryu2tf("delete from ews_ranap where jam=?",1,new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(),8).toString()
            })==true){
                tampil();
                emptTeks();
            }else{
                JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
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
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"NIP Petugas");
        }else if(Pernafasan1.getText().trim().equals("")){
            Valid.textKosong(Pernafasan1,"Pernafasan");
        }else if(ScorePernafasan.getText().trim().equals("")){
            Valid.textKosong(ScorePernafasan,"Score Pernafasan");
        }else if(Saturasi1.getText().trim().equals("")){
            Valid.textKosong(Saturasi1,"Saturasi");
        }else if(ScoreSaturasi.getText().trim().equals("")){
            Valid.textKosong(ScoreSaturasi,"Score Saturasi");
        }else if(Suhu1.getText().trim().equals("")){
            Valid.textKosong(Suhu1,"Suhu");
        }else if(ScoreSuhu.getText().trim().equals("")){
            Valid.textKosong(ScoreSuhu,"Score Suhu");
        }else if(Denyut1.getText().trim().equals("")){
            Valid.textKosong(Denyut1,"Denyut Jantung");
        }else if(ScoreDenyut.getText().trim().equals("")){
            Valid.textKosong(ScoreDenyut,"Score Denyut Jantung");
        }else if(Tekanan1.getText().trim().equals("")){
            Valid.textKosong(Tekanan1,"Tekanan Darah Sistolik");
        }else if(ScoreTekanan.getText().trim().equals("")){
            Valid.textKosong(ScoreTekanan,"Score Tekanan Darah Sistolik");
        }else if(Total.getText().trim().equals("")){
            Valid.textKosong(Total,"Total Score");
        }else if(Klasifikasi.getText().trim().equals("")){
            Valid.textKosong(Klasifikasi,"Klasifikasi");
        }else if(Respon.getText().trim().equals("")){
            Valid.textKosong(Respon,"Respon Klinis");
        }else if(Tindakan.getText().trim().equals("")){
            Valid.textKosong(Tindakan,"Tindakan");
        }else if(Frekuensi.getText().trim().equals("")){
            Valid.textKosong(Frekuensi,"Frekuensi Monitoring");
        }else if(Masuk1.getText().trim().equals("")){
            Valid.textKosong(Masuk1,"Peroral/NGT");
        }else if(Masuk2.getText().trim().equals("")){
            Valid.textKosong(Masuk2,"Parenteral/Transfusi");
        }else if(JumlahMasuk.getText().trim().equals("")){
            Valid.textKosong(JumlahMasuk,"Jumlah Masuk");
        }else if(Keluar1.getText().trim().equals("")){
            Valid.textKosong(Keluar1,"Feses");
        }else if(Keluar2.getText().trim().equals("")){
            Valid.textKosong(Keluar2,"Urine");
        }else if(Keluar3.getText().trim().equals("")){
            Valid.textKosong(Keluar3,"Muntah/NGT");
        }else if(Keluar4.getText().trim().equals("")){
            Valid.textKosong(Keluar4,"Drain/Darah");
        }else if(Keluar5.getText().trim().equals("")){
            Valid.textKosong(Keluar5,"IWL");
        }else if(JumlahKeluar.getText().trim().equals("")){
            Valid.textKosong(JumlahKeluar,"Jumlah Keluar");
        }else if(BC.getText().trim().equals("")){
            Valid.textKosong(BC,"Balance Cairan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(Sequel.mengedittf("ews_ranap","jam=?","no_rawat=?,tanggal=?,jam=?,nik=?,pernafasan=?,score_pernafasan=?,saturasi=?,score_saturasi=?,alat=?,score_alat=?,suhu=?,score_suhu=?,denyut=?,score_denyut=?,tekanan=?,score_tekanan=?,kesadaran=?,score_kesadaran=?,total_score=?,klasifikasi=?,respon=?,tindakan=?,frekuensi=?,skala_nyeri=?,bb=?,tb=?,lk=?,lp=?,masuk1=?,masuk2=?,jumlahmasuk=?,keluar1=?,keluar2=?,keluar3=?,keluar4=?,keluar5=?,jumlahkeluar=?,bc=?",39,new String[]{
                    TNoRw.getText(),tanggalNow.format(new Date()),jamNow.format(new Date()),KdPetugas.getText(),Pernafasan1.getText(),ScorePernafasan.getText(),Saturasi1.getText(),
                    ScoreSaturasi.getText(),Alat.getSelectedItem().toString(),ScoreAlat.getText(),Suhu1.getText(),ScoreSuhu.getText(),Denyut1.getText(),
                    ScoreDenyut.getText(),Tekanan1.getText(),ScoreTekanan.getText(),Kesadaran.getSelectedItem().toString(),ScoreKesadaran.getText(),Total.getText(),Klasifikasi.getText(),Respon.getText(),Tindakan.getText(),
                    Frekuensi.getText(),SkalaNyeri.getSelectedItem().toString(),BB.getText(),TB.getText(),LK.getText(),LP.getText(),Masuk1.getText(),Masuk2.getText(),JumlahMasuk.getText(),Keluar1.getText(),Keluar2.getText(),
                    Keluar3.getText(),Keluar4.getText(),Keluar5.getText(),JumlahKeluar.getText(),BC.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),8).toString()
                     })==true){
                       tampil();
                       emptTeks();
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
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
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            try{
                if(TCari.getText().equals("")){
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_ralan.tanggal,"+
                            "penilaian_awal_keperawatan_ralan.informasi,penilaian_awal_keperawatan_ralan.td,penilaian_awal_keperawatan_ralan.nadi,penilaian_awal_keperawatan_ralan.rr,penilaian_awal_keperawatan_ralan.suhu,penilaian_awal_keperawatan_ralan.bb,penilaian_awal_keperawatan_ralan.tb,"+
                            "penilaian_awal_keperawatan_ralan.nadi,penilaian_awal_keperawatan_ralan.rr,penilaian_awal_keperawatan_ralan.suhu,penilaian_awal_keperawatan_ralan.gcs,penilaian_awal_keperawatan_ralan.bb,penilaian_awal_keperawatan_ralan.tb,penilaian_awal_keperawatan_ralan.bmi,penilaian_awal_keperawatan_ralan.keluhan_utama,"+
                            "penilaian_awal_keperawatan_ralan.rpd,penilaian_awal_keperawatan_ralan.rpk,penilaian_awal_keperawatan_ralan.rpo,penilaian_awal_keperawatan_ralan.alergi,penilaian_awal_keperawatan_ralan.alat_bantu,penilaian_awal_keperawatan_ralan.ket_bantu,penilaian_awal_keperawatan_ralan.prothesa,"+
                            "penilaian_awal_keperawatan_ralan.ket_pro,penilaian_awal_keperawatan_ralan.adl,penilaian_awal_keperawatan_ralan.status_psiko,penilaian_awal_keperawatan_ralan.ket_psiko,penilaian_awal_keperawatan_ralan.hub_keluarga,penilaian_awal_keperawatan_ralan.tinggal_dengan,"+
                            "penilaian_awal_keperawatan_ralan.ket_tinggal,penilaian_awal_keperawatan_ralan.ekonomi,penilaian_awal_keperawatan_ralan.edukasi,penilaian_awal_keperawatan_ralan.ket_edukasi,penilaian_awal_keperawatan_ralan.berjalan_a,penilaian_awal_keperawatan_ralan.berjalan_b,"+
                            "penilaian_awal_keperawatan_ralan.berjalan_c,penilaian_awal_keperawatan_ralan.hasil,penilaian_awal_keperawatan_ralan.lapor,penilaian_awal_keperawatan_ralan.ket_lapor,penilaian_awal_keperawatan_ralan.sg1,penilaian_awal_keperawatan_ralan.nilai1,penilaian_awal_keperawatan_ralan.sg2,penilaian_awal_keperawatan_ralan.nilai2,"+
                            "penilaian_awal_keperawatan_ralan.total_hasil,penilaian_awal_keperawatan_ralan.nyeri,penilaian_awal_keperawatan_ralan.provokes,penilaian_awal_keperawatan_ralan.ket_provokes,penilaian_awal_keperawatan_ralan.quality,penilaian_awal_keperawatan_ralan.ket_quality,penilaian_awal_keperawatan_ralan.lokasi,penilaian_awal_keperawatan_ralan.menyebar,"+
                            "penilaian_awal_keperawatan_ralan.skala_nyeri,penilaian_awal_keperawatan_ralan.durasi,penilaian_awal_keperawatan_ralan.nyeri_hilang,penilaian_awal_keperawatan_ralan.ket_nyeri,penilaian_awal_keperawatan_ralan.pada_dokter,penilaian_awal_keperawatan_ralan.ket_dokter,penilaian_awal_keperawatan_ralan.rencana,"+
                            "penilaian_awal_keperawatan_ralan.nik,petugas.nama,penilaian_awal_keperawatan_ralan.budaya,penilaian_awal_keperawatan_ralan.ket_budaya "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join penilaian_awal_keperawatan_ralan on reg_periksa.no_rawat=penilaian_awal_keperawatan_ralan.no_rawat "+
                            "inner join petugas on penilaian_awal_keperawatan_ralan.nik=petugas.nik "+
                            "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                            "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? order by penilaian_awal_keperawatan_ralan.tanggal");
                }else{
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_ralan.tanggal,"+
                            "penilaian_awal_keperawatan_ralan.informasi,penilaian_awal_keperawatan_ralan.td,penilaian_awal_keperawatan_ralan.nadi,penilaian_awal_keperawatan_ralan.rr,penilaian_awal_keperawatan_ralan.suhu,penilaian_awal_keperawatan_ralan.bb,penilaian_awal_keperawatan_ralan.tb,"+
                            "penilaian_awal_keperawatan_ralan.nadi,penilaian_awal_keperawatan_ralan.rr,penilaian_awal_keperawatan_ralan.suhu,penilaian_awal_keperawatan_ralan.gcs,penilaian_awal_keperawatan_ralan.bb,penilaian_awal_keperawatan_ralan.tb,penilaian_awal_keperawatan_ralan.bmi,penilaian_awal_keperawatan_ralan.keluhan_utama,"+
                            "penilaian_awal_keperawatan_ralan.rpd,penilaian_awal_keperawatan_ralan.rpk,penilaian_awal_keperawatan_ralan.rpo,penilaian_awal_keperawatan_ralan.alergi,penilaian_awal_keperawatan_ralan.alat_bantu,penilaian_awal_keperawatan_ralan.ket_bantu,penilaian_awal_keperawatan_ralan.prothesa,"+
                            "penilaian_awal_keperawatan_ralan.ket_pro,penilaian_awal_keperawatan_ralan.adl,penilaian_awal_keperawatan_ralan.status_psiko,penilaian_awal_keperawatan_ralan.ket_psiko,penilaian_awal_keperawatan_ralan.hub_keluarga,penilaian_awal_keperawatan_ralan.tinggal_dengan,"+
                            "penilaian_awal_keperawatan_ralan.ket_tinggal,penilaian_awal_keperawatan_ralan.ekonomi,penilaian_awal_keperawatan_ralan.edukasi,penilaian_awal_keperawatan_ralan.ket_edukasi,penilaian_awal_keperawatan_ralan.berjalan_a,penilaian_awal_keperawatan_ralan.berjalan_b,"+
                            "penilaian_awal_keperawatan_ralan.berjalan_c,penilaian_awal_keperawatan_ralan.hasil,penilaian_awal_keperawatan_ralan.lapor,penilaian_awal_keperawatan_ralan.ket_lapor,penilaian_awal_keperawatan_ralan.sg1,penilaian_awal_keperawatan_ralan.nilai1,penilaian_awal_keperawatan_ralan.sg2,penilaian_awal_keperawatan_ralan.nilai2,"+
                            "penilaian_awal_keperawatan_ralan.total_hasil,penilaian_awal_keperawatan_ralan.nyeri,penilaian_awal_keperawatan_ralan.provokes,penilaian_awal_keperawatan_ralan.ket_provokes,penilaian_awal_keperawatan_ralan.quality,penilaian_awal_keperawatan_ralan.ket_quality,penilaian_awal_keperawatan_ralan.lokasi,penilaian_awal_keperawatan_ralan.menyebar,"+
                            "penilaian_awal_keperawatan_ralan.skala_nyeri,penilaian_awal_keperawatan_ralan.durasi,penilaian_awal_keperawatan_ralan.nyeri_hilang,penilaian_awal_keperawatan_ralan.ket_nyeri,penilaian_awal_keperawatan_ralan.pada_dokter,penilaian_awal_keperawatan_ralan.ket_dokter,penilaian_awal_keperawatan_ralan.rencana,"+
                            "penilaian_awal_keperawatan_ralan.nik,petugas.nama,penilaian_awal_keperawatan_ralan.budaya,penilaian_awal_keperawatan_ralan.ket_budaya "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join penilaian_awal_keperawatan_ralan on reg_periksa.no_rawat=penilaian_awal_keperawatan_ralan.no_rawat "+
                            "inner join petugas on penilaian_awal_keperawatan_ralan.nik=petugas.nik "+
                            "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                            "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? and reg_periksa.no_rawat like ? or "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? and pasien.no_rkm_medis like ? or "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? and pasien.nm_pasien like ? or "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? and penilaian_awal_keperawatan_ralan.nik like ? or "+
                            "penilaian_awal_keperawatan_ralan.tanggal between ? and ? and petugas.nama like ? order by penilaian_awal_keperawatan_ralan.tanggal");
                }

                try {
                    if(TCari.getText().equals("")){
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    }else{
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(3,"%"+TCari.getText()+"%");
                        ps.setString(4,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(5,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(6,"%"+TCari.getText()+"%");
                        ps.setString(7,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(8,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(9,"%"+TCari.getText()+"%");
                        ps.setString(10,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(11,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(12,"%"+TCari.getText()+"%");
                        ps.setString(13,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(14,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(15,"%"+TCari.getText()+"%");
                    }   
                    rs=ps.executeQuery();
                    htmlContent = new StringBuilder();
                    htmlContent.append(                             
                        "<tr class='isi'>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='9%'><b>PASIEN & PETUGAS</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'><b>I. KEADAAN UMUM</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'><b>II. STATUS NUTRISI</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='13%'><b>III. RIWAYAT KESEHATAN</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='8%'><b>IV. FUNGSIONAL</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='16%'><b>V. RIWAYAT PSIKO-SOSIAL SPIRITUAL DAN BUDAYA</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='16%'><b>VI. PENILAIAN RESIKO JATUH</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='11%'><b>VII. SKRINING GIZI</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='11%'><b>VIII. PENILAIAN TINGKAT NYERI</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='6%'><b>MASALAH & RENCANA KEPERAWATAN</b></td>"+
                        "</tr>"
                    );
                    while(rs.next()){
                        masalahkeperawatan="";
                        ps2=koneksi.prepareStatement(
                            "select master_masalah_keperawatan.kode_masalah,master_masalah_keperawatan.nama_masalah from master_masalah_keperawatan "+
                            "inner join penilaian_awal_keperawatan_ralan_masalah on penilaian_awal_keperawatan_ralan_masalah.kode_masalah=master_masalah_keperawatan.kode_masalah "+
                            "where penilaian_awal_keperawatan_ralan_masalah.no_rawat=? order by kode_masalah");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                masalahkeperawatan=rs2.getString("nama_masalah")+", "+masalahkeperawatan;
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
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>No.Rawat</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("no_rawat")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>No.R.M.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Nama Pasien</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>J.K.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("jk")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Agama</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("agama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Bahasa</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nama_bahasa")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Tgl.Lahir</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nama_cacat")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Cacat Fisik</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Petugas</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nik")+" "+rs.getString("nama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Tgl.Asuhan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("tanggal")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Informasi</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("informasi")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>TD</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("td")+"mmHg</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Nadi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("nadi")+"x/menit</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>RR</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("rr")+"x/menit</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Suhu</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("suhu")+"°C</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>GCS</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("gcs")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>BB</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("bb")+"Kg</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>TB</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("tb")+"cm</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>BMI</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("bmi")+"Kg/m²</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Keluhan Utama</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("keluhan_utama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPD</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("rpd")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPK</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("rpk")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPO</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("rpo")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Alergi</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("alergi")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Alat Bantu</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("alat_bantu")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Alat Bantu</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_bantu")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Prothesa</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("prothesa")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Prothesa</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_pro")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>ADL</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("adl")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Status Psikologis</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("status_psiko")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Ket. Psikologi</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_psiko")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Hubungan pasien dengan anggota keluarga</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("hub_keluarga")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Tinggal dengan</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("tinggal_dengan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Ket. Tinggal</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_tinggal")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Ekonomi</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ekonomi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Kepercayaan / Budaya / Nilai-nilai khusus yang perlu diperhatikan</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("budaya")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Ket. Budaya</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_budaya")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Edukasi diberikan kepada </td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("edukasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Ket. Edukasi</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_edukasi")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Tidak seimbang/sempoyongan/limbung</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_a")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Jalan dengan menggunakan alat bantu (kruk, tripot, kursi roda, orang lain)</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_b")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Menopang saat akan duduk, tampak memegang pinggiran kursi atau meja/benda lain sebagai penopang</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_c")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Hasil</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("hasil")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Dilaporan ke dokter?</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("lapor")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_lapor")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Apakah ada penurunan berat badanyang tidak diinginkan selama enam bulan terakhir?</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("sg1")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Apakah nafsu makan berkurang karena tidak nafsu makan?</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("sg2")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Nilai 1</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("nilai1")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Nilai 2</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("nilai2")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Total Skor</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("total_hasil")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Tingkat Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("provokes")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_provokes")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("quality")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_quality")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Lokas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("lokasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Menyebar</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("menyebar")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Skala Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("skala_nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Durasi</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("durasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Nyeri Hilang</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("nyeri_hilang")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Hilang Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Lapor Ke Dokter</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("pada_dokter")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_dokter")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "Masalah Keperawatan : "+masalahkeperawatan+"<br><br>"+
                                    "Rencana Keperawatan : "+rs.getString("rencana")+
                                "</td>"+
                            "</tr>"
                        );
                    }
                    LoadHTML.setText(
                        "<html>"+
                          "<table width='1800px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                           htmlContent.toString()+
                          "</table>"+
                        "</html>"
                    );

                    File g = new File("file2.css");            
                    BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                    bg.write(
                        ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                        ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                        ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                        ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                        ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                        ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
                    );
                    bg.close();

                    File f = new File("DataPenilaianAwalKeperawatanRalan.html");            
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                    bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                                "<table width='1800px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr class='isi2'>"+
                                        "<td valign='top' align='center'>"+
                                            "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                            akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                            akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                            "<font size='2' face='Tahoma'>DATA PENILAIAN AWAL KEPERAWATAN RAWAT JALAN<br><br></font>"+        
                                        "</td>"+
                                   "</tr>"+
                                "</table>")
                    );
                    bw.close();                         
                    Desktop.getDesktop().browse(f.toURI());
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
        this.setCursor(Cursor.getDefaultCursor());
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
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
//                ChkAccor.setSelected(true);
//                isMenu();
//                getMasalah();
                getData();
//                TabRawat.setSelectedIndex(0);
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
//                    ChkAccor.setSelected(true);
//                    isMenu();
//                    getMasalah();
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugasKeyPressed
        
    }//GEN-LAST:event_KdPetugasKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
//        pegawai.isCek();
        akses.setform("EWSRajal");
        pegawai.emptTeks();
        pegawai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pegawai.setLocationRelativeTo(internalFrame1);
        pegawai.setAlwaysOnTop(false);
        pegawai.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        //Valid.pindah(evt,Monitoring,BtnSimpan);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
//        tampilMasalah();
    }//GEN-LAST:event_formWindowOpened

    private void TPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TPasienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TPasienActionPerformed

    private void AlatItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AlatItemStateChanged
        if(Alat.getSelectedIndex()==0){
            ScoreAlat.setText("2");
        }else if(Alat.getSelectedIndex()==1){
            ScoreAlat.setText("0");
        }
//        isjml();
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }//GEN-LAST:event_AlatItemStateChanged

    private void AlatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlatKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_AlatKeyPressed

    private void KesadaranItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_KesadaranItemStateChanged
        if(Kesadaran.getSelectedIndex()==0){
            ScoreKesadaran.setText("0");
        }else if(Kesadaran.getSelectedIndex()==1){
            ScoreKesadaran.setText("3");
        }
//        isjml();
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }//GEN-LAST:event_KesadaranItemStateChanged

    private void KesadaranKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesadaranKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KesadaranKeyPressed

    private void ResponKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ResponKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ResponKeyPressed

    private void TindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TindakanKeyPressed

    private void ScoreSuhuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScoreSuhuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ScoreSuhuActionPerformed

    private void Pernafasan1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Pernafasan1KeyReleased
       if(Pernafasan1.getText().isEmpty()){
           ScorePernafasan.setText("");
       }else{
          pernafasan();  
       }     
    }//GEN-LAST:event_Pernafasan1KeyReleased

    private void Suhu1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Suhu1KeyTyped
        char kata= evt.getKeyChar();
        if(!((kata>='0') && (kata<='9') || (kata== KeyEvent.VK_PERIOD) || (kata== KeyEvent.VK_BACK_SPACE))){
            JOptionPane.showMessageDialog(null,"Hanya diperbolehkan menginputkan Angka dan Titik");
        }
    }//GEN-LAST:event_Suhu1KeyTyped

    private void Suhu1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Suhu1KeyReleased
       if(Suhu1.getText().isEmpty()){
           ScoreSuhu.setText("");
       }else{
          suhu();  
       }
    }//GEN-LAST:event_Suhu1KeyReleased

    private void SkalaNyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaNyeriKeyPressed
        Valid.pindah(evt,BB,TB);
    }//GEN-LAST:event_SkalaNyeriKeyPressed

    private void BBKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BBKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_BBKeyReleased

    private void TBKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_TBKeyReleased

    private void LKKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LKKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_LKKeyReleased

    private void LPKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LPKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_LPKeyReleased

    private void Masuk1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Masuk1KeyReleased
        isMasuk();
    }//GEN-LAST:event_Masuk1KeyReleased

    private void Masuk2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Masuk2KeyReleased
        isMasuk();
    }//GEN-LAST:event_Masuk2KeyReleased

    private void JumlahMasukKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahMasukKeyReleased
        isBc();
    }//GEN-LAST:event_JumlahMasukKeyReleased

    private void Keluar1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar1KeyReleased
        isKeluar();
    }//GEN-LAST:event_Keluar1KeyReleased

    private void Keluar2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar2KeyReleased
        isKeluar();
    }//GEN-LAST:event_Keluar2KeyReleased

    private void Keluar3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar3KeyReleased
        isKeluar();
    }//GEN-LAST:event_Keluar3KeyReleased

    private void Keluar4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar4KeyReleased
        isKeluar();
    }//GEN-LAST:event_Keluar4KeyReleased

    private void Keluar5KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar5KeyReleased
        isKeluar();
    }//GEN-LAST:event_Keluar5KeyReleased

    private void JumlahKeluarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahKeluarKeyReleased
       
    }//GEN-LAST:event_JumlahKeluarKeyReleased

    private void BCKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BCKeyReleased
       
    }//GEN-LAST:event_BCKeyReleased

    private void TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TotalActionPerformed

    private void Pernafasan1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Pernafasan1KeyTyped
        char kata= evt.getKeyChar();
        if(!((kata>='0') && (kata<='9') || (kata== KeyEvent.VK_BACK_SPACE))){
            JOptionPane.showMessageDialog(null,"Hanya diperbolehkan menginputkan Angka");
        }
    }//GEN-LAST:event_Pernafasan1KeyTyped

    private void Saturasi1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Saturasi1KeyTyped
        char kata= evt.getKeyChar();
        if(!((kata>='0') && (kata<='9') || (kata== KeyEvent.VK_BACK_SPACE))){
            JOptionPane.showMessageDialog(null,"Hanya diperbolehkan menginputkan Angka");
        }
    }//GEN-LAST:event_Saturasi1KeyTyped

    private void Saturasi1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Saturasi1KeyReleased
       if(Saturasi1.getText().isEmpty()){
           ScoreSaturasi.setText("");
       }else{
          saturasi();  
       }
    }//GEN-LAST:event_Saturasi1KeyReleased

    private void Denyut1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Denyut1KeyTyped
        char kata= evt.getKeyChar();
        if(!((kata>='0') && (kata<='9') || (kata== KeyEvent.VK_BACK_SPACE))){
            JOptionPane.showMessageDialog(null,"Hanya diperbolehkan menginputkan Angka");
        }
    }//GEN-LAST:event_Denyut1KeyTyped

    private void Denyut1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Denyut1KeyReleased
       if(Denyut1.getText().isEmpty()){
           ScoreDenyut.setText("");
       }else{
          denyut();  
       }
    }//GEN-LAST:event_Denyut1KeyReleased

    private void Tekanan1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tekanan1KeyTyped
        char kata= evt.getKeyChar();
        if(!((kata>='0') && (kata<='9') || (kata== KeyEvent.VK_BACK_SPACE))){
            JOptionPane.showMessageDialog(null,"Hanya diperbolehkan menginputkan Angka");
        }
    }//GEN-LAST:event_Tekanan1KeyTyped

    private void Tekanan1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tekanan1KeyReleased
        if(Tekanan1.getText().isEmpty()){
           ScoreTekanan.setText("");
       }else{
          tekanan();  
       }
    }//GEN-LAST:event_Tekanan1KeyReleased

    private void Masuk1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Masuk1KeyPressed
        
    }//GEN-LAST:event_Masuk1KeyPressed

    private void Masuk2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Masuk2KeyPressed
        
    }//GEN-LAST:event_Masuk2KeyPressed

    private void JumlahMasukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahMasukKeyPressed
      
    }//GEN-LAST:event_JumlahMasukKeyPressed

    private void Keluar1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar1KeyPressed
        
    }//GEN-LAST:event_Keluar1KeyPressed

    private void Keluar2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar2KeyPressed
        
    }//GEN-LAST:event_Keluar2KeyPressed

    private void Keluar3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar3KeyPressed
        
    }//GEN-LAST:event_Keluar3KeyPressed

    private void Keluar4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar4KeyPressed
        
    }//GEN-LAST:event_Keluar4KeyPressed

    private void Keluar5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Keluar5KeyPressed
        
    }//GEN-LAST:event_Keluar5KeyPressed

    private void JumlahKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahKeluarKeyPressed
        
    }//GEN-LAST:event_JumlahKeluarKeyPressed

    private void BCKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BCKeyPressed
        isBc();
    }//GEN-LAST:event_BCKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            EWSRanap dialog = new EWSRanap(new javax.swing.JFrame(), true);
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
    private widget.ComboBox Alat;
    private widget.TextBox BB;
    private widget.TextBox BC;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox Denyut1;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Frekuensi;
    private widget.TextBox Jk;
    private widget.TextBox JumlahKeluar;
    private widget.TextBox JumlahMasuk;
    private widget.TextBox KdPetugas;
    private widget.TextBox Keluar1;
    private widget.TextBox Keluar2;
    private widget.TextBox Keluar3;
    private widget.TextBox Keluar4;
    private widget.TextBox Keluar5;
    private widget.ComboBox Kesadaran;
    private widget.TextBox Klasifikasi;
    private widget.Label LCount;
    private widget.TextBox LK;
    private widget.TextBox LP;
    private widget.editorpane LoadHTML;
    private widget.TextBox Masuk1;
    private widget.TextBox Masuk2;
    private widget.TextBox NmPetugas;
    private usu.widget.glass.PanelGlass PanelWall;
    private widget.TextBox Pernafasan1;
    private widget.TextArea Respon;
    private widget.TextBox Saturasi1;
    private widget.TextBox ScoreAlat;
    private widget.TextBox ScoreDenyut;
    private widget.TextBox ScoreKesadaran;
    private widget.TextBox ScorePernafasan;
    private widget.TextBox ScoreSaturasi;
    private widget.TextBox ScoreSuhu;
    private widget.TextBox ScoreTekanan;
    private widget.ScrollPane Scroll;
    private widget.ComboBox SkalaNyeri;
    private widget.TextBox Suhu1;
    private widget.TextBox TB;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox Tekanan1;
    private widget.TextBox TglLahir;
    private widget.TextArea Tindakan;
    private widget.TextBox Total;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel106;
    private widget.Label jLabel11;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel219;
    private widget.Label jLabel220;
    private widget.Label jLabel222;
    private widget.Label jLabel224;
    private widget.Label jLabel225;
    private widget.Label jLabel227;
    private widget.Label jLabel228;
    private widget.Label jLabel230;
    private widget.Label jLabel231;
    private widget.Label jLabel233;
    private widget.Label jLabel234;
    private widget.Label jLabel236;
    private widget.Label jLabel237;
    private widget.Label jLabel238;
    private widget.Label jLabel240;
    private widget.Label jLabel241;
    private widget.Label jLabel243;
    private widget.Label jLabel244;
    private widget.Label jLabel245;
    private widget.Label jLabel246;
    private widget.Label jLabel247;
    private widget.Label jLabel248;
    private widget.Label jLabel249;
    private widget.Label jLabel250;
    private widget.Label jLabel251;
    private widget.Label jLabel252;
    private widget.Label jLabel253;
    private widget.Label jLabel254;
    private widget.Label jLabel255;
    private widget.Label jLabel256;
    private widget.Label jLabel257;
    private widget.Label jLabel258;
    private widget.Label jLabel259;
    private widget.Label jLabel260;
    private widget.Label jLabel262;
    private widget.Label jLabel53;
    private widget.Label jLabel54;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel85;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private widget.Label label14;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane17;
    private widget.ScrollPane scrollPane18;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,ews_ranap.tanggal,ews_ranap.jam,ews_ranap.nik,pegawai.nama,"+
                        "ews_ranap.pernafasan,ews_ranap.score_pernafasan,ews_ranap.saturasi,ews_ranap.score_saturasi,ews_ranap.alat,ews_ranap.score_alat,ews_ranap.suhu,ews_ranap.score_suhu,"+
                        "ews_ranap.denyut,ews_ranap.score_denyut,ews_ranap.tekanan,ews_ranap.score_tekanan,ews_ranap.kesadaran,ews_ranap.score_kesadaran,ews_ranap.total_score,ews_ranap.klasifikasi,ews_ranap.respon,"+
                        "ews_ranap.tindakan,ews_ranap.frekuensi,ews_ranap.skala_nyeri,ews_ranap.bb,ews_ranap.tb,ews_ranap.lk,ews_ranap.lp,ews_ranap.masuk1,ews_ranap.masuk2,ews_ranap.jumlahmasuk,"+
                        "ews_ranap.keluar1,ews_ranap.keluar2,ews_ranap.keluar3,ews_ranap.keluar4,ews_ranap.keluar5,ews_ranap.jumlahkeluar,ews_ranap.bc "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join ews_ranap on reg_periksa.no_rawat=ews_ranap.no_rawat "+
                        "inner join pegawai on ews_ranap.nik=pegawai.nik "+
                        "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                        "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                        "ews_ranap.tanggal between ? and ? order by ews_ranap.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,ews_ranap.tanggal,ews_ranap.jam,ews_ranap.nik,pegawai.nama,"+
                        "ews_ranap.pernafasan,ews_ranap.score_pernafasan,ews_ranap.saturasi,ews_ranap.score_saturasi,ews_ranap.alat,ews_ranap.score_alat,ews_ranap.suhu,ews_ranap.score_suhu,"+
                        "ews_ranap.denyut,ews_ranap.score_denyut,ews_ranap.tekanan,ews_ranap.score_tekanan,ews_ranap.kesadaran,ews_ranap.score_kesadaran,ews_ranap.total_score,ews_ranap.klasifikasi,ews_ranap.respon,"+
                        "ews_ranap.tindakan,ews_ranap.frekuensi,ews_ranap.skala_nyeri,ews_ranap.bb,ews_ranap.tb,ews_ranap.lk,ews_ranap.lp,ews_ranap.masuk1,ews_ranap.masuk2,ews_ranap.jumlahmasuk,"+
                        "ews_ranap.keluar1,ews_ranap.keluar2,ews_ranap.keluar3,ews_ranap.keluar4,ews_ranap.keluar5,ews_ranap.jumlahkeluar,ews_ranap.bc "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join ews_ranap on reg_periksa.no_rawat=ews_ranap.no_rawat "+
                        "inner join pegawai on ews_ranap.nik=pegawai.nik "+
                        "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                        "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                        "ews_ranap.tanggal between ? and ? and reg_periksa.no_rawat like ? or "+
                        "ews_ranap.tanggal between ? and ? and pasien.no_rkm_medis like ? or "+
                        "ews_ranap.tanggal between ? and ? and pasien.nm_pasien like ? or "+
                        "ews_ranap.tanggal between ? and ? and ews_ranap.nik like ? or "+
                        "ews_ranap.tanggal between ? and ? and pegawai.nama like ? order by ews_ranap.tanggal");
            }
                
            try {
                if(TCari.getText().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(5,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(8,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(9,"%"+TCari.getText()+"%");
                    ps.setString(10,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(11,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(12,"%"+TCari.getText()+"%");
                    ps.setString(13,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(14,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(15,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("jk"),rs.getString("nik"),rs.getString("nama"),rs.getString("tanggal"),rs.getString("jam"),
                        rs.getString("pernafasan"),rs.getString("score_pernafasan"),rs.getString("saturasi"),rs.getString("score_saturasi"),rs.getString("alat"),rs.getString("score_alat"),
                        rs.getString("suhu"),rs.getString("score_suhu"),rs.getString("denyut"),rs.getString("score_denyut"),rs.getString("tekanan"),rs.getString("score_tekanan"),
                        rs.getString("kesadaran"),rs.getString("score_kesadaran"),rs.getString("total_score"),rs.getString("klasifikasi"),rs.getString("respon"),rs.getString("tindakan"),
                        rs.getString("frekuensi"),rs.getString("skala_nyeri"),rs.getString("bb"),rs.getString("tb"),rs.getString("lk"),rs.getString("lp"),rs.getString("masuk1"),rs.getString("masuk2"),rs.getString("jumlahmasuk"),
                        rs.getString("keluar1"),rs.getString("keluar2"),rs.getString("keluar3"),rs.getString("keluar4"),rs.getString("keluar5"),rs.getString("jumlahkeluar"),rs.getString("bc")
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
    
//    private void isjml(){
//        if((!ScorePernafasan.getText().equals(""))&&(!ScoreSaturasi.getText().equals(""))&&(!ScoreAlat.getText().equals(""))&&(!ScoreSuhu.getText().equals(""))&&(!ScoreDenyut.getText().equals(""))&&(!ScoreTekanan.getText().equals(""))&&(!ScoreKesadaran.getText().equals(""))){
//            Total.setText(Valid.SetAngka2(
//                    Double.parseDouble(ScorePernafasan.getText().trim())+
//                    Double.parseDouble(ScoreSaturasi.getText().trim())+
//                    Double.parseDouble(ScoreAlat.getText().trim())+
//                    Double.parseDouble(ScoreSuhu.getText().trim())+
//                    Double.parseDouble(ScoreDenyut.getText().trim())+
//                    Double.parseDouble(ScoreTekanan.getText().trim())+
//                    Double.parseDouble(ScoreKesadaran.getText().trim())
//            ));
//        }
//    }
    
    private void pernafasan(){
        if(Integer.parseInt(Pernafasan1.getText())>=25){
            ScorePernafasan.setText("3");
        }else if(Integer.parseInt(Pernafasan1.getText())>=24){
            ScorePernafasan.setText("2");
        }else if(Integer.parseInt(Pernafasan1.getText())>=21){
            ScorePernafasan.setText("2");
        }else if(Integer.parseInt(Pernafasan1.getText())>=20){
            ScorePernafasan.setText("0");
        }else if(Integer.parseInt(Pernafasan1.getText())>=12){
            ScorePernafasan.setText("0");
        }else if(Integer.parseInt(Pernafasan1.getText())>=11){
            ScorePernafasan.setText("1");
        }else if(Integer.parseInt(Pernafasan1.getText())>=9){
            ScorePernafasan.setText("1");
        }else if(Integer.parseInt(Pernafasan1.getText())<=8){
            ScorePernafasan.setText("3");
        }
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }
    
    private void saturasi(){
        if(Integer.parseInt(Saturasi1.getText())>=96){
            ScoreSaturasi.setText("0");
        }else if(Integer.parseInt(Saturasi1.getText())>=95){
            ScoreSaturasi.setText("1");
        }else if(Integer.parseInt(Saturasi1.getText())>=94){
            ScoreSaturasi.setText("1");
        }else if(Integer.parseInt(Saturasi1.getText())>=93){
            ScoreSaturasi.setText("2");
        }else if(Integer.parseInt(Saturasi1.getText())>=92){
            ScoreSaturasi.setText("2");
        }else if(Integer.parseInt(Saturasi1.getText())<=91){
            ScoreSaturasi.setText("3");
        }
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }
    
    private void suhu(){
        if(Double.parseDouble(Suhu1.getText())>=39.1){
            ScoreSuhu.setText("2");
        }else if(Double.parseDouble(Suhu1.getText())>=39.0){
            ScoreSuhu.setText("1");
        }else if(Double.parseDouble(Suhu1.getText())>=38.1){
            ScoreSuhu.setText("1");
        }else if(Double.parseDouble(Suhu1.getText())>=38.0){
            ScoreSuhu.setText("0");
        }else if(Double.parseDouble(Suhu1.getText())>=36.1){
            ScoreSuhu.setText("0");
        }else if(Double.parseDouble(Suhu1.getText())>=36.0){
            ScoreSuhu.setText("1");
        }else if(Double.parseDouble(Suhu1.getText())>=35.1){
            ScoreSuhu.setText("1");
        }else if(Double.parseDouble(Suhu1.getText())<=35.0){
            ScoreSuhu.setText("3");
        }
        //itungannya mana
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }
    
    private void denyut(){
        if(Integer.parseInt(Denyut1.getText())>=131){
            ScoreDenyut.setText("3");
        }else if(Integer.parseInt(Denyut1.getText())>=130){
            ScoreDenyut.setText("2");
        }else if(Integer.parseInt(Denyut1.getText())>=111){
            ScoreDenyut.setText("2");
        }else if(Integer.parseInt(Denyut1.getText())>=110){
            ScoreDenyut.setText("1");
        }else if(Integer.parseInt(Denyut1.getText())>=91){
            ScoreDenyut.setText("1");
        }else if(Integer.parseInt(Denyut1.getText())>=90){
            ScoreDenyut.setText("0");
        }else if(Integer.parseInt(Denyut1.getText())>=51){
            ScoreDenyut.setText("0");
        }else if(Integer.parseInt(Denyut1.getText())>=50){
            ScoreDenyut.setText("1");
        }else if(Integer.parseInt(Denyut1.getText())>=41){
            ScoreDenyut.setText("1");
        }else if(Integer.parseInt(Denyut1.getText())<=40){
            ScoreDenyut.setText("3");
        }
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }
    
    private void tekanan(){
        if(Integer.parseInt(Tekanan1.getText())>=220){
            ScoreTekanan.setText("3");
        }else if(Integer.parseInt(Tekanan1.getText())>=219){
            ScoreTekanan.setText("0");
        }else if(Integer.parseInt(Tekanan1.getText())>=111){
            ScoreTekanan.setText("0");
        }else if(Integer.parseInt(Tekanan1.getText())>=110){
            ScoreTekanan.setText("1");
        }else if(Integer.parseInt(Tekanan1.getText())>=101){
            ScoreTekanan.setText("1");
        }else if(Integer.parseInt(Tekanan1.getText())>=100){
            ScoreTekanan.setText("2");
        }else if(Integer.parseInt(Tekanan1.getText())>=91){
            ScoreTekanan.setText("2");
        }else if(Integer.parseInt(Tekanan1.getText())<=90){
            ScoreTekanan.setText("3");
        }
        isTotalKlasifikasi();
        isTotalRespon();
        isTotalTindakan();
        isTotalFrekuensi();
    }
    
    private void isTotalKlasifikasi(){
        try {
            Total.setText((Integer.parseInt(ScorePernafasan.getText())+Integer.parseInt(ScoreSaturasi.getText())+Integer.parseInt(ScoreAlat.getText())+Integer.parseInt(ScoreSuhu.getText())+Integer.parseInt(ScoreDenyut.getText())+Integer.parseInt(ScoreTekanan.getText())+Integer.parseInt(ScoreKesadaran.getText()))+"");
            if(Total.getText().equals("0")){
                Klasifikasi.setText("Sangat Rendah");
            }else if(Total.getText().equals("1")){
                Klasifikasi.setText("Rendah");
            }else if(Total.getText().equals("2")){
                Klasifikasi.setText("Rendah");
            }else if(Total.getText().equals("3")){
                Klasifikasi.setText("Rendah");
            }else if(Total.getText().equals("4")){
                Klasifikasi.setText("Rendah");
            }else if(Total.getText().equals("5")){
                Klasifikasi.setText("Sedang");
            }else if(Total.getText().equals("6")){
                Klasifikasi.setText("Sedang");
            }else{
            Klasifikasi.setText("Tinggi");
        }
        } catch (Exception e) {
            Total.setText("0");
        }
      }
    
    private void isTotalRespon(){
        try {
            Total.setText((Integer.parseInt(ScorePernafasan.getText())+Integer.parseInt(ScoreSaturasi.getText())+Integer.parseInt(ScoreAlat.getText())+Integer.parseInt(ScoreSuhu.getText())+Integer.parseInt(ScoreDenyut.getText())+Integer.parseInt(ScoreTekanan.getText())+Integer.parseInt(ScoreKesadaran.getText()))+"");
            if(Total.getText().equals("0")){
                Respon.setText("Dilakukan monitoring");
            }else if(Total.getText().equals("1")){
                Respon.setText(
                        "Harus segera dievaluasi oleh perawat \n"
                        + "terdaftar yang kompeten, harus memutuskan \n"
                        + "apakah perubahan frekuensi pemantauan \n"
                        + "klinis atau wajib eskalasi perawatan klinis");
            }else if(Total.getText().equals("2")){
                Respon.setText(
                        "Harus segera dievaluasi oleh perawat \n"
                        + "terdaftar yang kompeten, harus memutuskan \n"
                        + "apakah perubahan frekuensi pemantauan \n"
                        + "klinis atau wajib eskalasi perawatan klinis");
            }else if(Total.getText().equals("3")){
                Respon.setText(
                        "Harus segera dievaluasi oleh perawat \n"
                        + "terdaftar yang kompeten, harus memutuskan \n"
                        + "apakah perubahan frekuensi pemantauan \n"
                        + "klinis atau wajib eskalasi perawatan klinis");
            }else if(Total.getText().equals("4")){
                 Respon.setText(
                        "Harus segera dievaluasi oleh perawat \n"
                        + "terdaftar yang kompeten, harus memutuskan \n"
                        + "apakah perubahan frekuensi pemantauan \n"
                        + "klinis atau wajib eskalasi perawatan klinis");
            }else if(Total.getText().equals("5")){
                 Respon.setText(
                        "Harus segera melakukan tinjauan \n"
                        + "mendesak oleh klinis yang terampil \n"
                        + "dengan kompetensi dalam penilaian \n"
                        + "penyakit akut di bangsal biasanya \n"
                        + "oleh dokter atau perawat dengan \n"
                        + "mempertimbangkan apakah eskalasi \n"
                        + "perawatan ke tim perawatan kritis \n"
                        + "diperlukan (yaitu tim penjangkauan perawatan kritis)");
            }else if(Total.getText().equals("6")){
                 Respon.setText(
                        "Harus segera melakukan tinjauan \n"
                        + "mendesak oleh klinis yang terampil \n"
                        + "dengan kompetensi dalam penilaian \n"
                        + "penyakit akut di bangsal biasanya \n"
                        + "oleh dokter atau perawat dengan \n"
                        + "mempertimbangkan apakah eskalasi \n"
                        + "perawatan ke tim perawatan kritis \n"
                        + "diperlukan (yaitu tim penjangkauan perawatan kritis)");
            }else{
            Respon.setText(
                        "Harus segera memberikan penilaian \n"
                        + "darurat secara klinis oleh tim \n"
                        + "critical care outreach atau code blue \n"
                        + "dengan kompetensi penanganan pasien \n"
                        + "kritis dan biasanya terjadi transper \n"
                        + "pasien ke area perawatan dengan alat bantu");
        }
        } catch (Exception e) {
            Total.setText("0");
        }
      }
    
    private void isTotalTindakan(){
        try {
            Total.setText((Integer.parseInt(ScorePernafasan.getText())+Integer.parseInt(ScoreSaturasi.getText())+Integer.parseInt(ScoreAlat.getText())+Integer.parseInt(ScoreSuhu.getText())+Integer.parseInt(ScoreDenyut.getText())+Integer.parseInt(ScoreTekanan.getText())+Integer.parseInt(ScoreKesadaran.getText()))+"");
            if(Total.getText().equals("0")){
                Tindakan.setText("Melanjutkan monitoring");
            }else if(Total.getText().equals("1")){
                Tindakan.setText(
                        "Perawat menassesment atau perawat meningkatkan \n"
                        + "frekuensi monitor");
            }else if(Total.getText().equals("2")){
                 Tindakan.setText(
                        "Perawat menassesment atau perawat meningkatkan \n"
                        + "frekuensi monitor");
            }else if(Total.getText().equals("3")){
                 Tindakan.setText(
                        "Perawat menassesment atau perawat meningkatkan \n"
                        + "frekuensi monitor");
            }else if(Total.getText().equals("4")){
                  Tindakan.setText(
                        "Perawat menassesment atau perawat meningkatkan \n"
                        + "frekuensi monitor");
            }else if(Total.getText().equals("5")){
                 Tindakan.setText(
                        "Perawat berkolaborasi dengan tim/pemberian \n"
                        + "assesment kegawatan/meningkatkan perawatan \n"
                        + "dengan fasilitas monitor yang lengkap");
            }else if(Total.getText().equals("6")){
                 Tindakan.setText(
                        "Perawat berkolaborasi dengan tim/pemberian \n"
                        + "assesment kegawatan/meningkatkan perawatan \n"
                        + "dengan fasilitas monitor yang lengkap");
            }else{
            Tindakan.setText(
                        "Berkolaborasi dengan tim medis/pemberian \n"
                        + "assesment kegawatan/pindah ruang HCU/ICU");
        }
        } catch (Exception e) {
            Total.setText("0");
        }
      }
    
    private void isTotalFrekuensi(){
        try {
            Total.setText((Integer.parseInt(ScorePernafasan.getText())+Integer.parseInt(ScoreSaturasi.getText())+Integer.parseInt(ScoreAlat.getText())+Integer.parseInt(ScoreSuhu.getText())+Integer.parseInt(ScoreDenyut.getText())+Integer.parseInt(ScoreTekanan.getText())+Integer.parseInt(ScoreKesadaran.getText()))+"");
            if(Total.getText().equals("0")){
                Frekuensi.setText("Minimal 12 Jam");
            }else if(Total.getText().equals("1")){
                Frekuensi.setText("Minimal 4-6 Jam");
            }else if(Total.getText().equals("2")){
                Frekuensi.setText("Minimal 4-6 Jam");
            }else if(Total.getText().equals("3")){
                Frekuensi.setText("Minimal 4-6 Jam");
            }else if(Total.getText().equals("4")){
                Frekuensi.setText("Minimal 4-6 Jam");
            }else if(Total.getText().equals("5")){
                Frekuensi.setText("Minimal 1 Jam");
            }else if(Total.getText().equals("6")){
                Frekuensi.setText("Minimal 1 Jam");
            }else{
            Frekuensi.setText("Bed side monitor/every time");
        }
        } catch (Exception e) {
            Total.setText("0");
        }
      }
    
    private void isMasuk(){
        try {
            JumlahMasuk.setText((Integer.parseInt(Masuk1.getText())+Integer.parseInt(Masuk2.getText()))+"");
        isBc();
        } catch (Exception e) {
            JumlahMasuk.setText("0");
        }
    }
    
    private void isKeluar(){
        try {
            JumlahKeluar.setText((Integer.parseInt(Keluar1.getText())+Integer.parseInt(Keluar2.getText())+Integer.parseInt(Keluar3.getText())+Integer.parseInt(Keluar4.getText())+Integer.parseInt(Keluar5.getText()))+"");
        isBc();
        } catch (Exception e) {
            JumlahKeluar.setText("0");
        }
    }
    
    private void isBc(){
        try {
            BC.setText((Integer.parseInt(JumlahMasuk.getText())-Integer.parseInt(JumlahKeluar.getText()))+"");
        } catch (Exception e) {
            BC.setText("0");
        }
    }
    
//    private void isTotalKlasifikasi(){
//            if(Total.getText().equals("0")){
//                Klasifikasi.setText("Sangat Rendah");
//            }else if(Total.getText().equals("1")){
//                Klasifikasi.setText("Rendah");
//            }else if(Total.getText().equals("2")){
//                Klasifikasi.setText("Rendah");
//            }else if(Total.getText().equals("3")){
//                Klasifikasi.setText("Rendah");
//            }else if(Total.getText().equals("4")){
//                Klasifikasi.setText("Rendah");
//            }else if(Total.getText().equals("5")){
//                Klasifikasi.setText("Sedang");
//            }else if(Total.getText().equals("6")){
//                Klasifikasi.setText("Sedang");
//            }else{
//            Klasifikasi.setText("Tinggi");
//        }
//    }

    public void emptTeks() {
        Pernafasan1.setText("");
        ScorePernafasan.setText("0");
        Saturasi1.setText("");
        ScoreSaturasi.setText("0");
        Alat.setSelectedIndex(0);
        ScoreAlat.setText("2");
        Suhu1.setText("");
        ScoreSuhu.setText("0");
        Denyut1.setText("");
        ScoreDenyut.setText("0");
        Tekanan1.setText("");
        ScoreTekanan.setText("0");
        Kesadaran.setSelectedIndex(0);
        ScoreKesadaran.setText("0");
        Total.setText("2");
        Klasifikasi.setText("");
        Respon.setText("");
        Tindakan.setText("");
        Frekuensi.setText("");
        SkalaNyeri.setSelectedIndex(0);
        BB.setText("");
        TB.setText("");
        LK.setText("");
        LP.setText("");
        Masuk1.setText("");
        Masuk2.setText("");
        JumlahMasuk.setText("0");
        Keluar1.setText("");
        Keluar2.setText("");
        Keluar3.setText("");
        Keluar4.setText("");
        Keluar5.setText("");
        JumlahKeluar.setText("0");
        BC.setText("0");
        TNoRw.requestFocus();
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            Jk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()); 
            KdPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            NmPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            Pernafasan1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            ScorePernafasan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            Saturasi1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            ScoreSaturasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            Alat.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            ScoreAlat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Suhu1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            ScoreSuhu.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            Denyut1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            ScoreDenyut.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            Tekanan1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            ScoreTekanan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            Kesadaran.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            ScoreKesadaran.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            Total.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            Klasifikasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            Respon.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            Tindakan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString());
            Frekuensi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString());
            SkalaNyeri.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());
            BB.setText(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());
            TB.setText(tbObat.getValueAt(tbObat.getSelectedRow(),30).toString());
            LK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),31).toString());
            LP.setText(tbObat.getValueAt(tbObat.getSelectedRow(),32).toString());
            Masuk1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),33).toString());
            Masuk2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),34).toString());
            JumlahMasuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),35).toString());
            Keluar1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),36).toString());
            Keluar2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),37).toString());
            Keluar3.setText(tbObat.getValueAt(tbObat.getSelectedRow(),38).toString());
            Keluar4.setText(tbObat.getValueAt(tbObat.getSelectedRow(),39).toString());
            Keluar5.setText(tbObat.getValueAt(tbObat.getSelectedRow(),40).toString());
            JumlahKeluar.setText(tbObat.getValueAt(tbObat.getSelectedRow(),41).toString());
            BC.setText(tbObat.getValueAt(tbObat.getSelectedRow(),42).toString());
            
             
            
//            try {
//                Valid.tabelKosong(tabModeMasalah);
//                
//                ps=koneksi.prepareStatement(
//                        "select master_masalah_keperawatan.kode_masalah,master_masalah_keperawatan.nama_masalah from master_masalah_keperawatan "+
//                        "inner join penilaian_awal_keperawatan_ralan_masalah on penilaian_awal_keperawatan_ralan_masalah.kode_masalah=master_masalah_keperawatan.kode_masalah "+
//                        "where penilaian_awal_keperawatan_ralan_masalah.no_rawat=? order by kode_masalah");
//                try {
//                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
//                    rs=ps.executeQuery();
//                    while(rs.next()){
//                        tabModeMasalah.addRow(new Object[]{true,rs.getString(1),rs.getString(2)});
//                    }
//                } catch (Exception e) {
//                    System.out.println("Notif : "+e);
//                } finally{
//                    if(rs!=null){
//                        rs.close();
//                    }
//                    if(ps!=null){
//                        ps.close();
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Notif : "+e);
//            }
        }
    }

    private void isRawat() {
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
        try {
            ps=koneksi.prepareStatement(
                    "select nm_pasien, if(jk='L','Laki-Laki','Perempuan') as jk,tgl_lahir,agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat "+
                    "from pasien inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik "+
                    "where no_rkm_medis=?");
            try {
                ps.setString(1,TNoRM.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
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
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
        Sequel.cariIsi("select if(jk='L','Laki-Laki','Perempuan') from pasien where no_rkm_medis=? ",Jk,TNoRM.getText());
        Sequel.cariIsi("select tgl_lahir from pasien where no_rkm_medis=? ",TglLahir,TNoRM.getText());
    }
    
    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        Sequel.cariIsi("select tgl_registrasi from reg_periksa where no_rawat='"+norwt+"'", DTPCari1);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        isPsien();
    }
    
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.gettindakan_ranap());
        BtnHapus.setEnabled(akses.gettindakan_ranap());
        BtnEdit.setEnabled(akses.gettindakan_ranap());
        BtnEdit.setEnabled(akses.gettindakan_ranap());
//        BtnTambahMasalah.setEnabled(akses.getmaster_masalah_keperawatan());  
        if(akses.getjml2()>=1){
            KdPetugas.setEditable(false);
            BtnDokter.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            Sequel.cariIsi("select nama from pegawai where nik=?", NmPetugas,KdPetugas.getText());
//            if(NmPetugas.getText().equals("")){
//                KdPetugas.setText("");
//                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
//            }
        }            
    }

    public void setTampil(){
       TabRawat.setSelectedIndex(1);
       tampil();
    }
    
//    private void tampilMasalah() {
//        try{
//            jml=0;
//            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
//                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
//                    jml++;
//                }
//            }
//
//            pilih=null;
//            pilih=new boolean[jml]; 
//            kode=null;
//            kode=new String[jml];
//            masalah=null;
//            masalah=new String[jml];
//
//            index=0;        
//            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
//                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
//                    pilih[index]=true;
//                    kode[index]=tbMasalahKeperawatan.getValueAt(i,1).toString();
//                    masalah[index]=tbMasalahKeperawatan.getValueAt(i,2).toString();
//                    index++;
//                }
//            } 
//
//            Valid.tabelKosong(tabModeMasalah);
//
//            for(i=0;i<jml;i++){
//                tabModeMasalah.addRow(new Object[] {
//                    pilih[i],kode[i],masalah[i]
//                });
//            }
//            ps=koneksi.prepareStatement("select * from master_masalah_keperawatan where kode_masalah like ? or nama_masalah like ? order by kode_masalah");
//            try {
//                ps.setString(1,"%"+TCariMasalah.getText().trim()+"%");
//                ps.setString(2,"%"+TCariMasalah.getText().trim()+"%");
//                rs=ps.executeQuery();
//                while(rs.next()){
//                    tabModeMasalah.addRow(new Object[]{false,rs.getString(1),rs.getString(2)});
//                }
//            } catch (Exception e) {
//                System.out.println("Notif : "+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(ps!=null){
//                    ps.close();
//                }
//            }
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }
//    }
    
//    private void isMenu(){
//        if(ChkAccor.isSelected()==true){
//            ChkAccor.setVisible(false);
//            PanelAccor.setPreferredSize(new Dimension(470,HEIGHT));
//            FormMenu.setVisible(true);  
//            FormMasalahRencana.setVisible(true);  
//            ChkAccor.setVisible(true);
//        }else if(ChkAccor.isSelected()==false){   
//            ChkAccor.setVisible(false);
//            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
//            FormMenu.setVisible(false);  
//            FormMasalahRencana.setVisible(false);   
//            ChkAccor.setVisible(true);
//        }
//    }

//    private void getMasalah() {
//        if(tbObat.getSelectedRow()!= -1){
//            TNoRM1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
//            TPasien1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()); 
//            DetailRencana.setText(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString());
//            try {
//                Valid.tabelKosong(tabModeDetailMasalah);
//                ps=koneksi.prepareStatement(
//                        "select ews_ranap.tata from ews_ranap "+
//                        "where ews_ranap.no_rawat=? ");
//                try {
//                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
//                    rs=ps.executeQuery();
//                    while(rs.next()){
//                        tabModeDetailMasalah.addRow(new Object[]{rs.getString(1),rs.getString(2)});
//                    }
//                } catch (Exception e) {
//                    System.out.println("Notif : "+e);
//                } finally{
//                    if(rs!=null){
//                        rs.close();
//                    }
//                    if(ps!=null){
//                        ps.close();
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Notif : "+e);
//            }
//        }
//    }
    
//    private void isBMI(){
//        if((!TB.getText().equals(""))&&(!BB.getText().equals(""))){
//            BMI.setText(Valid.SetAngka7(Valid.SetAngka(BB.getText())/((Valid.SetAngka(TB.getText())/100)*(Valid.SetAngka(TB.getText())/100)))+"");
//        }
//    }
}
