/*
  by Mas Elkhanza
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

/**
 *
 * @author dosen
 */
public final class SatuSehatKirimServiceRequestRadiologi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;   
    private int i=0;
    private String link="",json="",iddokter="",idpasien="";
    private ApiSatuSehat api=new ApiSatuSehat();
    private HttpHeaders headers ;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat=new SatuSehatCekNIK();  
    private StringBuilder htmlContent;    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;  
    private final Set<String> imagingStudyAutoGagal = new HashSet<>();
    private final Set<String> resolusiOrthancBerjalan = java.util.Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> routerDicomDiterima = Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> serviceRequestAcsnPerluPerbaikan = Collections.synchronizedSet(new HashSet<String>());
    private final Map<String,String> imagingStudySiapSinkron = Collections.synchronizedMap(new HashMap<String,String>());
    private boolean sedangAturPilihan = false;
    private static final String MODALITY_ROUTER_DICOM = "ROUTERDICOM";
    private static final String AET_ROUTER_DICOM = "DCMROUTER";
    private static final int PORT_DICOM_ROUTER = 11112;
    private static final int PORT_HTTP_ROUTER = 8080;
    private static final String PATH_HEALTH_ROUTER_DICOM = "/fhir/health";
    private static final int BATAS_TUNGGU_JOB_ROUTER_DETIK = 300;
    private static final int BATAS_TUNGGU_IMAGING_STUDY_DETIK = 120;
    private static final ZoneId ZONA_WAKTU_RS = ZoneId.of("Asia/Makassar");
    private static final DateTimeFormatter FORMAT_WAKTU_DATABASE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMAT_WAKTU_FHIR_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    private static final class TargetRouterDicom {
        private final String aet;
        private final String host;
        private final int port;

        private TargetRouterDicom(String aet, String host, int port) {
            this.aet=aet;
            this.host=host;
            this.port=port;
        }

        private String deskripsi() {
            return aet+"@"+host+":"+port;
        }
    }
    
    /** Creates new form DlgKamar
     * @param parent
     * @param modal */
    public SatuSehatKirimServiceRequestRadiologi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10,2);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new String[]{
                "P","No.Rawat","No.RM","Nama Pasien","No.KTP Pasien","Kode Dokter","Nama Dokter Perujuk",
                "No.KTP Dokter","ID Encounter","No.Permintaan","Tgl & Jam Permintaan","Diagnosa Klinis",
                "Nama Pemeriksaan","Radiologi Code","Radiologi System","Radiologi Display","ID Service Request",
                "Kode Pemeriksaan","ACSN","ID Imaging Study","Orthanc Study ID"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Boolean.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
                 java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
                 java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
                 java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
                 java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
                 java.lang.String.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbObat.setModel(tabMode);

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 21; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(105);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(150);
            }else if(i==4){
                column.setPreferredWidth(110);
            }else if(i==5){
                column.setPreferredWidth(80);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(110);
            }else if(i==8){
                column.setPreferredWidth(210);
            }else if(i==9){
                column.setPreferredWidth(110);
            }else if(i==10){
                column.setPreferredWidth(120);
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
                column.setPreferredWidth(210);
            }else if(i==17){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==18){
                column.setPreferredWidth(150);
            }else if(i==19){
                column.setPreferredWidth(210);
            }else if(i==20){
                column.setPreferredWidth(210);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        tbObat.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbObat.getSelectionModel().addListSelectionListener(evt -> {
            if(!evt.getValueIsAdjusting()){
                pilihBarisOtomatis(tbObat.getSelectedRow());
            }
        });
        tbObat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                int baris=tbObat.rowAtPoint(evt.getPoint());
                if(baris>=0&&SwingUtilities.isLeftMouseButton(evt)){
                    SwingUtilities.invokeLater(() -> pilihBarisOtomatis(baris));
                }
            }
        });
        tabMode.addTableModelListener(evt -> {
            if(evt.getColumn()==0&&!sedangAturPilihan){
                SwingUtilities.invokeLater(() -> aturTombolAlur());
            }
        });
        aturTombolAlur();
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        
        try {
            link=koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }  
        
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
        ppPilihSemua = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim1 = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnAll = new widget.Button();
        BtnKirim = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnKirimRouterDicom = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel15 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel17 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel18 = new widget.Label();
        CmbStatus = new widget.ComboBox();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnGetIDImagingStudiAuto = new widget.Button();
        BtnGetIDImagingStudiManual = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N
        jPopupMenu1.setPreferredSize(new java.awt.Dimension(222, 116));

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppPilihBelumTerkirim.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim.setText("Pilih Service Req Radiologi Belum Terkirim");
        ppPilihBelumTerkirim.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihBelumTerkirim.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihBelumTerkirim.setName("ppPilihBelumTerkirim"); // NOI18N
        ppPilihBelumTerkirim.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihBelumTerkirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihBelumTerkirimActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihBelumTerkirim);

        ppPilihBelumTerkirim1.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim1.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim1.setText("Pilih ID Imaging Study Belum Ada");
        ppPilihBelumTerkirim1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihBelumTerkirim1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihBelumTerkirim1.setName("ppPilihBelumTerkirim1"); // NOI18N
        ppPilihBelumTerkirim1.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihBelumTerkirim1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihBelumTerkirim1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihBelumTerkirim1);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(150, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Data Service Request Radiologi Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 130));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(53, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass8.add(LCount);

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

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('K');
        BtnKirim.setText("Kirim ServiceRequest");
        BtnKirim.setToolTipText("Kirim ServiceRequest ke SATUSEHAT");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(145, 30));
        BtnKirim.setVisible(false);
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirim);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png"))); // NOI18N
        BtnUpdate.setMnemonic('U');
        BtnUpdate.setText("Perbaiki ServiceRequest");
        BtnUpdate.setToolTipText("Perbaiki ServiceRequest yang sudah terkirim");
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(165, 30));
        BtnUpdate.setVisible(false);
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnUpdate);

        BtnKirimRouterDicom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirimRouterDicom.setMnemonic('R');
        BtnKirimRouterDicom.setText("Kirim ke Router DICOM");
        BtnKirimRouterDicom.setToolTipText("Kirim study Orthanc ke modality ROUTERDICOM");
        BtnKirimRouterDicom.setName("BtnKirimRouterDicom"); // NOI18N
        BtnKirimRouterDicom.setPreferredSize(new java.awt.Dimension(195, 30));
        BtnKirimRouterDicom.setVisible(false);
        BtnKirimRouterDicom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimRouterDicomActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirimRouterDicom);

        BtnGetIDImagingStudiAuto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/36.png"))); // NOI18N
        BtnGetIDImagingStudiAuto.setMnemonic('I');
        BtnGetIDImagingStudiAuto.setText("Sinkronkan ImagingStudy");
        BtnGetIDImagingStudiAuto.setToolTipText("Ambil otomatis ID ImagingStudy dari SATUSEHAT");
        BtnGetIDImagingStudiAuto.setName("BtnGetIDImagingStudiAuto"); // NOI18N
        BtnGetIDImagingStudiAuto.setPreferredSize(new java.awt.Dimension(175, 30));
        BtnGetIDImagingStudiAuto.setVisible(false);
        BtnGetIDImagingStudiAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGetIDImagingStudiAutoActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnGetIDImagingStudiAuto);

        BtnGetIDImagingStudiManual.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/36.png"))); // NOI18N
        BtnGetIDImagingStudiManual.setText("Sinkron Manual");
        BtnGetIDImagingStudiManual.setToolTipText("Gunakan jika sinkronisasi otomatis gagal");
        BtnGetIDImagingStudiManual.setName("BtnGetIDImagingStudiManual"); // NOI18N
        BtnGetIDImagingStudiManual.setPreferredSize(new java.awt.Dimension(135, 30));
        BtnGetIDImagingStudiManual.setVisible(false);
        BtnGetIDImagingStudiManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGetIDImagingStudiManualActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnGetIDImagingStudiManual);

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
        panelGlass8.add(BtnPrint);

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
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 76));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel15.setText("Tgl.Registrasi :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(jLabel15);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "16-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("s.d.");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel17);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "16-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel18.setText("Status :");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(jLabel18);

        CmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Sudah Terkirim", "Belum Terkirim" }));
        CmbStatus.setName("CmbStatus"); // NOI18N
        CmbStatus.setPreferredSize(new java.awt.Dimension(125, 23));
        CmbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CmbStatusActionPerformed(evt);
            }
        });
        panelGlass9.add(CmbStatus);

        jLabel16.setText("Key Word :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(150, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
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

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,BtnKeluar);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            htmlContent = new StringBuilder();
            htmlContent.append(                             
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter Perujuk</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Permintaan</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tgl & Jam Permintaan</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Diagnosa Klinis</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pemeriksaan</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi Code</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi System</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi Display</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Service Request</b></td>"+
                "</tr>"
            );
            for (i = 0; i < tabMode.getRowCount(); i++) {
                htmlContent.append(
                    "<tr class='isi'>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,16).toString()+"</td>"+
                    "</tr>");
            }
            LoadHTML.setText(
                "<html>"+
                  "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                   htmlContent.toString()+
                  "</table>"+
                "</html>"
            );
            htmlContent=null;

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

            File f = new File("DataSatuSehatServiceRequestRadiologi.html");            
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
            bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                        "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            "<tr class='isi2'>"+
                                "<td valign='top' align='center'>"+
                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                    "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT SERVICE REQUEST RADIOLOGI<br><br></font>"+        
                                "</td>"+
                           "</tr>"+
                        "</table>")
            );
            bw.close();                         
            Desktop.getDesktop().browse(f.toURI());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());       
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() ->tampil());
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,4).toString().equals(""))&&(!tbObat.getValueAt(i,7).toString().equals(""))&&tbObat.getValueAt(i,16).toString().equals("")){
                try{
                    int barisModel=tbObat.convertRowIndexToModel(i);
                    String acsn=nilaiTabel(barisModel,9);
                    if(acsn.isEmpty()){
                        throw new IllegalStateException("No.Permintaan radiologi belum tersedia");
                    }
                    // ServiceRequest harus diterbitkan lebih dahulu menggunakan
                    // ACSN=noorder, tanpa menunggu citra tersedia di Orthanc.
                    tabMode.setValueAt(acsn,barisModel,18);

                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Authorization", "Bearer "+api.TokenSatuSehat());

                    String idSudahAda=cariServiceRequestID(barisModel,headers);
                    if(!idSudahAda.isEmpty()){
                        simpanServiceRequestLokal(barisModel,idSudahAda);
                        System.out.println("ServiceRequest dengan ACSN "+acsn+" sudah tersedia. ID lokal disinkronkan : "+idSudahAda);
                        JOptionPane.showMessageDialog(this,
                                "ServiceRequest dengan ACSN "+acsn+" sudah ada di SATUSEHAT.\n"+
                                "ID ServiceRequest berhasil disinkronkan ke data lokal.",
                                "Informasi",JOptionPane.INFORMATION_MESSAGE);
                        continue;
                    }

                    String acsnLama=nilaiTabel(barisModel,9)+"."+nilaiTabel(barisModel,17);
                    String idFormatLama=cariServiceRequestID(barisModel,acsnLama,headers);
                    if(!idFormatLama.isEmpty()){
                        System.out.println("ServiceRequest format lama ditemukan: "+idFormatLama+
                                ". Memperbarui ACSN "+acsnLama+" -> "+acsn);
                        perbaruiIdentifierAcsnServiceRequest(
                                idFormatLama,acsnLama,acsn,headers);
                        String idVerifikasi=cariServiceRequestID(
                                barisModel,acsn,headers);
                        if(!idFormatLama.equals(idVerifikasi)){
                            throw new IllegalStateException(
                                    "Migrasi ACSN ServiceRequest belum dapat diverifikasi untuk "+acsn);
                        }
                        simpanServiceRequestLokal(barisModel,idFormatLama);
                        System.out.println("Migrasi ACSN ServiceRequest berhasil: "+acsnLama+
                                " -> "+acsn+" | ID "+idFormatLama);
                        JOptionPane.showMessageDialog(this,
                                "ServiceRequest lama berhasil ditemukan dan diperbarui.\n"+
                                "ACSN lama : "+acsnLama+"\n"+
                                "ACSN baru : "+acsn+"\n"+
                                "ID ServiceRequest tetap : "+idFormatLama,
                                "Migrasi ACSN",JOptionPane.INFORMATION_MESSAGE);
                        continue;
                    }

                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,7).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,4).toString());
                    if(iddokter==null||iddokter.trim().isEmpty()){
                        throw new IllegalStateException("ID SATUSEHAT dokter belum ditemukan");
                    }
                    if(idpasien==null||idpasien.trim().isEmpty()){
                        throw new IllegalStateException("ID SATUSEHAT pasien belum ditemukan");
                    }

                    json=buatPayloadServiceRequest(barisModel,"",iddokter,idpasien);
                    System.out.println("URL : "+link+"/ServiceRequest");
                    System.out.println("Request JSON : "+json);
                    requestEntity = new HttpEntity(json,headers);
                    json=api.getRest().exchange(link+"/ServiceRequest", HttpMethod.POST, requestEntity, String.class).getBody();
                    System.out.println("Result JSON : "+json);
                    root = mapper.readTree(json);
                    response = root.path("id");
                    if(response.asText().isEmpty()){
                        throw new IllegalStateException("Respons SATUSEHAT tidak berisi ID ServiceRequest");
                    }
                    simpanServiceRequestLokal(barisModel,response.asText());
                }catch(HttpStatusCodeException e){
                    tampilkanErrorSatuSehat("Kirim ServiceRequest",e);
                }catch(Exception e){
                    tampilkanErrorProses("Kirim ServiceRequest",e);
                }
            }
        }
        aturTombolAlur();
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        sedangAturPilihan=true;
        try{
            for(i=0;i<tbObat.getRowCount();i++){
                tbObat.setValueAt(true,i,0);
            }
        }finally{
            sedangAturPilihan=false;
        }
        aturTombolAlur();
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        sedangAturPilihan=true;
        try{
            for(i=0;i<tbObat.getRowCount();i++){
                tbObat.setValueAt(false,i,0);
            }
        }finally{
            sedangAturPilihan=false;
        }
        tbObat.clearSelection();
        aturTombolAlur();
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,4).toString().equals(""))&&(!tbObat.getValueAt(i,7).toString().equals(""))&&(!tbObat.getValueAt(i,16).toString().equals(""))){
                try{
                    int barisModel=tbObat.convertRowIndexToModel(i);
                    String noPermintaan=nilaiTabel(barisModel,9);
                    if(noPermintaan.isEmpty()){
                        throw new IllegalStateException("No.Permintaan radiologi belum tersedia");
                    }
                    // Perbaikan ServiceRequest tidak bergantung pada keberadaan
                    // study Orthanc. ACSN sumber kebenaran tetap noorder.
                    tabMode.setValueAt(noPermintaan,barisModel,18);
                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,7).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,4).toString());
                    if(iddokter==null||iddokter.trim().isEmpty()){
                        throw new IllegalStateException("ID SATUSEHAT dokter belum ditemukan");
                    }
                    if(idpasien==null||idpasien.trim().isEmpty()){
                        throw new IllegalStateException("ID SATUSEHAT pasien belum ditemukan");
                    }

                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                    String idServiceRequest=nilaiTabel(barisModel,16);
                    json=buatPayloadUpdateServiceRequest(
                            barisModel,idServiceRequest,iddokter,idpasien,headers);
                    System.out.println("URL : "+link+"/ServiceRequest/"+idServiceRequest);
                    System.out.println("Request JSON : "+json);
                    requestEntity = new HttpEntity(json,headers);
                    json=api.getRest().exchange(link+"/ServiceRequest/"+idServiceRequest, HttpMethod.PUT, requestEntity, String.class).getBody();
                    System.out.println("Result JSON : "+json);
                    JsonNode hasilUpdate=mapper.readTree(json);
                    if(!idServiceRequest.equals(hasilUpdate.path("id").asText())||
                            !serviceRequestMemilikiIdentifierAcsn(
                                    hasilUpdate,systemIdentifierAcsn(),noPermintaan)){
                        throw new IllegalStateException(
                                "Respons update belum mengonfirmasi ID dan ACSN ServiceRequest");
                    }
                    serviceRequestAcsnPerluPerbaikan.remove(kunciServiceRequestAcsn(barisModel));
                    System.out.println("[ROUTER-DICOM] ["+nilaiTabel(barisModel,9)+
                            "] ServiceRequest berhasil diperbaiki untuk ACSN "+nilaiTabel(barisModel,18));
                    tabMode.setValueAt(false,barisModel,0);
                }catch(HttpStatusCodeException e){
                    tampilkanErrorSatuSehat("Perbaiki ServiceRequest",e);
                }catch(Exception e){
                    tampilkanErrorProses("Perbaiki ServiceRequest",e);
                }
            }
        }
        aturTombolAlur();
    }//GEN-LAST:event_BtnUpdateActionPerformed

    private void BtnKirimRouterDicomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimRouterDicomActionPerformed
        final List<Integer> barisTerpilih=new ArrayList<Integer>();
        StringBuilder rincian=new StringBuilder();
        int tahapAksi=0;
        for(int baris=0;baris<tabMode.getRowCount();baris++){
            int tahap=tahapAlur(baris);
            if(Boolean.TRUE.equals(tabMode.getValueAt(baris,0))&&(tahap==3||tahap==6)){
                if(tahapAksi==0){
                    tahapAksi=tahap;
                }else if(tahapAksi!=tahap){
                    JOptionPane.showMessageDialog(this,
                            "Pilihan memiliki tahapan Router DICOM yang berbeda.",
                            "Router DICOM",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                barisTerpilih.add(baris);
                if(barisTerpilih.size()<=5){
                    rincian.append("\n• ").append(nilaiTabel(baris,3))
                            .append(" | No.Permintaan ").append(nilaiTabel(baris,9))
                            .append("\n  ACSN ").append(nilaiTabel(baris,18))
                            .append(" | Study ID ").append(nilaiTabel(baris,20));
                }
            }
        }
        if(barisTerpilih.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Tidak ada data pada tahap Router DICOM.",
                    "Router DICOM",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(tahapAksi==6){
            if(!runBackground(() -> prosesCekStatusRouterDicom(barisTerpilih))){
                JOptionPane.showMessageDialog(this,
                        "Masih ada proses lain yang berjalan. Silakan tunggu sampai selesai.",
                        "Router DICOM",JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        if(barisTerpilih.size()>5){
            rincian.append("\n• ... dan ").append(barisTerpilih.size()-5)
                    .append(" data lainnya");
        }

        int pilihan=JOptionPane.showConfirmDialog(this,
                "Pastikan dicom-router.exe sedang berjalan.\n"+
                "Study berikut akan dikirim Orthanc ke modality "+MODALITY_ROUTER_DICOM+":"+
                rincian.toString()+"\n\nLanjutkan pengiriman?",
                "Konfirmasi Kirim ke Router DICOM",
                JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(pilihan!=JOptionPane.YES_OPTION){
            return;
        }

        if(!runBackground(() -> prosesKirimRouterDicom(barisTerpilih))){
            JOptionPane.showMessageDialog(this,
                    "Masih ada proses lain yang berjalan. Silakan tunggu sampai selesai.",
                    "Router DICOM",JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_BtnKirimRouterDicomActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        if(CmbStatus.getSelectedIndex()!=0){
            CmbStatus.setSelectedIndex(0);
        }else{
            runBackground(() ->tampil());
        }
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            if(CmbStatus.getSelectedIndex()!=0){
                CmbStatus.setSelectedIndex(0);
            }else{
                runBackground(() ->tampil());
            }
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        } 
    }//GEN-LAST:event_formWindowOpened

    private void BtnGetIDImagingStudiAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiAutoActionPerformed
        int belumDitemukan=0;
        StringBuilder daftarBelumDitemukan=new StringBuilder();
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!Boolean.parseBoolean(tbObat.getValueAt(i, 0).toString())) continue;

            String noorder = tbObat.getValueAt(i, 9).toString();        
            String kdJenisPrw = tbObat.getValueAt(i, 17).toString();   
            String idServicerequest = tbObat.getValueAt(i, 16).toString(); 
            String kunci=kunciImagingStudy(i);

            String acsn = tbObat.getValueAt(i, 18)==null?"":tbObat.getValueAt(i, 18).toString().trim();
            if (acsn.isEmpty()) {
                imagingStudyAutoGagal.add(kunci);
                System.out.println("ACSN belum tersedia di radiologi_pacs_token untuk noorder " + noorder);
                continue;
            }
            System.out.println("Auto ACSN untuk baris " + i + " : " + acsn);

            String kunciRouter=kunciRouterDicom(i);
            String imagingId = imagingStudySiapSinkron.get(kunciRouter);
            if(imagingId==null||imagingId.isEmpty()){
                imagingId = getImagingStudyID(acsn);
            }
            if (imagingId != null && !imagingId.isEmpty()) {
                tbObat.setValueAt(acsn, i, 18);
                tbObat.setValueAt(imagingId, i, 19);
                tbObat.setValueAt(false, i, 0); 

                simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, acsn, imagingId);
                imagingStudyAutoGagal.remove(kunci);
                imagingStudySiapSinkron.remove(kunciRouter);
                routerDicomDiterima.remove(kunciRouter);
                System.out.println("Berhasil mendapatkan ID Imaging Study untuk noorder " + noorder + " : " + imagingId);
            } else {
                imagingStudyAutoGagal.add(kunci);
                belumDitemukan++;
                if(daftarBelumDitemukan.length()>0){
                    daftarBelumDitemukan.append(", ");
                }
                daftarBelumDitemukan.append(noorder);
                System.out.println("Gagal mendapatkan ID Imaging Study untuk ACSN " + acsn);
            }
        }
        if(belumDitemukan>0){
            JOptionPane.showMessageDialog(this,
                    "ImagingStudy belum ditemukan untuk "+daftarBelumDitemukan+".\n"+
                    "Pastikan proses pada console dicom-router.exe sudah selesai, "+
                    "kemudian coba sinkronkan kembali.",
                    "Sinkronkan ImagingStudy",JOptionPane.WARNING_MESSAGE);
        }
        aturTombolAlur();
    }//GEN-LAST:event_BtnGetIDImagingStudiAutoActionPerformed

    private void BtnGetIDImagingStudiManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiManualActionPerformed
         for (int i = 0; i < tbObat.getRowCount(); i++) {
            if(tbObat.getValueAt(i,0).toString().equals("true")){
                if (!Boolean.parseBoolean(tbObat.getValueAt(i, 0).toString())) continue;

                String noorder = tbObat.getValueAt(i, 9).toString();
                String kdJenisPrw = tbObat.getValueAt(i, 17).toString();
                String idServicerequest = tbObat.getValueAt(i, 16).toString();

                String acsn = javax.swing.JOptionPane.showInputDialog(this, 
                        "Masukkan ACSN untuk No.Order " + noorder + " :", 
                        "Manual ACSN", javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (acsn == null || acsn.trim().isEmpty()) {
                    System.out.println("Input ACSN dibatalkan untuk baris " + i);
                    continue;
                }
                acsn = acsn.trim();

                System.out.println("Manual ACSN untuk baris " + i + " : " + acsn);
                String imagingId = getImagingStudyID(acsn);
                if (imagingId != null && !imagingId.isEmpty()) {
                    tbObat.setValueAt(acsn, i, 18);
                    tbObat.setValueAt(imagingId, i, 19);
                    tbObat.setValueAt(false, i, 0);
                    simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, acsn, imagingId);
                    imagingStudyAutoGagal.remove(kunciImagingStudy(i));
                    imagingStudySiapSinkron.remove(kunciRouterDicom(i));
                    routerDicomDiterima.remove(kunciRouterDicom(i));
                    System.out.println("Berhasil mendapatkan ID Imaging Study untuk noorder " + noorder + " : " + imagingId);
                } else {
                    System.out.println("Gagal mendapatkan ID Imaging Study untuk ACSN " + acsn);
                }
            }
        }
        aturTombolAlur();
    }//GEN-LAST:event_BtnGetIDImagingStudiManualActionPerformed

    private void ppPilihBelumTerkirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirimActionPerformed
        sedangAturPilihan=true;
        try{
            for(int i = 0; i < tbObat.getRowCount(); i++) {
                tbObat.setValueAt(
                    (tbObat.getValueAt(i, 16) == null ||
                        tbObat.getValueAt(i, 16).toString().trim().equals("") ||
                        tbObat.getValueAt(i, 16).toString().equals("-")),
                    i, 0
                );
            }
        }finally{
            sedangAturPilihan=false;
        }
        aturTombolAlur();
    }//GEN-LAST:event_ppPilihBelumTerkirimActionPerformed

    private void ppPilihBelumTerkirim1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirim1ActionPerformed
        sedangAturPilihan=true;
        try{
            for(int i = 0; i < tbObat.getRowCount(); i++) {
                tbObat.setValueAt(
                    (tbObat.getValueAt(i, 19) == null ||
                        tbObat.getValueAt(i, 19).toString().trim().equals("") ||
                        tbObat.getValueAt(i, 19).toString().equals("-")),
                    i, 0
                );
            }
        }finally{
            sedangAturPilihan=false;
        }
        aturTombolAlur();
    }//GEN-LAST:event_ppPilihBelumTerkirim1ActionPerformed

    private void CmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CmbStatusActionPerformed
        runBackground(() ->tampil());
    }//GEN-LAST:event_CmbStatusActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatKirimServiceRequestRadiologi dialog = new SatuSehatKirimServiceRequestRadiologi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnGetIDImagingStudiAuto;
    private widget.Button BtnGetIDImagingStudiManual;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnKirimRouterDicom;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
    private widget.ComboBox CmbStatus;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppPilihBelumTerkirim;
    private javax.swing.JMenuItem ppPilihBelumTerkirim1;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void pilihBarisOtomatis(int baris) {
        if(baris<0||baris>=tbObat.getRowCount()||sedangAturPilihan){
            return;
        }
        int barisModel=tbObat.convertRowIndexToModel(baris);
        sedangAturPilihan=true;
        try{
            for(int index=0;index<tabMode.getRowCount();index++){
                boolean dipilih=index==barisModel;
                if(!Boolean.valueOf(dipilih).equals(tabMode.getValueAt(index,0))){
                    tabMode.setValueAt(dipilih,index,0);
                }
            }
        }finally{
            sedangAturPilihan=false;
        }
        aturTombolAlur();
        lengkapiDataOrthancOtomatis(barisModel);
    }

    private String nilaiTabel(int baris, int kolom) {
        Object nilai=tabMode.getValueAt(baris,kolom);
        return nilai==null?"":nilai.toString().trim();
    }

    private void lengkapiDataOrthancOtomatis(final int baris) {
        if(baris<0||baris>=tabMode.getRowCount()){
            return;
        }

        final String kunci=kunciImagingStudy(baris);
        if(!resolusiOrthancBerjalan.add(kunci)){
            return;
        }
        boolean dijalankan=runBackground(() -> {
            try{
                String[] dataOrthanc=pastikanDataOrthanc(baris);
                System.out.println("Data Orthanc otomatis ditemukan untuk No.Permintaan "+
                        nilaiTabel(baris,9)+" : ACSN "+dataOrthanc[0]+
                        ", Study ID "+dataOrthanc[1]);
                if(!nilaiTabel(baris,16).isEmpty()&&
                        !nilaiTabel(baris,16).equals("-")&&
                        (nilaiTabel(baris,19).isEmpty()||nilaiTabel(baris,19).equals("-"))){
                    String imagingId=getImagingStudyID(dataOrthanc[0],false);
                    if(!imagingId.isEmpty()){
                        String kunciRouter=kunciRouterDicom(baris);
                        imagingStudySiapSinkron.put(kunciRouter,imagingId);
                        routerDicomDiterima.remove(kunciRouter);
                        System.out.println("[ROUTER-DICOM] ["+nilaiTabel(baris,9)+
                                "] ImagingStudy sudah tersedia: "+imagingId+
                                ". Tahap diarahkan ke Sinkronkan ImagingStudy");
                    }
                }
            }catch(Exception e){
                System.out.println("Notifikasi Resolusi Orthanc Otomatis : "+e);
            }finally{
                resolusiOrthancBerjalan.remove(kunci);
                SwingUtilities.invokeLater(() -> aturTombolAlur());
            }
        });
        if(!dijalankan){
            resolusiOrthancBerjalan.remove(kunci);
        }
    }

    /**
     * Memastikan ACSN berasal dari study Orthanc yang cocok dengan Patient ID,
     * tanggal pemeriksaan, dan jam pemeriksaan. ACSN aktif wajib sama dengan
     * noorder; nilai kosong maupun format lama otomatis dinormalisasi.
     *
     * @return accession_number, orthanc_study_id.
     */
    private String[] pastikanDataOrthanc(int baris) throws Exception {
        String noRawat=nilaiTabel(baris,1);
        String patientId=nilaiTabel(baris,2);
        String noOrder=nilaiTabel(baris,9);
        String kdJenisPrw=nilaiTabel(baris,17);
        if(noRawat.isEmpty()||patientId.isEmpty()||noOrder.isEmpty()||kdJenisPrw.isEmpty()){
            throw new IllegalStateException("Data pasien atau permintaan radiologi belum lengkap untuk pencarian Orthanc");
        }

        String[] waktuPemeriksaan=cariWaktuPemeriksaanOrthanc(baris);
        String tglPeriksa=waktuPemeriksaan[0];
        String jamPeriksa=waktuPemeriksaan[1];
        String studyDate=tglPeriksa.replace("-","");
        String acsnCache=nilaiTabel(baris,18);
        String studyIdCache=nilaiTabel(baris,20);

        if(!acsnCache.isEmpty()&&!studyIdCache.isEmpty()&&acsnCache.equals(noOrder)){
            try{
                JsonNode studyCache=ambilStudyOrthanc(studyIdCache);
                if(studyOrthancSesuai(studyCache,patientId,studyDate,noOrder)){
                    perbaruiDataOrthancTabel(baris,noOrder,studyIdCache);
                    return new String[]{noOrder,studyIdCache};
                }
                System.out.println("Cache Orthanc tidak lagi sesuai, dilakukan pencarian ulang untuk "+noOrder);
            }catch(Exception e){
                System.out.println("Study ID Orthanc dari cache tidak tersedia, dilakukan pencarian ulang : "+e);
            }
        }else if(!acsnCache.isEmpty()&&!acsnCache.equals(noOrder)){
            System.out.println("Cache ACSN masih menggunakan format lama "+acsnCache+
                    ". Study Orthanc akan dinormalisasi menjadi noorder "+noOrder);
        }

        JsonNode studies=cariStudyOrthanc("",patientId,studyDate);
        if(jumlahDataOrthanc(studies)==0){
            throw new IllegalStateException("Study Orthanc tidak ditemukan untuk Patient ID "+patientId+
                    ", tanggal "+tglPeriksa+" dan jam "+jamPeriksa);
        }

        JsonNode study=pilihStudyOrthancMenurutWaktu(studies,jamPeriksa);
        if(study==null){
            throw new IllegalStateException("Study Orthanc yang sesuai tidak dapat ditentukan untuk Patient ID "+patientId);
        }

        String accessionNumber=tagStudyOrthanc(study,"AccessionNumber");
        if(!accessionNumber.equals(noOrder)){
            // ACSN aktif wajib sama persis dengan noorder. Format lama
            // noorder.kd_jenis_prw otomatis dimigrasikan menjadi noorder.
            String accessionSebelumnya=accessionNumber;
            JsonNode studyDenganNoOrder=cariStudyOrthanc(noOrder,patientId,studyDate);
            if(jumlahDataOrthanc(studyDenganNoOrder)>0){
                JsonNode studyTersedia=pilihStudyOrthancMenurutWaktu(
                        studyDenganNoOrder,jamPeriksa);
                if(!studyOrthancSesuai(studyTersedia,patientId,studyDate,noOrder)){
                    throw new IllegalStateException("Study Orthanc dengan ACSN "+noOrder+
                            " ditemukan tetapi datanya tidak sesuai pemeriksaan");
                }
                study=studyTersedia;
                accessionNumber=noOrder;
                System.out.println("ACSN "+noOrder+
                        " sudah tersedia pada study Orthanc yang sesuai, resource tersebut digunakan");
            }else{
                JsonNode pemilikAcsnLain=cariStudyOrthanc(noOrder,"","");
                if(jumlahDataOrthanc(pemilikAcsnLain)>0){
                    throw new IllegalStateException("ACSN "+noOrder+
                            " sudah digunakan oleh study Orthanc lain. AccessionNumber tidak diterbitkan ulang");
                }

                String studyIdLama=study.path("ID").asText();
                if(accessionSebelumnya.isEmpty()){
                    System.out.println("AccessionNumber Orthanc masih kosong. Menerbitkan ACSN "+noOrder+
                            " pada Study ID "+studyIdLama);
                }else{
                    System.out.println("AccessionNumber Orthanc masih format lama "+accessionSebelumnya+
                            ". Mengubah menjadi noorder "+noOrder+" pada Study ID "+studyIdLama);
                }
                JsonNode hasilModifikasi=terbitkanAccessionOrthanc(studyIdLama,noOrder);

                JsonNode studyHasil=null;
                String studyIdBaru=hasilModifikasi.path("ID").asText();
                if(!studyIdBaru.isEmpty()){
                    try{
                        studyHasil=ambilStudyOrthanc(studyIdBaru);
                    }catch(Exception e){
                        System.out.println("Study hasil modify belum dapat dibaca lewat ID, dilakukan pencarian ulang : "+e);
                    }
                }
                if(!studyOrthancSesuai(studyHasil,patientId,studyDate,noOrder)){
                    JsonNode hasilPencarian=cariStudyOrthanc(noOrder,patientId,studyDate);
                    if(jumlahDataOrthanc(hasilPencarian)==0){
                        throw new IllegalStateException("ACSN "+noOrder+
                                " sudah dikirim ke Orthanc tetapi study hasil modifikasi belum dapat diverifikasi");
                    }
                    studyHasil=pilihStudyOrthancMenurutWaktu(hasilPencarian,jamPeriksa);
                }
                if(!studyOrthancSesuai(studyHasil,patientId,studyDate,noOrder)){
                    throw new IllegalStateException("Verifikasi ACSN hasil modifikasi Orthanc gagal untuk "+noOrder);
                }
                study=studyHasil;
                accessionNumber=noOrder;
                if(accessionSebelumnya.isEmpty()){
                    System.out.println("ACSN Orthanc berhasil diterbitkan dan diverifikasi : "+accessionNumber);
                }else{
                    System.out.println("Migrasi ACSN Orthanc berhasil: "+accessionSebelumnya+
                            " -> "+accessionNumber);
                }
            }
        }
        if(!accessionNumber.equals(noOrder)){
            throw new IllegalStateException("ACSN Orthanc wajib sama dengan noorder "+noOrder+
                    ", tetapi yang terbaca "+accessionNumber);
        }

        String orthancStudyId=study.path("ID").asText();
        if(orthancStudyId.isEmpty()){
            throw new IllegalStateException("Orthanc Study ID tidak tersedia");
        }

        try{
            simpanCacheOrthanc(noRawat,tglPeriksa,jamPeriksa,noOrder,kdJenisPrw,
                    accessionNumber,orthancStudyId);
        }catch(Exception e){
            // radiologi_pacs_token hanya cache. Kegagalan cache tidak boleh
            // menggagalkan ACSN yang sudah terverifikasi langsung di Orthanc.
            System.out.println("Notifikasi Simpan Cache Orthanc : "+e);
        }
        perbaruiDataOrthancTabel(baris,accessionNumber,orthancStudyId);
        return new String[]{accessionNumber,orthancStudyId};
    }

    private String[] cariWaktuPemeriksaanOrthanc(int baris) throws Exception {
        String noOrder=nilaiTabel(baris,9);
        try{
            try(PreparedStatement pst=koneksi.prepareStatement(
                    "select tgl_hasil,jam_hasil from permintaan_radiologi where noorder=? limit 1")){
                pst.setString(1,noOrder);
                try(ResultSet rst=pst.executeQuery()){
                    if(rst.next()){
                        String tanggal=teksAman(rst.getString("tgl_hasil"));
                        String jam=teksAman(rst.getString("jam_hasil"));
                        if(tanggalPemeriksaanValid(tanggal)&&!jam.isEmpty()){
                            return new String[]{tanggal,jam};
                        }
                    }
                }
            }
        }catch(Exception e){
            // Beberapa data lama menyimpan zero-date yang dapat ditolak oleh
            // konfigurasi JDBC. Lanjutkan memakai waktu periksa_radiologi.
            System.out.println("Waktu hasil permintaan tidak dapat dibaca, gunakan data pemeriksaan : "+e);
        }

        String noRawat=nilaiTabel(baris,1);
        String kdJenisPrw=nilaiTabel(baris,17);
        LocalDateTime waktuPermintaan=null;
        try{
            waktuPermintaan=LocalDateTime.parse(nilaiTabel(baris,10),FORMAT_WAKTU_DATABASE);
        }catch(Exception e){
            System.out.println("Waktu permintaan tidak dapat dijadikan acuan pencarian pemeriksaan : "+e);
        }

        String tanggalTerbaik="";
        String jamTerbaik="";
        long selisihTerbaik=Long.MAX_VALUE;
        try(PreparedStatement pst=koneksi.prepareStatement(
                "select tgl_periksa,jam from periksa_radiologi where no_rawat=? and kd_jenis_prw=? "+
                "order by tgl_periksa desc,jam desc")){
            pst.setString(1,noRawat);
            pst.setString(2,kdJenisPrw);
            try(ResultSet rst=pst.executeQuery()){
                while(rst.next()){
                    String tanggal=teksAman(rst.getString("tgl_periksa"));
                    String jam=teksAman(rst.getString("jam"));
                    if(!tanggalPemeriksaanValid(tanggal)||jam.isEmpty()){
                        continue;
                    }
                    long selisih=0;
                    if(waktuPermintaan!=null){
                        try{
                            LocalDateTime waktuKandidat=LocalDateTime.parse(
                                    tanggal+" "+jam,FORMAT_WAKTU_DATABASE);
                            selisih=Math.abs(java.time.Duration.between(
                                    waktuPermintaan,waktuKandidat).getSeconds());
                        }catch(Exception e){
                            selisih=Long.MAX_VALUE-1;
                        }
                    }
                    if(tanggalTerbaik.isEmpty()||selisih<selisihTerbaik){
                        tanggalTerbaik=tanggal;
                        jamTerbaik=jam;
                        selisihTerbaik=selisih;
                    }
                }
            }
        }
        if(tanggalTerbaik.isEmpty()||jamTerbaik.isEmpty()){
            throw new IllegalStateException("Tanggal dan jam pemeriksaan radiologi belum tersedia untuk "+noOrder);
        }
        return new String[]{tanggalTerbaik,jamTerbaik};
    }

    private boolean tanggalPemeriksaanValid(String tanggal) {
        return tanggal!=null&&tanggal.matches("\\d{4}-\\d{2}-\\d{2}")&&!tanggal.startsWith("0000-");
    }

    private JsonNode cariStudyOrthanc(
            String accessionNumber, String patientId, String studyDate) throws Exception {
        com.fasterxml.jackson.databind.node.ObjectNode query=mapper.createObjectNode();
        if(!teksAman(accessionNumber).isEmpty()){
            query.put("AccessionNumber",accessionNumber.trim());
        }
        if(!teksAman(patientId).isEmpty()){
            query.put("PatientID",patientId.trim());
        }
        if(!teksAman(studyDate).isEmpty()){
            query.put("StudyDate",studyDate.trim());
        }

        com.fasterxml.jackson.databind.node.ObjectNode body=mapper.createObjectNode();
        body.put("Level","Study");
        body.set("Query",query);
        body.put("Expand",true);
        return requestJsonOrthanc("/tools/find",HttpMethod.POST,body);
    }

    private JsonNode ambilStudyOrthanc(String orthancStudyId) throws Exception {
        validasiIdOrthanc(orthancStudyId);
        return requestJsonOrthanc("/studies/"+orthancStudyId,HttpMethod.GET,null);
    }

    private JsonNode terbitkanAccessionOrthanc(
            String orthancStudyId, String accessionNumber) throws Exception {
        validasiIdOrthanc(orthancStudyId);
        com.fasterxml.jackson.databind.node.ObjectNode replace=mapper.createObjectNode();
        replace.put("AccessionNumber",accessionNumber);

        com.fasterxml.jackson.databind.node.ObjectNode body=mapper.createObjectNode();
        body.set("Replace",replace);
        body.put("KeepSource",false);
        return requestJsonOrthanc(
                "/studies/"+orthancStudyId+"/modify",HttpMethod.POST,body);
    }

    private JsonNode requestJsonOrthanc(
            String path, HttpMethod method, JsonNode body) throws Exception {
        ApiOrthanc orthanc=new ApiOrthanc();
        HttpHeaders orthancHeaders=new HttpHeaders();
        orthancHeaders.add("Authorization","Basic "+orthanc.Auth());
        orthancHeaders.add("Accept","application/json");

        String requestBody=body==null?null:mapper.writeValueAsString(body);
        if(body!=null){
            orthancHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        HttpEntity<String> orthancRequest=body==null
                ?new HttpEntity<String>(orthancHeaders)
                :new HttpEntity<String>(requestBody,orthancHeaders);
        org.springframework.http.ResponseEntity<String> orthancResponse;
        try{
            orthancResponse=orthanc.getRest().exchange(
                    urlDasarOrthanc()+path,method,orthancRequest,String.class);
        }catch(HttpStatusCodeException e){
            String detail=teksAman(e.getResponseBodyAsString());
            if(detail.isEmpty()){
                detail=e.getMessage();
            }
            throw new IOException("Orthanc menolak permintaan "+path+
                    " (HTTP "+e.getStatusCode()+") : "+batasiPesan(detail,500),e);
        }
        String responseBody=orthancResponse.getBody();
        if(responseBody==null||responseBody.trim().isEmpty()){
            return mapper.createObjectNode();
        }
        return mapper.readTree(responseBody);
    }

    private void prosesKirimRouterDicom(List<Integer> barisTerpilih) {
        final Map<Integer,String[]> dataSiapKirim=new java.util.LinkedHashMap<Integer,String[]>();
        final StringBuilder daftarGagal=new StringBuilder();
        int imagingStudySiap=0;
        int cstoreTerverifikasi=0;
        int belumTerbit=0;
        int gagal=0;
        TargetRouterDicom targetRouter=null;

        System.out.println("[ROUTER-DICOM] Memulai pemeriksaan "+barisTerpilih.size()+" data terpilih");
        for(Integer baris:barisTerpilih){
            try{
                if(baris==null||baris<0||baris>=tabMode.getRowCount()){
                    throw new IllegalStateException("Baris data sudah tidak tersedia");
                }
                String noPermintaan=nilaiTabel(baris,9);
                String idServiceRequest=nilaiTabel(baris,16);
                if(idServiceRequest.isEmpty()||idServiceRequest.equals("-")){
                    throw new IllegalStateException("ID ServiceRequest belum tersedia");
                }

                String[] dataOrthanc=pastikanDataOrthanc(baris);
                String acsn=dataOrthanc[0];

                HttpHeaders headersValidasiServiceRequest=new HttpHeaders();
                headersValidasiServiceRequest.setContentType(MediaType.APPLICATION_JSON);
                headersValidasiServiceRequest.add("Authorization", "Bearer "+api.TokenSatuSehat());
                validasiServiceRequestUntukRouterDicom(
                        baris,acsn,idServiceRequest,headersValidasiServiceRequest);

                String imagingId=cariImagingStudyIDSatuSehat(acsn,false);
                if(!imagingId.isEmpty()){
                    String kunci=kunciRouterDicom(baris);
                    imagingStudySiapSinkron.put(kunci,imagingId);
                    routerDicomDiterima.remove(kunci);
                    imagingStudySiap++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] ImagingStudy sudah tersedia: "+imagingId+
                            ". Pengiriman ulang dilewati");
                }else{
                    dataSiapKirim.put(baris,dataOrthanc);
                }
            }catch(Exception e){
                gagal++;
                catatGagalRouterDicom(daftarGagal,baris,e);
            }
        }

        if(!dataSiapKirim.isEmpty()){
            try{
                targetRouter=validasiTargetRouterDicom();
            }catch(Exception e){
                for(Integer baris:dataSiapKirim.keySet()){
                    gagal++;
                    catatGagalRouterDicom(daftarGagal,baris,e);
                }
                dataSiapKirim.clear();
            }
        }

        for(Map.Entry<Integer,String[]> data:dataSiapKirim.entrySet()){
            int baris=data.getKey();
            String noPermintaan=nilaiTabel(baris,9);
            String acsn=data.getValue()[0];
            String orthancStudyId=data.getValue()[1];
            String kunci=kunciRouterDicom(baris);
            boolean cstoreBerhasil=false;
            try{
                int jumlahInstance=hitungInstanceStudyOrthanc(orthancStudyId);
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Mengirim ACSN "+acsn+", Orthanc Study ID "+orthancStudyId+
                        ", "+jumlahInstance+" instance ke "+targetRouter.deskripsi());
                String jobId=kirimStudyKeRouterDicom(orthancStudyId);
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Job Orthanc dibuat: "+jobId);
                tungguJobOrthanc(
                        jobId,noPermintaan,jumlahInstance,targetRouter);
                cstoreBerhasil=true;
                routerDicomDiterima.add(kunci);
                cstoreTerverifikasi++;
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Orthanc memverifikasi C-STORE "+jumlahInstance+
                        " instance ke "+targetRouter.deskripsi()+
                        ". Menunggu hasil pemrosesan dicom-router.exe");

                String imagingId=tungguImagingStudy(acsn,noPermintaan);
                if(!imagingId.isEmpty()){
                    imagingStudySiapSinkron.put(kunci,imagingId);
                    routerDicomDiterima.remove(kunci);
                    imagingStudySiap++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] ImagingStudy siap disinkronkan: "+imagingId);
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] PROSES SELESAI - pengiriman Router DICOM sukses");
                }else{
                    belumTerbit++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] ImagingStudy belum terbit setelah "+
                            BATAS_TUNGGU_IMAGING_STUDY_DETIK+
                            " detik. Gunakan tombol Cek Status Router DICOM");
                }
            }catch(Exception e){
                if(cstoreBerhasil){
                    belumTerbit++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] C-STORE sudah terverifikasi, tetapi pemeriksaan ImagingStudy gagal: "+
                            (e.getMessage()==null?e.toString():e.getMessage()));
                }else{
                    routerDicomDiterima.remove(kunci);
                    imagingStudySiapSinkron.remove(kunci);
                    gagal++;
                    catatGagalRouterDicom(daftarGagal,baris,e);
                }
            }
        }

        final int hasilImagingStudySiap=imagingStudySiap;
        final int hasilCstoreTerverifikasi=cstoreTerverifikasi;
        final int hasilBelumTerbit=belumTerbit;
        final int hasilGagal=gagal;
        System.out.println("[ROUTER-DICOM] Ringkasan - C-STORE terverifikasi "+
                hasilCstoreTerverifikasi+
                ", ImagingStudy siap "+hasilImagingStudySiap+
                ", menunggu verifikasi router "+hasilBelumTerbit+
                ", gagal "+hasilGagal);
        SwingUtilities.invokeLater(() -> {
            if(!isDisplayable()){
                return;
            }
            aturTombolAlur();
            StringBuilder pesan=new StringBuilder();
            pesan.append("Proses Kirim ke Router DICOM selesai.")
                    .append("\nC-STORE terverifikasi: ").append(hasilCstoreTerverifikasi)
                    .append("\nImagingStudy siap disinkronkan: ").append(hasilImagingStudySiap);
            if(hasilBelumTerbit>0){
                pesan.append("\nMenunggu verifikasi DICOM Router: ").append(hasilBelumTerbit)
                        .append("\nSilakan lihat console dicom-router.exe, lalu gunakan ")
                        .append("Cek Status Router DICOM.");
            }
            if(hasilGagal>0){
                pesan.append("\nGagal: ").append(hasilGagal)
                        .append("\n\nRincian:")
                        .append(batasiPesan(daftarGagal.toString(),1200));
            }
            JOptionPane.showMessageDialog(this,pesan.toString(),
                    "Router DICOM",
                    hasilGagal>0?JOptionPane.WARNING_MESSAGE:JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private TargetRouterDicom validasiTargetRouterDicom() throws Exception {
        System.out.println("[ROUTER-DICOM] Memvalidasi konfigurasi modality "+
                MODALITY_ROUTER_DICOM+"...");
        JsonNode seluruhModality=requestJsonOrthanc(
                "/modalities?expand",HttpMethod.GET,null);
        JsonNode konfigurasi=cariKonfigurasiModality(seluruhModality);
        if(konfigurasi==null){
            JsonNode modality=requestJsonOrthanc(
                    "/modalities/"+MODALITY_ROUTER_DICOM,HttpMethod.GET,null);
            konfigurasi=konfigurasiModality(modality);
        }
        if(konfigurasi==null){
            throw new IllegalStateException(
                    "Konfigurasi modality "+MODALITY_ROUTER_DICOM+
                    " tidak dapat dibaca dari Orthanc");
        }

        String aet=nilaiKonfigurasiModality(
                konfigurasi,"AET","Aet","AETitle");
        String host=nilaiKonfigurasiModality(
                konfigurasi,"Host","Hostname");
        String nilaiPort=nilaiKonfigurasiModality(konfigurasi,"Port");
        int port;
        try{
            port=Integer.parseInt(nilaiPort);
        }catch(Exception e){
            throw new IllegalStateException(
                    "Port modality "+MODALITY_ROUTER_DICOM+
                    " tidak valid: "+nilaiPort);
        }

        TargetRouterDicom target=new TargetRouterDicom(aet,host,port);
        System.out.println("[ROUTER-DICOM] Target aktual Orthanc: "+
                target.deskripsi());
        if(!AET_ROUTER_DICOM.equalsIgnoreCase(aet)||
                port!=PORT_DICOM_ROUTER||host.isEmpty()){
            throw new IllegalStateException(
                    "Target modality "+MODALITY_ROUTER_DICOM+
                    " tidak sesuai dicom-router.exe. Ditemukan "+
                    target.deskripsi()+", seharusnya AET "+
                    AET_ROUTER_DICOM+" dan port "+PORT_DICOM_ROUTER+
                    ". Perbaiki konfigurasi modality di Orthanc");
        }

        cekKesehatanHttpRouterDicom(target);

        System.out.println("[ROUTER-DICOM] Memeriksa C-ECHO ke "+
                target.deskripsi()+"...");
        com.fasterxml.jackson.databind.node.ObjectNode body=mapper.createObjectNode();
        body.put("Timeout",10);
        requestJsonOrthanc("/modalities/"+MODALITY_ROUTER_DICOM+"/echo",
                HttpMethod.POST,body);
        System.out.println("[ROUTER-DICOM] C-ECHO ke "+
                target.deskripsi()+" berhasil");
        return target;
    }

    private JsonNode cariKonfigurasiModality(JsonNode seluruhModality) {
        if(seluruhModality==null||!seluruhModality.isObject()){
            return null;
        }
        java.util.Iterator<Map.Entry<String,JsonNode>> daftar=
                seluruhModality.fields();
        while(daftar.hasNext()){
            Map.Entry<String,JsonNode> item=daftar.next();
            if(MODALITY_ROUTER_DICOM.equalsIgnoreCase(item.getKey())){
                return konfigurasiModality(item.getValue());
            }
        }
        return null;
    }

    private JsonNode konfigurasiModality(JsonNode data) {
        if(data==null||!data.isObject()){
            return null;
        }
        if(data.path("Configuration").isObject()){
            return data.path("Configuration");
        }
        return data;
    }

    private String nilaiKonfigurasiModality(
            JsonNode konfigurasi, String... namaKolom) {
        for(String nama:namaKolom){
            String nilai=konfigurasi.path(nama).asText().trim();
            if(!nilai.isEmpty()){
                return nilai;
            }
        }
        return "";
    }

    private void cekKesehatanHttpRouterDicom(
            TargetRouterDicom target) throws Exception {
        String hostHealth=hostUntukHealthCheck(target.host);
        URI alamat=new URI(
                "http",null,hostHealth,PORT_HTTP_ROUTER,
                PATH_HEALTH_ROUTER_DICOM,null,null);
        String isi;
        java.net.HttpURLConnection koneksiRouter=null;
        try{
            koneksiRouter=(java.net.HttpURLConnection)
                    alamat.toURL().openConnection();
            koneksiRouter.setRequestMethod("GET");
            koneksiRouter.setRequestProperty("Accept","application/json");
            koneksiRouter.setConnectTimeout(5000);
            koneksiRouter.setReadTimeout(5000);
            int statusHttp=koneksiRouter.getResponseCode();
            java.io.InputStream aliran=statusHttp>=200&&statusHttp<300
                    ?koneksiRouter.getInputStream():koneksiRouter.getErrorStream();
            isi=bacaResponHttpRouter(aliran);
            if(statusHttp<200||statusHttp>=300){
                throw new IOException(
                        "HTTP "+statusHttp+": "+batasiPesan(isi,300));
            }
        }catch(Exception e){
            String detail=e.getMessage()==null?e.toString():e.getMessage();
            throw new IllegalStateException(
                    "Health-check dicom-router.exe gagal pada "+alamat+
                    ". Penyebab: "+batasiPesan(detail,300)+
                    ". Pastikan dicom-router.exe aktif, host modality benar, "+
                    "dan port "+PORT_HTTP_ROUTER+" tidak diblokir",e);
        }finally{
            if(koneksiRouter!=null){
                koneksiRouter.disconnect();
            }
        }
        if(isi==null||isi.trim().isEmpty()){
            throw new IllegalStateException(
                    "HTTP "+alamat+" merespons tanpa status kesehatan router");
        }

        JsonNode status;
        try{
            status=mapper.readTree(isi);
        }catch(Exception e){
            throw new IllegalStateException(
                    "Respons health-check dari "+alamat+
                    " bukan JSON yang valid: "+batasiPesan(isi,300),e);
        }
        boolean sehat=false;
        for(JsonNode issue:status.path("issue")){
            if("All OK".equalsIgnoreCase(
                    issue.path("details").path("text").asText().trim())){
                sehat=true;
                break;
            }
        }
        if(!sehat){
            throw new IllegalStateException(
                    "Layanan pada "+alamat+
                    " bukan dicom-router.exe yang siap menerima DICOM");
        }
        System.out.println("[ROUTER-DICOM] HTTP dicom-router.exe sehat pada "+
                alamat);
    }

    private String bacaResponHttpRouter(
            java.io.InputStream aliran) throws IOException {
        if(aliran==null){
            return "";
        }
        try(java.util.Scanner pembaca=
                new java.util.Scanner(aliran,"UTF-8").useDelimiter("\\A")){
            return pembaca.hasNext()?pembaca.next():"";
        }
    }

    private String hostUntukHealthCheck(String hostTarget) throws Exception {
        String host=hostTarget.trim();
        if(host.equalsIgnoreCase("localhost")||
                host.startsWith("127.")||host.equals("::1")||
                host.equals("[::1]")){
            URI alamatOrthanc=new URI(urlDasarOrthanc());
            String hostOrthanc=alamatOrthanc.getHost();
            if(hostOrthanc==null||hostOrthanc.trim().isEmpty()){
                throw new IllegalStateException(
                        "Host modality menggunakan loopback ("+host+
                        "), tetapi host server Orthanc tidak dapat ditentukan");
            }
            host=hostOrthanc.trim();
            System.out.println("[ROUTER-DICOM] Host modality menggunakan loopback. "+
                    "Health dicom-router.exe diperiksa melalui host server Orthanc "+
                    host);
        }
        if(host.equals("0.0.0.0")||host.equals("::")){
            throw new IllegalStateException(
                    "Host modality "+host+
                    " bukan alamat tujuan DICOM yang valid");
        }
        return host;
    }

    private int hitungInstanceStudyOrthanc(
            String orthancStudyId) throws Exception {
        validasiIdOrthanc(orthancStudyId);
        JsonNode study=ambilStudyOrthanc(orthancStudyId);
        int jumlah=0;
        for(JsonNode seriesId:study.path("Series")){
            String id=seriesId.asText().trim();
            validasiIdOrthanc(id);
            JsonNode series=requestJsonOrthanc(
                    "/series/"+id,HttpMethod.GET,null);
            jumlah+=series.path("Instances").size();
        }
        if(jumlah<=0){
            throw new IllegalStateException(
                    "Study Orthanc "+orthancStudyId+
                    " tidak mempunyai instance DICOM untuk dikirim");
        }
        return jumlah;
    }

    private String kirimStudyKeRouterDicom(String orthancStudyId) throws Exception {
        validasiIdOrthanc(orthancStudyId);
        com.fasterxml.jackson.databind.node.ArrayNode resources=mapper.createArrayNode();
        resources.add(orthancStudyId);

        com.fasterxml.jackson.databind.node.ObjectNode body=mapper.createObjectNode();
        body.set("Resources",resources);
        body.put("Synchronous",false);
        body.put("Timeout",60);

        JsonNode hasil=requestJsonOrthanc(
                "/modalities/"+MODALITY_ROUTER_DICOM+"/store",
                HttpMethod.POST,body);
        String jobId=hasil.path("ID").asText().trim();
        if(jobId.isEmpty()){
            throw new IllegalStateException(
                    "Orthanc tidak mengembalikan Job ID untuk pengiriman ke "+MODALITY_ROUTER_DICOM);
        }
        validasiIdOrthanc(jobId);
        return jobId;
    }

    private void tungguJobOrthanc(
            String jobId, String noPermintaan, int jumlahInstance,
            TargetRouterDicom target) throws Exception {
        for(int detik=0;detik<BATAS_TUNGGU_JOB_ROUTER_DETIK;detik++){
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException("Pemantauan job Orthanc dihentikan");
            }
            JsonNode job=requestJsonOrthanc("/jobs/"+jobId,HttpMethod.GET,null);
            String status=job.path("State").asText();
            if(status.equalsIgnoreCase("Success")){
                validasiHasilJobOrthanc(
                        job,jumlahInstance,target,noPermintaan);
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Job Orthanc berhasil dan hasil C-STORE tervalidasi (100%)");
                return;
            }
            if(status.equalsIgnoreCase("Failure")||status.equalsIgnoreCase("Paused")){
                String detail=job.path("ErrorDescription").asText();
                int statusDimse=cariStatusDimseTidakSukses(job);
                if(statusDimse>0){
                    detail="DICOM Router menolak C-STORE dengan status DIMSE 0x"+
                            String.format("%04X",statusDimse);
                }
                if(detail.isEmpty()){
                    detail="Status job Orthanc: "+status+
                            ", kode "+job.path("ErrorCode").asText();
                }
                throw new IllegalStateException(detail);
            }
            if(detik==0||detik%10==0){
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Job Orthanc "+status+", progres "+
                        job.path("Progress").asInt(0)+"%");
            }
            jedaRouterDicom(1000);
        }
        throw new IllegalStateException("Job Orthanc belum selesai setelah "+
                BATAS_TUNGGU_JOB_ROUTER_DETIK+" detik");
    }

    private void validasiHasilJobOrthanc(
            JsonNode job, int jumlahInstance, TargetRouterDicom target,
            String noPermintaan) {
        JsonNode content=job.path("Content");
        String remoteAet=nilaiKonfigurasiModality(
                content,"RemoteAet","RemoteAET","RemoteAETitle");
        if(!remoteAet.isEmpty()&&!target.aet.equalsIgnoreCase(remoteAet)){
            throw new IllegalStateException(
                    "Job Orthanc dikirim ke Remote AET "+remoteAet+
                    ", bukan "+target.aet);
        }

        int jumlahDilaporkan=-1;
        if(content.has("InstancesCount")){
            jumlahDilaporkan=content.path("InstancesCount").asInt(-1);
        }else if(content.has("InstanceCount")){
            jumlahDilaporkan=content.path("InstanceCount").asInt(-1);
        }
        if(jumlahDilaporkan>=0&&jumlahDilaporkan!=jumlahInstance){
            throw new IllegalStateException(
                    "Jumlah instance pada hasil job Orthanc tidak sesuai. "+
                    "Diharapkan "+jumlahInstance+", dilaporkan "+
                    jumlahDilaporkan);
        }
        int jumlahGagal=content.path("FailedInstancesCount").asInt(0);
        if(jumlahGagal>0){
            throw new IllegalStateException(
                    jumlahGagal+" instance gagal diterima DICOM Router");
        }

        int statusDimse=cariStatusDimseTidakSukses(content);
        if(statusDimse>0){
            throw new IllegalStateException(
                    "DICOM Router mengembalikan status DIMSE 0x"+
                    String.format("%04X",statusDimse));
        }
        if(jumlahDilaporkan<0){
            System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                    "] Versi Orthanc tidak melaporkan InstancesCount; "+
                    "validasi dilanjutkan menggunakan State dan status DIMSE");
        }else{
            System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                    "] Job melaporkan "+jumlahDilaporkan+
                    " instance ke Remote AET "+
                    (remoteAet.isEmpty()?target.aet:remoteAet));
        }
    }

    private int cariStatusDimseTidakSukses(JsonNode data) {
        if(data==null||data.isNull()||data.isMissingNode()){
            return 0;
        }
        if(data.isObject()){
            java.util.Iterator<Map.Entry<String,JsonNode>> daftar=data.fields();
            while(daftar.hasNext()){
                Map.Entry<String,JsonNode> item=daftar.next();
                String nama=item.getKey();
                if(nama.equalsIgnoreCase("DimseErrorStatus")||
                        nama.equalsIgnoreCase("DimseStatus")||
                        nama.equalsIgnoreCase("DicomStatus")){
                    int nilai=nilaiStatusDimse(item.getValue());
                    if(nilai>0){
                        return nilai;
                    }
                }
                int anak=cariStatusDimseTidakSukses(item.getValue());
                if(anak>0){
                    return anak;
                }
            }
        }else if(data.isArray()){
            for(JsonNode item:data){
                int anak=cariStatusDimseTidakSukses(item);
                if(anak>0){
                    return anak;
                }
            }
        }
        return 0;
    }

    private int nilaiStatusDimse(JsonNode data) {
        if(data==null||data.isNull()){
            return 0;
        }
        if(data.isNumber()){
            return data.asInt(0);
        }
        String nilai=data.asText().trim();
        if(nilai.isEmpty()||nilai.equalsIgnoreCase("Success")){
            return 0;
        }
        try{
            if(nilai.startsWith("0x")||nilai.startsWith("0X")){
                return Integer.parseInt(nilai.substring(2),16);
            }
            return Integer.parseInt(nilai);
        }catch(Exception e){
            return 0;
        }
    }

    private String tungguImagingStudy(String acsn, String noPermintaan) throws Exception {
        int jumlahPercobaan=Math.max(1,BATAS_TUNGGU_IMAGING_STUDY_DETIK/5);
        System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                "] Menunggu ImagingStudy diterbitkan DICOM Router");
        for(int percobaan=0;percobaan<jumlahPercobaan;percobaan++){
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException("Pemantauan ImagingStudy dihentikan");
            }
            String imagingId=getImagingStudyID(acsn,false);
            if(!imagingId.isEmpty()){
                return imagingId;
            }
            if(percobaan<jumlahPercobaan-1){
                jedaRouterDicom(5000);
            }
        }
        return "";
    }

    private void prosesCekStatusRouterDicom(
            List<Integer> barisTerpilih) {
        int siap=0;
        int masihMenunggu=0;
        int gagal=0;
        StringBuilder daftarGagal=new StringBuilder();
        System.out.println("[ROUTER-DICOM] Memeriksa status "+
                barisTerpilih.size()+" data yang menunggu router");
        for(Integer baris:barisTerpilih){
            try{
                String noPermintaan=nilaiTabel(baris,9);
                String acsn=nilaiTabel(baris,18);
                if(acsn.isEmpty()||acsn.equals("-")){
                    throw new IllegalStateException("ACSN belum tersedia");
                }
                String kunci=kunciRouterDicom(baris);
                String imagingId=cariImagingStudyIDSatuSehat(acsn,false);
                if(imagingId.isEmpty()){
                    routerDicomDiterima.add(kunci);
                    masihMenunggu++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] ImagingStudy belum diterbitkan");
                }else{
                    imagingStudySiapSinkron.put(kunci,imagingId);
                    routerDicomDiterima.remove(kunci);
                    siap++;
                    System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                            "] ImagingStudy sudah tersedia: "+imagingId+
                            ". Tahap diarahkan ke Sinkronkan ImagingStudy");
                }
            }catch(Exception e){
                gagal++;
                catatGagalRouterDicom(daftarGagal,baris,e);
            }
        }

        final int hasilSiap=siap;
        final int hasilMenunggu=masihMenunggu;
        final int hasilGagal=gagal;
        SwingUtilities.invokeLater(() -> {
            if(!isDisplayable()){
                return;
            }
            aturTombolAlur();
            StringBuilder pesan=new StringBuilder();
            pesan.append("Pemeriksaan status Router DICOM selesai.")
                    .append("\nImagingStudy siap disinkronkan: ")
                    .append(hasilSiap)
                    .append("\nMasih menunggu dicom-router.exe: ")
                    .append(hasilMenunggu);
            if(hasilGagal>0){
                pesan.append("\nGagal diperiksa: ").append(hasilGagal)
                        .append("\n\nRincian:")
                        .append(batasiPesan(daftarGagal.toString(),1200));
            }
            JOptionPane.showMessageDialog(this,pesan.toString(),
                    "Status Router DICOM",
                    hasilGagal>0?JOptionPane.WARNING_MESSAGE:
                            JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void jedaRouterDicom(long milidetik) throws InterruptedException {
        try{
            Thread.sleep(milidetik);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private void catatGagalRouterDicom(
            StringBuilder daftarGagal, Integer baris, Exception error) {
        String noPermintaan=(baris!=null&&baris>=0&&baris<tabMode.getRowCount())
                ?nilaiTabel(baris,9):"-";
        String detail=error.getMessage()==null?error.toString():error.getMessage();
        System.out.println("[ROUTER-DICOM] ["+noPermintaan+"] GAGAL: "+detail);
        if(daftarGagal.length()>0){
            daftarGagal.append("\n");
        }
        daftarGagal.append("\n• ").append(noPermintaan).append(": ")
                .append(batasiPesan(detail,300));
    }

    private String urlDasarOrthanc() {
        String url=koneksiDB.URLORTHANC().trim();
        while(url.endsWith("/")){
            url=url.substring(0,url.length()-1);
        }
        return url+":"+String.valueOf(koneksiDB.PORTORTHANC()).trim();
    }

    private void validasiIdOrthanc(String id) throws IOException {
        if(id==null||!id.matches("[A-Fa-f0-9-]+")){
            throw new IOException("ID resource Orthanc tidak valid");
        }
    }

    private int jumlahDataOrthanc(JsonNode data) {
        if(data==null||data.isNull()||data.isMissingNode()){
            return 0;
        }
        if(data.isArray()){
            return data.size();
        }
        return data.path("ID").asText().isEmpty()?0:1;
    }

    private List<JsonNode> daftarStudyOrthanc(JsonNode studies) {
        List<JsonNode> daftar=new ArrayList<JsonNode>();
        if(studies==null){
            return daftar;
        }
        if(studies.isArray()){
            for(JsonNode study:studies){
                if(!study.path("ID").asText().isEmpty()){
                    daftar.add(study);
                }
            }
        }else if(!studies.path("ID").asText().isEmpty()){
            daftar.add(studies);
        }
        return daftar;
    }

    private JsonNode pilihStudyOrthancMenurutWaktu(
            JsonNode studies, String jamPemeriksaan) {
        List<JsonNode> daftar=daftarStudyOrthanc(studies);
        if(daftar.isEmpty()){
            return null;
        }
        if(daftar.size()==1){
            return daftar.get(0);
        }

        int waktuTarget=waktuKeDetik(jamPemeriksaan);
        if(waktuTarget<0){
            throw new IllegalStateException("Jam pemeriksaan tidak valid untuk memilih study Orthanc");
        }

        JsonNode terbaik=null;
        int selisihTerbaik=Integer.MAX_VALUE;
        boolean hasilSeri=false;
        for(JsonNode kandidat:daftar){
            int waktuStudy=waktuKeDetik(tagStudyOrthanc(kandidat,"StudyTime"));
            if(waktuStudy<0){
                continue;
            }
            int selisih=Math.abs(waktuTarget-waktuStudy);
            boolean kandidatPunyaAcsn=!tagStudyOrthanc(kandidat,"AccessionNumber").isEmpty();
            boolean terbaikPunyaAcsn=terbaik!=null&&!tagStudyOrthanc(terbaik,"AccessionNumber").isEmpty();
            if(terbaik==null||selisih<selisihTerbaik){
                terbaik=kandidat;
                selisihTerbaik=selisih;
                hasilSeri=false;
            }else if(selisih==selisihTerbaik){
                if(kandidatPunyaAcsn&&!terbaikPunyaAcsn){
                    terbaik=kandidat;
                    hasilSeri=false;
                }else if(kandidatPunyaAcsn==terbaikPunyaAcsn){
                    hasilSeri=true;
                }
            }
        }
        if(terbaik==null){
            throw new IllegalStateException("Ditemukan beberapa study Orthanc tetapi StudyTime tidak tersedia");
        }
        if(hasilSeri){
            throw new IllegalStateException("Ditemukan beberapa study Orthanc dengan Patient ID, tanggal, dan selisih jam yang sama");
        }
        System.out.println("Study Orthanc dipilih berdasarkan jam terdekat, selisih "+
                selisihTerbaik+" detik, ID "+terbaik.path("ID").asText());
        return terbaik;
    }

    private int waktuKeDetik(String nilaiWaktu) {
        String waktu=teksAman(nilaiWaktu);
        if(waktu.isEmpty()){
            return -1;
        }
        try{
            int jam;
            int menit;
            int detik;
            if(waktu.contains(":")){
                String[] bagian=waktu.split(":");
                jam=Integer.parseInt(bagian[0]);
                menit=bagian.length>1?Integer.parseInt(bagian[1]):0;
                String bagianDetik=bagian.length>2?bagian[2]:"0";
                int titik=bagianDetik.indexOf(".");
                if(titik>=0){
                    bagianDetik=bagianDetik.substring(0,titik);
                }
                detik=Integer.parseInt(bagianDetik);
            }else{
                int titik=waktu.indexOf(".");
                if(titik>=0){
                    waktu=waktu.substring(0,titik);
                }
                waktu=waktu.replaceAll("[^0-9]","");
                if(waktu.length()==5){
                    waktu="0"+waktu;
                }else if(waktu.length()==4){
                    waktu=waktu+"00";
                }else if(waktu.length()==2){
                    waktu=waktu+"0000";
                }
                if(waktu.length()<6){
                    return -1;
                }
                waktu=waktu.substring(0,6);
                jam=Integer.parseInt(waktu.substring(0,2));
                menit=Integer.parseInt(waktu.substring(2,4));
                detik=Integer.parseInt(waktu.substring(4,6));
            }
            if(jam<0||jam>23||menit<0||menit>59||detik<0||detik>59){
                return -1;
            }
            return (jam*3600)+(menit*60)+detik;
        }catch(Exception e){
            return -1;
        }
    }

    private String tagStudyOrthanc(JsonNode study, String namaTag) {
        if(study==null||study.isNull()||study.isMissingNode()){
            return "";
        }
        String nilai=study.path("MainDicomTags").path(namaTag).asText();
        if(nilai.isEmpty()){
            nilai=study.path("RequestedTags").path(namaTag).asText();
        }
        return nilai.trim();
    }

    private String tagPatientOrthanc(JsonNode study, String namaTag) {
        if(study==null||study.isNull()||study.isMissingNode()){
            return "";
        }
        String nilai=study.path("PatientMainDicomTags").path(namaTag).asText();
        if(nilai.isEmpty()){
            nilai=tagStudyOrthanc(study,namaTag);
        }
        return nilai.trim();
    }

    private boolean studyOrthancSesuai(
            JsonNode study, String patientId, String studyDate, String accessionNumber) {
        if(study==null||study.isNull()||study.isMissingNode()||
                study.path("ID").asText().isEmpty()){
            return false;
        }
        return tagPatientOrthanc(study,"PatientID").equals(patientId)
                &&tagStudyOrthanc(study,"StudyDate").equals(studyDate)
                &&tagStudyOrthanc(study,"AccessionNumber").equals(accessionNumber);
    }

    private String teksAman(String nilai) {
        return nilai==null?"":nilai.trim();
    }

    private void simpanCacheOrthanc(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String noOrder, String kdJenisPrw, String accessionNumber,
            String orthancStudyId) throws Exception {
        String sql="insert into radiologi_pacs_token "+
                "(no_rawat,tgl_periksa,jam,token_pacs,noorder,kd_jenis_prw,"+
                "accession_number,orthanc_study_id,tgl_buat,pembuat) "+
                "values (?,?,?,null,?,?,?,?,now(),?) "+
                "on duplicate key update noorder=values(noorder),"+
                "accession_number=values(accession_number),"+
                "orthanc_study_id=values(orthanc_study_id)";
        try(PreparedStatement pst=koneksi.prepareStatement(sql)){
            pst.setString(1,noRawat);
            pst.setString(2,tglPeriksa);
            pst.setString(3,jamPeriksa);
            pst.setString(4,noOrder);
            pst.setString(5,kdJenisPrw);
            pst.setString(6,accessionNumber);
            pst.setString(7,orthancStudyId);
            pst.setString(8,akses.getkode());
            pst.executeUpdate();
        }
    }

    private void perbaruiDataOrthancTabel(
            final int baris, final String accessionNumber,
            final String orthancStudyId) throws Exception {
        Runnable pembaruan=() -> {
            if(baris>=0&&baris<tabMode.getRowCount()){
                tabMode.setValueAt(accessionNumber,baris,18);
                tabMode.setValueAt(orthancStudyId,baris,20);
                aturTombolAlur();
            }
        };
        if(SwingUtilities.isEventDispatchThread()){
            pembaruan.run();
        }else{
            SwingUtilities.invokeAndWait(pembaruan);
        }
    }

    private String buatPayloadServiceRequest(int baris, String idServiceRequest, String idDokter, String idPasien) {
        String noPermintaan=nilaiTabel(baris,9);
        String kodePemeriksaan=nilaiTabel(baris,17);
        // ACSN ServiceRequest berasal langsung dari noorder dan tidak menunggu
        // radiologi_pacs_token maupun study Orthanc.
        String acsn=noPermintaan;
        if(noPermintaan.isEmpty()||kodePemeriksaan.isEmpty()){
            throw new IllegalStateException("Nomor permintaan atau kode pemeriksaan radiologi belum lengkap");
        }
        if(acsn.isEmpty()||acsn.equals("-")){
            throw new IllegalStateException("ACSN tidak dapat dibentuk karena No.Permintaan kosong");
        }
        if(nilaiTabel(baris,13).isEmpty()||nilaiTabel(baris,14).isEmpty()||nilaiTabel(baris,15).isEmpty()){
            throw new IllegalStateException("Mapping kode radiologi SATUSEHAT belum lengkap untuk "+nilaiTabel(baris,12));
        }
        if(nilaiTabel(baris,8).isEmpty()){
            throw new IllegalStateException("ID Encounter SATUSEHAT belum tersedia untuk No.Rawat "+nilaiTabel(baris,1));
        }

        String idOrganisasi=koneksiDB.IDSATUSEHAT();
        if(idOrganisasi==null||idOrganisasi.trim().isEmpty()){
            throw new IllegalStateException("ID Organisasi SATUSEHAT belum dikonfigurasi");
        }
        String nomorPermintaanLokal=noPermintaan+(kodePemeriksaan.isEmpty()?"":"-"+kodePemeriksaan);
        String waktuUtc=formatWaktuFHIRUtc(nilaiTabel(baris,10));
        String displayEncounter="Permintaan "+nilaiTabel(baris,12)+" atas nama pasien "+nilaiTabel(baris,3)+
                " No.RM "+nilaiTabel(baris,2)+" No.Rawat "+nilaiTabel(baris,1)+
                ", pada tanggal "+nilaiTabel(baris,10);
        String alasanKlinis=nilaiTabel(baris,11);

        StringBuilder payload=new StringBuilder();
        payload.append("{")
                .append("\"resourceType\":\"ServiceRequest\",");
        if(idServiceRequest!=null&&!idServiceRequest.trim().isEmpty()){
            payload.append("\"id\":\"").append(escapeJson(idServiceRequest.trim())).append("\",");
        }
        payload.append("\"identifier\":[")
                .append("{")
                .append("\"system\":\"http://sys-ids.kemkes.go.id/servicerequest/").append(escapeJson(idOrganisasi)).append("\",")
                .append("\"value\":\"").append(escapeJson(nomorPermintaanLokal)).append("\"")
                .append("},")
                .append("{")
                .append("\"use\":\"usual\",")
                .append("\"type\":{\"coding\":[{")
                .append("\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",")
                .append("\"code\":\"ACSN\"")
                .append("}]},")
                .append("\"system\":\"http://sys-ids.kemkes.go.id/acsn/").append(escapeJson(idOrganisasi)).append("\",")
                .append("\"value\":\"").append(escapeJson(acsn)).append("\"")
                .append("}")
                .append("],")
                .append("\"status\":\"active\",")
                .append("\"intent\":\"original-order\",")
                .append("\"priority\":\"routine\",")
                .append("\"category\":[{\"coding\":[{")
                .append("\"system\":\"http://snomed.info/sct\",")
                .append("\"code\":\"363679005\",")
                .append("\"display\":\"Imaging\"")
                .append("}]}],")
                .append("\"code\":{\"coding\":[{")
                .append("\"system\":\"").append(escapeJson(nilaiTabel(baris,14))).append("\",")
                .append("\"code\":\"").append(escapeJson(nilaiTabel(baris,13))).append("\",")
                .append("\"display\":\"").append(escapeJson(nilaiTabel(baris,15))).append("\"")
                .append("}],")
                .append("\"text\":\"").append(escapeJson(nilaiTabel(baris,12))).append("\"")
                .append("},")
                .append("\"subject\":{\"reference\":\"Patient/").append(escapeJson(idPasien)).append("\"},")
                .append("\"encounter\":{")
                .append("\"reference\":\"Encounter/").append(escapeJson(nilaiTabel(baris,8))).append("\",")
                .append("\"display\":\"").append(escapeJson(displayEncounter)).append("\"")
                .append("},")
                .append("\"occurrenceDateTime\":\"").append(waktuUtc).append("\",")
                .append("\"authoredOn\":\"").append(waktuUtc).append("\",")
                .append("\"requester\":{")
                .append("\"reference\":\"Practitioner/").append(escapeJson(idDokter)).append("\",")
                .append("\"display\":\"").append(escapeJson(nilaiTabel(baris,6))).append("\"")
                .append("},")
                .append("\"performer\":[{")
                .append("\"reference\":\"Organization/").append(escapeJson(idOrganisasi)).append("\",")
                .append("\"display\":\"Ruang Radiologi/Petugas Radiologi\"")
                .append("}]");
        if(!alasanKlinis.isEmpty()){
            payload.append(",\"reasonCode\":[{\"text\":\"").append(escapeJson(alasanKlinis)).append("\"}]");
        }
        payload.append("}");
        return payload.toString();
    }

    /**
     * Membentuk payload PUT dari resource terbaru di SATUSEHAT. Elemen yang
     * dikelola SIMRS diperbarui dari data lokal, sedangkan elemen tambahan yang
     * mungkin dibuat SATUSEHAT atau integrasi lain tetap dipertahankan.
     */
    private String buatPayloadUpdateServiceRequest(
            int baris, String idServiceRequest, String idDokter,
            String idPasien, HttpHeaders requestHeaders) throws Exception {
        JsonNode resourceSaatIni=ambilServiceRequestSatuSehat(
                idServiceRequest,requestHeaders,false);
        if(resourceSaatIni==null||!resourceSaatIni.isObject()){
            throw new IllegalStateException(
                    "ServiceRequest "+idServiceRequest+" tidak dapat dibaca untuk diperbarui");
        }

        JsonNode payloadLokal=mapper.readTree(
                buatPayloadServiceRequest(baris,idServiceRequest,idDokter,idPasien));
        com.fasterxml.jackson.databind.node.ObjectNode resourceUpdate=
                (com.fasterxml.jackson.databind.node.ObjectNode)resourceSaatIni.deepCopy();

        String[] elemenDikelola={
            "status","intent","priority","category","code","subject",
            "encounter","occurrenceDateTime","authoredOn","requester","performer"
        };
        for(String namaElemen:elemenDikelola){
            if(payloadLokal.has(namaElemen)){
                resourceUpdate.set(namaElemen,payloadLokal.path(namaElemen).deepCopy());
            }
        }
        if(payloadLokal.has("reasonCode")){
            resourceUpdate.set("reasonCode",payloadLokal.path("reasonCode").deepCopy());
        }

        String idOrganisasi=koneksiDB.IDSATUSEHAT();
        String systemServiceRequest="http://sys-ids.kemkes.go.id/servicerequest/"+idOrganisasi;
        String systemAcsn=systemIdentifierAcsn();
        com.fasterxml.jackson.databind.node.ArrayNode identifierGabungan=
                mapper.createArrayNode();
        for(JsonNode identifier:resourceSaatIni.path("identifier")){
            String system=identifier.path("system").asText();
            if(system.equals(systemServiceRequest)||system.equals(systemAcsn)){
                continue;
            }
            identifierGabungan.add(identifier.deepCopy());
        }
        for(JsonNode identifier:payloadLokal.path("identifier")){
            identifierGabungan.add(identifier.deepCopy());
        }

        resourceUpdate.put("resourceType","ServiceRequest");
        resourceUpdate.put("id",idServiceRequest);
        resourceUpdate.set("identifier",identifierGabungan);
        return mapper.writeValueAsString(resourceUpdate);
    }

    private String formatWaktuFHIRUtc(String waktuDatabase) {
        if(waktuDatabase==null||waktuDatabase.trim().isEmpty()){
            throw new IllegalStateException("Tanggal dan jam permintaan radiologi belum tersedia");
        }
        LocalDateTime waktuLokal=LocalDateTime.parse(waktuDatabase.trim(),FORMAT_WAKTU_DATABASE);
        return waktuLokal.atZone(ZONA_WAKTU_RS)
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(FORMAT_WAKTU_FHIR_UTC);
    }

    private String escapeJson(String nilai) {
        if(nilai==null){
            return "";
        }
        StringBuilder hasil=new StringBuilder();
        for(int index=0;index<nilai.length();index++){
            char karakter=nilai.charAt(index);
            switch(karakter){
                case '"':
                    hasil.append("\\\"");
                    break;
                case '\\':
                    hasil.append("\\\\");
                    break;
                case '\b':
                    hasil.append("\\b");
                    break;
                case '\f':
                    hasil.append("\\f");
                    break;
                case '\n':
                    hasil.append("\\n");
                    break;
                case '\r':
                    hasil.append("\\r");
                    break;
                case '\t':
                    hasil.append("\\t");
                    break;
                default:
                    if(karakter<32){
                        hasil.append(String.format("\\u%04x",(int)karakter));
                    }else{
                        hasil.append(karakter);
                    }
                    break;
            }
        }
        return hasil.toString();
    }

    /**
     * Menyiapkan ServiceRequest sebelum C-STORE dijalankan. Resource dibaca
     * langsung berdasarkan ID ServiceRequest lokal, identifier ACSN dipastikan
     * memakai noorder saja, kemudian diverifikasi kembali lewat pencarian
     * identifier yang dipakai oleh DICOM Router.
     */
    private void validasiServiceRequestUntukRouterDicom(
            int baris, String acsn, String idServiceRequest,
            HttpHeaders requestHeaders) throws Exception {
        String noPermintaan=nilaiTabel(baris,9);
        String kodePemeriksaan=nilaiTabel(baris,13);
        String acsnTerbaru=noPermintaan;
        String kunci=kunciServiceRequestAcsn(baris);
        String systemAcsn=systemIdentifierAcsn();

        if(!acsn.equals(acsnTerbaru)){
            serviceRequestAcsnPerluPerbaikan.add(kunci);
            throw new IllegalStateException("ACSN Orthanc wajib menggunakan noorder saja. Ditemukan "+
                    acsn+", seharusnya "+acsnTerbaru);
        }

        System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                "] Menyiapkan ServiceRequest "+idServiceRequest+
                " sebelum pengiriman DICOM");

        try{
            JsonNode resource=ambilServiceRequestSatuSehat(
                    idServiceRequest,requestHeaders,false);
            if(resource==null||!resource.isObject()){
                throw new IllegalStateException(
                        "ServiceRequest "+idServiceRequest+" tidak dapat dibaca dari SATUSEHAT");
            }
            if(!serviceRequestMemilikiKodePemeriksaan(resource,kodePemeriksaan)){
                throw new IllegalStateException(
                        "Kode pemeriksaan ServiceRequest "+idServiceRequest+
                        " tidak cocok dengan kode lokal "+kodePemeriksaan);
            }

            if(!serviceRequestIdentifierAcsnSudahTepat(resource,systemAcsn,acsnTerbaru)){
                String acsnSaatIni=daftarNilaiIdentifierAcsn(resource,systemAcsn);
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] ACSN ServiceRequest belum sesuai. Nilai saat ini: "+
                        (acsnSaatIni.isEmpty()?"(kosong)":acsnSaatIni));
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Memperbarui ACSN ServiceRequest menjadi "+acsnTerbaru);

                perbaruiIdentifierAcsnServiceRequest(
                        idServiceRequest,acsnSaatIni,acsnTerbaru,requestHeaders);

                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] Update ACSN ServiceRequest berhasil. Melakukan verifikasi ulang");
            }else{
                System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                        "] ACSN ServiceRequest sudah sesuai: "+acsnTerbaru+
                        ". Update tidak diperlukan");
            }

            String idVerifikasi=cariServiceRequestID(
                    baris,acsnTerbaru,requestHeaders);
            if(idVerifikasi.isEmpty()){
                throw new IllegalStateException(
                        "ServiceRequest belum dapat ditemukan berdasarkan ACSN terbaru "+
                        acsnTerbaru+" setelah proses persiapan");
            }
            if(!idServiceRequest.equals(idVerifikasi)){
                throw new IllegalStateException(
                        "ServiceRequest dengan ACSN "+acsnTerbaru+
                        " ditemukan menggunakan ID "+idVerifikasi+
                        ", berbeda dengan ID lokal "+idServiceRequest+
                        ". Pengiriman dibatalkan untuk mencegah relasi data yang salah");
            }

            serviceRequestAcsnPerluPerbaikan.remove(kunci);
            System.out.println("[ROUTER-DICOM] ["+noPermintaan+
                    "] ServiceRequest siap: "+idVerifikasi+
                    " | ACSN "+acsnTerbaru+
                    ". Melanjutkan pengiriman ke Router DICOM");
        }catch(Exception e){
            serviceRequestAcsnPerluPerbaikan.add(kunci);
            throw e;
        }
    }

    private boolean serviceRequestIdentifierAcsnSudahTepat(
            JsonNode resource, String systemAcsn, String acsnTerbaru) {
        int jumlah=0;
        for(JsonNode identifier:resource.path("identifier")){
            if(!systemAcsn.equals(identifier.path("system").asText())){
                continue;
            }
            jumlah++;
            if(!acsnTerbaru.equals(identifier.path("value").asText())){
                return false;
            }
        }
        return jumlah==1;
    }

    private String daftarNilaiIdentifierAcsn(
            JsonNode resource, String systemAcsn) {
        StringBuilder hasil=new StringBuilder();
        for(JsonNode identifier:resource.path("identifier")){
            if(!systemAcsn.equals(identifier.path("system").asText())){
                continue;
            }
            String nilai=identifier.path("value").asText().trim();
            if(nilai.isEmpty()){
                continue;
            }
            if(hasil.length()>0){
                hasil.append(", ");
            }
            hasil.append(nilai);
        }
        return hasil.toString();
    }

    private String cariServiceRequestID(int baris, HttpHeaders requestHeaders) throws Exception {
        return cariServiceRequestID(baris,nilaiTabel(baris,18),requestHeaders);
    }

    private String cariServiceRequestID(
            int baris, String acsn, HttpHeaders requestHeaders) throws Exception {
        String systemAcsn=systemIdentifierAcsn();
        String identifier=systemAcsn+"|"+acsn;
        String alamat=link+"/ServiceRequest?identifier="+URLEncoder.encode(identifier,"UTF-8")+"&_count=50";
        URI uri=new URI(alamat);
        HttpEntity<String> getEntity=new HttpEntity<>(requestHeaders);

        System.out.println("URL Cek ServiceRequest : "+alamat);
        String hasil=api.getRest().exchange(uri,HttpMethod.GET,getEntity,String.class).getBody();
        System.out.println("Result Cek ServiceRequest : "+hasil);

        JsonNode bundle=mapper.readTree(hasil);
        int jumlahServiceRequest=0;
        String kodePemeriksaan=nilaiTabel(baris,13);
        String idLokal=nilaiTabel(baris,16);
        String idPertamaCocok="";
        for(JsonNode entry:bundle.path("entry")){
            JsonNode resource=entry.path("resource");
            if(!resource.path("resourceType").asText().equals("ServiceRequest")){
                continue;
            }
            if(!serviceRequestMemilikiIdentifierAcsn(resource,systemAcsn,acsn)){
                continue;
            }
            String idResource=resource.path("id").asText();
            if(idResource.isEmpty()){
                continue;
            }
            jumlahServiceRequest++;
            if(serviceRequestMemilikiKodePemeriksaan(resource,kodePemeriksaan)){
                if(idResource.equals(idLokal)){
                    return idResource;
                }
                if(idPertamaCocok.isEmpty()){
                    idPertamaCocok=idResource;
                }
            }
        }

        if(!idPertamaCocok.isEmpty()){
            return idPertamaCocok;
        }
        if(jumlahServiceRequest>0){
            throw new IllegalStateException("Ditemukan "+jumlahServiceRequest+
                    " ServiceRequest dengan ACSN "+acsn+
                    ", tetapi tidak ada kode pemeriksaan yang cocok dengan "+kodePemeriksaan);
        }
        return "";
    }

    private String systemIdentifierAcsn() {
        return "http://sys-ids.kemkes.go.id/acsn/"+koneksiDB.IDSATUSEHAT();
    }

    private boolean serviceRequestMemilikiKodePemeriksaan(
            JsonNode resource, String kodePemeriksaan) {
        for(JsonNode coding:resource.path("code").path("coding")){
            if(coding.path("code").asText().equals(kodePemeriksaan)){
                return true;
            }
        }
        return false;
    }

    private JsonNode ambilServiceRequestSatuSehat(
            String idServiceRequest, HttpHeaders requestHeaders,
            boolean kembalikanNullJikaTidakAda) throws Exception {
        if(idServiceRequest==null||idServiceRequest.trim().isEmpty()){
            return null;
        }
        String alamat=link+"/ServiceRequest/"+
                URLEncoder.encode(idServiceRequest.trim(),"UTF-8");
        try{
            HttpEntity<String> getEntity=new HttpEntity<>(requestHeaders);
            String hasil=api.getRest().exchange(
                    new URI(alamat),HttpMethod.GET,getEntity,String.class).getBody();
            JsonNode resource=mapper.readTree(hasil);
            if(!resource.path("resourceType").asText().equals("ServiceRequest")){
                throw new IllegalStateException(
                        "Resource "+idServiceRequest+" bukan ServiceRequest");
            }
            return resource;
        }catch(HttpStatusCodeException e){
            if(kembalikanNullJikaTidakAda&&e.getStatusCode().value()==404){
                return null;
            }
            throw e;
        }
    }

    private void perbaruiIdentifierAcsnServiceRequest(
            String idServiceRequest, String acsnLama, String acsnTerbaru,
            HttpHeaders requestHeaders) throws Exception {
        JsonNode resource=ambilServiceRequestSatuSehat(
                idServiceRequest,requestHeaders,false);
        if(resource==null||!resource.isObject()){
            throw new IllegalStateException(
                    "ServiceRequest "+idServiceRequest+" tidak dapat dibaca untuk migrasi ACSN");
        }

        String systemAcsn=systemIdentifierAcsn();
        if(serviceRequestIdentifierAcsnSudahTepat(resource,systemAcsn,acsnTerbaru)){
            System.out.println("ServiceRequest "+idServiceRequest+
                    " sudah memakai ACSN terbaru "+acsnTerbaru+
                    ". PUT tidak diperlukan");
            return;
        }

        com.fasterxml.jackson.databind.node.ObjectNode resourceUpdate=
                (com.fasterxml.jackson.databind.node.ObjectNode)resource.deepCopy();
        com.fasterxml.jackson.databind.node.ArrayNode identifierBaru=
                mapper.createArrayNode();
        for(JsonNode identifier:resource.path("identifier")){
            if(systemAcsn.equals(identifier.path("system").asText())){
                continue;
            }
            identifierBaru.add(identifier.deepCopy());
        }

        com.fasterxml.jackson.databind.node.ObjectNode identifierAcsn=
                mapper.createObjectNode();
        identifierAcsn.put("use","usual");
        com.fasterxml.jackson.databind.node.ObjectNode type=
                mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ArrayNode coding=
                mapper.createArrayNode();
        com.fasterxml.jackson.databind.node.ObjectNode kodeAcsn=
                mapper.createObjectNode();
        kodeAcsn.put("system","http://terminology.hl7.org/CodeSystem/v2-0203");
        kodeAcsn.put("code","ACSN");
        coding.add(kodeAcsn);
        type.set("coding",coding);
        identifierAcsn.set("type",type);
        identifierAcsn.put("system",systemAcsn);
        identifierAcsn.put("value",acsnTerbaru);
        identifierBaru.add(identifierAcsn);

        resourceUpdate.put("id",idServiceRequest);
        resourceUpdate.set("identifier",identifierBaru);
        String payload=mapper.writeValueAsString(resourceUpdate);
        String alamat=link+"/ServiceRequest/"+
                URLEncoder.encode(idServiceRequest,"UTF-8");
        System.out.println("URL Update ACSN ServiceRequest : "+alamat);
        System.out.println("Request Update ACSN ServiceRequest : "+payload);
        HttpEntity<String> putEntity=new HttpEntity<>(payload,requestHeaders);
        String hasil=api.getRest().exchange(
                new URI(alamat),HttpMethod.PUT,putEntity,String.class).getBody();
        System.out.println("Result Update ACSN ServiceRequest : "+hasil);

        JsonNode hasilUpdate=mapper.readTree(hasil);
        if(!hasilUpdate.path("id").asText().equals(idServiceRequest)||
                !serviceRequestMemilikiIdentifierAcsn(
                        hasilUpdate,systemAcsn,acsnTerbaru)){
            throw new IllegalStateException(
                    "Respons update ServiceRequest belum mengonfirmasi ACSN terbaru "+acsnTerbaru);
        }
    }

    private boolean serviceRequestMemilikiIdentifierAcsn(
            JsonNode resource, String systemAcsn, String acsn) {
        for(JsonNode identifier:resource.path("identifier")){
            if(identifier.path("system").asText().equals(systemAcsn)&&
                    identifier.path("value").asText().equals(acsn)){
                return true;
            }
        }
        return false;
    }

    private void simpanServiceRequestLokal(int baris, String idServiceRequest) throws Exception {
        String noPermintaan=nilaiTabel(baris,9);
        String kodePemeriksaan=nilaiTabel(baris,17);
        boolean sudahAda;

        try(PreparedStatement psCek=koneksi.prepareStatement(
                "select id_servicerequest from satu_sehat_servicerequest_radiologi where noorder=? and kd_jenis_prw=? limit 1")){
            psCek.setString(1,noPermintaan);
            psCek.setString(2,kodePemeriksaan);
            try(ResultSet rsCek=psCek.executeQuery()){
                sudahAda=rsCek.next();
            }
        }

        if(sudahAda){
            try(PreparedStatement psUpdate=koneksi.prepareStatement(
                    "update satu_sehat_servicerequest_radiologi set id_servicerequest=? where noorder=? and kd_jenis_prw=?")){
                psUpdate.setString(1,idServiceRequest);
                psUpdate.setString(2,noPermintaan);
                psUpdate.setString(3,kodePemeriksaan);
                psUpdate.executeUpdate();
            }
        }else{
            try(PreparedStatement psInsert=koneksi.prepareStatement(
                    "insert into satu_sehat_servicerequest_radiologi (noorder,kd_jenis_prw,id_servicerequest) values (?,?,?)")){
                psInsert.setString(1,noPermintaan);
                psInsert.setString(2,kodePemeriksaan);
                psInsert.setString(3,idServiceRequest);
                psInsert.executeUpdate();
            }
        }

        tabMode.setValueAt(idServiceRequest,baris,16);
        tabMode.setValueAt(false,baris,0);
    }

    private void tampilkanErrorSatuSehat(String proses, HttpStatusCodeException error) {
        String responseBody=error.getResponseBodyAsString();
        System.out.println("Notifikasi Bridging "+proses+" : "+error);
        System.out.println("Response Error SATUSEHAT : "+responseBody);

        String detail=ringkasOperationOutcome(responseBody);
        if(detail.isEmpty()){
            detail=error.getMessage();
        }
        JOptionPane.showMessageDialog(this,
                proses+" gagal.\nStatus HTTP: "+error.getStatusCode()+"\n"+batasiPesan(detail,1200),
                "SATUSEHAT",JOptionPane.ERROR_MESSAGE);
    }

    private void tampilkanErrorProses(String proses, Exception error) {
        String detail=error.getMessage()==null?error.toString():error.getMessage();
        System.out.println("Notifikasi "+proses+" : "+error);
        JOptionPane.showMessageDialog(this,
                proses+" gagal.\n"+batasiPesan(detail,1200),
                "SATUSEHAT",JOptionPane.ERROR_MESSAGE);
    }

    private String ringkasOperationOutcome(String responseBody) {
        if(responseBody==null||responseBody.trim().isEmpty()){
            return "";
        }
        try{
            JsonNode errorRoot=mapper.readTree(responseBody);
            StringBuilder detail=new StringBuilder();
            for(JsonNode issue:errorRoot.path("issue")){
                String pesan=issue.path("details").path("text").asText();
                if(pesan.isEmpty()){
                    pesan=issue.path("diagnostics").asText();
                }
                if(pesan.isEmpty()){
                    pesan=issue.path("code").asText();
                }
                if(!pesan.isEmpty()){
                    if(detail.length()>0){
                        detail.append("\n");
                    }
                    String severity=issue.path("severity").asText();
                    if(!severity.isEmpty()){
                        detail.append("[").append(severity).append("] ");
                    }
                    detail.append(pesan);
                    JsonNode expressions=issue.path("expression");
                    if(expressions.isArray()&&expressions.size()>0){
                        detail.append(" (").append(expressions.get(0).asText()).append(")");
                    }
                }
            }
            if(detail.length()>0){
                return detail.toString();
            }
            String pesan=errorRoot.path("message").asText();
            if(!pesan.isEmpty()){
                return pesan;
            }
        }catch(Exception e){
            System.out.println("Gagal membaca OperationOutcome : "+e);
        }
        return responseBody.trim();
    }

    private String batasiPesan(String pesan, int panjangMaksimal) {
        if(pesan==null){
            return "";
        }
        if(pesan.length()<=panjangMaksimal){
            return pesan;
        }
        return pesan.substring(0,panjangMaksimal)+"...";
    }

    private String kunciImagingStudy(int baris) {
        return nilaiTabel(baris,9)+"|"+nilaiTabel(baris,17);
    }

    private String kunciRouterDicom(int baris) {
        return kunciImagingStudy(baris)+"|"+nilaiTabel(baris,18)+"|"+nilaiTabel(baris,20);
    }

    private String kunciServiceRequestAcsn(int baris) {
        return kunciImagingStudy(baris)+"|"+nilaiTabel(baris,16)+"|"+nilaiTabel(baris,18);
    }

    private int tahapAlur(int baris) {
        boolean adaServiceRequest=!nilaiTabel(baris,16).isEmpty()&&!nilaiTabel(baris,16).equals("-");
        boolean adaAcsn=!nilaiTabel(baris,18).isEmpty()&&!nilaiTabel(baris,18).equals("-");
        boolean adaImagingStudy=!nilaiTabel(baris,19).isEmpty()&&!nilaiTabel(baris,19).equals("-");
        boolean adaOrthancStudy=!nilaiTabel(baris,20).isEmpty()&&!nilaiTabel(baris,20).equals("-");

        if(adaImagingStudy){
            return 5;
        }
        if(!adaServiceRequest){
            return 1;
        }
        if(serviceRequestAcsnPerluPerbaikan.contains(kunciServiceRequestAcsn(baris))){
            return 2;
        }
        if(adaAcsn&&adaOrthancStudy){
            String kunci=kunciRouterDicom(baris);
            if(imagingStudySiapSinkron.containsKey(kunci)){
                return 4;
            }
            if(routerDicomDiterima.contains(kunci)){
                return 6;
            }
            return 3;
        }
        // ServiceRequest sudah ada, tetapi citra/study Orthanc belum tersedia.
        // Kondisi ini bukan kesalahan ServiceRequest dan tidak perlu PUT ulang.
        return 7;
    }

    private void aturTombolAlur() {
        if(!SwingUtilities.isEventDispatchThread()){
            SwingUtilities.invokeLater(() -> aturTombolAlur());
            return;
        }

        BtnKirim.setVisible(false);
        BtnUpdate.setVisible(false);
        BtnKirimRouterDicom.setVisible(false);
        BtnGetIDImagingStudiAuto.setVisible(false);
        BtnGetIDImagingStudiManual.setVisible(false);
        BtnKirimRouterDicom.setText("Kirim ke Router DICOM");
        BtnKirimRouterDicom.setToolTipText(
                "Kirim study Orthanc ke modality "+MODALITY_ROUTER_DICOM);

        if(ceksukses){
            panelGlass8.setToolTipText("Sedang memeriksa atau memproses data radiologi");
            panelGlass8.revalidate();
            panelGlass8.repaint();
            return;
        }

        int jumlahTerpilih=0;
        int tahapTerpilih=0;
        boolean tahapBerbeda=false;
        boolean semuaAutoGagal=true;
        for(int baris=0;baris<tabMode.getRowCount();baris++){
            if(Boolean.TRUE.equals(tabMode.getValueAt(baris,0))){
                int tahap=tahapAlur(baris);
                if(jumlahTerpilih==0){
                    tahapTerpilih=tahap;
                }else if(tahapTerpilih!=tahap){
                    tahapBerbeda=true;
                }
                semuaAutoGagal=semuaAutoGagal&&imagingStudyAutoGagal.contains(kunciImagingStudy(baris));
                jumlahTerpilih++;
            }
        }

        if(jumlahTerpilih==0){
            panelGlass8.setToolTipText("Pilih baris data untuk menampilkan tombol proses");
        }else if(tahapBerbeda){
            panelGlass8.setToolTipText("Pilihan memiliki tahapan berbeda. Pilih data dengan tahapan yang sama");
        }else{
            panelGlass8.setToolTipText(null);
            if(tahapTerpilih==1){
                BtnKirim.setVisible(true);
            }else if(tahapTerpilih==2){
                BtnUpdate.setVisible(true);
            }else if(tahapTerpilih==3){
                BtnKirimRouterDicom.setVisible(true);
            }else if(tahapTerpilih==4){
                BtnGetIDImagingStudiAuto.setVisible(true);
                BtnGetIDImagingStudiManual.setVisible(semuaAutoGagal);
            }else if(tahapTerpilih==6){
                BtnKirimRouterDicom.setText("Cek Status Router DICOM");
                BtnKirimRouterDicom.setToolTipText(
                        "Periksa apakah ImagingStudy sudah diterbitkan DICOM Router");
                BtnKirimRouterDicom.setVisible(true);
            }else if(tahapTerpilih==7){
                panelGlass8.setToolTipText(
                        "ServiceRequest sudah terkirim. Menunggu citra/study tersedia di Orthanc");
            }
        }

        panelGlass8.revalidate();
        panelGlass8.repaint();
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            String statusKirim=CmbStatus.getSelectedItem()==null?"Semua":CmbStatus.getSelectedItem().toString();
            String filterStatus="";
            if(statusKirim.equals("Sudah Terkirim")){
                filterStatus="and ifnull(satu_sehat_servicerequest_radiologi.id_servicerequest,'')<>'' ";
            }else if(statusKirim.equals("Belum Terkirim")){
                filterStatus="and ifnull(satu_sehat_servicerequest_radiologi.id_servicerequest,'')='' ";
            }
            ps=koneksi.prepareStatement(
                   "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,permintaan_radiologi.dokter_perujuk as kd_dokter,pegawai.nama,pegawai.no_ktp as ktpdokter,"+
                   "satu_sehat_encounter.id_encounter,permintaan_radiologi.noorder,permintaan_radiologi.tgl_permintaan,permintaan_radiologi.jam_permintaan,permintaan_radiologi.diagnosa_klinis,"+
                   "jns_perawatan_radiologi.nm_perawatan,satu_sehat_mapping_radiologi.code,satu_sehat_mapping_radiologi.system,satu_sehat_mapping_radiologi.display,"+
                   "ifnull(satu_sehat_servicerequest_radiologi.id_servicerequest,'') as id_servicerequest,permintaan_pemeriksaan_radiologi.kd_jenis_prw, "+
                   "coalesce(nullif(radiologi_pacs_token.accession_number,''),ifnull(satu_sehat_imagingstudy_radiologi.acsn,'')) as acsn, "+
                   "ifnull(satu_sehat_imagingstudy_radiologi.id_imaging,'') as id_imaging,ifnull(radiologi_pacs_token.orthanc_study_id,'') as orthanc_study_id "+
                   "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                   "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "+
                   "inner join permintaan_radiologi on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                   "inner join pegawai on pegawai.nik=permintaan_radiologi.dokter_perujuk "+
                   "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                   "inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "+
                   "inner join satu_sehat_mapping_radiologi on satu_sehat_mapping_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                   "left join satu_sehat_servicerequest_radiologi on satu_sehat_servicerequest_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "+
                   "and satu_sehat_servicerequest_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "+
                   "left join satu_sehat_imagingstudy_radiologi on satu_sehat_imagingstudy_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "+
                   "and satu_sehat_imagingstudy_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "+
                   "left join radiologi_pacs_token on radiologi_pacs_token.no_rawat=reg_periksa.no_rawat "+
                   "and radiologi_pacs_token.noorder=permintaan_pemeriksaan_radiologi.noorder "+
                   "and radiologi_pacs_token.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "+
                   "where reg_periksa.tgl_registrasi between ? and ? "+
                   filterStatus+
                   (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                   "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or jns_perawatan_radiologi.nm_perawatan like ? or "+
                   "satu_sehat_mapping_radiologi.code like ? or permintaan_radiologi.noorder like ?)"));
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                    ps.setString(9,"%"+TCari.getText()+"%");
                    ps.setString(10,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        false,rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("no_ktp"),rs.getString("kd_dokter"),
                        rs.getString("nama"),rs.getString("ktpdokter"),rs.getString("id_encounter"),rs.getString("noorder"),rs.getString("tgl_permintaan")+" "+rs.getString("jam_permintaan"),
                        rs.getString("diagnosa_klinis"),rs.getString("nm_perawatan"),rs.getString("code"),rs.getString("system"),rs.getString("display"),rs.getString("id_servicerequest"),
                        rs.getString("kd_jenis_prw"),rs.getString("acsn"),rs.getString("id_imaging"),rs.getString("orthanc_study_id")
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
        aturTombolAlur();
    }

    public void isCek(){
        BtnKirim.setEnabled(akses.getsatu_sehat_kirim_servicerequest_radiologi());
        BtnUpdate.setEnabled(akses.getsatu_sehat_kirim_servicerequest_radiologi());
        BtnKirimRouterDicom.setEnabled(akses.getsatu_sehat_kirim_servicerequest_radiologi());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_servicerequest_radiologi());
    }
    
    public JTable getTable(){
        return tbObat;
    }
    
    private String getImagingStudyID(String acsn) {
        return getImagingStudyID(acsn,true);
    }

    private String getImagingStudyID(String acsn, boolean tampilkanLog) {
        try {
            return cariImagingStudyIDSatuSehat(acsn,tampilkanLog);
        } catch (Exception e) {
            System.out.println("Notifikasi getImagingStudyID untuk ACSN "+acsn+" : " + e);
            return "";
        }
    }

    private String cariImagingStudyIDSatuSehat(
            String acsn, boolean tampilkanLog) throws Exception {
        String idImaging="";
        HttpHeaders imagingHeaders=new HttpHeaders();
        imagingHeaders.setContentType(MediaType.APPLICATION_JSON);
        imagingHeaders.add("Authorization","Bearer "+api.TokenSatuSehat());
        HttpEntity<String> imagingRequest=new HttpEntity<>(imagingHeaders);

        String identifier="http://sys-ids.kemkes.go.id/acsn/"+
                koneksiDB.IDSATUSEHAT()+"|"+acsn;
        String alamat=link+"/ImagingStudy?identifier="+
                URLEncoder.encode(identifier,"UTF-8");
        if(tampilkanLog){
            System.out.println("URL ImagingStudy : "+alamat);
        }

        String jsonResponse=api.getRest().exchange(
                new URI(alamat),HttpMethod.GET,imagingRequest,String.class).getBody();
        if(tampilkanLog){
            System.out.println("JSON ImagingStudy : "+jsonResponse);
        }

        JsonNode imagingRoot=mapper.readTree(jsonResponse);
        for(JsonNode entry:imagingRoot.path("entry")){
            String kandidat=entry.path("resource").path("id").asText();
            if(!kandidat.isEmpty()){
                idImaging=kandidat;
                break;
            }
        }
        if(tampilkanLog&&!idImaging.isEmpty()){
            System.out.println("ImagingStudy ID untuk ACSN "+acsn+" : "+idImaging);
        }
        return idImaging;
    }
    
    private void simpanImagingStudy(String noorder, String kdJenisPrw, String idServiceRequest, String acsn, String idImaging) {
        try {
            String sqlCheck = "SELECT id_imaging FROM satu_sehat_imagingstudy_radiologi WHERE noorder = ? AND kd_jenis_prw = ?";
            PreparedStatement psCheck = koneksi.prepareStatement(sqlCheck);
            psCheck.setString(1, noorder);
            psCheck.setString(2, kdJenisPrw);
            ResultSet rsCheck = psCheck.executeQuery();
            boolean exists = rsCheck.next();
            rsCheck.close();
            psCheck.close();

            if (exists) {
                String sqlUpdate = "UPDATE satu_sehat_imagingstudy_radiologi SET id_servicerequest = ?, acsn = ?, id_imaging = ? WHERE noorder = ? AND kd_jenis_prw = ?";
                PreparedStatement psUpdate = koneksi.prepareStatement(sqlUpdate);
                psUpdate.setString(1, idServiceRequest);
                psUpdate.setString(2, acsn);
                psUpdate.setString(3, idImaging);
                psUpdate.setString(4, noorder);
                psUpdate.setString(5, kdJenisPrw);
                psUpdate.executeUpdate();
                psUpdate.close();
                System.out.println("Update record ImagingStudy untuk noorder " + noorder);
            } else {
                String sqlInsert = "INSERT INTO satu_sehat_imagingstudy_radiologi (noorder, kd_jenis_prw, id_servicerequest, acsn, id_imaging) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psInsert = koneksi.prepareStatement(sqlInsert);
                psInsert.setString(1, noorder);
                psInsert.setString(2, kdJenisPrw);
                psInsert.setString(3, idServiceRequest);
                psInsert.setString(4, acsn);
                psInsert.setString(5, idImaging);
                psInsert.executeUpdate();
                psInsert.close();
                System.out.println("Insert record ImagingStudy untuk noorder " + noorder);
            }
        } catch (Exception e) {
            System.out.println("Error simpan ImagingStudy: " + e);
        }
    }
    
    private boolean runBackground(Runnable task) {
        if (ceksukses) return false;
        if (executor.isShutdown() || executor.isTerminated()) return false;
        if (!isDisplayable()) return false;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(() -> aturTombolAlur());

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                            aturTombolAlur();
                        }
                    });
                }
            });
            return true;
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
            return false;
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
