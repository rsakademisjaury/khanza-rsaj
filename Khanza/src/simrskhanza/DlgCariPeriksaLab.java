package simrskhanza;
import kepegawaian.DlgCariPetugas;
import keuangan.Jurnal;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDBSalim;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import laporan.DlgBerkasRawat;
import rekammedis.RMRiwayatPerawatan;
import surat.SuratKeteranganCovid;
import wa.HasilPenunjangWhatsapp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import static com.itextpdf.kernel.pdf.PdfName.Pattern;
import java.util.regex.Pattern;
import fungsi.koneksiDB;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import keuangan.DlgPembayaranLaboratoriumRalan;
import keuangan.DlgPembayaranLaboratoriumRanap;
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


public class DlgCariPeriksaLab extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabMode2,tabMode3,tabMode4;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Jurnal jur=new Jurnal();
    private Connection koneksi=koneksiDB.condb();
    private DlgCariPasien member=new DlgCariPasien(null,false);
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private int i,jmlkunjungan=0,jmlpemeriksaan=0,jmlsubpemeriksaan=0;
    private PreparedStatement ps,ps2,ps3,ps4,psrekening,ps5,pspermintaan,psM,ps2M,ps3M,ps4M,psrekeningM,ps5M,pspermintaanM,psK,ps2K,ps3K,ps5K;
    private ResultSet rs,rs2,rs3,rs5,rsrekening,rspermintaan,rsM,rs2M,rs3M,rs5M,rsrekeningM,rspermintaanM,rsK,rs2K,rs3K,rs5K;
    private String kamar,namakamar,datapasien="",finger="",noRawatSumTulang="",tglPeriksaSumTulang="",jamperiksaSumTulang="",idSumTulang="";
    private boolean sukses=false;
    private double ttl=0,item=0,urutan=0;
    private StringBuilder htmlContent;
    private double ttljmdokter=0,ttljmpetugas=0,ttlkso=0,ttlpendapatan=0,ttlbhp=0,ttljasasarana=0,ttljmperujuk=0,ttlmenejemen=0;
    private String pelapor="",penerima="",diagnosa="",saran="",kesan="",Suspen_Piutang_Laborat_Ranap="",Laborat_Ranap="",Beban_Jasa_Medik_Dokter_Laborat_Ranap="",Utang_Jasa_Medik_Dokter_Laborat_Ranap="",
            Beban_Jasa_Medik_Petugas_Laborat_Ranap="",Utang_Jasa_Medik_Petugas_Laborat_Ranap="",Beban_Kso_Laborat_Ranap="",Utang_Kso_Laborat_Ranap="",
            HPP_Persediaan_Laborat_Rawat_inap="",Persediaan_BHP_Laborat_Rawat_Inap="",Beban_Jasa_Sarana_Laborat_Ranap="",Utang_Jasa_Sarana_Laborat_Ranap="",
            Beban_Jasa_Perujuk_Laborat_Ranap="",Utang_Jasa_Perujuk_Laborat_Ranap="",Beban_Jasa_Menejemen_Laborat_Ranap="",Utang_Jasa_Menejemen_Laborat_Ranap="",
            Suspen_Piutang_Laborat_Ralan="",Laborat_Ralan="",Beban_Jasa_Medik_Dokter_Laborat_Ralan="",Utang_Jasa_Medik_Dokter_Laborat_Ralan="",
            Beban_Jasa_Medik_Petugas_Laborat_Ralan="",Utang_Jasa_Medik_Petugas_Laborat_Ralan="",Beban_Kso_Laborat_Ralan="",Utang_Kso_Laborat_Ralan="",
            HPP_Persediaan_Laborat_Rawat_Jalan="",Persediaan_BHP_Laborat_Rawat_Jalan="",Beban_Jasa_Sarana_Laborat_Ralan="",Utang_Jasa_Sarana_Laborat_Ralan="",
            Beban_Jasa_Perujuk_Laborat_Ralan="",Utang_Jasa_Perujuk_Laborat_Ralan="",Beban_Jasa_Menejemen_Laborat_Ralan="",Utang_Jasa_Menejemen_Laborat_Ralan="",status="",FileName = "", kodeberkas = "";

    private boolean sedangGantiTab = false;

    /** Creates new form DlgProgramStudi
     * @param parent
     * @param modal */
    public DlgCariPeriksaLab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        installMenuInputCatatan();
        installTombolKirimHasilWALab();
        WaktuLapor.setDate(new Date());

        Object[] row={"No.Rawat","Pasien","Petugas","Tgl.Periksa","Jam Periksa","Dokter Perujuk","Penanggung Jawab"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter.setModel(tabMode);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 7; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(400);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(130);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(200);
            }else if(i==6){
                column.setPreferredWidth(200);
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());
            tabMode2=new DefaultTableModel(null,new Object[]{
                "No.Rawat","Pasien","Tgl.Periksa","Jam","Pemeriksaan",
                "Item Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan",
                "Ruang","Petugas","Dokter Perujuk","Penanggung Jawab"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter2.setModel(tabMode2);

        tbDokter2.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
            TableColumn column = tbDokter2.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(210);
            }else if(i==2){
                column.setPreferredWidth(65);
            }else if(i==3){
                column.setPreferredWidth(55);
            }else if(i==4){
                column.setPreferredWidth(155);
            }else if(i==5){
                column.setPreferredWidth(165);
            }else if(i==6){
                column.setPreferredWidth(70);
            }else if(i==7){
                column.setPreferredWidth(50);
            }else if(i==8){
                column.setPreferredWidth(110);
            }else if(i==9){
                column.setPreferredWidth(100);
            }else if(i==10){
                column.setPreferredWidth(130);
            }else if(i==11){
                column.setPreferredWidth(130);
            }else if(i==12){
                column.setPreferredWidth(160);
            }else if(i==13){
                column.setPreferredWidth(160);
            }
        }
        tbDokter2.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabMode3=new DefaultTableModel(null,new Object[]{
                "No.Rawat","Pasien","Petugas","Tgl.Periksa","Jam Periksa","Dokter Perujuk","Penanggung Jawab","Informasi Tambahan","Jam Pelaporan Hasil"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter1.setModel(tabMode3);

        tbDokter1.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbDokter1.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(220);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(130);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(200);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(200);
            }else if(i==8){
                column.setPreferredWidth(200);    
            }
        }
        tbDokter1.setDefaultRenderer(Object.class, new WarnaTable());    
        
        tabMode4=new DefaultTableModel(null,new Object[]{
                "No.Rawat","Pasien","Petugas","Tgl.Periksa","Jam Periksa","Dokter Perujuk","Penanggung Jawab","Informasi Tambahan","Jam Pelaporan Hasil"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter3.setModel(tabMode4);

        tbDokter3.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbDokter3.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(220);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(130);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(200);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(200);
            }else if(i==8){
                column.setPreferredWidth(200);    
            }
        }
        tbDokter3.setDefaultRenderer(Object.class, new WarnaTable());    
                
        NoRawat.setDocument(new batasInput((byte)17).getKata(NoRawat));
        kdmem.setDocument(new batasInput((byte)8).getKata(kdmem));
        kdptg.setDocument(new batasInput((byte)25).getKata(kdptg));
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
        
        member.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgCariPeriksaLab")){
                    if(member.getTable().getSelectedRow()!= -1){                   
                        kdmem.setText(member.getTable().getValueAt(member.getTable().getSelectedRow(),0).toString());
                        nmmem.setText(member.getTable().getValueAt(member.getTable().getSelectedRow(),1).toString());
                    } 
                    kdmem.requestFocus();
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
        
        member.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("DlgCariPeriksaLab")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        member.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        }); 
        
        petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgCariPeriksaLab")){
                    if(petugas.getTable().getSelectedRow()!= -1){                   
                        kdptg.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        nmptg.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }  
                    kdptg.requestFocus();
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
        
        
        try {
            psrekening=koneksi.prepareStatement("select * from set_akun_ranap");
            try {
                rsrekening=psrekening.executeQuery();
                while(rsrekening.next()){
                    Suspen_Piutang_Laborat_Ranap=rsrekening.getString("Suspen_Piutang_Laborat_Ranap");
                    Laborat_Ranap=rsrekening.getString("Laborat_Ranap");
                    Beban_Jasa_Medik_Dokter_Laborat_Ranap=rsrekening.getString("Beban_Jasa_Medik_Dokter_Laborat_Ranap");
                    Utang_Jasa_Medik_Dokter_Laborat_Ranap=rsrekening.getString("Utang_Jasa_Medik_Dokter_Laborat_Ranap");
                    Beban_Jasa_Medik_Petugas_Laborat_Ranap=rsrekening.getString("Beban_Jasa_Medik_Petugas_Laborat_Ranap");
                    Utang_Jasa_Medik_Petugas_Laborat_Ranap=rsrekening.getString("Utang_Jasa_Medik_Petugas_Laborat_Ranap");
                    Beban_Kso_Laborat_Ranap=rsrekening.getString("Beban_Kso_Laborat_Ranap");
                    Utang_Kso_Laborat_Ranap=rsrekening.getString("Utang_Kso_Laborat_Ranap");
                    HPP_Persediaan_Laborat_Rawat_inap=rsrekening.getString("HPP_Persediaan_Laborat_Rawat_inap");
                    Persediaan_BHP_Laborat_Rawat_Inap=rsrekening.getString("Persediaan_BHP_Laborat_Rawat_Inap");
                    Beban_Jasa_Sarana_Laborat_Ranap=rsrekening.getString("Beban_Jasa_Sarana_Laborat_Ranap");
                    Utang_Jasa_Sarana_Laborat_Ranap=rsrekening.getString("Utang_Jasa_Sarana_Laborat_Ranap");
                    Beban_Jasa_Perujuk_Laborat_Ranap=rsrekening.getString("Beban_Jasa_Perujuk_Laborat_Ranap");
                    Utang_Jasa_Perujuk_Laborat_Ranap=rsrekening.getString("Utang_Jasa_Perujuk_Laborat_Ranap");
                    Beban_Jasa_Menejemen_Laborat_Ranap=rsrekening.getString("Beban_Jasa_Menejemen_Laborat_Ranap");
                    Utang_Jasa_Menejemen_Laborat_Ranap=rsrekening.getString("Utang_Jasa_Menejemen_Laborat_Ranap");
                }
            } catch (Exception e) {
                System.out.println("Notif Rekening : "+e);
            } finally{
                if(rsrekening!=null){
                    rsrekening.close();
                }
                if(psrekening!=null){
                    psrekening.close();
                }
            }   
            
            psrekening=koneksi.prepareStatement("select * from set_akun_ralan");
            try {
                rsrekening=psrekening.executeQuery();
                while(rsrekening.next()){
                    Suspen_Piutang_Laborat_Ralan=rsrekening.getString("Suspen_Piutang_Laborat_Ralan");
                    Laborat_Ralan=rsrekening.getString("Laborat_Ralan");
                    Beban_Jasa_Medik_Dokter_Laborat_Ralan=rsrekening.getString("Beban_Jasa_Medik_Dokter_Laborat_Ralan");
                    Utang_Jasa_Medik_Dokter_Laborat_Ralan=rsrekening.getString("Utang_Jasa_Medik_Dokter_Laborat_Ralan");
                    Beban_Jasa_Medik_Petugas_Laborat_Ralan=rsrekening.getString("Beban_Jasa_Medik_Petugas_Laborat_Ralan");
                    Utang_Jasa_Medik_Petugas_Laborat_Ralan=rsrekening.getString("Utang_Jasa_Medik_Petugas_Laborat_Ralan");
                    Beban_Kso_Laborat_Ralan=rsrekening.getString("Beban_Kso_Laborat_Ralan");
                    Utang_Kso_Laborat_Ralan=rsrekening.getString("Utang_Kso_Laborat_Ralan");
                    HPP_Persediaan_Laborat_Rawat_Jalan=rsrekening.getString("HPP_Persediaan_Laborat_Rawat_Jalan");
                    Persediaan_BHP_Laborat_Rawat_Jalan=rsrekening.getString("Persediaan_BHP_Laborat_Rawat_Jalan");
                    Beban_Jasa_Sarana_Laborat_Ralan=rsrekening.getString("Beban_Jasa_Sarana_Laborat_Ralan");
                    Utang_Jasa_Sarana_Laborat_Ralan=rsrekening.getString("Utang_Jasa_Sarana_Laborat_Ralan");
                    Beban_Jasa_Perujuk_Laborat_Ralan=rsrekening.getString("Beban_Jasa_Perujuk_Laborat_Ralan");
                    Utang_Jasa_Perujuk_Laborat_Ralan=rsrekening.getString("Utang_Jasa_Perujuk_Laborat_Ralan");
                    Beban_Jasa_Menejemen_Laborat_Ralan=rsrekening.getString("Beban_Jasa_Menejemen_Laborat_Ralan");
                    Utang_Jasa_Menejemen_Laborat_Ralan=rsrekening.getString("Utang_Jasa_Menejemen_Laborat_Ralan");
                }
            } catch (Exception e) {
                System.out.println("Notif Rekening : "+e);
            } finally{
                if(rsrekening!=null){
                    rsrekening.close();
                }
                if(psrekening!=null){
                    psrekening.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML1.setEditable(true);
        LoadHTML1.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".head td{border-right: 1px solid #777777;font: 8.5px tahoma;height:10px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;}"+
                ".isi2 td{font: 8.5px tahoma;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
        );
        Document doc = kit.createDefaultDocument();

        LoadHTML1.setDocument(doc);
        LoadHTML1.setEditable(false);                   
    }

    /**
     * Menu Input Catatan ditambahkan secara dinamis agar file .form dan
     * generated code initComponents() tetap tidak berubah.
     */
    private void installMenuInputCatatan() {
        try {
            final javax.swing.JMenuItem mnInputCatatan = new javax.swing.JMenuItem();
            mnInputCatatan.setBackground(new java.awt.Color(255, 255, 254));
            mnInputCatatan.setFont(new java.awt.Font("Tahoma", 0, 11));
            mnInputCatatan.setForeground(new java.awt.Color(50, 50, 50));
            mnInputCatatan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
            mnInputCatatan.setText("Input Catatan");
            mnInputCatatan.setName("MnInputCatatan");
            mnInputCatatan.setPreferredSize(new java.awt.Dimension(190, 26));
            mnInputCatatan.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    inputCatatanLab();
                }
            });

            // MnSumsumtulang adalah menu terakhir bawaan. Sisipkan tepat setelahnya.
            int posisi = jPopupMenu1.getComponentIndex(MnSumsumtulang);
            if (posisi >= 0) {
                jPopupMenu1.insert(mnInputCatatan, posisi + 1);
            } else {
                jPopupMenu1.add(mnInputCatatan);
            }
        } catch (Exception ex) {
            System.out.println("Notif pasang menu Input Catatan Lab : " + ex);
        }
    }

    /**
     * Menampilkan form input catatan berdasarkan kombinasi
     * no_rawat + tgl_periksa + jam_periksa.
     */
    private void inputCatatanLab() {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data pemeriksaan laboratorium sudah habis...!!!!");
            TCari.requestFocus();
            return;
        }

        if (tbDokter.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data pemeriksaan laboratorium terlebih dahulu.");
            return;
        }

        // Tetap bisa dipakai saat user klik kanan pada baris detail pemeriksaan:
        // helper akan mencari baris header pemeriksaan terdekat di atasnya.
        String noRawat = ambilNilaiTableBarisKunci(tbDokter, 0, 0);
        String tglPeriksa = ambilNilaiTableBarisKunci(tbDokter, 0, 3);
        String jamPeriksa = ambilNilaiTableBarisKunci(tbDokter, 0, 4);

        if (noRawat.equals("") || tglPeriksa.equals("") || jamPeriksa.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "No. Rawat, tanggal, atau jam pemeriksaan belum terbaca.\n"
                    + "Silakan pilih baris pemeriksaan yang akan diberi catatan.");
            return;
        }

        final String tanggalDatabase = normalisasiTanggalCatatanLab(tglPeriksa);
        String catatanLama = ambilCatatanLab(noRawat, tanggalDatabase, jamPeriksa);

        javax.swing.JTextArea txtCatatan = new javax.swing.JTextArea(7, 48);
        txtCatatan.setLineWrap(true);
        txtCatatan.setWrapStyleWord(true);
        txtCatatan.setText(catatanLama);
        txtCatatan.setCaretPosition(0);

        javax.swing.JPanel panelCatatan = new javax.swing.JPanel(new java.awt.BorderLayout(5, 8));
        javax.swing.JPanel panelInfo = new javax.swing.JPanel(new java.awt.GridLayout(3, 1, 2, 2));
        panelInfo.add(new javax.swing.JLabel("No. Rawat     : " + noRawat));
        panelInfo.add(new javax.swing.JLabel("Tgl. Periksa  : " + tglPeriksa));
        panelInfo.add(new javax.swing.JLabel("Jam Periksa : " + jamPeriksa));
        panelCatatan.add(panelInfo, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel panelIsi = new javax.swing.JPanel(new java.awt.BorderLayout(3, 3));
        panelIsi.add(new javax.swing.JLabel("Catatan :"), java.awt.BorderLayout.NORTH);
        panelIsi.add(new javax.swing.JScrollPane(txtCatatan), java.awt.BorderLayout.CENTER);
        panelCatatan.add(panelIsi, java.awt.BorderLayout.CENTER);

        int pilihan = JOptionPane.showConfirmDialog(
                this,
                panelCatatan,
                "Input Catatan Hasil Laboratorium",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (pilihan != JOptionPane.OK_OPTION) {
            return;
        }

        PreparedStatement psCatatan = null;
        try {
            String isiCatatan = txtCatatan.getText() == null ? "" : txtCatatan.getText().trim();

            if (isiCatatan.equals("")) {
                psCatatan = koneksi.prepareStatement(
                        "delete from catatan_periksa_lab "
                        + "where no_rawat=? and tgl_periksa=? and jam_periksa=?");
                psCatatan.setString(1, noRawat);
                psCatatan.setString(2, tanggalDatabase);
                psCatatan.setString(3, jamPeriksa);
                psCatatan.executeUpdate();
                JOptionPane.showMessageDialog(null, "Catatan pemeriksaan berhasil dikosongkan.");
            } else {
                psCatatan = koneksi.prepareStatement(
                        "insert into catatan_periksa_lab "
                        + "(no_rawat,tgl_periksa,jam_periksa,catatan) values (?,?,?,?) "
                        + "on duplicate key update catatan=values(catatan)");
                psCatatan.setString(1, noRawat);
                psCatatan.setString(2, tanggalDatabase);
                psCatatan.setString(3, jamPeriksa);
                psCatatan.setString(4, isiCatatan);
                psCatatan.executeUpdate();
                JOptionPane.showMessageDialog(null, "Catatan pemeriksaan berhasil disimpan.");
            }
        } catch (Exception ex) {
            System.out.println("Notif simpan catatan laboratorium : " + ex);
            JOptionPane.showMessageDialog(null,
                    "Gagal menyimpan catatan pemeriksaan laboratorium.\n\n"
                    + "Pastikan tabel catatan_periksa_lab sudah dibuat.\n"
                    + "Detail : " + ex.getMessage());
        } finally {
            try {
                if (psCatatan != null) {
                    psCatatan.close();
                }
            } catch (Exception ex) {
                System.out.println("Notif tutup ps catatan laboratorium : " + ex);
            }
        }
    }

    /**
     * Mengambil catatan untuk satu sesi pemeriksaan laboratorium.
     */
    private String ambilCatatanLab(String noRawat, String tglPeriksa, String jamPeriksa) {
        PreparedStatement psCatatan = null;
        ResultSet rsCatatan = null;
        String hasil = "";

        try {
            psCatatan = koneksi.prepareStatement(
                    "select catatan from catatan_periksa_lab "
                    + "where no_rawat=? and tgl_periksa=? and jam_periksa=? limit 1");
            psCatatan.setString(1, noRawat == null ? "" : noRawat.trim());
            psCatatan.setString(2, normalisasiTanggalCatatanLab(tglPeriksa));
            psCatatan.setString(3, jamPeriksa == null ? "" : jamPeriksa.trim());
            rsCatatan = psCatatan.executeQuery();

            if (rsCatatan.next()) {
                hasil = rsCatatan.getString("catatan");
            }
        } catch (Exception ex) {
            // Dibuat non-blocking agar proses cetak lama tetap berjalan walau
            // tabel catatan belum tersedia di database.
            System.out.println("Notif ambil catatan laboratorium : " + ex);
        } finally {
            try {
                if (rsCatatan != null) {
                    rsCatatan.close();
                }
            } catch (Exception ex) {}
            try {
                if (psCatatan != null) {
                    psCatatan.close();
                }
            } catch (Exception ex) {}
        }

        return hasil == null ? "" : hasil;
    }

    /**
     * Menerima tanggal yyyy-MM-dd maupun dd-MM-yyyy/dd/MM/yyyy.
     */
    private String normalisasiTanggalCatatanLab(String tanggal) {
        String nilai = tanggal == null ? "" : tanggal.trim();
        if (nilai.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return nilai;
        }

        if (nilai.matches("\\d{2}[-/]\\d{2}[-/]\\d{4}")) {
            String[] bagian = nilai.split("[-/]");
            return bagian[2] + "-" + bagian[1] + "-" + bagian[0];
        }

        try {
            return Valid.SetTgl(nilai);
        } catch (Exception ex) {
            return nilai;
        }
    }

    /**
     * Tombol tambahan dibuat dinamis agar file .form dan initComponents()
     * tidak berubah. Tombol ini hanya mengirim berkas final Lab yang sudah
     * terupload di berkas_digital_perawatan.
     */
    private void installTombolKirimHasilWALab() {
        try {
            final widget.Button btnKirimHasilWALab = new widget.Button();
            btnKirimHasilWALab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/whatsapp (2).png")));
            btnKirimHasilWALab.setText("Kirim Hasil ke Pasien");
            btnKirimHasilWALab.setToolTipText("Kirim hasil laboratorium final ke WhatsApp pasien");
            btnKirimHasilWALab.setName("BtnKirimHasilWALab");
            btnKirimHasilWALab.setPreferredSize(new java.awt.Dimension(150, 30));
            btnKirimHasilWALab.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kirimHasilLabFinalKeWhatsAppPasien();
                }
            });

            // Letakkan sebelum tombol Keluar agar tidak mengganggu tombol bawaan.
            if (panelisi1 != null) {
                try { panelisi1.remove(BtnKeluar); } catch (Exception ex) {}
                panelisi1.add(btnKirimHasilWALab);
                try { panelisi1.add(BtnKeluar); } catch (Exception ex) {}
                panelisi1.revalidate();
                panelisi1.repaint();
            }
        } catch (Exception ex) {
            System.out.println("Notif tombol kirim hasil WA lab : " + ex);
        }
    }

    private void kirimHasilLabFinalKeWhatsAppPasien() {
        String noRawat = ambilNoRawatTerpilihUntukKirimWALab();
        String tglPeriksa = ambilTglPeriksaTerpilihUntukKirimWALab();
        String jamPeriksa = ambilJamPeriksaTerpilihUntukKirimWALab();

        if (noRawat == null || noRawat.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silakan pilih dulu data pemeriksaan laboratorium yang akan dikirim.");
            return;
        }
        noRawat = noRawat.trim();
        tglPeriksa = tglPeriksa == null ? "" : tglPeriksa.trim();
        jamPeriksa = jamPeriksa == null ? "" : jamPeriksa.trim();

        if (tglPeriksa.equals("") || jamPeriksa.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Tanggal/Jam pemeriksaan dari baris yang dipilih belum terbaca.\n"
                    + "Silakan klik baris header pemeriksaan pasien yang akan dikirim.");
            return;
        }

        String namaPasien = Sequel.cariIsi(
                "select pasien.nm_pasien from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "where reg_periksa.no_rawat=? limit 1", noRawat);
        if (namaPasien == null || namaPasien.trim().equals("")) namaPasien = ambilNamaPasienTerpilihUntukKirimWALab();
        if (namaPasien == null) namaPasien = "";

        java.util.ArrayList<HasilPenunjangWhatsapp.Berkas> daftarBerkas = ambilBerkasLabFinalUntukKirimWA(noRawat, tglPeriksa, jamPeriksa);
        if (daftarBerkas.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Belum ada berkas hasil laboratorium final untuk baris pemeriksaan yang dipilih.\n\n"
                    + "No. Rawat : " + noRawat + "\n"
                    + "Tgl/Jam   : " + tglPeriksa + " / " + jamPeriksa + "\n\n"
                    + "Upload ulang hasil dari baris pemeriksaan ini terlebih dahulu agar file memiliki penanda tanggal dan jam pemeriksaan.");
            return;
        }

        try {
            String noHp = Sequel.cariIsi(
                    "select pasien.no_tlp from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=? limit 1", noRawat);
            String sesi = HasilPenunjangWhatsapp.buatSesi(noRawat, namaPasien, noHp,
                    "Laboratorium", daftarBerkas);
            java.util.ArrayList<String> ids = new java.util.ArrayList<String>();
            for (HasilPenunjangWhatsapp.Berkas b : daftarBerkas) ids.add(b.id);
            String urlPopup = HasilPenunjangWhatsapp.getUrlPopup(sesi, ids);
            if (urlPopup == null || urlPopup.trim().equals("")) {
                JOptionPane.showMessageDialog(null, "Popup kirim WhatsApp belum dapat dibuka.");
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new java.net.URI(urlPopup));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + urlPopup);
            }
        } catch (Exception ex) {
            System.out.println("Notif kirim hasil lab ke WhatsApp pasien : " + ex);
            JOptionPane.showMessageDialog(null, "Gagal membuka popup kirim WhatsApp.\n\nDetail : " + ex.getMessage());
        }
    }

    private java.util.ArrayList<HasilPenunjangWhatsapp.Berkas> ambilBerkasLabFinalUntukKirimWA(String noRawat, String tglPeriksa, String jamPeriksa) {
        java.util.ArrayList<HasilPenunjangWhatsapp.Berkas> daftar = new java.util.ArrayList<HasilPenunjangWhatsapp.Berkas>();
        PreparedStatement psBerkasWA = null;
        ResultSet rsBerkasWA = null;
        String kunciBerkas = buatKunciUploadLabWA(noRawat, tglPeriksa, jamPeriksa).toLowerCase(java.util.Locale.ROOT);
        try {
            psBerkasWA = koneksi.prepareStatement(
                    "select berkas_digital_perawatan.lokasi_file "
                    + "from berkas_digital_perawatan "
                    + "where berkas_digital_perawatan.no_rawat=? "
                    + "and trim(ifnull(berkas_digital_perawatan.lokasi_file,''))<>'' "
                    + "and lower(berkas_digital_perawatan.lokasi_file) like ? "
                    + "and (berkas_digital_perawatan.kode='005' "
                    + "or berkas_digital_perawatan.kode in (select master_berkas_digital.kode from master_berkas_digital "
                    + "where upper(master_berkas_digital.nama) like '%BERKAS LAB%' "
                    + "or upper(master_berkas_digital.nama) like '%LABORAT%') "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%hasillaboratorium%' "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%hasil_laboratorium%' "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%laboratorium%') "
                    + "order by berkas_digital_perawatan.lokasi_file");
            psBerkasWA.setString(1, noRawat);
            psBerkasWA.setString(2, "%" + kunciBerkas + "%");
            rsBerkasWA = psBerkasWA.executeQuery();
            int nomor = 1;
            while (rsBerkasWA.next()) {
                String lokasi = rsBerkasWA.getString("lokasi_file");
                if (lokasi == null || lokasi.trim().equals("")) continue;
                if (!berkasGambarHasilLab(lokasi)) continue;
                String namaFile = ambilNamaFileHasilLabWA(lokasi);
                daftar.add(new HasilPenunjangWhatsapp.Berkas(
                        "lab_final_" + nomor++, buatUrlBerkasLabWA(lokasi), namaFile,
                        lokasi, noRawat, "005"));
            }
        } catch (Exception ex) {
            System.out.println("Notif ambil berkas lab final untuk WA : " + ex);
        } finally {
            try { if (rsBerkasWA != null) rsBerkasWA.close(); } catch (Exception ex) {}
            try { if (psBerkasWA != null) psBerkasWA.close(); } catch (Exception ex) {}
        }
        return daftar;
    }

    private String ambilNoRawatTerpilihUntukKirimWALab() {
        String noRawat = "";
        try {
            int tab = TabRawat.getSelectedIndex();
            if (tab == 1) noRawat = ambilNilaiTableBarisKunci(tbDokter2, 0, 0);
            else if (tab == 3) noRawat = ambilNilaiTableBarisKunci(tbDokter1, 0, 0);
            else if (tab == 4) noRawat = ambilNilaiTableBarisKunci(tbDokter3, 0, 0);
            else noRawat = ambilNilaiTableBarisKunci(tbDokter, 0, 0);
        } catch (Exception ex) {}
        if (noRawat == null || noRawat.trim().equals("")) noRawat = ambilNilaiTableBarisKunci(tbDokter, 0, 0);
        if (noRawat == null || noRawat.trim().equals("")) noRawat = ambilNilaiTableBarisKunci(tbDokter2, 0, 0);
        if (noRawat == null || noRawat.trim().equals("")) noRawat = ambilNilaiTableBarisKunci(tbDokter1, 0, 0);
        if (noRawat == null || noRawat.trim().equals("")) noRawat = ambilNilaiTableBarisKunci(tbDokter3, 0, 0);
        if ((noRawat == null || noRawat.trim().equals("")) && NoRawat != null) noRawat = NoRawat.getText();
        return noRawat == null ? "" : noRawat.trim();
    }

    private String ambilTglPeriksaTerpilihUntukKirimWALab() {
        String tgl = "";
        try {
            int tab = TabRawat.getSelectedIndex();
            if (tab == 1) tgl = ambilNilaiTableBarisKunci(tbDokter2, 0, 2);
            else if (tab == 3) tgl = ambilNilaiTableBarisKunci(tbDokter1, 0, 3);
            else if (tab == 4) tgl = ambilNilaiTableBarisKunci(tbDokter3, 0, 3);
            else tgl = ambilNilaiTableBarisKunci(tbDokter, 0, 3);
        } catch (Exception ex) {}
        return tgl == null ? "" : tgl.trim();
    }

    private String ambilJamPeriksaTerpilihUntukKirimWALab() {
        String jam = "";
        try {
            int tab = TabRawat.getSelectedIndex();
            if (tab == 1) jam = ambilNilaiTableBarisKunci(tbDokter2, 0, 3);
            else if (tab == 3) jam = ambilNilaiTableBarisKunci(tbDokter1, 0, 4);
            else if (tab == 4) jam = ambilNilaiTableBarisKunci(tbDokter3, 0, 4);
            else jam = ambilNilaiTableBarisKunci(tbDokter, 0, 4);
        } catch (Exception ex) {}
        return jam == null ? "" : jam.trim();
    }

    private String ambilNamaPasienTerpilihUntukKirimWALab() {
        String nama = "";
        try {
            int tab = TabRawat.getSelectedIndex();
            if (tab == 1) nama = ambilNilaiTableBarisKunci(tbDokter2, 0, 1);
            else if (tab == 3) nama = ambilNilaiTableBarisKunci(tbDokter1, 0, 1);
            else if (tab == 4) nama = ambilNilaiTableBarisKunci(tbDokter3, 0, 1);
            else nama = ambilNilaiTableBarisKunci(tbDokter, 0, 1);
        } catch (Exception ex) {}
        if (nama == null || nama.trim().equals("")) nama = ambilNilaiTableBarisKunci(tbDokter, 0, 1);
        if (nama == null || nama.trim().equals("")) nama = ambilNilaiTableBarisKunci(tbDokter2, 0, 1);
        if (nama == null || nama.trim().equals("")) nama = ambilNilaiTableBarisKunci(tbDokter1, 0, 1);
        if (nama == null || nama.trim().equals("")) nama = ambilNilaiTableBarisKunci(tbDokter3, 0, 1);
        return nama == null ? "" : nama.trim();
    }

    private String ambilNilaiTableBarisKunci(JTable tabel, int kolomKunci, int kolomTarget) {
        try {
            if (tabel == null || tabel.getSelectedRow() < 0) return "";
            for (int baris = tabel.getSelectedRow(); baris >= 0; baris--) {
                Object kunci = tabel.getValueAt(baris, kolomKunci);
                if (kunci != null && !kunci.toString().trim().equals("")) {
                    Object nilai = tabel.getValueAt(baris, kolomTarget);
                    return nilai == null ? "" : nilai.toString().trim();
                }
            }
        } catch (Exception ex) {}
        return "";
    }

    private String buatKunciUploadLabWA(String noRawat, String tglPeriksa, String jamPeriksa) {
        return bersihkanKunciUploadLabWA(noRawat)
                + "_" + bersihkanKunciUploadLabWA(tglPeriksa)
                + "_" + bersihkanKunciUploadLabWA(jamPeriksa);
    }

    private String ambilHeaderPemeriksaanUploadLabWA(String noRawat, String tglPeriksa, String jamPeriksa) {
        StringBuilder header = new StringBuilder();
        PreparedStatement psHeaderUpload = null;
        ResultSet rsHeaderUpload = null;
        try {
            psHeaderUpload = koneksi.prepareStatement(
                    "select distinct concat(periksa_lab.kd_jenis_prw,'_',jns_perawatan_lab.nm_perawatan) as header_pemeriksaan "
                    + "from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                    + "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? "
                    + "and (periksa_lab.tgl_periksa=? or periksa_lab.tgl_periksa=?) and periksa_lab.jam=? "
                    + "order by periksa_lab.kd_jenis_prw");
            psHeaderUpload.setString(1, noRawat);
            psHeaderUpload.setString(2, tglPeriksa);
            psHeaderUpload.setString(3, Valid.SetTgl(tglPeriksa));
            psHeaderUpload.setString(4, jamPeriksa);
            rsHeaderUpload = psHeaderUpload.executeQuery();
            int jumlah = 0;
            boolean adaLebih = false;
            while (rsHeaderUpload.next()) {
                jumlah++;
                if (jumlah <= 3) {
                    String nama = bersihkanNamaFileUploadLabWA(rsHeaderUpload.getString("header_pemeriksaan"));
                    if (!nama.equals("")) {
                        if (header.length() > 0) header.append("_");
                        header.append(nama);
                    }
                } else {
                    adaLebih = true;
                    break;
                }
            }
            if (adaLebih && header.length() > 0) header.append("_DLL");
        } catch (Exception ex) {
            System.out.println("Notif ambil header pemeriksaan upload lab WA : " + ex);
        } finally {
            try { if (rsHeaderUpload != null) rsHeaderUpload.close(); } catch (Exception ex) {}
            try { if (psHeaderUpload != null) psHeaderUpload.close(); } catch (Exception ex) {}
        }
        String hasil = header.toString();
        if (hasil.length() > 100) hasil = hasil.substring(0, 100).replaceAll("_+$", "");
        return hasil.equals("") ? "Pemeriksaan_Laboratorium" : hasil;
    }

    private String bersihkanNamaFileUploadLabWA(String nilai) {
        String hasil = nilai == null ? "" : nilai.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return hasil;
    }

    private String bersihkanKunciUploadLabWA(String nilai) {
        return nilai == null ? "" : nilai.replaceAll("[^A-Za-z0-9]", "");
    }

    private String ambilNilaiTable(JTable tabel, int kolom) {
        try {
            if (tabel == null || tabel.getSelectedRow() < 0) return "";
            Object nilai = tabel.getValueAt(tabel.getSelectedRow(), kolom);
            return nilai == null ? "" : nilai.toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }


private void simpanTemporaryLabAman(String data, String aksi) {
    PreparedStatement psTempLab = null;
    try {
        java.util.ArrayList<String> nilaiTempLab = pecahNilaiTemporaryLab(data);
        if (nilaiTempLab.isEmpty()) {
            return;
        }

        StringBuilder sqlTempLab = new StringBuilder("insert into temporary_lab values (");
        for (int x = 0; x < nilaiTempLab.size(); x++) {
            if (x > 0) {
                sqlTempLab.append(",");
            }
            sqlTempLab.append("?");
        }
        sqlTempLab.append(")");

        psTempLab = koneksi.prepareStatement(sqlTempLab.toString());
        for (int x = 0; x < nilaiTempLab.size(); x++) {
            psTempLab.setString(x + 1, nilaiTempLab.get(x));
        }
        psTempLab.executeUpdate();
    } catch (Exception e) {
        System.out.println("Notifikasi simpan temporary_lab (" + aksi + ") : " + e);
    } finally {
        try {
            if (psTempLab != null) {
                psTempLab.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tutup temporary_lab : " + e);
        }
    }
}

private java.util.ArrayList<String> pecahNilaiTemporaryLab(String data) {
    java.util.ArrayList<String> hasil = new java.util.ArrayList<String>();
    if (data == null) {
        return hasil;
    }

    String nilai = data.trim();
    if (nilai.startsWith("'")) {
        nilai = nilai.substring(1);
    }
    if (nilai.endsWith("'")) {
        nilai = nilai.substring(0, nilai.length() - 1);
    }

    String[] bagian = nilai.split("','", -1);
    for (int x = 0; x < bagian.length; x++) {
        hasil.add(bagian[x].replace("''", "'"));
    }
    return hasil;
}

    private boolean berkasGambarHasilLab(String lokasiFile) {
        String lokasi = lokasiFile == null ? "" : lokasiFile.toLowerCase(java.util.Locale.ROOT).trim();
        return lokasi.endsWith(".jpg") || lokasi.endsWith(".jpeg") || lokasi.endsWith(".png")
                || lokasi.endsWith(".bmp") || lokasi.endsWith(".gif") || lokasi.endsWith(".webp");
    }

    private String ambilNamaFileHasilLabWA(String lokasiFile) {
        String lokasi = lokasiFile == null ? "" : lokasiFile.replace('\\', '/').trim();
        int idx = lokasi.lastIndexOf('/');
        return idx >= 0 ? lokasi.substring(idx + 1) : lokasi;
    }

    private String buatUrlBerkasLabWA(String lokasiFile) {
        String lokasi = lokasiFile == null ? "" : lokasiFile.replace('\\', '/').trim();
        while (lokasi.startsWith("/")) lokasi = lokasi.substring(1);
        if (lokasi.startsWith("http://") || lokasi.startsWith("https://")) {
            if (lokasi.contains("/berkasrawat/pages/upload/")) return lokasi;
            if (lokasi.contains("/berkasrawat/")) {
                lokasi = lokasi.substring(lokasi.indexOf("/berkasrawat/") + "/berkasrawat/".length());
                while (lokasi.startsWith("/")) lokasi = lokasi.substring(1);
            } else {
                return lokasi;
            }
        }
        if (lokasi.startsWith(koneksiDB.HYBRIDWEB() + "/")) lokasi = lokasi.substring((koneksiDB.HYBRIDWEB() + "/").length());
        if (lokasi.startsWith("berkasrawat/")) lokasi = lokasi.substring("berkasrawat/".length());
        if (lokasi.startsWith("pages/upload/")) lokasi = lokasi.substring("pages/upload/".length());
        else if (lokasi.startsWith("upload/")) lokasi = lokasi.substring("upload/".length());
        else if (lokasi.startsWith("pages/")) {
            lokasi = lokasi.substring("pages/".length());
            if (lokasi.startsWith("upload/")) lokasi = lokasi.substring("upload/".length());
        }
        return "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                + koneksiDB.HYBRIDWEB() + "/berkasrawat/pages/upload/" + lokasi;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Kd2 = new widget.TextBox();
        Kd3 = new widget.TextBox();
        Kd4 = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MneLFG = new javax.swing.JMenuItem();
        MnCetakLab = new javax.swing.JMenu();
        MnCetakHasilLab = new javax.swing.JMenuItem();
        MnCetakHasilLab1 = new javax.swing.JMenuItem();
        MnCetakHasilLab2 = new javax.swing.JMenuItem();
        MnCetakHasilLab3 = new javax.swing.JMenuItem();
        MnCetakHasilLab4 = new javax.swing.JMenuItem();
        MnCetakHasilLab5 = new javax.swing.JMenuItem();
        MnCetakHasilLab6 = new javax.swing.JMenuItem();
        MnCetakHasilLab7 = new javax.swing.JMenuItem();
        MnCetakHasilLab8 = new javax.swing.JMenuItem();
        MnCetakHasilLab9 = new javax.swing.JMenuItem();
        MnCetakHasilLab10 = new javax.swing.JMenuItem();
        MnCetakHasilLab22 = new javax.swing.JMenuItem();
        MnPDFLab = new javax.swing.JMenu();
        MnCetakHasilLab11 = new javax.swing.JMenuItem();
        MnCetakHasilLab12 = new javax.swing.JMenuItem();
        MnCetakHasilLab13 = new javax.swing.JMenuItem();
        MnCetakHasilLab14 = new javax.swing.JMenuItem();
        MnCetakHasilLab15 = new javax.swing.JMenuItem();
        MnCetakHasilLab16 = new javax.swing.JMenuItem();
        MnCetakHasilLab17 = new javax.swing.JMenuItem();
        MnCetakHasilLab18 = new javax.swing.JMenuItem();
        MnCetakHasilLab19 = new javax.swing.JMenuItem();
        MnCetakHasilLab20 = new javax.swing.JMenuItem();
        MnCetakHasilLab21 = new javax.swing.JMenuItem();
        MnCetakHasilLab23 = new javax.swing.JMenuItem();
        MnCetakNota = new javax.swing.JMenuItem();
        MnUbah = new javax.swing.JMenuItem();
        MnUbah1 = new javax.swing.JMenuItem();
        MnSaranKesan = new javax.swing.JMenuItem();
        MnCetakSuratCovid = new javax.swing.JMenuItem();
        ppBerkasDigital = new javax.swing.JMenuItem();
        ppRiwayat = new javax.swing.JMenuItem();
        MnSumsumtulang = new javax.swing.JMenuItem();
        WindowSaran = new javax.swing.JDialog();
        internalFrame6 = new widget.InternalFrame();
        panelGlass6 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnCloseIn5 = new widget.Button();
        jPanel3 = new javax.swing.JPanel();
        Scroll4 = new widget.ScrollPane();
        Kesan = new widget.TextArea();
        Scroll3 = new widget.ScrollPane();
        Saran = new widget.TextArea();
        WindowPelapor = new javax.swing.JDialog();
        internalFrame7 = new widget.InternalFrame();
        panelGlass7 = new widget.panelisi();
        BtnSimpanPelapor = new widget.Button();
        BtnCloseIn6 = new widget.Button();
        jPanel4 = new javax.swing.JPanel();
        Pelapor = new widget.TextBox();
        WaktuLapor = new widget.Tanggal();
        Penerima = new widget.TextBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        WaktuRegis = new widget.Tanggal();
        jLabel9 = new javax.swing.JLabel();
        HasilCito = new widget.TextBox();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        MnPelapor = new javax.swing.JMenuItem();
        MnHapusDataCito = new javax.swing.JMenuItem();
        WindowPelaporKritis = new javax.swing.JDialog();
        internalFrame8 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpanPelaporKritis = new widget.Button();
        BtnCloseIn7 = new widget.Button();
        jPanel5 = new javax.swing.JPanel();
        PelaporKritis = new widget.TextBox();
        WaktuLaporKritis = new widget.Tanggal();
        PenerimaKritis = new widget.TextBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        HasilKritis = new widget.TextBox();
        jLabel10 = new javax.swing.JLabel();
        WaktuValidasi = new widget.Tanggal();
        UmurPasien = new widget.TextBox();
        JenisKelamin = new widget.TextBox();
        jPopupMenu3 = new javax.swing.JPopupMenu();
        MnPelaporKritis = new javax.swing.JMenuItem();
        MnHapusDataKritis = new javax.swing.JMenuItem();
        WindoweLFG = new javax.swing.JDialog();
        internalFrame9 = new widget.InternalFrame();
        panelGlass9 = new widget.panelisi();
        BtnSimpanPelaporKritis1 = new widget.Button();
        BtnCloseIn8 = new widget.Button();
        jPanel6 = new javax.swing.JPanel();
        PelaporKritis1 = new widget.TextBox();
        WaktuLaporKritis1 = new widget.Tanggal();
        PenerimaKritis1 = new widget.TextBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        HasilKritis1 = new widget.TextBox();
        jLabel15 = new javax.swing.JLabel();
        WaktuValidasi1 = new widget.Tanggal();
        WindowSumTulang = new javax.swing.JDialog();
        internalFrame10 = new widget.InternalFrame();
        jPanel7 = new javax.swing.JPanel();
        LWaktuValidasi = new javax.swing.JLabel();
        TglPengambilan = new widget.Tanggal();
        scrollPane5 = new widget.ScrollPane();
        TSelularitas = new widget.TextArea();
        scrollPane6 = new widget.ScrollPane();
        TEritropoietik = new widget.TextArea();
        LSelularitas = new javax.swing.JLabel();
        LEritropoietik = new javax.swing.JLabel();
        scrollPane7 = new widget.ScrollPane();
        TLeukopoietik = new widget.TextArea();
        LLeukopoietik = new javax.swing.JLabel();
        scrollPane8 = new widget.ScrollPane();
        TTrombopoietik = new widget.TextArea();
        LTrombopoietik = new javax.swing.JLabel();
        scrollPane9 = new widget.ScrollPane();
        TSelPlasma = new widget.TextArea();
        LSelPlasma = new javax.swing.JLabel();
        LMitosis = new javax.swing.JLabel();
        scrollPane10 = new widget.ScrollPane();
        TMitosis = new widget.TextArea();
        LKesan = new javax.swing.JLabel();
        scrollPane11 = new widget.ScrollPane();
        TKesan = new widget.TextArea();
        LSaran = new javax.swing.JLabel();
        scrollPane12 = new widget.ScrollPane();
        TSaran = new widget.TextArea();
        LRatio = new javax.swing.JLabel();
        TRatio = new widget.TextBox();
        BtnSimpanSumTulang = new widget.Button();
        MnCetakHasilSumTulang = new widget.Button();
        MnUploadSumTulang = new widget.Button();
        BtnCloseIn9 = new widget.Button();
        internalFrame1 = new widget.InternalFrame();
        panelisi3 = new widget.panelisi();
        label15 = new widget.Label();
        NoRawat = new widget.TextBox();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label16 = new widget.Label();
        label13 = new widget.Label();
        kdmem = new widget.TextBox();
        kdptg = new widget.TextBox();
        nmmem = new widget.TextBox();
        nmptg = new widget.TextBox();
        btnPasien = new widget.Button();
        btnPetugas = new widget.Button();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        BtnPrintKritis1 = new widget.Button();
        BtnPrintKritis2 = new widget.Button();
        panelisi1 = new widget.panelisi();
        label10 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        label9 = new widget.Label();
        BtnHapus = new widget.Button();
        BtnAll = new widget.Button();
        BtnPrint = new widget.Button();
        BtnUpload = new widget.Button();
        BtnPrintCito = new widget.Button();
        BtnPrintKritis = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        scrollPane1 = new widget.ScrollPane();
        tbDokter = new widget.Table();
        scrollPane2 = new widget.ScrollPane();
        tbDokter2 = new widget.Table();
        Scroll = new widget.ScrollPane();
        LoadHTML1 = new widget.editorpane();
        scrollPane3 = new widget.ScrollPane();
        tbDokter1 = new widget.Table();
        scrollPane4 = new widget.ScrollPane();
        tbDokter3 = new widget.Table();

        Kd2.setName("Kd2"); // NOI18N
        Kd2.setPreferredSize(new java.awt.Dimension(207, 23));
        Kd2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kd2ActionPerformed(evt);
            }
        });

        Kd3.setName("Kd3"); // NOI18N
        Kd3.setPreferredSize(new java.awt.Dimension(207, 23));
        Kd3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kd3ActionPerformed(evt);
            }
        });

        Kd4.setName("Kd4"); // NOI18N
        Kd4.setPreferredSize(new java.awt.Dimension(207, 23));
        Kd4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kd4ActionPerformed(evt);
            }
        });

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MneLFG.setBackground(new java.awt.Color(255, 255, 254));
        MneLFG.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MneLFG.setForeground(new java.awt.Color(50, 50, 50));
        MneLFG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MneLFG.setText("Cetak eLFG + *");
        MneLFG.setName("MneLFG"); // NOI18N
        MneLFG.setPreferredSize(new java.awt.Dimension(250, 30));
        MneLFG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MneLFGActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MneLFG);

        MnCetakLab.setBackground(new java.awt.Color(250, 255, 245));
        MnCetakLab.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakLab.setText("Cetak Hasil Lab");
        MnCetakLab.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakLab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnCetakLab.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnCetakLab.setName("MnCetakLab"); // NOI18N
        MnCetakLab.setPreferredSize(new java.awt.Dimension(190, 26));

        MnCetakHasilLab.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab.setText("Model 1");
        MnCetakHasilLab.setName("MnCetakHasilLab"); // NOI18N
        MnCetakHasilLab.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLabActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab);

        MnCetakHasilLab1.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab1.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab1.setText("Model 2");
        MnCetakHasilLab1.setName("MnCetakHasilLab1"); // NOI18N
        MnCetakHasilLab1.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab1ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab1);

        MnCetakHasilLab2.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab2.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab2.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab2.setText("Model 3");
        MnCetakHasilLab2.setName("MnCetakHasilLab2"); // NOI18N
        MnCetakHasilLab2.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab2ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab2);

        MnCetakHasilLab3.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab3.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab3.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab3.setText("Model 4");
        MnCetakHasilLab3.setName("MnCetakHasilLab3"); // NOI18N
        MnCetakHasilLab3.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab3ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab3);

        MnCetakHasilLab4.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab4.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab4.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab4.setText("Model 5");
        MnCetakHasilLab4.setName("MnCetakHasilLab4"); // NOI18N
        MnCetakHasilLab4.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab4ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab4);

        MnCetakHasilLab5.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab5.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab5.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab5.setText("Model 6");
        MnCetakHasilLab5.setName("MnCetakHasilLab5"); // NOI18N
        MnCetakHasilLab5.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab5ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab5);

        MnCetakHasilLab6.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab6.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab6.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab6.setText("Model 7");
        MnCetakHasilLab6.setName("MnCetakHasilLab6"); // NOI18N
        MnCetakHasilLab6.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab6ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab6);

        MnCetakHasilLab7.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab7.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab7.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab7.setText("Model 8");
        MnCetakHasilLab7.setName("MnCetakHasilLab7"); // NOI18N
        MnCetakHasilLab7.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab7ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab7);

        MnCetakHasilLab8.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab8.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab8.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab8.setText("Model 9");
        MnCetakHasilLab8.setName("MnCetakHasilLab8"); // NOI18N
        MnCetakHasilLab8.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab8ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab8);

        MnCetakHasilLab9.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab9.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab9.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab9.setText("Model 10");
        MnCetakHasilLab9.setName("MnCetakHasilLab9"); // NOI18N
        MnCetakHasilLab9.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab9ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab9);

        MnCetakHasilLab10.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab10.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab10.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab10.setText("Model 11");
        MnCetakHasilLab10.setName("MnCetakHasilLab10"); // NOI18N
        MnCetakHasilLab10.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab10ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab10);

        MnCetakHasilLab22.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab22.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab22.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab22.setText("Model 12");
        MnCetakHasilLab22.setName("MnCetakHasilLab22"); // NOI18N
        MnCetakHasilLab22.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab22ActionPerformed(evt);
            }
        });
        MnCetakLab.add(MnCetakHasilLab22);

        jPopupMenu1.add(MnCetakLab);

        MnPDFLab.setBackground(new java.awt.Color(250, 255, 245));
        MnPDFLab.setForeground(new java.awt.Color(50, 50, 50));
        MnPDFLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPDFLab.setText("PDF Hasil Lab");
        MnPDFLab.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnPDFLab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnPDFLab.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnPDFLab.setName("MnPDFLab"); // NOI18N
        MnPDFLab.setPreferredSize(new java.awt.Dimension(190, 26));

        MnCetakHasilLab11.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab11.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab11.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab11.setText("Model 1");
        MnCetakHasilLab11.setName("MnCetakHasilLab11"); // NOI18N
        MnCetakHasilLab11.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab11ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab11);

        MnCetakHasilLab12.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab12.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab12.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab12.setText("Model 2");
        MnCetakHasilLab12.setName("MnCetakHasilLab12"); // NOI18N
        MnCetakHasilLab12.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab12ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab12);

        MnCetakHasilLab13.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab13.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab13.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab13.setText("Model 3");
        MnCetakHasilLab13.setName("MnCetakHasilLab13"); // NOI18N
        MnCetakHasilLab13.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab13ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab13);

        MnCetakHasilLab14.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab14.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab14.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab14.setText("Model 4");
        MnCetakHasilLab14.setName("MnCetakHasilLab14"); // NOI18N
        MnCetakHasilLab14.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab14ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab14);

        MnCetakHasilLab15.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab15.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab15.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab15.setText("Model 5");
        MnCetakHasilLab15.setName("MnCetakHasilLab15"); // NOI18N
        MnCetakHasilLab15.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab15ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab15);

        MnCetakHasilLab16.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab16.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab16.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab16.setText("Model 6");
        MnCetakHasilLab16.setName("MnCetakHasilLab16"); // NOI18N
        MnCetakHasilLab16.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab16ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab16);

        MnCetakHasilLab17.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab17.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab17.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab17.setText("Model 7");
        MnCetakHasilLab17.setName("MnCetakHasilLab17"); // NOI18N
        MnCetakHasilLab17.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab17ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab17);

        MnCetakHasilLab18.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab18.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab18.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab18.setText("Model 8");
        MnCetakHasilLab18.setName("MnCetakHasilLab18"); // NOI18N
        MnCetakHasilLab18.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab18ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab18);

        MnCetakHasilLab19.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab19.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab19.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab19.setText("Model 9");
        MnCetakHasilLab19.setName("MnCetakHasilLab19"); // NOI18N
        MnCetakHasilLab19.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab19ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab19);

        MnCetakHasilLab20.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab20.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab20.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab20.setText("Model 10");
        MnCetakHasilLab20.setName("MnCetakHasilLab20"); // NOI18N
        MnCetakHasilLab20.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab20ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab20);

        MnCetakHasilLab21.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab21.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab21.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab21.setText("Model 11");
        MnCetakHasilLab21.setName("MnCetakHasilLab21"); // NOI18N
        MnCetakHasilLab21.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab21ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab21);

        MnCetakHasilLab23.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakHasilLab23.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakHasilLab23.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakHasilLab23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakHasilLab23.setText("Model 12");
        MnCetakHasilLab23.setName("MnCetakHasilLab23"); // NOI18N
        MnCetakHasilLab23.setPreferredSize(new java.awt.Dimension(100, 26));
        MnCetakHasilLab23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilLab23ActionPerformed(evt);
            }
        });
        MnPDFLab.add(MnCetakHasilLab23);

        jPopupMenu1.add(MnPDFLab);

        MnCetakNota.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakNota.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakNota.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakNota.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakNota.setText("Cetak Nota Lab");
        MnCetakNota.setName("MnCetakNota"); // NOI18N
        MnCetakNota.setPreferredSize(new java.awt.Dimension(190, 26));
        MnCetakNota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakNotaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakNota);

        MnUbah.setBackground(new java.awt.Color(255, 255, 254));
        MnUbah.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnUbah.setForeground(new java.awt.Color(50, 50, 50));
        MnUbah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnUbah.setText("Ubah Periksa Lab");
        MnUbah.setName("MnUbah"); // NOI18N
        MnUbah.setPreferredSize(new java.awt.Dimension(190, 26));
        MnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnUbahActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnUbah);

        MnUbah1.setBackground(new java.awt.Color(255, 255, 254));
        MnUbah1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnUbah1.setForeground(new java.awt.Color(50, 50, 50));
        MnUbah1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnUbah1.setText("Ubah Nilai Hasil");
        MnUbah1.setName("MnUbah1"); // NOI18N
        MnUbah1.setPreferredSize(new java.awt.Dimension(190, 26));
        MnUbah1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnUbah1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnUbah1);

        MnSaranKesan.setBackground(new java.awt.Color(255, 255, 254));
        MnSaranKesan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSaranKesan.setForeground(new java.awt.Color(50, 50, 50));
        MnSaranKesan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSaranKesan.setText("Kesan & Saran");
        MnSaranKesan.setName("MnSaranKesan"); // NOI18N
        MnSaranKesan.setPreferredSize(new java.awt.Dimension(190, 26));
        MnSaranKesan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSaranKesanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSaranKesan);

        MnCetakSuratCovid.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakSuratCovid.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratCovid.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSuratCovid.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratCovid.setText("Surat Keterangan Covid");
        MnCetakSuratCovid.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnCetakSuratCovid.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnCetakSuratCovid.setName("MnCetakSuratCovid"); // NOI18N
        MnCetakSuratCovid.setPreferredSize(new java.awt.Dimension(190, 26));
        MnCetakSuratCovid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratCovidActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratCovid);

        ppBerkasDigital.setBackground(new java.awt.Color(255, 255, 254));
        ppBerkasDigital.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBerkasDigital.setForeground(new java.awt.Color(50, 50, 50));
        ppBerkasDigital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBerkasDigital.setText("Berkas Digital Perawatan");
        ppBerkasDigital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBerkasDigital.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBerkasDigital.setName("ppBerkasDigital"); // NOI18N
        ppBerkasDigital.setPreferredSize(new java.awt.Dimension(190, 26));
        ppBerkasDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBerkasDigitalBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBerkasDigital);

        ppRiwayat.setBackground(new java.awt.Color(255, 255, 254));
        ppRiwayat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppRiwayat.setForeground(new java.awt.Color(50, 50, 50));
        ppRiwayat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppRiwayat.setText("Riwayat Perawatan");
        ppRiwayat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppRiwayat.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppRiwayat.setName("ppRiwayat"); // NOI18N
        ppRiwayat.setPreferredSize(new java.awt.Dimension(190, 26));
        ppRiwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppRiwayatBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppRiwayat);

        MnSumsumtulang.setBackground(new java.awt.Color(255, 255, 254));
        MnSumsumtulang.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSumsumtulang.setForeground(new java.awt.Color(50, 50, 50));
        MnSumsumtulang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSumsumtulang.setText("Hasil Evaluasi Sum Sum Tulang");
        MnSumsumtulang.setName("MnSumsumtulang"); // NOI18N
        MnSumsumtulang.setPreferredSize(new java.awt.Dimension(190, 26));
        MnSumsumtulang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSumsumtulangActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSumsumtulang);

        WindowSaran.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowSaran.setName("WindowSaran"); // NOI18N
        WindowSaran.setUndecorated(true);
        WindowSaran.setResizable(false);

        internalFrame6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Kesan & Saran ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame6.setName("internalFrame6"); // NOI18N
        internalFrame6.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass6.setName("panelGlass6"); // NOI18N
        panelGlass6.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('U');
        BtnSimpan.setText("Update");
        BtnSimpan.setToolTipText("Alt+U");
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
        panelGlass6.add(BtnSimpan);

        BtnCloseIn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnCloseIn5.setMnemonic('U');
        BtnCloseIn5.setText("Tutup");
        BtnCloseIn5.setToolTipText("Alt+U");
        BtnCloseIn5.setName("BtnCloseIn5"); // NOI18N
        BtnCloseIn5.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCloseIn5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn5ActionPerformed(evt);
            }
        });
        panelGlass6.add(BtnCloseIn5);

        internalFrame6.add(panelGlass6, java.awt.BorderLayout.PAGE_END);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 102));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)), "Kesan :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        Scroll4.setName("Scroll4"); // NOI18N
        Scroll4.setPreferredSize(new java.awt.Dimension(182, 183));

        Kesan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(201, 206, 196)));
        Kesan.setColumns(20);
        Kesan.setRows(5);
        Kesan.setName("Kesan"); // NOI18N
        Scroll4.setViewportView(Kesan);

        jPanel3.add(Scroll4, java.awt.BorderLayout.PAGE_START);

        Scroll3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)), "Saran :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        Scroll3.setName("Scroll3"); // NOI18N

        Saran.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(201, 206, 196)));
        Saran.setColumns(20);
        Saran.setRows(5);
        Saran.setName("Saran"); // NOI18N
        Scroll3.setViewportView(Saran);

        jPanel3.add(Scroll3, java.awt.BorderLayout.CENTER);

        internalFrame6.add(jPanel3, java.awt.BorderLayout.CENTER);

        WindowSaran.getContentPane().add(internalFrame6, java.awt.BorderLayout.CENTER);

        WindowPelapor.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowPelapor.setTitle("Input Pelapor / Penerima");
        WindowPelapor.setName("WindowPelapor"); // NOI18N
        WindowPelapor.setResizable(false);

        internalFrame7.setBorder(null);
        internalFrame7.setName("internalFrame7"); // NOI18N
        internalFrame7.setPreferredSize(new java.awt.Dimension(500, 300));
        internalFrame7.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpanPelapor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanPelapor.setMnemonic('U');
        BtnSimpanPelapor.setText("Simpan");
        BtnSimpanPelapor.setToolTipText("Alt+U");
        BtnSimpanPelapor.setIconTextGap(8);
        BtnSimpanPelapor.setName("BtnSimpanPelapor"); // NOI18N
        BtnSimpanPelapor.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpanPelapor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanPelaporActionPerformed(evt);
            }
        });
        BtnSimpanPelapor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanPelaporKeyPressed(evt);
            }
        });
        panelGlass7.add(BtnSimpanPelapor);

        BtnCloseIn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        BtnCloseIn6.setMnemonic('U');
        BtnCloseIn6.setText("Tutup");
        BtnCloseIn6.setToolTipText("Alt+U");
        BtnCloseIn6.setName("BtnCloseIn6"); // NOI18N
        BtnCloseIn6.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCloseIn6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn6ActionPerformed(evt);
            }
        });
        panelGlass7.add(BtnCloseIn6);

        internalFrame7.add(panelGlass7, java.awt.BorderLayout.PAGE_END);

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(300, 50));

        Pelapor.setName("Pelapor"); // NOI18N
        Pelapor.setPreferredSize(new java.awt.Dimension(80, 30));
        Pelapor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PelaporActionPerformed(evt);
            }
        });

        WaktuLapor.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuLapor.setName("WaktuLapor"); // NOI18N
        WaktuLapor.setPreferredSize(new java.awt.Dimension(80, 30));

        Penerima.setName("Penerima"); // NOI18N
        Penerima.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Waktu Lapor  :");
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Nama Pelapor  :");
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Nama Penerima  :");
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Waktu Regis  :");
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(150, 30));

        WaktuRegis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-07-2026 13:55:45" }));
        WaktuRegis.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuRegis.setName("WaktuRegis"); // NOI18N
        WaktuRegis.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Hasil Cito  :");
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));

        HasilCito.setName("HasilCito"); // NOI18N
        HasilCito.setPreferredSize(new java.awt.Dimension(80, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Penerima, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HasilCito, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Pelapor, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuLapor, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuRegis, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(107, 107, 107))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(WaktuRegis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WaktuLapor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Pelapor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Penerima, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HasilCito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(47, Short.MAX_VALUE))
        );

        internalFrame7.add(jPanel4, java.awt.BorderLayout.CENTER);

        WindowPelapor.getContentPane().add(internalFrame7, java.awt.BorderLayout.CENTER);

        jPopupMenu2.setName("jPopupMenu2"); // NOI18N

        MnPelapor.setBackground(new java.awt.Color(255, 255, 254));
        MnPelapor.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnPelapor.setForeground(new java.awt.Color(50, 50, 50));
        MnPelapor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPelapor.setText("Input Pelapor dan Penerima");
        MnPelapor.setName("MnPelapor"); // NOI18N
        MnPelapor.setPreferredSize(new java.awt.Dimension(200, 30));
        MnPelapor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPelaporActionPerformed(evt);
            }
        });
        jPopupMenu2.add(MnPelapor);

        MnHapusDataCito.setBackground(new java.awt.Color(255, 255, 254));
        MnHapusDataCito.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnHapusDataCito.setForeground(new java.awt.Color(50, 50, 50));
        MnHapusDataCito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        MnHapusDataCito.setText("Hapus Data Salah");
        MnHapusDataCito.setName("MnHapusDataCito"); // NOI18N
        MnHapusDataCito.setPreferredSize(new java.awt.Dimension(200, 30));
        MnHapusDataCito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnHapusDataCitoActionPerformed(evt);
            }
        });
        jPopupMenu2.add(MnHapusDataCito);

        WindowPelaporKritis.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowPelaporKritis.setTitle("Input Pelapor / Penerima");
        WindowPelaporKritis.setName("WindowPelaporKritis"); // NOI18N
        WindowPelaporKritis.setResizable(false);

        internalFrame8.setBorder(null);
        internalFrame8.setName("internalFrame8"); // NOI18N
        internalFrame8.setPreferredSize(new java.awt.Dimension(500, 300));
        internalFrame8.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpanPelaporKritis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanPelaporKritis.setMnemonic('U');
        BtnSimpanPelaporKritis.setText("Simpan");
        BtnSimpanPelaporKritis.setToolTipText("Alt+U");
        BtnSimpanPelaporKritis.setIconTextGap(8);
        BtnSimpanPelaporKritis.setName("BtnSimpanPelaporKritis"); // NOI18N
        BtnSimpanPelaporKritis.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpanPelaporKritis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanPelaporKritisActionPerformed(evt);
            }
        });
        BtnSimpanPelaporKritis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanPelaporKritisKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpanPelaporKritis);

        BtnCloseIn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        BtnCloseIn7.setMnemonic('U');
        BtnCloseIn7.setText("Tutup");
        BtnCloseIn7.setToolTipText("Alt+U");
        BtnCloseIn7.setName("BtnCloseIn7"); // NOI18N
        BtnCloseIn7.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCloseIn7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn7ActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnCloseIn7);

        internalFrame8.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(300, 50));

        PelaporKritis.setName("PelaporKritis"); // NOI18N
        PelaporKritis.setPreferredSize(new java.awt.Dimension(80, 30));

        WaktuLaporKritis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-07-2026 13:55:45" }));
        WaktuLaporKritis.setToolTipText("");
        WaktuLaporKritis.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuLaporKritis.setName("WaktuLaporKritis"); // NOI18N
        WaktuLaporKritis.setPreferredSize(new java.awt.Dimension(80, 30));

        PenerimaKritis.setName("PenerimaKritis"); // NOI18N
        PenerimaKritis.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Waktu Lapor  :");
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Nama Pelapor  :");
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Nama Penerima  :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Hasil Kritis  :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 30));

        HasilKritis.setName("HasilKritis"); // NOI18N
        HasilKritis.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Waktu Validasi  :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(150, 30));

        WaktuValidasi.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuValidasi.setName("WaktuValidasi"); // NOI18N
        WaktuValidasi.setPreferredSize(new java.awt.Dimension(80, 30));

        UmurPasien.setName("UmurPasien"); // NOI18N
        UmurPasien.setPreferredSize(new java.awt.Dimension(80, 30));

        JenisKelamin.setName("JenisKelamin"); // NOI18N
        JenisKelamin.setPreferredSize(new java.awt.Dimension(80, 30));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PenerimaKritis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HasilKritis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PelaporKritis, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuValidasi, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuLaporKritis, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(UmurPasien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JenisKelamin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(64, 64, 64))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WaktuValidasi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WaktuLaporKritis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PelaporKritis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PenerimaKritis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HasilKritis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(85, 85, 85)
                .addComponent(UmurPasien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        internalFrame8.add(jPanel5, java.awt.BorderLayout.CENTER);

        WindowPelaporKritis.getContentPane().add(internalFrame8, java.awt.BorderLayout.CENTER);

        jPopupMenu3.setName("jPopupMenu3"); // NOI18N

        MnPelaporKritis.setBackground(new java.awt.Color(255, 255, 254));
        MnPelaporKritis.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnPelaporKritis.setForeground(new java.awt.Color(50, 50, 50));
        MnPelaporKritis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPelaporKritis.setText("Input Pelapor, Penerima, Hasil Kritis");
        MnPelaporKritis.setName("MnPelaporKritis"); // NOI18N
        MnPelaporKritis.setPreferredSize(new java.awt.Dimension(250, 30));
        MnPelaporKritis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPelaporKritisActionPerformed(evt);
            }
        });
        jPopupMenu3.add(MnPelaporKritis);

        MnHapusDataKritis.setBackground(new java.awt.Color(255, 255, 254));
        MnHapusDataKritis.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnHapusDataKritis.setForeground(new java.awt.Color(50, 50, 50));
        MnHapusDataKritis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        MnHapusDataKritis.setText("Hapus Data Salah");
        MnHapusDataKritis.setName("MnHapusDataKritis"); // NOI18N
        MnHapusDataKritis.setPreferredSize(new java.awt.Dimension(200, 30));
        MnHapusDataKritis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnHapusDataKritisActionPerformed(evt);
            }
        });
        jPopupMenu3.add(MnHapusDataKritis);

        WindoweLFG.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindoweLFG.setTitle("Input Pelapor / Penerima");
        WindoweLFG.setName("WindoweLFG"); // NOI18N
        WindoweLFG.setResizable(false);

        internalFrame9.setBorder(null);
        internalFrame9.setName("internalFrame9"); // NOI18N
        internalFrame9.setPreferredSize(new java.awt.Dimension(500, 300));
        internalFrame9.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpanPelaporKritis1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanPelaporKritis1.setMnemonic('U');
        BtnSimpanPelaporKritis1.setText("Simpan");
        BtnSimpanPelaporKritis1.setToolTipText("Alt+U");
        BtnSimpanPelaporKritis1.setIconTextGap(8);
        BtnSimpanPelaporKritis1.setName("BtnSimpanPelaporKritis1"); // NOI18N
        BtnSimpanPelaporKritis1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpanPelaporKritis1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanPelaporKritis1ActionPerformed(evt);
            }
        });
        BtnSimpanPelaporKritis1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanPelaporKritis1KeyPressed(evt);
            }
        });
        panelGlass9.add(BtnSimpanPelaporKritis1);

        BtnCloseIn8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        BtnCloseIn8.setMnemonic('U');
        BtnCloseIn8.setText("Tutup");
        BtnCloseIn8.setToolTipText("Alt+U");
        BtnCloseIn8.setName("BtnCloseIn8"); // NOI18N
        BtnCloseIn8.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCloseIn8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn8ActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnCloseIn8);

        internalFrame9.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(300, 50));

        PelaporKritis1.setName("PelaporKritis1"); // NOI18N
        PelaporKritis1.setPreferredSize(new java.awt.Dimension(80, 30));

        WaktuLaporKritis1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-07-2026 13:55:46" }));
        WaktuLaporKritis1.setToolTipText("");
        WaktuLaporKritis1.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuLaporKritis1.setName("WaktuLaporKritis1"); // NOI18N
        WaktuLaporKritis1.setPreferredSize(new java.awt.Dimension(80, 30));

        PenerimaKritis1.setName("PenerimaKritis1"); // NOI18N
        PenerimaKritis1.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Waktu Lapor  :");
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Nama Pelapor  :");
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Nama Penerima  :");
        jLabel13.setName("jLabel13"); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Hasil Kritis  :");
        jLabel14.setName("jLabel14"); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(150, 30));

        HasilKritis1.setName("HasilKritis1"); // NOI18N
        HasilKritis1.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Waktu Validasi  :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(150, 30));

        WaktuValidasi1.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        WaktuValidasi1.setName("WaktuValidasi1"); // NOI18N
        WaktuValidasi1.setPreferredSize(new java.awt.Dimension(80, 30));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PenerimaKritis1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HasilKritis1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PelaporKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuValidasi1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaktuLaporKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(64, 64, 64))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(31, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WaktuValidasi1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WaktuLaporKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PelaporKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PenerimaKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HasilKritis1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );

        internalFrame9.add(jPanel6, java.awt.BorderLayout.CENTER);

        WindoweLFG.getContentPane().add(internalFrame9, java.awt.BorderLayout.CENTER);

        WindowSumTulang.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowSumTulang.setTitle("Hasil Evaluasi Sumsum Tulang");
        WindowSumTulang.setBackground(new java.awt.Color(255, 255, 255));
        WindowSumTulang.setName("WindowSumTulang"); // NOI18N
        WindowSumTulang.setUndecorated(true);
        WindowSumTulang.setSize(new java.awt.Dimension(1300, 650));
        WindowSumTulang.getContentPane().setLayout(null);

        internalFrame10.setBorder(null);
        internalFrame10.setName("internalFrame10"); // NOI18N
        internalFrame10.setPreferredSize(new java.awt.Dimension(1300, 620));
        internalFrame10.setLayout(new java.awt.BorderLayout());

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setPreferredSize(new java.awt.Dimension(1300, 620));
        jPanel7.setLayout(null);

        LWaktuValidasi.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LWaktuValidasi.setForeground(new java.awt.Color(51, 51, 51));
        LWaktuValidasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LWaktuValidasi.setText("Tanggal Pengambilan Sum Sum Tulang  :");
        LWaktuValidasi.setName("LWaktuValidasi"); // NOI18N
        LWaktuValidasi.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LWaktuValidasi);
        LWaktuValidasi.setBounds(20, 20, 240, 30);

        TglPengambilan.setDisplayFormat("dd-MM-yyyy");
        TglPengambilan.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        TglPengambilan.setName("TglPengambilan"); // NOI18N
        TglPengambilan.setPreferredSize(new java.awt.Dimension(80, 30));
        jPanel7.add(TglPengambilan);
        TglPengambilan.setBounds(250, 20, 170, 30);

        scrollPane5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane5.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane5.setName("scrollPane5"); // NOI18N

        TSelularitas.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TSelularitas.setColumns(20);
        TSelularitas.setForeground(new java.awt.Color(0, 0, 0));
        TSelularitas.setRows(5);
        TSelularitas.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TSelularitas.setName("TSelularitas"); // NOI18N
        TSelularitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TSelularitasKeyPressed(evt);
            }
        });
        scrollPane5.setViewportView(TSelularitas);

        jPanel7.add(scrollPane5);
        scrollPane5.setBounds(20, 90, 400, 70);

        scrollPane6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane6.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane6.setName("scrollPane6"); // NOI18N

        TEritropoietik.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TEritropoietik.setColumns(20);
        TEritropoietik.setForeground(new java.awt.Color(0, 0, 0));
        TEritropoietik.setRows(5);
        TEritropoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TEritropoietik.setName("TEritropoietik"); // NOI18N
        TEritropoietik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TEritropoietikKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(TEritropoietik);

        jPanel7.add(scrollPane6);
        scrollPane6.setBounds(430, 90, 400, 70);

        LSelularitas.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LSelularitas.setForeground(new java.awt.Color(51, 51, 51));
        LSelularitas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LSelularitas.setText("Selularitas  : ");
        LSelularitas.setName("LSelularitas"); // NOI18N
        LSelularitas.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LSelularitas);
        LSelularitas.setBounds(20, 60, 300, 30);

        LEritropoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LEritropoietik.setForeground(new java.awt.Color(51, 51, 51));
        LEritropoietik.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LEritropoietik.setText("Eritropoietik  : ");
        LEritropoietik.setName("LEritropoietik"); // NOI18N
        LEritropoietik.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LEritropoietik);
        LEritropoietik.setBounds(430, 60, 400, 30);

        scrollPane7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane7.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane7.setName("scrollPane7"); // NOI18N

        TLeukopoietik.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TLeukopoietik.setColumns(20);
        TLeukopoietik.setForeground(new java.awt.Color(0, 0, 0));
        TLeukopoietik.setRows(5);
        TLeukopoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TLeukopoietik.setName("TLeukopoietik"); // NOI18N
        TLeukopoietik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TLeukopoietikKeyPressed(evt);
            }
        });
        scrollPane7.setViewportView(TLeukopoietik);

        jPanel7.add(scrollPane7);
        scrollPane7.setBounds(840, 90, 400, 70);

        LLeukopoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LLeukopoietik.setForeground(new java.awt.Color(51, 51, 51));
        LLeukopoietik.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LLeukopoietik.setText("Leukopoietik  :");
        LLeukopoietik.setName("LLeukopoietik"); // NOI18N
        LLeukopoietik.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LLeukopoietik);
        LLeukopoietik.setBounds(840, 60, 400, 30);

        scrollPane8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane8.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane8.setName("scrollPane8"); // NOI18N

        TTrombopoietik.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TTrombopoietik.setColumns(20);
        TTrombopoietik.setForeground(new java.awt.Color(0, 0, 0));
        TTrombopoietik.setRows(5);
        TTrombopoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TTrombopoietik.setName("TTrombopoietik"); // NOI18N
        TTrombopoietik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TTrombopoietikKeyPressed(evt);
            }
        });
        scrollPane8.setViewportView(TTrombopoietik);

        jPanel7.add(scrollPane8);
        scrollPane8.setBounds(20, 200, 400, 70);

        LTrombopoietik.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LTrombopoietik.setForeground(new java.awt.Color(51, 51, 51));
        LTrombopoietik.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTrombopoietik.setText("Trombopoietik  :");
        LTrombopoietik.setName("LTrombopoietik"); // NOI18N
        LTrombopoietik.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LTrombopoietik);
        LTrombopoietik.setBounds(20, 170, 400, 30);

        scrollPane9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane9.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane9.setName("scrollPane9"); // NOI18N

        TSelPlasma.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TSelPlasma.setColumns(20);
        TSelPlasma.setForeground(new java.awt.Color(0, 0, 0));
        TSelPlasma.setRows(5);
        TSelPlasma.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TSelPlasma.setName("TSelPlasma"); // NOI18N
        TSelPlasma.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TSelPlasmaKeyPressed(evt);
            }
        });
        scrollPane9.setViewportView(TSelPlasma);

        jPanel7.add(scrollPane9);
        scrollPane9.setBounds(430, 200, 400, 70);

        LSelPlasma.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LSelPlasma.setForeground(new java.awt.Color(51, 51, 51));
        LSelPlasma.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LSelPlasma.setText("Sel Plasma  :");
        LSelPlasma.setName("LSelPlasma"); // NOI18N
        LSelPlasma.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LSelPlasma);
        LSelPlasma.setBounds(430, 170, 400, 30);

        LMitosis.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LMitosis.setForeground(new java.awt.Color(51, 51, 51));
        LMitosis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LMitosis.setText("Mitosis  :");
        LMitosis.setName("LMitosis"); // NOI18N
        LMitosis.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LMitosis);
        LMitosis.setBounds(840, 170, 400, 30);

        scrollPane10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane10.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane10.setName("scrollPane10"); // NOI18N

        TMitosis.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TMitosis.setColumns(20);
        TMitosis.setForeground(new java.awt.Color(0, 0, 0));
        TMitosis.setRows(5);
        TMitosis.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TMitosis.setName("TMitosis"); // NOI18N
        TMitosis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TMitosisKeyPressed(evt);
            }
        });
        scrollPane10.setViewportView(TMitosis);

        jPanel7.add(scrollPane10);
        scrollPane10.setBounds(840, 200, 400, 70);

        LKesan.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LKesan.setForeground(new java.awt.Color(51, 51, 51));
        LKesan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LKesan.setText("Kesan  :");
        LKesan.setName("LKesan"); // NOI18N
        LKesan.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LKesan);
        LKesan.setBounds(20, 320, 400, 30);

        scrollPane11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane11.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane11.setName("scrollPane11"); // NOI18N

        TKesan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TKesan.setColumns(20);
        TKesan.setForeground(new java.awt.Color(0, 0, 0));
        TKesan.setRows(5);
        TKesan.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TKesan.setName("TKesan"); // NOI18N
        TKesan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TKesanKeyPressed(evt);
            }
        });
        scrollPane11.setViewportView(TKesan);

        jPanel7.add(scrollPane11);
        scrollPane11.setBounds(20, 350, 400, 70);

        LSaran.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LSaran.setForeground(new java.awt.Color(51, 51, 51));
        LSaran.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LSaran.setText("Saran  :");
        LSaran.setName("LSaran"); // NOI18N
        LSaran.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LSaran);
        LSaran.setBounds(430, 320, 400, 30);

        scrollPane12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane12.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane12.setName("scrollPane12"); // NOI18N

        TSaran.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TSaran.setColumns(20);
        TSaran.setForeground(new java.awt.Color(0, 0, 0));
        TSaran.setRows(5);
        TSaran.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        TSaran.setName("TSaran"); // NOI18N
        TSaran.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TSaranKeyPressed(evt);
            }
        });
        scrollPane12.setViewportView(TSaran);

        jPanel7.add(scrollPane12);
        scrollPane12.setBounds(430, 350, 400, 70);

        LRatio.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        LRatio.setForeground(new java.awt.Color(51, 51, 51));
        LRatio.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LRatio.setText("Ratio Mieloid-Eritroid  :");
        LRatio.setName("LRatio"); // NOI18N
        LRatio.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel7.add(LRatio);
        LRatio.setBounds(20, 280, 160, 30);

        TRatio.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        TRatio.setName("TRatio"); // NOI18N
        jPanel7.add(TRatio);
        TRatio.setBounds(150, 280, 270, 30);

        BtnSimpanSumTulang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanSumTulang.setMnemonic('U');
        BtnSimpanSumTulang.setText("Simpan");
        BtnSimpanSumTulang.setToolTipText("Alt+U");
        BtnSimpanSumTulang.setIconTextGap(8);
        BtnSimpanSumTulang.setName("BtnSimpanSumTulang"); // NOI18N
        BtnSimpanSumTulang.setPreferredSize(new java.awt.Dimension(90, 30));
        BtnSimpanSumTulang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanSumTulangActionPerformed(evt);
            }
        });
        BtnSimpanSumTulang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanSumTulangKeyPressed(evt);
            }
        });
        jPanel7.add(BtnSimpanSumTulang);
        BtnSimpanSumTulang.setBounds(20, 440, 90, 30);

        MnCetakHasilSumTulang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/printer_blue.png"))); // NOI18N
        MnCetakHasilSumTulang.setMnemonic('U');
        MnCetakHasilSumTulang.setText("Cetak");
        MnCetakHasilSumTulang.setToolTipText("Alt+U");
        MnCetakHasilSumTulang.setIconTextGap(8);
        MnCetakHasilSumTulang.setName("MnCetakHasilSumTulang"); // NOI18N
        MnCetakHasilSumTulang.setPreferredSize(new java.awt.Dimension(90, 30));
        MnCetakHasilSumTulang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakHasilSumTulangActionPerformed(evt);
            }
        });
        jPanel7.add(MnCetakHasilSumTulang);
        MnCetakHasilSumTulang.setBounds(120, 440, 90, 30);

        MnUploadSumTulang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        MnUploadSumTulang.setMnemonic('U');
        MnUploadSumTulang.setText("Upload");
        MnUploadSumTulang.setToolTipText("Alt+U");
        MnUploadSumTulang.setIconTextGap(8);
        MnUploadSumTulang.setName("MnUploadSumTulang"); // NOI18N
        MnUploadSumTulang.setPreferredSize(new java.awt.Dimension(90, 30));
        MnUploadSumTulang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnUploadSumTulangActionPerformed(evt);
            }
        });
        jPanel7.add(MnUploadSumTulang);
        MnUploadSumTulang.setBounds(210, 440, 90, 30);

        BtnCloseIn9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        BtnCloseIn9.setMnemonic('U');
        BtnCloseIn9.setText("Tutup");
        BtnCloseIn9.setToolTipText("Alt+U");
        BtnCloseIn9.setIconTextGap(8);
        BtnCloseIn9.setName("BtnCloseIn9"); // NOI18N
        BtnCloseIn9.setPreferredSize(new java.awt.Dimension(90, 30));
        BtnCloseIn9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn9ActionPerformed(evt);
            }
        });
        jPanel7.add(BtnCloseIn9);
        BtnCloseIn9.setBounds(310, 440, 90, 30);

        internalFrame10.add(jPanel7, java.awt.BorderLayout.CENTER);

        WindowSumTulang.getContentPane().add(internalFrame10);
        internalFrame10.setBounds(3, 3, 1300, 620);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pemeriksaan Laboratorium Patologi Klinis ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 73));
        panelisi3.setLayout(null);

        label15.setText("No.Rawat :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label15);
        label15.setBounds(0, 10, 75, 23);

        NoRawat.setName("NoRawat"); // NOI18N
        NoRawat.setPreferredSize(new java.awt.Dimension(207, 23));
        NoRawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRawatKeyPressed(evt);
            }
        });
        panelisi3.add(NoRawat);
        NoRawat.setBounds(79, 10, 226, 23);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label11);
        label11.setBounds(0, 40, 75, 23);

        Tgl1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-07-2026" }));
        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        panelisi3.add(Tgl1);
        Tgl1.setBounds(79, 40, 100, 23);

        label16.setText("Pasien :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label16);
        label16.setBounds(385, 10, 60, 23);

        label13.setText("Petugas :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label13);
        label13.setBounds(385, 40, 60, 23);

        kdmem.setName("kdmem"); // NOI18N
        kdmem.setPreferredSize(new java.awt.Dimension(80, 23));
        kdmem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdmemKeyPressed(evt);
            }
        });
        panelisi3.add(kdmem);
        kdmem.setBounds(449, 10, 80, 23);

        kdptg.setName("kdptg"); // NOI18N
        kdptg.setPreferredSize(new java.awt.Dimension(80, 23));
        kdptg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdptgKeyPressed(evt);
            }
        });
        panelisi3.add(kdptg);
        kdptg.setBounds(449, 40, 80, 23);

        nmmem.setEditable(false);
        nmmem.setName("nmmem"); // NOI18N
        nmmem.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmmem);
        nmmem.setBounds(531, 10, 240, 23);

        nmptg.setEditable(false);
        nmptg.setName("nmptg"); // NOI18N
        nmptg.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmptg);
        nmptg.setBounds(531, 40, 240, 23);

        btnPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPasien.setMnemonic('1');
        btnPasien.setToolTipText("Alt+1");
        btnPasien.setName("btnPasien"); // NOI18N
        btnPasien.setPreferredSize(new java.awt.Dimension(28, 23));
        btnPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasienActionPerformed(evt);
            }
        });
        panelisi3.add(btnPasien);
        btnPasien.setBounds(774, 10, 28, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("Alt+2");
        btnPetugas.setName("btnPetugas"); // NOI18N
        btnPetugas.setPreferredSize(new java.awt.Dimension(28, 23));
        btnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasActionPerformed(evt);
            }
        });
        panelisi3.add(btnPetugas);
        btnPetugas.setBounds(774, 40, 28, 23);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label18);
        label18.setBounds(178, 40, 30, 23);

        Tgl2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-07-2026" }));
        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelisi3.add(Tgl2);
        Tgl2.setBounds(205, 40, 100, 23);

        BtnPrintKritis1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrintKritis1.setMnemonic('T');
        BtnPrintKritis1.setText("Rekap Pembayaran Ralan");
        BtnPrintKritis1.setToolTipText("Alt+T");
        BtnPrintKritis1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPrintKritis1.setName("BtnPrintKritis1"); // NOI18N
        BtnPrintKritis1.setPreferredSize(new java.awt.Dimension(190, 30));
        BtnPrintKritis1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintKritis1ActionPerformed(evt);
            }
        });
        BtnPrintKritis1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKritis1KeyPressed(evt);
            }
        });
        panelisi3.add(BtnPrintKritis1);
        BtnPrintKritis1.setBounds(810, 10, 180, 26);

        BtnPrintKritis2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrintKritis2.setMnemonic('T');
        BtnPrintKritis2.setText("Rekap Pembayaran Ranap");
        BtnPrintKritis2.setToolTipText("Alt+T");
        BtnPrintKritis2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPrintKritis2.setName("BtnPrintKritis2"); // NOI18N
        BtnPrintKritis2.setPreferredSize(new java.awt.Dimension(190, 30));
        BtnPrintKritis2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintKritis2ActionPerformed(evt);
            }
        });
        BtnPrintKritis2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKritis2KeyPressed(evt);
            }
        });
        panelisi3.add(BtnPrintKritis2);
        BtnPrintKritis2.setBounds(990, 10, 180, 26);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_START);

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(100, 56));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label10.setText("Key Word :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi1.add(label10);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(240, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi1.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('5');
        BtnCari.setToolTipText("Alt+5");
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
        panelisi1.add(BtnCari);

        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(30, 30));
        panelisi1.add(label9);

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
        panelisi1.add(BtnHapus);

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
        panelisi1.add(BtnAll);

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
        panelisi1.add(BtnPrint);

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
        panelisi1.add(BtnUpload);

        BtnPrintCito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrintCito.setMnemonic('T');
        BtnPrintCito.setText("Rekap Cito");
        BtnPrintCito.setToolTipText("Alt+T");
        BtnPrintCito.setName("BtnPrintCito"); // NOI18N
        BtnPrintCito.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrintCito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintCitoActionPerformed(evt);
            }
        });
        BtnPrintCito.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintCitoKeyPressed(evt);
            }
        });
        panelisi1.add(BtnPrintCito);

        BtnPrintKritis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrintKritis.setMnemonic('T');
        BtnPrintKritis.setText("Rekap Kritis");
        BtnPrintKritis.setToolTipText("Alt+T");
        BtnPrintKritis.setName("BtnPrintKritis"); // NOI18N
        BtnPrintKritis.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrintKritis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintKritisActionPerformed(evt);
            }
        });
        BtnPrintKritis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKritisKeyPressed(evt);
            }
        });
        panelisi1.add(BtnPrintKritis);

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
        panelisi1.add(BtnKeluar);

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbDokter.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokter.setComponentPopupMenu(jPopupMenu1);
        tbDokter.setName("tbDokter"); // NOI18N
        tbDokter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokterMouseClicked(evt);
            }
        });
        tbDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDokterKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(tbDokter);

        TabRawat.addTab("Data Pemeriksaan", scrollPane1);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        scrollPane2.setName("scrollPane2"); // NOI18N
        scrollPane2.setOpaque(true);

        tbDokter2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokter2.setName("tbDokter2"); // NOI18N
        tbDokter2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokter2MouseClicked(evt);
            }
        });
        tbDokter2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDokter2KeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(tbDokter2);

        TabRawat.addTab("Item Pemeriksaan", scrollPane2);

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        LoadHTML1.setBorder(null);
        LoadHTML1.setName("LoadHTML1"); // NOI18N
        Scroll.setViewportView(LoadHTML1);

        TabRawat.addTab("Detail Kunjungan", Scroll);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        scrollPane3.setName("scrollPane3"); // NOI18N
        scrollPane3.setOpaque(true);

        tbDokter1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokter1.setComponentPopupMenu(jPopupMenu2);
        tbDokter1.setName("tbDokter1"); // NOI18N
        tbDokter1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokter1MouseClicked(evt);
            }
        });
        tbDokter1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDokter1KeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(tbDokter1);

        TabRawat.addTab("Monitoring Hasil Lab Cito", scrollPane3);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        scrollPane4.setComponentPopupMenu(jPopupMenu3);
        scrollPane4.setName("scrollPane4"); // NOI18N
        scrollPane4.setOpaque(true);

        tbDokter3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokter3.setComponentPopupMenu(jPopupMenu3);
        tbDokter3.setName("tbDokter3"); // NOI18N
        tbDokter3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokter3MouseClicked(evt);
            }
        });
        tbDokter3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDokter3KeyPressed(evt);
            }
        });
        scrollPane4.setViewportView(tbDokter3);

        TabRawat.addTab(" Monitoring Hasil Lab Kritis ", scrollPane4);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
/*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

    private void btnPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasienActionPerformed
        akses.setform("DlgCariPeriksaLab");
        member.emptTeks();
        member.isCek();
        member.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        member.setLocationRelativeTo(internalFrame1);
        member.setAlwaysOnTop(false);
        member.setVisible(true);
    }//GEN-LAST:event_btnPasienActionPerformed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        akses.setform("DlgCariPeriksaLab");
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setAlwaysOnTop(false);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        Valid.pindah(evt,kdmem,Tgl2);
    }//GEN-LAST:event_Tgl1KeyPressed

    private void kdmemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdmemKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=?", nmmem,kdmem.getText());      
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=?", nmmem,kdmem.getText());
            NoRawat.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPasienActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=?", nmmem,kdmem.getText());
            Tgl1.requestFocus();      
        }
    }//GEN-LAST:event_kdmemKeyPressed

    private void NoRawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRawatKeyPressed
        Valid.pindah(evt, BtnKeluar, kdptg);
    }//GEN-LAST:event_NoRawatKeyPressed

    private void kdptgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdptgKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            nmptg.setText(petugas.tampil3(kdptg.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            nmptg.setText(petugas.tampil3(kdptg.getText()));
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            nmptg.setText(petugas.tampil3(kdptg.getText()));
            NoRawat.requestFocus();            
        }
    }//GEN-LAST:event_kdptgKeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed
        Valid.pindah(evt, Tgl1,kdptg);
    }//GEN-LAST:event_Tgl2KeyPressed

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
        TabRawatMouseClicked(null);
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
        NoRawat.setText("");
        kdmem.setText("");
        nmmem.setText("");
        kdptg.setText("");
        nmptg.setText("");
        TabRawatMouseClicked(null);
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));        
        switch (TabRawat.getSelectedIndex()) {
            case 0:
                if(tabMode.getRowCount()==0){
                    JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    TCari.requestFocus();
                }else if(tabMode.getRowCount()!=0){
                    
                    Sequel.queryu("delete from temporary_lab");
                    int row=tabMode.getRowCount();
                    for(i=0;i<row;i++){
                        simpanTemporaryLabAman("'0','"+
                                tabMode.getValueAt(i,0).toString()+"','"+
                                tabMode.getValueAt(i,1).toString()+"','"+
                                tabMode.getValueAt(i,2).toString()+"','"+
                                tabMode.getValueAt(i,3).toString()+"','"+
                                tabMode.getValueAt(i,4).toString()+"','"+
                                tabMode.getValueAt(i,5).toString()+"','"+
                                tabMode.getValueAt(i,6).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Periksa Lab");
                    }
                    
                    Map<String, Object> param = new HashMap<>();
                    param.put("namars",akses.getnamars());
                    param.put("alamatrs",akses.getalamatrs());
                    param.put("kotars",akses.getkabupatenrs());
                    param.put("propinsirs",akses.getpropinsirs());
                    param.put("kontakrs",akses.getkontakrs());
                    param.put("emailrs",akses.getemailrs());
                    param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
                    Valid.MyReport("rptDataLab.jasper","report","::[ Data Pemeriksaan Laboratorium ]::",param);
                }   break;
            case 1:
                if(tabMode2.getRowCount()==0){
                    JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    TCari.requestFocus();
                }else if(tabMode2.getRowCount()!=0){
                    
                    Sequel.queryu("delete from temporary_lab");
                    int row=tabMode2.getRowCount();
                    for(i=0;i<row;i++){
                        simpanTemporaryLabAman("'0','"+
                                tabMode2.getValueAt(i,0).toString()+"','"+
                                tabMode2.getValueAt(i,1).toString()+"','"+
                                tabMode2.getValueAt(i,2).toString()+"','"+
                                tabMode2.getValueAt(i,3).toString()+"','"+
                                tabMode2.getValueAt(i,4).toString()+"','"+
                                tabMode2.getValueAt(i,5).toString()+"','"+
                                tabMode2.getValueAt(i,6).toString()+"','"+
                                tabMode2.getValueAt(i,7).toString()+"','"+
                                tabMode2.getValueAt(i,8).toString()+"','"+
                                tabMode2.getValueAt(i,9).toString()+"','"+
                                tabMode2.getValueAt(i,10).toString()+"','"+
                                tabMode2.getValueAt(i,11).toString()+"','"+
                                tabMode2.getValueAt(i,12).toString()+"','"+
                                tabMode2.getValueAt(i,13).toString()+"','','','','','','','','','','','','','','','','','','','','','','',''","Periksa Lab");
                    }
                    
                    Map<String, Object> param = new HashMap<>();
                    param.put("namars",akses.getnamars());
                    param.put("alamatrs",akses.getalamatrs());
                    param.put("kotars",akses.getkabupatenrs());
                    param.put("propinsirs",akses.getpropinsirs());
                    param.put("kontakrs",akses.getkontakrs());
                    param.put("emailrs",akses.getemailrs());
                    param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
                    Valid.MyReport("rptDataLab2.jasper","report","::[ Data Item Pemeriksaan Laboratorium ]::",param);
                }   break;
            case 2:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {

                    File g = new File("file2.css");            
                    BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                    bg.write(
                        ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".head td{border-right: 1px solid #777777;font: 8.5px tahoma;height:10px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;}"+
                        ".isi2 td{font: 8.5px tahoma;height:12px;background: #ffffff;color:#323232;}"+
                        ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                    );
                    bg.close();

                    File f = new File("DetailKunjunganLab.html");            
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                    bw.write(LoadHTML1.getText().replaceAll("<head>","<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                            "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                "<tr class='isi2'>"+
                                    "<td valign='top' align='center'>"+
                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                        "<font size='2' face='Tahoma'>DETAIL KUNJUNGAN LAB PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                    "</td>"+
                               "</tr>"+
                            "</table>")
                    );
                    bw.close();                         
                    Desktop.getDesktop().browse(f.toURI());
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                }
                this.setCursor(Cursor.getDefaultCursor());
                break;
            default:
                break;
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnAll,BtnAll);
        }
    }//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        petugas.dispose();
        member.dispose();
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,NoRawat);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
    if(TabRawat.getSelectedIndex()==0){
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            if(Sequel.cariRegistrasi(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString())>0){
                JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi, data tidak boleh dihapus.\nSilahkan hubungi bagian kasir/keuangan ..!!");
                TCari.requestFocus();
            }else{
                int reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah bener belum data yang mau dihapus..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try{
                        Sequel.AutoComitFalse();
                        sukses=true;
                        status="";
                        ttljmdokter=0;ttljmpetugas=0;ttlkso=0;ttlpendapatan=0;ttlbhp=0;ttljasasarana=0;ttljmperujuk=0;ttlmenejemen=0;
                        ttljmdokter=Sequel.cariIsiAngka("select sum(tarif_tindakan_dokter) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmpetugas=Sequel.cariIsiAngka("select sum(tarif_tindakan_petugas) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlkso=Sequel.cariIsiAngka("select sum(kso) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlbhp=Sequel.cariIsiAngka("select sum(bhp) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlpendapatan=Sequel.cariIsiAngka("select sum(biaya) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljasasarana=Sequel.cariIsiAngka("select sum(bagian_rs) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmperujuk=Sequel.cariIsiAngka("select sum(tarif_perujuk) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlmenejemen=Sequel.cariIsiAngka("select sum(menejemen) from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");

                        ttljmdokter=ttljmdokter+Sequel.cariIsiAngka("select sum(bagian_dokter) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmpetugas=ttljmpetugas+Sequel.cariIsiAngka("select sum(bagian_laborat) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlkso=ttlkso+Sequel.cariIsiAngka("select sum(kso) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlbhp=ttlbhp+Sequel.cariIsiAngka("select sum(bhp) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlpendapatan=ttlpendapatan+Sequel.cariIsiAngka("select sum(biaya_item) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljasasarana=ttljasasarana+Sequel.cariIsiAngka("select sum(bagian_rs) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmperujuk=ttljmperujuk+Sequel.cariIsiAngka("select sum(bagian_perujuk) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlmenejemen=ttlmenejemen+Sequel.cariIsiAngka("select sum(menejemen) from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");

                        status=Sequel.cariIsi("select status from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                      "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                      "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");

                        if(Sequel.queryutf("delete from periksa_lab where periksa_lab.kategori='PK' and no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                      "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                      "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'")==true){                    
                            if(Sequel.queryutf("delete from detail_periksa_lab where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                      "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                      "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'")==false){
                                    sukses=false;                   
                            }
                        }else{
                            sukses=false;
                        }

                        if(sukses==true){
                            if(status.equals("Ranap")){
                                Sequel.queryu("delete from tampjurnal");    
                                if(ttlpendapatan>0){
                                    Sequel.menyimpan("tampjurnal","'"+Suspen_Piutang_Laborat_Ranap+"','Suspen Piutang Laborat Ranap','0','"+ttlpendapatan+"'","kredit=kredit+'"+(ttlpendapatan)+"'","kd_rek='"+Suspen_Piutang_Laborat_Ranap+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Laborat_Ranap+"','Pendapatan Laborat Rawat Inap','"+ttlpendapatan+"','0'","debet=debet+'"+(ttlpendapatan)+"'","kd_rek='"+Laborat_Ranap+"'");                              
                                }
                                if(ttljmdokter>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Dokter_Laborat_Ranap+"','Beban Jasa Medik Dokter Laborat Ranap','0','"+ttljmdokter+"'","kredit=kredit+'"+(ttljmdokter)+"'","kd_rek='"+Beban_Jasa_Medik_Dokter_Laborat_Ranap+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Dokter_Laborat_Ranap+"','Utang Jasa Medik Dokter Laborat Ranap','"+ttljmdokter+"','0'","debet=debet+'"+(ttljmdokter)+"'","kd_rek='"+Utang_Jasa_Medik_Dokter_Laborat_Ranap+"'");                           
                                }
                                if(ttljmpetugas>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Petugas_Laborat_Ranap+"','Beban Jasa Medik Petugas Laborat Ranap','0','"+ttljmpetugas+"'","kredit=kredit+'"+(ttljmpetugas)+"'","kd_rek='"+Beban_Jasa_Medik_Petugas_Laborat_Ranap+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Petugas_Laborat_Ranap+"','Utang Jasa Medik Petugas Laborat Ranap','"+ttljmpetugas+"','0'","debet=debet+'"+(ttljmpetugas)+"'","kd_rek='"+Utang_Jasa_Medik_Petugas_Laborat_Ranap+"'");                             
                                }
                                if(ttlbhp>0){
                                    Sequel.menyimpan("tampjurnal","'"+HPP_Persediaan_Laborat_Rawat_inap+"','HPP Persediaan Laborat Rawat Inap','0','"+ttlbhp+"'","kredit=kredit+'"+(ttlbhp)+"'","kd_rek='"+HPP_Persediaan_Laborat_Rawat_inap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Persediaan_BHP_Laborat_Rawat_Inap+"','Persediaan BHP Laborat Rawat Inap','"+ttlbhp+"','0'","debet=debet+'"+(ttlbhp)+"'","kd_rek='"+Persediaan_BHP_Laborat_Rawat_Inap+"'");                                
                                }
                                if(ttlkso>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Kso_Laborat_Ranap+"','HPP Persediaan Laborat Rawat Inap','0','"+ttlkso+"'","kredit=kredit+'"+(ttlkso)+"'","kd_rek='"+Beban_Kso_Laborat_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Kso_Laborat_Ranap+"','Persediaan BHP Laborat Rawat Inap','"+ttlkso+"','0'","debet=debet+'"+(ttlkso)+"'","kd_rek='"+Utang_Kso_Laborat_Ranap+"'");                                
                                }
                                if(ttljasasarana>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Sarana_Laborat_Ranap+"','Beban Jasa Sarana Laborat Ranap','0','"+ttljasasarana+"'","kredit=kredit+'"+(ttljasasarana)+"'","kd_rek='"+Beban_Jasa_Sarana_Laborat_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Sarana_Laborat_Ranap+"','Utang Jasa Sarana Laborat Ranap','"+ttljasasarana+"','0'","debet=debet+'"+(ttljasasarana)+"'","kd_rek='"+Utang_Jasa_Sarana_Laborat_Ranap+"'");                              
                                }
                                if(ttljmperujuk>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Perujuk_Laborat_Ranap+"','Beban Jasa Perujuk Laborat Ranap','0','"+ttljmperujuk+"'","kredit=kredit+'"+(ttljmperujuk)+"'","kd_rek='"+Beban_Jasa_Perujuk_Laborat_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Perujuk_Laborat_Ranap+"','Utang Jasa Perujuk Laborat Ranap','"+ttljmperujuk+"','0'","debet=debet+'"+(ttljmperujuk)+"'","kd_rek='"+Utang_Jasa_Perujuk_Laborat_Ranap+"'");                               
                                }
                                if(ttlmenejemen>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Menejemen_Laborat_Ranap+"','Beban Jasa Menejemen Laborat Ranap','0','"+ttlmenejemen+"'","kredit=kredit+'"+(ttlmenejemen)+"'","kd_rek='"+Beban_Jasa_Menejemen_Laborat_Ranap+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Menejemen_Laborat_Ranap+"','Utang Jasa Menejemen Laborat Ranap','"+ttlmenejemen+"','0'","debet=debet+'"+(ttlmenejemen)+"'","kd_rek='"+Utang_Jasa_Menejemen_Laborat_Ranap+"'");                               
                                }
                                sukses=jur.simpanJurnal(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),"U","PEMBATALAN PEMERIKSAAN LABORAT RAWAT INAP PASIEN "+tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString()+" OLEH "+akses.getkode());  
                            }else if(status.equals("Ralan")){
                                Sequel.queryu("delete from tampjurnal");    
                                if(ttlpendapatan>0){
                                    Sequel.menyimpan("tampjurnal","'"+Suspen_Piutang_Laborat_Ralan+"','Suspen Piutang Laborat Ralan','0','"+ttlpendapatan+"'","kredit=kredit+'"+(ttlpendapatan)+"'","kd_rek='"+Suspen_Piutang_Laborat_Ralan+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Laborat_Ralan+"','Pendapatan Laborat Rawat Jalan','"+ttlpendapatan+"','0'","debet=debet+'"+(ttlpendapatan)+"'","kd_rek='"+Laborat_Ralan+"'");                              
                                }
                                if(ttljmdokter>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Dokter_Laborat_Ralan+"','Beban Jasa Medik Dokter Laborat Ralan','0','"+ttljmdokter+"'","kredit=kredit+'"+(ttljmdokter)+"'","kd_rek='"+Beban_Jasa_Medik_Dokter_Laborat_Ralan+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Dokter_Laborat_Ralan+"','Utang Jasa Medik Dokter Laborat Ralan','"+ttljmdokter+"','0'","debet=debet+'"+(ttljmdokter)+"'","kd_rek='"+Utang_Jasa_Medik_Dokter_Laborat_Ralan+"'");                           
                                }
                                if(ttljmpetugas>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Petugas_Laborat_Ralan+"','Beban Jasa Medik Petugas Laborat Ralan','0','"+ttljmpetugas+"'","kredit=kredit+'"+(ttljmpetugas)+"'","kd_rek='"+Beban_Jasa_Medik_Petugas_Laborat_Ralan+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Petugas_Laborat_Ralan+"','Utang Jasa Medik Petugas Laborat Ralan','"+ttljmpetugas+"','0'","debet=debet+'"+(ttljmpetugas)+"'","kd_rek='"+Utang_Jasa_Medik_Petugas_Laborat_Ralan+"'");                             
                                }
                                if(ttlbhp>0){
                                    Sequel.menyimpan("tampjurnal","'"+HPP_Persediaan_Laborat_Rawat_Jalan+"','HPP Persediaan Laborat Rawat Jalan','0','"+ttlbhp+"'","kredit=kredit+'"+(ttlbhp)+"'","kd_rek='"+HPP_Persediaan_Laborat_Rawat_inap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Persediaan_BHP_Laborat_Rawat_Jalan+"','Persediaan BHP Laborat Rawat Jalan','"+ttlbhp+"','0'","debet=debet+'"+(ttlbhp)+"'","kd_rek='"+Persediaan_BHP_Laborat_Rawat_Inap+"'");                                
                                }
                                if(ttlkso>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Kso_Laborat_Ralan+"','HPP Persediaan Laborat Rawat Inap','0','"+ttlkso+"'","kredit=kredit+'"+(ttlkso)+"'","kd_rek='"+Beban_Kso_Laborat_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Kso_Laborat_Ralan+"','Persediaan BHP Laborat Rawat Inap','"+ttlkso+"','0'","debet=debet+'"+(ttlkso)+"'","kd_rek='"+Utang_Kso_Laborat_Ralan+"'");                                
                                }
                                if(ttljasasarana>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Sarana_Laborat_Ralan+"','Beban Jasa Sarana Laborat Ralan','0','"+ttljasasarana+"'","kredit=kredit+'"+(ttljasasarana)+"'","kd_rek='"+Beban_Jasa_Sarana_Laborat_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Sarana_Laborat_Ralan+"','Utang Jasa Sarana Laborat Ralan','"+ttljasasarana+"','0'","debet=debet+'"+(ttljasasarana)+"'","kd_rek='"+Utang_Jasa_Sarana_Laborat_Ralan+"'");                              
                                }
                                if(ttljmperujuk>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Perujuk_Laborat_Ralan+"','Beban Jasa Perujuk Laborat Ralan','0','"+ttljmperujuk+"'","kredit=kredit+'"+(ttljmperujuk)+"'","kd_rek='"+Beban_Jasa_Perujuk_Laborat_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Perujuk_Laborat_Ralan+"','Utang Jasa Perujuk Laborat Ralan','"+ttljmperujuk+"','0'","debet=debet+'"+(ttljmperujuk)+"'","kd_rek='"+Utang_Jasa_Perujuk_Laborat_Ralan+"'");                               
                                }
                                if(ttlmenejemen>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Menejemen_Laborat_Ralan+"','Beban Jasa Menejemen Laborat Ralan','0','"+ttlmenejemen+"'","kredit=kredit+'"+(ttlmenejemen)+"'","kd_rek='"+Beban_Jasa_Menejemen_Laborat_Ralan+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Menejemen_Laborat_Ralan+"','Utang Jasa Menejemen Laborat Ralan','"+ttlmenejemen+"','0'","debet=debet+'"+(ttlmenejemen)+"'","kd_rek='"+Utang_Jasa_Menejemen_Laborat_Ralan+"'");                               
                                }
                                sukses=jur.simpanJurnal(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),"U","PEMBATALAN PEMERIKSAAN LABORAT RAWAT JALAN PASIEN "+tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString()+" OLEH "+akses.getkode());  
                            }
                        }

                        if(sukses==true){
                            Sequel.Commit();
                            tampil();
                        }else{
                            JOptionPane.showMessageDialog(null,"Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                            Sequel.RollBack();
                        }
                        Sequel.AutoComitTrue();
                    }catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
                    }  
                }              
            }
        }
    }else if(TabRawat.getSelectedIndex()==1){
        JOptionPane.showMessageDialog(null,"Hanya bisa dilakukan hapus di Data Pemeriksaan..!!!");
        TabRawat.setSelectedIndex(0);
        TCari.requestFocus();
    }   
}//GEN-LAST:event_BtnHapusActionPerformed

private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari,BtnAll);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

private void tbDokterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokterMouseClicked
    if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbDokterMouseClicked

private void tbDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokterKeyPressed
   if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbDokterKeyPressed

    private void MnCetakHasilLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLabActionPerformed
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        if(tabMode.getRowCount()==0){
//            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
//            TCari.requestFocus();
//        }else if(Kd2.getText().trim().equals("")){
//            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
//        }else if(!(Kd2.getText().trim().equals(""))){   
//            try {   
//                ps4=koneksi.prepareStatement(
//                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+                    
//                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
//                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
//                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
//                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
//                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
//                try {
//                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
//                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
//                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
//                    rs=ps4.executeQuery();
//                    while(rs.next()){
//                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                        if(!kamar.equals("")){
//                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
//                                    " where kamar.kd_kamar='"+kamar+"' ");            
//                            kamar="Kamar";
//                        }else if(kamar.equals("")){
//                            kamar="Poli";
//                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
//                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
//                        }
//                        Map<String, Object> param = new HashMap<>();
//                        param.put("noperiksa",rs.getString("no_rawat"));
//                        param.put("norm",rs.getString("no_rkm_medis"));
//                        param.put("namapasien",rs.getString("nm_pasien"));
//                        param.put("jkel",rs.getString("jk"));
//                        param.put("umur",rs.getString("umur"));
//                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
//                        param.put("tanggal",rs.getString("tgl_periksa"));                        
//                        param.put("penjab",rs.getString("nm_dokter"));
//                        param.put("petugas",rs.getString("nama"));
//                        param.put("jam",rs.getString("jam"));                        
//                        param.put("alamat",rs.getString("alamat"));
//                        param.put("kamar",kamar);
//                        param.put("namakamar",namakamar);                        
//                        param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?",rs.getString("no_rkm_medis")));
//                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
//                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
//                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
//                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
//                        Sequel.queryu("delete from temporary_lab");
//
//                        ps2=koneksi.prepareStatement(
//                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
//                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
//                            "and periksa_lab.jam=?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
//                            ps2.setString(3,rs.getString("jam"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
////                                menampilkan judul nama pemeriksaan
//                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
//                                ps3=koneksi.prepareStatement(
//                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
//                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
//                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
//                                try {
//                                    ps3.setString(1,rs.getString("no_rawat"));
//                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
//                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
//                                    ps3.setString(4,rs.getString("jam"));
//                                    rs3=ps3.executeQuery();
//                                    while(rs3.next()){
//                                        simpanTemporaryLabAman("'0','         "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
//                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
//                                    }
//                                } catch (Exception e) {
//                                    System.out.println("Notif ps3 : "+e);
//                                } finally{
//                                    if(rs3!=null){
//                                        rs3.close();
//                                    }
//                                    if(ps3!=null){
//                                        ps3.close();
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif ps2 : "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        param.put("namars",akses.getnamars());
//                        param.put("alamatrs",akses.getalamatrs());
//                        param.put("kotars",akses.getkabupatenrs());
//                        param.put("propinsirs",akses.getpropinsirs());
//                        param.put("kontakrs",akses.getkontakrs());
//                        param.put("emailrs",akses.getemailrs());   
//                        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
//                        param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?",rs.getString("no_rkm_medis")));
//                        pspermintaan=koneksi.prepareStatement(
//                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel from permintaan_lab where "+
//                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
//                        try {
//                            pspermintaan.setString(1,rs.getString("no_rawat"));
//                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
//                            pspermintaan.setString(3,rs.getString("jam"));
//                            rspermintaan=pspermintaan.executeQuery();
//                            if(rspermintaan.next()){
//                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
//                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
//                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));                                
//                                param.put("tanggalsampel",rspermintaan.getString("tgl_sampel"));  
//                                param.put("jamsampel",rspermintaan.getString("jam_sampel"));
//                                Valid.MyReport("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
//                            }else{
//                                Valid.MyReport("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif : "+e);
//                        } finally{
//                            if(rspermintaan!=null){
//                                rspermintaan.close();
//                            }
//                            if(pspermintaan!=null){
//                                pspermintaan.close();
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    System.out.println("Notif ps4 : "+e);
//                } finally{
//                    if(rs!=null){
//                        rs.close();
//                    }
//                    if(ps4!=null){
//                        ps4.close();
//                    }
//                }
//            } catch (SQLException ex) {
//                System.out.println(ex);
//            }            
//        }
//        this.setCursor(Cursor.getDefaultCursor());

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            i=0;
                            while(rs2.next()){
                                simpanTemporaryLabAman("'"+i+"','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                i++;
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'"+i+"','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                        i++;
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    
    }//GEN-LAST:event_MnCetakHasilLabActionPerformed

// Tambahkan method ini di class yang sama, bisa di bawah MnCetakHasilLabActionPerformed
private String getStatusHLKritis2(String nilaiStr, String rujukanStr, String pemeriksaan, int umur, String jk) {
    String flag = "";
    try {
        double nilai = Double.parseDouble(nilaiStr);
        String[] batas = rujukanStr.replace(" ", "").split("-|–");
        double batasBawah = batas.length > 0 ? Double.parseDouble(batas[0]) : Double.NaN;
        double batasAtas = batas.length > 1 ? Double.parseDouble(batas[1]) : Double.NaN;

        if (!Double.isNaN(batasBawah) && nilai < batasBawah) {
            flag = "L";
        } else if (!Double.isNaN(batasAtas) && nilai > batasAtas) {
            flag = "H";
        }

        // ==== CEK NILAI KRITIS ====
        String p = pemeriksaan.toLowerCase();
        String kategoriUmur = (umur < 1) ? "Neonatus" : (umur < 18 ? "Anak" : "Dewasa");

        if (p.contains("hgb")) {
            if ((kategoriUmur.equals("Neonatus") && nilai <= 10.0) ||
                (!kategoriUmur.equals("Neonatus") && nilai <= 7.0)) flag += "*";
        } else if (p.contains("wbc") || p.contains("leukosit")) {
            if ((kategoriUmur.equals("Neonatus") && (nilai <= 5000 || nilai >= 50000)) ||
                (!kategoriUmur.equals("Neonatus") && (nilai < 1000 || nilai >= 50000))) flag += "*";
        } else if (p.contains("plt") || p.contains("trombosit")) {
            if ((kategoriUmur.equals("Neonatus") && nilai <= 50000) ||
                (!kategoriUmur.equals("Neonatus") && nilai <= 30000)) flag += "*";
        } else if (p.contains("aptt") && nilai > 150) {
            flag += "*";
        } else if (p.contains("glukosa")) {
            if ((kategoriUmur.equals("Neonatus") && (nilai <= 40 || nilai >= 250)) ||
                (!kategoriUmur.equals("Neonatus") && (nilai < 70 || nilai >= 600))) flag += "*";
        } else if (p.contains("kreatinin")) {
            if ((kategoriUmur.equals("Neonatus") && nilai >= 1.5) ||
                (kategoriUmur.equals("Anak") && nilai >= 2.5) ||
                (kategoriUmur.equals("Dewasa") && nilai >= 10.0)) flag += "*";
        } else if (p.contains("bilirubin") && nilai > 15.0) {
            flag += "*";
        } else if (p.contains("ckmb") && nilai > 75) {
            flag += "*";
        } else if (p.contains("troponin") && nilai > 0.05) {
            flag += "*";
        } else if (p.contains("kalium")) {
            if ((kategoriUmur.equals("Neonatus") && nilai <= 3.5) ||
                (!kategoriUmur.equals("Neonatus") && nilai <= 2.5) ||
                nilai >= 6.0) flag += "*";
        } else if (p.contains("ft4") && nilai >= 7.77) {
            flag += "*";
        }
    } catch (Exception e) {
        // Parsing gagal, biarkan kosong
    }
    return flag;
}
    
    
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        TabRawatMouseClicked(null);
    }//GEN-LAST:event_formWindowOpened

    private void MnCetakNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakNotaActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            try {
                
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    if(rs.next()){
                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            ttl=0;
                            while(rs2.next()){
                                item=rs2.getDouble("biaya");//Sequel.cariIsiAngka("select sum(biaya_item) from template_laboratorium where kd_jenis_prw=?",rs2.getString("kd_jenis_prw"));
                                ttl=ttl+item;                    
                                simpanTemporaryLabAman("'0','"+rs2.getString("kd_jenis_prw")+"','"+rs2.getString("nm_perawatan")+"','"+item+"','Pemeriksaan','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Transaksi Biaya Lab");                        
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        item=rs3.getDouble("biaya_item");
                                        ttl=ttl+item; 
                                        simpanTemporaryLabAman("'0','"+rs3.getString("kd_jenis_prw")+"','   "+rs3.getString("Pemeriksaan")+"','"+item+"','Detail Pemeriksaan','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Transaksi Biaya Lab");                        
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }                                
                            }   
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        simpanTemporaryLabAman("'0','','Total Biaya Pemeriksaan Lab','"+ttl+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Transaksi Biaya Lab");
                        Valid.panggilUrl("billing/LaporanBiayaLab.php?norm="+rs.getString("no_rkm_medis")+"&pasien="+rs.getString("nm_pasien").replaceAll(" ","_")
                                +"&tanggal="+rs.getString("tgl_periksa")+"&jam="+rs.getString("jam")+"&pjlab="+rs.getString("nm_dokter").replaceAll(" ","_")
                                +"&petugas="+rs.getString("nama").replaceAll(" ","_")+"&kasir="+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",akses.getkode())
                                +"&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB());
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }  
                
            } catch (Exception ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakNotaActionPerformed

    private void MnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnUbahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mengubah. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            if(Sequel.cariRegistrasi(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString())>0){
                JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi, data tidak boleh dihapus.\nSilahkan hubungi bagian kasir/keuangan ..!!");
                TCari.requestFocus();
            }else{
                DlgUbahPeriksaLab ubah=new DlgUbahPeriksaLab(null,false);
                ubah.isCek();
                ubah.setSize(this.getWidth()-20,this.getHeight()-20);
                ubah.setNoRm(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),
                        tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(), 
                        tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                ubah.setLocationRelativeTo(this);
                ubah.setVisible(true);
            }                
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnUbahActionPerformed

    private void MnCetakHasilLab1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab2Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);  
                            }else{
                                Valid.MyReport("rptPeriksaLab2.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);  
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }   
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab1ActionPerformed

    private void MnCetakHasilLab2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab2ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {     
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);     
                        param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa")); 
                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));                         
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab3Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab3.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab2ActionPerformed

    private void MnCetakHasilLab3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab3ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {      
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab4Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab4.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab3ActionPerformed

    private void MnCetakHasilLab4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab4ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {        
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab5Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab5.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab4ActionPerformed

    private void MnCetakHasilLab5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab5ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));        
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab6Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab6.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }                    
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab5ActionPerformed

    private void MnCetakHasilLab6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab6ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                            diagnosa=Sequel.cariIsi("select nm_penyakit from penyakit inner join diagnosa_pasien on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit where diagnosa_pasien.status='Ranap' and diagnosa_pasien.prioritas='1' and diagnosa_pasien.no_rawat=?",rs.getString("no_rawat"));
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                            diagnosa=Sequel.cariIsi("select nm_penyakit from penyakit inner join diagnosa_pasien on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit where diagnosa_pasien.status='Ralan' and diagnosa_pasien.prioritas='1' and diagnosa_pasien.no_rawat=?",rs.getString("no_rawat"));
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("diagnosa",diagnosa);
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab7Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab7.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab6ActionPerformed

    private void MnCetakHasilLab7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab7ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {    
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pekerjaan",Sequel.cariIsi("select pasien.pekerjaan from pasien where pasien.no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        param.put("noktp",Sequel.cariIsi("select pasien.no_ktp from pasien where pasien.no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab8Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab8.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }        

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab7ActionPerformed

    private void MnCetakHasilLab8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab8ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {        
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);    
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");  
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }                                
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab9Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab9.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }          
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab8ActionPerformed

    private void MnCetakHasilLab9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab9ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }                                
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab10Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab10.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab9ActionPerformed

    private void MnCetakHasilLab10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab10ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {      
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReport("rptPeriksaLab11Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab11.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab10ActionPerformed

    private void MnSaranKesanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSaranKesanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!"); 
            }else{
                try {
                    Kesan.setText("");
                    Saran.setText("");
                    ps5=koneksi.prepareStatement(
                        "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                    try {
                        ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                        ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                        ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                        rs=ps5.executeQuery();
                        while(rs.next()){      
                            Kesan.setText(rs.getString("kesan"));
                            Saran.setText(rs.getString("saran"));
                        } 
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rs!=null){
                            rs.close();
                        }
                        if(ps5!=null){
                            ps5.close();
                        }
                    }
                    WindowSaran.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    WindowSaran.setLocationRelativeTo(internalFrame1);
                    WindowSaran.setVisible(true);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }         
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnSaranKesanActionPerformed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(Kd2.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }else{
            if(Saran.getText().equals("")&&Kesan.getText().equals("")){
                Sequel.queryu2("delete from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                });
            }else{
                if(Sequel.menyimpantf2("saran_kesan_lab","?,?,?,?,?","Kesan & Saran", 5,new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString(),Saran.getText(),Kesan.getText()
                })==false){
                    Sequel.queryu2("update saran_kesan_lab set saran=?,kesan=? where no_rawat=? and tgl_periksa=? and jam=?",5,new String[]{
                        Saran.getText(),Kesan.getText(),tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),
                        tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                    });
                }
            }

            JOptionPane.showMessageDialog(null,"Proses update selesai...!!!!");
        }
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnCloseIn5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn5ActionPerformed
        WindowSaran.dispose();
    }//GEN-LAST:event_BtnCloseIn5ActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
    
    sedangGantiTab = true; // tandai: lagi pindah tab

    if(TabRawat.getSelectedIndex()==0){
        tampil();
    }else if(TabRawat.getSelectedIndex()==1){
        tampil2();
    }else if(TabRawat.getSelectedIndex()==2){
        tampil3();
    }else if(TabRawat.getSelectedIndex()==3){
        tampil4();   
    }else if(TabRawat.getSelectedIndex()==4){
        tampil5();      
    }

    // kosongkan setelah tampil (tanpa takut ditimpa getData lagi)
    NoRawat.setText("");
    Kd2.setText("");
    Kd3.setText("");
    Kd4.setText("");

    sedangGantiTab = false; // reset flag
    
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbDokter2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokter2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDokter2MouseClicked

    private void tbDokter2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokter2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDokter2KeyPressed

    private void MnCetakHasilLab11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab11ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));                         
                        param.put("lahir",Sequel.cariIsi("select pasien.tgl_lahir from pasien where no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));                                
                                param.put("tanggalsampel",rspermintaan.getString("tgl_sampel"));  
                                param.put("jamsampel",rspermintaan.getString("jam_sampel"));                                
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab11ActionPerformed

    private void MnCetakHasilLab12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab12ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab2Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab2.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab12ActionPerformed

    private void MnCetakHasilLab13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab13ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {     
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));                         
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));                                
                                Valid.MyReportPDF("rptPeriksaLab3Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab3.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab13ActionPerformed

    private void MnCetakHasilLab14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab14ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {      
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab4Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab4.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }           
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab14ActionPerformed

    private void MnCetakHasilLab15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab15ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {        
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab5Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab5.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }          
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab15ActionPerformed

    private void MnCetakHasilLab16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab16ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab6Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab6.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }            
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }                    
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab16ActionPerformed

    private void MnCetakHasilLab17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab17ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                            diagnosa=Sequel.cariIsi("select nm_penyakit from penyakit inner join diagnosa_pasien on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit where diagnosa_pasien.status='Ranap' and diagnosa_pasien.prioritas='1' and diagnosa_pasien.no_rawat=?",rs.getString("no_rawat"));
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                            diagnosa=Sequel.cariIsi("select nm_penyakit from penyakit inner join diagnosa_pasien on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit where diagnosa_pasien.status='Ralan' and diagnosa_pasien.prioritas='1' and diagnosa_pasien.no_rawat=?",rs.getString("no_rawat"));
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("diagnosa",diagnosa);
                        param.put("umur",rs.getString("umur"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab7Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab7.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab17ActionPerformed

    private void MnCetakHasilLab18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab18ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {    
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pekerjaan",Sequel.cariIsi("select pasien.pekerjaan from pasien where pasien.no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        param.put("noktp",Sequel.cariIsi("select pasien.no_ktp from pasien where pasien.no_rkm_medis=?",rs.getString("no_rkm_medis")));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab8Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab8.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }           

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab18ActionPerformed

    private void MnCetakHasilLab19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab19ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {        
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);      
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");  
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }                                
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab9Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab9.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab19ActionPerformed

    private void MnCetakHasilLab20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab20ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {         
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }                                
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab10Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab10.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab20ActionPerformed

    private void MnCetakHasilLab21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab21ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencteak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {      
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        
                        
                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        Sequel.queryu("delete from temporary_lab");
                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                Valid.MyReportPDF("rptPeriksaLab11Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab11.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }         

                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab21ActionPerformed

    private void MnUbah1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnUbah1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mengubah. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            DlgUbahNilaiLab ubah=new DlgUbahNilaiLab(null,false);
            ubah.isCek();
            ubah.setNoRm(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(), 
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
            ubah.setSize(this.getWidth()-20,this.getHeight()-20);
            ubah.setLocationRelativeTo(this);
            ubah.setVisible(true);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnUbah1ActionPerformed

    private void MnCetakSuratCovidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratCovidActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mengubah. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            if(tbDokter.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SuratKeteranganCovid resume=new SuratKeteranganCovid(null,false);
                resume.isCek();
                resume.emptTeks();
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setNoRm(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),Tgl1.getDate(),Tgl2.getDate());
                resume.tampil();
                resume.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakSuratCovidActionPerformed

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBerkasDigitalBtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mengubah. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){  
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DlgBerkasRawat berkas=new DlgBerkasRawat(null,true);
            berkas.setJudul("::[ Berkas Digital Perawatan ]::","berkasrawat/pages");
            try {
                if(akses.gethapus_berkas_digital_perawatan()==true){
                    berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                }else{
                    berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2nonhapus.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                }   
            } catch (Exception ex) {
                System.out.println("Notifikasi : "+ex);
            }

            berkas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            berkas.setLocationRelativeTo(internalFrame1);
            berkas.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppBerkasDigitalBtnPrintActionPerformed

    private void MnCetakHasilLab22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab22ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){   
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,pasien.tgl_lahir,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,diagnosa_klinis from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                param.put("klinis",rspermintaan.getString("diagnosa_klinis"));
                                Valid.MyReport("rptPeriksaLab12Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReport("rptPeriksaLab12.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab22ActionPerformed

    private void MnCetakHasilLab23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilLab23ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){   
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,pasien.tgl_lahir,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("lahir",rs.getString("lahir"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        kesan="";
                        saran="";
                        ps5=koneksi.prepareStatement(
                            "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs2=ps5.executeQuery();
                            while(rs2.next()){      
                                kesan=rs2.getString("kesan");
                                saran=rs2.getString("saran");
                            } 
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }                    
                        param.put("kesan",kesan);
                        param.put("saran",saran);
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,diagnosa_klinis from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                param.put("klinis",rspermintaan.getString("diagnosa_klinis"));
                                Valid.MyReportPDF("rptPeriksaLab12Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }else{
                                Valid.MyReportPDF("rptPeriksaLab12.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakHasilLab23ActionPerformed

    private void ppRiwayatBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppRiwayatBtnPrintActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")){
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!");
            }else{
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMRiwayatPerawatan resume=new RMRiwayatPerawatan(null,true);
                datapasien=Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?",Kd2.getText());
                resume.setNoRm(datapasien,tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString().replaceAll(datapasien+" ",""));
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
    }//GEN-LAST:event_ppRiwayatBtnPrintActionPerformed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());
        String noRawatUpload = ambilNilaiTableBarisKunci(tbDokter, 0, 0);
        String tglUpload = ambilNilaiTableBarisKunci(tbDokter, 0, 3);
        String jamUpload = ambilNilaiTableBarisKunci(tbDokter, 0, 4);

        String headerUpload = ambilHeaderPemeriksaanUploadLabWA(noRawatUpload, tglUpload, jamUpload);

        FileName = buatKunciUploadLabWA(noRawatUpload, tglUpload, jamUpload) + "_" + headerUpload + "_HasilLab";

        CreatePDF(FileName);
        ConvertPDFtoJPG(FileName);
        UploadJPG(FileName, "berkasrawat/pages/upload/");
        HapusJPG();

        ppBerkasDigitalBtnPrintActionPerformed(evt);
    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    private void tbDokter1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokter1MouseClicked
       if(tabMode3.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbDokter1MouseClicked

    private void tbDokter1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokter1KeyPressed
       if(tabMode3.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbDokter1KeyPressed

    private void BtnSimpanPelaporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporActionPerformed
    if(Pelapor.getText().trim().equals("")){
        Valid.textKosong(Pelapor,"Nama Pelapor");
    }else if(Penerima.getText().trim().equals("")){
        Valid.textKosong(Penerima,"Nama Penerima");
    }else if(HasilCito.getText().trim().equals("")){
        Valid.textKosong(HasilCito,"Hasil Cito");    
    } 
        
    if (Kd3.getText().equals("")) {
        JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
    } else {
        String noRawat = tbDokter1.getValueAt(tbDokter1.getSelectedRow(), 0).toString();
        String tglPeriksa = tbDokter1.getValueAt(tbDokter1.getSelectedRow(), 3).toString();
        String jam = tbDokter1.getValueAt(tbDokter1.getSelectedRow(), 4).toString();
        String jamregis = Valid.SetTanggalJam(WaktuRegis.getSelectedItem().toString());
        String pelapor = Pelapor.getText().trim();
        String penerima = Penerima.getText().trim();
        String jenis = "cito";
        String jamlaporcito = Valid.SetTanggalJam(WaktuLapor.getSelectedItem().toString());        
        String hasilcito = HasilCito.getText().trim();

//        // Validasi: cek apakah data sudah ada
//        if (Sequel.cariInteger(
//                "SELECT COUNT(*) FROM rsaj_monev_cito WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='cito'",
//                noRawat, tglPeriksa, jam) > 0) {
//            JOptionPane.showMessageDialog(null, "Maaf, data sudah dimasukkan sebelumnya...!!!!");
//            return;
//        }
        
        if (pelapor.equals("") && penerima.equals("")) {
            Sequel.queryu2(
                "DELETE FROM rsaj_monev_cito WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='cito'",
                3,
                new String[]{noRawat, tglPeriksa, jam}
            );
        } else {
            if (Sequel.menyimpantf2(
                "rsaj_monev_cito",
                "?,?,?,?,?,?,?,?,?",
                "MONEV CITO",
                9,
                new String[]{noRawat, tglPeriksa, jam, pelapor, penerima, jenis, jamlaporcito, hasilcito, jamregis }
            ) == false) {
                Sequel.queryu2(
                    "UPDATE rsaj_monev_cito SET pelapor=?, penerima=?, jam_lapor=? WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='cito'",
                    6,
                    new String[]{pelapor, penerima, jamlaporcito, noRawat, tglPeriksa, jam}
                );
            }
        }

        JOptionPane.showMessageDialog(null, "Proses update selesai...!!!!");
        tampil4();
        WindowPelapor.dispose();               
    }
    }//GEN-LAST:event_BtnSimpanPelaporActionPerformed

    private void BtnSimpanPelaporKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSimpanPelaporKeyPressed

    private void BtnCloseIn6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn6ActionPerformed
        WindowPelapor.dispose();
    }//GEN-LAST:event_BtnCloseIn6ActionPerformed

    private void Kd2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kd2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kd2ActionPerformed

    private void MnPelaporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnPelaporActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode3.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter1.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd3.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!"); 
            }else{
                try {
                    Pelapor.setText("");
                    Penerima.setText("");
                    ps5=koneksi.prepareStatement(
                        "select pelapor,penerima from rsaj_monev_cito where no_rawat=? and tgl_periksa=? and jam=?");  
                    try {
                        ps5M.setString(1,tbDokter1.getValueAt(tbDokter1.getSelectedRow(),0).toString());
                        ps5M.setString(2,tbDokter1.getValueAt(tbDokter1.getSelectedRow(),3).toString());
                        ps5M.setString(3,tbDokter1.getValueAt(tbDokter1.getSelectedRow(),4).toString());
                        rsM=ps5M.executeQuery();
                        while(rsM.next()){      
                            Pelapor.setText(rsM.getString("pelapor"));
                            Penerima.setText(rsM.getString("penerima"));
                        } 
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rsM!=null){
                            rsM.close();
                        }
                        if(ps5M!=null){
                            ps5M.close();
                        }
                    }
                    WindowPelapor.setSize(500, 300);
                    WindowPelapor.setLocationRelativeTo(internalFrame1);
                    WindowPelapor.setVisible(true);
                    Pelapor.requestFocus();                    
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }         
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnPelaporActionPerformed

    private void Kd3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kd3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kd3ActionPerformed

    private void BtnSimpanPelaporKritisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporKritisActionPerformed
    if(PelaporKritis.getText().trim().equals("")){
        Valid.textKosong(PelaporKritis,"Nama Pelapor");
    }else if(PenerimaKritis.getText().trim().equals("")){
        Valid.textKosong(PenerimaKritis,"Nama Penerima");
    }else if(HasilKritis.getText().trim().equals("")){
        Valid.textKosong(HasilKritis,"Hasil Kritis");    
    } 
        
    if (Kd4.getText().equals("")) {
        JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
    } else {
        String noRawatkritis = tbDokter3.getValueAt(tbDokter3.getSelectedRow(), 0).toString();
        String tglPeriksakritis = tbDokter3.getValueAt(tbDokter3.getSelectedRow(), 3).toString();
        String jam = tbDokter3.getValueAt(tbDokter3.getSelectedRow(), 4).toString();
        String jamvalidasi = Valid.SetTanggalJam(WaktuValidasi.getSelectedItem().toString());
        String pelaporkritis = PelaporKritis.getText().trim();
        String penerimakritis = PenerimaKritis.getText().trim();
        String jeniskritis = "kritis";
        String waktukritis = Valid.SetTanggalJam(WaktuLaporKritis.getSelectedItem().toString());
        String hasilkritis = HasilKritis.getText().trim();

//        // Validasi: cek apakah data sudah ada
//        if (Sequel.cariInteger(
//                "SELECT COUNT(*) FROM rsaj_monev_kritis WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='kritis'",
//                noRawatkritis, tglPeriksakritis, jam) > 0) {
//            JOptionPane.showMessageDialog(null, "Maaf, data sudah dimasukkan sebelumnya...!!!!");
//            return;
//        }
        
        if (pelaporkritis.equals("") && penerimakritis.equals("")) {
            Sequel.queryu2(
                "DELETE FROM rsaj_monev_kritis WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='kritis'",
                3,
                new String[]{noRawatkritis, tglPeriksakritis, jam}
            );
        } else {
            if (Sequel.menyimpantf2(
                "rsaj_monev_kritis",
                "?,?,?,?,?,?,?,?,?",
                "MONEV KRITIS",
                9,
                new String[]{noRawatkritis, tglPeriksakritis, jam, pelaporkritis, penerimakritis, jeniskritis, waktukritis, hasilkritis, jamvalidasi }
            ) == false) {
                Sequel.queryu2(
                    "UPDATE rsaj_monev_kritis SET pelapor=?, penerima=?, jam_lapor=?, hasilkritis=? WHERE no_rawat=? AND tgl_periksa=? AND jam=? AND jenis='kritis'",
                    7,
                    new String[]{pelaporkritis, penerimakritis, waktukritis, hasilkritis, noRawatkritis, tglPeriksakritis, jam}
                );
            }
        }

        JOptionPane.showMessageDialog(null, "Proses update selesai...!!!!");
        tampil5();
        WindowPelaporKritis.dispose();        
    }
    }//GEN-LAST:event_BtnSimpanPelaporKritisActionPerformed

    private void BtnSimpanPelaporKritisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporKritisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSimpanPelaporKritisKeyPressed

    private void BtnCloseIn7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn7ActionPerformed
    WindowPelaporKritis.dispose();
    }//GEN-LAST:event_BtnCloseIn7ActionPerformed

    private void tbDokter3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokter3MouseClicked
       if(tabMode4.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbDokter3MouseClicked

    private void tbDokter3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokter3KeyPressed
       if(tabMode4.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbDokter3KeyPressed

    private void MnPelaporKritisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnPelaporKritisActionPerformed
            
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode4.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter3.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd4.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!"); 
            }else{
                try {
                    PelaporKritis.setText("");
                    PenerimaKritis.setText("");
                    HasilKritis.setText("");
                    psK=koneksi.prepareStatement(
                        "select pelapor,penerima,hasilkritis from rsaj_monev_kritis where no_rawat=? and tgl_periksa=? and jam=?");  
                    try {
                        ps5K.setString(1,tbDokter3.getValueAt(tbDokter3.getSelectedRow(),0).toString());
                        ps5K.setString(2,tbDokter3.getValueAt(tbDokter3.getSelectedRow(),3).toString());
                        ps5K.setString(3,tbDokter3.getValueAt(tbDokter3.getSelectedRow(),4).toString());
                        rsK=ps5K.executeQuery();
                        while(rsK.next()){      
                            PelaporKritis.setText(rsK.getString("pelapor"));
                            PenerimaKritis.setText(rsK.getString("penerima"));
                            HasilKritis.setText(rsK.getString("hasilkritis"));
                        } 
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rsK!=null){
                            rsK.close();
                        }
                        if(ps5K!=null){
                            ps5K.close();
                        }
                    }
                    WindowPelaporKritis.setSize(500, 300);
                    WindowPelaporKritis.setLocationRelativeTo(internalFrame1);
                    WindowPelaporKritis.setVisible(true);
                    PelaporKritis.requestFocus();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }         
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnPelaporKritisActionPerformed

    private void Kd4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kd4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Kd4ActionPerformed

    private void BtnPrintCitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintCitoActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    try {
        Map<String, Object> param = new HashMap<>();
        param.put("tanggal1", Valid.SetTgl(Tgl1.getSelectedItem() + ""));
        param.put("tanggal2", Valid.SetTgl(Tgl2.getSelectedItem() + ""));
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select logo from setting"));

        Valid.MyReport("rptRekapHasilCito.jasper", "report", "::[ Rekap Hasil Laboratorium CITO ]::", param);


    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal mencetak: " + e.getMessage());
    } finally {
        this.setCursor(Cursor.getDefaultCursor());
    }
    }//GEN-LAST:event_BtnPrintCitoActionPerformed

    private void BtnPrintCitoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintCitoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPrintCitoKeyPressed

    private void BtnPrintKritisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintKritisActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    try {
        Map<String, Object> param = new HashMap<>();
        param.put("tanggal1", Valid.SetTgl(Tgl1.getSelectedItem() + ""));
        param.put("tanggal2", Valid.SetTgl(Tgl2.getSelectedItem() + ""));
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select logo from setting"));

        Valid.MyReport("rptRekapHasilKritis.jasper", "report", "::[ Rekap Hasil Laboratorium Kritis ]::", param);


    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal mencetak: " + e.getMessage());
    } finally {
        this.setCursor(Cursor.getDefaultCursor());
    }
    }//GEN-LAST:event_BtnPrintKritisActionPerformed

    private void BtnPrintKritisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKritisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPrintKritisKeyPressed

    private void PelaporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PelaporActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PelaporActionPerformed

    private void BtnSimpanPelaporKritis1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporKritis1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSimpanPelaporKritis1ActionPerformed

    private void BtnSimpanPelaporKritis1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanPelaporKritis1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSimpanPelaporKritis1KeyPressed

    private void BtnCloseIn8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCloseIn8ActionPerformed

    private void MneLFGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MneLFGActionPerformed
    
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if(tabMode.getRowCount()==0){
        JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    } else if(Kd2.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
    } else {
        try {   
            ps4 = koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama," +
                "DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip," +
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter," +
                "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat," +
                "dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir,pasien.tgl_lahir " +
                "from periksa_lab " +
                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join petugas on periksa_lab.nip=petugas.nip " +
                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
                "inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel " +
                "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec " +
                "inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab " +
                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? " +
                "group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");

            ps4.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString());
            ps4.setString(2, tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
            ps4.setString(3, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
            rs = ps4.executeQuery();

            while(rs.next()){
                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                if(!kamar.equals("")){
                    namakamar = kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
                    kamar = "Kamar";
                } else {
                    kamar = "Poli";
                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                }

                // GFR check
                boolean adaKreatinin = false;
                boolean bolehCetakGFR = false;
                double kreatinin = -1, gfr = 0;
                int umurAngka = Sequel.cariInteger("select TIMESTAMPDIFF(YEAR, ?, ?)", rs.getString("tgl_lahir"), Valid.SetTgl(rs.getString("tgl_periksa")));
                String jenisKelamin = rs.getString("jk");

                PreparedStatement psKreatinin = koneksi.prepareStatement(
                    "SELECT nilai FROM detail_periksa_lab " +
                    "WHERE no_rawat=? AND kd_jenis_prw='J000027' " +
                    "AND tgl_periksa=? AND jam=? " +
                    "AND id_template='3309' LIMIT 1");
                
                psKreatinin.setString(1, rs.getString("no_rawat"));
                psKreatinin.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                psKreatinin.setString(3, rs.getString("jam"));
                ResultSet rsKreatinin = psKreatinin.executeQuery();
                if(rsKreatinin.next()){
                    try {
                        kreatinin = Double.parseDouble(rsKreatinin.getString("nilai").replace(",", "."));
                        adaKreatinin = true;
                    } catch(Exception e){
                        System.out.println("Gagal parsing kreatinin: " + e);
                    }
                }
                rsKreatinin.close();
                psKreatinin.close();

                if(adaKreatinin && umurAngka > 18){
                    bolehCetakGFR = true;
                    double kappa = jenisKelamin.equalsIgnoreCase("L") ? 0.9 : 0.7;
                    double alpha = jenisKelamin.equalsIgnoreCase("L") ? -0.302 : -0.241;
                    double faktorJK = jenisKelamin.equalsIgnoreCase("L") ? 1.0 : 1.012;
                    double scrKappa = kreatinin / kappa;
                    double min = Math.min(scrKappa, 1);
                    double max = Math.max(scrKappa, 1);
                    gfr = 142 * Math.pow(min, alpha) * Math.pow(max, -1.200) * Math.pow(0.9938, umurAngka) * faktorJK;
                }

                Map<String, Object> param = new HashMap<>();
                param.put("noperiksa", rs.getString("no_rawat"));
                param.put("norm", rs.getString("no_rkm_medis"));
                param.put("namapasien", rs.getString("nm_pasien"));
                param.put("jkel", jenisKelamin);
                param.put("umur", rs.getString("umur"));
                param.put("pengirim", Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rs.getString("dokter_perujuk")));
                param.put("tanggal", rs.getString("tgl_periksa"));
                param.put("penjab", rs.getString("nm_dokter"));
                param.put("petugas", rs.getString("nama"));
                param.put("jam", rs.getString("jam"));
                param.put("alamat", rs.getString("alamat"));
                param.put("kamar", kamar);
                param.put("namakamar", namakamar);
                param.put("lahir", Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?", rs.getString("no_rkm_medis")));

                if(bolehCetakGFR){
                    param.put("kreatinin", Valid.SetAngka(kreatinin));
                    param.put("gfr", Valid.SetAngka(gfr));
                    param.put("umurAngka", umurAngka);
                    param.put("jenisKelamin", jenisKelamin);
                }

                finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("kd_dokter"));
                param.put("finger", "Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("nip"));
                param.put("finger2", "Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  

                Sequel.queryu("delete from temporary_lab");

                ps2 = koneksi.prepareStatement(
                    "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                    "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=?");
                ps2.setString(1, rs.getString("no_rawat"));
                ps2.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                ps2.setString(3, rs.getString("jam"));
                rs2 = ps2.executeQuery();
                while(rs2.next()){
                    simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                    ps3 = koneksi.prepareStatement(
                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                        "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.id_template from detail_periksa_lab inner join template_laboratorium "+
                        "on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                    ps3.setString(1, rs.getString("no_rawat"));
                    ps3.setString(2, rs2.getString("kd_jenis_prw"));
                    ps3.setString(3, Valid.SetTgl(rs.getString("tgl_periksa")));
                    ps3.setString(4, rs.getString("jam"));
                    rs3 = ps3.executeQuery();
                    
                    String kategoriUmur = getKategoriUmur(rs.getString("no_rawat"));
                    
                    while(rs3.next()){

                    item += rs3.getDouble("biaya_item");
                                    ttl += rs3.getDouble("biaya_item");

                                    String pemeriksaan = rs3.getString("Pemeriksaan").trim();
                                    String nilaiStr = normalisasiNilaiLabCetak(rs3.getString("nilai"));
                                    String satuan = rs3.getString("satuan");
                                    String rujukan = rs3.getString("nilai_rujukan") == null ? "" : rs3.getString("nilai_rujukan").trim();
                                    String keterangan = rs3.getString("keterangan") == null ? "" : rs3.getString("keterangan").trim();
                                    String flag = getStatusHLKritisLab(nilaiStr, rujukan, rs3.getString("kd_jenis_prw"), rs3.getString("id_template"), kategoriUmur);

                                    simpanTemporaryLabAman(
                                    "'0','         " + pemeriksaan + "','" + nilaiStr + "','" + satuan +
                                    "','" + rujukan + "','" + keterangan + "','" + flag +
                                    "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User");              
                    }
                    
                    rs3.close();
                    ps3.close();
                }
                rs2.close();
                ps2.close();

                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());   
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                boolean adaPermintaan = false;
                pspermintaan = koneksi.prepareStatement(
                    "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel from permintaan_lab where "+
                    "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                pspermintaan.setString(1, rs.getString("no_rawat"));
                pspermintaan.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                pspermintaan.setString(3, rs.getString("jam"));
                rspermintaan = pspermintaan.executeQuery();
                if(rspermintaan.next()){
                    adaPermintaan = true;
                    param.put("nopermintaan", rspermintaan.getString("noorder"));   
                    param.put("tanggalpermintaan", rspermintaan.getString("tgl_permintaan"));  
                    param.put("jampermintaan", rspermintaan.getString("jam_permintaan"));                                
                    param.put("tanggalsampel", rspermintaan.getString("tgl_sampel"));  
                    param.put("jamsampel", rspermintaan.getString("jam_sampel"));
                }
                rspermintaan.close();
                pspermintaan.close();

                // PILIH TEMPLATE CETAK YANG TEPAT
                if(bolehCetakGFR){
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReport("rptPeriksaLabGFR.jasper","report","::[ Pemeriksaan Laboratorium GFR ]::",param);   
                } else if(adaPermintaan){
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReport("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                } else {
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReport("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);   
                }
            }

            rs.close();
            ps4.close();
        } catch (Exception e) {
            System.out.println("Notif Cetak Lab: " + e);
        }
    }
    this.setCursor(Cursor.getDefaultCursor());
    
    }//GEN-LAST:event_MneLFGActionPerformed

    private void MnHapusDataCitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnHapusDataCitoActionPerformed
    
    String noRawat = NoRawat.getText();
        
    int konfirmasi = JOptionPane.showConfirmDialog(
        null,
        "Yakin ingin menghapus data pelaporan cito untuk No. Rawat: " + noRawat + "?",
        "Konfirmasi Hapus",
        JOptionPane.YES_NO_OPTION
    );

    if (konfirmasi == JOptionPane.YES_OPTION) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "DELETE FROM rsaj_monev_cito WHERE no_rawat=?"
            );
            ps.setString(1, noRawat);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Data berhasil dihapus.");
                WindowPelapor.dispose();
                tampil4(); // refresh tabel utama
            } else {
                JOptionPane.showMessageDialog(null, "Data gagal dihapus.");
            }

            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menghapus: " + e);
        }
    }
    }//GEN-LAST:event_MnHapusDataCitoActionPerformed

    private void MnHapusDataKritisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnHapusDataKritisActionPerformed
    
    String noRawat = NoRawat.getText();
        
    int konfirmasi = JOptionPane.showConfirmDialog(
        null,
        "Yakin ingin menghapus data pelaporan kritis untuk No. Rawat: " + noRawat + "?",
        "Konfirmasi Hapus",
        JOptionPane.YES_NO_OPTION
    );

    if (konfirmasi == JOptionPane.YES_OPTION) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "DELETE FROM rsaj_monev_kritis WHERE no_rawat=?"
            );
            ps.setString(1, noRawat);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Data berhasil dihapus.");
                WindowPelaporKritis.dispose();
                tampil5(); // refresh tabel utama
            } else {
                JOptionPane.showMessageDialog(null, "Data gagal dihapus.");
            }

            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menghapus: " + e);
        }
    }
    }//GEN-LAST:event_MnHapusDataKritisActionPerformed

    private void BtnPrintKritis1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintKritis1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgPembayaranLaboratoriumRalan RPR=new DlgPembayaranLaboratoriumRalan(null,false);
        //                RL.isCek();
        RPR.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        RPR.setLocationRelativeTo(internalFrame1);
        //                RL.setNoRm(TNoRw.getText(),DTPCari2.getDate());

        RPR.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintKritis1ActionPerformed

    private void BtnPrintKritis1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKritis1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPrintKritis1KeyPressed

    private void BtnPrintKritis2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintKritis2ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgPembayaranLaboratoriumRanap PLR=new DlgPembayaranLaboratoriumRanap(null,false);
        //                RL.isCek();
        PLR.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        PLR.setLocationRelativeTo(internalFrame1);
        //                RL.setNoRm(TNoRw.getText(),DTPCari2.getDate());

        PLR.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintKritis2ActionPerformed

    private void BtnPrintKritis2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKritis2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPrintKritis2KeyPressed

    private void MnSumsumtulangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSumsumtulangActionPerformed
            
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
            return;
        }

        if (tbDokter.getSelectedRow() <= -1) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan pilih data..!!");
            return;
        }

        if (Kd2.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
            return;
        }

        // --------- reset semua field di WindowSumTulang ---------
        java.util.Date now = new java.util.Date();
        TglPengambilan.setDate(now);   // default hari ini

        TRatio.setText("");
        TSelularitas.setText("");
        TEritropoietik.setText("");
        TLeukopoietik.setText("");
        TTrombopoietik.setText("");
        TSelPlasma.setText("");
        TMitosis.setText("");
        TKesan.setText("");
        TSaran.setText("");
        idSumTulang = "";       

        // simpan kunci utk proses simpan / update
        String noRawat    = tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString();
        String tglPeriksa = tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString();
        String jamPeriksa = tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString();

        noRawatSumTulang    = noRawat;
        tglPeriksaSumTulang = tglPeriksa;
        jamperiksaSumTulang = jamPeriksa;

        // --------- load dari tabel rsaj_lab_sum_tulang (kalau ada) ---------
        String sql =
            "SELECT id, tgl_pengambilan, ratio_mieloid_eritroid, " +
            "       selularitas, eritropoietik, leukopoietik, trombopoietik, " +
            "       sel_plasma, mitosis, kesan, saran " +
            "FROM rsaj_lab_sum_tulang " +
            "WHERE no_rawat=? AND tgl_periksa=? AND jam_periksa=?";

        try {
            ps5 = koneksi.prepareStatement(sql);
            ps5.setString(1, noRawat);
            ps5.setString(2, tglPeriksa);
            ps5.setString(3, jamPeriksa);
            rs = ps5.executeQuery();

            if (rs.next()) {
                // SUDAH ADA DATA -> isi form dari DB
                idSumTulang = rs.getString("id");  // simpan id utk update nanti

                java.sql.Date tglAmbil = rs.getDate("tgl_pengambilan");
                if (tglAmbil != null) {
                    TglPengambilan.setDate(new java.util.Date(tglAmbil.getTime()));
                }

                TRatio.setText(rs.getString("ratio_mieloid_eritroid"));
                TSelularitas.setText(rs.getString("selularitas"));
                TEritropoietik.setText(rs.getString("eritropoietik"));
                TLeukopoietik.setText(rs.getString("leukopoietik"));
                TTrombopoietik.setText(rs.getString("trombopoietik"));
                TSelPlasma.setText(rs.getString("sel_plasma"));
                TMitosis.setText(rs.getString("mitosis"));
                TKesan.setText(rs.getString("kesan"));
                TSaran.setText(rs.getString("saran"));
            }
            // kalau rs.next() == false -> idSumTulang tetap "", form tetap default (belum ada data)
        } catch (Exception e) {
            System.out.println("Notif load sumsum tulang : " + e);
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (Exception ex) {}
            }
            if (ps5 != null) {
                try { ps5.close(); } catch (Exception ex) {}
            }
        }

        // --------- tampilkan dialog ---------
        WindowSumTulang.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        WindowSumTulang.setLocationRelativeTo(internalFrame1);
        WindowSumTulang.setVisible(true);

    } catch (Exception ex) {
        System.out.println("Error MnSumsumtulangActionPerformed : " + ex);
    } finally {
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    }//GEN-LAST:event_MnSumsumtulangActionPerformed

    private void BtnSimpanSumTulangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanSumTulangActionPerformed
        
    String NoRawat    = tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString();
        
        if (NoRawat.equals("")) {
        JOptionPane.showMessageDialog(null, "No. Rawat belum dipilih..!");
        return;
    }

    // contoh pakai tgl/jam dari form lab kamu
    String noRawat    = tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString();
    String tglPeriksa = tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString();
    String jamPeriksa = tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString();

    simpanSumTulang(noRawat, tglPeriksa, jamPeriksa);     
    
    }//GEN-LAST:event_BtnSimpanSumTulangActionPerformed

    private void BtnSimpanSumTulangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanSumTulangKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSimpanSumTulangKeyPressed

    private void BtnCloseIn9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn9ActionPerformed
    this.WindowSumTulang.dispose();
    }//GEN-LAST:event_BtnCloseIn9ActionPerformed

    private void TSelularitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TSelularitasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TSelularitasKeyPressed

    private void TEritropoietikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TEritropoietikKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TEritropoietikKeyPressed

    private void TLeukopoietikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TLeukopoietikKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TLeukopoietikKeyPressed

    private void TTrombopoietikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TTrombopoietikKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TTrombopoietikKeyPressed

    private void TSelPlasmaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TSelPlasmaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TSelPlasmaKeyPressed

    private void TMitosisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TMitosisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TMitosisKeyPressed

    private void TKesanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKesanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TKesanKeyPressed

    private void TSaranKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TSaranKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TSaranKeyPressed

    private void MnCetakHasilSumTulangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakHasilSumTulangActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if(tabMode.getRowCount()==0){
        JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    }else if(Kd2.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
    }else if(!(Kd2.getText().trim().equals(""))){    
        try {   
            ps4=koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
            try {
                ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                rs=ps4.executeQuery();
                while(rs.next()){
                    
                    kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1");
                    if(!kamar.equals("")){
                        namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                " where kamar.kd_kamar='"+kamar+"' ");            
                        kamar="Kamar";
                    }else if(kamar.equals("")){
                        kamar="Poli";
                        namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                    }
                    Map<String, Object> param = new HashMap<>();
                    param.put("noperiksa",rs.getString("no_rawat"));
                    param.put("norm",rs.getString("no_rkm_medis"));
                    param.put("namapasien",rs.getString("nm_pasien"));
                    param.put("jkel",rs.getString("jk"));
                    param.put("umur",rs.getString("umur"));
                    param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                    param.put("tanggal",rs.getString("tgl_periksa"));
                    param.put("penjab",rs.getString("nm_dokter"));
                    param.put("petugas",rs.getString("nama"));
                    param.put("jam",rs.getString("jam"));
                    param.put("alamat",rs.getString("alamat"));
                    param.put("kamar",kamar);
                    param.put("namakamar",namakamar);
                    param.put("lahir",rs.getString("lahir") == null ? "" : rs.getString("lahir"));
                    finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                    param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                    finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                    param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                    
                    // =============== AMBIL DATA SUMSUM TULANG ===============
                    PreparedStatement psSum = null;
                    ResultSet rsSum = null;
                    try{
                        psSum = koneksi.prepareStatement(
                            "SELECT DATE_FORMAT(tgl_pengambilan,'%d %M %Y') AS tgl_pengambilan, " +
                            "       ratio_mieloid_eritroid, selularitas, eritropoietik, leukopoietik, " +
                            "       trombopoietik, sel_plasma, mitosis, kesan, saran " +
                            "FROM rsaj_lab_sum_tulang " +
                            "WHERE no_rawat=? AND tgl_periksa=? AND jam_periksa=? " +
                            "ORDER BY id DESC LIMIT 1"
                        );
                        psSum.setString(1, rs.getString("no_rawat"));
                        psSum.setString(2, Valid.SetTgl(rs.getString("tgl_periksa"))); // tgl_periksa di rs = dd-MM-yyyy
                        psSum.setString(3, rs.getString("jam"));
                        rsSum = psSum.executeQuery();
                        if(rsSum.next()){
                            // silakan sesuaikan nama param berikut dengan parameter di rptHasilSumsumTulang.jasper
                            param.put("tgl_pengambilan", rsSum.getString("tgl_pengambilan"));
                            param.put("ratio",         rsSum.getString("ratio_mieloid_eritroid"));
                            param.put("selularitas",   rsSum.getString("selularitas"));
                            param.put("eritropoietik", rsSum.getString("eritropoietik"));
                            param.put("leukopoietik",  rsSum.getString("leukopoietik"));
                            param.put("trombopoietik", rsSum.getString("trombopoietik"));
                            param.put("selplasma",     rsSum.getString("sel_plasma"));
                            param.put("mitosis",       rsSum.getString("mitosis"));
                            param.put("kesan_sumsum",  rsSum.getString("kesan"));
                            param.put("saran_sumsum",  rsSum.getString("saran"));
                        } else {
                            // kalau belum ada data tersimpan, kirim kosong
                            param.put("tgl_pengambilan", "");
                            param.put("ratio",         "");
                            param.put("selularitas",   "");
                            param.put("eritropoietik", "");
                            param.put("leukopoietik",  "");
                            param.put("trombopoietik", "");
                            param.put("selplasma",     "");
                            param.put("mitosis",       "");
                            param.put("kesan_sumsum",  "");
                            param.put("saran_sumsum",  "");
                        }
                    } catch (Exception exSum){
                        System.out.println("Notif sumsum tulang (cetak) : "+exSum);
                    } finally {
                        if(rsSum != null){
                            try { rsSum.close(); } catch(Exception ex) {}
                        }
                        if(psSum != null){
                            try { psSum.close(); } catch(Exception ex) {}
                        }
                    }
                    // ============= END AMBIL DATA SUMSUM TULANG =============

                    Sequel.queryu("delete from temporary_lab");

                    ps2=koneksi.prepareStatement(
                        "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                        "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                        "and periksa_lab.jam=?");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                        ps2.setString(3,rs.getString("jam"));
                        rs2=ps2.executeQuery();
                        i=0;
                        while(rs2.next()){
                            simpanTemporaryLabAman("'"+i+"','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                            i++;
                            ps3=koneksi.prepareStatement(
                                "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                            try {
                                ps3.setString(1,rs.getString("no_rawat"));
                                ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                ps3.setString(4,rs.getString("jam"));
                                rs3=ps3.executeQuery();
                                while(rs3.next()){
                                    simpanTemporaryLabAman("'"+i+"','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                            +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("Notif ps3 : "+e);
                            } finally{
                                if(rs3!=null){
                                    rs3.close();
                                }
                                if(ps3!=null){
                                    ps3.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif ps2 : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                    
                    param.put("namars",akses.getnamars());
                    param.put("alamatrs",akses.getalamatrs());
                    param.put("kotars",akses.getkabupatenrs());
                    param.put("propinsirs",akses.getpropinsirs());
                    param.put("kontakrs",akses.getkontakrs());
                    param.put("emailrs",akses.getemailrs());   
                    param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                    pspermintaan=koneksi.prepareStatement(
                            "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,"+
                            "IFNULL(DATE_FORMAT(tgl_sampel,'%d-%m-%Y'),'') as tgl_sampel,IFNULL(jam_sampel,'') as jam_sampel from permintaan_lab where "+
                            "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                    try {
                        pspermintaan.setString(1,rs.getString("no_rawat"));
                        pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                        pspermintaan.setString(3,rs.getString("jam"));
                        rspermintaan=pspermintaan.executeQuery();
                        if(rspermintaan.next()){
                            param.put("nopermintaan",rspermintaan.getString("noorder"));   
                            param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                            param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                            param.put("tanggalsampel",rspermintaan.getString("tgl_sampel"));
                            param.put("jamsampel",rspermintaan.getString("jam_sampel"));
                            Valid.MyReport("rptHasilSumsumTulang.jasper","report","::[ Hasil Evaluasi Sum Sum Tulang ]::",param);
                        }else{
                            param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                            Valid.MyReportPDF("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rspermintaan!=null){
                            rspermintaan.close();
                        }
                        if(pspermintaan!=null){
                            pspermintaan.close();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif ps4 : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps4!=null){
                    ps4.close();
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }            
    }
    this.setCursor(Cursor.getDefaultCursor());
    
    }//GEN-LAST:event_MnCetakHasilSumTulangActionPerformed

    private void MnUploadSumTulangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnUploadSumTulangActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());

        FileName = timestamp + "_" + tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().replace("/", "") + "_HasilSumsumTulang";

        CreatePDFSumTulang(FileName);
        ConvertPDFtoJPGSumTulang(FileName);
        UploadJPGSumTulang(FileName, "berkasrawat/pages/upload/");
        HapusJPGSumTulang();

        ppBerkasDigitalBtnPrintActionPerformed(evt);
    }//GEN-LAST:event_MnUploadSumTulangActionPerformed

    private void MnKirimWAActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
        
                                                          
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(Kd2.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
        }else if(!(Kd2.getText().trim().equals(""))){    
            try {   
                ps4=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.no_tlp,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                    " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                    "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                    "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                    "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
                try {
                    ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                    rs=ps4.executeQuery();
                    while(rs.next()){
                        
                        kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                        if(!kamar.equals("")){
                            namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                    " where kamar.kd_kamar='"+kamar+"' ");            
                            kamar="Kamar";
                        }else if(kamar.equals("")){
                            kamar="Poli";
                            namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                        }
                        Map<String, Object> param = new HashMap<>();
                        param.put("noperiksa",rs.getString("no_rawat"));
                        param.put("norm",rs.getString("no_rkm_medis"));
                        param.put("namapasien",rs.getString("nm_pasien"));
                        param.put("jkel",rs.getString("jk"));
                        param.put("umur",rs.getString("umur"));
                        param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                        param.put("tanggal",rs.getString("tgl_periksa"));
                        param.put("penjab",rs.getString("nm_dokter"));
                        param.put("petugas",rs.getString("nama"));
                        param.put("jam",rs.getString("jam"));
                        param.put("alamat",rs.getString("alamat"));
                        param.put("kamar",kamar);
                        param.put("namakamar",namakamar);
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                        param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                        

                        Sequel.queryu("delete from temporary_lab");

                        ps2=koneksi.prepareStatement(
                            "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                            "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                            "and periksa_lab.jam=?");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            ps2.setString(3,rs.getString("jam"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                ps3=koneksi.prepareStatement(
                                    "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                    "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                    "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                                try {
                                    ps3.setString(1,rs.getString("no_rawat"));
                                    ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                    ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                    ps3.setString(4,rs.getString("jam"));
                                    rs3=ps3.executeQuery();
                                    while(rs3.next()){
                                        simpanTemporaryLabAman("'0','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                                +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    }
                                } catch (Exception e) {
                                    System.out.println("Notif ps3 : "+e);
                                } finally{
                                    if(rs3!=null){
                                        rs3.close();
                                    }
                                    if(ps3!=null){
                                        ps3.close();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps2 : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                        pspermintaan=koneksi.prepareStatement(
                                "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel from permintaan_lab where "+
                                "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                        try {
                            pspermintaan.setString(1,rs.getString("no_rawat"));
                            pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                            pspermintaan.setString(3,rs.getString("jam"));
                            rspermintaan=pspermintaan.executeQuery();
                            if(rspermintaan.next()){
                                param.put("nopermintaan",rspermintaan.getString("noorder"));   
                                param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                                param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                                param.put("tanggalsampel",rspermintaan.getString("tgl_sampel"));  
                                param.put("jamsampel",rspermintaan.getString("jam_sampel"));                                
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",param);
                                
                                
                                //modif yahya
                                
                                try {
            String url = "https://api.fonnte.com/send";
            String tokenWA = koneksiDBSalim.TOKENWA();
            String noWa = rs.getString("no_tlp");
            String countryCode = "62"; 
            String template = Sequel.cariIsi("select wa_template from wa_template where no_template=?", "LAB");
            String pesan = template.replace("{nama}", rs.getString("nm_pasien"))
                    .replace("{rm}", rs.getString("no_rkm_medis"))
                    .replace("{antrian}", "")
                    .replace("{poli}", "")
                    .replace("{perujuk}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 5).toString())
                    .replace("{dokter}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString())
//                    .replace("{hasil}", rs.getString("hasil"))
                    .replace("{tanggal}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString()+" "+tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
            String simpanPesan = pesan.replace("*", "").replace("_", "").replace("{", "").replace("}", "");

            // Path to the local file to be sent
            String filePath = "report/rptPeriksaLabPermintaan.pdf";
            String fileName = tbDokter.getValueAt(tbDokter.getSelectedRow(), 1).toString()+".pdf"; // Optional

            String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", tokenWA);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                // Add form fields
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"target\"").append("\r\n\r\n").append(noWa).append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"message\"").append("\r\n\r\n").append(pesan).append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"countryCode\"").append("\r\n\r\n").append(countryCode).append("\r\n");

                // Add file part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
                writer.append("Content-Type: ").append("application/pd"
                        + "f").append("\r\n\r\n").flush();

                try (FileInputStream inputStream = new FileInputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush(); // Important to flush before closing writer
                }

                writer.append("\r\n").flush(); // End of file part

                // End of multipart request
                writer.append("--").append(boundary).append("--").append("\r\n").flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    String jsonResponse = response.toString();
                    System.out.println(jsonResponse);

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                    if ("false".equals(jsonNode.get("status").asText())) {
                        JOptionPane.showMessageDialog(null, "Gagal mengirim, mohon cek koneksi Whatsapp Gateway !!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if ("true".equals(jsonNode.get("status").asText())) {
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String dateNow = now.format(formatter);
//                        Sequel.queryu("INSERT INTO wa_report VALUES ('" + jsonNode.get("id").get(0).asText() + "', '" + rs.getString("no_rkm_medis") + "', '" + noWa + "', '" + akses.getkode() + "', 'Kirim Hasil Radilogi Pasien', '" + jsonNode.get("target").get(0).asText() + "', '" + simpanPesan + "', '', '" + jsonNode.get("status").asText() + "', '', '" + dateNow + "', '" + dateNow + "')");
                        JOptionPane.showMessageDialog(null, "Berhasil Mengirim Pesan !!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                throw new IOException("Server returned non-OK status: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
                                
//                                end modif yahya


                            }else{
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDF("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",param); 
                                
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rspermintaan!=null){
                                rspermintaan.close();
                            }
                            if(pspermintaan!=null){
                                pspermintaan.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps4 : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps4!=null){
                        ps4.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }            
        }
        this.setCursor(Cursor.getDefaultCursor());
    }                                             
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariPeriksaLab dialog = new DlgCariPeriksaLab(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCloseIn5;
    private widget.Button BtnCloseIn6;
    private widget.Button BtnCloseIn7;
    private widget.Button BtnCloseIn8;
    private widget.Button BtnCloseIn9;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnPrintCito;
    private widget.Button BtnPrintKritis;
    private widget.Button BtnPrintKritis1;
    private widget.Button BtnPrintKritis2;
    private widget.Button BtnSimpan;
    private widget.Button BtnSimpanPelapor;
    private widget.Button BtnSimpanPelaporKritis;
    private widget.Button BtnSimpanPelaporKritis1;
    private widget.Button BtnSimpanSumTulang;
    private widget.Button BtnUpload;
    private widget.TextBox HasilCito;
    private widget.TextBox HasilKritis;
    private widget.TextBox HasilKritis1;
    private widget.TextBox JenisKelamin;
    private widget.TextBox Kd2;
    private widget.TextBox Kd3;
    private widget.TextBox Kd4;
    private widget.TextArea Kesan;
    private javax.swing.JLabel LEritropoietik;
    private javax.swing.JLabel LKesan;
    private javax.swing.JLabel LLeukopoietik;
    private javax.swing.JLabel LMitosis;
    private javax.swing.JLabel LRatio;
    private javax.swing.JLabel LSaran;
    private javax.swing.JLabel LSelPlasma;
    private javax.swing.JLabel LSelularitas;
    private javax.swing.JLabel LTrombopoietik;
    private javax.swing.JLabel LWaktuValidasi;
    private widget.editorpane LoadHTML1;
    private javax.swing.JMenuItem MnCetakHasilLab;
    private javax.swing.JMenuItem MnCetakHasilLab1;
    private javax.swing.JMenuItem MnCetakHasilLab10;
    private javax.swing.JMenuItem MnCetakHasilLab11;
    private javax.swing.JMenuItem MnCetakHasilLab12;
    private javax.swing.JMenuItem MnCetakHasilLab13;
    private javax.swing.JMenuItem MnCetakHasilLab14;
    private javax.swing.JMenuItem MnCetakHasilLab15;
    private javax.swing.JMenuItem MnCetakHasilLab16;
    private javax.swing.JMenuItem MnCetakHasilLab17;
    private javax.swing.JMenuItem MnCetakHasilLab18;
    private javax.swing.JMenuItem MnCetakHasilLab19;
    private javax.swing.JMenuItem MnCetakHasilLab2;
    private javax.swing.JMenuItem MnCetakHasilLab20;
    private javax.swing.JMenuItem MnCetakHasilLab21;
    private javax.swing.JMenuItem MnCetakHasilLab22;
    private javax.swing.JMenuItem MnCetakHasilLab23;
    private javax.swing.JMenuItem MnCetakHasilLab3;
    private javax.swing.JMenuItem MnCetakHasilLab4;
    private javax.swing.JMenuItem MnCetakHasilLab5;
    private javax.swing.JMenuItem MnCetakHasilLab6;
    private javax.swing.JMenuItem MnCetakHasilLab7;
    private javax.swing.JMenuItem MnCetakHasilLab8;
    private javax.swing.JMenuItem MnCetakHasilLab9;
    private widget.Button MnCetakHasilSumTulang;
    private javax.swing.JMenu MnCetakLab;
    private javax.swing.JMenuItem MnCetakNota;
    private javax.swing.JMenuItem MnCetakSuratCovid;
    private javax.swing.JMenuItem MnHapusDataCito;
    private javax.swing.JMenuItem MnHapusDataKritis;
    private javax.swing.JMenu MnPDFLab;
    private javax.swing.JMenuItem MnPelapor;
    private javax.swing.JMenuItem MnPelaporKritis;
    private javax.swing.JMenuItem MnSaranKesan;
    private javax.swing.JMenuItem MnSumsumtulang;
    private javax.swing.JMenuItem MnUbah;
    private javax.swing.JMenuItem MnUbah1;
    private widget.Button MnUploadSumTulang;
    private javax.swing.JMenuItem MneLFG;
    private widget.TextBox NoRawat;
    private widget.TextBox Pelapor;
    private widget.TextBox PelaporKritis;
    private widget.TextBox PelaporKritis1;
    private widget.TextBox Penerima;
    private widget.TextBox PenerimaKritis;
    private widget.TextBox PenerimaKritis1;
    private widget.TextArea Saran;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane Scroll4;
    private widget.TextBox TCari;
    private widget.TextArea TEritropoietik;
    private widget.TextArea TKesan;
    private widget.TextArea TLeukopoietik;
    private widget.TextArea TMitosis;
    private widget.TextBox TRatio;
    private widget.TextArea TSaran;
    private widget.TextArea TSelPlasma;
    private widget.TextArea TSelularitas;
    private widget.TextArea TTrombopoietik;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.Tanggal TglPengambilan;
    private widget.TextBox UmurPasien;
    private widget.Tanggal WaktuLapor;
    private widget.Tanggal WaktuLaporKritis;
    private widget.Tanggal WaktuLaporKritis1;
    private widget.Tanggal WaktuRegis;
    private widget.Tanggal WaktuValidasi;
    private widget.Tanggal WaktuValidasi1;
    private javax.swing.JDialog WindowPelapor;
    private javax.swing.JDialog WindowPelaporKritis;
    private javax.swing.JDialog WindowSaran;
    private javax.swing.JDialog WindowSumTulang;
    private javax.swing.JDialog WindoweLFG;
    private widget.Button btnPasien;
    private widget.Button btnPetugas;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame10;
    private widget.InternalFrame internalFrame6;
    private widget.InternalFrame internalFrame7;
    private widget.InternalFrame internalFrame8;
    private widget.InternalFrame internalFrame9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JPopupMenu jPopupMenu3;
    private widget.TextBox kdmem;
    private widget.TextBox kdptg;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label13;
    private widget.Label label15;
    private widget.Label label16;
    private widget.Label label18;
    private widget.Label label9;
    private widget.TextBox nmmem;
    private widget.TextBox nmptg;
    private widget.panelisi panelGlass6;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBerkasDigital;
    private javax.swing.JMenuItem ppRiwayat;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane10;
    private widget.ScrollPane scrollPane11;
    private widget.ScrollPane scrollPane12;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.ScrollPane scrollPane8;
    private widget.ScrollPane scrollPane9;
    private widget.Table tbDokter;
    private widget.Table tbDokter1;
    private widget.Table tbDokter2;
    private widget.Table tbDokter3;
    // End of variables declaration//GEN-END:variables

//    private void tampil() {
//        try {
//            Valid.tabelKosong(tabMode);  
//            if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
//                ps=koneksi.prepareStatement(
//                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam,"+
//                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,penjab.png_jawab "+
//                    "from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "+
//                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                    "inner join petugas on periksa_lab.nip=petugas.nip "+
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
//                    "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter where periksa_lab.kategori='PK' and "+
//                    "periksa_lab.tgl_periksa between ? and ? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam) "+
//                    "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc");
//            }else{
//                ps=koneksi.prepareStatement(
//                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam,"+
//                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,penjab.png_jawab "+
//                    "from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "+
//                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                    "inner join petugas on periksa_lab.nip=petugas.nip "+
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
//                    "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter where periksa_lab.kategori='PK' and "+
//                    "periksa_lab.tgl_periksa between ? and ? and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and "+
//                    "(pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam) "+
//                    "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc");
//            }
//                
//            try {
//                if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                }else{
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                    ps.setString(3,"%"+NoRawat.getText()+"%");
//                    ps.setString(4,"%"+kdmem.getText()+"%");
//                    ps.setString(5,"%"+kdptg.getText()+"%");
//                    ps.setString(6,"%"+TCari.getText().trim()+"%");
//                    ps.setString(7,"%"+TCari.getText().trim()+"%");
//                    ps.setString(8,"%"+TCari.getText().trim()+"%");
//                    ps.setString(9,"%"+TCari.getText().trim()+"%");
//                }
//                    
//                rs=ps.executeQuery();
//                ttl=0;
//                while(rs.next()){
//                    kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                    if(!kamar.equals("")){
//                        namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
//                                    " where kamar.kd_kamar='"+kamar+"' ");            
//                        kamar="Kamar";
//                    }else if(kamar.equals("")){
//                        kamar="Poli";
//                        namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
//                                    "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
//                    }
//                    tabMode.addRow(new Object[]{rs.getString("no_rawat"),rs.getString("no_rkm_medis")+" "+rs.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",
//                                                rs.getString("nama"),rs.getString("tgl_periksa"),rs.getString("jam"),
//                                                Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")),rs.getString("nm_dokter")});
//                    tabMode.addRow(new Object[]{"","Cara Bayar : "+rs.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan"});
//                    ps2=koneksi.prepareStatement(
//                        "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
//                        "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
//                        "and periksa_lab.jam=? order by jns_perawatan_lab.kd_jenis_prw");
//                    try {
//                        ps2.setString(1,rs.getString("no_rawat"));
//                        ps2.setString(2,rs.getString("tgl_periksa"));
//                        ps2.setString(3,rs.getString("jam"));
//                        rs2=ps2.executeQuery();
//                        item=0;
//                        while(rs2.next()){     
//                           item=item+rs2.getDouble("biaya");
//                           ttl=ttl+rs2.getDouble("biaya");
//                           tabMode.addRow(new Object[]{"","",rs2.getString("kd_jenis_prw")+" "+rs2.getString("nm_perawatan")+" "+Valid.SetAngka(rs2.getDouble("biaya")),"","","",""});
//                           ps3=koneksi.prepareStatement(
//                                "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
//                                "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
//                                "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
//                           try {
//                                ps3.setString(1,rs.getString("no_rawat"));
//                                ps3.setString(2,rs2.getString("kd_jenis_prw"));
//                                ps3.setString(3,rs.getString("tgl_periksa"));
//                                ps3.setString(4,rs.getString("jam"));
//                                rs3=ps3.executeQuery();
//                                                                
////                                while(rs3.next()){
////                                    item=item+rs3.getDouble("biaya_item");
////                                    ttl=ttl+rs3.getDouble("biaya_item");
////                                    tabMode.addRow(new Object[]{"","","  "+rs3.getString("Pemeriksaan")+" "+Valid.SetAngka(rs3.getDouble("biaya_item")),rs3.getString("nilai"),
////                                                                rs3.getString("satuan"),rs3.getString("nilai_rujukan"),rs3.getString("keterangan")});
////                                }
//                                
//                                //modif mustafa ( otomatis H / L dan kritis )
//                                
//                                String kategoriUmur = getKategoriUmur(rs.getString("no_rawat"));
//
//                                while(rs3.next()){
//                                    item += rs3.getDouble("biaya_item");
//                                    ttl += rs3.getDouble("biaya_item");
//
//                                    String pemeriksaan = rs3.getString("Pemeriksaan").trim();
//                                    String nilaiStr = rs3.getString("nilai")
//                                                        
//                                                        .replace(",", "") // hapus koma ribuan seperti 323,000 jadi 323000
//                                                        .replace(".", ".") // opsional, pertahankan titik desimal
//                                                        .trim();
//                                    String satuan = rs3.getString("satuan");
//                                    String rujukan = rs3.getString("nilai_rujukan") == null ? "" : rs3.getString("nilai_rujukan").trim();
//                                    String keterangan = rs3.getString("keterangan") == null ? "" : rs3.getString("keterangan").trim();
//
//                                    String flag = "";
//
//                                    try {
//                                        double nilai = Double.parseDouble(nilaiStr);
//
//                                        // ==== CEK H/L berdasarkan nilai_rujukan ====
//                                        if (!rujukan.isEmpty()) {
//                                            try {
//                                                if (rujukan.contains("~") || rujukan.contains("-")) {
//                                                    String delimiter = rujukan.contains("~") ? "~" : "-";
//                                                    String[] split = rujukan.split(Pattern.quote(delimiter));
//                                                    double batasBawah = Double.parseDouble(split[0].trim());
//                                                    double batasAtas = Double.parseDouble(split[1].trim());
//
//                                                    if (nilai < batasBawah) {
//                                                        flag += "L";
//                                                    } else if (nilai > batasAtas) {
//                                                        flag += "H";
//                                                    }
//                                                } else if (rujukan.contains(">")) {
//                                                    double batas = Double.parseDouble(rujukan.replace(">", "").trim());
//                                                    if (nilai <= batas) {
//                                                        flag += "L";
//                                                    }
//                                                } else if (rujukan.contains("<")) {
//                                                    double batas = Double.parseDouble(rujukan.replace("<", "").trim());
//                                                    if (nilai >= batas) {
//                                                        flag += "H";
//                                                    }
//                                                }
//                                            } catch (Exception e) {
//                                                // rujukan parsing gagal
//                                            }
//                                        }
//
//
//                                        // ==== CEK NILAI KRITIS berdasarkan pemeriksaan & kategori umur ====
//                                        // NOTE: Jangan gunakan titik (.) sebagai pemisah ribuan, Java membacanya sebagai desimal.
//                                        String p = pemeriksaan.toLowerCase();
//                                        if (p.contains("hgb")) {
//                                            if (kategoriUmur.equals("Neonatus") && (nilai <= 10.0 || nilai >= 24.0)) flag += "*";
//                                            else if (kategoriUmur.equals("Anak") && (nilai <= 7.0 || nilai >= 20.0)) flag += "*";
//                                            else if (kategoriUmur.equals("Dewasa") && nilai <= 7.0) flag += "*";
//                                        } else if ((p.contains("wbc") || p.contains("leukosit"))) {
//                                            if (kategoriUmur.equals("Neonatus") && (nilai <= 5000 || nilai >= 50000)) flag += "*";
//                                            else if (!kategoriUmur.equals("Neonatus") && (nilai < 1000 || nilai >= 50000)) flag += "*";
//                                        } else if ((p.contains("plt") || p.contains("trombosit"))) {
//                                            if (kategoriUmur.equals("Neonatus") && nilai <= 50000) flag += "*";
//                                            else if (!kategoriUmur.equals("Neonatus") && (nilai <= 30000 || nilai >= 800000)) flag += "*";
//                                        } else if (p.contains("aptt") && nilai > 150) {
//                                            flag += "*";
//                                        } else if (p.contains("glukosa sewaktu")|| p.contains("gds strip") && (
//                                                   (kategoriUmur.equals("Neonatus") && (nilai <= 40 || nilai >= 250)) ||
//                                                   (!kategoriUmur.equals("Neonatus") && (nilai < 70 || nilai >= 600)))) {
//                                            flag += "*";
//                                        } else if (p.contains("kreatinin")) {
//                                            if (kategoriUmur.equals("Neonatus") && nilai >= 1.5) flag += "*";
//                                            else if (kategoriUmur.equals("Anak") && nilai >= 2.5) flag += "*";
//                                            else if (kategoriUmur.equals("Dewasa") && nilai >= 10.0) flag += "*";
//                                        } else if (p.contains("bilirubin")) {
//                                            if (kategoriUmur.equals("Neonatus") && nilai >= 15.0) flag += "*";                                                 
//                                        } else if (p.contains("ckmb") && nilai > 75) {
//                                            flag += "*";
//                                        } else if (p.contains("troponin") && nilai > 0.05) {
//                                            flag += "*";
//                                        } else if (p.contains("kalium")) {
//                                            if (kategoriUmur.equals("Neonatus") && (nilai <= 3.5 || nilai >=6.0)) flag += "*";
//                                            else if (!kategoriUmur.equals("Neonatus") && (nilai <= 2.5 || nilai >=6.0)) flag += "*";
//                                            if (nilai >= 6.0) flag += "*";
//                                        } else if (p.contains("ft4") && nilai >= 7.77) {
//                                            flag += "*";
//                                        }
//
//                                    } catch (Exception e) {
//                                        // parsing gagal = biarkan
//                                    }
//
//                                    tabMode.addRow(new Object[]{
//                                        "", "", "  " + pemeriksaan + " " + Valid.SetAngka(rs3.getDouble("biaya_item")),
//                                        nilaiStr, satuan, rujukan,
//                                        keterangan + (flag.equals("") ? "" : "" + flag + "")
//                                    });
//                                }
//                                
//                                // end modif mustafa
//                                
//                           } catch (Exception e) {
//                               System.out.println("Notif ps3 : "+e);
//                           } finally{
//                                if(rs3!=null){
//                                    rs3.close();
//                                }
//                                if(ps3!=null){
//                                    ps3.close();
//                                }
//                           }                                
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Notif ps2 : "+e);
//                    } finally{
//                        if(rs2!=null){
//                            rs2.close();
//                        }
//                        if(ps2!=null){
//                            ps2.close();
//                        }
//                    }                        
//
//                    saran="";kesan="";
//                    ps5=koneksi.prepareStatement(
//                        "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?");  
//                    try {
//                        ps5.setString(1,rs.getString("no_rawat"));
//                        ps5.setString(2,rs.getString("tgl_periksa"));
//                        ps5.setString(3,rs.getString("jam"));
//                        rs5=ps5.executeQuery();
//                        if(rs5.next()){      
//                            kesan=rs5.getString("kesan");saran=rs5.getString("saran");
//                        } 
//                    } catch (Exception e) {
//                        System.out.println("Notif : "+e);
//                    } finally{
//                        if(rs5!=null){
//                            rs5.close();
//                        }
//                        if(ps5!=null){
//                            ps5.close();
//                        }
//                    }   
//                    if(item>0){
//                        tabMode.addRow(new Object[]{"","","Biaya Periksa : "+Valid.SetAngka(item),"","","Kesan : "+kesan,"Saran : "+saran});
//                        tabMode.addRow(new Object[]{"","","","","","",""});
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Notif ps : "+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(ps!=null){
//                    ps.close();
//                }
//            }
//            
//            if(ttl>0){
//                  tabMode.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","",""});
//            }  
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//        
//    }
    
private void tampil() {
    try {
        Valid.tabelKosong(tabMode);
        if (NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
            ps = koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                "DATE_FORMAT(pasien.tgl_lahir,'%d/%m/%Y') as tgl_lahir, " + // << tambah tgl lahir
                "petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam," +
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,penjab.png_jawab " +
                "from periksa_lab " +
                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join petugas on periksa_lab.nip=petugas.nip " +
                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
                "group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam) " +
                "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc"
            );
        } else {
            ps = koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                "DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir, " + // << tambah tgl lahir
                "petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam," +
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,penjab.png_jawab " +
                "from periksa_lab " +
                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join petugas on periksa_lab.nip=petugas.nip " +
                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
                "and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and " +
                "(pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) " +
                "group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam) " +
                "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc"
            );
        }

        try {
            if (NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
                ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            } else {
                ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
                ps.setString(3, "%" + NoRawat.getText() + "%");
                ps.setString(4, "%" + kdmem.getText() + "%");
                ps.setString(5, "%" + kdptg.getText() + "%");
                ps.setString(6, "%" + TCari.getText().trim() + "%");
                ps.setString(7, "%" + TCari.getText().trim() + "%");
                ps.setString(8, "%" + TCari.getText().trim() + "%");
                ps.setString(9, "%" + TCari.getText().trim() + "%");
            }

            rs = ps.executeQuery();
            ttl = 0;
            while (rs.next()) {
                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='" + rs.getString("no_rawat") + "' order by kamar_inap.tgl_masuk desc limit 1");
                if (!kamar.equals("")) {
                    namakamar = kamar + ", " + Sequel.cariIsi(
                        "select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal " +
                        " where kamar.kd_kamar='" + kamar + "' "
                    );
                    kamar = "Kamar";
                } else if (kamar.equals("")) {
                    kamar = "Poli";
                    namakamar = Sequel.cariIsi(
                        "select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli " +
                        "where reg_periksa.no_rawat='" + rs.getString("no_rawat") + "'"
                    );
                }

                // << di sini aku selipkan tgl lahir setelah nama pasien >>
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis") + " " +
                    rs.getString("nm_pasien") + " - " + rs.getString("tgl_lahir") + " " + // ← tgl lahir setelah nama pasien
                    "(" + kamar + " : " + namakamar + ")",

                    rs.getString("nama"),
                    rs.getString("tgl_periksa"),
                    rs.getString("jam"),
                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rs.getString("dokter_perujuk")),
                    rs.getString("nm_dokter")
                });

                tabMode.addRow(new Object[]{"", "Cara Bayar : " + rs.getString("png_jawab"), "Pemeriksaan", "Hasil", "Satuan", "Nilai Rujukan", "Keterangan"});

                ps2 = koneksi.prepareStatement(
                    "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya " +
                    "from periksa_lab inner join jns_perawatan_lab " +
                    "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? " +
                    "and periksa_lab.jam=? order by jns_perawatan_lab.kd_jenis_prw"
                );
                try {
                    ps2.setString(1, rs.getString("no_rawat"));
                    ps2.setString(2, rs.getString("tgl_periksa"));
                    ps2.setString(3, rs.getString("jam"));
                    rs2 = ps2.executeQuery();
                    item = 0;
                    while (rs2.next()) {
                        item = item + rs2.getDouble("biaya");
                        ttl = ttl + rs2.getDouble("biaya");
                        tabMode.addRow(new Object[]{"", "", rs2.getString("kd_jenis_prw") + " " + rs2.getString("nm_perawatan") + " " + Valid.SetAngka(rs2.getDouble("biaya")), "", "", "", ""});
                        ps3 = koneksi.prepareStatement(
                            "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item," +
                            "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.id_template " +
                            "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template " +
                            "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? " +
                            "order by template_laboratorium.urut"
                        );
                        try {
                            ps3.setString(1, rs.getString("no_rawat"));
                            ps3.setString(2, rs2.getString("kd_jenis_prw"));
                            ps3.setString(3, rs.getString("tgl_periksa"));
                            ps3.setString(4, rs.getString("jam"));
                            rs3 = ps3.executeQuery();

                            String kategoriUmur = getKategoriUmur(rs.getString("no_rawat"));

                            while (rs3.next()) {
                                item += rs3.getDouble("biaya_item");
                                ttl += rs3.getDouble("biaya_item");

                                String pemeriksaan = rs3.getString("Pemeriksaan").trim();
                                String nilaiStr = normalisasiNilaiLabCetak(rs3.getString("nilai"));
                                String satuan = rs3.getString("satuan");
                                String rujukan = rs3.getString("nilai_rujukan") == null ? "" : rs3.getString("nilai_rujukan").trim();
                                String keterangan = rs3.getString("keterangan") == null ? "" : rs3.getString("keterangan").trim();
                                String flag = getStatusHLKritisLab(nilaiStr, rujukan, rs3.getString("kd_jenis_prw"), rs3.getString("id_template"), kategoriUmur);

                                tabMode.addRow(new Object[]{
                                    "", "", "  " + pemeriksaan + " " + Valid.SetAngka(rs3.getDouble("biaya_item")),
                                    nilaiStr, satuan, rujukan,
                                    keterangan + (flag.endsWith("*") ? "**" : (flag.equals("") ? "" : "*"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps3 : " + e);
                        } finally {
                            if (rs3 != null) rs3.close();
                            if (ps3 != null) ps3.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif ps2 : " + e);
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                saran = ""; kesan = "";
                ps5 = koneksi.prepareStatement(
                    "select saran,kesan from saran_kesan_lab where no_rawat=? and tgl_periksa=? and jam=?"
                );
                try {
                    ps5.setString(1, rs.getString("no_rawat"));
                    ps5.setString(2, rs.getString("tgl_periksa"));
                    ps5.setString(3, rs.getString("jam"));
                    rs5 = ps5.executeQuery();
                    if (rs5.next()) {
                        kesan = rs5.getString("kesan");
                        saran = rs5.getString("saran");
                    }
                } catch (Exception e) {
                    System.out.println("Notif : " + e);
                } finally {
                    if (rs5 != null) rs5.close();
                    if (ps5 != null) ps5.close();
                }

                if (item > 0) {
                    tabMode.addRow(new Object[]{"", "", "Biaya Periksa : " + Valid.SetAngka(item), "", "", "Kesan : " + kesan, "Saran : " + saran});
                    tabMode.addRow(new Object[]{"", "", "", "", "", "", ""});
                }
            }
        } catch (Exception e) {
            System.out.println("Notif ps : " + e);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }

        if (ttl > 0) {
            tabMode.addRow(new Object[]{">>", "Total : " + Valid.SetAngka(ttl), "", "", "", "", ""});
        }
    } catch (Exception ex) {
        System.out.println(ex);
    }
}
    
    
    private void tampil2() {
        try {
            Valid.tabelKosong(tabMode2);  
            if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.status,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,penjab.png_jawab,"+
                    "template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,detail_periksa_lab.keterangan,poliklinik.nm_poli, "+
                    "jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan from periksa_lab inner join reg_periksa inner join pasien inner join penjab "+
                    "inner join petugas inner join dokter inner join detail_periksa_lab inner join jns_perawatan_lab inner join template_laboratorium inner join poliklinik "+
                    "on detail_periksa_lab.id_template=template_laboratorium.id_template and periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.kd_dokter=dokter.kd_dokter and "+
                    "periksa_lab.no_rawat=detail_periksa_lab.no_rawat and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw and periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw and "+
                    "periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa and periksa_lab.jam=detail_periksa_lab.jam and periksa_lab.nip=petugas.nip and reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "and reg_periksa.kd_pj=penjab.kd_pj where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc,detail_periksa_lab.kd_jenis_prw,template_laboratorium.urut");
            }else{
                ps=koneksi.prepareStatement(
                    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.status,"+
                    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,dokter.nm_dokter,template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,penjab.png_jawab,"+
                    "template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,detail_periksa_lab.keterangan,poliklinik.nm_poli, "+
                    "jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan from periksa_lab inner join reg_periksa inner join pasien inner join penjab "+
                    "inner join petugas inner join dokter inner join detail_periksa_lab inner join jns_perawatan_lab inner join template_laboratorium inner join poliklinik "+
                    "on detail_periksa_lab.id_template=template_laboratorium.id_template and periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.kd_dokter=dokter.kd_dokter and "+
                    "periksa_lab.no_rawat=detail_periksa_lab.no_rawat and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw and periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw and "+
                    "periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa and periksa_lab.jam=detail_periksa_lab.jam and periksa_lab.nip=petugas.nip and reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "and reg_periksa.kd_pj=penjab.kd_pj where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and "+
                    "(pasien.nm_pasien like ? or petugas.nama like ? or dokter.nm_dokter like ? or template_laboratorium.Pemeriksaan like ? or jns_perawatan_lab.nm_perawatan like ? or detail_periksa_lab.keterangan like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) "+
                    "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc,detail_periksa_lab.kd_jenis_prw,template_laboratorium.urut");
            }
                
            try {
                if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else{
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                    ps.setString(3,"%"+NoRawat.getText()+"%");
                    ps.setString(4,"%"+kdmem.getText()+"%");
                    ps.setString(5,"%"+kdptg.getText()+"%");
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                    ps.setString(8,"%"+TCari.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                while(rs.next()){
                    kamar=rs.getString("nm_poli");
                    if(rs.getString("status").equals("Ranap")){
                        kamar=Sequel.cariIsi(
                                "select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                " and kamar_inap.kd_kamar=kamar.kd_kamar where no_rawat=? order by tgl_masuk desc limit 1 ",rs.getString("no_rawat"));
                    }
                    
                    tabMode2.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis")+" "+rs.getString("nm_pasien")+" ("+rs.getString("png_jawab")+")",
                        rs.getString("tgl_periksa"),rs.getString("jam"),rs.getString("nm_perawatan"),rs.getString("Pemeriksaan"),
                        rs.getString("nilai"),rs.getString("satuan"),rs.getString("nilai_rujukan"),rs.getString("keterangan"),
                        kamar,rs.getString("nama"),Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")),
                        rs.getString("nm_dokter")                        
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif ps : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }        
    }
    
    public void tampil3(){        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder();
            htmlContent.append(
                "<tr class='head'>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='1%'>No.</td>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='4%'>No.RM</td>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='12%'>Nama Pasien</td>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='2%'>Umur</td>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='1%'>J.K.</td>"+
                    "<td valign='top' bgcolor='#FFFAF8' align='center' width='80%'>Kunjungan</td>"+
                "</tr>"); 
            if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,penjab.png_jawab,"+
                    "pasien.jk from pasien inner join reg_periksa on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                    "inner join periksa_lab on reg_periksa.no_rawat=periksa_lab.no_rawat "+
                    "inner join dokter on periksa_lab.dokter_perujuk=dokter.kd_dokter "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw "+
                    "inner join petugas on periksa_lab.nip=petugas.nip "+
                    "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? group by pasien.no_rkm_medis order by tgl_registrasi desc");
            }else{
                ps=koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,penjab.png_jawab,"+
                    "pasien.jk from pasien inner join reg_periksa on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                    "inner join periksa_lab on reg_periksa.no_rawat=periksa_lab.no_rawat "+
                    "inner join dokter on periksa_lab.dokter_perujuk=dokter.kd_dokter "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw "+
                    "inner join petugas on periksa_lab.nip=petugas.nip "+
                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and periksa_lab.tgl_periksa between ? and ? and "+
                    "(pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or jns_perawatan_lab.nm_perawatan like ? or periksa_lab.no_rawat like ? or petugas.nama like ? or dokter.nm_dokter like ? or penjab.png_jawab like ?) group by pasien.no_rkm_medis order by tgl_registrasi desc");
            }
                
            try {
                if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else{
                    ps.setString(1,"%"+NoRawat.getText()+"%");
                    ps.setString(2,"%"+kdmem.getText()+"%");
                    ps.setString(3,"%"+kdptg.getText()+"%");
                    ps.setString(4,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(5,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                    ps.setString(8,"%"+TCari.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                i=1;
                jmlkunjungan=0;
                jmlpemeriksaan=0;
                jmlsubpemeriksaan=0;
                while(rs.next()){
                    htmlContent.append(
                        "<tr class='isi'>"+
                            "<td valign='top' align='center'>"+i+"</td>"+
                            "<td valign='top' align='center'>"+rs.getString("no_rkm_medis")+"</td>"+
                            "<td valign='top'>"+rs.getString("nm_pasien")+" ("+rs.getString("png_jawab")+")"+"</td>"+
                            "<td valign='top'>"+rs.getString("umurdaftar")+" "+rs.getString("sttsumur")+"</td>"+
                            "<td valign='top'>"+rs.getString("jk")+"</td>"+
                            "<td valign='top'>"+
                                "<table width='100%' border='0' align='center' cellpadding='2px' cellspacing='0' class='tbl_form'>"+
                                    "<tr class='isi'>"+
                                        "<td valign='top' bgcolor='#fdfff9' align='center' width='8%'>No.Rawat</td>"+
                                        "<td valign='top' bgcolor='#fdfff9' align='center' width='8%'>Tanggal</td>"+
                                        "<td valign='top' bgcolor='#fdfff9' align='center' width='84%'>Pemeriksaan</td>"+
                                    "</tr>");
                    
                    ps2=koneksi.prepareStatement(
                        "select periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam,dokter.nm_dokter from "+
                        "pasien inner join reg_periksa on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                        "inner join periksa_lab on reg_periksa.no_rawat=periksa_lab.no_rawat "+
                        "inner join dokter on periksa_lab.dokter_perujuk=dokter.kd_dokter "+
                        "inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw "+
                        "inner join petugas on periksa_lab.nip=petugas.nip "+
                        "where periksa_lab.kategori='PK' and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and reg_periksa.no_rkm_medis=? and periksa_lab.tgl_periksa between ? and ? and "+
                        "(pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or jns_perawatan_lab.nm_perawatan like ? or periksa_lab.no_rawat like ? or petugas.nama like ? or dokter.nm_dokter like ? )"+
                        "group by periksa_lab.tgl_periksa,periksa_lab.jam order by tgl_registrasi");
                        
                    try {
                        ps2.setString(1,"%"+NoRawat.getText()+"%");
                        ps2.setString(2,"%"+kdmem.getText()+"%");
                        ps2.setString(3,"%"+kdptg.getText()+"%");
                        ps2.setString(4,rs.getString("no_rkm_medis"));
                        ps2.setString(5,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                        ps2.setString(6,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                        ps2.setString(7,"%"+TCari.getText().trim()+"%");
                        ps2.setString(8,"%"+TCari.getText().trim()+"%");
                        ps2.setString(9,"%"+TCari.getText().trim()+"%");
                        ps2.setString(10,"%"+TCari.getText().trim()+"%");   
                        ps2.setString(11,"%"+TCari.getText().trim()+"%"); 
                        ps2.setString(12,"%"+TCari.getText().trim()+"%"); 
                               
                        rs2=ps2.executeQuery();
                        while(rs2.next()){
                            htmlContent.append(
                                "<tr class='isi'>"+
                                    "<td valign='top' align='center'>"+rs2.getString("no_rawat")+"</td>"+
                                    "<td valign='top'>"+rs2.getString("tgl_periksa")+" "+rs2.getString("jam")+"</td>"+
                                    "<td valign='top'>"+
                                        "<table width='100%' border='0' align='center' cellpadding='2px' cellspacing='0' class='tbl_form'>"+
                                            "<tr class='isi'>"+
                                                "<td valign='top' bgcolor='#fdfff9' align='center' width='16%'>P.J.Lab</td>"+
                                                "<td valign='top' bgcolor='#fdfff9' align='center' width='16%'>Perujuk</td>"+
                                                "<td valign='top' bgcolor='#fdfff9' align='center' width='16%'>Petugas Lab</td>"+
                                                "<td valign='top' bgcolor='#fdfff9' align='center' width='62%'>Detail Pemeriksaan</td>"+
                                            "</tr>");
                            
                            ps3=koneksi.prepareStatement(
                                "select periksa_lab.kd_dokter,dokter.nm_dokter,petugas.nama as nm_petugas,jns_perawatan_lab.nm_perawatan,periksa_lab.kd_jenis_prw from "+
                                "pasien inner join reg_periksa on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                                "inner join periksa_lab on reg_periksa.no_rawat=periksa_lab.no_rawat "+
                                "inner join dokter on periksa_lab.dokter_perujuk=dokter.kd_dokter "+
                                "inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw "+
                                "inner join petugas on periksa_lab.nip=petugas.nip "+
                                "where periksa_lab.kategori='PK' and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? and reg_periksa.no_rkm_medis=? and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.tgl_periksa between ? and ? and "+
                                "(pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or jns_perawatan_lab.nm_perawatan like ? or periksa_lab.no_rawat like ? or petugas.nama like ? or dokter.nm_dokter like ?)"+
                                "order by tgl_registrasi");
                                
                            try {
                                ps3.setString(1,"%"+NoRawat.getText()+"%");
                                ps3.setString(2,"%"+kdmem.getText()+"%");
                                ps3.setString(3,"%"+kdptg.getText()+"%");
                                ps3.setString(4,rs.getString("no_rkm_medis"));
                                ps3.setString(5,rs2.getString("no_rawat"));
                                ps3.setString(6,rs2.getString("tgl_periksa"));
                                ps3.setString(7,rs2.getString("jam"));
                                ps3.setString(8,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                                ps3.setString(9,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                                ps3.setString(10,"%"+TCari.getText().trim()+"%");
                                ps3.setString(11,"%"+TCari.getText().trim()+"%");
                                ps3.setString(12,"%"+TCari.getText().trim()+"%");
                                ps3.setString(13,"%"+TCari.getText().trim()+"%");
                                ps3.setString(14,"%"+TCari.getText().trim()+"%");
                                ps3.setString(15,"%"+TCari.getText().trim()+"%");
                                    
                                rs3=ps3.executeQuery();
                                while(rs3.next()){
                                    htmlContent.append(
                                        "<tr class='isi'>"+
                                            "<td valign='top'>"+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs3.getString("kd_dokter"))+"</td>"+
                                            "<td valign='top'>"+rs3.getString("nm_dokter")+"</td>"+
                                            "<td valign='top'>"+rs3.getString("nm_petugas")+"</td>"+
                                            "<td valign='top'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='2px' cellspacing='0' class='tbl_form'>"+
                                                    "<tr class='isi'>"+
                                                        "<td valign='top' bgcolor='#fdfff9' align='left' colspan='5'>"+rs3.getString("nm_perawatan")+"</td>"+
                                                    "</tr>");
                                    ps5=koneksi.prepareStatement(
                                            "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,"+
                                            "template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,"+
                                            "detail_periksa_lab.keterangan from detail_periksa_lab inner join "+
                                            "template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                            "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and "+
                                            "detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? "+
                                            "order by detail_periksa_lab.kd_jenis_prw,template_laboratorium.urut ");
                                    try {
                                        ps5.setString(1,rs2.getString("no_rawat"));
                                        ps5.setString(2,rs3.getString("kd_jenis_prw"));
                                        ps5.setString(3,rs2.getString("tgl_periksa"));
                                        ps5.setString(4,rs2.getString("jam"));
                                        rs5=ps5.executeQuery();
                                        if(rs5.next()){
                                            htmlContent.append(
                                                "<tr class='isi'>"+
                                                    "<td valign='top' bgcolor='#fdfff9' align='center' width='35%'>Pemeriksaan</td>"+
                                                    "<td valign='top' bgcolor='#fdfff9' align='center' width='15%'>Hasil</td>"+
                                                    "<td valign='top' bgcolor='#fdfff9' align='center' width='10%'>Satuan</td>"+
                                                    "<td valign='top' bgcolor='#fdfff9' align='center' width='20%'>Nilai Rujukan</td>"+
                                                    "<td valign='top' bgcolor='#fdfff9' align='center' width='20%'>Keterangan</td>"+
                                                "</tr>");
                                        }
                                        rs5.beforeFirst();
                                        while(rs5.next()){
                                            htmlContent.append(
                                                "<tr class='isi'>"+
                                                    "<td valign='top'>"+rs5.getString("Pemeriksaan")+"</td>"+
                                                    "<td valign='top' align='center'>"+rs5.getString("nilai")+"</td>"+
                                                    "<td valign='top' align='center'>"+rs5.getString("satuan")+"</td>"+
                                                    "<td valign='top' align='center'>"+rs5.getString("nilai_rujukan")+"</td>"+
                                                    "<td valign='top'>"+rs5.getString("keterangan")+"</td>"+
                                                "</tr>");
                                            jmlsubpemeriksaan++;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs5!=null){
                                            rs5.close();
                                        }
                                        if(ps5!=null){
                                            ps5.close();
                                        }
                                    }                                 
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>");
                                    jmlpemeriksaan++;                                    
                                }
                            } catch (Exception e) {
                                System.out.println("Notif : "+e);
                            } finally{
                                if(rs3!=null){
                                    rs3.close();
                                }
                                if(ps3!=null){
                                    ps3.close();
                                }
                            }
                            htmlContent.append(
                                        "</table>"+
                                    "</td>"+
                                "</tr>");
                            jmlkunjungan++;
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
                                "</table>"+
                            "</td>"+
                        "</tr>");
                    i++;
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
            if((i-1)>0){
                datapasien=
                      "<table width='1400px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                          "<tr class='isi'>"+
                            "<td valign='top' align='center' width='25%'>Jumlah Pasien : "+(i-1)+"</td>"+
                            "<td valign='top' align='center' width='25%'>Jumlah Kunjungan : "+(jmlkunjungan)+"</td>"+
                            "<td valign='top' align='center' width='25%'>Jumlah Pemeriksaan : "+(jmlpemeriksaan)+"</td>"+
                            "<td valign='top' align='center' width='25%'>Jumlah Sub Pemeriksaan : "+(jmlsubpemeriksaan)+"</td>"+
                          "</tr>"+
                      "</table>";
            }
            LoadHTML1.setText(
                    "<html>"+
                      "<table width='1400px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                       htmlContent.toString()+
                      "</table>"+datapasien+
                    "</html>");
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        } 
        this.setCursor(Cursor.getDefaultCursor());
    }
    
//private void tampil4() {
//    try {
//        Valid.tabelKosong(tabMode3);  
//        if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")){
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        } else {
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? " +
//                "and (pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        }
//
//        try {
//            if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")){
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//            } else {
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                psM.setString(3, "%"+NoRawat.getText()+"%");
//                psM.setString(4, "%"+kdmem.getText()+"%");
//                psM.setString(5, "%"+kdptg.getText()+"%");
//                psM.setString(6, "%"+TCari.getText().trim()+"%");
//                psM.setString(7, "%"+TCari.getText().trim()+"%");
//                psM.setString(8, "%"+TCari.getText().trim()+"%");
//                psM.setString(9, "%"+TCari.getText().trim()+"%");
//            }
//
//            rsM = psM.executeQuery();
//            ttl = 0;
//            while(rsM.next()) {
//                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rsM.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                if(!kamar.equals("")) {
//                    namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
//                    kamar = "Kamar";
//                } else {
//                    kamar = "Poli";
//                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rsM.getString("no_rawat")+"'");
//                }
//
//                tabMode3.addRow(new Object[]{
//                    rsM.getString("no_rawat"),
//                    rsM.getString("no_rkm_medis")+" "+rsM.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",
//                    rsM.getString("nama"),
//                    rsM.getString("tgl_periksa"),
//                    rsM.getString("jam"),
//                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rsM.getString("dokter_perujuk")),
//                    rsM.getString("nm_dokter"),
//                    rsM.getString("informasi_tambahan")
//                });
//
//                tabMode3.addRow(new Object[]{"","Cara Bayar : "+rsM.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan"});
//
//                ps2M = koneksi.prepareStatement(
//                    "select jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
//                    "from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
//                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? " +
//                    "order by jns_perawatan_lab.kd_jenis_prw"
//                );
//
//                ps2M.setString(1, rsM.getString("no_rawat"));
//                ps2M.setString(2, rsM.getString("tgl_periksa"));
//                ps2M.setString(3, rsM.getString("jam"));
//                rs2M = ps2M.executeQuery();
//                item = 0;
//                while(rs2M.next()) {
//                    item += rs2M.getDouble("biaya");
//                    ttl += rs2M.getDouble("biaya");
//                    tabMode3.addRow(new Object[]{"","",rs2M.getString("kd_jenis_prw")+" "+rs2M.getString("nm_perawatan")+" "+Valid.SetAngka(rs2M.getDouble("biaya")),"","","",""});
//
//                    ps3M = koneksi.prepareStatement(
//                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan, detail_periksa_lab.kd_jenis_prw " +
//                        "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template " +
//                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? " +
//                        "order by template_laboratorium.urut"
//                    );
//
//                    ps3M.setString(1, rsM.getString("no_rawat"));
//                    ps3M.setString(2, rs2M.getString("kd_jenis_prw"));
//                    ps3M.setString(3, rsM.getString("tgl_periksa"));
//                    ps3M.setString(4, rsM.getString("jam"));
//                    rs3M = ps3M.executeQuery();
//                    while(rs3M.next()) {
//                        item += rs3M.getDouble("biaya_item");
//                        ttl += rs3M.getDouble("biaya_item");
//                        tabMode3.addRow(new Object[]{"","","  "+rs3M.getString("Pemeriksaan")+" "+Valid.SetAngka(rs3M.getDouble("biaya_item")),
//                            rs3M.getString("nilai"),
//                            rs3M.getString("satuan"),
//                            rs3M.getString("nilai_rujukan"),
//                            rs3M.getString("keterangan")
//                        });
//                    }
//                    rs3M.close(); ps3M.close();
//                }
//                rs2M.close(); ps2M.close();
//
//                pelapor=""; penerima="";
//                ps5M = koneksi.prepareStatement("select pelapor, penerima from rsaj_monev_cito_kritis where no_rawat=? and tgl_periksa=? and jam=?");
//                ps5M.setString(1, rsM.getString("no_rawat"));
//                ps5M.setString(2, rsM.getString("tgl_periksa"));
//                ps5M.setString(3, rsM.getString("jam"));
//                rs5M = ps5M.executeQuery();
//                if(rs5M.next()) {
//                    pelapor = rs5M.getString("Pelapor");
//                    penerima = rs5M.getString("Penerima");
//                }
//                rs5M.close(); ps5M.close();
//
//                if(item > 0){
//                    tabMode3.addRow(new Object[]{"","","Biaya Periksa : "+Valid.SetAngka(item),"","","Pelapor : "+pelapor,"Penerima : "+penerima});
//                }
//            }
//            rsM.close(); psM.close();
//
//            if(ttl > 0){
//                tabMode3.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","",""});
//            }
//
//        } catch (Exception e) {
//            System.out.println("Notif psM : " + e);
//        }
//
//    } catch (Exception ex) {
//        System.out.println(ex);
//    }
//    }    
    
//// tampil yang muncul     
//private void tampil4() {
//    try {
//        Valid.tabelKosong(tabMode3);
//        if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        } else {
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? " +
//                "and (pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        }
//
//        try {
//            if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//            } else {
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                psM.setString(3, "%"+NoRawat.getText()+"%");
//                psM.setString(4, "%"+kdmem.getText()+"%");
//                psM.setString(5, "%"+kdptg.getText()+"%");
//                psM.setString(6, "%"+TCari.getText().trim()+"%");
//                psM.setString(7, "%"+TCari.getText().trim()+"%");
//                psM.setString(8, "%"+TCari.getText().trim()+"%");
//                psM.setString(9, "%"+TCari.getText().trim()+"%");
//            }
//
//            rsM = psM.executeQuery();
//            ttl = 0;
//            while(rsM.next()) {
//                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rsM.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                if(!kamar.equals("")) {
//                    namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
//                    kamar = "Kamar";
//                } else {
//                    kamar = "Poli";
//                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rsM.getString("no_rawat")+"'");
//                }
//
//                tabMode3.addRow(new Object[]{
//                    rsM.getString("no_rawat"),
//                    rsM.getString("no_rkm_medis")+" "+rsM.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",
//                    rsM.getString("nama"),
//                    rsM.getString("tgl_periksa"),
//                    rsM.getString("jam"),
//                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rsM.getString("dokter_perujuk")),
//                    rsM.getString("nm_dokter"),
//                    rsM.getString("informasi_tambahan"),
//                    "Pelapor : "+pelapor
//                });
//
//                // Ambil pelapor, penerima, dan jam lapor
//                String pelapor = "", penerima = "", waktu = "";
//                ps5M = koneksi.prepareStatement("select pelapor, penerima, waktu from rsaj_monev_cito_kritis where no_rawat=? and tgl_periksa=? and jam=?");
//                ps5M.setString(1, rsM.getString("no_rawat"));
//                ps5M.setString(2, rsM.getString("tgl_periksa"));
//                ps5M.setString(3, rsM.getString("jam"));
//                rs5M = ps5M.executeQuery();
//                if(rs5M.next()) {
//                    pelapor = rs5M.getString("pelapor");
//                    penerima = rs5M.getString("penerima");
//                    waktu = rs5M.getString("waktu");
//                }
//                rs5M.close(); ps5M.close();
//
//                // Tambah baris jam pelaporan hasil
//                tabMode3.addRow(new Object[]{"","Cara Bayar : "+rsM.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan","","Penerima : "+penerima});
//                tabMode3.addRow(new Object[]{"","","","","","","","","Jam Lapor : "+waktu});
//
//                ps2M = koneksi.prepareStatement(
//                    "select jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
//                    "from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
//                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? " +
//                    "order by jns_perawatan_lab.kd_jenis_prw"
//                );
//
//                ps2M.setString(1, rsM.getString("no_rawat"));
//                ps2M.setString(2, rsM.getString("tgl_periksa"));
//                ps2M.setString(3, rsM.getString("jam"));
//                rs2M = ps2M.executeQuery();
//                item = 0;
//                while(rs2M.next()) {
//                    item += rs2M.getDouble("biaya");
//                    ttl += rs2M.getDouble("biaya");
//                    tabMode3.addRow(new Object[]{"","",rs2M.getString("kd_jenis_prw")+" "+rs2M.getString("nm_perawatan")+" "+Valid.SetAngka(rs2M.getDouble("biaya")),"","","","",""});
//
//                    ps3M = koneksi.prepareStatement(
//                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan, detail_periksa_lab.kd_jenis_prw " +
//                        "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template " +
//                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? " +
//                        "order by template_laboratorium.urut"
//                    );
//
//                    ps3M.setString(1, rsM.getString("no_rawat"));
//                    ps3M.setString(2, rs2M.getString("kd_jenis_prw"));
//                    ps3M.setString(3, rsM.getString("tgl_periksa"));
//                    ps3M.setString(4, rsM.getString("jam"));
//                    rs3M = ps3M.executeQuery();
//                    while(rs3M.next()) {
//                        item += rs3M.getDouble("biaya_item");
//                        ttl += rs3M.getDouble("biaya_item");
//                        tabMode3.addRow(new Object[]{"","","  "+rs3M.getString("Pemeriksaan")+" "+Valid.SetAngka(rs3M.getDouble("biaya_item")),
//                            rs3M.getString("nilai"),
//                            rs3M.getString("satuan"),
//                            rs3M.getString("nilai_rujukan"),
//                            rs3M.getString("keterangan"),
//                            ""
//                        });
//                    }
//                    rs3M.close(); ps3M.close();
//                }
//                rs2M.close(); ps2M.close();
//
//                if(item > 0) {
//                    tabMode3.addRow(new Object[]{"","","Biaya Periksa : "+Valid.SetAngka(item),"","","",""});
//                }
//            }
//            rsM.close(); psM.close();
//
//            if(ttl > 0) {
//                tabMode3.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","","",""});
//            }
//
//        } catch (Exception e) {
//            System.out.println("Notif psM : " + e);
//        }
//    } catch (Exception ex) {
//        System.out.println(ex);
//    }
//}
    
private void tampil4() {
//    try {
//        Valid.tabelKosong(tabMode3);
//        if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        } else {
//            psM = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? " +
//                "and (pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%cito%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        }
//
//        try {
//            if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//            } else {
//                psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                psM.setString(3, "%"+NoRawat.getText()+"%");
//                psM.setString(4, "%"+kdmem.getText()+"%");
//                psM.setString(5, "%"+kdptg.getText()+"%");
//                psM.setString(6, "%"+TCari.getText().trim()+"%");
//                psM.setString(7, "%"+TCari.getText().trim()+"%");
//                psM.setString(8, "%"+TCari.getText().trim()+"%");
//                psM.setString(9, "%"+TCari.getText().trim()+"%");
//            }
//
//            rsM = psM.executeQuery();
//            ttl = 0;
//            while(rsM.next()) {
//                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rsM.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                if(!kamar.equals("")) {
//                    namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
//                    kamar = "Kamar";
//                } else {
//                    kamar = "Poli";
//                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rsM.getString("no_rawat")+"'");
//                }
//
////                // Ambil pelapor, penerima, dan waktu lapor sebelum ditampilkan
////                String pelapor = "", penerima = "", waktu = "", tat = "";                
////                ps5M = koneksi.prepareStatement("select pelapor, penerima, waktu from rsaj_monev_cito where no_rawat=? and tgl_periksa=? and jam=?");
////                ps5M.setString(1, rsM.getString("no_rawat"));
////                ps5M.setString(2, rsM.getString("tgl_periksa"));
////                ps5M.setString(3, rsM.getString("jam"));
////                rs5M = ps5M.executeQuery();
////                if(rs5M.next()) {
////                    pelapor = rs5M.getString("pelapor");
////                    penerima = rs5M.getString("penerima");
////                    waktu = rs5M.getString("waktu");
////                    tat = rs5M.getString(" SEMENTARA DIBUATKAN QUERYNYA ");
////                }
//
//                // Ambil pelapor, penerima, dan waktu lapor sebelum ditampilkan
//                String pelaporcito = "", penerimacito = "", jamregis = "", jamlaporcito = "", hasilcito = "", tatcito = "";                
//                ps5M = koneksi.prepareStatement(
//                    "SELECT " +
//                    "    pelapor, " +
//                    "    penerima, " +
//                    "    jam_regis, " +
//                    "    jam_lapor, " +
//                    "    hasilcito, " +
//                    "    CASE " +
//                    "        WHEN TIMESTAMPDIFF(SECOND, jam_regis, jam_lapor) < 60 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(SECOND, jam_regis, jam_lapor), ' detik') " +
//                    "        WHEN TIMESTAMPDIFF(MINUTE, jam_regis, jam_lapor) < 60 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(MINUTE, jam_regis, jam_lapor), ' menit') " +
//                    "        WHEN TIMESTAMPDIFF(HOUR, jam_regis, jam_lapor) < 24 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(HOUR, jam_regis, jam_lapor), ' jam') " +
//                    "        ELSE " +
//                    "            CONCAT(TIMESTAMPDIFF(DAY, jam_regis, jam_lapor), ' hari') " +
//                    "    END AS tatcito " +
//                    "FROM rsaj_monev_cito " +
//                    "WHERE no_rawat = ? " +
//                    "  AND tgl_periksa = ? " +
//                    "  AND jam = ?"
//                );
//
//                // Parameter untuk waktu awal (periksa_lab)
////                ps5M.setString(1, rsM.getString("tgl_periksa")); // periksa_lab.tgl_periksa
////                ps5M.setString(2, rsM.getString("jam"));         // periksa_lab.jam
//
//                // Parameter untuk WHERE clause
//                ps5M.setString(1, rsM.getString("no_rawat"));
//                ps5M.setString(2, rsM.getString("tgl_periksa"));
//                ps5M.setString(3, rsM.getString("jam"));
//
//                rs5M = ps5M.executeQuery();
//                if (rs5M.next()) {
//                    pelaporcito = rs5M.getString("pelapor");
//                    penerimacito = rs5M.getString("penerima");
//                    jamregis = rs5M.getString("jam_regis");
//                    jamlaporcito = rs5M.getString("jam_lapor");
//                    hasilcito = rs5M.getString("hasilcito");
//                    tatcito = rs5M.getString("tatcito") + " menit";
//                }
//                
//                rs5M.close(); ps5M.close();
//
//                tabMode3.addRow(new Object[]{
//                    rsM.getString("no_rawat"),
//                    rsM.getString("no_rkm_medis")+" "+rsM.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",
//                    rsM.getString("nama"),
//                    rsM.getString("tgl_periksa"),
//                    rsM.getString("jam"),
//                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rsM.getString("dokter_perujuk")),
//                    rsM.getString("nm_dokter"),
//                    rsM.getString("informasi_tambahan"),
//                    "Pelapor : "+pelaporcito                    
//                });
//
//                tabMode3.addRow(new Object[]{"","Cara Bayar : "+rsM.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan","","Penerima : "+penerimacito});
//                               
//
//                ps2M = koneksi.prepareStatement(
//                    "select jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
//                    "from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
//                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? " +
//                    "order by jns_perawatan_lab.kd_jenis_prw"
//                );
//                ps2M.setString(1, rsM.getString("no_rawat"));
//                ps2M.setString(2, rsM.getString("tgl_periksa"));
//                ps2M.setString(3, rsM.getString("jam"));
//                rs2M = ps2M.executeQuery();
//                item = 0;
//                while(rs2M.next()) {
//                    item += rs2M.getDouble("biaya");
//                    ttl += rs2M.getDouble("biaya");
//                    tabMode3.addRow(new Object[]{"","",rs2M.getString("kd_jenis_prw")+" "+rs2M.getString("nm_perawatan")+" "+Valid.SetAngka(rs2M.getDouble("biaya")),"","","","",""});
//                                        
//                    ps3M = koneksi.prepareStatement(
//                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan, detail_periksa_lab.kd_jenis_prw " +
//                        "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template " +
//                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? " +
//                        "order by template_laboratorium.urut"
//                    );
//                    ps3M.setString(1, rsM.getString("no_rawat"));
//                    ps3M.setString(2, rs2M.getString("kd_jenis_prw"));
//                    ps3M.setString(3, rsM.getString("tgl_periksa"));
//                    ps3M.setString(4, rsM.getString("jam"));
//                    rs3M = ps3M.executeQuery();
//                    while(rs3M.next()) {
//                        item += rs3M.getDouble("biaya_item");
//                        ttl += rs3M.getDouble("biaya_item");
//                        tabMode3.addRow(new Object[]{"","","  "+rs3M.getString("Pemeriksaan")+" "+Valid.SetAngka(rs3M.getDouble("biaya_item")),
//                            rs3M.getString("nilai"),
//                            rs3M.getString("satuan"),
//                            rs3M.getString("nilai_rujukan"),
//                            rs3M.getString("keterangan"),
//                            ""
//                        });
//                        
//                    }
//                    rs3M.close(); ps3M.close();
//                }
//                rs2M.close(); ps2M.close();
//
//                if(item > 0) {
//                    tabMode3.addRow(new Object[]{"","","Biaya Periksa : "+Valid.SetAngka(item),"","","","","Jam Regis : "+jamregis,"Hasil Cito : "+hasilcito});
//                    tabMode3.addRow(new Object[]{"","","","","","","","Jam Lapor : "+jamlaporcito,"TAT < 60 Menit : "+tatcito}); 
//                    tabMode3.addRow(new Object[]{"","","","","","","","",""});
//                }
//            }
//            rsM.close(); psM.close();
//
//            if(ttl > 0) {
//                tabMode3.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","","",""});
//            }
//
//        } catch (Exception e) {
//            System.out.println("Notif psM : " + e);
//        }
//    } catch (Exception ex) {
//        System.out.println(ex);
//    }

    Valid.tabelKosong(tabMode3);
    ttl = 0;

    String sqlM;
    boolean pakaiFilter = !(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals(""));

    if (pakaiFilter) {
        sqlM = "SELECT periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
               "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
               "FROM periksa_lab " +
               "INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat " +
               "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
               "INNER JOIN petugas ON periksa_lab.nip = petugas.nip " +
               "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj " +
               "INNER JOIN dokter ON periksa_lab.kd_dokter = dokter.kd_dokter " +
               "LEFT JOIN permintaan_lab ON periksa_lab.no_rawat = permintaan_lab.no_rawat " +
               "WHERE periksa_lab.kategori = 'PK' AND periksa_lab.tgl_periksa BETWEEN ? AND ? " +
               "AND periksa_lab.no_rawat LIKE ? AND reg_periksa.no_rkm_medis LIKE ? AND petugas.nip LIKE ? " +
               "AND (pasien.nm_pasien LIKE ? OR petugas.nama LIKE ? OR reg_periksa.no_rkm_medis LIKE ? OR penjab.png_jawab LIKE ?) " +
               "AND LOWER(IFNULL(permintaan_lab.informasi_tambahan, '')) LIKE '%cito%' " +
               "GROUP BY CONCAT(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
               "ORDER BY periksa_lab.tgl_periksa DESC, periksa_lab.jam DESC";
    } else {
        sqlM = "SELECT periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
               "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
               "FROM periksa_lab " +
               "INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat " +
               "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
               "INNER JOIN petugas ON periksa_lab.nip = petugas.nip " +
               "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj " +
               "INNER JOIN dokter ON periksa_lab.kd_dokter = dokter.kd_dokter " +
               "LEFT JOIN permintaan_lab ON periksa_lab.no_rawat = permintaan_lab.no_rawat " +
               "WHERE periksa_lab.kategori = 'PK' AND periksa_lab.tgl_periksa BETWEEN ? AND ? " +
               "AND LOWER(IFNULL(permintaan_lab.informasi_tambahan, '')) LIKE '%cito%' " +
               "GROUP BY CONCAT(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
               "ORDER BY periksa_lab.tgl_periksa DESC, periksa_lab.jam DESC";
    }

    try (PreparedStatement psM = koneksi.prepareStatement(sqlM)) {
        psM.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
        psM.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));

        if (pakaiFilter) {
            psM.setString(3, "%" + NoRawat.getText() + "%");
            psM.setString(4, "%" + kdmem.getText() + "%");
            psM.setString(5, "%" + kdptg.getText() + "%");
            for (int i = 6; i <= 9; i++) {
                psM.setString(i, "%" + TCari.getText().trim() + "%");
            }
        }

        try (ResultSet rsM = psM.executeQuery()) {
            while (rsM.next()) {
                String noRawat = rsM.getString("no_rawat");
                String tglPeriksa = rsM.getString("tgl_periksa");
                String jam = rsM.getString("jam");

                // Ambil kamar atau poli
                kamar = Sequel.cariIsi("SELECT IFNULL(kamar_inap.kd_kamar,'') FROM kamar_inap WHERE no_rawat=? ORDER BY tgl_masuk DESC LIMIT 1", noRawat);
                if (!kamar.equals("")) {
                    namakamar = kamar + ", " + Sequel.cariIsi("SELECT bangsal.nm_bangsal FROM bangsal INNER JOIN kamar ON bangsal.kd_bangsal = kamar.kd_bangsal WHERE kamar.kd_kamar=?", kamar);
                    kamar = "Kamar";
                } else {
                    kamar = "Poli";
                    namakamar = Sequel.cariIsi("SELECT poliklinik.nm_poli FROM poliklinik INNER JOIN reg_periksa ON poliklinik.kd_poli = reg_periksa.kd_poli WHERE reg_periksa.no_rawat=?", noRawat);
                }

                // Ambil data dari rsaj_monev_cito
                String pelapor = "", penerima = "", jamregis = "", jamlapor = "", hasilcito = "", tat = "";
                try (PreparedStatement psCito = koneksi.prepareStatement(
                        "SELECT pelapor, penerima, jam_regis, jam_lapor, hasilcito, " +
                        "CASE " +
                        "    WHEN TIMESTAMPDIFF(SECOND, jam_regis, jam_lapor) < 60 THEN CONCAT(TIMESTAMPDIFF(SECOND, jam_regis, jam_lapor), ' detik') " +
                        "    WHEN TIMESTAMPDIFF(MINUTE, jam_regis, jam_lapor) < 60 THEN CONCAT(TIMESTAMPDIFF(MINUTE, jam_regis, jam_lapor), ' menit') " +
                        "    WHEN TIMESTAMPDIFF(HOUR, jam_regis, jam_lapor) < 24 THEN CONCAT(TIMESTAMPDIFF(HOUR, jam_regis, jam_lapor), ' jam') " +
                        "    ELSE CONCAT(TIMESTAMPDIFF(DAY, jam_regis, jam_lapor), ' hari') " +
                        "END AS tat " +
                        "FROM rsaj_monev_cito WHERE no_rawat = ? AND tgl_periksa = ? AND jam = ?")) {
                    psCito.setString(1, noRawat);
                    psCito.setString(2, tglPeriksa);
                    psCito.setString(3, jam);
                    try (ResultSet rsCito = psCito.executeQuery()) {
                        if (rsCito.next()) {
                            pelapor = rsCito.getString("pelapor");
                            penerima = rsCito.getString("penerima");
                            jamregis = rsCito.getString("jam_regis");
                            jamlapor = rsCito.getString("jam_lapor");
                            hasilcito = rsCito.getString("hasilcito");
                            tat = rsCito.getString("tat");
                        }
                    }
                }

                // Tampilkan baris utama
                tabMode3.addRow(new Object[]{
                    noRawat,
                    rsM.getString("no_rkm_medis") + " " + rsM.getString("nm_pasien") + " (" + kamar + " : " + namakamar + ")",
                    rsM.getString("nama"),
                    tglPeriksa,
                    jam,
                    Sequel.cariIsi("SELECT dokter.nm_dokter FROM dokter WHERE kd_dokter=?", rsM.getString("dokter_perujuk")),
                    rsM.getString("nm_dokter"),
                    rsM.getString("informasi_tambahan"),
                    "Pelapor : " + pelapor
                });

                tabMode3.addRow(new Object[]{"", "Cara Bayar : " + rsM.getString("png_jawab"), "Pemeriksaan", "Hasil", "Satuan", "Nilai Rujukan", "Keterangan", "", "Penerima : " + penerima});

                // Ambil pemeriksaan lab dan detail
                try (PreparedStatement psJenis = koneksi.prepareStatement(
                        "SELECT jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
                        "FROM periksa_lab INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw " +
                        "WHERE periksa_lab.kategori = 'PK' AND periksa_lab.no_rawat = ? AND periksa_lab.tgl_periksa = ? AND periksa_lab.jam = ?")) {
                    psJenis.setString(1, noRawat);
                    psJenis.setString(2, tglPeriksa);
                    psJenis.setString(3, jam);

                    try (ResultSet rsJenis = psJenis.executeQuery()) {
                        while (rsJenis.next()) {
                            double item = rsJenis.getDouble("biaya");
                            ttl += item;

                            tabMode3.addRow(new Object[]{"", "", rsJenis.getString("kd_jenis_prw") + " " + rsJenis.getString("nm_perawatan") + " " + Valid.SetAngka(item), "", "", "", "", ""});

                            try (PreparedStatement psDetail = koneksi.prepareStatement(
                                    "SELECT template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, " +
                                    "detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan " +
                                    "FROM detail_periksa_lab INNER JOIN template_laboratorium ON detail_periksa_lab.id_template = template_laboratorium.id_template " +
                                    "WHERE detail_periksa_lab.no_rawat = ? AND detail_periksa_lab.kd_jenis_prw = ? AND detail_periksa_lab.tgl_periksa = ? AND detail_periksa_lab.jam = ?")) {
                                psDetail.setString(1, noRawat);
                                psDetail.setString(2, rsJenis.getString("kd_jenis_prw"));
                                psDetail.setString(3, tglPeriksa);
                                psDetail.setString(4, jam);

                                try (ResultSet rsDetail = psDetail.executeQuery()) {
                                    while (rsDetail.next()) {
                                        double biayaItem = rsDetail.getDouble("biaya_item");
                                        ttl += biayaItem;
                                        item += biayaItem;

                                        tabMode3.addRow(new Object[]{
                                            "", "", "  " + rsDetail.getString("Pemeriksaan") + " " + Valid.SetAngka(biayaItem),
                                            rsDetail.getString("nilai"),
                                            rsDetail.getString("satuan"),
                                            rsDetail.getString("nilai_rujukan"),
                                            rsDetail.getString("keterangan"),
                                            ""
                                        });
                                    }
                                }
                            }

                            // Tampilkan biaya item
                            if (item > 0) {
                                tabMode3.addRow(new Object[]{"", "", "Biaya Periksa : " + Valid.SetAngka(item), "", "", "", "", "Jam Regis : " + jamregis, "Hasil Cito : " + hasilcito});
                                tabMode3.addRow(new Object[]{"", "", "", "", "", "", "", "Jam Lapor : " + jamlapor, "TAT : " + tat});
                                tabMode3.addRow(new Object[]{"", "", "", "", "", "", "", "", ""});
                            }
                        }
                    }
                }
            }

            if (ttl > 0) {
                tabMode3.addRow(new Object[]{">>", "Total : " + Valid.SetAngka(ttl), "", "", "", "", "", ""});
            }
        }
    } catch (Exception ex) {
        System.out.println("Notif tampil4: " + ex);
    }
}

private void tampil5() {
//    try {
//        Valid.tabelKosong(tabMode4);
//        if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//            psK = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        } else {
//            psK = koneksi.prepareStatement(
//                "select periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
//                "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, permintaan_lab.informasi_tambahan " +
//                "from periksa_lab " +
//                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
//                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                "inner join petugas on periksa_lab.nip=petugas.nip " +
//                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
//                "left join permintaan_lab on periksa_lab.no_rawat=permintaan_lab.no_rawat " +
//                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa between ? and ? " +
//                "and periksa_lab.no_rawat like ? and reg_periksa.no_rkm_medis like ? and petugas.nip like ? " +
//                "and (pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ?) " +
//                "and lower(ifnull(permintaan_lab.informasi_tambahan,'')) like '%%' " +
//                "group by concat(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
//                "order by periksa_lab.tgl_periksa desc, periksa_lab.jam desc"
//            );
//        }
//
//        try {
//            if(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals("")) {
//                psK.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psK.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//            } else {
//                psK.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                psK.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                psK.setString(3, "%"+NoRawat.getText()+"%");
//                psK.setString(4, "%"+kdmem.getText()+"%");
//                psK.setString(5, "%"+kdptg.getText()+"%");
//                psK.setString(6, "%"+TCari.getText().trim()+"%");
//                psK.setString(7, "%"+TCari.getText().trim()+"%");
//                psK.setString(8, "%"+TCari.getText().trim()+"%");
//                psK.setString(9, "%"+TCari.getText().trim()+"%");
//            }
//
//            rsK = psK.executeQuery();
//            ttl = 0;
//            while(rsK.next()) {
//                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rsK.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
//                if(!kamar.equals("")) {
//                    namakamar = kamar + ", " + Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
//                    kamar = "Kamar";
//                } else {
//                    kamar = "Poli";
//                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rsK.getString("no_rawat")+"'");
//                }
//
////                // Ambil pelapor, penerima, dan waktu lapor sebelum ditampilkan
////                String pelapor = "", penerima = "", waktu = "", hasilkritis = "";
////                ps5K = koneksi.prepareStatement("select pelapor, penerima, waktu, hasilkritis from rsaj_monev_kritis where no_rawat=? and tgl_periksa=? and jam=?");
////                ps5K.setString(1, rsK.getString("no_rawat"));
////                ps5K.setString(2, rsK.getString("tgl_periksa"));
////                ps5K.setString(3, rsK.getString("jam"));
////                rs5K = ps5K.executeQuery();
////                if(rs5K.next()) {
////                    pelapor = rs5K.getString("pelapor");
////                    penerima = rs5K.getString("penerima");
////                    waktu = rs5K.getString("waktu");
////                    hasilkritis = rs5K.getString("hasilkritis");
////                }
//                
//                // Ambil pelapor, penerima, dan waktu lapor sebelum ditampilkan
//                String pelaporkritis = "", penerimakritis = "", jamvalidasi = "", jamlaporkritis = "", hasilkritis = "", tatkritis = "";                
//                ps5K = koneksi.prepareStatement(
//                    "SELECT " +
//                    "    pelapor, " +
//                    "    penerima, " +
//                    "    jam_validasi, " +
//                    "    jam_lapor, " +
//                    "    hasilkritis, " +
//                    "    CASE " +
//                    "        WHEN TIMESTAMPDIFF(SECOND, jam_validasi, jam_lapor) < 60 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(SECOND, jam_validasi, jam_lapor), ' detik') " +
//                    "        WHEN TIMESTAMPDIFF(MINUTE, jam_validasi, jam_lapor) < 60 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(MINUTE, jam_validasi, jam_lapor), ' menit') " +
//                    "        WHEN TIMESTAMPDIFF(HOUR, jam_validasi, jam_lapor) < 24 THEN " +
//                    "            CONCAT(TIMESTAMPDIFF(HOUR, jam_validasi, jam_lapor), ' jam') " +
//                    "        ELSE " +
//                    "            CONCAT(TIMESTAMPDIFF(DAY, jam_validasi, jam_lapor), ' hari') " +
//                    "    END AS tatkritis " +
//                    "FROM rsaj_monev_kritis " +
//                    "WHERE no_rawat = ? " +
//                    "  AND tgl_periksa = ? " +
//                    "  AND jam = ?"
//                );
//
////                // Parameter untuk waktu awal (periksa_lab)
////                ps5K.setString(1, rsK.getString("tgl_periksa")); // periksa_lab.tgl_periksa
////                ps5K.setString(2, rsK.getString("jam_lapor"));         // periksa_lab.jam
//
//                // Parameter untuk WHERE clause
//                ps5K.setString(1, rsK.getString("no_rawat"));
//                ps5K.setString(2, rsK.getString("tgl_periksa"));
//                ps5K.setString(3, rsK.getString("jam"));
//
//                rs5K = ps5K.executeQuery();
//                if (rs5K.next()) {
//                    pelaporkritis = rs5K.getString("pelapor");
//                    penerimakritis = rs5K.getString("penerima");
//                    jamvalidasi = rs5K.getString("jam_validasi");
//                    jamlaporkritis = rs5K.getString("jam_lapor");
//                    hasilkritis = rs5K.getString("hasilkritis");
//                    tatkritis = rs5K.getString("tatkritis") + " menit";
//                }
//                
//                rs5K.close(); ps5K.close();
//
//                tabMode4.addRow(new Object[]{
//                    rsK.getString("no_rawat"),
//                    rsK.getString("no_rkm_medis")+" "+rsK.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",
//                    rsK.getString("nama"),
//                    rsK.getString("tgl_periksa"),
//                    rsK.getString("jam"),
//                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rsK.getString("dokter_perujuk")),
//                    rsK.getString("nm_dokter"),
//                    rsK.getString("informasi_tambahan"),
//                    "Pelapor : "+pelaporkritis                    
//                });
//
//                tabMode4.addRow(new Object[]{"","Cara Bayar : "+rsK.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan","","Penerima : "+penerimakritis});
////                tabMode4.addRow(new Object[]{"","","","","","","","",""});
//
//                ps2K = koneksi.prepareStatement(
//                    "select jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
//                    "from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
//                    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? " +
//                    "order by jns_perawatan_lab.kd_jenis_prw"
//                );
//                
//                ps2K.setString(1, rsK.getString("no_rawat"));
//                ps2K.setString(2, rsK.getString("tgl_periksa"));
//                ps2K.setString(3, rsK.getString("jam"));
//                rs2K = ps2K.executeQuery();
//                item = 0;
//                while(rs2K.next()) {
//                    item += rs2K.getDouble("biaya");
//                    ttl += rs2K.getDouble("biaya");
//                    tabMode4.addRow(new Object[]{"","",rs2K.getString("kd_jenis_prw")+" "+rs2K.getString("nm_perawatan")+" "+Valid.SetAngka(rs2K.getDouble("biaya")),"","","","",""});
//
//                    ps3K = koneksi.prepareStatement(
//                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan, detail_periksa_lab.kd_jenis_prw " +
//                        "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template " +
//                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? " +
//                        "order by template_laboratorium.urut"
//                    );
//                    ps3K.setString(1, rsK.getString("no_rawat"));
//                    ps3K.setString(2, rs2K.getString("kd_jenis_prw"));
//                    ps3K.setString(3, rsK.getString("tgl_periksa"));
//                    ps3K.setString(4, rsK.getString("jam"));
//                    rs3K = ps3K.executeQuery();
//                    while(rs3K.next()) {
//                        item += rs3K.getDouble("biaya_item");
//                        ttl += rs3K.getDouble("biaya_item");
//                        tabMode4.addRow(new Object[]{"","","  "+rs3K.getString("Pemeriksaan")+" "+Valid.SetAngka(rs3K.getDouble("biaya_item")),
//                            rs3K.getString("nilai"),
//                            rs3K.getString("satuan"),
//                            rs3K.getString("nilai_rujukan"),
//                            rs3K.getString("keterangan"),
//                            ""
//                        });
//                    }
//                    rs3K.close(); ps3K.close();
//                }
//                rs2K.close(); ps2K.close();
//
//                if(item > 0) {
//                    tabMode4.addRow(new Object[]{"","","Biaya Periksa : "+Valid.SetAngka(item),"","","","","Jam Validasi : "+jamvalidasi,"Hasil Kritis : "+hasilkritis});
//                    tabMode4.addRow(new Object[]{"","","","","","","","Jam Lapor : "+jamlaporkritis,"TAT < 30 Menit : "+tatkritis});                
//                    tabMode4.addRow(new Object[]{"","","","","","","","",""});                
//                }
//            }
//            rsK.close(); psK.close();
//
//            if(ttl > 0) {
//                tabMode4.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","","",""});
//            }
//
//        } catch (Exception e) {
//            System.out.println("Notif psK : " + e);
//        }
//    } catch (Exception ex) {
//        System.out.println(ex);
//    }

    Valid.tabelKosong(tabMode4);
    ttl = 0;
    
    String baseQuery =
        "SELECT periksa_lab.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, petugas.nama, periksa_lab.tgl_periksa, periksa_lab.jam, " +
        "periksa_lab.dokter_perujuk, periksa_lab.kd_dokter, dokter.nm_dokter, penjab.png_jawab, IFNULL(permintaan_lab.informasi_tambahan,'') AS informasi_tambahan " +
        "FROM periksa_lab " +
        "INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat " +
        "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
        "INNER JOIN petugas ON periksa_lab.nip = petugas.nip " +
        "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj " +
        "INNER JOIN dokter ON periksa_lab.kd_dokter = dokter.kd_dokter " +
        "LEFT JOIN permintaan_lab ON periksa_lab.no_rawat = permintaan_lab.no_rawat " +
        "WHERE periksa_lab.kategori='PK' AND periksa_lab.tgl_periksa BETWEEN ? AND ? ";

    boolean adaFilter = !(NoRawat.getText().equals("") && kdmem.getText().equals("") && kdptg.getText().equals("") && TCari.getText().equals(""));
    if (adaFilter) {
        baseQuery +=
            "AND periksa_lab.no_rawat LIKE ? AND reg_periksa.no_rkm_medis LIKE ? AND petugas.nip LIKE ? " +
            "AND (pasien.nm_pasien LIKE ? OR petugas.nama LIKE ? OR reg_periksa.no_rkm_medis LIKE ? OR penjab.png_jawab LIKE ?) ";
    }

    baseQuery +=
        "GROUP BY CONCAT(periksa_lab.no_rawat, periksa_lab.tgl_periksa, periksa_lab.jam) " +
        "ORDER BY periksa_lab.tgl_periksa DESC, periksa_lab.jam DESC";

    try (PreparedStatement ps = koneksi.prepareStatement(baseQuery)) {
        ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
        ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));

        if (adaFilter) {
            ps.setString(3, "%" + NoRawat.getText() + "%");
            ps.setString(4, "%" + kdmem.getText() + "%");
            ps.setString(5, "%" + kdptg.getText() + "%");
            for (int i = 6; i <= 9; i++) {
                ps.setString(i, "%" + TCari.getText().trim() + "%");
            }
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String noRawat = rs.getString("no_rawat");
                String tglPeriksa = rs.getString("tgl_periksa");
                String jam = rs.getString("jam");

                // Kamar atau Poli
                String kamar = Sequel.cariIsi("SELECT IFNULL(kd_kamar,'') FROM kamar_inap WHERE no_rawat=? ORDER BY tgl_masuk DESC LIMIT 1", noRawat);
                String lokasi = kamar.isEmpty() ? "Poli" : "Kamar";
                String namaKamar = kamar.isEmpty()
                    ? Sequel.cariIsi("SELECT poliklinik.nm_poli FROM poliklinik INNER JOIN reg_periksa ON poliklinik.kd_poli=reg_periksa.kd_poli WHERE reg_periksa.no_rawat=?", noRawat)
                    : kamar + ", " + Sequel.cariIsi("SELECT bangsal.nm_bangsal FROM bangsal INNER JOIN kamar ON bangsal.kd_bangsal=kamar.kd_bangsal WHERE kamar.kd_kamar=?", kamar);

                // Data Monev Kritis
                String pelapor = "", penerima = "", jamValidasi = "", jamLapor = "", hasilKritis = "", tatKritis = "";
                try (PreparedStatement psKritis = koneksi.prepareStatement(
                        "SELECT pelapor, penerima, jam_validasi, jam_lapor, hasilkritis, " +
                        "CASE " +
                        " WHEN TIMESTAMPDIFF(SECOND, jam_validasi, jam_lapor) < 60 THEN CONCAT(TIMESTAMPDIFF(SECOND, jam_validasi, jam_lapor), ' detik') " +
                        " WHEN TIMESTAMPDIFF(MINUTE, jam_validasi, jam_lapor) < 60 THEN CONCAT(TIMESTAMPDIFF(MINUTE, jam_validasi, jam_lapor), ' menit') " +
                        " WHEN TIMESTAMPDIFF(HOUR, jam_validasi, jam_lapor) < 24 THEN CONCAT(TIMESTAMPDIFF(HOUR, jam_validasi, jam_lapor), ' jam') " +
                        " ELSE CONCAT(TIMESTAMPDIFF(DAY, jam_validasi, jam_lapor), ' hari') END AS tatkritis " +
                        "FROM rsaj_monev_kritis WHERE no_rawat=? AND tgl_periksa=? AND jam=?"
                    )) {
                    psKritis.setString(1, noRawat);
                    psKritis.setString(2, tglPeriksa);
                    psKritis.setString(3, jam);
                    try (ResultSet rsKritis = psKritis.executeQuery()) {
                        if (rsKritis.next()) {
                            pelapor = rsKritis.getString("pelapor");
                            penerima = rsKritis.getString("penerima");
                            jamValidasi = rsKritis.getString("jam_validasi");
                            jamLapor = rsKritis.getString("jam_lapor");
                            hasilKritis = rsKritis.getString("hasilkritis");
                            tatKritis = rsKritis.getString("tatkritis");
                        }
                    }
                }

                // Header
                tabMode4.addRow(new Object[]{
                    noRawat,
                    rs.getString("no_rkm_medis") + " " + rs.getString("nm_pasien") + " (" + lokasi + " : " + namaKamar + ")",
                    rs.getString("nama"),
                    tglPeriksa,
                    jam,
                    Sequel.cariIsi("SELECT dokter.nm_dokter FROM dokter WHERE kd_dokter=?", rs.getString("dokter_perujuk")),
                    rs.getString("nm_dokter"),
                    rs.getString("informasi_tambahan"),
                    "Pelapor : " + pelapor
                });

                tabMode4.addRow(new Object[]{"","Cara Bayar : " + rs.getString("png_jawab"),"Pemeriksaan","Hasil","Satuan","Nilai Rujukan","Keterangan","","Penerima : " + penerima});

                // Pemeriksaan & Detail
                double item = tampilDetailLab(noRawat, tglPeriksa, jam);
                ttl += item;

                if (item > 0) {
                    tabMode4.addRow(new Object[]{"","","Biaya Periksa : " + Valid.SetAngka(item),"","","","","Jam Validasi : " + jamValidasi,"Hasil Kritis : " + hasilKritis});
                    tabMode4.addRow(new Object[]{"","","","","","","","Jam Lapor : " + jamLapor,"TAT < 30 Menit : " + tatKritis});
                    tabMode4.addRow(new Object[]{"","","","","","","","",""});
                }
            }

            if (ttl > 0) {
                tabMode4.addRow(new Object[]{">>", "Total : " + Valid.SetAngka(ttl), "", "", "", "", "", ""});
            }

        }

    } catch (Exception e) {
        System.out.println("Notif tampil5() : " + e);
    }
}

private double tampilDetailLab(String noRawat, String tglPeriksa, String jam) throws SQLException {
    double subtotal = 0;

    try (PreparedStatement ps2 = koneksi.prepareStatement(
            "SELECT jns_perawatan_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, periksa_lab.biaya " +
            "FROM periksa_lab " +
            "INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw " +
            "WHERE periksa_lab.kategori='PK' AND periksa_lab.no_rawat=? AND periksa_lab.tgl_periksa=? AND periksa_lab.jam=? " +
            "ORDER BY jns_perawatan_lab.kd_jenis_prw"
        )) {
        ps2.setString(1, noRawat);
        ps2.setString(2, tglPeriksa);
        ps2.setString(3, jam);

        try (ResultSet rs2 = ps2.executeQuery()) {
            while (rs2.next()) {
                double biaya = rs2.getDouble("biaya");
                subtotal += biaya;

                tabMode4.addRow(new Object[]{"", "", rs2.getString("kd_jenis_prw") + " " + rs2.getString("nm_perawatan") + " " + Valid.SetAngka(biaya), "", "", "", "", ""});

                try (PreparedStatement ps3 = koneksi.prepareStatement(
                        "SELECT template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai, template_laboratorium.satuan, detail_periksa_lab.nilai_rujukan, detail_periksa_lab.biaya_item, detail_periksa_lab.keterangan " +
                        "FROM detail_periksa_lab " +
                        "INNER JOIN template_laboratorium ON detail_periksa_lab.id_template = template_laboratorium.id_template " +
                        "WHERE detail_periksa_lab.no_rawat=? AND detail_periksa_lab.kd_jenis_prw=? AND detail_periksa_lab.tgl_periksa=? AND detail_periksa_lab.jam=? " +
                        "ORDER BY template_laboratorium.urut"
                    )) {
                    ps3.setString(1, noRawat);
                    ps3.setString(2, rs2.getString("kd_jenis_prw"));
                    ps3.setString(3, tglPeriksa);
                    ps3.setString(4, jam);

                    try (ResultSet rs3 = ps3.executeQuery()) {
                        while (rs3.next()) {
                            double biayaItem = rs3.getDouble("biaya_item");
                            subtotal += biayaItem;
                            tabMode4.addRow(new Object[]{"", "", "  " + rs3.getString("Pemeriksaan") + " " + Valid.SetAngka(biayaItem),
                                rs3.getString("nilai"),
                                rs3.getString("satuan"),
                                rs3.getString("nilai_rujukan"),
                                rs3.getString("keterangan"),
                                ""
                            });
                        }
                    }
                }
            }
        }
    }

    return subtotal;
}

    
    public void SetNoRw(String norw){
        NoRawat.setText(norw);
        tampil();
        Sequel.cariIsi("select reg_periksa.tgl_registrasi from reg_periksa where reg_periksa.no_rawat='"+norw+"'", Tgl1);
    }
    
    private void getData() {
        
//    Kd2.setText("");
//    Kd3.setText("");
//    Kd4.setText("");
//    NoRawat.setText(""); // reset dulu
//
//        // Set Kd2
//        if(tbDokter.getSelectedRow() != -1){
//            Kd2.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
//        }
//
//        // Set Kd3
//        if(tbDokter1.getSelectedRow() != -1){
//            Kd3.setText(tbDokter1.getValueAt(tbDokter1.getSelectedRow(), 0).toString());
//        }
//
//        // Set Kd4
//        if(tbDokter3.getSelectedRow() != -1){
//            Kd4.setText(tbDokter3.getValueAt(tbDokter3.getSelectedRow(), 0).toString());
//        }
//
//        // Set NoRawat tergantung mana yang terisi
//        if(!Kd2.getText().equals("")){
//            NoRawat.setText(Kd2.getText());
//        } else if(!Kd3.getText().equals("")){
//            NoRawat.setText(Kd3.getText());
//        } else if(!Kd4.getText().equals("")){
//            NoRawat.setText(Kd4.getText());
//        }

    if (sedangGantiTab) {
        return; // kalau sedang pindah tab, jangan set NoRawat
    }

    Kd2.setText("");
    if(tbDokter.getSelectedRow()!= -1){
        Kd2.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
    }
    
    Kd3.setText("");
    if(tbDokter1.getSelectedRow()!= -1){
        Kd3.setText(tbDokter1.getValueAt(tbDokter1.getSelectedRow(),0).toString());
    }
    
    Kd4.setText("");
    if(tbDokter3.getSelectedRow()!= -1){
        Kd4.setText(tbDokter3.getValueAt(tbDokter3.getSelectedRow(),0).toString());
    }

    if(!Kd2.getText().equals("")){
        NoRawat.setText(Kd2.getText());
    } else if(!Kd3.getText().equals("")){
        NoRawat.setText(Kd3.getText());
    } else if(!Kd4.getText().equals("")){
        NoRawat.setText(Kd4.getText());
    }
    
    }
    
    public void isCek(){
        
    List<String> allowedUsers = Arrays.asList(
    "19941124", "19951241", "20001426", "20011470", "20051588", "20051589",
    "20081657", "20081658", "20091689", "20101782", "20111815",
    "K211911", "K211912", "K231961", "K231962", "K231963",
    "K242002", "K242003", "K242004","Admin Utama"
    );
    
        BtnHapus.setVisible(allowedUsers.contains(akses.getkode()));
        MnCetakHasilLab.setEnabled(allowedUsers.contains(akses.getkode()));
        MnCetakNota.setEnabled(allowedUsers.contains(akses.getkode()));
        MnUbah.setEnabled(allowedUsers.contains(akses.getkode()));
        BtnPrint.setEnabled(allowedUsers.contains(akses.getkode()));
        MnCetakSuratCovid.setEnabled(akses.getsurat_keterangan_covid());
        ppBerkasDigital.setEnabled(akses.getberkas_digital_perawatan());  
        ppRiwayat.setEnabled(akses.getresume_pasien());   
    }
    
    public void setPasien(String pasien){
        NoRawat.setText(pasien);
    }
    
    private void CreatePDF(String FileName) {
    
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if(tabMode.getRowCount()==0){
        JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    } else if(Kd2.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
    } else {
        try {   
            ps4 = koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama," +
                "DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip," +
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter," +
                "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat," +
                "dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir,pasien.tgl_lahir " +
                "from periksa_lab " +
                "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join petugas on periksa_lab.nip=petugas.nip " +
                "inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter " +
                "inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel " +
                "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec " +
                "inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab " +
                "where periksa_lab.kategori='PK' and periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? " +
                "group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");

            ps4.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString());
            ps4.setString(2, tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
            ps4.setString(3, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
            rs = ps4.executeQuery();

            while(rs.next()){
                kamar = Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc limit 1");
                if(!kamar.equals("")){
                    namakamar = kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamar+"'");
                    kamar = "Kamar";
                } else {
                    kamar = "Poli";
                    namakamar = Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                }

                // Cek khusus upload: HANYA J000021 memakai rptPeriksaLab3Permintaan.jasper
                boolean hanyaJ000021 = false;
                PreparedStatement psCekJ000021 = null;
                ResultSet rsCekJ000021 = null;
                try {
                    psCekJ000021 = koneksi.prepareStatement(
                        "select count(distinct kd_jenis_prw) as total_pemeriksaan, " +
                        "sum(if(kd_jenis_prw='J000021',1,0)) as total_j000021 " +
                        "from periksa_lab where kategori='PK' and no_rawat=? and tgl_periksa=? and jam=?");
                    psCekJ000021.setString(1, rs.getString("no_rawat"));
                    psCekJ000021.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                    psCekJ000021.setString(3, rs.getString("jam"));
                    rsCekJ000021 = psCekJ000021.executeQuery();
                    if(rsCekJ000021.next()){
                        hanyaJ000021 = (rsCekJ000021.getInt("total_pemeriksaan") == 1 && rsCekJ000021.getInt("total_j000021") == 1);
                    }
                } catch (Exception e) {
                    System.out.println("Notif cek upload J000021 : " + e);
                } finally {
                    if(rsCekJ000021 != null){
                        rsCekJ000021.close();
                    }
                    if(psCekJ000021 != null){
                        psCekJ000021.close();
                    }
                }

                // GFR check mengikuti prinsip MneLFGActionPerformed, tetapi upload GFR hanya untuk umur > 18 tahun
                boolean adaKreatinin = false;
                double kreatinin = -1, gfr = 0;
                int umurAngka = Sequel.cariInteger("select TIMESTAMPDIFF(YEAR, ?, ?)", rs.getString("tgl_lahir"), Valid.SetTgl(rs.getString("tgl_periksa")));
                String jenisKelamin = rs.getString("jk");

                PreparedStatement psKreatinin = koneksi.prepareStatement(
                    "SELECT nilai FROM detail_periksa_lab " +
                    "WHERE no_rawat=? AND kd_jenis_prw='J000027' " +
                    "AND tgl_periksa=? AND jam=? " +
                    "AND id_template='3309' LIMIT 1");
                
                psKreatinin.setString(1, rs.getString("no_rawat"));
                psKreatinin.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                psKreatinin.setString(3, rs.getString("jam"));
                ResultSet rsKreatinin = psKreatinin.executeQuery();
                if(rsKreatinin.next()){
                    try {
                        kreatinin = Double.parseDouble(rsKreatinin.getString("nilai").replace(",", "."));
                        adaKreatinin = true;
                    } catch(Exception e){
                        System.out.println("Gagal parsing kreatinin: " + e);
                    }
                }
                rsKreatinin.close();
                psKreatinin.close();

                if(adaKreatinin){
                    double kappa = jenisKelamin.equalsIgnoreCase("L") ? 0.9 : 0.7;
                    double alpha = jenisKelamin.equalsIgnoreCase("L") ? -0.302 : -0.241;
                    double faktorJK = jenisKelamin.equalsIgnoreCase("L") ? 1.0 : 1.012;
                    double scrKappa = kreatinin / kappa;
                    double min = Math.min(scrKappa, 1);
                    double max = Math.max(scrKappa, 1);
                    gfr = 142 * Math.pow(min, alpha) * Math.pow(max, -1.200) * Math.pow(0.9938, umurAngka) * faktorJK;
                }

                Map<String, Object> param = new HashMap<>();
                param.put("noperiksa", rs.getString("no_rawat"));
                param.put("norm", rs.getString("no_rkm_medis"));
                param.put("namapasien", rs.getString("nm_pasien"));
                param.put("jkel", jenisKelamin);
                param.put("umur", rs.getString("umur"));
                param.put("pengirim", Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", rs.getString("dokter_perujuk")));
                param.put("tanggal", rs.getString("tgl_periksa"));
                param.put("penjab", rs.getString("nm_dokter"));
                param.put("petugas", rs.getString("nama"));
                param.put("jam", rs.getString("jam"));
                param.put("alamat", rs.getString("alamat"));
                param.put("kamar", kamar);
                param.put("namakamar", namakamar);
                param.put("lahir", Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?", rs.getString("no_rkm_medis")));

                if(adaKreatinin){
                    param.put("kreatinin", Valid.SetAngka(kreatinin));
                    param.put("gfr", Valid.SetAngka(gfr));
                    param.put("umurAngka", umurAngka);
                    param.put("jenisKelamin", jenisKelamin);
                }

                finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("kd_dokter"));
                param.put("finger", "Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("nip"));
                param.put("finger2", "Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  

                Sequel.queryu("delete from temporary_lab");

                ps2 = koneksi.prepareStatement(
                    "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                    "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=?");
                ps2.setString(1, rs.getString("no_rawat"));
                ps2.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                ps2.setString(3, rs.getString("jam"));
                rs2 = ps2.executeQuery();
                while(rs2.next()){
                    simpanTemporaryLabAman("'0','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                    ps3 = koneksi.prepareStatement(
                        "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                        "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.id_template from detail_periksa_lab inner join template_laboratorium "+
                        "on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                        "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                    ps3.setString(1, rs.getString("no_rawat"));
                    ps3.setString(2, rs2.getString("kd_jenis_prw"));
                    ps3.setString(3, Valid.SetTgl(rs.getString("tgl_periksa")));
                    ps3.setString(4, rs.getString("jam"));
                    rs3 = ps3.executeQuery();
                    
                    String kategoriUmur = getKategoriUmur(rs.getString("no_rawat"));
                    
                    while(rs3.next()){

                    item += rs3.getDouble("biaya_item");
                                    ttl += rs3.getDouble("biaya_item");

                                    String pemeriksaan = rs3.getString("Pemeriksaan").trim();
                                    String nilaiStr = normalisasiNilaiLabCetak(rs3.getString("nilai"));
                                    String satuan = rs3.getString("satuan");
                                    String rujukan = rs3.getString("nilai_rujukan") == null ? "" : rs3.getString("nilai_rujukan").trim();
                                    String keterangan = rs3.getString("keterangan") == null ? "" : rs3.getString("keterangan").trim();
                                    String kdJenisPrwUpload = rs3.getString("kd_jenis_prw");
                                    String idTemplateUpload = rs3.getString("id_template");
                                    String flag = tanpaBintangHLKritisLab(kdJenisPrwUpload, idTemplateUpload)
                                            ? ""
                                            : getStatusHLKritisLab(nilaiStr, rujukan, kdJenisPrwUpload, idTemplateUpload, kategoriUmur);

                                    simpanTemporaryLabAman(
                                    "'0','         " + pemeriksaan + "','" + nilaiStr + "','" + satuan +
                                    "','" + rujukan + "','" + keterangan + "','" + flag +
                                    "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User");              
                    }
                    
                    rs3.close();
                    ps3.close();
                }
                rs2.close();
                ps2.close();

                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());   
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                boolean adaPermintaan = false;
                pspermintaan = koneksi.prepareStatement(
                    "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel from permintaan_lab where "+
                    "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                pspermintaan.setString(1, rs.getString("no_rawat"));
                pspermintaan.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
                pspermintaan.setString(3, rs.getString("jam"));
                rspermintaan = pspermintaan.executeQuery();
                if(rspermintaan.next()){
                    adaPermintaan = true;
                    param.put("nopermintaan", rspermintaan.getString("noorder"));   
                    param.put("tanggalpermintaan", rspermintaan.getString("tgl_permintaan"));  
                    param.put("jampermintaan", rspermintaan.getString("jam_permintaan"));                                
                    param.put("tanggalsampel", rspermintaan.getString("tgl_sampel"));  
                    param.put("jamsampel", rspermintaan.getString("jam_sampel"));
                }
                rspermintaan.close();
                pspermintaan.close();

                // PILIH TEMPLATE PDF UPLOAD YANG TEPAT
                String queryCetakUpload = "select no, temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11, temp12, temp13, temp14, temp15, temp16 from temporary_lab order by no asc";
                if(hanyaJ000021){
                    Valid.MyReportPDFqryUpload("rptPeriksaLab3Permintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",queryCetakUpload,FileName,param);
                } else if(adaKreatinin && umurAngka > 18){
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReportPDFqryUpload("rptPeriksaLabGFR.jasper","report","::[ Pemeriksaan Laboratorium GFR ]::",queryCetakUpload,FileName,param);
                } else if(adaPermintaan){
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReportPDFqryUpload("rptPeriksaLabPermintaan.jasper","report","::[ Pemeriksaan Laboratorium ]::",queryCetakUpload,FileName,param);
                } else {
                    param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                    Valid.MyReportPDFqryUpload("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",queryCetakUpload,FileName,param);
                }
            }

            rs.close();
            ps4.close();
        } catch (Exception e) {
            System.out.println("Notif Cetak Lab: " + e);
        }
    }
    this.setCursor(Cursor.getDefaultCursor());
    
    }

/**
 * Mencatat metadata upload agar berkas dari aplikasi Java tetap menampilkan
 * waktu dan user uploader di halaman Berkas Digital Perawatan.
 * Jika tabel metadata belum tersedia, upload utama tetap berjalan normal.
 */
private void simpanMetadataBerkasDigital(String noRawat, String kode, String lokasiFile, File fileUpload) {
    PreparedStatement psMeta = null;
    try {
        if (noRawat == null || noRawat.trim().equals("") ||
            kode == null || kode.trim().equals("") ||
            lokasiFile == null || lokasiFile.trim().equals("")) {
            return;
        }

        if (Sequel.cariInteger(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema=DATABASE() AND table_name='berkas_digital_perawatan_meta'") == 0) {
            System.out.println("Metadata upload tidak dicatat: tabel berkas_digital_perawatan_meta belum tersedia.");
            return;
        }

        String userUpload = akses.getkode() == null ? "" : akses.getkode().trim();
        if (userUpload.equals("")) userUpload = "system";

        String namaFile = "";
        long ukuranFile = 0;
        String tipeFile = "";

        if (fileUpload != null) {
            namaFile = fileUpload.getName();
            if (fileUpload.exists()) ukuranFile = fileUpload.length();
        }
        if (namaFile.equals("")) {
            namaFile = new File(lokasiFile.replace('\\', '/')).getName();
        }

        String namaLower = namaFile.toLowerCase(java.util.Locale.ROOT);
        if (namaLower.endsWith(".pdf")) tipeFile = "application/pdf";
        else if (namaLower.endsWith(".png")) tipeFile = "image/png";
        else if (namaLower.endsWith(".jpg") || namaLower.endsWith(".jpeg")) tipeFile = "image/jpeg";

        psMeta = koneksi.prepareStatement(
                "UPDATE berkas_digital_perawatan_meta SET " +
                "nama_file_asli=?, ukuran_file=?, tipe_file=?, uploaded_at=NOW(), uploaded_by=?, " +
                "deleted_at=NULL, deleted_by=NULL " +
                "WHERE no_rawat=? AND kode=? AND lokasi_file=? AND deleted_at IS NULL");
        psMeta.setString(1, namaFile);
        psMeta.setLong(2, ukuranFile);
        psMeta.setString(3, tipeFile);
        psMeta.setString(4, userUpload);
        psMeta.setString(5, noRawat.trim());
        psMeta.setString(6, kode.trim());
        psMeta.setString(7, lokasiFile.trim());
        int diperbarui = psMeta.executeUpdate();
        psMeta.close();
        psMeta = null;

        if (diperbarui == 0) {
            psMeta = koneksi.prepareStatement(
                    "INSERT INTO berkas_digital_perawatan_meta " +
                    "(no_rawat, kode, lokasi_file, nama_file_asli, ukuran_file, tipe_file, uploaded_at, uploaded_by) " +
                    "VALUES (?,?,?,?,?,?,NOW(),?)");
            psMeta.setString(1, noRawat.trim());
            psMeta.setString(2, kode.trim());
            psMeta.setString(3, lokasiFile.trim());
            psMeta.setString(4, namaFile);
            psMeta.setLong(5, ukuranFile);
            psMeta.setString(6, tipeFile);
            psMeta.setString(7, userUpload);
            psMeta.executeUpdate();
        }
    } catch (Exception e) {
        System.out.println("Notif metadata upload berkas digital : " + e);
    } finally {
        try { if (psMeta != null) psMeta.close(); } catch (Exception e) {}
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
            kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%SO Digital%'");
            if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".pdf'") > 0) {
                uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf", "pages/upload/" + FileName + ".pdf"
                });
            } else {
                uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf"
                });
            }

            // Menampilkan notifikasi
            if (uploadSuccess) {
                String noRawatUpload = tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim();
                String filePathUpload = "pages/upload/" + FileName + ".pdf";
                simpanMetadataBerkasDigital(noRawatUpload, kodeberkas, filePathUpload, file);
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS LAB%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                String noRawatUpload = ambilNilaiTableBarisKunci(tbDokter, 0, 0);

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        noRawatUpload, kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        noRawatUpload, kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    simpanMetadataBerkasDigital(noRawatUpload, kodeberkas, filePath, jpgFile);
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
//        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS LAB%'");
//        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".jpg'") > 0) {
//            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
//                tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg", "pages/upload/" + FileName + ".jpg"
//            });
//        } else {
//            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
//                tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg"
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


private String normalisasiNilaiLabCetak(String nilaiAsli) {
    if (nilaiAsli == null) return "";
    return nilaiAsli.trim();
}

private String normalisasiAngkaLab(String angkaRaw) {
    if (angkaRaw == null) return "";
    String angka = angkaRaw.trim();
    if (angka.equals("") || angka.equals("-")) return "";

    boolean adaKoma = angka.indexOf(',') >= 0;
    boolean adaTitik = angka.indexOf('.') >= 0;

    if (adaKoma && adaTitik) {
        if (angka.lastIndexOf(',') > angka.lastIndexOf('.')) {
            angka = angka.replace(".", "").replace(",", ".");
        } else {
            angka = angka.replace(",", "");
        }
    } else if (adaKoma) {
        if (angka.matches("-?\\d{1,3}(,\\d{3})+") && !angka.matches("-?0,\\d+")) {
            angka = angka.replace(",", "");
        } else {
            angka = angka.replace(",", ".");
        }
    } else if (adaTitik) {
        if (angka.matches("-?\\d{1,3}(\\.\\d{3})+") && !angka.matches("-?0\\.\\d+")) {
            angka = angka.replace(".", "");
        }
    }

    return angka;
}

private Double ambilDoubleAngkaLab(String nilaiAsli) {
    try {
        if (nilaiAsli == null) return null;
        String nilai = nilaiAsli.trim();
        if (nilai.indexOf('\'') >= 0 || nilai.indexOf('"') >= 0 || nilai.indexOf('`') >= 0) return null;
        nilai = nilai.replaceAll("^[<>]=?\\s*", "");
        nilai = nilai.replaceAll("[^0-9,\\.\\-]", "");
        String angka = normalisasiAngkaLab(nilai);
        if (!angka.matches("-?\\d+(\\.\\d+)?")) return null;
        return Double.valueOf(angka);
    } catch (Exception e) {
        return null;
    }
}

private Double[] ambilRentangAngkaLab(String nilaiAsli) {
    try {
        if (nilaiAsli == null) return null;
        String nilai = nilaiAsli.trim();
        if (nilai.equals("")) return null;

        // Abaikan tanda pembanding di depan hasil, misalnya >40 atau <10.
        nilai = nilai.replaceAll("^[<>]=?\\s*", "");
        nilai = nilai.replace("~", "-").replace("–", "-").replace("—", "-");

        // Mendukung hasil rentang seperti 10 - 20 atau 1:00 - 3:00.
        // Untuk format 1:00, nilai tetap dibaca sebagai 100 hanya untuk kebutuhan perbandingan.
        if (nilai.matches(".*\\d\\s*-\\s*\\d.*")) {
            String[] bagian = nilai.split("\\s*-\\s*", 2);
            if (bagian.length == 2) {
                Double bawah = ambilDoubleAngkaLab(bagian[0]);
                Double atas = ambilDoubleAngkaLab(bagian[1]);
                if (bawah != null && atas != null) {
                    if (bawah.doubleValue() <= atas.doubleValue()) {
                        return new Double[]{bawah, atas};
                    } else {
                        return new Double[]{atas, bawah};
                    }
                }
            }
        }

        Double tunggal = ambilDoubleAngkaLab(nilaiAsli);
        if (tunggal == null) return null;
        return new Double[]{tunggal, tunggal};
    } catch (Exception e) {
        return null;
    }
}

private boolean tanpaBintangHLKritisLab(String kdJenisPrw, String idTemplate) {
    String kode = (kdJenisPrw == null ? "" : kdJenisPrw.trim().toUpperCase()) + "|" +
                  (idTemplate == null ? "" : idTemplate.trim());

    // Khusus pemeriksaan ini tidak ditandai bintang walaupun hasil di luar nilai rujukan.
    return kode.equals("J000022|3279") || kode.equals("J000022|3280");
}

private String getStatusHLKritisLab(String nilaiStr, String rujukanStr, String kdJenisPrw, String idTemplate, String kategoriUmur) {
    if (tanpaBintangHLKritisLab(kdJenisPrw, idTemplate)) return "";

    String statusRujukan = getStatusHLRujukanLab(nilaiStr, rujukanStr);
    String statusKritis = getStatusKritisLab(nilaiStr, kdJenisPrw, idTemplate, kategoriUmur);
    if (!statusKritis.equals("")) return statusKritis;
    return statusRujukan;
}

private String getStatusHLRujukanLab(String nilaiStr, String rujukanStr) {
    try {
        Double[] nilaiRange = ambilRentangAngkaLab(nilaiStr);
        if (nilaiRange == null || rujukanStr == null) return "";
        double nilaiBawah = nilaiRange[0].doubleValue();
        double nilaiAtas = nilaiRange[1].doubleValue();

        String hasil = nilaiStr == null ? "" : nilaiStr.trim();
        boolean hasilLebih = hasil.startsWith(">");
        boolean hasilKurang = hasil.startsWith("<");

        String rujukan = rujukanStr.trim().replace("~", "-").replace("–", "-").replace("—", "-");
        if (rujukan.equals("")) return "";

        if (!rujukan.startsWith("<") && !rujukan.startsWith(">") && rujukan.matches(".*\\d\\s*-\\s*.*\\d.*")) {
            String[] batas = rujukan.split("\\s*-\\s*", 2);
            if (batas.length == 2) {
                Double bawah = ambilDoubleAngkaLab(batas[0]);
                Double atas = ambilDoubleAngkaLab(batas[1]);
                if (bawah != null && atas != null) {
                    if (hasilLebih) {
                        if (nilaiAtas >= atas.doubleValue()) return "H";
                    } else if (hasilKurang) {
                        if (nilaiBawah <= bawah.doubleValue()) return "L";
                    } else {
                        if (nilaiBawah < bawah.doubleValue()) return "L";
                        if (nilaiAtas > atas.doubleValue()) return "H";
                    }
                }
            }
        } else if (rujukan.startsWith("<")) {
            Double batas = ambilDoubleAngkaLab(rujukan);
            if (batas != null) {
                if (hasilLebih) {
                    if (nilaiAtas >= batas.doubleValue()) return "H";
                } else if (!hasilKurang && nilaiAtas >= batas.doubleValue()) {
                    return "H";
                }
            }
        } else if (rujukan.startsWith(">")) {
            Double batas = ambilDoubleAngkaLab(rujukan);
            if (batas != null) {
                if (hasilKurang) {
                    if (nilaiBawah <= batas.doubleValue()) return "L";
                } else if (!hasilLebih && nilaiBawah <= batas.doubleValue()) {
                    return "L";
                }
            }
        }
    } catch (Exception e) {
        // parsing gagal, dianggap tidak diberi tanda
    }
    return "";
}

private String getKategoriKritisLab(String kdJenisPrw, String idTemplate) {
    String kode = (kdJenisPrw == null ? "" : kdJenisPrw.trim().toUpperCase()) + "|" +
                  (idTemplate == null ? "" : idTemplate.trim());

    // Penentuan parameter kritis dibuat berdasarkan pasangan kd_jenis_prw + id_template
    // agar tidak salah sasaran ketika nama parameter berbeda-beda.
    if (kode.equals("J000120|4004") || kode.equals("J000121|4010") || kode.equals("J000020|3405")) {
        return "HEMOGLOBIN";
    }

    if (kode.equals("J000090|3780") || 
        kode.equals("J000090|3792") || kode.equals("J000103|3916") ||
        kode.equals("J000103|3921") || kode.equals("J000104|3936") ||
        kode.equals("J000120|4005") || kode.equals("J000121|4011") ||
        kode.equals("J000020|3416") || kode.equals("J000045|3505") ||
        kode.equals("J000046|3541")) {
        return "LEUKOSIT";
    }

    if (kode.equals("J000021|3523") || kode.equals("J000047|3558") ||
        kode.equals("J000123|3857") || kode.equals("J000120|4008") ||
        kode.equals("J000121|4014") || kode.equals("J000020|3412") ||
        kode.equals("J000045|3501") || kode.equals("J000046|3537")) {
        return "TROMBOSIT";
    }

    if (kode.equals("J000022|3282") || kode.equals("J000048|3576") ||
        kode.equals("J000049|3583") || kode.equals("J000050|3590")) {
        return "APTT";
    }

    if (kode.equals("J000025|3675") || kode.equals("J000025|3698") ||
        kode.equals("J000025|3295") || kode.equals("J000025|3296") ||
        kode.equals("J000055|3612") || kode.equals("J000055|3615") ||
        kode.equals("J000056|3622") || kode.equals("J000056|3624")) {
        return "GLUKOSA_DARAH_SEWAKTU";
    }

    if (kode.equals("J000027|3309") || kode.equals("J000027|3311") ||
        kode.equals("J000059|3648") || kode.equals("J000059|3650") ||
        kode.equals("J000060|3652") || kode.equals("J000060|3654") ||
        kode.equals("J000151|4322") || kode.equals("J000159|4352") ||
        kode.equals("J000159|4389")) {
        return "KREATININ";
    }

    if (kode.equals("J000024|3285") || kode.equals("J000024|3286") ||
        kode.equals("J000032|3388") || kode.equals("J000051|3592") ||
        kode.equals("J000054|3603") || kode.equals("J000054|3604") ||
        kode.equals("J000024|3674") || kode.equals("J000090|3772") ||
        kode.equals("J000103|3908") || kode.equals("J000051|3975") ||
        kode.equals("J000054|3977")) {
        return "BILIRUBIN";
    }

    if (kode.equals("J000026|3601") || kode.equals("J000057|3638") ||
        kode.equals("J000058|3646") || kode.equals("J000026|4331")) {
        return "TROPONIN_I";
    }

    if (kode.equals("J000151|4325") || kode.equals("J000159|4366")|| kode.equals("J000028|3383")|| kode.equals("J000079|3711")|| kode.equals("J000085|3733")) {
        return "KALIUM";
    }

    if (kode.equals("J000062|3708") || kode.equals("J000062|3709") ||
        kode.equals("J000153|4334")) {
        return "FT4";
    }

    return "";
}

private String getStatusKritisLab(String nilaiStr, String kdJenisPrw, String idTemplate, String kategoriUmur) {
    try {
        Double[] nilaiRange = ambilRentangAngkaLab(nilaiStr);
        if (nilaiRange == null) return "";
        double nilaiBawah = nilaiRange[0].doubleValue();
        double nilaiAtas = nilaiRange[1].doubleValue();
        String umur = kategoriUmur == null ? "Dewasa" : kategoriUmur;
        String parameter = getKategoriKritisLab(kdJenisPrw, idTemplate);

        if (parameter.equals("HEMOGLOBIN")) {
            if (umur.equals("Neonatus") && nilaiBawah <= 10.0) return "L*";
            if (umur.equals("Neonatus") && nilaiAtas >= 24.0) return "H*";
            if (umur.equals("Anak") && nilaiBawah <= 7.0) return "L*";
            if (umur.equals("Anak") && nilaiAtas >= 20.0) return "H*";
            if (umur.equals("Dewasa") && nilaiBawah <= 7.0) return "L*";
        } else if (parameter.equals("LEUKOSIT")) {
            if (umur.equals("Neonatus") && nilaiBawah <= 5000.0) return "L*";
            if (umur.equals("Neonatus") && nilaiAtas >= 50000.0) return "H*";
            if (!umur.equals("Neonatus") && nilaiBawah < 1000.0) return "L*";
            if (!umur.equals("Neonatus") && nilaiAtas >= 50000.0) return "H*";
        } else if (parameter.equals("TROMBOSIT")) {
            if (umur.equals("Neonatus") && nilaiBawah <= 50000.0) return "L*";
            if (!umur.equals("Neonatus") && nilaiBawah <= 30000.0) return "L*";
            if (!umur.equals("Neonatus") && nilaiAtas >= 800000.0) return "H*";
        } else if (parameter.equals("APTT")) {
            if (nilaiAtas > 150.0) return "H*";
        } else if (parameter.equals("GLUKOSA_DARAH_SEWAKTU")) {
            if (umur.equals("Neonatus") && nilaiBawah <= 40.0) return "L*";
            if (umur.equals("Neonatus") && nilaiAtas >= 250.0) return "H*";
            if (!umur.equals("Neonatus") && nilaiBawah < 70.0) return "L*";
            if (!umur.equals("Neonatus") && nilaiAtas >= 600.0) return "H*";
        } else if (parameter.equals("KREATININ")) {
            if (umur.equals("Neonatus") && nilaiAtas >= 1.5) return "H*";
            if (umur.equals("Anak") && nilaiAtas >= 2.5) return "H*";
            if (umur.equals("Dewasa") && nilaiAtas >= 10.0) return "H*";
        } else if (parameter.equals("BILIRUBIN")) {
            if (umur.equals("Neonatus") && nilaiAtas >= 15.0) return "H*";
        } else if (parameter.equals("TROPONIN_I")) {
            if (nilaiAtas > 0.05) return "H*";
        } else if (parameter.equals("KALIUM")) {
            if (umur.equals("Neonatus") && nilaiBawah <= 3.5) return "L*";
            if (!umur.equals("Neonatus") && nilaiBawah <= 2.5) return "L*";
            if (nilaiAtas >= 6.0) return "H*";
        } else if (parameter.equals("FT4")) {
            if (nilaiAtas >= 7.77) return "H*";
        }
    } catch (Exception e) {
        // tidak termasuk kritis
    }
    return "";
}

private String getKategoriUmur(String noRawat) {
    String umurStr = Sequel.cariIsi(
        "select pasien.umur from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?", 
        noRawat
    );
    int umurTahun = 0;
    int umurHari = 0;

    try {
        if(umurStr.toLowerCase().contains("hr")) {
            umurHari = Integer.parseInt(umurStr.replaceAll("[^0-9]", ""));
        } else {
            umurTahun = Integer.parseInt(umurStr.replaceAll("[^0-9]", ""));
        }
    } catch(Exception e){
        return "Dewasa"; // default fallback
    }

    if(umurHari > 0 && umurHari <= 28){
        return "Neonatus";
    } else if(umurTahun >= 1 && umurTahun < 18){
        return "Anak";
    } else {
        return "Dewasa";
    }
}

private String getStatusHLKritis(String nilaiStr, String nilaiRujukanStr, String pemeriksaan, int umur, String jk){
    try {
        double nilai = Double.parseDouble(nilaiStr);

        // Normalisasi nilai rujukan: bisa bentuk "4.0~10.0", ">=3.5", "<5.0", dll.
        if(nilaiRujukanStr != null){
            nilaiRujukanStr = nilaiRujukanStr.replace(",", ".").replace(" ", "").trim();
            
            if(nilaiRujukanStr.contains("~") || nilaiRujukanStr.contains("-")){
                String delimiter = nilaiRujukanStr.contains("~") ? "~" : "-";
                String[] batas = nilaiRujukanStr.split(Pattern.quote(delimiter));
                if(batas.length == 2){
                    double bawah = Double.parseDouble(batas[0]);
                    double atas = Double.parseDouble(batas[1]);
                    if(nilai < bawah) return "L";
                    else if(nilai > atas) return "H";
                }
            } else if(nilaiRujukanStr.startsWith("<")){
                double batas = Double.parseDouble(nilaiRujukanStr.substring(1));
                if(nilai >= batas) return "H";
            } else if(nilaiRujukanStr.startsWith(">")){
                double batas = Double.parseDouble(nilaiRujukanStr.substring(1));
                if(nilai <= batas) return "L";
            }
        }

        // Tambahkan logika cek nilai kritis berdasarkan tabel jika kamu punya (bisa mapping manual).
        if(isKritis(pemeriksaan, nilai, umur, jk)){
            return "*";
        }

    } catch(Exception e){
        // ignore parsing error
    }
    return "";
}

private boolean isKritis(String pemeriksaan, double nilai, int umur, String jk){
    if(pemeriksaan.equalsIgnoreCase("Glukosa")){
        return (nilai < 50 || nilai > 500);
    }
    if(pemeriksaan.equalsIgnoreCase("Hemoglobin")){
        return (nilai < 5 || nilai > 20);
    }
    // Tambahkan sesuai tabel kritis
    return false;
}

private void simpanSumTulang(String noRawat, String tglPeriksa, String jamPeriksa){
    String tglPengambilan = Valid.SetTgl(TglPengambilan.getSelectedItem() + "");

    try {
        if (idSumTulang == null || idSumTulang.trim().equals("")) {
            String sql = "INSERT INTO rsaj_lab_sum_tulang (" +
                         "no_rawat, tgl_periksa, jam_periksa, " +
                         "tgl_pengambilan, ratio_mieloid_eritroid, " +
                         "selularitas, eritropoietik, leukopoietik, trombopoietik, " +
                         "sel_plasma, mitosis, kesan, saran" +
                         ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, noRawat);
                ps.setString(2, tglPeriksa);
                ps.setString(3, jamPeriksa);
                ps.setString(4, tglPengambilan);
                ps.setString(5, TRatio.getText().trim());
                ps.setString(6, TSelularitas.getText().trim());
                ps.setString(7, TEritropoietik.getText().trim());
                ps.setString(8, TLeukopoietik.getText().trim());
                ps.setString(9, TTrombopoietik.getText().trim());
                ps.setString(10, TSelPlasma.getText().trim());
                ps.setString(11, TMitosis.getText().trim());
                ps.setString(12, TKesan.getText().trim());
                ps.setString(13, TSaran.getText().trim());
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE rsaj_lab_sum_tulang SET " +
                         "tgl_pengambilan=?, ratio_mieloid_eritroid=?, " +
                         "selularitas=?, eritropoietik=?, leukopoietik=?, trombopoietik=?, " +
                         "sel_plasma=?, mitosis=?, kesan=?, saran=? " +
                         "WHERE id=?";

            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, tglPengambilan);
                ps.setString(2, TRatio.getText().trim());
                ps.setString(3, TSelularitas.getText().trim());
                ps.setString(4, TEritropoietik.getText().trim());
                ps.setString(5, TLeukopoietik.getText().trim());
                ps.setString(6, TTrombopoietik.getText().trim());
                ps.setString(7, TSelPlasma.getText().trim());
                ps.setString(8, TMitosis.getText().trim());
                ps.setString(9, TKesan.getText().trim());
                ps.setString(10, TSaran.getText().trim());
                ps.setString(11, idSumTulang);
                ps.executeUpdate();
            }
        }

        JOptionPane.showMessageDialog(null, "Hasil evaluasi sumsum tulang berhasil disimpan.");
        this.WindowSumTulang.dispose();

    } catch (Exception e) {
        System.out.println("Notifikasi simpan sumsum tulang : " + e);
        JOptionPane.showMessageDialog(null, "Gagal menyimpan hasil evaluasi sumsum tulang. " + e);
    }
}

private void CreatePDFSumTulang(String FileName) {
                
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if(tabMode.getRowCount()==0){
        JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    }else if(Kd2.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik No.Rawat pada table untuk memilih...!!!!");
    }else if(!(Kd2.getText().trim().equals(""))){    
        try {   
            ps4=koneksi.prepareStatement(
                "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"+
                "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "+
                " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "+
                "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "+
                "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "+
                "periksa_lab.tgl_periksa=? and periksa_lab.jam=? and periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
            try {
                ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                rs=ps4.executeQuery();
                while(rs.next()){
                    
                    kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat='"+rs.getString("no_rawat")+"' order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1");
                    if(!kamar.equals("")){
                        namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                                " where kamar.kd_kamar='"+kamar+"' ");            
                        kamar="Kamar";
                    }else if(kamar.equals("")){
                        kamar="Poli";
                        namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                                "where reg_periksa.no_rawat='"+rs.getString("no_rawat")+"'");
                    }
                    Map<String, Object> param = new HashMap<>();
                    param.put("noperiksa",rs.getString("no_rawat"));
                    param.put("norm",rs.getString("no_rkm_medis"));
                    param.put("namapasien",rs.getString("nm_pasien"));
                    param.put("jkel",rs.getString("jk"));
                    param.put("umur",rs.getString("umur"));
                    param.put("pengirim",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs.getString("dokter_perujuk")));
                    param.put("tanggal",rs.getString("tgl_periksa"));
                    param.put("penjab",rs.getString("nm_dokter"));
                    param.put("petugas",rs.getString("nama"));
                    param.put("jam",rs.getString("jam"));
                    param.put("alamat",rs.getString("alamat"));
                    param.put("kamar",kamar);
                    param.put("namakamar",namakamar);
                    param.put("lahir",rs.getString("lahir") == null ? "" : rs.getString("lahir"));
                    finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("kd_dokter"));
                    param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nm_dokter")+"\nID "+(finger.equals("")?rs.getString("kd_dokter"):finger)+"\n"+rs.getString("tgl_periksa"));  
                    finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",rs.getString("nip"));
                    param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+rs.getString("nama")+"\nID "+(finger.equals("")?rs.getString("nip"):finger)+"\n"+rs.getString("tgl_periksa"));  
                    
                    // =============== AMBIL DATA SUMSUM TULANG ===============
                    PreparedStatement psSum = null;
                    ResultSet rsSum = null;
                    try{
                        psSum = koneksi.prepareStatement(
                            "SELECT DATE_FORMAT(tgl_pengambilan,'%d-%m-%Y') AS tgl_pengambilan, " +
                            "       ratio_mieloid_eritroid, selularitas, eritropoietik, leukopoietik, " +
                            "       trombopoietik, sel_plasma, mitosis, kesan, saran " +
                            "FROM rsaj_lab_sum_tulang " +
                            "WHERE no_rawat=? AND tgl_periksa=? AND jam_periksa=? " +
                            "ORDER BY id DESC LIMIT 1"
                        );
                        psSum.setString(1, rs.getString("no_rawat"));
                        psSum.setString(2, Valid.SetTgl(rs.getString("tgl_periksa"))); // tgl_periksa di rs = dd-MM-yyyy
                        psSum.setString(3, rs.getString("jam"));
                        rsSum = psSum.executeQuery();
                        if(rsSum.next()){
                            // silakan sesuaikan nama param berikut dengan parameter di rptHasilSumsumTulang.jasper
                            param.put("tgl_pengambilan", rsSum.getString("tgl_pengambilan"));
                            param.put("ratio",         rsSum.getString("ratio_mieloid_eritroid"));
                            param.put("selularitas",   rsSum.getString("selularitas"));
                            param.put("eritropoietik", rsSum.getString("eritropoietik"));
                            param.put("leukopoietik",  rsSum.getString("leukopoietik"));
                            param.put("trombopoietik", rsSum.getString("trombopoietik"));
                            param.put("selplasma",     rsSum.getString("sel_plasma"));
                            param.put("mitosis",       rsSum.getString("mitosis"));
                            param.put("kesan_sumsum",  rsSum.getString("kesan"));
                            param.put("saran_sumsum",  rsSum.getString("saran"));
                        } else {
                            // kalau belum ada data tersimpan, kirim kosong
                            param.put("tgl_pengambilan", "");
                            param.put("ratio",         "");
                            param.put("selularitas",   "");
                            param.put("eritropoietik", "");
                            param.put("leukopoietik",  "");
                            param.put("trombopoietik", "");
                            param.put("selplasma",     "");
                            param.put("mitosis",       "");
                            param.put("kesan_sumsum",  "");
                            param.put("saran_sumsum",  "");
                        }
                    } catch (Exception exSum){
                        System.out.println("Notif sumsum tulang (cetak) : "+exSum);
                    } finally {
                        if(rsSum != null){
                            try { rsSum.close(); } catch(Exception ex) {}
                        }
                        if(psSum != null){
                            try { psSum.close(); } catch(Exception ex) {}
                        }
                    }
                    // ============= END AMBIL DATA SUMSUM TULANG =============

                    Sequel.queryu("delete from temporary_lab");

                    ps2=koneksi.prepareStatement(
                        "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "+
                        "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "+
                        "and periksa_lab.jam=?");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        ps2.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                        ps2.setString(3,rs.getString("jam"));
                        rs2=ps2.executeQuery();
                        i=0;
                        while(rs2.next()){
                            simpanTemporaryLabAman("'"+i+"','"+rs2.getString("nm_perawatan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                            i++;
                            ps3=koneksi.prepareStatement(
                                "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"+
                                "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
                                "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
                            try {
                                ps3.setString(1,rs.getString("no_rawat"));
                                ps3.setString(2,rs2.getString("kd_jenis_prw"));
                                ps3.setString(3,Valid.SetTgl(rs.getString("tgl_periksa")));
                                ps3.setString(4,rs.getString("jam"));
                                rs3=ps3.executeQuery();
                                while(rs3.next()){
                                    simpanTemporaryLabAman("'"+i+"','  "+rs3.getString("Pemeriksaan")+"','"+rs3.getString("nilai")+"','"+rs3.getString("satuan")
                                            +"','"+rs3.getString("nilai_rujukan")+"','"+rs3.getString("keterangan")+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Data User"); 
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("Notif ps3 : "+e);
                            } finally{
                                if(rs3!=null){
                                    rs3.close();
                                }
                                if(ps3!=null){
                                    ps3.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif ps2 : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                    
                    param.put("namars",akses.getnamars());
                    param.put("alamatrs",akses.getalamatrs());
                    param.put("kotars",akses.getkabupatenrs());
                    param.put("propinsirs",akses.getpropinsirs());
                    param.put("kontakrs",akses.getkontakrs());
                    param.put("emailrs",akses.getemailrs());   
                    param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                    pspermintaan=koneksi.prepareStatement(
                            "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan,"+
                            "IFNULL(DATE_FORMAT(tgl_sampel,'%d-%m-%Y'),'') as tgl_sampel,IFNULL(jam_sampel,'') as jam_sampel from permintaan_lab where "+
                            "no_rawat=? and tgl_hasil=? and jam_hasil=?");
                    try {
                        pspermintaan.setString(1,rs.getString("no_rawat"));
                        pspermintaan.setString(2,Valid.SetTgl(rs.getString("tgl_periksa")));
                        pspermintaan.setString(3,rs.getString("jam"));
                        rspermintaan=pspermintaan.executeQuery();
                        if(rspermintaan.next()){
                            param.put("nopermintaan",rspermintaan.getString("noorder"));   
                            param.put("tanggalpermintaan",rspermintaan.getString("tgl_permintaan"));  
                            param.put("jampermintaan",rspermintaan.getString("jam_permintaan"));
                            param.put("tanggalsampel",rspermintaan.getString("tgl_sampel"));
                            param.put("jamsampel",rspermintaan.getString("jam_sampel"));
                            Valid.MyReportPDFqryUpload("rptHasilSumsumTulang.jasper","report","::[ Hasil Evaluasi Sum Sum Tulang ]::",
                                        "select no, temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11, temp12, temp13, temp14, temp15, temp16 from temporary_lab order by no asc",FileName, param);
                            }else{
                                param.put("catatan", ambilCatatanLab(rs.getString("no_rawat"), rs.getString("tgl_periksa"), rs.getString("jam")));
                                Valid.MyReportPDFqryUpload("rptPeriksaLab.jasper","report","::[ Pemeriksaan Laboratorium ]::",
                                        "select no, temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11, temp12, temp13, temp14, temp15, temp16 from temporary_lab order by no asc",FileName, param);                                
                            }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rspermintaan!=null){
                            rspermintaan.close();
                        }
                        if(pspermintaan!=null){
                            pspermintaan.close();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif ps4 : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps4!=null){
                    ps4.close();
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }            
    }
    this.setCursor(Cursor.getDefaultCursor());
    
    }

private void ConvertPDFtoJPGSumTulang(String FileName) {
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

private void UploadJPGSumTulang(String FileName, String docpath) {
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS LAB%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                String noRawatUpload = ambilNilaiTableBarisKunci(tbDokter, 0, 0);

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        noRawatUpload, kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        noRawatUpload, kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    simpanMetadataBerkasDigital(noRawatUpload, kodeberkas, filePath, jpgFile);
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

private void HapusJPGSumTulang() {
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

    
//      public void HasilLabPDFKlaimBPJSKompilasi(String NomorRawat, String NoRekamMedis) {
//         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//         try {
//             ps4 = koneksi.prepareStatement(
//                     "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"
//                     + "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
//                     + " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "
//                     + "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "
//                     + "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "
//                     + " periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
//             try {
//                 ps4.setString(1, NomorRawat);
//                 rs = ps4.executeQuery();
//                 while (rs.next()) {
//                     kamar = Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='" + rs.getString("no_rawat") + "' order by tgl_masuk desc limit 1");
//                     if (!kamar.equals("")) {
//                         namakamar = kamar + ", " + Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "
//                                 + " where kamar.kd_kamar='" + kamar + "' ");
//                         kamar = "Kamar";
//                     } else if (kamar.equals("")) {
//                         kamar = "Poli";
//                         namakamar = Sequel.cariIsi("select nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "
//                                 + "where reg_periksa.no_rawat='" + rs.getString("no_rawat") + "'");
//                     }
//                     Map<String, Object> param = new HashMap<>();
//                     param.put("noperiksa", rs.getString("no_rawat"));
//                     param.put("norm", rs.getString("no_rkm_medis") + " | Tgl Lhr : " + rs.getString("lahir"));
//                     param.put("namapasien", rs.getString("nm_pasien"));
//                     param.put("jkel", rs.getString("jk"));
//                     param.put("umur", rs.getString("umur"));
//                     param.put("pengirim", Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", rs.getString("dokter_perujuk")));
//                     param.put("tanggal", rs.getString("tgl_periksa"));
//                     param.put("penjab", rs.getString("nm_dokter"));
//                     param.put("petugas", rs.getString("nama"));
//                     param.put("jam", rs.getString("jam"));
//                     param.put("alamat", rs.getString("alamat"));
//                     param.put("kamar", kamar);
//                     param.put("namakamar", namakamar);
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("kd_dokter"));
//                     param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nm_dokter") + "\nID " + (finger.equals("") ? rs.getString("kd_dokter") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("nip"));
//                     param.put("finger2", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nama") + "\nID " + (finger.equals("") ? rs.getString("nip") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     Sequel.queryu("truncate table temporary_lab");

//                     ps2 = koneksi.prepareStatement(
//                             "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "
//                             + "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "
//                             + "and periksa_lab.jam=?");
//                     try {
//                         ps2.setString(1, rs.getString("no_rawat"));
//                         ps2.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         ps2.setString(3, rs.getString("jam"));
//                         rs2 = ps2.executeQuery();
//                         urutan = 0;
//                         while (rs2.next()) {
//                             urutan++;
//                             simpanTemporaryLabAman( "'0','" + rs2.getString("nm_perawatan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                             ps3 = koneksi.prepareStatement(
//                                     "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"
//                                     + "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
//                                     + "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
//                             try {
//                                 ps3.setString(1, rs.getString("no_rawat"));
//                                 ps3.setString(2, rs2.getString("kd_jenis_prw"));
//                                 ps3.setString(3, Valid.SetTgl(rs.getString("tgl_periksa")));
//                                 ps3.setString(4, rs.getString("jam"));
//                                 rs3 = ps3.executeQuery();
//                                 while (rs3.next()) {
//                                     simpanTemporaryLabAman( "'0','  " + rs3.getString("Pemeriksaan") + "','" + rs3.getString("nilai") + "','" + rs3.getString("satuan")
//                                             + "','" + rs3.getString("nilai_rujukan") + "','" + rs3.getString("keterangan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                                 }
//                             } catch (Exception e) {
//                                 System.out.println("Notif ps3 : " + e);
//                             } finally {
//                                 if (rs3 != null) {
//                                     rs3.close();
//                                 }
//                                 if (ps3 != null) {
//                                     ps3.close();
//                                 }
//                             }
//                         }
//                     } catch (Exception e) {
//                         System.out.println("Notif ps2 : " + e);
//                     } finally {
//                         if (rs2 != null) {
//                             rs2.close();
//                         }
//                         if (ps2 != null) {
//                             ps2.close();
//                         }
//                     }

//                     param.put("namars", akses.getnamars());
//                     param.put("alamatrs", akses.getalamatrs());
//                     param.put("kotars", akses.getkabupatenrs());
//                     param.put("propinsirs", akses.getpropinsirs());
//                     param.put("kontakrs", akses.getkontakrs());
//                     param.put("emailrs", akses.getemailrs());
//                     param.put("logo", Sequel.cariGambar("select logo from setting"));
//                     pspermintaan = koneksi.prepareStatement(
//                             "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "
//                             + "no_rawat=? and tgl_hasil=? and jam_hasil=?");
//                     try {
//                         pspermintaan.setString(1, rs.getString("no_rawat"));
//                         pspermintaan.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         pspermintaan.setString(3, rs.getString("jam"));
//                         rspermintaan = pspermintaan.executeQuery();
//                         if (rspermintaan.next()) {
//                             param.put("nopermintaan", rspermintaan.getString("noorder"));
//                             param.put("tanggalpermintaan", rspermintaan.getString("tgl_permintaan"));
//                             param.put("jampermintaan", rspermintaan.getString("jam_permintaan"));
// //                            Valid.MyReportPDFKlaim("rptPeriksaLabPermintaan.jasper", "report", "3PERIKSALAB", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", ""));
// //                        } else {
// //                            Valid.MyReportPDFKlaim("rptPeriksaLab.jasper", "report", "3PERIKSALAB", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", "") + "-" + urutan);
// //                        }
//                     } catch (Exception e) {
//                         System.out.println("Notif : " + e);
//                     } finally {
//                         if (rspermintaan != null) {
//                             rspermintaan.close();
//                         }
//                         if (pspermintaan != null) {
//                             pspermintaan.close();
//                         }
//                     }
//                 }
//             } catch (Exception e) {
//                 System.out.println("Notif ps4 : " + e);
//             } finally {
//                 if (rs != null) {
//                     rs.close();
//                 }
//                 if (ps4 != null) {
//                     ps4.close();
//                 }
//             }
//         } catch (SQLException ex) {
//             System.out.println(ex);
//         }

//         this.setCursor(Cursor.getDefaultCursor());
//     }
    
//     public void HasilLabPDFKlaimBPJS(String NomorRawat, String NoRekamMedis) {
//         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//         try {
//             ps4 = koneksi.prepareStatement(
//                     "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"
//                     + "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
//                     + " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "
//                     + "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "
//                     + "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "
//                     + " periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
//             try {
//                 ps4.setString(1, NomorRawat);
//                 rs = ps4.executeQuery();
//                 while (rs.next()) {
//                     kamar = Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='" + rs.getString("no_rawat") + "' order by tgl_masuk desc limit 1");
//                     if (!kamar.equals("")) {
//                         namakamar = kamar + ", " + Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "
//                                 + " where kamar.kd_kamar='" + kamar + "' ");
//                         kamar = "Kamar";
//                     } else if (kamar.equals("")) {
//                         kamar = "Poli";
//                         namakamar = Sequel.cariIsi("select nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "
//                                 + "where reg_periksa.no_rawat='" + rs.getString("no_rawat") + "'");
//                     }
//                     Map<String, Object> param = new HashMap<>();
//                     param.put("noperiksa", rs.getString("no_rawat"));
//                     param.put("norm", rs.getString("no_rkm_medis") + " | Tgl Lhr : " + rs.getString("lahir"));
//                     param.put("namapasien", rs.getString("nm_pasien"));
//                     param.put("jkel", rs.getString("jk"));
//                     param.put("umur", rs.getString("umur"));
//                     param.put("pengirim", Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", rs.getString("dokter_perujuk")));
//                     param.put("tanggal", rs.getString("tgl_periksa"));
//                     param.put("penjab", rs.getString("nm_dokter"));
//                     param.put("petugas", rs.getString("nama"));
//                     param.put("jam", rs.getString("jam"));
//                     param.put("alamat", rs.getString("alamat"));
//                     param.put("kamar", kamar);
//                     param.put("namakamar", namakamar);
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("kd_dokter"));
//                     param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nm_dokter") + "\nID " + (finger.equals("") ? rs.getString("kd_dokter") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("nip"));
//                     param.put("finger2", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nama") + "\nID " + (finger.equals("") ? rs.getString("nip") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     Sequel.queryu("truncate table temporary_lab");

//                     ps2 = koneksi.prepareStatement(
//                             "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "
//                             + "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "
//                             + "and periksa_lab.jam=?");
//                     try {
//                         ps2.setString(1, rs.getString("no_rawat"));
//                         ps2.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         ps2.setString(3, rs.getString("jam"));
//                         rs2 = ps2.executeQuery();
//                         urutan = 0;
//                         while (rs2.next()) {
//                             urutan++;
//                             simpanTemporaryLabAman( "'0','" + rs2.getString("nm_perawatan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                             ps3 = koneksi.prepareStatement(
//                                     "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"
//                                     + "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
//                                     + "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
//                             try {
//                                 ps3.setString(1, rs.getString("no_rawat"));
//                                 ps3.setString(2, rs2.getString("kd_jenis_prw"));
//                                 ps3.setString(3, Valid.SetTgl(rs.getString("tgl_periksa")));
//                                 ps3.setString(4, rs.getString("jam"));
//                                 rs3 = ps3.executeQuery();
//                                 while (rs3.next()) {
//                                     simpanTemporaryLabAman( "'0','  " + rs3.getString("Pemeriksaan") + "','" + rs3.getString("nilai") + "','" + rs3.getString("satuan")
//                                             + "','" + rs3.getString("nilai_rujukan") + "','" + rs3.getString("keterangan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                                 }
//                             } catch (Exception e) {
//                                 System.out.println("Notif ps3 : " + e);
//                             } finally {
//                                 if (rs3 != null) {
//                                     rs3.close();
//                                 }
//                                 if (ps3 != null) {
//                                     ps3.close();
//                                 }
//                             }
//                         }
//                     } catch (Exception e) {
//                         System.out.println("Notif ps2 : " + e);
//                     } finally {
//                         if (rs2 != null) {
//                             rs2.close();
//                         }
//                         if (ps2 != null) {
//                             ps2.close();
//                         }
//                     }

//                     param.put("namars", akses.getnamars());
//                     param.put("alamatrs", akses.getalamatrs());
//                     param.put("kotars", akses.getkabupatenrs());
//                     param.put("propinsirs", akses.getpropinsirs());
//                     param.put("kontakrs", akses.getkontakrs());
//                     param.put("emailrs", akses.getemailrs());
//                     param.put("logo", Sequel.cariGambar("select logo from setting"));
//                     pspermintaan = koneksi.prepareStatement(
//                             "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "
//                             + "no_rawat=? and tgl_hasil=? and jam_hasil=?");
//                     try {
//                         pspermintaan.setString(1, rs.getString("no_rawat"));
//                         pspermintaan.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         pspermintaan.setString(3, rs.getString("jam"));
//                         rspermintaan = pspermintaan.executeQuery();
//                         if (rspermintaan.next()) {
//                             param.put("nopermintaan", rspermintaan.getString("noorder"));
//                             param.put("tanggalpermintaan", rspermintaan.getString("tgl_permintaan"));
//                             param.put("jampermintaan", rspermintaan.getString("jam_permintaan"));
//                             Valid.MyReport("rptPeriksaLabPermintaan.jasper", "report", "PERIKSALAB", param);
//                         } else {
//                             Valid.MyReport("rptPeriksaLab.jasper", "report", "PERIKSALAB", param);
//                         }
//                     } catch (Exception e) {
//                         System.out.println("Notif : " + e);
//                     } finally {
//                         if (rspermintaan != null) {
//                             rspermintaan.close();
//                         }
//                         if (pspermintaan != null) {
//                             pspermintaan.close();
//                         }
//                     }
//                 }
//             } catch (Exception e) {
//                 System.out.println("Notif ps4 : " + e);
//             } finally {
//                 if (rs != null) {
//                     rs.close();
//                 }
//                 if (ps4 != null) {
//                     ps4.close();
//                 }
//             }
//         } catch (SQLException ex) {
//             System.out.println(ex);
//         }

//         this.setCursor(Cursor.getDefaultCursor());
//     }
 
    
//     public void HasilLabPDFKlaimBPJSKompilasiFarmasi(String NomorRawat, String NoRekamMedis) {
//         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//         try {
//             ps4 = koneksi.prepareStatement(
//                     "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,DATE_FORMAT(periksa_lab.tgl_periksa,'%d-%m-%Y') as tgl_periksa,periksa_lab.jam,periksa_lab.nip,"
//                     + "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
//                     + " from periksa_lab inner join reg_periksa inner join pasien inner join petugas  inner join dokter inner join kelurahan inner join kecamatan inner join kabupaten "
//                     + "on periksa_lab.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_lab.nip=petugas.nip and periksa_lab.kd_dokter=dokter.kd_dokter "
//                     + "and pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where periksa_lab.kategori='PK' and "
//                     + " periksa_lab.no_rawat=? group by concat(periksa_lab.no_rawat,periksa_lab.tgl_periksa,periksa_lab.jam)");
//             try {
//                 ps4.setString(1, NomorRawat);
//                 rs = ps4.executeQuery();
//                 while (rs.next()) {
//                     kamar = Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='" + rs.getString("no_rawat") + "' order by tgl_masuk desc limit 1");
//                     if (!kamar.equals("")) {
//                         namakamar = kamar + ", " + Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "
//                                 + " where kamar.kd_kamar='" + kamar + "' ");
//                         kamar = "Kamar";
//                     } else if (kamar.equals("")) {
//                         kamar = "Poli";
//                         namakamar = Sequel.cariIsi("select nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "
//                                 + "where reg_periksa.no_rawat='" + rs.getString("no_rawat") + "'");
//                     }
//                     Map<String, Object> param = new HashMap<>();
//                     param.put("noperiksa", rs.getString("no_rawat"));
//                     param.put("norm", rs.getString("no_rkm_medis") + " | Tgl Lhr : " + rs.getString("lahir"));
//                     param.put("namapasien", rs.getString("nm_pasien"));
//                     param.put("jkel", rs.getString("jk"));
//                     param.put("umur", rs.getString("umur"));
//                     param.put("pengirim", Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", rs.getString("dokter_perujuk")));
//                     param.put("tanggal", rs.getString("tgl_periksa"));
//                     param.put("penjab", rs.getString("nm_dokter"));
//                     param.put("petugas", rs.getString("nama"));
//                     param.put("jam", rs.getString("jam"));
//                     param.put("alamat", rs.getString("alamat"));
//                     param.put("kamar", kamar);
//                     param.put("namakamar", namakamar);
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("kd_dokter"));
//                     param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nm_dokter") + "\nID " + (finger.equals("") ? rs.getString("kd_dokter") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", rs.getString("nip"));
//                     param.put("finger2", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + rs.getString("nama") + "\nID " + (finger.equals("") ? rs.getString("nip") : finger) + "\n" + rs.getString("tgl_periksa"));
//                     Sequel.queryu("truncate table temporary_lab");

//                     ps2 = koneksi.prepareStatement(
//                             "select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan,periksa_lab.biaya from periksa_lab inner join jns_perawatan_lab "
//                             + "on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? "
//                             + "and periksa_lab.jam=?");
//                     try {
//                         ps2.setString(1, rs.getString("no_rawat"));
//                         ps2.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         ps2.setString(3, rs.getString("jam"));
//                         rs2 = ps2.executeQuery();
//                         urutan = 0;
//                         while (rs2.next()) {
//                             urutan++;
//                             simpanTemporaryLabAman( "'0','" + rs2.getString("nm_perawatan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                             ps3 = koneksi.prepareStatement(
//                                     "select template_laboratorium.Pemeriksaan, detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.biaya_item,"
//                                     + "detail_periksa_lab.keterangan,detail_periksa_lab.kd_jenis_prw from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
//                                     + "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut");
//                             try {
//                                 ps3.setString(1, rs.getString("no_rawat"));
//                                 ps3.setString(2, rs2.getString("kd_jenis_prw"));
//                                 ps3.setString(3, Valid.SetTgl(rs.getString("tgl_periksa")));
//                                 ps3.setString(4, rs.getString("jam"));
//                                 rs3 = ps3.executeQuery();
//                                 while (rs3.next()) {
//                                     simpanTemporaryLabAman( "'0','  " + rs3.getString("Pemeriksaan") + "','" + rs3.getString("nilai") + "','" + rs3.getString("satuan")
//                                             + "','" + rs3.getString("nilai_rujukan") + "','" + rs3.getString("keterangan") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "Data User");
//                                 }
//                             } catch (Exception e) {
//                                 System.out.println("Notif ps3 : " + e);
//                             } finally {
//                                 if (rs3 != null) {
//                                     rs3.close();
//                                 }
//                                 if (ps3 != null) {
//                                     ps3.close();
//                                 }
//                             }
//                         }
//                     } catch (Exception e) {
//                         System.out.println("Notif ps2 : " + e);
//                     } finally {
//                         if (rs2 != null) {
//                             rs2.close();
//                         }
//                         if (ps2 != null) {
//                             ps2.close();
//                         }
//                     }

//                     param.put("namars", akses.getnamars());
//                     param.put("alamatrs", akses.getalamatrs());
//                     param.put("kotars", akses.getkabupatenrs());
//                     param.put("propinsirs", akses.getpropinsirs());
//                     param.put("kontakrs", akses.getkontakrs());
//                     param.put("emailrs", akses.getemailrs());
//                     param.put("logo", Sequel.cariGambar("select logo from setting"));
//                     pspermintaan = koneksi.prepareStatement(
//                             "select noorder,DATE_FORMAT(tgl_permintaan,'%d-%m-%Y') as tgl_permintaan,jam_permintaan from permintaan_lab where "
//                             + "no_rawat=? and tgl_hasil=? and jam_hasil=?");
//                     try {
//                         pspermintaan.setString(1, rs.getString("no_rawat"));
//                         pspermintaan.setString(2, Valid.SetTgl(rs.getString("tgl_periksa")));
//                         pspermintaan.setString(3, rs.getString("jam"));
//                         rspermintaan = pspermintaan.executeQuery();
//                         if (rspermintaan.next()) {
//                             param.put("nopermintaan", rspermintaan.getString("noorder"));
//                             param.put("tanggalpermintaan", rspermintaan.getString("tgl_permintaan"));
//                             param.put("jampermintaan", rspermintaan.getString("jam_permintaan"));
//                             Valid.MyReportPDFKlaim("rptPeriksaLabPermintaan.jasper", "report", "3PERIKSALAB", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", ""));
//                         } else {
//                             Valid.MyReportPDFKlaim("rptPeriksaLab.jasper", "report", "3PERIKSALAB", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", "") + "-" + urutan);
//                         }
//                     } catch (Exception e) {
//                         System.out.println("Notif : " + e);
//                     } finally {
//                         if (rspermintaan != null) {
//                             rspermintaan.close();
//                         }
//                         if (pspermintaan != null) {
//                             pspermintaan.close();
//                         }
//                     }
//                 }
//             } catch (Exception e) {
//                 System.out.println("Notif ps4 : " + e);
//             } finally {
//                 if (rs != null) {
//                     rs.close();
//                 }
//                 if (ps4 != null) {
//                     ps4.close();
//                 }
//             }
//         } catch (SQLException ex) {
//             System.out.println(ex);
//         }

//         this.setCursor(Cursor.getDefaultCursor());
//     }
// }
