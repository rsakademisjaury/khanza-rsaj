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

package digitalsignature;

//import antrian.*;
//import custom.*;
//import bpjsvclaim.BPJSApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import simrskhanza.*;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import inventory.DlgCariKonversi;
import inventory.DlgCariObat;
import inventory.DlgCariObat2;
import inventory.DlgCariObat3;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * @author perpustakaan
 */
public class DlgViewFileKlaimDigital extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private String link="",URL="",requestJson="",FileName="",norawat="";
    private validasi Valid=new validasi();
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode,code,metadata;
    private JsonNode response;
    private TteApi apiTte=new TteApi();
    private Connection koneksi=koneksiDB.condb();
    private static final Properties prop = new Properties();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i,j;
//    

    /** Creates new form DlgPemberianObat
     * @param parent
     * @param modal */
    public DlgViewFileKlaimDigital(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(1285,350);
        nmFile.setVisible(false);
        
        Object[] row={"","No File","Nama File","Tgl. Sign","Jenis File",""};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                  boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
              }
              Class[] types = new Class[] {
                 java.lang.Boolean.class, java.lang.Object.class,java.lang.Object.class,  java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
                
             }; 
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbListFileTte.setModel(tabMode);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbListFileTte.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbListFileTte.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 6; i++) {
            TableColumn column = tbListFileTte.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(150);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(300);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
               column.setMinWidth(0);
                column.setMaxWidth(0);
            }
        }
        tbListFileTte.setDefaultRenderer(Object.class, new WarnaTable()); 
        
//        tampil();
         
    }

    //private DlgCariObatPenyakit dlgobtpny=new DlgCariObatPenyakit(null,false);
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnViewFile = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        internalFrame2 = new widget.InternalFrame();
        panelGlass9 = new widget.panelisi();
        jLabel7 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari1 = new widget.Button();
        BtnAll1 = new widget.Button();
        BtnKeluar = new widget.Button();
        BtnCari2 = new widget.Button();
        nmFile = new widget.TextBox();
        intListFileTTE = new widget.ScrollPane();
        tbListFileTte = new widget.Table();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnViewFile.setBackground(new java.awt.Color(255, 255, 254));
        MnViewFile.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnViewFile.setForeground(new java.awt.Color(70, 70, 70));
        MnViewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnViewFile.setText("View File");
        MnViewFile.setName("MnViewFile"); // NOI18N
        MnViewFile.setPreferredSize(new java.awt.Dimension(100, 26));
        MnViewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnViewFileActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnViewFile);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), ".:[ List Detail Dokumen Digital Klaim ]:.", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(70, 70, 70))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(875, 200));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout());

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Key Word :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel7);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('3');
        BtnCari1.setToolTipText("Alt+3");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari1ActionPerformed(evt);
            }
        });
        BtnCari1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari1KeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari1);

        BtnAll1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll1.setMnemonic('M');
        BtnAll1.setToolTipText("Alt+M");
        BtnAll1.setName("BtnAll1"); // NOI18N
        BtnAll1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAll1ActionPerformed(evt);
            }
        });
        BtnAll1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAll1KeyPressed(evt);
            }
        });
        panelGlass9.add(BtnAll1);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnKeluar.setMnemonic('T');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+T");
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
        panelGlass9.add(BtnKeluar);

        BtnCari2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/TIFF24.png"))); // NOI18N
        BtnCari2.setMnemonic('3');
        BtnCari2.setText("Download File");
        BtnCari2.setToolTipText("Alt+3");
        BtnCari2.setName("BtnCari2"); // NOI18N
        BtnCari2.setPreferredSize(new java.awt.Dimension(150, 23));
        BtnCari2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari2ActionPerformed(evt);
            }
        });
        BtnCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari2KeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari2);

        nmFile.setEditable(false);
        nmFile.setName("nmFile"); // NOI18N
        nmFile.setPreferredSize(new java.awt.Dimension(310, 23));
        nmFile.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nmFileKeyPressed(evt);
            }
        });
        panelGlass9.add(nmFile);

        internalFrame2.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(internalFrame2, java.awt.BorderLayout.PAGE_END);

        intListFileTTE.setName("intListFileTTE"); // NOI18N
        intListFileTTE.setOpaque(true);

        tbListFileTte.setAutoCreateRowSorter(true);
        tbListFileTte.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbListFileTte.setComponentPopupMenu(jPopupMenu1);
        tbListFileTte.setName("tbListFileTte"); // NOI18N
        tbListFileTte.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbListFileTteMouseClicked(evt);
            }
        });
        tbListFileTte.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbListFileTteKeyPressed(evt);
            }
        });
        intListFileTTE.setViewportView(tbListFileTte);

        internalFrame1.add(intListFileTTE, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void berhasilSimpan() {  
   
} 
    
    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
      
    }//GEN-LAST:event_formWindowActivated

    private void tbListFileTteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbListFileTteMouseClicked

    }//GEN-LAST:event_tbListFileTteMouseClicked

    private void tbListFileTteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbListFileTteKeyPressed
     if(tabMode.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                dispose();
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                
            }
        }
    }//GEN-LAST:event_tbListFileTteKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
//        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
//            BtnCariActionPerformed(null);
//        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
//            BtnCari.requestFocus();
//        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
//            BtnKeluar.requestFocus();
//        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        tampil();
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
//        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
//            BtnCariActionPerformed(null);
//        }else{
//            Valid.pindah(evt, TCari, BtnAll);
//        }
    }//GEN-LAST:event_BtnCari1KeyPressed

    private void BtnAll1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAll1ActionPerformed
        TCari.setText("");
        tampil();
    }//GEN-LAST:event_BtnAll1ActionPerformed

    private void BtnAll1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAll1KeyPressed

//        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
//            tampil();
//            TCari.setText("");
//        }else{
//            Valid.pindah(evt, BtnCari, TPasien);
//        }
    }//GEN-LAST:event_BtnAll1KeyPressed

    private void MnViewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnViewFileActionPerformed
        if(tbListFileTte.getSelectedRow()>-1){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            FileName=tbListFileTte.getValueAt(tbListFileTte.getSelectedRow(),5).toString();
            DlgViewPdf berkas=new DlgViewPdf(null,true);
            berkas.tampilPdf2(FileName);
            berkas.setButton(false);
            berkas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            berkas.setLocationRelativeTo(internalFrame1);
            berkas.setVisible(true);

            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnViewFileActionPerformed

    private void BtnCari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari2ActionPerformed
        createZip();
    }//GEN-LAST:event_BtnCari2ActionPerformed

    private void BtnCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari2KeyPressed

    private void nmFileKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmFileKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_nmFileKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgViewFileKlaimDigital dialog = new DlgViewFileKlaimDigital(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll1;
    private widget.Button BtnCari1;
    private widget.Button BtnCari2;
    private widget.Button BtnKeluar;
    private javax.swing.JMenuItem MnViewFile;
    private widget.TextBox TCari;
    private widget.ScrollPane intListFileTTE;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox nmFile;
    private widget.panelisi panelGlass9;
    private widget.Table tbListFileTte;
    // End of variables declaration//GEN-END:variables
    

    private void isPsien() {
//        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
    }
    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if(TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                    "select * from berkas_tte JOIN master_berkas_tte ON berkas_tte.kode = master_berkas_tte.kode "+
                    " where   no_rawat='"+norawat+"'");
            }else{
                  ps=koneksi.prepareStatement(
                    "select * from berkas_tte JOIN master_berkas_tte ON berkas_tte.kode = master_berkas_tte.kode "+
                    " where   no_dokumen like ? and status='MEDIS' and no_rawat='"+norawat+"'");
            }
            try {
                if(TCari.getText().equals("")){
                }else{
                    ps.setString(1,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        false,rs.getString("no_dokumen"),rs.getString("nama_file"),rs.getString("tgl_tte"),rs.getString("nama"),rs.getString("lokasi_file")
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
            
        } catch (Exception e) {
            System.out.println(e);
        }
//        int b=tabMode.getRowCount();
//        LCount.setText(""+b);
    }
    public JTable getTable(String FileName){
        return tbListFileTte;
    }
    
    void clicList(String FileName){
       
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgViewPdf berkas=new DlgViewPdf(null,true);
        berkas.tampilPdf2(FileName);
        berkas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        berkas.setLocationRelativeTo(internalFrame1);
        berkas.setVisible(true);
        
        this.setCursor(Cursor.getDefaultCursor());
        
    }
    public void setNoRawat(String noRawat, String namaFile){
        this.norawat=noRawat;
        nmFile.setText(namaFile);
    }
void createZip(){
     String zipFile = "tempfile//"+nmFile.getText()+".zip";
         
//        String[] srcFiles = { "tempfile//2022_01_18_000234.pdf", "tempfile//2022_01_31_000228.pdf", "tempfile//2022_02_01_000002.pdf"};
         
        try {
            // create byte buffer
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(i=0;i<tbListFileTte.getRowCount();i++){ 
            if(tbListFileTte.getValueAt(i,0).toString().equals("true")){
//                URL url =new URL("http://localhost/webapps/berkastte/"+tbListFileTte.getValueAt(i,5).toString());
//                File srcFile = new File(url);
//                File srcFile = Paths.get(url.toURI().getPath()).toFile();
                
                URL website = new URL("http://localhost/webapps/berkastte/"+tbListFileTte.getValueAt(i,5).toString());
                ReadableByteChannel rbc;
                rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos1 = new FileOutputStream("tempfile/"+tbListFileTte.getValueAt(i,2).toString());
                fos1.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos1.close();
                rbc.close();
                
                
                
                File srcFile = new File("tempfile/"+tbListFileTte.getValueAt(i,2).toString());
                
                FileInputStream fis = new FileInputStream(srcFile);
                zos.putNextEntry(new ZipEntry(srcFile.getName())); 
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
        }  
                zos.close();
             File srcFile = new File(zipFile);
             JFrame parentFrame = new JFrame();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setSelectedFile(new File(srcFile.getName()));
            int userSelection = fileChooser.showSaveDialog(parentFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                System.out.println("Save as file: " + fileToSave.getAbsolutePath());
                File destFile = new File(fileToSave.getAbsolutePath());
                try {
                    FileUtils.copyFile(srcFile, destFile);
                    JOptionPane.showMessageDialog(null,"Dokumen berhasil didownload.");
//                    srcFile.delete();
                    deleteFile();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,"Gagal mendownload template.");
                }
            }
        }
        catch (Exception ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }
    }
   void deleteFile(){
       File file = new File("tempfile");      
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
