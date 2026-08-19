/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
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
import java.awt.event.KeyEvent;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dosen
 */
public final class ApotekBPJSDaftarPelayananObat2 extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private validasi Valid=new validasi();
    private sekuel Sequel=new sekuel();
    private int i=0, y=0,reply=0;
    private ApiApotekBPJS api=new ApiApotekBPJS();
    private String URL="",link="",utc="",requestJson="";
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private JsonNode response;
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2,rs3;

    /** Creates new form DlgKamar
     * @param parent
     * @param modal */
    public ApotekBPJSDaftarPelayananObat2(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10,2);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new String[]{
                "No.SEP Apotek","No.SEP Asal","No.Resep","No.Kartu","Nama Peserta","Kode Jenis","Jenis Obat","Tgl.Pelayanan",
                "Kode Obat","Nama Obat","Tipe Obat","Signa 1","Signa 2","Racikan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKamar.setModel(tabMode);

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbKamar.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKamar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
            TableColumn column = tbKamar.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(160);
            }else if(i==1){
                column.setPreferredWidth(160);
            }else if(i==2){
                column.setPreferredWidth(100);
            }else if(i==3){
                column.setPreferredWidth(120);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(65);
            }else if(i==6){
                column.setPreferredWidth(180);
            }else if(i==7){
                column.setPreferredWidth(90);
            }else if(i==8){
                column.setPreferredWidth(90);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(70);
            }else if(i==11){
                column.setPreferredWidth(70);
            }else if(i==12){
                column.setPreferredWidth(70);
            }else if(i==13){
                column.setPreferredWidth(70);
            }
        }
        tbKamar.setDefaultRenderer(Object.class, new WarnaTable());
        
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
            link=koneksiDB.URLAPIAPOTEKBPJS();
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
        tbKamar = new widget.Table();
        panelGlass6 = new widget.panelisi();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        jLabel17 = new widget.Label();
        LCount = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        BtnHapus1 = new widget.Button();
        BtnHapus2 = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Obat Apotek BPJS ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbKamar.setAutoCreateRowSorter(true);
        tbKamar.setName("tbKamar"); // NOI18N
        Scroll.setViewportView(tbKamar);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass6.setName("panelGlass6"); // NOI18N
        panelGlass6.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel16.setText("Keyword :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass6.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
        TCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCariActionPerformed(evt);
            }
        });
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass6.add(TCari);

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
        panelGlass6.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass6.add(jLabel7);

        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(20, 23));
        panelGlass6.add(jLabel17);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass6.add(LCount);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "17-10-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass6.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass6.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "17-10-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass6.add(DTPCari2);

        BtnHapus1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus1.setMnemonic('H');
        BtnHapus1.setText("Hapus Resep dan Obat");
        BtnHapus1.setToolTipText("Alt+H");
        BtnHapus1.setName("BtnHapus1"); // NOI18N
        BtnHapus1.setPreferredSize(new java.awt.Dimension(170, 30));
        BtnHapus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapus1ActionPerformed(evt);
            }
        });
        BtnHapus1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapus1KeyPressed(evt);
            }
        });
        panelGlass6.add(BtnHapus1);

        BtnHapus2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus2.setMnemonic('H');
        BtnHapus2.setText("Hapus Resep");
        BtnHapus2.setToolTipText("Alt+H");
        BtnHapus2.setName("BtnHapus2"); // NOI18N
        BtnHapus2.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnHapus2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapus2ActionPerformed(evt);
            }
        });
        BtnHapus2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapus2KeyPressed(evt);
            }
        });
        panelGlass6.add(BtnHapus2);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus obat");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(120, 30));
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
        panelGlass6.add(BtnHapus);

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
        panelGlass6.add(BtnPrint);

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
        panelGlass6.add(BtnKeluar);

        internalFrame1.add(panelGlass6, java.awt.BorderLayout.PAGE_END);

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
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            //TCari.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
            for(int i=0;i<tabMode.getRowCount();i++){  
                Sequel.menyimpan("temporary","'"+i+"','"+
                                tabMode.getValueAt(i,0).toString()+"','"+
                                tabMode.getValueAt(i,1).toString()+"','"+
                                tabMode.getValueAt(i,2).toString()+"','"+
                                tabMode.getValueAt(i,3).toString()+"','"+
                                tabMode.getValueAt(i,4).toString()+"','"+
                                tabMode.getValueAt(i,5).toString()+"','"+
                                tabMode.getValueAt(i,6).toString()+"','"+
                                tabMode.getValueAt(i,7).toString()+"','"+
                                tabMode.getValueAt(i,8).toString()+"','"+
                                tabMode.getValueAt(i,9).toString()+"','"+
                                tabMode.getValueAt(i,10).toString()+"','"+
                                tabMode.getValueAt(i,11).toString()+"','"+
                                tabMode.getValueAt(i,12).toString()+"','"+
                                tabMode.getValueAt(i,14).toString()+"','"+
                                tabMode.getValueAt(i,15).toString()+"','"+
                                tabMode.getValueAt(i,16).toString()+"','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Daftar Pelayanan Obat Apotek BPJS"); 
            }
            
            Map<String, Object> param = new HashMap<>();                 
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            //param.put("peserta","No.Peserta : "+NoKartu.getText()+" Nama Peserta : "+NamaPasien.getText());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            Valid.MyReportqry("rptApotekBPJSDaftarPelayananKlaim.jasper","report","[ Daftar Pelayanan Apotek BPJS ]","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
            this.setCursor(Cursor.getDefaultCursor());
        }        
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampil();
            BtnPrint.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            tampil();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnCariActionPerformed(null);
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tampil();
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
    if (tbKamar.getSelectedRow() != -1) {

    if (!tbKamar.getValueAt(tbKamar.getSelectedRow(), 8).toString().equals("")) {
        // Ambil kode apotek BPJS berdasarkan kode barang
        String kode_apotek_bpjs2 = Sequel.cariIsi(
            "SELECT kode_brng_apotek_bpjs FROM maping_obat_apotek_bpjs WHERE kode_brng=?",
            tbKamar.getValueAt(tbKamar.getSelectedRow(), 8).toString()
        );

        try {
            // Hapus data dari tabel lokal (jika perlu)
            Sequel.meghapus(
                "bridging_apotek_bpjs_obat",
                "no_sjp",
                "kd_obat",
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 0).toString(),
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 8).toString()
            );

            // Hapus data obat ke server BPJS
            bodyWithDeletePerobat(
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 0).toString(),  // no_apotek
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 2).toString(),  // no_resep
                kode_apotek_bpjs2,                                           // kode obat BPJS
                "N",                                                         // tipe_obat
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 8).toString()   // kode obat lokal
            );
        } catch (Exception ex) {
            Logger.getLogger(ApotekBPJSDaftarPelayananObat2.class.getName())
                  .log(Level.SEVERE, null, ex);
        }

    } else {
        JOptionPane.showMessageDialog(null, "Kode obat tidak ditemukan untuk baris yang dipilih.");
    }

} else {
    JOptionPane.showMessageDialog(null, "Silahkan pilih dulu data yang mau dihapus..!!");
}


    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnPrint);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void TCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TCariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariActionPerformed

    private void BtnHapus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapus1ActionPerformed
        if(tbKamar.getSelectedRow()!= -1){
            reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah yakin No.Apotek "+tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString()+" dan semua obatnya mau dihapus..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                    ps = koneksi.prepareStatement("SELECT maping_obat_apotek_bpjs.kode_brng_apotek_bpjs, bridging_apotek_bpjs_obat.kd_obat, bridging_apotek_bpjs_obat.racikan FROM bridging_apotek_bpjs_obat INNER JOIN maping_obat_apotek_bpjs ON "+
                        "bridging_apotek_bpjs_obat.kd_obat=maping_obat_apotek_bpjs.kode_brng WHERE bridging_apotek_bpjs_obat.no_sep='"+tbKamar.getValueAt(tbKamar.getSelectedRow(),1).toString()+"' AND bridging_apotek_bpjs_obat.racikan='0' ");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        bodyWithDeleteRequest(rs.getString("kode_brng_apotek_bpjs"),"N",rs.getString("kd_obat"));    
                    }
                    
                    if (Sequel.cariIsi("select no_sep from bridging_apotek_bpjs_obat where no_sep=? AND bridging_apotek_bpjs_obat.racikan='0' ",tbKamar.getValueAt(tbKamar.getSelectedRow(),1).toString()).isEmpty()) {
                         Sequel.meghapus("bridging_apotek_bpjs_obat", "no_sep", "racikan",tbKamar.getValueAt(tbKamar.getSelectedRow(),1).toString(),"1");
                        bodyWithDeleteRequestResep(
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 0).toString(),  // no_apotek
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 1).toString(),  // no_resep                                                        // tipe_obat
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 2).toString()   // kode obat lokal
            );
                    }
                   
                }catch (Exception ex) {
                    System.out.println("Notifikasi Bridging : "+ex);
                }
            }
        }else{
            JOptionPane.showMessageDialog(null,"Silahkan pilih dulu data yang mau dihapus..!!");
        }
    }//GEN-LAST:event_BtnHapus1ActionPerformed

    private void BtnHapus1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapus1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnHapus1KeyPressed

    private void BtnHapus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapus2ActionPerformed
        if(tbKamar.getSelectedRow()!= -1){
            reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah yakin No.Apotek "+tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString()+" Mau dihapus..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    
                    try {
            // Hapus data obat ke server BPJS
            bodyWithDeleteRequestResep(
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 0).toString(),  // no_apotek
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 1).toString(),  // no_resep                                                        // tipe_obat
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 2).toString()   // kode obat lokal
            );
        } catch (Exception ex) {
            Logger.getLogger(ApotekBPJSDaftarPelayananObat2.class.getName())
                  .log(Level.SEVERE, null, ex);
        }

    } else {
        JOptionPane.showMessageDialog(null, "Resep tidak ditemukan untuk baris yang dipilih.");
    }

} else {
    JOptionPane.showMessageDialog(null, "Silahkan pilih dulu data yang mau dihapus..!!");
}
                    
                
    }//GEN-LAST:event_BtnHapus2ActionPerformed

    private void BtnHapus2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapus2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnHapus2KeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            ApotekBPJSDaftarPelayananObat2 dialog = new ApotekBPJSDaftarPelayananObat2(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnHapus;
    private widget.Button BtnHapus1;
    private widget.Button BtnHapus2;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel21;
    private widget.Label jLabel7;
    private widget.panelisi panelGlass6;
    private widget.Table tbKamar;
    // End of variables declaration//GEN-END:variables

public void tampil() {
    Valid.tabelKosong(tabMode);

    String cari = TCari.getText().trim();

    try {
        String sql =
                "SELECT bridging_apotek_bpjs.no_apotek, bridging_apotek_bpjs.no_sep, bridging_apotek_bpjs.no_resep, " +
                "bridging_sep.no_kartu, bridging_sep.nama_pasien, bridging_apotek_bpjs.jenis_obat, " +
                "bridging_apotek_bpjs.tgl_pelayanan, bridging_apotek_bpjs.tgl_resep " +
                "FROM bridging_apotek_bpjs " +
                "JOIN bridging_sep ON bridging_apotek_bpjs.no_sep = bridging_sep.no_sep " +
                "WHERE bridging_apotek_bpjs.tgl_pelayanan BETWEEN ? AND ? " +
                (cari.equals("") ? "" :
                "AND (bridging_apotek_bpjs.no_apotek LIKE ? " +
                "OR bridging_sep.no_sep LIKE ? " +
                "OR bridging_sep.nama_pasien LIKE ? " +
                "OR bridging_sep.no_kartu LIKE ? " +
                "OR bridging_apotek_bpjs.no_resep LIKE ?) ") +
                "ORDER BY bridging_apotek_bpjs.tgl_pelayanan DESC, bridging_apotek_bpjs.no_apotek DESC";

        ps = koneksi.prepareStatement(sql);

        int i = 1;
        ps.setString(i++, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
        ps.setString(i++, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));

        if (!cari.equals("")) {
            String likeCari = "%" + cari + "%";
            ps.setString(i++, likeCari); // no_apotek
            ps.setString(i++, likeCari); // no_sep
            ps.setString(i++, likeCari); // nama_pasien
            ps.setString(i++, likeCari); // no_kartu
            ps.setString(i++, likeCari); // no_resep
        }

        rs = ps.executeQuery();

        // prepare sekali, lalu pakai berulang (lebih aman & efisien)
        ps2 = koneksi.prepareStatement(
                "SELECT bridging_apotek_bpjs_obat.kd_obat, bridging_apotek_bpjs_obat.nm_obat, " +
                "bridging_apotek_bpjs_obat.jumlah, bridging_apotek_bpjs_obat.signa1, " +
                "bridging_apotek_bpjs_obat.signa2, bridging_apotek_bpjs_obat.racikan " +
                "FROM bridging_apotek_bpjs_obat WHERE bridging_apotek_bpjs_obat.no_sjp = ?"
        );

        while (rs.next()) {
            String jenisObat = rs.getString("jenis_obat");
            String jenisObatText;

            if ("1".equals(jenisObat)) {
                jenisObatText = "Obat PRB";
            } else if ("2".equals(jenisObat)) {
                jenisObatText = "Obat Kronis Belum Stabil";
            } else {
                jenisObatText = "Obat Kemoterapi";
            }

            tabMode.addRow(new String[]{
                rs.getString("no_apotek"),
                rs.getString("no_sep"),
                rs.getString("no_resep"),
                rs.getString("no_kartu"),
                rs.getString("nama_pasien"),
                rs.getString("jenis_obat"),
                jenisObatText,
                rs.getString("tgl_pelayanan"),
                "", "", "", "", "", "", "", "", ""
            });

            // tampil obat (detail)
            if (rs2 != null) {
                rs2.close();
                rs2 = null;
            }

            ps2.setString(1, rs.getString("no_apotek"));
            rs2 = ps2.executeQuery();

            while (rs2.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("no_apotek"),
                    rs.getString("no_sep"),
                    rs.getString("no_resep"),
                    "", "", "", "", "",
                    rs2.getString("kd_obat"),
                    rs2.getString("nm_obat"),
                    rs2.getString("jumlah"),
                    rs2.getString("signa1"),
                    rs2.getString("signa2"),
                    ("1".equals(rs2.getString("racikan")) ? "Ya" : "Tidak")
                });
            }
        }

    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    } finally {
        try {
            if (rs2 != null) {
                rs2.close();
            }
        } catch (Exception e) {
            System.out.println("Notif rs2 : " + e);
        }
        try {
            if (ps2 != null) {
                ps2.close();
            }
        } catch (Exception e) {
            System.out.println("Notif ps2 : " + e);
        }
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            System.out.println("Notif rs : " + e);
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif ps : " + e);
        }
    }

    LCount.setText("" + tabMode.getRowCount());
}

    public JTable getTable(){
        return tbKamar;
    }
    
    public static class HttpEntityEnclosingDeleteRequest extends HttpEntityEnclosingRequestBase {
        public HttpEntityEnclosingDeleteRequest(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }

    @Test
    public void bodyWithDeleteRequest(String Kode_obat, String tipe_obat, String kode_brng) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        SSLContext sslContext = SSLContext.getInstance("SSL");
        javax.net.ssl.TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        SSLSocketFactory sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme=new Scheme("https",443,sslFactory);
    
        HttpComponentsClientHttpRequestFactory factory=new HttpComponentsClientHttpRequestFactory(){
            @Override
            protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                if (HttpMethod.DELETE == httpMethod) {
                    return new HttpEntityEnclosingDeleteRequest(uri);
                }
                return super.createHttpUriRequest(httpMethod, uri);
            }
        };
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        restTemplate.setRequestFactory(factory);
        
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("X-Cons-ID", koneksiDB.CONSIDAPIAPOTEKBPJS());
            utc = String.valueOf(api.GetUTCdatetimeAsString());
            headers.add("X-Timestamp", utc);
            headers.add("X-Signature", api.getHmac(utc));
            headers.add("user_key", koneksiDB.USERKEYAPIAPOTEKBPJS());
            URL = link+"/pelayanan/obat/hapus";
            System.out.println("URL : "+URL);
            requestJson ="{ "+
                            "\"nosepapotek\":\""+tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString()+"\","+
                            "\"noresep\":\""+tbKamar.getValueAt(tbKamar.getSelectedRow(),2).toString()+"\","+
                            "\"kodeobat\":\""+Kode_obat+"\","+
                            "\"tipeobat\":\""+tipe_obat+"\"}  ";      
            System.out.println("respone : "+requestJson);
            requestEntity = new HttpEntity(requestJson,headers);
            root = mapper.readTree(restTemplate.exchange(URL, HttpMethod.DELETE,requestEntity, String.class).getBody());
            nameNode = root.path("metaData");
            System.out.println("code : "+nameNode.path("code").asText());
            System.out.println("message : "+nameNode.path("message").asText());
            if(nameNode.path("code").asText().equals("200")){
                Sequel.meghapus("bridging_apotek_bpjs_obat", "no_sep", "kd_obat",tbKamar.getValueAt(tbKamar.getSelectedRow(),1).toString(),kode_brng);
                JOptionPane.showMessageDialog(null,"Obat "+Kode_obat+" "+Sequel.cariIsi("select nama_brng_apotek_bpjs from maping_obat_apotek_bpjs where kode_brng_apotek_bpjs=?",Kode_obat)+" Berhasil diHapus");
            }else{
                JOptionPane.showMessageDialog(null,nameNode.path("message").asText());
            }
        } catch (Exception e) {   
            System.out.println("Notif : "+e);
            if(e.toString().contains("UnknownHostException")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server BPJS terputus...!");
            }
        }
    }
    
    @Test
    

public void bodyWithDeletePerobat(String no_apotek, String no_resep, String kode_apotek_bpjs2, String tipe_obat, String Kode_obat) throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    SSLContext sslContext = SSLContext.getInstance("SSL");
    javax.net.ssl.TrustManager[] trustManagers = {
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        }
    };
    sslContext.init(null, trustManagers, new SecureRandom());
    SSLSocketFactory sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    Scheme scheme = new Scheme("https", 443, sslFactory);

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory() {
        @Override
        protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
            if (HttpMethod.DELETE == httpMethod) {
                return new BPJSDataSEP.HttpEntityEnclosingDeleteRequest(uri);
            }
            return super.createHttpUriRequest(httpMethod, uri);
        }
    };

    factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
    restTemplate.setRequestFactory(factory);

    try {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("X-Cons-ID", koneksiDB.CONSIDAPIAPOTEKBPJS());
        utc = String.valueOf(api.GetUTCdatetimeAsString());
        headers.add("X-Timestamp", utc);
        headers.add("X-Signature", api.getHmac(utc));
        headers.add("user_key", koneksiDB.USERKEYAPIAPOTEKBPJS());

        URL = link + "/pelayanan/obat/hapus";
        requestJson = "{ " +
                "\"nosepapotek\":\"" + no_apotek + "\"," +
                "\"noresep\":\"" + no_resep + "\"," +
                "\"kodeobat\":\"" + kode_apotek_bpjs2 + "\"," +
                "\"tipeobat\":\"" + tipe_obat + "\"}";

        requestEntity = new HttpEntity(requestJson, headers);
        root = mapper.readTree(restTemplate.exchange(URL, HttpMethod.DELETE, requestEntity, String.class).getBody());
        nameNode = root.path("metaData");

        System.out.println("code : " + nameNode.path("code").asText());
        System.out.println("message : " + nameNode.path("message").asText());

        if (nameNode.path("code").asText().equals("200")) {
            Sequel.meghapus(
                "bridging_apotek_bpjs_obat",
                "no_sjp",
                "kd_obat", 
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 0).toString(),
                tbKamar.getValueAt(tbKamar.getSelectedRow(), 8).toString()
            );

            JOptionPane.showMessageDialog(
                null,
                "Obat " + Sequel.cariIsi(
                    "SELECT nama_brng_apotek_bpjs FROM maping_obat_apotek_bpjs WHERE kode_brng_apotek_bpjs=?",
                    kode_apotek_bpjs2
                ) + " berhasil dihapus."
            );
            tampil();
        } else {
            JOptionPane.showMessageDialog(null, nameNode.path("message").asText());
        }

    } catch (Exception e) {
        System.out.println("Notif : " + e);
        if (e.toString().contains("UnknownHostException")) {
            JOptionPane.showMessageDialog(null, "Koneksi ke server BPJS terputus...!");
        }
    }
}

    
    
    public void bodyWithDeleteRequestResep(String no_apotek,String no_sep,String no_resep) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        SSLContext sslContext = SSLContext.getInstance("SSL");
        javax.net.ssl.TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        SSLSocketFactory sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme=new Scheme("https",443,sslFactory);
    
        HttpComponentsClientHttpRequestFactory factory=new HttpComponentsClientHttpRequestFactory(){
            @Override
            protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                if (HttpMethod.DELETE == httpMethod) {
                    return new BPJSDataSEP.HttpEntityEnclosingDeleteRequest(uri);
                }
                return super.createHttpUriRequest(httpMethod, uri);
            }
        };
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        restTemplate.setRequestFactory(factory);
       
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("X-Cons-ID",koneksiDB.CONSIDAPIAPOTEKBPJS());
            utc=String.valueOf(api.GetUTCdatetimeAsString());
	    headers.add("X-Timestamp",utc);
	    headers.add("X-Signature",api.getHmac(utc));
            headers.add("user_key",koneksiDB.USERKEYAPIAPOTEKBPJS());
            URL = link+"/hapusresep";
            requestJson ="{ "+
                            "\"nosjp\":\""+tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString()+"\","+
                            "\"refasalsjp\":\""+tbKamar.getValueAt(tbKamar.getSelectedRow(),1).toString()+"\","+
                            "\"noresep\":\""+tbKamar.getValueAt(tbKamar.getSelectedRow(),2).toString()+"\"}  ";            
            requestEntity = new HttpEntity(requestJson,headers);
            root = mapper.readTree(restTemplate.exchange(URL, HttpMethod.DELETE,requestEntity, String.class).getBody());
            nameNode = root.path("metaData");
            System.out.println("code : "+nameNode.path("code").asText());
            System.out.println("message : "+nameNode.path("message").asText());
            if(nameNode.path("code").asText().equals("200")){
                Sequel.meghapus("bridging_apotek_bpjs", "no_apotek", tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString());
                JOptionPane.showMessageDialog(null,"Resep "+tbKamar.getValueAt(tbKamar.getSelectedRow(),0).toString()+" Berhasil diHapus");
                tampil();
            }else{
                JOptionPane.showMessageDialog(null,nameNode.path("message").asText());
            }
        } catch (Exception e) {   
            System.out.println("Notif : "+e);
            if(e.toString().contains("UnknownHostException")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server BPJS terputus...!");
            }
        }
    }
    
    
    
    public void setNoRm(String nosep) {        
        TCari.setText(nosep);
        tampil();
    }
}
