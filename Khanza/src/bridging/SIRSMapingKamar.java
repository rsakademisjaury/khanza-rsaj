/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgJnsPerawatanRalan.java
 *
 * Created on May 22, 2010, 11:58:21 PM
 */

package bridging;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import keuangan.DlgKamar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * @author dosen
 */
public final class SIRSMapingKamar extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;    
    private int i=0;
    private long currentTimestamp,offsetUTC7=0,timeStamp=0;
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private String link="",json="",iddokter="",idpasien="",totalbednya="",bedterpakainya="",timestampnya="",waktuUpdate="",kodeBangsal="";
    private JsonNode response,responsePesan,fasyankesArray,firstObject,secoundObject;
    private DlgKamar KamarRS=new DlgKamar(null,false);
    private AplicareCekReferensiKamarsirs KamarSIRS=new AplicareCekReferensiKamarsirs (null,false);
    private BPJSCekReferensiPoli polibpjs=new BPJSCekReferensiPoli(null,false);
    private ApiSatuSehat api=new ApiSatuSehat();
    

    /** Creates new form DlgJnsPerawatanRalan
     * @param parent
     * @param modal */
    public SIRSMapingKamar(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(8,1);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new Object[]{
            "Kode Kamar","Nama Kamar RS","Kelas RS","ID TT SIRS","Kelas SIRS","Ruang SIRS"}){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbJnsPerawatan.setModel(tabMode);

        tbJnsPerawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbJnsPerawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 6; i++) {
            TableColumn column = tbJnsPerawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(260);
            }else if(i==2){
                column.setPreferredWidth(100);
            }else if(i==3){
                column.setPreferredWidth(100);
            }else if(i==4){
                column.setPreferredWidth(160);
            }else if(i==5){
                column.setPreferredWidth(200);
            }
        }
        tbJnsPerawatan.setDefaultRenderer(Object.class, new WarnaTable());

        KdKamarRS.setDocument(new batasInput((byte)5).getKata(KdKamarRS)); 
        KdTTSIRS.setDocument(new batasInput((byte)15).getKata(KdTTSIRS)); 
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
        
        KamarRS.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(KamarRS.getTable().getSelectedRow()!= -1){                    
                    KdKamarRS.setText(KamarRS.getTable().getValueAt(KamarRS.getTable().getSelectedRow(),7).toString());
                    TkamarRS.setText(KamarRS.getTable().getValueAt(KamarRS.getTable().getSelectedRow(),3).toString());
                    kelaskamarRS.setText(KamarRS.getTable().getValueAt(KamarRS.getTable().getSelectedRow(),4).toString());
                }
                KdKamarRS.requestFocus();
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
        
        polibpjs.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(polibpjs.getTable().getSelectedRow()!= -1){                   
                    KdTTSIRS.setText(polibpjs.getTable().getValueAt(polibpjs.getTable().getSelectedRow(),1).toString());
                    NmRuangSIRS.setText(polibpjs.getTable().getValueAt(polibpjs.getTable().getSelectedRow(),2).toString());
                    KdTTSIRS.requestFocus();
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
        
        polibpjs.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    polibpjs.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        
        KamarSIRS.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(KamarSIRS.getTable().getSelectedRow()!= -1){   
                    KdTTSIRS.setText(KamarSIRS.getTable().getValueAt(KamarSIRS.getTable().getSelectedRow(),0).toString());
                    KdKelasSIRS.setText(KamarSIRS.getTable().getValueAt(KamarSIRS.getTable().getSelectedRow(),1).toString());
                    NmRuangSIRS.setText(KamarSIRS.getTable().getValueAt(KamarSIRS.getTable().getSelectedRow(),2).toString());
                    NmRuangSIRS.requestFocus();
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
        
        KamarSIRS.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    KamarSIRS.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });  
    
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbJnsPerawatan = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        KdKamarRS = new widget.TextBox();
        TkamarRS = new widget.TextBox();
        btnPoliRS = new widget.Button();
        jLabel19 = new widget.Label();
        KdTTSIRS = new widget.TextBox();
        NmRuangSIRS = new widget.TextBox();
        kelaskamarRS = new widget.TextBox();
        KdKelasSIRS = new widget.TextBox();
        btnKamarsirs = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Mapping Kamar SIRS ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbJnsPerawatan.setAutoCreateRowSorter(true);
        tbJnsPerawatan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbJnsPerawatan.setName("tbJnsPerawatan"); // NOI18N
        tbJnsPerawatan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJnsPerawatanMouseClicked(evt);
            }
        });
        tbJnsPerawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbJnsPerawatanKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbJnsPerawatan);

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

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(450, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
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
        jLabel7.setPreferredSize(new java.awt.Dimension(75, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 44));
        FormInput.setLayout(null);

        jLabel4.setText("Kamar RS :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 56, 23);

        KdKamarRS.setEditable(false);
        KdKamarRS.setHighlighter(null);
        KdKamarRS.setName("KdKamarRS"); // NOI18N
        FormInput.add(KdKamarRS);
        KdKamarRS.setBounds(59, 10, 70, 23);

        TkamarRS.setEditable(false);
        TkamarRS.setHighlighter(null);
        TkamarRS.setName("TkamarRS"); // NOI18N
        FormInput.add(TkamarRS);
        TkamarRS.setBounds(131, 10, 140, 23);

        btnPoliRS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPoliRS.setMnemonic('1');
        btnPoliRS.setToolTipText("Alt+1");
        btnPoliRS.setName("btnPoliRS"); // NOI18N
        btnPoliRS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPoliRSActionPerformed(evt);
            }
        });
        btnPoliRS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPoliRSKeyPressed(evt);
            }
        });
        FormInput.add(btnPoliRS);
        btnPoliRS.setBounds(390, 10, 28, 23);

        jLabel19.setText("Kamar SISR :");
        jLabel19.setName("jLabel19"); // NOI18N
        FormInput.add(jLabel19);
        jLabel19.setBounds(450, 10, 70, 23);

        KdTTSIRS.setEditable(false);
        KdTTSIRS.setHighlighter(null);
        KdTTSIRS.setName("KdTTSIRS"); // NOI18N
        FormInput.add(KdTTSIRS);
        KdTTSIRS.setBounds(530, 10, 70, 23);

        NmRuangSIRS.setEditable(false);
        NmRuangSIRS.setName("NmRuangSIRS"); // NOI18N
        FormInput.add(NmRuangSIRS);
        NmRuangSIRS.setBounds(720, 10, 160, 23);

        kelaskamarRS.setEditable(false);
        kelaskamarRS.setHighlighter(null);
        kelaskamarRS.setName("kelaskamarRS"); // NOI18N
        FormInput.add(kelaskamarRS);
        kelaskamarRS.setBounds(281, 10, 110, 23);

        KdKelasSIRS.setEditable(false);
        KdKelasSIRS.setName("KdKelasSIRS"); // NOI18N
        FormInput.add(KdKelasSIRS);
        KdKelasSIRS.setBounds(610, 10, 100, 23);

        btnKamarsirs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnKamarsirs.setMnemonic('1');
        btnKamarsirs.setToolTipText("Alt+1");
        btnKamarsirs.setName("btnKamarsirs"); // NOI18N
        btnKamarsirs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKamarsirsActionPerformed(evt);
            }
        });
        btnKamarsirs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnKamarsirsKeyPressed(evt);
            }
        });
        FormInput.add(btnKamarsirs);
        btnKamarsirs.setBounds(880, 10, 28, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPoliRSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPoliRSActionPerformed
        KamarRS.isCek();        
        KamarRS.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        KamarRS.setLocationRelativeTo(internalFrame1);
        KamarRS.setVisible(true);
}//GEN-LAST:event_btnPoliRSActionPerformed

    private void btnPoliRSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPoliRSKeyPressed
        
}//GEN-LAST:event_btnPoliRSKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(KdKamarRS.getText().trim().equals("")||TkamarRS.getText().trim().equals("")){
            Valid.textKosong(KdKamarRS,"Kode Kamar RS");
        }else if(KdTTSIRS.getText().trim().equals("")||NmRuangSIRS.getText().trim().equals("")){
            Valid.textKosong(KdTTSIRS,"Kode TT SIRS");
        }else{
            if(Sequel.menyimpantf("sirs_mapping_kamar","?,?,?,?","Mapping Kamar SIRS",4,new String[]{
                KdKamarRS.getText(),KdTTSIRS.getText(),KdKelasSIRS.getText(),NmRuangSIRS.getText()
            })==true){
                tampil();
                emptTeks();
            }                
        }
        
                for(i=0;i<tbJnsPerawatan.getRowCount();i++){
            if(tbJnsPerawatan.getValueAt(i,0).toString().equals("true")&&(!tbJnsPerawatan.getValueAt(i,4).toString().equals(""))&&(!tbJnsPerawatan.getValueAt(i,5).toString().equals(""))&&(!tbJnsPerawatan.getValueAt(i,6).toString().equals(""))){
                try {
            //        iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,8).toString());
              //      idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
              //      idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
                                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         
                Date now =new Date();
                
                          waktuUpdate = sdf.format(now);
                          currentTimestamp = System.currentTimeMillis() / 1000;
                          offsetUTC7 = 7 * 3600;
                          timeStamp = currentTimestamp + offsetUTC7;
                          timestampnya = String.valueOf(timeStamp);
                
                    try{
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    //    headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                        headers.add("X-rs-id", koneksiDB.IDSIRS());
                        headers.add("X-Timestamp", timestampnya);            
                        headers.add("X-pass",koneksiDB.PASSSIRS()); 
                        json = "{" +
                                "\"id_t_tt\" : "+tbJnsPerawatan.getValueAt(i, 4).toString()+"," +
                                "\"ruang\" : \""+tbJnsPerawatan.getValueAt(i, 6).toString()+"\"," +
                                "\"jumlah_ruang\" : \"0\","+
                                 "\"jumlah\" : \""+tbJnsPerawatan.getValueAt(i, 7).toString()+"\","+ 
                                 "\"terpakai\" : \""+tbJnsPerawatan.getValueAt(i, 8).toString()+"\","+
                                "\"terpakai_suspek\" : \"0\","+
                                "\"terpakai_konfirmasi\" : \"0\","+
                                "\"antrian\" : \"0\","+ 
                                "\"prepare\" : \"0\","+  
                                "\"prepare_plan\" : \"0\","+  
                                "\"covid\" : 0,"+   
                                "\"terpakai_dbd\" : \"0\","+   
                                "\"terpakai_dbd_anak\" : \"0\","+ 
                                "\"jumlah_dbd\" : \"0\"" +  
                                "}";
                        System.out.println("URL : "+link);
                        System.out.println("Request JSON : "+json);
                        requestEntity = new HttpEntity(json,headers);
                        json=api.getRest().exchange(link,HttpMethod.POST, requestEntity, String.class).getBody();
                        System.out.print(timestampnya);
                        System.out.println("Result JSON : "+json);
                        root = mapper.readTree(json);
                        
                        fasyankesArray = root.path("fasyankes");
                        
                        firstObject = fasyankesArray.get(0);
                        secoundObject = fasyankesArray.get(1);
                        
                        response = firstObject.path("status");
                        responsePesan = firstObject.path("message");
                        
                        kodeBangsal = tbJnsPerawatan.getValueAt(i, 1).toString();
                        if(Sequel.menyimpantf("sirs_mapping_kamar","?,?,?,?","Mapping Kamar SIRS",4,new String[]{
                                        KdKamarRS.getText(),KdTTSIRS.getText(),KdKelasSIRS.getText(),NmRuangSIRS.getText()
                                    })==true){
                                        tampil();
                                        emptTeks();
                        }          
                        
                    }catch(Exception e){
                        System.out.println("Notifikasi Bridging : "+e);
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                }
            }
        }
        tampil();
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit, BtnBatal);}
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
        Valid.hapusTable(tabMode,KdKamarRS,"sirs_mapping_kamar","kd_bangsal");
        tampil();
        emptTeks();
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(KdKamarRS.getText().trim().equals("")||TkamarRS.getText().trim().equals("")){
            Valid.textKosong(KdKamarRS,"Kode Kamar RS");
        }else if(KdTTSIRS.getText().trim().equals("")||NmRuangSIRS.getText().trim().equals("")){
            Valid.textKosong(KdTTSIRS,"Kode Kamar SIRS");
        }else{
            if(tbJnsPerawatan.getSelectedRow()>-1){
                if(Sequel.mengedittf("sirs_mapping_kamar","kd_bangsal=?","kd_bangsal=?,id_t_tt=?,tt_kelas=?,nm_ruang=?",5,new String[]{
                        KdKamarRS.getText(),KdTTSIRS.getText(),KdKelasSIRS.getText(),NmRuangSIRS.getText(),tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString()
                    })==true){
                    emptTeks();
                    tampil();
                }
            }                
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnEdit);
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
/**/
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
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbJnsPerawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJnsPerawatanMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbJnsPerawatanMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
        emptTeks();
    }//GEN-LAST:event_formWindowOpened

    private void tbJnsPerawatanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJnsPerawatanKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbJnsPerawatanKeyReleased

    private void btnKamarsirsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKamarsirsActionPerformed

        KamarSIRS.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        KamarSIRS.setLocationRelativeTo(internalFrame1);
        KamarSIRS.setVisible(true);
    }//GEN-LAST:event_btnKamarsirsActionPerformed

    private void btnKamarsirsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnKamarsirsKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnKamarsirsKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SIRSMapingKamar dialog = new SIRSMapingKamar(new javax.swing.JFrame(), true);
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
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KdKamarRS;
    private widget.TextBox KdKelasSIRS;
    private widget.TextBox KdTTSIRS;
    private widget.Label LCount;
    private widget.TextBox NmRuangSIRS;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TkamarRS;
    private widget.Button btnKamarsirs;
    private widget.Button btnPoliRS;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel19;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private widget.TextBox kelaskamarRS;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.Table tbJnsPerawatan;
    // End of variables declaration//GEN-END:variables

private void tampil() {
    Valid.tabelKosong(tabMode);
    try {
        ps = koneksi.prepareStatement(
            "select " +
            "    smk.kd_bangsal, " +
            "    ifnull(kmr.nm_bangsal,'') as nm_bangsal, " +
            "    ifnull(kmr.kelas,'') as kelas, " +
            "    ifnull(smk.id_t_tt,'') as id_t_tt, " +
            "    ifnull(smk.tt_kelas,'') as tt_kelas, " +
            "    ifnull(smk.nm_ruang,'') as nm_ruang " +
            "from sirs_mapping_kamar smk " +
            "left join ( " +
            "    select " +
            "        kamar_tt.kd_ruang, " +
            "        min(bangsal.nm_bangsal) as nm_bangsal, " +
            "        min(kamar.kelas) as kelas " +
            "    from kamar_tt " +
            "    inner join kamar on kamar.kd_kamar = kamar_tt.kd_kamar " +
            "    inner join bangsal on bangsal.kd_bangsal = kamar.kd_bangsal " +
            "    where kamar.statusdata='1' " +
            "    group by kamar_tt.kd_ruang " +
            ") kmr on kmr.kd_ruang = smk.kd_bangsal " +
            "where ( " +
            "    smk.kd_bangsal like ? or " +
            "    ifnull(kmr.nm_bangsal,'') like ? or " +
            "    ifnull(kmr.kelas,'') like ? or " +
            "    ifnull(smk.id_t_tt,'') like ? or " +
            "    ifnull(smk.tt_kelas,'') like ? or " +
            "    ifnull(smk.nm_ruang,'') like ? " +
            ") " +
            "order by smk.kd_bangsal"
        );

        try {
            String cari = "%" + TCari.getText().trim() + "%";
            ps.setString(1, cari);
            ps.setString(2, cari);
            ps.setString(3, cari);
            ps.setString(4, cari);
            ps.setString(5, cari);
            ps.setString(6, cari);

            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("kd_bangsal"),
                    rs.getString("nm_bangsal"),
                    rs.getString("kelas"),
                    rs.getString("id_t_tt"),
                    rs.getString("tt_kelas"),
                    rs.getString("nm_ruang")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif Ketersediaan : " + e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    }
    LCount.setText("" + tabMode.getRowCount());
}

    public void emptTeks() {
        KdKamarRS.setText("");
        TkamarRS.setText("");
        kelaskamarRS.setText("");
        KdTTSIRS.setText("");
        KdKelasSIRS.setText("");
        NmRuangSIRS.setText("");
        KdKamarRS.requestFocus();
    }

    private void getData() {
       if(tbJnsPerawatan.getSelectedRow()!= -1){
           KdKamarRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString());
           TkamarRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),1).toString());
           kelaskamarRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),2).toString());
           KdTTSIRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),3).toString());
           KdKelasSIRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),4).toString());
           NmRuangSIRS.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),5).toString());
        }
    }

}
