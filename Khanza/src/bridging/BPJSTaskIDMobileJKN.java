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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import keuangan.DlgBilingRalan;
import keuangan.DlgLhtPiutang;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * @author dosen
 */
public final class BPJSTaskIDMobileJKN extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    //private PreparedStatement ps;
    private PreparedStatement ps,psanak,psotomatis,psotomatis2,pskasir,pscaripiutang,psrekening;
   // private ResultSet rs; 
    private ResultSet rs, rs2,rskasir,rsrekening;
    private int i=0,sudah=0,adakelengkapan=0,tidakadakelengkapan=0;
    private ApiMobileJKN api=new ApiMobileJKN();
    private ApiMobileJKN apiMobileJKN=new ApiMobileJKN();
    private String URL="",link="",utc="",requestJson="",respon="200",stamp="",kelengkapan="";
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private JsonNode response;
    public DlgBilingRalan billing=new DlgBilingRalan(null,false);
    //private ApiBPJS api=new ApiBPJS();

    /** Creates new form DlgJnsPerawatanRalan
     * @param parent
     * @param modal */
    public BPJSTaskIDMobileJKN(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(8,1);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new Object[]{
                "No.Rawat","No.RM","Nama Pasien","No.HP","No.Kartu","NIK","Tanggal","Poliklinik","Dokter","Waktu RS","Waktu","Task Name","Task ID","Kelengkapan"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbJnsPerawatan.setModel(tabMode);

        tbJnsPerawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbJnsPerawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
            TableColumn column = tbJnsPerawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(83);
            }else if(i==4){
                column.setPreferredWidth(90);
            }else if(i==5){
                column.setPreferredWidth(103);
            }else if(i==6){
                column.setPreferredWidth(65);
            }else if(i==7){
                column.setPreferredWidth(140);
            }else if(i==8){
                column.setPreferredWidth(160);
            }else if(i==9){
                column.setPreferredWidth(115);
            }else if(i==10){
                column.setPreferredWidth(115);
            }else if(i==11){
                column.setPreferredWidth(150);
            }else if(i==12){
                column.setPreferredWidth(40);
            }else if(i==13){
                column.setPreferredWidth(40);
            }
        }
        tbJnsPerawatan.setDefaultRenderer(Object.class, new WarnaTable());

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
        
        try {
            link=koneksiDB.URLAPIMOBILEJKN();
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

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbJnsPerawatan = new widget.Table();
        panelGlass10 = new widget.panelisi();
        BtnUpdateTask6 = new widget.Button();
        BtnUpdateTask5 = new widget.Button();
        BtnUpdateTask = new widget.Button();
        BtnUpdateTask1 = new widget.Button();
        BtnUpdateTask2 = new widget.Button();
        BtnUpdateTask3 = new widget.Button();
        BtnUpdateTask4 = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        jLabel10 = new widget.Label();
        tgl2 = new widget.Tanggal();
        TStamp = new widget.TextBox();
        TBooking = new widget.TextBox();
        TTaskID = new widget.TextBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Task ID Mobile JKN ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbJnsPerawatan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbJnsPerawatan.setName("tbJnsPerawatan"); // NOI18N
        tbJnsPerawatan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJnsPerawatanMouseClicked(evt);
            }
        });
        tbJnsPerawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbJnsPerawatanKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbJnsPerawatan);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(65, 44));
        panelGlass10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        BtnUpdateTask6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask6.setMnemonic('K');
        BtnUpdateTask6.setText("2");
        BtnUpdateTask6.setToolTipText("Alt+K");
        BtnUpdateTask6.setIconTextGap(8);
        BtnUpdateTask6.setName("BtnUpdateTask6"); // NOI18N
        BtnUpdateTask6.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask6ActionPerformed(evt);
            }
        });
        BtnUpdateTask6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask6KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask6);

        BtnUpdateTask5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask5.setMnemonic('K');
        BtnUpdateTask5.setText("3");
        BtnUpdateTask5.setToolTipText("Alt+K");
        BtnUpdateTask5.setIconTextGap(8);
        BtnUpdateTask5.setName("BtnUpdateTask5"); // NOI18N
        BtnUpdateTask5.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask5ActionPerformed(evt);
            }
        });
        BtnUpdateTask5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask5KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask5);

        BtnUpdateTask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask.setMnemonic('K');
        BtnUpdateTask.setText("4");
        BtnUpdateTask.setToolTipText("Alt+K");
        BtnUpdateTask.setIconTextGap(8);
        BtnUpdateTask.setName("BtnUpdateTask"); // NOI18N
        BtnUpdateTask.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTaskActionPerformed(evt);
            }
        });
        BtnUpdateTask.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTaskKeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask);

        BtnUpdateTask1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask1.setMnemonic('K');
        BtnUpdateTask1.setText("5");
        BtnUpdateTask1.setToolTipText("Alt+K");
        BtnUpdateTask1.setIconTextGap(8);
        BtnUpdateTask1.setName("BtnUpdateTask1"); // NOI18N
        BtnUpdateTask1.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask1ActionPerformed(evt);
            }
        });
        BtnUpdateTask1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask1KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask1);

        BtnUpdateTask2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask2.setMnemonic('K');
        BtnUpdateTask2.setText("6");
        BtnUpdateTask2.setToolTipText("Alt+K");
        BtnUpdateTask2.setIconTextGap(8);
        BtnUpdateTask2.setName("BtnUpdateTask2"); // NOI18N
        BtnUpdateTask2.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask2ActionPerformed(evt);
            }
        });
        BtnUpdateTask2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask2KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask2);

        BtnUpdateTask3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask3.setMnemonic('K');
        BtnUpdateTask3.setText("7");
        BtnUpdateTask3.setToolTipText("Alt+K");
        BtnUpdateTask3.setIconTextGap(8);
        BtnUpdateTask3.setName("BtnUpdateTask3"); // NOI18N
        BtnUpdateTask3.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask3ActionPerformed(evt);
            }
        });
        BtnUpdateTask3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask3KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask3);

        BtnUpdateTask4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnUpdateTask4.setMnemonic('K');
        BtnUpdateTask4.setText("99");
        BtnUpdateTask4.setToolTipText("Alt+K");
        BtnUpdateTask4.setIconTextGap(8);
        BtnUpdateTask4.setName("BtnUpdateTask4"); // NOI18N
        BtnUpdateTask4.setPreferredSize(new java.awt.Dimension(60, 40));
        BtnUpdateTask4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateTask4ActionPerformed(evt);
            }
        });
        BtnUpdateTask4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateTask4KeyPressed(evt);
            }
        });
        panelGlass10.add(BtnUpdateTask4);

        internalFrame1.add(panelGlass10, java.awt.BorderLayout.EAST);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(30, 44));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(55, 23));

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "18-11-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "18-11-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(100, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });

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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(45, 23));

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(30, 23));

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(50, 23));
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

        jLabel10.setText("Waktu:");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(45, 23));
        jLabel10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jLabel10KeyPressed(evt);
            }
        });

        tgl2.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl2.setName("tgl2"); // NOI18N

        TStamp.setName("TStamp"); // NOI18N
        TStamp.setPreferredSize(new java.awt.Dimension(0, 23));
        TStamp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TStampKeyPressed(evt);
            }
        });

        TBooking.setName("TBooking"); // NOI18N
        TBooking.setPreferredSize(new java.awt.Dimension(0, 23));
        TBooking.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TBookingKeyPressed(evt);
            }
        });

        TTaskID.setName("TTaskID"); // NOI18N
        TTaskID.setPreferredSize(new java.awt.Dimension(0, 23));
        TTaskID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TTaskIDKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout panelGlass9Layout = new javax.swing.GroupLayout(panelGlass9);
        panelGlass9.setLayout(panelGlass9Layout);
        panelGlass9Layout.setHorizontalGroup(
            panelGlass9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGlass9Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(DTPCari1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(DTPCari2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(TCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(BtnCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(BtnAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(LCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(BtnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tgl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(TStamp, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TBooking, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TTaskID, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelGlass9Layout.setVerticalGroup(
            panelGlass9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGlass9Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(panelGlass9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DTPCari1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DTPCari2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelGlass9Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(tgl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGlass9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TStamp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TBooking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TTaskID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(32, 32, 32))
        );

        internalFrame1.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnCari,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

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
            Valid.pindah(evt, BtnCari, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TStampKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TStampKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TStampKeyPressed

    private void BtnUpdateTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTaskActionPerformed
        TaskID4();
    }//GEN-LAST:event_BtnUpdateTaskActionPerformed
     
    private void BtnUpdateTaskKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTaskKeyPressed
        // TODO add your handling code here
                if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnUpdateTaskActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
    }//GEN-LAST:event_BtnUpdateTaskKeyPressed

    private void TBookingKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBookingKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TBookingKeyPressed

    private void tbJnsPerawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJnsPerawatanMouseClicked
        // TODO add your handling code here:
        getDataTask();
    }//GEN-LAST:event_tbJnsPerawatanMouseClicked

    private void tbJnsPerawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJnsPerawatanKeyPressed
        // TODO add your handling code here:
        getDataTask();
    }//GEN-LAST:event_tbJnsPerawatanKeyPressed
    
    private void jLabel10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jLabel10KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel10KeyPressed

    private void BtnUpdateTask1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask1ActionPerformed
        // TODO add your handling code here:
        TaskID5();
    }//GEN-LAST:event_BtnUpdateTask1ActionPerformed

    private void BtnUpdateTask1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask1KeyPressed

    private void BtnUpdateTask2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask2ActionPerformed
        // TODO add your handling code here:
        TaskID6();
    }//GEN-LAST:event_BtnUpdateTask2ActionPerformed

    private void BtnUpdateTask2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask2KeyPressed

    private void BtnUpdateTask3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask3ActionPerformed
        // TODO add your handling code here:
        TaskID7();
    }//GEN-LAST:event_BtnUpdateTask3ActionPerformed

    private void BtnUpdateTask3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask3KeyPressed

    private void TTaskIDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TTaskIDKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TTaskIDKeyPressed

    private void BtnUpdateTask4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask4ActionPerformed
        // TODO add your handling code here:
        TaskID99();
    }//GEN-LAST:event_BtnUpdateTask4ActionPerformed

    private void BtnUpdateTask4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask4KeyPressed

    private void BtnUpdateTask5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask5ActionPerformed
        TaskID3();
    }//GEN-LAST:event_BtnUpdateTask5ActionPerformed

    private void BtnUpdateTask5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask5KeyPressed

    private void BtnUpdateTask6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateTask6ActionPerformed
       TaskID2();
    }//GEN-LAST:event_BtnUpdateTask6ActionPerformed

    private void BtnUpdateTask6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateTask6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateTask6KeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            BPJSTaskIDMobileJKN dialog = new BPJSTaskIDMobileJKN(new javax.swing.JFrame(), true);
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
    private widget.Button BtnUpdateTask;
    private widget.Button BtnUpdateTask1;
    private widget.Button BtnUpdateTask2;
    private widget.Button BtnUpdateTask3;
    private widget.Button BtnUpdateTask4;
    private widget.Button BtnUpdateTask5;
    private widget.Button BtnUpdateTask6;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.TextBox TBooking;
    private widget.TextBox TCari;
    private widget.TextBox TStamp;
    private widget.TextBox TTaskID;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass9;
    private widget.Table tbJnsPerawatan;
    private widget.Tanggal tgl2;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                   "SELECT reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_tlp,pasien.no_peserta,"+
                   "pasien.no_ktp,reg_periksa.tgl_registrasi,poliklinik.nm_poli,dokter.nm_dokter "+
                   "FROM reg_periksa INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                   "INNER JOIN poliklinik ON reg_periksa.kd_poli=poliklinik.kd_poli "+
                   "INNER JOIN dokter ON reg_periksa.kd_dokter=dokter.kd_dokter "+
                   "WHERE reg_periksa.tgl_registrasi BETWEEN ? AND ? "+(TCari.getText().equals("")?"":
                   "and (reg_periksa.no_rawat LIKE ? OR reg_periksa.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR "+
                   "pasien.no_tlp LIKE ? OR pasien.no_peserta LIKE ? OR pasien.no_ktp LIKE ? OR "+
                   "poliklinik.nm_poli LIKE ? OR dokter.nm_dokter LIKE ?) ")+
                   "order by reg_periksa.tgl_registrasi");
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().trim().equals("")){
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
                adakelengkapan=0;
                while(rs.next()){
                    kelengkapan=Sequel.cariIsi("select if(count(reg_periksa.no_rawat)>0,count(reg_periksa.no_rawat),'Tidak Ada') from reg_periksa where reg_periksa.no_rawat=?",rs.getString("no_rawat"));
                    if(kelengkapan.equals("Ada")){
                        adakelengkapan++;
                    }else{
                        tidakadakelengkapan++;
                    }
                    try {
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                        utc=String.valueOf(api.GetUTCdatetimeAsString());
                        headers.add("x-timestamp",utc);
                        headers.add("x-signature",api.getHmac(utc));
                        headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                        requestJson ="{" +
                                        "\"kodebooking\": \""+rs.getString("no_rawat")+"\"" +
                                     "}";
                        requestEntity = new HttpEntity(requestJson,headers);
                        URL = link+"/antrean/getlisttask";	
                        System.out.println("URL : "+URL);
                        System.out.println("JSON : "+requestJson);
                        root = mapper.readTree(api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                        nameNode = root.path("metadata");
                        if(nameNode.path("code").asText().equals("200")){
                            response = mapper.readTree(api.Decrypt(root.path("response").asText(),utc));
                            if(response.isArray()){
                                for(JsonNode list:response){
                                    tabMode.addRow(new Object[]{
                                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                                        rs.getString("no_tlp"),rs.getString("no_peserta"),rs.getString("no_ktp"),
                                        rs.getString("tgl_registrasi"),rs.getString("nm_poli"),rs.getString("nm_dokter"),
                                        list.path("wakturs").asText(),list.path("waktu").asText(),list.path("taskname").asText(),
                                        list.path("taskid").asText(),kelengkapan,
                                    });
                                }
                            }
                        }else {
                            System.out.println("Notif : "+nameNode.path("message").asText());               
                        }   
                    } catch (Exception ex) {
                        System.out.println("Notifikasi : "+ex);
                        if(ex.toString().contains("UnknownHostException")){
                            JOptionPane.showMessageDialog(rootPane,"Koneksi ke server BPJS terputus...!");
                        }
                    }
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
        
        try{
            ps=koneksi.prepareStatement(
                   "SELECT referensi_mobilejkn_bpjs.nobooking,reg_periksa.no_rkm_medis,pasien.nm_pasien,referensi_mobilejkn_bpjs.nohp,referensi_mobilejkn_bpjs.nomorkartu,"+
                   "referensi_mobilejkn_bpjs.nik,referensi_mobilejkn_bpjs.tanggalperiksa,poliklinik.nm_poli,dokter.nm_dokter "+
                   "FROM referensi_mobilejkn_bpjs INNER JOIN reg_periksa ON referensi_mobilejkn_bpjs.no_rawat=reg_periksa.no_rawat "+
                   "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                   "INNER JOIN poliklinik ON reg_periksa.kd_poli=poliklinik.kd_poli "+
                   "INNER JOIN dokter ON reg_periksa.kd_dokter=dokter.kd_dokter "+
                   "WHERE referensi_mobilejkn_bpjs.tanggalperiksa BETWEEN ? AND ? "+(TCari.getText().equals("")?"":
                   "and (referensi_mobilejkn_bpjs.nobooking LIKE ? OR reg_periksa.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR "+
                   "referensi_mobilejkn_bpjs.nohp LIKE ? OR referensi_mobilejkn_bpjs.nomorkartu LIKE ? OR referensi_mobilejkn_bpjs.nik LIKE ? OR "+
                   "poliklinik.nm_poli LIKE ? OR dokter.nm_dokter LIKE ?) ")+
                   "order by referensi_mobilejkn_bpjs.tanggalperiksa");
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().trim().equals("")){
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
                    kelengkapan=Sequel.cariIsi("select if(count(referensi_mobilejkn_bpjs.nobooking)>0,count(referensi_mobilejkn_bpjs.nobooking),'Tidak Ada') from referensi_mobilejkn_bpjs where referensi_mobilejkn_bpjs.nobooking=?",rs.getString("nobooking"));
                    if(kelengkapan.equals("Ada")){
                        adakelengkapan++;
                    }else{
                        tidakadakelengkapan++;
                    }
                    try {
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                        utc=String.valueOf(api.GetUTCdatetimeAsString());
                        headers.add("x-timestamp",utc);
                        headers.add("x-signature",api.getHmac(utc));
                        headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                        requestJson ="{" +
                                        "\"kodebooking\": \""+rs.getString("nobooking")+"\"" +
                                     "}";
                        requestEntity = new HttpEntity(requestJson,headers);
                        URL = link+"/antrean/getlisttask";	
                        System.out.println("URL : "+URL);
                        System.out.println("JSON : "+requestJson);
                        root = mapper.readTree(api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                        nameNode = root.path("metadata");
                        if(nameNode.path("code").asText().equals("200")){
                            response = mapper.readTree(api.Decrypt(root.path("response").asText(),utc));
                            if(response.isArray()){
                                for(JsonNode list:response){
                                    tabMode.addRow(new Object[]{
                                        rs.getString("nobooking"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                                        rs.getString("nohp"),rs.getString("nomorkartu"),rs.getString("nik"),
                                        rs.getString("tanggalperiksa"),rs.getString("nm_poli"),rs.getString("nm_dokter"),
                                        list.path("wakturs").asText(),list.path("waktu").asText(),list.path("taskname").asText(),
                                        list.path("taskid").asText(),kelengkapan
                                    });
                                }
                            }
                        }else {
                            System.out.println("Notif : "+nameNode.path("message").asText());                  
                        }   
                    } catch (Exception ex) {
                        System.out.println("Notifikasi : "+ex);
                        if(ex.toString().contains("UnknownHostException")){
                            JOptionPane.showMessageDialog(rootPane,"Koneksi ke server BPJS terputus...!");
                        }
                    }
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
    private void getDataTask() {
        if(tbJnsPerawatan.getSelectedRow()!= -1){
            TBooking.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString());
            TTaskID.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),12).toString()+1);
            TStamp.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),9).toString());
           // tgl2.setDate(tbJnsPerawatan.getDate(tbJnsPerawatan.getSelectedRow(),9));
//TeksArea.setText("\"kodebooking\": \""+TBooking.getText()+"\",\"taskid\":\""+TTaskID.getText()+1+"\",\"waktu\": \""+TStamp.getText()+"\"");
        }
    }
    private void getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	//String dateString = "22-03-2017 11:18:32";
        String dateString = tgl2.getSelectedItem().toString();
	try{
           //formatting the dateString to convert it into a Date 
	   Date date = sdf.parse(dateString);
	   //System.out.println("Given Time in milliseconds : "+date.getTime());

	   Calendar calendar = Calendar.getInstance();
	   //Setting the Calendar date and time to the given date and time
	   calendar.setTime(date);
	   System.out.println("Given Time in milliseconds : "+calendar.getTimeInMillis());
           Long stamplong=calendar.getTimeInMillis();
           String stamp=Long.toString(stamplong);
           TStamp.setText(stamp);
	}catch(ParseException e){
	   e.printStackTrace();
	 } 
    }
    private void getBilling(){
        try {
                    sudah=Sequel.cariInteger("select count(billing.no_rawat) from billing where billing.no_rawat=?",TBooking.getText());
                    pscaripiutang=koneksi.prepareStatement("select tgl_piutang from piutang_pasien where no_rkm_medis=? and status='Belum Lunas' order by tgl_piutang asc limit 1");
                    try{                                                
                        pscaripiutang.setString(1,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),1).toString());
                        rskasir=pscaripiutang.executeQuery();
                        if(rskasir.next()){
//                            i=JOptionPane.showConfirmDialog(null, "Masih ada tunggakan pembayaran, apa mau bayar sekarang ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
//                            if(i==JOptionPane.YES_OPTION){
                                 DlgLhtPiutang piutang=new DlgLhtPiutang(null,false);
                                 piutang.setNoRm(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),1).toString(),rskasir.getDate(1));
                                 piutang.tampil();
                                 piutang.isCek();
                                 piutang.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                                 piutang.setLocationRelativeTo(internalFrame1);
                                 piutang.setVisible(true);
//                            }else{
//                                if(akses.getbilling_ralan()==true){
//                                    //otomatisRalan();
//                                }
//                                  
//                                akses.setform("DlgKasirRalan");
//                                billing.TNoRw.setText(TBooking.getText());  
//                                billing.isCek();
//                                billing.isRawat(); 
//                                if(sudah>0){
//                                    billing.setPiutang();
//                                }
//                                billing.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
//                                billing.setLocationRelativeTo(internalFrame1);
//                                billing.setVisible(true);
//                            }
                        }else{
                            if(akses.getbilling_ralan()==true){
                               // otomatisRalan();
                            }
                            akses.setform("DlgKasirRalan");
                            billing.TNoRw.setText(TBooking.getText());  
                            billing.isCek();
                            billing.isRawat(); 
                            billing.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                            billing.setLocationRelativeTo(internalFrame1);
                            billing.setVisible(true);
                        }
                    }catch(Exception ex){
                        System.out.println("Notifikasi : "+ex);
                    } finally{
                        if(rskasir!=null){
                            rskasir.close();
                        }
                        if(pscaripiutang!=null){
                            pscaripiutang.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
    }
    private void TaskID2(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"2\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
    private void TaskID3(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"3\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
    private void TaskID4(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"4\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
     private void TaskID5(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"5\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
      private void TaskID6(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"6\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
     private void TaskID7(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"7\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }
     private void TaskID99(){
        respon="200";
         getTimeStamp();
                    if(!TTaskID.getText().equals("")){
                       try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
                            utc=String.valueOf(apiMobileJKN.GetUTCdatetimeAsString());
                            headers.add("x-timestamp",utc);
                            headers.add("x-signature",apiMobileJKN.getHmac(utc));
                            headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
                            requestJson ="{" +
                                                                     "\"kodebooking\": \""+TBooking.getText()+"\"," +
                                                                     "\"taskid\": \"99\"," +
                                                                     "\"waktu\": \""+TStamp.getText()+"\"" +
                                                                  "}";
                            System.out.println("JSON : "+requestJson+"\n");
                            requestEntity = new HttpEntity(requestJson,headers);
                            URL = koneksiDB.URLAPIMOBILEJKN()+"/antrean/updatewaktu";	
                            System.out.println("URL : "+URL);
                            root = mapper.readTree(apiMobileJKN.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody());
                            nameNode = root.path("metadata");  
                            respon=nameNode.path("code").asText();
                            System.out.println("respon WS BPJS Kirim Pakai NoRujukan : "+nameNode.path("code").asText()+" "+nameNode.path("message").asText()+"\n");
                            JOptionPane.showMessageDialog(null,""
                                    + " Respon Update TaskID "+nameNode.path("code").asText()+" "+nameNode.path("message").asText());
                       } catch (Exception e) {
                            //statusantrean=false;
                            System.out.println("Notif No.Rujuk : "+e);
                        }
                    } else{
                         JOptionPane.showMessageDialog(null,"Task ID Tidak Boleh Kosong.....!");
                    }
    }  
}
