/*
 * By Mas Elkhanza
 */


package rekammedis;

import fungsi.MetadataBerkas;
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
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
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
import kepegawaian.DlgCariPetugas;
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
 * @author perpustakaan
 */
public final class FormulirKFRProtokolTerapi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private RMCariKeluhan carikeluhan=new RMCariKeluhan(null,false);
//    private RMCariAsesmen cariasesmen=new RMCariAsesmen(null,false);
//    private RMCariObjek cariobjek=new RMCariObjek(null,false);
    private StringBuilder htmlContent;
    private String finger="", finger1="",FileName = "", kodeberkas = "";
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public FormulirKFRProtokolTerapi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No. Rawat",
            "No. Rekam Medis",
            "Nama Pasien",
            "Tgl. Lahir",
            "Umur",
            "JK",
            "Tanggal Formulir",
            "Subjektif",
            "Objektif",
            "Assesment",
            "Goal Treatment",
            "Tindakan Program Rehab Medik",
            "Edukasi",
            "Frekuensi Kunjungan",
            "RTL",
            "Kode Dokter",
            "Nama Dokter"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 17; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(100);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(60);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(30);
            }else if(i==6){
                column.setPreferredWidth(120);
            }else if(i==7){
                column.setPreferredWidth(150);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(150);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(160);
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
            }
            
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    KdDokter.requestFocus();
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
        
        carikeluhan.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(carikeluhan.getTable().getSelectedRow()!= -1){
                    Subjektif.append(carikeluhan.getTable().getValueAt(carikeluhan.getTable().getSelectedRow(),2).toString()+", ");
                    Subjektif.requestFocus();
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
        
//        cariasesmen.addWindowListener(new WindowListener() {
//            @Override
//            public void windowOpened(WindowEvent e) {}
//            @Override
//            public void windowClosing(WindowEvent e) {}
//            @Override
//            public void windowClosed(WindowEvent e) {
//                if(cariasesmen.getTable().getSelectedRow()!= -1){
//                    GoalTreatment.setText(cariasesmen.getTable().getValueAt(cariasesmen.getTable().getSelectedRow(),2).toString()+", ");
//                    GoalTreatment.requestFocus();
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
//        
//        cariobjek.addWindowListener(new WindowListener() {
//            @Override
//            public void windowOpened(WindowEvent e) {}
//            @Override
//            public void windowClosing(WindowEvent e) {}
//            @Override
//            public void windowClosed(WindowEvent e) {
//                if(cariobjek.getTable().getSelectedRow()!= -1){
//                    Objektif.setText(cariobjek.getTable().getValueAt(cariobjek.getTable().getSelectedRow(),2).toString()+", ");
//                    Objektif.requestFocus();
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
        
        jam();
        
        Alamat.setVisible(false);
        TglLahir.setVisible(false);
        JK.setVisible(false);
        
        

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
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnFormulirRawatJalanKFR = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnUpload = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TglLahir = new widget.TextBox();
        JK = new widget.TextBox();
        jLabel10 = new widget.Label();
        jLabel111 = new widget.Label();
        scrollPane16 = new widget.ScrollPane();
        Assesment = new widget.TextArea();
        jLabel112 = new widget.Label();
        scrollPane17 = new widget.ScrollPane();
        GoalTreatment = new widget.TextArea();
        jLabel113 = new widget.Label();
        scrollPane18 = new widget.ScrollPane();
        TindakanProgramRehabMedik = new widget.TextArea();
        jLabel114 = new widget.Label();
        scrollPane19 = new widget.ScrollPane();
        Edukasi = new widget.TextArea();
        jLabel16 = new widget.Label();
        Tanggal = new widget.Tanggal();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        Alamat = new widget.TextBox();
        jLabel18 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel109 = new widget.Label();
        scrollPane14 = new widget.ScrollPane();
        Subjektif = new widget.TextArea();
        jLabel110 = new widget.Label();
        scrollPane15 = new widget.ScrollPane();
        Objektif = new widget.TextArea();
        jLabel115 = new widget.Label();
        scrollPane20 = new widget.ScrollPane();
        FrekuensiKunjungan = new widget.TextArea();
        jLabel116 = new widget.Label();
        scrollPane21 = new widget.ScrollPane();
        RTL = new widget.TextArea();
        tgl_bertemu_dokter = new widget.Tanggal();
        jLabel17 = new widget.Label();
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

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnFormulirRawatJalanKFR.setBackground(new java.awt.Color(255, 255, 254));
        MnFormulirRawatJalanKFR.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnFormulirRawatJalanKFR.setForeground(new java.awt.Color(50, 50, 50));
        MnFormulirRawatJalanKFR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnFormulirRawatJalanKFR.setText("Formulir Rawat Jalan KFR Protokol Terapi");
        MnFormulirRawatJalanKFR.setName("MnFormulirRawatJalanKFR"); // NOI18N
        MnFormulirRawatJalanKFR.setPreferredSize(new java.awt.Dimension(270, 26));
        MnFormulirRawatJalanKFR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnFormulirRawatJalanKFRActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnFormulirRawatJalanKFR);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(1600, 800));
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Formulir Rawat Jalan KFR/Asesmen/Re-Asesmen/Protokol Terapi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1600, 800));
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
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.setPreferredSize(new java.awt.Dimension(457, 900));
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setPreferredSize(new java.awt.Dimension(102, 600));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 500));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(102, 500));
        FormInput.setLayout(null);

        TNoRw.setForeground(new java.awt.Color(0, 0, 0));
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(90, 20, 130, 30);

        TPasien.setEditable(false);
        TPasien.setForeground(new java.awt.Color(0, 0, 0));
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(320, 20, 470, 30);

        TNoRM.setEditable(false);
        TNoRM.setForeground(new java.awt.Color(0, 0, 0));
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(230, 20, 80, 30);

        TglLahir.setEditable(false);
        TglLahir.setForeground(new java.awt.Color(0, 0, 0));
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(1060, 20, 100, 30);

        JK.setEditable(false);
        JK.setForeground(new java.awt.Color(0, 0, 0));
        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N
        FormInput.add(JK);
        JK.setBounds(810, 60, 240, 30);

        jLabel10.setText("No. Rawat  :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 20, 85, 30);

        jLabel111.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel111.setText("Assessment :");
        jLabel111.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel111.setName("jLabel111"); // NOI18N
        FormInput.add(jLabel111);
        jLabel111.setBounds(440, 160, 190, 23);

        scrollPane16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane16.setName("scrollPane16"); // NOI18N

        Assesment.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Assesment.setColumns(20);
        Assesment.setForeground(new java.awt.Color(0, 0, 0));
        Assesment.setRows(5);
        Assesment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Assesment.setName("Assesment"); // NOI18N
        Assesment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AssesmentKeyPressed(evt);
            }
        });
        scrollPane16.setViewportView(Assesment);

        FormInput.add(scrollPane16);
        scrollPane16.setBounds(440, 190, 200, 230);

        jLabel112.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel112.setText("Planning ( Goal of Treatment )");
        jLabel112.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel112.setName("jLabel112"); // NOI18N
        FormInput.add(jLabel112);
        jLabel112.setBounds(670, 160, 210, 23);

        scrollPane17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane17.setName("scrollPane17"); // NOI18N

        GoalTreatment.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        GoalTreatment.setColumns(20);
        GoalTreatment.setForeground(new java.awt.Color(0, 0, 0));
        GoalTreatment.setRows(5);
        GoalTreatment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        GoalTreatment.setName("GoalTreatment"); // NOI18N
        GoalTreatment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GoalTreatmentKeyPressed(evt);
            }
        });
        scrollPane17.setViewportView(GoalTreatment);

        FormInput.add(scrollPane17);
        scrollPane17.setBounds(670, 190, 200, 230);

        jLabel113.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel113.setText("Tindakan Program Rehab Medik");
        jLabel113.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel113.setName("jLabel113"); // NOI18N
        FormInput.add(jLabel113);
        jLabel113.setBounds(880, 160, 210, 23);

        scrollPane18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane18.setName("scrollPane18"); // NOI18N

        TindakanProgramRehabMedik.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TindakanProgramRehabMedik.setColumns(20);
        TindakanProgramRehabMedik.setForeground(new java.awt.Color(0, 0, 0));
        TindakanProgramRehabMedik.setRows(5);
        TindakanProgramRehabMedik.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        TindakanProgramRehabMedik.setName("TindakanProgramRehabMedik"); // NOI18N
        TindakanProgramRehabMedik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TindakanProgramRehabMedikKeyPressed(evt);
            }
        });
        scrollPane18.setViewportView(TindakanProgramRehabMedik);

        FormInput.add(scrollPane18);
        scrollPane18.setBounds(880, 190, 200, 230);

        jLabel114.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel114.setText("Planning ( Edukasi )");
        jLabel114.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel114.setName("jLabel114"); // NOI18N
        FormInput.add(jLabel114);
        jLabel114.setBounds(1090, 160, 200, 23);

        scrollPane19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane19.setName("scrollPane19"); // NOI18N

        Edukasi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Edukasi.setColumns(20);
        Edukasi.setForeground(new java.awt.Color(0, 0, 0));
        Edukasi.setRows(5);
        Edukasi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Edukasi.setName("Edukasi"); // NOI18N
        Edukasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EdukasiKeyPressed(evt);
            }
        });
        scrollPane19.setViewportView(Edukasi);

        FormInput.add(scrollPane19);
        scrollPane19.setBounds(1090, 190, 200, 230);

        jLabel16.setText("Tanggal  :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 60, 85, 30);

        Tanggal.setForeground(new java.awt.Color(0, 0, 0));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-12-2025" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(90, 60, 100, 30);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(190, 60, 50, 30);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(240, 60, 50, 30);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(290, 60, 50, 30);

        ChkKejadian.setBackground(new java.awt.Color(255, 255, 255));
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(350, 60, 23, 30);

        Alamat.setEditable(false);
        Alamat.setForeground(new java.awt.Color(0, 0, 0));
        Alamat.setName("Alamat"); // NOI18N
        Alamat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AlamatActionPerformed(evt);
            }
        });
        FormInput.add(Alamat);
        Alamat.setBounds(810, 20, 240, 26);

        jLabel18.setText("Dokter DPJP  :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(0, 100, 85, 30);

        KdDokter.setEditable(false);
        KdDokter.setForeground(new java.awt.Color(0, 0, 0));
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(90, 100, 130, 30);

        NmDokter.setEditable(false);
        NmDokter.setForeground(new java.awt.Color(0, 0, 0));
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(230, 100, 560, 30);

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
        BtnDokter.setBounds(800, 100, 28, 30);

        jLabel109.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel109.setText("Subjective :");
        jLabel109.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel109.setName("jLabel109"); // NOI18N
        FormInput.add(jLabel109);
        jLabel109.setBounds(20, 160, 190, 23);

        scrollPane14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane14.setName("scrollPane14"); // NOI18N

        Subjektif.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Subjektif.setColumns(20);
        Subjektif.setForeground(new java.awt.Color(0, 0, 0));
        Subjektif.setRows(5);
        Subjektif.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Subjektif.setName("Subjektif"); // NOI18N
        Subjektif.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SubjektifKeyPressed(evt);
            }
        });
        scrollPane14.setViewportView(Subjektif);

        FormInput.add(scrollPane14);
        scrollPane14.setBounds(20, 190, 200, 230);

        jLabel110.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel110.setText("Objective :");
        jLabel110.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel110.setName("jLabel110"); // NOI18N
        FormInput.add(jLabel110);
        jLabel110.setBounds(230, 160, 190, 23);

        scrollPane15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane15.setName("scrollPane15"); // NOI18N

        Objektif.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Objektif.setColumns(20);
        Objektif.setForeground(new java.awt.Color(0, 0, 0));
        Objektif.setRows(5);
        Objektif.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Objektif.setName("Objektif"); // NOI18N
        Objektif.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ObjektifKeyPressed(evt);
            }
        });
        scrollPane15.setViewportView(Objektif);

        FormInput.add(scrollPane15);
        scrollPane15.setBounds(230, 190, 200, 230);

        jLabel115.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel115.setText("Planning ( Frekuensi Kunjungan )");
        jLabel115.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel115.setName("jLabel115"); // NOI18N
        FormInput.add(jLabel115);
        jLabel115.setBounds(670, 440, 240, 23);

        scrollPane20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane20.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane20.setName("scrollPane20"); // NOI18N

        FrekuensiKunjungan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        FrekuensiKunjungan.setColumns(20);
        FrekuensiKunjungan.setForeground(new java.awt.Color(0, 0, 0));
        FrekuensiKunjungan.setRows(5);
        FrekuensiKunjungan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        FrekuensiKunjungan.setName("FrekuensiKunjungan"); // NOI18N
        FrekuensiKunjungan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FrekuensiKunjunganKeyPressed(evt);
            }
        });
        scrollPane20.setViewportView(FrekuensiKunjungan);

        FormInput.add(scrollPane20);
        scrollPane20.setBounds(670, 470, 620, 70);

        jLabel116.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel116.setText("Rencana Tindak Lanjut ( Evaluasi / Rujuk / Selesai )");
        jLabel116.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jLabel116.setName("jLabel116"); // NOI18N
        FormInput.add(jLabel116);
        jLabel116.setBounds(20, 440, 350, 23);

        scrollPane21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPane21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPane21.setName("scrollPane21"); // NOI18N

        RTL.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RTL.setColumns(20);
        RTL.setForeground(new java.awt.Color(0, 0, 0));
        RTL.setRows(5);
        RTL.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        RTL.setName("RTL"); // NOI18N
        RTL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RTLKeyPressed(evt);
            }
        });
        scrollPane21.setViewportView(RTL);

        FormInput.add(scrollPane21);
        scrollPane21.setBounds(20, 470, 620, 70);

        tgl_bertemu_dokter.setForeground(new java.awt.Color(0, 0, 0));
        tgl_bertemu_dokter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-12-2025" }));
        tgl_bertemu_dokter.setDisplayFormat("dd-MM-yyyy");
        tgl_bertemu_dokter.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tgl_bertemu_dokter.setName("tgl_bertemu_dokter"); // NOI18N
        tgl_bertemu_dokter.setOpaque(false);
        tgl_bertemu_dokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tgl_bertemu_dokterKeyPressed(evt);
            }
        });
        FormInput.add(tgl_bertemu_dokter);
        tgl_bertemu_dokter.setBounds(690, 60, 100, 30);

        jLabel17.setText("Jadwal Ketemu Dokter  :");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel17);
        jLabel17.setBounds(390, 60, 290, 30);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Penilaian", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-12-2025" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-12-2025" }));
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
    if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
        Valid.textKosong(TNoRw, "Pasien");
    } else if (KdDokter.getText().trim().equals("") || NmDokter.getText().trim().equals("")) {
        Valid.textKosong(KdDokter, "Dokter");
    } else if (Subjektif.getText().trim().equals("")) {
        Valid.textKosong(Subjektif, "Subjektif");
    } else if (Objektif.getText().trim().equals("")) {
        Valid.textKosong(Objektif, "Objektif");
    } else if (Assesment.getText().trim().equals("")) {
        Valid.textKosong(Assesment, "Assesment");
    } else if (GoalTreatment.getText().trim().equals("")) {
        Valid.textKosong(GoalTreatment, "Goal Treatment");
    } else if (TindakanProgramRehabMedik.getText().trim().equals("")) {
        Valid.textKosong(TindakanProgramRehabMedik, "Tindakan Program Rehab Medik");
    } else if (Edukasi.getText().trim().equals("")) {
        Valid.textKosong(Edukasi, "Edukasi");
    } else if (FrekuensiKunjungan.getText().trim().equals("")) {
        Valid.textKosong(FrekuensiKunjungan, "Frekuensi Kunjungan");
    } else if (RTL.getText().trim().equals("")) {
        Valid.textKosong(RTL, "RTL");
    } else {
        // 🔹 Konfirmasi sebelum simpan
        int konfirmasi = JOptionPane.showConfirmDialog(
            null,
            "Apakah Anda yakin ingin menyimpan data ini?",
            "Konfirmasi Simpan",
            JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi == JOptionPane.YES_OPTION) {
            boolean tersimpan = Sequel.menyimpantf(
                "formulir_kfr_protokol_terapi",
                "?,?,?,?,?,?,?,?,?,?,?,?",
                "Data",
                12,
                new String[]{
                    TNoRw.getText(),
                    Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " +
                    Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    KdDokter.getText(),
                    Subjektif.getText(),
                    Objektif.getText(),
                    Assesment.getText(),
                    GoalTreatment.getText(),
                    TindakanProgramRehabMedik.getText(),
                    Edukasi.getText(),
                    FrekuensiKunjungan.getText(),
                    RTL.getText(),
                    Valid.SetTgl(tgl_bertemu_dokter.getSelectedItem() + "")
                }
            );

            // 🔹 Notifikasi hasil penyimpanan
            if (tersimpan) {
                JOptionPane.showMessageDialog(null, "Data berhasil disimpan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                tampil();
                emptTeks();
                TabRawat.setSelectedIndex(1);
                BtnUploadActionPerformed(null);
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menyimpan data. Periksa kembali isian Anda.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Penyimpanan dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Edukasi,BtnBatal);
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
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString())){
                    hapus();
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                }
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
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdDokter.getText().trim().equals("")||NmDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        } else if (Objektif.getText().trim().equals("")) {
            Valid.textKosong(Objektif, "Objektif");
        } else if (Assesment.getText().trim().equals("")) {
            Valid.textKosong(Assesment, "Assesment");
        } else if (GoalTreatment.getText().trim().equals("")) {
            Valid.textKosong(GoalTreatment, "Goal Treatment");
        } else if (TindakanProgramRehabMedik.getText().trim().equals("")) {
            Valid.textKosong(TindakanProgramRehabMedik, "Tindakan Program Rehab Medik");
        } else if (Edukasi.getText().trim().equals("")) {
            Valid.textKosong(Edukasi, "Edukasi");
        } else if (FrekuensiKunjungan.getText().trim().equals("")) {
            Valid.textKosong(FrekuensiKunjungan, "Frekuensi Kunjungan");
        }else if(RTL.getText().trim().equals("")){
            Valid.textKosong(RTL,"RTL");
        }else{     
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString())){
                        ganti();
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                    }
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
                if(TCari.getText().trim().equals("")){
                    ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,penilaian_pre_operasi.tanggal,"+
                        "penilaian_pre_operasi.kd_dokter,penilaian_pre_operasi.ringkasan_klinik,penilaian_pre_operasi.pemeriksaan_fisik,penilaian_pre_operasi.pemeriksaan_diagnostik,"+
                        "penilaian_pre_operasi.diagnosa_pre_operasi,penilaian_pre_operasi.rencana_tindakan_bedah,penilaian_pre_operasi.hal_hal_yang_perludi_persiapkan,"+
                        "penilaian_pre_operasi.terapi_pre_operasi,dokter.nm_dokter from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_pre_operasi on reg_periksa.no_rawat=penilaian_pre_operasi.no_rawat "+
                        "inner join dokter on penilaian_pre_operasi.kd_dokter=dokter.kd_dokter where "+
                        "penilaian_pre_operasi.tanggal between ? and ? order by penilaian_pre_operasi.tanggal");
                }else{
                    ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,penilaian_pre_operasi.tanggal,"+
                        "penilaian_pre_operasi.kd_dokter,penilaian_pre_operasi.ringkasan_klinik,penilaian_pre_operasi.pemeriksaan_fisik,penilaian_pre_operasi.pemeriksaan_diagnostik,"+
                        "penilaian_pre_operasi.diagnosa_pre_operasi,penilaian_pre_operasi.rencana_tindakan_bedah,penilaian_pre_operasi.hal_hal_yang_perludi_persiapkan,"+
                        "penilaian_pre_operasi.terapi_pre_operasi,dokter.nm_dokter from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_pre_operasi on reg_periksa.no_rawat=penilaian_pre_operasi.no_rawat "+
                        "inner join dokter on penilaian_pre_operasi.kd_dokter=dokter.kd_dokter where "+
                        "penilaian_pre_operasi.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                        "penilaian_pre_operasi.kd_dokter like ? or dokter.nm_dokter like ?) order by penilaian_pre_operasi.tanggal");
                }

                try {
                    if(TCari.getText().trim().equals("")){
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    }else{
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(3,"%"+TCari.getText()+"%");
                        ps.setString(4,"%"+TCari.getText()+"%");
                        ps.setString(5,"%"+TCari.getText()+"%");
                        ps.setString(6,"%"+TCari.getText()+"%");
                        ps.setString(7,"%"+TCari.getText()+"%");
                    }  
                    rs=ps.executeQuery();
                    htmlContent = new StringBuilder();
                    htmlContent.append(                             
                        "<tr class='isi'>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='105px'><b>No.Rawat</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='70px'><b>No.RM</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='150px'><b>Nama Pasien</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='65px'><b>Tgl.Lahir</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='55px'><b>J.K.</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='80px'><b>NIP</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='150px'><b>Nama Dokter</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='115px'><b>Tanggal</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Ringkasan Klinik</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Pemeriksaan Fisik</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Pemeriksaan Diagnostik</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Diagnosa Pre Operasi</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Rencana Tindakan Bedah</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Hal-hal Yang Perlu Dipersiapkan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center' width='300px'><b>Terapi Pre Operasi</b></td>"+
                        "</tr>"
                    );
                    while(rs.next()){
                        htmlContent.append(
                            "<tr class='isi'>"+
                               "<td valign='top'>"+rs.getString("no_rawat")+"</td>"+
                               "<td valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                               "<td valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                               "<td valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                               "<td valign='top'>"+rs.getString("jk")+"</td>"+
                               "<td valign='top'>"+rs.getString("kd_dokter")+"</td>"+
                               "<td valign='top'>"+rs.getString("nm_dokter")+"</td>"+
                               "<td valign='top'>"+rs.getString("tanggal")+"</td>"+
                               "<td valign='top'>"+rs.getString("ringkasan_klinik")+"</td>"+
                               "<td valign='top'>"+rs.getString("pemeriksaan_fisik")+"</td>"+
                               "<td valign='top'>"+rs.getString("pemeriksaan_diagnostik")+"</td>"+
                               "<td valign='top'>"+rs.getString("diagnosa_pre_operasi")+"</td>"+
                               "<td valign='top'>"+rs.getString("rencana_tindakan_bedah")+"</td>"+
                               "<td valign='top'>"+rs.getString("hal_hal_yang_perludi_persiapkan")+"</td>"+
                               "<td valign='top'>"+rs.getString("terapi_pre_operasi")+"</td>"+
                            "</tr>");
                    }
                    LoadHTML.setText(
                        "<html>"+
                          "<table width='2890px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
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

                    File f = new File("DataPenilaianAwalMedisRalan.html");            
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                    bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                                "<table width='2890px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr class='isi2'>"+
                                        "<td valign='top' align='center'>"+
                                            "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                            akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                            akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                            "<font size='2' face='Tahoma'>DATA PENILAIAN PRE OPERASI<br><br></font>"+        
                                        "</td>"+
                                   "</tr>"+
                                "</table>")
                    );
                    bw.close();                         
                    Desktop.getDesktop().browse(f.toURI());
                } catch (Exception e) {
                    System.out.println("Notif btnPrint : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }

            }catch(Exception e){
                System.out.println("Notifikasi btnPrint : "+e);
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
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbObat.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
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

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void AssesmentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AssesmentKeyPressed
        Valid.pindah2(evt,Objektif,GoalTreatment);
    }//GEN-LAST:event_AssesmentKeyPressed

    private void GoalTreatmentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GoalTreatmentKeyPressed
        Valid.pindah2(evt,Assesment,TindakanProgramRehabMedik);
    }//GEN-LAST:event_GoalTreatmentKeyPressed

    private void TindakanProgramRehabMedikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TindakanProgramRehabMedikKeyPressed
        Valid.pindah2(evt,GoalTreatment,Edukasi);
    }//GEN-LAST:event_TindakanProgramRehabMedikKeyPressed

    private void EdukasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EdukasiKeyPressed
        Valid.pindah2(evt,TindakanProgramRehabMedik,Edukasi);
    }//GEN-LAST:event_EdukasiKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt,TCari,Jam);
    }//GEN-LAST:event_TanggalKeyPressed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt,Tanggal,Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt,Jam,Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt,Menit,BtnDokter);
    }//GEN-LAST:event_DetikKeyPressed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            NmDokter.setText(dokter.tampil3(KdDokter.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Detik.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
//            btnPetugas1ActionPerformed(null);
            BtnDokterActionPerformed(null);
        }
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,Detik,Subjektif);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void SubjektifKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SubjektifKeyPressed
        Valid.pindah2(evt,Tanggal,Objektif);
    }//GEN-LAST:event_SubjektifKeyPressed

    private void ObjektifKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ObjektifKeyPressed
        Valid.pindah2(evt,Subjektif,Assesment);
    }//GEN-LAST:event_ObjektifKeyPressed

    private void MnFormulirRawatJalanKFRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnFormulirRawatJalanKFRActionPerformed
        if(tbObat.getSelectedRow()>-1){           
                    
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
            
            param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",tbObat.getValueAt(tbObat.getSelectedRow(),15).toString()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+ tbObat.getValueAt(tbObat.getSelectedRow(),15).toString() +".png");
            
            param.put("alamat_lengkap",Sequel.cariIsi("SELECT CONCAT(pasien.alamat, ',  ', kecamatan.nm_kec) AS alamat_lengkap FROM `pasien` inner join kecamatan on pasien.kd_kec = kecamatan.kd_kec where no_rkm_medis = ?",TNoRM.getText())); 
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),15).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),16).toString():finger)+"\n"+Tanggal.getSelectedItem()); 
            Valid.MyReportqry("rptFormulirRalanKFRProtokolTerapi.jasper","report","::[ Formulir Rawat Jalan KFR/Asesmen/Re-Asesmen/Protokol Terapi ]::",
                    "SELECT " +
                    "	reg_periksa.no_rawat, " +
                    "	pasien.no_rkm_medis, " +
                    "	pasien.nm_pasien, " +
                    "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir, " +
                    "	reg_periksa.umurdaftar, " +
                    "	reg_periksa.sttsumur, " +
                    "	pasien.jk, " +
                    "	date_format( formulir_kfr_protokol_terapi.tanggal, '%d-%m-%Y' ) AS periksa, " +
                    "	formulir_kfr_protokol_terapi.subjektif, " +
                    "	formulir_kfr_protokol_terapi.objektif, " +
                    "	formulir_kfr_protokol_terapi.asesmen, " +
                    "	formulir_kfr_protokol_terapi.goal_treatment, " +
                    "	formulir_kfr_protokol_terapi.tindakan_program_rehab, " +
                    "	formulir_kfr_protokol_terapi.edukasi, " +
                    "	formulir_kfr_protokol_terapi.frekuensi_kunjungan, " +
                    "	formulir_kfr_protokol_terapi.rtl, " +
                    "	formulir_kfr_protokol_terapi.kd_dokter, " +
                    "	dokter.nm_dokter, " +
                    "	dokter.no_ijn_praktek " +
                    "FROM " +
                    "	formulir_kfr_protokol_terapi " +
                    "	INNER JOIN reg_periksa ON formulir_kfr_protokol_terapi.no_rawat = reg_periksa.no_rawat  " +
                    "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "	INNER JOIN dokter ON formulir_kfr_protokol_terapi.kd_dokter = dokter.kd_dokter "+
                    "WHERE " +
                    "   reg_periksa.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "' " +
                    "   AND formulir_kfr_protokol_terapi.tanggal='" + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString() + "'", 
                    param);
        }
    }//GEN-LAST:event_MnFormulirRawatJalanKFRActionPerformed

    private void FrekuensiKunjunganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FrekuensiKunjunganKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_FrekuensiKunjunganKeyPressed

    private void RTLKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RTLKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RTLKeyPressed

    private void AlamatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AlamatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AlamatActionPerformed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());

        FileName = timestamp + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().replace("/", "") + "_FormulirKFR";

        CreatePDF(FileName);
        ConvertPDFtoJPG(FileName);
        UploadJPG(FileName, "berkasrawat/pages/upload/");
        HapusJPG();

//        ppBerkasDigitalBtnPrintActionPerformed(evt);

    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    private void tgl_bertemu_dokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tgl_bertemu_dokterKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tgl_bertemu_dokterKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            FormulirKFRProtokolTerapi dialog = new FormulirKFRProtokolTerapi(new javax.swing.JFrame(), true);
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
    private widget.TextBox Alamat;
    private widget.TextArea Assesment;
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
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.TextArea Edukasi;
    private widget.PanelBiasa FormInput;
    private widget.TextArea FrekuensiKunjungan;
    private widget.TextArea GoalTreatment;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnFormulirRawatJalanKFR;
    private widget.TextBox NmDokter;
    private widget.TextArea Objektif;
    private widget.TextArea RTL;
    private widget.ScrollPane Scroll;
    private widget.TextArea Subjektif;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TglLahir;
    private widget.TextArea TindakanProgramRehabMedik;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel109;
    private widget.Label jLabel110;
    private widget.Label jLabel111;
    private widget.Label jLabel112;
    private widget.Label jLabel113;
    private widget.Label jLabel114;
    private widget.Label jLabel115;
    private widget.Label jLabel116;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane14;
    private widget.ScrollPane scrollPane15;
    private widget.ScrollPane scrollPane16;
    private widget.ScrollPane scrollPane17;
    private widget.ScrollPane scrollPane18;
    private widget.ScrollPane scrollPane19;
    private widget.ScrollPane scrollPane20;
    private widget.ScrollPane scrollPane21;
    private widget.Table tbObat;
    private widget.Tanggal tgl_bertemu_dokter;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().toString().trim().equals("")){
                ps=koneksi.prepareStatement(
                    "SELECT " +
                    "	reg_periksa.no_rawat, " +
                    "	pasien.no_rkm_medis, " +
                    "	pasien.nm_pasien, " +
                    "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir, " +
                    "	reg_periksa.umurdaftar, " +
                    "	reg_periksa.sttsumur, " +
                    "	pasien.jk, " +
                    "	formulir_kfr_protokol_terapi.tanggal, " +
                    "	formulir_kfr_protokol_terapi.subjektif, " +
                    "	formulir_kfr_protokol_terapi.objektif, " +
                    "	formulir_kfr_protokol_terapi.asesmen, " +
                    "	formulir_kfr_protokol_terapi.goal_treatment, " +
                    "	formulir_kfr_protokol_terapi.tindakan_program_rehab, " +
                    "	formulir_kfr_protokol_terapi.edukasi, " +
                    "	formulir_kfr_protokol_terapi.frekuensi_kunjungan, " +
                    "	formulir_kfr_protokol_terapi.rtl, " +
                    "	formulir_kfr_protokol_terapi.kd_dokter, " +
                    "	dokter.nm_dokter " +
                    "FROM " +
                    "	formulir_kfr_protokol_terapi " +
                    "	INNER JOIN reg_periksa ON formulir_kfr_protokol_terapi.no_rawat = reg_periksa.no_rawat  " +
                    "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "	INNER JOIN dokter ON formulir_kfr_protokol_terapi.kd_dokter = dokter.kd_dokter " +
                    "WHERE " +
                    "   formulir_kfr_protokol_terapi.tanggal between ? and ? " +
                    "ORDER BY " +
                    "   formulir_kfr_protokol_terapi.tanggal desc ");
            }else{
                ps=koneksi.prepareStatement(
                    "SELECT " +
                    "	reg_periksa.no_rawat, " +
                    "	pasien.no_rkm_medis, " +
                    "	pasien.nm_pasien, " +
                    "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir, " +
                    "	reg_periksa.umurdaftar, " +
                    "	reg_periksa.sttsumur, " +
                    "	pasien.jk, " +
                    "	formulir_kfr_protokol_terapi.tanggal, " +
                    "	formulir_kfr_protokol_terapi.subjektif, " +
                    "	formulir_kfr_protokol_terapi.objektif, " +
                    "	formulir_kfr_protokol_terapi.asesmen, " +
                    "	formulir_kfr_protokol_terapi.goal_treatment, " +
                    "	formulir_kfr_protokol_terapi.tindakan_program_rehab, " +
                    "	formulir_kfr_protokol_terapi.edukasi, " +
                    "	formulir_kfr_protokol_terapi.frekuensi_kunjungan, " +
                    "	formulir_kfr_protokol_terapi.rtl, " +
                    "	formulir_kfr_protokol_terapi.kd_dokter, " +
                    "	dokter.nm_dokter " +
                    "FROM " +
                    "	formulir_kfr_protokol_terapi " +
                    "	INNER JOIN reg_periksa ON formulir_kfr_protokol_terapi.no_rawat = reg_periksa.no_rawat  " +
                    "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "	INNER JOIN dokter ON formulir_kfr_protokol_terapi.kd_dokter = dokter.kd_dokter " +
                    "WHERE " +
                    "   formulir_kfr_protokol_terapi.tanggal between ? and ? "+
                    "   and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or formulir_kfr_protokol_terapi.kd_dokter like ? or dokter.nm_dokter like ?) "+
                    "ORDER BY " +
                    "   formulir_kfr_protokol_terapi.tanggal desc ");
            }
                
            try {
                if(TCari.getText().toString().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                }
                    
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("lahir"),
                        rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),
                        rs.getString("jk"),
                        rs.getString("tanggal"),
                        rs.getString("subjektif"),
                        rs.getString("objektif"),
                        rs.getString("asesmen"),
                        rs.getString("goal_treatment"),
                        rs.getString("tindakan_program_rehab"),
                        rs.getString("edukasi"),
                        rs.getString("frekuensi_kunjungan"),
                        rs.getString("rtl"),
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter")
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
        }catch(SQLException e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        Alamat.setText("");
        Subjektif.setText("");
        Objektif.setText("");
        Assesment.setText("");
        GoalTreatment.setText("");
        TindakanProgramRehabMedik.setText("");
        Edukasi.setText("");
        FrekuensiKunjungan.setText("");
        RTL.setText("");
        Subjektif.requestFocus();
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            // "No.Rawat","No.R.M.","Nama Pasien","Umur","JK","Tgl.Lahir","Tanggal Uji","Diagnosis Fugsional","Diagnosis Medis","Hasil Yang Didapat","Kesimpulan","Rekomendasi","Kode Dokter","Nama Dokter"
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            Subjektif.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            Objektif.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            Assesment.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            GoalTreatment.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            TindakanProgramRehabMedik.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            Edukasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            FrekuensiKunjungan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            RTL.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Valid.SetTgl(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(11,13));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(14,15));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(17,19));
            
            // 🔹 Tambahkan bagian ini untuk ambil alamat lengkap dari database
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT CONCAT(p.alamat, ', ', k.nm_kec) AS alamat_lengkap " +
                    "FROM pasien p INNER JOIN kecamatan k ON p.kd_kec = k.kd_kec " +
                    "WHERE p.no_rkm_medis = ?"
                );
                ps.setString(1, TNoRM.getText());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Alamat.setText(rs.getString("alamat_lengkap"));
                } else {
                    Alamat.setText("-");
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                System.out.println("Error ambil alamat lengkap: " + e.getMessage());
            }
        }
    }

    public void isRawat() {   
    // isi dulu no RM dari no_rawat (biarkan seperti semula)
    Sequel.cariIsi(
        "select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='" + TNoRw.getText() + "' ",
        TNoRM
    );

    try {
        ps = koneksi.prepareStatement(
            "select reg_periksa.no_rkm_medis,pasien.nm_pasien, " +
            "if(pasien.jk='L','Laki-Laki','Perempuan') as jk," +
            "pasien.tgl_lahir,reg_periksa.tgl_registrasi " +
            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
            "where reg_periksa.no_rawat=?"
        );
        try {
            ps.setString(1, TNoRw.getText());
            rs = ps.executeQuery();
            if (rs.next()) {
                // ====== DATA PASIEN ======
                TNoRM.setText(rs.getString("no_rkm_medis"));
                DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                TPasien.setText(rs.getString("nm_pasien"));
                JK.setText(rs.getString("jk"));
                TglLahir.setText(rs.getString("tgl_lahir"));

                // ====== SOAP / PEMERIKSAAN ======
                Subjektif.setText(Sequel.cariIsi(
                    "select keluhan from pemeriksaan_ralan where no_rawat = ? ",
                    TNoRw.getText()
                ));
                Objektif.setText(Sequel.cariIsi(
                    "select pemeriksaan from pemeriksaan_ralan where no_rawat = ? ",
                    TNoRw.getText()
                ));
                Assesment.setText(Sequel.cariIsi(
                    "select penilaian from pemeriksaan_ralan where no_rawat = ? ",
                    TNoRw.getText()
                ));
                RTL.setText(Sequel.cariIsi(
                    "select evaluasi from pemeriksaan_ralan where no_rawat = ? ",
                    TNoRw.getText()
                ));
                TindakanProgramRehabMedik.setText(Sequel.cariIsi(
                    "select rtl from pemeriksaan_ralan where no_rawat = ? ",
                    TNoRw.getText()
                ));

                // ====== SET DOKTER BERDASARKAN USER LOGIN ======
                // kode user yang login
                String kdLogin = akses.getkode();

                if (kdLogin != null && !kdLogin.trim().equals("")) {
                    // cek apakah kdLogin ada di tabel dokter
                    String namaDokterLogin = Sequel.cariIsi(
                        "select nm_dokter from dokter where kd_dokter=?",
                        kdLogin
                    );

                    if (!namaDokterLogin.equals("")) {
                        // kalau memang dokter, isi ke form
                        KdDokter.setText(kdLogin);
                        NmDokter.setText(namaDokterLogin);

                        // kalau mau dikunci dari edit manual, boleh aktifkan ini:
                        // KdDokter.setEditable(false);
                        // NmDokter.setEditable(false);
                    } else {
                        // kalau yang login bukan dokter, kosongkan saja
                        KdDokter.setText("");
                        NmDokter.setText("");
                    }
                } else {
                    // tidak ada kode login, aman dikosongkan
                    KdDokter.setText("");
                    NmDokter.setText("");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat : " + e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    } catch (Exception e) {
        System.out.println("Notif isRawat : " + e);
    }
    }
    
//    private void isPsien() {
//        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
//        Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",Jk);
//        Sequel.cariIsi("select date_format(pasien.tgl_lahir,'%d-%m-%Y') from pasien where pasien.no_rkm_medis=? ",TglLahir,TNoRM.getText());
//        Sequel.cariIsi("SELECT CONCAT(pasien.alamat, ',  ', kecamatan.nm_kec) AS alamat_lengkap FROM `pasien` inner join kecamatan on pasien.kd_kec = kecamatan.kd_kec where no_rkm_medis = ? ",Alamat,TNoRM.getText());
//    }
 
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        Sequel.cariIsi("SELECT CONCAT(pasien.alamat, ',  ', kelurahan.nm_kel, ', ', kecamatan.nm_kec) AS alamat_lengkap FROM `pasien` inner join kecamatan on pasien.kd_kec = kecamatan.kd_kec INNER JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel where no_rkm_medis = ? ",
                Alamat,
                TNoRM.getText());       
                
        try (PreparedStatement ps = koneksi.prepareStatement(
        "SELECT COUNT(*) FROM formulir_kfr_protokol_terapi WHERE no_rawat = ?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    TabRawat.setSelectedIndex(1); 
                    isRawat();                   
                    tampil(); 
                } else {
                    TabRawat.setSelectedIndex(0); 
                    isRawat();                                   }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void setNoRm(String norwt,Date tgl2,String KodeDokter,String NamaDokter,String Operasi) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        Sequel.cariIsi("select reg_periksa.tgl_registrasi from reg_periksa where reg_periksa.no_rawat='"+norwt+"'", DTPCari1);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        KdDokter.setText(KodeDokter);
        NmDokter.setText(NamaDokter);
        TindakanProgramRehabMedik.setText(Operasi);
        Sequel.cariIsi("SELECT CONCAT(pasien.alamat, ',  ', kelurahan.nm_kel, ', ', kecamatan.nm_kec) AS alamat_lengkap FROM `pasien` inner join kecamatan on pasien.kd_kec = kecamatan.kd_kec INNER JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel where no_rkm_medis = ? ",
                Alamat,
                TNoRM.getText());    
        
        
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getuji_fungsi_kfr());
        BtnHapus.setEnabled(akses.getuji_fungsi_kfr());
        BtnEdit.setEnabled(akses.getuji_fungsi_kfr());
        BtnEdit.setEnabled(akses.getuji_fungsi_kfr());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            NmDokter.setText(dokter.tampil3(KdDokter.getText()));
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }            
    }
    
    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkKejadian.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkKejadian.isSelected()==false){
                    nilai_jam =Jam.getSelectedIndex();
                    nilai_menit =Menit.getSelectedIndex();
                    nilai_detik =Detik.getSelectedIndex();
                }

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
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }
    
    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
    // Pastikan baris dipilih dulu
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data yang akan dihapus terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔹 Konfirmasi pertama
        int konfirmasiHapus = JOptionPane.showConfirmDialog(
            null,
            "Apakah data yang akan dihapus sudah benar?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (konfirmasiHapus == JOptionPane.YES_OPTION) {
            // 🔹 Konfirmasi kedua (validasi ulang)
            int konfirmasiUlang = JOptionPane.showConfirmDialog(
                null,
                "Apakah Anda yakin ingin menghapus data ini?",
                "Validasi Ulang Hapus",
                JOptionPane.YES_NO_OPTION
            );

            if (konfirmasiUlang == JOptionPane.YES_OPTION) {
                // 🔹 Proses hapus
                boolean berhasil = Sequel.queryu2tf(
                    "delete from formulir_kfr_protokol_terapi where no_rawat=? and tanggal=?",
                    2,
                    new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()
                    }
                );

                if (berhasil) {
                    // 🔹 Jika berhasil, hapus dari tabel dan tampil notif
                    tabMode.removeRow(tbObat.getSelectedRow());
                    LCount.setText("" + tabMode.getRowCount());
                    emptTeks();

                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal menghapus data. Periksa kembali.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Penghapusan dibatalkan. Silakan periksa kembali data Anda.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Proses hapus dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void ganti() {
    // 🔹 Konfirmasi pertama: apakah yakin mau edit
        int konfirmasiEdit = JOptionPane.showConfirmDialog(
            null,
            "Apakah data yang akan diedit sudah benar?",
            "Konfirmasi Edit",
            JOptionPane.YES_NO_OPTION
        );
        if (konfirmasiEdit == JOptionPane.YES_OPTION) {
            // 🔹 Konfirmasi kedua: validasi ulang
            int konfirmasiUlang = JOptionPane.showConfirmDialog(
                null,
                "Apakah Anda yakin ingin mengedit data ini?",
                "Validasi Ulang Edit",
                JOptionPane.YES_NO_OPTION
            );
            if (konfirmasiUlang == JOptionPane.YES_OPTION) {
                // 🔹 Jalankan proses edit
                Sequel.mengedit(
                    "formulir_kfr_protokol_terapi",
                    "no_rawat=? AND tanggal=?",
                              "no_rawat=?,"
                            + "tanggal=?,"
                            + "kd_dokter=?,"
                            + "subjektif=?,"
                            + "objektif=?,"
                            + "asesmen=?,"
                            + "goal_treatment=?,"
                            + "tindakan_program_rehab=?,"
                            + "edukasi=?,"
                            + "frekuensi_kunjungan=?,"
                            + "rtl=?",
                    13,
                    new String[]{
                        TNoRw.getText(),
                        Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " +
                        Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDokter.getText(),
                        Subjektif.getText(),
                        Objektif.getText(),
                        Assesment.getText(),
                        GoalTreatment.getText(),
                        TindakanProgramRehabMedik.getText(),
                        Edukasi.getText(),
                        FrekuensiKunjungan.getText(),
                        RTL.getText(),
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()
                    }
                );
                // 🔹 Tampilkan notifikasi berhasil
                JOptionPane.showMessageDialog(null, "Data berhasil diperbarui.", "Informasi", JOptionPane.INFORMATION_MESSAGE);

                if (tabMode.getRowCount() != 0) {
                    tampil();
                }
                emptTeks();
            } else {
                JOptionPane.showMessageDialog(null, "Edit dibatalkan. Silakan periksa kembali data Anda.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Proses edit dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
private void CreatePDF(String FileName) {
        if(tbObat.getSelectedRow()>-1){           
                    
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
            
            param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",tbObat.getValueAt(tbObat.getSelectedRow(),15).toString()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+ tbObat.getValueAt(tbObat.getSelectedRow(),15).toString() +".png");
            
            param.put("alamat_lengkap",Sequel.cariIsi("SELECT CONCAT(pasien.alamat, ',  ', kecamatan.nm_kec) AS alamat_lengkap FROM `pasien` inner join kecamatan on pasien.kd_kec = kecamatan.kd_kec where no_rkm_medis = ?",TNoRM.getText())); 
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),15).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),16).toString():finger)+"\n"+Tanggal.getSelectedItem()); 
            Valid.MyReportPDFqryUpload("rptFormulirRalanKFRProtokolTerapi.jasper","report","::[ Formulir Rawat Jalan KFR/Asesmen/Re-Asesmen/Protokol Terapi ]::",
                    "SELECT " +
                    "	reg_periksa.no_rawat, " +
                    "	pasien.no_rkm_medis, " +
                    "	pasien.nm_pasien, " +
                    "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir, " +
                    "	reg_periksa.umurdaftar, " +
                    "	reg_periksa.sttsumur, " +
                    "	pasien.jk, " +
                    "	date_format( formulir_kfr_protokol_terapi.tanggal, '%d-%m-%Y' ) AS periksa, " +
                    "	formulir_kfr_protokol_terapi.subjektif, " +
                    "	formulir_kfr_protokol_terapi.objektif, " +
                    "	formulir_kfr_protokol_terapi.asesmen, " +
                    "	formulir_kfr_protokol_terapi.goal_treatment, " +
                    "	formulir_kfr_protokol_terapi.tindakan_program_rehab, " +
                    "	formulir_kfr_protokol_terapi.edukasi, " +
                    "	formulir_kfr_protokol_terapi.frekuensi_kunjungan, " +
                    "	formulir_kfr_protokol_terapi.rtl, " +
                    "	formulir_kfr_protokol_terapi.kd_dokter, " +
                    "	dokter.nm_dokter, " +
                    "	dokter.no_ijn_praktek " +
                    "FROM " +
                    "	formulir_kfr_protokol_terapi " +
                    "	INNER JOIN reg_periksa ON formulir_kfr_protokol_terapi.no_rawat = reg_periksa.no_rawat  " +
                    "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "	INNER JOIN dokter ON formulir_kfr_protokol_terapi.kd_dokter = dokter.kd_dokter "+
                    "WHERE " +
                    "   reg_periksa.no_rawat='" + TNoRw.getText() + "' ",FileName, param); 
        }
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
        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Berkas Lain-lain%'");
        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".pdf'") > 0) {
            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf", "pages/upload/" + FileName + ".pdf"
            });
        } else {
            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf"
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Form Rehabilitasi%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    
                    MetadataBerkas.simpan(
                        koneksi,
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(),
                        kodeberkas,
                        filePath,
                        jpgFile.getName(),
                        akses.getkode()
                    );
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
