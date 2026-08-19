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
public class DataInstansiTerintegrasi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
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
    public DataInstansiTerintegrasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        
        Object[] rowDataRs={"",""};
                
//            }){
//              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
//        };
        
       
        
       
        tabMode=new DefaultTableModel(null,new Object[]{
                "kd_instansi","Instansi","Username","Password","Secret Key","link","no_mou","latitude","longitude","distance"
                
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDataInstansiTerintegrasi.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbDataInstansiTerintegrasi.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDataInstansiTerintegrasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbDataInstansiTerintegrasi.getColumnModel().getColumn(i);
             if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(130);
            }else{
                 column.setPreferredWidth(130);
            }
        }
        tbDataInstansiTerintegrasi.setDefaultRenderer(Object.class, new WarnaTable());
       
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));

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
        distance = new widget.TextBox();
        status_keluar = new widget.TextBox();
        kd_instansi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        TabRawat = new javax.swing.JTabbedPane();
        DataInstansiTerintegrasi = new widget.ScrollPane();
        tbDataInstansiTerintegrasi = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnKeluar1 = new widget.Button();
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
        txt_username = new widget.TextBox();
        jLabel8 = new widget.Label();
        txt_nmInstansi = new widget.TextBox();
        jLabel9 = new widget.Label();
        txt_password = new widget.TextBox();
        jLabel11 = new widget.Label();
        txt_secretkey = new widget.TextBox();
        txt_linkws = new widget.TextBox();
        jLabel12 = new widget.Label();
        txt_noMou = new widget.TextBox();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        txt_latitude = new widget.TextBox();
        txt_longitude = new widget.TextBox();
        jLabel15 = new widget.Label();

        distance.setHighlighter(null);
        distance.setName("distance"); // NOI18N
        distance.setPreferredSize(new java.awt.Dimension(150, 23));
        distance.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                distanceKeyPressed(evt);
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), ".:[ Data Fasilitas Kesehatan Terintegrasi ]:.", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10), new java.awt.Color(50, 50, 50))); // NOI18N
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

        DataInstansiTerintegrasi.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        DataInstansiTerintegrasi.setName("DataInstansiTerintegrasi"); // NOI18N
        DataInstansiTerintegrasi.setOpaque(true);

        tbDataInstansiTerintegrasi.setAutoCreateRowSorter(true);
        tbDataInstansiTerintegrasi.setToolTipText("Klik data di table, kemudian klik kanan untuk memilih menu yang diinginkan");
        tbDataInstansiTerintegrasi.setName("tbDataInstansiTerintegrasi"); // NOI18N
        tbDataInstansiTerintegrasi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDataInstansiTerintegrasiMouseClicked(evt);
            }
        });
        tbDataInstansiTerintegrasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDataInstansiTerintegrasiKeyPressed(evt);
            }
        });
        DataInstansiTerintegrasi.setViewportView(tbDataInstansiTerintegrasi);

        TabRawat.addTab("Data Instansi Terintegrasi", DataInstansiTerintegrasi);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnKeluar1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnKeluar1.setMnemonic('K');
        BtnKeluar1.setText("Simpan");
        BtnKeluar1.setToolTipText("Alt+K");
        BtnKeluar1.setName("BtnKeluar1"); // NOI18N
        BtnKeluar1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluar1ActionPerformed(evt);
            }
        });
        BtnKeluar1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluar1KeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar1);

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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 150));
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
        FormInput.setPreferredSize(new java.awt.Dimension(190, 100));
        FormInput.setLayout(null);

        jLabel7.setText("Username :");
        jLabel7.setName("jLabel7"); // NOI18N
        FormInput.add(jLabel7);
        jLabel7.setBounds(0, 40, 100, 23);

        txt_username.setHighlighter(null);
        txt_username.setName("txt_username"); // NOI18N
        FormInput.add(txt_username);
        txt_username.setBounds(100, 40, 260, 23);

        jLabel8.setText("Nama Instansi :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 10, 100, 23);

        txt_nmInstansi.setHighlighter(null);
        txt_nmInstansi.setName("txt_nmInstansi"); // NOI18N
        FormInput.add(txt_nmInstansi);
        txt_nmInstansi.setBounds(100, 10, 260, 23);

        jLabel9.setText("Password :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 70, 100, 23);

        txt_password.setHighlighter(null);
        txt_password.setName("txt_password"); // NOI18N
        FormInput.add(txt_password);
        txt_password.setBounds(100, 70, 260, 23);

        jLabel11.setText("Secret Key :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(0, 100, 100, 23);

        txt_secretkey.setHighlighter(null);
        txt_secretkey.setName("txt_secretkey"); // NOI18N
        FormInput.add(txt_secretkey);
        txt_secretkey.setBounds(100, 100, 260, 23);

        txt_linkws.setHighlighter(null);
        txt_linkws.setName("txt_linkws"); // NOI18N
        FormInput.add(txt_linkws);
        txt_linkws.setBounds(470, 10, 260, 23);

        jLabel12.setText("Link Web Service :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(370, 10, 100, 23);

        txt_noMou.setHighlighter(null);
        txt_noMou.setName("txt_noMou"); // NOI18N
        FormInput.add(txt_noMou);
        txt_noMou.setBounds(470, 40, 260, 23);

        jLabel13.setText("No MOU :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(370, 40, 100, 23);

        jLabel14.setText("Latitude Maps :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(370, 70, 100, 23);

        txt_latitude.setHighlighter(null);
        txt_latitude.setName("txt_latitude"); // NOI18N
        FormInput.add(txt_latitude);
        txt_latitude.setBounds(470, 70, 260, 23);

        txt_longitude.setHighlighter(null);
        txt_longitude.setName("txt_longitude"); // NOI18N
        FormInput.add(txt_longitude);
        txt_longitude.setBounds(470, 100, 260, 23);

        jLabel15.setText("Longitude Maps :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(370, 100, 100, 23);

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
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil();
            TCari.setText("");
        }else{
//            Valid.pindah(evt, BtnCari, NamaPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
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
            tampil();
        }
          
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void distanceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_distanceKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_distanceKeyPressed

    private void status_keluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_status_keluarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_status_keluarKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            emptTeks();
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbDataInstansiTerintegrasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDataInstansiTerintegrasiKeyPressed
        
    }//GEN-LAST:event_tbDataInstansiTerintegrasiKeyPressed

    private void tbDataInstansiTerintegrasiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDataInstansiTerintegrasiMouseClicked
       
    }//GEN-LAST:event_tbDataInstansiTerintegrasiMouseClicked

    private void BtnKeluar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluar1ActionPerformed
         if(txt_nmInstansi.getText().trim().equals("")){
            Valid.textKosong(txt_nmInstansi,"Nama Instansi");
        }else if(txt_nmInstansi.getText().trim().equals("")){
            Valid.textKosong(txt_nmInstansi,"Nama Instansi");
        }else{
            updateDistance();
             Sequel.menyimpantf2("fasyankesintegration_user","?,?,?,?,?,?,?,?,?,?","No.Rawat",10,
                new String[]{null,txt_nmInstansi.getText(),txt_username.getText(),txt_password.getText(),txt_secretkey.getText(),txt_linkws.getText(),txt_noMou.getText(),
                    txt_latitude.getText(),txt_longitude.getText(),distance.getText()});
         }
        emptTeks();     
        tampil();     
    }//GEN-LAST:event_BtnKeluar1ActionPerformed

    private void BtnKeluar1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluar1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnKeluar1KeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DataInstansiTerintegrasi dialog = new DataInstansiTerintegrasi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar1;
    private widget.CekBox ChkInput;
    private widget.ScrollPane DataInstansiTerintegrasi;
    private widget.PanelBiasa FormInput;
    private widget.Label LCount;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox TCari;
    private javax.swing.JTabbedPane TabRawat;
    private javax.swing.ButtonGroup buttonGroup1;
    public widget.TextBox distance;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.TextBox kd_instansi;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    public widget.TextBox status_keluar;
    private widget.Table tbDataInstansiTerintegrasi;
    private widget.TextBox txt_latitude;
    private widget.TextBox txt_linkws;
    private widget.TextBox txt_longitude;
    private widget.TextBox txt_nmInstansi;
    private widget.TextBox txt_noMou;
    private widget.TextBox txt_password;
    private widget.TextBox txt_secretkey;
    private widget.TextBox txt_username;
    // End of variables declaration//GEN-END:variables
    private void tampil()
    {
     Valid.tabelKosong(tabMode);
       this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode);
        try {
            ps=koneksi.prepareStatement("select * from fasyankesintegration_user where  instansi like ? "); 
            try{  
                ps.setString(1,"%"+TCari.getText()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[] {
                        rs.getString("id"),rs.getString("instansi"),rs.getString("username"),rs.getString("password"),rs.getString("secretkey"),
                        rs.getString("link"),rs.getString("no_mou"),rs.getString("latitude"),rs.getString("longitude"),rs.getString("distance")+" Km"                 
                    });
                }                    
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
         this.setCursor(Cursor.getDefaultCursor());
         emptTeks();
    }
   
    public void emptTeks() {
        txt_nmInstansi.setText("");
        txt_username.setText("");
        txt_password.setText("");
        txt_secretkey.setText("");
        txt_linkws.setText("");
        txt_noMou.setText("");
        txt_latitude.setText("");
        txt_longitude.setText("");
        distance.setText("");
    }
    
    private void getData() {
     
    }
    private void updateDistance() {
     try{headers = new HttpHeaders();
                headers.add("x-koders",idrs);
//                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("x-pass",pass); 
                 body ="{" +
                         "\"latitude\": \""+txt_latitude.getText()+"\"," +
                         "\"longitude\": \""+txt_longitude.getText()+"\""+ 
                         "}";
                
                
                requestEntity = new HttpEntity(body,headers);
                root = mapper.readTree(api.getRest().exchange(link+"service/instansi/distance", HttpMethod.POST, requestEntity, String.class).getBody());
                distance.setText(root.path("distance").asText());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
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
       
    }
    
    public void setPasien(String nomr){
        ChkInput.setSelected(true);
        isForm(); 
        try {
            ps=koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.kd_kel,kelurahan.nm_kel,pasien.no_ktp,pasien.jk, "+
                    "date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,pasien.email,pasien.no_tlp,kecamatan.nm_kec,kabupaten.nm_kab,propinsi.nm_prop "+
                    "from pasien inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel "+
                    "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                    "inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab "+
                    "inner join propinsi on pasien.kd_prop=propinsi.kd_prop where pasien.no_rkm_medis=?");
            try {            
                ps.setString(1,nomr);
                rs=ps.executeQuery();
                if(rs.next()){
                    txt_username.setText(rs.getString("no_ktp"));

//                    NamaPasien.setText(rs.getString("nm_pasien"));
//                    NoRkmMedis.setText(rs.getString("jk").replaceAll("L","Laki-Laki").replaceAll("P","Perempuan"));
//                    TglLahir.setText(rs.getString("tgl_lahir"));
                   
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
