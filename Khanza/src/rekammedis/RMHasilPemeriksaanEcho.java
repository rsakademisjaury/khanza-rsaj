package rekammedis;

import bridging.ApiOrthanc;
import bridging.OrthancDICOM;
import com.fasterxml.jackson.databind.JsonNode;
import fungsi.MetadataBerkas;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import static fungsi.formatColumn.tanggal;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import laporan.DlgBerkasRawat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileName;
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
public final class RMHasilPemeriksaanEcho extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeDicom;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private StringBuilder htmlContent;
    private String finger="",kamar,namakamar;
    private JsonNode root;
    private String TANGGALMUNDUR="yes",FileName = "", kodeberkas = "";
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMHasilPemeriksaanEcho(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabMode = new DefaultTableModel(null, new Object[]{
            "No. Rawat", "No. RM", "Nama Pasien", "Tgl. Lahir", "Kode Dokter", "Nama Dokter", "Tanggal Pemeriksaan",
            "Aorta Root Diameter", "Valve Cups Motion", "Left Atrium Dimension", "Atrium LA/Ao Ratio", "Right Ventricle Dimension",
            "Left Ventricle ED", "Left Ventricle ES", "Left Ventricle EF", "Fract Shortening FS",
            "LVPW ED", "LVPW Thickening", "LVPW Motion Pattern",
            "IVS ED", "IVS Thickening", "IVS Motion Pattern", "IVS Ratio",
            "Anterior Leaflet", "Diastolic Motion Teks", "Diastolic Motion Combo", "Systolic Motion",
            "Posterior Leaflet Combo", "Posterior Leaflet Teks",
            "Tricuspid Valve", "Pulmonary Valve", "Pericardial Effusion Combo", "Pericardial Effusion Teks",
            "Valves AO", "Valves Mitral", "Chambers LA", "Chambers RV", "Chambers RA", "Chambers LV",
            "Myocardium", "PSLAX Segmental", "PSLAX Diffuse", "PSLAX Mass", "PSLAX Pericardium",
            "PSSAX Aortic Valve", "Mitral Valve", "Left Ventricle", "APAC Wall Motion", "APAC Segmental",
            "APAC Diffuse", "APAC Mitral Valve", "APAC Tricuspid Valve", "Shunts", "Valve Insufficiency", "Comments"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < tbObat.getColumnCount(); i++) {
        TableColumn column = tbObat.getColumnModel().getColumn(i);
        switch (i) {
            case 0: // No. Rawat
                column.setPreferredWidth(105);
                break;
            case 1: // No. RM
                column.setPreferredWidth(70);
                break;
            case 2: // Nama Pasien
                column.setPreferredWidth(150);
                break;
            case 3: // Tgl. Lahir
                column.setPreferredWidth(80);
                break;
            case 4: // Kode Dokter
                column.setPreferredWidth(100);
                break;
            case 5: // Nama Dokter
                column.setPreferredWidth(130);
                break;
            case 6: // Tanggal Pemeriksaan
                column.setPreferredWidth(120);
                break;
            case 56: // Comments
                column.setPreferredWidth(250);
                break;
            default: // Kolom hasil pemeriksaan lainnya
                column.setPreferredWidth(140);
                break;
        }
    }
        
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDicom=new DefaultTableModel(null,new Object[]{
            "UUID Pasien","ID Studies","ID Series"}){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbListDicom.setModel(tabModeDicom);
        tbListDicom.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbListDicom.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbListDicom.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(270);
            }else if(i==2){
                column.setPreferredWidth(270);
            }
        }
        tbListDicom.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        AortaRootDiameter.setDocument(new batasInput((int)30).getKata(AortaRootDiameter));
        ValveCupsMotion.setDocument(new batasInput((int)30).getKata(ValveCupsMotion));
        LeftAtriumDimension.setDocument(new batasInput((int)30).getKata(LeftAtriumDimension));
        AtriumLAAoRatio.setDocument(new batasInput((int)50).getKata(AtriumLAAoRatio));
        RightVentricleDimension.setDocument(new batasInput((int)50).getKata(RightVentricleDimension));
        LeftVentricleED.setDocument(new batasInput((int)100).getKata(LeftVentricleED));
        LeftVentricleES.setDocument(new batasInput((int)15).getKata(LeftVentricleES));
        LeftVentricleEF.setDocument(new batasInput((int)100).getKata(LeftVentricleEF));
//        Kesimpulan.setDocument(new batasInput((int)200).getKata(Kesimpulan));
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
        
        ChkAccor.setSelected(false);
        isPhoto();
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        LoadHTML2.setEditable(false);
        LoadHTML2.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(e.getURL().toURI());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        });
        LoadHTML2.setEditorKit(kit);
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
        LoadHTML2.setDocument(doc);
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
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

        LoadHTML = new widget.editorpane();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnHasilPemeriksaanEcho = new javax.swing.JMenuItem();
        TanggalRegistrasi = new widget.TextBox();
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
        label14 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel10 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        label11 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabel31 = new widget.Label();
        AortaRootDiameter = new widget.TextBox();
        RightVentricleDimension = new widget.TextBox();
        jLabel35 = new widget.Label();
        jLabel40 = new widget.Label();
        ValveCupsMotion = new widget.TextBox();
        jLabel41 = new widget.Label();
        LeftVentricleED = new widget.TextBox();
        AtriumLAAoRatio = new widget.TextBox();
        jLabel43 = new widget.Label();
        jLabel45 = new widget.Label();
        LeftVentricleES = new widget.TextBox();
        jLabel42 = new widget.Label();
        LeftAtriumDimension = new widget.TextBox();
        LeftVentricleEF = new widget.TextBox();
        jLabel32 = new widget.Label();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel47 = new widget.Label();
        jLabel48 = new widget.Label();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel49 = new widget.Label();
        jLabel50 = new widget.Label();
        jLabel51 = new widget.Label();
        jLabel52 = new widget.Label();
        jLabel53 = new widget.Label();
        jSeparator5 = new javax.swing.JSeparator();
        LVPWthickeningED = new widget.TextBox();
        jLabel59 = new widget.Label();
        FractShorteningFS = new widget.TextBox();
        jLabel60 = new widget.Label();
        LVPWThickening = new widget.TextBox();
        jLabel61 = new widget.Label();
        LVPWmationPattern = new widget.ComboBox();
        jLabel62 = new widget.Label();
        jLabel63 = new widget.Label();
        IVSThickeningIVS = new widget.TextBox();
        jLabel64 = new widget.Label();
        IVSthickening = new widget.TextBox();
        jLabel65 = new widget.Label();
        IVSmationPattern = new widget.ComboBox();
        jLabel66 = new widget.Label();
        IVSRatio = new widget.TextBox();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel67 = new widget.Label();
        jLabel68 = new widget.Label();
        jLabel69 = new widget.Label();
        jLabel70 = new widget.Label();
        AnteriorLeaflet = new widget.TextBox();
        DiastolicMotionTeks = new widget.TextBox();
        DiastolicMotionCMbox = new widget.ComboBox();
        jLabel71 = new widget.Label();
        SystolicMotion = new widget.ComboBox();
        jLabel72 = new widget.Label();
        PosteriorLeafletCMbox = new widget.ComboBox();
        PosteriorLeafletTeks = new widget.TextBox();
        jSeparator7 = new javax.swing.JSeparator();
        TricuspidValve = new widget.TextBox();
        jLabel54 = new widget.Label();
        PulmonaryValve = new widget.TextBox();
        jSeparator8 = new javax.swing.JSeparator();
        PericardialEffusionCMBox = new widget.ComboBox();
        PericardialEffusionTeks = new widget.TextBox();
        jLabel33 = new widget.Label();
        jLabel34 = new widget.Label();
        jLabel36 = new widget.Label();
        ValvesAO = new widget.TextBox();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel37 = new widget.Label();
        ValvesMitral = new widget.TextBox();
        jLabel38 = new widget.Label();
        jLabel39 = new widget.Label();
        jLabel55 = new widget.Label();
        ChambersLAC = new widget.TextBox();
        jLabel56 = new widget.Label();
        ChambersRAC = new widget.TextBox();
        jLabel57 = new widget.Label();
        ChambersRVC = new widget.TextBox();
        jLabel58 = new widget.Label();
        ChambersLVC = new widget.TextBox();
        jLabel73 = new widget.Label();
        jLabel74 = new widget.Label();
        jLabel75 = new widget.Label();
        PSLAXSegmental = new widget.TextBox();
        PSLAXDiffuse = new widget.TextBox();
        jLabel76 = new widget.Label();
        PSLAXMass = new widget.TextBox();
        Myocardium = new widget.TextBox();
        jLabel77 = new widget.Label();
        PSLAXPericardium = new widget.TextBox();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel78 = new widget.Label();
        jLabel79 = new widget.Label();
        PSSAXAorticValve = new widget.TextBox();
        jLabel80 = new widget.Label();
        MitralValve = new widget.TextBox();
        jLabel81 = new widget.Label();
        LeftVentricle = new widget.TextBox();
        jLabel82 = new widget.Label();
        jLabel83 = new widget.Label();
        jLabel84 = new widget.Label();
        jLabel85 = new widget.Label();
        APACSegmental = new widget.TextBox();
        APACDiffuce = new widget.TextBox();
        jLabel86 = new widget.Label();
        APACMitraValve = new widget.TextBox();
        APACWallMotion = new widget.TextBox();
        jLabel87 = new widget.Label();
        APACTriscupsidValve = new widget.TextBox();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jLabel88 = new widget.Label();
        Shunts = new widget.TextBox();
        jLabel89 = new widget.Label();
        ValveInsufficiency = new widget.TextBox();
        jLabel90 = new widget.Label();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel91 = new widget.Label();
        jScrollPane1 = new javax.swing.JScrollPane();
        Comments = new widget.TextArea();
        jLabel92 = new widget.Label();
        jLabel93 = new widget.Label();
        jSeparator13 = new javax.swing.JSeparator();
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
        TabData = new javax.swing.JTabbedPane();
        FormPhoto = new widget.PanelBiasa();
        FormPass3 = new widget.PanelBiasa();
        btnAmbil = new widget.Button();
        BtnRefreshPhoto1 = new widget.Button();
        Scroll5 = new widget.ScrollPane();
        LoadHTML2 = new widget.editorpane();
        FormOrthan = new widget.PanelBiasa();
        Scroll6 = new widget.ScrollPane();
        tbListDicom = new widget.Table();
        panelGlass7 = new widget.panelisi();
        btnDicom = new widget.Button();

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnHasilPemeriksaanEcho.setBackground(new java.awt.Color(255, 255, 254));
        MnHasilPemeriksaanEcho.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnHasilPemeriksaanEcho.setForeground(new java.awt.Color(50, 50, 50));
        MnHasilPemeriksaanEcho.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnHasilPemeriksaanEcho.setText("Hasil Pemeriksaan Echocardiogram");
        MnHasilPemeriksaanEcho.setName("MnHasilPemeriksaanEcho"); // NOI18N
        MnHasilPemeriksaanEcho.setPreferredSize(new java.awt.Dimension(320, 26));
        MnHasilPemeriksaanEcho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnHasilPemeriksaanEchoActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnHasilPemeriksaanEcho);

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Hasil Pemeriksaan ECHOCARDIOGRAFI ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1600, 850));
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
        TabRawat.setPreferredSize(new java.awt.Dimension(457, 480));

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setPreferredSize(new java.awt.Dimension(1516, 639));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(1516, 639));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "  Echocardiogram Report  ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1516, 639));
        FormInput.setLayout(null);

        TNoRw.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TNoRw.setText("2020/01/18/000001");
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(80, 30, 120, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(270, 30, 310, 23);

        TNoRM.setEditable(false);
        TNoRM.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TNoRM.setText("269205");
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(205, 30, 60, 23);

        label14.setText("Dokter :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(750, 30, 60, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdDokter);
        KdDokter.setBounds(830, 30, 100, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(935, 30, 295, 23);

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
        BtnDokter.setBounds(1240, 30, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(590, 30, 52, 23);

        TglLahir.setEditable(false);
        TglLahir.setText("2020-01-18");
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(650, 30, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(10, 30, 60, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(100, 70, 650, 1);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(1300, 30, 52, 26);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "23-05-2025 09:00:14" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(1370, 30, 140, 26);

        jLabel31.setText("Root diameter  :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(110, 80, 110, 26);

        AortaRootDiameter.setToolTipText("20 - 30 mm");
        AortaRootDiameter.setFocusTraversalPolicyProvider(true);
        AortaRootDiameter.setName("AortaRootDiameter"); // NOI18N
        AortaRootDiameter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AortaRootDiameterKeyPressed(evt);
            }
        });
        FormInput.add(AortaRootDiameter);
        AortaRootDiameter.setBounds(230, 80, 50, 26);

        RightVentricleDimension.setFocusTraversalPolicyProvider(true);
        RightVentricleDimension.setName("RightVentricleDimension"); // NOI18N
        RightVentricleDimension.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RightVentricleDimensionKeyPressed(evt);
            }
        });
        FormInput.add(RightVentricleDimension);
        RightVentricleDimension.setBounds(230, 163, 50, 26);

        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setText("MITRAL VALVE");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(15, 375, 100, 26);

        jLabel40.setText("Valve Cusp motion  :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(280, 80, 150, 26);

        ValveCupsMotion.setFocusTraversalPolicyProvider(true);
        ValveCupsMotion.setName("ValveCupsMotion"); // NOI18N
        ValveCupsMotion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ValveCupsMotionKeyPressed(evt);
            }
        });
        FormInput.add(ValveCupsMotion);
        ValveCupsMotion.setBounds(440, 80, 290, 26);

        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel41.setText("TRICUSPID VALVE");
        jLabel41.setName("jLabel41"); // NOI18N
        jLabel41.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel41);
        jLabel41.setBounds(15, 513, 135, 26);

        LeftVentricleED.setFocusTraversalPolicyProvider(true);
        LeftVentricleED.setName("LeftVentricleED"); // NOI18N
        LeftVentricleED.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftVentricleEDKeyPressed(evt);
            }
        });
        FormInput.add(LeftVentricleED);
        LeftVentricleED.setBounds(230, 203, 50, 26);

        AtriumLAAoRatio.setFocusTraversalPolicyProvider(true);
        AtriumLAAoRatio.setName("AtriumLAAoRatio"); // NOI18N
        AtriumLAAoRatio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AtriumLAAoRatioKeyPressed(evt);
            }
        });
        FormInput.add(AtriumLAAoRatio);
        AtriumLAAoRatio.setBounds(440, 120, 50, 26);

        jLabel43.setText("Ejection Fraction ( EF )  :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(80, 235, 140, 26);

        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel45.setText("PERICARDIAL EFFUSION");
        jLabel45.setName("jLabel45"); // NOI18N
        jLabel45.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel45);
        jLabel45.setBounds(15, 585, 135, 26);

        LeftVentricleES.setFocusTraversalPolicyProvider(true);
        LeftVentricleES.setName("LeftVentricleES"); // NOI18N
        LeftVentricleES.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftVentricleESKeyPressed(evt);
            }
        });
        FormInput.add(LeftVentricleES);
        LeftVentricleES.setBounds(440, 203, 50, 26);

        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel42.setText("LEFT ATRIUM");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(15, 120, 90, 26);

        LeftAtriumDimension.setFocusTraversalPolicyProvider(true);
        LeftAtriumDimension.setName("LeftAtriumDimension"); // NOI18N
        LeftAtriumDimension.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftAtriumDimensionKeyPressed(evt);
            }
        });
        FormInput.add(LeftAtriumDimension);
        LeftAtriumDimension.setBounds(230, 120, 50, 26);

        LeftVentricleEF.setFocusTraversalPolicyProvider(true);
        LeftVentricleEF.setName("LeftVentricleEF"); // NOI18N
        LeftVentricleEF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftVentricleEFKeyPressed(evt);
            }
        });
        FormInput.add(LeftVentricleEF);
        LeftVentricleEF.setBounds(230, 235, 50, 26);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("AORTA");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(15, 80, 50, 26);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 112, 750, 1);

        jLabel47.setText("Dimensi  :");
        jLabel47.setName("jLabel47"); // NOI18N
        FormInput.add(jLabel47);
        jLabel47.setBounds(110, 120, 110, 26);

        jLabel48.setText("LA / Ao Ratio  :");
        jLabel48.setName("jLabel48"); // NOI18N
        FormInput.add(jLabel48);
        jLabel48.setBounds(300, 120, 130, 26);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 155, 750, 1);

        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel49.setText("RIGHT VENTRICLE");
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(15, 163, 110, 26);

        jLabel50.setText("Dimensi  :");
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(130, 163, 90, 26);

        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel51.setText("LEFT VENTRICLE");
        jLabel51.setName("jLabel51"); // NOI18N
        FormInput.add(jLabel51);
        jLabel51.setBounds(15, 203, 110, 26);

        jLabel52.setText(" Dimensi ( ES )  :");
        jLabel52.setName("jLabel52"); // NOI18N
        FormInput.add(jLabel52);
        jLabel52.setBounds(300, 203, 130, 26);

        jLabel53.setText("Dimensi ( ED )  :");
        jLabel53.setName("jLabel53"); // NOI18N
        FormInput.add(jLabel53);
        jLabel53.setBounds(130, 203, 90, 26);

        jSeparator5.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator5.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(0, 195, 750, 1);

        LVPWthickeningED.setFocusTraversalPolicyProvider(true);
        LVPWthickeningED.setName("LVPWthickeningED"); // NOI18N
        LVPWthickeningED.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVPWthickeningEDKeyPressed(evt);
            }
        });
        FormInput.add(LVPWthickeningED);
        LVPWthickeningED.setBounds(230, 267, 50, 26);

        jLabel59.setText("Fract. Shortening ( FS )  :");
        jLabel59.setName("jLabel59"); // NOI18N
        FormInput.add(jLabel59);
        jLabel59.setBounds(300, 235, 130, 26);

        FractShorteningFS.setFocusTraversalPolicyProvider(true);
        FractShorteningFS.setName("FractShorteningFS"); // NOI18N
        FractShorteningFS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FractShorteningFSKeyPressed(evt);
            }
        });
        FormInput.add(FractShorteningFS);
        FractShorteningFS.setBounds(440, 235, 50, 26);

        jLabel60.setText("Mation pattern  :");
        jLabel60.setName("jLabel60"); // NOI18N
        FormInput.add(jLabel60);
        jLabel60.setBounds(500, 267, 130, 26);

        LVPWThickening.setFocusTraversalPolicyProvider(true);
        LVPWThickening.setName("LVPWThickening"); // NOI18N
        LVPWThickening.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVPWThickeningKeyPressed(evt);
            }
        });
        FormInput.add(LVPWThickening);
        LVPWThickening.setBounds(440, 267, 50, 26);

        jLabel61.setText("% thickening  :");
        jLabel61.setName("jLabel61"); // NOI18N
        FormInput.add(jLabel61);
        jLabel61.setBounds(290, 267, 140, 26);

        LVPWmationPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Normal", "Hyper", "Hypo" }));
        LVPWmationPattern.setName("LVPWmationPattern"); // NOI18N
        FormInput.add(LVPWmationPattern);
        LVPWmationPattern.setBounds(640, 267, 85, 26);

        jLabel62.setText("LVPW thickening ( ED )  :");
        jLabel62.setName("jLabel62"); // NOI18N
        FormInput.add(jLabel62);
        jLabel62.setBounds(70, 267, 150, 26);

        jLabel63.setText("IVS thickening  :");
        jLabel63.setName("jLabel63"); // NOI18N
        FormInput.add(jLabel63);
        jLabel63.setBounds(70, 299, 150, 26);

        IVSThickeningIVS.setFocusTraversalPolicyProvider(true);
        IVSThickeningIVS.setName("IVSThickeningIVS"); // NOI18N
        IVSThickeningIVS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVSThickeningIVSKeyPressed(evt);
            }
        });
        FormInput.add(IVSThickeningIVS);
        IVSThickeningIVS.setBounds(230, 299, 50, 26);

        jLabel64.setText("% thickening  :");
        jLabel64.setName("jLabel64"); // NOI18N
        FormInput.add(jLabel64);
        jLabel64.setBounds(300, 299, 130, 26);

        IVSthickening.setFocusTraversalPolicyProvider(true);
        IVSthickening.setName("IVSthickening"); // NOI18N
        IVSthickening.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVSthickeningKeyPressed(evt);
            }
        });
        FormInput.add(IVSthickening);
        IVSthickening.setBounds(440, 299, 50, 26);

        jLabel65.setText("Mation pattern  :");
        jLabel65.setName("jLabel65"); // NOI18N
        FormInput.add(jLabel65);
        jLabel65.setBounds(500, 299, 130, 26);

        IVSmationPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Normal", "Hyper", "Hypo", "Paradoxial" }));
        IVSmationPattern.setName("IVSmationPattern"); // NOI18N
        FormInput.add(IVSmationPattern);
        IVSmationPattern.setBounds(640, 299, 85, 26);

        jLabel66.setText("IVS / LVPW Ratio  :");
        jLabel66.setName("jLabel66"); // NOI18N
        FormInput.add(jLabel66);
        jLabel66.setBounds(20, 331, 200, 26);

        IVSRatio.setFocusTraversalPolicyProvider(true);
        IVSRatio.setName("IVSRatio"); // NOI18N
        IVSRatio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVSRatioKeyPressed(evt);
            }
        });
        FormInput.add(IVSRatio);
        IVSRatio.setBounds(230, 331, 50, 26);

        jSeparator6.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator6.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(0, 365, 750, 1);

        jLabel67.setText("anterior leaflet  :");
        jLabel67.setName("jLabel67"); // NOI18N
        FormInput.add(jLabel67);
        jLabel67.setBounds(110, 375, 110, 26);

        jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel68.setText("Amplitude ( D - E )");
        jLabel68.setName("jLabel68"); // NOI18N
        FormInput.add(jLabel68);
        jLabel68.setBounds(230, 375, 110, 26);

        jLabel69.setText("diastolic motion  :");
        jLabel69.setName("jLabel69"); // NOI18N
        FormInput.add(jLabel69);
        jLabel69.setBounds(110, 408, 110, 26);

        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel70.setText("Slope ( E - Fo )");
        jLabel70.setName("jLabel70"); // NOI18N
        FormInput.add(jLabel70);
        jLabel70.setBounds(230, 408, 120, 26);

        AnteriorLeaflet.setFocusTraversalPolicyProvider(true);
        AnteriorLeaflet.setName("AnteriorLeaflet"); // NOI18N
        AnteriorLeaflet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnteriorLeafletKeyPressed(evt);
            }
        });
        FormInput.add(AnteriorLeaflet);
        AnteriorLeaflet.setBounds(340, 375, 160, 26);

        DiastolicMotionTeks.setFocusTraversalPolicyProvider(true);
        DiastolicMotionTeks.setName("DiastolicMotionTeks"); // NOI18N
        DiastolicMotionTeks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiastolicMotionTeksKeyPressed(evt);
            }
        });
        FormInput.add(DiastolicMotionTeks);
        DiastolicMotionTeks.setBounds(340, 408, 50, 26);

        DiastolicMotionCMbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Normal", "MS", "A1", "Equivocal" }));
        DiastolicMotionCMbox.setName("DiastolicMotionCMbox"); // NOI18N
        FormInput.add(DiastolicMotionCMbox);
        DiastolicMotionCMbox.setBounds(400, 408, 100, 26);

        jLabel71.setText("systolic motion  :");
        jLabel71.setName("jLabel71"); // NOI18N
        FormInput.add(jLabel71);
        jLabel71.setBounds(110, 440, 110, 26);

        SystolicMotion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Normal", "SAM", "Prolapse", "Equivocal" }));
        SystolicMotion.setName("SystolicMotion"); // NOI18N
        FormInput.add(SystolicMotion);
        SystolicMotion.setBounds(230, 440, 100, 26);

        jLabel72.setText("posterior leaflet  :");
        jLabel72.setName("jLabel72"); // NOI18N
        FormInput.add(jLabel72);
        jLabel72.setBounds(110, 473, 110, 26);

        PosteriorLeafletCMbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Normal", "Dependent", "Motion", "Other" }));
        PosteriorLeafletCMbox.setName("PosteriorLeafletCMbox"); // NOI18N
        FormInput.add(PosteriorLeafletCMbox);
        PosteriorLeafletCMbox.setBounds(230, 473, 100, 26);

        PosteriorLeafletTeks.setFocusTraversalPolicyProvider(true);
        PosteriorLeafletTeks.setName("PosteriorLeafletTeks"); // NOI18N
        PosteriorLeafletTeks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PosteriorLeafletTeksKeyPressed(evt);
            }
        });
        FormInput.add(PosteriorLeafletTeks);
        PosteriorLeafletTeks.setBounds(335, 473, 400, 26);

        jSeparator7.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator7.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator7.setName("jSeparator7"); // NOI18N
        FormInput.add(jSeparator7);
        jSeparator7.setBounds(0, 505, 750, 1);

        TricuspidValve.setFocusTraversalPolicyProvider(true);
        TricuspidValve.setName("TricuspidValve"); // NOI18N
        TricuspidValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TricuspidValveKeyPressed(evt);
            }
        });
        FormInput.add(TricuspidValve);
        TricuspidValve.setBounds(150, 513, 585, 26);

        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel54.setText("PULMONARY VALVE");
        jLabel54.setName("jLabel54"); // NOI18N
        jLabel54.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel54);
        jLabel54.setBounds(15, 545, 130, 26);

        PulmonaryValve.setFocusTraversalPolicyProvider(true);
        PulmonaryValve.setName("PulmonaryValve"); // NOI18N
        PulmonaryValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PulmonaryValveKeyPressed(evt);
            }
        });
        FormInput.add(PulmonaryValve);
        PulmonaryValve.setBounds(150, 545, 585, 26);

        jSeparator8.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator8.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(0, 578, 750, 1);

        PericardialEffusionCMBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Small", "Moderate", "Large" }));
        PericardialEffusionCMBox.setName("PericardialEffusionCMBox"); // NOI18N
        FormInput.add(PericardialEffusionCMBox);
        PericardialEffusionCMBox.setBounds(150, 585, 80, 26);

        PericardialEffusionTeks.setFocusTraversalPolicyProvider(true);
        PericardialEffusionTeks.setName("PericardialEffusionTeks"); // NOI18N
        PericardialEffusionTeks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PericardialEffusionTeksKeyPressed(evt);
            }
        });
        FormInput.add(PericardialEffusionTeks);
        PericardialEffusionTeks.setBounds(235, 585, 500, 26);

        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("PSLAX");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(770, 80, 60, 26);

        jLabel34.setText("Chambers  :");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(800, 111, 100, 26);

        jLabel36.setText("Mitral  :");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(1210, 80, 50, 26);

        ValvesAO.setFocusTraversalPolicyProvider(true);
        ValvesAO.setName("ValvesAO"); // NOI18N
        ValvesAO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ValvesAOKeyPressed(evt);
            }
        });
        FormInput.add(ValvesAO);
        ValvesAO.setBounds(955, 80, 250, 26);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(960, 70, 750, 1);

        jLabel37.setText("AO  :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(910, 80, 40, 26);

        ValvesMitral.setFocusTraversalPolicyProvider(true);
        ValvesMitral.setName("ValvesMitral"); // NOI18N
        ValvesMitral.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ValvesMitralKeyPressed(evt);
            }
        });
        FormInput.add(ValvesMitral);
        ValvesMitral.setBounds(1265, 80, 250, 26);

        jLabel38.setText("Valves  :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(800, 80, 100, 26);

        jLabel39.setText("Myocardium  :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(800, 143, 100, 26);

        jLabel55.setText("LAC  :");
        jLabel55.setName("jLabel55"); // NOI18N
        FormInput.add(jLabel55);
        jLabel55.setBounds(910, 111, 40, 26);

        ChambersLAC.setFocusTraversalPolicyProvider(true);
        ChambersLAC.setName("ChambersLAC"); // NOI18N
        ChambersLAC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ChambersLACKeyPressed(evt);
            }
        });
        FormInput.add(ChambersLAC);
        ChambersLAC.setBounds(955, 111, 100, 26);

        jLabel56.setText("RAC  :");
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(1210, 111, 50, 26);

        ChambersRAC.setFocusTraversalPolicyProvider(true);
        ChambersRAC.setName("ChambersRAC"); // NOI18N
        ChambersRAC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ChambersRACKeyPressed(evt);
            }
        });
        FormInput.add(ChambersRAC);
        ChambersRAC.setBounds(1265, 111, 100, 26);

        jLabel57.setText("RVC  :");
        jLabel57.setName("jLabel57"); // NOI18N
        FormInput.add(jLabel57);
        jLabel57.setBounds(1060, 111, 40, 26);

        ChambersRVC.setFocusTraversalPolicyProvider(true);
        ChambersRVC.setName("ChambersRVC"); // NOI18N
        ChambersRVC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ChambersRVCKeyPressed(evt);
            }
        });
        FormInput.add(ChambersRVC);
        ChambersRVC.setBounds(1105, 111, 100, 26);

        jLabel58.setText("LVC  :");
        jLabel58.setName("jLabel58"); // NOI18N
        FormInput.add(jLabel58);
        jLabel58.setBounds(1370, 111, 40, 26);

        ChambersLVC.setFocusTraversalPolicyProvider(true);
        ChambersLVC.setName("ChambersLVC"); // NOI18N
        ChambersLVC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ChambersLVCKeyPressed(evt);
            }
        });
        FormInput.add(ChambersLVC);
        ChambersLVC.setBounds(1415, 111, 100, 26);

        jLabel73.setText("Wall motion abnormalities :");
        jLabel73.setName("jLabel73"); // NOI18N
        FormInput.add(jLabel73);
        jLabel73.setBounds(900, 143, 160, 26);

        jLabel74.setText("Diffuse  :");
        jLabel74.setName("jLabel74"); // NOI18N
        FormInput.add(jLabel74);
        jLabel74.setBounds(1210, 175, 50, 26);

        jLabel75.setText("Segmental  :");
        jLabel75.setName("jLabel75"); // NOI18N
        FormInput.add(jLabel75);
        jLabel75.setBounds(880, 175, 70, 26);

        PSLAXSegmental.setFocusTraversalPolicyProvider(true);
        PSLAXSegmental.setName("PSLAXSegmental"); // NOI18N
        PSLAXSegmental.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PSLAXSegmentalKeyPressed(evt);
            }
        });
        FormInput.add(PSLAXSegmental);
        PSLAXSegmental.setBounds(955, 175, 220, 26);

        PSLAXDiffuse.setFocusTraversalPolicyProvider(true);
        PSLAXDiffuse.setName("PSLAXDiffuse"); // NOI18N
        PSLAXDiffuse.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PSLAXDiffuseKeyPressed(evt);
            }
        });
        FormInput.add(PSLAXDiffuse);
        PSLAXDiffuse.setBounds(1265, 175, 250, 26);

        jLabel76.setText("Mass  :");
        jLabel76.setName("jLabel76"); // NOI18N
        FormInput.add(jLabel76);
        jLabel76.setBounds(800, 207, 150, 26);

        PSLAXMass.setFocusTraversalPolicyProvider(true);
        PSLAXMass.setName("PSLAXMass"); // NOI18N
        PSLAXMass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PSLAXMassKeyPressed(evt);
            }
        });
        FormInput.add(PSLAXMass);
        PSLAXMass.setBounds(955, 207, 220, 26);

        Myocardium.setFocusTraversalPolicyProvider(true);
        Myocardium.setName("Myocardium"); // NOI18N
        Myocardium.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MyocardiumKeyPressed(evt);
            }
        });
        FormInput.add(Myocardium);
        Myocardium.setBounds(1065, 143, 450, 26);

        jLabel77.setText("Pericardium  :");
        jLabel77.setName("jLabel77"); // NOI18N
        FormInput.add(jLabel77);
        jLabel77.setBounds(1170, 207, 90, 26);

        PSLAXPericardium.setFocusTraversalPolicyProvider(true);
        PSLAXPericardium.setName("PSLAXPericardium"); // NOI18N
        PSLAXPericardium.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PSLAXPericardiumKeyPressed(evt);
            }
        });
        FormInput.add(PSLAXPericardium);
        PSLAXPericardium.setBounds(1265, 207, 250, 26);

        jSeparator9.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator9.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator9.setName("jSeparator9"); // NOI18N
        FormInput.add(jSeparator9);
        jSeparator9.setBounds(750, 241, 750, 1);

        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel78.setText("PSSAX");
        jLabel78.setName("jLabel78"); // NOI18N
        FormInput.add(jLabel78);
        jLabel78.setBounds(770, 250, 50, 26);

        jLabel79.setText("Aortic valve  :");
        jLabel79.setName("jLabel79"); // NOI18N
        FormInput.add(jLabel79);
        jLabel79.setBounds(800, 250, 100, 26);

        PSSAXAorticValve.setFocusTraversalPolicyProvider(true);
        PSSAXAorticValve.setName("PSSAXAorticValve"); // NOI18N
        PSSAXAorticValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PSSAXAorticValveKeyPressed(evt);
            }
        });
        FormInput.add(PSSAXAorticValve);
        PSSAXAorticValve.setBounds(905, 250, 610, 26);

        jLabel80.setText("Mitral valve  :");
        jLabel80.setName("jLabel80"); // NOI18N
        FormInput.add(jLabel80);
        jLabel80.setBounds(800, 282, 100, 26);

        MitralValve.setFocusTraversalPolicyProvider(true);
        MitralValve.setName("MitralValve"); // NOI18N
        MitralValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MitralValveKeyPressed(evt);
            }
        });
        FormInput.add(MitralValve);
        MitralValve.setBounds(905, 282, 270, 26);

        jLabel81.setText("Left ventricle  :");
        jLabel81.setName("jLabel81"); // NOI18N
        FormInput.add(jLabel81);
        jLabel81.setBounds(1170, 282, 90, 26);

        LeftVentricle.setFocusTraversalPolicyProvider(true);
        LeftVentricle.setName("LeftVentricle"); // NOI18N
        LeftVentricle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftVentricleKeyPressed(evt);
            }
        });
        FormInput.add(LeftVentricle);
        LeftVentricle.setBounds(1265, 282, 250, 26);

        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel82.setText("AP4C");
        jLabel82.setName("jLabel82"); // NOI18N
        FormInput.add(jLabel82);
        jLabel82.setBounds(770, 325, 130, 26);

        jLabel83.setText("Wall motion abnormalities :");
        jLabel83.setName("jLabel83"); // NOI18N
        FormInput.add(jLabel83);
        jLabel83.setBounds(820, 325, 240, 26);

        jLabel84.setText("Diffuse  :");
        jLabel84.setName("jLabel84"); // NOI18N
        FormInput.add(jLabel84);
        jLabel84.setBounds(1180, 357, 80, 26);

        jLabel85.setText("Segmental  :");
        jLabel85.setName("jLabel85"); // NOI18N
        FormInput.add(jLabel85);
        jLabel85.setBounds(860, 357, 90, 26);

        APACSegmental.setFocusTraversalPolicyProvider(true);
        APACSegmental.setName("APACSegmental"); // NOI18N
        APACSegmental.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                APACSegmentalKeyPressed(evt);
            }
        });
        FormInput.add(APACSegmental);
        APACSegmental.setBounds(960, 357, 200, 26);

        APACDiffuce.setFocusTraversalPolicyProvider(true);
        APACDiffuce.setName("APACDiffuce"); // NOI18N
        APACDiffuce.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                APACDiffuceKeyPressed(evt);
            }
        });
        FormInput.add(APACDiffuce);
        APACDiffuce.setBounds(1265, 357, 250, 26);

        jLabel86.setText("Mitra valve  :");
        jLabel86.setName("jLabel86"); // NOI18N
        FormInput.add(jLabel86);
        jLabel86.setBounds(850, 389, 100, 26);

        APACMitraValve.setFocusTraversalPolicyProvider(true);
        APACMitraValve.setName("APACMitraValve"); // NOI18N
        APACMitraValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                APACMitraValveKeyPressed(evt);
            }
        });
        FormInput.add(APACMitraValve);
        APACMitraValve.setBounds(960, 389, 200, 26);

        APACWallMotion.setFocusTraversalPolicyProvider(true);
        APACWallMotion.setName("APACWallMotion"); // NOI18N
        APACWallMotion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                APACWallMotionKeyPressed(evt);
            }
        });
        FormInput.add(APACWallMotion);
        APACWallMotion.setBounds(1065, 325, 450, 26);

        jLabel87.setText("Triscupsid valve  :");
        jLabel87.setName("jLabel87"); // NOI18N
        FormInput.add(jLabel87);
        jLabel87.setBounds(1160, 389, 100, 26);

        APACTriscupsidValve.setFocusTraversalPolicyProvider(true);
        APACTriscupsidValve.setName("APACTriscupsidValve"); // NOI18N
        APACTriscupsidValve.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                APACTriscupsidValveKeyPressed(evt);
            }
        });
        FormInput.add(APACTriscupsidValve);
        APACTriscupsidValve.setBounds(1265, 389, 250, 26);

        jSeparator10.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator10.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator10.setName("jSeparator10"); // NOI18N
        FormInput.add(jSeparator10);
        jSeparator10.setBounds(750, 315, 750, 1);

        jSeparator11.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator11.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator11.setName("jSeparator11"); // NOI18N
        FormInput.add(jSeparator11);
        jSeparator11.setBounds(750, 423, 750, 1);

        jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel88.setText("DOPPLER ");
        jLabel88.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel88.setName("jLabel88"); // NOI18N
        FormInput.add(jLabel88);
        jLabel88.setBounds(770, 433, 70, 26);

        Shunts.setFocusTraversalPolicyProvider(true);
        Shunts.setName("Shunts"); // NOI18N
        Shunts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ShuntsKeyPressed(evt);
            }
        });
        FormInput.add(Shunts);
        Shunts.setBounds(905, 433, 230, 26);

        jLabel89.setText("Shunts  :");
        jLabel89.setName("jLabel89"); // NOI18N
        FormInput.add(jLabel89);
        jLabel89.setBounds(800, 433, 100, 26);

        ValveInsufficiency.setFocusTraversalPolicyProvider(true);
        ValveInsufficiency.setName("ValveInsufficiency"); // NOI18N
        ValveInsufficiency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ValveInsufficiencyKeyPressed(evt);
            }
        });
        FormInput.add(ValveInsufficiency);
        ValveInsufficiency.setBounds(1265, 433, 250, 26);

        jLabel90.setText("Valve insufficiency  :");
        jLabel90.setName("jLabel90"); // NOI18N
        FormInput.add(jLabel90);
        jLabel90.setBounds(1140, 433, 120, 26);

        jSeparator12.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator12.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(750, 470, 750, 1);

        jLabel91.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel91.setText("COMMENTS and FINAL CONCLUSION ");
        jLabel91.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel91.setName("jLabel91"); // NOI18N
        FormInput.add(jLabel91);
        jLabel91.setBounds(770, 480, 220, 26);

        jScrollPane1.setBackground(new java.awt.Color(247, 247, 247));
        jScrollPane1.setName("jScrollPane1"); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(226, 800));

        Comments.setColumns(20);
        Comments.setRows(5);
        Comments.setName("Comments"); // NOI18N
        Comments.setPreferredSize(new java.awt.Dimension(220, 800));
        jScrollPane1.setViewportView(Comments);

        FormInput.add(jScrollPane1);
        jScrollPane1.setBounds(770, 505, 745, 105);

        jLabel92.setBackground(new java.awt.Color(255, 255, 255));
        jLabel92.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel92.setText("M - MODE");
        jLabel92.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel92.setName("jLabel92"); // NOI18N
        FormInput.add(jLabel92);
        jLabel92.setBounds(15, 55, 90, 26);

        jLabel93.setBackground(new java.awt.Color(255, 255, 255));
        jLabel93.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel93.setText("2D - ECHOCARDIOGRAM ");
        jLabel93.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel93.setName("jLabel93"); // NOI18N
        FormInput.add(jLabel93);
        jLabel93.setBounds(770, 55, 180, 26);

        jSeparator13.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator13.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator13.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator13.setName("jSeparator13"); // NOI18N
        FormInput.add(jSeparator13);
        jSeparator13.setBounds(750, 70, 1, 600);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Hasil Pemeriksaan ECHO", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setDoubleBuffered(true);
        tbObat.setInheritsPopupMenu(true);
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

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "23-05-2025" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "23-05-2025" }));
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
        BtnCari.setIconTextGap(8);
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(70, 26));
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
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(430, 43));
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

        TabData.setBackground(new java.awt.Color(254, 255, 254));
        TabData.setForeground(new java.awt.Color(50, 50, 50));
        TabData.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabData.setName("TabData"); // NOI18N
        TabData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabDataMouseClicked(evt);
            }
        });

        FormPhoto.setBackground(new java.awt.Color(255, 255, 255));
        FormPhoto.setBorder(null);
        FormPhoto.setName("FormPhoto"); // NOI18N
        FormPhoto.setPreferredSize(new java.awt.Dimension(115, 73));
        FormPhoto.setLayout(new java.awt.BorderLayout());

        FormPass3.setBackground(new java.awt.Color(255, 255, 255));
        FormPass3.setBorder(null);
        FormPass3.setName("FormPass3"); // NOI18N
        FormPass3.setPreferredSize(new java.awt.Dimension(115, 40));

        btnAmbil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnAmbil.setMnemonic('U');
        btnAmbil.setText("Ambil");
        btnAmbil.setToolTipText("Alt+U");
        btnAmbil.setName("btnAmbil"); // NOI18N
        btnAmbil.setPreferredSize(new java.awt.Dimension(100, 30));
        btnAmbil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAmbilActionPerformed(evt);
            }
        });
        FormPass3.add(btnAmbil);

        BtnRefreshPhoto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnRefreshPhoto1.setMnemonic('U');
        BtnRefreshPhoto1.setText("Refresh");
        BtnRefreshPhoto1.setToolTipText("Alt+U");
        BtnRefreshPhoto1.setName("BtnRefreshPhoto1"); // NOI18N
        BtnRefreshPhoto1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRefreshPhoto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRefreshPhoto1ActionPerformed(evt);
            }
        });
        FormPass3.add(BtnRefreshPhoto1);

        FormPhoto.add(FormPass3, java.awt.BorderLayout.PAGE_END);

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);
        Scroll5.setPreferredSize(new java.awt.Dimension(200, 200));

        LoadHTML2.setBorder(null);
        LoadHTML2.setName("LoadHTML2"); // NOI18N
        Scroll5.setViewportView(LoadHTML2);

        FormPhoto.add(Scroll5, java.awt.BorderLayout.CENTER);

        TabData.addTab("Gambar Pemeriksaan ECHO", FormPhoto);

        FormOrthan.setBackground(new java.awt.Color(255, 255, 255));
        FormOrthan.setBorder(null);
        FormOrthan.setName("FormOrthan"); // NOI18N
        FormOrthan.setPreferredSize(new java.awt.Dimension(115, 73));
        FormOrthan.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll6.setName("Scroll6"); // NOI18N
        Scroll6.setOpaque(true);

        tbListDicom.setName("tbListDicom"); // NOI18N
        Scroll6.setViewportView(tbListDicom);

        FormOrthan.add(Scroll6, java.awt.BorderLayout.CENTER);

        panelGlass7.setBorder(null);
        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(115, 40));

        btnDicom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        btnDicom.setMnemonic('T');
        btnDicom.setText("Tampilkan DICOM");
        btnDicom.setToolTipText("Alt+T");
        btnDicom.setName("btnDicom"); // NOI18N
        btnDicom.setPreferredSize(new java.awt.Dimension(150, 30));
        btnDicom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDicomActionPerformed(evt);
            }
        });
        panelGlass7.add(btnDicom);

        FormOrthan.add(panelGlass7, java.awt.BorderLayout.PAGE_END);

        TabData.addTab("Integrasi Orthanc", FormOrthan);

        PanelAccor.add(TabData, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Hasil Pemeriksaan ECHO", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        TabRawat.getAccessibleContext().setAccessibleName("Input Hasil Pemeriksaan ECHOCARDIOGRAFI");

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
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(AortaRootDiameter.getText().trim().equals("")){
            Valid.textKosong(AortaRootDiameter,"Sitolik");
        }else if(Comments.getText().trim().equals("")){
            Valid.textKosong(Comments,"Comments");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19))==true){
                    simpan();
                }
            }
        }
    
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Comments,BtnBatal);
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
                if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
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
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(AortaRootDiameter.getText().trim().equals("")){
            Valid.textKosong(AortaRootDiameter,"AortaRootDiameter");
        }else if(Comments.getText().trim().equals("")){
            Valid.textKosong(Comments,"Kesimpulan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19))==true){
                                ganti();
                            }
                        }
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
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        if(tabMode.getRowCount()==0){
//            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
//            BtnBatal.requestFocus();
//        }else if(tabMode.getRowCount()!=0){
//            try{
//                htmlContent = new StringBuilder();
//                htmlContent.append(                             
//                    "<tr class='isi'>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.Rawat</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.RM</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Pasien</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tgl.Lahir</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kode Dokter</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Dokter</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tanggal</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Fungsi Sistolik LV</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Fungsi Diastolik LV</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kontraktilitas RV</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Dimensi Ruang Jantung</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Katup-katup</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Analisa Segmental</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>eRAP</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Lain-lain</b></td>"+
//                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kesimpulan</b></td>"+
//                    "</tr>"
//                );
//                for (i = 0; i < tabMode.getRowCount(); i++) {
//                    htmlContent.append(
//                        "<tr class='isi'>"+
//                           "<td valign='top'>"+tbObat.getValueAt(i,0).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
//                            "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
//                        "</tr>");
//                }
//                LoadHTML.setText(
//                    "<html>"+
//                      "<table width='2000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
//                       htmlContent.toString()+
//                      "</table>"+
//                    "</html>"
//                );
//
//                File g = new File("file2.css");            
//                BufferedWriter bg = new BufferedWriter(new FileWriter(g));
//                bg.write(
//                    ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
//                    ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
//                    ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
//                    ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
//                    ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
//                    ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
//                    ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
//                    ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
//                    ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
//                );
//                bg.close();
//
//                File f = new File("DataHasilPemeriksaanECHO.html");            
//                BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
//                bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
//                            "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
//                            "<table width='2000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
//                                "<tr class='isi2'>"+
//                                    "<td valign='top' align='center'>"+
//                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
//                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
//                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
//                                        "<font size='2' face='Tahoma'>DATA HASIL PEMERIKSAAN ECHO<br><br></font>"+        
//                                    "</td>"+
//                               "</tr>"+
//                            "</table>")
//                );
//                bw.close();                         
//                Desktop.getDesktop().browse(f.toURI());
//
//            }catch(Exception e){
//                System.out.println("Notifikasi : "+e);
//            }
//        }
//        this.setCursor(Cursor.getDefaultCursor());

if (tbObat.getSelectedRow() > -1) {
        kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='" + TNoRw.getText() + "' order by kamar_inap.tgl_masuk desc limit 1");

        if (!kamar.equals("")) {
            namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='" + kamar + "'");
            kamar = "Kamar";
        } else {
            kamar = "Poli";
            namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='" + TNoRw.getText() + "'");
        }

        String alamat = Sequel.cariIsi(
            "SELECT CONCAT(pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab) " +
            "FROM pasien " +
            "JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel " +
            "JOIN kecamatan ON pasien.kd_kec = kecamatan.kd_kec " +
            "JOIN kabupaten ON pasien.kd_kab = kabupaten.kd_kab " +
            "WHERE pasien.no_rkm_medis = ?", TNoRM.getText()
        );

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("kamar", kamar);
        param.put("namakamar", namakamar);
        param.put("alamat", alamat);
        param.put("tglRegistrasi", Sequel.cariIsi("Select tgl_registrasi from reg_periksa where no_rawat=?", TNoRw.getText()));

        String projectDir = System.getProperty("user.dir");
        String logoPath = projectDir + "/setting/logo2.png";
        param.put("logo2", logoPath);

        finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() +
                "\nDitandatangani secara elektronik oleh " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString() +
                "\nID " + (finger.equals("") ? tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString() : finger) +
                "\n" + Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()));

        // Debug log
        System.out.println("==== DEBUG CETAK ECHO ====");
        System.out.println("No. Rawat        : " + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        System.out.println("Nama Dokter      : " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        System.out.println("Tanggal Periksa  : " + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
        System.out.println("Kamar            : " + kamar);
        System.out.println("Nama Kamar       : " + namakamar);
        System.out.println("Alamat Pasien    : " + alamat);
        System.out.println("Logo Path        : " + logoPath);
        System.out.println("Finger Digital   : " + finger);
        System.out.println("Parameter Report : " + param.toString());

        String query = "SELECT ... WHERE hasil_pemeriksaan_echo.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'";
        System.out.println("Query Report     : " + query);
        System.out.println("Mulai mencetak report ECHO...");

        Valid.MyReportqry2("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
            "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, pasien.umur, pasien.jk, " +
            "hasil_pemeriksaan_echo.tanggal, hasil_pemeriksaan_echo.kd_dokter, dokter.nm_dokter, " +
            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
            "hasil_pemeriksaan_echo.Comments " +
            "FROM reg_periksa " +
            "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
            "INNER JOIN hasil_pemeriksaan_echo ON reg_periksa.no_rawat = hasil_pemeriksaan_echo.no_rawat " +
            "INNER JOIN dokter ON hasil_pemeriksaan_echo.kd_dokter = dokter.kd_dokter " +
            "WHERE hasil_pemeriksaan_echo.no_rawat = '" + tbObat.getValueAt(tbObat.getSelectedRow(),0).toString() + "'",
            param
        );
    } else {
        JOptionPane.showMessageDialog(null, "Silakan pilih data pemeriksaan terlebih dahulu.");
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
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                isPhoto();
                panggilPhoto();
                getData();
                tampilOrthanc();
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

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,TCari,Tanggal);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah2(evt,BtnDokter,AortaRootDiameter);
    }//GEN-LAST:event_TanggalKeyPressed

    private void MnHasilPemeriksaanEchoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnHasilPemeriksaanEchoActionPerformed
//     if(tbObat.getSelectedRow()>-1){   
////    if(tabMode.getRowCount()==0){
//            
//     kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+TNoRw.getText()+"' order by kamar_inap.tgl_masuk desc limit 1");
//        if(!kamar.equals("")){
//            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
//                    " where kamar.kd_kamar='"+kamar+"' ");            
//            kamar="Kamar";
//        }else if(kamar.equals("")){
//            kamar="Poli";
//            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
//                    "where reg_periksa.no_rawat='"+TNoRw.getText()+"'");
//            
//            String alamat = Sequel.cariIsi(
//                "SELECT CONCAT(pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab) " +
//                "FROM pasien " +
//                "JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel " +
//                "JOIN kecamatan ON pasien.kd_kec = kecamatan.kd_kec " +
//                "JOIN kabupaten ON pasien.kd_kab = kabupaten.kd_kab " +
//                "WHERE pasien.no_rkm_medis = ?",
//                TNoRM.getText()
//            );           
//            
//            Map<String, Object> param = new HashMap<>();
//            param.put("namars",akses.getnamars());
//            param.put("alamatrs",akses.getalamatrs());
//            param.put("kotars",akses.getkabupatenrs());
//            param.put("propinsirs",akses.getpropinsirs());
//            param.put("kontakrs",akses.getkontakrs());
//            param.put("emailrs",akses.getemailrs());   
//            param.put("kamar",kamar);
//            param.put("namakamar",namakamar); 
//            param.put("alamat",alamat);            
//            param.put("tglRegistrasi",Sequel.cariIsi("Select tgl_registrasi from reg_periksa where no_rawat=?",TNoRw.getText())); 
//            String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
//            String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
//            param.put("logo2", logoPath);
//            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
//            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),5).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),4).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString())); 
////            param.put("hasil","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+Sequel.cariIsi("select hasil_pemeriksaan_echo_gambar.photo from hasil_pemeriksaan_echo_gambar where hasil_pemeriksaan_echo_gambar.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()));
//            Valid.MyReportqry("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
//                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.umur,pasien.jk,hasil_pemeriksaan_echo.tanggal,"+
//                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
//                            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
//                            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
//                            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
//                            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
//                            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
//                            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
//                            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
//                            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
//                            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
//                            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
//                            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
//                            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
//                            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
//                            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
//                            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
//                            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
//                            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
//                            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
//                            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
//                            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
//                            "hasil_pemeriksaan_echo.Comments "+
//                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
//                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where hasil_pemeriksaan_echo.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);
//            }
//        }

    if (tbObat.getSelectedRow() > -1) {
        kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='" + TNoRw.getText() + "' order by kamar_inap.tgl_masuk desc limit 1");

        if (!kamar.equals("")) {
            namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='" + kamar + "'");
            kamar = "Kamar";
        } else {
            kamar = "Poli";
            namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='" + TNoRw.getText() + "'");
        }

        String alamat = Sequel.cariIsi(
            "SELECT CONCAT(pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab) " +
            "FROM pasien " +
            "JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel " +
            "JOIN kecamatan ON pasien.kd_kec = kecamatan.kd_kec " +
            "JOIN kabupaten ON pasien.kd_kab = kabupaten.kd_kab " +
            "WHERE pasien.no_rkm_medis = ?", TNoRM.getText()
        );

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("kamar", kamar);
        param.put("namakamar", namakamar);
        param.put("alamat", alamat);
        param.put("tglRegistrasi", Sequel.cariIsi("Select tgl_registrasi from reg_periksa where no_rawat=?", TNoRw.getText()));

        String projectDir = System.getProperty("user.dir");
        String logoPath = projectDir + "/setting/logo2.png";
        param.put("logo2", logoPath);

        finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() +
                "\nDitandatangani secara elektronik oleh " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString() +
                "\nID " + (finger.equals("") ? tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString() : finger) +
                "\n" + Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()));

        // Debug log
        System.out.println("==== DEBUG CETAK ECHO ====");
        System.out.println("No. Rawat        : " + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        System.out.println("Nama Dokter      : " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        System.out.println("Tanggal Periksa  : " + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
        System.out.println("Kamar            : " + kamar);
        System.out.println("Nama Kamar       : " + namakamar);
        System.out.println("Alamat Pasien    : " + alamat);
        System.out.println("Logo Path        : " + logoPath);
        System.out.println("Finger Digital   : " + finger);
        System.out.println("Parameter Report : " + param.toString());

        String query = "SELECT ... WHERE hasil_pemeriksaan_echo.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'";
        System.out.println("Query Report     : " + query);
        System.out.println("Mulai mencetak report ECHO...");

        Valid.MyReportqry2("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
            "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, pasien.umur, pasien.jk, " +
            "hasil_pemeriksaan_echo.tanggal, hasil_pemeriksaan_echo.kd_dokter, dokter.nm_dokter, " +
            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
            "hasil_pemeriksaan_echo.Comments " +
            "FROM reg_periksa " +
            "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
            "INNER JOIN hasil_pemeriksaan_echo ON reg_periksa.no_rawat = hasil_pemeriksaan_echo.no_rawat " +
            "INNER JOIN dokter ON hasil_pemeriksaan_echo.kd_dokter = dokter.kd_dokter " +
            "WHERE hasil_pemeriksaan_echo.no_rawat = '" + tbObat.getValueAt(tbObat.getSelectedRow(),0).toString() + "'",
            param
        );
    } else {
        JOptionPane.showMessageDialog(null, "Silakan pilih data pemeriksaan terlebih dahulu.");
    }

    }//GEN-LAST:event_MnHasilPemeriksaanEchoActionPerformed

    private void AortaRootDiameterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AortaRootDiameterKeyPressed
        Valid.pindah(evt,Tanggal,ValveCupsMotion);
    }//GEN-LAST:event_AortaRootDiameterKeyPressed

    private void RightVentricleDimensionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RightVentricleDimensionKeyPressed
        Valid.pindah(evt,AtriumLAAoRatio,LeftVentricleED);
    }//GEN-LAST:event_RightVentricleDimensionKeyPressed

    private void ValveCupsMotionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ValveCupsMotionKeyPressed
        Valid.pindah(evt,AortaRootDiameter,LeftAtriumDimension);
    }//GEN-LAST:event_ValveCupsMotionKeyPressed

    private void LeftVentricleEDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeftVentricleEDKeyPressed
        Valid.pindah(evt,RightVentricleDimension,LeftVentricleES);
    }//GEN-LAST:event_LeftVentricleEDKeyPressed

    private void AtriumLAAoRatioKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AtriumLAAoRatioKeyPressed
        Valid.pindah(evt,LeftAtriumDimension,RightVentricleDimension);
    }//GEN-LAST:event_AtriumLAAoRatioKeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isPhoto();
            panggilPhoto();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Silahkan pilih No.Pernyataan..!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void LeftVentricleESKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeftVentricleESKeyPressed
        Valid.pindah(evt,LeftVentricleED,LeftVentricleEF);
    }//GEN-LAST:event_LeftVentricleESKeyPressed

    private void LeftAtriumDimensionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeftAtriumDimensionKeyPressed
        Valid.pindah(evt,ValveCupsMotion,AtriumLAAoRatio);
    }//GEN-LAST:event_LeftAtriumDimensionKeyPressed

    private void LeftVentricleEFKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeftVentricleEFKeyPressed
        Valid.pindah(evt,LeftVentricleES,Comments);
    }//GEN-LAST:event_LeftVentricleEFKeyPressed

    private void btnAmbilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Valid.panggilUrl("hasilpemeriksaanecho/login.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Rawat terlebih dahulu..!!");
            }
        }
    }//GEN-LAST:event_btnAmbilActionPerformed

    private void BtnRefreshPhoto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhoto1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            panggilPhoto();
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Rawat terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnRefreshPhoto1ActionPerformed

    private void btnDicomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDicomActionPerformed
        if(tabModeDicom.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else {
            if(tbListDicom.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                OrthancDICOM orthan=new OrthancDICOM(null,false);
                orthan.setJudul("::[ DICOM Orthanc Pasien "+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+" "+tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()+", Series "+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString()+" ]::",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString().replaceAll("/","")+"_"+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+"_"+tbObat.getValueAt(tbObat.getSelectedRow(),2).toString().replaceAll(" ","_").replaceAll("/",""),tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString());
                try {
                    orthan.loadURL(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/web-viewer/app/viewer.html?series="+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString());
                } catch (Exception ex) {
                    System.out.println("Notifikasi : "+ex);
                }
                orthan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                orthan.setLocationRelativeTo(internalFrame1);
                orthan.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
            }
        }
    }//GEN-LAST:event_btnDicomActionPerformed

    private void TabDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabDataMouseClicked
        tampilOrthanc();
    }//GEN-LAST:event_TabDataMouseClicked

    private void LVPWthickeningEDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LVPWthickeningEDKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_LVPWthickeningEDKeyPressed

    private void FractShorteningFSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FractShorteningFSKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_FractShorteningFSKeyPressed

    private void LVPWThickeningKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LVPWThickeningKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_LVPWThickeningKeyPressed

    private void IVSThickeningIVSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IVSThickeningIVSKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IVSThickeningIVSKeyPressed

    private void IVSthickeningKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IVSthickeningKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IVSthickeningKeyPressed

    private void IVSRatioKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IVSRatioKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IVSRatioKeyPressed

    private void AnteriorLeafletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AnteriorLeafletKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_AnteriorLeafletKeyPressed

    private void DiastolicMotionTeksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiastolicMotionTeksKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DiastolicMotionTeksKeyPressed

    private void PosteriorLeafletTeksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PosteriorLeafletTeksKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PosteriorLeafletTeksKeyPressed

    private void TricuspidValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TricuspidValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TricuspidValveKeyPressed

    private void PulmonaryValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PulmonaryValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PulmonaryValveKeyPressed

    private void PericardialEffusionTeksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PericardialEffusionTeksKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PericardialEffusionTeksKeyPressed

    private void ValvesAOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ValvesAOKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValvesAOKeyPressed

    private void ValvesMitralKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ValvesMitralKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValvesMitralKeyPressed

    private void ChambersLACKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ChambersLACKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChambersLACKeyPressed

    private void ChambersRACKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ChambersRACKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChambersRACKeyPressed

    private void ChambersRVCKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ChambersRVCKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChambersRVCKeyPressed

    private void ChambersLVCKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ChambersLVCKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChambersLVCKeyPressed

    private void PSLAXSegmentalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PSLAXSegmentalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PSLAXSegmentalKeyPressed

    private void PSLAXDiffuseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PSLAXDiffuseKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PSLAXDiffuseKeyPressed

    private void PSLAXMassKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PSLAXMassKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PSLAXMassKeyPressed

    private void MyocardiumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MyocardiumKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_MyocardiumKeyPressed

    private void PSLAXPericardiumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PSLAXPericardiumKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PSLAXPericardiumKeyPressed

    private void PSSAXAorticValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PSSAXAorticValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PSSAXAorticValveKeyPressed

    private void MitralValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MitralValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_MitralValveKeyPressed

    private void LeftVentricleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeftVentricleKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_LeftVentricleKeyPressed

    private void APACSegmentalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_APACSegmentalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_APACSegmentalKeyPressed

    private void APACDiffuceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_APACDiffuceKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_APACDiffuceKeyPressed

    private void APACMitraValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_APACMitraValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_APACMitraValveKeyPressed

    private void APACWallMotionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_APACWallMotionKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_APACWallMotionKeyPressed

    private void APACTriscupsidValveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_APACTriscupsidValveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_APACTriscupsidValveKeyPressed

    private void ShuntsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ShuntsKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ShuntsKeyPressed

    private void ValveInsufficiencyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ValveInsufficiencyKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValveInsufficiencyKeyPressed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());

        FileName = timestamp + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().replace("/", "") + "_Hasil_Pemeriksaan_Echo";

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
            RMHasilPemeriksaanEcho dialog = new RMHasilPemeriksaanEcho(new javax.swing.JFrame(), true);
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
    private widget.TextBox APACDiffuce;
    private widget.TextBox APACMitraValve;
    private widget.TextBox APACSegmental;
    private widget.TextBox APACTriscupsidValve;
    private widget.TextBox APACWallMotion;
    private widget.TextBox AnteriorLeaflet;
    private widget.TextBox AortaRootDiameter;
    private widget.TextBox AtriumLAAoRatio;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnRefreshPhoto1;
    private widget.Button BtnSimpan;
    private widget.Button BtnUpload;
    private widget.TextBox ChambersLAC;
    private widget.TextBox ChambersLVC;
    private widget.TextBox ChambersRAC;
    private widget.TextBox ChambersRVC;
    private widget.CekBox ChkAccor;
    private widget.TextArea Comments;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox DiastolicMotionCMbox;
    private widget.TextBox DiastolicMotionTeks;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormOrthan;
    private widget.PanelBiasa FormPass3;
    private widget.PanelBiasa FormPhoto;
    private widget.TextBox FractShorteningFS;
    private widget.TextBox IVSRatio;
    private widget.TextBox IVSThickeningIVS;
    private widget.ComboBox IVSmationPattern;
    private widget.TextBox IVSthickening;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private widget.TextBox LVPWThickening;
    private widget.ComboBox LVPWmationPattern;
    private widget.TextBox LVPWthickeningED;
    private widget.TextBox LeftAtriumDimension;
    private widget.TextBox LeftVentricle;
    private widget.TextBox LeftVentricleED;
    private widget.TextBox LeftVentricleEF;
    private widget.TextBox LeftVentricleES;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML2;
    private widget.TextBox MitralValve;
    private javax.swing.JMenuItem MnHasilPemeriksaanEcho;
    private widget.TextBox Myocardium;
    private widget.TextBox NmDokter;
    private widget.TextBox PSLAXDiffuse;
    private widget.TextBox PSLAXMass;
    private widget.TextBox PSLAXPericardium;
    private widget.TextBox PSLAXSegmental;
    private widget.TextBox PSSAXAorticValve;
    private widget.PanelBiasa PanelAccor;
    private widget.ComboBox PericardialEffusionCMBox;
    private widget.TextBox PericardialEffusionTeks;
    private widget.ComboBox PosteriorLeafletCMbox;
    private widget.TextBox PosteriorLeafletTeks;
    private widget.TextBox PulmonaryValve;
    private widget.TextBox RightVentricleDimension;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll5;
    private widget.ScrollPane Scroll6;
    private widget.TextBox Shunts;
    private widget.ComboBox SystolicMotion;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabData;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.TextBox TricuspidValve;
    private widget.TextBox ValveCupsMotion;
    private widget.TextBox ValveInsufficiency;
    private widget.TextBox ValvesAO;
    private widget.TextBox ValvesMitral;
    private widget.Button btnAmbil;
    private widget.Button btnDicom;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel45;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel52;
    private widget.Label jLabel53;
    private widget.Label jLabel54;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel57;
    private widget.Label jLabel58;
    private widget.Label jLabel59;
    private widget.Label jLabel6;
    private widget.Label jLabel60;
    private widget.Label jLabel61;
    private widget.Label jLabel62;
    private widget.Label jLabel63;
    private widget.Label jLabel64;
    private widget.Label jLabel65;
    private widget.Label jLabel66;
    private widget.Label jLabel67;
    private widget.Label jLabel68;
    private widget.Label jLabel69;
    private widget.Label jLabel7;
    private widget.Label jLabel70;
    private widget.Label jLabel71;
    private widget.Label jLabel72;
    private widget.Label jLabel73;
    private widget.Label jLabel74;
    private widget.Label jLabel75;
    private widget.Label jLabel76;
    private widget.Label jLabel77;
    private widget.Label jLabel78;
    private widget.Label jLabel79;
    private widget.Label jLabel8;
    private widget.Label jLabel80;
    private widget.Label jLabel81;
    private widget.Label jLabel82;
    private widget.Label jLabel83;
    private widget.Label jLabel84;
    private widget.Label jLabel85;
    private widget.Label jLabel86;
    private widget.Label jLabel87;
    private widget.Label jLabel88;
    private widget.Label jLabel89;
    private widget.Label jLabel90;
    private widget.Label jLabel91;
    private widget.Label jLabel92;
    private widget.Label jLabel93;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private widget.Label label11;
    private widget.Label label14;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.Table tbListDicom;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                            "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, " +
                            "hasil_pemeriksaan_echo.tanggal, hasil_pemeriksaan_echo.kd_dokter, dokter.nm_dokter, " +
                            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
                            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
                            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
                            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
                            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
                            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
                            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
                            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
                            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
                            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
                            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
                            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
                            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
                            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
                            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
                            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
                            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
                            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
                            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
                            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
                            "hasil_pemeriksaan_echo.Comments " +
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where "+
                            "hasil_pemeriksaan_echo.tanggal between ? and ? order by hasil_pemeriksaan_echo.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,hasil_pemeriksaan_echo.tanggal,"+
                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
                            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
                            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
                            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
                            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
                            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
                            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
                            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
                            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
                            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
                            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
                            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
                            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
                            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
                            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
                            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
                            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
                            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
                            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
                            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
                            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
                            "hasil_pemeriksaan_echo.Comments "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where "+
                            "hasil_pemeriksaan_echo.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                            "hasil_pemeriksaan_echo.kd_dokter like ? or dokter.nm_dokter like ?) order by hasil_pemeriksaan_echo.tanggal");
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
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"),
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter"),
                        rs.getString("tanggal"),
                        rs.getString("AortaRootDiameter"),
                        rs.getString("ValveCupsMotion"),
                        rs.getString("LeftAtriumDimension"),
                        rs.getString("AtriumLAAoRatio"),
                        rs.getString("RightVentricleDimension"),
                        rs.getString("LeftVentricleED"),
                        rs.getString("LeftVentricleES"),
                        rs.getString("LeftVentricleEF"),
                        rs.getString("FractShorteningFS"),
                        rs.getString("LVPWthickeningED"),
                        rs.getString("LVPWThickening"),
                        rs.getString("LVPWmationPattern"),
                        rs.getString("IVSThickeningIVS"),
                        rs.getString("IVSthickening"),
                        rs.getString("IVSmationPattern"),
                        rs.getString("IVSRatio"),
                        rs.getString("AnteriorLeaflet"),
                        rs.getString("DiastolicMotionTeks"),
                        rs.getString("DiastolicMotionCMbox"),
                        rs.getString("SystolicMotion"),
                        rs.getString("PosteriorLeafletCMbox"),
                        rs.getString("PosteriorLeafletTeks"),
                        rs.getString("TricuspidValve"),
                        rs.getString("PulmonaryValve"),
                        rs.getString("PericardialEffusionCMBox"),
                        rs.getString("PericardialEffusionTeks"),
                        rs.getString("ValvesAO"),
                        rs.getString("ValvesMitral"),
                        rs.getString("ChambersLAC"),
                        rs.getString("ChambersRVC"),
                        rs.getString("ChambersRAC"),
                        rs.getString("ChambersLVC"),
                        rs.getString("Myocardium"),
                        rs.getString("PSLAXSegmental"),
                        rs.getString("PSLAXDiffuse"),
                        rs.getString("PSLAXMass"),
                        rs.getString("PSLAXPericardium"),
                        rs.getString("PSSAXAorticValve"),
                        rs.getString("MitralValve"),
                        rs.getString("LeftVentricle"),
                        rs.getString("APACWallMotion"),
                        rs.getString("APACSegmental"),
                        rs.getString("APACDiffuce"),
                        rs.getString("APACMitraValve"),
                        rs.getString("APACTriscupsidValve"),
                        rs.getString("Shunts"),
                        rs.getString("ValveInsufficiency"),
                        rs.getString("Comments")
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

//    Valid.tabelKosong(tabMode);
//    try {
//        ps = koneksi.prepareStatement(
//            "SELECT h.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, d.nm_dokter FROM hasil_pemeriksaan_echo h " +
//            "JOIN reg_periksa r ON h.no_rawat = r.no_rawat " +
//            "JOIN pasien p ON r.no_rkm_medis = p.no_rkm_medis " +
//            "JOIN dokter d ON h.kd_dokter = d.kd_dokter " +
//            "WHERE h.tanggal BETWEEN ? AND ? ORDER BY h.tanggal"
//        );
//        ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
//        ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
//        rs = ps.executeQuery();
//        while (rs.next()) {
//            tabMode.addRow(new Object[]{
//                rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"), rs.getString("tgl_lahir"),
//                rs.getString("kd_dokter"), rs.getString("nm_dokter"), rs.getString("tanggal"),
//                rs.getString("AortaRootDiameter"), rs.getString("ValveCupsMotion"), rs.getString("LeftAtriumDimension"), rs.getString("AtriumLAAoRatio"),
//                rs.getString("RightVentricleDimension"), rs.getString("LeftVentricleED"), rs.getString("LeftVentricleES"), rs.getString("LeftVentricleEF"),
//                rs.getString("FractShorteningFS"), rs.getString("LVPWthickeningED"), rs.getString("LVPWThickening"), rs.getString("LVPWmationPattern"),
//                rs.getString("IVSThickeningIVS"), rs.getString("IVSthickening"), rs.getString("IVSmationPattern"), rs.getString("IVSRatio"),
//                rs.getString("AnteriorLeaflet"), rs.getString("DiastolicMotionTeks"), rs.getString("DiastolicMotionCMbox"),
//                rs.getString("SystolicMotion"), rs.getString("PosteriorLeafletCMbox"), rs.getString("PosteriorLeafletTeks"),
//                rs.getString("TricuspidValve"), rs.getString("PulmonaryValve"), rs.getString("PericardialEffusionCMBox"), rs.getString("PericardialEffusionTeks"),
//                rs.getString("ValvesAO"), rs.getString("ValvesMitral"), rs.getString("ChambersLAC"), rs.getString("ChambersRVC"), rs.getString("ChambersRAC"), rs.getString("ChambersLVC"),
//                rs.getString("Myocardium"), rs.getString("PSLAXSegmental"), rs.getString("PSLAXDiffuse"), rs.getString("PSLAXMass"), rs.getString("PSLAXPericardium"),
//                rs.getString("PSSAXAorticValve"), rs.getString("MitralValve"), rs.getString("LeftVentricle"), rs.getString("APACWallMotion"),
//                rs.getString("APACSegmental"), rs.getString("APACDiffuce"), rs.getString("APACMitraValve"), rs.getString("APACTriscupsidValve"),
//                rs.getString("Shunts"), rs.getString("ValveInsufficiency"), rs.getString("Comments")
//            });
//        }
//    } catch (Exception e) {
//        System.out.println("Notif tampil : " + e);
//    } finally {
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(RMHasilPemeriksaanEcho.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }        
//    }
    
    }

    public void emptTeks() {
//        AortaRootDiameter.setText("");
//        ValveCupsMotion.setText("");
//        LeftAtriumDimension.setText("");
//        AtriumLAAoRatio.setText("");
//        RightVentricleDimension.setText("");
//        LeftVentricleED.setText("");
//        LeftVentricleES.setText("");
//        LeftVentricleEF.setText("");
//        Kesimpulan.setText("");
//        Tanggal.setDate(new Date());
//        TabRawat.setSelectedIndex(0);
        AortaRootDiameter.requestFocus();

    AortaRootDiameter.setText("");
    ValveCupsMotion.setText("");
    LeftAtriumDimension.setText("");
    AtriumLAAoRatio.setText("");
    RightVentricleDimension.setText("");
    LeftVentricleED.setText("");
    LeftVentricleES.setText("");
    LeftVentricleEF.setText("");
    FractShorteningFS.setText("");
    LVPWthickeningED.setText("");
    LVPWThickening.setText("");
    LVPWmationPattern.setSelectedIndex(0);
    IVSThickeningIVS.setText("");
    IVSthickening.setText("");
    IVSmationPattern.setSelectedIndex(0);
    IVSRatio.setText("");
    AnteriorLeaflet.setText("");
    DiastolicMotionTeks.setText("");
    DiastolicMotionCMbox.setSelectedIndex(0);
    SystolicMotion.setSelectedIndex(0);
    PosteriorLeafletCMbox.setSelectedIndex(0);
    PosteriorLeafletTeks.setText("");
    TricuspidValve.setText("");
    PulmonaryValve.setText("");
    PericardialEffusionCMBox.setSelectedIndex(0);
    PericardialEffusionTeks.setText("");
    ValvesAO.setText("");
    ValvesMitral.setText("");
    ChambersLAC.setText("");
    ChambersRVC.setText("");
    ChambersRAC.setText("");
    ChambersLVC.setText("");
    Myocardium.setText("");
    PSLAXSegmental.setText("");
    PSLAXDiffuse.setText("");
    PSLAXMass.setText("");
    PSLAXPericardium.setText("");
    PSSAXAorticValve.setText("");
    MitralValve.setText("");
    LeftVentricle.setText("");
    APACWallMotion.setText("");
    APACSegmental.setText("");
    APACDiffuce.setText("");
    APACMitraValve.setText("");
    APACTriscupsidValve.setText("");
    Shunts.setText("");
    ValveInsufficiency.setText("");
    Comments.setText("");
    Tanggal.setDate(new Date());
    KdDokter.setText("");
    NmDokter.setText("");
    TNoRw.requestFocus();
    
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
//            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
//            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
//            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
//            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
//            AortaRootDiameter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
//            ValveCupsMotion.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
//            LeftAtriumDimension.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
//            AtriumLAAoRatio.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
//            RightVentricleDimension.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
//            LeftVentricleED.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
//            LeftVentricleES.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
//            LeftVentricleEF.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
//            Kesimpulan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
//            Valid.SetTgl2(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());

        TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
        TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
        TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
        KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        NmDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        Valid.SetTgl2(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());

        AortaRootDiameter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
        ValveCupsMotion.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
        LeftAtriumDimension.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
        AtriumLAAoRatio.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
        RightVentricleDimension.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
        LeftVentricleED.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
        LeftVentricleES.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
        LeftVentricleEF.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
        FractShorteningFS.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString());
        LVPWthickeningED.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());
        LVPWThickening.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 17).toString());
        LVPWmationPattern.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 18).toString());
        IVSThickeningIVS.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 19).toString());
        IVSthickening.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 20).toString());
        IVSmationPattern.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 21).toString());
        IVSRatio.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 22).toString());
        AnteriorLeaflet.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 23).toString());
        DiastolicMotionTeks.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 24).toString());
        DiastolicMotionCMbox.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 25).toString());
        SystolicMotion.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 26).toString());
        PosteriorLeafletCMbox.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 27).toString());
        PosteriorLeafletTeks.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 28).toString());
        TricuspidValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 29).toString());
        PulmonaryValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 30).toString());
        PericardialEffusionCMBox.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 31).toString());
        PericardialEffusionTeks.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 32).toString());
        ValvesAO.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 33).toString());
        ValvesMitral.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 34).toString());
        ChambersLAC.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 35).toString());
        ChambersRVC.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 36).toString());
        ChambersRAC.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 37).toString());
        ChambersLVC.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 38).toString());
        Myocardium.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 39).toString());
        PSLAXSegmental.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 40).toString());
        PSLAXDiffuse.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 41).toString());
        PSLAXMass.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 42).toString());
        PSLAXPericardium.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 43).toString());
        PSSAXAorticValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 44).toString());
        MitralValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 45).toString());
        LeftVentricle.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 46).toString());
        APACWallMotion.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 47).toString());
        APACSegmental.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 48).toString());
        APACDiffuce.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 49).toString());
        APACMitraValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 50).toString());
        APACTriscupsidValve.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 51).toString());
        Shunts.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 52).toString());
        ValveInsufficiency.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 53).toString());
        Comments.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 54).toString()); // Comments
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
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
 
    public void setNoRm(String norwt,Date tgl2) {
        isPsien();
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        
        try (PreparedStatement ps = koneksi.prepareStatement(
        "SELECT COUNT(*) FROM hasil_pemeriksaan_echo WHERE no_rawat = ?")) {
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
    
private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }    
    
    public void isCek(){
//        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
//        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
//        BtnEdit.setEnabled(akses.gethasil_pemeriksaan_echo());        
//        if(akses.getjml2()>=1){
////            KdDokter.setEditable(false);
////            BtnDokter.setEnabled(false);
////            KdDokter.setText(akses.getkode());
//            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
//            if(NmDokter.getText().equals("")){
//                KdDokter.setText("");
//                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
//            }
//        }  
//        
//        if(TANGGALMUNDUR.equals("no")){
//            if(!akses.getkode().equals("Admin Utama")){
//                Tanggal.setEditable(false);
//                Tanggal.setEnabled(false);
//            }
//        }
    }
    
    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from hasil_pemeriksaan_echo where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void ganti() {
        if(Sequel.mengedittf("hasil_pemeriksaan_echo","no_rawat=?",
                "no_rawat=?, tanggal=?, kd_dokter=?," +
                "AortaRootDiameter=?, ValveCupsMotion=?, LeftAtriumDimension=?, AtriumLAAoRatio=?, RightVentricleDimension=?," +
                "LeftVentricleED=?, LeftVentricleES=?, LeftVentricleEF=?, FractShorteningFS=?, LVPWthickeningED=?, LVPWThickening=?," +
                "LVPWmationPattern=?, IVSThickeningIVS=?, IVSthickening=?, IVSmationPattern=?, IVSRatio=?, AnteriorLeaflet=?," +
                "DiastolicMotionTeks=?, DiastolicMotionCMbox=?, SystolicMotion=?, PosteriorLeafletCMbox=?, PosteriorLeafletTeks=?," +
                "TricuspidValve=?, PulmonaryValve=?, PericardialEffusionCMBox=?, PericardialEffusionTeks=?, ValvesAO=?, ValvesMitral=?," +
                "ChambersLAC=?, ChambersRVC=?, ChambersRAC=?, ChambersLVC=?, Myocardium=?, PSLAXSegmental=?, PSLAXDiffuse=?," +
                "PSLAXMass=?, PSLAXPericardium=?, PSSAXAorticValve=?, MitralValve=?, LeftVentricle=?, APACWallMotion=?," +
                "APACSegmental=?, APACDiffuce=?, APACMitraValve=?, APACTriscupsidValve=?, Shunts=?, ValveInsufficiency=?, Comments=?",52,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),KdDokter.getText(),
                AortaRootDiameter.getText(), ValveCupsMotion.getText(), LeftAtriumDimension.getText(),
                AtriumLAAoRatio.getText(), RightVentricleDimension.getText(), LeftVentricleED.getText(), LeftVentricleES.getText(),
                LeftVentricleEF.getText(), FractShorteningFS.getText(), LVPWthickeningED.getText(), LVPWThickening.getText(),
                LVPWmationPattern.getSelectedItem().toString(), IVSThickeningIVS.getText(), IVSthickening.getText(),
                IVSmationPattern.getSelectedItem().toString(), IVSRatio.getText(), AnteriorLeaflet.getText(),
                DiastolicMotionTeks.getText(), DiastolicMotionCMbox.getSelectedItem().toString(),
                SystolicMotion.getSelectedItem().toString(), PosteriorLeafletCMbox.getSelectedItem().toString(),
                PosteriorLeafletTeks.getText(), TricuspidValve.getText(), PulmonaryValve.getText(),
                PericardialEffusionCMBox.getSelectedItem().toString(), PericardialEffusionTeks.getText(),
                ValvesAO.getText(), ValvesMitral.getText(), ChambersLAC.getText(), ChambersRVC.getText(),
                ChambersRAC.getText(), ChambersLVC.getText(), Myocardium.getText(), PSLAXSegmental.getText(),
                PSLAXDiffuse.getText(), PSLAXMass.getText(), PSLAXPericardium.getText(), PSSAXAorticValve.getText(),
                MitralValve.getText(), LeftVentricle.getText(), APACWallMotion.getText(), APACSegmental.getText(),
                APACDiffuce.getText(), APACMitraValve.getText(), APACTriscupsidValve.getText(), Shunts.getText(),
                ValveInsufficiency.getText(), Comments.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
            })==true){
                tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
                tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
                tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
                tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),3);
                tbObat.setValueAt(KdDokter.getText(),tbObat.getSelectedRow(),4);
                tbObat.setValueAt(NmDokter.getText(),tbObat.getSelectedRow(),5);
                tbObat.setValueAt(Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),6);
                tbObat.setValueAt(AortaRootDiameter.getText(), tbObat.getSelectedRow(), 7);
                tbObat.setValueAt(ValveCupsMotion.getText(), tbObat.getSelectedRow(), 8);
                tbObat.setValueAt(LeftAtriumDimension.getText(), tbObat.getSelectedRow(), 9);
                tbObat.setValueAt(AtriumLAAoRatio.getText(), tbObat.getSelectedRow(), 10);
                tbObat.setValueAt(RightVentricleDimension.getText(), tbObat.getSelectedRow(), 11);
                tbObat.setValueAt(LeftVentricleED.getText(), tbObat.getSelectedRow(), 12);
                tbObat.setValueAt(LeftVentricleES.getText(), tbObat.getSelectedRow(), 13);
                tbObat.setValueAt(LeftVentricleEF.getText(), tbObat.getSelectedRow(), 14);
                tbObat.setValueAt(FractShorteningFS.getText(), tbObat.getSelectedRow(), 15);
                tbObat.setValueAt(LVPWthickeningED.getText(), tbObat.getSelectedRow(), 16);
                tbObat.setValueAt(LVPWThickening.getText(), tbObat.getSelectedRow(), 17);
                tbObat.setValueAt(LVPWmationPattern.getSelectedItem().toString(), tbObat.getSelectedRow(), 18);
                tbObat.setValueAt(IVSThickeningIVS.getText(), tbObat.getSelectedRow(), 19);
                tbObat.setValueAt(IVSthickening.getText(), tbObat.getSelectedRow(), 20);
                tbObat.setValueAt(IVSmationPattern.getSelectedItem().toString(), tbObat.getSelectedRow(), 21);
                tbObat.setValueAt(IVSRatio.getText(), tbObat.getSelectedRow(), 22);
                tbObat.setValueAt(AnteriorLeaflet.getText(), tbObat.getSelectedRow(), 23);
                tbObat.setValueAt(DiastolicMotionTeks.getText(), tbObat.getSelectedRow(), 24);
                tbObat.setValueAt(DiastolicMotionCMbox.getSelectedItem().toString(), tbObat.getSelectedRow(), 25);
                tbObat.setValueAt(SystolicMotion.getSelectedItem().toString(), tbObat.getSelectedRow(), 26);
                tbObat.setValueAt(PosteriorLeafletCMbox.getSelectedItem().toString(), tbObat.getSelectedRow(), 27);
                tbObat.setValueAt(PosteriorLeafletTeks.getText(), tbObat.getSelectedRow(), 28);
                tbObat.setValueAt(TricuspidValve.getText(), tbObat.getSelectedRow(), 29);
                tbObat.setValueAt(PulmonaryValve.getText(), tbObat.getSelectedRow(), 30);
                tbObat.setValueAt(PericardialEffusionCMBox.getSelectedItem().toString(), tbObat.getSelectedRow(), 31);
                tbObat.setValueAt(PericardialEffusionTeks.getText(), tbObat.getSelectedRow(), 32);
                tbObat.setValueAt(ValvesAO.getText(), tbObat.getSelectedRow(), 33);
                tbObat.setValueAt(ValvesMitral.getText(), tbObat.getSelectedRow(), 34);
                tbObat.setValueAt(ChambersLAC.getText(), tbObat.getSelectedRow(), 35);
                tbObat.setValueAt(ChambersRVC.getText(), tbObat.getSelectedRow(), 36);
                tbObat.setValueAt(ChambersRAC.getText(), tbObat.getSelectedRow(), 37);
                tbObat.setValueAt(ChambersLVC.getText(), tbObat.getSelectedRow(), 38);
                tbObat.setValueAt(Myocardium.getText(), tbObat.getSelectedRow(), 39);
                tbObat.setValueAt(PSLAXSegmental.getText(), tbObat.getSelectedRow(), 40);
                tbObat.setValueAt(PSLAXDiffuse.getText(), tbObat.getSelectedRow(), 41);
                tbObat.setValueAt(PSLAXMass.getText(), tbObat.getSelectedRow(), 42);
                tbObat.setValueAt(PSLAXPericardium.getText(), tbObat.getSelectedRow(), 43);
                tbObat.setValueAt(PSSAXAorticValve.getText(), tbObat.getSelectedRow(), 44);
                tbObat.setValueAt(MitralValve.getText(), tbObat.getSelectedRow(), 45);
                tbObat.setValueAt(LeftVentricle.getText(), tbObat.getSelectedRow(), 46);
                tbObat.setValueAt(APACWallMotion.getText(), tbObat.getSelectedRow(), 47);
                tbObat.setValueAt(APACSegmental.getText(), tbObat.getSelectedRow(), 48);
                tbObat.setValueAt(APACDiffuce.getText(), tbObat.getSelectedRow(), 49);
                tbObat.setValueAt(APACMitraValve.getText(), tbObat.getSelectedRow(), 50);
                tbObat.setValueAt(APACTriscupsidValve.getText(), tbObat.getSelectedRow(), 51);
                tbObat.setValueAt(Shunts.getText(), tbObat.getSelectedRow(), 52);
                tbObat.setValueAt(ValveInsufficiency.getText(), tbObat.getSelectedRow(), 53);
                tbObat.setValueAt(Comments.getText(), tbObat.getSelectedRow(), 54);
                emptTeks();
                tampil();
                TabRawat.setSelectedIndex(1);
        }
    }
    
    private void isPhoto(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(530,HEIGHT));
            TabData.setVisible(true);  
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){    
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            TabData.setVisible(false);  
            ChkAccor.setVisible(true);
        }
    }

    private void panggilPhoto() {
        if(FormPhoto.isVisible()==true){
            try {
                ps=koneksi.prepareStatement("select hasil_pemeriksaan_echo_gambar.photo from hasil_pemeriksaan_echo_gambar where hasil_pemeriksaan_echo_gambar.no_rawat=?");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        if(rs.getString("photo").equals("")||rs.getString("photo").equals("-")){
                            LoadHTML2.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
                        }else{
                            LoadHTML2.setText("<html><body><center><a href='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+rs.getString("photo")+"'><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+rs.getString("photo")+"' alt='photo' width='550' height='550'/></a></center></body></html>");
                        }  
                    }else{
                        LoadHTML2.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
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

    private void simpan() {
        if(Sequel.menyimpantf("hasil_pemeriksaan_echo","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",51,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),KdDokter.getText(),
                AortaRootDiameter.getText(), ValveCupsMotion.getText(), LeftAtriumDimension.getText(),
                AtriumLAAoRatio.getText(), RightVentricleDimension.getText(), LeftVentricleED.getText(), LeftVentricleES.getText(),
                LeftVentricleEF.getText(), FractShorteningFS.getText(), LVPWthickeningED.getText(), LVPWThickening.getText(),
                LVPWmationPattern.getSelectedItem().toString(), IVSThickeningIVS.getText(), IVSthickening.getText(),
                IVSmationPattern.getSelectedItem().toString(), IVSRatio.getText(), AnteriorLeaflet.getText(),
                DiastolicMotionTeks.getText(), DiastolicMotionCMbox.getSelectedItem().toString(),
                SystolicMotion.getSelectedItem().toString(), PosteriorLeafletCMbox.getSelectedItem().toString(),
                PosteriorLeafletTeks.getText(), TricuspidValve.getText(), PulmonaryValve.getText(),
                PericardialEffusionCMBox.getSelectedItem().toString(), PericardialEffusionTeks.getText(),
                ValvesAO.getText(), ValvesMitral.getText(), ChambersLAC.getText(), ChambersRVC.getText(),
                ChambersRAC.getText(), ChambersLVC.getText(), Myocardium.getText(), PSLAXSegmental.getText(),
                PSLAXDiffuse.getText(), PSLAXMass.getText(), PSLAXPericardium.getText(), PSSAXAorticValve.getText(),
                MitralValve.getText(), LeftVentricle.getText(), APACWallMotion.getText(), APACSegmental.getText(),
                APACDiffuce.getText(), APACMitraValve.getText(), APACTriscupsidValve.getText(), Shunts.getText(),
                ValveInsufficiency.getText(), Comments.getText()
            })==true){
                tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),KdDokter.getText(),NmDokter.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),
                AortaRootDiameter.getText(), ValveCupsMotion.getText(), LeftAtriumDimension.getText(),
                AtriumLAAoRatio.getText(), RightVentricleDimension.getText(), LeftVentricleED.getText(), LeftVentricleES.getText(),
                LeftVentricleEF.getText(), FractShorteningFS.getText(), LVPWthickeningED.getText(), LVPWThickening.getText(),
                LVPWmationPattern.getSelectedItem().toString(), IVSThickeningIVS.getText(), IVSthickening.getText(),
                IVSmationPattern.getSelectedItem().toString(), IVSRatio.getText(), AnteriorLeaflet.getText(),
                DiastolicMotionTeks.getText(), DiastolicMotionCMbox.getSelectedItem().toString(),
                SystolicMotion.getSelectedItem().toString(), PosteriorLeafletCMbox.getSelectedItem().toString(),
                PosteriorLeafletTeks.getText(), TricuspidValve.getText(), PulmonaryValve.getText(),
                PericardialEffusionCMBox.getSelectedItem().toString(), PericardialEffusionTeks.getText(),
                ValvesAO.getText(), ValvesMitral.getText(), ChambersLAC.getText(), ChambersRVC.getText(),
                ChambersRAC.getText(), ChambersLVC.getText(), Myocardium.getText(), PSLAXSegmental.getText(),
                PSLAXDiffuse.getText(), PSLAXMass.getText(), PSLAXPericardium.getText(), PSSAXAorticValve.getText(),
                MitralValve.getText(), LeftVentricle.getText(), APACWallMotion.getText(), APACSegmental.getText(),
                APACDiffuce.getText(), APACMitraValve.getText(), APACTriscupsidValve.getText(), Shunts.getText(),
                ValveInsufficiency.getText(), Comments.getText()
                });
                emptTeks();                
                tampil();
                TabRawat.setSelectedIndex(1); 
                LCount.setText(""+tabMode.getRowCount());
        }
}
    
    private void tampilOrthanc() {
        if(TabData.isVisible()==true){
            if(tbObat.getSelectedRow()!= -1){
                 if(TabData.getSelectedIndex()==1){
                     try {
                         Valid.tabelKosong(tabModeDicom);
                         ApiOrthanc orthanc=new ApiOrthanc();
                         root=orthanc.AmbilSeries(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),Valid.SetTgl(DTPCari1.getSelectedItem()+"").replaceAll("-",""),Valid.SetTgl(DTPCari2.getSelectedItem()+"").replaceAll("-",""));
                         for(JsonNode list:root){
                             for(JsonNode sublist:list.path("Series")){
                                  tabModeDicom.addRow(new Object[]{
                                       list.path("PatientMainDicomTags").path("PatientID").asText(),list.path("ID").asText(),sublist.asText()
                                  });   
                             }        
                         }
                     } catch (Exception e) {
                         System.out.println("Notif : "+e);
                     }
                 }
            }
        }
    }

private void CreatePDF(String FileName) {
//    if(tbObat.getSelectedRow()>-1){                                  
//            
//     kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+TNoRw.getText()+"' order by kamar_inap.tgl_masuk desc limit 1");
//        if(!kamar.equals("")){
//            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
//                    " where kamar.kd_kamar='"+kamar+"' ");            
//            kamar="Kamar";
//        }else if(kamar.equals("")){
//            kamar="Poli";
//            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
//                    "where reg_periksa.no_rawat='"+TNoRw.getText()+"'");
//            
//            String alamat = Sequel.cariIsi(
//                "SELECT CONCAT(pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab) " +
//                "FROM pasien " +
//                "JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel " +
//                "JOIN kecamatan ON pasien.kd_kec = kecamatan.kd_kec " +
//                "JOIN kabupaten ON pasien.kd_kab = kabupaten.kd_kab " +
//                "WHERE pasien.no_rkm_medis = ?",
//                TNoRM.getText()
//            );           
//            
//            Map<String, Object> param = new HashMap<>();
//            param.put("namars",akses.getnamars());
//            param.put("alamatrs",akses.getalamatrs());
//            param.put("kotars",akses.getkabupatenrs());
//            param.put("propinsirs",akses.getpropinsirs());
//            param.put("kontakrs",akses.getkontakrs());
//            param.put("emailrs",akses.getemailrs());   
//            param.put("kamar",kamar);
//            param.put("namakamar",namakamar); 
//            param.put("alamat",alamat);            
//            param.put("tglRegistrasi",Sequel.cariIsi("Select tgl_registrasi from reg_periksa where no_rawat=?",TNoRw.getText())); 
//            String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
//            String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
//            param.put("logo2", logoPath);
//            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
//            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),5).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),4).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString())); 
////            param.put("hasil","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+Sequel.cariIsi("select hasil_pemeriksaan_echo_gambar.photo from hasil_pemeriksaan_echo_gambar where hasil_pemeriksaan_echo_gambar.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()));
//            Valid.MyReportPDFqryUpload("rptCetakHasilPemeriksaanECHO.jasper", "report", "::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
////            Valid.MyReportqry("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
//                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.umur,pasien.jk,hasil_pemeriksaan_echo.tanggal,"+
//                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
//                            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
//                            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
//                            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
//                            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
//                            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
//                            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
//                            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
//                            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
//                            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
//                            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
//                            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
//                            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
//                            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
//                            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
//                            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
//                            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
//                            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
//                            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
//                            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
//                            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
//                            "hasil_pemeriksaan_echo.Comments "+
//                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
//                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where hasil_pemeriksaan_echo.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",FileName, param);   
//            }
//        }     

if (tbObat.getSelectedRow() > -1) {
        kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='" + TNoRw.getText() + "' order by kamar_inap.tgl_masuk desc limit 1");

        if (!kamar.equals("")) {
            namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='" + kamar + "'");
            kamar = "Kamar";
        } else {
            kamar = "Poli";
            namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='" + TNoRw.getText() + "'");
        }

        String alamat = Sequel.cariIsi(
            "SELECT CONCAT(pasien.alamat, ', ', kelurahan.nm_kel, ', ', kecamatan.nm_kec, ', ', kabupaten.nm_kab) " +
            "FROM pasien " +
            "JOIN kelurahan ON pasien.kd_kel = kelurahan.kd_kel " +
            "JOIN kecamatan ON pasien.kd_kec = kecamatan.kd_kec " +
            "JOIN kabupaten ON pasien.kd_kab = kabupaten.kd_kab " +
            "WHERE pasien.no_rkm_medis = ?", TNoRM.getText()
        );

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("kamar", kamar);
        param.put("namakamar", namakamar);
        param.put("alamat", alamat);
        param.put("tglRegistrasi", Sequel.cariIsi("Select tgl_registrasi from reg_periksa where no_rawat=?", TNoRw.getText()));

        String projectDir = System.getProperty("user.dir");
        String logoPath = projectDir + "/setting/logo2.png";
        param.put("logo2", logoPath);

        finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() +
                "\nDitandatangani secara elektronik oleh " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString() +
                "\nID " + (finger.equals("") ? tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString() : finger) +
                "\n" + Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()));

        // Debug log
        System.out.println("==== DEBUG CETAK ECHO ====");
        System.out.println("No. Rawat        : " + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        System.out.println("Nama Dokter      : " + tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        System.out.println("Tanggal Periksa  : " + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
        System.out.println("Kamar            : " + kamar);
        System.out.println("Nama Kamar       : " + namakamar);
        System.out.println("Alamat Pasien    : " + alamat);
        System.out.println("Logo Path        : " + logoPath);
        System.out.println("Finger Digital   : " + finger);
        System.out.println("Parameter Report : " + param.toString());

        String query = "SELECT ... WHERE hasil_pemeriksaan_echo.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'";
        System.out.println("Query Report     : " + query);
        System.out.println("Mulai mencetak report ECHO...");

        Valid.MyReportPDFqryUpload("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
            "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, pasien.umur, pasien.jk, " +
            "hasil_pemeriksaan_echo.tanggal, hasil_pemeriksaan_echo.kd_dokter, dokter.nm_dokter, " +
            "hasil_pemeriksaan_echo.AortaRootDiameter, hasil_pemeriksaan_echo.ValveCupsMotion, " +
            "hasil_pemeriksaan_echo.LeftAtriumDimension, hasil_pemeriksaan_echo.AtriumLAAoRatio, " +
            "hasil_pemeriksaan_echo.RightVentricleDimension, hasil_pemeriksaan_echo.LeftVentricleED, " +
            "hasil_pemeriksaan_echo.LeftVentricleES, hasil_pemeriksaan_echo.LeftVentricleEF, " +
            "hasil_pemeriksaan_echo.FractShorteningFS, hasil_pemeriksaan_echo.LVPWthickeningED, " +
            "hasil_pemeriksaan_echo.LVPWThickening, hasil_pemeriksaan_echo.LVPWmationPattern, " +
            "hasil_pemeriksaan_echo.IVSThickeningIVS, hasil_pemeriksaan_echo.IVSthickening, " +
            "hasil_pemeriksaan_echo.IVSmationPattern, hasil_pemeriksaan_echo.IVSRatio, " +
            "hasil_pemeriksaan_echo.AnteriorLeaflet, hasil_pemeriksaan_echo.DiastolicMotionTeks, " +
            "hasil_pemeriksaan_echo.DiastolicMotionCMbox, hasil_pemeriksaan_echo.SystolicMotion, " +
            "hasil_pemeriksaan_echo.PosteriorLeafletCMbox, hasil_pemeriksaan_echo.PosteriorLeafletTeks, " +
            "hasil_pemeriksaan_echo.TricuspidValve, hasil_pemeriksaan_echo.PulmonaryValve, " +
            "hasil_pemeriksaan_echo.PericardialEffusionCMBox, hasil_pemeriksaan_echo.PericardialEffusionTeks, " +
            "hasil_pemeriksaan_echo.ValvesAO, hasil_pemeriksaan_echo.ValvesMitral, hasil_pemeriksaan_echo.ChambersLAC, " +
            "hasil_pemeriksaan_echo.ChambersRVC, hasil_pemeriksaan_echo.ChambersRAC, hasil_pemeriksaan_echo.ChambersLVC, " +
            "hasil_pemeriksaan_echo.Myocardium, hasil_pemeriksaan_echo.PSLAXSegmental, hasil_pemeriksaan_echo.PSLAXDiffuse, " +
            "hasil_pemeriksaan_echo.PSLAXMass, hasil_pemeriksaan_echo.PSLAXPericardium, hasil_pemeriksaan_echo.PSSAXAorticValve, " +
            "hasil_pemeriksaan_echo.MitralValve, hasil_pemeriksaan_echo.LeftVentricle, hasil_pemeriksaan_echo.APACWallMotion, " +
            "hasil_pemeriksaan_echo.APACSegmental, hasil_pemeriksaan_echo.APACDiffuce, hasil_pemeriksaan_echo.APACMitraValve, " +
            "hasil_pemeriksaan_echo.APACTriscupsidValve, hasil_pemeriksaan_echo.Shunts, hasil_pemeriksaan_echo.ValveInsufficiency, " +
            "hasil_pemeriksaan_echo.Comments " +
            "FROM reg_periksa " +
            "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
            "INNER JOIN hasil_pemeriksaan_echo ON reg_periksa.no_rawat = hasil_pemeriksaan_echo.no_rawat " +
            "INNER JOIN dokter ON hasil_pemeriksaan_echo.kd_dokter = dokter.kd_dokter " +
            "WHERE hasil_pemeriksaan_echo.no_rawat = '"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",FileName, param  
        );
    } else {
        JOptionPane.showMessageDialog(null, "Silakan pilih data pemeriksaan terlebih dahulu.");
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
        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS RESUME%'");
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


//    try {
//        // Pastikan file PDF ada
//        File pdfFile = new File("tmpPDF/" + FileName + ".pdf");
//        if (!pdfFile.exists()) {
//            System.err.println("File PDF tidak ditemukan: " + pdfFile.getAbsolutePath());
//            return;
//        }
//
//        // Load PDF
//        PDDocument document = PDDocument.load(pdfFile);
//        PDFRenderer pdfRenderer = new PDFRenderer(document);
//
//        // Pastikan folder tmpJPG ada
//        File jpgDir = new File("tmpJPG");
//        if (!jpgDir.exists() && !jpgDir.mkdir()) {
//            System.err.println("Gagal membuat folder tmpJPG.");
//            document.close();
//            return;
//        }
//
//        // Render halaman pertama PDF ke gambar
//        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
//
//        // Simpan ke JPG
//        File jpgFile = new File(jpgDir, FileName + ".jpg");
//        ImageIO.write(image, "jpg", jpgFile);
//
//        // Tutup dokumen PDF
//        document.close();
//
//        System.out.println("Konversi berhasil: " + jpgFile.getAbsolutePath());
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Hasil ECHO%'");
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

//    try {
//        File file = new File("tmpJPG/" + FileName + ".jpg");
//        byte[] data = FileUtils.readFileToByteArray(file);
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/upload.php?doc=" + docpath);
//        ByteArrayBody fileData = new ByteArrayBody(data, FileName + ".jpg");
//        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//        reqEntity.addPart("file", fileData);
//        postRequest.setEntity(reqEntity);
//        httpClient.execute(postRequest);
//        
//        boolean uploadSuccess = false;
//        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Berkas Resume%'");
//        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".jpg'") > 0) {
//            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
//                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg", "pages/upload/" + FileName + ".jpg"
//            });
//        } else {
//            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
//                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg"
//            });
//        }
//        
//        if (uploadSuccess) {
//            JOptionPane.showMessageDialog(null, "Upload berhasil!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
//        } else {
//            JOptionPane.showMessageDialog(null, "Upload gagal disimpan ke database.", "Peringatan", JOptionPane.WARNING_MESSAGE);
//        }
//    } catch (Exception e) {
//        System.out.println("Upload error: " + e);
//        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat upload: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
//    }
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
    
private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {                                                        
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
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                        }else{
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2nonhapus.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
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
    }                                                       
    
}
