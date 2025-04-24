package integration;

//import corona.*;
import bridging.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
//import fungsi.WarnaTableCorona;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;
import keuangan.DlgBilingRalan;
import keuangan.DlgLhtPiutang;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import simrskhanza.DlgKelurahan;

/**
 *
 * @author dosen
 */
public class DataPasienIntegration extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeResumIGD,tabModeResumRalan,tabModeResumRanap,tabMode1,tabMode2,tabMode3;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,psDokter,pscaripiutang;
    private ResultSet rs,rsDokter;
    private int a=0,b=0,c=0,d=0,e=0,f=0,g=0,h=0,i=0,j=0,k=0,l=0,m=0,n=0,o=0,z=0,zz=0,za=0,zb=0,zc=0,zd=0,ze=0,zf=0,zg=0,zh=0,zl=0,zm=0,zn=0,zo=0;
    private String aa,bb,cc,dd,ee,ff,gg,hh,ll,mm,nn,oo,pp;
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private CoronaReferensiJK jk=new CoronaReferensiJK(null,false);
    private CoronaReferensiKewarganegaraan kewarganegaraan=new CoronaReferensiKewarganegaraan(null,false);
    private CoronaReferensiSumberPenularan penularan=new CoronaReferensiSumberPenularan(null,false);
    private CoronaReferensiKecamatan kecamatan=new CoronaReferensiKecamatan(null,false);
//    private DataPasienSIMRS dataPasienSIMRS=new DataPasienSIMRS(null,false);
    private final GBRIntegration GbrIntegration=new GBRIntegration(null,false);
    private CoronaReferensiStatusRawat statusrawat=new CoronaReferensiStatusRawat(null,false);
    private CoronaReferensiStatusIsolasi statusisolasi=new CoronaReferensiStatusIsolasi(null,false);
    private CoronaReferensiPropinsi propinsi=new CoronaReferensiPropinsi(null,false);
    private CoronaReferensiKabupaten kabupaten=new CoronaReferensiKabupaten(null,false);
    private DlgKelurahan kelurahan=new DlgKelurahan(null,false);
    private ApiIntegration api=new ApiIntegration();
    private String jenis_pasien="",profesi="",link="",idrs="",body="",dokter="",comma="",y="",pass="";
    private HttpHeaders headers ;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response,pendaftaran,detailperiksa;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public DataPasienIntegration(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] rowDataRs={"","","","",""};
                
//            }){
//              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
//        };
        
        tabMode1=new DefaultTableModel(null,rowDataRs){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class ,java.lang.Object.class,java.lang.Object.class,java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDetailPasien.setModel(tabMode1);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbDetailPasien.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDetailPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 5; i++) {
            TableColumn column = tbDetailPasien.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(300);
            }         
        }
        tbDetailPasien.setDefaultRenderer(Object.class, new WarnaTable());
        tabModeResumIGD=new DefaultTableModel(null,new Object[]{"","","","",""
        }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class ,java.lang.Object.class,java.lang.Object.class,java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbResumeIGD.setModel(tabModeResumIGD);
        tbResumeIGD.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbResumeIGD.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 5; i++) {
            TableColumn column = tbResumeIGD.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(600);
            }
            
            
        }
        tbResumeIGD.setDefaultRenderer(Object.class, new WarnaTable());
        tabModeResumRalan=new DefaultTableModel(null,new Object[]{"","","","",""
        }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class ,java.lang.Object.class,java.lang.Object.class,java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbResumeRalan.setModel(tabModeResumRalan);
        tbResumeRalan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbResumeRalan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 5; i++) {
            TableColumn column = tbResumeRalan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(600);
            }
            
            
        }
        tbResumeRalan.setDefaultRenderer(Object.class, new WarnaTable());
         tabModeResumRanap=new DefaultTableModel(null,new Object[]{"","","","",""
        }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class ,java.lang.Object.class,java.lang.Object.class,java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbResumeRanap.setModel(tabModeResumRanap);
        tbResumeRanap.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbResumeRanap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 5; i++) {
            TableColumn column = tbResumeRanap.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(600);
            }
            
            
        }
        tbResumeRanap.setDefaultRenderer(Object.class, new WarnaTable());
        tabMode2=new DefaultTableModel(null,new Object[]{"","","","",""
        }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class ,java.lang.Object.class,java.lang.Object.class,java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDetailRad.setModel(tabMode2);
        tbDetailRad.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDetailRad.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 5; i++) {
            TableColumn column = tbDetailRad.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(600);
            }
            
            
        }
        tbDetailRad.setDefaultRenderer(Object.class, new WarnaTable());
        tabMode3=new DefaultTableModel(null,new Object[]{"","","","","","","","","","",""
        }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                 java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                 java.lang.Object.class
            };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDetailLab.setModel(tabMode3);
        tbDetailLab.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDetailLab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 11; i++) {
            TableColumn column = tbDetailLab.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(30);
            }else if(i==2){
                column.setPreferredWidth(300);
            }else if(i==3){
                column.setPreferredWidth(30);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(100);
            }else if(i==6){
                column.setPreferredWidth(100);
            }else if(i==7){
                column.setPreferredWidth(100);
            }else if(i==8){
                column.setPreferredWidth(100);
            }else if(i==9){
                column.setPreferredWidth(100);
            }else if(i==10){
                column.setPreferredWidth(100);
            }
            
            
        }
        tbDetailLab.setDefaultRenderer(Object.class, new WarnaTable());
        tabMode=new DefaultTableModel(null,new Object[]{
                "Jarak","Instansi","No KTP","Nama","Jenis Kelamin","Tempat Lahir","Tgl. Lahir","Alamat","No Kartu BPJS","Umur","Kewarganegaraan",
                "Source","kode_prov","Provinsi","kode_kab","Kabupaten/Kota","kec","Kecamatan",
                "Status Keluar","Kode Rs","Tgl Lapor","Status Rawat","Status Isolasi","Email","No Telepon","Jenis Pasien","Tgl Update",
                
                
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDataPasienTerintegrasi.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbDataPasienTerintegrasi.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDataPasienTerintegrasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 27; i++) {
            TableColumn column = tbDataPasienTerintegrasi.getColumnModel().getColumn(i);
            if(i==0){
                 column.setPreferredWidth(130);
            }else if(i==1){
                column.setPreferredWidth(130);
            }else if(i==2){
                column.setPreferredWidth(120);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(90);
            }else if(i==5){
                column.setPreferredWidth(100);
            }else if(i==6){
                column.setPreferredWidth(100);
            }else if(i==7){
                column.setPreferredWidth(200);
            }else if(i==8){
               column.setPreferredWidth(100);
            }else if(i==9){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==10){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==11){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==12){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==13){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==14){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==15){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==16){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==17){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==18){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==19){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==20){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==21){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==22){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==23){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==24){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==25){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==26){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }
        }
        tbDataPasienTerintegrasi.setDefaultRenderer(Object.class, new WarnaTable());
       
        Object[] rowDataDouble={"P","No Rawat", "No.KTP/Paspor","No.RM","Inisial","Nama Lengkap","Tgl. Masuk","Tgl. Keluar","Jenis Kelamin","Tgl.Lahir","Dokter Kontributor","Umur","Kewarganegaraan",
                "Source","kode_prov","Provinsi","kode_kab","Kabupaten/Kota","kec","Kecamatan",
                "Status Keluar","Kode Rs","Tgl Lapor","Status Rawat","Status Isolasi","Email","No Telepon","Jenis Pasien","Tgl Update",
                "id_allrecord","id_pasien"};
                
//            }){
//              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
//        };
       
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));

        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                      
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                       
                    }
                }
            });
        } 
        
        ChkInput.setSelected(false);
        isForm();
        
        try {
//            link=koneksiDB.URLAPIINTEGRATIONFASKES();
//            idrs=koneksiDB.IDINTEGRATIONFASKES();
//            pass=koneksiDB.PASSINTEGRATIONFASKES();
        } catch (Exception e) {
            System.out.println("E : "+e);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        idpasien = new widget.TextBox();
        status_keluar = new widget.TextBox();
        kd_instansi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        TabRawat = new javax.swing.JTabbedPane();
        DataPasienTerintegrasi = new widget.ScrollPane();
        tbDataPasienTerintegrasi = new widget.Table();
        riwayatResumeIGD = new widget.ScrollPane();
        tbResumeIGD = new widget.Table();
        riwayatResumeRalan = new widget.ScrollPane();
        tbResumeRalan = new widget.Table();
        riwayatResumeRanap = new widget.ScrollPane();
        tbResumeRanap = new widget.Table();
        DetailPasien = new widget.ScrollPane();
        tbDetailPasien = new widget.Table();
        DetailRad = new widget.ScrollPane();
        tbDetailRad = new widget.Table();
        DetailLab = new widget.ScrollPane();
        tbDetailLab = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass7 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel7 = new widget.Label();
        NoKTP = new widget.TextBox();
        jLabel8 = new widget.Label();
        DTPReg = new widget.Tanggal();

        idpasien.setHighlighter(null);
        idpasien.setName("idpasien"); // NOI18N
        idpasien.setPreferredSize(new java.awt.Dimension(150, 23));
        idpasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                idpasienKeyPressed(evt);
            }
        });

        status_keluar.setHighlighter(null);
        status_keluar.setName("status_keluar"); // NOI18N
        status_keluar.setPreferredSize(new java.awt.Dimension(150, 23));
        status_keluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                status_keluarKeyPressed(evt);
            }
        });

        kd_instansi.setName("kd_instansi"); // NOI18N
        kd_instansi.setPreferredSize(new java.awt.Dimension(207, 23));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Bridging Antar Fasilitas Kesehatan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1462, 801));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setBackground(new java.awt.Color(255, 255, 254));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(70, 70, 70));
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        DataPasienTerintegrasi.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        DataPasienTerintegrasi.setName("DataPasienTerintegrasi"); // NOI18N
        DataPasienTerintegrasi.setOpaque(true);

        tbDataPasienTerintegrasi.setAutoCreateRowSorter(true);
        tbDataPasienTerintegrasi.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbDataPasienTerintegrasi.setName("tbDataPasienTerintegrasi"); // NOI18N
        tbDataPasienTerintegrasi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDataPasienTerintegrasiMouseClicked(evt);
            }
        });
        tbDataPasienTerintegrasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDataPasienTerintegrasiKeyPressed(evt);
            }
        });
        DataPasienTerintegrasi.setViewportView(tbDataPasienTerintegrasi);

        TabRawat.addTab("Data Pasien Dari RS Terintegrasi", DataPasienTerintegrasi);

        riwayatResumeIGD.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        riwayatResumeIGD.setName("riwayatResumeIGD"); // NOI18N
        riwayatResumeIGD.setOpaque(true);

        tbResumeIGD.setAutoCreateRowSorter(true);
        tbResumeIGD.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbResumeIGD.setName("tbResumeIGD"); // NOI18N
        tbResumeIGD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbResumeIGDMouseClicked(evt);
            }
        });
        tbResumeIGD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbResumeIGDKeyPressed(evt);
            }
        });
        riwayatResumeIGD.setViewportView(tbResumeIGD);

        TabRawat.addTab("Riwayat Resume IGD", riwayatResumeIGD);

        riwayatResumeRalan.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        riwayatResumeRalan.setName("riwayatResumeRalan"); // NOI18N
        riwayatResumeRalan.setOpaque(true);

        tbResumeRalan.setAutoCreateRowSorter(true);
        tbResumeRalan.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbResumeRalan.setName("tbResumeRalan"); // NOI18N
        tbResumeRalan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbResumeRalanMouseClicked(evt);
            }
        });
        tbResumeRalan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbResumeRalanKeyPressed(evt);
            }
        });
        riwayatResumeRalan.setViewportView(tbResumeRalan);

        TabRawat.addTab("Riwayat Resume Ralan", riwayatResumeRalan);

        riwayatResumeRanap.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        riwayatResumeRanap.setName("riwayatResumeRanap"); // NOI18N
        riwayatResumeRanap.setOpaque(true);

        tbResumeRanap.setAutoCreateRowSorter(true);
        tbResumeRanap.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbResumeRanap.setName("tbResumeRanap"); // NOI18N
        tbResumeRanap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbResumeRanapMouseClicked(evt);
            }
        });
        tbResumeRanap.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbResumeRanapKeyPressed(evt);
            }
        });
        riwayatResumeRanap.setViewportView(tbResumeRanap);

        TabRawat.addTab("Riwayat Resume Ranap", riwayatResumeRanap);

        DetailPasien.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        DetailPasien.setName("DetailPasien"); // NOI18N
        DetailPasien.setOpaque(true);

        tbDetailPasien.setAutoCreateRowSorter(true);
        tbDetailPasien.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbDetailPasien.setName("tbDetailPasien"); // NOI18N
        tbDetailPasien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetailPasienMouseClicked(evt);
            }
        });
        tbDetailPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetailPasienKeyPressed(evt);
            }
        });
        DetailPasien.setViewportView(tbDetailPasien);

        TabRawat.addTab("Detail Data Pasien", DetailPasien);

        DetailRad.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        DetailRad.setName("DetailRad"); // NOI18N
        DetailRad.setOpaque(true);

        tbDetailRad.setAutoCreateRowSorter(true);
        tbDetailRad.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbDetailRad.setName("tbDetailRad"); // NOI18N
        tbDetailRad.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetailRadMouseClicked(evt);
            }
        });
        tbDetailRad.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetailRadKeyPressed(evt);
            }
        });
        DetailRad.setViewportView(tbDetailRad);

        TabRawat.addTab("Detail Data Radiologi", DetailRad);

        DetailLab.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        DetailLab.setName("DetailLab"); // NOI18N
        DetailLab.setOpaque(true);

        tbDetailLab.setAutoCreateRowSorter(true);
        tbDetailLab.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbDetailLab.setName("tbDetailLab"); // NOI18N
        tbDetailLab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetailLabMouseClicked(evt);
            }
        });
        tbDetailLab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetailLabKeyPressed(evt);
            }
        });
        DetailLab.setViewportView(tbDetailLab);

        TabRawat.addTab("Detail Data Laboratorium", DetailLab);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel10.setText("Record :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass8.add(jLabel10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(95, 23));
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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass7.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(360, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass7.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('7');
        BtnCari.setToolTipText("Alt+7");
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
        panelGlass7.add(BtnCari);

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
        panelGlass7.add(BtnAll);

        jPanel3.add(panelGlass7, java.awt.BorderLayout.CENTER);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 100));
        PanelInput.setRequestFocusEnabled(false);
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Input Data");
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

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(190, 40));
        FormInput.setLayout(null);

        jLabel7.setText("No.KTP :");
        jLabel7.setName("jLabel7"); // NOI18N
        FormInput.add(jLabel7);
        jLabel7.setBounds(0, 10, 110, 23);

        NoKTP.setEditable(false);
        NoKTP.setHighlighter(null);
        NoKTP.setName("NoKTP"); // NOI18N
        NoKTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NoKTPActionPerformed(evt);
            }
        });
        FormInput.add(NoKTP);
        NoKTP.setBounds(114, 10, 310, 23);

        jLabel8.setText("Tgl Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 40, 110, 23);

        DTPReg.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-07-2022" }));
        DTPReg.setDisplayFormat("dd-MM-yyyy");
        DTPReg.setName("DTPReg"); // NOI18N
        DTPReg.setOpaque(false);
        FormInput.add(DTPReg);
        DTPReg.setBounds(110, 40, 90, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");

}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
           
            TCari.setText("");
        }else{
//            Valid.pindah(evt, BtnCari, NamaPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

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
 if(TabRawat.getSelectedIndex()==0){
            emptTeks();
            tampilDataIntegrasi();
        }else if(TabRawat.getSelectedIndex()==1){
            emptTeks();
            tampilRiwayatResumeIGD();
        }else if(TabRawat.getSelectedIndex()==2){
            emptTeks();
            tampilRiwayatResumeRalan();
        }else if(TabRawat.getSelectedIndex()==3){
            emptTeks();
            tampilRiwayatResumeRanap();
        }else if(TabRawat.getSelectedIndex()==4){
//            emptTeks();
//            tampilDetailLab();
        }
          
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void idpasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idpasienKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_idpasienKeyPressed

    private void status_keluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_status_keluarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_status_keluarKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            emptTeks();
            tampilDataIntegrasi();
        }else if(TabRawat.getSelectedIndex()==1){
            emptTeks();
            tampilRiwayatResumeIGD();
        }else if(TabRawat.getSelectedIndex()==2){
            emptTeks();
            tampilRiwayatResumeRalan();
        }else if(TabRawat.getSelectedIndex()==3){
            emptTeks();
            tampilRiwayatResumeRanap();
        }else if(TabRawat.getSelectedIndex()==4){
//            emptTeks();
//            tampilDetailLab();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbDetailPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetailPasienKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailPasienKeyPressed

    private void tbDetailPasienMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetailPasienMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailPasienMouseClicked

    private void tbDataPasienTerintegrasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDataPasienTerintegrasiKeyPressed
        if(tabMode1.getRowCount()!=0){
            try {
                getDataPasienRs();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbDataPasienTerintegrasiKeyPressed

    private void tbDataPasienTerintegrasiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDataPasienTerintegrasiMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getDataPasienRs();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbDataPasienTerintegrasiMouseClicked

    private void tbDetailRadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetailRadMouseClicked
if(tabMode2.getRowCount()!=0){
            try {
            } catch (java.lang.NullPointerException e) {
            } if(evt.getClickCount()==2){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
           if(tbDetailRad.getValueAt(tbDetailRad.getSelectedRow(),4).toString().equals(""))
           {
               
           }else
           {
            GbrIntegration.setJudul("::[ Gambar Integration ]::","gbrpemeriksaan/pages");
            try {
               GbrIntegration.loadURL(tbDetailRad.getValueAt(tbDetailRad.getSelectedRow(),4).toString());
           } catch (Exception ex) {
                System.out.println("Notifikasi : "+ex);
            }

            GbrIntegration.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            GbrIntegration.setLocationRelativeTo(internalFrame1);
            GbrIntegration.setVisible(true);   
           }
            this.setCursor(Cursor.getDefaultCursor());   
            }
            
        }        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailRadMouseClicked

    private void tbDetailRadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetailRadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailRadKeyPressed

    private void tbDetailLabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetailLabMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailLabMouseClicked

    private void tbDetailLabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetailLabKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetailLabKeyPressed

    private void tbResumeRalanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbResumeRalanMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeRalanMouseClicked

    private void tbResumeRalanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbResumeRalanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeRalanKeyPressed

    private void tbResumeRanapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbResumeRanapMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeRanapMouseClicked

    private void tbResumeRanapKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbResumeRanapKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeRanapKeyPressed

    private void NoKTPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NoKTPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NoKTPActionPerformed

    private void tbResumeIGDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbResumeIGDMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeIGDMouseClicked

    private void tbResumeIGDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbResumeIGDKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbResumeIGDKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DataPasienIntegration dialog = new DataPasienIntegration(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPReg;
    private widget.ScrollPane DataPasienTerintegrasi;
    private widget.ScrollPane DetailLab;
    private widget.ScrollPane DetailPasien;
    private widget.ScrollPane DetailRad;
    private widget.PanelBiasa FormInput;
    private widget.Label LCount;
    private widget.TextBox NoKTP;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox TCari;
    private javax.swing.JTabbedPane TabRawat;
    private javax.swing.ButtonGroup buttonGroup1;
    public widget.TextBox idpasien;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private widget.TextBox kd_instansi;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.ScrollPane riwayatResumeIGD;
    private widget.ScrollPane riwayatResumeRalan;
    private widget.ScrollPane riwayatResumeRanap;
    public widget.TextBox status_keluar;
    private widget.Table tbDataPasienTerintegrasi;
    private widget.Table tbDetailLab;
    private widget.Table tbDetailPasien;
    private widget.Table tbDetailRad;
    private widget.Table tbResumeIGD;
    private widget.Table tbResumeRalan;
    private widget.Table tbResumeRanap;
    // End of variables declaration//GEN-END:variables
   
    private void tampilDataIntegrasi() {
       
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode);
        try{headers = new HttpHeaders();
                headers.add("x-koders",idrs); 
                headers.add("x-pass",pass); 
                 body ="{" +
                          "\"nik\": \""+NoKTP.getText()+"\"," +
                         "\"tgl_lahir\": \""+Valid.SetTgl(DTPReg.getSelectedItem()+"")+"\"" +
                         "}";
                requestEntity = new HttpEntity(body,headers);
                  System.out.println("E : "+link);
                root = mapper.readTree(api.getRest().exchange(link+"service/patient/datapribadi", HttpMethod.POST, requestEntity, String.class).getBody());
                 System.out.println("E : "+root);
                response = root.path("response").path("patient");
              
                if(response.isArray()){ 
                for(JsonNode list:response){
                    if(list.path("instansi").asText().toLowerCase().contains(TCari.getText().toLowerCase()))
                            {
                                    tabMode.addRow(new String[]{
                                      list.path("distance").asText(),list.path("instansi").asText(),list.path("no_ktp").asText(),list.path("nm_pasien").asText(),list.path("jk").asText(),list.path("tmp_lahir").asText(),list.path("tgl_lahir").asText(),list.path("alamat").asText(),list.path("no_peserta").asText()
                                   }); 
                              }
                }   
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, Belum Ada data di Fasyankes Lain yang terhubung");
        }
        LCount.setText(""+tabMode.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
    private void tampilRiwayatResumeIGD() {
       
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabModeResumIGD);
        try{headers = new HttpHeaders();
                headers.add("x-koders",idrs); 
                headers.add("x-pass",pass); 
                 body ="{" +
                          "\"nik\": \""+NoKTP.getText()+"\"," +
                         "\"tgl_lahir\": \""+Valid.SetTgl(DTPReg.getSelectedItem()+"")+"\"" +
                         "}";
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"service/patient/resumepasienigd", HttpMethod.POST, requestEntity, String.class).getBody());
                response = root.path("response");
//                System.out.println("Coba IGD"+response);

                if(response.isArray()){ 
                for(JsonNode list:response){
                    if(list.path("instansi").asText().toLowerCase().contains(TCari.getText().toLowerCase()))
                            {
                                    tabModeResumIGD.addRow(new String[]{
                                         "Fasyankes",":",list.path("instansi").asText(),"",""
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "No Rekam Medis",":",list.path("no_rkm_medis").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "Nama",":",list.path("nm_pasien").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "Tgl Lahir",":",list.path("tgl_lahir").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "Jenis Kelamin",":",list.path("jk").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Ruang/Poli",":",""
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Asal Pasien",":",list.path("dataResume").path("asal_pasien").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Anamnesa",":",list.path("dataResume").path("keluhan_utama").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Pemeriksaan Fisik",":",list.path("dataResume").path("jalannya_penyakit").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Pemeriksaan Radiologi",":",list.path("dataResume").path("pemeriksaan_penunjang").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Pemeriksaan LAB",":",list.path("dataResume").path("hasil_laborat").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Diagnosa Utama",":","("+list.path("dataResume").path("kd_diagnosa_utama").asText()+") "+list.path("dataResume").path("diagnosa_utama").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Diagnosa Sekunder 1",":","("+list.path("dataResume").path("kd_diagnosa_sekunder").asText()+") "+list.path("dataResume").path("diagnosa_sekunder").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Diagnosa Sekunder 2",":","("+list.path("dataResume").path("kd_diagnosa_sekunder2").asText()+") "+list.path("dataResume").path("diagnosa_sekunder2").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Diagnosa Sekunder 3",":","("+list.path("dataResume").path("kd_diagnosa_sekunder3").asText()+") "+list.path("dataResume").path("diagnosa_sekunder3").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Diagnosa Sekunder 4",":","("+list.path("dataResume").path("kd_diagnosa_sekunder4").asText()+") "+list.path("dataResume").path("diagnosa_sekunder4").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Prosedur Utama",":","("+list.path("dataResume").path("kd_prosedur_utama").asText()+") "+list.path("dataResume").path("prosedur_utama").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Prosedur Sekunder 1",":","("+list.path("dataResume").path("kd_prosedur_sekunder").asText()+") "+list.path("dataResume").path("prosedur_sekunder").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Prosedur Sekunder 2",":","("+list.path("dataResume").path("kd_prosedur_sekunder2").asText()+") "+list.path("dataResume").path("prosedur_sekunder2").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Prosedur Sekunder 3",":","("+list.path("dataResume").path("kd_prosedur_sekunder3").asText()+") "+list.path("dataResume").path("prosedur_sekunder3").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Tindakan Yang Dilakukan",":",list.path("dataResume").path("tindakan").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Terapi Pulang",":",list.path("dataResume").path("obat_pulang").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Kondisi Saat Pulang",":",list.path("dataResume").path("tindak_lanjut").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","","Rencana Tindak Lanjut ",":",list.path("dataResume").path("tindakan").asText()
                                   });
                                    tabModeResumIGD.addRow(new String[]{
                                         "","",""
                                   });
                              }
                }   
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        if(tabModeResumIGD.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, Belum Ada data di Fasyankes Lain yang terhubung");
        }
        LCount.setText(""+tabModeResumIGD.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
    
    private void tampilRiwayatResumeRalan() {
       
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabModeResumRalan);
        try{headers = new HttpHeaders();
                headers.add("x-koders",idrs); 
                headers.add("x-pass",pass); 
                 body ="{" +
                          "\"nik\": \""+NoKTP.getText()+"\"," +
                         "\"tgl_lahir\": \""+Valid.SetTgl(DTPReg.getSelectedItem()+"")+"\"" +
                         "}";
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"service/patient/resumepasienralan", HttpMethod.POST, requestEntity, String.class).getBody());
//                response = root.path("response").path("resumeRalan");
                response = root.path("response").path("resumeRalan");
//                System.out.println("Coba Ralan"+response);
                if(response.isArray()){ 
                for(JsonNode list:response){
                    if(list.path("instansi").asText().toLowerCase().contains(TCari.getText().toLowerCase()))
                            {
                                    tabModeResumRalan.addRow(new String[]{
                                         "Fasyankes",":",list.path("instansi").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "No Rekam Medis",":",list.path("no_rkm_medis").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "Nama",":",list.path("nm_pasien").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "Tgl Lahir",":",list.path("tgl_lahir").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "Jenis Kelamin",":",list.path("jk").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Ruang/Poli",":",""
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Asal Pasien",":",list.path("dataResume").path("asal_pasien").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Anamnesa",":",list.path("dataResume").path("keluhan_utama").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Pemeriksaan Fisik",":",list.path("dataResume").path("jalannya_penyakit").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Pemeriksaan Radiologi",":",list.path("dataResume").path("pemeriksaan_penunjang").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Pemeriksaan LAB",":",list.path("dataResume").path("hasil_laborat").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Diagnosa Utama",":","("+list.path("dataResume").path("kd_diagnosa_utama").asText()+") "+list.path("dataResume").path("diagnosa_utama").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Diagnosa Sekunder 1",":","("+list.path("dataResume").path("kd_diagnosa_sekunder").asText()+") "+list.path("dataResume").path("diagnosa_sekunder").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Diagnosa Sekunder 2",":","("+list.path("dataResume").path("kd_diagnosa_sekunder2").asText()+") "+list.path("dataResume").path("diagnosa_sekunder2").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Diagnosa Sekunder 3",":","("+list.path("dataResume").path("kd_diagnosa_sekunder3").asText()+") "+list.path("dataResume").path("diagnosa_sekunder3").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Diagnosa Sekunder 4",":","("+list.path("dataResume").path("kd_diagnosa_sekunder4").asText()+") "+list.path("dataResume").path("diagnosa_sekunder4").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Prosedur Utama",":","("+list.path("dataResume").path("kd_prosedur_utama").asText()+") "+list.path("dataResume").path("prosedur_utama").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Prosedur Sekunder 1",":","("+list.path("dataResume").path("kd_prosedur_sekunder").asText()+") "+list.path("dataResume").path("prosedur_sekunder").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Prosedur Sekunder 2",":","("+list.path("dataResume").path("kd_prosedur_sekunder2").asText()+") "+list.path("dataResume").path("prosedur_sekunder2").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Prosedur Sekunder 3",":","("+list.path("dataResume").path("kd_prosedur_sekunder3").asText()+") "+list.path("dataResume").path("prosedur_sekunder3").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Tindakan Yang Dilakukan",":",list.path("dataResume").path("tindakan").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Terapi Pulang",":",list.path("dataResume").path("obat_pulang").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Kondisi Saat Pulang",":",list.path("dataResume").path("tindak_lanjut").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Rencana Tindak Lanjut ",":",list.path("dataResume").path("tindakan").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Tanggal Kontrol ",":",list.path("dataResume").path("tanggal").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","","Kontrol Di ",":",list.path("dataResume").path("di").asText()
                                   });
                                    tabModeResumRalan.addRow(new String[]{
                                         "","",""
                                   });
                              }
                }   
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        if(tabModeResumRalan.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, Belum Ada data di Fasyankes Lain yang terhubung");
        }
        LCount.setText(""+tabModeResumRalan.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
    private void tampilRiwayatResumeRanap() {
       
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabModeResumRanap);
        try{headers = new HttpHeaders();
                headers.add("x-koders",idrs); 
                headers.add("x-pass",pass); 
                 body ="{" +
                          "\"nik\": \""+NoKTP.getText()+"\"," +
                         "\"tgl_lahir\": \""+Valid.SetTgl(DTPReg.getSelectedItem()+"")+"\"" +
                         "}";
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"service/patient/resumepasienranap", HttpMethod.POST, requestEntity, String.class).getBody());
                response = root.path("response").path("resumeRanap");
                if(response.isArray()){ 
                for(JsonNode list:response){
                    if(list.path("instansi").asText().toLowerCase().contains(TCari.getText().toLowerCase()))
                            {
//                                    tabMode.addRow(new String[]{
//                                      list.path("distance").asText(),list.path("instansi").asText(),list.path("no_ktp").asText(),list.path("nm_pasien").asText(),list.path("jk").asText(),list.path("tmp_lahir").asText(),list.path("tgl_lahir").asText(),list.path("alamat").asText(),list.path("no_peserta").asText()
//                                   }); 
                                    tabModeResumRanap.addRow(new String[]{
                                         "Fasyankes",":",list.path("instansi").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "No Rekam Medis",":",list.path("no_rkm_medis").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "Nama",":",list.path("nm_pasien").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "Tgl Lahir",":",list.path("tgl_lahir").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "Jenis Kelamin",":",list.path("jk").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dokter DPJP",":",list.path("dataResume").path("nm_dokter1").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Tanggal Masuk",":",list.path("dataResume").path("masuk").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Tanggal Keluar",":",list.path("dataResume").path("keluar").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Awal",":",list.path("dataResume").path("diagnosa_awal").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Alasan Masuk",":",list.path("dataResume").path("alasan").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Ringkasan Riwayat Penyakit",":",list.path("dataResume").path("keluhan_utama").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Pemeriksaan Fisik",":",list.path("dataResume").path("jalannya_penyakit").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Pemeriksaan Radiologi",":",list.path("dataResume").path("pemeriksaan_penunjang").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Pemeriksaan LAB",":",list.path("dataResume").path("hasil_laborat").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Terapi Pengobatan Selama DI Rumah Sakit",":",list.path("dataResume").path("terapi").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Utama",":","("+list.path("dataResume").path("kd_diagnosa_utama").asText()+") "+list.path("dataResume").path("diagnosa_utama").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Sekunder 1",":","("+list.path("dataResume").path("kd_diagnosa_sekunder").asText()+") "+list.path("dataResume").path("diagnosa_sekunder").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Sekunder 2",":","("+list.path("dataResume").path("kd_diagnosa_sekunder2").asText()+") "+list.path("dataResume").path("diagnosa_sekunder2").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Sekunder 3",":","("+list.path("dataResume").path("kd_diagnosa_sekunder3").asText()+") "+list.path("dataResume").path("diagnosa_sekunder3").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diagnosa Sekunder 4",":","("+list.path("dataResume").path("kd_diagnosa_sekunder4").asText()+") "+list.path("dataResume").path("diagnosa_sekunder4").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Prosedur Utama",":","("+list.path("dataResume").path("kd_prosedur_utama").asText()+") "+list.path("dataResume").path("prosedur_utama").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Prosedur Sekunder 1",":","("+list.path("dataResume").path("kd_prosedur_sekunder").asText()+") "+list.path("dataResume").path("prosedur_sekunder").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Prosedur Sekunder 2",":","("+list.path("dataResume").path("kd_prosedur_sekunder2").asText()+") "+list.path("dataResume").path("prosedur_sekunder2").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Prosedur Sekunder 3",":","("+list.path("dataResume").path("kd_prosedur_sekunder3").asText()+") "+list.path("dataResume").path("prosedur_sekunder3").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Alergi (Reaksi Obat)",":",list.path("dataResume").path("alergi").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Diet",":",list.path("dataResume").path("diet").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Hasil LAB Belum Selesai",":",list.path("dataResume").path("lab_belum").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Edukasi",":",list.path("dataResume").path("edukasi").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Cara Keluar",":",list.path("dataResume").path("cara_keluar").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Cara Keluar Lainnya",":",list.path("dataResume").path("keluar_lainnya").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Keadaan Pulang",":",list.path("dataResume").path("Keadaan").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Keadaan Pulang Lainnya",":",list.path("dataResume").path("Keadaan_lainnya").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Pengobatan Dilanjutkan",":",list.path("dataResume").path("pengobatan").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Pengobatan Dilanjutkan Lainnya",":",list.path("dataResume").path("pengobatan_lainnya").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Tanggal & Jam Kontrol",":",list.path("dataResume").path("kontrol").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama1").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang1").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis1").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama2").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang2").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis2").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama3").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang3").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis3").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama4").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang4").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis4").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama5").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang5").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis5").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama6").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang6").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis6").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama7").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang7").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis7").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama8").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang8").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis8").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Nama Obat",":",list.path("dataResume").path("nama9").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Jumlah",":",list.path("dataResume").path("jml_barang9").asText()
                                   });
                                    tabModeResumRanap.addRow(new String[]{
                                         "","","Dosis",":",list.path("dataResume").path("dosis9").asText()
                                   });
                              }
                }   
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        if(tabModeResumRanap.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, Belum Ada data di Fasyankes Lain yang terhubung");
        }
        LCount.setText(""+tabModeResumRanap.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
    private void tampilDetailPasien(){
        
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode1);
        try{    
           
             headers = new HttpHeaders();
                headers.add("x-koders",idrs);
//                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("x-pass",pass); 
                 body ="{" +
                         "\"id\": \""+kd_instansi.getText()+"\"," +
                         "\"nik\": \""+NoKTP.getText()+"\"" +
                         "}";
                
                
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"pendaftaran", HttpMethod.POST, requestEntity, String.class).getBody());
                response = root.path("data");
               //  System.out.println("Data Integrasi pas : "+response);
               
                
                 tabMode1.addRow(new String[]{
                     "Nama",":",response.path("nm_pasien").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "Jenis Kelamin",":",response.path("jk").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "Tempat Lahir",":",response.path("tmp_lahir").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "Tanggal Lahir",":",response.path("tgl_lahir").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "Alamat",":",response.path("alamat").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "No Kartu BPJS",":",response.path("no_peserta").asText()
                                   });
                 tabMode1.addRow(new String[]{
                     "Data Pendaftaran",":",""
                                   });
                   pendaftaran= response.path("pendaftaran");
//                   System.out.println("Data Integrasi pas : "+pendaftaran);
                 a=1; if(pendaftaran.isArray()){
                        for(JsonNode list:pendaftaran){
                           
                             tabMode1.addRow(new String[]{
                                "",a+".","Tgl. Registrasi",":",list.path("tgl_registrasi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Nama Poli",":",list.path("nm_poli").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Nama Dokter",":",list.path("nm_dokter").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Suhu Tubuh",":",list.path("suhu_tubuh").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Tensi",":",list.path("tensi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Nadi",":",list.path("nadi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Respirasi",":",list.path("respirasi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","GCS",":",list.path("gcs").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Tinggi",":",list.path("tinggi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Berat",":",list.path("berat").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Alergi",":",list.path("alergi").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Suhu",":",list.path("suhu").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Keluhan",":",list.path("keluhan").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Pemeriksaan",":",list.path("pemeriksaan").asText()
                                              });
                             tabMode1.addRow(new String[]{
                                "","","Penilaian",":",list.path("penilaian").asText()
                                              });
                            
                             a++;
                        }
                      
                      
                  }
                
                 
                 
              
                                    
                
            
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode1.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
            
    private void  tampilDetailRad()
 {
     this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode2);
        try{    
           

            headers = new HttpHeaders();
                headers.add("x-koders",idrs);
//                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("x-pass",pass); 
                 body ="{" +
                         "\"id\": \""+kd_instansi.getText()+"\"," +
                         "\"nik\": \""+NoKTP.getText()+"\"" +
                         "}";
                
                
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"radiologi", HttpMethod.POST, requestEntity, String.class).getBody());
                response = root.path("data");
               //  System.out.println("Data Integrasi pas : "+response);
               
                
                 tabMode2.addRow(new String[]{
                     "Nama",":",response.path("nm_pasien").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "Jenis Kelamin",":",response.path("jk").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "Tempat Lahir",":",response.path("tmp_lahir").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "Tanggal Lahir",":",response.path("tgl_lahir").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "Alamat",":",response.path("alamat").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "No Kartu BPJS",":",response.path("no_peserta").asText()
                                   });
                 tabMode2.addRow(new String[]{
                     "Data Pemeriksaan",":",""
                                   });
                   pendaftaran= response.path("pemeriksaan");
//                   System.out.println("Data Integrasi pas : "+pendaftaran);
                 a=1; if(pendaftaran.isArray()){
                        for(JsonNode list:pendaftaran){
                           
                             tabMode2.addRow(new String[]{
                                "",a+".","Tgl. Periksa",":",list.path("tgl_periksa").asText()
                                              });
                             tabMode2.addRow(new String[]{
                                "","","Jam",":",list.path("jam").asText()
                                              });
                             tabMode2.addRow(new String[]{
                                "","","Kode Pemeriksaan",":",list.path("kd_jenis_prw").asText()
                                              });
                             tabMode2.addRow(new String[]{
                                "","","Nama Pemeriksaan",":",list.path("nm_perawatan").asText()
                                              });
                             tabMode2.addRow(new String[]{
                                "","","Hasil",":",list.path("hasil").asText()
                                              });
                              tabMode2.addRow(new String[]{
                                "","","Foto",":",list.path("hasil").asText()
                                              });
                             detailperiksa= list.path("foto");
//                   System.out.println("Data Integrasi pas : "+pendaftaran);
                    b=1; if(detailperiksa.isArray()){
                        for(JsonNode listFoto:detailperiksa){
                            if(listFoto.path("path").asText().equals("")){
                               tabMode2.addRow(new String[]{
                                "","","Foto Rontgen",":","",""
                                              }); 
                            }else
                            {
                            byte[] decoded = Base64.getDecoder().decode(listFoto.path("path").asText());
                             tabMode2.addRow(new String[]{
                                "","","Foto Rontgen",":","Clik 2x untuk tampilkan Foto",new String(decoded)
                                              });
                            }
                            
                        }
                        }
                            
                             a++;
                        }
                      
                      
                  }
                
                 
                 
              
                                    
                
            
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode2.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
 }
    
 private void  tampilDetailLab()
 {
     this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode3);
        try{    
          
            headers = new HttpHeaders();
                headers.add("x-koders",idrs);
//                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("x-pass",pass); 
                 body ="{" +
                         "\"id\": \""+kd_instansi.getText()+"\"," +
                         "\"nik\": \""+NoKTP.getText()+"\"" +
                         "}";
                
                
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"lab", HttpMethod.POST, requestEntity, String.class).getBody());
                response = root.path("data");
               //  System.out.println("Data Integrasi pas : "+response);
               
                
                 tabMode3.addRow(new String[]{
                     "Nama",":",response.path("nm_pasien").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "Jenis Kelamin",":",response.path("jk").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "Tempat Lahir",":",response.path("tmp_lahir").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "Tanggal Lahir",":",response.path("tgl_lahir").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "Alamat",":",response.path("alamat").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "No Kartu BPJS",":",response.path("no_peserta").asText(),"","","",""
                                   });
                 tabMode3.addRow(new String[]{
                     "Data Pemeriksaan",":",""
                                   });
                   pendaftaran= response.path("pemeriksaan_lab");
                   
//                   System.out.println("Data Integrasi pas : "+pendaftaran);
                 a=1; if(pendaftaran.isArray()){
                        for(JsonNode list:pendaftaran){
                           
                             tabMode3.addRow(new String[]{
                                "",a+".","Tgl. Periksa",":",list.path("tgl_periksa").asText(),"",""
                                              });
                             tabMode3.addRow(new String[]{
                                "","","Jam",":",list.path("jam").asText(),"",""
                                              });
                             tabMode3.addRow(new String[]{
                                "","","Kode Pemeriksaan",":",list.path("kd_jenis_prw").asText(),"",""
                                              });
                             tabMode3.addRow(new String[]{
                                "","","Nama Pemeriksaan",":",list.path("nm_perawatan").asText(),"",""
                                              });
                             a++;
                        
                        detailperiksa= list.path("detail_periksa");
//                        System.out.println("Data Integrasi pas : "+detailperiksa);
                        tabMode3.addRow(new String[]{
                                "","","","","Tgl. Periksa","Jam Periksa","Id Template","Nama Pemeriksaan","Nilai","Nilai Rujukan","Keterangan"
                                              });
                        b=1; if(detailperiksa.isArray()){
                        for(JsonNode listDetail:detailperiksa){
                           
                             tabMode3.addRow(new String[]{
                                "","","",b+".",listDetail.path("tgl_periksa").asText(),listDetail.path("jam").asText(),listDetail.path("id_template").asText(),
                                 listDetail.path("pemeriksaan").asText(),listDetail.path("nilai").asText(),listDetail.path("nilai_rujukan").asText(),list.path("keterangan").asText()
                                              });
                             
                             b++;
                        }
                        }
                      
                      
                  }
                
                 
                 
              
                                    
                 }    
            
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode2.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
 }  
    public void emptTeks() {

//        NamaPasien.setText("");
//        NoKTP.setText("");
//        Inisial.setText("");
//        NoRkmMedis.setText("");
//        TglLahir.setText("");
//        noRawat.setText("");
//        Inisial.requestFocus();
//        TglMasuk.setText("");
//        TglKeluar.setText("");
//        TglUpdate.setText("");
//        TKontributorDokter.setText("");
//        cmbJenisPasienView.setSelectedIndex(0);
        idpasien.setText("");
    }
     
     
     
    
    
    private void getDataPasienRs() {
      if(tbDataPasienTerintegrasi.getSelectedRow()!= -1){ 
//            instansi.setText(tbDataPasienTerintegrasi.getValueAt(tbDataPasienTerintegrasi.getSelectedRow(),1).toString());
//            kd_instansi.setText(tbDataPasienTerintegrasi.getValueAt(tbDataPasienTerintegrasi.getSelectedRow(),0).toString());
        if(tbDataPasienTerintegrasi.getValueAt(tbDataPasienTerintegrasi.getSelectedRow(),1).toString().equals(""))
        {
        
        }else
        {
          
        }
        
      } 
      
    }
    
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,100));
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

          
        }   
    }
    
    public void setPasien(String nomr){
        ChkInput.setSelected(true);
        isForm(); 
        try {
            ps=koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.kd_kel,kelurahan.nm_kel,pasien.no_ktp,pasien.jk, "+
                    "date_format(pasien.tgl_lahir,'%Y-%m-%d') as tgl_lahir,pasien.email,pasien.no_tlp,kecamatan.nm_kec,kabupaten.nm_kab,propinsi.nm_prop "+
                    "from pasien inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel "+
                    "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                    "inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab "+
                    "inner join propinsi on pasien.kd_prop=propinsi.kd_prop where pasien.no_rkm_medis=?");
            try {            
                ps.setString(1,nomr);
                rs=ps.executeQuery();
                if(rs.next()){
                    NoKTP.setText(rs.getString("no_ktp"));

//                    NamaPasien.setText(rs.getString("nm_pasien"));
//                    NoRkmMedis.setText(rs.getString("jk").replaceAll("L","Laki-Laki").replaceAll("P","Perempuan"));
                    Valid.SetTgl(DTPReg,rs.getString("tgl_lahir"));
                   
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }finally{
                if(rs != null ){
                    rs.close();
                }
                if(ps != null ){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
     public void SetJenisPasien(String jenis_pasien){
        this.jenis_pasien=jenis_pasien;
    }

}
